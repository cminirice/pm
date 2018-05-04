/**
 * 
 */
package com.guttv.pm.core.msg.nt;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.guttv.pm.core.msg.queue.Queue;

/**
 * @author Peter
 *
 */
public class NativeQueueFactory {
	private final static Map<String,Queue> queueMap = new HashMap<String,Queue>();
	
	/**
	 * 取得本地队列
	 * @param path
	 * @return
	 */
	static synchronized Queue getQueue(String path) {
		Queue queue = queueMap.get(path);
		if(queue == null) {
			queue = new NativeQueue(path);
			queueMap.put(path, queue);
		}
		return queue;
	}
	
	private static final String tab = "\t";
	private static final String newLine = "\n";
	/**
	 * 取得队列的信息，如果路径为空，返回所有队列的信息
	 * @param path
	 * @return
	 */
	public static String getQueueInfo(String path) {
		if(queueMap.size() == 0) {
			return "no queue";
		}
		Queue queue = null;
		StringBuilder sb = new StringBuilder("native queue").append(newLine);
		if(path == null || path.trim().length() == 0) {
			//发送的队列为空的情况 
			Set<String> keys = queueMap.keySet();
			
			for(String key : keys) {
				queue = queueMap.get(key);
				if(queue != null) {
					sb.append(key).append(tab).append(queue.size()).append(newLine);
				}
			}
		}else {
			//发送的队列不为空时，返回该队列的信息
			queue = queueMap.get(path);
			if(queue != null) {
				sb.append(path).append(tab).append(queue.size()).append(newLine);
			}else {
				sb.append("no queue named '").append(path).append("'");
			}
		}
		return sb.toString();
	}
}
