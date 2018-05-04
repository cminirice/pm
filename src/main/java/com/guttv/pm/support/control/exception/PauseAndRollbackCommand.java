/**
 * 
 */
package com.guttv.pm.support.control.exception;

/**
 * @author Peter
 *
 */
public class PauseAndRollbackCommand extends ExecuteControlCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see com.guttv.pm.support.control.exception.ExecuteControlCommand#getCode()
	 */
	@Override
	public int getCode() {
		return CommandCode.PAUSE_ROLLBACK;
	}
}
