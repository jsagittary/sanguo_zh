package com.gryphpoem.game.zw.gameplay.local.world.battle;

import com.gryphpoem.cross.fight.report.CrossFightReport;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonDailyAttackTaskService;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonDailyRestrictTaskService;
import com.gryphpoem.game.zw.gameplay.local.util.MapCurdEvent;
import com.gryphpoem.game.zw.gameplay.local.util.MapEvent;
import com.gryphpoem.game.zw.gameplay.local.util.dto.AttackParamDto;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.WorldEntityType;
import com.gryphpoem.game.zw.gameplay.local.world.army.BaseArmy;
import com.gryphpoem.game.zw.gameplay.local.world.army.MapMarch;
import com.gryphpoem.game.zw.gameplay.local.world.map.BaseWorldEntity;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Effect;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.domain.s.StaticActBandit;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.fight.FightLogic;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.pojo.world.Battle;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.resource.util.Turple;
import com.gryphpoem.game.zw.rpc.comsumer.RpcFighterConsumer;
import com.gryphpoem.game.zw.service.*;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName AttackPlayerBattle.java
 * @Description ????????????battle
 * @author QiuKun
 * @date 2019???3???23???
 */
public class AttackPlayerBattle extends AbsCommonBattle {

    public AttackPlayerBattle(Battle battle) {
        super(battle);
    }

