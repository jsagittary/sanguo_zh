package com.gryphpoem.game.zw.resource.dao.impl.p;

import java.util.List;
import java.util.Map;

import com.gryphpoem.game.zw.resource.dao.BaseDao;

/**
 * @ClassName MergeDao.java
 * @Description 合服使用
 * @author QiuKun
 * @date 2018年9月5日
 */
public class MergeDao extends BaseDao {

	/**
	 * 创建数据库
	 * 
	 * @param dbName
	 */
	public void createGameDb(String dbName) {
		Map<String, Object> map = paramsMap();
		map.put("dbName", dbName);
		this.getSqlSession().update("MergeDao.createDB", map);
	}

	/**
	 * 删除数据库
	 * 
	 * @param dbName
	 */
	public void dropDb(String dbName) {
		Map<String, Object> map = paramsMap();
		map.put("dbName", dbName);
		this.getSqlSession().update("MergeDao.dropDb", map);
	}

	public void execSql(String sql) {
		Map<String, Object> map = paramsMap();
		map.put("sqlStr", sql);
		this.getSqlSession().update("MergeDao.execSql", map);
	}

	public List<String> showTables() {
		return this.getSqlSession().selectList("MergeDao.showTables");
	}

	public Map<String, String> showCreateTable(String tableName) {
		Map<String, Object> map = paramsMap();
		map.put("tableName", tableName);
		return this.getSqlSession().selectOne("MergeDao.showCreateTable", map);
	}

	/**
	 * 测试连接
	 */
	public void testConnect() {
		this.getSqlSession().selectOne("MergeDao.testConnect");
	}
}
