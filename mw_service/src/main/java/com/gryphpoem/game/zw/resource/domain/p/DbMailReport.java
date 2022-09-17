package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.gameplay.local.util.DelayInvokeEnvironment;
import com.gryphpoem.game.zw.gameplay.local.util.DelayRun;
import com.gryphpoem.game.zw.manager.MailReportDataManager;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-09-15 15:07
 */
public class DbMailReport implements DbSerializeId, DelayRun {
    private long lordId;
    private int keyId;
    private int expireTime;
    private byte[] report;

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public byte[] getReport() {
        return report;
    }

    public void setReport(byte[] report) {
        this.report = report;
    }

    public int getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(int expireTime) {
        this.expireTime = expireTime;
    }

    public DbMailReport(long lordId, int keyId, byte[] report) {
        this.lordId = lordId;
        this.keyId = keyId;
        this.report = report;
    }

    @Override
    public long getSerializeIdId() {
        return lordId;
    }

    @Override
    public int deadlineTime() {
        return this.expireTime;
    }

    @Override
    public void deadRun(int runTime, DelayInvokeEnvironment env) {
        MailReportDataManager mailReportDataManager = (MailReportDataManager) env;
        mailReportDataManager.removeOneReport(this.lordId, this.keyId);
    }
}
