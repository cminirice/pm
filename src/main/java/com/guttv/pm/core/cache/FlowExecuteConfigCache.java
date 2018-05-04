/**
 * 
 */
package com.guttv.pm.core.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import com.guttv.pm.core.bean.ComponentNodeBean;
import com.guttv.pm.core.flow.FlowExecuteConfig;
import com.guttv.pm.utils.Enums.FlowExecuteStatus;

/**
 * @author Peter
 *
 */
public class FlowExecuteConfigCache {

	protected Logger logger = LoggerFactory.getLogger(FlowExecuteConfigCache.class);

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
	 * 该方法只给 单机版的程序从Action中调用。 修改流程的状态
	 * 
	 * @param flow
	 */
	public void updateStatus(String flowExeCode, int status, String statusDesc) throws Exception {

		if (StringUtils.isBlank(flowExeCode)) {
			return;
		}

		FlowExecuteStatus enumStatus = FlowExecuteStatus.valueOf(status);
		if (enumStatus == null) {
			throw new Exception("不存在值为[" + status + "]的状态");
		}

		w.lock();
		try {
			FlowExecuteConfig config = flowExecuteConfigMap.get(flowExeCode);
			if (config == null) {
				throw new Exception("不存在执行编码为[" + flowExeCode + "]的执行配置");
			}
			config.setStatus(enumStatus, statusDesc);
			config.setUpdateTime(new Date());
		} finally {
			w.unlock();
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
		w.lock();
		try {
			/**
			 * 因其它模板更新状态，都是通过对象直接更新状态值，因此，缓存中的对象不能更改，特别是 ComponentNodeBean
			 */
			FlowExecuteConfig old = flowExecuteConfigMap.get(flowExecuteConfig.getFlowExeCode());
			if (old == null) {
				flowExecuteConfigMap.put(flowExecuteConfig.getFlowExeCode(), flowExecuteConfig);
			} else {
				copy2(flowExecuteConfig, old);
			}
			return old;
		} finally {
			w.unlock();
		}
	}

	public static void copy2(FlowExecuteConfig from, FlowExecuteConfig to) {
		to.setFlow(from.getFlow());
		to.setStatus(from.getStatus(), from.getStatusDesc());
		to.setComDispatch(from.getComDispatchs());
		to.setComFlowProsMap(from.getComFlowProsMap());

		// 节点信息复制
		List<ComponentNodeBean> fromNodes = from.getComNodes();
		List<ComponentNodeBean> toNodes = to.getComNodes();
		if (fromNodes == null) {
			to.setComNodes(null);
		}

		Iterator<ComponentNodeBean> iter = toNodes.iterator();
		ComponentNodeBean cnb = null;
		// 先把源中的属性复制到目录对象中
		while (iter.hasNext()) {
			cnb = iter.next();
			boolean find = false; // 标记找到节点
			for (ComponentNodeBean cn : fromNodes) {
				// 在源中找到节点，把属性复制过去
				if (cnb.getNodeID().equals(cn.getNodeID())) {
					find = true;
					// 以了防止改了属性漏改此处，采用效率比较低的方法
					BeanUtils.copyProperties(cn, cnb);
					/*
					 * cnb.setComponentClz(cn.getComponentClz());
					 * cnb.setComponentCn(cn.getComponentCn());
					 * cnb.setCreateTime(cn.getCreateTime());
					 * cnb.setFlowCode(cn.getFlowCode()); cnb.setId(cn.getId());
					 * cnb.setStatus(cn.getStatus());
					 * cnb.setUpdateTime(cn.getUpdateTime());
					 */
					break;
				}
			}

			// 如果在源中没有找到，直接在源中删除
			if (!find) {
				iter.remove();
			}
		}

		// 再把源中多的复制到目标中
		if (fromNodes.size() > toNodes.size()) {
			List<ComponentNodeBean> tmp = new ArrayList<ComponentNodeBean>();
			for (ComponentNodeBean cn : fromNodes) {
				boolean find = false;
				for (ComponentNodeBean c : toNodes) {
					if (cn.getNodeID().equals(c.getNodeID())) {
						find = true;
						break;
					}
				}
				if (!find) {
					tmp.add(cn);
				}
			}
			toNodes.addAll(tmp);
		}

	}

	/**
	 * 删除一个流程执行配置
	 * 
	 * @param flowExecuteCode
	 * @return
	 */
	public FlowExecuteConfig uncacheFlowExecuteConfig(String flowExecuteCode) {
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

	// 重入锁
	private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();

	// 下面是单例配置
	private FlowExecuteConfigCache() {
	};

	private static FlowExecuteConfigCache instance = new FlowExecuteConfigCache();

	public static synchronized FlowExecuteConfigCache getInstance() {
		return instance;
	}
}