    @Override
    public void doFight(int now, MapWarData mapWarData) {
        int pos = getBattle().getPos();
        // ????????????
        BaseWorldEntity baseWorldEntity = mapWarData.getCrossWorldMap().getAllMap().get(pos);
        if (baseWorldEntity == null || baseWorldEntity.getType() != WorldEntityType.PLAYER) {
            // ????????????
            cancelBattleAndReturnArmy(mapWarData, CancelBattleType.UNKNOW);
            return;
        }
        // ???????????????
        Player defPlayer = battle.getDefencer();
        if (defPlayer == null) return;
        Effect effect = battle.getDefencer().getEffect().get(EffectConstant.PROTECT);
        if (effect != null && effect.getEndTime() > now) {
            // ????????????
            cancelBattleAndReturnArmy(mapWarData, CancelBattleType.UNKNOW);
            return;
        }

        FightService fightService = DataResource.ac.getBean(FightService.class);
        WarDataManager warDataManager = DataResource.ac.getBean(WarDataManager.class);
        WorldWarSeasonDailyAttackTaskService dailyAttackTaskService = DataResource.ac
                .getBean(WorldWarSeasonDailyAttackTaskService.class);
        SeasonTalentService seasonTalentService = DataResource.ac.getBean(SeasonTalentService.class);
        addDefendRoleHeros(mapWarData);
        Fighter attacker = fightService.createMultiPlayerFighter(battle, null);
        Fighter defender = fightService.createCampBattleDefencer(battle, null);
        FightLogic fightLogic = new FightLogic(attacker, defender, true, battle.getType());
        warDataManager.packForm(fightLogic.getRecordBuild(), attacker.forces, defender.forces);
        fightLogic.fight();// ????????????????????????

        //????????????-??????????????????
        ActivityDiaoChanService.killedAndDeathTask0(attacker,true,true);
        ActivityDiaoChanService.killedAndDeathTask0(defender,true,true);

        // ????????????
        boolean atkSuccess = fightLogic.getWinState() == ArmyConstant.FIGHT_RESULT_SUCCESS;
        // ????????????????????????????????????, key:roleId
        Map<Long, ChangeInfo> changeMap = new HashMap<>();
        // ????????????
        WarService warService = DataResource.ac.getBean(WarService.class);
        if (attacker.lost > 0) {
            warService.subBattleHeroArm(attacker.forces, changeMap, AwardFrom.CITY_BATTLE_ATTACK);
            // ??????????????????????????????
            dailyAttackTaskService.addPlayerDailyAttackOther(attacker.forces);
        }
        if (defender.lost > 0) {
            warService.subBattleHeroArm(defender.forces, changeMap, AwardFrom.CITY_BATTLE_DEFEND);
            // ??????????????????????????????
            dailyAttackTaskService.addPlayerDailyAttackOther(defender.forces);
        }

        // ????????????
        Map<Long, List<Award>> recoverArmyAwardMap = new HashMap<>();
        MedalDataManager medalDataManager = DataResource.ac.getBean(MedalDataManager.class);
        // ????????????????????????????????????
        medalDataManager.angelInWhite(attacker, recoverArmyAwardMap);
        medalDataManager.angelInWhite(defender, recoverArmyAwardMap);
        //????????????????????????---????????????
        seasonTalentService.execSeasonTalentEffect303(attacker, recoverArmyAwardMap);
        seasonTalentService.execSeasonTalentEffect303(defender, recoverArmyAwardMap);

        // ????????????-????????????????????????
        medalDataManager.sustainTheWarByMeansOfWar(attacker, defender, recoverArmyAwardMap, atkSuccess);
        // buff??????,?????????????????????
        mapWarData.getCrossWorldMap().rebelBuffRecoverArmy(attacker, defender, recoverArmyAwardMap);

        // ????????????
        SolarTermsDataManager solarTermsDataManager = DataResource.ac.getBean(SolarTermsDataManager.class);
        CommonPb.RptAtkPlayer.Builder rpt = CommonPb.RptAtkPlayer.newBuilder();
        CommonPb.Record record = fightLogic.generateRecord();
        rpt.setNightEffect(solarTermsDataManager.getNightEffect() != null); // ??????
        rpt.setResult(atkSuccess);
        rpt.setRecord(record);

        Player atkPlayer = battle.getSponsor();
        Lord atkLord = atkPlayer.lord;
        rpt.setAttack(PbHelper.createRptMan(atkLord.getPos(), atkLord.getNick(), atkLord.getVip(), atkLord.getLevel()));
        rpt.setAtkSum(PbHelper.createRptSummary(attacker.total, attacker.lost, atkLord.getCamp(), atkLord.getNick(),
                atkLord.getPortrait(), atkPlayer.getDressUp().getCurPortraitFrame()));

        Lord defLord = defPlayer.lord;
        rpt.setDefMan(PbHelper.createRptMan(defLord.getPos(), defLord.getNick(), defLord.getVip(), defLord.getLevel()));
        rpt.setDefSum(PbHelper.createRptSummary(defender.total, defender.lost, defLord.getCamp(), defLord.getNick(),
                defLord.getPortrait(), defPlayer.getDressUp().getCurPortraitFrame()));

        List<CommonPb.Award> loseList = new ArrayList<>();
        List<CommonPb.Award> dropList = new ArrayList<>(); // ??????

        BuildingDataManager buildingDataManager = DataResource.ac.getBean(BuildingDataManager.class);
        RewardDataManager rewardDataManager = DataResource.ac.getBean(RewardDataManager.class);
        TaskDataManager taskDataManager = DataResource.ac.getBean(TaskDataManager.class);
        BattlePassDataManager battlePassDataManager = DataResource.ac.getBean(BattlePassDataManager.class);
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        ActivityDataManager activityDataManager = DataResource.ac.getBean(ActivityDataManager.class);
        HonorDailyService honorDailyService = DataResource.ac.getBean(HonorDailyService.class);
        CampDataManager campDataManager = DataResource.ac.getBean(CampDataManager.class);
        CampService campService = DataResource.ac.getBean(CampService.class);
        MailDataManager mailDataManager = DataResource.ac.getBean(MailDataManager.class);
        WorldWarSeasonDailyRestrictTaskService restrictTaskService = DataResource.ac
                .getBean(WorldWarSeasonDailyRestrictTaskService.class);
        MapMarch mapMarchArmy = mapWarData.getCrossWorldMap().getMapMarchArmy();
        if (atkSuccess) {
            try {
                // ??????????????????
                dropList.addAll(buildingDataManager.dropList4War(atkPlayer, battle.getDefencer(), loseList, false));
                // ??????????????????????????????
                Award award = warService.checkPlaneChipAward(atkPlayer);
                if (!CheckNull.isNull(award)) {
                    dropList.add(award);
                }
                // ????????????????????????
                List<Award> actHitDrop = activityDataManager.getActHitDrop(atkPlayer, 0,
                        StaticActBandit.ACT_HIT_DROP_TYPE_1);
                if (!CheckNull.isEmpty(actHitDrop)) {
                    dropList.addAll(actHitDrop);
                }
            } catch (MwException e) {
                LogUtil.error(e);
            }
            // ????????????-???????????? ????????????
            medalDataManager.peacekeepingForces(defender, defPlayer);
            // ?????????????????????????????????10?????????
            dropList.add(Award.newBuilder().setType(AwardType.MONEY).setId(AwardType.Money.EXP).setCount(10).build());
            // ????????????????????????????????????
            rewardDataManager.sendRewardByAwardList(atkPlayer, dropList, AwardFrom.CITY_BATTLE_ATTACK);
            // ???????????????????????? ???????????????
            warService.recordPartyBattle(battle, battle.getType(), battle.getAtkCamp(), true);

            // ????????????,????????????
            playerHitFly(mapWarData.getCrossWorldMap(), defPlayer, battle.getType(), atkPlayer);
            mailDataManager.sendNormalMail(battle.getDefencer(), MailConstant.MOLD_HIT_FLY,
                    TimeHelper.getCurrentSecond(), atkPlayer.lord.getNick(), atkPlayer.lord.getNick());
            // ?????????????????????
            mapMarchArmy.retreatGuardArmy(defLord.getPos(), true);

            // ???????????????????????????
            List<Integer> cancelBattleIds = mapWarData.getBattlePosCache().get(pos).stream()
                    .filter(battlId -> battlId.intValue() != getBattleId()).collect(Collectors.toList());
            if (!CheckNull.isEmpty(cancelBattleIds)) {
                cancelBattleIds.stream().map(battleId -> mapWarData.getAllBattles().get(battleId))
                        .filter(b -> b != null).forEach(b -> {
                            b.cancelBattleAndReturnArmy(mapWarData, CancelBattleType.DEF_HITFLY);
                        });
            }
            taskDataManager.updTask(atkPlayer, TaskType.COND_BATTLE_CITY_LV_CNT, 1);
            activityDataManager.updDay7ActSchedule(atkPlayer, ActivityConst.ACT_TASK_ATK,
                    battle.getDefencer().building.getCommand());

            //????????????-??????????????????
            ActivityDiaoChanService.completeTask(atkPlayer, ETask.HITFLY_PLAYER);
            TaskService.processTask(atkPlayer, ETask.HITFLY_PLAYER);

            // ????????????
            for (long roles : battle.getAtkRoles()) {
                Player actPlayer = playerDataManager.getPlayer(roles);
                if (actPlayer != null) {
                    activityDataManager.updDay7ActSchedule(atkPlayer, ActivityConst.ACT_TASK_JOIN_OR_ATK,
                            battle.getDefencer().building.getCommand());
                    activityDataManager.updAtkCityActSchedule(atkPlayer, ActivityConst.ACT_TASK_JOIN_OR_ATK,
                            battle.getDefencer().building.getCommand());
                }
            }
            // ?????????++
            atkPlayer.common.incrKillNum();
            // ???????????????
            activityDataManager.updRankActivity(atkPlayer, ActivityConst.ACT_CITY_BATTLE_RANK, 1);
            for (Long atkRole : getBattle().getAtkRoles()) {
                // ?????????????????????
                restrictTaskService.updatePlayerDailyRestrictTask(playerDataManager.getPlayer(atkRole),
                        TaskType.COND_BATTLE_CITY_LV_CNT, 1);
            }
        } else {
            // ?????????????????????
            mapMarchArmy.retreatGuardArmy(defLord.getPos(), false);
        }

        // ??????????????????=????????????+????????????/2??????????????????????????????, ???????????????
        HashMap<Long, Map<Integer, Integer>> exploitAwardMap = new HashMap<>();
        warService.addBattleHeroExp(attacker.forces, AwardFrom.CITY_BATTLE_ATTACK, rpt, true, true,
                battle.isCityBattle(), changeMap, true, exploitAwardMap);
        warService.addBattleHeroExp(defender.forces, AwardFrom.CITY_BATTLE_DEFEND, rpt, false, true,
                battle.isCityBattle(), changeMap, true, exploitAwardMap);

        CommonPb.Report.Builder report = CommonPb.Report.newBuilder();
        report.setTime(now);
        report.setRptPlayer(rpt);

        taskDataManager.updTask(atkPlayer, TaskType.COND_ATTCK_PLAYER_CNT, 1, defLord.getLevel());
        // ???????????????????????????
        honorDailyService.addAndCheckHonorReports(atkPlayer, battle.getDefencer(), atkSuccess, battle.getType());
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
        // ????????????????????????
        Optional.of(campDataManager.getParty(battle.getAtkCamp()))
                .ifPresent((party) -> campService.checkHonorRewardAndSendSysChat(party));
        CrossWorldMap cmap = mapWarData.getCrossWorldMap();
        int cityId = 0;
        Turple<Integer, Integer> atkPos = cmap.posToTurple(atkLord.getPos());
        Turple<Integer, Integer> defPos = cmap.posToTurple(battle.getPos());
        warService.sendCityBattleMail(battle, cityId, atkLord, defLord, atkPos, defPos, atkSuccess, report, dropList,
                loseList, now, recoverArmyAwardMap);
        // ???????????????????????????
        warService.autoFillArmy(battle.getDefencer());
        // ?????????????????????????????????
        warService.sendRoleResChange(changeMap);
        // ???????????????
//        int heroid = report.getRptPlayer().getAtkHero(0).getHeroId();
        warService.logBattle(battle, fightLogic.getWinState(),attacker,defender, rpt.getAtkHeroList(), rpt.getDefHeroList());
        // ?????????????????????
        mapWarData.getCrossWorldMap().publishMapEvent(MapEvent.mapEntity(battle.getPos(), MapCurdEvent.UPDATE));
        //?????????????????????????????????????????????
        cmap.getGlobalWarFire().attackPlayerBattleFinish(attacker, defender, battle, baseWorldEntity);
    }

