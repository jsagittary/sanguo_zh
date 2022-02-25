package com.gryphpoem.game.zw.resource.pojo.world;

import com.gryphpoem.game.zw.pb.CommonPb;

/**
 * @description:
 * @author: zhou jie
 * @time: 2021/11/11 15:25
 */
public class BerlinBattleFrenzy {

    /**
     * 未触发状态 0
     */
    public static final int BERLIN_BATTLE_FRENZY_STATUS_0 = 0;
    /**
     * 触发状态 1
     */
    public static final int BERLIN_BATTLE_FRENZY_STATUS_1 = 1;
    /**
     * 触发结束状态 2
     */
    public static final int BERLIN_BATTLE_FRENZY_STATUS_2 = 2;


    /**
     * 触发状态
     * 未触发状态 0, 触发状态 1, 触发结束状态 2, 状态转换 0 -> 1 -> 2 -> 1 -> 2
     */
    private int status;

    /**
     * 当状态为1的时候, 这里代表的是触发的结束时间
     */
    private int endTime;

    /**
     * 当状态不为0的是, 这里代表本次活动, 当前触发了几次战斗狂热
     */
    private int count;

    /**
     * 记录本次触发持续时长
     */
    private int duration;

    public BerlinBattleFrenzy() {
    }

    /**
     * 反序列化
     * @param bbf 战斗狂热
     */
    public BerlinBattleFrenzy(CommonPb.BerlinBattleFrenzy bbf) {
        this.status = bbf.getStatus();
        this.endTime = bbf.getEndTime();
        this.count = bbf.getCount();
        this.duration = bbf.getDuration();
    }


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    /**
     * 重开清除数据
     */
    public void clear() {
        this.status = 0;
        this.count = 0;
        this.endTime = 0;
        this.duration = 0;
    }
}
