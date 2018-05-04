package com.guttv.pm.core.fp;

import com.guttv.pm.core.bean.ScriptBean;
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
public class ScriptToZookeeper {

    private static Logger logger = LoggerFactory.getLogger(ScriptToZookeeper.class.getSimpleName());

    /**
     * 将Script信息持久化到zookeeper
     *
     * @param bean
     * @throws Exception
     */
    public static void persistanceToZookeeper(ScriptBean bean) throws Exception {
        if (StringUtils.isEmpty(bean.getCode())) {
            return;
        }
        String path = ZKPaths.makePath(PathConstants.SCRIPT_PATH, bean.getCode());
        path = ZookeeperHelper.getRealPath(path);

        CuratorFramework client = CuratorClientFactory.getClient();
        try {
            client.start();
            //如果是第一遍
            ScriptBean oldBean =getFromZookeeper(bean.getCode());
            if(oldBean!=null){
                if ("".equals(Utils.getString(oldBean.getCreateTime()))){
                    bean.setCreateTime(new Date());
                }else {
                    bean.setCreateTime(oldBean.getCreateTime());
                }
            }
            ZookeeperHelper.putToZookeeper(path, client, bean);
            logger.info("ScriptBean数据保存到zookeeper路径" + path + "上：" + JsonUtil.toJson(bean));
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
        String path = ZKPaths.makePath(PathConstants.SCRIPT_PATH, code);
        path = ZookeeperHelper.getRealPath(path);
        delete(path);
    }

    public static void deleteFromZookeeper(ScriptBean bean) throws Exception {
        if (StringUtils.isEmpty(bean.getCode())) {
            return;
        }
        String path = ZKPaths.makePath(PathConstants.SCRIPT_PATH, bean.getCode());
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

    public static List<ScriptBean> getListFromZookeeper() throws Exception {
        String path = PathConstants.SCRIPT_PATH;
        path = ZookeeperHelper.getRealPath(path);
        List<ScriptBean> list = new ArrayList<>();
        // zookeeper客户端
        CuratorFramework client = CuratorClientFactory.getClient();
        try {
            client.start();
            List<String> codes = client.getChildren().forPath(path);
            if (codes != null && codes.size() > 0) {
                for (String code : codes){
                    ScriptBean bean =ZookeeperHelper.getFromZookeeper(path + "/" + code, client, ScriptBean.class);
                    list.add(bean);
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


    public static ScriptBean getFromZookeeper(String code) throws Exception {
        String path = PathConstants.SCRIPT_PATH;
        path = ZookeeperHelper.getRealPath(path);
        ScriptBean bean;
        CuratorFramework client = CuratorClientFactory.getClient();
        try {
            client.start();
            bean =ZookeeperHelper.getFromZookeeper(path + "/" + code, client, ScriptBean.class);
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
