package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.cross.constants.FightCommonConstant;

import java.util.List;
import java.util.Map;

/**
 * @author TanDonghai
 * @ClassName StaticNpc.java
 * @Description NPC配置信息
 * @date 创建时间：2017年4月1日 下午2:03:14
 */
public class StaticNpc {
    private int npcId;
    private int exp;// 击败该NPC可以获得的最高经验
    private int lv;// 等级
    private int quality;// 品质
    private int line;// 兵力排数
    private int armType;// 兵种类型
    private int armLv;// 兵种品质、等级
    private Map<Integer, Integer> attr;// 属性，格式：[[attrId,value]...]
    /**
     * 登场技能
     */
    private List<Integer> onStageSkills;
    /**
     * 主动技能
     */
    private List<Integer> activeSkills;
    /**
     * 每回合充能属性
     */
    private List<List<Integer>> chargeEveryRound;
    /**
     * 技能等级
     */
    private int skillLv;

    private int totalArm = -1;

    public int getNpcId() {
        return npcId;
    }

    public void setNpcId(int npcId) {
        this.npcId = npcId;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getArmType() {
        return armType;
    }

    public void setArmType(int armType) {
        this.armType = armType;
    }

    public int getArmLv() {
        return armLv;
    }

    public void setArmLv(int armLv) {
        this.armLv = armLv;
    }

    public Map<Integer, Integer> getAttr() {
        return attr;
    }

    public void setAttr(Map<Integer, Integer> attr) {
        this.attr = attr;
    }

    public int getTotalArm() {
        if (totalArm < 0) {
            Integer count = getAttr().get(FightCommonConstant.AttrId.LEAD);
            totalArm = null == count ? 0 : count;
        }
        return totalArm;
    }

    public int getSpeed() {
        return getAttr().getOrDefault(FightCommonConstant.AttrId.SPEED, 0);
    }

    public List<Integer> getOnStageSkills() {
        return onStageSkills;
    }

    public void setOnStageSkills(List<Integer> onStageSkills) {
        this.onStageSkills = onStageSkills;
    }

    public List<Integer> getActiveSkills() {
        return activeSkills;
    }

    public void setActiveSkills(List<Integer> activeSkills) {
        this.activeSkills = activeSkills;
    }

    public List<List<Integer>> getChargeEveryRound() {
        return chargeEveryRound;
    }

    public void setChargeEveryRound(List<List<Integer>> chargeEveryRound) {
        this.chargeEveryRound = chargeEveryRound;
    }

    public int getSkillLv() {
        return skillLv;
    }

    public void setSkillLv(int skillLv) {
        this.skillLv = skillLv;
    }

    @Override
    public String toString() {
        return "StaticNpc [npcId=" + npcId + ", exp=" + exp + ", lv=" + lv + ", quality=" + quality + ", line=" + line
                + ", armType=" + armType + ", armLv=" + armLv + ", attr=" + attr + "]";
    }
}
