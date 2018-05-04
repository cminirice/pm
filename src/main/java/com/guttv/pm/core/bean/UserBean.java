/**
 * 
 */
package com.guttv.pm.core.bean;

import java.util.Date;
import java.util.Set;

/**
 * @author Peter
 *
 */
public class UserBean{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3887558575039585374L;

	private String name = null;
	
	private String password = null;

	private Long roleId = null;

	private RoleBean roleBean = null;

	private Set<Long> auths = null;

	private String email = null;

	private Date updateTime;

	private Date createTime;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Long getRoleId() {
		return roleId;
	}

	public void setRoleId(Long roleId) {
		this.roleId = roleId;
	}

	public RoleBean getRoleBean() {
		return roleBean;
	}

	public void setRoleBean(RoleBean roleBean) {
		this.roleBean = roleBean;
	}

	public Set<Long> getAuths() {
		return auths;
	}

	public void setAuths(Set<Long> auths) {
		this.auths = auths;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
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
