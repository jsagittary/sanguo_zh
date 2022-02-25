package com.gryphpoem.game.zw.executor;

import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import com.gryphpoem.game.zw.core.executor.OrderedQueuePoolExecutor;
import com.gryphpoem.game.zw.core.structs.TasksQueue;
import com.gryphpoem.game.zw.task.AbstractTask;
import com.gryphpoem.game.zw.task.TaskQueuePool;

/**
 * @ClassName QueuePoolExecutor.java
 * @Description
 * @author QiuKun
 * @date 2019年4月30日
 */
public class QueuePoolExecutor extends ThreadPoolExecutor {
    private Logger log = LogManager.getLogger(OrderedQueuePoolExecutor.class);

    // 线程对应的多个任务队列
    private TaskQueuePool<Integer, AbstractTask> pool = new TaskQueuePool<>();

    private String name;

    private int maxQueueSize;

    private boolean isStoped = false;

    public QueuePoolExecutor(String name, int corePoolSize, int maxQueueSize) {
        super(corePoolSize, 2 * corePoolSize, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
                new CustomizableThreadFactory(name));
        this.name = name;
        this.maxQueueSize = maxQueueSize;
    }

    public QueuePoolExecutor(int corePoolSize) {
        this("queue-pool", corePoolSize, 10000);
    }

    /**
     * 增加执行任务
     * 
     * @param queueType
     * @param value
     * @return
     */
    public boolean addTask(int queueType, AbstractTask task) {
        boolean result = false;
        if (isStoped) {
            return result;
        }
        TasksQueue<AbstractTask> queue = pool.getTasksQueue(queueType);
        boolean run = false;
        synchronized (queue) {
            if (maxQueueSize > 0) {
                if (queue.size() > maxQueueSize) {
                    log.error("队列已满" + name + "(" + queueType + ")" + "抛弃指令!");
                    queue.clear();
                }
            }
            result = queue.add(task);
            if (result) {
                task.setTasksQueue(queue);
                if (queue.isProcessingCompleted()) {
                    queue.setProcessingCompleted(false);
                    run = true;
                }
            } else {
                log.error("队列添加任务失败 key:" + queueType);
            }
        }
        if (run) {
            execute(queue.poll());
        }
        return result;
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        AbstractTask task = (AbstractTask) r;
        TasksQueue<AbstractTask> queue = task.getTasksQueue();
        if (queue != null) {
            AbstractTask afterWork = null;
            synchronized (queue) {
                afterWork = queue.poll();
                if (afterWork == null) {
                    queue.setProcessingCompleted(true);
                } else {
                    execute(afterWork);
                }
            }
        }
    }

    public void registerTaskQueue(int type) {
        pool.registerTaskQueue(type);
    }

    public Set<Integer> getKeys() {
        return pool.getKeys();
    }

    /**
     * 获取剩余任务数量
     */
    public int getTaskCounts() {
        return super.getActiveCount() + pool.getTaskCounts();
    }

    public void stop() {
        isStoped = true;
    }
}
