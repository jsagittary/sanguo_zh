package com.gryphpoem.game.zw.resource.constant;

import java.util.List;

/**
 * @author TanDonghai
 * @ClassName SystemId.java
 * @Description s_system表中的id代表的含义，在这里统一描述
 * @date 创建时间：2017年3月22日 上午11:28:21
 */
public class SystemId {

    /**
     * 全区全服角色id统一长度
     */
    public static final int ROLE_ID_LEN = 1;

    /**
     * 将领装备栏总数
     */
    public static final int HERO_EQUIP_NUM = 2;

    /**
     * 将领上阵队列长度
     */
    public static final int HERO_BATTLE_LEN = 3;

    /**
     * 资源领取次数上限
     */
    public static final int RES_GAIN_MAX = 4;
    /**
     * 战机技能数
     */
    public static final int PLANE_SKILL_LEN = 5;
    // /** 兵营募兵基础兵力 */
    // public static int FACTORY_ARM_MIN = 5;
    // /** 兵营募兵最小单位时间秒 */
    // public static int FACTORY_ARM_MIN_TIME = 6;
    /** 兵营募兵初始队列 */
    // public static int FACTORY_ARM_INIT_QUE = 7;
    /**
     * 兵营扩建初始等级
     */
    public static int FACTORY_EXPAND_NINT_LEVL = 8;
    /**
     * 兵营加时初始等级
     */
    public static int FACTORY_TIME_NINT_LEVL = 9;
    /**
     * 初始关卡ID
     */
    public static int INIT_COMBAT_ID = 10;

    /**
     * 玩家开启世界地图功能的等级限制
     */
    public static final int OPEN_WORLD_LV = 11;

    // /** 角色等级上限 */
    // public static final int ROLE_MAX_LV = 12;

    /**
     * 玩家初始行军时间系数
     */
    public static final int MARCH_TIME_RATIO = 13;

    /**
     * 战斗计算躲避的概率，万分比
     */
    public static final int DODGE_PRO = 14;

    /**
     * 战斗计算暴击的概率，万分比
     */
    public static final int CRIT_PRO = 15;

    /**
     * 战斗暴击倍率，支持小数
     */
    public static final int CRIT_MULTI = 16;

    /**
     * 玩家邮件标题长度上限
     */
    public static final int MAIL_TITLE_LEN = 17;

    /**
     * 玩家邮件内容长度上限
     */
    public static final int MAIL_CONTENT_LEN = 18;

    /**
     * 聊天信息显示条数
     */
    public static final int MAX_CHAT_COUNT = 19;

    /**
     * 聊天信息长度限制
     */
    public static final int MAX_CHAT_LEN = 20;

    /**
     * 玩家注册登录奖励
     */
    public static final int REGISTER_REWARD = 21;
    /**
     * 最大洗练次数
     */
    public static final int EQUIP_MAX_BAPTIZECNT = 22;
    /**
     * 洗练一个小时增加一次
     */
    public static final int EQUIP_BAPTIZECNT_TIME = 23;
    /**
     * 金币洗练消耗
     */
    public static final int EQUIP_GOLD_BAPTIZE_1 = 24;
    /**
     * 3星满级金币洗练消耗
     */
    public static final int EQUIP_GOLD_BAPTIZE_2 = 25;

    /**
     * 奔袭战增加时间
     */
    public static final int RAID_BATTLE_TIME = 26;
    /**
     * 远征战增加时间
     */
    public static final int EXPEDITION_BATTLE_TIME = 27;
    /**
     * 城池最大产出数量
     */
    public static final int CITY_MAX_PRODUCE = 28;
    /**
     * 阵营战城池保护时间
     */
    public static final int CITY_FREE_TIME = 29;

    /**
     * 商城折扣商品随机个数
     */
    public static final int SHOP_OFF_RANDOM_NUM = 30;
    /**
     * 商城折扣百分比
     */
    public static final int SHOP_OFF = 31;

    /**
     * 侦查类型对加成玩家侦查科技的加成等级
     */
    public static final int SCOUT_TYPE_ADD = 32;

    /**
     * 单次侦查CD时间
     */
    public static final int SCOUT_CD = 33;

    /**
     * 侦查允许最大CD时间
     */
    public static final int SCOUT_CD_MAX_TIME = 34;

    /**
     * 装备初始格子
     */
    public static int BAG_INIT_CNT = 35;
    /**
     * 装备格子购买[次数,价格,格子]
     */
    public static int BAG_BUY = 36;

    /**
     * 城池竞选时间
     */
    public static final int CAMPAIGN_TIME = 37;

    /**
     * 城池城主额外奖励道具数量
     */
    public static final int CITY_EXTRA_REWARD_NUM = 38;

    /**
     * 城池城主任期时间
     */
    public static final int CITY_OWNER_TIME = 39;

    /**
     * 城池单次修复比例
     */
    public static final int CITY_REPAIR_RATIO = 40;

    /**
     * 将领洗髓免费次数刷新时间
     */
    public static final int HERO_WASH_TIME = 41;
    /**
     * 将领洗髓免费次数上限
     */
    public static final int HERO_WASH_FREE_MAX = 42;
    /**
     * 将领至尊洗髓消耗金币数
     */
    public static final int HERO_WASH_GOLD = 43;

    /**
     * 聚宝盆翻牌倒计时
     */
    public static final int TREASURE_OPEN_TIME = 44;
    /**
     * 资源兑换一次增加时长
     */
    public static final int TREASURE_TRADE_TIME = 45;
    /**
     * 资源时长达到多少禁止兑换
     */
    public static final int TREASURE_TRADE_MAX_TIME = 46;

    /**
     * 开启自动补兵所需科技等级
     */
    public static final int AUTO_ARM_NEED_TECH_LV = 47;
    /**
     * 城墙npc换兵种消耗金币
     */
    public static final int WALL_CHANGE_ARMY_GOLD = 48;
    /**
     * 驻防将领上限
     */
    public static final int WALL_HELP_MAX_NUM = 49;
    /**
     * 高级重建家园保护时间
     */
    public static final int REBUILD_PROTECT_HIGHT = 50;
    /**
     * 低级重建家园保护时间
     */
    public static final int REBUILD_PROTECT_NORMAL = 51;

    /**
     * 每日军团建设次数上限
     */
    public static final int PARTY_BUILD_MAX = 52;
    /**
     * 军团公告长度
     */
    public static final int PARTY_SLOGAN_LEN = 53;

    /**
     * 事件数目固定6条记录
     */
    public static final int ROLE_OPT_NUM = 55;
    /**
     * 兵营募兵耗粮系数
     */
    public static final int FACTORY_ARM_NEED_FOOD = 56;

    /**
     * 邮件保存时间
     */
    public static final int MAIL_MAX_TIME = 58;
    /**
     * 军团荣誉排行榜显示条数
     */
    public static final int PARTY_HONOR_RANK_LEN = 59;

    /**
     * 军团官员选举显示数量（系统自动任命官员数）
     */
    public static final int PARTY_ELECT_NUM = 60;
    /**
     * 军团官员选举排行榜最大长度
     */
    public static final int PARTY_ELECT_RANK_NUM = 61;
    /**
     * 军团官员选举，拉票单价
     */
    public static final int PARTY_CANVASS_GOLD = 62;
    /**
     * 军团官员选举时长
     */
    public static final int PARTY_ELECT_TIME = 63;
    /**
     * 军团官员任期天数
     */
    public static final int PARTY_JOB_DATE = 64;

