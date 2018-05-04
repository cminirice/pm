/**
 * 
 */
package com.guttv.pm.core.flow;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.guttv.pm.utils.Decompression;

/**
 * @author Peter
 *
 */
public class ComponentClassLoader extends URLClassLoader {

	/**
	 * 修改类加载的顺序
	 */
	public Class<?> loadClass(String name) throws ClassNotFoundException {

		Class<?> clz = null;
		synchronized (getClassLoadingLock(name)) {
			// 先从内存中找找看
			clz = findLoadedClass(name);

			if (clz == null) {
				try {
					// 从组件包中找找
					clz = findClass(name);
				} catch (Exception e) {
				}

				// 如果还没有，就从系统加载类中找
				if (clz == null) {
					clz = ClassLoader.getSystemClassLoader().loadClass(name);
				}
			}
		}

		return clz;
	}

	public ComponentClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

	public ComponentClassLoader(URL[] urls) {
		super(urls);
	}

	/**
	 * 
	 * @param clz
	 * @return
	 * @throws Exception
	 */
	public Object newInstance(Class<?> clz) throws Exception {
		if (context == null) {
			return clz.newInstance();
		}

		try {
			return context.getBean(clz);
		} catch (Exception e) {
			logger.debug("从context加载类[" + clz.getName() + "]失败，尝试调用空构造函数:" + e.getMessage());
			return clz.newInstance();
		}
	}

	/**
	 * 
	 * @param clzName
	 * @return
	 * @throws Exception
	 */
	public Object newInstance(String clzName) throws Exception {
		return newInstance(loadClass(clzName));
	}

	/**
	 * 
	 * @param beanName
	 * @return
	 */
	public Object getBean(String beanName) {
		return context.getBean(beanName);
	}

	private boolean init = false;

	public void tryInitApplicationContext(String comID) throws Exception {

		if (init) {
			return;
		}

		// 只初始化一次，不管成功失败
		synchronized (this) {
			if (init) {
				return;
			}
			init = true;

			URL[] urls = this.getURLs();
			List<String> paths = new ArrayList<String>();
			if (urls != null && urls.length > 0) {
				for (URL url : urls) {
					File file = new File(url.getFile());
					if (file.exists()) {
						logger.debug("检测文件：" + url.getFile());
						paths.addAll(Decompression.getResource(file, ".*" + comID + "-spring.xml"));
					}
				}
			}

			ClassLoader old = Thread.currentThread().getContextClassLoader();

			try {
				// mybatis加载的时候用到此classloader
				Thread.currentThread().setContextClassLoader(this);

				// 处理配置文件bean
				List<String> change = new ArrayList<String>();
				change.add("classpath:*" + comID + "-spring.xml");
				for (String path : paths) {
					change.add("classpath:" + path);
				}
				
				logger.info("找到路径：" + change);
				context = new ClassPathXmlApplicationContext();
				context.setClassLoader(this);
				context.setConfigLocations(change.toArray(new String[change.size()]));
				context.refresh();
				if (logger.isDebugEnabled()) {
					logger.debug("共生成bean[" + context.getBeanDefinitionCount() + "]个:"
							+ Arrays.toString(context.getBeanDefinitionNames()));
				}
				if (logger.isInfoEnabled()) {
					logger.info("共生成bean[" + context.getBeanDefinitionCount() + "]个");
				}

			} finally {
				// 用完还得把以前的loader放回去
				Thread.currentThread().setContextClassLoader(old);
			}
		}
	}

	public void scan(String... basePackages) {
		// 处理注解bean
		AnnotationConfigApplicationContext con = new AnnotationConfigApplicationContext();
		context.setParent(con);
		// con.setParent(context);
		con.setClassLoader(this);
		con.scan(basePackages);
		con.refresh();
		context.refresh();
		logger.debug("扫描路径" + Arrays.toString(basePackages) + "共生成bean[" + con.getBeanDefinitionCount() + "]个:"
				+ Arrays.toString(con.getBeanDefinitionNames()));
	}

	public void close() throws IOException {
		IOUtils.closeQuietly(context);
		context = null;
		super.close();
	}

	private ClassPathXmlApplicationContext context = null;

	protected static final Logger logger = LoggerFactory.getLogger(ComponentClassLoader.class);
}
