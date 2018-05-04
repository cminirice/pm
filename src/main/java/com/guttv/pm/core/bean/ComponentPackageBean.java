/**
 * 
 */
package com.guttv.pm.core.bean;

import java.util.ArrayList;
import java.util.List;

import com.guttv.pm.code.ann.FieldMeta;
import com.guttv.pm.code.ann.TableMeta;

/**
 * 
 * 组件包信息，与一次注册对应
 * @author Peter
 *
 */
@TableMeta(cn="组件包",pkg="comPack")
public class ComponentPackageBean extends BaseBean{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3325715070388190619L;

	/**
	 * 唯一标记一组 组件 的标识
	 */
	@FieldMeta(cn="组件标识",length=128,list=true,required=true,edit=false,search=true,sort=false)
	private String comID = null;
	
	/**
	 * ZIP包的MD5值
	 */
	@FieldMeta(cn="包MD5值",length=128,list=true,required=true,edit=false,search=false,sort=false)
	private String md5 = null;
	
	/**
	 * 组件包路径
	 */
	@FieldMeta(cn="组件包路径",length=128,list=true,required=true,edit=false,search=false,sort=false)
	private String comPackageFilePath = null;
	
	private String srcFileName = null;
	
	/**
	 * 记录该一组所有的组件
	 */
	private List<ComponentBean> components = null;

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public String getComPackageFilePath() {
		return comPackageFilePath;
	}

	public void setComPackageFilePath(String comPackageFilePath) {
		this.comPackageFilePath = comPackageFilePath;
	}

	public String getComID() {
		return comID;
	}

	public void setComID(String comID) {
		this.comID = comID;
	}

	public String getSrcFileName() {
		return srcFileName;
	}

	public void setSrcFileName(String srcFileName) {
		this.srcFileName = srcFileName;
	}

	public List<ComponentBean> getComponents() {
		return components;
	}

	public void setComponents(List<ComponentBean> components) {
		this.components = components;
	}
	
	public ComponentPackageBean clone() {
		ComponentPackageBean cpb = new ComponentPackageBean();
		cpb.setComID(comID);
		cpb.setComPackageFilePath(comPackageFilePath);
		cpb.setMd5(md5);
		if(this.getComponents() != null) {
			List<ComponentBean> coms = new ArrayList<ComponentBean>();
			cpb.setComponents(coms);
			for(ComponentBean cb : getComponents()) {
				coms.add(cb.clone());
			}
		}
		cpb.setSrcFileName(this.getSrcFileName());
		cpb.setId(this.getId());
		cpb.setUpdateTime(this.getUpdateTime());
		cpb.setCreateTime(this.getCreateTime());
		return cpb;
	}
}
