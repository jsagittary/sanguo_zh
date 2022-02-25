package com.gryphpoem.game.zw.mgr;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.executor.DelayTaskWheelTimer;
import com.gryphpoem.game.zw.executor.DelayTaskWheelTimer.DelayTask;
import com.gryphpoem.game.zw.executor.DelayTaskWheelTimer.RemoteCallBack;
import com.gryphpoem.game.zw.executor.DelayTaskWheelTimer.RemoteCallBackParam;
import com.gryphpoem.game.zw.executor.ExcutorQueueType;
import com.gryphpoem.game.zw.executor.ExcutorType;
import com.gryphpoem.game.zw.executor.QueuePoolExecutor;
import com.gryphpoem.game.zw.task.AbstractTask;

/**
 * @ClassName ExecutorPoolMgr.java
 * @Description
 * @author QiuKun
 * @date 2019年4月30日
 */
public class ExecutorPoolMgr {

    private static ExecutorPoolMgr ins;

    private ExecutorPoolMgr() {
    }

    public static ExecutorPoolMgr getIns() {
        if (ins == null) {
            synchronized (ExecutorPoolMgr.class) {
                if (ins == null) {
                    ins = new ExecutorPoolMgr();
                    ins.init();
                }
            }
        }
        return ins;
    }

    // 线程池
    private Map<ExcutorType, QueuePoolExecutor> poolMap = new HashMap<>();

    /** 直接执行的线程池 */
    private ThreadPoolExecutor excutor;
    /** 延迟执行的任务检测线程 */
    private DelayTaskWheelTimer delayWheelTimer;

    private void init() {
        // 注册线程池
        for (ExcutorType t : ExcutorType.values()) {
            registerExcutor(t);
        }
        registerExcutorQueue(ExcutorType.LOGIC, ExcutorQueueType.LOGIC_MAIN);
        registerExcutorQueue(ExcutorType.MSG, ExcutorQueueType.MSG_SEND);
        registerExcutorQueue(ExcutorType.MSG, ExcutorQueueType.MSG_RECV);

        registerExcutorQueue(ExcutorType.SAVE, ExcutorQueueType.SAVE_1);
        registerExcutorQueue(ExcutorType.SAVE, ExcutorQueueType.SAVE_2);
        registerExcutorQueue(ExcutorType.SAVE, ExcutorQueueType.SAVE_3);
        registerExcutorQueue(ExcutorType.SAVE, ExcutorQueueType.SAVE_4);
        registerExcutorQueue(ExcutorType.SAVE, ExcutorQueueType.SAVE_5);
        registerExcutorQueue(ExcutorType.SAVE, ExcutorQueueType.SAVE_6);
        registerExcutorQueue(ExcutorType.SAVE, ExcutorQueueType.SAVE_7);
        registerExcutorQueue(ExcutorType.SAVE, ExcutorQueueType.SAVE_8);

        // 直接运行的线程池
        int cpuNum = Runtime.getRuntime().availableProcessors();
        excutor = new ThreadPoolExecutor(2, cpuNum, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        // 开启延迟队列线程,主要用于移除 过期的
        delayWheelTimer = new DelayTaskWheelTimer("delayTask-");
    }

    /**
     * 注册线程池
     * 
     * @param type
     */
    public void registerExcutor(ExcutorType type) {
        poolMap.put(type, new QueuePoolExecutor(type.getName(), type.getCorePoolSize(), -1));
    }

    /**
     * 注册队列
     * 
     * @param excutorType
     * @param queueType
     */
    public void registerExcutorQueue(ExcutorType excutorType, ExcutorQueueType queueType) {
        registerTaskQueue(excutorType, queueType.getType());
    }

    /**
     * 注册队列
     * 
     * @param excutorType
     * @param type
     */
    public void registerTaskQueue(ExcutorType excutorType, int type) {
        poolMap.get(excutorType).registerTaskQueue(type);
    }

    /**
     * 添加任务
     * 
     * @param type
     * @param queueType
     * @param task
     */
    public void addTask(ExcutorType type, ExcutorQueueType queueType, AbstractTask task) {
        addTask(type, queueType.getType(), task);
    }

    public void addTask(ExcutorType type, int queueType, AbstractTask task) {
        if (!poolMap.containsKey(type)) {
            LogUtil.error("未知的逻辑处理类型，type:" + type);
            return;
        }
        poolMap.get(type).addTask(queueType, task);
    }

    public long addRemoteCallBack(RemoteCallBack callBack, RemoteCallBackParam param) {
        if (delayWheelTimer != null) {
            return delayWheelTimer.addRemoteCallBack(callBack, param);
        }
        return -1;
    }

    /**
     * 添加需延迟执行的任务
     * 
     * @param task
     */
    public long addDelayTask(DelayTask task, long delay, TimeUnit unit) {
        if (delayWheelTimer != null) {
            delayWheelTimer.addDelayTask(task, delay, unit);
            return task.getTaskId();
        }
        return -1;
    }

    /**
     * 获取延迟队列
     * 
     * @param taskId
     * @return
     */
    public DelayTask getDelayTask(long taskId) {
        if (delayWheelTimer != null) {
            return delayWheelTimer.getDelayTask(taskId);
        }
        return null;
    }

    /**
     * 直接异步运行的线程
     * 
     * @param r
     */
    public void directExecute(Runnable r) {
        excutor.execute(r);
    }

    /**
     * 线程池停止前，先执行完所有任务
     */
    public void stop() throws InterruptedException {
        if (!excutor.isShutdown()) {
            excutor.shutdown();
        }

        if (delayWheelTimer != null) {
            delayWheelTimer.stopped();
        }

        long begin = System.currentTimeMillis();
        LogUtil.debug("stop ....");
        Map<ExcutorType, CountDownLatch> stopMap = new HashMap<>();
        for (Entry<ExcutorType, QueuePoolExecutor> entry : poolMap.entrySet()) {
            ExcutorType key = entry.getKey();
            QueuePoolExecutor queuePool = entry.getValue();

            Set<Integer> types = queuePool.getKeys();
            CountDownLatch cl = new CountDownLatch(types.size());
            // 为每个队列加入停止计数器后，不再接收任务
            for (int queueType : types) {
                queuePool.addTask(queueType, new AbstractTask() {
                    @Override
                    public void work() {
                        cl.countDown();
                    }
                });
            }
            queuePool.stop();

            stopMap.put(key, cl);
        }

        for (Entry<ExcutorType, CountDownLatch> entry : stopMap.entrySet()) {
            try {
                // 等待(最长60s)所有任务执行完成
                entry.getValue().await(60, TimeUnit.SECONDS);
                poolMap.get(entry.getKey()).shutdown();
            } catch (InterruptedException e) {
                LogUtil.error("QueuePoolExecutor Stop Error", e);
            }
        }

        long end = System.currentTimeMillis();
        LogUtil.debug("线程池停止完毕,用时:" + (end - begin) + " ms");

        poolMap.clear();
    }

}
