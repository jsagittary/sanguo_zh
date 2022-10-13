package com.gryphpoem.game.zw.resource.constant;

import com.gryphpoem.game.zw.resource.util.ActParamTabLoader;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.SystemTabLoader;

import java.util.*;

/**
 * @author QiuKun
 * @ClassName ActParamConstant.java
 * @Description 活动配置常量
 * @date 2017年12月22日
 */
public final class ActParamConstant {

    private ActParamConstant() {
    }

    /**
     * 副本活动翻倍倍数
     */
    public static int ACT_COMBAT_DOUBLE_NUM;
    /**
     * 副本掉落
     */
    public static List<List<Integer>> ACT_COMBAT_DROP;
    /**
     * 匪军图纸倍数
     */
    public static int ACT_BANDIT_DRAWING;
    /**
     * 匪军迁城倍数
     */
    public static int ACT_BANDIT_MOVE;
    /**
     * 匪军资源倍数
     */
    public static int ACT_BANDIT_RES;
    /**
     * 匪军加速 配置
     */
    public static List<List<Integer>> ACT_BANDIT_ACCE;
    /**
     * 匪军徽章 配置
     */
    public static List<List<Integer>> ACT_BANDIT_BADGE;
    /**
     * 匪军徽章 MAP配置
     */
    public static Map<Integer, List<List<Integer>>> ACT_BANDIT_BADGE_MAP;
    /**
     * 盖世太保等级 配置
     */
    public static List<Integer> ACT_GESTAPO_LEVEL;
    /**
     * 盖世太保密信、徽章兑换资源
     */
    public static List<List<Integer>> ACT_GESTAPO_EXCHANG_RES;
    /**
     * 盖世太保兵力返还比例
     */
    public static int ACT_GESTAPO_RECOVER_ARMY_NUM;
    /**
     * 图纸活动
     */
    public static int ACT_CITY_DRAWING;
    /**
     * 采集翻倍
     */
    public static int ACT_COLLECT_REWARD;
    /**
     * 制造翻倍
     */
    public static int ACT_RECRUIT_REWARD;
    /**
     * 全军返利最小活动等级
     */
    public static int ACT_ALL_CHARGE_LORD_LV;
    /**
     * 精英部队最小活动等级
     */
    public static int ACT_VIP_LORD_LV;
    /**
     * 黑鹰计划免费次数
     */
    public static int BLACKHAWK_FREE_COUNT;
    /**
     * 黑鹰计划刷新时间(单位秒)
     */
    public static int BLACKHAWK_REFRESH_INTERVAL;
    /**
     * 黑鹰计划刷新初始金额
     */
    public static int BLACKHAWK_INIT_PAY_GOLD;
    /**
     * 黑鹰计划每次刷新递增额度
     */
    public static int BLACKHAWK_INCR_GOLD;
    /**
     * 黑鹰计划招募将领ID
     */
    public static int BLACKHAWK_HERO_ID;
    /**
     * 黑鹰计划招募将领信物所需个数
     */
    public static int BLACKHAWK_NEED_TOKEN;
    /**
     * 黑鹰计划招信物格子的id
     */
    public static final int BLACKHAWK_TOKEN_KEYID = 6;
    /**
     * 黑鹰计划持续时间 单位（秒 ）
     */
    public static int ACT_BLACK_TIME;

    /**
     * 空降补给的|资源|军备|物资|补给花费黄金
     */
    public static List<Integer> SUPPLY_DORP_GOLD;

    /**
     * 购买成长计划要求[VIP等级，金币]
     */
    public static List<Integer> OPEN_ACT_GROW_NEED;

    /**
     * 首充礼包持续时间 单位（秒 ）
     */
    public static int ACT_FIRSH_CHARGE_TIME;

    /**
     * 充值金额对应VIP经验系数
     */
    public static int PAY_VIP_COEFF;
    /**
     * 翻倍奖励活动的倍数
     */
    public static int ACT_DOUBLE_NUM;
    /**
     * 生产加速时间减少比例
     */
    public static int ACT_PRODUCTTION_NUM;
    /**
     * 全军返利第一名阵营玩家奖励
     */
    public static List<List<Integer>> ACT_ALL_CHARGE_REWARD;
    /**
     * 勇冠三军第一名阵营玩家奖励
     */
    public static List<List<Integer>> ACT_BRAVEST_ARMY_AWARD;
    /**
     * 攻城掠地活动等级配置 [参与等级]
     */
    public static List<Integer> ACT_ATK_CITY_LEVEL;

    /**
     * 盖世太保排行阵营奖励
     */
    public static Integer ACT_GESTAPO_CAMP_RANK_LEVEL;

    /**
     * 军备促销积分箱累计额度
     */
    public static Integer ACT_PROP_PROMOTION_AWARD_NUM;

    /**
     * 幸运转盘金币抽奖奖励
     */
    public static List<Integer> ACT_TURNPLATE_GOLD_AWRAD;

    /**
     * 装备转盘金币抽奖奖励
     */
    public static List<Integer> EQUIP_TURNPLATE_GOLD_AWRAD;

    /**
     * 装备转盘全服抽奖次数
     */
    public static List<Integer> EQUIP_TURNPLATE_GLOBAL_NUM;

    /**
     * 装备转盘需要发送跑马灯的装备
     */
    public static List<List<Integer>> EQUIP_TURNPLATE_SEND_CHAT_AWARD;

    /**
     * 好运道中奖广播
     */
    public static List<List<Integer>> ACT_GOLD_LUCK_SEND_CHAT_AWARD;

    /**
     * 幸运转盘,碎片兑换奖励
     */
    public static List<Integer> ACT_TURNPLATE_EXCHANGE_AWRAD;

    /**
     * 充值转盘档位
     */
    public static List<List<Integer>> ACT_PAY_TURNPLATE_PAY_GOLD;
    /**
     * 特惠礼包活动, 每月的第周 周几 持续几天 [[2,4],[5],[3]]
     */
    public static List<List<Integer>> ACT_GIFT_PROMOTION_WEEKOFMONTH_WEEK_DURATION;

    /**
     * 充值排行-上榜需充值的额度
     */
    public static Integer PAY_RANKINGS_CONDITION;

    /**
     * 兵力排行-上榜需战斗损兵
     */
    public static Integer TROOPS_RANKINGS_CONDITION;

    /**
     * 攻城战排行-上榜需攻城战次数
     */
    public static Integer CITY_WAR_RANKINGS_CONDITION;

    /**
     * 营地战排行-上榜需营地站次数
     */
    public static Integer CAMP_WAR_RANKINGS_CONDITION;

    /**
     * 建设排行-上榜需建设站次数
     */
    public static Integer BUIDING_RANKINGS_CONDITION;

    /**
     * 改造排行-上榜需改造次数
     */
    public static Integer REFORM_RANKINGS_CONDITION;

    /**
     * 打造排行-上榜需打造次数
     */
    public static Integer FORGE_RANKINGS_CONDITION;

    /**
     * 补给排行-上榜需补给获得量
     */
    public static Integer SUPPLY_RANKINGS_CONDITION;

    /**
     * 矿石排行-上榜需矿石获得量
     */
    public static Integer ORE_RANKINGS_CONDITION;

    /**
     * 礼物排行-上榜需礼物积分
     */
    public static Integer GIFT_RANKINGS_CONDITION;

    /**
     * 幸运转盘排行-上榜所需积分
     */
    public static Integer LUCKY_TRUNPLATE_RANKINGS_CONDITION;

    /**
     * 装备转盘排行-上榜所需积分
     */
    public static Integer EQUIP_TRUNPLATE_RANKINGS_CONDITION;
    /**
     * 棋盘点数权重
     */
    public static List<List<Integer>> ACT_MONOPOLY_CNT_WEIGHT;
    /**
     * 阵营排行
     **/
    public static Integer ACT_CAMP_RANK;//