    /**
     * 个人资源点最大采集队列数
     */
    public static final int ACQUISITION_MAX_QUE = 65;
    /**
     * 军团任务每三小时刷一次
     */
    public static final int PARTY_TASK_REFRESH_PER_HOUR = 66;
    /**
     * 军团任务额外奖励每阶段获得上限
     */
    public static final int PARTY_TASK_EXT_REWARD_CNT = 67;
    /**
     * 体力最大值
     */
    public static final int POWER_MAX = 68;
    /**
     * 恢复1点能量秒数
     */
    public static final int POWER_BACK_SECOND = 69;
    // /** 将领突破概率 */
    // public static final int HERO_BREAK_PRO = 70;

    /**
     * 良将寻访功能开启，玩家等级限制
     */
    public static final int HERO_SEARCH_ROLE_LV = 71;
    /**
     * 良将寻访，金币消耗
     */
    public static final int NORMAL_HERO_GOLD = 72;
    /**
     * 神将寻访，金币消耗
     */
    public static final int SUPER_HERO_GOLD = 73;
    /**
     * 良将转为将令数
     */
    public static final int NORMAL_HERO_TOKEN = 74;
    /**
     * 神将转为将令数
     */
    public static final int SUPER_HERO_TOKEN = 75;
    /**
     * 闪电战[最大时间,消耗体力,消耗道具,增加时间]
     */
    public static final int FLAY_BATTLE_TIME = 76;
    /**
     * 奔袭战[最大时间,消耗体力,消耗道具,增加时间]
     */
    public static final int RUN_BATTLE_TIME = 77;
    /**
     * 远征战[最大时间,消耗体力,消耗道具,增加时间]
     */
    public static final int WALK_BATTLE_TIME = 78;
    /**
     * 军团职位闪电奔袭远征战消耗[[消耗体力,消耗道具]]
     */
    public static final int PARY_JOB_BATTLE_COST = 79;
    /**
     * 资源领取次数每隔多少秒涨一次
     */
    public static final int RES_ADD_TIME = 80;
    /**
     * 玩家等级上限
     */
    public static final int MAX_ROLE_LV = 81;
    /**
     * 司令部等级上限
     */
    public static final int MAX_COMMAND_LV = 82;
    /**
     * 驻军时间小时
     */
    public static final int ARMY_STATE_GUARD_TIME = 83;
    /**
     * 世界任务开启taskId
     */
    public static final int WORLD_TASK_OPEN_TASK_ID = 84;
    /**
     * 建筑免费加速时间秒
     */
    public static final int FREE_CD_BUILD = 85;
    /**
     * 行军耗补给系数
     */
    public static final int MOVE_COST_FOOD = 86;
    /**
     * 名城最多占领几个
     */
    public static final int CITY_TYPE_8_MAX = 87;
    /**
     * NPC每4个小时攻城
     */
    public static final int CITY_NPC_FIGHT = 88;
    /**
     * 修改城池名称所需的改名贴的数量
     */
    public static final int CITY_RENAME_COST = 89;
    /**
     * 徒弟上限个数
     */
    public static final int MAX_APPRENTICE_COUNT = 90;
    /**
     * 都城开发需要30分钟冷却
     */
    public static final int HOME_DEV_CD = 91;
    /**
     * 都城开发增加人口
     */
    public static final int HOME_DEV_GAIN = 92;
    /** 都城开发消耗 */
    // public static final int HOME_DEV_COST = 93;
    /**
     * 201最近的城cityId
     */
    public static final int HOME_1_CITY_ROUND = 97;
    /**
     * 201最近的城cityId
     */
    public static final int HOME_2_CITY_ROUND = 98;
    /**
     * 201最近的城cityId
     */
    public static final int HOME_3_CITY_ROUND = 99;
    /**
     * 201最近的城cityId
     */
    public static final int HOME_4_CITY_ROUND = 100;
    /**
     * 月卡每日的奖励
     */
    public static final int MONTH_CARD_REWARD = 101;
    /**
     * 好友上限个数
     */
    public static final int MAX_FRIEND_COUNT = 102;
    /**
     * 攻击系数
     */
    public static final int ATK_RATIO = 103;
    /**
     * 防御系数
     */
    public static final int DEF_RATIO = 104;
    /**
     * 兵力系数
     */
    public static final int ARMY_RATIO = 105;
    /**
     * 面板属性系数
     */
    public static final int ATTR_RATIO = 106;
    /**
     * 科技快研需要购买VIP礼包
     */
    public static final int TECH_QUICK_VIP_BAG = 107;
    /**
     * 装备回收需要购买VIP礼包
     */
    public static final int EQUIP_DECOMPOSE_VIP_BAG = 109;
    /**
     * 装备回收分解装备会有50%的几率返还装备图纸
     */
    public static final int EQUIP_DECOMPOSE_RATE = 110;
    /**
     * 成为徒弟的最大等级
     */
    public static final int MAX_APPRENTICE_LV = 111;
    /**
     * 成为师傅的最小等级
     */
    public static final int MIN_MASTER_LV = 112;
    /**
     * 拜师奖励
     */
    public static final int ADD_MASTER_REWARD = 113;
    /**
     * 名城人口
     */
    public static final int MILDDLE_CITY_PEOPLE = 114;
    /**
     * 洗髓保底值次数
     */
    public static final int WASH_TOTAL_FLOOR_COUNT = 115;
    /**
     * 洗髓保底增长的值
     */
    public static final int ADD_WASH_TOTAL = 116;
    /**
     * 免费洗髓浮动表
     */
    public static final int WASH_FREE_FLUCTUATE = 117;
    /**
     * 付费洗髓浮动表
     */
    public static final int WASH_PAY_FLUCTUATE = 118;
    /**
     * 超过15分钟不允许发起战斗
     */
    public static final int ATK_MAX_TIME = 119;
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
     * 打城罩子保护时间分钟
     */
    public static final int CITY_PROTECT_TIME = 126;
    /**
     * 内阁采集将领等级要求
     */
    public static final int ACQ_HERO_REQUIRE = 127;
    /**
     * 内阁御林军将领等级要求
     */
    public static final int GUARDS_HERO_REQUIRE = 57;
    /**
     * 内阁天策府初始点兵id
     */
    public static final int CABINET_INIT_ID = 129;
    /**
     * 内阁财富署,御林卫,天策府开启等级
     */
    public static final int CABINET_CONDITION = 130;
    /**
     * 100金币城墙满兵
     */
    public static final int WALL_FULL_ARMY_NEED_GOLD = 131;
    /**
     * 空降补给的|资源|军备|物资|补给花费黄金
     */
    public static final int SUPPLY_DORP_GOLD = 132;
    public static final int K1 = 133;
    public static final int K2 = 134;
    public static final int K3 = 135;
    public static final int K4 = 136;
    public static final int K5 = 137;
    public static final int K6 = 138;
    public static final int K7 = 139;
    public static final int K0 = 140;
    /**
     * 低级重建家园基数
     */
    public static final int REBUILD_AWARD_NORMAL = 141;
    /**
     * 高级重建家园基数
     */
    public static final int REBUILD_AWARD_HIGHT = 142;
    /**
     * 进官员选举榜等级限制
     */
    public static final int PARTY_JOB_MIN_LV = 143;
    /**
     * 装备洗练等级概率(万分比)
     */
    public static final int EQUIP_LV_PROBABILITY = 144;
    /**
     * 装备洗练技能种类随机(权重)
     */
    public static final int EQUIP_SKILL_PROBABILITY = 145;
    /**
     * 体力购买价格
     */
    public static final int BUY_ACT_PRICE = 146;
    /**
     * 体力购买获得体力
     */
    public static final int BUY_ACT_REWARD = 147;
    /**
     * 召唤持续时间(秒)
     */
    public static final int SUMMON_KEEP_TIME = 148;
    /**
     * 召唤所需要的低级迁城符数量
     */
    public static final int SUMMON_NEED_PROP_CNT = 149;
    /**
     * 召唤范围半径
     */
    public static final int SUMMON_RADIUS = 150;
    /**
     * 召唤响应者范围半径
     */
    public static final int SUMMON_RESPOND_RADIUS = 151;
    /**
     * 需强制刷新流寇的等级（1~6）
     */
    public static final int TASK_COMBAT_LV = 152;
    /**
     * 新建角色进入世界时加保护罩
     */
    public static final int NEW_LORD_PROTECT_TIME = 153;
    /**
     * 小号功能，等级小于10级
     */
    public static final int SMALL_ID_LV = 154;
    /**
     * 小号功能，未登录超过多少天
     */
    public static final int SMALL_LOGIN_DAY = 155;
    /**
     * 购买成长计划要求[VIP等级，金币]
     */
    public static final int OPEN_ACT_GROW_NEED = 156;
    /**
     * 首充礼包持续时间
     */
    public static final int ACT_FIRSH_CHARGE_TIME = 159;
    /**
     * 黑鹰计划持续时间
     */
    public static final int ACT_BLACK_TIME = 160;
    /**
     * 邮件删除时间天
     */
    public static final int DEL_MAIL_DAY = 161;
    /**
     * 油，电，补给，矿石购买保底值
     */
    public static final int RES_BUY_BASE_VAL = 162;
    /**
     * 创建角色发的邮件ID
     */
    public static final int MAIL_FOR_CREATE_ROLE = 163;
    /**
     * 充值金额对应VIP经验系数
     */
    public static final int PAY_VIP_COEFF = 164;
    /**
     * 阵营战等级限制
     */
    public static final int ATTACK_STATE_NEED_LV = 165;
    /**
     * 购买建造返还金币[前2次对应的金币]
     */
    public static final int BUY_BUILD_GAIN_GOLD = 166;
    /**
     * 不同阵营私聊消耗金币数
     */
    public static final int PRIVATE_CHAT_COST_GOLD = 167;
    /**
     * 私聊间隔时间
     */
    public static final int PRIVATE_CHAT_INTERVAL = 168;
    /**
     * 采集队伍上限
     */
    public static final int MINE_MAX_CNT = 172;
    /**
     * 翻倍奖励活动的倍数
     */
    public static final int ACT_DOUBLE_NUM = 173;
    /**
     * 任命官员花费的金币
     */
    public static final int APPOINT_COST = 174;
    /**
     * 神将寻访 第5次必出其中一个将领配置
     */
    public static final int SEARCH_SUPER_HERO_FOR_FIVE = 175;
    /**
     * 达到某次数，必出某神将的配置
     */
    public static final int SEARCH_SUPER_HERO_SPECIAL = 176;
    /**
     * 装备洗练保底配置
     */
    public static final int EQUIP_BAPTIZE_MIN_UPLV_CONF = 177;
    /**
     * 皇城采集加经验的采集时间
     */
    public static final int HOME_CITY_ADD_EXP_COLLECT_TIME = 178;
    /**
     * 闪电战、奔袭战、远征战时间上限
     */
    public static final int MARCH_UPPER_LIMIT_TIME = 179;
    /**
     * 营地战递增时间
     */
    public static final int CITY_BATTLE_INCREASE_TIME = 180;
    /**
     * 要塞发起州郡 闪电战、奔袭战、远征战时间
     */
    public static final int SPECIAL_BATTLE_TIME = 181;
    /**
     * 要塞发起州郡营地战行军时间
     */
    public static final int SPECIAL_MARCH_TIME = 182;
    /**
     * 大喇叭长驻时间 单位秒
     */
    public static final int PLAYER_WORLD_CHAT_SHOW_TIME = 183;
    /**
     * 剧情任务开始的触发条件(将领Id)
     */
    public static final int SECTIONTASK_BEGIN_HERO_ID = 184;
    /**
     * Sentry Dsn 配置
     */
    public static final int SENTRY_DSN = 185;
    /**
     * 被驻防城墙最小等级
     */
    public static final int GARRISON_WALL_REQUIRE_LV = 186;
    /**
     * 显示当前任务触发的剧情id
     */
    public static final int SHOW_CURTASK_TRIGGET_ID = 187;
    /**
     * 节气昼夜功能开启需要全服攻下据点city Type类型
     */
    public static final int START_SOLAR_TERMS_CITY_TYPE = 192;
    /**
     * 昼夜功能夜晚时间定义区间
     */
    public static final int DAY_AND_NIGHT_RANGE = 193;
    /**
     * 夜晚战斗防守方属性加成
     */
    public static final int DAY_AND_NIGHT_DEF_BONUS = 194;
    /**
     * 生产加速活动加速减少
     */
    public static final int ACT_PRODUCTTION_NUM = 195;
    /**
     * 全军返利第一名阵营玩家奖励
     */
    public static final int ACT_ALL_CHARGE_REWARD = 196;
    /**
     * 每个区域真实玩家的容量
     */
    public static final int AREA_REAL_PLAER_CAPACITY = 198;
    /**
     * 每日使用体力丹上限
     */
    public static final int USE_PROP_ADD_POWER_MAX = 199;
    /**
     * 低迁随机到本地概率
     */
    public static final int LOW_MOVE_CITY_PROBABILITY = 200;
    /**
     * 大喇叭的字符长度限制
     */
    public static final int WORLD_CHAT_MAX_LENGTH = 201;
    /**
     * 每日攻击流寇上限值
     */
    public static final int ATTACK_BANDIT_MAX = 203;
    /**
     * 剩余资源点移除的时间
     */
    public static final int MINE_RM_MIN_TIME = 205;
    /**
     * 建筑升级时间超过的时间
     */
    public static final int BUILD_UPGRADE_TIME_EXCEEDED = 206;
    /**
     * 玩家推荐的数量
     */
    public static final int RECOMMEND_PLAYER_CNT = 207;
    /**
     * 完成某个任务奖励商用建造队列的任务id
     */
    public static final int AWARD_COMMERCIAL_BUILD_QUEUEN_TASKID = 208;
    /**
     * 战斗忽略要暴击和闪避的副本id
     */
    public static final int DONT_DODGE_CRIT_COMBATID = 209;
    /**
     * 副本星级规则
     */
    public static final int COMBAT_STAR_RULE = 210;
    /**
     * 情报部最大互动次数
     */
    public static final int CIA_INTERACTION_MAX_CNT = 211;
    /**
     * 情报部约会最大次数
     */
    public static final int CIA_APPOINTMENT_MAX_CNT = 212;
    /**
     * 开启情报时间(按创建角色第几填开启)
     */
//    public static final int OPEN_CIA_TIME = 213;
    /**
     * 单次互动胜利获得好感度系数
     */
    public static final int CIA_INTERACTION_WIN = 213;
    /**
     * 单次互动失败获得好感度系数
     */
    public static final int CIA_INTERACTION_FAIL = 214;
    /**
     * 单次约会增加的好感度
     */
    public static final int CIA_APPOINTMENT_CRIT = 215;
    /**
     * 单次互动提升的亲密度
     */
//    public static final int CIA_INTERACT_INTIMACY_CRIT = 217;

