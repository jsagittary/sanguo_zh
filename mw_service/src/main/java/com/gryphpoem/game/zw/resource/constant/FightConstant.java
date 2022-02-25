package com.gryphpoem.game.zw.resource.constant;

/**
 * @author: ZhouJie
 * @date: Create in 2018-12-28 18:56
 * @description: 战斗常量
 * @modified By:
 */
public class FightConstant {

    public interface TriggerCond{
        int TriggerCond_0 = 0;//必定触发
        int TriggerCond_1 = 1;//概率触发
    }

    public interface TargetSelect{
        int TARGET_SELECT_0 = 0;//当前回合的敌方战斗部队
    }

    public interface Effect{
        int EFFECT_101 = 101;//对当前敌方兵排造成单次伤害
        int EFFECT_102 = 102;//对敌方当前兵排造成多段伤害
        int EFFECT_103 = 103;//对敌方多兵排造成多段伤害
        int EFFECT_104 = 104;//暴击buff bufftype=5
    }

    /** 战斗buff类型 */
    public interface BuffType {
        /** 免除对方X次的攻击 */
        int BUFF_TYPE_DEF_CNT = 1;
        /** 可抵消XXX次伤害  护盾未破之前，兵力将不会损失 */
        int BUFF_TYPE_DEF_HURT = 2;
        /** 受到致命伤害时，避免致死 */
        int BUFF_TYPE_NOT_DEAD = 3;
        /** 易伤buff，受到的伤害添加XX% */
        int BUFF_TYPE_UP_HURT = 4;
        /** 必定暴击 */
        int BUFF_TYPE_CRIT = 5;
        /** 必定命中 */
        int BUFF_TYPE_HIT = 6;
    }

    /**
     * buff的param
     */
    public interface BuffParam {
        /** 记录重置时回合数 */
        int CONTINUE_NUM = 1;
        /** 记录重置时buff效果 */
        int BUFF_VAL = 2;
    }

}
