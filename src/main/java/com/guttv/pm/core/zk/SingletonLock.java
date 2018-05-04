/**
 * 
 */
package com.guttv.pm.core.zk;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.guttv.pm.utils.HttpUtil;
import com.guttv.pm.utils.Utils;

/**
 * 在多实例部署架构中，提供功能的单点运行支持
 * 调用lock方法，返回true时，可以运行，false：说明已经有其它实例已经运行，调用close()方法时，释放zookeeper连接，同时释放运行锁
 * 
 * @author Peter
 *
 */
public class SingletonLock {
	protected final Logger logger = LoggerFactory.getLogger(SingletonLock.class);
	private String path = null;
	private CuratorFramework client = null;
	private Gson gson = new Gson();
	private SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");

	public SingletonLock(String p) {
		this.path = ZookeeperHelper.getRealPath(p) + "/lock";
	}

	/**
	 * 确定不用锁的情况下，需要掉用该方法释放zookeeper连接， 同时会释放zookeeper上的锁（删除路径 ）
	 */
	public void close() {
		CloseableUtils.closeQuietly(client);
	}

	private String key = null;

	/**
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean lock() {
		// 如果没有配置路径，默认取得运行锁
		if (StringUtils.isBlank(path)) {
			return true;
		}

		// 第一次请求时加载
		if (client == null) {
			client = CuratorClientFactory.getClient();
			try {
				client.start();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				return false;
			}
		}

		try {
			// 判断路径是否存在
			if (client.checkExists().forPath(path) != null) {
				// 如果路径存在，并且本地key为空，说明是其它系统取得的锁
				if (this.key == null) {
					return false;
				}
				Map<String, String> datas = gson.fromJson(new String(client.getData().forPath(path)), Map.class);
				String k = datas.get("key");
				// 比较本地key和zookeeper上是否一致，确定是否取得锁
				return key.equals(k);
			} else {
				String k = UUID.randomUUID().toString();
				Map<String, String> datas = new HashMap<String, String>();
				datas.put("key", k);
				datas.put("ip", HttpUtil.getLocalIP());
				String localtion = null;
				try {
					localtion = Utils.getProjectLocation();
				} catch (Exception e) {
					logger.warn("获取系统路径异常：" + e.getMessage());
					localtion = "";
				}
				datas.put("localtion", localtion);
				datas.put("timestamp", format.format(new Date(System.currentTimeMillis())));
				// 创建路径，并且保存key值，根据保存是否成功，确定是否取得锁
				client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path,
						gson.toJson(datas).getBytes());
				key = k;
				return true;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	public String getPath() {
		return path;
	}

	public void unlock() {
		if (StringUtils.isNotBlank(path) && client != null) {
			try {
				if (client.checkExists().forPath(path) != null) {
					client.delete().forPath(path);
				}
			} catch (Exception e) {
				logger.info("释放锁[" + path + "]异常：" + e.getMessage());
			}
		}
	}

	public void setClient(CuratorFramework client) {
		this.client = client;
	}
}
