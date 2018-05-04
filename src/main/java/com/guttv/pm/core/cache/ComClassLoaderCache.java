/**
 * 
 */
package com.guttv.pm.core.cache;

import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.guttv.pm.core.bean.ComponentPackageBean;
import com.guttv.pm.core.flow.ComponentClassLoader;

/**
 * 组件的类加载器缓存
 * 
 * @author Peter
 *
 */
public class ComClassLoaderCache {

	protected Logger logger = LoggerFactory.getLogger(ComClassLoaderCache.class);

	/**
	 * 保存注册的加载类
	 */
	private final Map<String, URLClassLoader> comClassLoaderMap = new HashMap<String, URLClassLoader>();

	/**
	 * 获取一个类加载器
	 * 
	 * @param comID
	 * @return
	 */
	public URLClassLoader getComponentClassLoader(String comID) {
		r.lock();
		try {
			return comClassLoaderMap.get(comID);
		} finally {
			r.unlock();
		}
	}

	/**
	 * 获取组件实例
	 * 
	 * @param comID
	 * @param clzName
	 * @return
	 * @throws Exception
	 */
	public Object getInstance(String comID, String clzName) throws Exception {
		URLClassLoader urlClassLoader = getComponentClassLoader(comID);
		if (urlClassLoader == null) {
			ComponentPackageBean taskRegist = ComponentPackageCache.getInstance().getComPack(comID);
			if (taskRegist == null) {
				throw new Exception("没有发现标识为[" + comID + "]的注入组件信息");
			} else {
				throw new Exception(
						"找到标识为[" + comID + "]的注入组件文件为[" + taskRegist.getComPackageFilePath() + "]，请确认其中有JAR包文件");
			}
		}

		if (urlClassLoader instanceof ComponentClassLoader) {
			return ((ComponentClassLoader) urlClassLoader).newInstance(clzName);
		} else {
			Class<?> clz = urlClassLoader.loadClass(clzName);
			return clz.newInstance();
			// return ReflectUtil.newInstance(clz); //不能用该方法，组件修改代码重新注册，会反馈以前的类
		}
	}

	/**
	 * 缓存一个流程类加载器
	 * 
	 * @param flow
	 * @return
	 */
	public void cacheComponentClassLoader(String comID, URLClassLoader urlClassLoader) {
		if (urlClassLoader == null || StringUtils.isBlank(comID)) {
			logger.error("缓存失败，组件标识为空或者类加载器为空。：version=" + comID);
			return;
		}
		w.lock();
		try {
			URLClassLoader old = comClassLoaderMap.put(comID, urlClassLoader);

			// 把以前的关闭 需要判断是否为同一个
			if (old != null && old != urlClassLoader) {
				IOUtils.closeQuietly(old);
			}
		} finally {
			w.unlock();
		}
	}

	/**
	 * 卸载一个类加载器
	 * 
	 * @param comID
	 */
	public void uncacheComponentCassLoader(String comID) {
		if (StringUtils.isBlank(comID)) {
			return;
		}
		w.lock();
		try {
			URLClassLoader old = comClassLoaderMap.remove(comID);
			// 把以前的关闭
			if (old != null) {
				IOUtils.closeQuietly(old);
			}
		} finally {
			w.unlock();
		}
	}

	// 重入锁
	private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();

	// 下面是单例配置
	private ComClassLoaderCache() {
	}

	private static ComClassLoaderCache instance = new ComClassLoaderCache();

	public static synchronized ComClassLoaderCache getInstance() {
		return instance;
	}
}
