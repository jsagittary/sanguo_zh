package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticHeroSearch.java
 * @Description 良将、神将寻访配置信息
 * @author TanDonghai
 * @date 创建时间：2017年5月23日 下午2:32:47
 *
 */
public class StaticHeroSearch {
	private int autoId;// 自增id
	private int searchType;// 抽卡池
	private int rewardType;// 奖励结果类型: 奖励结果类型，1 橙色武将，2 紫色武将 3 橙色武将碎片 4 紫色武将碎片 5 道具奖励
	private int grid;// 界面上的格子索引
	private List<List<Integer>> rewardList;// 具体奖励，格式：[[type,id,count]...]
	private int weight;// 随机到的权重

	public int getAutoId() {
		return autoId;
	}

	public void setAutoId(int autoId) {
		this.autoId = autoId;
	}

	public int getSearchType() {
		return searchType;
	}

	public void setSearchType(int searchType) {
		this.searchType = searchType;
	}

	public int getRewardType() {
		return rewardType;
	}

	public void setRewardType(int rewardType) {
		this.rewardType = rewardType;
	}

	public int getGrid() {
		return grid;
	}

	public void setGrid(int grid) {
		this.grid = grid;
	}

	public List<List<Integer>> getRewardList() {
		return rewardList;
	}

	public void setRewardList(List<List<Integer>> rewardList) {
		this.rewardList = rewardList;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	@Override
	public String toString() {
		return "StaticHeroSearch [autoId=" + autoId + ", searchType=" + searchType + ", rewardType=" + rewardType
				+ ", grid=" + grid + ", rewardList=" + rewardList + ", weight=" + weight + "]";
	}
}
