package com.gryphpoem.game.zw.gameplay.local.world.warfire;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.Date;

/**
 * @program: civilization_zh
 * @description: 战火燎原buff
 * @author: zhou jie
 * @create: 2020-12-24 15:34
 */
public class WarFireBuff {

    private int startTime;
    private int endTime;
    private int type;
    private int lv;

    public WarFireBuff() {
    }

    public WarFireBuff(int type) {
        this.type = type;
        this.lv = 1;
    }

    public WarFireBuff(int startTime, int endTime, int type, int lv) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
        this.lv = lv;
    }

    public WarFireBuff(Date startDate, Date endDate, int type, int lv) {
        this.startTime = TimeHelper.dateToSecond(startDate);
        this.endTime = TimeHelper.dateToSecond(endDate);
        this.type = type;
        this.lv = lv;
    }

    public WarFireBuff(CommonPb.WarFireBuffPb buff) {
        this();
        this.type = buff.getType();
        this.lv = buff.getLv();
        this.startTime = buff.getStartTime();
        this.endTime = buff.getEndTime();
    }

    public static CommonPb.WarFireBuffPb toBuffInfo(WarFireBuff wfb) {
        CommonPb.WarFireBuffPb.Builder builder = CommonPb.WarFireBuffPb.newBuilder();
        builder.setType(wfb.getType());
        builder.setLv(wfb.getLv());
        builder.setStartTime(wfb.getStartTime());
        builder.setEndTime(wfb.getEndTime());
        return builder.build();
    }

    /**
     * 是否在生效时间内
     * @return true 生效 , false 不生效
     */
    public boolean isInTime() {
        int now = TimeHelper.getCurrentSecond();
        return now >= getStartTime() && now < getEndTime();
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
        return "WarFireBuff{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                ", type=" + type +
                ", lv=" + lv +
                '}';
    }
}