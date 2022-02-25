package com.gryphpoem.game.zw.service;

import com.alibaba.fastjson.JSON;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticPropDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticTaskDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.CommonPb.Mill;
import com.gryphpoem.game.zw.pb.CommonPb.RptHero;
import com.gryphpoem.game.zw.pb.GamePb1.SynGainResRs;
import com.gryphpoem.game.zw.pb.GamePb3.*;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.constant.task.TaskCategory;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.p.CampMember;
import com.gryphpoem.game.zw.resource.domain.p.Sectiontask;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.Prop;
import com.gryphpoem.game.zw.resource.pojo.Task;
import com.gryphpoem.game.zw.resource.pojo.WorldTask;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.party.Camp;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.resource.util.eventdata.EventDataUp;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.session.SeasonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务：包括主线支线任务，世界任务，军团任务，攻打世界boss
 */
@Service
public class TaskService {
    @Autowired
    private SeasonService seasonService;
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private BuildingDataManager buildingDataManager;
    @Autowired
    private CampDataManager campDataManager;
    @Autowired
    private WorldDataManager worldDataManager;
    @Autowired
    private TaskDataManager taskDataManager;
    @Autowired
    private BuildingService buildingService;
    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private PropService propService;
    @Autowired
    private ChatDataManager chatDataManager;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private BattlePassDataManager battlePassDataManager;
    @Autowired
    private TitleService titleService;

    /**
     * 获取主支线,剧情任务
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetMajorTaskRs getMajorTaskRq(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        // 将主支线任务添加到玩家身上
        Map<Integer, Task> majorTasks = player.majorTasks;
        if (majorTasks.isEmpty()) {
            // List<StaticTask> list = StaticTaskDataMgr.getInitMajorTask();
            // if (!CheckNull.isEmpty(list)) {
            // for (StaticTask e : list) {
            // Task task = new Task(e.getTaskId());
            // majorTasks.put(e.getTaskId(), task);
            // }
            // }
            for (StaticTask e : StaticTaskDataMgr.getOpenList()) {
                Task task = new Task(e.getTaskId());
                majorTasks.put(e.getTaskId(), task);
                LogUtil.debug("初始化任务=" + task);
            }
        }
        GetMajorTaskRs.Builder builder = GetMajorTaskRs.newBuilder();
        // 当前显示的任务
        if (CheckNull.isEmpty(player.curMajorTaskIds)) {
            List<Integer> curTask = taskDataManager.getCurTask(player);
            player.curMajorTaskIds.clear();
            player.curMajorTaskIds.addAll(curTask);
        }
        for (Task task : majorTasks.values()) {
            int taskId = task.getTaskId();
            // 检测剧情任务的触发
            // checkTriggerSectionTask(task, player);
            StaticTask stask = StaticTaskDataMgr.getTaskById(taskId);
            if (stask == null) {
                continue;
            }
            // 过滤主支线已领取
            if (task.getStatus() == TaskType.TYPE_STATUS_REWARD
                    && (stask.getType() == TaskType.TYPE_MAIN || stask.getType() == TaskType.TYPE_SUB)) {
                continue;
            }
            if (task.getStatus() == TaskType.TYPE_STATUS_REWARD && stask.getType() == TaskType.TYPE_SECTION) {
                builder.addTask(PbHelper.createTaskPb(task));
                continue;
            }
            taskDataManager.currentMajorTask(player, task, stask);
            if (task.getSchedule() >= stask.getSchedule() && task.getStatus() == TaskType.TYPE_STATUS_UNFINISH) {
                task.setStatus(TaskType.TYPE_STATUS_FINISH);
            }
            if (task.getSchedule() < stask.getSchedule()) {
                task.setStatus(TaskType.TYPE_STATUS_UNFINISH);
            }
            builder.addTask(PbHelper.createTaskPb(task));
        }
        if (showCurMajorTask(player)) {
            builder.addAllCurTaskId(player.curMajorTaskIds);
            LogUtil.debug("当前需要显示的任务id: ", player.curMajorTaskIds, " , roleId:", roleId);
        }
        // 剧情章节
        Sectiontask sectiontask = getCurSectionAndCheckStatus(player);
        if (sectiontask != null) {
            if (sectiontask.getStatus() != TaskType.TYPE_STATUS_REWARD) {
                builder.setCurSection(sectiontask.ser());
            } else {
                // 已经领取说明都做完了,检测是否有新的剧情
                StaticSectiontask nextSSection = StaticTaskDataMgr.getSectiontaskById(sectiontask.getSectionId() + 1);
                if (nextSSection != null) {
                    Sectiontask nextSection = new Sectiontask(nextSSection.getSectionId(),
                            TaskType.TYPE_STATUS_UNFINISH);
                    player.sectiontask.add(nextSection);
                    builder.setCurSection(nextSection.ser());

                    List<Integer> newTasks = nextSSection.getSectionTask();
                    // 触发章节任务
                    for (Integer taskId : newTasks) {
                        StaticTask sTask = StaticTaskDataMgr.getTaskById(taskId);
                        if (StaticTaskDataMgr.getTaskById(taskId) != null) {
                            if (!player.majorTasks.containsKey(taskId)) {
                                Task task = new Task(taskId);
                                player.majorTasks.put(taskId, task);
                                taskDataManager.currentMajorTask(player, task, sTask);
                                builder.addTask(PbHelper.createTaskPb(task));
                                LogUtil.debug("触发了下个剧情章节的任务  roleId:", player.roleId, " task:", task);
                            } else {
                                Task oldTask = player.majorTasks.get(taskId);
                                builder.addTask(PbHelper.createTaskPb(oldTask));
                                LogUtil.debug("触发了下个剧情章节的任务中有老数据  roleId:", player.roleId, " oldTask:", oldTask);
                            }
                        } else {
                            LogUtil.error("剧情章节任务配置出错  roleId:", player.roleId, " taskId:", taskId, ", nextSSection:",
                                    nextSSection.getSectionId());
                        }
                    }
                    LogUtil.debug("有新的章节加入 roleId:", player.roleId, ", newSection:", nextSSection);
                }
            }
        }
        return builder.build();
    }

    /**
     * 检测剧情任务的触发
     *
     * @param task   任务
     * @param player 玩家
     */
    public void checkTriggerSectionTask(Task task, Player player) {
        if (task.getStatus() == TaskType.TYPE_STATUS_REWARD) {
            if (task.getTaskId() == Constant.SECTIONTASK_BEGIN_HERO_ID.get(0)) {
                // 触发第一章剧情任务
                taskDataManager.triggerSectiontask(player);
            } else if (task.getTaskId() == Constant.SECTIONTASK_BEGIN_HERO_ID.get(1)) {
                // 触发第十一章剧情任务
                taskDataManager.triggerSectiontask2(player);
            }
        }
    }


    /**
     * 是否可以显示当前任务
     *
     * @param player
     * @return true 可以显示
     */
    private boolean showCurMajorTask(Player player) {
        // if (CheckNull.isEmpty(player.sectiontask)) {
        // return false;
        // }
        // Sectiontask section = player.sectiontask.stream()
        // .filter(s -> s.getSectionId() == Constant.SHOW_CURTASK_TRIGGET_ID).findFirst().orElse(null);
        // return section != null && section.getStatus() == TaskType.TYPE_STATUS_REWARD;

        return true;
    }

    /**
     * 获取当前剧情章节并检测状态
     *
     * @param player
     * @return null 说明还没激活剧情任务
     */
    private Sectiontask getCurSectionAndCheckStatus(Player player) {
        if (CheckNull.isEmpty(player.sectiontask)) {
            return null;
        }
        Sectiontask section = player.sectiontask.get(player.sectiontask.size() - 1);
        if (section.getStatus() == TaskType.TYPE_STATUS_UNFINISH) {
            // 检测状态
            StaticSectiontask sSection = StaticTaskDataMgr.getSectiontaskById(section.getSectionId());
            if (sSection != null && checkSectionStatus(player, sSection)) {
                section.setStatus(TaskType.TYPE_STATUS_FINISH);
            }
        }
        return section;
    }

