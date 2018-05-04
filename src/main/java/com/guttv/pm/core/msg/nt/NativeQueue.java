/**
 * 
 */
package com.guttv.pm.core.msg.nt;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.guttv.pm.core.msg.queue.Queue;

/**
 * @author Peter
 *
 */
class NativeQueue implements Queue{
	protected Logger logger = LoggerFactory.getLogger("queue");

	/** 用安全队列实现的数据通道 */
	private BlockingQueue<Object> queue = new LinkedBlockingQueue<Object>();
	
	private int maxLength = 50000;
	
	private String path = null;
	
	NativeQueue(String p){
		this.path = p;
	}

	/*
	 * (non-Javadoc)
	 * @see com.guttv.oms.tp.core.queue.Queue#produce(java.lang.Object)
	 */
	@Override
	public void produce(Object data) throws Exception {
		if(queue.size() > maxLength) {
			logger.error("内存通道["+path+"]已经达到上限["+maxLength+"],丢弃数据：" + data);
			return;
		}
		queue.add(data);
		logger.debug("向通道["+path+"]发送数据：" + data);
	}

	/**
	 * 处理数据，如果没有数据，返回空，不阻塞
	 */
	@Override
	public Object consumer() throws Exception {
		Object obj = queue.poll();
		if(obj != null) {
			logger.debug("从通道["+path+"]取出数据：" + obj);
		}
		return obj;
	}

	/**
	 * 处理数据，如果没有数据，等待超时时间。超时单位：毫秒
	 */
	@Override
	public Object consumer(int timeout) throws Exception {
		Object obj = queue.poll(timeout, TimeUnit.MILLISECONDS);
		if(obj != null) {
			logger.debug("从通道["+path+"]取出数据：" + obj);
		}
		return obj;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	@Override
	public int size() {
		return queue.size();
	}

	@Override
	public void close() throws IOException {
	}
}
