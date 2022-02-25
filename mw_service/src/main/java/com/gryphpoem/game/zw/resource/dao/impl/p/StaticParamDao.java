package com.gryphpoem.game.zw.resource.dao.impl.p;

import java.util.List;

import com.gryphpoem.game.zw.resource.dao.BaseDao;
import com.gryphpoem.game.zw.resource.domain.p.StaticParam;

public class StaticParamDao extends BaseDao {
    public List<StaticParam> selectStaticParams() {
        return this.getSqlSession().selectList("StaticParamDao.selectStaticParams");
    }

    public void insertStaticParam(StaticParam data) {
        this.getSqlSession().insert("StaticParamDao.insertStaticParam", data);
    }
 
}
