/**
 * 
 */
package com.guttv.pm.platform.action.meta.comparator;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import com.guttv.pm.core.flow.FlowExecuteConfig;

/**
 * @author Peter
 *
 */
public class FlowExecuteConfigComparator implements Comparator<FlowExecuteConfig> {
	private String field = null;
	private String type = null;  //desc asc
	public FlowExecuteConfigComparator(String field,String type) {
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
	public int compare(FlowExecuteConfig o1, FlowExecuteConfig o2) {
		
		
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
		}else if (field.equalsIgnoreCase("flowExeCode")) {
			//升序
			if("asc".equalsIgnoreCase(type)) {
				if(o1 == null || o1.getFlowExeCode() == null) {
					return -1;
				} else {
					return o1.getFlowExeCode().compareTo(o2.getFlowExeCode());
				}
			} else { //降序
				if(o1 == null || o1.getFlowExeCode() == null) {
					return 1;
				} else {
					return - o1.getFlowExeCode().compareTo(o2.getFlowExeCode());
				}
			}
		}else if (field.equalsIgnoreCase("flowCode")) {
			//升序
			if("asc".equalsIgnoreCase(type)) {
				if(o1 == null || o1.getFlowCode() == null) {
					return -1;
				} else {
					return o1.getFlowCode().compareTo(o2.getFlowCode());
				}
			} else { //降序
				if(o1 == null || o1.getFlowCode() == null) {
					return 1;
				} else {
					return - o1.getFlowCode().compareTo(o2.getFlowCode());
				}
			}
		}else if (field.equalsIgnoreCase("flowName")) {
			//升序
			if("asc".equalsIgnoreCase(type)) {
				if(o1 == null || o1.getFlowName() == null) {
					return -1;
				} else {
					return o1.getFlowName().compareTo(o2.getFlowName());
				}
			} else { //降序
				if(o1 == null || o1.getFlowName() == null) {
					return 1;
				} else {
					return - o1.getFlowName().compareTo(o2.getFlowName());
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
