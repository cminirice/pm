/**
 *
 */
package com.guttv.pm.platform.action.container;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.guttv.pm.container.onstart.ContainerRegister;
import com.guttv.pm.core.bean.ExecuteContainer;
import com.guttv.pm.core.cache.ConfigCache;
import com.guttv.pm.core.rpc.ExecuteContainerService;
import com.guttv.pm.core.zk.ZookeeperHelper;
import com.guttv.pm.platform.PlatformMain;
import com.guttv.pm.platform.action.BaseAction;
import com.guttv.pm.platform.action.container.comparator.ExecuteContainerComparator;
import com.guttv.pm.platform.action.container.search.ExecuteContainerFilter;
import com.guttv.pm.platform.container.ExecuteContainerCache;
import com.guttv.pm.utils.Constants;
import com.guttv.pm.utils.Enums.ExecuteContainerStatus;
import com.guttv.pm.utils.JsonUtil;
import com.guttv.pm.utils.Pager;
import com.guttv.rpc.client.RpcProxy;

/**
 * 执行容器信息，原则上，执行容器信息都是从ZK上直接获取，需要更新的状态都要经过RPC进行修改然后再从ZK上同步到平台，这与容器的执行配置列表的原则有所不同
 *
 * @author Peter
 *
 */
@Controller
@RequestMapping("/container")
public class ExecuteContainerAction extends BaseAction {

	@RequestMapping("/list")
	public String list(Pager pager, HttpServletRequest request, HttpServletResponse response) {
		List<ExecuteContainer> containers = ExecuteContainerCache.getInstance().getAllExecuteContainer();

		// 过滤 即查找
		containers = new ExecuteContainerFilter().filter(containers, pager.getSearchBy(), pager.getKeyword());

		// 排序
		if (containers != null && containers.size() > 0) {
			Collections.sort(containers, new ExecuteContainerComparator(pager.getOrderBy(), pager.getOrder()));
		}

		pager.setTotalCount(containers == null ? 0 : containers.size());
		pager.setResult(pager(pager,containers));

		request.setAttribute("pager", pager);

		return "/container/list";
	}

	@RequestMapping("/view")
	public String view(String containerID, HttpServletRequest request, HttpServletResponse response) {
		request.setAttribute("executeContainer", ExecuteContainerCache.getInstance().getExecuteContainer(containerID));
		return "/container/view";
	}

