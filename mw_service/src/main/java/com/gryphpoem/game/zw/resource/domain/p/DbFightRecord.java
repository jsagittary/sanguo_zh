package com.gryphpoem.game.zw.resource.domain.p;

import java.util.Date;

/**
 * @author zhou jie
 * @time 2022/9/22 18:01
 */
public class DbFightRecord implements DbSerializeId {

    private long keyId;
    private volatile Date createTime;
    private byte[] record;

    public long getKeyId() {
        return keyId;
    }

    public void setKeyId(long keyId) {
        this.keyId = keyId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public byte[] getRecord() {
        return record;
    }

    public void setRecord(byte[] record) {
        this.record = record;
    }

    public DbFightRecord() {
    }

    public DbFightRecord(long keyId, Date createTime, byte[] record) {
        this.keyId = keyId;
        this.createTime = createTime;
        this.record = record;
    }

    @Override
    public long getSerializeIdId() {
        return keyId;
    }
}
