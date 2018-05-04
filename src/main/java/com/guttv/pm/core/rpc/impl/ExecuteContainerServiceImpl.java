/**
 * 
 */
package com.guttv.pm.core.rpc.impl;

import java.util.List;
import java.util.Map;

import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.guttv.pm.container.ContainerFlowExecConfCache;
import com.guttv.pm.container.onstart.ContainerRegister;
import com.guttv.pm.core.bean.ExecuteContainer;
import com.guttv.pm.core.cache.ConfigCache;
import com.guttv.pm.core.flow.FlowExecuteConfig;
import com.guttv.pm.core.flow.FlowExecuteEngine;
import com.guttv.pm.core.rpc.ExecuteContainerService;
import com.guttv.pm.core.zk.PathConstants;
import com.guttv.pm.core.zk.ZookeeperHelper;
import com.guttv.pm.utils.Enums.ExecuteContainerStatus;
import com.guttv.pm.utils.Utils;

/**
 * @author Peter
 *
 */
@Component
public class ExecuteContainerServiceImpl implements ExecuteContainerService {

	protected Logger logger = LoggerFactory.getLogger(ExecuteContainerServiceImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.guttv.pm.common.rpc.ExecuteContainerService#update(com.guttv.pm.
	 * common.bean.ExecuteContainer)
	 */
	@Override
	public void update(ExecuteContainer container) throws Exception {
		if (container == null) {
			return;
		}

		ExecuteContainer localContainer = ContainerRegister.getInstance().getCurrentContainer();
		if (localContainer == null) {
			String error = "容器中缓存的容器信息为空";
			logger.error(error);
			throw new Exception(error);
		}

		// 目前只更新 别名，心跳周期，和备注
		localContainer.setAlias(container.getAlias());
		localContainer.setHeartbeatPeriod(container.getHeartbeatPeriod());
		localContainer.setRemark(container.getRemark());

		localContainer.setUpdateTime(Utils.getCurrentTimeString());

		ZookeeperHelper.putToZookeeper(localContainer.getRegistPath(),
				ContainerRegister.getInstance().getCuratorFrameworkClient(), localContainer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.guttv.pm.common.rpc.ExecuteContainerService#shutdown()
	 */
	public void shutdown() throws Exception {
		if (ContainerRegister.getInstance().isExecuteContainer()) {
			System.exit(0);
		} else {
			throw new Exception("只有纯执行容器才能被关闭");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.guttv.pm.common.rpc.ExecuteContainerService#forbbiden()
	 */
	@Override
	public void forbbiden() throws Exception {
		ContainerRegister.getInstance().updateStatus(ExecuteContainerStatus.FORBBIDEN);

		// 删除所有流程执行配置
		List<FlowExecuteConfig> configs = ContainerFlowExecConfCache.getInstance().getAllFlowExecuteConfigs();
		if (configs != null && configs.size() > 0) {
			for (FlowExecuteConfig config : configs) {
				FlowExecuteEngine.stopFlowExecuteConfig(config);
				ContainerFlowExecConfCache.getInstance().removeFlowExecuteConfig(config.getFlowExeCode());
			}
		}

		// 删除流程任务
		ExecuteContainer container = ContainerRegister.getInstance().getCurrentContainer();
		String execFlowPath = ZKPaths.makePath(container.getRegistPath(), PathConstants.CONTAINER_EXECFLOW_PATH);
		ZookeeperHelper.deleteFromZookeeper(execFlowPath, ContainerRegister.getInstance().getCuratorFrameworkClient());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.guttv.pm.common.rpc.ExecuteContainerService#start()
	 */
	@Override
	public void start() throws Exception {
		ContainerRegister.getInstance().updateStatus(ExecuteContainerStatus.NORMAL);
	}

	@Override
	public void refreshSpringConfig() throws Exception {
		ConfigCache.getInstance().refresh();
	}

	@Override
	public Map<String, Object> getSpringConfig() throws Exception {
		return ConfigCache.getInstance().getSpringConfigMap();
	}
}
