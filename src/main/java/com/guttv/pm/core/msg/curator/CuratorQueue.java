/**
 * 
 */
package com.guttv.pm.core.msg.curator;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.queue.SimpleDistributedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.guttv.pm.core.msg.queue.Queue;
import com.guttv.pm.core.zk.CuratorClientFactory;

/**
 * @author Peter
 *
 */
class CuratorQueue implements Queue{
	protected Logger logger = LoggerFactory.getLogger("queue");
	private String path = null;
	
	private SimpleDistributedQueue queue = null;
	
	private final DataPackageSerializer parser = new DataPackageSerializer();
	
	private CuratorFramework client = null;
	
	CuratorQueue(String p) {
		this.path = p;
		CuratorFramework client = CuratorClientFactory.getClient();
		client.start();
		this.client = client;
		queue = new SimpleDistributedQueue(client,path);
	}
	
	/**
	 * 生产数据，往队列里发送数据
	 * @param data
	 * @throws Exception
	 */
	public void produce(Object data) throws Exception{
		queue.offer(parser.serialize(data));
		logger.debug("向通道["+path+"]发送数据：" + data);
	}
	
	/**
	 * 消费数据，如果没有数据，返回空，不阻塞
	 * @return
	 * @throws Exception
	 */
	public Object consumer() throws Exception{
		byte[] bytes = queue.poll();
		if(bytes == null) return null;
		Object obj = parser.deserialize(bytes);
		logger.debug("从通道["+path+"]取出数据：" + obj);
		return obj;
	}
	
	/**
	 * 消费数据，如果没有数据，等待millisecond毫秒 如果还没有数据，返回空
	 * @return
	 * @throws Exception
	 */
	public Object consumer(int millisecond) throws Exception{
		byte[] bytes = queue.poll(millisecond,TimeUnit.MILLISECONDS);
		if(bytes == null) return null;
		
		Object obj = parser.deserialize(bytes);
		logger.debug("从通道["+path+"]取出数据：" + obj);
		return obj;
	}

	/*
	 * (non-Javadoc)
	 * @see com.guttv.oms.tp.core.queue.Queue#size()
	 */
	@Override
	public int size() {
		 List<String> nodes = null;
		 try {
			 nodes = client.getChildren().forPath(path);
		 }catch(Exception e) {
			 return 0;
		 }
		 return nodes==null?0:nodes.size();
	}
	@Override
	public void close() throws IOException {
		client.close();
	}
}
