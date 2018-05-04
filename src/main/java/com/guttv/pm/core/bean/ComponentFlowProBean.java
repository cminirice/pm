/**
 * 
 */
package com.guttv.pm.core.bean;

/**
 * @author Peter
 *
 */
public class ComponentFlowProBean extends ComponentProBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2167576439731658135L;

	private String flowCode = null; //流程编码
	
	private String nodeID = null;  //对应流程中node的ID

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
	
	public ComponentFlowProBean clone() {
		ComponentFlowProBean cfpb = new ComponentFlowProBean();
		cfpb.setCn(this.getCn());
		cfpb.setComponentClz(this.getComponentClz());
		cfpb.setName(this.getName());
		cfpb.setType(this.getType());
		cfpb.setValue(this.getValue());
		cfpb.setFlowCode(flowCode);
		cfpb.setNodeID(nodeID);
		cfpb.setId(this.getId());
		cfpb.setUpdateTime(this.getUpdateTime());
		cfpb.setCreateTime(this.getCreateTime());
		return cfpb;
	}
}
