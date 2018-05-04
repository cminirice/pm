/**
 * 
 */
package com.guttv.pm.core.cache;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.StandardServletEnvironment;

import com.guttv.pm.utils.Utils;

/**
 * @author Peter
 * 
 */
@Component
public class ConfigCache {

	@Autowired
	private Environment env = null;

	/**
	 * 这样用的目的是为了能在表态方法中也可以用该对象 也可以在其它类中通过注解的方法把ConfigCache注解到属性中
	 * 
	 * @Autowired private ConfigCache config = null;
	 * 
	 * @param context
	 */
	public static synchronized void init(ApplicationContext context) {
		if (instance != null) {
			return;
		}
		instance = context.getBean(ConfigCache.class);
	}

	// 表态的对象不支持自动注入
	// @Autowired Autowired annotation is not supported on static fields
	private static ConfigCache instance = null;

	public static ConfigCache getInstance() {
		return instance;
	}

	private ConfigCache() {
	}

	public String getProperty(String key, String def) {
		r.lock();
		try {
			return env.getProperty(key, def);
		} finally {
			r.unlock();
		}
	}

	/**
	 * 获取spring的配置文件信息
	 * 
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getSpringConfigMap() throws Exception {
		StandardServletEnvironment sse = (StandardServletEnvironment) env;
		MutablePropertySources mps = sse.getPropertySources();
		Iterator<PropertySource<?>> iter = mps.iterator();
		PropertiesPropertySource pps = null;
		PropertySource<?> ps = null;
		Map<String, Object> configMap = new LinkedHashMap<String, Object>();
		r.lock();
		try {
			while (iter.hasNext()) {
				ps = iter.next();
				if (ps instanceof PropertiesPropertySource) {
					pps = (PropertiesPropertySource) ps;
					configMap.putAll(pps.getSource());
					break;
				}
			}
		} finally {
			r.unlock();
		}

		return configMap;
	}

	private long lastModify = 0;

	/**
	 * 更新配置文件
	 * 
	 * @throws Exception
	 */
	public synchronized void refresh() throws Exception {
		File configFile = Utils.getPropertiesFile();
		if (configFile == null) {
			throw new Exception("没有找到配置文件");
		}
		w.lock();
		try {
			if (lastModify == configFile.lastModified()) {
				return;
			}

			StandardServletEnvironment sse = (StandardServletEnvironment) env;
			MutablePropertySources mps = sse.getPropertySources();
			Iterator<PropertySource<?>> iter = mps.iterator();
			PropertiesPropertySource pps = null;
			PropertySource<?> ps = null;
			while (iter.hasNext()) {
				ps = iter.next();
				if (ps instanceof PropertiesPropertySource) {
					pps = (PropertiesPropertySource) ps;
					break;
				}
			}

			if (pps != null) {
				Properties p = new Properties();
				FileInputStream is = null;
				try {
					is = new FileInputStream(configFile);
					p.load(is);
				} finally {
					IOUtils.closeQuietly(is);
				}

				PropertiesPropertySource newPPS = new PropertiesPropertySource(pps.getName(), p);
				mps.addLast(newPPS);
				lastModify = configFile.lastModified();
			}
		} finally {
			w.unlock();
		}
	}

	public int getProperty(String keyName, int def) {
		try {
			return Integer.parseInt(getProperty(keyName, String.valueOf(def)));
		} catch (NumberFormatException e) {
			return def;
		}
	}

	public long getProperty(String keyName, long def) {
		try {
			return Long.parseLong(getProperty(keyName, String.valueOf(def)));
		} catch (NumberFormatException e) {
			return def;
		}
	}

	public boolean getProperty(String keyName, boolean def) {
		try {
			return Boolean.parseBoolean(getProperty(keyName, String.valueOf(def)));
		} catch (RuntimeException e) {
			return def;
		}
	}

	// 重入锁
	private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();
}
