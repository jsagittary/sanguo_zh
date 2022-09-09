package com.gryphpoem.game.zw.resource.constant;

import com.gryphpoem.game.zw.resource.util.SystemTabLoader;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @ClassName ChatConst.java
 * @Description 聊天相关常量配置
 * @author TanDonghai
 * @date 创建时间：2017年4月7日 上午9:56:14
 *
 */
public class ChatConst {

    /** 聊天禁言检测最小字符数 */
    public static int CHECK_CHAT_MIN_CNT;
    /** 聊天禁言相似比率 单位:万分比 */
    public static float CHECK_CHAT_RATE;
    /** 聊天禁言: [比较条数, 相似达到条数] */
    public static List<Integer> CHECK_CHAT_COMPARE_CNT;
    /** 聊天禁言时间 单位:分钟 */
    public static int CHAT_SILENCE_TIME;
    /** 聊天禁言VIP限制: 等级 */
    public static int CHECK_SILENCE_VIP;

    /** 聊天信息显示条数 */
    public static int MAX_CHAT_COUNT;
    /** 聊天信息长度限制 */
    public static int MAX_CHAT_LEN;
    /** 大喇叭的字符长度限制 */
    public static int WORLD_CHAT_MAX_LENGTH;

    /** 聊天保存的付费活跃消息条数 */
    public static int SYSTEM_NOTICE_NUM;

    /** 分享坐标的频率判断 */
    public static int SHARE_CHAT_CD;

    /** 重启或停服后保留的聊天消息ChatId */
    public static List<Integer> SAVE_CHATID_LIST;
    
    public static List<List<Integer>> CHAT_SHARE_CNT;


    /**
     * s_system表中定义的常量初始化
     */
    public static void loadSystem() {
        MAX_CHAT_COUNT = SystemTabLoader.getIntegerSystemValue(SystemId.MAX_CHAT_COUNT, 15);
        MAX_CHAT_LEN = SystemTabLoader.getIntegerSystemValue(SystemId.MAX_CHAT_LEN, 100);
        WORLD_CHAT_MAX_LENGTH = SystemTabLoader.getIntegerSystemValue(SystemId.WORLD_CHAT_MAX_LENGTH, 100);
        CHECK_CHAT_MIN_CNT = SystemTabLoader.getIntegerSystemValue(SystemId.CHECK_CHAT_MIN_CNT, 16);
        CHECK_CHAT_RATE = (float) (SystemTabLoader.getIntegerSystemValue(SystemId.CHECK_CHAT_RATE, 7000)
                / Constant.TEN_THROUSAND);
        CHECK_CHAT_COMPARE_CNT = SystemTabLoader.getListIntSystemValue(SystemId.CHECK_CHAT_COMPARE_CNT, "[]");
        CHAT_SILENCE_TIME = SystemTabLoader.getIntegerSystemValue(SystemId.CHAT_SILENCE_TIME, 30);
        CHECK_SILENCE_VIP = SystemTabLoader.getIntegerSystemValue(SystemId.CHECK_SILENCE_VIP, 3);
        SYSTEM_NOTICE_NUM = SystemTabLoader.getIntegerSystemValue(SystemId.SYSTEM_NOTICE_NUM, 15);
        SHARE_CHAT_CD = SystemTabLoader.getIntegerSystemValue(SystemId.SHARE_CHAT_CD, 10);
        SAVE_CHATID_LIST = SystemTabLoader.getListIntSystemValue(SystemId.SAVE_CHATID_LIST, "[]");
        CHAT_SHARE_CNT = SystemTabLoader.getListListIntSystemValue(SystemId.CHAT_SHARE_CNT,"[[]]");

    }

