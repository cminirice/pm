/**
 * 
 */
package com.guttv.rpc.client;

import org.springframework.stereotype.Controller;

/**
 * @author Peter
 *
 */
@Controller
public class HelloWorldImpl implements HelloWorld{

	@Override
	public String sayHello(String name) {
		return "minimice say hello to " + name;
	}

}
