package com.guttv.pm.core.fp;

import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.guttv.pm.utils.Enums.ComponentNodeStatus;
import com.guttv.pm.utils.Enums.FlowExecuteStatus;
import com.guttv.pm.core.bean.ComponentNodeBean;
import com.guttv.pm.core.cache.FlowExecuteConfigCache;
import com.guttv.pm.core.cache.TaskCache;
import com.guttv.pm.core.flow.FlowExecuteConfig;
import com.guttv.pm.core.flow.FlowExecuteEngine;
import com.guttv.pm.core.zk.CuratorClientFactory;
import com.guttv.pm.core.zk.PathConstants;
import com.guttv.pm.core.zk.ZookeeperHelper;
import com.guttv.pm.utils.JsonUtil;

public class FlowExecConfigToZookeeper {

	private static Logger logger = LoggerFactory.getLogger(FlowExecConfigToZookeeper.class);

	/**
	 * 把流程执行配置信息持久化到zookeeper上。如果已经有，更新；没有，创建。
	 * 
	 * @param config
	 * @throws Exception
	 */
	public static void persistanceToZookeeper(FlowExecuteConfig config) throws Exception {
		if (StringUtils.isBlank(config.getFlowExeCode())) {
			return;
		}
		//
		String path = ZKPaths.makePath(PathConstants.FLOW_EXEC_CONFIG_PATH, config.getFlowExeCode());

		path = ZookeeperHelper.getRealPath(path);

		CuratorFramework client = CuratorClientFactory.getClient();
		try {
			client.start();

			ZookeeperHelper.putToZookeeper(path, client, config);

			logger.info("FlowExecuteConfig数据保存到zookeeper路径" + path + "上：" + JsonUtil.toJson(config));
		} finally {
			if (client != null) {
				IOUtils.closeQuietly(client);
			}
		}
	}

	public static void deleteFromZookeeper(String flowExeCode) throws Exception {
		if (StringUtils.isBlank(flowExeCode)) {
			return;
		}

		//
		String path = ZKPaths.makePath(PathConstants.FLOW_EXEC_CONFIG_PATH, flowExeCode);

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
	 * 从ZK上删除流程执行配置信息
	 * 
	 * @param config
	 * @throws Exception
	 */
	public static void deleteFromZookeeper(FlowExecuteConfig config) throws Exception {
		if (StringUtils.isBlank(config.getFlowExeCode())) {
			return;
		}

		//
		String path = ZKPaths.makePath(PathConstants.FLOW_EXEC_CONFIG_PATH, config.getFlowExeCode());

		path = ZookeeperHelper.getRealPath(path);

		CuratorFramework client = CuratorClientFactory.getClient();
		try {
			client.start();

			ZookeeperHelper.deleteFromZookeeper(path, client);

			logger.info("删除zookeeper上路径：" + path + ".内容：" + JsonUtil.toJson(config));
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
		String path = PathConstants.FLOW_EXEC_CONFIG_PATH;

		path = ZookeeperHelper.getRealPath(path);

		// zookeeper客户端
		CuratorFramework client = CuratorClientFactory.getClient();
		client.start();

		try {

			List<String> flowExecCodes = null;
			try {
				flowExecCodes = client.getChildren().forPath(path);
			} catch (NoNodeException e1) {
				logger.warn("没有任何流程执行配置信息。");
				return;
			}

			if (flowExecCodes != null && flowExecCodes.size() > 0) {
				FlowExecuteConfig config = null;
				FlowExecuteConfig old = null;
				String p = null;
				for (String flowExecCode : flowExecCodes) {
					p = path + "/" + flowExecCode;
					try {
						config = ZookeeperHelper.getFromZookeeper(p, client, FlowExecuteConfig.class);
						
						// 清空以前的执行记录
						config.cleanStatusDesc();

						old = FlowExecuteConfigCache.getInstance().cacheFlowExecuteConfig(config);

						logger.info("从zk上缓存数据到本地：" + JsonUtil.toJson(config));
						if (old != null) {
							logger.info("替换掉旧数据为：" + JsonUtil.toJson(old));
						}

						// 如果需要，启动这个流程
						if (config.getStatus() == FlowExecuteStatus.STARTING
								|| config.getStatus() == FlowExecuteStatus.RUNNING) {

							config.setStatus(FlowExecuteStatus.LOCKED);

							// 下面这是启动
							TaskCache.getInstance().stopTasksByFlowExeCode(config.getFlowExeCode());
							// 清空以前的执行记录
							config.cleanStatusDesc();
							// 启动
							FlowExecuteEngine.excuteFlowExecuteConfig(config);
						} else if (config.getStatus() == FlowExecuteStatus.STOPPED
								|| config.getStatus() == FlowExecuteStatus.PAUSE
								|| config.getStatus() == FlowExecuteStatus.ERROR) {

							config.setStatus(FlowExecuteStatus.FINISH);
							// 清空以前的执行记录
							config.cleanStatusDesc();
							// 修改节点状态
							List<ComponentNodeBean> comNodes = config.getComNodes();
							if (comNodes != null && comNodes.size() > 0) {
								for (ComponentNodeBean comNode : comNodes) {
									comNode.setStatus(ComponentNodeStatus.FINISH);
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
