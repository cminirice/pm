/**
 * 
 */
package com.guttv.pm.core.zk.listener;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.guttv.pm.core.bean.ComponentBean;
import com.guttv.pm.core.cache.ComponentCache;
import com.guttv.pm.core.zk.PathConstants;
import com.guttv.pm.core.zk.ZookeeperHelper;
import com.guttv.pm.utils.Constants;

/**
 * @author Peter
 *
 */
public class ComponentListener {
	protected static final Logger logger = LoggerFactory.getLogger(ComponentListener.class);

	/**
	 * 监听组件的变化
	 * 
	 * @param client
	 * @throws Exception
	 */
	@SuppressWarnings("resource")
	public static void watch(final CuratorFramework client) throws Exception {

		String cachePath = ZookeeperHelper.getRealPath(PathConstants.COMPONENT_PATH);

		logger.info("监听组件路径：" + cachePath);

		TreeCache treeCache = new TreeCache(client, cachePath);
		try {
			TreeCacheListener treeCacheListener = new TreeCacheListener() {
				private Gson gson = new Gson();

				@Override
				public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
					ChildData data = event.getData();
					if (data == null) {
						return;
					}

					String path = data.getPath();
					byte[] bytes = data.getData();
					if (bytes == null || bytes.length == 0 || (bytes.length == 1 && bytes[0] == 0)) {
						return;
					}

					switch (event.getType()) {
					case NODE_ADDED:
						// 从注册的入口新增
						logger.debug("暂不支持组件的新增：" + path);
						break;
					case NODE_UPDATED:
						// 更新结点

						try {

							ComponentBean com = gson.fromJson(new String(bytes, Constants.ENCODING),
									ComponentBean.class);

							logger.info("更新组件[" + com.getClz() + "]信息...");
							ComponentCache.getInstance().cacheComponent(com);

						} catch (Exception e) {
							logger.error("重新注册组件包异常：" + path, e);
						}
						break;
					case NODE_REMOVED:
						// 组件是在组件包卸载时删除的
						logger.debug("暂不支持组件的删除：" + path);
						break;
					default:
						break;
					}
				}
			};
			treeCache.getListenable().addListener(treeCacheListener);
			treeCache.start();
		} catch (Exception e) {
			logger.error("启动组件监听异常：" + e.getMessage(), e);
			if (treeCache != null) {
				treeCache.close();
			}
			throw e;
		}

	}
}
