package com.guttv.service.impl;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.guttv.dao.${codeEntity.name}Dao;
import com.guttv.frame.entity.${codeEntity.name};
import com.guttv.frame.service.impl.BaseServiceImpl;
import com.guttv.service.${codeEntity.name}Service;

/**
 * @author Peter 
 *
 */
@Service("${codeEntity.fsName}Service")
public class ${codeEntity.name}ServiceImpl extends BaseServiceImpl<${codeEntity.name}, String> implements ${codeEntity.name}Service {

	@Resource(name="${codeEntity.fsName}Dao")
	private ${codeEntity.name}Dao ${codeEntity.fsName}Dao = null;
	@Resource(name="${codeEntity.fsName}Dao")
	public void set${codeEntity.name}Dao(${codeEntity.name}Dao ${codeEntity.fsName}Dao) {
		this.setBaseDao(${codeEntity.fsName}Dao);
	}

}
