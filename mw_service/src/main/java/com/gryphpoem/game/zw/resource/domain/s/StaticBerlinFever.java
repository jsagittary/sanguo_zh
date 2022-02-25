package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 柏林战斗狂热
 * @description:
 * @author: zhou jie
 * @time: 2021/11/11 15:24
 */
public class StaticBerlinFever {

    /**
     * 唯一id
     */
    private int id;

    /**
     * 攻击&防守方兵力差值
     */
    private List<Integer> forceDifference;

    /**
     * 持续时间
     */
    private int duration;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getForceDifference() {
        return forceDifference;
    }

    public void setForceDifference(List<Integer> forceDifference) {
        this.forceDifference = forceDifference;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
