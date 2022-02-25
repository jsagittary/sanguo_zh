package com.gryphpoem.game.zw.core.work;

import com.gryphpoem.game.zw.core.structs.TasksQueue;
import com.gryphpoem.game.zw.core.util.LogUtil;

public abstract class AbstractWork implements Runnable {

    protected long threadId;
    protected long createTime;

    private TasksQueue<AbstractWork> tasksQueue;

    public AbstractWork() {
        createTime = System.currentTimeMillis();
    }

    public TasksQueue<AbstractWork> getTasksQueue() {
        return tasksQueue;
    }

    public void setTasksQueue(TasksQueue<AbstractWork> tasksQueue) {
        this.tasksQueue = tasksQueue;
    }

    @Override
    public void run() {
        threadId = Thread.currentThread().getId();
        long start = System.currentTimeMillis();
        try {
            work();
            long end = System.currentTimeMillis();
            long interval = end - start;
            if (interval >= 1000) { // 消息处理超过1s的时候进行打印
                long liveTime = end - createTime;
                if (interval < 3000) {
                    LogUtil.warn("work: " + this.toString() + ",interval: " + interval + ",liveTime: " + liveTime);
                } else {
                    LogUtil.error("work: " + this.toString() + ",interval: " + interval + ",liveTime: " + liveTime
                            + ", queueSize:" + (tasksQueue != null ? tasksQueue.size() : 0));
                }
            }
        } catch (Throwable e) {
            LogUtil.error("run work execute exception. action : " + this.toString(), e);
        }
    }

    public void work(){}

}
