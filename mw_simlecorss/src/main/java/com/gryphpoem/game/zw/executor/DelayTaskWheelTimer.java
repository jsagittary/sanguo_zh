package com.gryphpoem.game.zw.executor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import com.gryphpoem.game.zw.mgr.ExecutorPoolMgr;
import com.gryphpoem.game.zw.task.AbstractTask;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

/**
 * 延迟时间轮定时器
 */
public class DelayTaskWheelTimer {
    /** <taskId,DelayTask> */
    private final Map<Long, DelayTask> taskMap = new ConcurrentHashMap<>();
    /** 时间轮 */
    private final HashedWheelTimer hashedWheelTimer;

    public DelayTaskWheelTimer(final String prefix) {
        hashedWheelTimer = new HashedWheelTimer(new CustomizableThreadFactory(prefix), 100, TimeUnit.MILLISECONDS);
    }

    /**
     * 添加远程回调
     * 
     * @param callBack
     * @return 返回任务id
     */
    public long addRemoteCallBack(RemoteCallBack callBack, RemoteCallBackParam param) {
        RemoteCallBackTask delayTask = new RemoteCallBackTask(this, param, callBack);
        delayTask.setType(ExcutorType.LOGIC, ExcutorQueueType.LOGIC_MAIN); // 设置到主队列进行执行
        long taskId = delayTask.getTaskId();
        taskMap.put(taskId, delayTask);
        addDelayTask(delayTask, 3000, TimeUnit.MICROSECONDS);
        return taskId;
    }

    /**
     * 添加延迟任务队列
     * 
     * @param deTask
     * @param delay
     * @param unit
     */
    public void addDelayTask(DelayTask deTask, long delay, TimeUnit unit) {
        taskMap.put(deTask.taskId, deTask);
        hashedWheelTimer.newTimeout(deTask, 3000, TimeUnit.MICROSECONDS); // 延迟执行
    }

    public DelayTask getDelayTask(long taskId) {
        return taskMap.get(taskId);
    }

    public void stopped() {
        hashedWheelTimer.stop();
    }

    /**
     * 远程回调
     *
     */
    public static interface RemoteCallBack {
        /**
         * 
         * @param param 运行的需要参数,可以为null
         */
        public void run(RemoteCallBackParam param);
    }

    /**
     * 远程回调的参数
     *
     */
    public static interface RemoteCallBackParam {

    }

    /**
     * 远程回调任务
     */
    public static class RemoteCallBackTask extends DelayTask {
        private RemoteCallBackParam remoteCallBackParam;
        private RemoteCallBack callBack;

        public RemoteCallBackTask(DelayTaskWheelTimer delayTaskWheelTimer, RemoteCallBackParam remoteCallBackParam,
                RemoteCallBack callBack) {
            super(delayTaskWheelTimer);
            this.remoteCallBackParam = remoteCallBackParam;
            this.callBack = callBack;
        }

        @Override
        public void work() {
            if (callBack != null) {
                callBack.run(remoteCallBackParam);
            }
        }
    }

    /**
     * 延迟任务
     *
     */
    public static abstract class DelayTask extends AbstractTask implements TimerTask {

        private final static AtomicLong TASK_ID = new AtomicLong();

        private final long taskId; // 任务id
        private volatile boolean valid; // 是否有效(默认有效)
        protected DelayTaskWheelTimer delayTaskWheelTimer;

        private ExcutorType excutorType;
        private ExcutorQueueType queueType;

        public DelayTask(DelayTaskWheelTimer delayTaskWheelTimer) {
            this.valid = true;
            this.taskId = TASK_ID.incrementAndGet();
            this.delayTaskWheelTimer = delayTaskWheelTimer;
        }

        public long getTaskId() {
            return taskId;
        }

        public boolean hasType() {
            return this.excutorType != null && this.queueType != null;
        }

        public void setType(ExcutorType excutorType, ExcutorQueueType queueType) {
            this.excutorType = excutorType;
            this.queueType = queueType;
        }

        public boolean checkValid() {
            return valid;
        }

        @Override
        public void run(Timeout timeout) throws Exception {
            // 定时器到了执行
            if (!(timeout.task() instanceof RemoteCallBackTask)) { // 不是RemoteCallBackTask就执行
                execute();
            }
            // 移除任务
            delayTaskWheelTimer.taskMap.remove(this.taskId);
        }

        public synchronized void execute() {
            if (!valid) {
                return;
            }
            valid = false;
            if (hasType()) {
                ExecutorPoolMgr.getIns().addTask(excutorType, queueType, this);
            } else {
                ExecutorPoolMgr.getIns().directExecute(this);
            }
        }

    }
}
