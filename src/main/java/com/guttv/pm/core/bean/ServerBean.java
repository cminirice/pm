package com.guttv.pm.core.bean;

import java.util.Date;

/**
 * @author donghongchen
 * @create 2018-01-26 10:40
 **/
//@TableMeta
public class ServerBean implements java.io.Serializable{

    private static final long serialVersionUID = 1L;

    private String ip;

    private Integer port;

    private String userName;

    private String password;

    private String homePath;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHomePath() {
        return homePath;
    }

    public void setHomePath(String homePath) {
        this.homePath = homePath;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    private String code;

    private Date updateTime ;

    private Date createTime;

    private Long id = -1L;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ServerBean{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", homePath='" + homePath + '\'' +
                ", code='" + code + '\'' +
                ", updateTime=" + updateTime +
                ", createTime=" + createTime +
                ", id=" + id +
                '}';
    }
}
