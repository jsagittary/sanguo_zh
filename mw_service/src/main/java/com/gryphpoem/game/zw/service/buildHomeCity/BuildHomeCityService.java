package com.gryphpoem.game.zw.service.buildHomeCity;

import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.dataMgr.StaticBuildCityDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticBuildingDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticCombatDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticIniDataMgr;
import com.gryphpoem.game.zw.manager.BuildingDataManager;
import com.gryphpoem.game.zw.manager.FightRecordDataManager;
import com.gryphpoem.game.zw.manager.MailDataManager;
import com.gryphpoem.game.zw.manager.MsgDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.manager.WarDataManager;
import com.gryphpoem.game.zw.manager.WorldDataManager;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.BattlePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.pb.GamePb2;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Fighter;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.constant.BuildingType;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.MailConstant;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.dao.impl.p.BuildingDao;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Building;
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
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.buildHomeCity.BuildingState;
import com.gryphpoem.game.zw.resource.pojo.buildHomeCity.MapCell;
import com.gryphpoem.game.zw.resource.pojo.buildHomeCity.MiniGameScoutState;
import com.gryphpoem.game.zw.resource.pojo.buildHomeCity.PeaceAndWelfareRecord;
import com.gryphpoem.game.zw.resource.pojo.world.Battle;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import com.gryphpoem.game.zw.resource.util.MapHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.CombatService;
import com.gryphpoem.game.zw.service.FightService;
import com.gryphpoem.game.zw.service.GmCmd;
import com.gryphpoem.game.zw.service.GmCmdService;
import com.gryphpoem.game.zw.service.WallService;
import com.gryphpoem.game.zw.service.WarService;
import com.gryphpoem.game.zw.service.WorldService;
import com.gryphpoem.game.zw.service.simulator.LifeSimulatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private BuildingDataManager buildingDataManager;
    @Autowired
    private BuildingDao buildingDao;
    @Autowired
    private LifeSimulatorService lifeSimulatorService;
    @Autowired
    private FightService fightService;
    @Autowired
    private WarService warService;
    @Autowired
    private WallService wallService;
    @Autowired
    private WorldService worldService;
    @Autowired
    private FightRecordDataManager fightRecordDataManager;
    @Autowired
    private WorldDataManager worldDataManager;
    @Autowired
    private WarDataManager warDataManager;
    @Autowired
    private MailDataManager mailDataManager;

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
        Map<Integer, MapCell> mapCellData = player.getMapCellData();
        if (mapCellData.containsKey(cellId)) {
            throw new MwException(GameError.MAP_CELL_ALREADY_EXPLORED, String.format("探索主城迷雾区时, 该迷雾区已被探索, roleId:%s, cellId:%s", roleId, cellId));
        }
        // 配置的周边4个格子是否已解锁
        List<Integer> neighborCellList = staticHomeCityCell.getNeighborCellList();
        boolean anyMatch = neighborCellList.stream().anyMatch(mapCellData::containsKey);
        if (!anyMatch) {
            throw new MwException(GameError.NO_ONE_NEIGHBOR_IS_UNLOCK, String.format("探索主城迷雾区时, 四周的格子没有一个是解锁的, roleId:%s, cellId:%s", roleId, cellId));
        }

        List<MapCell> newAddMapCellData = new ArrayList<>();
        // 新增解锁的地图格子
        MapCell mapCell = new MapCell();
        mapCell.setCellId(cellId);
        mapCell.setReclaimed(false);
        int banditId = staticHomeCityCell.getHasBandit();
        mapCell.setNpcId(banditId);
        List<Integer> simInfo = getBanditTalkSimulator(banditId);
        int simType = 0;
        int simId = 0;
        if (CheckNull.nonEmpty(simInfo) && simInfo.size() >= 2) {
            simType = simInfo.get(0);
            simId = simInfo.get(1);
        }
        mapCell.setSimType(banditId > 0 ? simType : 0);
        mapCell.setSimId(banditId > 0 ? simId : 0);
        mapCell.setBanditRefreshTime(banditId > 0 ? -1 : 0);
        newAddMapCellData.add(mapCell);
        mapCellData.put(cellId, mapCell);
        // 关联解锁的地图格子
        List<Integer> bindCellList = staticHomeCityCell.getBindCellList();
        for (Integer bindCellId : bindCellList) {
            StaticHomeCityCell bindStaticCell = StaticBuildCityDataMgr.getStaticHomeCityCellById(cellId);
            MapCell bindCell = new MapCell();
            bindCell.setCellId(cellId);
            bindCell.setReclaimed(false);
            int banditId_ = bindStaticCell.getHasBandit();
            bindCell.setNpcId(banditId_);
            List<Integer> simInfo_ = getBanditTalkSimulator(banditId_);
            int simType_ = 0;
            int simId_ = 0;
            if (CheckNull.nonEmpty(simInfo_) && simInfo_.size() >= 2) {
                simType_ = simInfo_.get(0);
                simId_ = simInfo_.get(1);
            }
            mapCell.setSimType(banditId_ > 0 ? simType_ : 0);
            mapCell.setSimId(banditId_ > 0 ? simId_ : 0);
            bindCell.setBanditRefreshTime(banditId_ > 0 ? -1 : 0);
            if (!mapCellData.containsKey(bindCellId)) {
                newAddMapCellData.add(bindCell);
            }
            mapCellData.put(bindCellId, bindCell);
        }

        GamePb1.ExploreRs.Builder builder = GamePb1.ExploreRs.newBuilder();
        for (MapCell temp : newAddMapCellData) {
            builder.addMapCellData(temp.ser());
        }
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
        Map<Integer, MapCell> mapCellData = player.getMapCellData();
        MapCell mapCell = mapCellData.get(cellId);
        if (mapCell == null) {
            throw new MwException(GameError.INSUFFICIENT_LORD_LEVEL, String.format("开垦地基时, 该格子还未解锁, roleId:%s, cellId:%s", roleId, cellId));
        }
        if (mapCell.getReclaimed()) {
            throw new MwException(GameError.INSUFFICIENT_LORD_LEVEL, String.format("开垦地基时, 该格子已被开垦, roleId:%s, cellId:%s", roleId, cellId));
        }
        GamePb1.ReclaimFoundationRs.Builder builder = GamePb1.ReclaimFoundationRs.newBuilder();
        // 更新格子开垦状态
        mapCell.setReclaimed(true);
        builder.addMapCellData(mapCell.ser());// 返回被开垦格子的信息
        // 获取玩家新增解锁的地基id, 解锁地基需要该地基所占的格子全部被开垦完成
        List<Integer> foundationIdList = StaticBuildCityDataMgr.getFoundationIdListByCellId(cellId); // 开垦的格子对应可解锁的地基
        List<Integer> unlockFoundationIdList = new ArrayList<>();
        // 获取玩家已开垦的格子
        List<Integer> reclaimedCellIdList = new ArrayList<>();
        for (MapCell temp : mapCellData.values()) {
            if (temp.getReclaimed()) {
                reclaimedCellIdList.add(temp.getCellId());
            }
        }
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
            boolean wallUnlock = buildingDataManager.checkBuildingLock(player, BuildingType.WALL);
            if (wallUnlock) {
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
        Map<Integer, PeaceAndWelfareRecord> peaceAndWelfareRecordMap = player.getPeaceAndWelfareRecord();
        int type = rq.getType();
        if (type != 1 && type != 2) {
            throw new MwException(GameError.PARAM_ERROR, String.format("玩家进行安民济物时, 参数错误, roleId:%S, type:%s", roleId, type));
        }
        PeaceAndWelfareRecord peaceAndWelfareRecord = peaceAndWelfareRecordMap.get(type);
        int now = TimeHelper.getCurrentSecond();
        if (peaceAndWelfareRecord == null) {
            peaceAndWelfareRecord = new PeaceAndWelfareRecord();
            peaceAndWelfareRecord.setType(type);
            peaceAndWelfareRecord.setLastTime(now);
            peaceAndWelfareRecord.setCurDayCnt(0);
            peaceAndWelfareRecord.setTotalCnt(0);
            peaceAndWelfareRecordMap.put(type, peaceAndWelfareRecord);
        }

        if (peaceAndWelfareRecord.getCurDayCnt() > 0 && DateHelper.isToday(TimeHelper.getDate(peaceAndWelfareRecord.getLastTime()))) {
            throw new MwException(GameError.PARAM_ERROR, String.format("玩家今日已进行过安民济物, roleId:%s, type:%s, lastTime:%s", roleId, type, peaceAndWelfareRecord.getLastTime()));
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
        peaceAndWelfareRecord.setCurDayCnt(peaceAndWelfareRecord.getCurDayCnt() + 1);
        peaceAndWelfareRecord.setLastTime(now);

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
    public List<Integer> getBanditTalkSimulator(int banditId) {
        StaticSimNpc staticSimNpc = StaticBuildCityDataMgr.getStaticSimNpcById(banditId);
        if (staticSimNpc == null) {
            return null;
        }

        List<List<Integer>> simTypeList = staticSimNpc.getSimType();
        if (CheckNull.isEmpty(simTypeList)) {
            return null;
        }

        List<Integer> result = null;
        for (List<Integer> simType : simTypeList) {
            if (CheckNull.isEmpty(simType) || simType.size() < 3) {
                continue;
            }
            boolean hit = RandomHelper.isHitRangeIn10000(simType.get(2));
            if (hit) {
                result = simType;
                break;
            }
        }
        if (CheckNull.isEmpty(result)) {
            Collections.shuffle(simTypeList);
            result = simTypeList.get(0);
        }

        return result;
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
        Map<Integer, MapCell> mapCellData = player.getMapCellData();
        int cellId = rq.getCellId();
        if (!mapCellData.containsKey(cellId)) {
            throw new MwException(GameError.PARAM_ERROR, String.format("清剿土匪时, 地图格不存在或未解锁, roleId:%s, cellId:%s", roleId, cellId));
        }
        MapCell mapCell = mapCellData.get(cellId);
        if (mapCell == null || mapCell.getNpcId() == 0 || mapCell.getSimType() == 0) {
            throw new MwException(GameError.DATA_EXCEPTION, String.format("清剿土匪时, 地图格对应的土匪信息错误, roleId:%s, cellId:%s", roleId, cellId));
        }
        /*int simulatorType = mapCell.getSimType();
        List<StaticSimulatorStep> staticSimulatorStepList = StaticBuildCityDataMgr.getStaticSimulatorStepByType(simulatorType);
        // 获取土匪引导对话模拟器的第1步，根据玩家选择的选项判断其清剿土匪的方式
        StaticSimulatorStep firstStep = staticSimulatorStepList.stream().min(Comparator.comparingLong(StaticSimulatorStep::getId)).orElse(null);
        if (firstStep == null || CheckNull.isEmpty(firstStep.getChoose())) {
            throw new MwException(GameError.NO_CONFIG, String.format("清剿土匪时, 未获取到土匪对应的对话模拟器配置, roleId:%s, cellId:%s", roleId, cellId));
        }

        long choose = rq.getChooseId();
        List<List<Long>> firstStepChooseList = firstStep.getChoose();
        List<Long> firstStepChoose = firstStepChooseList.stream().filter(tmp -> CheckNull.nonEmpty(tmp) && tmp.size() >= 3 && tmp.get(0) == choose).findFirst().orElse(null);
        if (firstStepChoose == null) {
            throw new MwException(GameError.NO_CONFIG, String.format("清剿土匪时, 未获取到土匪对应的对话模拟器配置, roleId:%s, cellId:%s", roleId, cellId));
        }

        long selectedChooseId = firstStepChoose.get(0);*/
        long selectedChooseId = rq.getChooseId();
        boolean clearResult = rq.getClearResult();
        List<CommonPb.LifeSimulatorStep> lifeSimulatorStepList = rq.getLifeSimulatorStepList();

        StaticSimulatorChoose staticSimulatorChoose = StaticBuildCityDataMgr.getStaticSimulatorChoose(selectedChooseId);
        List<List<Integer>> finalRewardList = new ArrayList<>();
        if (CheckNull.nonEmpty(staticSimulatorChoose.getMiniGame())) {
            // 小游戏结算
            // 获取小游戏奖励
            List<List<Integer>> rewardList = staticSimulatorChoose.getRewardList();
            finalRewardList.addAll(rewardList);
        } else if (CheckNull.nonEmpty(lifeSimulatorStepList)) {
            // 根据配置规则, 说明是进入模拟器结算方式
            // 标识模拟器是否结束
            boolean isEnd = false;
            // List<List<Integer>> finalCharacterFixList = new ArrayList<>();
            // List<Integer> delay = null;
            for (CommonPb.LifeSimulatorStep lifeSimulatorStep : lifeSimulatorStepList) {
                int chooseId = lifeSimulatorStep.getChooseId();
                if (chooseId > 0) {
                    StaticSimulatorChoose sSimulatorChoose = StaticBuildCityDataMgr.getStaticSimulatorChoose(chooseId);
                    // // 性格值变化
                    // List<List<Integer>> characterFix = sSimulatorChoose.getCharacterFix();
                    // finalCharacterFixList.addAll(characterFix);
                    // 奖励变化
                    List<List<Integer>> rewardList = sSimulatorChoose.getRewardList();
                    finalRewardList.addAll(rewardList);
                    // TODO buff增益
                    List<List<Integer>> buff = sSimulatorChoose.getBuff();
                }
                long stepId = lifeSimulatorStep.getStepId();
                StaticSimulatorStep staticSimulatorStep = StaticBuildCityDataMgr.getStaticSimulatorStepById(stepId);
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
                /*// 如果该步有延时执行, 新增模拟器器延时任务
                delay = staticSimulatorStep.getDelay();
                if (CheckNull.nonEmpty(delay)) {
                    LifeSimulatorInfo delaySimulator = new LifeSimulatorInfo();
                    delaySimulator.setType(delay.get(1));// 延时后执行哪一个模拟器
                    delaySimulator.setPauseTime(TimeHelper.getCurrentDay());
                    delaySimulator.setDelay(delay.get(0));// 延时时间
                    List<LifeSimulatorInfo> lifeSimulatorInfos = player.getLifeSimulatorRecordMap().computeIfAbsent(4, k -> new ArrayList<>());
                    lifeSimulatorInfos.add(delaySimulator);
                }*/
            }
            if (!isEnd) {
                throw new MwException(GameError.SIMULATOR_IS_NOT_END, String.format("记录模拟器结果时, 模拟器未结束, roleId:%s", roleId));
            }
                /*// 更新性格值并发送对应奖励
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
                }*/
            // 模拟器结算根据是否有奖励来判断清剿结果
            if (CheckNull.nonEmpty(finalRewardList)) {
                clearResult = true;
            }
        } else {
            // 战斗结算
            List<Integer> heroIdList = rq.getHeroIdList();
            if (CheckNull.isEmpty(heroIdList)) {
                throw new MwException(GameError.PARAM_ERROR, String.format("战斗方式清剿土匪时, 上阵将领为空, roleId:%s, cellId:%s", roleId, cellId));
            }
            int combatId = 0;
            if (staticSimulatorChoose.getCombatId() == 0) {
                // 调用玩家当前所处战役章节前1章中最后1关的npc配置
                int maxSuccessCombatId = player.combats.values().stream().mapToInt(Combat::getCombatId).max().orElse(0);
                combatId = StaticCombatDataMgr.getCombatMap().values().stream()
                        .filter(staticCombat -> staticCombat.getIndex() == 6 && staticCombat.getCombatId() <= maxSuccessCombatId)
                        .mapToInt(StaticCombat::getCombatId)
                        .max()
                        .orElse(0);
                combatId = combatId == 0 ? Constant.INIT_COMBAT_ID : combatId;
            } else {
                combatId = staticSimulatorChoose.getCombatId();
            }
            if (combatId > 0) {
                StaticCombat staticCombat = StaticCombatDataMgr.getStaticCombat(combatId);
                if (staticCombat == null) {
                    clearResult = false;
                } else {
                    clearResult = doCombat(player, staticCombat, heroIdList, builder);
                }
            }
            if (clearResult) {
                finalRewardList = staticSimulatorChoose.getRewardList();
            }
        }

        // 清剿成功, 发放奖励, 清除土匪状态
        if (clearResult) {
            for (List<Integer> list : finalRewardList) {
                if (CheckNull.isEmpty(list) || list.size() < 4) {
                    continue;
                }
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
                CommonPb.Award.Builder awardBuilder = CommonPb.Award.newBuilder();
                awardBuilder.setType(list.get(0));
                awardBuilder.setId(list.get(1));
                awardBuilder.setCount(list.get(2));
                builder.addAward(awardBuilder.build());
            }
            delBanditStateOfCell(player, cellId);
            playerDataManager.syncRoleInfo(player);
        }

        builder.setClearResult(clearResult);
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
    private boolean doCombat(Player player,
                             StaticCombat staticCombat,
                             List<Integer> heroIds,
                             GamePb1.ClearBanditRs.Builder builder) throws MwException {
        int combatId = staticCombat.getCombatId();
        Fighter attacker = fightService.createCombatPlayerFighter(player, heroIds);
        Fighter defender = fightService.createNpcFighter(staticCombat.getForm());
        FightLogic fightLogic = new FightLogic(attacker, defender, true);
        if (Constant.DONT_DODGE_CRIT_COMBATID.contains(combatId)) {
            fightLogic.setCareCrit(false);
            fightLogic.setCareDodge(false);
        }
        fightLogic.start();
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
                        force,
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
                        force,
                        null,
                        0,
                        0,
                        force.totalLost
                )).collect(Collectors.toList());
        builder.addAllDefHero(defendHeroInfo);

        BattlePb.BattleRoundPb battleRoundPb = fightLogic.generateRecord();
        builder.setRecord(battleRoundPb);
        return fightLogic.getWinState() == 1;
    }

    /**
     * 清除地图土匪状态
     *
     * @param player
     * @param cellId
     */
    private void delBanditStateOfCell(Player player, int cellId) {
        Map<Integer, MapCell> mapCellData = player.getMapCellData();
        MapCell mapCell = mapCellData.get(cellId);
        if (mapCell == null) {
            return;
        }
        mapCell.setNpcId(0);
        mapCell.setSimType(0);
        mapCell.setSimId(0);
        mapCell.setBanditRefreshTime(0);
    }

    /**
     * 土匪过期清除定时器
     */
    public void autoDelBanditTimerLogic() {
        Iterator<Player> iterator = playerDataManager.getPlayers().values().iterator();
        int now = TimeHelper.getCurrentSecond();
        int banditRemainTime = Constant.BANDIT_REMAIN_TIME;
        if (banditRemainTime <= 0) {
            LogUtil.error(String.format("土匪持续时间配置错误, BANDIT_REMAIN_TIME-5023:%s", banditRemainTime));
            return;
        }
        while (iterator.hasNext()) {
            Player player = iterator.next();
            try {
                Map<Integer, MapCell> mapCellData = player.getMapCellData();
                List<Integer> cellIds = mapCellData.values().stream()
                        .filter(tmp -> tmp.getNpcId() > 0
                                && tmp.getBanditRefreshTime() != -1
                                && now - tmp.getBanditRefreshTime() > banditRemainTime
                        )
                        .map(MapCell::getCellId)
                        .collect(Collectors.toList());
                if (CheckNull.isEmpty(cellIds)) {
                    continue;
                }
                mapCellData.forEach((key, value) -> {
                    if (cellIds.contains(key)) {
                        value.setNpcId(0);
                        value.setSimType(0);
                        value.setSimId(0);
                        value.setBanditRefreshTime(0);
                    }
                });
                playerDataManager.syncRoleInfo(player);
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
        List<StaticHomeCityCell> canRefreshBanditCellList = StaticBuildCityDataMgr.getCanRefreshBanditCellList(); // 所有可刷新土匪的主城地图格
        List<Integer> banditRefreshCondition = Constant.BANDIT_REFRESH_CONDITION; // 土匪每日刷新开启条件
        if (CheckNull.isEmpty(banditRefreshCondition) || banditRefreshCondition.size() < 2) {
            LogUtil.error(String.format("土匪每日刷新配置错误, BANDIT_REFRESH_CONDITION-5027:%s", banditRefreshCondition.toString()));
            return;
        }
        while (iterator.hasNext()) {
            Player player = iterator.next();
            Date refreshDay = DateHelper.afterDayTimeDate(player.account.getCreateDate(), banditRefreshCondition.get(0) + 1);
            if (player.lord.getLevel() < banditRefreshCondition.get(1) || DateHelper.isSameDate(refreshDay, new Date())) {
                // 领主等级未达到, 或当前时间距离玩家创建账号的时间未达到配置要求的间隔时间, 则不刷新
                continue;
            }
            Map<Integer, MapCell> mapCellData = player.getMapCellData();
            // 校验当天是否已经有格子刷过土匪了
            List<MapCell> refreshedBanditMapCells = mapCellData.values().stream()
                    .filter(tmp -> tmp.getNpcId() > 0 && DateHelper.isSameDate(TimeHelper.secondToDate(tmp.getBanditRefreshTime()), new Date()))
                    .collect(Collectors.toList());
            if (CheckNull.nonEmpty(refreshedBanditMapCells)) {
                // 当天已经刷新过了不再刷新
                continue;
            }
            // 获取可随机的npc, 类型为每日土匪, 领主等级和君王殿等级满足要求
            List<StaticSimNpc> npcList = StaticBuildCityDataMgr.getStaticSimNpcList().stream()
                    .filter(tmp -> tmp.getType() == 2
                            && CheckNull.nonEmpty(tmp.getNpcLock())
                            && tmp.getNpcLock().size() >= 2
                            && tmp.getNpcLock().get(0) <= player.lord.getLevel()
                            && tmp.getNpcLock().get(1) <= player.building.getCommand()
                    )
                    .collect(Collectors.toList());
            if (CheckNull.isEmpty(npcList)) {
                continue;
            }
            try {
                // 获取玩家没有土匪的格子
                List<Integer> ownCellIds = mapCellData.entrySet().stream()
                        .filter(tmp -> tmp.getValue().getNpcId() == 0)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());
                // 获取可刷新土匪的格子
                List<Integer> cellIds = canRefreshBanditCellList.stream()
                        .map(StaticHomeCityCell::getId)
                        .filter(ownCellIds::contains).collect(Collectors.toList());
                if (CheckNull.isEmpty(cellIds)) {
                    continue;
                }
                Collections.shuffle(cellIds);
                int cellId = cellIds.get(0);
                MapCell mapCell = mapCellData.get(cellId);
                Collections.shuffle(npcList);
                StaticSimNpc staticSimNpc = npcList.get(0);
                int banditId = staticSimNpc == null ? 0 : staticSimNpc.getId();
                if (banditId == 0) {
                    continue;
                }
                mapCell.setNpcId(banditId);
                List<Integer> simInfo = getBanditTalkSimulator(banditId);
                int simType = 0;
                int simId = 0;
                if (CheckNull.nonEmpty(simInfo) && simInfo.size() >= 2) {
                    simType = simInfo.get(0);
                    simId = simInfo.get(1);
                }
                if (simType == 0 || simId == 0) {
                    continue;
                }
                mapCell.setSimType(simType);
                mapCell.setSimId(simId);
                mapCell.setBanditRefreshTime(now);
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
        List<Integer> rebelAttackConfig = Constant.REBEL_ATTACK_CONFIG;
        int rebelInvadeWarnTime = Constant.REBEL_INVADE_WARN_TIME;
        if (CheckNull.isEmpty(rebelAttackConfig) || rebelAttackConfig.size() < 3 || rebelInvadeWarnTime <= 0) {
            LogUtil.error(String.format("叛军入侵配置错误, REBEL_ATTACK_CONFIG-5003:%s, REBEL_INVADE_WARN_TIME-5026:%s", rebelAttackConfig.toString(), rebelInvadeWarnTime));
            return;
        }
        while (iterator.hasNext()) {
            Player player = iterator.next();
            if (player.lord.getLevel() < rebelAttackConfig.get(0) || player.lord.getLevel() > rebelAttackConfig.get(1)) {
                continue;
            }
            try {
                int pos = player.lord.getPos();
                // 获取这个点的战斗
                LinkedList<Battle> battles = warDataManager.getBattlePosMap().get(pos);
                if (CheckNull.nonEmpty(battles)) {
                    for (Battle battle : battles) {
                        if (battle.getType() == WorldConstant.BATTLE_TYPE_REBEL_INVADE && battle.getBattleTime() < now) {
                            GamePb2.SyncAttackRoleRs.Builder builder = GamePb2.SyncAttackRoleRs.newBuilder();
                            builder.setAtkCamp(Constant.Camp.NPC);
                            builder.setAtkName("叛军");
                            builder.setAtkTime(battle.getBattleTime());
                            builder.setStatus(WorldConstant.ATTACK_ROLE_0);
                            builder.setBattleType(battle.getType());
                            BasePb.Base.Builder msg = PbHelper.createSynBase(GamePb2.SyncAttackRoleRs.EXT_FIELD_NUMBER, GamePb2.SyncAttackRoleRs.ext, builder.build());
                            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
                            break;
                        }
                    }
                }
                // 获取玩家最近一次被玩家攻击时间
                int playerAttackTime = player.getPlayerAttackTime();
                // 获取最近一次叛军入侵时间
                int rebelInvadeTime = player.getRebelInvadeTime();
                if (now - playerAttackTime < rebelAttackConfig.get(2) && now - rebelInvadeTime < rebelAttackConfig.get(2)) {
                    continue;
                }
                // 如果二者都超过配置的持续时间, 则触发叛军入侵
                Combat combat = player.combats.values().stream()
                        .max(Comparator.comparingInt(Combat::getCombatId))
                        .orElse(null);
                if (combat == null) {
                    continue;
                }
                int combatId = 0;
                if (combat.getStar() <= 0) {
                    // 星级数小于0时, 表示该关卡未通过, 则直接取该战役关卡
                    combatId = combat.getCombatId();
                } else {
                    // 星级数大于0, 表示该关卡已通过, 则该关卡后面的一关
                    combatId = StaticCombatDataMgr.getCombatMap().values().stream()
                            .filter(tmp -> tmp.getCombatId() > combat.getCombatId())
                            .mapToInt(StaticCombat::getCombatId)
                            .min()
                            .orElse(0);
                }
                if (combatId == 0) {
                    continue;
                }
                StaticCombat staticCombat = StaticCombatDataMgr.getStaticCombat(combatId);
                Battle battle = new Battle();
                battle.setType(WorldConstant.BATTLE_TYPE_REBEL_INVADE);
                battle.setBattleType(staticCombat.getCombatId());
                battle.setBattleTime(now + rebelInvadeWarnTime - 1);
                battle.setBeginTime(now);
                battle.setDefencerId(player.roleId);
                battle.setPos(player.lord.getPos());
                battle.setDefencer(player);
                battle.setAtkCamp(Constant.Camp.NPC);
                battle.setDefCamp(player.lord.getCamp());
                battle.addAtkArm(staticCombat.getTotalArm());// 进攻方兵力
                warDataManager.addBattle(player, battle); // 添加战斗
                // 通知被攻击玩家
                if (player.isLogin) {
                    GamePb2.SyncAttackRoleRs.Builder builder = GamePb2.SyncAttackRoleRs.newBuilder();
                    builder.setAtkCamp(Constant.Camp.NPC);
                    builder.setAtkName("叛军");
                    builder.setAtkTime(battle.getBattleTime());
                    builder.setStatus(WorldConstant.ATTACK_ROLE_1);
                    builder.setBattleType(battle.getType());
                    BasePb.Base.Builder msg = PbHelper.createSynBase(GamePb2.SyncAttackRoleRs.EXT_FIELD_NUMBER, GamePb2.SyncAttackRoleRs.ext, builder.build());
                    MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
                }
            } catch (Exception e) {
                LogUtil.error("叛军入侵定时器报错, lordId:" + player.lord.getLordId(), e);
            } finally {
                player.setRebelInvadeTime(now);
                player.setPlayerAttackTime(now);
            }
        }
    }

    /**
     * 叛军入侵的战斗逻辑
     *
     * @param battle
     * @param now
     * @param removeBattleIdSet
     */
    public void rebelInvadeFightLogic(Battle battle, int now, Set<Integer> removeBattleIdSet) {
        int combatId = battle.getBattleType();
        StaticCombat staticCombat = StaticCombatDataMgr.getStaticCombat(combatId);

        // 防守者,兵力 添加
        warService.addCityDefendRoleHeros(battle);
        Fighter attacker = fightService.createNpcFighter(staticCombat.getForm()); // npc攻击
        Fighter defender = fightService.createCampBattleDefencer(battle, null);
        FightLogic fightLogic = new FightLogic(attacker, defender, true, battle.getType());
        warDataManager.packForm(fightLogic.getRecordBuild(), attacker.forces, defender.forces);
        fightLogic.start();

        boolean defSuccess = !(fightLogic.getWinState() == FightConstant.FIGHT_RESULT_SUCCESS);

        long defRoleId = battle.getDefencerId();
        Player defPlayer = playerDataManager.getPlayer(defRoleId);

        // 战报信息
        CommonPb.RptAtkPlayer.Builder rpt = CommonPb.RptAtkPlayer.newBuilder();
        rpt.setResult(defSuccess);
        rpt.setAttack(PbHelper.createRptMan(defPlayer.lord.getPos(), "叛军", 0, 0));
        rpt.setDefMan(PbHelper.createRptMan(defPlayer.lord.getPos(), defPlayer.lord.getNick(), defPlayer.lord.getVip(), defPlayer.lord.getLevel(), defPlayer.roleId));
        rpt.setAtkSum(PbHelper.createRptSummary(
                attacker.total,
                attacker.lost,
                Constant.Camp.NPC,
                "叛军",
                -1,
                -1)
        );
        rpt.setDefSum(PbHelper.createRptSummary(
                defender.total,
                defender.lost,
                defPlayer.lord.getCamp(),
                defPlayer.lord.getNick(),
                defPlayer.lord.getPortrait(),
                defPlayer.getDressUp().getCurPortraitFrame())
        );
        for (Force force : defender.forces) {
            CommonPb.RptHero rptHero = fightService.forceToRptHeroNoExp(force);
            if (rptHero != null) {
                rpt.addDefHero(rptHero);
            }
        }
        rpt.addAllAtkHero(attacker.forces.stream()
                .map(force -> PbHelper.createRptHero(Constant.Role.BANDIT, force.killed, 0, force, null, 0, 0, force.totalLost))
                .collect(Collectors.toList())
        );
        CommonPb.Report report = fightRecordDataManager.generateReport(rpt.build(), fightLogic, now);

        List<CommonPb.Award> dropList = null;
        if (!defSuccess) {
            // 防守失败
            dropList = subResAfterFailDefendRebel(defPlayer);
        }

        // 发送邮件
        Turple<Integer, Integer> xy = MapHelper.reducePos(defPlayer.lord.getPos());
        int defX = xy.getA();
        int defY = xy.getB();
        Object[] param = {defPlayer.lord.getNick(), defPlayer.lord.getNick(), defX, defY};
        if (defSuccess) {
            // 防守成功, 没有损失
            mailDataManager.sendReportMail(defPlayer, report, MailConstant.DEFEND_REBEL_INVADE_SUCCESS, null, now,
                    null, param);
        } else {
            Object[] params = Arrays.copyOf(param, param.length + 1);
            params[param.length] = battle.getDefencer().lord.getNick();
            mailDataManager.sendReportMail(defPlayer, report, MailConstant.DEFEND_REBEL_INVADE_FAIL, dropList, now,
                    null, params);
        }
        LogLordHelper.commonLog("rebelInvade", AwardFrom.REBEL_INVADE_DEFEND, defPlayer, defSuccess);
        // 日志记录
        warService.logBattle(battle, fightLogic.getWinState(), attacker, defender, rpt.getDefHeroList(), rpt.getAtkHeroList());
    }

    /**
     * 叛军入侵防守失败后, 扣减资源
     *
     * @param def
     * @return
     */
    private List<CommonPb.Award> subResAfterFailDefendRebel(Player def) {
        List<CommonPb.Award> list = new ArrayList<>();
        // 攻方获得资源公式，人口获得(可掠夺资源量 = 侦查到资源量 = 玩家资源量 - 玩家仓库资源保护量)
        if (def == null) {
            return list;
        }

        long[] proRes = buildingDataManager.getProtectRes(def);

        // 损失资源上限值=ROUNDDOWN(主公LV/10,0)*300000+500000
        int maxLose = def.lord.getLevel() / 10 * 300000 + 500000;

        int defStorehouseLv = BuildingDataManager.getBuildingTopLv(def, BuildingType.STOREHOUSE);
        float lostPro = 0.35f - defStorehouseLv * 0.005f;
        int loseOil = (int) (Math.min((int) Math.ceil(proRes[0] * lostPro), maxLose) * 0.1);
        int loseFood = (int) (Math.min((int) Math.ceil(proRes[1] * lostPro), maxLose) * 0.1);
        int loseEle = (int) (Math.min((int) Math.ceil(proRes[2] * lostPro), maxLose) * 0.1);

        ChangeInfo change = ChangeInfo.newIns();
        if (loseOil > 0) {
            rewardDataManager.subResource(def, AwardType.Resource.OIL, loseOil, AwardFrom.FIGHT_DEF);// , "被掠夺"
            list.add(CommonPb.Award.newBuilder().setType(AwardType.RESOURCE).setId(AwardType.Resource.OIL).setCount(loseOil)
                    .build());
            // 记录更改过的玩家游戏资源类型
            change.addChangeType(AwardType.RESOURCE, AwardType.Resource.OIL);
        }
        if (loseEle > 0) {
            rewardDataManager.subResource(def, AwardType.Resource.ELE, loseEle, AwardFrom.FIGHT_DEF);// , "被掠夺"
            list.add(CommonPb.Award.newBuilder().setType(AwardType.RESOURCE).setId(AwardType.Resource.ELE).setCount(loseEle)
                    .build());
            change.addChangeType(AwardType.RESOURCE, AwardType.Resource.ELE);
        }
        if (loseFood > 0) {
            rewardDataManager.subResource(def, AwardType.Resource.FOOD, loseFood, AwardFrom.FIGHT_DEF);// , "被掠夺"
            list.add(CommonPb.Award.newBuilder().setType(AwardType.RESOURCE).setId(AwardType.Resource.FOOD)
                    .setCount(loseFood).build());
            change.addChangeType(AwardType.RESOURCE, AwardType.Resource.FOOD);
        }
        // 幸福度损失
        List<Integer> happinessLostCoefficient = Constant.HAPPINESS_LOST_COEFFICIENT;
        int loseHappiness = 0;
        if (CheckNull.nonEmpty(happinessLostCoefficient) || happinessLostCoefficient.size() >= 2) {
            int happiness = def.getHappiness();
            loseHappiness = Math.max((int) Math.round(happiness * happinessLostCoefficient.get(0) / Constant.TEN_THROUSAND), happinessLostCoefficient.get(1));
            loseHappiness = Math.min(happiness, loseHappiness);
            loseHappiness = (int) (loseHappiness * 0.1);
            def.subHappiness(loseHappiness);
        }
        // 居民损失
        int residentBottomLimitCoefficient = Constant.RESIDENT_BOTTOM_LIMIT_COEFFICIENT; // 居民数量保底, 总人口低于上限人口的这个万分比, 无法被掠夺
        int loseResident = 0;
        if (def.getResidentTotalCnt() > def.getResidentTopLimit() * (residentBottomLimitCoefficient / Constant.TEN_THROUSAND)) {
            List<Integer> residentLostCoefficient = Constant.RESIDENT_LOST_COEFFICIENT;
            if (CheckNull.nonEmpty(residentLostCoefficient) || residentLostCoefficient.size() >= 2) {
                int idleResidentCnt = def.getIdleResidentCnt();
                loseResident = Math.max((int) Math.round(idleResidentCnt * residentLostCoefficient.get(0) / Constant.TEN_THROUSAND), residentLostCoefficient.get(1));
                loseResident = Math.min(idleResidentCnt, loseResident);
                loseResident = (int) (loseResident * 0.1);
                def.subIdleResidentCnt(loseResident);
            }
        }

        LogUtil.debug(def.roleId + ",叛军入侵失去资源oil=" + loseOil + ",food=" + loseFood + ",ele=" + loseEle +
                ",human=" + def.resource.getHuman() + ",loseHappiness=" + loseHappiness + ",loseResident=" + loseResident);
        // 向客户端同步玩家资源数据
        rewardDataManager.syncRoleResChanged(def, change);
        // 同步领主幸福度和居民变化
        playerDataManager.syncRoleInfo(def);
        return list;
    }

    // 斥候前往大世界地图探索小游戏事件
    public GamePb1.ExploreMiniGameInWorldRs exploreMiniGameInWorld(long roleId) {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        MiniGameScoutState miniGameScoutState = player.getMiniGameScoutState();
        int now = TimeHelper.getCurrentSecond();
        if (miniGameScoutState == null) {
            miniGameScoutState = new MiniGameScoutState();
            miniGameScoutState.setAct(3);
            miniGameScoutState.setLatestRecoveryActTime(now);
            miniGameScoutState.setExploreTime(0);
            player.setMiniGameScoutState(miniGameScoutState);
        }
        if (miniGameScoutState.getAct() <= 0) {
            throw new MwException(GameError.PARAM_ERROR, String.format("斥候前往大世界探索小游戏时, 行动力不足, roleId:%s, scoutAct:%s", roleId, miniGameScoutState.getAct()));
        }

        miniGameScoutState.setAct(miniGameScoutState.getAct() - 1);
        miniGameScoutState.setExploreTime(15);
        GamePb1.ExploreMiniGameInWorldRs.Builder builder = GamePb1.ExploreMiniGameInWorldRs.newBuilder();
        builder.setExploreTime(miniGameScoutState.getExploreTime());
        return builder.build();
    }

    // 斥候体力恢复
    public void recoveryMiniGameScoutActTimeLogic() {
        Iterator<Player> playerIterator = playerDataManager.getAllPlayer().values().iterator();
        while (playerIterator.hasNext()) {
            Player player = playerIterator.next();
            MiniGameScoutState miniGameScoutState = player.getMiniGameScoutState();
            if (miniGameScoutState.getAct() >= 3) {
                continue;
            }
            int now = TimeHelper.getCurrentSecond();
            int period = now - miniGameScoutState.getLatestRecoveryActTime();
            int addAct = period / 4 * 60 * 60;
            if (addAct > 0) {
                miniGameScoutState.setAct(Math.min(miniGameScoutState.getAct() + addAct, 3));
                miniGameScoutState.setLatestRecoveryActTime(miniGameScoutState.getLatestRecoveryActTime() + addAct * 4 * 60 * 60);
                synMiniGameScoutState(player);
            }
        }
    }

    /**
     * 向客户端同步前往大世界探索小游戏的斥候的状态
     *
     * @param player
     */
    public void synMiniGameScoutState(Player player) {
        GamePb1.SynMiniGameScoutStateRs.Builder builder = GamePb1.SynMiniGameScoutStateRs.newBuilder();
        MiniGameScoutState miniGameScoutState = player.getMiniGameScoutState();
        builder.setActPower(miniGameScoutState.getAct());
        builder.setExploreTime(miniGameScoutState.getExploreTime());
        builder.setMiniGameId(miniGameScoutState.getMiniGameId());
        builder.setMiniGamePos(miniGameScoutState.getMiniGamePos());
        if (player.ctx != null) {
            BasePb.Base.Builder msg = PbHelper.createSynBase(GamePb1.SynMiniGameScoutStateRs.EXT_FIELD_NUMBER, GamePb1.SynMiniGameScoutStateRs.ext,
                    builder.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
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
            case "resetHappiness":
                // 重置幸福度
                player.setHappiness(50);
            case "randomBandit":
                // 指定刷新土匪
                randomBanditOnCell(player, Integer.parseInt(params[1]), Integer.parseInt(params[1]), Integer.parseInt(params[1]), Integer.parseInt(params[1]));
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
        Map<Integer, MapCell> mapCellData = player.getMapCellData();
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
        Map<Integer, MapCell> mapCellData = player.getMapCellData();
        List<Integer> foundationData = player.getFoundationData();
        List<StaticHomeCityCell> staticHomeCityCellList = StaticBuildCityDataMgr.getStaticHomeCityCellList();
        for (StaticHomeCityCell sHomeCityCell : staticHomeCityCellList) {
            if (!mapCellData.containsKey(sHomeCityCell.getId())) {
                MapCell mapCell = new MapCell();
                mapCell.setCellId(sHomeCityCell.getId());
                mapCell.setReclaimed(true);
                int banditId = sHomeCityCell.getHasBandit();
                mapCell.setNpcId(banditId);
                List<Integer> simInfo = DataResource.ac.getBean(BuildHomeCityService.class).getBanditTalkSimulator(banditId);
                int simType = 0;
                int simId = 0;
                if (CheckNull.nonEmpty(simInfo) && simInfo.size() >= 2) {
                    simType = simInfo.get(0);
                    simId = simInfo.get(1);
                }
                mapCell.setSimType(banditId > 0 ? simType : 0);
                mapCell.setSimId(banditId > 0 ? simId : 0);
                mapCell.setBanditRefreshTime(-1);
                mapCellData.put(sHomeCityCell.getId(), mapCell);
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

    /**
     * 指定刷新土匪
     *
     * @param player
     * @param cellId
     * @param banditId
     * @param simType
     * @param simId
     */
    public void randomBanditOnCell(Player player, int cellId, int banditId, int simType, int simId) {
        Map<Integer, MapCell> mapCellData = player.getMapCellData();
        MapCell mapCell = mapCellData.get(cellId);
        if (mapCell != null) {
            mapCell.setNpcId(banditId);
            mapCell.setSimType(simType);
            mapCell.setSimId(simId);
            mapCell.setBanditRefreshTime(TimeHelper.getCurrentSecond());
        }
        playerDataManager.syncRoleInfo(player);
    }

}