/**
 * 
 */
package com.guttv.pm.core.flow;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.guttv.pm.core.bean.ComponentBean;
import com.guttv.pm.core.bean.ComponentDispatchBean;
import com.guttv.pm.core.bean.ComponentFlowProBean;
import com.guttv.pm.core.bean.ComponentNodeBean;
import com.guttv.pm.core.cache.ComClassLoaderCache;
import com.guttv.pm.core.msg.ConsumerFactory;
import com.guttv.pm.core.msg.ProducerFactory;
import com.guttv.pm.core.msg.queue.Producer;
import com.guttv.pm.core.task.AbstractTask;
import com.guttv.pm.core.task.OnceTaskProxy;
import com.guttv.pm.core.task.QuartzTaskProxy;
import com.guttv.pm.core.task.RecycleTaskProxy;
import com.guttv.pm.core.task.RestTaskProxy;
import com.guttv.pm.core.task.SimpleDispathRule;
import com.guttv.pm.core.task.TaskProxy;
import com.guttv.pm.utils.Enums.ComponentNodeStatus;
import com.guttv.pm.utils.Enums.ComponentProType;
import com.guttv.pm.utils.Enums.ComponentRunType;
import com.guttv.pm.utils.ReflectUtil;

/**
 * @author Peter
 *
 */
public class TaskBuilder {
	private static Logger logger = LoggerFactory.getLogger(TaskBuilder.class);

	public static TaskBuilder newBuilder() {
		return new TaskBuilder();
	}

	// 流程配置
	private FlowExecuteConfig flowExeConfig = null;
	// 节点
	private ComponentNodeBean cnb = null;
	// 组件
	private ComponentBean com = null;
	// 下标
	private int index = 1;

	public TaskBuilder withFlowExecuteConfig(FlowExecuteConfig fec) {
		this.flowExeConfig = fec;
		return this;
	}

	public TaskBuilder withComponentNode(ComponentNodeBean cnb) {
		this.cnb = cnb;
		return this;
	}

	public TaskBuilder withComponent(ComponentBean com) {
		this.com = com;
		return this;
	}

	public TaskBuilder withIndex(int index) {
		this.index = index;
		return this;
	}

