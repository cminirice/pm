/**
 * 
 */
package com.guttv.rpc.common;

import java.io.Serializable;

/**
 * @author Peter
 *
 */
public class RpcResponse implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String requestId = null;
	private Throwable error = null;
	private Object result = null;

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public Throwable getError() {
		return error;
	}

	public void setError(Throwable error) {
		this.error = error;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}
	
	private String hostAddr = null;
	private String hostName = null;
	public String getHostAddr() {
		return hostAddr;
	}
	public String getHostName() {
		return hostName;
	}
	public RpcResponse(){
		hostAddr = HostInfo.getHostAddr();
		hostName = HostInfo.getHostName();
	}
}