	/**
	 * 更新执行容器的基本信息 该方法只做更新
	 *
	 * @param flow
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@RequestMapping("/saveOrUpdate")
	public String saveOrUpdate(ExecuteContainer container, HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		if (StringUtils.isNotBlank(container.getContainerID())) {
			ExecuteContainer old = ExecuteContainerCache.getInstance().getExecuteContainer(container.getContainerID());
			if (old != null) {
				// 调用RPC更新
				if (ExecuteContainerStatus.NORMAL.equals(old.getStatus())) {
					long timeout = ConfigCache.getInstance().getProperty(Constants.RPC_TIMEOUT, 100000);
					RpcProxy prcProxy = null;
					try {
						prcProxy = ExecuteContainerCache.getInstance().getZookeeperRpcProxy(container.getContainerID(),
								timeout);
						if (prcProxy == null) {
							request.setAttribute("errMsg", "获取容器的接口代理为空");
						} else {
							ExecuteContainerService containerService = (ExecuteContainerService) prcProxy
									.create(ExecuteContainerService.class);

							containerService.update(container);

							request.setAttribute("message", "更新指令已经执行成功,请稍后刷新列表");
						}
					} catch (Exception e) {
						request.setAttribute("errMsg", "执行接口代理异常：" + e.getMessage());
						logger.error("执行容器[" + container.getContainerID() + "]的接口代理异常：" + e.getMessage(), e);
					}

				} else {
					request.setAttribute("errMsg", "当前状态[" + old.getStatus() + "]不支持修改");
				}

			} else {
				request.setAttribute("errMsg", "缓存中没有找到ID为[" + container.getContainerID() + "]的容器");
			}

		} else {
			request.setAttribute("errMsg", "没有取得容器ID");
		}

		return list(new Pager(), request, response);
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
	public String input(String containerID, HttpServletRequest request, HttpServletResponse response) {
		view(containerID, request, response);
		return "/container/input";
	}

	@RequestMapping(value = "/shutdown", method = RequestMethod.POST)
	@ResponseBody
	public String shutdown(String containerID, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> result = new HashMap<String, Object>();
		try {

			if (StringUtils.isBlank(containerID)) {
				result.put("status", "fail");
				result.put("message", "执行容器ID为空");
				return JsonUtil.toJson(result);
			}

			ExecuteContainer container = ExecuteContainerCache.getInstance().getExecuteContainer(containerID);
			if (container == null) {
				result.put("status", "fail");
				result.put("message", "缓存中不存在ID为[" + containerID + "]的执行容器");
				return JsonUtil.toJson(result);
			}

			// 只有正常状态的容器才能发关闭命令
			if (!ExecuteContainerStatus.NORMAL.equals(container.getStatus())) {
				result.put("status", "fail");
				result.put("message", "只有[" + ExecuteContainerStatus.NORMAL.getName() + "]状态的容器才能关闭");
				return JsonUtil.toJson(result);
			}

			if (!container.isOnlyContainer()) {
				result.put("status", "fail");
				result.put("message", "只有仅以执行容器模式启动的容器才能被关闭");
				return JsonUtil.toJson(result);
			}

			// 判断执行容器是不是就是服务本身 容器ID一样，并且进程号一样
			String pid = System.getProperty("PID");
			if (containerID.equalsIgnoreCase(ContainerRegister.getContainerID()) && pid.equals(container.getPid())) {
				result.put("status", "fail");
				result.put("message", "不能关闭管理平台自身带的执行容器");
				return JsonUtil.toJson(result);
			}

			long timeout = ConfigCache.getInstance().getProperty(Constants.RPC_TIMEOUT, 100000);
			RpcProxy prcProxy = null;
			try {
				prcProxy = ExecuteContainerCache.getInstance().getZookeeperRpcProxy(container.getContainerID(), timeout,
						false);
				if (prcProxy == null) {
					result.put("status", "fail");
					result.put("message", "获取容器的接口代理为空");
					return JsonUtil.toJson(result);
				} else {
					ExecuteContainerService containerService = (ExecuteContainerService) prcProxy
							.create(ExecuteContainerService.class);

					containerService.shutdown();

					result.put("status", "success");
					result.put("message", "指令已发出，请稍后观察容器状态");

					return JsonUtil.toJson(result);
				}
			} catch (Exception e) {
				logger.error("执行容器[" + container.getContainerID() + "]的接口代理异常：" + e.getMessage(), e);
				result.put("status", "fail");
				result.put("message", "执行接口代理异常");
				return JsonUtil.toJson(result);
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.put("status", "fail");
			result.put("message", e.getMessage());
			return JsonUtil.toJson(result);
		}
	}

	@RequestMapping(value = "/forbbiden", method = RequestMethod.POST)
	@ResponseBody
	public String forbbiden(String containerID, HttpServletRequest request, HttpServletResponse response) {

		Map<String, Object> result = new HashMap<String, Object>();
		try {

			if (StringUtils.isBlank(containerID)) {
				result.put("status", "fail");
				result.put("message", "执行容器ID为空");
				return JsonUtil.toJson(result);
			}

			ExecuteContainer container = ExecuteContainerCache.getInstance().getExecuteContainer(containerID);
			if (container == null) {
				result.put("status", "fail");
				result.put("message", "缓存中不存在ID为[" + containerID + "]的执行容器");
				return JsonUtil.toJson(result);
			}

			// 只有正常状态的容器才能发关闭命令
			if (!ExecuteContainerStatus.NORMAL.equals(container.getStatus())) {
				result.put("status", "fail");
				result.put("message", "只有[" + ExecuteContainerStatus.NORMAL.getName() + "]状态的容器才能禁用");
				return JsonUtil.toJson(result);
			}

			long timeout = ConfigCache.getInstance().getProperty(Constants.RPC_TIMEOUT, 100000);
			RpcProxy prcProxy = null;
			try {
				prcProxy = ExecuteContainerCache.getInstance().getZookeeperRpcProxy(container.getContainerID(), timeout,
						true);
				if (prcProxy == null) {
					result.put("status", "fail");
					result.put("message", "获取容器的接口代理为空");
					return JsonUtil.toJson(result);
				} else {
					ExecuteContainerService containerService = (ExecuteContainerService) prcProxy
							.create(ExecuteContainerService.class);

					containerService.forbbiden();

					result.put("status", "success");
					result.put("message", "指令已发出，请稍后观察容器状态");

					return JsonUtil.toJson(result);
				}
			} catch (Exception e) {
				logger.error("执行容器[" + container.getContainerID() + "]的接口代理异常：" + e.getMessage(), e);
				result.put("status", "fail");
				result.put("message", "执行接口代理异常");
				return JsonUtil.toJson(result);
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.put("status", "fail");
			result.put("message", e.getMessage());
			return JsonUtil.toJson(result);
		}
	}

	/**
	 * 启用
	 *
	 * @param containerID
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/start", method = RequestMethod.POST)
	@ResponseBody
	public String start(String containerID, HttpServletRequest request, HttpServletResponse response) {

		Map<String, Object> result = new HashMap<String, Object>();
		try {

			if (StringUtils.isBlank(containerID)) {
				result.put("status", "fail");
				result.put("message", "执行容器ID为空");
				return JsonUtil.toJson(result);
			}

			ExecuteContainer container = ExecuteContainerCache.getInstance().getExecuteContainer(containerID);
			if (container == null) {
				result.put("status", "fail");
				result.put("message", "缓存中不存在ID为[" + containerID + "]的执行容器");
				return JsonUtil.toJson(result);
			}

			// 只有正常状态的容器才能发关闭命令
			if (!ExecuteContainerStatus.FORBBIDEN.equals(container.getStatus())) {
				result.put("status", "fail");
				result.put("message", "只有[" + ExecuteContainerStatus.FORBBIDEN.getName() + "]状态的容器才能启用");
				return JsonUtil.toJson(result);
			}

			long timeout = ConfigCache.getInstance().getProperty(Constants.RPC_TIMEOUT, 100000);
			RpcProxy prcProxy = null;

			try {
				prcProxy = ExecuteContainerCache.getInstance().getZookeeperRpcProxy(container.getContainerID(), timeout,
						true);
				if (prcProxy == null) {
					result.put("status", "fail");
					result.put("message", "获取容器的接口代理为空");
					return JsonUtil.toJson(result);
				} else {
					ExecuteContainerService containerService = (ExecuteContainerService) prcProxy
							.create(ExecuteContainerService.class);

					containerService.start();

					result.put("status", "success");
					result.put("message", "指令已发出，请稍后观察容器状态");

					return JsonUtil.toJson(result);
				}
			} catch (Exception e) {
				logger.error("执行容器[" + container.getContainerID() + "]的接口代理异常：" + e.getMessage(), e);
				result.put("status", "fail");
				result.put("message", "执行接口代理异常");
				return JsonUtil.toJson(result);
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.put("status", "fail");
			result.put("message", e.getMessage());
			return JsonUtil.toJson(result);
		}
	}

	/**
	 * 删除容器
	 *
	 * @param containerID
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/deleteContainer", method = RequestMethod.POST)
	@ResponseBody
	public String deleteContainer(String containerID, HttpServletRequest request, HttpServletResponse response) {

		Map<String, Object> result = new HashMap<String, Object>();
		try {

			if (StringUtils.isBlank(containerID)) {
				result.put("status", "fail");
				result.put("message", "执行容器ID为空");
				return JsonUtil.toJson(result);
			}

			ExecuteContainer container = ExecuteContainerCache.getInstance().getExecuteContainer(containerID);
			if (container == null) {
				result.put("status", "fail");
				result.put("message", "缓存中不存在ID为[" + containerID + "]的执行容器");
				return JsonUtil.toJson(result);
			}

			// 只有正常状态的容器才能发关闭命令
			if (ExecuteContainerStatus.NORMAL.equals(container.getStatus())) {
				result.put("status", "fail");
				result.put("message", "只有非[" + ExecuteContainerStatus.NORMAL.getName() + "]状态的容器才能被删除");
				return JsonUtil.toJson(result);
			}

			// 删除前尝试发一个禁用指令
			RpcProxy prcProxy = null;
			try {
				prcProxy = ExecuteContainerCache.getInstance().getZookeeperRpcProxy(container.getContainerID(), 0,
						false);
				if (prcProxy != null) {
					ExecuteContainerService containerService = (ExecuteContainerService) prcProxy
							.create(ExecuteContainerService.class);

					containerService.forbbiden();
				}
			} catch (Exception e) {
				logger.error("执行容器[" + container.getContainerID() + "]的接口代理异常：" + e.getMessage(), e);
			}

			ZookeeperHelper.deleteFromZookeeper(container.getRegistPath(), PlatformMain.client);

			result.put("status", "success");
			result.put("message", "容器注册路径已经删除，请稍后刷新列表");
			return JsonUtil.toJson(result);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.put("status", "fail");
			result.put("message", e.getMessage());
			return JsonUtil.toJson(result);
		}
	}

	/**
	 * 删除容器
	 *
	 * @param containerID
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/refreshSpringconfig", method = RequestMethod.POST)
	@ResponseBody
	public String refreshSpringconfig(String containerID, HttpServletRequest request, HttpServletResponse response) {

		Map<String, Object> result = new HashMap<String, Object>();
		try {

			if (StringUtils.isBlank(containerID)) {
				result.put("status", "fail");
				result.put("message", "执行容器ID为空");
				return JsonUtil.toJson(result);
			}

			ExecuteContainer container = ExecuteContainerCache.getInstance().getExecuteContainer(containerID);
			if (container == null) {
				result.put("status", "fail");
				result.put("message", "缓存中不存在ID为[" + containerID + "]的执行容器");
				return JsonUtil.toJson(result);
			}

			// 只有正常状态的容器才能发命令
			if (!ExecuteContainerStatus.NORMAL.equals(container.getStatus())) {
				result.put("status", "fail");
				result.put("message", "只有非[" + ExecuteContainerStatus.NORMAL.getName() + "]状态的容器才能操作");
				return JsonUtil.toJson(result);
			}

			// 删除前尝试发一个禁用指令
			RpcProxy prcProxy = null;
			try {
				long timeout = ConfigCache.getInstance().getProperty(Constants.RPC_TIMEOUT, 100000);
				prcProxy = ExecuteContainerCache.getInstance().getZookeeperRpcProxy(container.getContainerID(), timeout,
						true);
				if (prcProxy != null) {
					ExecuteContainerService containerService = (ExecuteContainerService) prcProxy
							.create(ExecuteContainerService.class);

					containerService.refreshSpringConfig();
					;
				}
			} catch (Exception e) {
				logger.error("执行容器[" + container.getContainerID() + "]的接口代理异常：" + e.getMessage(), e);
			}

			result.put("status", "success");
			result.put("message", "刷新指令已经发出，请稍后刷新列表");
			return JsonUtil.toJson(result);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.put("status", "fail");
			result.put("message", e.getMessage());
			return JsonUtil.toJson(result);
		}
	}

	@RequestMapping("/viewSpringconfig")
	public String viewSpringconfig(String containerID, HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		request.setAttribute("containerID", containerID);
		if (StringUtils.isBlank(containerID)) {
			request.setAttribute("errMsg", "执行容器ID为空");
			return "/container/viewSpringConfig";
		}

		ExecuteContainer container = ExecuteContainerCache.getInstance().getExecuteContainer(containerID);
		if (container == null) {
			request.setAttribute("errMsg", "缓存中不存在ID为[" + containerID + "]的执行容器");
			return "/container/viewSpringConfig";
		}

		// 只有正常状态的容器才能获取信息
		if (!ExecuteContainerStatus.NORMAL.equals(container.getStatus())) {
			request.setAttribute("errMsg", "只有非[" + ExecuteContainerStatus.NORMAL.getName() + "]状态的容器才能获取配置信息");
			return "/container/viewSpringConfig";
		}

		// 删除前尝试发一个禁用指令
		RpcProxy prcProxy = null;
		try {
			long timeout = ConfigCache.getInstance().getProperty(Constants.RPC_TIMEOUT, 100000);
			prcProxy = ExecuteContainerCache.getInstance().getZookeeperRpcProxy(container.getContainerID(), timeout,
					true);
			if (prcProxy != null) {
				ExecuteContainerService containerService = (ExecuteContainerService) prcProxy
						.create(ExecuteContainerService.class);

				request.setAttribute("properties", containerService.getSpringConfig());

			}
		} catch (Exception e) {
			logger.error("执行容器[" + container.getContainerID() + "]的接口代理异常：" + e.getMessage(), e);
			request.setAttribute("errMsg", "执行容器[" + container.getContainerID() + "]的接口代理异常：" + e.getMessage());
		}

		return "/container/viewSpringConfig";
	}

}