    /**
     * 聊天禁言: [比较条数, 相似达到条数]
     */
    public static final int CHECK_CHAT_COMPARE_CNT = 218;
    /**
     * 聊天禁言检测最小字符数
     */
    public static final int CHECK_CHAT_MIN_CNT = 219;
    /**
     * 聊天禁言相似比率 单位:万分比
     */
    public static final int CHECK_CHAT_RATE = 220;
    /**
     * 聊天禁言时间 单位:分钟
     */
    public static final int CHAT_SILENCE_TIME = 221;
    /**
     * 都城开发次数上限
     */
    public static final int UP_CAPITALCITY_MAX_CNT = 222;
    /**
     * 超级矿点 重置状态 到 生产状态 的cd
     */
    public static final int SUPERMINE_RESET_TO_PRODUCED_CD = 228;
    /**
     * 超级矿点 停产状态 到 重置状态的cd
     */
    public static final int SUPERMINE_STOP_TO_RESET_CD = 229;
    /**
     * 聊天禁言VIP限制: 等级
     */
    public static final int CHECK_SILENCE_VIP = 230;

    /**
     * 前线阵地伤害系数
     */
    public static final int BATTLE_FRONT_HURT = 223;
    /**
     * 阵地进攻间隔
     */
    public static final int BATTLE_FRONT_ATK_CD = 224;
    /**
     * 柏林会战预显示时间(定时器)
     */
    public static final int PRE_VIEW_CRON = 225;
    /**
     * 柏林会战开启时间(定时器)
     */
    public static final int BERLIN_BEGIN_CRON = 226;
    /**
     * 柏林会战胜利占领时间, 单位 : 秒
     */
    public static final int BERLIN_WIN_OF_TIME = 227;
    /**
     * 柏林会战强袭消耗金币
     */
    public static final int BERLIN_PRESS_GOLD = 231;
    /**
     * 柏林临时读取时间
     */
    public static final int BERLIN_TEMP_TIME = 232;
    /**
     * 荣耀日报进攻方,防守方排名筛选
     */
    public static final int HONOR_DAILY_RANK = 233;
    /**
     * 矿石转盘抽奖规则配置
     */
    public static final int ORE_TURNPLATE_CONFIG = 234;
    /**
     * 柏林会战参战角色获得军备箱
     */
    public static final int BERLIN_JOIN_BATTLE_REWARD = 235;
    /**
     * 柏林将领复活CD
     */
    public static final int BERLIN_RESURRECTION_CD = 236;
    /**
     * 柏林补偿奖励
     */
    public static final int BERLIN_COMPENSATION_AWARD = 238;
    /**
     * 柏林强袭出击金币消耗
     */
    public static final int BERLIN_PRESS_CNT_CONSUME = 239;
    /**
     * 柏林复活次数金币消耗
     */
    public static final int BERLIN_RESURRECTION_CNT_CONSUME = 240;
    /**
     * 柏林立即出击金币消耗
     */
    public static final int BERLIN_IMMEDIATELY_CNT_CONSUME = 241;
    /**
     * 备战柏林耗资源最大次数
     */
    public static final int BERLIN_PREWAR_BUFF_BUY_RES_CNT = 242;
    /**
     * 免柏林将领复活CD的持续时间
     */
    public static final int FREE_BERLIN_RESURRECTION_CD_TIME = 243;
    /**
     * 每日购买世界boss次数上限值
     */
    public static final int ATTACK_WORLD_BOSS_MAX = 245;
    /**
     * 世界boss次数购买所需金币配置
     */
    public static final int BUY_WORLD_BOSS_GOLD = 246;

