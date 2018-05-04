/**
 * 
 */
package com.guttv.pm.platform.action.container;

import java.util.Collections;
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

import com.guttv.pm.core.bean.ExecuteContainer;
import com.guttv.pm.core.cache.ConfigCache;
import com.guttv.pm.core.cache.FlowExecuteConfigCache;
import com.guttv.pm.core.flow.FlowExecuteConfig;
import com.guttv.pm.core.rpc.FlowExecuteConfigService;
import com.guttv.pm.platform.action.BaseAction;
import com.guttv.pm.platform.action.meta.comparator.FlowExecuteConfigComparator;
import com.guttv.pm.platform.action.meta.search.FlowExecuteConfigFilter;
import com.guttv.pm.platform.container.ExecuteContainerCache;
import com.guttv.pm.utils.Constants;
import com.guttv.pm.utils.Enums.ExecuteContainerStatus;
import com.guttv.pm.utils.Enums.FlowExecuteStatus;
import com.guttv.pm.utils.JsonUtil;
import com.guttv.pm.utils.Pager;
import com.guttv.rpc.client.RpcProxy;

/**
 * 
 * 容器的流程执行配置信息，原则上都要通过RPC从容器中获取，跟容器信息不同
 * 
 * @author Peter
 *
 */
@Controller
@RequestMapping("/containerFlowExeConf")
public class ContainerFlowExecConfAction extends BaseAction {

	/**
	 * 判断容器是不是存在，是不是正常状态
	 * 
	 * @param containerID
	 * @param request
	 * @return 条件都通过，返回容器对象，否则返回null
	 */
	private ExecuteContainer checkContainer(String containerID, HttpServletRequest request) {
		if (StringUtils.isBlank(containerID)) {
			request.setAttribute("errMsg", "执行容器ID为空");
			return null;
		}

		ExecuteContainer container = ExecuteContainerCache.getInstance().getExecuteContainer(containerID);
		if (container == null) {
			request.setAttribute("errMsg", "不存在容器ID为[" + containerID + "]的执行容器");
			return null;
		}

		// 只有正常状态的容器才能发关闭命令
		if (!ExecuteContainerStatus.NORMAL.equals(container.getStatus())) {
			request.setAttribute("errMsg", "只有[" + ExecuteContainerStatus.NORMAL.getName() + "]状态的容器才有此操作");
			return null;
		}
		return container;
	}

	/**
	 * 获取RPC服务
	 * 
	 * @param request
	 * @return
	 */
	private FlowExecuteConfigService getRpcService(ExecuteContainer container, HttpServletRequest request) {
		// 调用RPC
		long timeout = ConfigCache.getInstance().getProperty(Constants.RPC_TIMEOUT, 30000);
		RpcProxy prcProxy = null;
		try {
			prcProxy = ExecuteContainerCache.getInstance().getZookeeperRpcProxy(container.getContainerID(), timeout);
			if (prcProxy == null) {
				request.setAttribute("errMsg", "获取容器的接口代理为空");
				return null;
			} else {
				return prcProxy.create(FlowExecuteConfigService.class);
			}
		} catch (Exception e) {
			request.setAttribute("errMsg", "获取代理接口异常：" + e.getMessage());
			logger.error("获取执行容器[" + container.getContainerID() + "]的代理接口异常：" + e.getMessage(), e);
			return null;
		}
	}

