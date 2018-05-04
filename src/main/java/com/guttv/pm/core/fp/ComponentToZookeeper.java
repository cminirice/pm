/**
 * 
 */
package com.guttv.pm.core.fp;

import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.guttv.pm.core.bean.ComponentBean;
import com.guttv.pm.core.cache.ComponentCache;
import com.guttv.pm.core.zk.CuratorClientFactory;
import com.guttv.pm.core.zk.PathConstants;
import com.guttv.pm.core.zk.ZookeeperHelper;
import com.guttv.pm.utils.JsonUtil;

/**
 * @author Peter
 *
 */
public class ComponentToZookeeper {
	private static Logger logger = LoggerFactory.getLogger(ComponentToZookeeper.class);

	/**
	 * 把组件信息持久化到zookeeper上。如果已经有，更新；没有，创建。
	 * 
	 * @param com
	 * @throws Exception
	 */
	public static void persistanceToZookeeper(ComponentBean com) throws Exception {
		if (StringUtils.isBlank(com.getComID()) || StringUtils.isBlank(com.getClz())) {
			return;
		}
		//
		String path = ZKPaths.makePath(PathConstants.COMPONENT_PATH, com.getComID(), com.getClz().replace(".", "_"));

		path = ZookeeperHelper.getRealPath(path);

		CuratorFramework client = CuratorClientFactory.getClient();
		try {
			client.start();

			ZookeeperHelper.putToZookeeper(path, client, com);

			logger.info("ComponentBean数据保存到zookeeper路径" + path + "上：" + JsonUtil.toJson(com));
		} finally {
			if (client != null) {
				IOUtils.closeQuietly(client);
			}
		}
	}

	/**
	 * 
	 * @param id
	 *            comID
	 * @throws Exception
	 */
	public static void deleteFromZookeeper(String comID) throws Exception {
		if (StringUtils.isBlank(comID)) {
			return;
		}

		String path = ZKPaths.makePath(PathConstants.COMPONENT_PATH, comID);

		path = ZookeeperHelper.getRealPath(path);

		CuratorFramework client = CuratorClientFactory.getClient();
		client.start();

		try {
			List<String> comIDs = null;
			try {
				comIDs = client.getChildren().forPath(path);
			} catch (NoNodeException e1) {
				logger.warn("没有任何组件信息。");
				return;
			}

			if (comIDs != null && comIDs.size() > 0) {
				// 循环删除所有的组件
				String p = null;
				for (String id : comIDs) {
					try {
						p = path + "/" + id;
						try {
							logger.info("删除路径：" + p);
							ZookeeperHelper.deleteFromZookeeper(p, client);
						} catch (Exception e) {
							logger.error("删除数据异常，路径：" + p, e);
						}
					} catch (Exception e) {
						logger.error("从zk服务器读取数据异常，路径：" + p, e);
					}
				}
			}

			// 再删除根节点
			logger.info("删除路径：" + path);
			ZookeeperHelper.deleteFromZookeeper(path, client);
		} finally {
			if (client != null) {
				IOUtils.closeQuietly(client);
			}
		}

	}

	/**
	 * 从ZK上删除组件信息
	 * 
	 * @param com
	 * @throws Exception
	 */
	public static void deleteFromZookeeper(ComponentBean com) throws Exception {
		if (StringUtils.isBlank(com.getComID()) || StringUtils.isBlank(com.getClz())) {
			return;
		}

		//
		String path = ZKPaths.makePath(PathConstants.COMPONENT_PATH, com.getComID(), com.getClz().replace(".", "_"));

		path = ZookeeperHelper.getRealPath(path);

		CuratorFramework client = CuratorClientFactory.getClient();
		try {
			client.start();

			ZookeeperHelper.deleteFromZookeeper(path, client);

			logger.info("删除zookeeper上路径：" + path + ".内容：" + JsonUtil.toJson(com));
		} finally {
			if (client != null) {
				IOUtils.closeQuietly(client);
			}
		}
	}

	/**
	 * 系统启动的时候，需要先下载组件包信息并且注册上，
	 * 运行此方法是，系统认为所有的组件包已经全部经过注册，所以在执行此方法时，应该会打印“替换旧数据”类似字样。
	 * 读取zookeeper上的信息，并缓存到本地
	 */
	public static void cacheFromZookeeper() throws Exception {
		//
		String path = PathConstants.COMPONENT_PATH;

		path = ZookeeperHelper.getRealPath(path);

		// zookeeper客户端
		CuratorFramework client = CuratorClientFactory.getClient();
		client.start();

		try {
			List<String> comIDs = null;
			try {
				comIDs = client.getChildren().forPath(path);
			} catch (NoNodeException e1) {
				logger.warn("没有任何组件信息。");
				return;
			}

			if (comIDs != null && comIDs.size() > 0) {
				ComponentBean com = null;
				ComponentBean old = null;
				String p = null;
				for (String comID : comIDs) {
					try {
						p = path + "/" + comID;
						List<String> clzs = client.getChildren().forPath(p);
						if (clzs != null && clzs.size() > 0) {
							for (String pp : clzs) {
								String comPath = p + "/" + pp;

								try {
									com = ZookeeperHelper.getFromZookeeper(comPath, client, ComponentBean.class);

									old = ComponentCache.getInstance().cacheComponent(com);
									logger.info("从zk上缓存数据到本地：" + JsonUtil.toJson(com));
									if (old != null) {
										logger.info("替换掉旧数据为：" + JsonUtil.toJson(old));
									}
								} catch (Exception e) {
									logger.error("从zk服务器读取数据异常，路径：" + comPath, e);
								}
							}

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
}
