/**
 * 
 */
package com.guttv.pm.core.task;

import java.io.Closeable;
import java.lang.reflect.Method;

import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;

/**
 * @author Peter
 *
 */
public class RestTaskProxy extends AbstractRecycleTask implements TaskProxy {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.guttv.pm.core.task.AbstractRecycleTask#dispose(java.lang.Object)
	 */
	@Override
	public Object dispose(Object data) throws Exception {
		logger.info("I'm alive!!!");
		return null;
	}

	public void init() throws Exception {
		if (initMethod != null) {
			logger.debug("调用初始化方法：" + initMethod.getName());
			initMethod.invoke(proxy);
		}
		
		// 初始化处理句柄
		initHandlerMehod();

		if (this.getPeriod() < miniPeriod) {
			this.setPeriod(miniPeriod);
		}
	}

	/**
	 * 初始化句柄
	 * 
	 * @throws Exception
	 */
	private void initHandlerMehod() throws Exception {
		Object comInstance = this.getProxy();
		Class<?> comClz = comInstance.getClass();
		RestController restAnn = comClz.getAnnotation(RestController.class);

		// 找到所有的rest接口，全部发布
		Method[] methods = comClz.getMethods();
		for (Method method : methods) {
			RequestMapping rm = method.getAnnotation(RequestMapping.class);
			if (rm != null) {
				// 如果有发布失败（接口重复的），close方法会把发布成功的卸载
				String[] values = rm.value();
				for (String p : values) {
					RestPathMapping.getInstance().cacheHandlerMethod(restAnn.value() + p,
							new HandlerMethod(comInstance, method), this);
				}
			}
		}
	}

	@Override
	public void setPause(boolean pause) {
		try {
			if (pause) {
				// 暂停的时候，就把所有的接口删除
				if (this.getProxy() != null) {
					RestPathMapping.getInstance().uncacheHandlerMethod(this.getProxy());
				}
			} else {
				// 初始化处理句柄
				initHandlerMehod();
			}
			super.setPause(pause);
		} catch (Exception e) {
			logger.error("处理[" + this.getComponentClz() + "][" + this.getNodeID() + "]暂停请求[" + pause + "]异常："
					+ e.getMessage(), e);
		}
	}

	public void setStop(boolean stop) {
		if (stop) {
			if (this.getProxy() != null) {
				RestPathMapping.getInstance().uncacheHandlerMethod(this.getProxy());
			}
		}
		super.setStop(stop);
	}

	// 此类任务的周期不易过短
	private long miniPeriod = 30000;

	// 回收资源
	public void close() {
		if (this.getProxy() != null) {
			RestPathMapping.getInstance().uncacheHandlerMethod(this.getProxy());
		}

		if (proxy instanceof Closeable) {
			IOUtils.closeQuietly((Closeable) proxy);
		}

		if (closeMethod != null) {
			try {
				closeMethod.invoke(proxy);
			} catch (Throwable e) {
				logger.error(
						"组件[" + this.getComponentClz() + "]调用关闭方法[" + closeMethod.getName() + "]时异常：" + e.getMessage(),
						e);
			}
		}

		proxy = null;
		method = null;
		initMethod = null;
		closeMethod = null;

		super.close();
	}

	private Object proxy = null;
	private Method method = null;
	private Method initMethod = null;
	private Method closeMethod = null;

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