    /**
     * 矿石转盘-抽取金币消耗配置
     */
    public static List<List<Integer>> ORE_TRUNPLATE_GOLD;

    /**
     * 新矿石转盘金币抽奖奖励
     */
    public static List<Integer> ORE_TURNPLATE_GOLD_AWRAD_NEW;

    /**
     * 新充值排行-上榜所需充值额度
     */
    public static Integer PAY_RANKINGS_CONDITION_NEW;
    /**
     * 消耗排行上榜所需额度
     */
    public static Integer ACT_CONSUME_GOLD_RANK;

    /**
     * 名将转盘金币抽奖奖励
     */
    public static List<Integer> FAMOUS_GENERAL_TURNPLATE_GOLD_AWRAD;

    /**
     * 名将转盘排行-上榜所需积分
     */
    public static Integer FAMOUS_GENERAL_TRUNPLATE_RANKINGS_CONDITION;

    /**
     * 名将转盘,碎片兑换奖励
     */
    public static List<List<Integer>> ACT_FAMOUS_GENERAL_EXCHANGE_AWRAD;
    /**
     * 阵营战斗力排行 进榜条件[最低指挥官等级，最低战力要求]
     */
    public static List<Integer> ACT_CAMP_FIGHT_RANK_JOIN_COND;
    /**
     * 阵营排行中战力最高的阵营奖励，没有对应档次时默认第一个，填写格式[[活动档次1,奖励1],[活动档次2，奖励2],[活动档次3，奖励3]...]
     */
    public static List<List<Integer>> ACT_CAMP_FIGHT_WIN_CAMP_AWARD;
    /**
     * 许愿池聊天气泡奖励
     */
    public static List<List<Integer>> ACT_WISHING_AWARD;

    /**
     * 幸运奖池 [初始值、充值百分比转换到奖池、充值百分比转换为次数]
     **/
    public static List<Integer> LUCKY_POOL_1;
    /**
     * 幸运奖池 [活跃度、抽奖次数]
     **/
    public static List<List<Integer>> LUCKY_POOL_2;

    /**
     * 红包活动发送的红包id
     */
    public static List<List<Integer>> ACT_RED_PACKET;

    /**
     * 戒指活动总共刷新次数
     */
    public static Integer BARTON_TOTAL_REFRESH_COUNT;
    /**
     * 戒指活动刷新金额
     */
    public static Integer BARTON_REFRESH_COST_GOLD;

    /**
     * 触发式礼包,将领触发,忽略这两个将领
     */
    public static List<Integer> ACT_TRIGGER_HERO_IGNORE;

    /**
     * 导师排行的配置[[minLv, maxLv, integral], ...]
     */
    public static List<List<Integer>> ACT_TUTOR_RANK_CONF;
    /**
     * 导师排行-上榜条件
     */
    public static Integer ACT_TUTOR_CONDITION_RANK;

    /**
     * 活动广播最大消息个数[[activityId,maxNum], ...]
     */
    public static List<List<Integer>> ACT_NOTICE_NUM;

    /**
     * 阵营活动大国风范效果 [[提升百分比],[获得贡献值]]
     */
    public static List<List<Integer>> ROYAL_ARENA_COUNTRY_STYLE;
    /**
     * 阵营活动刺探效果 [[减少贡献值],[获得贡献值]]
     */
    public static List<List<Integer>> ROYAL_ARENA_DETECT_STYLE;
    /**
     * 阵营活动大国风范钻石消耗 每次消耗的钻石
     */
    public static Integer ROYAL_ARENA_COUNTRY_STYLE_CONSUME;
    /**
     * 阵营活动刺探钻石消耗 每次消耗的钻石
     */
    public static Integer ROYAL_ARENA_DETECT_STYLE_CONSUME;
    /**
     * 阵营活动大国风范购买次数 [[个人次数],[阵营次数]]
     */
    public static List<List<Integer>> ROYAL_ARENA_COUNTRY_STYLE_CNT;
    /**
     * 阵营活动刺探购买次数 [[个人次数],[阵营次数]]
     */
    public static List<List<Integer>> ROYAL_ARENA_DETECT_STYLE_CNT;

    /**
     * 阵营任务购买次数 [[免费次数], [可购买次数]]
     */
    public static List<List<Integer>> ROYAL_ARENA_TASK_CNT;
    /**
     * 阵营任务购买价格 [[首次购买价格], [每次递增]]
     */
    public static List<List<Integer>> ROYAL_ARENA_TASK_PRICE;
    /**
     * 阵营活动免费刷新次数 每个任务免费刷新次数
     */
    public static Integer ROYAL_ARENA_TASK_FREE_REFRESH_CNT;
    /**
     * 阵营活动任务刷新价格
     */
    public static Integer ROYAL_ARENA_TASK_REFRESH_PRICE;

    /**
     * 阵营活动任务时间 [[一星时间],[递增时间]],秒
     */
    public static List<List<Integer>> ROYAL_ARENA_TASK_ENDTIME;

    /**
     * 阵营排行的上榜条件
     */
    public static List<List<Integer>> ACT_ROYAL_ARENA_RANK;

    /**
     * 建造礼包持续时间
     */
    public static Integer BUILD_GIFT_CHARGE_TIME;

    /**
     * [[充值金额],[持续天数]]
     */
    public static List<List<Integer>> ACT_DEDICATED_CUSTOMER_SERVICE_CONF;

    /**
     * [[充值金额]]
     */
    public static List<List<Integer>> ACT_WECHAT_PAY;
    /**
     * 神兵宝具广播
     */
    public static List<List<Integer>> ACT_MAGIC_PROP_IDS_IN_CHAT;

    //////////////圣诞活动常量
    public static int CHRISTMAS_311;
    public static int CHRISTMAS_312;
    public static int CHRISTMAS_313;
    public static List<Integer> CHRISTMAS_314;
    public static int CHRISTMAS_315;
    public static List<Integer> CHRISTMAS_316;
    public static Date CHRISTMAS_317;
    public static List<Integer> CHRISTMAS_318;

    public static int REPAIRCASTLE_311;
    public static int REPAIRCASTLE_312;
    public static int REPAIRCASTLE_313;
    public static List<Integer> REPAIRCASTLE_314;
    public static int REPAIRCASTLE_315;
    public static List<Integer> REPAIRCASTLE_316;
    public static List<Integer> REPAIRCASTLE_318;

    public static List<Integer> SUMMER_330;
    public static List<Integer> SUMMER_331;

    // 斋月拜访圣坛

    // 免费拜访次数上限
    public static int ACT_VISIT_ALTAR_FREE_CNT;

    // 购买拜访次数上限
    public static int ACT_VISIT_ALTAR_GOLD_CNT;

    // 每阶段购买互动次数所需钻石数量
    public static List<List<Integer>> ACT_VISIT_ALTAR_GOLD_CONSUME;

    // 金币拜访消耗
    public static List<List<Integer>> ACT_VISIT_OIL_CONSUME;

    // 钻石拜访消耗
    public static List<List<Integer>> ACT_VISIT_GOLD_CONSUME;

    // 圣坛刷新时间crontab
    public static String ACT_VISIT_REFRESH_CRON;

    //周年庆放烟花活动参数[[活动id,消耗道具id,消耗数量,随机库id]]
    public static List<List<Integer>> ACT_FIRE_WORK;

    //皮肤返场活动玩家可购买次数
    public static int ACT_SKIN_ENCORE_BUY_COUNT;

    //秋季拍卖场活动时间
    public static List<List<Integer>> ACT_AUCTION_TIME_LIST;

