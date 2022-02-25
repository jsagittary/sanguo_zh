package com.gryphpoem.game.zw.task;

import com.gryphpoem.game.zw.core.structs.TasksQueue;
import com.gryphpoem.game.zw.core.util.LogUtil;

/**
 * @ClassName AbstractTask.java
 * @Description
 * @author QiuKun
 * @date 2019年4月30日
 */
public abstract class AbstractTask implements Runnable {

    protected long threadId;
    protected long createTime;

    private TasksQueue<AbstractTask> tasksQueue;

    public AbstractTask() {
        createTime = System.currentTimeMillis();
    }

    public long getThreadId() {
        return threadId;
    }

    public TasksQueue<AbstractTask> getTasksQueue() {
        return tasksQueue;
    }

    public void setTasksQueue(TasksQueue<AbstractTask> tasksQueue) {
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

    public abstract void work();

}
