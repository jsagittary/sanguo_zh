package com.gryphpoem.game.zw.resource.constant;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.SentryHelper;
import com.gryphpoem.game.zw.resource.util.ActParamTabLoader;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.SystemTabLoader;
import com.gryphpoem.push.constant.PushConstant;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author TanDonghai
 * @ClassName Constant.java
 * @Description 通用常量定义类
 * @date 创建时间：2017年3月11日 下午4:35:58
 */
public final class Constant {

    /**
     * 角色id计算中，服务器id需要乘的倍数，PS：roleId=12000001，表示serverId=12，multi=1000000
     */
    public final static int ROLE_ID_MULTI = 1000000;

    /*** 王朝遗迹中防守部队上限*/
    public static int RELIC_DEFEND_ARMY_MAX_COUNT = 10000;
    /**
     * 玩家开启世界地图功能的等级限制
     */
    public static int OPEN_WORLD_LV;

    // /** 角色等级上限 */
    // public static int ROLE_MAX_LV;

    /**
     * 玩家初始行军时间系数
     */
    public static int MARCH_TIME_RATIO;

    /**
     * 战斗计算躲避的概率，万分比
     */
    public static int DODGE_PRO;

    /**
     * 战斗计算暴击的概率，万分比
     */
    public static int CRIT_PRO;

    /**
     * 战斗暴击倍率
     */
    public static float CRIT_MULTI;

    /**
     * 资源领取次数上限
     */
    public static int RES_GAIN_MAX;
    /**
     * 兵营扩建初始等级
     */
    public static int FACTORY_EXPAND_NINT_LEVL;
    /**
     * 兵营加时初始等级
     */
    public static int FACTORY_TIME_NINT_LEVL;
    /**
     * 初始关卡ID
     */
    public static int INIT_COMBAT_ID;
    /**
     * 金红色掉落最低等级
     */
    public static int RED_DROP_AWARD_LV;
    /**
     * 最大洗练次数
     */
    public static int EQUIP_MAX_BAPTIZECNT;
    /**
     * 洗练一个小时增加一次
     */
    public static int EQUIP_BAPTIZECNT_TIME;
    /**
     * 金币洗练消耗
     */
    public static int EQUIP_GOLD_BAPTIZE_1;
    /**
     * 3星满级金币洗练消耗
     */
    public static int EQUIP_GOLD_BAPTIZE_2;

    /**
     * 玩家注册登录奖励
     */
    public static List<List<Integer>> REGISTER_REWARD;
    /**
     * 商城折扣商品随机个数
     */
    public static int SHOP_OFF_RANDOM_NUM;
    /**
     * 商城折扣百分比
     */
    public static int SHOP_OFF;

    /**
     * 资源副本次数
     */
    public static int COMBAT_RES_CNT = 10;
    /**
     * 资源副本次数购买持续时间
     */
    public static int COMBAT_RES_TIME = 7200;
    /**
     * 装备初始格子
     */
    public static int BAG_INIT_CNT;
    /**
     * 装备格子购买[次数,价格,格子]
     */
    public static List<List<Integer>> BAG_BUY;

    /**
     * 聚宝盆翻牌倒计时
     */
    public static int TREASURE_OPEN_TIME;
    /**
     * 资源兑换一次增加时长
     */
    public static int TREASURE_TRADE_TIME;
    /**
     * 资源时长达到多少禁止兑换
     */
    public static int TREASURE_TRADE_MAX_TIME;

    /**
     * 开启自动补兵所需科技等级
     */
    public static int AUTO_ARM_NEED_TECH_LV;
    /**
     * 高级重建家园保护时间
     */
    public static int REBUILD_PROTECT_HIGHT;
    /**
     * 低级重建家园保护时间
     */
    public static int REBUILD_PROTECT_NORMAL;
    /**
     * 高级重建家园基数
     */
    public static int REBUILD_AWARD_HIGHT;
    /**
     * 低级重建家园基数
     */
    public static int REBUILD_AWARD_NORMAL;
    /**
     * 事件数目固定6条记录
     */
    public static int ROLE_OPT_NUM;
    /**
     * 兵营募兵耗粮系数
     */
    public static int FACTORY_ARM_NEED_FOOD;
    /**
     * 国器满阶段
     */
    public static int SUPER_EQUIP_MAX_STEP = 100;
    /**
     * 城墙npc换兵种消耗金币
     */
    public static int WALL_CHANGE_ARMY_GOLD;
    /**
     * 驻防将领上限
     */
    public static int WALL_HELP_MAX_NUM;
    /**
     * 100金币城墙满兵
     */
    public static int WALL_FULL_ARMY_NEED_GOLD;
    /**
     * 体力最大值
     */
    public static int POWER_MAX;
    /**
     * 恢复1点能量秒数
     */
    public static int POWER_BACK_SECOND;
    /**
     * 战斗掠夺系数
     */
    public static float FIGHT_GAIN_PRO = 0.2F;
    /**
     * 战斗被掠夺系数
     */
    public static float FIGHT_LOSE_PRO = 0.3F;
    // /** 将领突破概率 */
    // public static List<List<Integer>> HERO_BREAK_PRO;
    /**
     * 资源领取次数每隔多少秒涨一次
     */
    public static int RES_ADD_TIME;
    /**
     * 玩家等级上限
     */
    public static int MAX_ROLE_LV;
    /**
     * 司令部等级上限
     */
    public static int MAX_COMMAND_LV;
    /**
     * 驻军时间小时
     */
    public static int ARMY_STATE_GUARD_TIME;
    /**
     * 修改城池名称所需的改名贴的数量
     */
    public static int CITY_RENAME_COST;
    /**
     * 月卡天数
     */
    public final static int MONTH_CARD_DAY = 30;
    /**
     * 月卡每日的奖励
     */
    public static List<List<Integer>> MONTH_CARD_REWARD;
    /**
     * 好友上限个数
     */
    public static int MAX_FRIEND_COUNT;
    /**
     * 徒弟上限个数
     */
    public static int MAX_APPRENTICE_COUNT;
    /**
     * 成为徒弟的最大等级
     */
    public static int MAX_APPRENTICE_LV;
    /**
     * 成为师傅的最小等级
     */
    public static int MIN_MASTER_LV;
    /**
     * 拜师奖励
     */
    public static List<List<Integer>> ADD_MASTER_REWARD;
    /**
     * 洗髓保底值次数
     */
    public static int WASH_TOTAL_FLOOR_COUNT;
    /**
     * 洗髓保底增长的值
     */
    public static int ADD_WASH_TOTAL;
    /**
     * 免费洗髓浮动表
     */
    public static List<List<Integer>> WASH_FREE_FLUCTUATE;
    /**
     * 付费洗髓浮动表
     */
    public static List<List<Integer>> WASH_PAY_FLUCTUATE;
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
    public static List<Integer> BLACKHAWK_HERO_ID;
    /**
     * 黑鹰计划招募将领信物所需个数
     */
    public static int BLACKHAWK_NEED_TOKEN;
    /**
     * 黑鹰计划招信物格子的id
     */
    public static final int BLACKHAWK_TOKEN_KEYID = 6;
    /**
     * 内阁采集将领等级要求
     */
    public static List<Integer> ACQ_HERO_REQUIRE;
    /**
     * 内阁御林军将领等级要求
     */
    public static List<Integer> GUARDS_HERO_REQUIRE;
    /**
     * 内阁特攻将领等级要求
     */
    public static List<Integer> COMMANDO_HERO_REQUIRE;
    /**
     * 内阁天策府初始点兵id
     */
    public static int CABINET_INIT_ID;
    /**
     * 内阁财富署,御林卫,天策府开启等级
     */
    public static List<Integer> CABINET_CONDITION;
    /**
     * 空降补给的|资源|军备|物资|补给花费黄金
     */
    public static List<Integer> SUPPLY_DORP_GOLD;
    /**
     * 体力购买价格
     */
    public static List<Integer> BUY_ACT_PRICE;
    /**
     * 体力购买获得体力
     */
    public static int BUY_ACT_REWARD;
    /**
     * 召唤持续时间(秒)
     */
    public static int SUMMON_KEEP_TIME;
    /**
     * 召唤所需要的低级迁城符数量
     */
    public static Map<Integer, Integer> SUMMON_NEED_PROP_CNT;
    /**
     * 召唤范围半径
     */
    public static int SUMMON_RADIUS;
    /**
     * 召唤响应者范围半径
     */
    public static int SUMMON_RESPOND_RADIUS;
    /**
     * 小号功能，等级小于10级
     */
    public static int SMALL_ID_LV;
    /**
     * 小号功能，未登录超过多少天
     */
    public static int SMALL_LOGIN_DAY;
    /**
     * 购买成长计划要求[VIP等级，金币]
     */
    public static List<Integer> OPEN_ACT_GROW_NEED;
    /**
     * 首充礼包持续时间 单位（秒 ）
     */
    public static int ACT_FIRSH_CHARGE_TIME;
    /**
     * 黑鹰计划持续时间 单位（秒 ）
     */
    public static int ACT_BLACK_TIME;
    /**
     * 邮件删除时间天
     */
    public static int DEL_MAIL_DAY;
    /**
     * 油，电，补给，矿石购买保底值
     */
    public static List<Integer> RES_BUY_BASE_VAL;
    /**
     * 创建角色发的邮件ID
     */
    public static List<List<Integer>> MAIL_FOR_CREATE_ROLE;
    /**
     * 充值金额对应VIP经验系数
     */
    public static int PAY_VIP_COEFF;
    /**
     * 购买建造返还金币[前2次对应的金币]
     */
    public static List<Integer> BUY_BUILD_GAIN_GOLD;
    /**
     * 科技快研需要购买VIP礼包
     */
    public static int TECH_QUICK_VIP_BAG;
    /**
     * 不同阵营私聊消耗金币数
     */
    public static int PRIVATE_CHAT_COST_GOLD;
    /**
     * 私聊间隔时间 单位秒
     */
    public static int PRIVATE_CHAT_INTERVAL;
    /**
     * 翻倍奖励活动的倍数
     */
    public static int ACT_DOUBLE_NUM;
    /**
     * 装备回收分解装备会有50%的几率返还装备图纸
     */
    public static int EQUIP_DECOMPOSE_RATE;
    /**
     * 装备回收需要购买VIP礼包
     */
    public static int EQUIP_DECOMPOSE_VIP_BAG;
    /**
     * 任命官员花费的金币
     */
    public static int APPOINT_COST;
    /**
     * 装备洗练保底配置
     */
    public static Map<Integer, Integer> EQUIP_BAPTIZE_MIN_UPLV_CONF;
    /**
     * 皇城采集加经验的采集时间
     */
    public static int HOME_CITY_ADD_EXP_COLLECT_TIME;
    /**
     * 大喇叭长驻时间 单位秒
     */
    public static int PLAYER_WORLD_CHAT_SHOW_TIME;
    /**
     * 剧情任务开始的触发条件(将领Id)
     */
    public static List<Integer> SECTIONTASK_BEGIN_HERO_ID;
    /**
     * 被驻防城墙最小等级
     */
    public static int GARRISON_WALL_REQUIRE_LV;
    /**
     * 显示当前任务触发的剧情id
     */
    public static int SHOW_CURTASK_TRIGGET_ID;

