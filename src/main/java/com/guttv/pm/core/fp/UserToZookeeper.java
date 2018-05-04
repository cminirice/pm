package com.guttv.pm.core.fp;

import com.guttv.pm.core.bean.UserBean;
import com.guttv.pm.core.zk.CuratorClientFactory;
import com.guttv.pm.core.zk.PathConstants;
import com.guttv.pm.core.zk.ZookeeperHelper;
import com.guttv.pm.utils.JsonUtil;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class UserToZookeeper {
    private static Logger logger = LoggerFactory.getLogger(ComponentPackToZookeeper.class);

    /**
     * 把管理员持久化到zookeeper上。如果已经有，更新；没有，创建。
     *
     * @param user
     * @throws Exception
     */
    public static Boolean persistanceToZookeeper(UserBean user) {
        if (StringUtils.isBlank(user.getName())) {
            return false;
        }
        String path = ZKPaths.makePath(PathConstants.USER_PATH, user.getName());
        path = ZookeeperHelper.getRealPath(path);
        CuratorFramework client = CuratorClientFactory.getClient();
        try {
            client.start();
            ZookeeperHelper.putToZookeeper(path, client, user);
            logger.info("userBean数据保存到zookeeper路径" + path + "上：" + JsonUtil.toJson(user));
            return true;
        } catch (Exception e) {
            logger.info("userBean数据保存到zookeeper路径" + path + "上：" + JsonUtil.toJson(user) + "出现异常" + e);
        } finally {
            if (client != null) {
                IOUtils.closeQuietly(client);
            }
        }
        return false;
    }

    /**
     * 删除用户
     * @param userName
     * @throws Exception
     */
    public static boolean deleteFromZookeeper(String userName){
        if (StringUtils.isBlank(userName)) {
            return false;
        }
        String path = ZKPaths.makePath(PathConstants.USER_PATH, userName);
        path = ZookeeperHelper.getRealPath(path);
        CuratorFramework client = CuratorClientFactory.getClient();
        try {
            client.start();
            ZookeeperHelper.deleteFromZookeeper(path, client);
            logger.info("删除用户，zookeeper上路径：" + path);
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
     * 查询指定的用户
     * @param userName
     * @throws Exception
     */
    public static UserBean getUserFromZookeeper(String userName) {
        if (StringUtils.isBlank(userName)) {
            return null;
        }
        String path = ZKPaths.makePath(PathConstants.USER_PATH, userName);
        path = ZookeeperHelper.getRealPath(path);
        CuratorFramework client = CuratorClientFactory.getClient();
        try {
            client.start();
            return ZookeeperHelper.getFromZookeeper(path, client, UserBean.class);
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
     * 查询全部的用户
     * @throws Exception
     */
    public static List<UserBean> getAllUserFromZookeeper() {
        String path = PathConstants.USER_PATH;
        path = ZookeeperHelper.getRealPath(path);
        CuratorFramework client = CuratorClientFactory.getClient();
        List<UserBean> userList = new ArrayList<>();
        client.start();
        try {
            List<String> userNames;
            try {
                userNames = client.getChildren().forPath(path);
            } catch (Exception e1) {
                logger.warn("没有任何管理员信息。");
                return null;
            }
            if (userNames != null && userNames.size() > 0) {
                UserBean userBean = null;
                String p = null;

                for (String userName : userNames) {
                    p = path + "/" + userName;
                    try {
                        userBean = ZookeeperHelper.getFromZookeeper(p, client, UserBean.class);
                        if (userBean != null) {
                            userList.add(userBean);
                        }
                    } catch (Exception e) {
                        logger.error("从zk服务器读取数据异常，路径：" + p, e);
                    }
                }
            }
        } finally {
            IOUtils.closeQuietly(client);
        }
        return userList;
    }


}
