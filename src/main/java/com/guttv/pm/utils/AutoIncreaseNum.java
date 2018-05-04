/**
 * 
 */
package com.guttv.pm.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Peter
 *
 */
public class AutoIncreaseNum {

	private static final int INIT = 1;
	private static final AtomicInteger atomicNum = new AtomicInteger(INIT);
	
	//获取自动序号
	public static int getSequence() {
		return atomicNum.incrementAndGet();
	}
}
