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
     * 百分比除数
     */
    public static final double HUNDRED = 100.0;
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
     * 战斗结果：平局或其他
     */
    public static final int FIGHT_RESULT_DRAW = 0;
    /**
     * 战斗结果：胜利
     */
    public static final int FIGHT_RESULT_SUCCESS = 1;
    /**
     * 战斗结果：失败
     */
    public static final int FIGHT_RESULT_FAIL = 2;

    /**
     * 将领属性：攻击
     */
    public static final int ATTR_ATTACK = 1;
    /**
     * 将领属性：防御
     */
    public static final int ATTR_DEFEND = 2;
    /**
     * 将领属性：单排兵力
     */
    public static final int ATTR_LEAD = 3;

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
         * 主动释放
         */
        int ACTIVE_RELEASE = -2;
        /**
         * 被动释放
         */
        int PASSIVE_RELEASE = -1;
        /**
         * 回合开始
         */
        int ROUND_START = 0;
        /**
         * 技能前
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
        /**
         * 指定技能组施放后
         */
        int AFTER_CASTING_THE_SPECIFIED_SKILL_GROUP = 107;
        /**
         * 存在BUFF分组
         */
        int BUFF_GROUP_EXISTS = 108;
        /**
         * 存在一定数量BUFF分组
         */
        int BUFF_GROUP_NUM_EXISTS = 109;
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
         * 任何人
         */
        ANYONE(0, null),
        /**
         * 技能释放者
         */
        RELEASE_SKILL(1, null),
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
    interface EffectStatus {
        int DISAPPEAR = 0;
        int APPEAR = 1;
    }

    interface ValueType {
        int RATIO = 1;      // 万分比
        int FIX_VALUE = 2;  // 固定值
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

    /**
     * 效果逻辑id
     */
    interface EffectLogicId {
        /**
         * 技能伤害
         */
        int SKILL_DAMAGE = 3;
        /**
         * 能量恢复
         */
        int ENERGY_RECOVERY = 4;
        /**
         * 能量扣除
         */
        int ENERGY_DEDUCTION = 5;
        /**
         * 士气恢复
         */
        int MORALE_RECOVERY = 6;
        /**
         * 士气扣除
         */
        int MORALE_DEDUCTION = 7;
        /**
         * 护盾
         */
        int SHIELD = 8;
        /**
         * 连击
         */
        int DOUBLE_HIT = 21;
        /**
         * 无视所有伤害
         */
        int INVINCIBLE_DAMAGE = 22;
        /**
         * 沉默
         */
        int SILENCE = 32;
        /**
         * 净化buff
         */
        int PURIFY = 33;
        /**
         * 驱散buff
         */
        int DISPEL = 34;
        /**
         * 兵种克制时伤害提升
         */
        int DAMAGE_INCREASED_FIGHTING = 45;
        /**
         * 兵种被克制时受伤降低
         */
        int DAMAGE_REDUCED_ARM_RESTRAINED = 46;

        /**
         * 攻击提升
         */
        int ATTACK_INCREASED = 51;
        /**
         * 攻击降低
         */
        int REDUCED_ATTACK = 52;
        /**
         * 防御提升
         */
        int DEFENSE_INCREASED = 53;
        /**
         * 防御降低
         */
        int REDUCED_DEFENSE = 54;
        /**
         * 破城提升
         */
        int BROKEN_CITY_PROMOTION = 55;
        int BROKEN_CITY_REDUCED = 56;
        /**
         * 守城提升
         */
        int UPWARD_GUARDING = 57;
        int GUARD_CITY_REDUCED = 58;
        /**
         * 破甲提升
         */
        int ARMOR_PIERCING_ENHANCEMENT = 59;
        int ARMOR_PIERCING_REDUCTION = 60;
        /**
         * 防护提升
         */
        int PROTECTION_LIFTING = 61;
        int REDUCED_PROTECTION = 62;
        /**
         * 速度提升
         */
        int SPEED_INCREASE = 63;
        int SPEED_REDUCTION = 64;
        /**
         * 能量恢复值提升
         */
        int ENERGY_RECOVERY_VALUE_INCREASED = 65;
        /**
         * 能量恢复值降低
         */
        int ENERGY_RECOVERY_VALUE_DECREASES = 66;
        /**
         * 士气恢复值提升
         */
        int MORALE_RECOVERY_VALUE_INCREASED = 67;
        /**
         * 士气恢复值降低
         */
        int MORALE_RECOVERY_VALUE_REDUCED = 68;
        /**
         * 士气扣除值提升
         */
        int MORALE_DEDUCTION_VALUE_INCREASED = 69;
        /**
         * 士气扣除值降低
         */
        int REDUCED_MORALE_DEDUCTION = 70;
        /**
         * 普攻伤害提升
         */
        int INCREASE_COMMON_ATTACK_DAMAGE = 71;
        /**
         * 普攻伤害降低
         */
        int COMMON_ATTACK_DAMAGE_REDUCED = 72;
        /**
         * 技能伤害提升
         */
        int SKILL_DAMAGE_INCREASED = 73;
        /**
         * 技能伤害降低
         */
        int SKILL_DAMAGE_REDUCED = 74;
        /**
         * 最终伤害提升
         */
        int INCREASE_FINAL_DAMAGE = 75;
        /**
         * 最终伤害降低
         */
        int FINAL_DAMAGE_REDUCED = 76;
        /**
         * 受普攻伤害提升
         */
        int Increased_damage_general_attack = 77;
        /**
         * 受普攻伤害降低
         */
        int BE_COMMON_ATTACK_DAMAGE_REDUCED = 78;
        /**
         * 受技能伤害提升
         */
        int BE_INCREASED_SKILL_DAMAGE = 79;
        /**
         * 受技能伤害降低
         */
        int BE_SKILL_DAMAGE_REDUCED = 80;
        /**
         * 受最终伤害提升
         */
        int BE_INCREASE_FINAL_DAMAGE = 81;
        /**
         * 受最终伤害降低
         */
        int BE_FINAL_DAMAGE_REDUCED = 82;
        /**
         * 受技能伤害提升
         */
        int BE_SKILL_DAMAGE_INCREASED = 83;
        /**
         * 受技能伤害降低
         */
        int BE_SKILL_DAMAGE_REDUCED_DECREASED = 84;
        /**
         * 受最终伤害提升
         */
        int BE_INCREASE_FINAL_DAMAGE_PROMOTION = 85;
        /**
         * 受最终伤害降低
         */
        int FINAL_DAMAGE_REDUCED_DECREASED = 86;

        /**
         * 效果内加buff
         */
        int ADD_BUFF_TO_THE_EFFECT = 99;
        /**
         * 暴击率提升万分比
         */
        int INCREASE_CRITICAL_HIT_RATE = 102;
        /**
         * 暴击伤害提升
         */
        int CRITICAL_DAMAGE_INCREASED = 103;
        /**
         * 技能额外暴击率提升万分比
         */
        int INCREASED_EXTRA_CRITICAL_HIT_RATE_OF_SKILL = 104;
        /**
         * 技能额外暴击伤害提升万分比
         */
        int INCREASED_EXTRA_CRITICAL_DAMAGE_OF_SKILL = 105;
    }
}
