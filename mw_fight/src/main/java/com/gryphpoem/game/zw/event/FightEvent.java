package com.gryphpoem.game.zw.event;

import com.gryphpoem.game.zw.pojo.p.FightContextHolder;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-10 16:18
 */
public class FightEvent {
    /**
     * buff 触发事件
     */
    public static class BuffTriggerEvent {
        public FightContextHolder contextHolder;
        public int timing;
        public Object[] params;

        public BuffTriggerEvent(FightContextHolder contextHolder, int timing, Object[] params) {
            this.contextHolder = contextHolder;
            this.timing = timing;
            this.params = params;
        }
    }
}
