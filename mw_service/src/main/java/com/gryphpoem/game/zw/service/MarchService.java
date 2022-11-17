package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.dataMgr.*;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonDailyRestrictTaskService;
import com.gryphpoem.game.zw.logic.FightSettleLogic;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.BattlePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Fighter;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.pojo.p.NpcForce;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.*;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
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
     * 行军结束处理逻辑
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
                // 城战、阵营战
                LogUtil.debug("部队行军结束，参加战斗, roleId:", player.roleId, ", army:", army);
                fightPlayer(player, army, now);
            } else {
                LogUtil.debug("部队行军结束，未找到目标，返回, roleId:", player.roleId, ", army:", army);
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
            // 攻打点兵统领
            fightCabinetLead(player, army, now);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_ATK_CABINET_TASK) {
            // 攻击流寇任务
            fightBandit4Taks(player, army, now);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_ATK_GESTAPO) {
            // 攻击盖世太保
            Gestapo gestapo = worldDataManager.getGestapoByPos(pos);
            if (!CheckNull.isNull(gestapo)) {
                LogUtil.debug("部队行军结束，参加战斗, roleId:", player.roleId, ", army:", army);
                fightGestapo(player, army, now);
                worldService.synRetreatArmy(player, army, now);
            } else {
                LogUtil.debug("部队行军结束，未找到目标，返回, roleId:", player.roleId, ", army:", army);
                Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
                mailDataManager.sendReportMail(player, null, MailConstant.MOLD_ATK_GESTAPO_NOT_FOUND, null, now,
                        xy.getA(), xy.getB(), xy.getA(), xy.getB());
                worldService.retreatArmy(player, army, now);
            }
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_LIGHTNING_WAR) {
            // 攻打闪电战
            LightningWarBoss lightningWarBoss = worldDataManager.getLightningWarBossByArea(player.lord.getArea());
            if (!CheckNull.isNull(lightningWarBoss) && !lightningWarBoss.isNotInitOrDead()) {
                LogUtil.debug("部队行军结束，参加战斗, roleId:", player.roleId, ", army:", army);
                fightLightningWar(player, army, now);
            } else {
                LogUtil.debug("部队行军结束，未找到目标，返回, roleId:", player.roleId, ", army:", army);
                Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
                mailDataManager.sendReportMail(player, null, MailConstant.MOLD_ATK_GESTAPO_NOT_FOUND, null, now,
                        xy.getA(), xy.getB(), xy.getA(), xy.getB());
                worldService.retreatArmy(player, army, now);
            }
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_COLLECT_SUPERMINE) {
            // 超级矿点采集
            superMineService.marchEndcollectSuperMineLogic(player, army, now);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_ATK_SUPERMINE) {
            // 超级矿点攻击
            superMineService.marchEndAtkSuperMineLogic(player, army, now);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_HELP_SUPERMINE) {
            // 超级矿点驻防
            superMineService.marchEndHelpSuperMineLogic(player, army, now);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_BERLIN_WAR
                || army.getType() == ArmyConstant.ARMY_TYPE_BATTLE_FRONT_WAR) {
            // 参与柏林会战
            BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
            if (!CheckNull.isNull(berlinWar) && berlinWar.getStatus() == WorldConstant.BERLIN_STATUS_OPEN) {
                LogUtil.debug("部队行军结束，参加战斗, roleId:", player.roleId, ", army:", army);
                fightBerlinWar(player, army, now);
            } else {
                LogUtil.debug("部队行军结束，活动结束，返回, roleId:", player.roleId, ", army:", army);
                Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
                mailDataManager.sendReportMail(player, null, MailConstant.MOLD_ATK_TARGET_NOT_FOUND, null, now,
                        xy.getA(), xy.getB(), xy.getA(), xy.getB());
                worldService.retreatArmy(player, army, now);
            }
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_REBEL_BATTLE) {
            rebelService.marchEndRebelHelpArmy(player, army, now);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_COUNTER_BOSS_DEF) {
            // 反攻德意志BOSS防守ARMY
            CounterAttack counterAttack = globalDataManager.getGameGlobal().getCounterAttack();
            if (!CheckNull.isNull(counterAttack) && !counterAttack.isNotInitOrDead()) {
                counterAtkService.marchEndBossDefArmy(player, army, now);
            } else {
                LogUtil.debug("部队行军结束，活动结束，返回, roleId:", player.roleId, ", army:", army);
                Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
                mailDataManager.sendReportMail(player, null, MailConstant.MOLD_ATK_TARGET_NOT_FOUND, null, now,
                        xy.getA(), xy.getB(), xy.getA(), xy.getB());
                worldService.retreatArmy(player, army, now);
            }

        } else if (army.getType() == ArmyConstant.ARMY_TYPE_COUNTER_BOSS_ATK_HELP) {
            // 反攻德意志BOSS进攻
            // 玩家驻防
            counterAtkService.marchEndBossAtkHelpArmy(player, army, now);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_DECISIVE_BATTLE) {
            // 决战
            LogUtil.debug("部队行军结束，参加战斗, roleId:", player.roleId, ", army:", army);
            fightDecisiveWar(player, army, now);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_ATTACK_AIRSHIP) {
            // 攻打飞艇
            fightAirShip(player, army, now);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_ATTACK_SCHEDULE_BOSS) {
            // 攻打世界boss
            // worldScheduleService.fightSchedBossLogic(player, army, now);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_ALTAR) {
            ramadanVisitAltarService.marchEnd(player, army, now);
            // 不论是否拜访成功, 都需要返回行军
            worldService.retreatArmy(player, army, now);
            worldService.synRetreatArmy(player, army, now);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_RELIC_BATTLE) {
            relicsFightService.marchEnd(player, army, now);
        }
    }

    /**
     * 返回行军结束逻辑
     *
     * @param player
     * @param army
     */
    public void retreatEnd(Player player, Army army) {
        // 移除行军路线
        worldDataManager.removeMarch(player, army);
        LogUtil.common("部队返回完成, army:", army);

        // 部队返回

        // 将领返回
        Hero hero;
        for (CommonPb.PartnerHeroIdPb twoInt : army.getHero()) {
            hero = player.heros.get(twoInt.getPrincipleHeroId());
            if (Objects.nonNull(hero)) {
                hero.setState(ArmyConstant.ARMY_STATE_IDLE);
            }
            if (CheckNull.nonEmpty(twoInt.getDeputyHeroIdList())) {
                twoInt.getDeputyHeroIdList().forEach(heroId -> {
                    Hero hero_ = player.heros.get(heroId);
                    if (CheckNull.isNull(hero_)) return;
                    hero_.setState(ArmyConstant.ARMY_STATE_IDLE);
                });
            }
        }

        // 加资源
        if (!CheckNull.isEmpty(army.getGrab())) {
            List<CommonPb.Award> grab = army.getGrab();
            rewardDataManager.sendRewardByAwardList(player, grab, AwardFrom.COLLECT);
            // 给都城加经验
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
                        LogUtil.debug("皇城采集时间满足,加都城经验 addExp:", addExp);
                    }
                    worldDataManager.getCampCollectTime().put(camp, time);
                }
            }
        }
        // 部队回城补兵一次
        warService.autoFillArmy(player);
        // 地图通知
        List<Integer> posList = MapHelper
                .getAreaStartPos(MapHelper.getLineAcorss(army.getTarget(), player.lord.getPos()));
        posList.add(army.getTarget());
        posList.add(player.lord.getPos());
        EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.LINE_TYPE));
    }

    /**
     * 把玩家加入到Battle的攻或者守方中，在warService里面触发战斗
     *
     * @param player
     * @param army
     * @param now
     */
    private void fightPlayer(Player player, Army army, int now) {
        // 把玩家部队加入城战部队中
        // int pos = army.getTarget();
        // Integer battleId = player.battleMap.get(pos);
        Integer battleId = army.getBattleId();
        LogUtil.debug("玩家参加战斗id, roleId:", player.roleId, ", battleId:", battleId);
        if (null == battleId) {
            battleId = 0;
        }
        Battle battle = warDataManager.getBattleMap().get(battleId);
        LogUtil.debug("玩家参加战斗信息, roleId:", player.roleId, ", battle:", battle);

        if (null == battle) {
            // 战斗对象未找到，部队返回
            worldService.retreatArmy(player, army, now);
            worldService.synRetreatArmy(player, army, now);
            return;
        }

        if (battle.isCityBattle() && battle.getDefencer() != null) {
            // 对方开启保护
            Effect effect = battle.getDefencer().getEffect().get(EffectConstant.PROTECT);
            if (effect != null && effect.getEndTime() > now) {
                LogUtil.debug("该坐标开启保护，不能攻击", player.roleId, ", battle:", battle);
                worldService.retreatArmy(player, army, now);
                worldService.synRetreatArmy(player, army, now);
                // 发邮件
                Player defencer = battle.getDefencer();
                String nick = defencer.lord.getNick();
                Turple<Integer, Integer> rPos = MapHelper.reducePos(defencer.lord.getPos());
                mailDataManager.sendNormalMail(player, MailConstant.MOLD_ATTACK_TARGET_HAS_PROTECT, now, nick,
                        rPos.getA(), rPos.getB(), nick, rPos.getA(), rPos.getB());
                return;
            }
        }

        // 设置部队状态
        army.setState(ArmyConstant.ARMY_STATE_BATTLE);

        Hero hero;
        // int armCount = 0;
        for (CommonPb.PartnerHeroIdPb twoInt : army.getHero()) {
            hero = player.heros.get(twoInt.getPrincipleHeroId());
            hero.setState(ArmyConstant.ARMY_STATE_BATTLE);
        }

        int camp = player.lord.getCamp();
        if (camp == battle.getAtkCamp()) {
            worldService.addBattleArmy(battle, player.roleId, army.getHero(), army.getKeyId(), true);
        } else {
            worldService.addBattleArmy(battle, player.roleId, army.getHero(), army.getKeyId(), false);
        }

        LogUtil.debug("最终玩家参加战斗信息, roleId:", player.roleId, ", battle:", battle);
    }

    /**
     * 攻击流寇
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
     * 攻击任务流寇
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
     * 攻击流寇战斗逻辑
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
        // 兵力恢复
        Map<Long, List<CommonPb.Award>> recoverArmyAwardMap = new HashMap<>();
        if (null == staticBandit) {
            LogUtil.debug("流寇id未配置或已消失, banditId:", banditId);
            // 发送邮件通知
            Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
            mailDataManager.sendReportMail(player, null, MailConstant.MOLD_ATK_TARGET_NOT_FOUND, null, now,
                    recoverArmyAwardMap, xy.getA(), xy.getB(), xy.getA(), xy.getB());

            // 部队返回
            worldService.retreatArmy(player, army, now);
            return true;
        }

        // 解救任务
        boolean bandit_task_999 = WorldConstant.BANDIT_LV_999 == staticBandit.getLv();
        // 跨级打
        Integer historyLv = player.trophy.get(TrophyConstant.TROPHY_1);
        historyLv = historyLv != null ? historyLv : 0;
        if (!bandit_task_999 && staticBandit.getLv() > historyLv + 1) {
            LogUtil.common("跨级攻打流寇, banditId:", banditId);
            // 发送邮件通知
            Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
            mailDataManager.sendReportMail(player, null, MailConstant.MOLD_ATK_TARGET_NOT_FOUND, null, now,
                    recoverArmyAwardMap, xy.getA(), xy.getB());

            // 部队返回
            worldService.retreatArmy(player, army, now);
            return true;
        }

        StaticNpc npc;
        for (Integer npcId : staticBandit.getForm()) {
            npc = StaticNpcDataMgr.getNpcMap().get(npcId);
            if (null == npc) {
                LogUtil.error("NPCid未配置, npcId:", npcId);

                // 部队返回
                worldService.retreatArmy(player, army, now);
                return true;
            }
        }

        // 战斗计算
        Fighter attacker = fightService.createFighter(player, army.getHero());
        Fighter defender = fightService.createBanditFighter(banditId);
        FightLogic fightLogic = new FightLogic(attacker, defender, true);
        fightLogic.start();

        //貂蝉任务-杀敌阵亡数量
        ActivityDiaoChanService.killedAndDeathTask0(attacker, false, true);

        // 记录玩家有改变的资源类型, key:roleId
        Map<Long, ChangeInfo> changeMap = new HashMap<>();
        // 兵力恢复
        List<CommonPb.Award> recoverArmyAward = new ArrayList<>();
        // 损兵处理
        worldService.subHeroArm(player, attacker.forces, AwardFrom.ATTACK_BANDIT, changeMap);
        if (attacker.lost > 0) {
            // 损兵排行
            activityDataManager.updRankActivity(player, ActivityConst.ACT_ARMY_RANK, attacker.lost);
            // 荣耀日报损兵进度
            honorDailyDataManager.addAndCheckHonorReport2s(player, HonorDailyConstant.COND_ID_14, attacker.lost);
            // 战令的损兵进度
            battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_SUB_HERO_ARMY, attacker.lost);
            // 可以恢复的兵力
            List<List<Integer>> armyAward = worldService.attckBanditLostRecvCalc(player, attacker.forces, now,
                    staticBandit.getLv(), WorldConstant.LOST_RECV_CALC_NIGHT);
            if (!CheckNull.isEmpty(armyAward)) {
                recoverArmyAward = rewardDataManager.sendReward(player, armyAward, AwardFrom.RECOVER_ARMY);// "夜袭功能或医疗箱兵力恢复"
            }
            // 执行勋章白衣天使特技逻辑
            medalDataManager.angelInWhite(attacker, recoverArmyAwardMap);
            //执行赛季天赋技能---伤病恢复
            seasonTalentService.execSeasonTalentEffect303(attacker, recoverArmyAwardMap);
            if (!CheckNull.isEmpty(recoverArmyAwardMap)) {
                List<CommonPb.Award> awards = recoverArmyAwardMap.get(player.roleId);
                if (!CheckNull.isEmpty(awards)) {
                    recoverArmyAward.addAll(awards);
                }
            }
        }

        // 战斗记录
        BattlePb.BattleRoundPb record = fightLogic.generateRecord();

        Lord lord = player.lord;
        boolean isSuccess = fightLogic.getWinState() == FightConstant.FIGHT_RESULT_SUCCESS;
        CommonPb.RptAtkBandit.Builder rpt = CommonPb.RptAtkBandit.newBuilder();
        rpt.setResult(isSuccess);
        rpt.setAttack(PbHelper.createRptMan(lord.getPos(), lord.getNick(), lord.getVip(), lord.getLevel()));
        rpt.setDefend(PbHelper.createRptBandit(banditId, pos));
        rpt.setAtkSum(PbHelper.createRptSummary(attacker.total, attacker.lost, lord.getCamp(), lord.getNick(),
                lord.getPortrait(), player.getDressUp().getCurPortraitFrame()));
        rpt.setDefSum(PbHelper.createRptSummary(defender.total, defender.lost, 0, null, -1, -1));
        // 给将领加经验
        rpt.addAllAtkHero(fightSettleLogic.banditFightHeroExpReward(player, attacker.forces));
        DataResource.ac.getBean(WorldService.class).buildRptHeroData(defender, rpt, false);
        rpt.setRecord(record);

        // 邮件参数
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
        // 发送战斗奖励，发送战报
        if (isSuccess) {
            if (staticBandit.getLv() > historyLv && !bandit_task_999) {
                player.trophy.put(TrophyConstant.TROPHY_1, staticBandit.getLv());
            }
            //挂机 根据损并检查是否可挂机这个等级的叛军, 进攻成功
            if (attacker.lost <= Constant.ONHOOK_1066 && player.getPlayerOnHook().getMaxRebelLv() < staticBandit.getLv()) {
                player.getPlayerOnHook().setMaxRebelLv(staticBandit.getLv());
            }
            // 地图上移除流寇
            worldDataManager.removeBandit(pos, 0);

            List<CommonPb.Award> dropList = new ArrayList<>();
            // 勋章加成
            double medalNum = medalDataManager.aSurpriseAttackOnTheBanditArmy(attacker);
            // 获取活动翻倍
            int num = activityDataManager.getActDoubleNum(player);
            // 匪军资源活动
            double resNum = activityDataManager.getActBanditRes(player);
            // 名城加成
            double cityBuffer = worldDataManager.getCityBuffer(worldDataManager.checkCityBuffer(player.lord.getPos()),
                    WorldConstant.CityBuffer.CABINET_AWARD_BUFFER, player.roleId);
            // 金币buff
            Effect goldEffect = player.getEffect().get(EffectConstant.BANDIT_GOLD_BUFFER);
            // 木材buff
            Effect woodEffect = player.getEffect().get(EffectConstant.BANDIT_WOOD_BUFFER);
            List<List<Integer>> baseAwards = new ArrayList<>();
            for (List<Integer> award : staticBandit.getAwardBase()) {
                List<Integer> newAward = new ArrayList<>();
                int awardType = award.get(0);
                int awardId = award.get(1);
                newAward.add(awardType);
                newAward.add(awardId);
                // 奖励数量 = (配置资源数量 * 勋章加成 * 999活动加成 * 509活动加成 * (1 + 名城加成))
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
                        // 必中图纸
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
                                // 8级及8级以上的叛军只掉落绿色图纸
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
                        // 加入叛军掉落信息中
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
            // 掉落的飞机碎片
            tmp = rewardDataManager.sendReward(player, staticBandit.getAwardPlanePieces(), AwardFrom.BANDIT_DROP);
            if (tmp != null) {
                dropList.addAll(tmp);
            }
            // 更新匪军加速活动
            tmp = activityDataManager.upActBanditAcce(player);
            if (tmp != null) {
                dropList.addAll(tmp);
            }
            // 盖世太保活动奖励
            tmp = activityDataManager.getActGestapoAward(player, staticBandit.getLv());
            if (tmp != null) {
                dropList.addAll(tmp);
            }
            // 匪军掉落活动
            tmp = activityDataManager.getActHitDrop(player, staticBandit.getLv(), StaticActBandit.ACT_HIT_DROP_TYPE_2);
            if (tmp != null) {
                dropList.addAll(tmp);
            }
            // 图腾掉落
            dropList.addAll(totemService.dropTotem(player, 2, AwardFrom.TOTOEM_DROP_PANJUN));

            // 更新任务进度
            taskDataManager.updTask(player, TaskType.COND_BANDIT_LV_CNT, 1, staticBandit.getLv());
            battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_BANDIT_LV_CNT, 1);
            royalArenaService.updTaskSchedule(player.roleId, TaskType.COND_BANDIT_LV_CNT, 1);
            activityDataManager.updActivity(player, ActivityConst.ACT_ELIMINATE_BANDIT, 1, staticBandit.getLv(), true);

            //貂蝉任务-攻打流寇
            ActivityDiaoChanService.completeTask(player, ETask.FIGHT_REBEL, staticBandit.getBanditId(), staticBandit.getLv());
            //喜悦金秋-日出而作-攻打流寇
            TaskService.processTask(player, ETask.FIGHT_REBEL, staticBandit.getBanditId(), staticBandit.getLv());
            TaskService.handleTask(player, ETask.FIGHT_REBEL, staticBandit.getBanditId(), staticBandit.getLv());
            //称号-攻打流寇
            titleService.processTask(player, ETask.FIGHT_REBEL);
            if (!bandit_task_999) {
                // 30急开启世界任务
                if (taskDataManager.isOpenWorldTask(player)) {
                    taskDataManager.updWorldTaskSelf(player.roleId, TaskType.WORLD_TASK_TYPE_BANDIT, 1,
                            staticBandit.getLv());
                }
                // 同阵营玩家每打5个流寇+1点皇城经验
                if (MapHelper.getAreaIdByPos(pos) == WorldConstant.AREA_TYPE_13) {
                    int camp = player.lord.getCamp();
                    addHomeCityExpBy5Cnt(camp, worldDataManager.getCampCapitalBanditCnt());
                }
                // 流寇重新刷新
                // worldDataManager.refreshBandit(pos, staticBandit.getLv());
                StaticNightRaidMgr.incrNightRaidBandit(player, now, staticBandit.getLv()); // 夜袭功能的流寇数量++
                syncNightRaidRs(player, now, staticBandit.getLv());// 胜利后推送
                // 流寇上限++
                player.setBanditCnt(player.getBanditCnt() + 1);
                // 更新活动进度
                activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_ATK_BANDIT, staticBandit.getLv());
                robinHoodService.updateTaskSchedule(player, ActivityConst.ACT_TASK_ATK_BANDIT, 1, staticBandit.getLv());
                // 荣耀日报 打匪军成功更新
                honorDailyService.addAndCheckHonorReport2s(player, HonorDailyConstant.COND_ID_3);
                // 添加并且检测军团补给
                campService.addAndCheckPartySupply(player, PartyConstant.SupplyType.KILL_BANDIT,
                        staticBandit.getBanditId());
                // 更新世界目标进度: 攻打盖世太保或流寇
                worldScheduleService.updateScheduleGoal(player, ScheduleConstant.GOAL_COND_ATK_GESTAPO_BANDIT, 0);
                // 世界争霸攻打匪军完成记录
                worldWarSeasonDailyRestrictTaskService.updatePlayerDailyRestrictTaskAttackBandit(player, staticBandit);
            }
            //如果是挂机中野外打怪胜利也要算到剩余里面
            PlayerOnHook playerOnHook = player.getPlayerOnHook();
            if (Objects.nonNull(playerOnHook) && playerOnHook.getState() == 1 && playerOnHook.getAskLastAnnihilateNumber() != 0) {
                playerOnHook.setAskLastAnnihilateNumber(playerOnHook.getAskLastAnnihilateNumber() - 1);
            }
            // 发送战报
            CommonPb.RptAtkBandit rpt_ = rpt.build();
            mailDataManager.sendReportMail(player, worldService.createAtkBanditReport(rpt_, now),
                    MailConstant.MOLD_ATK_BANDIT_SUCC, dropList, now, tParam, cParam, recoverArmyAward);
            // 更新功能活动数据
            DataResource.ac.getBean(DrawCardPlanTemplateService.class).updateFunctionData(player, FunctionTrigger.DEFEAT_THE_ROBBER, 1);
        } else {
            if (!bandit_task_999) {
                // 荣耀日报 打匪军失败更新
                honorDailyService.addAndCheckHonorReport2s(player, HonorDailyConstant.COND_ID_9);
            }
            CommonPb.RptAtkBandit rpt_ = rpt.build();
            mailDataManager.sendReportMail(player, worldService.createAtkBanditReport(rpt_, now),
                    MailConstant.MOLD_ATK_BANDIT_FAIL, null, now, tParam, cParam, recoverArmyAward);
        }
        LogLordHelper.commonLog("attckBandit", AwardFrom.COMMON, player, staticBandit.getBanditId(), isSuccess);
        //上报数数
        EventDataUp.battle(player.account, player.lord, attacker, "atk", String.valueOf(banditId), String.valueOf(WorldConstant.BATTLE_TYPE_BANDIT),
                String.valueOf(fightLogic.getWinState()), lord.getLordId(), rpt.getAtkHeroList());
        // 判断当前任务列表中是否有流寇任务
        if (worldService.checkCurTaskHasBandit(staticBandit.getLv(), historyLv) || bandit_task_999) {
            retreatArmyByMarchTime(player, army, now, Constant.ATTACK_BANDIT_MARCH_TIME);
        } else {
            // 正常部队返回
            worldService.retreatArmy(player, army, now);
        }
        // 通知客户端玩家资源变化
        worldService.sendRoleResChange(changeMap);
        return false;
    }

    /**
     * 挂机 - 打叛军
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
        // 勋章加成
        double medalNum = 1d;
        // 获取活动翻倍
        int num = activityDataManager.getActDoubleNum(player);
        // 匪军资源活动
        double resNum = activityDataManager.getActBanditRes(player);
        // 名城加成
        double cityBuffer = worldDataManager.getCityBuffer(worldDataManager.checkCityBuffer(player.lord.getPos()),
                WorldConstant.CityBuffer.CABINET_AWARD_BUFFER, player.roleId);
        // 金币buff
        Effect goldEffect = player.getEffect().get(EffectConstant.BANDIT_GOLD_BUFFER);
        // 木材buff
        Effect woodEffect = player.getEffect().get(EffectConstant.BANDIT_WOOD_BUFFER);
        List<List<Integer>> baseAwards = new ArrayList<>();
        for (List<Integer> award : staticBandit.getAwardBase()) {
            List<Integer> newAward = new ArrayList<>();
            int awardType = award.get(0);
            int awardId = award.get(1);
            newAward.add(awardType);
            newAward.add(awardId);
            // 奖励数量 = (配置资源数量 * 勋章加成 * 999活动加成 * 509活动加成 * (1 + 名城加成))
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

        ////直接发放
        activityDataManager.upActBanditAcce(player);
        activityDataManager.getActGestapoAward(player, staticBandit.getLv());
        activityDataManager.getActHitDrop(player, staticBandit.getLv(), StaticActBandit.ACT_HIT_DROP_TYPE_2);

        // 图腾掉落
        dropList.addAll(totemService.dropTotem(player, 2, AwardFrom.TOTOEM_DROP_PANJUN));

        // 更新任务进度
        taskDataManager.updTask(player, TaskType.COND_BANDIT_LV_CNT, 1, staticBandit.getLv());
        battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_BANDIT_LV_CNT, 1);
        royalArenaService.updTaskSchedule(player.roleId, TaskType.COND_BANDIT_LV_CNT, 1);
        activityDataManager.updActivity(player, ActivityConst.ACT_ELIMINATE_BANDIT, 1, staticBandit.getLv(), true);

        //貂蝉任务-攻打流寇
        ActivityDiaoChanService.completeTask(player, ETask.FIGHT_REBEL, staticBandit.getBanditId(), staticBandit.getLv());
        //喜悦金秋-日出而作-攻打流寇
        TaskService.processTask(player, ETask.FIGHT_REBEL, staticBandit.getBanditId(), staticBandit.getLv());
        TaskService.handleTask(player, ETask.FIGHT_REBEL, staticBandit.getBanditId(), staticBandit.getLv());
        //称号-攻打流寇
        titleService.processTask(player, ETask.FIGHT_REBEL);

        boolean bandit_task_999 = WorldConstant.BANDIT_LV_999 == staticBandit.getLv();
        if (!bandit_task_999) {
            // 30急开启世界任务
            if (taskDataManager.isOpenWorldTask(player)) {
                taskDataManager.updWorldTaskSelf(player.roleId, TaskType.WORLD_TASK_TYPE_BANDIT, 1, staticBandit.getLv());
            }

            player.setBanditCnt(player.getBanditCnt() + 1);

            activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_ATK_BANDIT, staticBandit.getLv());
            robinHoodService.updateTaskSchedule(player, ActivityConst.ACT_TASK_ATK_BANDIT, 1, staticBandit.getLv());
            // 荣耀日报 打匪军成功更新
            honorDailyService.addAndCheckHonorReport2s(player, HonorDailyConstant.COND_ID_3);
            // 添加并且检测军团补给
            campService.addAndCheckPartySupply(player, PartyConstant.SupplyType.KILL_BANDIT, staticBandit.getBanditId());
            // 更新世界目标进度: 攻打盖世太保或流寇
            worldScheduleService.updateScheduleGoal(player, ScheduleConstant.GOAL_COND_ATK_GESTAPO_BANDIT, 0);
            // 世界争霸攻打匪军完成记录
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
     * 打完流寇的夜袭功能进度推送
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
     * 自定义时间的返回
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
        for (CommonPb.PartnerHeroIdPb twoInt : army.getHero()) {
            hero = player.heros.get(twoInt.getPrincipleHeroId());
            hero.setState(ArmyConstant.ARMY_STATE_RETREAT);
            if (CheckNull.nonEmpty(twoInt.getDeputyHeroIdList())) {
                twoInt.getDeputyHeroIdList().forEach(heroId -> {
                    Hero hero_ = player.heros.get(heroId);
                    if (Objects.nonNull(hero_)) {
                        hero_.setState(ArmyConstant.ARMY_STATE_RETREAT);
                    }
                });
            }
        }
    }

    /**
     * 做五次某事加本阵营都城经验1点
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
        //当前玩家采集已到达矿点, 移除当前玩家采集队列
        worldDataManager.removeCollectMineTeam(player.lord.getLordId(), pos);

        StaticMine staticMine = worldDataManager.getMineByPos(pos);
        if (null == staticMine) {
            worldService.noMineRetreat(player, army, now);
        } else {
            if (worldDataManager.hasGuard(pos)) {
                // 矿点已有人在采集，开启战斗
                worldService.fightMineGuard(player, army, now);
            } else {
                // 没有人采集，开始采集
                worldService.collectArmy(player, army, now);
            }
        }
    }

    /**
     * 驻防部队到达处理
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
            heroId = army.getHero().get(0).getPrincipleHeroId();
        }

        // 对方已被击飞了, 发送驻防部队返回邮件
        if (targetPlayer == null) {
            mailDataManager.sendNormalMail(lord, MailConstant.MOLD_GARRISON_RETREAT, now, player.lord.getNick(), heroId,
                    player.lord.getNick(), heroId);
            worldService.retreatArmyByDistance(player, army, now);
            return;
        }

        // 双方有一方处于决战状态, 驻防部队返回, 发送遣返邮件
        if (!CheckNull.isNull(lord) && !CheckNull.isNull(target) && lord.getDecisiveInfo().isDecisive()
                || target.getDecisiveInfo().isDecisive()) {
            // 给派兵驻防的玩家发遣返邮件
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
            // 对方驻防人数满了
            worldService.retreatArmyByDistance(player, army, now);
            String nick = targetPlayer.lord.getNick();
            mailDataManager.sendNormalMail(player, MailConstant.WALL_HELP_FILL, TimeHelper.getCurrentSecond(), nick,
                    heroId, nick, heroId);
            return;
        }

        army.setState(ArmyConstant.ARMY_STATE_GUARD);
        // 计算能驻防的最大时间
        int maxTime = Constant.ARMY_STATE_GUARD_TIME * TimeHelper.HOUR_S;// 计算能驻防的最大时间
        army.setDuration(maxTime);
        army.setEndTime(now + maxTime);

        worldDataManager.addPlayerGuard(pos, army);

        army.setHeroState(player, ArmyConstant.ARMY_STATE_GUARD);
        worldService.synWallCallBackRs(1, army);
    }

    /**
     * 把玩家加入到Battle的攻或者守方中，在warService里面触发战斗
     *
     * @param player
     * @param army
     * @param now
     */
    private void fightGestapo(Player player, Army army, int now) {
        Integer battleId = army.getBattleId();
        LogUtil.debug("玩家参加战斗id, roleId:", player.roleId, ", battleId:", battleId);
        if (null == battleId) {
            battleId = 0;
        }
        Battle battle = warDataManager.getBattleMap().get(battleId);
        LogUtil.debug("玩家参加战斗信息, roleId:", player.roleId, ", battle:", battle);

        if (null == battle) {
            // 战斗对象未找到，部队返回
            worldService.retreatArmy(player, army, now);
            worldService.synRetreatArmy(player, army, now);
            return;
        }

        // 设置部队状态
        army.setState(ArmyConstant.ARMY_GESTAPO_BATTLE);
        army.setHeroState(player, ArmyConstant.ARMY_GESTAPO_BATTLE);

        int camp = player.lord.getCamp();
        if (camp == battle.getAtkCamp()) {
            worldService.addBattleArmy(battle, player.roleId, army.getHero(), army.getKeyId(), true);
        }
        LogUtil.debug("最终玩家参加战斗信息, roleId:", player.roleId, ", battle:", battle);
    }

    /**
     * 把玩家加入到Battle的攻方
     *
     * @param player
     * @param army
     * @param now
     */
    private void fightLightningWar(Player player, Army army, int now) {
        Integer battleId = army.getBattleId();
        LogUtil.debug("玩家参加战斗id, roleId:", player.roleId, ", battleId:", battleId);
        if (null == battleId) {
            battleId = 0;
        }
        Battle battle = warDataManager.getSpecialBattleMap().get(battleId);
        LogUtil.debug("玩家参加战斗信息, roleId:", player.roleId, ", battle:", battle);

        if (null == battle) {
            // 战斗对象未找到，部队返回
            worldService.retreatArmy(player, army, now);
            worldService.synRetreatArmy(player, army, now);
            return;
        }

        // 设置部队状态
        army.setState(ArmyConstant.ARMY_LIGHTNING_WAR);
        army.setHeroState(player, ArmyConstant.ARMY_LIGHTNING_WAR);
        int camp = player.lord.getCamp();
        if (camp != battle.getDefCamp()) {
            worldService.addBattleArmy(battle, player.roleId, army.getHero(), army.getKeyId(), true);
        }
        LogUtil.debug("最终玩家参加战斗信息, roleId:", player.roleId, ", battle:", battle);
    }

    /**
     * 攻打点兵统领
     *
     * @param player
     * @param army
     * @param now
     */
    private void fightCabinetLead(Player player, Army army, int now) {
        int pos = army.getTarget();
        CabinetLead lead = worldDataManager.getCabinetLeadByPos(pos);
        if (null == lead) {
            LogUtil.error("点兵统领未找到坐标, pos:", pos);
            // 发送邮件通知
            Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
            mailDataManager.sendReportMail(player, null, MailConstant.MOLD_ATK_TARGET_NOT_FOUND, null, now, xy.getA(),
                    xy.getB(), xy.getA(), xy.getB());
            // 部队返回
            worldService.retreatArmy(player, army, now);
            return;
        }
        StaticCabinetPlan staticCabinetPlan = StaticBuildingDataMgr.getCabinetPlanById(lead.getCabinetPlanId());

        if (null == staticCabinetPlan
                || null == StaticBanditDataMgr.getBanditMap().get(staticCabinetPlan.getBanditId())) {

            LogUtil.error("点兵统领未配置, StaticCabinetPlanId:", lead.getCabinetPlanId());
            // 发送邮件通知
            Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
            mailDataManager.sendReportMail(player, null, MailConstant.MOLD_ATK_TARGET_NOT_FOUND, null, now, xy.getA(),
                    xy.getB());
            // 部队返回
            worldService.retreatArmy(player, army, now);
            return;
        }

        int banditId = staticCabinetPlan.getBanditId();
        StaticBandit staticBandit = StaticBanditDataMgr.getBanditMap().get(banditId);

        for (Integer npcId : staticBandit.getForm()) {
            StaticNpc npc = StaticNpcDataMgr.getNpcMap().get(npcId);
            if (null == npc) {
                LogUtil.error("NPCid未配置, npcId:", npcId);
                // 部队返回
                worldService.retreatArmy(player, army, now);
                return;
            }
        }

        // 战斗计算
        Fighter attacker = fightService.createFighter(player, army.getHero());
        Fighter defender = fightService.createBanditFighter(banditId);
        FightLogic fightLogic = new FightLogic(attacker, defender, true);
        fightLogic.start();

        //貂蝉任务-杀敌阵亡数量
        ActivityDiaoChanService.killedAndDeathTask0(attacker, false, true);
        ActivityDiaoChanService.killedAndDeathTask0(defender, false, true);

        // 兵力恢复
        Map<Long, List<CommonPb.Award>> recoverArmyAwardMap = new HashMap<>();
        // 记录玩家有改变的资源类型, key:roleId
        Map<Long, ChangeInfo> changeMap = new HashMap<>();
        // 损兵处理
        worldService.subHeroArm(player, attacker.forces, AwardFrom.ATTACK_BANDIT, changeMap);
        if (attacker.lost > 0) {
            // 执行勋章白衣天使特技逻辑
            medalDataManager.angelInWhite(attacker, recoverArmyAwardMap);
            //执行赛季天赋技能---伤病恢复
            seasonTalentService.execSeasonTalentEffect303(attacker, recoverArmyAwardMap);
            // 损兵排行
            activityDataManager.updRankActivity(player, ActivityConst.ACT_ARMY_RANK, attacker.lost);
            // 荣耀日报损兵进度
            honorDailyDataManager.addAndCheckHonorReport2s(player, HonorDailyConstant.COND_ID_14, attacker.lost);
            // 战令的损兵进度
            battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_SUB_HERO_ARMY, attacker.lost);
        }

        // 战斗记录
        BattlePb.BattleRoundPb record = fightLogic.generateRecord();
        Lord lord = player.lord;
        // 是否战胜目标
        boolean isSuccess = fightLogic.getWinState() == FightConstant.FIGHT_RESULT_SUCCESS;

        CommonPb.RptAtkBandit.Builder rpt = CommonPb.RptAtkBandit.newBuilder();
        rpt.setResult(isSuccess);
        rpt.setAttack(PbHelper.createRptMan(lord.getPos(), lord.getNick(), lord.getVip(), lord.getLevel()));
        rpt.setDefend(PbHelper.createRptBandit(banditId, pos));
        rpt.setAtkSum(PbHelper.createRptSummary(attacker.total, attacker.lost, lord.getCamp(), lord.getNick(),
                lord.getPortrait(), player.getDressUp().getCurPortraitFrame()));
        rpt.setDefSum(PbHelper.createRptSummary(defender.total, defender.lost, 0, null, -1, -1));

        // 给将领加经验
        rpt.addAllAtkHero(fightSettleLogic.banditFightHeroExpReward(player, attacker.forces));
        DataResource.ac.getBean(WorldService.class).buildRptHeroData(defender, rpt, false);
        rpt.setRecord(record);

        // 邮件标题参数
        List<String> tParam = new ArrayList<>();
        tParam.add(lord.getNick());
        tParam.add(String.valueOf(staticBandit.getBanditId()));
        // 内容参数
        List<String> cParam = new ArrayList<>();
        cParam.add(lord.getNick());
        Turple<Integer, Integer> xy = MapHelper.reducePos(lord.getPos());
        cParam.add(String.valueOf(xy.getA()));
        cParam.add(String.valueOf(xy.getB()));
        cParam.add(String.valueOf(staticBandit.getBanditId()));
        xy = MapHelper.reducePos(pos);
        cParam.add(String.valueOf(xy.getA()));
        cParam.add(String.valueOf(xy.getB()));

        // 发送战斗奖励，发送战报
        if (isSuccess) {
            // 从地图上移除点兵统领
            worldDataManager.removeBandit(pos, 1);

            List<CommonPb.Award> dropList = new ArrayList<>();
            List<List<Integer>> rewardList = new ArrayList<>();
            int taskCount = 0;

            List<List<Integer>> awardOthers = staticBandit.getAwardOthers();
            if (!CheckNull.isEmpty(awardOthers)) {
                for (int i = 0; i < awardOthers.size() - 1; i++) {
                    rewardList.add(staticBandit.getAwardOthers().get(i));
                }
                // 最后一个为掉落的点兵统领任务道具
                List<Integer> taskProp = awardOthers.get(awardOthers.size() - 1);
                if (taskProp.size() >= 3) {
                    taskCount = taskProp.get(2);
                    dropList.add(PbHelper.createAwardPb(taskProp.get(0), taskProp.get(1), taskProp.get(2)));
                }
            }

            List<CommonPb.Award> tmp = rewardDataManager.sendReward(player, rewardList, AwardFrom.CABINET_LEAD_DROP);// "点兵统领掉落"
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
            // 掉落的飞机碎片
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

            // 给目标军团加点兵统领经验
            campService.addCabinetLeadExp(lead.getCamp(), taskCount);

            // 给目标玩家加上点兵数
            Player targetPlayer = playerDataManager.getPlayer(lead.getRoleId());
            if (targetPlayer != null && targetPlayer.cabinet != null) {
                Cabinet cabinet = targetPlayer.cabinet;
                cabinet.setLeadStep(cabinet.getLeadStep() + 1);
            }

        } else {
            mailDataManager.sendReportMail(player, worldService.createAtkBanditReport(rpt.build(), now),
                    MailConstant.MOLD_ATK_LEAD_FAIL, null, now, tParam, cParam);
        }
        // 部队返回
        worldService.retreatArmy(player, army, now);
        // 通知客户端玩家资源变化
        worldService.sendRoleResChange(changeMap);
    }

    /**
     * 把将领加入到CityInfo的RoleQueue队列中
     *
     * @param player
     * @param army
     * @param now
     */
    public void fightBerlinWar(Player player, Army army, int now) {
        Integer atkType = army.getBattleId();
        LogUtil.debug("玩家参加战斗id, roleId:", player.roleId, ", atkType:", atkType);
        int cityId = army.getTargetId();
        BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
        if (CheckNull.isNull(berlinWar)) {
            // 柏林对象未找到，部队返回
            worldService.retreatArmy(player, army, now);
            worldService.synRetreatArmy(player, army, now);
            return;
        }

        BerlinCityInfo cityInfo = berlinWar.getCityInfoByCityId(cityId);
        if (CheckNull.isNull(cityInfo)) {
            // 据点对象未找到，部队返回
            worldService.retreatArmy(player, army, now);
            worldService.synRetreatArmy(player, army, now);
            return;
        }

        // 设置部队状态
        army.setState(ArmyConstant.ARMY_BERLIN_WAR);

        // 创建BerlinForce对象
        CommonPb.PartnerHeroIdPb twoInt = army.getHero().get(0);
        HeroUtil.setHeroState(twoInt, player, ArmyConstant.ARMY_BERLIN_WAR);

        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(twoInt.getPrincipleHeroId());
        if (null == staticHero) {
            LogUtil.error("创建Fighter，heroId未配置, heroId:", twoInt.getPrincipleHeroId());
            // 据点对象未找到，部队返回
            worldService.retreatArmy(player, army, now);
            worldService.synRetreatArmy(player, army, now);
            return;
        }
        int atkOrDef = cityInfo.getCamp() == player.lord.getCamp() ? WorldConstant.BERLIN_DEF
                : WorldConstant.BERLIN_ATK;
        BerlinForce berlinForce = fightService.createBerlinForce(player, staticHero, twoInt,
                atkOrDef, atkType, now, player.lord.getCamp());
        cityInfo.getRoleQueue().add(berlinForce);
        LogUtil.debug("最终玩家参加战斗信息, roleId:", player.roleId, ", berlinForce:", berlinForce);
    }

    /**
     * 决战行军到达
     *
     * @param player
     * @param army
     * @param now
     */
    private void fightDecisiveWar(Player player, Army army, int now) {

        // 把玩家部队加入城战部队中
        Integer battleId = army.getBattleId();
        LogUtil.debug("玩家参加战斗id, roleId:", player.roleId, ", battleId:", battleId);
        if (null == battleId) {
            battleId = 0;
        }
        Battle battle = warDataManager.getBattleMap().get(battleId);
        LogUtil.debug("玩家参加战斗信息, roleId:", player.roleId, ", battle:", battle);

        if (null == battle) {
            // 战斗对象未找到，部队返回
            worldService.retreatArmy(player, army, now);
            worldService.synRetreatArmy(player, army, now);
            return;
        }

        // 设置部队状态
        army.setState(ArmyConstant.ARMY_STATE_BATTLE);

        army.setHeroState(player, ArmyConstant.ARMY_STATE_BATTLE);
        LogUtil.debug("最终玩家参加战斗信息, roleId:", player.roleId, ", battle:", battle);

    }

    /**
     * 出击飞艇行军到达
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
        if (CheckNull.isNull(airShip) || airShip.isRefreshStatus() || airShipKey != airShip.getKeyId()) { // 飞艇被击飞, 或者逃跑,
            // 发送目标丢失邮件
            mailDataManager.sendNormalMail(player, MailConstant.MOLD_ATK_TARGET_NOT_FOUND, now, xy.getA(), xy.getB(),
                    xy.getA(), xy.getB());
            worldService.retreatArmy(player, army, now);
            worldService.synRetreatArmy(player, army, now);
            return;
        }
        StaticAirship sAirship = StaticWorldDataMgr.getAirshipMap().get(airShip.getId());
        if (CheckNull.isNull(sAirship)) {
            // 发送目标丢失邮件
            mailDataManager.sendNormalMail(player, MailConstant.MOLD_ATK_TARGET_NOT_FOUND, now, xy.getA(), xy.getB(),
                    xy.getA(), xy.getB());
            worldService.retreatArmy(player, army, now);
            worldService.synRetreatArmy(player, army, now);
            return;
        }

        // 修改hero状态
        army.setHeroState(player, ArmyConstant.ARMY_STATE_ATTACK_AIRSHIP_WAIT);

        int camp = player.lord.getCamp();
        List<CommonPb.BattleRole> battleRoles = airShip.getJoinRoles().get(camp);
        if (CheckNull.isNull(battleRoles)) {
            battleRoles = new ArrayList<>();
            airShip.getJoinRoles().put(camp, battleRoles);
        }

        // 设置部队状态
        army.setState(ArmyConstant.ARMY_STATE_ATTACK_AIRSHIP_WAIT);

        // 加入玩家的信息
        battleRoles.add(CommonPb.BattleRole.newBuilder().setKeyId(army.getKeyId()).setRoleId(player.roleId)
                .addAllPartnerHeroId(army.getHero()).build());

        // 这里参与人数不走通用配置
        long joinRoleCnt = battleRoles.stream().mapToLong(CommonPb.BattleRole::getRoleId).distinct().count();
        if (joinRoleCnt >= sAirship.getParticipate()) {
            // 达成条件, 开启战斗
            fightAirShip(airShip, battleRoles, player.lord.getCamp());
        } else if (joinRoleCnt == 1) {
            // 推送满足条件的集结
            campService.syncRallyBattle(player, null, airShip);
        }
    }

    /**
     * 飞艇战斗
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

        // 归属者奖励, 后期判断次数上限
        // Player belongRole = playerDataManager.getPlayer(airShip.getBelongRoleId());
        // if (CheckNull.isNull(belongRole)) {
        // LogUtil.error("飞艇没归属者, airShip:", airShipId, ", pos:", airShipPos);
        // return;
        // }

        // 战斗计算
        Fighter attacker = fightService.createFighterByBattleRole(battleRoles);
        Fighter defender = fightService.createFighter(airShip.getNpc());
        FightLogic fightLogic = new FightLogic(attacker, defender, true);
        fightLogic.start();

        //貂蝉任务-杀敌阵亡数量
        ActivityDiaoChanService.killedAndDeathTask0(attacker, false, true);
        ActivityDiaoChanService.killedAndDeathTask0(defender, false, true);

        boolean atkSuccess = fightLogic.getWinState() == FightConstant.FIGHT_RESULT_SUCCESS;

        // 需要返回的玩家
        Set<Long> retreatPlayers;

        // 兵力恢复
        Map<Long, List<CommonPb.Award>> recoverArmyAwardMap = new HashMap<>();
        // 记录玩家有改变的资源类型, key:roleId
        Map<Long, ChangeInfo> changeMap = new HashMap<>();
        Map<Long, List<CommonPb.Award>> dropMap = new HashMap<>();

        // 攻击方损兵处理
        if (attacker.lost > 0) {
            warService.subBattleHeroArm(attacker.forces, changeMap, AwardFrom.AIR_SHIP_BATTLE);
        }

        // 执行勋章白衣天使特技逻辑
        medalDataManager.angelInWhite(attacker, recoverArmyAwardMap);
        //执行赛季天赋技能---伤病恢复
        seasonTalentService.execSeasonTalentEffect303(attacker, recoverArmyAwardMap);
        // 执行勋章-以战养战特技逻辑
        medalDataManager.sustainTheWarByMeansOfWar(attacker, defender, recoverArmyAwardMap, atkSuccess);

        // Report战报
        Player firstAttackPlayer = playerDataManager.getPlayer(battleRoles.get(0).getRoleId());
        CommonPb.RptAtkPlayer.Builder rpt = createAirShipRptBuilder(camp, attacker, defender, fightLogic, atkSuccess,
                firstAttackPlayer, airShipId, airShipPos);
        CommonPb.Report.Builder report = worldService.createAtkPlayerReport(rpt.build(), now);

        StaticAirship sAirShip = StaticWorldDataMgr.getAirshipMap().get(airShipId);
        if (CheckNull.isNull(sAirShip)) {
            LogUtil.error("飞艇战斗结算, 未找到飞艇配置, airShipId", airShipId);
            return;
        }

        // 进攻飞艇的玩家对象
        List<Player> battlePlayer = battleRoles.stream()
                // 归属者不能获取协助者奖励
                .filter(role -> role.getRoleId() != airShip.getBelongRoleId())
                .mapToLong(CommonPb.BattleRole::getRoleId)
                // 去除重复
                .distinct()
                .mapToObj(roleId -> playerDataManager.getPlayer(roleId)).filter(p -> !CheckNull.isNull(p))
                .collect(Collectors.toList());

        // 如果没有玩家就跳出
        if (CheckNull.isEmpty(battlePlayer)) {
            return;
        }

        if (atkSuccess) { // 飞艇被击杀

            // 归属者奖励
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
            // 飞艇被摧毁跑马灯
            chatDataManager.sendSysChat(ChatConst.CHAT_AIR_SHIP_DEAD, airShip.getAreaId(), 0, airShip.getId(), camp, battlePlayer.get(0).lord.getNick());
            // 根据区域杀敌数, 判断是否发送区域buff
            airshipService.incrementAndGetKill(areaId, camp);
            // Optional.ofNullable(StaticWorldDataMgr.getAreaMap().get(areaId))
            //         .ifPresent(sArea -> Optional.ofNullable(StaticWorldDataMgr.getAirshipAreaMap().get(sArea.getOpenOrder()))
            //                 .ifPresent(sAirArea -> {
            //                     // 如果杀敌数量满足
            //                     if (sAirArea.getNum() == killNum) {
            //                         airshipService.updateBuff(sAirArea, areaId);
            //                     }
            //                 }));

            // 飞艇击飞所有玩家都返回
            retreatPlayers = airShip.getJoinRoles()
                    .values()
                    .stream()
                    .flatMap(Collection::stream)
                    .map(CommonPb.BattleRole::getRoleId)
                    .collect(Collectors.toSet());

            // 地图上移除飞艇
            airshipService.removeAirshipFromMap(airShip, now, AirshipWorldData.STATUS_DEAD_REFRESH);
        } else {
            // 防守方损兵处理
            if (defender.lost > 0) {
                airShip.getNpc().clear();
                for (Force force : defender.forces) {
                    if (force.alive()) {
                        airShip.getNpc().add(new NpcForce(force.id, force.hp, force.curLine));
                    }
                }
            }

            // 推送满足条件的集结(取消)
            campService.syncCancelRallyBattle(null, null, airShip);
            // 移除当前阵营
            retreatPlayers = airShip.getJoinRoles().remove(camp).stream().map(CommonPb.BattleRole::getRoleId)
                    .collect(Collectors.toSet());
        }

        // 参与者, 不包括归属者
        battlePlayer.forEach(p -> {
            List<CommonPb.Award> drops = dropMap.computeIfAbsent(p.roleId, (k) -> new ArrayList<>());
            // List<CommonPb.Award> drops = dropMap.get(p.roleId);
            // if (CheckNull.isNull(drops)) {
            //     drops = new ArrayList<>();
            //     dropMap.put(p.roleId, drops);
            // }
            if (atkSuccess) {
                // 当天首次击杀
                if (Constant.AIRSHIP_CAN_AWARD_CNT.get(0) == p.getAndCreateAirshipPersonData().getKillAwardCnt()) {
                    List<CommonPb.Award> awards = rewardDataManager.sendReward(p, sAirShip.getAwardFirst(), AwardFrom.AIR_SHIP_BATTLE_AWARD);
                    if (!CheckNull.isEmpty(awards)) {
                        drops.addAll(awards);
                    }
                    taskDataManager.updTask(p, TaskType.COND_522, 1);
                }
                // 记录获取击杀奖励次数
                p.getAndCreateAirshipPersonData().subKillAwardCnt(1);
                activityDataManager.updDay7ActSchedule(p, ActivityConst.ACT_TASK_MULTI_BANDIT_CNT, sAirShip.getLv());
                //圣诞活动掉落
                List<CommonPb.Award> actHitDrop = activityDataManager.getActHitDrop(p, sAirShip.getLv(), StaticActBandit.ACT_HIT_DROP_TYPE_3);
                drops.addAll(actHitDrop);

                //貂蝉任务-攻打精英叛军
                ActivityDiaoChanService.completeTask(p, ETask.FIGHT_ELITE_REBEL, sAirShip.getId(), sAirShip.getLv());
                //喜悦金秋-日出而作-攻打精英叛军
                TaskService.processTask(p, ETask.FIGHT_ELITE_REBEL, sAirShip.getId(), sAirShip.getLv());
            }
            if (p.getAndCreateAirshipPersonData().getAttendAwardCnt() > 0) {
                // 记录获取参与奖励次数
                p.getAndCreateAirshipPersonData().subAttendAwardCnt(1);

                // 固定掉落
                List<CommonPb.Award> awards = rewardDataManager.sendReward(p, sAirShip.getAwardRegular(), AwardFrom.AIR_SHIP_BATTLE_AWARD);
                if (!CheckNull.isEmpty(awards)) {
                    drops.addAll(awards);
                }
                // 概率掉落
                List<Integer> randomAward = RandomUtil.getRandomByWeight(sAirShip.getAwardExtra(), 3, false);
                if (!CheckNull.isEmpty(randomAward)) {
                    awards = rewardDataManager.sendReward(p, Collections.singletonList(randomAward), AwardFrom.AIR_SHIP_BATTLE_AWARD);
                    if (!CheckNull.isEmpty(awards)) {
                        drops.addAll(awards);
                    }
                }
                //称号-攻打精英叛军(仅包含奖励的每天5次)
                titleService.processTask(p, ETask.FIGHT_ELITE_REBEL);
            } else {
                mailDataManager.sendNormalMail(p, MailConstant.MOLD_AIR_SHIP_HELP_AWARD_MAX, now);
            }
        });

        List<Integer> posList = new ArrayList<>();

        // 需要返回的Player
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
            // 如果是参战人员, 发送战报
            if (battleRoles.stream().anyMatch(battleRole -> battleRole.getRoleId() == p.roleId)) {
                Turple<Integer, Integer> atkPos = MapHelper.reducePos(p.lord.getPos());
                mailDataManager.sendReportMail(p, report,
                        atkSuccess ? MailConstant.MOLD_AIR_SHIP_BATTLE_SUC : MailConstant.MOLD_AIR_SHIP_BATTLE_FAIL,
                        dropMap.get(p.roleId), now, recoverArmyAwardMap, p.lord.getNick(), airShipId, p.lord.getNick(),
                        atkPos.getA(), atkPos.getB(), airShipId, defPos.getA(), defPos.getB());
            } else {
                if (atkSuccess) { // 发送飞艇被击毁
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
     * 记录飞艇战斗
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
            win = String.valueOf(FightConstant.FIGHT_RESULT_SUCCESS);
        } else {
            win = String.valueOf(FightConstant.FIGHT_RESULT_FAIL);
        }
        battleRoles.stream().map(rb -> rb.getRoleId()).distinct().map(rId -> playerDataManager.getPlayer(rId))
                .filter(p -> p != null)
                .forEach(player -> {
                    LogLordHelper.otherLog("airShipBattle", player.account.getServerId(), player.roleId,
                            "atk", areaId, airShipKeyIdAndId, atkSuccess, airShipPos, player.lord.getCamp());
                    //上报数数
                    EventDataUp.battle(player.account, player.lord, attacker, "atk", airShipKeyIdAndId,
                            String.valueOf(WorldConstant.BATTLE_TYPE_AIRSHIP), win, firstAttackPlayer.roleId, atkHero, param);
                });
    }

    public CommonPb.RptAtkPlayer.Builder createAirShipRptBuilder(int camp, Fighter attacker, Fighter defender,
                                                                 FightLogic fightLogic, boolean atkSuccess, Player belongRole, int airShipId, int airShipPos) {
        // 战斗记录
        CommonPb.RptAtkPlayer.Builder rpt = CommonPb.RptAtkPlayer.newBuilder();
        rpt.setResult(atkSuccess);
        // 记录双方汇总信息
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
        BattlePb.BattleRoundPb record = fightLogic.generateRecord();
        rpt.setRecord(record);
        return rpt;
    }

    /**
     * 争夺飞艇归属权
     *
     * @param player
     * @param army
     * @param airShip
     */
    private void fightAirBelongLogic(Player player, Army army, AirshipWorldData airShip) {
        if (CheckNull.isNull(player) || CheckNull.isNull(army) || CheckNull.isNull(airShip)) return;

        // 设置归属权
        airShip.setBelongRoleId(player.roleId);

        // 获取归属权发送跑马灯
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
                    // mailDataManager.sendNormalMail(p, Objects.equals(player.roleId, p.roleId) ? // 归属权获取者
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
