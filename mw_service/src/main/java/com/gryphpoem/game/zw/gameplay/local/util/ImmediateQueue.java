package com.gryphpoem.game.zw.gameplay.local.util;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * @Author: GeYuanpeng
 * @Date: 2022/11/7 10:29
 */
public class ImmediateQueue<E extends ImmediateRun> {

    private final ImmediateInvokeEnvironment environment;
    private final Queue<E> queue;

    public ImmediateQueue(ImmediateInvokeEnvironment environment) {
        this.queue = new PriorityQueue<>(Comparator.comparing(ImmediateRun::getStartTime));
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
            // 取出队列的头元素(不从队列移除)
            E e = queue.peek();
            // 时间未到
            if (e.getStartTime() < now) {
                break;
            }
            // 时间已过
            if (e.getEndTime() > now) {
                remove(e);
            }
            // 取出队列的头元素(从队列移除)
            e = queue.poll();
            try {
                e.startRun(now, environment);
            } catch (Exception ex) {
                LogUtil.error(ex);
            }
        }
    }
}
