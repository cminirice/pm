/**
 * 
 */
package com.guttv.pm.core.bean;

import com.guttv.pm.utils.Enums.ComponentProType;

/**
 * 
 * 组件属性 与配置文件中的属性对应
 * @author Peter
 *
 */
public class ComponentProBean extends BaseBean {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6384427086864528310L;

	//组件的类名称
	private String componentClz = null;
	
	//属性的类型
	private ComponentProType type = ComponentProType.NOR;
	
	//属性中文名称
	private String cn = null;
	
	//属性英文名称
	private String name = null;
	
	//属性值
	private String value= null;

	public String getComponentClz() {
		return componentClz;
	}

	public void setComponentClz(String componentClz) {
		this.componentClz = componentClz;
	}

	public ComponentProType getType() {
		return type;
	}

	public void setType(ComponentProType type) {
		this.type = type;
	}

	public String getCn() {
		return cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public ComponentProBean clone() {
		ComponentProBean cpb = new ComponentProBean();
		cpb.setCn(this.getCn());
		cpb.setComponentClz(this.getComponentClz());
		cpb.setName(this.getName());
		cpb.setType(type);
		cpb.setValue(value);
		cpb.setId(this.getId());
		cpb.setUpdateTime(this.getUpdateTime());
		cpb.setCreateTime(this.getCreateTime());
		return cpb;
	}
}
