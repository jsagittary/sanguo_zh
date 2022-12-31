package com.gryphpoem.game.zw.pojo.p;

import com.gryphpoem.push.util.CheckNull;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description: 战斗中的buff与效果
 * Author: zhangpeng
 * createTime: 2022-10-31 10:47
 */
@Data
public class FightBuffEffect {
    /**
     * 效果map  <效果逻辑id, <效果id， 效果列表>>
     */
    private Map<Integer, Map<Integer, List<FightEffectData>>> effectMap;
    /**
     * 玩家归属
     */
    private Force force;
    /**
     * 武将归属
     */
    private int heroId;

    public FightBuffEffect(Force force, int heroId) {
        this.force = force;
        this.heroId = heroId;
        this.effectMap = new HashMap<>();
    }

    public List<FightEffectData> getDataList(int effectLogicId, int effectId) {
        if (CheckNull.isEmpty(effectMap)) return null;
        List<FightEffectData> dataList;
        Map<Integer, List<FightEffectData>> dataMap;
        if (CheckNull.isEmpty(dataMap = effectMap.get(effectLogicId)) ||
                CheckNull.isNull(dataList = dataMap.get(effectId)))
            return null;
        return dataList;
    }
}
