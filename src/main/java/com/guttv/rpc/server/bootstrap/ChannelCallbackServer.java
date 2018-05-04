/**
 * 
 */
package com.guttv.rpc.server.bootstrap;

import java.net.InetSocketAddress;

import com.google.gson.Gson;
import com.guttv.rpc.common.L;
import com.guttv.rpc.common.RpcRequest;
import com.guttv.rpc.server.executor.RpcRequestExecutor;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author Peter
 *
 */
public class ChannelCallbackServer extends SimpleChannelInboundHandler<RpcRequest> {

	private final Gson gson = new Gson();
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
		// 获取请求来源IP
		InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
		String clientIP = insocket.getAddress().getHostAddress();

		L.logger.debug("收到来自[" + clientIP + "]的请求[" + request.getRequestId() + "]...");
		L.logger.debug("请求[" + request.getRequestId() + "]调用[" + request.getClassName() + "]的["
				+ request.getMethodName() + "]方法，参数列表：" + gson.toJson(request.getParameters()));

		ctx.writeAndFlush(executor.invoke(request)).addListener(ChannelFutureListener.CLOSE);

		L.logger.info("消息[" + request.getRequestId() + "]反馈完毕。");
	}

	public ChannelCallbackServer() {
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		ctx.writeAndFlush(cause).addListener(ChannelFutureListener.CLOSE);
		super.exceptionCaught(ctx, cause);
	}

	public ChannelCallbackServer(RpcRequestExecutor executor) {
		this.executor = executor;
	}

	private RpcRequestExecutor executor = null;

	public RpcRequestExecutor getExecutor() {
		return executor;
	}

	public void setExecutor(RpcRequestExecutor executor) {
		this.executor = executor;
	}
}
