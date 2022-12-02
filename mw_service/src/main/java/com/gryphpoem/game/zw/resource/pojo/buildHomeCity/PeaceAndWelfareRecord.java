package com.gryphpoem.game.zw.resource.pojo.buildHomeCity;

import com.gryphpoem.game.zw.pb.SerializePb;

/**
 * 安民济物记录
 *
 * @Author: GeYuanpeng
 * @Date: 2022/12/2 11:54
 */
public class PeaceAndWelfareRecord {

    private int type; // 类型: 1-举办戏剧; 2-千秋庆典

    private int lastTime; // 最近一次玩的时间(秒)

    private int curDayCnt; // 当天已经玩的次数

    private int totalCnt; // 累计玩的次数

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLastTime() {
        return lastTime;
    }

    public void setLastTime(int lastTime) {
        this.lastTime = lastTime;
    }

    public int getCurDayCnt() {
        return curDayCnt;
    }

    public void setCurDayCnt(int curDayCnt) {
        this.curDayCnt = curDayCnt;
    }

    public int getTotalCnt() {
        return totalCnt;
    }

    public void setTotalCnt(int totalCnt) {
        this.totalCnt = totalCnt;
    }

    public SerializePb.SerPeaceAndWelfareRecord ser() {
        SerializePb.SerPeaceAndWelfareRecord.Builder builder = SerializePb.SerPeaceAndWelfareRecord.newBuilder();
        builder.setType(this.type);
        builder.setLastTime(this.lastTime);
        builder.setCurDayCnt(this.curDayCnt);
        builder.setTotalCnt(this.totalCnt);
        return builder.build();
    }

    public PeaceAndWelfareRecord dser(SerializePb.SerPeaceAndWelfareRecord pb) {
        this.type = pb.getType();
        this.lastTime = pb.getLastTime();
        this.curDayCnt = pb.getCurDayCnt();
        this.totalCnt = pb.getTotalCnt();
        return this;
    }

}
