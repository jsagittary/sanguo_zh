package com.gryphpoem.game.zw.resource.dao.impl.p;

import com.gryphpoem.game.zw.resource.dao.BaseDao;
import com.gryphpoem.game.zw.resource.dao.sqlMap.p.MailReportMapper;
import com.gryphpoem.game.zw.resource.domain.p.DbMailReport;

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

    public int replaceMailReport(DbMailReport dbMailReport) {
        return this.getMapper(MailReportMapper.class).insert(dbMailReport);
    }


    public void save(DbMailReport dbMailReport) {
        if (updateMailReport(dbMailReport) == 0) {
            replaceMailReport(dbMailReport);
        }
    }
}
