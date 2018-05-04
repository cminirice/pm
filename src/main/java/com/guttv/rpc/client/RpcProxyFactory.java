/**
 * 
 */
package com.guttv.rpc.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.springframework.context.ApplicationContext;

/**
 * @author Peter
 *
 */
public class RpcProxyFactory {

	
	/**
	 * 按执行句柄生成代理类
	 * @param h
	 * @return
	 */
	public static RpcProxy createInvocationHandlerProxy(final InvocationHandler h) {
		return new RpcProxy() {
			private InvocationHandler handler = h;

		/**
		 * 这个方法里面只能传interface类型的数据，不能传类
		 */
			@SuppressWarnings("unchecked")
			public <T> T create(Class<T> interfaceClass) {
				return ((T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[] { interfaceClass }, handler));
			}
		};
	}

	/**
	 * 调用spring bean执行
	 * 
	 * @return
	 */
	public static RpcProxy createSpringContextProxy(final ApplicationContext ctxt) {
		return new RpcProxy() {
			private ApplicationContext context = ctxt;
			public <T> T create(Class<T> clz) {
				return context.getBean(clz);
			}
		};
	}
}