    /**
     * 勋章商店刷新上限配置
     */
    public static final int MEDAL_GOODS_REFRESH_MAX = 248;
    /**
     * 勋章商店刷新消耗的金币配置
     */
    public static final int MEDAL_GOODS_REFRESH_GOLD = 249;
    /**
     * 勋章强化消耗的金条配置
     */
    public static final int MEDAL_INTENSIFY_GOLD_BAR = 251;
    /**
     * 副本勋章掉落权重配置
     */
    public static final int TASK_MEDAL_BURST_WEIGHT = 252;
    /**
     * 勋章捐献金条返还配置
     */
    public static final int MEDAL_DONATE_GOLD_BAR_RETURN = 253;
    /**
     * 勋章商店每日刷新时间点配置
     */
    public static final int MEDAL_GOODS_REFRESH_EVERYDAY = 254;

    /**
     * 攻打NPC城池行军速度加倍
     */
    public static final int ATTACK_NPC_CITY_MARCH_NUM = 255;

    /**
     * 名将转盘活动推送消息展示数量
     */
    public static final int FAMOUS_GENERAL_TURNPLATE_CHATS_CNT = 256;

    /**
     * 转盘活动 需要发送跑马灯的道具id
     */
    public static final int SEND_CHAT_PROP_IDS = 257;

    /**
     * 名将转盘能够兑换的道具
     */
    public static final int FAMOUS_GENERAL_EXCHANGE_PROP = 258;

    /**
     * 等级到达满级后，本应获取的经验转化为友谊积分的比例
     */
    public static final int EXP_EXCHANGE_CREDIT = 259;

    /**
     * 将领可装战机总数
     */
    public static final int HERO_WAR_PLANE_NUM = 260;
    /**
     * 空军基地免费抽取配置, 格式[战机1卡池免费抽取时间(单位:秒)，1天可以免费抽取的次数]
     */
    public static final int PLANE_SEARCH_FREE_NUM = 261;
    /**
     * 空军基地寻访价格配置, 格式：[单抽价格，10抽价格], 填金币数
     */
    public static final int PLANE_SEARCH_GOLD = 262;
    /**
     * 战机系数
     */
    public static final int WAR_PLANE_RADIO_NUM = 263;
    /**
     * 空军基地奖励时间
     */
    public static final int PLANE_FACTORY_AWARD_TIME = 264;
    /**
     * 战机的最大等级
     */
    public static final int PLANE_MAX_LEVEL = 265;
    /**
     * 空军基地寻访抽取跑马灯配置, 抽取战机的品质
     */
    public static final int PLANE_SEARCH_CHAT_QUALITY = 266;
    /**
     * 击飞玩家获得战机, 格式:[掉落概率(单位:)，最大数量]
     */
    public static final int PLANE_HIT_FLY_AWARD = 267;

    // 匪军叛乱开启时间配置 每月的[[第几周,第几周],[周几,开始时间(准点小时数),持续的秒值]]
    public static final int REBEL_START_TIME_CFG = 269;
    /**
     * 低级卡池第一次抽取的飞机
     */
    public static final int PLANE_FIRST_SEARCH_AWARD = 270;

