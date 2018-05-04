package com.guttv.pm.core.fp;

import com.guttv.pm.core.bean.ServerBean;
import com.guttv.pm.core.zk.CuratorClientFactory;
import com.guttv.pm.core.zk.PathConstants;
import com.guttv.pm.core.zk.ZookeeperHelper;
import com.guttv.pm.utils.JsonUtil;
import com.guttv.pm.utils.Utils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author donghongchen
 * @create 2018-02-01 10:24
 **/
public class ServerToZookeeper {

    private static Logger logger = LoggerFactory.getLogger(ServerToZookeeper.class.getSimpleName());

    /**
     * 将服务器配置信息持久化到zookeeper
     *
     * @param bean
     * @throws Exception
     */
    public static void persistanceToZookeeper(ServerBean bean) throws Exception {
        if (StringUtils.isEmpty(bean.getCode())) {
            return;
        }
        String path = ZKPaths.makePath(PathConstants.SERVER_PATH, bean.getCode());
        path = ZookeeperHelper.getRealPath(path);

        CuratorFramework client = CuratorClientFactory.getClient();
        try {
            client.start();
            //如果是第一遍
            ServerBean entity = ServerToZookeeper.getFromZookeeper(bean.getCode());
            if(entity!=null){
                if ("".equals(Utils.getString(entity.getCreateTime()))){
                    bean.setCreateTime(new Date());
                }else {
                    bean.setCreateTime(entity.getCreateTime());
                }
            }
            ZookeeperHelper.putToZookeeper(path, client, bean);

            logger.info("ServerBean数据保存到zookeeper路径" + path + "上：" + JsonUtil.toJson(bean));
        } finally {
            if (client != null) {
                IOUtils.closeQuietly(client);
            }
        }

    }


    public static void deleteFromZookeeper(String code) throws Exception {
        if (StringUtils.isEmpty(code)) {
            return;
        }
        String path = ZKPaths.makePath(PathConstants.SERVER_PATH, code);
        path = ZookeeperHelper.getRealPath(path);
        delete(path);
    }

    public static void deleteFromZookeeper(ServerBean bean) throws Exception {
        if (StringUtils.isEmpty(bean.getCode())) {
            return;
        }
        String path = ZKPaths.makePath(PathConstants.SERVER_PATH, bean.getCode());
        path = ZookeeperHelper.getRealPath(path);
        delete(path);
    }

    private static void delete(String path) throws Exception {
        CuratorFramework client = CuratorClientFactory.getClient();
        try {
            client.start();
            ZookeeperHelper.deleteFromZookeeper(path, client);
            logger.info("删除zookeeper上路径 ：" + path);
        } finally {
            if (client != null) {
                IOUtils.closeQuietly(client);
            }
        }
    }

    public static List<ServerBean> getListFromZookeeper() throws Exception {
        String path = PathConstants.SERVER_PATH;
        path = ZookeeperHelper.getRealPath(path);
        List<ServerBean> list = new ArrayList<>();
        // zookeeper客户端
        CuratorFramework client = CuratorClientFactory.getClient();
        try {
            client.start();
            List<String> codes = client.getChildren().forPath(path);
            if (codes != null && codes.size() > 0) {
                for (String code : codes){
                    ServerBean serverBean =ZookeeperHelper.getFromZookeeper(path + "/" + code, client, ServerBean.class);
                    list.add(serverBean);
                }
            }
        } finally {
            try {
                client.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return list;
    }


    public static ServerBean getFromZookeeper(String code) throws Exception {
        String path = PathConstants.SERVER_PATH;
        path = ZookeeperHelper.getRealPath(path);
        ServerBean bean;
        CuratorFramework client = CuratorClientFactory.getClient();
        try {
            client.start();
            bean =ZookeeperHelper.getFromZookeeper(path + "/" + code, client, ServerBean.class);
        }finally {
            try {
                client.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return bean;
    }

}