    /**
     * 节气昼夜功能开启需要全服攻下据点city Type类型
     */
    public static int START_SOLAR_TERMS_CITY_TYPE;
    /**
     * 昼夜功能夜晚时间定义区间
     */
    public static List<Integer> DAY_AND_NIGHT_RANGE;
    /**
     * 夜晚战斗防守方属性加成
     */
    public static Map<Integer, Integer> DAY_AND_NIGHT_DEF_BONUS;

    /**
     * 生产加速时间减少比例
     */
    public static int ACT_PRODUCTTION_NUM;
    /**
     * Sentry Dsn 配置
     */
    public static String SENTRY_DSN;
    /**
     * 全军返利第一名阵营玩家奖励
     */
    public static List<List<Integer>> ACT_ALL_CHARGE_REWARD;
    /**
     * 每个区域真实玩家的容量
     */
    public static int AREA_REAL_PLAER_CAPACITY;
    /**
     * 每日使用体力丹上限
     */
    public static int USE_PROP_ADD_POWER_MAX;
    /**
     * 玩家登录刷新流寇距上次离线时间间隔
     */
    public static int REFRESH_BANDITS_OFFLINE_TIME;
    /**
     * 低迁随机到本地概率
     */
    public static int LOW_MOVE_CITY_PROBABILITY;
    /**
     * 每日攻击流寇上限值
     */
    public static int ATTACK_BANDIT_MAX;
    /**
     * 每日购买世界boss次数上限值
     */
    public static int ATTACK_WORLD_BOSS_MAX;
    /**
     * 世界boss次数购买所需金币
     */
    public static int BUY_WORLD_BOSS_GOLD;
    /**
     * 剩余资源点移除的时间
     */
    public static int MINE_RM_MIN_TIME;
    /**
     * 建筑升级时间超过的时间
     */
    public static int BUILD_UPGRADE_TIME_EXCEEDED;
    /**
     * 玩家推荐的数量
     */
    public static int RECOMMEND_PLAYER_CNT;
    /**
     * 完成某个任务奖励商用建造队列的任务id
     */
    public static int AWARD_COMMERCIAL_BUILD_QUEUEN_TASKID;
    /**
     * 战斗忽略要暴击和闪避的副本id
     */
    public static List<Integer> DONT_DODGE_CRIT_COMBATID;
    /**
     * 副本星级规则
     */
    public static List<List<Integer>> COMBAT_STAR_RULE;
    /**
     * 招募获得将领辛德勒1021时，自动给其穿上装备
     */
    public static List<List<Integer>> REWARD_HERO_EQUIP;
    /**
     * 招募获得哪些将领时候 ，自动给其穿上装备
     */
    public static List<Integer> REWARD_EQUIP_HERO_IDS;
    /**
     * 城池首杀奖励
     */
    public static List<List<List<Integer>>> CITY_FIRST_KILL_REWARD;
    /**
     * 城池首杀奖励,key: cityType
     */
    public static Map<Integer, List<List<Integer>>> CITY_TYPE_KILL_REWARD;
    /**
     * 宝石关卡购买次数对应价格
     */
    public static List<Integer> STONE_COMBAT_BUY_PRICE;
    /**
     * 超级工厂的额外消耗燃油系数
     */
    public static int TRAIN_FACTORY_NORMAL;
    /**
     * 情报部最大互动次数
     */
    public static int CIA_INTERACTION_MAX_CNT;
    /**
     * 情报部最大约会次数
     */
    public static int CIA_APPOINTMENT_MAX_CNT;
    /** 开启情报时间(按创建角色第几填开启) */
//    public static int OPEN_CIA_TIME;
    /**
     * 单次互动胜利获得好感度系数
     */
    public static int CIA_INTERACTION_WIN;
    /**
     * 单次互动失败获得好感度系数
     */
    public static int CIA_INTERACTION_FAIL;
    /**
     * 单次约会增加的好感度
     */
    public static int CIA_APPOINTMENT_CRIT;
    /** 单次互动提升的亲密度 */
//    public static List<List<Integer>> CIA_INTERACT_INTIMACY_CRIT;
    /**
     * 超级矿点 停产状态 到 重置状态的cd
     */
    public static int SUPERMINE_STOP_TO_RESET_CD;
    /**
     * 超级矿点 重置状态 到 生产状态 的cd
     */
    public static int SUPERMINE_RESET_TO_PRODUCED_CD;
    /**
     * 行军加速打流寇任务ID
     */
    public static List<Integer> ATK_BANDIT_MARCH_TIME_TASKID;
    /**
     * 可用将领头像
     */
    public static Map<Integer, Integer> USE_HERO_PORTRAIT;
    /**
     * 全服第【N】位等级提升至45级
     */
    public static int ROLE_45_DIGIT;
    /**
     * 紫色装备打造推送世界消息所需最低排名值
     */
    public static int PART_PURPLE_RANK;
    /**
     * 橙色装备打造推送世界消息所需最低排名值
     */
    public static int PART_ORANGE_RANK;
    /**
     * 装备打造秘技概率范围值 和 命中间隔值
     */
    public static List<Integer> EQUIP_MAKE_RANGE;
    /**
     * 矿石转盘抽奖规则配置
     */
    public static List<List<Integer>> ORE_TURNPLATE_CONFIG;
    /**
     * 出生地坐标分配限制(第一个值完成某个任务分配,第二值如果填写表示创角色分配)
     */
    public static int ALLOC_POS_CONDITION;
    /**
     * 名将转盘活动推送消息展示数量
     */
    public static int FAMOUS_GENERAL_TURNPLATE_CHATS_CNT;
    /**
     * 转盘活动 需要发送跑马灯的道具id
     */
    public static List<List<Integer>> SEND_CHAT_PROP_IDS;
    /**
     * 名将转盘能够兑换的道具
     */
    public static List<List<Integer>> FAMOUS_GENERAL_EXCHANGE_PROP;
    /**
     * 等级到达满级后，本应获取的经验转化为友谊积分的比例
     */
    public static List<Integer> EXP_EXCHANGE_CREDIT;
    // 匪军叛乱开启时间配置 每月的[[第几周,第几周],[周几,开始时间(准点小时数),持续的秒值]]
    public static List<List<Integer>> REBEL_START_TIME_CFG;
    /**
     * 匪军叛乱参加玩家的角色等级条件
     */
    public static int REBEL_ROLE_LV_COND;
    /**
     * 匪军叛乱通关全阵营邮件奖励积分
     */
    public static int REBEL_ALL_PASS_AWARD;
    /**
     * 匪军叛乱世界任务完成开启条件
     */
    public static int REBEL_WORLD_TASKID_COND;
    /**
     * 配件进阶材料消耗
     */
    public static List<List<Integer>> STONE_IMPROVE_NEED_COST;
    /**
     * 排行榜显示每页显示的个数
     */
    public static int RANK_PAGE_CNT;
    /**
     * city表中读取form_combine字段的区服
     */
    public static List<List<Integer>> CITY_FORM_SERVERID;
    /**
     * 忽略打印的日志协议号
     */
    public static Set<Integer> IGNORE_LOG_CMD;
    /**
     * 副本队伍的人数
     */
    public static int MEMBER_OF_COMBAT_TEAM;
    /**
     * 副本队伍喊话cd
     */
    public static int COMBAT_TEAM_CHAT_CD;
    /**
     * 副本协助奖励的次数
     */
    public static int COMBAT_TEAM_AWARD_CNT;
    /**
     * 飞艇时间配置 [[开始时间(小时),持续时间(秒)], [开始时间(小时),持续时间(秒)]]
     */
    public static List<List<Integer>> AIRSHIP_TIME_CFG;
    /**
     * 飞艇解锁世界任务id
     */
    public static int AIRSHIP_WORLDTASKID_CFG;
    /**
     * 飞艇加入人的数量
     */
    public static int AIRSHIP_JOIN_MEMBER_CNT;
    /**
     * 飞艇可得奖默认次数 [每日归属奖励,每日协助奖励]
     */
    public static List<Integer> AIRSHIP_CAN_AWARD_CNT;
    /**
     * 任务流寇的行军时间
     */
    public static int ATTACK_BANDIT_MARCH_TIME;
    /**
     * 任务给免费征收次数
     */
    public static List<List<Integer>> TASK_GIVE_GAINRES;
    /**
     * 清除蝎王倒计时的任务ID，领取任务奖励后触发
     */
    public static int CLEAR_SCORPION_ACTIVATE_END_TIME_TASK;
    /**
     * 爱丽丝增援触发任务ID
     */
    public static List<Integer> ALICE_RESCUE_MISSION_TASK;
    /**
     * 爱丽丝增援任务邮件奖励
     */
    public static List<List<Integer>> ALICE_RESCUE_MISSION_MAIL_AWARD;


    /**
     * 推荐好友最近离线时长
     */
    public static int RECOMMEND_PLAYER_OFF_TIME;
    /**
     * 反攻德意志解锁状态: 0为关闭，1为开启
     */
    public static int COUNTER_ATK_FUNCTION_LOCK;
    /**
     * 推送开关 1 开启 0 关闭 (默认不开启)
     */
    public static Integer PUSH_CONFIG_SWITCH;

