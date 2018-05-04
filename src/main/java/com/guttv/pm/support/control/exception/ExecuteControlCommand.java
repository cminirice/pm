/**
 * 
 */
package com.guttv.pm.support.control.exception;

/**
 * 
 * 二次开发组件的时候用到的控制流程运行的命令
 * @author Peter
 *
 */
public abstract class ExecuteControlCommand extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 获取控制编码
	 * 
	 * @return
	 */
	public abstract int getCode();
}
