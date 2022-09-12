package com.gryphpoem.game.zw.resource.dao.impl.p;

import com.gryphpoem.game.zw.resource.dao.BaseDao;
import com.gryphpoem.game.zw.resource.domain.p.Common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-08-12 17:35
 */
public class HeroDao extends BaseDao {
    public Common selectCommon(long lordId) {
        return this.getSqlSession().selectOne("CommonDao.selectCommon", lordId);
    }

    public int insertCommon(Common common) {
        return this.getSqlSession().insert("CommonDao.insertCommon", common);
    }

    public int updateCommon(Common common) {
        return this.getSqlSession().update("CommonDao.updateCommon", common);
    }

    public int replaceCommon(Common common) {
        return this.getSqlSession().insert("CommonDao.replaceCommon", common);
    }

    public void save(Common common) {
        if (updateCommon(common) == 0) {
            replaceCommon(common);
        }
    }

    public List<Common> load() {
        List<Common> list = new ArrayList<>();
        long curIndex = 0L;
        int count = 1000;
        int pageSize = 0;
        while (true) {
            List<Common> page = load(curIndex, count);
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

    private List<Common> load(long curIndex, int count) {
        Map<String, Object> params = paramsMap();
        params.put("curIndex", curIndex);
        params.put("count", count);
        return this.getSqlSession().selectList("CommonDao.load", params);
    }
}
