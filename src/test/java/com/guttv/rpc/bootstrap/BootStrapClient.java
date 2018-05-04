/**
 * 
 */
package com.guttv.rpc.bootstrap;

import com.guttv.pm.core.rpc.Ping;
import com.guttv.rpc.client.RpcProxyFactory;
import com.guttv.rpc.client.bootstrap.BootStrapInvocationHandler;

/**
 * @author Peter
 *
 */
public class BootStrapClient {

	public static void main(String[] a) {

		BootStrapInvocationHandler handler = new BootStrapInvocationHandler("10.2.1.218", 9876, 3000);

		Ping ping = RpcProxyFactory.createInvocationHandlerProxy(handler).create(Ping.class);
		ping.ping();
		System.out.println(ping.sayHello("minimice"));

	}
}
