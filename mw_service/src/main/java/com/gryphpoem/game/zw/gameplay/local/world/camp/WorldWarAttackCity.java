package com.gryphpoem.game.zw.gameplay.local.world.camp;

import java.util.Map;

/**
 * Created by pengshuo on 2019/3/23 14:51
 * <br>Description: 世界阵营 城市征战
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class WorldWarAttackCity {
    /** 阵营积分 */
    private int campIntegral;
    /** 玩家积分积分 */
    private int personalIntegral;
    /** 玩家世界争霸阵营军威值奖励 领取记录*/
    private Map<Integer,Integer> awardRecord;
    /** 红点数量 */
    private int tips;

    public int getCampIntegral() {
        return campIntegral;
    }

    public void setCampIntegral(int campIntegral) {
        this.campIntegral = campIntegral;
    }

    public int getPersonalIntegral() {
        return personalIntegral;
    }

    public void setPersonalIntegral(int personalIntegral) {
        this.personalIntegral = personalIntegral;
    }

    public Map<Integer, Integer> getAwardRecord() {
        return awardRecord;
    }

    public void setAwardRecord(Map<Integer, Integer> awardRecord) {
        this.awardRecord = awardRecord;
    }

    public int getTips() {
        return tips;
    }

    public void setTips(int tips) {
        this.tips = tips;
    }

    @Override
    public String toString() {
        return "WorldWarAttackCity{" +
                "campIntegral=" + campIntegral +
                ", personalIntegral=" + personalIntegral +
                ", awardRecord=" + awardRecord +
                ", tips=" + tips +
                '}';
    }
}
