/**
 * 
 */
package com.guttv.rpc.server.executor;

import com.guttv.rpc.common.RpcRequest;
import com.guttv.rpc.common.RpcResponse;

/**
 * 执行远程请求
 * @author Peter
 *
 */
public interface RpcRequestExecutor {
	
	/**
	 * 执行RPC请求
	 * @param request
	 * @return
	 * @throws Throwable
	 */
	RpcResponse invoke(RpcRequest request) throws Exception;
}