	@RequestMapping("/list")
	public String list(Pager pager, String containerID, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		request.setAttribute("containerID", containerID);

		// 验证容器状态
		ExecuteContainer container = checkContainer(containerID, request);
		if (container == null) {
			request.getRequestDispatcher("/container/list").forward(request, response);
			return null;
		}

		// 调用RPC
		try {
			FlowExecuteConfigService flowExecuteConfigService = getRpcService(container, request);
			if (flowExecuteConfigService == null) {
				request.getRequestDispatcher("/container/list").forward(request, response);
				return null;
			}

			List<FlowExecuteConfig> flowExecuteConfigs = flowExecuteConfigService.getAllFlowExecuteConfigs();

			// 过滤 即查找
			if (flowExecuteConfigs != null && flowExecuteConfigs.size() > 0) {
				flowExecuteConfigs = new FlowExecuteConfigFilter().filter(flowExecuteConfigs, pager.getSearchBy(),
						pager.getKeyword());
			}

			// 排序
			if (flowExecuteConfigs != null && flowExecuteConfigs.size() > 0) {
				Collections.sort(flowExecuteConfigs,
						new FlowExecuteConfigComparator(pager.getOrderBy(), pager.getOrder()));
			}

			pager.setResult(pager(pager,flowExecuteConfigs));
			pager.setTotalCount(flowExecuteConfigs == null ? 0 : flowExecuteConfigs.size());
		} catch (Exception e) {
			request.setAttribute("errMsg", "执行接口代理异常：" + e.getMessage());
			request.getRequestDispatcher("/container/list").forward(request, response);
			logger.error("执行容器[" + container.getContainerID() + "]的接口代理异常：" + e.getMessage(), e);
			return null;
		}

		return "/containerFlowExeConf/list";
	}

