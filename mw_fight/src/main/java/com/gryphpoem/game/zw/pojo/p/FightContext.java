package com.gryphpoem.game.zw.pojo.p;

import lombok.Data;

import java.util.HashMap;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-10-31 15:41
 */
@Data
public class FightContext {
    /**
     * <buff唯一id, map<效果id, 效果值>>
     */
    private HashMap<Long, HashMap<Integer, Object>> effectValueMap;
    private Force attacker;
    private Force defender;
    /**
     * 回合数
     */
    private int roundNum;

    public void clear() {
        this.effectValueMap.clear();
        this.attacker = null;
        this.defender = null;
        this.roundNum = 0;
    }
}
