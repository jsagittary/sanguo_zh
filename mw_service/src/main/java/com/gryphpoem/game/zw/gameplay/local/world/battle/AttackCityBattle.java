package com.gryphpoem.game.zw.gameplay.local.world.battle;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.gameplay.local.constant.CrossWorldMapConstant;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonDailyAttackTaskService;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonDailyRestrictTaskService;
import com.gryphpoem.game.zw.gameplay.local.util.MapCurdEvent;
import com.gryphpoem.game.zw.gameplay.local.util.MapEvent;
import com.gryphpoem.game.zw.gameplay.local.util.dto.AttackParamDto;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.WorldEntityType;
import com.gryphpoem.game.zw.gameplay.local.world.army.BaseArmy;
import com.gryphpoem.game.zw.gameplay.local.world.map.BaseWorldEntity;
import com.gryphpoem.game.zw.gameplay.local.world.map.CityMapEntity;
import com.gryphpoem.game.zw.dataMgr.StaticNpcDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.domain.s.StaticCity;
import com.gryphpoem.game.zw.resource.domain.s.StaticNpc;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.fight.FightLogic;
import com.gryphpoem.game.zw.resource.pojo.fight.FightRecord;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;
import com.gryphpoem.game.zw.resource.pojo.world.Battle;
import com.gryphpoem.game.zw.resource.pojo.world.City;
import com.gryphpoem.game.zw.resource.pojo.world.CityHero;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.service.*;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author: ZhouJie
 * @date: Create in 2019-04-01 13:52
 * @description:
 * @modified By:
 */
public class AttackCityBattle extends AbsCommonBattle {

    public AttackCityBattle(Battle battle) {
        super(battle);
    }

