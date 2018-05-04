/**
 * 
 */
package com.guttv.pm.platform.container;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.guttv.pm.core.bean.ExecuteContainer;
import com.guttv.pm.core.bean.Heartbeat;
import com.guttv.pm.core.cache.ConfigCache;
import com.guttv.pm.core.zk.PathConstants;
import com.guttv.pm.platform.PlatformMain;
import com.guttv.pm.utils.Constants;
import com.guttv.pm.utils.Enums.ExecuteContainerStatus;
import com.guttv.pm.utils.Utils;
import com.guttv.rpc.client.RpcProxy;
import com.guttv.rpc.client.RpcProxyFactory;
import com.guttv.rpc.client.bootstrap.BootStrapInvocationHandler;
import com.guttv.rpc.client.zk.AbstractZKInvocationHandler;
import com.guttv.rpc.client.zk.ZkWithNorespInvocHandler;
import com.guttv.rpc.client.zk.ZookeeperInvocationHandler;
import com.guttv.rpc.common.CheckPortUtil;

/**
 * @author Peter
 *
 */
public class ExecuteContainerCache {

	protected Logger logger = LoggerFactory.getLogger(ExecuteContainerCache.class);

	// 保存执行容器<clz,ExecuteContainer>
	private final Map<String, ExecuteContainer> containerMap = new HashMap<String, ExecuteContainer>();

	/**
	 * 缓存执行容器信息，如果参数中的执行容器中没有心跳信息，而旧数据里有，把旧的心跳信息放到新数据中
	 * 
	 * @param container
	 * @return
	 */
	public ExecuteContainer cacheExecuteContainer(ExecuteContainer container) {
		w.lock();
		try {
			ExecuteContainer old = containerMap.put(container.getContainerID(), container);
			if (old != null && container.getHeartbeat() == null) {
				container.setHeartbeat(old.getHeartbeat());
			}

			refreshStatus(container);

			return old;
		} finally {
			w.unlock();
		}
	}

	/**
	 * 更新容的心跳信息 更新执行容的状态
	 * 
	 * @param containerID
	 * @param heartbeat
	 * @return
	 */
	public Heartbeat refreshHeartbeat(String containerID, Heartbeat heartbeat) {

		if (heartbeat == null) {
			return null;
		}

		w.lock();
		try {
			ExecuteContainer container = containerMap.get(containerID);
			if (container == null) {
				return null;
			}

			// 先设置心跳信息
			Heartbeat old = container.getHeartbeat();
			container.setHeartbeat(heartbeat);

			// 更新状态
			refreshStatus(container);

			return old;
		} finally {
			w.unlock();
		}

	}

	public void refreshStatus(ExecuteContainer container) {

		// 禁用的不更新状态
		if (ExecuteContainerStatus.SHUTDOWN.equals(container.getStatus())
				|| ExecuteContainerStatus.FORBBIDEN.equals(container.getStatus())) {
			return;
		}

		w.lock();

		try {

			Heartbeat heartbeat = container.getHeartbeat();
			if (heartbeat == null) {

				// 只有正常状态的才更新成超时，异常 或者其它新加状态，可根据情况修改
				if (ExecuteContainerStatus.NORMAL.equals(container.getStatus()) || container.getStatus() == null) {
					container.setStatus(ExecuteContainerStatus.TIMEOUT);
				}
				return;
			}

			// 再判断是否超时
			long period = container.getHeartbeatPeriod();
			try {
				Date heartbeatTime = Utils.getTime(heartbeat.getHeartbeatTime(), Utils.DEFAULT_TIME_FORMAT1);

				// 放到此处为了修改并刷新配置后不用重启
				int timeoutPeriod = ConfigCache.getInstance().getProperty(Constants.HEARTBEAT_LOSS_COUNT, 3);

				if ((System.currentTimeMillis() - heartbeatTime.getTime()) > (timeoutPeriod * period)) {

					// 如果是正常状态才更新成超时，异常状态不再改变
					if (ExecuteContainerStatus.NORMAL.equals(container.getStatus()) || container.getStatus() == null) {

						container.setStatus(ExecuteContainerStatus.TIMEOUT);

						logger.warn("执行容器[" + container.getContainerID() + "][" + container.getIp() + "]心跳超时，上次心跳时间["
								+ heartbeat.getHeartbeatTime() + "]");
					}

				} else {
					if (container.getStatus() == null || !ExecuteContainerStatus.NORMAL.equals(container.getStatus())) {

						// 此处是发现容器心跳恢复了，超时、异常等状态都要更新成正常
						container.setStatus(ExecuteContainerStatus.NORMAL);
						container.setStatusDesc("心跳恢复：" + Utils.getCurrentTimeString());

						logger.warn("执行容器[" + container.getContainerID() + "][" + container.getIp() + "]心跳恢复...");
					}
				}

			} catch (ParseException e) {
				String error = "检查执行容器[" + container.getContainerID() + "]心跳信息时异常：" + e.getMessage();
				logger.warn(error, e);
				container.setStatus(ExecuteContainerStatus.EXCEPTION);
				container.setStatusDesc(error);
			}
		} finally {
			w.unlock();
		}

	}

