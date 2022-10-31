package com.gryphpoem.game.zw.pojo.p;

import com.gryphpoem.game.zw.buff.IFightBuff;
import com.gryphpoem.push.util.CheckNull;
import lombok.Data;

import java.util.HashMap;

/**
 * Description: 战斗中的buff与效果
 * Author: zhangpeng
 * createTime: 2022-10-31 10:47
 */
@Data
public class FightBuffEffect {
    /**
     * buff效果map
     */
    private HashMap<Long, HashMap<Integer, Integer>> buffEffectMap;
    /**
     * 效果map
     */
    private HashMap<Integer, Double> effectMap;
    /**
     * buff map
     */
    private HashMap<Long, IFightBuff> fightBuffMap;

    public FightBuffEffect() {
        this.buffEffectMap = new HashMap<>();
        this.effectMap = new HashMap<>();
        this.fightBuffMap = new HashMap<>();
    }

    /**
     * buff效果转为effect总览
     */
    public void buffTransferEffect() {
        if (CheckNull.isEmpty(buffEffectMap))
            return;
        if (CheckNull.isNull(effectMap))
            effectMap = new HashMap<>();
    }
}
