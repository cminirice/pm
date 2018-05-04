/**
 * 
 */
package com.guttv.pm.core.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.guttv.pm.core.bean.FlowBean;

/**
 * @author Peter
 *
 */
public class FlowCache {

	protected Logger logger = LoggerFactory.getLogger(FlowCache.class);

	// 保存所有的流程<flowCode,FlowBean>
	private final Map<String, FlowBean> flowMap = new HashMap<String, FlowBean>();

	/**
	 * 获取一个流程
	 * 
	 * @param flowCode
	 * @return
	 */
	public FlowBean getFlow(String flowCode) {
		if (StringUtils.isBlank(flowCode)) {
			return null;
		}
		FlowBean flow = null;
		r.lock();
		try {
			flow = flowMap.get(flowCode);
		} finally {
			r.unlock();
		}

		return flow == null ? null : flow.clone();
	}

	/**
	 * 获取所有的流程
	 * 
	 * @return
	 */
	public List<FlowBean> getAllFlows() {
		List<FlowBean> flows = new ArrayList<FlowBean>();
		r.lock();
		try {
			Collection<FlowBean> values = flowMap.values();
			if (values != null && values.size() > 0) {
				for (FlowBean fb : values) {
					flows.add(fb.clone());
				}
			}
		} finally {
			r.unlock();
		}
		return flows;
	}

	/**
	 * 缓存一个流程
	 * 
	 * @param flow
	 * @return
	 */
	public FlowBean cacheFlow(FlowBean flow) {
		if (flow == null || StringUtils.isBlank(flow.getCode())) {
			logger.error("缓存失败，对象为空或者对象中编码类型为空");
			return null;
		}
		w.lock();
		try {
			return flowMap.put(flow.getCode(), flow);
		} finally {
			w.unlock();
		}
	}

	/**
	 * 修改流程的状态
	 * 
	 * @param flow
	 */
	public boolean updateStatus(FlowBean flow) {

		if (flow == null || StringUtils.isBlank(flow.getCode()) && flow.getStatus() != null) {
			return false;
		}

		w.lock();
		try {
			FlowBean old = flowMap.get(flow.getCode());
			if (old != null && old.getStatus() != flow.getStatus()) {
				old.setStatus(flow.getStatus());
				old.setUpdateTime(new Date());
				return true;
			}
		} finally {
			w.unlock();
		}
		return false;
	}

	/**
	 * 移除流程
	 * 
	 * @param code
	 * @return
	 */
	public FlowBean uncacheFlow(String code) {
		if (StringUtils.isBlank(code)) {
			return null;
		}

		w.lock();
		try {
			return flowMap.remove(code);
		} finally {
			w.unlock();
		}

	}

	// 重入锁
	private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();

	// 下面是单例配置
	private FlowCache() {
	}

	private static FlowCache instance = new FlowCache();

	public static synchronized FlowCache getInstance() {
		return instance;
	}
}
