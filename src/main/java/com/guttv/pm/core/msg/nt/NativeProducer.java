/**
 * 
 */
package com.guttv.pm.core.msg.nt;

import java.io.IOException;

import com.guttv.pm.core.msg.queue.AbstractCheckRuleProducer;
import com.guttv.pm.core.msg.queue.Queue;

/**
 * @author Peter
 *
 */
public class NativeProducer extends AbstractCheckRuleProducer {
	
	private Queue queue = null;
	private String name = null;
	public NativeProducer(String path) {
		this.setName(path);
		queue = NativeQueueFactory.getQueue(path);
	}

	/* (non-Javadoc)
	 * @see com.guttv.oms.tp.core.queue.Producer#write(java.lang.Object)
	 */
	@Override
	public void write(Object data) throws Exception {
		queue.produce(data);
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
	public int size() {
		return queue.size();
	}
}
