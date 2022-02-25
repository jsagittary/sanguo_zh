package com.gryphpoem.game.zw.resource.constant;

/**
 * 宝具常量
 */
public interface TreasureWareConst {


    //===============宝具类型======================
    /** 普通宝具*/
    public static final int ORDINARY_TREASURE_WARE = 1;
    /** 远古宝具*/
    public static final int ANCIENT_TREASURE_WARE = 2;

    //===============宝具上锁状态======================
    /** 宝具上锁*/
    public static final int TREASURE_WARE_LOCKED = 2;
    /** 普通未上锁*/
    public static final int TREASURE_WARE_UNLOCKED = 1;

    //===============宝具位置======================
    /** 宝具已获取，可使用*/
    public static final int TREASURE_IN_USING = 1;
    /** 宝具未获取，不可使用*/
    public static final int TREASURE_IN_MAIL = 2;
    /** 宝具已分解，不可使用*/
    public static final int TREASURE_HAS_DECOMPOSED = 3;

    //================宝具品质=====================
    public static final int BLUE_QUALITY = 3;//蓝色品质
    public static final int PURPLE_QUALITY = 4;//紫色色品质
    public static final int RED_QUALITY = 6;//红色品质
    public static final int ANCIENT_QUALITY = 7;//远古品质

    //=================宝具属性类型(决定宝具名称)=================
    /** 攻击类型*/
    public static final int ATTACK_TYPE = 1;
    /** 防御类型*/
    public static final int DEFENCE_TYPE = 2;
    /** 任意类型*/
    public static final int ANY_TYPE = 3;
    /** 超过该条数为何种类型*/
    public static final int OVER_ATTR_NUM = 2;

    //=================专属属性类型=================
    /**
     * 专属属性类型
     */
    interface SpecialType {
        /**
         * 采集类型
         */
        int COLLECT_TYPE = 1;

        /**
         * 禁卫军类型
         */
        int JANITOR_TYPE = 2;

        /**
         * 英雄类型
         */
        int HERO_TYPE = 3;

        /**
         * 赛季英雄
         */
        int SEASON_HERO = 4;

        /**
         * 增加属性
         */
        int ADD_ATTR = 5;

        /**
         * 采集类型中的特殊属性类型
         */
        interface CollectType {
            /**
             * 时长
             */
            int DURATION = 1;

            /**
             * 采矿速率
             */
            int SPEED = 2;
        }

        /**
         * 赛季英雄中的特殊类型
         */
        interface SeasonHero {
            /**
             * 技能释放概率
             */
            int SKILL_RELEASE_PROBABILITY = 1;

            /**
             * 技能伤害提升
             */
            int SKILL_DAMAGE_INCREASED = 2;

            /**
             * buff伤害提升
             */
            int BUFF_DAMAGE_INCREASED = 3;

            /**
             * buff作用兵排提升
             */
            int BUFF_EFFECT_ARMY_ROWS = 4;

            /**
             * 无实际意义，将专属属性填充到英雄中
             */
            int ANY_SUB_TYPE = 100;
        }
    }

    //============================穿戴状态==========================
    /** 宝具穿戴：穿上 */
    public static final int TREASURE_WARE_ON = 1;
    /** 宝具穿戴：卸下 */
    public static final int TREASURE_WARE_DOWN = 0;
    /** 宝具穿戴：替换 */
    public static final int TREASURE_WARE_REPLACE = 2;

}
