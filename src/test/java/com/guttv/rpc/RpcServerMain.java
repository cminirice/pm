/**
 * 
 */
package com.guttv.rpc;

import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;

import com.guttv.pm.core.zk.CuratorClientFactory;
import com.guttv.rpc.common.L;
import com.guttv.rpc.server.accept.CommandAcceptor;
import com.guttv.rpc.server.ack.CommandAck;
import com.guttv.rpc.server.bootstrap.BootstrapCommandAcceptor;
import com.guttv.rpc.server.executor.RpcRequestExecutor;
import com.guttv.rpc.server.executor.SpringContextExecutor;
import com.guttv.rpc.server.zk.ZookeeperCommandAcceptor;
import com.guttv.rpc.server.zk.ZookeeperCommandAck;

/**
 * @author Peter
 *
 */
@EnableAutoConfiguration
@SpringBootApplication
@RestController
@Configuration
@ComponentScan("com.guttv")
public class RpcServerMain {

	public static void main(String[] a) throws Exception {

		ApplicationContext context = SpringApplication.run(RpcServerMain.class, a);

		try {
			acceptZookeeperCommand(context);
			
			bootstrapCommand(context);

		} catch (Throwable error) {
			L.logger.error(error.getMessage(), error);
			System.exit(-1);
		} finally {

		}
	}

	public static void bootstrapCommand(ApplicationContext context) throws Exception {

		RpcRequestExecutor executor = new SpringContextExecutor(context);

		CommandAcceptor acceptor = null;
		try {
			acceptor = new BootstrapCommandAcceptor("127.0.0.1", 9009, executor);
			
			acceptor.accept();
		} catch (Exception e) {
			acceptor.close();
		}
		
	}

	public static void acceptZookeeperCommand(ApplicationContext context) throws Exception {
		final CuratorFramework client = CuratorClientFactory.getClient();
		client.start();

		CommandAck commandAck = new ZookeeperCommandAck(client);
		RpcRequestExecutor executor = new SpringContextExecutor(context);
		final ZookeeperCommandAcceptor acceptor = new ZookeeperCommandAcceptor(client, RpcClientMain.reqPath);
		acceptor.setThreadPool(Executors.newFixedThreadPool(10));
		acceptor.setCommandAck(commandAck);
		acceptor.setExecutor(executor);

		acceptor.accept();

		// 监听结点是不是被删除
		final NodeCache nodeCache = new NodeCache(client, RpcClientMain.reqPath);
		NodeCacheListener nodeCacheListener = new NodeCacheListener() {
			@Override
			public void nodeChanged() throws Exception {
				ChildData childData = nodeCache.getCurrentData();

				if (childData == null) {
					L.logger.warn("监测到RPC命令通道[" + RpcClientMain.reqPath + "]被删除，监听服务将要退出...");
					nodeCache.close();
					IOUtils.closeQuietly(client);
					IOUtils.closeQuietly(acceptor);
				}

			}
		};
		nodeCache.getListenable().addListener(nodeCacheListener);
		nodeCache.start();
	}
}
