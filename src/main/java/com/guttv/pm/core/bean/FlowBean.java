/**
 * 
 */
package com.guttv.pm.core.bean;

import com.guttv.pm.code.ann.FieldMeta;
import com.guttv.pm.code.ann.TableMeta;
import com.guttv.pm.utils.Enums.FlowStatus;

/**
 * 
 * 记录流程的信息  与页面画的流程对应
 * 
 * @author Peter
 *
 */
@TableMeta(cn="流程",pkg="flow")
public class FlowBean extends BaseBean{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2346030341682987004L;
	
	@FieldMeta(cn="流程编码",length=32,list=true,required=true,edit=false,search=true,sort=false)
	private String code = null;
	
	@FieldMeta(cn="流程名称",length=128,list=true,required=true,edit=true,search=true,sort=false)
	private String name = null;
	
	@FieldMeta(cn="状态",length=32,list=true,required=true,edit=false,search=true,sort=false)
	private FlowStatus status = FlowStatus.NORMAL;
	
	//状态描述
	@FieldMeta(cn="状态描述",length=256,list=true,required=false,edit=true,search=false,sort=false)
	private String statusDesc = null;
	
	//流程说明
	@FieldMeta(cn="备注",length=1024,list=false,required=false,edit=true,search=false,sort=false)
	private String remark = null;
	
	//流程的内容，用来恢复界面编辑
	private String flowContent = null;
	
	//组件属性内容
	private String flowComPros = null;
	
	//组件和节点的对应关系
	private String nodeVSCom = null;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public FlowStatus getStatus() {
		return status;
	}

	public void setStatus(FlowStatus status) {
		this.status = status;
	}

	public String getStatusDesc() {
		return statusDesc;
	}

	public void setStatusDesc(String statusDesc) {
		this.statusDesc = statusDesc;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getFlowContent() {
		return flowContent;
	}

	public void setFlowContent(String flowContent) {
		this.flowContent = flowContent;
	}

	public String getFlowComPros() {
		return flowComPros;
	}

	public void setFlowComPros(String flowComPros) {
		this.flowComPros = flowComPros;
	}

	public String getNodeVSCom() {
		return nodeVSCom;
	}

	public void setNodeVSCom(String nodeVSCom) {
		this.nodeVSCom = nodeVSCom;
	}
	
	public FlowBean clone() {
		FlowBean flow = new FlowBean();
		flow.setCode(code);
		flow.setFlowComPros(flowComPros);
		flow.setFlowContent(flowContent);
		flow.setName(name);
		flow.setNodeVSCom(nodeVSCom);
		flow.setRemark(remark);
		flow.setStatus(status);
		flow.setStatusDesc(statusDesc);
		flow.setId(this.getId());
		flow.setUpdateTime(this.getUpdateTime());
		flow.setCreateTime(this.getCreateTime());
		return flow;
	}
}
