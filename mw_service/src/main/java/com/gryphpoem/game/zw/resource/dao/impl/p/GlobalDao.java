package com.gryphpoem.game.zw.resource.dao.impl.p;

import com.gryphpoem.game.zw.resource.dao.BaseDao;
import com.gryphpoem.game.zw.resource.domain.p.DbGlobal;

/**
 * @ClassName GlobalDao.java
 * @Description 全局数据Dao
 * @author TanDonghai
 * @date 创建时间：2017年3月23日 下午2:53:13
 *
 */
public class GlobalDao extends BaseDao {
    public DbGlobal selectGlobal() {
        return this.getSqlSession().selectOne("GlobalDao.selectGlobal");
    }

    public int updateGlobal(DbGlobal dbGlobal) {
        return this.getSqlSession().update("GlobalDao.updateGlobal", dbGlobal);
    }

    public int replaceGlobal(DbGlobal dbGlobal) {
        return this.getSqlSession().insert("GlobalDao.replaceGlobal", dbGlobal);
    }

    public void save(DbGlobal dbGlobal) {
        if (updateGlobal(dbGlobal) == 0) {
            replaceGlobal(dbGlobal);
        }
    }

    public void insertGlobal(DbGlobal dbGlobal) {
        this.getSqlSession().insert("GlobalDao.insertGlobal", dbGlobal);
    }

    public int dropGlobalBack(){
        return this.getSqlSession().update("GlobalDao.dropGlobalBack");
    }
    
    public int copyGlobal() {
        return this.getSqlSession().update("GlobalDao.copyGlobal");
    }

    public int clearGlobal() {
        return this.getSqlSession().delete("GlobalDao.clearGlobal");
    }
}
