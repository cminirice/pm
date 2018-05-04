
下面是RMI框架的实用示例
两种实现方式：
1：基于zookeeper消息的传递
2：基于bootstrap消息的传弟
3：该功能可以单独打包使用，在修改RPC时，不要引用 com.guttv.rpc 以前的任何自开发类

server端均是基于spring的执行实现
如果服务端有其它方式实现可以继承 RpcRequestExecutor 接口


依赖包

		<dependency>
			<groupId>org.apache.curator</groupId>
			<artifactId>curator-recipes</artifactId>
			<version>2.7.1</version>
		</dependency>

		<!-- https://mvnrepository.com/artifact/org.apache.curator/curator-framework -->
		<dependency>
			<groupId>org.apache.curator</groupId>
			<artifactId>curator-framework</artifactId>
			<version>4.0.0</version>
		</dependency>
		<dependency>
			<groupId>com.google.code.gson</groupId>
			<artifactId>gson</artifactId>
			<version>2.8.2</version>
		</dependency>
		<dependency>
			<groupId>cglib</groupId>
			<artifactId>cglib</artifactId>
			<version>3.2.5</version>
		</dependency>

		<!-- https://mvnrepository.com/artifact/org.objenesis/objenesis -->
		<dependency>
			<groupId>org.objenesis</groupId>
			<artifactId>objenesis</artifactId>
			<version>2.6</version>
		</dependency>

		<!-- https://mvnrepository.com/artifact/org.apache.commons/commons-collections4 -->
		<dependency>
			<groupId>org.apache.commons</groupId>
			<artifactId>commons-collections4</artifactId>
			<version>4.1</version>
		</dependency>

		<!-- https://mvnrepository.com/artifact/com.dyuproject.protostuff/protostuff-core -->
		<dependency>
			<groupId>com.dyuproject.protostuff</groupId>
			<artifactId>protostuff-core</artifactId>
			<version>1.1.3</version>
		</dependency>

		<!-- https://mvnrepository.com/artifact/com.dyuproject.protostuff/protostuff-runtime -->
		<dependency>
			<groupId>com.dyuproject.protostuff</groupId>
			<artifactId>protostuff-runtime</artifactId>
			<version>1.1.3</version>
		</dependency>

		<!-- https://mvnrepository.com/artifact/io.netty/netty-all -->
		<dependency>
			<groupId>io.netty</groupId>
			<artifactId>netty-all</artifactId>
			<version>4.1.17.Final</version>
		</dependency>




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

import com.guttv.pm.common.zk.CuratorClientFactory;
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
@ComponentScan("com.guttv.rpc.client")
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





/**
 * 
 */
package com.guttv.rpc;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;

import com.guttv.pm.common.zk.CuratorClientFactory;
import com.guttv.rpc.client.HelloWorld;
import com.guttv.rpc.client.RpcProxyFactory;
import com.guttv.rpc.client.bootstrap.BootStrapInvocationHandler;
import com.guttv.rpc.client.zk.ZookeeperInvocationHandler;

/**
 * @author Peter
 *
 */
@EnableAutoConfiguration
@SpringBootApplication
@RestController
@Configuration
public class RpcClientMain {

	public static final String reqPath = "/dev/guttv/command/request";
	public static final String resPath = "/dev/guttv/command/response";
	public static ApplicationContext context;

	public static void main(String[] a) throws Exception {
		context = SpringApplication.run(RpcClientMain.class, a);

		invokeBootstrapRpc();
		
		invokeZookeeperRpc();
		
		invokeLocal();
	}

	public static void invokeLocal() {
		HelloWorld hello = RpcProxyFactory.createSpringContextProxy(context).create(HelloWorld.class);
		System.out.println(hello.sayHello("peter"));
	}

	public static void invokeZookeeperRpc() {
		CuratorFramework client = CuratorClientFactory.getClient();

		client.start();

		ZookeeperInvocationHandler handler = new ZookeeperInvocationHandler();
		handler.setClient(client);
		handler.setRequestRootPath(reqPath);
		handler.setResponseRootPath(resPath);
		handler.setTimeout(100000);

		final HelloWorld hello1 = RpcProxyFactory.createInvocationHandlerProxy(handler).create(HelloWorld.class);
		final AtomicInteger atomicNum = new AtomicInteger(1);
		for (int i = 0; i < 15; i++) {
			// System.out.println(hello1.sayHello("minimice"+atomicNum.getAndIncrement()));
			new Thread() {
				public void run() {
					String name = "zookeeper " + atomicNum.getAndIncrement() + " ";
					System.out.println(name + hello1.sayHello(name));
				}
			}.start();

		}
	}

	public static void invokeBootstrapRpc() {
		BootStrapInvocationHandler handler = new BootStrapInvocationHandler("127.0.0.1", 9009);
		
		final HelloWorld hello = RpcProxyFactory.createInvocationHandlerProxy(handler).create(HelloWorld.class);
		
		final AtomicInteger atomicNum = new AtomicInteger(1);
		for (int i = 0; i < 15; i++) {
			// System.out.println(hello1.sayHello("minimice"+atomicNum.getAndIncrement()));
			new Thread() {
				public void run() {
					String name = "bootstrap " + atomicNum.getAndIncrement() + " ";
					System.out.println(name + hello.sayHello(name));
				}
			}.start();

		}
	}
}




package com.guttv.rpc.client;

public interface HelloWorld {

	/**
	 * @param name
	 * @return
	 */
	public String sayHello(String name);
}




/**
 * 
 */
package com.guttv.rpc.client;

import org.springframework.stereotype.Controller;

/**
 * @author Peter
 *
 */
@Controller
public class HelloWorldImpl implements HelloWorld{

	@Override
	public String sayHello(String name) {
		return "minimice say hello to " + name;
	}

}