    /**
     * 兵种克制相关系数
     */
    public static final int K8 = 271;
    public static final int K9 = 272;
    public static final int K10 = 273;

    /**
     * 匪军叛乱参加玩家的角色等级条件
     */
    public static final int REBEL_ROLE_LV_COND = 274;
    /**
     * 匪军叛乱通关全阵营邮件奖励积分
     */
    public static final int REBEL_ALL_PASS_AWARD = 275;

    /**
     * 战机高级卡池免费抽取间隔时间
     */
    public static final int PLANE_SUPER_SEARCH_CD = 276;
    /**
     * 德意志反攻活动发起进攻时间 每月的[[第几周,第几周],[周几,开始时间(准点小时数),持续的秒值],[每个阶段的持续时间（秒）]]
     */
    public static final int COUNTER_ATTACK_BERLIN_TIME_CFG = 277;
    /**
     * 配件进阶材料消耗
     */
    public static final int STONE_IMPROVE_NEED_COST = 278;
    /**
     * 德意志反攻每个阵营选取的玩家个数
     */
    public static final int COUNTER_ATTACK_CAMP_HIT_CNT = 279;
    /**
     * 德意志反攻积分
     */
    public static final int COUNTER_ATTACK_CREDIT = 280;
    /**
     * 柏林会战损兵军功
     */
    public static final int BERLIN_LOST_EXPLOIT_NUM = 281;
    /**
     * 匪军叛乱世界任务开启条件
     */
    public static final int REBEL_WORLD_TASKID_COND = 290;

    /**
     * 决战玩家等级限制
     */
    public static final int DECISIVE_BATTLE_LEVEL = 296;
    /**
     * 柏林会战霸主奖励决战指令配置
     */
    public static final int BERLIN_OVERLORD_COMPENSATION_AWARD = 297;
    /**
     * 决战失败相关时间数据配置
     */
    public static final int DECISIVE_BATTLE_FINAL_TIME = 298;

    /**
     * city表中读取form_combine字段的区服
     */
    public static final int CITY_FORM_SERVERID = 292;

    /**
     * 教官开启后开放教官id
     */
    public static final int UNSEAL_MENTOR_ID = 293;
    /**
     * 教官升级功能相关配置 升级一次消耗升级手册的数量，升级操作一次给予的经验值，钞票升级一次花费的钞票，钞票升级一次给予的经验值
     */
    public static final int MENTOR_UP_CONF = 294;
    /**
     * 教官升级功能, 道具消耗配置
     */
    public static final int MENTOR_UP_PROP_CONF = 295;
    /**
     * 将领可装战机解锁条件
     */
    public static final int HERO_WAR_PLANE_UNLOCK = 299;
    /**
     * 限时卡池3的配置
     */
    public static final int PLANE_SEARCH_LIMIT_NUM = 300;

    /**
     * 触发战机技能的兵排
     */
    public static final int LINE_PLANE_SKILL = 301;
    /**
     * 补给持续时间(秒)
     */
    public static final int SUPPLY_CONTINUE_TIME = 303;
    /**
     * 阵营消耗金币产生补给能量值(消耗100金币生成5点能量)
     */
    public static final int COST_GOLD_ENERGY_NUM = 304;
    /**
     * 阵营补给, 每日最大生成数量
     */
    public static int PARTY_SUPPLY_MAX_CNT_CONF = 305;
    /**
     * 阵营补给, 一键领取的配置type
     */
    public static int PARTY_SUPPLY_ONE_KEY_ALL = 306;

    /**
     * 每天可攻打世界boss次数
     */
    public static final int ATK_BOSS_CNT_EVERDAY = 307;
    /**
     * 世界BOSS奖励
     */
    public static final int SCHEDULE_BOSS_AWARD = 308;
    /**
     * 德意志反攻活动增强系数
     */
    public static final int COUNTER_ATK_FORM_COEF = 309;
    /**
     * 戒指洗练等级概率(万分比)
     */
    public static final int RING_LV_PROBABILITY = 310;
    /**
     * 戒指洗练技能种类随机(权重)
     */
    public static final int RING_SKILL_PROBABILITY = 311;
    /**
     * 戒指改造消耗
     */
    public static final int RING_BAPTIZE_COST = 312;
    /**
     * 戒指超级改造消耗
     */
    public static final int RING_SUPER_BAPTIZE_COST = 313;
    /**
     * 装备可以镶嵌宝石数量
     */
    public static final int EQUIP_JEWEL_MAX = 314;
    /**
     * 强化几次可以提升资质
     */
    public static final int UP_ATTR_COUNT = 315;
    /**
     * 戒指洗练保底配置
     */
    public static final int RING_BAPTIZE_MIN_UPLV_CONF = 316;
    /**
     * 世界争霸开放时间 周几[6,7,1,2]
     */
    public static final int WORLDWAR_OPEN_WEEK = 317;
    /**
     * 进入世界争霸所需迁城道具数量（世界争霸随机迁城道具）
     */
    public static final int ENTER_CROSS_MAP_PROP = 318;
    /**
     * 进入世界争霸后返回德意志所需迁城道具数量（随机迁城道具）
     */
    public static final int LEAVE_CROSS_MAP_PROP = 319;
    /**
     * 世界争霸随机出生点位置（除指定国都外的额外区域）
     */
    public static final int CROSS_MAP_RANDOM_CELLID = 320;
    /**
     * 打城罩子保护时间分钟
     */
    public static final int CITY_PROTECT_TIME_NEW_MAP = 321;
    /**
     * 新勋章强化消耗金锭配置
     */
    public static final int MEDAL_INTENSIFY_GOLD_INGOT = 322;
    /**
     * 新勋章金锭返还配置
     */
    public static final int MEDAL_DONATE_GOLD_INGOT_RETURN = 323;
    /**
     * 内阁特攻将领等级要求
     */
    public static final int COMMANDO_HERO_REQUIRE = 324;
    /**
     * 金红色掉落最低等级
     */
    public static final int RED_DROP_AWARD_LV = 332;

    /**
     * 纽约争霸优胜奖
     */
    public static final int NEWYORK_WAR_JOIN_SUCCESS_AWARD = 325;
    /**
     * 纽约争霸参与奖
     */
    public static final int NEWYORK_WAR_JOIN_AWARD = 326;
    /**
     * 纽约争霸进入排行榜最低杀敌数量
     */
    public static final int NEWYORK_WAR_RANK_MIN_ATTACK = 327;
    /**
     * 纽约争霸每轮攻守持续时间
     */
    public static final int NEWYORK_WAR_EACH_ROUND_ATTACK = 328;
    /**
     * 纽约争霸每轮休战持续时间
     */
    public static final int NEWYORK_WAR_EACH_ROUND_TRUCE = 329;
    /**
     * 纽约争霸预显示时间
     */
    public static final int NEWYORK_WAR_PRE_TIME = 330;
    /**
     * 纽约争霸开启时间
     */
    public static final int NEWYORK_WAR_START_END_TIME = 331;
    /**
     * 纽约争霸损兵获得经验比
     */
    public static final int NEWYORK_WAR_LOST_EXP = 333;
    /**
     * 纽约争霸开启时间条件 单位周
     */
    public static final int NEWYORK_WAR_BEGIN_WEEK = 334;

