/**
 * 
 */
package com.guttv.rpc.client;

/**
 * @author Peter
 *
 */
// @Controller
// bean中不能有两个实现，否则按interface查找时就会有异常
public class SecondHelloWorldImpl implements HelloWorld {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.guttv.rpc.client.HelloWorld#sayHello(java.lang.String)
	 */
	@Override
	public String sayHello(String name) {
		return "second say hello to " + name;
	}

}
