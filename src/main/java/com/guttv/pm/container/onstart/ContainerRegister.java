/**
 *
 */
package com.guttv.pm.container.onstart;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import com.google.gson.Gson;
import com.guttv.pm.container.FlowExecuteContainerMain;
import com.guttv.pm.core.bean.ExecuteContainer;
import com.guttv.pm.core.cache.ConfigCache;
import com.guttv.pm.core.zk.PathConstants;
import com.guttv.pm.core.zk.SingletonLock;
import com.guttv.pm.core.zk.ZookeeperHelper;
import com.guttv.pm.utils.Constants;
import com.guttv.pm.utils.Enums.ExecuteContainerStatus;
import com.guttv.pm.utils.HttpUtil;
import com.guttv.pm.utils.MD5Util;
import com.guttv.pm.utils.Utils;

/**
 * @author Peter
 *
 */
public class ContainerRegister {

	protected static Logger logger = LoggerFactory.getLogger(ContainerRegister.class);

	void regist(CuratorFramework client, ApplicationContext context, int serverPort, int rpcServerPort) {
		String registPath = getContainerRegistPath();

		// 自从启动端口随便启后，就要给心跳加锁
		SingletonLock lock = new SingletonLock(registPath);
		lock.setClient(client);
		if (!lock.lock()) {
			logger.warn("没有获取到执行容锁，初步判断是重复启动，或者旧进程未关闭，终止本次启动。");
			System.exit(-1);
			return;
		}

		// 设置全局锁
		this.lock = lock;
		this.client = client;
		this.context = context;
		this.serverPort = serverPort;
		this.rpcServerPort = rpcServerPort;

		Gson gson = new Gson();

		ExecuteContainer container = null;
		try {

			// 下面是更新注册或者更新容器信息，下面是基本固定的信息

			if (client.checkExists().forPath(registPath) != null) {

				// 这里是以前已经启动过程序
				try {
					byte[] data = client.getData().forPath(registPath);
					String json = new String(data, Constants.ENCODING);
					container = gson.fromJson(json, ExecuteContainer.class);

				} catch (Exception e) {
					logger.error("从ZK上获取容器信息异常，将重新注册：" + e.getMessage(), e);
				}
			}

			if (container == null) {

				String ipList = HttpUtil.getIPList();
				String ip = HttpUtil.getLocalIP();
				String location = Utils.getProjectLocation();

				// 新添加的执行容器，或者需要重新建的
				container = new ExecuteContainer();
				container.setCreateTime(Utils.getCurrentTimeString());
				container.setIpList(ipList);
				container.setContainerID(getContainerID());
				container.setIp(ip);
				container.setLocation(location);
				// 如果是从服务器上获取的心跳周期，不用重新设置
				long heartbeatPeriod = ConfigCache.getInstance().getProperty(Constants.HEARTBEAT_PERIOD, 60000);
				container.setHeartbeatPeriod(heartbeatPeriod);
				container.setAlias(ip);
			}

			container.setLatestStartTime(Utils.getCurrentTimeString());
			container.setUpdateTime(Utils.getCurrentTimeString());
			container.setServerPort(serverPort);
			container.setPid(System.getProperty("PID"));
			container.setUserDir(System.getProperty("user.dir"));
			container.setOperator(System.getProperty("user.name"));
			container.setRegistPath(registPath);
			container.setRpcServerPort(rpcServerPort);
			container.setOnlyContainer(isExecuteContainer());
			try {
				container.setHostname(InetAddress.getLocalHost().getHostName());
			} catch (Exception e) {
			}

			try {
				if (context != null) {
					ServletContext servletContext = context.getBean(ServletContext.class);
					String contextPath = servletContext.getContextPath();
					if (StringUtils.isBlank(contextPath)) {
						container.setContextPath("/");
					} else {
						container.setContextPath(contextPath);
					}
				}
			} catch (Throwable e1) {
				logger.warn("获取上下文路径异常：" + e1.getMessage());
				container.setContextPath("/");
			}

			String json = gson.toJson(container);
			logger.info("注册执行容器：" + json);

			// 不是禁用的都是正常
			if (!ExecuteContainerStatus.FORBBIDEN.equals(container.getStatus())) {
				container.setStatus(ExecuteContainerStatus.NORMAL);
			}

			ZookeeperHelper.putToZookeeper(container.getRegistPath(), client, container);

			this.container = container;
		} catch (Exception e2) {
			logger.error("执行容器注册失败，执行容器将要退出执行：" + e2.getMessage(), e2);
			try {
				client.close();
			} catch (Exception e) {
			}
			System.exit(-1);
			return;
		}
	}

