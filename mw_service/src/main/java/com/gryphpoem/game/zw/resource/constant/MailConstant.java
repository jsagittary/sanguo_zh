package com.gryphpoem.game.zw.resource.constant;

import com.gryphpoem.game.zw.resource.util.SystemTabLoader;

import java.util.List;

/**
 * @author TanDonghai
 * @ClassName MailConstant.java
 * @Description 邮件相关常量类
 * @date 创建时间：2017年4月4日 下午6:17:57
 */
public class MailConstant {

    /**
     * 邮件标题长度上限
     */
    public static int MAIL_TITLE_LEN;

    /**
     * 邮件内容长度上限
     */
    public static int MAIL_CONTENT_LEN;

    /**
     * 邮件保存时间，单位：秒
     */
    public static int MAIL_MAX_TIME;

    /**
     * 阵营邮件发送最低等级
     */
    public static int SEND_CAMP_MAIL_LEVEL;

    /**
     * 阵营邮件发送间隔
     */
    public static int CAMP_MAIL_CD;

    /**
     * 阵营邮件发送所需金币
     */
    public static List<Integer> CAMP_MAIL_GOLD;

    /**
     * 阵营邮件发送所需VIP等级限制
     */
    public static int CAMP_MAIL_VIP;

    /**
     * 阵营邮件发送免费的官职
     */
    public static List<Integer> CAMP_MAIL_FREE_JOB;

    /**
     * 阵营邮件发送内容最大长度
     */
    public static int CAMP_MAIL_CONTENT_MAX;

    /**
     * 阵营邮件手机端推送最低等级和离线时长
     */
    public static List<Integer> CAMP_MAIL_MIN_GRADE_AND_OFFLINE_LONG;

