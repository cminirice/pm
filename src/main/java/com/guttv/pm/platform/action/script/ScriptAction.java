package com.guttv.pm.platform.action.script;

import com.guttv.pm.core.bean.ScriptBean;
import com.guttv.pm.core.fp.ScriptToZookeeper;
import com.guttv.pm.platform.action.BaseAction;
import com.guttv.pm.platform.action.script.comparator.ScriptComparator;
import com.guttv.pm.platform.action.script.execute.RmtShellExecutorBuilder;
import com.guttv.pm.platform.action.script.search.ScriptFilter;
import com.guttv.pm.utils.JsonUtil;
import com.guttv.pm.utils.Pager;
import com.guttv.pm.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
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
 * @create 2018-02-06 16:11
 **/
@Controller
@RequestMapping("/script")
public class ScriptAction extends BaseAction {

	@RequestMapping("/list")
	public String list(Pager pager, HttpServletRequest request, HttpServletResponse response) {
		List<ScriptBean> list = null;
		try {
			list = ScriptToZookeeper.getListFromZookeeper();

			// 过滤 即查找
			list = new ScriptFilter().filter(list, pager.getSearchBy(), pager.getKeyword());
			// 排序
			if (list != null && list.size() > 0) {
				Collections.sort(list, new ScriptComparator(pager.getOrderBy(), pager.getOrder()));
			}

			pager.setTotalCount(list == null ? 0 : list.size());
			pager.setResult(pager(pager, list));
			request.setAttribute("pager", pager);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return "/script/list";
	}

	@GetMapping("/detail")
	public ModelAndView detail(@RequestParam("code") String code) {
		ModelAndView modelAndView = new ModelAndView("/script/detail");
		if (!"0".equals(code) && !"".equals(code)) {
			try {
				ScriptBean entity = ScriptToZookeeper.getFromZookeeper(code);
				modelAndView.addObject("entity", entity);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return modelAndView;
	}

	@PostMapping("/saveOrUpdate")
	@ResponseBody
	public String add(ScriptBean bean) {
		Map<String, Object> data = new HashMap<>(8);
		if ("".equals(Utils.getString(bean.getCode()))) {
			bean.setCode(Utils.getUUID());
			bean.setStatus(0);
			bean.setCreateTime(new Date());
		}
		bean.setUpdateTime(new Date());
		bean.setStatus(Utils.getInt(bean.getStatus(), 0));
		if (!bean.getFilePath().endsWith("/")) {
			bean.setFilePath(bean.getFilePath() + "/");
		}
		try {
			ScriptToZookeeper.persistanceToZookeeper(bean);
			data.put("status", true);
			data.put("message", "success");
		} catch (Exception e) {
			logger.error("saveOrUpdate数据异常：" + e.getMessage(), e);
			data.put("status", false);
			data.put("message", e.getMessage());
		}
		return JsonUtil.toJson(data);
	}

	@GetMapping("/copy")
	@ResponseBody
	public String copy(@RequestParam("code") String code) {
		Map<String, Object> data = new HashMap<>(8);
		try {
			ScriptBean oldBean = ScriptToZookeeper.getFromZookeeper(code);
			ScriptBean newBean = new ScriptBean();
			newBean.setStatus(0);
			newBean.setFilePath(oldBean.getFilePath());
			newBean.setUpdateTime(new Date());
			newBean.setCode(Utils.getUUID());
			newBean.setFileName(oldBean.getFileName());
			newBean.setRemoteTarget(oldBean.getRemoteTarget());
			newBean.setDecompressionCMD(oldBean.getDecompressionCMD());
			newBean.setMd5(oldBean.getMd5());
			newBean.setShCMD(oldBean.getShCMD());
			newBean.setShutdown(oldBean.getShutdown());
			newBean.setCreateTime(new Date());
			ScriptToZookeeper.persistanceToZookeeper(newBean);
			data.put("status", true);
			data.put("message", "success");
		} catch (Exception e) {
			logger.error("copy数据异常：" + e.getMessage(), e);
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
			ScriptToZookeeper.deleteFromZookeeper(code);
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

	@GetMapping("/execute")
	@ResponseBody
	public String execute(@RequestParam("code") String code) {
		Map<String, Object> data = new HashMap<>(8);
		logger.info("脚本准备执行");
		rmtShellExecutorBuilder.executeAsync(code);
		data.put("status", true);
		data.put("message", "执行中，稍后刷新列表");
		return "success";
	}

	@GetMapping("/shutdown")
	@ResponseBody
	public String shutdown(@RequestParam("code") String code) {
		Map<String, Object> data = new HashMap<>(8);
		logger.info("脚本准备停止");
		rmtShellExecutorBuilder.shutdown(code);
		data.put("status", true);
		data.put("message", "停止中，稍后刷新列表");
		return "success";
	}

	@Autowired
	RmtShellExecutorBuilder rmtShellExecutorBuilder;



	@InitBinder
	protected void init(HttpServletRequest request, ServletRequestDataBinder binder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateFormat.setLenient(false);
		binder.registerCustomEditor(java.util.Date.class, new CustomDateEditor(dateFormat, false));
	}
}
