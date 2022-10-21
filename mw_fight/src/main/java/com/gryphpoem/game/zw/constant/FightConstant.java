package com.gryphpoem.game.zw.constant;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-10-20 17:50
 */
public interface FightConstant {
    /**
     * BUFF 生效时机
     */
    interface BuffEffectTiming {
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
        MY_PRINCIPAL_HERO(11),
        MY_DEPUTY_HERO(12),
        RANDOM_MY_HERO(13),
        ALL_MY_HERO(14),
        ENEMY_PRINCIPAL_HERO(21),
        ENEMY_DEPUTY_HERO(22),
        RANDOM_ENEMY_HERO(23),
        ALL_ENEMY_HERO(24),
        ;

        private int type;

        public int getType() {
            return type;
        }

        BuffObjective(int type) {
            this.type = type;
        }
    }
}
