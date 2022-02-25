package com.gryphpoem.game.zw.resource.pojo;

import com.gryphpoem.game.zw.dataMgr.StaticTaskDataMgr;
import com.gryphpoem.game.zw.resource.constant.TaskType;
import com.gryphpoem.game.zw.resource.domain.s.StaticTask;

/**
 * @ClassName Task.java
 * @Description 任务类
 * @author TanDonghai
 * @date 创建时间：2017年3月21日 下午2:06:54
 *
 */
public class Task {
    private int taskId;
    private long schedule;// 进度
    private int status; // 完成状态 详细见TaskType状态
    private int accept;

    public Task() {
    }

    public Task(int taskId) {
        this.taskId = taskId;
    }

    public Task(int taskId, long schedule, int status, int accept) {
        this.taskId = taskId;
        this.schedule = schedule;
        this.status = status;
        this.accept = accept;
    }

    public boolean isFinished() {
        return status == TaskType.TYPE_STATUS_FINISH || status == TaskType.TYPE_STATUS_REWARD;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public long getSchedule() {
        return schedule;
    }

    public void setSchedule(long schedule) {
        this.schedule = schedule;
        StaticTask stask = StaticTaskDataMgr.getTaskById(taskId);
        if (stask != null && status == 0 && schedule >= stask.getSchedule()) {
            setStatus(TaskType.TYPE_STATUS_FINISH);
        }
    }

    public void setSchedule0(long schedule){
        this.schedule = schedule;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getAccept() {
        return accept;
    }

    public void setAccept(int accept) {
        this.accept = accept;
    }

    @Override
    public String toString() {
        return "Task [taskId=" + taskId + ", schedule=" + schedule + ", status=" + status + ", accept=" + accept + "]";
    }

}
