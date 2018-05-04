/**
 * 
 */
package com.guttv.pm.core.msg;

import com.guttv.pm.core.msg.curator.CuratorConsumer;
import com.guttv.pm.core.msg.nt.NativeConsumer;
import com.guttv.pm.core.msg.queue.Consumer;
import com.guttv.pm.core.msg.rabbit.RabbitConsumer;
import com.guttv.pm.core.zk.ZookeeperHelper;

/**
 * @author Peter
 *
 */
public class ConsumerFactory {

	public static Consumer create(String path) throws Exception{
		if(path == null || path.trim().length() == 0) {
			return null;
		}
		
		if(path.startsWith(ProducerFactory.ZK_PRE)) {
			//zookeeper前缀
			String sub = path.substring(ProducerFactory.ZK_PRE.length());
			return new CuratorConsumer(ZookeeperHelper.getRealPath(sub));
		}else if(path.startsWith(ProducerFactory.NATIVE_PRE)) {
			String sub = path.substring(ProducerFactory.NATIVE_PRE.length());
			return new NativeConsumer(sub);
		}else if(path.startsWith(ProducerFactory.RABBIT_PRE)) {
			String sub = path.substring(ProducerFactory.RABBIT_PRE.length());
			return new RabbitConsumer(sub);
		}else {
			throw new Exception("不支持的类型["+path+"]");
		}
	}
}
