package com.gryphpoem.game.zw.service.activity.task;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.Java8Utils;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticActTaskDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.p.ActivityTask;
import com.gryphpoem.game.zw.resource.domain.s.StaticActTreasureWareJourney;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.util.ActTaskUtil;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.service.GmCmd;
import com.gryphpoem.game.zw.service.PersonalAct;
import com.gryphpoem.game.zw.service.activity.PersonalActService;
import com.gryphpoem.game.zw.service.activity.task.abs.AbsTaskActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@PersonalAct(actTypes = {ActivityConst.ACT_TREASURE_WARE_JOURNEY})
public class TreasureWareTaskActService extends AbsTaskActivityService implements PersonalActService {
    private static final int taskIndex = 0;

    @Autowired
    private PlayerDataManager playerDataManager;

    /**
     * 获得活动任务信息
     *
     * @param roleId
     * @param req
     * @return
     */
    public GamePb5.GetActTwJourneyRs getActTwJourneyInfo(long roleId, GamePb5.GetActTwJourneyRq req) {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int actType = req.getActivityType();
        List<ActivityTask> taskList = getTaskList(player, player.getPersonalActs().getKeyId(actType), actType, false, false, taskIndex, getFunctionId(actType));
        if (CheckNull.isEmpty(taskList))
            return GamePb5.GetActTwJourneyRs.newBuilder().build();

        GamePb5.GetActTwJourneyRs.Builder builder = GamePb5.GetActTwJourneyRs.newBuilder();
        taskList.forEach(task -> {
            if (CheckNull.isNull(task)) return;
            builder.addActTask(PbHelper.createActTaskPb(task));
        });
        return builder.build();
    }

    /**
     * 领取任务奖励
     *
     * @param roleId
     * @param req
     * @return
     */
    public GamePb5.ReceiveActTwJourneyAwardRs receiveActTwJourneyAward(long roleId, GamePb5.ReceiveActTwJourneyAwardRq req) {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int actType = req.getActType();
        int confId = req.getConfId();


        List<ActivityTask> taskList = getTaskList(player, player.getPersonalActs().getKeyId(actType), actType, false, true, taskIndex, getFunctionId(actType));
        Integer actId = player.getPersonalActs().getKeyId(actType);
        ActivityBase ab = PersonalActService.getActivityBase(actId, actType);
        if (CheckNull.isEmpty(taskList) || CheckNull.isNull(actId) || CheckNull.isNull(ab)) {
            throw new MwException(GameError.NO_CONFIG.getCode(),
                    "未找到任何任务配置, roleId:,", roleId, ", taskList.size: ",
                    CheckNull.isEmpty(taskList) ? 0 : taskList.size(), ", actId: ", actId, ", actType: ", actType);
        }

        StaticActTreasureWareJourney staticJourney = StaticActTaskDataMgr.getActTwJourney(ab.getActivityId(), confId);
        if (CheckNull.isNull(staticJourney)) {
            throw new MwException(GameError.NO_CONFIG.getCode(),
                    "未找到任何任务配置, roleId:,", roleId, ", actId: ", actId, ", confId: ", confId, ", actType: ", actType);
        }

        ActivityTask activityTask = taskList.stream().filter(task -> Objects.nonNull(task) && task.getUid() == confId).findFirst().orElse(null);
        if (CheckNull.isNull(activityTask) || activityTask.getCount() <= 0) {
            throw new MwException(GameError.ACTIVITY_NOT_FINISH.getCode(), "活动未完成, roleId:,", roleId);
        }
        //已经领取过奖励
        if (activityTask.getDrawCount() > 0) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId: %d, 任务uid: %d, 已经领取过奖励", player.getLordId(), req.getConfId()));
        }

        // 检测背包是否已满
        rewardDataManager.checkBag(player, staticJourney.getAwardList());
        GamePb5.ReceiveActTwJourneyAwardRs.Builder builder = GamePb5.ReceiveActTwJourneyAwardRs.newBuilder();
        // 获取活动翻倍
        for (List<Integer> e : staticJourney.getAwardList()) {
            int type = e.get(0);
            int itemId = e.get(1);
            int count = e.get(2);
            if (type == AwardType.EQUIP) {
                for (int c = 0; c < count; c++) {
                    int itemKey = rewardDataManager.addAward(player, type, itemId, 1, AwardFrom.RECEIVE_DAY_TREASURE_WARE_TASK_AWARD,
                            DataResource.ac.getBean(ServerSetting.class).getServerID(), ab.getPlanKeyId(), req.getConfId());
                    builder.addAward(PbHelper.createAwardPb(type, itemId, 1, itemKey));
                }
            } else {
                int itemKey = rewardDataManager.addAward(player, type, itemId, count, AwardFrom.RECEIVE_DAY_TREASURE_WARE_TASK_AWARD,
                        DataResource.ac.getBean(ServerSetting.class).getServerID(), ab.getPlanKeyId(), req.getConfId());
                builder.addAward(PbHelper.createAwardPb(type, itemId, count, itemKey));
            }
        }

