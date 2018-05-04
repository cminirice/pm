/**
 * 
 */
package com.guttv.rpc.server.ack;

import com.guttv.rpc.common.RpcRequest;
import com.guttv.rpc.common.RpcResponse;

/**
 * @author Peter
 *
 */
public class DonothingCommandAck implements CommandAck {

	/* (non-Javadoc)
	 * @see com.guttv.rpc.server.ack.CommandAck#ack(com.guttv.rpc.common.RpcRequest, com.guttv.rpc.common.RpcResponse)
	 */
	@Override
	public void ack(RpcRequest request, RpcResponse response) throws Throwable {

	}

}
