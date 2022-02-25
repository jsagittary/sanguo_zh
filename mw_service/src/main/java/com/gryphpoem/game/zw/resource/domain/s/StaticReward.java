package com.gryphpoem.game.zw.resource.domain.s;

import java.util.Map;

/**
 * @ClassName StaticReward.java
 * @Description 随机奖励概率配置信息
 * @author TanDonghai
 * @date 创建时间：2017年4月5日 下午6:28:11
 *
 */
public class StaticReward {
	private int rewardId;
	private Map<Integer, Integer> rewardStr;

	public int getRewardId() {
		return rewardId;
	}

	public void setRewardId(int rewardId) {
		this.rewardId = rewardId;
	}

	public Map<Integer, Integer> getRewardStr() {
		return rewardStr;
	}

	public void setRewardStr(Map<Integer, Integer> rewardStr) {
		this.rewardStr = rewardStr;
	}

	@Override
	public String toString() {
		return "StaticReward [rewardId=" + rewardId + ", rewardMap=" + rewardStr + "]";
	}
}
