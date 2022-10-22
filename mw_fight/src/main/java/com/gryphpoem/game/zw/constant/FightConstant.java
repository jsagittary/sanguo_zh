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
        int ROUND_START = 0;
        int SKILL_BEFORE = 1;
    }

    interface BuffEffectiveType {
        int ACTIVE = 1;
    }

    interface BuffEffect {
        int BUFF = 1;
        int EFFECT = 2;
    }

    /**
     * buff 作用目标
     */
    public enum BuffObjective {
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
}
