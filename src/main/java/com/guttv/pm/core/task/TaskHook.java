/**
 * 
 */
package com.guttv.pm.core.task;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.guttv.pm.core.cache.TaskCache;

/**
 * @author Peter
 *
 */
public class TaskHook extends Thread {
	protected Logger logger = LoggerFactory.getLogger("task");
	@Override
	public void run() {
		logger.error("系统被终止，所有的任务将被停止...");
		List<AbstractTask> taskList = TaskCache.getInstance().getAllTasks();
		if(taskList != null && taskList.size() > 0) {
			for(AbstractTask at : taskList) {
				logger.info("线程[" + at.getName() + "][" + at.getId() + "] 将被停止....");
				if(at instanceof AbstractRecycleTask) {
					AbstractRecycleTask art = (AbstractRecycleTask)at;
					art.setStop(true);
				}
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			
			//下面检查任务是否均停止
			long start = System.currentTimeMillis();
			boolean running = true;
			//如果等待时间小于10S，并且有任务在运行
			while(System.currentTimeMillis() - start < 10000 && running) {
				//先假设所有的线程都运行结束
				running = false;
				for(AbstractTask at : taskList) {
					if(!at.isFinished()) {
						logger.info("线程[" + at.getName() + "][" + at.getId() + "]未执行结束，线程状态：" + at.getThreadStackTrace());
						//如果有一个线程正在运行，或者阻塞状态，认为任务正在执行
						running = true;
						break;
					}
				}
				
				if(running) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
			}
			
		}
		
		logger.info("系统退出");
		System.exit(0);
	}
}
