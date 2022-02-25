package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.pb.CommonPb;

/**
 * @ClassName RebelBuff.java
 * @Description 匪军叛乱buff
 * @author QiuKun
 * @date 2018年10月26日
 */
public class RebelBuff {
    private int startTime;
    private int endTime;
    private int type;
    private int lv;

    public RebelBuff() {
    }

    public RebelBuff(int startTime, int endTime, int type, int lv) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
        this.lv = lv;
    }

    public void dser(CommonPb.RebelBuff ser) {
        this.startTime = ser.getStartTime();
        this.endTime = ser.getEndTime();
        this.type = ser.getType();
        this.lv = ser.getLv();
    }

    public CommonPb.RebelBuff ser() {
        CommonPb.RebelBuff.Builder builder = CommonPb.RebelBuff.newBuilder();
        builder.setEndTime(this.getEndTime());
        builder.setStartTime(this.startTime);
        builder.setType(this.type);
        builder.setLv(this.lv);
        return builder.build();
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    @Override
    public String toString() {
        return "RebelBuff [startTime=" + startTime + ", endTime=" + endTime + ", type=" + type + ", lv=" + lv + "]";
    }

}
