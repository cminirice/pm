package com.guttv.pm.core.zk;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import com.guttv.pm.core.cache.ConfigCache;
import com.guttv.pm.utils.Constants;

public class CuratorClientFactory {
	
	/**
	 * 获取curator zookeeper客户端
	 * @return
	 */
	public static CuratorFramework getClient() {
		String connectionString = ConfigCache.getInstance().getProperty(Constants.connectionStringKey, "");
		if(StringUtils.isBlank(connectionString)) {
			connectionString = ConfigCache.getInstance().getProperty("zookeeper.connect", "");
		}
		ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);
		return CuratorFrameworkFactory.newClient(connectionString, retryPolicy);
	}
}
