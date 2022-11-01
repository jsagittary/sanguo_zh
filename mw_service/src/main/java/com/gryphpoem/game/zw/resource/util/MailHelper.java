package com.gryphpoem.game.zw.resource.util;

import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.resource.constant.MailConstant;

import java.util.HashMap;
import java.util.Map;

/**
 * @author TanDonghai
 * @ClassName MailHelper.java
 * @Description 邮件工具类
 * @date 创建时间：2017年4月11日 下午8:01:43
 */
public class MailHelper {
    /**
     * 记录各邮件的标题和内容参数个数
     */
    private static Map<Integer, Turple<Integer, Integer>> paramNumMap;

    /**
     * 无参数
     */
    private static final Turple<Integer, Integer> nonParam = new Turple<Integer, Integer>(0, 0);

    static {
        paramNumMap = new HashMap<>();
        registerMailParamNum();
    }

    /**
     * 注册邮件参数个数
     *
     * @param mailId          邮件id
     * @param titleParamNum   标题参数个数
     * @param contentParamNum 邮件内容参数个数
     */
    private static void putMailParamNum(int mailId, int titleParamNum, int contentParamNum) {
        Turple<Integer, Integer> param = new Turple<Integer, Integer>(titleParamNum, contentParamNum);
        paramNumMap.put(mailId, param);
    }

