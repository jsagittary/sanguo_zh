package com.gryphpoem.game.zw.gameplay.local.world.dominate;

/**
 * Description: 雄踞一方都督信息
 * Author: zhangpeng
 * createTime: 2022-11-22 15:29
 */
public class DominateSideGovernor {

    private long roleId;                  // 玩家id
    private long time;                   // 成为都督的时间
    private int cityId;                  // 城池id

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
}
