package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @author: ZhouJie
 * @date: Create in 2019-03-21 14:31
 * @description: 戒指强化配置
 * @modified By:
 */
public class StaticRingStrengthen {
    /**
     * 强化等级
     */
    private int level;
    /**
     * 强满当前级别需的成长值
     */
    private int exp;
    /**
     * 每次强化添加的成长值
     */
    private int expUp;
    /**
     * 后端真实基础强化概率,万份比
     */
    private int realBaseProbability;
    /**
     * [强化次数，对应增加的强化成功率数值（万份比）]
     * 达到指定强化次数后，将添加成功率
     */
    private List<Integer> realUpProbability;
    /**
     * 资质对应的额外添加的属性值
     * [[属性类型1，属性值1],[属性类型2，属性值2]..]
     */
    private List<List<Integer>> upAttr;
    /**
     * 强化的物品消耗
     */
    private List<Integer> consume;
    /**
     * 强化次数给与的额外属性
     * [次数，额外属性类型，额外属性值]
     */
    private List<List<Integer>> exAttr;
    /**
     * 当前属性值
     * [[属性类型1，属性值1],[属性类型2，属性值2]..]
     */
    private List<List<Integer>> attr;
    /**
     * 对应装备id
     */
    private int equipId;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getExpUp() {
        return expUp;
    }

    public void setExpUp(int expUp) {
        this.expUp = expUp;
    }

    public int getRealBaseProbability() {
        return realBaseProbability;
    }

    public void setRealBaseProbability(int realBaseProbability) {
        this.realBaseProbability = realBaseProbability;
    }

    public List<Integer> getRealUpProbability() {
        return realUpProbability;
    }

    public void setRealUpProbability(List<Integer> realUpProbability) {
        this.realUpProbability = realUpProbability;
    }

    public List<List<Integer>> getUpAttr() {
        return upAttr;
    }

    public void setUpAttr(List<List<Integer>> upAttr) {
        this.upAttr = upAttr;
    }

    public List<Integer> getConsume() {
        return consume;
    }

    public void setConsume(List<Integer> consume) {
        this.consume = consume;
    }

    public List<List<Integer>> getExAttr() {
        return exAttr;
    }

    public void setExAttr(List<List<Integer>> exAttr) {
        this.exAttr = exAttr;
    }

    public List<List<Integer>> getAttr() {
        return attr;
    }

    public void setAttr(List<List<Integer>> attr) {
        this.attr = attr;
    }

    public int getEquipId() {
        return equipId;
    }

    public void setEquipId(int equipId) {
        this.equipId = equipId;
    }
}
