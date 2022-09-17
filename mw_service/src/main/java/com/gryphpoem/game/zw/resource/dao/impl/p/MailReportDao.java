package com.gryphpoem.game.zw.resource.dao.impl.p;

import com.gryphpoem.game.zw.resource.dao.BaseDao;
import com.gryphpoem.game.zw.resource.dao.sqlMap.p.MailReportMapper;
import com.gryphpoem.game.zw.resource.domain.p.DbMailReport;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-09-15 14:16
 */
public class MailReportDao extends BaseDao {
    public DbMailReport selectMailReport(long lordId, int mailKeyId) {
        return this.getMapper(MailReportMapper.class).selectByLordId(lordId, mailKeyId);
    }

    public int updateMailReport(DbMailReport dbMailReport) {
        return this.getMapper(MailReportMapper.class).update(dbMailReport);
    }

    public void replaceMailReport(DbMailReport dbMailReport) {
        if (this.getMapper(MailReportMapper.class).insert(dbMailReport) > 0) {
            dbMailReport.setExpireTime(TimeHelper.getCurrentSecond() + 1 * TimeHelper.MINUTE);
        }
    }


    public void save(DbMailReport dbMailReport) {
        replaceMailReport(dbMailReport);
    }

}
