/**
 * 
 */
package com.guttv.pm.platform;

import java.io.File;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.guttv.pm.container.ContainerFlowExecConfCache;
import com.guttv.pm.core.cache.ConfigCache;
import com.guttv.pm.core.cache.FlowExecuteConfigCache;
import com.guttv.pm.core.cache.TaskCache;
import com.guttv.pm.core.flow.FlowExecuteConfig;
import com.guttv.pm.core.flow.FlowExecuteEngine;
import com.guttv.pm.core.task.QuartzTaskScheduler;
import com.guttv.pm.utils.Constants;
import com.guttv.pm.utils.Enums.FlowExecuteStatus;
import com.guttv.pm.utils.ReflectUtil;

/**
 * @author Peter
 *
 */
public class PlatformShutdownHook extends Thread {
	protected static Logger logger = LoggerFactory.getLogger(PlatformShutdownHook.class);

	public void run() {

		int timeout = ConfigCache.getInstance().getProperty(Constants.SHUTDOWN_HOOK_TIMEOUT, 60000);

		try {
			
			try {
				//首先退出任务调度
				QuartzTaskScheduler.getInstance().shutdown();
			} catch (Exception e1) {
				logger.warn("退出任务调度异常:" + e1.getMessage(),e1);
			}

			// 删除tomcat虚拟目录
			try {
				ApplicationContext context = PlatformMain.context;
				if (context != null) {
					ServletContext servletContext = context.getBean(ServletContext.class);
					String servletContextPath = servletContext.getRealPath("/");

					File contextFile = new File(servletContextPath);
					File tmp = new File(System.getProperty("java.io.tmpdir"));

					// 虚拟目录存在，并且父目录必须是临时文件夹
					if (contextFile.exists() && tmp.equals(contextFile.getParentFile())) {
						boolean delete = FileUtils.deleteQuietly(contextFile);
						logger.info("删除系统虚拟目录[" + delete + "]：" + contextFile);
					}
				}
			} catch (Exception e1) {
				logger.error("退出程序时，删除tomcat虚拟目录异常：" + e1.getMessage());
			}
			
			try {
				Object tomcat = ReflectUtil.getFieldValue(PlatformMain.embeddedServletContainer, "tomcat");
				Object value = ReflectUtil.getFieldValue(tomcat, "basedir");
				if (value != null) {
					File baseFile = new File(value.toString());
					File tmp = new File(System.getProperty("java.io.tmpdir"));

					// 虚拟目录存在，并且父目录必须是临时文件夹
					if (baseFile.exists() && tmp.equals(baseFile.getParentFile())) {
						boolean delete = FileUtils.deleteQuietly(baseFile);
						logger.info("删除系统虚拟目录[" + delete + "]：" + baseFile);
					}
				}
			} catch (Throwable e) {
			}

			// 关闭所有任务
			List<String> flowExeCodes = TaskCache.getInstance().getAllExecutedFlowCode();
			if (flowExeCodes != null && flowExeCodes.size() > 0) {
				FlowExecuteConfig config = null;
				for (String flowExeCode : flowExeCodes) {
					config = FlowExecuteConfigCache.getInstance().getFlowExecuteConfig(flowExeCode);
					if (config != null) {
						// 如果缓存中有的话，从这里停止
						FlowExecuteEngine.stopFlowExecuteConfig(config);
					} else {
						// 如果缓存里没有，直接从任务处停止一下
						TaskCache.getInstance().stopTasksByFlowExeCode(flowExeCode);
					}
				}
			}

			// 检查任务有没有还没有执行完的
			boolean flag = false; // 标记是否都结束
			List<FlowExecuteConfig> flowExecuteConfigs = ContainerFlowExecConfCache.getInstance()
					.getAllFlowExecuteConfigs();

			if (flowExecuteConfigs != null && flowExecuteConfigs.size() > 0) {
				long start = System.currentTimeMillis();
				while ((System.currentTimeMillis() - start) < timeout && !flag) {
					Thread.sleep(2000);
					flag = true; // 假定都执行完了
					for (FlowExecuteConfig config : flowExecuteConfigs) {
						// 如果不是下面几种认为没有执行完
						if (!(FlowExecuteStatus.ERROR.equals(config.getStatus())
								|| FlowExecuteStatus.FORBIDDEN.equals(config.getStatus())
								|| FlowExecuteStatus.FINISH.equals(config.getStatus()))) {
							flag = false;
							logger.info(
									"发现流程配置[" + config.getFlowExeCode() + "]还没有结束，当前状态[" + config.getStatus() + "]");
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("退出等待时异常：" + e.getMessage(), e);
		} finally {
			// 关闭ZK连接
			try {
				PlatformMain.client.close();
			} catch (Exception e) {
				logger.error("退出时，关闭ZK连接异常：" + e.getMessage(), e);
			}
		}

		logger.info("管理平台将要退出...");

	}
}
