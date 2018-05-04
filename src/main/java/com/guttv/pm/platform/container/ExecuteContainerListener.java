/**
 * 
 */
package com.guttv.pm.platform.container;

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
import com.guttv.pm.core.bean.ExecuteContainer;
import com.guttv.pm.core.zk.PathConstants;
import com.guttv.pm.core.zk.ZookeeperHelper;

/**
 * @author Peter
 *
 */
public class ExecuteContainerListener {
	protected static final Logger logger = LoggerFactory.getLogger(ExecuteContainerListener.class);

	/**
	 * 开启执行容器监听
	 * 
	 * @param client
	 * @param path
	 */
	@SuppressWarnings("resource")
	public static void start(final CuratorFramework client) throws Exception {

		String cachePath = ZookeeperHelper.getRealPath(PathConstants.CONTAINER_ROOT_PATH);

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

							// 取下来信息
							ExecuteContainer container = ZookeeperHelper.getFromZookeeper(path, client,
									ExecuteContainer.class);

							if (container == null) {
								return;
							}

							logger.info("新增执行容器[" + container.getContainerID() + "]信息：" + gson.toJson(container));

							ExecuteContainerCache.getInstance().cacheExecuteContainer(container);

							ContainerHeartbeatListener.start(client, container);

						} catch (Exception e) {
							logger.error("新增执行容器异常：" + path, e);
						}
						break;
					case CHILD_UPDATED:
						// 更新结点
						try {
							// 取下来信息
							ExecuteContainer container = ZookeeperHelper.getFromZookeeper(path, client,
									ExecuteContainer.class);

							logger.info("更新执行容器[" + container.getContainerID() + "]信息：");
							ExecuteContainerCache.getInstance().cacheExecuteContainer(container);

							// 偿试重新监听
							ContainerHeartbeatListener.start(client, container);

						} catch (Exception e) {
							logger.error("更新执行容器异常：" + path, e);
						}
						break;
					case CHILD_REMOVED:
						// 如果路径不是空的，删除本地的执行容器
						try {
							if (StringUtils.isNotBlank(path)) {
								// 取出COMID
								String containerID = ZKPaths.getNodeFromPath(path).toUpperCase();
								ExecuteContainer old = ExecuteContainerCache.getInstance()
										.unCacheExecuteContainer(containerID);
								logger.info("删除执行容器[" + containerID + "]缓存：" + (old == null ? "" : gson.toJson(old)));

								ContainerHeartbeatListener.stop(containerID);
							}
						} catch (Exception e) {
							logger.error("从缓存中删除执行容器异常：" + path, e);
						}
						break;
					default:
						break;
					}

				}

			};

			pathChildrenCache.start(StartMode.NORMAL);

			pathChildrenCache.getListenable().addListener(childrenCacheListener);

			logger.info("监听执行容器路径：" + cachePath);

		} catch (Exception e) {
			logger.error("启动组件包监听异常：" + e.getMessage(), e);
			if (pathChildrenCache != null) {
				pathChildrenCache.close();
			}
			throw e;
		}

	}
}