    public static void loadSystem() {
        MAIL_TITLE_LEN = SystemTabLoader.getIntegerSystemValue(SystemId.MAIL_TITLE_LEN, 30);
        MAIL_CONTENT_LEN = SystemTabLoader.getIntegerSystemValue(SystemId.MAIL_CONTENT_LEN, 400);
        MAIL_MAX_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.MAIL_MAX_TIME, 7 * 24 * 3600);
        SEND_CAMP_MAIL_LEVEL = SystemTabLoader.getIntegerSystemValue(SystemId.SEND_CAMP_MAIL_LEVEL, 55);
        CAMP_MAIL_CD = SystemTabLoader.getIntegerSystemValue(SystemId.CAMP_MAIL_CD, 60);
        CAMP_MAIL_GOLD = SystemTabLoader.getListIntSystemValue(SystemId.CAMP_MAIL_GOLD, "[]");
        CAMP_MAIL_VIP = SystemTabLoader.getIntegerSystemValue(SystemId.CAMP_MAIL_VIP, 3);
        CAMP_MAIL_FREE_JOB = SystemTabLoader.getListIntSystemValue(SystemId.CAMP_MAIL_FREE_JOB, "[1,2,3]");
        CAMP_MAIL_CONTENT_MAX = SystemTabLoader.getIntegerSystemValue(SystemId.CAMP_MAIL_CONTENT_MAX, 100);
        CAMP_MAIL_MIN_GRADE_AND_OFFLINE_LONG = SystemTabLoader
                .getListIntSystemValue(SystemId.CAMP_MAIL_MIN_GRADE_AND_OFFLINE_LONG, "[45,2]");
    }

    /**
     * 邮件类型：打武装力量（流寇）
     */
    public static final int TYPE_ATK_FORCE = 1;
    /**
     * 邮件类型：采集报告
     */
    public static final int TYPE_COLLET = 2;
    /**
     * 邮件类型：系统邮件
     */
    public static final int TYPE_SYSTEM = 3;
    /**
     * 邮件类型：活动邮件
     */
    public static final int TYPE_ACTIVITY = 4;
    /**
     * 邮件类型：战斗报告
     */
    public static final int TYPE_REPORT = 5;
    /**
     * 邮件类型：玩家邮件
     */
    public static final int TYPE_ROLE = 6;

    /**
     * 邮件状态：未读
     */
    public static final int STATE_UNREAD = 1;
    /**
     * 邮件状态：已读
     */
    public static final int STATE_READ = 2;
    /**
     * 邮件状态：含附件未读
     */
    public static final int STATE_UNREAD_ITEM = 3;
    /**
     * 邮件状态：含附件已读
     */
    public static final int STATE_READ_ITEM = 4;
    /**
     * 邮件状态：附件已读已领取
     */
    public static final int STATE_NO_ITEM = 5;
    /**
     * 邮件状态：未锁定
     */
    public static final int STATE_NO_LOCK = 0;
    /**
     * 邮件状态：锁定
     */
    public static final int STARE_LOCK = 1;

    /************* 邮件战报状态 **************/
    /**
     * 过期邮件战报
     */
    public static final int EXPIRED_REPORT = 1;

    /**
     * 邮件存在战报
     */
    public static final int EXISTENCE_REPORT = 2;
    
    /************* 邮件模版id开始 **************/
    /**
     * 攻击流寇成功战报
     */
    public static final int MOLD_ATK_BANDIT_SUCC = 1;
    /**
     * 攻击流寇失败战报
     */
    public static final int MOLD_ATK_BANDIT_FAIL = 2;
    /**
     * 进攻目标丢失
     */
    public static final int MOLD_ATK_TARGET_NOT_FOUND = 3;
    /**
     * 采集报告
     */
    public static final int MOLD_COLLECT = 4;
    /**
     * 采集目标丢失
     */
    public static final int MOLD_COLLECT_NO_TARGET = 5;
    /**
     * 占领采集点失败
     */
    public static final int MOLD_COLLECT_ATK_FAIL = 6;
    /**
     * 占领采集点成功
     */
    public static final int MOLD_COLLECT_ATK_SUCC = 7;
    /**
     * 采集点防守成功
     */
    public static final int MOLD_COLLECT_DEF_SUCC = 8;
    /**
     * 采集点防守失败 战报邮件
     */
    public static final int MOLD_COLLECT_DEF_FAIL = 9;
    /**
     * 采集点防守失败 采集邮件
     */
    public static final int MOLD_COLLECT_DEF_FAIL_COLLECT = 526;
    /**
     * 采集中途撤回
     */
    public static final int MOLD_COLLECT_RETREAT = 10;
    /**
     * 行军报告，城战被攻击玩家迁城，通知攻击方玩家
     */
    public static final int MOLD_CITY_DEF_FLEE_ATK = 11;
    /**
     * 行军报告，城战被攻击玩家迁城，通知去增援的盟友
     */
    public static final int MOLD_CITY_DEF_FLEE_DEF = 12;
    /**
     * 行军报告，发起城战玩家撤回部队，通知防守方玩家
     */
    public static final int MOLD_CITY_ATK_RETREAT_DEF = 13;
    /**
     * 行军报告，发起城战玩家撤回部队，通知攻击方的增援玩家
     */
    public static final int MOLD_CITY_ATK_RETREAT_ATK = 14;
    /**
     * 敌方侦查成功
     */
    public static final int MOLD_ENEMY_SCOUT_SUCC = 15;
    /**
     * 侦查成功
     */
    public static final int MOLD_SCOUT_SUCC = 16;
    /**
     * 侦查失败
     */
    public static final int MOLD_SCOUT_FAIL = 17;
    /**
     * 城战防守失败
     */
    public static final int MOLD_DEF_CITY_FAIL = 18;
    /**
     * 城战防守成功
     */
    public static final int MOLD_DEF_CITY_SUCC = 19;
    /**
     * 城战进攻失败
     */
    public static final int MOLD_ATK_CITY_FAIL = 20;
    /**
     * 城战进攻成功
     */
    public static final int MOLD_ATK_CITY_SUCC = 21;
    /**
     * 军团战（国战、阵营战）防守失败
     */
    public static final int MOLD_DEF_CAMP_FAIL = 22;
    /**
     * 军团战（国战、阵营战）防守成功
     */
    public static final int MOLD_DEF_CAMP_SUCC = 23;
    /**
     * 军团战（国战、阵营战）进攻失败
     */
    public static final int MOLD_ATK_CAMP_FAIL = 24;
    /**
     * 军团战（国战、阵营战）进攻成功
     */
    public static final int MOLD_ATK_CAMP_SUCC = 25;
    /**
     * 城池竞选成功
     */
    public static final int MOLD_CAMPAIGN_SUCC = 26;
    /**
     * 城池竞选失败
     */
    public static final int MOLD_CAMPAIGN_FAIL = 27;
    /**
     * 城池城主额外奖励
     */
    public static final int MOLD_CITY_EXTRA_REWARD = 28;
    /**
     * 城主任期结束
     */
    public static final int MOLD_CITY_OWNER_END = 29;
    /**
     * 后台发补偿的邮件
     */
    public static final int MOLD_GM_COMPENSATE = 30;
    /**
     * 后台发福利邮件
     */
    public static final int MOLD_GM_WALFARE = 31;
    /**
     * 版本更新公告
     */
    public static final int MOLD_GM_UPDATE = 32;
    /**
     * 纯自定义邮件
     */
    public static final int MOLD_GM_CUSTOM = 33;
    /**
     * 充值到账
     */
    public static final int MOLD_PAY_DONE = 34;
    /**
     * 月卡每日奖励邮件
     */
    public static final int MOLD_MONTH_CARD_REWARD = 35;
    /**
     * 拜师奖励邮件
     */
    public static final int MOLD_ADD_MASTER_REWARD = 36;
    /**
     * 收徒成功
     */
    public static final int MOLD_ADD_APPRENTICE = 37;
    /**
     * 敌方侦查失败
     */
    public static final int MOLD_ENEMY_SCOUT_FAIL = 38;
    /**
     * 驻防将领返回
     */
    public static final int MOLD_GARRISON_RETREAT = 39;
    /**
     * 驻防将领被遣返
     */
    public static final int MOLD_GARRISON_REPATRIATE = 40;
    /**
     * 基地被击飞
     */
    public static final int MOLD_HIT_FLY = 41;
    /**
     * 攻击点兵统领成功战报
     */
    public static final int MOLD_ATK_LEAD_SUCC = 42;
    /**
     * 攻击点兵统领失败战报
     */
    public static final int MOLD_ATK_LEAD_FAIL = 43;
    /**
     * 驻防将领8小时返回
     */
    public static final int MOLD_GARRISON_FILL_TIME_RETREAT = 44;
    /**
     * 选举公告
     */
    public static final int MOLD_PARTY_JOB_OPEN = 45;
    /**
     * 升级奖励
     */
    public static final int MOLD_PARTY_LV_REWARD = 46;
    /**
     * 邮件过期
     */
    public static final int MOLD_MAIL_EXPIRED = 47;
    /**
     * 欢迎邮件
     */
    public static final int MOLD_MAIL_WELLCOME = 48;
    /**
     * 欢迎邮件
     */
    public static final int MOLD_MAIL_WELLCOME2 = 49;
    /**
     * 雇佣建造队金币返还
     */
    public static final int BUY_BUILD_1 = 50;
    /**
     * 雇佣建造队金币返还
     */
    public static final int BUY_BUILD_2 = 51;
    /**
     * 驻防将领被击杀
     */
    public static final int WALL_HELP_KILLED = 52;
    /**
     * 驻防位置已满
     */
    public static final int WALL_HELP_FILL = 53;
    /**
     * 对方开启保护罩返回
     */
    public static final int MOLD_ATTACK_TARGET_HAS_PROTECT = 54;
    /**
     * 已被其他指挥官击败
     */
    public static final int MOLD_ATTACK_TARGET_FLY = 55;
    /**
     * 推荐阵营奖励
     */
    public static final int MOLD_RECOMMEND_CAMP_REWARD = 56;
    /**
     * 每日登陆奖励
     */
    public static final int MOLD_LOGIN_REWARD = 57;
    /**
     * 充值双倍金币
     */
    public static final int MOLD_FIRST_DOUBLE_PAY_DONE = 58;
    /**
     * 攻打盖世太保成功
     */
    public static final int MOLD_ATK_GESTAPO_SUCC = 59;
    /**
     * 攻打盖世太保失败
     */
    public static final int MOLD_ATK_GESTAPO_FAIL = 60;
    /**
     * 攻打盖世太保目标丢失
     */
    public static final int MOLD_ATK_GESTAPO_NOT_FOUND = 61;
    /**
     * 盖世太保活动结束道具回收
     */
    public static final int MOLD_ACT_PROP_RECYCLE = 62;
    /**
     * 城市首杀奖励
     */
    public static final int MOLD_FIRST_KILL_REWARD = 63;
    /**
     * 闪电战进攻成功
     */
    public static final int MOLD_ATK_LIGHTNING_WAR_BOSS = 64;
    /**
     * 闪电战boss击杀
     */
    public static final int MOLD_ATK_LIGHTNING_WAR_BOSS_SUCC = 65;
    /**
     * 盖世太保排行榜个人奖励
     */
    public static final int MOLD_GESTAPO_KILL_REWARD = 66;
    /**
     * 盖世太保排行榜阵营奖励
     */
    public static final int MOLD_GESTAPO_KILL_CAMP_REWARD = 67;
    /**
     * 超级采集点已满
     */
    public static final int MOLD_SUPER_MINE_COLLECT_FILL = 68;
    /**
     * 采集报告
     */
    public static final int MOLD_SUPER_MINE_COLLECT = 69;
    /**
     * 攻打超级资源点失败
     */
    public static final int MOLD_SUPER_ATK_FAIL = 70;
    /**
     * 攻打超级资源点成功
     */
    public static final int MOLD_SUPER_ATK_SUCCESS = 71;
    /**
     * 超级资源点防守成功
     */
    public static final int MOLD_SUPER_DEF_SUCCESS = 72;
    /**
     * 超级资源点防守失败
     */
    public static final int MOLD_SUPER_DEF_FAIL = 73;
    /**
     * 超级资源点采集中途撤回
     */
    public static final int MOLD_SUPER_MINE_COLLECT_MIDWAY_RETURN = 74;
    /**
     * 驻防将领返回
     */
    public static final int MOLD_SUPER_MINE_HELP_RETURN = 75;
    /**
     * 驻防将领被击杀
     */
    public static final int MOLD_SUPER_MINE_HELP_KILL = 76;
    /**
     * 驻防位置已满
     */
    public static final int MOLD_SUPER_MINE_HELP_FILL = 77;
    /**
     * 攻打超级资源点失败 %s[%s,%s的的]敌人落荒而逃，我军部队已自动返回
     */
    public static final int MOLD_SUPER_MINE_ATK_RUN_AWAY = 78;
    /**
     * 阵营成功占领柏林
     */
    public static final int MOLD_BERLIN_WAR_REWARD = 79; // 本次柏林会战由【帝国】阵营成功占领柏林，指挥官本次会战获得【19000】积分
    /**
     * 阵营邮件
     */
    public static final int MOLD_CAMP_MAIL = 80;
    /**
     * 大型资源点停产
     */
    public static final int MOLD_SUPER_MINE_STOP = 81;
    /**
     * 下次柏林会战预告
     */
    public static final int MOLD_NEXT_BERLIN_PREVIEW = 82;
    /**
     * 柏林会战优胜奖
     */
    public static final int MOLD_BERLIN_SUC_CAMP = 83;
    /**
     * 柏林会战参与奖
     */
    public static final int MOLD_BERLIN_FAIL_CAMP = 84;
    /**
     * 更新说明
     */
    public static final int MOLD_GM_UPDATE_EXPLAIN = 85;
    /**
     * 柏林活动调整补偿
     */
    public static final int MOLD_BERLIN_COMPENSATION_AWARD = 86;
    /**
     * 匪军叛乱防守失败 给参加者发
     */
    public static final int MOLD_REBEL_DEF_FAIL_JOIN = 87;
    /**
     * 匪军叛乱防守成功 给参加者发
     */
    public static final int MOLD_REBEL_DEF_SUCC_JOIN = 88;
    /**
     * 匪军叛乱全通过
     */
    public static final int MOLD_REBEL_ALL_PASS = 89;
    /**
     * 匪军叛乱防守失败 给帮助者发
     */
    public static final int MOLD_REBEL_DEF_FAIL_HELP = 90;
    /**
     * 匪军叛乱防守成功 给帮助者发
     */
    public static final int MOLD_REBEL_DEF_SUCC_HELP = 91;
    /**
     * 反攻德意志BOSS防守 给参加者发
     */
    public static final int MOLD_COUNTER_BOSS_DEF_JOIN = 92;
    /**
     * 反攻德意志BOSS进攻 玩家防守成功
     */
    public static final int MOLD_COUNTER_BOSS_ATK_FAIL = 93;
    /**
     * 反攻德意志BOSS进攻 玩家防守失败
     */
    public static final int MOLD_COUNTER_BOSS_ATK_SUCC = 94;
    /**
     * 反攻德意志BOSS防守 击杀当前BOSS
     */
    public static final int MOLD_COUNTER_BOSS_DEF_DEAD = 95;
    /**
     * 反攻德意志BOSS防守 击杀最后BOSS
     */
    public static final int MOLD_COUNTER_BOSS_DEF_END = 96;
    /**
     * 反攻德意志BOSS 参与积分奖励
     */
    public static final int MOLD_COUNTER_BOSS_JOIN_BATTLE = 97;
    /**
     * 反攻德意志BOSS 胜利奖励
     */
    public static final int MOLD_COUNTER_BOSS_JOIN_AWARD = 98;
    /**
     * 功能特权卡 金币部分
     */
    public static final int MOLD_FUN_CARD_GOLD = 99;
    /**
     * 功能特权卡 每日奖励部分
     */
    public static final int MOLD_FUN_CARD_AWARD = 100;
    /**
     * 世界霸主奖励决战指令邮件
     */
    public static final int DECISIVE_BATTLE_AWARD = 101;
    /**
     * 决战行军报告
     */
    public static final int DECISIVE_BATTLE_MARCH_REPORT = 102;
    /**
     * 决战进攻失败邮件模板
     */
    public static final int DECISIVE_BATTLE_ATK_FAIL = 103;
    /**
     * 决战进攻成功邮件模板
     */
    public static final int DECISIVE_BATTLE_ATK_SUCCESS = 104;
    /**
     * 决战防守失败邮件模板
     */
    public static final int DECISIVE_BATTLE_DEF_FAIL = 105;
    /**
     * 决战防守成功邮件模板
     */
    public static final int DECISIVE_BATTLE_DEF_SUCCESS = 106;
    /**
     * 决战进攻取消邮件模板
     */
    public static final int DECISIVE_BATTLE_ATK_CANCEL = 107;
    /**
     * 决战防御取消邮件模板
     */
    public static final int DECISIVE_BATTLE_DEF_CANCEL = 108;
    /**
     * 决战中玩家驻防被遣返邮件模板
     */
    public static final int DECISIVE_BATTLE_GARRISON_CANCEL = 109;
    /**
     * 获得飞艇归属权
     */
    public static final int MOLD_AIR_SHIP_GET_BELONG = 110;
    /**
     * 未获得飞艇归属权
     */
    public static final int MOLD_AIR_SHIP_NOT_GET_BELONG = 111;
    /**
     * 成功抢夺飞艇归属权
     */
    public static final int MOLD_AIRSHIP_FIGHT_BELONG = 112;
    /**
     * 攻打飞艇成功
     */
    public static final int MOLD_AIR_SHIP_BATTLE_SUC = 113;
    /**
     * 攻打飞艇失败
     */
    public static final int MOLD_AIR_SHIP_BATTLE_FAIL = 114;
    /**
     * 飞艇逃离
     */
    public static final int MOLD_AIRSHIP_RUN_AWAY = 115;
    /**
     * 飞艇被击飞
     */
    public static final int MOLD_AIR_SHIP_DEAD = 116;
    /**
     * 飞艇归属者奖励
     */
    public static final int MOLD_AIR_SHIP_BELONG_AWARD = 117;
    /**
     * 飞艇协助者奖励上限
     */
    public static final int MOLD_AIR_SHIP_HELP_AWARD_MAX = 118;
    /**
     * 飞艇归属者奖励上限
     */
    public static final int MOLD_AIR_SHIP_BELONG_AWARD_MAX = 119;
    /**
     * 飞艇归属权被抢夺
     */
    public static final int MOLD_AIR_SHIP_LOST_BELONG = 120;
    /**
     * 世界boss攻打成功
     */
    public static final int MOLD_ATK_SCHEDULE_BOSS_SUC = 121;
    /**
     * 世界boss攻打失败
     */
    public static final int MOLD_ATK_SCHEDULE_BOSS_FAIL = 122;

    /**
     * 世界进程排行奖励
     */
    public static final int MOLD_WORLD_SCHEDULE_RANK_AWARD = 123;
    /**
     * 新地图已经关闭
     */
    public static final int MOLD_WORLD_WAR_CLOSE = 124;
    /**
     * 新地图采集中断
     */
    public static final int MOLD_CROSSMAP_COLLECT_BREAK = 125;
    /**
     * 新地图城池竞选成功
     */
    public static final int MOLD_NEWMAP_CAMPAIGN_SUCC = 126;
    /**
     * 奖章兑换活动结束道具回收
     */
    public static final int MOLD_ACT_BANDIT_CONVERT_AWARD = 127;
    /**
     * 纽约争夺战优胜奖
     */
    public static final int MOLD_NEWYORK_WAR_JOIN_SUCCESS_AWARD = 128;
    /**
     * 纽约争夺战参与奖
     */
    public static final int MOLD_NEWYORK_WAR_JOIN_AWARD = 129;
    /**
     * 阵营杀敌排行奖励
     */
    public static final int MOLD_NEWYORK_WAR_CAMP_RANK_AWARD = 130;
    /**
     * 个人杀敌排行奖励
     */
    public static final int MOLD_NEWYORK_WAR_PERSONAL_RANK_AWARD = 131;
    /**
     * 争夺战活动进攻成功
     */
    public static final int MOLD_NEWYORK_WAR_ROUND_ATTACK_SUCCESS = 132;
    /**
     * 争夺战活动进攻失败
     */
    public static final int MOLD_NEWYORK_WAR_ROUND_ATTACK_FAIL = 133;
    /**
     * 争夺战活动防守成功
     */
    public static final int MOLD_NEWYORK_WAR_ROUND_DEFINE_SUCCESS = 134;
    /**
     * 争夺战活动防守失败
     */
    public static final int MOLD_NEWYORK_WAR_ROUND_DEFINE_FAIL = 135;
    /**
     * 跨服战阵营排行奖
     */
    public static final int MOLD_CROSS_WAR_CAMP_AWARD = 136;
    /**
     * 跨服战个人排行奖
     */
    public static final int MOLD_CROSS_WAR_PERSONAL_AWARD = 137;
    /**
     * 跨服战参与奖
     */
    public static final int MOLD_CROSS_WAR_JOIN_AWARD = 138;
    /**
     * 跨服战优胜奖
     */
    public static final int MOLD_CROSS_WAR_WIN_AWARD = 139;
    /**
     * 爱丽丝奖励邮件
     */
    public static final int MOLD_ALICE_AWARD = 140;

    /**
     * 进攻世界boss次数不足
     */
    public static final int MOLD_ATK_BOSS_CNT_EVERDAY = 141;

    /**
     * 进攻世界boss个人排行奖励
     */
    public static final int MOLD_ATK_BOSS_PERSON_RANK_AWARD = 142;

    /**
     * 进攻世界boss获取的奖励
     */
    public static final int MOLD_ATK_BOSS_AWARD = 143;

    /**
     * 背包已满的邮件
     */
    public static final int MOLD_OUT_OF_RANGE_AWARD = 144;

    /**
     * 称号已过期的邮件
     */
    public static final int TITLE_TIME_IS_EXPIRED = 145;

    /**
     * 礼包充值成功
     */
    public static final int MOLD_PAY_GIFT_SUC = 401;
    /**
     * 礼包充值失败
     */
    public static final int MOLD_PAY_GIFT_FAIL = 402;
    /**
     * 首充礼包
     */
    public static final int MOLD_FIRST_PAY_AWARD = 403;
    /**
     * 空降补给金币返还
     */
    public static final int MOLD_SUPPLY_DORP_RETURN = 404;
    /**
     * 空降补给金币和未领取的道具返还
     */
    public static final int MOLD_SUPPLY_DORP_ALL_RETURN = 405;
    /**
     * 未领奖的活动发放
     */
    public static final int MOLD_ACT_UNREWARDED_REWARD = 406;
    /**
     * 全军返利阵营优胜奖
     */
    public static final int MOLD_ACT_ALL_CHARGE_REWARD = 407;
    /**
     * 活动道具兑换
     */
    public static final int MOLD_ACT_EXCHANGE_REWARD = 408;
    /**
     * 阵营排行
     **/
    public static final int MOLD_ACT_CAMPRANK_REWARD = 409;
    /**
     * 世界争霸通用奖励
     */
    public static final int WORLD_WAR_COMMON_REWARD = 410;
    /**
     * 世界争霸个人积分排名奖励
     */
    public static final int WORLD_WAR_PERSONAL_RANK_REWARD = 411;
    /**
     * 世界争霸阵营积分排名奖励
     */
    public static final int WORLD_WAR_CAMP_RANK_REWARD = 412;
    /**
     * 战令未领取邮件奖励
     */
    public static final int MOLD_BATTLE_PASS_REWARD = 413;
    /**
     * 建筑礼包购买后发送的邮件
     */
    public static final int MOLD_BUILD_GIFT_REWARD = 414;

    /**
     * 勇冠三军阵营优胜奖
     */
    public static final int MOLD_ACT_BRAVEST_ARMY_AWARD = 415;

    /**
     * 玩家注册第4天邮件
     */
    public static final int MOLD_PLAYER_REGISTRATION_FOUR_AWARD = 416;

    /**
     * 玩家注册第7天邮件
     */
    public static final int MOLD_PLAYER_REGISTRATION_SEVEN_AWARD = 417;

    public static final int ACT_CHRISTMAS_CAMP_RANK_MAIL = 418;

    /**
     * 赛季结束返还
     */
    public static final int RESET_END_SEASON_TALENT = 419;

    /**
     * 阵营排行优胜奖
     */
    public static final int MOLD_ACT_WIND_CAMP_FIGHT_RANK_REWARD = 502;

    /**
     * 加入社群邮件奖励
     */
    public static final int MOLD_JOIN_COMMUNITY_REWARD = 503;

    /**
     * 阵营对拼活动的阵营排行奖励
     */
    public static final int MOLD_ROYAL_ARENA_CAMP_REWARD = 504;
    /**
     * 阵营对拼活动的个人排行奖励
     */
    public static final int MOLD_ROYAL_ARENA_PERSON_REWARD = 505;

    /**
     * 禁言通知
     */
    public static final int MOLD_SILENCE = 506;

    /**
     * 微信签到奖励
     */
    public static final int MOLD_WECHAT_SIGN_REWARD = 507;

    /**
     * 师徒关系解除
     */
    public static final int MOLD_DEL_MASTER_APPRENTICE = 508;

    /**
     * 战火燎原活动奖励  @Deprecated 废弃,请使用邮件ID 519
     */
    public static final int MOLD_ACT_WAR_FIRE_AWARD = 509;

    /**
     * 战火燎原积分不足 @Deprecated 废弃,请使用邮件ID 520
     */
    public static final int MOLD_ACT_WAR_FIRE_SCROE_NOT_ENOUGH = 510;

    /**
     * 战火燎原相同阵营炸矿
     */
    public static final int MOLD_WAR_FIRE_SAME_CAMP_COLLECT = 511;

    /**
     * 沙盘演武报名成功
     */
    public static final int MOLD_SAND_TABLE_CAMP_ENROLL = 512;
    /**
     * 演武获胜 演武失败 演武平局
     */
    public static final int MOLD_SAND_TABLE_ROUND_OVER_WIN = 513;
    public static final int MOLD_SAND_TABLE_ROUND_OVER_LOSE = 514;
    public static final int MOLD_SAND_TABLE_ROUND_OVER_DRAW = 515;
    /**
     * 演武阵营奖励
     */
    public static final int MOLD_SAND_TABLE_CAMP_RANK_REWARD = 516;
    /**
     * 演武个人奖励(您在沙盤演武中擊敗了#num1個領主)
     */
    public static final int MOLD_SAND_TABLE_PERSONAL_REWARD = 517;

    /**
     * 演武个人奖励(击败敌方一名领主)
     */
    public static final int MOLD_SAND_TABLE_PERSONAL_REWARD_TWO = 527;

    /**
     * 演武个人奖励(敌方无人应战)
     */
    public static final int MOLD_SAND_TABLE_PERSONAL_REWARD_THREE = 528;

    /**
     * 演武报名奖励
     */
    public static final int MOLD_SAND_TABLE_ENROLL_REWARD = 518;

    /**
     * 战火燎原活动奖励  废弃,请使用邮件ID
     */
    public static final int MOLD_ACT_WAR_FIRE_AWARD_NEW = 519;

    /**
     * 战火燎原积分不足
     */
    public static final int MOLD_ACT_WAR_FIRE_SCROE_NOT_ENOUGH_NEW = 520;

    public static final int MOLD_ACT_DIAOCHAN_RANK_AWARD = 521;


    public static final int MOLD_SEASON_522 = 522;//赛季旅程排行奖励
    public static final int MOLD_SEASON_523 = 523;//赛季旅程阵营排行奖励
    public static final int MOLD_SEASON_524 = 524;//赛季旅程未领取奖励
    public static final int MOLD_SEASON_525 = 525;//宏伟宝库未领取奖励


    /**
     * 圣坛互动成功
     */
    public static final int MOLD_VISIT_ALTAR_SUCCESS = 708;
    /**
     * 圣坛互动失败
     */
    public static final int MOLD_VISIT_ALTAR_FAIL = 709;


    /**
     * 皮肤返场活动皮肤发放成功
     */
    public static final int MOLD_ACT_SKIN_ENCORE_SUCCESS = 801;
    /**
     * 皮肤返场活动皮肤发放失败
     */
    public static final int MOLD_ACT_SKIN_ENCORE_FAIL = 802;

    /**
     * 秋季拍卖活动--竞拍被超过
     */
    public static final int BID_WAS_OVERTAKEN = 805;

    /**
     * 秋季拍卖活动--竞拍成功获得
     */
    public static final int SUCCESSFUL_BIDDING = 803;

    /**
     * 秋季拍卖活动--竞拍失败
     */
    public static final int BID_FAILED = 804;


    /**
     * 音乐节-阵营排名奖励
     * 尊敬的领主，您所在的%s阵营在%s活动中获得了第%s名，获得如下奖励，请查收。
     */
    public static final int MUSIC_CAMP_RANK_AWARD = 811;

    /**
     * 音乐节-个人排名奖励
     * 尊敬的领主，您在%s活动中获得了第%s名，获得如下奖励，请查收。
     */
    public static final int MUSIC_PLAYER_RANK_AWARD = 812;
    /**
     * 玛雅音乐节成功举办
     * 尊敬的领主，您创作了%s乐谱，为%s阵营音乐节做出了贡献，获得如下奖励，请查收。
     */
    public static final int MUSIC_CAMP_CONDUCT_SUCCESS = 813;

    /**
     * 尊敬的领主，你累计点亮了%s盏长明灯，获得如下奖励，请查收。
     */
    public static final int MOD_LONG_LIGHT = 814;

    /**
     * 神兵宝具礼包邮件
     */
    public static final int ACT_MAGIC_TREASURE_WARE_GIFT_BAG = 815;


    /*** 王朝遗迹 进攻成功*/
    public static final int MOLD_HIS_REMAINS_ATTACK_SUCCESS = 902;
    /*** 王朝遗迹 防守失败*/
    public static final int MOLD_HIS_REMAINS_DEFEND_FAILURE = 904;
    /*** 王朝遗迹 进攻失败*/
    public static final int MOLD_HIS_REMAINS_ATTACK_FAILURE = 901;
    /*** 王朝遗迹 防守成功*/
    public static final int MOLD_HIS_REMAINS_DEFEND_SUCCESS = 903;
    /*** 王朝遗迹 探索已结束*/
    public static final int MOLD_RELIC_PROBE_OVER = 905;
    /*** 王朝遗迹 王朝遗迹已消失*/
    public static final int MOLD_RELIC_PROBE_VANISH = 906;
    /*** 王朝遗迹 阵营排名奖励*/
    public static final int RELIC_OVER_CAMPRANK_AWARD = 907;
    /*** 王朝遗迹 积分奖励*/
    public static final int RELIC_OVER_SCORE_AWARD = 908;


    /**
     * 限时活动物品自动兑换
     */
    public static final int MOLD_ACT_TIME_LIMIT_EXCHANGE_AWARD = 1000;

    /**
     * 雄踞一方进攻失败
     */
    public static final int MOLD_DOMINATE_ATTACK_FAIL = 1101;

    /**
     * 雄踞一方进攻胜利
     */
    public static final int MOLD_DOMINATE_ATTACK_SUCCESS = 1102;

    /**
     * 雄踞一方防守胜利
     */
    public static final int MOLD_DOMINATE_ATTACK_DEFEND_SUCCESS = 1103;

    /**
     * 雄踞一方防守失败
     */
    public static final int MOLD_DOMINATE_ATTACK_DEFEND_FAIL = 1104;

    /**
     * 司隶雄踞一方活动奖励
     */
    public static final int MOLD_SI_LI_DOMINATE_AWARD = 1105;


    //========================跨服邮件模板===================================

    /**
     * 跨服充值活动总榜奖励
     */
    public static final int MOLD_ACT_CROSS_RECHARGE_TOTAL = 2001;
    /**
     * 跨服充值活动最佳日榜奖励
     */
    public static final int MOLD_ACT_CROSS_RECHARGE_DAILY = 2002;


    // 跨服采集报告
    public static final int CROSS_COLLECT_REPORT = 2104;

    // 采集目标丢失
    public static final int CROSS_COLLECT_NO_TARGET = 2105;

    // 采集点进攻失败
    public static final int CROSS_COLLECT_ATK_FAIL = 2106;

    // 采集点进攻成功
    public static final int CROSS_COLLECT_ATK_SUCCESS = 2107;

    // 采集点防守成功
    public static final int CROSS_COLLECT_DEF_SUCCESS = 2108;

    // 采集点防守失败
    public static final int CROSS_COLLECT_DEF_FAIL = 2109;

    // 采集中途撤回
    public static final int CROSS_COLLECT_RETREAT = 2110;

    // 防守方逃跑
    public static final int CROSS_PLAYER_DEF_FLEE_ATK = 2111;

    // 行军报告，城战被攻击玩家迁城，通知去增援的盟友
    public static final int MOLD_PLAYER_DEF_FLEE_DEF = 2112;

    // 行军报告，发起城战玩家撤回部队，通知防守方玩家
    public static final int MOLD_PLAYER_ATK_RETREAT_DEF = 2113;

    // 行军报告，发起城战玩家撤回部队，通知攻击方的增援玩家
    public static final int MOLD_PLAYER_ATK_RETREAT_ATK = 2114;

    // 敌方侦查成功
    public static final int CROSS_ENEMY_SCOUT_SUCCESS = 2115;

    // 侦查成功
    public static final int CROSS_SCOUT_SUCCESS = 2116;

    // 侦查失败
    public static final int CROSS_SCOUT_FAIL = 2117;

    // 敌方侦查失败
    public static final int CROSS_ENEMY_SCOUT_FAIL = 2138;

    // 城战防守失败
    public static final int CROSS_DEF_PLAYER_FAIL = 2118;

    // 城战防守成功
    public static final int CROSS_DEF_PLAYER_SUCCESS = 2119;

    // 进攻玩家失败
    public static final int CROSS_ATK_PLAYER_FAIL = 2120;

    // 进攻玩家成功
    public static final int CROSS_ATK_PLAYER_SUCCESS = 2121;

    // 被击飞
    public static final int CROSS_HIT_FLY = 2141;

    // 已被其他指挥官击败
    public static final int CROSS_MOLD_ATTACK_TARGET_FLY = 2155;

    // 相同阵营炸矿
    public static final int CROSS_WAR_FIRE_SAME_CAMP_COLLECT = 2197;

    // 战火玩法奖励
    public static final int CROSS_WAR_FIRE_AWARD = 2198;

    // 战火玩法积分不足
    public static final int CROSS_WAR_FIRE_SCORE_NOT_ENOUGH = 2199;

    // 叛军入侵防守失败
    public static final int DEFEND_REBEL_INVADE_FAIL = 3001;

    // 叛军入侵防守成功
    public static final int DEFEND_REBEL_INVADE_SUCCESS = 3002;


    /**
     * 不计入消耗功能数组
     */
    public static final int[] CROSS_MAIL_TEMPLATE_ID = new int[]{CROSS_WAR_FIRE_AWARD};
}