    /**
     * 检测章节任务是否可以领取奖励,章节内所有任务都是领取状态即可领取
     *
     * @param player
     * @param sSection
     * @return true可领取奖励
     */
    private boolean checkSectionStatus(Player player, StaticSectiontask sSection) {
        List<Integer> taskIds = sSection.getSectionTask();
        for (Integer id : taskIds) {
            Task task = player.majorTasks.get(id);
            if (task == null) {
                return false;
            }
            // 在这里进行任务状态的刷新
            StaticTask stask = StaticTaskDataMgr.getTaskById(id);
            taskDataManager.currentMajorTask(player, task, stask);
            if (task.getSchedule() >= stask.getSchedule() && task.getStatus() == TaskType.TYPE_STATUS_UNFINISH) {
                task.setStatus(TaskType.TYPE_STATUS_FINISH);
            }
            if (task.getSchedule() < stask.getSchedule()) {
                task.setStatus(TaskType.TYPE_STATUS_UNFINISH);
            }
            if (task.getStatus() == TaskType.TYPE_STATUS_UNFINISH) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取主支线,剧情任务 领奖
     *
     * @param roleId
     * @param taskId
     * @return
     * @throws MwException
     */
    public TaskAwardRs taskAwardRq(long roleId, int taskId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        StaticTask staticTask = StaticTaskDataMgr.getTaskById(taskId);
        if (staticTask == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(),
                    "领取任务奖励时,配置找不到, roleId:" + roleId + ",taskId=" + taskId);
        }
        int finishValue = staticTask.getSchedule();

        Task task = player.majorTasks.get(taskId);
        if (task == null) {
            throw new MwException(GameError.NO_TASK.getCode(), "领取任务奖励时,无此任务, roleId:" + roleId + ",taskId=" + taskId);
        }
        if (task.getStatus() == TaskType.TYPE_STATUS_REWARD) {
            throw new MwException(GameError.REWARD_GAIN.getCode(), "领取任务奖励时,重复领取, roleId:" + roleId);
        }

        taskDataManager.currentMajorTask(player, task, staticTask);
        if (task.getSchedule() < finishValue) { // && staticTask.getCond() != TaskType.COND_OTHER_TASK_CNT
            throw new MwException(GameError.TASK_NO_FINISH.getCode(),
                    "领取任务奖励时,任务未完成, roleId:" + roleId + ",taskId=" + taskId);
        }

        // task增加日志记录 (基本信息、战力、关卡类型、关卡id)
        LogLordHelper.commonLog("task", AwardFrom.COMMON, player, player.lord.getFight(), staticTask.getType(),
                staticTask.getTaskId());

        int now = TimeHelper.getCurrentSecond();
        // 救援爱丽丝任务
        if (task.getTaskId() == Constant.ALICE_RESCUE_MISSION_TASK.get(0)) {
            player.setMixtureData(PlayerConstant.ALICE_TRIGGER_TIME, now);
            player.setMixtureData(PlayerConstant.ALICE_AWARD_TIME, (int) (TimeHelper.getSomeDayAfterOrBerfore(new Date(), 0, 23, 59, 59).getTime() / 1000L));
            // 同步玩家扩展数据
            playerDataManager.syncMixtureData(player);
        }
        // 蝎王任务
        if (task.getTaskId() == Constant.CLEAR_SCORPION_ACTIVATE_END_TIME_TASK) {
            player.setMixtureData(PlayerConstant.SCORPION_ACTIVATE_END_TIME, now);
        }
        // 支线任务
        if (staticTask.getType() == TaskType.TYPE_SUB) {
            taskDataManager.updTask(player, TaskType.COND_OTHER_TASK_CNT, 1);
        }
        // 世界开启
        if (taskId == Constant.ALLOC_POS_CONDITION) {
            worldDataManager.openPos(player);
        }
        task.setStatus(TaskType.TYPE_STATUS_REWARD);
        TaskAwardRs.Builder builder = TaskAwardRs.newBuilder();
        List<List<Integer>> awardList = staticTask.getAwardList();
        // 获取活动翻倍
        int num = activityDataManager.getActDoubleNum(player);
        if (awardList != null && !awardList.isEmpty() && staticTask.getIsGet() > 0) {
            for (List<Integer> ee : awardList) {
                int type = ee.get(0);
                int id = ee.get(1);
                int count = ee.get(2);
                count *= num;
                int keyId = rewardDataManager.addAward(player, type, id, count, AwardFrom.TASK_DAYIY_AWARD);
                builder.addAward(PbHelper.createAwardPb(type, id, count, keyId));
            }
        }
        LogLordHelper.commonLog("taskAward", AwardFrom.TASK_DAYIY_AWARD, player, taskId);
        // 触发剧情任务
        checkTriggerSectionTask(task, player);

        // 触发下一个任务
        // if (staticTask.getType() == TaskType.TYPE_MAIN) {
        // player.majorTasks.remove(taskId); 完成后不做删除， 方便查询任务解锁
        List<StaticTask> triggerList = StaticTaskDataMgr.getTriggerTask(taskId);
        if (triggerList != null) {
            for (StaticTask ee : triggerList) {
                Task etask = player.majorTasks.get(ee.getTaskId());
                processTask(player, ee.getTaskId());
                if (etask != null) {
                    taskDataManager.currentMajorTask(player, etask, ee);
                    builder.addTask(PbHelper.createTaskPb(etask));
                    LogUtil.debug("触发下一个成就任务=" + etask);
                    continue;
                }
                etask = new Task(ee.getTaskId());
                player.majorTasks.put(ee.getTaskId(), etask);
                taskDataManager.currentMajorTask(player, etask, ee);
                builder.addTask(PbHelper.createTaskPb(etask));
            }
        }
        // 解锁资源建筑
        buildingDataManager.refreshSourceData(player);
        // 计算当前应该需要显示的任务
        if (showCurMajorTask(player)) {
            List<Integer> curTask = taskDataManager.getCurTask(player);
            player.curMajorTaskIds.clear();
            player.curMajorTaskIds.addAll(curTask);
            LogUtil.debug("当前需要显示的任务id: ", player.curMajorTaskIds, " , roleId:", roleId);
            builder.addAllCurTaskId(player.curMajorTaskIds);
        }
        buildingService.addAtuoBuild(player);// 触发自动建造
        // }

        // 特殊,商用建造队列赠送
        if (StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_BUILD_GIFT)) {
            Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_BUILD_GIFT);
            if (!CheckNull.isNull(activity) && activityDataManager.currentActivity(player, activity, 0, now) == 0) {
                activityDataManager.updActivity(player, ActivityConst.ACT_BUILD_GIFT, 1, 0, true);
            }
        }
        // StaticFunctionOpen sOpen = StaticFunctionDataMgr.getOpenById(FunctionConstant.FUNC_BUILD_GIFT);
        // if (!CheckNull.isNull(sOpen) && sOpen.getTaskId() == taskId) {
        //
        //
        // }
        // 剧情章节
        Sectiontask sectiontask = getCurSectionAndCheckStatus(player);
        if (sectiontask != null) {
            StaticSectiontask sectiontask1 = null;
            if (sectiontask.getStatus() != TaskType.TYPE_STATUS_REWARD) {
                sectiontask1 = StaticTaskDataMgr.getSectiontaskById(sectiontask.getSectionId());
                builder.setCurSection(sectiontask.ser());
            } else {
                // 已经领取说明都做完了,检测是否有新的剧情
                sectiontask1 = StaticTaskDataMgr.getSectiontaskById(sectiontask.getSectionId() + 1);
                if (sectiontask1 != null) {
                    Sectiontask nextSection = new Sectiontask(sectiontask1.getSectionId(),
                            TaskType.TYPE_STATUS_UNFINISH);
                    player.sectiontask.add(nextSection);
                    builder.setCurSection(nextSection.ser());
                    LogUtil.debug("有新的章节加入 roleId:", player.roleId, ", newSection:", sectiontask1);
                }
            }
            if (!CheckNull.isNull(sectiontask1)) {
                for (Integer tId : sectiontask1.getSectionTask()) {
                    StaticTask sTask = StaticTaskDataMgr.getTaskById(tId);
                    if (StaticTaskDataMgr.getTaskById(tId) != null) {
                        if (!player.majorTasks.containsKey(tId)) {
                            Task taskT = new Task(tId);
                            player.majorTasks.put(tId, taskT);
                            taskDataManager.currentMajorTask(player, taskT, sTask);
                            builder.addTask(PbHelper.createTaskPb(taskT));
                            LogUtil.debug("触发了下个剧情章节的任务  roleId:", player.roleId, " task:", taskT);
                        } else {
                            Task oldTask = player.majorTasks.get(tId);
                            builder.addTask(PbHelper.createTaskPb(oldTask));
                            LogUtil.debug("触发了下个剧情章节的任务中有老数据  roleId:", player.roleId, " oldTask:", oldTask);
                        }
                    } else {
                        LogUtil.error("剧情章节任务配置出错  roleId:", player.roleId, " taskId:", tId, ", nextSSection:",
                                sectiontask1.getSectionId());
                    }
                }
            }

        }