	/**
	 * 删除执行容信息
	 * 
	 * @param containerID
	 * @return
	 */
	public ExecuteContainer unCacheExecuteContainer(String containerID) {
		w.lock();
		try {
			return containerMap.remove(containerID);
		} finally {
			w.unlock();
		}
	}

	/**
	 * 获取执行容信息
	 * 
	 * @param containerID
	 * @return
	 */
	public ExecuteContainer getExecuteContainer(String containerID) {
		r.lock();
		try {
			return containerMap.get(containerID);
		} finally {
			r.unlock();
		}
	}

	/**
	 * 获取所有的执行容器信息
	 * 
	 * @return
	 */
	public List<ExecuteContainer> getAllExecuteContainer() {
		List<ExecuteContainer> containers = new ArrayList<ExecuteContainer>();
		r.lock();
		try {
			containers.addAll(containerMap.values());
		} finally {
			r.unlock();
		}
		return containers;
	}

	/**
	 * 获取RPC代理
	 * 
	 * @param containerID
	 * @return
	 * @throws Exception
	 */
	public RpcProxy getZookeeperRpcProxy(String containerID, long timeout) throws Exception {
		return getZookeeperRpcProxy(containerID, timeout, true);
	}

	/**
	 * 是否需要反馈消息
	 * 
	 * @param containerID
	 * @param timeout
	 * @param hasResponse
	 *            是否要接收反馈
	 * @return
	 * @throws Exception
	 */
	public RpcProxy getZookeeperRpcProxy(String containerID, long timeout, boolean hasResponse) throws Exception {
		ExecuteContainer container = getExecuteContainer(containerID);
		if (container == null) {
			return null;
		}

		// 创建代理
		String rpcRequestPath = ZKPaths.makePath(container.getRegistPath(), PathConstants.RPC_REQEUST);
		AbstractZKInvocationHandler handler = null;
		if (hasResponse) {
			handler = new ZookeeperInvocationHandler();
			String rpcResponsePath = ZKPaths.makePath(container.getRegistPath(), PathConstants.RPC_RESPONSE);
			handler.setResponseRootPath(rpcResponsePath);
		} else {
			handler = new ZkWithNorespInvocHandler();
		}

		handler.setClient(PlatformMain.client);
		handler.setRequestRootPath(rpcRequestPath);

		// ConfigCache.getInstance().getProperty(Constants.RPC_TIMEOUT, 100000)
		handler.setTimeout(timeout);

		return RpcProxyFactory.createInvocationHandlerProxy(handler);
	}

	/**
	 * 找bootstrapRpc
	 * 
	 * @param containerID
	 * @return
	 */
	public RpcProxy getBootstrapRpcProxy(String containerID) {
		ExecuteContainer container = getExecuteContainer(containerID);
		if (container == null) {
			return null;
		}

		int port = container.getRpcServerPort();
		if (port < 1) {
			return null; // 远程没有开该服务
		}

		// 执行容器的所有IP
		String ipList = container.getIpList();
		if (StringUtils.isBlank(ipList)) {
			ipList = container.getIp();
		}

		String availableIP = getReachableIP(ipList);
		if (StringUtils.isBlank(availableIP)) {
			availableIP = container.getIp();
			// return null; //getReachableIP 通过试IP能达的方法不太灵验
		}

		// 创建代理
		long timeout = ConfigCache.getInstance().getProperty(Constants.RPC_TIMEOUT, 60000);
		BootStrapInvocationHandler handler = new BootStrapInvocationHandler(availableIP, port, timeout);
		return RpcProxyFactory.createInvocationHandlerProxy(handler);
	}

	/**
	 * 获取执行容器的springboot服务根路径
	 * 
	 * @param containerID
	 * @return
	 */
	public StringBuilder getSpringbootServerPath(String containerID) {
		ExecuteContainer container = getExecuteContainer(containerID);
		if (container == null) {
			return null;
		}

		int port = container.getServerPort();
		if (port < 1) {
			return null; // 远程没有开该服务
		}

		// 执行容器的所有IP
		String ipList = container.getIpList();
		if (StringUtils.isBlank(ipList)) {
			ipList = container.getIp();
		}

		String availableIP = getReachableIP(ipList);
		if (StringUtils.isBlank(availableIP)) {
			availableIP = container.getIp();
			// return null; //getReachableIP 通过试IP能达的方法不太灵验
		}

		// 拼接访问地址并返回
		String contentPath = container.getContextPath();
		return new StringBuilder("http://").append(availableIP).append(":").append(port)
				.append(StringUtils.isBlank(contentPath) ? "/" : (contentPath + "/"));

	}

	private String getReachableIP(String ipList) {
		if (StringUtils.isBlank(ipList)) {
			return null; // 没有取到容器IP
		}

		// 找出可用的一个
		String[] ips = ipList.split(",");
		String availableIP = null;
		for (String ip : ips) {
			// 禁用127.0.0.1
			if ("127.0.0.1".equals(ip)) {
				continue;
			}
			if (CheckPortUtil.isReachable(ip, 10000)) {
				availableIP = ip;
				break;
			}
		}
		return availableIP;
	}

	// 重入锁
	private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();

	public static ExecuteContainerCache getInstance() {
		return ExecuteContainerCache.Single.instance;
	}

	private ExecuteContainerCache() {
	}

	private static class Single {
		private static ExecuteContainerCache instance = new ExecuteContainerCache();
	}
}
