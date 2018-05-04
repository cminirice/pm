/**
 * 
 */
package com.guttv.pm.core.msg.nt;

import java.io.IOException;

import com.guttv.pm.core.msg.queue.Consumer;
import com.guttv.pm.core.msg.queue.Queue;

/**
 * @author Peter
 *
 */
public class NativeConsumer implements Consumer {
	public NativeConsumer(String path) {
		this.setName(path);
		queue = NativeQueueFactory.getQueue(path);
	}
	private Queue queue = null;
	private String name = null;

	/* (non-Javadoc)
	 * @see com.guttv.oms.tp.core.queue.Consumer#read()
	 */
	@Override
	public Object read() throws Exception {
		return queue.consumer();
	}

	/* (non-Javadoc)
	 * @see com.guttv.oms.tp.core.queue.Consumer#read(int)
	 */
	@Override
	public Object read(int wait) throws Exception {
		return queue.consumer(wait);
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
	@Override
	public void close() throws IOException {
		queue.close();
	}

	@Override
	public void fallback(Object data) throws Exception {
		queue.produce(data);
	}
	
	@Override
	public int size() {
		return queue.size();
	}

	@Override
	public void commit(Object data) throws Exception {
	}
}
