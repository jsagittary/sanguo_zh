package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @program: server
 * @description:
 * @author: zhou jie
 * @create: 2019-12-02 14:41
 */
public class StaticBattlePassLv {

    private int id;

    /**
     * 等级
     */
    private int lv;
    /**
     * 升级需要的经验
     */
    private int needExp;
    /**
     * 直接升到下一级需要的金币
     */
    private int subGold;
    /**
     * 当前等级可以领取的普通奖励
     */
    private List<List<Integer>> normalAward;
    /**
     * 战令进阶后可以领取的奖励
     */
    private List<List<Integer>> specialAward;

    /**
     * 对应s_battlepass_plan表里的keyId
     */
    private int planKey;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPlanKey() {
        return planKey;
    }

    public void setPlanKey(int planKey) {
        this.planKey = planKey;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public int getNeedExp() {
        return needExp;
    }

    public void setNeedExp(int needExp) {
        this.needExp = needExp;
    }

    public int getSubGold() {
        return subGold;
    }

    public void setSubGold(int subGold) {
        this.subGold = subGold;
    }

    public List<List<Integer>> getNormalAward() {
        return normalAward;
    }

    public void setNormalAward(List<List<Integer>> normalAward) {
        this.normalAward = normalAward;
    }

    public List<List<Integer>> getSpecialAward() {
        return specialAward;
    }

    public void setSpecialAward(List<List<Integer>> specialAward) {
        this.specialAward = specialAward;
    }
}