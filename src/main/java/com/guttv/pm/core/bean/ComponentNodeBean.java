/**
 * 
 */
package com.guttv.pm.core.bean;

import java.util.Date;

import com.guttv.pm.utils.Enums.ComponentNodeStatus;

/**
 * @author Peter
 *
 */
public class ComponentNodeBean extends BaseBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = -431010242208428762L;

	private String flowCode = null;
	
	private String nodeID = null;
	
	private String componentClz = null;
	
	private String componentCn = null;
	
	private String comID = null;
	
	//
	private ComponentNodeStatus status = ComponentNodeStatus.INIT;
	
	private String statusDesc = null;

	public String getFlowCode() {
		return flowCode;
	}

	public void setFlowCode(String flowCode) {
		this.flowCode = flowCode;
	}

	public String getNodeID() {
		return nodeID;
	}

	public void setNodeID(String nodeID) {
		this.nodeID = nodeID;
	}

	public String getComponentClz() {
		return componentClz;
	}

	public void setComponentClz(String componentClz) {
		this.componentClz = componentClz;
	}

	public ComponentNodeStatus getStatus() {
		return status;
	}

	public void setStatus(ComponentNodeStatus status) {
		if(this.status == null || !this.status.equals(status)) {
			this.setUpdateTime(new Date());
		}
		this.status = status;
	}

	public String getComponentCn() {
		return componentCn;
	}

	public void setComponentCn(String componentCn) {
		this.componentCn = componentCn;
	}
	
	public String getComID() {
		return comID;
	}

	public void setComID(String comID) {
		this.comID = comID;
	}

	public String getStatusDesc() {
		return statusDesc;
	}

	public void setStatusDesc(String statusDesc) {
		this.statusDesc = statusDesc;
	}

	public ComponentNodeBean clone() {
		ComponentNodeBean cnb = new ComponentNodeBean();
		cnb.setComponentClz(this.componentClz);
		cnb.setComponentCn(componentCn);
		cnb.setComID(comID);
		cnb.setFlowCode(flowCode);
		cnb.setNodeID(nodeID);
		cnb.setStatus(status);
		cnb.setStatusDesc(this.statusDesc);
		cnb.setId(this.getId());
		cnb.setUpdateTime(this.getUpdateTime());
		cnb.setCreateTime(this.getCreateTime());
		return cnb;
	}
}
