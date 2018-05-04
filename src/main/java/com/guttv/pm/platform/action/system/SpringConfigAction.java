/**
 * 
 */
package com.guttv.pm.platform.action.system;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.guttv.pm.core.cache.ConfigCache;
import com.guttv.pm.platform.action.BaseAction;
import com.guttv.pm.utils.JsonUtil;

/**
 * @author Peter
 *
 */
@Controller
@RequestMapping("/springconfig")
public class SpringConfigAction extends BaseAction {

	@RequestMapping("/view")
	public String view(HttpServletRequest request, HttpServletResponse response) {
		try {
			request.setAttribute("properties", ConfigCache.getInstance().getSpringConfigMap());
		} catch (Exception e) {
			request.setAttribute("errMsg", "读取配置文件异常：" + e.getMessage());
			logger.error("获取配置信息异常：" + e.getMessage(), e);
		}
		return "/springconfig/view";
	}

	@RequestMapping(value = "/refresh", method = RequestMethod.POST)
	@ResponseBody
	public String refresh(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> result = new HashMap<String, Object>();
		try {
			ConfigCache.getInstance().refresh();
			result.put("status", "success");
			result.put("message", "更新成功");
			return JsonUtil.toJson(result);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.put("status", "fail");
			result.put("message", e.getMessage());
			return JsonUtil.toJson(result);
		}
	}
}
