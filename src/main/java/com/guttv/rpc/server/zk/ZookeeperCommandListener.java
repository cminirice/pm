/**
 * 
 */
package com.guttv.rpc.server.zk;

import java.util.concurrent.ExecutorService;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

import com.google.gson.Gson;
import com.guttv.rpc.common.L;
import com.guttv.rpc.common.RpcRequest;
import com.guttv.rpc.common.RpcResponse;
import com.guttv.rpc.common.SerializationUtil;
import com.guttv.rpc.server.RpcCommandRunnable;
import com.guttv.rpc.server.ack.CommandAck;
import com.guttv.rpc.server.executor.RpcRequestExecutor;

/**
 * @author Peter
 *
 */
public class ZookeeperCommandListener implements PathChildrenCacheListener {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.curator.framework.recipes.cache.PathChildrenCacheListener#
	 * childEvent(org.apache.curator.framework.CuratorFramework,
	 * org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent)
	 */
	@Override
	public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {

		ChildData data = event.getData();

		if (data == null) {
			return;
		}

		switch (event.getType()) {

		// 新增结点
		case CHILD_ADDED:
			// 更新结点
		case CHILD_UPDATED:
			String path = data.getPath();
			byte[] bytes = null;
			try {
				bytes = client.getData().forPath(path);
			} catch (Exception e1) {
				L.logger.debug("接收请求异常，请求可能被其它服务处理，请求路径：" + path);
				break;
			}

			if (bytes == null || bytes.length == 0 || (bytes.length == 1 && bytes[0] == 0)) {
				break;
			}

			RpcRequest request = null;
			try {
				// 为了打印日志，先反序列化出请求
				request = SerializationUtil.deserialize(bytes, RpcRequest.class);

				// 如果删除不成功，说明有其它程序已经处理了该消息
				try {
					client.delete().forPath(path);
				} catch (Exception e) {
					L.logger.info("请求[" + request.getRequestId() + "]已经被其它服务端处理，不再处理....");
					return;
				}

				L.logger.debug("准备处理请求[" + request.getRequestId() + "]消息来源[" + path + "][" + request.getHostAddr()
						+ "][" + request.getHostName() + "]...");

				// 反馈消息队列不存在的不处理
				if (request.isNeedResponse() && client.checkExists().forPath(request.getResponsePath()) == null) {
					L.logger.warn(
							"请求[" + request.getRequestId() + "]的反馈队列[" + request.getResponsePath() + "]已经不不存在，丢弃该消息");
					break;
				}

				L.logger.debug("请求[" + request.getRequestId() + "]调用[" + request.getClassName() + "]的["
						+ request.getMethodName() + "]方法，参数列表：" + gson.toJson(request.getParameters()));

				// 时间长的不处理
				long createTime = data.getStat().getCtime();
				long diff = System.currentTimeMillis() - createTime;
				if (diff > ignoreTimeout) {
					L.logger.warn("请求[" + request.getRequestId() + "]消息已经超时[" + diff + "]");
					break;
				}

				// 启动线程池执行
				threadPool.submit(new RpcCommandRunnable(request, executor, commandAck));

			} catch (Exception e) {
				L.logger.error(e.getMessage(), e);

				// 尝试返回一个异常
				if (request != null) {
					try {
						RpcResponse response = new RpcResponse();
						response.setRequestId(request.getRequestId());
						response.setError(e);
						commandAck.ack(request, response);
					} catch (Throwable e1) {
						L.logger.error("尝试反馈消息[" + request.getRequestId() + "]异常：" + e1.getMessage());
					}
				}
			}
			break;
		case CHILD_REMOVED:
			break;
		default:
			break;
		}
	}

	public ZookeeperCommandListener(RpcRequestExecutor executor, ExecutorService threadPool, CommandAck commandAck,
			long ignoreTimeout) {
		this.setCommandAck(commandAck);
		this.setExecutor(executor);
		this.setThreadPool(threadPool);
		this.setIgnoreTimeout(ignoreTimeout);
	}

	private CommandAck commandAck = null;
	private RpcRequestExecutor executor = null;
	private final Gson gson = new Gson();

	/**
	 * 收到的消息，如果创建时间太长，就不再处理
	 */
	private long ignoreTimeout = 60000;

	public long getIgnoreTimeout() {
		return ignoreTimeout;
	}

	public void setIgnoreTimeout(long ignoreTimeout) {
		this.ignoreTimeout = ignoreTimeout;
	}

	private ExecutorService threadPool = null;

	public ExecutorService getThreadPool() {
		return threadPool;
	}

	public void setThreadPool(ExecutorService threadPool) {
		this.threadPool = threadPool;
	}

	public CommandAck getCommandAck() {
		return commandAck;
	}

	public void setCommandAck(CommandAck commandAck) {
		this.commandAck = commandAck;
	}

	public RpcRequestExecutor getExecutor() {
		return executor;
	}

	public void setExecutor(RpcRequestExecutor executor) {
		this.executor = executor;
	}

}
