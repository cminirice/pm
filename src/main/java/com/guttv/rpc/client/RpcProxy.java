/**
 * 
 */
package com.guttv.rpc.client;

/**
 * @author Peter
 *
 */
public interface RpcProxy {
	
	/**
	 * 按接口返回一个类
	 * @param interfaceClass
	 * @return
	 */
	public <T> T create(Class<T> interfaceClass);
	
}
