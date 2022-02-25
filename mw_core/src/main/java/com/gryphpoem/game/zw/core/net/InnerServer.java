package com.gryphpoem.game.zw.core.net;

import com.gryphpoem.game.zw.core.Server;
import com.gryphpoem.game.zw.core.common.ChannelAttr;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.executor.OrderedQueuePoolExecutor;
import com.gryphpoem.game.zw.core.net.base.BaseChannelHandler;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.work.SendMsgWork;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CrossPb.HeartRq;
import com.gryphpoem.game.zw.pb.CrossPb.ServerRegistRq;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 跨服的客户端
 */
public abstract class InnerServer extends Server {
    private static Logger log = LogManager.getLogger(InnerServer.class);

    private static final int Link_Count = 1;
    /** 心跳发送间隔 */
    private static final int HEART_SEND_INTERVAL_SEC = 120;

    private Bootstrap bootstrap;

    AtomicInteger maxMessage = new AtomicInteger(0);
    AtomicInteger maxConnect = new AtomicInteger(0);

    public OrderedQueuePoolExecutor sendExcutor = new OrderedQueuePoolExecutor("消息发送队列", 100, -1);
    public OrderedQueuePoolExecutor recvExcutor = new OrderedQueuePoolExecutor("消息接收队列", 100, -1);

    private int selfServerId;
    /** 目标跨服id */
    private final int targetServerId;
    private String crossIp;
    private int port;
    private Channel[] sessions = new Channel[Link_Count];
    private boolean isValid = false;
    /** 重试和重连机制 */
    private final ClientTryer clientTryer;

    public InnerServer(String ip, int port, int selfServerId, int targetServerId) {
        super("InnerServer");
        this.selfServerId = selfServerId;
        this.crossIp = ip;
        this.port = port;
        this.targetServerId = targetServerId;
        this.clientTryer = new ClientTryer();
    }

    @Override
    public void run() {
        super.run();
        LogUtil.start("开始连接跨服 ,selfServerId:" + selfServerId + " ,crossIp:" + crossIp + " , crossPort:" + port);
        bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup()).channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        bootstrap.handler(new InnerChannelHandler());

