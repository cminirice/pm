/**
 * 
 */
package com.guttv.pm.core.zk;

import com.guttv.pm.core.cache.ConfigCache;
import com.guttv.pm.utils.Constants;

/**
 * @author Peter
 *
 */
public class PathConstants {

	/**
	 * 获取统一编码路径
	 */
	public static final String SEQUENCE_PATH = ConfigCache.getInstance().getProperty(Constants.SEQUENCE_PATH,
			"/guttv/pm/sequence");

	/**
	 * 用于基于ZK的RPC消息的请求队列，需要跟执行容器拼接使用
	 */
	public static final String RPC_REQEUST = ConfigCache.getInstance().getProperty(Constants.RPC_REQUEST_PATH,
			"/rpc/request");

	/**
	 * 用于基于ZK的RPC消息的请求反馈队列，需要跟执行容器拼接使用
	 */
	public static final String RPC_RESPONSE = ConfigCache.getInstance().getProperty(Constants.RPC_RESPONSE_PATH,
			"/rpc/response");

	/**
	 * 执行容器上报位置
	 */
	public static final String CONTAINER_ROOT_PATH = ConfigCache.getInstance()
			.getProperty(Constants.CONTAINER_ROOT_PATH, "/guttv/pm/container"); //

	/**
	 * 心跳目录
	 */
	public static final String CONTAINER_HEARTBEAT_PATH = "heartbeat";

	/**
	 * 执行容器正在执行的流程配置相对路径
	 */
	public static final String CONTAINER_EXECFLOW_PATH = "execflow";

	/**
	 * 系统属性的路径
	 */
	public static final String ZOOKEEPER_SYSCONFIG_ROOT_PATH = ConfigCache.getInstance()
			.getProperty(Constants.SYSCONFIG_ROOT_PATH, "/guttv/pm/sysconfig"); // "/guttv/pm/sysconfig";

	/**
	 * 组件的路径
	 */
	public static final String COMPONENT_PATH = ConfigCache.getInstance().getProperty(Constants.COMPONENT_PATH,
			"/guttv/pm/meta/component"); // "/guttv/pm/meta/component/";

	/**
	 * 组件包的路径
	 */
	public static final String COMPONENT_PACK_PATH = ConfigCache.getInstance()
			.getProperty(Constants.COMPONENT_PACK_PATH, "/guttv/pm/meta/compack"); // "/guttv/pm/meta/compack/";

	/**
	 * 流程的路径
	 */
	public static final String FLOW_PATH = ConfigCache.getInstance().getProperty(Constants.FLOW_PATH,
			"/guttv/pm/meta/flow"); // "/guttv/pm/meta/flow/";

	/**
	 * 流程执委配置的路径
	 */
	public static final String FLOW_EXEC_CONFIG_PATH = ConfigCache.getInstance()
			.getProperty(Constants.FLOW_EXEC_CONFIG_PATH, "/guttv/pm/exec/flow"); // "/guttv/pm/exec/flow/";

	/**
	 * 用户信息保存路径
	 */
	public static final String USER_PATH = ConfigCache.getInstance().getProperty(Constants.USER_PATH, "/guttv/pm/user");

	/**
	 * 角色信息保存路径
	 */
	public static final String ROLE_PATH = ConfigCache.getInstance().getProperty(Constants.ROLE_PATH, "/guttv/pm/role");

	/**
	 * 权限信息保存路径
	 */
	public static final String PRIVILEGE_PATH = ConfigCache.getInstance().getProperty(Constants.PRIVILEGE_PATH,
			"/guttv/pm/privilege");


	/**
	 * 服务器配置的路径
	 */
	public static final String SERVER_PATH = ConfigCache.getInstance().getProperty(Constants.SERVER_PATH, "/guttv/pm/server");
	/**
	 * 脚本配置持久化路径
	 */
	public static final String SCRIPT_PATH = ConfigCache.getInstance().getProperty(Constants.SCRIPT_PATH, "/guttv/pm/script");
	/**
	 * 脚本配置持久化路径
	 */
	public static final String SERVER_SCRIPT_MAPPING_PATH = ConfigCache.getInstance().getProperty(Constants.SERVER_SCRIPT_MAPPING_PATH, "/guttv/pm/server_script_mapping");

}
