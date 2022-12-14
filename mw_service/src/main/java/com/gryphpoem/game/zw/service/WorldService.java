package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.*;
import com.gryphpoem.game.zw.gameplay.local.manger.CrossWorldMapDataManager;
import com.gryphpoem.game.zw.gameplay.local.service.CrossArmyService;
import com.gryphpoem.game.zw.gameplay.local.service.CrossAttackService;
import com.gryphpoem.game.zw.gameplay.local.service.CrossSummonService;
import com.gryphpoem.game.zw.gameplay.local.service.CrossWorldMapService;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonDailyAttackTaskService;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonDailyRestrictTaskService;
import com.gryphpoem.game.zw.gameplay.local.util.MapCurdEvent;
import com.gryphpoem.game.zw.gameplay.local.util.MapEvent;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.battle.BaseMapBattle;
import com.gryphpoem.game.zw.logic.FightSettleLogic;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.CommonPb.BattleRole;
import com.gryphpoem.game.zw.pb.CommonPb.Prop;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.pb.GamePb1.SynWallCallBackRs;
import com.gryphpoem.game.zw.pb.GamePb2;
import com.gryphpoem.game.zw.pb.GamePb2.*;
import com.gryphpoem.game.zw.pb.GamePb3;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pb.GamePb4.GetAreaCentreCityRs;
import com.gryphpoem.game.zw.pb.GamePb4.GetBattleByIdRq;
import com.gryphpoem.game.zw.pb.GamePb4.GetBattleByIdRs;
import com.gryphpoem.game.zw.pb.GamePb4.GetNightRaidInfoRs;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.constant.Constant.AttrId;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.*;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.GameGlobal;
import com.gryphpoem.game.zw.resource.pojo.Mail;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.army.Guard;
import com.gryphpoem.game.zw.resource.pojo.army.March;
import com.gryphpoem.game.zw.resource.pojo.dressup.BaseDressUpEntity;
import com.gryphpoem.game.zw.resource.pojo.fight.FightLogic;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.party.Camp;
import com.gryphpoem.game.zw.resource.pojo.relic.GlobalRelic;
import com.gryphpoem.game.zw.resource.pojo.relic.RelicEntity;
import com.gryphpoem.game.zw.resource.pojo.world.*;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.resource.util.eventdata.EventDataUp;
import com.gryphpoem.game.zw.service.activity.*;
import com.gryphpoem.game.zw.service.relic.RelicService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author TanDonghai
 * @ClassName WorldService.java
 * @Description ????????????, ???????????????????????????????????????????????????????????????????????????
 * ??????????????????????????????????????????????????????????????????????????????????????????????????????Battle???????????????????????????????????????????????????????????????warServic?????????
 * worldTimerLogic???????????????????????????????????????????????????????????????????????????????????????????????????????????????
 * @date ???????????????2017???3???30??? ??????10:20:18
 */
@Service
public class WorldService {

    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private BerlinWarService berlinWarService;

    @Autowired
    private GlobalDataManager globalDataManager;

    @Autowired
    private WorldDataManager worldDataManager;

    @Autowired
    private RewardDataManager rewardDataManager;

    @Autowired
    private TaskDataManager taskDataManager;

    @Autowired
    private WarDataManager warDataManager;

    @Autowired
    private FightService fightService;

    @Autowired
    private MailDataManager mailDataManager;

    @Autowired
    private HeroService heroService;

    @Autowired
    private MineService mineService;

    @Autowired
    private WarService warService;

    @Autowired
    private FightSettleLogic fightSettleLogic;

    @Autowired
    private TechDataManager techDataManager;

    @Autowired
    private ActivityDataManager activityDataManager;

    @Autowired
    private WallService wallService;

    @Autowired
    private VipDataManager vipDataManager;
    // @Autowired
    // private ArmyService armyService;
    @Autowired
    private BuildingDataManager buildingDataManager;

    @Autowired
    private ActivityService activityService;
    @Autowired
    private GestapoService gestapoService;
    @Autowired
    private ServerSetting serverSetting;
    @Autowired
    private SuperMineService superMineService;
    @Autowired
    private AirshipService airshipService;
    @Autowired
    private HonorDailyService honorDailyService;
    @Autowired
    private HonorDailyDataManager honorDailyDataManager;
    @Autowired
    private MedalDataManager medalDataManager;

    @Autowired
    private ChatDataManager chatDataManager;
    @Autowired
    private RebelService rebelService;
    @Autowired
    private CounterAtkService counterAtkService;
    @Autowired
    private DecisiveBattleService decisiveBattleService;
    @Autowired
    private MarchService marchService;
    @Autowired
    private WorldScheduleService worldScheduleService;
    @Autowired
    private CrossArmyService crossArmyService;
    @Autowired
    private CrossAttackService crossAttackService;
    @Autowired
    private WorldWarSeasonDailyAttackTaskService dailyAttackTaskService;
    @Autowired
    private CrossWorldMapDataManager crossWorldMapDataManager;
    @Autowired
    private CrossSummonService crossSummonService;
    @Autowired
    private BattlePassDataManager battlePassDataManager;
    @Autowired
    private ActivityChristmasService activityChristmasService;
    @Autowired
    private DressUpDataManager dressUpDataManager;
    @Autowired
    private SeasonTalentService seasonTalentService;
    @Autowired
    private RamadanVisitAltarService ramadanVisitAltarService;
    @Autowired
    private MusicFestivalCreativeService musicFestivalCreativeService;
    @Autowired
    private CampService campService;

    /**
     * ?????????????????????????????????
     *
     * @param roleId
     * @param area
     * @return
     * @throws MwException
     */
    public GetAreaRs getArea(long roleId, int area) throws MwException {
        // ????????????????????????
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        StaticArea staticArea = StaticWorldDataMgr.getAreaMap().get(area);
        if (null == staticArea || !staticArea.isOpen()) {
            throw new MwException(GameError.AREA_NOT_OPEN.getCode(), "????????????????????????????????????, roleId:", roleId, ", area:", area);
        }
        worldDataManager.openPos(player);

        // ??????????????????
        // Area worldArea = worldDataManager.getAreaByAreaId(area);
        // if (worldArea.getStatus() == WorldConstant.AREA_STATUS_CLOSE) {
        // throw new MwException(GameError.AREA_NOT_OPEN.getCode(),
        // "??????????????????????????????????????????, roleId:", roleId, ", area:", area);
        // }

        // ????????????
        GetAreaRs.Builder builder = GetAreaRs.newBuilder();
        // ??????????????????????????????
        List<Player> playerList = worldDataManager.getPlayerInArea(area);
        if (!CheckNull.isEmpty(playerList)) {
            for (Player p : playerList) {
                builder.addForce(PbHelper.createAreaForcePb(p.lord.getPos(), p.lord.getCamp(), p.lord.getLevel(),
                        p.building.getCommand()));
            }
        }

        // ????????????
        List<Gestapo> gestapoByArea = worldDataManager.getGestapoByArea(area);
        if (!CheckNull.isEmpty(gestapoByArea)) {
            for (Gestapo gestapo : gestapoByArea) {
                builder.addGestapo(
                        PbHelper.createAreaGestapoPb(gestapo.getPos(), gestapo.getRoleId(), gestapo.getEndTime()));
            }
        }

        // ????????????
        List<StaticCity> cityList = StaticWorldDataMgr.getCityByArea(area);
        if (CheckNull.isEmpty(cityList)) {
            LogUtil.error("?????????????????????, area:", area);
        } else {
            City city;
            for (StaticCity staticCity : cityList) {
                city = worldDataManager.getCityById(staticCity.getCityId());
                if (null == city) {
                    LogUtil.error("??????????????????????????????????????????!!!cityId:", staticCity.getCityId());
                } else {
                    Player cityOwner = playerDataManager.getPlayer(city.getOwnerId());
                    builder.addCity(PbHelper.createAreaCityPb(player, city, cityOwner));
                }
            }
        }

        // ?????????????????????????????????
        for (Army army : player.armys.values()) {
            LogUtil.debug("roleId army=" + roleId + ":" + army);
            if (army.getState() == ArmyConstant.ARMY_STATE_RETREAT) {
                builder.addLine(PbHelper.createTwoIntPb(army.getTarget(), player.lord.getPos()));
            } else {
                builder.addLine(PbHelper.createTwoIntPb(player.lord.getPos(), army.getTarget()));
            }
        }

        // ????????????????????????
        if (area == WorldConstant.AREA_TYPE_13 && BerlinWar.getInstance() != null) {
            StaticWorldDataMgr.getBerlinWarMap().values().stream()
                    .filter(sblw -> sblw.getType() == StaticBerlinWarDataMgr.BATTLEFRONT_TYPE).forEach(s -> {
                        CommonPb.AreaCity.Builder areaCityBuilder = CommonPb.AreaCity.newBuilder();
                        BerlinWar berlinWar = BerlinWar.getInstance();
                        int cityId = s.getKeyId();
                        areaCityBuilder.setCityId(cityId);
                        BerlinCityInfo battleFront = berlinWar.getBattlefrontByCityId(cityId);
                        if (!CheckNull.isNull(berlinWar) && !CheckNull.isNull(battleFront)) {
                            areaCityBuilder.setCamp(battleFront.getCamp());
                            areaCityBuilder.setFinishTime(battleFront.getNextAtkTime());
                        } else {
                            areaCityBuilder.setCamp(Constant.Camp.NPC);
                        }
                        builder.addCity(areaCityBuilder.build());
                    });
        }

        RelicEntity relicEntity = worldDataManager.getRelicEntityMap().values().stream().filter(o -> o.getArea() == area).findFirst().orElse(null);
        Optional.ofNullable(relicEntity).ifPresent(o -> {
            GlobalRelic globalRelic = globalDataManager.getGameGlobal().getGlobalRelic();
            CommonPb.MapRelicForce.Builder mapRelicForce = CommonPb.MapRelicForce.newBuilder();
            mapRelicForce.setPos(o.getPos());
            mapRelicForce.setSafeExpire(globalRelic.getSafeExpire());
            mapRelicForce.setOverExpire(globalRelic.getOverExpire());
            mapRelicForce.setHoldCamp(o.getHoldCamp());
            builder.addMapRelicForce(mapRelicForce);
        });
        return builder.build();
    }

    /**
     * ??????????????????????????????
     *
     * @param roleId
     * @param area
     * @return
     */
    public GetMarchRs getMarch(long roleId, int area) {
        GetMarchRs.Builder builder = GetMarchRs.newBuilder();

        // ??????????????????????????????????????????
        Map<Integer, March> marchMap = worldDataManager.getMarchInArea(area);
        if (!CheckNull.isEmpty(marchMap)) {
            for (March march : marchMap.values()) {
                if (march.isInMarch()) {
                    Integer battleId = march.getArmy().getBattleId();
                    Battle battle = battleId != null ? warDataManager.getBattleMap().get(battleId) : null;
                    int battleType = battle != null ? battle.getBattleType() : 0;
                    builder.addMarch(PbHelper.createMapLinePb(march, battleType));
                }
            }
        }

        return builder.build();
    }