    /** 聊天频道：世界 */
    public static final int CHANNEL_WORLD = 1;
    /** 聊天频道：GM */
    public static final int CHANNEL_GM = 2;
    /** 聊天频道：阵营、国家 */
    public static final int CHANNEL_CAMP = 3;
    /** 聊天频道：私聊 */
    public static final int CHANNEL_PRIVATE = 4;
    /** 聊天频道：区域 */
    public static final int CHANNEL_AREA = 5;
    /** 跨服频道聊天 */
    public static final int CHANNEL_CROSS = 6;
    /** 阵营邮件私聊 */
    public static final int IS_CAMP_MAIL_CHAT = 1;
    /** 阵营邮件私聊会话 */
    public static final int IS_CAMP_MAIL_CHAT_DIALOG = 1;

    /** 日志记录: 世界 */
    public static final String WORLD_LOG = "worldChat";
    /** 日志记录: 阵营 */
    public static final String CAMP_LOG = "campChat";
    /** 日志记录: 阵营 */
    public static final String PRIVATE_LOG = "privateChat";
    /** 日志记录: 区域 */
    public static final String AREA_LOG = "areaChat";

    /********** 下面为聊天和公告模版id ************/
    /** 后台公告 */
    public static final int CHAT_GM_NOTICE = 0;
    /** 玩家被攻击，求援 */
    public static final int CHAT_DEF_SEEK_HELP = 1;// 我的基地[%s,%s]被%s的%s发起了进攻，请求支援！
    /** 玩家侦查信息 */
    public static final int CHAT_ROLE_SCOUT = 2;// %s 侦查 %s[点击查看]
    /** 玩家攻击其他人，求援 */
    public static final int CHAT_ATK_SEEK_HELP = 3;// 我对%s的%s[%s,%s]发起了战斗，请求支援！
    /** 分享战报 */
    public static final int CHAT_SHARE_REPORT = 4;// %s VS %s[点击播放]
    /** 城防损兵通知 */
    public static final int CHAT_CITY_DEMAGED = 5;// 我方区域%s受到敌方玩家攻击，需要修复[点击前往]
    /** 城池占领通知 */
    public static final int CHAT_CITY_OCCUPIED = 6;// %s的%s[%s,%s]被%s的%s占领
    /** 城池被进攻通知 */
    public static final int CHAT_CITY_DEF = 7;// %s的%s对我方的%s发起了战斗。敌军来犯，我城势单力薄，还请诸位同袍伸出援手。[点击参加军团战]
    /** 国王上线通知 */
    public static final int CHAT_KING_LOGIN = 8;// 国王%s上线啦！
    /** 丞相上线通知 */
    public static final int CHAT_PRIME_LOGIN = 9;// 丞相%s上线啦！
    /** 军师上线通知 */
    public static final int CHAT_ADVISER_LOGIN = 10;// 军师%s上线啦！
    /** 打造装备通知 */
    public static final int CHAT_FORGE_EQUIP = 11;// 恭喜%s成为全服第%s位打造出%s的指挥官！
    /** 招募将领通知 */
    public static final int CHAT_RECRUIT_HERO = 12;// 恭喜%s招募到了神将%s，突破后将变得十分强力！
    /** 任务额外奖励通知 */
    public static final int CHAT_EXTRA_TASK_REWARD = 13;// 鸿运当头，恭喜玩家%s完成军团任务时获得了额外的军功奖励！
    /** 将领突破通知 */
    public static final int CHAT_HERO_BREAK = 14;// [["%s",0],["通过突破获得了神将",0],["%s",0]]
    /** 本阵营进攻城池通知 */
    public static final int CHAT_CITY_ATK = 15;// [["我方的",0],["%s",0],["对",0],["%s",0],["%s",0],["%s",0],["发起了军团战，请各位指挥官加入战斗。",0],["点击参加军团战",1]]
    /** 阵营战,城战 */
    public static final int CHAT_CITY_REPORT = 18;// [["%s",0],[" VS ",0],["%s",0],["%s",0],["[点击查看]",1]]
    /** 第一个Boss死亡后通知 */
    public static final int CHAT_WORLD_BOSS_1 = 19;
    /** 第二个Boss死亡后通知 */
    public static final int CHAT_WORLD_BOSS_2 = 20;
    /** 国王修改公告同步通知 */
    public static final int CHAT_KING_MODIFY_SLOGAN = 21;// [["总司令",0],["%s",0],["修改了阵营公告，请各位指挥官前往查看。",0]]
    /** 当选为总司令 */
    public static final int CHAT_KING_ELECTED = 22;// [["%s",0],["众望所归，当选为总司令",0]]
    /** 当选为政委 */
    public static final int CHAT_PRIME_ELECTED = 23;// [["%s",0],["众望所归，当选为政委",0]]
    /** 当选为参谋长 */
    public static final int CHAT_ADVISER_ELECTED = 24;// [["%s",0],["众望所归，当选为参谋长",0]]
    /** 满级 */
    public static final int CHAT_FILL_LEVEL = 25;// [["经过不懈努力，",0],["%s",0],["终于满级了！",0]]
    /** 发起召唤后的通知 */
    public static final int CHAT_SUMMON_TEAM = 26;
    /** 城池征收 */
    public static final int CHAT_CITY_LEVY = 27;
    /** 城池征收 双倍 */
    public static final int CHAT_CITY_LEVY_DOUBLE = 28;
    /** 被都城卫戍队攻击通知 */
    public static final int CHAT_HOME_CITY_DEF = 29;// [["%s",0],["的都城卫戍队",0],["对我方的",0],["%s",0],["%s",0],["发起了阵营战，请各位指挥官参与协防。",0],["[点击参加阵营战]",1]]
    /** 城卫戍队攻击其他阵营通知 */
    public static final int CHAT_HOME_CITY_ACT = 30;// [["我方的都城卫戍队对",0],["%s",0],["%s",0],["%s",0],["发起了阵营战，请各位指挥官加入战斗。",0],["点击参加阵营战",1]]
    /** 城池被NPC占领 */
    public static final int CHAT_CITY_NPC_OCCUPIED = 31;
    /** 分享NPC攻打城池战报 */
    public static final int CHAT_SHARE_NPC_REPORT = 32;
    /** 所有指挥官可前往阵营领取军功奖励 */
    public static final int CHAT_PARTY_HONOR_REWARD_HIT = 33;// [["%s",0],["指挥官齐心协力，达成每日",0],["%s",0],["荣誉目标，所有指挥官可前往阵营领取军功奖励。",0]]
    /** 匪军战斗分享 */
    public static final int CHAT_BANDIT_REPORT = 34;// [["%s",0],[" VS ",0],["Lv."],["%s",0],["%s",0],["[点击查看]",1]]
    /** 自己阵营的城池被干，求援 */
    public static final int CHAT_CAMP_DEF_SEEK_HELP = 35;
    /** 攻击他方阵营的城池，求援 */
    public static final int CHAT_CAMP_ATK_SEEK_HELP = 36;
    /** 攻击盖世太保，求援 */
    public static final int CHAT_GESTAPO_ATK_SEEK_HELP = 37;// [["我对",0],["%s",0],["[%s,%s]",1],["发起了战斗，请求支援！",0]]
    /** 攻击盖世太保 */
    public static final int CHAT_GESTAPO_ATK = 38;// [["%s",0],[" VS ",0],["%s",0],["[点击查看]",1]]
    /** 城市首杀 */
    public static final int CHAT_CITY_FIRST_KILL = 39;// [["恭喜",0],["%s",0],["阵营的",0],["%s",0],["带领的队伍成功攻克",0],["%s",0],["，获得了",0],["%s",0],["区域的",0],["%s",0],["首杀，参与的队伍成员可获得丰厚的首杀奖励!",0]]
    /** 闪电战活动开启推送 */
    public static final int CHAT_LIGHTNING_WAR_BEGIN = 40;// [["警报！德意志大军前来集结，请各阵营的全体指挥官派兵前往参战！",0]]
    /** 闪电战战斗几轮推送 */
    public static final int CHAT_ATK_CHAT = 41;// [["第",0],["%s",0],["轮闪电战已经打响，请各位指挥官加入战斗。",0],["[点击参加闪电战]",1]]
    /** BOSS被击杀，活动结束推送 */
    public static final int CHAT_BOSS_DEAD = 42;// [["恭喜各位指挥官，",0],["%s",0],["区域的德意志大军被击杀，敌军势力受到重创，请各位继续努力，早日赢得大战胜利！",0]]
    /** BOSS未被击杀，活动结束推送 */
    public static final int CHAT_BOSS_NOT_DEAD = 43;// [["各位指挥官，",0],["%s",0],["区域的德意志大军被击退，敌军势力受到重创，请各位继续努力，早日赢得大战胜利！",0]]
    /** 解锁女武神 [["恭喜",0],["%s",0],["领主",0],["%s",0],["购买希露德礼包，成功解锁佳人-女武神希露德",0],["%s",1]] */
    public static final int CHAT_CIA_UNLOCK_AGENT_3 = 47;
    /** 解锁貂蝉 [["恭喜",0],["%s",0],["领主",0],["%s",0],["贵族等级达到4级，成功解锁佳人-貂蝉",0],["%s",1]] */
    public static final int CHAT_CIA_UNLOCK_AGENT_2 = 48;
    /** 闪电战活动预告推送 */
    public static final int CHAT_LIGHTNING_WAR_ANN = 49;// [["还有",0],[" %s ",0],["分钟开启闪电战，请各位指挥官做好准备！",0]
    /** 超级矿点被攻击,请求支援 */
    public static final int CHAT_SUPER_MINE_DEF_HELP = 50;// [["我方阵营的",0],["%s",0],["[%s,%s]",1],["被",0],["%s",0],["的",0],["%s",0],["发起了进攻，请求支援！",0]]
    /** 柏林会战开启推送 */
    public static final int CHAT_BERLIN_OPEN = 51;// [["本次柏林会战已开启，三军可前往",0],["%s",0],["参与争夺，成功占领柏林的阵营指挥官将就任世界霸主",0]]
    /** 柏林会战关闭推送 */
    public static final int CHAT_BERLIN_CLOSE = 52;// [["本次柏林会战已结束，恭喜",0],["%s",0],["阵营成功占领柏林，帝国总司令",0],["%s",0],["就任世界霸主",0]]
    /** 柏林会战:占领柏林推送 */
    public static final int CHAT_OCCUPY_BERLIN = 53;// [["%s",0],["阵营的",0],["%s",0],["率领部队成功攻陷柏林，占领满30分钟即可获得柏林占领权",0]]
    /** 柏林会战:占领阵地推送 */
    public static final int CHAT_OCCUPY_BATTLEFRONT = 54;// [["%s",0],["阵营的",0],["%s",0],["率领部队成功攻陷",0],["%s",0],["坐标",0],["[%s,%s]",1],[",占领柏林，指日可待",0]]
    /** 柏林会战:阵地炮击推送 */
    public static final int CHAT_BATTLEFRONT_ATK = 55;// [["%s",0],["%s",0],["装弹完成，对柏林发动攻击，造成",0],["%s",0],["点伤害",0]]

