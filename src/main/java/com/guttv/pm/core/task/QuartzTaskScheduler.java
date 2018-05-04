/**
 * 
 */
package com.guttv.pm.core.task;

import java.util.Date;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.guttv.pm.utils.Utils;

/**
 * @author Peter
 *
 */
public class QuartzTaskScheduler {

	protected Logger logger = LoggerFactory.getLogger("task");

	/**
	 * 添加调度任务
	 * 
	 * @param jobDetail
	 * @param trigger
	 * @throws SchedulerException
	 */
	public JobKey scheduleJob(JobDetail jobDetail, Trigger trigger) throws SchedulerException {
		Date firstFireTime = scheduler.scheduleJob(jobDetail, trigger);
		JobKey jobKey = jobDetail.getKey();
		logger.debug("添加新的调度任务[" + jobKey + "]，首次执行时间：" + Utils.getTimeString(firstFireTime.getTime()));
		return jobKey;
	}

	/**
	 * 暂停
	 * 
	 * @param jobKey
	 * @throws SchedulerException
	 */
	public void pauseJob(JobKey jobKey) throws SchedulerException {
		scheduler.pauseJob(jobKey);
		logger.debug("暂停调度任务[" + jobKey + "]");
	}

	/**
	 * 继续执行
	 * 
	 * @param jobKey
	 * @throws SchedulerException
	 */
	public void resumeJob(JobKey jobKey) throws SchedulerException {
		scheduler.resumeJob(jobKey);
		logger.debug("继续执行调度任务[" + jobKey + "]");
	}

	/**
	 * 删除任务
	 * 
	 * @param jobKey
	 * @throws SchedulerException
	 */
	public void deleteJob(JobKey jobKey) throws SchedulerException {
		scheduler.deleteJob(jobKey);
		logger.debug("移除调度任务[" + jobKey + "]");
	}

	public void shutdown() throws SchedulerException {
		scheduler.shutdown(true);
		logger.debug("任务调度器退出");
	}

	/**
	 * 获取下次的执行时间
	 * 
	 * @param triggerKey
	 * @return
	 * @throws SchedulerException
	 */
	public Date getNextFireTime(TriggerKey triggerKey) throws SchedulerException {
		Trigger trigger = scheduler.getTrigger(triggerKey);
		if (trigger != null) {
			return trigger.getNextFireTime();
		}
		return null;
	}

	private QuartzTaskScheduler() {
		// 1.创建Scheduler的工厂
		SchedulerFactory sf = new StdSchedulerFactory();
		// 2.从工厂中获取调度器实例
		try {
			scheduler = sf.getScheduler();

			scheduler.start();

		} catch (SchedulerException e) {
			logger.error("初始化quartz任务调度异常：" + e.getMessage(), e);
		}
	}

	private static class Single {
		private static QuartzTaskScheduler instance = new QuartzTaskScheduler();
	}

	public static QuartzTaskScheduler getInstance() {
		return Single.instance;
	}

	private Scheduler scheduler = null;

	// 获取任务调度
	public Scheduler getScheduler() {
		return scheduler;
	}

}
