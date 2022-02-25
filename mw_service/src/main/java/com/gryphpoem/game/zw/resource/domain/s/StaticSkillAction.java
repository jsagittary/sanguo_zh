package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-04-16 16:48
 */
public class StaticSkillAction {
    //技能唯一ID
    private int id;
    //技能基础
    private int baseSkill;
    //释放技能消耗
    private List<List<Integer>> releaseCost;
    //技能释放优先级
    private int priority;
    //触发条件, 0-概率触发
    private int triggerCond;
    //触发参数
    private List<List<Integer>> triggerParam;
    //技能释放目标
    private int target;
    //目标参数
    private List<List<Integer>> targetParam;
    //技能效果
    private int effect;
    //技能效果参数
    private List<List<Integer>> effectParam;
    //技能BUFF
    private List<Integer> buffs;
    //技能战力
    private int fightVal;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBaseSkill() {
        return baseSkill;
    }

    public void setBaseSkill(int baseSkill) {
        this.baseSkill = baseSkill;
    }

    public List<List<Integer>> getReleaseCost() {
        return releaseCost;
    }

    public void setReleaseCost(List<List<Integer>> releaseCost) {
        this.releaseCost = releaseCost;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getTriggerCond() {
        return triggerCond;
    }

    public void setTriggerCond(int triggerCond) {
        this.triggerCond = triggerCond;
    }

    public List<List<Integer>> getTriggerParam() {
        return triggerParam;
    }

    public void setTriggerParam(List<List<Integer>> triggerParam) {
        this.triggerParam = triggerParam;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public List<List<Integer>> getTargetParam() {
        return targetParam;
    }

    public void setTargetParam(List<List<Integer>> targetParam) {
        this.targetParam = targetParam;
    }

    public int getEffect() {
        return effect;
    }

    public void setEffect(int effect) {
        this.effect = effect;
    }

    public List<List<Integer>> getEffectParam() {
        return effectParam;
    }

    public void setEffectParam(List<List<Integer>> effectParam) {
        this.effectParam = effectParam;
    }

    public List<Integer> getBuffs() {
        return buffs;
    }

    public void setBuffs(List<Integer> buffs) {
        this.buffs = buffs;
    }

    public int getFightVal() {
        return fightVal;
    }

    public void setFightVal(int fightVal) {
        this.fightVal = fightVal;
    }
}