	/**
	 * 创建任务对象，把属性值赋进去
	 * 
	 * @return
	 * @throws Exception
	 */
	public AbstractTask build() throws Exception {
		AbstractTask at = null;
		Object comInstance = ComClassLoaderCache.getInstance().getInstance(com.getComID(), cnb.getComponentClz());
		Class<?> comClz = comInstance.getClass();
		List<ComponentFlowProBean> cfps = flowExeConfig.getComFlowPros(cnb.getNodeID());

		if (comInstance instanceof AbstractTask) {
			at = (AbstractTask) comInstance;

			// 设置对象的属性
			if (cfps != null && cfps.size() > 0) {
				for (ComponentFlowProBean cfp : cfps) {
					try {
						ReflectUtil.evaluate2Field(at, cfp.getName(), cfp.getValue());
					} catch (Exception e) {
						String error = "流程执行配置[" + flowExeConfig.getFlowCode() + "]中的节点[" + cnb.getNodeID() + "]对应的组件["
								+ cnb.getComponentClz() + "]向属性赋值[field=" + cfp.getName() + "][value=" + cfp.getValue()
								+ "]失败：" + e.getMessage();
						logger.warn(error);
					}
				}
			}

		} else {
			// 其它的任务使用代理启动

			TaskProxy taskProxy = null;

			if (ComponentRunType.Cycle.equals(com.getRunType())) {// 周期运行
				at = new RecycleTaskProxy();
				taskProxy = (RecycleTaskProxy) at;
			} else if (ComponentRunType.Once.equals(com.getRunType())) {// 单次运行
				at = new OnceTaskProxy();
				taskProxy = (OnceTaskProxy) at;
			} else if (ComponentRunType.Scheduler.equals(com.getRunType())) {// quartz调度
				at = new QuartzTaskProxy();
				taskProxy = (QuartzTaskProxy) at;
			} else if (ComponentRunType.Rest.equals(com.getRunType())) {// rest
				at = new RestTaskProxy();
				taskProxy = (RestTaskProxy) at;
			} else {
				throw new Exception("组件有不支持的运行类型[" + com.getRunType() + "]");
			}

			taskProxy.setProxy(comInstance);

			// rest类型的任务不需要校验method
			if (!ComponentRunType.Rest.equals(com.getRunType())) {
				try {
					Method method = null;
					if (com.isNeedRead()) {
						method = comClz.getDeclaredMethod(com.getMethod(), Object.class);
					} else {
						method = comClz.getDeclaredMethod(com.getMethod());
					}
					taskProxy.setMethod(method);
				} catch (Exception e) {
					String error = "流程执行配置[" + flowExeConfig.getFlowCode() + "]在找节点[" + cnb.getNodeID() + "]组件["
							+ com.getName() + "]的方法[" + com.getMethod() + "]时异常:" + e.getMessage();
					logger.error(error);
					IOUtils.closeQuietly(at);
					throw new Exception(error);
				}
			}

			// 校验初始化方法
			if (StringUtils.isNotBlank(com.getInitMethod())) {
				taskProxy.setInitMethod(comClz.getDeclaredMethod(com.getInitMethod()));
			}

			// 校验关闭方法
			if (StringUtils.isNotBlank(com.getCloseMethod())) {
				taskProxy.setCloseMethod(comClz.getDeclaredMethod(com.getCloseMethod()));
			}

			// 设置对象的属性
			if (cfps != null && cfps.size() > 0) {
				for (ComponentFlowProBean cfp : cfps) {
					try {
						// 需要区分开类型赋值
						if (cfp.getType() == ComponentProType.NOR) {
							// 这个是组件配置
							ReflectUtil.evaluate2Field(comInstance, cfp.getName(), cfp.getValue());
						} else {
							// 这个是系统的任务配置
							ReflectUtil.evaluate2Field(taskProxy, cfp.getName(), cfp.getValue());
						}

					} catch (Exception e) {
						String error = "流程执行配置[" + flowExeConfig.getFlowCode() + "]中的节点[" + cnb.getNodeID() + "]对应的组件["
								+ cnb.getComponentClz() + "]向属性赋值[field=" + cfp.getName() + "][value=" + cfp.getValue()
								+ "]失败：" + e.getMessage();
						logger.warn(error);
					}
				}
			}

		}

		// 配置任务参数
		String flowExeCode = flowExeConfig.getFlowExeCode();

		at.setName(com.getName() + "-" + cnb.getNodeID() + "-" + index);
		at.setFlowExeCode(flowExeConfig.getFlowExeCode());
		at.setComponentNode(cnb);
		at.setNeedRead(com.isNeedRead());
		at.setNeedWrite(com.isNeedWrite());

		List<ComponentDispatchBean> comDispatchs = flowExeConfig.getComDispatchs();

		// 如果需要读的话，设置消费者
		if (com.isNeedRead()) {
			// 创建消费者
			boolean flag = false; // 标记是否找到
			for (ComponentDispatchBean cd : comDispatchs) {

				// 此时的每条线都必须有列队名称
				if (StringUtils.isBlank(cd.getQueue())) {
					String error = "流程执行配置[" + flowExeCode + "][lineID=" + cd.getLineID() + "]的队列名称为空";
					logger.error(error);
					throw new Exception(error);
				}

				// 两个节点指向同一个结点的时候，两条线的通道应该相同
				if (cnb.getNodeID().equals(cd.getToNode())) {
					if (!flag) {
						// 如果是首次找到读队列，直接赋值
						at.setReceiveQueue(cd.getQueue());
						flag = true; // 标记已经找到读的队列
						try {
							at.setConsumer(ConsumerFactory.create(cd.getQueue()));
						} catch (Exception e) {
							String error = "流程执行配置[" + flowExeCode + "]生成组件[name=" + com.getName() + "][lineID="
									+ cd.getLineID() + "]的消息队列时异常：" + e.getMessage();
							logger.error(error);
							throw new Exception(error);
						}

						// 验证启点是否被禁用 打出警告
						ComponentNodeBean fromNode = flowExeConfig.getComponentNode(cd.getFromNode());
						if (fromNode != null && fromNode.getStatus() == ComponentNodeStatus.FORBIDDEN) {
							String warn = "节点[" + cd.getToNode() + "]需要从节点[" + cd.getFromNode() + "]中读取数据，但是["
									+ cd.getFromNode() + "]是禁用状态，有可能影响正常流程正常运行";
							logger.warn(warn);
							flowExeConfig.setStatusDesc(warn);
						}
					} else {
						// 如果以前已经有过读队列，比较接收队列名称是否一样。也就是一个组件只能从同一个队列中读数据。
						if (!cd.getQueue().equals(at.getReceiveQueue())) {
							// 如果指向同一节点的多条线的队列名称不一样，抛异常
							String error = "流程执行配置[" + flowExeCode + "]中有多条线指向节点[name=" + com.getName() + ";nodeID="
									+ cnb.getNodeID() + "]，但队列名称不一样，流程启动异常";
							logger.error(error);
							throw new Exception(error);
						}
					}
				}
			}

			// 如果没有找到，发出异常
			if (!flag) {
				String error = "流程执行配置[" + flowExeCode + "]中节点[name=" + com.getName() + ";nodeID=" + cnb.getNodeID()
						+ "]对应组件配置需要读数据，但没有找到队列V_V";
				logger.error(error);
				throw new Exception(error);
			}
		}

		Producer producer = null;
		// 如果需要写
		if (com.isNeedWrite()) {

			// 创建生产者
			boolean flag = false; // 标记是否找到
			for (ComponentDispatchBean cd : comDispatchs) {

				// 此时的每条线都必须有列队名称
				if (StringUtils.isBlank(cd.getQueue())) {
					String error = "流程执行配置[" + flowExeCode + "][lineID=" + cd.getLineID() + "]的队列名称为空";
					logger.error(error);
					throw new Exception(error);
				}

				// 设置生产者
				if (cnb.getNodeID().equals(cd.getFromNode())) {
					try {
						producer = ProducerFactory.create(cd.getQueue());

						if (!StringUtils.isBlank(cd.getRule())) {
							producer.setDispatchRule(new SimpleDispathRule(cd.getRule()));
						}
						at.addProducer(producer);
						flag = true;

						// 验证终点是否被禁用 打出警告
						ComponentNodeBean toNode = flowExeConfig.getComponentNode(cd.getToNode());
						if (toNode != null && toNode.getStatus() == ComponentNodeStatus.FORBIDDEN) {
							String warn = "节点[" + cd.getFromNode() + "]需要向节点[" + cd.getToNode() + "]中写取数据，但是["
									+ cd.getToNode() + "]是禁用状态，有可能影响正常流程正常运行";
							logger.warn(warn);
							flowExeConfig.setStatusDesc(warn);
						}

					} catch (Exception e) {
						String error = "流程执行配置[" + flowExeCode + "]生成组件[name=" + com.getName() + "][lineID="
								+ cd.getLineID() + "]的消息队列时异常：" + e.getMessage();
						logger.error(error);
						throw new Exception(error);
					}
				}
			}

			// 如果没有找到，写的时候如果到不到生产者，只提示，不抛异常
			if (!flag) {
				String error = "流程执行配置[" + flowExeCode + "]中节点[name=" + com.getName() + ";nodeID=" + com.getComID()
						+ "]对应组件配置需要写数据，但没有找到队列V_V。但流程将被继续启动";
				logger.warn(error);
			}

			// 处理异常通道
			at.setErrorProducer(ProducerFactory.create(at.getErrorQueue()));
			// 处理告警通道
			at.setAlarmProducer(ProducerFactory.create(at.getAlarmQueue()));
			//添加抄送队列
			if(StringUtils.isNotBlank(at.getDuplicationQueue())) {
				at.addProducer(ProducerFactory.create(at.getDuplicationQueue()));
			}
		}

		return at;
	}
}