    /**
     * 邮件参数个数统一在这里注册
     */
    public static void registerMailParamNum() {
        putMailParamNum(MailConstant.ACT_CHRISTMAS_CAMP_RANK_MAIL, 2, 2);
        // 攻击流寇成功战报
        putMailParamNum(MailConstant.MOLD_ATK_BANDIT_SUCC, 2, 7);
        // 攻击流寇失败战报
        putMailParamNum(MailConstant.MOLD_ATK_BANDIT_FAIL, 2, 6);
        // 进攻目标丢失
        putMailParamNum(MailConstant.MOLD_ATK_TARGET_NOT_FOUND, 2, 2);
        // 采集报告,标题参数只需要显示2个，客户端需要3个参数才能显示正确
        putMailParamNum(MailConstant.MOLD_COLLECT, 3, 4);
        // 采集目标丢失
        putMailParamNum(MailConstant.MOLD_COLLECT_NO_TARGET, 0, 0);
        // 占领采集点失败
        putMailParamNum(MailConstant.MOLD_COLLECT_ATK_FAIL, 2, 6);
        // 占领采集点成功
        putMailParamNum(MailConstant.MOLD_COLLECT_ATK_SUCC, 2, 6);
        // 采集点防守成功
        putMailParamNum(MailConstant.MOLD_COLLECT_DEF_SUCC, 2, 6);
        // 采集点防守失败
        putMailParamNum(MailConstant.MOLD_COLLECT_DEF_FAIL, 2, 6);
        putMailParamNum(MailConstant.MOLD_COLLECT_DEF_FAIL_COLLECT, 2, 6);
        // 采集中途撤回
        putMailParamNum(MailConstant.MOLD_COLLECT_RETREAT, 0, 4);
        // 行军报告，城战被攻击玩家迁城，通知攻击方玩家
        putMailParamNum(MailConstant.MOLD_CITY_DEF_FLEE_ATK, 1, 1);
        // 行军报告，城战被攻击玩家迁城，通知去增援的盟友
        putMailParamNum(MailConstant.MOLD_CITY_DEF_FLEE_DEF, 1, 1);
        // 行军报告，发起城战玩家撤回部队，通知防守方玩家
        putMailParamNum(MailConstant.MOLD_CITY_ATK_RETREAT_DEF, 5, 5);
        // 行军报告，发起城战玩家撤回部队，通知攻击方的增援玩家
        putMailParamNum(MailConstant.MOLD_CITY_ATK_RETREAT_ATK, 5, 5);
        // 敌方侦查成功
        putMailParamNum(MailConstant.MOLD_ENEMY_SCOUT_SUCC, 5, 5);
        // 侦查成功
        putMailParamNum(MailConstant.MOLD_SCOUT_SUCC, 5, 5);
        // 侦查失败
        putMailParamNum(MailConstant.MOLD_SCOUT_FAIL, 5, 5);
        // 城战防守失败
        putMailParamNum(MailConstant.MOLD_DEF_CITY_FAIL, 4, 9);
        // 城战防守成功
        putMailParamNum(MailConstant.MOLD_DEF_CITY_SUCC, 4, 8);
        // 城战进攻失败
        putMailParamNum(MailConstant.MOLD_ATK_CITY_FAIL, 4, 7);
        // 城战进攻成功
        putMailParamNum(MailConstant.MOLD_ATK_CITY_SUCC, 4, 8);
        // 军团战（国战、阵营战）防守失败
        putMailParamNum(MailConstant.MOLD_DEF_CAMP_FAIL, 4, 8);
        // 军团战（国战、阵营战）防守成功
        putMailParamNum(MailConstant.MOLD_DEF_CAMP_SUCC, 4, 8);
        // 军团战（国战、阵营战）进攻失败
        putMailParamNum(MailConstant.MOLD_ATK_CAMP_FAIL, 2, 6);
        // 军团战（国战、阵营战）进攻成功
        putMailParamNum(MailConstant.MOLD_ATK_CAMP_SUCC, 2, 7);
        // 城池竞选成功
        putMailParamNum(MailConstant.MOLD_CAMPAIGN_SUCC, 1, 2);
        // 城池竞选失败
        putMailParamNum(MailConstant.MOLD_CAMPAIGN_FAIL, 2, 2);
        // 城池城主额外奖励
        putMailParamNum(MailConstant.MOLD_CITY_EXTRA_REWARD, 1, 2);
        // 城主任期结束
        putMailParamNum(MailConstant.MOLD_CITY_OWNER_END, 1, 1);
        // 后台发补偿的邮件
        putMailParamNum(MailConstant.MOLD_GM_COMPENSATE, 0, 0);
        // 后台发福利邮件
        putMailParamNum(MailConstant.MOLD_GM_WALFARE, 0, 0);
        // 版本更新公告
        putMailParamNum(MailConstant.MOLD_GM_UPDATE, 1, 1);
        // 纯自定义邮件
        putMailParamNum(MailConstant.MOLD_GM_CUSTOM, 1, 1);
        // 纯自定义邮件
        putMailParamNum(MailConstant.MOLD_HIT_FLY, 1, 1);
        // 拜师奖励(给徒弟的)
        putMailParamNum(MailConstant.MOLD_ADD_MASTER_REWARD, 1, 1);
        // 升级奖励
        putMailParamNum(MailConstant.MOLD_PARTY_LV_REWARD, 1, 1);
        // 充值到账
        putMailParamNum(MailConstant.MOLD_PAY_DONE, 1, 2);
        // 充值礼包成功
        putMailParamNum(MailConstant.MOLD_PAY_GIFT_FAIL, 1, 1);
        // 首冲礼包注册
        putMailParamNum(MailConstant.MOLD_FIRST_PAY_AWARD, 1, 1);
        // 充值礼包失败
        putMailParamNum(MailConstant.MOLD_PAY_GIFT_SUC, 1, 1);
        // 月卡
        putMailParamNum(MailConstant.MOLD_MONTH_CARD_REWARD, 1, 1);
        // 欢迎
        putMailParamNum(MailConstant.MOLD_MAIL_WELLCOME, 0, 0);
        // 欢迎
        putMailParamNum(MailConstant.MOLD_MAIL_WELLCOME2, 0, 0);
        // 雇佣建造队金币返还
        putMailParamNum(MailConstant.BUY_BUILD_1, 1, 1);
        // 雇佣建造队金币返还
        putMailParamNum(MailConstant.BUY_BUILD_2, 1, 1);
        // 驻防将领被击杀
        putMailParamNum(MailConstant.WALL_HELP_KILLED, 2, 2);
        // 驻防位置已满
        putMailParamNum(MailConstant.WALL_HELP_FILL, 2, 2);
        // 对方开启保护罩返回
        putMailParamNum(MailConstant.MOLD_ATTACK_TARGET_HAS_PROTECT, 3, 3);
        // 已被其他指挥官击败
        putMailParamNum(MailConstant.MOLD_ATTACK_TARGET_FLY, 1, 1);
        // 收徒成功
        putMailParamNum(MailConstant.MOLD_ADD_APPRENTICE, 1, 1);
        // 驻防将领返回
        putMailParamNum(MailConstant.MOLD_GARRISON_RETREAT, 2, 2);
        // 驻防将领被遣返
        putMailParamNum(MailConstant.MOLD_GARRISON_REPATRIATE, 2, 2);
        // 敌方侦查失败
        putMailParamNum(MailConstant.MOLD_ENEMY_SCOUT_FAIL, 5, 5);
        // 驻防将领8小时返回
        putMailParamNum(MailConstant.MOLD_GARRISON_FILL_TIME_RETREAT, 2, 2);
        // 驻防将领8小时返回
        putMailParamNum(MailConstant.MOLD_RECOMMEND_CAMP_REWARD, 1, 1);
        // 空降补给金币返还
        putMailParamNum(MailConstant.MOLD_SUPPLY_DORP_RETURN, 1, 1);
        // 空降补给金币和未领取的道具返还
        putMailParamNum(MailConstant.MOLD_SUPPLY_DORP_ALL_RETURN, 1, 1);
        // 未领奖的活动发放
        putMailParamNum(MailConstant.MOLD_ACT_UNREWARDED_REWARD, 2, 2);
        // 全军返利阵营优胜奖
        putMailParamNum(MailConstant.MOLD_ACT_ALL_CHARGE_REWARD, 1, 1);
        // 充值双倍金币
        putMailParamNum(MailConstant.MOLD_FIRST_DOUBLE_PAY_DONE, 1, 1);
        // 攻打盖世太保成功
        putMailParamNum(MailConstant.MOLD_ATK_GESTAPO_SUCC, 2, 7);
        // 攻打盖世太保失败
        putMailParamNum(MailConstant.MOLD_ATK_GESTAPO_FAIL, 2, 6);
        // 攻打盖世太保目标丢失
        putMailParamNum(MailConstant.MOLD_ATK_GESTAPO_NOT_FOUND, 2, 2);
        // 盖世太保活动结束道具回收
        putMailParamNum(MailConstant.MOLD_ACT_PROP_RECYCLE, 2, 2);
        // 城市首杀奖励
        putMailParamNum(MailConstant.MOLD_FIRST_KILL_REWARD, 1, 1);
        // 闪电战boss攻打奖励
        putMailParamNum(MailConstant.MOLD_ATK_LIGHTNING_WAR_BOSS, 2, 7);
        // 闪电战boss击杀奖励
        putMailParamNum(MailConstant.MOLD_ATK_LIGHTNING_WAR_BOSS_SUCC, 2, 1);
        // 盖世太保排行榜个人奖励
        putMailParamNum(MailConstant.MOLD_GESTAPO_KILL_REWARD, 0, 1);
        // 盖世太保排行榜阵营奖励
        putMailParamNum(MailConstant.MOLD_GESTAPO_KILL_CAMP_REWARD, 0, 2);
        // 盖世太保排行榜阵营奖励
        putMailParamNum(MailConstant.MOLD_ACT_EXCHANGE_REWARD, 2, 2);
        // 阵营邮件
        putMailParamNum(MailConstant.MOLD_CAMP_MAIL, 4, 2);
        // 超级采集点已满
        putMailParamNum(MailConstant.MOLD_SUPER_MINE_COLLECT_FILL, 0, 3);
        // 采集报告
        putMailParamNum(MailConstant.MOLD_SUPER_MINE_COLLECT, 2, 3);
        // 攻打超级资源点失败
        putMailParamNum(MailConstant.MOLD_SUPER_ATK_FAIL, 3, 7);
        // 攻打超级资源点成功
        putMailParamNum(MailConstant.MOLD_SUPER_ATK_SUCCESS, 3, 7);
        // 超级资源点防守成功
        putMailParamNum(MailConstant.MOLD_SUPER_DEF_SUCCESS, 3, 7);
        // 超级资源点防守失败
        putMailParamNum(MailConstant.MOLD_SUPER_DEF_FAIL, 3, 7);
        // 超级资源点采集中途撤回
        putMailParamNum(MailConstant.MOLD_SUPER_MINE_COLLECT_MIDWAY_RETURN, 0, 3);
        // 驻防将领返回
        putMailParamNum(MailConstant.MOLD_SUPER_MINE_HELP_RETURN, 4, 4);
        // 驻防将领被击杀
        putMailParamNum(MailConstant.MOLD_SUPER_MINE_HELP_KILL, 4, 4);
        // 驻防位置已满
        putMailParamNum(MailConstant.MOLD_SUPER_MINE_HELP_FILL, 4, 4);
        // 驻防位置已满
        putMailParamNum(MailConstant.MOLD_SUPER_MINE_ATK_RUN_AWAY, 3, 3);
        // 大型资源点停产
        putMailParamNum(MailConstant.MOLD_SUPER_MINE_STOP, 3, 7);
        // 下次柏林会战预告
        putMailParamNum(MailConstant.MOLD_NEXT_BERLIN_PREVIEW, 0, 2);
        // 阵营成功占领柏林
        putMailParamNum(MailConstant.MOLD_BERLIN_WAR_REWARD, 1, 5);
        // 柏林会战优胜奖
        putMailParamNum(MailConstant.MOLD_BERLIN_SUC_CAMP, 0, 0);
        // 柏林会战参与奖
        putMailParamNum(MailConstant.MOLD_BERLIN_FAIL_CAMP, 0, 0);
        // 柏林会战参与奖
        putMailParamNum(MailConstant.MOLD_GM_UPDATE_EXPLAIN, 1, 1);
        // 柏林活动调整补偿
        putMailParamNum(MailConstant.MOLD_BERLIN_COMPENSATION_AWARD, 0, 0);
        // 开服阵容奖励
        putMailParamNum(MailConstant.MOLD_ACT_CAMPRANK_REWARD, 2, 0);
        // 匪军叛乱防守失败 给参加者发
        putMailParamNum(MailConstant.MOLD_REBEL_DEF_FAIL_JOIN, 2, 4);
        // 匪军叛乱防守成功 给参加者发
        putMailParamNum(MailConstant.MOLD_REBEL_DEF_SUCC_JOIN, 2, 4);
        // 匪军叛乱全通过
        putMailParamNum(MailConstant.MOLD_REBEL_ALL_PASS, 0, 1);
        // 匪军叛乱防守失败 给帮助者发
        putMailParamNum(MailConstant.MOLD_REBEL_DEF_FAIL_HELP, 2, 4);
        // 匪军叛乱防守成功 给帮助者发
        putMailParamNum(MailConstant.MOLD_REBEL_DEF_SUCC_HELP, 2, 4);
        // 阵营排行优胜奖
        putMailParamNum(MailConstant.MOLD_ACT_WIND_CAMP_FIGHT_RANK_REWARD, 1, 1);
        // 反攻德意志BOSS防守 给参加者发
        putMailParamNum(MailConstant.MOLD_COUNTER_BOSS_DEF_JOIN, 1, 8);
        // 反攻德意志BOSS进攻 玩家防守成功
        putMailParamNum(MailConstant.MOLD_COUNTER_BOSS_ATK_FAIL, 1, 4);
        // 反攻德意志BOSS进攻 玩家防守失败
        putMailParamNum(MailConstant.MOLD_COUNTER_BOSS_ATK_SUCC, 1, 4);
        // 反攻德意志BOSS防守 击杀当前BOSS
        putMailParamNum(MailConstant.MOLD_COUNTER_BOSS_DEF_DEAD, 0, 4);
        // 反攻德意志BOSS防守 击杀最后BOSS
        putMailParamNum(MailConstant.MOLD_COUNTER_BOSS_DEF_END, 0, 4);
        // 反攻德意志BOSS防守 击杀最后BOSS
        putMailParamNum(MailConstant.MOLD_COUNTER_BOSS_JOIN_BATTLE, 0, 1);
        // 反攻德意志BOSS 胜利奖励
        putMailParamNum(MailConstant.MOLD_COUNTER_BOSS_JOIN_AWARD, 0, 1);
        // 功能特权卡 金币部分
        putMailParamNum(MailConstant.MOLD_FUN_CARD_GOLD, 1, 1);
        // 功能特权卡 每日奖励部分
        putMailParamNum(MailConstant.MOLD_FUN_CARD_AWARD, 2, 2);
        // 决战行军报告
        putMailParamNum(MailConstant.DECISIVE_BATTLE_MARCH_REPORT, 1, 3);
        // 决战进攻失败邮件模板
        putMailParamNum(MailConstant.DECISIVE_BATTLE_ATK_FAIL, 4, 7);
        // 决战进攻成功邮件模板
        putMailParamNum(MailConstant.DECISIVE_BATTLE_ATK_SUCCESS, 4, 7);
        // 决战防守失败邮件模板
        putMailParamNum(MailConstant.DECISIVE_BATTLE_DEF_FAIL, 4, 4);
        // 决战防守成功邮件模板
        putMailParamNum(MailConstant.DECISIVE_BATTLE_DEF_SUCCESS, 4, 4);
        // 决战进攻取消邮件模板
        putMailParamNum(MailConstant.DECISIVE_BATTLE_ATK_CANCEL, 5, 5);
        // 决战防御取消邮件模板
        putMailParamNum(MailConstant.DECISIVE_BATTLE_DEF_CANCEL, 5, 5);
        // 决战中玩家驻防被遣返邮件模板
        putMailParamNum(MailConstant.DECISIVE_BATTLE_GARRISON_CANCEL, 4, 4);
        // 飞艇逃离
        putMailParamNum(MailConstant.MOLD_AIRSHIP_RUN_AWAY, 1, 1);
        // 获得飞艇归属权
        // putMailParamNum(MailConstant.MOLD_AIR_SHIP_GET_BELONG, 1, 2);
        // 未获得飞艇归属权
        // putMailParamNum(MailConstant.MOLD_AIR_SHIP_NOT_GET_BELONG, 1, 2);
        // 攻打飞艇成功
        putMailParamNum(MailConstant.MOLD_AIR_SHIP_BATTLE_SUC, 2, 6);
        // 攻打飞艇失败
        putMailParamNum(MailConstant.MOLD_AIR_SHIP_BATTLE_FAIL, 2, 6);
        // 飞艇被击飞
        putMailParamNum(MailConstant.MOLD_AIR_SHIP_DEAD, 1, 1);
        // 成功抢夺飞艇归属权
        // putMailParamNum(MailConstant.MOLD_AIRSHIP_FIGHT_BELONG, 1, 3);
        // 飞艇归属者奖励
        // putMailParamNum(MailConstant.MOLD_AIR_SHIP_BELONG_AWARD, 0, 3);
        // 飞艇归属权被抢夺
        // putMailParamNum(MailConstant.MOLD_AIR_SHIP_LOST_BELONG, 2, 7);
        // 世界boss攻打成功
        putMailParamNum(MailConstant.MOLD_ATK_SCHEDULE_BOSS_SUC, 2, 7);
        // 世界boss攻打失败
        putMailParamNum(MailConstant.MOLD_ATK_SCHEDULE_BOSS_FAIL, 2, 7);
        // 世界进程阶段结束奖励
        putMailParamNum(MailConstant.MOLD_WORLD_SCHEDULE_RANK_AWARD, 1, 2);
        // 世界争霸通用奖励
        putMailParamNum(MailConstant.WORLD_WAR_COMMON_REWARD, 1, 1);
        // 世界争霸个人积分排名奖励
        putMailParamNum(MailConstant.WORLD_WAR_PERSONAL_RANK_REWARD, 1, 2);
        // 世界争霸阵营积分排名奖励
        putMailParamNum(MailConstant.WORLD_WAR_CAMP_RANK_REWARD, 1, 2);
        // 建筑礼包邮件奖励
        putMailParamNum(MailConstant.MOLD_BUILD_GIFT_REWARD, 0, 0);
        // 勇冠三军阵营优胜奖
        putMailParamNum(MailConstant.MOLD_ACT_BRAVEST_ARMY_AWARD, 1, 1);
        // 世界争霸阵营积分排名奖励
        putMailParamNum(MailConstant.WORLD_WAR_CAMP_RANK_REWARD, 1, 2);
        // 战令未领取邮件奖励
        putMailParamNum(MailConstant.MOLD_BATTLE_PASS_REWARD, 0, 0);
        /** 新地图已经关闭 */
        putMailParamNum(MailConstant.MOLD_WORLD_WAR_CLOSE, 0, 0);
        /** 新地图采集中断 */
        putMailParamNum(MailConstant.MOLD_CROSSMAP_COLLECT_BREAK, 2, 2);
        /** 新地图城池竞选成功 */
        putMailParamNum(MailConstant.MOLD_NEWMAP_CAMPAIGN_SUCC, 1, 1);
        /** 奖章兑换活动结束道具回收 */
        putMailParamNum(MailConstant.MOLD_ACT_BANDIT_CONVERT_AWARD, 1, 1);
        /** 纽约争夺战优胜奖 */
        putMailParamNum(MailConstant.MOLD_NEWYORK_WAR_JOIN_SUCCESS_AWARD, 0, 2);
        /** 纽约争夺战参与奖 */
        putMailParamNum(MailConstant.MOLD_NEWYORK_WAR_JOIN_AWARD, 0, 1);
        /** 阵营杀敌排行奖励 */
        putMailParamNum(MailConstant.MOLD_NEWYORK_WAR_CAMP_RANK_AWARD, 0, 3);
        /** 个人杀敌排行奖励 */
        putMailParamNum(MailConstant.MOLD_NEWYORK_WAR_PERSONAL_RANK_AWARD, 0, 2);
        /** 争夺战活动进攻成功 */
        putMailParamNum(MailConstant.MOLD_NEWYORK_WAR_ROUND_ATTACK_SUCCESS, 1, 6);
        /** 争夺战活动进攻失败 */
        putMailParamNum(MailConstant.MOLD_NEWYORK_WAR_ROUND_ATTACK_FAIL, 1, 3);
        /** 争夺战活动防守成功 */
        putMailParamNum(MailConstant.MOLD_NEWYORK_WAR_ROUND_DEFINE_SUCCESS, 1, 3);
        /** 争夺战活动防守失败 */
        putMailParamNum(MailConstant.MOLD_NEWYORK_WAR_ROUND_DEFINE_FAIL, 1, 3);
        /** 跨服战阵营排行奖 */
        putMailParamNum(MailConstant.MOLD_CROSS_WAR_CAMP_AWARD, 0, 2);
        /** 跨服战个人排行奖 */
        putMailParamNum(MailConstant.MOLD_CROSS_WAR_PERSONAL_AWARD, 0, 1);
        /** 跨服战参与奖 */
        putMailParamNum(MailConstant.MOLD_CROSS_WAR_JOIN_AWARD, 0, 0);
        /** 跨服战优胜奖 */
        putMailParamNum(MailConstant.MOLD_CROSS_WAR_WIN_AWARD, 0, 1);
        /** 爱丽丝奖励邮件 */
        putMailParamNum(MailConstant.MOLD_ALICE_AWARD, 0, 0);
        // 进攻世界boss个人排行奖励
        putMailParamNum(MailConstant.MOLD_ATK_BOSS_PERSON_RANK_AWARD, 0, 2);
        // 进攻世界boss获取的奖励
        putMailParamNum(MailConstant.MOLD_ATK_BOSS_AWARD, 0, 0);
        // 加入社群邮件奖励
        putMailParamNum(MailConstant.MOLD_JOIN_COMMUNITY_REWARD, 0, 0);
        // 阵营对拼活动的阵营排行奖励
        putMailParamNum(MailConstant.MOLD_ROYAL_ARENA_CAMP_REWARD, 0, 1);
        // 阵营对拼活动的个人排行奖励
        putMailParamNum(MailConstant.MOLD_ROYAL_ARENA_PERSON_REWARD, 0, 1);
        // 禁言通知
        putMailParamNum(MailConstant.MOLD_SILENCE, 0, 0);
        // 微信签到奖励
        putMailParamNum(MailConstant.MOLD_WECHAT_SIGN_REWARD, 0, 0);
        // 师徒关系解除
        putMailParamNum(MailConstant.MOLD_DEL_MASTER_APPRENTICE, 0, 1);
        //战火燎原活动积分奖励
        putMailParamNum(MailConstant.MOLD_ACT_WAR_FIRE_AWARD_NEW, 0, 17);
        //战火燎原未获得奖励
        putMailParamNum(MailConstant.MOLD_ACT_WAR_FIRE_SCROE_NOT_ENOUGH_NEW, 0, 14);
        // 战火燎原相同阵营炸矿
        putMailParamNum(MailConstant.MOLD_WAR_FIRE_SAME_CAMP_COLLECT, 0, 3);
        putMailParamNum(MailConstant.MOLD_ACT_DIAOCHAN_RANK_AWARD, 2, 4);
        putMailParamNum(MailConstant.MOLD_OUT_OF_RANGE_AWARD, 0, 0);
        //赛季
        putMailParamNum(MailConstant.MOLD_SEASON_522, 0, 1);
        putMailParamNum(MailConstant.MOLD_SEASON_523, 0, 1);
        putMailParamNum(MailConstant.MOLD_SEASON_524, 0, 0);
        putMailParamNum(MailConstant.MOLD_SEASON_525, 0, 0);


        // 限时活动物品自动兑换
        putMailParamNum(MailConstant.MOLD_ACT_TIME_LIMIT_EXCHANGE_AWARD, 0, 4);

        //沙盘演武
        putMailParamNum(MailConstant.MOLD_SAND_TABLE_CAMP_ENROLL, 0, 1);
        putMailParamNum(MailConstant.MOLD_SAND_TABLE_ROUND_OVER_WIN, 1, 6);
        putMailParamNum(MailConstant.MOLD_SAND_TABLE_ROUND_OVER_LOSE, 1, 6);
        putMailParamNum(MailConstant.MOLD_SAND_TABLE_ROUND_OVER_DRAW, 1, 6);
        putMailParamNum(MailConstant.MOLD_SAND_TABLE_CAMP_RANK_REWARD, 2, 4);
        putMailParamNum(MailConstant.MOLD_SAND_TABLE_PERSONAL_REWARD, 1, 3);
        putMailParamNum(MailConstant.MOLD_SAND_TABLE_ENROLL_REWARD, 0, 3);
        putMailParamNum(MailConstant.MOLD_SAND_TABLE_PERSONAL_REWARD_TWO, 0, 2);
        putMailParamNum(MailConstant.MOLD_SAND_TABLE_PERSONAL_REWARD_THREE, 0, 2);

        // 拜访圣坛
        putMailParamNum(MailConstant.MOLD_VISIT_ALTAR_SUCCESS, 0, 1);
        putMailParamNum(MailConstant.MOLD_VISIT_ALTAR_FAIL, 0, 1);

        //皮肤返场活动
        putMailParamNum(MailConstant.MOLD_ACT_SKIN_ENCORE_SUCCESS, 1, 1);
        putMailParamNum(MailConstant.MOLD_ACT_SKIN_ENCORE_FAIL, 1, 1);

        //赛季天赋结束邮件
        putMailParamNum(MailConstant.RESET_END_SEASON_TALENT, 0, 1);

        //秋季拍卖活动
        putMailParamNum(MailConstant.SUCCESSFUL_BIDDING, 0, 3);
        putMailParamNum(MailConstant.BID_WAS_OVERTAKEN, 0, 2);
        putMailParamNum(MailConstant.BID_FAILED, 0, 2);

        //音乐创作
        putMailParamNum(MailConstant.MUSIC_CAMP_RANK_AWARD, 0, 3);
        putMailParamNum(MailConstant.MUSIC_PLAYER_RANK_AWARD, 0, 2);
        putMailParamNum(MailConstant.MUSIC_CAMP_CONDUCT_SUCCESS, 0, 2);

        putMailParamNum(MailConstant.MOD_LONG_LIGHT, 1, 1);

        //跨服充值活动奖励发放
        putMailParamNum(MailConstant.MOLD_ACT_CROSS_RECHARGE_TOTAL, 0, 1);
        putMailParamNum(MailConstant.MOLD_ACT_CROSS_RECHARGE_DAILY, 0, 1);
        putMailParamNum(MailConstant.ACT_MAGIC_TREASURE_WARE_GIFT_BAG, 1, 1);

        putMailParamNum(MailConstant.CROSS_COLLECT_REPORT, 2, 4);
        putMailParamNum(MailConstant.CROSS_COLLECT_NO_TARGET, 0, 0);
        putMailParamNum(MailConstant.CROSS_COLLECT_ATK_FAIL, 4, 8);
        putMailParamNum(MailConstant.CROSS_COLLECT_ATK_SUCCESS, 4, 8);
        putMailParamNum(MailConstant.CROSS_COLLECT_DEF_SUCCESS, 4, 8);
        putMailParamNum(MailConstant.CROSS_COLLECT_DEF_FAIL, 4, 8);
        putMailParamNum(MailConstant.CROSS_COLLECT_RETREAT, 0, 4);
        putMailParamNum(MailConstant.CROSS_PLAYER_DEF_FLEE_ATK, 2, 2);
        putMailParamNum(MailConstant.MOLD_PLAYER_DEF_FLEE_DEF, 2, 2);
        putMailParamNum(MailConstant.MOLD_PLAYER_ATK_RETREAT_DEF, 7, 7);
        putMailParamNum(MailConstant.MOLD_PLAYER_ATK_RETREAT_ATK, 7, 7);
        putMailParamNum(MailConstant.CROSS_ENEMY_SCOUT_SUCCESS, 6, 6);
        putMailParamNum(MailConstant.CROSS_SCOUT_SUCCESS, 6, 6);
        putMailParamNum(MailConstant.CROSS_SCOUT_FAIL, 6, 6);
        putMailParamNum(MailConstant.CROSS_ENEMY_SCOUT_FAIL, 6, 6);
        putMailParamNum(MailConstant.CROSS_DEF_PLAYER_FAIL, 6, 11);
        putMailParamNum(MailConstant.CROSS_DEF_PLAYER_SUCCESS, 6, 10);
        putMailParamNum(MailConstant.CROSS_ATK_PLAYER_FAIL, 6, 9);
        putMailParamNum(MailConstant.CROSS_ATK_PLAYER_SUCCESS, 6, 10);
        putMailParamNum(MailConstant.CROSS_HIT_FLY, 2, 2);
        putMailParamNum(MailConstant.CROSS_MOLD_ATTACK_TARGET_FLY, 2, 2);
        putMailParamNum(MailConstant.CROSS_WAR_FIRE_SAME_CAMP_COLLECT, 0, 3);
        putMailParamNum(MailConstant.CROSS_WAR_FIRE_SCORE_NOT_ENOUGH, 0, 14);
        putMailParamNum(MailConstant.CROSS_WAR_FIRE_AWARD, 0, 17);

        //王朝遗迹
        putMailParamNum(MailConstant.MOLD_HIS_REMAINS_ATTACK_FAILURE, 4, 4);
        putMailParamNum(MailConstant.MOLD_HIS_REMAINS_ATTACK_SUCCESS, 4, 4);
        putMailParamNum(MailConstant.MOLD_HIS_REMAINS_DEFEND_FAILURE, 4, 4);
        putMailParamNum(MailConstant.MOLD_HIS_REMAINS_DEFEND_SUCCESS, 4, 4);
        putMailParamNum(MailConstant.MOLD_RELIC_PROBE_OVER, 0, 1);
        putMailParamNum(MailConstant.MOLD_RELIC_PROBE_VANISH, 0, 0);
    }

    /**
     * 根据邮件id，返回邮件的标题和内容参数个数
     *
     * @param mailId
     * @return 返回参数个数
     */
    public static Turple<Integer, Integer> getMailParamNum(int mailId) {
        Turple<Integer, Integer> param = paramNumMap.get(mailId);
        if (null == param) {
            param = nonParam;
        }

        return param;
    }

}
