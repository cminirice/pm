/**
 * 
 */
package com.guttv.pm.util;

import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * @author Peter
 *
 */
public class ReflectTest {

	public static void main(String[]a) throws Exception{
		Method method = ReflectTest.class.getDeclaredMethod("ff",Object.class);
		Class clz = method.getReturnType();
		System.out.println("void".equalsIgnoreCase(clz.getName()));
		Object obj = new HashMap<String,String>();
		
		Object ret = method.invoke(Class.forName("com.guttv.pm.util.ReflectTest").newInstance(), clz);
		System.out.println(ret);
	}
	
	public void ff(Object obj) {
		return ;
	}
}
