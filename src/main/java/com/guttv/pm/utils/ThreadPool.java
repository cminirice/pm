/**
 * 
 */
package com.guttv.pm.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.guttv.pm.core.cache.ConfigCache;

/**
 * @author minimice
 *
 */
public class ThreadPool {
	private ExecutorService threadPool = null;
	public static int DEFAULT_MAX_THREAD_NUM = ConfigCache.getInstance().getProperty(Constants.THREAD_POOL_MAX_SIZE, 300);
	private ThreadPool() {
		threadPool = Executors.newFixedThreadPool(DEFAULT_MAX_THREAD_NUM);
	}
	private static ThreadPool pool = new ThreadPool();
	public static ThreadPool getPool() {
		return pool;
	}
	
	public ExecutorService getExecutorService() {
		return threadPool;
	}
	
	public void submit(Runnable thread) {
		threadPool.submit(thread);
	}
}
