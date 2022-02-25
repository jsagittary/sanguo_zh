package com.gryphpoem.game.zw.resource.dao.impl.p;


import com.gryphpoem.game.zw.resource.common.ServerHotfix;
import com.gryphpoem.game.zw.resource.dao.BaseDao;

/**
 * @author zhangdh
 * @ClassName: DataRepairDao
 * @Description: 线上玩家BUG处理查询类
 * @date 2017-07-03 15:51
 */
public class DataRepairDao extends BaseDao {

    public int insertHotfifxResult(ServerHotfix hotfix) {
        return getSqlSession().insert("insertHotfix", hotfix);
    }

}
