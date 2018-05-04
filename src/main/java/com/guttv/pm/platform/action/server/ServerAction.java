package com.guttv.pm.platform.action.server;

import com.guttv.pm.core.bean.ServerBean;
import com.guttv.pm.core.fp.ServerToZookeeper;
import com.guttv.pm.platform.action.BaseAction;
import com.guttv.pm.platform.action.server.comparator.ServerComparator;
import com.guttv.pm.platform.action.server.search.ServerFilter;
import com.guttv.pm.utils.JsonUtil;
import com.guttv.pm.utils.Pager;
import com.guttv.pm.utils.Utils;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author donghongchen
 * @create 2018-01-26 11:14
 **/
@Controller
@RequestMapping("/server")
public class ServerAction extends BaseAction {

	@RequestMapping("/list")
	public String list(Pager pager, HttpServletRequest request, HttpServletResponse response) {
		List<ServerBean> list = null;
		try {
			list = ServerToZookeeper.getListFromZookeeper();

			// 过滤 即查找
			list = new ServerFilter().filter(list, pager.getSearchBy(), pager.getKeyword());
			if (list != null && list.size() > 0) {
				// 排序
				Collections.sort(list, new ServerComparator(pager.getOrderBy(), pager.getOrder()));
			}

			pager.setTotalCount(list == null ? 0 : list.size());
			pager.setResult(pager(pager, list));
			request.setAttribute("pager", pager);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return "/server/list";
	}

	@GetMapping("/detail")
	public ModelAndView add(@RequestParam("code") String code) {
		ModelAndView modelAndView = new ModelAndView("/server/detail");
		if (!"0".equals(code) && !"".equals(code)) {
			try {
				ServerBean entity = ServerToZookeeper.getFromZookeeper(code);
				modelAndView.addObject("entity", entity);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return modelAndView;
	}

	@PostMapping("/saveOrUpdate")
	@ResponseBody
	public String add(ServerBean bean, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> data = new HashMap<>(8);
		if ("".equals(Utils.getString(bean.getCode()))) {
			bean.setCode(Utils.getUUID());
			bean.setCreateTime(new Date());
		}
		bean.setUpdateTime(new Date());
		try {
			ServerToZookeeper.persistanceToZookeeper(bean);
			data.put("status", true);
			data.put("message", "sccess");
		} catch (Exception e) {
			logger.error("saveOrUpdate数据异常：" + e.getMessage(), e);
			data.put("status", false);
			data.put("message", e.getMessage());
		}
		return JsonUtil.toJson(data);
	}

	@PostMapping("/delete")
	@ResponseBody
	public String delete(String code) {
		Map<String, Object> data = new HashMap<>(8);
		try {
			ServerToZookeeper.deleteFromZookeeper(code);
		} catch (Exception e) {
			logger.error("删除zookeeper上异常：" + e.getMessage(), e);
			data.put("status", false);
			data.put("message", e.getMessage());
			return JsonUtil.toJson(data);
		}
		data.put("status", true);
		data.put("message", "已经从ZK上删除，稍后刷新列表");
		return JsonUtil.toJson(data);
	}


	@InitBinder
	protected void init(HttpServletRequest request, ServletRequestDataBinder binder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateFormat.setLenient(false);
		binder.registerCustomEditor(java.util.Date.class, new CustomDateEditor(dateFormat, false));
	}
}
