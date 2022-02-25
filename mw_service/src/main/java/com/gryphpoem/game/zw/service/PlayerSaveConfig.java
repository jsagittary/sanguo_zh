package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.resource.domain.Player;

/**
 * @ClassName PlayerSaveConfig.java
 * @Description 玩家保存配置
 * @author QiuKun
 * @date 2018年11月8日
 */
public interface PlayerSaveConfig {

    /*-------------时间单位都是秒值--------------*/

    /**
     * 一天的秒数
     */
    public int DAY_SECOND = 24 * 60 * 60;
    /**
     * 一周的秒数
     */
    public int WEEK_SECOND = 7 * DAY_SECOND;
    /**
     * 一个月的秒数
     */
    public int MONTH_SECOND = 4 * WEEK_SECOND;

    /**
     * 在线玩家保存的周期
     */
    public int ONLINE_PLAYER_SAVE_DELAY_PERIOD = 10 * 60;

    /**
     * 1天以内的离线玩家保存周期
     */
    public int DAY_OFFLINE_PLAYER_SAVE_PERIOD = 3 * 60 * 60;

    /**
     * 大于1天小于1周的离线玩家保存周期
     */
    public int WEEK_OFFLINE_PLAYER_SAVE_PERIOD = 9 * 60 * 60;

    /**
     * 大于1周小于1个月的离线玩家保存周期
     */
    public int MONTH_OFFLINE_PLAYER_SAVE_PERIOD = 24 * 60 * 60;

    /**
     * 大于1个月的离线玩家保存周期
     */
    public int OVER_MONTH_OFFLINE_PLAYER_SAVE_PERIOD = 7 * 24 * 60 * 60;

    /**
     * 单次保存的数量
     */
    public int ONCE_SAVE_CNT = 500;

    /**
     * 这个玩家本次是否可以保存
     * 
     * @param player
     * @return
     */
    public static boolean isCanSave(Player player, int now) {
        if (player.immediateSave) { // 带有立刻保存标记的玩家
            return true;
        }
        int saveInterval = now - player.lastSaveTime;
        if (player.isLogin) { // 在线玩家
            return saveInterval >= ONLINE_PLAYER_SAVE_DELAY_PERIOD;
        } else {// 离线玩家
            int offTime = now - player.lord.getOffTime();
            if (offTime < DAY_SECOND) {
                return saveInterval >= DAY_OFFLINE_PLAYER_SAVE_PERIOD;
            } else if (offTime >= DAY_SECOND && offTime < WEEK_SECOND) {
                return saveInterval >= WEEK_OFFLINE_PLAYER_SAVE_PERIOD;
            } else if (offTime >= WEEK_SECOND && offTime < MONTH_SECOND) {
                return saveInterval >= WEEK_OFFLINE_PLAYER_SAVE_PERIOD;
            } else {
                return saveInterval >= OVER_MONTH_OFFLINE_PLAYER_SAVE_PERIOD;
            }
        }
    }

}
