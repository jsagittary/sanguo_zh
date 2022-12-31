package com.gryphpoem.game.zw.gameplay.local.service;

import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.gameplay.local.constant.CrossWorldMapConstant;
import com.gryphpoem.game.zw.gameplay.local.constant.cross.NewCrossConstant;
import com.gryphpoem.game.zw.gameplay.local.manger.CrossWorldMapDataManager;
import com.gryphpoem.game.zw.gameplay.local.util.MapCurdEvent;
import com.gryphpoem.game.zw.gameplay.local.util.MapEvent;
import com.gryphpoem.game.zw.gameplay.local.util.dto.AttackParamDto;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.WorldEntityType;
import com.gryphpoem.game.zw.gameplay.local.world.WorldWarOpen;
import com.gryphpoem.game.zw.gameplay.local.world.WorldWarPlanInfo;
import com.gryphpoem.game.zw.gameplay.local.world.army.BaseArmy;
import com.gryphpoem.game.zw.gameplay.local.world.army.MapMarch;
import com.gryphpoem.game.zw.gameplay.local.world.army.PlayerArmy;
import com.gryphpoem.game.zw.gameplay.local.world.battle.BaseMapBattle;
import com.gryphpoem.game.zw.gameplay.local.world.battle.BaseMapBattle.CancelBattleType;
import com.gryphpoem.game.zw.gameplay.local.world.battle.MapWarData;
import com.gryphpoem.game.zw.gameplay.local.world.map.*;
import com.gryphpoem.game.zw.gameplay.local.world.warfire.GlobalWarFire;
import com.gryphpoem.game.zw.gameplay.local.world.warfire.PlayerWarFire;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.MapCity;
import com.gryphpoem.game.zw.pb.CommonPb.MapForce.Builder;
import com.gryphpoem.game.zw.pb.CommonPb.MapLine;
import com.gryphpoem.game.zw.pb.GamePb5.*;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.army.Guard;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.world.Battle;
import com.gryphpoem.game.zw.resource.pojo.world.CounterAttack;
import com.gryphpoem.game.zw.resource.pojo.world.GlobalRebellion;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.service.WarService;
import com.gryphpoem.game.zw.service.WorldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 地图相关
 */