    //秋季拍卖回合持续时间
    public static int ACT_AUCTION_ROUND_PERIOD;

    /**
     * 喜悦金秋获得随机奖励数量上限
     */
    public static int ACT_GOLDEN_AUTUMN_AWARD_UPPER_LIMIT;

    /**
     * 喜悦金秋获得随机奖励所需播种
     */
    public static int ACT_GOLDEN_AUTUMN_AWARD_SOWING;

    /**
     * 喜悦金秋获随机奖励库
     */
    public static List<List<Integer>> ACT_GOLDEN_AUTUMN_RANDOM_AWARD_LIBRARY;

    /**
     * 喜悦金秋获种子果实比例
     */
    public static List<List<Integer>> ACT_GOLDEN_AUTUMN_SEED_FRUIT_PROPORTION;

    /**
     * 秋季拍卖 最高加持有次数
     */
    public static int HIGHEST_Bid_COUNT;

    /**
     * 拍卖即将结束跑马灯时间
     */
    public static int ACT_AUCTION_ABOUT_TO_END;

    /**
     * 拍卖活动类型最高出价次数
     */
    public static Map<Integer, Integer> ACT_AUCTION_TYPE_HIGHEST_COUNT;

    /**
     * 玛雅音乐创作活动参数
     * [免费次数,购买次数,每次购买价格]
     */
    public static List<Integer> MUSIC_CRT_OFFICE_PARAMS;
    /**
     * 音乐创作室,难度对应的任务数量
     * KEY:难度, VALUE:数量
     */
    public static Map<Integer, Integer> MUSIC_CRT_OFFICE_TASK_COUNT_BY_DIFFICULT;

    /**
     * 宝具转盘打造增加转盘次数
     */
    public static List<List<Integer>> ACT_MAGIC_TREASURE_WARE_TURNTABLE_CNT;
    /**
     * 宝具打造增加宝具排行分数
     */
    public static List<List<Integer>> ACT_MAGIC_TREASURE_WARE_RANK_SCORE;
    /**
     * 张飞转盘道具保底规则
     */
    public static List<Integer> ACT_FAMOUS_GENERAL_TURNTABLE_GUARANTEE;
    /**
     * 王朝遗迹活动，遗迹建筑刷新的时刻
     */
    public static String ACT_RELIC_REFRESH;
    /**
     * 王朝遗迹活动，遗迹建筑刷新后的保护期和探索期
     */
    public static List<Integer> ACT_RELIC_STAMP;
    /**
     * 王朝遗迹跑马灯触发条件
     */
    public static Map<Integer, Integer> RELIC_CHAT_KILL_BROADCAST_MAP;
    /**
     * 王朝遗迹活动，伤病恢复系数
     */
    public static int RELIC_RECOVERY_RATIO;
    /**
     * 王朝遗迹活动，每个进攻兵团的最大战斗场数
     */
    public static int RELIC_ARMY_FIGHT_MAX;
    /**
     * 王朝遗迹帮一奖励
     */
    public static List<List<Integer>> RELIC_MAX_SCORE_PLAYER_WARD;
    /**
     * 王朝遗迹 行军加速
     */
    public static int RELIC_MARCH_SPEEDUP;

