package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;
import java.util.Map;

/**
 * @ClassName StaticPartyRanks.java
 * @Description 军团军阶配置表
 * @author TanDonghai
 * @date 创建时间：2017年4月25日 下午4:09:34
 *
 */
public class StaticPartyRanks {
	private int ranks;// 军阶id
	private List<List<Integer>> cost;// 晋升所需消耗的材料
	private Map<Integer, Integer> attr;// 军阶加成属性

	public int getRanks() {
		return ranks;
	}

	public void setRanks(int ranks) {
		this.ranks = ranks;
	}

	public List<List<Integer>> getCost() {
		return cost;
	}

	public void setCost(List<List<Integer>> cost) {
		this.cost = cost;
	}

	public Map<Integer, Integer> getAttr() {
		return attr;
	}

	public void setAttr(Map<Integer, Integer> attr) {
		this.attr = attr;
	}

	@Override
	public String toString() {
		return "StaticPartyRanks [ranks=" + ranks + ", cost=" + cost + ", attr=" + attr + "]";
	}
}
