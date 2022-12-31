package com.gryphpoem.game.zw.listener.impl;

import com.gryphpoem.game.zw.listener.Listener;

import java.util.EventObject;

/**
 * Description: 场次监听器
 * Author: zhangpeng
 * createTime: 2022-12-30 22:53
 */
public interface SessionListener extends Listener {
    @Override
    default void fireAfterEventInvoked(EventObject event) {
        sessionEnd();
    }

    void sessionEnd();
}
