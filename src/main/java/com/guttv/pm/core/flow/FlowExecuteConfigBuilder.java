/**
 * 
 */
package com.guttv.pm.core.flow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.guttv.pm.core.bean.ComponentBean;
import com.guttv.pm.core.bean.ComponentDispatchBean;
import com.guttv.pm.core.bean.ComponentFlowProBean;
import com.guttv.pm.core.bean.ComponentNodeBean;
import com.guttv.pm.core.bean.ComponentProBean;
import com.guttv.pm.core.bean.FlowBean;
import com.guttv.pm.core.cache.ComponentCache;
import com.guttv.pm.core.cache.FlowCache;
import com.guttv.pm.core.msg.ProducerFactory;
import com.guttv.pm.core.task.SimpleDispathRule;
import com.guttv.pm.core.zk.DistributedSequence;
import com.guttv.pm.utils.Enums.FlowStatus;

/**
 * @author Peter
 *
 */
public class FlowExecuteConfigBuilder {

	private static Logger logger = LoggerFactory.getLogger(FlowExecuteConfigBuilder.class);

	public static void main(String[] a) throws Exception {
		SimpleDispathRule rule = new SimpleDispathRule("asdfwerf");
		System.out.println("############################" + rule.check(null));
	}

	/**
	 * 创建流程执行对象，没有存入缓存
	 * 
	 * @param flowCode
	 * @return
	 * @throws Exception
	 */
	public static FlowExecuteConfig build(String flowCode) throws Exception {
		if (StringUtils.isBlank(flowCode)) {
			throw new Exception("流程编码参数为空");
		}

		FlowBean flow = FlowCache.getInstance().getFlow(flowCode);

		if (flow == null) {
			throw new Exception("不存在code为[" + flowCode + "]的流程");
		}

		if (flow.getStatus() != FlowStatus.NORMAL) {
			throw new Exception("流程状态异常[" + flow.getStatus() + "]异常");
		}

		FlowExecuteConfig flowExeConfig = build(flowCode, flow.getFlowContent(), flow.getFlowComPros(),
				flow.getNodeVSCom());

		flowExeConfig.setFlow(flow);

		return flowExeConfig;
	}

