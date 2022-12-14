package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.*;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonDailyRestrictTaskService;
import com.gryphpoem.game.zw.logic.FightSettleLogic;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.*;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.fight.FightLogic;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;
import com.gryphpoem.game.zw.resource.pojo.fight.NpcForce;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.plan.FunctionTrigger;
import com.gryphpoem.game.zw.resource.pojo.world.*;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.resource.util.eventdata.EventDataUp;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.activity.ActivityRobinHoodService;
import com.gryphpoem.game.zw.service.activity.RamadanVisitAltarService;
import com.gryphpoem.game.zw.service.plan.DrawCardPlanTemplateService;
import com.gryphpoem.game.zw.service.relic.RelicsFightService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import com.gryphpoem.game.zw.service.totem.TotemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: ZhouJie
 * @date: Create in 2019-01-21 14:07
 * @description:
 * @modified By:
 */
@Service
public class MarchService {

    @Autowired
    private SuperMineService superMineService;
    @Autowired
    private CounterAtkService counterAtkService;
    @Autowired
    private WorldService worldService;
    @Autowired
    private HonorDailyService honorDailyService;
    @Autowired
    private CampService campService;
    @Autowired
    private RebelService rebelService;
    @Autowired
    private FightService fightService;
    @Autowired
    private CityService cityService;
    @Autowired
    private WarService warService;
    @Autowired
    private AirshipService airshipService;
    @Autowired
    private WorldScheduleService worldScheduleService;
    @Autowired
    private ActivityRobinHoodService robinHoodService;

    @Autowired
    private WarDataManager warDataManager;
    @Autowired
    private WorldDataManager worldDataManager;
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private GlobalDataManager globalDataManager;
    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private MedalDataManager medalDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private HonorDailyDataManager honorDailyDataManager;
    @Autowired
    private TaskDataManager taskDataManager;
    @Autowired
    private ChatDataManager chatDataManager;
    @Autowired
    private BattlePassDataManager battlePassDataManager;
    @Autowired
    private RoyalArenaService royalArenaService;
    @Autowired
    private FightSettleLogic fightSettleLogic;
    @Autowired
    private SeasonTalentService seasonTalentService;

    @Autowired
    private WorldWarSeasonDailyRestrictTaskService worldWarSeasonDailyRestrictTaskService;

    @Autowired
    private RamadanVisitAltarService ramadanVisitAltarService;
    @Autowired
    private TotemService totemService;

    @Autowired
    private TitleService titleService;
    @Autowired
    private RelicsFightService relicsFightService;

    /**
     * ????????????????????????
     *
     * @param player
     * @param army
     * @param now
     */
    public void marchEnd(Player player, Army army, int now) {

        int pos = army.getTarget();
        if (army.getType() == ArmyConstant.ARMY_TYPE_ATK_PLAYER || army.getType() == ArmyConstant.ARMY_TYPE_ATK_CAMP) {
            Player target = worldDataManager.getPosData(pos);
            if (target != null || worldDataManager.isCityPos(pos)) {
                // ??????????????????
                LogUtil.debug("?????????????????????????????????, roleId:", player.roleId, ", army:", army);
                fightPlayer(player, army, now);
            } else {
                LogUtil.debug("?????????????????????????????????????????????, roleId:", player.roleId, ", army:", army);
                if (army.getTarLordId() > 0) {
                    Player tarPlayer = playerDataManager.getPlayer(army.getTarLordId());
                    if (tarPlayer != null) {
                        mailDataManager.sendNormalMail(player, MailConstant.MOLD_CITY_DEF_FLEE_ATK, now,
                                tarPlayer.lord.getNick(), tarPlayer.lord.getNick());
                    }
                } else {
                    Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
                    mailDataManager.sendReportMail(player, null, MailConstant.MOLD_ATK_TARGET_NOT_FOUND, null, now,
                            xy.getA(), xy.getB(), xy.getA(), xy.getB());
                }
                worldService.retreatArmy(player, army, now);
            }
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_ATK_BANDIT) {
            fightBandit(player, army, now);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_COLLECT) {
            fightMine(player, army, now);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_GUARD) {
            guardArmy(player, army, now);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_ATK_CABINET_LEAD) {
            // ??????????????????
            fightCabinetLead(player, army, now);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_ATK_CABINET_TASK) {
            // ??????????????????
            fightBandit4Taks(player, army, now);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_ATK_GESTAPO) {
            // ??????????????????
            Gestapo gestapo = worldDataManager.getGestapoByPos(pos);
            if (!CheckNull.isNull(gestapo)) {
                LogUtil.debug("?????????????????????????????????, roleId:", player.roleId, ", army:", army);
                fightGestapo(player, army, now);
                worldService.synRetreatArmy(player, army, now);
            } else {
                LogUtil.debug("?????????????????????????????????????????????, roleId:", player.roleId, ", army:", army);
                Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
                mailDataManager.sendReportMail(player, null, MailConstant.MOLD_ATK_GESTAPO_NOT_FOUND, null, now,
                        xy.getA(), xy.getB(), xy.getA(), xy.getB());
                worldService.retreatArmy(player, army, now);
            }
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_LIGHTNING_WAR) {
            // ???????????????
            LightningWarBoss lightningWarBoss = worldDataManager.getLightningWarBossByArea(player.lord.getArea());
            if (!CheckNull.isNull(lightningWarBoss) && !lightningWarBoss.isNotInitOrDead()) {
                LogUtil.debug("?????????????????????????????????, roleId:", player.roleId, ", army:", army);
                fightLightningWar(player, army, now);
            } else {
                LogUtil.debug("?????????????????????????????????????????????, roleId:", player.roleId, ", army:", army);
                Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
                mailDataManager.sendReportMail(player, null, MailConstant.MOLD_ATK_GESTAPO_NOT_FOUND, null, now,
                        xy.getA(), xy.getB(), xy.getA(), xy.getB());
                worldService.retreatArmy(player, army, now);
            }
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_COLLECT_SUPERMINE) {
            // ??????????????????
            superMineService.marchEndcollectSuperMineLogic(player, army, now);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_ATK_SUPERMINE) {
            // ??????????????????
            superMineService.marchEndAtkSuperMineLogic(player, army, now);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_HELP_SUPERMINE) {
            // ??????????????????
            superMineService.marchEndHelpSuperMineLogic(player, army, now);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_BERLIN_WAR
                || army.getType() == ArmyConstant.ARMY_TYPE_BATTLE_FRONT_WAR) {
            // ??????????????????
            BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
            if (!CheckNull.isNull(berlinWar) && berlinWar.getStatus() == WorldConstant.BERLIN_STATUS_OPEN) {
                LogUtil.debug("?????????????????????????????????, roleId:", player.roleId, ", army:", army);
                fightBerlinWar(player, army, now);
            } else {
                LogUtil.debug("??????????????????????????????????????????, roleId:", player.roleId, ", army:", army);
                Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
                mailDataManager.sendReportMail(player, null, MailConstant.MOLD_ATK_TARGET_NOT_FOUND, null, now,
                        xy.getA(), xy.getB(), xy.getA(), xy.getB());
                worldService.retreatArmy(player, army, now);
            }
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_REBEL_BATTLE) {
            rebelService.marchEndRebelHelpArmy(player, army, now);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_COUNTER_BOSS_DEF) {
            // ???????????????BOSS??????ARMY
            CounterAttack counterAttack = globalDataManager.getGameGlobal().getCounterAttack();
            if (!CheckNull.isNull(counterAttack) && !counterAttack.isNotInitOrDead()) {
                counterAtkService.marchEndBossDefArmy(player, army, now);
            } else {
                LogUtil.debug("??????????????????????????????????????????, roleId:", player.roleId, ", army:", army);
                Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
                mailDataManager.sendReportMail(player, null, MailConstant.MOLD_ATK_TARGET_NOT_FOUND, null, now,
                        xy.getA(), xy.getB(), xy.getA(), xy.getB());
                worldService.retreatArmy(player, army, now);
            }

        } else if (army.getType() == ArmyConstant.ARMY_TYPE_COUNTER_BOSS_ATK_HELP) {
            // ???????????????BOSS??????
            // ????????????
            counterAtkService.marchEndBossAtkHelpArmy(player, army, now);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_DECISIVE_BATTLE) {
            // ??????
            LogUtil.debug("?????????????????????????????????, roleId:", player.roleId, ", army:", army);
            fightDecisiveWar(player, army, now);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_ATTACK_AIRSHIP) {
            // ????????????
            fightAirShip(player, army, now);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_ATTACK_SCHEDULE_BOSS) {
            // ????????????boss
            // worldScheduleService.fightSchedBossLogic(player, army, now);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_ALTAR) {
            ramadanVisitAltarService.marchEnd(player, army, now);
            // ????????????????????????, ?????????????????????
            worldService.retreatArmy(player, army, now);
            worldService.synRetreatArmy(player, army, now);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_RELIC_BATTLE) {
            relicsFightService.marchEnd(player, army, now);
        }
    }

