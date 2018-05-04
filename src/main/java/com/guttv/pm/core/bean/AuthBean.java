package com.guttv.pm.core.bean;

public class AuthBean extends BaseBean{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long parentAuthId = null;

    private String name = null;

    private String url = null;

    public Long getParentAuthId() {
        return parentAuthId;
    }

    public void setParentAuthId(Long parentAuthId) {
        this.parentAuthId = parentAuthId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
