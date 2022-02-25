package com.gryphpoem.game.zw.resource.dao;

import java.util.HashMap;

import org.mybatis.spring.support.SqlSessionDaoSupport;

public class BaseDao extends SqlSessionDaoSupport {
	protected HashMap<String, Object> paramsMap() {
		HashMap<String, Object> paramsMap = new HashMap<>();
		return paramsMap;
	}

	protected <T> T getMapper(Class<T> clazz){
		return super.getSqlSession().getMapper(clazz);
	}

}
