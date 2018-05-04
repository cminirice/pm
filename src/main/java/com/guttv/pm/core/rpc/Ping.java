package com.guttv.pm.core.rpc;

public interface Ping {

	/**
	 * 只是ping一下，没有异常表示连通
	 */
	public void ping();
	
	/**
	 * 测试字符串
	 * @param name
	 * @return
	 */
	public String sayHello(String name);
}
