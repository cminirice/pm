package com.guttv.pm.core.fp;

import com.guttv.pm.core.bean.RoleBean;
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
import java.util.List;

public class RoleToZookeeper {
    private static Logger logger = LoggerFactory.getLogger(ComponentPackToZookeeper.class);

    /**
     * 把角色持久化到zookeeper上。如果已经有，更新；没有，创建。
     *
     * @param role
     */
    public static Boolean persistanceToZookeeper(RoleBean role) {
        if (role.getId() < 0) {
            return false;
        }
        String path = ZKPaths.makePath(PathConstants.ROLE_PATH, role.getId().toString());
        path = ZookeeperHelper.getRealPath(path);
        CuratorFramework client = CuratorClientFactory.getClient();
        try {
            client.start();
            ZookeeperHelper.putToZookeeper(path, client, role);
            logger.info("roleBean数据保存到zookeeper路径" + path + "上：" + JsonUtil.toJson(role));
            return true;
        } catch (Exception e) {
            logger.info("roleBean数据保存到zookeeper路径" + path + "上：" + JsonUtil.toJson(role) + "出现异常" + e);
        } finally {
            if (client != null) {
                IOUtils.closeQuietly(client);
            }
        }
        return false;
    }

    /**
     * 删除角色
     * @param id
     */
    public static boolean deleteFromZookeeper(Long id){
        if (id < 0) {
            return false;
        }
        String path = ZKPaths.makePath(PathConstants.ROLE_PATH, id.toString());
        path = ZookeeperHelper.getRealPath(path);
        CuratorFramework client = CuratorClientFactory.getClient();
        try {
            client.start();
            ZookeeperHelper.deleteFromZookeeper(path, client);
            logger.info("删除角色，zookeeper上路径：" + path);
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
     * 查询指定的角色
     * @param id
     */
    public static RoleBean getRoleFromZookeeper(Long id) {
        if (id < 0) {
            return null;
        }
        String path = ZKPaths.makePath(PathConstants.ROLE_PATH, id.toString());
        path = ZookeeperHelper.getRealPath(path);
        CuratorFramework client = CuratorClientFactory.getClient();
        try {
            client.start();
            return ZookeeperHelper.getFromZookeeper(path, client, RoleBean.class);
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
     * 查询全部的角色
     */
    public static List<RoleBean> getAllRoleFromZookeeper() {
        String path = PathConstants.ROLE_PATH;
        path = ZookeeperHelper.getRealPath(path);
        CuratorFramework client = CuratorClientFactory.getClient();
        List<RoleBean> roleList = new ArrayList<>();
        client.start();
        try {
            List<String> ids;
            try {
                ids = client.getChildren().forPath(path);
            } catch (Exception e1) {
                logger.warn("没有任何角色信息。");
                return null;
            }
            if (ids != null && ids.size() > 0) {
                RoleBean roleBean = null;
                String p = null;

                for (String id : ids) {
                    p = path + "/" + id;
                    try {
                        roleBean = ZookeeperHelper.getFromZookeeper(p, client, RoleBean.class);
                        if (roleBean != null) {
                            roleList.add(roleBean);
                        }
                    } catch (Exception e) {
                        logger.error("从zk服务器读取数据异常，路径：" + p, e);
                    }
                }
            }
        } finally {
            IOUtils.closeQuietly(client);
        }
        return roleList;
    }


}