        return builder.build();
    }

    /**
     * 给N次征收
     *
     * @param player
     * @param type
     * @param count
     */
    private void addGainResCnt(Player player, int type, int count) {
        List<Mill> millPbList = player.mills.values().stream().filter(mill -> mill.isUnlock() && mill.getType() == type)
                .map(mill -> {
                    mill.setResCnt(mill.getResCnt() + count);
                    if (mill.getResCnt() > Constant.RES_GAIN_MAX) { // 防止爆掉了
                        mill.setResCnt(Constant.RES_GAIN_MAX);
                    }
                    return PbHelper.createMillPb(mill);
                }).collect(Collectors.toList());

        SynGainResRs.Builder builder = SynGainResRs.newBuilder();
        builder.addAllMills(millPbList);
        Base.Builder msg = PbHelper.createSynBase(SynGainResRs.EXT_FIELD_NUMBER, SynGainResRs.ext, builder.build());
        MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));

    }

    /**
     * 给资源+1次征收
     *
     * @param player
     * @param taskId
     */
    public void processTask(Player player, int taskId) {
        if (!CheckNull.isEmpty(Constant.TASK_GIVE_GAINRES)) {
            List<Integer> config = Constant.TASK_GIVE_GAINRES.stream()
                    .filter(l -> !CheckNull.isEmpty(l) && l.get(0) == taskId).findFirst().orElse(null);
            if (config != null && config.size() >= 3) {
                addGainResCnt(player, config.get(1), config.get(2));
            }
        }
    }

    /**
     * 获取军团任务
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetPartyTaskRs getPartyTask(Long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        // 将主线任务添加到玩家身上
        Map<Integer, Task> taskMap = player.partyTask;

        GetPartyTaskRs.Builder builder = GetPartyTaskRs.newBuilder();
        Iterator<Task> it = taskMap.values().iterator();
        CampMember p = campDataManager.getCampMember(player.lord.getLordId());
        if (p.getTaskLv() == 0) {
            Camp camp = campDataManager.getParty(player.lord.getCamp());
            p.setTaskLv(camp.getPartyLv());
            LogUtil.debug(roleId + ",军团任务等级重置为" + camp.getPartyLv());
        }
        while (it.hasNext()) {
            Task task = it.next();
            int taskId = task.getTaskId();
            StaticPartyTask stask = StaticTaskDataMgr.getPartyTask(taskId);
            if (stask == null) {
                continue;
            }
            if (stask.getLv() != p.getTaskLv()) {
                continue;
            }
            taskDataManager.currentTask(player, task, stask.getCond(), stask.getCondId(), stask.getSchedule());
            if (task.getSchedule() >= stask.getSchedule() && task.getStatus() == 0) {
                task.setStatus(TaskType.TYPE_STATUS_FINISH);
            }
            if (task.getSchedule() < stask.getSchedule()) {
                task.setStatus(TaskType.TYPE_STATUS_UNFINISH);
            }
            builder.addTask(PbHelper.createTaskPb(task));
        }
        int nowHour = TimeHelper.getCurrentHour();
        int difHour = nowHour % PartyConstant.PARTY_TASK_REFRESH_PER_HOUR;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, PartyConstant.PARTY_TASK_REFRESH_PER_HOUR - difHour);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        builder.setEndTime((int) (cal.getTimeInMillis() / 1000));
        builder.setTaskLv(p.getTaskLv());
        return builder.build();
    }

    /**
     * 领取军团任务奖励
     *
     * @param roleId
     * @param taskId
     * @return
     * @throws MwException
     */
    public PartyTaskAwardRs partyTaskAward(Long roleId, int taskId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        // 星期五无活动
        if (TimeHelper.isFriday()) {
            throw new MwException(GameError.TASK_NO_FINISH.getCode(), "周五无活动, roleId:" + roleId + ",taskId=" + taskId);
        }
        Task task = player.partyTask.get(taskId);
        if (task == null) {
            throw new MwException(GameError.TASK_NO_FINISH.getCode(),
                    "领取任务奖励时,任务未完成, roleId:" + roleId + ",taskId=" + taskId);
        }
        StaticPartyTask stask = StaticTaskDataMgr.getPartyTask(taskId);
        if (stask == null) {
            throw new MwException(GameError.TASK_NO_FINISH.getCode(),
                    "领取任务奖励时,任务未完成, roleId:" + roleId + ",taskId=" + taskId);
        }
        if (task.getStatus() == TaskType.TYPE_STATUS_REWARD) {
            throw new MwException(GameError.TASK_REWARD.getCode(), "军团奖励已领取, roleId:" + roleId + ",taskId=" + taskId);
        }
        CampMember p = campDataManager.getCampMember(player.lord.getLordId());
        if (stask.getLv() != p.getTaskLv()) {
            throw new MwException(GameError.TASK_NO_FINISH.getCode(),
                    "领取任务奖励时,任务未完成, roleId:" + roleId + ",taskId=" + taskId);
        }
        taskDataManager.currentTask(player, task, stask.getCond(), stask.getCond(), stask.getSchedule());
        int finishValue = stask.getSchedule();
        if (task.getSchedule() < finishValue) {
            throw new MwException(GameError.TASK_NO_FINISH.getCode(),
                    "领取任务奖励时,任务未完成, roleId:" + roleId + ",taskId=" + taskId);
        }

        task.setStatus(TaskType.TYPE_STATUS_REWARD);

        PartyTaskAwardRs.Builder builder = PartyTaskAwardRs.newBuilder();
        activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_CAMP_TASK_FINSH_CNT, 1);
        // 给予奖励
        builder.addAllAward(
                rewardDataManager.addAwardDelaySync(player, stask.getAward(), null, AwardFrom.PARTY_TASK_REWARD));
        if (PartyConstant.PARTY_TASK_EXT_REWARD_CNT >= p.getTaskAwardCnt()) {
            List<List<Integer>> awardList = stask.getExtReward();
            if (awardList != null) {
                if (RandomHelper.isHitRangeIn10000(stask.getExtRatio())) {
                    for (List<Integer> ee : awardList) {
                        int type = ee.get(0);
                        int id = ee.get(1);
                        int count = ee.get(2);
                        int keyId = rewardDataManager.addAward(player, type, id, count, AwardFrom.TASK_DAYIY_AWARD);
                        builder.addAward(PbHelper.createAwardPb(type, id, count, keyId));
                    }
                    p.setTaskAwardCnt(p.getTaskAwardCnt() + 1);
                    // 额外奖励发军团聊天消息
                    chatDataManager.sendSysChat(ChatConst.CHAT_EXTRA_TASK_REWARD, player.lord.getCamp(), 0,
                            player.lord.getNick());
                }
            }
        }
        builder.setTask(PbHelper.createTaskPb(task));
        return builder.build();
    }

    /**
     * 获取世界任务信息
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    // @Deprecated
    // public GetWorldTaskRs getWorldTaskRq(Long roleId) throws MwException {
    // Player player = playerDataManager.checkPlayerIsExist(roleId);
    // GetWorldTaskRs.Builder builder = GetWorldTaskRs.newBuilder();
    // int myId = 1;
    // for (Task task : player.worldTasks.values()) {
    // if (task.getTaskId() > myId) {
    // myId = task.getTaskId();
    // }
    // }
    // Task task = player.worldTasks.get(myId);
    // if (task == null) {
    // task = new Task(myId, 0, TaskType.TYPE_STATUS_UNFINISH, 1);
    // player.worldTasks.put(myId, task);
    // }
    // WorldTask worldTask = worldDataManager.getWorldTask();
    // for (Entry<Integer, CommonPb.WorldTask> kv :
    // worldTask.getWorldTaskMap().entrySet()) {
    // builder.addWorldTask(kv.getValue());
    // }
    // // 如果当前进度为世界boss，而且boss被击败，则当前进度进入下一个任务
    // StaticWorldTask staticWorldTask = StaticWorldDataMgr.getWorldTask(myId);
    // if (staticWorldTask == null) {
    // LogUtil.debug("myId===staticWorldTask null=============" + myId);
    // }
    // playerDataManager.refreshDaily(player);
    // CommonPb.WorldTask worldTaskObj = worldTask.getWorldTaskMap().get(myId);
    // if (staticWorldTask.getTaskType() == TaskType.WORLD_TASK_TYPE_BOSS &&
    // worldTaskObj != null
    // && worldTaskObj.getTaskCnt() > 0) {
    // int nextMyId = processNextWorldTask(player, myId, worldTask);
    // for (Task v : player.worldTasks.values()) {
    // if (v.getTaskId() > myId) {
    // nextMyId = v.getTaskId();
    // }
    // }
    // // 发送最终状态
    // task = player.worldTasks.get(nextMyId);
    // }
    // // 没有开世界任务时就给客户端发0
    // builder.setMyId(taskDataManager.isOpenWorldTask(player) ?
    // task.getTaskId() : 0);
    // builder.setMyCnt((int) task.getSchedule());
    // builder.setMyStatus(task.getStatus());
    // LogUtil.debug("当前世界任务进度=" + worldTask.getWorldTaskId().get() +
    // ",worldTask=" + worldTask + ",myId=" + myId
    // + ",myTask=" + task);
    // return builder.build();
    // }

    /**
     * 领取世界任务奖励
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    // @Deprecated
    // public GainWorldTaskRs gainWorldTaskRq(Long roleId) throws MwException {
    // Player player = playerDataManager.checkPlayerIsExist(roleId);
    // // 检查是否开启
    // if (!taskDataManager.isOpenWorldTask(player)) {
    // throw new MwException(GameError.FUNCTION_LOCK.getCode(),
    // "领取任务奖励时,世界任务未开启, roleId:" + roleId);
    // }
    // GainWorldTaskRs.Builder builder = GainWorldTaskRs.newBuilder();
    // // 检查是否完成
    // int myId = 1;
    // for (Task task : player.worldTasks.values()) {
    // if (task.getTaskId() > myId) {
    // myId = task.getTaskId();
    // }
    // }
    // StaticWorldTask staticWorldTask = StaticWorldDataMgr.getWorldTask(myId);
    //
    // Task task = player.worldTasks.get(myId);
    // if (task != null && task.getStatus() == TaskType.TYPE_STATUS_REWARD) {
    // throw new MwException(GameError.REWARD_GAIN.getCode(), "领取任务奖励时,重复领取,
    // roleId:" + roleId);
    // }
    //
    // WorldTask worldTask = worldDataManager.getWorldTask();
    // boolean myCampReward = false;
    // if (staticWorldTask.getTaskType() == TaskType.WORLD_TASK_TYPE_BANDIT) {
    // // 打流寇次数
    // if (task == null || task.getSchedule() < staticWorldTask.getCond()) {
    // throw new MwException(GameError.TASK_NO_FINISH.getCode(),
    // "领取任务奖励时,任务未完成, roleId:" + roleId + ",我的任务ID=" + task);
    // }
    // } else if (staticWorldTask.getTaskType() ==
    // TaskType.WORLD_TASK_TYPE_BOSS) {
    // // 世界boss不用领奖
    // throw new MwException(GameError.TASK_NO_FINISH.getCode(),
    // "领取任务奖励时,任务未完成, roleId:" + roleId + ",我的任务ID=" + myId);
    // } else if (staticWorldTask.getTaskType() ==
    // TaskType.WORLD_TASK_TYPE_CITY) {
    // CommonPb.WorldTask worldTaskObj = worldTask.getWorldTaskMap().get(myId);
    // if (worldTaskObj == null || worldTaskObj.getTaskCnt() <= 0) {
    // throw new MwException(GameError.TASK_NO_FINISH.getCode(),
    // "领取任务奖励时,任务未完成, roleId:" + roleId + ",我的任务ID=" + myId);
    // }
    // if (worldTaskObj.getCamp() == player.lord.getCamp()) {
    // myCampReward = true;
    // }
    // } else {
    // throw new MwException(GameError.TASK_NO_FINISH.getCode(), "领取任务奖励时,任务未完成,
    // roleId:" + roleId);
    // }
    //
    // // 保存已完成id
    // Task newTask = new Task(myId, 1, TaskType.TYPE_STATUS_REWARD, 1);
    // player.worldTasks.put(myId, newTask);
    // // 生成新的
    // int nextMyId = processNextWorldTask(player, myId, worldTask);
    // for (Task t : player.worldTasks.values()) {
    // if (t.getTaskId() > myId) {
    // nextMyId = t.getTaskId();
    // }
    // }
    // // 发送最终状态
    // newTask = player.worldTasks.get(nextMyId);
    // builder.setMyId(newTask.getTaskId());
    // builder.setMyCnt((int) newTask.getSchedule());
    // builder.setMyStatus(newTask.getStatus());
    // if (myCampReward) {
    // builder.addAllAward(
    // rewardDataManager.sendReward(player, staticWorldTask.getCampAward(),
    // AwardFrom.WORLD_TASK));
    // } else {
    // builder.addAllAward(
    // rewardDataManager.sendReward(player, staticWorldTask.getAwardList(),
    // AwardFrom.WORLD_TASK));
    // }
    // CommonPb.WorldTask worldTaskObj =
    // worldTask.getWorldTaskMap().get(nextMyId);
    // if (worldTaskObj != null) {
    // builder.setWorldTask(worldTaskObj);
    // }
    // LogUtil.debug("领取世界任务奖励,当前世界任务进度=" + worldTask.getWorldTaskId().get() +
    // ",myTask=" + newTask + ",nextMyId="
    // + nextMyId + ", 是否双倍=" + myCampReward);
    // LogLordHelper.commonLog("worldTaskAward", AwardFrom.TASK_DAYIY_AWARD,
    // player, newTask.getTaskId(), nextMyId);
    // return builder.build();
    // }

    /**
     * 如果下个任务为boss，则跳过boss，继续往下一个任务
     *
     * @param myId
     */
    private int processNextWorldTask(Player player, int myId, WorldTask worldTask) {
        Task newTask = null;
        StaticWorldTask staticWorldTask = StaticWorldDataMgr.getWorldTask(myId + 1);
        if (staticWorldTask != null) {
            myId++;
            // 如果世界任务已完成，则直接可以领奖
            CommonPb.WorldTask nextWorldTask = worldTask.getWorldTaskMap().get(myId);
            // 世界任务已完成
            if (nextWorldTask != null && nextWorldTask.getTaskCnt() > 0) {
                if (staticWorldTask.getTaskType() == TaskType.WORLD_TASK_TYPE_BOSS) {
                    newTask = new Task(myId, 1, TaskType.TYPE_STATUS_REWARD, 1);
                    player.worldTasks.put(myId, newTask);
                    processNextWorldTask(player, myId, worldTask);
                } else {
                    newTask = new Task(myId, 1, TaskType.TYPE_STATUS_FINISH, 1);
                    player.worldTasks.put(myId, newTask);
                }
            } else {
                newTask = new Task(myId, 0, TaskType.TYPE_STATUS_UNFINISH, 1);
                player.worldTasks.put(myId, newTask);
            }
            LogUtil.debug("循环处理 当前世界任务进度=" + worldTask.getWorldTaskId().get() + ",myTask=" + newTask + ",nextWorldTask="
                    + nextWorldTask + ",myId=" + myId);
        }
        return myId;
    }

    /**
     * 打世界boss
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    // @Deprecated
    // public AtkWorldBossRs atkWorldBoss(Long roleId) throws MwException {
    // Player player = playerDataManager.checkPlayerIsExist(roleId);
    // // 检查是否开启
    // if (!taskDataManager.isOpenWorldTask(player)) {
    // throw new MwException(GameError.FUNCTION_LOCK.getCode(),
    // "攻打世界BOSS,世界任务未开启, roleId:" + roleId);
    // }
    // int myId = 1;
    // for (Task task : player.worldTasks.values()) {
    // if (task.getTaskId() > myId) {
    // myId = task.getTaskId();
    // }
    // }
    // // 判断是否开启
    // StaticWorldTask staticWorldTask = StaticWorldDataMgr.getWorldTask(myId);
    // if (staticWorldTask.getTaskType() != TaskType.WORLD_TASK_TYPE_BOSS) {
    //
    // throw new MwException(GameError.TASK_NO_FINISH.getCode(), "打boss时时,任务未完成,
    // roleId:" + roleId);
    // }
    // WorldTask worldTask = worldDataManager.getWorldTask();
    // staticWorldTask =
    // StaticWorldDataMgr.getWorldTask(worldTask.getWorldTaskId().get());
    // if (staticWorldTask.getTaskType() != TaskType.WORLD_TASK_TYPE_BOSS) {
    // throw new MwException(GameError.TASK_NO_FINISH.getCode(), "打boss时时,任务未完成,
    // roleId:" + roleId);
    // }
    //
    // if (!player.isOnBattle()) {
    // throw new MwException(GameError.HERO_NOT_BATTLE.getCode(), "打boss时,未上阵,
    // roleId:" + roleId);
    // }
    //
    // // 刷新每日攻打次数
    // playerDataManager.refreshDaily(player);
    // Task task = player.worldTasks.get(myId);
    // if (task != null && task.getSchedule() >= 1 +
    // Constant.ATTACK_WORLD_BOSS_MAX) {
    // throw new MwException(GameError.ACTIVITY_IS_JOIN.getCode(), "打boss没次数,
    // roleId:" + roleId);
    // }
    // // 判断是否是免费打
    // if (task.getSchedule() > 0) {// 需要花金币
    // rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY,
    // AwardType.Money.GOLD,
    // Constant.BUY_WORLD_BOSS_GOLD, AwardFrom.WORLD_BOSS_BUY,
    // task.getTaskId());
    // }
    //
    // player.worldTasks.put(myId, new Task(myId, task.getSchedule() + 1,
    // TaskType.TYPE_STATUS_UNFINISH, 1));
    // // 进入战斗
    // return doAtkWorldBoss(player, staticWorldTask, worldTask);
    // }

    // private AtkWorldBossRs doAtkWorldBoss(Player player, StaticWorldTask
    // staticWorldTask, WorldTask worldTask)
    // throws MwException {
    // // 检测世界boss是否被干掉
    // CommonPb.WorldTask worldTaskObj =
    // worldTask.getWorldTaskMap().get(staticWorldTask.getTaskId());
    // if (worldTaskObj.getHp() <= 0) {
    // throw new MwException(GameError.BOSS_IS_DEAD.getCode(), "boss已经被打死,
    // roleId:", player.roleId);
    // }
    // AtkWorldBossRs.Builder builder = AtkWorldBossRs.newBuilder();
    // Fighter attacker = fightService.createWorldBossPlayerFighter(player);
    // Fighter defender = worldTask.getDefender();
    // LogUtil.debug("attacker=" + attacker + ",defender=" + defender);
    // defender = (defender == null || defender.getTotal() == 0)
    // ? fightService.createNpcFighter(staticWorldTask.getParam()) : defender;
    // FightLogic fightLogic = new FightLogic(attacker, defender, true);
    // int bossHp = 0;
    // for (Force f : defender.forces) {
    // bossHp += f.hp;
    // }
    // if (bossHp <= 0) {
    // throw new MwException(GameError.BOSS_IS_DEAD.getCode(), "boss已经被打死,
    // roleId:", player.roleId);
    // }
    // fightLogic.fight();
    // builder.setResult(fightLogic.getWinState());
    // if (fightLogic.getWinState() == 1) {
    // } else {
    // builder.setResult(-1);
    // }
    //
    // // 不论输赢都给奖励
    // builder.addAllAward(
    // rewardDataManager.sendReward(player, staticWorldTask.getAwardList(),
    // AwardFrom.GAIN_COMBAT));
    // // 给将领加经验 其实是0
    // builder.addAllAtkHero(addHeroExp(player, attacker.getForces()));
    // CommonPb.Record record = fightLogic.generateRecord();
    // builder.setRecord(record);
    //
    // int totalHp = defender.getTotal();
    // int lostTotal = defender.getLost();// 总损失兵力
    // int curLost = worldTaskObj.getHp() - (defender.getTotal() - lostTotal);//
    // 本次损失兵力
    // LogUtil.debug("roleId:", player.roleId, ", 世界boss当前剩余血:" +
    // worldTaskObj.getHp(), ",本次扣血:", curLost, ", 总扣血:",
    // lostTotal, ", 总血量:", totalHp);
    // LogLordHelper.commonLog("worldBoss", AwardFrom.WORLD_TASK, player,
    // staticWorldTask.getTaskId(),
    // worldTaskObj.getHp(), curLost, lostTotal, totalHp);
    // if (worldTaskObj.getHp() <= curLost) {
    // // 打死了,生成下个
    // StaticWorldTask staticNextWorldTask =
    // StaticWorldDataMgr.getWorldTask(staticWorldTask.getTaskId() + 1);
    // if (staticNextWorldTask != null) {
    // player.worldTasks.put(staticWorldTask.getTaskId() + 1,
    // new Task(staticWorldTask.getTaskId() + 1, 0,
    // TaskType.TYPE_STATUS_UNFINISH, 1));
    // }
    // worldTask.setDefender(null);
    // if (staticWorldTask.getTaskId() == TaskType.WORLD_BOSS_TASK_ID_1) {
    // globalDataManager.openAreaData(2);
    // chatDataManager.sendSysChat(ChatConst.CHAT_WORLD_BOSS_1,
    // player.lord.getCamp(), 0,
    // staticWorldTask.getTaskId());
    // partyService.openPartyJobDelay();
    // } else if (staticWorldTask.getTaskId() == TaskType.WORLD_BOSS_TASK_ID_2)
    // {
    // globalDataManager.openAreaData(3);
    // chatDataManager.sendSysChat(ChatConst.CHAT_WORLD_BOSS_2,
    // player.lord.getCamp(), 0,
    // staticWorldTask.getTaskId());
    // }
    // LogUtil.debug("roleId:", player.roleId, ", 干掉了世界boss==================");
    // } else {
    // worldTask.setDefender(defender);
    // }
    // taskDataManager.updWorldTask(TaskType.WORLD_TASK_TYPE_BOSS, curLost);
    // // if (staticWorldTask.getTaskId() == TaskType.WORLD_BOSS_TASK_ID_2) {
    // // // 初始化柏林会战
    // // ScheduleManager.getInstance().initBerlinJob();
    // // }
    // builder.setWorldTask(worldTask.getWorldTaskMap().get(staticWorldTask.getTaskId()));
    // return builder.build();
    // }

    /**
     * 打世界boss给将领加经验,默认为0
     *
     * @return
     */
    private List<RptHero> addHeroExp(Player player, List<Force> forces) {
        int addExp = 0;
        List<RptHero> rptList = new ArrayList<>(forces.size());
        for (Force force : forces) {
            Hero hero = player.heros.get(force.id);
            if (null == hero) continue;
            rptList.add(PbHelper.createRptHero(Constant.Role.PLAYER, force.killed, 0, force.id, player.lord.getNick(),
                    hero.getLevel(), addExp, force.lost, hero.getDecorated()));
        }
        return rptList;
    }

    /**
     * 检查客户端可以直接完成的任务
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public CheckTaskRs checkClientTask(Long roleId, CheckTaskRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int taskId = req.getTaskId();
        Task task = player.majorTasks.get(taskId);
        if (task == null) {
            LogUtil.debug("task isnull=" + taskId);
            return null;
        }
        StaticTask staticTask = StaticTaskDataMgr.getTaskById(taskId);
        if (staticTask == null) {
            LogUtil.debug("task config isnull=" + taskId);
            return null;
        }
        if (staticTask.getCond() != TaskType.COND_CLICK && staticTask.getCond() != TaskType.COND_20
                && staticTask.getCond() != TaskType.COND_21 && staticTask.getCond() != TaskType.COND_24
                && staticTask.getCond() != TaskType.COND_25 && staticTask.getCond() != TaskType.COND_FIGHT_SHOW) {
            LogUtil.debug("task config isnull=" + taskId);
            return null;
        }
        Task preTask = player.majorTasks.get(staticTask.getTriggerId());
        if (task.getStatus() != TaskType.TYPE_STATUS_REWARD
                && (staticTask.getTriggerId() == 0 || (preTask != null && preTask.getStatus() > 0))) {
            task.setSchedule(1);
            task.setStatus(TaskType.TYPE_STATUS_FINISH);
        }
        CheckTaskRs.Builder builder = CheckTaskRs.newBuilder();
        builder.setTask(PbHelper.createTaskPb(task));
        return builder.build();
    }

    /**
     * 获取任务信息
     *
     * @param roleId
     * @param taskId
     * @return
     * @throws MwException
     */
    public TaskInfoRs taskInfo(Long roleId, int taskId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Task task = player.majorTasks.get(taskId);
        TaskInfoRs.Builder builder = TaskInfoRs.newBuilder();
        if (task != null) {
            StaticTask stask = StaticTaskDataMgr.getTaskById(taskId);
            if (stask != null) {
                // 如果已完成，则不判断
                if (task.getStatus() == TaskType.TYPE_STATUS_UNFINISH) {
                    taskDataManager.currentMajorTask(player, task, stask);
                }
                builder.setTask(PbHelper.createTaskPb(task));
            }
        }
        return builder.build();
    }

    /**
     * 剧情章节领奖
     *
     * @param roleId
     * @param sectionId
     * @return
     * @throws MwException
     */
    public SectionAwardRs sectionAward(Long roleId, int sectionId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        StaticSectiontask sSection = StaticTaskDataMgr.getSectiontaskById(sectionId);
        if (sSection == null) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "章节配置错误 roleId:", roleId, ", sectionId:", sectionId);
        }
        int now = TimeHelper.getCurrentSecond();
        // 获取当前剧情章节并检测状态
        getCurSectionAndCheckStatus(player);
        Sectiontask section = player.sectiontask.stream().filter(s -> s.getSectionId() == sectionId).findFirst()
                .orElse(null);
        if (section == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "章节领奖未初始化 roleId:", roleId, ", sectionId:", sectionId);
        }
        if (section.getStatus() == TaskType.TYPE_STATUS_REWARD) {
            throw new MwException(GameError.REWARD_GAIN.getCode(), "章节领奖已经领取 roleId:", roleId, ", sectionId:",
                    sectionId);
        }
        // 检测是否可以领取
        if (section.getStatus() == TaskType.TYPE_STATUS_UNFINISH) {
            throw new MwException(GameError.TASK_NO_FINISH.getCode(), "章节未完成 roleId:", roleId, ", sectionId:",
                    sectionId);
        }
        SectionAwardRs.Builder builder = SectionAwardRs.newBuilder();

        List<List<Integer>> awardLists = new ArrayList<>();
        // 检测是否有任务没有领取
        sSection.getSectionTask().stream().map(curId -> player.majorTasks.get(curId)).filter(task -> !CheckNull.isNull(task) && task.getStatus() != TaskType.TYPE_STATUS_REWARD).forEach(task -> {
            StaticTask staticTask = StaticTaskDataMgr.getTaskById(task.getTaskId());
            if (task.getTaskId() == Constant.CLEAR_SCORPION_ACTIVATE_END_TIME_TASK) {
                player.setMixtureData(PlayerConstant.SCORPION_ACTIVATE_END_TIME, now);
            }
            // 修改任务已领奖
            task.setStatus(TaskType.TYPE_STATUS_REWARD);
            // 将任务状态修改后的发送给客户端
            builder.addTask(PbHelper.createTaskPb(task));
            // task增加日志记录 (基本信息、战力、关卡类型、关卡id)
            LogLordHelper.commonLog("task", AwardFrom.COMMON, player, player.lord.getFight(), staticTask.getType(),
                    staticTask.getTaskId());
            // 支线任务
            if (staticTask.getType() == TaskType.TYPE_SUB) {
                taskDataManager.updTask(player, TaskType.COND_OTHER_TASK_CNT, 1);
            }
            awardLists.addAll(staticTask.getAwardList());
        });
        section.setStatus(TaskType.TYPE_STATUS_REWARD);// 当前章节修改状态

        // 触发下一个章节
        StaticSectiontask nextSSection = StaticTaskDataMgr.getSectiontaskById(sectionId + 1);
        if (nextSSection != null) {
            Sectiontask nextSection = new Sectiontask(nextSSection.getSectionId(), TaskType.TYPE_STATUS_UNFINISH);
            player.sectiontask.add(nextSection);
            List<Integer> newTasks = nextSSection.getSectionTask();
            // 触发章节任务
            for (Integer taskId : newTasks) {
                StaticTask sTask = StaticTaskDataMgr.getTaskById(taskId);
                if (sTask != null) {
                    if (!player.majorTasks.containsKey(taskId)) {
                        Task task = new Task(taskId);
                        player.majorTasks.put(taskId, task);
                        builder.addTask(PbHelper.createTaskPb(task));
                        LogUtil.debug("触发了下个剧情章节的任务  roleId:", player.roleId, " task:", task);
                    } else {
                        Task oldTask = player.majorTasks.get(taskId);
                        builder.addTask(PbHelper.createTaskPb(oldTask));
                        LogUtil.debug("触发了下个剧情章节的任务中有老数据  roleId:", player.roleId, " oldTask:", oldTask);
                    }
                } else {
                    LogUtil.error("剧情章节任务配置出错  roleId:", player.roleId, " taskId:", taskId, ", nextSSection:",
                            nextSSection.getSectionId());
                }
            }
            builder.setCurSection(nextSection.ser());
            LogUtil.debug("触发了下个剧情章节  roleId:", player.roleId, ", nextSSection:", nextSSection.getSectionId());
        } else {
            LogUtil.debug("没有章节可触发  roleId:", player.roleId, ", nextSSection:");
        }
        awardLists.addAll(sSection.getSectionAward());
        List<Award> awardList = rewardDataManager.addAwardDelaySync(player, RewardDataManager.mergeAward(awardLists), null, AwardFrom.SECTIONTASK_REWARD);
        // 合并奖励
        builder.addAllAward(awardList);
        return builder.build();
    }

    /*-------------------------------日常活动相关start----------------------------------*/

    /**
     * 获取日常任务
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetDailyTaskRs getDailyTask(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        GetDailyTaskRs.Builder builder = GetDailyTaskRs.newBuilder();
        player.getDailyTask().values().forEach(t -> builder.addDailyTask(PbHelper.createTaskPb(t)));
        builder.addAllDailyIsGet(player.getDailyIsGet());
        builder.setDailyTaskLivenss(player.getDailyTaskLivenss());
        return builder.build();
    }

    /**
     * 日常活跃度领奖
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public DailyAwardRs dailyAward(long roleId, DailyAwardRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int awardId = req.getId();
        Set<Integer> dailyIsGet = player.getDailyIsGet();
        if (dailyIsGet.contains(awardId)) {
            throw new MwException(GameError.REWARD_GAIN.getCode(), "奖励已经被领取, roleId:", player.roleId);
        }
        StaticDailyTaskAward sAward = StaticTaskDataMgr.getDailyTaskAwardMapById(awardId);
        if (sAward == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "日常任务奖励配置没找到, roleId:", player.roleId, " id:",
                    awardId);
        }
        //暂不检查赛季
//        if(sAward.getSeason() > 0){
//            int currSeason = seasonService.getCurrSeason();
//            if(currSeason != sAward.getSeason()){
//                throw new MwException(GameError.PARAM_ERROR.getCode(),GameError.err(roleId,"日常任务活跃奖励配置错误，当前赛季不匹配",currSeason, JSON.toJSONString(sAward)));
//            }
//            //开放期才能领取日常任务的赛季活跃奖励
//            int currState = seasonService.getSeasonState();
//            if(currState != SeasonConst.STATE_OPEN){
//                throw new MwException(GameError.PARAM_ERROR.getCode(),GameError.err(roleId,"日常任务活跃奖励配置错误，当前不是开放期",currSeason,currState, JSON.toJSONString(sAward)));
//            }
        //天赋优化 调整活跃进度奖励不根据赛季时间调整,根据自身时间配置开启
//            if (!duringPeriod(sAward)) {
//                throw new MwException(GameError.PARAM_ERROR.getCode(),GameError.err(roleId,"日常任务活跃奖励配置错误，当前不是开放期", awardId, JSON.toJSONString(sAward)));
//            }
//        }
        //等级检查
        if (player.lord.getLevel() < sAward.getLevel().get(0) || player.lord.getLevel() > sAward.getLevel().get(1)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "领取日常任务活跃奖励, 等级错误", player.lord.getLevel(), JSON.toJSONString(sAward)));
        }
        if (player.getDailyTaskLivenss() < sAward.getValue()) {
            throw new MwException(GameError.TASK_NO_FINISH.getCode(), "日常任务活跃度不够, roleId:", player.roleId, " id:",
                    awardId, ", hasLivenss:", player.getDailyTaskLivenss());
        }

        dailyIsGet.add(awardId);
        DailyAwardRs.Builder builder = DailyAwardRs.newBuilder();
        builder.addAllAward(
                rewardDataManager.addAwardDelaySync(player, sAward.getAward(), null, AwardFrom.DAILY_TASK_AWARD));
        builder.addAllDailyIsGet(dailyIsGet);
        return builder.build();
    }

    private boolean duringPeriod(StaticDailyTaskAward award) {
        if (CheckNull.isNull(award.getSeasonBeginTime()) || CheckNull.isNull(award.getSeasonEndTime())) {
            return false;
        }

        Date now = new Date();
        return now.after(award.getSeasonBeginTime()) && now.before(award.getSeasonEndTime());
    }

    /**
     * 日常任务活跃度领取
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public LivenssAwardRs livenssAward(long roleId, LivenssAwardRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int taskId = req.getTaskId();
        StaticTask sTask = StaticTaskDataMgr.getDayiyMap().get(taskId);
        if (sTask == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "日常活动领取活跃未找到配置, roleId:", player.roleId, ", taskId:",
                    taskId);
        }
        Task task = player.getDailyTask().get(taskId);
        if (task == null || task.getSchedule() < sTask.getSchedule()) {
            throw new MwException(GameError.TASK_NO_FINISH.getCode(), "任务未完成 roleId:", roleId, ", taskId:", taskId);
        }
        if (task.getStatus() == TaskType.TYPE_STATUS_REWARD) {
            throw new MwException(GameError.REWARD_GAIN.getCode(), "奖励已经被领取, roleId:", player.roleId, ", taskId:",
                    taskId);
        }
        task.setStatus(TaskType.TYPE_STATUS_REWARD);
        int sch = sTask.getAwardList().get(0).get(0);
        player.setDailyTaskLivenss(player.getDailyTaskLivenss() + sch); // 加上活跃度
        //上报数数
        EventDataUp.credits(player.account, player.lord, player.getDailyTaskLivenss(), sch, CreditsConstant.DAILY_TASK, AwardFrom.DAILY_TASK_AWARD);

        activityDataManager.updateLuckyPoolLive(player, player.getDailyTaskLivenss());
        // 更新七日活动每日任务领取的次数
        activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_DAILY_TASK_CNT, 1);

        //貂蝉任务-完成每日任务
        ActivityDiaoChanService.completeTask(player, ETask.FINISHED_DAILYTASK);
        TaskService.processTask(player, ETask.FINISHED_DAILYTASK);
        titleService.processTask(player, ETask.FINISHED_DAILYTASK);

        LogUtil.debug("roleId:", player.roleId, " 日常任务活跃度增加: taskId:", taskId, ",sch:", sch, ",Livenss",
                player.getDailyTaskLivenss());
        battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_DAILY_LIVENSS, sch);
        LivenssAwardRs.Builder builder = LivenssAwardRs.newBuilder();
        builder.setDailyTask(PbHelper.createTaskPb(task));
        builder.setDailyTaskLivenss(player.getDailyTaskLivenss());
        return builder.build();
    }

    /*-------------------------------日常活动相关end----------------------------------*/

    /**
     * 修复玩家主线任务
     */
    public void gmFixMajorTask() {
        List<StaticTask> majorListTask = StaticTaskDataMgr.getMajorMap().values().stream()
                .filter(t -> t.getType() == TaskType.TYPE_MAIN).sorted(Comparator.comparing(StaticTask::getMainSort))
                .collect(Collectors.toList());
        StaticTask lastTask = StaticTaskDataMgr.getLastMainTask();
        for (Player player : playerDataManager.getPlayers().values()) {
            // 重新计算主线任务
            player.curMajorTaskIds.clear();
            player.curMajorTaskIds.addAll(taskDataManager.getCurTask(player));

            StaticTask curMajorSTask = player.curMajorTaskIds.stream()
                    .map(taskId -> StaticTaskDataMgr.getMajorMap().get(taskId))
                    .filter(t -> t.getType() == TaskType.TYPE_MAIN).findFirst().orElse(null);
            if (curMajorSTask == null) { // 说明最后一关通关了
                curMajorSTask = lastTask;
            }
            for (StaticTask st : majorListTask) {
                if (curMajorSTask.getMainSort() > st.getMainSort()) {
                    if (!player.majorTasks.containsKey(st.getTaskId())) {
                        Task task = new Task(st.getTaskId());
                        task.setSchedule(st.getSchedule());
                        task.setStatus(TaskType.TYPE_STATUS_REWARD);
                        player.majorTasks.put(task.getTaskId(), task);
                        LogUtil.debug("修复玩家主线任务  roleId:", player.roleId, ", addTaskId:", task.getTaskId(),
                                ", curMajorTask:", curMajorSTask.getTaskId());
                    }
                } else {
                    break;
                }
            }
        }
    }

    /**
     * 蝎王激活
     *
     * @param roleId 玩家id
     * @param req    请求协议
     * @return 返回协议
     * @throws MwException 自定义异常
     */
    public GamePb4.ScorpionActivateRs scorpionActivate(Long roleId, GamePb4.ScorpionActivateRq req) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        GamePb4.ScorpionActivateRs.Builder builder = GamePb4.ScorpionActivateRs.newBuilder();
        if (req.hasType()) {
            int type = req.getType();
            // 使用道具
            if (type == 1) {
                int propId = PropConstant.SCORPION_ACTIVATE_PROP;

                Prop prop = player.props.get(propId);
                if (prop == null || prop.getCount() == 0) {
                    throw new MwException(GameError.NO_PROP.getCode(), "道具使用,无此道具, roleId:" + roleId);
                }
                rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.PROP, propId, 1, AwardFrom.USE_PROP);
                // 更新使用蝎王解锁时间
                int now = TimeHelper.getCurrentSecond();
                player.setMixtureData(PlayerConstant.SCORPION_ACTIVATE_END_TIME, StaticPropDataMgr.SCORPION_ACTIVATE_TIME.get(0) + now);
                taskDataManager.updTask(player, TaskType.COND_SCORPION_ACTIVATE_PREPOSITION, 1);
                builder.setTime(StaticPropDataMgr.SCORPION_ACTIVATE_TIME.get(0) + now);
            } else if (type == 2) {
                // 召唤将领
                int activateTime = player.getMixtureDataById(PlayerConstant.SCORPION_ACTIVATE_END_TIME);
                int now = TimeHelper.getCurrentSecond();
                if (activateTime == 0 || activateTime > now) {
                    throw new MwException(GameError.SCORPION_ACTIVATE_ERROR.getCode(), "激活蝎王, 前置条件未完成, 或者时间未达到, activateTime:", activateTime);
                }

                Hero hero = rewardDataManager.addHero(player, StaticPropDataMgr.SCORPION_ACTIVATE_TIME.get(1), AwardFrom.SCORPION_ACTIVATE_AWARD);
                taskDataManager.updTask(player, TaskType.COND_SCORPION_ACTIVATE, 1);
                builder.setHero(PbHelper.createHeroPb(hero, player));
            } else if (type == 3) {
                // 救援爱丽丝
                int aliceAwardTime = player.getMixtureDataById(PlayerConstant.ALICE_AWARD_TIME);
                int now = TimeHelper.getCurrentSecond();
                if (aliceAwardTime == 0 || aliceAwardTime > now) {
                    throw new MwException(GameError.ALICE_AWARD_ERROR.getCode(), "救援爱丽丝, 前置条件未完成, 或者时间未达到, aliceAwardTime:", aliceAwardTime);
                }
                Hero hero = rewardDataManager.addHero(player, Constant.ALICE_RESCUE_MISSION_TASK.get(1), AwardFrom.ALICE_AWARD);
                if (hero == null) {
                    throw new MwException(GameError.ALICE_AWARD_RECEIVED.getCode(), "救援爱丽丝, 奖励已经领取");
                }
                builder.setHero(PbHelper.createHeroPb(hero, player));
            }
        }

        return builder.build();
    }

    /**
     * 个人目标任务
     *
     * @param roleId 玩家id
     * @param ids    目标任务id
     * @return 响应
     * @throws MwException 自定义异常
     */
    public GetAdvanceTaskRs getAdvanceTask(long roleId, List<Integer> ids) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        // 检测并修改个人目标进度
        checkAndModTask(player);

        List<Task> showTask;
        if (CheckNull.isEmpty(ids)) {
            showTask = new ArrayList<>(player.getAdvanceTask().values());
        } else {
            showTask = player.getAdvanceTask().values().stream().filter(task -> ids.contains(task.getTaskId())).collect(Collectors.toList());
        }
        return GetAdvanceTaskRs.newBuilder().addAllAdvanceTask(showTask.stream().map(PbHelper::createTaskPb).collect(Collectors.toList())).build();
    }

    /**
     * 个人目标领取
     *
     * @param roleId 玩家id
     * @param req    请求
     * @return 响应
     * @throws MwException 自定义异常
     */
    public AdvanceAwardRs advanceAward(long roleId, AdvanceAwardRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int taskId = req.getTaskId();
        StaticTask staticTask = StaticTaskDataMgr.getTaskById(taskId);
        if (staticTask == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(),
                    "领取任务奖励时,配置找不到, roleId:" + roleId + ",taskId=" + taskId);
        }

        Task task = player.getAdvanceTask().get(taskId);
        if (task == null) {
            throw new MwException(GameError.NO_TASK.getCode(), "领取任务奖励时,无此任务, roleId:" + roleId + ",taskId=" + taskId);
        }
        if (task.getStatus() == TaskType.TYPE_STATUS_REWARD) {
            throw new MwException(GameError.REWARD_GAIN.getCode(), "领取任务奖励时,重复领取, roleId:" + roleId);
        }

        // 检测并修改个人目标进度
        checkAndModTask(player);
        if (task.getStatus() == TaskType.TYPE_STATUS_UNFINISH) {
            throw new MwException(GameError.TASK_NO_FINISH.getCode(), "领取任务奖励时,任务未完成, roleId:" + roleId + ",taskId=" + taskId);
        }

        // task增加日志记录 (基本信息、战力、关卡类型、关卡id)
        LogLordHelper.commonLog("advanceTask", AwardFrom.COMMON, player, player.lord.getFight(), staticTask.getType(), taskId);

        task.setStatus(TaskType.TYPE_STATUS_REWARD);

        List<List<Integer>> awardList = staticTask.getAwardList();
        if (!CheckNull.isEmpty(awardList)) {
            // 奖励同步给客户端
            rewardDataManager.sendReward(player, awardList, AwardFrom.ADVANCE_AWARD, taskId);
        }

        return AdvanceAwardRs.newBuilder().setAdvanceTask(PbHelper.createTaskPb(task)).build();
    }


    /**
     * 检测并修改个人目标进度
     *
     * @param player 玩家对象
     */
    private void checkAndModTask(Player player) {
        if (CheckNull.isNull(player)) {
            return;
        }
        Map<Integer, Task> advanceTask = player.getAdvanceTask();
        StaticTaskDataMgr.getAdvanceMap()
                .forEach((taskId, sTask) -> {
                    Task task = advanceTask.computeIfAbsent(taskId, (t) -> new Task(sTask.getTaskId()));
                    if (task.getStatus() == TaskType.TYPE_STATUS_UNFINISH) {
                        // 如果任务状态是未完成, 每次获取的时候, 主动检测任务进度
                        taskDataManager.currentTask(player, task, sTask.getCond(), sTask.getCondId(), sTask.getSchedule());
                    }
                    if (task.getSchedule() >= sTask.getSchedule() && task.getStatus() == 0) {
                        task.setStatus(TaskType.TYPE_STATUS_FINISH);
                    }
                    if (task.getSchedule() < sTask.getSchedule()) {
                        task.setStatus(TaskType.TYPE_STATUS_UNFINISH);
                    }
                });
    }

    /**
     * @param player
     * @param eTask
     * @param params
     */
    public static void handleTask(Player player, ETask eTask, int... params) {
        try {
            SeasonService seasonService = DataResource.ac.getBean(SeasonService.class);
            seasonService.handleTreasuryTask0(player, eTask, params);
            seasonService.handleJourneyTask0(player, eTask, params);
        } catch (Exception e) {
            LogUtil.error("处理任务发生错误, ", e);
        }
    }

    /**
     * 处理任务
     *
     * @param player 玩家信息
     * @param eTask  任务类型
     * @param params 任务参数
     */
    public static void processTask(Player player, ETask eTask, int... params) {
        Map<String, TaskFinishService> resultMap = DataResource.ac.getBeansOfType(TaskFinishService.class);
        if (CheckNull.nonEmpty(resultMap)) {
            for (Map.Entry<String, TaskFinishService> entry : resultMap.entrySet()) {
                try {
                    entry.getValue().process(player, eTask, params);
                } catch (Exception e) {
                    LogUtil.error(String.format("service: %s, roleId: %d, process task type: %d, params: %s",
                            entry.getKey(), player.getLordId(), eTask.getTaskType(), Arrays.toString(params)), e);
                }
            }
        }
    }

    /**
     * 检查任务条件并处理任务进度和状态
     *
     * @param task
     * @param condList
     * @param eTask
     * @param params
     * @return
     */
    public boolean checkTaskCondition(Player player, Task task, List<Integer> condList, ETask eTask, int... params) {
        boolean b = false;
        Hero hero;
        int count;
        switch (eTask) {
            case FIGHT_REBEL:
                if (params[1] >= condList.get(0) && params[1] <= condList.get(1)) {
                    b = true;
                    task.setSchedule(task.getSchedule() + 1);
                    if (task.getSchedule() >= condList.get(2)) {
                        task.setStatus(TaskType.TYPE_STATUS_FINISH);
                        task.setSchedule(condList.get(2));
                    }
                }
                break;
            case MAKE_ARMY:
                if (condList.get(0) == 0 || condList.get(0) == params[0]) {
                    b = true;
                    task.setSchedule(task.getSchedule() + params[1]);
                    if (task.getSchedule() >= condList.get(1)) {
                        task.setStatus(TaskType.TYPE_STATUS_FINISH);
                        task.setSchedule(condList.get(1));
                    }
                }
                break;
            case FINISHED_TASK:
                if (params[0] == condList.get(0)) {
                    if (params[0] == TaskCategory.TREASURY.getCategory()) {//宝库任务 累计的完成个数
                        count = player.getPlayerSeasonData().getFinishedCount();
                        if (count != task.getSchedule()) {
                            b = true;
                            task.setSchedule(count);
                        }
                        if (task.getSchedule() >= condList.get(1)) {
                            task.setStatus(TaskType.TYPE_STATUS_FINISH);
                            task.setSchedule(condList.get(1));
                        }
                    }
                }
                break;
            case GET_TASKAWARD:
                if (params[0] == condList.get(0)) {
                    if (params[0] == TaskCategory.TREASURY.getCategory()) {
                        count = player.getPlayerSeasonData().getGetAwardCount();
                        if (count != task.getSchedule()) {
                            b = true;
                            task.setSchedule(count);
                        }
                        if (task.getSchedule() >= condList.get(1)) {
                            task.setStatus(TaskType.TYPE_STATUS_FINISH);
                            task.setSchedule(condList.get(1));
                        }
                    }
                }
                break;
            case JOIN_ACTIVITY:
                if (params[0] == condList.get(0)) {
                    b = true;
                    task.setSchedule(task.getSchedule() + 1);
                    if (task.getSchedule() >= condList.get(1)) {
                        task.setStatus(TaskType.TYPE_STATUS_FINISH);
                        task.setSchedule(condList.get(1));
                    }
                }
                break;
            case CONSUME_DIAMOND:
            case ARMY_MAK_LOST:
                b = true;
                task.setSchedule(task.getSchedule() + params[0]);
                if (task.getSchedule() >= condList.get(0)) {
                    task.setStatus(TaskType.TYPE_STATUS_FINISH);
                    task.setSchedule(condList.get(0));
                }
                break;
            case GET_HERO:
                if (Objects.nonNull(player.heros.get(condList.get(0)))) {
                    b = true;
                    task.setSchedule(task.getSchedule() + 1);
                    task.setStatus(TaskType.TYPE_STATUS_FINISH);
                }
                break;
            case HERO_UPSTAR:
                hero = player.heros.get(condList.get(0));
                if (Objects.nonNull(hero)) {
                    if (hero.getCgyStage() > condList.get(1) || (hero.getCgyStage() == condList.get(1) && hero.getCgyLv() >= condList.get(2))) {
                        task.setStatus(TaskType.TYPE_STATUS_FINISH);
                        task.setSchedule(1);
                    }
                    b = true;
                }
                break;
            case HERO_UPSKILL:
                hero = player.heros.get(condList.get(0));
                if (Objects.nonNull(hero)) {
                    task.setSchedule(hero.getSkillLevels().getOrDefault(condList.get(1), 0));
                    if (task.getSchedule() >= condList.get(2)) {
                        task.setStatus(TaskType.TYPE_STATUS_FINISH);
                        task.setSchedule(condList.get(2));
                    }
                    b = true;
                }
                break;
            case HERO_LEVELUP:
                hero = player.heros.get(condList.get(0));
                if (Objects.nonNull(hero)) {
                    task.setSchedule(hero.getLevel());
                    if (task.getSchedule() >= condList.get(1)) {
                        task.setStatus(TaskType.TYPE_STATUS_FINISH);
                        task.setSchedule(condList.get(1));
                    }
                    b = true;
                }
                break;
            default:
        }
        return b;
    }

}
