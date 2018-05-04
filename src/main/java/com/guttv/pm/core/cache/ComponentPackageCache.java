/**
 * 
 */
package com.guttv.pm.core.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.guttv.pm.core.bean.ComponentPackageBean;

/**
 * @author Peter
 *
 */
public class ComponentPackageCache {

	protected Logger logger = LoggerFactory.getLogger(ComponentPackageCache.class);

	// 保存所有的组件注册 <comID,ComponentPackageBean>
	private final Map<String, ComponentPackageBean> comPackMap = new ConcurrentHashMap<String, ComponentPackageBean>();

	/**
	 * 获取一个组件注册对象
	 * 
	 * @param comID
	 * @return
	 */
	public ComponentPackageBean getComPack(String comID) {
		if (StringUtils.isBlank(comID)) {
			return null;
		}

		ComponentPackageBean comPack = null;
		r.lock();
		try {
			comPack = comPackMap.get(comID);
			return comPack == null ? null : comPack.clone();
		} finally {
			r.unlock();
		}
	}

	/**
	 * 获取所有的comID
	 * 
	 * @return
	 */
	public Set<String> getAllComIDs() {
		Set<String> comIDs = new HashSet<String>();
		r.lock();
		try {
			comIDs.addAll(comPackMap.keySet());
		} finally {
			r.unlock();
		}
		return comIDs;
	}

	/**
	 * 获取所有的注册包
	 * 
	 * @return
	 */
	public List<ComponentPackageBean> getAllComPack() {
		List<ComponentPackageBean> comPacks = new ArrayList<ComponentPackageBean>();
		r.lock();
		try {
			Collection<ComponentPackageBean> values = comPackMap.values();
			if (values != null && values.size() > 0) {
				for (ComponentPackageBean cpb : values) {
					comPacks.add(cpb.clone());
				}
			}
		} finally {
			r.unlock();
		}
		return comPacks;
	}

	/**
	 * 缓存一个组件包
	 * 
	 * @param comID
	 * @return
	 */
	public ComponentPackageBean uncacheComPack(String comID) {
		w.lock();
		try {
			return comPackMap.remove(comID);
		} finally {
			w.unlock();
		}
	}

	/**
	 * 缓存一个组件包
	 * 
	 * @param comPack
	 * @return old
	 */
	public ComponentPackageBean cacheComPack(ComponentPackageBean comPack) {
		if (comPack == null || StringUtils.isBlank(comPack.getComID())) {
			logger.error("缓存失败，对象为空或者对象中实现类为空");
			return null;
		}
		w.lock();
		try {
			return comPackMap.put(comPack.getComID(), comPack);
		} finally {
			w.unlock();
		}
	}

	/**
	 * 组件包的数量
	 * 
	 * @return
	 */
	public int countComponentPack() {
		return comPackMap.size();
	}

	// 重入锁
	private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();

	// 下面是单例配置
	private ComponentPackageCache() {
	};

	private static ComponentPackageCache instance = new ComponentPackageCache();

	public static synchronized ComponentPackageCache getInstance() {
		return instance;
	}

}
