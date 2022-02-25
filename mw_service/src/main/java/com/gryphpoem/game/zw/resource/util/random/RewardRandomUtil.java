package com.gryphpoem.game.zw.resource.util.random;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gryphpoem.game.zw.resource.domain.s.StaticRewardRandom;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.RandomHelper;

/**
 * @ClassName RewardRadomUtil.java
 * @Description 随机奖励类型工具类
 * @author TanDonghai
 * @date 创建时间：2017年4月5日 下午7:13:06
 *
 */
public class RewardRandomUtil {
	// 记录个randomId对应的奖励物品总权重
	private static Map<Integer, Integer> totalWeightMap;

	private static Map<Integer, List<List<Integer>>> randomMap;

	public static void initData(Map<Integer, StaticRewardRandom> map) {
		int totalWight;
		randomMap = new HashMap<>();
		totalWeightMap = new HashMap<>();
		for (StaticRewardRandom srr : map.values()) {
			if (!CheckNull.isEmpty(srr.getRandomStr())) {
				randomMap.put(srr.getRandomId(), srr.getRandomStr());

				totalWight = 0;
				for (List<Integer> list : srr.getRandomStr()) {
					totalWight += list.get(3);
				}
				totalWeightMap.put(srr.getRandomId(), totalWight);
			}
		}
	}

	/**
	 * 根据randomId随机获取对应的奖励信息
	 * 
	 * @param randomId
	 * @return
	 */
	public static List<Integer> getAwardByRandomId(int randomId) {
		int totalWight = totalWeightMap.get(randomId);
		int random = RandomHelper.randomInSize(totalWight);

		int temp = 0;
		for (List<Integer> list : randomMap.get(randomId)) {
			temp += list.get(3);
			if (temp >= random) {
				return list;
			}
		}

		return null;
	}
}
