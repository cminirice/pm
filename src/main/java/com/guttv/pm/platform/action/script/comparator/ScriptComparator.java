package com.guttv.pm.platform.action.script.comparator;

import com.guttv.pm.core.bean.ScriptBean;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;


/**
 * @author donghongchen
 * @create 2018-02-05 16:56
 **/
public class ScriptComparator implements Comparator<ScriptBean> {
    private String field = null;
    private String type = null;  //desc asc

    public ScriptComparator(String field, String type) {
        this.field = field;
        this.type = type;
        if (StringUtils.isBlank(field)) {
            this.field = "createTime";
        }
        if (StringUtils.isBlank(type)) {
            this.type = "desc";
        }
    }

    @Override
    public int compare(ScriptBean o1, ScriptBean o2) {
        if (field.equalsIgnoreCase("id")) {
            //升序
            if ("asc".equalsIgnoreCase(type)) {
                if (o1 == null || o1.getId() == null) {
                    return -1;
                } else {
                    return o1.getId().compareTo(o2.getId());
                }
            } else { //降序
                if (o1 == null || o1.getId() == null) {
                    return 1;
                } else {
                    return -o1.getId().compareTo(o2.getId());
                }
            }
        } else if (field.equalsIgnoreCase("updateTime")) {
            //升序
            if ("asc".equalsIgnoreCase(type)) {
                if (o1 == null || o1.getUpdateTime() == null) {
                    return -1;
                } else {
                    return o1.getUpdateTime().compareTo(o2.getUpdateTime());
                }
            } else { //降序
                if (o1 == null || o1.getUpdateTime() == null) {
                    return 1;
                } else {
                    return -o1.getUpdateTime().compareTo(o2.getUpdateTime());
                }
            }
        }  else if (field.equalsIgnoreCase("createTime")) {
            //升序
            if ("asc".equalsIgnoreCase(type)) {
                if (o1 == null || o1.getCreateTime() == null) {
                    return -1;
                } else {
                    return o1.getCreateTime().compareTo(o2.getCreateTime());
                }
            } else { //降序
                if (o1 == null || o1.getCreateTime() == null) {
                    return 1;
                } else {
                    return -o1.getCreateTime().compareTo(o2.getCreateTime());
                }
            }
        } else if (field.equalsIgnoreCase("code")) {
            if ("asc".equalsIgnoreCase(type)) {
                if (o1 == null || o1.getCode() == null) {
                    return -1;
                } else {
                    return o1.getCode().compareTo(o2.getCode());
                }
            } else { //降序
                if (o1 == null || o1.getCode() == null) {
                    return 1;
                } else {
                    return -o1.getCode().compareTo(o2.getCode());
                }
            }
        }
        return 0;
    }
}