    /**
     * ??????????????????????????????
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public AttackRolesRs getAttackRoles(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        AttackRolesRs.Builder builder = AttackRolesRs.newBuilder();

        // ????????????????????????
        int area = player.lord.getArea();
        int pos = player.lord.getPos();

        List<Battle> battleList = null;
        if (CrossWorldMapService.isOnCrossMap(player)) {
            // ????????????????????????????????????
            CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(area);
            if (Objects.nonNull(cMap)) {
                List<BaseMapBattle> battlesByPos = cMap.getMapWarData().getBattlesByPos(pos);
                if (!CheckNull.isEmpty(battlesByPos)) {
                    battleList = battlesByPos.stream().map(BaseMapBattle::getBattle).collect(Collectors.toList());
                }
            }
        } else {
            // ????????????????????????
            battleList = warDataManager.getBattlePosMap().get(pos);
        }
        if (!CheckNull.isEmpty(battleList)) {
            // ?????????????????????
            addAttackRoles(roleId, builder, battleList);
        }

        // if (!CheckNull.isEmpty(warDataManager.getSpecialBattleMap())) {
        // List<Battle> specialBattle =
        // warDataManager.getSpecialBattleMap().values().stream() //
        // ????????????????????????BOSS???????????????
        // .filter(battle -> battle.getType() ==
        // WorldConstant.BATTLE_TYPE_COUNTER_ATK
        // && battle.getBattleType() == WorldConstant.COUNTER_ATK_ATK &&
        // battle.getDefencer() != null
        // && battle.getDefencer().roleId ==
        // roleId).distinct().collect(Collectors.toList());
        // if (!CheckNull.isEmpty(specialBattle)) {
        // addAttackRoles(roleId, builder, specialBattle);
        // }
        // }

        // ????????????
        GlobalRebellion globalRebellion = globalDataManager.getGameGlobal().getGlobalRebellion();
        PlayerRebellion pr = player.getPlayerRebellion();
        builder.setMyJoin(globalRebellion.getJoinRoleId().contains(player.roleId));
        builder.setIsDead(pr != null && pr.isDead());
        builder.setGRebellion(globalRebellion.ser(false));
        return builder.build();
    }

    private void addAttackRoles(long roleId, AttackRolesRs.Builder builder, List<Battle> battleList) {
        if (!CheckNull.isEmpty(battleList)) {
            for (Battle battle : battleList) {
                if (battle.getDefencer() != null && battle.getDefencer().roleId == roleId) {
                    Player atkPlayer = battle.getSponsor();

                    int battleType = battle.getType();
                    List<String> params = new ArrayList<>();
                    if (battleType == WorldConstant.BATTLE_TYPE_COUNTER_ATK) {
                        CounterAttack counterAttack = globalDataManager.getGameGlobal().getCounterAttack();
                        params.add(String.valueOf(counterAttack.getCurrentAtkCnt()));
                        params.add(String.valueOf(counterAttack.getCityId()));
                    }
                    SyncAttackRoleRs attackRoleRs = PbHelper.createSyncAttackRoleRs(
                            CheckNull.isNull(atkPlayer) ? null : atkPlayer.lord, battle.getBattleTime(),
                            WorldConstant.ATTACK_ROLE_1, battleType, params);
                    builder.addAttackRoles(attackRoleRs);
                }
            }
        }
    }

    /**
     * ????????????????????????????????????
     *
     * @param roleId
     * @param blockList
     * @return
     * @throws MwException
     */
    public GetMapRs getMap(long roleId, List<Integer> blockList) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        // ????????????
        GetMapRs.Builder builder = GetMapRs.newBuilder();
        City city;
        Player owner;
        StaticMine mine;
        StaticBandit bandit;
        List<Player> playerList;
        List<StaticCity> cityList;
        Map<Integer, Integer> mineMap;
        Map<Integer, Integer> banditMap;
        Map<Integer, CabinetLead> cabinetLeadMap;
        Map<Integer, Gestapo> gestapoInBlockMap;
        int now = TimeHelper.getCurrentSecond();
        worldDataManager.openPos(player);
        for (Integer block : blockList) {
            // ??????????????????
            gestapoInBlockMap = worldDataManager.getGestapoInBlock(block);
            if (!CheckNull.isEmpty(gestapoInBlockMap)) {
                for (Entry<Integer, Gestapo> entry : gestapoInBlockMap.entrySet()) {
                    Integer pos = entry.getKey();
                    boolean battle = false;
                    int battleTime = 0;
                    LinkedList<Battle> battles = warDataManager.getBattlePosMap().get(pos);
                    if (!CheckNull.isEmpty(battles)) {
                        List<Battle> battleList = battles.stream()
                                .filter(e -> e.getType() == WorldConstant.BATTLE_TYPE_GESTAPO)
                                .collect(Collectors.toList());
                        if (!CheckNull.isEmpty(battleList)) {
                            Battle bat = battles.get(0);
                            battleTime = bat.getBattleTime();
                            battle = battleTime > 0 ? true : false;
                        }
                    }
                    Gestapo gestapo = entry.getValue();
                    StaticGestapoPlan staticGestapoPlan = StaticWorldDataMgr.getGestapoPlanById(gestapo.getGestapoId());
                    if (CheckNull.isNull(staticGestapoPlan)) {
                        LogUtil.error("?????????????????????????????????????????????, StaticGestapoPlanId:", gestapo.getGestapoId());
                        continue;
                    }
                    builder.addForce(PbHelper.createMapForce(pos, WorldConstant.FORCE_TYPE_GESTAPO,
                            gestapo.getGestapoId(), null, -1, null, -1, battle, 0, battleTime));
                }
            }

            // ??????????????????
            cabinetLeadMap = worldDataManager.getCabinetLeadInBlock(block);
            if (!CheckNull.isEmpty(cabinetLeadMap)) {
                for (Entry<Integer, CabinetLead> entry : cabinetLeadMap.entrySet()) {
                    CabinetLead lead = entry.getValue();
                    StaticCabinetPlan cabinetPlan = StaticBuildingDataMgr.getCabinetPlanById(lead.getCabinetPlanId());
                    if (null == cabinetPlan) {
                        LogUtil.error("?????????????????????????????????????????????, StaticCabinetPlanId:", lead.getCabinetPlanId());
                        continue;
                    }
                    builder.addForce(PbHelper.createMapForce(entry.getKey(), WorldConstant.FORCE_TYPE_CABINET_LEAD,
                            cabinetPlan.getBanditId(), null, -1, null, -1, false, 0, 0));
                }
            }

            // ????????????
            banditMap = worldDataManager.getBanditInBlock(block);
            if (!CheckNull.isEmpty(banditMap)) {
                for (Entry<Integer, Integer> entry : banditMap.entrySet()) {
                    bandit = StaticBanditDataMgr.getBanditMap().get(entry.getValue());
                    if (null == bandit) {
                        LogUtil.error("???????????????????????????????????????, banditId:", entry.getValue());
                        continue;
                    }

                    builder.addForce(PbHelper.createMapForce(entry.getKey(), WorldConstant.FORCE_TYPE_BANDIT,
                            bandit.getBanditId(), null, -1, null, -1, false, 0, 0));
                }
            }

            // ????????????
            mineMap = worldDataManager.getMineInBlock(block);
            if (!CheckNull.isEmpty(mineMap)) {
                int pos;
                int resource;
                Guard guard;
                CommonPb.Collect.Builder collect;
                for (Entry<Integer, Integer> entry : mineMap.entrySet()) {
                    mine = StaticWorldDataMgr.getMineMap().get(entry.getValue());
                    if (null == mine) {
                        LogUtil.error("???????????????????????????????????????, mineId:", entry.getValue());
                        continue;
                    }

                    pos = entry.getKey();
                    if (worldDataManager.hasGuard(pos)) {// ????????? ????????????????????????????????????
                        guard = worldDataManager.getGuardByPos(pos);
                        collect = PbHelper.createCollectBuilder(guard);
                        resource = calcMineResource(guard.getPlayer(), pos, now);
                    } else {
                        collect = null;
                        resource = worldDataManager.getMineResource(pos);
                    }

                    builder.addForce(
                            PbHelper.createMapForce(entry.getKey(), WorldConstant.FORCE_TYPE_MINE, mine.getMineId(),
                                    null, -1, collect == null ? null : collect.build(), resource, false, 0, 0));
                }
            }
            // ??????????????????
            List<SuperMine> superMine = superMineService.getSuperMineByBlock(block);
            for (SuperMine sm : superMine) {
                builder.addForce(PbHelper.createMapForcePb(sm));
            }
            // ????????????
            List<AirshipWorldData> airshipList = airshipService.getAirshipByBlock(block);
            if (!CheckNull.isEmpty(airshipList)) {
                for (AirshipWorldData aswd : airshipList) {
                    builder.addForce(PbHelper.createMapForcePb(aswd, playerDataManager));
                }
            }
/*            // ????????????boss
            List<ScheduleBoss> bosses = globalDataManager.getGameGlobal().getGlobalSchedule().bosses();
            if (!CheckNull.isEmpty(bosses)) {
                for (ScheduleBoss boss : bosses) {
                    int scheduleId = boss.getScheduleId();
                    List<StaticScheduleBoss> scheduleBosses = StaticWorldDataMgr.getScheduleBossById(scheduleId);
                    if (!CheckNull.isEmpty(scheduleBosses)) {
                        for (StaticScheduleBoss ssb : scheduleBosses) {
                            builder.addForce(PbHelper.createMapForcePb(ssb, boss));
                        }
                    }
                }
            }*/
            // ????????????
            playerList = worldDataManager.getPlayerInBlock(block);
            Effect effect;
            for (Player p : playerList) {
                effect = p.getEffect().get(EffectConstant.PROTECT);
                // ????????????????????????????????????
                boolean battle = warDataManager.posHaveBattle(p.lord.getPos());
                int prot = (effect != null && effect.getEndTime() > now) ? 1 : 0;
                int protTime = effect != null ? effect.getEndTime() : 0;
                boolean showSummon = p.summon != null && p.summon.getStatus() != 0
                        && now < p.summon.getLastTime() + Constant.SUMMON_KEEP_TIME
                        && p.lord.getCamp() == player.lord.getCamp();
                int rebelRoundId = rebelService.getRoundIdByPlayer(p);
                builder.addForce(PbHelper.createMapForceByPlayer(p, battle, prot, showSummon, protTime, rebelRoundId));
            }

            // ????????????
            cityList = StaticWorldDataMgr.getCityInBlock(block);
            if (!CheckNull.isEmpty(cityList)) {
                CommonPb.MapCity mapCity;
                for (StaticCity staticCity : cityList) {
                    city = worldDataManager.getCityById(staticCity.getCityId());
                    if (city == null) {
                        LogUtil.error("???????????? city null==" + staticCity.getCityId());
                        continue;
                    }
                    owner = playerDataManager.getPlayer(city.getOwnerId());
                    // ???????????????????????? ???????????????
                    if (city.getOwnerId() > 0 && owner == null) {
                        city.setOwnerId(0);
                    }
                    LinkedList<Battle> battleList = warDataManager.getBattlePosMap().get(staticCity.getCityPos());
                    Set<Integer> atkCamp = null;
                    if (!CheckNull.isEmpty(battleList)) {
                        atkCamp = battleList.stream().map(Battle::getAtkCamp).collect(Collectors.toSet());
                    }
                    mapCity = PbHelper.createMapCityPb(player, staticCity.getCityPos(), city, staticCity,
                            owner == null ? null : owner.lord.getNick(), atkCamp);
                    if (null != mapCity) {
                        builder.addCity(mapCity);
                    }
                }
            }
            // ????????????????????????
            List<StaticBerlinWar> berlinList = StaticWorldDataMgr.getBlockBerlinWarMap().get(block);
            BerlinWar berlinWar = BerlinWar.getInstance();
            if (berlinWar != null && !CheckNull.isEmpty(berlinList)) {
                for (StaticBerlinWar bl : berlinList) {
                    CommonPb.MapCity.Builder mapCityBuilder = CommonPb.MapCity.newBuilder();
                    int cityId = bl.getKeyId();
                    mapCityBuilder.setCityId(cityId);
                    BerlinCityInfo battleFront = berlinWar.getBattlefrontByCityId(cityId);
                    if (!CheckNull.isNull(berlinWar) && !CheckNull.isNull(battleFront)) {
                        mapCityBuilder.setPos(battleFront.getPos());
                        mapCityBuilder.setCamp(battleFront.getCamp());
                        mapCityBuilder.setFinishTime(battleFront.getNextAtkTime());
                    } else {
                        mapCityBuilder.setPos(bl.getCityPos());
                        mapCityBuilder.setCamp(Constant.Camp.NPC);
                    }
                    builder.addCity(mapCityBuilder.build());
                }
            }
            // ????????????????????????
            ramadanVisitAltarService.getMap(block, builder);

            //??????
            DataResource.getBean(RelicService.class).getMapForce(block, builder);
        }
        builder.addAllBlock(blockList);
        return builder.build();
    }

    /**
     * ???????????????????????????
     *
     * @param player
     * @param pos
     * @param now
     * @return
     */
    private int calcMineResource(Player player, int pos, int now) {
        return mineService.calcMineResource(player, pos, now);
    }

    /**
     * ????????????????????????
     *
     * @param roleId
     * @param pos
     * @return
     */
    public GetMineRs getMine(long roleId, int pos) {
        GetMineRs.Builder builder = GetMineRs.newBuilder();
        StaticMine staticMine = worldDataManager.getMineByPos(pos);
        if (null != staticMine) {
            int total = worldDataManager.getMineResource(pos);
            Guard guard = worldDataManager.getGuardByPos(pos);
            if (null != guard) {
                CommonPb.Collect.Builder collect = PbHelper.createCollectBuilder(guard);
                builder.setCollect(collect.build());
                int now = TimeHelper.getCurrentSecond();
                int surplus = calcMineResource(guard.getPlayer(), pos, now);
                builder.setStartTime(guard.getBeginTime());
                builder.setEndTime(guard.getEndTime());
                builder.setResource(surplus);
                if (guard.getRoleId() == roleId) {// ??????????????????????????????????????????????????????????????????
                    builder.setGrab(total - surplus);
                }
            } else {
                builder.setResource(total);
            }
        }

        return builder.build();

    }

    /**
     * ?????????????????????????????????????????????????????? ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????,???????????????????????????????????????,
     * ????????????????????????Battle????????????????????????????????????????????????warService????????????
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public AttackPosRs attackPos(long roleId, AttackPosRq req) throws MwException {
        // ????????????????????????
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int pos = req.getPos();

        if (worldDataManager.isEmptyPos(pos)) {
            throw new MwException(GameError.EMPTY_POS.getCode(), "????????????????????????????????????????????????, roleId:", roleId, ", pos:", pos);
        }

        List<Integer> heroIdList = new ArrayList<>();
        heroIdList.addAll(req.getHeroIdList());
        heroIdList = heroIdList.stream().distinct().collect(Collectors.toList());

        // ????????????????????????
        // checkFormHero(player, heroIdList);
        checkFormHeroSupport(player, heroIdList, pos);

        Hero hero;
        int armCount = 0;
        int defArmCount = 0;
        List<TwoInt> form = new ArrayList<>();
        for (Integer heroId : heroIdList) {
            hero = player.heros.get(heroId);
            form.add(PbHelper.createTwoIntPb(heroId, hero.getCount()));
            armCount += hero.getCount();
        }

        // ??????????????????????????????
        int type = 0;
        int battleId = 0;
        int targetId = 0;
        long tarLordId = 0;
        int battleType = 0;// 1 ????????????2 ????????????3 ?????????
        int now = TimeHelper.getCurrentSecond();
        int marchTime = marchTime(player, pos, worldDataManager.getBanditIdByPos(pos) > 0 ||
                worldDataManager.getAirshipWorldDataMap().get(pos) != null);
        // ????????????????????????
        if (worldDataManager.isRelicPos(pos)) {
            marchTime = (int) (marchTime * ActParamConstant.RELIC_MARCH_SPEEDUP / NumberUtil.TEN_THOUSAND_DOUBLE);
        }
        // ??????????????????
        List<Award> marchConsume = new ArrayList<>();
        // ?????????
        int subType = 0;

        // ??????????????????
        // if (marchTime > WorldConstant.ATK_MAX_TIME) {
        // throw new MwException(GameError.ATK_MAX_TIME.getCode(), "??????????????????,
        // roleId:", roleId, ", pos:",
        // pos + ",marchTime=" + marchTime);
        // }

        int needFood = checkMarchFood(player, marchTime, armCount); // ????????????

        Battle battle = null;
        int reqType = req.getType();
        if (worldDataManager.isPlayerPos(pos)) {// ????????????
            Player target = worldDataManager.getPosData(pos);
            // ????????????????????????
            if (target.lord.getCamp() == player.lord.getCamp()) {
                throw new MwException(GameError.SAME_CAMP.getCode(), "????????????????????????, roleId:", roleId, ", pos:", pos);
            }

            // ???????????????????????????
            if (player.getDecisiveInfo().isDecisive() || target.getDecisiveInfo().isDecisive()) {
                throw new MwException(GameError.DECISIVE_BATTLE_NO_ATK.getCode(), "??????????????????????????????, roleId:", roleId,
                        ", pos:", pos);
            }
            // ??????????????????
            Effect effect = target.getEffect().get(EffectConstant.PROTECT);
            if (effect != null && effect.getEndTime() > now) {
                throw new MwException(GameError.PROTECT.getCode(), "????????????????????????????????????, roleId:", roleId, ", pos:",
                        pos + ",tarRoleId:" + target.roleId);
            }

            // ????????????
            battleType = req.hasType() ? reqType : WorldConstant.CITY_BATTLE_BLITZ;// ???????????????

            // ????????????????????????(???????????????)
            int battleMarchTime = marchTime;
            if (marchTime <= WorldConstant.MARCH_UPPER_LIMIT_TIME.get(0)) {// ??????????????????5??????
                if (WorldConstant.CITY_BATTLE_RAID == battleType) {
                    battleMarchTime = marchTime + WorldConstant.CITY_BATTLE_INCREASE_TIME;
                } else if (WorldConstant.CITY_BATTLE_EXPEDITION == battleType) {
                    battleMarchTime = marchTime + (WorldConstant.CITY_BATTLE_INCREASE_TIME * 2);
                }
            } else if (marchTime <= WorldConstant.MARCH_UPPER_LIMIT_TIME.get(1)
                    && marchTime > WorldConstant.MARCH_UPPER_LIMIT_TIME.get(0)) { // ????????????
                // ??????10?????????
                if (WorldConstant.CITY_BATTLE_EXPEDITION == battleType) {
                    battleMarchTime = marchTime + WorldConstant.CITY_BATTLE_INCREASE_TIME;
                }
            }

            StaticArea targetSArea = StaticWorldDataMgr.getAreaMap().get(target.lord.getArea());
            StaticArea mySArea = StaticWorldDataMgr.getAreaMap().get(player.lord.getArea());
            if (mySArea.getOpenOrder() == WorldConstant.AREA_ORDER_3) { // ?????????????????????,????????????????????????
                if (targetSArea.getOpenOrder() != WorldConstant.AREA_ORDER_3) {
                    // ??????????????????????????????(????????????)
                    marchTime = WorldConstant.SPECIAL_MARCH_TIME;
                    if (WorldConstant.CITY_BATTLE_RAID == battleType) {
                        battleMarchTime = WorldConstant.SPECIAL_BATTLE_TIME.get(1);
                    } else if (WorldConstant.CITY_BATTLE_EXPEDITION == battleType) {
                        battleMarchTime = WorldConstant.SPECIAL_BATTLE_TIME.get(2);
                    } else {// ?????????
                        battleMarchTime = WorldConstant.SPECIAL_BATTLE_TIME.get(0);
                    }
                } else {
                    checkMarchTimeByType(player, marchTime, battleMarchTime);
                }
            } else if (mySArea.getOpenOrder() == WorldConstant.AREA_ORDER_2) {// ?????????????????????,???????????????
                if (targetSArea.getOpenOrder() != WorldConstant.AREA_ORDER_2) {
                    throw new MwException(GameError.CAMP_BATTLE_AREA_ERROR.getCode(), "??????????????????,???????????????, roleId:", roleId,
                            ", my area:", mySArea.getArea(), ", target area:", target.lord.getArea());
                }
                checkMarchTimeByType(player, marchTime, battleMarchTime);

            } else if (mySArea.getOpenOrder() == WorldConstant.AREA_ORDER_1) { // ?????????????????????????????????
                if (targetSArea.getArea() != mySArea.getArea()) {
                    throw new MwException(GameError.CAMP_BATTLE_AREA_ERROR.getCode(), "??????????????????,???????????????, roleId:", roleId,
                            ", my area:", mySArea.getArea(), ", target area:", target.lord.getArea());
                }
                checkMarchTimeByType(player, marchTime, battleMarchTime);
            }
            // ????????????????????????
            playerDataManager.autoAddArmy(target);
            // ??????????????????
            checkPower(player, battleType, marchTime);
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.RESOURCE, AwardType.Resource.FOOD, needFood,
                    AwardFrom.ATK_POS);

            type = ArmyConstant.ARMY_TYPE_ATK_PLAYER;

            for (Integer heroId : target.heroBattle) {
                hero = target.heros.get(heroId);
                if (hero != null) {
                    defArmCount += hero.getCount();
                }
            }
            battle = new Battle();
            battle.setType(WorldConstant.BATTLE_TYPE_CITY);
            battle.setBattleType(battleType);
            battle.setBattleTime(now + battleMarchTime);
            battle.setBeginTime(now);
            battle.setDefencerId(target.lord.getLordId());
            battle.setPos(pos);
            battle.setSponsor(player);
            battle.setDefencer(target);
            battle.setDefCamp(target.lord.getCamp());
            // battle.getAtkRoleList().add(player);
            // battle.getAtkHeroIdMap().put(roleId, heroIdList);
            battle.addAtkArm(armCount);
            battle.addDefArm(defArmCount);
            battle.getAtkRoles().add(roleId);
            // battle.add
            warDataManager.addBattle(player, battle);
            battleId = battle.getBattleId();
            tarLordId = target.lord.getLordId();
            HashSet<Integer> set = player.battleMap.get(pos);
            if (set == null) {
                set = new HashSet<>();
                player.battleMap.put(pos, set);
            }
            set.add(battleId);
            set = target.battleMap.get(pos);
            if (set == null) {
                set = new HashSet<>();
                target.battleMap.put(pos, set);
            }
            set.add(battleId);

            LogUtil.debug("==player.battleMap===" + player.battleMap);

            // ?????????????????????
            if (target.isLogin) {
                syncAttackRole(target, player.lord, battle.getBattleTime(), WorldConstant.ATTACK_ROLE_1);
            }

            // ???????????????????????????????????????????????????
            // PushMessageUtil.pushMessage(target.account, PushConstant.ID_ATTACKED, target.lord.getNick(),
            //         player.lord.getNick());
            // ???????????????????????????????????????????????????
            PushMessageUtil.pushMessage(target.account, PushConstant.ENEMIES_ARE_APPROACHING);
            // ????????????
            removeProTect(player, AwardFrom.ATTACK_PLAYER_BATTLE, battle.getPos());
        } else if (worldDataManager.isCabinetLeadPos(pos)) {// ??????????????????
            checkSameArea(player, pos);
            type = ArmyConstant.ARMY_TYPE_ATK_CABINET_LEAD;
            targetId = worldDataManager.getCabinetLeadByPos(pos).getCabinetPlanId();
            // ????????????
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.RESOURCE, AwardType.Resource.FOOD, needFood,
                    AwardFrom.ATK_POS);
        } else if (worldDataManager.isBanditPos(pos)) {// ????????????
            if (player.getBanditCnt() >= Constant.ATTACK_BANDIT_MAX) {
                throw new MwException(GameError.BANDIT_OVER_MAX_CNT.getCode(), "???????????????????????????:", roleId, ", BanditCnt:",
                        player.getBanditCnt());
            }
            int banditId = worldDataManager.getBanditIdByPos(pos);
            StaticBandit staticBandit = StaticBanditDataMgr.getBanditMap().get(banditId);
            if (null == staticBandit) {
                LogUtil.error("??????id?????????, banditId:", banditId);
                throw new MwException(GameError.EMPTY_POS.getCode(), "????????????????????????, roleId:", roleId, ", pos:", pos);
            }
            Integer banditLv = player.trophy.get(TrophyConstant.TROPHY_1);
            banditLv = banditLv != null ? banditLv : 0;
            if (staticBandit.getLv() > banditLv + 1) {
                throw new MwException(GameError.BANDIT_LV_ERROR.getCode(), "?????????????????????, roleId:", roleId, ", pos:", pos);
            }
            checkSameArea(player, pos);
            type = ArmyConstant.ARMY_TYPE_ATK_BANDIT;
            targetId = worldDataManager.getBanditIdByPos(pos);
            // ????????????
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.RESOURCE, AwardType.Resource.FOOD, needFood,
                    AwardFrom.ATK_POS);
            // ????????????????????????[%]
            marchTime = airshipService.getAirShipMarchTime(player, marchTime);
            // ??????????????????
            if (checkCurTaskHasBandit(staticBandit.getLv(), banditLv)) {
                marchTime = Constant.ATTACK_BANDIT_MARCH_TIME;
            }

        } else if (worldDataManager.isMinePos(pos)) {// ??????
            // ????????????????????????
            int cnt = 0;
            for (Army army : player.armys.values()) {
                if (army.getType() == ArmyConstant.ARMY_TYPE_COLLECT
                        || army.getType() == ArmyConstant.ARMY_TYPE_COLLECT_SUPERMINE) {
                    cnt++;
                }
                if (army.getTarget() == pos && army.getState() != ArmyConstant.ARMY_STATE_RETREAT) {
                    throw new MwException(GameError.ALREADY_COLLECT_HERO.getCode(), "???????????????????????????????????????, roleId:", roleId);
                }
            }
            if (cnt >= WorldConstant.MINE_MAX_CNT) {
                throw new MwException(GameError.MINE_MAX_NUM.getCode(), "??????????????????????????????, roleId:", roleId, ", pos:", pos);
            }

            checkCollectSanctuaryMine(player, pos);
            checkCollectMineCount(player, pos, roleId, vipDataManager.getCollectMineCount(player.lord.getVip()));

            checkSameArea(player, pos);
            type = ArmyConstant.ARMY_TYPE_COLLECT;
            // ?????????????????????????????? ?????????
            boolean mineHasPlayer = false; // ????????????????????????
            if (mineHasPlayer = worldDataManager.hasGuard(pos)) {
                // ?????????????????????
                com.gryphpoem.game.zw.resource.pojo.Prop prop = player.props.get(PropConstant.ITEM_ID_5043);
                if (null == prop || prop.getCount() < 1) {
                    throw new MwException(GameError.ATTACK_MINE_PROP_NOT_ENOUGH.getCode(), " ????????????????????????????????? roleId:",
                            roleId);
                }

                Guard guard = worldDataManager.getGuardByPos(pos);
                if (guard.getPlayer().roleId != roleId) {
                    removeProTect(player, AwardFrom.COLLECT_WAR, pos); // ???????????????
                    // ?????????????????????
                    defArmCount = 0;
                    for (TwoInt tmp : guard.getArmy().getHero()) {
                        defArmCount += tmp.getV2();
                    }

                    battle = createMineBattle(guard.getPlayer(), player, pos, armCount, defArmCount, now, marchTime);
                    battleId = battle.getBattleId();
//                    if (guard.getPlayer().isLogin) {
//                        syncAttackRole(guard.getPlayer(), player.lord, battle.getBattleTime(), WorldConstant.ATTACK_ROLE_1);
//                    }
                }
            }
            targetId = worldDataManager.getMineByPos(pos).getMineId();
            List<List<Integer>> needCost = combineAttackMineCost(needFood, mineHasPlayer);
            rewardDataManager.checkAndSubPlayerRes(player, needCost, AwardFrom.ATK_POS);

            if (checkAddCollectMineCount(player, pos)) {
                player.addCollectMineCount();
            }
            //???????????????????????????
            worldDataManager.addCollectMine(roleId, pos);
        } else if (worldDataManager.isAltarPos(pos)) {
            ramadanVisitAltarService.visitAltar(pos, roleId, reqType, marchConsume);
            // ????????????
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.RESOURCE, AwardType.Resource.FOOD, needFood, AwardFrom.ATK_POS);
            type = ArmyConstant.ARMY_TYPE_ALTAR;
            subType = reqType;
        } else if (worldDataManager.isRelicPos(pos)) {
            //????????????
            DataResource.getBean(RelicService.class).checkArmy(player, pos);
            type = ArmyConstant.ARMY_TYPE_RELIC_BATTLE;
            // ??????????????????, ??????????????????
            rewardDataManager.subPlayerResHasChecked(player, AwardType.RESOURCE, AwardType.Resource.FOOD,
                    needFood, AwardFrom.ATK_POS);
            ChangeInfo change = ChangeInfo.newIns();
            change.addChangeType(AwardType.RESOURCE, AwardType.Resource.FOOD);
            // ????????????????????????????????????
            rewardDataManager.syncRoleResChanged(player, change);
            DataResource.getBean(RelicService.class).checkArmyMarchTime(player, marchTime);
        } else {
            throw new MwException(GameError.ATTACK_POS_ERROR.getCode(), "AttackPos??????????????????, roleId:", roleId, ", pos:",
                    pos);
        }

        Army army = new Army(player.maxKey(), type, pos, ArmyConstant.ARMY_STATE_MARCH, form, marchTime - 1,
                now + marchTime - 1, player.getDressUp());
        army.setBattleId(battleId);
        army.setTargetId(targetId);
        army.setLordId(roleId);
        army.setTarLordId(tarLordId);
        army.setBattleTime(battle != null ? battle.getBattleTime() : 0);
        army.setOriginPos(player.lord.getPos());
        // ??????buffer??????
        army.setOriginCity(worldDataManager.checkCityBuffer(player.lord.getPos()));
        army.setHeroMedals(heroIdList.stream()
                .map(heroId -> medalDataManager.getHeroMedalByHeroIdAndIndex(player, heroId, MedalConst.HERO_MEDAL_INDEX_0))
                .filter(Objects::nonNull)
                .map(PbHelper::createMedalPb)
                .collect(Collectors.toList()));
        if (!CheckNull.isEmpty(marchConsume)) {
            army.setMarchConsume(marchConsume);
        }
        if (subType > 0) {
            army.setSubType(subType);
        }
        player.armys.put(army.getKeyId(), army);
        if (worldDataManager.isMinePos(pos) && Objects.nonNull(battle))
            warDataManager.addBattle(player, battle);
        // ??????????????????
        March march = new March(player, army);
        worldDataManager.addMarch(march);

        // ??????????????????
        for (Integer heroId : heroIdList) {
            hero = player.heros.get(heroId);
            hero.setState(ArmyConstant.ARMY_STATE_MARCH);
        }

        // ????????????
        AttackPosRs.Builder builder = AttackPosRs.newBuilder();

        builder.setArmy(PbHelper.createArmyPb(army, false));
        if (battle != null) {
            builder.setBattle(PbHelper.createBattlePb(battle));
        }
        builder.setCollectMineCount(player.getCollectMineCount());

        // ??????????????????
        List<Integer> posList = MapHelper.getAreaStartPos(MapHelper.getLineAcorss(pos, player.lord.getPos()));
        posList.add(pos);
        posList.add(player.lord.getPos());
        EventBus.getDefault().post(
                new Events.AreaChangeNoticeEvent(posList, roleId, Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));
        return builder.build();
    }

    public List<List<Integer>> combineAttackMineCost(int needFood, boolean mineHasPlayer) {
        List<List<Integer>> needCost = new ArrayList<>(2);
        List<Integer> food = new ArrayList<>(3);
        food.add(AwardType.RESOURCE);
        food.add(AwardType.Resource.FOOD);
        food.add(needFood);
        needCost.add(food);
        if (mineHasPlayer) {
            List<Integer> prop = new ArrayList<>(3);
            prop.add(AwardType.PROP);
            prop.add(PropConstant.ITEM_ID_5043);
            prop.add(1);
            needCost.add(prop);
        }
        return needCost;
    }

    /**
     * ??????????????????
     *
     * @param player
     * @param marchTime
     * @param armCount
     * @throws MwException
     */
    public int checkMarchFood(Player player, int marchTime, int armCount) throws MwException {
        // ????????????
        int needFood = getNeedFood(marchTime, armCount);
        if (needFood > player.resource.getFood()) {
            throw new MwException(GameError.RESOURCE_NOT_ENOUGH.getCode(), "??????????????????, roleId:", player.roleId,
                    "needFood:", needFood, " haveFood:", player.resource.getFood());
        }
        return needFood;
    }

    /**
     * ????????????????????????
     *
     * @param player
     * @param pos
     */
    public void checkCollectMineCount(Player player, int pos, long roleId, int vipCollectMineCount) throws MwException {
        //??????????????????
        if (!checkAddCollectMineCount(player, pos)) {
            return;
        }

        if (player.getCollectMineCount() >= vipCollectMineCount) {
            throw new MwException(GameError.COLLECT_MINE_OVER_MAX_CNT.getCode(), "??????????????????????????????????????????:", roleId, ", collectMineCount:",
                    player.getCollectMineCount());
        }
    }

    private boolean checkCollectSanctuaryMine(Player player, int pos) throws MwException {
        // ??????????????????????????????
//            if (MapHelper.getAreaIdByPos(pos) == WorldConstant.AREA_TYPE_13 && MapHelper.isInKingCityAreaPos(
//                    StaticWorldDataMgr.getCityMap().get(WorldConstant.BERLIN_CITY_ID).getCityPos(), pos)) {
//                BerlinWar berlinWar = BerlinWar.getInstance();
//                BerlinCityInfo berlinCityInfo = berlinWar.getBerlinCityInfo();
//                if (!CheckNull.isNull(berlinWar) && berlinWar.getStatus() != WorldConstant.BERLIN_STATUS_OPEN
//                        && !CheckNull.isNull(berlinCityInfo) && berlinCityInfo.getCamp() != Constant.Camp.NPC) {
//                    if (berlinCityInfo.getCamp() != player.lord.getCamp()) {
//                        throw new MwException(GameError.BERLIN_MINE_CANT_COLLECT.getCode(),
//                                "????????????????????????, ???????????????????????????, berlinCamp:", berlinCityInfo.getCamp(), ", playerCamp",
//                                player.lord.getCamp());
//                    }
//                }
//            }

        // ????????????(???????????????????????????
        if (MapHelper.getAreaIdByPos(pos) != WorldConstant.AREA_TYPE_13) {
            return false;
        }
        if (!MapHelper.isInKingCityAreaPos(
                StaticWorldDataMgr.getCityMap().get(WorldConstant.BERLIN_CITY_ID).getCityPos(), pos)) {
            return false;
        }

        BerlinWar berlinWar = BerlinWar.getInstance();
        if (CheckNull.isNull(berlinWar)) {
            return false;
        }
        if (berlinWar.getStatus() == WorldConstant.BERLIN_STATUS_OPEN) {
            return false;
        }
        BerlinCityInfo berlinCityInfo = berlinWar.getBerlinCityInfo();
        if (CheckNull.isNull(berlinCityInfo)) {
            return false;
        }
        if (berlinCityInfo.getCamp() == Constant.Camp.NPC) {
            return false;
        }
        if (berlinCityInfo.getCamp() != player.lord.getCamp()) {
            throw new MwException(GameError.BERLIN_MINE_CANT_COLLECT.getCode(),
                    "????????????????????????, ???????????????????????????, berlinCamp:", berlinCityInfo.getCamp(), ", playerCamp",
                    player.lord.getCamp());
        }

        return true;
    }

    /**
     * ????????????????????????????????????
     *
     * @param player
     * @param pos
     * @return
     */
    private boolean checkAddCollectMineCount(Player player, int pos) {
        Guard guard = worldDataManager.getGuardByPos(pos);
        if (null == guard) {
            return false;
        }

        Player defPlayer = guard.getPlayer();
        if (null == defPlayer) {
            return false;
        }

        //???????????????????????????
        if (defPlayer.lord.getCamp() != player.lord.getCamp()) {
            return false;
        }

        return true;
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param marchTime
     * @param battleType
     * @throws MwException
     */
    private void checkMarchTimeByType(Player player, int marchTime, int battleType) throws MwException {
        if (WorldConstant.CITY_BATTLE_BLITZ == battleType) {// ?????????????????????
            if (marchTime > WorldConstant.MARCH_UPPER_LIMIT_TIME.get(0)) {
                throw new MwException(GameError.ATK_MAX_TIME.getCode(), "??????????????????????????????????????????, roleId:", player.roleId,
                        ",marchTime=" + marchTime, ", battleType=", battleType);
            }
        } else if (WorldConstant.CITY_BATTLE_RAID == battleType) {
            if (marchTime > WorldConstant.MARCH_UPPER_LIMIT_TIME.get(1)) {
                throw new MwException(GameError.ATK_MAX_TIME.getCode(), "??????????????????????????????????????????, roleId:", player.roleId,
                        ",marchTime=" + marchTime, ", battleType=", battleType);
            }
        }
    }

    /**
     * ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param roleId
     * @param pos
     * @param lv
     * @param heroIdList
     * @return
     * @throws MwException
     */
    public AttackPosRs attackPos4Task(long roleId, int pos, int lv, List<Integer> heroIdList) throws MwException {
        // ????????????????????????
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // ?????????????????????
        // ????????????????????????
        // checkFormHero(player, heroIdList);
        checkFormHeroSupport(player, heroIdList, pos);
        if (player.getBanditCnt() >= Constant.ATTACK_BANDIT_MAX) {
            throw new MwException(GameError.BANDIT_OVER_MAX_CNT.getCode(), "???????????????????????????:", roleId, ", BanditCnt:",
                    player.getBanditCnt());
        }
        List<StaticBandit> banditList = StaticBanditDataMgr.getBanditByLv(lv);
        if (CheckNull.isEmpty(banditList)) {
            LogUtil.error("???????????????????????????, lv:" + lv);
            throw new MwException(GameError.EMPTY_POS.getCode(), "????????????????????????, roleId:", roleId, ", pos:", pos);
        }

        StaticBandit staticBandit = banditList.get(RandomHelper.randomInSize(banditList.size()));
        if (null == staticBandit) {
            LogUtil.error("??????id?????????, banditId:", banditList);
            throw new MwException(GameError.EMPTY_POS.getCode(), "????????????????????????, roleId:", roleId, ", pos:", pos);
        }

        Integer banditLv = player.trophy.get(TrophyConstant.TROPHY_1);
        banditLv = banditLv != null ? banditLv : 0;
        if (WorldConstant.BANDIT_LV_999 != lv) {
            if (staticBandit.getLv() > banditLv + 1) {
                throw new MwException(GameError.BANDIT_LV_ERROR.getCode(), "?????????????????????, roleId:", roleId, ", pos:", pos,
                        ",staticBandit=" + staticBandit, "banditLv=" + banditLv);
            }
        }

        Hero hero;
        int armCount = 0;
        List<TwoInt> form = new ArrayList<>();
        for (Integer heroId : heroIdList) {
            hero = player.heros.get(heroId);
            form.add(PbHelper.createTwoIntPb(heroId, hero.getCount()));
            armCount += hero.getCount();
        }

        // ??????????????????????????????
        int battleId = 0;
        int targetId = staticBandit.getBanditId();
        int now = TimeHelper.getCurrentSecond();
        int marchTime = marchTime(player, pos);

        // ??????????????????
        if (marchTime > WorldConstant.ATK_MAX_TIME) {
            throw new MwException(GameError.ATK_MAX_TIME.getCode(), "??????????????????, roleId:", roleId, ", pos:",
                    pos + ",marchTime=" + marchTime);
        }

        // ????????????
        int needFood = getNeedFood(marchTime, armCount);
        if (needFood > player.resource.getFood()) {
            throw new MwException(GameError.RESOURCE_NOT_ENOUGH.getCode(), "??????????????????, roleId:", roleId, ", pos:", pos);
        }
        Battle battle = null;

        int type = ArmyConstant.ARMY_TYPE_ATK_CABINET_TASK;
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.RESOURCE, AwardType.Resource.FOOD, needFood,
                AwardFrom.ATK_POS);

        if (checkCurTaskHasBandit(staticBandit.getLv(), banditLv) || WorldConstant.BANDIT_LV_999 == lv) {
            marchTime = Constant.ATTACK_BANDIT_MARCH_TIME;// ?????????????????? ,??????
        }
        Army army = new Army(player.maxKey(), type, pos, ArmyConstant.ARMY_STATE_MARCH, form, marchTime,
                now + marchTime, player.getDressUp());
        army.setBattleId(battleId);
        army.setTargetId(targetId);
        army.setLordId(roleId);
        army.setBattleTime(battle != null ? battle.getBattleTime() : 0);
        army.setOriginPos(player.lord.getPos());
        army.setHeroMedals(heroIdList.stream()
                .map(heroId -> medalDataManager.getHeroMedalByHeroIdAndIndex(player, heroId, MedalConst.HERO_MEDAL_INDEX_0))
                .filter(Objects::nonNull)
                .map(PbHelper::createMedalPb)
                .collect(Collectors.toList()));

        player.armys.put(army.getKeyId(), army);
        // ????????????, ???????????????????????????????????????????????????
        // if (worldDataManager.isPlayerPos(pos) && battle != null) {
        // addBattleArmy(battle, player.roleId, heroIdList, army.getKeyId(),
        // true);
        // }

        // ??????????????????
        March march = new March(player, army);
        worldDataManager.addMarch(march);

        // ??????????????????
        for (Integer heroId : heroIdList) {
            hero = player.heros.get(heroId);
            hero.setState(ArmyConstant.ARMY_STATE_MARCH);
        }

        // ????????????
        AttackPosRs.Builder builder = AttackPosRs.newBuilder();
        builder.setArmy(PbHelper.createArmyPb(army, false));

        synRetreatArmy(player, army, now);
        return builder.build();
    }

    public void checkSameArea(Player player, int pos) throws MwException {
        StaticArea staticArea = StaticWorldDataMgr.getAreaMap().get(MapHelper.getAreaIdByPos(pos));
        if (staticArea.getOpenOrder() < 1
                || staticArea.getArea() != StaticWorldDataMgr.getAreaMap().get(player.lord.getArea()).getArea()) {
            throw new MwException(GameError.CAMP_BATTLE_AREA_ERROR.getCode(), "??????????????????,???????????????, roleId:",
                    player.lord.getLordId(), ", target area:", staticArea.getArea());
        }
    }

    /**
     * ???????????????????????????????????????
     *
     * @param player
     * @param battleType
     * @param marchTime
     * @throws MwException
     */
    public void checkPower(Player player, int battleType, int marchTime) throws MwException {
        int maxTime = 0;// ??????????????????, ???????????????????????????
        int costItemNum = 0;// ???????????????
        int costPower = 0;// ????????????
        int jobPrivilege = 0;// ??????????????????
        // List<Integer> costPrivilege = null;// ????????????????????????
        // ?????????????????????????????????
        if (battleType == WorldConstant.CITY_BATTLE_BLITZ) {
            maxTime = WorldConstant.FLAY_BATTLE_TIME.get(0);
            costItemNum = WorldConstant.FLAY_BATTLE_TIME.get(2);
            costPower = WorldConstant.FLAY_BATTLE_TIME.get(1);
            jobPrivilege = PartyConstant.PRIVILEGE_BLITZ;
            // costPrivilege = WorldConstant.PARY_JOB_BATTLE_COST.get(0);
        } else if (battleType == WorldConstant.CITY_BATTLE_RAID) {
            maxTime = WorldConstant.RUN_BATTLE_TIME.get(0);
            costItemNum = WorldConstant.RUN_BATTLE_TIME.get(2);
            costPower = WorldConstant.RUN_BATTLE_TIME.get(1);
            jobPrivilege = PartyConstant.PRIVILEGE_RAID;
            // costPrivilege = WorldConstant.PARY_JOB_BATTLE_COST.get(1);
        } else if (battleType == WorldConstant.CITY_BATTLE_EXPEDITION) {
            maxTime = WorldConstant.WALK_BATTLE_TIME.get(0);
            costItemNum = WorldConstant.WALK_BATTLE_TIME.get(2);
            costPower = WorldConstant.WALK_BATTLE_TIME.get(1);
            jobPrivilege = PartyConstant.PRIVILEGE_EXPEDITION;
            // costPrivilege = WorldConstant.PARY_JOB_BATTLE_COST.get(2);
        } else if (battleType == WorldConstant.CITY_BATTLE_DECISIVE) {
            maxTime = 0;
            costItemNum = 0;
            costPower = 0;
            jobPrivilege = 0;
        }

        if (maxTime > 0 && marchTime > maxTime) {
            throw new MwException(GameError.ATK_MAX_TIME.getCode(), "??????????????????????????????????????????, roleId:", player.lord.getLordId());
        }

        boolean hasPrivilege = false;
        int privilegeCostPower = 0;
        if (StaticPartyDataMgr.jobHavePrivilege(player.lord.getJob(), jobPrivilege)) {
            List<Integer> costPrivilege = StaticPartyDataMgr.getJobPrivilegeVal(player.lord.getJob(), jobPrivilege);
            costItemNum = costPrivilege.get(2);
            privilegeCostPower = costPrivilege.get(1);

            hasPrivilege = true;
            LogUtil.debug(player.lord.getLordId() + "????????????costItemNum=" + costItemNum + ",costPower=" + costPower
                    + ",costPrivilege=" + costPrivilege);
        }

        int seasonTalentPowerBuff = 0;
        if (costPower != 0) {
            seasonTalentPowerBuff = seasonTalentService.getSeasonTalentEffectValue(player, SeasonConst.TALENT_EFFECT_601);
        }

        if (hasPrivilege) {
            costPower = seasonTalentPowerBuff < (costPower - privilegeCostPower) ?
                    privilegeCostPower : (costPower - seasonTalentPowerBuff < 0 ? 0 : costPower - seasonTalentPowerBuff);
        } else {
            costPower = (costPower - seasonTalentPowerBuff) > 0 ? costPower - seasonTalentPowerBuff : 0;
        }

        //?????????????????????????????????, ????????????????????????
        if (costItemNum > 0) {
            double costPower_ = costPower;
            costItemNum = (int) Math.ceil(costPower_ / 5);
        }

        // ???????????? ????????????????????????
        if (costItemNum > 0) {
            if (rewardDataManager.propIsEnough(player, PropConstant.ITEM_ID_5043, costItemNum)) {
                rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.PROP, PropConstant.ITEM_ID_5043,
                        costItemNum, AwardFrom.ATACK_PLAYER);
            } else {
                try {
                    rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.ACT,
                            costPower, AwardFrom.ATACK_PLAYER);
                } catch (MwException mwException) {
                    // ???????????????????????????
                    activityService.checkTriggerGiftSync(ActivityConst.TRIGGER_GIFT_DOCOMBAT_ACT, player);
                    throw mwException;
                }
            }
        }
    }

    /**
     * ???????????????????????????
     *
     * @param target
     * @param atkLord
     * @param atkTime
     * @param status
     */
    public void syncAttackRole(Player target, Lord atkLord, int atkTime, int status) {
        syncAttackRole(target, atkLord, atkTime, status, 0);
    }

    /**
     * ???????????????????????????
     *
     * @param target
     * @param atkLord
     * @param atkTime
     * @param status
     * @param battleType ????????????
     * @param params     ????????????
     */
    public void syncAttackRole(Player target, Lord atkLord, int atkTime, int status, int battleType, Object... params) {
        ArrayList<String> paramStr = new ArrayList<>();
        for (Object param : params) {
            paramStr.add(String.valueOf(param));
        }
        if (target.ctx == null) return;
        SyncAttackRoleRs builder = PbHelper.createSyncAttackRoleRs(atkLord, atkTime, status, battleType, paramStr);
        Base.Builder msg = PbHelper.createSynBase(SyncAttackRoleRs.EXT_FIELD_NUMBER, SyncAttackRoleRs.ext, builder);
        MsgDataManager.getIns().add(new Msg(target.ctx, msg.build(), target.roleId));
    }

    /**
     * ???????????????????????????,??????????????????
     *
     * @param player
     * @param heroIdList
     * @throws MwException
     */
    public void checkFormHeroSupport(Player player, List<Integer> heroIdList, int pos) throws MwException {
        long roleId = player.roleId;
        playerDataManager.autoAddArmy(player);
        if (CheckNull.isEmpty(heroIdList)) {
            throw new MwException(GameError.ATTACK_POS_NO_HERO.getCode(), "AttackPos???????????????, roleId:", roleId);
        }
        // ?????????????????????????????????
        boolean isMinePos = worldDataManager.isMinePos(pos) && !worldDataManager.isPlayerPos(pos);

        if (worldDataManager.isPlayerPos(pos)) {// ???????????? ??????????????????????????????
            isMinePos = false;
        }
        Hero hero;
        for (Integer heroId : heroIdList) {
            hero = player.heros.get(heroId);
            if (null == hero) {
                throw new MwException(GameError.HERO_NOT_FOUND.getCode(), "AttackPos???????????????????????????, roleId:", roleId,
                        ", heroId:", heroId);
            }

            if (!player.isOnBattleHero(heroId) && !player.isOnAcqHero(heroId) && !player.isOnCommandoHero(heroId)) {
                throw new MwException(GameError.HERO_NOT_BATTLE.getCode(), "AttackPos??????????????????,?????????????????????????????? roleId:", roleId,
                        ", heroId:", heroId);
            }

            if (!hero.isIdle()) {
                throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "AttackPos????????????????????????, roleId:", roleId,
                        ", heroId:", heroId, ", state:", hero.getState());
            }

            if (hero.getCount() <= 0) {
                throw new MwException(GameError.HERO_NO_ARM.getCode(), "AttackPos?????????????????????, roleId:", roleId, ", heroId:",
                        heroId, ", count:", hero.getCount());
            }
            if (!isMinePos) {
                // ???????????????????????????,????????????????????????
                if (hero.isOnAcq()) {
                    throw new MwException(GameError.ACQ_HERO_NOT_ATTACK.getCode(), "AttackPos?????????????????????????????????, roleId:",
                            roleId);
                }
            } else {
                // ???????????????????????????, ?????????????????????
                if (hero.isCommando()) {
                    throw new MwException(GameError.COMMANDO_HERO_NOT_ATK.getCode(), "AttackPos?????????????????????????????????, roleId:",
                            roleId);
                }
            }

        }
        // ??????????????????????????????
        if (isMinePos) {
            if (heroIdList.size() != 1) {
                // ?????????????????????,????????????????????????
                throw new MwException(GameError.COLLECT_WORK_ONLYONE.getCode(), "AttackPos?????????????????????????????????, roleId:", roleId,
                        ", heroIdList.size:", heroIdList.size());
            }
            int stateAcqCount = 0; // ??????????????????????????????
            for (int heroId : player.heroAcq) {
                Hero h = player.heros.get(heroId);
                if (null != h && h.getState() == HeroConstant.HERO_STATE_COLLECT) {
                    stateAcqCount++;
                }
            }
            for (int heroId : player.heroBattle) {
                Hero h = player.heros.get(heroId);
                if (null != h && h.getState() == HeroConstant.HERO_STATE_COLLECT) {
                    stateAcqCount++;
                }
            }
            if (stateAcqCount >= 4) {
                throw new MwException(GameError.COLLECT_HERO_OVER_MAX.getCode(), "AttackPos????????????????????????????????????, roleId:",
                        roleId, ", stateAcqCount:", stateAcqCount);
            }
        }
    }

    /**
     * ???????????????????????????
     *
     * @param player
     * @param heroIdList
     * @throws MwException
     */
    public void checkFormHero(Player player, List<Integer> heroIdList) throws MwException {
        long roleId = player.roleId;
        if (CheckNull.isEmpty(heroIdList)) {
            throw new MwException(GameError.ATTACK_POS_NO_HERO.getCode(), "AttackPos???????????????, roleId:", roleId);
        }

        Hero hero;
        for (Integer heroId : heroIdList) {
            hero = player.heros.get(heroId);
            if (null == hero) {
                throw new MwException(GameError.HERO_NOT_FOUND.getCode(), "AttackPos???????????????????????????, roleId:", roleId,
                        ", heroId:", heroId);
            }

            if (!player.isOnBattleHero(heroId) && !player.isOnCommandoHero(heroId)) {
                throw new MwException(GameError.HERO_NOT_BATTLE.getCode(), "AttackPos??????????????????, roleId:", roleId,
                        ", heroId:", heroId);
            }

            if (!hero.isIdle()) {
                throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "AttackPos????????????????????????, roleId:", roleId,
                        ", heroId:", heroId, ", state:", hero.getState());
            }

            if (hero.getCount() <= 0) {
                throw new MwException(GameError.HERO_NO_ARM.getCode(), "AttackPos?????????????????????, roleId:", roleId, ", heroId:",
                        heroId, ", count:", hero.getCount());
            }
        }
    }


    /**
     * ???????????????????????????
     *
     * @param player
     * @param heroIdList
     * @throws MwException
     */
    public void checkFormHeroBerlin(Player player, List<Integer> heroIdList) throws MwException {
        long roleId = player.roleId;
        if (CheckNull.isEmpty(heroIdList)) {
            throw new MwException(GameError.ATTACK_POS_NO_HERO.getCode(), "AttackPos???????????????, roleId:", roleId);
        }

        Hero hero;
        for (Integer heroId : heroIdList) {
            hero = player.heros.get(heroId);
            if (null == hero) {
                throw new MwException(GameError.HERO_NOT_FOUND.getCode(), "AttackPos???????????????????????????, roleId:", roleId,
                        ", heroId:", heroId);
            }

            if (!player.isOnBattleHero(heroId)) {
                throw new MwException(GameError.HERO_NOT_BATTLE.getCode(), "AttackPos??????????????????, roleId:", roleId,
                        ", heroId:", heroId);
            }

            if (!hero.isIdle()) {
                throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "AttackPos????????????????????????, roleId:", roleId,
                        ", heroId:", heroId, ", state:", hero.getState());
            }

            if (hero.getCount() <= 0) {
                throw new MwException(GameError.HERO_NO_ARM.getCode(), "AttackPos?????????????????????, roleId:", roleId, ", heroId:",
                        heroId, ", count:", hero.getCount());
            }
            if (hero.isCommando()) {
                throw new MwException(GameError.COMMANDO_HERO_NOT_ATK.getCode(), "???????????????,???????????????????????????, roleId:", roleId,
                        ", heroId:", heroId);
            }
        }
    }

    /**
     * ???????????????????????????????????????????????? ?????????????????????=8*???|X???|+|Y???|???*???1-????????????_??????[%])*(1-????????????_??????[%])/(1+???????????????[%]??? ????????????
     *
     * @param player
     * @param pos
     * @return
     */
    public int marchTime(Player player, int pos, Object... params) {
        int distance = calcDistance(player.lord.getPos(), pos);

        int baseRatio = Constant.MARCH_TIME_RATIO;

        int time = distance * baseRatio;
        //********************??????????????????****************************
        // ????????????
        TechDataManager techDataManager = DataResource.ac.getBean(TechDataManager.class);
        double addRatio = techDataManager.getTechEffect4SingleVal(player, TechConstant.TYPE_6);
        if (!ObjectUtils.isEmpty(params) && (boolean) params[0]) {
            addRatio *= 2;
        }

        // ????????????
        double berlinJobEffect = BerlinWar.getBerlinBuffVal(player.roleId, BerlinWarConstant.BUFF_TYPE_MARCH_TIME);
        // ????????????:????????????
        SeasonTalentService seasonTalentService = DataResource.ac.getBean(SeasonTalentService.class);
        double seasonTalentEffect = seasonTalentService.getSeasonTalentEffectValue(player, SeasonConst.TALENT_EFFECT_301);
        //??????????????????????????????????????????
        if (ArmyConstant.GUARD_MARCH == Optional.ofNullable(player.getMarchType().get(pos)).orElse(0)) {
            seasonTalentEffect += seasonTalentService.getSeasonTalentEffectValue(worldDataManager.getPosData(pos), SeasonConst.TALENT_EFFECT_610);
            //??????????????????????????????
            player.getMarchType().remove(pos);
        }
        //********************??????????????????****************************
        int skinAdd = 0;
        Map<Integer, BaseDressUpEntity> castleSkinMap = dressUpDataManager.getDressUpByType(player, AwardType.CASTLE_SKIN);
        if (!CheckNull.isEmpty(castleSkinMap)) {
            List<StaticCastleSkin> staticCastleSkinList = castleSkinMap.values().stream().map(entity -> StaticLordDataMgr.getCastleSkinMapById(entity.getId())).filter(staticCastleSkin -> staticCastleSkin.getEffectType() == 4).collect(Collectors.toList());
            for (StaticCastleSkin o : staticCastleSkinList) {
                int star = player.getCastleSkinStarById(o.getId());
                StaticCastleSkinStar staticCastleSkinStar = StaticLordDataMgr.getCastleSkinStarById(o.getId() * 100 + star);
                skinAdd += staticCastleSkinStar.getEffectVal();
            }
        }

//        int addRatio4 = sCastleSkin != null ? sCastleSkin.getEffectVal() : 0;
        int addRatio4 = skinAdd;

        // ??????????????????
        int addRatio5 = 0;
        Map<Integer, BaseDressUpEntity> marchLineMap = dressUpDataManager.getDressUpByType(player, AwardType.MARCH_SPECIAL_EFFECTS);
        if (!CheckNull.isEmpty(marchLineMap)) {
            addRatio5 = marchLineMap
                    .keySet()
                    .stream()
                    .map(StaticLordDataMgr::getMarchLine)
                    .filter(Objects::nonNull)
                    .filter(conf -> conf.getEffectType() == StaticMarchLine.EFFECT_TYPE_WALK_SPEED)
                    .mapToInt(StaticMarchLine::getEffectVal)
                    .sum();
        }

        // buff??????
        Effect effect = player.getEffect().get(EffectConstant.WALK_SPEED);
        double addRatio1 = effect != null ? effect.getEffectVal() : 0;

        effect = player.getEffect().get(EffectConstant.WALK_SPEED_HIGHT);
        // ?????????
        double addRatio2 = effect != null ? effect.getEffectVal() : 0;

        effect = player.getEffect().get(EffectConstant.PREWAR_WALK_SPEED);
        // ????????????buff
        double addRatio3 = effect != null ? effect.getEffectVal() : 0;

        // ??????Buff
        double cityBuffer = worldDataManager.getCityBuffer(worldDataManager.checkCityBuffer(player.lord.getPos()),
                WorldConstant.CityBuffer.MARCH_BUFFER, player.roleId);

        // ?????????????????????=8*???|X???|+|Y???|???/???1+????????????_??????[%]+????????????[%]+??????????????????[%])*(1-????????????[%])*(1-???????????????[%])*(1-??????????????????[%]*(1-??????????????????[%]))
        try {
            time = (int) Math.ceil((time / (1 + (addRatio + berlinJobEffect + cityBuffer + seasonTalentEffect) / Constant.TEN_THROUSAND)
                    * (1 - addRatio1 / Constant.TEN_THROUSAND)
                    * (1 - addRatio2 / Constant.TEN_THROUSAND)
                    * (1 - addRatio3 / Constant.TEN_THROUSAND)
                    * (1 - addRatio4 / Constant.TEN_THROUSAND)
                    * (1 - addRatio5 / Constant.TEN_THROUSAND)));
        } catch (Exception e) {
            LogUtil.error("????????????????????????", e);
        }
        if (time < 1) {
            time = 1;
        }
        LogUtil.debug("roleId:", player.lord.getLordId(), ", ???????????? = ", distance, ", ???????????????=", addRatio, ", ?????????=", addRatio1, ", ?????????=", addRatio2, ", ????????????buff=",
                addRatio3, ", ????????????=", berlinJobEffect, ", ??????Buff=", cityBuffer, ", ????????????", addRatio4, ", ??????????????????: ", addRatio5, "???????????? = ", time);
        return time;
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????X???Y???????????????
     *
     * @param pos1
     * @param pos2
     * @return
     */
    public int calcDistance(int pos1, int pos2) {
        return MapHelper.calcDistance(pos1, pos2);
    }

    /**
     * ????????????
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public RetreatRs retreat(long roleId, RetreatRq req) throws MwException {
        // ????????????????????????
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int keyId = req.getKeyId();
        int type = req.getType();
        Army army = player.armys.get(keyId);
        if (null == army) {
            throw new MwException(GameError.RETREAT_ARMY_NOT_FOUND.getCode(), "????????????????????????????????????, roleId:", roleId,
                    ", keyId:", keyId);
        }
        // ?????????????????????????????????,??????????????????,???????????????????????????????????????
        if (MapHelper.getAreaIdByPos(army.getTarget()) != player.lord.getArea()) {
            March march = new March(player, army);
            worldDataManager.addMarch(march);
        }

        if (army.getState() == ArmyConstant.ARMY_STATE_MARCH) {
            // ??????????????????
            boolean isFree = false;
            int propId = 0;
            if (type == ArmyConstant.MOVE_BACK_TYPE_1) {
                propId = PropConstant.ITEM_ID_5021;
            } else if (type == ArmyConstant.MOVE_BACK_TYPE_2) {
                propId = PropConstant.ITEM_ID_5022;
            } else if (type == 3) {
                // vip????????????,????????????
                if (player.common.getRetreat() >= vipDataManager.getNum(player.lord.getVip(), VipConstant.RETREAT)) {
                    throw new MwException(GameError.SHOP_VIP_BUY_CNT.getCode(), "??????????????????vip????????????, roleId:" + roleId);
                }
                // ????????????
                type = ArmyConstant.MOVE_BACK_TYPE_1;
                isFree = true;
                player.common.setRetreat(player.common.getRetreat() + 1);
            } else {
                throw new MwException(GameError.NO_CD_TIME.getCode(), "????????????????????????, roleId:,", roleId, ", type:", type);
            }
            if (!isFree) {
                rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.PROP, propId, 1,
                        AwardFrom.MOVE_RETREAT);
            }
        }
//        else {
//            throw new MwException(GameError.RETREAT_ARMY_NOT_MARCH_STATE.getCode(),
//                    String.format("roleId :%d, armyId :%d target :%d, not in march", player.getLordId(), army.getKeyId(), army.getTarget()));
//        }

        RetreatRs.Builder builder = RetreatRs.newBuilder();
        // ??????????????????
        if (army.getType() == ArmyConstant.ARMY_TYPE_COLLECT_SUPERMINE) {// ??????????????????
            superMineService.retreatCollectArmy(player, army, type);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_ATK_SUPERMINE) {// ??????????????????
            superMineService.retreatAtkArmy(player, army, type);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_HELP_SUPERMINE) { // ??????????????????
            superMineService.retreatHelpArmy(player, army, type);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_BERLIN_WAR) { // ????????????????????????????????????
            berlinWarService.retreatBerlinArmy(player, army, type);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_BATTLE_FRONT_WAR) { // ?????????????????????????????????
            berlinWarService.retreatBattleFrontArmy(player, army, type);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_REBEL_BATTLE) { // ????????????
            rebelService.retreatRebelHelpArmy(player, army, type);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_COUNTER_BOSS_ATK_HELP) { // ???????????????BOSS?????????,
            // ??????
            counterAtkService.retreatBossAtkHelpArmy(player, army, type);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_DECISIVE_BATTLE) {// ????????????
            decisiveBattleService.retreatDecisiveArmy(roleId, player, type, army);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_ATTACK_AIRSHIP) {// ???????????????
            airshipService.retreatArmy(player, army, type);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_RELIC_BATTLE) {
            DataResource.getBean(RelicService.class).retreatArmy(player, army);
        } else {
            // ????????????????????????
            originalReturnArmyProcess(roleId, player, keyId, type, army);
        }
        // ??????????????????
        List<Integer> posList = MapHelper
                .getAreaStartPos(MapHelper.getLineAcorss(army.getTarget(), player.lord.getPos()));
        posList.add(army.getTarget());
        posList.add(player.lord.getPos());
        EventBus.getDefault()
                .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));
        return builder.build();
    }

    /**
     * ???????????????????????????
     *
     * @param roleId
     * @param player
     * @param keyId
     * @param type
     * @param army
     * @throws MwException
     */
    public void originalReturnArmyProcess(long roleId, Player player, int keyId, int type, Army army) {
        // ???????????????????????????????????????????????????????????????
        Integer battleId = army.getBattleId();
        LogUtil.debug("????????????,battleId=" + battleId + ",keyId=" + keyId + ",type=" + type);
        int now = TimeHelper.getCurrentSecond();

        int armyState = army.getState();
        //????????????????????????(?????????????????????????????????)???????????????
        if (army.getState() == ArmyConstant.ARMY_STATE_COLLECT) {// ??????????????????????????????
            retreatSettleCollect(army, type, player, now, roleId);
        }

        if (null != battleId && battleId > 0) {// ????????????
            Battle battle = warDataManager.getBattleMap().get(battleId);
            if (null != battle) {
                int camp = player.lord.getCamp();
                int armCount = army.getArmCount();
                LogUtil.debug(roleId + ",????????????=" + armCount);
                battle.updateArm(camp, -armCount);
                // ????????????
                retreatArmy(player, army, now, type); // ??????army??????
                if (battle.getType() == WorldConstant.BATTLE_TYPE_CITY) { // ??????
                    // ?????????
                    if (battle.getSponsor() != null && battle.getSponsor().roleId == roleId
                            && checkArmyOnPosLast(battle, army.getTarget())) {// ????????????????????????
                        // ?????????????????????????????????????????????????????????
                        // ????????????????????????????????????????????????????????????
                        warService.cancelCityBattle(army.getTarget(), true, battle, false);
                        // ????????????, ????????????????????? ???????????????
                        Player target = worldDataManager.getPosData(army.getTarget());
                        if (target != null && target.isLogin) {
                            syncAttackRole(target, player.lord, army.getEndTime(), WorldConstant.ATTACK_ROLE_0);
                        }
                    } else {
                        // ???????????????,??????battle?????????
                        removeBattleArmy(battle, roleId, keyId, battle.getAtkCamp() == camp);
                    }
                } else if (battle.getType() == WorldConstant.BATTLE_TYPE_MINE_GUARD) {
                    //??????????????????, ???????????????????????????????????????
                    warDataManager.removeBattleById(battleId);
                } else {// ?????????
                    removeBattleArmy(battle, roleId, keyId, battle.getAtkCamp() == camp);
                }

                // ??????????????????
                processMarchArmy(player.lord.getArea(), army, keyId);

                // player.battleMap.remove(battle.getPos());
                HashSet<Integer> battleIds = player.battleMap.get(battle.getPos());
                if (battleIds != null) {
                    battleIds.remove(battleId);
                }
            } else {// ???????????????????????????????????????
                //
                Battle specialBattle = warDataManager.getSpecialBattleMap().get(battleId);
                if (army.getType() == ArmyConstant.ARMY_TYPE_LIGHTNING_WAR // ???????????????????????????????????????BOSS
                        || army.getType() == ArmyConstant.ARMY_TYPE_COUNTER_BOSS_DEF
                        && !CheckNull.isNull(specialBattle)) {
                    int camp = player.lord.getCamp();
                    int armCount = army.getArmCount();
                    LogUtil.debug(roleId + ",????????????=" + armCount);
                    specialBattle.updateAtkBoss(camp, -armCount);
                    // ????????????
                    retreatArmy(player, army, now, type);
                    // ??????????????????????????????
                    removeBattleArmy(specialBattle, roleId, keyId, true);
                    // ??????????????????
                    processMarchArmy(player.lord.getArea(), army, keyId);

                    HashSet<Integer> battleIds = player.battleMap.get(specialBattle.getPos());
                    if (battleIds != null) {
                        battleIds.remove(battleId);
                    }
                } else {
                    LogUtil.debug("roleId:", roleId, ", ?????????????????????", army);
                    retreatArmy(player, army, now, type);
                }
            }
        } else {
            // ???????????????????????????
            // ????????????
            retreatArmy(player, army, now, type);
        }

        //??????????????????????????????????????????, ???????????????????????????
        if (armyState == ArmyConstant.ARMY_STATE_COLLECT) {
            //???????????????????????????
            cancelMineBattle(army.getTarget(), now, player);
        }

        synRetreatArmy(player, army, now); // ??????army??????
    }

    public void retreatSettleCollect(Army army, int type, Player player, int now, long roleId) {
        type = 0;
        int pos = army.getTarget();
        // ????????????
        int resource = calcCollect(player, army, now);

        List<Award> grab = army.getGrab();// ????????????
        StaticMine staticMine = worldDataManager.getMineByPos(pos);
        if (staticMine == null) {
            LogUtil.error("role:" + roleId + "??????????????????---????????????");
            LogLordHelper.commonLog("retreatArmy", AwardFrom.COMMON, player);
        } else {
            Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
            boolean effect = mineService.hasCollectEffect(player, staticMine.getMineType(),
                    new Date(army.getBeginTime() * 1000L), army);// ????????????
            int heroId = army.getHero().get(0).getV1();
            Hero hero = player.heros.get(heroId);
            int time = now - army.getBeginTime();
            army.setCollectTime(time);
            int addExp = (int) Math.ceil(time * 1.0 / Constant.MINUTE) * 20;// ??????????????????
            addExp = heroService.adaptHeroAddExp(player, addExp);
            // ??????????????????
            addExp = heroService.addHeroExp(hero, addExp, player.lord.getLevel(), player);

            CommonPb.MailCollect collect = PbHelper.createMailCollectPb(time, hero, addExp, grab, effect);

            // ??????????????????
            mailDataManager.sendCollectMail(player, null, MailConstant.MOLD_COLLECT_RETREAT, collect, now,
                    staticMine.getLv(), staticMine.getMineId(), xy.getA(), xy.getB());

            // ??????????????????
            mineService.updateMine(pos, resource);
            // ????????????????????????
            worldDataManager.removeMineGuard(pos);

            //????????????-????????????
            ActivityDiaoChanService.completeTask(player, ETask.COLLECT_RES, staticMine.getMineType(), grab.get(0).getCount());
            TaskService.processTask(player, ETask.COLLECT_RES, staticMine.getMineType(), grab.get(0).getCount());
        }
    }

    /**
     * ????????????????????????????????????
     *
     * @param pos
     * @param now
     * @param player
     */
    public void cancelMineBattle(int pos, int now, Player player) {
        //?????????????????????????????????
        Battle collectBattle;
        Player collectPlayer;
        MineData mineData = worldDataManager.getMineMap().get(pos);
        if (CheckNull.isNull(mineData))
            return;

        if (!ObjectUtils.isEmpty(mineData.getCollectTeam())) {
            for (Long collectorId : mineData.getCollectTeam()) {
                collectPlayer = playerDataManager.getPlayer(collectorId);
                if (CheckNull.isNull(collectPlayer) || ObjectUtils.isEmpty(collectPlayer.armys))
                    continue;

                for (Army cArmy : collectPlayer.armys.values()) {
                    if (cArmy.getTarget() != pos || now > cArmy.getEndTime() ||
                            cArmy.getType() != ArmyConstant.ARMY_TYPE_COLLECT || cArmy.getState() != ArmyConstant.ARMY_STATE_MARCH
                            || cArmy.getBattleId() == null || cArmy.getBattleId() <= 0)
                        continue;

                    collectBattle = warDataManager.getBattleMap().get(cArmy.getBattleId());
                    if (!CheckNull.isNull(collectBattle)) {
                        warDataManager.removeBattleById(collectBattle.getBattleId());
//                        if (player.isLogin)
//                            syncAttackRole(player, collectPlayer.lord, cArmy.getEndTime(),
//                                    WorldConstant.ATTACK_ROLE_0);
                    }
                }
            }
        }
    }

    /**
     * ????????????????????????????????????????????????????????????????????????
     *
     * @param battle
     * @param pos
     * @return true???????????????
     */
    public boolean checkArmyOnPosLast(Battle battle, int pos) {
        Player player = battle.getSponsor();
        for (Army army : player.armys.values().stream().filter(s -> s.getBattleId() != null && s.getBattleId() == battle.getBattleId()).collect(Collectors.toList())) {
            if (army.getTarget() == pos && !army.isRetreat()) {
                return false;
            }
        }
        return true;
    }

    /**
     * ????????????????????????
     *
     * @param area
     * @param army
     * @param keyId
     */
    public void processMarchArmy(int area, Army army, int keyId) {
        // ??????????????????
        Map<Integer, March> marchMap = worldDataManager.getMarchInArea(area);
        if (!CheckNull.isEmpty(marchMap)) {
            March march = marchMap.get(keyId);
            if (march == null) {
                return;
            }
            march.setArmy(army);
        }
    }

    public void removeBattleArmy(Battle battle, Long roleId, Integer keyId, boolean isAtk) {
        List<BattleRole> list = null;
        if (isAtk) {
            list = battle.getAtkList();
        } else {
            list = battle.getDefList();
        }
        if (list == null || list.isEmpty()) {
            return;
        }
        LogUtil.debug("????????????removeBattleArmy=" + list + ",roleId=" + roleId + ",keyId=" + keyId);
        Iterator<BattleRole> it = list.iterator();
        while (it.hasNext()) {
            BattleRole battleRole = it.next();
            if (battleRole.getKeyId() == keyId.intValue()) {
                LogUtil.debug("removeBattleArmy,armyMap=" + battleRole);
                it.remove();
            }
        }
        // ????????????id
        if (list.stream().noneMatch(br -> br.getRoleId() == roleId)) {// ???????????????battle??????????????????????????????
            if (isAtk) {
                battle.getAtkRoles().remove(roleId);
            } else {
                battle.getDefRoles().remove(roleId);
            }
            // ?????????????????????
            battle.getInvites().remove(roleId);
        }

    }

    // public void removeBattleArmy(Battle battle, Long roleId, Integer keyId,
    // boolean isAtk) {
    // List<Map<Long, Map<Integer, List<Integer>>>> list = null;
    // if (isAtk) {
    // list = battle.getAtks();
    // } else {
    // list = battle.getDefs();
    // }
    // if (list == null || list.isEmpty()) {
    // return;
    // }
    // LogUtil.debug("????????????removeBattleArmy=" + list + ",roleId=" + roleId +
    // ",keyId=" + keyId);
    // for (Map<Long, Map<Integer, List<Integer>>> roleKV : list) {
    // Map<Integer, List<Integer>> armyMap = roleKV.get(roleId);
    // LogUtil.debug("removeBattleArmy,armyMap=" + armyMap);
    // if (armyMap != null && !armyMap.isEmpty()) {
    // armyMap.remove(keyId);
    // }
    // }
    // }

    /**
     * ???????????????????????????
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GetBattleRs getBattle(long roleId, GetBattleRq req) throws MwException {
        int pos = req.getPos();
        int type = req.getType();// 0 ????????????, 1 ??????????????????
        GetBattleRs.Builder builder = GetBattleRs.newBuilder();
        builder.setPos(pos);
        if (type == 0) {
            LinkedList<Battle> battleList = warDataManager.getBattlePosMap().get(pos);
            if (!CheckNull.isEmpty(battleList)) {
                for (Battle battle : battleList) {
                    int defArm = getDefArmCntByBattle(battle);
                    builder.addBattle(PbHelper.createBattlePb(battle, defArm));
                }
            }
            if (worldDataManager.isPlayerPos(pos)) { // ???????????????battle
                Player p = worldDataManager.getPosData(pos);
                Integer battleId = warDataManager.getRebelBattleCacheMap().get(p.roleId);
                if (battleId != null) {
                    Battle battle = warDataManager.getBattleMap().get(battleId);
                    if (battle != null) {
                        int defArm = getDefArmCntByBattle(battle);
                        builder.addBattle(PbHelper.createBattlePb(battle, defArm));
                    }
                }
            }
        } else if (type == 1) {// ??????????????????
            SuperMine sm = worldDataManager.getSuperMineMap().get(pos);
            if (CheckNull.isNull(sm)) {
                throw new MwException(GameError.SUPER_MINE_NOT_EXIST.getCode(), "?????????????????????, roleId:", roleId, ", pos:", pos);
            }
            int defArm = sm.defArmyCnt();
            long defLordId = 0L;
            SuperGuard sg = sm.getCollectArmy().stream().filter(s -> s.getArmy().getLordId() == roleId).findFirst()
                    .orElse(null);
            defLordId = sg == null ? 0L : sg.getArmy().getLordId();
            if (sm != null) {
                List<Integer> battleIds = sm.getBattleIds();
                if (!CheckNull.isEmpty(battleIds)) {
                    for (int battleId : battleIds) {
                        Battle battle = warDataManager.getBattleMap().get(battleId);
                        if (battle != null) {

                            Integer cnt = battle.getHelpChatCnt().get(roleId);
                            int chatCnt = cnt == null ? 0 : cnt.intValue(); // ???????????????????????????
                            builder.addBattle(PbHelper.createBattlePb(battle, defArm, 0, chatCnt, defLordId));
                        }
                    }
                }
            }
        }
        return builder.build();
    }

    /**
     * ????????????id??????????????????
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GetBattleByIdRs getBattleById(long roleId, GetBattleByIdRq req) throws MwException {
        List<Integer> battleIdList = req.getBattleIdList();
        GetBattleByIdRs.Builder builder = GetBattleByIdRs.newBuilder();
        for (int battleId : battleIdList) {
            Battle battle = warDataManager.getBattleMap().get(battleId);
            if (battle == null) {
                battle = warDataManager.getSpecialBattleMap().get(battleId);
            }
            if (battle != null) {
                if (battle.getType() == WorldConstant.BATTLE_TYPE_SUPER_MINE) { // ??????????????????????????????
                    SuperMine sm = worldDataManager.getSuperMineMap().get(battle.getPos());
                    if (sm == null) continue;
                    int defArm = sm.defArmyCnt();
                    Integer cnt = battle.getHelpChatCnt().get(roleId);
                    int chatCnt = cnt == null ? 0 : cnt.intValue(); // ???????????????????????????
                    SuperGuard sg = sm.getCollectArmy().stream().filter(s -> s.getArmy().getLordId() == roleId)
                            .findFirst().orElse(null);
                    long defLordId = sg == null ? 0L : sg.getArmy().getLordId();
                    builder.addBattle(PbHelper.createBattlePb(battle, defArm, 0, chatCnt, defLordId));
                } else {
                    int defArm = getDefArmCntByBattle(battle);
                    builder.addBattle(PbHelper.createBattlePb(battle, defArm));
                }
            }
        }
        return builder.build();
    }

    /**
     * ???????????????????????????
     *
     * @param battle
     * @return
     */
    public int getDefArmCntByBattle(Battle battle) {
        int defArm = battle.getDefArm();
        if (battle.getType() == WorldConstant.BATTLE_TYPE_CITY || battle.isRebellionBattle()
                || (battle.isCounterAtkBattle() && battle.getBattleType() == WorldConstant.COUNTER_ATK_ATK
                || battle.isDecisiveBattle())) {
            int realDefArm = 0;
            // ????????????????????????????????????
            Player defencer = battle.getDefencer();
            try {
                playerDataManager.autoAddArmy(defencer);
                wallService.processAutoAddArmy(defencer);
            } catch (Exception e) {
                LogUtil.error("??????Battle????????????", e);
            }
            // ??????????????????,????????????
            List<Hero> heroList = defencer.getDefendHeros();
            for (Hero hero : heroList) {
                if (hero.getCount() > 0) {
                    realDefArm += hero.getCount();
                }
            }
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
                    realDefArm += maxArmy;
                }
            }
            // ?????????????????????????????????
            List<Army> list = worldDataManager.getPlayerGuard(defencer.lord.getPos());
            if (list != null && !list.isEmpty()) {
                Player tarPlayer = null;
                for (Army army : list) {
                    tarPlayer = playerDataManager.getPlayer(army.getLordId());
                    if (tarPlayer == null) {
                        continue;
                    }
                    //????????????,????????????????????????????????????????????????
                    if (tarPlayer.getCamp() != defencer.getCamp()) {
                        continue;
                    }
                    for (TwoInt twoInt : army.getHero()) {
                        realDefArm += twoInt.getV2();
                    }
                }
            }
            // ????????????????????????(????????????????????????)
            for (BattleRole br : battle.getDefList()) {
                Player player = playerDataManager.getPlayer(br.getRoleId());
                if (player != null) {
                    List<Integer> heroIdList = br.getHeroIdList();
                    if (!CheckNull.isEmpty(heroIdList)) {
                        for (Integer heroId : heroIdList) {
                            StaticHero sHero = StaticHeroDataMgr.getHeroMap().get(heroId);
                            if (null == sHero) {
                                continue;
                            }
                            Hero hero = player.heros.get(heroId);
                            if (null == hero) {
                                continue;
                            }
                            if (hero.getCount() <= 0) {
                                continue;
                            }
                            realDefArm += hero.getCount();
                        }
                    }
                }
            }
            // ???????????????????????????????????????
            realDefArm += getDefArmyCountByCityBattle(battle);
            defArm = realDefArm;
        }

        //????????????
