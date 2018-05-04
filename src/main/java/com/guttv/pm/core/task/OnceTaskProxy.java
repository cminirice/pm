/**
 * 
 */
package com.guttv.pm.core.task;

import java.io.Closeable;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.guttv.pm.core.zk.SingletonLock;
import com.guttv.pm.support.control.exception.RollbackCommand;

/**
 * @author Peter
 *
 */
public class OnceTaskProxy extends AbstractTask implements TaskProxy {

	private Object proxy = null;

	private Method method = null;

	private Method initMethod = null;

	private Method closeMethod = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.guttv.pm.frame.task.AbstractTask#dispose()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void dispose() throws Exception {

		Object obj = null;
		Object result = null;

		long start = 0;
		// 如果没有停止，一直循环
		try {
			doHeartBeat();
			try {

				if (!tryLock()) {
					logger.info("[" + this.getId() + "][" + this.getName() + "] 没有获取运行锁:"
							+ (lock == null ? singleLockPath : lock.getPath()));
					return;
				}

				// 记录执行开始时间
				start = System.currentTimeMillis();

				// 如果需要读数据，则把参数传进去
				if (this.isNeedRead()) {

					obj = this.readData(readTimeout);

					if (obj != null) {
						logger.info(this.getName() + " 收到数据：" + obj);
					}

					result = method.invoke(proxy, obj);

				} else {
					// 否则 不传参数
					result = method.invoke(proxy);
				}

				if (result != null) {
					logger.info(this.getName() + " 处理结果：" + result);
				}

				if (this.isNeedWrite() && result != null) {
					// 把结果数据发到下个节点
					// 如果是List集合，认为是多值返回，顺序下发
					if (result instanceof List) {
						List<Object> list = (List<Object>) result;
						for (Object one : list) {
							this.writeData(one);
						}
					} else {
						this.writeData(result);
					}
				}

				// 提交事务
				this.commit(obj);

				logger.debug("用时：" + (System.currentTimeMillis() - start));

			} catch (RollbackCommand rollback) {
				logger.error("收到需要回滚的指令：" + rollback.getMessage(), rollback);

				// 如果处理出现错误，发送到错误通道
				if (obj != null) {
					this.writerError(obj);
				}

				// 回滚请求
				this.fallback(obj);
			} catch (Throwable e) {
				logger.error("任务[" + this.getName() + "]处理严重错误：" + e.getMessage(), e);

				// 如果处理出现错误，发送到错误通道
				if (obj != null) {
					this.writerError(obj);
				}

				// 除了有回滚请求的，其它都要提交，（防止有错误数据，影响其它数据执行）
				this.commit(obj);

				throw e;
			}
		} finally {
			unlock();
			proxy = null;
			method = null;
		}

		logger.info("任务[" + this.getName() + "]结束" + this.getClass());
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

	private String singleLockPath = null;

	private SingletonLock lock = null;

	public boolean tryLock() {
		if (StringUtils.isBlank(singleLockPath)) {
			return true;
		}

		if (lock == null) {
			lock = new SingletonLock(singleLockPath);
		}
		return lock.lock();
	}

	public void unlock() {
		if (lock != null) {
			lock.unlock();
		}
	}

	// 如果此处配置为false,根据情况需要配置period属性并且尽量不为0，防止系统死循环
	private boolean needRead = true;

	private boolean needWrite = true;

	// 如果需要读数据，该值为读取超时时间
	private int readTimeout = 5000;

	public String getSingleLockPath() {
		return singleLockPath;
	}

	public void setSingleLockPath(String singleLockPath) {
		this.singleLockPath = singleLockPath;
	}

	public boolean isNeedRead() {
		return needRead;
	}

	public void setNeedRead(boolean needRead) {
		this.needRead = needRead;
	}

	public boolean isNeedWrite() {
		return needWrite;
	}

	public void setNeedWrite(boolean needWrite) {
		this.needWrite = needWrite;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
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
