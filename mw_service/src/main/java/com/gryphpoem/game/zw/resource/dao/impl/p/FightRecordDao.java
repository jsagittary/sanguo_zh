package com.gryphpoem.game.zw.resource.dao.impl.p;

import com.gryphpoem.game.zw.resource.dao.BaseDao;
import com.gryphpoem.game.zw.resource.dao.sqlMap.p.FightRecordMapper;
import com.gryphpoem.game.zw.resource.domain.p.DbFightRecord;

import java.util.Date;

/**
 * @author zhou jie
 * @time 2022/9/22 18:04
 */
public class FightRecordDao extends BaseDao {

    public DbFightRecord selectFightRecord(long recordId) {
        return this.getMapper(FightRecordMapper.class).selectByReportId(recordId);
    }

    public int updateFightRecord(DbFightRecord dbFightRecord) {
        return this.getMapper(FightRecordMapper.class).update(dbFightRecord);
    }

    public void deleteFightRecord(DbFightRecord dbFightRecord) {
    }

    public void replaceFightRecord(DbFightRecord dbFightRecord) {
        this.getMapper(FightRecordMapper.class).replace(dbFightRecord);
    }

    public void deleteExpired(Date expiredTime) {
        this.getMapper(FightRecordMapper.class).deleteExpired(expiredTime);
    }

}
