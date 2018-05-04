/**
 * 
 */
package com.guttv.pm.container.onstart;

import org.springframework.context.ApplicationContext;

import com.guttv.rpc.server.accept.CommandAcceptor;
import com.guttv.rpc.server.bootstrap.BootstrapCommandAcceptor;
import com.guttv.rpc.server.executor.RpcRequestExecutor;
import com.guttv.rpc.server.executor.SpringContextExecutor;

/**
 * @author Peter
 *
 */
public class StartBootstrapRpcAcceptor {

	/**
	 * 启动bootstrap类型的RPC接收
	 * 
	 * @param context
	 * @param port
	 */
	public static void start(ApplicationContext context, int port) {

		RpcRequestExecutor executor = new SpringContextExecutor(context);

		CommandAcceptor acceptor = null;
		try {

			acceptor = new BootstrapCommandAcceptor(port, executor);

			acceptor.accept();

		} catch (Exception e) {
			try {
				acceptor.close();
			} catch (Exception e1) {
			}
		}
	}
}
