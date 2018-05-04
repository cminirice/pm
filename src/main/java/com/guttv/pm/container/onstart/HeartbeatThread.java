/**
 * 
 */
package com.guttv.pm.container.onstart;

import java.lang.management.ManagementFactory;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.guttv.pm.container.ContainerFlowExecConfCache;
import com.guttv.pm.core.bean.ExecuteContainer;
import com.guttv.pm.core.bean.Heartbeat;
import com.guttv.pm.core.cache.ComponentCache;
import com.guttv.pm.core.cache.ComponentPackageCache;
import com.guttv.pm.core.cache.TaskCache;
import com.guttv.pm.core.zk.PathConstants;
import com.guttv.pm.core.zk.SingletonLock;
import com.guttv.pm.core.zk.ZookeeperHelper;
import com.guttv.pm.utils.HttpUtil;
import com.guttv.pm.utils.Utils;

/**
 * @author Peter
 *
 */
public class HeartbeatThread extends Thread {

	protected static Logger logger = LoggerFactory.getLogger(HeartbeatThread.class);

	public static HeartbeatThread getInstance() {
		return instance;
	}

	private static HeartbeatThread instance = new HeartbeatThread();

	private HeartbeatThread() {
		this.setName(HeartbeatThread.class.getSimpleName());
		this.setDaemon(true);
	}

	private boolean stop = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		ExecuteContainer container = ContainerRegister.getInstance().getCurrentContainer();

		SingletonLock lock = ContainerRegister.getInstance().getLock();

		CuratorFramework client = ContainerRegister.getInstance().getCuratorFrameworkClient();

		long heartbeatPeriod = ContainerRegister.getInstance().getCurrentContainer().getHeartbeatPeriod();

		Gson gson = new Gson();

		String heartbeatPath = ZookeeperHelper
				.getRealPath(ZKPaths.makePath(container.getRegistPath(), PathConstants.CONTAINER_HEARTBEAT_PATH));

		// 该对象放在循环外面，一方面考虑可以节省对象，另一方面可以上报putHeartbeatError异常信息；
		Heartbeat heartbeat = new Heartbeat();
		heartbeat.setContainerID(ContainerRegister.getContainerID());
		heartbeat.setContainerIP(HttpUtil.getLocalIP());

		// 周期地上报心跳，并上报一些系统信息，下面是变化的信息
		while (!stop) {
			try {
				long start = System.currentTimeMillis();

				if (!lock.lock()) {
					logger.warn("有其它程序入侵，或者ZK信息被修改....");
				}

				try {
					double uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000.0;
					logger.debug("JVM running for " + uptime);
				} catch (Throwable ex) {
				}

				// 心跳时间
				heartbeat.setHeartbeatTime(Utils.getTimeString(start));

				// 内存信息
				heartbeat.putMemoryInfo(Utils.getMemoryInfo());

				// 线程信息
				heartbeat.putThreadNum(Utils.getAllThread().length);

				// 组件包数量
				heartbeat.putComponentPackCount(ComponentPackageCache.getInstance().countComponentPack());

				// 组件数量
				heartbeat.putComponentCount(ComponentCache.getInstance().countComponent());

				// 执行流程数
				heartbeat.putFlowExecuteConfigCount(ContainerFlowExecConfCache.getInstance().countFlowExecuteConfig());

				// 正在执行的流程数
				heartbeat.putExecutedFlowCount(TaskCache.getInstance().getAllExecutedFlowCode().size());

				// 任务数
				heartbeat.putTasksCount(TaskCache.getInstance().getAllTasks().size());

				String json = gson.toJson(heartbeat);
				logger.info("上报心跳：" + json);

				ZookeeperHelper.putToZookeeper(heartbeatPath, client, heartbeat);

				// 心跳周期可以从服务器端控制，可能随时更新
				// 系统启动时候应该会确定是用ZK上的心跳周期，还是默认的，执行容器不应该为空
				heartbeatPeriod = ContainerRegister.getInstance().getCurrentContainer().getHeartbeatPeriod();

				try {
					long sleepTime = (start + heartbeatPeriod - System.currentTimeMillis());

					if (sleepTime < 0) {
						// 到这里是因为上报心跳的时间太长，或者配置的周期太小
						String error = "上报心跳的时间已经大于上报周期[" + heartbeatPeriod + "][" + sleepTime + "]，请检查配置的周期是否合理";
						logger.warn(error);
						heartbeat.putHeartbeatError(error);
						sleepTime = 10000;
					} else {
						heartbeat.getInfo().remove(Heartbeat.HEARTBEAT_ERROR);
					}

					Thread.sleep(sleepTime);
				} catch (InterruptedException e1) {
				}
			} catch (Exception e) {
				logger.error("上报心跳异常：" + e.getMessage(), e);
				try {
					Thread.sleep(2 * heartbeatPeriod);
				} catch (InterruptedException e1) {
				}
			}
		}

		logger.info("心跳信息上报任务退出...");
	}

}
