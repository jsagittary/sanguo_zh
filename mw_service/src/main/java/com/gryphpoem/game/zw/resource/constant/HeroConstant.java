package com.gryphpoem.game.zw.resource.constant;

import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.SystemTabLoader;

import java.util.List;
import java.util.Map;

/**
 * @ClassName HeroConstant.java
 * @Description 将领常量配置类
 * @author TanDonghai
 * @date 创建时间：2017年3月27日 下午3:45:08
 *
 */
public class HeroConstant {

    /** 将领装备栏总数 */
    public static int HERO_EQUIP_NUM;

    /** 将领上阵队列长度 */
    public static int HERO_BATTLE_LEN;

    /** 良将寻访功能开启，玩家等级限制 */
    public static int HERO_SEARCH_ROLE_LV;

    /** 良将寻访，金币消耗 */
    public static List<Integer> NORMAL_HERO_GOLD;

    /** 神将寻访，金币消耗 */
    public static List<Integer> SUPER_HERO_GOLD;

    /** 良将转为将令数 */
    public static int NORMAL_HERO_TOKEN;

    /** 神将转为将令数 */
    public static int SUPER_HERO_TOKEN;

    /** 神将寻访 第5次必出其中一个将领配置 */
    public static List<List<Integer>> SEARCH_SUPER_HERO_FOR_FIVE;
    /** 达到某次数，必出某神将的配置 */
    public static Map<Integer, Integer> SEARCH_SUPER_HERO_SPECIAL;

    /** 喀秋莎 将领id */
    public static int GQS_HERO_ID;

    /** 将领可装战机总数 */
    public static int HERO_WAR_PLANE_NUM;
    /** 将领可装战机解锁条件 */
    public static List<List<Integer>> HERO_WAR_PLANE_UNLOCK;

    /**
     * 英雄进化后重组的返还比例
     */
    public static int HERO_REGROUP_AWARD_NUM;

    /**
     * 心愿英雄获取次数
     */
    public static int WISH_HERO_COUNT;


    /**
     * 寻访免费次数间隔时间（秒）
     */
    public static int DRAW_HERO_CARD_FREE_TIMES_TIME_INTERVAL;

    /**
     * 常驻寻访金币消耗
     */
    public static List<Integer> PERMANENT_QUEST_GOLD_CONSUMPTION;

    /**
     * 限时寻访金币消耗
     */
    public static List<Integer> TIME_LIMITED_SEARCH_FOR_GOLD_COIN_CONSUMPTION;
    /**
     * 寻访重复武将转化碎片
     */
    public static int DRAW_DUPLICATE_HERO_TO_TRANSFORM_FRAGMENTS;

    /**
     * 寻访橙色武将碎片保底次数
     */
    public static int DRAW_ORANGE_HERO_FRAGMENT_GUARANTEED_TIMES;

    /**
     * 寻访橙色武将保底次数
     */
    public static int DRAW_MINIMUM_NUMBER_OF_ORANGE_HERO;
    /**
     * 第一次抽卡必出奖励
     */
    public static int FIRST_DRAW_CARD_HERO_REWARD;
    /**
     * 已使用活动抽取次数必出奖励
     */
    public static List<List<Integer>> ACTIVE_DRAWS_USED_COUNT_HERO_REWARD;
    /**
     * 每日寻访可增加心愿值次数
     */
    public static int DAILY_DRAW_CARD_CAN_INCREASE_WISH_POINTS;
    /**
     * 每日寻访单抽折扣消耗玉璧
     */
    public static int DAILY_DRAW_SINGLE_DRAW_DISCOUNT_TO_CONSUME_JADE;
    /**
     * 寻访心愿值上限
     */
    public static int DRAW_CARD_WISH_VALUE_LIMIT;

