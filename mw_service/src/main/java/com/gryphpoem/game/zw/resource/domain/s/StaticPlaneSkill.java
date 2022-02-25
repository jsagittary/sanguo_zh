package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.resource.constant.Constant;

/**
 * @author: ZhouJie
 * @date: Create in 2018-10-12 11:33
 * @description: 战机技能表
 * @modified By:
 */
public class StaticPlaneSkill {

    private int skillId;        // 技能ID

    /**
     * 技能类型
     * 1.无视防御的固定值伤害
     * 2.所属将领攻击力x%的伤害
     * 3.所属将领防御力x%的伤害
     * 4.所属将领兵力x%的伤害
     */
    private int type;

    /**
     * 与type对应
     * type填为1时，写固定值伤害
     * type填2，3，4时，填写万分比
     */
    private int skillEffect;

    /**
     * 获取技能效果
     * @return
     */
    public double getVal() {
        return type == 1 ? skillEffect : skillEffect / Constant.TEN_THROUSAND;
    }

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

    public int getSkillEffect() {
        return skillEffect;
    }

    public void setSkillEffect(int skillEffect) {
        this.skillEffect = skillEffect;
    }

}