    /**
     * 世界争霸开放时间 周几[6,7,1,2]
     */
    public static List<Integer> WORLDWAR_OPEN_WEEK;
    /**
     * 进入世界争霸所需迁城道具数量（世界争霸随机迁城道具）
     */
    public static List<Integer> ENTER_CROSS_MAP_PROP;
    /**
     * 进入世界争霸后返回德意志所需迁城道具数量（随机迁城道具）
     */
    public static List<Integer> LEAVE_CROSS_MAP_PROP;
    /**
     * 世界争霸随机出生点位置（除指定国都外的额外区域）
     */
    public static List<List<Integer>> CROSS_MAP_RANDOM_CELLID;

    /**
     * 跨服战时间 [[周几,开始时间准点,持续时间(秒)]]
     */
    public static List<List<Integer>> CROSS_FIGHT_OPEN_TIME;
    /**
     * 跨服将领操作消耗 [[跨服单挑消耗],[跨服回防消耗],[跨服偷袭消耗]]
     */
    public static List<List<Integer>> CROSS_OPERATE_HERO_COST;
    /**
     * 跨服复活将领的消耗 [消耗金币数]
     */
    public static List<Integer> CROSS_REVIVE_HERO_COST;
    /**
     * 跨服将领复活时间
     */
    public static int CROSS_REVIVE_HERO_TIME;
    /**
     * 跨服参与奖
     */
    public static List<List<Integer>> CROSS_JOIN_AWARD;
    /**
     * 跨服优胜奖
     */
    public static List<List<Integer>> CROSS_WIN_AWARD;

    /**
     * 加入社群钻石奖励数量
     */
    public static int JOIN_COMMUNITY_AWARD;

    /**
     * 市场点击箱子后触发礼包概率
     */
    public static List<List<Integer>> TRIGGER_GIFT_TREASURE_OPEN_PROBABILITY;

    /**
     * 广告奖励
     */
    public static List<List<Integer>> ADVERTISEMENT_REWARD;

    /**
     * 玩家注册4天邮件奖励
     */
    public static List<List<Integer>> REGISTER_FOUR_REWARD;

    /**
     * 玩家注册4天邮件奖励
     */
    public static List<List<Integer>> REGISTER_SEVEN_REWARD;

    /**
     * 需要上报数数的渠道
     */
    public static List<Integer> THINKING_DATA_PLAT;


    /**
     * 自动发送相应渠道邮件的等级
     */
    public static int AUTO_SENDMAIL_LEVEL;

    /**
     * 自动发送相应渠道邮件的vip等级
     */
    public static int AUTO_SENDMAIL_VIP_LV;

    /**
     * 解除师徒关系, 需要师傅离线天数
     */
    public static int DEL_MASTER_NEED_OFFLINE_DAY;

    /**
     * 解除师徒关系, 需要徒弟离线天数
     */
    public static int DEL_APPRENTICE_NEED_OFFLINE_DAY;

    /**
     * 再次拜师的cd时间
     */
    public static int ADD_MASTER_AGAIN_CD_TIME;

    /**
     * 剩余争霸霸主皮肤id
     */
    public static int BERLIN_WINNER_SKIN_ID;

    /**
     * 行宫好感度突破
     */
    public static List<List<Integer>> CIA_FAVORABILITY_QUALITY;

    public static List<Integer> CIA_FAVORABILITY_QUALITY_LIST;

    /**
     * 行宫星级突破
     */
    public static List<List<Integer>> CIA_STAR_QUALITY;

    public static List<Integer> CIA_STAR_QUALITY_LIST;

    /**
     * 开启天赋祝福上限
     */
    public static int OPEN_TALENT_BLESS_LIMIT;

    /**
     * 失败开启添加祝福固定值
     */
    public static int FAIL_OPEN_TALENT_BLESS_FIXED_VALUE;

    /**
     * 单次开启天赋祝福消耗
     */
    public static int OPEN_TALENT_SINGLE_BLESS_CONSUME;

    /**
     * 开启天赋祝福概率
     */
    public static List<List<Integer>> OPEN_TALENT_BLESS_PROBABILITY;

    /**
     * 宝具已分解定时删除间隔时间
     */
    public static int DEL_DECOMPOSED_TREASURE_WARE;

    /**
     * 沙盘演武 定义常量
     */
    public static String SAND_TABLE_PREVIEW;
    public static List<String> SAND_TABLE_OPEN_END;
    public static int SAND_TABLE_1054;
    public static int SAND_TABLE_1055;
    public static int SAND_TABLE_1056;
    public static int SAND_TABLE_1057;
    public static List<List<Integer>> SAND_TABLE_1058;

    /**
     * 挂机叛军
     */
    public static int ONHOOK_1061;
    public static int ONHOOK_1062;
    public static int ONHOOK_1063;
    public static int ONHOOK_1064;
    public static List<Integer> ONHOOK_1065;
    public static int ONHOOK_1066;

    public static int DEFAULT_NAME_PLATE_ID;
    public static int DEFAULT_PORTRAIT_FRAME_ID;
    public static int DEFAULT_MARCH_LINE_ID;


    //消耗天赋卷重置赛季天赋
    public static List<Integer> SEASON_TALENT_RESET_COST_PROP;
    //消耗钻石重置赛季天赋
    public static List<Integer> SEASON_TALENT_RESET_COST_DIAMOND;
    /***
     * 赛季天赋重置返还天赋点比率
     */
    public static int SEASON_TALENT_RESET_RETURN_RATE;

    /**
     * 上传玩家数据到跨服服务器的最低等级, 默认40级
     */
    public static int RPC_UPLOAD_LORD_DATA_LOWEST_LV;

    //撤离的冷却时间
    public static int CAMP_REBUILD_LEAVE_SECONDS;
    //生产红装世界进程限制
    public static int FORGE_RED_EQUIP_SCHEDULE;
    //船坞红色材料世界进程限制
    public static int DOCK_RED_MATERIAL_SCHEDULE;
    /**
     * 邮件战报最多保存数
     */
    public static int MAIL_MAX_SAVE_COUNT;

    public static Map<Integer, List<Integer>> ATTACK_REPORT_EXPIRE_TIME;

    public static Map<Integer, List<Integer>> DEFENCE_REPORT_EXPIRE_TIME;

    /**
     * 配饰升星单次上限
     */
    public static int STONE_IMPROVE_UP_MAX_CONF;
    /**
     * 宝具背包初始容量
     */
    public static int TREASURE_WARE_BAG_INIT;
    /**
     * 购买宝具背包容量规则[购买次数,购买递增价格,单次购买格子]
     */
    public static List<Integer> BUY_TREASURE_WARE_BAG_RULE;

    /**
     * 宝具副本每天最多通关次数
     */
    public static int TREASURE_COMBAT_DAILY_PROMOTE_MAX;

    /**
     * 宝具副本非vip快速扫荡次数
     */
    public static int TREASURE_COMBAT_DAILY_WIPE_MAX;

    /**
     * 宝具副本挂机累计时长秒
     */
    public static int TREASURE_ON_HOOK_AGGREGATE;

    /**
     * 宝具副本购买扫荡次数单次递增价格
     */
    public static Map<Integer, Integer> TREASURE_WIPE_INCREASE_PRICE;

    /**
     * 宝具副本扫荡奖励次数
     */
    public static int TREASURE_COMBAT_WIPE_AWARD;

    /**
     * 宝具副本默认解锁上阵位
     */
    public static int TREASURE_COMBAT_DEFAULT_UNLOCK;

    /**
     * 宝具副本解锁位置条件
     */
    public static List<List<Integer>> TREASURE_UNLOCK_HERO_POS_CONF;

    /**
     * 宝具攻击类属性
     */
    public static List<Integer> TREASURE_WARE_ATTACK_ATTR_TYPE;

    /**
     * 宝具防守类属性
     */
    public static List<Integer> TREASURE_WARE_DEFENCE_ATTR_TYPE;


    /**
     * 宝具副本产出单位时间
     */
    public static int TREASURE_WARE_RES_OUTPUT_TIME_UNIT;

    /**
     * 宝具洗练, 主体宝具属性系数
     */
    public static Map<Integer, Integer> TREASURE_WARE_MASTER_ATTR_COEFFICIENT;
    /**
     * 宝具洗练, 主体宝具属性评价系数
     */
    public static List<Integer> TREASURE_WARE_MASTER_STAGE_COEFFICIENT;
    /**
     * 宝具洗练, 材料宝具属性系数
     */
    public static Map<Integer, Integer> TREASURE_WARE_MATERIAL_ATTR_COEFFICIENT;
    /**
     * 宝具洗练, 材料宝具属性评价系数
     */
    public static List<Integer> TREASURE_WARE_MATERIAL_STAGE_COEFFICIENT;
    /**
     * 蓝/紫宝具总体评价区间
     */
    public static List<List<Integer>> TREASURE_WARE_LOWER_STAGE;
    /**
     * 橙/红/远古宝具总体评价区间
     */
    public static List<List<Integer>> TREASURE_WARE_HIGHER_STAGE;
    /**
     * 宝具属性阶数对应的评估分值 KEY: 属性阶数 VALUE: 阶数对应的分值
     */
    public static Map<Integer, Integer> TREASURE_WARE_ATTR_STAGE_SCORE;

    //图腾 万能碎片
    public static int TOTEM_UNIVERSAL_CHIP_ID;
    //图腾 图腾符文时强化增加概率，万分比
    public static int TOTEM_QH_MATERIAL_PROB;
    //图腾 单次强化使用符文石上限
    public static int TOTEM_QH_MATERIAL_MAX;
    //图腾 图腾不同栏位的解锁等级
    public static List<Integer> TOTEM_UNLOCK_PLACE_LV;
    /**
     * 提前多长时间进入跨服战火地图
     */
    public static int EARLY_ENTRY_CROSS_MAP_PERIOD;

    public static List<List<Integer>> LONG_LIGHT_DAY_AWARD;

    /**
     * 跨服排名第一的人物形象奖励
     */
    public static List<Integer> CROSS_WAR_FIRE_WINNER_PORTRAIT;

    /**
     * 品质与万能碎片的兑换
     */
    public static List<List<Integer>> EXCHANGE_OF_QUALITY_AND_UNIVERSAL_FRAGMENT;

    /**
     * 征战自选箱掉落上限
     */
    public static List<List<Integer>> BATTLE_PICK_BOX_DROP_CAP;

    /**
     * 叛军掉落图纸保底配置
     */
    public static List<List<Integer>> REBEL_DROP_BLUEPRINT_GUARANTEE_CONFIGURATION;

