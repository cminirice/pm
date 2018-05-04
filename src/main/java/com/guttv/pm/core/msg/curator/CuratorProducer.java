/**
 * 
 */
package com.guttv.pm.core.msg.curator;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.guttv.pm.core.msg.queue.AbstractCheckRuleProducer;
import com.guttv.pm.core.msg.queue.Queue;

/**
 * @author Peter
 *
 */
public class CuratorProducer  extends AbstractCheckRuleProducer{
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	private Queue queue = null;
	private String name = null;
	
	public CuratorProducer(String path) {
		this.setName(path);
		queue = CuratorQueueFactory.getQueue(path);
	}

	/*
	 * (non-Javadoc)
	 * @see com.guttv.oms.tp.core.queue.Producer#write(java.lang.Object)
	 */
	@Override
	public void write(Object data) throws Exception {
		if(data == null) return;
		checkConnection();
		queue.produce(data);
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
		if(queue != null) {
			queue.close();
			CuratorQueueFactory.removieQueue(this.name);
		}
	}
	
	@Override
	public int size() {
		return queue!=null?queue.size():0;
	}
	
	void checkConnection() {
		if(queue == null) {
			queue = CuratorQueueFactory.getQueue(this.getName());
		}
	}
}