	/**
	 * 
	 * @param flowCode
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static FlowExecuteConfig build(String flowCode, String gooFlowContent, String flowComPros, String nodeVSCom)
			throws Exception {

		FlowExecuteConfig flowExeConfig = new FlowExecuteConfig();
		flowExeConfig.setFlowExeCode(RandomStringUtils.randomNumeric(16));
		flowExeConfig.setId(DistributedSequence.getInstance().getNext());

		Gson gson = new Gson();
		// 转换字符串为Map
		Map<String, Map<String, String>> flowComProsMap = gson.fromJson(flowComPros, Map.class);
		Map<String, String> nodeVSComMap = gson.fromJson(nodeVSCom, Map.class);
		Map gooMap = gson.fromJson(gooFlowContent, Map.class);

		// 定义属性存储
		List<ComponentDispatchBean> comDispatchs = new ArrayList<ComponentDispatchBean>();
		Map<String, List<ComponentFlowProBean>> comFlowProsMap = new HashMap<String, List<ComponentFlowProBean>>();
		List<ComponentNodeBean> comNodes = new ArrayList<ComponentNodeBean>();
		flowExeConfig.setComDispatch(comDispatchs);
		flowExeConfig.setComFlowProsMap(comFlowProsMap);
		flowExeConfig.setComNodes(comNodes);

		// 解析流程名称
		String title = (String) gooMap.get("title");
		if (StringUtils.isBlank(title)) {
			title = "无名流程";
		}

		// 解析流程组件节点
		Map nodesMap = (Map) gooMap.get("nodes");
		if (nodesMap == null || nodesMap.size() == 0) {
			throw new Exception("流程[" + title + "]内容里没有发现任何组件节点");
		}

		// 校验所有的节点是否存在
		Iterator<String> nodeIDIter = nodesMap.keySet().iterator();
		String nodeID = null;
		String clz = null;
		boolean flag = false; // 标记是否有实结点，
		while (nodeIDIter.hasNext()) {
			nodeID = nodeIDIter.next();
			clz = nodeVSComMap.get(nodeID);
			if (StringUtils.isBlank(clz)) {
				// 这个地方是虚节点，丢弃
				continue;
			}

			flag = true; // 有实结点

			ComponentBean com = ComponentCache.getInstance().getComponent(clz);
			if (com == null) {
				String error = "不存在class为[" + clz + "]的组件，nodeID为[" + nodeID + "],流程名称为：" + title;
				logger.error(error);
				throw new Exception(error);
			}

			// 包装 ComNodeBean
			ComponentNodeBean comNode = new ComponentNodeBean();
			comNode.setFlowCode(flowCode);
			comNode.setNodeID(nodeID);
			comNode.setComponentClz(clz);
			comNode.setComID(com.getComID());
			Map nodeData = (Map) nodesMap.get(nodeID);
			if (nodeData != null) { // 流程中重新命名的名字优先
				Object cn = nodeData.get("name");
				if (cn != null) {
					comNode.setComponentCn(cn.toString());
				}
			}
			if (StringUtils.isBlank(comNode.getComponentCn())) {
				comNode.setComponentCn(com.getCn());
			}

			comNodes.add(comNode);
		}

		if (!flag) {
			throw new Exception("流程[" + title + "]内容里没有发现任何实节点");
		}

		// 解析流程的连接线
		Map<String, Map<String, String>> linesMap = (Map) gooMap.get("lines");
		if (linesMap != null && linesMap.size() > 0) {

			// "from\":\"demo_node_26\",\"to\":\"demo_node_27\",\"name\":\"DATA.equals(\\\"guttv\\\")
			String fromNodeID = null;
			String toNodeID = null;
			String rule = null;
			String fromClz = null;
			String toClz = null;
			Map<String, String> line = null;
			for (String lineID : linesMap.keySet()) {
				line = linesMap.get(lineID);
				fromNodeID = line.get("from");
				toNodeID = line.get("to");
				rule = line.get("name");

				fromClz = nodeVSComMap.get(fromNodeID);
				toClz = nodeVSComMap.get(toNodeID);
				if (StringUtils.isBlank(fromClz) && StringUtils.isBlank(toClz)) {
					logger.warn("连接线[" + lineID + "]from[" + fromNodeID + "]to[" + toNodeID + "]的两端都为虚节点，丢弃连接线，流程名称："
							+ title);
					continue;
				}

				// 包装发送关系 有虚节点儿的线
				ComponentDispatchBean comDispatch = new ComponentDispatchBean();
				comDispatch.setLineID(lineID);
				comDispatch.setFlowCode(flowCode);

				// 设置组件发送队列
				comDispatch.setFromNode(fromNodeID);
				comDispatch.setFromComponent(fromClz); // fromClz为空的是虚节点
				// 校验开始结点
				if (StringUtils.isNotBlank(fromClz)) {

					ComponentBean fromCom = ComponentCache.getInstance().getComponent(fromClz);

					// 校验实现类是否存在
					if (fromCom == null) {
						String error = "连接线[" + lineID + "]from[" + fromNodeID + "]to[" + toNodeID + "]的起点组件[" + fromClz
								+ "]不存在，流程名称为：" + title;
						logger.error(error);
						throw new Exception(error);
					}

					// 校验连接线的开始结点有没有写的能力
					if (!fromCom.isNeedWrite()) {
						String error = "连接线[" + lineID + "]from[" + fromNodeID + "]to[" + toNodeID + "]的起点组件[" + fromClz
								+ "][" + fromCom.getCn() + "]不能有写操作，该流程无效,流程名称为：" + title;
						logger.error(error);
						throw new Exception(error);
					}
				}

				// 设置组件发送队列
				comDispatch.setToNode(toNodeID);
				comDispatch.setToComponent(toClz); // toClz为空的是虚节点
				comDispatch.setRule(rule);

				// 校验终点
				if (StringUtils.isNotBlank(toClz)) {
					ComponentBean toCom = ComponentCache.getInstance().getComponent(toClz);

					// 校验实现类是否存在
					if (toCom == null) {
						String error = "连接线[" + lineID + "]from[" + fromNodeID + "]to[" + toNodeID + "]的终点组件[" + toClz
								+ "]不存在，流程名称为：" + title;
						logger.error(error);
						throw new Exception(error);
					}

					// 校验连接线的终点是否有读能力
					if (toCom != null && !toCom.isNeedRead()) {
						String error = "连接线[" + lineID + "]from[" + fromNodeID + "]to[" + toNodeID + "]的终点组件[" + toClz
								+ "][" + toCom.getCn() + "]不能有读操作，该流程无效,流程名称为：" + title;
						logger.error(error);
						throw new Exception(error);
					}

					// 设置队列
					if (StringUtils.isNotBlank(toCom.getReceive())) {
						comDispatch.setQueue(toCom.getReceive());
					} else {
						// comDispatch.setQueue(toCom.getQueueType() +
						// toCom.getName());
						String type = StringUtils.isBlank(toCom.getQueueType()) ? ProducerFactory.RABBIT_PRE
								: toCom.getQueueType();
						String path = flowExeConfig.getId() + "_" + toNodeID + (StringUtils.isBlank(toCom.getName()) ? ""
								: ("_" + toCom.getName()));
						comDispatch.setQueue(type + path);
					}

					// 校验 组件可以做为多条线的终点，但多条线的队列必须是一样的，也就是组件只能从一个队列里读数据
					// 此处不再校验，画图时可以不一样，后面给出修改页面，启动时再校验此限制
					/*
					 * for(ComponentDispatchBean cd : comDispatchs) {
					 * if(toClz.equals(cd.getToComponent()) &&
					 * !comDispatch.getQueue().equals(cd.getQueue())) { String
					 * error =
					 * "连接线标识["+lineID+"]和连接线标识["+cd.getLineID()+"]的终点组件都是["+
					 * toClz+"]，一个有效组件只能从一个队列里读取，该流程无效,流程名称为：" + title;
					 * logger.error(error); throw new Exception(error); } }
					 */

				}

				// 添加到集合里
				comDispatchs.add(comDispatch);
			}
		}

		// 按本流程的节点设置属性值
		for (ComponentNodeBean cnb : comNodes) {
			ComponentBean component = ComponentCache.getInstance().getComponent(cnb.getComponentClz());
			List<ComponentProBean> comPros = component.getComponentPros();
			if (comPros == null || comPros.size() == 0) {
				// 该组件不需要属性值
				continue;
			}

			// 如果需要读的话，得有以这个节点为终点的连线
			if (component.isNeedRead()) {
				boolean find = false;
				for (ComponentDispatchBean cdb : comDispatchs) {
					if (cnb.getNodeID().equals(cdb.getToNode())) {
						find = true;
					}
				}
				if (!find) {
					throw new Exception("节点[" + cnb.getNodeID() + "][" + cnb.getComponentClz()
							+ "]对应的组件需要读取数据，但从流程中没有发现以该结点为终点的连接，不能创建流程执行配置");
				}
			}

			List<ComponentFlowProBean> comFlowPros = new ArrayList<ComponentFlowProBean>();
			ComponentFlowProBean comFlowPro = null;
			Map<String, String> proMap = flowComProsMap.get(cnb.getNodeID());
			;
			comFlowProsMap.put(cnb.getNodeID(), comFlowPros);

			// 遍历源属性值，设置流程中的属性值
			for (ComponentProBean comPro : comPros) {
				comFlowPro = fromComPro(comPro);
				comFlowPro.setFlowCode(flowCode);
				comFlowPro.setNodeID(cnb.getNodeID());

				// 如果配置中有该属性配置，不管是否为空，保存配置中的值
				if (proMap != null && proMap.containsKey(comFlowPro.getName())) {
					comFlowPro.setValue(proMap.get(comFlowPro.getName()));
				}

				// 添加保存
				comFlowPros.add(comFlowPro);
			}
		}

		return flowExeConfig;
	}

	/**
	 * 此处需要复制一份，否则会把缓存的数据修改 从组件属性对象 复制为 组件流程属性对象
	 * 
	 * @param comPro
	 * @return
	 */
	private static ComponentFlowProBean fromComPro(ComponentProBean tp) {
		ComponentFlowProBean comFlowPro = new ComponentFlowProBean();

		comFlowPro.setComponentClz(tp.getComponentClz());
		comFlowPro.setType(tp.getType());
		comFlowPro.setCn(tp.getCn());
		comFlowPro.setName(tp.getName());
		comFlowPro.setValue(tp.getValue());
		comFlowPro.setCreateTime(tp.getCreateTime());
		comFlowPro.setUpdateTime(tp.getUpdateTime());

		// 这个方法会降低效率,复制得1.5秒左右，但增加属性时不用考虑此处
		// BeanUtils.copyProperties(tp, comFlowPro);

		return comFlowPro;
	}
}
