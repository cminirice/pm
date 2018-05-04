/**
 * 
 */
package com.guttv.pm.core.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.retry.RetryNTimes;

/**
 * @author Peter
 *
 */
public class DistributedSequence {

	private DistributedAtomicLong atomicLong = null;

	public synchronized void init(CuratorFramework client, String path) {
		if (atomicLong != null) {
			return;
		}
		atomicLong = new DistributedAtomicLong(client, path, new RetryNTimes(3, 1));
	}

	public long getNext() throws Exception {
		AtomicValue<Long> value = atomicLong.increment();
		return value.postValue();
	}

	private DistributedSequence() {
	}

	private static DistributedSequence instance = new DistributedSequence();

	public static DistributedSequence getInstance() {
		return instance;
	}
}
