/**
 * 
 */
package com.guttv.pm.core.bean;

import java.util.ArrayList;
import java.util.List;

import com.guttv.pm.code.ann.FieldMeta;
import com.guttv.pm.code.ann.TableMeta;
import com.guttv.pm.utils.Enums.ComponentRunType;
import com.guttv.pm.utils.Enums.ComponentStatus;

/**
 * 
 * 记录原始组件配置的信息
 * @author Peter
 *
 */
@TableMeta(cn="组件",pkg="component")
public class ComponentBean extends BaseBean{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1859041596340137782L;

	@FieldMeta(cn="组件标识",length=128,list=true,required=true,edit=false,search=true,sort=false)
	private String comID = null;  //组件的唯一标识
	
	@FieldMeta(cn="组",length=128,list=true,required=true,edit=false,search=true,sort=false)
	private String group = null;
	
	@FieldMeta(cn="名称",length=128,list=true,required=true,edit=false,search=true,sort=true)
	private String name = null;  //组件名称
	
	@FieldMeta(cn="中文名",length=128,list=true,required=true,edit=true,search=true,sort=true)
	private String cn = null;  //组件名称
	
	@FieldMeta(cn="类名",length=128,list=true,required=true,edit=false,search=true,sort=false)
	private String clz = null;   //组件实现类
	
	//组件执行的方法，与needRead,needWrite组合使用，如果needRead为true时，该方法必须有一个Object参数，如果needWrite为true时，
	//该方法可以有一个返回值
	@FieldMeta(cn="方法名",length=64,list=true,required=false,edit=false,search=false,sort=false)
	private String method = null;
	
	@FieldMeta(cn="初始化方法名",length=64,list=false,required=false,edit=false,search=false,sort=false)
	private String initMethod = null;
	
	@FieldMeta(cn="关闭方法名",length=64,list=false,required=false,edit=false,search=false,sort=false)
	private String closeMethod = null;
	
	//方法执行的类型
	@FieldMeta(cn="运行类型",length=16,list=true,required=true,edit=false,search=false,sort=false)
	private ComponentRunType runType = null;
	
	@FieldMeta(cn="线程数",length=1,list=false,required=false,edit=false,search=false,sort=false)
	private int threadNum = 1;   //现程数
	
	@FieldMeta(cn="状态",length=2,list=false,required=true,edit=false,search=false,sort=false)
	private Integer status = ComponentStatus.NORMAL.getValue();   //状态
	
	@FieldMeta(cn="需要读",length=8,list=false,required=true,edit=false,search=false,sort=false)
	private boolean needRead = true;  //是否需要从其它读数据
	
	@FieldMeta(cn="需要写",length=8,list=false,required=true,edit=false,search=false,sort=false)
	private boolean needWrite = true;  //是否需要向其它写数据
	
	@FieldMeta(cn="队列类型",length=32,list=false,required=false,edit=false,search=false,sort=false)
	private String queueType = null;  //使用队列的类型
	
	@FieldMeta(cn="接收队列",length=256,list=false,required=false,edit=true,search=false,sort=false)
	private String receive = null; //接收JOB通道
	
	@FieldMeta(cn="描述信息",length=1024,list=false,required=false,edit=false,search=false,sort=false)
	private String description = null;
	
	private List<ComponentDispatchBean> dispatchs = null; //发送数据通道
	
	private List<ComponentProBean> componentPros = null;  //任务属性
	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCn() {
		return cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}

	public String getClz() {
		return clz;
	}

	public void setClz(String clz) {
		this.clz = clz;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getInitMethod() {
		return initMethod;
	}

	public void setInitMethod(String initMethod) {
		this.initMethod = initMethod;
	}

	public String getCloseMethod() {
		return closeMethod;
	}

	public void setCloseMethod(String closeMethod) {
		this.closeMethod = closeMethod;
	}

	public ComponentRunType getRunType() {
		return runType;
	}

	public void setRunType(ComponentRunType type) {
		this.runType = type;
	}

	public int getThreadNum() {
		return threadNum;
	}

	public void setThreadNum(int threadNum) {
		this.threadNum = threadNum;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public boolean isNeedRead() {
		return needRead;
	}

	public void setNeedRead(boolean needRead) {
		this.needRead = needRead;
	}

	public boolean isNeedWrite() {
		return needWrite;
	}

	public void setNeedWrite(boolean needWrite) {
		this.needWrite = needWrite;
	}

	public String getQueueType() {
		return queueType;
	}

	public void setQueueType(String queueType) {
		this.queueType = queueType;
	}

	public String getReceive() {
		return receive;
	}

	public void setReceive(String receive) {
		this.receive = receive;
	}

	public List<ComponentDispatchBean> getDispatchs() {
		return dispatchs;
	}

	public void setDispatchs(List<ComponentDispatchBean> dispatchs) {
		this.dispatchs = dispatchs;
	}

	public List<ComponentProBean> getComponentPros() {
		return componentPros;
	}

	public void setComponentPros(List<ComponentProBean> componentPros) {
		this.componentPros = componentPros;
	}

	public String getComID() {
		return comID;
	}

	public void setComID(String comID) {
		this.comID = comID;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	@Override
	public ComponentBean clone(){
		ComponentBean com = new ComponentBean();
		com.setClz(this.getClz());
		com.setCn(this.getCn());
		com.setComID(this.getComID());
		com.setGroup(this.getGroup());
		com.setMethod(this.getMethod());
		com.setInitMethod(this.getInitMethod());
		com.setCloseMethod(this.getCloseMethod());
		com.setName(this.getName());
		com.setNeedRead(this.isNeedRead());
		com.setNeedWrite(this.isNeedWrite());
		com.setQueueType(this.getQueueType());
		com.setReceive(this.getReceive());
		com.setRunType(this.getRunType());
		com.setStatus(this.getStatus());
		com.setThreadNum(this.getThreadNum());
		com.setDescription(this.description);
		com.setId(this.getId());
		com.setUpdateTime(this.getUpdateTime());
		com.setCreateTime(this.getCreateTime());
		
		if(this.getComponentPros() != null) {
			List<ComponentProBean> cpp = new ArrayList<ComponentProBean>();
			com.setComponentPros(cpp);
			for(ComponentProBean c : this.getComponentPros()) {
				cpp.add(c.clone());
			}
		}
		
		if(this.getDispatchs() != null) {
			List<ComponentDispatchBean> cdb = new ArrayList<ComponentDispatchBean>();
			com.setDispatchs(cdb);
			for(ComponentDispatchBean c : this.getDispatchs()) {
				cdb.add(c.clone());
			}
		}
		
		return com;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
