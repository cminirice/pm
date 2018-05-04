/**
 * 
 */
package com.guttv.pm.core.task;

import java.io.Closeable;
import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.guttv.pm.core.bean.ComponentNodeBean;
import com.guttv.pm.core.cache.TaskCache;
import com.guttv.pm.core.msg.queue.Consumer;
import com.guttv.pm.core.msg.queue.Producer;
import com.guttv.pm.utils.Enums.ComponentNodeStatus;

/**
 * @author Peter
 *
 */
public abstract class AbstractTask implements Runnable, Closeable {
	protected Logger logger = LoggerFactory.getLogger("task");

	protected Thread taskThread = null;

	private String receiveQueue = null;

	// 有错误数据时，把当前数据发送到该队列
	private String errorQueue = null;

	// 出现异常时，把异常信息发到该队列
	private String alarmQueue = null;
	
	//添加抄送队列，还发送的数据再发送到此队列里一份，异常和告警不会发到此队列
	private String duplicationQueue = null;

	private String id = null;

	private int maxDepth = 50000;

	protected AbstractTask() {
		id = UUID.randomUUID().toString().replaceAll("-", "");
	}

	// 启动任务
	public final void run() {

		finished = false;
		componentNode.setStatus(ComponentNodeStatus.RUNNING);
		componentNode.setStatusDesc("running");
		try {
			logger.info("准备启动任务[" + this.getName() + "]:" + this.getClass());

			taskThread = Thread.currentThread();
			taskThread.setName(this.getName());

			startTime = System.currentTimeMillis();
			TaskCache.getInstance().registTask(this);

			doHeartBeat();
			init();
			dispose();

			logger.info("任务[" + this.getName() + ":taskID=" + this.getId() + "][FlowExeCode=" + this.getFlowExeCode()
					+ "][FlowCode=" + this.getFlowCode() + "][comClz=" + this.getComponentClz() + "][NodeID="
					+ this.getNodeID() + "]结束" + this.getClass());

		} catch (Throwable error) {

			componentNode.setStatus(ComponentNodeStatus.ERROR);
			componentNode.setStatusDesc("执行异常：" + error.getMessage());

			// 处理告警
			try {
				this.writeAlarm(ExceptionUtils.getStackTrace(error));
			} catch (Exception e) {
				logger.error("发送异常信息告警时异常：" + e.getMessage());
			}

			logger.error("任务[" + this.getName() + ":taskID=" + this.getId() + "][FlowExeCode=" + this.getFlowExeCode()
					+ "][FlowCode=" + this.getFlowCode() + "][comClz=" + this.getComponentClz() + "][NodeID="
					+ this.getNodeID() + "]执行异常:" + error.getMessage(), error);
		} finally {

			finished = true; // 标记任务结束

			// 没有异常的，设置为执行结束
			if (componentNode.getStatus() != ComponentNodeStatus.ERROR) {
				componentNode.setStatus(ComponentNodeStatus.FINISH);
				componentNode.setStatusDesc(ComponentNodeStatus.FINISH.getName());
			}

			taskThread = new FinishedThread(); // 以前的线程有可能是从线程池中启动，任务结束后，线程被回收，并没有结束
			taskThread.setName(this.getName());

			endTime = System.currentTimeMillis();

			// 关闭
			IOUtils.closeQuietly(this);
		}
	}

	// 初始化方法
	protected void init() throws Exception {
	}

	// 本层有需要关闭的，
	public void close() {
		if (this.consumer != null) {
			IOUtils.closeQuietly(consumer);
			this.consumer = null;
		}
		if (this.producers.size() > 0) {
			for (Producer p : producers) {
				IOUtils.closeQuietly(p);
			}
			this.producers.clear();
		}
		if (errorProducer != null) {
			IOUtils.closeQuietly(errorProducer);
		}
		if (alarmProducer != null) {
			IOUtils.closeQuietly(alarmProducer);
		}
	}

	protected Object readData(int timeout) throws Exception {
		if (consumer == null)
			return null;
		Object obj = consumer.read(timeout);
		if (obj != null) {
			logger.debug("[" + this.getName() + "]收到数据：" + obj);
		}
		return obj;
	}

	protected Object readData() throws Exception {
		if (consumer == null)
			return null;
		Object obj = consumer.read();
		if (obj != null) {
			logger.debug("[" + this.getName() + "]收到数据：" + obj);
		}
		return obj;
	}

	protected void fallback(Object data) throws Exception {
		if (consumer != null) {
			consumer.fallback(data);
		}
	}

	protected void commit(Object data) throws Exception {
		if (consumer != null) {
			consumer.commit(data);
		}
	}

	/**
	 * 发送到所有的消费者，如果有一个及以上消费者发送异常，返回:false 一个消费者发送失败，不会影响其它的消费者
	 * 
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	protected boolean writeData(Object obj) throws Exception {
		if (obj == null) {
			return true;
		}
		boolean result = true;
		for (Producer p : producers) {
			try {
				if (p.size() > maxDepth) {
					logger.warn(
							"队列[" + p.getName() + "]的深度为[" + p.size() + "]，大于设置最大上限值[" + maxDepth + "]，丢弃数据：" + obj);
					continue;
				}

				if (p.checkRule(obj)) {
					p.write(obj);
					logger.debug("[" + this.getName() + "]发送数据到[" + p.getName() + "]消费者：" + obj);
				} else {
					logger.debug("检查规则未通过，[" + this.getName() + "]不能发送数据到[" + p.getName() + "]消费者：" + obj);
				}

			} catch (Exception e) {
				// throw new Exception("发送数据["+p.getName()+"]失败：" +
				// e.getMessage(),e);
				logger.error("[" + this.getName() + "]发送数据到[" + p.getName() + "]失败：" + e.getMessage(), e);
				result = false;
			}
		}
		return result;
	}

	/**
	 * 处理业务逻辑，如果是常驻线程，需要在此方法内循环执行
	 * 
	 * @throws Exception
	 */
	public abstract void dispose() throws Exception;

