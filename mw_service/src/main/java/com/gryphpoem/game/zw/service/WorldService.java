package com.gryphpoem.game.zw.service;

import com.gryphpoem.cross.constants.FightCommonConstant;
import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.core.util.Turple;
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
import com.gryphpoem.game.zw.pb.*;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.CommonPb.BattleRole;
import com.gryphpoem.game.zw.pb.CommonPb.Prop;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.pb.GamePb1.SynWallCallBackRs;
import com.gryphpoem.game.zw.pb.GamePb2.*;
import com.gryphpoem.game.zw.pb.GamePb4.GetAreaCentreCityRs;
import com.gryphpoem.game.zw.pb.GamePb4.GetBattleByIdRq;
import com.gryphpoem.game.zw.pb.GamePb4.GetBattleByIdRs;
import com.gryphpoem.game.zw.pb.GamePb4.GetNightRaidInfoRs;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Fighter;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.*;
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
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.hero.PartnerHero;
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
 * @Description 世界相关, 主要获取地图数据，包涵区域城池玩家数据，行军路线；
 * 处理资源采集、打流寇、迁城、侦查，以及城战、营战的准备阶段，主要生成Battle对象，包涵攻方守方的数据，真正的战斗实现在warServic里面。
 * worldTimerLogic行军状态判断定时器，处理采集、打流寇、打任务流寇、点兵统领、驻防的行军状态
 * @date 创建时间：2017年3月30日 上午10:20:18
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
     * 获取某个行政区域的数据
     *
     * @param roleId
     * @param area
     * @return
     * @throws MwException
     */
    public GetAreaRs getArea(long roleId, int area) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        StaticArea staticArea = StaticWorldDataMgr.getAreaMap().get(area);
        if (null == staticArea || !staticArea.isOpen()) {
            throw new MwException(GameError.AREA_NOT_OPEN.getCode(), "获取分区数据，分区未开启, roleId:", roleId, ", area:", area);
        }
        worldDataManager.openPos(player);

        // 世界任务解锁
        // Area worldArea = worldDataManager.getAreaByAreaId(area);
        // if (worldArea.getStatus() == WorldConstant.AREA_STATUS_CLOSE) {
        // throw new MwException(GameError.AREA_NOT_OPEN.getCode(),
        // "获取分区数据，世界任务未解锁, roleId:", roleId, ", area:", area);
        // }

        // 返回协议
        GetAreaRs.Builder builder = GetAreaRs.newBuilder();
        // 获取分区中的玩家数据
        List<Player> playerList = worldDataManager.getPlayerInArea(area);
        if (!CheckNull.isEmpty(playerList)) {
            for (Player p : playerList) {
                builder.addForce(PbHelper.createAreaForcePb(p.lord.getPos(), p.lord.getCamp(), p.lord.getLevel(),
                        p.building.getCommand()));
            }
        }

        // 盖世太保
        List<Gestapo> gestapoByArea = worldDataManager.getGestapoByArea(area);
        if (!CheckNull.isEmpty(gestapoByArea)) {
            for (Gestapo gestapo : gestapoByArea) {
                builder.addGestapo(
                        PbHelper.createAreaGestapoPb(gestapo.getPos(), gestapo.getRoleId(), gestapo.getEndTime()));
            }
        }

        // 城池信息
        List<StaticCity> cityList = StaticWorldDataMgr.getCityByArea(area);
        if (CheckNull.isEmpty(cityList)) {
            LogUtil.error("分区城池未配置, area:", area);
        } else {
            City city;
            for (StaticCity staticCity : cityList) {
                city = worldDataManager.getCityById(staticCity.getCityId());
                if (null == city) {
                    LogUtil.error("有城池信息没有生成，重大错误!!!cityId:", staticCity.getCityId());
                } else {
                    Player cityOwner = playerDataManager.getPlayer(city.getOwnerId());
                    builder.addCity(PbHelper.createAreaCityPb(player, city, cityOwner));
                }
            }
        }

        // 获取玩家自己的行军线路
        for (Army army : player.armys.values()) {
            LogUtil.debug("roleId army=" + roleId + ":" + army);
            if (army.getState() == ArmyConstant.ARMY_STATE_RETREAT) {
                builder.addLine(PbHelper.createTwoIntPb(army.getTarget(), player.lord.getPos()));
            } else {
                builder.addLine(PbHelper.createTwoIntPb(player.lord.getPos(), army.getTarget()));
            }
        }

        // 获取柏林相关建筑
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
     * 获取区域内的行军路线
     *
     * @param roleId
     * @param area
     * @return
     */
    public GetMarchRs getMarch(long roleId, int area) {
        GetMarchRs.Builder builder = GetMarchRs.newBuilder();

        // 获取区域内所有玩家的行军路线
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
     * 获取自己被攻击的情报
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public AttackRolesRs getAttackRoles(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        AttackRolesRs.Builder builder = AttackRolesRs.newBuilder();

        // 玩家的区域和坐标
        int area = player.lord.getArea();
        int pos = player.lord.getPos();

        List<Battle> battleList = null;
        if (CrossWorldMapService.isOnCrossMap(player)) {
            // 获取跨服地图这个点的战斗
            CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(area);
            if (Objects.nonNull(cMap)) {
                List<BaseMapBattle> battlesByPos = cMap.getMapWarData().getBattlesByPos(pos);
                if (!CheckNull.isEmpty(battlesByPos)) {
                    battleList = battlesByPos.stream().map(BaseMapBattle::getBattle).collect(Collectors.toList());
                }
            }
        } else {
            // 获取这个点的战斗
            battleList = warDataManager.getBattlePosMap().get(pos);
        }
        if (!CheckNull.isEmpty(battleList)) {
            // 玩家的军情信息
            addAttackRoles(roleId, builder, battleList);
        }

        // if (!CheckNull.isEmpty(warDataManager.getSpecialBattleMap())) {
        // List<Battle> specialBattle =
        // warDataManager.getSpecialBattleMap().values().stream() //
        // 加上被反攻德意志BOSS进攻的军情
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

        // 匪军叛乱
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
     * 获取地图中某个区块的数据
     *
     * @param roleId
     * @param blockList
     * @return
     * @throws MwException
     */
    public GetMapRs getMap(long roleId, List<Integer> blockList) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        // 返回协议
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
            // 获取盖世太保
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
                        LogUtil.error("获取盖世太保，有盖世太保未配置, StaticGestapoPlanId:", gestapo.getGestapoId());
                        continue;
                    }
                    builder.addForce(PbHelper.createMapForce(pos, WorldConstant.FORCE_TYPE_GESTAPO,
                            gestapo.getGestapoId(), null, -1, null, -1, battle, 0, battleTime));
                }
            }

            // 获取点兵统领
            cabinetLeadMap = worldDataManager.getCabinetLeadInBlock(block);
            if (!CheckNull.isEmpty(cabinetLeadMap)) {
                for (Entry<Integer, CabinetLead> entry : cabinetLeadMap.entrySet()) {
                    CabinetLead lead = entry.getValue();
                    StaticCabinetPlan cabinetPlan = StaticBuildingDataMgr.getCabinetPlanById(lead.getCabinetPlanId());
                    if (null == cabinetPlan) {
                        LogUtil.error("获取点兵统领，有点兵统领未配置, StaticCabinetPlanId:", lead.getCabinetPlanId());
                        continue;
                    }
                    builder.addForce(PbHelper.createMapForce(entry.getKey(), WorldConstant.FORCE_TYPE_CABINET_LEAD,
                            cabinetPlan.getBanditId(), null, -1, null, -1, false, 0, 0));
                }
            }

            // 获取流寇
            banditMap = worldDataManager.getBanditInBlock(block);
            if (!CheckNull.isEmpty(banditMap)) {
                for (Entry<Integer, Integer> entry : banditMap.entrySet()) {
                    bandit = StaticBanditDataMgr.getBanditMap().get(entry.getValue());
                    if (null == bandit) {
                        LogUtil.error("获取流寇，有流寇数据未配置, banditId:", entry.getValue());
                        continue;
                    }

                    builder.addForce(PbHelper.createMapForce(entry.getKey(), WorldConstant.FORCE_TYPE_BANDIT,
                            bandit.getBanditId(), null, -1, null, -1, false, 0, 0));
                }
            }

            // 获取矿点
            mineMap = worldDataManager.getMineInBlock(block);
            if (!CheckNull.isEmpty(mineMap)) {
                int pos;
                int resource;
                Guard guard;
                CommonPb.Collect.Builder collect;
                for (Entry<Integer, Integer> entry : mineMap.entrySet()) {
                    mine = StaticWorldDataMgr.getMineMap().get(entry.getValue());
                    if (null == mine) {
                        LogUtil.error("获取矿点，有矿点数据未配置, mineId:", entry.getValue());
                        continue;
                    }

                    pos = entry.getKey();
                    if (worldDataManager.hasGuard(pos)) {// 如果有 玩家在采集，矿点采集信息
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
            // 获取超级矿点
            List<SuperMine> superMine = superMineService.getSuperMineByBlock(block);
            for (SuperMine sm : superMine) {
                builder.addForce(PbHelper.createMapForcePb(sm));
            }
            // 获取飞艇
            List<AirshipWorldData> airshipList = airshipService.getAirshipByBlock(block);
            if (!CheckNull.isEmpty(airshipList)) {
                for (AirshipWorldData aswd : airshipList) {
                    builder.addForce(PbHelper.createMapForcePb(aswd, playerDataManager));
                }
            }
/*            // 获取世界boss
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
            // 获取玩家
            playerList = worldDataManager.getPlayerInBlock(block);
            Effect effect;
            for (Player p : playerList) {
                effect = p.getEffect().get(EffectConstant.PROTECT);
                // 坐标点迁移后反攻战斗标识
                boolean battle = warDataManager.posHaveBattle(p.lord.getPos());
                int prot = (effect != null && effect.getEndTime() > now) ? 1 : 0;
                int protTime = effect != null ? effect.getEndTime() : 0;
                boolean showSummon = p.summon != null && p.summon.getStatus() != 0
                        && now < p.summon.getLastTime() + Constant.SUMMON_KEEP_TIME
                        && p.lord.getCamp() == player.lord.getCamp();
                int rebelRoundId = rebelService.getRoundIdByPlayer(p);
                builder.addForce(PbHelper.createMapForceByPlayer(p, battle, prot, showSummon, protTime, rebelRoundId));
            }

            // 获取城池
            cityList = StaticWorldDataMgr.getCityInBlock(block);
            if (!CheckNull.isEmpty(cityList)) {
                CommonPb.MapCity mapCity;
                for (StaticCity staticCity : cityList) {
                    city = worldDataManager.getCityById(staticCity.getCityId());
                    if (city == null) {
                        LogUtil.error("获取城池 city null==" + staticCity.getCityId());
                        continue;
                    }
                    owner = playerDataManager.getPlayer(city.getOwnerId());
                    // 如果城主没有找到 就直接清空
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
            // 获取柏林相关建筑
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
            // 圣坛地图上的数据
            ramadanVisitAltarService.getMap(block, builder);

            //遗迹
            DataResource.getBean(RelicService.class).getMapForce(block, builder);
        }
        builder.addAllBlock(blockList);
        return builder.build();
    }

    /**
     * 计算矿点剩余资源数
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
     * 获取矿点采集详情
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
                if (guard.getRoleId() == roleId) {// 如果是自己采集的矿，返回玩家已采集到的资源数
                    builder.setGrab(total - surplus);
                }
            } else {
                builder.setResource(total);
            }
        }

        return builder.build();

    }

    /**
     * 攻击某个坐标的势力（包括玩家和流寇） 打城战会分闪电、奔袭、远征，只有闪电战是不允许支援，战斗都在有效时间内才能加入,会根据官职消耗体力或者令牌,
     * 发起战斗后，生成Battle对象，进入战斗倒计时，倒计时会在warService触发战斗
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public AttackPosRs attackPos(long roleId, AttackPosRq req) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int pos = req.getPos();

        if (worldDataManager.isEmptyPos(pos)) {
            throw new MwException(GameError.EMPTY_POS.getCode(), "该坐标为空闲坐标，不能攻击或采集, roleId:", roleId, ", pos:", pos);
        }

        List<PartnerHero> heroIdList = new ArrayList<>();
        heroIdList.addAll(req.getHeroIdList().stream().distinct().map(heroId ->
                player.getPlayerFormation().getPartnerHero(heroId)).filter(pa -> !HeroUtil.isEmptyPartner(pa)).collect(Collectors.toList()));

        // 检查出征将领信息
        checkFormHeroSupport(player, heroIdList, pos);

        Hero hero;
        int armCount = 0;
        int defArmCount = 0;
        List<CommonPb.PartnerHeroIdPb> form = new ArrayList<>();
        for (PartnerHero partnerHero : heroIdList) {
            if (HeroUtil.isEmptyPartner(partnerHero)) continue;
            form.add(partnerHero.convertTo());
            armCount += partnerHero.getPrincipalHero().getCount();
        }

        // 各势力类型的条件判断
        int type = 0;
        int battleId = 0;
        int targetId = 0;
        long tarLordId = 0;
        int battleType = 0;// 1 闪电战，2 奔袭战，3 远征战
        int now = TimeHelper.getCurrentSecond();
        int marchTime = marchTime(player, pos, worldDataManager.getBanditIdByPos(pos) > 0 ||
                worldDataManager.getAirshipWorldDataMap().get(pos) != null);
        // 记录行军消耗
        List<Award> marchConsume = new ArrayList<>();
        // 子类型
        int subType = 0;

        // 战斗最长距离
        // if (marchTime > WorldConstant.ATK_MAX_TIME) {
        // throw new MwException(GameError.ATK_MAX_TIME.getCode(), "超过最大距离,
        // roleId:", roleId, ", pos:",
        // pos + ",marchTime=" + marchTime);
        // }

        int needFood = checkMarchFood(player, marchTime, armCount); // 检查补给

        Battle battle = null;
        int reqType = req.getType();
        if (worldDataManager.isPlayerPos(pos)) {// 攻击玩家
            Player target = worldDataManager.getPosData(pos);
            // 同阵营不允许战斗
            if (target.lord.getCamp() == player.lord.getCamp()) {
                throw new MwException(GameError.SAME_CAMP.getCode(), "同阵营，不能攻击, roleId:", roleId, ", pos:", pos);
            }

            // 决战中不能发起进攻
            if (player.getDecisiveInfo().isDecisive() || target.getDecisiveInfo().isDecisive()) {
                throw new MwException(GameError.DECISIVE_BATTLE_NO_ATK.getCode(), "决战中，不能发起攻击, roleId:", roleId,
                        ", pos:", pos);
            }
            // 对方开启保护
            Effect effect = target.getEffect().get(EffectConstant.PROTECT);
            if (effect != null && effect.getEndTime() > now) {
                throw new MwException(GameError.PROTECT.getCode(), "该坐标开启保护，不能攻击, roleId:", roleId, ", pos:",
                        pos + ",tarRoleId:" + target.roleId);
            }

            // 城战类型
            battleType = req.hasType() ? reqType : WorldConstant.CITY_BATTLE_BLITZ;// 默认闪电战

            // 战斗即将开始时间(默认的规则)
            int battleMarchTime = marchTime;
            if (marchTime <= WorldConstant.MARCH_UPPER_LIMIT_TIME.get(0)) {// 行军时间小于5分钟
                if (WorldConstant.CITY_BATTLE_RAID == battleType) {
                    battleMarchTime = marchTime + WorldConstant.CITY_BATTLE_INCREASE_TIME;
                } else if (WorldConstant.CITY_BATTLE_EXPEDITION == battleType) {
                    battleMarchTime = marchTime + (WorldConstant.CITY_BATTLE_INCREASE_TIME * 2);
                }
            } else if (marchTime <= WorldConstant.MARCH_UPPER_LIMIT_TIME.get(1)
                    && marchTime > WorldConstant.MARCH_UPPER_LIMIT_TIME.get(0)) { // 大于分钟
                // 小于10分钟时
                if (WorldConstant.CITY_BATTLE_EXPEDITION == battleType) {
                    battleMarchTime = marchTime + WorldConstant.CITY_BATTLE_INCREASE_TIME;
                }
            }

            StaticArea targetSArea = StaticWorldDataMgr.getAreaMap().get(target.lord.getArea());
            StaticArea mySArea = StaticWorldDataMgr.getAreaMap().get(player.lord.getArea());
            if (mySArea.getOpenOrder() == WorldConstant.AREA_ORDER_3) { // 自己在皇城区域,任何地方都可以打
                if (targetSArea.getOpenOrder() != WorldConstant.AREA_ORDER_3) {
                    // 皇城打下面州郡的情况(特殊情况)
                    marchTime = WorldConstant.SPECIAL_MARCH_TIME;
                    if (WorldConstant.CITY_BATTLE_RAID == battleType) {
                        battleMarchTime = WorldConstant.SPECIAL_BATTLE_TIME.get(1);
                    } else if (WorldConstant.CITY_BATTLE_EXPEDITION == battleType) {
                        battleMarchTime = WorldConstant.SPECIAL_BATTLE_TIME.get(2);
                    } else {// 闪电战
                        battleMarchTime = WorldConstant.SPECIAL_BATTLE_TIME.get(0);
                    }
                } else {
                    checkMarchTimeByType(player, marchTime, battleMarchTime);
                }
            } else if (mySArea.getOpenOrder() == WorldConstant.AREA_ORDER_2) {// 自己在州的情况,州只能打州
                if (targetSArea.getOpenOrder() != WorldConstant.AREA_ORDER_2) {
                    throw new MwException(GameError.CAMP_BATTLE_AREA_ERROR.getCode(), "跨区域不允许,发起个人战, roleId:", roleId,
                            ", my area:", mySArea.getArea(), ", target area:", target.lord.getArea());
                }
                checkMarchTimeByType(player, marchTime, battleMarchTime);

            } else if (mySArea.getOpenOrder() == WorldConstant.AREA_ORDER_1) { // 自己在郡只能打本区域的
                if (targetSArea.getArea() != mySArea.getArea()) {
                    throw new MwException(GameError.CAMP_BATTLE_AREA_ERROR.getCode(), "跨区域不允许,发起个人战, roleId:", roleId,
                            ", my area:", mySArea.getArea(), ", target area:", target.lord.getArea());
                }
                checkMarchTimeByType(player, marchTime, battleMarchTime);
            }
            // 对方开启自动补兵
            playerDataManager.autoAddArmy(target);
            // 检测战斗消耗
            checkPower(player, battleType, marchTime);
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.RESOURCE, AwardType.Resource.FOOD, needFood,
                    AwardFrom.ATK_POS);

            type = ArmyConstant.ARMY_TYPE_ATK_PLAYER;

            for (PartnerHero partnerHero : target.getPlayerFormation().getHeroBattle()) {
                if (HeroUtil.isEmptyPartner(partnerHero)) continue;
                hero = partnerHero.getPrincipalHero();
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

            // 通知被攻击玩家
            if (target.isLogin) {
                syncAttackRole(target, player.lord, battle.getBattleTime(), WorldConstant.ATTACK_ROLE_1);
            }

            // 给被攻击玩家推送消息（应用外推送）
            // PushMessageUtil.pushMessage(target.account, PushConstant.ID_ATTACKED, target.lord.getNick(),
            //         player.lord.getNick());
            // 给被攻击玩家推送消息（应用外推送）
            PushMessageUtil.pushMessage(target.account, PushConstant.ENEMIES_ARE_APPROACHING);
            // 保护取消
            removeProTect(player, AwardFrom.ATTACK_PLAYER_BATTLE, battle.getPos());
        } else if (worldDataManager.isCabinetLeadPos(pos)) {// 攻击点兵统领
            checkSameArea(player, pos);
            type = ArmyConstant.ARMY_TYPE_ATK_CABINET_LEAD;
            targetId = worldDataManager.getCabinetLeadByPos(pos).getCabinetPlanId();
            // 检查补给
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.RESOURCE, AwardType.Resource.FOOD, needFood,
                    AwardFrom.ATK_POS);
        } else if (worldDataManager.isBanditPos(pos)) {// 攻击流寇
            if (player.getBanditCnt() >= Constant.ATTACK_BANDIT_MAX) {
                throw new MwException(GameError.BANDIT_OVER_MAX_CNT.getCode(), "攻打流寇已超过上限:", roleId, ", BanditCnt:",
                        player.getBanditCnt());
            }
            int banditId = worldDataManager.getBanditIdByPos(pos);
            StaticBandit staticBandit = StaticBanditDataMgr.getBanditMap().get(banditId);
            if (null == staticBandit) {
                LogUtil.error("流寇id未配置, banditId:", banditId);
                throw new MwException(GameError.EMPTY_POS.getCode(), "攻打流寇，不存在, roleId:", roleId, ", pos:", pos);
            }
            Integer banditLv = player.trophy.get(TrophyConstant.TROPHY_1);
            banditLv = banditLv != null ? banditLv : 0;
            if (staticBandit.getLv() > banditLv + 1) {
                throw new MwException(GameError.BANDIT_LV_ERROR.getCode(), "不能跨级打流寇, roleId:", roleId, ", pos:", pos);
            }
            checkSameArea(player, pos);
            type = ArmyConstant.ARMY_TYPE_ATK_BANDIT;
            targetId = worldDataManager.getBanditIdByPos(pos);
            // 检查补给
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.RESOURCE, AwardType.Resource.FOOD, needFood,
                    AwardFrom.ATK_POS);
            // 多人叛军行军加成[%]
            marchTime = airshipService.getAirShipMarchTime(player, marchTime);
            // 有打流寇任务
            if (checkCurTaskHasBandit(staticBandit.getLv(), banditLv)) {
                marchTime = Constant.ATTACK_BANDIT_MARCH_TIME;
            }

        } else if (worldDataManager.isMinePos(pos)) {// 采矿
            // 判断采集队伍上限
            int cnt = 0;
            for (Army army : player.armys.values()) {
                if (army.getType() == ArmyConstant.ARMY_TYPE_COLLECT
                        || army.getType() == ArmyConstant.ARMY_TYPE_COLLECT_SUPERMINE) {
                    cnt++;
                }
                if (army.getTarget() == pos && army.getState() != ArmyConstant.ARMY_STATE_RETREAT) {
                    throw new MwException(GameError.ALREADY_COLLECT_HERO.getCode(), "该矿点已经派出将领进行采集, roleId:", roleId);
                }
            }
            if (cnt >= WorldConstant.MINE_MAX_CNT) {
                throw new MwException(GameError.MINE_MAX_NUM.getCode(), "已达将领达到采集上限, roleId:", roleId, ", pos:", pos);
            }

            checkCollectSanctuaryMine(player, pos);
            checkCollectMineCount(player, pos, roleId, vipDataManager.getCollectMineCount(player.lord.getVip()));

            checkSameArea(player, pos);
            type = ArmyConstant.ARMY_TYPE_COLLECT;
            // 当矿点已经有人采集了 破罩子
            boolean mineHasPlayer = false; // 矿点是是否有玩家
            if (mineHasPlayer = worldDataManager.hasGuard(pos)) {
                // 检道具是否充足
                com.gryphpoem.game.zw.resource.pojo.Prop prop = player.props.get(PropConstant.ITEM_ID_5043);
                if (null == prop || prop.getCount() < 1) {
                    throw new MwException(GameError.ATTACK_MINE_PROP_NOT_ENOUGH.getCode(), " 进攻资源田消耗道具不足 roleId:",
                            roleId);
                }

                Guard guard = worldDataManager.getGuardByPos(pos);
                if (guard.getPlayer().roleId != roleId) {
                    removeProTect(player, AwardFrom.COLLECT_WAR, pos); // 移除保护罩
                    // 通知被攻击玩家
                    defArmCount = guard.getArmy().getArmCount();

                    battle = createMineBattle(guard.getPlayer(), player, pos, armCount, defArmCount, now, marchTime);
                    battleId = battle.getBattleId();
                }
            }
            targetId = worldDataManager.getMineByPos(pos).getMineId();
            List<List<Integer>> needCost = combineAttackMineCost(needFood, mineHasPlayer);
            rewardDataManager.checkAndSubPlayerRes(player, needCost, AwardFrom.ATK_POS);

            if (checkAddCollectMineCount(player, pos)) {
                player.addCollectMineCount();
            }
            //将玩家加入采矿队列
            worldDataManager.addCollectMine(roleId, pos);
        } else if (worldDataManager.isAltarPos(pos)) {
            ramadanVisitAltarService.visitAltar(pos, roleId, reqType, marchConsume);
            // 检查补给
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.RESOURCE, AwardType.Resource.FOOD, needFood, AwardFrom.ATK_POS);
            type = ArmyConstant.ARMY_TYPE_ALTAR;
            subType = reqType;
        } else if (worldDataManager.isRelicPos(pos)) {
            //探索遗迹
            DataResource.getBean(RelicService.class).checkArmy(player, pos);
            type = ArmyConstant.ARMY_TYPE_RELIC_BATTLE;
            //遗迹行军时间减半
            marchTime = (int) (marchTime * ActParamConstant.RELIC_MARCH_SPEEDUP / NumberUtil.TEN_THOUSAND_DOUBLE);
        } else {
            throw new MwException(GameError.ATTACK_POS_ERROR.getCode(), "AttackPos，坐标不正确, roleId:", roleId, ", pos:",
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
        // 名城buffer记录
        army.setOriginCity(worldDataManager.checkCityBuffer(player.lord.getPos()));
        army.setHeroMedals(heroIdList.stream()
                .map(pa -> medalDataManager.getHeroMedalByHeroIdAndIndex(player, pa.getPrincipalHero().getHeroId(), MedalConst.HERO_MEDAL_INDEX_0))
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
        // 添加行军路线
        March march = new March(player, army);
        worldDataManager.addMarch(march);

        // 改变行军状态
        for (PartnerHero pa : heroIdList) {
            pa.setState(ArmyConstant.ARMY_STATE_MARCH);
        }

        // 返回协议
        AttackPosRs.Builder builder = AttackPosRs.newBuilder();

        builder.setArmy(PbHelper.createArmyPb(army, false));
        if (battle != null) {
            builder.setBattle(PbHelper.createBattlePb(battle));
        }
        builder.setCollectMineCount(player.getCollectMineCount());

        // 区域变化推送
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
     * 检测行军补给
     *
     * @param player
     * @param marchTime
     * @param armCount
     * @throws MwException
     */
    public int checkMarchFood(Player player, int marchTime, int armCount) throws MwException {
        // 检查补给
        int needFood = getNeedFood(marchTime, armCount);
        if (needFood > player.resource.getFood()) {
            throw new MwException(GameError.RESOURCE_NOT_ENOUGH.getCode(), "行军补给不足, roleId:", player.roleId,
                    "needFood:", needFood, " haveFood:", player.resource.getFood());
        }
        return needFood;
    }

    /**
     * 校驗攻擊位置次數
     *
     * @param player
     * @param pos
     */
    public void checkCollectMineCount(Player player, int pos, long roleId, int vipCollectMineCount) throws MwException {
        //采集次数校验
        if (!checkAddCollectMineCount(player, pos)) {
            return;
        }

        if (player.getCollectMineCount() >= vipCollectMineCount) {
            throw new MwException(GameError.COLLECT_MINE_OVER_MAX_CNT.getCode(), "每日攻打同阵营采集已超过上限:", roleId, ", collectMineCount:",
                    player.getCollectMineCount());
        }
    }

    private boolean checkCollectSanctuaryMine(Player player, int pos) throws MwException {
        // 柏林区域的采集权判断
//            if (MapHelper.getAreaIdByPos(pos) == WorldConstant.AREA_TYPE_13 && MapHelper.isInKingCityAreaPos(
//                    StaticWorldDataMgr.getCityMap().get(WorldConstant.BERLIN_CITY_ID).getCityPos(), pos)) {
//                BerlinWar berlinWar = BerlinWar.getInstance();
//                BerlinCityInfo berlinCityInfo = berlinWar.getBerlinCityInfo();
//                if (!CheckNull.isNull(berlinWar) && berlinWar.getStatus() != WorldConstant.BERLIN_STATUS_OPEN
//                        && !CheckNull.isNull(berlinCityInfo) && berlinCityInfo.getCamp() != Constant.Camp.NPC) {
//                    if (berlinCityInfo.getCamp() != player.lord.getCamp()) {
//                        throw new MwException(GameError.BERLIN_MINE_CANT_COLLECT.getCode(),
//                                "柏林区域的资源点, 只有占领方才能采集, berlinCamp:", berlinCityInfo.getCamp(), ", playerCamp",
//                                player.lord.getCamp());
//                    }
//                }
//            }

        // 柏林区域(圣域）的采集权判断
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
                    "柏林区域的资源点, 只有占领方才能采集, berlinCamp:", berlinCityInfo.getCamp(), ", playerCamp",
                    player.lord.getCamp());
        }

        return true;
    }

    /**
     * 校验是否可以增加采集次数
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

        //非同阵营不增加次数
        if (defPlayer.lord.getCamp() != player.lord.getCamp()) {
            return false;
        }

        return true;
    }

    /**
     * 检测行军时间是否满足行军类型
     *
     * @param marchTime
     * @param battleType
     * @throws MwException
     */
    private void checkMarchTimeByType(Player player, int marchTime, int battleType) throws MwException {
        if (WorldConstant.CITY_BATTLE_BLITZ == battleType) {// 闪电战不能超过
            if (marchTime > WorldConstant.MARCH_UPPER_LIMIT_TIME.get(0)) {
                throw new MwException(GameError.ATK_MAX_TIME.getCode(), "超过战斗类型最大距离最大距离, roleId:", player.roleId,
                        ",marchTime=" + marchTime, ", battleType=", battleType);
            }
        } else if (WorldConstant.CITY_BATTLE_RAID == battleType) {
            if (marchTime > WorldConstant.MARCH_UPPER_LIMIT_TIME.get(1)) {
                throw new MwException(GameError.ATK_MAX_TIME.getCode(), "超过战斗类型最大距离最大距离, roleId:", player.roleId,
                        ",marchTime=" + marchTime, ", battleType=", battleType);
            }
        }
    }

    /**
     * 攻击任务怪，任务怪是由客户端根据当前任务生成，服务器只判断任务是否存在，存在则生成对应等级的流寇，并进行进军，进军结束会触发战斗
     *
     * @param roleId
     * @param pos
     * @param lv
     * @param heroIdList
     * @return
     * @throws MwException
     */
    public AttackPosRs attackPos4Task(long roleId, int pos, int lv, List<Integer> heroIdList) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // 是否有任务流寇
        // 检查出征将领信息
        // checkFormHero(player, heroIdList);
        List<PartnerHero> heroIdList_ = new ArrayList<>();
        heroIdList_.addAll(heroIdList.stream().distinct().map(heroId ->
                player.getPlayerFormation().getPartnerHero(heroId)).filter(pa -> !HeroUtil.isEmptyPartner(pa)).collect(Collectors.toList()));

        checkFormHeroSupport(player, heroIdList_, pos);
        if (player.getBanditCnt() >= Constant.ATTACK_BANDIT_MAX) {
            throw new MwException(GameError.BANDIT_OVER_MAX_CNT.getCode(), "攻打流寇已超过上限:", roleId, ", BanditCnt:",
                    player.getBanditCnt());
        }
        List<StaticBandit> banditList = StaticBanditDataMgr.getBanditByLv(lv);
        if (CheckNull.isEmpty(banditList)) {
            LogUtil.error("该等级没有配置流寇, lv:" + lv);
            throw new MwException(GameError.EMPTY_POS.getCode(), "攻打流寇，不存在, roleId:", roleId, ", pos:", pos);
        }

        StaticBandit staticBandit = banditList.get(RandomHelper.randomInSize(banditList.size()));
        if (null == staticBandit) {
            LogUtil.error("流寇id未配置, banditId:", banditList);
            throw new MwException(GameError.EMPTY_POS.getCode(), "攻打流寇，不存在, roleId:", roleId, ", pos:", pos);
        }

        Integer banditLv = player.trophy.get(TrophyConstant.TROPHY_1);
        banditLv = banditLv != null ? banditLv : 0;
        if (WorldConstant.BANDIT_LV_999 != lv) {
            if (staticBandit.getLv() > banditLv + 1) {
                throw new MwException(GameError.BANDIT_LV_ERROR.getCode(), "不能跨级打流寇, roleId:", roleId, ", pos:", pos,
                        ",staticBandit=" + staticBandit, "banditLv=" + banditLv);
            }
        }

        Hero hero;
        int armCount = 0;
        List<CommonPb.PartnerHeroIdPb> form = new ArrayList<>();
        for (PartnerHero partnerHero : heroIdList_) {
            if (HeroUtil.isEmptyPartner(partnerHero)) continue;
            form.add(partnerHero.convertTo());
            armCount += partnerHero.getPrincipalHero().getCount();
        }

        // 各势力类型的条件判断
        int battleId = 0;
        int targetId = staticBandit.getBanditId();
        int now = TimeHelper.getCurrentSecond();
        int marchTime = marchTime(player, pos);

        // 战斗最长距离
        if (marchTime > WorldConstant.ATK_MAX_TIME) {
            throw new MwException(GameError.ATK_MAX_TIME.getCode(), "超过最大距离, roleId:", roleId, ", pos:",
                    pos + ",marchTime=" + marchTime);
        }

        // 检查补给
        int needFood = getNeedFood(marchTime, armCount);
        if (needFood > player.resource.getFood()) {
            throw new MwException(GameError.RESOURCE_NOT_ENOUGH.getCode(), "行军补给不足, roleId:", roleId, ", pos:", pos);
        }
        Battle battle = null;

        int type = ArmyConstant.ARMY_TYPE_ATK_CABINET_TASK;
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.RESOURCE, AwardType.Resource.FOOD, needFood,
                AwardFrom.ATK_POS);

        if (checkCurTaskHasBandit(staticBandit.getLv(), banditLv) || WorldConstant.BANDIT_LV_999 == lv) {
            marchTime = Constant.ATTACK_BANDIT_MARCH_TIME;// 有打流寇任务 ,秒去
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
        // 攻击玩家, 加入部队逻辑全部放入到达后加入队列
        // if (worldDataManager.isPlayerPos(pos) && battle != null) {
        // addBattleArmy(battle, player.roleId, heroIdList, army.getKeyId(),
        // true);
        // }

        // 添加行军路线
        March march = new March(player, army);
        worldDataManager.addMarch(march);

        // 改变行军状态
        for (PartnerHero partnerHero : heroIdList_) {
            partnerHero.setState(ArmyConstant.ARMY_STATE_MARCH);
        }

        // 返回协议
        AttackPosRs.Builder builder = AttackPosRs.newBuilder();
        builder.setArmy(PbHelper.createArmyPb(army, false));

        synRetreatArmy(player, army, now);
        return builder.build();
    }

    public void checkSameArea(Player player, int pos) throws MwException {
        StaticArea staticArea = StaticWorldDataMgr.getAreaMap().get(MapHelper.getAreaIdByPos(pos));
        if (staticArea.getOpenOrder() < 1
                || staticArea.getArea() != StaticWorldDataMgr.getAreaMap().get(player.lord.getArea()).getArea()) {
            throw new MwException(GameError.CAMP_BATTLE_AREA_ERROR.getCode(), "跨区域不允许,发起个人战, roleId:",
                    player.lord.getLordId(), ", target area:", staticArea.getArea());
        }
    }

    /**
     * 攻打玩家消耗体力或者城战令
     *
     * @param player
     * @param battleType
     * @param marchTime
     * @throws MwException
     */
    public void checkPower(Player player, int battleType, int marchTime) throws MwException {
        int maxTime = 0;// 最大行军时间, 超过此时间不能攻打
        int costItemNum = 0;// 消耗城战令
        int costPower = 0;// 消耗体力
        int jobPrivilege = 0;// 军团职位特权
        // List<Integer> costPrivilege = null;// 军团职位特权消耗
        // 判断距离是否能开启城战
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
            throw new MwException(GameError.ATK_MAX_TIME.getCode(), "时间太长，不能使用改类型战斗, roleId:", player.lord.getLordId());
        }

        boolean hasPrivilege = false;
        int privilegeCostPower = 0;
        if (StaticPartyDataMgr.jobHavePrivilege(player.lord.getJob(), jobPrivilege)) {
            List<Integer> costPrivilege = StaticPartyDataMgr.getJobPrivilegeVal(player.lord.getJob(), jobPrivilege);
            costItemNum = costPrivilege.get(2);
            privilegeCostPower = costPrivilege.get(1);

            hasPrivilege = true;
            LogUtil.debug(player.lord.getLordId() + "战斗消耗costItemNum=" + costItemNum + ",costPower=" + costPower
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

        //消耗的进攻指令向上取整, 获得进攻指令个数
        if (costItemNum > 0) {
            double costPower_ = costPower;
            costItemNum = (int) Math.ceil(costPower_ / 5);
        }

        // 判断官员 消耗体力或者道具
        if (costItemNum > 0) {
            if (rewardDataManager.propIsEnough(player, PropConstant.ITEM_ID_5043, costItemNum)) {
                rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.PROP, PropConstant.ITEM_ID_5043,
                        costItemNum, AwardFrom.ATACK_PLAYER);
            } else {
                try {
                    rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.ACT,
                            costPower, AwardFrom.ATACK_PLAYER);
                } catch (MwException mwException) {
                    // 攻打玩家行动力不足
                    activityService.checkTriggerGiftSync(ActivityConst.TRIGGER_GIFT_DOCOMBAT_ACT, player);
                    throw mwException;
                }
            }
        }
    }

    /**
     * 通知被攻击玩家军情
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
     * 通知被攻击玩家军情
     *
     * @param target
     * @param atkLord
     * @param atkTime
     * @param status
     * @param battleType 战斗类型
     * @param params     扩展参数
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
     * 检查玩家的出征将领,支持采集将领
     *
     * @param player
     * @param heroIdList
     * @throws MwException
     */
    public void checkFormHeroSupport(Player player, List<PartnerHero> heroIdList, int pos) throws MwException {
        long roleId = player.roleId;
        playerDataManager.autoAddArmy(player);
        if (CheckNull.isEmpty(heroIdList)) {
            throw new MwException(GameError.ATTACK_POS_NO_HERO.getCode(), "AttackPos未设置将领, roleId:", roleId);
        }
        // 目标点是否是一个采集点
        boolean isMinePos = worldDataManager.isMinePos(pos) && !worldDataManager.isPlayerPos(pos);

        if (worldDataManager.isPlayerPos(pos)) {// 解决矿点 和位置重叠时玩家重叠
            isMinePos = false;
        }
        Hero hero;
        for (PartnerHero partnerHero : heroIdList) {
            if (HeroUtil.isEmptyPartner(partnerHero)) {
                throw new MwException(GameError.HERO_NOT_FOUND.getCode(), "AttackPos，玩家没有这个将领, roleId:", roleId,
                        ", heroId:", 0);
            }

            hero = partnerHero.getPrincipalHero();
            int heroId = hero.getHeroId();
            if (!player.isOnBattleHero(heroId) && !player.isOnAcqHero(heroId) && !player.isOnCommandoHero(heroId)) {
                throw new MwException(GameError.HERO_NOT_BATTLE.getCode(), "AttackPos，将领未上阵,又未在采集队列中上阵 roleId:", roleId,
                        ", heroId:", heroId);
            }

            if (!hero.isIdle()) {
                throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "AttackPos，将领不在空闲中, roleId:", roleId,
                        ", heroId:", heroId, ", state:", hero.getState());
            }

            if (hero.getCount() <= 0) {
                throw new MwException(GameError.HERO_NO_ARM.getCode(), "AttackPos，将领没有带兵, roleId:", roleId, ", heroId:",
                        heroId, ", count:", hero.getCount());
            }
            if (!isMinePos) {
                // 如果是一个非采集点,但派出了采集将领
                if (hero.isOnAcq()) {
                    throw new MwException(GameError.ACQ_HERO_NOT_ATTACK.getCode(), "AttackPos，采集将领只能进行采集, roleId:",
                            roleId);
                }
            } else {
                // 如果是一个特攻将领, 但是派出来采集
                if (hero.isCommando()) {
                    throw new MwException(GameError.COMMANDO_HERO_NOT_ATK.getCode(), "AttackPos，特攻将领不能进行采集, roleId:",
                            roleId);
                }
            }

        }
        // 如果目标是采集的时候
        if (isMinePos) {
            if (heroIdList.size() != 1) {
                // 目标为采集的时,只能排出一个将领
                throw new MwException(GameError.COLLECT_WORK_ONLYONE.getCode(), "AttackPos，将领采集时只能有一个, roleId:", roleId,
                        ", heroIdList.size:", heroIdList.size());
            }
            int stateAcqCount = 0; // 有多少个将领正在采集
            for (PartnerHero partnerHero : player.getPlayerFormation().getHeroAcq()) {
                if (HeroUtil.isEmptyPartner(partnerHero)) continue;
                Hero h = partnerHero.getPrincipalHero();
                if (null != h && h.getState() == HeroConstant.HERO_STATE_COLLECT) {
                    stateAcqCount++;
                }
            }
            for (PartnerHero partnerHero : player.getPlayerFormation().getHeroBattle()) {
                if (HeroUtil.isEmptyPartner(partnerHero)) continue;
                Hero h = partnerHero.getPrincipalHero();
                if (null != h && h.getState() == HeroConstant.HERO_STATE_COLLECT) {
                    stateAcqCount++;
                }
            }
            if (stateAcqCount >= 4) {
                throw new MwException(GameError.COLLECT_HERO_OVER_MAX.getCode(), "AttackPos，当前采集将领已超过上限, roleId:",
                        roleId, ", stateAcqCount:", stateAcqCount);
            }
        }
    }

    /**
     * 检查玩家的出征将领
     *
     * @param player
     * @param heroIdList
     * @throws MwException
     */
    public void checkFormHero(Player player, List<Integer> heroIdList) throws MwException {
        long roleId = player.roleId;
        if (CheckNull.isEmpty(heroIdList)) {
            throw new MwException(GameError.ATTACK_POS_NO_HERO.getCode(), "AttackPos未设置将领, roleId:", roleId);
        }

        Hero hero;
        for (Integer heroId : heroIdList) {
            hero = player.heros.get(heroId);
            if (null == hero) {
                throw new MwException(GameError.HERO_NOT_FOUND.getCode(), "AttackPos，玩家没有这个将领, roleId:", roleId,
                        ", heroId:", heroId);
            }

            if (!player.isOnBattleHero(heroId) && !player.isOnCommandoHero(heroId)) {
                throw new MwException(GameError.HERO_NOT_BATTLE.getCode(), "AttackPos，将领未上阵, roleId:", roleId,
                        ", heroId:", heroId);
            }

            if (!hero.isIdle()) {
                throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "AttackPos，将领不在空闲中, roleId:", roleId,
                        ", heroId:", heroId, ", state:", hero.getState());
            }

            if (hero.getCount() <= 0) {
                throw new MwException(GameError.HERO_NO_ARM.getCode(), "AttackPos，将领没有带兵, roleId:", roleId, ", heroId:",
                        heroId, ", count:", hero.getCount());
            }
        }
    }


    /**
     * 检查玩家的出征将领
     *
     * @param player
     * @param heroIdList
     * @throws MwException
     */
    public void checkFormHeroBerlin(Player player, List<Integer> heroIdList) throws MwException {
        long roleId = player.roleId;
        if (CheckNull.isEmpty(heroIdList)) {
            throw new MwException(GameError.ATTACK_POS_NO_HERO.getCode(), "AttackPos未设置将领, roleId:", roleId);
        }

        Hero hero;
        PartnerHero partnerHero;
        for (Integer heroId : heroIdList) {
            partnerHero = player.getPlayerFormation().getPartnerHero(heroId);
            if (HeroUtil.isEmptyPartner(partnerHero)) {
                throw new MwException(GameError.HERO_NOT_FOUND.getCode(), "AttackPos，玩家没有这个将领, roleId:", roleId,
                        ", heroId:", heroId);
            }

            if (!player.isOnBattleHero(heroId)) {
                throw new MwException(GameError.HERO_NOT_BATTLE.getCode(), "AttackPos，将领未上阵, roleId:", roleId,
                        ", heroId:", heroId);
            }

            hero = partnerHero.getPrincipalHero();
            if (!hero.isIdle()) {
                throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "AttackPos，将领不在空闲中, roleId:", roleId,
                        ", heroId:", heroId, ", state:", hero.getState());
            }

            if (hero.getCount() <= 0) {
                throw new MwException(GameError.HERO_NO_ARM.getCode(), "AttackPos，将领没有带兵, roleId:", roleId, ", heroId:",
                        heroId, ", count:", hero.getCount());
            }
            if (hero.isCommando()) {
                throw new MwException(GameError.COMMANDO_HERO_NOT_ATK.getCode(), "挑战关卡时,选择的将领不能进攻, roleId:", roleId,
                        ", heroId:", heroId);
            }
        }
    }

    /**
     * 计算玩家行军到目标坐标需要的时间 行军时间（秒）=8*（|X差|+|Y差|）*（1-行军加速_科技[%])*(1-行军加速_道具[%])/(1+军曹官加成[%]） 向上取整
     *
     * @param player
     * @param pos
     * @return
     */
    public int marchTime(Player player, int pos, Object... params) {
        int distance = calcDistance(player.lord.getPos(), pos);

        int baseRatio = Constant.MARCH_TIME_RATIO;

        int time = distance * baseRatio;
        //********************增加行军速度****************************
        // 科技加成
        TechDataManager techDataManager = DataResource.ac.getBean(TechDataManager.class);
        double addRatio = techDataManager.getTechEffect4SingleVal(player, TechConstant.TYPE_6);
        if (!ObjectUtils.isEmpty(params) && (boolean) params[0]) {
            addRatio *= 2;
        }

        // 柏林官员
        double berlinJobEffect = BerlinWar.getBerlinBuffVal(player.roleId, BerlinWarConstant.BUFF_TYPE_MARCH_TIME);
        // 赛季天赋:行军加速
        SeasonTalentService seasonTalentService = DataResource.ac.getBean(SeasonTalentService.class);
        double seasonTalentEffect = seasonTalentService.getSeasonTalentEffectValue(player, SeasonConst.TALENT_EFFECT_301);
        //驻军行军速度赛季天赋优化加成
        if (ArmyConstant.GUARD_MARCH == Optional.ofNullable(player.getMarchType().get(pos)).orElse(0)) {
            seasonTalentEffect += seasonTalentService.getSeasonTalentEffectValue(worldDataManager.getPosData(pos), SeasonConst.TALENT_EFFECT_610);
            //去除驻军行军类型标记
            player.getMarchType().remove(pos);
        }
        //********************减少行军时间****************************
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

        // 行军特效加成
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

        // buff加成
        Effect effect = player.getEffect().get(EffectConstant.WALK_SPEED);
        double addRatio1 = effect != null ? effect.getEffectVal() : 0;

        effect = player.getEffect().get(EffectConstant.WALK_SPEED_HIGHT);
        // 军曹官
        double addRatio2 = effect != null ? effect.getEffectVal() : 0;

        effect = player.getEffect().get(EffectConstant.PREWAR_WALK_SPEED);
        // 柏林战前buff
        double addRatio3 = effect != null ? effect.getEffectVal() : 0;

        // 名城Buff
        double cityBuffer = worldDataManager.getCityBuffer(worldDataManager.checkCityBuffer(player.lord.getPos()),
                WorldConstant.CityBuffer.MARCH_BUFFER, player.roleId);

        // 行军时间（秒）=8*（|X差|+|Y差|）/（1+行军加速_科技[%]+官职加成[%]+赛季天赋加成[%])*(1-道具加成[%])*(1-军曹官加成[%])*(1-柏林战前加成[%]*(1-城池皮肤加成[%]))
        try {
            time = (int) Math.ceil((time / (1 + (addRatio + berlinJobEffect + cityBuffer + seasonTalentEffect) / Constant.TEN_THROUSAND)
                    * (1 - addRatio1 / Constant.TEN_THROUSAND)
                    * (1 - addRatio2 / Constant.TEN_THROUSAND)
                    * (1 - addRatio3 / Constant.TEN_THROUSAND)
                    * (1 - addRatio4 / Constant.TEN_THROUSAND)
                    * (1 - addRatio5 / Constant.TEN_THROUSAND)));
        } catch (Exception e) {
            LogUtil.error("行军时间计算出错", e);
        }
        if (time < 1) {
            time = 1;
        }
        LogUtil.debug("roleId:", player.lord.getLordId(), ", 实际时间 = ", distance, ", 科技等加成=", addRatio, ", 风字令=", addRatio1, ", 军曹官=", addRatio2, ", 柏林战前buff=",
                addRatio3, ", 柏林官员=", berlinJobEffect, ", 名城Buff=", cityBuffer, ", 皮肤加成", addRatio4, ", 行军特效加成: ", addRatio5, "最终时间 = ", time);
        return time;
    }

    /**
     * 计算两个坐标点的距离，这里的距离不是直线距离，而是X、Y轴距离的和
     *
     * @param pos1
     * @param pos2
     * @return
     */
    public int calcDistance(int pos1, int pos2) {
        return MapHelper.calcDistance(pos1, pos2);
    }

    /**
     * 撤回部队
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public RetreatRs retreat(long roleId, RetreatRq req) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int keyId = req.getKeyId();
        int type = req.getType();
        Army army = player.armys.get(keyId);
        if (null == army) {
            throw new MwException(GameError.RETREAT_ARMY_NOT_FOUND.getCode(), "撤回部队，未找到部队信息, roleId:", roleId,
                    ", keyId:", keyId);
        }
        // 不在一个区域的部队撤回,重新添加一次,解决中途被击飞到领一个区域
        if (MapHelper.getAreaIdByPos(army.getTarget()) != player.lord.getArea()) {
            March march = new March(player, army);
            worldDataManager.addMarch(march);
        }

        if (army.getState() == ArmyConstant.ARMY_STATE_MARCH) {
            // 需要消耗道具
            boolean isFree = false;
            int propId = 0;
            if (type == ArmyConstant.MOVE_BACK_TYPE_1) {
                propId = PropConstant.ITEM_ID_5021;
            } else if (type == ArmyConstant.MOVE_BACK_TYPE_2) {
                propId = PropConstant.ITEM_ID_5022;
            } else if (type == 3) {
                // vip特权次数,免费召回
                if (player.common.getRetreat() >= vipDataManager.getNum(player.lord.getVip(), VipConstant.RETREAT)) {
                    throw new MwException(GameError.SHOP_VIP_BUY_CNT.getCode(), "商品购买时，vip次数不够, roleId:" + roleId);
                }
                // 普通撤回
                type = ArmyConstant.MOVE_BACK_TYPE_1;
                isFree = true;
                player.common.setRetreat(player.common.getRetreat() + 1);
            } else {
                throw new MwException(GameError.NO_CD_TIME.getCode(), "行军召回传参错误, roleId:,", roleId, ", type:", type);
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
        // 部队返回处理
        if (army.getType() == ArmyConstant.ARMY_TYPE_COLLECT_SUPERMINE) {// 超级矿点采集
            superMineService.retreatCollectArmy(player, army, type);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_ATK_SUPERMINE) {// 超级矿点攻击
            superMineService.retreatAtkArmy(player, army, type);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_HELP_SUPERMINE) { // 超级矿点驻防
            superMineService.retreatHelpArmy(player, army, type);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_BERLIN_WAR) { // 柏林皇城战不可以主动撤回
            berlinWarService.retreatBerlinArmy(player, army, type);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_BATTLE_FRONT_WAR) { // 柏林据点战可以主动撤回
            berlinWarService.retreatBattleFrontArmy(player, army, type);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_REBEL_BATTLE) { // 匪军叛乱
            rebelService.retreatRebelHelpArmy(player, army, type);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_COUNTER_BOSS_ATK_HELP) { // 反攻德意志BOSS进攻战,
            // 驻防
            counterAtkService.retreatBossAtkHelpArmy(player, army, type);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_DECISIVE_BATTLE) {// 决战撤回
            decisiveBattleService.retreatDecisiveArmy(roleId, player, type, army);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_ATTACK_AIRSHIP) {// 飞艇的返回
            airshipService.retreatArmy(player, army, type);
        } else if (army.getType() == ArmyConstant.ARMY_TYPE_RELIC_BATTLE) {
            DataResource.getBean(RelicService.class).retreatArmy(player, army);
        } else {
            // 原来行军返回处理
            originalReturnArmyProcess(roleId, player, keyId, type, army);
        }
        // 推送区域变化
        List<Integer> posList = MapHelper
                .getAreaStartPos(MapHelper.getLineAcorss(army.getTarget(), player.lord.getPos()));
        posList.add(army.getTarget());
        posList.add(player.lord.getPos());
        EventBus.getDefault()
                .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));
        return builder.build();
    }

    /**
     * 原来的行军返回处理
     *
     * @param roleId
     * @param player
     * @param keyId
     * @param type
     * @param army
     * @throws MwException
     */
    public void originalReturnArmyProcess(long roleId, Player player, int keyId, int type, Army army) {
        // 玩家撤回已加入城战或阵营战的部队，相关处理
        Integer battleId = army.getBattleId();
        LogUtil.debug("部队撤回,battleId=" + battleId + ",keyId=" + keyId + ",type=" + type);
        int now = TimeHelper.getCurrentSecond();

        int armyState = army.getState();
        //直接处理采集部队(不管部队中是否包含战斗)，结算采集
        if (army.getState() == ArmyConstant.ARMY_STATE_COLLECT) {// 部队采集中，结算采集
            retreatSettleCollect(army, type, player, now, roleId);
        }

        if (null != battleId && battleId > 0) {// 战斗类型
            Battle battle = warDataManager.getBattleMap().get(battleId);
            if (null != battle) {
                int camp = player.lord.getCamp();
                int armCount = army.getArmCount();
                LogUtil.debug(roleId + ",撤退部队=" + armCount);
                battle.updateArm(camp, -armCount);
                // 主动召回
                retreatArmy(player, army, now, type); // 返回army状态
                if (battle.getType() == WorldConstant.BATTLE_TYPE_CITY) { // 城战
                    // 打玩家
                    if (battle.getSponsor() != null && battle.getSponsor().roleId == roleId
                            && checkArmyOnPosLast(battle, army.getTarget())) {// 如果是发起者撤退
                        // 判断这个玩家是否是撤回的是最后一个部队
                        // 玩家发起的城战，发起人撤回部队，城战取消
                        warService.cancelCityBattle(army.getTarget(), true, battle, false);
                        // 通知撤退, 客户端收到消息 重新拉数据
                        Player target = worldDataManager.getPosData(army.getTarget());
                        if (target != null && target.isLogin) {
                            syncAttackRole(target, player.lord, army.getEndTime(), WorldConstant.ATTACK_ROLE_0);
                        }
                    } else {
                        // 不是发起者,移除battle的兵力
                        removeBattleArmy(battle, roleId, keyId, battle.getAtkCamp() == camp);
                    }
                } else if (battle.getType() == WorldConstant.BATTLE_TYPE_MINE_GUARD) {
                    //采集驻守撤回, 半路撤回部队，取消战斗提示
                    warDataManager.removeBattleById(battleId);
                } else {// 阵营战
                    removeBattleArmy(battle, roleId, keyId, battle.getAtkCamp() == camp);
                }

                // 更新地图速度
                processMarchArmy(player.lord.getArea(), army, keyId);

                // player.battleMap.remove(battle.getPos());
                HashSet<Integer> battleIds = player.battleMap.get(battle.getPos());
                if (battleIds != null) {
                    battleIds.remove(battleId);
                }
            } else {// 没有战斗时就直接让部队返回
                //
                Battle specialBattle = warDataManager.getSpecialBattleMap().get(battleId);
                if (army.getType() == ArmyConstant.ARMY_TYPE_LIGHTNING_WAR // 闪电战和主动打反攻德意志的BOSS
                        || army.getType() == ArmyConstant.ARMY_TYPE_COUNTER_BOSS_DEF
                        && !CheckNull.isNull(specialBattle)) {
                    int camp = player.lord.getCamp();
                    int armCount = army.getArmCount();
                    LogUtil.debug(roleId + ",撤退部队=" + armCount);
                    specialBattle.updateAtkBoss(camp, -armCount);
                    // 主动召回
                    retreatArmy(player, army, now, type);
                    // 主动撤回的都是进攻方
                    removeBattleArmy(specialBattle, roleId, keyId, true);
                    // 更新地图速度
                    processMarchArmy(player.lord.getArea(), army, keyId);

                    HashSet<Integer> battleIds = player.battleMap.get(specialBattle.getPos());
                    if (battleIds != null) {
                        battleIds.remove(battleId);
                    }
                } else {
                    LogUtil.debug("roleId:", roleId, ", 修复型撤退部队", army);
                    retreatArmy(player, army, now, type);
                }
            }
        } else {
            // 非战斗类型主动撤回
            // 主动召回
            retreatArmy(player, army, now, type);
        }

        //不管采矿当前部队是否带有战斗, 一律撤回被攻击提示
        if (armyState == ArmyConstant.ARMY_STATE_COLLECT) {
            //取消采矿被攻击提示
            cancelMineBattle(army.getTarget(), now, player);
        }

        synRetreatArmy(player, army, now); // 同步army状态
    }

    public void retreatSettleCollect(Army army, int type, Player player, int now, long roleId) {
        type = 0;
        int pos = army.getTarget();
        // 采集结算
        int resource = calcCollect(player, army, now);

        List<Award> grab = army.getGrab();// 采集获得
        StaticMine staticMine = worldDataManager.getMineByPos(pos);
        if (staticMine == null) {
            LogUtil.error("role:" + roleId + "采集无法返回---强制返回");
            LogLordHelper.commonLog("retreatArmy", AwardFrom.COMMON, player);
        } else {
            Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
            boolean effect = mineService.hasCollectEffect(player, staticMine.getMineType(),
                    new Date(army.getBeginTime() * 1000L), army);// 采集加成
            int heroId = army.getHero().get(0).getPrincipleHeroId();
            Hero hero = player.heros.get(heroId);
            int time = now - army.getBeginTime();
            army.setCollectTime(time);
            int addExp = (int) Math.ceil(time * 1.0 / Constant.MINUTE) * 20;// 将领采集经验
            addExp = heroService.adaptHeroAddExp(player, addExp);
            // 给将领加经验
            int addExp_ = heroService.addHeroExp(hero, addExp, player.lord.getLevel(), player);
            addDeputyHeroExp(addExp, army.getHero().get(0), player);


            CommonPb.MailCollect collect = PbHelper.createMailCollectPb(time, hero, addExp_, grab, effect);

            // 发送邮件通知
            mailDataManager.sendCollectMail(player, null, MailConstant.MOLD_COLLECT_RETREAT, collect, now,
                    staticMine.getLv(), staticMine.getMineId(), xy.getA(), xy.getB());

            // 更新矿点资源
            mineService.updateMine(pos, resource);
            // 移除采集守卫信息
            worldDataManager.removeMineGuard(pos);

            //貂蝉任务-采集资源
            ActivityDiaoChanService.completeTask(player, ETask.COLLECT_RES, staticMine.getMineType(), grab.get(0).getCount());
            TaskService.processTask(player, ETask.COLLECT_RES, staticMine.getMineType(), grab.get(0).getCount());
        }
    }

    /**
     * 给副将增加经验
     *
     * @param addExp
     * @param partnerHeroIdPb
     * @param player
     */
    public void addDeputyHeroExp(int addExp, CommonPb.PartnerHeroIdPb partnerHeroIdPb, Player player) {
        if (CheckNull.nonEmpty(partnerHeroIdPb.getDeputyHeroIdList())) {
            for (Integer heroId : partnerHeroIdPb.getDeputyHeroIdList()) {
                Hero hero_ = player.heros.get(heroId);
                if (CheckNull.isNull(hero_)) continue;
                heroService.addHeroExp(hero_, addExp, player.lord.getLevel(), player);
            }
        }
    }

    /**
     * 取消当前矿点所有战斗提示
     *
     * @param pos
     * @param now
     * @param player
     */
    public void cancelMineBattle(int pos, int now, Player player) {
        //采集队列取消被攻击报警
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
     * 判断这个玩家在这个坐标是否是撤回的是最后一个部队
     *
     * @param battle
     * @param pos
     * @return true是最后一只
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
     * 更新地图部队速度
     *
     * @param area
     * @param army
     * @param keyId
     */
    public void processMarchArmy(int area, Army army, int keyId) {
        // 更新地图速度
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
        LogUtil.debug("行军召回removeBattleArmy=" + list + ",roleId=" + roleId + ",keyId=" + keyId);
        Iterator<BattleRole> it = list.iterator();
        while (it.hasNext()) {
            BattleRole battleRole = it.next();
            if (battleRole.getKeyId() == keyId.intValue()) {
                LogUtil.debug("removeBattleArmy,armyMap=" + battleRole);
                it.remove();
            }
        }
        // 移除玩家id
        if (list.stream().noneMatch(br -> br.getRoleId() == roleId)) {// 该玩家在该battle中没有任何兵力就移除
            if (isAtk) {
                battle.getAtkRoles().remove(roleId);
            } else {
                battle.getDefRoles().remove(roleId);
            }
            // 移除已邀请状态
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
    // LogUtil.debug("行军召回removeBattleArmy=" + list + ",roleId=" + roleId +
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
     * 获取城战或国战详情
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GetBattleRs getBattle(long roleId, GetBattleRq req) throws MwException {
        int pos = req.getPos();
        int type = req.getType();// 0 默认战斗, 1 超级矿点战斗
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
            if (worldDataManager.isPlayerPos(pos)) { // 匪军叛乱的battle
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
        } else if (type == 1) {// 超级矿点战斗
            SuperMine sm = worldDataManager.getSuperMineMap().get(pos);
            if (CheckNull.isNull(sm)) {
                throw new MwException(GameError.SUPER_MINE_NOT_EXIST.getCode(), "超级矿点不存在, roleId:", roleId, ", pos:", pos);
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
                            int chatCnt = cnt == null ? 0 : cnt.intValue(); // 获取自己的喊话次数
                            builder.addBattle(PbHelper.createBattlePb(battle, defArm, 0, chatCnt, defLordId));
                        }
                    }
                }
            }
        }
        return builder.build();
    }

    /**
     * 根据战斗id获取咱都详情
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
                if (battle.getType() == WorldConstant.BATTLE_TYPE_SUPER_MINE) { // 超级矿点战斗另外处理
                    SuperMine sm = worldDataManager.getSuperMineMap().get(battle.getPos());
                    if (sm == null) continue;
                    int defArm = sm.defArmyCnt();
                    Integer cnt = battle.getHelpChatCnt().get(roleId);
                    int chatCnt = cnt == null ? 0 : cnt.intValue(); // 获取自己的喊话次数
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
     * 获取战斗防守的兵力
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
            // 重新计算玩家防守方的兵力
            Player defencer = battle.getDefencer();
            try {
                playerDataManager.autoAddArmy(defencer);
                wallService.processAutoAddArmy(defencer);
            } catch (Exception e) {
                LogUtil.error("获取Battle补兵出错", e);
            }
            // 空闲上阵将领,和城防军
            List<PartnerHero> heroList = defencer.getDefendHeroList();
            for (PartnerHero hero : heroList) {
                if (HeroUtil.isEmptyPartner(hero)) continue;
                if (hero.getPrincipalHero().getCount() > 0) {
                    realDefArm += hero.getPrincipalHero().getCount();
                }
            }
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
                    realDefArm += maxArmy;
                }
            }
            // 驻守本城的其他玩家将领
            List<Army> list = worldDataManager.getPlayerGuard(defencer.lord.getPos());
            if (list != null && !list.isEmpty()) {
                Player tarPlayer = null;
                for (Army army : list) {
                    tarPlayer = playerDataManager.getPlayer(army.getLordId());
                    if (tarPlayer == null) {
                        continue;
                    }
                    //数据异常,玩家阵营与驻防阵营不是同一个阵营
                    if (tarPlayer.getCamp() != defencer.getCamp()) {
                        continue;
                    }
                    realDefArm += army.getArmCount();
                }
            }
            // 别人来帮助的兵力(已经达到目的地了)
            for (BattleRole br : battle.getDefList()) {
                Player player = playerDataManager.getPlayer(br.getRoleId());
                if (player != null) {
                    List<CommonPb.PartnerHeroIdPb> heroIdList = br.getPartnerHeroIdList();
                    if (!CheckNull.isEmpty(heroIdList)) {
                        for (CommonPb.PartnerHeroIdPb pb : heroIdList) {
                            StaticHero sHero = StaticHeroDataMgr.getHeroMap().get(pb.getPrincipleHeroId());
                            if (null == sHero) {
                                continue;
                            }
                            Hero hero = player.heros.get(pb.getPrincipleHeroId());
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
            // 别人来帮助的正在路上的兵力
            realDefArm += getDefArmyCountByCityBattle(battle);
            defArm = realDefArm;
        }

        //超级矿点战斗
        if (battle.isAtkSuperMine()) {
            SuperMine superMine = worldDataManager.getSuperMineMap().get(battle.getPos());
            List<Army> allArmy = new ArrayList<>();
            if (Objects.nonNull(superMine)) {
                List<Army> collect = superMine.getCollectArmy().stream().map(SuperGuard::getArmy).collect(Collectors.toList());
                allArmy.addAll(superMine.getHelpArmy()); // 驻防在前面
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

                    for (CommonPb.PartnerHeroIdPb twoInt : army.getHero()) {
                        staticHero = StaticHeroDataMgr.getHeroMap().get(twoInt.getPrincipleHeroId());
                        if (null == staticHero) {
                            continue;
                        }
                        Hero hero = player.heros.get(twoInt.getPrincipleHeroId());
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
     * 获取别人来帮助防守的正在路上的兵力
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
                    cnt += army.getArmCount();
                }
            }
        }
        return cnt;
    }

    /**
     * 加入城战（攻击玩家）或国战 或 匪军叛乱
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public JoinBattleRs joinBattle(long roleId, JoinBattleRq req) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int camp = player.lord.getCamp();
        int battleId = req.getBattleId();
        Battle battle = warDataManager.getBattleMap().get(battleId);
        battle = CheckNull.isNull(battle) ? warDataManager.getSpecialBattleMap().get(battleId) : battle;
        if (null == battle) {
            throw new MwException(GameError.BATTLE_NOT_FOUND.getCode(), "战争信息未找到, roleId:", roleId, ", battleId:",
                    battleId);
        }
        StaticArea targetSArea = StaticWorldDataMgr.getAreaMap().get(MapHelper.getAreaIdByPos(battle.getPos()));
        StaticArea mySArea = StaticWorldDataMgr.getAreaMap().get(player.lord.getArea());

        // 城战区域检测
        checkArea(player, targetSArea, mySArea);

        if (battle.isCityBattle()) {
            if (player.getDecisiveInfo().isDecisive()) {
                throw new MwException(GameError.DECISIVE_BATTLE_ING.getCode(), "您正处于决战状态,不能加入城战 , roleId:", roleId);
            }
            if (battle.getDefencer().roleId == roleId) {
                throw new MwException(GameError.HAS_JOIN_BATTLE.getCode(), "城战被攻击方不用再加入战斗, roleId:", roleId,
                        ", battleId:", battleId);
            }
            // 是闪电战进攻方不让加入,防守方可以加入
            if (battle.getBattleType() == WorldConstant.CITY_BATTLE_BLITZ && camp == battle.getAtkCamp()) {
                throw new MwException(GameError.QUICKLY_BATTLE_NOT_JOIN.getCode(), "闪电战不让加入战斗, roleId:", roleId,
                        ", battleId:", battleId);
            }
        } else if (battle.isRebellionBattle()) {// 匪军叛乱

        } else if (battle.isCounterAtkBattle()) {// 反攻德意志
            if (battle.getBattleType() == WorldConstant.COUNTER_ATK_DEF || camp != battle.getDefCamp()) {
                throw new MwException(GameError.COUNTER_CANT_JOIN_BATTLE.getCode(), "反攻德意志参数错误, 不能加入战斗, roleId:",
                        roleId, ", battleId:", battleId);
            }
        } else {// 阵营战
            if (WorldConstant.ATTACK_STATE_NEED_LV > player.lord.getLevel()) {
                throw new MwException(GameError.ATTACK_STATE_NEED_LV.getCode(), "指挥官先磨砺至45级，再发动阵营战吧, roleId:", roleId);
            }
            // StaticCity staticCity =
            // StaticWorldDataMgr.getCityByPos(battle.getPos());
            // if (staticCity.getArea() != player.lord.getArea()) {
            // throw new MwException(GameError.CAMP_BATTLE_AREA_ERROR.getCode(),
            // "跨区域不允许,发起阵营战, roleId:", roleId,
            // ", cityId:", staticCity.getCityId());
            // }
        }

        // 允许一个玩家多个部队加入
        // if (player.battleMap.containsKey(battle.getPos())) {
        // throw new MwException(GameError.HAS_JOIN_BATTLE.getCode(), "玩家已加入战斗,
        // roleId:", roleId, ", battleId:",
        // battleId);
        // }

        if (battle.getAtkCamp() != camp && battle.getDefCamp() != camp) {
            throw new MwException(GameError.CAN_NOT_JOIN_BATTLE.getCode(), "不是本阵营的战斗，不能参加, roleId:", roleId,
                    ", battleId:", battleId, ", roleCamp:", camp);
        }

        int pos = getJoinBattlePos(battle);
        // 检查出征将领信息
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

        // 攻打NPC城行军速度翻倍(除开柏林和四个炮塔)
        StaticCity staticCity = null;
        if (battle.isCampBattle()) {
            staticCity = StaticWorldDataMgr.getCityByPos(pos);
            if (!CheckNull.isNull(staticCity)
                    && CheckNull.isNull(StaticBerlinWarDataMgr.getBerlinSettingById(staticCity.getCityId()))) {
                City city = worldDataManager.getCityById(staticCity.getCityId());
                if (city.getCamp() == Constant.Camp.NPC) {
                    marchTime = (int) (marchTime
                            * (1 - (WorldConstant.ATTACK_NPC_CITY_MARCH_NUM / Constant.TEN_THROUSAND)));
                    // 多人叛军行军加成[%]
                    marchTime = airshipService.getAirShipMarchTime(player, marchTime);
                }
            }
        }

        // 计算时间是否赶得上
        if (now + marchTime > battle.getBattleTime()) {
            throw new MwException(GameError.BATTLE_CD_TIME.getCode(), "加入城战,赶不上时间, roleId:", roleId, ", pos:",
                    pos + ",行军时间=" + (now + marchTime) + ",城战倒计时=" + battle.getBattleTime());
        }

        // 计算补给
        int needFood = checkMarchFood(player, marchTime, armCount);
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.RESOURCE, AwardType.Resource.FOOD, needFood,
                AwardFrom.ATK_POS);

        List<CommonPb.PartnerHeroIdPb> form = new ArrayList<>();
        for (Integer heroId : heroIdList) {
            PartnerHero partnerHero = player.getPlayerFormation().getPartnerHero(heroId);
            if (HeroUtil.isEmptyPartner(partnerHero)) continue;
            form.add(partnerHero.convertTo());
            partnerHero.setState(ArmyConstant.ARMY_STATE_MARCH);
        }

        int type = ArmyConstant.ARMY_TYPE_ATK_CAMP;
        if (battle.isCityBattle()) {
            type = ArmyConstant.ARMY_TYPE_ATK_PLAYER;
        } else if (battle.isCampBattle()) {
            type = ArmyConstant.ARMY_TYPE_ATK_CAMP;
        } else if (battle.isRebellionBattle()) {// 匪军叛乱驻防
            type = ArmyConstant.ARMY_TYPE_REBEL_BATTLE;
        } else if (battle.isCounterAtkBattle() && battle.getBattleType() == WorldConstant.COUNTER_ATK_ATK) { // 反攻德意志驻防
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

        // 添加行军路线
        March march = new March(player, army);
        worldDataManager.addMarch(march);

        // 破护盾 攻击方
        if (camp == battle.getAtkCamp()) {
            removeProTect(player, AwardFrom.JOIN_BATTLE_WAR, pos);
            battle.getAtkRoles().add(roleId);
        } else if (camp == battle.getDefCamp()) {
            battle.getDefRoles().add(roleId);
        }

        // 推送区数据改变
        List<Integer> posList = MapHelper.getAreaStartPos(MapHelper.getLineAcorss(player.lord.getPos(), pos));
        posList.add(pos);
        posList.add(player.lord.getPos());
        EventBus.getDefault().post(
                new Events.AreaChangeNoticeEvent(posList, roleId, Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));
        // 返回协议
        JoinBattleRs.Builder builder = JoinBattleRs.newBuilder();

        // 更新battle兵力
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
     * 检测区域的逻辑
     *
     * @param player
     * @param targetSArea
     * @param mySArea
     * @throws MwException
     */
    public void checkArea(Player player, StaticArea targetSArea, StaticArea mySArea) throws MwException {
        if (mySArea.getOpenOrder() == WorldConstant.AREA_ORDER_3) { // 自己在皇城区域,任何地方都可以打
        } else if (mySArea.getOpenOrder() == WorldConstant.AREA_ORDER_2) {// 自己在州的情况,州只能打州
            if (targetSArea.getOpenOrder() != WorldConstant.AREA_ORDER_2) {
                throw new MwException(GameError.CAMP_BATTLE_AREA_ERROR.getCode(), "跨区域不允许战斗, roleId:", player.roleId,
                        ", my area:", mySArea.getArea(), ", target area:", targetSArea.getArea());
            }
        } else if (mySArea.getOpenOrder() == WorldConstant.AREA_ORDER_1) { // 自己在郡只能打本区域的
            if (targetSArea.getArea() != mySArea.getArea()) {
                throw new MwException(GameError.CAMP_BATTLE_AREA_ERROR.getCode(), "跨区域不允许战斗, roleId:", player.roleId,
                        ", my area:", mySArea.getArea(), ", target area:", targetSArea.getArea());
            }
        }
    }

    public int getNeedFood(int marchTime, int armCount) {
        return marchTime * WorldConstant.MOVE_COST_FOOD + (int) (Math.ceil(armCount * 1.0f / 10));
    }

    /**
     * 根据当前世界进度，判断是否能对相对应的城池发起攻击
     *
     * @param cityType
     * @return true 可以攻击
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
     * 发起国战（攻击其他国家城池）
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public AttackStateRs attackState(long roleId, AttackStateRq req) throws MwException {
        int now = TimeHelper.getCurrentSecond();
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        if (WorldConstant.ATTACK_STATE_NEED_LV > player.lord.getLevel()) {
            throw new MwException(GameError.ATTACK_STATE_NEED_LV.getCode(), "指挥官先磨砺至45级，再发动阵营战吧, roleId:", roleId);
        }

        int cityId = req.getCityId();
        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(cityId);
        if (null == staticCity) {
            throw new MwException(GameError.CITY_NOT_FOUND.getCode(), "发起阵营战，未找到城池, roleId:", roleId, ", cityId:",
                    cityId);
        }
        // 世界进度阶段判断
        if (!canAttackStateForWorldSchedule(staticCity.getType())) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "世界进度不匹配 cityType:", staticCity.getType(),
                    ", currId");
        }

        // 闪电战活动期间不能发起攻城战
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
                                "闪电战即将开启,暂时不能发起攻城战, roleId:", roleId);
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
                    throw new MwException(GameError.CAMP_BATTLE_HAS_EXISTS.getCode(), "发起阵营战，已有玩家发起对该城的阵营战, roleId:",
                            roleId, ", cityId:", cityId);
                }
            }
        }

        // 皇城必须在区域内
        // if (staticCity.getArea() == WorldConstant.AREA_TYPE_13 &&
        // staticCity.getArea() != player.lord.getArea()) {
        // throw new MwException(GameError.CAMP_BATTLE_AREA_ERROR.getCode(),
        // "跨区域不允许,发起阵营战, roleId:", roleId,
        // ", cityId:", cityId);
        // }

        // 郡城只能打郡城
        // if (staticArea.getOpenOrder() == 1 && staticArea.getOpenOrder() !=
        // StaticWorldDataMgr.getAreaMap()
        // .get(player.lord.getArea()).getOpenOrder()) {
        // throw new MwException(GameError.CAMP_BATTLE_AREA_ERROR.getCode(),
        // "跨区域不允许,发起阵营战, roleId:", roleId,
        // ", cityId:", cityId);
        // }

        City city = worldDataManager.getCityById(cityId);

        if (city.getCamp() > 0 && camp == city.getCamp()) {
            throw new MwException(GameError.SAME_CAMP.getCode(), "同阵营不允许,发起阵营战, roleId:", roleId, ", cityId:",
                    staticCity.getCityId());
        }
        if (city.getProtectTime() > now) {
            throw new MwException(GameError.CITY_PROTECT.getCode(), "城池保护中, roleId:", roleId, ", cityId:", cityId);
        }

        int job = player.lord.getJob();
        // 城池是都城的情况下
        if (staticCity.getType() == WorldConstant.CITY_TYPE_HOME) {
            // 只有司令库发起都城战斗
            if (job != PartyConstant.Job.KING) {
                throw new MwException(GameError.NO_PRIVILEGE_ABOUT_KING.getCode(), "发起都城宣战权限不足,司令才能发起, roleId:", roleId,
                        ", cityId:", cityId);
            }
            // 都城只能占领一次
            if (city.getCamp() > 0) {
                throw new MwException(GameError.CAMP_BATTLE_AREA_ERROR.getCode(), "该城已被占领, roleId:", roleId,
                        ", cityId:", cityId);
            }

            // 我当前的阵营是否已拥有都城
            if (worldDataManager.checkHasHome(camp) != null) {
                throw new MwException(GameError.CAMP_BATTLE_AREA_ERROR.getCode(), "已有都城,不能重复占领 roleId:", roleId,
                        ", cityId:", cityId);
            }
            // 检测是否对其他都城发起战斗, 同时只能对一个都城进行发起战斗
            if (checkCityTypeHomeHasBattle(player)) {
                throw new MwException(GameError.TYPE_HOME_CITY_BATTLE_ONLYONE.getCode(), " 同时只能对一个都城发起战斗 roleId:",
                        roleId, ", cityId:", cityId);
            }
        }
        // 跨区不能发起
        if (staticCity.getArea() != player.lord.getArea()) {
            // 但是司令 政委 参谋长 可以跨区宣战
            if (job == PartyConstant.Job.KING || job == PartyConstant.Job.COMMISSAR || job == PartyConstant.Job.CHIEF) {
            } else {
                throw new MwException(GameError.CAMP_BATTLE_AREA_ERROR.getCode(), "跨区域不允许,发起阵营战, roleId:", roleId,
                        ", cityId:", staticCity.getCityId());
            }
        }

        // 城池是名城的情况下,并且是中立
        if (staticCity.getType() == WorldConstant.CITY_TYPE_8) {
            if (city.getCamp() == Constant.Camp.NPC && !checkAttkableType8NpcCity(camp)) {
                // 检查本方阵营的名城是否多余7个,如果多余7个就不让发起
                throw new MwException(GameError.CITY_TYPE_8_NOT_ATTACK.getCode(), "本方阵营的名城数量超过上限不让发起战斗 roleId:",
                        roleId);
            }
        }

        if (CheckNull.isEmpty(battleList)) {
            city.setAttackCamp(player.lord.getCamp());// 记录进攻该城池的阵营
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

            // 通知拥有城池的阵营城池被攻击消息
            chatDataManager.sendSysChat(ChatConst.CHAT_CITY_DEF, city.getCamp(), 0, camp, player.lord.getNick(),
                    cityId);
        } else {
            battle.setAtkNpc(true);
        }

        // 通知进攻方阵营战消息
        chatDataManager.sendSysChat(ChatConst.CHAT_CITY_ATK, camp, 0, player.lord.getNick(), city.getCamp(), cityId);

        // 添加战斗记录
        warDataManager.addBattle(player, battle);

        // 通知阵营战信息
        syncAttackCamp(battle, cityId);

        AttackStateRs.Builder builder = AttackStateRs.newBuilder();
        List<Integer> posList = new ArrayList<>();
        posList.add(staticCity.getCityPos());
        EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_TYPE));
        return builder.build();
    }

    /**
     * 检测当前阵营是否可以对中立名城发起进攻
     *
     * @param camp
     * @return true 可以发起
     */
    public boolean checkAttkableType8NpcCity(int camp) {
        int battleCnt = 0;// 已发起的其他阵营特区个数+已发起的中立城个数
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
        int hasCityCnt = worldDataManager.getPeoPle4MiddleCity(camp);// 已占领特区个数
        LogUtil.debug("检测当前阵营是否可以对名城发起进攻  battleCnt:", battleCnt, ", hasCityCnt:", hasCityCnt);
        // 已占领特区个数+已发起的其他阵营特区个数+已发起的中立城个数 是否大于等于7，若是则不能发起国战
        return (battleCnt + hasCityCnt) < WorldConstant.CITY_TYPE_8_MAX;

    }

    /**
     * 检测是否已经对都城发起了战斗
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
     * NPC发起的进攻
     *
     * @param atkCity 进攻的都城NPC
     * @param defCity 防守的名城
     */
    public void processAtk(City atkCity, City defCity) {
        int camp = atkCity.getCamp();
        int cityId = defCity.getCityId();
        LinkedList<Battle> battleList = warDataManager.getBattlePosMap()
                .get(StaticWorldDataMgr.getCityMap().get(cityId).getCityPos());

        // 判断是否有本阵营的玩家发起营战，有则取消
        if (!CheckNull.isEmpty(battleList)) {
            for (Battle battle : battleList) {
                if (battle.getAtkCamp() == camp) {
                    LogUtil.debug("怪物攻城，已有玩家发起对该城的阵营战cityId=" + atkCity.getCityId());
                    return;
                }
            }
        }
        LogUtil.debug("怪物攻城 都城NPC自动发起攻城atk=" + atkCity + ",defCity=" + defCity);
        // 修改防守方的城池的状态
        defCity.setAttackCamp(atkCity.getCamp());// 记录进攻该城池的阵营
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

        // 计算都城NPC兵力
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
        LogUtil.debug("=====怪物攻城进攻方的兵力显示:", battle.getAtkArm() + ",arm=" + armTotal);
        LogUtil.debug("=====怪物攻城防守方的兵力显示:", battle.getDefArm() + ",arm=" + defCity.getCurArm());
        if (defCity.getCamp() != Constant.Camp.NPC) {
            if (defCity.getOwnerId() > 0) {
                battle.setDefencer(playerDataManager.getPlayer(defCity.getOwnerId()));
            }

            // 通知拥有城池的阵营城池被攻击消息
            chatDataManager.sendSysChat(ChatConst.CHAT_HOME_CITY_DEF, defCity.getCamp(), 0, camp, cityId);
        } else {
            battle.setAtkNpc(true);
        }

        // 通知进攻方阵营战消息
        chatDataManager.sendSysChat(ChatConst.CHAT_HOME_CITY_ACT, camp, 0, defCity.getCamp(), cityId);

        // 添加战斗记录
        warDataManager.addBattle(null, battle);

        // 通知阵营战信息
        syncAttackCamp(battle, cityId);
    }

    /**
     * 通知客户端阵营战信息
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
        // 通知所有与本区已开通关联的区域内，相关联的玩家
        List<Integer> areaIdList = staticArea.getUnlockArea();

        ConcurrentHashMap<Long, Player> playerMap = playerDataManager.getPlayerByAreaList(areaIdList);
        for (Player player : playerMap.values()) {
            if (player.isLogin && (player.lord.getCamp() == atkCamp || player.lord.getCamp() == defCamp)) {
                MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
            }
        }
    }

    /**
     * 移除保护罩
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
        LogUtil.debug(atk.roleId + ",护盾消失" + effect + ",当前护罩" + atk.getEffect().get(EffectConstant.PROTECT));
        LogLordHelper.logRemoveProtect(atk, from, pos);
        syncProTectRs(atk);
    }

    /**
     * 推送破了保护罩
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
     * 获取阵营战信息
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetCampBattleRs getCampBattle(long roleId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int camp = player.lord.getCamp();
        int areaId = player.lord.getArea();
        LogUtil.debug(roleId + ",获取阵营战信息camp=" + camp + ",areaId=" + areaId);
        GetCampBattleRs.Builder builder = GetCampBattleRs.newBuilder();
        StaticArea staticArea = StaticWorldDataMgr.getAreaMap().get(areaId);
        if (null != staticArea) {
            List<StaticCity> cityList;
            LinkedList<Battle> battleList;
            List<Integer> areaIdList = staticArea.getUnlockArea();// 玩家所有能去的区域
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
     * 阵营城池征收
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetCampCityLevyRs getCampLevy(Long roleId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int camp = player.lord.getCamp();
        int areaId = player.lord.getArea();
        LogUtil.debug(roleId + ",获取阵营战信息camp=" + camp + ",areaId=" + areaId);
        GetCampCityLevyRs.Builder builder = GetCampCityLevyRs.newBuilder();
        // 城池信息
        List<StaticCity> cityList = StaticWorldDataMgr.getCityByArea(areaId);
        if (CheckNull.isEmpty(cityList)) {
            LogUtil.error("分区城池未配置, area:", areaId);
        } else {
            City city;
            for (StaticCity staticCity : cityList) {
                city = worldDataManager.getCityById(staticCity.getCityId());
                if (null == city) {
                    LogUtil.error("有城池信息没有生成，重大错误!!!cityId:", staticCity.getCityId());
                    continue;
                }
                // 如果不是同一阵营的
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
     * 把一个区域的人合并到另一个区域
     *
     * @param fromAreaId 需要被合并的区域
     * @param toAreaId   目标区域id
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
                    player.ctx.close();// 踢下线
                    player.logOut();// 登出
                }
                int prePos = player.lord.getPos();
                int newPos = moveCityByGmAfterProcess(player, toAreaId);
                notifyPos.add(prePos);
                notifyPos.add(newPos);
            }
            // 地图通知
            EventBus.getDefault()
                    .post(new Events.AreaChangeNoticeEvent(notifyPos, Events.AreaChangeNoticeEvent.MAP_AND_AREA_TYPE));
        }
    }

    /**
     * 清空某个坐标点
     *
     * @param pos
     */
    public void clearPos(int pos) {
        if (worldDataManager.isPlayerPos(pos)) { // 如果是玩家直接迁城
            Player player = worldDataManager.getPosData(pos);
            moveCityByGmAfterProcess(player, player.lord.getArea());
            LogUtil.start("清除位置有其他东西: 移除玩家 roleId:" + player.roleId + ", pos:" + pos);
        } else if (worldDataManager.isBanditPos(pos)) { // 流寇移除
            worldDataManager.removeBandit(pos, 0);
            LogUtil.start("清除位置有其他东西: 移除流寇:" + " pos:" + pos);
        } else if (worldDataManager.isCabinetLeadPos(pos)) {
            CabinetLead lead = worldDataManager.getCabinetLeadByPos(pos);
            worldDataManager.removeBandit(pos, 1); // 直接给他加上进度
            // 给目标玩家加上点兵数
            Player targetPlayer = playerDataManager.getPlayer(lead.getRoleId());
            if (targetPlayer != null && targetPlayer.cabinet != null) {
                Cabinet cabinet = targetPlayer.cabinet;
                cabinet.setLeadStep(cabinet.getLeadStep() + 1);
            }
        } else if (worldDataManager.isGestapoPos(pos)) {
            worldDataManager.removeBandit(pos, 2);
            LogUtil.start("清除位置有其他东西: 移除盖世太保:" + " pos:" + pos);
        } else if (worldDataManager.isMinePos(pos)) {
            Guard guard = worldDataManager.getGuardByPos(pos);
            if (guard != null) Optional.ofNullable(guard).flatMap(g -> Optional.ofNullable(g.getArmy()))
                    .flatMap(army -> Optional.ofNullable(playerDataManager.getPlayer(army.getLordId())))
                    .ifPresent(p -> {
                        retreatArmy(p, guard.getArmy(), TimeHelper.getCurrentSecond(), ArmyConstant.MOVE_BACK_TYPE_2);
                        LogUtil.start("清除位置有其他东西: 部队采集:" + " pos:" + pos + ", roleId:" + p.roleId + " army:"
                                + guard.getArmy());
                    });

            worldDataManager.removeMine(pos);
            LogUtil.start("清除位置有其他东西: 移除矿点:" + " pos:" + pos);
        }
    }

    /**
     * 检测柏林炮台点上是否有其他东西
     */
    public void checkBerlinBuilingHasOther() {
        Set<Integer> posSet = StaticWorldDataMgr.getBerlinWarMap().values().stream()
                .filter(bl -> bl.getType() == StaticBerlinWarDataMgr.BATTLEFRONT_TYPE)
                .flatMap(bl -> bl.getPosList().stream()).collect(Collectors.toSet());
        for (int pos : posSet) {
            clearPos(pos);
        }
        // 柏林格子上有玩家的,随机迁走
        posSet = StaticWorldDataMgr.getBerlinWarMap().values().stream()
                .filter(bl -> bl.getType() == StaticBerlinWarDataMgr.BERLIN_TYPE)
                .flatMap(bl -> bl.getPosList().stream()).collect(Collectors.toSet());
        List<Award> awards = new ArrayList<>();
        awards.addAll(PbHelper.createAwardsPb(WorldConstant.BERLIN_COMPENSATION_AWARD));
        int now = TimeHelper.getCurrentSecond();
        for (Integer pos : posSet) {
            if (worldDataManager.isPlayerPos(pos)) { // 如果是玩家直接迁城
                Player player = worldDataManager.getPosData(pos);
                moveCityByGmAfterProcess(player, WorldConstant.AREA_TYPE_13);
                if (!CheckNull.isEmpty(awards)) {
                    // 发送高级迁城令
                    mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_BERLIN_COMPENSATION_AWARD,
                            AwardFrom.BERLIN_COMPENSATION_AWARD, now);
                    LogUtil.start("炮塔位置有其他东西: 移除玩家 roleId:" + player.roleId + ", pos:" + pos);
                }
            }
        }
    }

    /**
     * 不带提示的GM迁城,调用者需要自己验证区域是否合理
     *
     * @param player
     * @param areaId
     * @return 返回
     */
    private int moveCityByGmAfterProcess(Player player, int areaId) {
        int prePos = player.lord.getPos();
        // 如果有攻击该玩家的城战还未开始，结束城战
        if (player.battleMap.containsKey(prePos)) {// 改到玩家到达后没发现目标
            warService.cancelCityBattle(prePos, false);
        }

        // 友军的城墙驻防部队返回
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
                int heroId = army.getHero().get(0).getPrincipleHeroId();
                mailDataManager.sendNormalMail(tPlayer, MailConstant.MOLD_GARRISON_RETREAT, now, player.lord.getNick(),
                        heroId, player.lord.getNick(), heroId);
            }
            //清空所有驻防部队
            if (CheckNull.nonEmpty(guradArays)) {
                LogUtil.error("clear roleId: {}, pos: {} all error guard army", player.roleId, prePos);
                worldDataManager.removePlayerGuard(prePos);
            }
        }
        long roleId = player.roleId;
        int newPos = worldDataManager.randomEmptyPosInArea(areaId);
        // 更新玩家坐标
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
     * GM命令进行迁城,不受区域规则约束
     *
     * @param player
     * @param areaId
     */

    public void moveCityByGm(Player player, int areaId) {
        if (player == null) {
            return;
        }
        int prePos = player.lord.getPos();
        if (prePos < 0) {// 还未分配坐标的玩家禁止使用
            return;
        }
        // if (!CheckNull.isEmpty(player.armys)) {// 有将领在外不让迁城
        // return;
        // }
        StaticArea sArea = StaticWorldDataMgr.getAreaMap().get(areaId);
        if (sArea == null || !sArea.isOpen()) {
            return;
        }
        int newPos = moveCityByGmAfterProcess(player, areaId);
        if (player.ctx != null) {
            player.ctx.close();// 踢下线
            player.logOut();// 登出
        }
        // 通知其他玩家数据改变
        List<Integer> posList = new ArrayList<>();
        posList.add(prePos);
        posList.add(newPos);
        EventBus.getDefault()
                .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_AREA_TYPE));
    }

    /**
     * 迁城时检测世界boss
     *
     * @param player
     * @param newArea
     * @throws MwException
     */
    private void checkMoveCityByBoss(Player player, int newArea) throws MwException {
        long roleId = player.roleId;

        // // 世界boss2没被打死,功能没全解锁,只能在指定范围飞
        // CommonPb.WorldTask worldTask =
        // worldDataManager.getWorldTask().getWorldTaskMap()
        // .get(TaskType.WORLD_BOSS_TASK_ID_2);
        // if (worldTask == null || worldTask.getHp() > 0) {
        // worldTask =
        // worldDataManager.getWorldTask().getWorldTaskMap().get(TaskType.WORLD_BOSS_TASK_ID_1);
        // // 世界boss1没被打死，则只能在本区域随机
        // if (worldTask == null || worldTask.getHp() > 0) {
        // if (newArea != player.lord.getArea()) {
        // throw new MwException(GameError.AREA_NOT_OPEN.getCode(), "迁城,该区域未开放,
        // roleId:,", roleId, ",area=",
        // player.lord.getArea(), ",newArea=", newArea, ",worldTask=",
        // worldTask);
        // }
        // } else {
        // StaticArea staticArea =
        // StaticWorldDataMgr.getAreaMap().get(player.lord.getArea());
        // if (!staticArea.getOpenAreaId().contains(newArea) && newArea !=
        // player.lord.getArea()) {
        // throw new MwException(GameError.AREA_NOT_OPEN.getCode(), "迁城,该区域未开放,
        // roleId:,", roleId, ",area=",
        // player.lord.getArea(), ",newArea=", newArea, ",worldTask=",
        // worldTask);
        // }
        // }
        // }

        // 新世界进度逻辑
        int currentSchduleId = worldScheduleService.getCurrentSchduleId();
        int bossDeadState = worldScheduleService.getBossDeadState();
        if (bossDeadState == ScheduleConstant.BOSS_NO_DEAD) {
            if (newArea != player.lord.getArea()) {// 世界boss1没被打死，则只能在本区域随机
                throw new MwException(GameError.AREA_NOT_OPEN.getCode(), "迁城,该区域未开放, roleId:,", roleId, ",area=",
                        player.lord.getArea(), ",newArea=", newArea, ",currentSchduleId=", currentSchduleId);
            }
        } else if (bossDeadState == ScheduleConstant.BOSS_1_DEAD) { // 打死第1个世界boss
            StaticArea staticArea = StaticWorldDataMgr.getAreaMap().get(player.lord.getArea());
            if (!staticArea.getOpenAreaId().contains(newArea) && newArea != player.lord.getArea()) {
                throw new MwException(GameError.AREA_NOT_OPEN.getCode(), "迁城,该区域未开放, roleId:,", roleId, ",area=",
                        player.lord.getArea(), ",newArea=", newArea, ",currentSchduleId=", currentSchduleId);
            }
        } else if (bossDeadState == ScheduleConstant.BOSS_2_DEAD) { // 第二个世界boss打死到处可以飞

        }

        // 德意志等级判断
        if (newArea == WorldConstant.AREA_TYPE_13
                && !StaticFunctionDataMgr.funcitonIsOpen(player, BuildingType.ENTER_AREA_13_COND)) {
            throw new MwException(GameError.AREA_NOT_OPEN.getCode(), "迁城 你自身的条件为达到, roleId:,", roleId);
        }
    }

    /**
     * 获取世界boss状态
     *
     * @return 0 第1个boss未打死, 1 第1个boss打死, 2 第2个boss打死
     */

    public int bossDeadState() {
        // CommonPb.WorldTask boss1 =
        // worldDataManager.getWorldTask().getWorldTaskMap().get(TaskType.WORLD_BOSS_TASK_ID_1);
        // CommonPb.WorldTask boss2 =
        // worldDataManager.getWorldTask().getWorldTaskMap().get(TaskType.WORLD_BOSS_TASK_ID_2);
        // if (boss1 == null || boss1.getHp() > 0) { // 第1个世界boss没打死
        // return 0;
        // } else if (boss2 != null && boss2.getHp() <= 0) {// 第2个boss打死
        // return 2;
        // } else {
        // return 1;
        // }

        // 使用 新的世界进度 boss的判断
        return worldScheduleService.getBossDeadState();

    }

    /**
     * 玩家迁城
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public MoveCityRs moveCity(long roleId, MoveCityRq req) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        if (!CheckNull.isEmpty(player.armys)) {
            throw new MwException(GameError.MOVE_HERO_OUT.getCode(), "迁城，有将领未返回, roleId:,", roleId);
        }

        if (player.getDecisiveInfo().isDecisive()) {
            throw new MwException(GameError.DECISIVE_BATTLE_ING.getCode(), "玩家正在决战中,不能迁城, roleId:", roleId);
        }

        int pos = req.getPos();
        int type = req.getType();
        int prePos = player.lord.getPos();

        int preAreaId = MapHelper.getAreaIdByPos(prePos);
        StaticArea preSArea = StaticWorldDataMgr.getAreaMap().get(preAreaId);
        if (preSArea == null) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "自己所在的区域有错误, roleId:,", roleId, ", prePos:", prePos);
        }

        // 根据类型获取相应道具类型
        int propId = 0;
        if (type == WorldConstant.MOVE_TYPE_POS) {
            propId = PropConstant.ITEM_ID_5003;
            if (pos == prePos) {
                throw new MwException(GameError.MOVE_SAME_POS.getCode(), "定点迁城，目标坐标为玩家当前坐标, roleId:,", roleId, ", pos:",
                        pos);
            }

            if (!worldDataManager.isEmptyPos(pos)) {
                throw new MwException(GameError.MOVE_NOT_EMPTY_POS.getCode(), "定点迁城，目标坐标不为空, roleId:,", roleId,
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
            throw new MwException(GameError.NO_CONFIG.getCode(), "迁城，参数传入错误, roleId:,", roleId, ", pos:",
                    pos + ",type:" + type);
        }

        if (type == WorldConstant.MOVE_TYPE_AREA || type == WorldConstant.MOVE_TYPE_POS) {// 指定区域
            // 检查能否飞
            int newArea = MapHelper.getAreaIdByPos(pos);
            StaticArea staticArea = StaticWorldDataMgr.getAreaMap().get(newArea);
            if (null == staticArea || !staticArea.isOpen()) {
                throw new MwException(GameError.AREA_NOT_OPEN.getCode(), "获取分区数据，分区未开启, roleId:", roleId, ", area:",
                        newArea);
            }
            checkMoveCityByBoss(player, newArea);
        }

        // 郡迁城令的判断
        if (type == WorldConstant.MOVE_TYPE_OPEN_ORDER_1) {
            int preArea = player.lord.getArea();
            int newArea = MapHelper.getAreaIdByPos(pos);
            if (preArea == newArea) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "不允许在本区域使用跨郡道具迁城   roleId:", roleId,
                        ", preArea:", preArea, ", newArea:", newArea);
            }
            int bossDeadState = worldScheduleService.getBossDeadState();
            // 开了州之后不能使用
            if (bossDeadState != ScheduleConstant.BOSS_NO_DEAD) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "已经开州城不让使用郡迁城令   roleId:", roleId);
            }
            StaticArea sArea = StaticWorldDataMgr.getAreaMap().get(newArea);
            if (WorldConstant.AREA_ORDER_1 != sArea.getOpenOrder()) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "郡迁城令只能在郡之间迁城   roleId:", roleId, ", newArea:",
                        newArea);
            }
        }

        // 前往州城判断,自己必须在郡中
        if (type == WorldConstant.MOVE_TYPE_TO_STATE) {
            if (preSArea.getOpenOrder() != WorldConstant.AREA_ORDER_1) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "自己所在的区域有错误, roleId:,", roleId, ", preAreaId:",
                        preAreaId);
            }
            // 判断世界boss的清空
            int bossState = bossDeadState();
            if (bossState == 0) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "第一个世界boss没死不能去州城, roleId:,", roleId,
                        ", preAreaId:", preAreaId);
            }
            if (bossState == 1) {
                // 第一个boss已经死掉,但是第二boss没死,消耗3个低级迁城令
                rewardDataManager.checkPropIsEnough(player, propId, 3, "玩家迁城");
                rewardDataManager.subProp(player, propId, 3, AwardFrom.MOVE_CITY);// ,
                // "玩家迁城"
            }
        } else {
            rewardDataManager.checkPropIsEnough(player, propId, 1, "玩家迁城");
            rewardDataManager.subProp(player, propId, 1, AwardFrom.MOVE_CITY);// ,
            // "玩家迁城"
        }
        // 根据道具类型计算新坐标
        int newPos = 0;
        if (type == WorldConstant.MOVE_TYPE_POS) {
            newPos = pos;
        } else if (type == WorldConstant.MOVE_TYPE_AREA) {
            // 皇城区域的判断
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
                throw new MwException(GameError.NO_CONFIG.getCode(), "区域配置错误,", roleId, ", preAreaId:", preAreaId);
            }
            newPos = worldDataManager.randomEmptyPosInArea(newAreaId);
        }

        // 如果有攻击该玩家的城战还未开始，结束城战
        if (player.battleMap.containsKey(prePos)) {// 改到玩家到达后没发现目标
            warService.cancelCityBattle(prePos, newPos, false, true);
        }

        // 友军的城墙驻防部队返回
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
                int heroId = army.getHero().get(0).getPrincipleHeroId();
                mailDataManager.sendNormalMail(tPlayer, MailConstant.MOLD_GARRISON_RETREAT, now, player.lord.getNick(),
                        heroId, player.lord.getNick(), heroId);
            }
            //清空所有驻防部队
            if (CheckNull.nonEmpty(guradArays)) {
                LogUtil.error("clear roleId: {}, pos: {} all error guard army", roleId, prePos);
                worldDataManager.removePlayerGuard(prePos);
            }
        }

        int newArea = MapHelper.getAreaIdByPos(newPos);
        LogUtil.debug(
                roleId + ",迁城" + ",当前所在area=" + player.lord.getArea() + ",newPos=" + newPos + ",newArea=" + newArea);

        // 更新玩家坐标
        if (playerDataManager.getPlayerByArea(player.lord.getArea()) != null) {
            playerDataManager.getPlayerByArea(player.lord.getArea()).remove(roleId);
        } else {
            LogUtil.debug(roleId + ",迁城找不到Map" + ",当前所在area=" + player.lord.getArea() + ",newPos=" + newPos
                    + ",newArea=" + newArea);
        }
        player.lord.setPos(newPos);
        player.lord.setArea(newArea);
        worldDataManager.removePlayerPos(prePos, player);
        worldDataManager.putPlayer(player);

        // 荣耀日报添加
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
        // 通知其他玩家数据改变
        onPlayerPosChangeCallbcak(player, prePos, newPos, WorldConstant.CHANGE_POS_TYPE_1);
        EventBus.getDefault().post(
                new Events.AreaChangeNoticeEvent(posList, roleId, Events.AreaChangeNoticeEvent.MAP_AND_AREA_TYPE));
        //玩家迁城后 如果是当前玩家发起了集结则需要变更坐标信息在推送SyncRallyBattleRs协议 & 玩家是否有召唤权限 & 在召唤时间内
        if (Objects.nonNull(player.summon) && player.summon.getStatus() != 0 && StaticPartyDataMgr.jobHavePrivilege(player.lord.getJob(), PartyConstant.PRIVILEGE_CALL) && player.summon.getLastTime() + Constant.SUMMON_KEEP_TIME > TimeHelper.getCurrentSecond()) {
            //推送同阵营集结信息
            this.sendSameCampAssembleInfo(player);
        }
        return builder.build();
    }

    /**
     * 某个玩家坐标发生改变时回调方法
     *
     * @param player
     * @param prePos
     * @param newPos
     * @param type   1 迁城, 2 被击飞 , 3 召唤
     */
    public void onPlayerPosChangeCallbcak(Player player, int prePos, int newPos, int type) {
        if (type == WorldConstant.CHANGE_POS_TYPE_2) { // 被击飞
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
     * 侦查
     *
     * @param roleId
     * @param pos
     * @param type
     * @return
     * @throws MwException
     */
    public ScoutPosRs scoutPos(long roleId, int pos, int type) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // 科技限制
        if (!techDataManager.isOpen(player, TechConstant.TYPE_11)) {
            throw new MwException(GameError.SCOUT_POS_NEED_TECH.getCode(), "不能侦查,请提升科技等级, roleId:,", roleId, ", pos:",
                    pos);
        }
        if (CrossWorldMapService.isOnCrossMap(player)) { // 在新地图走其他侦查
            return crossAttackService.scoutPos(player, pos, type);
        }
        if (!worldDataManager.isPlayerPos(pos)) {
            throw new MwException(GameError.POS_NO_PLAYER.getCode(), "侦查，目标坐标没有玩家, roleId:,", roleId, ", pos:", pos);
        }
        Player target = worldDataManager.getPosData(pos);

        int cdTime = player.common.getScoutCdTime();
        int now = TimeHelper.getCurrentSecond();
        if (cdTime > now + WorldConstant.SCOUT_CD_MAX_TIME) {
            throw new MwException(GameError.SCOUT_CD_TIME.getCode(), "侦查超过最大允许CD时间, roleId:", roleId, ", pos:", pos,
                    ", cdTime:", cdTime);
        }

        // 触发对方的一次自动补兵
        playerDataManager.autoAddArmy(target);
        int cityLv = target.building.getCommand();

        StaticScoutCost ssc = StaticScoutDataMgr.getScoutCostByCityLv(cityLv);
        if (null == ssc) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "侦查消耗未配置, roleId:,", roleId, ", cityLv:", cityLv);
        }

        List<List<Integer>> costList;
        if (type == WorldConstant.SCOUT_TYPE_PRIMARY) {
            costList = ssc.getPrimary();
        } else if (type == WorldConstant.SCOUT_TYPE_MIDDLE) {
            costList = ssc.getMiddle();
        } else if (type == WorldConstant.SCOUT_TYPE_SENIOR) {
            costList = ssc.getSenior();
        } else {
            throw new MwException(GameError.SCOUT_TYPE_ERROR.getCode(), "侦查类型不正确, roleId:,", roleId, ", type:", type);
        }

        // 检查并扣除消耗
        rewardDataManager.checkAndSubPlayerRes(player, costList, AwardFrom.SCOUT);

        // 记录玩家侦查CD
        if (cdTime < now) {
            cdTime = now;
        }
        cdTime += WorldConstant.SCOUT_CD;
        player.common.setScoutCdTime(cdTime);

        int scoutLv = player.getTechLvById(WorldConstant.SCOUT_TECH_ID);
        int targetLv = target.getTechLvById(WorldConstant.SCOUT_TECH_ID);
        int gap = scoutLv - targetLv;
        gap += WorldConstant.getScoutAddByType(type);// 侦查类型加成

        Mail mail = null;
        Lord tarLord = target.lord;
        Turple<Integer, Integer> tarXy = MapHelper.reducePos(pos);
        Lord lord = player.lord;
        Turple<Integer, Integer> xy = MapHelper.reducePos(lord.getPos());
        int ret = StaticScoutDataMgr.randomScoutResultByLvGap(gap);
        if (ret != WorldConstant.SCOUT_RET_FAIL) {
            // 通知被侦查方敌人侦查成功
            mailDataManager.sendReportMail(target, null, MailConstant.MOLD_ENEMY_SCOUT_SUCC, null, now, lord.getCamp(),
                    lord.getLevel(), lord.getNick(), xy.getA(), xy.getB(), lord.getCamp(), lord.getLevel(),
                    lord.getNick(), xy.getA(), xy.getB());

            // 推送被侦查消息
            // PushMessageUtil.pushMessage(target.account, PushConstant.ID_SCOUTED, target.lord.getNick(), lord.getNick());

            // 根据侦查结果计算侦察到的信息
            CommonPb.ScoutRes sRes = null;
            CommonPb.ScoutCity city = null;
            List<CommonPb.ScoutHero> sHeroList = null;
            if (ret >= WorldConstant.SCOUT_RET_SUCC1) {// 只获取资源信息
                Resource res = target.resource;
                // 仓库保护
                long[] proRes = buildingDataManager.getProtectRes(target);
                Map<Integer, Integer> canPlunderRes = buildingDataManager.canPlunderScout(target, player, proRes);
                List<TwoInt> canPlunderList = new ArrayList<>();
                for (Entry<Integer, Integer> kv : canPlunderRes.entrySet()) {
                    TwoInt ti = PbHelper.createTwoIntPb(kv.getKey(), kv.getValue());
                    canPlunderList.add(ti);
                }
                sRes = PbHelper.createScoutResPb(proRes[1], proRes[2], proRes[0], res.getOre(), res.getHuman(),
                        canPlunderList);
                if (ret >= WorldConstant.SCOUT_RET_SUCC2) {// 获取资源、城池信息
                    city = PbHelper.createScoutCityPb(target.building.getWall(), tarLord.getFight(),
                            (int) res.getArm1(), (int) res.getArm2(), (int) res.getArm3());
                    if (ret >= WorldConstant.SCOUT_RET_SUCC3) {// 获取资源、城池、将领信息
                        List<PartnerHero> defheros = target.getAllOnBattleHeroList();// 玩家所有上阵将领信息
                        sHeroList = new ArrayList<>();
                        int state;
                        int source;
                        for (PartnerHero hero : defheros) {
                            source = WorldConstant.HERO_SOURCE_BATTLE;
                            state = getScoutHeroState(source, hero.getPrincipalHero().getState());
                            sHeroList.add(PbHelper.createScoutHeroPb(hero.getPrincipalHero(), source, state, target));
                        }

                        // 城防将、城防军、协防驻守玩家的将领信息
                    }
                }
            }
            CommonPb.MailScout scout = PbHelper.createMailScoutPb(sRes, city, sHeroList);
            mail = mailDataManager.sendScoutMail(player, MailConstant.MOLD_SCOUT_SUCC, scout, now, tarLord.getCamp(),
                    tarLord.getLevel(), tarLord.getNick(), tarXy.getA(), tarXy.getB(), tarLord.getCamp(),
                    tarLord.getLevel(), tarLord.getNick(), tarXy.getA(), tarXy.getB());
        } else {// 侦查失败邮件
            mail = mailDataManager.sendScoutMail(player, MailConstant.MOLD_SCOUT_FAIL, null, now, tarLord.getCamp(),
                    tarLord.getLevel(), tarLord.getNick(), tarXy.getA(), tarXy.getB(), tarLord.getCamp(),
                    tarLord.getLevel(), tarLord.getNick(), tarXy.getA(), tarXy.getB());
            // 给敌方发侦查失败邮件
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
     * 清除CD时间
     *
     * @param roleId
     * @param type
     * @return
     * @throws MwException
     */
    public ClearCDRs clearCD(long roleId, int type) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int cdTime = 0;
        if (type == WorldConstant.CD_TYPE_SCOUT) {
            cdTime = player.common.getScoutCdTime();
        }

        int now = TimeHelper.getCurrentSecond();
        if (cdTime <= now) {
            throw new MwException(GameError.NO_CD_TIME.getCode(), "没有需要清除的CD时间, roleId:,", roleId, ", type:", type);
        }

        int cost = (int) Math.ceil(player.common.getScoutCdTime() - now * 1.00 / 60);

        // 检查并扣除消耗
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, cost,
                AwardFrom.CLEAR_CD);

        // 清除CD时间
        if (type == WorldConstant.CD_TYPE_SCOUT) {
            player.common.setScoutCdTime(0);
        }

        ClearCDRs.Builder builder = ClearCDRs.newBuilder();
        return builder.build();
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

    public void fightMineGuard(Player atkplayer, Army army, int now) {
        int pos = army.getTarget();
        Guard guard = worldDataManager.getGuardByPos(pos);
        if (null == guard) {// 正常情况下，不应该走进这里
            LogUtil.error("矿点守卫不存在, pos:" + pos);
            StaticMine staticMine = worldDataManager.getMineByPos(pos);
            if (null == staticMine) {
                noMineRetreat(atkplayer, army, now);
            } else {
                collectArmy(atkplayer, army, now);
            }
            return;
        }

        // 战斗计算
        Player defPlayer = guard.getPlayer();

        // 给被攻击玩家推送消息（应用外推送）
        // PushMessageUtil.pushMessage(defPlayer.account, PushConstant.COLLECT_BY_ATTCK, atkplayer.lord.getNick());

        Fighter attacker = fightService.createFighter(atkplayer, army.getHero());
        Fighter defender = fightService.createFighter(defPlayer, guard.getForm());
        FightLogic fightLogic = new FightLogic(attacker, defender, true);
        fightLogic.start();

        //貂蝉任务-杀敌阵亡数量
        ActivityDiaoChanService.killedAndDeathTask0(attacker, true, true);
        ActivityDiaoChanService.killedAndDeathTask0(defender, true, true);

        // 记录玩家有改变的资源类型, key:roleId
        Map<Long, ChangeInfo> changeMap = new HashMap<>();
        // 兵力恢复
        Map<Long, List<Award>> recoverArmyAwardMap = new HashMap<>();

        // 损兵处理
        if (attacker.lost > 0) {
            subHeroArm(atkplayer, attacker.forces, AwardFrom.ATTACK_GUARD, changeMap);
            // 执行勋章白衣天使特技逻辑
            medalDataManager.angelInWhite(attacker, recoverArmyAwardMap);
            //执行赛季天赋技能---伤病恢复
            seasonTalentService.execSeasonTalentEffect303(attacker, recoverArmyAwardMap);
            // 损兵排行
            activityDataManager.updRankActivity(atkplayer, ActivityConst.ACT_ARMY_RANK, attacker.lost);
            // 荣耀日报损兵进度
            honorDailyDataManager.addAndCheckHonorReport2s(atkplayer, HonorDailyConstant.COND_ID_14, attacker.lost);
            // 战令的损兵进度
            battlePassDataManager.updTaskSchedule(atkplayer.roleId, TaskType.COND_SUB_HERO_ARMY, attacker.lost);
            // 采集战玩家杀敌数量计入每日杀敌任务
            dailyAttackTaskService.addPlayerDailyAttackOther(defPlayer, attacker.lost);
        }
        if (defender.lost > 0) {
            subHeroArm(defPlayer, defender.forces, AwardFrom.DEFEND_GUARD, changeMap);
            // 执行勋章白衣天使特技逻辑
            medalDataManager.angelInWhite(defender, recoverArmyAwardMap);
            //执行赛季天赋技能---伤病恢复
            seasonTalentService.execSeasonTalentEffect303(defender, recoverArmyAwardMap);
            // 损兵排行
            activityDataManager.updRankActivity(defPlayer, ActivityConst.ACT_ARMY_RANK, defender.lost);
            // 荣耀日报损兵进度
            honorDailyDataManager.addAndCheckHonorReport2s(defPlayer, HonorDailyConstant.COND_ID_14, defender.lost);
            // 战令的损兵进度
            battlePassDataManager.updTaskSchedule(defPlayer.roleId, TaskType.COND_SUB_HERO_ARMY, defender.lost);
            // 采集战玩家杀敌数量计入每日杀敌任务
            dailyAttackTaskService.addPlayerDailyAttackOther(atkplayer, defender.lost);
        }

        // 战斗记录
        Lord atkLord = atkplayer.lord;
        Lord defLord = defPlayer.lord;
        boolean isSuccess = fightLogic.getWinState() == FightConstant.FIGHT_RESULT_SUCCESS;

        BattlePb.BattleRoundPb record = fightLogic.generateRecord();
        CommonPb.RptAtkPlayer.Builder rpt = CommonPb.RptAtkPlayer.newBuilder();
        rpt.setResult(isSuccess);
        rpt.setAttack(PbHelper.createRptMan(atkLord.getPos(), atkLord.getNick(), atkLord.getVip(), atkLord.getLevel()));
        rpt.setDefMan(PbHelper.createRptMan(defLord.getPos(), defLord.getNick(), defLord.getVip(), defLord.getLevel()));
        rpt.setAtkSum(PbHelper.createRptSummary(attacker.total, attacker.lost, atkLord.getCamp(), atkLord.getNick(),
                atkLord.getPortrait(), atkplayer.getDressUp().getCurPortraitFrame()));
        rpt.setDefSum(PbHelper.createRptSummary(defender.total, defender.lost, defLord.getCamp(), defLord.getNick(),
                defLord.getPortrait(), defPlayer.getDressUp().getCurPortraitFrame()));

        int addExp = 0;// 进攻玩家，只有进攻胜利时，进攻方会获得玩家经验。经验（临时）= 进攻方玩家等级*2
        rpt.addAllAtkHero(fightSettleLogic.mineFightHeroExpReward(atkplayer, attacker.forces));
        rpt.addAllDefHero(fightSettleLogic.mineFightHeroExpReward(defPlayer, defender.forces));
        rpt.setRecord(record);

        Turple<Integer, Integer> atkPos = MapHelper.reducePos(atkLord.getPos());
        Turple<Integer, Integer> defPos = MapHelper.reducePos(defLord.getPos());
        CommonPb.Report.Builder report = createAtkPlayerReport(rpt.build(), now);

        //上报数数(攻击方)
        EventDataUp.battle(atkplayer.account, atkplayer.lord, attacker, "atk", CheckNull.isNull(army.getBattleId()) ? "0" : String.valueOf(army.getBattleId()), String.valueOf(WorldConstant.BATTLE_TYPE_MINE_GUARD),
                String.valueOf(fightLogic.getWinState()), atkplayer.roleId, rpt.getAtkHeroList());
        //上报数数(防守方)
        EventDataUp.battle(defPlayer.account, defPlayer.lord, defender, "def", CheckNull.isNull(army.getBattleId()) ? "0" : String.valueOf(army.getBattleId()), String.valueOf(WorldConstant.BATTLE_TYPE_MINE_GUARD),
                String.valueOf(fightLogic.getWinState()), atkplayer.roleId, rpt.getDefHeroList());

        //进攻方胜利或者防守方兵力为0，防守方结束采集
        boolean collectEnd = false;
        if (isSuccess || Objects.isNull(defender.getAliveForce())) {
            // 防御方采集结算
            int resource = calcCollect(defPlayer, guard.getArmy(), now);
            // 将领采集经验 采集增加将领经验 = 采集时间分钟 * 20
            int time = now - guard.getBeginTime();
            guard.getArmy().setCollectTime(time);// 设置采集时长
            // 失败方部队返回
            retreatArmyByDistance(defPlayer, guard.getArmy(), now);
            // 更新矿点资源
            worldDataManager.putMineResource(pos, resource);
            // 移除采集守卫信息
            worldDataManager.removeMineGuard(pos);

            addExp = (int) Math.ceil(time * 1.0 / Constant.MINUTE) * 20;
            addExp = heroService.adaptHeroAddExp(defPlayer, addExp);
            // 给将领加经验
            Hero hero = defPlayer.heros.get(guard.getHeroId());
            int chiefAddExp = heroService.addHeroExp(hero, addExp, defLord.getLevel(), defPlayer);
            DataResource.ac.getBean(WorldService.class).addDeputyHeroExp(addExp, guard.getArmy().getHero().get(0), defPlayer);

            LogUtil.error("roleId:", defPlayer.roleId, ", ===采集时间:", time, ", 将领经验:", chiefAddExp,
                    ", 采集物质:" + guard.getGrab());
            boolean effect = mineService.hasCollectEffect(defPlayer, guard.getGrab().get(0).getId(),
                    new Date(guard.getBeginTime() * 1000L), guard.getArmy());// 采集加成;

            CommonPb.MailCollect collect = PbHelper.createMailCollectPb(time, hero, chiefAddExp, guard.getGrab(), effect);
            //战报邮件
            mailDataManager.sendCollectMail(defPlayer, report, MailConstant.MOLD_COLLECT_DEF_FAIL, null, now,
                    recoverArmyAwardMap, defLord.getNick(), atkLord.getNick(), atkLord.getNick(), atkPos.getA(),
                    atkPos.getB(), defLord.getNick(), defPos.getA(), defPos.getB());
            //采集邮件
            mailDataManager.sendCollectMail(defPlayer, null, MailConstant.MOLD_COLLECT_DEF_FAIL_COLLECT, collect, now,
                    recoverArmyAwardMap, defLord.getNick(), atkLord.getNick(), atkLord.getNick(), atkPos.getA(),
                    atkPos.getB(), defLord.getNick(), defPos.getA(), defPos.getB());

            // 推送失败方部队状态
            synRetreatArmy(defPlayer, guard.getArmy(), now);

            //貂蝉任务-采集资源
            ActivityDiaoChanService.completeTask(defPlayer, ETask.COLLECT_RES, guard.getGrab().get(0).getId(), guard.getGrab().get(0).getCount());
            TaskService.processTask(defPlayer, ETask.COLLECT_RES, guard.getGrab().get(0).getId(), guard.getGrab().get(0).getCount());

            collectEnd = true;
        }
        if (isSuccess) {// 进攻方胜利，原来采集的部队返回，进攻方部队开始采集
            // 进攻方开始采集
            collectArmy(atkplayer, army, now);

            // 发送战报邮件
            mailDataManager.sendCollectMail(atkplayer, report, MailConstant.MOLD_COLLECT_ATK_SUCC, null, now,
                    recoverArmyAwardMap, atkLord.getNick(), defLord.getNick(), atkLord.getNick(), atkPos.getA(),
                    atkPos.getB(), defLord.getNick(), defPos.getA(), defPos.getB());
        } else {// 进攻方失败，返回
            retreatArmyByDistance(atkplayer, army, now);

            // 发送战报邮件
            mailDataManager.sendCollectMail(atkplayer, report, MailConstant.MOLD_COLLECT_ATK_FAIL, null, now,
                    recoverArmyAwardMap, atkLord.getNick(), defLord.getNick(), atkLord.getNick(), atkPos.getA(),
                    atkPos.getB(), defLord.getNick(), defPos.getA(), defPos.getB());
            if (!collectEnd) {
                mailDataManager.sendCollectMail(defPlayer, report, MailConstant.MOLD_COLLECT_DEF_SUCC, null, now,
                        recoverArmyAwardMap, defLord.getNick(), atkLord.getNick(), atkLord.getNick(), atkPos.getA(),
                        atkPos.getB(), defLord.getNick(), defPos.getA(), defPos.getB());
            }
        }

        //移除当前战斗警报
        if (army.getBattleId() != null && army.getBattleId() > 0) {
            warDataManager.removeBattleById(army.getBattleId());
        }
        // 通知客户端玩家资源变化
        sendRoleResChange(changeMap);
    }

    /**
     * 推送部队状态
     *
     * @param defPlayer
     * @param army
     * @param now
     */
    public void synRetreatArmy(Player defPlayer, Army army, int now) {

        Base.Builder msg = PbHelper.createSynBase(SyncArmyRs.EXT_FIELD_NUMBER, SyncArmyRs.ext,
                SyncArmyRs.newBuilder().setArmy(PbHelper.createArmyPb(army, false)).build());
        MsgDataManager.getIns().add(new Msg(defPlayer.ctx, msg.build(), defPlayer.roleId));
        LogUtil.debug("推送部队状态target=" + army.getTarget() + ",player=" + defPlayer.roleId);
    }

    /**
     * 计算玩家采集到的资源量
     *
     * @param player
     * @param army
     * @param now
     * @return 返回采集后的剩余资源数
     */
    public int calcCollect(Player player, Army army, int now) {
        return mineService.calcCollect(player, army, now);
    }

    /**
     * 采集部队到达目的地后，没有矿，返回
     *
     * @param player
     * @param army
     * @param now
     */
    public void noMineRetreat(Player player, Army army, int now) {
        // 发送邮件通知
        mailDataManager.sendCollectMail(player, null, MailConstant.MOLD_COLLECT_NO_TARGET, null, now);

        // 部队返回
        retreatArmyByDistance(player, army, now);

        //取消采矿被攻击提示
        cancelMineBattle(army.getTarget(), now, player);
        //清除矿点后续采集队列
        MineData mineData = worldDataManager.getMineMap().get(army.getTarget());
        if (Objects.nonNull(mineData))
            mineData.clearCollectTeam();
    }

    /**
     * 进攻目标丢失
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
        // 部队返回
        retreatArmyByDistance(player, army, now);
    }

    /**
     * 开启采集
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

        int heroId = army.getHero().get(0).getPrincipleHeroId();
        army.setState(ArmyConstant.ARMY_STATE_COLLECT);
        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(heroId);
        // 计算能采集的最大时间
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

        army.setHeroState(player, ArmyConstant.ARMY_STATE_COLLECT);

        //矿点被占领后, 后续若有玩家采集部队增加被攻击提示
        syncRallyMineBattle(pos, army, now, player);
    }

    /**
     * 同步矿点战斗提示
     *
     * @param pos
     * @param army
     * @param now
     * @param player
     */
    private void syncRallyMineBattle(int pos, Army army, int now, Player player) {
        //采集被占领后，后续有采集者就有战斗产生，发送队伍信息
        MineData mineData = worldDataManager.getMineMap().get(pos);
        if (ObjectUtils.isEmpty(mineData.getCollectTeam())) {
            return;
        }

        int defArmCount = army.getArmCount();

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
                    //添加被打提示
                    attackArmCount += cArmy.getArmCount();

                    battle = createMineBattle(player, collectPlayer, pos, attackArmCount, defArmCount, now, cArmy.getEndTime() - now);
                    cArmy.setBattleId(battle.getBattleId());
                    warDataManager.addBattle(collectPlayer, battle);
                } else {
                    //取消之前被攻击者提示
                    DataResource.getBean(CampService.class).syncCancelRallyBattle(null, battle, null);

                    //添加采矿玩家警报提示
                    battle.setDefencer(player);
                    battle.setDefArm(defArmCount);
                    battle.setDefCamp(player.getCamp());
                    battle.setDefencerId(player.getLordId());
                    DataResource.getBean(CampService.class).syncRallyBattle(collectPlayer, battle, null);
                }
            }
        }
    }

    /**
     * 创建矿点战斗对象
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
     * 玩家采集结束处理
     *
     * @param guard
     * @param now
     */
    public void finishCollect(Guard guard, int now) {
        List<Award> grab = guard.getGrab();// 采集获得
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
                    new Date(guard.getBeginTime() * 1000L), guard.getArmy());// 采集加成
            guard.getArmy().setCollectTime(time);
            int addExp = (int) Math.ceil(time * 1.0 / Constant.MINUTE) * 20;// 将领采集经验
            addExp = heroService.adaptHeroAddExp(player, addExp);
            // 给将领加经验
            int chiefAddExp = heroService.addHeroExp(hero, addExp, player.lord.getLevel(), player);
            // 给副将加经验
            DataResource.ac.getBean(WorldService.class).addDeputyHeroExp(addExp, guard.getArmy().getHero().get(0), player);

            CommonPb.MailCollect collect = PbHelper.createMailCollectPb(time, hero, chiefAddExp, grab, effect);

            // 记录玩家采集铀矿时间
            WorldWarSeasonDailyRestrictTaskService restrictTaskService = DataResource.ac
                    .getBean(WorldWarSeasonDailyRestrictTaskService.class);
            restrictTaskService.updatePlayerDailyRestrictTask(player, TaskType.COND_WORLD_WAR_MINE_CNT,
                    guard.getArmy().getCollectTime());
            // 发送邮件通知
            mailDataManager.sendCollectMail(player, null, MailConstant.MOLD_COLLECT, collect, now,
                    grab.get(0).getType(), grab.get(0).getId(), grab.get(0).getCount(), staticMine.getLv(),
                    staticMine.getMineId(), xy.getA(), xy.getB());
            // 推送
            pushMsgCollect(player, grab.get(0));

            //貂蝉任务-采集资源
            ActivityDiaoChanService.completeTask(player, ETask.COLLECT_RES, staticMine.getMineType(), grab.get(0).getCount());
            TaskService.processTask(player, ETask.COLLECT_RES, staticMine.getMineType(), grab.get(0).getCount());
        } catch (Exception e) {
            LogUtil.error("采集完成 出错 roleId:", player.roleId, ", army:", guard.getArmy());
        }

        // 部队返回
        retreatArmyByDistance(player, guard.getArmy(), now);
        //取消矿点战斗攻击提示
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
            LogUtil.error("采集资源名称未找到，跳过消息推送, roleId:", player.roleId, ", id:", txtId);
            return;
        }
        // PushMessageUtil.pushMessage(player.account, PushConstant.COLLECT_COMPLETE, name);
    }

    /*-------------------------------夜袭功能start-----------------------------------*/

    /**
     * 获取夜袭功能信息
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
        int openServerDay = serverSetting.getOpenServerDay(new Date());// 开服第几天
        builder.setCurOpenServerDay(openServerDay);
        return builder.build();
    }

    /*-------------------------------夜袭功能end-----------------------------------*/

    /**
     * 检测当前任务列表中是否有流寇任务
     *
     * @param player
     * @return true 表示任务列表中有匪军任务
     */
    public boolean checkCurTaskHasBandit(Player player) {
        return !Collections.disjoint(Constant.ATK_BANDIT_MARCH_TIME_TASKID, player.chapterTask.getOpenTasks().keySet()); // 有交集
    }

    /**
     * 检测当前流寇是否可以加速
     *
     * @param lv       当前攻打的匪军等级
     * @param banditLv 记录已攻打的匪军最大等级
     * @return 是否加速
     */
    public boolean checkCurTaskHasBandit(int lv, int banditLv) {
        return (lv >= Constant.ATK_BANDIT_MARCH_TIME_TASKID.get(0) && lv <= Constant.ATK_BANDIT_MARCH_TIME_TASKID.get(1)) && lv > banditLv;
    }

    /**
     * 计算兵力恢复具值
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

        // 兵力恢复换算成奖励
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
     * 扣除将领损失兵力
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
            if (force.killed > 0 && from != AwardFrom.ATTACK_BANDIT) {
                // 大杀四方
                activityDataManager.updActivity(player, ActivityConst.ACT_BIG_KILL, force.killed, 0, true);
            }
        }

    }

    private boolean checkNullBattle(Player player, Army army, int now) {
        Integer battleId = army.getBattleId();
        if (null == battleId) {
            LogUtil.error("玩家参加战斗id, roleId:", player.roleId, ", battleId:", battleId);
            battleId = 0;
        }
        Battle battle = warDataManager.getBattleMap().get(battleId);
        if (null == battle) {
            LogUtil.error("玩家参加战斗信息, roleId:", player.roleId, ", battle:", battle);
            // 战斗对象未找到
            return true;
        }
        return false;
    }

    public void addBattleArmy(Battle battle, long roleId, List<CommonPb.PartnerHeroIdPb> heroIdList, int keyId, boolean isAtk) {
        if (isAtk) {
            battle.getAtkList()
                    .add(BattleRole.newBuilder().setKeyId(keyId).setRoleId(roleId).addAllPartnerHeroId(heroIdList).build());
        } else {
            battle.getDefList()
                    .add(BattleRole.newBuilder().setKeyId(keyId).setRoleId(roleId).addAllPartnerHeroId(heroIdList).build());
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

    /*--------------------------------部队返回start-----------------------------------*/

    /**
     * 队伍返回,带地图同步的
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
        // // 皇城打下面州郡的情况(特殊情况)
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
        // 提示通知返回
        List<Integer> posList = MapHelper
                .getAreaStartPos(MapHelper.getLineAcorss(army.getTarget(), player.lord.getPos()));
        posList.add(army.getTarget());
        posList.add(player.lord.getPos());
        EventBus.getDefault()
                .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));
    }

    /**
     * 队伍返回 不带地图同步
     *
     * @param player 玩家对象
     * @param army   行军队列
     * @param now    现在的时间
     */
    public void retreatArmy(Player player, Army army, int now) {
        if (null == player || null == army || army.isRetreat()) {
            return;
        }
        // 玩家撤回已加入城战或阵营战的部队，相关处理
        Integer battleId = army.getBattleId();
        if (null != battleId && battleId > 0) {
            Battle battle = warDataManager.getBattleMap().get(battleId);
            if (null != battle) {
                int camp = player.lord.getCamp();
                int armCount = army.getArmCount();
                LogUtil.debug(player.roleId + ",撤退部队=" + armCount);
                // 扣除部队的兵力
                battle.updateArm(camp, -armCount);
            }
        }
        // 部队返回直接取去的时候的时间， 以免暴露被击飞后的坐标, 走多远返回就多远
        int marchTime = marchTime(player, army.getTarget(), army.getType() == ArmyConstant.ARMY_TYPE_ATK_BANDIT);
        // 半路撤回
        marchTime = berlinWarService.getMarchTime(player, army, marchTime);
        marchTime = gestapoService.getGestapoMarchTime(army, marchTime);

        //遗迹行军减半
        if (army.getType() == ArmyConstant.ARMY_TYPE_RELIC_BATTLE)
            marchTime = (int) (marchTime * ActParamConstant.RELIC_MARCH_SPEEDUP / NumberUtil.TEN_THOUSAND_DOUBLE);

        // 皇城并且是跨区域判断重新计算
        int startArea = MapHelper.getAreaIdByPos(player.lord.getPos());
        int targetArea = MapHelper.getAreaIdByPos(army.getTarget());
        if (startArea == WorldConstant.AREA_TYPE_13 && startArea != targetArea) {
            // 皇城打下面州郡的情况(特殊情况)
            // marchTime = WorldConstant.SPECIAL_MARCH_TIME;
            marchTime = army.getDuration(); // 去多久回多久
        }

        // 不在一个区域的部队撤回,重新添加一次,解决中途被击飞到领一个区域
        if (MapHelper.getAreaIdByPos(army.getTarget()) != player.lord.getArea()) {
            March march = new March(player, army);
            worldDataManager.addMarch(march);
        }

        army.setState(ArmyConstant.ARMY_STATE_RETREAT);
        army.setDuration(marchTime);
        army.setEndTime(now + marchTime);

        army.setHeroState(player, ArmyConstant.ARMY_STATE_RETREAT);
    }

    /**
     * 主动使用道具召回部队情况
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
        if (army.getEndTime() > now && army.getState() == ArmyConstant.ARMY_STATE_MARCH) {// 折半只有在行军过程中才有
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
        } else {// 默认方式
            army.setDuration(marchTime);
            army.setEndTime(now + marchTime);
        }

        army.setHeroState(player, ArmyConstant.ARMY_STATE_RETREAT);
    }

    /*--------------------------------部队返回 end-----------------------------------*/

    public void buildRptHeroData(Fighter defender, CommonPb.RptAtkBandit.Builder rpt, boolean calAward) {
        for (Force force : defender.forces) {
            int award = 0;
            if (calAward) {
                award = (int) Math.ceil(BattleUtil.addPlayerMilitary(defender.fightLogic.getBattleType(), force));
            }
            Player tmpP = DataResource.ac.getBean(PlayerDataManager.class).getPlayer(force.ownerId);
            if (CheckNull.isNull(tmpP)) {
                rpt.addDefHero(
                        PbHelper.createRptHero(Constant.Role.BANDIT, force.killed, award, force, null, 0, 0, force.totalLost));
            } else {
                rpt.addDefHero(
                        PbHelper.createRptHero(Constant.Role.BANDIT, force.killed, award, force, playerDataManager.getNickByLordId(force.ownerId), 0, 0, force.totalLost));
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
                        PbHelper.createRptHero(roleType, force.killed, award, force, null, 0, 0, force.totalLost));
            } else {
                rpt.addDefHero(
                        PbHelper.createRptHero(roleType, force.killed, award, force, playerDataManager.getNickByLordId(force.ownerId), 0, 0, force.totalLost));
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
     * 行军状态判断定时器，处理采集、打流寇、打任务流寇、点兵统领、驻防
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
                LogUtil.error(e, "执行行军结束定时任务出现错误, lordId:", player.lord.getLordId());
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
                    if (state == ArmyConstant.ARMY_STATE_MARCH) {// 行军结束
                        marchService.marchEnd(player, army, now);
                    } else if (state == ArmyConstant.ARMY_STATE_RETREAT) {// 返回结束
                        marchService.retreatEnd(player, army);
                        it.remove();
                    } else if (state == ArmyConstant.ARMY_STATE_GUARD) {// 驻防结束
                        retreatArmyByDistance(player, army, now);
                        synWallCallBackRs(0, army);
                        // 发送驻防结束邮件
                        final Army fArmy = army;
                        Optional.of(fArmy).flatMap(a -> Optional.of(a.getTarLordId()))// j8的null判断
                                .flatMap(roleId -> Optional.ofNullable(playerDataManager.getPlayer(roleId)))
                                .ifPresent(tPlayer -> {
                                    mailDataManager.sendNormalMail(player, MailConstant.MOLD_GARRISON_FILL_TIME_RETREAT,
                                            now, tPlayer.lord.getNick(), fArmy.getHero().get(0).getPrincipleHeroId(),
                                            tPlayer.lord.getNick(), fArmy.getHero().get(0).getPrincipleHeroId());
                                });
                        worldDataManager.removePlayerGuard(fArmy.getTarget(), fArmy);
                    } else if (state == ArmyConstant.ARMY_STATE_BATTLE) {// 城战或阵营战
                        if (army.getType() == ArmyConstant.ARMY_TYPE_ATK_PLAYER
                                || army.getType() == ArmyConstant.ARMY_TYPE_ATK_CAMP) {
                            if (checkNullBattle(player, army, now)) {// 找不到战报，行军撤回
                                marchService.marchEnd(player, army, now);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.error("行军结束，操作报错, roleIdId:" + player.lord.getLordId() + ", army:" + army + Arrays.toString(e.getStackTrace()));
            }
        }
    }

    /**
     * 通知驻防信息
     *
     * @param status 状态(0不在驻防 1在驻防)
     * @param army
     */
    public void synWallCallBackRs(int status, Army army) {
        if (army == null) {
            LogUtil.debug("通知驻防,army is null");
            return;
        }
        Player player = playerDataManager.getPlayer(army.getLordId());
        if (player == null) {
            LogUtil.debug("通知驻防,player is null," + army.getLordId());
            return;
        }
        // 重新获取兵力
        int armyCnt = army.getHero().get(0).getCount();
        Hero hero = player.heros.get(army.getHero().get(0).getPrincipleHeroId());
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
        LogUtil.debug("驻防推送target=" + army.getTarget() + ",player=" + army.getLordId() + ",status=" + status);
        Player target = worldDataManager.getPosData(army.getTarget());
        if (target == null) {
            LogUtil.debug("通知驻防,target is null," + army.getTarget());
            return;
        }
        MsgDataManager.getIns().add(new Msg(target.ctx, msg.build(), target.roleId));
    }

    /**
     * 行军加速
     *
     * @param roleId
     * @param type   1高级行军加速2顶级行军加速3行军召回4高级行军召回
     * @param keyId
     * @return
     * @throws MwException
     */
    public MoveCDRs moveCd(long roleId, int type, int keyId) throws MwException {
        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (player.lord.getArea() > WorldConstant.AREA_MAX_ID) {
            return crossArmyService.moveCd(player, type, keyId);
        }
        Army army = player.armys.get(keyId);
        if (army == null) {
            throw new MwException(GameError.NO_CD_TIME.getCode(), "行军加速没有战斗, roleId:,", roleId, ", type:", type);
        }
        if ((type == ArmyConstant.MOVE_TYPE_1 || type == ArmyConstant.MOVE_TYPE_2)
                && army.getState() != ArmyConstant.ARMY_STATE_RETREAT) {
            throw new MwException(GameError.NO_CD_TIME.getCode(), "只有撤回才能行军加速, roleId:,", roleId, ", type:", type);
        }
        int now = TimeHelper.getCurrentSecond();
        if (now > army.getEndTime()) {
            throw new MwException(GameError.NO_CD_TIME.getCode(), "行军加速传参错误, roleId:,", roleId, ", type:", type);
        }
        int propId = 0;
        if (type == ArmyConstant.MOVE_TYPE_1) {
            propId = PropConstant.ITEM_ID_5011;
        } else if (type == ArmyConstant.MOVE_TYPE_2) {
            propId = PropConstant.ITEM_ID_5012;
        } else {
            throw new MwException(GameError.NO_CD_TIME.getCode(), "行军加速传参错误, roleId:,", roleId, ", type:", type);
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

        // 更新地图速度
        processMarchArmy(player.lord.getArea(), army, keyId);

        builder.setArmy(PbHelper.createArmyPb(army, false));
        builder.setStatus(1);
        builder.setProp(Prop.newBuilder().setPropId(propId).setCount(player.props.get(propId).getCount()));
        // 通知加速
        // Player target = worldDataManager.getPosData(army.getTarget());
        // if (target != null && target.isLogin) {
        // syncAttackRole(target, player.lord, army.getEndTime(), WorldConstant.ATTACK_ROLE_0);
        // }
        return builder.build();
    }

    // ============================召唤相关start============================

    /**
     * 发起集结入口
     */
    public InitiateGatherEntranceRs initiateGatherEntrance(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        InitiateGatherEntranceRs.Builder initiateGatherEntranceRs = InitiateGatherEntranceRs.newBuilder().setStatus(false);
        int schdule = worldScheduleService.getCurrentSchduleId();
        if (schdule < ScheduleConstant.SCHEDULE_ID_6) {
            LogUtil.error("世界进程不匹配! 当前时间进程:" + schdule);
            if (player.getMixtureData().containsKey(PlayerConstant.WHETHER_ASSEMBLY_ENTRANCE)) {
                player.cleanMixtureData(PlayerConstant.WHETHER_ASSEMBLY_ENTRANCE);
            }
            return initiateGatherEntranceRs.build();
        }

        Camp camp = campService.getCampInfo(player.roleId, player.getCamp());
        // 检查军团是否开启了选举
        if (!camp.isInEceltion()) {
            LogUtil.error("玩家投票，军团未开启投票, status:" + camp.getStatus());
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
     * 发起召唤
     *
     * @param lordId
     * @return
     * @throws MwException
     */
    public SummonTeamRs summonTeam(GamePb2.SummonTeamRq req, long lordId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(lordId);

        // 检测玩家是否有权限
        int job = player.lord.getJob();
        int privilege = PartyConstant.PRIVILEGE_CALL;
        if (!StaticPartyDataMgr.jobHavePrivilege(job, privilege)) {
            throw new MwException(GameError.NO_PRIVILEGE.getCode(), "没有这个特权, roleId:", lordId, ", job:", job,
                    ", privilege:", privilege);
        }
        if (player.summon == null) {
            player.summon = new Summon();
        }

        Summon summon = player.summon;
        Date lastDate = new Date(summon.getLastTime() * 1000L);
        if (!DateHelper.isToday(lastDate)) {
            summon.setCount(0); // 次数清零
        }
        // 获取次数和召唤人数
        List<Integer> val = StaticPartyDataMgr.getJobPrivilegeVal(job, privilege);
        if (CheckNull.isEmpty(val) || val.size() != 3) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "发起召唤,官员特权配置错误");
        }
        int cnt = val.get(1); // 可用次数
        int sum = val.get(2); // 可召唤的人数

        summon.setSum(sum);
        // 刷新状态
        int now = TimeHelper.getCurrentSecond();
        int endTime = summon.getLastTime() + Constant.SUMMON_KEEP_TIME;
        if (now > endTime || summon.getRespondId().size() >= sum) {
            summon.getRespondId().clear();
            summon.setStatus(0);
        }
        // 未开启召唤
        if (summon.getStatus() == 0) {
            if (req.hasTy() && req.getTy() == 1) {
                throw new MwException(GameError.SUMMON_NOT_OPEN.getCode(), String.format("roleId :%d, 本次召唤已结束, 已召唤次数 :%d", lordId, summon.getCount()));
            }
            if (summon.getCount() > cnt) {
                throw new MwException(GameError.SUMMON_COUNT_NOT_ENOUGH.getCode(), "roleId:", lordId, ", 已召唤次数",
                        summon.getCount(), " 召唤次数不够");
            }
            // 消耗
            if (summon.getCount() > 0) {// 首次免费
                if (CheckNull.isEmpty(Constant.SUMMON_NEED_PROP_CNT)
                        || Constant.SUMMON_NEED_PROP_CNT.get(job) == null) {
                    throw new MwException(GameError.NO_CONFIG.getCode(), "召唤消耗配置错误");
                }
                rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.PROP, PropConstant.ITEM_ID_5001,
                        Constant.SUMMON_NEED_PROP_CNT.get(job), AwardFrom.USE_PROP);
            }
            summon.setLastTime(now);
            summon.setStatus(1);
            summon.getRespondId().clear();
            summon.setCount(summon.getCount() + 1);
        }
        if (summon.getStatus() != 0) { // 召唤已结束不发聊天
            // 发消息
            chatDataManager.sendSysChat(ChatConst.CHAT_SUMMON_TEAM, player.lord.getCamp(), 0, player.lord.getJob(),
                    player.lord.getNick(), player.lord.getArea(), player.lord.getCamp(), player.lord.getPos(), lordId);
        }
        SummonTeamRs.Builder builder = SummonTeamRs.newBuilder();
        builder.setEndTime(summon.getLastTime() + Constant.SUMMON_KEEP_TIME);
        builder.setCount(summon.getCount());
        builder.setStatus(summon.getStatus());

        // 通知其他玩家地图数据改变
        if (player.lord.getArea() > WorldConstant.AREA_MAX_ID) {
            // 新地图的推送
            int mapId = player.lord.getArea();
            CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(mapId);
            if (cMap != null) {
                cMap.publishMapEvent(MapEvent.mapEntity(player.lord.getPos(), MapCurdEvent.UPDATE));
            }
        } else {
            // 老地图的推送
            List<Integer> posList = new ArrayList<>();
            posList.add(player.lord.getPos());
            EventBus.getDefault()
                    .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_AREA_TYPE));
        }
        //推送同阵营集结信息
        this.sendSameCampAssembleInfo(player);
        //关闭世界界面发起集结入口banner
        if (player.getMixtureDataById(PlayerConstant.WHETHER_ASSEMBLY_ENTRANCE) != 0) {
            player.setMixtureData(PlayerConstant.WHETHER_ASSEMBLY_ENTRANCE, 0);
        }
        return builder.build();
    }

    /**
     * 推送同阵营集结信息
     */
    private void sendSameCampAssembleInfo(Player player) throws MwException {
        //推送SyncRallyBattleRs集结信息
        CommonPb.Assemble assemble = this.syncAssemblyInfo(player);
        //获取同一阵营的玩家推送当前summonPlayer发起的集结信息因次数已满需移除客户端界面显示的信息
        Optional.ofNullable(playerDataManager.getPlayerByCamp(player.getCamp())).ifPresent(map -> {
            map.values().forEach(puPay -> {
                Base.Builder msg = PbHelper.createRsBase(GamePb3.SyncRallyBattleRs.EXT_FIELD_NUMBER, GamePb3.SyncRallyBattleRs.ext,
                        GamePb3.SyncRallyBattleRs.newBuilder().setAssemble(assemble).build());
                MsgDataManager.getIns().add(new Msg(puPay.ctx, msg.build(), puPay.roleId));
            });
        });
    }

    /**
     * 生成Assemble集结信息
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
     * 响应召唤
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
            throw new MwException(GameError.DECISIVE_BATTLE_NO_SUMMON.getCode(), "自己处于决战中不能响应召唤");
        }
        if (lordId == summonId) {
            throw new MwException(GameError.SUMMON_PARAM_NOT_CONFORM.getCode(), "自己不能响应自己的召唤");
        }
        if (player.lord.getCamp() != summonPlayer.lord.getCamp()) {
            throw new MwException(GameError.SUMMON_PARAM_NOT_CONFORM.getCode(), "非同一阵营,不能响应召唤");
        }
        if (summonPlayer.summon == null) {
            // 对方没有开启召唤
            throw new MwException(GameError.SUMMON_NOT_OPEN.getCode(), "对方为开启召唤");
        }
        // 新地图 和老地图之前不能响应召唤
        int preArea = player.lord.getArea();
        int willGoArea = summonPlayer.lord.getArea();
        if (preArea > WorldConstant.AREA_MAX_ID ^ willGoArea > WorldConstant.AREA_MAX_ID) {
            throw new MwException(GameError.SUMMON_CANNOT_IN_WAR_FIRE.getCode(), "新老地图之间不能进行召唤 preArea:", preArea,
                    ", willGoArea:", willGoArea);
        }
        if ((preArea > WorldConstant.AREA_MAX_ID && willGoArea > WorldConstant.AREA_MAX_ID && preArea != willGoArea)) {
            throw new MwException(GameError.SUMMON_CANNOT_IN_WAR_FIRE.getCode(), "新地图与新地图之间不能进行召唤 preArea:", preArea,
                    ", willGoArea:", willGoArea);
        }

        if (!CheckNull.isEmpty(player.armys)) {
            throw new MwException(GameError.MOVE_HERO_OUT.getCode(), "迁城，有将领未返回, roleId:,", lordId);
        }
        // 对方无特权
        int job = summonPlayer.lord.getJob();
        int privilege = PartyConstant.PRIVILEGE_CALL;
        if (!StaticPartyDataMgr.jobHavePrivilege(job, privilege)) {
            throw new MwException(GameError.NO_PRIVILEGE.getCode(), "响应召唤,对方没有这个特权, roleId:", lordId, ", job:", job,
                    ", privilege:", privilege);
        }
        if (!CheckNull.isEmpty(player.armys)) {
            throw new MwException(GameError.MOVE_HERO_OUT.getCode(), "响应召唤，有将领未返回, roleId:,", lordId);
        }
        // 获取次数和召唤人数
        List<Integer> val = StaticPartyDataMgr.getJobPrivilegeVal(summonPlayer.lord.getJob(), privilege);
        if (CheckNull.isEmpty(val) || val.size() != 3) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "发起召唤,官员特权配置错误, roleId:", lordId);
        }
        int sum = val.get(2); // 可召唤的人数
        // 对方召唤是否结束
        Summon summon = summonPlayer.summon;
        int now = TimeHelper.getCurrentSecond();
        int endTime = summon.getLastTime() + Constant.SUMMON_KEEP_TIME;
        if (summon.getStatus() == 0 || now > endTime || summon.getRespondId().size() >= sum) {
            throw new MwException(GameError.SUMMON_NOT_OPEN.getCode(), "召唤结束,或还未开启召唤");
        }
        // 新地图的响应召唤
        if (summonPlayer.lord.getArea() > WorldConstant.AREA_MAX_ID) {
            return crossSummonService.summonRespond(player, summonPlayer);
        } else { // 老地图的召唤
            // 判断区域
            List<Integer> scope = MapHelper.getRoundPos(summonPlayer.lord.getPos(), Constant.SUMMON_RESPOND_RADIUS);
            if (scope.contains(player.lord.getPos())) {
                // 距离对方距离太近禁止迁城
                throw new MwException(GameError.SUMMON_DISTANCE_TOO_CLOSE.getCode(), "距离太近不能响应召唤");
            }
            // 在对方周围随机一个空坐标
            List<Integer> scope2 = MapHelper.getRoundPos(summonPlayer.lord.getPos(), Constant.SUMMON_RADIUS);
            scope2 = scope2.stream().filter(pos -> worldDataManager.isEmptyPos(pos)).collect(Collectors.toList());// 得出全部空位
            if (CheckNull.isEmpty(scope2)) {
                throw new MwException(GameError.SUMMON_NOT_POS.getCode(), "对方周围没有空位");
            }
            // 迁城逻辑
            int prePos = player.lord.getPos();

            int newPos = scope2.get(RandomHelper.randomInSize(scope2.size()));
            int newArea = MapHelper.getAreaIdByPos(newPos);
            LogUtil.debug("roleId:", lordId, ", 即将被召唤到 area:", newArea, ", pos:", newPos);

            // 如果有攻击该玩家的城战还未开始，结束城战
            if (player.battleMap.containsKey(prePos)) {// 改到玩家到达后没发现目标
                warService.cancelCityBattle(prePos, newPos, false, true);
            }

            StaticArea staticArea = StaticWorldDataMgr.getAreaMap().get(newArea);
            if (null == staticArea || !staticArea.isOpen()) {
                throw new MwException(GameError.AREA_NOT_OPEN.getCode(), "获取分区数据，分区未开启, roleId:", lordId, ", area:",
                        newArea);
            }

            // 友军的城墙驻防部队返回
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
                    int heroId = army.getHero().get(0).getPrincipleHeroId();
                    mailDataManager.sendNormalMail(tPlayer, MailConstant.MOLD_GARRISON_RETREAT, now, player.lord.getNick(),
                            heroId, player.lord.getNick(), heroId);
                }
                //清空所有驻防部队
                if (CheckNull.nonEmpty(guardArrays)) {
                    LogUtil.error("clear roleId: {}, pos: {} all error guard army", lordId, prePos);
                    worldDataManager.removePlayerGuard(prePos);
                }
            }

            // 更新玩家坐标
            if (playerDataManager.getPlayerByArea(player.lord.getArea()) != null) {
                playerDataManager.getPlayerByArea(player.lord.getArea()).remove(lordId);
            } else {
                LogUtil.debug(lordId + ",召唤迁城找不到Map" + ",当前所在area=" + player.lord.getArea() + ",newPos=" + newPos
                        + ",newArea=" + MapHelper.getAreaIdByPos(newPos));
            }
            player.lord.setPos(newPos);
            player.lord.setArea(MapHelper.getAreaIdByPos(newPos));
            worldDataManager.removePlayerPos(prePos, player);
            worldDataManager.putPlayer(player);

            // 给召唤者添加人数
            summon.getRespondId().add(lordId);
            if (summon.getRespondId().size() >= sum) { // 如果满了就结束
                //如果响应人数达到阈值则推送移除红点信息
                GamePb3.SyncRallyBattleRs.Builder syncRallyBattleRs = GamePb3.SyncRallyBattleRs.newBuilder();
                syncRallyBattleRs.setAssemble(this.syncAssemblyInfo(summonPlayer));
                syncRallyBattleRs.setStatus(1);
                //获取同一阵营的玩家推送当前summonPlayer发起的集结信息因次数已满需移除客户端界面显示的信息
                Optional.ofNullable(playerDataManager.getPlayerByCamp(player.getCamp())).ifPresent(map -> {
                    map.values().forEach(puPay -> {
                        Base.Builder msg = PbHelper.createRsBase(GamePb3.SyncRallyBattleRs.EXT_FIELD_NUMBER, GamePb3.SyncRallyBattleRs.ext, syncRallyBattleRs.build());
                        MsgDataManager.getIns().add(new Msg(puPay.ctx, msg.build(), puPay.roleId));
                    });
                });

                summon.setStatus(0);
                summon.getRespondId().clear();
            }
            // 推送召唤数据
            syncSummonState(summonPlayer, summonPlayer.lord.getLordId(), summon);
            SummonRespondRs.Builder builder = SummonRespondRs.newBuilder();
            builder.setPos(newPos);

            // 通知其他玩家地图数据改变
            List<Integer> posList = new ArrayList<>();
            posList.add(newPos);
            posList.add(prePos);
            posList.add(summonPlayer.lord.getPos());
            onPlayerPosChangeCallbcak(player, prePos, newPos, WorldConstant.CHANGE_POS_TYPE_3);
            EventBus.getDefault()
                    .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_AREA_TYPE));

            //关闭响应人的世界界面 发起集结入口banner
            if (player.getMixtureDataById(PlayerConstant.WHETHER_ASSEMBLY_ENTRANCE) != 0) {
                player.setMixtureData(PlayerConstant.WHETHER_ASSEMBLY_ENTRANCE, 0);
            }
            return builder.build();
        }
    }

    /**
     * 推送召唤信息
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
        LogUtil.debug("推送召唤信息状态 targetRoleId=", targer.roleId, ", endTime:", endTime, ", status:" + summon.getStatus());
    }

    /**
     * 获取召唤信息
     *
     * @param lordId
     * @return
     * @throws MwException
     */
    public GetSummonRs getSummon(long lordId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(lordId);

        // 检测玩家是否有权限
        int job = player.lord.getJob();
        int privilege = PartyConstant.PRIVILEGE_CALL;
        if (!StaticPartyDataMgr.jobHavePrivilege(job, privilege)) {
            throw new MwException(GameError.NO_PRIVILEGE.getCode(), "没有这个特权, roleId:", lordId, ", job:", job,
                    ", privilege:", privilege);
        }
        if (player.summon == null) {
            player.summon = new Summon();
        }
        Summon summon = player.summon;
        Date lastDate = new Date(summon.getLastTime() * 1000L);
        if (!DateHelper.isToday(lastDate)) {
            summon.setCount(0); // 次数清零
        }
        Integer battleCount = player.getMixtureData().get(PlayerConstant.DECISIVE_BATTLE_COUNT);
        LogUtil.debug("getSummon 已召唤次数:", summon.getCount());
        GetSummonRs.Builder builder = GetSummonRs.newBuilder();
        if (battleCount == null) {
            builder.setDecisiveBattleCount(0);
        } else {
            builder.setDecisiveBattleCount(battleCount);
        }
        builder.setCount(player.summon.getCount());
        return builder.build();
    }

    // ============================召唤相关end============================

    /**
     * 进入世界 /返回基地
     *
     * @param lordId
     * @param isEnter
     * @return
     * @throws MwException
     */
    public EnterWorldRs enterWorld(long lordId, boolean isEnter) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(lordId);
        EnterWorldRs.Builder builder = EnterWorldRs.newBuilder();
        if (isEnter) {// 进入世界
            builder.addAllOpenArea(StaticWorldDataMgr.getOpenAreaIdSet());
            int now = TimeHelper.getCurrentSecond();
            // 刷新流寇
            if (player.enterWorldCnt == 0 && now - player.lord.getOffTime() >= Constant.REFRESH_BANDITS_OFFLINE_TIME) {
                LogUtil.debug("执行刷新流寇规则4: roleId:", lordId, ", ");
                worldDataManager.processBanditByPlayer(player);
            }
            player.enterWorldCnt++;
        } else {// 退出世界
            EventBus.getDefault().post(new Events.RmMapFocusEvent(player));
        }
        //圣诞活动 是否显示圣诞效果
        builder.setChristmas(activityChristmasService.isShowEffect(player) ? 1 : 0);
        //演唱会效果
//        builder.setShowConcert(musicFestivalCreativeService.showConcert(player));
        return builder.build();
    }

    /**
     * 客户端屏幕焦点所在区域
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
     * 获取每个区域中心城池状态
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
     * 获取玩家当前所在区域的首杀信息
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
     * 向玩家推送占领的城池被人攻击完成消息
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
            LogUtil.error("城市名称未找到，跳过消息推送, roleId:", account.getLordId(), ", cityId:", cityId);
            return;
        }

        // 推送 消息
        // PushMessageUtil.pushMessage(account, PushConstant.ATTCK_CAMP_BATTLE, nick, areaName, cityName);
    }

}