    public static void loadSystem() {
        HERO_EQUIP_NUM = SystemTabLoader.getIntegerSystemValue(SystemId.HERO_EQUIP_NUM, 6);
        HERO_BATTLE_LEN = SystemTabLoader.getIntegerSystemValue(SystemId.HERO_BATTLE_LEN, 4);
        HERO_SEARCH_ROLE_LV = SystemTabLoader.getIntegerSystemValue(SystemId.HERO_SEARCH_ROLE_LV, 88);
        NORMAL_HERO_GOLD = SystemTabLoader.getListIntSystemValue(SystemId.NORMAL_HERO_GOLD, "[200,1800]");
        SUPER_HERO_GOLD = SystemTabLoader.getListIntSystemValue(SystemId.SUPER_HERO_GOLD, "[400,3600]");
        NORMAL_HERO_TOKEN = SystemTabLoader.getIntegerSystemValue(SystemId.NORMAL_HERO_TOKEN, 100);
        SUPER_HERO_TOKEN = SystemTabLoader.getIntegerSystemValue(SystemId.SUPER_HERO_TOKEN, 500);
        SEARCH_SUPER_HERO_FOR_FIVE = SystemTabLoader.getListListIntSystemValue(SystemId.SEARCH_SUPER_HERO_FOR_FIVE,
                "[[]]");
        SEARCH_SUPER_HERO_SPECIAL = SystemTabLoader.getMapIntSystemValue(SystemId.SEARCH_SUPER_HERO_SPECIAL, "[[]]");
        GQS_HERO_ID = SystemTabLoader.getIntegerSystemValue(SystemId.GQS_HERO_ID, 1854);
        HERO_WAR_PLANE_NUM = SystemTabLoader.getIntegerSystemValue(SystemId.HERO_WAR_PLANE_NUM, 4);
        HERO_WAR_PLANE_UNLOCK = SystemTabLoader.getListListIntSystemValue(SystemId.HERO_WAR_PLANE_UNLOCK, "[[]]");
        HERO_REGROUP_AWARD_NUM = SystemTabLoader.getIntegerSystemValue(SystemId.HERO_REGROUP_AWARD_NUM, 8000);
        WISH_HERO_COUNT = SystemTabLoader.getIntegerSystemValue(SystemId.WISH_HERO_COUNT, 20);
        DRAW_HERO_CARD_FREE_TIMES_TIME_INTERVAL = SystemTabLoader.getIntegerSystemValue(SystemId.DRAW_HERO_CARD_FREE_TIMES_TIME_INTERVAL, 41400);
        PERMANENT_QUEST_GOLD_CONSUMPTION = SystemTabLoader.getListIntSystemValue(SystemId.PERMANENT_QUEST_GOLD_CONSUMPTION, "[]");
        TIME_LIMITED_SEARCH_FOR_GOLD_COIN_CONSUMPTION = SystemTabLoader.getListIntSystemValue(SystemId.TIME_LIMITED_SEARCH_FOR_GOLD_COIN_CONSUMPTION, "[]");
        DRAW_DUPLICATE_HERO_TO_TRANSFORM_FRAGMENTS = SystemTabLoader.getIntegerSystemValue(SystemId.DRAW_DUPLICATE_HERO_TO_TRANSFORM_FRAGMENTS, 20);
        DRAW_ORANGE_HERO_FRAGMENT_GUARANTEED_TIMES = SystemTabLoader.getIntegerSystemValue(SystemId.DRAW_ORANGE_HERO_FRAGMENT_GUARANTEED_TIMES, 10);
        DRAW_MINIMUM_NUMBER_OF_ORANGE_HERO = SystemTabLoader.getIntegerSystemValue(SystemId.DRAW_MINIMUM_NUMBER_OF_ORANGE_HERO, 40);
        FIRST_DRAW_CARD_HERO_REWARD = SystemTabLoader.getIntegerSystemValue(SystemId.FIRST_DRAW_CARD_HERO_REWARD, 17);
        ACTIVE_DRAWS_USED_COUNT_HERO_REWARD = SystemTabLoader.getListListIntSystemValue(SystemId.ACTIVE_DRAWS_USED_COUNT_HERO_REWARD, "[[]]");
        DAILY_DRAW_CARD_CAN_INCREASE_WISH_POINTS = SystemTabLoader.getIntegerSystemValue(SystemId.DAILY_DRAW_CARD_CAN_INCREASE_WISH_POINTS, 4);
        DAILY_DRAW_SINGLE_DRAW_DISCOUNT_TO_CONSUME_JADE = SystemTabLoader.getIntegerSystemValue(SystemId.DAILY_DRAW_SINGLE_DRAW_DISCOUNT_TO_CONSUME_JADE, 100);
        DRAW_CARD_WISH_VALUE_LIMIT = SystemTabLoader.getIntegerSystemValue(SystemId.DRAW_CARD_WISH_VALUE_LIMIT, 100);
    }

    /**
     * 根据将领寻访类型和寻访次数类型，获取对应的金币消耗
     *
     * @param searchType
     * @param countType
     * @return 当找不到对应的类型时，会返回-1
     */
    public static int getHeroSearchGoldByType(int searchType, int countType) {
        if (searchType == SEARCH_TYPE_NORMAL) {
            if (!CheckNull.isEmpty(NORMAL_HERO_GOLD) && NORMAL_HERO_GOLD.size() >= countType) {
                return NORMAL_HERO_GOLD.get(countType - 1);
            }
        } else if (searchType == SEARCH_TYPE_SUPER) {
            if (!CheckNull.isEmpty(SUPER_HERO_GOLD) && SUPER_HERO_GOLD.size() >= countType) {
                return SUPER_HERO_GOLD.get(countType - 1);
            }
        }

        return -1;
    }

    /** 城墙自动补兵系数 */
    public static final int WALL_HERO_AUTO_ARMY_COEFFICIENT = 1800;

    /** 将领属性：攻击 */
    public static final int ATTR_ATTACK = 1;
    /** 将领属性：防御 */
    public static final int ATTR_DEFEND = 2;
    /** 将领属性：单排兵力 */
    public static final int ATTR_LEAD = 3;