    /** 首充并且领取喀秋莎 */
    public static final int CHAT_FIRST_FLUSH = 57;// [["恭喜【",0],["%s",0],["】指挥官【",0],["%s",0],["】完成首冲，招募紫色战车将领",0],["%s",1]]
    /** 购买VIP6礼包-招募紫色坦克将领崔可夫 */
    public static final int CHAT_BUY_VIP3 = 58;// [["恭喜【",0],["%s",0],["】指挥官【",0],["%s",0],["】购买Vip6礼包，招募紫色坦克将领",0],["%s",1]]
    /** 购买VIP8礼包-招募橙色火箭将领麦克阿瑟 */
    public static final int CHAT_BUY_VIP8 = 59;// [["恭喜【",0],["%s",0],["】指挥官【",0],["%s",0],["】购买Vip8礼包，招募橙色火箭将领",0],["%s",1]]
    // /** 研究科技-初级指挥学 */
    // public static final int CHAT_SCIENTIFIC_RESEARCH_CJZHX = 60;//
    // [["恭喜【",0],["%s",0],["】指挥官【",0],["%s",0],["】成功研究科技",0],["初级指挥学",1],["，兵力提升为3排，战力大涨，",0],["我也要",1]]
    // /** 研究科技-初级统率力 */
    // public static final int CHAT_SCIENTIFIC_RESEARCH_CJTSL = 61;//
    // [["恭喜【",0],["%s",0],["】指挥官【",0],["%s",0],["】成功研究科技",0],["初级统率力",1],["，可上阵将领提升为3名，战力大涨，",0],["我也要",1]]
    // /** 研究科技-中级指挥学 */
    // public static final int CHAT_SCIENTIFIC_RESEARCH_ZJZHX = 62;//
    // [["恭喜【",0],["%s",0],["】指挥官【",0],["%s",0],["】成功研究科技",0],["中级指挥学",1],["，兵力提升为4排，战力大涨，",0],["我也要",1]]
    // /** 研究科技-中级统率力 */
    // public static final int CHAT_SCIENTIFIC_RESEARCH_ZJTSL = 63;//
    // [["恭喜【",0],["%s",0],["】指挥官【",0],["%s",0],["】成功研究科技",0],["中级统率力",1],["，可上阵将领提升为4名，战力大涨，",0],["我也要",1]]
    /** 打造装备-全服第【1-3】位成功打造紫色装备【AKM突击步枪】 */
    public static final int CHAT_MAKE_PURPLE_EQUIPMENT_AKM = 64;// [["恭喜【",0],["%s",0],["】指挥官【",0],["%s",0],["】全服第【1-3】位成功打造紫色装备【AKM突击步枪】，实力大增，",0],["我也要",1]]
    /** 打造装备-全服第【1-5】位成功打造橙色装备【AKM突击步枪】 */
    public static final int CHAT_MAKE_ORANGE_EQUIPMENT_AKM = 65;// [["恭喜【",0],["%s",0],["】指挥官【",0],["%s",0],["】全服第【1-5】位成功打造紫色装备【AKM突击步枪】，实力大增，",0],["我也要",1]]
    /** 等级提升-全服第【1-50】位等级提升至45级 */
    public static final int CHAT_GRADE_IPGRADING_45 = 66;// [["恭喜【",0],["%s",0],["】指挥官【",0],["%s",0],["】全服第【1-50】位等级提升至45级，45级参与阵营战，争夺最高荣誉，",0],["我也要升级",1]]
    /** 霸主任命官职 */
    public static final int CHAT_APPOINT_BERLIN_JOB = 67;
    /** 将领授勋 */
    public static final int CHAT_HERO_DECORATED = 68;
    /** 极品装备科技触发命中 */
    public static final int THE_BEST_EQUIP = 69;
    /** 装备 转盘 抽中特殊道具时，发送世界公告 */
    public static final int CHAT_EQUIP_TURNPLATE_GLOBAL_NUM = 70;
    /** 幸运 转盘 抽中特殊道具时，发送世界公告 */
    public static final int CHAT_LUCKY_TURNPLATE_GLOBAL_NUM = 71;

