/**
 * 
 */
package com.guttv.pm.container.onstart;

import java.util.concurrent.Executors;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StopWatch;

import com.guttv.pm.container.ContainerFlowExecConfCache;
import com.guttv.pm.core.cache.ConfigCache;
import com.guttv.pm.core.zk.PathConstants;
import com.guttv.pm.utils.Constants;
import com.guttv.pm.utils.Utils;
import com.guttv.rpc.common.FindAvailablePort;

/**
 * @author Peter
 *
 */
public class StartExecuteSupport {

	/**
	 * 启动支持执行流程的功能
	 * 
	 * @throws Exception
	 */
	public static void start(ApplicationContext context, CuratorFramework client, int serverPort, StopWatch stopWatch,
			Logger logger) throws Exception {
		String str = ConfigCache.getInstance().getProperty(Constants.CONTAINER_RPC_PORT, null);
		int[] scope = Utils.getPortScope(str);
		int rpcServerPort = FindAvailablePort.find(scope[0], scope[1]);
		if (rpcServerPort > 1000) {
			try {
				logger.info("将要启动bootstrap类型的RPC服务 with port [" + rpcServerPort + "]...");
				stopWatch.start("startBootstrapRPC");
				StartBootstrapRpcAcceptor.start(context, rpcServerPort);
				stopWatch.stop();
			} catch (Exception e) {
				logger.error("bootstrap类型的RPC服务启动异常：" + e.getMessage(), e);
			}
		} else {
			rpcServerPort = -1;
			logger.info("端口[" + rpcServerPort + "]被视为无效端口,执行容器没有启动bootstrap类型的RPC服务");
		}

		// 开始注册执行容器 暂定把组件信息都下载完后再注册
		logger.info("开始注册容器....");
		stopWatch.start("registContainer");
		ContainerRegister.getInstance().regist(client, context, serverPort, rpcServerPort);
		stopWatch.stop();

		logger.info("开始启动ZK类型的RPC服务...");
		// 暂定为服务启动失败，则退出启动
		stopWatch.start("startZKRPC");
		String rpcPath = ZKPaths.makePath(ContainerRegister.getContainerRegistPath(), PathConstants.RPC_REQEUST);
		StartZookeeperRpcAcceptor.start(context, client, rpcPath,
				Executors.newFixedThreadPool(ConfigCache.getInstance().getProperty(Constants.RPC_THREADPOOL_SIZE, 10)));
		stopWatch.stop();
		
		stopWatch.start("loadFlowExecConf");
		ContainerFlowExecConfCache.getInstance().load();
		stopWatch.stop();

		logger.info("启动心跳上报任务...");
		stopWatch.start("startHeartbeat");
		HeartbeatThread.getInstance().start();
		stopWatch.stop();
	}
}
