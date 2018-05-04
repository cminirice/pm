/**
 * 
 */
package com.guttv.rpc.client.zk;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;

import com.guttv.rpc.common.L;
import com.guttv.rpc.common.RandomStringUtils;
import com.guttv.rpc.common.RpcRequest;
import com.guttv.rpc.common.RpcResponse;
import com.guttv.rpc.common.SerializationUtil;

/**
 * @author Peter
 *
 */
public class ZookeeperInvocationHandler extends AbstractZKInvocationHandler {

	private String requestRootPath = null;
	private String responseRootPath = null;
	private CuratorFramework client = null;
	private long timeout = 30000;

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

		String respPath = responseRootPath + "/" + request.getRequestId();
		L.logger.info("创建反馈消息路径：" + respPath);
		String reqPath = null;

		try {

			// 先准备好返回消息节点 返回消息结点由客户端创建，并由客户端删除
			// 创建节点的时候发送一个空数据，不设置数据的话，会默认把系统的IP发过去
			client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(respPath,
					new byte[] { 0 });
			L.logger.debug("反馈消息路径[" + respPath + "]创建成功。");
			request.setResponsePath(respPath);

			// 记数
			final CountDownLatch latch = new CountDownLatch(1);

			// 添加反馈消息节点监听
			final List<RpcResponse> result = new ArrayList<RpcResponse>(1);
			final NodeCache nodeCache = new NodeCache(client, respPath);

			NodeCacheListener nodeCacheListener = new NodeCacheListener() {
				@Override
				public void nodeChanged() throws Exception {
					ChildData childData = nodeCache.getCurrentData();

					if (childData == null) {
						// 这个地方有可能是队列被删除了
						String errorMsg = "反馈消息队列被删除" + request.getResponsePath();
						L.logger.error(errorMsg);
						RpcResponse response = new RpcResponse();
						response.setRequestId(request.getRequestId());
						response.setError(new Exception(errorMsg));
						result.add(response);
						closeQuiet();
						return;
					}
					byte[] data = childData.getData();
					if (data == null || (data.length == 1 && data[0] == 0)) {
						// 到这里的数据是创建节点时，发送的默认数据
						return;
					}
					try {
						result.add(SerializationUtil.deserialize(childData.getData(), RpcResponse.class));
					} catch (Throwable error) {
						L.logger.error("反序列化反馈请求时异常：" + error.getMessage(), error);
						RpcResponse response = new RpcResponse();
						response.setRequestId(request.getRequestId());
						response.setError(error);
						result.add(response);
					} finally {
						closeQuiet();
					}

				}

				private void closeQuiet() {
					latch.countDown();
					if (nodeCache != null) {
						try {
							nodeCache.close();
						} catch (Exception e) {
						}
					}
				}
			};
			nodeCache.getListenable().addListener(nodeCacheListener);
			nodeCache.start();

			// 创建消息节点,并发送数据
			reqPath = client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
					.forPath(ZKPaths.makePath(requestRootPath, "request"), SerializationUtil.serialize(request));

			L.logger.info("请求ID[" + request.getRequestId() + "]对应的请求队列[" + reqPath + "]，反馈消息队列[" + respPath + "]");

			latch.await(timeout, TimeUnit.MILLISECONDS);

			// 关闭监听
			if (nodeCache != null) {
				try {
					nodeCache.close();
				} catch (Exception e) {
				}
			}

			if (result.size() == 0) {
				throw new TimeoutException("超过[" + timeout + "]时间没有收到反馈消息");
			}

			RpcResponse response = result.get(0);
			L.logger.debug("[" + response.getRequestId() + "]请求收到反馈，处理IP["+response.getHostAddr()+"]["+response.getHostName()+"]");

			Throwable error = response.getError();
			if (error != null) {
				L.logger.error("[" + response.getRequestId() + "]请求处理异常：" + error.getMessage());
				throw error;
			}

			return response.getResult();
		} finally {
			// 删除反馈消息节点
			try {
				client.delete().deletingChildrenIfNeeded().forPath(respPath);
			} catch (Exception e) {
				// L.logger.warn("请求[" + request.getRequestId() + "]在删除路径[" +
				// respPath + "]时异常：" + e.getMessage());
			}
			try {
				client.delete().deletingChildrenIfNeeded().forPath(reqPath);
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

	public String getResponseRootPath() {
		return responseRootPath;
	}

	public void setResponseRootPath(String responseRootPath) {
		this.responseRootPath = responseRootPath;
	}

	public CuratorFramework getClient() {
		return client;
	}

	public void setClient(CuratorFramework client) {
		this.client = client;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

}
