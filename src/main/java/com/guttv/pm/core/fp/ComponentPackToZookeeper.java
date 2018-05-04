/**
 * 
 */
package com.guttv.pm.core.fp;

import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.guttv.pm.core.bean.ComponentPackageBean;
import com.guttv.pm.core.cache.ComponentPackageCache;
import com.guttv.pm.core.flow.ComPackExistException;
import com.guttv.pm.core.flow.ComPackRegisteManager;
import com.guttv.pm.core.zk.CuratorClientFactory;
import com.guttv.pm.core.zk.PathConstants;
import com.guttv.pm.core.zk.ZookeeperHelper;
import com.guttv.pm.utils.JsonUtil;

/**
 * @author Peter
 *
 */
public class ComponentPackToZookeeper {

	private static Logger logger = LoggerFactory.getLogger(ComponentPackToZookeeper.class);

	/**
	 * 把组件包信息持久化到zookeeper上。如果已经有，更新；没有，创建。
	 * 
	 * @param comPack
	 * @throws Exception
	 */
	public static void persistanceToZookeeper(ComponentPackageBean comPack) throws Exception {
		if (StringUtils.isBlank(comPack.getComID())) {
			return;
		}
		//
		String path = ZKPaths.makePath(PathConstants.COMPONENT_PACK_PATH, comPack.getComID());

		path = ZookeeperHelper.getRealPath(path);

		CuratorFramework client = CuratorClientFactory.getClient();
		try {
			client.start();

			ZookeeperHelper.putToZookeeper(path, client, comPack);

			logger.info("ComponentPackageBean数据保存到zookeeper路径" + path + "上：" + JsonUtil.toJson(comPack));
		} finally {
			if (client != null) {
				IOUtils.closeQuietly(client);
			}
		}
	}

	/**
	 * 从ZK上删除组件包信息
	 * 
	 * @param comPack
	 * @throws Exception
	 */
	public static void deleteFromZookeeper(String comID) throws Exception {

		if (StringUtils.isBlank(comID)) {
			return;
		}

		//
		String path = ZKPaths.makePath(PathConstants.COMPONENT_PACK_PATH, comID);

		path = ZookeeperHelper.getRealPath(path);

		CuratorFramework client = CuratorClientFactory.getClient();
		try {
			client.start();

			ZookeeperHelper.deleteFromZookeeper(path, client);

			logger.info("删除组件包信息，zookeeper上路径：" + path);
		} finally {
			if (client != null) {
				IOUtils.closeQuietly(client);
			}
		}
	}

	/**
	 * 本方法认为是系统启动时，从服务器上同步数据到本地，本方法判断了本地是否存在组件包，发现本地有组件，直接卸载
	 * 读取zookeeper上的信息，并缓存到本地
	 */
	public static void cacheFromZookeeper(boolean cacheClassLoader) throws Exception {
		//
		String path = PathConstants.COMPONENT_PACK_PATH;

		path = ZookeeperHelper.getRealPath(path);

		// zookeeper客户端
		CuratorFramework client = CuratorClientFactory.getClient();
		client.start();

		try {
			List<String> comIDs = null;
			try {
				comIDs = client.getChildren().forPath(path);
			} catch (NoNodeException e1) {
				logger.warn("没有任何组件包信息。");
				return;
			}
			if (comIDs != null && comIDs.size() > 0) {
				ComponentPackageBean comPack = null;
				ComponentPackageBean old = null;
				String p = null;
				for (String comID : comIDs) {
					p = ZKPaths.makePath(path, comID);
					try {
						comPack = ZookeeperHelper.getFromZookeeper(p, client, ComponentPackageBean.class);

						old = ComponentPackageCache.getInstance().getComPack(comPack.getComID());
						if (old != null) {
							logger.warn("发现本地有组件包，准备卸载后重新注册：" + JsonUtil.toJson(old));
							ComPackRegisteManager.getInstance().unRegistComPack(comPack.getComID());
						}

						ComPackRegisteManager.getInstance().registComPack(comPack, cacheClassLoader);
						logger.info("从zk上缓存数据到本地：" + JsonUtil.toJson(comPack));
						if (old != null) {
							logger.info("替换掉旧数据为：" + JsonUtil.toJson(old));
						}
					} catch (Exception e) {
						logger.error("从zk服务器读取数据异常，路径：" + p, e);
					}
				}
			}
		} finally {
			if (client != null) {
				IOUtils.closeQuietly(client);
			}
		}
	}

	/**
	 * 
	 * @param comIDs
	 * @param cacheClassLoader
	 * @throws Exception
	 */
	public static void cacheFromZookeeper(Set<String> comIDs, boolean cacheClassLoader) throws Exception {
		if (comIDs == null || comIDs.size() == 0) {
			return;
		}

		comIDs.removeAll(ComponentPackageCache.getInstance().getAllComIDs());
		if (comIDs.size() == 0) {
			return;
		}

		//
		String path = PathConstants.COMPONENT_PACK_PATH;

		path = ZookeeperHelper.getRealPath(path);

		// zookeeper客户端
		CuratorFramework client = CuratorClientFactory.getClient();
		client.start();

		try {

			ComponentPackageBean comPack = null;
			ComponentPackageBean old = null;
			String p = null;
			for (String comID : comIDs) {
				p = ZKPaths.makePath(path, comID);
				try {
					comPack = ZookeeperHelper.getFromZookeeper(p, client, ComponentPackageBean.class);

					old = ComponentPackageCache.getInstance().getComPack(comPack.getComID());
					if (old != null) {
						logger.debug("本地已经存在组件包[" + comID + "]");
						continue;
					}

					ComPackRegisteManager.getInstance().registComPack(comPack, cacheClassLoader);
					logger.info("从zk上缓存数据到本地：" + JsonUtil.toJson(comPack));
				} catch (ComPackExistException e) {
					logger.warn("注册时发现已经存在组件包[" + comID + "]：" + e.getMessage());
				}
			}

		} finally {
			if (client != null) {
				IOUtils.closeQuietly(client);
			}
		}

	}

}
