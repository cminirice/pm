package com.guttv.dao.impl;

import org.springframework.stereotype.Repository;

import com.guttv.dao.${codeEntity.name}Dao;
import com.guttv.frame.dao.impl.BaseDaoImpl;
import com.guttv.frame.entity.${codeEntity.name};

/**
 * @author Peter 
 *
 */
@Repository("${codeEntity.fsName}Dao")
public class ${codeEntity.name}DaoImpl extends BaseDaoImpl<${codeEntity.name}, String> implements ${codeEntity.name}Dao {


}