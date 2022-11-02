package com.gryphpoem.game.zw.pojo.p;

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
     * 效果map  <效果id， 效果列表>
     */
    private Map<Integer, Map<Integer, List<FightEffectData>>> effectMap;

    public FightBuffEffect() {
        this.effectMap = new HashMap<>();
    }
}
