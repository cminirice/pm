/**
 * 
 */
package com.guttv.pm.platform.action.meta.comparator;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import com.guttv.pm.core.bean.ComponentPackageBean;

/**
 * @author Peter
 *
 */
public class ComponentPackageComparator implements Comparator<ComponentPackageBean> {


	private String field = null;
	private String type = null;  //desc asc
	public ComponentPackageComparator(String field,String type) {
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
	public int compare(ComponentPackageBean o1, ComponentPackageBean o2) {
		
		
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
		}else if (field.equalsIgnoreCase("md5")) {
			//升序
			if("asc".equalsIgnoreCase(type)) {
				if(o1 == null || o1.getMd5() == null) {
					return -1;
				} else {
					return o1.getMd5().compareTo(o2.getMd5());
				}
			} else { //降序
				if(o1 == null || o1.getMd5() == null) {
					return 1;
				} else {
					return - o1.getMd5().compareTo(o2.getMd5());
				}
			}
		}else if (field.equalsIgnoreCase("comPackageFilePath")) {
			//升序
			if("asc".equalsIgnoreCase(type)) {
				if(o1 == null || o1.getComPackageFilePath() == null) {
					return -1;
				} else {
					return o1.getComPackageFilePath().compareTo(o2.getComPackageFilePath());
				}
			} else { //降序
				if(o1 == null || o1.getComPackageFilePath() == null) {
					return 1;
				} else {
					return - o1.getComPackageFilePath().compareTo(o2.getComPackageFilePath());
				}
			}
		}
		
		return 0;
	}


}
