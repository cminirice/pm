/**
 * 
 */
package com.guttv.pm.core.bean;

import com.guttv.pm.code.ann.FieldMeta;
import com.guttv.pm.code.ann.TableMeta;
import com.guttv.pm.utils.Enums.ExecuteContainerStatus;

/**
 * @author Peter
 *
 */
@TableMeta(cn="执行容器",pkg="container")
public class ExecuteContainer {
	
	@FieldMeta(cn="容器别名",length=64,list=true,required=true,edit=true,search=true,sort=true)
	private String alias = null; // 别名
	
	@FieldMeta(cn="容器IP",length=64,list=true,required=true,edit=false,search=true,sort=true)
	private String ip = null;
	
	@FieldMeta(cn="容器端口",length=8,list=true,required=true,edit=false,search=false,sort=false)
	private int serverPort = -1;
	
	@FieldMeta(cn="主机名称",length=128,list=true,required=true,edit=false,search=true,sort=true)
	private String hostname = null; // 主机名
	
	@FieldMeta(cn="进程ID",length=8,list=true,required=true,edit=false,search=false,sort=false)
	private String pid = null;
	
	@FieldMeta(cn="上下文",length=16,list=true,required=false,edit=false,search=false,sort=false)
	private String contextPath = null;
	
	@FieldMeta(cn="启动时间",length=32,list=true,required=true,edit=false,search=false,sort=true)
	private String latestStartTime = null; // 启动时间
	
	@FieldMeta(cn="容器状态",length=32,list=true,required=true,edit=false,search=false,sort=true)
	private ExecuteContainerStatus status = null;
	
	@FieldMeta(cn="容ID",length=32,list=true,required=false,edit=false,search=false,sort=false)
	private String containerID = null;
	
	@FieldMeta(cn="RPC端口",length=32,list=false,required=true,edit=false,search=false,sort=false)
	private int rpcServerPort = -1; // bootstrap RPC服务端口
	
	@FieldMeta(cn="IP列表",length=256,list=false,required=false,edit=false,search=false,sort=false)
	private String ipList = null;
	
	@FieldMeta(cn="用户目录",length=256,list=false,required=false,edit=false,search=false,sort=false)
	private String userDir = null; // 用户目录
	
	@FieldMeta(cn="心跳周期",length=8,list=false,required=true,edit=true,search=false,sort=false)
	private long heartbeatPeriod = 0;
	
	@FieldMeta(cn="仅容器",length=8,list=true,required=false,edit=false,search=false,sort=false)
	private boolean onlyContainer = true;
	
	@FieldMeta(cn="注册路径",length=256,list=false,required=false,edit=false,search=false,sort=false)
	private String registPath = null; // 注册到zk上的路径
	
	@FieldMeta(cn="容器目录",length=256,list=false,required=false,edit=false,search=false,sort=false)
	private String location = null; // 程序目录
	
	@FieldMeta(cn="创建时间",length=32,list=false,required=false,edit=false,search=false,sort=false)
	private String createTime = null;
	
	@FieldMeta(cn="启动用户",length=32,list=false,required=false,edit=false,search=false,sort=false)
	private String operator = null; // 操作用户
	
	@FieldMeta(cn="更新时间",length=32,list=false,required=false,edit=false,search=false,sort=false)
	private String updateTime = null;
	
	@FieldMeta(cn="备注",length=256,list=false,required=false,edit=true,search=false,sort=false)
	private String remark = null;// 备注
	
	@FieldMeta(cn="状态描述",length=256,list=false,required=false,edit=false,search=false,sort=false)
	private String statusDesc = null;

	@FieldMeta(cn="心跳信息",length=256,list=false,required=false,edit=false,search=false,sort=false)
	private Heartbeat heartbeat = null;
	

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getLatestStartTime() {
		return latestStartTime;
	}

	public void setLatestStartTime(String latestStartTime) {
		this.latestStartTime = latestStartTime;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getIpList() {
		return ipList;
	}

	public void setIpList(String ipList) {
		this.ipList = ipList;
	}

	public long getHeartbeatPeriod() {
		return heartbeatPeriod;
	}

	public void setHeartbeatPeriod(long heartbeatPeriod) {
		this.heartbeatPeriod = heartbeatPeriod;
	}

	public String getContainerID() {
		return containerID;
	}

	public void setContainerID(String containerID) {
		this.containerID = containerID;
	}

	public String getRegistPath() {
		return registPath;
	}

	public void setRegistPath(String registPath) {
		this.registPath = registPath;
	}

	public Heartbeat getHeartbeat() {
		return heartbeat;
	}

	public void setHeartbeat(Heartbeat heartbeat) {
		this.heartbeat = heartbeat;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public String getUserDir() {
		return userDir;
	}

	public void setUserDir(String userDir) {
		this.userDir = userDir;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public int getRpcServerPort() {
		return rpcServerPort;
	}

	public void setRpcServerPort(int rpcServerPort) {
		this.rpcServerPort = rpcServerPort;
	}

	public ExecuteContainerStatus getStatus() {
		return status;
	}

	public void setStatus(ExecuteContainerStatus status) {
		this.status = status;
	}

	public String getStatusDesc() {
		return statusDesc;
	}

	public void setStatusDesc(String statusDesc) {
		this.statusDesc = statusDesc;
	}

	public boolean isOnlyContainer() {
		return onlyContainer;
	}

	public void setOnlyContainer(boolean onlyContainer) {
		this.onlyContainer = onlyContainer;
	}

}
