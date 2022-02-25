package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticRewardRandom.java
 * @Description 随机奖励权重配置
 * @author TanDonghai
 * @date 创建时间：2017年4月5日 下午6:30:09
 *
 */
public class StaticRewardRandom {
	private int randomId;
	private List<List<Integer>> randomStr;

	public int getRandomId() {
		return randomId;
	}

	public void setRandomId(int randomId) {
		this.randomId = randomId;
	}

	public List<List<Integer>> getRandomStr() {
		return randomStr;
	}

	public void setRandomStr(List<List<Integer>> randomStr) {
		this.randomStr = randomStr;
	}

	@Override
	public String toString() {
		return "StaticRewardRandom [randomId=" + randomId + ", random=" + randomStr + "]";
	}
}
