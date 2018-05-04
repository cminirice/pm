package com.guttv.pm.container.onstart;

import java.util.concurrent.ExecutorService;

import org.apache.commons.io.IOUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.springframework.context.ApplicationContext;

import com.guttv.rpc.common.L;
import com.guttv.rpc.server.ack.CommandAck;
import com.guttv.rpc.server.executor.RpcRequestExecutor;
import com.guttv.rpc.server.executor.SpringContextExecutor;
import com.guttv.rpc.server.zk.ZookeeperCommandAcceptor;
import com.guttv.rpc.server.zk.ZookeeperCommandAck;

public class StartZookeeperRpcAcceptor {

	/**
	 * 启动zookeeper类型的RPC
	 * 
	 * @param context
	 * @param client
	 * @throws Exception
	 */
	public static void start(ApplicationContext context, final CuratorFramework client, final String path,
			ExecutorService threadPool) throws Exception {

		CommandAck commandAck = new ZookeeperCommandAck(client);
		// CommandAck commandAck = new DonothingCommandAck();

		RpcRequestExecutor executor = new SpringContextExecutor(context);
		final ZookeeperCommandAcceptor acceptor = new ZookeeperCommandAcceptor(client, path);
		acceptor.setThreadPool(threadPool);
		acceptor.setCommandAck(commandAck);
		acceptor.setExecutor(executor);

		acceptor.accept();

		// 监听结点是不是被删除
		final NodeCache nodeCache = new NodeCache(client, path);
		NodeCacheListener nodeCacheListener = new NodeCacheListener() {
			@Override
			public void nodeChanged() throws Exception {
				ChildData childData = nodeCache.getCurrentData();

				if (childData == null) {
					L.logger.warn("监测到RPC命令通道[" + path + "]被删除，监听服务将要退出...");
					nodeCache.close();
					IOUtils.closeQuietly(acceptor);
				}
			}
		};
		nodeCache.getListenable().addListener(nodeCacheListener);
		nodeCache.start();
	}
}
