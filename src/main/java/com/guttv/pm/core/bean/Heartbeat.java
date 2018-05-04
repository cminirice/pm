/**
 * 
 */
package com.guttv.pm.core.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Peter
 *
 */
public class Heartbeat {

	private String heartbeatTime = null;
	
	private String containerID = null;
	
	private String containerIP = null;

	private final Map<String, String> info = new HashMap<String, String>();

	public String getHeartbeatTime() {
		return heartbeatTime;
	}

	public void setHeartbeatTime(String heartbeatTime) {
		this.heartbeatTime = heartbeatTime;
	}

	public String getContainerID() {
		return containerID;
	}

	public void setContainerID(String containerID) {
		this.containerID = containerID;
	}

	public String getContainerIP() {
		return containerIP;
	}

	public void setContainerIP(String containerIP) {
		this.containerIP = containerIP;
	}

	public Map<String, String> getInfo() {
		return info;
	}

	public boolean contain(String key) {
		return info.containsKey(key);
	}

	public void put(String key, String value) {
		info.put(key, value);
	}

	/**
	 * 异常信息
	 * 
	 * @param msg
	 */
	public void putHeartbeatError(String msg) {
		put(HEARTBEAT_ERROR, msg);
	}

	/**
	 * 内存信息
	 * 
	 * @param memoryInfo
	 */
	public void putMemoryInfo(String memoryInfo) {
		put(MEMORY_INFO, memoryInfo);
	}

	/**
	 * 线程数
	 * 
	 * @param threadNum
	 */
	public void putThreadNum(int threadNum) {
		put(THREAD_NUMBER, Integer.toString(threadNum));
	}

	/**
	 * 组件数
	 * 
	 * @param count
	 */
	public void putComponentCount(int count) {
		put(COMPONENT_COUNT, Integer.toString(count));
	}

	/**
	 * 组件包数
	 * 
	 * @param count
	 */
	public void putComponentPackCount(int count) {
		put(COMPONENTPACK_COUNT, Integer.toString(count));
	}

	/**
	 * 流程配置数
	 * 
	 * @param count
	 */
	public void putFlowExecuteConfigCount(int count) {
		put(FLOWEXECUTECONFIG_COUNT, Integer.toString(count));
	}

	/**
	 * 正在执行的流程数
	 * 
	 * @param count
	 */
	public void putExecutedFlowCount(int count) {
		put(EXECUTED_FLOW_COUNT, Integer.toString(count));
	}

	/**
	 * 任务数
	 * 
	 * @param count
	 */
	public void putTasksCount(int count) {
		put(TASKS_COUNT, Integer.toString(count));
	}

	public static final String HEARTBEAT_ERROR = "heartbeat_error";
	public static final String MEMORY_INFO = "memory_info";
	public static final String THREAD_NUMBER = "thread_number";
	public static final String COMPONENT_COUNT = "component_count";
	public static final String COMPONENTPACK_COUNT = "componentpack_count";
	public static final String FLOWEXECUTECONFIG_COUNT = "flowexecuteconfig_count";
	public static final String EXECUTED_FLOW_COUNT = "executed_flow_count";
	public static final String TASKS_COUNT = "tasks_count";

}
