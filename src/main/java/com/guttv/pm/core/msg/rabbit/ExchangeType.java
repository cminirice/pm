/**
 * 
 */
package com.guttv.pm.core.msg.rabbit;

import org.apache.commons.lang3.EnumUtils;

/**
 * @author Peter
 *
 */
public enum ExchangeType {

	DIRECT("direct"),FANOUT("fanout"),TOPIC("topic"),HEADERS("headers");
	private String value = null;
	private ExchangeType(String value) {
		this.value = value;
	}
	public String getValue() {
		return value;
	}
	
	public static void main(String[]a) {
		ExchangeType type = EnumUtils.getEnum(ExchangeType.class, "DIRECT");
		System.out.println(type);
	}
}
