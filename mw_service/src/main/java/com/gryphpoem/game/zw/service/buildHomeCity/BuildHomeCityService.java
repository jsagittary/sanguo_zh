package com.gryphpoem.game.zw.service.buildHomeCity;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticBuildCityDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticBuildingDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticIniDataMgr;
import com.gryphpoem.game.zw.manager.BuildingDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.BuildingType;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.dao.impl.p.BuildingDao;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Building;
import com.gryphpoem.game.zw.resource.domain.p.BuildingExt;
import com.gryphpoem.game.zw.resource.domain.p.Mill;
import com.gryphpoem.game.zw.resource.domain.s.StaticBuildingInit;
import com.gryphpoem.game.zw.resource.domain.s.StaticBuildingLv;
import com.gryphpoem.game.zw.resource.domain.s.StaticHomeCityCell;
import com.gryphpoem.game.zw.resource.domain.s.StaticHomeCityFoundation;
import com.gryphpoem.game.zw.resource.domain.s.StaticIniLord;
import com.gryphpoem.game.zw.resource.pojo.buildHomeCity.BuildingState;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.GmCmd;
import com.gryphpoem.game.zw.service.GmCmdService;
import com.gryphpoem.game.zw.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 主城建设(包括：探索迷雾、开垦地基、摆放建筑)
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/5 10:20
 */
