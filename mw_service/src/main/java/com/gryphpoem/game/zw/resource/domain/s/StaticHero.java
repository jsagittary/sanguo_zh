package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;
import java.util.Map;

/**
 * @ClassName StaticHero.java
 * @Description 武将基础信息配置
 * @author TanDonghai
 * @date 创建时间：2017年3月11日 下午4:23:57
 *
 */
public class StaticHero {
    private int heroId;//
    private int heroType;
    private int state;// 国家
    private int season;//赛季英雄(赛季ID)
    private int quality;// 品质，参照s_quality表
    private int type;// 兵种类型，1.步兵 2.骑兵 3.弓兵
    private int base;// 初始基础资质
    private int baseAdd;// 初始基础增量
    private int attack;// 初始攻资质
    private int defend;// 初始防资质
    private int lead;// 初始兵资质
    private int totalMax;// 总资质增量上限
    private int attackMax;// 攻资质上限
    private int defendMax;// 防资质上限
    private int leadMax;// 兵资质上限
    private int attackMin;// 攻资质下限
    private int defendMin;// 防资质下限
    private int leadMin;// 兵资质下限
    private int line;// 兵力初始排数
    private Map<Integer, Integer> attr;// 武将属性 [[id,value]]
    private Map<Integer, Integer> growth;// 属性成长值 [[id,value]]
    private Map<Integer, Integer> radix;// 品质影响基数 [[id,value]]，value为万分比
    private Map<Integer, Integer> ratio;// 资质影响系数 [[id,value]]，value为万分比
    private int nextId;// 突破后ID
    private int collect;// 采集时长，单位：秒
    private int skillId;// 技能ID
    private List<List<Integer>> activateConsume;// 激活进化组的消耗
    private Map<Integer, Integer> activateAttr;// 激活加成属性
    private List<Integer> evolveGroup;// 技能组，链接到 hero_evolve表格中的group字段
    private List<List<Integer>> recombination;// 重组的消耗
    /** 合成英雄需要的碎片数量*/
    private int chips;

    public int getHeroType() {
        return heroType;
    }

    public void setHeroType(int heroType) {
        this.heroType = heroType;
    }

    public int getHeroId() {
        return heroId;
    }

    public void setHeroId(int heroId) {
        this.heroId = heroId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getBase() {
        return base;
    }

    public void setBase(int base) {
        this.base = base;
    }

    public int getBaseAdd() {
        return baseAdd;
    }

    public void setBaseAdd(int baseAdd) {
        this.baseAdd = baseAdd;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefend() {
        return defend;
    }

    public void setDefend(int defend) {
        this.defend = defend;
    }

    public int getLead() {
        return lead;
    }

    public void setLead(int lead) {
        this.lead = lead;
    }

    public int getTotalMax() {
        return totalMax;
    }

    public void setTotalMax(int totalMax) {
        this.totalMax = totalMax;
    }

    public int getAttackMax() {
        return attackMax;
    }

    public void setAttackMax(int attackMax) {
        this.attackMax = attackMax;
    }

    public int getDefendMax() {
        return defendMax;
    }

    public void setDefendMax(int defendMax) {
        this.defendMax = defendMax;
    }

    public int getLeadMax() {
        return leadMax;
    }

    public void setLeadMax(int leadMax) {
        this.leadMax = leadMax;
    }

    public int getAttackMin() {
        return attackMin;
    }

    public void setAttackMin(int attackMin) {
        this.attackMin = attackMin;
    }

    public int getDefendMin() {
        return defendMin;
    }

    public void setDefendMin(int defendMin) {
        this.defendMin = defendMin;
    }

    public int getLeadMin() {
        return leadMin;
    }

    public void setLeadMin(int leadMin) {
        this.leadMin = leadMin;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public Map<Integer, Integer> getAttr() {
        return attr;
    }

    public void setAttr(Map<Integer, Integer> attr) {
        this.attr = attr;
    }

    public Map<Integer, Integer> getGrowth() {
        return growth;
    }

    public void setGrowth(Map<Integer, Integer> growth) {
        this.growth = growth;
    }

    public Map<Integer, Integer> getRadix() {
        return radix;
    }

    public void setRadix(Map<Integer, Integer> radix) {
        this.radix = radix;
    }

    public Map<Integer, Integer> getRatio() {
        return ratio;
    }

    public void setRatio(Map<Integer, Integer> ratio) {
        this.ratio = ratio;
    }

    public int getNextId() {
        return nextId;
    }

    public void setNextId(int nextId) {
        this.nextId = nextId;
    }

    public int getCollect() {
        return collect;
    }

    public void setCollect(int collect) {
        this.collect = collect;
    }

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

    public List<List<Integer>> getActivateConsume() {
        return activateConsume;
    }

    public void setActivateConsume(List<List<Integer>> activateConsume) {
        this.activateConsume = activateConsume;
    }

    public Map<Integer, Integer> getActivateAttr() {
        return activateAttr;
    }

    public void setActivateAttr(Map<Integer, Integer> activateAttr) {
        this.activateAttr = activateAttr;
    }

    public List<List<Integer>> getRecombination() {
        return recombination;
    }

    public void setRecombination(List<List<Integer>> recombination) {
        this.recombination = recombination;
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public boolean isSeasonHero() {
        return season > 0;
    }

    public List<Integer> getEvolveGroup() {
        return evolveGroup;
    }

    public void setEvolveGroup(List<Integer> evolveGroup) {
        this.evolveGroup = evolveGroup;
    }

    public int getChips() {
        return chips;
    }

    public void setChips(int chips) {
        this.chips = chips;
    }

    /**
     * 根据属性id获取将领的基础属性值
     * 
     * @param attrId
     * @return
     */
    public int getBaseAttrById(int attrId) {
        Integer value = getAttr().get(attrId);
        return null == value ? 0 : value;
    }

    /**
     * 根据属性id获取将领的资质影响系数（万分比）
     * 
     * @param attrId
     * @return
     */
    public int getAttrRatioById(int attrId) {
        Integer value = getRatio().get(attrId);
        return null == value ? 0 : value;
    }

    /**
     * 根据属性id获取品质影响基数（万分比）
     * 
     * @param attrId
     * @return
     */
    public int getAttrRadixById(int attrId) {
        Integer value = getRadix().get(attrId);
        return null == value ? 0 : value;
    }

    /**
     * 根据属性id获取将领的成长值
     * 
     * @param attrId
     * @return
     */
    public int getAttrGrowthById(int attrId) {
        Integer value = getGrowth().get(attrId);
        return null == value ? 0 : value;
    }
}
