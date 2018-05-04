/**
 * 
 */
package com.guttv.pm.core.rpc;

import java.util.List;

import com.guttv.pm.core.flow.FlowExecuteConfig;

/**
 * @author Peter
 *
 */
public interface FlowExecuteConfigService {

	/**
	 * 获取容器中的流程执行配置信息
	 * 
	 * @param flowExeCode
	 * @return
	 * @throws Exception
	 */
	public FlowExecuteConfig getFlowExecuteConfig(String flowExeCode) throws Exception;

	/**
	 * 获取 所有的流程执行配置信息
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<FlowExecuteConfig> getAllFlowExecuteConfigs() throws Exception;

	/**
	 * 获取容器所有的流程执行配置编码
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<String> getAllFlowExecConfCodes() throws Exception;

	/**
	 * 向执行容器添加执行流程
	 * 
	 * @param flowExeCodes
	 *            流程执行编码
	 * @return 添加的结果信息
	 * @throws Exception
	 */
	public String addFlowExecConfigs(String[] flowExeCodes) throws Exception;

	/**
	 * 停止流程执行配置的运行
	 * 
	 * @param flowExeCode
	 * @throws Exception
	 */
	public void stopFlowExecConfig(String flowExeCode) throws Exception;

	/**
	 * 启动流程执行配置
	 * 
	 * @param flowExeCode
	 * @throws Exception
	 */
	public void startFlowExecConfig(String flowExeCode) throws Exception;

	/**
	 * 初始化流程执行配置，会关闭一下
	 * 
	 * @param flowExeCode
	 * @throws Exception
	 */
	public void initFlowExecConfig(String flowExeCode) throws Exception;

	/**
	 * 删除流程执行配置，需要把ZK上的也删除同时停止
	 * 
	 * @param flowExeCode
	 */
	public void deleteFlowExeConfig(String flowExeCode) throws Exception;

	/**
	 * 继续执行节点
	 * 
	 * @param flowExeCode
	 * @param nodeID
	 */
	public void taskGo(String flowExeCode, String nodeID) throws Exception;

	/**
	 * 暂停执行节点
	 * 
	 * @param flowExeCode
	 * @param nodeID
	 * @throws Exception
	 */
	public void taskPause(String flowExeCode, String nodeID) throws Exception;

	/**
	 * 获取节点的任务堆栈
	 * 
	 * @param flowExeCode
	 * @param nodeID
	 * @return
	 * @throws Exception
	 */
	public String taskStacktrace(String flowExeCode, String nodeID) throws Exception;
}
