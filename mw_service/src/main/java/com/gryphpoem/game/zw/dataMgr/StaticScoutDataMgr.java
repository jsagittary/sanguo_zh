package com.gryphpoem.game.zw.dataMgr;

import java.util.List;
import java.util.Map;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.dao.impl.s.StaticDataDao;
import com.gryphpoem.game.zw.resource.domain.s.StaticScoutCost;
import com.gryphpoem.game.zw.resource.domain.s.StaticScoutWeight;
import com.gryphpoem.game.zw.resource.util.RandomHelper;

public class StaticScoutDataMgr {

	private static StaticDataDao staticDataDao = DataResource.ac.getBean(StaticDataDao.class);

	// 侦查消耗信息
	private static Map<Integer, StaticScoutCost> costMap;

	private static Map<Integer, StaticScoutWeight> weightMap;

	public static void init() {
		Map<Integer, StaticScoutCost> costMap = staticDataDao.selectScoutCostMap();
		StaticScoutDataMgr.costMap = costMap;

		Map<Integer, StaticScoutWeight> weightMap = staticDataDao.selectScoutWeightMap();
		for (StaticScoutWeight ssw : weightMap.values()) {
			ssw.calcTotalWeight();
		}
		StaticScoutDataMgr.weightMap = weightMap;
	}

	public static StaticScoutCost getScoutCostByCityLv(int cityLv) {
		return costMap.get(cityLv);
	}

	public static StaticScoutWeight getScoutWeightByLvGap(int gap) {
		return weightMap.get(gap);
	}

	/**
	 * 根据侦查科技等级差随机获取侦查结果
	 * 
	 * @param gap
	 * @return
	 */
	public static int randomScoutResultByLvGap(int gap) {
		StaticScoutWeight ssw = getScoutWeightByLvGap(gap);
		if (null != ssw) {
			int temp = 0;
			int random = RandomHelper.randomInSize(ssw.getTotalWeight());
			for (List<Integer> list : ssw.getWeightList()) {
				temp += list.get(1);
				if (temp >= random) {
					return list.get(0);
				}
			}
		} else {
			LogUtil.error("随机侦查结果，侦查信息未配置, gap:", gap);
		}

		return WorldConstant.SCOUT_RET_FAIL;
	}
}
