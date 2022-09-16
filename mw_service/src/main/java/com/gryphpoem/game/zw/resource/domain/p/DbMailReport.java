package com.gryphpoem.game.zw.resource.domain.p;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-09-15 15:07
 */
public class DbMailReport implements DbSerializeId {
    private long lordId;
    private int keyId;
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

    public DbMailReport(long lordId, int keyId, byte[] report) {
        this.lordId = lordId;
        this.keyId = keyId;
        this.report = report;
    }

    @Override
    public long getSerializeIdId() {
        return lordId;
    }
}