    /*-------------------------------load数据-------------------------------------*/
    public static void loadSystem() {
        RELIC_MARCH_SPEEDUP = ActParamTabLoader.getIntegerSystemValue(ActParamId.ACT_RELIC_MARCH_SPEEDUP, 5000);
        RELIC_MAX_SCORE_PLAYER_WARD = ActParamTabLoader.getListListIntSystemValue(ActParamId.ACT_RELIC_MAX_SCORE_PLAYER_WARD, "[]");
        RELIC_ARMY_FIGHT_MAX = ActParamTabLoader.getIntegerSystemValue(ActParamId.ACT_RELIC_ARMY_FIGHT_MAX, 10);
        RELIC_RECOVERY_RATIO = ActParamTabLoader.getIntegerSystemValue(ActParamId.ACT_RELIC_RECOVERY_RATIO, 5000);
        RELIC_CHAT_KILL_BROADCAST_MAP = ActParamTabLoader.getMapIntSystemValue(ActParamId.ACT_RELIC_CHAT_KILL_BROADCAST, "[]");
        ACT_RELIC_REFRESH = ActParamTabLoader.getStringSystemValue(ActParamId.ACT_RELIC_REFRESH, "0 0 7,15,23 * * ?");
        ACT_RELIC_STAMP = ActParamTabLoader.getListIntSystemValue(ActParamId.ACT_RELIC_STAMP, "[600,1800,2]");
        ACT_MAGIC_TREASURE_WARE_TURNTABLE_CNT = ActParamTabLoader.getListListIntSystemValue(ActParamId.ACT_MAGIC_TREASURE_WARE_TURNTABLE_CNT, "[[]]");
        ACT_MAGIC_TREASURE_WARE_RANK_SCORE = ActParamTabLoader.getListListIntSystemValue(ActParamId.ACT_MAGIC_TREASURE_WARE_RANK_SCORE, "[[]]");

        MUSIC_CRT_OFFICE_PARAMS = ActParamTabLoader.getListIntSystemValue(ActParamId.MUSIC_CRT_OFFICE_PARAMS, "[]");
        MUSIC_CRT_OFFICE_TASK_COUNT_BY_DIFFICULT = ActParamTabLoader.getMapIntSystemValue(ActParamId.MUSIC_CRT_OFFICE_TASK_COUNT_BY_DIFFICULT, "[[]]");

        ACT_FIRE_WORK = ActParamTabLoader.getListListIntSystemValue(ActParamId.ACT_FIRE_WORK_ID, "[[]]");
        ACT_SKIN_ENCORE_BUY_COUNT = ActParamTabLoader.getIntegerSystemValue(ActParamId.ACT_SKIN_ENCORE_BUY_COUNT_ID, 0);

        SUMMER_330 = ActParamTabLoader.getListIntSystemValue(330, "[]");
        SUMMER_331 = ActParamTabLoader.getListIntSystemValue(331, "[]");

        REPAIRCASTLE_311 = ActParamTabLoader.getIntegerSystemValue(321, 5);
        REPAIRCASTLE_312 = ActParamTabLoader.getIntegerSystemValue(322, 7500);
        REPAIRCASTLE_313 = ActParamTabLoader.getIntegerSystemValue(323, 10);
        REPAIRCASTLE_314 = ActParamTabLoader.getListIntSystemValue(324, "[]");
        REPAIRCASTLE_315 = ActParamTabLoader.getIntegerSystemValue(325, 10);
        REPAIRCASTLE_316 = ActParamTabLoader.getListIntSystemValue(326, "[]");
        REPAIRCASTLE_318 = ActParamTabLoader.getListIntSystemValue(327, "[]");

        CHRISTMAS_311 = ActParamTabLoader.getIntegerSystemValue(311, 5);
        CHRISTMAS_312 = ActParamTabLoader.getIntegerSystemValue(312, 7500);
        CHRISTMAS_313 = ActParamTabLoader.getIntegerSystemValue(313, 10);
        CHRISTMAS_314 = ActParamTabLoader.getListIntSystemValue(314, "[]");
        CHRISTMAS_315 = ActParamTabLoader.getIntegerSystemValue(315, 10);
        CHRISTMAS_316 = ActParamTabLoader.getListIntSystemValue(316, "[]");
        CHRISTMAS_317 = ActParamTabLoader.getDateValue(317, "2100-01-20 23:59:59");
        CHRISTMAS_318 = ActParamTabLoader.getListIntSystemValue(318, "[]");

        ACT_COMBAT_DOUBLE_NUM = ActParamTabLoader.getIntegerSystemValue(ActParamId.ACT_COMBAT_DOUBLE_NUM, 2);
        ACT_COMBAT_DROP = ActParamTabLoader.getListListIntSystemValue(ActParamId.ACT_COMBAT_DROP, "[[4,5011,1,400]]");
        ACT_BANDIT_DRAWING = ActParamTabLoader.getIntegerSystemValue(ActParamId.ACT_BANDIT_DRAWING, 2);
        ACT_BANDIT_MOVE = ActParamTabLoader.getIntegerSystemValue(ActParamId.ACT_BANDIT_MOVE, 2);
        ACT_BANDIT_RES = ActParamTabLoader.getIntegerSystemValue(ActParamId.ACT_BANDIT_RES, 2);
        ACT_BANDIT_ACCE = ActParamTabLoader.getListListIntSystemValue(ActParamId.ACT_BANDIT_ACCE, "[[]]");
        ACT_CITY_DRAWING = ActParamTabLoader.getIntegerSystemValue(ActParamId.ACT_CITY_DRAWING, 2);
        ACT_BANDIT_BADGE = ActParamTabLoader.getListListIntSystemValue(ActParamId.ACT_BANDIT_BADGE, "[[]]");
        ACT_MAGIC_PROP_IDS_IN_CHAT = ActParamTabLoader.getListListIntSystemValue(ActParamId.ACT_MAGIC_PROP_IDS_IN_CHAT, "[[]]");
        if (!CheckNull.isEmpty(ACT_BANDIT_BADGE_MAP)) {
            ACT_BANDIT_BADGE_MAP.clear();
        }
        if (!CheckNull.isEmpty(ACT_BANDIT_BADGE)) {
            for (List<Integer> badge : ACT_BANDIT_BADGE) {
                for (int i = badge.get(0); i <= badge.get(1); i++) {
                    if (CheckNull.isEmpty(ACT_BANDIT_BADGE_MAP)) {
                        ACT_BANDIT_BADGE_MAP = new HashMap<>();
                    }
                    List<List<Integer>> lists = ACT_BANDIT_BADGE_MAP.get(i);
                    if (CheckNull.isEmpty(lists)) {
                        lists = new ArrayList<>();
                    }
                    List<Integer> badgeList = new ArrayList<>();
                    badgeList.add(badge.get(2));
                    badgeList.add(badge.get(3));
                    badgeList.add(badge.get(4));
                    badgeList.add(badge.get(5));
                    lists.add(badgeList);
                    ACT_BANDIT_BADGE_MAP.put(i, lists);
                }
            }
        }
        ACT_GESTAPO_LEVEL = ActParamTabLoader.getListIntSystemValue(ActParamId.ACT_GESTAPO_LEVEL, "[]");
        ACT_GESTAPO_EXCHANG_RES = ActParamTabLoader.getListListIntSystemValue(ActParamId.ACT_GESTAPO_EXCHANG_RES,
                "[[]]");

        BLACKHAWK_FREE_COUNT = ActParamTabLoader.getIntegerSystemValue(ActParamId.BLACKHAWK_FREE_COUNT, 4);
        BLACKHAWK_REFRESH_INTERVAL = ActParamTabLoader.getIntegerSystemValue(ActParamId.BLACKHAWK_REFRESH_INTERVAL,
                18000);
        BLACKHAWK_INIT_PAY_GOLD = ActParamTabLoader.getIntegerSystemValue(ActParamId.BLACKHAWK_INIT_PAY_GOLD, 10);
        BLACKHAWK_INCR_GOLD = ActParamTabLoader.getIntegerSystemValue(ActParamId.BLACKHAWK_INCR_GOLD, 10);
        BLACKHAWK_HERO_ID = ActParamTabLoader.getIntegerSystemValue(ActParamId.BLACKHAWK_HERO_ID, 0);
        BLACKHAWK_NEED_TOKEN = ActParamTabLoader.getIntegerSystemValue(ActParamId.BLACKHAWK_NEED_TOKEN, 7);

        SUPPLY_DORP_GOLD = SystemTabLoader.getListIntSystemValue(SystemId.SUPPLY_DORP_GOLD, "[500,500,500]");

        OPEN_ACT_GROW_NEED = ActParamTabLoader.getListIntSystemValue(ActParamId.OPEN_ACT_GROW_NEED, "[]");

        ACT_FIRSH_CHARGE_TIME = ActParamTabLoader.getIntegerSystemValue(ActParamId.ACT_FIRSH_CHARGE_TIME, 604800);
        ACT_BLACK_TIME = ActParamTabLoader.getIntegerSystemValue(ActParamId.ACT_BLACK_TIME, 345600);

        PAY_VIP_COEFF = ActParamTabLoader.getIntegerSystemValue(ActParamId.PAY_VIP_COEFF, 10);
        ACT_DOUBLE_NUM = ActParamTabLoader.getIntegerSystemValue(ActParamId.ACT_DOUBLE_NUM, 2);
        ACT_PRODUCTTION_NUM = ActParamTabLoader.getIntegerSystemValue(ActParamId.ACT_PRODUCTTION_NUM, 5000);
        ACT_ALL_CHARGE_REWARD = SystemTabLoader.getListListIntSystemValue(ActParamId.ACT_ALL_CHARGE_REWARD, "[[]]");
        ACT_BRAVEST_ARMY_AWARD = ActParamTabLoader.getListListIntSystemValue(ActParamId.ACT_BRAVEST_ARMY_AWARD, "[[]]");
        ACT_COLLECT_REWARD = ActParamTabLoader.getIntegerSystemValue(ActParamId.ACT_COLLECT_REWARD, 5000);
        ACT_RECRUIT_REWARD = ActParamTabLoader.getIntegerSystemValue(ActParamId.ACT_RECRUIT_REWARD, 10000);
        ACT_ALL_CHARGE_LORD_LV = ActParamTabLoader.getIntegerSystemValue(ActParamId.ACT_ALL_CHARGE_LORD_LV, 40);
        ACT_VIP_LORD_LV = ActParamTabLoader.getIntegerSystemValue(ActParamId.ACT_VIP_LORD_LV, 40);
        ACT_GESTAPO_RECOVER_ARMY_NUM = ActParamTabLoader.getIntegerSystemValue(ActParamId.ACT_GESTAPO_RECOVER_ARMY_NUM,
                7000);
        ACT_ATK_CITY_LEVEL = ActParamTabLoader.getListIntSystemValue(ActParamId.ACT_ATK_CITY_LEVEL, "[]");
        ACT_GESTAPO_CAMP_RANK_LEVEL = ActParamTabLoader.getIntegerSystemValue(ActParamId.ACT_GESTAPO_CAMP_RANK_LEVEL,
                60);
        ACT_PROP_PROMOTION_AWARD_NUM = ActParamTabLoader.getIntegerSystemValue(ActParamId.ACT_PROP_PROMOTION_AWARD_NUM,
                100);
        ACT_TURNPLATE_GOLD_AWRAD = ActParamTabLoader.getListIntSystemValue(ActParamId.ACT_TURNPLATE_GOLD_AWRAD, "[]");
        EQUIP_TURNPLATE_GOLD_AWRAD = ActParamTabLoader.getListIntSystemValue(ActParamId.EQUIP_TURNPLATE_GOLD_AWRAD,
                "[]");
        EQUIP_TURNPLATE_GLOBAL_NUM = ActParamTabLoader.getListIntSystemValue(ActParamId.EQUIP_TURNPLATE_GLOBAL_NUM,
                "[]");
        EQUIP_TURNPLATE_SEND_CHAT_AWARD = ActParamTabLoader.getListListIntSystemValue(ActParamId.EQUIP_TURNPLATE_SEND_CHAT_AWARD,
                "[[]]");
        ACT_TURNPLATE_EXCHANGE_AWRAD = ActParamTabLoader.getListIntSystemValue(ActParamId.ACT_TURNPLATE_EXCHANGE_AWRAD,
                "[]");
        ACT_PAY_TURNPLATE_PAY_GOLD = ActParamTabLoader.getListListIntSystemValue(ActParamId.ACT_PAY_TURNPLATE_PAY_GOLD,
                "[[]]");
        ACT_GIFT_PROMOTION_WEEKOFMONTH_WEEK_DURATION = ActParamTabLoader
                .getListListIntSystemValue(ActParamId.ACT_GIFT_PROMOTION_WEEKOFMONTH_WEEK_DURATION, "[[2,4],[5],[3]]");
        PAY_RANKINGS_CONDITION = ActParamTabLoader.getIntegerSystemValue(ActParamId.PAY_RANKINGS_CONDITION, 88);
        TROOPS_RANKINGS_CONDITION = ActParamTabLoader.getIntegerSystemValue(ActParamId.TROOPS_RANKINGS_CONDITION, 88);
        CITY_WAR_RANKINGS_CONDITION = ActParamTabLoader.getIntegerSystemValue(ActParamId.CITY_WAR_RANKINGS_CONDITION,
                88);
        CAMP_WAR_RANKINGS_CONDITION = ActParamTabLoader.getIntegerSystemValue(ActParamId.CAMP_WAR_RANKINGS_CONDITION,
                88);
        BUIDING_RANKINGS_CONDITION = ActParamTabLoader.getIntegerSystemValue(ActParamId.BUIDING_RANKINGS_CONDITION, 88);
        REFORM_RANKINGS_CONDITION = ActParamTabLoader.getIntegerSystemValue(ActParamId.REFORM_RANKINGS_CONDITION, 88);
        FORGE_RANKINGS_CONDITION = ActParamTabLoader.getIntegerSystemValue(ActParamId.FORGE_RANKINGS_CONDITION, 88);
        SUPPLY_RANKINGS_CONDITION = ActParamTabLoader.getIntegerSystemValue(ActParamId.SUPPLY_RANKINGS_CONDITION, 88);
        ORE_RANKINGS_CONDITION = ActParamTabLoader.getIntegerSystemValue(ActParamId.ORE_RANKINGS_CONDITION, 88);
        GIFT_RANKINGS_CONDITION = ActParamTabLoader.getIntegerSystemValue(ActParamId.GIFT_RANKINGS_CONDITION, 88);
        LUCKY_TRUNPLATE_RANKINGS_CONDITION = ActParamTabLoader
                .getIntegerSystemValue(ActParamId.LUCKY_TRUNPLATE_RANKINGS_CONDITION, 88);
        EQUIP_TRUNPLATE_RANKINGS_CONDITION = ActParamTabLoader
                .getIntegerSystemValue(ActParamId.EQUIP_TRUNPLATE_RANKINGS_CONDITION, 88);
        ACT_MONOPOLY_CNT_WEIGHT = SystemTabLoader.getListListIntSystemValue(ActParamId.ACT_MONOPOLY_CNT_WEIGHT, "[[]]");
        ACT_CAMP_RANK = ActParamTabLoader.getIntegerSystemValue(ActParamId.ACT_CAMP_RANK, 10000);
        ORE_TRUNPLATE_GOLD = ActParamTabLoader.getListListIntSystemValue(ActParamId.ORE_TRUNPLATE_GOLD, "[[]]");
        ORE_TURNPLATE_GOLD_AWRAD_NEW = ActParamTabLoader.getListIntSystemValue(ActParamId.ORE_TURNPLATE_GOLD_AWRAD_NEW,
                "[]");
        PAY_RANKINGS_CONDITION_NEW = ActParamTabLoader.getIntegerSystemValue(ActParamId.PAY_RANKINGS_CONDITION_NEW, 88);
        FAMOUS_GENERAL_TURNPLATE_GOLD_AWRAD = ActParamTabLoader
                .getListIntSystemValue(ActParamId.FAMOUS_GENERAL_TURNPLATE_GOLD_AWRAD, "[]");
        FAMOUS_GENERAL_TRUNPLATE_RANKINGS_CONDITION = ActParamTabLoader
                .getIntegerSystemValue(ActParamId.FAMOUS_GENERAL_TRUNPLATE_RANKINGS_CONDITION, 20);
        ACT_FAMOUS_GENERAL_EXCHANGE_AWRAD = ActParamTabLoader
                .getListListIntSystemValue(ActParamId.ACT_FAMOUS_GENERAL_EXCHANGE_AWRAD, "[[]]");
        ACT_CAMP_FIGHT_RANK_JOIN_COND = ActParamTabLoader
                .getListIntSystemValue(ActParamId.ACT_CAMP_FIGHT_RANK_JOIN_COND, "[50,10000]");
        ACT_CAMP_FIGHT_WIN_CAMP_AWARD = ActParamTabLoader
                .getListListIntSystemValue(ActParamId.ACT_CAMP_FIGHT_WIN_CAMP_AWARD, "[[]]");
        ACT_WISHING_AWARD = ActParamTabLoader.getListListIntSystemValue(ActParamId.ACT_WISHING_AWARD, "[[]]");
        ACT_RED_PACKET = ActParamTabLoader.getListListIntSystemValue(ActParamId.ACT_RED_PACKET, "[[]]");
        ACT_CONSUME_GOLD_RANK = ActParamTabLoader.getIntegerSystemValue(ActParamId.ACT_CONSUME_GOLD_RANK, 2000);

        LUCKY_POOL_1 = ActParamTabLoader.getListIntSystemValue(ActParamId.ACT_LUCKY_POOL1, "[]");
        LUCKY_POOL_2 = ActParamTabLoader.getListListIntSystemValue(ActParamId.ACT_LUCKY_POOL2, "[[]]");
        // 戒指活动总共刷新次数
        BARTON_TOTAL_REFRESH_COUNT = ActParamTabLoader.getIntegerSystemValue(ActParamId.BARTON_TOTAL_REFRESH_COUNT, 10);
        // 戒指活动刷新花费金币
        BARTON_REFRESH_COST_GOLD = ActParamTabLoader.getIntegerSystemValue(ActParamId.BARTON_REFRESH_COST_GOLD, 50);
        ACT_TRIGGER_HERO_IGNORE = ActParamTabLoader.getListIntSystemValue(ActParamId.ACT_TRIGGER_HERO_IGNORE, "[]");
        ACT_TUTOR_RANK_CONF = ActParamTabLoader.getListListIntSystemValue(ActParamId.ACT_TUTOR_RANK_CONF, "[[]]");
        ACT_TUTOR_CONDITION_RANK = ActParamTabLoader.getIntegerSystemValue(ActParamId.ACT_TUTOR_CONDITION_RANK, 100);
        ACT_NOTICE_NUM = ActParamTabLoader.getListListIntSystemValue(ActParamId.ACT_NOTICE_NUM, "[[]]");

        // 阵营对拼活动
        ROYAL_ARENA_COUNTRY_STYLE = ActParamTabLoader.getListListIntSystemValue(ActParamId.ROYAL_ARENA_COUNTRY_STYLE, "[[]]");
        ROYAL_ARENA_DETECT_STYLE = ActParamTabLoader.getListListIntSystemValue(ActParamId.ROYAL_ARENA_DETECT_STYLE, "[[]]");
        ROYAL_ARENA_COUNTRY_STYLE_CONSUME = ActParamTabLoader.getIntegerSystemValue(ActParamId.ROYAL_ARENA_COUNTRY_STYLE_CONSUME, 0);
        ROYAL_ARENA_DETECT_STYLE_CONSUME = ActParamTabLoader.getIntegerSystemValue(ActParamId.ROYAL_ARENA_DETECT_STYLE_CONSUME, 0);
        ROYAL_ARENA_COUNTRY_STYLE_CNT = ActParamTabLoader.getListListIntSystemValue(ActParamId.ROYAL_ARENA_COUNTRY_STYLE_CNT, "[[]]");
        ROYAL_ARENA_DETECT_STYLE_CNT = ActParamTabLoader.getListListIntSystemValue(ActParamId.ROYAL_ARENA_DETECT_STYLE_CNT, "[[]]");
        ROYAL_ARENA_TASK_CNT = ActParamTabLoader.getListListIntSystemValue(ActParamId.ROYAL_ARENA_TASK_CNT, "[[]]");
        ROYAL_ARENA_TASK_PRICE = ActParamTabLoader.getListListIntSystemValue(ActParamId.ROYAL_ARENA_TASK_PRICE, "[[]]");
        ROYAL_ARENA_TASK_FREE_REFRESH_CNT = ActParamTabLoader.getIntegerSystemValue(ActParamId.ROYAL_ARENA_TASK_FREE_REFRESH_CNT, 0);
        ROYAL_ARENA_TASK_REFRESH_PRICE = ActParamTabLoader.getIntegerSystemValue(ActParamId.ROYAL_ARENA_TASK_REFRESH_PRICE, 0);
        ROYAL_ARENA_TASK_ENDTIME = ActParamTabLoader.getListListIntSystemValue(ActParamId.ROYAL_ARENA_TASK_ENDTIME, "[[]]");
        ACT_ROYAL_ARENA_RANK = ActParamTabLoader.getListListIntSystemValue(ActParamId.ACT_ROYAL_ARENA_RANK, "[[]]");
        BUILD_GIFT_CHARGE_TIME = ActParamTabLoader.getIntegerSystemValue(ActParamId.BUILD_GIFT_CHARGE_TIME, 604800);

        ACT_GOLD_LUCK_SEND_CHAT_AWARD = ActParamTabLoader.getListListIntSystemValue(ActParamId.ACT_GOLD_LUCK_SEND_CHAT_AWARD,
                "[[]]");
        ACT_DEDICATED_CUSTOMER_SERVICE_CONF = ActParamTabLoader.getListListIntSystemValue(ActParamId.ACT_DEDICATED_CUSTOMER_SERVICE_CONF, "[[]]");
        ACT_WECHAT_PAY = ActParamTabLoader.getListListIntSystemValue(ActParamId.ACT_WECHAT_PAY_CONF, "[[]]");

        ACT_VISIT_ALTAR_FREE_CNT = ActParamTabLoader.getIntegerSystemValue(ActParamId.ACT_VISIT_ALTAR_FREE_CNT, 3);
        ACT_VISIT_ALTAR_GOLD_CNT = ActParamTabLoader.getIntegerSystemValue(ActParamId.ACT_VISIT_ALTAR_GOLD_CNT, 30);
        ACT_VISIT_ALTAR_GOLD_CONSUME = ActParamTabLoader.getListListIntSystemValue(ActParamId.ACT_VISIT_ALTAR_GOLD_CONSUME, "[[1,5,50],[6,10,100],[11,30,200]]");
        ACT_VISIT_OIL_CONSUME = ActParamTabLoader.getListListIntSystemValue(ActParamId.ACT_VISIT_OIL_CONSUME, "[[2,1,20000]]");
        ACT_VISIT_GOLD_CONSUME = ActParamTabLoader.getListListIntSystemValue(ActParamId.ACT_VISIT_GOLD_CONSUME, "[[1,3,120]]");
        ACT_VISIT_REFRESH_CRON = ActParamTabLoader.getStringSystemValue(ActParamId.ACT_VISIT_REFRESH_CRON, "0 0 0,14,18,22 * * ? ");
        ACT_AUCTION_TIME_LIST = ActParamTabLoader.getListListIntSystemValue(ActParamId.ACT_AUCTION_TIME, "[[1,10],[2,16],[3,21],[4,10],[5,16],[6,21]]");
        ACT_AUCTION_ROUND_PERIOD = ActParamTabLoader.getIntegerSystemValue(ActParamId.ACT_AUCTION_ROUND_PERIOD, 18000);
        HIGHEST_Bid_COUNT = ActParamTabLoader.getIntegerSystemValue(ActParamId.HIGHEST_Bid_COUNT, 15);
        ACT_AUCTION_ABOUT_TO_END = ActParamTabLoader.getIntegerSystemValue(ActParamId.ACT_AUCTION_ABOUT_TO_END, 180);
        List<List<Integer>> ACT_AUCTION_TYPE_HIGHEST_COUNT_LIST = ActParamTabLoader.getListListIntSystemValue(ActParamId.ACT_AUCTION_TYPE_HIGHEST_COUNT, "[[]]");
        ACT_AUCTION_TYPE_HIGHEST_COUNT = new HashMap<>();
        ACT_AUCTION_TYPE_HIGHEST_COUNT_LIST.forEach(list -> {
            if (list.isEmpty())
                return;
            ACT_AUCTION_TYPE_HIGHEST_COUNT.put(list.get(0), list.get(1));
        });

        ACT_GOLDEN_AUTUMN_AWARD_UPPER_LIMIT = ActParamTabLoader.getIntegerSystemValue(ActParamId.ACT_GOLDEN_AUTUMN_AWARD_UPPER_LIMIT, 99);
        ACT_GOLDEN_AUTUMN_AWARD_SOWING = ActParamTabLoader.getIntegerSystemValue(ActParamId.ACT_GOLDEN_AUTUMN_AWARD_SOWING, 100);
        ACT_GOLDEN_AUTUMN_RANDOM_AWARD_LIBRARY = ActParamTabLoader.getListListIntSystemValue(ActParamId.ACT_GOLDEN_AUTUMN_random_AWARD_Library, "[[]]");
        ACT_GOLDEN_AUTUMN_SEED_FRUIT_PROPORTION = ActParamTabLoader.getListListIntSystemValue(ActParamId.ACT_GOLDEN_AUTUMN_SEED_FRUIT_PROPORTION, "[[]]");
        ACT_FAMOUS_GENERAL_TURNTABLE_GUARANTEE = ActParamTabLoader.getListIntSystemValue(ActParamId.ACT_FAMOUS_GENERAL_TURNTABLE_GUARANTEE, "[]");
    }

