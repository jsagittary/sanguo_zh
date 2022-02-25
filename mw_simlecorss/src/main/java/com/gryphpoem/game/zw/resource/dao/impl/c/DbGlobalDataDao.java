package com.gryphpoem.game.zw.resource.dao.impl.c;

import java.util.Map;

import com.gryphpoem.game.zw.resource.dao.BaseDao;
import com.gryphpoem.game.zw.resource.domain.p.DbGlobalData;

/**
 * @ClassName DbGlobalDataDao.java
 * @Description 公共数据dao
 * @author QiuKun
 * @date 2019年6月14日
 */
public class DbGlobalDataDao extends BaseDao {

    public Map<Integer, DbGlobalData> selectAll() {
        return this.getSqlSession().selectMap("DbGlobalDataDao.selectAll", "type");
    }

    public DbGlobalData selectByType(int type) {
        return this.getSqlSession().selectOne("DbGlobalDataDao.selectByType", type);
    }

    public int insert(DbGlobalData data) {
        return this.getSqlSession().insert("DbGlobalDataDao.insert", data);
    }

    public int update(DbGlobalData data) {
        return this.getSqlSession().update("DbGlobalDataDao.update", data);
    }

    public int replace(DbGlobalData data) {
        return this.getSqlSession().update("DbGlobalDataDao.replace", data);
    }
}
