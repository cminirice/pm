/**
 * 
 */
package com.guttv.pm.core.task;

import java.io.Closeable;
import java.lang.reflect.Method;

import org.apache.commons.io.IOUtils;

/**
 * @author Peter
 *
 */
public class RecycleTaskProxy extends AbstractRecycleTask implements TaskProxy {

	private Object proxy = null;

	private Method method = null;

	private Method initMethod = null;

	private Method closeMethod = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.guttv.pm.frame.task.AbstractRecycleTask#dispose(java.lang.Object)
	 */
	@Override
	public Object dispose(Object data) throws Exception {

		Object returnValue = null;

		// 如果需要读数据，则把参数传进去
		if (this.isNeedRead()) {
			returnValue = method.invoke(proxy, data);
		} else {
			// 否则 不传参数
			returnValue = method.invoke(proxy);
		}

		return returnValue;
	}

	// 初始化方法
	protected void init() throws Exception {
		if (initMethod != null) {
			logger.debug("调用初始化方法：" + initMethod.getName());
			initMethod.invoke(proxy);
		}
	}

	// 本方法需要递层向上层关闭
	public void close() {
		if (proxy instanceof Closeable) {
			IOUtils.closeQuietly((Closeable) proxy);
		}

		if (closeMethod != null) {
			try {
				closeMethod.invoke(proxy);
			} catch (Exception e) {
				logger.error("组件[" + this.getComponentClz() + "]调用关闭方法[" + closeMethod.getName() + "]时异常：" + e.getMessage(), e);
			}
		}
		
		proxy = null;
		method = null;
		initMethod = null;
		closeMethod = null;
		
		super.close();
	}

	public Object getProxy() {
		return proxy;
	}

	public void setProxy(Object proxy) {
		this.proxy = proxy;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public Method getInitMethod() {
		return initMethod;
	}

	public void setInitMethod(Method initMethod) {
		this.initMethod = initMethod;
	}

	public Method getCloseMethod() {
		return closeMethod;
	}

	public void setCloseMethod(Method closeMethod) {
		this.closeMethod = closeMethod;
	}
}
