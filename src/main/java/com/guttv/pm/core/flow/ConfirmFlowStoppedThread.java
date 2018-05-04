/**
 * 
 */
package com.guttv.pm.core.flow;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.guttv.pm.core.task.AbstractTask;
import com.guttv.pm.utils.Enums.FlowExecuteStatus;

/**
 * 确认流程的任务是否结束
 * @author Peter
 *
 */
class ConfirmFlowStoppedThread extends Thread{

	private static Logger logger = LoggerFactory.getLogger(ConfirmFlowStoppedThread.class);
	
	private List<AbstractTask> tasks = null;
	private FlowExecuteConfig flowExeConfig = null;
	
	//费博那契数列
	private int  checkPeriod[]= {1,1,2,3,5,8,13,21,34};
	private int max_check_time = 12;
	
	public ConfirmFlowStoppedThread(FlowExecuteConfig flowExeConfig,List<AbstractTask> tasks) {
		this.flowExeConfig = flowExeConfig;
		this.tasks = tasks;
		this.setDaemon(true);
		this.setPriority(Thread.MIN_PRIORITY);
	}
	
	public void run() {
		if(tasks == null || tasks.size() == 0) {
			return;
		}
		
		//标记所有的线程是否都结束
		boolean flag = false;
		int times = 0;  //检查次数
		//如果有线没有结束，就一直检查
		while(!flag) {
			try {
				Thread.sleep(checkPeriod[times >= checkPeriod.length ? (checkPeriod.length-1):times]*1000);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(),e);
				return;
			}
			
			times++;
			flag = true; //判断前，假定所有的都执行完了
			for(AbstractTask at : tasks) {
				if(!at.isFinished()) {
					
					flag = false;
					
					logger.info("流程执行实例["+flowExeConfig.getFlowExeCode()+"]["+flowExeConfig.getFlowName()+"]在第["+times+"]次检查时，组件["+at.getNodeID()+"]["+at.getComponentClz()+"]还未结束["+at.isFinished()+"]"+at.getThreadState());
					
					if(times % max_check_time == 0) {
						//此处可以采用强制停止
						logger.error("流程执行实例[" + flowExeConfig.getFlowExeCode() + "][" + flowExeConfig.getFlowName()+"]在第["+times+"]次检查时，组件["+at.getNodeID()+"]["+at.getComponentClz()+"]还未结束["+at.isFinished()+"]，任务堆栈信息：" + at.getThreadStackTrace());
						flowExeConfig.setStatusDesc("执行配置被停止后，经过["+times+"]个周期的检测，仍有任务没有结束");
					}
					break;
				}
			}
		}
		
		try {
			flowExeConfig.setStatus(FlowExecuteStatus.FINISH, "检测到流程已经执行结束");
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		logger.info("流程执行实例[FlowExeCode="+flowExeConfig.getFlowExeCode()+"][FlowCode="+flowExeConfig.getFlowCode()+"][FlowName="+flowExeConfig.getFlowName()+"]在第["+times+"]次检查时，发现已经全部停止，监控线程退出.");
		
		this.flowExeConfig = null;
		this.tasks = null;
	}
}
