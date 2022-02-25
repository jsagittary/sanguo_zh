package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.pb.CommonPb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User:        zhoujie
 * Date:        2020/2/14 16:30
 * Description:
 */
public class ActRobinHood {

    /**
     * 活动奖励未领取
     */
    public static final int AWARD_STATUS_0 = 0;
    /**
     * 活动奖励已领取
     */
    public static final int AWARD_STATUS_1 = 1;

    /**
     * 活动id
     */
    private int activityId;
    /**
     * 领取状态
     */
    private int status;
    /**
     * key:任务 keyId, val: 任务数据
     */
    private Map<Integer, RobinHoodTask> robinHoodTaskMap = new HashMap<>();
    /**
     * 活动开始时间
     */
    private int beginTime;

    public ActRobinHood() {
    }

    /**
     * 初始化构造
     * @param activityId
     * @param beginTime
     */
    public ActRobinHood(int activityId,  int beginTime) {
        this.activityId = activityId;
        this.status = ActRobinHood.AWARD_STATUS_0;
        this.beginTime = beginTime;
    }

    /**
     * 反序列化
     * @param robinHood
     */
    public ActRobinHood(CommonPb.RobinHood robinHood) {
        this.activityId = robinHood.getActivityId();
        this.status = robinHood.getStatus();
        List<CommonPb.RobinHoodTask> taskList = robinHood.getTaskList();
        if (taskList != null && !taskList.isEmpty()) {
            for (CommonPb.RobinHoodTask robinHoodTask : taskList) {
                this.robinHoodTaskMap.put(robinHoodTask.getKeyId(), new RobinHoodTask(robinHoodTask));
            }
        }
        this.beginTime = robinHood.getBeginTime();
    }

    /**
     * 序列化
     * @return
     */
    public CommonPb.RobinHood ser() {
        CommonPb.RobinHood.Builder builder = CommonPb.RobinHood.newBuilder();
        builder.setActivityId(this.activityId);
        this.getRobinHoodTaskMap().values().forEach(rht -> builder.addTask(rht.ser()));
        builder.setStatus(this.status);
        builder.setBeginTime(this.beginTime);
        return builder.build();
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Map<Integer, RobinHoodTask> getRobinHoodTaskMap() {
        return robinHoodTaskMap;
    }

    public void setRobinHoodTaskMap(Map<Integer, RobinHoodTask> robinHoodTaskMap) {
        this.robinHoodTaskMap = robinHoodTaskMap;
    }

    public int getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(int beginTime) {
        this.beginTime = beginTime;
    }


}
