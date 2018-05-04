/**
 * 
 */
package com.guttv.pm.platform.action.meta.comparator;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import com.guttv.pm.core.bean.ComponentBean;

/**
 * @author Peter
 *
 */
public class ComponentComparator implements Comparator<ComponentBean> {
	private String field = null;
	private String type = null;  //desc asc
	public ComponentComparator(String field,String type) {
		this.field = field;
		this.type = type;
		if(StringUtils.isBlank(field)) {
			this.field = "id";
		}
		if(StringUtils.isBlank(type)) {
			this.type = "desc";
		}
	}
	
	@Override
	public int compare(ComponentBean o1, ComponentBean o2) {
		
		
		if(field.equalsIgnoreCase("id")) {
			//升序
			if("asc".equalsIgnoreCase(type)) {
				if(o1 == null || o1.getId() == null) {
					return -1;
				} else {
					return o1.getId().compareTo(o2.getId());
				}
			} else { //降序
				if(o1 == null || o1.getId() == null) {
					return 1;
				} else {
					return - o1.getId().compareTo(o2.getId());
				}
			}
		}else if(field.equalsIgnoreCase("updateTime")) {
			//升序
			if("asc".equalsIgnoreCase(type)) {
				if(o1 == null || o1.getUpdateTime() == null) {
					return -1;
				} else {
					return o1.getUpdateTime().compareTo(o2.getUpdateTime());
				}
			} else { //降序
				if(o1 == null || o1.getUpdateTime() == null) {
					return 1;
				} else {
					return - o1.getUpdateTime().compareTo(o2.getUpdateTime());
				}
			}
		}else if (field.equalsIgnoreCase("comID")) {
			//升序
			if("asc".equalsIgnoreCase(type)) {
				if(o1 == null || o1.getComID() == null) {
					return -1;
				} else {
					return o1.getComID().compareTo(o2.getComID());
				}
			} else { //降序
				if(o1 == null || o1.getComID() == null) {
					return 1;
				} else {
					return - o1.getComID().compareTo(o2.getComID());
				}
			}
		}else if (field.equalsIgnoreCase("group")) {
			//升序
			if("asc".equalsIgnoreCase(type)) {
				if(o1 == null || o1.getGroup() == null) {
					return -1;
				} else {
					return o1.getGroup().compareTo(o2.getGroup());
				}
			} else { //降序
				if(o1 == null || o1.getGroup() == null) {
					return 1;
				} else {
					return - o1.getGroup().compareTo(o2.getGroup());
				}
			}
		}else if (field.equalsIgnoreCase("name")) {
			//升序
			if("asc".equalsIgnoreCase(type)) {
				if(o1 == null || o1.getName() == null) {
					return -1;
				} else {
					return o1.getName().compareTo(o2.getName());
				}
			} else { //降序
				if(o1 == null || o1.getName() == null) {
					return 1;
				} else {
					return - o1.getName().compareTo(o2.getName());
				}
			}
		}else if (field.equalsIgnoreCase("cn")) {
			//升序
			if("asc".equalsIgnoreCase(type)) {
				if(o1 == null || o1.getCn() == null) {
					return -1;
				} else {
					return o1.getCn().compareTo(o2.getCn());
				}
			} else { //降序
				if(o1 == null || o1.getCn() == null) {
					return 1;
				} else {
					return - o1.getCn().compareTo(o2.getCn());
				}
			}
		}else if (field.equalsIgnoreCase("clz")) {
			//升序
			if("asc".equalsIgnoreCase(type)) {
				if(o1 == null || o1.getClz() == null) {
					return -1;
				} else {
					return o1.getClz().compareTo(o2.getClz());
				}
			} else { //降序
				if(o1 == null || o1.getClz() == null) {
					return 1;
				} else {
					return - o1.getClz().compareTo(o2.getClz());
				}
			}
		}else if (field.equalsIgnoreCase("method")) {
			//升序
			if("asc".equalsIgnoreCase(type)) {
				if(o1 == null || o1.getMethod() == null) {
					return -1;
				} else {
					return o1.getMethod().compareTo(o2.getMethod());
				}
			} else { //降序
				if(o1 == null || o1.getMethod() == null) {
					return 1;
				} else {
					return - o1.getMethod().compareTo(o2.getMethod());
				}
			}
		}else if (field.equalsIgnoreCase("runType")) {
			//升序
			if("asc".equalsIgnoreCase(type)) {
				if(o1 == null || o1.getRunType() == null) {
					return -1;
				} else {
					return o1.getRunType().compareTo(o2.getRunType());
				}
			} else { //降序
				if(o1 == null || o1.getRunType() == null) {
					return 1;
				} else {
					return - o1.getRunType().compareTo(o2.getRunType());
				}
			}
		}else if (field.equalsIgnoreCase("status")) {
			//升序
			if("asc".equalsIgnoreCase(type)) {
				if(o1 == null || o1.getStatus() == null) {
					return -1;
				} else {
					return o1.getStatus().compareTo(o2.getStatus());
				}
			} else { //降序
				if(o1 == null || o1.getStatus() == null) {
					return 1;
				} else {
					return - o1.getStatus().compareTo(o2.getStatus());
				}
			}
		}
		
		return 0;
	}

}
