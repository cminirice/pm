package com.guttv.pm.platform;

import com.guttv.pm.container.onstart.StartExecuteSupport;
import com.guttv.pm.core.cache.ConfigCache;
import com.guttv.pm.core.zk.CuratorClientFactory;
import com.guttv.pm.core.zk.DistributedSequence;
import com.guttv.pm.core.zk.PathConstants;
import com.guttv.pm.core.zk.ZookeeperHelper;
import com.guttv.pm.core.zk.listener.ComponentListener;
import com.guttv.pm.core.zk.listener.ComponentPackListener;
import com.guttv.pm.core.zk.listener.FlowExecConfigListener;
import com.guttv.pm.core.zk.listener.FlowListener;
import com.guttv.pm.platform.container.ExecuteContainerListener;
import com.guttv.pm.platform.container.HeartbeatMonitor;
import com.guttv.pm.utils.Constants;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;

/**
 * 前端页面入口
 *
 */

@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
@SpringBootApplication
@RestController
@Configuration
@ImportResource(locations = { "classpath:kaptcha.xml" })
@ComponentScan("com.guttv.pm.core,com.guttv.pm.platform")
public class PlatformMain implements ApplicationListener<EmbeddedServletContainerInitializedEvent> {

	protected static final Logger logger = LoggerFactory.getLogger(PlatformMain.class);

	public static ApplicationContext context;

	public static CuratorFramework client = null;

	// 标记是否支持执行流程
	public static boolean supportExecuteFlow = true;

	public static int serverPort = -1;

	public static void main(String[] args) throws Exception {

		try {
			StopWatch stopWatch = new StopWatch();

			logger.info("启动流程制作管理平台服务...");
			stopWatch.start("server");
			context = SpringApplication.run(PlatformMain.class, args);
			stopWatch.stop();

			logger.info("设置系统属性...");
			stopWatch.start("initConfigCache");
			ConfigCache.init(context);
			stopWatch.stop();

			ServletContext servletContext = context.getBean(ServletContext.class);
			servletContext.setAttribute(Constants.USER_NAME_CONFIG,
					ConfigCache.getInstance().getProperty(Constants.USER_NAME_CONFIG, null));

			// 设置是否是单机运行
			boolean singleServer = ConfigCache.getInstance().getProperty(Constants.SERVER_START_SINGLESERVER, false);
			if (singleServer) {
				servletContext.setAttribute(Constants.SERVLETCONTEXT_SINGLESERVER_KEY, true);
			}

			Runtime.getRuntime().addShutdownHook(new PlatformShutdownHook());

			stopWatch.start("initCurator");
			client = CuratorClientFactory.getClient();
			client.start();
			stopWatch.stop();

			logger.info("初始化自增序号...");
			stopWatch.start("initSequence");
			DistributedSequence seq = DistributedSequence.getInstance();
			seq.init(client, ZookeeperHelper.getRealPath(PathConstants.SEQUENCE_PATH));
			stopWatch.stop();

			boolean startExecuteSurpport = ConfigCache.getInstance().getProperty(Constants.SERVER_START_EXECUTE_SUPPORT,
					false);
			if (!singleServer && startExecuteSurpport) {
				// 启动支持执行流程功能
				StartExecuteSupport.start(context, client, serverPort, stopWatch, logger);
			}

			supportExecuteFlow = singleServer || startExecuteSurpport;

			stopWatch.start("startWatch");
			logger.info("开启组件包信息监听...");
			ComponentPackListener.watch(client, true, supportExecuteFlow, true);
			logger.info("开启组件信息监听...");
			ComponentListener.watch(client);
			logger.info("开启流程信息监听...");
			FlowListener.watch(client);
			logger.info("开启流程执行配置信息监听...");
			FlowExecConfigListener.watch(client);

			// 单机执行时，不支持容器监听及心跳检查
			if (!singleServer) {
				logger.info("开启执行容器监听...");
				ExecuteContainerListener.start(client);
				stopWatch.stop();

				logger.info("开启执行容状态检查线程...");
				stopWatch.start("HeartbeatMonitor");
				HeartbeatMonitor.getInstance().start();
				stopWatch.stop();
			}

			JarFileArchiveJspSupport.doSupport(context, stopWatch);

			logger.info("流程制作管理平台服务启动成功.");
			logger.info(stopWatch.prettyPrint());
		} catch (Exception e) {
			logger.error("流程制作管理平台服务启动失败：" + e.getMessage(), e);
			try {
				if (client != null) {
					client.close();
				}
			} catch (Exception e1) {
			}
			System.exit(-1);
			return;
		}
	}

	@RequestMapping(value = "/", name = "测试")
	public String home() {
		return "欢迎使用环球合一流程管理系统!";
	}

	@RequestMapping(value = "/ping", name = "ping")
	public String ping() {
		return "ping";
	}

	/**
	 * 设置上传文件的大小
	 *
	 * @return
	 */
	@Bean
	public MultipartConfigElement multipartConfigElement() {
		MultipartConfigFactory factory = new MultipartConfigFactory();
		factory.setMaxFileSize(10 * 1024L * 1024L);
		return factory.createMultipartConfig();
	}

	public static EmbeddedServletContainer embeddedServletContainer = null;

	@Override
	public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {
		serverPort = event.getEmbeddedServletContainer().getPort();
		embeddedServletContainer = event.getEmbeddedServletContainer();
	}
}
