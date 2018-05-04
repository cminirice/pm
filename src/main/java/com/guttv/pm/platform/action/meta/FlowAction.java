/**
 * 
 */
package com.guttv.pm.platform.action.meta;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.guttv.pm.core.bean.ComponentBean;
import com.guttv.pm.core.bean.ComponentDispatchBean;
import com.guttv.pm.core.bean.ComponentNodeBean;
import com.guttv.pm.core.bean.ComponentProBean;
import com.guttv.pm.core.bean.FlowBean;
import com.guttv.pm.core.cache.ComponentCache;
import com.guttv.pm.core.cache.FlowCache;
import com.guttv.pm.core.flow.FlowExecuteConfig;
import com.guttv.pm.core.flow.FlowExecuteConfigBuilder;
import com.guttv.pm.core.fp.FlowExecConfigToZookeeper;
import com.guttv.pm.core.fp.FlowToZookeeper;
import com.guttv.pm.core.zk.DistributedSequence;
import com.guttv.pm.platform.action.BaseAction;
import com.guttv.pm.platform.action.meta.comparator.FlowComparator;
import com.guttv.pm.platform.action.meta.search.FlowFilter;
import com.guttv.pm.utils.Enums.ComponentProType;
import com.guttv.pm.utils.Enums.ComponentStatus;
import com.guttv.pm.utils.Enums.FlowStatus;
import com.guttv.pm.utils.JsonUtil;
import com.guttv.pm.utils.Pager;

/**
 * @author Peter
 *
 */
@Controller
@RequestMapping("/flow")
public class FlowAction extends BaseAction {

	@RequestMapping("/list")
	public String list(Pager pager, HttpServletRequest request, HttpServletResponse response) {
		List<FlowBean> flows = FlowCache.getInstance().getAllFlows();

		// 过滤 即查找
		flows = new FlowFilter().filter(flows, pager.getSearchBy(), pager.getKeyword());

		// 排序
		Collections.sort(flows, new FlowComparator(pager.getOrderBy(), pager.getOrder()));

		pager.setResult(pager(pager,flows));
		pager.setTotalCount(flows == null ? 0 : flows.size());
		return "/flow/list";
	}

