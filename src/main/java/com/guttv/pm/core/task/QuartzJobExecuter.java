package com.guttv.pm.core.task;

import java.lang.reflect.Method;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuartzJobExecuter implements Job {
	protected Logger logger = LoggerFactory.getLogger("task");
	public static final String PROXY_INSTANCE = "proxy_instance";
	public static final String PROXY_EXECUTE_METHOD = "proxy_execute_method";

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.info("开始执行任务：" + context.getJobDetail().getKey());
		JobDataMap dataMap = context.getMergedJobDataMap();
		Object instance = dataMap.get(PROXY_INSTANCE);
		Method method = (Method) dataMap.get(PROXY_EXECUTE_METHOD);
		try {
			method.invoke(instance);
		} catch (Exception e) {
			logger.error("执行任务[" + context.getJobDetail().getKey() + "]异常：" + e.getMessage(), e);
		}
	}
}
