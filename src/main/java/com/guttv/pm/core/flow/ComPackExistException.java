/**
 * 
 */
package com.guttv.pm.core.flow;

/**
 * @author Peter
 *
 */
public class ComPackExistException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5502600234182925758L;

	public ComPackExistException() {
		super();
	}

	public ComPackExistException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ComPackExistException(String message, Throwable cause) {
		super(message, cause);
	}

	public ComPackExistException(String message) {
		super(message);
	}

	public ComPackExistException(Throwable cause) {
		super(cause);
	}

}
