/**
 * 
 */
package com.guttv.rpc.server.executor;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.ApplicationContext;

import com.guttv.rpc.common.L;
import com.guttv.rpc.common.RpcRequest;
import com.guttv.rpc.common.RpcResponse;

import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

/**
 * @author Peter
 *
 */
public class SpringContextExecutor implements RpcRequestExecutor {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.guttv.rpc.server.executor.RpcRequestExecutor#invoke(com.guttv.rpc.
	 * common.RpcRequest)
	 */
	@Override
	public RpcResponse invoke(RpcRequest request) throws Exception {
		RpcResponse response = new RpcResponse();
		response.setRequestId(request.getRequestId());

		try {
			L.logger.debug("开始执行请求[" + request.getRequestId() + "]访问[" + request.getClassName() + "]["
					+ request.getMethodName() + "]...");

			// 先找到接口的类信息
			String className = request.getClassName();
			Class<?> clz = Class.forName(className);

			// 找到spring bean
			Object serviceBean = context.getBean(clz);

			// 取出来各种参数
			String methodName = request.getMethodName();
			Class<?>[] parameterTypes = request.getParameterTypes();
			Object[] parameters = request.getParameters();

			// 用cglib执行方法
			FastClass serviceFastClass = FastClass.create(clz);
			FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
			response.setResult(serviceFastMethod.invoke(serviceBean, parameters));

			L.logger.debug("请求[" + request.getRequestId() + "]执行完毕...");
		} catch (Throwable error) {
			Throwable root = ExceptionUtils.getRootCause(error);
			L.logger.error("执行请求[" + request.getRequestId() + "]异常：" + error.getMessage(), root == null ? error : root);
			response.setError(root);
		}

		return response;
	}

	private ApplicationContext context = null;

	public SpringContextExecutor() {
	}

	public SpringContextExecutor(ApplicationContext context) {
		this.context = context;
	}

	public ApplicationContext getContext() {
		return context;
	}

	public void setContext(ApplicationContext context) {
		this.context = context;
	}
}
