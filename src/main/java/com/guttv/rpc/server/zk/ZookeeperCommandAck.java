/**
 * 
 */
package com.guttv.rpc.server.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.KeeperException;

import com.guttv.rpc.common.L;
import com.guttv.rpc.common.RpcRequest;
import com.guttv.rpc.common.RpcResponse;
import com.guttv.rpc.common.SerializationUtil;
import com.guttv.rpc.server.ack.CommandAck;

/**
 * @author Peter
 *
 */
public class ZookeeperCommandAck implements CommandAck {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.guttv.rpc.server.ack.CommandAck#ack(com.guttv.rpc.common.RpcRequest,
	 * com.guttv.rpc.common.RpcResponse)
	 */
	@Override
	public void ack(RpcRequest request, RpcResponse response) throws Throwable {
		if (request.isNeedResponse()) {
			L.logger.debug("开始反馈[" + request.getRequestId() + "]对应的结果...");
			try {
				client.setData().forPath(request.getResponsePath(), SerializationUtil.serialize(response));
				L.logger.debug("[" + request.getRequestId() + "]反馈结果成功");
			} catch (KeeperException.NoNodeException e) {
				L.logger.debug("反馈消息的路径已经不存在：" + e.getMessage());
			}
		} else {

			// 如果全部是不需要反馈的请求，在new ZookeeperCommandAcceptor后，set
			// DonothingCommandAck 即可
			L.logger.debug("请求[" + request.getRequestId() + "]设置不需要反馈");
		}
	}

	private CuratorFramework client = null;

	public ZookeeperCommandAck() {
	}

	public ZookeeperCommandAck(CuratorFramework client) {
		this.client = client;
	}

	public CuratorFramework getClient() {
		return client;
	}

	public void setClient(CuratorFramework client) {
		this.client = client;
	};
}
