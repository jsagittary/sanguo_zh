package com.gryphpoem.game.zw.resource.pojo.world;

import com.gryphpoem.game.zw.pb.CommonPb;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: ZhouJie
 * @date: Create in 2018-11-09 11:12
 * @description: 循环业务功能数据, For Example(匪军叛乱, 德意志反攻)
 * @modified By:
 */
public class CycleData {

    protected int openCnt;              // 开启第几次
    protected int curPreViewTime;       // 本次预显示的时间
    protected int curRoundStartTime;    // 本次开启时间
    protected int curRoundEndTime;      // 本次结束时间
    protected int curEndTime;           // 全部结束时间
    protected int curRound;             // 第波数
    protected int nextRoundTime;        // 下一波的时间

    /**
     * 业务数据
     */
    protected Map<Long, Integer> statusMap = new HashMap<>();
    protected Map<Long, Integer> cntMap = new HashMap<>();

    /**
     * 重置数据
     * statusMap与cntMap数据, 自己在子类里根据需求清除
     */
    public void reset() {
        this.curPreViewTime = 0;
        this.curRoundStartTime = 0;
        this.curRoundEndTime = 0;
        this.curEndTime = 0;
        this.curRound = 0;
        this.nextRoundTime = 0;
        this.statusMap.clear();
        this.cntMap.clear();
    }

    public CommonPb.CycleData createCycleDataPb() {
        CommonPb.CycleData.Builder builder = CommonPb.CycleData.newBuilder();
        builder.setOpenCnt(openCnt);
        builder.setCurPreViewTime(curPreViewTime);
        builder.setCurRoundStartTime(curRoundStartTime);
        builder.setCurRoundEndTime(curRoundEndTime);
        builder.setCurEndTime(curEndTime);
        builder.setCurRound(curRound);
        builder.setNextRoundTime(nextRoundTime);
        return builder.build();
    }

    public void dser(CommonPb.CycleData cycleData) {
        this.openCnt = cycleData.getOpenCnt();
        this.curPreViewTime = cycleData.getCurPreViewTime();
        this.curRoundStartTime = cycleData.getCurRoundStartTime();
        this.curRoundEndTime = cycleData.getCurRoundEndTime();
        this.curEndTime = cycleData.getCurEndTime();
        this.curRound = cycleData.getCurRound();
        this.nextRoundTime = cycleData.getNextRoundTime();
    }

    public int incrOpenCnt() {
        return ++openCnt;
    }

    public int incrCurRound() {
        return curRound++;
    }

    public int getOpenCnt() {
        return openCnt;
    }

    public void setOpenCnt(int openCnt) {
        this.openCnt = openCnt;
    }

    public int getCurPreViewTime() {
        return curPreViewTime;
    }

    public void setCurPreViewTime(int curPreViewTime) {
        this.curPreViewTime = curPreViewTime;
    }

    public int getCurRoundStartTime() {
        return curRoundStartTime;
    }

    public void setCurRoundStartTime(int curRoundStartTime) {
        this.curRoundStartTime = curRoundStartTime;
    }

    public int getCurRoundEndTime() {
        return curRoundEndTime;
    }

    public void setCurRoundEndTime(int curRoundEndTime) {
        this.curRoundEndTime = curRoundEndTime;
    }

    public int getCurEndTime() {
        return curEndTime;
    }

    public void setCurEndTime(int curEndTime) {
        this.curEndTime = curEndTime;
    }

    public int getCurRound() {
        return curRound;
    }

    public int getNextRoundTime() {
        return nextRoundTime;
    }

    public void setNextRoundTime(int nextRoundTime) {
        this.nextRoundTime = nextRoundTime;
    }

    public Map<Long, Integer> getStatusMap() {
        return statusMap;
    }

    public void setStatusMap(Map<Long, Integer> statusMap) {
        this.statusMap = statusMap;
    }

    public Map<Long, Integer> getCntMap() {
        return cntMap;
    }

    public void setCntMap(Map<Long, Integer> cntMap) {
        this.cntMap = cntMap;
    }

}
