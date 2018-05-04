/**
 * 
 */
package com.guttv.pm.core.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.guttv.pm.core.bean.ComponentBean;
import com.guttv.pm.utils.JsonUtil;

/**
 * @author Peter
 *
 */
public class ComponentCache {

	protected Logger logger = LoggerFactory.getLogger(ComponentCache.class);
	
	//保存所有的组件<clz,ComponentBean>
	private final Map<String,ComponentBean> componentMap = new ConcurrentHashMap<String,ComponentBean>();
	
	/**
	 * 获取一个组件对象
	 * @param clz
	 * @return
	 */
	public ComponentBean getComponent(String clz) {
		if(StringUtils.isBlank(clz)) {
			return null;
		}
		
		ComponentBean com = null;
		r.lock();
		try {
			com = componentMap.get(clz);
			return com==null?null:com.clone();
		}finally {
			r.unlock();
		}
	}
	
	public List<ComponentBean> getAllComponents(){
		List<ComponentBean> components = new ArrayList<ComponentBean>();
		
		r.lock();
		try {
			Collection<ComponentBean> values = componentMap.values();
			if(values != null && values.size() > 0) {
				for(ComponentBean com : values) {
					components.add(com.clone());
				}
			}
		}finally {
			r.unlock();
		}
		return components;
	}
	
	/**
	 * 获取组件包里所有的组件
	 * @param comID
	 * @return
	 */
	public List<ComponentBean> getComponents(String comID){
		if(StringUtils.isBlank(comID)) {
			return null;
		}
		
		List<ComponentBean> components = new ArrayList<ComponentBean>();
		if(StringUtils.isBlank(comID)) {
			return components;
		}
		r.lock();
		try {
			for(ComponentBean com : componentMap.values()) {
				if(comID.equals(com.getComID())) {
					components.add(com.clone());
				}
			}
		}finally {
			r.unlock();
		}
		return components;
	}
	
	
	public boolean updateStatus(ComponentBean comp) {
		if(comp == null || StringUtils.isBlank(comp.getClz()) && comp.getStatus() != null) {
			return false;
		}
		
		w.lock();
		try {
			ComponentBean com = componentMap.get(comp.getClz());
			if(com != null && com.getStatus() != comp.getStatus()) {
				com.setStatus(comp.getStatus());
				com.setUpdateTime(new Date());
				return true;
			}
			
		} finally{
			w.unlock();
		}
		return false;
	}
	
	/**
	 * 缓存一个组件集合
	 * @param coms
	 */
	public void cacheComponent(List<ComponentBean> coms) {
		if(coms == null || coms.size() == 0) {
			return;
		}
		
		for(ComponentBean com : coms) {
			ComponentBean old = cacheComponent(com);
			if(old != null) {
				logger.info("注册包[comID="+old.getComID()+"][clz="+old.getClz()+"]被[clz="+com.getClz()+"]替换：" + JsonUtil.toJson(old));
			}
		}
	}
	
	/**
	 * 缓存一个组件
	 * @param com
	 * @return
	 */
	public ComponentBean cacheComponent(ComponentBean com) {
		if(com == null || StringUtils.isBlank(com.getClz())) {
			logger.error("缓存失败，对象为空或者对象中实现类为空");
			return null;
		}
		w.lock();
		try {
			return componentMap.put(com.getClz(), com);
		} finally{
			w.unlock();
		}
	}
	
	

	/**
	 * 卸载所有唯一标识为comID的组件
	 * @param comID
	 */
	public void uncacheComponent(String comID) {
		if(StringUtils.isBlank(comID)) {
			return;
		}
		
		w.lock();
		try {
			Set<String> clsSet = componentMap.keySet();
			ComponentBean com = null;
			for(String clz : clsSet) {
				com = componentMap.get(clz);
				if(comID.equals(com.getComID())) {
					logger.info("卸载组件：" + JsonUtil.toJson(componentMap.remove(clz)));
				}
			}
		} finally{
			w.unlock();
		}
		
	}
	
	/**
	 * 返回组件的数量
	 * @return
	 */
	public int countComponent() {
		return componentMap.size();
	}
	
	//重入锁
	private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();
	
	//下面是单例配置
	private ComponentCache() {};
	private static ComponentCache instance = new ComponentCache();
	public static synchronized ComponentCache getInstance() {
		return instance;
	}
}
