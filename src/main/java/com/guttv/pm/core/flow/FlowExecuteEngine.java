package com.guttv.pm.core.flow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.guttv.pm.core.bean.ComponentBean;
import com.guttv.pm.core.bean.ComponentNodeBean;
import com.guttv.pm.core.cache.ComponentCache;
import com.guttv.pm.core.cache.TaskCache;
import com.guttv.pm.core.fp.ComponentPackToZookeeper;
import com.guttv.pm.core.task.AbstractRecycleTask;
import com.guttv.pm.core.task.AbstractTask;
import com.guttv.pm.utils.Enums.ComponentNodeStatus;
import com.guttv.pm.utils.Enums.ComponentStatus;
import com.guttv.pm.utils.Enums.FlowExecuteStatus;
import com.guttv.pm.utils.ThreadPool;

public class FlowExecuteEngine {

	private static Logger logger = LoggerFactory.getLogger(FlowExecuteEngine.class);

	/**
	 * 启动一个流程
	 * 
	 * @param flowExeCode
	 * @throws Exception
	 */
	public synchronized static void excuteFlowExecuteConfig(FlowExecuteConfig flowExeConfig) throws Exception {

		String flowExeCode = flowExeConfig.getFlowExeCode();
		String err = null;

		if (flowExeConfig.getStatus() == null) {
			flowExeConfig.setStatus(FlowExecuteStatus.INIT);
		}

		// 如果以前执行过，并且失败不再重新执行，等待恢复状态后再启动
		if (flowExeConfig.getStatus() == FlowExecuteStatus.ERROR) {
			err = "早期流程执行失败，清先确认异常信息，恢复流程配置状态后重新启动!";
			logger.warn(err);
			throw new Exception(err);
		}

		if (flowExeConfig.getStatus() != FlowExecuteStatus.INIT && flowExeConfig.getStatus() != FlowExecuteStatus.FINISH
				&& flowExeConfig.getStatus() != FlowExecuteStatus.LOCKED) {
			err = "流程执行配置不是初始化或者完成状态，不支持当前状态[" + flowExeConfig.getStatus().getName() + "]的流程配置启动";
			logger.warn(err);
			throw new Exception(err);
		}

		// 如果是完成状态时或者其它状态进到此时，把以前的任务停丢
		List<AbstractTask> oldTasks = TaskCache.getInstance().stopTasksByFlowExeCode(flowExeCode);
		if (oldTasks != null && oldTasks.size() > 0 && flowExeConfig.getStatus() != FlowExecuteStatus.FINISH) {
			logger.error("启动执行流程配置任务[" + flowExeCode + "]时，发现内存中仍有部分任务，共[" + oldTasks.size() + "]条，已经做了停止处理，任务如下：");
			for (AbstractTask at : oldTasks) {
				logger.error("任务[" + flowExeCode + "][" + at.getComponentClz() + "][" + at.getComponentCn() + "]["
						+ at.getFlowCode() + "][" + at.getName() + "],状态：" + at.getThreadState() + ",堆栈信息："
						+ at.getThreadStackTrace());
			}
		}

		try {
			// 启动前把以前的任务停止并删除；
			// 正常情况下启动时，应该是没有在执行的任务并且缓存中没有该流程对应的任务数据，此处只是为了防止异常情况
			TaskCache.getInstance().stopTasksByFlowExeCode(flowExeCode);

			// 先把流程设置成启动中状态
			flowExeConfig.setStatus(FlowExecuteStatus.STARTING);

			// 尝试下载并注册需要的组件包
			ComponentPackToZookeeper.cacheFromZookeeper(flowExeConfig.getUsedComIDs(), true);

			// 先包装所有的任务对象
			List<AbstractTask> tasks = new ArrayList<AbstractTask>();

			// 取出所有的流程节点
			List<ComponentNodeBean> cnbs = flowExeConfig.getComNodes();
			if (cnbs == null || cnbs.size() == 0) {
				err = "流程执行配置[" + flowExeCode + "]竟然没有一个节点";
				flowExeConfig.setStatus(FlowExecuteStatus.ERROR, err);
				logger.warn(err);
				throw new Exception(err);
			}

			// 循环节点生成任务
			String clz = null;
			AbstractTask at = null;

			for (ComponentNodeBean cnb : cnbs) {

				// 节点可以被禁用
				if (cnb.getStatus() != null && cnb.getStatus() == ComponentNodeStatus.FORBIDDEN) {
					logger.info("节点[" + cnb.getNodeID() + "],组件[" + cnb.getComponentClz() + "]在流程配置["
							+ flowExeConfig.getFlowCode() + "]中的状态为[" + cnb.getStatus() + "]，不启动。");
					continue;
				}

				// 获取组件配置
				clz = cnb.getComponentClz();
				if (StringUtils.isBlank(clz)) {
					String error = "流程执行配置[" + flowExeConfig.getFlowCode() + "]中节点[" + cnb.getNodeID()
							+ "]对应的class类型为空";
					logger.error(error);
					throw new Exception(error);
				}
				ComponentBean com = ComponentCache.getInstance().getComponent(clz);
				if (com == null) {
					String error = "流程[" + flowExeConfig.getFlowCode() + "]中的节点[" + cnb.getNodeID() + "]对应的组件["
							+ cnb.getComponentClz() + "]不存在！";
					logger.error(error);
					throw new Exception(error);
				}
				if (com.getStatus() == null || com.getStatus() != ComponentStatus.NORMAL.getValue()) {
					String error = "流程执行配置[" + flowExeConfig.getFlowCode() + "]中的节点[" + cnb.getNodeID() + "]对应的组件["
							+ cnb.getComponentClz() + "]的状态异常[" + com.getStatus() + "]";
					logger.error(error);
					throw new Exception(error);
				}

				// 有可能是需要多线程处理的情况
				for (int i = 1; i <= com.getThreadNum(); i++) {

					// 创建任务对象
					at = TaskBuilder.newBuilder().withComponent(com).withComponentNode(cnb)
							.withFlowExecuteConfig(flowExeConfig).withIndex(i).build();

					// 特别处理一下周期执行的程序，是不是暂停的状态
					if (cnb.getStatus() == ComponentNodeStatus.PAUSE) {
						if (at instanceof AbstractRecycleTask) {
							AbstractRecycleTask art = (AbstractRecycleTask) at;
							art.setPause(true);
						}
					}

					// 添加到任务队列里
					tasks.add(at);
				}

			}

			// 再循环启动所有的任务
			for (AbstractTask task : tasks) {
				ThreadPool.getPool().submit(task);
				logger.info("启动流程执行配置节点[flowExeCode=" + flowExeCode + "][flowCode=" + at.getFlowCode() + "][clz="
						+ at.getComponentClz() + "][TaskID=" + at.getId() + "]");
			}

			// 执行到这儿的话，就认为流程启动成功
			flowExeConfig.setStatus(FlowExecuteStatus.RUNNING);
			flowExeConfig.setUpdateTime(new Date());

			/**
			 * 添加监控
			 */
			FlowExecuteMonitor.startMonitor(flowExeConfig);
		} catch (Throwable e) {
			// 出现异常就把 任务清理一次 此时可以没有任务
			List<AbstractTask> tasks = TaskCache.getInstance().stopTasksByFlowExeCode(flowExeCode);
			// 启动线程，监控线程是否确实被停掉
			if (tasks != null && tasks.size() > 0) {
				new ConfirmFlowStoppedThread(flowExeConfig, tasks).start();
			}
			// 标记流程执行异常
			flowExeConfig.setStatus(FlowExecuteStatus.ERROR, "启动失败：" + e.getMessage());
			flowExeConfig.setUpdateTime(new Date());
			logger.error("启动流程执行配置[" + flowExeCode + "]时出现异常：" + e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * 停止一个流程的运行
	 * 
	 * @param flowExeCode
	 */
	public synchronized static void stopFlowExecuteConfig(FlowExecuteConfig flowExeConfig) {
		String flowExeCode = flowExeConfig.getFlowExeCode();
		// 不验证是否存在，先停了再说 >_<
		List<AbstractTask> tasks = TaskCache.getInstance().stopTasksByFlowExeCode(flowExeCode);

		// 启动线程，监控线程是否确实被停掉
		if (tasks != null && tasks.size() > 0) {
			new ConfirmFlowStoppedThread(flowExeConfig, tasks).start();
			flowExeConfig.setStatus(FlowExecuteStatus.STOPPED);
		} else {
			flowExeConfig.setStatus(FlowExecuteStatus.FINISH);
		}

		flowExeConfig.setUpdateTime(new Date());
	}

}
