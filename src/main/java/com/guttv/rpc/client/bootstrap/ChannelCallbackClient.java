/**
 * 
 */
package com.guttv.rpc.client.bootstrap;

import java.util.concurrent.TimeoutException;

import com.guttv.rpc.common.L;
import com.guttv.rpc.common.RpcRequest;
import com.guttv.rpc.common.RpcResponse;
import com.guttv.rpc.common.bootstrap.BootstrapDecoder;
import com.guttv.rpc.common.bootstrap.BootstrapEncoder;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author Peter
 *
 */
public class ChannelCallbackClient extends SimpleChannelInboundHandler<RpcResponse> {

	private RpcResponse response = null;
	private Object lock = new byte[0];

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
		this.response = response;
		synchronized (lock) {
			lock.notifyAll(); // 收到响应，唤醒线程
		}

		if (ctx != null) {
			ctx.close();
		}
	}

	public RpcResponse send(RpcRequest request) throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel channel) throws Exception {
					channel.pipeline().addLast(new BootstrapEncoder(RpcRequest.class)) // 将RPC请求进行编码（为了发送请求）
							.addLast(new BootstrapDecoder(RpcResponse.class)) // 将RPC响应进行解码（为了处理响应）
							.addLast(ChannelCallbackClient.this); // 使用RpcClient发送RPC请求
				}
			}).option(ChannelOption.SO_KEEPALIVE, true);

			L.logger.debug("将要发送请求[" + request.getRequestId() + "]至[" + host + ":" + port + "]");
			ChannelFuture future = bootstrap.connect(host, port).sync();
			future.channel().writeAndFlush(request).sync();

			synchronized (lock) {
				lock.wait(timeout); // 未收到响应，使线程等待
			}

			if (response != null) {
				future.channel().closeFuture().sync();
				L.logger.debug("请求[" + request.getRequestId() + "]处理完成，处理IP[" + response.getHostAddr() + "]");
			} else {
				if (response == null) {
					response = new RpcResponse();
				}
				// 包装异常返回
				response.setError(new TimeoutException("超过[" + timeout + "]时间没有收到响应"));
			}

			return response;

		} finally {
			group.shutdownGracefully();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (response == null) {
			response = new RpcResponse();
		}

		// 包装异常返回
		response.setError(cause);

		synchronized (lock) {
			lock.notifyAll(); // 收到响应，唤醒线程
		}

		if (ctx != null) {
			ctx.close();
		}
	}

	public ChannelCallbackClient(String host, int port, long timeout) {
		this.host = host;
		this.port = port;
		this.timeout = timeout;
	}

	private String host = null;
	private int port = -1;
	private long timeout = 60000;
}
