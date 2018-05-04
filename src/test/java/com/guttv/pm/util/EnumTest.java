/**
 * 
 */
package com.guttv.pm.util;

import org.apache.commons.lang3.EnumUtils;

import com.guttv.pm.utils.Enums.YesOrNo;

/**
 * @author Peter
 *
 */
public class EnumTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		YesOrNo yes = EnumUtils.getEnum(YesOrNo.class, "YES");
		System.out.println(yes);
	}
}
