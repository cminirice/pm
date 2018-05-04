/**
 * 
 */
package com.guttv.rpc.common;

import java.io.Serializable;

/**
 * @author Peter
 *
 */
public class RpcRequest implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String requestId = null;

	// 可以用作关联ID，不同的框架可以有不同的用处
	private String responsePath = null;

	private String className = null;
	private String methodName = null;
	private Class<?>[] parameterTypes = null;
	private Object[] parameters = null;
	
	private boolean needResponse = true;

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}

	public void setParameterTypes(Class<?>[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	public Object[] getParameters() {
		return parameters;
	}

	public void setParameters(Object[] parameters) {
		this.parameters = parameters;
	}

	public String getResponsePath() {
		return responsePath;
	}

	public void setResponsePath(String responsePath) {
		this.responsePath = responsePath;
	}

	public boolean isNeedResponse() {
		return needResponse;
	}

	public void setNeedResponse(boolean needResponse) {
		this.needResponse = needResponse;
	}

	
	private String hostAddr = null;
	private String hostName = null;
	public String getHostAddr() {
		return hostAddr;
	}
	public String getHostName() {
		return hostName;
	}
	public RpcRequest(){
		hostAddr = HostInfo.getHostAddr();
		hostName = HostInfo.getHostName();
	}
}
