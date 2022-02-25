package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.pb.CommonPb;

public class StaticElementServer {
    /**
     * 主服id
     */
    private int serverId;
    /**
     * 阵营
     */
    private int camp;

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public int getCamp() {
        return camp;
    }

    public void setCamp(int camp) {
        this.camp = camp;
    }

    public StaticElementServer(int serverId, int camp) {
        this.serverId = serverId;
        this.camp = camp;
    }

    public CommonPb.TwoInt createPb() {
        return CommonPb.TwoInt.newBuilder().setV1(serverId).setV2(camp).build();
    }
}
