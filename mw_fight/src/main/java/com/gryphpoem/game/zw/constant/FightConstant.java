package com.gryphpoem.game.zw.constant;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.ObjectUtils;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-10-20 17:50
 */
public interface FightConstant {
    /**
     * 万分比除数
     */
    public static final double TEN_THOUSAND = 10000.0;
    /**
     * 攻击方
     */
    static final int[] ATK_SIZE = new int[]{1};
    /**
     * 防守方
     */
    static final int[] DEF_SIZE = new int[]{2};
    /**
     * 攻击防守全体
     */
    static final int[] ALL_SIZE = new int[]{1, 2};

    /**
     * 攻击方与防守方
     */
    interface ForceSide {
        int ATTACKER = 1;
        int DEFENDER = 2;
    }

    /**
     * 武将类型
     */
    interface HeroType {
        /**
         * 主将
         */
        int PRINCIPAL_HERO = 1;
        /**
         * 副将
         */
        int DEPUTY_HERO = 2;
    }

    /**
     * BUFF 生效时机
     */
    interface BuffEffectTiming {
        /**
         * 回合开始
         */
        int ROUND_START = 0;
        /**
         * 技能后
         */
        int SKILL_BEFORE = 11;
        /**
         * 技能后
         */
        int SKILL_AFTER = 12;
        /**
         * 普攻前
         */
        int BEFORE_GENERAL_ATTACK = 21;
        /**
         * 普攻后
         */
        int AFTER_GENERAL_ATTACK = 22;
        /**
         * 被普攻前
         */
        int BEFORE_BEING_ATTACKED = 31;
        /**
         * 被普攻后
         */
        int AFTER_BEING_ATTACKED = 32;
        /**
         * 受技能伤害前
         */
        int BEFORE_SKILL_DAMAGE = 41;
        /**
         * 受技能伤害后
         */
        int AFTER_SKILL_DAMAGE = 42;
        /**
         * 受击前
         */
        int BEFORE_BEING_HIT = 51;
        /**
         * 受击后
         */
        int AFTER_BEING_HIT = 52;
        /**
         * 掉血前
         */
        int BEFORE_BLEEDING = 61;
        /**
         * 掉血后
         */
        int AFTER_BLEEDING = 62;
        /**
         * 指定回合开始
         */
        int START_OF_DESIGNATED_ROUND = 102;
        /**
         * 血量低于百分比
         */
        int BLOOD_VOLUME_BELOW_PERCENTAGE = 103;
        /**
         * 存在指定BUFF_ID
         */
        int SPECIFIED_BUFF_ID_EXISTS = 104;
        /**
         * 指定BUFF_ID消失时
         */
        int SPECIFIED_BUFF_ID_DISAPPEARS = 105;
        /**
         * 指定BUFF_ID叠加到指定层数
         */
        int SPECIFY_BUFF_TO_STACK_TO_THE_SPECIFIED_LAYER_NUM = 106;
    }

    interface BuffEffectiveType {
        int ACTIVE = 1;
        int PASSIVE = 2;
        int CONDITION = 3;
    }

    interface BuffEffect {
        int BUFF = 1;
        int EFFECT = 2;
        int TIMING = 3;
    }

    /**
     * buff 作用目标
     */
    public enum BuffObjective {
        /**
         * buff挂载者
         */
        BUFF_LOADER(3, null),

        /**
         * 己方主将
         */
        MY_PRINCIPAL_HERO(11, ATK_SIZE),

        /**
         * 己方副将
         */
        MY_DEPUTY_HERO(12, ATK_SIZE),

        /**
         * 己方随机将领
         */
        RANDOM_MY_HERO(13, ATK_SIZE),

        /**
         * 己方所有将领
         */
        ALL_MY_HERO(14, ATK_SIZE),
        /**
         * 己方至少一将
         */
        AT_LEAST_ONE_HERO_FROM_MY_SIDE(15, ATK_SIZE),
        /**
         * 敌方主将
         */
        ENEMY_PRINCIPAL_HERO(21, DEF_SIZE),

        /**
         * 敌方副将
         */
        ENEMY_DEPUTY_HERO(22, DEF_SIZE),

        /**
         * 敌方随机将领
         */
        RANDOM_ENEMY_HERO(23, DEF_SIZE),

        /**
         * 敌方所有将领
         */
        ALL_ENEMY_HERO(24, DEF_SIZE),

        /**
         * 敌方至少一将
         */
        AT_LEAST_ONE_HERO_FROM_ENEMY_SIDE(25, DEF_SIZE),
        ;

        private int type;
        /**
         * 被作用方, 1是 攻击方, 2是被攻击方
         */
        private int[] affectedSize;

        public int getType() {
            return type;
        }

        BuffObjective(int type) {
            this.type = type;
        }

        BuffObjective(int type, int[] affectedSize) {
            this.type = type;
            this.affectedSize = affectedSize;
        }

        public static BuffObjective convertTo(int type) {
            for (BuffObjective tmp : BuffObjective.values()) {
                if (tmp.type == type) return tmp;
            }

            return null;
        }

        public Boolean isAttackerSize(int forceSize) {
            if (ObjectUtils.isEmpty(this.affectedSize))
                return null;
            if (ArrayUtils.contains(this.affectedSize, forceSize)) {
                return true;
            }
            return false;
        }
    }

    /**
     * 伤害来源类型
     */
    interface DamageType {
        int SKILL = 1;
        int ORIGINAL_ATTACK = 2;
        int OTHER_DAMAGE = 3;
    }

    interface ReplacementBuffRule {
        /**
         * 效果强度优先
         */
        int MORE_STRONG = 1;
        /**
         * 回合数更长
         */
        int LONGER_ROUNDS = 2;
    }
}
