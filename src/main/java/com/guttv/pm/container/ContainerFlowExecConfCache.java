/**
 * 
 */
package com.guttv.pm.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.guttv.pm.container.onstart.ContainerRegister;
import com.guttv.pm.core.bean.ExecuteContainer;
import com.guttv.pm.core.cache.FlowExecuteConfigCache;
import com.guttv.pm.core.flow.FlowExecuteConfig;
import com.guttv.pm.core.zk.PathConstants;
import com.guttv.pm.core.zk.ZookeeperHelper;
import com.guttv.pm.utils.Enums.ExecuteContainerStatus;

/**
 * @author Peter
 *
 */
public class ContainerFlowExecConfCache {

	protected Logger logger = LoggerFactory.getLogger(ContainerFlowExecConfCache.class);

	// 保存所有的流程<flowExecuteCode,FlowExecuteConfig>
	private final Map<String, FlowExecuteConfig> flowExecuteConfigMap = new HashMap<String, FlowExecuteConfig>();

	/**
	 * 获取一个流程执行配置
	 * 
	 * @param flowExecuteCode
	 * @return
	 */
	public FlowExecuteConfig getFlowExecuteConfig(String flowExecuteCode) {
		if (StringUtils.isBlank(flowExecuteCode)) {
			return null;
		}

		r.lock();
		try {
			return flowExecuteConfigMap.get(flowExecuteCode);
		} finally {
			r.unlock();
		}
	}

	/**
	 * 获取所有的流程执行配置
	 * 
	 * @return
	 */
	public List<FlowExecuteConfig> getAllFlowExecuteConfigs() {
		List<FlowExecuteConfig> flowExecuteConfigs = new ArrayList<FlowExecuteConfig>();
		r.lock();
		try {
			Collection<FlowExecuteConfig> values = flowExecuteConfigMap.values();
			if (values != null && values.size() > 0) {
				for (FlowExecuteConfig fb : values) {
					flowExecuteConfigs.add(fb);
				}
			}
		} finally {
			r.unlock();
		}
		return flowExecuteConfigs;
	}

	/**
	 * 缓存一个流程执行配置
	 * 
	 * @param flowExecuteConfig
	 * @return
	 */
	public FlowExecuteConfig cacheFlowExecuteConfig(FlowExecuteConfig flowExecuteConfig) {
		if (flowExecuteConfig == null || StringUtils.isBlank(flowExecuteConfig.getFlowExeCode())) {
			logger.error("缓存失败，对象为空或者对象中编码类型为空");
			return null;
		}

		// 为了减轻数据，进行阉割
		if (flowExecuteConfig.getFlow() != null) {
			flowExecuteConfig.getFlow().setFlowComPros(null);
			flowExecuteConfig.getFlow().setFlowContent(null);
			flowExecuteConfig.getFlow().setNodeVSCom(null);
		}

		w.lock();
		try {
			FlowExecuteConfig old = flowExecuteConfigMap.get(flowExecuteConfig.getFlowExeCode());
			if (old == null) {
				flowExecuteConfigMap.put(flowExecuteConfig.getFlowExeCode(), flowExecuteConfig);
			} else {
				FlowExecuteConfigCache.copy2(flowExecuteConfig, old);
			}
			return old;
		} finally {
			w.unlock();
		}
	}

	/**
	 * 删除一个流程执行配置
	 * 
	 * @param flowExecuteCode
	 * @return
	 */
	public FlowExecuteConfig removeFlowExecuteConfig(String flowExecuteCode) {
		if (StringUtils.isBlank(flowExecuteCode)) {
			return null;
		}

		w.lock();
		try {
			return flowExecuteConfigMap.remove(flowExecuteCode);
		} finally {
			w.unlock();
		}
	}

	public int countFlowExecuteConfig() {
		return flowExecuteConfigMap.size();
	}

	public void load() throws Exception {
		ExecuteContainer container = ContainerRegister.getInstance().getCurrentContainer();
		if (container == null) {
			throw new Exception("执行容器尚未注册");
		}

		if (ExecuteContainerStatus.FORBBIDEN.equals(container.getStatus())) {
			logger.info("禁用状态的容器不需要加载流程执行配置");
			return;
		}

		String execFlowPath = ZKPaths.makePath(container.getRegistPath(), PathConstants.CONTAINER_EXECFLOW_PATH);
		CuratorFramework client = ContainerRegister.getInstance().getCuratorFrameworkClient();
		if (client.checkExists().forPath(execFlowPath) != null) {
			// 获取所有的流程执行配置节点
			List<String> children = client.getChildren().forPath(execFlowPath);
			if (children != null && children.size() > 0) {

				for (String child : children) {
					try {
						// 下载流程执行配置数据
						String childPath = ZKPaths.makePath(execFlowPath, child);

						FlowExecuteConfig config = ZookeeperHelper.getFromZookeeper(childPath, client,
								FlowExecuteConfig.class);
						// 缓存
						this.cacheFlowExecuteConfig(config);

					} catch (Exception e) {
						logger.error("处理路径[" + child + "]时，出现异常：" + e.getMessage(), e);
					}

				}
			}
		}
	}

	// 重入锁
	private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();

	// 下面是单例配置
	private ContainerFlowExecConfCache() {
	}

	private static class Single {
		private static ContainerFlowExecConfCache instance = new ContainerFlowExecConfCache();
	}

	public static ContainerFlowExecConfCache getInstance() {
		return Single.instance;
	}
}
