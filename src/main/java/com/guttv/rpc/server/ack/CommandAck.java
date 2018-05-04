package com.guttv.rpc.server.ack;

import com.guttv.rpc.common.RpcRequest;
import com.guttv.rpc.common.RpcResponse;

public interface CommandAck {

	/**
	 * 反馈消息
	 * @param request
	 * @param response
	 * @throws Throwable
	 */
	void ack(RpcRequest request,RpcResponse response) throws Throwable;
}
