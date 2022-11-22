package com.gryphpoem.game.zw.service.chapterTask;

import com.google.common.collect.Lists;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticChapterTaskDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticTaskDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.constant.task.TaskCone513Type;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.s.StaticTask;
import com.gryphpoem.game.zw.resource.domain.s.StaticTaskChapter;
import com.gryphpoem.game.zw.resource.pojo.Task;
import com.gryphpoem.game.zw.resource.pojo.chapterTask.ChapterTask;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.BuildingService;
import com.gryphpoem.game.zw.service.GmCmd;
import com.gryphpoem.game.zw.service.GmCmdService;
import com.gryphpoem.game.zw.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * desc: 章节任务
 * author: huangxm
 * date: 2022/5/24 18:43
 **/
@Service
public class ChapterTaskService implements GmCmdService {
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private BuildingDataManager buildingDataManager;
    @Autowired
    private WorldDataManager worldDataManager;
    @Autowired
    private BuildingService buildingService;
    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private ChapterTaskDataManager chapterTaskDataManager;
    @Autowired
    private TaskDataManager taskDataManager;
    @Autowired
    private TaskService taskService;

    /**
     * 获取主支线,剧情任务
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GamePb5.GetChapterTaskRs.Builder getChapterTask(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        ChapterTask chapterTask = player.chapterTask;
        int chapterId = chapterTask.getChapterId();
        chapterTaskDataManager.checkPlayerChapterTask(player);
        GamePb5.GetChapterTaskRs.Builder builder = GamePb5.GetChapterTaskRs.newBuilder();
        builder.setCurrentChapter(chapterId);
        builder.setIsReceive(chapterTask.currentChapterIsReceive());
        Map<Integer, CommonPb.Task> tasksPb = chapterTaskDataManager.getTasksPb(player);
        builder.addAllTask(tasksPb.values());
        builder.addAllCurTaskId(tasksPb.keySet());
        return builder;
    }

    /**
     * 获取主支线  领奖
     */
    public GamePb5.GetChapterTaskAwardRs.Builder getChapterTaskAward(long roleId, int taskId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        StaticTask staticTask = StaticTaskDataMgr.getTaskById(taskId);
        if (Objects.isNull(staticTask)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "领取任务奖励时,配置找不到, roleId:" + roleId + ",taskId=" + taskId);
        }
        ChapterTask chapterTask = player.chapterTask;
        Task task = player.chapterTask.getOpenTasks().get(taskId);
        if (Objects.isNull(task)) {
            throw new MwException(GameError.NO_TASK.getCode(), "领取任务奖励时,无此任务, roleId:" + roleId + ",taskId=" + taskId);
        }
        if (task.getStatus() == TaskType.TYPE_STATUS_REWARD) {
            throw new MwException(GameError.REWARD_GAIN.getCode(), "领取任务奖励时,重复领取, roleId:" + roleId);
        }

        taskDataManager.currentMajorTask(player, task, staticTask);
        if (task.getSchedule() < staticTask.getSchedule()) {
            throw new MwException(GameError.TASK_NO_FINISH.getCode(), "领取任务奖励时,任务未完成, roleId:" + roleId + ",taskId=" + taskId);
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
            taskDataManager.updTask(player, TaskType.COND_513, 1, TaskCone513Type.SIDE_QUEST);
        }
        // 世界开启
        if (taskId == Constant.ALLOC_POS_CONDITION) {
            worldDataManager.openPos(player);
        }
        task.setStatus(TaskType.TYPE_STATUS_REWARD);

        GamePb5.GetChapterTaskAwardRs.Builder builder = GamePb5.GetChapterTaskAwardRs.newBuilder();
        List<List<Integer>> awardList = staticTask.getAwardList();
        // 获取活动翻倍
        int num = activityDataManager.getActDoubleNum(player);
        if (CheckNull.nonEmpty(awardList) && staticTask.getIsGet() > 0) {
            builder.addAllAward(rewardDataManager.sendReward(player, awardList, num, AwardFrom.GET_CHAPTER_AWARD));
        }
        LogLordHelper.commonLog("taskAward", AwardFrom.TASK_DAYIY_AWARD, player, taskId);
        List<StaticTask> triggerList = StaticTaskDataMgr.getTriggerTask(taskId);
        if (Objects.nonNull(triggerList)) {
            for (StaticTask ee : triggerList) {
                taskService.processTask(player, ee.getTaskId());
                Task etask = chapterTask.getOpenTasks().computeIfAbsent(ee.getTaskId(), x -> new Task(ee.getTaskId()));
                taskDataManager.currentMajorTask(player, etask, ee);
                LogUtil.debug("触发下一个主线任务=" + etask);
            }
        }
        // 解锁资源建筑
        buildingDataManager.refreshSourceData(player);
        // 触发自动建造
        buildingService.addAutoBuild(player);

