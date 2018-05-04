/**
 * 
 */
package com.guttv.pm.platform.action.meta.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.file.Matcher;

import com.guttv.pm.core.bean.ComponentPackageBean;
import com.guttv.pm.platform.action.SearchFilter;

/**
 * @author Peter
 *
 */
public class ComponentPackageFilter implements SearchFilter<ComponentPackageBean> {

	/*
	 * (non-Javadoc)
	 * @see com.guttv.pm.view.action.meta.search.SearchFilter#filter(java.util.List, java.lang.String, java.lang.String)
	 */
	@Override
	public List<ComponentPackageBean> filter(List<ComponentPackageBean> list, String filterField, String value) {

		if(StringUtils.isBlank(filterField) || StringUtils.isBlank(value) || list == null || list.size() == 0) {
			return list;
		}
		
		List<ComponentPackageBean> components = new ArrayList<ComponentPackageBean>();
		
		String pattern = "*"+value+"*";
		//循环查找吧
		for(ComponentPackageBean comPack : list) {
			
			//分为同的字段，不同的查询方法
			if(filterField.equals("comID")) {
				if(StringUtils.isNotBlank(comPack.getComID()) && Matcher.match(pattern, comPack.getComID(), false)) {
					components.add(comPack);
				}
			} else {
				//在没有实现的情况下，把所有的返回
				components.add(comPack);
			}
		}
		
		return components;
	
	}

}