    /**
     * ???????????????
     *  @param cMap
     * @param player
     * @param battleType
     * @param atkPlayer
     */
    private void playerHitFly(CrossWorldMap cMap, Player player, int battleType, Player atkPlayer) {
        if (player.lord.getArea() != cMap.getMapId()) {
            return;
        }
        int prePos = player.lord.getPos();
        int newPos = cMap.getRandomOpenEmptyPosSafeArea(player.lord.getCamp());
        BaseWorldEntity playerEntity = cMap.removeWorldEntity(prePos);
        playerEntity.setPos(newPos);
        cMap.addWorldEntity(playerEntity);
        player.lord.setPos(newPos);

        // ???????????????????????????????????????
        if (battleType != WorldConstant.BATTLE_TYPE_DECISIVE_BATTLE
                && BuildingDataManager.getBuildingLv(BuildingType.WALL, player) > 0) {
            player.setFireState(true);
        }

        ActivityDataManager activityDataManager = DataResource.ac.getBean(ActivityDataManager.class);
        WarService warService = DataResource.ac.getBean(WarService.class);
        BuildingDataManager buildingDataManager = DataResource.ac.getBean(BuildingDataManager.class);
        // ???????????????????????????
        activityDataManager.updateTriggerStatus(ActivityConst.TRIGGER_GIFT_REBUILD, player, 1);
        // ???????????????????????????
        warService.syncRoleMove(player, newPos);
        // ????????????
        buildingDataManager.SyncRebuild(player, atkPlayer);
        // ????????????
        cMap.publishMapEvent(MapEvent.mapEntity(prePos, MapCurdEvent.DELETE),
                MapEvent.mapEntity(newPos, MapCurdEvent.CREATE));
    }

