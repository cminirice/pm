/**
 * 
 */
package com.guttv.pm.code;

import com.guttv.pm.code.ann.TableMeta;
import com.guttv.pm.utils.JsonUtil;

/**
 * @author Peter
 *
 */
public class CodeTable{
	private String cn = null;
	private String pkg = null;
	public CodeTable(TableMeta t) {
		this.setCn(t.cn());
		this.setPkg(t.pkg());
	}
	public String getCn() {
		return cn;
	}
	public void setCn(String cn) {
		this.cn = cn;
	}
	public String getPkg() {
		return pkg;
	}
	public void setPkg(String pkg) {
		this.pkg = pkg;
	}
	
	public String toString() {
		return this.getClass().getName() + JsonUtil.toJson(this);
	}
}