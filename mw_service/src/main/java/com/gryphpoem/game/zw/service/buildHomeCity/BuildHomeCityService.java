package com.gryphpoem.game.zw.service.buildHomeCity;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticBuildCityDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticBuildingDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticCombatDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticIniDataMgr;
import com.gryphpoem.game.zw.manager.BuildingDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.manager.SmallIdManager;
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
import com.gryphpoem.game.zw.resource.domain.p.Combat;
import com.gryphpoem.game.zw.resource.domain.p.Mill;
import com.gryphpoem.game.zw.resource.domain.s.StaticBuildingInit;
import com.gryphpoem.game.zw.resource.domain.s.StaticBuildingLv;
import com.gryphpoem.game.zw.resource.domain.s.StaticCombat;
import com.gryphpoem.game.zw.resource.domain.s.StaticHomeCityCell;
import com.gryphpoem.game.zw.resource.domain.s.StaticHomeCityFoundation;
import com.gryphpoem.game.zw.resource.domain.s.StaticIniLord;
import com.gryphpoem.game.zw.resource.domain.s.StaticSimNpc;
import com.gryphpoem.game.zw.resource.domain.s.StaticSimulatorChoose;
import com.gryphpoem.game.zw.resource.domain.s.StaticSimulatorStep;
import com.gryphpoem.game.zw.resource.pojo.buildHomeCity.BuildingState;
import com.gryphpoem.game.zw.resource.pojo.fight.FightLogic;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.pojo.simulator.LifeSimulatorInfo;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.RandomHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.CombatService;
import com.gryphpoem.game.zw.service.FightService;
import com.gryphpoem.game.zw.service.GmCmd;
import com.gryphpoem.game.zw.service.GmCmdService;
import com.gryphpoem.game.zw.service.PlayerService;
import com.gryphpoem.game.zw.service.simulator.LifeSimulatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
    @Autowired
    private SmallIdManager smallIdManager;
    @Autowired
    private LifeSimulatorService lifeSimulatorService;
    @Autowired
    private FightService fightService;

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
        cellState.add(staticHomeCityCell.getHasBandit()); // 土匪keyId
        cellState.add(getBanditTalkSimulator(staticHomeCityCell.getHasBandit())); // 土匪对应的对话引导模拟器
        cellState.add(-1); // 侦察出现的土匪不会自动过期消失
        newAddMapCellData.put(cellId, cellState);
        // 关联解锁的地图格子
        List<Integer> bindCellList = staticHomeCityCell.getBindCellList();
        for (Integer bindCellId : bindCellList) {
            StaticHomeCityCell bindCell = StaticBuildCityDataMgr.getStaticHomeCityCellById(cellId);
            List<Integer> bindCellState = new ArrayList<>(2);
            bindCellState.add(0); // 未开垦
            bindCellState.add(bindCell.getHasBandit()); // 土匪keyId
            bindCellState.add(getBanditTalkSimulator(bindCell.getHasBandit())); // 土匪对应的对话引导模拟器
            bindCellState.add(-1); // 侦察出现的土匪不会自动过期消失
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
            mapCell.setReclaimed(value.get(0) == 1);
            mapCell.setNpcId(value.get(1));
            mapCell.setSimType(value.get(2));
            mapCell.setBanditRefreshTime(value.get(3));
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
        int latestPlayTime = peaceAndWelfareRecord.get(type);
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
        int happinessTopLimit = Constant.HAPPINESS_TOP_LIMIT;
        player.setHappiness(Math.min(happinessTopLimit, player.getHappiness() + happinessAdd.get(0)));
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
     * 给土匪随机对话引导模拟器
     *
     * @param banditId
     * @return
     */
    public int getBanditTalkSimulator(int banditId) {
        StaticSimNpc staticSimNpc = StaticBuildCityDataMgr.getStaticSimNpcById(banditId);
        if (staticSimNpc == null) {
            return 0;
        }

        List<List<Integer>> simTypeList = staticSimNpc.getSimType();
        if (CheckNull.isEmpty(simTypeList)) {
            return 0;
        }
        for (List<Integer> simType : simTypeList) {
            if (CheckNull.isEmpty(simType) || simType.size() < 2) {
                continue;
            }
            boolean hit = RandomHelper.isHitRangeIn10000(simType.get(1));
            if (hit) {
                return simType.get(0);
            }
        }

        return 0;
    }

    /**
     * 清剿土匪
     *
     * @param roleId
     * @param rq
     * @return
     */
    public GamePb1.ClearBanditRs clearBandit(long roleId, GamePb1.ClearBanditRq rq) {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        GamePb1.ClearBanditRs.Builder builder = GamePb1.ClearBanditRs.newBuilder();
        Map<Integer, List<Integer>> mapCellData = player.getMapCellData();
        int cellId = rq.getCellId();
        if (!mapCellData.containsKey(cellId)) {
            throw new MwException(GameError.PARAM_ERROR, String.format("清剿土匪时, 地图格不存在或未解锁, roleId:%s, cellId:%s", roleId, cellId));
        }
        int clearType = rq.getClearType();
        CommonPb.Award addAward = null;
        CommonPb.Award subAward = null;
        // 模拟器结算
        if (clearType == 1) {
            List<CommonPb.LifeSimulatorStep> lifeSimulatorStepList = rq.getLifeSimulatorStepList();
            if (CheckNull.nonEmpty(lifeSimulatorStepList)) {
                // 标识模拟器是否结束
                boolean isEnd = false;
                List<List<Integer>> finalRewardList = new ArrayList<>();
                List<List<Integer>> finalCharacterFixList = new ArrayList<>();
                List<Integer> delay = null;
                for (CommonPb.LifeSimulatorStep lifeSimulatorStep : lifeSimulatorStepList) {
                    int chooseId = lifeSimulatorStep.getChooseId();
                    if (chooseId > 0) {
                        StaticSimulatorChoose sSimulatorChoose = StaticBuildCityDataMgr.getStaticSimulatorChoose(chooseId);
                        // 性格值变化
                        List<List<Integer>> characterFix = sSimulatorChoose.getCharacterFix();
                        finalCharacterFixList.addAll(characterFix);
                        // 奖励变化
                        List<List<Integer>> rewardList = sSimulatorChoose.getRewardList();
                        finalRewardList.addAll(rewardList);
                        // TODO buff增益
                        List<List<Integer>> buff = sSimulatorChoose.getBuff();
                    }
                    long stepId = lifeSimulatorStep.getStepId();
                    StaticSimulatorStep staticSimulatorStep = StaticBuildCityDataMgr.getStaticSimulatorStep(stepId);
                    // 根据配置, 如果没有下一步, 则模拟器结束
                    long nextId = staticSimulatorStep.getNextId();
                    List<List<Long>> staticChooseList = staticSimulatorStep.getChoose();
                    List<Long> playerChoose = new ArrayList<>();
                    if (CheckNull.nonEmpty(staticChooseList)) {
                        playerChoose = staticChooseList.stream().filter(tmp -> tmp.size() == 3 && tmp.get(0) == (long) chooseId && tmp.get(1) != 0L).findFirst().orElse(null);
                    }
                    if (CheckNull.nonEmpty(playerChoose)) {
                        nextId = playerChoose.get(1);
                    }
                    if (!isEnd) {
                        isEnd = nextId == 0L;
                    }
                    // 如果该步有延时执行, 新增模拟器器延时任务
                    delay = staticSimulatorStep.getDelay();
                    if (CheckNull.nonEmpty(delay)) {
                        LifeSimulatorInfo delaySimulator = new LifeSimulatorInfo();
                        delaySimulator.setType(delay.get(1));// 延时后执行哪一个模拟器
                        delaySimulator.setPauseTime(TimeHelper.getCurrentDay());
                        delaySimulator.setDelay(delay.get(0));// 延时时间
                        List<LifeSimulatorInfo> lifeSimulatorInfos = player.getLifeSimulatorRecordMap().computeIfAbsent(4, k -> new ArrayList<>());
                        lifeSimulatorInfos.add(delaySimulator);
                    }
                }
                if (!isEnd) {
                    throw new MwException(GameError.SIMULATOR_IS_NOT_END, String.format("记录模拟器结果时, 模拟器未结束, roleId:%s", roleId));
                }
                // 更新性格值并发送对应奖励
                if (CheckNull.nonEmpty(finalCharacterFixList)) {
                    if (CheckNull.isEmpty(player.getCharacterData())) {
                        player.setCharacterData(new HashMap<>(6));
                    }
                    if (CheckNull.isEmpty(player.getCharacterRewardRecord())) {
                        player.setCharacterRewardRecord(new HashMap<>(8));
                    }
                    for (List<Integer> characterChange : finalCharacterFixList) {
                        int index = characterChange.get(0);
                        int value = characterChange.get(1);
                        int addOrSub = characterChange.get(0);
                        lifeSimulatorService.updateCharacterData(player.getCharacterData(), index, value, addOrSub);
                    }
                    lifeSimulatorService.checkAndSendCharacterReward(player);
                    // 同步领主性格变化
                    playerDataManager.syncRoleInfo(player);
                }
                // 更新对应奖励变化
                if (CheckNull.nonEmpty(finalRewardList)) {
                    for (List<Integer> reward : finalRewardList) {
                        int awardType = reward.get(0);
                        int awardId = reward.get(1);
                        int awardCount = reward.get(2);
                        int addOrSub = reward.get(3);
                        switch (addOrSub) {
                            case 1:
                                rewardDataManager.sendRewardSignle(player, awardType, awardId, awardCount, AwardFrom.SIMULATOR_CHOOSE_REWARD, "");
                                break;
                            case 0:
                                // 如果资源不足则扣减至0
                                rewardDataManager.subPlayerResCanSubCount(player, awardType, awardId, awardCount, AwardFrom.SIMULATOR_CHOOSE_REWARD, "");
                                break;
                        }
                    }
                }
            }
            if (!rq.getClearResult()) {
                return builder.build();
            }
        }

        // 小游戏结算
        if (clearType == 2) {
            if (!rq.getClearResult()) {
                return builder.build();
            }
            int miniGameId = rq.getMiniGameId();
            // TODO 获取小游戏奖励
        }

        // 战斗结算
        boolean combatResult = false;
        if (clearType == 3) {
            List<Integer> heroIdList = rq.getHeroIdList();
            if (CheckNull.isEmpty(heroIdList)) {
                throw new MwException(GameError.PARAM_ERROR, String.format("战斗方式清剿土匪时, 上阵将领为空, roleId:%s, cellId:%s", roleId, cellId));
            }
            // 采用当前玩家通关的战役的最后一关的配置
            Combat combat = player.combats.values().stream().max(Comparator.comparingInt(Combat::getCombatId)).orElse(null);
            if (combat != null) {
                StaticCombat staticCombat = StaticCombatDataMgr.getStaticCombat(combat.getCombatId());
                combatResult = doCombat(player, staticCombat, heroIdList, builder);
            }
        }

        // 清剿成功, 清除地图土匪状态
        if (rq.getClearResult() || combatResult) {
            delBanditStateOfCell(player, cellId);
        }

        return builder.build();
    }

    /**
     * 战斗清剿土匪
     *
     * @param player
     * @param staticCombat
     * @param heroIds
     * @param builder
     * @return
     * @throws MwException
     */
    private boolean doCombat(Player player, StaticCombat staticCombat, List<Integer> heroIds, GamePb1.ClearBanditRs.Builder builder)
            throws MwException {
        int combatId = staticCombat.getCombatId();
        Fighter attacker = fightService.createCombatPlayerFighter(player, heroIds);
        Fighter defender = fightService.createNpcFighter(staticCombat.getForm());
        FightLogic fightLogic = new FightLogic(attacker, defender, true);
        if (Constant.DONT_DODGE_CRIT_COMBATID.contains(combatId)) {
            fightLogic.setCareCrit(false);
            fightLogic.setCareDodge(false);
        }
        fightLogic.fight();
        builder.setCombatResult(fightLogic.getWinState());
        if (fightLogic.getWinState() == 1) {
            int star = DataResource.ac.getBean(CombatService.class).combatStarCalc(attacker.getLost(), attacker.getTotal());
            builder.setStar(star);
        }

        // 进攻方hero信息
        List<CommonPb.RptHero> attackHeroInfo = attacker.forces.stream()
                .map(force -> PbHelper.createRptHero(
                        Constant.Role.PLAYER,
                        force.killed,
                        0,
                        force.id,
                        null,
                        0,
                        0,
                        force.totalLost
                )).collect(Collectors.toList());
        builder.addAllAtkHero(attackHeroInfo);
        // 防守方hero信息
        List<CommonPb.RptHero> defendHeroInfo = defender.forces.stream()
                .map(force -> PbHelper.createRptHero(
                        Constant.Role.BANDIT,
                        force.killed,
                        0,
                        force.id,
                        null,
                        0,
                        0,
                        force.totalLost
                )).collect(Collectors.toList());
        builder.addAllDefHero(defendHeroInfo);

        CommonPb.Record record = fightLogic.generateRecord();
        builder.setRecord(record);

        return fightLogic.getWinState() == 1;
    }

    /**
     * 清除地图土匪状态
     *
     * @param player
     * @param cellId
     */
    private void delBanditStateOfCell(Player player, int cellId) {
        Map<Integer, List<Integer>> mapCellData = player.getMapCellData();
        List<Integer> cellState = mapCellData.get(cellId);
        if (CheckNull.isEmpty(cellState)) {
            return;
        }
        int isReclaimed = cellState.get(0);
        cellState.clear();
        cellState.add(isReclaimed);
        cellState.add(0); // 土匪id清空
        cellState.add(0); // 对话引导模拟器类型清空
    }

    /**
     * 土匪过期清除定时器
     */
    public void autoDelBanditTimerLogic() {
        Iterator<Player> iterator = playerDataManager.getPlayers().values().iterator();
        int now = TimeHelper.getCurrentSecond();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            try {
                Map<Integer, List<Integer>> mapCellData = player.getMapCellData();
                mapCellData.entrySet().stream()
                        .filter(tmp ->
                                CheckNull.nonEmpty(tmp.getValue())
                                        && tmp.getValue().size() >= 4
                                        && tmp.getValue().get(1) > 0
                                        && tmp.getValue().get(3) != -1 && now > tmp.getValue().get(3)
                        )
                        .forEach(entry -> {
                            int reclaimed = entry.getValue().get(0);
                            List<Integer> newCellState = new ArrayList<>(4);
                            newCellState.add(reclaimed);
                            newCellState.add(0);
                            newCellState.add(0);
                            newCellState.add(0);
                            entry.setValue(newCellState);
                        });
            } catch (Exception e) {
                LogUtil.error("土匪过期清除定时器报错, lordId:" + player.lord.getLordId(), e);
            }
        }
    }

    /**
     * 土匪每日刷新
     */
    public void refreshBanditJob() {
        Iterator<Player> iterator = playerDataManager.getPlayers().values().iterator();
        int now = TimeHelper.getCurrentSecond();
        List<StaticHomeCityCell> canRefreshBanditCellList = StaticBuildCityDataMgr.getCanRefreshBanditCellList();
        List<StaticSimNpc> npcList = StaticBuildCityDataMgr.getStaticSimNpcList().stream()
                .filter(tmp -> tmp.getType() == 2)
                .collect(Collectors.toList());
        if (CheckNull.isEmpty(npcList)) {
            return;
        }
        while (iterator.hasNext()) {
            Player player = iterator.next();
            if (player.lord.getLevel() < 42 || DateHelper.isSameDate(player.account.getCreateDate(), new Date())) {
                continue;
            }
            try {
                Map<Integer, List<Integer>> mapCellData = player.getMapCellData();
                List<Integer> ownCellIds = mapCellData.entrySet().stream()
                        .filter(tmp -> CheckNull.nonEmpty(tmp.getValue()) && tmp.getValue().size() >= 4 && tmp.getValue().get(1) == 0)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());
                List<Integer> cellIds = canRefreshBanditCellList.stream()
                        .map(StaticHomeCityCell::getId)
                        .filter(ownCellIds::contains).collect(Collectors.toList());
                if (CheckNull.isEmpty(cellIds)) {
                    continue;
                }
                Collections.shuffle(cellIds);
                int cellId = cellIds.get(0);
                List<Integer> cellState = mapCellData.get(cellId);
                Collections.shuffle(npcList);
                StaticSimNpc staticSimNpc = npcList.get(0);
                List<List<Integer>> simTypeList = staticSimNpc.getSimType();
                int simType = 0;
                if (CheckNull.nonEmpty(simTypeList)) {
                    for (List<Integer> list : simTypeList) {
                        if (CheckNull.isEmpty(list) || list.size() < 2) {
                            continue;
                        }
                        int weight = list.get(1);
                        boolean hit = RandomHelper.isHitRangeIn10000(weight);
                        if (hit) {
                            simType = list.get(0);
                            break;
                        }
                    }
                }
                if (simType == 0) {
                    continue;
                }
                cellState.set(1, staticSimNpc.getId());
                cellState.set(2, simType);
                cellState.set(3, now);
                playerDataManager.syncRoleInfo(player);
            } catch (Exception e) {
                LogUtil.error("土匪每日刷新定时器报错, lordId:" + player.lord.getLordId(), e);
            }
        }
    }

    /**
     * 叛军入侵定时器
     */
    public void rebelInvadeTimerLogic() {
        Iterator<Player> iterator = playerDataManager.getPlayers().values().iterator();
        int now = TimeHelper.getCurrentSecond();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            if (player.lord.getLevel() < 42 || player.lord.getLevel() > 63) {
                continue;
            }
            try {
                // 获取玩家最近一次被玩家攻击时间
                int playerAttackTime = player.getPlayerAttackTime();
                // 获取最近一次被叛军入侵时间
                int rebelInvadeTime = player.getRebelInvadeTime();
                if (now - playerAttackTime < 24 * 60 * 60 && now - rebelInvadeTime < 24 * 60 * 60) {
                    continue;
                }
                // 如果二者都超过24小时, 进行叛军入侵
                int maxCombatId = player.combats.values().stream()
                        .mapToInt(Combat::getCombatId)
                        .max()
                        .orElse(0);
                int combatId = StaticCombatDataMgr.getCombatMap().values().stream()
                        .filter(tmp -> tmp.getCombatId() > maxCombatId)
                        .mapToInt(StaticCombat::getCombatId)
                        .min()
                        .orElse(0);
                if (combatId == 0) {
                    continue;
                }
                StaticCombat staticCombat = StaticCombatDataMgr.getStaticCombat(combatId);

            } catch (Exception e) {
                LogUtil.error("土匪过期清除定时器报错, lordId:" + player.lord.getLordId(), e);
            }
        }
    }

    @GmCmd("buildCity")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {
        switch (params[0]) {
            case "clearMapCell":
                // 重置所有已探索的格子、已开垦的地基、已解锁的建筑
                resetWholeMainCityMapAndBuilding(player);
                break;
            case "fixFoundationData":
                // 去除重复地基
                fixFoundationData(player);
                break;
            case "openTheWholeMap":
                // 一键解锁全部地图格和地基
                openTheWholeMap(player);
                break;
            case "clearBuildQue":
                // 清除建筑队列
                player.buildQue.clear();
                break;
            case "resetResidentData":
                // 重置居民数据
                resetResidentData(player);
                break;
            case "resetHappinessTime":
                // 重置幸福度恢复时间
                player.setHappinessTime(0);
                break;
            default:
        }
        playerDataManager.syncRoleInfo(player);
    }

    /**
     * 重置所有已探索的格子、已开垦的地基、已解锁的建筑
     *
     * @param player
     */
    public void resetWholeMainCityMapAndBuilding(Player player) {
        Map<Integer, List<Integer>> mapCellData = player.getMapCellData();
        List<Integer> foundationData = player.getFoundationData();
        Map<Integer, BuildingState> buildingData = player.getBuildingData();
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
        buildingDataManager.updateBuildingLockState(player);
    }

    /**
     * 去除重复地基
     *
     * @param player
     */
    public void fixFoundationData(Player player) {
        List<Integer> foundationData = player.getFoundationData();
        List<Integer> newFoundationData = foundationData.stream().distinct().collect(Collectors.toList());
        foundationData.clear();
        foundationData.addAll(newFoundationData);
    }

    /**
     * 一键解锁全部地图格和地基
     *
     * @param player
     */
    public void openTheWholeMap(Player player) {
        Map<Integer, List<Integer>> mapCellData = player.getMapCellData();
        List<Integer> foundationData = player.getFoundationData();
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
    }

    /**
     * 重置玩家人口数据
     *
     * @param player
     */
    public void resetResidentData(Player player) {
        List<Integer> residentData = new ArrayList<>(player.getResidentData());
        Map<Integer, BuildingState> buildingData = player.getBuildingData();
        StaticIniLord staticIniLord = StaticIniDataMgr.getLordIniData();
        List<Integer> residentCnt = staticIniLord.getResidentCnt();
        residentData.clear();
        if (CheckNull.nonEmpty(residentCnt)) {
            residentData.add(residentCnt.get(1)); // 总数
            residentData.add(residentCnt.get(1)); // 空闲数
            residentData.add(residentCnt.get(0)); // 上限
        } else {
            residentData.add(4); // 总数
            residentData.add(4); // 空闲数
            residentData.add(4); // 上限
        }
        player.setResidentData(residentData);
        // 获取已解锁的民居加成居民上限
        int sum = buildingData.values().stream()
                .filter(tmp -> tmp.getBuildingType() == BuildingType.RESIDENT_HOUSE && tmp.getBuildingLv() > 0)
                .mapToInt(BuildingState::getResidentTopLimit)
                .sum();
        buildingData.values().forEach(buildingState -> buildingState.setResidentCnt(0));
        player.addResidentTopLimit(sum);
    }
}