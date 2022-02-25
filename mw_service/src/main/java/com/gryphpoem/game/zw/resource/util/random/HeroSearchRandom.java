package com.gryphpoem.game.zw.resource.util.random;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gryphpoem.game.zw.resource.constant.HeroConstant;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroSearch;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.RandomHelper;

/**
 * @ClassName HeroSearchRandom.java
 * @Description 良将、神将寻访随机
 * @author TanDonghai
 * @date 创建时间：2017年5月23日 下午2:45:41
 *
 */
public class HeroSearchRandom {
	// key:searchType
	private static Map<Integer, List<StaticHeroSearch>> searchMap;

	private static Map<Integer, Integer> totalWeightMap;

	/** 良将寻访，必出良将key */
	private static final int NORMAL_HERO = 888;
	/** 神将寻访，必出神将key */
	private static final int SUPER_HERO = 999;

	/**
	 * 初始化，加载配置数据时调用
	 * 
	 * @param map
	 */
	public static void init(Map<Integer, StaticHeroSearch> map) {
		searchMap = new HashMap<>();
		totalWeightMap = new HashMap<>();

		if (!CheckNull.isEmpty(map)) {
			Integer totalWeight;
			List<StaticHeroSearch> list;
			for (StaticHeroSearch shs : map.values()) {
				list = searchMap.get(shs.getSearchType());
				if (null == list) {
					list = new ArrayList<>();
					searchMap.put(shs.getSearchType(), list);
				}
				list.add(shs);

				totalWeight = totalWeightMap.get(shs.getSearchType());
				if (null == totalWeight) {
					totalWeightMap.put(shs.getSearchType(), shs.getWeight());
				} else {
					totalWeightMap.put(shs.getSearchType(), totalWeight + shs.getWeight());
				}

				// 只出将领的随机处理
				if (shs.getRewardType() == HeroConstant.SEARCH_RESULT_HERO) {
					int key = NORMAL_HERO;
					if (shs.getSearchType() == HeroConstant.SEARCH_TYPE_SUPER) {
						key = SUPER_HERO;
					}
					list = searchMap.get(key);
					if (null == list) {
						list = new ArrayList<>();
						searchMap.put(key, list);
					}
					list.add(shs);

					totalWeight = totalWeightMap.get(key);
					if (null == totalWeight) {
						totalWeightMap.put(key, shs.getWeight());
					} else {
						totalWeightMap.put(key, totalWeight + shs.getWeight());
					}
				}
			}
		}
	}

	/**
	 * 根据将领寻访类型随机返回一个结果
	 * 
	 * @param searchType 寻访类型，1 良将寻访，2 神将寻访
	 * @return
	 */
	public static StaticHeroSearch randomRewardBySearchType(int searchType) {
		Integer totalWeight = totalWeightMap.get(searchType);
		if (null == totalWeight) {
			return null;
		}

		int temp = 0;
		int random = RandomHelper.randomInSize(totalWeight);
		List<StaticHeroSearch> list = searchMap.get(searchType);
		if (!CheckNull.isEmpty(list)) {
			for (StaticHeroSearch shs : list) {
				temp += shs.getWeight();
				if (temp >= random) {
					return shs;
				}
			}
		}

		return null;
	}

	/**
	 * 根据将领寻访类型随机返回一个将领奖励结果（该方法仅在必出将领的情况下使用）
	 * 
	 * @param searchType 寻访类型，1 良将寻访，2 神将寻访
	 * @return
	 */
	public static StaticHeroSearch randomHeroBySearchType(int searchType) {
		int key = NORMAL_HERO;
		if (searchType == HeroConstant.SEARCH_TYPE_SUPER) {
			key = SUPER_HERO;
		}

		Integer totalWeight = totalWeightMap.get(key);
		if (null == totalWeight) {
			return null;
		}

		int temp = 0;
		int random = RandomHelper.randomInSize(totalWeight);
		List<StaticHeroSearch> list = searchMap.get(key);
		if (!CheckNull.isEmpty(list)) {
			for (StaticHeroSearch shs : list) {
				temp += shs.getWeight();
				if (temp >= random) {
					return shs;
				}
			}
		}

		return null;
	}
}
