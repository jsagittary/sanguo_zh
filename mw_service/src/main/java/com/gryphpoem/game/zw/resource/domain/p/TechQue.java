package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.pb.CommonPb;

/**
 * 科技队列
 * 
 * @author tyler
 *
 */
public class TechQue {
    private int id;
    private int endTime;// 研究结束时间
    private int freeCnt;// 已使用科技官免费次数
    private int freeTime;// 使用的科技官免费时间(服务器用)
    private int freeOtherCnt;// 免费加速次数(奖励获取)
    private int param;// 免费加速的参数值,记录免费时间(单位秒)

    /**
     * 是否有免费加速没有使用
     *
     * @return
     */
    public boolean haveFreeSpeed() {
        return freeOtherCnt > 0;
    }

    public TechQue() {
    }

    public TechQue(int id, int endTime, int freeCnt, int freeTime) {
        this.id = id;
        this.endTime = endTime;
        this.freeCnt = freeCnt;
        this.freeTime = freeTime;
    }

    public TechQue(CommonPb.TechQue techQue) {
        this.id = techQue.getTechId();
        this.endTime = techQue.getTechEndTime();
        this.freeCnt = techQue.getFreeSpeedCnt();
        this.freeTime = techQue.getFreeTime();
        if (techQue.hasFreeOtherCnt()) {
            this.freeOtherCnt = techQue.getFreeOtherCnt();
        }
        if (techQue.hasParam()) {
            this.param = techQue.getParam();
        }
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFreeCnt() {
        return freeCnt;
    }

    public void setFreeCnt(int freeCnt) {
        this.freeCnt = freeCnt;
    }

    public int getFreeTime() {
        return freeTime;
    }

    public void setFreeTime(int freeTime) {
        if (freeTime < 0) {
            freeTime = 0;
        }
        this.freeTime = freeTime;
    }

    public int getFreeOtherCnt() {
        return freeOtherCnt;
    }

    public void setFreeOtherCnt(int freeOtherCnt) {
        this.freeOtherCnt = freeOtherCnt;
    }

    public int getParam() {
        return param;
    }

    public void setParam(int param) {
        this.param = param;
    }

    public int incrFreeOtherCnt() {
        return ++freeOtherCnt;
    }

    public int decreaseFreeOtherCnt() {
        if (freeOtherCnt > 0) {
            --freeOtherCnt;
        }
        return freeOtherCnt;
    }

    @Override
    public String toString() {
        return "TechQue [id=" + id + ", endTime=" + endTime + ", freeCnt=" + freeCnt + ", freeTime=" + freeTime
                + ", freeOtherCnt=" + freeOtherCnt + ", param=" + param + "]";
    }

}