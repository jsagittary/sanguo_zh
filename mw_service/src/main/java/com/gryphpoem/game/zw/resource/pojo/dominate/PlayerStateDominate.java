package com.gryphpoem.game.zw.resource.pojo.dominate;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-23 18:59
 */
public class PlayerStateDominate {
    private long roleId;
    private Map<Integer, Integer> continuousKillCntMap;

    public PlayerStateDominate(long roleId) {
        this.roleId = roleId;
        this.continuousKillCntMap = new HashMap<>();
    }

    public Map<Integer, Integer> getContinuousKillCntMap() {
        return continuousKillCntMap;
    }

    public Map<Integer, Integer> getContinuousKillCnt() {
        return continuousKillCntMap;
    }

    /**
     * 增加连杀次数
     */
    public void incContinuousKillCnt(int cityId) {
        this.continuousKillCntMap.merge(cityId, 1, Integer::sum);
    }
}
