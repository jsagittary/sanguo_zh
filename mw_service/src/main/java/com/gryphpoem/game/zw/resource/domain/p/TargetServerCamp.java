package com.gryphpoem.game.zw.resource.domain.p;

/**
 * 目标区服信息 存储源区服id+camp -> 目录区服id
 */
public class TargetServerCamp {

    private int originServerId; // 最源区服id
    private int camp; // 阵营
    private int targetServerId;// 目标区服id

    public TargetServerCamp() {
    }

    public TargetServerCamp(int originServerId, int camp, int targetServerId) {
        this.originServerId = originServerId;
        this.camp = camp;
        this.targetServerId = targetServerId;
    }

    public int getOriginServerId() {
        return originServerId;
    }

    public void setOriginServerId(int originServerId) {
        this.originServerId = originServerId;
    }

    public void setCamp(int camp) {
        this.camp = camp;
    }

    public void setTargetServerId(int targetServerId) {
        this.targetServerId = targetServerId;
    }

    public int getCamp() {
        return camp;
    }

    public int getTargetServerId() {
        return targetServerId;
    }

    public String prettyPrint() {
        return originServerId + " " + camp + " -> " + targetServerId;
    }

    @Override
    public String toString() {
        return "TargetServerCamp [originServerId=" + originServerId + ", camp=" + camp + ", targetServerId="
                + targetServerId + "]";
    }

}
