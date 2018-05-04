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

import com.guttv.pm.core.bean.FlowBean;
import com.guttv.pm.core.cache.FlowCache;
import com.guttv.pm.core.zk.CuratorClientFactory;
import com.guttv.pm.core.zk.PathConstants;
import com.guttv.pm.core.zk.ZookeeperHelper;
import com.guttv.pm.utils.JsonUtil;

/**
 * @author Peter
 *
 */
public class FlowToZookeeper {

	private static Logger logger = LoggerFactory.getLogger(FlowToZookeeper.class);

	/**
	 * 把流程信息持久化到zookeeper上。如果已经有，更新；没有，创建。
	 * 
	 * @param flow
	 * @throws Exception
	 */
	public static void persistanceToZookeeper(FlowBean flow) throws Exception {
		if (StringUtils.isBlank(flow.getCode())) {
			return;
		}

		//
		String path = ZKPaths.makePath(PathConstants.FLOW_PATH, flow.getCode());

		path = ZookeeperHelper.getRealPath(path);

		CuratorFramework client = CuratorClientFactory.getClient();
		try {
			client.start();

			ZookeeperHelper.putToZookeeper(path, client, flow);

			logger.info("FlowBean数据保存到zookeeper路径" + path + "上：" + JsonUtil.toJson(flow));
		} finally {
			if (client != null) {
				IOUtils.closeQuietly(client);
			}
		}
	}

	public static void deleteFromZookeeper(String flowCode) throws Exception {
		if (StringUtils.isBlank(flowCode)) {
			return;
		}
		//
		String path = ZKPaths.makePath(PathConstants.FLOW_PATH, flowCode);

		path = ZookeeperHelper.getRealPath(path);

		CuratorFramework client = CuratorClientFactory.getClient();
		try {
			client.start();

			ZookeeperHelper.deleteFromZookeeper(path, client);

			logger.info("删除zookeeper上路径：" + path);
		} finally {
			if (client != null) {
				IOUtils.closeQuietly(client);
			}
		}
	}

	/**
	 * 从ZK上删除流程信息
	 * 
	 * @param flow
	 * @throws Exception
	 */
	public static void deleteFromZookeeper(FlowBean flow) throws Exception {
		if (StringUtils.isBlank(flow.getCode())) {
			return;
		}
		//
		String path = ZKPaths.makePath(PathConstants.FLOW_PATH, flow.getCode());

		path = ZookeeperHelper.getRealPath(path);

		CuratorFramework client = CuratorClientFactory.getClient();
		try {
			client.start();

			ZookeeperHelper.deleteFromZookeeper(path, client);

			logger.info("删除zookeeper上路径：" + path + ".内容：" + JsonUtil.toJson(flow));
		} finally {
			if (client != null) {
				IOUtils.closeQuietly(client);
			}
		}
	}

	/**
	 * 读取zookeeper上的信息，并缓存到本地
	 */
	public static void cacheFromZookeeper() throws Exception {
		//
		String path = PathConstants.FLOW_PATH;

		path = ZookeeperHelper.getRealPath(path);

		// zookeeper客户端
		CuratorFramework client = CuratorClientFactory.getClient();
		client.start();

		try {

			List<String> flowCodes = null;
			try {
				flowCodes = client.getChildren().forPath(path);
			} catch (NoNodeException e1) {
				logger.warn("没有任何流信息。");
				return;
			}

			if (flowCodes != null && flowCodes.size() > 0) {
				FlowBean flow = null;
				FlowBean old = null;
				String p = null;
				for (String code : flowCodes) {
					p = path + "/" + code;
					try {
						flow = ZookeeperHelper.getFromZookeeper(p, client, FlowBean.class);

						old = FlowCache.getInstance().cacheFlow(flow);
						logger.info("从zk上缓存数据到本地：" + JsonUtil.toJson(flow));
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

}