    /*-------------------------------id值-------------------------------------*/

    /**
     * 活动配置Id常量值
     */
    public static final class ActParamId {

        public static final int MUSIC_CRT_OFFICE_TASK_COUNT_BY_DIFFICULT = 367;
        public static final int MUSIC_CRT_OFFICE_PARAMS = 366;

        /**
         * 副本活动翻倍倍数
         */
        public static final int ACT_COMBAT_DOUBLE_NUM = 1;
        /**
         * 副本掉落
         */
        public static final int ACT_COMBAT_DROP = 2;
        /**
         * 匪军图纸
         */
        public static final int ACT_BANDIT_DRAWING = 3;
        /**
         * 匪军迁城
         */
        public static final int ACT_BANDIT_MOVE = 4;
        /**
         * 匪军资源
         */
        public static final int ACT_BANDIT_RES = 5;
        /**
         * 匪军加速
         */
        public static final int ACT_BANDIT_ACCE = 6;
        /**
         * 图纸活动
         */
        public static final int ACT_CITY_DRAWING = 7;
        /**
         * 采集翻倍
         */
        public static final int ACT_COLLECT_REWARD = 8;
        /**
         * 制造翻倍
         */
        public static final int ACT_RECRUIT_REWARD = 9;
        /**
         * 全军返利/勇冠三军最小活动等级
         */
        public static final int ACT_ALL_CHARGE_LORD_LV = 10;
        /**
         * 精英部队最小活动等级
         */
        public static final int ACT_VIP_LORD_LV = 11;
        /**
         * 匪军徽章
         */
        public static final int ACT_BANDIT_BADGE = 12;
        /**
         * 盖世太保等级限制
         */
        public static final int ACT_GESTAPO_LEVEL = 13;
        /**
         * 盖世太保等级限制
         */
        public static final int ACT_GESTAPO_EXCHANG_RES = 14;
        /**
         * 盖世太保兵力返还比例
         */
        public static final int ACT_GESTAPO_RECOVER_ARMY_NUM = 15;
        /**
         * 攻城掠地活动等级配置
         */
        public static final int ACT_ATK_CITY_LEVEL = 16;

