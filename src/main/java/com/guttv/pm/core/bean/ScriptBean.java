package com.guttv.pm.core.bean;

import java.util.Date;

/**
 * @author donghongchen
 * @create 2018-02-06 15:54
 **/
public class ScriptBean implements java.io.Serializable{

    private static final long serialVersionUID = 1L;

    /**
     * 文件路径，支持ftp和http协议
     */
    private String filePath;
    /**
     * 远端路径
     */
    private String remoteTarget;

    /**
     * 解压命令
     */
    private String decompressionCMD;

    /**
     * 执行命令
     */
    private String shCMD;

    /**
     * 编号
     */
    private String code;
    /**
     * 文件名
     */
    private String fileName;
    /**
     * 状态
     */
    private Integer status;

    private String desc;

    /**
     * 文件md5
     */
    private String md5;

    /**
     * 停止命令
     */
    private String shutdown;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getRemoteTarget() {
        return remoteTarget;
    }

    public void setRemoteTarget(String remoteTarget) {
        this.remoteTarget = remoteTarget;
    }

    public String getDecompressionCMD() {
        return decompressionCMD;
    }

    public void setDecompressionCMD(String decompressionCMD) {
        this.decompressionCMD = decompressionCMD;
    }

    public String getShCMD() {
        return shCMD;
    }

    public void setShCMD(String shCMD) {
        this.shCMD = shCMD;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getShutdown() {
        return shutdown;
    }

    public void setShutdown(String shutdown) {
        this.shutdown = shutdown;
    }


    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }


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
        return "ScriptBean{" +
                "filePath='" + filePath + '\'' +
                ", remoteTarget='" + remoteTarget + '\'' +
                ", decompressionCMD='" + decompressionCMD + '\'' +
                ", shCMD='" + shCMD + '\'' +
                ", code='" + code + '\'' +
                ", fileName='" + fileName + '\'' +
                ", status=" + status +
                ", desc='" + desc + '\'' +
                ", md5='" + md5 + '\'' +
                ", shutdown='" + shutdown + '\'' +
                '}';
    }
}
