/**
 * 
 */
package com.guttv.pm.core.zk.listener;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.guttv.pm.core.bean.ComponentNodeBean;
import com.guttv.pm.core.cache.FlowExecuteConfigCache;
import com.guttv.pm.core.cache.TaskCache;
import com.guttv.pm.core.flow.FlowExecuteConfig;
import com.guttv.pm.core.flow.FlowExecuteEngine;
import com.guttv.pm.core.zk.PathConstants;
import com.guttv.pm.core.zk.ZookeeperHelper;
import com.guttv.pm.utils.Enums.ComponentNodeStatus;
import com.guttv.pm.utils.Enums.FlowExecuteStatus;

/**
 * @author Peter
 *
 */
public class FlowExecConfigListener {

	protected static final Logger logger = LoggerFactory.getLogger(FlowExecConfigListener.class);

	/**
	 * 监听流程配置的变化
	 * 
	 * @param client
	 * @param path
	 */
	@SuppressWarnings("resource")
	public static void watch(final CuratorFramework client) throws Exception {

		String cachePath = ZookeeperHelper.getRealPath(PathConstants.FLOW_EXEC_CONFIG_PATH);

		logger.info("监听流程配置路径：" + cachePath);

		PathChildrenCache pathChildrenCache = new PathChildrenCache(client, cachePath, false);

		try {
			PathChildrenCacheListener childrenCacheListener = new PathChildrenCacheListener() {

				private Gson gson = new Gson();

				@Override
				public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {

					ChildData data = event.getData();

					if (data == null) {
						return;
					}

					String path = data.getPath();

					switch (event.getType()) {

					// 新增结点
					case CHILD_ADDED:
						try {
							FlowExecuteConfig config = ZookeeperHelper.getFromZookeeper(path, client,
									FlowExecuteConfig.class);

							// 这个地方如果以前有，就不再缓存
							FlowExecuteConfig old = FlowExecuteConfigCache.getInstance()
									.getFlowExecuteConfig(config.getFlowExeCode());
							if (old == null) {
								logger.info("添加新流程执行配置：" + gson.toJson(config));

								// 清空以前的执行记录
								config.cleanStatusDesc();

								// 如果需要，启动这个流程
								if (config.getStatus() == FlowExecuteStatus.STARTING
										|| config.getStatus() == FlowExecuteStatus.RUNNING) {

									config.setStatus(FlowExecuteStatus.LOCKED);

									// 下面这是启动
									TaskCache.getInstance().stopTasksByFlowExeCode(config.getFlowExeCode());

									// 启动
									FlowExecuteEngine.excuteFlowExecuteConfig(config);
								} else if (config.getStatus() == FlowExecuteStatus.STOPPED
										|| config.getStatus() == FlowExecuteStatus.PAUSE
										|| config.getStatus() == FlowExecuteStatus.ERROR) {

									config.setStatus(FlowExecuteStatus.FINISH);

									// 修改节点状态
									List<ComponentNodeBean> comNodes = config.getComNodes();
									if (comNodes != null && comNodes.size() > 0) {
										for (ComponentNodeBean comNode : comNodes) {
											comNode.setStatus(ComponentNodeStatus.FINISH);
										}
									}
								}

								FlowExecuteConfigCache.getInstance().cacheFlowExecuteConfig(config);
							} else {
								logger.debug("已经存在流程[" + config.getFlowExeCode() + "]");
							}

						} catch (Exception e) {
							logger.error("新增流程执行配置异常：" + path, e);
						}
						break;
					case CHILD_UPDATED:
						try {
							FlowExecuteConfig config = ZookeeperHelper.getFromZookeeper(path, client,
									FlowExecuteConfig.class);

							logger.info("更新新流程执行配置：" + gson.toJson(config));
							FlowExecuteConfig old = FlowExecuteConfigCache.getInstance().cacheFlowExecuteConfig(config);
							if (old != null) {
								logger.debug("替换旧的流程执行配置[" + old.getFlowExeCode() + "]：" + gson.toJson(old));
								break;
							}
						} catch (Exception e) {
							logger.error("新增流程执行配置异常：" + path, e);
						}
						break;
					case CHILD_REMOVED:
						// 如果路径不是空的，把本地的流程执行配置缓存删除
						if (StringUtils.isNotBlank(path)) {
							try {
								// 取出code
								String code = ZKPaths.getNodeFromPath(path);
								FlowExecuteConfig old = FlowExecuteConfigCache.getInstance()
										.uncacheFlowExecuteConfig(code);

								logger.info("删除流程执行配置[" + code + "]缓存：" + gson.toJson(old));
								if (old != null) {
									FlowExecuteEngine.stopFlowExecuteConfig(old);
								}
							} catch (Exception e) {
								logger.error("处理流程执行配置数据时异常：" + path, e);
							}
						}
						break;
					default:
						break;
					}

				}

			};

			pathChildrenCache.start(StartMode.NORMAL);

			pathChildrenCache.getListenable().addListener(childrenCacheListener);

		} catch (Exception e) {
			logger.error("启动流程执行配置监听异常：" + e.getMessage(), e);
			if (pathChildrenCache != null) {
				pathChildrenCache.close();
			}
			throw e;
		}
	}

}
