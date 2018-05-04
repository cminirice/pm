/**
 * 
 */
package com.guttv.pm.core.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.guttv.pm.core.task.AbstractRecycleTask;
import com.guttv.pm.core.task.AbstractTask;
import com.guttv.pm.core.task.TaskProxy;

/**
 * @author Peter
 *
 */
public final class TaskCache {
	private static Logger logger = LoggerFactory.getLogger(TaskCache.class);

	private TaskCache() {
	}

	private static class Single {
		private static TaskCache instance = new TaskCache();
	}

	public static TaskCache getInstance() {
		return Single.instance;
	}

	// 重入锁
	private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();

	// 保存任务对象
	private final Map<String, List<AbstractTask>> taskMap = new HashMap<String, List<AbstractTask>>();

	// 注册一个任务对象
	public void registTask(AbstractTask task) {
		List<AbstractTask> tasks = null;

		// flowExeCode 为空的话，统一放到null的列表中
		String flowExeCode = task.getFlowExeCode();
		if (StringUtils.isBlank(flowExeCode)) {
			flowExeCode = "null";
		}

		w.lock();
		try {
			tasks = taskMap.get(flowExeCode);
			if (tasks == null) {
				tasks = new ArrayList<AbstractTask>();
				taskMap.put(flowExeCode, tasks);
			}
			tasks.add(task);
		} finally {
			w.unlock();
		}
	}

	/**
	 * 获取所有执行中的流程的code
	 * 
	 * @return
	 */
	public List<String> getAllExecutedFlowCode() {
		List<String> flowExeCodes = new ArrayList<String>();
		r.lock();
		try {
			flowExeCodes.addAll(taskMap.keySet());
		} finally {
			r.unlock();
		}
		return flowExeCodes;
	}

	/**
	 * 按流程执行配置查找任务
	 * 
	 * @param flowExeCode
	 * @return
	 */
	public List<AbstractTask> getTasksByFlowExeCode(String flowExeCode) {
		if (StringUtils.isBlank(flowExeCode)) {
			return null;
		}

		List<AbstractTask> tasks = null;
		r.lock();
		try {
			tasks = taskMap.get(flowExeCode);
		} finally {
			r.unlock();
		}
		return tasks;
	}

	/**
	 * 根据 flowCode获取这个流程的所有任务
	 * 
	 * @param flowCode
	 * @return
	 */
	public List<AbstractTask> getTasksByFlowCode(String flowCode) {
		if (StringUtils.isBlank(flowCode)) {
			return null;
		}

		List<AbstractTask> tasks = new ArrayList<AbstractTask>();
		r.lock();
		try {
			for (List<AbstractTask> ats : taskMap.values()) {
				if (ats != null && ats.size() > 0 && flowCode.equals(ats.get(0).getFlowCode())) {
					tasks.addAll(ats);
				}
			}
		} finally {
			r.unlock();
		}
		return tasks;
	}

	/**
	 * 进行任务暂停、恢复，如果没有周期性的任务，反馈false
	 * 
	 * @param flowExeCode
	 * @param pause
	 * @return
	 */
	public boolean setPauseStatusByFlowExeCode(String flowExeCode, boolean pause) {
		if (StringUtils.isBlank(flowExeCode)) {
			return false;
		}

		List<AbstractTask> tasks = null;
		boolean flag = false;
		w.lock();
		try {
			tasks = taskMap.get(flowExeCode);
			if (tasks != null && tasks.size() > 0) {
				for (AbstractTask at : tasks) {
					if (at instanceof AbstractRecycleTask) {
						AbstractRecycleTask art = (AbstractRecycleTask) at;
						if (art.isFinished() || art.isStop()) {
							continue; // 完成的，或者停止的，不能暂停或者启动
						} else {
							art.setPause(pause);
							flag = true; // 标记有周期任务
						}
					}
				}
			}
		} finally {
			w.unlock();
		}
		return flag;
	}

