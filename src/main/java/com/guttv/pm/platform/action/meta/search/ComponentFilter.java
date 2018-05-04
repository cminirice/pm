/**
 * 
 */
package com.guttv.pm.platform.action.meta.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.file.Matcher;

import com.guttv.pm.core.bean.ComponentBean;
import com.guttv.pm.platform.action.SearchFilter;

/**
 * @author Peter
 *
 */
public class ComponentFilter implements SearchFilter<ComponentBean>{

	/*
	 * (non-Javadoc)
	 * @see com.guttv.pm.view.action.meta.search.SearchFilter#filter(java.util.List, java.lang.String, java.lang.String)
	 */
	@Override
	public  List<ComponentBean> filter(List<ComponentBean> list, String filterField, String value) {
		if(StringUtils.isBlank(filterField) || StringUtils.isBlank(value) || list == null || list.size() == 0) {
			return list;
		}
		
		List<ComponentBean> components = new ArrayList<ComponentBean>();
		
		String pattern = "*"+value+"*";
		//循环查找吧
		for(ComponentBean com : list) {
			
			//分为同的字段，不同的查询方法
			if(filterField.equals("comID")) {
				if(StringUtils.isNotBlank(com.getComID()) && Matcher.match(pattern, com.getComID(), false)) {
					components.add(com);
				}
			}else if(filterField.equals("group")) {
				if(StringUtils.isNotBlank(com.getGroup()) && Matcher.match(pattern, com.getGroup(), false)) {
					components.add(com);
				}
			}else if(filterField.equals("name")) {
				if(StringUtils.isNotBlank(com.getName()) && Matcher.match(pattern, com.getName(), false)) {
					components.add(com);
				}
			}else if(filterField.equals("cn")) {
				if(StringUtils.isNotBlank(com.getCn()) && Matcher.match(pattern, com.getCn(), false)) {
					components.add(com);
				}
			}else if(filterField.equals("clz")) {
				if(StringUtils.isNotBlank(com.getClz()) && Matcher.match(pattern, com.getClz(), false)) {
					components.add(com);
				}
			}else {
				//在没有实现的情况下，把所有的返回
				components.add(com);
			}
		}
		
		return components;
	}
	
	public static void main(String a[]) {
		String pattern = "*omc*";
		
		String src = "omc-v1.0";
		
		System.out.println(Matcher.match(pattern, src, false));
	}

}
