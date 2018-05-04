package com.guttv.pm.core.bean;

import java.util.Date;
import java.util.Set;

public class RoleBean{

    private Long id = -1L;

    /**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private String name = null;

    private String description = null;

    private Set<Long> auths = null;

    private Date updateTime;

    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Long> getAuths() {
        return auths;
    }

    public void setAuths(Set<Long> auths) {
        this.auths = auths;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