    /**
     * ????????????????????????
     *
     * @param player
     * @param army
     */
    public void retreatEnd(Player player, Army army) {
        // ??????????????????
        worldDataManager.removeMarch(player, army);
        LogUtil.common("??????????????????, army:", army);

        // ????????????

        // ????????????
        Hero hero;
        for (CommonPb.TwoInt twoInt : army.getHero()) {
            hero = player.heros.get(twoInt.getV1());
            if (Objects.nonNull(hero)) {
                hero.setState(ArmyConstant.ARMY_STATE_IDLE);
            }
        }

        // ?????????
        if (!CheckNull.isEmpty(army.getGrab())) {
            List<CommonPb.Award> grab = army.getGrab();
            rewardDataManager.sendRewardByAwardList(player, grab, AwardFrom.COLLECT);
            // ??????????????????
            if (MapHelper.getAreaIdByPos(army.getTarget()) == WorldConstant.AREA_TYPE_13) {
                int camp = player.lord.getCamp();
                City city = worldDataManager.checkHasHome(camp);
                if (city != null) {
                    Long time = worldDataManager.getCampCollectTime().get(camp);
                    time = time == null ? 0 : time;
                    time += army.getCollectTime();
                    if (time >= Constant.HOME_CITY_ADD_EXP_COLLECT_TIME) {
                        int addExp = (int) (time / Constant.HOME_CITY_ADD_EXP_COLLECT_TIME);
                        time = time % Constant.HOME_CITY_ADD_EXP_COLLECT_TIME;

                        cityService.addHomeCityPopulation(city, addExp);
                        LogUtil.debug("????????????????????????,??????????????? addExp:", addExp);
                    }
                    worldDataManager.getCampCollectTime().put(camp, time);
                }
            }
        }
        // ????????????????????????
        warService.autoFillArmy(player);
        // ????????????
        List<Integer> posList = MapHelper
                .getAreaStartPos(MapHelper.getLineAcorss(army.getTarget(), player.lord.getPos()));
        posList.add(army.getTarget());
        posList.add(player.lord.getPos());
        EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.LINE_TYPE));
    }

    /**
     * ??????????????????Battle???????????????????????????warService??????????????????
     *
     * @param player
     * @param army
     * @param now
     */
    private void fightPlayer(Player player, Army army, int now) {
        // ????????????????????????????????????
        // int pos = army.getTarget();
        // Integer battleId = player.battleMap.get(pos);
        Integer battleId = army.getBattleId();
        LogUtil.debug("??????????????????id, roleId:", player.roleId, ", battleId:", battleId);
        if (null == battleId) {
            battleId = 0;
        }
        Battle battle = warDataManager.getBattleMap().get(battleId);
        LogUtil.debug("????????????????????????, roleId:", player.roleId, ", battle:", battle);

        if (null == battle) {
            // ????????????????????????????????????
            worldService.retreatArmy(player, army, now);
            worldService.synRetreatArmy(player, army, now);
            return;
        }

        if (battle.isCityBattle() && battle.getDefencer() != null) {
            // ??????????????????
            Effect effect = battle.getDefencer().getEffect().get(EffectConstant.PROTECT);
            if (effect != null && effect.getEndTime() > now) {
                LogUtil.debug("????????????????????????????????????", player.roleId, ", battle:", battle);
                worldService.retreatArmy(player, army, now);
                worldService.synRetreatArmy(player, army, now);
                // ?????????
                Player defencer = battle.getDefencer();
                String nick = defencer.lord.getNick();
                Turple<Integer, Integer> rPos = MapHelper.reducePos(defencer.lord.getPos());
                mailDataManager.sendNormalMail(player, MailConstant.MOLD_ATTACK_TARGET_HAS_PROTECT, now, nick,
                        rPos.getA(), rPos.getB(), nick, rPos.getA(), rPos.getB());
                return;
            }
        }

        // ??????????????????
        army.setState(ArmyConstant.ARMY_STATE_BATTLE);

        Hero hero;
        // int armCount = 0;
        List<Integer> heroIdList = new ArrayList<>();
        for (CommonPb.TwoInt twoInt : army.getHero()) {
            hero = player.heros.get(twoInt.getV1());
            hero.setState(ArmyConstant.ARMY_STATE_BATTLE);
            heroIdList.add(hero.getHeroId());
            // armCount += hero.getCount();
        }
        // ?????????????????????????????????????????? ??????????????????????????????
        // if (!battle.isCityBattle() || player.roleId !=
        // battle.getSponsor().roleId) {
        int camp = player.lord.getCamp();
        if (camp == battle.getAtkCamp()) {
            worldService.addBattleArmy(battle, player.roleId, heroIdList, army.getKeyId(), true);
        } else {
            worldService.addBattleArmy(battle, player.roleId, heroIdList, army.getKeyId(), false);
        }
        // }

        LogUtil.debug("??????????????????????????????, roleId:", player.roleId, ", battle:", battle);
    }

    /**
     * ????????????
     *
     * @param player
     * @param army
     * @param now
     * @return
     */
    private boolean fightBandit(Player player, Army army, int now) {
        int pos = army.getTarget();
        int banditId = worldDataManager.getBanditIdByPos(pos);
        return fightBanditLogic(player, army, now, pos, banditId);
    }

    /**
     * ??????????????????
     *
     * @param player
     * @param army
     * @param now
     * @return
     */
    private boolean fightBandit4Taks(Player player, Army army, int now) {
        int pos = army.getTarget();
        int banditId = army.getTargetId();
        return fightBanditLogic(player, army, now, pos, banditId);
    }

    /**
     * ????????????????????????
     *
     * @param player
     * @param army
     * @param now
     * @param pos
     * @param banditId
     * @return
     */
    private boolean fightBanditLogic(Player player, Army army, int now, int pos, int banditId) {
        StaticBandit staticBandit = StaticBanditDataMgr.getBanditMap().get(banditId);
        // ????????????
        Map<Long, List<CommonPb.Award>> recoverArmyAwardMap = new HashMap<>();
        if (null == staticBandit) {
            LogUtil.debug("??????id?????????????????????, banditId:", banditId);
            // ??????????????????
            Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
            mailDataManager.sendReportMail(player, null, MailConstant.MOLD_ATK_TARGET_NOT_FOUND, null, now,
                    recoverArmyAwardMap, xy.getA(), xy.getB(), xy.getA(), xy.getB());

            // ????????????
            worldService.retreatArmy(player, army, now);
            return true;
        }

        // ????????????
        boolean bandit_task_999 = WorldConstant.BANDIT_LV_999 == staticBandit.getLv();
        // ?????????
        Integer historyLv = player.trophy.get(TrophyConstant.TROPHY_1);
        historyLv = historyLv != null ? historyLv : 0;
        if (!bandit_task_999 && staticBandit.getLv() > historyLv + 1) {
            LogUtil.common("??????????????????, banditId:", banditId);
            // ??????????????????
            Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
            mailDataManager.sendReportMail(player, null, MailConstant.MOLD_ATK_TARGET_NOT_FOUND, null, now,
                    recoverArmyAwardMap, xy.getA(), xy.getB());

            // ????????????
            worldService.retreatArmy(player, army, now);
            return true;
        }

        StaticNpc npc;
        for (Integer npcId : staticBandit.getForm()) {
            npc = StaticNpcDataMgr.getNpcMap().get(npcId);
            if (null == npc) {
                LogUtil.error("NPCid?????????, npcId:", npcId);

                // ????????????
                worldService.retreatArmy(player, army, now);
                return true;
            }
        }

        // ????????????
        Fighter attacker = fightService.createFighter(player, army.getHero());
        Fighter defender = fightService.createBanditFighter(banditId);
        FightLogic fightLogic = new FightLogic(attacker, defender, true);
        fightLogic.fight();

        //????????????-??????????????????
        ActivityDiaoChanService.killedAndDeathTask0(attacker, false, true);

        // ????????????????????????????????????, key:roleId
        Map<Long, ChangeInfo> changeMap = new HashMap<>();
        // ????????????
        List<CommonPb.Award> recoverArmyAward = new ArrayList<>();
        // ????????????
        worldService.subHeroArm(player, attacker.forces, AwardFrom.ATTACK_BANDIT, changeMap);
        if (attacker.lost > 0) {
            // ????????????
            activityDataManager.updRankActivity(player, ActivityConst.ACT_ARMY_RANK, attacker.lost);
            // ????????????????????????
            honorDailyDataManager.addAndCheckHonorReport2s(player, HonorDailyConstant.COND_ID_14, attacker.lost);
            // ?????????????????????
            battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_SUB_HERO_ARMY, attacker.lost);
            // ?????????????????????
            List<List<Integer>> armyAward = worldService.attckBanditLostRecvCalc(player, attacker.forces, now,
                    staticBandit.getLv(), WorldConstant.LOST_RECV_CALC_NIGHT);
            if (!CheckNull.isEmpty(armyAward)) {
                recoverArmyAward = rewardDataManager.sendReward(player, armyAward, AwardFrom.RECOVER_ARMY);// "????????????????????????????????????"
            }
            // ????????????????????????????????????
            medalDataManager.angelInWhite(attacker, recoverArmyAwardMap);
            //????????????????????????---????????????
            seasonTalentService.execSeasonTalentEffect303(attacker, recoverArmyAwardMap);
            if (!CheckNull.isEmpty(recoverArmyAwardMap)) {
                List<CommonPb.Award> awards = recoverArmyAwardMap.get(player.roleId);
                if (!CheckNull.isEmpty(awards)) {
                    recoverArmyAward.addAll(awards);
                }
            }
        }

        // ????????????
        CommonPb.Record record = fightLogic.generateRecord();

        Lord lord = player.lord;
        boolean isSuccess = fightLogic.getWinState() == ArmyConstant.FIGHT_RESULT_SUCCESS;
        CommonPb.RptAtkBandit.Builder rpt = CommonPb.RptAtkBandit.newBuilder();
        rpt.setResult(isSuccess);
        rpt.setAttack(PbHelper.createRptMan(lord.getPos(), lord.getNick(), lord.getVip(), lord.getLevel()));
        rpt.setDefend(PbHelper.createRptBandit(banditId, pos));
        rpt.setAtkSum(PbHelper.createRptSummary(attacker.total, attacker.lost, lord.getCamp(), lord.getNick(),
                lord.getPortrait(), player.getDressUp().getCurPortraitFrame()));
        rpt.setDefSum(PbHelper.createRptSummary(defender.total, defender.lost, 0, null, -1, -1));
        // ??????????????????
        rpt.addAllAtkHero(fightSettleLogic.banditFightHeroExpReward(player, attacker.forces));
        DataResource.ac.getBean(WorldService.class).buildRptHeroData(defender, rpt, false);
        rpt.setRecord(record);

        // ????????????
        List<String> tParam = new ArrayList<>();
        tParam.add(lord.getNick());
        tParam.add(String.valueOf(staticBandit.getLv()));
        List<String> cParam = new ArrayList<>();
        cParam.add(lord.getNick());
        Turple<Integer, Integer> xy = MapHelper.reducePos(lord.getPos());
        cParam.add(String.valueOf(xy.getA()));
        cParam.add(String.valueOf(xy.getB()));
        cParam.add(String.valueOf(staticBandit.getLv()));
        xy = MapHelper.reducePos(pos);
        cParam.add(String.valueOf(xy.getA()));
        cParam.add(String.valueOf(xy.getB()));
        cParam.add(lord.getNick());
        // ?????????????????????????????????
        if (isSuccess) {
            if (staticBandit.getLv() > historyLv && !bandit_task_999) {
                player.trophy.put(TrophyConstant.TROPHY_1, staticBandit.getLv());
            }
            //?????? ??????????????????????????????????????????????????????, ????????????
            if (attacker.lost <= Constant.ONHOOK_1066 && player.getPlayerOnHook().getMaxRebelLv() < staticBandit.getLv()) {
                player.getPlayerOnHook().setMaxRebelLv(staticBandit.getLv());
            }
            // ?????????????????????
            worldDataManager.removeBandit(pos, 0);

            List<CommonPb.Award> dropList = new ArrayList<>();
            // ????????????
            double medalNum = medalDataManager.aSurpriseAttackOnTheBanditArmy(attacker);
            // ??????????????????
            int num = activityDataManager.getActDoubleNum(player);
            // ??????????????????
            double resNum = activityDataManager.getActBanditRes(player);
            // ????????????
            double cityBuffer = worldDataManager.getCityBuffer(worldDataManager.checkCityBuffer(player.lord.getPos()),
                    WorldConstant.CityBuffer.CABINET_AWARD_BUFFER, player.roleId);
            // ??????buff
            Effect goldEffect = player.getEffect().get(EffectConstant.BANDIT_GOLD_BUFFER);
            // ??????buff
            Effect woodEffect = player.getEffect().get(EffectConstant.BANDIT_WOOD_BUFFER);
            List<List<Integer>> baseAwards = new ArrayList<>();
            for (List<Integer> award : staticBandit.getAwardBase()) {
                List<Integer> newAward = new ArrayList<>();
                int awardType = award.get(0);
                int awardId = award.get(1);
                newAward.add(awardType);
                newAward.add(awardId);
                // ???????????? = (?????????????????? * ???????????? * 999???????????? * 509???????????? * (1 + ????????????))
                int count = (int) (award.get(2) * medalNum * num * resNum * (1 + cityBuffer));
                if (awardType == AwardType.RESOURCE && awardId == AwardType.Resource.OIL && !CheckNull.isNull(goldEffect)) {
                    int effectVal = goldEffect.getEffectVal();
                    if (effectVal > 0) {
                        int add = (int) (award.get(2) * (effectVal / Constant.TEN_THROUSAND));
                        if (add > 0) {
                            cParam.add(String.valueOf(EffectConstant.BANDIT_GOLD_BUFFER));
                            cParam.add(String.valueOf(goldEffect.getEndTime() - now));
                            count += add;
                        }
                    }
                } else if (awardType == AwardType.RESOURCE && awardId == AwardType.Resource.ELE && !CheckNull.isNull(woodEffect)) {
                    int effectVal = woodEffect.getEffectVal();
                    if (effectVal > 0) {
                        int add = (int) (award.get(2) * (effectVal / Constant.TEN_THROUSAND));
                        if (add > 0) {
                            cParam.add(String.valueOf(EffectConstant.BANDIT_WOOD_BUFFER));
                            cParam.add(String.valueOf(woodEffect.getEndTime() - now));
                            count += add;
                        }
                    }
                }
                newAward.add(count);
                baseAwards.add(newAward);
            }

            List<CommonPb.Award> tmp = rewardDataManager.sendReward(player, baseAwards, AwardFrom.BANDIT_DROP);
            if (tmp != null) {
                dropList.addAll(tmp);
            }
            int drawingNum = activityDataManager.getActBanditDrawing(player);
            tmp = rewardDataManager.sendReward(player, staticBandit.getAwardDrawing(), drawingNum,
                    AwardFrom.BANDIT_DROP);
            List<Integer> guaranteedConfigRule = null;
            for (List<Integer> l : Constant.REBEL_DROP_BLUEPRINT_GUARANTEE_CONFIGURATION) {
                if (CheckNull.isEmpty(l) || l.size() != 2 || l.get(0) != staticBandit.getLv()) continue;
                guaranteedConfigRule = l;
                break;
            }
            if (CheckNull.nonEmpty(tmp)) {
                dropList.addAll(tmp);
                if (CheckNull.nonEmpty(staticBandit.getAwardDrawing()) && CheckNull.nonEmpty(guaranteedConfigRule)) {
                    List<History> guaranteedDrops = player.typeInfo.getOrDefault(Constant.TypeInfo.REBEL_DROP_GUARANTEED_TIMES, null);
                    if (CheckNull.isNull(guaranteedDrops)) {
                        guaranteedDrops = new ArrayList<>();
                        player.typeInfo.put(Constant.TypeInfo.REBEL_DROP_GUARANTEED_TIMES, guaranteedDrops);
                    }
                    History rebelHistory = null;
                    if (CheckNull.nonEmpty(guaranteedDrops)) {
                        for (History history : guaranteedDrops) {
                            if (CheckNull.isNull(history) || history.getId() != staticBandit.getLv())
                                continue;
                            rebelHistory = history;
                            break;
                        }
                    }
                    if (Objects.nonNull(rebelHistory)) {
                        rebelHistory.setParam(0);
                    }
                }
            } else {
                if (CheckNull.nonEmpty(staticBandit.getAwardDrawing()) && CheckNull.nonEmpty(guaranteedConfigRule)) {
                    List<History> guaranteedDrops = player.typeInfo.getOrDefault(Constant.TypeInfo.REBEL_DROP_GUARANTEED_TIMES, null);
                    if (CheckNull.isNull(guaranteedDrops)) {
                        guaranteedDrops = new ArrayList<>();
                        player.typeInfo.put(Constant.TypeInfo.REBEL_DROP_GUARANTEED_TIMES, guaranteedDrops);
                    }
                    History rebelHistory = null;
                    if (CheckNull.nonEmpty(guaranteedDrops)) {
                        for (History history : guaranteedDrops) {
                            if (CheckNull.isNull(history) || history.getId() != staticBandit.getLv())
                                continue;
                            rebelHistory = history;
                            break;
                        }
                    }
                    if (CheckNull.isNull(rebelHistory)) {
                        rebelHistory = new History(staticBandit.getLv(), 0);
                        guaranteedDrops.add(rebelHistory);
                    }

                    int guaranteedRebelDrops = CheckNull.isNull(rebelHistory) ? 0 : rebelHistory.getParam();
                    if (guaranteedRebelDrops < guaranteedConfigRule.get(1) - 1) {
                        rebelHistory.setParam(++guaranteedRebelDrops);
                    } else {
                        // ????????????
                        CommonPb.Award award = null;
                        if (staticBandit.getAwardDrawing().size() == 1 && staticBandit.getAwardDrawing().get(0).size() >= 3) {
                            List<Integer> dropItem = staticBandit.getAwardDrawing().get(0);
                            award = rewardDataManager.sendRewardSignle(player, dropItem.get(0), dropItem.get(1), dropItem.get(2) * drawingNum, AwardFrom.BANDIT_DROP);
                        } else {
                            if (staticBandit.getLv() < 8) {
                                int totalWeight = 0;
                                for (List<Integer> l : staticBandit.getAwardDrawing()) {
                                    if (CheckNull.isEmpty(l) || l.size() < 4)
                                        continue;
                                    totalWeight += l.get(3);
                                }
                                int randomNum = RandomHelper.randomInSize(totalWeight);
                                int temp = 0;
                                for (List<Integer> dropItem : staticBandit.getAwardDrawing()) {
                                    if (CheckNull.isEmpty(dropItem) || dropItem.size() < 4)
                                        continue;
                                    temp += dropItem.get(3);
                                    if (temp >= randomNum) {
                                        award = rewardDataManager.sendRewardSignle(player, dropItem.get(0), dropItem.get(1), dropItem.get(2) * drawingNum, AwardFrom.BANDIT_DROP);
                                        break;
                                    }
                                }
                            } else {
                                // 8??????8???????????????????????????????????????
                                List<Integer> dropItem = null;
                                for (List<Integer> l : staticBandit.getAwardDrawing()) {
                                    if (CheckNull.isEmpty(l) || l.size() < 3)
                                        continue;
                                    StaticProp staticProp = StaticPropDataMgr.getPropMap(l.get(1));
                                    if (CheckNull.isNull(staticProp) || staticProp.getQuality() != Constant.Quality.green)
                                        continue;
                                    dropItem = l;
                                    break;
                                }
                                if (CheckNull.nonEmpty(dropItem)) {
                                    award = rewardDataManager.sendRewardSignle(player, dropItem.get(0), dropItem.get(1), dropItem.get(2) * drawingNum, AwardFrom.BANDIT_DROP);
                                }
                            }
                        }
                        // ???????????????????????????
                        if (Objects.nonNull(award)) dropList.add(award);
                        rebelHistory.setParam(0);
                    }
                }
            }

            int moveNum = activityDataManager.getActBanditMove(player);
            tmp = rewardDataManager.sendReward(player, staticBandit.getAwardProp(), moveNum, AwardFrom.BANDIT_DROP);
            if (tmp != null) {
                dropList.addAll(tmp);
            }
            tmp = rewardDataManager.sendReward(player, staticBandit.getAwardOthers(), AwardFrom.BANDIT_DROP);
            if (tmp != null) {
                dropList.addAll(tmp);
            }
            // ?????????????????????
            tmp = rewardDataManager.sendReward(player, staticBandit.getAwardPlanePieces(), AwardFrom.BANDIT_DROP);
            if (tmp != null) {
                dropList.addAll(tmp);
            }
            // ????????????????????????
            tmp = activityDataManager.upActBanditAcce(player);
            if (tmp != null) {
                dropList.addAll(tmp);
            }
            // ????????????????????????
            tmp = activityDataManager.getActGestapoAward(player, staticBandit.getLv());
            if (tmp != null) {
                dropList.addAll(tmp);
            }
            // ??????????????????
            tmp = activityDataManager.getActHitDrop(player, staticBandit.getLv(), StaticActBandit.ACT_HIT_DROP_TYPE_2);
            if (tmp != null) {
                dropList.addAll(tmp);
            }
            // ????????????
            dropList.addAll(totemService.dropTotem(player, 2, AwardFrom.TOTOEM_DROP_PANJUN));

            // ??????????????????
            taskDataManager.updTask(player, TaskType.COND_BANDIT_LV_CNT, 1, staticBandit.getLv());
            battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_BANDIT_LV_CNT, 1);
            royalArenaService.updTaskSchedule(player.roleId, TaskType.COND_BANDIT_LV_CNT, 1);
            activityDataManager.updActivity(player, ActivityConst.ACT_ELIMINATE_BANDIT, 1, staticBandit.getLv(), true);

            //????????????-????????????
            ActivityDiaoChanService.completeTask(player, ETask.FIGHT_REBEL, staticBandit.getBanditId(), staticBandit.getLv());
            //????????????-????????????-????????????
            TaskService.processTask(player, ETask.FIGHT_REBEL, staticBandit.getBanditId(), staticBandit.getLv());
            TaskService.handleTask(player, ETask.FIGHT_REBEL, staticBandit.getBanditId(), staticBandit.getLv());
            //??????-????????????
            titleService.processTask(player, ETask.FIGHT_REBEL);
            if (!bandit_task_999) {
                // 30?????????????????????
                if (taskDataManager.isOpenWorldTask(player)) {
                    taskDataManager.updWorldTaskSelf(player.roleId, TaskType.WORLD_TASK_TYPE_BANDIT, 1,
                            staticBandit.getLv());
                }
                // ?????????????????????5?????????+1???????????????
                if (MapHelper.getAreaIdByPos(pos) == WorldConstant.AREA_TYPE_13) {
                    int camp = player.lord.getCamp();
                    addHomeCityExpBy5Cnt(camp, worldDataManager.getCampCapitalBanditCnt());
                }
                // ??????????????????
                // worldDataManager.refreshBandit(pos, staticBandit.getLv());
                StaticNightRaidMgr.incrNightRaidBandit(player, now, staticBandit.getLv()); // ???????????????????????????++
                syncNightRaidRs(player, now, staticBandit.getLv());// ???????????????
                // ????????????++
                player.setBanditCnt(player.getBanditCnt() + 1);
                // ??????????????????
                activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_ATK_BANDIT, staticBandit.getLv());
                robinHoodService.updateTaskSchedule(player, ActivityConst.ACT_TASK_ATK_BANDIT, 1, staticBandit.getLv());
                // ???????????? ?????????????????????
                honorDailyService.addAndCheckHonorReport2s(player, HonorDailyConstant.COND_ID_3);
                // ??????????????????????????????
                campService.addAndCheckPartySupply(player, PartyConstant.SupplyType.KILL_BANDIT,
                        staticBandit.getBanditId());
                // ????????????????????????: ???????????????????????????
                worldScheduleService.updateScheduleGoal(player, ScheduleConstant.GOAL_COND_ATK_GESTAPO_BANDIT, 0);
                // ????????????????????????????????????
                worldWarSeasonDailyRestrictTaskService.updatePlayerDailyRestrictTaskAttackBandit(player, staticBandit);
            }
            //????????????????????????????????????????????????????????????
            PlayerOnHook playerOnHook = player.getPlayerOnHook();
            if (Objects.nonNull(playerOnHook) && playerOnHook.getState() == 1 && playerOnHook.getAskLastAnnihilateNumber() != 0) {
                playerOnHook.setAskLastAnnihilateNumber(playerOnHook.getAskLastAnnihilateNumber() - 1);
            }
            // ????????????
            CommonPb.RptAtkBandit rpt_ = rpt.build();
            mailDataManager.sendReportMail(player, worldService.createAtkBanditReport(rpt_, now),
                    MailConstant.MOLD_ATK_BANDIT_SUCC, dropList, now, tParam, cParam, recoverArmyAward);
            // ????????????????????????
            DataResource.ac.getBean(DrawCardPlanTemplateService.class).updateFunctionData(player, FunctionTrigger.DEFEAT_THE_ROBBER, 1);
        } else {
            if (!bandit_task_999) {
                // ???????????? ?????????????????????
                honorDailyService.addAndCheckHonorReport2s(player, HonorDailyConstant.COND_ID_9);
            }
            CommonPb.RptAtkBandit rpt_ = rpt.build();
            mailDataManager.sendReportMail(player, worldService.createAtkBanditReport(rpt_, now),
                    MailConstant.MOLD_ATK_BANDIT_FAIL, null, now, tParam, cParam, recoverArmyAward);
        }
        LogLordHelper.commonLog("attckBandit", AwardFrom.COMMON, player, staticBandit.getBanditId(), isSuccess);
        //????????????
        EventDataUp.battle(player.account, player.lord, attacker, "atk", String.valueOf(banditId), String.valueOf(WorldConstant.BATTLE_TYPE_BANDIT),
                String.valueOf(fightLogic.getWinState()), lord.getLordId(), rpt.getAtkHeroList());
        // ????????????????????????????????????????????????
        if (worldService.checkCurTaskHasBandit(staticBandit.getLv(), historyLv) || bandit_task_999) {
            retreatArmyByMarchTime(player, army, now, Constant.ATTACK_BANDIT_MARCH_TIME);
        } else {
            // ??????????????????
            worldService.retreatArmy(player, army, now);
        }
        // ?????????????????????????????????
        worldService.sendRoleResChange(changeMap);
        return false;
    }

    /**
     * ?????? - ?????????
     *
     * @param player
     * @param rebelLv
     */
    public List<CommonPb.Award> onHookBandit(Player player, int rebelLv) {
        List<CommonPb.Award> dropList = new ArrayList<>();

        StaticBandit staticBandit = StaticBanditDataMgr.getBanditMap().get(rebelLv);
        if (Objects.isNull(staticBandit)) {
            return dropList;
        }
        if (player.getBanditCnt() >= Constant.ATTACK_BANDIT_MAX) {
            return dropList;
        }
        // ????????????
        double medalNum = 1d;
        // ??????????????????
        int num = activityDataManager.getActDoubleNum(player);
        // ??????????????????
        double resNum = activityDataManager.getActBanditRes(player);
        // ????????????
        double cityBuffer = worldDataManager.getCityBuffer(worldDataManager.checkCityBuffer(player.lord.getPos()),
                WorldConstant.CityBuffer.CABINET_AWARD_BUFFER, player.roleId);
        // ??????buff
        Effect goldEffect = player.getEffect().get(EffectConstant.BANDIT_GOLD_BUFFER);
        // ??????buff
        Effect woodEffect = player.getEffect().get(EffectConstant.BANDIT_WOOD_BUFFER);
        List<List<Integer>> baseAwards = new ArrayList<>();
        for (List<Integer> award : staticBandit.getAwardBase()) {
            List<Integer> newAward = new ArrayList<>();
            int awardType = award.get(0);
            int awardId = award.get(1);
            newAward.add(awardType);
            newAward.add(awardId);
            // ???????????? = (?????????????????? * ???????????? * 999???????????? * 509???????????? * (1 + ????????????))
            int count = (int) (award.get(2) * medalNum * num * resNum * (1 + cityBuffer));
            if (awardType == AwardType.RESOURCE && awardId == AwardType.Resource.OIL && !CheckNull.isNull(goldEffect)) {
                int effectVal = goldEffect.getEffectVal();
                if (effectVal > 0) {
                    int add = (int) (award.get(2) * (effectVal / Constant.TEN_THROUSAND));
                    if (add > 0) {
                        count += add;
                    }
                }
            } else if (awardType == AwardType.RESOURCE && awardId == AwardType.Resource.ELE && !CheckNull.isNull(woodEffect)) {
                int effectVal = woodEffect.getEffectVal();
                if (effectVal > 0) {
                    int add = (int) (award.get(2) * (effectVal / Constant.TEN_THROUSAND));
                    if (add > 0) {
                        count += add;
                    }
                }
            }
            newAward.add(count);
            baseAwards.add(newAward);
        }
        dropList.addAll(PbHelper.createAwardsPb(baseAwards));

        List<List<Integer>> tmpList = new ArrayList<>();
        int drawingNum = activityDataManager.getActBanditDrawing(player);
        Optional.ofNullable(staticBandit.getAwardDrawing()).ifPresent(tmps -> tmps.forEach(tmp -> {
            if (tmp.size() == 4) {
                if (!RandomHelper.isHitRangeIn10000(tmp.get(3))) {
                    return;
                }
            }
            List<Integer> tmp_ = new ArrayList<>();
            tmp_.add(tmp.get(0));
            tmp_.add(tmp.get(1));
            tmp_.add(tmp.get(2) * drawingNum);
            tmpList.add(tmp_);
        }));
        dropList.addAll(PbHelper.createAwardsPb(tmpList));

        tmpList.clear();
        int moveNum = activityDataManager.getActBanditMove(player);
        Optional.ofNullable(staticBandit.getAwardProp()).ifPresent(tmps -> tmps.forEach(tmp -> {
            if (tmp.size() == 4) {
                if (!RandomHelper.isHitRangeIn10000(tmp.get(3))) {
                    return;
                }
            }
            List<Integer> tmp_ = new ArrayList<>();
            tmp_.add(tmp.get(0));
            tmp_.add(tmp.get(1));
            tmp_.add(tmp.get(2) * moveNum);
            tmpList.add(tmp_);
        }));
        dropList.addAll(PbHelper.createAwardsPb(tmpList));

        tmpList.clear();
        Optional.ofNullable(staticBandit.getAwardOthers()).ifPresent(tmps -> tmps.forEach(tmp -> {
            if (tmp.size() == 4) {
                if (!RandomHelper.isHitRangeIn10000(tmp.get(3))) {
                    return;
                }
            }
            List<Integer> tmp_ = new ArrayList<>();
            tmp_.add(tmp.get(0));
            tmp_.add(tmp.get(1));
            tmp_.add(tmp.get(2));
            tmpList.add(tmp_);
        }));
        dropList.addAll(PbHelper.createAwardsPb(tmpList));

        tmpList.clear();
        Optional.ofNullable(staticBandit.getAwardPlanePieces()).ifPresent(tmps -> tmps.forEach(tmp -> {
            if (tmp.size() == 4) {
                if (!RandomHelper.isHitRangeIn10000(tmp.get(3))) {
                    return;
                }
            }
            List<Integer> tmp_ = new ArrayList<>();
            tmp_.add(tmp.get(0));
            tmp_.add(tmp.get(1));
            tmp_.add(tmp.get(2));
            tmpList.add(tmp_);
        }));
        dropList.addAll(PbHelper.createAwardsPb(tmpList));

        ////????????????
        activityDataManager.upActBanditAcce(player);
        activityDataManager.getActGestapoAward(player, staticBandit.getLv());
        activityDataManager.getActHitDrop(player, staticBandit.getLv(), StaticActBandit.ACT_HIT_DROP_TYPE_2);

        // ????????????
        dropList.addAll(totemService.dropTotem(player, 2, AwardFrom.TOTOEM_DROP_PANJUN));

        // ??????????????????
        taskDataManager.updTask(player, TaskType.COND_BANDIT_LV_CNT, 1, staticBandit.getLv());
        battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_BANDIT_LV_CNT, 1);
        royalArenaService.updTaskSchedule(player.roleId, TaskType.COND_BANDIT_LV_CNT, 1);
        activityDataManager.updActivity(player, ActivityConst.ACT_ELIMINATE_BANDIT, 1, staticBandit.getLv(), true);

        //????????????-????????????
        ActivityDiaoChanService.completeTask(player, ETask.FIGHT_REBEL, staticBandit.getBanditId(), staticBandit.getLv());
        //????????????-????????????-????????????
        TaskService.processTask(player, ETask.FIGHT_REBEL, staticBandit.getBanditId(), staticBandit.getLv());
        TaskService.handleTask(player, ETask.FIGHT_REBEL, staticBandit.getBanditId(), staticBandit.getLv());
        //??????-????????????
        titleService.processTask(player, ETask.FIGHT_REBEL);

        boolean bandit_task_999 = WorldConstant.BANDIT_LV_999 == staticBandit.getLv();
        if (!bandit_task_999) {
            // 30?????????????????????
            if (taskDataManager.isOpenWorldTask(player)) {
                taskDataManager.updWorldTaskSelf(player.roleId, TaskType.WORLD_TASK_TYPE_BANDIT, 1, staticBandit.getLv());
            }

            player.setBanditCnt(player.getBanditCnt() + 1);

            activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_ATK_BANDIT, staticBandit.getLv());
            robinHoodService.updateTaskSchedule(player, ActivityConst.ACT_TASK_ATK_BANDIT, 1, staticBandit.getLv());
            // ???????????? ?????????????????????
            honorDailyService.addAndCheckHonorReport2s(player, HonorDailyConstant.COND_ID_3);
            // ??????????????????????????????
            campService.addAndCheckPartySupply(player, PartyConstant.SupplyType.KILL_BANDIT, staticBandit.getBanditId());
            // ????????????????????????: ???????????????????????????
            worldScheduleService.updateScheduleGoal(player, ScheduleConstant.GOAL_COND_ATK_GESTAPO_BANDIT, 0);
            // ????????????????????????????????????
            worldWarSeasonDailyRestrictTaskService.updatePlayerDailyRestrictTaskAttackBandit(player, staticBandit);
        }

        int subTotal = RandomUtil.randomIntIncludeEnd(Constant.ONHOOK_1065.get(0), Constant.ONHOOK_1065.get(1));
        this.onHookSubArmy(player, subTotal);

        return dropList;
    }

    public void onHookSubArmy(Player player, int sub) {
        int subTmp = sub / 3;
        Map<Integer, Integer> armys = player.getPlayerOnHook().getArmys();
        long sum = armys.values().stream().collect(Collectors.summarizingInt(x -> x.intValue())).getSum();
        if (sum < sub) {
            return;
        }
        for (EArmyType eArmyType : EArmyType.values()) {
            if (sub <= 0) {
                break;
            }
            int count = armys.get(eArmyType.getType());
            if (count <= 0) {
                continue;
            } else {
                int n;
                if (subTmp == 0) {
                    if (count >= sub) {
                        n = sub;
                    } else {
                        n = count;
                    }
                } else {
                    if (count >= subTmp) {
                        n = subTmp;
                    } else {
                        n = count;
                    }
                }
                armys.put(eArmyType.getType(), count - n);
                sub -= n;
            }
        }

        if (sub > 0) {
            this.onHookSubArmy(player, sub);
        }
    }

    /**
     * ???????????????????????????????????????
     *
     * @param player
     * @param now
     * @param banditLv
     */
    private void syncNightRaidRs(Player player, int now, int banditLv) {
        StaticNightRaid nightRaid = StaticNightRaidMgr.getNightRaid();
        int attackBanditCnt = StaticNightRaidMgr.getNightRaidBanditCnt(player, now);
        if (nightRaid.hasByBanditLv(banditLv) && player != null && player.isLogin && player.ctx != null) {
            GamePb4.SyncNightRaidRs.Builder b = GamePb4.SyncNightRaidRs.newBuilder();
            b.setAttackBanditCnt(attackBanditCnt);
            BasePb.Base.Builder builder = PbHelper.createSynBase(GamePb4.SyncNightRaidRs.EXT_FIELD_NUMBER,
                    GamePb4.SyncNightRaidRs.ext, b.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, builder.build(), player.roleId));
        }
    }

    /**
     * ????????????????????????
     *
     * @param player
     * @param army
     * @param now
     * @param marchTime
     */
    private void retreatArmyByMarchTime(Player player, Army army, int now, int marchTime) {
        if (null == player || null == army || army.isRetreat()) {
            return;
        }

        army.setState(ArmyConstant.ARMY_STATE_RETREAT);
        army.setDuration(marchTime);
        army.setEndTime(now + marchTime);

        Hero hero;
        for (CommonPb.TwoInt twoInt : army.getHero()) {
            hero = player.heros.get(twoInt.getV1());
            hero.setState(ArmyConstant.ARMY_STATE_RETREAT);
        }
    }

    /**
     * ???????????????????????????????????????1???
     *
     * @param camp
     * @param campCntMap
     */
    private void addHomeCityExpBy5Cnt(int camp, Map<Integer, Integer> campCntMap) {
        City city = worldDataManager.checkHasHome(camp);
        if (city != null) {
            Integer cnt = campCntMap.get(camp);
            cnt = cnt == null ? 0 : cnt;
            cnt += 1;
            if (cnt % 5 == 0) {
                cnt = 0;

                cityService.addHomeCityPopulation(city, 1);
            }
            campCntMap.put(camp, cnt);
        }
    }

    private void fightMine(Player player, Army army, int now) {
        int pos = army.getTarget();
        //?????????????????????????????????, ??????????????????????????????
        worldDataManager.removeCollectMineTeam(player.lord.getLordId(), pos);

        StaticMine staticMine = worldDataManager.getMineByPos(pos);
        if (null == staticMine) {
            worldService.noMineRetreat(player, army, now);
        } else {
            if (worldDataManager.hasGuard(pos)) {
                // ???????????????????????????????????????
                worldService.fightMineGuard(player, army, now);
            } else {
                // ??????????????????????????????
                worldService.collectArmy(player, army, now);
            }
        }
    }

    /**
     * ????????????????????????
     *
     * @param player
     * @param army
     * @param now
     */
    private void guardArmy(Player player, Army army, int now) {
        int pos = army.getTarget();
        Player targetPlayer = worldDataManager.getPosData(pos);
        long lordId = army.getLordId();
        long tarLordId = army.getTarLordId();
        Player lord = playerDataManager.getPlayer(lordId);
        Player target = playerDataManager.getPlayer(tarLordId);
        Turple<Integer, Integer> xyInArea = MapHelper.reducePos(pos);
        int heroId = 0;
        if (!CheckNull.isEmpty(army.getHero())) {
            heroId = army.getHero().get(0).getV1();
        }

        // ?????????????????????, ??????????????????????????????
        if (targetPlayer == null) {
            mailDataManager.sendNormalMail(lord, MailConstant.MOLD_GARRISON_RETREAT, now, player.lord.getNick(), heroId,
                    player.lord.getNick(), heroId);
            worldService.retreatArmyByDistance(player, army, now);
            return;
        }

        // ?????????????????????????????????, ??????????????????, ??????????????????
        if (!CheckNull.isNull(lord) && !CheckNull.isNull(target) && lord.getDecisiveInfo().isDecisive()
                || target.getDecisiveInfo().isDecisive()) {
            // ???????????????????????????????????????
            mailDataManager.sendNormalMail(player, MailConstant.DECISIVE_BATTLE_GARRISON_CANCEL, now,
                    target.lord.getNick(), xyInArea.getA(), xyInArea.getB(), heroId, target.lord.getNick(),
                    xyInArea.getA(), xyInArea.getB(), heroId);
            worldService.retreatArmyByDistance(player, army, now);
            return;
        }
        StaticBuildingLv buildingLv = StaticBuildingDataMgr.getStaticBuildingLevel(BuildingType.WALL,
                targetPlayer.building.getWall());
        int max = buildingLv != null && !CheckNull.isEmpty(buildingLv.getCapacity())
                ? buildingLv.getCapacity().get(0).get(0) : 0;
        List<Army> guardArmys = worldDataManager.getPlayerGuard(pos);

        if (guardArmys != null && guardArmys.size() >= max) {
            // ????????????????????????
            worldService.retreatArmyByDistance(player, army, now);
            String nick = targetPlayer.lord.getNick();
            mailDataManager.sendNormalMail(player, MailConstant.WALL_HELP_FILL, TimeHelper.getCurrentSecond(), nick,
                    heroId, nick, heroId);
            return;
        }

        army.setState(ArmyConstant.ARMY_STATE_GUARD);
        // ??????????????????????????????
        int maxTime = Constant.ARMY_STATE_GUARD_TIME * TimeHelper.HOUR_S;// ??????????????????????????????
        army.setDuration(maxTime);
        army.setEndTime(now + maxTime);

        worldDataManager.addPlayerGuard(pos, army);

        Hero hero;
        for (CommonPb.TwoInt twoInt : army.getHero()) {
            hero = player.heros.get(twoInt.getV1());
            hero.setState(ArmyConstant.ARMY_STATE_GUARD);
        }

        worldService.synWallCallBackRs(1, army);
    }

    /**
     * ??????????????????Battle???????????????????????????warService??????????????????
     *
     * @param player
     * @param army
     * @param now
     */
    private void fightGestapo(Player player, Army army, int now) {
        Integer battleId = army.getBattleId();
        LogUtil.debug("??????????????????id, roleId:", player.roleId, ", battleId:", battleId);
        if (null == battleId) {
            battleId = 0;
        }
        Battle battle = warDataManager.getBattleMap().get(battleId);
        LogUtil.debug("????????????????????????, roleId:", player.roleId, ", battle:", battle);

        if (null == battle) {
            // ????????????????????????????????????
            worldService.retreatArmy(player, army, now);
            worldService.synRetreatArmy(player, army, now);
            return;
        }

        // ??????????????????
        army.setState(ArmyConstant.ARMY_GESTAPO_BATTLE);

        Hero hero;
        List<Integer> heroIdList = new ArrayList<>();
        for (CommonPb.TwoInt twoInt : army.getHero()) {
            hero = player.heros.get(twoInt.getV1());
            hero.setState(ArmyConstant.ARMY_GESTAPO_BATTLE);
            heroIdList.add(hero.getHeroId());
        }
        int camp = player.lord.getCamp();
        if (camp == battle.getAtkCamp()) {
            worldService.addBattleArmy(battle, player.roleId, heroIdList, army.getKeyId(), true);
        }
        LogUtil.debug("??????????????????????????????, roleId:", player.roleId, ", battle:", battle);
    }

    /**
     * ??????????????????Battle?????????
     *
     * @param player
     * @param army
     * @param now
     */
    private void fightLightningWar(Player player, Army army, int now) {
        Integer battleId = army.getBattleId();
        LogUtil.debug("??????????????????id, roleId:", player.roleId, ", battleId:", battleId);
        if (null == battleId) {
            battleId = 0;
        }
        Battle battle = warDataManager.getSpecialBattleMap().get(battleId);
        LogUtil.debug("????????????????????????, roleId:", player.roleId, ", battle:", battle);

        if (null == battle) {
            // ????????????????????????????????????
            worldService.retreatArmy(player, army, now);
            worldService.synRetreatArmy(player, army, now);
            return;
        }

        // ??????????????????
        army.setState(ArmyConstant.ARMY_LIGHTNING_WAR);

        Hero hero;
        List<Integer> heroIdList = new ArrayList<>();
        for (CommonPb.TwoInt twoInt : army.getHero()) {
            hero = player.heros.get(twoInt.getV1());
            hero.setState(ArmyConstant.ARMY_LIGHTNING_WAR);
            heroIdList.add(hero.getHeroId());
        }
        int camp = player.lord.getCamp();
        if (camp != battle.getDefCamp()) {
            worldService.addBattleArmy(battle, player.roleId, heroIdList, army.getKeyId(), true);
        }
        LogUtil.debug("??????????????????????????????, roleId:", player.roleId, ", battle:", battle);
    }

    /**
     * ??????????????????
     *
     * @param player
     * @param army
     * @param now
     */
    private void fightCabinetLead(Player player, Army army, int now) {
        int pos = army.getTarget();
        CabinetLead lead = worldDataManager.getCabinetLeadByPos(pos);
        if (null == lead) {
            LogUtil.error("???????????????????????????, pos:", pos);
            // ??????????????????
            Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
            mailDataManager.sendReportMail(player, null, MailConstant.MOLD_ATK_TARGET_NOT_FOUND, null, now, xy.getA(),
                    xy.getB(), xy.getA(), xy.getB());
            // ????????????
            worldService.retreatArmy(player, army, now);
            return;
        }
        StaticCabinetPlan staticCabinetPlan = StaticBuildingDataMgr.getCabinetPlanById(lead.getCabinetPlanId());

        if (null == staticCabinetPlan
                || null == StaticBanditDataMgr.getBanditMap().get(staticCabinetPlan.getBanditId())) {

            LogUtil.error("?????????????????????, StaticCabinetPlanId:", lead.getCabinetPlanId());
            // ??????????????????
            Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
            mailDataManager.sendReportMail(player, null, MailConstant.MOLD_ATK_TARGET_NOT_FOUND, null, now, xy.getA(),
                    xy.getB());
            // ????????????
            worldService.retreatArmy(player, army, now);
            return;
        }

        int banditId = staticCabinetPlan.getBanditId();
        StaticBandit staticBandit = StaticBanditDataMgr.getBanditMap().get(banditId);

        for (Integer npcId : staticBandit.getForm()) {
            StaticNpc npc = StaticNpcDataMgr.getNpcMap().get(npcId);
            if (null == npc) {
                LogUtil.error("NPCid?????????, npcId:", npcId);
                // ????????????
                worldService.retreatArmy(player, army, now);
                return;
            }
        }

        // ????????????
        Fighter attacker = fightService.createFighter(player, army.getHero());
        Fighter defender = fightService.createBanditFighter(banditId);
        FightLogic fightLogic = new FightLogic(attacker, defender, true);
        fightLogic.fight();

        //????????????-??????????????????
        ActivityDiaoChanService.killedAndDeathTask0(attacker, false, true);
        ActivityDiaoChanService.killedAndDeathTask0(defender, false, true);

        // ????????????
        Map<Long, List<CommonPb.Award>> recoverArmyAwardMap = new HashMap<>();
        // ????????????????????????????????????, key:roleId
        Map<Long, ChangeInfo> changeMap = new HashMap<>();
        // ????????????
        worldService.subHeroArm(player, attacker.forces, AwardFrom.ATTACK_BANDIT, changeMap);
        if (attacker.lost > 0) {
            // ????????????????????????????????????
            medalDataManager.angelInWhite(attacker, recoverArmyAwardMap);
            //????????????????????????---????????????
            seasonTalentService.execSeasonTalentEffect303(attacker, recoverArmyAwardMap);
            // ????????????
            activityDataManager.updRankActivity(player, ActivityConst.ACT_ARMY_RANK, attacker.lost);
            // ????????????????????????
            honorDailyDataManager.addAndCheckHonorReport2s(player, HonorDailyConstant.COND_ID_14, attacker.lost);
            // ?????????????????????
            battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_SUB_HERO_ARMY, attacker.lost);
        }

        // ????????????
        CommonPb.Record record = fightLogic.generateRecord();
        Lord lord = player.lord;
        // ??????????????????
        boolean isSuccess = fightLogic.getWinState() == ArmyConstant.FIGHT_RESULT_SUCCESS;

        CommonPb.RptAtkBandit.Builder rpt = CommonPb.RptAtkBandit.newBuilder();
        rpt.setResult(isSuccess);
        rpt.setAttack(PbHelper.createRptMan(lord.getPos(), lord.getNick(), lord.getVip(), lord.getLevel()));
        rpt.setDefend(PbHelper.createRptBandit(banditId, pos));
        rpt.setAtkSum(PbHelper.createRptSummary(attacker.total, attacker.lost, lord.getCamp(), lord.getNick(),
                lord.getPortrait(), player.getDressUp().getCurPortraitFrame()));
        rpt.setDefSum(PbHelper.createRptSummary(defender.total, defender.lost, 0, null, -1, -1));

        // ??????????????????
        rpt.addAllAtkHero(fightSettleLogic.banditFightHeroExpReward(player, attacker.forces));
        DataResource.ac.getBean(WorldService.class).buildRptHeroData(defender, rpt, false);
        rpt.setRecord(record);

        // ??????????????????
        List<String> tParam = new ArrayList<>();
        tParam.add(lord.getNick());
        tParam.add(String.valueOf(staticBandit.getBanditId()));
        // ????????????
        List<String> cParam = new ArrayList<>();
        cParam.add(lord.getNick());
        Turple<Integer, Integer> xy = MapHelper.reducePos(lord.getPos());
        cParam.add(String.valueOf(xy.getA()));
        cParam.add(String.valueOf(xy.getB()));
        cParam.add(String.valueOf(staticBandit.getBanditId()));
        xy = MapHelper.reducePos(pos);
        cParam.add(String.valueOf(xy.getA()));
        cParam.add(String.valueOf(xy.getB()));

        // ?????????????????????????????????
        if (isSuccess) {
            // ??????????????????????????????
            worldDataManager.removeBandit(pos, 1);

            List<CommonPb.Award> dropList = new ArrayList<>();
            List<List<Integer>> rewardList = new ArrayList<>();
            int taskCount = 0;

            List<List<Integer>> awardOthers = staticBandit.getAwardOthers();
            if (!CheckNull.isEmpty(awardOthers)) {
                for (int i = 0; i < awardOthers.size() - 1; i++) {
                    rewardList.add(staticBandit.getAwardOthers().get(i));
                }
                // ????????????????????????????????????????????????
                List<Integer> taskProp = awardOthers.get(awardOthers.size() - 1);
                if (taskProp.size() >= 3) {
                    taskCount = taskProp.get(2);
                    dropList.add(PbHelper.createAwardPb(taskProp.get(0), taskProp.get(1), taskProp.get(2)));
                }
            }

            List<CommonPb.Award> tmp = rewardDataManager.sendReward(player, rewardList, AwardFrom.CABINET_LEAD_DROP);// "??????????????????"
            if (tmp != null) {
                dropList.addAll(tmp);
            }
            tmp = rewardDataManager.sendReward(player, staticBandit.getAwardDrawing(), AwardFrom.CABINET_LEAD_DROP);
            if (tmp != null) {
                dropList.addAll(tmp);
            }
            tmp = rewardDataManager.sendReward(player, staticBandit.getAwardProp(), AwardFrom.CABINET_LEAD_DROP);
            if (tmp != null) {
                dropList.addAll(tmp);
            }
            tmp = rewardDataManager.sendReward(player, staticBandit.getAwardBase(), AwardFrom.CABINET_LEAD_DROP);
            if (tmp != null) {
                dropList.addAll(tmp);
            }
            // ?????????????????????
            tmp = rewardDataManager.sendReward(player, staticBandit.getAwardPlanePieces(), AwardFrom.CABINET_LEAD_DROP);
            if (tmp != null) {
                dropList.addAll(tmp);
            }

            cParam.add(lord.getNick());
            cParam.add(String.valueOf(lead.getCamp()));

            List<CommonPb.Award> recoverList = null;
            if (recoverArmyAwardMap.containsKey(player.roleId)) {
                recoverList = recoverArmyAwardMap.get(player.roleId);
            }
            mailDataManager.sendReportMail(player, worldService.createAtkBanditReport(rpt.build(), now),
                    MailConstant.MOLD_ATK_LEAD_SUCC, dropList, now, tParam, cParam, recoverList);

            // ????????????????????????????????????
            campService.addCabinetLeadExp(lead.getCamp(), taskCount);

            // ??????????????????????????????
            Player targetPlayer = playerDataManager.getPlayer(lead.getRoleId());
            if (targetPlayer != null && targetPlayer.cabinet != null) {
                Cabinet cabinet = targetPlayer.cabinet;
                cabinet.setLeadStep(cabinet.getLeadStep() + 1);
            }

        } else {
            mailDataManager.sendReportMail(player, worldService.createAtkBanditReport(rpt.build(), now),
                    MailConstant.MOLD_ATK_LEAD_FAIL, null, now, tParam, cParam);
        }
        // ????????????
        worldService.retreatArmy(player, army, now);
        // ?????????????????????????????????
        worldService.sendRoleResChange(changeMap);
    }

    /**
     * ??????????????????CityInfo???RoleQueue?????????
     *
     * @param player
     * @param army
     * @param now
     */
    public void fightBerlinWar(Player player, Army army, int now) {
        Integer atkType = army.getBattleId();
        LogUtil.debug("??????????????????id, roleId:", player.roleId, ", atkType:", atkType);
        int cityId = army.getTargetId();
        BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
        if (CheckNull.isNull(berlinWar)) {
            // ????????????????????????????????????
            worldService.retreatArmy(player, army, now);
            worldService.synRetreatArmy(player, army, now);
            return;
        }

        BerlinCityInfo cityInfo = berlinWar.getCityInfoByCityId(cityId);
        if (CheckNull.isNull(cityInfo)) {
            // ????????????????????????????????????
            worldService.retreatArmy(player, army, now);
            worldService.synRetreatArmy(player, army, now);
            return;
        }

        // ??????????????????
        army.setState(ArmyConstant.ARMY_BERLIN_WAR);

        // ??????BerlinForce??????
        CommonPb.TwoInt twoInt = army.getHero().get(0);
        Hero hero = player.heros.get(twoInt.getV1());
        hero.setState(ArmyConstant.ARMY_BERLIN_WAR);
        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(twoInt.getV1());
        if (null == staticHero) {
            LogUtil.error("??????Fighter???heroId?????????, heroId:", twoInt.getV1());
            // ????????????????????????????????????
            worldService.retreatArmy(player, army, now);
            worldService.synRetreatArmy(player, army, now);
            return;
        }
        int atkOrDef = cityInfo.getCamp() == player.lord.getCamp() ? WorldConstant.BERLIN_DEF
                : WorldConstant.BERLIN_ATK;
        BerlinForce berlinForce = fightService.createBerlinForce(player, staticHero, twoInt.getV1(), twoInt.getV2(),
                atkOrDef, atkType, now, player.lord.getCamp());
        cityInfo.getRoleQueue().add(berlinForce);
        LogUtil.debug("??????????????????????????????, roleId:", player.roleId, ", berlinForce:", berlinForce);
    }

    /**
     * ??????????????????
     *
     * @param player
     * @param army
     * @param now
     */
    private void fightDecisiveWar(Player player, Army army, int now) {

        // ????????????????????????????????????
        Integer battleId = army.getBattleId();
        LogUtil.debug("??????????????????id, roleId:", player.roleId, ", battleId:", battleId);
        if (null == battleId) {
            battleId = 0;
        }
        Battle battle = warDataManager.getBattleMap().get(battleId);
        LogUtil.debug("????????????????????????, roleId:", player.roleId, ", battle:", battle);

        if (null == battle) {
            // ????????????????????????????????????
            worldService.retreatArmy(player, army, now);
            worldService.synRetreatArmy(player, army, now);
            return;
        }

        // ??????????????????
        army.setState(ArmyConstant.ARMY_STATE_BATTLE);

        Hero hero;
        // int armCount = 0;
        List<Integer> heroIdList = new ArrayList<>();
        for (CommonPb.TwoInt twoInt : army.getHero()) {
            hero = player.heros.get(twoInt.getV1());
            hero.setState(ArmyConstant.ARMY_STATE_BATTLE);
            heroIdList.add(hero.getHeroId());
        }

        LogUtil.debug("??????????????????????????????, roleId:", player.roleId, ", battle:", battle);

    }

    /**
     * ????????????????????????
     *
     * @param player
     * @param army
     * @param now
     */
    private void fightAirShip(Player player, Army army, int now) {
        int pos = army.getTarget();
        int airShipKey = army.getTargetId();
        Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
        AirshipWorldData airShip = worldDataManager.getAirshipWorldDataMap().get(pos);
        if (CheckNull.isNull(airShip) || airShip.isRefreshStatus() || airShipKey != airShip.getKeyId()) { // ???????????????, ????????????,
            // ????????????????????????
            mailDataManager.sendNormalMail(player, MailConstant.MOLD_ATK_TARGET_NOT_FOUND, now, xy.getA(), xy.getB(),
                    xy.getA(), xy.getB());
            worldService.retreatArmy(player, army, now);
            worldService.synRetreatArmy(player, army, now);
            return;
        }
        StaticAirship sAirship = StaticWorldDataMgr.getAirshipMap().get(airShip.getId());
        if (CheckNull.isNull(sAirship)) {
            // ????????????????????????
            mailDataManager.sendNormalMail(player, MailConstant.MOLD_ATK_TARGET_NOT_FOUND, now, xy.getA(), xy.getB(),
                    xy.getA(), xy.getB());
            worldService.retreatArmy(player, army, now);
            worldService.synRetreatArmy(player, army, now);
            return;
        }

        // ??????hero??????
        Hero hero;
        List<Integer> heroIdList = new ArrayList<>();
        for (CommonPb.TwoInt twoInt : army.getHero()) {
            hero = player.heros.get(twoInt.getV1());
            hero.setState(ArmyConstant.ARMY_STATE_ATTACK_AIRSHIP_WAIT);
            heroIdList.add(hero.getHeroId());
        }

        // long belongRoleId = airShip.getBelongRoleId();
        // if (belongRoleId == 0) { // ?????????????????????
        // fightAirBelongLogic(player, army, airShip);
        // } else{}
        int camp = player.lord.getCamp();
        List<CommonPb.BattleRole> battleRoles = airShip.getJoinRoles().get(camp);
        if (CheckNull.isNull(battleRoles)) {
            battleRoles = new ArrayList<>();
            airShip.getJoinRoles().put(camp, battleRoles);
        }

        // ??????????????????
        army.setState(ArmyConstant.ARMY_STATE_ATTACK_AIRSHIP_WAIT);

        // ?????????????????????
        battleRoles.add(CommonPb.BattleRole.newBuilder().setKeyId(army.getKeyId()).setRoleId(player.roleId)
                .addAllHeroId(heroIdList).build());

        // ????????????????????????????????????
        long joinRoleCnt = battleRoles.stream().mapToLong(CommonPb.BattleRole::getRoleId).distinct().count();
        if (joinRoleCnt >= sAirship.getParticipate()) {
            // ????????????, ????????????
            fightAirShip(airShip, battleRoles, player.lord.getCamp());
        } else if (joinRoleCnt == 1) {
            // ???????????????????????????
            campService.syncRallyBattle(player, null, airShip);
        }
    }

    /**
     * ????????????
     *
     * @param airShip
     * @param battleRoles
     * @param camp
     */
    private void fightAirShip(AirshipWorldData airShip, List<CommonPb.BattleRole> battleRoles, int camp) {

        int now = TimeHelper.getCurrentSecond();

        int airShipId = airShip.getId();
        int airShipPos = airShip.getPos();
        int areaId = airShip.getAreaId();
        Turple<Integer, Integer> defPos = MapHelper.reducePos(airShipPos);

        // ???????????????, ????????????????????????
        // Player belongRole = playerDataManager.getPlayer(airShip.getBelongRoleId());
        // if (CheckNull.isNull(belongRole)) {
        // LogUtil.error("??????????????????, airShip:", airShipId, ", pos:", airShipPos);
        // return;
        // }

        // ????????????
        Fighter attacker = fightService.createFighterByBattleRole(battleRoles);
        Fighter defender = fightService.createFighter(airShip.getNpc());
        FightLogic fightLogic = new FightLogic(attacker, defender, true);
        fightLogic.fight();

        //????????????-??????????????????
        ActivityDiaoChanService.killedAndDeathTask0(attacker, false, true);
        ActivityDiaoChanService.killedAndDeathTask0(defender, false, true);

        boolean atkSuccess = fightLogic.getWinState() == ArmyConstant.FIGHT_RESULT_SUCCESS;

        // ?????????????????????
        Set<Long> retreatPlayers;

        // ????????????
        Map<Long, List<CommonPb.Award>> recoverArmyAwardMap = new HashMap<>();
        // ????????????????????????????????????, key:roleId
        Map<Long, ChangeInfo> changeMap = new HashMap<>();
        Map<Long, List<CommonPb.Award>> dropMap = new HashMap<>();

        // ?????????????????????
        if (attacker.lost > 0) {
            warService.subBattleHeroArm(attacker.forces, changeMap, AwardFrom.AIR_SHIP_BATTLE);
        }

        // ????????????????????????????????????
        medalDataManager.angelInWhite(attacker, recoverArmyAwardMap);
        //????????????????????????---????????????
        seasonTalentService.execSeasonTalentEffect303(attacker, recoverArmyAwardMap);
        // ????????????-????????????????????????
        medalDataManager.sustainTheWarByMeansOfWar(attacker, defender, recoverArmyAwardMap, atkSuccess);

        // Report??????
        Player firstAttackPlayer = playerDataManager.getPlayer(battleRoles.get(0).getRoleId());
        CommonPb.RptAtkPlayer.Builder rpt = createAirShipRptBuilder(camp, attacker, defender, fightLogic, atkSuccess,
                firstAttackPlayer, airShipId, airShipPos);
        CommonPb.Report.Builder report = worldService.createAtkPlayerReport(rpt.build(), now);

        StaticAirship sAirShip = StaticWorldDataMgr.getAirshipMap().get(airShipId);
        if (CheckNull.isNull(sAirShip)) {
            LogUtil.error("??????????????????, ?????????????????????, airShipId", airShipId);
            return;
        }

        // ???????????????????????????
        List<Player> battlePlayer = battleRoles.stream()
                // ????????????????????????????????????
                .filter(role -> role.getRoleId() != airShip.getBelongRoleId())
                .mapToLong(CommonPb.BattleRole::getRoleId)
                // ????????????
                .distinct()
                .mapToObj(roleId -> playerDataManager.getPlayer(roleId)).filter(p -> !CheckNull.isNull(p))
                .collect(Collectors.toList());

        // ???????????????????????????
        if (CheckNull.isEmpty(battlePlayer)) {
            return;
        }

        if (atkSuccess) { // ???????????????

            // ???????????????
            // if (belongRole.getAndCreateAirshipPersonData().getBelongAwardCnt() > 0) {
            // belongRole.getAndCreateAirshipPersonData().subBelongAwardCnt(1);
            // List<CommonPb.Award> awards = rewardDataManager.sendReward(belongRole, sAirShip.getAwardBelonging(),
            // AwardFrom.AIR_SHIP_BATTLE_AWARD);
            // if (!CheckNull.isEmpty(awards)) {
            // mailDataManager.sendAttachMail(belongRole, null, MailConstant.MOLD_AIR_SHIP_BELONG_AWARD,
            // AwardFrom.AIR_SHIP_BATTLE_AWARD, now, awards, airShipId, defPos.getA(), defPos.getB());
            // }
            // } else {
            // mailDataManager.sendNormalMail(belongRole, MailConstant.MOLD_AIR_SHIP_BELONG_AWARD_MAX, now);
            // }
            //
            // ????????????????????????
            chatDataManager.sendSysChat(ChatConst.CHAT_AIR_SHIP_DEAD, airShip.getAreaId(), 0, airShip.getId(), camp, battlePlayer.get(0).lord.getNick());
            // ?????????????????????, ????????????????????????buff
            airshipService.incrementAndGetKill(areaId, camp);
            // Optional.ofNullable(StaticWorldDataMgr.getAreaMap().get(areaId))
            //         .ifPresent(sArea -> Optional.ofNullable(StaticWorldDataMgr.getAirshipAreaMap().get(sArea.getOpenOrder()))
            //                 .ifPresent(sAirArea -> {
            //                     // ????????????????????????
            //                     if (sAirArea.getNum() == killNum) {
            //                         airshipService.updateBuff(sAirArea, areaId);
            //                     }
            //                 }));

            // ?????????????????????????????????
            retreatPlayers = airShip.getJoinRoles()
                    .values()
                    .stream()
                    .flatMap(Collection::stream)
                    .map(CommonPb.BattleRole::getRoleId)
                    .collect(Collectors.toSet());

            // ?????????????????????
            airshipService.removeAirshipFromMap(airShip, now, AirshipWorldData.STATUS_DEAD_REFRESH);
        } else {
            // ?????????????????????
            if (defender.lost > 0) {
                airShip.getNpc().clear();
                for (Force force : defender.forces) {
                    if (force.alive()) {
                        airShip.getNpc().add(new NpcForce(force.id, force.hp, force.curLine));
                    }
                }
            }

            // ???????????????????????????(??????)
            campService.syncCancelRallyBattle(null, null, airShip);
            // ??????????????????
            retreatPlayers = airShip.getJoinRoles().remove(camp).stream().map(CommonPb.BattleRole::getRoleId)
                    .collect(Collectors.toSet());
        }

        // ?????????, ??????????????????
        battlePlayer.forEach(p -> {
            List<CommonPb.Award> drops = dropMap.computeIfAbsent(p.roleId, (k) -> new ArrayList<>());
            // List<CommonPb.Award> drops = dropMap.get(p.roleId);
            // if (CheckNull.isNull(drops)) {
            //     drops = new ArrayList<>();
            //     dropMap.put(p.roleId, drops);
            // }
            if (atkSuccess) {
                // ??????????????????
                if (Constant.AIRSHIP_CAN_AWARD_CNT.get(0) == p.getAndCreateAirshipPersonData().getKillAwardCnt()) {
                    List<CommonPb.Award> awards = rewardDataManager.sendReward(p, sAirShip.getAwardFirst(), AwardFrom.AIR_SHIP_BATTLE_AWARD);
                    if (!CheckNull.isEmpty(awards)) {
                        drops.addAll(awards);
                    }
                    taskDataManager.updTask(p, TaskType.COND_522, 1);
                }
                // ??????????????????????????????
                p.getAndCreateAirshipPersonData().subKillAwardCnt(1);
                activityDataManager.updDay7ActSchedule(p, ActivityConst.ACT_TASK_MULTI_BANDIT_CNT, sAirShip.getLv());
                //??????????????????
                List<CommonPb.Award> actHitDrop = activityDataManager.getActHitDrop(p, sAirShip.getLv(), StaticActBandit.ACT_HIT_DROP_TYPE_3);
                drops.addAll(actHitDrop);

                //????????????-??????????????????
                ActivityDiaoChanService.completeTask(p, ETask.FIGHT_ELITE_REBEL, sAirShip.getId(), sAirShip.getLv());
                //????????????-????????????-??????????????????
                TaskService.processTask(p, ETask.FIGHT_ELITE_REBEL, sAirShip.getId(), sAirShip.getLv());
            }
            if (p.getAndCreateAirshipPersonData().getAttendAwardCnt() > 0) {
                // ??????????????????????????????
                p.getAndCreateAirshipPersonData().subAttendAwardCnt(1);

                // ????????????
                List<CommonPb.Award> awards = rewardDataManager.sendReward(p, sAirShip.getAwardRegular(), AwardFrom.AIR_SHIP_BATTLE_AWARD);
                if (!CheckNull.isEmpty(awards)) {
                    drops.addAll(awards);
                }
                // ????????????
                List<Integer> randomAward = RandomUtil.getRandomByWeight(sAirShip.getAwardExtra(), 3, false);
                if (!CheckNull.isEmpty(randomAward)) {
                    awards = rewardDataManager.sendReward(p, Collections.singletonList(randomAward), AwardFrom.AIR_SHIP_BATTLE_AWARD);
                    if (!CheckNull.isEmpty(awards)) {
                        drops.addAll(awards);
                    }
                }
                //??????-??????????????????(????????????????????????5???)
                titleService.processTask(p, ETask.FIGHT_ELITE_REBEL);
            } else {
                mailDataManager.sendNormalMail(p, MailConstant.MOLD_AIR_SHIP_HELP_AWARD_MAX, now);
            }
        });

        List<Integer> posList = new ArrayList<>();

        // ???????????????Player
        retreatPlayers.forEach(roleId -> {
            Player p = playerDataManager.getPlayer(roleId);
            if (CheckNull.isNull(p)) {
                return;
            }
            p.armys.values().stream()
                    .filter(army -> army.getType() == ArmyConstant.ARMY_TYPE_ATTACK_AIRSHIP
                            && army.getTarget() == airShipPos && army.getState() != ArmyConstant.ARMY_STATE_RETREAT)
                    .forEach(army -> {
                        worldService.retreatArmy(p, army, now);
                        worldService.synRetreatArmy(p, army, now);
                    });
            posList.add(p.lord.getPos());
            // ?????????????????????, ????????????
            if (battleRoles.stream().anyMatch(battleRole -> battleRole.getRoleId() == p.roleId)) {
                Turple<Integer, Integer> atkPos = MapHelper.reducePos(p.lord.getPos());
                mailDataManager.sendReportMail(p, report,
                        atkSuccess ? MailConstant.MOLD_AIR_SHIP_BATTLE_SUC : MailConstant.MOLD_AIR_SHIP_BATTLE_FAIL,
                        dropMap.get(p.roleId), now, recoverArmyAwardMap, p.lord.getNick(), airShipId, p.lord.getNick(),
                        atkPos.getA(), atkPos.getB(), airShipId, defPos.getA(), defPos.getB());
            } else {
                if (atkSuccess) { // ?????????????????????
                    mailDataManager.sendNormalMail(p, MailConstant.MOLD_AIR_SHIP_DEAD, now, airShipId, airShipId);
                }
            }
        });

        posList.add(airShipPos);
        if (!CheckNull.isEmpty(posList)) {
            EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));
        }

        logAirShipBattle(areaId, battleRoles, atkSuccess, airShip.getKeyId() + "_" + airShipId, airShipPos, attacker, firstAttackPlayer, rpt.getAtkHeroList(), String.valueOf(airShipId));
    }

    /**
     * ??????????????????
     *
     * @param areaId
     * @param battleRoles
     * @param atkSuccess
     * @param airShipKeyIdAndId
     * @param airShipPos
     */
    public void logAirShipBattle(int areaId, List<CommonPb.BattleRole> battleRoles, boolean atkSuccess, String airShipKeyIdAndId,
                                 int airShipPos, Fighter attacker, Player firstAttackPlayer, List<CommonPb.RptHero> atkHero, Object... param) {
        String win;
        if (atkSuccess) {
            win = String.valueOf(ArmyConstant.FIGHT_RESULT_SUCCESS);
        } else {
            win = String.valueOf(ArmyConstant.FIGHT_RESULT_FAIL);
        }
        battleRoles.stream().map(rb -> rb.getRoleId()).distinct().map(rId -> playerDataManager.getPlayer(rId))
                .filter(p -> p != null)
                .forEach(player -> {
                    LogLordHelper.otherLog("airShipBattle", player.account.getServerId(), player.roleId,
                            "atk", areaId, airShipKeyIdAndId, atkSuccess, airShipPos, player.lord.getCamp());
                    //????????????
                    EventDataUp.battle(player.account, player.lord, attacker, "atk", airShipKeyIdAndId,
                            String.valueOf(WorldConstant.BATTLE_TYPE_AIRSHIP), win, firstAttackPlayer.roleId, atkHero, param);
                });
    }

    public CommonPb.RptAtkPlayer.Builder createAirShipRptBuilder(int camp, Fighter attacker, Fighter defender,
                                                                 FightLogic fightLogic, boolean atkSuccess, Player belongRole, int airShipId, int airShipPos) {
        // ????????????
        CommonPb.RptAtkPlayer.Builder rpt = CommonPb.RptAtkPlayer.newBuilder();
        rpt.setResult(atkSuccess);
        // ????????????????????????
        rpt.setAttack(PbHelper.createRptMan(belongRole.lord.getPos(), belongRole.lord.getNick(),
                belongRole.lord.getVip(), belongRole.lord.getLevel()));
        rpt.setDefCity(PbHelper.createRptCityPb(airShipId, airShipPos));
        rpt.setAtkSum(PbHelper.createRptSummary(attacker.total, attacker.lost, camp, belongRole.lord.getNick(), belongRole.lord.getPortrait(), belongRole.getDressUp().getCurPortraitFrame()));
        rpt.setDefSum(PbHelper.createRptSummary(defender.total, defender.lost, 0, null, -1, -1));
        for (Force force : attacker.forces) {
            CommonPb.RptHero rptHero = fightService.forceToRptHeroNoExp(force);
            if (rptHero != null) {
                rpt.addAtkHero(rptHero);
            }
        }
        DataResource.ac.getBean(WorldService.class).buildRptHeroData(defender, rpt, Constant.Role.BANDIT, false);
        CommonPb.Record record = fightLogic.generateRecord();
        rpt.setRecord(record);
        return rpt;
    }

    /**
     * ?????????????????????
     *
     * @param player
     * @param army
     * @param airShip
     */
    private void fightAirBelongLogic(Player player, Army army, AirshipWorldData airShip) {
        if (CheckNull.isNull(player) || CheckNull.isNull(army) || CheckNull.isNull(airShip)) return;

        // ???????????????
        airShip.setBelongRoleId(player.roleId);

        // ??????????????????????????????
        int pos = airShip.getPos();
        int id = airShip.getId();
        chatDataManager.sendSysChat(ChatConst.CHAT_AIR_SHIP_GET_BELONG, airShip.getAreaId(), 0, player.lord.getCamp(),
                player.lord.getNick(), airShip.getId(), airShip.getPos());

        int now = TimeHelper.getCurrentSecond();

        worldDataManager.getMarchMap().values().stream().flatMap(map -> map.values().stream())
                .map(march -> march.getArmy())
                .filter(a -> a.getType() == ArmyConstant.ARMY_TYPE_ATTACK_AIRSHIP && a.getTarget() == pos)
                .map(a -> a.getLordId()).distinct().map(roleId -> playerDataManager.getPlayer(roleId.longValue()))
                .filter(p -> !CheckNull.isNull(p)).forEach(p -> {
                    // mailDataManager.sendNormalMail(p, Objects.equals(player.roleId, p.roleId) ? // ??????????????????
                    //                 MailConstant.MOLD_AIR_SHIP_GET_BELONG : MailConstant.MOLD_AIR_SHIP_NOT_GET_BELONG, now, id, id,
                    //         pos);
                    p.armys.values()
                            .stream().filter(a -> a.getType() == ArmyConstant.ARMY_TYPE_ATTACK_AIRSHIP
                                    && a.getTarget() == pos && a.getState() != ArmyConstant.ARMY_STATE_RETREAT)
                            .forEach(a -> {
                                worldService.retreatArmy(p, a, now);
                                worldService.synRetreatArmy(p, a, now);
                            });
                });
    }

}