    /**
     * s_system表中定义的常量初始化
     */
    public static void loadSystem() {

        TREASURE_WARE_ATTR_STAGE_SCORE = SystemTabLoader.getMapIntSystemValue(SystemId.TREASURE_WARE_ATTR_STAGE_SCORE, "[[]]");
        TREASURE_WARE_HIGHER_STAGE = SystemTabLoader.getListListIntSystemValue(SystemId.TREASURE_WARE_HIGHER_STAGE, "[[]]");
        TREASURE_WARE_LOWER_STAGE = SystemTabLoader.getListListIntSystemValue(SystemId.TREASURE_WARE_LOWER_STAGE, "[[]]");
        TREASURE_WARE_MATERIAL_STAGE_COEFFICIENT = SystemTabLoader.getListIntSystemValue(SystemId.TREASURE_WARE_MATERIAL_STAGE_COEFFICIENT, "[]");
        TREASURE_WARE_MATERIAL_ATTR_COEFFICIENT = SystemTabLoader.getMapIntSystemValue(SystemId.TREASURE_WARE_MATERIAL_ATTR_COEFFICIENT, "[[]]");
        TREASURE_WARE_MASTER_STAGE_COEFFICIENT = SystemTabLoader.getListIntSystemValue(SystemId.TREASURE_WARE_MASTER_STAGE_COEFFICIENT, "[]");
        TREASURE_WARE_MASTER_ATTR_COEFFICIENT = SystemTabLoader.getMapIntSystemValue(SystemId.TREASURE_WARE_MASTER_ATTR_COEFFICIENT, "[[]]");

        LONG_LIGHT_DAY_AWARD = ActParamTabLoader.getListListIntSystemValue(369, "[[]]");

        TOTEM_UNIVERSAL_CHIP_ID = SystemTabLoader.getIntegerSystemValue(3005, 100193);
        TOTEM_QH_MATERIAL_PROB = SystemTabLoader.getIntegerSystemValue(3000, 400);
        TOTEM_QH_MATERIAL_MAX = SystemTabLoader.getIntegerSystemValue(3001, 10);
        TOTEM_UNLOCK_PLACE_LV = SystemTabLoader.getListIntSystemValue(3002, "[120,125,130,135,140,145,150,155]");

        DOCK_RED_MATERIAL_SCHEDULE = SystemTabLoader.getIntegerSystemValue(1087, 13);
        FORGE_RED_EQUIP_SCHEDULE = SystemTabLoader.getIntegerSystemValue(1086, 13);
        CAMP_REBUILD_LEAVE_SECONDS = SystemTabLoader.getIntegerSystemValue(1085, 1800);

        RPC_UPLOAD_LORD_DATA_LOWEST_LV = SystemTabLoader.getIntegerSystemValue(SystemId.RPC_UPLOAD_LORD_DATA_LOWEST_LV, 40);
        //赛季天赋
        SEASON_TALENT_RESET_COST_PROP = SystemTabLoader.getListIntSystemValue(SystemId.SEASON_TALENT_RESET_COST_PROP, "[]");
        SEASON_TALENT_RESET_COST_DIAMOND = SystemTabLoader.getListIntSystemValue(SystemId.SEASON_TALENT_RESET_COST_DIAMOND, "[300,100]");
        SEASON_TALENT_RESET_RETURN_RATE = SystemTabLoader.getIntegerSystemValue(SystemId.SEASON_TALENT_RESET_RETURN_RATE, 8000);

        // 默认的装扮配置
        DEFAULT_NAME_PLATE_ID = SystemTabLoader.getIntegerSystemValue(SystemId.DEFAULT_NAME_PLATE_ID, 1);
        DEFAULT_PORTRAIT_FRAME_ID = SystemTabLoader.getIntegerSystemValue(SystemId.DEFAULT_PORTRAIT_FRAME_ID, 1);
        DEFAULT_MARCH_LINE_ID = SystemTabLoader.getIntegerSystemValue(SystemId.DEFAULT_MARCH_LINE_ID, 1);


        ONHOOK_1061 = SystemTabLoader.getIntegerSystemValue(1061, 100);
        ONHOOK_1062 = SystemTabLoader.getIntegerSystemValue(1062, 300);
        ONHOOK_1063 = SystemTabLoader.getIntegerSystemValue(1063, 120);
        ONHOOK_1064 = SystemTabLoader.getIntegerSystemValue(1064, 30000);
        ONHOOK_1065 = SystemTabLoader.getListIntSystemValue(1065, "[10,100]");
        ONHOOK_1066 = SystemTabLoader.getIntegerSystemValue(1066, 100);

        SAND_TABLE_PREVIEW = SystemTabLoader.getStringSystemValue(1052, "0 0 9 ? * SAT");
        SAND_TABLE_OPEN_END = SystemTabLoader.getListStringSystemValue(1053, "[]");
        SAND_TABLE_1054 = SystemTabLoader.getIntegerSystemValue(1054, 1200);
        SAND_TABLE_1055 = SystemTabLoader.getIntegerSystemValue(1055, 54000);
        SAND_TABLE_1056 = SystemTabLoader.getIntegerSystemValue(1056, 2);
        SAND_TABLE_1057 = SystemTabLoader.getIntegerSystemValue(1057, 25);
        SAND_TABLE_1058 = SystemTabLoader.getListListIntSystemValue(1058, "[[]]");

        // ROLE_ID_LEN = SystemTabLoader.getIntegerSystemValue(SystemId.ROLE_ID_LEN, 7);
        // ROLE_ID_MULTI = (int) Math.pow(10, SystemTabLoader.getIntegerSystemValue(SystemId.ROLE_ID_LEN, 7));
        OPEN_WORLD_LV = SystemTabLoader.getIntegerSystemValue(SystemId.OPEN_WORLD_LV, 10);
        // ROLE_MAX_LV = SystemTabLoader.getIntegerSystemValue(SystemId.ROLE_MAX_LV, 80);
        MARCH_TIME_RATIO = SystemTabLoader.getIntegerSystemValue(SystemId.MARCH_TIME_RATIO, 8);
        DODGE_PRO = SystemTabLoader.getIntegerSystemValue(SystemId.DODGE_PRO, 500);
        CRIT_PRO = SystemTabLoader.getIntegerSystemValue(SystemId.CRIT_PRO, 500);
        CRIT_MULTI = SystemTabLoader.getFloatSystemValue(SystemId.CRIT_MULTI, 1.5f);
        RES_GAIN_MAX = SystemTabLoader.getIntegerSystemValue(SystemId.RES_GAIN_MAX, 12);
        FACTORY_EXPAND_NINT_LEVL = SystemTabLoader.getIntegerSystemValue(SystemId.FACTORY_EXPAND_NINT_LEVL, 1);
        FACTORY_TIME_NINT_LEVL = SystemTabLoader.getIntegerSystemValue(SystemId.FACTORY_TIME_NINT_LEVL, 1);
        INIT_COMBAT_ID = SystemTabLoader.getIntegerSystemValue(SystemId.INIT_COMBAT_ID, 1011);
        RED_DROP_AWARD_LV = SystemTabLoader.getIntegerSystemValue(SystemId.RED_DROP_AWARD_LV, 1);
        REGISTER_REWARD = SystemTabLoader.getListListIntSystemValue(SystemId.REGISTER_REWARD, "[[]]");
        EQUIP_MAX_BAPTIZECNT = SystemTabLoader.getIntegerSystemValue(SystemId.EQUIP_MAX_BAPTIZECNT, 12);
        EQUIP_BAPTIZECNT_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.EQUIP_BAPTIZECNT_TIME, 3600);
        EQUIP_GOLD_BAPTIZE_1 = SystemTabLoader.getIntegerSystemValue(SystemId.EQUIP_GOLD_BAPTIZE_1, 5);
        EQUIP_GOLD_BAPTIZE_2 = SystemTabLoader.getIntegerSystemValue(SystemId.EQUIP_GOLD_BAPTIZE_2, 200);
        SHOP_OFF_RANDOM_NUM = SystemTabLoader.getIntegerSystemValue(SystemId.SHOP_OFF_RANDOM_NUM, 3);
        SHOP_OFF = SystemTabLoader.getIntegerSystemValue(SystemId.SHOP_OFF, 80);
        BAG_INIT_CNT = SystemTabLoader.getIntegerSystemValue(SystemId.BAG_INIT_CNT, 30);
        BAG_BUY = SystemTabLoader.getListListIntSystemValue(SystemId.BAG_BUY, "[[]]");
        TREASURE_OPEN_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.TREASURE_OPEN_TIME, 60);
        TREASURE_TRADE_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.TREASURE_TRADE_TIME, 600);
        TREASURE_TRADE_MAX_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.TREASURE_TRADE_MAX_TIME, 7200);
        AUTO_ARM_NEED_TECH_LV = SystemTabLoader.getIntegerSystemValue(SystemId.AUTO_ARM_NEED_TECH_LV, 1);
        REBUILD_PROTECT_HIGHT = SystemTabLoader.getIntegerSystemValue(SystemId.REBUILD_PROTECT_HIGHT, 7200);
        REBUILD_PROTECT_NORMAL = SystemTabLoader.getIntegerSystemValue(SystemId.REBUILD_PROTECT_NORMAL, 3600);
        ROLE_OPT_NUM = SystemTabLoader.getIntegerSystemValue(SystemId.ROLE_OPT_NUM, 6);
        FACTORY_ARM_NEED_FOOD = SystemTabLoader.getIntegerSystemValue(SystemId.FACTORY_ARM_NEED_FOOD, 5);
        WALL_CHANGE_ARMY_GOLD = SystemTabLoader.getIntegerSystemValue(SystemId.WALL_CHANGE_ARMY_GOLD, 100);
        WALL_HELP_MAX_NUM = SystemTabLoader.getIntegerSystemValue(SystemId.WALL_HELP_MAX_NUM, 10);
        WALL_FULL_ARMY_NEED_GOLD = SystemTabLoader.getIntegerSystemValue(SystemId.WALL_FULL_ARMY_NEED_GOLD, 100);
        POWER_MAX = SystemTabLoader.getIntegerSystemValue(SystemId.POWER_MAX, 100);
        POWER_BACK_SECOND = SystemTabLoader.getIntegerSystemValue(SystemId.POWER_BACK_SECOND, 360);
        // HERO_BREAK_PRO = SystemTabLoader.getListListIntSystemValue(SystemId.HERO_BREAK_PRO,
        // "[[10,20,10],[21,300,20]]");
        RES_ADD_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.RES_ADD_TIME, 300);
        MAX_ROLE_LV = SystemTabLoader.getIntegerSystemValue(SystemId.MAX_ROLE_LV, 150);
        MAX_COMMAND_LV = SystemTabLoader.getIntegerSystemValue(SystemId.MAX_COMMAND_LV, 25);
        ARMY_STATE_GUARD_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.ARMY_STATE_GUARD_TIME, 8);
        CITY_RENAME_COST = SystemTabLoader.getIntegerSystemValue(SystemId.CITY_RENAME_COST, 2);
        MONTH_CARD_REWARD = SystemTabLoader.getListListIntSystemValue(SystemId.MONTH_CARD_REWARD, "[[1,3,100]]");
        MAX_FRIEND_COUNT = SystemTabLoader.getIntegerSystemValue(SystemId.MAX_FRIEND_COUNT, 100);
        MAX_APPRENTICE_COUNT = SystemTabLoader.getIntegerSystemValue(SystemId.MAX_APPRENTICE_COUNT, 30);
        MAX_APPRENTICE_LV = SystemTabLoader.getIntegerSystemValue(SystemId.MAX_APPRENTICE_LV, 49);
        MIN_MASTER_LV = SystemTabLoader.getIntegerSystemValue(SystemId.MIN_MASTER_LV, 50);
        ADD_MASTER_REWARD = SystemTabLoader.getListListIntSystemValue(SystemId.ADD_MASTER_REWARD, "[[1,3,100]]");
        WASH_TOTAL_FLOOR_COUNT = SystemTabLoader.getIntegerSystemValue(SystemId.WASH_TOTAL_FLOOR_COUNT, 6);
        ADD_WASH_TOTAL = SystemTabLoader.getIntegerSystemValue(SystemId.ADD_WASH_TOTAL, 1);
        WASH_FREE_FLUCTUATE = SystemTabLoader.getListListIntSystemValue(SystemId.WASH_FREE_FLUCTUATE, "[[]]");
        WASH_PAY_FLUCTUATE = SystemTabLoader.getListListIntSystemValue(SystemId.WASH_PAY_FLUCTUATE, "[[]]");
        BLACKHAWK_FREE_COUNT = SystemTabLoader.getIntegerSystemValue(SystemId.BLACKHAWK_FREE_COUNT, 4);
        BLACKHAWK_REFRESH_INTERVAL = SystemTabLoader.getIntegerSystemValue(SystemId.BLACKHAWK_REFRESH_INTERVAL, 18000);
        BLACKHAWK_INIT_PAY_GOLD = SystemTabLoader.getIntegerSystemValue(SystemId.BLACKHAWK_INIT_PAY_GOLD, 10);
        BLACKHAWK_INCR_GOLD = SystemTabLoader.getIntegerSystemValue(SystemId.BLACKHAWK_INCR_GOLD, 10);
        // BLACKHAWK_HERO_ID = SystemTabLoader.getListIntSystemValue(SystemId.BLACKHAWK_HERO_ID, "[]");
        BLACKHAWK_NEED_TOKEN = SystemTabLoader.getIntegerSystemValue(SystemId.BLACKHAWK_NEED_TOKEN, 7);
        ACQ_HERO_REQUIRE = SystemTabLoader.getListIntSystemValue(SystemId.ACQ_HERO_REQUIRE, "[]");
        COMMANDO_HERO_REQUIRE = SystemTabLoader.getListIntSystemValue(SystemId.COMMANDO_HERO_REQUIRE, "[]");
        GUARDS_HERO_REQUIRE = SystemTabLoader.getListIntSystemValue(SystemId.GUARDS_HERO_REQUIRE, "[]");
        CABINET_INIT_ID = SystemTabLoader.getIntegerSystemValue(SystemId.CABINET_INIT_ID, 1);
        CABINET_CONDITION = SystemTabLoader.getListIntSystemValue(SystemId.CABINET_CONDITION, "[1,2,3]");
        SUPPLY_DORP_GOLD = SystemTabLoader.getListIntSystemValue(SystemId.SUPPLY_DORP_GOLD, "[500,500,500]");
        REBUILD_AWARD_NORMAL = SystemTabLoader.getIntegerSystemValue(SystemId.REBUILD_AWARD_NORMAL, 1);
        REBUILD_AWARD_HIGHT = SystemTabLoader.getIntegerSystemValue(SystemId.REBUILD_AWARD_HIGHT, 1);
        BUY_ACT_PRICE = SystemTabLoader.getListIntSystemValue(SystemId.BUY_ACT_PRICE, "[]");
        BUY_ACT_REWARD = SystemTabLoader.getIntegerSystemValue(SystemId.BUY_ACT_REWARD, 1);
        SUMMON_KEEP_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.SUMMON_KEEP_TIME, 600);
        SUMMON_NEED_PROP_CNT = SystemTabLoader.getMapIntSystemValue(SystemId.SUMMON_NEED_PROP_CNT,
                "[[1,4],[2,4],[3,4],[4,4]]");
        SUMMON_RADIUS = SystemTabLoader.getIntegerSystemValue(SystemId.SUMMON_RADIUS, 3);
        SUMMON_RESPOND_RADIUS = SystemTabLoader.getIntegerSystemValue(SystemId.SUMMON_RESPOND_RADIUS, 3);
        SMALL_ID_LV = SystemTabLoader.getIntegerSystemValue(SystemId.SMALL_ID_LV, 10);
        SMALL_LOGIN_DAY = SystemTabLoader.getIntegerSystemValue(SystemId.SMALL_LOGIN_DAY, 30);
        OPEN_ACT_GROW_NEED = SystemTabLoader.getListIntSystemValue(SystemId.OPEN_ACT_GROW_NEED, "[]");
        ACT_FIRSH_CHARGE_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.ACT_FIRSH_CHARGE_TIME, 604800);
        ACT_BLACK_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.ACT_BLACK_TIME, 345600);
        DEL_MAIL_DAY = SystemTabLoader.getIntegerSystemValue(SystemId.DEL_MAIL_DAY, 7);
        RES_BUY_BASE_VAL = SystemTabLoader.getListIntSystemValue(SystemId.RES_BUY_BASE_VAL, "[4000,3000,2000,500]");
        MAIL_FOR_CREATE_ROLE = SystemTabLoader.getListListIntSystemValue(SystemId.MAIL_FOR_CREATE_ROLE, "[[]]");
        PAY_VIP_COEFF = SystemTabLoader.getIntegerSystemValue(SystemId.PAY_VIP_COEFF, 10);
        BUY_BUILD_GAIN_GOLD = SystemTabLoader.getListIntSystemValue(SystemId.BUY_BUILD_GAIN_GOLD, "[]");
