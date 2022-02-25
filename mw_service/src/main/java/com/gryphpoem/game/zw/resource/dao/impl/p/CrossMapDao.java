package com.gryphpoem.game.zw.resource.dao.impl.p;

import java.util.List;
import java.util.Map;

import com.gryphpoem.game.zw.resource.dao.BaseDao;
import com.gryphpoem.game.zw.resource.domain.p.DbCrossMap;

/**
 * @ClassName CrossMapDao.java
 * @Description
 * @author QiuKun
 * @date 2019年4月2日
 */
public class CrossMapDao extends BaseDao {

    public List<DbCrossMap> selectAll() {
        return this.getSqlSession().selectList("CrossMapDao.selectAll");
    }

    public Map<Integer, DbCrossMap> selectAllMap() {
        return this.getSqlSession().selectMap("CrossMapDao.selectAll", "mapId");
    }

    public int update(DbCrossMap data) {
        return this.getSqlSession().update("CrossMapDao.update", data);
    }

    public int replace(DbCrossMap data) {
        return this.getSqlSession().insert("CrossMapDao.replace", data);
    }

    public void save(DbCrossMap data) {
        if (update(data) == 0) {
            replace(data);
        }
    }

    public void insert(DbCrossMap data) {
        this.getSqlSession().insert("CrossMapDao.insert", data);
    }

}
