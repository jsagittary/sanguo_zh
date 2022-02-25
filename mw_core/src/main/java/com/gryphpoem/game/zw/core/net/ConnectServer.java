package com.gryphpoem.game.zw.core.net;

import com.gryphpoem.game.zw.core.Server;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.executor.DelayOrderQueuePoolExecutor;
import com.gryphpoem.game.zw.core.executor.NonOrderedQueuePoolExecutor;
import com.gryphpoem.game.zw.core.executor.OrderedQueuePoolExecutor;
import com.gryphpoem.game.zw.core.net.base.BaseChannelHandler;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ConnectServer extends Server {

    // GameServer gameServer;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    public GlobalTrafficShapingHandler trafficShapingHandler;
    ServerBootstrap bootstrap;

    public AtomicInteger maxMessage = new AtomicInteger(0);
    public AtomicInteger maxConnect = new AtomicInteger(0);
    public OrderedQueuePoolExecutor sendExcutor = new OrderedQueuePoolExecutor("消息发送队列", 100, -1);
    public OrderedQueuePoolExecutor recvExcutor = new OrderedQueuePoolExecutor("消息接收队列", 100, -1);
    public NonOrderedQueuePoolExecutor actionExcutor = new NonOrderedQueuePoolExecutor(500);
    public DelayOrderQueuePoolExecutor delayExecutor = new DelayOrderQueuePoolExecutor("delayTask-", this);

    public static int MAX_CONNECT = 20000;

    private int port;

    public ConnectServer(int port) {
        super("ConnectServer");
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    protected void stop() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        recvExcutor.shutdown();
        if (delayExecutor != null) {
            delayExecutor.stopped();
        }
    }

    @Override
    public void run() {
        super.run();

        // 定义两个工作线程 bossGroup workerGroup 用于管理channel连接
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        bootstrap = new ServerBootstrap();
        trafficShapingHandler = new GlobalTrafficShapingHandler(workerGroup, 5000L);
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        // 通过NoDelay禁用Nagle,使消息立即发出去，不用等待到一定的数据量才发出去
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.childHandler(new ConnectChannelHandler());
        bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
        // bootstrap.childOption(ChannelOption.ALLOCATOR, new
        // PooledByteBufAllocator(false));
        // bootstrap.childOption(ChannelOption.SO_RCVBUF, 1048576);
        // bootstrap.childOption(ChannelOption.SO_SNDBUF, 1048576);

        ChannelFuture f;
        try {
            // 绑定端口，同步等待成功
            System.out.println("port:" + port);
            f = bootstrap.bind(port).sync();

            // 等待服务端监听端口关闭
            f.channel().closeFuture().sync();

            // System.out.println("connect server init port success!");
        } catch (InterruptedException e) {
            LogUtil.error("服务器启动绑定端口异常", e);
        }
    }

    private class ConnectChannelHandler extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            // initChannel:" + Thread.currentThread().getId());
            LogUtil.channel("ConnectChannelHandler initChannel:" + Thread.currentThread().getId());

            // System.out.println(Thread.currentThread().getId());
            // InetSocketAddress address = (InetSocketAddress)
            // ch.remoteAddress();
            // System.out.println("ip:" +
            // address.getAddress().getHostAddress());
            // System.out.println("port:" + address.getPort());

            ChannelPipeline pipeLine = ch.pipeline();
            pipeLine.addLast(trafficShapingHandler);
            // 心跳 360秒查看一次在线的客户端channel是否空闲
            pipeLine.addLast(new IdleStateHandler(360, 0, 0, TimeUnit.SECONDS));
            pipeLine.addLast(new HeartbeatHandler());
            pipeLine.addLast("frameEncoder", new LengthFieldPrepender(4));
            pipeLine.addLast("protobufEncoder", new ProtobufEncoder());

            pipeLine.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4));
            pipeLine.addLast("protobufDecoder",
                    new ProtobufDecoder(BasePb.Base.getDefaultInstance(), DataResource.getRegistry()));
            pipeLine.addLast("protobufHandler", initGameServerHandler());
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            super.channelUnregistered(ctx);
            // channelUnregistered:" + Thread.currentThread().getId());
            LogUtil.channel("ConnectChannelHandler channelUnregistered:" + Thread.currentThread().getId());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
            // channelInactive:" + Thread.currentThread().getId());
            LogUtil.channel("ConnectChannelHandler channelInactive:" + Thread.currentThread().getId());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            // exceptionCaught:" + Thread.currentThread().getId());
            System.out.println("error:" + cause.getMessage());
            LogUtil.channel("ConnectChannelHandler exceptionCaught:" + Thread.currentThread().getId());
            ctx.close();
        }
    }

    /**
     * 初始化处理客户端协议交互的handler，在创建ConnectServer对象是，必须实现该方法
     *
     * @return
     */
    protected abstract BaseChannelHandler initGameServerHandler();

    /**
     * 协议到达后的读取操作
     *
     * @param ctx
     * @param msg
     */
    public abstract void channelRead(ChannelHandlerContext ctx, Base msg);

    @Override
    public String getGameType() {
        return "connect";
    }
}

class HeartbeatHandler extends ChannelDuplexHandler {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            if (e.state() == IdleState.READER_IDLE) {
                LogUtil.channel("HeartbeatHandler trigger READER_IDLE");
                ctx.close();
            }
        }
    }
}
