package com.gryphpoem.game.zw.resource.dao.impl.p;

import java.util.List;

import com.gryphpoem.game.zw.resource.dao.BaseDao;
import com.gryphpoem.game.zw.resource.domain.p.GlobalActivity;

public class ActivityDao extends BaseDao {

    public List<GlobalActivity> selectGlobalActivity() {
        return this.getSqlSession().selectList("ActivityDao.selectGlobalActivity");
    }

    public void updateActivity(GlobalActivity globalActivity) {
        if (update(globalActivity) == 0) {
            insertGlobalActivity(globalActivity);
        }
    }

    public void insertGlobalActivity(GlobalActivity globalActivity) {
        this.getSqlSession().insert("ActivityDao.insertGlobalActivity", globalActivity);
    }

    public int update(GlobalActivity globalActivity) {
        return this.getSqlSession().update("ActivityDao.updateGlobalActivity", globalActivity);
    }

}
