package com.gryphpoem.game.zw.gameplay.local.world.dominate;

import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.pojo.GamePb;

/**
 * Description: 雄踞一方都督信息
 * Author: zhangpeng
 * createTime: 2022-11-22 15:29
 */
public class DominateSideGovernor implements GamePb<SerializePb.SerDominateSideGovernor> {

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

    public DominateSideGovernor() {}

    public DominateSideGovernor(long roleId, long time, int cityId) {
        this.roleId = roleId;
        this.time = time;
        this.cityId = cityId;
    }

    public DominateSideGovernor(SerializePb.SerDominateSideGovernor ser) {
        this.roleId = ser.getRoleId();
        this.time = ser.getTime();
        this.cityId = ser.getCityId();
    }

    @Override
    public SerializePb.SerDominateSideGovernor createPb(boolean isSaveDb) {
        SerializePb.SerDominateSideGovernor.Builder builder = SerializePb.SerDominateSideGovernor.newBuilder();
        builder.setCityId(this.cityId);
        builder.setTime(time);
        builder.setRoleId(roleId);
        return builder.build();
    }
}
