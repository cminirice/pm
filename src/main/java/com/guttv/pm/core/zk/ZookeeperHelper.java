/**
 * 
 */
package com.guttv.pm.core.zk;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;

import com.google.gson.Gson;
import com.guttv.pm.core.cache.ConfigCache;
import com.guttv.pm.utils.Constants;

/**
 * @author Peter
 *
 */
public class ZookeeperHelper {

	public static String getRealPath(String path) {
		if (StringUtils.isBlank(path)) {
			return null;
		}

		path = checkPath(path);

		String pre = ConfigCache.getInstance().getProperty(Constants.ZOOKEEPER_PRE_PATH, "");
		if (StringUtils.isNotBlank(pre)) {
			if (!pre.startsWith("/")) {
				pre = "/" + pre;
			}

			if (!path.startsWith(pre)) {
				path = ZKPaths.makePath(pre, path);
			}
		}

		return checkPath(path);
	}

	public static String checkPath(String path) {
		path = path.trim();

		if (!path.startsWith("/")) {
			path = "/" + path;
		}
		path = path.replaceAll("\\\\", "/");
		while (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		while (path.indexOf("//") >= 0) {
			path = path.replace("//", "/");
		}
		return path.toLowerCase();
	}

	/**
	 * 从zookeeper上读取对象
	 * 
	 * @param path
	 * @param client
	 * @param clz
	 * @return
	 * @throws Exception
	 */
	public static <T> T getFromZookeeper(String path, CuratorFramework client, Class<T> clz) throws Exception {
		if (client.checkExists().forPath(path) == null) {
			return null;
		}

		byte[] bytes = client.getData().forPath(path);
		if (bytes == null || bytes.length == 0) {
			return null;
		}
		return new Gson().fromJson(new String(bytes, Constants.ENCODING), clz);
	}

	/**
	 * 把数据写到zookeeper上
	 * 
	 * @param path
	 * @param client
	 * @param data
	 * @throws Exception
	 */
	public static void putToZookeeper(String path, CuratorFramework client, Object data) throws Exception {
		putToZookeeper(path, client, data, false);
	}

	/**
	 * 
	 * @param path
	 * @param client
	 * @param data
	 * @param save 是不是只做保存操作
	 * @throws Exception
	 */
	public static void putToZookeeper(String path, CuratorFramework client, Object data, boolean save)
			throws Exception {
		if (!save && client.checkExists().forPath(path) != null) {
			client.setData().forPath(path, new Gson().toJson(data).getBytes(Constants.ENCODING));
		} else {
			client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path,
					new Gson().toJson(data).getBytes(Constants.ENCODING));
		}
	}

	/**
	 * 
	 * @param path
	 * @param client
	 * @throws Exception
	 */
	public static void deleteFromZookeeper(String path, CuratorFramework client) throws Exception {
		if (client.checkExists().forPath(path) == null) {
			return;
		}
		client.delete().deletingChildrenIfNeeded().forPath(path);
	}
}