    @Override
    public void doFight(int now, MapWarData mapWarData) {
        int pos = getBattle().getPos();
        CrossWorldMap cmap = mapWarData.getCrossWorldMap();
        BaseWorldEntity baseWorldEntity = cmap.getAllMap().get(pos);
        if (baseWorldEntity == null || baseWorldEntity.getType() != WorldEntityType.CITY) {
            // ????????????
            cancelBattleAndReturnArmy(mapWarData, CancelBattleType.UNKNOW);
            return;
        }
        CityMapEntity cityMapEntity = (CityMapEntity) baseWorldEntity;
        City city = cityMapEntity.getCity();
        if (city.getProtectTime() > now) {
            // ????????????
            cancelBattleAndReturnArmy(mapWarData, CancelBattleType.UNKNOW);
            return;
        }

        FightService fightService = DataResource.ac.getBean(FightService.class);
        WarDataManager warDataManager = DataResource.ac.getBean(WarDataManager.class);
        MedalDataManager medalDataManager = DataResource.ac.getBean(MedalDataManager.class);
        GlobalDataManager globalDataManager = DataResource.ac.getBean(GlobalDataManager.class);
        WorldWarSeasonDailyAttackTaskService dailyAttackTaskService = DataResource.ac
                .getBean(WorldWarSeasonDailyAttackTaskService.class);
        SeasonTalentService seasonTalentService = DataResource.ac.getBean(SeasonTalentService.class);

        Fighter attacker = fightService.createMultiPlayerFighter(battle, null);
        Fighter defender = fightService.createCrossWarCampBattleDef(battle, city.getFormList());
        FightLogic fightLogic = new FightLogic(attacker, defender, true, battle.getType());
        warDataManager.packForm(fightLogic.getRecordBuild(), attacker.forces, defender.forces);
        fightLogic.fight();// ????????????????????????

        //????????????-??????????????????
        ActivityDiaoChanService.killedAndDeathTask0(attacker,true,true);
        ActivityDiaoChanService.killedAndDeathTask0(defender,true,true);

        // ???????????? <roleId, <heroId, exploit>>
        HashMap<Long, Map<Integer, Integer>> exploitAwardMap = new HashMap<>();
        // ??????????????????-???????????? ??????
        if (battle.getType() == WorldConstant.BATTLE_TYPE_CAMP) {
            medalDataManager.militaryMeritIsProminent(attacker, defender, exploitAwardMap);
        }
        // ????????????
        boolean atkSuccess = fightLogic.getWinState() == ArmyConstant.FIGHT_RESULT_SUCCESS;
        // ?????????????????????????????? ???????????? ??????????????? ??????????????????????????????
        if(city.getCityId() == CrossWorldMapConstant.NEW_YORK_CITY_ID  && atkSuccess
                && fightLogic.getAttrChangeState() == ArmyConstant.ATTR_CHANGE_STATE_YES){
            // ????????????????????????????????????
            int cur = globalDataManager.getGameGlobal().getCrossCityNpcChgAttrCnt();
            globalDataManager.getGameGlobal().setCrossCityNpcChgAttrCnt(cur + 1);
            LogUtil.common("???????????????????????????????????? cur_up_count ",cur," add_up_count ",1);
        }
        // ????????????????????????????????????, key:roleId
        Map<Long, ChangeInfo> changeMap = new HashMap<>();
        // ????????????
        WarService warService = DataResource.ac.getBean(WarService.class);

        // ???????????????????????????
        if (attacker.lost > 0) {
            warService.subBattleHeroArm(attacker.forces, changeMap, AwardFrom.CAMP_BATTLE_ATTACK);
            // ??????????????????????????????
            dailyAttackTaskService.addPlayerDailyAttackOther(attacker.forces);
        }
        // ?????????????????????
        if (defender.lost > 0) {
            if (battle.isAtkNpc()) {
                if (atkSuccess) {
                    // ???????????????
                    warService.subBattleNpcArm(defender.forces, city);
                }
            } else {
                // ?????????npc?????????
                warService.subBattleNpcArm(defender.forces, city);
                warService.subBattleHeroArm(defender.forces, changeMap, AwardFrom.CAMP_BATTLE_DEFEND);
            }
            // ??????????????????????????????
            dailyAttackTaskService.addPlayerDailyAttackOther(defender.forces);
        }

        // ????????????
        Map<Long, List<CommonPb.Award>> recoverArmyAwardMap = new HashMap<>();
        // ????????????????????????????????????
        medalDataManager.angelInWhite(attacker, recoverArmyAwardMap);
        medalDataManager.angelInWhite(defender, recoverArmyAwardMap);
        //????????????????????????---????????????
        seasonTalentService.execSeasonTalentEffect303(attacker, recoverArmyAwardMap);
        seasonTalentService.execSeasonTalentEffect303(defender, recoverArmyAwardMap);

        // ????????????-????????????????????????
        medalDataManager.sustainTheWarByMeansOfWar(attacker, defender, recoverArmyAwardMap, atkSuccess);

        // ????????????
        SolarTermsDataManager solarTermsDataManager = DataResource.ac.getBean(SolarTermsDataManager.class);
        CommonPb.RptAtkPlayer.Builder rpt = CommonPb.RptAtkPlayer.newBuilder();
        CommonPb.Record record = fightLogic.generateRecord();
        // ??????
        rpt.setNightEffect(solarTermsDataManager.getNightEffect() != null);
        rpt.setResult(atkSuccess);
        rpt.setRecord(record);

        Player atkPlayer = battle.getSponsor();
        Lord atkLord = atkPlayer.lord;
        // ???????????????????????????????????????
        String atkNick = atkLord.getNick();
        rpt.setAttack(PbHelper.createRptMan(atkLord.getPos(), atkNick, atkLord.getVip(), atkLord.getLevel()));
        // ????????????????????????
        rpt.setAtkSum(PbHelper.createRptSummary(attacker.total, attacker.lost, atkLord.getCamp(), atkNick,
                atkLord.getPortrait(), atkPlayer.getDressUp().getCurPortraitFrame()));

        Lord defLord = null;
        if (null != battle.getDefencer()) {
            defLord = battle.getDefencer().lord;
        }
        int cityId = city.getCityId();
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

        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        HonorDailyService honorDailyService = DataResource.ac.getBean(HonorDailyService.class);
        ChatDataManager chatDataManager = DataResource.ac.getBean(ChatDataManager.class);
        CityService cityService = DataResource.ac.getBean(CityService.class);
        CampDataManager campDataManager = DataResource.ac.getBean(CampDataManager.class);
        CampService campService = DataResource.ac.getBean(CampService.class);
        TaskDataManager taskDataManager = DataResource.ac.getBean(TaskDataManager.class);
        BattlePassDataManager battlePassDataManager = DataResource.ac.getBean(BattlePassDataManager.class);
        ActivityDataManager activityDataManager = DataResource.ac.getBean(ActivityDataManager.class);

        WorldWarSeasonDailyRestrictTaskService restrictTaskService = DataResource.ac
                .getBean(WorldWarSeasonDailyRestrictTaskService.class);

        Turple<Integer, Integer> atkPos = cmap.posToTurple(atkLord.getPos());
        if (atkPos == null) {
            atkPos = new Turple<Integer, Integer>(0, 0);
        }
        Turple<Integer, Integer> defPos = cmap.posToTurple(battle.getPos());
        Map<Long, FightRecord> recordMap = null;
        if (atkSuccess) {
            // ?????????????????????????????????????????????
            city.setCamp(battle.getAtkCamp());
            // ???????????????????????? ???????????????
            warService.recordPartyBattle(battle, battle.getType(), battle.getAtkCamp(), true);
            // ?????????????????????????????????????????????
            int preCityCamp = city.getCamp();
            // ????????????????????????
            List<Long> joinRoles = battle.getAtkList().stream().map(r -> r.getRoleId()).distinct()
                    .collect(Collectors.toList());
            // ?????????????????????
            int hasCityCnt = (int) cmap.getCityMap().values().stream()
                    .filter(cityEntity -> cityEntity.getCity().getCityId() != CrossWorldMapConstant.NEW_YORK_CITY_ID)
                    .filter(cityEntity -> cityEntity.getCity().getCamp() == battle.getAtkCamp()).count();
            StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(city.getCityId());
            if (hasCityCnt > CrossWorldMapConstant.OWN_CITY_MAX_CNT) {
                // ?????????npc??????????????????
                city.setCamp(Constant.Camp.NPC);
            }
            // ????????????
            armRepair(city, staticCity);
            city.setStatus(WorldConstant.CITY_STATUS_FREE);

            city.setProtectTime(now + WorldConstant.CITY_PROTECT_TIME_NEW_MAP * TimeHelper.MINUTE);
            // ????????????
            if (preCityCamp == Constant.Camp.NPC) {
                final City cityF = city;
                joinRoles.forEach(roleId -> cityF.getFirstKillReward().put(roleId, 0));
            } else {
                // ??????????????????????????????
                city.getFirstKillReward().clear();
            }

            // ?????? ??????????????????
            honorDailyService.addAndCheckHonorReport2s(battle.getSponsor(), HonorDailyConstant.COND_ID_2);
            if (Constant.START_SOLAR_TERMS_CITY_TYPE == staticCity.getType()) {
                // ??????????????????
                if (!solarTermsDataManager.isSolarTermsBegin()) {
                    solarTermsDataManager.setSolarTermsBeginTime(now);
                }
            }

            // ???????????????????????????
            List<Integer> cancelBattleIds = mapWarData.getBattlePosCache().get(pos).stream()
                    .filter(battlId -> battlId.intValue() != getBattleId()).collect(Collectors.toList());
            if (!CheckNull.isEmpty(cancelBattleIds)) {
                cancelBattleIds.stream().map(battleId -> mapWarData.getAllBattles().get(battleId))
                        .filter(b -> b != null).forEach(b -> {
                            b.cancelBattleAndReturnArmy(mapWarData, CancelBattleType.UNKNOW);
                        });
            }

            // ??????????????????????????????,????????????????????????
            if (!CheckNull.isNullTrim(atkNick)) {
                chatDataManager.sendSysChat(ChatConst.CHAT_CITY_OCCUPIED, battle.getAtkCamp(), 1, city.getFinishTime(),
                        battle.getDefCamp(), cityId, defPos.getA(), defPos.getB(), battle.getAtkCamp(), atkNick);
                chatDataManager.sendSysChat(ChatConst.CHAT_CITY_OCCUPIED, battle.getDefCamp(), 1, city.getFinishTime(),
                        battle.getDefCamp(), cityId, defPos.getA(), defPos.getB(), battle.getAtkCamp(), atkNick);
            } else {
                chatDataManager.sendSysChat(ChatConst.CHAT_CITY_NPC_OCCUPIED, battle.getAtkCamp(), 1,
                        city.getFinishTime(), battle.getDefCamp(), cityId, defPos.getA(), defPos.getB(),
                        battle.getAtkCamp());
                chatDataManager.sendSysChat(ChatConst.CHAT_CITY_NPC_OCCUPIED, battle.getDefCamp(), 1,
                        city.getFinishTime(), battle.getDefCamp(), cityId, defPos.getA(), defPos.getB(),
                        battle.getAtkCamp());
            }

            try {
                // ??????????????????
                Turple<Integer, Integer> xy = staticCity.getCityPosXy();
                // ?????????????????????
                PartyLogHelper.addPartyLog(battle.getAtkCamp(), PartyConstant.LOG_CITY_CONQUERED, cityId, xy.getA(),
                        xy.getB(), atkNick);

                // ??????????????????????????????
                if (!battle.isAtkNpc()) {
                    PartyLogHelper.addPartyLog(battle.getDefCamp(), PartyConstant.LOG_CITY_BREACHED, cityId, xy.getA(),
                            xy.getB(), battle.getAtkCamp(), atkNick);
                }
            } catch (MwException e) {
                LogUtil.error(e);
            }

            // ??????????????????????????????
            battle.getAtkList().stream().mapToLong(rb -> rb.getRoleId()).distinct().forEach(roleId -> {
                Player actPlayer = playerDataManager.getPlayer(roleId);
                // ???????????????????????????
                taskDataManager.updTask(actPlayer, TaskType.COND_BATTLE_STATE_LV_CNT, 1, staticCity.getType());
                taskDataManager.updTask(actPlayer, TaskType.COND_BATTLE_STATE_LV_CNT, 1);
                restrictTaskService.updatePlayerDailyRestrictTask(actPlayer, TaskType.COND_BATTLE_STATE_LV_CNT, 1);
                activityDataManager.updActivity(actPlayer, ActivityConst.ACT_ATTACK_CITY, 1, staticCity.getType(),
                        true);
                activityDataManager.updRankActivity(actPlayer, ActivityConst.ACT_CAMP_BATTLE_RANK, 1);
                activityDataManager.updDay7ActSchedule(actPlayer, ActivityConst.ACT_TASK_ATTACK, staticCity.getType());
                activityDataManager.updDay7ActSchedule(actPlayer, ActivityConst.ACT_TASK_CITY);
                if (preCityCamp != Constant.Camp.NPC && preCityCamp != actPlayer.lord.getCamp()) {
                    activityDataManager.updAtkCityActSchedule(actPlayer, ActivityConst.ACT_TASK_JOIN_ATK_OTHER_CITY);
                }
                // ???????????????
                activityDataManager.updAtkCityActSchedule(actPlayer, ActivityConst.ACT_TASK_ATTACK,
                        staticCity.getType());
                // ????????????????????????
                activityDataManager.updAtkCityActSchedule(actPlayer, ActivityConst.ACT_TASK_CITY);
            });
            // ??????city??????
            cityService.syncPartyCity(city, staticCity);
            recordMap = warService.recordRoleFight(attacker.forces, true);
        } else {
            // ??????????????????
            List<BaseMapBattle> cityBattleList = mapWarData.getBattlesByPos(pos);
            if (!CheckNull.isEmpty(cityBattleList)) {
                // ??????????????????????????????,????????????????????????
                if (cityBattleList.size() == 1) {
                    city.setStatus(WorldConstant.CITY_STATUS_CALM);
                } else if (cityBattleList.size() > 1) {
                    // ???????????????????????????????????????????????????????????????
                    BaseMapBattle nextBattle = cityBattleList.get(1);
                    city.setAttackCamp(nextBattle.getBattle().getAtkCamp());
                }
            } else {
                // ??????else ???????????????????????????????????????
                city.setStatus(WorldConstant.CITY_STATUS_CALM);
            }
            recordMap = warService.recordRoleFight(defender.forces, false);
        }

        CommonPb.Report.Builder report = CommonPb.Report.newBuilder();
        report.setTime(now);

        warService.addBattleHeroExp(attacker.forces, AwardFrom.CAMP_BATTLE_ATTACK, rpt, true, false,
                battle.isCityBattle(), changeMap, true, exploitAwardMap);
        if (!battle.isAtkNpc()) {
            warService.addBattleHeroExp(defender.forces, AwardFrom.CAMP_BATTLE_DEFEND, rpt, false, false,
                    battle.isCityBattle(), changeMap, true, exploitAwardMap);
        } else {
            DataResource.ac.getBean(WorldService.class).buildRptHeroData(defender, rpt, Constant.Role.CITY, true);
        }
        report.setRptPlayer(rpt);
        // ????????????????????????
        Optional.of(campDataManager.getParty(battle.getAtkCamp()))
                .ifPresent((party) -> campService.checkHonorRewardAndSendSysChat(party));
        // ??????????????????????????????????????????????????????????????????
        Map<Long, List<Award>> dropMap = warService.sendResourceReward(recordMap, changeMap);
        warService.sendCampBattleMail(battle, cityId, atkLord, defLord, atkPos, defPos, atkSuccess, report, dropMap,
                now, recoverArmyAwardMap);

        // ?????????????????????
        mapWarData.getCrossWorldMap().publishMapEvent(MapEvent.mapEntity(battle.getPos(), MapCurdEvent.UPDATE));

        // ?????????????????????????????????
        warService.sendRoleResChange(changeMap);
        // ???????????????
        warService.logBattle(battle, fightLogic.getWinState(),attacker,defender, rpt.getAtkHeroList(), rpt.getDefHeroList());
    }