        // 建立多条连接
        for (int i = 0; i < Link_Count; i++) {
            connect(i, crossIp, port);
        }
        // 心跳发送定时发送
        clientTryer.startSendHeart(() -> sendHeart(), HEART_SEND_INTERVAL_SEC);
    }

    public boolean connect(int index, String ip, int port) {
        if (bootstrap == null) {
            return false;
        }
        try {
            ChannelFuture future = bootstrap.connect(ip, port).sync();
            Channel channel = future.channel();
            channel.closeFuture().addListener(ChannelFutureListener.CLOSE);
            if (future.isSuccess() && channel.isActive()) {
                sessions[index] = channel;
                channel.attr(ChannelAttr.SESSION_INDEX).set(index);
                sendRegist(channel, index);
                LogUtil.start(
                        "连接跨服成功,serverid:" + selfServerId + " ,ip:" + ip + " , port:" + port + " , index:" + index);
                isValid = true;
                return true;
            } else {
                LogUtil.start("连接跨服失败，10s后进入重连 serverid:" + selfServerId + " ,ip:" + ip + " , port:" + port);
                retryConnect(index);
                return false;
            }
        } catch (Exception e) {
            LogUtil.error("连接跨服失败，后续定时器尝试重连 serverid:" + selfServerId + " ,ip:" + ip + " , port:" + port, e);
            retryConnect(index);
            return false;
        }
    }

    public synchronized boolean reConnect(int index) {
        if (index < 0 || index >= sessions.length) {
            LogUtil.error("index invalid,index=" + index + " ,reconn failed and exit!");
            return true;
        }

        Channel session = sessions[index];
        if (isActive(session)) {
            LogUtil.error("session is valid，index=" + index + " ,reconn ok and exit!");
            return true;
        }
        return connect(index, crossIp, port);
    }

    /**
     * 重试
     * 
     * @param index
     * @param period
     */
    public void retryConnect(int index) {
        clientTryer.ctry(this, index, 10); // 默认都是 10s后重试
    }

    /**
     * 发送消息
     * 
     * @param base
     */
    public void sendMsg(Base base) {
        sendMsg(base, base.getLordId());
    }

    private void sendMsg(Base base, long lordId) {
        int count = sessions.length;
        int random = 0;
        if (lordId > 0) {
            random = (int) (lordId % count);
        } else {
            random = (int) (Thread.currentThread().getId() % count);
        }
        Channel channel = sessions[random];

        if (!isActive(channel)) {
            for (Channel temp : sessions) {
                if (isActive(temp)) {
                    channel = temp;
                    break;
                }
            }

            if (!isActive(channel)) {
                isValid = false;
                LogUtil.error("所有 session 均断开连接");
                return;
            }
        }
        sendMsgChannel(channel, base);
    }

    private void sendMsgChannel(Channel channel, Base base) {
        if (isActive(channel)) {
            LogUtil.innerMessage(base);
            Long key = channel.attr(ChannelAttr.ID).get();
            if (key == null) {
                key = 1L;
                channel.attr(ChannelAttr.ID).set(key);
            }
            sendExcutor.addTask(key, new SendMsgWork(channel, base));
        }
    }

    public boolean isValid() {
        return this.isValid;
    }

    private boolean isActive(Channel channel) {
        return channel != null && channel.isActive();
    }

    public int getTargetServerId() {
        return targetServerId;
    }

    // 向跨服发送注册协议
    private void sendRegist(Channel channel, int index) {
        ServerRegistRq.Builder regist = ServerRegistRq.newBuilder();
        regist.setServerId(selfServerId);
        regist.setTotal(Link_Count);
        regist.setIndex(index);

        Base.Builder pb = Base.newBuilder();
        pb.setCmd(ServerRegistRq.EXT_FIELD_NUMBER);
        pb.setExtension(ServerRegistRq.ext, regist.build());
        Base base = pb.build();
        sendMsg(base);
    }

    /**
     * 心跳发送
     */
    private void sendHeart() {
        HeartRq.Builder regist = HeartRq.newBuilder();
        Base.Builder pb = Base.newBuilder();
        pb.setCmd(HeartRq.EXT_FIELD_NUMBER);
        pb.setExtension(HeartRq.ext, regist.build());
        Base base = pb.build();
        for (Channel c : sessions) {
            sendMsgChannel(c, base);
        }
    }

    protected abstract BaseChannelHandler initInnerHandler();

    public abstract void channelRead(ChannelHandlerContext ctx, Base msg);

    private class InnerChannelHandler extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            log.trace("InnerChannelHandler initChannel:" + Thread.currentThread().getId());

            ChannelPipeline pipeLine = ch.pipeline();
            pipeLine.addLast(new IdleStateHandler(360, 0, 0, TimeUnit.SECONDS));
            pipeLine.addLast(new HeartbeatHandler());
            pipeLine.addLast("frameEncoder", new LengthFieldPrepender(4));
            pipeLine.addLast("protobufEncoder", new ProtobufEncoder());
            pipeLine.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4));
            pipeLine.addLast("protobufDecoder",
                    new ProtobufDecoder(BasePb.Base.getDefaultInstance(), DataResource.getRegistry()));
            pipeLine.addLast("protobufHandler", initInnerHandler());
        }

        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            super.channelUnregistered(ctx);
            log.trace("InnnerChannelHandler channelUnregistered:" + Thread.currentThread().getId());
        }

        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
            log.trace("InnnerChannelHandler channelInactive:" + Thread.currentThread().getId());
        }

        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            log.trace("InnnerChannelHandler exceptionCaught:" + Thread.currentThread().getId());
            ctx.close();
        }
    }

    @Override
    public String getGameType() {
        return "inner";
    }

    @Override
    protected void stop() {
        if (bootstrap == null) {
            return;
        }
        try {
            clientTryer.stop();
            if (bootstrap != null) {
                bootstrap.group().shutdownGracefully().syncUninterruptibly();
            }
        } catch (Exception e) {
            LogUtil.error("close err", e);
        }
    }

    public String toString(int index) {
        return "InnerServer [serverId=" + selfServerId + ", crossIp=" + crossIp + ", port=" + port + ", index=" + index
                + "]";
    }
}
