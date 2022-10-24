package com.gryphpoem.game.zw.data.p;

import com.gryphpoem.push.util.CheckNull;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-10-20 16:02
 */
@Data
public class FightResult {
    /**
     * buff与effect map
     */
    private Map<Integer, Map<Integer, Integer>> buffEffectMap;
    /**
     * effect参数集合
     */
    private Map<Integer, Integer> effectMap;

    /**
     * buff转为effect总览
     */
    public void buffTransferEffect() {
        if (CheckNull.isEmpty(buffEffectMap))
            return;
        effectMap = new HashMap<>();
        buffEffectMap.values().forEach(effectMap::putAll);
    }
}
