/**
 * 
 */
package com.guttv.pm.platform.action.meta.comparator;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import com.guttv.pm.core.bean.FlowBean;

/**
 * @author Peter
 *
 */
public class FlowComparator implements Comparator<FlowBean> {
	private String field = null;
	private String type = null;  //desc asc
	public FlowComparator(String field,String type) {
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
	public int compare(FlowBean o1, FlowBean o2) {
		
		
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
		}else if (field.equalsIgnoreCase("code")) {
			//升序
			if("asc".equalsIgnoreCase(type)) {
				if(o1 == null || o1.getCode() == null) {
					return -1;
				} else {
					return o1.getCode().compareTo(o2.getCode());
				}
			} else { //降序
				if(o1 == null || o1.getCode() == null) {
					return 1;
				} else {
					return - o1.getCode().compareTo(o2.getCode());
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
