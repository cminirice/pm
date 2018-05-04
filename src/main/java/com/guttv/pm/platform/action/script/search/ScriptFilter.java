package com.guttv.pm.platform.action.script.search;

import com.guttv.pm.core.bean.ScriptBean;
import com.guttv.pm.platform.action.SearchFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.file.Matcher;

import java.util.ArrayList;
import java.util.List;

/**
 * @author donghongchen
 * @create 2018-02-06 16:17
 **/
public class ScriptFilter implements SearchFilter<ScriptBean> {

    @Override
    public List<ScriptBean> filter(List<ScriptBean> list, String filterField, String value) {
        if (StringUtils.isBlank(filterField) || StringUtils.isBlank(value) || list == null || list.size() == 0) {
            return list;
        }
        List<ScriptBean> beans = new ArrayList<>();
        String pattern = "*" + value + "*";
        // 循环查找吧
        for (ScriptBean bean : list) {
            // 分为同的字段，不同的查询方法
            if (filterField.equals("fileName")) {
                if (StringUtils.isNotBlank(bean.getFileName()) && Matcher.match(pattern, bean.getFileName(), false)) {
                    beans.add(bean);
                }
            } else if (filterField.equals("code")) {
                if (StringUtils.isNotBlank(bean.getCode()) && Matcher.match(pattern, bean.getCode(), false)) {
                    beans.add(bean);
                }
            } else {
                // 在没有实现的情况下，把所有的返回
                beans.add(bean);
            }
        }
        return beans;
    }

}