@Component
public class CrossWorldMapService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private CrossWorldMapDataManager crossWorldMapDataManager;
    @Autowired
    private CrossAttackService crossAttackService;
    @Autowired
    private WarService warService;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private WorldDataManager worldDataManager;
    @Autowired
    private WorldService worldService;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private GlobalDataManager globalDataManager;

    /**
     * 跨服获取区域数据
     *
     * @param roleId 玩家id
     * @param req    请求参数
     * @return 响应参数
     * @throws MwException
     */
    public GetCrossAreaRs getCrossArea(long roleId, GetCrossAreaRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        CrossWorldMap cMap = checkCrossWorldMap(player, req.getMapId());
        GetCrossAreaRs.Builder builder = GetCrossAreaRs.newBuilder();
        // 玩家数据
        cMap.getPlayerMap().values()
                .stream()
                .map(PlayerMapEntity::toAreaForcePb)
                .forEach(builder::addForce);

        // 城池信息
        cMap.getCityMap().values()
                .stream()
                .map(mapCity -> mapCity.toAreaCityPb(player, cMap))
                .filter(Objects::nonNull)
                .forEach(builder::addCity);

        // 安全区信息
        cMap.getSafeArea()
                .stream()
                .map(WFSafeAreaMapEntity::toAreaSafePb)
                .filter(Objects::nonNull)
                .forEach(builder::addSafe);

        // 矿点信息
        cMap.getAllMap()
                .values()
                .stream()
                .filter(entity -> entity.getType() == WorldEntityType.MINE)
                .map(entity -> (MineMapEntity) entity)
                .map(MineMapEntity::toAreaForcePb)
                .forEach(builder::addMine);

        // 获取玩家自己的行军线路
        MapMarch mapMarchArmy = cMap.getMapMarchArmy();
        PlayerArmy playerArmy = mapMarchArmy.getPlayerArmyMap().get(roleId);
        if (playerArmy != null && player.lord.getArea() == req.getMapId()) {
            for (BaseArmy army : playerArmy.getArmy().values()) {
                if (army.getState() == ArmyConstant.ARMY_STATE_RETREAT) {
                    builder.addLine(PbHelper.createTwoIntPb(army.getArmy().getTarget(), player.lord.getPos()));
                } else if (army.getState() == ArmyConstant.ARMY_STATE_MARCH) {
                    builder.addLine(PbHelper.createTwoIntPb(player.lord.getPos(), army.getArmy().getTarget()));
                }
            }
        }
        return builder.build();
    }

    /**
     * @param p     玩家对象
     * @param mapId 地图id
     * @return 地图数据
     * @throws MwException 自定义异常
     */
    public CrossWorldMap checkCrossWorldMap(Player p, int mapId) throws MwException {
        List<List<Integer>> warFireOpenCondConf = WorldConstant.WAR_FIRE_OPEN_COND_CONF;
        if (!CheckNull.isEmpty(warFireOpenCondConf)) {
            Integer lvConf = warFireOpenCondConf.get(0).get(0);
            // 战火燎原参与等级
            if (p.lord.getLevel() < lvConf) {
                throw new MwException(GameError.WAR_FIRE_JOIN_LEVEL_NOT_ENOUGH.getCode(), "参加战火燎原等级不足 roleId:", p.roleId);
            }
        }
        // 做检测
        CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(mapId);
        // if (!StaticFunctionDataMgr.funcitonIsOpen(p, FunctionConstant.FUNC_ID_ENTER_WORLDWAR)) {
        //     throw new MwException(GameError.FUNCTION_LOCK.getCode(), "世界争霸功能未解锁 roleId:", p.roleId);
        // }
        if (cMap == null) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "此地图不存在 mapId:", mapId);
        }
        GlobalWarFire globalWarFireOpen = cMap.getGlobalWarFire();
        if (globalWarFireOpen == null) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "地图还未开放 mapId:", mapId);
        }
        if (globalWarFireOpen.getStage() == GlobalWarFire.STAGE_OVER) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "地图还未开放 mapId:", mapId);
        }
        return cMap;
    }

    /**
     * 获取地图数据
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GetCrossMapRs getCrossMap(long roleId, GetCrossMapRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int mapId = req.getMapId();
        CrossWorldMap cMap = checkCrossWorldMap(player, mapId);
        GetCrossMapRs.Builder builder = GetCrossMapRs.newBuilder();
        List<Integer> cellList = req.getBlockList();
        for (Integer cellId : cellList) {
            cMap.addWatcher(roleId, cellId); // 添加观察者
            List<Integer> cellPosList = cMap.getCellPosList(cellId);
            if (CheckNull.isEmpty(cellPosList)) {
                continue;
            }
            for (Integer pos : cellPosList) {
                BaseWorldEntity baseWorldEntity = cMap.getAllMap().get(pos);
                if (baseWorldEntity == null) continue;
                if (baseWorldEntity.getPos() != pos) { // 防止多点物体,打印多次
                    continue;
                }
                if (baseWorldEntity.getType() == WorldEntityType.CITY) { // 城池
                    if (baseWorldEntity instanceof CityMapEntity) {
                        CityMapEntity city = (CityMapEntity) baseWorldEntity;
                        MapCity mapCityPb = city.toMapCityPb(player, cMap);
                        if (mapCityPb != null) builder.addCity(mapCityPb);
                    }
                } else { // 其他数据
                    Builder mapForcePbBuilder = baseWorldEntity.toMapForcePb(cMap);
                    if (mapForcePbBuilder != null) {
                        builder.addForce(mapForcePbBuilder.build());
                    }
                }
            }
        }
        builder.addAllBlock(cellList);
        return builder.build();
    }

    /**
     * 获取部队信息
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GetCrossMarchRs getCrossMarch(long roleId, GetCrossMarchRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int mapId = req.getMapId();
        CrossWorldMap cMap = checkCrossWorldMap(player, mapId);
        MapMarch mapMarchArmy = cMap.getMapMarchArmy();
        GetCrossMarchRs.Builder builder = GetCrossMarchRs.newBuilder();
        List<MapLine> mapLineListPb = mapMarchArmy.getDelayArmysQueue().getQueue().stream()
                .filter(BaseArmy::isInMarch).map(baseArmy -> baseArmy.toMapLinePb(mapMarchArmy))
                .filter(Objects::nonNull).collect(Collectors.toList());
        builder.addAllMarch(mapLineListPb);
        return builder.build();
    }

    /**
     * 获取矿点采集详情
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GetCrossMineRs getCrossMine(long roleId, GetCrossMineRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int mapId = req.getMapId();
        int pos = req.getPos();
        CrossWorldMap cMap = checkCrossWorldMap(player, mapId);
        GetCrossMineRs.Builder builder = GetCrossMineRs.newBuilder();
        BaseWorldEntity baseWorldEntity = cMap.getAllMap().get(pos);
        if (baseWorldEntity != null && baseWorldEntity.getType() == WorldEntityType.MINE
                && baseWorldEntity instanceof MineMapEntity) {
            MineMapEntity mine = (MineMapEntity) baseWorldEntity;
            Guard guard = mine.getGuard();
            if (null != guard) {
                CommonPb.Collect.Builder collect = PbHelper.createCollectBuilder(guard);
                builder.setCollect(collect.build());
                int surplus = mine.calcRemainRes();
                builder.setStartTime(guard.getBeginTime());
                builder.setEndTime(guard.getEndTime());
                builder.setResource(surplus);
                // 如果是自己采集的矿，返回玩家已采集到的资源数
                if (guard.getRoleId() == roleId) {
                    int nowSec = TimeHelper.getCurrentSecond();
                    builder.setGrab(mine.calcCollect(nowSec));
//                    builder.setGrab(mine.getRemainRes() - surplus);
                }
            } else {
                builder.setResource(mine.calcRemainRes());
            }
        }
        return builder.build();
    }

    /**
     * 检测该玩家是否在对应的地图上
     *
     * @param roleId
     * @param cMap
     * @throws MwException
     */
    public static void checkPlayerOnMap(long roleId, CrossWorldMap cMap) throws MwException {
        if (!cMap.getPlayerMap().containsKey(roleId)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "不在该地图上不能进行操作, roleId:", roleId, ", mapId:",
                    cMap.getMapId());
        }
    }

    /**
     * 是否在跨服的地图上
     *
     * @param player
     * @return
     */
    public static boolean isOnCrossMap(Player player) {
        int area = player.lord.getArea();
        return area > WorldConstant.AREA_MAX_ID && area < NewCrossConstant.CROSS_WAR_FIRE_MAP;
    }

    /**
     * 进入或离开跨服地图
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public EnterLeaveCrossRs enterLeaveCross(long roleId, EnterLeaveCrossRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int mapId = req.getMapId();
        CrossWorldMap cMap = checkCrossWorldMap(player, mapId);
        if (!req.getEnter()) {
            cMap.removeWatcher(roleId);
        } else {
            cMap.enterMap(roleId);
        }
        EnterLeaveCrossRs.Builder builder = EnterLeaveCrossRs.newBuilder();
        return builder.build();
    }

    /**
     * 获取地图的开放情况
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GetCrossMapInfoRs getCrossMapInfo(long roleId, GetCrossMapInfoRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int mapId = req.getMapId();
        CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(mapId);
        GetCrossMapInfoRs.Builder builder = GetCrossMapInfoRs.newBuilder();
        if (cMap != null) {
            WorldWarOpen worldWarOpen = cMap.getWorldWarOpen();
            WorldWarPlanInfo worldWarPlanInfo = worldWarOpen.getWorldWarPlanInfo();
            if (worldWarPlanInfo != null) {
                builder.setPlan(WorldWarPlanInfo.toWorldWarPlanInfoPb(worldWarPlanInfo));
            }
            GlobalWarFire globalWarFire = cMap.getGlobalWarFire();
            if (Objects.nonNull(globalWarFire)) {
                builder.setWfInfo(GlobalWarFire.toWarFireInfoPb(globalWarFire, player));
            }
            builder.setOpenType(cMap.getMapOpenType().getId());
        }
        return builder.build();
    }

    /**
     * 获取跨服的战斗详情
     *
     * @param roleId 玩家角色id
     * @param req    请求参数
     * @return 坐标点的所有战斗
     * @throws MwException 自定义异常
     */
    public GetCrossBattleRs getCrossBattle(long roleId, GetCrossBattleRq req) throws MwException {

        int mapId = req.getMapId();
        int pos = req.getPos();
        CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(mapId);
        GetCrossBattleRs.Builder builder = GetCrossBattleRs.newBuilder();
        if (cMap != null) {
            List<BaseMapBattle> battles = cMap.getMapWarData().getBattlesByPos(pos);
            if (!CheckNull.isEmpty(battles)) {
                battles.forEach(baseBattle -> builder.addBattle(PbHelper.createBattlePb(baseBattle.getBattle(),
                        BaseMapBattle.getDefArmCntByBattle(baseBattle.getBattle(), cMap))));

            }
        }
        return builder.build();
    }

    /**
     * 跨服加入战斗
     *
     * @param roleId 玩家角色id
     * @param req    请求参数
     * @return army和battle的信息
     * @throws MwException 自定义异常
     */
    public JoinBattleCrossRs joinBattleCross(long roleId, JoinBattleCrossRq req) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int mapId = req.getMapId();
        int battleId = req.getBattleId();
        List<Integer> heroIdList = req.getHeroIdList().stream().distinct().collect(Collectors.toList());

        CrossWorldMap cMap = checkCrossWorldMap(player, mapId);
        CrossWorldMapService.checkPlayerOnMap(roleId, cMap);

        BaseMapBattle baseMapBattle = cMap.getMapWarData().getAllBattles().get(battleId);
        if (baseMapBattle == null) {
            throw new MwException(GameError.BATTLE_NOT_FOUND.getCode(), "战争信息未找到, roleId:", roleId, ", battleId:",
                    battleId);
        }
        int pos = baseMapBattle.getBattle().getPos();
        // 检查出征将领信息
        crossAttackService.checkFormHeroSupport(player, heroIdList, pos, cMap);

        // 行军时间
        int marchTime = cMap.marchTime(cMap, player, player.lord.getPos(), pos);
        // 计算时间是否赶得上
        int now = TimeHelper.getCurrentSecond();
        Battle battle = baseMapBattle.getBattle();
        if (now + marchTime > battle.getBattleTime()) {
            throw new MwException(GameError.BATTLE_CD_TIME.getCode(), "加入城战,赶不上时间, roleId:", roleId, ", pos:",
                    pos + ",行军时间=" + (now + marchTime) + ",城战倒计时=" + battle.getBattleTime());
        }
        // 检查补给
        int armCount = heroIdList.stream().mapToInt(heroId -> {
            Hero hero = player.heros.get(heroId);
            return hero == null ? 0 : hero.getCount();
        }).sum(); // 出兵的总兵力
        int needFood = worldService.checkMarchFood(player, marchTime, armCount);

        AttackParamDto param = new AttackParamDto();
        param.setInvokePlayer(player);
        param.setCrossWorldMap(cMap);
        param.setHeroIdList(heroIdList);
        param.setMarchTime(marchTime);
        param.setArmCount(armCount);
        param.setNeedFood(needFood);

        baseMapBattle.joinBattle(param);
        // 参数
        JoinBattleCrossRs.Builder builder = JoinBattleCrossRs.newBuilder();
        CommonPb.Army army = param.getArmy();
        if (army != null) {
            builder.setArmy(army);
        }
        CommonPb.Battle battlePb = param.getBattle();
        if (battle != null) {
            builder.setBattle(battlePb);
        }
        return builder.build();
    }

    /*------------------------------------迁城------------------------------------------*/

    /**
     * 迁城
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public CrossMoveCityRs crossMoveCity(long roleId, CrossMoveCityRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int mapId = req.getMapId();
        CrossWorldMap cMap = checkCrossWorldMap(player, mapId);
        int moveType = req.getType();
        int pos = req.getPos();

        CrossMoveCityRs.Builder builder = CrossMoveCityRs.newBuilder();
        // 消耗的道具
        int costPropId = 0;
        if (moveType == CrossWorldMapConstant.MOVE_CITY_TYPE_ENTER) {
            // costPropId = Constant.ENTER_CROSS_MAP_PROP.get(0);
            processMoveCityEnter(player, cMap, costPropId, builder);
        } else if (moveType == CrossWorldMapConstant.MOVE_CITY_TYPE_LEAVE) {
            // costPropId = Constant.LEAVE_CROSS_MAP_PROP.get(0);
            processMoveCityLeave(player, cMap, costPropId, builder);
        } else if (moveType == CrossWorldMapConstant.MOVE_CITY_TYPE_RANDOM) {
            costPropId = PropConstant.ITEM_ID_5002;
            processMoveCityRandom(player, cMap, costPropId, builder);
        } else if (moveType == CrossWorldMapConstant.MOVE_CITY_TYPE_POS) {
            costPropId = PropConstant.ITEM_ID_5003;
            processMoveCityPos(player, cMap, costPropId, pos, builder);
        } else {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "迁城参数错误, roleId:", roleId, ", moveType:", moveType);
        }

        builder.setPos(player.lord.getPos());
        if (player.props.get(costPropId) != null) {
            builder.setProp(PbHelper.createPropPb(player.props.get(costPropId)));
        }
        builder.setArea(player.lord.getArea());
        return builder.build();
    }

    /**
     * 新地图定点迁城
     *
     * @param player
     * @param cMap
     * @param costPropId
     * @param pos
     * @param builder
     * @throws MwException
     */
    private void processMoveCityPos(Player player, CrossWorldMap cMap, int costPropId, int pos,
                                    CrossMoveCityRs.Builder builder) throws MwException {
        long roleId = player.lord.getLordId();
        if (!cMap.isEmptyPos(pos)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), " 此处不是一个空点 roleId:", roleId, "坐标:",
                    cMap.posToStr(pos));
        }
        // 地方阵营的安全区
        Set<Integer> otherSafeArea = Arrays.stream(Constant.Camp.camps)
                .filter(camp -> camp != player.lord.getCamp())
                .boxed()
                .flatMap(camp -> cMap.getAllMap()
                        .values()
                        .stream()
                        .filter(entity -> entity.getType() == WorldEntityType.WAR_FIRE_SAFE_AREA)
                        .map(entity -> (WFSafeAreaMapEntity) entity)
                        .filter(safe -> safe.getCamp() == camp))
                .map(WFSafeAreaMapEntity::getCellId)
                .collect(Collectors.toSet());
        if (otherSafeArea.contains(cMap.posToCell(pos))) {
            throw new MwException(GameError.WAR_FIRE_MOVE_OTHER_SAFE_AREA_ERROR.getCode(), "战火燎原不能迁城到敌方阵营的安全区, roleId: ", player.roleId, ", pos: ", pos);
        }
        checkNewMapMoveCityPrecondition(player, cMap, costPropId, 1);
        commonMoveCity(player, cMap, pos, CancelBattleType.DEFMOVECITY);
        if (!cMap.isInSafeArea(player)) {
            // 保护取消
            worldService.removeProTect(player, AwardFrom.WAR_FIRE_NOT_SAFE_AREA, pos);
        }
    }

    /**
     * 新地图随机迁城
     *
     * @param player
     * @param cMap
     * @param builder
     * @throws MwException
     */
    private void processMoveCityRandom(Player player, CrossWorldMap cMap, int costPropId,
                                       CrossMoveCityRs.Builder builder) throws MwException {
        checkNewMapMoveCityPrecondition(player, cMap, costPropId, 1);
        // 新地图上设置坐标
        int newPos = cMap.getRandomOpenEmptyPos(player.lord.getCamp());
        commonMoveCity(player, cMap, newPos, CancelBattleType.DEFMOVECITY);
        if (!cMap.isInSafeArea(player)) {
            // 保护取消
            worldService.removeProTect(player, AwardFrom.WAR_FIRE_NOT_SAFE_AREA, newPos);
        }
    }

    private void checkNewMapMoveCityPrecondition(Player player, CrossWorldMap cMap, int costPropId, int costPropCnt)
            throws MwException {
        long roleId = player.lord.getLordId();
        checkPlayerOnMap(roleId, cMap);
        if (player.lord.getArea() != cMap.getMapId()) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "你不在对应的地图,不能退出 roleId:", roleId);
        }
        PlayerArmy playerArmy = cMap.getMapMarchArmy().getPlayerArmyMap().get(roleId);
        if (playerArmy != null && !playerArmy.getArmy().isEmpty()) {
            throw new MwException(GameError.MOVE_HERO_OUT.getCode(), "迁城，有将领未返回, roleId:,", roleId);
        }
        // 战火燎原退出不扣道具
        rewardDataManager.checkAndSubPlayerRes(player, AwardType.PROP, costPropId, costPropCnt,
                AwardFrom.CROSS_MOVE_CITY, false);
    }

    /**
     * 迁城移除该点的驻防部队和战斗信息
     *
     * @param player
     * @param cMap
     * @param type
     */
    public static void returnArmyByMoveCity(Player player, CrossWorldMap cMap, CancelBattleType type) {
        int prePos = player.lord.getPos();
        MapWarData mapWarData = cMap.getMapWarData();
        // 取消该点的战斗,并返回部队
        mapWarData.cancelBattleByPosAndReturnArmy(prePos, type);
        // 驻防部队移除
        cMap.getMapMarchArmy().retreatGuardArmy(prePos, true);
    }

    /**
     * 公共的迁城逻辑
     *
     * @param player
     * @param cMap
     * @param newPos
     * @param type
     */
    public static void commonMoveCity(Player player, CrossWorldMap cMap, int newPos, CancelBattleType type) {
        int area = player.lord.getArea();
        if (area != cMap.getMapId() || !cMap.isEmptyPos(newPos)) {
            return;
        }
        int prePos = player.lord.getPos();

        returnArmyByMoveCity(player, cMap, type);

        cMap.removeWorldEntity(prePos); // 移除旧位置
        // 添加新位置
        PlayerMapEntity mapEntity = new PlayerMapEntity(newPos, player);
        cMap.addWorldEntity(mapEntity);
        player.lord.setPos(newPos);
        // 新地图通知
        cMap.publishMapEvent(MapEvent.mapEntity(prePos, MapCurdEvent.DELETE),
                MapEvent.mapArea(prePos, MapCurdEvent.DELETE), MapEvent.mapEntity(newPos, MapCurdEvent.CREATE),
                MapEvent.mapArea(newPos, MapCurdEvent.CREATE));
    }

    /**
     * 退出新地图
     *
     * @param player
     * @param cMap
     * @param costPropId
     * @param builder
     */
    private void processMoveCityLeave(Player player, CrossWorldMap cMap, int costPropId,
                                      CrossMoveCityRs.Builder builder) throws MwException {
        long roleId = player.lord.getLordId();
        int prePos = player.lord.getPos();

        checkNewMapMoveCityPrecondition(player, cMap, costPropId, Constant.LEAVE_CROSS_MAP_PROP.get(1));
        returnArmyByMoveCity(player, cMap, CancelBattleType.DEFMOVECITY);
        cMap.removeWorldEntity(prePos); // 新的地图上的点

        // // 移除观察者
        // cMap.removeWatcher(player.roleId);

        // 新地图的推送
        cMap.publishMapEvent(MapEvent.mapEntity(prePos, MapCurdEvent.DELETE));
        // 在德意志找个点迁过去
        int newPos = worldDataManager.randomKingAreaPos(player.lord.getCamp());
        if (cMap.getGlobalWarFire().getStage() == GlobalWarFire.STAGE_RUNNING) {
            // 在正式期间, 记录玩家离开的时间 + CD时间
            player.setMixtureData(PlayerConstant.LEAVE_WAR_FIRE_TIME, TimeHelper.getCurrentSecond() + (WorldConstant.WAR_FIRE_ENTER_CD));
            // 同步扩展数据
            playerDataManager.syncMixtureData(player);
        }
        // 玩家进入新地图埋点
        LogLordHelper.commonLog("warFireLeave", AwardFrom.WAR_FIRE_LEAVE, player, prePos, newPos);
        player.lord.setPos(newPos);
        player.lord.setArea(MapHelper.getAreaIdByPos(newPos));
        // 退出地图重新计算玩家所有将领属性
        CalculateUtil.reCalcAllHeroAttr(player);
        worldDataManager.putPlayer(player);
        List<Integer> posList = new ArrayList<>();
        posList.add(newPos);
        EventBus.getDefault().post(
                new Events.AreaChangeNoticeEvent(posList, roleId, Events.AreaChangeNoticeEvent.MAP_AND_AREA_TYPE));
    }

    /**
     * 是否可以进入新地图
     *
     * @param cMap
     * @return
     */
    private boolean canEnterMap(CrossWorldMap cMap) {
        return cMap.getGlobalWarFire().canEnterMap();
    }

    /**
     * 进入新地图
     *
     * @param player     玩家
     * @param cMap       新地图
     * @param costPropId 消耗的道具
     * @param builder    响应协议
     * @throws MwException
     */
    private void processMoveCityEnter(Player player, CrossWorldMap cMap, int costPropId,
                                      CrossMoveCityRs.Builder builder) throws MwException {
        long roleId = player.lord.getLordId();
        int prePos = player.lord.getPos();
        if (!CheckNull.isEmpty(player.armys)) {
            throw new MwException(GameError.MOVE_HERO_OUT.getCode(), "迁城，有将领未返回, roleId:,", roleId);
        }
        if (player.getDecisiveInfo().isDecisive()) {
            throw new MwException(GameError.DECISIVE_BATTLE_ING.getCode(), "玩家正在决战中,不能迁城, roleId:", roleId);
        }
        if (player.lord.getArea() == cMap.getMapId()) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "你已经在新地图, roleId:", roleId);
        }
        if (!canEnterMap(cMap)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "还没有到活动时间不能进入, roleId:", roleId);
        }
        // 匪军叛乱 或 德意志反攻期间内不让进入世界争霸
        int now = TimeHelper.getCurrentSecond();
        GlobalRebellion globalRebellion = globalDataManager.getGameGlobal().getGlobalRebellion();
        if (globalRebellion.getCurRoundEndTime() > now && globalRebellion.getJoinRoleId().contains(roleId)
                && !player.getAndCreateRebellion().isDead()) {
            throw new MwException(GameError.DO_NOT_ENTRY_WARWORLD_MAP_REBELLION.getCode(),
                    "玩家正在参加匪军叛乱活动不能进入新地图, roleId:", roleId);
        }
        CounterAttack counterAttack = globalDataManager.getGameGlobal().getCounterAttack();
        if (counterAttack.getCurRoundEndTime() > now
                && counterAttack.getCampHitRoleId(player.lord.getCamp()).contains(roleId)) {
            throw new MwException(GameError.DO_NOT_ENTRY_WARWORLD_MAP_COUNTERATTACK.getCode(),
                    "玩家正在参加德意志反攻活动不能进入新地图, roleId:", roleId);
        }

        // 离开的时间
        int leaveTime = player.getMixtureDataById(PlayerConstant.LEAVE_WAR_FIRE_TIME);
        if (leaveTime > 0 && leaveTime > now) {
            throw new MwException(GameError.DO_NOT_ENTRY_WAR_FIRE_MAP_LEAVE_TIME.getCode(), "玩家退出不足15分钟, roleId:", roleId);
        }

        if (costPropId > 0) {
            rewardDataManager.checkAndSubPlayerRes(player, AwardType.PROP, costPropId, Constant.ENTER_CROSS_MAP_PROP.get(1), AwardFrom.CROSS_MOVE_CITY, false);
        }

        if (player.battleMap.containsKey(prePos)) {// 改到玩家到达后没发现目标
            warService.cancelCityBattle(prePos, -1, false, true);
        }
        // 新地图上进入安全区
        int newPos = cMap.getEmptyPosSafeArea(player.lord.getCamp());
        // 安全区没有空闲坐标
        if (newPos == 0) {
            // 玩家进入新地图埋点
            LogLordHelper.commonLog("warFireEnterFail", AwardFrom.WAR_FIRE_ENTER, player);
            // 没有空闲坐标
            throw new MwException(GameError.WAR_FIRE_ENTER_NOT_HAVE_EMPTY_POS.getCode(), "战火燎原安全区没有空闲坐标, roleId:", roleId);
        }
        // 友军的城墙驻防部队返回
        List<Army> guradArays = worldDataManager.getPlayerGuard(prePos);
        if (!CheckNull.isEmpty(guradArays)) {
            for (Army army : guradArays) {
                Player tPlayer = playerDataManager.getPlayer(army.getLordId());
                if (tPlayer == null || tPlayer.armys.get(army.getKeyId()) == null) {
                    continue;
                }
                worldService.retreatArmyByDistance(tPlayer, army, now);
                worldService.synRetreatArmy(tPlayer, army, now);
                worldDataManager.removePlayerGuard(army.getTarget(), army);
                int heroId = army.getHero().get(0).getPrincipleHeroId();
                mailDataManager.sendNormalMail(tPlayer, MailConstant.MOLD_GARRISON_RETREAT, now, player.lord.getNick(),
                        heroId, player.lord.getNick(), heroId);
            }
        }
        // 旧地图上移除玩家
        playerDataManager.getPlayerByArea(player.lord.getArea()).remove(roleId);
        worldDataManager.removePlayerPos(prePos, player);
        // 旧地图上推送
        List<Integer> posList = new ArrayList<>(1);
        posList.add(prePos);
        EventBus.getDefault()
                .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_AREA_TYPE));

        int newArea = cMap.getMapId();
        PlayerMapEntity mapEntity = new PlayerMapEntity(newPos, player);
        cMap.addWorldEntity(mapEntity);
        player.lord.setPos(newPos);
        player.lord.setArea(newArea);
        // 玩家进入新地图埋点
        LogLordHelper.commonLog("warFireEnter", AwardFrom.WAR_FIRE_ENTER, player, prePos, newPos);
        // 如果在活动开启期间内, 进入活动, 重新计算属性和战斗力
        GlobalWarFire globalWarFire = cMap.getGlobalWarFire();
        globalWarFire.getPlayerWarFire(roleId).setStatus(PlayerWarFire.REGISTRY_STATUS);
        if (globalWarFire.getStage() == GlobalWarFire.STAGE_RUNNING) {
            CalculateUtil.reCalcAllHeroAttr(player);
        }
        // 新地图通知
        cMap.publishMapEvent(MapEvent.mapEntity(newPos, MapCurdEvent.CREATE),
                MapEvent.mapArea(newPos, MapCurdEvent.CREATE));

    }

    /**
     * 获取城池的详情
     *
     * @param roleId 玩家id
     * @param cityId 城池id
     * @return 城池详情
     * @throws MwException 自定义异常
     */
    public GetCrossCityInfoRs getCrossCityInfo(Long roleId, int cityId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        CrossWorldMap cMap = checkCrossWorldMap(player, CrossWorldMapConstant.CROSS_MAP_ID);

        GetCrossCityInfoRs.Builder builder = GetCrossCityInfoRs.newBuilder();
        cMap.getCityMap().values()
                .stream()
                .filter(ce -> ce.getCity().getCityId() == cityId)
                .map(mapCity -> mapCity.toMapCityPb(player, cMap))
                .filter(Objects::nonNull)
                .findFirst()
                .ifPresent(builder::setCity);
        return builder.build();
    }
}
