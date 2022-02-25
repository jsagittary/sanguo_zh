package com.gryphpoem.game.zw.resource.domain.s;

/**
 * @ClassName StaticSkill.java
 * @Description 技能表
 * @author QiuKun
 * @date 2018年3月21日
 */
public class StaticSkill {
    private int skillId;
    private int type;// 兵种类型，1.战车（步兵） 2.坦克（骑兵） 3.火箭（弓兵）
    private int val;// 技能系数

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return "StaticSkill [skillId=" + skillId + ", type=" + type + ", val=" + val + "]";
    }

}
