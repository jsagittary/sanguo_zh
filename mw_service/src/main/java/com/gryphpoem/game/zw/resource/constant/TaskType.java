package com.gryphpoem.game.zw.resource.constant;

public class TaskType {

    public static final int TYPE_MAIN = 1;// 主线
    public static final int TYPE_SUB = 2;// 支线
    public static final int TYPE_DAYIY = 3;// 日常
    public static final int TYPE_LIVE = 4;// 活跃
    public static final int TYPE_SECTION = 5;// 剧情任务
    public static final int TYPE_ADVANCE = 6;// 个人目标任务

    public static final int TYPE_STATUS_UNFINISH = 0;// 未完成
    public static final int TYPE_STATUS_FINISH = 1;// 已完成
    public static final int TYPE_STATUS_REWARD = 2;// 已领取奖励

    // 格式：条件，id，次数
    public static final int COND_LORD_LV = 1; // 玩家升级(无id，等级)
    public static final int COND_COMBAT_ID_WIN = 2; // 通关某个关卡(关卡ID，次数)
    public static final int COND_BANDIT_LV_CNT = 3; // 击败野怪(野怪等级，次数), 野怪等级0 表示任意等级
    public static final int COND_ARM_CNT = 4; // 募兵达到次数(无id，次数)
    public static final int COND_ARM_TYPE_CNT = 5; // 兵数量(兵种，数量)

    public static final int COND_CLICK = 7; // 点击屏幕特定位置
    public static final int COND_OTHER_TASK_CNT = 8; // 完成支线任务的数量
    public static final int COND_FIGHT_SHOW = 9; // 通过演示战斗
    public static final int COND_TECH_LV = 10; // 研究指定科技到指定等级
    public static final int COND_BUILDING_TYPE_LV = 11; // 建筑类型到多少级(建筑类型，等级)
    public static final int COND_RES_FOOD_CNT = 12; // 供应站(等级， 数量)
    public static final int COND_RES_OIL_CNT = 13; // 炼油厂(等级， 数量)
    public static final int COND_RES_ELE_CNT = 14; // 发电厂(等级， 数量)
    public static final int COND_RES_ORE_CNT = 15; // 矿石精炼厂(等级， 数量)
    public static final int COND_BATTLE_STATE_LV_CNT = 16; // 参与国战攻下任意1座地图据点(等级， 次数)0 表示任意等级
    public static final int COND_BATTLE_CITY_LV_CNT = 17; // 参与城战击飞任意3座玩家城池(等级，次数)0 表示任意等级

    /** 累计采集铀矿xx小时 */
    public static final int COND_WORLD_WAR_MINE_CNT = 18;
    /** 招募兵力xx */
    public static final int COND_RECRUIT_ARMS_CNT = 19;
    /** 获得xx补给数量 */
    public static final int COND_PLUNDER_RESOURCE_CNT = 20;