    /** 恭喜【阵营】指挥官【邱少云】购买Vip【7】礼包 */
    public static final int CHAT_VIP_BUY = 74;
    /** 名将 转盘 抽中特殊道具时，发送世界公告 */
    public static final int CHAT_FAMOUS_GENERAL_TURNPLATE_GLOBAL_NUM = 75;
    /** 恭喜指挥官【崔可夫】在战机研发室中获得【XX战机】，我也要参与 */
    public static final int CHAT_PLANE_SEARCH = 76;
    /** 我的基地[255,255]被第5波匪军发起了进攻，请求支援！ */
    public static final int CHAT_DEF_REBEL_HELP = 77;
    /** 分享坐标 */
    public static final int CHAT_SHARE_POS = 78;
    /** 名将 转盘 兑换将领时，发送世界公告 */
    public static final int CHAT_FAMOUS_GENERAL_EXCHANGE_GLOBAL_NUM = 79;

    /** 德意志指挥官埃尔文在巴黎（255，154）集结，请全体指挥官派兵前往参战，前往参与 */
    public static final int CHAT_COUNTER_ATK_BOSS_DEF = 80; // [["德意志指挥官埃尔文在",0],["%s",0],["%s",0],["%s",0],["集结，请全体指挥官派兵前往参战",0],["前往参与",1]]
    /** 德意志精锐部队，即将突袭我方基地，请全体指挥官派兵前往参与，前往参与 */
    public static final int CHAT_COUNTER_ATK_BOSS_ATK = 81; // [["德意志精锐部队，即将突袭我方基地",0],["%s",0],["请全体指挥官派兵前往参战",0],["前往参与",1]]
    /** 恭喜各位指挥官，本次德意志反攻战，成功击退敌军，我方赢得胜利！ */
    public static final int CHAT_COUNTER_ATK_END_BOSS_DEAD = 82; // [["恭喜各位指挥官，本次德意志反攻战，成功击退敌军，我方赢得胜利！",0]]
    /** 各位指挥官，本次德意志反攻战，我方未能获得胜利，请各位再接再厉，为下次反攻战做好准备 */
    public static final int CHAT_COUNTER_ATK_END_BOSS_NOT_DEAD = 83; // [["恭喜各位指挥官，本次德意志反攻战，成功击退敌军，我方赢得胜利！",0]]
    /** 我的基地[255,255]被德意志部队发起了进攻，请求支援！ */
    public static final int CHAT_COUNTER_ATK_BOSS_ATK_HELP = 84; // [["我的基地",0],["[%s,%s]",1],["被德意志部队",0],["发起了进攻，请求支援！",0]]
    /** 惊喜礼包的跑马灯 */
    public static final int CHAT_SURPRISE_PACK = 87;
    /** 诞许愿活获得 专属聊天气泡 */
    public static final int CHAT_WISHING_AWARD = 88;
    /** 队伍副消息 */
    public static final int CHAT_COMBAT_TEAM = 89;
    /** 多人副本首次通关 */
    public static final int CHAT_MULT_COMBAT_FIRSTPASS = 90;
    /** 幸运奖池 公告 */
    public static final int CHAT_LUCKY_POOL = 92;
    /** 飞艇被击杀 */
    public static final int CHAT_AIR_SHIP_DEAD = 93;
    /** 获取争斗权 */
    public static final int CHAT_AIR_SHIP_GET_BELONG = 94;
    /** 飞艇重新刷出来 */
    public static final int CHAT_AIRSHIP_REAPPEAR = 95;
    /** 幸运奖池活动中获得XX戒指 */
    public static final int CHAT_SURPRISE_GIFT = 97;
    /** 纽约争夺战活动广播预告 */
    public static final int CHAT_NEW_YORK_WAR_IMMEDIATELY = 98;
    /** 纽约争夺战开启通知 */
    public static final int CHAT_NEW_YORK_WAR_ROUND_START = 99;
    /** 纽约争夺战防守成功广播 */
    public static final int CHAT_NEW_YORK_WAR_DEFEND_SUCCESS = 100;
    /** 纽约争夺战争夺成功广播 */
    public static final int CHAT_NEW_YORK_WAR_ATTACK_SUCCESS = 101;
    /** 纽约争夺战结束广播 */
    public static final int CHAT_NEW_YORK_WAR_END = 102;

