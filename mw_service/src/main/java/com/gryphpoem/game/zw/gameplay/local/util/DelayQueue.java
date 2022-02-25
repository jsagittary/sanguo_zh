package com.gryphpoem.game.zw.gameplay.local.util;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * @ClassName DelayQueue.java
 * @Description 延迟队列
 * @author QiuKun
 * @date 2019年3月22日
 */
public class DelayQueue<E extends DelayRun> {

    private final DelayInvokeEnvironment environment;
    private final Queue<E> queue;

    public DelayQueue(DelayInvokeEnvironment environment) {
        this.queue = new PriorityQueue<>(Comparator.comparing(DelayRun::deadlineTime));
        this.environment = environment;
    }

    public boolean add(E e) {
        return queue.offer(e);
    }

    public boolean remove(E e) {
        return queue.remove(e);
    }

    public Queue<E> getQueue() {
        return queue;
    }
    
    public void clearQueue(){
        this.queue.clear();
    }

    /**
     * 跑秒定时器
     */
    public void runSec() {
        int now = TimeHelper.getCurrentSecond();

        while (!queue.isEmpty()) {
            E e = queue.peek();
            // 时间未到
            if (e.deadlineTime() > now) {
                break;
            }
            e = queue.poll();
            try {
                e.deadRun(now, environment);
            } catch (Exception ex) {
                LogUtil.error(ex);
            }
        }
    }
}
