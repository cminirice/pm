/**
 * 
 */
package com.guttv.pm.core.flow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.guttv.pm.core.bean.ComponentNodeBean;
import com.guttv.pm.core.cache.ConfigCache;
import com.guttv.pm.core.cache.TaskCache;
import com.guttv.pm.core.task.AbstractRecycleTask;
import com.guttv.pm.core.task.AbstractTask;
import com.guttv.pm.utils.Constants;
import com.guttv.pm.utils.Enums.ComponentNodeStatus;
import com.guttv.pm.utils.Enums.FlowExecuteStatus;

/**
 * 监控流程执行配置执行的状态
 * 
 * @author Peter
 *
 */
public class FlowExecuteMonitor extends Thread {

	private static Logger logger = LoggerFactory.getLogger(FlowExecuteMonitor.class);

	private String flowExeCode = null;

	private FlowExecuteConfig config = null;

	private FlowExecuteMonitor(FlowExecuteConfig config) {
		this.config = config;
		this.flowExeCode = config.getFlowExeCode();
		this.setName(flowExeCode + "Monitor");
		this.setDaemon(true);
		this.setPriority(Thread.MIN_PRIORITY);
	}

	private static Map<String, FlowExecuteMonitor> monitorMap = new HashMap<String, FlowExecuteMonitor>();

	private boolean start = false;

	private long period = ConfigCache.getInstance().getProperty(Constants.SERVER_FLOWEXECUTE_MONITOR_PERIOD, 10000);

