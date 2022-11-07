package com.gryphpoem.game.zw.gameplay.local.util;

/**
 * @Author: GeYuanpeng
 * @Date: 2022/11/7 10:28
 */
public interface ImmediateInvokeEnvironment {

    public ImmediateQueue getImmediateQueue();

    /**
     * 给跑秒定时器调用
     */
    default void runSec() {
        ImmediateQueue immediateQueue = getImmediateQueue();
        if (immediateQueue != null) {
            immediateQueue.runSec();
        }
    }
}
