/**
 * 
 */
package com.guttv.pm.core.rpc.impl;

import org.springframework.stereotype.Component;

import com.guttv.pm.core.rpc.Ping;

/**
 * @author Peter
 *
 */
@Component
public class JustPing implements Ping {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.guttv.pm.common.rpc.Ping#ping()
	 */
	@Override
	public void ping() {
	}

	/*
	 * (non-Javadoc)
	 * @see com.guttv.pm.common.rpc.Ping#sayHello(java.lang.String)
	 */
	public String sayHello(String name) {
		return "Hello " + name;
	}

}
