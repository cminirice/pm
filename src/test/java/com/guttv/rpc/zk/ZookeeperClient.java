/**
 * 
 */
package com.guttv.rpc.zk;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.curator.framework.CuratorFramework;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import com.guttv.pm.core.cache.ConfigCache;
import com.guttv.pm.core.rpc.Ping;
import com.guttv.pm.core.zk.CuratorClientFactory;
import com.guttv.rpc.RpcClientMain;
import com.guttv.rpc.RpcServerMain;
import com.guttv.rpc.client.RpcProxyFactory;
import com.guttv.rpc.client.zk.ZkWithNorespInvocHandler;

/**
 * @author Peter
 *
 */
@SpringBootApplication
public class ZookeeperClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ApplicationContext context = SpringApplication.run(RpcServerMain.class, args);
		ConfigCache.init(context);

		CuratorFramework client = CuratorClientFactory.getClient();

		client.start();

		/*ZookeeperInvocationHandler handler = new ZookeeperInvocationHandler();
		handler.setClient(client);
		handler.setRequestRootPath(RpcClientMain.reqPath);
		handler.setResponseRootPath(RpcClientMain.resPath);
		handler.setTimeout(100000);*/
		
		
		ZkWithNorespInvocHandler handler = new ZkWithNorespInvocHandler();
		handler.setClient(client);
		handler.setRequestRootPath(RpcClientMain.reqPath);
		//handler.setRequestRootPath("/dev/guttv/pm/container/c4e2b6da19402eb50f7bccce41eb5bbe/rpc/request");
		handler.setTimeout(100000);

		final Ping hello = RpcProxyFactory.createInvocationHandlerProxy(handler).create(Ping.class);
		final AtomicInteger atomicNum = new AtomicInteger(1);
		for (int i = 0; i < 15; i++) {
			new Thread() {
				public void run() {
					String name = "zookeeper " + atomicNum.getAndIncrement() + " ";
					System.out.println(name + hello.sayHello(name));
				}
			}.start();
		}
	

	}

}
