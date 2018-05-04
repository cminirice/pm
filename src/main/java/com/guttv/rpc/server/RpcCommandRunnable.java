/**
 * 
 */
package com.guttv.rpc.server;

import com.guttv.rpc.common.L;
import com.guttv.rpc.common.RpcRequest;
import com.guttv.rpc.server.ack.CommandAck;
import com.guttv.rpc.server.executor.RpcRequestExecutor;

/**
 * @author Peter
 *
 */
public class RpcCommandRunnable implements Runnable {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			L.logger.info("开始处理请求[" + request.getRequestId() + "]...");
			commandAck.ack(request, executor.invoke(request));
		} catch (Throwable e) {
			L.logger.error(e.getMessage(), e);
		}
	}

	public RpcCommandRunnable() {
	}

	public RpcCommandRunnable(RpcRequest request, RpcRequestExecutor executor, CommandAck commandAck) {
		this.setRequest(request);
		this.setExecutor(executor);
		this.setCommandAck(commandAck);
	}

	private RpcRequest request = null;
	private RpcRequestExecutor executor = null;
	private CommandAck commandAck = null;

	public RpcRequestExecutor getExecutor() {
		return executor;
	}

	public void setExecutor(RpcRequestExecutor executor) {
		this.executor = executor;
	}

	public CommandAck getCommandAck() {
		return commandAck;
	}

	public void setCommandAck(CommandAck commandAck) {
		this.commandAck = commandAck;
	}

	public RpcRequest getRequest() {
		return request;
	}

	public void setRequest(RpcRequest request) {
		this.request = request;
	}
}
