package com.gryphpoem.game.zw.service.buildHomeCity;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticBuildCityDataMgr;
import com.gryphpoem.game.zw.manager.BuildingDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticEconomicCrop;
import com.gryphpoem.game.zw.resource.domain.s.StaticHomeCityCell;
import com.gryphpoem.game.zw.resource.domain.s.StaticHomeCityFoundation;
import com.gryphpoem.game.zw.resource.pojo.BuildingState;
import com.gryphpoem.game.zw.service.GmCmd;
import com.gryphpoem.game.zw.service.GmCmdService;
import com.gryphpoem.game.zw.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
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
public class BuildHomeCityService /*implements DelayInvokeEnvironment*/ implements GmCmdService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private BuildingDataManager buildingDataManager;

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
        cellState.add(staticHomeCityCell.getHasBandit() == null ? 0 : staticHomeCityCell.getHasBandit()); // 是否有土匪
        newAddMapCellData.put(cellId, cellState);
        // 关联解锁的地图格子
        List<Integer> bindCellList = staticHomeCityCell.getBindCellList();
        for (Integer bindCellId : bindCellList) {
            StaticHomeCityCell bindCell = StaticBuildCityDataMgr.getStaticHomeCityCellById(cellId);
            List<Integer> bindCellState = new ArrayList<>(2);
            bindCellState.add(0); // 未开垦
            bindCellState.add(bindCell.getHasBandit() == null ? 0 : staticHomeCityCell.getHasBandit()); // 是否有土匪
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
        // int farmerCount = rq.getFarmerCount();
        // 获取目标格子配置
        StaticHomeCityCell staticHomeCityCell = StaticBuildCityDataMgr.getStaticHomeCityCellById(cellId);
        if (staticHomeCityCell == null) {
            throw new MwException(GameError.NO_CONFIG, String.format("开垦地基时, 未找到主城地图格配置, roleId:%s, cellId:%s", roleId, cellId));
        }
        // 开垦格子前, 需要先探索
        if (!player.getMapCellData().containsKey(cellId)) {
            throw new MwException(GameError.INSUFFICIENT_LORD_LEVEL, String.format("开垦地基时, 该格子还未解锁, roleId:%s, cellId:%s", roleId, cellId));
        }
        if (player.getMapCellData().get(cellId).get(0) == 1) {
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
        Map<Integer, List<Integer>> mapCellData = player.getMapCellData();
        List<Integer> cellState = new ArrayList<>(mapCellData.get(cellId));
        cellState.set(0, 1);
        mapCellData.put(cellId, cellState);
        // 返回被开垦格子的信息
        CommonPb.MapCell.Builder mapCell = CommonPb.MapCell.newBuilder();
        mapCell.setCellId(cellId);
        mapCell.addAllState(cellState);
        builder.addMapCellData(mapCell.build());
        // 获取玩家新增解锁的地基id, 解锁地基需要该地基所占的格子全部被开垦完成
        List<Integer> foundationIdList = StaticBuildCityDataMgr.getFoundationIdListByCellId(cellId); // 开垦的格子对应可解锁的地基
        List<Integer> unlockFoundationIdList = new ArrayList<>();
        // 获取玩家已开垦的格子
        List<Integer> reclaimedCellIdList = new ArrayList<>();
        player.getMapCellData().forEach((key, value) -> {
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
        // 返回解锁的地基信息
        builder.addAllFoundationData(unlockFoundationIdList);
        return builder.build();
    }

    /**
     * 建筑摆放(交换建筑位置)
     *
     * @param roleId
     * @param rq
     * @return
     */
    public GamePb1.SwapBuildingLocationRs swapBuildingLocation(long roleId, GamePb1.SwapBuildingLocationRq rq) {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int sourceBuildingId = rq.getBuildingId();
        int targetFoundationId = rq.getTargetFoundationId();
        // 校验建筑是否已建造
        buildingDataManager.checkBuildingIsCreate(sourceBuildingId, player);
        // 校验目标地基是否已解锁
        List<Integer> foundationData = player.getFoundationData();
        if (!foundationData.contains(targetFoundationId)) {
            throw new MwException(GameError.FOUNDATION_NOT_UNLOCK, String.format("交换建筑位置时, 目标地基未解锁, roleId:%s, targetFoundationId:%s", roleId, targetFoundationId));
        }
        // 校验原建筑地基信息
        Map<Integer, BuildingState> buildingData = player.getBuildingData();
        BuildingState sourceBuildingState = buildingData.get(sourceBuildingId);
        Integer sourceFoundationId = sourceBuildingState.getFoundationId();
        if (sourceBuildingState.getFoundationId() == null) {
            throw new MwException(GameError.FOUNDATION_DATA_OF_BUILDING_IS_ERROR, String.format("交换建筑位置时, 未获取到原建筑的地基信息, roleId:%s, sourceBuildingId:%s", roleId, sourceBuildingId));
        }
        // 目标地基是否已有建筑
        Integer targetBuildingId = buildingData.values().stream()
                .filter(tmp ->tmp.getFoundationId().equals(targetFoundationId))
                .map(BuildingState::getBuildingId)
                .findFirst()
                .orElse(null);
        // 交换位置
        GamePb1.SwapBuildingLocationRs.Builder builder = GamePb1.SwapBuildingLocationRs.newBuilder();
        sourceBuildingState.setFoundationId(targetFoundationId);
        buildingData.put(sourceBuildingId, sourceBuildingState);
        builder.addBuildingState(sourceBuildingState.creatPb());
        if (targetBuildingId != null) {
            BuildingState targetBuildingState = buildingData.get(targetBuildingId);
            targetBuildingState.setFoundationId(sourceFoundationId);
            buildingData.put(targetBuildingId, targetBuildingState);
            builder.addBuildingState(targetBuildingState.creatPb());
        }
        return builder.build();
    }

    /**
     * 给资源建筑分配经济副作物
     *
     * @param roleId
     * @param rq
     * @return
     */
    public GamePb1.AssignEconomicCropRs assignEconomicCropToResBuilding(long roleId, GamePb1.AssignEconomicCropRq rq) {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int buildingId = rq.getBuildingId();
        List<Integer> economicCropIdList = rq.getEconomicCropIdList();
        // 获取玩家对应建筑等级
        int buildingLv = player.mills.get(buildingId).getLv();
        for (Integer economicCropId : economicCropIdList) {
            // 获取经济作物配置
            StaticEconomicCrop staticEconomicCrop = StaticBuildCityDataMgr.getStaticEconomicCropByPropId(economicCropId);
            if (staticEconomicCrop == null
                    || staticEconomicCrop.getNeedBuildingLv() == null
                    || staticEconomicCrop.getProductTime() == null
                    || staticEconomicCrop.getProductCnt() == null) {
                throw new MwException(GameError.NO_CONFIG, "分配经济作物时, 未找到对应的经济作物配置, roleId:%s, economicCropId:%s", roleId, economicCropId);
            }
            Map<Integer, Integer> needBuildingLv = staticEconomicCrop.getNeedBuildingLv();
            if (!needBuildingLv.containsKey(buildingId) || buildingLv < needBuildingLv.get(buildingId)) {
                throw new MwException(GameError.PARAM_ERROR, String.format("分配经济作物时, 玩家建筑等级未达到要求, roleId:%s, buildingId:%s, economicCropId:%s", roleId, buildingId, economicCropId));
            }

            Integer productCnt = staticEconomicCrop.getProductCnt();
            Integer productTime = staticEconomicCrop.getProductTime();
            // 创建经济作物延时任务、离线任务
        }



        GamePb1.AssignEconomicCropRs.Builder builder = GamePb1.AssignEconomicCropRs.newBuilder();
        return builder.build();
    }

    // 给建筑派遣居民
    public void dispatchResidentToBuilding(long roleId) {

    }

    // 一键派遣
    public void autoDispatchResident(long roleId) {

    }

    // 委任武将
    public void dispatchHeroToBuilding(long roleId) {

    }

    // 一键委任
    public void autoDispatchHero(long roleId) {

    }

    // 提交经济订单
    public void submitEconomicOrder(long roleId) {

    }

    // 刷新玩家经济订单栏
    public void refreshEconomicOrder(long roleId) {

    }

    // 安民济物
    public void peaceAndWelfare(long roleId) {
        // 饮至策勋

        // 千秋庆典
    }

    // 清剿土匪
    public void clearBandit(long roleId) {

    }

    // 叛军入侵
    public void rebelInvade(long roleId) {

    }

    /*private DelayQueue<BuildHomeCityDelayRun> DELAY_QUEUE = new DelayQueue<>(this);

    @Override
    public DelayQueue getDelayQueue() {
        return DELAY_QUEUE;
    }*/

    /**
     * 迷雾探索结束时要做的事
     *
     * @param cellId
     * @param player
     * @param scoutIndex
     */
    public void doAtExploreEnd(Integer cellId, int scoutIndex, Player player) {
        // Map<Integer, Integer> mapCellData = player.getMapCellData();
        // // 新增解锁的地图格子
        // mapCellData.put(cellId, 0);
        // // 关联解锁的地图格子
        // StaticHomeCityCell staticHomeCityCell = StaticDataMgr.getStaticHomeCityCellById(cellId);
        // List<Integer> bindCellList = staticHomeCityCell.getBindCellList();
        // for (Integer bindCellId : bindCellList) {
        //     if (!mapCellData.containsKey(bindCellId)) {
        //         mapCellData.put(bindCellId, 0);
        //     }
        // }
        // // 恢复探索的侦察兵状态为空闲, 并向客户端同步
        // player.getScoutData().put(scoutIndex, 0);
        // playerDataManager.syncRoleInfo(player);
        // // 移除探索队列
        // player.getExploreQue().remove(scoutIndex);
        // // 向客户端同步探索完成的格子
        // GamePb1.SynExploreOrReclaimRs.Builder builder = GamePb1.SynExploreOrReclaimRs.newBuilder();
        // builder.setType(1);
        // builder.setCellId(cellId);
        // builder.setFinishedExploreQueIndex(scoutIndex);
        // BasePb.Base.Builder msg = PbHelper.createSynBase(GamePb1.SynExploreOrReclaimRs.EXT_FIELD_NUMBER, GamePb1.SynExploreOrReclaimRs.ext, builder.build());
        // MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
    }

    /**
     * 地基开垦结束时要做的事
     *
     * @param cellId
     * @param farmerCnt
     * @param player
     */
    public void doAtReclaimEnd(Integer cellId, int farmerCnt, Player player, Integer reclaimIndex) {
        // // 获取玩家新增解锁的地基id, 解锁地基需要该地基所占的格子全部被开垦完成
        // List<Integer> foundationIdList = StaticDataMgr.getFoundationIdListByCellId(cellId); // 开垦的格子对应可解锁的地基
        // List<Integer> unlockFoundationIdList = new ArrayList<>();
        // // 获取玩家已开垦的格子
        // List<Integer> reclaimedCellIdList = (List<Integer>) player.getMapCellData().entrySet().stream().filter(entry -> entry.getValue() == 1).map(Map.Entry::getKey);
        // for (Integer foundationId : foundationIdList) {
        //     // 判断该地基所要求格子是否已全部开垦, 如果是, 则解锁该地基
        //     StaticHomeCityFoundation staticFoundation = StaticDataMgr.getStaticHomeCityFoundationById(foundationId);
        //     if (reclaimedCellIdList.containsAll(staticFoundation.getCellList())) {
        //         unlockFoundationIdList.add(foundationId);
        //     }
        // }
        // // 玩家新增解锁的地基
        // player.getFoundationData().addAll(foundationIdList);
        // // 释放开垦的农民, 并向客户端同步
        // player.addIdleFarmerCount(farmerCnt);
        // playerDataManager.syncRoleInfo(player);
        // // 移除开垦队列
        // player.getReclaimQue().remove(reclaimIndex);
        // // 向客户端同步开垦出的地基
        // GamePb1.SynExploreOrReclaimRs.Builder builder = GamePb1.SynExploreOrReclaimRs.newBuilder();
        // builder.setType(2);
        // builder.setCellId(cellId);
        // builder.addAllFoundationId(unlockFoundationIdList);
        // builder.setFinishedReclaimQueIndex(reclaimIndex);
        // BasePb.Base.Builder msg = PbHelper.createSynBase(GamePb1.SynExploreOrReclaimRs.EXT_FIELD_NUMBER, GamePb1.SynExploreOrReclaimRs.ext, builder.build());
        // MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
    }

    @GmCmd("buildCity")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {
        switch (params[0]) {
            // 清楚所有已探索的格子
            case "clearMapCell":
                player.getMapCellData().clear();
                List<Integer> cellState = new ArrayList<>(2);
                cellState.add(1);
                cellState.add(0);
                player.getMapCellData().put(1, cellState);
                playerDataManager.syncRoleInfo(player);
                break;
            case "fixFoundationData":
                List<Integer> newFoundationData = player.getFoundationData().stream().distinct().collect(Collectors.toList());
                player.getFoundationData().clear();
                player.getFoundationData().addAll(newFoundationData);
                playerDataManager.syncRoleInfo(player);
                break;
            default:
        }
    }
}