    /**
     * ????????????
     * 
     * @param city
     * @param staticCity
     */
    private void armRepair(City city, StaticCity staticCity) {
        for (CityHero hero : city.getFormList()) {
            StaticNpc sNpc = StaticNpcDataMgr.getNpcMap().get(hero.getNpcId());
            if (sNpc != null) {
                hero.setCurArm(sNpc.getTotalArm());
            }
        }
    }

    @Override
    protected void onCancelBattleAfter(MapWarData mapWarData, CancelBattleType cancelType) {
        super.onCancelBattleAfter(mapWarData, cancelType);
    }

    /**
     * ????????????
     * 
     * @param param ??????
     * @throws MwException ???????????????
     */
    @Override
    public void joinBattle(AttackParamDto param) throws MwException {
        int now = TimeHelper.getCurrentSecond();
        Player player = param.getInvokePlayer();
        int camp = player.lord.getCamp();
        long roleId = player.roleId;
        CrossWorldMap crossWorldMap = param.getCrossWorldMap();
        int battleId = getBattleId();
        Battle battle = getBattle();
        if (WorldConstant.ATTACK_STATE_NEED_LV > player.lord.getLevel()) {
            throw new MwException(GameError.ATTACK_STATE_NEED_LV.getCode(), "?????????????????????45???????????????????????????, roleId:", roleId);
        }

        if (battle.getAtkCamp() != camp && battle.getDefCamp() != camp) {
            throw new MwException(GameError.CAN_NOT_JOIN_BATTLE.getCode(), "???????????????????????????????????????, roleId:", roleId,
                    ", battleId:", battleId, ", roleCamp:", camp);
        }
        // ????????????
        checkAndSubFood(param);
        BaseArmy baseArmy = createBaseArmy(param, now, ArmyConstant.ARMY_TYPE_ATK_CAMP);
        addBattleRole(param,AwardFrom.ATTACK_CITY_BATTLE);
        // ????????????
        crossWorldMap.publishMapEvent(baseArmy.createMapEvent(MapCurdEvent.CREATE),
                MapEvent.mapEntity(player.lord.getPos(), MapCurdEvent.UPDATE));
        // ???????????????
        param.setArmy(PbHelper.createArmyPb(baseArmy.getArmy(), false));
        param.setBattle(PbHelper.createBattlePb(battle));
    }
}
