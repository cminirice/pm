/**
 * 
 */
package com.guttv.pm.core.bean;

/**
 * @author Peter
 *
 */
public class ComponentDispatchBean  extends BaseBean{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8753209798941086599L;

	//流程编码
	private String flowCode = null;
	
	//连接线ID
	private String lineID = null;
	
	// 对应流程中连接线的启点，记录nodeID
	private String fromNode = null;
	
	private String fromComponent = null;
	
	// 对应流程中连接线的终点，记录nodeID
	private String toNode = null;
	
	private String toComponent = null;
	
	//使用队列
	private String queue = null;
	
	//写规则
	private String rule = null;

	public String getFlowCode() {
		return flowCode;
	}

	public void setFlowCode(String flowCode) {
		this.flowCode = flowCode;
	}

	public String getLineID() {
		return lineID;
	}

	public void setLineID(String lineID) {
		this.lineID = lineID;
	}

	public String getFromNode() {
		return fromNode;
	}

	public void setFromNode(String fromNode) {
		this.fromNode = fromNode;
	}

	public String getToNode() {
		return toNode;
	}

	public void setToNode(String toNode) {
		this.toNode = toNode;
	}

	public String getQueue() {
		return queue;
	}

	public void setQueue(String queue) {
		this.queue = queue;
	}

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	public String getFromComponent() {
		return fromComponent;
	}

	public void setFromComponent(String fromComponent) {
		this.fromComponent = fromComponent;
	}

	public String getToComponent() {
		return toComponent;
	}

	public void setToComponent(String toComponent) {
		this.toComponent = toComponent;
	}
	
	public ComponentDispatchBean clone() {
		ComponentDispatchBean cdb = new ComponentDispatchBean();
		
		cdb.setFlowCode(flowCode);
		cdb.setFromComponent(fromComponent);
		cdb.setFromNode(fromNode);
		cdb.setLineID(lineID);
		cdb.setQueue(queue);
		cdb.setRule(rule);
		cdb.setToComponent(toComponent);
		cdb.setToNode(toNode);
		cdb.setId(this.getId());
		cdb.setUpdateTime(this.getUpdateTime());
		cdb.setCreateTime(this.getCreateTime());
		return cdb;
	}
}
