package com.aervon.java.support.rpc.core.binder;

import com.aervon.java.support.rpc.core.Constants;
import com.aervon.java.support.rpc.core.RpcRequest;
import com.aervon.java.support.rpc.core.RpcResponse;
import com.aervon.java.support.rpc.core.utils.JsonUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

/**
 * <p> Project: NettyRpc </p>
 * <p> Version: 1.0 </p>
 * <p> File: RpcNettyServiceHandler.java </p>
 * <p> Description: RPC服务端Handler. </p>
 * <p> Author: Aervon </p>
 * <p> Date: 2019/9/28 </p>
 */
public abstract class RpcNettyServiceHandler extends SimpleChannelInboundHandler<HttpMessage> {

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, HttpMessage msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            RpcRequest rpcRequest = getRequestBody((FullHttpRequest) msg);
            channelReceived(ctx, rpcRequest);
        }
    }

    protected abstract void channelReceived(ChannelHandlerContext ctx, RpcRequest rpcRequest);

    private RpcRequest getRequestBody(FullHttpRequest httpRequest) {
        ByteBuf byteBuf = httpRequest.content();
        String body = byteBuf.toString(CharsetUtil.UTF_8);
        return JsonUtils.fromJson(body, RpcRequest.class);
    }

    /**
     * 将RPC回复转换成HTTP回复，并发送
     */
    protected void sendResponse(ChannelHandlerContext ctx, RpcResponse rpcResponse) {
        rpcResponse.setJsonrpc(Constants.JSON_RPC_VERSION);
        String msg = JsonUtils.toJson(rpcResponse);
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_0, HttpResponseStatus.OK, Unpooled.wrappedBuffer(msg.getBytes(CharsetUtil.UTF_8)));
        response.headers()
                .set(HttpHeaders.Names.CONTENT_TYPE, "text/plain;charset=UTF-8")
                .set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE)
                .set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
