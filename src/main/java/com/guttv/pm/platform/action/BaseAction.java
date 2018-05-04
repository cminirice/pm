/**
 * 
 */
package com.guttv.pm.platform.action;

import com.guttv.pm.utils.Pager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter
 *
 */
public class BaseAction {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private Integer id = null;

	private String code = null;

	protected Pager pager = new Pager();

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Pager getPager() {
		return pager;
	}

	public void setPager(Pager pager) {
		this.pager = pager;
	}

	protected List<Object> pager(Pager pager, List<?> beans) {
		List<Object> result = new ArrayList<Object>();
		if (beans == null || beans.size() == 0) {
			return result;
		}

		int end = pager.getPageNumber() * pager.getPageSize();
		int start = end - pager.getPageSize();
		result.addAll(beans.subList(start, end > beans.size() ? beans.size() : end));
		return result;
	}

}
