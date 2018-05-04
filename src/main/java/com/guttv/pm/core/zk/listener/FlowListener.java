/**
 * 
 */
package com.guttv.pm.core.zk.listener;

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
import com.guttv.pm.core.bean.FlowBean;
import com.guttv.pm.core.cache.FlowCache;
import com.guttv.pm.core.zk.PathConstants;
import com.guttv.pm.core.zk.ZookeeperHelper;

/**
 * @author Peter
 *
 */
public class FlowListener {

	protected static final Logger logger = LoggerFactory.getLogger(FlowListener.class);

	/**
	 * 监听流程的变化
	 * 
	 * @param client
	 * @param path
	 */
	@SuppressWarnings("resource")
	public static void watch(final CuratorFramework client) throws Exception{

		String cachePath = ZookeeperHelper.getRealPath(PathConstants.FLOW_PATH);

		logger.info("监听流程路径：" + cachePath);

		PathChildrenCache pathChildrenCache = null;

		try {
			pathChildrenCache = new PathChildrenCache(client, cachePath, false);
			
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
							FlowBean flow = ZookeeperHelper.getFromZookeeper(path, client, FlowBean.class);

							logger.info("添加新流程：" + gson.toJson(flow));
							FlowBean old = FlowCache.getInstance().cacheFlow(flow);
							if (old != null) {
								logger.debug("替换旧的流程[" + old.getCode() + "]：" + gson.toJson(old));
								break;
							}
						} catch (Exception e) {
							logger.error("新增流程异常：" + path, e);
						}
						break;
					case CHILD_UPDATED:
						try {
							FlowBean flow = ZookeeperHelper.getFromZookeeper(path, client, FlowBean.class);

							logger.info("更新新流程：" + gson.toJson(flow));
							FlowBean old = FlowCache.getInstance().cacheFlow(flow);
							if (old != null) {
								logger.debug("替换旧流程信息[" + old.getCode() + "]：" + gson.toJson(old));
								break;
							}
						} catch (Exception e) {
							logger.error("更新流程异常：" + path, e);
						}
						break;
					case CHILD_REMOVED:
						// 如果路径不是空的，把本地的流程缓存删除
						if (StringUtils.isNotBlank(path)) {
							try {
								// 取出code
								String code = ZKPaths.getNodeFromPath(path);
								FlowBean old = FlowCache.getInstance().uncacheFlow(code);
								logger.info("删除流程[" + code + "]缓存：" + gson.toJson(old));
							} catch (Exception e) {
								logger.error("处理流程数据时异常：" + path, e);
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
			logger.error("启动流程监听异常：" + e.getMessage(), e);
			if (pathChildrenCache != null) {
				pathChildrenCache.close();
			}
			throw e;
		}
	}

}
