package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticPitchCombat.java
 * @Description 荣耀演习场副本配置
 * @author QiuKun
 * @date 2018年11月28日
 */
public class StaticPitchCombat {
    public static final int PITCH_COMBAT_TYPE_1 = 1;

    private int combatId;
    private int type;// 类型: 1 教官副本
    private int preId; // 前置id
    private int fightpower;// 推荐战力
    private int gearCount;// 该关卡产出的装备数量
    private List<List<Integer>> firstAwardRand; // 首次 产生装备 [[gearOrder,count]],gearOrder装备的阶级
    private List<List<Integer>> firstAward;// 首次 通关奖励
    private List<List<Integer>> winAwardRand; // 产生装备 [[gearOrder,count]],gearOrder装备的阶级
    private List<List<Integer>> winAward;// 通关奖励
    private List<List<Integer>> form;// 阵型
    private int firstAwardpoint; // 首次通关获得点数
    private int winAwardpoint;// 扫荡奖励点数

    public int getCombatId() {
        return combatId;
    }

    public void setCombatId(int combatId) {
        this.combatId = combatId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getPreId() {
        return preId;
    }

    public void setPreId(int preId) {
        this.preId = preId;
    }

    public int getFightpower() {
        return fightpower;
    }

    public void setFightpower(int fightpower) {
        this.fightpower = fightpower;
    }

    public int getGearCount() {
        return gearCount;
    }

    public void setGearCount(int gearCount) {
        this.gearCount = gearCount;
    }

    public List<List<Integer>> getFirstAwardRand() {
        return firstAwardRand;
    }

    public void setFirstAwardRand(List<List<Integer>> firstAwardRand) {
        this.firstAwardRand = firstAwardRand;
    }

    public List<List<Integer>> getWinAwardRand() {
        return winAwardRand;
    }

    public void setWinAwardRand(List<List<Integer>> winAwardRand) {
        this.winAwardRand = winAwardRand;
    }

    public List<List<Integer>> getForm() {
        return form;
    }

    public void setForm(List<List<Integer>> form) {
        this.form = form;
    }

    public int getFirstAwardpoint() {
        return firstAwardpoint;
    }

    public void setFirstAwardpoint(int firstAwardpoint) {
        this.firstAwardpoint = firstAwardpoint;
    }

    public int getWinAwardpoint() {
        return winAwardpoint;
    }

    public void setWinAwardpoint(int winAwardpoint) {
        this.winAwardpoint = winAwardpoint;
    }

    public List<List<Integer>> getFirstAward() {
        return firstAward;
    }

    public void setFirstAward(List<List<Integer>> firstAward) {
        this.firstAward = firstAward;
    }

    public List<List<Integer>> getWinAward() {
        return winAward;
    }

    public void setWinAward(List<List<Integer>> winAward) {
        this.winAward = winAward;
    }

}
