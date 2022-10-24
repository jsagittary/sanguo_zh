package com.gryphpoem.game.zw.server;

import com.google.protobuf.InvalidProtocolBufferException;
import com.gryphpoem.game.zw.constant.MergeConstant;
import com.gryphpoem.game.zw.constant.MergeUtils;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.datasource.DynamicDataSource;
import com.gryphpoem.game.zw.domain.MasterCacheData;
import com.gryphpoem.game.zw.face.MergeSvrGlobal;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.dao.impl.p.ActivityDao;
import com.gryphpoem.game.zw.resource.dao.impl.p.CampDao;
import com.gryphpoem.game.zw.resource.dao.impl.p.GlobalDao;
import com.gryphpoem.game.zw.resource.domain.p.DbGlobal;
import com.gryphpoem.game.zw.resource.domain.p.DbParty;
import com.gryphpoem.game.zw.resource.domain.p.GlobalActivity;
import com.gryphpoem.game.zw.resource.domain.s.StaticArea;
import com.gryphpoem.game.zw.resource.domain.s.StaticSchedule;
import com.gryphpoem.game.zw.resource.domain.s.StaticScheduleGoal;
import com.gryphpoem.game.zw.resource.pojo.GameGlobal;
import com.gryphpoem.game.zw.resource.pojo.Trophy;
import com.gryphpoem.game.zw.resource.pojo.global.ScheduleGoal;
import com.gryphpoem.game.zw.resource.pojo.global.WorldSchedule;
import com.gryphpoem.game.zw.resource.pojo.party.Camp;
import com.gryphpoem.game.zw.resource.pojo.sandtable.SandTableContest;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @author QiuKun
 * @ClassName GlobalMergeService.java
 * @Description 公共数据的处理
 * @date 2018年9月18日
 */
@Service
public class GlobalMergeService {

    @Autowired
    private GlobalDao globalDao;

    @Autowired
    private CampDao campDao;

    @Autowired
    private ActivityDao activityDao;

    public void saveData(MasterCacheData serverData) {
        int masterServerId = serverData.getMasterServerId();
        DynamicDataSource.DataSourceContextHolder.setDBType(MergeConstant.getDstDatasourceKey(masterServerId));

        globalDao.insertGlobal(serverData.getGameGlobal().ser());
        LogUtil.start("保存军公共据完成  serverId:" + masterServerId);
        for (Camp p : serverData.getPartyMap().values()) {
            campDao.insertParty(p.ser());
        }
        LogUtil.start("保存军团数据完成  serverId:" + masterServerId);

        for (GlobalActivity gAct : serverData.getGlobalActivitieList()) {
            activityDao.insertGlobalActivity(gAct);
        }
        LogUtil.start("保存共享活动数据据完成  serverId:" + masterServerId);
    }

    /**
     * 加载公共数据到内存
     *
     * @param serverData
     * @throws InvalidProtocolBufferException
     */
    public void loadGlobal(MasterCacheData serverData) throws InvalidProtocolBufferException {
        // 切换到主服的数据源
        int masterServerId = serverData.getMasterServerId();
        DynamicDataSource.DataSourceContextHolder.setDBType(MergeConstant.getSrcDatasourceKey(masterServerId));
        // 全服数据
        DbGlobal dbGlobal = globalDao.selectGlobal();
        GameGlobal gameGlobal = new GameGlobal();
        gameGlobal.dser(dbGlobal);
        gameGlobal.setGlobalId(1);
        serverData.setGameGlobal(gameGlobal);

        // 公共的活动 数据
        List<GlobalActivity> globalActivitieList = activityDao.selectGlobalActivity();
        serverData.setGlobalActivitieList(globalActivitieList);
    }

    /**
     * 加载军团数据
     *
     * @param serverData
     */
    public void loadPatry(MasterCacheData serverData) {
        // 切换到主服的数据源
        int masterServerId = serverData.getMasterServerId();
        DynamicDataSource.DataSourceContextHolder.setDBType(MergeConstant.getSrcDatasourceKey(masterServerId));
        List<DbParty> partyList = campDao.selectParty();
        Map<Integer, Camp> partyMap = new HashMap<Integer, Camp>();
        for (DbParty dbParty : partyList) {
            partyMap.put(dbParty.getCamp(), new Camp(dbParty));
        }
        serverData.setPartyMap(partyMap);
    }

