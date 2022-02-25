package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @author: ZhouJie
 * @date: Create in 2018-12-28 14:27
 * @description: 战斗技能
 * @modified By:
 */
public class StaticFightSkill {

    private int skillId;                        // 技能ID，唯一Key值
    private int type;                           // 1 将领技能, 2 战机入场技能, 3 战机专业技能
    private int order;                          // 在战斗中的技能释放先后顺序，t同样的type值，需要保持一致
    private int probability;                    // 技能触发概率(万分比)
    private int value;                          // 技能伤害值(具体值)
    private int fade;                           // 衰减值，填写万份比，总是和上一次的值作比较,如上一次伤害为100，衰减值填2000，则此次伤害为80
    private List<List<Integer>> attackTime;     // [[攻击次数值1,权重值1],....]'
    private int buffId;                         // 若此技能有buff效果，则链接到 s_fight_buff表，填写表中的ID

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

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getProbability() {
        return probability;
    }

    public void setProbability(int probability) {
        this.probability = probability;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getFade() {
        return fade;
    }

    public void setFade(int fade) {
        this.fade = fade;
    }

    public List<List<Integer>> getAttackTime() {
        return attackTime;
    }

    public void setAttackTime(List<List<Integer>> attackTime) {
        this.attackTime = attackTime;
    }

    public int getBuffId() {
        return buffId;
    }

    public void setBuffId(int buffId) {
        this.buffId = buffId;
    }
}