        activityTask.setDrawCount(activityTask.getDrawCount() + 1);
        builder.setActTask(PbHelper.createActTaskPb(activityTask));
        builder.setIsAllGainActivity(checkAllGainActivity(taskList));
        return builder.build();
    }

    @Override
    public boolean isAllGainActivity(Player player, ActivityBase actBase, Activity activity) {
        if (CheckNull.isNull(player) || CheckNull.isNull(actBase) || CheckNull.isNull(activity) || actBase.getStep0() == ActivityConst.OPEN_CLOSE)
            return true;

        return checkAllGainActivity(getTaskList(player, player.getPersonalActs().getKeyId(actBase.getActivityType()),
                actBase.getActivityType(), false, false, taskIndex, getFunctionId(actBase.getActivityType())));
    }

    private boolean checkAllGainActivity(List<ActivityTask> activityTasks) {
        if (CheckNull.isEmpty(activityTasks))
            return true;
        for (ActivityTask activityTask : activityTasks) {
            if (CheckNull.isNull(activityTask))
                continue;
            if (activityTask.getDrawCount() <= 0)
                return false;
        }
        return true;
    }


    @Override
    public void process(Player player, ETask eTask, int... params) {
        try {
            Integer actId;
            ActivityBase ab;
            int[] types = getActivityType();
            for (int actType : types) {
                List<ActivityTask> taskMap = getTaskList(player, player.getPersonalActs().getKeyId(actType),
                        actType, false, false, taskIndex, getFunctionId(actType));
                actId = player.getPersonalActs().getKeyId(actType);
                ab = PersonalActService.getActivityBase(actId, actType);
                if (CheckNull.isEmpty(taskMap) || CheckNull.isNull(actId) || CheckNull.isNull(ab)) {
                    LogUtil.common(String.format("player not own any task! roleId: %d, actType: %d, serverId: %d, actId: %s",
                            player.lord.getLordId(), actType, player.account.getServerId(), actId));
                    continue;
                }

                //当前任务ID且未完成的任务列表
                boolean functionUnlock = StaticFunctionDataMgr.funcitonIsOpen(player, getFunctionId(actType));
                Integer finalActivityId = ab.getActivityId();
                List<ActivityTask> unFinishTaskList = taskMap.stream()
                        .filter(t -> t.getTaskId() == eTask.getTaskType())
                        .filter(t -> t.getCount() == 0)
                        .filter(t -> {
                            StaticActTreasureWareJourney staticData = StaticActTaskDataMgr.getActTwJourney(finalActivityId, t.getUid());
                            if (CheckNull.isNull(staticData)) {
                                return false;
                            }
                            if (!staticData.isFromNow())
                                return true;
                            return functionUnlock;
                        })
                        .collect(Collectors.toList());
                if (CheckNull.isEmpty(unFinishTaskList)) continue;
                for (ActivityTask actTask : unFinishTaskList) {
                    StaticActTreasureWareJourney staticData = StaticActTaskDataMgr.getActTwJourney(finalActivityId, actTask.getUid());
                    if (Objects.isNull(staticData) || CheckNull.isEmpty(staticData.getParams())) {
                        continue;
                    }
                    List<Integer> cfgParams = staticData.getParams();
                    int needCount = CheckNull.nonEmpty(cfgParams) ? cfgParams.get(cfgParams.size() - 1) : 0;
                    boolean isChange = ActTaskUtil.updTaskSchedule(player, actTask, eTask, cfgParams, params);
                    if (isChange) {
                        LogUtil.debug(String.format("roleId: %d, 宝具征程活动任务ID: %d, 任务类型: %d, 进度发生变化当前进度: (%d/%d)",
                                player.getLordId(), actTask.getUid(), actTask.getTaskId(), actTask.getProgress(), needCount));
                        if (ActTaskUtil.checkTaskFinished(player, actTask, eTask, staticData.getParams())) {
                            actTask.setCount(1);
                            //如果任务已经完成通知客户端
                            activityDataManager.syncActChange(player, actType, 1);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.error(String.format("roleId: %d, taskTy: %d, params: %s", player.getLordId(),
                    eTask.getTaskType(), Arrays.toString(params)), e);
        }
    }


    @Override
    protected int[] getActivityType() {
        return new int[]{ActivityConst.ACT_TREASURE_WARE_JOURNEY};
    }

    @GmCmd("twTaskAct")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {
        String cmd = params[0];
        if ("clear".equalsIgnoreCase(cmd)) {
            for (int actType : getActivityType()) {
                Activity activity = player.activitys.get(actType);
                if (CheckNull.isNull(activity))
                    continue;
                activity.getDayTasks().clear();
            }
        }
        if ("finishAll".equalsIgnoreCase(cmd)) {
            for (int actType : getActivityType()) {
                List<ActivityTask> taskList = getTaskList(player, player.getPersonalActs().getKeyId(actType), actType, false, false, taskIndex, getFunctionId(actType));
                if (CheckNull.isNull(taskList))
                    return;
                taskList.forEach(pTask -> {
                    pTask.setCount(1);
                    pTask.setProgress(100000000);
                });
            }
        }
        if ("fix".equalsIgnoreCase(cmd)) {
            Java8Utils.syncMethodInvoke(() -> fixActTaskData());
        }
    }

    @Override
    public void initActData(Player player, int actType) {
        getTaskList(player, player.getPersonalActs().getKeyId(actType), actType, false, false, taskIndex, getFunctionId(actType));
    }

    @Override
    public void checkActivityTaskData(int actId, int actType, long lordId, List<ActivityTask> taskList) {
        Player player = playerDataManager.getPlayer(lordId);
        Collection<StaticActTreasureWareJourney> journeys = StaticActTaskDataMgr.getActTwJourneyList(actId);
        if (CheckNull.isEmpty(journeys)) {
            LogUtil.error(String.format("actId: %d, staticTaskList is empty, actType: %d", actId, actType));
            return;
        }

        Map<Integer, ActivityTask> taskMap = null;
        if (CheckNull.nonEmpty(taskList)) {
            taskMap = new HashMap<>();
            ActivityTask activityTask;
            StaticActTreasureWareJourney staticData;
            Iterator<ActivityTask> it = taskList.iterator();
            while (it.hasNext()) {
                activityTask = it.next();
                if (CheckNull.isNull(activityTask))
                    continue;
                staticData = StaticActTaskDataMgr.getActTwJourney(actId, activityTask.getUid());
                if (CheckNull.isNull(staticData))
                    it.remove();
                taskMap.put(activityTask.getUid(), activityTask);
            }
        }

        for (StaticActTreasureWareJourney data : journeys) {
            if (CheckNull.isNull(data))
                continue;
            if (CheckNull.nonEmpty(taskMap) && taskMap.containsKey(data.getKeyId()))
                continue;
            ActivityTask activityTask = new ActivityTask(data.getTaskType());
            activityTask.setTaskId(data.getTaskType());
            activityTask.setUid(data.getKeyId());
            taskList.add(activityTask);
            ETask eTask = ETask.getByType(activityTask.getTaskId());
            if (!data.isFromNow() && Objects.nonNull(player) && Objects.nonNull(eTask)) {
                boolean isChange = ActTaskUtil.updTaskSchedule(player, activityTask, eTask, data.getParams());
                if (isChange) {
                    if (ActTaskUtil.checkTaskFinished(player, activityTask, eTask, data.getParams())) {
                        activityTask.setCount(1);
                    }
                }
            }
        }
    }

    private int getFunctionId(int actType) {
        switch (actType) {
            case ActivityConst.ACT_TREASURE_WARE_JOURNEY:
                return FunctionConstant.ACT_TREASURE_WARE_JOURNEY;
            default:
                return 0;
        }
    }

    public void fixActTaskData() {
        Collection<Player> players = playerDataManager.getAllPlayer().values();
        if (CheckNull.isEmpty(players))
            return;

        players.forEach(player -> {
            Activity activity = player.activitys.get(ActivityConst.ACT_TREASURE_WARE_JOURNEY);
            if (CheckNull.isNull(activity)) {
                LogUtil.error("activity not found, roleId: ", player.lord.getLordId());
                return;
            }
            Collection<StaticActTreasureWareJourney> configTaskList = StaticActTaskDataMgr.getActTwJourneyList(activity.getActivityId());
            if (CheckNull.isEmpty(configTaskList)) {
                LogUtil.error("config task list is empty, lord: ", player.lord.getLordId());
                return;
            }
            List<StaticActTreasureWareJourney> combatConfigList = configTaskList.stream().filter(configTask ->
                    configTask.getTaskType() == ETask.PASS_TREASURE_WARE_COMBAT_ID.getTaskType()).collect(Collectors.toList());
            if (CheckNull.isEmpty(combatConfigList)) {
                LogUtil.error("config task list is empty, lord: ", player.lord.getLordId());
                return;
            }
            List<ActivityTask> taskList = activity.getDayTasks().get(taskIndex);
            if (CheckNull.isEmpty(taskList))
                return;
            Map<Integer, ActivityTask> combatTaskMap = taskList.stream().filter(task -> Objects.nonNull(task) && task.getTaskId() ==
                    ETask.PASS_TREASURE_WARE_COMBAT_ID.getTaskType() && task.getDrawCount() <= 0 && task.getCount() <= 0).collect(Collectors.toMap(ActivityTask::getUid, Function.identity()));
            if (CheckNull.isEmpty(combatTaskMap))
                return;
            int curCombatId = player.getTreasureCombat().getCurCombatId();
            combatConfigList.forEach(combatData -> {
                if (!combatTaskMap.containsKey(combatData.getKeyId()))
                    return;
                if (curCombatId >= combatData.getParams().get(0)) {
                    combatTaskMap.get(combatData.getKeyId()).setCount(1);
                    combatTaskMap.get(combatData.getKeyId()).setDrawCount(1);
                    combatTaskMap.get(combatData.getKeyId()).setProgress(1);
                }
            });
        });
    }
}