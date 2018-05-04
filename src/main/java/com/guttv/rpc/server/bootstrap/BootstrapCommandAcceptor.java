/**
 * 
 */
package com.guttv.rpc.server.bootstrap;

import java.io.IOException;

import com.guttv.rpc.common.CheckPortUtil;
import com.guttv.rpc.common.L;
import com.guttv.rpc.common.RpcRequest;
import com.guttv.rpc.common.RpcResponse;
import com.guttv.rpc.common.bootstrap.BootstrapDecoder;
import com.guttv.rpc.common.bootstrap.BootstrapEncoder;
import com.guttv.rpc.server.accept.CommandAcceptor;
import com.guttv.rpc.server.executor.RpcRequestExecutor;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author Peter
 *
 */
public class BootstrapCommandAcceptor implements CommandAcceptor {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.guttv.rpc.server.accept.CommandAcceptor#accept()
	 */
	@Override
	public void accept() throws Exception {

		if (!CheckPortUtil.isAvailable(port)) {
			throw new Exception("port:" + port + " already in use");
		}

		new Thread() {
			public void run() {
				try {
					ServerBootstrap bootstrap = new ServerBootstrap();

					// 下面方法中的SimpleChannelInboundHandler 对象不能先new出来传进去，不能共用
					// 否则抛出异常：ChannelPipelineException xxx is not a @Sharable
					// handler,
					// so can't be added or removed multiple times.
					ChannelInitializer<SocketChannel> channelInitializer = new ChannelInitializer<SocketChannel>() {
						public void initChannel(SocketChannel channel) throws Exception {

							channel.pipeline().addLast(new BootstrapDecoder(RpcRequest.class)) // 将RPC请求进行解码（为了处理请求）
									.addLast(new BootstrapEncoder(RpcResponse.class)) // 将RPC响应进行编码（为了返回响应）
									.addLast(new ChannelCallbackServer(executor));
						}

					};

					bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
							.childHandler(channelInitializer).option(ChannelOption.SO_BACKLOG, 128)
							.childOption(ChannelOption.SO_KEEPALIVE, true);

					if (host != null && host.trim().length() > 0) {
						future = bootstrap.bind(host, port).sync();
						L.logger.info("Bootstrap已经绑定[" + host + ":" + port + "]...");

					} else {

						future = bootstrap.bind(port).sync();
						L.logger.info("Bootstrap已经绑定[" + port + "]...");

					}

					future.channel().closeFuture().sync();

					try {
						close();
					} catch (IOException e1) {
					}
				} catch (Exception e) {
					L.logger.error("Bootstrap服务监听异常：" + e.getMessage(), e);
					try {
						close();
					} catch (IOException e1) {
					}
				}
			}
		}.start();
	}

	private ChannelFuture future = null;
	private EventLoopGroup bossGroup = new NioEventLoopGroup();
	private EventLoopGroup workerGroup = new NioEventLoopGroup();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		if (future != null) {
			try {
				future.cancel(true);
			} catch (Exception e) {
			}
		}
		workerGroup.shutdownGracefully();
		bossGroup.shutdownGracefully();
	}

	public BootstrapCommandAcceptor(String host, int port, RpcRequestExecutor executor) {
		this.host = host;
		this.port = port;
		this.executor = executor;
	}

	public BootstrapCommandAcceptor(int port, RpcRequestExecutor executor) {
		this.port = port;
		this.executor = executor;
	}

	private RpcRequestExecutor executor = null;
	private String host = null;
	private int port = -1;

	public RpcRequestExecutor getExecutor() {
		return executor;
	}

	public void setExecutor(RpcRequestExecutor executor) {
		this.executor = executor;
	}

}
