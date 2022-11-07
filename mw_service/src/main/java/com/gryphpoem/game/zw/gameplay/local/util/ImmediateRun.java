package com.gryphpoem.game.zw.gameplay.local.util;

/**
 * @Author: GeYuanpeng
 * @Date: 2022/11/7 10:29
 */
public interface ImmediateRun {


    /**
     * 开始时间
     *
     * @return
     */
    int getStartTime();

    int getEndTime();

    /**
     * 立即运行
     *
     * @param runTime 当前运行的时间
     */
    void startRun(int runTime, ImmediateInvokeEnvironment env);

    /**
     * 时间改变时调用
     *
     * @param queue
     */
    default void changeStartTime(ImmediateQueue queue) {
        queue.getQueue().remove(this);
        queue.getQueue().add(this);
    }

}
