package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.pb.CommonPb;

/**
 * @ClassName CrossBuff.java
 * @Description 跨服buff
 * @author QiuKun
 * @date 2019年5月15日
 */
public class CrossBuff {
    private int startTime;
    private int endTime;
    private int type;
    private int lv;

    public CrossBuff() {
    }

    public CrossBuff(int startTime, int endTime, int type, int lv) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
        this.lv = lv;
    }

    public static CrossBuff createCrossBuff(CommonPb.CrossBuff ser) {
        CrossBuff buff = new CrossBuff();
        buff.startTime = ser.getStartTime();
        buff.endTime = ser.getEndTime();
        buff.type = ser.getType();
        buff.lv = ser.getLv();
        return buff;

    }

    public CommonPb.CrossBuff toPb() {
        CommonPb.CrossBuff.Builder builder = CommonPb.CrossBuff.newBuilder();
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
        return "CrossBuff [startTime=" + startTime + ", endTime=" + endTime + ", type=" + type + ", lv=" + lv + "]";
    }

}