	/**
	 * 准备添加新执行配置
	 * 
	 * @param pager
	 * @param containerID
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/beforeAdd")
	public String beforeAdd(Pager pager, String containerID, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		request.setAttribute("containerID", containerID);

		// 验证容器状态
		ExecuteContainer container = checkContainer(containerID, request);
		if (container == null) {
			request.getRequestDispatcher("/container/list").forward(request, response);
			return null;
		}

		// 先从本地取出所有的流程执行配置
		List<FlowExecuteConfig> flowExecuteConfigs = FlowExecuteConfigCache.getInstance().getAllFlowExecuteConfigs();

		if (flowExecuteConfigs != null) {
			// 去掉禁用的
			Iterator<FlowExecuteConfig> iter = flowExecuteConfigs.iterator();
			while (iter.hasNext()) {
				if (FlowExecuteStatus.FORBIDDEN.equals(iter.next().getStatus())) {
					iter.remove();
				}
			}
		}

		if (flowExecuteConfigs == null || flowExecuteConfigs.size() == 0) {
			request.setAttribute("errMsg", "没有可用的流程执行配置");
			request.getRequestDispatcher("/containerFlowExeConf/list").forward(request, response);
			return null;
		}

		// 调用RPC
		try {
			// 获取RPC服务
			FlowExecuteConfigService flowExecuteConfigService = getRpcService(container, request);
			if (flowExecuteConfigService == null) {
				request.getRequestDispatcher("/container/list").forward(request, response);
				return null;
			}

			List<String> containerFlowExeConfigCodes = flowExecuteConfigService.getAllFlowExecConfCodes();

			// 去掉已经添加过的
			if (containerFlowExeConfigCodes != null && containerFlowExeConfigCodes.size() > 0) {
				Iterator<FlowExecuteConfig> iter = null;
				FlowExecuteConfig config = null;
				for (String code : containerFlowExeConfigCodes) {
					iter = flowExecuteConfigs.iterator();
					while (iter.hasNext()) {
						config = iter.next();
						if (code.equals(config.getFlowExeCode())) {
							iter.remove();
							break;
						}
					}
				}
			}

			// 过滤 即查找
			flowExecuteConfigs = new FlowExecuteConfigFilter().filter(flowExecuteConfigs, pager.getSearchBy(),
					pager.getKeyword());

			// 排序
			Collections.sort(flowExecuteConfigs, new FlowExecuteConfigComparator(pager.getOrderBy(), pager.getOrder()));

			pager.setResult(flowExecuteConfigs);
			pager.setTotalCount(flowExecuteConfigs == null ? 0 : flowExecuteConfigs.size());

		} catch (Exception e) {
			request.setAttribute("errMsg", "执行接口代理异常：" + e.getMessage());
			request.getRequestDispatcher("/container/list").forward(request, response);
			logger.error("执行容器[" + container.getContainerID() + "]的接口代理异常：" + e.getMessage(), e);
			return null;
		}

		return "/containerFlowExeConf/add";
	}

	/**
	 * 添加新执行配置
	 * 
	 * @param pager
	 * @param containerID
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/add")
	public String add(String containerID, String[] ids, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		request.setAttribute("containerID", containerID);

		// 验证容器状态
		ExecuteContainer container = checkContainer(containerID, request);
		if (container == null) {
			request.getRequestDispatcher("/container/list").forward(request, response);
			return null;
		}

		if (ids == null || ids.length == 0) {
			request.setAttribute("message", "没有选择任务流程执行配置信息");
			request.getRequestDispatcher("/containerFlowExeConf/list").forward(request, response);
			return null;
		}

		// 调用RPC
		try {
			// 获取RPC服务
			FlowExecuteConfigService flowExecuteConfigService = getRpcService(container, request);
			if (flowExecuteConfigService == null) {
				request.getRequestDispatcher("/container/list").forward(request, response);
				return null;
			}
			String message = flowExecuteConfigService.addFlowExecConfigs(ids);
			if (StringUtils.isBlank(message)) {
				request.setAttribute("message", message);
			}
		} catch (Exception e) {
			request.setAttribute("errMsg", "执行接口代理异常：" + e.getMessage());
			request.getRequestDispatcher("/container/list").forward(request, response);
			logger.error("执行容器[" + container.getContainerID() + "]的接口代理异常：" + e.getMessage(), e);
			return null;
		}

		// 这个地方必须得redirect，如果用request.getRequestDispatcher("").forward(request,
		// response);，页面刷新时，会重新添加
		response.sendRedirect(request.getContextPath() + "/containerFlowExeConf/list?containerID=" + containerID);
		return null;
	}

	/**
	 * 校验 containerID、flowExeCode及容器状态
	 * 
	 * @param containerID
	 * @param flowExeCode
	 * @param request
	 * @return
	 */
	private FlowExecuteConfigService checkAndGetRpcService(String containerID, String flowExeCode,
			HttpServletRequest request) {
		request.setAttribute("containerID", containerID);

		ExecuteContainer container = checkContainer(containerID, request);
		if (container == null) {
			return null;
		}

		if (StringUtils.isBlank(flowExeCode)) {
			request.setAttribute("errMsg", "流程执行编码为空");
			return null;
		}

		// 获取RPC服务
		FlowExecuteConfigService flowExecuteConfigService = getRpcService(container, request);
		if (flowExecuteConfigService == null) {
			return null;
		}
		return flowExecuteConfigService;
	}

