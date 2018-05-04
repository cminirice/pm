/**
 * 
 */
package com.guttv.rpc.client.zk;

import java.lang.reflect.InvocationHandler;

import org.apache.curator.framework.CuratorFramework;

/**
 * @author Peter
 *
 */
public abstract class AbstractZKInvocationHandler implements InvocationHandler {
	public String getRequestRootPath() {
		return null;
	}

	public void setRequestRootPath(String requestRootPath) {
	}

	public String getResponseRootPath() {
		return null;
	}

	public void setResponseRootPath(String responseRootPath) {
	}

	public CuratorFramework getClient() {
		return null;
	}

	public void setClient(CuratorFramework client) {
	}

	public long getTimeout() {
		return -1;
	}

	public void setTimeout(long timeout) {
	}
}
