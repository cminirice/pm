/**
 * 
 */
package com.guttv.pm.platform.action.meta;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.guttv.pm.core.bean.ComponentBean;
import com.guttv.pm.core.bean.ComponentProBean;
import com.guttv.pm.core.cache.ComponentCache;
import com.guttv.pm.core.fp.ComponentToZookeeper;
import com.guttv.pm.platform.action.BaseAction;
import com.guttv.pm.platform.action.meta.comparator.ComponentComparator;
import com.guttv.pm.platform.action.meta.search.ComponentFilter;
import com.guttv.pm.utils.JsonUtil;
import com.guttv.pm.utils.Pager;

/**
 * @author Peter
 *
 */
@Controller
@RequestMapping("/component")
public class ComponentAction extends BaseAction {

	@RequestMapping("/list")
	public String list(Pager pager, HttpServletRequest request, HttpServletResponse response) {
		List<ComponentBean> components = ComponentCache.getInstance().getAllComponents();

		// 过滤 即查找
		components = new ComponentFilter().filter(components, pager.getSearchBy(), pager.getKeyword());

		// 排序
		Collections.sort(components, new ComponentComparator(pager.getOrderBy(), pager.getOrder()));

		pager.setTotalCount(components == null ? 0 : components.size());
		pager.setResult(pager(pager,components));

		return "/component/list";
	}

	@RequestMapping("/view")
	public String view(String clz, HttpServletRequest request, HttpServletResponse response) {
		request.setAttribute("com", ComponentCache.getInstance().getComponent(clz));
		return "/component/view";
	}

	@RequestMapping("/beforeUpdateProperties")
	public String beforeUpdateProperties(String clz, HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		ComponentBean com = ComponentCache.getInstance().getComponent(clz);
		if (com == null) {
			request.setAttribute("errMsg", "不存在类名为[" + clz + "]的组件。");
			return "/component/updateProperties";
		}

		request.setAttribute("com", com);

		return "/component/updateProperties";
	}

	/**
	 * 修改组件的属性值
	 * 
	 * @param comPro
	 * @param request
	 * @param response
	 * @return
	 * @throws ServletException
	 */
	@RequestMapping(value = "/updateProperties", method = RequestMethod.POST)
	public String updateProperties(String clz, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		try {
			ComponentBean com = ComponentCache.getInstance().getComponent(clz);
			if (com == null) {
				request.setAttribute("errMsg", "不存在类名为[" + clz + "]的组件。");
				return "/component/updateProperties";
			}

			com.setUpdateTime(new Date());
			request.setAttribute("com", com);

			List<ComponentProBean> componentPros = com.getComponentPros();
			if (componentPros != null && componentPros.size() > 0) {
				boolean changed = false;
				for (ComponentProBean cp : componentPros) {
					// 参数的拼写规则得与页面一致
					String value = request.getParameter(cp.getType() + "_" + cp.getName());
					if (value != null && !value.trim().equals(cp.getValue())) {
						cp.setValue(value.trim());
						cp.setUpdateTime(new Date());
						changed = true;
					}
				}

				if (changed) {
					com.setComponentPros(componentPros);

					// 先同步到zookeeper
					ComponentToZookeeper.persistanceToZookeeper(com);

					// 再缓存本地
					// 不再缓存本地，有监听从ZK上读取
					// ComponentCache.getInstance().cacheComponent(com);

					request.setAttribute("messsage", "修改属性信息已经发到ZK，请稍后刷新");
				} else {
					request.setAttribute("messsage", "没有作任何改动");
				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			request.setAttribute("errMsg", "修改属性错误：" + e.getMessage());
			return "/component/updateProperties";
		}

		request.getRequestDispatcher("/component/list").forward(request, response);
		return null;
	}

	/**
	 * 修改组件的属性值
	 * 
	 * @param com
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/updateStatus", method = RequestMethod.POST)
	@ResponseBody
	public String updateStatus(ComponentBean com, HttpServletRequest request, HttpServletResponse response) {

		Map<String, Object> result = new HashMap<String, Object>();
		try {

			ComponentBean old = ComponentCache.getInstance().getComponent(com.getClz());
			if (old != null) {
				old.setStatus(com.getStatus());
				old.setUpdateTime(new Date());
				// 同步到zookeeper
				ComponentToZookeeper.persistanceToZookeeper(old);
				result.put("status", "success");
				result.put("message", "修改消息已经发出，请稍后刷新列表");
			} else {
				result.put("status", "fail");
				result.put("message", "不存在类为[" + com.getClz() + "]的组件");
			}

			return JsonUtil.toJson(result);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.put("status", "fail");
			result.put("message", e.getMessage());
			return JsonUtil.toJson(result);
		}
	}
}