    /**
     * 引导蝎王解锁时间
     */
    public static final int SCORPION_ACTIVATE_TIME = 341;

    /**
     * 爱丽丝增援触发任务ID
     */
    public static final int ALICE_RESCUE_MISSION_TASK = 342;

    /**
     * 爱丽丝增援任务邮件奖励
     */
    public static final int ALICE_RESCUE_MISSION_MAIL_AWARD = 346;

    /**
     * 英雄进化时的返还比例
     */
    public static final int HERO_REGROUP_AWARD_NUM = 347;

    /**
     * 市场触发礼包
     */
    public static final int TRIGGER_GIFT_TREASURE_OPEN_PROBABILITY = 348;

    /**
     * 柏林势力值计算的配置
     */
    public static final int BERLIN_INFLUENCE_CONF = 350;

    /**
     * 聊天保存的付费活跃消息条数
     */
    public static final int SYSTEM_NOTICE_NUM = 351;

    /**
     * 分享坐标的频率判断
     */
    public static final int SHARE_CHAT_CD = 352;

    /**
     * 重启或停服后保留的聊天消息ChatId
     */
    public static final int SAVE_CHATID_LIST = 353;

    /**
     * 广告奖励
     */
    public static final int ADVERTISEMENT_REWARD = 354;

    /**
     * 官员投票的等级限制
     */
    public static final int PARTY_VOTE_LV = 356;

    /**
     * 快速买兵价格 [[购买1次时兵数，钻石价格],[购买2次时兵数，钻石价格]
     */
    public static final int QUICK_BUY_ARMY_PRICE = 357;

    /**
     * 快速买兵数量占max兵营容量比 系数 万份比
     */
    public static final int QUICK_BUY_ARMY_COEF = 358;


    /**
     * 玩家注册4天邮件奖励
     */
    public static final int REGISTER_FOUR_REWARD = 361;

    /**
     * 玩家注册7天邮件奖励
     */
    public static final int REGISTER_SEVEN_REWARD = 362;

    /**
     * 剩余争霸霸主皮肤id
     */
    public static final int BERLIN_WINNER_SKIN_ID = 363;

    /**
     * 柏林会战：战斗狂热状态触发配置
     */
    public static final int BERLIN_BATTLE_FRENZY_TRIGGER_CONF = 370;

    /**
     * 柏林战斗狂热最大攻击轮数
     */
    public static final int BERLIN_BATTLE_FRENZY_MAX_ROUND = 371;

    /**
     * 柏林战斗狂热扩展伤害
     */
    public static final int BERLIN_BATTLE_FRENZY_EXT_HURT = 372;

    /**
     * 玩家登录刷新流寇距上次离线时间间隔
     */
    public static final int REFRESH_BANDITS_OFFLINE_TIME = 1008;
    /**
     * 招募获得将领辛德勒1021时，自动给其穿上装备
     */
    public static final int REWARD_HERO_EQUIP = 1013;
    /**
     * 城池首杀奖励
     */
    public static final int CITY_FIRST_KILL_REWARD = 1014;
    /**
     * 宝石关卡购买次数对应价格
     */
    public static final int STONE_COMBAT_BUY_PRICE = 1015;
    /**
     * 超级工厂的额外消耗燃油系数
     */
    public static final int TRAIN_FACTORY_NORMAL = 1016;
    /**
     * 可用将领头像
     */
    public static final int USE_HERO_PORTRAIT = 1017;
    /**
     * 出生地坐标分配限制(第一个值完成某个任务分配,第二值如果填写表示创角色分配)
     */
    public static final int ALLOC_POS_CONDITION = 1018;
    /**
     * 排行榜显示每页显示的个数
     */
    public static final int RANK_PAGE_CNT = 1020;
    /**
     * 副本队伍人数
     */
    public static final int MEMBER_OF_COMBAT_TEAM = 1021;
    /**
     * 副本队伍喊话cd
     */
    public static final int COMBAT_TEAM_CHAT_CD = 1022;
    /**
     * 协助奖励的次数
     */
    public static final int COMBAT_TEAM_AWARD_CNT = 1023;

    /**
     * 飞艇时间配置 [开始时间(小时),持续时间(秒)]
     */
    public static final int AIRSHIP_TIME_CFG = 1024;
    /**
     * 飞艇解锁世界任务id
     */
    public static final int AIRSHIP_WORLDTASKID_CFG = 1025;
    /**
     * 飞艇加入人的数量
     */
    public static final int AIRSHIP_JOIN_MEMBER_CNT = 1026;
    /**
     * 飞艇可得奖默认次数 [每日归属奖励,每日协助奖励]
     */
    public static final int AIRSHIP_CAN_AWARD_CNT = 1027;
    /**
     * 推送配置开启 1 开启 0 关闭
     */
    public static final int PUSH_CONFIG_SWITCH = 1028;
    /**
     * 招募获得哪些将领时候 ，自动给其穿上装备
     */
    public static final int REWARD_EQUIP_HERO_IDS = 1031;

    /**
     * 加入社群钻石奖励数量
     */
    public static final int JOIN_COMMUNITY_AWARD = 1032;
    /**
     * 推荐好友最近离线时长
     */
    public static final int RECOMMEND_PLAYER_OFF_TIME = 1033;
    /**
     * 反攻德意志解锁状态
     */
    public static final int COUNTER_ATK_FUNCTION_LOCK = 1034;

    /*
     * 渠道邮件发送等级
     * */
    public static final int AUTO_SENDMAIL_LEVEL = 1035;

    /**
     * 渠道邮件发送VIP等级
     */
    public static final int AUTO_SENDMAIL_VIP_LV = 1036;

    /**
     * 英雄分享和兵书分享每日次数限制
     */
    public static final int CHAT_SHARE_CNT = 1039;

    /**
     * 解除师徒关系, 需要师傅离线天数
     */
    public static final int DEL_MASTER_NEED_OFFLINE_DAY = 1040;

    /**
     * 解除师徒关系, 需要徒弟离线天数
     */
    public static final int DEL_APPRENTICE_NEED_OFFLINE_DAY = 1041;

    /**
     * 再次拜师的cd时间
     */
    public static final int ADD_MASTER_AGAIN_CD_TIME = 1042;

    /**
     * 战火燎原功能开启时间配置
     */
    public static final int WAR_FIRE_TIME_CONF = 1044;

    /**
     * 战火燎原等级区服配置
     */
    public static final int WAR_FIRE_OPEN_COND_CONF = 1045;

    /**
     * 战火燎原安全区配置
     */
    public static final int WAR_FIRE_SAFE_AREA = 1046;

    /**
     * 战火燎原发放个人奖励和阵营排名奖励时玩家所需的最低积分
     */
    public static final int WAR_FIRE_AWARDS_SCORE_LIMIT = 1047;

    /**
     * 战火燎原每击杀X兵力获得积分
     */
    public static final int WAR_FIRE_KILL_SCORE = 1048;

    /**
     * 战火燎原退出活动后可再次参加活动时间间隔
     */
    public static final int WAR_FIRE_ENTER_CD = 1049;

