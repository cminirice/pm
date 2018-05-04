/**
 * 
 */
package com.guttv.pm.platform.container;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.guttv.pm.core.bean.ExecuteContainer;
import com.guttv.pm.core.cache.ConfigCache;
import com.guttv.pm.core.rpc.Ping;
import com.guttv.pm.utils.Constants;
import com.guttv.pm.utils.Enums.ExecuteContainerStatus;
import com.guttv.pm.utils.HttpUtil;
import com.guttv.pm.utils.SendMailTools;
import com.guttv.pm.utils.Utils;
import com.guttv.rpc.client.RpcProxy;

/**
 * @author Peter
 *
 */
public class HeartbeatMonitor extends Thread {

	private static Logger logger = LoggerFactory.getLogger(HeartbeatMonitor.class);

	private long period = ConfigCache.getInstance().getProperty(Constants.CONTAINER_CHECKTIMEOUT_PERIOD, 30000);

	public void run() {
		this.setName("HeartbeatMonitor");
		while (true) {
			try {
				Thread.sleep(period);

				/**
				 * 更新执行容器的状态
				 */
				List<ExecuteContainer> containers = ExecuteContainerCache.getInstance().getAllExecuteContainer();
				if (containers != null && containers.size() > 0) {
					for (ExecuteContainer container : containers) {

						ExecuteContainerStatus oldStatus = container.getStatus();

						// 禁用的容器不再检查
						if (ExecuteContainerStatus.FORBBIDEN.equals(oldStatus)
								|| ExecuteContainerStatus.SHUTDOWN.equals(oldStatus)) {
							continue;
						}

						ExecuteContainerCache.getInstance().refreshStatus(container);

						// 如果是超时的话，并且以前不是超时，Ping它一下
						if (!ExecuteContainerStatus.TIMEOUT.equals(oldStatus)
								&& ExecuteContainerStatus.TIMEOUT.equals(container.getStatus())) {

							// 不管怎么样，进行体检
							examineExecuteContainer(container);

							// 发邮件
							String toUser = ConfigCache.getInstance().getProperty(Constants.CONTAINER_TIMEOUT_SENDMAIL,
									null);
							if (StringUtils.isNotBlank(toUser)) {
								String subject = "执行容器[" + container.getAlias() + "][" + container.getIp() + "]心跳超时";
								String content = new StringBuilder("检测小助[").append(HttpUtil.getLocalIP()).append("][")
										.append(HttpUtil.getHostName()).append("]已经体检过容器[")
										.append(container.getContainerID()).append("][").append(container.getLocation())
										.append("]连接状态：").append(container.getStatusDesc()).toString();
								SendMailTools.sendMail(subject, content, toUser);
								logger.info("向[" + toUser + "]发出邮件：" + content);
							}

							logger.warn(
									"小助在确认执行容器[" + container.getContainerID() + "]心跳超时发现：" + container.getStatusDesc());
						}
					}
				}

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				try {
					Thread.sleep(5 * period);
				} catch (InterruptedException e1) {
					logger.error(e1.getMessage(), e1);
				}
			}

		}
	}

	public void examineExecuteContainer(ExecuteContainer container) {
		// 记录描述信息
		StringBuilder sb = new StringBuilder();
		boolean flag = false; // 标记是否有异常情况，如果有通的接口，认为有异常。

		// 获取执行容器的springboot服务根路径
		StringBuilder springbootPath = ExecuteContainerCache.getInstance()
				.getSpringbootServerPath(container.getContainerID());
		if (springbootPath == null) {
			sb.append("不能获取springboot访问路径；");
		} else {
			// 尝试调用
			String pingPath = ConfigCache.getInstance().getProperty(Constants.CONTAINER_PING_PATH, "pingContainer");

			try {

				logger.debug("检测执行容器[" + container.getContainerID() + "]路径：" + pingPath);

				HttpUtil.httpRequest(springbootPath.append(pingPath).toString(), "GET", "text/HTML", Constants.ENCODING,
						null, 3000, 3000);

				sb.append("springboot接口[" + springbootPath + "]正常；");

				flag = true;
			} catch (Exception e) {
				sb.append("springboot接口[" + springbootPath + "]异常；");
			}
		}

		// 尝试Zookeeper RPC
		RpcProxy proxy = null;
		try {
			proxy = ExecuteContainerCache.getInstance().getZookeeperRpcProxy(container.getContainerID(), 5000);
			if (proxy == null) {
				sb.append("获取ZK RPC失败；");
			} else {

				try {
					Ping ping = (Ping) proxy.create(Ping.class);
					ping.ping();
					sb.append("调用ZK RPC正常；");
					flag = true;
				} catch (Exception e) {
					sb.append("调用ZK RPC异常；");
				}
			}
		} catch (Exception e1) {
			sb.append("获取ZK RPC代理异常；");
		}

		// 尝试bootstrap RPC
		proxy = null;
		try {
			proxy = ExecuteContainerCache.getInstance().getBootstrapRpcProxy(container.getContainerID());
			if (proxy == null) {
				sb.append("获取Bootstrap RPC失败；");
			} else {

				try {
					Ping ping = (Ping) proxy.create(Ping.class);
					ping.ping();
					sb.append("调用Bootstrap RPC正常；");
					flag = true;
				} catch (Exception e) {
					sb.append("调用Bootstrap RPC异常；");
				}
			}
		} catch (Exception e1) {
			sb.append("获取Bootstrap RPC代理异常；");
		}

		// 这个地方显示，虽然心跳超时，但是有接口是通的
		if (flag && ExecuteContainerStatus.TIMEOUT.equals(container.getStatus())) {
			container.setStatus(ExecuteContainerStatus.EXCEPTION);
		}

		// 这个地方显示以前有检测出来接口有通的(EXCEPTION)，本次没有通的(flag=false)，把异常还原到超时
		if (!flag && ExecuteContainerStatus.EXCEPTION.equals(container.getStatus())) {
			container.setStatus(ExecuteContainerStatus.TIMEOUT);
		}

		// 如果心跳正常，但是所有的接口都不通了，更新成超时
		if (!flag && ExecuteContainerStatus.NORMAL.equals(container.getStatus())) {
			container.setStatus(ExecuteContainerStatus.TIMEOUT);
		}

		container.setStatusDesc("接口体检时间[" + Utils.getCurrentTimeString() + "]，接口体检报告：" + sb.toString());
		logger.debug("执行容器[" + container.getContainerID() + "]体检结果：" + container.getStatusDesc());
	}

	private static class Single {
		private static HeartbeatMonitor instance = new HeartbeatMonitor();
	}

	private HeartbeatMonitor() {
		this.setDaemon(true);
		this.setPriority(Thread.MIN_PRIORITY);
	}

	public static HeartbeatMonitor getInstance() {
		return Single.instance;
	}
}