        /**
         * 盖世太保排行阵营奖励
         */
        public static final int ACT_GESTAPO_CAMP_RANK_LEVEL = 17;

        /**
         * 军备促销积分箱累计额度
         */
        public static final int ACT_PROP_PROMOTION_AWARD_NUM = 18;

        /**
         * 幸运转盘金币抽奖奖励
         */
        public static final int ACT_TURNPLATE_GOLD_AWRAD = 19;

        /**
         * 幸运转盘,碎片兑换奖励
         */
        public static final int ACT_TURNPLATE_EXCHANGE_AWRAD = 20;

        /**
         * 装备转盘金币抽奖奖励
         */
        public static final int EQUIP_TURNPLATE_GOLD_AWRAD = 21;

        /**
         * 装备转盘全服抽奖次数
         */
        public static final int EQUIP_TURNPLATE_GLOBAL_NUM = 22;

        /**
         * 充值排行-上榜需充值的额度
         */
        public static final int PAY_RANKINGS_CONDITION = 23;

        /**
         * 兵力排行-上榜需战斗损兵
         */
        public static final int TROOPS_RANKINGS_CONDITION = 24;

        /**
         * 攻城战排行-上榜需攻城战次数
         */
        public static final int CITY_WAR_RANKINGS_CONDITION = 25;

