package com.gryphpoem.game.zw.resource.domain.p;

/**
 * 打造队列
 * 
 * @author tyler
 *
 */
public class EquipQue {
    private int keyId;
    private int equipId;
    private int period;
    private int endTime;
    private int freeCnt;
    private int freeTime;
    private int employeId; // 上一次加速雇员的id

    public EquipQue(int keyId, int equipId, int period, int endTime, int freeCnt, int freeTime) {
        this.keyId = keyId;
        this.equipId = equipId;
        this.period = period;
        this.endTime = endTime;
        this.freeCnt = freeCnt;
        this.freeTime = freeTime;
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

    public int getFreeCnt() {
        return freeCnt;
    }

    public void setFreeCnt(int freeCnt) {
        this.freeCnt = freeCnt;
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getEquipId() {
        return equipId;
    }

    public void setEquipId(int equipId) {
        this.equipId = equipId;
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

    public int getEmployeId() {
        return employeId;
    }

    public void setEmployeId(int employeId) {
        this.employeId = employeId;
    }

    @Override
    public String toString() {
        return "EquipQue [keyId=" + keyId + ", equipId=" + equipId + ", period=" + period + ", endTime=" + endTime
                + ", freeCnt=" + freeCnt + ", freeTime=" + freeTime + ", employeId=" + employeId + "]";
    }

}
