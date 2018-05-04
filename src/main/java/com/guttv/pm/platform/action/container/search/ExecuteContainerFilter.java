/**
 * 
 */
package com.guttv.pm.platform.action.container.search;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.file.Matcher;

import com.guttv.pm.core.bean.ExecuteContainer;
import com.guttv.pm.platform.action.SearchFilter;

/**
 * @author Peter
 *
 */
public class ExecuteContainerFilter implements SearchFilter<ExecuteContainer> {

	@Override
	public List<ExecuteContainer> filter(List<ExecuteContainer> list, String filterField, String value) {
		if (StringUtils.isBlank(filterField) || StringUtils.isBlank(value) || list == null || list.size() == 0) {
			return list;
		}

		List<ExecuteContainer> containers = new ArrayList<ExecuteContainer>();

		String pattern = "*" + value + "*";
		// 循环查找吧
		for (ExecuteContainer container : list) {

			// 分为同的字段，不同的查询方法
			if (filterField.equals("alias")) {
				if (StringUtils.isNotBlank(container.getAlias()) && Matcher.match(pattern, container.getAlias(), false)) {
					containers.add(container);
				}
			} else if (filterField.equals("ip")) {
				if (StringUtils.isNotBlank(container.getIp()) && Matcher.match(pattern, container.getIp(), false)) {
					containers.add(container);
				}
			} else if (filterField.equals("hostname")) {
				if (StringUtils.isNotBlank(container.getHostname()) && Matcher.match(pattern, container.getHostname(), false)) {
					containers.add(container);
				}
			} else {
				// 在没有实现的情况下，把所有的返回
				containers.add(container);
			}
		}

		return containers;
	}

}
