/**
 * 
 */
package com.guttv.pm.utils;

/**
 * @author Peter
 *
 */
public class Constants {

	// 工程用的编码
	public static final String ENCODING = "UTF-8";

	public static final String PM_HOME = "PM_HOME";
	
	//组件配置文件的匹配规则
	public static final String COMPONENT_CONFIG_PATTERN = ".*com.xml";

	// 组件默认的组
	public static final String DEFAULT_COMPONENT_GROUP = "default";

	// 配置文件
	public static final String CONFIG = "/conf.properties";

	// 组件包的临时存放目录
	public static final String TMP_DIR = System.getProperty("java.io.tmpdir") + "/com/";

	// 用户输入用户名和密码错误的次数
	public static final String INPUT_WRONG_COUNT = "input_wrong_count";
	// 存在session中的是否启用验证码的标识
	public static final String CHECKKAPTCHA = "checkKaptcha";
	// 系统活动会话数在上下文中的主键
	public static final String ACTIVE_SESSION_NUMBER = "activeSessionNum";
	// 当前登陆用户信息保存在Session中的key
	public static final String CURRENT_USER = "currentUser";
	// 重定向的路径
	public static final String LOGINURL = "/login";
	public static final String MAIN_URL = "/view/main.jsp";

	// 下面是springboot配置文件需要配置的key

	// 组件存放的FTP服务器地址
	public static final String COM_FTP_SERVER = "com.ftp.server";

	// zookeeper连接串主键
	public final static String connectionStringKey = "zookeeper_connectionstring";

	// 线程池大小
	public static final String THREAD_POOL_MAX_SIZE = "thread_pool_max_size";

	// rabbitMQ 地址 主键
	public static String RABBIT_ADDR = "rabbit_addr";

	public static final String USER_NAME_CONFIG = "admin_username";
	public static final String USER_PWD_CONFIG = "admin_password";
	// 最大登陆失败次数，超过该值后，启用验证码
	public static final String MAX_LOGIN_ERROR_TIME = "max.login.error.time";

	// 执行容器启动的RPC端口范围
	public static final String CONTAINER_RPC_PORT = "container.rpc.port";
	public static final String SHUTDOWN_HOOK_TIMEOUT = "shutdown.hook.timeout";
	// 启动服务的端口
	public static final String SERVER_PORT = "server.port";
	// 执行容在server.port不能用的情况下，从该范围中找一个可用的启动端口
	public static final String SERVER_PORT_SCOPE = "server.port.scope";
	// 上面配置的端口范围的分隔符
	public static final String PORT_SCOPE_SEP = "port.scope.seperator";
	// 服务端是否要启动执行流程功能
	public static final String SERVER_START_EXECUTE_SUPPORT = "server.start.execute.support";
	// 服务端平台是否是单机运行
	public static final String SERVER_START_SINGLESERVER = "server.start.singleServer";
	// 单机运行标识保存在ServletContext 中有主键
	public static final String SERVLETCONTEXT_SINGLESERVER_KEY = "singleServer";
	// 客户端在RPC调用时的超时时间
	public static final String RPC_TIMEOUT = "rpc.client.timeout";
	// 服务器端丢弃请求的超时时间
	public static final String RPC_IGNORE_TIMEOUT = "rpc.server.ignore.timeout";
	// RPC服务器端开启处理客户端请求的线程数
	public static final String RPC_THREADPOOL_SIZE = "rpc.server.threadpool.size";
	// 监控流程配置执行状态的周期 监控周期
	public static final String SERVER_FLOWEXECUTE_MONITOR_PERIOD = "server.flowexecute.monitor.period";
	// 系统启动时，标记系统位置的环境变量主键
	public static final String SERVER_LOCATION_ENV_KEY = "sever.location.env";
	// 执行容器ping接口的路径
	public static final String CONTAINER_PING_PATH = "container.ping.path";
	// 发布后平台的webapp目录
	public static final String PLATFORM_WEBAPP_DIR = "platform.webapp.dir";
	// 容器心跳超时时，需要发送的邮件
	public static final String CONTAINER_TIMEOUT_SENDMAIL = "container.timeout.sendmail";
	// 执行容器上报心跳的周期
	public static final String HEARTBEAT_PERIOD = "container.heartbeat.period";
	// 执行容器超过多少个周期没有心跳，服务器则认为执行容器心跳超时
	public static final String HEARTBEAT_LOSS_COUNT = "container.heartbeat.timeout.period";
	// 检查执行容状态的循环周期
	public static final String CONTAINER_CHECKTIMEOUT_PERIOD = "container.checktimeout.period";

	// 下面是springboot配置中需要配置的zookeeper路径的主键值

	// zookeeper路径前缀
	public static final String ZOOKEEPER_PRE_PATH = "zookeeper_pre_path";
	// 分布式序列号路径
	public static final String SEQUENCE_PATH = "zookeeper.sequence.path";
	// RPC请求的路径，只是后缀，用的时候，还是会放在执行容器路径后面
	public static final String RPC_REQUEST_PATH = "rpc.request.path";
	public static final String RPC_RESPONSE_PATH = "rpc.response.path";
	// 执行容器注册的路径
	public static final String CONTAINER_ROOT_PATH = "zookeeper.container.root.path";
	// 系统属性的根路径
	public static final String SYSCONFIG_ROOT_PATH = "zookeeper.sysconfig.root.path";
	// 组件的路径
	public static final String COMPONENT_PATH = "zookeeper.component.path";
	// 组件包的路径
	public static final String COMPONENT_PACK_PATH = "zookeeper.component.pack.path";
	// 流程的路径
	public static final String FLOW_PATH = "zookeeper.flow.path";
	// 流程执行配置的路径
	public static final String FLOW_EXEC_CONFIG_PATH = "zookeeper.flow.exec.config.path";
	// 用户信息路径
	public static final String USER_PATH = "zookeeper.users.path";
	// 角色信息路径
	public static final String ROLE_PATH = "zookeeper.roles.path";
	// 权限信息路径
	public static final String PRIVILEGE_PATH = "zookeeper.privileges.path";

	public static final String SERVER_PATH="zookeeper.server.path";

	public static final String SCRIPT_PATH="zookeeper.script.path";

	public static final String SERVER_SCRIPT_MAPPING_PATH="zookeeper.server_script_mapping.path";

}
