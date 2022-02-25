package com.gryphpoem.game.zw.gameplay.local.util;

/**
 * @ClassName DelayInvokeEnvironment.java
 * @Description 延迟队列运行执行环境
 * @author QiuKun
 * @date 2019年3月22日
 */
public interface DelayInvokeEnvironment {

    DelayQueue getDelayQueue();

    /**
     * 给跑秒定时器调用
     */
    default void runSec() {
        DelayQueue delayQueue = getDelayQueue();
        if (delayQueue != null) {
            delayQueue.runSec();
        }
    }
}
