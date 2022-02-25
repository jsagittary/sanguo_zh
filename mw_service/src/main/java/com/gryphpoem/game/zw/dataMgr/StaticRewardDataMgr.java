package com.gryphpoem.game.zw.dataMgr;

import java.util.Map;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.s.StaticReward;
import com.gryphpoem.game.zw.resource.domain.s.StaticRewardRandom;
import com.gryphpoem.game.zw.resource.util.random.RewardRandomUtil;

/**
 * @ClassName StaticRewardDataMgr.java
 * @Description 奖励相关配置
 * @author TanDonghai
 * @date 创建时间：2017年4月5日 下午6:34:33
 *
 */
public class StaticRewardDataMgr {

	private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);

	private static Map<Integer, StaticReward> rewardMap;

	private static Map<Integer, StaticRewardRandom> randomMap;

	public static void init() {
		Map<Integer, StaticReward> rewardMap = staticDataDao.selectRewradMap();
		StaticRewardDataMgr.rewardMap = rewardMap;

		Map<Integer, StaticRewardRandom> randomMap = staticDataDao.selectRewardRandomMap();
		StaticRewardDataMgr.randomMap = randomMap;
		
		RewardRandomUtil.initData(randomMap);
	}

	public static Map<Integer, StaticReward> getRewardMap() {
		return rewardMap;
	}

	public static Map<Integer, StaticRewardRandom> getRandomMap() {
		return randomMap;
	}

}
