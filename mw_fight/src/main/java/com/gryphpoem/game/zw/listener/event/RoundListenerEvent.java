package com.gryphpoem.game.zw.listener.event;

import java.util.EventObject;

/**
 * Description: 回合监听器
 * Author: zhangpeng
 * createTime: 2022-12-30 22:51
 */
public class RoundListenerEvent extends EventObject {
    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public RoundListenerEvent(Object source) {
        super(source);
    }
}
