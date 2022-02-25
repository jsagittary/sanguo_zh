package com.gryphpoem.game.zw.core.net;

import com.gryphpoem.game.zw.core.Server;
import com.gryphpoem.game.zw.core.executor.NonOrderedQueuePoolExecutor;
import com.gryphpoem.game.zw.core.executor.OrderedQueuePoolExecutor;
import com.gryphpoem.game.zw.core.handler.AbsHttpHandler;
import com.gryphpoem.game.zw.core.message.MessagePool;
import com.gryphpoem.game.zw.core.net.base.HttpBaseChannelHandler;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pb.BasePb.Base;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public abstract class HttpServer extends Server {
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	public OrderedQueuePoolExecutor sendExcutor = new OrderedQueuePoolExecutor("消息发送队列", 100, -1);
	public NonOrderedQueuePoolExecutor publicActionExcutor = new NonOrderedQueuePoolExecutor(500);

	private int port;

	public HttpServer(int port) {
		super("HttpServer");
		this.port = port;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public String getGameType() {
		return "http";
	}

	@Override
	protected void stop() {
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
	}

	public void run() {
		super.run();

		bossGroup = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) throws Exception {
							// server端发送的是httpResponse，所以要使用HttpResponseEncoder进行编码
							ch.pipeline().addLast(new HttpResponseEncoder());
							// server端接收到的是httpRequest，所以要使用HttpRequestDecoder进行解码
							ch.pipeline().addLast(new HttpRequestDecoder());
							ch.pipeline().addLast(initHandler());
						}
					}).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE,
							true)/*
									 * .childOption(ChannelOption. RCVBUF_ALLOCATOR, new
									 * AdaptiveRecvByteBufAllocator(64, 1024 * 2, 65536))
									 */;

			ChannelFuture f = b.bind(port).sync();

			f.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			LogUtil.error("Http Server start Exception", e);
		}
	}

	/**
	 * HTTP协议到达处理handler的初始化，创建HttpServer对象时，必须实现该方法
	 * 
	 * @return
	 */
	public abstract HttpBaseChannelHandler initHandler();

	public void doPublicCommand(Base msg) {

		int cmd = msg.getCmd();
		AbsHttpHandler handler;
		try {
			LogUtil.s2sMessage(msg);

			handler = MessagePool.getIns().getHttpHandler(cmd);
			if (handler != null) {
				handler.setMsg(msg);
				publicActionExcutor.execute(handler);
			}

		} catch (Exception e) {
			LogUtil.error("与帐号服之间的消息逻辑放入线程池中执行异常", e);
		}
	}
}