    public static final int COND_18 = 18;// 建筑开始升级就算完成，不要求立升级结束
    public static final int COND_19 = 19;// 开始研究科技就算完成，不用等到研究结束(科技类型ID， 次数)
    public static final int COND_20 = 20;// 前往世界或者已经停留在世界
    public static final int COND_21 = 21;// 前往基地或者已经停留在基地
    public static final int COND_22 = 22;// 参与NPC城战斗 ，不论胜负(cityType, 次数)
    public static final int COND_24 = 24;// 查看建筑通关关卡，关卡ID
    public static final int COND_25 = 25;// 打开某个界面就算完成任务，界面ID
    public static final int COND_26 = 26;// 获得指定武将，武将ID
    public static final int COND_27 = 27;// 获得某一类武将，武将类型
    public static final int COND_28 = 28;// 上阵某一类武将，武将类型
    public static final int COND_29 = 29;// 开始打造装备，就算完成，不用立即完成打造
    public static final int COND_30 = 30;// 雇佣科技馆研究员(>=等级 次数)
    public static final int COND_31 = 31;// 创建角色名才算完成
    public static final int COND_32 = 32;// 给一个或多个武将的指定部位穿装备，身上有装备也算完成(任意将领的部位,装备数量)
    public static final int COND_33 = 33;// 科技馆研究员加速
    public static final int COND_ENTER_COMBAT_34 = 34;// 进入副本战斗就算完成任务，战斗胜负结果不论
    public static final int COND_BUILDING_ID_LV = 35; // 建筑Id到多少级(建筑id，等级)
    public static final int COND_LOGIN_36 = 36; // 每日登陆
    public static final int COND_COMBAT_37 = 37; // 通关任意关卡次数(0,次数)
    public static final int COND_CAMP_BUILD_38 = 38; // 阵营建设次数(0,次数)
    public static final int COND_RECRUIT_ARMY_39 = 39; // (预留) 募兵数量 (兵种(0:任意兵种),数量)
    public static final int COND_BUY_ACT_40 = 40; // 够买体力次数(0,次数)
    public static final int COND_JOIN_CAMP_BATTLE_41 = 41; // 参加阵营战的次数(是否胜利(0:无论胜利失败,1:必须胜利), 次数)
    public static final int COND_WASH_HERO_42 = 42; // 将领特训次数(0,特训次数)
    public static final int COND_BUILDING_UP_43 = 43; // 开始升级建筑次数(0,次数)
    public static final int COND_TECH_UP_44 = 44; // 开始研究科技次数(0,次数)
    public static final int COND_MONTH_CARD_STATE_45 = 45; // 是否处于月卡状态(0,1)
    public static final int COND_PAY_46 = 46; // 充值任意金额(0,1)
    public static final int COND_STONE_COMBAT_47 = 47; // 通关任意宝石副本(0,次数)
    public static final int COND_STONE_COMPOUND_COMBAT_48 = 48; // 宝石合成次数(0,次数)
    public static final int COND_STONE_HOLE_49 = 49; // 已镶嵌的宝石的数量(0,数量)
    public static final int COND_ATTCK_PLAYER_CNT = 50; // 参与城战攻打任意座玩家城池,不用击飞(等级，次数)0 表示任意等级

    public static final int COND_PITCHCOMBAT_PASS = 51;// 通关指定ID的荣耀演习场关卡,取的最高的通关关卡 (演练场type,combatId)
    public static final int COND_PITCHCOMBAT_PASS_CNT = 52;// 通关几次荣耀演习场关卡 不限关卡ID (演练场type,次数)
    public static final int COND_MENTOR_EQUIP_GEARORDER = 53;// 装备指定gearorder的教官装备,已装备也算 (装备type,gearOrder)
    public static final int COND_MENTOR_SKILL_UPLV = 54;// 升级指定教官技能至XX级,已经升级也算 (技能type,技能等级)
    public static final int COND_MENTOR_UPLV = 55;// 升级教官等级至XX级,已经升级也算 (教官type,lv)

    public static final int COND_MENTOR_UPLV_CNT = 56;// 升级教官N次,升级次数(教官type,次数)
    public static final int COND_SEARCH_PLANE_HIGH = 57;// 进行N次高级战机开发(0,次数)
    public static final int COND_SEARCH_PLANE_LOW = 58;// 进行N次低级战机开发(0,次数)
    public static final int COND_FITTING_PLANE_ID = 59;// 装备指定ID的飞机(飞机type,1)

