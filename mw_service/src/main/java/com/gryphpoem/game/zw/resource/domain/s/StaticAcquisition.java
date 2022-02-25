package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticAcquisition.java
 * @Description 个人资源点配置信息
 * @author TanDonghai
 * @date 创建时间：2017年5月9日 下午6:00:01
 *
 */
public class StaticAcquisition {
	private int id;// 资源id
	private int type;// 资源类型
	private int maxNum;// 一天最多可采集次数
	private int collectTime;// 采集时间
	private List<List<Integer>> cost;// 消耗资源，格式：[[type,id,count]...]
	private List<List<Integer>> reward;// 奖励资源，格式：[[type,id,count]...]

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getMaxNum() {
		return maxNum;
	}

	public void setMaxNum(int maxNum) {
		this.maxNum = maxNum;
	}

	public int getCollectTime() {
		return collectTime;
	}

	public void setCollectTime(int collectTime) {
		this.collectTime = collectTime;
	}

	public List<List<Integer>> getCost() {
		return cost;
	}

	public void setCost(List<List<Integer>> cost) {
		this.cost = cost;
	}

	public List<List<Integer>> getReward() {
		return reward;
	}

	public void setReward(List<List<Integer>> reward) {
		this.reward = reward;
	}

	@Override
	public String toString() {
		return "StaticAcquisition [id=" + id + ", type=" + type + ", maxNum=" + maxNum + ", collectTime=" + collectTime
				+ ", cost=" + cost + ", reward=" + reward + "]";
	}
}
