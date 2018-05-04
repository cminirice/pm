package com.guttv.pm.util;

import org.apache.commons.io.IOUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;

import com.guttv.pm.core.zk.CuratorClientFactory;
import com.guttv.pm.core.zk.PathConstants;
import com.guttv.pm.core.zk.ZookeeperHelper;
import com.guttv.rpc.common.SerializationUtil;

public class TestZookeeper {

	public static void main(String[]a) throws Exception{
		//
		String path = PathConstants.FLOW_PATH;
		
		path = ZookeeperHelper.getRealPath(path);
		
		CuratorFramework client = CuratorClientFactory.getClient();
		try {
			client.start();
			
			//ZookeeperHelper.putToZookeeper(path, client, "abc");
			
			String ret = client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
			.forPath("/dev/guttv/message/request", SerializationUtil.serialize("minimice"));
			System.out.println(ret);
			
		} finally {
			if(client != null) {
				IOUtils.closeQuietly(client);
			}
		}
	}
}
