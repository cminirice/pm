/**
 * 
 */
package com.guttv.pm.core.msg.queue;

import java.io.Closeable;

/**
 * @author Peter
 *
 */
public interface Producer extends Closeable{
	/**
	 * 设置生产者名称
	 * @param name
	 */
	public void setName(String name);
	
	/**
	 * 取得生产者的名称
	 * @return
	 */
	public String getName();
	
	/**
	 * 库存数量
	 * @return
	 */
	public int size();
	
	/**
	 * 向不同的地方生产数据
	 * @param data
	 * @throws Exception
	 */
	public void write(Object data) throws Exception;
	
	/**
	 * 检测data是否符合规则，否则不能调用write方法
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public boolean checkRule(Object data) throws Exception;
	
	public void setDispatchRule(AbstractDispatchRule rule);
	public AbstractDispatchRule getDispatchRule();
}