//        if (battle.isMineGuardBattle()) {
//            Guard guard = worldDataManager.getGuardByPos(battle.getPos());
//            if (Objects.nonNull(guard)) {
//
//            }
//        }
        //??????????????????
        if (battle.isAtkSuperMine()) {
            SuperMine superMine = worldDataManager.getSuperMineMap().get(battle.getPos());
            List<Army> allArmy = new ArrayList<>();
            if (Objects.nonNull(superMine)) {
                List<Army> collect = superMine.getCollectArmy().stream().map(SuperGuard::getArmy).collect(Collectors.toList());
                allArmy.addAll(superMine.getHelpArmy()); // ???????????????
                allArmy.addAll(collect);
            }

            if (!ObjectUtils.isEmpty(allArmy)) {
                StaticHero staticHero;
                int realDefArm = 0;
                for (Army army : allArmy) {
                    Player player = playerDataManager.getPlayer(army.getLordId());
                    if (player == null) {
                        continue;
                    }

                    for (TwoInt twoInt : army.getHero()) {
                        staticHero = StaticHeroDataMgr.getHeroMap().get(twoInt.getV1());
                        if (null == staticHero) {
                            continue;
                        }
                        Hero hero = player.heros.get(twoInt.getV1());
                        if (hero == null) {
                            continue;
                        }

                        realDefArm += hero.getCount();
                    }
                }

                defArm = realDefArm;
            }
        }

        return defArm;
    }

    /**
     * ???????????????????????????????????????????????????
     *
     * @param battle
     * @return
     */
    private int getDefArmyCountByCityBattle(Battle battle) {
        int cnt = 0;
        int pos = battle.getPos();
        for (Long rId : battle.getDefRoles()) {
            Player defP = playerDataManager.getPlayer(rId);
            if (defP == null) {
                continue;
            }
            for (Army army : defP.armys.values()) {
                if (army.getTarget() == pos && army.getState() == ArmyConstant.ARMY_STATE_MARCH) {
                    for (TwoInt kv : army.getHero()) {
                        cnt += kv.getV2();
                    }
                }
            }
        }
        return cnt;
    }

    /**
     * ??????????????????????????????????????? ??? ????????????
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public JoinBattleRs joinBattle(long roleId, JoinBattleRq req) throws MwException {
        // ????????????????????????
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int camp = player.lord.getCamp();
        int battleId = req.getBattleId();
        Battle battle = warDataManager.getBattleMap().get(battleId);
        battle = CheckNull.isNull(battle) ? warDataManager.getSpecialBattleMap().get(battleId) : battle;
        if (null == battle) {
            throw new MwException(GameError.BATTLE_NOT_FOUND.getCode(), "?????????????????????, roleId:", roleId, ", battleId:",
                    battleId);
        }
        StaticArea targetSArea = StaticWorldDataMgr.getAreaMap().get(MapHelper.getAreaIdByPos(battle.getPos()));
        StaticArea mySArea = StaticWorldDataMgr.getAreaMap().get(player.lord.getArea());

        // ??????????????????
        checkArea(player, targetSArea, mySArea);

        if (battle.isCityBattle()) {
            if (player.getDecisiveInfo().isDecisive()) {
                throw new MwException(GameError.DECISIVE_BATTLE_ING.getCode(), "????????????????????????,?????????????????? , roleId:", roleId);
            }
            if (battle.getDefencer().roleId == roleId) {
                throw new MwException(GameError.HAS_JOIN_BATTLE.getCode(), "???????????????????????????????????????, roleId:", roleId,
                        ", battleId:", battleId);
            }
            // ?????????????????????????????????,?????????????????????
            if (battle.getBattleType() == WorldConstant.CITY_BATTLE_BLITZ && camp == battle.getAtkCamp()) {
                throw new MwException(GameError.QUICKLY_BATTLE_NOT_JOIN.getCode(), "???????????????????????????, roleId:", roleId,
                        ", battleId:", battleId);
            }
        } else if (battle.isRebellionBattle()) {// ????????????

        } else if (battle.isCounterAtkBattle()) {// ???????????????
            if (battle.getBattleType() == WorldConstant.COUNTER_ATK_DEF || camp != battle.getDefCamp()) {
                throw new MwException(GameError.COUNTER_CANT_JOIN_BATTLE.getCode(), "???????????????????????????, ??????????????????, roleId:",
                        roleId, ", battleId:", battleId);
            }
        } else {// ?????????
            if (WorldConstant.ATTACK_STATE_NEED_LV > player.lord.getLevel()) {
                throw new MwException(GameError.ATTACK_STATE_NEED_LV.getCode(), "?????????????????????45???????????????????????????, roleId:", roleId);
            }
            // StaticCity staticCity =
            // StaticWorldDataMgr.getCityByPos(battle.getPos());
            // if (staticCity.getArea() != player.lord.getArea()) {
            // throw new MwException(GameError.CAMP_BATTLE_AREA_ERROR.getCode(),
            // "??????????????????,???????????????, roleId:", roleId,
            // ", cityId:", staticCity.getCityId());
            // }
        }

        // ????????????????????????????????????
        // if (player.battleMap.containsKey(battle.getPos())) {
        // throw new MwException(GameError.HAS_JOIN_BATTLE.getCode(), "?????????????????????,
        // roleId:", roleId, ", battleId:",
        // battleId);
        // }

        if (battle.getAtkCamp() != camp && battle.getDefCamp() != camp) {
            throw new MwException(GameError.CAN_NOT_JOIN_BATTLE.getCode(), "???????????????????????????????????????, roleId:", roleId,
                    ", battleId:", battleId, ", roleCamp:", camp);
        }

        int pos = getJoinBattlePos(battle);
        // ????????????????????????
        List<Integer> heroIdList = req.getHeroIdList();
        checkFormHero(player, heroIdList);

        int armCount = 0;
        Hero hero;
        for (Integer heroId : heroIdList) {
            hero = player.heros.get(heroId);
            armCount += hero.getCount();
        }

        int now = TimeHelper.getCurrentSecond();
        int marchTime = marchTime(player, pos);

        // ??????NPC?????????????????????(???????????????????????????)
        StaticCity staticCity = null;
        if (battle.isCampBattle()) {
            staticCity = StaticWorldDataMgr.getCityByPos(pos);
            if (!CheckNull.isNull(staticCity)
                    && CheckNull.isNull(StaticBerlinWarDataMgr.getBerlinSettingById(staticCity.getCityId()))) {
                City city = worldDataManager.getCityById(staticCity.getCityId());
                if (city.getCamp() == Constant.Camp.NPC) {
                    marchTime = (int) (marchTime
                            * (1 - (WorldConstant.ATTACK_NPC_CITY_MARCH_NUM / Constant.TEN_THROUSAND)));
                    // ????????????????????????[%]
                    marchTime = airshipService.getAirShipMarchTime(player, marchTime);
                }
            }
        }

        // ???????????????????????????
        if (now + marchTime > battle.getBattleTime()) {
            throw new MwException(GameError.BATTLE_CD_TIME.getCode(), "????????????,???????????????, roleId:", roleId, ", pos:",
                    pos + ",????????????=" + (now + marchTime) + ",???????????????=" + battle.getBattleTime());
        }

        // ????????????
        int needFood = checkMarchFood(player, marchTime, armCount);
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.RESOURCE, AwardType.Resource.FOOD, needFood,
                AwardFrom.ATK_POS);

        List<TwoInt> form = new ArrayList<>();
        for (Integer heroId : heroIdList) {
            hero = player.heros.get(heroId);
            hero.setState(ArmyConstant.ARMY_STATE_MARCH);
            form.add(PbHelper.createTwoIntPb(heroId, hero.getCount()));
        }

        int type = ArmyConstant.ARMY_TYPE_ATK_CAMP;
        if (battle.isCityBattle()) {
            type = ArmyConstant.ARMY_TYPE_ATK_PLAYER;
        } else if (battle.isCampBattle()) {
            type = ArmyConstant.ARMY_TYPE_ATK_CAMP;
        } else if (battle.isRebellionBattle()) {// ??????????????????
            type = ArmyConstant.ARMY_TYPE_REBEL_BATTLE;
        } else if (battle.isCounterAtkBattle() && battle.getBattleType() == WorldConstant.COUNTER_ATK_ATK) { // ?????????????????????
            type = ArmyConstant.ARMY_TYPE_COUNTER_BOSS_ATK_HELP;
        }

        Army army = new Army(player.maxKey(), type, pos, ArmyConstant.ARMY_STATE_MARCH, form, marchTime,
                now + marchTime, player.getDressUp());
        army.setBattleId(battleId);
        army.setLordId(roleId);
        army.setTarLordId(battle.getDefencerId());
        army.setBattleTime(battle != null ? battle.getBattleTime() : 0);
        army.setOriginPos(player.lord.getPos());
        army.setHeroMedals(heroIdList.stream()
                .map(heroId -> medalDataManager.getHeroMedalByHeroIdAndIndex(player, heroId, MedalConst.HERO_MEDAL_INDEX_0))
                .filter(Objects::nonNull)
                .map(PbHelper::createMedalPb)
                .collect(Collectors.toList()));

        player.armys.put(army.getKeyId(), army);
        HashSet<Integer> set = player.battleMap.get(pos);
        if (set == null) {
            set = new HashSet<>();
            player.battleMap.put(pos, set);
        }
        set.add(battle.getBattleId());
        // player.battleMap.put(pos, battle.getBattleId());

        // ??????????????????
        March march = new March(player, army);
        worldDataManager.addMarch(march);

        // ????????? ?????????
        if (camp == battle.getAtkCamp()) {
            removeProTect(player, AwardFrom.JOIN_BATTLE_WAR, pos);
            battle.getAtkRoles().add(roleId);
        } else if (camp == battle.getDefCamp()) {
            battle.getDefRoles().add(roleId);
        }

        // ?????????????????????
        List<Integer> posList = MapHelper.getAreaStartPos(MapHelper.getLineAcorss(player.lord.getPos(), pos));
        posList.add(pos);
        posList.add(player.lord.getPos());
        EventBus.getDefault().post(
                new Events.AreaChangeNoticeEvent(posList, roleId, Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));
        // ????????????
        JoinBattleRs.Builder builder = JoinBattleRs.newBuilder();

        // ??????battle??????
        battle.updateArm(camp, armCount);

        builder.setArmy(PbHelper.createArmyPb(army, false));
        builder.setBattle(PbHelper.createBattlePb(battle));
        return builder.build();
    }

    private int getJoinBattlePos(Battle battle) {
        if (battle.isRebellionBattle()) {
            return battle.getDefencer().lord.getPos();
        } else {
            return battle.getPos();
        }
    }

    /**
     * ?????????????????????
     *
     * @param player
     * @param targetSArea
     * @param mySArea
     * @throws MwException
     */
    public void checkArea(Player player, StaticArea targetSArea, StaticArea mySArea) throws MwException {
        if (mySArea.getOpenOrder() == WorldConstant.AREA_ORDER_3) { // ?????????????????????,????????????????????????
        } else if (mySArea.getOpenOrder() == WorldConstant.AREA_ORDER_2) {// ?????????????????????,???????????????
            if (targetSArea.getOpenOrder() != WorldConstant.AREA_ORDER_2) {
                throw new MwException(GameError.CAMP_BATTLE_AREA_ERROR.getCode(), "????????????????????????, roleId:", player.roleId,
                        ", my area:", mySArea.getArea(), ", target area:", targetSArea.getArea());
            }
        } else if (mySArea.getOpenOrder() == WorldConstant.AREA_ORDER_1) { // ?????????????????????????????????
            if (targetSArea.getArea() != mySArea.getArea()) {
                throw new MwException(GameError.CAMP_BATTLE_AREA_ERROR.getCode(), "????????????????????????, roleId:", player.roleId,
                        ", my area:", mySArea.getArea(), ", target area:", targetSArea.getArea());
            }
        }
    }

    public int getNeedFood(int marchTime, int armCount) {
        return marchTime * WorldConstant.MOVE_COST_FOOD + (int) (Math.ceil(armCount * 1.0f / 10));
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????
     *
     * @param cityType
     * @return true ????????????
     */
    public boolean canAttackStateForWorldSchedule(int cityType) {
        StaticSchedule sSchedule = StaticWorldDataMgr.getScheduleById(worldScheduleService.getCurrentSchduleId());
        if (sSchedule == null) {
            return true;
        }
        if (CheckNull.isEmpty(sSchedule.getAttckCity())) {
            return true;
        }
        return sSchedule.getAttckCity().contains(cityType);
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public AttackStateRs attackState(long roleId, AttackStateRq req) throws MwException {
        int now = TimeHelper.getCurrentSecond();
        // ????????????????????????
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        if (WorldConstant.ATTACK_STATE_NEED_LV > player.lord.getLevel()) {
            throw new MwException(GameError.ATTACK_STATE_NEED_LV.getCode(), "?????????????????????45???????????????????????????, roleId:", roleId);
        }

        int cityId = req.getCityId();
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(cityId);
        if (null == staticCity) {
            throw new MwException(GameError.CITY_NOT_FOUND.getCode(), "?????????????????????????????????, roleId:", roleId, ", cityId:",
                    cityId);
        }
        // ????????????????????????
        if (!canAttackStateForWorldSchedule(staticCity.getType())) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "????????????????????? cityType:", staticCity.getType(),
                    ", currId");
        }

        // ??????????????????????????????????????????
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_LIGHTNING_WAR);
        if (!CheckNull.isNull(activity)) {
            ActivityBase actBase = StaticActivityDataMgr.getActivityByType(ActivityConst.ACT_LIGHTNING_WAR);
            if (!CheckNull.isNull(actBase)) {
                StaticLightningWar lightningWar = StaticLightningWarDataMgr.getLightningWar();
                Date endDate = DateHelper.afterStringTime(actBase.getBeginTime(), lightningWar.getEndTime());
                Date banDate = DateHelper.afterStringTime(actBase.getEndTime(), lightningWar.getBanTime());
                LightningWarBoss boss = worldDataManager.getLightningWarBossByArea(staticCity.getArea());
                if (!CheckNull.isNull(boss) && staticCity.getCityId() == boss.getId() && !boss.isNotInitOrDead()) {
                    if (DateHelper.isInTime(new Date(), banDate, endDate)) {
                        throw new MwException(GameError.IN_ACT_LIGHTNING_WAR_TIME.getCode(),
                                "?????????????????????,???????????????????????????, roleId:", roleId);
                    }
                }
            }
        }

        // StaticArea staticArea =
        // StaticWorldDataMgr.getAreaMap().get(staticCity.getArea());

        LinkedList<Battle> battleList = warDataManager.getBattlePosMap().get(staticCity.getCityPos());
        int camp = player.lord.getCamp();
        if (!CheckNull.isEmpty(battleList)) {
            for (Battle battle : battleList) {
                if (battle.getAtkCamp() == camp) {
                    throw new MwException(GameError.CAMP_BATTLE_HAS_EXISTS.getCode(), "?????????????????????????????????????????????????????????, roleId:",
                            roleId, ", cityId:", cityId);
                }
            }
        }

        // ????????????????????????
        // if (staticCity.getArea() == WorldConstant.AREA_TYPE_13 &&
        // staticCity.getArea() != player.lord.getArea()) {
        // throw new MwException(GameError.CAMP_BATTLE_AREA_ERROR.getCode(),
        // "??????????????????,???????????????, roleId:", roleId,
        // ", cityId:", cityId);
        // }

        // ?????????????????????
        // if (staticArea.getOpenOrder() == 1 && staticArea.getOpenOrder() !=
        // StaticWorldDataMgr.getAreaMap()
        // .get(player.lord.getArea()).getOpenOrder()) {
        // throw new MwException(GameError.CAMP_BATTLE_AREA_ERROR.getCode(),
        // "??????????????????,???????????????, roleId:", roleId,
        // ", cityId:", cityId);
        // }

        City city = worldDataManager.getCityById(cityId);

        if (city.getCamp() > 0 && camp == city.getCamp()) {
            throw new MwException(GameError.SAME_CAMP.getCode(), "??????????????????,???????????????, roleId:", roleId, ", cityId:",
                    staticCity.getCityId());
        }
        if (city.getProtectTime() > now) {
            throw new MwException(GameError.CITY_PROTECT.getCode(), "???????????????, roleId:", roleId, ", cityId:", cityId);
        }

        int job = player.lord.getJob();
        // ???????????????????????????
        if (staticCity.getType() == WorldConstant.CITY_TYPE_HOME) {
            // ?????????????????????????????????
            if (job != PartyConstant.Job.KING) {
                throw new MwException(GameError.NO_PRIVILEGE_ABOUT_KING.getCode(), "??????????????????????????????,??????????????????, roleId:", roleId,
                        ", cityId:", cityId);
            }
            // ????????????????????????
            if (city.getCamp() > 0) {
                throw new MwException(GameError.CAMP_BATTLE_AREA_ERROR.getCode(), "??????????????????, roleId:", roleId,
                        ", cityId:", cityId);
            }

            // ???????????????????????????????????????
            if (worldDataManager.checkHasHome(camp) != null) {
                throw new MwException(GameError.CAMP_BATTLE_AREA_ERROR.getCode(), "????????????,?????????????????? roleId:", roleId,
                        ", cityId:", cityId);
            }
            // ???????????????????????????????????????, ?????????????????????????????????????????????
            if (checkCityTypeHomeHasBattle(player)) {
                throw new MwException(GameError.TYPE_HOME_CITY_BATTLE_ONLYONE.getCode(), " ??????????????????????????????????????? roleId:",
                        roleId, ", cityId:", cityId);
            }
        }
        // ??????????????????
        if (staticCity.getArea() != player.lord.getArea()) {
            // ???????????? ?????? ????????? ??????????????????
            if (job == PartyConstant.Job.KING || job == PartyConstant.Job.COMMISSAR || job == PartyConstant.Job.CHIEF) {
            } else {
                throw new MwException(GameError.CAMP_BATTLE_AREA_ERROR.getCode(), "??????????????????,???????????????, roleId:", roleId,
                        ", cityId:", staticCity.getCityId());
            }
        }

        // ???????????????????????????,???????????????
        if (staticCity.getType() == WorldConstant.CITY_TYPE_8) {
            if (city.getCamp() == Constant.Camp.NPC && !checkAttkableType8NpcCity(camp)) {
                // ???????????????????????????????????????7???,????????????7??????????????????
                throw new MwException(GameError.CITY_TYPE_8_NOT_ATTACK.getCode(), "????????????????????????????????????????????????????????? roleId:",
                        roleId);
            }
        }

        if (CheckNull.isEmpty(battleList)) {
            city.setAttackCamp(player.lord.getCamp());// ??????????????????????????????
            city.setStatus(WorldConstant.CITY_STATUS_BATTLE);
        }

        Battle battle = new Battle();
        battle.setType(WorldConstant.BATTLE_TYPE_CAMP);
        battle.setBattleTime(now + staticCity.getCountdown());
        battle.setBeginTime(now);
        battle.setPos(staticCity.getCityPos());
        battle.setSponsor(player);
        battle.setDefCamp(city.getCamp());
        battle.addDefArm(city.getCurArm());
        battle.getAtkRoles().add(roleId);
        battle.setAtkCamp(camp);
        if (city.getCamp() != Constant.Camp.NPC) {
            if (city.getOwnerId() > 0) {
                Player defencer = playerDataManager.getPlayer(city.getOwnerId());
                battle.setDefencer(defencer);
                pushAttackCamp(defencer.account, staticCity.getArea(), staticCity.getCityId(), defencer.lord.getNick());
            }

            // ????????????????????????????????????????????????
            chatDataManager.sendSysChat(ChatConst.CHAT_CITY_DEF, city.getCamp(), 0, camp, player.lord.getNick(),
                    cityId);
        } else {
            battle.setAtkNpc(true);
        }

        // ??????????????????????????????
        chatDataManager.sendSysChat(ChatConst.CHAT_CITY_ATK, camp, 0, player.lord.getNick(), city.getCamp(), cityId);

        // ??????????????????
        warDataManager.addBattle(player, battle);

        // ?????????????????????
        syncAttackCamp(battle, cityId);

        AttackStateRs.Builder builder = AttackStateRs.newBuilder();
        List<Integer> posList = new ArrayList<>();
        posList.add(staticCity.getCityPos());
        EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_TYPE));
        return builder.build();
    }

    /**
     * ?????????????????????????????????????????????????????????
     *
     * @param camp
     * @return true ????????????
     */
    public boolean checkAttkableType8NpcCity(int camp) {
        int battleCnt = 0;// ????????????????????????????????????+???????????????????????????
        List<StaticCity> sSityList = StaticWorldDataMgr.getCityByArea(WorldConstant.AREA_TYPE_13);
        if (sSityList != null) {
            for (StaticCity city : sSityList) {
                if (city.getType() == WorldConstant.CITY_TYPE_8) {
                    List<Battle> battleList = warDataManager.getBattlePosMap().get(city.getCityPos());
                    if (!CheckNull.isEmpty(battleList)) {
                        for (Battle battle : battleList) {
                            if (battle.getAtkCamp() == camp) {
                                battleCnt++;
                            }
                        }
                    }
                }
            }
        }
        int hasCityCnt = worldDataManager.getPeoPle4MiddleCity(camp);// ?????????????????????
        LogUtil.debug("???????????????????????????????????????????????????  battleCnt:", battleCnt, ", hasCityCnt:", hasCityCnt);
        // ?????????????????????+????????????????????????????????????+??????????????????????????? ??????????????????7??????????????????????????????
        return (battleCnt + hasCityCnt) < WorldConstant.CITY_TYPE_8_MAX;

    }

    /**
     * ??????????????????????????????????????????
     *
     * @param player
     * @return
     */
    private boolean checkCityTypeHomeHasBattle(Player player) {
        int homePos1 = StaticWorldDataMgr.getCityMap().get(WorldConstant.HOME_CITY_1).getCityPos();
        int homePos2 = StaticWorldDataMgr.getCityMap().get(WorldConstant.HOME_CITY_2).getCityPos();
        int homePos3 = StaticWorldDataMgr.getCityMap().get(WorldConstant.HOME_CITY_3).getCityPos();
        int homePos4 = StaticWorldDataMgr.getCityMap().get(WorldConstant.HOME_CITY_4).getCityPos();
        return warDataManager.checkPlayerSponsorBatter(homePos1, player)
                || warDataManager.checkPlayerSponsorBatter(homePos2, player)
                || warDataManager.checkPlayerSponsorBatter(homePos3, player)
                || warDataManager.checkPlayerSponsorBatter(homePos4, player);

    }

    /**
     * NPC???????????????
     *
     * @param atkCity ???????????????NPC
     * @param defCity ???????????????
     */
    public void processAtk(City atkCity, City defCity) {
        int camp = atkCity.getCamp();
        int cityId = defCity.getCityId();
        LinkedList<Battle> battleList = warDataManager.getBattlePosMap()
                .get(StaticWorldDataMgr.getCityMap().get(cityId).getCityPos());

        // ????????????????????????????????????????????????????????????
        if (!CheckNull.isEmpty(battleList)) {
            for (Battle battle : battleList) {
                if (battle.getAtkCamp() == camp) {
                    LogUtil.debug("??????????????????????????????????????????????????????cityId=" + atkCity.getCityId());
                    return;
                }
            }
        }
        LogUtil.debug("???????????? ??????NPC??????????????????atk=" + atkCity + ",defCity=" + defCity);
        // ?????????????????????????????????
        defCity.setAttackCamp(atkCity.getCamp());// ??????????????????????????????
        defCity.setStatus(WorldConstant.CITY_STATUS_BATTLE);
        // String nick = "";
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(cityId);
        Battle battle = new Battle();
        int now = TimeHelper.getCurrentSecond();
        battle.setType(WorldConstant.BATTLE_TYPE_CAMP);
        battle.setBattleTime(now + staticCity.getCountdown());
        battle.setBeginTime(now);
        battle.setPos(staticCity.getCityPos());
        battle.setDefCamp(defCity.getCamp());
        battle.setAtkCity(atkCity.getCityId());
        battle.setAtkPos(StaticWorldDataMgr.getCityMap().get(atkCity.getCityId()).getCityPos());
        battle.setAtkCamp(atkCity.getCamp());

        // ????????????NPC??????
        int devLv = atkCity.getCityLv() > 0 ? atkCity.getCityLv() : 1;
        StaticCityDev cityDev = StaticWorldDataMgr.getCityDev(devLv);
        int armTotal = 0;
        StaticNpc npc;
        for (Integer npcId : cityDev.getForm()) {
            npc = StaticNpcDataMgr.getNpcMap().get(npcId);
            armTotal += npc.getTotalArm();
        }

        battle.updateArm(camp, armTotal);
        battle.updateArm(defCity.getCamp(), defCity.getCurArm());
        LogUtil.debug("=====????????????????????????????????????:", battle.getAtkArm() + ",arm=" + armTotal);
        LogUtil.debug("=====????????????????????????????????????:", battle.getDefArm() + ",arm=" + defCity.getCurArm());
        if (defCity.getCamp() != Constant.Camp.NPC) {
            if (defCity.getOwnerId() > 0) {
                battle.setDefencer(playerDataManager.getPlayer(defCity.getOwnerId()));
            }

            // ????????????????????????????????????????????????
            chatDataManager.sendSysChat(ChatConst.CHAT_HOME_CITY_DEF, defCity.getCamp(), 0, camp, cityId);
        } else {
            battle.setAtkNpc(true);
        }

        // ??????????????????????????????
        chatDataManager.sendSysChat(ChatConst.CHAT_HOME_CITY_ACT, camp, 0, defCity.getCamp(), cityId);

        // ??????????????????
        warDataManager.addBattle(null, battle);

        // ?????????????????????
        syncAttackCamp(battle, cityId);
    }

    /**
     * ??????????????????????????????
     *
     * @param battle
     * @param cityId
     */
    public void syncAttackCamp(Battle battle, int cityId) {
        int atkCamp = battle.getAtkCamp();
        int defCamp = battle.getDefCamp();
        SyncStateBattleRs.Builder builder = SyncStateBattleRs.newBuilder();
        builder.setBattle(PbHelper.createCampBattlePb(battle, cityId));
        Base.Builder msg = PbHelper.createSynBase(SyncStateBattleRs.EXT_FIELD_NUMBER, SyncStateBattleRs.ext,
                builder.build());

        int areaId = MapHelper.getAreaIdByPos(battle.getPos());
        StaticArea staticArea = StaticWorldDataMgr.getAreaMap().get(areaId);
        // ?????????????????????????????????????????????????????????????????????
        List<Integer> areaIdList = staticArea.getUnlockArea();

        ConcurrentHashMap<Long, Player> playerMap = playerDataManager.getPlayerByAreaList(areaIdList);
        for (Player player : playerMap.values()) {
            if (player.isLogin && (player.lord.getCamp() == atkCamp || player.lord.getCamp() == defCamp)) {
                MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
            }
        }
    }

    /**
     * ???????????????
     *
     * @param atk
     */
    public void removeProTect(Player atk, AwardFrom from, int pos) {
        Effect effect = atk.getEffect().get(EffectConstant.PROTECT);
        if (effect == null || effect.getEndTime() < TimeHelper.getCurrentSecond()) {
            return;
        }
        effect.setEndTime(TimeHelper.getCurrentSecond() - 1);
        atk.rmEffect(EffectConstant.PROTECT);
        LogUtil.debug(atk.roleId + ",????????????" + effect + ",????????????" + atk.getEffect().get(EffectConstant.PROTECT));
        LogLordHelper.logRemoveProtect(atk, from, pos);
        syncProTectRs(atk);
    }

    /**
     * ?????????????????????
     *
     * @param atk
     */
    private void syncProTectRs(Player atk) {
        SyncProTectRs.Builder builder = SyncProTectRs.newBuilder();
        builder.setRoleId(atk.roleId);
        builder.setPos(atk.lord.getPos());
        builder.setProt(0);
        Base.Builder msg = PbHelper.createSynBase(SyncProTectRs.EXT_FIELD_NUMBER, SyncProTectRs.ext, builder.build());
        MsgDataManager.getIns().add(new Msg(atk.ctx, msg.build(), atk.roleId));
    }

    /**
     * ?????????????????????
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetCampBattleRs getCampBattle(long roleId) throws MwException {
        // ????????????????????????
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int camp = player.lord.getCamp();
        int areaId = player.lord.getArea();
        LogUtil.debug(roleId + ",?????????????????????camp=" + camp + ",areaId=" + areaId);
        GetCampBattleRs.Builder builder = GetCampBattleRs.newBuilder();
        StaticArea staticArea = StaticWorldDataMgr.getAreaMap().get(areaId);
        if (null != staticArea) {
            List<StaticCity> cityList;
            LinkedList<Battle> battleList;
            List<Integer> areaIdList = staticArea.getUnlockArea();// ???????????????????????????
            for (Integer area : areaIdList) {
                cityList = StaticWorldDataMgr.getCityByArea(area);
                if (cityList != null) {
                    for (StaticCity city : cityList) {
                        battleList = warDataManager.getBattlePosMap().get(city.getCityPos());
                        if (!CheckNull.isEmpty(battleList)) {
                            for (Battle battle : battleList) {
                                if (battle.getAtkCamp() == camp || battle.getDefCamp() == camp) {
                                    builder.addBattle(PbHelper.createCampBattlePb(battle, city.getCityId()));
                                }
                            }
                        }
                    }
                }
            }
        }

        return builder.build();
    }

    /**
     * ??????????????????
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetCampCityLevyRs getCampLevy(Long roleId) throws MwException {
        // ????????????????????????
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int camp = player.lord.getCamp();
        int areaId = player.lord.getArea();
        LogUtil.debug(roleId + ",?????????????????????camp=" + camp + ",areaId=" + areaId);
        GetCampCityLevyRs.Builder builder = GetCampCityLevyRs.newBuilder();
        // ????????????
        List<StaticCity> cityList = StaticWorldDataMgr.getCityByArea(areaId);
        if (CheckNull.isEmpty(cityList)) {
            LogUtil.error("?????????????????????, area:", areaId);
        } else {
            City city;
            for (StaticCity staticCity : cityList) {
                city = worldDataManager.getCityById(staticCity.getCityId());
                if (null == city) {
                    LogUtil.error("??????????????????????????????????????????!!!cityId:", staticCity.getCityId());
                    continue;
                }
                // ???????????????????????????
                if (city.getCamp() != player.lord.getCamp()) {
                    continue;
                }
                Player cityOwner = playerDataManager.getPlayer(city.getOwnerId());
                builder.addCity(PbHelper.createAreaCityPb(player, city, cityOwner));

            }
        }

        return builder.build();
    }

    /**
     * ?????????????????????????????????????????????
     *
     * @param fromAreaId ????????????????????????
     * @param toAreaId   ????????????id
     */
    public void mergeArea(int fromAreaId, int toAreaId) {
        StaticArea sFromArea = StaticWorldDataMgr.getAreaMap().get(fromAreaId);
        if (sFromArea == null || !sFromArea.isOpen()) {
            return;
        }
        StaticArea sToArea = StaticWorldDataMgr.getAreaMap().get(toAreaId);
        if (sToArea == null || !sToArea.isOpen()) {
            return;
        }
        Collection<Player> players = playerDataManager.getPlayerByArea(fromAreaId).values();
        if (!CheckNull.isEmpty(players)) {
            List<Player> fromAreaPlayer = new ArrayList<>();
            fromAreaPlayer.addAll(players);
            List<Integer> notifyPos = new ArrayList<>();
            for (Player player : fromAreaPlayer) {
                if (player.ctx != null) {
                    player.ctx.close();// ?????????
                    player.logOut();// ??????
                }
                int prePos = player.lord.getPos();
                int newPos = moveCityByGmAfterProcess(player, toAreaId);
                notifyPos.add(prePos);
                notifyPos.add(newPos);
            }
            // ????????????
            EventBus.getDefault()
                    .post(new Events.AreaChangeNoticeEvent(notifyPos, Events.AreaChangeNoticeEvent.MAP_AND_AREA_TYPE));
        }
    }

    /**
     * ?????????????????????
     *
     * @param pos
     */
    public void clearPos(int pos) {
        if (worldDataManager.isPlayerPos(pos)) { // ???????????????????????????
            Player player = worldDataManager.getPosData(pos);
            moveCityByGmAfterProcess(player, player.lord.getArea());
            LogUtil.start("???????????????????????????: ???????????? roleId:" + player.roleId + ", pos:" + pos);
        } else if (worldDataManager.isBanditPos(pos)) { // ????????????
            worldDataManager.removeBandit(pos, 0);
            LogUtil.start("???????????????????????????: ????????????:" + " pos:" + pos);
        } else if (worldDataManager.isCabinetLeadPos(pos)) {
            CabinetLead lead = worldDataManager.getCabinetLeadByPos(pos);
            worldDataManager.removeBandit(pos, 1); // ????????????????????????
            // ??????????????????????????????
            Player targetPlayer = playerDataManager.getPlayer(lead.getRoleId());
            if (targetPlayer != null && targetPlayer.cabinet != null) {
                Cabinet cabinet = targetPlayer.cabinet;
                cabinet.setLeadStep(cabinet.getLeadStep() + 1);
            }
        } else if (worldDataManager.isGestapoPos(pos)) {
            worldDataManager.removeBandit(pos, 2);
            LogUtil.start("???????????????????????????: ??????????????????:" + " pos:" + pos);
        } else if (worldDataManager.isMinePos(pos)) {
            Guard guard = worldDataManager.getGuardByPos(pos);
            if (guard != null) Optional.ofNullable(guard).flatMap(g -> Optional.ofNullable(g.getArmy()))
                    .flatMap(army -> Optional.ofNullable(playerDataManager.getPlayer(army.getLordId())))
                    .ifPresent(p -> {
                        retreatArmy(p, guard.getArmy(), TimeHelper.getCurrentSecond(), ArmyConstant.MOVE_BACK_TYPE_2);
                        LogUtil.start("???????????????????????????: ????????????:" + " pos:" + pos + ", roleId:" + p.roleId + " army:"
                                + guard.getArmy());
                    });

            worldDataManager.removeMine(pos);
            LogUtil.start("???????????????????????????: ????????????:" + " pos:" + pos);
        }
    }

    /**
     * ?????????????????????????????????????????????
     */
    public void checkBerlinBuilingHasOther() {
        Set<Integer> posSet = StaticWorldDataMgr.getBerlinWarMap().values().stream()
                .filter(bl -> bl.getType() == StaticBerlinWarDataMgr.BATTLEFRONT_TYPE)
                .flatMap(bl -> bl.getPosList().stream()).collect(Collectors.toSet());
        for (int pos : posSet) {
            clearPos(pos);
        }
        // ???????????????????????????,????????????
        posSet = StaticWorldDataMgr.getBerlinWarMap().values().stream()
                .filter(bl -> bl.getType() == StaticBerlinWarDataMgr.BERLIN_TYPE)
                .flatMap(bl -> bl.getPosList().stream()).collect(Collectors.toSet());
        List<Award> awards = new ArrayList<>();
        awards.addAll(PbHelper.createAwardsPb(WorldConstant.BERLIN_COMPENSATION_AWARD));
        int now = TimeHelper.getCurrentSecond();
        for (Integer pos : posSet) {
            if (worldDataManager.isPlayerPos(pos)) { // ???????????????????????????
                Player player = worldDataManager.getPosData(pos);
                moveCityByGmAfterProcess(player, WorldConstant.AREA_TYPE_13);
                if (!CheckNull.isEmpty(awards)) {
                    // ?????????????????????
                    mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_BERLIN_COMPENSATION_AWARD,
                            AwardFrom.BERLIN_COMPENSATION_AWARD, now);
                    LogUtil.start("???????????????????????????: ???????????? roleId:" + player.roleId + ", pos:" + pos);
                }
            }
        }
    }

    /**
     * ???????????????GM??????,?????????????????????????????????????????????
     *
     * @param player
     * @param areaId
     * @return ??????
     */
    private int moveCityByGmAfterProcess(Player player, int areaId) {
        int prePos = player.lord.getPos();
        // ????????????????????????????????????????????????????????????
        if (player.battleMap.containsKey(prePos)) {// ????????????????????????????????????
            warService.cancelCityBattle(prePos, false);
        }

        // ?????????????????????????????????
        List<Army> guradArays = worldDataManager.getPlayerGuard(prePos);
        int now = TimeHelper.getCurrentSecond();
        if (!CheckNull.isEmpty(guradArays)) {
            for (Army army : guradArays) {
                Player tPlayer = playerDataManager.getPlayer(army.getLordId());
                if (tPlayer == null || tPlayer.armys.get(army.getKeyId()) == null) {
                    LogUtil.error("lordId: {}, army keyId: {}, pos: {} not found!!! guard army will remove",
                            army.getLordId(), army.getKeyId(), army.getTarget());
                    continue;
                }
                retreatArmyByDistance(tPlayer, army, now);
                synRetreatArmy(tPlayer, army, now);
                worldDataManager.removePlayerGuard(army.getTarget(), army);
                int heroId = army.getHero().get(0).getV1();
                mailDataManager.sendNormalMail(tPlayer, MailConstant.MOLD_GARRISON_RETREAT, now, player.lord.getNick(),
                        heroId, player.lord.getNick(), heroId);
            }
            //????????????????????????
            if (CheckNull.nonEmpty(guradArays)) {
                LogUtil.error("clear roleId: {}, pos: {} all error guard army", player.roleId, prePos);
                worldDataManager.removePlayerGuard(prePos);
            }
        }
        long roleId = player.roleId;
        int newPos = worldDataManager.randomEmptyPosInArea(areaId);
        // ??????????????????
        if (playerDataManager.getPlayerByArea(player.lord.getArea()) != null) {
            playerDataManager.getPlayerByArea(player.lord.getArea()).remove(roleId);
        }
        player.lord.setPos(newPos);
        player.lord.setArea(MapHelper.getAreaIdByPos(newPos));
        worldDataManager.removePlayerPos(prePos, player);
        worldDataManager.putPlayer(player);
        return newPos;
    }

    /**
     * GM??????????????????,????????????????????????
     *
     * @param player
     * @param areaId
     */

    public void moveCityByGm(Player player, int areaId) {
        if (player == null) {
            return;
        }
        int prePos = player.lord.getPos();
        if (prePos < 0) {// ???????????????????????????????????????
            return;
        }
        // if (!CheckNull.isEmpty(player.armys)) {// ???????????????????????????
        // return;
        // }
        StaticArea sArea = StaticWorldDataMgr.getAreaMap().get(areaId);
        if (sArea == null || !sArea.isOpen()) {
            return;
        }
        int newPos = moveCityByGmAfterProcess(player, areaId);
        if (player.ctx != null) {
            player.ctx.close();// ?????????
            player.logOut();// ??????
        }
        // ??????????????????????????????
        List<Integer> posList = new ArrayList<>();
        posList.add(prePos);
        posList.add(newPos);
        EventBus.getDefault()
                .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_AREA_TYPE));
    }

    /**
     * ?????????????????????boss
     *
     * @param player
     * @param newArea
     * @throws MwException
     */
    private void checkMoveCityByBoss(Player player, int newArea) throws MwException {
        long roleId = player.roleId;

        // // ??????boss2????????????,??????????????????,????????????????????????
        // CommonPb.WorldTask worldTask =
        // worldDataManager.getWorldTask().getWorldTaskMap()
        // .get(TaskType.WORLD_BOSS_TASK_ID_2);
        // if (worldTask == null || worldTask.getHp() > 0) {
        // worldTask =
        // worldDataManager.getWorldTask().getWorldTaskMap().get(TaskType.WORLD_BOSS_TASK_ID_1);
        // // ??????boss1??????????????????????????????????????????
        // if (worldTask == null || worldTask.getHp() > 0) {
        // if (newArea != player.lord.getArea()) {
        // throw new MwException(GameError.AREA_NOT_OPEN.getCode(), "??????,??????????????????,
        // roleId:,", roleId, ",area=",
        // player.lord.getArea(), ",newArea=", newArea, ",worldTask=",
        // worldTask);
        // }
        // } else {
        // StaticArea staticArea =
        // StaticWorldDataMgr.getAreaMap().get(player.lord.getArea());
        // if (!staticArea.getOpenAreaId().contains(newArea) && newArea !=
        // player.lord.getArea()) {
        // throw new MwException(GameError.AREA_NOT_OPEN.getCode(), "??????,??????????????????,
        // roleId:,", roleId, ",area=",
        // player.lord.getArea(), ",newArea=", newArea, ",worldTask=",
        // worldTask);
        // }
        // }
        // }

        // ?????????????????????
        int currentSchduleId = worldScheduleService.getCurrentSchduleId();
        int bossDeadState = worldScheduleService.getBossDeadState();
        if (bossDeadState == ScheduleConstant.BOSS_NO_DEAD) {
            if (newArea != player.lord.getArea()) {// ??????boss1??????????????????????????????????????????
                throw new MwException(GameError.AREA_NOT_OPEN.getCode(), "??????,??????????????????, roleId:,", roleId, ",area=",
                        player.lord.getArea(), ",newArea=", newArea, ",currentSchduleId=", currentSchduleId);
            }
        } else if (bossDeadState == ScheduleConstant.BOSS_1_DEAD) { // ?????????1?????????boss
            StaticArea staticArea = StaticWorldDataMgr.getAreaMap().get(player.lord.getArea());
            if (!staticArea.getOpenAreaId().contains(newArea) && newArea != player.lord.getArea()) {
                throw new MwException(GameError.AREA_NOT_OPEN.getCode(), "??????,??????????????????, roleId:,", roleId, ",area=",
                        player.lord.getArea(), ",newArea=", newArea, ",currentSchduleId=", currentSchduleId);
            }
        } else if (bossDeadState == ScheduleConstant.BOSS_2_DEAD) { // ???????????????boss?????????????????????

        }

        // ?????????????????????
        if (newArea == WorldConstant.AREA_TYPE_13
                && !StaticFunctionDataMgr.funcitonIsOpen(player, BuildingType.ENTER_AREA_13_COND)) {
            throw new MwException(GameError.AREA_NOT_OPEN.getCode(), "?????? ???????????????????????????, roleId:,", roleId);
        }
    }

    /**
     * ????????????boss??????
     *
     * @return 0 ???1???boss?????????, 1 ???1???boss??????, 2 ???2???boss??????
     */

    public int bossDeadState() {
        // CommonPb.WorldTask boss1 =
        // worldDataManager.getWorldTask().getWorldTaskMap().get(TaskType.WORLD_BOSS_TASK_ID_1);
        // CommonPb.WorldTask boss2 =
        // worldDataManager.getWorldTask().getWorldTaskMap().get(TaskType.WORLD_BOSS_TASK_ID_2);
        // if (boss1 == null || boss1.getHp() > 0) { // ???1?????????boss?????????
        // return 0;
        // } else if (boss2 != null && boss2.getHp() <= 0) {// ???2???boss??????
        // return 2;
        // } else {
        // return 1;
        // }

        // ?????? ?????????????????? boss?????????
        return worldScheduleService.getBossDeadState();

    }

    /**
     * ????????????
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public MoveCityRs moveCity(long roleId, MoveCityRq req) throws MwException {
        // ????????????????????????
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        if (!CheckNull.isEmpty(player.armys)) {
            throw new MwException(GameError.MOVE_HERO_OUT.getCode(), "???????????????????????????, roleId:,", roleId);
        }

        if (player.getDecisiveInfo().isDecisive()) {
            throw new MwException(GameError.DECISIVE_BATTLE_ING.getCode(), "?????????????????????,????????????, roleId:", roleId);
        }

        int pos = req.getPos();
        int type = req.getType();
        int prePos = player.lord.getPos();

        int preAreaId = MapHelper.getAreaIdByPos(prePos);
        StaticArea preSArea = StaticWorldDataMgr.getAreaMap().get(preAreaId);
        if (preSArea == null) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "??????????????????????????????, roleId:,", roleId, ", prePos:", prePos);
        }

        // ????????????????????????????????????
        int propId = 0;
        if (type == WorldConstant.MOVE_TYPE_POS) {
            propId = PropConstant.ITEM_ID_5003;
            if (pos == prePos) {
                throw new MwException(GameError.MOVE_SAME_POS.getCode(), "????????????????????????????????????????????????, roleId:,", roleId, ", pos:",
                        pos);
            }

            if (!worldDataManager.isEmptyPos(pos)) {
                throw new MwException(GameError.MOVE_NOT_EMPTY_POS.getCode(), "????????????????????????????????????, roleId:,", roleId,
                        ", pos:", pos);
            }
        } else if (type == WorldConstant.MOVE_TYPE_AREA) {
            propId = PropConstant.ITEM_ID_5002;
        } else if (type == WorldConstant.MOVE_TYPE_RANDOM) {
            propId = PropConstant.ITEM_ID_5001;
        } else if (type == WorldConstant.MOVE_TYPE_OPEN_ORDER_1) {
            propId = PropConstant.ITEM_ID_5004;
        } else if (type == WorldConstant.MOVE_TYPE_TO_STATE) {
            propId = PropConstant.ITEM_ID_5001;
        } else {
            throw new MwException(GameError.NO_CONFIG.getCode(), "???????????????????????????, roleId:,", roleId, ", pos:",
                    pos + ",type:" + type);
        }

        if (type == WorldConstant.MOVE_TYPE_AREA || type == WorldConstant.MOVE_TYPE_POS) {// ????????????
            // ???????????????
            int newArea = MapHelper.getAreaIdByPos(pos);
            StaticArea staticArea = StaticWorldDataMgr.getAreaMap().get(newArea);
            if (null == staticArea || !staticArea.isOpen()) {
                throw new MwException(GameError.AREA_NOT_OPEN.getCode(), "????????????????????????????????????, roleId:", roleId, ", area:",
                        newArea);
            }
            checkMoveCityByBoss(player, newArea);
        }

        // ?????????????????????
        if (type == WorldConstant.MOVE_TYPE_OPEN_ORDER_1) {
            int preArea = player.lord.getArea();
            int newArea = MapHelper.getAreaIdByPos(pos);
            if (preArea == newArea) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "?????????????????????????????????????????????   roleId:", roleId,
                        ", preArea:", preArea, ", newArea:", newArea);
            }
            int bossDeadState = worldScheduleService.getBossDeadState();
            // ???????????????????????????
            if (bossDeadState != ScheduleConstant.BOSS_NO_DEAD) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "???????????????????????????????????????   roleId:", roleId);
            }
            StaticArea sArea = StaticWorldDataMgr.getAreaMap().get(newArea);
            if (WorldConstant.AREA_ORDER_1 != sArea.getOpenOrder()) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "????????????????????????????????????   roleId:", roleId, ", newArea:",
                        newArea);
            }
        }

        // ??????????????????,?????????????????????
        if (type == WorldConstant.MOVE_TYPE_TO_STATE) {
            if (preSArea.getOpenOrder() != WorldConstant.AREA_ORDER_1) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "??????????????????????????????, roleId:,", roleId, ", preAreaId:",
                        preAreaId);
            }
            // ????????????boss?????????
            int bossState = bossDeadState();
            if (bossState == 0) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "???????????????boss?????????????????????, roleId:,", roleId,
                        ", preAreaId:", preAreaId);
            }
            if (bossState == 1) {
                // ?????????boss????????????,????????????boss??????,??????3??????????????????
                rewardDataManager.checkPropIsEnough(player, propId, 3, "????????????");
                rewardDataManager.subProp(player, propId, 3, AwardFrom.MOVE_CITY);// ,
                // "????????????"
            }
        } else {
            rewardDataManager.checkPropIsEnough(player, propId, 1, "????????????");
            rewardDataManager.subProp(player, propId, 1, AwardFrom.MOVE_CITY);// ,
            // "????????????"
        }
        // ?????????????????????????????????
        int newPos = 0;
        if (type == WorldConstant.MOVE_TYPE_POS) {
            newPos = pos;
        } else if (type == WorldConstant.MOVE_TYPE_AREA) {
            // ?????????????????????
            int areaId = MapHelper.getAreaIdByPos(pos);
            newPos = areaId == WorldConstant.AREA_TYPE_13 ? worldDataManager.randomKingAreaPos(player.lord.getCamp())
                    : worldDataManager.randomEmptyPosInArea(areaId);
        } else if (type == WorldConstant.MOVE_TYPE_RANDOM) {
            newPos = worldDataManager.randomPlayerPos(player, player.lord.getPos(), player.lord.getCamp());
        } else if (type == WorldConstant.MOVE_TYPE_OPEN_ORDER_1) {
            newPos = worldDataManager.randomEmptyPosInArea(MapHelper.getAreaIdByPos(pos));
        } else if (type == WorldConstant.MOVE_TYPE_TO_STATE) {
            Integer newAreaId = preSArea.getGotoArea().stream().findAny().orElse(null);
            if (newAreaId == null) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "??????????????????,", roleId, ", preAreaId:", preAreaId);
            }
            newPos = worldDataManager.randomEmptyPosInArea(newAreaId);
        }

        // ????????????????????????????????????????????????????????????
        if (player.battleMap.containsKey(prePos)) {// ????????????????????????????????????
            warService.cancelCityBattle(prePos, newPos, false, true);
        }

        // ?????????????????????????????????
        List<Army> guradArays = worldDataManager.getPlayerGuard(prePos);
        int now = TimeHelper.getCurrentSecond();
        if (!CheckNull.isEmpty(guradArays)) {
            for (Army army : guradArays) {
                Player tPlayer = playerDataManager.getPlayer(army.getLordId());
                if (tPlayer == null || tPlayer.armys.get(army.getKeyId()) == null) {
                    LogUtil.error("lordId: {}, army keyId: {}, pos: {} not found!!! guard army will remove",
                            army.getLordId(), army.getKeyId(), army.getTarget());
                    continue;
                }
                retreatArmyByDistance(tPlayer, army, now);
                synRetreatArmy(tPlayer, army, now);
                worldDataManager.removePlayerGuard(army.getTarget(), army);
                int heroId = army.getHero().get(0).getV1();
                mailDataManager.sendNormalMail(tPlayer, MailConstant.MOLD_GARRISON_RETREAT, now, player.lord.getNick(),
                        heroId, player.lord.getNick(), heroId);
            }
            //????????????????????????
            if (CheckNull.nonEmpty(guradArays)) {
                LogUtil.error("clear roleId: {}, pos: {} all error guard army", roleId, prePos);
                worldDataManager.removePlayerGuard(prePos);
            }
        }

        int newArea = MapHelper.getAreaIdByPos(newPos);
        LogUtil.debug(
                roleId + ",??????" + ",????????????area=" + player.lord.getArea() + ",newPos=" + newPos + ",newArea=" + newArea);

        // ??????????????????
        if (playerDataManager.getPlayerByArea(player.lord.getArea()) != null) {
            playerDataManager.getPlayerByArea(player.lord.getArea()).remove(roleId);
        } else {
            LogUtil.debug(roleId + ",???????????????Map" + ",????????????area=" + player.lord.getArea() + ",newPos=" + newPos
                    + ",newArea=" + newArea);
        }
        player.lord.setPos(newPos);
        player.lord.setArea(newArea);
        worldDataManager.removePlayerPos(prePos, player);
        worldDataManager.putPlayer(player);

        // ??????????????????
        if (type == WorldConstant.MOVE_TYPE_POS || type == WorldConstant.MOVE_TYPE_AREA
                || type == WorldConstant.MOVE_TYPE_RANDOM) {
            honorDailyService.addAndCheckHonorReport2s(player, HonorDailyConstant.COND_ID_1);
        }

        MoveCityRs.Builder builder = MoveCityRs.newBuilder();
        builder.setPos(newPos);
        if (player.props.get(propId) != null) {
            builder.setProp(PbHelper.createPropPb(player.props.get(propId)));
        }
        List<Integer> posList = new ArrayList<>();
        posList.add(prePos);
        posList.add(newPos);
        // ??????????????????????????????
        onPlayerPosChangeCallbcak(player, prePos, newPos, WorldConstant.CHANGE_POS_TYPE_1);
        EventBus.getDefault().post(
                new Events.AreaChangeNoticeEvent(posList, roleId, Events.AreaChangeNoticeEvent.MAP_AND_AREA_TYPE));
        //??????????????? ????????????????????????????????????????????????????????????????????????SyncRallyBattleRs?????? & ??????????????????????????? & ??????????????????
        if (Objects.nonNull(player.summon) && player.summon.getStatus() != 0 && StaticPartyDataMgr.jobHavePrivilege(player.lord.getJob(), PartyConstant.PRIVILEGE_CALL) && player.summon.getLastTime() + Constant.SUMMON_KEEP_TIME > TimeHelper.getCurrentSecond()) {
            //???????????????????????????
            this.sendSameCampAssembleInfo(player);
        }
        return builder.build();
    }

    /**
     * ?????????????????????????????????????????????
     *
     * @param player
     * @param prePos
     * @param newPos
     * @param type   1 ??????, 2 ????????? , 3 ??????
     */
    public void onPlayerPosChangeCallbcak(Player player, int prePos, int newPos, int type) {
        if (type == WorldConstant.CHANGE_POS_TYPE_2) { // ?????????
            List<Battle> battles = warDataManager.getBattlePosMap().get(prePos);
            if (!CheckNull.isEmpty(battles)) {
                Iterator<Battle> it = battles.iterator();
                while (it.hasNext()) {
                    Battle battle = it.next();
                    if (!CheckNull.isNull(battle) && battle.getType() == WorldConstant.BATTLE_TYPE_COUNTER_ATK
                            && battle.getBattleType() == WorldConstant.COUNTER_ATK_ATK && newPos > 0) {
                        it.remove();
                        warDataManager.getSpecialBattleMap().remove(prePos);
                        battle.setPos(newPos);
                        warDataManager.addSpecialBattle(battle);
                        Player defencer = battle.getDefencer();
                        if (!CheckNull.isNull(defencer)) {
                            HashSet<Integer> set = defencer.battleMap.get(newPos);
                            if (set == null) {
                                set = new HashSet<>();
                                defencer.battleMap.put(newPos, set);
                            }
                            set.add(battle.getBattleId());
                        }
                    }
                }
            }
        }
        rebelService.processJoinPlayerMovePos(player, prePos, newPos);
    }

    /**
     * ??????
     *
     * @param roleId
     * @param pos
     * @param type
     * @return
     * @throws MwException
     */
    public ScoutPosRs scoutPos(long roleId, int pos, int type) throws MwException {
        // ????????????????????????
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // ????????????
        if (!techDataManager.isOpen(player, TechConstant.TYPE_11)) {
            throw new MwException(GameError.SCOUT_POS_NEED_TECH.getCode(), "????????????,?????????????????????, roleId:,", roleId, ", pos:",
                    pos);
        }
        if (CrossWorldMapService.isOnCrossMap(player)) { // ???????????????????????????
            return crossAttackService.scoutPos(player, pos, type);
        }
        if (!worldDataManager.isPlayerPos(pos)) {
            throw new MwException(GameError.POS_NO_PLAYER.getCode(), "?????????????????????????????????, roleId:,", roleId, ", pos:", pos);
        }
        Player target = worldDataManager.getPosData(pos);

        int cdTime = player.common.getScoutCdTime();
        int now = TimeHelper.getCurrentSecond();
        if (cdTime > now + WorldConstant.SCOUT_CD_MAX_TIME) {
            throw new MwException(GameError.SCOUT_CD_TIME.getCode(), "????????????????????????CD??????, roleId:", roleId, ", pos:", pos,
                    ", cdTime:", cdTime);
        }

        // ?????????????????????????????????
        playerDataManager.autoAddArmy(target);
        int cityLv = target.building.getCommand();

        StaticScoutCost ssc = StaticScoutDataMgr.getScoutCostByCityLv(cityLv);
        if (null == ssc) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "?????????????????????, roleId:,", roleId, ", cityLv:", cityLv);
        }

        List<List<Integer>> costList;
        if (type == WorldConstant.SCOUT_TYPE_PRIMARY) {
            costList = ssc.getPrimary();
        } else if (type == WorldConstant.SCOUT_TYPE_MIDDLE) {
            costList = ssc.getMiddle();
        } else if (type == WorldConstant.SCOUT_TYPE_SENIOR) {
            costList = ssc.getSenior();
        } else {
            throw new MwException(GameError.SCOUT_TYPE_ERROR.getCode(), "?????????????????????, roleId:,", roleId, ", type:", type);
        }

        // ?????????????????????
        rewardDataManager.checkAndSubPlayerRes(player, costList, AwardFrom.SCOUT);

        // ??????????????????CD
        if (cdTime < now) {
            cdTime = now;
        }
        cdTime += WorldConstant.SCOUT_CD;
        player.common.setScoutCdTime(cdTime);

        int scoutLv = player.getTechLvById(WorldConstant.SCOUT_TECH_ID);
        int targetLv = target.getTechLvById(WorldConstant.SCOUT_TECH_ID);
        int gap = scoutLv - targetLv;
        gap += WorldConstant.getScoutAddByType(type);// ??????????????????

        Mail mail = null;
        Lord tarLord = target.lord;
        Turple<Integer, Integer> tarXy = MapHelper.reducePos(pos);
        Lord lord = player.lord;
        Turple<Integer, Integer> xy = MapHelper.reducePos(lord.getPos());
        int ret = StaticScoutDataMgr.randomScoutResultByLvGap(gap);
        if (ret != WorldConstant.SCOUT_RET_FAIL) {
            // ????????????????????????????????????
            mailDataManager.sendReportMail(target, null, MailConstant.MOLD_ENEMY_SCOUT_SUCC, null, now, lord.getCamp(),
                    lord.getLevel(), lord.getNick(), xy.getA(), xy.getB(), lord.getCamp(), lord.getLevel(),
                    lord.getNick(), xy.getA(), xy.getB());

            // ?????????????????????
            // PushMessageUtil.pushMessage(target.account, PushConstant.ID_SCOUTED, target.lord.getNick(), lord.getNick());

            // ??????????????????????????????????????????
            CommonPb.ScoutRes sRes = null;
            CommonPb.ScoutCity city = null;
            List<CommonPb.ScoutHero> sHeroList = null;
            if (ret >= WorldConstant.SCOUT_RET_SUCC1) {// ?????????????????????
                Resource res = target.resource;
                // ????????????
                long[] proRes = buildingDataManager.getProtectRes(target);
                Map<Integer, Integer> canPlunderRes = buildingDataManager.canPlunderScout(target, player, proRes);
                List<TwoInt> canPlunderList = new ArrayList<>();
                for (Entry<Integer, Integer> kv : canPlunderRes.entrySet()) {
                    TwoInt ti = PbHelper.createTwoIntPb(kv.getKey(), kv.getValue());
                    canPlunderList.add(ti);
                }
                sRes = PbHelper.createScoutResPb(proRes[1], proRes[2], proRes[0], res.getOre(), res.getHuman(),
                        canPlunderList);
                if (ret >= WorldConstant.SCOUT_RET_SUCC2) {// ???????????????????????????
                    city = PbHelper.createScoutCityPb(target.building.getWall(), tarLord.getFight(),
                            (int) res.getArm1(), (int) res.getArm2(), (int) res.getArm3());
                    if (ret >= WorldConstant.SCOUT_RET_SUCC3) {// ????????????????????????????????????
                        List<Hero> defheros = target.getAllOnBattleHeros();// ??????????????????????????????
                        sHeroList = new ArrayList<>();
                        int state;
                        int source;
                        for (Hero hero : defheros) {
                            source = WorldConstant.HERO_SOURCE_BATTLE;
                            state = getScoutHeroState(source, hero.getState());
                            sHeroList.add(PbHelper.createScoutHeroPb(hero, source, state, target));
                        }

                        // ?????????????????????????????????????????????????????????
                    }
                }
            }
            CommonPb.MailScout scout = PbHelper.createMailScoutPb(sRes, city, sHeroList);
            mail = mailDataManager.sendScoutMail(player, MailConstant.MOLD_SCOUT_SUCC, scout, now, tarLord.getCamp(),
                    tarLord.getLevel(), tarLord.getNick(), tarXy.getA(), tarXy.getB(), tarLord.getCamp(),
                    tarLord.getLevel(), tarLord.getNick(), tarXy.getA(), tarXy.getB());
        } else {// ??????????????????
            mail = mailDataManager.sendScoutMail(player, MailConstant.MOLD_SCOUT_FAIL, null, now, tarLord.getCamp(),
                    tarLord.getLevel(), tarLord.getNick(), tarXy.getA(), tarXy.getB(), tarLord.getCamp(),
                    tarLord.getLevel(), tarLord.getNick(), tarXy.getA(), tarXy.getB());
            // ??????????????????????????????
            Turple<Integer, Integer> myXy = MapHelper.reducePos(player.lord.getPos());
            mailDataManager.sendScoutMail(target, MailConstant.MOLD_ENEMY_SCOUT_FAIL, null, now, player.lord.getCamp(),
                    player.lord.getLevel(), player.lord.getNick(), myXy.getA(), myXy.getB(), player.lord.getCamp(),
                    player.lord.getLevel(), player.lord.getNick(), myXy.getA(), myXy.getB());
        }
        honorDailyService.addAndCheckHonorReport2s(player, HonorDailyConstant.COND_ID_10);

        ScoutPosRs.Builder builder = ScoutPosRs.newBuilder();
        builder.setCdTime(player.common.getScoutCdTime());
        builder.setMail(PbHelper.createMailPb(mail, player));
        return builder.build();
    }

    public int getScoutHeroState(int source, int heroState) {
        if (source == WorldConstant.HERO_SOURCE_BATTLE) {
            if (heroState == HeroConstant.HERO_STATE_IDLE) {
                return WorldConstant.SCOUT_HERO_STATE_DEF;
            } else if (heroState == HeroConstant.HERO_STATE_COLLECT) {
                return WorldConstant.SCOUT_HERO_STATE_COLLECT;
            } else if (heroState == HeroConstant.HERO_STATE_GUARD) {
                return WorldConstant.HERO_SOURCE_GUARD;
            } else {
                return WorldConstant.SCOUT_HERO_STATE_OUT;
            }
        } else if (source == WorldConstant.HERO_SOURCE_GUARD) {
            return WorldConstant.SCOUT_HERO_STATE_GUARD;
        }
        return WorldConstant.SCOUT_HERO_STATE_DEF;
    }

    /**
     * ??????CD??????
     *
     * @param roleId
     * @param type
     * @return
     * @throws MwException
     */
    public ClearCDRs clearCD(long roleId, int type) throws MwException {
        // ????????????????????????
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int cdTime = 0;
        if (type == WorldConstant.CD_TYPE_SCOUT) {
            cdTime = player.common.getScoutCdTime();
        }

        int now = TimeHelper.getCurrentSecond();
        if (cdTime <= now) {
            throw new MwException(GameError.NO_CD_TIME.getCode(), "?????????????????????CD??????, roleId:,", roleId, ", type:", type);
        }

        int cost = (int) Math.ceil(player.common.getScoutCdTime() - now * 1.00 / 60);

        // ?????????????????????
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, cost,
                AwardFrom.CLEAR_CD);

        // ??????CD??????
        if (type == WorldConstant.CD_TYPE_SCOUT) {
            player.common.setScoutCdTime(0);
        }

        ClearCDRs.Builder builder = ClearCDRs.newBuilder();
        return builder.build();
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

    public void fightMineGuard(Player atkplayer, Army army, int now) {
        int pos = army.getTarget();
        Guard guard = worldDataManager.getGuardByPos(pos);
        if (null == guard) {// ???????????????????????????????????????
            LogUtil.error("?????????????????????, pos:" + pos);
            StaticMine staticMine = worldDataManager.getMineByPos(pos);
            if (null == staticMine) {
                noMineRetreat(atkplayer, army, now);
            } else {
                collectArmy(atkplayer, army, now);
            }
            return;
        }

        // ????????????
        Player defPlayer = guard.getPlayer();

        // ???????????????????????????????????????????????????
        // PushMessageUtil.pushMessage(defPlayer.account, PushConstant.COLLECT_BY_ATTCK, atkplayer.lord.getNick());

        Fighter attacker = fightService.createFighter(atkplayer, army.getHero());
        Fighter defender = fightService.createFighter(defPlayer, guard.getForm());
        FightLogic fightLogic = new FightLogic(attacker, defender, true);
        fightLogic.fight();

        //????????????-??????????????????
        ActivityDiaoChanService.killedAndDeathTask0(attacker, true, true);
        ActivityDiaoChanService.killedAndDeathTask0(defender, true, true);

        // ????????????????????????????????????, key:roleId
        Map<Long, ChangeInfo> changeMap = new HashMap<>();
        // ????????????
        Map<Long, List<Award>> recoverArmyAwardMap = new HashMap<>();

        // ????????????
        if (attacker.lost > 0) {
            subHeroArm(atkplayer, attacker.forces, AwardFrom.ATTACK_GUARD, changeMap);
            // ????????????????????????????????????
            medalDataManager.angelInWhite(attacker, recoverArmyAwardMap);
            //????????????????????????---????????????
            seasonTalentService.execSeasonTalentEffect303(attacker, recoverArmyAwardMap);
            // ????????????
            activityDataManager.updRankActivity(atkplayer, ActivityConst.ACT_ARMY_RANK, attacker.lost);
            // ????????????????????????
            honorDailyDataManager.addAndCheckHonorReport2s(atkplayer, HonorDailyConstant.COND_ID_14, attacker.lost);
            // ?????????????????????
            battlePassDataManager.updTaskSchedule(atkplayer.roleId, TaskType.COND_SUB_HERO_ARMY, attacker.lost);
            // ???????????????????????????????????????????????????
            dailyAttackTaskService.addPlayerDailyAttackOther(defPlayer, attacker.lost);
        }
        if (defender.lost > 0) {
            subHeroArm(defPlayer, defender.forces, AwardFrom.DEFEND_GUARD, changeMap);
            // ????????????????????????????????????
            medalDataManager.angelInWhite(defender, recoverArmyAwardMap);
            //????????????????????????---????????????
            seasonTalentService.execSeasonTalentEffect303(defender, recoverArmyAwardMap);
            // ????????????
            activityDataManager.updRankActivity(defPlayer, ActivityConst.ACT_ARMY_RANK, defender.lost);
            // ????????????????????????
            honorDailyDataManager.addAndCheckHonorReport2s(defPlayer, HonorDailyConstant.COND_ID_14, defender.lost);
            // ?????????????????????
            battlePassDataManager.updTaskSchedule(defPlayer.roleId, TaskType.COND_SUB_HERO_ARMY, defender.lost);
            // ???????????????????????????????????????????????????
            dailyAttackTaskService.addPlayerDailyAttackOther(atkplayer, defender.lost);
        }

        // ????????????
        Lord atkLord = atkplayer.lord;
        Lord defLord = defPlayer.lord;
        boolean isSuccess = fightLogic.getWinState() == ArmyConstant.FIGHT_RESULT_SUCCESS;

        CommonPb.Record record = fightLogic.generateRecord();
        CommonPb.RptAtkPlayer.Builder rpt = CommonPb.RptAtkPlayer.newBuilder();
        rpt.setResult(isSuccess);
        rpt.setAttack(PbHelper.createRptMan(atkLord.getPos(), atkLord.getNick(), atkLord.getVip(), atkLord.getLevel()));
        rpt.setDefMan(PbHelper.createRptMan(defLord.getPos(), defLord.getNick(), defLord.getVip(), defLord.getLevel()));
        rpt.setAtkSum(PbHelper.createRptSummary(attacker.total, attacker.lost, atkLord.getCamp(), atkLord.getNick(),
                atkLord.getPortrait(), atkplayer.getDressUp().getCurPortraitFrame()));
        rpt.setDefSum(PbHelper.createRptSummary(defender.total, defender.lost, defLord.getCamp(), defLord.getNick(),
                defLord.getPortrait(), defPlayer.getDressUp().getCurPortraitFrame()));

        int addExp = 0;// ??????????????????????????????????????????????????????????????????????????????????????????= ?????????????????????*2
        rpt.addAllAtkHero(fightSettleLogic.mineFightHeroExpReward(atkplayer, attacker.forces));
        rpt.addAllDefHero(fightSettleLogic.mineFightHeroExpReward(defPlayer, defender.forces));
        rpt.setRecord(record);

        Turple<Integer, Integer> atkPos = MapHelper.reducePos(atkLord.getPos());
        Turple<Integer, Integer> defPos = MapHelper.reducePos(defLord.getPos());
        CommonPb.Report.Builder report = createAtkPlayerReport(rpt.build(), now);

        //????????????(?????????)
        EventDataUp.battle(atkplayer.account, atkplayer.lord, attacker, "atk", CheckNull.isNull(army.getBattleId()) ? "0" : String.valueOf(army.getBattleId()), String.valueOf(WorldConstant.BATTLE_TYPE_MINE_GUARD),
                String.valueOf(fightLogic.getWinState()), atkplayer.roleId, rpt.getAtkHeroList());
        //????????????(?????????)
        EventDataUp.battle(defPlayer.account, defPlayer.lord, defender, "def", CheckNull.isNull(army.getBattleId()) ? "0" : String.valueOf(army.getBattleId()), String.valueOf(WorldConstant.BATTLE_TYPE_MINE_GUARD),
                String.valueOf(fightLogic.getWinState()), atkplayer.roleId, rpt.getDefHeroList());

        //???????????????????????????????????????0????????????????????????
        boolean collectEnd = false;
        if (isSuccess || Objects.isNull(defender.getAliveForce())) {
            // ?????????????????????
            int resource = calcCollect(defPlayer, guard.getArmy(), now);
            // ?????????????????? ???????????????????????? = ?????????????????? * 20
            int time = now - guard.getBeginTime();
            guard.getArmy().setCollectTime(time);// ??????????????????
            // ?????????????????????
            retreatArmyByDistance(defPlayer, guard.getArmy(), now);
            // ??????????????????
            worldDataManager.putMineResource(pos, resource);
            // ????????????????????????
            worldDataManager.removeMineGuard(pos);

            addExp = (int) Math.ceil(time * 1.0 / Constant.MINUTE) * 20;
            addExp = heroService.adaptHeroAddExp(defPlayer, addExp);
            // ??????????????????
            Hero hero = defPlayer.heros.get(guard.getHeroId());
            addExp = heroService.addHeroExp(hero, addExp, defLord.getLevel(), defPlayer);

            LogUtil.error("roleId:", defPlayer.roleId, ", ===????????????:", time, ", ????????????:", addExp,
                    ", ????????????:" + guard.getGrab());
            boolean effect = mineService.hasCollectEffect(defPlayer, guard.getGrab().get(0).getId(),
                    new Date(guard.getBeginTime() * 1000L), guard.getArmy());// ????????????;

            CommonPb.MailCollect collect = PbHelper.createMailCollectPb(time, hero, addExp, guard.getGrab(), effect);
            //????????????
            mailDataManager.sendCollectMail(defPlayer, report, MailConstant.MOLD_COLLECT_DEF_FAIL, null, now,
                    recoverArmyAwardMap, defLord.getNick(), atkLord.getNick(), atkLord.getNick(), atkPos.getA(),
                    atkPos.getB(), defLord.getNick(), defPos.getA(), defPos.getB());
            //????????????
            mailDataManager.sendCollectMail(defPlayer, null, MailConstant.MOLD_COLLECT_DEF_FAIL_COLLECT, collect, now,
                    recoverArmyAwardMap, defLord.getNick(), atkLord.getNick(), atkLord.getNick(), atkPos.getA(),
                    atkPos.getB(), defLord.getNick(), defPos.getA(), defPos.getB());

            // ???????????????????????????
            synRetreatArmy(defPlayer, guard.getArmy(), now);

            //????????????-????????????
            ActivityDiaoChanService.completeTask(defPlayer, ETask.COLLECT_RES, guard.getGrab().get(0).getId(), guard.getGrab().get(0).getCount());
            TaskService.processTask(defPlayer, ETask.COLLECT_RES, guard.getGrab().get(0).getId(), guard.getGrab().get(0).getCount());

            collectEnd = true;
        }
        if (isSuccess) {// ???????????????????????????????????????????????????????????????????????????
            // ?????????????????????
            collectArmy(atkplayer, army, now);

            // ??????????????????
            mailDataManager.sendCollectMail(atkplayer, report, MailConstant.MOLD_COLLECT_ATK_SUCC, null, now,
                    recoverArmyAwardMap, atkLord.getNick(), defLord.getNick(), atkLord.getNick(), atkPos.getA(),
                    atkPos.getB(), defLord.getNick(), defPos.getA(), defPos.getB());
        } else {// ????????????????????????
            retreatArmyByDistance(atkplayer, army, now);

            // ???????????????????????????????????????
            // List<TwoInt> form = new ArrayList<>();
            // Hero hero;
            // for (Force force : defender.forces) {
            // if (force.totalLost > 0) {
            // hero = defPlayer.heros.get(force.id);
            // if (null == hero) {
            // LogUtil.error("??????????????????????????????, heroId:", force.id);
            // continue;
            // }
            // form.add(PbHelper.createTwoIntPb(force.id, hero.getCount()));
            // }
            // }
            // guard.getArmy().setHero(form);

            // ??????????????????
            mailDataManager.sendCollectMail(atkplayer, report, MailConstant.MOLD_COLLECT_ATK_FAIL, null, now,
                    recoverArmyAwardMap, atkLord.getNick(), defLord.getNick(), atkLord.getNick(), atkPos.getA(),
                    atkPos.getB(), defLord.getNick(), defPos.getA(), defPos.getB());
            if (!collectEnd) {
                mailDataManager.sendCollectMail(defPlayer, report, MailConstant.MOLD_COLLECT_DEF_SUCC, null, now,
                        recoverArmyAwardMap, defLord.getNick(), atkLord.getNick(), atkLord.getNick(), atkPos.getA(),
                        atkPos.getB(), defLord.getNick(), defPos.getA(), defPos.getB());
            }
//            // ?????????????????????????????????
//            sendRoleResChange(changeMap);
//            return;
        }

        //????????????????????????
        if (army.getBattleId() != null && army.getBattleId() > 0) {
            warDataManager.removeBattleById(army.getBattleId());
//            if (defPlayer.isLogin) {
//                syncAttackRole(defPlayer, atkplayer.lord, army.getEndTime(),
//                        WorldConstant.ATTACK_ROLE_0);
//            }
        }
        // ?????????????????????????????????
        sendRoleResChange(changeMap);
    }

    /**
     * ??????????????????
     *
     * @param defPlayer
     * @param army
     * @param now
     */
    public void synRetreatArmy(Player defPlayer, Army army, int now) {

        Base.Builder msg = PbHelper.createSynBase(SyncArmyRs.EXT_FIELD_NUMBER, SyncArmyRs.ext,
                SyncArmyRs.newBuilder().setArmy(PbHelper.createArmyPb(army, false)).build());
        MsgDataManager.getIns().add(new Msg(defPlayer.ctx, msg.build(), defPlayer.roleId));
        LogUtil.debug("??????????????????target=" + army.getTarget() + ",player=" + defPlayer.roleId);
    }

    /**
     * ?????????????????????????????????
     *
     * @param player
     * @param army
     * @param now
     * @return ?????????????????????????????????
     */
    public int calcCollect(Player player, Army army, int now) {
        return mineService.calcCollect(player, army, now);
    }

    /**
     * ???????????????????????????????????????????????????
     *
     * @param player
     * @param army
     * @param now
     */
    public void noMineRetreat(Player player, Army army, int now) {
        // ??????????????????
        mailDataManager.sendCollectMail(player, null, MailConstant.MOLD_COLLECT_NO_TARGET, null, now);

        // ????????????
        retreatArmyByDistance(player, army, now);

        //???????????????????????????
        cancelMineBattle(army.getTarget(), now, player);
        //??????????????????????????????
        MineData mineData = worldDataManager.getMineMap().get(army.getTarget());
        if (Objects.nonNull(mineData))
            mineData.clearCollectTeam();
    }

    /**
     * ??????????????????
     *
     * @param player
     * @param army
     * @param now
     */
    public void noTargetRetreat(Player player, Army army, int now) {
        int pos = army.getTarget();
        Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
        mailDataManager.sendReportMail(player, null, MailConstant.MOLD_ATK_TARGET_NOT_FOUND, null, now, xy.getA(),
                xy.getB(), xy.getA(), xy.getB());
        // ????????????
        retreatArmyByDistance(player, army, now);
    }

    /**
     * ????????????
     *
     * @param player
     * @param army
     * @param now
     */
    public void collectArmy(Player player, Army army, int now) {
        int pos = army.getTarget();
        StaticMine staticMine = worldDataManager.getMineByPos(pos);
        if (null == staticMine) {
            noMineRetreat(player, army, now);
            return;
        }

        int heroId = army.getHero().get(0).getV1();
        army.setState(ArmyConstant.ARMY_STATE_COLLECT);
        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(heroId);
        // ??????????????????????????????
        int maxTime = mineService.collectTime(staticHero.getCollect(), pos, heroId, player);
        int resource = worldDataManager.getMineResource(pos);
        double speed = mineService.collectSpeed(staticMine.getSpeed(), pos, heroId, player);
        double maxCollect = speed * 1.0d * maxTime / Constant.HOUR;
        if (resource < maxCollect) {
            maxCollect = resource;
        }
        maxTime = (int) Math.ceil(maxCollect * Constant.HOUR / speed);

        army.setDuration(maxTime);
        army.setEndTime(now + maxTime);

        Guard guard = new Guard(player, army);
        worldDataManager.addMineGuard(guard);

        Hero hero;
        for (TwoInt twoInt : army.getHero()) {
            hero = player.heros.get(twoInt.getV1());
            hero.setState(ArmyConstant.ARMY_STATE_COLLECT);
        }

        //??????????????????, ???????????????????????????????????????????????????
        syncRallyMineBattle(pos, army, now, player);
    }

    /**
     * ????????????????????????
     *
     * @param pos
     * @param army
     * @param now
     * @param player
     */
    private void syncRallyMineBattle(int pos, Army army, int now, Player player) {
        //??????????????????????????????????????????????????????????????????????????????
        MineData mineData = worldDataManager.getMineMap().get(pos);
        if (ObjectUtils.isEmpty(mineData.getCollectTeam())) {
            return;
        }

        int defArmCount = 0;
        for (TwoInt tmp : army.getHero()) {
            defArmCount += tmp.getV2();
        }

        Battle battle;
        Player collectPlayer;
        int attackArmCount = 0;
        LinkedList<Long> collectTeam = mineData.getCollectTeam();
        for (Long collector : collectTeam) {
            collectPlayer = playerDataManager.getPlayer(collector);
            if (CheckNull.isNull(collectPlayer) || ObjectUtils.isEmpty(collectPlayer.armys))
                continue;

            for (Army cArmy : collectPlayer.armys.values()) {
                if (cArmy.getTarget() != pos || now > cArmy.getEndTime() ||
                        cArmy.getType() != ArmyConstant.ARMY_TYPE_COLLECT || cArmy.getState() != ArmyConstant.ARMY_STATE_MARCH)
                    continue;

                Integer battleId = cArmy.getBattleId() == null ? 0 : cArmy.getBattleId();
                battle = warDataManager.getBattleMap().get(battleId);
                if (CheckNull.isNull(battle) || battleId.intValue() == 0) {
                    //??????????????????
                    for (TwoInt tmp : cArmy.getHero()) {
                        attackArmCount += tmp.getV2();
                    }

                    battle = createMineBattle(player, collectPlayer, pos, attackArmCount, defArmCount, now, cArmy.getEndTime() - now);
                    cArmy.setBattleId(battle.getBattleId());
                    warDataManager.addBattle(collectPlayer, battle);
                } else {
                    //??????????????????????????????
//                    if (battle.getDefencer().isLogin) {
//                        syncAttackRole(battle.getDefencer(), collectPlayer.lord, battle.getBattleTime(),
//                                WorldConstant.ATTACK_ROLE_0);
//                    }
                    DataResource.getBean(CampService.class).syncCancelRallyBattle(null, battle, null);

                    //??????????????????????????????
                    battle.setDefencer(player);
                    battle.setDefArm(defArmCount);
                    battle.setDefCamp(player.getCamp());
                    battle.setDefencerId(player.getLordId());
                    DataResource.getBean(CampService.class).syncRallyBattle(collectPlayer, battle, null);
                }
//
//                if (player.isLogin) {
//                    syncAttackRole(player, collectPlayer.lord, cArmy.getEndTime(), WorldConstant.ATTACK_ROLE_1);
//                }
            }
        }
    }

    /**
     * ????????????????????????
     *
     * @param defencePlayer
     * @param attackPlayer
     * @param pos
     * @param armCount
     * @param defArmCount
     * @param now
     * @param marchTime
     * @return
     */
    public Battle createMineBattle(Player defencePlayer, Player attackPlayer, int pos, int armCount, int defArmCount, int now, int marchTime) {
        Battle battle = new Battle();
        battle.setType(WorldConstant.BATTLE_TYPE_MINE_GUARD);
        battle.setBattleType(WorldConstant.BATTLE_TYPE_MINE_GUARD);
        battle.setBattleTime(now + marchTime);
        battle.setBeginTime(now);
        battle.setDefencerId(defencePlayer.lord.getLordId());
        battle.setPos(pos);
        battle.setSponsor(attackPlayer);
        battle.setDefencer(defencePlayer);
        battle.setDefCamp(defencePlayer.lord.getCamp());
        battle.addAtkArm(armCount);
        battle.addDefArm(defArmCount);
        battle.getAtkRoles().add(attackPlayer.getLordId());

        return battle;
    }

    /**
     * ????????????????????????
     *
     * @param guard
     * @param now
     */
    public void finishCollect(Guard guard, int now) {
        List<Award> grab = guard.getGrab();// ????????????
        StaticMine staticMine = worldDataManager.getMineByPos(guard.getPos());
        Player player = guard.getPlayer();
        if (staticMine == null) {
            retreatArmyByDistance(player, guard.getArmy(), now);
            return;
        }
        try {
            Turple<Integer, Integer> xy = MapHelper.reducePos(guard.getPos());
            Hero hero = player.heros.get(guard.getHeroId());
            int time = now - guard.getBeginTime();
            boolean effect = mineService.hasCollectEffect(player, staticMine.getMineType(),
                    new Date(guard.getBeginTime() * 1000L), guard.getArmy());// ????????????
            guard.getArmy().setCollectTime(time);
            int addExp = (int) Math.ceil(time * 1.0 / Constant.MINUTE) * 20;// ??????????????????
            addExp = heroService.adaptHeroAddExp(player, addExp);
            // ??????????????????
            addExp = heroService.addHeroExp(hero, addExp, player.lord.getLevel(), player);

            CommonPb.MailCollect collect = PbHelper.createMailCollectPb(time, hero, addExp, grab, effect);

            // ??????????????????????????????
            WorldWarSeasonDailyRestrictTaskService restrictTaskService = DataResource.ac
                    .getBean(WorldWarSeasonDailyRestrictTaskService.class);
            restrictTaskService.updatePlayerDailyRestrictTask(player, TaskType.COND_WORLD_WAR_MINE_CNT,
                    guard.getArmy().getCollectTime());
            // ??????????????????
            mailDataManager.sendCollectMail(player, null, MailConstant.MOLD_COLLECT, collect, now,
                    grab.get(0).getType(), grab.get(0).getId(), grab.get(0).getCount(), staticMine.getLv(),
                    staticMine.getMineId(), xy.getA(), xy.getB());
            // ??????
            pushMsgCollect(player, grab.get(0));

            //????????????-????????????
            ActivityDiaoChanService.completeTask(player, ETask.COLLECT_RES, staticMine.getMineType(), grab.get(0).getCount());
            TaskService.processTask(player, ETask.COLLECT_RES, staticMine.getMineType(), grab.get(0).getCount());
        } catch (Exception e) {
            LogUtil.error("???????????? ?????? roleId:", player.roleId, ", army:", guard.getArmy());
        }

        // ????????????
        retreatArmyByDistance(player, guard.getArmy(), now);
        //??????????????????????????????
        cancelMineBattle(guard.getPos(), now, guard.getPlayer());
    }

    public void pushMsgCollect(Player player, Award award) {
        int type = award.getType();
        int id = award.getId();
        StringBuilder sb = new StringBuilder("s_resource_");
        if (type == AwardType.RESOURCE) {
            sb.append(id);
        } else if (type == AwardType.MONEY && id == AwardType.Money.GOLD) {
            sb.append(5);
        }
        String txtId = sb.toString();
        String name = StaticIniDataMgr.getTextName(txtId);
        if (null == name) {
            LogUtil.error("????????????????????????????????????????????????, roleId:", player.roleId, ", id:", txtId);
            return;
        }
        // PushMessageUtil.pushMessage(player.account, PushConstant.COLLECT_COMPLETE, name);
    }

    /*-------------------------------????????????start-----------------------------------*/

    /**
     * ????????????????????????
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetNightRaidInfoRs getNightRaidInfo(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int now = TimeHelper.getCurrentSecond();
        StaticNightRaid sNightRaid = StaticNightRaidMgr.getNightRaid();
        GetNightRaidInfoRs.Builder builder = GetNightRaidInfoRs.newBuilder();
        builder.addAllWeek(sNightRaid.getWeek());
        builder.setHintTimeStart(sNightRaid.getHintTimeStart());
        builder.setHintTimeEnd(sNightRaid.getHintTimeEnd());
        builder.setDuringTimeStart(sNightRaid.getDuringTimeStart());
        builder.setDuringTimeEnd(sNightRaid.getDuringTimeEnd());
        builder.addAllBanditLv(sNightRaid.getBanditLv());
        builder.setMaxBanditCnt(sNightRaid.getBanditCount());
        builder.setAttackBanditCnt(StaticNightRaidMgr.getNightRaidBanditCnt(player, now));
        builder.setOpenBegin(sNightRaid.getOpenBegin());
        int openServerDay = serverSetting.getOpenServerDay(new Date());// ???????????????
        builder.setCurOpenServerDay(openServerDay);
        return builder.build();
    }

    /*-------------------------------????????????end-----------------------------------*/

    /**
     * ????????????????????????????????????????????????
     *
     * @param player
     * @return true ????????????????????????????????????
     */
    public boolean checkCurTaskHasBandit(Player player) {
        return !Collections.disjoint(Constant.ATK_BANDIT_MARCH_TIME_TASKID, player.chapterTask.getOpenTasks().keySet()); // ?????????
    }

    /**
     * ????????????????????????????????????
     *
     * @param lv       ???????????????????????????
     * @param banditLv ????????????????????????????????????
     * @return ????????????
     */
    public boolean checkCurTaskHasBandit(int lv, int banditLv) {
        return (lv >= Constant.ATK_BANDIT_MARCH_TIME_TASKID.get(0) && lv <= Constant.ATK_BANDIT_MARCH_TIME_TASKID.get(1)) && lv > banditLv;
    }

    /**
     * ????????????????????????
     *
     * @param player
     * @param attckFoce
     * @param now
     * @param banditLv
     * @return
     */
    public List<List<Integer>> attckBanditLostRecvCalc(Player player, List<Force> attckFoce, int now, int banditLv,
                                                       int type) {
        List<List<Integer>> armyRecvAward = new ArrayList<>();
        Map<Integer, Integer> lostArmy = new HashMap<>();
        for (Force f : attckFoce) {
            if (f.ownerId != player.roleId) {
                continue;
            }
            int lost = f.totalLost;
            int armyType = f.armType;
            Integer cnt = lostArmy.get(armyType);
            cnt = cnt == null ? 0 : cnt.intValue();
            int sum = cnt + lost;
            lostArmy.put(armyType, sum);
        }
        Map<Integer, Integer> recoverArmyCnt = null;
        switch (type) {
            case WorldConstant.LOST_RECV_CALC_NIGHT:
                recoverArmyCnt = CalculateUtil.calcRecoverArmyCntByNight(player, now, banditLv, lostArmy);
                break;
            case WorldConstant.LOST_RECV_CALC_GESTAPO:
                recoverArmyCnt = activityDataManager.calcRecoverArmyCntByGestapo(lostArmy);
                break;
        }

        // ???????????????????????????
        if (!CheckNull.isEmpty(recoverArmyCnt)) {
            for (Entry<Integer, Integer> kv : recoverArmyCnt.entrySet()) {
                List<Integer> award = new ArrayList<>();
                award.add(AwardType.ARMY);
                award.add(kv.getKey());
                award.add(kv.getValue());
                armyRecvAward.add(award);
            }
        }
        return armyRecvAward;
    }

    /**
     * ????????????????????????
     *
     * @param player
     * @param forces
     */
    public void subHeroArm(Player player, List<Force> forces, AwardFrom from, Map<Long, ChangeInfo> changeMap) {
        if (null == player || CheckNull.isEmpty(forces)) {
            return;
        }

        int lost;
        Hero hero;
        ChangeInfo info;
        for (Force force : forces) {
            if (force.totalLost > 0) {
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
            if (force.killed > 0 && from != AwardFrom.ATTACK_BANDIT) {
                // ????????????
                activityDataManager.updActivity(player, ActivityConst.ACT_BIG_KILL, force.killed, 0, true);
            }
        }

    }

    private boolean checkNullBattle(Player player, Army army, int now) {
        Integer battleId = army.getBattleId();
        if (null == battleId) {
            LogUtil.error("??????????????????id, roleId:", player.roleId, ", battleId:", battleId);
            battleId = 0;
        }
        Battle battle = warDataManager.getBattleMap().get(battleId);
        if (null == battle) {
            LogUtil.error("????????????????????????, roleId:", player.roleId, ", battle:", battle);
            // ?????????????????????
            return true;
        }
        return false;
    }

    public void addBattleArmy(Battle battle, long roleId, List<Integer> heroIdList, int keyId, boolean isAtk) {
        if (isAtk) {
            battle.getAtkList()
                    .add(BattleRole.newBuilder().setKeyId(keyId).setRoleId(roleId).addAllHeroId(heroIdList).build());
        } else {
            battle.getDefList()
                    .add(BattleRole.newBuilder().setKeyId(keyId).setRoleId(roleId).addAllHeroId(heroIdList).build());
        }
    }

    // public void addBattleArmy(Battle battle, long roleId, List<Integer>
    // heroIdList, int keyId, boolean isAtk) {
    // Map<Integer, List<Integer>> keyMap = new HashMap<>();
    // keyMap.put(keyId, heroIdList);
    // Map<Long, Map<Integer, List<Integer>>> map = new HashMap<>();
    // map.put(roleId, keyMap);
    // if (isAtk) {
    // battle.getAtks().add(map);
    // } else {
    // battle.getDefs().add(map);
    // }
    // }

    /*--------------------------------????????????start-----------------------------------*/

    /**
     * ????????????,??????????????????
     *
     * @param player
     * @param army
     * @param now
     */
    public void retreatArmyByDistance(Player player, Army army, int now) {
        if (null == player || null == army || army.isRetreat()) {
            return;
        }
        // int marchTime = marchTime(player, army.getTarget());
        //
        // int startArea = MapHelper.getAreaIdByPos(player.lord.getPos());
        // int targetArea = MapHelper.getAreaIdByPos(army.getTarget());
        // if (startArea == WorldConstant.AREA_TYPE_13 && startArea !=
        // targetArea) {
        // Battle battle =
        // warDataManager.getBattleMap().get(army.getBattleId());
        // if (battle != null && battle.getSponsor().lord.getCamp() ==
        // player.lord.getCamp()) {
        // // ??????????????????????????????(????????????)
        // marchTime = WorldConstant.SPECIAL_MARCH_TIME;
        // }
        // }
        //
        // army.setState(ArmyConstant.ARMY_STATE_RETREAT);
        // army.setDuration(marchTime);
        // army.setEndTime(now + marchTime);
        //
        // Hero hero;
        // for (TwoInt twoInt : army.getHero()) {
        // hero = player.heros.get(twoInt.getV1());
        // hero.setState(ArmyConstant.ARMY_STATE_RETREAT);
        // }

        retreatArmy(player, army, now);
        // ??????????????????
        List<Integer> posList = MapHelper
                .getAreaStartPos(MapHelper.getLineAcorss(army.getTarget(), player.lord.getPos()));
        posList.add(army.getTarget());
        posList.add(player.lord.getPos());
        EventBus.getDefault()
                .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));
    }

    /**
     * ???????????? ??????????????????
     *
     * @param player ????????????
     * @param army   ????????????
     * @param now    ???????????????
     */
    public void retreatArmy(Player player, Army army, int now) {
        if (null == player || null == army || army.isRetreat()) {
            return;
        }
        // ???????????????????????????????????????????????????????????????
        Integer battleId = army.getBattleId();
        if (null != battleId && battleId > 0) {
            Battle battle = warDataManager.getBattleMap().get(battleId);
            if (null != battle) {
                int camp = player.lord.getCamp();
                int armCount = army.getArmCount();
                LogUtil.debug(player.roleId + ",????????????=" + armCount);
                // ?????????????????????
                battle.updateArm(camp, -armCount);
            }
        }
        // ????????????????????????????????????????????? ?????????????????????????????????, ????????????????????????
        int marchTime = marchTime(player, army.getTarget(), army.getType() == ArmyConstant.ARMY_TYPE_ATK_BANDIT);
        // ????????????
        marchTime = berlinWarService.getMarchTime(player, army, marchTime);
        marchTime = gestapoService.getGestapoMarchTime(army, marchTime);

        //??????????????????
        if (army.getType() == ArmyConstant.ARMY_TYPE_RELIC_BATTLE)
            marchTime = (int) (marchTime * ActParamConstant.RELIC_MARCH_SPEEDUP / NumberUtil.TEN_THOUSAND_DOUBLE);

        // ??????????????????????????????????????????
        int startArea = MapHelper.getAreaIdByPos(player.lord.getPos());
        int targetArea = MapHelper.getAreaIdByPos(army.getTarget());
        if (startArea == WorldConstant.AREA_TYPE_13 && startArea != targetArea) {
            // ??????????????????????????????(????????????)
            // marchTime = WorldConstant.SPECIAL_MARCH_TIME;
            marchTime = army.getDuration(); // ??????????????????
        }

        // ?????????????????????????????????,??????????????????,???????????????????????????????????????
        if (MapHelper.getAreaIdByPos(army.getTarget()) != player.lord.getArea()) {
            March march = new March(player, army);
            worldDataManager.addMarch(march);
        }

        army.setState(ArmyConstant.ARMY_STATE_RETREAT);
        army.setDuration(marchTime);
        army.setEndTime(now + marchTime);

        Hero hero;
        for (TwoInt twoInt : army.getHero()) {
            hero = player.heros.get(twoInt.getV1());
            hero.setState(ArmyConstant.ARMY_STATE_RETREAT);
        }
    }

    /**
     * ????????????????????????????????????
     *
     * @param player
     * @param army
     * @param now
     * @param type
     */
    public void retreatArmy(Player player, Army army, int now, int type) {
        if (null == player || null == army || army.isRetreat()) {
            return;
        }
        int marchTime = marchTime(player, army.getTarget());
        if (army.getEndTime() > now && army.getState() == ArmyConstant.ARMY_STATE_MARCH) {// ????????????????????????????????????
            marchTime = now + army.getDuration() - army.getEndTime();
        }

        marchTime = gestapoService.getGestapoMarchTime(army, marchTime);

        army.setState(ArmyConstant.ARMY_STATE_RETREAT);

        if (type == ArmyConstant.MOVE_BACK_TYPE_1) {
            army.setDuration(army.getDuration() / 2);
            army.setEndTime(now + (marchTime) / 2);
        } else if (type == ArmyConstant.MOVE_BACK_TYPE_2) {
            army.setDuration(1);
            army.setEndTime(now + 1);
        } else {// ????????????
            army.setDuration(marchTime);
            army.setEndTime(now + marchTime);
        }

        Hero hero;
        for (TwoInt twoInt : army.getHero()) {
            hero = player.heros.get(twoInt.getV1());
            hero.setState(ArmyConstant.ARMY_STATE_RETREAT);
        }
    }

    /*--------------------------------???????????? end-----------------------------------*/

    public void buildRptHeroData(Fighter defender, CommonPb.RptAtkBandit.Builder rpt, boolean calAward) {
        for (Force force : defender.forces) {
            int award = 0;
            if (calAward) {
                award = (int) (force.killed * 0.8f + force.totalLost * 0.2f);
            }
            Player tmpP = DataResource.ac.getBean(PlayerDataManager.class).getPlayer(force.ownerId);
            if (CheckNull.isNull(tmpP)) {
                rpt.addDefHero(
                        PbHelper.createRptHero(Constant.Role.BANDIT, force.killed, award, force.id, null, 0, 0, force.lost));
            } else {
                Hero hero = tmpP.heros.get(force.id);
                if (CheckNull.isNull(hero)) {
                    rpt.addDefHero(
                            PbHelper.createRptHero(Constant.Role.BANDIT, force.killed, award, force.id, playerDataManager.getNickByLordId(force.ownerId), 0, 0, force.lost));
                } else {
                    rpt.addDefHero(
                            PbHelper.createRptHero(Constant.Role.BANDIT, force.killed, award, hero, playerDataManager.getNickByLordId(force.ownerId), 0, 0, force.lost));
                }
            }
        }
    }

    public void buildRptHeroData(Fighter defender, CommonPb.RptAtkPlayer.Builder rpt, int roleType, boolean calAward) {
        for (Force force : defender.forces) {
            int award = 0;
            if (calAward) {
                award = (int) (force.killed * 0.8f + force.totalLost * 0.2f);
            }
            Player tmpP = DataResource.ac.getBean(PlayerDataManager.class).getPlayer(force.ownerId);
            if (CheckNull.isNull(tmpP)) {
                rpt.addDefHero(
                        PbHelper.createRptHero(roleType, force.killed, award, force.id, null, 0, 0, force.lost));
            } else {
                Hero hero = tmpP.heros.get(force.id);
                if (CheckNull.isNull(hero)) {
                    rpt.addDefHero(
                            PbHelper.createRptHero(roleType, force.killed, award, force.id, playerDataManager.getNickByLordId(force.ownerId), 0, 0, force.lost));
                } else {
                    rpt.addDefHero(
                            PbHelper.createRptHero(roleType, force.killed, award, hero, playerDataManager.getNickByLordId(force.ownerId), 0, 0, force.lost));
                }
            }
        }
    }

    public CommonPb.Report.Builder createAtkBanditReport(CommonPb.RptAtkBandit rpt, int now) {
        CommonPb.Report.Builder report = CommonPb.Report.newBuilder();
        report.setTime(now);
        report.setRptBandit(rpt);
        return report;
    }

    public CommonPb.Report.Builder createAtkPlayerReport(CommonPb.RptAtkPlayer rpt, int now) {
        CommonPb.Report.Builder report = CommonPb.Report.newBuilder();
        report.setTime(now);
        report.setRptPlayer(rpt);
        return report;
    }

    /**
     * ????????????????????????????????????????????????????????????????????????????????????????????????
     */
    public void worldTimerLogic() {
        Iterator<Player> iterator = playerDataManager.getPlayers().values().iterator();
        int now = TimeHelper.getCurrentSecond();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            try {
                if (!player.isActive() || player.armys.isEmpty()) {
                    continue;
                }
                dealWorldAction(player, now);
            } catch (Exception e) {
                LogUtil.error(e, "??????????????????????????????????????????, lordId:", player.lord.getLordId());
            }
        }
    }

    private void dealWorldAction(Player player, int now) {
        Map<Integer, Army> map = player.armys;
        Iterator<Army> it = map.values().iterator();

        int state = 0;
        Army army;
        while (it.hasNext()) {
            army = it.next();
            try {
                state = army.getState();
                if (now >= army.getEndTime()) {
                    if (state == ArmyConstant.ARMY_STATE_MARCH) {// ????????????
                        marchService.marchEnd(player, army, now);
                    } else if (state == ArmyConstant.ARMY_STATE_RETREAT) {// ????????????
                        marchService.retreatEnd(player, army);
                        it.remove();
                    } else if (state == ArmyConstant.ARMY_STATE_GUARD) {// ????????????
                        retreatArmyByDistance(player, army, now);
                        synWallCallBackRs(0, army);
                        // ????????????????????????
                        final Army fArmy = army;
                        Optional.of(fArmy).flatMap(a -> Optional.of(a.getTarLordId()))// j8???null??????
                                .flatMap(roleId -> Optional.ofNullable(playerDataManager.getPlayer(roleId)))
                                .ifPresent(tPlayer -> {
                                    mailDataManager.sendNormalMail(player, MailConstant.MOLD_GARRISON_FILL_TIME_RETREAT,
                                            now, tPlayer.lord.getNick(), fArmy.getHero().get(0).getV1(),
                                            tPlayer.lord.getNick(), fArmy.getHero().get(0).getV1());
                                });
                        worldDataManager.removePlayerGuard(fArmy.getTarget(), fArmy);
                    } else if (state == ArmyConstant.ARMY_STATE_BATTLE) {// ??????????????????
                        if (army.getType() == ArmyConstant.ARMY_TYPE_ATK_PLAYER
                                || army.getType() == ArmyConstant.ARMY_TYPE_ATK_CAMP) {
                            if (checkNullBattle(player, army, now)) {// ??????????????????????????????
                                marchService.marchEnd(player, army, now);
                            }
                        }
                    }
                    // ?????????key
//                    Integer battleId = army.getBattleId();
//                    if (ArmyConstant.ARMY_TYPE_BERLIN_WAR != army.getType()
//                            && ArmyConstant.ARMY_TYPE_BATTLE_FRONT_WAR != army.getType()
//                            && ArmyConstant.ARMY_TYPE_ATK_SUPERMINE != army.getType()
//                            && army.getState() != ArmyConstant.ARMY_STATE_RETREAT
//                            && ArmyConstant.ARMY_TYPE_COLLECT != army.getType()
//                            && battleId != null && battleId > 0 && now >= army.getBattleTime() && !warDataManager.getBattleMap().containsKey(battleId)) {
//                        LogUtil.debug("--------------???????????????????????? ??????  roleId:", player.roleId);
//                        // ????????????????????????id
//                        retreatArmy(player, army, TimeHelper.getCurrentSecond(), ArmyConstant.MOVE_BACK_TYPE_2);
//                        // ??????army??????
//                        synRetreatArmy(player, army, now); // ??????army??????
//                        LogUtil.debug("--------------??????????????????: ", army);
//                        int keyId = army.getKeyId();
//                        // ?????????????????????
//                        if (army.getType() == ArmyConstant.ARMY_TYPE_COLLECT) {
//                            worldDataManager.removeMineGuard(army.getTarget());
//                        } else if (army.getType() == ArmyConstant.ARMY_TYPE_COLLECT_SUPERMINE) {
//                            SuperMine sm = worldDataManager.getSuperMineMap().get(army.getTarget());
//                            if (Objects.nonNull(sm)) {
//                                sm.removeCollectArmy(player.roleId, keyId);
//                            }
//                        }
//                        Set<Integer> ids = army.getHero().stream().map(TwoInt::getV1).collect(Collectors.toSet());
//                        for (int i = 1; i < player.heroBattle.length; i++) {
//                            int heroId = player.heroBattle[i];
//                            // ??????army???
//                            if (!ids.contains(heroId)) {
//                                continue;
//                            }
//                            Hero hero = player.heros.get(heroId);
//                            if (hero != null) {
//                                hero.setState(HeroConstant.HERO_STATE_IDLE);
//                            }
//                            LogUtil.debug("--------------??????????????????????????? heroId: ", heroId);
//                        }
//                        for (int i = 1; i < player.heroAcq.length; i++) {
//                            int heroId = player.heroAcq[i];
//                            // ??????army???
//                            if (!ids.contains(heroId)) {
//                                continue;
//                            }
//                            Hero hero = player.heros.get(heroId);
//                            if (hero != null) {
//                                hero.setState(HeroConstant.HERO_STATE_IDLE);
//                            }
//                            LogUtil.debug("--------------??????????????????????????? heroId: ", heroId);
//                        }
//                        LogUtil.debug("--------------???????????????????????? ??????  roleId:", player.roleId);
//                    }
                }
            } catch (Exception e) {
                LogUtil.error("???????????????????????????, roleIdId:" + player.lord.getLordId() + ", army:" + army + Arrays.toString(e.getStackTrace()));
            }
        }
    }

    /**
     * ??????????????????
     *
     * @param status ??????(0???????????? 1?????????)
     * @param army
     */
    public void synWallCallBackRs(int status, Army army) {
        if (army == null) {
            LogUtil.debug("????????????,army is null");
            return;
        }
        Player player = playerDataManager.getPlayer(army.getLordId());
        if (player == null) {
            LogUtil.debug("????????????,player is null," + army.getLordId());
            return;
        }
        // ??????????????????
        int armyCnt = army.getHero().get(0).getV2();
        Hero hero = player.heros.get(army.getHero().get(0).getV1());
        if (hero != null) {
            armyCnt = hero.getCount();
        }
        Base.Builder msg = PbHelper
                .createSynBase(SynWallCallBackRs.EXT_FIELD_NUMBER, SynWallCallBackRs.ext,
                        SynWallCallBackRs
                                .newBuilder().setStatus(status).setWallHero(PbHelper.createWallHeroPb(army,
                                        player.lord.getLevel(), player.lord.getNick(), army.getEndTime(), armyCnt))
                                .build());
        MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        LogUtil.debug("????????????target=" + army.getTarget() + ",player=" + army.getLordId() + ",status=" + status);
        Player target = worldDataManager.getPosData(army.getTarget());
        if (target == null) {
            LogUtil.debug("????????????,target is null," + army.getTarget());
            return;
        }
        MsgDataManager.getIns().add(new Msg(target.ctx, msg.build(), target.roleId));
    }

    /**
     * ????????????
     *
     * @param roleId
     * @param type   1??????????????????2??????????????????3????????????4??????????????????
     * @param keyId
     * @return
     * @throws MwException
     */
    public MoveCDRs moveCd(long roleId, int type, int keyId) throws MwException {
        // ????????????????????????
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (player.lord.getArea() > WorldConstant.AREA_MAX_ID) {
            return crossArmyService.moveCd(player, type, keyId);
        }
        Army army = player.armys.get(keyId);
        if (army == null) {
            throw new MwException(GameError.NO_CD_TIME.getCode(), "????????????????????????, roleId:,", roleId, ", type:", type);
        }
        if ((type == ArmyConstant.MOVE_TYPE_1 || type == ArmyConstant.MOVE_TYPE_2)
                && army.getState() != ArmyConstant.ARMY_STATE_RETREAT) {
            throw new MwException(GameError.NO_CD_TIME.getCode(), "??????????????????????????????, roleId:,", roleId, ", type:", type);
        }
        int now = TimeHelper.getCurrentSecond();
        if (now > army.getEndTime()) {
            throw new MwException(GameError.NO_CD_TIME.getCode(), "????????????????????????, roleId:,", roleId, ", type:", type);
        }
        int propId = 0;
        if (type == ArmyConstant.MOVE_TYPE_1) {
            propId = PropConstant.ITEM_ID_5011;
        } else if (type == ArmyConstant.MOVE_TYPE_2) {
            propId = PropConstant.ITEM_ID_5012;
        } else {
            throw new MwException(GameError.NO_CD_TIME.getCode(), "????????????????????????, roleId:,", roleId, ", type:", type);
        }

        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.PROP, propId, 1, AwardFrom.MOVE_CD);
        MoveCDRs.Builder builder = MoveCDRs.newBuilder();
        if (type == ArmyConstant.MOVE_TYPE_1) {
            army.setDuration(army.getDuration() / 2);
            army.setEndTime(now + (army.getEndTime() - now) / 2);
        } else if (type == ArmyConstant.MOVE_TYPE_2) {
            army.setDuration(1);
            army.setEndTime(now + 1);
        }

        // ??????????????????
        processMarchArmy(player.lord.getArea(), army, keyId);

        builder.setArmy(PbHelper.createArmyPb(army, false));
        builder.setStatus(1);
        builder.setProp(Prop.newBuilder().setPropId(propId).setCount(player.props.get(propId).getCount()));
        // ????????????
        // Player target = worldDataManager.getPosData(army.getTarget());
        // if (target != null && target.isLogin) {
        // syncAttackRole(target, player.lord, army.getEndTime(), WorldConstant.ATTACK_ROLE_0);
        // }
        return builder.build();
    }

    // ============================????????????start============================

    /**
     * ??????????????????
     */
    public InitiateGatherEntranceRs initiateGatherEntrance(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        InitiateGatherEntranceRs.Builder initiateGatherEntranceRs = InitiateGatherEntranceRs.newBuilder().setStatus(false);
        int schdule = worldScheduleService.getCurrentSchduleId();
        if (schdule < ScheduleConstant.SCHEDULE_ID_6) {
            LogUtil.error("?????????????????????! ??????????????????:" + schdule);
            if (player.getMixtureData().containsKey(PlayerConstant.WHETHER_ASSEMBLY_ENTRANCE)) {
                player.cleanMixtureData(PlayerConstant.WHETHER_ASSEMBLY_ENTRANCE);
            }
            return initiateGatherEntranceRs.build();
        }

        Camp camp = campService.getCampInfo(player.roleId, player.getCamp());
        // ?????????????????????????????????
        if (!camp.isInEceltion()) {
            LogUtil.error("????????????????????????????????????, status:" + camp.getStatus());
            if (player.getMixtureData().containsKey(PlayerConstant.WHETHER_ASSEMBLY_ENTRANCE)) {
                player.cleanMixtureData(PlayerConstant.WHETHER_ASSEMBLY_ENTRANCE);
            }
            return initiateGatherEntranceRs.build();
        }
        if (player.getMixtureData().containsKey(PlayerConstant.WHETHER_ASSEMBLY_ENTRANCE)) {
            initiateGatherEntranceRs.setStatus(player.getMixtureDataById(PlayerConstant.WHETHER_ASSEMBLY_ENTRANCE) != 0);
        } else {
            initiateGatherEntranceRs.setStatus(true);
            player.setMixtureData(PlayerConstant.WHETHER_ASSEMBLY_ENTRANCE, 1);
        }
        return initiateGatherEntranceRs.build();
    }

    /**
     * ????????????
     *
     * @param lordId
     * @return
     * @throws MwException
     */
    public SummonTeamRs summonTeam(GamePb2.SummonTeamRq req, long lordId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(lordId);

        // ???????????????????????????
        int job = player.lord.getJob();
        int privilege = PartyConstant.PRIVILEGE_CALL;
        if (!StaticPartyDataMgr.jobHavePrivilege(job, privilege)) {
            throw new MwException(GameError.NO_PRIVILEGE.getCode(), "??????????????????, roleId:", lordId, ", job:", job,
                    ", privilege:", privilege);
        }
        if (player.summon == null) {
            player.summon = new Summon();
        }

        Summon summon = player.summon;
        Date lastDate = new Date(summon.getLastTime() * 1000L);
        if (!DateHelper.isToday(lastDate)) {
            summon.setCount(0); // ????????????
        }
        // ???????????????????????????
        List<Integer> val = StaticPartyDataMgr.getJobPrivilegeVal(job, privilege);
        if (CheckNull.isEmpty(val) || val.size() != 3) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "????????????,????????????????????????");
        }
        int cnt = val.get(1); // ????????????
        int sum = val.get(2); // ??????????????????

        summon.setSum(sum);
        // ????????????
        int now = TimeHelper.getCurrentSecond();
        int endTime = summon.getLastTime() + Constant.SUMMON_KEEP_TIME;
        if (now > endTime || summon.getRespondId().size() >= sum) {
            summon.getRespondId().clear();
            summon.setStatus(0);
        }
        // ???????????????
        if (summon.getStatus() == 0) {
            if (req.hasTy() && req.getTy() == 1) {
                throw new MwException(GameError.SUMMON_NOT_OPEN.getCode(), String.format("roleId :%d, ?????????????????????, ??????????????? :%d", lordId, summon.getCount()));
            }
            if (summon.getCount() > cnt) {
                throw new MwException(GameError.SUMMON_COUNT_NOT_ENOUGH.getCode(), "roleId:", lordId, ", ???????????????",
                        summon.getCount(), " ??????????????????");
            }
            // ??????
            if (summon.getCount() > 0) {// ????????????
                if (CheckNull.isEmpty(Constant.SUMMON_NEED_PROP_CNT)
                        || Constant.SUMMON_NEED_PROP_CNT.get(job) == null) {
                    throw new MwException(GameError.NO_CONFIG.getCode(), "????????????????????????");
                }
                rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.PROP, PropConstant.ITEM_ID_5001,
                        Constant.SUMMON_NEED_PROP_CNT.get(job), AwardFrom.USE_PROP);
            }
            summon.setLastTime(now);
            summon.setStatus(1);
            summon.getRespondId().clear();
            summon.setCount(summon.getCount() + 1);
        }
        if (summon.getStatus() != 0) { // ???????????????????????????
            // ?????????
            chatDataManager.sendSysChat(ChatConst.CHAT_SUMMON_TEAM, player.lord.getCamp(), 0, player.lord.getJob(),
                    player.lord.getNick(), player.lord.getArea(), player.lord.getCamp(), player.lord.getPos(), lordId);
        }
        SummonTeamRs.Builder builder = SummonTeamRs.newBuilder();
        builder.setEndTime(summon.getLastTime() + Constant.SUMMON_KEEP_TIME);
        builder.setCount(summon.getCount());
        builder.setStatus(summon.getStatus());

        // ????????????????????????????????????
        if (player.lord.getArea() > WorldConstant.AREA_MAX_ID) {
            // ??????????????????
            int mapId = player.lord.getArea();
            CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(mapId);
            if (cMap != null) {
                cMap.publishMapEvent(MapEvent.mapEntity(player.lord.getPos(), MapCurdEvent.UPDATE));
            }
        } else {
            // ??????????????????
            List<Integer> posList = new ArrayList<>();
            posList.add(player.lord.getPos());
            EventBus.getDefault()
                    .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_AREA_TYPE));
        }
        //???????????????????????????
        this.sendSameCampAssembleInfo(player);
        //????????????????????????????????????banner
        if (player.getMixtureDataById(PlayerConstant.WHETHER_ASSEMBLY_ENTRANCE) != 0) {
            player.setMixtureData(PlayerConstant.WHETHER_ASSEMBLY_ENTRANCE, 0);
        }
        return builder.build();
    }

    /**
     * ???????????????????????????
     */
    private void sendSameCampAssembleInfo(Player player) throws MwException {
        //??????SyncRallyBattleRs????????????
        CommonPb.Assemble assemble = this.syncAssemblyInfo(player);
        //???????????????????????????????????????summonPlayer???????????????????????????????????????????????????????????????????????????
        Optional.ofNullable(playerDataManager.getPlayerByCamp(player.getCamp())).ifPresent(map -> {
            map.values().forEach(puPay -> {
                Base.Builder msg = PbHelper.createRsBase(GamePb3.SyncRallyBattleRs.EXT_FIELD_NUMBER, GamePb3.SyncRallyBattleRs.ext,
                        GamePb3.SyncRallyBattleRs.newBuilder().setAssemble(assemble).build());
                MsgDataManager.getIns().add(new Msg(puPay.ctx, msg.build(), puPay.roleId));
            });
        });
    }

    /**
     * ??????Assemble????????????
     */
    public CommonPb.Assemble syncAssemblyInfo(Player player) throws MwException {
        CommonPb.Assemble.Builder assemble = CommonPb.Assemble.newBuilder();
        assemble.setSponsorId(player.roleId);
        assemble.setSponsor(player.lord.getNick());
        assemble.setAreaId(player.lord.getArea());
        assemble.setOfficialPosition(player.lord.getJob());
        assemble.setPos(player.lord.getPos());
        assemble.setCount(player.summon.getRespondId().size());
        assemble.setSum(player.summon.getSum());
        assemble.setEndTime(player.summon.getLastTime() + Constant.SUMMON_KEEP_TIME);
        assemble.setCampId(player.getCamp());
        assemble.setPortrait(dressUpDataManager.curDressUpId(player, AwardType.PORTRAIT));
        assemble.setPortraitFrame(dressUpDataManager.curDressUpId(player, AwardType.PORTRAIT_FRAME));
        return assemble.build();
    }

    /**
     * ????????????
     *
     * @param lordId
     * @param summonId
     * @return
     * @throws MwException
     */
    public SummonRespondRs summonRespond(long lordId, long summonId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(lordId);
        Player summonPlayer = playerDataManager.checkPlayerIsExist(summonId);
        if (player.getDecisiveInfo().isDecisive()) {
            throw new MwException(GameError.DECISIVE_BATTLE_NO_SUMMON.getCode(), "???????????????????????????????????????");
        }
        if (lordId == summonId) {
            throw new MwException(GameError.SUMMON_PARAM_NOT_CONFORM.getCode(), "?????????????????????????????????");
        }
        if (player.lord.getCamp() != summonPlayer.lord.getCamp()) {
            throw new MwException(GameError.SUMMON_PARAM_NOT_CONFORM.getCode(), "???????????????,??????????????????");
        }
        if (summonPlayer.summon == null) {
            // ????????????????????????
            throw new MwException(GameError.SUMMON_NOT_OPEN.getCode(), "?????????????????????");
        }
        // ????????? ????????????????????????????????????
        int preArea = player.lord.getArea();
        int willGoArea = summonPlayer.lord.getArea();
        if (preArea > WorldConstant.AREA_MAX_ID ^ willGoArea > WorldConstant.AREA_MAX_ID) {
            throw new MwException(GameError.SUMMON_CANNOT_IN_WAR_FIRE.getCode(), "???????????????????????????????????? preArea:", preArea,
                    ", willGoArea:", willGoArea);
        }
        if ((preArea > WorldConstant.AREA_MAX_ID && willGoArea > WorldConstant.AREA_MAX_ID && preArea != willGoArea)) {
            throw new MwException(GameError.SUMMON_CANNOT_IN_WAR_FIRE.getCode(), "????????????????????????????????????????????? preArea:", preArea,
                    ", willGoArea:", willGoArea);
        }

        if (!CheckNull.isEmpty(player.armys)) {
            throw new MwException(GameError.MOVE_HERO_OUT.getCode(), "???????????????????????????, roleId:,", lordId);
        }
        // ???????????????
        int job = summonPlayer.lord.getJob();
        int privilege = PartyConstant.PRIVILEGE_CALL;
        if (!StaticPartyDataMgr.jobHavePrivilege(job, privilege)) {
            throw new MwException(GameError.NO_PRIVILEGE.getCode(), "????????????,????????????????????????, roleId:", lordId, ", job:", job,
                    ", privilege:", privilege);
        }
        if (!CheckNull.isEmpty(player.armys)) {
            throw new MwException(GameError.MOVE_HERO_OUT.getCode(), "?????????????????????????????????, roleId:,", lordId);
        }
        // ???????????????????????????
        List<Integer> val = StaticPartyDataMgr.getJobPrivilegeVal(summonPlayer.lord.getJob(), privilege);
        if (CheckNull.isEmpty(val) || val.size() != 3) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "????????????,????????????????????????, roleId:", lordId);
        }
        int sum = val.get(2); // ??????????????????
        // ????????????????????????
        Summon summon = summonPlayer.summon;
        int now = TimeHelper.getCurrentSecond();
        int endTime = summon.getLastTime() + Constant.SUMMON_KEEP_TIME;
        if (summon.getStatus() == 0 || now > endTime || summon.getRespondId().size() >= sum) {
            throw new MwException(GameError.SUMMON_NOT_OPEN.getCode(), "????????????,?????????????????????");
        }
        // ????????????????????????
        if (summonPlayer.lord.getArea() > WorldConstant.AREA_MAX_ID) {
            return crossSummonService.summonRespond(player, summonPlayer);
        } else { // ??????????????????
            // ????????????
            List<Integer> scope = MapHelper.getRoundPos(summonPlayer.lord.getPos(), Constant.SUMMON_RESPOND_RADIUS);
            if (scope.contains(player.lord.getPos())) {
                // ????????????????????????????????????
                throw new MwException(GameError.SUMMON_DISTANCE_TOO_CLOSE.getCode(), "??????????????????????????????");
            }
            // ????????????????????????????????????
            List<Integer> scope2 = MapHelper.getRoundPos(summonPlayer.lord.getPos(), Constant.SUMMON_RADIUS);
            scope2 = scope2.stream().filter(pos -> worldDataManager.isEmptyPos(pos)).collect(Collectors.toList());// ??????????????????
            if (CheckNull.isEmpty(scope2)) {
                throw new MwException(GameError.SUMMON_NOT_POS.getCode(), "????????????????????????");
            }
            // ????????????
            int prePos = player.lord.getPos();

            int newPos = scope2.get(RandomHelper.randomInSize(scope2.size()));
            int newArea = MapHelper.getAreaIdByPos(newPos);
            LogUtil.debug("roleId:", lordId, ", ?????????????????? area:", newArea, ", pos:", newPos);

            // ????????????????????????????????????????????????????????????
            if (player.battleMap.containsKey(prePos)) {// ????????????????????????????????????
                warService.cancelCityBattle(prePos, newPos, false, true);
            }

            StaticArea staticArea = StaticWorldDataMgr.getAreaMap().get(newArea);
            if (null == staticArea || !staticArea.isOpen()) {
                throw new MwException(GameError.AREA_NOT_OPEN.getCode(), "????????????????????????????????????, roleId:", lordId, ", area:",
                        newArea);
            }

            // ?????????????????????????????????
            List<Army> guardArrays = worldDataManager.getPlayerGuard(prePos);
            if (!CheckNull.isEmpty(guardArrays)) {
                for (Army army : guardArrays) {
                    Player tPlayer = playerDataManager.getPlayer(army.getLordId());
                    if (tPlayer == null || tPlayer.armys.get(army.getKeyId()) == null) {
                        LogUtil.error("lordId: {}, army keyId: {}, pos: {} not found!!! guard army will remove",
                                army.getLordId(), army.getKeyId(), army.getTarget());
                        continue;
                    }
                    retreatArmyByDistance(tPlayer, army, now);
                    synRetreatArmy(tPlayer, army, now);
                    worldDataManager.removePlayerGuard(army.getTarget(), army);
                    int heroId = army.getHero().get(0).getV1();
                    mailDataManager.sendNormalMail(tPlayer, MailConstant.MOLD_GARRISON_RETREAT, now, player.lord.getNick(),
                            heroId, player.lord.getNick(), heroId);
                }
                //????????????????????????
                if (CheckNull.nonEmpty(guardArrays)) {
                    LogUtil.error("clear roleId: {}, pos: {} all error guard army", lordId, prePos);
                    worldDataManager.removePlayerGuard(prePos);
                }
            }

            // ??????????????????
            if (playerDataManager.getPlayerByArea(player.lord.getArea()) != null) {
                playerDataManager.getPlayerByArea(player.lord.getArea()).remove(lordId);
            } else {
                LogUtil.debug(lordId + ",?????????????????????Map" + ",????????????area=" + player.lord.getArea() + ",newPos=" + newPos
                        + ",newArea=" + MapHelper.getAreaIdByPos(newPos));
            }
            player.lord.setPos(newPos);
            player.lord.setArea(MapHelper.getAreaIdByPos(newPos));
            worldDataManager.removePlayerPos(prePos, player);
            worldDataManager.putPlayer(player);

            // ????????????????????????
            summon.getRespondId().add(lordId);
            if (summon.getRespondId().size() >= sum) { // ?????????????????????
                //?????????????????????????????????????????????????????????
                GamePb3.SyncRallyBattleRs.Builder syncRallyBattleRs = GamePb3.SyncRallyBattleRs.newBuilder();
                syncRallyBattleRs.setAssemble(this.syncAssemblyInfo(summonPlayer));
                syncRallyBattleRs.setStatus(1);
                //???????????????????????????????????????summonPlayer???????????????????????????????????????????????????????????????????????????
                Optional.ofNullable(playerDataManager.getPlayerByCamp(player.getCamp())).ifPresent(map -> {
                    map.values().forEach(puPay -> {
                        Base.Builder msg = PbHelper.createRsBase(GamePb3.SyncRallyBattleRs.EXT_FIELD_NUMBER, GamePb3.SyncRallyBattleRs.ext, syncRallyBattleRs.build());
                        MsgDataManager.getIns().add(new Msg(puPay.ctx, msg.build(), puPay.roleId));
                    });
                });

                summon.setStatus(0);
                summon.getRespondId().clear();
            }
            // ??????????????????
            syncSummonState(summonPlayer, summonPlayer.lord.getLordId(), summon);
            SummonRespondRs.Builder builder = SummonRespondRs.newBuilder();
            builder.setPos(newPos);

            // ????????????????????????????????????
            List<Integer> posList = new ArrayList<>();
            posList.add(newPos);
            posList.add(prePos);
            posList.add(summonPlayer.lord.getPos());
            onPlayerPosChangeCallbcak(player, prePos, newPos, WorldConstant.CHANGE_POS_TYPE_3);
            EventBus.getDefault()
                    .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_AREA_TYPE));

            //?????????????????????????????? ??????????????????banner
            if (player.getMixtureDataById(PlayerConstant.WHETHER_ASSEMBLY_ENTRANCE) != 0) {
                player.setMixtureData(PlayerConstant.WHETHER_ASSEMBLY_ENTRANCE, 0);
            }
            return builder.build();
        }
    }

    /**
     * ??????????????????
     *
     * @param targer
     * @param summonId
     * @param summon
     */
    public void syncSummonState(Player targer, long summonId, Summon summon) {
        SyncSummonStateRs.Builder builder = SyncSummonStateRs.newBuilder();
        builder.setSummonId(summonId);
        int endTime = summon.getLastTime() + Constant.SUMMON_KEEP_TIME;
        builder.setEndTime(endTime);
        builder.addAllRespondId(summon.getRespondId());
        builder.setStatus(summon.getStatus());
        builder.setCount(summon.getCount());
        Base.Builder msg = PbHelper.createSynBase(SyncSummonStateRs.EXT_FIELD_NUMBER, SyncSummonStateRs.ext,
                builder.build());
        MsgDataManager.getIns().add(new Msg(targer.ctx, msg.build(), targer.roleId));
        LogUtil.debug("???????????????????????? targetRoleId=", targer.roleId, ", endTime:", endTime, ", status:" + summon.getStatus());
    }

    /**
     * ??????????????????
     *
     * @param lordId
     * @return
     * @throws MwException
     */
    public GetSummonRs getSummon(long lordId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(lordId);

        // ???????????????????????????
        int job = player.lord.getJob();
        int privilege = PartyConstant.PRIVILEGE_CALL;
        if (!StaticPartyDataMgr.jobHavePrivilege(job, privilege)) {
            throw new MwException(GameError.NO_PRIVILEGE.getCode(), "??????????????????, roleId:", lordId, ", job:", job,
                    ", privilege:", privilege);
        }
        if (player.summon == null) {
            player.summon = new Summon();
        }
        Summon summon = player.summon;
        Date lastDate = new Date(summon.getLastTime() * 1000L);
        if (!DateHelper.isToday(lastDate)) {
            summon.setCount(0); // ????????????
        }
        Integer battleCount = player.getMixtureData().get(PlayerConstant.DECISIVE_BATTLE_COUNT);
        LogUtil.debug("getSummon ???????????????:", summon.getCount());
        GetSummonRs.Builder builder = GetSummonRs.newBuilder();
        if (battleCount == null) {
            builder.setDecisiveBattleCount(0);
        } else {
            builder.setDecisiveBattleCount(battleCount);
        }
        builder.setCount(player.summon.getCount());
        return builder.build();
    }

    // ============================????????????end============================

    /**
     * ???????????? /????????????
     *
     * @param lordId
     * @param isEnter
     * @return
     * @throws MwException
     */
    public EnterWorldRs enterWorld(long lordId, boolean isEnter) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(lordId);
        EnterWorldRs.Builder builder = EnterWorldRs.newBuilder();
        if (isEnter) {// ????????????
            builder.addAllOpenArea(StaticWorldDataMgr.getOpenAreaIdSet());
            int now = TimeHelper.getCurrentSecond();
            // ????????????
            if (player.enterWorldCnt == 0 && now - player.lord.getOffTime() >= Constant.REFRESH_BANDITS_OFFLINE_TIME) {
                LogUtil.debug("????????????????????????4: roleId:", lordId, ", ");
                worldDataManager.processBanditByPlayer(player);
            }
            player.enterWorldCnt++;
        } else {// ????????????
            EventBus.getDefault().post(new Events.RmMapFocusEvent(player));
        }
        //???????????? ????????????????????????
        builder.setChristmas(activityChristmasService.isShowEffect(player) ? 1 : 0);
        //???????????????
