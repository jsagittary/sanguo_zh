package com.gryphpoem.game.zw.service.activity.task.abs;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.p.ActivityTask;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.service.GmCmdService;
import com.gryphpoem.game.zw.service.TaskFinishService;
import com.gryphpoem.game.zw.service.activity.AbsSimpleActivityService;
import com.gryphpoem.game.zw.service.activity.PersonalActService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 活动任务处理基础service
 *
 */
public abstract class AbsTaskActivityService extends AbsSimpleActivityService implements TaskFinishService, GmCmdService {

    /**
     * 获取活动任务列表
     *
     * @param player
     * @param actKeyId
     * @param actType
     * @param process
     * @param errorCode
     * @param index
     * @param functionId
     * @return
     * @throws MwException
     */
    protected List<ActivityTask> getTaskList(Player player, Integer actKeyId, int actType, boolean process, boolean errorCode, int index, int functionId) throws MwException {
        List<ActivityTask> taskList;
        try {
            if (Arrays.stream(getActivityType()).noneMatch(type -> type == actType)) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "参数错误, service无法处理此类型, activityType: ", actType);
            }
            if (!process && !StaticFunctionDataMgr.funcitonIsOpen(player, functionId)) {
                throw new MwException(GameError.FUNCTION_LOCK.getCode(), "function未解锁, roleId:", player.roleId);
            }

            ActivityBase ab = PersonalActService.getActivityBase(actKeyId, actType);
            Activity activity = PersonalActService.getActivity(actKeyId, actType, player);
            if (CheckNull.isNull(activity) || CheckNull.isNull(ab)) {
                throw new MwException(GameError.ACTIVITY_NO_EXIST.getCode(), "活动不存在, activityType: ", actType);
            }

            taskList = activity.getDayTasks().get(index);
            if (CheckNull.isNull(taskList))
                taskList = new ArrayList<>();
            checkActivityTaskData(activity.getActivityId(), actType, player.lord.getLordId(), taskList);
            PersonalActService.savePersonalActs(actType, ab.getPlanKeyId(), player, this.getClass());
            if (CheckNull.nonEmpty(taskList)) {
                activity.getDayTasks().put(index, taskList);
            }
        } catch (MwException e) {
            if (!errorCode)
                return null;
            throw e;
        }

        return taskList;
    }

    public abstract void checkActivityTaskData(int actId,  int actType, long lordId, List<ActivityTask> taskList);
}
