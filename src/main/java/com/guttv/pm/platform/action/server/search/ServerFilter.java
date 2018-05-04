package com.guttv.pm.platform.action.server.search;

import com.guttv.pm.core.bean.ServerBean;
import com.guttv.pm.platform.action.SearchFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.file.Matcher;

import java.util.ArrayList;
import java.util.List;

/**
 * @author donghongchen
 * @create 2018-01-27 15:05
 **/
public class ServerFilter implements SearchFilter<ServerBean>{

    @Override
    public List<ServerBean> filter(List<ServerBean> list, String filterField, String value) {
        if (StringUtils.isBlank(filterField) || StringUtils.isBlank(value) || list == null || list.size() == 0) {
            return list;
        }


        List<ServerBean> serverBeans = new ArrayList<>();

        String pattern = "*" + value + "*";
        // 循环查找吧
        for (ServerBean bean : list) {

            // 分为同的字段，不同的查询方法
            if (filterField.equals("ip")) {
                if (StringUtils.isNotBlank(bean.getIp()) && Matcher.match(pattern, bean.getIp(), false)) {
                    serverBeans.add(bean);
                }
            } else if (filterField.equals("username")) {
                if (StringUtils.isNotBlank(bean.getUserName()) && Matcher.match(pattern, bean.getUserName(), false)) {
                    serverBeans.add(bean);
                }
            } else {
                // 在没有实现的情况下，把所有的返回
                serverBeans.add(bean);
            }
        }
        return serverBeans;
    }

}
