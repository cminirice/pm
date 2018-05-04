package com.guttv.pm.utils;

import java.util.List;

public class Pager {

	public static final Integer seria = Integer.valueOf(500);
	private int pageNumber = 1;
	private int pageSize = 15;
	private String searchBy = null;
	private String keyword = null;
	private String orderBy = null;
	private String order = null;
	private int tatalCount = 0;
	private List<?> result = null;

	public int getPageCount() {
		int i = this.tatalCount / this.pageSize;
		if (this.tatalCount % this.pageSize > 0)
			++i;
		return i;
	}

	public int getPageNumber() {
		return this.pageNumber;
	}

	public void setPageNumber(int paramInt) {
		if (paramInt < 1)
			paramInt = 1;
		this.pageNumber = paramInt;
	}

	public int getPageSize() {
		return this.pageSize;
	}

	public void setPageSize(int paramInt) {
		if (paramInt < 1)
			paramInt = 1;
		else if (paramInt > seria.intValue())
			paramInt = seria.intValue();
		this.pageSize = paramInt;
	}

	public String getSearchBy() {
		return this.searchBy;
	}

	public void setSearchBy(String paramString) {
		this.searchBy = paramString;
	}

	public String getKeyword() {
		return this.keyword;
	}

	public void setKeyword(String paramString) {
		this.keyword = paramString;
	}

	public String getOrderBy() {
		return this.orderBy;
	}

	public void setOrderBy(String paramString) {
		this.orderBy = paramString;
	}

	public int getTotalCount() {
		return this.tatalCount;
	}

	public void setTotalCount(int paramInt) {
		this.tatalCount = paramInt;
	}

	public List<?> getResult() {
		return this.result;
	}

	public void setResult(List<?> paramList) {
		this.result = paramList;
	}

	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}
}