        /**
         * 营地战排行-上榜需营地站次数
         */
        public static final int CAMP_WAR_RANKINGS_CONDITION = 26;

        /**
         * 建设排行-上榜需建设站次数
         */
        public static final int BUIDING_RANKINGS_CONDITION = 27;

        /**
         * 改造排行-上榜需改造次数
         */
        public static final int REFORM_RANKINGS_CONDITION = 28;

        /**
         * 打造排行-上榜需打造次数
         */
        public static final int FORGE_RANKINGS_CONDITION = 29;

        /**
         * 补给排行-上榜需补给获得量
         */
        public static final int SUPPLY_RANKINGS_CONDITION = 30;

        /**
         * 矿石排行-上榜需矿石获得量
         */
        public static final int ORE_RANKINGS_CONDITION = 31;

        /**
         * 礼物排行-上榜需礼物积分
         */
        public static final int GIFT_RANKINGS_CONDITION = 32;

        /**
         * 幸运转盘排行-上榜所需积分
         */
        public static final int LUCKY_TRUNPLATE_RANKINGS_CONDITION = 33;

        /**
         * 装备转盘排行-上榜所需积分
         */
        public static final int EQUIP_TRUNPLATE_RANKINGS_CONDITION = 34;
        /**
         * 矿石转盘-抽取消耗配置
         */
        public static final int ORE_TRUNPLATE_GOLD = 35;
        /**
         * 棋盘点数权重
         */
        public static final int ACT_MONOPOLY_CNT_WEIGHT = 36;
        /**
         * 阵营排行
         **/
        public static final int ACT_CAMP_RANK = 37;

        /**
         * 锆石转盘金币抽奖奖励
         */
        public static final int ORE_TURNPLATE_GOLD_AWRAD_NEW = 38;

        /**
         * 新充值排行-上榜需充值的额度
         */
        public static final int PAY_RANKINGS_CONDITION_NEW = 39;

        /**
         * 名将转盘金币抽奖奖励
         */
        public static final int FAMOUS_GENERAL_TURNPLATE_GOLD_AWRAD = 40;

        /**
         * 名将转盘排行-上榜所需积分
         */
        public static final int FAMOUS_GENERAL_TRUNPLATE_RANKINGS_CONDITION = 41;

        /**
         * 名将转盘,碎片兑换奖励
         */
        public static final int ACT_FAMOUS_GENERAL_EXCHANGE_AWRAD = 42;
        /**
         * 阵营排行中战力最高的阵营奖励，没有对应档次时默认第一个，填写格式[[活动档次1,奖励1],[活动档次2，奖励2],[活动档次3，奖励3]...]
         */
        public static final int ACT_CAMP_FIGHT_WIN_CAMP_AWARD = 44;
        /**
         * 阵营战斗力排行 进榜条件[最低指挥官等级，最低战力要求]
         */
        public static final int ACT_CAMP_FIGHT_RANK_JOIN_COND = 45;
        /**
         * 许愿池额外奖励
         */
        public static final int ACT_WISHING_AWARD = 46;
        /**
         * 红包活动发送的红包id
         */
        public static final int ACT_RED_PACKET = 47;
        /**
         * 消耗排行上榜所需额度
         */
        public static final int ACT_CONSUME_GOLD_RANK = 50;
        /**
         * 幸运奖池配置
         */
        public static final int ACT_LUCKY_POOL1 = 48;
        /**
         * 幸运奖池配置
         */
        public static final int ACT_LUCKY_POOL2 = 49;
        /**
         * 触发式礼包,将领触发,忽略这两个将领
         */
        public static final int ACT_TRIGGER_HERO_IGNORE = 51;
        /**
         * 装备转盘需要发送跑马灯的装备
         */
        public static final int EQUIP_TURNPLATE_SEND_CHAT_AWARD = 52;

        /**
         * 导师排行的配置[[minLv, maxLv, integral], ...]
         */
        public static final int ACT_TUTOR_RANK_CONF = 53;
        /**
         * 导师排行-上榜条件
         */
        public static final int ACT_TUTOR_CONDITION_RANK = 54;

        /**
         * 活动广播最大消息个数[[activityId,maxNum], ...]
         */
        public static final int ACT_NOTICE_NUM = 55;

        /**
         * 戒指活动总共刷新次数
         */
        public static final int BARTON_TOTAL_REFRESH_COUNT = 110;
        /**
         * 戒指活动刷新花费金币
         */
        public static final int BARTON_REFRESH_COST_GOLD = 111;

        /**
         * 黑鹰计划免费次数
         */
        public static final int BLACKHAWK_FREE_COUNT = 120;
        /**
         * 黑鹰计划刷新时间(单位秒)
         */
        public static final int BLACKHAWK_REFRESH_INTERVAL = 121;
        /**
         * 黑鹰计划刷新初始金额
         */
        public static final int BLACKHAWK_INIT_PAY_GOLD = 122;
        /**
         * 黑鹰计划每次刷新递增额度
         */
        public static final int BLACKHAWK_INCR_GOLD = 123;
        /**
         * 黑鹰计划招募将领ID
         */
        public static final int BLACKHAWK_HERO_ID = 124;
        /**
         * 黑鹰计划招募将领信物所需个数
         */
        public static final int BLACKHAWK_NEED_TOKEN = 125;

        /**
         * 空降补给的|资源|军备|物资|补给花费黄金
         */
        public static final int SUPPLY_DORP_GOLD = 132;
        /**
         * 黑鹰计划持续时间
         */
        public static final int ACT_BLACK_TIME = 160;

        /**
         * 购买成长计划要求[VIP等级，金币]
         */
        public static final int OPEN_ACT_GROW_NEED = 156;

        /**
         * 首充礼包持续时间
         */
        public static final int ACT_FIRSH_CHARGE_TIME = 159;

