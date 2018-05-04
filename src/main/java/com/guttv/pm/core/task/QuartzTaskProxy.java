/**
 * 
 */
package com.guttv.pm.core.task;

import java.io.Closeable;
import java.lang.reflect.Method;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

import com.guttv.pm.utils.Utils;

/**
 * @author Peter
 *
 */
public class QuartzTaskProxy extends AbstractRecycleTask implements TaskProxy {

	private JobKey jobKey = null;

	private TriggerKey triggerKey = null;

	private JobDataMap dataMap = new JobDataMap();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.guttv.pm.core.task.AbstractTask#dispose()
	 */
	@Override
	public Object dispose(Object data) throws Exception {
		if (this.isFinished()) {
			logger.info("任务[" + jobKey + "]心跳时发现下次执行时间为空，退出调度任务");
			this.setStop(true);
		}

		Date preFireTime = QuartzTaskScheduler.getInstance().getNextFireTime(triggerKey);
		if (preFireTime != null) {
			logger.debug("下次执行时间：" + Utils.getTimeString(preFireTime.getTime()));
		}

		return null;
	}

	// 初始化方法
	protected void init() throws Exception {
		//
		dataMap.put(QuartzJobExecuter.PROXY_INSTANCE, proxy);
		dataMap.put(QuartzJobExecuter.PROXY_EXECUTE_METHOD, method);

		String name = this.getId() + this.getName();
		// 创建JobDetail
		JobDetail jobDetail = JobBuilder.newJob(QuartzJobExecuter.class).withIdentity(name, this.getFlowExeCode())
				.withDescription(null).setJobData(dataMap).build();

		Trigger trigger = TriggerBuilder.newTrigger().withIdentity(name, this.getFlowExeCode()).withPriority(priority)
				.withSchedule(CronScheduleBuilder.cronSchedule(crontab).withMisfireHandlingInstructionDoNothing())
				.build();

		triggerKey = trigger.getKey();

		jobKey = QuartzTaskScheduler.getInstance().scheduleJob(jobDetail, trigger);

		if (initMethod != null) {
			logger.debug("调用初始化方法：" + initMethod.getName());
			initMethod.invoke(proxy);
		}

		// 只是一个心跳，周期不易过短
		if (this.getPeriod() < miniPeriod) {
			this.setPeriod(miniPeriod);
		}

		// 暂时不支持读写
		this.setNeedRead(false);
		this.setNeedWrite(false);
	}

	// 任务调度周期 不能为空 ，不要轻易改字段名称，注册时判断组件中有没有该属性名称了
	private String crontab = null;
	// 任务执行周期
	private int priority = Trigger.DEFAULT_PRIORITY;

	private long miniPeriod = 30000;

	public String getCrontab() {
		return crontab;
	}

	public void setCrontab(String crontab) {
		this.crontab = crontab;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public long getMiniPeriod() {
		return miniPeriod;
	}

	public void setMiniPeriod(long miniPeriod) {
		this.miniPeriod = miniPeriod;
	}

	@Override
	public boolean isFinished() {
		try {
			return QuartzTaskScheduler.getInstance().getNextFireTime(triggerKey) == null;
		} catch (SchedulerException e) {
			logger.error("获取下次执行时间异常：" + e.getMessage(), e);
			return true;
		}
	}

	@Override
	public void setPause(boolean pause) {
		if (jobKey == null) {
			return;
		}
		try {
			if (pause) {
				QuartzTaskScheduler.getInstance().pauseJob(jobKey);
			} else {
				QuartzTaskScheduler.getInstance().resumeJob(jobKey);
			}
			super.setPause(pause);
		} catch (SchedulerException e) {
			logger.error("设置任务[" + jobKey + "]暂停状态[" + pause + "]异常：" + e.getMessage(), e);
		}
	}

	/*
	 * 停止任务 (non-Javadoc)
	 * 
	 * @see com.guttv.pm.core.task.AbstractRecycleTask#setStop(boolean)
	 */
	public void setStop(boolean stop) {
		if (stop) {
			try {
				QuartzTaskScheduler.getInstance().deleteJob(jobKey);
			} catch (SchedulerException e) {
				logger.error("移除任务[" + jobKey + "]异常：" + e.getMessage(), e);
			}
		}
		super.setStop(stop);
	}

	// 本方法需要递层向上层关闭
	public void close() {
		try {
			QuartzTaskScheduler.getInstance().deleteJob(jobKey);
		} catch (SchedulerException e) {
			logger.error("移除任务[" + jobKey + "]异常2：" + e.getMessage(), e);
		}

		if (proxy instanceof Closeable) {
			IOUtils.closeQuietly((Closeable) proxy);
		}

		if (closeMethod != null) {
			try {
				closeMethod.invoke(proxy);
			} catch (Throwable e) {
				logger.error(
						"组件[" + this.getComponentClz() + "]调用关闭方法[" + closeMethod.getName() + "]时异常：" + e.getMessage(),
						e);
			}
		}

		jobKey = null;
		triggerKey = null;
		proxy = null;
		method = null;
		initMethod = null;
		closeMethod = null;

		super.close();
	}

	private Object proxy = null;

	private Method method = null;

	private Method initMethod = null;

	private Method closeMethod = null;

	public Object getProxy() {
		return proxy;
	}

	public void setProxy(Object proxy) {
		this.proxy = proxy;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public Method getInitMethod() {
		return initMethod;
	}

	public void setInitMethod(Method initMethod) {
		this.initMethod = initMethod;
	}

	public Method getCloseMethod() {
		return closeMethod;
	}

	public void setCloseMethod(Method closeMethod) {
		this.closeMethod = closeMethod;
	}
}
