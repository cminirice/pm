/**
 * 
 */
package com.guttv.rpc.common;

import java.util.UUID;

/**
 * @author Peter
 *
 */
public class RandomStringUtils {

	/**
	 * 返回去掉横线的UUID
	 * @return
	 */
	public static String uuid() {
		return UUID.randomUUID().toString().replace("-", "");
	}
}
