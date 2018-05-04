/**
 * 
 */
package com.guttv.pm.platform.action.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.guttv.pm.core.bean.ComponentBean;
import com.guttv.pm.core.bean.ComponentDispatchBean;
import com.guttv.pm.core.bean.ComponentFlowProBean;
import com.guttv.pm.core.bean.ComponentNodeBean;
import com.guttv.pm.core.bean.FlowBean;
import com.guttv.pm.core.cache.ComponentCache;
import com.guttv.pm.core.cache.FlowCache;
import com.guttv.pm.core.cache.FlowExecuteConfigCache;
import com.guttv.pm.core.cache.TaskCache;
import com.guttv.pm.core.flow.FlowExecuteConfig;
import com.guttv.pm.core.flow.FlowExecuteConfigBuilder;
import com.guttv.pm.core.flow.FlowExecuteEngine;
import com.guttv.pm.core.fp.FlowExecConfigToZookeeper;
import com.guttv.pm.core.fp.FlowToZookeeper;
import com.guttv.pm.core.task.AbstractTask;
import com.guttv.pm.core.zk.DistributedSequence;
import com.guttv.pm.platform.action.BaseAction;
import com.guttv.pm.platform.action.meta.comparator.FlowExecuteConfigComparator;
import com.guttv.pm.platform.action.meta.search.FlowExecuteConfigFilter;
import com.guttv.pm.utils.Enums.ComponentNodeStatus;
import com.guttv.pm.utils.Enums.ComponentProType;
import com.guttv.pm.utils.Enums.FlowExecuteStatus;
import com.guttv.pm.utils.JsonUtil;
import com.guttv.pm.utils.Pager;

/**
 * @author Peter
 *
 */
@Controller
@RequestMapping("/flowExeConfig")
public class FlowExecuteConfigAction extends BaseAction {

	@RequestMapping("/list")
	public String list(Pager pager, HttpServletRequest request, HttpServletResponse response) {
		List<FlowExecuteConfig> flowExecuteConfigs = FlowExecuteConfigCache.getInstance().getAllFlowExecuteConfigs();

		// 过滤 即查找
		flowExecuteConfigs = new FlowExecuteConfigFilter().filter(flowExecuteConfigs, pager.getSearchBy(),
				pager.getKeyword());

		// 排序
		Collections.sort(flowExecuteConfigs, new FlowExecuteConfigComparator(pager.getOrderBy(), pager.getOrder()));

		pager.setResult(pager(pager,flowExecuteConfigs));
		pager.setTotalCount(flowExecuteConfigs == null ? 0 : flowExecuteConfigs.size());
		return "/flowExeConfig/list";
	}

