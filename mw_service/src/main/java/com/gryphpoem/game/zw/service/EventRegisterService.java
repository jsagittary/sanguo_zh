package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.eventbus.EventBus;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-08-17 17:31
 */
public interface EventRegisterService {
    default void registerEvent() {
        EventBus.getDefault().register(this);
    }
}
