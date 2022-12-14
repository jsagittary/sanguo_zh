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
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;

/**
 * HTTP请求到达处理handler，用于与账号服、后台服务器等之间的通讯
 *
 * @Description
 * @author TanDonghai
 *
 */
public class HttpMessageHandler extends HttpBaseChannelHandler {
    private HttpServer httpServer;
    private ByteArrayOutputStream body;

    public HttpMessageHandler(HttpServer httpServer) {
        super();
        this.httpServer = httpServer;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpContent) {
            ByteBuf in = ((HttpContent) msg).content();
            byte[] data = new byte[in.readableBytes()];
            in.readBytes(data);
            body.write(data);
            if (msg instanceof LastHttpContent) {
                Base base = PbHelper.parseFromByte(body.toByteArray());
                Base rsBase = PbHelper.createRsBase(base.getCmd() + 1, GameError.OK.getCode());
                byte[] rsData = rsBase.toByteArray();
                byte[] rsLen = PbHelper.putShort((short) rsData.length);
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
                        Unpooled.wrappedBuffer(rsLen, rsData));
                response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/octet-stream");
                response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
                ctx.write(response);
                ctx.flush();
                ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);

                httpServer.doPublicCommand(base);
                body = null;
            }
        } else if (msg instanceof HttpRequest) {
            body = new ByteArrayOutputStream();
        }
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