	/**
	 * 更新容器的状态，前提是必须先注册过
	 *
	 * @param status
	 * @throws Exception
	 */
	public void updateStatus(ExecuteContainerStatus status) throws Exception {
		if (status == null) {
			return;
		}
		if (container == null) {
			// logger.warn("更新执行容器状态为[" + status.getName() + "]时，发现本地容器为空");
			// 重复启动或者没有获得文件锁，启动失败时，会执行到这里，没必要打印日志
			return;
		}

		container.setStatus(status);
		container.setStatusDesc("容器于[" + Utils.getCurrentTimeString() + "]时" + status.getName());

		ZookeeperHelper.putToZookeeper(container.getRegistPath(), client, container);
	}

	/**
	 * 服务端口
	 */
	public int serverPort = -1;

	public int getServerPort() {
		return serverPort;
	}

	/**
	 * RPC端口
	 */
	public int rpcServerPort = -1;

	public int getRpcServerPort() {
		return rpcServerPort;
	}

	/**
	 * 获取锁
	 *
	 * @return
	 */
	public SingletonLock getLock() {
		return lock;
	}

	private CuratorFramework client = null;

	private SingletonLock lock = null;

	// 本执行容器信息
	private ExecuteContainer container = null;

	public ExecuteContainer getCurrentContainer() {
		return container;
	}

	public CuratorFramework getCuratorFrameworkClient() {
		return client;
	}

	/**
	 *
	 * com.guttv.common包下面的类用该对象
	 */
	private ApplicationContext context = null;

	public ApplicationContext getApplicationContext() {
		return context;
	}

	/**
	 * 判断当前执行的 是不是执行容器,判断方法应该是找到启动类比较准确，目前还没有找到启动类的方法
	 *
	 * @return
	 */
	public boolean isExecuteContainer() {
		if (context == null) {
			return false;
		}

		Map<String, Object> springBootBeansMap = context.getBeansWithAnnotation(SpringBootApplication.class);
		if (springBootBeansMap == null || springBootBeansMap.size() == 0) {
			return false;
		}

		Collection<Object> springBootBeans = springBootBeansMap.values();
		for (Object bean : springBootBeans) {
			if (bean instanceof FlowExecuteContainerMain) {
				return true;
			}
		}

		return false;
	}

	private static String heartbeatPath = null;

	public static String getContainerRegistPath() {
		if (StringUtils.isNotBlank(heartbeatPath)) {
			return heartbeatPath;
		}
		heartbeatPath = ZookeeperHelper
				.getRealPath(ZKPaths.makePath(PathConstants.CONTAINER_ROOT_PATH, getContainerID()));
		return heartbeatPath;
	}

	private static String containerID = "";

	public static String getContainerID() {
		if (StringUtils.isNotBlank(containerID)) {
			return containerID;
		}

		synchronized (containerID) {
			if (StringUtils.isBlank(containerID)) {

				String ip = HttpUtil.getLocalIP();
				String mac = HttpUtil.getMac();
				String projectPath = Utils.getProjectLocation();
				StringBuilder sb = new StringBuilder(ip).append(mac).append(projectPath);

				logger.debug("本执行容器的基本信息：" + sb.toString());

				try {
					containerID = MD5Util.md5(sb.toString(), Constants.ENCODING);
				} catch (Exception e) {
					logger.error("生成执行容器唯一标识异常，将启动临时标识，异常信息：" + e.getMessage(), e);
					containerID = "L" + RandomStringUtils.randomNumeric(16);
				}
			}
		}
		return containerID;
	}

	// 下面是单例
	public static ContainerRegister getInstance() {
		return Single.instance;
	}

	private static class Single {
		private static ContainerRegister instance = new ContainerRegister();
	}

	private ContainerRegister() {
	}
}
