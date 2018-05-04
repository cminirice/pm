/**
 * 
 */
package com.guttv.pm.core.msg;

import com.guttv.pm.core.msg.curator.CuratorProducer;
import com.guttv.pm.core.msg.nt.NativeProducer;
import com.guttv.pm.core.msg.queue.Producer;
import com.guttv.pm.core.msg.rabbit.RabbitProducer;
import com.guttv.pm.core.zk.ZookeeperHelper;

/**
 * @author Peter
 *
 */
public class ProducerFactory {
	//zookeeper 队列的前缀
	public static final String ZK_PRE = "zk://";
	
	//本地内存通道的前缀
	public static final String NATIVE_PRE = "native://";
	
	//Kafka通道的前缀
	public static final String KAFKA_PRE = "kafka://";
	
	//rabbit通道的前缀
	public static final String RABBIT_PRE = "rabbit://";

	public static Producer create(String path) throws Exception{
		if(path == null || path.trim().length() == 0) {
			return null;
		}
		
		if(path.startsWith(ZK_PRE)) {
			//zookeeper客户端
			String sub = path.substring(ZK_PRE.length());
			
			return new CuratorProducer(ZookeeperHelper.getRealPath(sub));
		}else if(path.startsWith(NATIVE_PRE)) {
			String sub = path.substring(NATIVE_PRE.length());
			return new NativeProducer(sub);
		}else if(path.startsWith(ProducerFactory.RABBIT_PRE)) {
			String sub = path.substring(ProducerFactory.RABBIT_PRE.length());
			return new RabbitProducer(sub);
		} else {
			throw new Exception("不支持的类型["+path+"]");
		}
	}
}