        /**
         * 充值金额对应VIP经验系数
         */
        public static final int PAY_VIP_COEFF = 164;
        /**
         * 翻倍奖励活动的倍数
         */
        public static final int ACT_DOUBLE_NUM = 173;
        /**
         * 生产加速活动加速减少
         */
        public static final int ACT_PRODUCTTION_NUM = 195;
        /**
         * 全军返利第一名阵营玩家奖励
         */
        public static final int ACT_ALL_CHARGE_REWARD = 196;
        /**
         * 充值转盘档位
         */
        public static final int ACT_PAY_TURNPLATE_PAY_GOLD = 198;
        /**
         * 特惠礼包活动, 每月的第周 周几 持续几天 [[2,4],[5],[3]]
         */
        public static final int ACT_GIFT_PROMOTION_WEEKOFMONTH_WEEK_DURATION = 199;

        /**
         * 阵营活动大国风范效果 [[提升百分比],[获得贡献值]]
         */
        public static final int ROYAL_ARENA_COUNTRY_STYLE = 202;
        /**
         * 阵营活动刺探效果 [[减少贡献值],[获得贡献值]]
         */
        public static final int ROYAL_ARENA_DETECT_STYLE = 203;

        /**
         * 阵营活动大国风范钻石消耗 每次消耗的钻石
         */
        public static final int ROYAL_ARENA_COUNTRY_STYLE_CONSUME = 204;

        /**
         * 阵营活动刺探钻石消耗 每次消耗的钻石
         */
        public static final int ROYAL_ARENA_DETECT_STYLE_CONSUME = 205;

        /**
         * 阵营活动大国风范购买次数 [[个人次数],[阵营次数]]
         */
        public static final int ROYAL_ARENA_COUNTRY_STYLE_CNT = 206;

        /**
         * 阵营活动刺探购买次数 [[个人次数],[阵营次数]]
         */
        public static final int ROYAL_ARENA_DETECT_STYLE_CNT = 207;

        /**
         * 阵营任务购买次数 [[免费次数], [可购买次数]]
         */
        public static final int ROYAL_ARENA_TASK_CNT = 208;

        /**
         * 阵营任务购买价格 [[首次购买价格], [每次递增]]
         */
        public static final int ROYAL_ARENA_TASK_PRICE = 209;

        /**
         * 阵营活动免费刷新次数 每个任务免费刷新次数
         */
        public static final int ROYAL_ARENA_TASK_FREE_REFRESH_CNT = 210;

        /**
         * 阵营活动任务刷新价格
         */
        public static final int ROYAL_ARENA_TASK_REFRESH_PRICE = 211;

        /**
         * 阵营活动任务时间 [[一星时间],[递增时间]],秒
         */
        public static final int ROYAL_ARENA_TASK_ENDTIME = 212;
        /**
         * 阵营排行的上榜条件
         */
        public static final int ACT_ROYAL_ARENA_RANK = 213;

        /**
         * 勇冠三军第一名阵营玩家奖励
         */
        public static final int ACT_BRAVEST_ARMY_AWARD = 214;

        /**
         * 建造礼包持续时间
         */
        public static final int BUILD_GIFT_CHARGE_TIME = 215;

        /**
         * 好运道中奖广播
         */
        public static final int ACT_GOLD_LUCK_SEND_CHAT_AWARD = 302;

        /**
         * vip客服活动条件
         */
        public static final int ACT_DEDICATED_CUSTOMER_SERVICE_CONF = 303;
        /**
         * 微信充值活动条件
         */
        public static final int ACT_WECHAT_PAY_CONF = 304;

        /**
         * 领主每日免费与圣坛互动上限
         */
        public static final int ACT_VISIT_ALTAR_FREE_CNT = 332;
        /**
         * 领主每日可购买的互动次数上限
         */
        public static final int ACT_VISIT_ALTAR_GOLD_CNT = 333;
        /**
         * 每阶段购买互动次数所需钻石数量
         */
        public static final int ACT_VISIT_ALTAR_GOLD_CONSUME = 334;
        /**
         * 领主使用黄金参观所需黄金数量
         */
        public static final int ACT_VISIT_OIL_CONSUME = 335;
        /**
         * 领主使用钻石拜访所需钻石数量
         */
        public static final int ACT_VISIT_GOLD_CONSUME = 336;
        /**
         * 圣坛刷新的crontab表达式 0 0 0,14,18,22 * * ?
         */
        public static final int ACT_VISIT_REFRESH_CRON = 337;

        /**
         * 皮肤返场活动玩家可购买次数
         */
        public static final int ACT_SKIN_ENCORE_BUY_COUNT_ID = 340;

        public static final int ACT_FIRE_WORK_ID = 351;

        /**
         * 秋季拍卖回合时间
         */
        public static final int ACT_AUCTION_TIME = 354;
        /**
         * 秋季拍卖回合持续时间
         */
        public static final int ACT_AUCTION_ROUND_PERIOD = 359;
        /**
         * 喜悦金秋获得随机奖励数量上限
         */
        public static int ACT_GOLDEN_AUTUMN_AWARD_UPPER_LIMIT = 355;

        /**
         * 喜悦金秋获得随机奖励所需播种
         */
        public static int ACT_GOLDEN_AUTUMN_AWARD_SOWING = 356;

        /**
         * 喜悦金秋获随机奖励库
         */
        public static int ACT_GOLDEN_AUTUMN_random_AWARD_Library = 357;

        /**
         * 喜悦金秋获种子果实比例
         */
        public static int ACT_GOLDEN_AUTUMN_SEED_FRUIT_PROPORTION = 358;

        /**
         * 秋季拍卖 最高加持有次数
         */
        public static final int HIGHEST_Bid_COUNT = 362;

        /**
         * 拍卖即将结束跑马灯时间
         */
        public static final int ACT_AUCTION_ABOUT_TO_END = 363;

        /**
         * 拍卖活动类型最高出价次数
         */
        public static final int ACT_AUCTION_TYPE_HIGHEST_COUNT = 364;

        /**
         * 宝具转盘打造增加转盘次数
         */
        public static final int ACT_MAGIC_TREASURE_WARE_TURNTABLE_CNT = 375;
        /**
         * 宝具打造增加宝具排行分数
         */
        public static final int ACT_MAGIC_TREASURE_WARE_RANK_SCORE = 376;
        /**
         * 神兵宝具广播
         */
        public static final int ACT_MAGIC_PROP_IDS_IN_CHAT = 378;

        /**
         * 张飞转盘道具保底规则
         */
        public static int ACT_FAMOUS_GENERAL_TURNTABLE_GUARANTEE = 379;

        /**
         * 王朝遗迹活动，遗迹建筑刷新的时刻0 0 7, 15,23 * * ?
         */
        public static final int ACT_RELIC_REFRESH = 400;
        /**
         * 王朝遗迹活动，遗迹建筑刷新后的保护期和探索期[600,1800,2]
         */
        public static final int ACT_RELIC_STAMP = 401;

        /**
         * 王朝遗迹活动，每个进攻兵团的最大战斗场数
         */
        public static final int ACT_RELIC_ARMY_FIGHT_MAX = 402;
        /**
         * 王朝遗迹活动，伤病恢复系数
         */
        public static final int ACT_RELIC_RECOVERY_RATIO = 404;
        /**
         * 王朝遗迹连续击杀广播
         */
        public static final int ACT_RELIC_CHAT_KILL_BROADCAST = 405;
        /**
         * 王朝遗迹榜一奖励
         */
        public static final int ACT_RELIC_MAX_SCORE_PLAYER_WARD = 384;

        public static final int ACT_RELIC_MARCH_SPEEDUP = 406;
    }

}
