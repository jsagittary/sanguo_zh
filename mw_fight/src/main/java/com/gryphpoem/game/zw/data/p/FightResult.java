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
     * buff效果转为effect总览
     */
    public void buffTransferEffect() {
        if (CheckNull.isEmpty(buffEffectMap))
            return;
        if (CheckNull.isNull(effectMap))
            effectMap = new HashMap<>();
        buffEffectMap.values().forEach(effectMap::putAll);
    }

    /**
     * 清除当前这回合中buff 效果信息
     */
    public void clear() {
        if (!CheckNull.isEmpty(buffEffectMap))
            buffEffectMap.clear();
        if (!CheckNull.isEmpty(effectMap))
            effectMap.clear();
    }
}
