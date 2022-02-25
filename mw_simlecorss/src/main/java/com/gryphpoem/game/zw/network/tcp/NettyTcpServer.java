package com.gryphpoem.game.zw.network.tcp;

import java.util.concurrent.TimeUnit;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pb.BasePb;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
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

/**
 * @ClassName NettyTcpServer.java
 * @Description
 * @author QiuKun
 * @date 2019年4月29日
 */
public class NettyTcpServer {

    public static interface ChannelHandlerFactory {
        ChannelHandler create();
    }

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private GlobalTrafficShapingHandler trafficShapingHandler;

    protected final int serverPort;
    private ChannelHandlerFactory channelHandlerFactory;

    public NettyTcpServer(int serverPort, ChannelHandlerFactory channelHandlerFactory) {
        this.serverPort = serverPort;
        this.channelHandlerFactory = channelHandlerFactory;
    }

    public boolean startService() {
        boolean serviceFlag = true;
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        trafficShapingHandler = new GlobalTrafficShapingHandler(workerGroup, 5000L);
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class);
        // TIME_WAIT时可重用端口，服务器关闭后可立即重启
        b.option(ChannelOption.SO_REUSEADDR, true);
        // 设置了ServerSocket类的SO_RCVBUF选项，就相当于设置了Socket对象的接收缓冲区大小，64KB
        b.option(ChannelOption.SO_RCVBUF, 1024 * 64);
        // 请求连接的最大队列长度，如果backlog参数的值大于操作系统限定的队列的最大长度，那么backlog参数无效
        b.option(ChannelOption.SO_BACKLOG, 1024);
        // 使用内存池的缓冲区重用机制
        b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        // 当客户端发生断网或断电等非正常断开的现象，如果服务器没有设置SO_KEEPALIVE选项，则会一直不关闭SOCKET。具体的时间由OS配置
        b.childOption(ChannelOption.SO_KEEPALIVE, true);
        // 在调用close方法后，将阻塞n秒，让未完成发送的数据尽量发出，NETTY中这部分操作调用方法异步进行。我们的游戏业务没有这种需要，所以设置为0
        b.childOption(ChannelOption.SO_LINGER, 0);
        // 数据包不缓冲,立即发出
        b.childOption(ChannelOption.TCP_NODELAY, true);
        // 发送缓冲大小，默认8192
        b.childOption(ChannelOption.SO_SNDBUF, 1024 * 64);
        // 使用内存池的缓冲区重用机制
        b.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        b.childHandler(new SockerChannelInitializer());

        try {
            ChannelFuture cf = b.bind(serverPort).sync();
            cf.channel().closeFuture().addListener(ChannelFutureListener.CLOSE);
            LogUtil.start("tcp服务启成功 port:" + serverPort);
        } catch (Exception e) {
            LogUtil.error("服务器启动绑定端口异常", e);
            serviceFlag = false;
        }
        return serviceFlag;
    }

    public boolean stopService() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        return true;
    }

    private class SockerChannelInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeLine = ch.pipeline();
            pipeLine.addLast("traffic", trafficShapingHandler);
            pipeLine.addLast("idleState", new IdleStateHandler(300, 0, 0, TimeUnit.SECONDS));
            pipeLine.addLast(new HeartbeatHandler());

            pipeLine.addLast("frameEncoder", new LengthFieldPrepender(4));
            pipeLine.addLast("protobufEncoder", new ProtobufEncoder());

            pipeLine.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4));
            pipeLine.addLast("protobufDecoder",
                    new ProtobufDecoder(BasePb.Base.getDefaultInstance(), DataResource.getRegistry()));
            pipeLine.addLast("protobufHandler", channelHandlerFactory.create());
        }
    }

    private class HeartbeatHandler extends ChannelDuplexHandler {
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
}