    // 激活蝎王的前置任务
    public static final int COND_SCORPION_ACTIVATE_PREPOSITION = 60;
    // 激活蝎王
    public static final int COND_SCORPION_ACTIVATE = 61;
    // 每日任务活跃度
    public static final int COND_DAILY_LIVENSS = 62;
    // 获取资源的数量
    public static final int COND_RESOURCE_CNT = 63;
    // 消耗金币
    public static final int COND_SUB_GOLD_CNT = 64;
    // 宝石副本购买次数
    public static final int COND_STONE_COMBAT_BUY_CNT = 65;
    // 消耗兵种
    public static final int COND_SUB_HERO_ARMY = 66;
    // 获得指定数量的钻石
    public static final int COND_GOLD_CNT = 67;
    // 参与指定次数的匪军叛乱
    public static final int COND_REBEL_ATK_CNT = 68;
    // 打造大于指定品质的装备
    public static final int COND_HERO_EQUIPID_QUALITY = 69;
    // 每日在大型资源点采集x次
    public static final int COND_SUPER_MINE_CNT = 70;
    // 每日建设x次都城
    public static final int COND_UP_CITY_CNT = 71;
    // 己方阵营，获得圣域争夺战的胜利x次
    public static final int COND_BERLIN_WIN_CNT = 72;
    // 己方阵营，占领x次投石车
    public static final int COND_BERLIN_FRONT_CNT = 73;
    // 累计拥有X个觉醒英雄
    public static final int COND_HERO_DECORATED_HAVE_CNT = 74;
    // 宴请X次英雄（私人宴请和国际宴请次数都算）
    public static final int COND_SEARCH_HERO_CNT = 75;
    // 获得军功数量X
    public static final int COND_EXPLOIT_CNT = 76;
    // 特工解锁
    public static final int COND_UNLOCK_AGENT = 77;
    // 特工约会
    public static final int COND_APPOINTMENT_AGENT = 78;
    // 特工互动
    public static final int COND_INTERACTION_AGENT = 79;
    // 特工送礼
    public static final int COND_PRESENT_GIFT_AGENT = 80;
    // 将指定HeroId的英雄提升至指定等级
    public static final int COND_DESIGNATED_HERO_ID_UPGRADE = 81;
    // 将指定品质的英雄提升至指定等级
    public static final int COND_DESIGNATED_HERO_QUALITY_UPGRADE = 82;
    //垂钓大师
    public static final int COND_FISHING_MASTER = 83;
    //领取宝具挂机奖励
    public static final int COND_GET_TREASURE_WARE_HOOK_AWARD = 84;
    //打造宝具次数
    public static final int COND_TREASURE_WARE_MAKE_COUNT = 85;

    public static final int COND_FREE_CD = 100; // 使用免费加速功能
    public static final int COND_RES_AWARD = 101; // 使用征收功能
    public static final int COND_EQUIP = 102; // 给武将穿装备，武将ID和装备部位ID(双重条件)
    public static final int COND_EQUIP_BUILD = 103; // 打造装备，打造装备ID和打造数量
    public static final int COND_TREASURE = 104; // 使用聚宝盆功能1次
    public static final int COND_HERO_UP = 105; // 武将上阵(将领ID不填表示任意, 次数)
    public static final int COND_FACTORY_RECRUIT = 106; // 为雇佣军工厂军事专家 军事专家(>=等级 次数)
    public static final int COND_EQUIP_SPEED = 107; // 装备加速打造 次数
    public static final int COND_COMMAND_ADD = 108; // 雇佣天气控制仪 天气控制仪等级(>=等级 次数)
    public static final int COND_EQUIP_BAPTIZE = 110; // 装备改造 次数
    public static final int COND_SUPER_EQUIP = 111; // 国器升级 国器ID >=等级
    public static final int COND_CHEMICAL = 112; // 化工厂 生产次数
    public static final int COND_HERO_EQUIPID = 113; // 给武将穿装备，穿戴指定装备ID
    public static final int COND_USE_PROP = 114;// 使用指定道具
    public static final int COND_ONLINE_AWARD_CNT = 115; // 领取在线奖励次数