@Service
public class BuildHomeCityService implements GmCmdService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private BuildingDataManager buildingDataManager;
    @Autowired
    private BuildingDao buildingDao;

    /**
     * 探索迷雾
     *
     * @param roleId
     * @param rq
     * @return
     */
    public GamePb1.ExploreRs exploreFog(long roleId, GamePb1.ExploreRq rq) {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int cellId = rq.getCellId();
        // int scoutIndex = rq.getScoutIndex();
        // 获取目标迷雾格子配置
        StaticHomeCityCell staticHomeCityCell = StaticBuildCityDataMgr.getStaticHomeCityCellById(cellId);
        if (staticHomeCityCell == null) {
            throw new MwException(GameError.NO_CONFIG, String.format("探索主城迷雾区时, 未找到主城地图格配置, roleId:%s, cellId:%s", roleId, cellId));
        }
        // 校验探索条件
        Integer needLordLevel = staticHomeCityCell.getLevel();
        if (needLordLevel != null && player.lord.getLevel() < needLordLevel) {
            throw new MwException(GameError.INSUFFICIENT_LORD_LEVEL, String.format("探索主城迷雾区时, 未达到领主等级要求, roleId:%s, cellId:%s", roleId, cellId));
        }
        /*Integer scoutState = player.getScoutData().get(scoutIndex);
        if (scoutState == 1) {
            throw new MwException(GameError.SCOUT_NOT_IDLE, String.format("探索主城迷雾区时, 侦察兵非空闲状态, roleId:%s, scoutIndex:%s", roleId, scoutIndex));
        }*/
        if (player.getMapCellData().containsKey(cellId)) {
            throw new MwException(GameError.MAP_CELL_ALREADY_EXPLORED, String.format("探索主城迷雾区时, 该迷雾区已被探索, roleId:%s, cellId:%s", roleId, cellId));
        }
        // 配置的周边4个格子是否已解锁
        List<Integer> neighborCellList = staticHomeCityCell.getNeighborCellList();
        boolean anyMatch = neighborCellList.stream().anyMatch(tmp -> player.getMapCellData().containsKey(tmp));
        if (!anyMatch) {
            throw new MwException(GameError.NO_ONE_NEIGHBOR_IS_UNLOCK, String.format("探索主城迷雾区时, 四周的格子没有一个是解锁的, roleId:%s, cellId:%s", roleId, cellId));
        }

        /*// 新增定时任务开始探索
        int now = TimeHelper.getCurrentSecond();
        int endTime = staticHomeCityCell.getExploreTime() + now;
        ExploreQue exploreQue = new ExploreQue(player.maxKey(), scoutIndex, cellId, staticHomeCityCell.getExploreTime(), endTime);
        player.getExploreQue().put(scoutIndex, exploreQue); // 更新玩家探索队列
        player.getScoutData().put(scoutIndex, 1); // 更新侦察兵状态
        // 添加探索结束后的延时任务
        DELAY_QUEUE.add(new BuildHomeCityDelayRun(1, endTime, cellId, scoutIndex, 0, player));*/

        Map<Integer, List<Integer>> mapCellData = player.getMapCellData();
        Map<Integer, List<Integer>> newAddMapCellData = new HashMap<>();
        List<Integer> cellState = new ArrayList<>(2);
        // 新增解锁的地图格子
        cellState.add(0); // 未开垦
        cellState.add(staticHomeCityCell.getHasBandit()); // 是否有土匪
        newAddMapCellData.put(cellId, cellState);
        // 关联解锁的地图格子
        List<Integer> bindCellList = staticHomeCityCell.getBindCellList();
        for (Integer bindCellId : bindCellList) {
            StaticHomeCityCell bindCell = StaticBuildCityDataMgr.getStaticHomeCityCellById(cellId);
            List<Integer> bindCellState = new ArrayList<>(2);
            bindCellState.add(0); // 未开垦
            bindCellState.add(bindCell.getHasBandit()); // 是否有土匪
            if (!mapCellData.containsKey(bindCellId)) {
                newAddMapCellData.put(bindCellId, bindCellState);
            }
        }
        mapCellData.putAll(newAddMapCellData);

        GamePb1.ExploreRs.Builder builder = GamePb1.ExploreRs.newBuilder();
        newAddMapCellData.forEach((key, value) -> {
            CommonPb.MapCell.Builder mapCell = CommonPb.MapCell.newBuilder();
            mapCell.setCellId(key);
            mapCell.addAllState(value);
            builder.addMapCellData(mapCell.build());
        });
        return builder.build();
    }

    /**
     * 开垦地基<br>
     * 一个格子一个格子开垦, 地基所占的格子全部开垦完后, 则解锁地基
     *
     * @param roleId
     * @param rq
     * @return
     */
    public GamePb1.ReclaimFoundationRs reclaimFoundation(long roleId, GamePb1.ReclaimFoundationRq rq) {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int cellId = rq.getCellId();
        // 获取目标格子配置
        StaticHomeCityCell staticHomeCityCell = StaticBuildCityDataMgr.getStaticHomeCityCellById(cellId);
        if (staticHomeCityCell == null) {
            throw new MwException(GameError.NO_CONFIG, String.format("开垦地基时, 未找到主城地图格配置, roleId:%s, cellId:%s", roleId, cellId));
        }
        // 开垦格子前, 需要先探索
        Map<Integer, List<Integer>> mapCellData = player.getMapCellData();
        if (!mapCellData.containsKey(cellId)) {
            throw new MwException(GameError.INSUFFICIENT_LORD_LEVEL, String.format("开垦地基时, 该格子还未解锁, roleId:%s, cellId:%s", roleId, cellId));
        }
        if (mapCellData.get(cellId).get(0) == 1) {
            throw new MwException(GameError.INSUFFICIENT_LORD_LEVEL, String.format("开垦地基时, 该格子已被开垦, roleId:%s, cellId:%s", roleId, cellId));
        }
        /*// 校验是否有空闲农民
        if (player.getIdleFarmerCount() < farmerCount) {
            throw new MwException(GameError.INSUFFICIENT_LORD_LEVEL, String.format("开垦地基时, 没有足够空闲的农民, roleId:%s, cellId:%s", roleId, cellId));
        }*/

        /*// 新增定时任务开始开垦
        int now = TimeHelper.getCurrentSecond();
        int endTime = staticHomeCityCell.getExploreTime() + now;
        Map<Integer, ReclaimQue> reclaimQueMap = player.getReclaimQue();
        int reclaimIndex = reclaimQueMap.keySet().stream().max(Integer::compareTo).orElse(0);
        ReclaimQue reclaimQue = new ReclaimQue(player.maxKey(), reclaimIndex + 1, farmerCount, cellId, staticHomeCityCell.getReclaimTime(), endTime);
        reclaimQueMap.put(reclaimIndex + 1, reclaimQue); // 更新玩家开垦队列
        player.subIdleFarmerCount(farmerCount); // 更新空闲农民数量
        // 添加开垦结束后的延时任务
        DELAY_QUEUE.add(new BuildHomeCityDelayRun(2, endTime, cellId, 0, farmerCount, player));*/
        GamePb1.ReclaimFoundationRs.Builder builder = GamePb1.ReclaimFoundationRs.newBuilder();
        // 更新格子开垦状态
        List<Integer> cellState = new ArrayList<>(mapCellData.get(cellId));
        cellState.set(0, 1);
        mapCellData.put(cellId, cellState);
        CommonPb.MapCell.Builder mapCell = CommonPb.MapCell.newBuilder();
        mapCell.setCellId(cellId);
        mapCell.addAllState(cellState);
        builder.addMapCellData(mapCell.build());// 返回被开垦格子的信息
        // 获取玩家新增解锁的地基id, 解锁地基需要该地基所占的格子全部被开垦完成
        List<Integer> foundationIdList = StaticBuildCityDataMgr.getFoundationIdListByCellId(cellId); // 开垦的格子对应可解锁的地基
        List<Integer> unlockFoundationIdList = new ArrayList<>();
        // 获取玩家已开垦的格子
        List<Integer> reclaimedCellIdList = new ArrayList<>();
        mapCellData.forEach((key, value) -> {
            if (value.get(0) == 1) {
                reclaimedCellIdList.add(key);
            }
        });
        for (Integer foundationId : foundationIdList) {
            // 判断该地基所要求格子是否已全部开垦, 如果是, 则解锁该地基
            StaticHomeCityFoundation staticFoundation = StaticBuildCityDataMgr.getStaticHomeCityFoundationById(foundationId);
            if (reclaimedCellIdList.containsAll(staticFoundation.getCellList())) {
                unlockFoundationIdList.add(foundationId);
            }
        }
        // 玩家新增解锁的地基
        player.getFoundationData().addAll(unlockFoundationIdList);
        List<Integer> distinctFoundationList = player.getFoundationData().stream().distinct().collect(Collectors.toList());
        player.getFoundationData().clear();
        player.getFoundationData().addAll(distinctFoundationList);

        // 如果开垦的格子满足城墙解锁条件
        if (player.getFoundationData().containsAll(Constant.WALL_UNLOCK_CONDITION)) {
            BuildingState buildingState = new BuildingState();
            buildingState.setBuildingId(BuildingType.WALL);
            buildingState.setBuildingLv(1);
            buildingState.setBuildingType(BuildingType.WALL);
            StaticBuildingLv sBuildingLevel = StaticBuildingDataMgr.getStaticBuildingLevel(BuildingType.WALL, 1);
            if (sBuildingLevel == null) {
                throw new MwException(GameError.NO_CONFIG, String.format("解锁城墙时, 未获取到城墙的等级配置, roleId:%s, buildingType:%s, buildingLv:%s", player.roleId, BuildingType.WALL, 1));
            }
            buildingState.setResidentCnt(0);
            buildingState.setResidentTopLimit(sBuildingLevel.getResident());
            player.getBuildingData().put(BuildingType.WALL, buildingState);
            player.building.setWall(1);
            BuildingExt buildingExt = player.buildingExts.get(BuildingType.WALL);
            int now = TimeHelper.getCurrentSecond();
            if (buildingExt == null) {
                buildingExt = new BuildingExt(BuildingType.WALL, BuildingType.WALL, true);
                buildingExt.setUnLockTime(now);
                player.buildingExts.put(BuildingType.WALL, buildingExt);
            } else {
                buildingExt.setUnLockTime(now);
                buildingExt.setUnlock(true);
            }
        }

        // 返回解锁的地基信息
        builder.addAllFoundationData(unlockFoundationIdList);
        return builder.build();
    }

    /**
     * 安民济物
     *
     * @param roleId
     * @param rq
     * @return
     */
    public GamePb1.PeaceAndWelfareRs peaceAndWelfare(long roleId, GamePb1.PeaceAndWelfareRq rq) {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Map<Integer, Integer> peaceAndWelfareRecord = player.getPeaceAndWelfareRecord();
        int type = rq.getType();
        Integer latestPlayTime = peaceAndWelfareRecord.get(type);
        if (latestPlayTime > 0 && DateHelper.isToday(TimeHelper.getDate(latestPlayTime))) {
            throw new MwException(GameError.PARAM_ERROR, String.format("玩家今日已进行过安民济物, roleId:%s, type:%s, latestTime:%s", roleId, type, TimeHelper.getDate(latestPlayTime)));
        }
        List<List<Integer>> typeConfig = null;
        switch (type) {
            case 1:
                typeConfig = Constant.PEACE_WELFARE_TYPE1_CONFIG;
                break;
            case 2:
                typeConfig = Constant.PEACE_WELFARE_TYPE2_CONFIG;
                break;
        }
        if (CheckNull.isEmpty(typeConfig) || typeConfig.size() < 3) {
            throw new MwException(GameError.NO_CONFIG, String.format("玩家进行安民济物时, 配置错误, roleId:%S, type:%s", roleId, type));
        }
        List<Integer> consume = typeConfig.get(0); // 消耗
        List<Integer> happinessAdd = typeConfig.get(1); // 增加的幸福度
        List<Integer> residentAdd = typeConfig.get(2); // 增加的人口
        if (consume.size() < 3 || happinessAdd.size() < 1 || residentAdd.size() < 1) {
            throw new MwException(GameError.NO_CONFIG, String.format("玩家进行安民济物时, 配置错误, roleId:%S, type:%s", roleId, type));
        }

        rewardDataManager.checkAndSubPlayerResHasSync(player, consume.get(0), consume.get(1), consume.get(2), AwardFrom.COMMON, "");
        player.setHappiness(player.getHappiness() + happinessAdd.get(0));
        // TODO 重新计算幸福度的范围, 同步更新人口恢复速度与资源产出速度
        int residentTopLimit = player.getResidentTopLimit();
        int residentTotalCnt = player.getResidentTotalCnt();
        int finalAddResident = residentTotalCnt + residentAdd.get(0) > residentTopLimit ? residentTopLimit - residentTotalCnt : residentAdd.get(0);
        player.addResidentTotalCnt(finalAddResident);
        player.addIdleResidentCnt(finalAddResident);

        // 更新记录时间
        peaceAndWelfareRecord.put(type, TimeHelper.getCurrentSecond());

        // 同步玩家信息
        playerDataManager.syncRoleInfo(player);

        GamePb1.PeaceAndWelfareRs.Builder builder = GamePb1.PeaceAndWelfareRs.newBuilder();
        return builder.build();
    }

    /**
     * 幸福度自然更新的定时器逻辑<br>
     * 小于会恢复度上限开始增长; 大于恢复度上限开始损耗
     */
    public void HappinessTimerLogic() {
        Iterator<Player> iterator = playerDataManager.getPlayers().values().iterator();
        int now = TimeHelper.getCurrentSecond();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            try {
                int happinessRecoveryTopLimit = Constant.HAPPINESS_RECOVERY_TOP_LIMIT;
                int happiness = player.getHappiness();
                if (happiness < happinessRecoveryTopLimit) {
                    // 开始增长
                } else if (happiness == happinessRecoveryTopLimit) {
                    // 不变
                } else {
                    // 开始减少
                }
                // 根据更新后的幸福度, 同步更新人口恢复速度、资源生产速度
            } catch (Exception e) {
                LogUtil.error("幸福度恢复的逻辑定时器报错, lordId:" + player.lord.getLordId(), e);
            }
        }
    }

    /**
     * 人口恢复的定时器逻辑<br>
     * 人口总数小于人口上限就开始恢复
     */
    public void ResidentTimerLogic() {
        Iterator<Player> iterator = playerDataManager.getPlayers().values().iterator();
        int now = TimeHelper.getCurrentSecond();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            try {

            } catch (Exception e) {
                LogUtil.error("幸福度恢复的逻辑定时器报错, lordId:" + player.lord.getLordId(), e);
            }
        }
    }

    // 清剿土匪
    public void clearBandit(long roleId) {

    }

    // 叛军入侵
    public void rebelInvade(long roleId) {

    }

    /**
     * 迷雾探索结束时要做的事
     *
     * @param cellId
     * @param player
     * @param scoutIndex
     */
    public void doAtExploreEnd(Integer cellId, int scoutIndex, Player player) {
        /*Map<Integer, Integer> mapCellData = player.getMapCellData();
        // 新增解锁的地图格子
        mapCellData.put(cellId, 0);
        // 关联解锁的地图格子
        StaticHomeCityCell staticHomeCityCell = StaticDataMgr.getStaticHomeCityCellById(cellId);
        List<Integer> bindCellList = staticHomeCityCell.getBindCellList();
        for (Integer bindCellId : bindCellList) {
            if (!mapCellData.containsKey(bindCellId)) {
                mapCellData.put(bindCellId, 0);
            }
        }
        // 恢复探索的侦察兵状态为空闲, 并向客户端同步
        player.getScoutData().put(scoutIndex, 0);
        playerDataManager.syncRoleInfo(player);
        // 移除探索队列
        player.getExploreQue().remove(scoutIndex);
        // 向客户端同步探索完成的格子
        GamePb1.SynExploreOrReclaimRs.Builder builder = GamePb1.SynExploreOrReclaimRs.newBuilder();
        builder.setType(1);
        builder.setCellId(cellId);
        builder.setFinishedExploreQueIndex(scoutIndex);
        BasePb.Base.Builder msg = PbHelper.createSynBase(GamePb1.SynExploreOrReclaimRs.EXT_FIELD_NUMBER, GamePb1.SynExploreOrReclaimRs.ext, builder.build());
        MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));*/
    }

    /**
     * 地基开垦结束时要做的事
     *
     * @param cellId
     * @param farmerCnt
     * @param player
     */
    public void doAtReclaimEnd(Integer cellId, int farmerCnt, Player player, Integer reclaimIndex) {
        /*// 获取玩家新增解锁的地基id, 解锁地基需要该地基所占的格子全部被开垦完成
        List<Integer> foundationIdList = StaticDataMgr.getFoundationIdListByCellId(cellId); // 开垦的格子对应可解锁的地基
        List<Integer> unlockFoundationIdList = new ArrayList<>();
        // 获取玩家已开垦的格子
        List<Integer> reclaimedCellIdList = (List<Integer>) player.getMapCellData().entrySet().stream().filter(entry -> entry.getValue() == 1).map(Map.Entry::getKey);
        for (Integer foundationId : foundationIdList) {
            // 判断该地基所要求格子是否已全部开垦, 如果是, 则解锁该地基
            StaticHomeCityFoundation staticFoundation = StaticDataMgr.getStaticHomeCityFoundationById(foundationId);
            if (reclaimedCellIdList.containsAll(staticFoundation.getCellList())) {
                unlockFoundationIdList.add(foundationId);
            }
        }
        // 玩家新增解锁的地基
        player.getFoundationData().addAll(foundationIdList);
        // 释放开垦的农民, 并向客户端同步
        player.addIdleFarmerCount(farmerCnt);
        playerDataManager.syncRoleInfo(player);
        // 移除开垦队列
        player.getReclaimQue().remove(reclaimIndex);
        // 向客户端同步开垦出的地基
        GamePb1.SynExploreOrReclaimRs.Builder builder = GamePb1.SynExploreOrReclaimRs.newBuilder();
        builder.setType(2);
        builder.setCellId(cellId);
        builder.addAllFoundationId(unlockFoundationIdList);
        builder.setFinishedReclaimQueIndex(reclaimIndex);
        BasePb.Base.Builder msg = PbHelper.createSynBase(GamePb1.SynExploreOrReclaimRs.EXT_FIELD_NUMBER, GamePb1.SynExploreOrReclaimRs.ext, builder.build());
        MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));*/
    }

    @GmCmd("buildCity")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {
        Map<Integer, List<Integer>> mapCellData = player.getMapCellData();
        List<Integer> foundationData = player.getFoundationData();
        Map<Integer, BuildingState> buildingData = player.getBuildingData();
        switch (params[0]) {
            case "clearMapCell":
                // 重置所有已探索的格子、已开垦的地基、已解锁的建筑
                mapCellData.clear();
                foundationData.clear();
                buildingData.clear();
                player.mills.clear();
                player.buildingExts.clear();
                StaticIniLord staticIniLord = StaticIniDataMgr.getLordIniData();
                playerDataManager.initBuildingInfo(player, staticIniLord);
                Building building = new Building();
                building.setLordId(player.roleId);
                Map<Integer, StaticBuildingInit> initBuildingMap = StaticBuildingDataMgr.getBuildingInitMap();
                for (StaticBuildingInit buildingInit : initBuildingMap.values()) {
                    if (buildingData.get(buildingInit.getBuildingId()) == null) {
                        BuildingState buildingState = new BuildingState(buildingInit.getBuildingId(), buildingInit.getBuildingType());
                        buildingState.setBuildingLv(buildingInit.getInitLv());
                        buildingData.put(buildingInit.getBuildingId(), buildingState);
                    }
                    if (BuildingDataManager.isResType(buildingInit.getBuildingType())) {
                        player.mills.put(buildingInit.getBuildingId(), new Mill(buildingInit.getBuildingId(),
                                buildingInit.getBuildingType(), buildingInit.getInitLv(), 0));
                    } else {
                        switch (buildingInit.getBuildingType()) {
                            case BuildingType.COMMAND:
                                building.setCommand(buildingInit.getInitLv());
                                break;
                            case BuildingType.WALL:
                                building.setWall(buildingInit.getInitLv());
                                break;
                            case BuildingType.TECH:
                                building.setTech(buildingInit.getInitLv());
                                break;
                            case BuildingType.STOREHOUSE:
                                building.setStoreHouse(buildingInit.getInitLv());
                                break;
                            case BuildingType.MALL:
                                building.setMall(buildingInit.getInitLv());
                                break;
                            case BuildingType.REMAKE_WEAPON_HOUSE:
                                building.setRemakeWeaponHouse(buildingInit.getInitLv());
                                break;
                            case BuildingType.FACTORY_1:
                                building.setFactory1(buildingInit.getInitLv());
                                break;
                            case BuildingType.FACTORY_2:
                                building.setFactory2(buildingInit.getInitLv());
                                break;
                            case BuildingType.FACTORY_3:
                                building.setFactory3(buildingInit.getInitLv());
                                break;
                            case BuildingType.FERRY:
                                building.setFerry(buildingInit.getInitLv());
                                break;
                            case BuildingType.MAKE_WEAPON_HOUSE:
                                building.setMakeWeaponHouse(buildingInit.getInitLv());
                                break;
                            case BuildingType.WAR_COLLEGE:
                                building.setWarCollege(buildingInit.getInitLv());
                                break;
                            case BuildingType.TRADE_CENTRE:
                                building.setTradeCentre(buildingInit.getInitLv());
                                break;
                            case BuildingType.WAR_FACTORY:
                                building.setWarFactory(buildingInit.getInitLv());
                                break;
                            case BuildingType.TRAIN_FACTORY_1:
                                building.setTrainFactory1(buildingInit.getInitLv());
                                break;
                            case BuildingType.TRAIN_FACTORY_2:
                                building.setTrain2(buildingInit.getInitLv());
                                break;
                            case BuildingType.AIR_BASE:
                                building.setAirBase(buildingInit.getInitLv());
                                break;
                            case BuildingType.SEASON_TREASURY:
                                building.setSeasonTreasury(buildingInit.getInitLv());
                                break;
                            case BuildingType.CIA:
                                building.setCia(buildingInit.getInitLv());
                                break;
                            case BuildingType.SMALL_GAME_HOUSE:
                                building.setSmallGameHouse(buildingInit.getInitLv());
                                break;
                            case BuildingType.DRAW_HERO_HOUSE:
                                building.setDrawHeroHouse(buildingInit.getInitLv());
                                break;
                            case BuildingType.SUPER_EQUIP_HOUSE:
                                building.setSuperEquipHouse(buildingInit.getInitLv());
                                break;
                            case BuildingType.STATUTE:
                                building.setSuperEquipHouse(buildingInit.getInitLv());
                                break;
                            case BuildingType.MEDAL_HOUSE:
                                building.setSuperEquipHouse(buildingInit.getInitLv());
                                break;
                            default:
                                break;
                        }
                    }
                }
                buildingDao.updateBuilding(building);
                player.building = building;
                // 更新解锁解锁状态
                // buildingDataManager.updateBuildingLockState(player);
                break;
            case "fixFoundationData":
                // 去除重复地基
                List<Integer> newFoundationData = foundationData.stream().distinct().collect(Collectors.toList());
                foundationData.clear();
                foundationData.addAll(newFoundationData);
                break;
            case "openTheWholeMap":
                // 一键解锁全部地图格和地基
                List<StaticHomeCityCell> staticHomeCityCellList = StaticBuildCityDataMgr.getStaticHomeCityCellList();
                for (StaticHomeCityCell sHomeCityCell : staticHomeCityCellList) {
                    if (!mapCellData.containsKey(sHomeCityCell.getId())) {
                        List<Integer> cellState = new ArrayList<>(2);
                        cellState.add(1);
                        cellState.add(sHomeCityCell.getHasBandit());
                        mapCellData.put(sHomeCityCell.getId(), cellState);
                    }
                }
                List<StaticHomeCityFoundation> staticHomeCityFoundationList = StaticBuildCityDataMgr.getStaticHomeCityFoundationList();
                for (StaticHomeCityFoundation sHomeCityFoundation : staticHomeCityFoundationList) {
                    if (!foundationData.contains(sHomeCityFoundation.getId())) {
                        foundationData.add(sHomeCityFoundation.getId());
                    }
                }
                break;
            case "clearBuildQue":
                player.buildQue.clear();
                break;
            default:
        }
        playerDataManager.syncRoleInfo(player);
    }
}