    /** 将领所在队列：空闲 */
    public static final int HERO_STATUS_IDLE = 0;
    /** 将领所在队列：上阵 */
    public static final int HERO_STATUS_BATTLE = 1;
    /** 将领所在队列：采集 */
    public static final int HERO_STATUS_COLLECT = 2;
    /** 将领所在队列：防守 (废弃)*/
    public static final int HERO_STATUS_DEFEND = 3;
    /** 将领所在队列：城墙上阵 */
    public static final int HERO_STATUS_WALL_BATTLE = 4;
    /** 将领所在队列: 特攻队 */
    public static final int HERO_STATUS_COMMANDO = 5;

    /** 将领状态：空闲 */
    public static final int HERO_STATE_IDLE = 0;
    /** 将领状态：出征 */
    public static final int HERO_STATE_BATTLE = 1;
    /** 将领状态：返回 */
    public static final int HERO_STATE_RETREAT = 2;
    /** 将领状态：采集 */
    public static final int HERO_STATE_COLLECT = 3;
    /** 将领状态：驻守 */
    public static final int HERO_STATE_GUARD = 4;

    /** 上阵将领队列位置：1号位 */
    public static final int HERO_BATTLE_1 = 1;
    /** 上阵将领队列位置：3号位 */
    public static final int HERO_BATTLE_2 = 2;
    /** 上阵将领队列位置：3号位 */
    public static final int HERO_BATTLE_3 = 3;
    /** 上阵将领队列位置：4号位 */
    public static final int HERO_BATTLE_4 = 4;

    /** 将领快速升级类型：低级 */
    public static final int QUICK_UP_TYPE_LOW = 1;
    /** 将领快速升级类型：中级 */
    public static final int QUICK_UP_TYPE_MIDDLE = 2;
    /** 将领快速升级类型：高级 */
    public static final int QUICK_UP_TYPE_HIGH = 3;
    /** 将领快速升级类型：顶级 */
    public static final int QUICK_UP_TYPE_TOP = 4;

    /** 将领寻访类型：良将寻访 */
    public static final int SEARCH_TYPE_NORMAL = 1;
    /** 将领寻访类型：神将寻访 */
    public static final int SEARCH_TYPE_SUPER = 2;

    /** 寻访次数类型：寻访一次 */
    public static final int COUNT_TYPE_ONE = 1;
    /** 寻访次数类型：寻访10次 */
    public static final int COUNT_TYPE_TEN = 2;

    /** 将领寻访消耗类型：免费次数 */
    public static final int SEARCH_COST_FREE = 1;
    /** 将领寻访消耗类型：良将令或神将令 */
    public static final int SEARCH_COST_PROP = 2;
    /** 将领寻访消耗类型：金币 */
    public static final int SEARCH_COST_GOLD = 3;

    /** 良将寻访必出良将的次数 */
    public static final int NORMAL_SPECIL_NUM = 10;
    /** 神将寻访必出神将的次数 */
    public static final int SUPER_SPECIL_NUM = 10;
    /** 神将寻访首次激活必出神将的次数 */
    public static final int SUPER_FIRST_SPECIL_NUM = 5;

    /** 将领寻访结果:将领 */
    public static final int SEARCH_RESULT_HERO = 1;
    /** 将领寻访结果:将令 */
    public static final int SEARCH_RESULT_TOKEN = 2;

    /** 良将寻访免费次数CD时间 */
    public static final int NORMAL_SEARCH_CD = 24 * 60 * 60;

    /** 神将激活后的有效时间 */
    public static final int SUPER_OPEN_TIME = 3 * 24 * 60 * 60;

    /** 良将令id */
    public static final int NORMAL_HERO_ID = 5211;
    /** 神将令id */
    public static final int SUPER_HERO_ID = 5213;

    /** 出良将增加神将激活进度（百分比） */
    public static final int HERO_ADD_PROCESS = 7;
    /** 出将令增加神将激活进度（百分比） */
    public static final int TOKEN_ADD_PROCESS = 2;
    /** 上阵将领更换位置:类型 */
    public static final int CHANGE_POS_TYPE = 0;
    /** 防守将领更换位置:类型 */
    public static final int CHANGE_DEFEND_POS_TYPE = 1;
    /** 副本将领更换位置:类型 */
    public static final int CHANGE_COMBAT_POS_TYPE = 2;
    /** 出征将领更换位置:类型 */
    public static final int CHANGE_BATTLE_POS_TYPE = 3;


    /**
     * 激活 1, 进化 2, 重组 3
     */
    public static final int AWAKEN_HERO_TYPE_1 = 1;
    public static final int AWAKEN_HERO_TYPE_2 = 2;
    public static final int AWAKEN_HERO_TYPE_3 = 3;

    public static final int AWAKEN_PART_MIN = 1;
    public static final int AWAKEN_PART_MAX = 5;

    /** 橙将*/
    public static final int QUALITY_ORANGE_HERO = 5;

}