//        TECH_QUICK_VIP_BAG = SystemTabLoader.getIntegerSystemValue(SystemId.TECH_QUICK_VIP_BAG, 5);
        PRIVATE_CHAT_COST_GOLD = SystemTabLoader.getIntegerSystemValue(SystemId.PRIVATE_CHAT_COST_GOLD, 2);
        PRIVATE_CHAT_INTERVAL = SystemTabLoader.getIntegerSystemValue(SystemId.PRIVATE_CHAT_INTERVAL, 60);
        ACT_DOUBLE_NUM = SystemTabLoader.getIntegerSystemValue(SystemId.ACT_DOUBLE_NUM, 2);
        ACT_PRODUCTTION_NUM = SystemTabLoader.getIntegerSystemValue(SystemId.ACT_PRODUCTTION_NUM, 5000);
        EQUIP_DECOMPOSE_RATE = SystemTabLoader.getIntegerSystemValue(SystemId.EQUIP_DECOMPOSE_RATE, 0);
        EQUIP_DECOMPOSE_VIP_BAG = SystemTabLoader.getIntegerSystemValue(SystemId.EQUIP_DECOMPOSE_VIP_BAG, 6);
        APPOINT_COST = SystemTabLoader.getIntegerSystemValue(SystemId.APPOINT_COST, 100);
        EQUIP_BAPTIZE_MIN_UPLV_CONF = SystemTabLoader.getMapIntSystemValue(SystemId.EQUIP_BAPTIZE_MIN_UPLV_CONF,
                "[[]]");
        HOME_CITY_ADD_EXP_COLLECT_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.HOME_CITY_ADD_EXP_COLLECT_TIME,
                32400);
        PLAYER_WORLD_CHAT_SHOW_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.PLAYER_WORLD_CHAT_SHOW_TIME,
                259200);
        SECTIONTASK_BEGIN_HERO_ID = SystemTabLoader.getListIntSystemValue(SystemId.SECTIONTASK_BEGIN_HERO_ID, "[]");
        SENTRY_DSN = SystemTabLoader.getStringSystemValue(SystemId.SENTRY_DSN, null);
        SentryHelper.initSentry(SENTRY_DSN);
        GARRISON_WALL_REQUIRE_LV = SystemTabLoader.getIntegerSystemValue(SystemId.GARRISON_WALL_REQUIRE_LV, 15);
        SHOW_CURTASK_TRIGGET_ID = SystemTabLoader.getIntegerSystemValue(SystemId.SHOW_CURTASK_TRIGGET_ID, 10);
        START_SOLAR_TERMS_CITY_TYPE = SystemTabLoader.getIntegerSystemValue(SystemId.START_SOLAR_TERMS_CITY_TYPE, 5);
        DAY_AND_NIGHT_RANGE = SystemTabLoader.getListIntSystemValue(SystemId.DAY_AND_NIGHT_RANGE, "[1,6]");
        DAY_AND_NIGHT_DEF_BONUS = SystemTabLoader.getMapIntSystemValue(SystemId.DAY_AND_NIGHT_DEF_BONUS,
                "[[1,20],[2,20]]");
        ACT_ALL_CHARGE_REWARD = SystemTabLoader.getListListIntSystemValue(SystemId.ACT_ALL_CHARGE_REWARD, "[[]]");

        AREA_REAL_PLAER_CAPACITY = SystemTabLoader.getIntegerSystemValue(SystemId.AREA_REAL_PLAER_CAPACITY, 1000);
        USE_PROP_ADD_POWER_MAX = SystemTabLoader.getIntegerSystemValue(SystemId.USE_PROP_ADD_POWER_MAX, 20);
        REFRESH_BANDITS_OFFLINE_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.REFRESH_BANDITS_OFFLINE_TIME,
                3600);
        LOW_MOVE_CITY_PROBABILITY = SystemTabLoader.getIntegerSystemValue(SystemId.LOW_MOVE_CITY_PROBABILITY, 55);
        ATTACK_BANDIT_MAX = SystemTabLoader.getIntegerSystemValue(SystemId.ATTACK_BANDIT_MAX, 300);
        ATTACK_WORLD_BOSS_MAX = SystemTabLoader.getIntegerSystemValue(SystemId.ATTACK_WORLD_BOSS_MAX, 2);
        BUY_WORLD_BOSS_GOLD = SystemTabLoader.getIntegerSystemValue(SystemId.BUY_WORLD_BOSS_GOLD, 30);
        MINE_RM_MIN_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.MINE_RM_MIN_TIME, 600);
        BUILD_UPGRADE_TIME_EXCEEDED = SystemTabLoader.getIntegerSystemValue(SystemId.BUILD_UPGRADE_TIME_EXCEEDED, 3600);
        RECOMMEND_PLAYER_CNT = SystemTabLoader.getIntegerSystemValue(SystemId.RECOMMEND_PLAYER_CNT, 5);
        AWARD_COMMERCIAL_BUILD_QUEUEN_TASKID = SystemTabLoader
                .getIntegerSystemValue(SystemId.AWARD_COMMERCIAL_BUILD_QUEUEN_TASKID, 0);
        DONT_DODGE_CRIT_COMBATID = SystemTabLoader.getListIntSystemValue(SystemId.DONT_DODGE_CRIT_COMBATID, "[]");
        REWARD_HERO_EQUIP = SystemTabLoader.getListListIntSystemValue(SystemId.REWARD_HERO_EQUIP, "[[]]");
        REWARD_EQUIP_HERO_IDS = SystemTabLoader.getListIntSystemValue(SystemId.REWARD_EQUIP_HERO_IDS, "[]");
        COMBAT_STAR_RULE = SystemTabLoader.getListListIntSystemValue(SystemId.COMBAT_STAR_RULE, "[[]]");
        CITY_FIRST_KILL_REWARD = SystemTabLoader.getListListListIntSystemValue(SystemId.CITY_FIRST_KILL_REWARD,
                "[[[]]]");
        CITY_TYPE_KILL_REWARD = new HashMap<>();
        if (!CheckNull.isEmpty(CITY_FIRST_KILL_REWARD)) {
            for (int i = 0; i < CITY_FIRST_KILL_REWARD.size(); i++) {
                CITY_TYPE_KILL_REWARD.put(i + 1, CITY_FIRST_KILL_REWARD.get(i));
            }
        }
        STONE_COMBAT_BUY_PRICE = SystemTabLoader.getListIntSystemValue(SystemId.STONE_COMBAT_BUY_PRICE, "[]");
        STONE_IMPROVE_UP_MAX_CONF = SystemTabLoader.getIntegerSystemValue(SystemId.STONE_IMPROVE_UP_MAX_CONF, 2000);
        TRAIN_FACTORY_NORMAL = SystemTabLoader.getIntegerSystemValue(SystemId.TRAIN_FACTORY_NORMAL, 8000);
        CIA_INTERACTION_MAX_CNT = SystemTabLoader.getIntegerSystemValue(SystemId.CIA_INTERACTION_MAX_CNT, 50);
        CIA_APPOINTMENT_MAX_CNT = SystemTabLoader.getIntegerSystemValue(SystemId.CIA_APPOINTMENT_MAX_CNT, 1);
