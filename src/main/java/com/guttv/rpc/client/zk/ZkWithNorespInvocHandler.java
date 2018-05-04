package com.guttv.rpc.client.zk;

import java.lang.reflect.Method;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;

import com.guttv.rpc.common.L;
import com.guttv.rpc.common.RandomStringUtils;
import com.guttv.rpc.common.RpcRequest;
import com.guttv.rpc.common.SerializationUtil;

public class ZkWithNorespInvocHandler extends AbstractZKInvocationHandler {

	private String requestRootPath = null;
	private CuratorFramework client = null;

	@Override
	public Object invoke(Object proxy, Method method, Object[] parameters) throws Throwable {

		// 包装请求
		final RpcRequest request = new RpcRequest();

		// 不能用此方法，proxy是自动生成的代理类
		// request.setClassName(proxy.getClass().getName());

		request.setClassName(method.getDeclaringClass().getName());
		request.setMethodName(method.getName());
		request.setParameters(parameters);
		request.setParameterTypes(method.getParameterTypes());
		request.setRequestId(RandomStringUtils.uuid());

		request.setNeedResponse(false); // 这个地方标记不需要反馈
		String reqPath = null;

		try {

			// 创建消息节点,并发送数据
			reqPath = client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
					.forPath(ZKPaths.makePath(requestRootPath, "request"), SerializationUtil.serialize(request));

			L.logger.info("请求ID[" + request.getRequestId() + "]对应的请求队列[" + reqPath + "]，已经配置没有反馈消息，该请求将反馈null");

			return null;
		} finally {
			try {
				// 没有反馈的消息，不能试着删除请求路径，会导致服务端读不到数据
				// client.delete().deletingChildrenIfNeeded().forPath(reqPath);
			} catch (Exception e) {
				// L.logger.warn("请求[" + request.getRequestId() + "]在删除路径[" +
				// reqPath + "]时异常：" + e.getMessage());
			}
		}
	}

	public String getRequestRootPath() {
		return requestRootPath;
	}

	public void setRequestRootPath(String requestRootPath) {
		this.requestRootPath = requestRootPath;
	}

	public CuratorFramework getClient() {
		return client;
	}

	public void setClient(CuratorFramework client) {
		this.client = client;
	}

	public long getTimeout() {
		return -1;
	}

	public void setTimeout(long timeout) {
	}

}
