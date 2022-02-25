package com.gryphpoem.game.zw.resource.domain.s;

/**
 * @author: ZhouJie
 * @date: Create in 2018-12-28 14:27
 * @description: 战斗buff配置
 * @modified By:
 */
public class StaticFightBuff {

    private int buffId;             // key
    /**
     * buff类型
     * 1.机身固化     触发时可免除对方X次的攻击  不论对方攻击伤害如何
     * 2.防弹涂层     生成护盾，吸收伤害，可抵消XXX次伤害  护盾未破之前，兵力将不会损失
     * 3.紧急装置     受到致命伤害时，避免致死
     * 4.燃烧弹       易伤buff，受到的伤害添加XX%
     * 5.火力核心     必定暴击
     * 6.制导改良     必定命中
     */
    private int buffType;
    private int buffVal;             // buff的数值
    private int times;               // 作用回合数
    private int row;                 // 作用兵排数
    private int object;              // 0-对自己起效  1-对敌人起效

    public int getBuffId() {
        return buffId;
    }

    public void setBuffId(int buffId) {
        this.buffId = buffId;
    }

    public int getBuffType() {
        return buffType;
    }

    public void setBuffType(int buffType) {
        this.buffType = buffType;
    }

    public int getBuffVal() {
        return buffVal;
    }

    public void setBuffVal(int buffVal) {
        this.buffVal = buffVal;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getObject() {
        return object;
    }

    public void setObject(int object) {
        this.object = object;
    }
}
