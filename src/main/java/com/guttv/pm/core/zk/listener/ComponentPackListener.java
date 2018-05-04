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

import com.guttv.pm.core.bean.ComponentPackageBean;
import com.guttv.pm.core.cache.ComponentPackageCache;
import com.guttv.pm.core.flow.ComPackRegisteManager;
import com.guttv.pm.core.zk.PathConstants;
import com.guttv.pm.core.zk.ZookeeperHelper;

/**
 * 
 * @author Peter
 *
 */
@SuppressWarnings("resource")
public class ComponentPackListener {
	protected static final Logger logger = LoggerFactory.getLogger(ComponentPackListener.class);

	/**
	 * 监听组件包的变化 平台上不注册，必须得在上传前注册，需要校验有效性
	 * 
	 * @param client
	 * @param deleteJar
	 *            卸载时是否删除FTP上的文件，平台上删除，执行容器不删除
	 * @param cacheClassLoader
	 *            是否缓存classLoader,如果仅启动平台，不需要缓存类加载器
	 * @param registIfNotExist
	 *            是否是本地不存在的话就注册，执行容器在执行流程配置用到组件包的时候再下载注册
	 * 
	 */
	public static void watch(final CuratorFramework client, final boolean deleteJar, final boolean cacheClassLoader,
			final boolean registIfNotExist) throws Exception {

		String cachePath = ZookeeperHelper.getRealPath(PathConstants.COMPONENT_PACK_PATH);

		logger.info("监听组件包路径：" + cachePath);

		PathChildrenCache pathChildrenCache = new PathChildrenCache(client, cachePath, false);

		try {
			PathChildrenCacheListener childrenCacheListener = new PathChildrenCacheListener() {

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
							ComponentPackageBean comPack = ZookeeperHelper.getFromZookeeper(path, client,
									ComponentPackageBean.class);

							logger.debug("发现新的组件包[" + comPack.getComID() + "]");

							ComponentPackageBean old = ComponentPackageCache.getInstance()
									.getComPack(comPack.getComID());
							if (old != null) {
								logger.debug("本地已经存在[" + comPack.getComID() + "]组件：" + path);
								break;
							} else {
								if (!registIfNotExist) {
									// 配置了不存在也不注册
									logger.debug("配置了不存在也不注册，丢弃组件[" + comPack.getComID() + "]");
									break;
								}
								// 如果本地有的话，就不再重新注册了
								logger.info("注册组件包[" + comPack + "]：" + path);
								ComPackRegisteManager.getInstance().registComPack(comPack, cacheClassLoader);
							}
						} catch (Exception e) {
							logger.error("注册组件包异常：" + path, e);
						}
						break;
					case CHILD_UPDATED:
						// 更新结点
						logger.debug("暂不支持组件包的更新：" + path);
						break;
					case CHILD_REMOVED:
						// 如果路径不是空的，把本地的组件包卸载
						if (StringUtils.isNotBlank(path)) {
							// 取出COMID
							String comID = ZKPaths.getNodeFromPath(path);
							try {
								// 出异常时卸载
								ComPackRegisteManager.getInstance().unRegistComPack(comID, deleteJar);
							} catch (Exception e) {
								logger.error("卸载组件包[" + comID + "]时出现异常：" + e.getMessage(), e);
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
			logger.error("启动组件包监听异常：" + e.getMessage(), e);
			if (pathChildrenCache != null) {
				pathChildrenCache.close();
			}
			throw e;
		}
	}
}
