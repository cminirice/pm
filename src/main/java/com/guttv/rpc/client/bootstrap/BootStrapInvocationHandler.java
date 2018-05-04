/**
 * 
 */
package com.guttv.rpc.client.bootstrap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.guttv.rpc.common.RandomStringUtils;
import com.guttv.rpc.common.RpcRequest;
import com.guttv.rpc.common.RpcResponse;

/**
 * @author Peter
 *
 */
public class BootStrapInvocationHandler implements InvocationHandler {

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		RpcRequest request = new RpcRequest(); // 创建并初始化 RPC 请求
		request.setRequestId(RandomStringUtils.uuid());
		request.setClassName(method.getDeclaringClass().getName());
		request.setMethodName(method.getName());
		request.setParameterTypes(method.getParameterTypes());
		request.setParameters(args);

		ChannelCallbackClient callback = new ChannelCallbackClient(host, port, timeout);
		RpcResponse response = callback.send(request);

		if (response.getError() != null) {
			throw response.getError();
		} else {
			return response.getResult();
		}
	}

	public BootStrapInvocationHandler(String host, int port, long timeout) {
		this.host = host;
		this.port = port;
		this.timeout = timeout;
	}

	public BootStrapInvocationHandler(String host, int port) {
		this.host = host;
		this.port = port;
	}

	private String host = null;
	private int port = -1;
	private long timeout = 60000;

}
