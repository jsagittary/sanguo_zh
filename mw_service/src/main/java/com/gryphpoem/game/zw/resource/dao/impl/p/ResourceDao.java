package com.gryphpoem.game.zw.resource.dao.impl.p;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.gryphpoem.game.zw.resource.dao.BaseDao;
import com.gryphpoem.game.zw.resource.domain.p.Resource;

public class ResourceDao extends BaseDao {
    public Resource selectResource(long lordId) {
        return this.getSqlSession().selectOne("ResourceDao.selectResource", lordId);
    }

    public int updateResource(Resource resource) {
        return this.getSqlSession().update("ResourceDao.updateResource", resource);
    }

    public int replaceResource(Resource resource) {
        return this.getSqlSession().insert("ResourceDao.replaceResource", resource);
    }

    public void save(Resource resource) {
        if (updateResource(resource) == 0) {
            replaceResource(resource);
        }
    }

    // public void updateOut(Resource resource) {
    // this.getSqlSession().update("ResourceDao.updateOut", resource);
    // }
    //
    // public void updateMax(Resource resource) {
    // this.getSqlSession().update("ResourceDao.updateMax", resource);
    // }
    //
    // public void updateMaxAndOut(Resource resource) {
    // this.getSqlSession().update("ResourceDao.updateMaxAndOut", resource);
    // }
    //
    // public void updateTime(Resource resource) {
    // this.getSqlSession().update("ResourceDao.updateTime", resource);
    // }

    public void insertResource(Resource resource) {
        this.getSqlSession().insert("ResourceDao.insertResource", resource);
    }


    public List<Resource> load() {
        List<Resource> list = new ArrayList<>();
        long curIndex = 0L;
        int count = 1000;
        int pageSize = 0;
        while (true) {
            List<Resource> page = load(curIndex, count);
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

    private List<Resource> load(long curIndex, int count) {
        Map<String, Object> params = paramsMap();
        params.put("curIndex", curIndex);
        params.put("count", count);
        return this.getSqlSession().selectList("ResourceDao.load", params);
    }
}
