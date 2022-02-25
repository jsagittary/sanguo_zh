package com.gryphpoem.game.zw.resource.dao.impl.p;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.gryphpoem.game.zw.resource.dao.BaseDao;
import com.gryphpoem.game.zw.resource.domain.p.DataNew;
import com.gryphpoem.game.zw.resource.util.DbDataCheckSum;

public class DataNewDao extends BaseDao {
    private DbDataCheckSum checkSum = new DbDataCheckSum(DataNew.class, "lordId");

//    public DataNew selectData(Long lordId) {
//        return this.getSqlSession().selectOne("DataNewDao.selectData", lordId);
//    }

    public void insertData(DataNew data) {
        this.getSqlSession().insert("DataNewDao.insertData", data);
    }

//    public int insertFullData(DataNew data) {
//        return this.getSqlSession().insert("DataNewDao.insertFullData", data);
//    }

    public int updateData(DataNew data) {
        return this.getSqlSession().update("DataNewDao.updateData", data);
    }

    public int replaceData(DataNew data) {
        return this.getSqlSession().insert("DataNewDao.replaceData", data);
    }

    public void updateOptimize(DataNew data) {
        Map<String, Object> saveParam = checkSum.saveParam(data);
        if (saveParam != null && saveParam.size() > 1) {
            this.getSqlSession().update("DataNewDao.updateOptimize", saveParam);
        }
    }

    public void save(DataNew data) {
        if (updateData(data) == 0) {
            replaceData(data);
        }
    }

    public List<DataNew> loadData() {
        List<DataNew> list = new ArrayList<DataNew>();
        long curIndex = 0;
        int count = 2000;
        int pageSize = 0;
        while (true) {
            List<DataNew> page = loadData(curIndex, count);
            pageSize = page.size();
            if (pageSize > 0) {
                list.addAll(page);
                curIndex = page.get(pageSize - 1).getLordId();
            } else {
                break;
            }

            if (page.size() < count) {
                break;
            }
        }
        return list;
    }

    private List<DataNew> loadData(long curIndex, int count) {
        Map<String, Object> params = paramsMap();
        params.put("curIndex", curIndex);
        params.put("count", count);
        return this.getSqlSession().selectList("DataNewDao.loadData", params);
    }
}
