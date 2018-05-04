package com.guttv.pm.platform;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StopWatch;

import com.guttv.pm.core.cache.ConfigCache;
import com.guttv.pm.utils.Constants;
import com.guttv.pm.utils.Utils;
import com.guttv.pm.utils.file.CheckSrcModify2TargetListener;
import com.guttv.pm.utils.file.CheckTargetFileUndeleteListener;

class JarFileArchiveJspSupport {
	protected static final Logger logger = LoggerFactory.getLogger(JarFileArchiveJspSupport.class);

	/**
	 * 对代码打JAR包，JSP在外面的程序进行支持
	 * 
	 * @throws IOException
	 */
	static void doSupport(ApplicationContext context, StopWatch stopWatch) throws Exception {
		// 找到发布的ServletContextPath,判断是不是在临时文件夹里
		ServletContext servletContext = context.getBean(ServletContext.class);
		String servletContextPath = servletContext.getRealPath("/");

		File contextFile = new File(servletContextPath);
		if (!contextFile.exists()) {
			logger.debug("平台的ServletContextPath文件夹[" + servletContextPath + "]不存在，退出支持...");
			return;
		}

		File tmp = new File(System.getProperty("java.io.tmpdir"));
		if (!tmp.equals(contextFile.getParentFile())) {
			logger.info("平台的ServletContextPath虚拟文件夹[" + contextFile.getParent() + "]不是系统的临时文件夹[" + tmp.getAbsolutePath()
					+ "]，平台认为不是JAR运行状态，退出支持");
			return;
		}

		String home = Utils.getProjectLocation();
		String from = home + ConfigCache.getInstance().getProperty(Constants.PLATFORM_WEBAPP_DIR, "/webapp");

		File fromDir = new File(from);
		if (!fromDir.exists()) {
			logger.warn("系统配置的webapp文件夹[" + from + "]不存在，无法提供支持，可能导致平台无法访问，如果需要访问平台，请确认路径是否正确：" + from);
			return;
		}

		stopWatch.start("JarFileArchiveJspSupport");

		FileUtils.copyDirectory(fromDir, contextFile);
		logger.info("复制WEB页面从[" + from + "]到[" + servletContextPath + "]");

		// 文件更新监听
		FileAlterationMonitor monitor = new FileAlterationMonitor(1000);

		// 当虚拟tomcat下面的文件被删除时，重新复制一份过去
		FileAlterationObserver observer = new FileAlterationObserver(contextFile);
		observer.addListener(new CheckTargetFileUndeleteListener(contextFile, fromDir));
		monitor.addObserver(observer);

		// 当源文件修改时，把源文件复制到tomcat虚拟目录中
		observer = new FileAlterationObserver(fromDir);
		observer.addListener(new CheckSrcModify2TargetListener(contextFile, fromDir));
		monitor.addObserver(observer);

		stopWatch.stop();

		// go
		monitor.start();

	}
}
