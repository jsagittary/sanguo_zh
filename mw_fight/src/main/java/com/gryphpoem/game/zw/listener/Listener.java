package com.gryphpoem.game.zw.listener;

import java.util.EventListener;
import java.util.EventObject;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-12-30 22:46
 */
public interface Listener extends EventListener {
    /**
     * 监听器执行
     *
     * @param event
     */
    public void fireAfterEventInvoked(EventObject event);
}
