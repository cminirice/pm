package com.guttv.pm.platform.action.meta;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.guttv.pm.core.bean.ComponentBean;
import com.guttv.pm.core.bean.ComponentPackageBean;
import com.guttv.pm.core.cache.ComponentPackageCache;
import com.guttv.pm.core.flow.ComPackExistException;
import com.guttv.pm.core.flow.ComPackRegisteManager;
import com.guttv.pm.core.fp.ComponentPackToZookeeper;
import com.guttv.pm.core.fp.ComponentToZookeeper;
import com.guttv.pm.platform.PlatformMain;
import com.guttv.pm.platform.action.BaseAction;
import com.guttv.pm.platform.action.meta.comparator.ComponentPackageComparator;
import com.guttv.pm.platform.action.meta.search.ComponentPackageFilter;
import com.guttv.pm.utils.Constants;
import com.guttv.pm.utils.JsonUtil;
import com.guttv.pm.utils.Pager;

@Controller
@RequestMapping("/comPack")
public class ComponentPackageAction extends BaseAction {

	@RequestMapping("/list")
	public String list(Pager pager, HttpServletRequest request, HttpServletResponse response) {
		List<ComponentPackageBean> comPacks = ComponentPackageCache.getInstance().getAllComPack();

		// 过滤 即查找
		comPacks = new ComponentPackageFilter().filter(comPacks, pager.getSearchBy(), pager.getKeyword());

		// 排序
		Collections.sort(comPacks, new ComponentPackageComparator(pager.getOrderBy(), pager.getOrder()));

		pager.setResult(pager(pager,comPacks));
		pager.setTotalCount(comPacks == null ? 0 : comPacks.size());
		return "/comPack/list";
	}

	@RequestMapping("/view")
	public String view(ComponentPackageBean comPack, HttpServletRequest request, HttpServletResponse response) {
		request.setAttribute("comPack", ComponentPackageCache.getInstance().getComPack(comPack.getComID()));
		return "/comPack/view";
	}

	@RequestMapping(value = "/unRegist", method = RequestMethod.POST)
	@ResponseBody
	public String unRegist(String comID, HttpServletRequest request, HttpServletResponse response) {

		Map<String, Object> result = new HashMap<String, Object>();
		try {
			if (StringUtils.isBlank(comID)) {
				result.put("status", "fail");
				result.put("message", "组件包标识为空");
				return JsonUtil.toJson(result);
			}

			// 获取组件包信息
			ComponentPackageBean comPack = ComponentPackageCache.getInstance().getComPack(comID);
			if (comPack == null) {
				result.put("status", "fail");
				result.put("message", "不存在标识为[" + comID + "]的组件包");
				return JsonUtil.toJson(result);
			}

			// 从zookeeper上删除组件信息
			logger.info("从zookeeper上删除组件信息[" + comID + "]组件信息....");
			ComponentToZookeeper.deleteFromZookeeper(comID);

			// 从zookeeper上删除组件包信息
			logger.info("从zookeeper上删除组件信息[" + comID + "]组件包信息....");
			ComponentPackToZookeeper.deleteFromZookeeper(comID);

			// 卸载
			// 不再在此卸载，从ZK上收到删除消息时再卸载
			// ComPackRegisteManager.getInstance().unRegistComPack(comID);

			result.put("status", "success");
			result.put("message", "卸载消息发送成功，请稍后刷新");
			return JsonUtil.toJson(result);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.put("status", "fail");
			result.put("message", e.getMessage());
			return JsonUtil.toJson(result);
		}
	}

	/**
	 * 因为注册前需要校验，不能放到监听处做，
	 * 
	 * @param comPackFile
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/regist", method = RequestMethod.POST)
	@ResponseBody
	public String regist(MultipartFile comPackFile, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> result = new HashMap<String, Object>();
		File dest = null;
		ComponentPackageBean comPack = null;
		try {
			if (comPackFile == null || comPackFile.isEmpty()) {
				result.put("status", false);
				result.put("message", "请选择文件！");
				return JsonUtil.toJson(result);
			}

			String fileName = comPackFile.getOriginalFilename();

			dest = new File(Constants.TMP_DIR + fileName);
			if (!dest.getParentFile().exists()) { // 判断文件父目录是否存在
				FileUtils.forceMkdir(dest.getParentFile());
			}

			comPackFile.transferTo(dest); // 保存文件

			comPack = new ComponentPackageBean();
			comPack.setComPackageFilePath(dest.getPath());

			// 注册
			ComPackRegisteManager.getInstance().registComPack(comPack, PlatformMain.supportExecuteFlow);

			// 上传组件包 失败需要卸载
			comPack = ComPackRegisteManager.getInstance().uploadAndCreate(comPack, dest);

			List<ComponentBean> coms = comPack.getComponents();
			if (coms != null) {
				for (ComponentBean com : coms) {
					ComponentToZookeeper.persistanceToZookeeper(com);
				}
			}

			// 缓存到zookeeper上 失败需要卸载
			ComponentPackToZookeeper.persistanceToZookeeper(comPack);

			result.put("status", true);

		} catch (ComPackExistException e) {
			// 如果是组件包已经存在的异常，不能卸载。
			result.put("status", false);
			result.put("message", e.getMessage());
		} catch (Exception e) {

			// 出现异常，需要卸载
			if (comPack != null && StringUtils.isNotBlank(comPack.getComID())) {
				try {
					// 出异常时卸载
					ComPackRegisteManager.getInstance().unRegistComPack(comPack.getComID());
				} catch (Exception e1) {
					logger.error("卸载组件包[" + comPack.getComPackageFilePath() + "][" + comPack.getComID() + "]时出现异常："
							+ e.getMessage(), e);
				}
			}

			logger.error(e.getMessage(), e);
			result.put("status", false);
			result.put("message", e.getMessage());
		} finally {
			if (dest != null) {
				FileUtils.deleteQuietly(dest);
			}
		}

		return JsonUtil.toJson(result);
	}

}
