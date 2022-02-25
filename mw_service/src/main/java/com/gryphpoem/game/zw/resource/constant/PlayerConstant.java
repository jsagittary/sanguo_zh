package com.gryphpoem.game.zw.resource.constant;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName PlayerConstant.java
 * @Description 玩家杂七杂八的数据存储的key
 * @author QiuKun
 * @date 2018年7月17日
 */
public interface PlayerConstant {

    /** 每日已开发都城次数 */
    int UP_CAPITALCITY_CNT = 1;
    /** 每日已使用免费开发都城次数 */
    int FREE_UP_CAPITALCITY_CNT = 2;
    /** 荣耀日报未读红点 */
    int HONOR_REPORT_TIPS = 3;
    /** 玩家装备打造次数记录 只记录紫装以上（含紫装）总次数 */
    int EQUIP_MAKE_COUNT = 4;
    /** 玩家装备打造次数记录 只记录紫装以上（含紫装）当前回合的次数 */
    int EQUIP_MAKE_NUM = 5;
    /** 极品装备科技 记录玩家当前命中概率 */
    int EQUIP_MAKE_PROBABILITY = 6;
    /** 极品装备科技 记录玩家当前命中最低间隔值 */
    int EQUIP_MAKE_INTERVAL = 7;
    /** 荣耀日报解锁时间 */
    int RYRB_LOCK_TIME = 8;
    /** 极品装备科技 记录玩家当前命中范围 */
    int EQUIP_MAKE_RANGE = 9;

    /*----------------------荣耀日报相关获取----------------------------*/
    /** 当天搬离城市X次 */
    int HONOR_DAILY_COND_ID_1 = 10;
    /** 占领了非本阵营的据点x次 */
    int HONOR_DAILY_COND_ID_2 = 11;
    /** 击败匪徒达到X次 */
    int HONOR_DAILY_COND_ID_3 = 12;
    /** 当天击败X个敌人 */
    int HONOR_DAILY_COND_ID_4 = 13;
    /** 当天击败X个盟军阵营 y等级 */
    int HONOR_DAILY_COND_ID_5 = 14;
    /** 当天击败X个帝国阵营 y等级 */
    int HONOR_DAILY_COND_ID_6 = 15;
    /** 当天击败X个联军阵营 y等级 */
    int HONOR_DAILY_COND_ID_7 = 16;
    /** 当天击败X个联军阵营 y等级 */
    int HONOR_DAILY_COND_ID_8 = 17;
    /** 当天打匪军失败x次 y等级 */
    int HONOR_DAILY_COND_ID_9 = 18;
    /** 当天对其他玩家使用x次侦查 */
    int HONOR_DAILY_COND_ID_10 = 19;
    /** 当天派遣将领出战行走最远x英里 */
    int HONOR_DAILY_COND_ID_11 = 20;
    /** 当天被其他玩家掠夺了X个资源 */
    int HONOR_DAILY_COND_ID_12 = 21;
    /** 当天掠夺其他玩家x个资源最（3种资源之和，燃油、电力、补给）） */
    int HONOR_DAILY_COND_ID_13 = 22;
    /** 当天损失了x个士兵（3种兵之和） */
    int HONOR_DAILY_COND_ID_14 = 23;
    /** 当天送礼的亲密值达到x点（互动次数不算，礼物亲密值之和） */
    int HONOR_DAILY_COND_ID_15 = 24;


    /*-----------------------------------------------------------------*/

    /*----------------------战机功能相关获取----------------------------*/

    /** 高级寻访次数 */
    int PLANE_FACTORY_MAKE_NUM = 25;
    /** 寻访奖励是否领取 */
    int PLANE_FACTORY_SEARCH_AWARD = 26;

    /*-----每日清除-----*/
    /** 当天最后一次寻访时间 */
    int PLANE_LAST_FREE_SEARCH_TIME = 27;
    /** 当天总寻访次数 */
    int PLANE_FREE_SEARCH_CNT = 28;
    /** 当天击飞玩家获得战机碎片次数 */
    int PLANE_HIT_FLY_AWARD_CNT = 29;

    /** 第一次寻访 */
    int PLANE_FACTORY_SEARCH_FIRST_COUNT = 30;
    /** 最后一次高级免费寻访时间 */
    int PLANE_SUPER_FREE_SEARCH_TIME = 31;

    /*-----------------------------------------------------------------*/

