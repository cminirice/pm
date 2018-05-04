/**
 * 
 */
package com.guttv.pm.code;

import java.lang.reflect.Field;

import com.guttv.pm.code.ann.FieldMeta;
import com.guttv.pm.utils.JsonUtil;

/**
 * @author Peter
 *
 */
public class CodeField {

	private String cn = null;
	
	private String name = null;
	
	private String type = null;
	
	private int length = 32;
	
	private boolean required = false;
	
	private boolean list = false;
	
	private boolean edit = true;
	
	private boolean search = false;
	
	private boolean sort = false;
	
	public CodeField(FieldMeta f,Field field) {
		this.setCn(f.cn());
		this.setName(field.getName());
		this.setType(field.getType().getSimpleName());
		this.setLength(f.length());
		this.setRequired(f.required());
		this.setList(f.list());
		this.setEdit(f.edit());
		this.setSearch(f.search());
		this.setSort(f.sort());
	}

	public String getCn() {
		return cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public boolean isList() {
		return list;
	}

	public void setList(boolean list) {
		this.list = list;
	}

	public boolean isEdit() {
		return edit;
	}

	public void setEdit(boolean edit) {
		this.edit = edit;
	}

	public boolean isSearch() {
		return search;
	}

	public void setSearch(boolean search) {
		this.search = search;
	}

	public boolean isSort() {
		return sort;
	}

	public void setSort(boolean sort) {
		this.sort = sort;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String toString() {
		return this.getClass().getName() + JsonUtil.toJson(this);
	}
}
