package com.gryphpoem.game.zw.resource.pojo.buildHomeCity;

import com.gryphpoem.game.zw.pb.CommonPb;

/**
 * @Author: GeYuanpeng
 * @Date: 2022/11/7 13:43
 */
public class ExploreQue {

    private int keyId;

    private int scoutIndex;

    private int cellId;

    private int period;

    private int endTime;

    private int freeTime;

    public ExploreQue(int keyId, int scoutIndex, int cellId, int period, int endTime) {
        this.keyId = keyId;
        this.scoutIndex = scoutIndex;
        this.cellId = cellId;
        this.period = period;
        this.endTime = endTime;
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getScoutIndex() {
        return scoutIndex;
    }

    public void setScoutIndex(int scoutIndex) {
        this.scoutIndex = scoutIndex;
    }

    public int getCellId() {
        return cellId;
    }

    public void setCellId(int cellId) {
        this.cellId = cellId;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public int getFreeTime() {
        return freeTime;
    }

    public void setFreeTime(int freeTime) {
        this.freeTime = freeTime;
    }

    public CommonPb.ExploreQue creatExplorePb() {
        CommonPb.ExploreQue.Builder builder = CommonPb.ExploreQue.newBuilder();
        builder.setKeyId(this.getKeyId());
        builder.setScoutIndex(this.getScoutIndex());
        builder.setCellId(this.getCellId());
        builder.setPeriod(this.getPeriod());
        builder.setEndTime(this.getEndTime());
        builder.setFreeTime(this.getFreeTime());
        return builder.build();
    }

    @Override
    public String toString() {
        return "ExploreFogQue{" +
                "keyId=" + keyId +
                ", scoutIndex=" + scoutIndex +
                ", cellId=" + cellId +
                ", period=" + period +
                ", endTime=" + endTime +
                ", freeTime=" + freeTime +
                '}';
    }
}
