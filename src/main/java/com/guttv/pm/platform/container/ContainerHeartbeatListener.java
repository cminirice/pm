package com.guttv.pm.platform.container;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.guttv.pm.core.bean.ExecuteContainer;
import com.guttv.pm.core.bean.Heartbeat;
import com.guttv.pm.core.zk.PathConstants;
import com.guttv.pm.utils.Constants;

public class ContainerHeartbeatListener {

	protected static Logger logger = LoggerFactory.getLogger(ContainerHeartbeatListener.class);

	private static final Map<String, NodeCache> nodeCacheMap = new HashMap<String, NodeCache>();

	public static synchronized void start(final CuratorFramework client, final ExecuteContainer container)
			throws Exception {

		if (nodeCacheMap.containsKey(container.getContainerID())) {
			logger.debug("已经存在[" + container.getContainerID() + "]的心跳监听器.");
			return;
		}

		String cachePath = ZKPaths.makePath(container.getRegistPath(), PathConstants.CONTAINER_HEARTBEAT_PATH);
		final NodeCache nodeCache = new NodeCache(client, cachePath);

		NodeCacheListener nodeCacheListener = new NodeCacheListener() {
			private Gson gson = new Gson();

			@Override
			public void nodeChanged() throws Exception {
				ChildData childData = nodeCache.getCurrentData();

				if (childData == null) {
					return;
				}

				byte[] data = childData.getData();
				if (data == null || (data.length == 1 && data[0] == 0)) {
					// 到这里的数据是创建节点时，发送的默认数据
					return;
				}

				Heartbeat heartbeat = gson.fromJson(new String(data, Constants.ENCODING), Heartbeat.class);
				Heartbeat old = ExecuteContainerCache.getInstance().refreshHeartbeat(container.getContainerID(),
						heartbeat);

				logger.info("更新执行容器[" + container.getContainerID() + "]的心跳信息：" + gson.toJson(heartbeat));
				logger.debug("旧的心跳信息为：" + gson.toJson(old));
			}
		};
		nodeCache.getListenable().addListener(nodeCacheListener);
		nodeCache.start();

		logger.info("开始监听执行容器心跳路径：" + cachePath);
		nodeCacheMap.put(container.getContainerID(), nodeCache);
	}

	public synchronized static void stop(String containerID) {
		NodeCache nodeCache = nodeCacheMap.remove(containerID);
		if (nodeCache != null) {
			try {
				nodeCache.close();
			} catch (IOException e) {
			}
		}
	}
}