	/**
	 * 查看容器的流程执行配置
	 * 
	 * @param pager
	 * @param containerID
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/view")
	public String view(String containerID, String flowExeCode, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		// 调用RPC
		try {
			// 获取RPC服务
			FlowExecuteConfigService rpcService = checkAndGetRpcService(containerID, flowExeCode, request);
			if (rpcService == null) {
				request.getRequestDispatcher("/container/list").forward(request, response);
				return null;
			}
			FlowExecuteConfig flowExeConfig = rpcService.getFlowExecuteConfig(flowExeCode);
			if (flowExeConfig == null) {
				request.setAttribute("errMsg", "执行容器没有编码为[" + flowExeCode + "]的流程执行配置信息");
				request.getRequestDispatcher("/containerFlowExeConf/list").forward(request, response);
				return null;
			} else {
				request.setAttribute("flowExeConfig", flowExeConfig);
				return "/containerFlowExeConf/view";
			}
		} catch (Exception e) {
			request.setAttribute("errMsg", "执行接口代理异常：" + e.getMessage());
			request.getRequestDispatcher("/container/list").forward(request, response);
			logger.error("执行容器[" + containerID + "]的接口代理异常：" + e.getMessage(), e);
			return null;
		}
	}

	/**
	 * 停止流程执行配置
	 * 
	 * @param pager
	 * @param containerID
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/stopFlowExecConfig", method = RequestMethod.POST)
	@ResponseBody
	public String stopFlowExecConfig(String containerID, String flowExeCode, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		// 调用RPC
		try {
			// 获取RPC服务
			FlowExecuteConfigService rpcService = checkAndGetRpcService(containerID, flowExeCode, request);
			if (rpcService == null) {
				result.put("status", "fail");
				result.put("message", request.getAttribute("errMsg"));
				return JsonUtil.toJson(result);
			}

			rpcService.stopFlowExecConfig(flowExeCode);

			result.put("status", "success");
			result.put("message", "指令已经发出，稍后刷新列表查看");
			return JsonUtil.toJson(result);
		} catch (Exception e) {
			logger.error("执行容器[" + containerID + "]的接口代理异常：" + e.getMessage(), e);
			result.put("status", "fail");
			result.put("message", "执行接口代理异常：" + e.getMessage());
			return JsonUtil.toJson(result);
		}
	}

	/**
	 * 停止流程执行配置
	 * 
	 * @param pager
	 * @param containerID
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/startFlowExecConfig", method = RequestMethod.POST)
	@ResponseBody
	public String startFlowExecConfig(String containerID, String flowExeCode, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		// 调用RPC
		try {
			// 获取RPC服务
			FlowExecuteConfigService rpcService = checkAndGetRpcService(containerID, flowExeCode, request);
			if (rpcService == null) {
				result.put("status", "fail");
				result.put("message", request.getAttribute("errMsg"));
				return JsonUtil.toJson(result);
			}

			rpcService.startFlowExecConfig(flowExeCode);

			result.put("status", "success");
			result.put("message", "指令已经发出，稍后刷新列表查看");
			return JsonUtil.toJson(result);
		} catch (Exception e) {
			logger.error("执行容器[" + containerID + "]的接口代理异常：" + e.getMessage(), e);
			result.put("status", "fail");
			result.put("message", "执行接口代理异常：" + e.getMessage());
			return JsonUtil.toJson(result);
		}
	}

	/**
	 * 初始化流程执行配置
	 * 
	 * @param pager
	 * @param containerID
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/initFlowExecConfig", method = RequestMethod.POST)
	@ResponseBody
	public String initFlowExecConfig(String containerID, String flowExeCode, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		// 调用RPC
		try {
			// 获取RPC服务
			FlowExecuteConfigService rpcService = checkAndGetRpcService(containerID, flowExeCode, request);
			if (rpcService == null) {
				result.put("status", "fail");
				result.put("message", request.getAttribute("errMsg"));
				return JsonUtil.toJson(result);
			}

			rpcService.initFlowExecConfig(flowExeCode);

			result.put("status", "success");
			result.put("message", "指令已经发出，稍后刷新列表查看");
			return JsonUtil.toJson(result);
		} catch (Exception e) {
			logger.error("执行容器[" + containerID + "]的接口代理异常：" + e.getMessage(), e);
			result.put("status", "fail");
			result.put("message", "执行接口代理异常：" + e.getMessage());
			return JsonUtil.toJson(result);
		}
	}

	/**
	 * 删除流程执行配置
	 * 
	 * @param pager
	 * @param containerID
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/deleteFlowExeConfig", method = RequestMethod.POST)
	@ResponseBody
	public String deleteFlowExeConfig(String containerID, String flowExeCode, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		// 调用RPC
		try {
			// 获取RPC服务
			FlowExecuteConfigService rpcService = checkAndGetRpcService(containerID, flowExeCode, request);
			if (rpcService == null) {
				result.put("status", "fail");
				result.put("message", request.getAttribute("errMsg"));
				return JsonUtil.toJson(result);
			}

			rpcService.deleteFlowExeConfig(flowExeCode);

			result.put("status", "success");
			result.put("message", "指令已经发出，稍后刷新列表查看");
			return JsonUtil.toJson(result);
		} catch (Exception e) {
			logger.error("执行容器[" + containerID + "]的接口代理异常：" + e.getMessage(), e);
			result.put("status", "fail");
			result.put("message", "执行接口代理异常：" + e.getMessage());
			return JsonUtil.toJson(result);
		}
	}

	/**
	 * 继续执行结点
	 * 
	 * @param pager
	 * @param containerID
	 * @param nodeID
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/taskGo", method = RequestMethod.POST)
	@ResponseBody
	public String taskGo(String containerID, String flowExeCode, String nodeID, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		// 调用RPC
		try {
			// 获取RPC服务
			FlowExecuteConfigService rpcService = checkAndGetRpcService(containerID, flowExeCode, request);
			if (rpcService == null) {
				result.put("status", "fail");
				result.put("message", request.getAttribute("errMsg"));
				return JsonUtil.toJson(result);
			}

			rpcService.taskGo(flowExeCode, nodeID);

			result.put("status", "success");
			result.put("message", "指令已经发出，稍后刷新页面查看");
			return JsonUtil.toJson(result);
		} catch (Exception e) {
			logger.error("执行容器[" + containerID + "]的接口代理异常：" + e.getMessage(), e);
			result.put("status", "fail");
			result.put("message", "执行接口代理异常：" + e.getMessage());
			return JsonUtil.toJson(result);
		}
	}

	/**
	 * 暂停执行结点
	 * 
	 * @param pager
	 * @param containerID
	 * @param nodeID
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/taskPause", method = RequestMethod.POST)
	@ResponseBody
	public String taskPause(String containerID, String flowExeCode, String nodeID, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		// 调用RPC
		try {
			// 获取RPC服务
			FlowExecuteConfigService rpcService = checkAndGetRpcService(containerID, flowExeCode, request);
			if (rpcService == null) {
				result.put("status", "fail");
				result.put("message", request.getAttribute("errMsg"));
				return JsonUtil.toJson(result);
			}

			rpcService.taskPause(flowExeCode, nodeID);

			result.put("status", "success");
			result.put("message", "指令已经发出，稍后刷新页面查看");
			return JsonUtil.toJson(result);
		} catch (Exception e) {
			logger.error("执行容器[" + containerID + "]的接口代理异常：" + e.getMessage(), e);
			result.put("status", "fail");
			result.put("message", "执行接口代理异常：" + e.getMessage());
			return JsonUtil.toJson(result);
		}
	}

	/**
	 * 获取任务结点的任务堆栈信息
	 * 
	 * @param pager
	 * @param containerID
	 * @param nodeID
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/taskStacktrace", method = RequestMethod.POST)
	@ResponseBody
	public String taskStacktrace(String containerID, String flowExeCode, String nodeID, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		// 调用RPC
		try {
			// 获取RPC服务
			FlowExecuteConfigService rpcService = checkAndGetRpcService(containerID, flowExeCode, request);
			if (rpcService == null) {
				result.put("status", "fail");
				result.put("message", request.getAttribute("errMsg"));
				return JsonUtil.toJson(result);
			}

			String stacktrace = rpcService.taskStacktrace(flowExeCode, nodeID);

			if (StringUtils.isBlank(stacktrace)) {
				result.put("status", "fail");
				result.put("message", "没有取到任务信息");
			} else {
				result.put("status", "success");
				result.put("message", stacktrace);
			}

			return JsonUtil.toJson(result);
		} catch (Exception e) {
			logger.error("执行容器[" + containerID + "]的接口代理异常：" + e.getMessage(), e);
			result.put("status", "fail");
			result.put("message", "执行接口代理异常：" + e.getMessage());
			return JsonUtil.toJson(result);
		}
	}
}