    /**
     * 战火燎原据点单个阵营部队上限
     */
    public static final int WAR_FIRE_CITY_ARMY_MAX = 1050;

    /**
     * 战火燎原请求支援CD
     */
    public static final int WAR_FIRE_CITY_HELP_CD = 1051;

    /**
     * 战火燎原行军系数
     */
    public static final int WAR_FIRE_MARCH_TIME_COEF = 1059;

    /**
     * 行宫好感度星级突破
     */
    public static final int CIA_FAVORABILITY_QUALITY = 1077;

    /** 宝具洗练, 主体宝具属性系数 */
    public static final int TREASURE_WARE_MASTER_ATTR_COEFFICIENT = 650;
    /** 宝具洗练, 主体宝具属性评价系数*/
    public static final int TREASURE_WARE_MASTER_STAGE_COEFFICIENT = 651;
    /** 宝具洗练, 材料宝具属性系数 */
    public static final int TREASURE_WARE_MATERIAL_ATTR_COEFFICIENT = 652;
    /** 宝具洗练, 材料宝具属性评价系数*/
    public static final int TREASURE_WARE_MATERIAL_STAGE_COEFFICIENT = 653;
    /** 蓝/紫宝具总体评价区间 */
    public static final int TREASURE_WARE_LOWER_STAGE = 654;
    /** 橙/红/远古宝具总体评价区间 */
    public static final int TREASURE_WARE_HIGHER_STAGE = 655;
    /** 宝具属性阶数对应的评估分值*/
    public static final int TREASURE_WARE_ATTR_STAGE_SCORE = 656;
    /** 橙色/红色宝具材料单个玩家每天掉落数量 */
    public static final int TREASURE_MATERIALS_OUTPUT_NUM = 657;

    /** 宝具副本 钻石购买击败玩家次数 */
    public static final int TREASURE_COMBAT_CHALLENGE_PLAYER_PURCHASE = 777;
    /** 刷新挑战玩家消耗钻石数 */
    public static final int TREASURE_CHALLENGE_REFRESH_COST_DIAMOND = 778;



    /**
     * 行宫星级突破
     */
    public static final int CIA_STAR_QUALITY = 1078;

    /**
     * 开启天赋祝福上限
     */
    public static final int OPEN_TALENT_BLESS_LIMIT = 1080;

    /**
     * 失败开启添加祝福固定值
     */
    public static final int FAIL_OPEN_TALENT_BLESS_FIXED_VALUE = 1081;

    /**
     * 单次开启天赋祝福消耗
     */
    public static final int OPEN_TALENT_SINGLE_BLESS_CONSUME = 1082;

    /**
     * 开启天赋祝福概率
     */
    public static final int OPEN_TALENT_BLESS_PROBABILITY = 1083;

    /**
     * 装备自动锁定
     */
    public static final int EQUIP_AUTO_LOCK = 1090;

    /**
     * 心愿英雄获取次数
     */
    public static final int WISH_HERO_COUNT = 1101;


    // ========================机器人相关配置begin===========================
    /**
     * 机器人创建配置
     */
    public static final int ROBOT_INIT = 501;
    /**
     * 机器人默认睡眠间隔时间，单位：秒
     */
    public static final int ROBOT_SLEEP = 502;
    /**
     * 单次唤醒机器人最大数
     */
    public static final int ROBOT_AWAKE_LIMIT = 503;
    /**
     * 单次执行机器人定时任务最长时间限制，单位：毫秒
     */
    public static final int ATTACK_BANDIT_MIN_LV = 504;
    /**
     * 机器人执行自动招募士兵逻辑的周期，单位：秒
     */
    public static final int ARM_RECRUIT_DELAY = 505;
    /**
     * 装备改造逻辑执行时间间隔，单位：秒
     */
    public static final int EQUIP_REFIT_DELAY = 506;
    /**
     * 自动挑战副本逻辑执行时间间隔，单位：秒
     */
    public static final int DO_COMBAT_DELAY = 507;
    /**
     * 机器人自动搜寻矿点半径
     */
    public static final int MINE_RADIUS = 508;
    /**
     * 机器人日志打印开关
     */
    public static final int ROBOT_LOG_STATE = 509;
    /**
     * 机器人功能开关
     */
    public static final int ROBOT_STATE_SWITCH = 510;
    /**
     * 机器人区域数量,list[0] : 单次迁移的上限,list[1] : 区域中容纳的机器人的上限
     */
    public static final int ROBOT_AREA_COUNT = 511;
    /**
     * 行军加速打流寇任务ID
     */
    public static final int ATK_BANDIT_MARCH_TIME_TASKID = 512;

    /**
     * 清除蝎王倒计时的任务ID，领取任务奖励后触发
     */
    public static final int CLEAR_SCORPION_ACTIVATE_END_TIME_TASK = 520;

    /**
     * 配件超出当前经验等级上限
     */
    public static final int STONE_IMPROVE_UP_MAX_CONF = 282;

    // =========================机器人相关配置end============================

    // ========================阵营邮件相关配置begin===========================

    /**
     * 阵营邮件发送最低等级
     */
    public static final int SEND_CAMP_MAIL_LEVEL = 513;
    /**
     * 阵营邮件发送间隔
     */
    public static final int CAMP_MAIL_CD = 514;
    /**
     * 阵营邮件发送所需金币
     */
    public static final int CAMP_MAIL_GOLD = 515;
    /**
     * 阵营邮件发送所需VIP等级
     */
    public static final int CAMP_MAIL_VIP = 516;
    /**
     * 阵营邮件发送免费的官职
     */
    public static final int CAMP_MAIL_FREE_JOB = 517;
    /**
     * 阵营邮件发送内容最大长度
     */
    public static final int CAMP_MAIL_CONTENT_MAX = 518;
    /**
     * 阵营邮件手机端推送最低等级和离线时长
     */
    public static final int CAMP_MAIL_MIN_GRADE_AND_OFFLINE_LONG = 519;

    // =========================阵营邮件相关配置end============================

    // ========================打造装备相关配置begin===========================
    /**
     * 紫色装备打造推送世界消息所需最低排名值
     */
    public static final int PART_PURPLE_RANK = 600;
    /**
     * 橙色装备打造推送世界消息所需最低排名值
     */
    public static final int PART_ORANGE_RANK = 601;
    /**
     * 装备打造秘技概率范围值 和 命中间隔值
     */
    public static final int EQUIP_MAKE_RANGE = 602;

    // =========================打造装备相关配置end============================

    // ========================等级相关配置begin===========================
    /**
     * 全服玩家等级提升至45级推送世界消息的玩家位数
     */
    public static final int ROLE_45_DIGIT = 620;

    // =========================等级相关配置end============================
    /**
     * 任务流寇的行军时间
     */
    public static final int ATTACK_BANDIT_MARCH_TIME = 625;
    /**
     * 任务给免费征收次数
     */
    public static final int TASK_GIVE_GAINRES = 626;

    /**
     * 首充赠送的将领id【非赠送配置】发送公告用
     */
    public static final int GQS_HERO_ID = 621;
    /**
     * 世界争霸纽约调整系数 除以10000
     */
    public static final int WORLD_WAR_CITY_EFFECT = 627;

    /**
     * 宝具攻击类属性
     */
    public static final int TREASURE_WARE_ATTACK_ATTR_TYPE = 628;

