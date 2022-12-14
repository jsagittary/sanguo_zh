package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.cross.gameplay.battle.c2g.dto.HeroFightSummary;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.*;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonDailyAttackTaskService;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonDailyRestrictTaskService;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.CommonPb.BattleRole;
import com.gryphpoem.game.zw.pb.CommonPb.RptHero;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.pb.GamePb2.SyncRoleMoveRs;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.constant.Constant.AttrId;
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
import com.gryphpoem.game.zw.resource.pojo.fight.*;
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
 * @Description ???????????? ????????????????????????Battle?????????
 * @date ???????????????2017???4???12??? ??????5:18:39
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
     * ???????????????????????????????????????
     */
    public void batlleTimeLogic() {
        Battle battle;
        int now = TimeHelper.getCurrentSecond();
        // ?????????BattleId
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
                        //????????????????????????
                        warDataManager.removeBattleById(battle.getBattleId());
                    }
                    // warDataManager.removePosBattleById(battle.getPos(),
                    // battle.getBattleId());
                    // its.remove();// ???????????????????????????
                }
            } catch (Exception e) {
                LogUtil.error(e, "??????????????????????????????????????? battle:", battle);
                // ????????????
                removeBattleIdSet.add(battle.getBattleId());
                // retreatBattleArmy(battle, now);
                // warDataManager.removePosBattleById(battle.getPos(),
                // battle.getBattleId());
                // its.remove();// ???????????????????????????
            }
        }

        for (Integer battleId : removeBattleIdSet) {
            battle = warDataManager.getBattleMap().get(battleId);
            if (CheckNull.isNull(battle))
                continue;

            warDataManager.removeBattleById(battleId);
            retreatBattleArmy(battle, now);// ?????????????????????????????????
            removePlayerJoinBattle(battle);// ????????????????????????
            LogUtil.debug("??????battleId=" + battleId);
        }
    }

    /**
     * ??????????????????????????????,???????????????????????? ???????????????????????????????????????????????????????????????????????????????????????????????????????????? ???????????????????????????????????????????????????????????????????????????????????????????????????????????????Army??? ??????NPC??????????????????
     *
     * @param battle
     * @param now
     * @param removeBattleIdSet
     * @throws MwException
     */
    public void gestapoFightLogic(Battle battle, int now, Set<Integer> removeBattleIdSet) throws MwException {
        if (CheckNull.isNull(battle))
            return;
        LogUtil.debug("????????????, battle:", battle);

        int pos = battle.getPos();
        CommonPb.RptAtkPlayer.Builder rpt = CommonPb.RptAtkPlayer.newBuilder();

        // ????????????
        Fighter defender = null;
        Gestapo gestapo = worldDataManager.getGestapoByPos(pos);
        if (CheckNull.isNull(gestapo)) {
            LogUtil.error("?????????????????????, pos:", pos);
            // ????????????
            removeBattleIdSet.add(battle.getBattleId());
            return;
        }
        StaticGestapoPlan staticGestapoPlan = StaticWorldDataMgr.getGestapoPlanById(gestapo.getGestapoId());
        if (CheckNull.isNull(staticGestapoPlan)) {
            LogUtil.error("??????????????????Fighter??? ?????????????????????, pos:", pos);
            // ????????????
            removeBattleIdSet.add(battle.getBattleId());
            return;
        }
        defender = fightService.createGestapoBattleDefencer(battle,
                null == staticGestapoPlan ? null : staticGestapoPlan.getFormList());
        Fighter attacker = fightService.createMultiPlayerFighter(battle, null);
        LogUtil.debug("defender=" + defender + ",attacker=" + attacker);
        FightLogic fightLogic = new FightLogic(attacker, defender, true, battle.getType());
        warDataManager.packForm(fightLogic.getRecordBuild(), attacker.forces, defender.forces);
        fightLogic.fight();

        //????????????-??????????????????
        ActivityDiaoChanService.killedAndDeathTask0(attacker, true, true);
        ActivityDiaoChanService.killedAndDeathTask0(defender, true, true);

        boolean atkSuccess = fightLogic.getWinState() == ArmyConstant.FIGHT_RESULT_SUCCESS;
        // ????????????????????????????????????, key:roleId
        Map<Long, ChangeInfo> changeMap = new HashMap<>();
        // ????????????
        Map<Long, List<Award>> recoverArmyAwardMap = new HashMap<>();
        // ????????????
        if (attacker.lost > 0) {
            if (battle.isGestapoBattle()) {
                subBattleHeroArm(attacker.forces, changeMap, AwardFrom.CITY_BATTLE_ATTACK);
                for (BattleRole battleRole : battle.getAtkList()) {
                    long roleId = battleRole.getRoleId();
                    Player player = playerDataManager.getPlayer(roleId);
                    // ????????????
                    activityDataManager.updRankActivity(player, ActivityConst.ACT_ARMY_RANK, attacker.lost);
                    // ????????????????????????
                    honorDailyDataManager.addAndCheckHonorReport2s(player, HonorDailyConstant.COND_ID_14,
                            attacker.lost);
                    // ?????????????????????
                    battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_SUB_HERO_ARMY, attacker.lost);
                    // ?????????????????????
                    List<List<Integer>> armyAward = worldService.attckBanditLostRecvCalc(player, attacker.forces, now,
                            0, WorldConstant.LOST_RECV_CALC_GESTAPO);
                    if (!CheckNull.isEmpty(armyAward)) {
                        List<Award> awards = rewardDataManager.sendReward(player, armyAward, AwardFrom.RECOVER_ARMY);
                        recoverArmyAwardMap.put(roleId, awards);
                    }
                    // ????????????????????????????????????
                    medalDataManager.angelInWhite(attacker, recoverArmyAwardMap);
                    //????????????????????????---????????????
                    seasonTalentService.execSeasonTalentEffect303(attacker, recoverArmyAwardMap);
                }
            }
        }

        // ????????????
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
        CommonPb.Record record = fightLogic.generateRecord();
        rpt.setNightEffect(solarTermsDataManager.getNightEffect() != null);
        rpt.setResult(atkSuccess);
        rpt.setRecord(record);
        // ???????????????????????????????????????
        rpt.setAttack(PbHelper.createRptMan(atkPosLord, atkNick, atkVip, atkLevel));
        // ????????????????????????
        rpt.setAtkSum(PbHelper.createRptSummary(attacker.total, attacker.lost, atkCamp, atkNick, atkPortrait, atkPortraitFrame));
        rpt.setDefCity(PbHelper.createRptCityPb(gestapo.getGestapoId(), pos));
        rpt.setDefSum(PbHelper.createRptSummary(defender.total, defender.lost, battle.getDefCamp(), null, 0, 0));

        LinkedList<Battle> list = warDataManager.getBattlePosMap().get(battle.getPos());
        List<CommonPb.Award> awardProp = new ArrayList<>();
        List<CommonPb.Award> tmp;
        // ??????????????????=????????????+????????????/2??????????????????????????????
        if (battle.isGestapoBattle()) {
            addBattleHeroExp(attacker.forces, AwardFrom.GESTAPO_BATTLE_ATTACK, rpt, true, false, false, changeMap,
                    false, null);
            addBattleHeroExp(defender.forces, AwardFrom.GESTAPO_BATTLE_ATTACK, rpt, false, false, false, changeMap,
                    false, null);
            if (atkSuccess) {
                // ????????????????????????
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
                    activityDataManager.updRankActivity(player, ActivityConst.ACT_GESTAPO_RANK, goal);// ?????????
                    activityDataManager.updGlobalActivity(player, ActivityConst.ACT_ATK_GESTAPO, goal,
                            player.lord.getCamp());// ?????????
                }
                // ??????????????????????????????????????????
                for (Battle battle2 : list) {
                    LogUtil.debug("??????????????????battleId=" + battle2.getBattleId());
                    removeBattleIdSet.add(battle2.getBattleId());
                    // ?????????????????????
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
                // ????????????????????????
                worldDataManager.removeBandit(pos, 2);
                // ??????????????????????????????
                campService.addAndCheckPartySupply(atkPlayer, PartyConstant.SupplyType.KILL_GESTAPO,
                        staticGestapoPlan.getType());
                // ????????????????????????: ???????????????????????????
                worldScheduleService.updateScheduleGoal(atkPlayer, ScheduleConstant.GOAL_COND_ATK_GESTAPO_BANDIT, 0);
            } else {
                gestapo.setStatus(WorldConstant.CITY_STATUS_CALM);
            }
            // ??????
            removeBattleIdSet.add(battle.getBattleId());

            CommonPb.Report.Builder report = worldService.createAtkPlayerReport(rpt.build(), now);
            // ??????????????????
            sendGestapoBattleMail(battle, staticGestapoPlan, atkSuccess, report, awardProp, now, recoverArmyAwardMap);

            // ??????????????????
            List<Integer> posList = new ArrayList<>();
            posList.add(gestapo.getPos());
            // ??????????????????????????????
            EventBus.getDefault().post(
                    new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_AREA_AND_LINE));
        }

        // ?????????????????????????????????
        sendRoleResChange(changeMap);
        // ???????????????
        logBattle(battle, fightLogic.getWinState(), attacker, defender, rpt.getAtkHeroList(), rpt.getDefHeroList(), String.valueOf(staticGestapoPlan.getType()));
    }

    /**
     * ??????????????????????????????
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
            // ????????????
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
            // ?????????
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
     * ?????????????????????
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
        LogUtil.debug("--------------???????????? battle:", battle);

        Player atkPlayer = battle.getSponsor();
        Player defencer = battle.getDefencer();

        CommonPb.RptAtkPlayer.Builder rpt = CommonPb.RptAtkPlayer.newBuilder();
        addCityDefendRoleHeros(battle);// ??????????????????????????????

        Fighter attacker = fightService.createMultiPlayerFighter(battle, null);// ?????????
        Fighter defender = fightService.createCampBattleDefencer(battle, null);// ?????????
        FightLogic fightLogic = new FightLogic(attacker, defender, true, battle.getType());
        warDataManager.packForm(fightLogic.getRecordBuild(), attacker.forces, defender.forces);
        fightLogic.fight();// ????????????????????????

        boolean atkSuccess = fightLogic.getWinState() == ArmyConstant.FIGHT_RESULT_SUCCESS;

        // ----------------------------- ???????????? -----------------------------
        // ????????????
        Map<Long, List<Award>> recoverArmyAwardMap = new HashMap<>();
        // ????????????????????????????????????, key:roleId
        Map<Long, ChangeInfo> changeMap = new HashMap<>();
        if (attacker.lost > 0) {
            subBattleHeroArm(attacker.forces, changeMap, AwardFrom.DECISIVE_BATTLE_AWARD_ATK);
        }
        if (defender.lost > 0) {
            subBattleHeroArm(defender.forces, changeMap, AwardFrom.DECISIVE_BATTLE_AWARD_DEF);
        }
        // ????????????????????????????????????
        medalDataManager.angelInWhite(attacker, recoverArmyAwardMap);
        medalDataManager.angelInWhite(defender, recoverArmyAwardMap);
        //????????????????????????---????????????
        seasonTalentService.execSeasonTalentEffect303(attacker, recoverArmyAwardMap);
        seasonTalentService.execSeasonTalentEffect303(defender, recoverArmyAwardMap);
        // ????????????-????????????????????????
        medalDataManager.sustainTheWarByMeansOfWar(attacker, defender, recoverArmyAwardMap, atkSuccess);

        // ----------------------------- ???????????? -----------------------------

        // ????????????
        CommonPb.Record record = fightLogic.generateRecord();
        rpt.setNightEffect(solarTermsDataManager.getNightEffect() != null);
        rpt.setResult(atkSuccess);
        rpt.setRecord(record);
        // ?????????????????????
        rpt.setAttack(PbHelper.createRptMan(atkPlayer.lord.getPos(), atkPlayer.lord.getNick(), atkPlayer.lord.getVip(),
                atkPlayer.lord.getLevel()));
        rpt.setDefMan(PbHelper.createRptMan(defencer.lord.getPos(), defencer.lord.getNick(), defencer.lord.getVip(),
                defencer.lord.getLevel()));
        // ?????????????????????
        rpt.setAtkSum(PbHelper.createRptSummary(attacker.total, attacker.lost, atkPlayer.lord.getCamp(),
                atkPlayer.lord.getNick(), atkPlayer.lord.getPortrait(), atkPlayer.getDressUp().getCurPortraitFrame()));
        rpt.setDefSum(PbHelper.createRptSummary(defender.total, defender.lost, defencer.lord.getCamp(),
                defencer.lord.getNick(), defencer.lord.getPortrait(), defencer.getDressUp().getCurPortraitFrame()));

        // ??????????????????=????????????+????????????/2??????????????????????????????, ???????????????
        addBattleHeroExp(attacker.forces, AwardFrom.DECISIVE_BATTLE_AWARD_ATK, rpt, true, true, true, changeMap, true,
                null);
        addBattleHeroExp(defender.forces, AwardFrom.DECISIVE_BATTLE_AWARD_DEF, rpt, false, true, true, changeMap, true,
                null);

        Turple<Integer, Integer> atkPos = MapHelper.reducePos(atkPlayer.lord.getPos());
        Turple<Integer, Integer> defPos = MapHelper.reducePos(defencer.lord.getPos());
        CommonPb.Report.Builder report = worldService.createAtkPlayerReport(rpt.build(), now);

        if (atkSuccess) {
            // ???????????????????????????????????????????????????????????????
            playerHitFly(battle.getDefencer(), battle.getBattleType(), atkPlayer);
            airshipService.changAirshipBelong(battle.getDefencer().roleId, atkPlayer.roleId);
            mailDataManager.sendNormalMail(battle.getDefencer(), MailConstant.MOLD_HIT_FLY,
                    TimeHelper.getCurrentSecond(), atkPlayer.lord.getNick(), atkPlayer.lord.getNick());
            // ?????????++
            atkPlayer.common.incrKillNum();
            DecisiveInfo decisiveInfo = defencer.getDecisiveInfo();
            if (decisiveInfo != null) {
                decisiveInfo.setFlyTime(now);
                decisiveInfo.setFlyRole(atkPlayer.roleId);
            }
            // ?????????????????????
            restrictTaskService.updatePlayerDailyRestrictTask(atkPlayer, TaskType.COND_BATTLE_CITY_LV_CNT, 1);
        } else { // ???????????????
        }
        // ??????????????????
        mailDataManager.sendReportMail(atkPlayer, report,
                atkSuccess ? MailConstant.DECISIVE_BATTLE_ATK_SUCCESS : MailConstant.DECISIVE_BATTLE_ATK_FAIL, null,
                now, recoverArmyAwardMap, atkPlayer.lord.getNick(), defencer.lord.getCamp(), defencer.lord.getLevel(),
                defencer.lord.getNick(), atkPlayer.lord.getNick(), atkPos.getA(), atkPos.getB(),
                defencer.lord.getLevel(), defencer.lord.getNick(), defPos.getA(), defPos.getB());
        // ??????????????????
        mailDataManager.sendReportMail(defencer, report,
                atkSuccess ? MailConstant.DECISIVE_BATTLE_DEF_FAIL : MailConstant.DECISIVE_BATTLE_DEF_SUCCESS, null,
                now, recoverArmyAwardMap, defencer.lord.getNick(), atkPlayer.lord.getCamp(), atkPlayer.lord.getLevel(),
                atkPlayer.lord.getNick(), atkPlayer.lord.getLevel(), atkPlayer.lord.getNick(), atkPos.getA(),
                atkPos.getB());

        // ??????????????????
        removeBattleIdSet.add(battle.getBattleId());
        // ??????????????????
        atkPlayer.getDecisiveInfo().setDecisive(false);
        defencer.getDecisiveInfo().setDecisive(false);
        // ???????????????????????????
        honorDailyService.addAndCheckBattleHonorReports(atkPlayer, defencer, atkSuccess, battle.getType());
        retreatBattleArmy(battle, now);
        // ???????????????
        List<Integer> posList = new ArrayList<>();
        posList.add(atkPlayer.lord.getPos());
        posList.add(defencer.lord.getPos());
        // ??????????????????????????????
        EventBus.getDefault()
                .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_AREA_TYPE));
        // ?????????????????????????????????
        sendRoleResChange(changeMap);
        // ???????????????
        logBattle(battle, fightLogic.getWinState(), attacker, defender, rpt.getAtkHeroList(), rpt.getDefHeroList());
        LogUtil.war(">>>>>>>>>>>>>>????????????????????????>>>>>>>>>>>>>>");
    }

    /**
     * ??????????????????????????????????????????????????????????????? ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????Army??? ??????NPC??????????????????
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

        LogUtil.debug("????????????, battle:", battle);

        int pos = battle.getPos();
        // ????????????????????????????????????????????????
        if (battle.isCityBattle() && battle.getDefencer() != null) {
            // ????????????
            Effect effect = battle.getDefencer().getEffect().get(EffectConstant.PROTECT);
            if (effect != null && effect.getEndTime() > now) {
                LogUtil.debug("????????????????????????????????????battle:", battle);
                removeBattleIdSet.add(battle.getBattleId());
                return;
            }
        }

        // ??????????????????????????? ??????????????????????????????????????????????????????
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
        // // ????????????????????????????????????
        // mailDataManager.sendReportMail(battle.getSponsor(), null,
        // MailConstant.MOLD_ATK_CAMP_FAIL, null, now,
        // atkLord.getNick(), cityId, atkLord.getNick(), atkPos.getA(),
        // atkPos.getB(), cityId, defPos.getA(),
        // defPos.getB());
        // // ????????????????????????????????????
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

        // ????????????
        int cityId = 0;
        City city = null;
        Fighter defender = null;
        StaticCity staticCity = StaticWorldDataMgr.getCityByPos(pos);
        // if (battle.isAtkNpc()) {
        // if (null == staticCity) {
        // LogUtil.error("???????????????Fighter??? ?????????????????????, pos:", pos);
        // // ????????????
        // removeBattleIdSet.add(battle.getBattleId());
        // return;
        // }
        // cityId = staticCity.getCityId();
        // city = worldDataManager.getCityById(cityId);
        // defender = fightService.createCityNpcFighter(cityId);
        // } else {
        if (battle.isCityBattle()) {// ??????????????????????????????????????????????????????
            addCityDefendRoleHeros(battle);
        } else {

            if (null == staticCity) {
                LogUtil.error("???????????????Fighter??? ?????????????????????, pos:", pos);
                // ????????????
                removeBattleIdSet.add(battle.getBattleId());
                return;
            }

            cityId = staticCity.getCityId();

            // ?????????????????????NPC?????????????????????????????????
            city = worldDataManager.getCityById(cityId);
            if (city.getProtectTime() > now) {
                LogUtil.debug("???????????????, ????????????battleId=" + battle.getBattleId() + ",city=" + city);
                // ????????????
                removeBattleIdSet.add(battle.getBattleId());
                return;
            }
        }

        defender = fightService.createCampBattleDefencer(battle, null == city ? null : city.getFormList());

        // ????????????????????????????????????
        City atkCity = worldDataManager.getCityById(battle.getAtkCity());
        // Fighter attacker = fightService.createMultiPlayerFighter(atkRoleList,
        // battle.getAtkHeroIdMap(),
        // null == atkCity ? null : getFormList4City(atkCity.getCityLv()));
        Fighter attacker = fightService.createMultiPlayerFighter(battle,
                null == atkCity ? null : getFormList4City(atkCity.getCityLv()));
        LogUtil.debug("atkCity=" + atkCity + ",attacker=" + attacker);
        FightLogic fightLogic = new FightLogic(attacker, defender, true, battle.getType());
        warDataManager.packForm(fightLogic.getRecordBuild(), attacker.forces, defender.forces);
        fightLogic.fight();// ????????????????????????

        //????????????-??????????????????
        ActivityDiaoChanService.killedAndDeathTask0(attacker, true, true);
        ActivityDiaoChanService.killedAndDeathTask0(defender, true, true);

        // ???????????? <roleId, <heroId, exploit>>
        HashMap<Long, Map<Integer, Integer>> exploitAwardMap = new HashMap<>();
        // ??????????????????-???????????? ??????
        if (battle.getType() == WorldConstant.BATTLE_TYPE_CAMP) {// ??????
            medalDataManager.militaryMeritIsProminent(attacker, defender, exploitAwardMap);
        }

        boolean atkSuccess = fightLogic.getWinState() == ArmyConstant.FIGHT_RESULT_SUCCESS;

        // ????????????
        Map<Long, List<Award>> recoverArmyAwardMap = new HashMap<>();
        // ????????????????????????????????????, key:roleId
        Map<Long, ChangeInfo> changeMap = new HashMap<>();

        // ???????????????????????????
        if (attacker.lost > 0) {
            dailyAttackTaskService.addPlayerDailyAttackOther(attacker.forces);
            if (battle.isCityBattle()) {
                subBattleHeroArm(attacker.forces, changeMap, AwardFrom.CITY_BATTLE_ATTACK);
            } else {
                subBattleHeroArm(attacker.forces, changeMap, AwardFrom.CAMP_BATTLE_ATTACK);
            }
        }
        // ?????????????????????
        if (defender.lost > 0) { // ????????????????????????
            dailyAttackTaskService.addPlayerDailyAttackOther(defender.forces);
            if (battle.isCityBattle()) {
                subBattleHeroArm(defender.forces, changeMap, AwardFrom.CITY_BATTLE_DEFEND);
            } else {
                if (battle.isAtkNpc()) {
                    if (atkSuccess) {// ???????????????
                        subBattleNpcArm(defender.forces, worldDataManager.getCityById(cityId));
                    }
                } else {
                    // ?????????npc?????????
                    subBattleNpcArm(defender.forces, worldDataManager.getCityById(cityId));
                    subBattleHeroArm(defender.forces, changeMap, AwardFrom.CAMP_BATTLE_DEFEND);
                }
            }
        }

        // ????????????????????????????????????
        medalDataManager.angelInWhite(attacker, recoverArmyAwardMap);
        medalDataManager.angelInWhite(defender, recoverArmyAwardMap);

        //????????????????????????---????????????
        seasonTalentService.execSeasonTalentEffect303(attacker, recoverArmyAwardMap);
        seasonTalentService.execSeasonTalentEffect303(defender, recoverArmyAwardMap);

        // ????????????-????????????????????????
        medalDataManager.sustainTheWarByMeansOfWar(attacker, defender, recoverArmyAwardMap, atkSuccess);

        // ????????????
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
        CommonPb.Record record = fightLogic.generateRecord();
        rpt.setNightEffect(solarTermsDataManager.getNightEffect() != null);
        rpt.setResult(atkSuccess);
        rpt.setRecord(record);
        // ???????????????????????????????????????
        rpt.setAttack(PbHelper.createRptMan(atkPosLord, atkNick, atkVip, atkLevel));
        // ????????????????????????
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
            // ??????????????????
            if (battle.isCityBattle()) {
                dropList = buildingDataManager.dropList4War(atkPlayer, battle.getDefencer(), loseList);// ????????????
                // ?????????
                // ??????????????????

                // ????????????-???????????? ????????????
                medalDataManager.peacekeepingForces(defender, battle.getDefencer());

                // ?????????????????????????????????10?????????
                dropList.add(
                        Award.newBuilder().setType(AwardType.MONEY).setId(AwardType.Money.EXP).setCount(10).build());
                // ??????????????????????????????
                Award award = checkPlaneChipAward(atkPlayer);
                if (!CheckNull.isNull(award)) {
                    dropList.add(award);
                }
                // ????????????????????????????????????
                rewardDataManager.sendRewardByAwardList(atkPlayer, dropList, AwardFrom.CITY_BATTLE_ATTACK);
                // ????????????????????????
                List<Award> actHitDrop = activityDataManager
                        .getActHitDrop(atkPlayer, 0, StaticActBandit.ACT_HIT_DROP_TYPE_1);
                if (!CheckNull.isEmpty(actHitDrop)) {
                    dropList.addAll(actHitDrop);
                }
            }
            // ?????????????????????????????????
            recordMap = recordRoleFight(attacker.forces, true);

            // ???????????????????????? ???????????????
            recordPartyBattle(battle, battle.getType(), battle.getAtkCamp(), true);
        } else {
            recordMap = recordRoleFight(defender.forces, false);
        }

        if (!battle.isCityBattle()) {// ?????????????????????????????????????????????
            dropMap = sendResourceReward(recordMap, changeMap);
        }

        // ??????????????????=????????????+????????????/2??????????????????????????????, ???????????????
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
                // ???????????????????????????????????????????????????????????????
                playerHitFly(battle.getDefencer(), battle.getBattleType(), atkPlayer);
                airshipService.changAirshipBelong(battle.getDefencer().roleId, atkPlayer.roleId);
                mailDataManager.sendNormalMail(battle.getDefencer(), MailConstant.MOLD_HIT_FLY,
                        TimeHelper.getCurrentSecond(), atkPlayer.lord.getNick(), atkPlayer.lord.getNick());
                // ??????????????????
                wallService.retreatArmy(pos, true, battle.getDefencer());

                // ??????????????????????????????????????????
                for (Battle battle2 : list) {
                    LogUtil.debug("??????????????????battleId=" + battle2.getBattleId());
                    removeBattleIdSet.add(battle2.getBattleId());
                    if (battle != battle2) {
                        sendOtherAtkPlayer(battle2, now);// ????????????????????????????????????????????????
                    }
                }
                taskDataManager.updTask(atkPlayer, TaskType.COND_BATTLE_CITY_LV_CNT, 1);
                battlePassDataManager.updTaskSchedule(atkPlayer.roleId, TaskType.COND_BATTLE_CITY_LV_CNT, 1);
                royalArenaService.updTaskSchedule(atkPlayer.roleId, TaskType.COND_BATTLE_CITY_LV_CNT, 1);
                if (defLord != null) {
                    activityDataManager.updDay7ActSchedule(atkPlayer, ActivityConst.ACT_TASK_ATK,
                            battle.getDefencer().building.getCommand());
                    // activityDataManager.updAtkCityActSchedule(atkPlayer,
                    // ActivityConst.ACT_TASK_ATK, // ????????????
                    // battle.getDefencer().building.getCommand());
                    for (long roles : battle.getAtkRoles()) {
                        Player actPlayer = playerDataManager.getPlayer(roles);
                        if (actPlayer != null) {
                            activityDataManager.updDay7ActSchedule(actPlayer, ActivityConst.ACT_TASK_JOIN_OR_ATK,
                                    battle.getDefencer().building.getCommand());
                            activityDataManager.updAtkCityActSchedule(actPlayer, ActivityConst.ACT_TASK_JOIN_OR_ATK, // ????????????
                                    battle.getDefencer().building.getCommand());
                            // ?????????????????????
                            restrictTaskService.updatePlayerDailyRestrictTask(actPlayer, TaskType.COND_BATTLE_CITY_LV_CNT, 1);
                        }
                    }
                }
                // ?????????++
                atkPlayer.common.incrKillNum();
                // ???????????????
                activityDataManager.updRankActivity(atkPlayer, ActivityConst.ACT_CITY_BATTLE_RANK, 1);
            } else {
                // ??????????????????, ???????????????????????????
                removeBattleIdSet.add(battle.getBattleId());
                // ????????????????????????
                wallService.retreatArmy(pos, false, battle.getDefencer());
            }

            int defLv = battle.getDefencer() == null ? 1 : battle.getDefencer().lord.getLevel();

            taskDataManager.updTask(atkPlayer, TaskType.COND_ATTCK_PLAYER_CNT, 1, defLv);
            royalArenaService.updTaskSchedule(atkPlayer.roleId, TaskType.COND_ATTCK_PLAYER_CNT, 1);
            // ???????????????????????????
            honorDailyService.addAndCheckHonorReports(atkPlayer, battle.getDefencer(), atkSuccess, battle.getType());

            // ????????????
            battle.getAtkList().stream().mapToLong(role -> role.getRoleId()).distinct().forEach(roleId -> {
                Player actPlayer = playerDataManager.getPlayer(roleId);
                activityDataManager.updAtkCityActSchedule(actPlayer, ActivityConst.ACT_TASK_ATK_AND_JOIN, // ??????????????????
                        battle.getDefencer().building.getCommand());
            });
            battle.getDefList().stream().mapToLong(role -> role.getRoleId()).distinct().forEach(roleId -> {
                Player defPalyer = playerDataManager.getPlayer(roleId);
                if (!battle.getDefencer().roleId.equals(defPalyer.roleId)) {
                    activityDataManager.updAtkCityActSchedule(defPalyer, ActivityConst.ACT_TASK_JOIN_OR_DEF, // ??????????????????
                            battle.getDefencer().building.getCommand());
                }
            });
            // ???????????????, ???????????????
            PushMessageUtil.pushMessage(battle.getDefencer().account, PushConstant.ATTACKED_AND_BEATEN);
        } else {
            if (null == city) {
                LogUtil.error("?????????????????????????????????????????????, cityId:", cityId);
            } else {
                if (atkSuccess) {
                    // ?????????????????????????????????????????????
                    int preCityCamp = city.getCamp();
                    city.setCamp(battle.getAtkCamp());
                    city.cleanOwner(true);
                    LogUtil.debug("?????????????????????????????????, cityId:", cityId, "camp:", battle.getAtkCamp());
                    if (staticCity.getType() == WorldConstant.CITY_TYPE_KING) {// ??????
                    } else if (staticCity.getType() == WorldConstant.CITY_TYPE_HOME) {// ??????
                        // ??????4??????????????????????????????
                        city.setNextAtkBeginTime(now);
                        city.setStatus(WorldConstant.CITY_STATUS_CALM);
                        worldDataManager.addCampSuperMine(city.getCamp());
                    } else {// ?????????
                        // ????????????????????????7???
                        List<Long> joinRoles = battle.getAtkList().stream().map(r -> r.getRoleId()).distinct()
                                .collect(Collectors.toList());// ????????????????????????
                        if (staticCity.getType() == WorldConstant.CITY_TYPE_8 && worldDataManager
                                .getPeoPle4MiddleCity(battle.getAtkCamp()) > WorldConstant.CITY_TYPE_8_MAX) {
                            // ?????????npc??????????????????
                            city.setCamp(Constant.Camp.NPC);
                            city.setStatus(WorldConstant.CITY_STATUS_FREE);
                            city.setProtectTime(now + WorldConstant.CITY_PROTECT_TIME * TimeHelper.MINUTE);
                        } else {
                            // ????????????
                            city.setStatus(WorldConstant.CITY_STATUS_FREE);
                            city.setCloseTime(now + WorldConstant.CITY_FREE_TIME);
                            city.startCampaign(now);
                            city.getAttackRoleId().addAll(joinRoles);
                            city.setProtectTime(now + WorldConstant.CITY_PROTECT_TIME * TimeHelper.MINUTE);
                        }
                        // ????????????
                        if (preCityCamp == Constant.Camp.NPC) {
                            final City cityF = city;
                            joinRoles.forEach(roleId -> {
                                Player player = playerDataManager.getPlayer(roleId);
                                // npc??????????????????????????????????????????????????????????????????
                                if (player != null && player.checkNpcFirstKillReward(staticCity.getCityId())) {
                                    cityF.getFirstKillReward().put(roleId, 0);
                                    player.addNpcFirstKillRecord(staticCity.getCityId());
                                }
                            });
                        } else {
                            // ??????????????????????????????
                            city.getFirstKillReward().clear();
                        }
                        // ????????????
                        firstBloodCity = checkUpdAreafirstKill(staticCity, city, battle.getAtkLordId(), joinRoles) ? LogParamConstant.IS_FIRST_KILL_CITY : LogParamConstant.IS_NOT_FIRST_KILL_CITY;
                        // ?????? ??????????????????
                        honorDailyService.addAndCheckHonorReport2s(battle.getSponsor(), HonorDailyConstant.COND_ID_2);
                    }
                    if (Constant.START_SOLAR_TERMS_CITY_TYPE == staticCity.getType()) {
                        // ??????????????????
                        if (!solarTermsDataManager.isSolarTermsBegin()) {
                            solarTermsDataManager.setSolarTermsBeginTime(now);
                        }
                    }
                    // ?????????????????????????????????????????????????????????
                    for (Battle battle2 : list) {
                        LogUtil.debug("??????????????????battleId=" + battle2.getBattleId() + ",list=" + list.size());
                        removeBattleIdSet.add(battle2.getBattleId());
                    }

                    // ??????????????????????????????,????????????????????????
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

                    // ??????????????????
                    Turple<Integer, Integer> xy = staticCity.getCityPosXy();
                    // ?????????????????????
                    PartyLogHelper.addPartyLog(battle.getAtkCamp(), PartyConstant.LOG_CITY_CONQUERED, city.getCityId(),
                            xy.getA(), xy.getB(), atkNick);

                    if (!battle.isAtkNpc()) {// ??????????????????????????????
                        PartyLogHelper.addPartyLog(battle.getDefCamp(), PartyConstant.LOG_CITY_BREACHED,
                                city.getCityId(), xy.getA(), xy.getB(), battle.getAtkCamp(), atkNick);
                    }

                    // 2019-3-9 ???????????????????????????????????????
                    // ??????????????????
                    // if (atkPlayer != null &&
                    // taskDataManager.isOpenWorldTask(atkPlayer) &&
                    // StaticWorldDataMgr.getWorldTask(worldDataManager.getWorldTask().getWorldTaskId().get()
                    // + 1)
                    // != null) {
                    // taskDataManager.updWorldTask(TaskType.WORLD_TASK_TYPE_CITY,
                    // 1, staticCity.getType(),
                    // atkPlayer.lord.getCamp());
                    // }

                    // ????????????
                    // ??????????????????????????????
                    battle.getAtkList().stream().mapToLong(BattleRole::getRoleId).distinct().forEach(roleId -> {
                        Player actPlayer = playerDataManager.getPlayer(roleId);
                        // ???????????????????????????
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
                                staticCity.getType());// ???????????????
                        activityDataManager.updAtkCityActSchedule(actPlayer, ActivityConst.ACT_TASK_CITY);// ????????????????????????
                        LogUtil.debug("??????????????????????????????????????? ????????????=" + actPlayer.day7Act.getTankTypes() + ",key="
                                + (ActivityConst.ACT_TASK_ATTACK + "_" + staticCity.getType()));

                        //????????????-???????????????????????????
                        ActivityDiaoChanService.completeTask(actPlayer, ETask.JOIN_CITY_WAR, staticCity.getType());
                        TaskService.processTask(actPlayer, ETask.JOIN_CITY_WAR, staticCity.getType());
                    });
                    // ??????city??????
                    cityService.syncPartyCity(city, staticCity);
                    // ??????????????????????????????
                    campService.addAndCheckPartySupply(atkPlayer, PartyConstant.SupplyType.CONQUER_CITY,
                            staticCity.getType());
                    // ????????????????????????
                    worldScheduleService.updateScheduleGoal(atkPlayer, ScheduleConstant.GOAL_COND_CONQUER_CITY,
                            staticCity.getCityId());
                } else {
                    // ????????????????????????
                    removeBattleIdSet.add(battle.getBattleId());
                    // ??????????????????
                    LinkedList<Battle> cityBattleList = warDataManager.getBattlePosMap().get(staticCity.getCityPos());
                    if (!CheckNull.isEmpty(cityBattleList)) {
                        if (cityBattleList.size() == 1) { // ??????????????????????????????,????????????????????????
                            city.setStatus(WorldConstant.CITY_STATUS_CALM);
                        }
                    } else {
                        // ??????else ???????????????????????????????????????
                        city.setStatus(WorldConstant.CITY_STATUS_CALM);
                    }

                    // ???????????????????????????????????????????????????????????????
                    if (list.size() > 1) {
                        Battle battle2 = list.get(1);
                        city.setAttackCamp(battle2.getAtkCamp());
                    }
                    // ???????????????????????????????????????
                    chatDataManager.sendSysChat(ChatConst.CHAT_CITY_DEMAGED, battle.getDefCamp(), 0, cityId);
                }

                // ??????????????????????????????
                battle.getAtkList().stream().mapToLong(BattleRole::getRoleId).distinct().forEach(roleId -> {
                    Player player = playerDataManager.getPlayer(roleId);
                    if (player != null) {
                        activityDataManager.updActivity(player, ActivityConst.ACT_ATTACK_CITY, 1, 0, true);
                        activityDataManager.updAtkCityActSchedule(player, ActivityConst.ACT_TASK_JOIN_ATK,
                                staticCity.getType()); // ??????????????????
                        activityRobinHoodService.updateTaskSchedule(player, ActivityConst.ACT_TASK_JOIN_ATK, 1, staticCity.getType());
                        activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_JOIN_ATK, staticCity.getType());
                        taskDataManager.updTask(player, TaskType.COND_JOIN_CAMP_BATTLE_41, 1, atkSuccess ? 1 : 0);
                        battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_JOIN_CAMP_BATTLE_41, 1, staticCity.getType());
                        royalArenaService.updTaskSchedule(player.roleId, TaskType.COND_JOIN_CAMP_BATTLE_41, 1, staticCity.getType());
                        if (staticCity != null) {
                            taskDataManager.updTask(player, TaskType.COND_22, 1, staticCity.getType());
                            taskDataManager.updTask(player, TaskType.COND_520, attacker.getForces().stream().filter(e -> e.ownerId == roleId).mapToInt(e -> e.killed).sum());
                            taskDataManager.updTask(player,TaskType.COND_521,1,staticCity.getType());
                            //????????????
                            taskDataManager.updTask(player,TaskType.COND_996,1);
                        }
                    }
                });
                battle.getDefList().stream().mapToLong(BattleRole::getRoleId).distinct().forEach(roleId -> {
                    Player player = playerDataManager.getPlayer(roleId);
                    if (player != null) {
                        activityDataManager.updActivity(player, ActivityConst.ACT_ATTACK_CITY, 1, 0, true);
                        activityDataManager.updAtkCityActSchedule(player, ActivityConst.ACT_TASK_JOIN_DEF,
                                staticCity.getType()); // ??????????????????
                        activityRobinHoodService.updateTaskSchedule(player, ActivityConst.ACT_TASK_JOIN_ATK, 1, staticCity.getType());
                        activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_JOIN_ATK, staticCity.getType());
                        taskDataManager.updTask(player, TaskType.COND_JOIN_CAMP_BATTLE_41, 1, atkSuccess ? 0 : 1);
                        battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_JOIN_CAMP_BATTLE_41, 1, staticCity.getType());
                        royalArenaService.updTaskSchedule(player.roleId, TaskType.COND_JOIN_CAMP_BATTLE_41, 1, staticCity.getType());
                        taskDataManager.updTask(player, TaskType.COND_520, attacker.getForces().stream().filter(e -> e.ownerId == roleId).mapToInt(e -> e.killed).sum());
                        taskDataManager.updTask(player,TaskType.COND_521,1,staticCity.getType());
                    }
                });

                // ??????????????????
                List<Integer> posList = new ArrayList<>();
                posList.add(staticCity.getCityPos());
                // ??????????????????????????????
                EventBus.getDefault().post(
                        new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));
            }

        }
        // ????????????????????????
        Optional.of(campDataManager.getParty(battle.getAtkCamp()))
                .ifPresent((party) -> campService.checkHonorRewardAndSendSysChat(party));

        // ??????????????????
        if (battle.isCityBattle()) {// ??????????????????????????????????????????????????????????????????????????????
            sendCityBattleMail(battle, cityId, atkLord, defLord, atkPos, defPos, atkSuccess, report, dropList, loseList,
                    now, recoverArmyAwardMap);
            // ???????????????????????????
            autoFillArmy(battle.getDefencer());
        } else {// ??????????????????????????????????????????????????????????????????
            sendCampBattleMail(battle, cityId, atkLord, defLord, atkPos, defPos, atkSuccess, report, dropMap, now,
                    recoverArmyAwardMap);
        }

        // ?????????????????????????????????
        sendRoleResChange(changeMap);
        // ???????????????
        logBattle(battle, fightLogic.getWinState(), attacker, defender, rpt.getAtkHeroList(), rpt.getDefHeroList(), String.valueOf(cityId), firstBloodCity);
    }

    /**
     * ?????????????????????????????????
     *
     * @param player
     */
    public Award checkPlaneChipAward(Player player) throws MwException {
        Award award = null;
        if (CheckNull.isNull(player)) {
            return award;
        }
        final boolean[] flag = new boolean[1];
        // ??????????????????
        List<Hero> battleHeros = player.getAllOnBattleHeros();
        if (CheckNull.isEmpty(battleHeros)) {
            return award;
        }
        // ????????????????????????????????????
        List<Integer> battlePlane = battleHeros.stream()
                .filter(hero -> hero.getState() == ArmyConstant.ARMY_STATE_BATTLE)
                .flatMap(hero -> hero.getWarPlanes().stream()).collect(Collectors.toList());
        if (CheckNull.isEmpty(battlePlane)) {
            return award;
        }
        // ???????????????????????????
        battlePlane = battlePlane.stream().filter(planeId -> {
            try {
                flag[0] = true;
                WarPlane plane = player.checkWarPlaneIsExist(planeId);
                if (!CheckNull.isNull(plane)) {
                    StaticPlaneUpgrade maxLv = StaticWarPlaneDataMgr
                            .getPlaneMaxLvByFilter(e -> plane.getType() == e.getPlaneType() && e.getNextId() == 0
                                    && CheckNull.isEmpty(e.getReformNeed()));
                    if (!CheckNull.isNull(maxLv) && maxLv.getPlaneId() == plane.getPlaneId()) { // ?????????????????????????????????
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
        if (cnt >= PlaneConstant.PLANE_HIT_FLY_AWARD.get(1)) { // ?????????????????????????????????????????????
            return award;
        }
        if (RandomHelper.isHitRangeIn10000(PlaneConstant.PLANE_HIT_FLY_AWARD.get(0))) {
            Collections.shuffle(battlePlane); // ????????????
            int planeId = battlePlane.get(0);
            WarPlane warPlane = player.checkWarPlaneIsExist(planeId);
            if (!CheckNull.isNull(warPlane)) {
                StaticPlaneInit sPlaneInit = StaticWarPlaneDataMgr.getPlaneInitByType(warPlane.getType());
                if (!CheckNull.isNull(sPlaneInit)) {
                    List<Integer> synthesis = sPlaneInit.getSynthesis(); // ????????????
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
     * ???????????????????????????,???????????????
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
            // ??????????????????????????????????????????
            activityTriggerService.battleCampTriggerGift(staticCity.getType());
        }

        // ????????????????????????
        if (!area.isInKillList(cityType)) {
            List<List<Integer>> confAwd = Constant.CITY_TYPE_KILL_REWARD.get(cityType);
            if (CheckNull.isEmpty(confAwd)) {
                LogUtil.error("???????????????????????????: cityType" + cityType);
                return false;
            }

            List<Award> awards = new ArrayList<>(PbHelper.createAwardsPb(confAwd));
            // ???????????????
            if (!awards.isEmpty()) {
                // ?????????Area???????????????????????????
                List<Long> alreadyAward = worldDataManager.getAreaMap().values().stream()
                        .filter(a -> !CheckNull.isEmpty(a.getCityFirstKill()))
                        .flatMap(a -> a.getCityFirstKill().entrySet().stream()
                                // ??????????????????, ??????????????????????????????
                                .filter(en -> Integer.valueOf(en.getKey().split("_")[0]) == cityType && !CheckNull.isEmpty(en.getValue()))
                                // ????????????????????????
                                .map(en -> en.getValue().get(WorldConstant.KILL_ATKLIST))
                                .flatMap(Collection::stream))
                        .distinct().collect(Collectors.toList());


                // ???????????????????????????
                joinRoles.forEach(roleId -> {
                    if (alreadyAward.contains(roleId)) { // ??????????????????????????????, ??????????????????
                        return;
                    }
                    Player player = playerDataManager.getPlayer(roleId);
                    mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_FIRST_KILL_REWARD,
                            AwardFrom.CITY_FIRST_KILL_AWARD, TimeHelper.getCurrentSecond(), city.getCityId(),
                            city.getCityId());
                });
            }
            // ???????????????
            chatDataManager.sendSysChat(ChatConst.CHAT_CITY_FIRST_KILL, sponsor.lord.getCamp(), 0,
                    sponsor.lord.getCamp(), sponsor.lord.getNick(), staticCity.getCityId(), staticCity.getArea(),
                    cityType);

            // ???????????????, ???????????????
            sponsorList.add(sponsorId);
            HashMap<String, List<Long>> killMap = new HashMap<>();
            killMap.put(WorldConstant.KILL_SPONSOR, sponsorList);
            // ????????????????????????
            killMap.put(WorldConstant.KILL_ATKLIST, joinRoles);
            String cityInfo = cityType + "_" + staticCity.getCityId();
            area.getCityFirstKill().put(cityInfo, killMap);

            //????????????-????????????
            joinRoles.forEach(roleId -> {
                Player player = playerDataManager.getPlayer(roleId);
                ActivityDiaoChanService.completeTask(player, ETask.CITY_FIRSTKILLED);
                TaskService.processTask(player, ETask.CITY_FIRSTKILLED);
                //??????-????????????
                titleService.processTask(player,ETask.CITY_FIRSTKILLED);
            });

            return true;
        }

        return false;
    }

    /**
     * ??????????????????
     *
     * @param battle
     * @param winState ???????????????
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
        // ????????????????????????
        battle.getAtkList().stream().map(rb -> rb.getRoleId()).distinct().map(rId -> playerDataManager.getPlayer(rId))
                .filter(p -> p != null).forEach(player -> {
                    LogLordHelper.otherLog("battle", player.account.getServerId(), player.roleId, "atk", battleId, type,
                            win, pos, sponsorId, defencerId, atkCamp);
                    //????????????
                    EventDataUp.battle(player.account, player.lord, attacker, "atk", battleId, type, win, Long.parseLong(sponsorId), atkHeroList, param);
                });

        // ????????????????????????
        battle.getDefList().stream().map(rb -> rb.getRoleId()).distinct().map(rId -> playerDataManager.getPlayer(rId))
                .filter(p -> p != null).forEach(player -> {
                    LogLordHelper.otherLog("battle", player.account.getServerId(), player.roleId, "def", battleId, type,
                            win, pos, sponsorId, defencerId, atkCamp);
                    //????????????
                    if (defender != null) {
                        EventDataUp.battle(player.account, player.lord, defender, "def", battleId, type, win, Long.parseLong(sponsorId), defHeroList, param);
                    }
                });

    }

    /**
     * ????????????,?????????????????????????????????
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
            LogUtil.error("?????????????????? roleId:", player.roleId, e);
        }
    }

    /**
     * ??????????????????????????????????????????????????????
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
     * ????????????????????????
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
//            // ????????????+1??????????????????,???????????????
//            if (battleType == WorldConstant.BATTLE_TYPE_CAMP) {
//                member.campBattle();
//                party.addPartyHonorRank(PartyConstant.RANK_TYPE_CAMP, player.roleId, player.lord.getNick(),
//                        member.getCampBattle(), now);
//            }
//        }
        //fix ?????????????????????????????? ???????????????????????????
        list.stream()
                .map(BattleRole::getRoleId)
                // ??????, ??????????????????????????????????????????, ??????????????????????????????
                .distinct()
                .map(playerDataManager::getPlayer)
                .forEach(p -> {
                    CampMember campMember = campDataManager.getCampMember(p.roleId);
                    // ????????????+1??????????????????,???????????????
                    if (battleType == WorldConstant.BATTLE_TYPE_CAMP) {
                        campMember.campBattle();
                        party.addPartyHonorRank(PartyConstant.RANK_TYPE_CAMP, p.roleId, p.lord.getNick(),
                                campMember.getCampBattle(), now);
                    }
                });
        // ?????????,???????????????????????????????????????
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
     * ?????????????????????????????????
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
     * ???????????????????????????????????????????????????
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
     * ???????????????????????????
     *
     * @param recordMap
     * @param changeMap
     * @return ?????????????????????????????????????????????????????????
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
                continue; // ??????????????????????????????????????????????????????
            info = changeMap.get(record.getRoleId());
            if (null == info) {
                info = ChangeInfo.newIns();
                changeMap.put(record.getRoleId(), info);
            }

            // ??????????????????????????????????????????????????????????????????????????????????????????????????????=????????????+????????????*2K?????????????????????=????????????+????????????*1.5K
            // ?????????????????????????????????
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
     * ???????????????????????????????????? ?????????????????????????????????????????????????????????
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
                LogUtil.debug("?????????????????????????????????,player is null=" + roleId);
                continue;
            }
            // player.battleMap.remove(battle.getPos());
            LogUtil.debug("?????????????????????????????????pos=" + battle.getPos() + ",map=" + player.battleMap);
            HashSet<Integer> battleIds = player.battleMap.get(battle.getPos());
            if (battleIds != null) {
                if (battleIds.size() > 1) {
                    battleIds.remove(battle.getBattleId());
                } else {
                    player.battleMap.remove(battle.getPos());
                }
            }
            LogUtil.debug("?????????????????????????????????pos=" + battle.getPos() + ",map=" + player.battleMap);
        }
        for (BattleRole map : battle.getDefList()) {
            roleId = map.getRoleId();
            Player player = playerDataManager.getPlayer(roleId);
            if (player == null) {
                LogUtil.debug("?????????????????????????????????,player is null=" + roleId);
                continue;
            }
            // player.battleMap.remove(battle.getPos());
            LogUtil.debug("def?????????????????????????????????pos=" + battle.getPos() + ",map=" + player.battleMap);
            HashSet<Integer> battleIds = player.battleMap.get(battle.getPos());
            if (battleIds != null) {
                if (battleIds.size() > 1) {
                    battleIds.remove(battle.getBattleId());
                } else {
                    player.battleMap.remove(battle.getPos());
                }
            }
            LogUtil.debug("def?????????????????????????????????pos=" + battle.getPos() + ",map=" + player.battleMap);
        }
    }

    /**
     * ????????????????????????????????????
     *
     * @param player    ??????????????????
     * @param type      battleType
     * @param atkPlayer ???????????????
     */
    private void playerHitFly(Player player, int type, Player atkPlayer) {
        if (null == player) {
            return;
        }
        int prePos = player.lord.getPos();
        int newPos = worldDataManager.randomPlayerPos(player, prePos, player.lord.getCamp());

        // ??????????????????
        player.lord.setPos(newPos);
        int newArea = MapHelper.getAreaIdByPos(newPos);
        player.lord.setArea(newArea);
        worldDataManager.removePlayerPos(prePos, player);
        worldDataManager.putPlayer(player);
        if (type != WorldConstant.BATTLE_TYPE_DECISIVE_BATTLE
                && BuildingDataManager.getBuildingLv(BuildingType.WALL, player) > 0) {// ???????????????????????????????????????
            player.setFireState(true);// ??????????????????
        }
        if (newArea != MapHelper.getAreaIdByPos(prePos)) {// ???????????????
            for (Army army : player.armys.values()) { // ????????????????????????
                worldDataManager.addMarch(new March(player, army));
            }
        }

        // ???????????????
        if (player.isFirstHitFly()) {
            if (player.isLogin) {
                // ???????????????
                activityService.checkTriggerGiftSync(ActivityConst.TRIGGER_GIFT_FIRST_BY_HIT_FLY, player);
                player.setHitFlyCount(player.getHitFlyCount() + 1);
            } else {
                player.setOffOnlineHitFly(true);
            }
        }

        // ???????????????????????????
        activityDataManager.updateTriggerStatus(ActivityConst.TRIGGER_GIFT_REBUILD, player, 1);

        // ???????????????????????????
        syncRoleMove(player, newPos);
        // ????????????
        buildingDataManager.SyncRebuild(player, atkPlayer);
        // ??????????????????
        worldDataManager.syncWorldChangeEvent(WorldEvent.createWorldEvent(prePos, WorldEvent.EVENT_HIT_FLY));

        // ???????????????
        List<Integer> posList = new ArrayList<>();
        posList.add(prePos);
        posList.add(newPos);
        worldService.onPlayerPosChangeCallbcak(player, prePos, newPos, WorldConstant.CHANGE_POS_TYPE_2);
        // ??????????????????????????????
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
     * ???????????????????????????????????????????????????????????????
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

        // ??????????????????
        // battle.getDefRoleList().add(defencer);
        autoFillArmy(defencer);
        // try {
        // playerDataManager.autoAddArmy(defencer);
        // wallService.processAutoAddArmy(defencer);
        // } catch (Exception e) {
        // LogUtil.error("???????????????", e);
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
        LogUtil.debug(defencer.lord.getPos() + "????????????????????????=" + heroList + ",?????????Defs=" + battle.getDefList());

        int now = TimeHelper.getCurrentSecond();
        // ??????NPC
        WallNpc wallNpc = null;
        if (!defencer.wallNpc.isEmpty()) {
            for (Entry<Integer, WallNpc> ks : defencer.wallNpc.entrySet()) {
                wallNpc = ks.getValue();
                StaticWallHeroLv staticSuperEquipLv = StaticBuildingDataMgr.getWallHeroLv(wallNpc.getHeroNpcId(),
                        wallNpc.getLevel());
                int maxArmy = staticSuperEquipLv.getAttr().get(AttrId.LEAD);
                if (wallNpc.getCount() < maxArmy) {
                    continue;
                }
                wallNpc.setAddTime(now); // ???????????????????????????
                battle.getDefList().add(BattleRole.newBuilder().setKeyId(WorldConstant.ARMY_TYPE_WALL_NPC)
                        .setRoleId(defencer.roleId).addHeroId(ks.getKey()).build());
            }
        }

        // ?????????????????????????????????
        List<Army> list = worldDataManager.getPlayerGuard(defencer.lord.getPos());
        if (list != null && !list.isEmpty()) {
            Player tarPlayer = null;
            for (Army army : list) {
                if (army.getType() != ArmyConstant.ARMY_TYPE_GUARD) {
                    LogUtil.error(String.format("battleId :%d, ????????????type ?????? :%s", battle.getBattleId(), army));
                    continue;
                }
                tarPlayer = playerDataManager.getPlayer(army.getLordId());
                if (tarPlayer == null) continue;
                //??????????????????????????????????????????????????????
                if (tarPlayer.getCamp() != defencer.getCamp()) {
                    LogUtil.error2Sentry(String.format("battleId %d, pos %d, ???????????????????????? defence.camp %d, defence.pos %d, defence.lordId :%d, army.player.camp %d, army.player.lordId :%d",
                            battle.getBattleId(), battle.getPos(), defencer.getCamp(), defencer.lord.getPos(), defencer.getLordId(), tarPlayer.getCamp(), tarPlayer.getLordId()));
                    continue;
                }
                heroIdList = new ArrayList<>();
                for (TwoInt twoInt : army.getHero()) {
                    heroIdList.add(twoInt.getV1());
                }
                // armyMap.put(army.getKeyId(), heroIdList);
                // ??????????????????,???????????????(???????????????????????????ID???)
                BattleRole.Builder battleRoleBuilder = BattleRole.newBuilder().setKeyId(WorldConstant.ARMY_TYPE_HLEP).setRoleId(tarPlayer.roleId).addAllHeroId(heroIdList);
                if (!ObjectUtils.isEmpty(army.getSeasonTalentAttr())) {
                    battleRoleBuilder.addAllSeasonTalentAdd(army.getSeasonTalentAttr());
                }
                battle.getDefList().add(battleRoleBuilder.build());
            }
        }
        LogUtil.debug(defencer.lord.getPos() + ", ??????????????????=" + list + ",?????????Defs=" + battle.getDefList());
    }

    // /**
    // * ???????????????????????????????????????????????????????????????
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
    // // ??????????????????
    // map = new HashMap<>();
    // armyMap = new HashMap<>();
    // // battle.getDefRoleList().add(defencer);
    // try {
    // wallService.processAutoAddArmy(defencer);
    // } catch (Exception e) {
    // LogUtil.error("???????????????", e);
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
    // LogUtil.debug(defencer.lord.getPos() + "????????????????????????=" + heroList +
    // ",?????????Defs=" + battle.getDefs());
    //
    // // ??????NPC
    //
    // // ?????????????????????????????????
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
    // armyMap.put(-1, heroIdList);// ??????????????????,???????????????(???????????????????????????ID???)
    // map.put(tarPlayer.roleId, armyMap);
    // battle.getDefs().add(map);
    // }
    // }
    // LogUtil.debug(defencer.lord.getPos() + "????????????=" + list + ",?????????Defs=" +
    // battle.getDefs());
    // }

    /**
     * ?????????????????????????????????
     *
     * @param battle
     * @param now
     */
    public void retreatBattleArmy(Battle battle, int now) {
        // ??????????????????????????? ??????????????????????????????????????????army???BattleId
        retreatBattleArmy(battle, now, battle.getAtkRoles());
        retreatBattleArmy(battle, now, battle.getDefRoles());

        // List<BattleRole> list = new ArrayList<>();
        // LogUtil.debug("?????????????????????????????????battle=" + battle);
        // list.addAll(battle.getAtkList());
        // list.addAll(battle.getDefList());
        // long roleId;
        // for (BattleRole map : list) {
        // roleId = map.getRoleId();
        // player = playerDataManager.getPlayer(roleId);
        // if (player == null) {
        // LogUtil.debug("?????????????????????????????????,player is null=" + roleId);
        // continue;
        // }
        // // ??????????????????????????????????????????????????????????????????????????????0???????????????
        // if (map.getKeyId() > 0) {
        // Army army = player.armys.get(map.getKeyId());
        // if (army != null) { // && army.getTarget() == battle.getPos()
        // LogUtil.debug("?????????????????????????????????armyTarget=" + army.getTarget() +
        // ",battlePos=" + battle.getPos()
        // + ",army=" + army);
        // worldService.retreatArmy(player, army, now);
        //
        // // ??????
        // worldService.synRetreatArmy(player, army, now);
        // }
        // }
        // }
    }

    private void retreatBattleArmy(Battle battle, int now, Set<Long> roleIds) {
        // ??????????????????????????? ??????????????????????????????????????????army???BattleId
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
                LogUtil.debug("?????????????????????????????????armyTarget=" + army.getTarget() + ",battlePos=" + battle.getPos() + ",army="
                        + army);
                worldService.retreatArmy(player, army, now);
                // ??????
                worldService.synRetreatArmy(player, army, now);
            }
        }
    }

    /**
     * ?????????????????????
     *
     * @param battle
     * @param now
     */
    public void retreatAllBattleArmy(Battle battle, int now) {
        Set<Long> list = new HashSet<>();
        LogUtil.debug("?????????????????????????????????battle=" + battle);
        list.addAll(battle.getAtkRoles());
        list.addAll(battle.getDefRoles());
        for (long roleId : list) {
            Player player = playerDataManager.getPlayer(roleId);
            if (player == null) {
                LogUtil.debug("?????????????????????????????????,player is null=" + roleId);
                continue;
            }

            if (player.armys == null || player.armys.isEmpty()) {
                LogUtil.debug("?????????????????????????????????,armyMap is null=" + player.armys);
                continue;
            }

            for (Army army : player.armys.values()) {
                if (army != null && army.getBattleId() != null && army.getBattleId() == battle.getBattleId()) {
                    LogUtil.debug("?????????????????????????????????armyTarget=" + army.getTarget() + ",battlePos=" + battle.getPos()
                            + ",army=" + army);
                    worldService.retreatArmy(player, army, now);
                    worldService.synRetreatArmy(player, army, now); // ??????
                }
            }
        }
    }

    /**
     * ??????????????????????????????
     *
     * @param battle
     * @param now
     * @param posList
     */
    private void retreatAllGestapoArmy(Battle battle, int now, List<Integer> posList) {
        Set<Long> list = new HashSet<>();
        LogUtil.debug("?????????????????????????????????battle=" + battle);
        list.addAll(battle.getAtkRoles());
        for (long roleId : list) {
            Player player = playerDataManager.getPlayer(roleId);
            if (player == null) {
                LogUtil.debug("?????????????????????????????????,player is null=" + roleId);
                continue;
            }

            if (player.armys == null || player.armys.isEmpty()) {
                LogUtil.debug("?????????????????????????????????,armyMap is null=" + player.armys);
                continue;
            }

            for (Army army : player.armys.values()) {
                if (army != null && army.getBattleId() != null && army.getBattleId() == battle.getBattleId()) {
                    int targetPos = battle.getPos();
                    LogUtil.debug("?????????????????????????????????armyTarget=" + army.getTarget() + ",battlePos=" + battle.getPos()
                            + ",army=" + army);
                    worldService.retreatArmy(player, army, now);
                    worldService.synRetreatArmy(player, army, now); // ??????
                    // ?????????????????????
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
     * ???????????????????????????????????????????????????
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
        // ???????????????
        sendCityBattleAtkMails(battle, atkSuccess, report, dropList, now, recoverArmyAwardMap, atkLord.getNick(),
                defLord.getCamp(), defLord.getLevel(), defLord.getNick(), atkLord.getNick(), atkPos.getA(),
                atkPos.getB(), defLord.getLevel(), defLord.getNick(), defPos.getA(), defPos.getB());

        // ???????????????
        sendCityBattleDefMails(battle, !atkSuccess, report, loseList, now, recoverArmyAwardMap, defLord.getNick(),
                atkLord.getCamp(), atkLord.getLevel(), atkLord.getNick(), atkLord.getCamp(), atkLord.getLevel(),
                atkLord.getNick(), atkPos.getA(), atkPos.getB(), defLord.getNick(), defPos.getA(), defPos.getB());
    }

    /**
     * ??????????????????????????????????????????
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
        if (null == defLord) {// ?????????????????????
            // ???????????????
            if (atkLord != null) {
                sendCampBattleAtkMails(battle, battle.getAtkList(), atkSuccess, report, dropMap, now,
                        recoverArmyAwardMap, atkLord.getNick(), cityId, atkLord.getNick(), atkPos.getA(), atkPos.getB(),
                        cityId, defPos.getA(), defPos.getB());
            } else if (!battle.getAtkList().isEmpty()) {
                sendCampBattleAtkMails(battle, battle.getAtkList(), atkSuccess, report, dropMap, now,
                        recoverArmyAwardMap, battle.getAtkName(), cityId, battle.getAtkName(), atkPos.getA(),
                        atkPos.getB(), cityId, defPos.getA(), defPos.getB());
            }
            if (!battle.isAtkNpc()) {// ???????????????
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
            // ???????????????
            if (atkLord != null) {
                sendCampBattleAtkMails(battle, battle.getAtkList(), atkSuccess, report, dropMap, now,
                        recoverArmyAwardMap, atkLord.getNick(), cityId, atkLord.getNick(), atkPos.getA(), atkPos.getB(),
                        cityId, defPos.getA(), defPos.getB());
            } else if (!battle.getAtkList().isEmpty()) {
                sendCampBattleAtkMails(battle, battle.getAtkList(), atkSuccess, report, dropMap, now,
                        recoverArmyAwardMap, battle.getAtkName(), cityId, battle.getAtkName(), atkPos.getA(),
                        atkPos.getB(), cityId, defPos.getA(), defPos.getB());
            }
            // ???????????????
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
     * ??????????????????????????????
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
        LogUtil.debug("???????????????????????????????????????, playerNum:", list.size());
        Set<Long> ids = new HashSet<>();
        list.forEach(role -> ids.add(role.getRoleId()));// ????????????????????????
        ids.add(battle.getDefencerId()); // ????????????
        for (Long roleId : ids) {
            Player player = playerDataManager.getPlayer(roleId);
            if (player == null) {
                LogUtil.debug("?????????????????????Fighter??????,player is null=" + roleId);
                continue;
            }
            ids.add(roleId);
            if (defSuccess) {
                // ???????????????????????????
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
     * ??????????????????????????????
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
        LogUtil.debug("???????????????????????????????????????, playerNum:", list.size());
        long roleId;
        Set<Long> ids = new HashSet<>();
        for (BattleRole map : list) {
            roleId = map.getRoleId();
            if (ids.contains(roleId)) {
                continue;
            }
            Player player = playerDataManager.getPlayer(roleId);
            if (player == null) {
                LogUtil.debug("?????????????????????Fighter??????,player is null=" + roleId);
                continue;
            }
            ids.add(roleId);
            if (atkSuccess) {
                Object[] params = Arrays.copyOf(param, param.length + 1);
                params[param.length] = battle.getSponsor() != null ? battle.getSponsor().lord.getNick() : "";
                mailDataManager.sendReportMail(player, report, MailConstant.MOLD_ATK_CITY_SUCC, dropList, now,
                        recoverArmyAwardMap, params);
            } else {// ?????????????????????????????????
                mailDataManager.sendReportMail(player, report, MailConstant.MOLD_ATK_CITY_FAIL, null, now,
                        recoverArmyAwardMap, param);
            }
        }
    }

    /**
     * ??????????????????????????????
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
        LogUtil.debug("????????????????????????????????????, playerNum:", list.size());
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
                LogUtil.debug("?????????????????????Fighter??????,player is null=" + roleId);
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
                LogUtil.debug("?????????????????????Fighter??????,player is null=" + defender);
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
     * ??????????????????????????????
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
        LogUtil.debug("????????????????????????????????????, playerNum:", list.size());
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
                LogUtil.debug("?????????????????????Fighter??????,player is null=" + roleId);
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

        if (battle.getSponsor() != null && (!ids.contains(battle.getSponsor().roleId.longValue()))) {// ????????????????????????,???????????????????????????
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
     * ?????????????????????????????????
     *
     * @param forces
     * @param from
     * @param rpt
     * @param isAttacker
     * @param atkPlayer       ?????????????????????????????????
     * @param addExploit      ??????????????????
     * @param exploitAwardMap
     */
    public void addBattleHeroExp(List<Force> forces, AwardFrom from, CommonPb.RptAtkPlayer.Builder rpt,
                                 boolean isAttacker, boolean atkPlayer, boolean cityBattle, Map<Long, ChangeInfo> changeMap,
                                 boolean addExploit, HashMap<Long, Map<Integer, Integer>> exploitAwardMap) {
        for (Force force : forces) {
            // ????????????
            long roleId = force.ownerId;
            ChangeInfo info = changeMap.get(roleId);
            if (null == info) {
                info = ChangeInfo.newIns();
                changeMap.put(roleId, info);
            }
            int type = force.roleType;
            int kill = force.killed;
            int heroId = force.id;
            // ???????????? ; ?????? ??????=?????????,?????????????????????
            int count = cityBattle ? force.totalLost : (int) (force.killed * 0.8f + force.totalLost * 0.2f);
            int award = addExploit ? count : 0;// ??????
            String owner = playerDataManager.getNickByLordId(roleId);
            int lv = 0;// ????????????
            int addExp = 0;// ????????????
            int lost = force.lost;
            int heroDecorated = 0;
            Hero hero = null;
            if (force.roleType == Constant.Role.CITY) {

            } else if (force.roleType == Constant.Role.WALL) {
                Player player = playerDataManager.getPlayer(roleId);
                if (player == null) {
                    continue;
                }
                award = addExploit(player, award, info, from); // ?????????
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
                // ??????????????????
                if (force.hasFight) {
                    addExp = (force.killed + force.totalLost) / 2;// ??????=????????????+????????????/2
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
            RptHero rptHero = PbHelper.createRptHero(type, kill, award, heroId, owner, lv, addExp, lost, hero);
            if (isAttacker) {
                rpt.addAtkHero(rptHero);
            } else {
                rpt.addDefHero(rptHero);
            }
        }
    }

    /**
     * ??????????????????
     *
     * @param heroFightSummary
     * @param player
     * @param from
     */
    public void addCrossBattleHeroExp(HeroFightSummary heroFightSummary, Player player, AwardFrom from) {
        // ????????????
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
     * ?????????
     *
     * @param player
     * @param count
     * @param info
     * @param from
     * @return
     */
    public int addExploit(Player player, int count, ChangeInfo info, AwardFrom from) {
        // ????????????
        int award = count;
        double techAdd = techDataManager.getTechEffect4Single(player, TechConstant.TYPE_26);
        if (techAdd > 0) {
            award += award * techAdd;
        }
        if (award > 0) {
            // ??????????????????
            rewardDataManager.addAward(player, AwardType.MONEY, AwardType.Money.EXPLOIT, award, from);
            if (!CheckNull.isNull(info)) {
                info.addChangeType(AwardType.MONEY, AwardType.Money.EXPLOIT);
            }
        }
        return award;

    }

    /**
     * ?????????????????????
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
     * ???????????????????????????
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
                LogUtil.error("??????????????????????????????, roleId:", force.ownerId);
                continue;
            }
            if (force.totalLost > 0) {
                // ?????????????????????
                battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_SUB_HERO_ARMY, force.totalLost);
                activityDataManager.updRankActivity(player, ActivityConst.ACT_ARMY_RANK, force.totalLost);
                // ????????????????????????
                honorDailyDataManager.addAndCheckHonorReport2s(player, HonorDailyConstant.COND_ID_14, force.totalLost);
                if (force.roleType == Constant.Role.WALL) {
                    WallNpc wallNpc = player.wallNpc.get(force.id);
                    if (null == wallNpc) {
                        LogUtil.error("????????????????????????????????????NPC, wallId:", force.id);
                        continue;
                    }

                    lost = wallNpc.subArm(force.totalLost);
                    LogLordHelper.wallNPCArm(from, player.account, player.lord, wallNpc.getHeroNpcId(),
                            wallNpc.getCount(), -lost, Constant.ACTION_SUB);
                } else {

                    hero = player.heros.get(force.id);
                    if (null == hero) {
                        LogUtil.error("??????????????????????????????, heroId:", force.id);
                        continue;
                    }
                    lost = hero.subArm(force.totalLost);
                    StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
                    if (Objects.nonNull(staticHero)) {
                        // ?????????????????????????????????
                        int armType = staticHero.getType();
                        // LogLordHelper.heroArm(from, player.account, player.lord, hero.getHeroId(), hero.getCount(), -lost, staticHero.getType(),
                        //         Constant.ACTION_SUB);

                        // ????????????????????????
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
                // ????????????
                activityDataManager.updActivity(player, ActivityConst.ACT_BIG_KILL, force.killed, 0, true);
            }
        }
    }

    /**
     * ?????????????????????
     *
     * @param targetPos ????????????
     * @param atkCancel ????????????????????????
     */
    public void cancelCityBattle(int targetPos, boolean atkCancel) {
        cancelCityBattle(targetPos, 0, atkCancel, true);
    }

    /**
     * ?????????????????????????????????
     *
     * @param targetPos ????????????
     * @param newPos    ?????????
     * @param atkCancel ????????????????????????
     */
    public void cancelCityBattle(int targetPos, int newPos, boolean atkCancel, boolean distance) {
        if (worldDataManager.isEmptyPos(targetPos)) {
            return;
        }

        if (!worldDataManager.isPlayerPos(targetPos)) {
            return;
        }

        // ?????????????????????????????????(?????????????????????, ?????????????????????)
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
            if (battle.getDefencer().roleId != lord.getLordId()) {// ????????????????????????????????????????????????????????????????????????????????????????????????
                LogUtil.error("???????????????????????????, targetPos:", targetPos, ", targetId:", lord.getLordId(), ", defencerId:",
                        battle.getDefencer().roleId);
            }

            retreatAllBattleArmy(battle, now);// ????????????

            // ????????????, ????????????????????? ???????????????
            Player target = worldDataManager.getPosData(targetPos);
            Lord lord1 = battle.getSponsor().lord;
            if (target != null && target.isLogin) {
                worldService.syncAttackRole(target, lord1, battle.getBattleTime(), WorldConstant.ATTACK_ROLE_0);
            }


            sendCancelBattleMail(targetPos, atkCancel, battle, now, lord);  // ????????????????????????
        }

        warDataManager.removePosExchangeSpecialBattle(targetPos, newPos);// ???????????????????????????????????????,
        // ?????????????????????

        if (distance) {
            // ????????????
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
     * @Description: ??????????????????, ???????????????
     */
    public void cancelGestapoBattle(int targetPos) {
        if (worldDataManager.isEmptyPos(targetPos)) {
            return;
        }
        if (!worldDataManager.isGestapoPos(targetPos)) {
            return;
        }

        // ?????????????????????????????????
        LinkedList<Battle> battleList = warDataManager.getBattlePosMap().get(targetPos);
        if (CheckNull.isEmpty(battleList)) {
            return;
        }
        List<Integer> posList = new ArrayList<>();
        int now = TimeHelper.getCurrentSecond();
        for (Battle battle : battleList) {
            retreatAllGestapoArmy(battle, now, posList);// ????????????
        }
        warDataManager.removePosAllCityBattle(targetPos);// ??????????????????
        // ????????????
        posList.add(targetPos);
        EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.LINE_TYPE));
    }

    /**
     * ????????????
     *
     * @param targetPos ??????
     * @param atkCancel ??????????????????
     * @param battle
     * @param distance  ????????????
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
        if (battle.getDefencer().roleId != lord.getLordId()) {// ????????????????????????????????????????????????????????????????????????????????????????????????
            LogUtil.error("???????????????????????????, targetPos:", targetPos, ", targetId:", lord.getLordId(), ", defencerId:",
                    battle.getDefencer().roleId);
        }

        warDataManager.removeBattleById(battle.getBattleId());
        LogUtil.debug("??????battleId=" + battle.getBattleId());
        retreatBattleArmy(battle, now);// ????????????
        removePlayerJoinBattle(battle);// ????????????????????????

        sendCancelBattleMail(targetPos, atkCancel, battle, now, lord); // ????????????????????????

        if (distance) {
            // ????????????
            List<Integer> posList = MapHelper.getAreaStartPos(MapHelper.getLineAcorss(targetPos, lord.getPos()));
            posList.add(targetPos);
            posList.add(lord.getPos());
            EventBus.getDefault()
                    .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.LINE_TYPE));
        }
    }

    /**
     * ????????????????????????
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
        if (atkCancel) { // ???????????????
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
            // ?????????????????????
            sendCancelCityDefMail(battle, now, lord.getNick(), lord.getNick());
        }
    }

    /**
     * ??????????????????????????????????????????????????????????????????
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
     * ?????????????????????????????????????????????????????????
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
     * ???????????????????????????????????????????????????????????????
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
        if (battle.getDefencer() != null) {// ???????????????
            mailDataManager.sendNormalMail(battle.getDefencer(), MailConstant.MOLD_CITY_ATK_RETREAT_DEF, time, param);
        }
    }

    /**
     * ??????
     *
     * @param mPlayer ?????????
     * @param tPlayer ?????????
     * @param heroIds ??????id
     * @return ????????????
     */
    public GamePb4.CompareNotesRs compareNotesFightLogic(Player mPlayer, Player tPlayer, List<Integer> heroIds) {
        // ?????????????????????????????????
        Fighter attacker = fightService.createCombatPlayerFighter(mPlayer, heroIds);
        Fighter defender = fightService.createCombatPlayerFighter(tPlayer, tPlayer.getAllOnBattleHeros().stream().map(Hero::getHeroId).collect(Collectors.toList()));
        FightLogic fightLogic = new FightLogic(attacker, defender, true);
        fightLogic.fight();
        GamePb4.CompareNotesRs.Builder builder = GamePb4.CompareNotesRs.newBuilder();
        // ????????????
        CommonPb.Record record = fightLogic.generateRecord();
        CommonPb.RptAtkPlayer.Builder rpt = CommonPb.RptAtkPlayer.newBuilder();
        rpt.setNightEffect(solarTermsDataManager.getNightEffect() != null);
        rpt.setResult(fightLogic.getWinState() == ArmyConstant.FIGHT_RESULT_SUCCESS);
        rpt.setRecord(record);
        // ?????????????????????
        rpt.setAttack(PbHelper.createRptMan(mPlayer.lord.getPos(), mPlayer.lord.getNick(), mPlayer.lord.getVip(),
                mPlayer.lord.getLevel()));
        rpt.setDefMan(PbHelper.createRptMan(tPlayer.lord.getPos(), tPlayer.lord.getNick(), tPlayer.lord.getVip(),
                tPlayer.lord.getLevel()));
        // ?????????????????????
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
     * ????????????????????????????????????????????????????????????
     *
     * @param battle
     * @param force
     * @param hero
     * @param player
     * @param map
     */
    public void fightForceBuff(Battle battle, Force force, Hero hero, Player player, CommonPb.BattleRole map) {
        if (!ObjectUtils.isEmpty(battle.getDefencer())) {
            //????????? ??????????????????
            if (force.ownerId == battle.getDefencer().roleId && Objects.nonNull(hero)) {
                if (hero.isOnWall()) {
                    //?????????????????????????????????
                    force.attrData.addAttrValue(DataResource.getBean(SeasonTalentService.class).
                            getSeasonTalentEffectTwoInt(player, hero, SeasonConst.TALENT_EFFECT_619));
                    //??????????????????
                    treasureWareBuff(player, hero, force.attrData);
                }
            }
        }
        //?????????????????? ?????????????????????????????????????????????
        if (Objects.nonNull(map)) {
            force.attrData.addAttrValue(map.getSeasonTalentAddList());
        }
    }

    /**
     * ??????????????????
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
