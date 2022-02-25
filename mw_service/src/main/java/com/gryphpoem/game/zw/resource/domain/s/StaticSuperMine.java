package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;
import java.util.Map;

/**
 * @ClassName StaticSuperMine.java
 * @Description 超级矿点配置
 * @author QiuKun
 * @date 2018年7月17日
 */
public class StaticSuperMine {
    private int mineId;
    private int mineType;// 1 矿点类型 超级油田 2 超级变电站 3 超级庄园 4 超级矿场
    private List<List<Integer>> reward;// 矿点资源产出总量
    private int speed;// 采集速度，单位：资源/小时
    private Map<Integer, Integer> collectTime;// 将领可采集时间上限，格式：[[quality,maxTime]]，其中maxTime单位：秒

    public int getMineId() {
        return mineId;
    }

    public void setMineId(int mineId) {
        this.mineId = mineId;
    }

    public int getMineType() {
        return mineType;
    }

    public void setMineType(int mineType) {
        this.mineType = mineType;
    }

    public List<List<Integer>> getReward() {
        return reward;
    }

    public void setReward(List<List<Integer>> reward) {
        this.reward = reward;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public Map<Integer, Integer> getCollectTime() {
        return collectTime;
    }

    public void setCollectTime(Map<Integer, Integer> collectTime) {
        this.collectTime = collectTime;
    }

    @Override
    public String toString() {
        return "StaticSuperMine [mineId=" + mineId + ", mineType=" + mineType + ", reward=" + reward + ", speed="
                + speed + ", collectTime=" + collectTime + "]";
    }

}
