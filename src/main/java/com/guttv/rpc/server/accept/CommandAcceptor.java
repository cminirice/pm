/**
 * 
 */
package com.guttv.rpc.server.accept;

import java.io.Closeable;

/**
 * 获取命令接口
 * @author Peter
 *
 */
public interface CommandAcceptor extends Closeable{

	/**
	 * 接收一个命令
	 * @param timeout
	 * @return
	 * @throws Exception
	 */
	void accept() throws Exception;
}
