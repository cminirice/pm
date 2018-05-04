/**
 *
 */
package com.guttv.pm.container;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.guttv.pm.container.onstart.StartExecuteSupport;
import com.guttv.pm.core.cache.ConfigCache;
import com.guttv.pm.core.zk.CuratorClientFactory;
import com.guttv.pm.core.zk.DistributedSequence;
import com.guttv.pm.core.zk.PathConstants;
import com.guttv.pm.core.zk.ZookeeperHelper;
import com.guttv.pm.core.zk.listener.ComponentPackListener;
import com.guttv.pm.utils.Constants;
import com.guttv.pm.utils.Utils;
import com.guttv.rpc.common.CheckPortUtil;
import com.guttv.rpc.common.FindAvailablePort;

/**
 * @author Peter
 *
 */
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
@SpringBootApplication
@RestController
@ComponentScan(value = "com.guttv.pm.core,com.guttv.pm.container")
public class FlowExecuteContainerMain
		implements EmbeddedServletContainerCustomizer, ApplicationListener<EmbeddedServletContainerInitializedEvent> {

	public static ApplicationContext context;

	public static int serverPort = -1;

	public static EmbeddedServletContainer embeddedServletContainer = null;

	@Autowired
	private ConfigCache config = null;

	public static void main(String[] args) throws Exception {
		CuratorFramework client = null;

		try {

			StopWatch stopWatch = new StopWatch();

			logger.info("将要启动执行容器...");
			stopWatch.start("server");
			context = SpringApplication.run(FlowExecuteContainerMain.class, args);
			stopWatch.stop();

			logger.info("设置系统属性...");
			stopWatch.start("initConfigCache");
			ConfigCache.init(context);
			stopWatch.stop();

			Runtime.getRuntime().addShutdownHook(new ContainerShutdownHook());

			stopWatch.start("initCurator");
			client = CuratorClientFactory.getClient();
			try {
				client.start();
			} catch (Exception e) {
				logger.error("启动ZK连接异常：" + e.getMessage(), e);
				System.exit(-1);
				return;
			}
			stopWatch.stop();

			logger.info("初始化自增序号...");
			DistributedSequence.getInstance().init(client, ZookeeperHelper.getRealPath(PathConstants.SEQUENCE_PATH));

			// 启动支持执行流程功能
			StartExecuteSupport.start(context, client, serverPort, stopWatch, logger);

			stopWatch.start("startWatch");
			logger.info("开启组件包信息监听...");
			ComponentPackListener.watch(client, false, true, false);
			stopWatch.stop();

			logger.info("执行容器启动成功，用时[" + stopWatch.getTotalTimeSeconds() + "]");
			logger.info(stopWatch.prettyPrint());

		} catch (Exception e) {
			logger.error("执行容器启动失败：" + e.getMessage(), e);
			try {
				client.close();
			} catch (Exception e1) {
			}
			System.exit(-1);
			return;
		}
	}

	@Override
	public void customize(ConfigurableEmbeddedServletContainer server) {
		String strPort = config.getProperty(Constants.SERVER_PORT, null);
		int port = -1;
		if (StringUtils.isNotBlank(strPort)) {
			int tmpPort = Integer.parseInt(strPort);
			if (CheckPortUtil.isAvailable(tmpPort)) {
				port = tmpPort;
			}
		}

		// 如果默认的端口不能用，就从范围里随便找一个
		if (port < 0) {
			String str = config.getProperty(Constants.SERVER_PORT_SCOPE, null);
			int[] scope = Utils.getPortScope(str);
			port = FindAvailablePort.find(scope[0], scope[1]);
		}

		server.setPort(port);
		System.setProperty(Constants.SERVER_PORT, String.valueOf(port));
		logger.info("启动端口是：" + port);
	}

	protected static final Logger logger = LoggerFactory.getLogger(FlowExecuteContainerMain.class);

	@RequestMapping(value = "/pingContainer", name = "ping")
	public String ping() {
		return "ping";
	}

	@Override
	public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {
		serverPort = event.getEmbeddedServletContainer().getPort();
		embeddedServletContainer = event.getEmbeddedServletContainer();
	}
}
