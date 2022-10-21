package com.gryphpoem.game.zw.resource.util.random;

import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.resource.domain.s.StaticMedalGoods;
import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenqi
 * @ClassName: MedalGoodsRandom
 * @Description: 勋章 - 商品随机
 * @date 2018年9月12日
 */
public class MedalGoodsRandom {

    private static Map<Integer, List<StaticMedalGoods>> typeMedalGoods;
    private static Map<Integer, Integer> typeTotalWeight;

    /**
     * 初始化，加载配置数据时调用
     *
     * @param map
     */
    public static void init(Map<Integer, StaticMedalGoods> map) {
        typeMedalGoods = new HashMap<>();
        typeTotalWeight = new HashMap<>();
        if (!CheckNull.isEmpty(map)) {
            for (StaticMedalGoods shs : map.values()) {
                int type = shs.getType();
                List<StaticMedalGoods> sMedalGoods = typeMedalGoods.get(type);
                if (sMedalGoods == null) {
                    sMedalGoods = new ArrayList<>();
                    typeMedalGoods.put(type, sMedalGoods);
                }
                sMedalGoods.add(shs);
                int weight = typeTotalWeight.getOrDefault(type, 0);
                typeTotalWeight.put(type, weight + shs.getWeight());
            }
        }
    }

    /**
     * 根据类型获取一个商品
     *
     * @param type 类型
     * @return 商品id
     */
    public static Integer randomMedalGoods(int type) {
        // 根据type取总权重
        int totalWeight = typeTotalWeight.getOrDefault(type, 0);
        if (totalWeight == 0) {
            return null;
        }
        int temp = 0;
        int random = RandomHelper.randomInSize(totalWeight);
        // 根据type取商品
        List<StaticMedalGoods> medalGoods = typeMedalGoods.get(type);
        if (!CheckNull.isEmpty(medalGoods)) {
            for (StaticMedalGoods shs : medalGoods) {
                temp += shs.getWeight();
                if (temp >= random) {
                    return shs.getMedalGoodsId();
                }
            }
        }
        return null;
    }

    /**
     * 获取指定数量和类型的商品
     *
     * @param type  类型
     * @param count 数量
     * @param goods 商品
     */
    public static void randomMedalGoods(int type, int count, List<Integer> goods) {
        for (int i = 0; i < count; i++) {
            Integer good = randomMedalGoods(type);
            if (good != null) {
                goods.add(good);
            }
        }
    }

}
