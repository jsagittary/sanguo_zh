package com.gryphpoem.game.zw.resource.constant;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ActivityConst {

    public static final int CLEAN_DAY = 1;// 每日清理
    public static final int CLEAN_RETAIN = 2;// 保留清理

    /** DISPLAY-OPEN阶段,活动未开启 */
    public static final int DISPLAY_OPEN = -2;//预显示
    /** BEGIN之前 ,活动未开启 */
    public static final int OPEN_CLOSE = -1;//关闭
    /** BEGIN-END阶段 */
    public static final int OPEN_STEP = 0;//开启中
    /** END-DISPLAY阶段 ,不能更新活动进度 */
    public static final int OPEN_AWARD = 1;//显示期

    // 0可以领取 1进度未达到 2天数未达到 3已领取
    public static final int ACT_7_STATUS_AWARD = 0;// 可以领取
    public static final int ACT_7_STATUS_NO_SCHEDULE = 1;// 进度未达到
    public static final int ACT_7_STATUS_NO_DAY = 2;// 天数未达到
    public static final int ACT_7_STATUS_HAS_GAIN = 3;// 已领取

    /** 活动领奖状态 **/
    public static final int CAN_NOT_AWARD = 0;// 不可领奖
    public static final int CAN_AWARD = 1;// 可领奖

    public static final int ASC = 0; // 小到大排序
    public static final int DESC = 1; // 大道小排序

    /** 攻城掠地 **/
    public static final int INIT_STATUS = 1; // 目标进度初始值

    /** 闪电战消息推送类型 */
    public static final int ACT_BEGIN = 1;
    public static final int ATTACK_CNT = 2;
    public static final int ACT_END = 3;
    public static final int ACT_ANN = 4;// 活动预告

    /* 活动类型 */
    /** 首充礼包 */
    public static final int ACT_FIRSH_CHARGE = 1;// 首充礼包 首次充值领取奖励
    public static final int ACT_7DAY = 2;// 七日狂欢 新建角色开启为期7天的活动
    public static final int ACT_BLACK = 3;// 黑鹰计划（原七星拜将） 新建角色开启为期4天的活动
    public static final int ACT_COMMAND_LV = 4;// 基地升级（原主城升级） 永久活动，升级基地领取奖励
    public static final int ACT_ATTACK_CITY = 5;// 攻占据点（原攻城掠地） 永久活动，攻打据点领取奖励
    public static final int ACT_LEVEL = 6;// 成长基金（原成长计划） 永久活动，V4花1000黄金购买，升级领取黄金
    public static final int ACT_VIP_BAG = 7;// 特价礼包 充值购买特价礼包
    public static final int ACT_FOOD = 8;// 能量赠送（原出师大宴） 每日固定两个时间段领取额外能量
    public static final int ACT_VIP = 9;// 精英部队（原大咖带队） 全服VIP达到指定数量，全服玩家领取奖励
    public static final int ACT_ALL_CHARGE = 10;// 全军返利 全服累计充值领取奖励，充值最高军团领取额外奖励

    public static final int ACT_ATK_GESTAPO = 12;// 攻打盖世太保 击杀匪军搜集召唤券召唤盖世太保
    public static final int ACT_ATTACK_CITY_NEW = 13;// 新攻城掠地活动 活动期间完成攻打敌军或敌军据点活跃任务，领取活跃度奖励
    public static final int ACT_LIGHTNING_WAR = 14;// 闪电战活动 开服第3天晚8~10点开启的自动攻城战，击败匪军BOSS
    public static final int ACT_PAY_7DAY = 15; // 七日充值
    public static final int ACT_PROP_PROMOTION = 16;// 军备促销 开服第1~7天，全服玩家金币购买打折道具，积累阵营积分，积分每达到一定额度阵营玩家领取1次奖励
    public static final int ACT_GESTAPO_RANK = 17;// 盖世太保杀敌排行
    public static final int ACT_WAR_ROAD = 19;// 战火试炼 通关战火试炼，征战大陆
    public static final int ACT_GIFT_PROMOTION = 20;// 礼物特惠
    public static final int ACT_MONOPOLY = 21;// 大富翁活动
    public static final int ACT_CAMP_RANK = 22;// 开服阵营排行
    public static final int ACT_THREE_REBATE = 23; // 三倍返利活动
    public static final int ACT_CAMP_FIGHT_RANK = 24;// 阵营战斗力排行
    public static final int ACT_WISHING_WELL = 25;// 许愿池活动
    public static final int ACT_SIGIN = 26;// 签到
    public static final int ACT_RED_PACKET = 27;// 红包活动
    public static final int ACT_EXCHANGE_PROP = 28;// 兑换活动
    public static final int ACT_BARTON_DISCOUNT = 29;// 巴顿打折购买活动
    public static final int ACT_ANNIVERSARY_DATE_GIFT = 30;// 一周年特卖礼包
    public static final int ACT_BANDIT_AWARD = 31;// 剿灭匪军或击飞敌军可获得奖章，奖章可到活动页面兑换奖励
    public static final int ACT_SIGN_IN_NEW = 32;//新签到活动

    // 战机开发
    public static final int ACT_WAR_PLANE_SEARCH = 33;
    // 罗宾汉活动
    public static final int ACT_ROBIN_HOOD = 35;
    // 建筑礼包
    public static final int ACT_BUILD_GIFT = 36;
    // 阵营比拼
    public static final int ACT_ROYAL_ARENA = 37;
    // 复活节活动
    public static final int ACT_EASTER = 38;
    // 勇冠三军
    public static final int ACT_BRAVEST_ARMY = 39;
    // 国庆节签到活动, 复用的32的签到活动
    public static final int ACT_SIGN_IN_NATIONAL = 40;
    // 中秋好运道活动
    public static final int ACT_GOOD_LUCK = 41;
    //圣诞活动
    public static final int ACT_CHRISTMAS = 42;
    //赶年兽活动
    public static final int ACT_MONSTER_NIAN = 43;
    //貂蝉降临
    public static final int ACT_DIAOCHAN = 44;
    //掉落控制
    public static final int ACT_DROP_CONTROL = 45;


    public static final int ACT_MERGE_SIGN_IN = 46;// 合服签到(参考40)
    public static final int ACT_MERGE_CHARGE_CONTINUE = 47;// 合服每日连续充值(参考111)
    public static final int ACT_MERGE_PAY_RANK = 48;// 合服充值排行(参考112)
    public static final int ACT_MERGE_PROP_PROMOTION = 49;// 合服特卖(参考 16)

    public static final int ACT_REPAIR_CASTLE = 50;//修缮城堡
    public static final int ACT_SEASON_HERO = 51;   //赛季英雄活动 44换皮

    public static final int ACT_DRAGON_BOAT_EXCHANGE = 52;//端午活动兑换

    public static final int ACT_SUMMER_CHARGE = 53;//夏日充值
    public static final int ACT_SUMMER_TURNPLATE = 54;//夏日转盘
    public static final int ACT_SUMMER_CASTLE = 55;//夏日沙雕
    public static final int ACT_VISIT_ALTAR = 56;//斋月---拜访圣坛

    public static final int ACT_ANNIVERSARY_TURNTABLE = 57;//周年转盘
    public static final int ACT_ANNIVERSARY_FIREWORK = 58;//周年烟花
    public static final int ACT_ANNIVERSARY_JIGSAW = 59;//周年拼图
    public static final int ACT_ANNIVERSARY_SKIN = 60;//周年皮肤返场
    public static final int ACT_ANNIVERSARY_EGG = 61;//周年彩蛋

    public static final int ACT_GOLDEN_AUTUMN_FARM = 62;//喜悦金秋
    /**
     * 秋季拍卖
     */
    public static final int ACT_AUCTION = 63;

    /**
     * 音乐节活动-售票处
     */
    public static final int ACT_MUSIC_FESTIVAL_BOX_OFFICE = 64;
    /**
     * 音乐节活动-创作室
     */
    public static final int ACT_MUSIC_FESTIVAL_CREATIVE_OFFICE = 65;

    //2022新年活动
    public static final int ACT_NEWYEAR_2022_LONGLIGHT = 66;
    public static final int ACT_NEWYEAR_2022_FIREWORK = 67;
	public static final int ACT_NEWYEAR_2022_FISH = 68;
	public static final int ACT_FIRST_PAY_RESET = 69;//首充重置

    public static final int ACT_TREASURE_WARE_JOURNEY = 71;//宝具征程
    public static final int ACT_MAGIC_TREASURE_WARE = 72;//神兵宝具

    public static final int ACT_GIFT_PAY = 101;// 充值有礼 累计充值指定黄金领取奖励
    public static final int ACT_COST_GOLD = 102;// 消费有礼 累计消费指定黄金领取奖励
    public static final int ACT_PAY_RANK = 103;// 充值排行 根据充值参与排名领取超值奖励！活动首日19:00开始按排名计算奖励，活动结束后未领取奖励将以邮件形式补发
    public static final int ACT_SUPPLY_DORP = 104;// 空降补给（原屯田计划） 花费黄金购买，每天登录领取奖励，领完五次奖励后返还消耗的黄金
    public static final int ACT_DAILY_PAY = 105;// 每日充值 每天充值达到指定金额领取奖励，完成前一档才能激活后一档
    public static final int ACT_FREE_LUXURY_GIFTS = 107;// 免费豪礼 第二日登录，免费领取紫色装备
    public static final int ACT_DAY_DISCOUNTS = 108;// 每日特惠活动
    // public static final int ACT_GIFT_DISCOUNTS = 109;// 礼物特惠
    public static final int ACT_PAY_RANK_NEW = 109;// 充值排行-新
                                                   // (被策划强行变成了合服排行)根据充值参与排名领取超值奖励！活动首日19:00开始按排名计算奖励，活动结束后未领取奖励将以邮件形式补发
    public static final int ACT_CHARGE_TOTAL = 110;// 累计充值
    public static final int ACT_CHARGE_CONTINUE = 111;// 连续充值
    public static final int ACT_PAY_RANK_V_3 = 112;// 充值排行的第3个版本了(与109一样)
                                                   // 根据充值参与排名领取超值奖励！活动首日19:00开始按排名计算奖励，活动结束后未领取奖励将以邮件形式补发
    public static final int ACT_REAL_NAME = 114;  //实名认证活动
    public static final int ACT_PHONE_BINDING = 115; //手机绑定
    public static final int ACT_WECHAT = 116; // 微信公众号
    public static final int ACT_DEDICATED_CUSTOMER_SERVICE = 117; // 专属客服
    public static final int ACT_WECHAT_PAY = 118; // 微信公众号充值
    public static final int ACT_WECHAT_SIGNIN = 119; // 微信公众号签到

    public static final int ACT_LOGIN_EVERYDAY = 200;// 每日登陆活动
    public static final int ACT_LUCKY_TURNPLATE = 201;// 神秘装备（原幸运罗盘）
    public static final int ACT_PAY_TURNPLATE = 203;// 充值转盘
    public static final int ACT_ORE_TURNPLATE = 204;// 矿石转盘
    public static final int ACT_EQUIP_TURNPLATE = 205;// 装备转盘
    public static final int ACT_ORE_TURNPLATE_NEW = 206;// 锆石转盘
    public static final int FAMOUS_GENERAL_TURNPLATE = 207;// 名将转盘
    public static final int ACT_LUCKY_POOL = 208;// 幸运奖池
    public static final int ACT_LUCKY_TURNPLATE_NEW = 209; // 幸运转盘(转到指定次数可以有额外的奖励)
    public static final int ACT_HOT_PRODUCT = 210;// 热销商品
    public static final int ACT_LUCKY_TURNPLATE_NEW_YEAR = 211;//新春转盘
    public static final int ACT_SEASON_TURNPLATE = 212;//赛季转盘，同209类型，赛季专用
    public static final int ACT_AUTUMN_TURNPLATE = 213;//丰收转盘
    public static final int ACT_HELP_SHENGYU = 215;//助力圣域

    public static final int ACT_ONLINE_GIFT = 301;// 在线奖励 每日在线指定时长可领取奖励,(领完前一档后才能激活下一档哟!)
    public static final int ACT_GIFT_OL = 305;// 在线奖励 每日在线指定时长领取奖励

    public static final int ACT_ARMY_RANK = 401;// 兵力排行 根据战斗损兵的数量排名，领取奖励
    public static final int ACT_CAMP_BATTLE_RANK = 402;// 团战排行 根据参加进攻据点并发生战斗（国战）的次数排名，领取奖励
    public static final int ACT_CITY_BATTLE_RANK = 403;// 城战排行 根据发起进攻敌军基地并发生战斗（城战）的次数排名，领取奖励
    public static final int ACT_PARTY_BUILD_RANK = 404;// 建设排行 根据阵营建设次数参与排行领取超值奖励
    public static final int ACT_REMOULD_RANK = 405;// 改造排行（原洗炼排行） 根据装备改造次数参与排名领取超值奖励
    public static final int ACT_FORGE_RANK = 406;// 打造排行（原锻造排行） 根据装备制造次数参与排名领取超值奖励
    public static final int ACT_SUPPLY_RANK = 407;// 补给排行（原屯粮排行） 根据获得补给数量参与排名领取超值奖励
    public static final int ACT_ORE_RANK = 408;// 矿石排行（原屯铁排行） 根据获得矿石数量参与排名领取超值奖励
    public static final int ACT_PRESENT_GIFT_RANK = 409;// 情报部送礼排行
    public static final int ACT_CONSUME_GOLD_RANK = 410;// 消费排行活动
    public static final int ACT_TUTOR_RANK = 411;// 导师排行

    public static final int ACT_CHALLENGE_COMBAT = 412; // 挑战战役
    public static final int ACT_TRAINED_SOLDIERS = 413; // 训练士兵
    public static final int ACT_BIG_KILL = 414; // 大杀四方
    public static final int ACT_COLLECT_RESOURCES = 415; // 采集资源
    public static final int ACT_RESOUCE_SUB = 416; // 物资消耗
    public static final int ACT_EQUIP_MATERIAL = 417; // 装备物资
    public static final int ACT_ELIMINATE_BANDIT = 418; // 消灭匪军
    public static final int ACT_TRAINED_SOLDIERS_DAILY = 419; // 训练士兵(每日清除进度)
    public static final int ACT_WAR_ROAD_DAILY = 420;// 战火试炼 通关战火试炼，征战大陆(每日清除进度)


    /** 副本活动翻倍 */
    public static final int ACT_COMBAT_DOUBLE_REWARD = 501;// 副本活动翻倍
    public static final int ACT_CITY_DRAWING = 503;// 图纸活动 据点生产图纸的CD时间减半
    public static final int ACT_PRODUCTTION_EXPEDITE = 504;// 生产加速 活动期间，化工厂生产材料的时间减半。
    public static final int ACT_COMBAT_DROP = 505;// 关卡掉落 挑战或扫荡关卡有概率额外获得道具
    public static final int ACT_BANDIT_ACCE = 506;// 匪军加速 消灭武装力量有概率使建筑升级、科技研发、募兵获得5分钟的加速
    public static final int ACT_BANDIT_DRAWING = 507;// 匪军图纸 消灭武装力量可获得双倍的图纸
    public static final int ACT_BANDIT_MOVE = 508;// 匪军迁城 消灭武装力量可获得双倍的迁城道具
    public static final int ACT_BANDIT_RES = 509;// 匪军资源 消灭武装力量可获得额外50%的资源
    public static final int ACT_COLLECT_REWARD = 510;// 采集翻倍 采集获得的资源增加50%
    public static final int ACT_RECRUIT_REWARD = 511;// 制造翻倍（原作坊加速） 兵营每分钟募兵数量增加100%
    public static final int ACT_SHARE_REWARD = 523;// 分享好友 通过分享活动邀请好友进入游戏，到达对应的人数即可领取奖励
    public static final int ACT_QUESTIONNAIRE = 524;    //问卷调查活动类型

    public static final int ACT_DOUBLE_REWARD = 999;// 活动翻倍

    //**************************跨服活动**************************
    public static final int CROSS_ACT_RECHARGE_RANK = 2001;//跨服充值排名活动

    /** 结束还持续显示的活动 */
    public static final int[] ACT_END_DISPLAY_ARRAY = new int[] {
            ACT_SUPPLY_DORP,
            ACT_PAY_RANK_NEW,
            ACT_PAY_RANK_V_3,
            ACT_MERGE_PAY_RANK,
            ACT_CONSUME_GOLD_RANK,
            ACT_TUTOR_RANK,
            ACT_BRAVEST_ARMY,
            ACT_CHRISTMAS,ACT_REPAIR_CASTLE,
            ACT_DIAOCHAN,ACT_SEASON_HERO,
            CROSS_ACT_RECHARGE_RANK};

    /**
     * 是不是结束还持续显示的活动
     *
     * @param actType
     * @return
     */
    public static boolean isEndDisplayAct(int actType) {
        // return Arrays.binarySearch(ACT_END_DISPLAY_ARRAY, actType) >= 0;
        return Arrays.stream(ActivityConst.ACT_END_DISPLAY_ARRAY).boxed().anyMatch(at -> at.equals(actType));
    }

    /**
     * 是不是兑换道具活动
     * @param activityType
     * @return
     */
    public static boolean isExchangePropAct(int activityType) {
        return ActivityConst.ACT_EXCHANGE_PROP == activityType || ActivityConst.ACT_ANNIVERSARY_DATE_GIFT == activityType || ActivityConst.ACT_BANDIT_AWARD == activityType;
    }

    // public static final int ACT_RANK_COMBAT = 16;// 秘密武器（原幸运罗盘） 待定
    // public static final int ACT_17 = 17;// 疯狂开采（原镔铁转盘） 待定
    // public static final int ACT_18 = 18;// 军团鼓舞（强国策） 待定
    // public static final int ACT_19 = 19;// 充值排行 根据充值金额排名，领取奖励
    // public static final int ACT_22 = 22;// 洗炼排行 根据装备洗炼的次数排名，领取奖励
    // public static final int ACT_23 = 23;// 建设排行 根据升级建筑的次数排名，领取奖励
    // public static final int ACT_24 = 24;// 打造排行（原锻造排行） 根据打造指定品质及以上装备的次数排名，领取奖励
    // public static final int ACT_25 = 25;// 兵力排行 根据战斗损兵的数量排名，领取奖励
    // public static final int ACT_26 = 26;// 矿石排行（原屯铁排行） 根据获得矿石的数量排名，领取奖励
    // public static final int ACT_27 = 27;// 补给排行（原屯粮排行） 根据获得补给的数量排名，领取奖励
    // public static final int ACT_28 = 28;// 双倍经验 挑战或扫荡关卡获得经验翻倍（除图纸、国器碎片关卡外）

    /* ======================== 活动任务类型,对应数据库的s_task_type  ========================*/
    // 登录
    public static final int ACT_TASK_LOGIN = 1;
    // 指挥官等级
    public static final int ACT_TASK_LEVEL = 2;
    // 招募 雇佣
    public static final int ACT_TASK_RECRUIT = 3;
    // 完成某个科技
    public static final int ACT_TASK_TECH = 5;
    // 累计充值
    public static final int ACT_TASK_CHARGE = 6;
    // 参与攻下几座指定类型的城
    public static final int ACT_TASK_ATTACK = 7;
    // 所在地图本军团最多拥有几座指定类型的城
    public static final int ACT_TASK_CITY = 8;
    // 击杀几级以上武装力量
    public static final int ACT_TASK_ATK_BANDIT = 9;
    // 收集至少几件指定品质及以上的某部位装备
    public static final int ACT_TASK_EQUIP = 10;
    // 拥有至少几名某品质的将领
    public static final int ACT_TASK_HERO = 11;
    // 关卡进度
    public static final int ACT_TASK_COMBAT = 12;
    // 将领洗髓达到资质上限的将领数量，分品质
    public static final int ACT_TASK_HERO_WASH = 13;
    // 司令部升级
    public static final int ACT_TASK_BUILDING = 14;
    // 装备洗炼至满级
    public static final int ACT_TASK_EQUIP_WASH = 15;
    // 购买礼包
    public static final int ACT_TASK_VIP_BUY = 16;
    // 打造超级武器
    public static final int ACT_TASK_EQUIP_BUILD = 17;
    // 超级武器升级
    public static final int ACT_TASK_SUPER_EQUIP = 18;
    // 击飞玩家基地
    public static final int ACT_TASK_ATK = 19;
    // 战斗力
    public static final int ACT_TASK_FIGHT = 20;
    // 参与击飞几座至少多少级的玩家基地
    public static final int ACT_TASK_JOIN_OR_ATK = 21;
    // 参与几次军团战
    public static final int ACT_TASK_FIGHT_PARTY = 22;
    // 指定时间领取奖励 恢复体力
    public static final int ACT_TASK_ACT = 23;
    // 全服VIP几达到多少人
    public static final int ACT_TASK_ALL_VIP = 24;
    // 全服累计充值到多少金额
    public static final int ACT_TASK_ALL_CHARGE = 25;
    // 累计消费多少金额
    public static final int ACT_TASK_COST_GOLD = 26;
    // 第几天登录
    public static final int ACT_TASK_LOGIN_CNT = 27;
    // 第几天充值多少金额
    public static final int ACT_TASK_DAY_CHARGE = 28;
    // 在线多长时间
    public static final int ACT_TASK_ONLINE = 29;
    // 申请总督
    public static final int ACT_TASK_JOIN_CAMPAGIN = 30;
    // 城池征收
    public static final int ACT_TASK_CITY_LEVY = 31;
    // 防守本国城池
    public static final int ACT_TASK_JOIN_DEF = 32;
    // 协防阵营玩家
    public static final int ACT_TASK_JOIN_OR_DEF = 33;
    // 参与攻打据点
    public static final int ACT_TASK_JOIN_ATK = 34;
    // 参与并攻下敌对阵营的城（需要敌对阵营）
    public static final int ACT_TASK_JOIN_ATK_OTHER_CITY = 35;
    // 参与并攻打至少多少级的玩家基地
    public static final int ACT_TASK_ATK_AND_JOIN = 36;
    // 拜师
    public static final int ACT_TASK_MASTER = 37;
    // 收徒
    public static final int ACT_TASK_APPRENTICES = 38;
    // 完成xx次日常任务
    public static final int ACT_TASK_DAILY_TASK_CNT = 39;
    // 市场中翻盘xx次
    public static final int ACT_TASK_TREASURE_OPEN_CNT = 40;
    // 完成xx次战令任务
    public static final int ACT_TASK_BATTLE_PASS_TASK_CNT = 41;
    // 帝国征程通关x章节
    public static final int ACT_TASK_STONE_COMBAT_CNT = 42;
    // 拥有x个x级配饰(原宝石)
    public static final int ACT_TASK_STONE_CNT = 43;
    // 将领特训x次
    public static final int ACT_TASK_HERO_WASH_CNT = 44;
    // 船坞进行x次x材料交易
    public static final int ACT_TASK_CHEMICAL_RECRUIT_CNT = 45;
    // 完成x次阵营任务
    public static final int ACT_TASK_CAMP_TASK_FINSH_CNT = 46;
    // 装备改造xx次
    public static final int ACT_TASK_EQUIP_WASH_CNT = 47;
    // 军衔达到xx级
    public static final int ACT_TASK_PROMOTE_LV = 48;
    // 累积造兵xx数量
    public static final int ACT_TASK_ARM_TYPE_CNT = 49;
    // 大学升到xx等级
    public static final int ACT_TASK_UNIVERSITY_LV = 50;
    // 使用xx道具
    public static final int ACT_TASK_USE_PROP = 51;
    // 上阵将领指定部位(param里面填)穿戴xx品质的装备
    public static final int ACT_TASK_EQUIP_QUALITY_CNT = 52;
    // 宝石合成次数(0,次数)
    public static final int COND_STONE_COMPOUND_COMBAT = 53;
    // 提升至少几名指定品质的英雄至指定等级
    public static final int ACT_TASK_HERO_QUALITY_UPGRADE_CNT = 54;
    // 拥有几名指定星级的佳人
    public static final int ACT_TASK_AGENT_STAR_CNT = 55;
    // 击败指定等级的多人叛军x个
    public static final int ACT_TASK_MULTI_BANDIT_CNT = 56;
    /* ======================== 触发式礼包类型,对应数据库的s_trigger_rule  ========================*/
    public static final int TRIGGER_GIFT_HERO_WASH = 1;// 将领免费特训次数减为0
    public static final int TRIGGER_GIFT_EQUIP_BAPTIZE = 2;// 装备改造次数减为0
    public static final int TRIGGER_GIFT_CITY_LEVY = 3;// 征收城的图纸，双倍征收道具数量为0
    public static final int TRIGGER_GIFT_UPGRADE_BUILD = 4;// 建筑升级时间超过*小时
    public static final int TRIGGER_GIFT_DOCOMBAT_ACT = 5;// 攻打副本行动力不足
    public static final int TRIGGER_GIFT_DOCOMBAT_FAIL = 6;// 攻打副本失败(仅帐号注册前7天触发)
    public static final int TRIGGER_GIFT_FIRST_BY_HIT_FLY = 7;// 首次被击飞（离线情况下被击飞，则在上线时触发）
    public static final int TRIGGER_GIFT_REPLENISH_INSUFFICIENT = 8;// 补兵时兵力不足
    public static final int TRIGGER_GIFT_REBUILD = 9;// 重建家园
    /** 7级升级8级基地资源不足时触发 */
    public static final int TRIGGER_GIFT_REBUILD_7_8 = 10;
    /** 8级升级9级基地时触发 9级升级10级基地时触发*/
    public static final int TRIGGER_GIFT_REBUILD_8_9_10 = 11;
    /** 玩家获得第一个紫色品质将领时触发 玩家获得第一个橙色品质将领时触发 */
    public static final int TRIGGER_GIFT_FIRST_RARE_HERO = 12;
    /** 时间触发 */
    public static final int TRIGGER_GIFT_TIME_COND = 13;
    public static final int TRIGGER_GIFT_TREASURE_OPEN = 14;//市场翻牌触发礼包
    public static final int TRIGGER_GIFT_EXPEDITION_FAIL = 15;//帝国远征战斗失败（宝石副本）

    /**
     * 拥有将领授勋
     */
    public static final int TRIGGER_GIFT_HERO_DECORATED = 16;

    /**
     * 首次将领寻访
     */
    public static final int TRIGGER_GIFT_HERO_SEARCH = 17;

    /**
     * 通关副本
     */
    public static final int TRIGGER_GIFT_DO_COMBAT = 18;

    /**
     * 通关帝国远征副本
     */
    public static final int TRIGGER_GIFT_EMPIRE_EXPEDITION_COMBAT = 19;

    /**
     * 攻打指定type城池
     */
    public static final int TRIGGER_GIFT_ATK_CITY_SUC = 20;

    /**
     * 玩家达到指定等级后触发
     */
    public static final int TRIGGER_GIFT_ROLE_LEVEL = 21;

    /**
     * 根据礼包来触发
     *//*
    public static final int TRIGGER_GIFT_GIFT = 21;


    */
    /**
     * 根据最近的付费情况来触发
     *//*
    public static final int TRIGGER_GIFT_RECENTLY_PAID = 22;*/

    /**
     * 获得x等级的配饰 (卸下配饰不算)
     */
    public static final int TRIGGER_ADD_STONE = 23;

    /**
     * 配饰升星到x星
     */
    public static final int TRIGGER_STONE_IMPROVE_UP = 24;

    /**
     * 宝具副本通关某关卡
     */
    public static final int TRIGGER_DO_TREASURE_COMBAT = 25;

    /**
     * 打造某品质宝具
     */
    public static final int TRIGGER_MAKE_TREASURE_WARE = 26;

    /**
     * 强化某品质宝具到某等级
     */
    public static final int TRIGGER_STRENGTH_TREASURE_WARE = 27;

    /**
     * 获得某个英雄
     */
    public static final int TRIGGER_GET_ANY_HERO = 28;

    public static final int NOT_TRIGGER_STATUS = 0;// 未触发
    public static final int TRIGGER_STATUS = 1;// 触发
    public static final int STATUS_HAS_GAIN = 2;// 已领取

    public static final int LUCKY_TURNPLATE_FREE = 1;// 免费抽奖
    public static final int LUCKY_TURNPLATE_GOLD = 2;// 金币抽奖
    public static final int LUCKY_TURNPLATE_PROP = 3;// 道具抽奖
    public static final int LUCKY_TURNTABLE_ACT_EXCLUSIVE_TIMES = 4;    //活动专属次数

    /*----------------每日特惠的存储key说明----------------*/
    public static final int ACT_DAYDICOUNTS_LV_KEY = 1; // 每日特惠活动存储到statusCnt中的玩家等级的key
    public static final int ACT_DAYDICOUNTS_RANK_KEY = 2; // 每日特惠活动存储到statusCnt中的玩家军阶的key
    /*----------------每日特惠的存储key说明----------------*/

    /*----------------矿石转盘 statusMap key说明start----------------*/
    public static final int SURPLUS_NUM = 1;// 矿石转盘剩余次数
    public static final int ALREADY_NUM = 2;// 矿石转盘已转次数
    public static final int SURPLUS_GOLD = 3;// 矿石转盘当前档位已消费的金币数
    /*----------------矿石转盘 statusMap key说明end----------------*/

    /*----------------全军返利 saveMap key说明start----------------*/
    public static final int ACT_ALL_CHARGE_LORD_GOLD = 88;// 活动期间玩家充值的金币数
    /*----------------全军返利 saveMap key说明end----------------*/

    /*----------------特殊活动 type对应s_special_plan的 activityType start----------------*/
    public static final int SPECIAL_ACT_PLANE_LIMIT_SEARCH = 1;// 战机的限时卡池
    /*----------------特殊活动 end----------------*/

    /**
     * 活动期间内战机高级开发次数
     */
    public static final int ACT_WAR_PLANE_TYPE_1 = 1;
    /**
     * 新获得某品质的战机
     */
    public static final int ACT_WAR_PLANE_TYPE_2 = 2;
    /**
     * 已拥有某品质的战机
     */
    public static final int ACT_WAR_PLANE_TYPE_3 = 3;


    /**
     * 大富翁存储key值管理
     */
    public static interface ActMonopolyKey {
        /** 今日充值金币数(在进行下一轮时会清0) key值存储在statusCnt中 */
        public static final int STATUSCNT_TODAY_PAY_KEY = 1;
        /** 今日拥有玩色子的次数 值存储在statusCnt中 */
        public static final int STATUSCNT_HASCNT_KEY = 2;
        /** 今日已经玩色子的次数 值存储在statusCnt中 */
        public static final int STATUSCNT_PLAY_CNT_KEY = 3;
        /** 今日充值金币数总数(在进行下一轮时不会清除) key值存储在statusCnt中 */
        public static final int STATUSCNT_TODAY_ALL_PAY_KEY = 4;
        /** 当前在那一轮, 存储在saveMap中 */
        public static final int SAVEMAP_CUR_ROUND_KEY = -1;
        /** 当前在第格, 存储在saveMap中 */
        public static final int SAVEMAP_CUR_GRID_KEY = -2;
        /** 上一次当天首次充值的时间点, 存储在saveMap中 */
        public static final int SAVEMAP_LAST_PAY_TIME_KEY = -3;
    }

    public static interface ActWishingWellKey {
        /** 许愿池存储当前已许愿第几次的key值 */
        public static final int STATUSCNT_WISHING_CUR_CNT_KEY = 0;
        /** 额外奖励领取的key值 */
        public static final int STATUSMAP_WISHING_EXTRA_AWARD_KEY = 0;
    }

    /**
     * 热销商品的key值定义
     */
    public interface ActHotProduct {

        /**
         * 跟s_act_hot_product表里面的tab值, 1是购买, 2是消耗
         * (在进行下一轮时会清0)
         */
        int STATUS_BUY_COUNT = 1;
        int STATUS_SPEND_SUM = 2;
    }

    public static final int[] COMBINED_SERVICE_REMOVED_ACT_TYPE = new int[]{ActivityConst.ACT_MAGIC_TREASURE_WARE};
}