    /**
     * 处理公共数据
     *
     * @param serverData
     */
    public void globalDataProcess(MasterCacheData serverData) {
        GameGlobal gameGlobal = serverData.getGameGlobal();
        // 区域数据处理,区域全开
        gameGlobal.getAreaMap().values().forEach(area -> {
            area.getCityFirstKill().clear();
            StaticArea sArea = StaticWorldDataMgr.getAreaMap().get(area.getArea());
            if (sArea != null) {
                if (sArea.getOpenOrder() == WorldConstant.AREA_ORDER_1) {
                    area.setStatus(WorldConstant.AREA_STATUS_OPEN);
                } else {
                    area.setStatus(WorldConstant.AREA_STATUS_PASS);
                }
            }
        });
        // 城池的处理,恢复到初始状态
        gameGlobal.getCityMap().values().forEach(city -> city.mergeClearStateToInit(gameGlobal.getGlobalSchedule().getCurrentScheduleId()));
        // 世界任务的处理,直接到任务9
        worldTaskProcess(serverData, gameGlobal);
        // 全服成就处理
        Trophy trophy = gameGlobal.getTrophy();
        if (trophy != null) {
            trophy.getRankEquips().clear();
        }
        // 流寇 矿点清除
        gameGlobal.getBanditMap().clear();
        gameGlobal.getMineMap().clear();
        gameGlobal.getMineResourceMap().clear();
        gameGlobal.getSuperMineCampMap().clear();
        // 战斗信息清除
        gameGlobal.getBattleMap().clear();
        gameGlobal.getSpecialBattleMap().clear();
        // 闪电战清除
        gameGlobal.getLightningWarBossMap().clear();
        gameGlobal.getSendChatCnt().clear();// 闪电战聊天清除
        // 点兵统领清除
        gameGlobal.getCabinetLeadMap().clear();
        // 盖世太保清除
        gameGlobal.getGestapoMap().clear();
        // 聊天红包清除
        gameGlobal.getPrivateChat().clear();
        gameGlobal.getAreaChat().clear();
        gameGlobal.getCampChat().clear();
        gameGlobal.getActivityChat().clear();
        gameGlobal.getDialogMap().clear();
        gameGlobal.getWorldRoleChat().clear();
        gameGlobal.getRedPacketMap().clear();
        // 柏林会战清除
        gameGlobal.setBerlinWar(null);
        // 荣耀日报清除
        gameGlobal.setHonorDaily(null);
        // 公告活动处理
        List<GlobalActivity> gActList = serverData.getGlobalActivitieList();
        for (GlobalActivity gAct : gActList) {
            // 全军返利清除
            if (gAct.getActivityType() == ActivityConst.ACT_ALL_CHARGE) {
                gAct.setTopupa(0);
                gAct.setTopupb(0);
                gAct.setTopupc(0);
            }
        }
        // 匪军叛乱处理
        gameGlobal.getGlobalRebellion().reset();
        // 清除阵营补给上限
        gameGlobal.cleanMixtureData(GlobalConstant.getCleanList());
        // 清除公共的杂七杂八的数据
        for (Iterator<Entry<String, Map<Integer, Integer>>> it = gameGlobal.getMixtureData().entrySet().iterator(); it.hasNext(); ) {
            Entry<String, Map<Integer, Integer>> en = it.next();
            String key = en.getKey();
            if (!MergeUtils.REATIN_GLOBAL_MIXTURE_DATA_KEY.contains(key)) {
                it.remove();
            }
        }
        gameGlobal.setSandTableContest(new SandTableContest());
        //合服全局数据处理
        DataResource.getBeans(MergeSvrGlobal.class).forEach(bean -> bean.mergeGlobal(gameGlobal));
    }

    /**
     * 世界进程每阶段限时目标处理
     *
     * @param masterCacheData 主服数据
     */
    public void worldTaskScheduleGoalProcess(MasterCacheData masterCacheData) {
        Map<Long, Integer> playersScheduleGoal = masterCacheData.getAllPlayer().values().stream()
                .collect(Collectors.toMap(p -> p.roleId, s -> ScheduleGoal.ALREADY_RECEIVED));
        Map<Integer, WorldSchedule> worldScheduleMap = Optional.ofNullable(
                masterCacheData.getGameGlobal().getGlobalSchedule()
        ).map(e -> e.getScheduleMap()).orElse(null);
        if (worldScheduleMap != null) {
            // 当前进度已结束
            worldScheduleMap.entrySet().stream().filter(e -> e.getValue().getStatus() == ScheduleConstant.SCHEDULE_STATUS_FINISH).forEach(e -> {
                Map<Integer, ScheduleGoal> goal = e.getValue().getGoal();
                if (goal != null) {
                    goal.entrySet().stream().forEach(g -> {
                        Map<Long, Integer> statusMap = g.getValue().getStatusMap();
                        statusMap.clear();
                        statusMap.putAll(playersScheduleGoal);
                    });
                }
            });
        }
    }

