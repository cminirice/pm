/**
 * 
 */
package com.guttv.pm.core.task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.guttv.pm.core.zk.SingletonLock;
import com.guttv.pm.support.control.exception.CommandCode;
import com.guttv.pm.support.control.exception.ExecuteControlCommand;
import com.guttv.pm.utils.Enums.ComponentNodeStatus;

/**
 * @author Peter
 *
 */
public abstract class AbstractRecycleTask extends AbstractTask {

	private int readTimeout = 5000;

	private long period = 0;

	private String singleLockPath = null;
	private long lockRetryTime = 60 * 1000;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.guttv.oms.tp.core.task.AbstractTask#dispose()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public final void dispose() throws Exception {

		Object data = null;
		Object result = null;

		SimpleDateFormat format = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
		long start = System.currentTimeMillis();
		// 如果没有停止，一直循环
		try {
			while (!this.stop) {
				doHeartBeat();
				data = null;
				try {
					// 如果设置了暂停，sleep pauseTime 秒
					if (this.isPause()) {
						int diff = (int) ((System.currentTimeMillis() - pauseStart) / 60000);
						if (diff > pauseMinite) {
							pauseMinite = diff;
							logger.debug("任务[" + this.getName() + "]被暂停[" + pauseMinite + "]分钟，开始暂停时间："
									+ format.format(new Date(pauseStart)));
						}

						Thread.sleep(pauseTime);
						continue;
					}

					if (!tryLock()) {
						logger.info("[" + this.getId() + "][" + this.getName() + "] 没有获取运行锁:"
								+ (lock == null ? singleLockPath : lock.getPath()));
						Thread.sleep(lockRetryTime);
						continue;
					}

					if (this.isNeedRead()) {
						// 如果子类不是从通道里读取数据处理，需要重写readData 方法 空实现即可
						// 如果子类不需要向下层节点发送数据，需要重写writeData 方法，空实现即可

						data = this.readData(readTimeout);

						if (data == null) { // 防止程序空跑，占用CPU太多
							Thread.sleep(100);
							continue;
						}
					}

					// 记录执行开始时间
					start = System.currentTimeMillis();

					if (data != null) {
						logger.info(this.getName() + " 收到数据：" + data);
					}

					// 执行逻辑
					try {
						result = dispose(data);
					} catch (Exception e) {
						List<Throwable> throwList = ExceptionUtils.getThrowableList(e);
						logger.error(e.getMessage(), e);
						logger.error("*****************************");
						Throwable throwable = (throwList.get(throwList.size() - 1));
						logger.error(throwable.getMessage(), throwable);
						logger.error("*****************************");
						throw throwList.get(throwList.size() - 1);
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
					this.commit(data);

					result = null;

					// 如果正常运行，更新一下状态
					if (this.getComponentNode() != null
							&& ComponentNodeStatus.ERROR.equals(this.getComponentNode().getStatus()) ) {
						this.getComponentNode().setStatus(ComponentNodeStatus.RUNNING);
						this.getComponentNode().setStatusDesc("恢复正常运行");
					}

					// 如果设置了运行周期
					long sleep = period - (System.currentTimeMillis() - start);

					logger.debug("用时：" + (System.currentTimeMillis() - start));
					//
					Thread.sleep(sleep > 0 ? sleep : 1); // 防止程序配置错误导致系统死循环

				} catch (ExecuteControlCommand command) {
					int code = command.getCode();
					logger.warn("收到控制指令[" + code + "]：" + command.getMessage(), command);
					if ((code & CommandCode.PAUSE) == CommandCode.PAUSE) {
						this.setPause(true);
					}
					if ((code & CommandCode.ROLLBACK) == CommandCode.ROLLBACK) {
						this.fallback(data);
					}
				} catch (Exception e) {
					logger.error("任务[" + this.getName() + "]处理异常：" + e.getMessage(), e);

					if (this.getComponentNode() != null) {
						this.getComponentNode().setStatus(ComponentNodeStatus.ERROR);
						this.getComponentNode().setStatusDesc("出现异常：" + e.getMessage());
					}

					// 如果处理出现错误，发送到错误通道
					if (data != null) {
						Map<String,Object>  errorData = new HashMap<String,Object>();
						errorData.put("data", data);
						errorData.put("error",e);
						this.writerError(errorData);
						this.commit(data);
					}
					// this.fallback(obj); //不再回滚，把错误数据发到异常队列，避免有影响

					// 处理告警
					this.writeAlarm(ExceptionUtils.getStackTrace(e));

					Thread.sleep(5000);
				}

			}
		} catch (Throwable e) { // 到这里的认识不能再执行周期函数

			logger.error("任务[" + this.getName() + "]处理严重错误：" + e.getMessage(), e);

			// 如果处理出现错误，发送到错误通道
			if (data != null) {
				Map<String,Object>  errorData = new HashMap<String,Object>();
				errorData.put("data", data);
				errorData.put("error",e);
				this.writerError(errorData);
				this.commit(data);
			}

			// 向上抛
			throw new Exception(e);
		} finally {
			unlock();
			logger.info("任务[" + this.getName() + "]结束" + this.getClass());
		}
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public abstract Object dispose(Object data) throws Exception;

	// 本层没有需要关闭的
	public void close() {
		super.close();
	}

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

	// 暂停时 sleep 的时间
	private long pauseTime = 1000;

	// 暂停的开始时间
	private long pauseStart = 0;

	// 暂停的分钟数，用来打印日志用
	private int pauseMinite = 0;

	// 暂停
	private boolean pause = false;
	// 停止
	private boolean stop = false;

	public boolean isPause() {
		return pause;
	}

	public void setPause(boolean pause) {
		if (pause) { // 开始暂停
			pauseStart = System.currentTimeMillis();
			this.getComponentNode().setStatus(ComponentNodeStatus.PAUSE);
		} else { // 暂停结束
			pauseStart = 0;
			this.getComponentNode().setStatus(ComponentNodeStatus.RUNNING);
		}
		pauseMinite = 0;
		this.pause = pause;
	}

	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		if (!this.stop && stop) {
			this.getComponentNode().setStatus(ComponentNodeStatus.STOPPED);
		}
		this.stop = stop;
	}

	public long getPeriod() {
		return period;
	}

	public void setPeriod(long period) {
		this.period = period;
	}

	public long getPauseTime() {
		return pauseTime;
	}

	public void setPauseTime(long pauseTime) {
		this.pauseTime = pauseTime;
	}

	public String getSingleLockPath() {
		return singleLockPath;
	}

	public void setSingleLockPath(String singleLockPath) {
		this.singleLockPath = singleLockPath;
	}

	public long getLockRetryTime() {
		return lockRetryTime;
	}

	public void setLockRetryTime(long lockRetryTime) {
		this.lockRetryTime = lockRetryTime;
	}
}
