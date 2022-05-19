package com.gryphpoem.game.zw.core;

import java.io.ByteArrayOutputStream;

import com.gryphpoem.game.zw.core.net.HttpServer;
import com.gryphpoem.game.zw.core.net.base.HttpBaseChannelHandler;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

/**
 * HTTP请求到达处理handler，用于与账号服、后台服务器等之间的通讯
 *
 * @author TanDonghai
 * @Description
 */
public class HttpMessageHandler extends HttpBaseChannelHandler {
    private HttpServer httpServer;
//    private ByteArrayOutputStream body;

    public HttpMessageHandler(HttpServer httpServer) {
        super();
        this.httpServer = httpServer;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        // HttpRequest request = null;
        // if (msg instanceof HttpRequest) {
        // HttpRequest request = (HttpRequest) msg;
        //
        // String uri = request.getUri();
        // System.out.println("Uri:" + uri);
        //
        // HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(
        // new DefaultHttpDataFactory(false), request);
        //
        // }

        // HttpContent content = (HttpContent) msg;

        // System.out.println("http server read:" + );

        // ByteBuf buf = content.content();
        // byte[] packet = new byte[buf.readableBytes()];
        // buf.readBytes(packet);

        ByteBuf in = ((FullHttpRequest) msg).content();
        byte[] data = new byte[in.readableBytes()];
        in.readBytes(data);
//        if (body == null)
//            body = new ByteArrayOutputStream();
//        body.write(data);
        // System.out.println("bodyLen:"+body.toByteArray().length);
//        Base base = PbHelper.parseFromByte(body.toByteArray());
        Base base = PbHelper.parseFromByte(data);
        // buf.release();

        // String res = "I am OK";
        Base rsBase = PbHelper.createRsBase(base.getCmd() + 1, GameError.OK.getCode());
        byte[] rsData = rsBase.toByteArray();
        byte[] rsLen = PbHelper.putShort((short) rsData.length);
        // Content-Type:application/octet-stream
        // FullHttpResponse response = new
        // DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
        // HttpResponseStatus.OK,
        // Unpooled.wrappedBuffer(res.getBytes("UTF-8")));
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(rsLen, rsData));
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/octet-stream");
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
        // if (HttpHeaders.isKeepAlive(request)) {
        // response.headers().set(HttpHeaders.Names.CONNECTION,
        // Values.KEEP_ALIVE);
        // }
        ctx.write(response);
        ctx.flush();
        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);

        httpServer.doPublicCommand(base);
//        body = null;

        // Base base = PbHelper.parseFromByte(packet);
        //
        // // buf.release();
        //
        // // String res = "I am OK";
        // Base rsBase = PbHelper.createRsBase(base.getCmd() + 1,
        // GameError.OK.getCode());
        // byte[] rsData = rsBase.toByteArray();
        // byte[] rsLen = PbHelper.putShort((short) rsData.length);
        // // Content-Type:application/octet-stream
        // // FullHttpResponse response = new
        // // DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
        // // HttpResponseStatus.OK,
        // // Unpooled.wrappedBuffer(res.getBytes("UTF-8")));
        // FullHttpResponse response = new
        // DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
        // HttpResponseStatus.OK, Unpooled.wrappedBuffer(rsLen, rsData));
        // response.headers().set(HttpHeaders.Names.CONTENT_TYPE,
        // "application/octet-stream");
        // response.headers().set(HttpHeaders.Names.CONTENT_LENGTH,
        // response.content().readableBytes());
        // // if (HttpHeaders.isKeepAlive(request)) {
        // // response.headers().set(HttpHeaders.Names.CONNECTION,
        // // Values.KEEP_ALIVE);
        // // }
        // ctx.write(response);
        // ctx.flush();
        // ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        //
        // httpServer.doPublicCommand(base);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LogUtil.error("HttpServerInboundHandler exceptionCaught", cause);
        ctx.close();
    }
}
