/**
 * 
 */
package com.guttv.rpc.server.zk;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

import com.guttv.pm.core.cache.ConfigCache;
import com.guttv.pm.utils.Constants;
import com.guttv.rpc.common.L;
import com.guttv.rpc.server.accept.CommandAcceptor;
import com.guttv.rpc.server.ack.CommandAck;
import com.guttv.rpc.server.executor.RpcRequestExecutor;

/**
 * 
 * 接收来自zookeeper的命令消息
 * 
 * @author Peter
 *
 */
public class ZookeeperCommandAcceptor implements CommandAcceptor {

	public void accept() throws Exception {
		
		pathChildrenCache = new PathChildrenCache(client, path, false);

		PathChildrenCacheListener childrenCacheListener = new ZookeeperCommandListener(executor, threadPool, commandAck,
				ignoreTimeout);

		pathChildrenCache.start(StartMode.NORMAL);

		pathChildrenCache.getListenable().addListener(childrenCacheListener);

	}

	@Override
	public void close() throws IOException {
		if (pathChildrenCache != null) {
			try {
				L.logger.debug("[" + path + "]路径的RPC命令接收服务关闭");

				pathChildrenCache.close();
				pathChildrenCache = null;
			} catch (Exception e) {
			}
		}
		client = null;
		threadPool = null;
		commandAck = null;
		executor = null;
	}

	// 接收消息用
	private CuratorFramework client = null;
	private String path = null;

	/**
	 * 收到的消息，如果创建时间太长，就不再处理
	 */
	private long ignoreTimeout = ConfigCache.getInstance().getProperty(Constants.RPC_IGNORE_TIMEOUT, 60000);

	public long getIgnoreTimeout() {
		return ignoreTimeout;
	}

	public void setIgnoreTimeout(long ignoreTimeout) {
		this.ignoreTimeout = ignoreTimeout;
	}


	public ZookeeperCommandAcceptor() {
	}

	public ZookeeperCommandAcceptor(CuratorFramework client, String path) {
		this.client = client;
		this.path = path;
	}

	/**
	 * 命令监听
	 */
	private PathChildrenCache pathChildrenCache = null;

	/**
	 * 负责处理请求的线程管理
	 */
	private ExecutorService threadPool = null;
	
	private CommandAck commandAck = null;
	
	private RpcRequestExecutor executor = null;

	public void setCommandAck(CommandAck commandAck) {
		this.commandAck = commandAck;
	}

	public void setExecutor(RpcRequestExecutor executor) {
		this.executor = executor;
	}

	public CuratorFramework getClient() {
		return client;
	}

	public void setClient(CuratorFramework client) {
		this.client = client;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public ExecutorService getThreadPool() {
		return threadPool;
	}

	public void setThreadPool(ExecutorService threadPool) {
		this.threadPool = threadPool;
	}
}
