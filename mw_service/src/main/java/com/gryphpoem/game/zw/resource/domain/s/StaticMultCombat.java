package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticMultCombat.java
 * @Description 多人关卡
 * @author QiuKun
 * @date 2018年12月26日
 */
public class StaticMultCombat {

    private int combatId;
    private int preId; // 前置id
    private int fightpower;// 推荐战力
    private int gearCount;// 该关卡产出的装备数量
    private List<List<Integer>> firstPassAward; // 首通奖励
    private int firstPassPoint; // 首通奖励点数
    private List<List<Integer>> firstPassRand; // 首通装备
    private List<List<Integer>> passAward;// 通关奖励
    private int passPoint;// 通关点数
    private List<List<Integer>> passRand;// 通关装备

    private List<List<Integer>> teamAward; // 合作奖励
    private List<Integer> form;// 阵型

    public int getCombatId() {
        return combatId;
    }

    public void setCombatId(int combatId) {
        this.combatId = combatId;
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

    public List<List<Integer>> getFirstPassAward() {
        return firstPassAward;
    }

    public void setFirstPassAward(List<List<Integer>> firstPassAward) {
        this.firstPassAward = firstPassAward;
    }

    public List<List<Integer>> getPassAward() {
        return passAward;
    }

    public void setPassAward(List<List<Integer>> passAward) {
        this.passAward = passAward;
    }

    public List<List<Integer>> getTeamAward() {
        return teamAward;
    }

    public void setTeamAward(List<List<Integer>> teamAward) {
        this.teamAward = teamAward;
    }

    public List<Integer> getForm() {
        return form;
    }

    public void setForm(List<Integer> form) {
        this.form = form;
    }

    public int getFirstPassPoint() {
        return firstPassPoint;
    }

    public void setFirstPassPoint(int firstPassPoint) {
        this.firstPassPoint = firstPassPoint;
    }

    public List<List<Integer>> getFirstPassRand() {
        return firstPassRand;
    }

    public void setFirstPassRand(List<List<Integer>> firstPassRand) {
        this.firstPassRand = firstPassRand;
    }

    public int getPassPoint() {
        return passPoint;
    }

    public void setPassPoint(int passPoint) {
        this.passPoint = passPoint;
    }

    public List<List<Integer>> getPassRand() {
        return passRand;
    }

    public void setPassRand(List<List<Integer>> passRand) {
        this.passRand = passRand;
    }

}
