/**
 * 
 */
package com.guttv.pm.core.msg.queue;

/**
 * @author Peter
 *
 */
public abstract class AbstractDispatchRule {

	/**
	 * 检测data是否符合分发规则
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public abstract boolean check(Object data) throws Exception;
}
