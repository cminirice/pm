package com.guttv.pm.core.fp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.guttv.pm.core.bean.AuthBean;
import com.guttv.pm.core.zk.CuratorClientFactory;
import com.guttv.pm.core.zk.PathConstants;
import com.guttv.pm.core.zk.ZookeeperHelper;
import com.guttv.pm.utils.JsonUtil;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AuthToZookeeper {
    private static Logger logger = LoggerFactory.getLogger(ComponentPackToZookeeper.class);

    /**
     * 把权限持久化到zookeeper上。如果已经有，更新；没有，创建。
     *
     * @param auth
     */
    public static Boolean persistanceToZookeeper(AuthBean auth) {
        if (auth.getId() < 0) {
            return false;
        }
        String path;
        if(auth.getParentAuthId() == null || auth.getParentAuthId() == 0){
            path = ZKPaths.makePath(PathConstants.PRIVILEGE_PATH, auth.getId().toString());
        }else {
            path = ZKPaths.makePath(PathConstants.PRIVILEGE_PATH, auth.getParentAuthId().toString(), auth.getId().toString());
        }
        path = ZookeeperHelper.getRealPath(path);
        CuratorFramework client = CuratorClientFactory.getClient();
        try {
            client.start();
            ZookeeperHelper.putToZookeeper(path, client, auth);
            logger.info("Auth数据保存到zookeeper路径" + path + "上：" + JsonUtil.toJson(auth));
            return true;
        } catch (Exception e) {
            logger.info("Auth数据保存到zookeeper路径" + path + "上：" + JsonUtil.toJson(auth) + "出现异常" + e);
        } finally {
            if (client != null) {
                IOUtils.closeQuietly(client);
            }
        }
        return false;
    }

    /**
     * 删除权限
     * @param id
     */
    public static boolean deleteFromZookeeper(Long id, Long parentAuthId){
        if (id < 0) {
            return false;
        }
        String path;
        if(parentAuthId == null || parentAuthId == 0){
            path = ZKPaths.makePath(PathConstants.PRIVILEGE_PATH, id.toString());
        }else {
            path = ZKPaths.makePath(PathConstants.PRIVILEGE_PATH, parentAuthId.toString(), id.toString());
        }
        path = ZookeeperHelper.getRealPath(path);
        CuratorFramework client = CuratorClientFactory.getClient();
        try {
            client.start();
            ZookeeperHelper.deleteFromZookeeper(path, client);
            logger.info("删除权限，zookeeper上路径：" + path);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (client != null) {
                org.apache.commons.io.IOUtils.closeQuietly(client);
            }
        }
        return false;
    }

    /**
     * 查询指定的权限
     * @param id
     */
    public static AuthBean getAuthFromZookeeper(Long id, Long parentAuthId) {
        if (id < 0) {
            return null;
        }
        String path;
        if(parentAuthId == null || parentAuthId == 0){
            path = ZKPaths.makePath(PathConstants.PRIVILEGE_PATH, id.toString());
        }else {
            path = ZKPaths.makePath(PathConstants.PRIVILEGE_PATH, parentAuthId.toString(), id.toString());
        }
        path = ZookeeperHelper.getRealPath(path);
        CuratorFramework client = CuratorClientFactory.getClient();
        try {
            client.start();
            return ZookeeperHelper.getFromZookeeper(path, client, AuthBean.class);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (client != null) {
                org.apache.commons.io.IOUtils.closeQuietly(client);
            }
        }
        return null;
    }

    /**
     * 查询全部的权限
     */
    public static List<AuthBean> getAllAuthFromZookeeper(Long parentAuthId) {
        String path;
        if(parentAuthId == null || parentAuthId == 0){
            path = PathConstants.PRIVILEGE_PATH;
        }else {
            path = ZKPaths.makePath(PathConstants.PRIVILEGE_PATH, parentAuthId.toString());
        }
        path = ZookeeperHelper.getRealPath(path);
        CuratorFramework client = CuratorClientFactory.getClient();
        List<AuthBean> authList = new ArrayList<>();
        client.start();
        try {
            List<String> ids;
            try {
                ids = client.getChildren().forPath(path);
            } catch (Exception e1) {
                logger.warn("没有任何权限信息。");
                return null;
            }
            if (ids != null && ids.size() > 0) {
                //排个序
                Collections.sort(ids);
                AuthBean authBaen = null;
                String p = null;

                for (String id : ids) {
                    p = path + "/" + id;
                    try {
                        authBaen = ZookeeperHelper.getFromZookeeper(p, client, AuthBean.class);
                        if (authBaen != null) {
                            authList.add(authBaen);
                        }
                    } catch (Exception e) {
                        logger.error("从zk服务器读取数据异常，路径：" + p, e);
                    }
                }
            }
        } finally {
            IOUtils.closeQuietly(client);
        }
        return authList;
    }

    public static void main(String[] args) {
        String json = "[{\"name\":\"执行容器列表\",\"id\":1000,\"parentAuthId\":0},{\"name\":\"组件包列表\",\"id\":1100,\"parentAuthId\":0},{\"name\":\"组件列表\",\"id\":1200,\"parentAuthId\":0},{\"name\":\"流程制作\",\"id\":1300,\"parentAuthId\":0},{\"name\":\"流程列表\",\"id\":1400,\"parentAuthId\":0},{\"name\":\"执行列表\",\"id\":1500,\"parentAuthId\":0},{\"name\":\"管理员列表\",\"id\":1600,\"parentAuthId\":0},{\"name\":\"角色列表\",\"id\":1700,\"parentAuthId\":0},{\"name\":\"权限列表\",\"id\":1800,\"parentAuthId\":0},{\"name\":\"配置属性列表\",\"id\":1900,\"parentAuthId\":0},{\"name\":\"服务器列表\",\"id\":2000,\"parentAuthId\":0},{\"name\":\"查看\",\"id\":1001,\"parentAuthId\":1000},{\"name\":\"编辑\",\"id\":1002,\"parentAuthId\":1000},{\"name\":\"配置\",\"id\":1003,\"parentAuthId\":1000},{\"name\":\"禁用\",\"id\":1004,\"parentAuthId\":1000},{\"name\":\"流程\",\"id\":1005,\"parentAuthId\":1000},{\"name\":\"关闭\",\"id\":1006,\"parentAuthId\":1000},{\"name\":\"删除\",\"id\":1007,\"parentAuthId\":1000},{\"name\":\"启用\",\"id\":1008,\"parentAuthId\":1000},{\"name\":\"注册\",\"id\":1101,\"parentAuthId\":1100},{\"name\":\"查看\",\"id\":1102,\"parentAuthId\":1100},{\"name\":\"卸载\",\"id\":1103,\"parentAuthId\":1100},{\"name\":\"查看\",\"id\":1201,\"parentAuthId\":1200},{\"name\":\"停用\",\"id\":1202,\"parentAuthId\":1200},{\"name\":\"修改属性\",\"id\":1203,\"parentAuthId\":1200},{\"name\":\"启用\",\"id\":1204,\"parentAuthId\":1200},{\"name\":\"新建流程\",\"id\":1401,\"parentAuthId\":1400},{\"name\":\"编辑流程\",\"id\":1402,\"parentAuthId\":1400},{\"name\":\"查看流程\",\"id\":1403,\"parentAuthId\":1400},{\"name\":\"查看\",\"id\":1404,\"parentAuthId\":1400},{\"name\":\"编辑\",\"id\":1405,\"parentAuthId\":1400},{\"name\":\"停用\",\"id\":1406,\"parentAuthId\":1400},{\"name\":\"删除\",\"id\":1407,\"parentAuthId\":1400},{\"name\":\"执行配置\",\"id\":1408,\"parentAuthId\":1400},{\"name\":\"复制\",\"id\":1409,\"parentAuthId\":1400},{\"name\":\"查看\",\"id\":1501,\"parentAuthId\":1500},{\"name\":\"查看流程\",\"id\":1502,\"parentAuthId\":1500},{\"name\":\"重生\",\"id\":1503,\"parentAuthId\":1500},{\"name\":\"删除\",\"id\":1504,\"parentAuthId\":1500},{\"name\":\"还原\",\"id\":1505,\"parentAuthId\":1500},{\"name\":\"任务\",\"id\":1506,\"parentAuthId\":1500},{\"name\":\"停止\",\"id\":1507,\"parentAuthId\":1500},{\"name\":\"继续\",\"id\":1508,\"parentAuthId\":1500},{\"name\":\"暂停\",\"id\":1509,\"parentAuthId\":1500},{\"name\":\"启动\",\"id\":1510,\"parentAuthId\":1500},{\"name\":\"重启\",\"id\":1511,\"parentAuthId\":1500},{\"name\":\"复位\",\"id\":1512,\"parentAuthId\":1500},{\"name\":\"修改名称\",\"id\":1513,\"parentAuthId\":1500},{\"name\":\"修改属性1\",\"id\":1514,\"parentAuthId\":1500},{\"name\":\"修改属性2\",\"id\":1515,\"parentAuthId\":1500},{\"name\":\"禁用\",\"id\":1516,\"parentAuthId\":1500},{\"name\":\"修改通道\",\"id\":1517,\"parentAuthId\":1500},{\"name\":\"启用\",\"id\":1518,\"parentAuthId\":1500},{\"name\":\"单机停止\",\"id\":1519,\"parentAuthId\":1500},{\"name\":\"单机继续\",\"id\":1520,\"parentAuthId\":1500},{\"name\":\"单机暂停\",\"id\":1521,\"parentAuthId\":1500},{\"name\":\"新增\",\"id\":1601,\"parentAuthId\":1600},{\"name\":\"修改\",\"id\":1602,\"parentAuthId\":1600},{\"name\":\"删除\",\"id\":1603,\"parentAuthId\":1600},{\"name\":\"新增\",\"id\":1701,\"parentAuthId\":1700},{\"name\":\"修改\",\"id\":1702,\"parentAuthId\":1700},{\"name\":\"删除\",\"id\":1703,\"parentAuthId\":1700},{\"name\":\"新增\",\"id\":1801,\"parentAuthId\":1800},{\"name\":\"修改\",\"id\":1802,\"parentAuthId\":1800},{\"name\":\"删除\",\"id\":1803,\"parentAuthId\":1800},{\"name\":\"更新缓存\",\"id\":1901,\"parentAuthId\":1900},{\"name\":\"新增\",\"id\":2001,\"parentAuthId\":2000}]";
        Gson gson = new Gson();
        List<AuthBean> authBeans = gson.fromJson(json,new TypeToken<List<AuthBean>>() {}.getType());
        for (AuthBean authBean : authBeans) {
            persistanceToZookeeper(authBean);
        }
    }
}
