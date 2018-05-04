/**
 * 
 */
package com.guttv.pm.code;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.guttv.pm.utils.JsonUtil;

/**
 * @author Peter
 *
 */
public class CodeEntity {

	//类型
	private String clz = null;
	
	//中文名
	private String cn = null;
	
	//类名
	private String name = null;
	
	//第一个字母是小写的类名  firstShortName
	private String fsName = null;
	
	//用下划线分隔开的名称  SystemProperties --> system_properties
	private String underscores = null;
	
	//
	private CodeTable tableMeta = null;
	
	//在列表中显示的字段
	private List<String> listFields = new ArrayList<String>();
	
	//在编缉页面显示的字段
	private List<String> editFields = new ArrayList<String>();
	
	//在列表上面的查询里面需要查询的字段
	private List<String> searchFields = new ArrayList<String>();
	
	//所有的字段
	private Map<String,CodeField> fields = new LinkedHashMap<String,CodeField>();
	
	public String getClz() {
		return clz;
	}

	public void setClz(String clz) {
		this.clz = clz;
	}

	public String getCn() {
		return cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFsName() {
		return fsName;
	}

	public void setFsName(String fsName) {
		this.fsName = fsName;
	}
	
	public String getUnderscores() {
		return underscores;
	}

	public void setUnderscores(String underscores) {
		this.underscores = underscores;
	}

	private void addListField(String f) {
		listFields.add(f);
	}

	public List<String> getListFields() {
		return listFields;
	}
	
	private void addEditField(String f) {
		editFields.add(f);
	}

	public List<String> getEditFields() {
		return editFields;
	}

	public CodeTable getTableMeta() {
		return tableMeta;
	}

	public void setTableMeta(CodeTable tableMeta) {
		this.tableMeta = tableMeta;
	}
	
	private void addSearchField(String f) {
		searchFields.add(f);
	}

	public List<String> getSearchFields() {
		return searchFields;
	}
	
	public void addField(CodeField f) {
		fields.put(f.getName(), f);
		if(f.isList()) {
			this.addListField(f.getName());
		}
		if(f.isEdit()) {
			this.addEditField(f.getName());
		}
		if(f.isSearch()) {
			this.addSearchField(f.getName());
		}
	}

	public Map<String, CodeField> getFields() {
		return fields;
	}
	
	public String toString() {
		return this.getClass().getName() + JsonUtil.toJson(this);
	}
}
