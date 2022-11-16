package com.gryphpoem.game.zw.service;

import com.gryphpoem.cross.constants.FightCommonConstant;
import com.gryphpoem.cross.gameplay.battle.c2g.dto.HeroFightSummary;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.dataMgr.*;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonDailyAttackTaskService;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonDailyRestrictTaskService;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.BattlePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.CommonPb.BattleRole;
import com.gryphpoem.game.zw.pb.CommonPb.RptHero;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.pb.GamePb2.SyncRoleMoveRs;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pojo.p.*;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.*;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.WarPlane;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.army.March;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.party.Camp;
import com.gryphpoem.game.zw.resource.pojo.world.*;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.resource.util.eventdata.EventDataUp;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.activity.ActivityRobinHoodService;
import com.gryphpoem.game.zw.service.activity.ActivityService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @author TanDonghai
 * @ClassName WarService.java
 * @Description 战争相关 主要定时处理所有Battle的战报
 * @date 创建时间：2017年4月12日 下午5:18:39
 */
@Service
public class WarService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private WorldDataManager worldDataManager;
    @Autowired
    private CampDataManager campDataManager;
    @Autowired
    private WarDataManager warDataManager;
    @Autowired
    private WorldService worldService;
    @Autowired
    private FightService fightService;
    @Autowired
    private HeroService heroService;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private ChatDataManager chatDataManager;
    @Autowired
    private TechDataManager techDataManager;
    @Autowired
    private BuildingDataManager buildingDataManager;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private TaskDataManager taskDataManager;
    @Autowired
    private RoyalArenaService royalArenaService;
    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private ActivityRobinHoodService activityRobinHoodService;
    @Autowired
    private WallService wallService;
    @Autowired
    private CampService campService;
    @Autowired
    private SolarTermsDataManager solarTermsDataManager;
    @Autowired
    private SuperMineService superMineService;
    @Autowired
    private HonorDailyService honorDailyService;
    @Autowired
    private HonorDailyDataManager honorDailyDataManager;
    @Autowired
    private CityService cityService;
    @Autowired
    private MedalDataManager medalDataManager;
    @Autowired
    private RebelService rebelService;
    @Autowired
    private AirshipService airshipService;
    @Autowired
    private WorldScheduleService worldScheduleService;
    @Autowired
    private WorldWarSeasonDailyRestrictTaskService restrictTaskService;
    @Autowired
    private BattlePassDataManager battlePassDataManager;
    @Autowired
    private ActivityTriggerService activityTriggerService;
    @Autowired
    private WorldWarSeasonDailyAttackTaskService dailyAttackTaskService;
    @Autowired
    private ActivityDiaoChanService activityDiaoChanService;
    @Autowired
    private SeasonTalentService seasonTalentService;
    @Autowired
    private TitleService titleService;

    /**
     * 城战、阵营战的执行定时任务
     */
    public void batlleTimeLogic() {
        Battle battle;
        int now = TimeHelper.getCurrentSecond();
        // 移除的BattleId
        Set<Integer> removeBattleIdSet = new HashSet<>();
        Iterator<Battle> its = warDataManager.getBattleMap().values().iterator();
        while (its.hasNext()) {
            battle = its.next();
            try {
                if (now >= battle.getBattleTime()) {
                    if (battle.isGestapoBattle()) {
                        gestapoFightLogic(battle, now, removeBattleIdSet);
                    } else if (battle.isCityBattle() || battle.isCampBattle()) {
                        battleFightLogic(battle, now, removeBattleIdSet);
                    } else if (battle.isAtkSuperMine()) {
                        warDataManager.removeSuperMineBattleById(battle.getBattleId());
                        superMineService.removeBattle(battle.getPos(), battle.getBattleId());
                    } else if (battle.isRebellionBattle()) {
                        its.remove();
                        rebelService.processBattleLogic(battle, now, removeBattleIdSet);
                    } else if (battle.isDecisiveBattle()) {
                        decisiveBattleFightLogic(battle, now, removeBattleIdSet);
                    } else if (battle.isMineGuardBattle()) {
                        //矿点战斗直接移除
                        warDataManager.removeBattleById(battle.getBattleId());
                    }
                    // warDataManager.removePosBattleById(battle.getPos(),
                    // battle.getBattleId());
                    // its.remove();// 城战结束，移除数据
                }
            } catch (Exception e) {
                LogUtil.error(e, "战争定时处理任务出现异常， battle:", battle);
                // 部队返回
                removeBattleIdSet.add(battle.getBattleId());
                // retreatBattleArmy(battle, now);
                // warDataManager.removePosBattleById(battle.getPos(),
                // battle.getBattleId());
                // its.remove();// 城战结束，移除数据
            }
        }

        for (Integer battleId : removeBattleIdSet) {
            battle = warDataManager.getBattleMap().get(battleId);
            if (CheckNull.isNull(battle))
                continue;

            warDataManager.removeBattleById(battleId);
            retreatBattleArmy(battle, now);// 部队没有返回的立即返回
            removePlayerJoinBattle(battle);// 移除玩家参与记录
            LogUtil.debug("移除battleId=" + battleId);
        }
    }

    /**
     * 单场盖世太保战斗逻辑,并生成战报和推送 注意要点：战前会自动补兵，按顺序生成在城内的上阵将领，驻防军的战斗数据， 战斗结束后，如果是击飞玩家或者占领城池，则该坐标点的后续战斗都取消，并返回Army。 都城NPC自动攻城处理
     *
     * @param battle
     * @param now
     * @param removeBattleIdSet
     * @throws MwException
     */
    public void gestapoFightLogic(Battle battle, int now, Set<Integer> removeBattleIdSet) throws MwException {
        if (CheckNull.isNull(battle))
            return;
        LogUtil.debug("开始战斗, battle:", battle);

        int pos = battle.getPos();
        CommonPb.RptAtkPlayer.Builder rpt = CommonPb.RptAtkPlayer.newBuilder();

        // 战斗计算
        Fighter defender = null;
        Gestapo gestapo = worldDataManager.getGestapoByPos(pos);
        if (CheckNull.isNull(gestapo)) {
            LogUtil.error("未找到盖世太保, pos:", pos);
            // 部队返回
            removeBattleIdSet.add(battle.getBattleId());
            return;
        }
        StaticGestapoPlan staticGestapoPlan = StaticWorldDataMgr.getGestapoPlanById(gestapo.getGestapoId());
        if (CheckNull.isNull(staticGestapoPlan)) {
            LogUtil.error("创建盖世太保Fighter， 未找到太保配置, pos:", pos);
            // 部队返回
            removeBattleIdSet.add(battle.getBattleId());
            return;
        }
        defender = fightService.createGestapoBattleDefencer(battle,
                null == staticGestapoPlan ? null : staticGestapoPlan.getFormList());
        Fighter attacker = fightService.createMultiPlayerFighter(battle, null);
        LogUtil.debug("defender=" + defender + ",attacker=" + attacker);
        FightLogic fightLogic = new FightLogic(attacker, defender, true, battle.getType());
        warDataManager.packForm(fightLogic.getRecordBuild(), attacker.forces, defender.forces);
        fightLogic.start();

        //貂蝉任务-杀敌阵亡数量
        ActivityDiaoChanService.killedAndDeathTask0(attacker, true, true);
        ActivityDiaoChanService.killedAndDeathTask0(defender, true, true);

        boolean atkSuccess = fightLogic.getWinState() == FightConstant.FIGHT_RESULT_SUCCESS;
        // 记录玩家有改变的资源类型, key:roleId
        Map<Long, ChangeInfo> changeMap = new HashMap<>();
        // 兵力恢复
        Map<Long, List<Award>> recoverArmyAwardMap = new HashMap<>();
        // 损兵处理
        if (attacker.lost > 0) {
            if (battle.isGestapoBattle()) {
                subBattleHeroArm(attacker.forces, changeMap, AwardFrom.CITY_BATTLE_ATTACK);
                for (BattleRole battleRole : battle.getAtkList()) {
                    long roleId = battleRole.getRoleId();
                    Player player = playerDataManager.getPlayer(roleId);
                    // 损兵排行
                    activityDataManager.updRankActivity(player, ActivityConst.ACT_ARMY_RANK, attacker.lost);
                    // 荣耀日报损兵进度
                    honorDailyDataManager.addAndCheckHonorReport2s(player, HonorDailyConstant.COND_ID_14,
                            attacker.lost);
                    // 战令的损兵进度
                    battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_SUB_HERO_ARMY, attacker.lost);
                    // 可以恢复的兵力
                    List<List<Integer>> armyAward = worldService.attckBanditLostRecvCalc(player, attacker.forces, now,
                            0, WorldConstant.LOST_RECV_CALC_GESTAPO);
                    if (!CheckNull.isEmpty(armyAward)) {
                        List<Award> awards = rewardDataManager.sendReward(player, armyAward, AwardFrom.RECOVER_ARMY);
                        recoverArmyAwardMap.put(roleId, awards);
                    }
                    // 执行勋章白衣天使特技逻辑
                    medalDataManager.angelInWhite(attacker, recoverArmyAwardMap);
                    //执行赛季天赋技能---伤病恢复
                    seasonTalentService.execSeasonTalentEffect303(attacker, recoverArmyAwardMap);
                }
            }
        }

        // 战斗记录
        int atkPosLord = battle.getAtkPos();
        String atkNick = battle.getAtkName();
        int atkVip = 0;
        int atkLevel = 0;
        int atkCamp = battle.getAtkCamp();
        int atkPortrait = 0;
        int atkPortraitFrame = 0;

        Player atkPlayer = battle.getSponsor();
        Lord atkLord = null;
        if (atkPlayer != null) {
            atkLord = atkPlayer.lord;
            atkPosLord = atkLord.getPos();
            atkNick = atkLord.getNick();
            atkVip = atkLord.getVip();
            atkLevel = atkLord.getLevel();
            atkCamp = atkLord.getCamp();
            atkPortrait = atkLord.getPortrait();
            atkPortraitFrame = atkPlayer.getDressUp().getCurPortraitFrame();
        }
        BattlePb.BattleRoundPb record = fightLogic.generateRecord();
        rpt.setNightEffect(solarTermsDataManager.getNightEffect() != null);
        rpt.setResult(atkSuccess);
        rpt.setRecord(record);
        // 记录发起进攻和防守方的信息
        rpt.setAttack(PbHelper.createRptMan(atkPosLord, atkNick, atkVip, atkLevel));
        // 记录双方汇总信息
        rpt.setAtkSum(PbHelper.createRptSummary(attacker.total, attacker.lost, atkCamp, atkNick, atkPortrait, atkPortraitFrame));
        rpt.setDefCity(PbHelper.createRptCityPb(gestapo.getGestapoId(), pos));
        rpt.setDefSum(PbHelper.createRptSummary(defender.total, defender.lost, battle.getDefCamp(), null, 0, 0));

        LinkedList<Battle> list = warDataManager.getBattlePosMap().get(battle.getPos());
        List<CommonPb.Award> awardProp = new ArrayList<>();
        List<CommonPb.Award> tmp;
        // 给将领加经验=（杀敌数+损兵数）/2，并记录双方将领信息
        if (battle.isGestapoBattle()) {
            addBattleHeroExp(attacker.forces, AwardFrom.GESTAPO_BATTLE_ATTACK, rpt, true, false, false, changeMap,
                    false, null);
            addBattleHeroExp(defender.forces, AwardFrom.GESTAPO_BATTLE_ATTACK, rpt, false, false, false, changeMap,
                    false, null);
            if (atkSuccess) {
                // 发送奖励给胜利方
                Set<Long> ids = new HashSet<>();
                for (BattleRole battleRole : battle.getAtkList()) {
                    long roleId = battleRole.getRoleId();
                    if (ids.contains(roleId))
                        continue;
                    ids.add(roleId);
                    Player player = playerDataManager.getPlayer(roleId);
                    tmp = rewardDataManager.sendReward(player, staticGestapoPlan.getAwardProp(),
                            AwardFrom.GESTAPO_ATTACK_REWARD);
                    if (tmp != null && CheckNull.isEmpty(awardProp)) {
                        awardProp.addAll(tmp);
                    }
                    int goal = staticGestapoPlan.getGoal();
                    List<Award> awards = recoverArmyAwardMap.get(roleId);
                    if (CheckNull.isEmpty(awards)) {
                        awards = new ArrayList<>();
                        recoverArmyAwardMap.put(roleId, awards);
                    }
                    awards.add(PbHelper.createAwardPb(100, 1, goal));
                    activityDataManager.updRankActivity(player, ActivityConst.ACT_GESTAPO_RANK, goal);// 个人榜
                    activityDataManager.updGlobalActivity(player, ActivityConst.ACT_ATK_GESTAPO, goal,
                            player.lord.getCamp());// 阵营榜
                }
                // 城战攻方胜利后，后续城战取消
                for (Battle battle2 : list) {
                    LogUtil.debug("移除后续城战battleId=" + battle2.getBattleId());
                    removeBattleIdSet.add(battle2.getBattleId());
                    // 目标丢失的邮件
                    if (battle.isGestapoBattle()) {
                        for (BattleRole battleRole : battle2.getAtkList()) {
                            Player player = playerDataManager.getPlayer(battleRole.getRoleId());
                            if (battle.getBattleId() == battle2.getBattleId())
                                continue;
                            if (player.lord.getCamp() == battle.getAtkCamp())
                                continue;
                            Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
                            mailDataManager.sendReportMail(player, null, MailConstant.MOLD_ATK_GESTAPO_NOT_FOUND, null,
                                    now, xy.getA(), xy.getB(), xy.getA(), xy.getB());
                        }
                    }
                }
                // 移除这个盖世太保
                worldDataManager.removeBandit(pos, 2);
                // 添加并且检测军团补给
                campService.addAndCheckPartySupply(atkPlayer, PartyConstant.SupplyType.KILL_GESTAPO,
                        staticGestapoPlan.getType());
                // 更新世界目标进度: 攻打盖世太保或流寇
                worldScheduleService.updateScheduleGoal(atkPlayer, ScheduleConstant.GOAL_COND_ATK_GESTAPO_BANDIT, 0);
            } else {
                gestapo.setStatus(WorldConstant.CITY_STATUS_CALM);
            }
            // 移除
            removeBattleIdSet.add(battle.getBattleId());

            CommonPb.Report.Builder report = worldService.createAtkPlayerReport(rpt.build(), now);
            // 发送邮件通知
            sendGestapoBattleMail(battle, staticGestapoPlan, atkSuccess, report, awardProp, now, recoverArmyAwardMap);

            // 通知周围玩家
            List<Integer> posList = new ArrayList<>();
            posList.add(gestapo.getPos());
            // 通知其他玩家数据改变
            EventBus.getDefault().post(
                    new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_AREA_AND_LINE));
        }

        // 通知客户端玩家资源变化
        sendRoleResChange(changeMap);
        // 战斗打日志
        logBattle(battle, fightLogic.getWinState(), attacker, defender, rpt.getAtkHeroList(), rpt.getDefHeroList(), String.valueOf(staticGestapoPlan.getType()));
    }

    /**
     * 发送盖世太保战斗邮件
     *
     * @param battle
     * @param staticGestapoPlan
     * @param atkSuccess
     * @param report
     * @param awardProp
     * @param now
     * @param recoverArmyAwardMap
     */
    private void sendGestapoBattleMail(Battle battle, StaticGestapoPlan staticGestapoPlan, boolean atkSuccess,
                                       CommonPb.Report.Builder report, List<Award> awardProp, int now,
                                       Map<Long, List<Award>> recoverArmyAwardMap) {

        List<BattleRole> atkList = battle.getAtkList();
        Set<Long> ids = new HashSet<>();
        for (BattleRole battleRole : atkList) {
            long roleId = battleRole.getRoleId();
            if (ids.contains(roleId))
                continue;
            Player player = playerDataManager.getPlayer(roleId);
            if (player == null)
                continue;
            ids.add(roleId);
            List<Award> recoverList = null;
            if (recoverArmyAwardMap.containsKey(roleId)) {
                recoverList = recoverArmyAwardMap.get(roleId);
            }
            // 邮件参数
            List<String> tParam = new ArrayList<>();
            tParam.add(player.lord.getNick());
            tParam.add(String.valueOf(staticGestapoPlan.getType()));
            List<String> cParam = new ArrayList<>();
            cParam.add(player.lord.getNick());
            Turple<Integer, Integer> xy = MapHelper.reducePos(player.lord.getPos());
            cParam.add(String.valueOf(xy.getA()));
            cParam.add(String.valueOf(xy.getB()));
            cParam.add(String.valueOf(staticGestapoPlan.getType()));
            xy = MapHelper.reducePos(battle.getPos());
            cParam.add(String.valueOf(xy.getA()));
            cParam.add(String.valueOf(xy.getB()));
            // 胜利方
            if (atkSuccess) {
                mailDataManager.sendReportMail(player, report, MailConstant.MOLD_ATK_GESTAPO_SUCC, awardProp, now,
                        tParam, cParam, recoverList);
            } else {
                mailDataManager.sendReportMail(player, report, MailConstant.MOLD_ATK_GESTAPO_FAIL, null, now, tParam,
                        cParam, recoverList);
            }
        }

    }

    /**
     * 决战逻辑的处理
     *
     * @param battle
     * @param now
     * @param removeBattleIdSet
     * @throws MwException
     */
    private void decisiveBattleFightLogic(Battle battle, int now, Set<Integer> removeBattleIdSet) throws MwException {
        if (null == battle) {
            return;
        }
        LogUtil.debug("--------------开始决战 battle:", battle);

        Player atkPlayer = battle.getSponsor();
        Player defencer = battle.getDefencer();

        CommonPb.RptAtkPlayer.Builder rpt = CommonPb.RptAtkPlayer.newBuilder();
        addCityDefendRoleHeros(battle);// 加上被攻击玩家的将领

        Fighter attacker = fightService.createMultiPlayerFighter(battle, null);// 攻击方
        Fighter defender = fightService.createCampBattleDefencer(battle, null);// 防守方
        FightLogic fightLogic = new FightLogic(attacker, defender, true, battle.getType());
        warDataManager.packForm(fightLogic.getRecordBuild(), attacker.forces, defender.forces);
        fightLogic.start();// 战斗逻辑处理方法

        boolean atkSuccess = fightLogic.getWinState() == FightConstant.FIGHT_RESULT_SUCCESS;

        // ----------------------------- 兵力处理 -----------------------------
        // 兵力恢复
        Map<Long, List<Award>> recoverArmyAwardMap = new HashMap<>();
        // 记录玩家有改变的资源类型, key:roleId
        Map<Long, ChangeInfo> changeMap = new HashMap<>();
        if (attacker.lost > 0) {
            subBattleHeroArm(attacker.forces, changeMap, AwardFrom.DECISIVE_BATTLE_AWARD_ATK);
        }
        if (defender.lost > 0) {
            subBattleHeroArm(defender.forces, changeMap, AwardFrom.DECISIVE_BATTLE_AWARD_DEF);
        }
        // 执行勋章白衣天使特技逻辑
        medalDataManager.angelInWhite(attacker, recoverArmyAwardMap);
        medalDataManager.angelInWhite(defender, recoverArmyAwardMap);
        //执行赛季天赋技能---伤病恢复
        seasonTalentService.execSeasonTalentEffect303(attacker, recoverArmyAwardMap);
        seasonTalentService.execSeasonTalentEffect303(defender, recoverArmyAwardMap);
        // 执行勋章-以战养战特技逻辑
        medalDataManager.sustainTheWarByMeansOfWar(attacker, defender, recoverArmyAwardMap, atkSuccess);

        // ----------------------------- 兵力处理 -----------------------------

        // 战斗记录
        BattlePb.BattleRoundPb record = fightLogic.generateRecord();
        rpt.setNightEffect(solarTermsDataManager.getNightEffect() != null);
        rpt.setResult(atkSuccess);
        rpt.setRecord(record);
        // 双方的玩家信息
        rpt.setAttack(PbHelper.createRptMan(atkPlayer.lord.getPos(), atkPlayer.lord.getNick(), atkPlayer.lord.getVip(),
                atkPlayer.lord.getLevel()));
        rpt.setDefMan(PbHelper.createRptMan(defencer.lord.getPos(), defencer.lord.getNick(), defencer.lord.getVip(),
                defencer.lord.getLevel()));
        // 双方的汇总信息
        rpt.setAtkSum(PbHelper.createRptSummary(attacker.total, attacker.lost, atkPlayer.lord.getCamp(),
                atkPlayer.lord.getNick(), atkPlayer.lord.getPortrait(), atkPlayer.getDressUp().getCurPortraitFrame()));
        rpt.setDefSum(PbHelper.createRptSummary(defender.total, defender.lost, defencer.lord.getCamp(),
                defencer.lord.getNick(), defencer.lord.getPortrait(), defencer.getDressUp().getCurPortraitFrame()));

        // 给将领加经验=（杀敌数+损兵数）/2，并记录双方将领信息, 和军工奖励
        addBattleHeroExp(attacker.forces, AwardFrom.DECISIVE_BATTLE_AWARD_ATK, rpt, true, true, true, changeMap, true,
                null);
        addBattleHeroExp(defender.forces, AwardFrom.DECISIVE_BATTLE_AWARD_DEF, rpt, false, true, true, changeMap, true,
                null);

        Turple<Integer, Integer> atkPos = MapHelper.reducePos(atkPlayer.lord.getPos());
        Turple<Integer, Integer> defPos = MapHelper.reducePos(defencer.lord.getPos());
        CommonPb.Report.Builder report = worldService.createAtkPlayerReport(rpt.build(), now);

        if (atkSuccess) {
            // 进攻方胜利，防守方玩家被击飞，重新随机坐标
            playerHitFly(battle.getDefencer(), battle.getBattleType(), atkPlayer);
            airshipService.changAirshipBelong(battle.getDefencer().roleId, atkPlayer.roleId);
            mailDataManager.sendNormalMail(battle.getDefencer(), MailConstant.MOLD_HIT_FLY,
                    TimeHelper.getCurrentSecond(), atkPlayer.lord.getNick(), atkPlayer.lord.getNick());
            // 杀敌数++
            atkPlayer.common.incrKillNum();
            DecisiveInfo decisiveInfo = defencer.getDecisiveInfo();
            if (decisiveInfo != null) {
                decisiveInfo.setFlyTime(now);
                decisiveInfo.setFlyRole(atkPlayer.roleId);
            }
            // 记录玩家被击飞
            restrictTaskService.updatePlayerDailyRestrictTask(atkPlayer, TaskType.COND_BATTLE_CITY_LV_CNT, 1);
        } else { // 进攻方失败
        }
        // 进攻方发邮件
        mailDataManager.sendReportMail(atkPlayer, report,
                atkSuccess ? MailConstant.DECISIVE_BATTLE_ATK_SUCCESS : MailConstant.DECISIVE_BATTLE_ATK_FAIL, null,
                now, recoverArmyAwardMap, atkPlayer.lord.getNick(), defencer.lord.getCamp(), defencer.lord.getLevel(),
                defencer.lord.getNick(), atkPlayer.lord.getNick(), atkPos.getA(), atkPos.getB(),
                defencer.lord.getLevel(), defencer.lord.getNick(), defPos.getA(), defPos.getB());
        // 防守方发邮件
        mailDataManager.sendReportMail(defencer, report,
                atkSuccess ? MailConstant.DECISIVE_BATTLE_DEF_FAIL : MailConstant.DECISIVE_BATTLE_DEF_SUCCESS, null,
                now, recoverArmyAwardMap, defencer.lord.getNick(), atkPlayer.lord.getCamp(), atkPlayer.lord.getLevel(),
                atkPlayer.lord.getNick(), atkPlayer.lord.getLevel(), atkPlayer.lord.getNick(), atkPos.getA(),
                atkPos.getB());

        // 移除战斗信息
        removeBattleIdSet.add(battle.getBattleId());
        // 移除决战状态
        atkPlayer.getDecisiveInfo().setDecisive(false);
        defencer.getDecisiveInfo().setDecisive(false);
        // 检测并添加荣耀日报
        honorDailyService.addAndCheckBattleHonorReports(atkPlayer, defencer, atkSuccess, battle.getType());
        retreatBattleArmy(battle, now);
        // 通知周围玩
        List<Integer> posList = new ArrayList<>();
        posList.add(atkPlayer.lord.getPos());
        posList.add(defencer.lord.getPos());
        // 通知其他玩家数据改变
        EventBus.getDefault()
                .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_AREA_TYPE));
        // 通知客户端玩家资源变化
        sendRoleResChange(changeMap);
        // 战斗打日志
        logBattle(battle, fightLogic.getWinState(), attacker, defender, rpt.getAtkHeroList(), rpt.getDefHeroList());
        LogUtil.war(">>>>>>>>>>>>>>决战逻辑执行完毕>>>>>>>>>>>>>>");
    }

    /**
     * 单场城战或阵营战战斗逻辑，并生成战报和推送 注意要点：战前会自动补兵，按顺序生成在城内的上阵将领，驻防军的战斗数据，战斗结束后，如果是击飞玩家或者占领城池，则该坐标点的后续战斗都取消，并返回Army。 都城NPC自动攻城处理
     *
     * @param battle
     * @param now
     * @param removeBattleIdSet
     * @throws MwException
     */
    public void battleFightLogic(Battle battle, int now, Set<Integer> removeBattleIdSet) throws MwException {
        if (null == battle) {
            return;
        }

        LogUtil.debug("开始战斗, battle:", battle);

        int pos = battle.getPos();
        // 如果目标正处于保护状态，部队返回
        if (battle.isCityBattle() && battle.getDefencer() != null) {
            // 部队返回
            Effect effect = battle.getDefencer().getEffect().get(EffectConstant.PROTECT);
            if (effect != null && effect.getEndTime() > now) {
                LogUtil.debug("该坐标开启保护，不能攻击battle:", battle);
                removeBattleIdSet.add(battle.getBattleId());
                return;
            }
        }

        // 如果不是怪物攻城， 而且是发起国战，没人参与打，直接结束
        // if (battle.isCampBattle() && battle.getSponsor() != null &&
        // battle.getAtkRoles().isEmpty()) {
        // removeBattleIdSet.add(battle.getBattleId());
        // StaticCity staticCity = StaticWorldDataMgr.getCityByPos(pos);
        // City city = worldDataManager.getCityById(staticCity.getCityId());
        // city.setStatus(WorldConstant.CITY_STATUS_CALM);
        // Player atkP = battle.getSponsor();
        // Turple<Integer, Integer> atkPos =
        // MapHelper.reducePos(atkP.lord.getPos());
        // Turple<Integer, Integer> defPos =
        // MapHelper.reducePos(battle.getPos());
        // int cityId = staticCity.getCityId();
        // Lord atkLord = battle.getSponsor().lord;
        // // 给进攻方发送进攻失败邮件
        // mailDataManager.sendReportMail(battle.getSponsor(), null,
        // MailConstant.MOLD_ATK_CAMP_FAIL, null, now,
        // atkLord.getNick(), cityId, atkLord.getNick(), atkPos.getA(),
        // atkPos.getB(), cityId, defPos.getA(),
        // defPos.getB());
        // // 给防守防发送防守成功邮件
        // if (battle.getDefencer() != null) {
        // mailDataManager.sendReportMail(battle.getDefencer(), null,
        // MailConstant.MOLD_DEF_CAMP_SUCC, null, now,
        // cityId, atkLord.getCamp(), atkLord.getLevel(), atkLord.getNick(),
        // atkLord.getCamp(),
        // atkLord.getLevel(), atkLord.getNick(), atkPos.getA(), atkPos.getB(),
        // cityId, defPos.getA(),
        // defPos.getB());
        // }
        // List<Integer> posList = new ArrayList<>();
        // posList.add(staticCity.getCityPos());
        // EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList,
        // 0L, 3));
        // return;
        // }

        CommonPb.RptAtkPlayer.Builder rpt = CommonPb.RptAtkPlayer.newBuilder();

        // 战斗计算
        int cityId = 0;
        City city = null;
        Fighter defender = null;
        StaticCity staticCity = StaticWorldDataMgr.getCityByPos(pos);
        // if (battle.isAtkNpc()) {
        // if (null == staticCity) {
        // LogUtil.error("创建阵营战Fighter， 未找到城池配置, pos:", pos);
        // // 部队返回
        // removeBattleIdSet.add(battle.getBattleId());
        // return;
        // }
        // cityId = staticCity.getCityId();
        // city = worldDataManager.getCityById(cityId);
        // defender = fightService.createCityNpcFighter(cityId);
        // } else {
        if (battle.isCityBattle()) {// 如果是城战，加上被攻击玩家的上阵将领
            addCityDefendRoleHeros(battle);
        } else {

            if (null == staticCity) {
                LogUtil.error("创建阵营战Fighter， 未找到城池配置, pos:", pos);
                // 部队返回
                removeBattleIdSet.add(battle.getBattleId());
                return;
            }

            cityId = staticCity.getCityId();

            // 阵营战，将城池NPC守军兵力加入防御阵型中
            city = worldDataManager.getCityById(cityId);
            if (city.getProtectTime() > now) {
                LogUtil.debug("城池保护中, 部队返回battleId=" + battle.getBattleId() + ",city=" + city);
                // 部队返回
                removeBattleIdSet.add(battle.getBattleId());
                return;
            }
        }

        defender = fightService.createCampBattleDefencer(battle, null == city ? null : city.getFormList());

        // 判断是否由都城发起的攻击
        City atkCity = worldDataManager.getCityById(battle.getAtkCity());
        // Fighter attacker = fightService.createMultiPlayerFighter(atkRoleList,
        // battle.getAtkHeroIdMap(),
        // null == atkCity ? null : getFormList4City(atkCity.getCityLv()));
        Fighter attacker = fightService.createMultiPlayerFighter(battle,
                null == atkCity ? null : getFormList4City(atkCity.getCityLv()));
        LogUtil.debug("atkCity=" + atkCity + ",attacker=" + attacker);
        FightLogic fightLogic = new FightLogic(attacker, defender, true, battle.getType());
        warDataManager.packForm(fightLogic.getRecordBuild(), attacker.forces, defender.forces);
        fightLogic.start();// 战斗逻辑处理方法

        //貂蝉任务-杀敌阵亡数量
        ActivityDiaoChanService.killedAndDeathTask0(attacker, true, true);
        ActivityDiaoChanService.killedAndDeathTask0(defender, true, true);

        // 军功显赫 <roleId, <heroId, exploit>>
        HashMap<Long, Map<Integer, Integer>> exploitAwardMap = new HashMap<>();
        // 执行勋章特技-军功显赫 逻辑
        if (battle.getType() == WorldConstant.BATTLE_TYPE_CAMP) {// 打城
            medalDataManager.militaryMeritIsProminent(attacker, defender, exploitAwardMap);
        }

        boolean atkSuccess = fightLogic.getWinState() == FightConstant.FIGHT_RESULT_SUCCESS;

        // 兵力恢复
        Map<Long, List<Award>> recoverArmyAwardMap = new HashMap<>();
        // 记录玩家有改变的资源类型, key:roleId
        Map<Long, ChangeInfo> changeMap = new HashMap<>();

        // 发起攻击方损兵处理
        if (attacker.lost > 0) {
            dailyAttackTaskService.addPlayerDailyAttackOther(attacker.forces);
            if (battle.isCityBattle()) {
                subBattleHeroArm(attacker.forces, changeMap, AwardFrom.CITY_BATTLE_ATTACK);
            } else {
                subBattleHeroArm(attacker.forces, changeMap, AwardFrom.CAMP_BATTLE_ATTACK);
            }
        }
        // 防守方损兵处理
        if (defender.lost > 0) { // 扣除战斗中的损兵
            dailyAttackTaskService.addPlayerDailyAttackOther(defender.forces);
            if (battle.isCityBattle()) {
                subBattleHeroArm(defender.forces, changeMap, AwardFrom.CITY_BATTLE_DEFEND);
            } else {
                if (battle.isAtkNpc()) {
                    if (atkSuccess) {// 打败才扣兵
                        subBattleNpcArm(defender.forces, worldDataManager.getCityById(cityId));
                    }
                } else {
                    // 先扣除npc的兵力
                    subBattleNpcArm(defender.forces, worldDataManager.getCityById(cityId));
                    subBattleHeroArm(defender.forces, changeMap, AwardFrom.CAMP_BATTLE_DEFEND);
                }
            }
        }

        // 执行勋章白衣天使特技逻辑
        medalDataManager.angelInWhite(attacker, recoverArmyAwardMap);
        medalDataManager.angelInWhite(defender, recoverArmyAwardMap);

        //执行赛季天赋技能---伤病恢复
        seasonTalentService.execSeasonTalentEffect303(attacker, recoverArmyAwardMap);
        seasonTalentService.execSeasonTalentEffect303(defender, recoverArmyAwardMap);

        // 执行勋章-以战养战特技逻辑
        medalDataManager.sustainTheWarByMeansOfWar(attacker, defender, recoverArmyAwardMap, atkSuccess);

        // 战斗记录
        int atkPosLord = battle.getAtkPos();
        String atkNick = battle.getAtkName();
        int atkVip = 0;
        int atkLevel = 0;
        int atkCamp = battle.getAtkCamp();
        int atkPortrait = 0;
        int atkPortraitFrame = 0;

        Player atkPlayer = battle.getSponsor();
        Lord atkLord = null;
        if (atkPlayer != null) {
            atkLord = atkPlayer.lord;
            atkPosLord = atkLord.getPos();
            atkNick = atkLord.getNick();
            atkVip = atkLord.getVip();
            atkLevel = atkLord.getLevel();
            atkCamp = atkLord.getCamp();
            atkPortrait = atkLord.getPortrait();
            atkPortraitFrame = atkPlayer.getDressUp().getCurPortraitFrame();
        }
        Lord defLord = null;
        if (null != battle.getDefencer()) {
            defLord = battle.getDefencer().lord;
        }
        BattlePb.BattleRoundPb record = fightLogic.generateRecord();
        rpt.setNightEffect(solarTermsDataManager.getNightEffect() != null);
        rpt.setResult(atkSuccess);
        rpt.setRecord(record);
        // 记录发起进攻和防守方的信息
        rpt.setAttack(PbHelper.createRptMan(atkPosLord, atkNick, atkVip, atkLevel));
        // 记录双方汇总信息
        rpt.setAtkSum(PbHelper.createRptSummary(attacker.total, attacker.lost, atkCamp, atkNick, atkPortrait, atkPortraitFrame));
        if (battle.isAtkNpc() || null == defLord) {
            rpt.setDefCity(PbHelper.createRptCityPb(cityId, pos));
            rpt.setDefSum(PbHelper.createRptSummary(defender.total, defender.lost, battle.getDefCamp(), null, 0, 0));
        } else {
            if (cityId > 0) {
                rpt.setDefCity(PbHelper.createRptCityPb(cityId, pos));
            }
            rpt.setDefMan(
                    PbHelper.createRptMan(defLord.getPos(), defLord.getNick(), defLord.getVip(), defLord.getLevel()));
            rpt.setDefSum(PbHelper.createRptSummary(defender.total, defender.lost, defLord.getCamp(), defLord.getNick(),
                    defLord.getPortrait(), battle.getDefencer().getDressUp().getCurPortraitFrame()));
        }

        Map<Long, FightRecord> recordMap = null;
        List<CommonPb.Award> dropList = null;
        List<CommonPb.Award> loseList = new ArrayList<>();
        Map<Long, List<Award>> dropMap = null;
        if (atkSuccess) {
            // 掉落奖励处理
            if (battle.isCityBattle()) {
                dropList = buildingDataManager.dropList4War(atkPlayer, battle.getDefencer(), loseList);// 里面包含
                // 防守方
                // 生成重建资源

                // 执行勋章-维和部队 特技逻辑
                medalDataManager.peacekeepingForces(defender, battle.getDefencer());

                // 固定给胜利方玩家角色加10点经验
                dropList.add(
                        Award.newBuilder().setType(AwardType.MONEY).setId(AwardType.Money.EXP).setCount(10).build());
                // 击飞获得战机碎片奖励
                Award award = checkPlaneChipAward(atkPlayer);
                if (!CheckNull.isNull(award)) {
                    dropList.add(award);
                }
                // 发送奖励给发起攻击的玩家
                rewardDataManager.sendRewardByAwardList(atkPlayer, dropList, AwardFrom.CITY_BATTLE_ATTACK);
                // 击杀玩家掉落活动
                List<Award> actHitDrop = activityDataManager
                        .getActHitDrop(atkPlayer, 0, StaticActBandit.ACT_HIT_DROP_TYPE_1);
                if (!CheckNull.isEmpty(actHitDrop)) {
                    dropList.addAll(actHitDrop);
                }
            }
            // 计算玩家获得的奖励内容
            recordMap = recordRoleFight(attacker.forces, true);

            // 记录攻方战斗次数 成功才记录
            recordPartyBattle(battle, battle.getType(), battle.getAtkCamp(), true);
        } else {
            recordMap = recordRoleFight(defender.forces, false);
        }

        if (!battle.isCityBattle()) {// 只有阵营战才给胜利方发资源奖励
            dropMap = sendResourceReward(recordMap, changeMap);
        }

        // 给将领加经验=（杀敌数+损兵数）/2，并记录双方将领信息, 和军工奖励
        if (battle.isCityBattle()) {
            addBattleHeroExp(attacker.forces, AwardFrom.CITY_BATTLE_ATTACK, rpt, true, true, battle.isCityBattle(),
                    changeMap, true, exploitAwardMap);
            addBattleHeroExp(defender.forces, AwardFrom.CITY_BATTLE_DEFEND, rpt, false, true, battle.isCityBattle(),
                    changeMap, true, exploitAwardMap);
        } else {
            addBattleHeroExp(attacker.forces, AwardFrom.CAMP_BATTLE_ATTACK, rpt, true, false, battle.isCityBattle(),
                    changeMap, true, exploitAwardMap);
            if (!battle.isAtkNpc()) {
                addBattleHeroExp(defender.forces, AwardFrom.CAMP_BATTLE_DEFEND, rpt, false, false,
                        battle.isCityBattle(), changeMap, true, exploitAwardMap);
            } else {
                DataResource.ac.getBean(WorldService.class).buildRptHeroData(defender, rpt, Constant.Role.CITY, true);
            }
        }

        Turple<Integer, Integer> atkPos = MapHelper.reducePos(atkPosLord);
        Turple<Integer, Integer> defPos = MapHelper.reducePos(battle.getPos());
        CommonPb.Report.Builder report = worldService.createAtkPlayerReport(rpt.build(), now);

        String firstBloodCity = LogParamConstant.NO_FIRST_KILL_CITY;
        LinkedList<Battle> list = warDataManager.getBattlePosMap().get(battle.getPos());
        if (battle.isCityBattle()) {
            if (atkSuccess) {
                // 进攻方胜利，防守方玩家被击飞，重新随机坐标
                playerHitFly(battle.getDefencer(), battle.getBattleType(), atkPlayer);
                airshipService.changAirshipBelong(battle.getDefencer().roleId, atkPlayer.roleId);
                mailDataManager.sendNormalMail(battle.getDefencer(), MailConstant.MOLD_HIT_FLY,
                        TimeHelper.getCurrentSecond(), atkPlayer.lord.getNick(), atkPlayer.lord.getNick());
                // 驻守将领返回
                wallService.retreatArmy(pos, true, battle.getDefencer());

                // 城战攻方胜利后，后续城战取消
                for (Battle battle2 : list) {
                    LogUtil.debug("移除后续城战battleId=" + battle2.getBattleId());
                    removeBattleIdSet.add(battle2.getBattleId());
                    if (battle != battle2) {
                        sendOtherAtkPlayer(battle2, now);// 城战目标被击飞需要发邮件给进攻方
                    }
                }
                taskDataManager.updTask(atkPlayer, TaskType.COND_BATTLE_CITY_LV_CNT, 1);
                battlePassDataManager.updTaskSchedule(atkPlayer.roleId, TaskType.COND_BATTLE_CITY_LV_CNT, 1);
                royalArenaService.updTaskSchedule(atkPlayer.roleId, TaskType.COND_BATTLE_CITY_LV_CNT, 1);
                if (defLord != null) {
                    activityDataManager.updDay7ActSchedule(atkPlayer, ActivityConst.ACT_TASK_ATK,
                            battle.getDefencer().building.getCommand());
                    // activityDataManager.updAtkCityActSchedule(atkPlayer,
                    // ActivityConst.ACT_TASK_ATK, // 击飞玩家
                    // battle.getDefencer().building.getCommand());
                    for (long roles : battle.getAtkRoles()) {
                        Player actPlayer = playerDataManager.getPlayer(roles);
                        if (actPlayer != null) {
                            activityDataManager.updDay7ActSchedule(actPlayer, ActivityConst.ACT_TASK_JOIN_OR_ATK,
                                    battle.getDefencer().building.getCommand());
                            activityDataManager.updAtkCityActSchedule(actPlayer, ActivityConst.ACT_TASK_JOIN_OR_ATK, // 参与击飞
                                    battle.getDefencer().building.getCommand());
                            // 记录玩家被击飞
                            restrictTaskService.updatePlayerDailyRestrictTask(actPlayer, TaskType.COND_BATTLE_CITY_LV_CNT, 1);
                        }
                    }
                }
                // 杀敌数++
                atkPlayer.common.incrKillNum();
                // 活动排行榜
                activityDataManager.updRankActivity(atkPlayer, ActivityConst.ACT_CITY_BATTLE_RANK, 1);
            } else {
                // 移除城战信息, 但不能移除驻防部队
                removeBattleIdSet.add(battle.getBattleId());
                // 驻守将领没兵返回
                wallService.retreatArmy(pos, false, battle.getDefencer());
            }

            int defLv = battle.getDefencer() == null ? 1 : battle.getDefencer().lord.getLevel();

            taskDataManager.updTask(atkPlayer, TaskType.COND_ATTCK_PLAYER_CNT, 1, defLv);
            royalArenaService.updTaskSchedule(atkPlayer.roleId, TaskType.COND_ATTCK_PLAYER_CNT, 1);
            // 检测并添加荣耀日报
            honorDailyService.addAndCheckHonorReports(atkPlayer, battle.getDefencer(), atkSuccess, battle.getType());

            // 不论胜负
            battle.getAtkList().stream().mapToLong(role -> role.getRoleId()).distinct().forEach(roleId -> {
                Player actPlayer = playerDataManager.getPlayer(roleId);
                activityDataManager.updAtkCityActSchedule(actPlayer, ActivityConst.ACT_TASK_ATK_AND_JOIN, // 参与攻打玩家
                        battle.getDefencer().building.getCommand());
            });
            battle.getDefList().stream().mapToLong(role -> role.getRoleId()).distinct().forEach(roleId -> {
                Player defPalyer = playerDataManager.getPlayer(roleId);
                if (!battle.getDefencer().roleId.equals(defPalyer.roleId)) {
                    activityDataManager.updAtkCityActSchedule(defPalyer, ActivityConst.ACT_TASK_JOIN_OR_DEF, // 参与防守玩家
                            battle.getDefencer().building.getCommand());
                }
            });
            // 应用外推送, 正在被攻击
            PushMessageUtil.pushMessage(battle.getDefencer().account, PushConstant.ATTACKED_AND_BEATEN);
        } else {
            if (null == city) {
                LogUtil.error("更新城池阵营，城池信息未初始化, cityId:", cityId);
            } else {
                if (atkSuccess) {
                    // 阵营战，攻方胜利后设置城池归属
                    int preCityCamp = city.getCamp();
                    city.setCamp(battle.getAtkCamp());
                    city.cleanOwner(true);
                    LogUtil.debug("更新城池阵营，城池变更, cityId:", cityId, "camp:", battle.getAtkCamp());
                    if (staticCity.getType() == WorldConstant.CITY_TYPE_KING) {// 皇城
                    } else if (staticCity.getType() == WorldConstant.CITY_TYPE_HOME) {// 都城
                        // 都城4小时自动发起怪物攻城
                        city.setNextAtkBeginTime(now);
                        city.setStatus(WorldConstant.CITY_STATUS_CALM);
                        worldDataManager.addCampSuperMine(city.getCamp());
                    } else {// 其他城
                        // 名城最多只能占领7个
                        List<Long> joinRoles = battle.getAtkList().stream().map(r -> r.getRoleId()).distinct()
                                .collect(Collectors.toList());// 只有参加战斗的人
                        if (staticCity.getType() == WorldConstant.CITY_TYPE_8 && worldDataManager
                                .getPeoPle4MiddleCity(battle.getAtkCamp()) > WorldConstant.CITY_TYPE_8_MAX) {
                            // 设置成npc并且加上罩子
                            city.setCamp(Constant.Camp.NPC);
                            city.setStatus(WorldConstant.CITY_STATUS_FREE);
                            city.setProtectTime(now + WorldConstant.CITY_PROTECT_TIME * TimeHelper.MINUTE);
                        } else {
                            // 开启竞选
                            city.setStatus(WorldConstant.CITY_STATUS_FREE);
                            city.setCloseTime(now + WorldConstant.CITY_FREE_TIME);
                            city.startCampaign(now);
                            city.getAttackRoleId().addAll(joinRoles);
                            city.setProtectTime(now + WorldConstant.CITY_PROTECT_TIME * TimeHelper.MINUTE);
                        }
                        // 首杀奖励
                        if (preCityCamp == Constant.Camp.NPC) {
                            final City cityF = city;
                            joinRoles.forEach(roleId -> {
                                Player player = playerDataManager.getPlayer(roleId);
                                // npc城池首杀奖励，每个满足条件的城池只能生效一次
                                if (player != null && player.checkNpcFirstKillReward(staticCity.getCityId())) {
                                    cityF.getFirstKillReward().put(roleId, 0);
                                    player.addNpcFirstKillRecord(staticCity.getCityId());
                                }
                            });
                        } else {
                            // 阵营更换清空首杀奖励
                            city.getFirstKillReward().clear();
                        }
                        // 首杀奖励
                        firstBloodCity = checkUpdAreafirstKill(staticCity, city, battle.getAtkLordId(), joinRoles) ? LogParamConstant.IS_FIRST_KILL_CITY : LogParamConstant.IS_NOT_FIRST_KILL_CITY;
                        // 打城 荣耀日报更新
                        honorDailyService.addAndCheckHonorReport2s(battle.getSponsor(), HonorDailyConstant.COND_ID_2);
                    }
                    if (Constant.START_SOLAR_TERMS_CITY_TYPE == staticCity.getType()) {
                        // 判断开启节气
                        if (!solarTermsDataManager.isSolarTermsBegin()) {
                            solarTermsDataManager.setSolarTermsBeginTime(now);
                        }
                    }
                    // 阵营战攻方胜利后，该城的后续阵营战取消
                    for (Battle battle2 : list) {
                        LogUtil.debug("移除后续国战battleId=" + battle2.getBattleId() + ",list=" + list.size());
                        removeBattleIdSet.add(battle2.getBattleId());
                    }

                    // 推送城池变更聊天消息,发给攻防和防守方
                    if (!CheckNull.isNullTrim(atkNick)) {
                        chatDataManager.sendSysChat(ChatConst.CHAT_CITY_OCCUPIED, battle.getAtkCamp(), 1,
                                city.getFinishTime(), battle.getDefCamp(), cityId, defPos.getA(), defPos.getB(),
                                battle.getAtkCamp(), atkNick);
                        chatDataManager.sendSysChat(ChatConst.CHAT_CITY_OCCUPIED, battle.getDefCamp(), 1,
                                city.getFinishTime(), battle.getDefCamp(), cityId, defPos.getA(), defPos.getB(),
                                battle.getAtkCamp(), atkNick);
                    } else {
                        chatDataManager.sendSysChat(ChatConst.CHAT_CITY_NPC_OCCUPIED, battle.getAtkCamp(), 1,
                                city.getFinishTime(), battle.getDefCamp(), cityId, defPos.getA(), defPos.getB(),
                                battle.getAtkCamp());
                        chatDataManager.sendSysChat(ChatConst.CHAT_CITY_NPC_OCCUPIED, battle.getDefCamp(), 1,
                                city.getFinishTime(), battle.getDefCamp(), cityId, defPos.getA(), defPos.getB(),
                                battle.getAtkCamp());
                    }

                    // 记录军团日志
                    Turple<Integer, Integer> xy = staticCity.getCityPosXy();
                    // 进攻方占领日志
                    PartyLogHelper.addPartyLog(battle.getAtkCamp(), PartyConstant.LOG_CITY_CONQUERED, city.getCityId(),
                            xy.getA(), xy.getB(), atkNick);

                    if (!battle.isAtkNpc()) {// 防守方城池被攻破日志
                        PartyLogHelper.addPartyLog(battle.getDefCamp(), PartyConstant.LOG_CITY_BREACHED,
                                city.getCityId(), xy.getA(), xy.getB(), battle.getAtkCamp(), atkNick);
                    }

                    // 2019-3-9 不需要更新老的世界任务去掉
                    // 开启世界任务
                    // if (atkPlayer != null &&
                    // taskDataManager.isOpenWorldTask(atkPlayer) &&
                    // StaticWorldDataMgr.getWorldTask(worldDataManager.getWorldTask().getWorldTaskId().get()
                    // + 1)
                    // != null) {
                    // taskDataManager.updWorldTask(TaskType.WORLD_TASK_TYPE_CITY,
                    // 1, staticCity.getType(),
                    // atkPlayer.lord.getCamp());
                    // }

                    // 完成活动
                    // 只给参与的人完成任务
                    battle.getAtkList().stream().mapToLong(BattleRole::getRoleId).distinct().forEach(roleId -> {
                        Player actPlayer = playerDataManager.getPlayer(roleId);
                        // 更新参与者军团任务
                        taskDataManager.updTask(actPlayer, TaskType.COND_BATTLE_STATE_LV_CNT, 1);
                        royalArenaService.updTaskSchedule(actPlayer.roleId, TaskType.COND_BATTLE_STATE_LV_CNT, 1);
                        battlePassDataManager.updTaskSchedule(actPlayer.roleId, TaskType.COND_BATTLE_STATE_LV_CNT, 1, staticCity.getType());
                        restrictTaskService.updatePlayerDailyRestrictTask(actPlayer, TaskType.COND_BATTLE_STATE_LV_CNT, 1);
                        activityDataManager.updActivity(actPlayer, ActivityConst.ACT_ATTACK_CITY, 1,
                                staticCity.getType(), true);
                        activityDataManager.updRankActivity(actPlayer, ActivityConst.ACT_CAMP_BATTLE_RANK, 1);
                        activityDataManager.updDay7ActSchedule(actPlayer, ActivityConst.ACT_TASK_ATTACK,
                                staticCity.getType());
                        activityRobinHoodService.updateTaskSchedule(actPlayer, ActivityConst.ACT_TASK_ATTACK, 1, staticCity.getType());
                        activityDataManager.updDay7ActSchedule(actPlayer, ActivityConst.ACT_TASK_CITY);
                        if (preCityCamp != Constant.Camp.NPC && preCityCamp != actPlayer.lord.getCamp()) {
                            activityDataManager.updAtkCityActSchedule(actPlayer,
                                    ActivityConst.ACT_TASK_JOIN_ATK_OTHER_CITY);
                        }
                        activityDataManager.updAtkCityActSchedule(actPlayer, ActivityConst.ACT_TASK_ATTACK,
                                staticCity.getType());// 参与并攻陷
                        activityDataManager.updAtkCityActSchedule(actPlayer, ActivityConst.ACT_TASK_CITY);// 当前战区据点增加
                        LogUtil.debug("参与攻下几座指定类型的城， 开始活动=" + actPlayer.day7Act.getTankTypes() + ",key="
                                + (ActivityConst.ACT_TASK_ATTACK + "_" + staticCity.getType()));

                        //貂蝉任务-参加国战，成功占领
                        ActivityDiaoChanService.completeTask(actPlayer, ETask.JOIN_CITY_WAR, staticCity.getType());
                        TaskService.processTask(actPlayer, ETask.JOIN_CITY_WAR, staticCity.getType());
                    });
                    // 阵营city推送
                    cityService.syncPartyCity(city, staticCity);
                    // 添加并且检测军团补给
                    campService.addAndCheckPartySupply(atkPlayer, PartyConstant.SupplyType.CONQUER_CITY,
                            staticCity.getType());
                    // 更新世界目标进度
                    worldScheduleService.updateScheduleGoal(atkPlayer, ScheduleConstant.GOAL_COND_CONQUER_CITY,
                            staticCity.getCityId());
                } else {
                    // 进攻方失败，移除
                    removeBattleIdSet.add(battle.getBattleId());
                    // 修改城池状态
                    LinkedList<Battle> cityBattleList = warDataManager.getBattlePosMap().get(staticCity.getCityPos());
                    if (!CheckNull.isEmpty(cityBattleList)) {
                        if (cityBattleList.size() == 1) { // 当前城池只有一场战斗,才会改成空闲状态
                            city.setStatus(WorldConstant.CITY_STATUS_CALM);
                        }
                    } else {
                        // 这个else 只是为了容错的修改城池状态
                        city.setStatus(WorldConstant.CITY_STATUS_CALM);
                    }

                    // 如果后面还有其他阵营战的队列，更新进攻阵营
                    if (list.size() > 1) {
                        Battle battle2 = list.get(1);
                        city.setAttackCamp(battle2.getAtkCamp());
                    }
                    // 聊天通知防守方城池需要修复
                    chatDataManager.sendSysChat(ChatConst.CHAT_CITY_DEMAGED, battle.getDefCamp(), 0, cityId);
                }

                // 进攻方防守方活动更新
                battle.getAtkList().stream().mapToLong(BattleRole::getRoleId).distinct().forEach(roleId -> {
                    Player player = playerDataManager.getPlayer(roleId);
                    if (player != null) {
                        activityDataManager.updActivity(player, ActivityConst.ACT_ATTACK_CITY, 1, 0, true);
                        activityDataManager.updAtkCityActSchedule(player, ActivityConst.ACT_TASK_JOIN_ATK,
                                staticCity.getType()); // 参与攻打据点
                        activityRobinHoodService.updateTaskSchedule(player, ActivityConst.ACT_TASK_JOIN_ATK, 1, staticCity.getType());
                        activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_JOIN_ATK, staticCity.getType());
                        taskDataManager.updTask(player, TaskType.COND_JOIN_CAMP_BATTLE_41, 1, atkSuccess ? 1 : 0);
                        battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_JOIN_CAMP_BATTLE_41, 1, staticCity.getType());
                        royalArenaService.updTaskSchedule(player.roleId, TaskType.COND_JOIN_CAMP_BATTLE_41, 1, staticCity.getType());
                        if (staticCity != null) {
                            taskDataManager.updTask(player, TaskType.COND_22, 1, staticCity.getType());
                            taskDataManager.updTask(player, TaskType.COND_520, attacker.getForces().stream().filter(e -> e.ownerId == roleId).mapToInt(e -> e.killed).sum());
                            taskDataManager.updTask(player, TaskType.COND_521, 1, staticCity.getType());
                            //支线任务
                            taskDataManager.updTask(player, TaskType.COND_996, 1);
                        }
                    }
                });
                battle.getDefList().stream().mapToLong(BattleRole::getRoleId).distinct().forEach(roleId -> {
                    Player player = playerDataManager.getPlayer(roleId);
                    if (player != null) {
                        activityDataManager.updActivity(player, ActivityConst.ACT_ATTACK_CITY, 1, 0, true);
                        activityDataManager.updAtkCityActSchedule(player, ActivityConst.ACT_TASK_JOIN_DEF,
                                staticCity.getType()); // 参与防守据点
                        activityRobinHoodService.updateTaskSchedule(player, ActivityConst.ACT_TASK_JOIN_ATK, 1, staticCity.getType());
                        activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_JOIN_ATK, staticCity.getType());
                        taskDataManager.updTask(player, TaskType.COND_JOIN_CAMP_BATTLE_41, 1, atkSuccess ? 0 : 1);
                        battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_JOIN_CAMP_BATTLE_41, 1, staticCity.getType());
                        royalArenaService.updTaskSchedule(player.roleId, TaskType.COND_JOIN_CAMP_BATTLE_41, 1, staticCity.getType());
                        taskDataManager.updTask(player, TaskType.COND_520, attacker.getForces().stream().filter(e -> e.ownerId == roleId).mapToInt(e -> e.killed).sum());
                        taskDataManager.updTask(player, TaskType.COND_521, 1, staticCity.getType());
                    }
                });

                // 通知周围玩家
                List<Integer> posList = new ArrayList<>();
                posList.add(staticCity.getCityPos());
                // 通知其他玩家数据改变
                EventBus.getDefault().post(
                        new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));
            }

        }
        // 军团礼包消息推送
        Optional.of(campDataManager.getParty(battle.getAtkCamp()))
                .ifPresent((party) -> campService.checkHonorRewardAndSendSysChat(party));

        // 发送邮件通知
        if (battle.isCityBattle()) {// 城战，只有被进攻玩家会损失，发起进攻的玩家会获得奖励
            sendCityBattleMail(battle, cityId, atkLord, defLord, atkPos, defPos, atkSuccess, report, dropList, loseList,
                    now, recoverArmyAwardMap);
            // 防守方触发自动补兵
            autoFillArmy(battle.getDefencer());
        } else {// 阵营战，不论进攻防守，胜利失败，双方都有奖励
            sendCampBattleMail(battle, cityId, atkLord, defLord, atkPos, defPos, atkSuccess, report, dropMap, now,
                    recoverArmyAwardMap);
        }

        // 通知客户端玩家资源变化
        sendRoleResChange(changeMap);
        // 战斗打日志
        logBattle(battle, fightLogic.getWinState(), attacker, defender, rpt.getAtkHeroList(), rpt.getDefHeroList(), String.valueOf(cityId), firstBloodCity);
    }

    /**
     * 检测击飞的战机碎片奖励
     *
     * @param player
     */
    public Award checkPlaneChipAward(Player player) throws MwException {
        Award award = null;
        if (CheckNull.isNull(player)) {
            return award;
        }
        final boolean[] flag = new boolean[1];
        // 所有上阵将领
        List<Hero> battleHeros = player.getAllOnBattleHeros();
        if (CheckNull.isEmpty(battleHeros)) {
            return award;
        }
        // 所有上阵战机并出战的战机
        List<Integer> battlePlane = battleHeros.stream()
                .filter(hero -> hero.getState() == ArmyConstant.ARMY_STATE_BATTLE)
                .flatMap(hero -> hero.getWarPlanes().stream()).collect(Collectors.toList());
        if (CheckNull.isEmpty(battlePlane)) {
            return award;
        }
        // 过滤已经满配的战机
        battlePlane = battlePlane.stream().filter(planeId -> {
            try {
                flag[0] = true;
                WarPlane plane = player.checkWarPlaneIsExist(planeId);
                if (!CheckNull.isNull(plane)) {
                    StaticPlaneUpgrade maxLv = StaticWarPlaneDataMgr
                            .getPlaneMaxLvByFilter(e -> plane.getType() == e.getPlaneType() && e.getNextId() == 0
                                    && CheckNull.isEmpty(e.getReformNeed()));
                    if (!CheckNull.isNull(maxLv) && maxLv.getPlaneId() == plane.getPlaneId()) { // 当前战机为最大品质等级
                        flag[0] = false;
                    }
                }
            } catch (MwException e) {
                LogUtil.error(e);
            }
            return flag[0];
        }).collect(Collectors.toList());
        if (CheckNull.isEmpty(battlePlane)) {
            return award;
        }
        int cnt = player.getMixtureDataById(PlayerConstant.PLANE_HIT_FLY_AWARD_CNT);
        if (cnt >= PlaneConstant.PLANE_HIT_FLY_AWARD.get(1)) { // 大于每日击飞玩家可获得碎片次数
            return award;
        }
        if (RandomHelper.isHitRangeIn10000(PlaneConstant.PLANE_HIT_FLY_AWARD.get(0))) {
            Collections.shuffle(battlePlane); // 打乱顺序
            int planeId = battlePlane.get(0);
            WarPlane warPlane = player.checkWarPlaneIsExist(planeId);
            if (!CheckNull.isNull(warPlane)) {
                StaticPlaneInit sPlaneInit = StaticWarPlaneDataMgr.getPlaneInitByType(warPlane.getType());
                if (!CheckNull.isNull(sPlaneInit)) {
                    List<Integer> synthesis = sPlaneInit.getSynthesis(); // 合成需要
                    if (!CheckNull.isEmpty(synthesis)) {
                        player.setMixtureData(PlayerConstant.PLANE_HIT_FLY_AWARD_CNT, cnt + 1);
                        award = (Award.newBuilder().setType(AwardType.PLANE_CHIP).setId(synthesis.get(1)).setCount(1)
                                .build());
                    }
                }
            }
        }
        return award;
    }

    /**
     * 检查是否是城市首杀,并发送奖励
     *
     * @param staticCity
     * @param city
     * @param sponsorId
     * @param joinRoles
     */
    public boolean checkUpdAreafirstKill(StaticCity staticCity, City city, long sponsorId, List<Long> joinRoles)
            throws MwException {
        List<Long> atkList = joinRoles.stream().filter(roleId -> !roleId.equals(sponsorId))
                .collect(Collectors.toList());
        if (sponsorId == 0L) {
            return false;
        }
        Player sponsor = playerDataManager.checkPlayerIsExist(sponsorId);
        ArrayList<Long> sponsorList = new ArrayList<>();
        Area area = worldDataManager.getAreaByAreaId(staticCity.getArea());
        int cityType = staticCity.getType();

        Area alreadyFiKill = worldDataManager.getAreaMap().values().stream().filter(a -> a.isInKillList(cityType)).findFirst().orElse(null);
        if (alreadyFiKill == null) {
            // 说明全世界首次击破城池的类型
            activityTriggerService.battleCampTriggerGift(staticCity.getType());
        }

        // 当前区域没有首杀
        if (!area.isInKillList(cityType)) {
            List<List<Integer>> confAwd = Constant.CITY_TYPE_KILL_REWARD.get(cityType);
            if (CheckNull.isEmpty(confAwd)) {
                LogUtil.error("城池首杀奖励未配置: cityType" + cityType);
                return false;
            }

            List<Award> awards = new ArrayList<>(PbHelper.createAwardsPb(confAwd));
            // 发奖励邮件
            if (!awards.isEmpty()) {
                // 在所有Area中领过首杀奖励的人
                List<Long> alreadyAward = worldDataManager.getAreaMap().values().stream()
                        .filter(a -> !CheckNull.isEmpty(a.getCityFirstKill()))
                        .flatMap(a -> a.getCityFirstKill().entrySet().stream()
                                // 过滤城池类型, 并且首杀记录不为空的
                                .filter(en -> Integer.valueOf(en.getKey().split("_")[0]) == cityType && !CheckNull.isEmpty(en.getValue()))
                                // 获取参与者的记录
                                .map(en -> en.getValue().get(WorldConstant.KILL_ATKLIST))
                                .flatMap(Collection::stream))
                        .distinct().collect(Collectors.toList());


                // 给参与人员发送奖励
                joinRoles.forEach(roleId -> {
                    if (alreadyAward.contains(roleId)) { // 已经领到首杀奖励的人, 不再发送奖励
                        return;
                    }
                    Player player = playerDataManager.getPlayer(roleId);
                    mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_FIRST_KILL_REWARD,
                            AwardFrom.CITY_FIRST_KILL_AWARD, TimeHelper.getCurrentSecond(), city.getCityId(),
                            city.getCityId());
                });
            }
            // 首杀跑马灯
            chatDataManager.sendSysChat(ChatConst.CHAT_CITY_FIRST_KILL, sponsor.lord.getCamp(), 0,
                    sponsor.lord.getCamp(), sponsor.lord.getNick(), staticCity.getCityId(), staticCity.getArea(),
                    cityType);

            // 发完奖励后, 再添加数据
            sponsorList.add(sponsorId);
            HashMap<String, List<Long>> killMap = new HashMap<>();
            killMap.put(WorldConstant.KILL_SPONSOR, sponsorList);
            // 这里不过滤发起者
            killMap.put(WorldConstant.KILL_ATKLIST, joinRoles);
            String cityInfo = cityType + "_" + staticCity.getCityId();
            area.getCityFirstKill().put(cityInfo, killMap);

            //貂蝉任务-城池首杀
            joinRoles.forEach(roleId -> {
                Player player = playerDataManager.getPlayer(roleId);
                ActivityDiaoChanService.completeTask(player, ETask.CITY_FIRSTKILLED);
                TaskService.processTask(player, ETask.CITY_FIRSTKILLED);
                //称号-城池首杀
                titleService.processTask(player, ETask.CITY_FIRSTKILLED);
            });

            return true;
        }

        return false;
    }

    /**
     * 打印战斗日志
     *
     * @param battle
     * @param winState 胜利的状态
     */
    public void logBattle(Battle battle, int winState, Fighter attacker, Fighter defender, List<RptHero> atkHeroList, List<RptHero> defHeroList, Object... param) {
        String battleId = battle.getBattleId() + "_" + battle.getBattleTime();
        String type = String.valueOf(battle.getType());
        String win = String.valueOf(winState);
        String pos = String.valueOf(battle.getPos());
        String sponsorId = String
                .valueOf(battle.getSponsor() != null ? battle.getSponsor().roleId : battle.getSponsorId());
        String defencerId = String
                .valueOf(battle.getDefencer() != null ? battle.getDefencer().roleId : battle.getDefencerId());
        String atkCamp = String.valueOf(battle.getAtkCamp());
        // 攻击方玩家打日志
        battle.getAtkList().stream().map(rb -> rb.getRoleId()).distinct().map(rId -> playerDataManager.getPlayer(rId))
                .filter(p -> p != null).forEach(player -> {
                    LogLordHelper.otherLog("battle", player.account.getServerId(), player.roleId, "atk", battleId, type,
                            win, pos, sponsorId, defencerId, atkCamp);
                    //上报数数
                    EventDataUp.battle(player.account, player.lord, attacker, "atk", battleId, type, win, Long.parseLong(sponsorId), atkHeroList, param);
                });

        // 防守方玩家打日志
        battle.getDefList().stream().map(rb -> rb.getRoleId()).distinct().map(rId -> playerDataManager.getPlayer(rId))
                .filter(p -> p != null).forEach(player -> {
                    LogLordHelper.otherLog("battle", player.account.getServerId(), player.roleId, "def", battleId, type,
                            win, pos, sponsorId, defencerId, atkCamp);
                    //上报数数
                    if (defender != null) {
                        EventDataUp.battle(player.account, player.lord, defender, "def", battleId, type, win, Long.parseLong(sponsorId), defHeroList, param);
                    }
                });

    }

    /**
     * 自动补兵,城墙和上阵将领自动补兵
     *
     * @param player
     */
    public void autoFillArmy(Player player) {
        if (player == null)
            return;
        try {
            playerDataManager.autoAddArmy(player);
            wallService.processAutoAddArmy(player);
        } catch (Exception e) {
            LogUtil.error("自动补兵出错 roleId:", player.roleId, e);
        }
    }

    /**
     * 城战目标被击飞需要发邮件给其他进攻方
     *
     * @param battle
     * @param now
     */
    private void sendOtherAtkPlayer(Battle battle, int now) {
        if (battle.isCityBattle()) {
            Player defencer = battle.getDefencer();
            if (defencer != null) {
                for (Long roleId : battle.getAtkRoles()) {
                    Player player = playerDataManager.getPlayer(roleId);
                    if (player == null)
                        continue;
                    mailDataManager.sendNormalMail(player, MailConstant.MOLD_ATTACK_TARGET_FLY, now,
                            defencer.lord.getNick(), defencer.lord.getNick());
                }
            }
        }
    }

    /**
     * 获取都城攻击部队
     *
     * @param devLv
     * @return
     */
    @Deprecated
    private List<CityHero> getFormList4City(int devLv) {
        devLv = devLv > 0 ? devLv : 1;
        StaticCityDev cityDev = StaticWorldDataMgr.getCityDev(devLv);
        CityHero hero;
        StaticNpc npc;
        List<CityHero> formList = cityDev.getFormList();
        if (formList != null) {
            return formList;
        } else {
            formList = new ArrayList<>();
        }
        for (Integer npcId : cityDev.getForm()) {
            npc = StaticNpcDataMgr.getNpcMap().get(npcId);
            hero = new CityHero(npcId, npc.getTotalArm());
            formList.add(hero);
        }
        cityDev.setFormList(formList);
        return formList;
    }

    public void recordPartyBattle(Battle battle, int battleType, int camp, boolean isAtk) {
        if (camp == 0) {
            return;
        }
        Camp party;
        CampMember member;
        party = campDataManager.getParty(camp);
        if (battleType == WorldConstant.BATTLE_TYPE_CAMP) {
            party.campBattle();
        } else {
            party.cityBattle();
        }
        List<BattleRole> list = isAtk ? battle.getAtkList() : battle.getDefList();
        int now = TimeHelper.getCurrentSecond();
//        for (BattleRole battleRole : list) {
//            long roleId = battleRole.getRoleId();
//            Player player = playerDataManager.getPlayer(roleId);
//            member = campDataManager.getCampMember(player.roleId);
//            // 阵营战会+1在荣耀排行中,营地战不会
//            if (battleType == WorldConstant.BATTLE_TYPE_CAMP) {
//                member.campBattle();
//                party.addPartyHonorRank(PartyConstant.RANK_TYPE_CAMP, player.roleId, player.lord.getNick(),
//                        member.getCampBattle(), now);
//            }
//        }
        //fix 派多个将领打同一个城 国战排行榜计算多次
        list.stream()
                .map(BattleRole::getRoleId)
                // 去重, 避免一个玩家派出多个队列进攻, 增加多次国战次数记录
                .distinct()
                .map(playerDataManager::getPlayer)
                .forEach(p -> {
                    CampMember campMember = campDataManager.getCampMember(p.roleId);
                    // 阵营战会+1在荣耀排行中,营地战不会
                    if (battleType == WorldConstant.BATTLE_TYPE_CAMP) {
                        campMember.campBattle();
                        party.addPartyHonorRank(PartyConstant.RANK_TYPE_CAMP, p.roleId, p.lord.getNick(),
                                campMember.getCampBattle(), now);
                    }
                });
        // 营地战,只会给发起者加到荣耀排行中
        if (battleType == WorldConstant.BATTLE_TYPE_CITY) {
            Player player = battle.getSponsor();
            member = campDataManager.getCampMember(player.roleId);
            if (member != null) {
                member.cityBattle();
                party.addPartyHonorRank(PartyConstant.RANK_TYPE_CITY, player.roleId, player.lord.getNick(),
                        member.getCityBattle(), now);
            }
        }
    }

    /**
     * 通知客户端玩家资源变化
     *
     * @param changeMap
     */
    public void sendRoleResChange(Map<Long, ChangeInfo> changeMap) {
        Player player;
        for (Entry<Long, ChangeInfo> entry : changeMap.entrySet()) {
            player = playerDataManager.getPlayer(entry.getKey());
            rewardDataManager.syncRoleResChanged(player, entry.getValue());
        }
    }

    /**
     * 根据玩家参战将领，统计玩家战斗数据
     *
     * @param forces
     * @param attacker
     * @return
     */
    public Map<Long, FightRecord> recordRoleFight(List<Force> forces, boolean attacker) {
        Map<Long, FightRecord> map = new HashMap<>();
        for (Force force : forces) {
            addRoleFightRecord(map, attacker, force.ownerId, force.killed, force.totalLost, 0);
        }
        return map;
    }

    private void addRoleFightRecord(Map<Long, FightRecord> recordMap, boolean attacker, long roleId, int killed,
                                    int lost, int exploit) {
        FightRecord record = recordMap.get(roleId);
        if (null == record) {
            record = new FightRecord(roleId);
            recordMap.put(roleId, record);
        }
        record.addLost(lost);
        record.addKilled(killed);
        record.addExploit(exploit);
        record.setAttacker(attacker);
    }

    /**
     * 发送资源相关的奖励
     *
     * @param recordMap
     * @param changeMap
     * @return 返回阵营战，获得奖励玩家具体的奖励信息
     */
    public Map<Long, List<Award>> sendResourceReward(Map<Long, FightRecord> recordMap,
                                                     Map<Long, ChangeInfo> changeMap) {
        Map<Long, List<Award>> campDropMap = new HashMap<>();
        Player player;
        ChangeInfo info;
        List<Award> dropList;
        for (FightRecord record : recordMap.values()) {
            player = playerDataManager.getPlayer(record.getRoleId());
            if (player == null) {
                continue;
            }
            if (!record.isAttacker())
                continue; // 防守方不不管胜利失败不会给油和电奖励
            info = changeMap.get(record.getRoleId());
            if (null == info) {
                info = ChangeInfo.newIns();
                changeMap.put(record.getRoleId(), info);
            }

            // 未杀敌未损兵，无资源奖励；有杀敌有损兵，资源奖励暂定为，银币（燃油）=（杀敌数+损兵数）*2K，木材（电力）=（杀敌数+损兵数）*1.5K
            // 改为只要参与了就有奖励
            // if (record.getKilled() > 0 && record.getLost() > 0) {
            dropList = new ArrayList<>(2);
            campDropMap.put(record.getRoleId(), dropList);
            int oil = (int) (20000 + (record.getKilled() + record.getLost()) * 0.5);
            int elec = (int) (12000 + (record.getKilled() + record.getLost()) * 0.3);
            rewardDataManager.addAward(player, AwardType.RESOURCE, AwardType.Resource.OIL, oil,
                    AwardFrom.CAMP_BATTLE_ATTACK);
            rewardDataManager.addAward(player, AwardType.RESOURCE, AwardType.Resource.ELE, elec,
                    AwardFrom.CAMP_BATTLE_ATTACK);
            // if (record.isAttacker()) {
            // } else {
            // rewardDataManager.addAward(player, AwardType.RESOURCE,
            // AwardType.Resource.OIL, oil,
            // AwardFrom.CAMP_BATTLE_DEFEND);
            // rewardDataManager.addAward(player, AwardType.RESOURCE,
            // AwardType.Resource.ELE, elec,
            // AwardFrom.CAMP_BATTLE_DEFEND);
            // }
            dropList.add(PbHelper.createAwardPb(AwardType.RESOURCE, AwardType.Resource.OIL, oil));
            dropList.add(PbHelper.createAwardPb(AwardType.RESOURCE, AwardType.Resource.ELE, elec));
            // }

            info.addChangeType(AwardType.RESOURCE, AwardType.Resource.OIL);
            info.addChangeType(AwardType.RESOURCE, AwardType.Resource.ELE);
        }
        return campDropMap;
    }

    /**
     * 移除战斗中所有参与玩家， 若坐标位置有多个战斗，只移除对应的战斗
     *
     * @param battle
     */
    public void removePlayerJoinBattle(Battle battle) {
        if (null == battle) {
            return;
        }

        long roleId;
        for (BattleRole map : battle.getAtkList()) {
            roleId = map.getRoleId();
            Player player = playerDataManager.getPlayer(roleId);
            if (player == null) {
                LogUtil.debug("移除战斗中所有参与玩家,player is null=" + roleId);
                continue;
            }
            // player.battleMap.remove(battle.getPos());
            LogUtil.debug("移除战斗中所有参与玩家pos=" + battle.getPos() + ",map=" + player.battleMap);
            HashSet<Integer> battleIds = player.battleMap.get(battle.getPos());
            if (battleIds != null) {
                if (battleIds.size() > 1) {
                    battleIds.remove(battle.getBattleId());
                } else {
                    player.battleMap.remove(battle.getPos());
                }
            }
            LogUtil.debug("移除战斗中所有参与玩家pos=" + battle.getPos() + ",map=" + player.battleMap);
        }
        for (BattleRole map : battle.getDefList()) {
            roleId = map.getRoleId();
            Player player = playerDataManager.getPlayer(roleId);
            if (player == null) {
                LogUtil.debug("移除战斗中所有参与玩家,player is null=" + roleId);
                continue;
            }
            // player.battleMap.remove(battle.getPos());
            LogUtil.debug("def移除战斗中所有参与玩家pos=" + battle.getPos() + ",map=" + player.battleMap);
            HashSet<Integer> battleIds = player.battleMap.get(battle.getPos());
            if (battleIds != null) {
                if (battleIds.size() > 1) {
                    battleIds.remove(battle.getBattleId());
                } else {
                    player.battleMap.remove(battle.getPos());
                }
            }
            LogUtil.debug("def移除战斗中所有参与玩家pos=" + battle.getPos() + ",map=" + player.battleMap);
        }
    }

    /**
     * 玩家被击飞，重新生成坐标
     *
     * @param player    被击飞的玩家
     * @param type      battleType
     * @param atkPlayer 进攻方对象
     */
    private void playerHitFly(Player player, int type, Player atkPlayer) {
        if (null == player) {
            return;
        }
        int prePos = player.lord.getPos();
        int newPos = worldDataManager.randomPlayerPos(player, prePos, player.lord.getCamp());

        // 更新玩家坐标
        player.lord.setPos(newPos);
        int newArea = MapHelper.getAreaIdByPos(newPos);
        player.lord.setArea(newArea);
        worldDataManager.removePlayerPos(prePos, player);
        worldDataManager.putPlayer(player);
        if (type != WorldConstant.BATTLE_TYPE_DECISIVE_BATTLE
                && BuildingDataManager.getBuildingLv(BuildingType.WALL, player) > 0) {// 城墙建起之后才会有失火状态
            player.setFireState(true);// 设置失火状态
        }
        if (newArea != MapHelper.getAreaIdByPos(prePos)) {// 被跨区击飞
            for (Army army : player.armys.values()) { // 重新计算行军线路
                worldDataManager.addMarch(new March(player, army));
            }
        }

        // 首次被击飞
        if (player.isFirstHitFly()) {
            if (player.isLogin) {
                // 首次被击飞
                activityService.checkTriggerGiftSync(ActivityConst.TRIGGER_GIFT_FIRST_BY_HIT_FLY, player);
                player.setHitFlyCount(player.getHitFlyCount() + 1);
            } else {
                player.setOffOnlineHitFly(true);
            }
        }

        // 更新触发式礼包进度
        activityDataManager.updateTriggerStatus(ActivityConst.TRIGGER_GIFT_REBUILD, player, 1);

        // 通知玩家被击飞迁城
        syncRoleMove(player, newPos);
        // 重建家园
        buildingDataManager.SyncRebuild(player, atkPlayer);
        // 击飞事件推送
        worldDataManager.syncWorldChangeEvent(WorldEvent.createWorldEvent(prePos, WorldEvent.EVENT_HIT_FLY));

        // 通知周围玩
        List<Integer> posList = new ArrayList<>();
        posList.add(prePos);
        posList.add(newPos);
        worldService.onPlayerPosChangeCallbcak(player, prePos, newPos, WorldConstant.CHANGE_POS_TYPE_2);
        // 通知其他玩家数据改变
        EventBus.getDefault()
                .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_AREA_TYPE));
    }

    public void syncRoleMove(Player player, int newPos) {
        SyncRoleMoveRs.Builder builder = SyncRoleMoveRs.newBuilder();
        builder.setNewPos(newPos);
        builder.setIsFireState(player.isFireState());
        Base.Builder msg = PbHelper.createRsBase(SyncRoleMoveRs.EXT_FIELD_NUMBER, SyncRoleMoveRs.ext, builder.build());
        MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
    }

    /**
     * 将城战被攻击玩家可出战用将领加入防御阵型中
     *
     * @param battle
     */
    public void addCityDefendRoleHeros(Battle battle) {
        if (null == battle || battle.isAtkNpc()) {
            return;
        }

        Player defencer = battle.getDefencer();
        // Map<Long, Map<Integer, List<Integer>>> map;
        // Map<Integer, List<Integer>> armyMap;

        List<Integer> heroIdList;

        // 自己的城防军
        // battle.getDefRoleList().add(defencer);
        autoFillArmy(defencer);
        // try {
        // playerDataManager.autoAddArmy(defencer);
        // wallService.processAutoAddArmy(defencer);
        // } catch (Exception e) {
        // LogUtil.error("战斗前补兵", e);
        // }
        List<Hero> heroList = defencer.getDefendHeros();
        // List<Integer> heroIdList =
        // battle.getDefHeroIdMap().get(defencer.roleId);
        heroIdList = new ArrayList<>();
        for (Hero hero : heroList) {
            if (hero.getCount() <= 0) {
                continue;
            }
            heroIdList.add(hero.getHeroId());
        }
        battle.getDefList().add(BattleRole.newBuilder().setKeyId(WorldConstant.ARMY_TYPE_HLEP)
                .setRoleId(defencer.roleId).addAllHeroId(heroIdList).build());
        LogUtil.debug(defencer.lord.getPos() + "自己部队驻防信息=" + heroList + ",防守方Defs=" + battle.getDefList());

        int now = TimeHelper.getCurrentSecond();
        // 城防NPC
        WallNpc wallNpc = null;
        if (!defencer.wallNpc.isEmpty()) {
            for (Entry<Integer, WallNpc> ks : defencer.wallNpc.entrySet()) {
                wallNpc = ks.getValue();
                StaticWallHeroLv staticSuperEquipLv = StaticBuildingDataMgr.getWallHeroLv(wallNpc.getHeroNpcId(),
                        wallNpc.getLevel());
                int maxArmy = staticSuperEquipLv.getAttr().get(FightCommonConstant.AttrId.LEAD);
                if (wallNpc.getCount() < maxArmy) {
                    continue;
                }
                wallNpc.setAddTime(now); // 刷新一下补兵的时间
                battle.getDefList().add(BattleRole.newBuilder().setKeyId(WorldConstant.ARMY_TYPE_WALL_NPC)
                        .setRoleId(defencer.roleId).addHeroId(ks.getKey()).build());
            }
        }

        // 驻守本城的其他玩家将领
        List<Army> list = worldDataManager.getPlayerGuard(defencer.lord.getPos());
        if (list != null && !list.isEmpty()) {
            Player tarPlayer = null;
            for (Army army : list) {
                if (army.getType() != ArmyConstant.ARMY_TYPE_GUARD) {
                    LogUtil.error(String.format("battleId :%d, 驻防部队type 异常 :%s", battle.getBattleId(), army));
                    continue;
                }
                tarPlayer = playerDataManager.getPlayer(army.getLordId());
                if (tarPlayer == null) continue;
                //避免驻防玩家与防守玩家不是同一个阵营
                if (tarPlayer.getCamp() != defencer.getCamp()) {
                    LogUtil.error2Sentry(String.format("battleId %d, pos %d, 驻防部队阵营异常 defence.camp %d, defence.pos %d, defence.lordId :%d, army.player.camp %d, army.player.lordId :%d",
                            battle.getBattleId(), battle.getPos(), defencer.getCamp(), defencer.lord.getPos(), defencer.getLordId(), tarPlayer.getCamp(), tarPlayer.getLordId()));
                    continue;
                }
                heroIdList = new ArrayList<>();
                for (TwoInt twoInt : army.getHero()) {
                    heroIdList.add(twoInt.getV1());
                }
                // armyMap.put(army.getKeyId(), heroIdList);
                // 防止防守成功,守军被撤回(撤回时会判断有部队ID的)
                BattleRole.Builder battleRoleBuilder = BattleRole.newBuilder().setKeyId(WorldConstant.ARMY_TYPE_HLEP).setRoleId(tarPlayer.roleId).addAllHeroId(heroIdList);
                if (!ObjectUtils.isEmpty(army.getSeasonTalentAttr())) {
                    battleRoleBuilder.addAllSeasonTalentAdd(army.getSeasonTalentAttr());
                }
                battle.getDefList().add(battleRoleBuilder.build());
            }
        }
        LogUtil.debug(defencer.lord.getPos() + ", 城墙驻防信息=" + list + ",防守方Defs=" + battle.getDefList());
    }

    // /**
    // * 将城战被攻击玩家可出战用将领加入防御阵型中
    // *
    // * @param battle
    // */
    // public void addCityDefendRoleHeros(Battle battle) {
    // if (null == battle || battle.isAtkNpc()) {
    // return;
    // }
    //
    // Player defencer = battle.getDefencer();
    // Map<Long, Map<Integer, List<Integer>>> map;
    // Map<Integer, List<Integer>> armyMap;
    //
    // List<Integer> heroIdList;
    //
    // // 自己的城防军
    // map = new HashMap<>();
    // armyMap = new HashMap<>();
    // // battle.getDefRoleList().add(defencer);
    // try {
    // wallService.processAutoAddArmy(defencer);
    // } catch (Exception e) {
    // LogUtil.error("战斗前补兵", e);
    // }
    // List<Hero> heroList = defencer.getDefendHeros();
    // // List<Integer> heroIdList =
    // battle.getDefHeroIdMap().get(defencer.roleId);
    // heroIdList = new ArrayList<>();
    // for (Hero hero : heroList) {
    // if (hero.getCount() <= 0) {
    // continue;
    // }
    // heroIdList.add(hero.getHeroId());
    // }
    // armyMap.put(-1, heroIdList);
    // map.put(defencer.roleId, armyMap);
    // battle.getDefs().add(map);
    // LogUtil.debug(defencer.lord.getPos() + "自己部队驻防信息=" + heroList +
    // ",防守方Defs=" + battle.getDefs());
    //
    // // 城防NPC
    //
    // // 驻守本城的其他玩家将领
    // List<Army> list =
    // worldDataManager.getPlayerGuard(defencer.lord.getPos());
    // if (list != null && !list.isEmpty()) {
    // Player tarPlayer = null;
    // for (Army army : list) {
    // tarPlayer = playerDataManager.getPlayer(army.getLordId());
    // if (tarPlayer == null) {
    // continue;
    // }
    // heroIdList = new ArrayList<>();
    // armyMap = new HashMap<>();
    // map = new HashMap<>();
    // for (TwoInt twoInt : army.getHero()) {
    // heroIdList.add(twoInt.getV1());
    // }
    // // armyMap.put(army.getKeyId(), heroIdList);
    // armyMap.put(-1, heroIdList);// 防止防守成功,守军被撤回(撤回时会判断有部队ID的)
    // map.put(tarPlayer.roleId, armyMap);
    // battle.getDefs().add(map);
    // }
    // }
    // LogUtil.debug(defencer.lord.getPos() + "驻防信息=" + list + ",防守方Defs=" +
    // battle.getDefs());
    // }

    /**
     * 返还所有参战玩家的部队
     *
     * @param battle
     * @param now
     */
    public void retreatBattleArmy(Battle battle, int now) {
        // 攻防双方撤回部队， 先取参与者，然后遍历参与者的army的BattleId
        retreatBattleArmy(battle, now, battle.getAtkRoles());
        retreatBattleArmy(battle, now, battle.getDefRoles());

        // List<BattleRole> list = new ArrayList<>();
        // LogUtil.debug("返还所有参战玩家的部队battle=" + battle);
        // list.addAll(battle.getAtkList());
        // list.addAll(battle.getDefList());
        // long roleId;
        // for (BattleRole map : list) {
        // roleId = map.getRoleId();
        // player = playerDataManager.getPlayer(roleId);
        // if (player == null) {
        // LogUtil.debug("返还所有参战玩家的部队,player is null=" + roleId);
        // continue;
        // }
        // // 撤回所有部队，排除驻防部队，驻防部队在击飞或者兵力为0的时候撤回
        // if (map.getKeyId() > 0) {
        // Army army = player.armys.get(map.getKeyId());
        // if (army != null) { // && army.getTarget() == battle.getPos()
        // LogUtil.debug("返还所有参战玩家的部队armyTarget=" + army.getTarget() +
        // ",battlePos=" + battle.getPos()
        // + ",army=" + army);
        // worldService.retreatArmy(player, army, now);
        //
        // // 推送
        // worldService.synRetreatArmy(player, army, now);
        // }
        // }
        // }
    }

    private void retreatBattleArmy(Battle battle, int now, Set<Long> roleIds) {
        // 攻防双方撤回部队， 先取参与者，然后遍历参与者的army的BattleId
        Player player;
        for (long roleId : roleIds) {
            player = playerDataManager.getPlayer(roleId);
            if (player == null) {
                continue;
            }
            for (Entry<Integer, Army> kv : player.armys.entrySet()) {
                Army army = kv.getValue();
                if (army == null || army.getBattleId() == null || army.getBattleId() != battle.getBattleId()) {
                    continue;
                }
                if (army.getState() == ArmyConstant.ARMY_STATE_RETREAT) {
                    continue;
                }
                LogUtil.debug("返还所有参战玩家的部队armyTarget=" + army.getTarget() + ",battlePos=" + battle.getPos() + ",army="
                        + army);
                worldService.retreatArmy(player, army, now);
                // 推送
                worldService.synRetreatArmy(player, army, now);
            }
        }
    }

    /**
     * 撤退所有参战者
     *
     * @param battle
     * @param now
     */
    public void retreatAllBattleArmy(Battle battle, int now) {
        Set<Long> list = new HashSet<>();
        LogUtil.debug("返还所有参战玩家的部队battle=" + battle);
        list.addAll(battle.getAtkRoles());
        list.addAll(battle.getDefRoles());
        for (long roleId : list) {
            Player player = playerDataManager.getPlayer(roleId);
            if (player == null) {
                LogUtil.debug("返还所有参战玩家的部队,player is null=" + roleId);
                continue;
            }

            if (player.armys == null || player.armys.isEmpty()) {
                LogUtil.debug("返还所有参战玩家的部队,armyMap is null=" + player.armys);
                continue;
            }

            for (Army army : player.armys.values()) {
                if (army != null && army.getBattleId() != null && army.getBattleId() == battle.getBattleId()) {
                    LogUtil.debug("返还所有参战玩家的部队armyTarget=" + army.getTarget() + ",battlePos=" + battle.getPos()
                            + ",army=" + army);
                    worldService.retreatArmy(player, army, now);
                    worldService.synRetreatArmy(player, army, now); // 推送
                }
            }
        }
    }

    /**
     * 撤退所有太保的参战者
     *
     * @param battle
     * @param now
     * @param posList
     */
    private void retreatAllGestapoArmy(Battle battle, int now, List<Integer> posList) {
        Set<Long> list = new HashSet<>();
        LogUtil.debug("返还所有参战玩家的部队battle=" + battle);
        list.addAll(battle.getAtkRoles());
        for (long roleId : list) {
            Player player = playerDataManager.getPlayer(roleId);
            if (player == null) {
                LogUtil.debug("返还所有参战玩家的部队,player is null=" + roleId);
                continue;
            }

            if (player.armys == null || player.armys.isEmpty()) {
                LogUtil.debug("返还所有参战玩家的部队,armyMap is null=" + player.armys);
                continue;
            }

            for (Army army : player.armys.values()) {
                if (army != null && army.getBattleId() != null && army.getBattleId() == battle.getBattleId()) {
                    int targetPos = battle.getPos();
                    LogUtil.debug("返还所有参战玩家的部队armyTarget=" + army.getTarget() + ",battlePos=" + battle.getPos()
                            + ",army=" + army);
                    worldService.retreatArmy(player, army, now);
                    worldService.synRetreatArmy(player, army, now); // 推送
                    // 目标丢失的邮件
                    if (battle.isGestapoBattle()) {
                        Turple<Integer, Integer> xy = MapHelper.reducePos(targetPos);
                        mailDataManager.sendReportMail(player, null, MailConstant.MOLD_ATK_GESTAPO_NOT_FOUND, null, now,
                                xy.getA(), xy.getB(), xy.getA(), xy.getB());
                        MapHelper.getAreaStartPos((MapHelper.getLineAcorss(targetPos, player.lord.getPos())));
                        posList.add(player.lord.getPos());
                    }
                }
            }
        }
    }

    /**
     * 发送城战（玩家之间的战斗）通知邮件
     *
     * @param battle
     * @param cityId
     * @param atkLord
     * @param defLord
     * @param atkPos
     * @param defPos
     * @param atkSuccess
     * @param report
     * @param dropList
     * @param loseList
     * @param now
     */
    public void sendCityBattleMail(Battle battle, int cityId, Lord atkLord, Lord defLord,
                                   Turple<Integer, Integer> atkPos, Turple<Integer, Integer> defPos, boolean atkSuccess,
                                   CommonPb.Report.Builder report, List<Award> dropList, List<Award> loseList, int now,
                                   Map<Long, List<Award>> recoverArmyAwardMap) {
        // 进攻方邮件
        sendCityBattleAtkMails(battle, atkSuccess, report, dropList, now, recoverArmyAwardMap, atkLord.getNick(),
                defLord.getCamp(), defLord.getLevel(), defLord.getNick(), atkLord.getNick(), atkPos.getA(),
                atkPos.getB(), defLord.getLevel(), defLord.getNick(), defPos.getA(), defPos.getB());

        // 防守方邮件
        sendCityBattleDefMails(battle, !atkSuccess, report, loseList, now, recoverArmyAwardMap, defLord.getNick(),
                atkLord.getCamp(), atkLord.getLevel(), atkLord.getNick(), atkLord.getCamp(), atkLord.getLevel(),
                atkLord.getNick(), atkPos.getA(), atkPos.getB(), defLord.getNick(), defPos.getA(), defPos.getB());
    }

    /**
     * 发送阵营战（军团战）通知邮件
     *
     * @param battle
     * @param cityId
     * @param atkLord
     * @param defLord
     * @param atkPos
     * @param defPos
     * @param atkSuccess
     * @param report
     * @param dropMap
     * @param now
     * @param recoverArmyAwardMap
     */
    public void sendCampBattleMail(Battle battle, int cityId, Lord atkLord, Lord defLord,
                                   Turple<Integer, Integer> atkPos, Turple<Integer, Integer> defPos, boolean atkSuccess,
                                   CommonPb.Report.Builder report, Map<Long, List<Award>> dropMap, int now,
                                   Map<Long, List<Award>> recoverArmyAwardMap) {
        if (null == defLord) {// 城池没有所属者
            // 进攻方邮件
            if (atkLord != null) {
                sendCampBattleAtkMails(battle, battle.getAtkList(), atkSuccess, report, dropMap, now,
                        recoverArmyAwardMap, atkLord.getNick(), cityId, atkLord.getNick(), atkPos.getA(), atkPos.getB(),
                        cityId, defPos.getA(), defPos.getB());
            } else if (!battle.getAtkList().isEmpty()) {
                sendCampBattleAtkMails(battle, battle.getAtkList(), atkSuccess, report, dropMap, now,
                        recoverArmyAwardMap, battle.getAtkName(), cityId, battle.getAtkName(), atkPos.getA(),
                        atkPos.getB(), cityId, defPos.getA(), defPos.getB());
            }
            if (!battle.isAtkNpc()) {// 防守方邮件
                if (atkLord != null) {
                    sendCampBattleDefMails(battle, battle.getDefList(), 0, !atkSuccess, report, dropMap, now,
                            recoverArmyAwardMap, cityId, atkLord.getCamp(), atkLord.getLevel(), atkLord.getNick(),
                            atkLord.getCamp(), atkLord.getLevel(), atkLord.getNick(), atkPos.getA(), atkPos.getB(),
                            cityId, defPos.getA(), defPos.getB());
                } else {
                    sendCampBattleDefMails(battle, battle.getDefList(), 0, !atkSuccess, report, dropMap, now,
                            recoverArmyAwardMap, cityId, battle.getAtkCamp(), 0, battle.getAtkName(),
                            battle.getAtkCamp(), 0, battle.getAtkName(), atkPos.getA(), atkPos.getB(), cityId,
                            defPos.getA(), defPos.getB());
                }
            }
        } else {
            // 进攻方邮件
            if (atkLord != null) {
                sendCampBattleAtkMails(battle, battle.getAtkList(), atkSuccess, report, dropMap, now,
                        recoverArmyAwardMap, atkLord.getNick(), cityId, atkLord.getNick(), atkPos.getA(), atkPos.getB(),
                        cityId, defPos.getA(), defPos.getB());
            } else if (!battle.getAtkList().isEmpty()) {
                sendCampBattleAtkMails(battle, battle.getAtkList(), atkSuccess, report, dropMap, now,
                        recoverArmyAwardMap, battle.getAtkName(), cityId, battle.getAtkName(), atkPos.getA(),
                        atkPos.getB(), cityId, defPos.getA(), defPos.getB());
            }
            // 防守方邮件
            if (atkLord != null) {
                sendCampBattleDefMails(battle, battle.getDefList(), defLord.getLordId(), !atkSuccess, report, dropMap,
                        now, recoverArmyAwardMap, cityId, atkLord.getCamp(), atkLord.getLevel(), atkLord.getNick(),
                        atkLord.getCamp(), atkLord.getLevel(), atkLord.getNick(), atkPos.getA(), atkPos.getB(), cityId,
                        defPos.getA(), defPos.getB());
            } else {
                sendCampBattleDefMails(battle, battle.getDefList(), defLord.getLordId(), !atkSuccess, report, dropMap,
                        now, recoverArmyAwardMap, cityId, battle.getAtkCamp(), 0, "", battle.getAtkCamp(), 0,
                        battle.getAtkName(), atkPos.getA(), atkPos.getB(), cityId, defPos.getA(), defPos.getB());
            }
        }
    }

    /**
     * 营地战给防守方发邮件
     *
     * @param battle
     * @param defSuccess
     * @param report
     * @param dropList
     * @param now
     * @param param
     */
    private void sendCityBattleDefMails(Battle battle, boolean defSuccess, CommonPb.Report.Builder report,
                                        List<Award> dropList, int now, Map<Long, List<CommonPb.Award>> recoverArmyAwardMap, Object... param) {
        List<BattleRole> list = battle.getDefList();
        LogUtil.debug("发送城战防守方战斗报告邮件, playerNum:", list.size());
        Set<Long> ids = new HashSet<>();
        list.forEach(role -> ids.add(role.getRoleId()));// 添加所有参战的人
        ids.add(battle.getDefencerId()); // 添加防守
        for (Long roleId : ids) {
            Player player = playerDataManager.getPlayer(roleId);
            if (player == null) {
                LogUtil.debug("创建阵营战守方Fighter对象,player is null=" + roleId);
                continue;
            }
            ids.add(roleId);
            if (defSuccess) {
                // 防守成功，没有损失
                mailDataManager.sendReportMail(player, report, MailConstant.MOLD_DEF_CITY_SUCC, null, now,
                        recoverArmyAwardMap, param);
            } else {
                Object[] params = Arrays.copyOf(param, param.length + 1);
                params[param.length] = battle.getDefencer().lord.getNick();
                mailDataManager.sendReportMail(player, report, MailConstant.MOLD_DEF_CITY_FAIL, dropList, now,
                        recoverArmyAwardMap, params);
            }
        }
    }

    /**
     * 营地战给进攻方发邮件
     *
     * @param battle
     * @param atkSuccess
     * @param report
     * @param dropList
     * @param now
     * @param param
     */
    private void sendCityBattleAtkMails(Battle battle, boolean atkSuccess, CommonPb.Report.Builder report,
                                        List<Award> dropList, int now, Map<Long, List<CommonPb.Award>> recoverArmyAwardMap, Object... param) {
        List<BattleRole> list = battle.getAtkList();
        LogUtil.debug("发送城战进攻方战斗报告邮件, playerNum:", list.size());
        long roleId;
        Set<Long> ids = new HashSet<>();
        for (BattleRole map : list) {
            roleId = map.getRoleId();
            if (ids.contains(roleId)) {
                continue;
            }
            Player player = playerDataManager.getPlayer(roleId);
            if (player == null) {
                LogUtil.debug("创建阵营战守方Fighter对象,player is null=" + roleId);
                continue;
            }
            ids.add(roleId);
            if (atkSuccess) {
                Object[] params = Arrays.copyOf(param, param.length + 1);
                params[param.length] = battle.getSponsor() != null ? battle.getSponsor().lord.getNick() : "";
                mailDataManager.sendReportMail(player, report, MailConstant.MOLD_ATK_CITY_SUCC, dropList, now,
                        recoverArmyAwardMap, params);
            } else {// 进攻失败，没有掉落奖励
                mailDataManager.sendReportMail(player, report, MailConstant.MOLD_ATK_CITY_FAIL, null, now,
                        recoverArmyAwardMap, param);
            }
        }
    }

    /**
     * 阵营战给防守方发邮件
     *
     * @param battle
     * @param list
     * @param defender
     * @param defSuccess
     * @param report
     * @param dropMap
     * @param now
     * @param param
     */
    private void sendCampBattleDefMails(Battle battle, List<BattleRole> list, long defender, boolean defSuccess,
                                        CommonPb.Report.Builder report, Map<Long, List<Award>> dropMap, int now,
                                        Map<Long, List<CommonPb.Award>> recoverArmyAwardMap, Object... param) {
        LogUtil.debug("发送阵营战防守方战报邮件, playerNum:", list.size());
        List<Award> dropList;
        long roleId;
        Set<Long> ids = new HashSet<>();
        for (BattleRole map : list) {
            roleId = map.getRoleId();
            if (ids.contains(roleId)) {
                continue;
            }
            Player player = playerDataManager.getPlayer(roleId);
            if (player == null) {
                LogUtil.debug("创建阵营战守方Fighter对象,player is null=" + roleId);
                continue;
            }
            ids.add(roleId);
            if (defSuccess) {
                // Object[] params = Arrays.copyOf(param, param.length + 1);
                // params[param.length] = player.lord.getNick();
                // dropList = dropMap.get(player.roleId);
                mailDataManager.sendReportMail(player, report, MailConstant.MOLD_DEF_CAMP_SUCC, null, now,
                        recoverArmyAwardMap, param);
            } else {
                mailDataManager.sendReportMail(player, report, MailConstant.MOLD_DEF_CAMP_FAIL, null, now,
                        recoverArmyAwardMap, param);
            }
        }
        if (!ids.contains(defender)) {
            Player player = playerDataManager.getPlayer(defender);
            if (player == null) {
                LogUtil.debug("创建阵营战守方Fighter对象,player is null=" + defender);
            } else {
                if (defSuccess) {
                    // Object[] params = Arrays.copyOf(param, param.length + 1);
                    // LogUtil.debug("params==" + params + ",param=" + param +
                    // ",player.lord.getNick()="
                    // + player.lord.getNick());
                    // params[param.length] = player.lord.getNick();
                    // dropList = dropMap.get(player.roleId);
                    mailDataManager.sendReportMail(player, report, MailConstant.MOLD_DEF_CAMP_SUCC, null, now,
                            recoverArmyAwardMap, param);
                } else {
                    mailDataManager.sendReportMail(player, report, MailConstant.MOLD_DEF_CAMP_FAIL, null, now,
                            recoverArmyAwardMap, param);
                }
            }
        }
    }

    /**
     * 阵营战给进攻方发邮件
     *
     * @param battle
     * @param list
     * @param atkSuccess
     * @param report
     * @param dropMap
     * @param now
     * @param param
     */
    private void sendCampBattleAtkMails(Battle battle, List<BattleRole> list, boolean atkSuccess,
                                        CommonPb.Report.Builder report, Map<Long, List<Award>> dropMap, int now,
                                        Map<Long, List<CommonPb.Award>> recoverArmyAwardMap, Object... param) {
        LogUtil.debug("发送阵营战进攻方战报邮件, playerNum:", list.size());
        List<Award> dropList;
        long roleId;
        Set<Long> ids = new HashSet<>();
        for (BattleRole map : list) {
            roleId = map.getRoleId();
            if (ids.contains(roleId)) {
                continue;
            }
            Player player = playerDataManager.getPlayer(roleId);
            if (player == null) {
                LogUtil.debug("创建阵营战守方Fighter对象,player is null=" + roleId);
                continue;
            }
            ids.add(roleId);
            if (atkSuccess) {
                Object[] params = Arrays.copyOf(param, param.length + 1);
                params[param.length] = player.lord.getNick();
                dropList = dropMap.get(player.roleId);
                mailDataManager.sendReportMail(player, report, MailConstant.MOLD_ATK_CAMP_SUCC, dropList, now,
                        recoverArmyAwardMap, params);
            } else {
                mailDataManager.sendReportMail(player, report, MailConstant.MOLD_ATK_CAMP_FAIL, null, now,
                        recoverArmyAwardMap, param);
            }
        }

        if (battle.getSponsor() != null && (!ids.contains(battle.getSponsor().roleId.longValue()))) {// 发起者没有派兵时,给发起者发失败邮件
            Player sponsor = battle.getSponsor();
            if (atkSuccess) {
                Object[] params = Arrays.copyOf(param, param.length + 1);
                params[param.length] = sponsor.lord.getNick();
                dropList = dropMap.get(sponsor.roleId);
                mailDataManager.sendReportMail(sponsor, report, MailConstant.MOLD_ATK_CAMP_SUCC, dropList, now,
                        recoverArmyAwardMap, params);
            } else {
                mailDataManager.sendReportMail(sponsor, report, MailConstant.MOLD_ATK_CAMP_FAIL, null, now,
                        recoverArmyAwardMap, param);
            }
        }
    }

    /**
     * 给参战将领加经验和军工
     *
     * @param forces
     * @param from
     * @param rpt
     * @param isAttacker
     * @param atkPlayer       是否是城战（攻击玩家）
     * @param addExploit      是否获取军工
     * @param exploitAwardMap
     */
    public void addBattleHeroExp(List<Force> forces, AwardFrom from, CommonPb.RptAtkPlayer.Builder rpt,
                                 boolean isAttacker, boolean atkPlayer, boolean cityBattle, Map<Long, ChangeInfo> changeMap,
                                 boolean addExploit, HashMap<Long, Map<Integer, Integer>> exploitAwardMap) {
        for (Force force : forces) {
            // 奖励记录
            long roleId = force.ownerId;
            ChangeInfo info = changeMap.get(roleId);
            if (null == info) {
                info = ChangeInfo.newIns();
                changeMap.put(roleId, info);
            }
            int type = force.roleType;
            int kill = force.killed;
            int heroId = force.id;
            // 军工计算 ; 打人 军工=损兵数,其他按照公式来
            int count = cityBattle ? force.totalLost : (int) (force.killed * 0.8f + force.totalLost * 0.2f);
            int award = addExploit ? count : 0;// 军工
            String owner = playerDataManager.getNickByLordId(roleId);
            int lv = 0;// 将领等级
            int addExp = 0;// 将领经验
            int lost = force.lost;
            int heroDecorated = 0;
            Hero hero = null;
            if (force.roleType == Constant.Role.CITY) {

            } else if (force.roleType == Constant.Role.WALL) {
                Player player = playerDataManager.getPlayer(roleId);
                if (player == null) {
                    continue;
                }
                award = addExploit(player, award, info, from); // 加军工
                heroId = player.wallNpc.get(heroId).getWallHeroLvId();
            } else if (force.roleType == Constant.Role.PLAYER) {
                Player player = playerDataManager.getPlayer(roleId);
                if (player == null) {
                    continue;
                }
                hero = player.heros.get(heroId);
                if (hero == null) {
                    continue;
                }
                // 给将领加经验
                if (force.hasFight) {
                    addExp = (force.killed + force.totalLost) / 2;// 经验=（杀敌数+损兵数）/2
                    addExp = heroService.adaptHeroAddExp(player, addExp);
                    addExp = heroService.addHeroExp(hero, addExp, player.lord.getLevel(), player);
                }
                award = addExploit(player, award, info, from);
                lv = hero.getLevel();
                heroDecorated = hero.getDecorated();
            } else {

            }
            if (!CheckNull.isEmpty(exploitAwardMap)) {
                final Map<Integer, Integer> exploitAward = exploitAwardMap.get(roleId);
                if (!CheckNull.isEmpty(exploitAward)) {
                    int exploit = exploitAward.getOrDefault(heroId, 0);
                    if (exploit > 0) {
                        award += exploit;
                    }
                }
            }
            RptHero rptHero = PbHelper.createRptHero(type, kill, award, force, owner, lv, addExp, lost);
            if (isAttacker) {
                rpt.addAtkHero(rptHero);
            } else {
                rpt.addDefHero(rptHero);
            }
        }
    }

    /**
     * 跨服发送奖励
     *
     * @param heroFightSummary
     * @param player
     * @param from
     */
    public void addCrossBattleHeroExp(HeroFightSummary heroFightSummary, Player player, AwardFrom from) {
        // 奖励记录
        int heroId = heroFightSummary.getHeroId();
        if (heroFightSummary.getExploit() > 0) addExploit(player, heroFightSummary.getExploit(), null, from);

        if (heroFightSummary.getExp() > 0) {
            Hero hero = player.heros.get(heroId);
            if (Objects.nonNull(hero)) {
                heroService.addHeroExp(hero, heroFightSummary.getExp(), player.lord.getLevel(), player);
            }
        }
    }

    /**
     * 加军工
     *
     * @param player
     * @param count
     * @param info
     * @param from
     * @return
     */
    public int addExploit(Player player, int count, ChangeInfo info, AwardFrom from) {
        // 科技加成
        int award = count;
        double techAdd = techDataManager.getTechEffect4Single(player, TechConstant.TYPE_26);
        if (techAdd > 0) {
            award += award * techAdd;
        }
        if (award > 0) {
            // 增加玩家军功
            rewardDataManager.addAward(player, AwardType.MONEY, AwardType.Money.EXPLOIT, award, from);
            if (!CheckNull.isNull(info)) {
                info.addChangeType(AwardType.MONEY, AwardType.Money.EXPLOIT);
            }
        }
        return award;

    }

    /**
     * 扣除城防军兵力
     *
     * @param forces
     * @param city
     */
    public void subBattleNpcArm(List<Force> forces, City city) {
        if (CheckNull.isEmpty(forces)) {
            return;
        }

        CityHero hero;
        for (Force force : forces) {
            hero = city.getCityHero(force.id);
            if (hero != null) {
                hero.subArm(force.totalLost);
            }
        }
    }

    /**
     * 扣除战斗中兵力消耗
     *
     * @param forces
     * @param changeMap
     * @param from
     */
    public void subBattleHeroArm(List<Force> forces, Map<Long, ChangeInfo> changeMap, AwardFrom from) {
        if (CheckNull.isEmpty(forces)) {
            return;
        }

        int lost;
        Hero hero;
        Player player;
        ChangeInfo info;
        for (Force force : forces) {
            if (force.roleType == Constant.Role.CITY) {
                continue;
            }
            player = playerDataManager.getPlayer(force.ownerId);
            if (player == null) {
                LogUtil.error("扣除兵力，未找到玩家, roleId:", force.ownerId);
                continue;
            }
            if (force.totalLost > 0) {
                // 战令的损兵进度
                battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_SUB_HERO_ARMY, force.totalLost);
                activityDataManager.updRankActivity(player, ActivityConst.ACT_ARMY_RANK, force.totalLost);
                // 荣耀日报损兵进度
                honorDailyDataManager.addAndCheckHonorReport2s(player, HonorDailyConstant.COND_ID_14, force.totalLost);
                if (force.roleType == Constant.Role.WALL) {
                    WallNpc wallNpc = player.wallNpc.get(force.id);
                    if (null == wallNpc) {
                        LogUtil.error("扣除兵力，未找到城防将领NPC, wallId:", force.id);
                        continue;
                    }

                    lost = wallNpc.subArm(force.totalLost);
                    LogLordHelper.wallNPCArm(from, player.account, player.lord, wallNpc.getHeroNpcId(),
                            wallNpc.getCount(), -lost, Constant.ACTION_SUB);
                } else {

                    hero = player.heros.get(force.id);
                    if (null == hero) {
                        LogUtil.error("扣除兵力，未找到将领, heroId:", force.id);
                        continue;
                    }
                    lost = hero.subArm(force.totalLost);
                    StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
                    if (Objects.nonNull(staticHero)) {
                        // 获取武将对应类型的兵力
                        int armType = staticHero.getType();
                        // LogLordHelper.heroArm(from, player.account, player.lord, hero.getHeroId(), hero.getCount(), -lost, staticHero.getType(),
                        //         Constant.ACTION_SUB);

                        // 上报玩家兵力变化
                        LogLordHelper.playerArm(
                                from,
                                player,
                                armType,
                                Constant.ACTION_SUB,
                                -lost
                        );
                    }

                    info = changeMap.get(force.ownerId);
                    if (null == info) {
                        info = ChangeInfo.newIns();
                        changeMap.put(force.ownerId, info);
                    }
                    info.addChangeType(AwardType.HERO_ARM, hero.getHeroId());
                }
            }
            if (force.killed > 0) {
                // 大杀四方
                activityDataManager.updActivity(player, ActivityConst.ACT_BIG_KILL, force.killed, 0, true);
            }
        }
    }

    /**
     * 兼容之前的方法
     *
     * @param targetPos 城战坐标
     * @param atkCancel 是否是进攻方取消
     */
    public void cancelCityBattle(int targetPos, boolean atkCancel) {
        cancelCityBattle(targetPos, 0, atkCancel, true);
    }

    /**
     * 玩家已经迁城时取消城战
     *
     * @param targetPos 城战坐标
     * @param newPos    新坐标
     * @param atkCancel 是否是进攻方取消
     */
    public void cancelCityBattle(int targetPos, int newPos, boolean atkCancel, boolean distance) {
        if (worldDataManager.isEmptyPos(targetPos)) {
            return;
        }

        if (!worldDataManager.isPlayerPos(targetPos)) {
            return;
        }

        // 获取该坐标所有战斗信息(除了反攻德意志, 迁城不结束战斗)
        List<Battle> battleList = warDataManager.getBattlePosMap().get(targetPos);
        if (CheckNull.isEmpty(battleList)) {
            return;
        }

        int now = TimeHelper.getCurrentSecond();
        Lord lord = worldDataManager.getPosData(targetPos).lord;
        for (Battle battle : battleList) {
            if (battle.getType() == WorldConstant.BATTLE_TYPE_COUNTER_ATK) {
                continue;
            }
            if (battle.getDefencer().roleId != lord.getLordId()) {// 正常逻辑是先取消城战，在执行迁城逻辑，所以正常不应该出现这种情况
                LogUtil.error("城战玩家数据不一致, targetPos:", targetPos, ", targetId:", lord.getLordId(), ", defencerId:",
                        battle.getDefencer().roleId);
            }

            retreatAllBattleArmy(battle, now);// 部队返回

            // 通知撤退, 客户端收到消息 重新拉数据
            Player target = worldDataManager.getPosData(targetPos);
            Lord lord1 = battle.getSponsor().lord;
            if (target != null && target.isLogin) {
                worldService.syncAttackRole(target, lord1, battle.getBattleTime(), WorldConstant.ATTACK_ROLE_0);
            }


            sendCancelBattleMail(targetPos, atkCancel, battle, now, lord);  // 发送取消战斗邮件
        }

        warDataManager.removePosExchangeSpecialBattle(targetPos, newPos);// 移除坐标点上的所有战斗信息,
        // 并处理特殊战斗

        if (distance) {
            // 推送线图
            List<Integer> posList = MapHelper.getAreaStartPos((MapHelper.getLineAcorss(targetPos, lord.getPos())));
            posList.add(targetPos);
            posList.add(lord.getPos());
            EventBus.getDefault()
                    .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.LINE_TYPE));
        }
    }

    /**
     * @Author: ZhouJie
     * @Date: 2018-04-10 21:06
     * @Description: 取消太保战斗, 并返回行军
     */
    public void cancelGestapoBattle(int targetPos) {
        if (worldDataManager.isEmptyPos(targetPos)) {
            return;
        }
        if (!worldDataManager.isGestapoPos(targetPos)) {
            return;
        }

        // 获取该坐标所有战斗信息
        LinkedList<Battle> battleList = warDataManager.getBattlePosMap().get(targetPos);
        if (CheckNull.isEmpty(battleList)) {
            return;
        }
        List<Integer> posList = new ArrayList<>();
        int now = TimeHelper.getCurrentSecond();
        for (Battle battle : battleList) {
            retreatAllGestapoArmy(battle, now, posList);// 部队返回
        }
        warDataManager.removePosAllCityBattle(targetPos);// 移除太保数据
        // 推送线图
        posList.add(targetPos);
        EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.LINE_TYPE));
    }

    /**
     * 取消城战
     *
     * @param targetPos 坐标
     * @param atkCancel 是否是进攻方
     * @param battle
     * @param distance  是否同步
     */
    public void cancelCityBattle(int targetPos, boolean atkCancel, Battle battle, boolean distance) {
        if (worldDataManager.isEmptyPos(targetPos)) {
            return;
        }

        if (!worldDataManager.isPlayerPos(targetPos)) {
            return;
        }

        int now = TimeHelper.getCurrentSecond();
        Lord lord = worldDataManager.getPosData(targetPos).lord;
        if (battle.getDefencer().roleId != lord.getLordId()) {// 正常逻辑是先取消城战，在执行迁城逻辑，所以正常不应该出现这种情况
            LogUtil.error("城战玩家数据不一致, targetPos:", targetPos, ", targetId:", lord.getLordId(), ", defencerId:",
                    battle.getDefencer().roleId);
        }

        warDataManager.removeBattleById(battle.getBattleId());
        LogUtil.debug("移除battleId=" + battle.getBattleId());
        retreatBattleArmy(battle, now);// 部队返回
        removePlayerJoinBattle(battle);// 移除玩家参与记录

        sendCancelBattleMail(targetPos, atkCancel, battle, now, lord); // 发送取消战斗邮件

        if (distance) {
            // 推送线图
            List<Integer> posList = MapHelper.getAreaStartPos(MapHelper.getLineAcorss(targetPos, lord.getPos()));
            posList.add(targetPos);
            posList.add(lord.getPos());
            EventBus.getDefault()
                    .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.LINE_TYPE));
        }
    }

    /**
     * 发送取消战斗邮件
     *
     * @param targetPos
     * @param atkCancel
     * @param battle
     * @param now
     * @param lord
     */
    private void sendCancelBattleMail(int targetPos, boolean atkCancel, Battle battle, int now, Lord lord) {
        String nick = "";
        Turple<Integer, Integer> xy;
        if (atkCancel) { // 进攻方取消
            if (battle.getType() == WorldConstant.BATTLE_TYPE_DECISIVE_BATTLE) {
                nick = battle.getSponsor() != null ? battle.getSponsor().lord.getNick() : "";
                xy = MapHelper.reducePos(targetPos);
                sendCancelDecisiveAtkMail(battle, now, nick, battle.getDefCamp(), lord.getNick(), xy.getA(), xy.getB(),
                        nick, battle.getDefCamp(), lord.getNick(), xy.getA(), xy.getB());
            } else {
                nick = battle.getSponsor() != null ? battle.getSponsor().lord.getNick() : "";
                xy = MapHelper.reducePos(targetPos);
                sendCancelCityAtkMail(battle, now, nick, battle.getDefCamp(), lord.getNick(), xy.getA(), xy.getB(),
                        nick, battle.getDefCamp(), lord.getNick(), xy.getA(), xy.getB());
            }
        } else {
            // 被攻击玩家迁城
            sendCancelCityDefMail(battle, now, lord.getNick(), lord.getNick());
        }
    }

    /**
     * 给城战所有参与玩机发送被攻击玩家放弃防守邮件
     *
     * @param battle
     * @param now
     * @param param
     */
    private void sendCancelCityDefMail(Battle battle, int now, Object... param) {
        Player player;
        for (long roleId : battle.getAtkRoles()) {
            player = playerDataManager.getPlayer(roleId);
            if (player == null) {
                continue;
            }
            mailDataManager.sendNormalMail(player, MailConstant.MOLD_CITY_DEF_FLEE_ATK, now, param);
        }
        for (long roleId : battle.getDefRoles()) {
            player = playerDataManager.getPlayer(roleId);
            if (player == null) {
                continue;
            }
            mailDataManager.sendNormalMail(player, MailConstant.MOLD_CITY_DEF_FLEE_DEF, now, param);
        }
    }

    /**
     * 给决战参与玩家发送发起攻击玩家放弃邮件
     *
     * @param battle
     * @param time
     * @param param
     */
    private void sendCancelDecisiveAtkMail(Battle battle, int time, Object... param) {
        if (!CheckNull.isNull(battle.getSponsor())) {
            mailDataManager.sendNormalMail(battle.getSponsor(), MailConstant.DECISIVE_BATTLE_ATK_CANCEL, time, param);
        }
        if (!CheckNull.isNull(battle.getDefencer())) {
            mailDataManager.sendNormalMail(battle.getDefencer(), MailConstant.DECISIVE_BATTLE_DEF_CANCEL, time, param);
        }
    }

    /**
     * 给城战所有参与玩家发送发起攻击玩家放弃邮件
     *
     * @param battle
     * @param time
     * @param param
     */
    private void sendCancelCityAtkMail(Battle battle, int time, Object... param) {
        Player player;
        for (long roleId : battle.getAtkRoles()) {
            player = playerDataManager.getPlayer(roleId);
            if (player == null) {
                continue;
            }
            mailDataManager.sendNormalMail(player, MailConstant.MOLD_CITY_ATK_RETREAT_ATK, time, param);
        }
        for (long roleId : battle.getDefRoles()) {
            player = playerDataManager.getPlayer(roleId);
            if (player == null) {
                continue;
            }
            mailDataManager.sendNormalMail(player, MailConstant.MOLD_CITY_ATK_RETREAT_DEF, time, param);
        }
        if (battle.getDefencer() != null) {// 通知防守方
            mailDataManager.sendNormalMail(battle.getDefencer(), MailConstant.MOLD_CITY_ATK_RETREAT_DEF, time, param);
        }
    }

    /**
     * 切磋
     *
     * @param mPlayer 发起方
     * @param tPlayer 目标方
     * @param heroIds 将领id
     * @return 战报数据
     */
    public GamePb4.CompareNotesRs compareNotesFightLogic(Player mPlayer, Player tPlayer, List<Integer> heroIds) {
        // 进攻方将领和防守方将领
        Fighter attacker = fightService.createCombatPlayerFighter(mPlayer, heroIds);
        Fighter defender = fightService.createCombatPlayerFighter(tPlayer, tPlayer.getAllOnBattleHeros().stream().map(Hero::getHeroId).collect(Collectors.toList()));
        FightLogic fightLogic = new FightLogic(attacker, defender, true);
        fightLogic.start();
        GamePb4.CompareNotesRs.Builder builder = GamePb4.CompareNotesRs.newBuilder();
        // 战斗记录
        BattlePb.BattleRoundPb record = fightLogic.generateRecord();
        CommonPb.RptAtkPlayer.Builder rpt = CommonPb.RptAtkPlayer.newBuilder();
        rpt.setNightEffect(solarTermsDataManager.getNightEffect() != null);
        rpt.setResult(fightLogic.getWinState() == FightConstant.FIGHT_RESULT_SUCCESS);
        rpt.setRecord(record);
        // 双方的玩家信息
        rpt.setAttack(PbHelper.createRptMan(mPlayer.lord.getPos(), mPlayer.lord.getNick(), mPlayer.lord.getVip(),
                mPlayer.lord.getLevel()));
        rpt.setDefMan(PbHelper.createRptMan(tPlayer.lord.getPos(), tPlayer.lord.getNick(), tPlayer.lord.getVip(),
                tPlayer.lord.getLevel()));
        // 双方的汇总信息
        rpt.setAtkSum(PbHelper.createRptSummary(attacker.total, attacker.lost, mPlayer.lord.getCamp(),
                mPlayer.lord.getNick(), mPlayer.lord.getPortrait(), mPlayer.getDressUp().getCurPortraitFrame()));
        rpt.setDefSum(PbHelper.createRptSummary(defender.total, defender.lost, tPlayer.lord.getCamp(),
                tPlayer.lord.getNick(), tPlayer.lord.getPortrait(), tPlayer.getDressUp().getCurPortraitFrame()));

        for (Force force : attacker.forces) {
            CommonPb.RptHero rptHero = fightService.forceToRptHeroNoExp(force);
            if (rptHero != null) {
                rpt.addAtkHero(rptHero);
            }
        }
        for (Force force : defender.forces) {
            CommonPb.RptHero rptHero = fightService.forceToRptHeroNoExp(force);
            if (rptHero != null) {
                rpt.addDefHero(rptHero);
            }
        }

        builder.setReport(worldService.createAtkPlayerReport(rpt.build(), TimeHelper.getCurrentSecond()));
        return builder.build();
    }

    /**
     * 战斗添加额外属性加成（不参与战斗力计算）
     *
     * @param battle
     * @param force
     * @param hero
     * @param player
     * @param map
     */
    public void fightForceBuff(Battle battle, Force force, Hero hero, Player player, CommonPb.BattleRole map) {
        if (!ObjectUtils.isEmpty(battle.getDefencer())) {
            //阵营战 防守方是玩家
            if (force.ownerId == battle.getDefencer().roleId && Objects.nonNull(hero)) {
                if (hero.isOnWall()) {
                    //禁卫军赛季天赋属性加成
                    force.attrData.addAttrValue(DataResource.getBean(SeasonTalentService.class).
                            getSeasonTalentEffectTwoInt(player, hero, SeasonConst.TALENT_EFFECT_619));
                    //宝具加成属性
                    treasureWareBuff(player, hero, force.attrData);
                }
            }
        }
        //赛季天赋优化 加上玩家英雄驻城守军的属性加成
        if (Objects.nonNull(map)) {
            force.attrData.addAttrValue(map.getSeasonTalentAddList());
        }
    }

    /**
     * 宝具加成属性
     *
     * @param player
     * @param hero
     * @param attrData
     */
    private void treasureWareBuff(Player player, Hero hero, AttrData attrData) {
        Object buff = DataResource.getBean(TreasureWareService.class).
                getTreasureWareBuff(player, hero, TreasureWareConst.SpecialType.JANITOR_TYPE, 0);
        if (ObjectUtils.isEmpty(buff) || !(buff instanceof List)) {
            return;
        }

        List<List<Integer>> buffEffect = (List<List<Integer>>) buff;
        for (List<Integer> list : buffEffect) {
            attrData.addValue(list.get(0), list.get(1));
        }
    }
}
