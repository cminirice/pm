/**
 * 
 */
package com.guttv.pm.core.rpc.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.guttv.pm.container.ContainerFlowExecConfCache;
import com.guttv.pm.container.onstart.ContainerRegister;
import com.guttv.pm.core.bean.ExecuteContainer;
import com.guttv.pm.core.cache.TaskCache;
import com.guttv.pm.core.flow.FlowExecuteConfig;
import com.guttv.pm.core.flow.FlowExecuteEngine;
import com.guttv.pm.core.rpc.FlowExecuteConfigService;
import com.guttv.pm.core.zk.PathConstants;
import com.guttv.pm.core.zk.ZookeeperHelper;
import com.guttv.pm.utils.Enums.FlowExecuteStatus;

/**
 * @author Peter
 *
 */
@Component
public class FlowExecuteConfigServiceImpl implements FlowExecuteConfigService {
	protected Logger logger = LoggerFactory.getLogger(FlowExecuteConfigServiceImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.guttv.pm.common.rpc.FlowExecuteConfigService#getFlowExecuteConfig(
	 * java.lang.String)
	 */
	public FlowExecuteConfig getFlowExecuteConfig(String flowExeCode) throws Exception {
		return ContainerFlowExecConfCache.getInstance().getFlowExecuteConfig(flowExeCode);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.guttv.pm.common.rpc.FlowExecuteConfigService#getAllFlowExecuteConfigs
	 * ()
	 */
	public List<FlowExecuteConfig> getAllFlowExecuteConfigs() throws Exception {
		return ContainerFlowExecConfCache.getInstance().getAllFlowExecuteConfigs();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.guttv.pm.common.rpc.FlowExecuteConfigService#getAllFlowExecConfCodes(
	 * )
	 */
	public List<String> getAllFlowExecConfCodes() throws Exception {
		List<FlowExecuteConfig> configs = ContainerFlowExecConfCache.getInstance().getAllFlowExecuteConfigs();
		List<String> codes = new ArrayList<String>();
		if (configs == null || configs.size() == 0) {
			return codes;
		}

		for (FlowExecuteConfig config : configs) {
			codes.add(config.getFlowExeCode());
		}
		return codes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.guttv.pm.common.rpc.FlowExecuteConfigService#addFlowExecConfigs(java.
	 * lang.String[])
	 */
	public String addFlowExecConfigs(String[] flowExeCodes) throws Exception {
		if (flowExeCodes == null || flowExeCodes.length == 0) {
			return null;
		}

		// 循环添加
		FlowExecuteConfig config = null;
		String configRootPath = ZookeeperHelper.getRealPath(PathConstants.FLOW_EXEC_CONFIG_PATH);
		CuratorFramework client = ContainerRegister.getInstance().getCuratorFrameworkClient();
		ExecuteContainer container = ContainerRegister.getInstance().getCurrentContainer();
		int add = 0;
		int update = 0;
		int error = 0;
		String containerExecFlowPath = ZKPaths.makePath(container.getRegistPath(),
				PathConstants.CONTAINER_EXECFLOW_PATH);
		for (String flowExeCode : flowExeCodes) {

			try {
				// 下载流程执行配置
				config = ZookeeperHelper.getFromZookeeper(ZKPaths.makePath(configRootPath, flowExeCode), client,
						FlowExecuteConfig.class);
				if (config == null) {
					error++;
					continue;
				}

				config.setStatus(FlowExecuteStatus.INIT);
				String path = ZKPaths.makePath(containerExecFlowPath, flowExeCode);

				/**
				 * 此地也可以先阉割再上传,只保存，
				 */
				// 上传ZK
				if (config.getFlow() != null) {
					config.getFlow().setFlowComPros(null);
					config.getFlow().setFlowContent(null);
					config.getFlow().setNodeVSCom(null);
				}
				config.cleanStatusDesc();
				ZookeeperHelper.putToZookeeper(path, client, config, true);

				// 再保存到缓存
				FlowExecuteConfig old = ContainerFlowExecConfCache.getInstance().cacheFlowExecuteConfig(config);
				if (old != null) {
					update++;
				} else {
					add++;
				}

			}catch(NodeExistsException e) {
				error++;
			} catch (Exception e) {
				logger.error("添加流程执行配置异常：" + e.getMessage(), e);
				error++;
			}
		}
		StringBuilder sb = new StringBuilder();
		if (add > 0) {
			sb.append("成功添加[").append(add).append("];");
		}
		if (update > 0) {
			sb.append("更新[").append(add).append("];");
		}
		if (error > 0) {
			sb.append("执行错误[").append(add).append("];");
		}
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.guttv.pm.common.rpc.FlowExecuteConfigService#stopFlowExecConfig(java.
	 * lang.String)
	 */
	@Override
	public void stopFlowExecConfig(String flowExeCode) throws Exception {
		FlowExecuteConfig config = ContainerFlowExecConfCache.getInstance().getFlowExecuteConfig(flowExeCode);
		if (config == null) {
			throw new Exception("执行容器上已经不存在[" + flowExeCode + "]的流程执行配置");
		}

		FlowExecuteStatus status = config.getStatus();
		if (!FlowExecuteStatus.RUNNING.equals(status) && !FlowExecuteStatus.PAUSE.equals(status)) {
			throw new Exception("当前状态不能停止操作");
		}

		FlowExecuteEngine.stopFlowExecuteConfig(config);
	}

	@Override
	public void startFlowExecConfig(String flowExeCode) throws Exception {
		FlowExecuteConfig config = ContainerFlowExecConfCache.getInstance().getFlowExecuteConfig(flowExeCode);
		if (config == null) {
			throw new Exception("执行容器上已经不存在[" + flowExeCode + "]的流程执行配置");
		}

		FlowExecuteStatus status = config.getStatus();
		if (!FlowExecuteStatus.INIT.equals(status) && !FlowExecuteStatus.FINISH.equals(status)) {
			throw new Exception("当前状态不能启动操作");
		}

		// 试着关闭以前的任务，如果真有没执行完的，可能会影响后面的启动
		// 正常情况下，前端页面有控制是正常状态的才能启动
		FlowExecuteEngine.stopFlowExecuteConfig(config);

		// 如果上面停止失败，下面执行也会失败
		FlowExecuteEngine.excuteFlowExecuteConfig(config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.guttv.pm.common.rpc.FlowExecuteConfigService#initFlowExecConfig(java.
	 * lang.String)
	 */
	@Override
	public void initFlowExecConfig(String flowExeCode) throws Exception {
		FlowExecuteConfig config = ContainerFlowExecConfCache.getInstance().getFlowExecuteConfig(flowExeCode);
		if (config == null) {
			throw new Exception("执行容器上已经不存在[" + flowExeCode + "]的流程执行配置");
		}
		// 试着关闭以前的任务，如果真有没执行完的，可能会影响后面的启动
		// 正常情况下，前端页面有控制是正常状态的才能启动
		FlowExecuteEngine.stopFlowExecuteConfig(config);

		config.setStatus(FlowExecuteStatus.INIT, "RPC调用初始化状态");
	}

	/*
	 * 
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.guttv.pm.common.rpc.FlowExecuteConfigService#deleteFlowExeConfig(java
	 * .lang.String)
	 */
	@Override
	public void deleteFlowExeConfig(String flowExeCode) throws Exception {
		FlowExecuteConfig config = ContainerFlowExecConfCache.getInstance().removeFlowExecuteConfig(flowExeCode);
		if (config == null) {
			throw new Exception("执行容器上已经不存在[" + flowExeCode + "]的流程执行配置");
		}

		// 试着关闭以前的任务
		FlowExecuteEngine.stopFlowExecuteConfig(config);

		// 再删除ZK
		CuratorFramework client = ContainerRegister.getInstance().getCuratorFrameworkClient();
		ExecuteContainer container = ContainerRegister.getInstance().getCurrentContainer();
		String containerExecFlowPath = ZKPaths.makePath(container.getRegistPath(),
				PathConstants.CONTAINER_EXECFLOW_PATH);
		ZookeeperHelper.deleteFromZookeeper(ZKPaths.makePath(containerExecFlowPath, flowExeCode), client);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.guttv.pm.core.rpc.FlowExecuteConfigService#taskGo(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public void taskGo(String flowExeCode, String nodeID) throws Exception {
		FlowExecuteConfig config = ContainerFlowExecConfCache.getInstance().getFlowExecuteConfig(flowExeCode);
		if (config == null) {
			throw new Exception("执行容器上已经不存在[" + flowExeCode + "]的流程执行配置");
		}

		TaskCache.getInstance().setPauseStatusByFlowExeCode(flowExeCode, nodeID, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.guttv.pm.core.rpc.FlowExecuteConfigService#taskPause(java.lang.
	 * String, java.lang.String)
	 */
	@Override
	public void taskPause(String flowExeCode, String nodeID) throws Exception {
		FlowExecuteConfig config = ContainerFlowExecConfCache.getInstance().getFlowExecuteConfig(flowExeCode);
		if (config == null) {
			throw new Exception("执行容器上已经不存在[" + flowExeCode + "]的流程执行配置");
		}

		TaskCache.getInstance().setPauseStatusByFlowExeCode(flowExeCode, nodeID, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.guttv.pm.core.rpc.FlowExecuteConfigService#taskStacktrace(java.lang.
	 * String, java.lang.String)
	 */
	@Override
	public String taskStacktrace(String flowExeCode, String nodeID) throws Exception {
		return TaskCache.getInstance().getTaskStacktrace(flowExeCode, nodeID);
	}
}