	/**
	 * 进入查看页面
	 * 
	 * @param code
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/view")
	public String view(String code, HttpServletRequest request, HttpServletResponse response) {
		if (StringUtils.isNotBlank(code)) {
			FlowBean flow = FlowCache.getInstance().getFlow(code);
			request.setAttribute("flow", flow);
		}
		return "/flow/view";
	}

	/**
	 * 更新流程的基本信息 该方法只做更新
	 * 
	 * @param flow
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	@RequestMapping("/saveOrUpdate")
	public String saveOrUpdate(FlowBean flow, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		if (StringUtils.isNotBlank(flow.getCode())) {
			FlowBean old = FlowCache.getInstance().getFlow(flow.getCode());
			if (old != null) {
				old.setName(flow.getName());
				old.setStatusDesc(flow.getStatusDesc());
				old.setRemark(flow.getRemark());

				try {
					// 保存到zookeeper
					FlowToZookeeper.persistanceToZookeeper(old);
				} catch (Exception e) {
					logger.error("同步到zookeeper异常：" + e.getMessage(), e);
					request.setAttribute("errMsg", "同步到zookeeper异常：" + e.getMessage());
					request.setAttribute("flow", flow);
					return "/flow/input";
				}

				// 再添加到缓存
				// 不再更新，由监控更新
				// FlowCache.getInstance().cacheFlow(old);
			}
		}

		request.getRequestDispatcher("/flow/list").forward(request, response);
		return null;
	}

	/**
	 * 进入修改流程基本信息页面
	 * 
	 * @param code
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/input")
	public String input(String code, HttpServletRequest request, HttpServletResponse response) {
		view(code, request, response);
		return "/flow/input";
	}

	/**
	 * 进入制作流程页面
	 * 
	 * @param code
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/buildFlow")
	public String buildFlow(String code, HttpServletRequest request, HttpServletResponse response) {
		if (StringUtils.isNotBlank(code)) {
			request.setAttribute("flow", FlowCache.getInstance().getFlow(code));
		}

		FlowAction.preparedFlowView(request);

		return "/flow/buildFlow";
	}

	/**
	 * 
	 * @param code
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/viewFlow")
	public String viewFlow(String code, HttpServletRequest request, HttpServletResponse response) {
		buildFlow(code, request, response);
		return "/flow/viewFlow";
	}

	public static void preparedFlowView(HttpServletRequest request) {
		List<ComponentBean> components = ComponentCache.getInstance().getAllComponents();
		List<ComponentBean> coms = new ArrayList<ComponentBean>();
		Set<String> groups = new HashSet<String>();
		Map<String, List<ComponentProBean>> comProsMap = new HashMap<String, List<ComponentProBean>>();

		if (components != null && components.size() > 0) {
			List<ComponentProBean> comPros = null;
			for (ComponentBean cb : components) {
				if (cb.getStatus() != null && cb.getStatus().equals(ComponentStatus.NORMAL.getValue())) {

					// 对应组件的选择框
					coms.add(cb);
					groups.add(cb.getGroup());

					// 对应属性，只添加可编辑的属性
					comPros = new ArrayList<ComponentProBean>();
					if (cb.getComponentPros() != null && cb.getComponentPros().size() > 0) {
						for (ComponentProBean cpb : cb.getComponentPros()) {
							if (cpb.getType() == ComponentProType.NOR) {
								comPros.add(cpb);
							}
						}
					}

					comProsMap.put(cb.getClz(), comPros);

					cb.setComponentPros(null); // 该对象为clone体，为了减少向页面传递数据，此处更新为空
					cb.setDispatchs(null);
				}
			}
		}
		request.setAttribute("groups", groups);
		request.setAttribute("coms", coms);
		request.setAttribute("comProsMap", JsonUtil.toJson(comProsMap));
	}

	/**
	 * 保存制作的流程
	 * 
	 * @param flow
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	@RequestMapping("/addFlow")
	public String addFlow(FlowBean flow, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		//将前台表单提交的这个对象转换编码格式
		String flowString = JsonUtil.toJson(flow);
		flow = JsonUtil.fromJson(flowString, FlowBean.class);
		try {
			logger.info("下面是验证流程的有效性......");
			FlowExecuteConfigBuilder.build(flow.getCode(), flow.getFlowContent(), flow.getFlowComPros(),
					flow.getNodeVSCom());
		} catch (Exception e) {
			logger.warn(e.getMessage() + ";流程内容:" + flow.getFlowContent());
			request.setAttribute("flow", flow);
			request.setAttribute("errMsg", e.getMessage());
			buildFlow(null, request, response);
			return "/flow/buildFlow";
		}

		if (StringUtils.isBlank(flow.getCode())) {
			flow.setCode(RandomStringUtils.randomNumeric(16));
			try {
				flow.setId(DistributedSequence.getInstance().getNext());
			} catch (Exception e) {
				logger.error("获取分布式ID异常：" + e.getMessage(), e);
			}
			logger.info("添加新流程：" + JsonUtil.toJson(flow));

		} else {
			FlowBean old = FlowCache.getInstance().getFlow(flow.getCode());
			old.setName(flow.getName());
			old.setFlowContent(flow.getFlowContent());
			old.setFlowComPros(flow.getFlowComPros());
			old.setNodeVSCom(flow.getNodeVSCom());
			logger.info("替换流程，新流程：" + JsonUtil.toJson(old));
			old.setUpdateTime(new Date());
			flow = old;
			try {
				if (flow.getId() == null || flow.getId() < 0) {
					flow.setId(DistributedSequence.getInstance().getNext());
				}
			} catch (Exception e) {
				logger.error("获取ID时异常：" + e.getMessage(), e);
			}
		}

		// 先到zookeeper
		try {
			// 保存到zookeeper
			FlowToZookeeper.persistanceToZookeeper(flow);

		} catch (Exception e) {
			logger.error("保存到zookeeper上异常，流程内容:" + flow.getFlowContent(), e);
			request.setAttribute("flow", flow);
			request.setAttribute("errMsg", "保存到zookeeper上异常" + e.getMessage());
			buildFlow(null, request, response);
			return "/flow/buildFlow";
		}

		// 再到本地缓存
		// 不再更新缓存，由监听处理
		// FlowBean old = FlowCache.getInstance().cacheFlow(flow);
		// if (old != null) {
		// logger.info("流程被替换，旧流程：" + JsonUtil.toJson(old));
		// }

		request.getRequestDispatcher("/flow/list").forward(request, response);
		return null;
	}

	/**
	 * 只更新流程的状态
	 * 
	 * @param flow
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/updateStatus", method = RequestMethod.POST)
	@ResponseBody
	public String updateStatus(FlowBean flow, HttpServletRequest request, HttpServletResponse response) {

		Map<String, Object> result = new HashMap<String, Object>();
		try {

			FlowBean old = FlowCache.getInstance().getFlow(flow.getCode());
			if (old != null) {
				// 先到zookeeper
				try {
					old.setStatus(flow.getStatus());
					old.setUpdateTime(new Date());
					// 保存到zookeeper
					FlowToZookeeper.persistanceToZookeeper(old);

					result.put("status", "success");
					result.put("message", "状态已经更新到ZK，请稍后刷新");
				} catch (Exception e) {
					String errMsg = "同步到zookeeper上异常：" + e.getMessage();
					logger.error(errMsg, e);
					result.put("status", "fail");
					result.put("message", errMsg);
					return JsonUtil.toJson(result);
				}
			} else {
				result.put("status", "fail");
				result.put("message", "缓存中没有编码为[" + flow.getCode() + "]的流程");
			}

			return JsonUtil.toJson(result);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.put("status", "fail");
			result.put("message", e.getMessage());
			return JsonUtil.toJson(result);
		}
	}

	/**
	 * 删除流程信息
	 * 
	 * @param code
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	@ResponseBody
	public String delete(String code, HttpServletRequest request, HttpServletResponse response) {

		Map<String, Object> result = new HashMap<String, Object>();
		try {

			try {
				// 先删除zookeeper
				FlowToZookeeper.deleteFromZookeeper(code);

			} catch (Exception e) {
				String errMsg = "删除zookeeper上异常：" + e.getMessage();
				logger.error(errMsg, e);
				result.put("status", "fail");
				result.put("message", e.getMessage());
				return JsonUtil.toJson(result);
			}

			// 此处不再处理，由监听处理
			// FlowBean flow = FlowCache.getInstance().uncacheFlow(code);

			// logger.info("删除流程：" + JsonUtil.toJson(flow));

			result.put("status", "success");
			result.put("message", "已经从ZK上删除，稍后刷新列表");
			return JsonUtil.toJson(result);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.put("status", "fail");
			result.put("message", e.getMessage());
			return JsonUtil.toJson(result);
		}
	}

	/**
	 * 生成流程执行配置
	 * 
	 * @param code
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/createFlowExecuteConfig", method = RequestMethod.POST)
	@ResponseBody
	public String createFlowExecuteConfig(String code, HttpServletRequest request, HttpServletResponse response) {

		Map<String, Object> result = new HashMap<String, Object>();
		try {

			FlowExecuteConfig flowExecuteConfig = FlowExecuteConfigBuilder.build(code);

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

			// 此处不再缓存，改由监听处理
			// FlowExecuteConfig old =
			// FlowExecuteConfigCache.getInstance().cacheFlowExecuteConfig(flowExecuteConfig);

			// logger.info("生成流程执行配置：" + JsonUtil.toJson(flowExecuteConfig));

			// if (old != null) {
			// logger.info("替换旧流程执行配置：" + JsonUtil.toJson(old));
			// }

			result.put("status", "success");
			result.put("message", "已经上传到ZK，稍后刷新列表");
			return JsonUtil.toJson(result);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.put("status", "fail");
			result.put("message", e.getMessage());
			return JsonUtil.toJson(result);
		}
	}

	/**
	 * 复制一个流程
	 * 
	 * @param code
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/copyFlow", method = RequestMethod.POST)
	@ResponseBody
	public String copyFlow(String code, HttpServletRequest request, HttpServletResponse response) {

		Map<String, Object> result = new HashMap<String, Object>();
		try {

			// 先取出来流程信息
			FlowBean flow = FlowCache.getInstance().getFlow(code);
			if (flow == null) {
				result.put("status", "fail");
				result.put("message", "不存在编码为[" + code + "]的流程了");
				return JsonUtil.toJson(result);
			}

			// 创建流程的可执行配置,一方面可以找出所有的节点，另一方面可以检验流程的有效性
			FlowExecuteConfig config = FlowExecuteConfigBuilder.build(flow.getCode(), flow.getFlowContent(),
					flow.getFlowComPros(), flow.getNodeVSCom());

			// 先替换所有的线的ID
			if (config.getComDispatchs() != null && config.getComDispatchs().size() > 0) {
				for (ComponentDispatchBean cdb : config.getComDispatchs()) {
					String id = String.valueOf(System.currentTimeMillis());
					if (StringUtils.isNotBlank(flow.getFlowContent())) {
						flow.setFlowContent(flow.getFlowContent().replaceAll(cdb.getLineID(), id));
					}
					if (StringUtils.isNotBlank(flow.getFlowComPros())) {
						flow.setFlowComPros(flow.getFlowComPros().replaceAll(cdb.getLineID(), id));
					}
					if (StringUtils.isNotBlank(flow.getNodeVSCom())) {
						flow.setNodeVSCom(flow.getNodeVSCom().replaceAll(cdb.getLineID(), id));
					}
					Thread.sleep(1);
				}
			}

			// 再替换节点的ID
			if (config.getComNodes() != null && config.getComNodes().size() > 0) {
				for (ComponentNodeBean cnb : config.getComNodes()) {
					String id = String.valueOf(System.currentTimeMillis());
					if (StringUtils.isNotBlank(flow.getFlowContent())) {
						flow.setFlowContent(flow.getFlowContent().replaceAll(cnb.getNodeID(), id));
					}
					if (StringUtils.isNotBlank(flow.getFlowComPros())) {
						flow.setFlowComPros(flow.getFlowComPros().replaceAll(cnb.getNodeID(), id));
					}
					if (StringUtils.isNotBlank(flow.getNodeVSCom())) {
						flow.setNodeVSCom(flow.getNodeVSCom().replaceAll(cnb.getNodeID(), id));
					}
					Thread.sleep(1);
				}
			}

			// 修改其它属性
			flow.setStatus(FlowStatus.NORMAL);
			flow.setRemark("由流程[" + flow.getName() + "][" + flow.getCode() + "]复制而来");
			flow.setName(flow.getName() + "[复制品]");
			flow.setCode(RandomStringUtils.randomNumeric(16));
			flow.setId(DistributedSequence.getInstance().getNext());
			flow.setCreateTime(new Date());
			flow.setUpdateTime(new Date());

			try {
				// 保存到zookeeper
				FlowToZookeeper.persistanceToZookeeper(flow);

			} catch (Exception e) {
				String errMsg = "保存到zookeeper上异常：" + e.getMessage();
				logger.error(errMsg, e);
				result.put("status", "fail");
				result.put("message", errMsg);
				return JsonUtil.toJson(result);
			}

			// 不再更新缓存，改由监听处理
			// 缓存起来
			// FlowBean old = FlowCache.getInstance().cacheFlow(flow);
			// if (old != null) {
			// logger.info("流程被替换掉：" + JsonUtil.toJson(old));
			// }

			result.put("status", "success");
			result.put("message", "已经上传到ZK上，稍后刷新列表查看");
			return JsonUtil.toJson(result);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.put("status", "fail");
			result.put("message", e.getMessage());
			return JsonUtil.toJson(result);
		}
	}
}