    /** 打开随机橙色战机箱时发送广播，全军返利活动中获得 [["恭喜",0],["指挥官",0],["%s",0],["在橙色战机箱中获得",0],["%s",0],["战机,",0],["前往获得",1]] */
    public static final int CHAT_ORANGE_PLANE_BOX = 103;

    /** 世界BOSS开启预告广播 [["已达到世界进程第",0],["%s",0],["阶段",0],["请各指挥官前往战区摧毁废墟，击败废墟守军后可进入",0]["%s",0],["战区",0]] **/
    public static final int CHAT_SCHEDULE_5_OR_9 = 106;

    /**
     * 阵营活动大国风范购买广播
     */
    public static final int CHAT_ROYAL_ARENA_COUNTRY_BUY = 107;
    /**
     * 阵营活动刺探购买广播
     */
    public static final int CHAT_ROYAL_ARENA_DETECT_BUY = 108;

    /**
     * 英雄分享
     */
    public static final int CHAT_HERO_SHARE = 110;

    /**
     * 兵书分享
     */
    public static final int CHAT_MEDAL_SHARE = 111;

    public static final int CHAT_EXCHANGE_SKIN = 115;

    /**
     * 战火燎原兑换商品
     */
    public static final int CHAT_WAR_FIRE_SHOP = 121;

