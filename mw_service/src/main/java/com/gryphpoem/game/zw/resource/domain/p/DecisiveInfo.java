package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.dataMgr.StaticBuildingDataMgr;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

/**
 * @author: ZhouJie
 * @date: Create in 2018-12-12 10:10
 * @description: 决战相关信息
 * @modified By:
 */
public class DecisiveInfo {

    private boolean decisive; // 战斗状态
    private long flyRole; // 击飞我的玩家
    private int flyTime; // 被击飞时间
    private int propTime; // 产出时间(决战指令)
    private boolean propStatus; // 产出状态(0: 生产中, 1: 可领取)

    public SerializePb.SerDecisiveInfo ser() {
        SerializePb.SerDecisiveInfo.Builder builder = SerializePb.SerDecisiveInfo.newBuilder();
        builder.setDecisive(decisive);
        builder.setFlyRole(flyRole);
        builder.setFlyTime(flyTime);
        builder.setPropTime(propTime);
        builder.setPropStatus(propStatus);
        return builder.build();
    }

    public void dser(SerializePb.SerDecisiveInfo ser) {
        if (ser.hasDecisive()) this.decisive = ser.getDecisive();
        if (ser.hasFlyRole()) this.flyRole = ser.getFlyRole();
        if (ser.hasFlyTime()) this.flyTime = ser.getFlyTime();
        if (ser.hasPropTime()) this.propTime = ser.getPropTime();
        if (ser.hasPropStatus()) this.propStatus = ser.getPropStatus();
    }

    /**
     * 初始化产出信息
     * 
     * @return
     */
    public void init() {
        if (propTime == 0 && !propStatus) {
            nextPropTime();
        }
    }

    /**
     * 检测产出状态
     */
    public void checkPropStatus() {
        int now = TimeHelper.getCurrentSecond();
        if (now >= propTime) {
            this.propStatus = true;
        }
    }

    /**
     * 下一次产出时间
     */
    public void nextPropTime() {
        int nextTime = (TimeHelper.getCurrentSecond() + (StaticBuildingDataMgr.getBobmConf().get(0) * TimeHelper.DAY_S));
        if (nextTime > 0) {
            this.propTime = nextTime;
            this.propStatus = false;
        }
    }

    public boolean isDecisive() {
        return decisive;
    }

    public void setDecisive(boolean decisive) {
        this.decisive = decisive;
    }

    public long getFlyRole() {
        return flyRole;
    }

    public void setFlyRole(long flyRole) {
        this.flyRole = flyRole;
    }

    public int getFlyTime() {
        return flyTime;
    }

    public void setFlyTime(int flyTime) {
        this.flyTime = flyTime;
    }

    public int getPropTime() {
        return propTime;
    }

    public boolean isPropStatus() {
        return propStatus;
    }

    public void setPropStatus(boolean propStatus) {
        this.propStatus = propStatus;
    }

}