    @Override
    protected void onCancelBattleAfter(MapWarData mapWarData, CancelBattleType cancelType) {
        MailDataManager mailDataManager = DataResource.ac.getBean(MailDataManager.class);

        String atkNick = battle.getSponsor() != null ? battle.getSponsor().lord.getNick() : "";
        int defCamp = battle.getDefCamp();
        String defNick = battle.getDefencer() != null ? battle.getDefencer().lord.getNick() : "";
        CrossWorldMap crossWorldMap = mapWarData.getCrossWorldMap();
        int[] xy = crossWorldMap.posToXy(getBattle().getPos());
        int x = xy != null ? xy[0] : 0;
        int y = xy != null ? xy[1] : 0;
        int now = TimeHelper.getCurrentSecond();
        if (cancelType == CancelBattleType.ATKCANCEL) {
            for (Player player : getPlayerByRoleId(getBattle().getAtkRoles())) {
                mailDataManager.sendNormalMail(player, MailConstant.MOLD_CITY_ATK_RETREAT_ATK, now, atkNick, defCamp,
                        defNick, x, y, atkNick, defCamp, defNick, x, y);
            }
            for (Player player : getPlayerByRoleId(getBattle().getDefRoles())) {
                mailDataManager.sendNormalMail(player, MailConstant.MOLD_CITY_ATK_RETREAT_DEF, now, atkNick, defCamp,
                        defNick, x, y, atkNick, defCamp, defNick, x, y);
            }
            if (battle.getDefencer() != null) {// ???????????????
                mailDataManager.sendNormalMail(battle.getDefencer(), MailConstant.MOLD_CITY_ATK_RETREAT_DEF, now,
                        atkNick, defCamp, defNick, x, y, atkNick, defCamp, defNick, x, y);
            }
        } else if (cancelType == CancelBattleType.DEFMOVECITY) {// ?????????????????????
            for (Player player : getPlayerByRoleId(getBattle().getAtkRoles())) {
                mailDataManager.sendNormalMail(player, MailConstant.MOLD_CITY_DEF_FLEE_ATK, now, defNick, defNick);
            }
            for (Player player : getPlayerByRoleId(getBattle().getDefRoles())) {
                mailDataManager.sendNormalMail(player, MailConstant.MOLD_CITY_DEF_FLEE_DEF, now, defNick, defNick);
            }
        } else if (cancelType == CancelBattleType.DEF_HITFLY) { // ???????????????
            for (Player player : getPlayerByRoleId(getBattle().getAtkRoles())) {
                mailDataManager.sendNormalMail(player, MailConstant.MOLD_ATTACK_TARGET_FLY, now, defNick, defNick);
            }
        }
    }

