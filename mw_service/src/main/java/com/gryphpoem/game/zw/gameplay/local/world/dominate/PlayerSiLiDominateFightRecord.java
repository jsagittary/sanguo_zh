package com.gryphpoem.game.zw.gameplay.local.world.dominate;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-24 11:11
 */
public class PlayerSiLiDominateFightRecord {
    private int killCnt;        // 总击杀兵力
    private int killRankTime;   // 累积击杀上榜时间
    private Map<Integer, Integer> continuousKillCntMap;

    public PlayerSiLiDominateFightRecord() {
        this.continuousKillCntMap = new HashMap<>();
    }

    public void addKillCnt(int hurt, int now) {
        this.killCnt += hurt;
        this.killRankTime = now;
    }

    public int getKillCnt() {
        return killCnt;
    }

    public int getKillRankTime() {
        return killRankTime;
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
    public int incContinuousKillCnt(int cityId) {
        int killCnt = this.continuousKillCntMap.getOrDefault(cityId, 0);
        this.continuousKillCntMap.put(cityId, ++killCnt);
        return killCnt;
    }

    /**
     * 清除连杀次数
     *
     * @param cityId
     */
    public void clearContinuousKillCnt(int cityId) {
        int killCnt = this.continuousKillCntMap.getOrDefault(cityId, 0);
        if (killCnt > 0) killCnt = 0;
        this.continuousKillCntMap.put(cityId, killCnt);
    }
}
