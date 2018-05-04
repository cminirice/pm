/**
 * 
 */
package com.guttv.pm.action.${(codeEntity.tableMeta.pkg)!};

import javax.annotation.Resource;

import org.apache.struts2.convention.annotation.ParentPackage;

import com.guttv.action.BaseAction;
import com.guttv.bean.OperateStatusEnum;
import com.guttv.frame.entity.${codeEntity.name};
import com.guttv.service.${codeEntity.name}Service;
import com.guttv.util.Constants;

/**
 * ${codeEntity.cn}信息
 * @author Peter
 *
 */
@ParentPackage("${codeEntity.tableMeta.pkg}")
public class ${codeEntity.name}Action extends BaseAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Resource(name="${codeEntity.fsName}Service")
	private ${codeEntity.name}Service ${codeEntity.fsName}Service = null;
	
	private ${codeEntity.name} ${codeEntity.fsName} = null;
	
	
	public String list() {
		try {
			this.pager = ${codeEntity.fsName}Service.findPager(pager);
		} catch (Exception e) {
			log.error("查询${codeEntity.cn}信息失败：" + e.getMessage(),e);
			this.addActionError("查询${codeEntity.cn}信息失败：" + e.getMessage());
			return ERROR;
		}
		return LIST;
	}
	
	public String edit() {
		try {
			${codeEntity.fsName} = this.${codeEntity.fsName}Service.load(this.id);
		} catch (Exception e) {
			log.error("查询${codeEntity.cn}信息失败：" + e.getMessage(),e);
			this.addActionError("查询${codeEntity.cn}信息失败：" + e.getMessage());
			return ERROR;
		}
		return INPUT;
	}
	
	public String add() {
		return INPUT;
	}
	
	public String view() {
		try {
			${codeEntity.fsName} = this.${codeEntity.fsName}Service.load(this.id);
		} catch (Exception e) {
			log.error("查询${codeEntity.cn}信息失败：" + e.getMessage(),e);
			this.addActionError("查询${codeEntity.cn}信息失败：" + e.getMessage());
			return ERROR;
		}
		return VIEW;
	}
	
	public void saveOrUpdate() {
		
		try {
			if(${codeEntity.fsName}.getId() == null || ${codeEntity.fsName}.getId().trim().length() == 0) {
				this.${codeEntity.fsName}Service.save(${codeEntity.fsName});
				this.addActionMessage("添加${codeEntity.cn}成功！");
			}else {
				this.${codeEntity.fsName}Service.update(${codeEntity.fsName});
				this.addActionMessage("编辑${codeEntity.cn}成功！");
			}
		} catch (Exception e) {
			log.error("操作失败：" + e.getMessage(),e);
			this.addActionError("操作失败：" + e.getMessage());
			return ERROR;
		}
		
		this.setRedirectUrl("/${(codeEntity.tableMeta.pkg)!}/list");
		
		return;
	}
	
	public String delete(){
		try {
			if(this.ids == null || this.ids.length == 0) {
				return this.ajaxMsg(OperateStatusEnum.error, "请至少选择一个${codeEntity.cn}信息！");
			}else {
				${codeEntity.fsName}Service.delete(ids);
				return this.ajaxMsg(OperateStatusEnum.success, "删除成功！");
			}
		} catch (Exception e) {
			log.error("删除异常：" + e.getMessage(),e);
			this.addActionError("删除异常:" + e.getMessage());
			return ERROR;
		}
	}
	

	public ${codeEntity.name} get${codeEntity.name}() {
		return ${codeEntity.fsName};
	}

	public void set${codeEntity.name}(${codeEntity.name} ${codeEntity.fsName}) {
		this.${codeEntity.fsName} = ${codeEntity.fsName};
	}

}
