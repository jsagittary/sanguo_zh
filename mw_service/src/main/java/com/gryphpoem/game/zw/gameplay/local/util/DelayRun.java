package com.gryphpoem.game.zw.gameplay.local.util;

/**
 * @ClassName DelayRun.java
 * @Description 需要延迟执行的
 * @author QiuKun
 * @date 2019年3月22日
 */
public interface DelayRun {

    /**
     * 结束时间
     * 
     * @return
     */
    int deadlineTime();

    /**
     * 时间结束运行
     * 
     * @param runTime 当前运行的时间
     */
    void deadRun(int runTime, DelayInvokeEnvironment env);

    /**
     * 时间改变时调用
     * 
     * @param queue
     */
    default void changeDeadlineTime(DelayQueue queue) {
        queue.getQueue().remove(this);
        queue.getQueue().add(this);
    }
}
