/**
 * 
 */
package com.guttv.pm.core.flow;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.guttv.pm.core.bean.ComponentDispatchBean;
import com.guttv.pm.core.bean.ComponentFlowProBean;
import com.guttv.pm.core.bean.ComponentNodeBean;
import com.guttv.pm.core.bean.FlowBean;
import com.guttv.pm.utils.Enums.FlowExecuteStatus;

/**
 * @author Peter
 *
 */
public class FlowExecuteConfig {

	private Long id = -1L;

	// 流程执行编号
	private String flowExeCode = null;

	private FlowBean flow = null;

	// 流程执行状态
	private FlowExecuteStatus status = FlowExecuteStatus.INIT;

	// 流程执行的状态描述
	private String statusDesc = null;

	private List<ComponentDispatchBean> comDispatchs = null;

	// 保存流程的组件属性
	// <nodeID, List<ComponentFlowProBean> >
	private Map<String, List<ComponentFlowProBean>> comFlowProsMap = null;

	private List<ComponentNodeBean> comNodes = null;

	public ComponentNodeBean getComponentNode(String nodeID) {
		if (comNodes == null || comNodes.size() == 0 || StringUtils.isBlank(nodeID)) {
			return null;
		}

		for (ComponentNodeBean cnb : comNodes) {
			if (nodeID.equals(cnb.getNodeID())) {
				return cnb;
			}
		}

		return null;
	}

	/**
	 * 获取节点的属性
	 * 
	 * @param nodeID
	 * @return
	 */
	public List<ComponentFlowProBean> getComFlowPros(String nodeID) {
		return comFlowProsMap.get(nodeID);
	}

	// 获取节点和组件的对应关系 <nodeID,ComClz>
	public Map<String, String> getNodevsComMap() {
		if (comNodes == null || comNodes.size() == 0) {
			return null;
		}

		Map<String, String> nodexvsCom = new HashMap<String, String>();
		for (ComponentNodeBean tnb : comNodes) {
			nodexvsCom.put(tnb.getNodeID(), tnb.getComponentClz());
		}
		return nodexvsCom;
	}

	/**
	 * 获取本流程执行配置用到的comID
	 * 
	 * @return
	 */
	public Set<String> getUsedComIDs() {
		Set<String> comIDs = new HashSet<String>();
		if (comNodes == null || comNodes.size() == 0) {
			return comIDs;
		}

		for (ComponentNodeBean cnb : comNodes) {
			comIDs.add(cnb.getComID());
		}
		return comIDs;
	}

	public String getFlowCode() {
		if (flow == null) {
			return null;
		}
		return flow.getCode();
	}

	public String getFlowName() {
		if (flow == null) {
			return null;
		}
		return flow.getName();
	}

	public String getFlowExeCode() {
		return flowExeCode;
	}

	public void setFlowExeCode(String flowExeCode) {
		this.flowExeCode = flowExeCode;
	}

	public FlowBean getFlow() {
		return flow;
	}

	public FlowExecuteStatus getStatus() {
		return status;
	}

	public void setStatus(FlowExecuteStatus status) {
		if (status == null)
			return;
		this.setStatus(status, status.getName());
	}

	public void setStatus(FlowExecuteStatus status, String statusDesc) {
		this.status = status;
		synchronized (statusDescList) {
			while (statusDescList.size() > 10) {
				statusDescList.remove(0);
			}
			if (StringUtils.isNotBlank(statusDesc) && !statusDesc.equals(this.statusDesc)) {
				statusDescList.add(new StatusDesc(statusDesc));
				this.statusDesc = statusDesc;
			}
		}
	}

	public void setFlow(FlowBean flow) {
		this.flow = flow;
	}

	public List<ComponentDispatchBean> getComDispatchs() {
		return comDispatchs;
	}

	public void setComDispatch(List<ComponentDispatchBean> comDispatchs) {
		this.comDispatchs = comDispatchs;
	}

	public Map<String, List<ComponentFlowProBean>> getComFlowProsMap() {
		return comFlowProsMap;
	}

	public void setComFlowProsMap(Map<String, List<ComponentFlowProBean>> comFlowProsMap) {
		this.comFlowProsMap = comFlowProsMap;
	}

	public void setComNodes(List<ComponentNodeBean> comNodes) {
		this.comNodes = comNodes;
	}

	public String getStatusDesc() {
		return statusDesc;
	}

	public void setStatusDesc(String statusDesc) {
		setStatus(this.getStatus(), statusDesc);
	}

	// 记录几条状态描述信息
	private final List<StatusDesc> statusDescList = new LinkedList<StatusDesc>();

	public List<StatusDesc> getStatusDescList() {
		return statusDescList;
	}

	public void cleanStatusDesc() {
		statusDescList.clear();
	}

	public static class StatusDesc {
		StatusDesc(String desc) {
			this.desc = desc;
		}

		private String desc = null;
		private Date createTime = new Date();

		public String getDesc() {
			return desc;
		}

		public void setDesc(String desc) {
			this.desc = desc;
		}

		public Date getCreateTime() {
			return createTime;
		}

		public void setCreateTime(Date createTime) {
			this.createTime = createTime;
		}
	}

	/**
	 * 获取流程中所有的组件节点
	 * 
	 * @return
	 */
	public List<ComponentNodeBean> getComNodes() {
		return comNodes;
	}

	private Date updateTime = new Date();
	private Date createTime = new Date();

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
}
