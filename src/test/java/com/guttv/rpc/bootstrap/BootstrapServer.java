/**
 * 
 */
package com.guttv.rpc.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import com.guttv.rpc.common.L;
import com.guttv.rpc.server.accept.CommandAcceptor;
import com.guttv.rpc.server.bootstrap.BootstrapCommandAcceptor;
import com.guttv.rpc.server.executor.RpcRequestExecutor;
import com.guttv.rpc.server.executor.SpringContextExecutor;

/**
 * @author Peter
 *
 */
@EnableAutoConfiguration
@ComponentScan("com.guttv.rpc.client")
public class BootstrapServer {

	public static void main(String[]a) {
		ApplicationContext context = SpringApplication.run(BootstrapServer.class, a);

		try {
			RpcRequestExecutor executor = new SpringContextExecutor(context);

			CommandAcceptor acceptor = null;
			try {
				acceptor = new BootstrapCommandAcceptor(9876, executor);
				
				acceptor.accept();
			} catch (Exception e) {
				acceptor.close();
			}

		} catch (Throwable error) {
			L.logger.error(error.getMessage(), error);
			System.exit(-1);
		} finally {

		}
	}
}
