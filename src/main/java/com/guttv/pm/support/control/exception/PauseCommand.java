/**
 * 
 */
package com.guttv.pm.support.control.exception;

/**
 * 暂停，系统收到此命令时，会把周期执行的流程暂停执行
 * 
 * @author Peter
 *
 */
public class PauseCommand extends ExecuteControlCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public int getCode() {
		return CommandCode.PAUSE;
	}

}