    /**
     * 战火燎原进攻城池
     */
    public static final int CHAT_WAR_FIRE_ATK_HELP = 123;
    /**
     * 战火燎原防守城池
     */
    public static final int CHAT_WAR_FIRE_DEF_HELP = 124;

    /**
     * [["恭喜",0],["%s"],["领主",0],["%s"],["将",0],["%s"],["军职升级到",0],["%s"],["，！战力飞升！可喜可贺！",0]]
     * 恭喜【亚洲】领主【刘磊】将【赵云】军职升级到【高级督军】！可喜可贺！
     */
    public static final int SEASON_HERO_UPGRADE_STAGE = 125;
    /**
     * [["恭喜",0],["%s"],["领主",0],["%s"],["解锁了赛季英雄",0],["%s"],["突破后将变得十分强力！",0]]
     * 恭喜【亚洲】领主【刘磊】解锁了赛季英雄【赵云】
     */
    public static final int SEASON_GET_HERO = 127;

    /**
     * 圣坛已经出现在xx.xx，请各位领主抓紧时间前往拜访
     */
    public static final int CHAT_REFRESH_ALTAR = 128;

    /**
     * 圣坛已消失，请等待下一次开启。下一次开启时间为x月x日xx：xx
     */
    public static final int CHAT_CLEAR_ALTAR = 129;