    //新增任务
    public static final int COND_500 = 500;//研究科技X等级LV.X
    public static final int COND_501 = 501;//有至少x名英雄同时装备绿品质及以上的x装备
    public static final int COND_502 = 502;//所有英雄穿戴中的装备共有x条x级及以上攻击改造属性
    public static final int COND_503 = 503;//所有英雄穿戴中的装备共有x条x级及以上任意改造属性
    public static final int COND_504 = 504;//上阵英雄一共穿戴x件x品质及以上的装备
    public static final int COND_505 = 505;//任意一名英雄穿戴x件x品质及以上的装备
    public static final int COND_506 = 506;//累计获得x个x道具
    public static final int COND_507 = 507;//改名1次
    public static final int COND_508 = 508;//提升x建筑容量至x
    public static final int COND_509 = 509;//同时上阵x名x品质及以上的战斗英雄
    public static final int COND_510 = 510;//同时上阵x名x品质及以上的采集英雄英雄
    public static final int COND_511 = 511;//x名英雄的资质达到x
    public static final int COND_512 = 512;//x名英雄的攻击资质达到x
    public static final int COND_513 = 513;//累计完成x次x任务
    public static final int COND_514 = 514;//拥有x个x级及以上的英雄
    public static final int COND_515 = 515;//与x名佳人好感度达到x
    public static final int COND_516 = 516;//x名英雄战力分别达到x
    public static final int COND_517 = 517;//分别将x名英雄突破到x
    public static final int COND_519 = 519;//有至少x名英雄同时装备蓝品质及以上的x装备
    public static final int COND_520 = 520;//在国战中击杀x名敌军
    public static final int COND_521 = 521;//参与x次x类型国战
    public static final int COND_522 = 522;//完成x次精英叛军每日首杀
    //    public static final int COND_524 = 524;//完成指定关卡的帝国远征   同47，524废弃
    public static final int COND_525 = 525;//通关x次宝具关卡
    public static final int COND_527 = 527;//给英雄装备宝具
    public static final int COND_528 = 528;//进行x次宝具强化
    public static final int COND_529 = 529;//进行x次宝具洗炼
    public static final int COND_530 = 530;//在宝具远征中挑战玩家x次
    public static final int COND_531 = 531;//通关指定的宝具关卡
    public static final int COND_532 = 532;//将x个宝具强化到x级
    public static final int COND_533 = 533;//累计在宝具远征中战胜玩家x次
    public static final int COND_534 = 534;//上阵的x位英雄全部装备宝具
    public static final int COND_535 = 535;//获得x件x及以上品质的宝具
    public static final int COND_536 = 536;//获得x件带特技的x品质及以上的宝具


    public static final int COND_990 = 990;//完成x次寻访
    public static final int COND_991 = 991;//将任意x品质以上英雄升到x级 无需参数接取任务处理
    public static final int COND_992 = 992;//累计对x品质以上英雄进行x次突破
    public static final int COND_993 = 993;//累计对x品质以上英雄进行x次特训
    public static final int COND_994 = 994;//打造x品质装备x件
    public static final int COND_995 = 995;//累计挑战或扫荡战役x次
    public static final int COND_996 = 996;//累计参与阵营战x次

    // 世界任务
    public static final int WORLD_TASK_TYPE_BANDIT = 1; // 打流寇
    public static final int WORLD_TASK_TYPE_CITY = 2; // 打城市
    public static final int WORLD_TASK_TYPE_BOSS = 3; // 世界boss

    /** 第一个Boss死亡后通知的任务ID */
    public static final int WORLD_BOSS_TASK_ID_1 = 5;//
    /** 第二个Boss死亡后通知的任务ID */
    public static final int WORLD_BOSS_TASK_ID_2 = 9;//


    public static final int SECOND_FIRST_ID = 9;
    public static final int SECOND_SECTION_ID = 11;

    /** 被动更新任务类型数组，需要主动调用更新任务方法才会更新任务状态 */
    public static final int[] UNACTIVE_UPDATE_TASK = { COND_COMBAT_ID_WIN, COND_BANDIT_LV_CNT, COND_ARM_CNT,
            COND_ARM_TYPE_CNT, COND_BUILDING_TYPE_LV, COND_BATTLE_STATE_LV_CNT, COND_BATTLE_CITY_LV_CNT, COND_18,
            COND_19, COND_22, COND_26, COND_27, COND_28, COND_29, COND_30, COND_31, COND_33, COND_FREE_CD,
            COND_RES_AWARD, COND_EQUIP, COND_EQUIP_BUILD, COND_TREASURE, COND_HERO_UP, COND_FACTORY_RECRUIT,
            COND_EQUIP_SPEED, COND_COMMAND_ADD, COND_EQUIP_BAPTIZE, COND_CHEMICAL };
}
