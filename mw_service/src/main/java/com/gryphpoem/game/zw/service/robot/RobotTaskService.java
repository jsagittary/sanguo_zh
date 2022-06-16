package com.gryphpoem.game.zw.service.robot;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticLordDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticTaskDataMgr;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.manager.RobotDataManager;
import com.gryphpoem.game.zw.manager.TaskDataManager;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.TaskType;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Robot;
import com.gryphpoem.game.zw.resource.domain.s.StaticGuidAward;
import com.gryphpoem.game.zw.resource.domain.s.StaticTask;
import com.gryphpoem.game.zw.resource.pojo.Task;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.service.TaskService;
import com.gryphpoem.game.zw.service.chapterTask.ChapterTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @Description 机器人任务相关服务类
 * @author TanDonghai
 * @date 创建时间：2017年10月23日 下午7:00:57
 *
 */
@Service
public class RobotTaskService {
    @Autowired
    private TaskDataManager taskDataManager;

    @Autowired
    private RobotDataManager robotDataManager;

    @Autowired
    private RewardDataManager rewardDataManager;

    @Autowired
    private TaskService taskService;

    /**
     * 自动领取当前已完成的任务奖励
     * 
     * @param player
     */
    public void autoTaskReward(Player player) {
        // 引导奖励处理
        guideReward(player);

        List<Task> finishedTask = getFinishedTask(player);
        for (Task task : finishedTask) {
            taskReward(player, task);
        }
    }

    /**
     * 新手引导奖励处理，机器人直接领取所有新手引导奖励
     * 
     * @param player
     */
    private void guideReward(Player player) {
        Robot robot = robotDataManager.getRobotMap().get(player.roleId);
        // 如果没有领取过，领取
        if (robot.getGuideIndex() <= 0) {
            for (StaticGuidAward sga : StaticLordDataMgr.getGuidAwardMap().values()) {
                if (!CheckNull.isEmpty(sga.getRewards())) {
                    rewardDataManager.sendReward(player, sga.getRewards(), AwardFrom.GUID_AWARD);
                }
            }
            robot.setGuideIndex(1);
        }
    }

    /**
     * 领取任务奖励
     * 
     * @param player
     * @param task
     */
    private void taskReward(Player player, Task task) {
        StaticTask staticTask = StaticTaskDataMgr.getTaskById(task.getTaskId());
        if (null == staticTask) {
            return;
        }

        try {
            DataResource.ac.getBean(ChapterTaskService.class).getChapterTaskAward(player.roleId, task.getTaskId());
        } catch (MwException e) {
            LogUtil.robot(e, "机器人领取任务奖励出错, robot:", player.roleId, ", task:", task);
        }
    }

    /**
     * 获取当前已完成但未领取奖励的任务
     * 
     * @param player
     * @return
     */
    private List<Task> getFinishedTask(Player player) {
        List<Task> finishedTask = new ArrayList<>();
//        // 将主线任务添加到玩家身上
//        Map<Integer, Task> taskMap = player.majorTasks;
//        if (taskMap.isEmpty()) {
//            initMajorTask(taskMap);
//        }
//
//        Iterator<Task> it = taskMap.values().iterator();
//        while (it.hasNext()) {
//            Task task = it.next();
//            int taskId = task.getTaskId();
//            StaticTask stask = StaticTaskDataMgr.getTaskById(taskId);
//            if (stask == null) {
//                continue;
//            }
//
//            // 特殊任务处理逻辑
//            specialTaskHandler(player, task, stask);
//
//            // 过滤已领取
//            if (task.getStatus() == TaskType.TYPE_STATUS_REWARD) {
//                // LogUtil.robot("机器人已领取奖励的任务, robot:", player.roleId, ", taskId:", task.getTaskId());
//            } else {
//                if (task.getSchedule() >= stask.getSchedule() && task.getStatus() == 0) {
//                    task.setStatus(TaskType.TYPE_STATUS_FINISH);
//                }
//
//                if (task.getStatus() == TaskType.TYPE_STATUS_FINISH) {
//                    finishedTask.add(task);
//                } else {
//                    // 刷新任务状态
//                    taskDataManager.currentMajorTask(player, task, stask);
//                }
//            }
//        }
        return finishedTask;
    }

    /**
     * 初始化主线任务
     * 
     * @param taskMap
     */
    private void initMajorTask(Map<Integer, Task> taskMap) {
        List<StaticTask> list = StaticTaskDataMgr.getInitMajorTask();
        if (null == list) {
            list = new ArrayList<>(1);
            list.add(StaticTaskDataMgr.getFirstTask());
        }
        for (StaticTask e : list) {
            Task task = new Task(e.getTaskId());
            taskMap.put(e.getTaskId(), task);
        }
        for (StaticTask e : StaticTaskDataMgr.getOpenList()) {
            Task task = new Task(e.getTaskId());
            taskMap.put(e.getTaskId(), task);
        }
    }

    /**
     * 特殊任务处理
     * 
     * @param player
     * @param task
     * @param stask
     */
    private void specialTaskHandler(Player player, Task task, StaticTask stask) {
        if (task.isFinished()) {
            // 以完成任务为触发条件的处理逻辑
        } else if (isCanSkipProcessTask(stask.getCond())) {
            // 机器人直接默认完成
            task.setSchedule(stask.getSchedule());
        }
    }

    /**
     * 是否是机器人可以直接跳过任务过程，记为完成状态的任务类型
     * 
     * @param taskType
     * @return
     */
    private boolean isCanSkipProcessTask(int taskType) {
        return taskType == TaskType.COND_CLICK || taskType == TaskType.COND_FIGHT_SHOW || taskType == TaskType.COND_31
                || taskType == TaskType.COND_20 || taskType == TaskType.COND_21 || taskType == TaskType.COND_24
                || taskType == TaskType.COND_25 || taskType == TaskType.COND_FREE_CD || taskType == TaskType.COND_BANDIT_LV_CNT;
    }
}