    /**
     * 拜访圣坛活动已结束，敬请期待下一次开放
     */
    public static final int CHAT_VISIT_ALTAR_END = 130;

    /**
     * 我在码头钓到了一条【大白鲨】，[点击查看]
     */
    public static final int CHAT_FISHING_SHARE_FISH = 131;
    /**
     * 运气爆棚！恭喜亚洲领主赵四在码头钓到珍品鱼类【大白鲨】！欧气四射！可喜可贺！我也想要！
     */
    public static final int CHAT_FISHING_STOWROD = 132;
    /**
     * 恭喜【国家】领主【邱少云】全服第【1-5】位成功打造红色装备【AKM突击步枪】，实力大增，我也要
     */
    public static final int CHAT_MAKE_RED_EQUIPMENT = 133;

    /**
     * 新的一轮秋季拍卖已经开始，请各位领主抓紧时间前往拍卖场。立即前往
     */
    public static final int CHAT_ACT_AUCTION_ROUND_START = 134;
    /**
     * 领主xxx财大气粗，一口价直接买下xxx，真是羡煞旁人
     */
    public static final int CHAT_ACT_DIRECT_BIDDING = 135;
    /**
     * 本轮拍卖活动还有3分钟即将结束，请各位领主把握时间
     */
    public static final int CHAT_ACT_AUCTION_ROUND_END = 136;

    /**
     * 宝石进阶跑马灯
     */
    public static final int CHAT_DO_STONE_IMPROVE = 137;