	public void run() {
		try {
			logger.info("开始监控流程：" + this.getName());

			while (start) {

				try {
					Thread.sleep(period);
				} catch (InterruptedException e) {
					logger.error("监控线异常[" + this.getName() + "]:" + e.getMessage(), e);
					return;
				}

				logger.debug("开始给[" + flowExeCode + "]流程体检");

				/**
				 * 如果流程执行结束，退出
				 */
				if (config.getStatus() == FlowExecuteStatus.FINISH) {
					logger.info("流程执行配置[" + flowExeCode + "]执行结束,监控线程将退出...");
					start = false;
					continue;
				}

				List<ComponentNodeBean> comNodes = config.getComNodes();
				if (comNodes == null || comNodes.size() == 0) {
					config.setStatusDesc("监控小助发现执行配置没有一个节点信息。");
				}

				// 监控任务是否有启动异常的
				if (comNodes != null && comNodes.size() > 0) {
					boolean flag = false;
					int count = 0;
					for (ComponentNodeBean comNode : comNodes) {
						if (ComponentNodeStatus.FORBIDDEN.equals(comNode.getStatus())) {
							continue;
						}
						// 有不失败的做标记
						if (ComponentNodeStatus.ERROR.equals(comNode.getStatus())) {
							count++;
						} else {
							flag = true;
						}
					}

					// 全部执行异常
					if (!flag) {
						config.setStatusDesc("监控小助发现流程中组件全部执行异常");
					} else if (count > 0) { // 部分执行异常
						config.setStatusDesc("监控小助发现流程中有[" + count + "]个组件执行异常");
					}
				}

				// 监控节点是否都执行结束了
				if (comNodes != null && comNodes.size() > 0) {
					boolean flag = true; // 标记全部执行结束
					for (ComponentNodeBean comNode : comNodes) {
						if (ComponentNodeStatus.FORBIDDEN.equals(comNode.getStatus())) {
							continue;
						}

						// 发现有没执行结束的
						if (!ComponentNodeStatus.FINISH.equals(comNode.getStatus())) {
							flag = false;
							break;
						}
					}

					if (flag) {
						// 全部都执行结束了
						config.setStatus(FlowExecuteStatus.FINISH, "监控小助发现流程中所有组件执行结束");
						start = false;
						continue;
					}
				}

				/**
				 * 下面检查任务
				 */
				// 检查缓存中还有没有任务
				List<AbstractTask> tasks = TaskCache.getInstance().getTasksByFlowExeCode(flowExeCode);
				if (tasks == null || tasks.size() == 0) {
					if(FlowExecuteStatus.STOPPED.equals(config.getStatus())) {
						config.setStatusDesc("监控小助发现缓存中没有本流程的任何任务，状态是停止，小助退出监控");
					}else {
						// 全部都执行结束了
						config.setStatus(FlowExecuteStatus.FINISH, "监控小助发现缓存中没有本流程的任何任务，设置完成");
					}
					logger.info("监控小助发现缓存中没有流程[" + this.flowExeCode + "]的任何任务");
					start = false;
					continue;
				} else {

					/**
					 * 下面判断是否所有任务都结束了
					 */
					// 检查所有的任务是否有异常，是否都结束了
					boolean flag = true; // 标记任务已经完成
					for (AbstractTask at : tasks) {
						if (!at.isFinished()) {
							flag = false; // 如果有一个没有结束，标记为没有完成，停止检查
							break;
						}
					}

					// 到下面的是全部执行完的情况
					if (flag) {
						// 全部都执行结束了
						config.setStatus(FlowExecuteStatus.FINISH, "监控小助发现所有的任务均执行完成");
						TaskCache.getInstance().stopTasksByFlowExeCode(config.getFlowExeCode());
						logger.info("监控小助发现执行配置[" + this.flowExeCode + "]所有的任务均执行完成");
						start = false;
						continue;
					}

					/**
					 * 下面判断任务是否都被暂停
					 */
					// 判断任务中所有的任务是否都被暂停了
					if (!FlowExecuteStatus.PAUSE.equals(config.getStatus())) {
						flag = true; // 标记所有的任务都被暂停
						for (AbstractTask at : tasks) {
							if (at.isFinished()) {
								continue; // 完成的不在统计范围
							}

							if (at instanceof AbstractRecycleTask) {
								AbstractRecycleTask art = (AbstractRecycleTask) at;
								if (!art.isPause()) {
									flag = false; // 有一个没有被暂停，就认为没有暂停
									break;
								}
							}
						}
						
						if (flag) {
							// 这里是所有的任务都被暂停了，除完成的
							config.setStatus(FlowExecuteStatus.PAUSE, "监控小助发现所有的任务都是暂停状态，已经帮您更新流程执行状态为暂停");
							logger.info("监控小助发现所有的任务都是暂停状态，已经帮您更新流程执行[" + this.flowExeCode + "]状态为暂停");
							// 不能退出监控
						}
					}

					/**
					 * 下面判断如果流程被暂停了，检查有没有启动的
					 */
					// 判断任务中所有的任务是否都被暂停了
					if (FlowExecuteStatus.PAUSE.equals(config.getStatus())) {
						flag = true; // 标记所有的任务都被暂停
						for (AbstractTask at : tasks) {
							if (at.isFinished()) {
								continue; // 完成的不在统计范围
							}

							if (at instanceof AbstractRecycleTask) {
								AbstractRecycleTask art = (AbstractRecycleTask) at;
								if (!art.isPause()) {
									flag = false; // 有一个没有被暂停，就认为没有暂停
									break;
								}
							}
						}
						
						if (!flag) {
							// 这里是至少有一个任务没有被暂停
							config.setStatus(FlowExecuteStatus.RUNNING, "监控小助发现有任务重新执行，已经帮您更新流程执行状态为执行中");
							logger.info("监控小助发现有任务重新执行，已经帮您更新流程执行配置[" + this.flowExeCode + "]状态为执行中");
							// 不能退出监控
						}
					}
				}
			}

			logger.info("监控小助已经完成任务，将要退出对流程配置[" + this.flowExeCode + "]的检查");
		} finally {
			synchronized (monitorMap) {
				// 找出是不是已经有监控，如果没有新建并保存
				FlowExecuteMonitor monitor = monitorMap.remove(flowExeCode);
				if (monitor != null) {
					logger.info("小助退出对执行配置[" + monitor.flowExeCode + "]的监控");
				}
			}

			// 释放对象
			flowExeCode = null;
			config = null;
		}

		// 监控流程的状态和任务的状态是不是都不一样
	}

	public static synchronized void startMonitor(FlowExecuteConfig flowExeConfig) {
		if (flowExeConfig == null || StringUtils.isBlank(flowExeConfig.getFlowExeCode())) {
			return;
		}
		synchronized (monitorMap) {
			// 找出是不是已经有监控，如果没有新建并保存
			FlowExecuteMonitor monitor = monitorMap.get(flowExeConfig.getFlowExeCode());
			if (monitor == null) {
				monitor = new FlowExecuteMonitor(flowExeConfig);
				monitorMap.put(flowExeConfig.getFlowExeCode(), monitor);
			}

			// 如果没有启动，启动
			if (!monitor.start) {
				monitor.start = true;
				monitor.start();
			}
		}

		logger.info("目前被监控的流程[" + monitorMap.size() + "]:" + monitorMap.keySet());
	}
}
