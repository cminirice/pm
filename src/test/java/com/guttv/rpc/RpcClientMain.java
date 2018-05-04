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

import com.guttv.pm.core.zk.CuratorClientFactory;
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
		
		//invokeZookeeperRpc();
		
		//invokeLocal();
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
		BootStrapInvocationHandler handler = new BootStrapInvocationHandler("127.0.0.1", 9876);
		
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
