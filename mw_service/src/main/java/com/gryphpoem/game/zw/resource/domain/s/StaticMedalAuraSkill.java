package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @author chenqi
 * @ClassName: MedalAuraSkill
 * @Description: 勋章-光环技能 配置
 * @date 2018年9月11日
 */
public class StaticMedalAuraSkill {

    /**
     * 光环技能id
     */
    private Integer auraSkillId;

    /**
     * 技能品质
     */
    private Integer quality;

    /**
     * 技能等级
     */
    private Integer level;

    /**
     * 作用于兵种的类型
     */
    private Integer armType;

    /**
     * 属性加成效果值[[属性id,加成值],..]
     */
    private List<List<Integer>> attrEffect;

    /**
     * 技能效果值
     */
    private Integer skillEffect;

    /**
     * 技能生效所需激活的特技
     */
    private List<Integer> matchSkill;

    /**
     * 橙色勋章激活红色光环技能
     */
    private int activeSkill;

    public int getActiveSkill() {
        return activeSkill;
    }

    public void setActiveSkill(int activeSkill) {
        this.activeSkill = activeSkill;
    }

    public List<Integer> getMatchSkill() {
        return matchSkill;
    }

    public void setMatchSkill(List<Integer> matchSkill) {
        this.matchSkill = matchSkill;
    }

    public Integer getAuraSkillId() {
        return auraSkillId;
    }

    public void setAuraSkillId(Integer auraSkillId) {
        this.auraSkillId = auraSkillId;
    }

    public Integer getQuality() {
        return quality;
    }

    public void setQuality(Integer quality) {
        this.quality = quality;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getArmType() {
        return armType;
    }

    public void setArmType(Integer armType) {
        this.armType = armType;
    }

    public List<List<Integer>> getAttrEffect() {
        return attrEffect;
    }

    public void setAttrEffect(List<List<Integer>> attrEffect) {
        this.attrEffect = attrEffect;
    }

    public Integer getSkillEffect() {
        return skillEffect;
    }

    public void setSkillEffect(Integer skillEffect) {
        this.skillEffect = skillEffect;
    }

}