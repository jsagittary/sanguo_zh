package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.pb.CommonPb;

/**
 * @ClassName DbMasterApprentice.java
 * @Description 师徒关系
 * @author QiuKun
 * @date 2017年7月1日
 */
public class DbMasterApprentice {
    /**
     * 师傅
     */
    public static final int RELATION_MASTER = 0;
    /**
     * 徒弟
     */
    public static final int RELATION_APPRENTICE = 1;


    /**
     * 已经同意
     */
    public static final int STATE_AGREE = 0;
    /**
     * 等待同意
     */
    public static final int STATE_WAIT_APPROVAL = 1;

    private long lordId; // 角色id
    private int createTime; // 师徒关系成立时间
    private int relation; // 关系 0 师傅 , 1 徒弟
    private int staus; // 状态 0 待同意 , 1 已同意

    public DbMasterApprentice() {
    }

    public DbMasterApprentice(long lordId, int createTime, int relation) {
        this.lordId = lordId;
        this.createTime = createTime;
        this.relation = relation;
        this.staus = STATE_WAIT_APPROVAL;
    }

    public DbMasterApprentice(long lordId, int createTime, int relation, int staus) {
        this.lordId = lordId;
        this.createTime = createTime;
        this.relation = relation;
        this.staus = staus;
    }

    public DbMasterApprentice(CommonPb.DbMasterApprentice ser) {
        this.lordId = ser.getLordId();
        this.createTime = ser.getCreateTime();
        this.relation = ser.getRelation();
        this.staus = ser.getStaus();
    }

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public int getCreateTime() {
        return createTime;
    }

    public void setCreateTime(int createTime) {
        this.createTime = createTime;
    }

    public int getRelation() {
        return relation;
    }

    public void setRelation(int relation) {
        this.relation = relation;
    }

    public int getStaus() {
        return staus;
    }

    public void setStaus(int staus) {
        this.staus = staus;
    }

    @Override
    public String toString() {
        return "DbMasterApprentice{" +
                "lordId=" + lordId +
                ", createTime=" + createTime +
                ", relation=" + relation +
                ", staus=" + staus +
                '}';
    }

}