    private static List<Player> getPlayerByRoleId(Collection<Long> roleIds) {
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        return roleIds.stream().map(roleId -> playerDataManager.getPlayer(roleId)).filter(p -> p != null)
                .collect(Collectors.toList());
    }

    @Override
    public void joinBattle(AttackParamDto param) throws MwException {
        int now = TimeHelper.getCurrentSecond();
        Player player = param.getInvokePlayer();
        long roleId = player.roleId;
        int battleId = param.getBattleId();
        if (battle.getDefencer().roleId == roleId) {
            throw new MwException(GameError.HAS_JOIN_BATTLE.getCode(), "???????????????????????????????????????, roleId:", roleId, ", battleId:",
                    battleId);
        }
        // ?????????????????????????????????,?????????????????????
        int camp = player.lord.getCamp();
        if (battle.getBattleType() == WorldConstant.CITY_BATTLE_BLITZ && camp == battle.getAtkCamp()) {
            throw new MwException(GameError.QUICKLY_BATTLE_NOT_JOIN.getCode(), "???????????????????????????, roleId:", roleId,
                    ", battleId:", battleId);
        }
        // ????????????
        checkAndSubFood(param);
        BaseArmy baseArmy = createBaseArmy(param, now, ArmyConstant.ARMY_TYPE_ATK_PLAYER);
        addBattleRole(param,AwardFrom.ATTACK_PLAYER_BATTLE);
        // ????????????
        CrossWorldMap crossWorldMap = param.getCrossWorldMap();
        crossWorldMap.publishMapEvent(baseArmy.createMapEvent(MapCurdEvent.CREATE),
                MapEvent.mapEntity(player.lord.getPos(), MapCurdEvent.UPDATE));
        // ???????????????
        param.setArmy(PbHelper.createArmyPb(baseArmy.getArmy(), false));
        param.setBattle(PbHelper.createBattlePb(battle));
    }

}
