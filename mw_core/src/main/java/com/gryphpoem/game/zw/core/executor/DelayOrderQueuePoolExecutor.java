package com.gryphpoem.game.zw.core.executor;

import com.gryphpoem.game.zw.core.net.ConnectServer;
import com.gryphpoem.game.zw.core.util.ChannelUtil;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.work.WWork;
import com.gryphpoem.game.zw.pb.BasePb;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @program: empire_cn
 * @description:
 * @author: zhou jie
 * @create: 2020-06-15 14:35
 */
public class DelayOrderQueuePoolExecutor {

    /**
     * 延迟任务
     * <roleId, <taskId, Task>>
     */
    private final Map<Long, Map<Long, DelayTask>> taskMap = new ConcurrentHashMap<>();
    /**
     * 时间轮
     */
    private final HashedWheelTimer hashedWheelTimer;
    /**
     * 连接服务
     */
    private ConnectServer connectServer;

    /**
     * 构造方法
     *
     * @param prefix        前缀
     * @param connectServer 连接服务注册
     */
    public DelayOrderQueuePoolExecutor(final String prefix, ConnectServer connectServer) {
        this.hashedWheelTimer = new HashedWheelTimer(new CustomizableThreadFactory(prefix), 100, TimeUnit.MILLISECONDS);
        this.connectServer = connectServer;
    }

    /**
     * 延迟任务放入执行队列
     *
     * @param delayTask 将延迟任务放入执行队列
     */
    public void addTask(DelayTask delayTask) {
        this.connectServer.sendExcutor.addTask(ChannelUtil.getChannelId(delayTask.getCtx()), delayTask);
    }

    /**
     * 添加延迟任务队列
     *
     * @param deTask 延迟任务
     * @param delay  延迟时间
     * @param unit   TimeUnit
     */
    public void addDelayTask(DelayTask deTask, long delay, TimeUnit unit) {
        // 记录延迟任务
        Map<Long, DelayTask> delayTaskMap = this.taskMap.computeIfAbsent(deTask.roleId, (k) -> new ConcurrentHashMap<>());
        // LogUtil.error("添加延迟任务, task:", deTask);
        // 延迟执行
        Timeout timeout = hashedWheelTimer.newTimeout(deTask, delay, unit);
        // 设置句柄
        deTask.setTimeout(timeout);
        delayTaskMap.put(deTask.taskId, deTask);
    }

    /**
     * 添加推送延迟任务
     *
     * @param syncPlayerTask 同步玩家任务
     */
    public void addSyncDelayTask(SyncPlayerTask syncPlayerTask) {
        addDelayTask(syncPlayerTask, 50, TimeUnit.MILLISECONDS);
    }

    /**
     * 添加推送延迟任务
     *
     * @param ctx    channel上下文
     * @param roleId 玩家id
     * @param msg    协议
     * @return 任务id
     */
    public long addSyncDelayTask(ChannelHandlerContext ctx, long roleId, BasePb.Base msg) {
        SyncPlayerTask syncPlayerTask = new SyncPlayerTask(this, roleId, ctx, msg);
        // 添加推送延迟任务
        addSyncDelayTask(syncPlayerTask);
        return syncPlayerTask.getTaskId();
    }

    /**
     * 移除延迟任务
     *
     * @param deTask 延迟任务
     * @return
     */
    private DelayTask removeDelayTask(DelayTask deTask) {
        Map<Long, DelayTask> delayTaskMap = this.taskMap.computeIfAbsent(deTask.roleId, (k) -> new ConcurrentHashMap<>());
        return delayTaskMap.remove(deTask.taskId);
    }

    /**
     * 停止时间轮
     */
    public void stopped() {
        hashedWheelTimer.stop();
    }

    /**
     * 刷新玩家的id
     *
     * @param roleId 玩家id
     */
    public void refreshDelayTask(long roleId) {
        Optional.ofNullable(this.taskMap.get(roleId))
                .ifPresent(delayTask -> {
                    delayTask.values()
                            .forEach(task -> {
                                // 移除延迟任务
                                Optional.ofNullable(removeDelayTask(task))
                                        // 添加任务到执行队列
                                        .ifPresent(remove -> {
                                            Timeout timeout = remove.getTimeout();
                                            if (timeout.cancel()) {
                                                addTask(task);
                                            } else {
                                                LogUtil.error("取消延迟任务失败: task:", remove.getTaskId(), ", ");
                                            }
                                        });
                            });
                });
    }


    /**
     * 同步玩家任务
     */
    public static class SyncPlayerTask extends DelayTask {

        public SyncPlayerTask(DelayOrderQueuePoolExecutor delayOrderQueuePoolExecutor, long roleId, ChannelHandlerContext ctx, BasePb.Base msg) {
            super(delayOrderQueuePoolExecutor, roleId, ctx, msg);
        }

        @Override
        public void work() {
            super.run();
        }
    }


    /**
     * 延迟任务
     */
    private static abstract class DelayTask extends WWork implements TimerTask {

        private final static AtomicLong TASK_ID = new AtomicLong();

        /**
         * 唯一id
         */
        private final long taskId;
        /**
         * 玩家id
         */
        private final long roleId;
        /**
         * 判断有效性
         */
        private volatile boolean valid;
        /**
         * 句柄
         */
        private Timeout timeout;
        /**
         * 延时执行
         */
        private DelayOrderQueuePoolExecutor delayOrderQueuePoolExecutor;

        public long getTaskId() {
            return taskId;
        }

        public long getRoleId() {
            return roleId;
        }

        public boolean isValid() {
            return valid;
        }

        public void setTimeout(Timeout timeout) {
            this.timeout = timeout;
        }

        public Timeout getTimeout() {
            return timeout;
        }

        public DelayTask(DelayOrderQueuePoolExecutor delayOrderQueuePoolExecutor, long roleId, ChannelHandlerContext ctx, BasePb.Base msg) {
            super(ctx, msg);
            this.valid = true;
            this.taskId = TASK_ID.incrementAndGet();
            this.roleId = roleId;
            this.delayOrderQueuePoolExecutor = delayOrderQueuePoolExecutor;
        }

        @Override
        public void run(Timeout timeout) {
            // 任务取消了
            if (timeout.isCancelled()) {
                return;
            }
            // 定时器到了执行
            execute();
            // 移除任务
            delayOrderQueuePoolExecutor.removeDelayTask(this);
        }

        /**
         * 执行逻辑
         */
        public synchronized void execute() {
            if (!valid) {
                return;
            }
            valid = false;
            // LogUtil.error("执行延迟任务, task:", this);
            // 添加执行的任务
            delayOrderQueuePoolExecutor.addTask(this);
        }
    }
}