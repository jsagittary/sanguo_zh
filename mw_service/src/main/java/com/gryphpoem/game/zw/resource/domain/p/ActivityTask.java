package com.gryphpoem.game.zw.resource.domain.p;

/**
 * @author xwind
 * @date 2021/3/4
 */
public class ActivityTask {
    private int uid;
    private int taskId;
    private int progress;
    private int count;
    private int drawCount;

    public ActivityTask() {
    }

    public ActivityTask(int taskId) {
        this.taskId = taskId;
    }

    public ActivityTask(int taskId, int progress, int count) {
        this.taskId = taskId;
        this.progress = progress;
        this.count = count;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getDrawCount() {
        return drawCount;
    }

    public void setDrawCount(int drawCount) {
        this.drawCount = drawCount;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }
}
