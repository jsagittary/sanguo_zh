package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 聚宝盆
 * 
 * @author tyler
 *
 */
public class StaticTreasure {
	private int id; //
	private int type; // 类型1银币库，2金币库
	private List<List<Integer>> reward; //
	private List<List<Integer>> cost; //
	private int pro; //
	private int lv;

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

	public List<List<Integer>> getReward() {
		return reward;
	}

	public void setReward(List<List<Integer>> reward) {
		this.reward = reward;
	}

	public List<List<Integer>> getCost() {
		return cost;
	}

	public void setCost(List<List<Integer>> cost) {
		this.cost = cost;
	}

	public int getPro() {
		return pro;
	}

	public void setPro(int pro) {
		this.pro = pro;
	}

	public int getLv() {
		return lv;
	}

	public void setLv(int lv) {
		this.lv = lv;
	}

	@Override
	public String toString() {
		return "StaticTreasure [id=" + id + ", type=" + type + ", reward=" + reward + ", cost=" + cost + ", pro=" + pro
				+ ", lv=" + lv + "]";
	}
	
	
}
