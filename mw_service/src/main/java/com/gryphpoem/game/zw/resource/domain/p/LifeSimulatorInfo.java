package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.pb.CommonPb;

/**
 * 人生模拟器记录
 *
 * @Author: GeYuanpeng
 * @Date: 2022/10/26 17:14
 */
public class LifeSimulatorInfo {

    private int type;  // 模拟器类型

    private long stepId; // 进行到哪一步了

    private int addDate; // 城镇事件刷新的日期(秒)

    private int pauseTime; // 事件暂停的时间点(秒)

    private int delay; // 到下一步的延时(天)

    private int bindType; // 1-绑定的建筑; 2-绑定的NPC

    private int bindId; // 模拟器绑定的建筑或NPC的id

    public LifeSimulatorInfo() {
    }

    public LifeSimulatorInfo(int type, int bindType, int bindId) {
        this.type = type;
        this.bindType = bindType;
        this.bindId = bindId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getStepId() {
        return stepId;
    }

    public void setStepId(long stepId) {
        this.stepId = stepId;
    }

    public int getAddDate() {
        return addDate;
    }

    public void setAddDate(int addDate) {
        this.addDate = addDate;
    }

    public int getPauseTime() {
        return pauseTime;
    }

    public void setPauseTime(int pauseTime) {
        this.pauseTime = pauseTime;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getBindType() {
        return bindType;
    }

    public void setBindType(int bindType) {
        this.bindType = bindType;
    }

    public int getBindId() {
        return bindId;
    }

    public void setBindId(int bindId) {
        this.bindId = bindId;
    }

    public CommonPb.LifeSimulatorInfoPb ser() {
        CommonPb.LifeSimulatorInfoPb.Builder builder = CommonPb.LifeSimulatorInfoPb.newBuilder();
        builder.setType(this.type);
        builder.setStepId(this.stepId);
        builder.setAddDate(this.addDate);
        builder.setPauseTime(this.pauseTime);
        builder.setDelay(this.delay);
        builder.setBindType(this.bindType);
        builder.setBindId(this.bindId);
        return builder.build();
    }

    public LifeSimulatorInfo dser(CommonPb.LifeSimulatorInfoPb pb) {
        this.type = pb.getType();
        this.stepId = pb.getStepId();
        this.addDate = pb.getAddDate();
        this.pauseTime = pb.getPauseTime();
        this.delay = pb.getDelay();
        this.bindType = pb.getBindType();
        this.bindId = pb.getBindId();
        return this;
    }

}
