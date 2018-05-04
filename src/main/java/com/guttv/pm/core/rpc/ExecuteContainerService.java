/**
 * 
 */
package com.guttv.pm.core.rpc;

import java.util.Map;

import com.guttv.pm.core.bean.ExecuteContainer;

/**
 * @author Peter
 *
 */
public interface ExecuteContainerService {

	/**
	 * 更新组件信息
	 * 
	 * @param container
	 */
	public void update(ExecuteContainer container) throws Exception;

	/**
	 * 关闭执行容器
	 * 
	 * @throws Exception
	 */
	public void shutdown() throws Exception;

	/**
	 * 禁用该执行容器
	 * 
	 * @throws Exception
	 */
	public void forbbiden() throws Exception;

	/**
	 * 启用该执行容器
	 * 
	 * @throws Exception
	 */
	public void start() throws Exception;

	/**
	 * 重新加载springboot的配置文件
	 * 
	 * @throws Exception
	 */
	public void refreshSpringConfig() throws Exception;

	/**
	 * 获取springboot的配置文件信息
	 * 
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getSpringConfig() throws Exception;

}