    /** 每日清除的数据keyId */
    List<Integer> CLEAN_LIST_KEY = new ArrayList<>();

    /** 给客户端显示数据keyId (在GetLord协议的mixtureData字段显示) */
    List<Integer> SHOW_CLIENT_LIST_KEY = new ArrayList<>();

    /** 反攻德意志获取的积分 */
    int COUNTER_ATK_CREDIT = 32;
    /** 教官钞票 */
    int MENTOR_BILL = 33;
    /** 决战次数标识*/
    int DECISIVE_BATTLE_COUNT = 34;

    /** 世界进度中, 玩家历史最大战斗力 */
    int WORLD_SCHEDULE_MAX_FIGHT = 35;
    /** 世界进度中, 可攻打boss次数(每日清除) */
    int WORLD_SCHEDULE_ATK_BOSS_CNT = 36;
    /** 掉落活动, 每天可掉落次数(每次清除) */
    int ACT_BANDIT_AWARD_COUNT = 37;
    /** 金锭 */
    int GOLD_INGOT = 38;

    /**
     * 引导蝎王解锁时间
     */
    int SCORPION_ACTIVATE_END_TIME = 39;

    /**
     * 救援爱丽丝触发的时间
     */
    int ALICE_TRIGGER_TIME = 40;

    /**
     * 救援爱丽丝获取奖励的时间
     */
    int ALICE_AWARD_TIME = 41;

    /**
     * 每日发送阵营邮件次数
     */
    int DAILY_SEND_MAIL_CNT = 42;

    /**
     * 每日对世界boss的伤害
     */
    int DAILY_ATK_BOSS_VAL = 43;

    /**
     * 加入社区奖励, 客户端需要显示
     */
    int JOIN_COMMUNITY_AWARD = 44;

    /**
     * 市场每日翻牌次数
     */
    int MARKET_TRIGGER_TIMES = 45;

    /**
     * FB的结束时间
     */
    int FB_END_TIME = 46;

    /**
     * 每日广告奖励领取次数
     */
    int DAILY_ADVERTISEMENT_REWARD = 47;

    /**
     * 每日快速购买兵的数量
     */
    int DAILY_QUICK_BUY_ARMY = 48;

    /**
     * 发送渠道邮件
     */
    int SEND_CHANNEL_MAIL = 49;

    /**
     * 每日英雄分享次数
     */
    int DAILY_HERO_SHARE_CNT = 50;

    /**
     * 每日兵书分享次数
     */
    int DAILY_MEDAL_SHARE_CNT = 51;

    /**
     * 解除师傅的时间
     */
    int DEL_MASTER_TIME = 52;

    /**
     * 圣诞活动, 每天可掉落次数(每次清除)
     */
    int ACT_CHRISTMAS_AWARD_COUNT = 53;

    /**
     * 最近创建支付订单时间
     */
    int LAST_PAY_TIME = 54;

    /**
     * 最近离开战火燎原的时间
     */
    int LEAVE_WAR_FIRE_TIME = 55;

    /**
     * 战火燎原货币
     */
    int WAR_FIRE_PRICE = 56;

    int ACT_DROP_CONTROL_COUNT = 57;

    /**
     * 修缮城堡
     */
    int ACT_REPAIR_CASTLE_COUNT = 58;

    /**
     * 是否开启官员入口banner(0-不开启, 1-开启)
     */
    int WHETHER_ASSEMBLY_ENTRANCE = 61;

    /**
     * 宝具每日分享次数
     */
    int DAILY_TREASURE_WARE_SHARE_CNT = 62;

    /**
     *  跨服燎原铸金币
     */
    int CROSS_WAR_FIRE_PRICE = 63;

    /**
     * 注意：10001 - 10007 是最近一周的充值金额的key
     */
    int RECENTLY_PAY = 10000;