	/**
	 * 进入查看页面
	 * 
	 * @param flowExeCode
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/view")
	public String view(String flowExeCode, HttpServletRequest request, HttpServletResponse response) {
		if (StringUtils.isNotBlank(flowExeCode)) {
			FlowExecuteConfig flowExeConfig = FlowExecuteConfigCache.getInstance().getFlowExecuteConfig(flowExeCode);
			request.setAttribute("flowExeConfig", flowExeConfig);
		}
		return "/flowExeConfig/view";
	}

	/**
	 * 查看流程图
	 * 
	 * @param flowExeCode
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/viewFlow")
	public String viewFlow(String flowExeCode, HttpServletRequest request, HttpServletResponse response) {

		if (StringUtils.isNotBlank(flowExeCode)) {
			FlowExecuteConfig flowExeConfig = FlowExecuteConfigCache.getInstance().getFlowExecuteConfig(flowExeCode);
			if (flowExeConfig != null) {
				request.setAttribute("flow", flowExeConfig.getFlow());
			} else {
				request.setAttribute("errMsg", "没有找到编码为[" + flowExeCode + "]的流程执行配置数据");
			}

		}

		FlowAction.preparedFlowView(request);

		return "/flow/viewFlow";

	}

	/**
	 * 进入修改流程执行配置信息页面
	 * 
	 * @param flowExeCode
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/input")
	public String input(String flowExeCode, HttpServletRequest request, HttpServletResponse response) {
		view(flowExeCode, request, response);
		return "/flowExeConfig/input";
	}

	/**
	 * 进入查看流程执行任务页面
	 * 
	 * @param flowExeCode
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/tasksList")
	public String tasksList(String flowExeCode, HttpServletRequest request, HttpServletResponse response) {
		if (StringUtils.isNotBlank(flowExeCode)) {
			List<AbstractTask> tasksList = TaskCache.getInstance().getTasksByFlowExeCode(flowExeCode);
			request.setAttribute("tasksList", tasksList);
		}
		return "/flowExeConfig/tasksList";
	}

	@RequestMapping(value = "/rebuild", method = RequestMethod.POST)
	@ResponseBody
	public String rebuild(String flowExeCode, HttpServletRequest request, HttpServletResponse response) {

		Map<String, Object> result = new HashMap<String, Object>();

		if (StringUtils.isBlank(flowExeCode)) {
			result.put("status", "fail");
			result.put("message", "编码为空");
			return JsonUtil.toJson(result);
		}

		FlowExecuteConfig oldFlowExeConfig = FlowExecuteConfigCache.getInstance().getFlowExecuteConfig(flowExeCode);
		if (oldFlowExeConfig == null) {
			result.put("status", "fail");
			result.put("message", "编码为[" + flowExeCode + "]的执行配置信息已经不存在");
			return JsonUtil.toJson(result);
		}

		// 只有完成状态的实例才能删除
		if (oldFlowExeConfig.getStatus() != FlowExecuteStatus.INIT
				&& oldFlowExeConfig.getStatus() != FlowExecuteStatus.FINISH) {
			result.put("status", "fail");
			result.put("message", "只有完成状态的数据才能重新生成");
			return JsonUtil.toJson(result);
		}

		try {
			// 生成新的流程
			FlowExecuteConfig flowExecuteConfig = FlowExecuteConfigBuilder.build(oldFlowExeConfig.getFlowCode());

			// 替换以前的属性值
			flowExecuteConfig.setCreateTime(oldFlowExeConfig.getCreateTime());
			flowExecuteConfig.setStatus(oldFlowExeConfig.getStatus(), "经过重生");
			flowExecuteConfig.setFlowExeCode(flowExeCode);
			flowExecuteConfig.setId(oldFlowExeConfig.getId());

			// 再替换节点属性
			Map<String, List<ComponentFlowProBean>> comFlowProsMap = flowExecuteConfig.getComFlowProsMap();
			if (comFlowProsMap != null && comFlowProsMap.size() > 0) {
				String nodeID = null;
				Iterator<String> iter = comFlowProsMap.keySet().iterator();
				while (iter.hasNext()) {
					nodeID = iter.next();

					// 新执行配置的属性
					List<ComponentFlowProBean> comFlowPros = comFlowProsMap.get(nodeID);
					if (comFlowPros != null && comFlowPros.size() > 0) {

						// 取原属性进行比较
						List<ComponentFlowProBean> oldComFlowPros = oldFlowExeConfig.getComFlowPros(nodeID);

						if (oldComFlowPros != null && oldComFlowPros.size() > 0) {
							for (ComponentFlowProBean comFlowPro : comFlowPros) {
								for (ComponentFlowProBean oldComFlowPro : oldComFlowPros) {
									// 必须节点ID 、 组件class、属性类型、属性名称 都相同才能继承源值
									if (comFlowPro.getType().equals(oldComFlowPro.getType())
											&& comFlowPro.getName().equals(oldComFlowPro.getName())
											&& comFlowPro.getNodeID().equals(oldComFlowPro.getNodeID())
											&& comFlowPro.getComponentClz().equals(oldComFlowPro.getComponentClz())) {
										// 替换为原值
										comFlowPro.setValue(oldComFlowPro.getValue());
										break;
									}
								}

							}
						}
					}
				}
			}

			// 再替换原分发属性
			List<ComponentDispatchBean> comDispatchs = flowExecuteConfig.getComDispatchs();
			if (comDispatchs != null && comDispatchs.size() > 0) {
				List<ComponentDispatchBean> oldComDispatchs = oldFlowExeConfig.getComDispatchs();
				for (ComponentDispatchBean cdb : comDispatchs) {
					for (ComponentDispatchBean oldCdb : oldComDispatchs) {
						// 没有要求必须是同一条线
						if (cdb.getFromNode().equals(oldCdb.getFromNode()) // 起始结点ID
																			// 要一样
								&& cdb.getToNode().equals(oldCdb.getToNode()) // 并且终点ID要一样
						// 起始结点的组件也得一样
								&& ((cdb.getFromComponent() == null && oldCdb.getFromComponent() == null)
										|| (cdb.getFromComponent() != null
												&& cdb.getFromComponent().equals(oldCdb.getFromComponent())))
												// 终点的结点组件也得一样
								&& ((cdb.getToComponent() == null && oldCdb.getToComponent() == null)
										|| (cdb.getToComponent() != null
												&& cdb.getToComponent().equals(oldCdb.getToComponent())))) {
							cdb.setQueue(oldCdb.getQueue());
							cdb.setRule(oldCdb.getRule());
							break;
						}
					}
				}
			}

			// 保存到zookeeper上
			try {
				FlowExecConfigToZookeeper.persistanceToZookeeper(flowExecuteConfig);
			} catch (Exception e) {
				String errMsg = "保存到zookeeper上失败：" + e.getMessage();
				logger.error(errMsg, e);
				result.put("status", "fail");
				result.put("message", errMsg);
				return JsonUtil.toJson(result);
			}

			// 不再此处更新缓存，由监听处理
			// FlowExecuteConfig old =
			// FlowExecuteConfigCache.getInstance().cacheFlowExecuteConfig(flowExecuteConfig);

			// logger.info("生成流程执行配置：" + JsonUtil.toJson(flowExecuteConfig));

			// if (old != null) {
			// logger.info("替换旧流程执行配置：" + JsonUtil.toJson(old));
			// }

			result.put("status", "success");
			result.put("message", "已经发送到ZK，请稍后刷新列表");
			return JsonUtil.toJson(result);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.put("status", "fail");
			result.put("message", e.getMessage());
			return JsonUtil.toJson(result);
		}
	}

	/**
	 * 还原 原流程图
	 * 
	 * @param flowExeCode
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/recovery", method = RequestMethod.POST)
	@ResponseBody
	public String recovery(String flowExeCode, HttpServletRequest request, HttpServletResponse response) {

		Map<String, Object> result = new HashMap<String, Object>();

		if (StringUtils.isBlank(flowExeCode)) {
			result.put("status", "fail");
			result.put("message", "编码为空");
			return JsonUtil.toJson(result);
		}

		FlowExecuteConfig flowExeConfig = FlowExecuteConfigCache.getInstance().getFlowExecuteConfig(flowExeCode);
		if (flowExeConfig == null) {
			result.put("status", "fail");
			result.put("message", "编码为[" + flowExeCode + "]的执行配置信息已经不存在");
			return JsonUtil.toJson(result);
		}

		FlowBean old = FlowCache.getInstance().getFlow(flowExeConfig.getFlowCode());
		if (old != null) {
			result.put("status", "fail");
			result.put("message", "编码为[" + flowExeConfig.getFlowCode() + "]的流程图仍然存在");
			return JsonUtil.toJson(result);
		}

		try {
			FlowBean flow = flowExeConfig.getFlow();
			// 验证是否遭到破坏 没有异常即表示可用
			FlowExecuteConfigBuilder.build(flow.getCode(), flow.getFlowContent(), flow.getFlowComPros(),
					flow.getNodeVSCom());

			flow.setRemark("由流程执行配置[" + flowExeConfig.getFlowExeCode() + "]恢复生成");
			flow.setUpdateTime(new Date());
			// 保存到zookeeper上
			try {
				if (flow.getId() == null || flow.getId() < 0) {
					flow.setId(DistributedSequence.getInstance().getNext());
				}

				FlowToZookeeper.persistanceToZookeeper(flow);
			} catch (Exception e) {
				String errMsg = "保存到zookeeper上失败：" + e.getMessage();
				logger.error(errMsg, e);
				result.put("status", "fail");
				result.put("message", errMsg);
				return JsonUtil.toJson(result);
			}

			// 保存到本地内存
			// 不再此处更新缓存，由监听处理
			// FlowCache.getInstance().cacheFlow(flow);

			result.put("status", "success");
			result.put("message", "已经上传ZK，请稍后刷新列表");
			return JsonUtil.toJson(result);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.put("status", "fail");
			result.put("message", e.getMessage());
			return JsonUtil.toJson(result);
		}

	}

	/**
	 * 删除执行配置
	 * 
	 * @param flow
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	@ResponseBody
	public String delete(String flowExeCode, HttpServletRequest request, HttpServletResponse response) {

		Map<String, Object> result = new HashMap<String, Object>();
		try {

			if (StringUtils.isBlank(flowExeCode)) {
				result.put("status", "fail");
				result.put("message", "编码为空");
				return JsonUtil.toJson(result);
			}
			FlowExecuteConfig flowExeConfig = FlowExecuteConfigCache.getInstance().getFlowExecuteConfig(flowExeCode);
			// 只有完成状态的实例才能删除
			if (flowExeConfig == null || flowExeConfig.getStatus() != FlowExecuteStatus.INIT
					&& flowExeConfig.getStatus() != FlowExecuteStatus.FINISH
					&& flowExeConfig.getStatus() != FlowExecuteStatus.ERROR) {
				result.put("status", "fail");
				result.put("message", "只有完成状态的数据才能被删除");
				return JsonUtil.toJson(result);
			}

			// 先删除zookeeper服务器上的
			FlowExecConfigToZookeeper.deleteFromZookeeper(flowExeCode);

			// 再清理本地缓存
			// 不在此删除，由监听处理
			// flowExeConfig =
			// FlowExecuteConfigCache.getInstance().uncacheFlowExecuteConfig(flowExeCode);
			// TaskCache.getIntence().stopTasksByFlowExeCode(flowExeCode);
			// logger.info("删除流程执行配置：" + JsonUtil.toJson(flowExeConfig));

			result.put("status", "success");
			result.put("message", "已经从ZK上删除，请稍后刷新列表页面");
			return JsonUtil.toJson(result);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.put("status", "fail");
			result.put("message", e.getMessage());
			return JsonUtil.toJson(result);
		}
	}

	/**
	 * 只更新流程执行配置的状态
	 * 
	 * @param flow
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/updateFlowExecName", method = RequestMethod.POST)
	@ResponseBody
	public String updateFlowExecName(String flowExeCode, String name, HttpServletRequest request,
			HttpServletResponse response) {

		Map<String, Object> result = new HashMap<String, Object>();

		try {
			if (StringUtils.isBlank(flowExeCode) || StringUtils.isBlank(name)) {
				result.put("status", "fail");
				result.put("message", "流程编码或者名字不能为空");
				return JsonUtil.toJson(result);
			}

			FlowExecuteConfig config = FlowExecuteConfigCache.getInstance().getFlowExecuteConfig(flowExeCode);
			if (config == null) {
				result.put("status", "fail");
				result.put("message", "不存在执行编码为[" + flowExeCode + "]的执行配置");
				return JsonUtil.toJson(result);
			}

			// 这个对象不是clone的，可以直接修改
			config.getFlow().setName(name);
			config.setUpdateTime(new Date());

			// 保存到zookeeper上
			try {
				FlowExecConfigToZookeeper.persistanceToZookeeper(config);
			} catch (Exception e) {
				logger.error("本地数据已经修改,保存到zookeeper上失败：" + e.getMessage(), e);
				result.put("status", "fail");
				result.put("message", "本地数据已经修改,保存到zookeeper上失败：" + e.getMessage());
				return JsonUtil.toJson(result);
			}

			result.put("status", "success");
			result.put("message", "已经发送到ZK，请稍后刷新列表页面");
			return JsonUtil.toJson(result);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.put("status", "fail");
			result.put("message", "失败：" + e.getMessage());
			return JsonUtil.toJson(result);
		}

	}

	/**
	 * 只更新流程执行配置的状态
	 * 
	 * @param flow
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/updateStatus", method = RequestMethod.POST)
	@ResponseBody
	public String updateStatus(String flowExeCode, int status, HttpServletRequest request,
			HttpServletResponse response) {

		Map<String, Object> result = new HashMap<String, Object>();
		// 记录原状态
		FlowExecuteStatus oldStatus = null;
		try {

			FlowExecuteStatus enumStatus = FlowExecuteStatus.valueOf(status);
			if (enumStatus == null) {
				result.put("status", "fail");
				result.put("message", "不存在值为[" + status + "]的状态");
				return JsonUtil.toJson(result);
			}

			FlowExecuteConfig config = FlowExecuteConfigCache.getInstance().getFlowExecuteConfig(flowExeCode);
			if (config == null) {
				result.put("status", "fail");
				result.put("message", "不存在执行编码为[" + flowExeCode + "]的执行配置");
				return JsonUtil.toJson(result);
			}

			if (config.getStatus().getValue() == status) {
				result.put("status", "fail");
				result.put("message", "与原状态相同，请刷新确认");
				return JsonUtil.toJson(result);
			}

			oldStatus = config.getStatus();

			// 恢复初始化状态
			if (status == FlowExecuteStatus.INIT.getValue()) {
				// 目前只支持下面这三种进行状态恢复 STARTING状态的恢复一定要小心
				if (oldStatus != FlowExecuteStatus.ERROR && oldStatus != FlowExecuteStatus.STARTING
						&& oldStatus != FlowExecuteStatus.LOCKED) {
					result.put("status", "fail");
					result.put("message", "不支持[" + oldStatus.getName() + "]状态下的流程进行状态恢复");
					return JsonUtil.toJson(result);
				}

				// 恢复前做下任务删除
				TaskCache.getInstance().stopTasksByFlowExeCode(flowExeCode); // 以防万一

				// 状态恢复
				FlowExecuteConfigCache.getInstance().updateStatus(flowExeCode, FlowExecuteStatus.INIT.getValue(),
						"页面指令[" + FlowExecuteStatus.valueOf(status) + "]");

			} else if (status == FlowExecuteStatus.STARTING.getValue()) {// 启动
				// 由暂停启动 由初始化启动 由完成重新启动

				// 目前只支持下面这三种进行状态启动
				if (oldStatus != FlowExecuteStatus.PAUSE && oldStatus != FlowExecuteStatus.INIT
						&& oldStatus != FlowExecuteStatus.FINISH) {
					result.put("status", "fail");
					result.put("message", "不支持[" + oldStatus.getName() + "]状态下的流程启动");
					return JsonUtil.toJson(result);
				}

				// 如果以前只是暂停，启动一下即可
				if (oldStatus == FlowExecuteStatus.PAUSE) {
					boolean pause = TaskCache.getInstance().setPauseStatusByFlowExeCode(flowExeCode, false);
					if (pause) {
						// 暂停
						FlowExecuteConfigCache.getInstance().updateStatus(flowExeCode,
								FlowExecuteStatus.RUNNING.getValue(),
								"页面指令[" + FlowExecuteStatus.valueOf(status) + "]修改成功");
					} else {
						result.put("status", "fail");
						result.put("message", "流程中没有周期的任务，或者其它原因");
						return JsonUtil.toJson(result);
					}
				} else {
					// 锁定
					FlowExecuteConfigCache.getInstance().updateStatus(flowExeCode, FlowExecuteStatus.LOCKED.getValue(),
							"页面指令[" + FlowExecuteStatus.valueOf(status) + "]，锁定流程配置");

					// 下面这是启动
					TaskCache.getInstance().stopTasksByFlowExeCode(flowExeCode);

					// 启动
					FlowExecuteEngine.excuteFlowExecuteConfig(config);
				}

			} else if (status == FlowExecuteStatus.PAUSE.getValue()) { // 要求暂停的

				// 目前只支持执行状态的流程进行暂停启动
				if (oldStatus != FlowExecuteStatus.RUNNING) {
					result.put("status", "fail");
					result.put("message", "不支持[" + oldStatus.getName() + "]状态下的流程暂停");
					return JsonUtil.toJson(result);
				}

				// 重启
				boolean pause = TaskCache.getInstance().setPauseStatusByFlowExeCode(flowExeCode, true);
				if (pause) {
					// 修改状态
					FlowExecuteConfigCache.getInstance().updateStatus(flowExeCode, FlowExecuteStatus.PAUSE.getValue(),
							"页面指令[" + FlowExecuteStatus.valueOf(status) + "]修改成功");
				} else {
					result.put("status", "fail");
					result.put("message", "流程中没有周期的任务，或者其它原因");
					return JsonUtil.toJson(result);
				}

			} else if (status == FlowExecuteStatus.STOPPED.getValue()) { // 要求停止
				// 目前只支持执行状态的流程进行暂停启动
				if (oldStatus != FlowExecuteStatus.RUNNING && oldStatus != FlowExecuteStatus.PAUSE) {
					result.put("status", "fail");
					result.put("message", "不支持[" + oldStatus.getName() + "]状态下的流程停止");
					return JsonUtil.toJson(result);
				}

				FlowExecuteEngine.stopFlowExecuteConfig(config);
			} else {
				logger.warn("不支持的状态值[" + status + "]");

				FlowExecuteConfigCache.getInstance().updateStatus(flowExeCode, oldStatus.getValue(),
						"不支持的状态值[" + status + "],恢复原状态");

				result.put("status", "fail");
				result.put("message", "不支持的状态值[" + status + "]");
				return JsonUtil.toJson(result);
			}

			// 保存到zookeeper上
			try {
				FlowExecConfigToZookeeper
						.persistanceToZookeeper(FlowExecuteConfigCache.getInstance().getFlowExecuteConfig(flowExeCode));
			} catch (Exception e) {
				logger.error("本地状态已经修改,保存到zookeeper上失败：" + e.getMessage(), e);
				result.put("status", "fail");
				result.put("message", "本地状态已经修改,保存到zookeeper上失败：" + e.getMessage());
				return JsonUtil.toJson(result);
			}

			config.setUpdateTime(new Date());

			result.put("status", "success");
			result.put("message", "修改成功");
			return JsonUtil.toJson(result);
		} catch (Exception e) {

			logger.error(e.getMessage(), e);
			result.put("status", "fail");
			result.put("message", e.getMessage());

			// 异常恢复原状态
			if (oldStatus != null) {
				try {
					FlowExecuteConfigCache.getInstance().updateStatus(flowExeCode, oldStatus.getValue(),
							"更新状态异常：" + e.getMessage());
				} catch (Exception e1) {
					logger.error(e1.getMessage(), e1);
				}
			}
			return JsonUtil.toJson(result);
		}
	}

	/**
	 * 只更新流程执行配置中某一个任务的状态
	 * 
	 * @param flow
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/updateNodeStatus", method = RequestMethod.POST)
	@ResponseBody
	public String updateNodeStatus(String flowExeCode, String nodeID, int status, HttpServletRequest request,
			HttpServletResponse response) {

		Map<String, Object> result = new HashMap<String, Object>();
		// 记录原状态
		ComponentNodeStatus oldStatus = null;
		try {

			ComponentNodeStatus enumStatus = ComponentNodeStatus.valueOf(status);
			if (enumStatus == null) {
				result.put("status", "fail");
				result.put("message", "不存在值为[" + status + "]的状态");
				return JsonUtil.toJson(result);
			}

			FlowExecuteConfig config = FlowExecuteConfigCache.getInstance().getFlowExecuteConfig(flowExeCode);
			if (config == null) {
				result.put("status", "fail");
				result.put("message", "不存在执行编码为[" + flowExeCode + "]的执行配置");
				return JsonUtil.toJson(result);
			}

			ComponentNodeBean comNode = config.getComponentNode(nodeID);
			if (comNode == null) {
				result.put("status", "fail");
				result.put("message", "流程配置中不存在结点ID为[" + nodeID + "]的结点");
				return JsonUtil.toJson(result);
			}

			if (comNode.getStatus().getValue() == status) {
				result.put("status", "fail");
				result.put("message", "与原状态相同，请刷新确认");
				return JsonUtil.toJson(result);
			}

			oldStatus = comNode.getStatus();

			if (status == ComponentNodeStatus.STARTING.getValue()) {// 启动
				// 由暂停启动 由初始化启动 由完成重新启动

				// 目前只支持下面几种状态的启动
				if (oldStatus != ComponentNodeStatus.PAUSE) {
					result.put("status", "fail");
					result.put("message", "不支持[" + oldStatus.getName() + "]状态下的流程启动");
					return JsonUtil.toJson(result);
				}

				// 如果以前只是暂停，启动一下即可
				boolean pause = TaskCache.getInstance().setPauseStatusByFlowExeCode(flowExeCode, nodeID, false);
				if (pause) {
					// 暂停
					comNode.setStatus(ComponentNodeStatus.RUNNING);
				} else {
					result.put("status", "fail");
					result.put("message", "流程中没有周期的任务，或者其它原因");
					return JsonUtil.toJson(result);
				}

			} else if (status == ComponentNodeStatus.PAUSE.getValue()) { // 要求暂停的

				// 目前只支持执行状态的流程进行暂停启动
				if (oldStatus != ComponentNodeStatus.RUNNING) {
					result.put("status", "fail");
					result.put("message", "不支持[" + oldStatus.getName() + "]状态下的流程暂停");
					return JsonUtil.toJson(result);
				}

				// 重启
				boolean pause = TaskCache.getInstance().setPauseStatusByFlowExeCode(flowExeCode, nodeID, true);
				if (pause) {
					// 修改状态
					comNode.setStatus(ComponentNodeStatus.PAUSE);
				} else {
					result.put("status", "fail");
					result.put("message", "流程中没有周期的任务，或者其它原因");
					return JsonUtil.toJson(result);
				}

			} else if (status == ComponentNodeStatus.STOPPED.getValue()) { // 要求停止
				// 目前只支持执行状态的流程进行暂停启动
				if (oldStatus != ComponentNodeStatus.RUNNING && oldStatus != ComponentNodeStatus.PAUSE) {
					result.put("status", "fail");
					result.put("message", "不支持[" + oldStatus.getName() + "]状态下的流程停止");
					return JsonUtil.toJson(result);
				}

				TaskCache.getInstance().stopTasksByFlowExeCode(flowExeCode, nodeID);
			} else if (status == ComponentNodeStatus.INIT.getValue()) { // 要求
																		// 初始化
				// 目前只支持禁用状态的流程进行初始化
				if (oldStatus != ComponentNodeStatus.FORBIDDEN) {
					result.put("status", "fail");
					result.put("message", "不支持[" + oldStatus.getName() + "]状态下的流程初始化");
					return JsonUtil.toJson(result);
				}

				comNode.setStatus(ComponentNodeStatus.INIT);
			} else if (status == ComponentNodeStatus.FORBIDDEN.getValue()) { // 要求禁用
				// 目前只支持执行状态的流程进行暂停启动
				if (oldStatus != ComponentNodeStatus.INIT && oldStatus != ComponentNodeStatus.FINISH) {
					result.put("status", "fail");
					result.put("message", "不支持[" + oldStatus.getName() + "]状态下的流程禁用");
					return JsonUtil.toJson(result);
				}

				comNode.setStatus(ComponentNodeStatus.FORBIDDEN);
			} else {
				logger.warn("不支持的状态值[" + status + "]");

				result.put("status", "fail");
				result.put("message", "不支持的状态值[" + status + "]");
				return JsonUtil.toJson(result);
			}

			comNode.setUpdateTime(new Date());

			// 保存到zookeeper上
			try {
				FlowExecConfigToZookeeper
						.persistanceToZookeeper(FlowExecuteConfigCache.getInstance().getFlowExecuteConfig(flowExeCode));
			} catch (Exception e) {
				logger.error("本地状态已经修改,保存到zookeeper上失败：" + e.getMessage(), e);
				result.put("status", "fail");
				result.put("message", "本地状态已经修改,保存到zookeeper上失败：" + e.getMessage());
				return JsonUtil.toJson(result);
			}

			result.put("status", "success");
			result.put("message", "已经上传到ZK，请稍后刷新列表");
			return JsonUtil.toJson(result);
		} catch (Exception e) {

			logger.error(e.getMessage(), e);
			result.put("status", "fail");
			result.put("message", e.getMessage());

			return JsonUtil.toJson(result);
		}
	}

	/**
	 * 
	 * @param flowExeCode
	 * @param nodeID
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/beforeUpdateAllProperties", method = RequestMethod.GET)
	public String beforeUpdateAllProperties(String flowExeCode, HttpServletRequest request,
			HttpServletResponse response) {

		request.setAttribute("flowExeCode", flowExeCode);

		// 找流程配置数据
		FlowExecuteConfig config = FlowExecuteConfigCache.getInstance().getFlowExecuteConfig(flowExeCode);
		if (config == null) {
			request.setAttribute("errMsg", "没有找到编码为[" + flowExeCode + "]的流程执行配置数据");
			return "/flowExeConfig/updateAllProperties";
		}

		// 存放所有的可编辑的属性
		List<ComponentFlowProBean> comFlowPros = new ArrayList<ComponentFlowProBean>();
		// 遍历所有的结点，找出所有的可编辑属性
		Map<String, List<ComponentFlowProBean>> flowProsMap = config.getComFlowProsMap();
		if (flowProsMap != null && flowProsMap.size() > 0) {
			Iterator<String> iter = flowProsMap.keySet().iterator();
			String id = null;
			List<ComponentFlowProBean> flowPros = null;
			while (iter.hasNext()) {
				id = iter.next();
				flowPros = flowProsMap.get(id);
				if (flowPros != null && flowPros.size() > 0) {
					for (ComponentFlowProBean flowPro : flowPros) {
						// 只添加普通属性
						if (flowPro.getType() == ComponentProType.NOR) {
							comFlowPros.add(flowPro);
						}
					}
				}
			}
		}

		request.setAttribute("comFlowPros", comFlowPros);
		if (comFlowPros.size() == 0) {
			request.setAttribute("errMsg", "没有任何可编辑属性");
		}

		return "/flowExeConfig/updateAllProperties";
	}

	/**
	 * 修改属性
	 * 
	 * @param flowExeCode
	 * @param nodeID
	 * @param clz
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/updateAllProperties", method = RequestMethod.POST)
	public String updateAllProperties(String flowExeCode, HttpServletRequest request, HttpServletResponse response) {

		request.setAttribute("flowExeCode", flowExeCode);

		// 找流程配置数据
		FlowExecuteConfig config = FlowExecuteConfigCache.getInstance().getFlowExecuteConfig(flowExeCode);
		if (config == null) {
			request.setAttribute("errMsg", "没有找到编码为[" + flowExeCode + "]的流程执行配置数据");
			return "/flowExeConfig/updateAllProperties";
		}

		request.setAttribute("flowExeConfig", config);

		if (config.getStatus() != FlowExecuteStatus.INIT && config.getStatus() != FlowExecuteStatus.FINISH) {
			request.setAttribute("errMsg", "只有初始化或者完成状态的流程可以修改属性");
			return "/flowExeConfig/view";
		}

		boolean changed = false;
		// 遍历所有的结点，找出所有的可编辑属性
		Map<String, List<ComponentFlowProBean>> flowProsMap = config.getComFlowProsMap();
		if (flowProsMap != null && flowProsMap.size() > 0) {
			Iterator<String> iter = flowProsMap.keySet().iterator();
			String id = null;
			List<ComponentFlowProBean> flowPros = null;
			while (iter.hasNext()) {
				id = iter.next();
				flowPros = flowProsMap.get(id);
				if (flowPros != null && flowPros.size() > 0) {
					for (ComponentFlowProBean flowPro : flowPros) {
						// 只添加普通属性
						if (flowPro.getType() == ComponentProType.NOR) {
							// 参数的拼写规则得与页面一致
							String value = request.getParameter(
									flowPro.getNodeID() + "_" + flowPro.getType() + "_" + flowPro.getName());
							if (value != null && !value.trim().equals(flowPro.getValue())) {
								flowPro.setValue(value.trim());
								flowPro.setUpdateTime(new Date());
								changed = true;
							}
						}
					}
				}
			}
		}

		if (changed) {
			// 保存到zookeeper上
			try {
				FlowExecConfigToZookeeper
						.persistanceToZookeeper(FlowExecuteConfigCache.getInstance().getFlowExecuteConfig(flowExeCode));

				request.setAttribute("message", "保存属性成功");
			} catch (Exception e) {
				String errMsg = "本地属性值已经修改,保存到zookeeper上失败：" + e.getMessage();
				logger.error(errMsg, e);
				request.setAttribute("errMsg", errMsg);
			}
		}

		return "/flowExeConfig/view";

	}

	/**
	 * 
	 * @param flowExeCode
	 * @param nodeID
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/beforeUpdateProperties", method = RequestMethod.GET)
	public String beforeUpdateProperties(String flowExeCode, String nodeID, String clz, HttpServletRequest request,
			HttpServletResponse response) {

		request.setAttribute("flowExeCode", flowExeCode);
		request.setAttribute("nodeID", nodeID);

		// 找流程配置数据
		FlowExecuteConfig config = FlowExecuteConfigCache.getInstance().getFlowExecuteConfig(flowExeCode);
		if (config == null) {
			request.setAttribute("errMsg", "没有找到编码为[" + flowExeCode + "]的流程执行配置数据");
			return "/flowExeConfig/updateProperties";
		}

		// 找流程中的节点信息
		ComponentNodeBean comNode = config.getComponentNode(nodeID);
		if (comNode == null) {
			request.setAttribute("errMsg", "流程执行配置中没有找到节点[" + nodeID + "]数据");
			return "/flowExeConfig/updateProperties";
		}

		if (StringUtils.isNotBlank(clz) && !clz.equals(comNode.getComponentClz())) {
			request.setAttribute("errMsg", "流程执行配置中节点[" + nodeID + "]的类不是：" + clz);
			return "/flowExeConfig/updateProperties";
		}

		// 找对应的组件信息
		ComponentBean com = ComponentCache.getInstance().getComponent(comNode.getComponentClz());
		if (com == null) {
			request.setAttribute("errMsg", "没有找到类名为[" + comNode.getComponentClz() + "]的组件");
			return "/flowExeConfig/updateProperties";
		}
		request.setAttribute("com", com);

		// 找组件属性
		List<ComponentFlowProBean> comFlowPros = config.getComFlowPros(comNode.getNodeID());
		request.setAttribute("comFlowPros", comFlowPros);
		if (comFlowPros == null || comFlowPros.size() == 0) {
			request.setAttribute("errMsg", "没有任务属性信息");
		}

		return "/flowExeConfig/updateProperties";
	}

	/**
	 * 修改属性
	 * 
	 * @param flowExeCode
	 * @param nodeID
	 * @param clz
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/updateProperties", method = RequestMethod.POST)
	public String updateProperties(String flowExeCode, String nodeID, String clz, HttpServletRequest request,
			HttpServletResponse response) {

		request.setAttribute("flowExeCode", flowExeCode);
		request.setAttribute("nodeID", nodeID);

		// 找流程配置数据
		FlowExecuteConfig config = FlowExecuteConfigCache.getInstance().getFlowExecuteConfig(flowExeCode);
		if (config == null) {
			request.setAttribute("errMsg", "没有找到编码为[" + flowExeCode + "]的流程执行配置数据");
			return "/flowExeConfig/updateProperties";
		}

		request.setAttribute("flowExeConfig", config);

		if (config.getStatus() != FlowExecuteStatus.INIT && config.getStatus() != FlowExecuteStatus.FINISH) {
			request.setAttribute("errMsg", "只有初始化或者完成状态的流程可以修改属性");
			return "/flowExeConfig/view";
		}

		// 找流程中的节点信息
		ComponentNodeBean comNode = config.getComponentNode(nodeID);
		if (comNode == null) {
			request.setAttribute("errMsg", "流程执行配置中没有找到节点[" + nodeID + "]数据");
			return "/flowExeConfig/updateProperties";
		}

		if (StringUtils.isNotBlank(clz) && !clz.equals(comNode.getComponentClz())) {
			request.setAttribute("errMsg", "流程执行配置中节点[" + nodeID + "]的类不是：" + clz);
			return "/flowExeConfig/updateProperties";
		}

		// 找对应的组件信息
		ComponentBean com = ComponentCache.getInstance().getComponent(comNode.getComponentClz());
		if (com == null) {
			request.setAttribute("errMsg", "没有找到类名为[" + comNode.getComponentClz() + "]的组件");
			return "/flowExeConfig/updateProperties";
		}

		// 找组件属性
		List<ComponentFlowProBean> comFlowPros = config.getComFlowPros(comNode.getNodeID());
		if (comFlowPros != null && comFlowPros.size() > 0) {
			boolean changed = false;
			for (ComponentFlowProBean cfp : comFlowPros) {
				// 参数的拼写规则得与页面一致
				String value = request.getParameter(cfp.getType() + "_" + cfp.getName());
				if (value != null && !value.trim().equals(cfp.getValue())) {
					cfp.setValue(value.trim());
					cfp.setUpdateTime(new Date());
					changed = true;
				}
			}

			if (changed) {
				// 保存到zookeeper上
				try {
					FlowExecConfigToZookeeper.persistanceToZookeeper(
							FlowExecuteConfigCache.getInstance().getFlowExecuteConfig(flowExeCode));

					request.setAttribute("message", "保存属性成功");
				} catch (Exception e) {
					String errMsg = "本地属性值已经修改,保存到zookeeper上失败：" + e.getMessage();
					logger.error(errMsg, e);
					request.setAttribute("errMsg", errMsg);
				}
			}
		}

		return "/flowExeConfig/view";

	}

	/**
	 * 
	 * @param flowExeCode
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/beforeUpdateDispatch", method = RequestMethod.GET)
	public String beforeUpdateDispatch(String flowExeCode, HttpServletRequest request, HttpServletResponse response) {

		// 找流程配置数据
		FlowExecuteConfig config = FlowExecuteConfigCache.getInstance().getFlowExecuteConfig(flowExeCode);
		if (config == null) {
			request.setAttribute("errMsg", "没有找到编码为[" + flowExeCode + "]的流程执行配置数据");
			return "/flowExeConfig/updateDispatch";
		}

		request.setAttribute("flowExeConfig", config);

		return "/flowExeConfig/updateDispatch";
	}

	@RequestMapping(value = "/updateDispatch", method = RequestMethod.POST)
	public String updateDispatch(String flowExeCode, HttpServletRequest request, HttpServletResponse response) {

		// 找流程配置数据
		FlowExecuteConfig config = FlowExecuteConfigCache.getInstance().getFlowExecuteConfig(flowExeCode);
		if (config == null) {
			request.setAttribute("errMsg", "没有找到编码为[" + flowExeCode + "]的流程执行配置数据");
			return "/flowExeConfig/updateDispatch";
		}

		request.setAttribute("flowExeConfig", config);

		if (config.getStatus() != FlowExecuteStatus.INIT && config.getStatus() != FlowExecuteStatus.FINISH) {
			request.setAttribute("errMsg", "只有初始化或者完成状态的流程可以修改通道信息");
			return "/flowExeConfig/view";
		}

		List<ComponentDispatchBean> comDispatchs = config.getComDispatchs();
		if (comDispatchs == null || comDispatchs.size() == 0) {
			request.setAttribute("message", "本流程没有任何通道");
		} else {
			//
			String value = null;
			for (ComponentDispatchBean cdb : comDispatchs) {
				value = request.getParameter("queue" + cdb.getLineID());
				boolean changed = false;
				if (value != null) {
					cdb.setQueue(value.trim());
					changed = true;
				}
				value = request.getParameter("rule" + cdb.getLineID());
				if (value != null) {
					cdb.setRule(value.trim());
					changed = true;
				}
				if (changed) {
					cdb.setUpdateTime(new Date());
				}
			}

			// 保存到zookeeper上
			try {
				FlowExecConfigToZookeeper
						.persistanceToZookeeper(FlowExecuteConfigCache.getInstance().getFlowExecuteConfig(flowExeCode));

				request.setAttribute("message", "修改队列信息成功");
			} catch (Exception e) {
				String errMsg = "本地队列信息成功,保存到zookeeper上失败：" + e.getMessage();
				logger.error(errMsg, e);
				request.setAttribute("errMsg", errMsg);
			}
		}

		return "/flowExeConfig/view";

	}
}