    /**
     * 宝具防守类属性
     */
    public static final int TREASURE_WARE_DEFENCE_ATTR_TYPE = 629;

    /**
     * 宝具副本解锁位置条件
     */
    public static final int TREASURE_UNLOCK_HERO_POS_CONF = 630;

    /**
     * 宝具副本每天最多通关次数
     */
    public static final int TREASURE_COMBAT_DAILY_PROMOTE_MAX = 635;

    /**
     * 宝具副本非vip快速扫荡次数
     */
    public static final int TREASURE_COMBAT_DAILY_WIPE_MAX = 636;

    /**
     * 宝具副本扫荡奖励次数
     */
    public static final int TREASURE_COMBAT_WIPE_AWARD = 637;

    /**
     * 宝具副本挂机累计时长秒
     */
    public static final int TREASURE_ON_HOOK_AGGREGATE = 638;
    /**
     * 宝具副本购买扫荡次数单次递增价格
     */
    public static final int TREASURE_WIPE_INCREASE_PRICE =  639;

    /**
     * 宝具副本默认解锁上阵位
     */
    public static final int TREASURE_COMBAT_DEFAULT_UNLOCK = 640;
    /**
     * 常驻寻访金币消耗
     */
    public static final int PERMANENT_QUEST_GOLD_CONSUMPTION = 642;
    /**
     * 限时寻访金币消耗
     */
    public static final int TIME_LIMITED_SEARCH_FOR_GOLD_COIN_CONSUMPTION = 643;
    /**
     * 寻访重复武将转化碎片
     */
    public static final int DRAW_DUPLICATE_HERO_TO_TRANSFORM_FRAGMENTS = 644;

    /**
     * 寻访橙色武将碎片保底次数
     */
    public static final int DRAW_ORANGE_HERO_FRAGMENT_GUARANTEED_TIMES = 645;

    /**
     * 寻访橙色武将保底次数
     */
    public static final int DRAW_MINIMUM_NUMBER_OF_ORANGE_HERO = 646;

    /**
     * 寻访免费次数间隔时间（秒）
     */
    public static final int DRAW_HERO_CARD_FREE_TIMES_TIME_INTERVAL = 647;

    /**
     * 第一次抽卡必出奖励
     */
    public static int FIRST_DRAW_CARD_HERO_REWARD = 648;

    /**
     * 已使用活动抽取次数必出奖励
     */
    public static int ACTIVE_DRAWS_USED_COUNT_HERO_REWARD = 649;

    /**
     * 每日寻访可增加心愿值次数
     */
    public static int DAILY_DRAW_CARD_CAN_INCREASE_WISH_POINTS = 658;

    /**
     * 每日寻访单抽折扣消耗玉璧
     */
    public static int DAILY_DRAW_SINGLE_DRAW_DISCOUNT_TO_CONSUME_JADE = 659;

    /**
     * 寻访心愿值上限
     */
    public static int DRAW_CARD_WISH_VALUE_LIMIT = 660;

    /**
     * 限时寻访击败叛军次数和免费次数
     */
    public static int TIME_LIMITED_DRAW_DEFEATED_REBELS_NUM_AND_FREE_TIMES = 661;

    /**
     * 全部英雄品阶等级上限
     */
    public static int ALL_HERO_GRADE_CAPS = 1103;
    /**
     * 默认铭牌id
     */
    public static final int DEFAULT_NAME_PLATE_ID = 1070;

    /**
     * 默认头像框id
     */
    public static final int DEFAULT_PORTRAIT_FRAME_ID = 1071;

    /**
     * 默认的行军特效id
     */
    public static final int DEFAULT_MARCH_LINE_ID = 1072;

    /**
     *
     */
    public static final int SEASON_TALENT_RESET_COST_PROP = 1073;

    /**
     * 赛季天赋重置消耗
     */
    public static final int SEASON_TALENT_RESET_COST_DIAMOND = 1074;

    public static final int SEASON_TALENT_RESET_RETURN_RATE = 1075;

    /**
     * 宝具背包初始容量
     */
    public static final int TREASURE_WARE_BAG_INIT = 1094;
    /**
     * 购买宝具背包容量规则[购买次数,购买递增价格,单次购买格子]
     */
    public static final int BUY_TREASURE_WARE_BAG_RULE = 1095;

    /**
     * 宝具副本产出单位时间
     */
    public static final int TREASURE_WARE_RES_OUTPUT_TIME_UNIT = 1096;

    /**
     * 品质与万能碎片的兑换
     */
    public static final int EXCHANGE_OF_QUALITY_AND_UNIVERSAL_FRAGMENT = 4001;

    /**
     * 英雄合成所需碎片数量
     */
    public static int NUMBER_OF_SHARDS_REQUIRED_FOR_HERO_SYNTHESIS = 4002;

    /**
     * 宝具已分解定时删除间隔时间
     */
    public static final int DEL_DECOMPOSED_TREASURE_WARE = 9991;

    /**
     * 进攻战报保存时间
     */
    public static final int ATTACK_REPORT_EXPIRE_TIME = 9995;
    /**
     * 防守战报保存时间
     */
    public static final int DEFENCE_REPORT_EXPIRE_TIME = 9996;

    /**
     * 邮件战报最多保存数
     */
    public static final int MAIL_MAX_SAVE_COUNT = 9997;

    /**
     * 需要上报数数的渠道
     */
    public static final int THINKING_DATA_PLAT = 9998;

    /**
     * 忽略打印的日志协议号
     */
    public static final int IGNORE_LOG_CMD = 9999;


    // =======================跨服相关===========================
    /**
     * 跨服复活将领的消耗 [消耗金币数]
     */
    public static final int CROSS_REVIVE_HERO_COST = 335;
    /**
     * 跨服将领复活时间
     */
    public static final int CROSS_REVIVE_HERO_TIME = 336;
    /**
     * 跨服战时间 [[周几,开始时间准点,持续时间(秒)]]
     */
    public static final int CROSS_FIGHT_OPEN_TIME = 337;
    /**
     * 跨服将领操作消耗 [[跨服单挑消耗],[跨服回防消耗],[跨服偷袭消耗]]
     */
    public static final int CROSS_OPERATE_HERO_COST = 338;
    /**
     * 跨服参与奖
     */
    public static final int CROSS_JOIN_AWARD = 339;
    /**
     * 跨服优胜奖
     */
    public static final int CROSS_WIN_AWARD = 340;

    // =======================跨服相关===========================
    /**
     * 更新玩家数据到rpc-player服务器中的最低等级限制
     */
    public static final int RPC_UPLOAD_LORD_DATA_LOWEST_LV = 2001;

    /**
     * 跨服战火燎原每击杀X兵力获得积分
     */
    public static final int CROSS_WAR_FIRE_KILL_SCORE = 2006;

    /**
     * 跨服战火燎原退出活动后可再次参加活动时间间隔
     */
    public static final int CROSS_WAR_FIRE_ENTER_CD = 2007;

    /**
     * 提前多长时间进入跨服战火地图
     */
    public static final int EARLY_ENTRY_CROSS_MAP_PERIOD = 2011;

    /**
     * 跨服排名第一的人物形象奖励
     */
    public static final int CROSS_WAR_FIRE_WINNER_PORTRAIT = 2012;

}