    /**
     * 给客户端显示数据keyId (在GetLord协议的mixtureData字段显示)
     *
     * @return
     */
     static List<Integer> getShowClientList() {
        if (SHOW_CLIENT_LIST_KEY.isEmpty()) {
            SHOW_CLIENT_LIST_KEY.add(UP_CAPITALCITY_CNT);
            SHOW_CLIENT_LIST_KEY.add(FREE_UP_CAPITALCITY_CNT);
            SHOW_CLIENT_LIST_KEY.add(SCORPION_ACTIVATE_END_TIME);
            SHOW_CLIENT_LIST_KEY.add(ALICE_TRIGGER_TIME);
            SHOW_CLIENT_LIST_KEY.add(ALICE_AWARD_TIME);
            SHOW_CLIENT_LIST_KEY.add(JOIN_COMMUNITY_AWARD);
            SHOW_CLIENT_LIST_KEY.add(FB_END_TIME);
            SHOW_CLIENT_LIST_KEY.add(DAILY_ADVERTISEMENT_REWARD);
            SHOW_CLIENT_LIST_KEY.add(DAILY_QUICK_BUY_ARMY);
            SHOW_CLIENT_LIST_KEY.add(LEAVE_WAR_FIRE_TIME);
        }
        return SHOW_CLIENT_LIST_KEY;
    }

    /**
     * 是否在客户端显示列表中的keyId
     *
     * @param keyId
     * @return
     */
     static boolean isInShowClientList(int keyId) {
        return getShowClientList().contains(keyId);
    }

    /**
     * 每日清除的数据keyId
     *
     * @return
     */
     static List<Integer> getCleanList() {
        if (CLEAN_LIST_KEY.isEmpty()) {
            CLEAN_LIST_KEY.add(UP_CAPITALCITY_CNT);
            CLEAN_LIST_KEY.add(FREE_UP_CAPITALCITY_CNT);
            CLEAN_LIST_KEY.add(HONOR_REPORT_TIPS);

            CLEAN_LIST_KEY.add(HONOR_DAILY_COND_ID_1);
            CLEAN_LIST_KEY.add(HONOR_DAILY_COND_ID_2);
            CLEAN_LIST_KEY.add(HONOR_DAILY_COND_ID_3);
            CLEAN_LIST_KEY.add(HONOR_DAILY_COND_ID_4);
            CLEAN_LIST_KEY.add(HONOR_DAILY_COND_ID_5);
            CLEAN_LIST_KEY.add(HONOR_DAILY_COND_ID_6);
            CLEAN_LIST_KEY.add(HONOR_DAILY_COND_ID_7);
            CLEAN_LIST_KEY.add(HONOR_DAILY_COND_ID_8);
            CLEAN_LIST_KEY.add(HONOR_DAILY_COND_ID_9);
            CLEAN_LIST_KEY.add(HONOR_DAILY_COND_ID_10);
            CLEAN_LIST_KEY.add(HONOR_DAILY_COND_ID_11);
            CLEAN_LIST_KEY.add(HONOR_DAILY_COND_ID_12);
            CLEAN_LIST_KEY.add(HONOR_DAILY_COND_ID_13);
            CLEAN_LIST_KEY.add(HONOR_DAILY_COND_ID_14);
            CLEAN_LIST_KEY.add(HONOR_DAILY_COND_ID_15);

            CLEAN_LIST_KEY.add(PLANE_LAST_FREE_SEARCH_TIME);
            CLEAN_LIST_KEY.add(PLANE_FREE_SEARCH_CNT);
            CLEAN_LIST_KEY.add(PLANE_HIT_FLY_AWARD_CNT);
            CLEAN_LIST_KEY.add(DECISIVE_BATTLE_COUNT);
            CLEAN_LIST_KEY.add(WORLD_SCHEDULE_ATK_BOSS_CNT);

            CLEAN_LIST_KEY.add(ACT_BANDIT_AWARD_COUNT);
            CLEAN_LIST_KEY.add(DAILY_SEND_MAIL_CNT);
            CLEAN_LIST_KEY.add(MARKET_TRIGGER_TIMES);


            CLEAN_LIST_KEY.add(DAILY_ATK_BOSS_VAL);
            CLEAN_LIST_KEY.add(DAILY_ADVERTISEMENT_REWARD);
            CLEAN_LIST_KEY.add(DAILY_QUICK_BUY_ARMY);
            CLEAN_LIST_KEY.add(DAILY_TREASURE_WARE_SHARE_CNT);

            CLEAN_LIST_KEY.add(DAILY_HERO_SHARE_CNT);
            CLEAN_LIST_KEY.add(DAILY_MEDAL_SHARE_CNT);

            CLEAN_LIST_KEY.add(ACT_CHRISTMAS_AWARD_COUNT);
            CLEAN_LIST_KEY.add(ACT_DROP_CONTROL_COUNT);
            CLEAN_LIST_KEY.add(ACT_REPAIR_CASTLE_COUNT);
        }
        return CLEAN_LIST_KEY;
    }

}
