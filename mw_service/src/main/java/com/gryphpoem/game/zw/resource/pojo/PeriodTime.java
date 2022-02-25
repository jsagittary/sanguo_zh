package com.gryphpoem.game.zw.resource.pojo;

/**
 * @ClassName PeriodTime.java
 * @Description 周期性的时间
 * @author QiuKun
 * @date 2019年5月15日
 */
public class PeriodTime {

    /** 预显示 */
    protected int preViewTime;
    /** 开始时间 */
    protected int startTime;
    /** 结束时间 */
    protected int endTime;

    public int getPreViewTime() {
        return preViewTime;
    }

    public void setPreViewTime(int preViewTime) {
        this.preViewTime = preViewTime;
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

    @Override
    public String toString() {
        return "PeriodTime [preViewTime=" + preViewTime + ", startTime=" + startTime + ", endTime=" + endTime + "]";
    }

}