    /**
     * 宝石升星跑马灯
     */
    public static final int CHAT_STONE_IMPROVE_UP_LV = 138;

    /**
     * [["恭喜",0],["%s"],["阵营成功开启玛雅音乐节",0],["，舞台位于主城左下方",0],["，请领主们前往主城参加活动。",0],["参加创作活动的领主每人将会获得一份特殊的阵营奖励！",0]]
     *
     */
    public static final int CAMP_OPEN_MUSIC_SUCCESS = 139;


    /**
     * 宝具分享
     */
    public static final int CHAT_TREASURE_WARE_SHARE = 140;

    /**
     * [["恭喜",0],["%s"],["主公",0],["%s"],["的",0],["%s"],["已提升至满阶！",0]
     *
     */
    public static final int CHAT_HERO_FULL_GRADE = 141;

    public static final int FIREWORKS_PREVIEW = 200;//烟花活动开始前的跑马灯
    public static final int FIREWORKS_LETOFF = 201;//系统放烟花的跑马灯
    public static final int YEARFISH_LONG = 202;//年年有鱼活动捕到龙鱼
    public static final int FIREWORKS_LETOFF1 = 203;//玩家燃放烟花的跑马灯

    /**
     * 广播跨服充值活动总榜第一
     */
    public static final int CHAT_CROSS_RECHARGE_SETTLE_TOTAL = 2001;
    /**
     * 广播跨服充值活动日榜第一
     */
    public static final int CHAT_CROSS_RECHARGE_SETTLE_DAILY = 2002;

    public static Set<Integer> SENDCHAT_HELP_CHATID_SET = new HashSet<>();
    public static Set<Integer> SENDCHAT_HELP_DEF_CHATID_SET = new HashSet<>();
    public static Set<Integer> SENDCHAT_HELP_ATK_CHATID_SET = new HashSet<>();

    static {
        SENDCHAT_HELP_CHATID_SET.add(ChatConst.CHAT_DEF_SEEK_HELP);
        SENDCHAT_HELP_CHATID_SET.add(ChatConst.CHAT_ATK_SEEK_HELP);
        SENDCHAT_HELP_CHATID_SET.add(ChatConst.CHAT_CAMP_DEF_SEEK_HELP);
        SENDCHAT_HELP_CHATID_SET.add(ChatConst.CHAT_CAMP_ATK_SEEK_HELP);
        SENDCHAT_HELP_CHATID_SET.add(ChatConst.CHAT_GESTAPO_ATK_SEEK_HELP);
        SENDCHAT_HELP_CHATID_SET.add(ChatConst.CHAT_SUPER_MINE_DEF_HELP);
        SENDCHAT_HELP_CHATID_SET.add(ChatConst.CHAT_DEF_REBEL_HELP);
        SENDCHAT_HELP_CHATID_SET.add(ChatConst.CHAT_COUNTER_ATK_BOSS_ATK_HELP);

        // 防守的chatId
        SENDCHAT_HELP_DEF_CHATID_SET.add(ChatConst.CHAT_DEF_SEEK_HELP);
        SENDCHAT_HELP_DEF_CHATID_SET.add(ChatConst.CHAT_CAMP_DEF_SEEK_HELP);
        SENDCHAT_HELP_DEF_CHATID_SET.add(ChatConst.CHAT_DEF_REBEL_HELP);
        SENDCHAT_HELP_DEF_CHATID_SET.add(ChatConst.CHAT_COUNTER_ATK_BOSS_ATK_HELP);

        // 进攻的chaId
        SENDCHAT_HELP_ATK_CHATID_SET.add(ChatConst.CHAT_ATK_SEEK_HELP);
        SENDCHAT_HELP_ATK_CHATID_SET.add(ChatConst.CHAT_CAMP_ATK_SEEK_HELP);
        SENDCHAT_HELP_ATK_CHATID_SET.add(ChatConst.CHAT_GESTAPO_ATK_SEEK_HELP);
    }

}
