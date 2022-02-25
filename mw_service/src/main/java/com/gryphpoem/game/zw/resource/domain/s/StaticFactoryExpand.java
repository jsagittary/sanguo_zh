package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 兵营扩建
 * 
 * @author tyler
 *
 */
public class StaticFactoryExpand {
    private int id;
	private int cnt;// 扩建次数
	private int type;// 1步兵
	private List<Integer> cost;// 消耗
	private int buildNum;// 募兵队列
	private int armNum;// 加兵营容量
	private List<List<Integer>> gain;// 获得

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

    public int getCnt() {
		return cnt;
	}

	public void setCnt(int cnt) {
		this.cnt = cnt;
	}

	public List<Integer> getCost() {
        return cost;
    }

    public void setCost(List<Integer> cost) {
        this.cost = cost;
    }

    public int getBuildNum() {
		return buildNum;
	}

	public void setBuildNum(int buildNum) {
		this.buildNum = buildNum;
	}

	public int getArmNum() {
		return armNum;
	}

	public void setArmNum(int armNum) {
		this.armNum = armNum;
	}

	public List<List<Integer>> getGain() {
		return gain;
	}

	public void setGain(List<List<Integer>> gain) {
		this.gain = gain;
	}

}