        // 特殊,商用建造队列赠送
        if (StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_BUILD_GIFT)) {
            Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_BUILD_GIFT);
            if (!CheckNull.isNull(activity) && activityDataManager.currentActivity(player, activity, 0, now) == 0) {
                activityDataManager.updActivity(player, ActivityConst.ACT_BUILD_GIFT, 1, 0, true);
            }
        }
        Map<Integer, CommonPb.Task> tasksPbMap = chapterTaskDataManager.getTasksPb(player);
        builder.addAllCurTaskId(tasksPbMap.keySet());
        builder.addAllTask(tasksPbMap.values());
        return builder;
    }

    /**
     * 章节奖励领取
     */
    public GamePb5.GetChapterAwardRs.Builder getChapterAward(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        ChapterTask chapterTask = player.chapterTask;
        int chapterId = chapterTask.getChapterId();
        if (chapterTask.currentChapterIsReceive()) {
            throw new MwException(GameError.AWARD_HAD_GOT.getCode(), "领取任务奖励时,重复领取, roleId:" + roleId + "chapterId:", chapterId);
        }
        Map<Integer, StaticTask> staticChapterTaskMap = StaticTaskDataMgr.getStaticChapterTaskMap(chapterId);
        if (CheckNull.isEmpty(staticChapterTaskMap)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "领取任务奖励时,章节配置为空 roleId:" + roleId + "chapterId:", chapterId);
        }
        // 当前章节未完成的数量
        long count = chapterTask.getOpenTasks().values().stream()
                .filter(e -> staticChapterTaskMap.containsKey(e.getTaskId()) && e.getStatus() != TaskType.TYPE_STATUS_REWARD)
                .count();
        if (count > 0) {
            throw new MwException(GameError.TASK_NO_FINISH.getCode(), "领取任务奖励时,任务未完成, roleId:" + roleId + "  chapterId:" + chapterId);
        }
        StaticTaskChapter staticTaskChapter = StaticChapterTaskDataMgr.getStaticTaskChapter(chapterId);
        if (Objects.isNull(staticTaskChapter)) {
            throw new MwException(GameError.NO_CONFIG, "章节配置不存在 roleId:", roleId, "chapter:", chapterId);
        }
        GamePb5.GetChapterAwardRs.Builder builder = GamePb5.GetChapterAwardRs.newBuilder();
        builder.addAllAward(rewardDataManager.sendReward(player, staticTaskChapter.getAwardList(), AwardFrom.GET_CHAPTER_TASK));
        chapterTask.getChapterStatus().put(chapterId, 1);
        int nextChapter = chapterTaskDataManager.getNextChapter(player);
        chapterTask.setChapter(nextChapter);
        chapterTaskDataManager.checkPlayerChapterTask(player);
        builder.setCurrentChapter(nextChapter);
        Map<Integer, CommonPb.Task> tasksPb = chapterTaskDataManager.getTasksPb(player);
        builder.addAllTask(tasksPb.values());
        builder.addAllCurTaskId(tasksPb.keySet());
        builder.setIsReceive(chapterTask.currentChapterIsReceive());
        return builder;
    }

    @Override
    @GmCmd("chapterTask")
    public void handleGmCmd(Player player, String... params) throws Exception {
        switch (params[0]) {
            case "setId":
                int param = Integer.parseInt(params[1]);
                gmSetPlayerChapter(player, param);
                break;
            case "finish":
                int taskId = Integer.parseInt(params[1]);
                Task task = player.chapterTask.getOpenTasks().get(taskId);
                if (Objects.isNull(task)) {
                    throw new MwException(GameError.PARAM_ERROR, "gm命令完成任务错误,任务不存在" + taskId);
                }
                StaticTask staticTask = StaticTaskDataMgr.getTaskById(task.getTaskId());
                if (Objects.isNull(staticTask)) {
                    throw new MwException(GameError.PARAM_ERROR, "gm命令完成任务错误,任务不存在" + taskId);
                }
                task.setSchedule(staticTask.getSchedule());
                chapterTaskDataManager.synTaskInfo(player, Lists.newArrayList(PbHelper.createTaskPb(task, staticTask.getType())));
                break;
            case "allSetId": {
                param = Integer.parseInt(params[1]);
                playerDataManager.getAllPlayer().values().stream().filter(Objects::nonNull).forEach(e -> {
                    gmSetPlayerChapter(e, param);
                });
                break;
            }
            default:
                break;
        }
    }


    /**
     * gm设置玩家的章节任务
     */
    private void gmSetPlayerChapter(Player player, int targetId) throws MwException {
        if (targetId <= 0) return;
        StaticTaskChapter staticTaskChapter = StaticChapterTaskDataMgr.getStaticTaskChapterMap().get(targetId);
        if (Objects.isNull(staticTaskChapter)) throw new MwException(GameError.PARAM_ERROR, "目标章节不存在");
        ChapterTask chapterTask = player.chapterTask;
        int chapterId = chapterTask.getChapterId();
        LogUtil.error("设置玩家章节 lordId:" + player.roleId + "  玩家当前章节：" + chapterId + "   目标章节：" + targetId);
        if (targetId > chapterId) {
            while (true) {
                int nextChapter = chapterTaskDataManager.getNextChapter(player);
                if (nextChapter > targetId || nextChapter == chapterTask.getChapterId()) {
                    break;
                }
                chapterTask.getChapterStatus().put(chapterTask.getChapterId(), 1);
                chapterTaskDataManager.checkPlayerChapterTask(player);
            }
        } else if (targetId < chapterId) {
            player.chapterTask = new ChapterTask();
            chapterTaskDataManager.checkPlayerChapterTask(player);
            gmSetPlayerChapter(player, targetId);
            return;
        }
        chapterTask.getOpenTasks().values().forEach(task -> {
            StaticTask staticTask = StaticTaskDataMgr.getTaskById(task.getTaskId());
            if (Objects.nonNull(staticTask)
                    && staticTask.getChapter() != 0
                    && staticTask.getChapter() < chapterTask.getChapterId()
                    && staticTask.getType() == TaskType.TYPE_MAIN) {
                task.setSchedule(staticTask.getSchedule());
                task.setStatus(TaskType.TYPE_STATUS_REWARD);
            }
        });
        LogUtil.error("设置玩家章节完成 lordId:" + player.roleId + "  玩家当前章节：" + chapterTask.getChapterId() + "   目标章节：" + targetId);
    }
}