	/**
	 * 把流程中的所有任务停止
	 * 
	 * @param flowExeCode
	 */
	public List<AbstractTask> stopTasksByFlowExeCode(String flowExeCode) {
		if (StringUtils.isBlank(flowExeCode)) {
			return null;
		}
		List<AbstractTask> tasks = null;
		w.lock();
		try {
			tasks = taskMap.remove(flowExeCode);
		} finally {
			w.unlock();
		}

		if (tasks != null && tasks.size() > 0) {
			for (AbstractTask at : tasks) {
				if (at instanceof AbstractRecycleTask) {
					AbstractRecycleTask art = (AbstractRecycleTask) at;
					// 没有结束的，并且没有停止的 方能停止
					if (!art.isFinished() && !art.isStop()) {
						logger.info("停止流程中[" + flowExeCode + "][" + art.getFlowCode() + "]的任务[nodeID:" + art.getNodeID()
								+ "][class=" + art.getComponentClz() + "][taskid=" + art.getId() + "]");
						art.setStop(true);
					}
				}
			}
		}
		return tasks;
	}

	/**
	 * 获取所有的任务列表
	 * 
	 * @return
	 */
	public List<AbstractTask> getAllTasks() {
		List<AbstractTask> tasks = new ArrayList<AbstractTask>();
		r.lock();
		try {
			for (List<AbstractTask> ats : taskMap.values()) {
				tasks.addAll(ats);
			}
		} finally {
			r.unlock();
		}
		return tasks;
	}

	/**
	 * 
	 * @param flowExeCode
	 * @param nodeID
	 * @param pause
	 * @return
	 */
	public boolean setPauseStatusByFlowExeCode(String flowExeCode, String nodeID, boolean pause) {

		if (StringUtils.isBlank(flowExeCode) || StringUtils.isBlank(nodeID)) {
			return false;
		}

		List<AbstractTask> tasks = null;
		boolean flag = false;
		w.lock();
		try {
			tasks = taskMap.get(flowExeCode);
			if (tasks != null && tasks.size() > 0) {
				for (AbstractTask at : tasks) {
					if (nodeID.equals(at.getNodeID()) && at instanceof AbstractRecycleTask) {
						AbstractRecycleTask art = (AbstractRecycleTask) at;
						if (art.isFinished() || art.isStop()) {
							break; // 完成的，或者停止的，不能暂停或者启动
						} else {
							art.setPause(pause);
							flag = true; // 标记有周期任务
							break;
						}
					}
				}
			}
		} finally {
			w.unlock();
		}
		return flag;
	}

	/**
	 * 停止流程中的一个节点
	 * 
	 * @param flowExeCode
	 * @param nodeID
	 */
	public void stopTasksByFlowExeCode(String flowExeCode, String nodeID) {
		if (StringUtils.isBlank(flowExeCode) || StringUtils.isBlank(nodeID)) {
			return;
		}

		List<AbstractTask> tasks = null;
		w.lock();
		try {
			tasks = taskMap.get(flowExeCode);
			if (tasks != null && tasks.size() > 0) {
				for (AbstractTask at : tasks) {
					if (nodeID.equals(at.getNodeID()) && at instanceof AbstractRecycleTask) {
						AbstractRecycleTask art = (AbstractRecycleTask) at;

						// 没有结束的，并且没有停止的 方能停止
						if (!art.isFinished() && !art.isStop()) {
							art.setStop(true);
						}
					}
				}
			}
		} finally {
			w.unlock();
		}
	}

	/**
	 * 获取节点的堆栈信息
	 * 
	 * @param flowExeCode
	 * @param nodeID
	 * @return
	 */
	public String getTaskStacktrace(String flowExeCode, String nodeID) {

		if (StringUtils.isBlank(flowExeCode) || StringUtils.isBlank(nodeID)) {
			return null;
		}

		List<AbstractTask> tasks = null;
		r.lock();
		try {
			tasks = taskMap.get(flowExeCode);
			if (tasks != null && tasks.size() > 0) {
				for (AbstractTask at : tasks) {
					if (nodeID.equals(at.getNodeID()) && at instanceof AbstractRecycleTask) {
						return at.getThreadStackTrace();
					}
				}
			}
		} finally {
			r.unlock();
		}
		return null;
	}

	/**
	 * 根据代理对象，获取代理的任务
	 * 
	 * @param proxy
	 * @return
	 */
	public AbstractTask getTaskByProxy(Object proxy) {
		r.lock();
		try {
			for (List<AbstractTask> ats : taskMap.values()) {
				for (AbstractTask at : ats) {
					if (at instanceof TaskProxy) {
						if (at == proxy) { // 这里必须是同一个对象
							return at;
						}
					}
				}
			}
		} finally {
			r.unlock();
		}
		return null;
	}

}
