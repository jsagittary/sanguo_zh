package com.gryphpoem.game.zw.resource.pojo.world;

import com.gryphpoem.game.zw.pb.CommonPb;

/**
 * @ClassName CabinetLead.java
 * @Description 点兵统领
 * @author QiuKun
 * @date 2017年7月17日
 */
public class CabinetLead {
    // id_roleId_camp
    private int cabinetPlanId; // cabinetPlan表的id
    private long roleId; // 角色
    private int camp; // 阵营
    private int pos;// 位置

    public CabinetLead() {
    }

    public CabinetLead(CommonPb.CabinetLead pb) {
        setPos(pb.getPos());
        setRoleId(pb.getRoleId());
        setCamp(pb.getCamp());
        setCabinetPlanId(pb.getCabinetPlanId());
    }

    public int getCabinetPlanId() {
        return cabinetPlanId;
    }

    public void setCabinetPlanId(int cabinetPlanId) {
        this.cabinetPlanId = cabinetPlanId;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getCamp() {
        return camp;
    }

    public void setCamp(int camp) {
        this.camp = camp;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

}
