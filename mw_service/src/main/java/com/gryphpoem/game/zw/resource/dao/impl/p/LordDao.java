package com.gryphpoem.game.zw.resource.dao.impl.p;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.dao.BaseDao;
import com.gryphpoem.game.zw.resource.domain.p.Lord;

public class LordDao extends BaseDao {
    public Lord selectLordById(Long lordId) {
        return this.getSqlSession().selectOne("LordDao.selectLordById", lordId);
    }

    public List<Long> selectLordNotSmallIds() {
        return getSqlSession().selectList("LordDao.selectLordNotSmallIds");
    }

    // public Map<Long, Lord> getLordListInId(List<Long> lordIds) {
    // return this.getSqlSession().selectMap("LordDao.getLordListInId", lordIds, "lordId");
    // }

    // public Lord selectLordByNick(String nick) {
    // return this.getSqlSession().selectOne("LordDao.selectLordByNick", nick);
    // }

    // public int sameNameCount(String nick) {
    // return this.getSqlSession().selectOne("LordDao.sameNameCount", nick);
    // }

    public void updateNickPortrait(Lord lord) {
        this.getSqlSession().update("LordDao.updateNickPortrait", lord);
    }

    public int updateLord(Lord lord) {
        return this.getSqlSession().update("LordDao.updateLord", lord);
    }

    public int replaceLord(Lord lord) {
        return this.getSqlSession().insert("LordDao.replaceLord", lord);
    }

    public void save(Lord lord) {
        if (this.updateLord(lord) == 0) {
            replaceLord(lord);
        }
    }

    public void insertLord(Lord lord) {
        this.getSqlSession().insert("LordDao.insertLord", lord);
    }

    public int insertFullLord(Lord lord) {
        return this.getSqlSession().insert("LordDao.insertFullLord", lord);
    }

    public List<Lord> load() {
        List<Lord> list = new ArrayList<Lord>();
        long curIndex = 0L;
        int count = 1000;
        int pageSize = 0;
        while (true) {
            List<Lord> page = load(curIndex, count);
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
        LogUtil.start("共加载Lord数据 " + list.size() + " 条");
        return list;
    }

    private List<Lord> load(long curIndex, int count) {
        Map<String, Object> params = paramsMap();
        params.put("curIndex", curIndex);
        params.put("count", count);
        return this.getSqlSession().selectList("LordDao.load", params);
    }

    public Integer selectLordCount() {
        return this.getSqlSession().selectOne("LordDao.selectLordCount");
    }

    /**
     * 合服的时候加载玩家数据
     * 
     * @param camp
     * @return
     */
    public List<Lord> mergeLoad(int camp) {
        Map<String, Object> params = paramsMap();
        params.put("camp", camp);
        return this.getSqlSession().selectList("LordDao.mergeLoad", params);
    }

    public List<Lord> mergeLoadByCamps(List<Integer> camps) {
        Map<String, Object> params = paramsMap();
        params.put("camps", camps);
        return this.getSqlSession().selectList("LordDao.mergeLoadByCamps", params);
    }
}