//        builder.setShowConcert(musicFestivalCreativeService.showConcert(player));
        return builder.build();
    }

    /**
     * ?????????????????????????????????
     *
     * @param lordId
     * @param areaId
     * @return
     * @throws MwException
     */
    public ScreenAreaFocusRs screenAreaFocus(long lordId, int areaId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(lordId);
        EventBus.getDefault().post(new Events.AddAreaChangeEvent(player, areaId));
        ScreenAreaFocusRs.Builder builder = ScreenAreaFocusRs.newBuilder();
        return builder.build();
    }

    /**
     * ????????????????????????????????????
     *
     * @param lordId
     * @return
     * @throws MwException
     */
    public GetAreaCentreCityRs getAreaCentreCity(long lordId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(lordId);
        GetAreaCentreCityRs.Builder builder = GetAreaCentreCityRs.newBuilder();
        Map<Integer, StaticCity> cityMap = StaticWorldDataMgr.getMaxTypeCityByArea();
        BerlinWar berlinWar = BerlinWar.getInstance();
        cityMap.values().forEach(sCity -> {
            City city = worldDataManager.getCityById(sCity.getCityId());
            if (null != city) {
                Player cityOwner = playerDataManager.getPlayer(city.getOwnerId());
                if (sCity.getArea() == WorldConstant.AREA_TYPE_13) {
                    if (!CheckNull.isNull(berlinWar) && !CheckNull.isEmpty(berlinWar.getHistoryWinner())) {
                        cityOwner = playerDataManager.getPlayer(berlinWar.getHistoryWinner().get(0).getB());
                    }
                }
                builder.addCity(PbHelper.createAreaCityPb(player, city, cityOwner));
            }
        });
        GameGlobal gameGlobal = globalDataManager.getGameGlobal();
        Map<Integer, Integer> mixtureDataById = gameGlobal.getMixtureDataById(GlobalConstant.WORLD_AREA_FIRST_KILL_END_DISPLAY_TIME);
        Map<Integer, StaticArea> areaMap = StaticWorldDataMgr.getAreaMap();
        AtomicBoolean allDone = new AtomicBoolean(true);
        areaMap.values().forEach(sArea -> {
            Area area = worldDataManager.getAreaByAreaId(sArea.getArea());
            if (!CheckNull.isNull(area)) {
                StaticCity maxCity = StaticWorldDataMgr.getMaxTypeCityByArea(area.getArea());
                if (!CheckNull.isNull(maxCity)) {
                    if (area.getArea() == WorldConstant.AREA_TYPE_13 && !CheckNull.isNull(berlinWar)) {
                        if (!CheckNull.isEmpty(berlinWar.getHistoryWinner())) {
                            Player cityOwner = playerDataManager.getPlayer(berlinWar.getHistoryWinner().get(0).getB());
                            builder.addKillInfo(PbHelper.createKillInfo(cityOwner, berlinWar, maxCity));
                        } else {
                            allDone.set(false);
                        }
                    } else {
                        String cityInfo = maxCity.getType() + "_" + maxCity.getCityId();
                        Map<String, List<Long>> firstKillInfo = area.getFirstKillInfo(cityInfo);
                        try {
                            if (!CheckNull.isEmpty(firstKillInfo)) {
                                builder.addKillInfo(getCityFirstKill(cityInfo, firstKillInfo, area.getArea()));
                            } else {
                                allDone.set(false);
                            }
                        } catch (MwException e) {
                            LogUtil.error(e);
                        }
                    }
                }
            }
        });
        if (mixtureDataById.getOrDefault(0, 0) == 0 && allDone.get()) {
            mixtureDataById.put(0, TimeHelper.dateToSecond(TimeHelper.getSomeDayAfterOrBerfore(new Date(), 7 - 1, 23, 59, 59)));
            gameGlobal.setMixtureData(GlobalConstant.WORLD_AREA_FIRST_KILL_END_DISPLAY_TIME, mixtureDataById);
        }
        builder.setEndDisplay(mixtureDataById.getOrDefault(0, 0));
        return builder.build();
    }

    private CommonPb.CityAtkFirstKill getCityFirstKill(String cityInfo, Map<String, List<Long>> firstKillInfo, int area)
            throws MwException {
        CommonPb.CityAtkFirstKill.Builder builder = CommonPb.CityAtkFirstKill.newBuilder();
        builder.setArea(area);
        builder.setType(Integer.valueOf(cityInfo.split("_")[0]));
        builder.setCityId(Integer.valueOf(cityInfo.split("_")[1]));
        if (firstKillInfo.containsKey(WorldConstant.KILL_SPONSOR)) {
            List<Long> sponsor = firstKillInfo.get(WorldConstant.KILL_SPONSOR);
            if (!CheckNull.isEmpty(sponsor)) {
                Player player = playerDataManager.getPlayer(sponsor.get(0));
                if (!CheckNull.isNull(player)) {
                    builder.setCamp(player.lord.getCamp());
                    builder.setSponsor(
                            PbHelper.createAtkFirstKillInfo(playerDataManager.checkPlayerIsExist(sponsor.get(0))));
                }
            }
        }
        if (firstKillInfo.containsKey(WorldConstant.KILL_ATKLIST)) {
            List<Long> atklist = firstKillInfo.get(WorldConstant.KILL_ATKLIST);
            for (Long roleId : atklist) {
                builder.addAtklist(PbHelper.createAtkFirstKillInfo(playerDataManager.checkPlayerIsExist(roleId)));
            }
        }
        return builder.build();
    }

    /**
     * ?????????????????????????????????????????????
     *
     * @param roleId
     * @throws MwException
     */
    public GamePb4.GetCityFirstKillRs getCityFirstKill(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Area area = worldDataManager.getAreaByAreaId(player.lord.getArea());
        GamePb4.GetCityFirstKillRs.Builder builder = GamePb4.GetCityFirstKillRs.newBuilder();
        for (Entry<String, Map<String, List<Long>>> entry : area.getCityFirstKill().entrySet()) {
            builder.addKillInfo(createCityFirstKill(entry, area.getArea()));
        }
        return builder.build();
    }

    private CommonPb.CityAtkFirstKill createCityFirstKill(Entry<String, Map<String, List<Long>>> entry, int area)
            throws MwException {
        CommonPb.CityAtkFirstKill.Builder builder = CommonPb.CityAtkFirstKill.newBuilder();
        String[] cityInfo = entry.getKey().split("_");
        String cityType = cityInfo[0];
        String cityId = cityInfo[1];
        builder.setArea(area);
        builder.setType(Integer.valueOf(cityType));
        builder.setCityId(Integer.valueOf(cityId));
        if (entry.getValue().containsKey(WorldConstant.KILL_SPONSOR)) {
            List<Long> sponsor = entry.getValue().get(WorldConstant.KILL_SPONSOR);
            if (!CheckNull.isEmpty(sponsor)) {
                Player player = playerDataManager.getPlayer(sponsor.get(0));
                if (!CheckNull.isNull(player)) {
                    builder.setCamp(player.lord.getCamp());
                    builder.setSponsor(PbHelper.createAtkFirstKillInfo(player));
                }
            }
        }
        if (entry.getValue().containsKey(WorldConstant.KILL_ATKLIST)) {
            List<Long> atklist = entry.getValue().get(WorldConstant.KILL_ATKLIST);
            for (Long roleId : atklist) {
                Player player = playerDataManager.getPlayer(roleId);
                if (!CheckNull.isNull(player)) {
                    builder.addAtklist(PbHelper.createAtkFirstKillInfo(player));
                }
            }
        }
        return builder.build();
    }

    /**
     * ??????????????????????????????????????????????????????
     *
     * @param account
     * @param areaId
     * @param cityId
     * @param nick
     */
    public void pushAttackCamp(Account account, int areaId, int cityId, String nick) {
        String areaTxt = "s_area_" + areaId;
        String cityTxt = "s_city_" + cityId;
        String areaName = StaticIniDataMgr.getTextName(areaTxt);
        String cityName = StaticIniDataMgr.getTextName(cityTxt);
        if (null == areaName) {
            LogUtil.error("??????????????????????????????????????????, roleId:", account.getLordId(), ", cityId:", cityId);
            return;
        }

        // ?????? ??????
        // PushMessageUtil.pushMessage(account, PushConstant.ATTCK_CAMP_BATTLE, nick, areaName, cityName);
    }

}