    /**
     * 世界任务的 处理
     *
     * @param serverData 主服数据
     * @param gameGlobal 公共数据
     */
    private void worldTaskProcess(MasterCacheData serverData, GameGlobal gameGlobal) {
        Optional.ofNullable(gameGlobal.getGlobalSchedule()).ifPresent(globalSchedule -> {
            int curScheduleId = globalSchedule.getCurrentScheduleId();
            if (curScheduleId > ScheduleConstant.SCHEDULE_BOOS_2_ID) {
                globalSchedule.getScheduleMap()
                        .entrySet()
                        .stream()
                        .filter(en -> en.getKey() > ScheduleConstant.SCHEDULE_BOOS_2_ID)
                        .map(Entry::getValue)
                        .forEach(worldSchedule -> {
                            StaticSchedule staticSchedule = StaticWorldDataMgr.getScheduleById(worldSchedule.getId());
                            // 初始化限时目标
                            if (!CheckNull.isEmpty(staticSchedule.getGoal())) {
                                Map<Integer, ScheduleGoal> goal = new HashMap<>(10);
                                for (int goalId : staticSchedule.getGoal()) {
                                    StaticScheduleGoal sScheduleGoal = StaticWorldDataMgr.getScheduleGoalById(goalId);
                                    if (!CheckNull.isNull(sScheduleGoal)) {
                                        // 初始化默认的目标值
                                        goal.put(goalId, new ScheduleGoal(goalId));
                                        // 更新限时目标的完成
                                        int condId = sScheduleGoal.getCond();
                                        if (condId == ScheduleConstant.GOAL_COND_HERO_DECORATED) {
                                            int lv = sScheduleGoal.getCondId();
                                            int sum = serverData.getAllPlayer().values().stream().mapToInt(p -> (int) p.heros.values().stream().filter(hero -> hero.getDecorated() >= lv).count()).sum();
                                            if (sum > 0) {
                                                worldSchedule.getStatusCnt().clear();
                                                worldSchedule.updateCondStatus(condId, sum);
                                            }
                                        }
                                    }
                                }
                                worldSchedule.setGoal(goal);
                            }
                        });

            }
        });
        // WorldTask wTask = gameGlobal.getWorldTask();
        // for (int i = 2; i <= TaskType.WORLD_BOSS_TASK_ID_2; i++) {
        // StaticWorldTask e = StaticWorldDataMgr.getWorldTaskMap().get(i);
        // if (e == null) {
        // continue;
        // }
        // wTask.getWorldTaskId().set(e.getTaskId());
        // wTask.getWorldTaskMap().put(e.getTaskId(),
        // CommonPb.WorldTask.newBuilder().setTaskId(e.getTaskId()).setTaskCnt(1).setCamp(1).setHp(0).build());
        // StaticWorldTask worldTask =
        // StaticWorldDataMgr.getWorldTask(wTask.getWorldTaskId().get() + 1);
        // if (worldTask != null) {
        // // 新开启
        // int newWorldTaskId = wTask.getWorldTaskId().incrementAndGet();
        // wTask.getWorldTaskMap().put(newWorldTaskId,
        // CommonPb.WorldTask.newBuilder().setTaskId(newWorldTaskId).setHp(worldTask.getHp()).build());
        // if (worldTask.getTaskId() == TaskType.WORLD_BOSS_TASK_ID_1
        // || worldTask.getTaskId() == TaskType.WORLD_BOSS_TASK_ID_2) {
        // wTask.setDefender(null);
        // }
        // }
        // }
        // 新进度
        //gameGlobal.setGlobalSchedule(new GlobalSchedule(ScheduleConstant.SCHEDULE_MAX_ID));

    }

    /**
     * 军团数据进行处理
     *
     * @param serverData
     */
    public void partyDataProcess(MasterCacheData serverData) {
        for (Entry<Integer, Camp> kv : serverData.getPartyMap().entrySet()) {
            Camp camp = kv.getValue();
            int endTime = TimeHelper.getSomeDayAfter(1, 12, 0, 0);
            camp.setEndTime(endTime);
            camp.setStatus(PartyConstant.PARTY_STATUS_ELECT_END);
            camp.setSlogan("");
            camp.setAuthor("");
            // 每日刷新的次数
            camp.setBuild(0);
            camp.setCityBattle(0);
            camp.setCampBattle(0);
            camp.setRefreshTime(TimeHelper.getCurrentSecond());
            // 排行清空
            camp.getCityRank().clear();
            camp.getCampRank().clear();
            camp.getBuildRank().clear();
            // 军团现任官员列表清空
            camp.getOfficials().clear();
            camp.getLog().clear();// 军团日志清空
            camp.getElectionList().clear(); // 军团选举排行信息清空
            // 军团补给清除
            camp.getPartySupplies().clear();
            camp.setSandTableWin(0);
            camp.setSandTableWinMax(0);
        }
    }

}