//        OPEN_CIA_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.OPEN_CIA_TIME, 2);
        CIA_INTERACTION_WIN = SystemTabLoader.getIntegerSystemValue(SystemId.CIA_INTERACTION_WIN, 3);
        CIA_INTERACTION_FAIL = SystemTabLoader.getIntegerSystemValue(SystemId.CIA_INTERACTION_FAIL, 1);
        CIA_APPOINTMENT_CRIT = SystemTabLoader.getIntegerSystemValue(SystemId.CIA_APPOINTMENT_CRIT, 5);
//        CIA_INTERACT_INTIMACY_CRIT = SystemTabLoader.getListListIntSystemValue(SystemId.CIA_INTERACT_INTIMACY_CRIT,
//                "[[]]");
        SUPERMINE_STOP_TO_RESET_CD = SystemTabLoader.getIntegerSystemValue(SystemId.SUPERMINE_STOP_TO_RESET_CD, 3600);
        SUPERMINE_RESET_TO_PRODUCED_CD = SystemTabLoader.getIntegerSystemValue(SystemId.SUPERMINE_RESET_TO_PRODUCED_CD,
                3600);
        ATK_BANDIT_MARCH_TIME_TASKID = SystemTabLoader.getListIntSystemValue(SystemId.ATK_BANDIT_MARCH_TIME_TASKID,
                "[]");
        CLEAR_SCORPION_ACTIVATE_END_TIME_TASK = SystemTabLoader.getIntegerSystemValue(SystemId.CLEAR_SCORPION_ACTIVATE_END_TIME_TASK, 1033);
        ALICE_RESCUE_MISSION_TASK = SystemTabLoader.getListIntSystemValue(SystemId.ALICE_RESCUE_MISSION_TASK, "[]");
        ALICE_RESCUE_MISSION_MAIL_AWARD = SystemTabLoader.getListListIntSystemValue(SystemId.ALICE_RESCUE_MISSION_MAIL_AWARD, "[[]]");
        USE_HERO_PORTRAIT = SystemTabLoader.getMapIntSystemValue(SystemId.USE_HERO_PORTRAIT, "[[]]");
        ROLE_45_DIGIT = SystemTabLoader.getIntegerSystemValue(SystemId.ROLE_45_DIGIT, 50);
        PART_PURPLE_RANK = SystemTabLoader.getIntegerSystemValue(SystemId.PART_PURPLE_RANK, 3);
        PART_ORANGE_RANK = SystemTabLoader.getIntegerSystemValue(SystemId.PART_ORANGE_RANK, 5);
        EQUIP_MAKE_RANGE = SystemTabLoader.getListIntSystemValue(SystemId.EQUIP_MAKE_RANGE, "[10000,62]");
        ORE_TURNPLATE_CONFIG = SystemTabLoader.getListListIntSystemValue(SystemId.ORE_TURNPLATE_CONFIG, "[[]]");
        ALLOC_POS_CONDITION = SystemTabLoader.getIntegerSystemValue(SystemId.ALLOC_POS_CONDITION, 1059);
        FAMOUS_GENERAL_TURNPLATE_CHATS_CNT = SystemTabLoader
                .getIntegerSystemValue(SystemId.FAMOUS_GENERAL_TURNPLATE_CHATS_CNT, 20);
        SEND_CHAT_PROP_IDS = SystemTabLoader.getListListIntSystemValue(SystemId.SEND_CHAT_PROP_IDS, "[[]]");
        FAMOUS_GENERAL_EXCHANGE_PROP = SystemTabLoader.getListListIntSystemValue(SystemId.FAMOUS_GENERAL_EXCHANGE_PROP,
                "[[]]");
        EXP_EXCHANGE_CREDIT = SystemTabLoader.getListIntSystemValue(SystemId.EXP_EXCHANGE_CREDIT, "[500,1]");
        REBEL_START_TIME_CFG = SystemTabLoader.getListListIntSystemValue(SystemId.REBEL_START_TIME_CFG, "[[]]");
        REBEL_ROLE_LV_COND = SystemTabLoader.getIntegerSystemValue(SystemId.REBEL_ROLE_LV_COND, 70);
        REBEL_ALL_PASS_AWARD = SystemTabLoader.getIntegerSystemValue(SystemId.REBEL_ALL_PASS_AWARD, 2000);
        REBEL_WORLD_TASKID_COND = SystemTabLoader.getIntegerSystemValue(SystemId.REBEL_WORLD_TASKID_COND, 3);
        STONE_IMPROVE_NEED_COST = SystemTabLoader.getListListIntSystemValue(SystemId.STONE_IMPROVE_NEED_COST, "[[]]");
        RANK_PAGE_CNT = SystemTabLoader.getIntegerSystemValue(SystemId.RANK_PAGE_CNT, 20);
        CITY_FORM_SERVERID = SystemTabLoader.getListListIntSystemValue(SystemId.CITY_FORM_SERVERID, "[[]]");
        IGNORE_LOG_CMD = SystemTabLoader.getListIntSystemValue(SystemId.IGNORE_LOG_CMD, "[]").stream()
                .collect(Collectors.toSet());
        MEMBER_OF_COMBAT_TEAM = SystemTabLoader.getIntegerSystemValue(SystemId.MEMBER_OF_COMBAT_TEAM, 3);
        COMBAT_TEAM_CHAT_CD = SystemTabLoader.getIntegerSystemValue(SystemId.COMBAT_TEAM_CHAT_CD, 10);
        COMBAT_TEAM_AWARD_CNT = SystemTabLoader.getIntegerSystemValue(SystemId.COMBAT_TEAM_AWARD_CNT, 10);

        AIRSHIP_TIME_CFG = SystemTabLoader.getListListIntSystemValue(SystemId.AIRSHIP_TIME_CFG, "[[]]");
        AIRSHIP_WORLDTASKID_CFG = SystemTabLoader.getIntegerSystemValue(SystemId.AIRSHIP_WORLDTASKID_CFG, 5);
        AIRSHIP_JOIN_MEMBER_CNT = SystemTabLoader.getIntegerSystemValue(SystemId.AIRSHIP_JOIN_MEMBER_CNT, 5);
        AIRSHIP_CAN_AWARD_CNT = SystemTabLoader.getListIntSystemValue(SystemId.AIRSHIP_CAN_AWARD_CNT, "[5,10]");
        ATTACK_BANDIT_MARCH_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.ATTACK_BANDIT_MARCH_TIME, 3);
        TASK_GIVE_GAINRES = SystemTabLoader.getListListIntSystemValue(SystemId.TASK_GIVE_GAINRES, "[[]]");
        WORLDWAR_OPEN_WEEK = SystemTabLoader.getListIntSystemValue(SystemId.WORLDWAR_OPEN_WEEK, "[]");
        ENTER_CROSS_MAP_PROP = SystemTabLoader.getListIntSystemValue(SystemId.ENTER_CROSS_MAP_PROP, "[]");
        LEAVE_CROSS_MAP_PROP = SystemTabLoader.getListIntSystemValue(SystemId.LEAVE_CROSS_MAP_PROP, "[]");
        CROSS_MAP_RANDOM_CELLID = SystemTabLoader.getListListIntSystemValue(SystemId.CROSS_MAP_RANDOM_CELLID,
                "[[],[93,83,73,72,71,94,84,74,64,63,62,61],[98,88,78,79,80,97,87,77,67,68,69,70],[21,22,23,13,3,31,32,33,34,24,14,4]]");
        // 推送是否开启 1 开启 0 关闭
        PUSH_CONFIG_SWITCH = SystemTabLoader.getIntegerSystemValue(SystemId.PUSH_CONFIG_SWITCH, 0);
        LogUtil.start("推送初始化, url:" + PushConstant.PUSH_SERVER_URL + ", PUSH_CONFIG_SWITCH:" + PUSH_CONFIG_SWITCH);
        RECOMMEND_PLAYER_OFF_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.RECOMMEND_PLAYER_OFF_TIME, 0);
        COUNTER_ATK_FUNCTION_LOCK = SystemTabLoader.getIntegerSystemValue(SystemId.COUNTER_ATK_FUNCTION_LOCK, 0);
        CROSS_FIGHT_OPEN_TIME = SystemTabLoader.getListListIntSystemValue(SystemId.CROSS_FIGHT_OPEN_TIME,
                "[[7,20,5400]]");
        CROSS_OPERATE_HERO_COST = SystemTabLoader.getListListIntSystemValue(SystemId.CROSS_OPERATE_HERO_COST,
                "[[1,3,10],[1,3,10],[1,3,10]]");
        CROSS_REVIVE_HERO_COST = SystemTabLoader.getListIntSystemValue(SystemId.CROSS_REVIVE_HERO_COST, "[10,20]");
        CROSS_REVIVE_HERO_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.CROSS_REVIVE_HERO_TIME, 60);
        CROSS_JOIN_AWARD = SystemTabLoader.getListListIntSystemValue(SystemId.CROSS_JOIN_AWARD, "[[4,1001,1],[4,1002,1]]");
        CROSS_WIN_AWARD = SystemTabLoader.getListListIntSystemValue(SystemId.CROSS_WIN_AWARD, "[[4,1001,10],[4,1002,10]]");
        JOIN_COMMUNITY_AWARD = SystemTabLoader.getIntegerSystemValue(SystemId.JOIN_COMMUNITY_AWARD, 200);
        TRIGGER_GIFT_TREASURE_OPEN_PROBABILITY = SystemTabLoader.getListListIntSystemValue(SystemId.TRIGGER_GIFT_TREASURE_OPEN_PROBABILITY, "[[5000,2],[314,315]]");
        ADVERTISEMENT_REWARD = SystemTabLoader.getListListIntSystemValue(SystemId.ADVERTISEMENT_REWARD, "[[]]");
        REGISTER_FOUR_REWARD = SystemTabLoader.getListListIntSystemValue(SystemId.REGISTER_FOUR_REWARD, "[[]]");
        REGISTER_SEVEN_REWARD = SystemTabLoader.getListListIntSystemValue(SystemId.REGISTER_SEVEN_REWARD, "[[]]");
        THINKING_DATA_PLAT = SystemTabLoader.getListIntSystemValue(SystemId.THINKING_DATA_PLAT, "[]");
        AUTO_SENDMAIL_LEVEL = SystemTabLoader.getIntegerSystemValue(SystemId.AUTO_SENDMAIL_LEVEL, 0);
        AUTO_SENDMAIL_VIP_LV = SystemTabLoader.getIntegerSystemValue(SystemId.AUTO_SENDMAIL_VIP_LV, 0);
        DEL_MASTER_NEED_OFFLINE_DAY = SystemTabLoader.getIntegerSystemValue(SystemId.DEL_MASTER_NEED_OFFLINE_DAY, 0);
        DEL_APPRENTICE_NEED_OFFLINE_DAY = SystemTabLoader.getIntegerSystemValue(SystemId.DEL_APPRENTICE_NEED_OFFLINE_DAY, 0);
        ADD_MASTER_AGAIN_CD_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.ADD_MASTER_AGAIN_CD_TIME, 0);
        BERLIN_WINNER_SKIN_ID = SystemTabLoader.getIntegerSystemValue(SystemId.BERLIN_WINNER_SKIN_ID, 0);
        CIA_FAVORABILITY_QUALITY = Optional.ofNullable(SystemTabLoader.getListListIntSystemValue(SystemId.CIA_FAVORABILITY_QUALITY, "[[]]")).orElse(new ArrayList<>());
        CIA_FAVORABILITY_QUALITY_LIST = CIA_FAVORABILITY_QUALITY.stream().map(list -> list.get(1)).collect(Collectors.toList());
        CIA_STAR_QUALITY = Optional.ofNullable(SystemTabLoader.getListListIntSystemValue(SystemId.CIA_STAR_QUALITY, "[[]]")).orElse(new ArrayList<>());
        CIA_STAR_QUALITY_LIST = CIA_STAR_QUALITY.stream().map(list -> list.get(1)).collect(Collectors.toList());
        OPEN_TALENT_BLESS_LIMIT = SystemTabLoader.getIntegerSystemValue(SystemId.OPEN_TALENT_BLESS_LIMIT, 0);
        FAIL_OPEN_TALENT_BLESS_FIXED_VALUE = SystemTabLoader.getIntegerSystemValue(SystemId.FAIL_OPEN_TALENT_BLESS_FIXED_VALUE, 0);
        OPEN_TALENT_SINGLE_BLESS_CONSUME = SystemTabLoader.getIntegerSystemValue(SystemId.OPEN_TALENT_SINGLE_BLESS_CONSUME, 0);
        OPEN_TALENT_BLESS_PROBABILITY = Optional.ofNullable(SystemTabLoader.getListListIntSystemValue(SystemId.OPEN_TALENT_BLESS_PROBABILITY, "[[]]")).orElse(new ArrayList<>());

        MAIL_MAX_SAVE_COUNT = SystemTabLoader.getIntegerSystemValue(SystemId.MAIL_MAX_SAVE_COUNT, 30);
        ATTACK_REPORT_EXPIRE_TIME = initMailExpireData(SystemTabLoader.getListListIntSystemValue(SystemId.ATTACK_REPORT_EXPIRE_TIME, "[[]]"));
        DEFENCE_REPORT_EXPIRE_TIME = initMailExpireData(SystemTabLoader.getListListIntSystemValue(SystemId.DEFENCE_REPORT_EXPIRE_TIME, "[[]]"));
        EARLY_ENTRY_CROSS_MAP_PERIOD = SystemTabLoader.getIntegerSystemValue(SystemId.EARLY_ENTRY_CROSS_MAP_PERIOD, 1800);
        TREASURE_WARE_BAG_INIT = SystemTabLoader.getIntegerSystemValue(SystemId.TREASURE_WARE_BAG_INIT, 0);
        BUY_TREASURE_WARE_BAG_RULE = SystemTabLoader.getListIntSystemValue(SystemId.BUY_TREASURE_WARE_BAG_RULE, "[10,20]");

        TREASURE_UNLOCK_HERO_POS_CONF = SystemTabLoader.getListListIntSystemValue(SystemId.TREASURE_UNLOCK_HERO_POS_CONF, "[[5,2001,1,16,100],[6,3001,1,16,100],[7,4001,1,16,100],[8,5001,1,16,100]]");
        TREASURE_COMBAT_DAILY_PROMOTE_MAX = SystemTabLoader.getIntegerSystemValue(SystemId.TREASURE_COMBAT_DAILY_PROMOTE_MAX, 10);
        TREASURE_COMBAT_DAILY_WIPE_MAX = SystemTabLoader.getIntegerSystemValue(SystemId.TREASURE_COMBAT_DAILY_WIPE_MAX, 10);
        TREASURE_ON_HOOK_AGGREGATE = SystemTabLoader.getIntegerSystemValue(SystemId.TREASURE_ON_HOOK_AGGREGATE, 14400);
        List<Integer> wipePrice = SystemTabLoader.getListIntSystemValue(SystemId.TREASURE_WIPE_INCREASE_PRICE, "[0,50,100,150,200]");
        TREASURE_COMBAT_WIPE_AWARD = SystemTabLoader.getIntegerSystemValue(SystemId.TREASURE_COMBAT_WIPE_AWARD, 240);
        TREASURE_WIPE_INCREASE_PRICE = new HashMap<>();
        if (CheckNull.nonEmpty(wipePrice)) {
            for (int i = 0; i < wipePrice.size(); i++) {
                TREASURE_WIPE_INCREASE_PRICE.put(i, wipePrice.get(i));
            }
        }
        TREASURE_COMBAT_DEFAULT_UNLOCK = SystemTabLoader.getIntegerSystemValue(SystemId.TREASURE_COMBAT_DEFAULT_UNLOCK, 4);
        DEL_DECOMPOSED_TREASURE_WARE = SystemTabLoader.getIntegerSystemValue(SystemId.DEL_DECOMPOSED_TREASURE_WARE, 7);
        TREASURE_WARE_ATTACK_ATTR_TYPE = SystemTabLoader.getListIntSystemValue(SystemId.TREASURE_WARE_ATTACK_ATTR_TYPE, "[1,21,35]");
        TREASURE_WARE_DEFENCE_ATTR_TYPE = SystemTabLoader.getListIntSystemValue(SystemId.TREASURE_WARE_DEFENCE_ATTR_TYPE, "[2,22,36]");
        TREASURE_WARE_RES_OUTPUT_TIME_UNIT = SystemTabLoader.getIntegerSystemValue(SystemId.TREASURE_WARE_RES_OUTPUT_TIME_UNIT, 60);
        CROSS_WAR_FIRE_WINNER_PORTRAIT = SystemTabLoader.getListIntSystemValue(SystemId.CROSS_WAR_FIRE_WINNER_PORTRAIT, "[9,20,2592000]");
        EXCHANGE_OF_QUALITY_AND_UNIVERSAL_FRAGMENT = SystemTabLoader.getListListIntSystemValue(SystemId.EXCHANGE_OF_QUALITY_AND_UNIVERSAL_FRAGMENT, "[[]]");
        BATTLE_PICK_BOX_DROP_CAP = SystemTabLoader.getListListIntSystemValue(SystemId.BATTLE_PICK_BOX_DROP_CAP, "[[]]");
        REBEL_DROP_BLUEPRINT_GUARANTEE_CONFIGURATION = SystemTabLoader.getListListIntSystemValue(SystemId.REBEL_DROP_BLUEPRINT_GUARANTEE_CONFIGURATION, "[[]]");
    }

    private static Map<Integer, List<Integer>> initMailExpireData(List<List<Integer>> systemList) {
        Map<Integer, List<Integer>> finalData = new HashMap<>();
        Optional.ofNullable(systemList).ifPresent(list -> {
            List<Integer> moldIds = list.get(0);
            List<Integer> params = list.get(1);
            if (ObjectUtils.isEmpty(moldIds) || ObjectUtils.isEmpty(params))
                return;

            moldIds.forEach(moldId -> {
                finalData.put(moldId, params);
            });
        });

        return finalData;
    }

    /**
     * 品质定义
     */
    public interface Quality {// 品质
        int white = 1; // 白色
        int green = 2; // 绿色
        int blue = 3; // 蓝色
        int purple = 4; // 紫色
        int orange = 5; // 橙色
        int red = 6; // 红色

        int[] qualitys = {white, green, blue, purple, orange, red};
    }

    /**
     * 国家阵营定义
     */
    public interface Camp {// 国家阵营 1 帝国，2 联军，3 盟军
        int NPC = 0;// 群雄，NPC扫
        int EMPIRE = 1;// 帝国 巴比伦
        int ALLIED = 2;// 联军 欧洲
        int UNION = 3;// 盟军 亚洲

        int[] camps = {EMPIRE, ALLIED, UNION};
    }

    /**
     * 将领属性id定义
     */
    public interface AttrId {
        int ATTACK = 1;// 攻击
        int DEFEND = 2;// 防御
        int LEAD = 3;// 兵力
        int LINE = 4;// 排数
        int ATK_MUT = 11;// 攻击附加万分比
        int DEF_MUT = 12;// 防御附加万分比
        int LEAD_MUT = 13;// 兵力附加万分比
        int ATTACK_TOWN = 21;// 攻坚
        int DEFEND_TOWN = 22;// 据守
        int CRIT = 31;// 暴击伤害倍率
        int CRIT_CHANCE = 32;// 暴击概率
        int CRITDEF = 33;// 免暴值
        int ATTACK_EXT = 35;// 穿甲
        int DEFEND_EXT = 36;// 防护
        int EVADE = 44;// 闪避
        int MORE_INFANTRY_DAMAGE = 46;//对步兵伤害加成
        int MORE_CAVALRY_DAMAGE = 47;//对骑兵伤害加成
        int MORE_ARCHER_DAMAGE = 48;//对弓兵伤害加成
        int FIGHT = 999;// 战斗力, 直接加战斗力
        //----程序使用的特殊属性,用来优化代码执行,目前未加入到策划属性体系
        int DMG_INC = 10001;//增伤
        int DMG_DEC = 10002;//伤害减免
        int SPEED = 11001;
        int LESS_INFANTRY_MUT = 50;//对步兵战斗时减伤最终伤害的万分比
        int LESS_CAVALRY_MUT = 51;//对骑兵战斗时减伤最终伤害的万分比
        int LESS_ARCHER_MUT = 52;//对弓兵战斗时减伤最终伤害的万分比
        int MORE_INFANTRY_ATTACK = 101;//对步兵攻击提升
        int MORE_INFANTRY_ATTACK_EXT = 135;//对步兵破甲提升
        int MORE_CAVALRY_ATTACK = 201;//对骑兵攻击提升
        int MORE_CAVALRY_ATTACK_EXT = 235;//对骑兵破甲提升
        int MORE_ARCHER_ATTACK = 301;//对弓兵攻击提升
        int MORE_ARCHER_ATTACK_EXT = 335;//对弓兵破甲提升

    }

    public static int[] ATTRS = new int[]{AttrId.ATTACK, AttrId.DEFEND, AttrId.LEAD, AttrId.ATTACK_TOWN,
            AttrId.DEFEND_TOWN, AttrId.ATTACK_EXT, AttrId.DEFEND_EXT};

    public static int[] BASE_ATTRS = new int[]{AttrId.ATTACK, AttrId.DEFEND, AttrId.LEAD};

    /**
     * 扩展属性
     */
    public static int[] EXT_ATTRS = new int[]{AttrId.ATTACK_TOWN, AttrId.DEFEND_TOWN, AttrId.ATTACK_EXT,
            AttrId.DEFEND_EXT, AttrId.FIGHT, AttrId.LESS_INFANTRY_MUT, AttrId.LESS_CAVALRY_MUT, AttrId.LESS_ARCHER_MUT};

    /**
     * 显示战力id定义
     */
    public interface ShowFightId {
        int HERO = 1; //武将
        int EQUIP = 2; //装备
        int SUPER_EQUIP = 3; //神器
        int PARTY_RANK = 4;  //爵位
        int FEMALE_AGENT = 5; //行宫
        int STONE = 6;//配件
        int MEDAL = 7;//勋章
        int PLANE = 8;//战机
        int OTHER = 9;//科技、其它
        int SEASON = 10;//宝具
        int CASTLE_SKIN = 11;// 城堡皮肤
        int TOTEM = 12;//阵法图腾
        int TREASURE_WARE = 13;//宝具
        int HERO_BIOGRAPHY = 14;    // 武将列传
    }

    /**
     * 非战斗属性
     */
    public interface AttrIdNofight {
        int ATTACK = 1000;// 行军速度
    }

    public interface Role {
        int PLAYER = 1;// 玩家
        int BANDIT = 2;// 流寇NPC(s_npc)
        int CITY = 3;// 城池守将NPC(s_npc)
        int WALL = 4;// 城墙NPC(s_wall_hero_lv)
        int GESTAPO = 5;// 盖世太保NPC(s_npc)
    }


    /**
     * 商店
     */
    public interface ShopType {
        int shop_type_1 = 1;// 特价商店
        int shop_type_2 = 2;// 军事商店
        int shop_type_3 = 3;// 其他商店
    }

    /**
     * 特殊商店ID
     */
    public interface ShopId {
        int shop_id_1 = 1;// 银币
        int shop_id_2 = 2;// 木材
        int shop_id_3 = 3;// 粮草
        int shop_id_4 = 4;// 镔铁
        int shop_id_5 = 5;// 自动建造
        int shop_id_7 = 7;// 人口
    }

    /**
     * 完成事件ID
     */
    public interface OptId {
        int id_1 = 1;// 建筑
        int id_2 = 2;// 科技：%s%s研究完成
        int id_3 = 3;// 募兵：%s招募完成
        int id_4 = 4;// 聚宝：%s聚宝成功
        int id_5 = 5;// 装备：%s制造完成
    }

    /**
     * 固定道具ID
     */
    public interface PropId {

    }

    /**
     * 副本类型(1普通副本,2资源副本,3国器碎片副本,4招募副本,5资源建筑图纸副本,6装备图纸副本,7建筑副本,8奖励)
     *
     * @author tyler
     */
    public interface CombatType {
        int type_1 = 1;// 普通副本
        int type_2 = 2;// 资源副本
        int type_3 = 3;// 国器碎片副本
        int type_4 = 4;// 招募两个将领
        int type_5 = 5;// 资源建筑图纸副本
        int type_6 = 6;// 装备图纸副本
        int type_7 = 7;// 开启建筑副本
        int type_8 = 8;// 招募1个将领
        int type_9 = 9;// 小游戏副本
    }

    /**
     * 排行榜类型
     *
     * @author tyler
     */
    public interface RankType {
        int type_1 = 1;// 战斗力
        int type_2 = 2;// 等级
        int type_3 = 3;// 军工
        int type_4 = 4;// 军阶
        int type_5 = 5;// 副本
        int type_6 = 6;// 混合排行规则
        int type_7 = 7;// 特工等级排行
        int type_8 = 8;// 荣耀演练场 教官副本排行
    }

    /**
     * 玩家奖励类型
     *
     * @author tyler
     */
    public interface AwardType {
        int TYPE_1 = 1;// 重建家园
    }

    /**
     * 玩家记录
     */
    public interface TypeInfo {
        int TYPE_1 = 1;// 官员招募记录
        // 叛军掉落保底次数
        int REBEL_DROP_GUARANTEED_TIMES = 2;
    }

    public interface ArmyType {
        int INFANTRY_ARMY_TYPE = 1;//步兵
        int CAVALRY_ARMY_TYPE = 2;//骑兵
        int ARCHER_ARMY_TYPE = 3;//弓兵
    }

    /**
     * 万分比除数
     */
    public static final double TEN_THROUSAND = 10000.0;

    /**
     * 百分比除数
     */
    public static final double HUNDRED = 100.0;

    /**
     * 整数一百
     */
    public static final int INT_HUNDRED = 100;

    /**
     * 一小时的秒数
     */
    public static final int HOUR = 60 * 60;

    /**
     * 一分钟的秒数
     */
    public static final int MINUTE = 60;

    /**
     * 玩家游戏资源变更操作行为：获得
     */
    public static final int ACTION_ADD = 1;
    /**
     * 玩家游戏资源变更操作行为：减少、失去
     */
    public static final int ACTION_SUB = 0;

    /**
     * 自有平台号
     */
    public static final int SELF_PLAT_NO = 1;

    /**
     * 新手引导条件类型：任务条件
     */
    public static final int GUIDE_CON_TYPE_TASK = 1;

    /**
     * 宝石副本的容量
     */
    public final static int STONE_COMBAT_CNT_CAPACITY = 10;

    /**
     * 宝石副本的免费道具
     */
    public static final int STONE_FREE_PROP_ID = 26007;
    /**
     * 玩家等级45
     */
    public final static int ROLE_GRADE_45 = 45;
    /**
     * 剧情任务的id, 对应s_section_task的id
     */
    public static final int SECTION_ID_1 = 1;
    public static final int SECTION_ID_9 = 9;
    public static final int SECTION_ID_11 = 11;
}
