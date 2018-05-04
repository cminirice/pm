/**
 * 
 */
package com.guttv.pm.core.task;

import java.lang.reflect.Method;

/**
 * @author Peter
 *
 */
public interface TaskProxy {

	Object getProxy();

	void setProxy(Object proxy);

	Method getMethod();

	void setMethod(Method method);
	
	Method getInitMethod();

	void setInitMethod(Method initMethod);

	Method getCloseMethod() ;
	
	void setCloseMethod(Method closeMethod);
}
