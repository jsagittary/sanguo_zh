package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;
import java.util.Map;

public class StaticMine {
    /**金矿*/
    public static final int MINE_TYPE_GOLD = 5;
    /**铀矿*/
    public static final int MINE_TYPE_URANIUM  = 6;
    
	private int mineId;
	private int mineType;// 矿点类型
	private int lv;// 矿点等级
	private int weight;// 同等级间，矿点随机到的权
	private int speed;// 采集速度，单位：资源/小时
	private List<List<Integer>> reward;// 矿点资源产出总量，格式：[[type,id,count]]
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

	public int getLv() {
		return lv;
	}

	public void setLv(int lv) {
		this.lv = lv;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public List<List<Integer>> getReward() {
		return reward;
	}

	public void setReward(List<List<Integer>> reward) {
		this.reward = reward;
	}

	public Map<Integer, Integer> getCollectTime() {
		return collectTime;
	}

	public void setCollectTime(Map<Integer, Integer> collectTime) {
		this.collectTime = collectTime;
	}

	public int getResourceId() {
		return reward.get(0).get(1);
	}

	public int getResource() {
		return reward.get(0).get(2);
	}

	@Override
	public String toString() {
		return "StaticMine [mineId=" + mineId + ", mineType=" + mineType + ", lv=" + lv + ", weight=" + weight
				+ ", speed=" + speed + ", reward=" + reward + ", collectTime=" + collectTime + "]";
	}
}
