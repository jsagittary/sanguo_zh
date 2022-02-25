package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticRecommend.java
 * @Description 推荐阵营奖励的配置
 * @author QiuKun
 * @date 2017年11月2日
 */
public class StaticRecommend {
    /**
     * 策略1 人数最少的阵营
     */
    public static final int STRATEGY_TYPE_NUM_SMALL = 1;
    /**
     * 策略2 两百名玩家内战斗总和力小的阵营
     */
    public static final int STRATEGY_TYPE_FIGHT_SMALL = 2;
    private int keyId;// 唯一Id
    private int beginDay;// 服务器开始时间
    private int endDay;// 服务器结束时间
    private List<List<Integer>> award;// 推荐阵营奖励
    private int type; // 1 人数最少的阵营, 2 两百名玩家内战斗总和力小的阵营

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getBeginDay() {
        return beginDay;
    }

    public void setBeginDay(int beginDay) {
        this.beginDay = beginDay;
    }

    public int getEndDay() {
        return endDay;
    }

    public void setEndDay(int endDay) {
        this.endDay = endDay;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

}
