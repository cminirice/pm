/**
 * 
 */
package com.guttv.pm.support.control.exception;

/**
 * @author Peter
 *
 */
public interface CommandCode {
	
	/**
	 * 暂停
	 */
	int PAUSE = 8;

	/**
	 * 回滚数据
	 */
	int ROLLBACK = 16;

	/**
	 * 暂停并且回滚
	 */
	int PAUSE_ROLLBACK = PAUSE | ROLLBACK;
	
}