	// 任务心跳信息
	private long lastHeartBeatTime = 0;

	protected final void doHeartBeat() {
		lastHeartBeatTime = System.currentTimeMillis();
	}

	public long getLastHeartBeatTime() {
		return lastHeartBeatTime;
	}

	// 任务启始时间
	private long startTime = 0;
	private long endTime = 0;

	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	// 生产者和消息者
	private Consumer consumer = null;
	private final List<Producer> producers = new ArrayList<Producer>();

	public void setConsumer(Consumer consumer) {
		logger.info(this.getName() + " 添加消费者：" + (consumer == null ? "null" : consumer.getClass()));
		this.consumer = consumer;
	}

	public void addProducer(Producer producer) {
		if (producer != null) {
			logger.info(this.getName() + " 添加生产者：" + producer.getClass());
			producers.add(producer);
		}
	}

	/**
	 * 错误信息的输出通道
	 */
	private Producer errorProducer = null;

	public void setErrorProducer(Producer errorProducer) {
		this.errorProducer = errorProducer;
	}

	public void writerError(Object obj) throws Exception {
		if (errorProducer != null) {
			errorProducer.write(obj);
		}
	}

	/**
	 * 系统告警信息的输出通道
	 */
	private Producer alarmProducer = null;

	public void setAlarmProducer(Producer alarmProducer) {
		this.alarmProducer = alarmProducer;
	}

	public void writeAlarm(Object obj) throws Exception {
		if (alarmProducer != null) {
			alarmProducer.write(obj);
		}
	}

	// 任务名称
	private String name = null;

	public String getName() {
		return name == null ? this.getClass().getSimpleName() : name;
	}

	public void setName(String name) {
		this.name = name;
	}

	// 任务是否结束
	private boolean finished = false;

	public boolean isFinished() {
		return finished;
	}

	public String getReceiveQueue() {
		return receiveQueue;
	}

	public void setReceiveQueue(String receiveQueue) {
		this.receiveQueue = receiveQueue;
	}

	public String getId() {
		return id;
	}

	public int getMaxDepth() {
		return maxDepth;
	}

	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}

	/**
	 * 获取任务执行线程的状态
	 * 
	 * @return
	 */
	public State getThreadState() {
		if (taskThread == null) {
			logger.warn("task[" + this.getName() + "] has not been started yet!");
		}
		return taskThread == null ? null : taskThread.getState();
	}

	public String getThreadStackTrace() {
		StringBuilder sb = new StringBuilder();
		if (taskThread == null) {
			logger.warn("task[" + this.getName() + "] has not been started yet!");
			return "null";
		}
		StackTraceElement[] traces = taskThread.getStackTrace();
		for (StackTraceElement elem : traces) {
			sb.append(elem).append("\r\n");
		}
		return sb.toString();
	}

	private boolean needRead = true;
	private boolean needWrite = true;

	public boolean isNeedRead() {
		return needRead;
	}

	public void setNeedRead(boolean needRead) {
		this.needRead = needRead;
	}

	public boolean isNeedWrite() {
		return needWrite;
	}

	public void setNeedWrite(boolean needWrite) {
		this.needWrite = needWrite;
	}

	/**
	 * 下面是组件相关的属性
	 */
	private String flowExeCode = null; // 流程执行编码
	private ComponentNodeBean componentNode = null;

	public String getComponentClz() {
		return componentNode == null ? null : componentNode.getComponentClz();
	}

	public String getComponentCn() {
		return componentNode == null ? null : componentNode.getComponentCn();
	}

	public String getNodeID() {
		return componentNode == null ? null : componentNode.getNodeID();
	}

	public String getFlowCode() {
		return componentNode == null ? null : componentNode.getFlowCode();
	}

	public String getFlowExeCode() {
		return flowExeCode;
	}

	public void setFlowExeCode(String flowExeCode) {
		this.flowExeCode = flowExeCode;
	}

	public ComponentNodeBean getComponentNode() {
		return componentNode;
	}

	public void setComponentNode(ComponentNodeBean componentNode) {
		this.componentNode = componentNode;
	}

	/**
	 * 当运行
	 * 
	 * @author Peter
	 *
	 */
	public static class FinishedThread extends Thread {
		public FinishedThread() {
			this.start();
		}

		public void run() {
			/**
			 * 空方法
			 */
		}
	}

	public String getErrorQueue() {
		return errorQueue;
	}

	public void setErrorQueue(String errorQueue) {
		this.errorQueue = errorQueue;
	}

	public String getAlarmQueue() {
		return alarmQueue;
	}

	public void setAlarmQueue(String alarmQueue) {
		this.alarmQueue = alarmQueue;
	}

	public String getDuplicationQueue() {
		return duplicationQueue;
	}

	public void setDuplicationQueue(String duplicationQueue) {
		this.duplicationQueue = duplicationQueue;
	}

}
