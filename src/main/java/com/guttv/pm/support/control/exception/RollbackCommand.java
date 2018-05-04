/**
 * 
 */
package com.guttv.pm.support.control.exception;

/**
 * @author Peter
 *
 */
public class RollbackCommand extends ExecuteControlCommand {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see com.guttv.pm.support.control.exception.ExecuteControlException#getCode()
	 */
	@Override
	public int getCode() {
		return CommandCode.ROLLBACK;
	}

}
