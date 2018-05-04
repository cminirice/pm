/**
 * 
 */
package com.guttv.pm.core.msg.queue;

import java.io.Closeable;

/**
 * @author Peter
 *
 */
public interface Queue extends Closeable{

	/**
	 * 生产数据接口
	 * @param data
	 * @throws Exception
	 */
	public abstract void produce(Object data) throws Exception;
	
	/**
	 * 处理数据 
	 * @return
	 * @throws Exception
	 */
	public abstract Object consumer() throws Exception;
	
	/**
	 * 读取数据，如果没有数据，等待超时返回空
	 * @param millisecond
	 * @return
	 * @throws Exception
	 */
	public abstract Object consumer(int timeout) throws Exception;
	
	/**
	 * 取得队列的深度
	 * @return
	 */
	public abstract int size();
	
}
