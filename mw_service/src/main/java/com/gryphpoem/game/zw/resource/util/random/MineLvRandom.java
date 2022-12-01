package com.gryphpoem.game.zw.resource.util.random;

import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.resource.domain.s.StaticMine;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.RandomUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author TanDonghai
 * @ClassName MineLvRandom.java
 * @Description 用于矿点随机
 * @date 创建时间：2017年4月1日 上午10:44:15
 */
public class MineLvRandom {
    // 老地图使用
    private static Map<Integer, List<StaticMine>> mineLvMap = null;

    // key:lv, value:weight
    private static Map<Integer, Integer> totalWeightMap;
    // 新地图使用 <lv,list<StaticMine>>
    private static Map<Integer, List<StaticMine>> newMapMineLvMap;

    public static void initData(Map<Integer, StaticMine> mineMap) {
        Integer totalWeight;
        List<StaticMine> list;
        mineLvMap = new HashMap<>();
        totalWeightMap = new HashMap<>();
        for (StaticMine mine : mineMap.values()) {
            if (mine.getMineType() == StaticMine.MINE_TYPE_URANIUM) { // 老地图过滤掉铀矿
                continue;
            }
            list = mineLvMap.get(mine.getLv());
            if (null == list) {
                list = new ArrayList<>();
                mineLvMap.put(mine.getLv(), list);
            }
            list.add(mine);

            totalWeight = totalWeightMap.get(mine.getLv());
            if (null == totalWeight) {
                totalWeightMap.put(mine.getLv(), mine.getWeight());
            } else {
                totalWeightMap.put(mine.getLv(), totalWeight + mine.getWeight());
            }
        }
        newMapMineLvMap = mineMap.values().stream().collect(Collectors.groupingBy(StaticMine::getLv));
    }

    /**
     * 新地图使用
     *
     * @param mineLv
     * @return
     */
    public static StaticMine randomNewMapMineByLv(int mineLv) {
        List<StaticMine> list = newMapMineLvMap.get(mineLv);
        if (CheckNull.isEmpty(list)) {
            return null;
        }
        StaticMine res = RandomUtil.getWeightByList(list, StaticMine::getWeight);
        return res;
    }

    /**
     * 根据矿点等级，随机一个同等级的矿
     *
     * @param mineLv
     * @return
     */
    public static StaticMine randomMineByLv(int mineLv) {
        List<StaticMine> list = mineLvMap.get(mineLv);
        if (CheckNull.isEmpty(list)) {
            return null;
        }

        Integer totalWeight = totalWeightMap.get(mineLv);
        if (null == totalWeight) {
            return list.get(0);
        }

        int temp = 0;
        int random = RandomHelper.randomInSize(totalWeight);
        for (StaticMine mine : list) {
            temp += mine.getWeight();
            if (temp >= random) {
                return mine;
            }
        }
        return null;
    }
}
