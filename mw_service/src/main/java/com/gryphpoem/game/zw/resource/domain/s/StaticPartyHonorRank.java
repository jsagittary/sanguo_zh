package com.gryphpoem.game.zw.resource.domain.s;

/**
 * @ClassName StaticPartyHonorRank.java
 * @Description 军团荣誉排行配置信息
 * @author TanDonghai
 * @date 创建时间：2017年5月4日 下午8:45:17
 *
 */
public class StaticPartyHonorRank {
	private int rankType;// 荣誉排行榜类型，1 每周城战次数排行榜，2 国战次数排行榜，3 建设次数排行榜
	private int rank;// 排名，从1开始，需要列出所有需要配置的排名，用-1表示其他（未进入榜单前列）排名
	private int reward;// 奖励选票数

	public int getRankType() {
		return rankType;
	}

	public void setRankType(int rankType) {
		this.rankType = rankType;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getReward() {
		return reward;
	}

	public void setReward(int reward) {
		this.reward = reward;
	}

	@Override
	public String toString() {
		return "StaticPartyHonorRank [rankType=" + rankType + ", rank=" + rank + ", reward=" + reward + "]";
	}
}
