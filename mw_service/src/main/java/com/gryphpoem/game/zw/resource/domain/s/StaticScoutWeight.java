package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticScoutWeight.java
 * @Description 侦查结果权重配置表
 * @author TanDonghai
 * @date 创建时间：2017年4月18日 下午8:40:52
 *
 */
public class StaticScoutWeight {
	private int gap;// 科技等级差
	private List<List<Integer>> weightList;// 侦查结果权重配置，格式：[[result,weight]...]，result为侦查结果类型，0 失败，1 资源，2 资源、城池，3
											// 资源、城池、将领，weight为权重
	private int totalWeight;

	public int getGap() {
		return gap;
	}

	public void setGap(int gap) {
		this.gap = gap;
	}

	public List<List<Integer>> getWeightList() {
		return weightList;
	}

	public void setWeightList(List<List<Integer>> weightList) {
		this.weightList = weightList;
	}

	public int getTotalWeight() {
		return totalWeight;
	}

	public void calcTotalWeight() {
		for (List<Integer> list : weightList) {
			totalWeight += list.get(1);
		}
	}
}
