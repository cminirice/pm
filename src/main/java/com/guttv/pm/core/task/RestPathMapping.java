/**
 * 
 */
package com.guttv.pm.core.task;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.PatternMatchUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;

/**
 * @author Peter
 *
 */
public class RestPathMapping {

	public static void main(String[] a) {
		System.out.println(PatternMatchUtils.simpleMatch("/rest/test3/{name}", "/rest/test3/peter"));
		PathMatcher pathMather = new AntPathMatcher();
		System.out.println(pathMather.match("/rest/test3/{name}", "/rest/test3/peter"));
		System.out.println(pathMather.match("/rest/test3/{name}", "/rest/test3/*"));
		System.out.println(pathMather.match("/rest/test3", "/rest/test3/"));
		System.out.println(pathMather.match("/rest/test3/", "/rest/test3"));
	}

	protected Logger logger = LoggerFactory.getLogger(RestPathMapping.class);

	private Map<String, PathMappingInfo> handlerMethodMap = new HashMap<String, PathMappingInfo>();

	/**
	 * 缓存路径处理句柄
	 * 
	 * @param path
	 * @param handlerMethod
	 */
	public void cacheHandlerMethod(String path, HandlerMethod handlerMethod, AbstractTask task) throws Exception {
		w.lock();
		try {
			if (hasIntersectantPath(path)) {
				throw new Exception("已经存在路径[" + path + "]的映射方法");
			}

			handlerMethodMap.put(path, new PathMappingInfo(handlerMethod,
					handlerMethod.getMethod().getAnnotation(RequestMapping.class), path, task));
		} finally {
			w.unlock();
		}
	}

	/**
	 * 找出有相交的路径，互相包含的都算数
	 * 
	 * @param path
	 * @return
	 */
	private boolean hasIntersectantPath(String path) {
		Set<String> pathSet = handlerMethodMap.keySet();
		for (String p : pathSet) {
			// if (PatternMatchUtils.simpleMatch(p, path) ||
			// PatternMatchUtils.simpleMatch(path, p)) {
			if (pathMather.match(p, path) || pathMather.match(path, p)) {
				logger.debug("存在与[" + path + "]交叉的路径[" + p + "]");
				return true;
			}
		}
		return false;
	}

	/**
	 * 按组件类删除处理句柄
	 * 
	 * @param proxy
	 */
	public void uncacheHandlerMethod(Object comInstance) {
		w.lock();
		try {
			Set<String> pathSet = handlerMethodMap.keySet();
			String path = null;
			for (Iterator<String> iter = pathSet.iterator(); iter.hasNext();) {
				path = iter.next();
				if (comInstance == handlerMethodMap.get(path).getHandlerMethod().getBean()) {
					iter.remove();
					logger.debug("按代理卸载路径处理句柄：" + path);
				} else if (comInstance == handlerMethodMap.get(path).getTask()) {
					iter.remove();
					logger.debug("按任务卸载路径处理句柄：" + path);
				}
			}
		} finally {
			w.unlock();
		}
	}

	/**
	 * 获取处理句柄
	 * 
	 * @param path
	 * @return
	 */
	public PathMappingInfo getHandleMethod(String path) {
		r.lock();
		try {
			Set<String> pathSet = handlerMethodMap.keySet();
			for (String p : pathSet) {
				if (pathMather.match(p, path)) {
					return handlerMethodMap.get(p);
				}
			}
		} finally {
			r.unlock();
		}
		return null;
	}

	public PathMatcher getPathMatcher() {
		return pathMather;
	}

	// 重入锁
	private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();

	// 下面是单例配置
	private RestPathMapping() {
		pathMather.setCaseSensitive(false);
	}

	private AntPathMatcher pathMather = new AntPathMatcher();
	private static RestPathMapping instance = new RestPathMapping();

	public static synchronized RestPathMapping getInstance() {
		return instance;
	}

	public static class PathMappingInfo {
		private RequestMapping requestMapping = null;
		private HandlerMethod handlerMethod = null;
		private String path = null;
		private AbstractTask task = null;

		PathMappingInfo(HandlerMethod g, RequestMapping r, String p, AbstractTask t) {
			this.requestMapping = r;
			this.handlerMethod = g;
			this.path = p;
			this.task = t;
		}

		public RequestMapping getRequestMapping() {
			return requestMapping;
		}

		public HandlerMethod getHandlerMethod() {
			return handlerMethod;
		}

		public String getPath() {
			return path;
		}

		public AbstractTask getTask() {
			return task;
		}

	}
}
