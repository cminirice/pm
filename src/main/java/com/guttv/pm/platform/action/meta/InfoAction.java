/**
 * 
 */
package com.guttv.pm.platform.action.meta;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.guttv.pm.core.cache.ComponentCache;
import com.guttv.pm.core.cache.ComponentPackageCache;
import com.guttv.pm.core.cache.FlowCache;
import com.guttv.pm.core.cache.FlowExecuteConfigCache;
import com.guttv.pm.core.cache.TaskCache;
import com.guttv.pm.platform.action.BaseAction;
import com.guttv.pm.utils.JsonUtil;
import com.guttv.pm.utils.Utils;

/**
 * @author Peter
 *
 */
@Controller
@RequestMapping("/info")
public class InfoAction extends BaseAction {

	@RequestMapping(value = "/meta", method = RequestMethod.POST)
	@ResponseBody
	public String metaInfo() {
		Map<String, Object> result = new HashMap<String, Object>();

		// 组件包数量
		result.put("comPack", ComponentPackageCache.getInstance().getAllComPack().size());

		// 组件
		result.put("com", ComponentCache.getInstance().getAllComponents().size());

		// 流程数
		result.put("flow", FlowCache.getInstance().getAllFlows().size());

		// 执行配置
		result.put("flowExecConfig", FlowExecuteConfigCache.getInstance().getAllFlowExecuteConfigs().size());

		// 任务数
		result.put("tasks", TaskCache.getInstance().getAllTasks().size());

		return JsonUtil.toJson(result);
	}

	@RequestMapping(value = "/platform", method = RequestMethod.POST)
	@ResponseBody
	public String platformInfo() {
		Map<String, Object> result = new HashMap<String, Object>();

		// 内存信息
		result.put("memoryInfo", Utils.getMemoryInfo());

		// 线程数
		result.put("threadNumbers", Utils.getAllThread().length);

		// 进程
		result.put("pid", System.getProperty("PID"));

		// 平台目录
		result.put("dir", Utils.getProjectLocation());

		// 运行时长
		long time = ManagementFactory.getRuntimeMXBean().getUptime();
		
		
		result.put("runTime", Utils.getBeautifulTimeLengh(time));

		return JsonUtil.toJson(result);
	}
}
