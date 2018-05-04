/**
 * 
 */
package com.guttv.pm.core.msg.curator;

import java.io.IOException;

import com.guttv.pm.core.msg.queue.Consumer;
import com.guttv.pm.core.msg.queue.Queue;


/**
 * @author Peter
 *
 */
public class CuratorConsumer implements Consumer{
	
	public CuratorConsumer(String path) {
		this.setName(path);
		this.queue = CuratorQueueFactory.getQueue(path);
	}

	private Queue queue = null;
	private String name = null;
	
	/*
	 * (non-Javadoc)
	 * @see com.guttv.oms.tp.core.queue.Consumer#read()
	 */
	@Override
	public Object read() throws Exception {
		checkConnection();
		return queue.consumer();
	}

	/*
	 * (non-Javadoc)
	 * @see com.guttv.oms.tp.core.queue.Consumer#reader(int)
	 */
	@Override
	public Object read(int wait) throws Exception {
		checkConnection();
		return queue.consumer(wait);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public void close() throws IOException {
		if(queue !=null) {
			queue.close();
			CuratorQueueFactory.removieQueue(this.name);
		}
	}

	@Override
	public void fallback(Object data) throws Exception {
		if(queue != null) {
			queue.produce(data);
		}
	}
	
	@Override
	public int size() {
		return queue!=null?queue.size():0;
	}

	@Override
	public void commit(Object data) throws Exception {
	}
	
	void checkConnection() {
		if(queue == null) {
			queue = CuratorQueueFactory.getQueue(this.getName());
		}
	}
}
