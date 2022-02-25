package com.gryphpoem.game.zw.resource.pojo.world;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticGestapoPlan;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

/**
 * @Author: ZhouJie
 * @Date: Create in 2018-03-28 14:40
 * @Description: 盖世太保
 * @Modified By:
 */
public class Gestapo {
    private int GestapoId; // s_gestapo表的id
    private long roleId; // 角色
    private int endTime; // 存在结束时间点
    private int pos;// 位置
    private int status;// 状态 0: 空闲, 1:  战斗开启

    /**
     * 低级盖世太保
     */
    public static final int GESTAPO_ID_1 = 1;

    public Gestapo() {
    }

    public Gestapo(int pos, StaticGestapoPlan staticGestapoPlan, Player player) {
        setPos(pos);
        setRoleId(player.lord.getLordId());
        setGestapoId(staticGestapoPlan.getGestapoId());
        setEndTime(staticGestapoPlan.getExistenceTime() + TimeHelper.getCurrentSecond());
        setStatus(0);
    }

    public Gestapo(CommonPb.Gestapo pb) {
        setPos(pb.getPos());
        setRoleId(pb.getRoleId());
        setEndTime(pb.getEndTime());
        setGestapoId(pb.getGestapoId());
        setStatus(pb.getStatus());
    }

    public int getGestapoId() {
        return GestapoId;
    }

    public void setGestapoId(int gestapoId) {
        GestapoId = gestapoId;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
