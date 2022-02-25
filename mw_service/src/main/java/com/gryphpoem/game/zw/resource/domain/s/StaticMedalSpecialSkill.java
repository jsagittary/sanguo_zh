package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @author chenqi
 * @ClassName: MedalSpecialSkill
 * @Description: 勋章-特殊技能  配置
 * @date 2018年9月11日
 */
public class StaticMedalSpecialSkill {

    /**
     * 特殊技能id
     */
    private Integer specialSkillId;

    /**
     * 技能品质
     */
    private Integer quality;

    /**
     * 技能等级
     */
    private Integer level;

    /**
     * 加成的属性id
     */
    private Integer attrId;

    /**
     * 属性加成值
     */
    private Integer attrEffect;

    /**
     * 技能效果值
     */
    private List<Integer> skillEffect;

    /**
     * 技能生效所需激活的特技
     */
    private int matchSkill;

    public Integer getSpecialSkillId() {
        return specialSkillId;
    }

    public void setSpecialSkillId(Integer specialSkillId) {
        this.specialSkillId = specialSkillId;
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

    public Integer getAttrId() {
        return attrId;
    }

    public void setAttrId(Integer attrId) {
        this.attrId = attrId;
    }

    public Integer getAttrEffect() {
        return attrEffect;
    }

    public void setAttrEffect(Integer attrEffect) {
        this.attrEffect = attrEffect;
    }

    public List<Integer> getSkillEffect() {
        return skillEffect;
    }

    public void setSkillEffect(List<Integer> skillEffect) {
        this.skillEffect = skillEffect;
    }

    public int getMatchSkill() {
        return matchSkill;
    }

    public void setMatchSkill(int matchSkill) {
        this.matchSkill = matchSkill;
    }
}
