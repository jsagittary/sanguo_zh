package com.gryphpoem.game.zw.resource.dao.impl.p;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gryphpoem.game.zw.resource.dao.BaseDao;
import com.gryphpoem.game.zw.resource.domain.p.Building;

public class BuildingDao extends BaseDao {
    public Building selectBuilding(long lordId) {
        return this.getSqlSession().selectOne("BuildingDao.selectBuilding", lordId);
    }

    public int insertBuilding(Building building) {
        return this.getSqlSession().insert("BuildingDao.insertBuilding", building);
    }

    public int updateBuilding(Building building) {
        return this.getSqlSession().update("BuildingDao.updateBuilding", building);
    }

    public int replaceBuilding(Building building) {
        return this.getSqlSession().insert("BuildingDao.replaceBuilding", building);
    }

    public void save(Building building) {
        if (updateBuilding(building) == 0) {
            replaceBuilding(building);
        }
    }

    public List<Building> load() {
        List<Building> list = new ArrayList<>();
        long curIndex = 0L;
        int count = 1000;
        int pageSize = 0;
        while (true) {
            List<Building> page = load(curIndex, count);
            pageSize = page.size();
            if (pageSize > 0) {
                list.addAll(page);
                curIndex = page.get(pageSize - 1).getLordId();
            } else {
                break;
            }

            if (pageSize < count) {
                break;
            }
        }
        return list;
    }

    private List<Building> load(long curIndex, int count) {
        HashMap<String, Object> params = paramsMap();
        params.put("curIndex", curIndex);
        params.put("count", count);
        return this.getSqlSession().selectList("BuildingDao.load", params);
    }
}
