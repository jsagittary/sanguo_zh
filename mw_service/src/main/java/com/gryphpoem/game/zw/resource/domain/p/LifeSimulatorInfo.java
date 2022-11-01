package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.pb.CommonPb;

/**
 * 人生模拟器记录
 *
 * @Author: GeYuanpeng
 * @Date: 2022/10/26 17:14
 */
public class LifeSimulatorInfo {

    private Integer type;  // 模拟器类型

    private Long stepId; // 进行到哪一步了

    private Integer addDate; // 城镇事件刷新的日期(秒)

    private Integer pauseTime; // 事件暂停的时间点(秒)

    private Integer delay; // 到下一步的延时(天)

    private Integer bindType; // 1-绑定的建筑; 2-绑定的NPC

    private Integer bindId; // 模拟器绑定的建筑或NPC的id

    public LifeSimulatorInfo() {
    }

    public LifeSimulatorInfo(int type, int bindType, int bindId) {
        this.type = type;
        this.bindType = bindType;
        this.bindId = bindId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getStepId() {
        return stepId;
    }

    public void setStepId(Long stepId) {
        this.stepId = stepId;
    }

    public Integer getAddDate() {
        return addDate;
    }

    public void setAddDate(Integer addDate) {
        this.addDate = addDate;
    }

    public Integer getPauseTime() {
        return pauseTime;
    }

    public void setPauseTime(Integer pauseTime) {
        this.pauseTime = pauseTime;
    }

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    public Integer getBindType() {
        return bindType;
    }

    public void setBindType(Integer bindType) {
        this.bindType = bindType;
    }

    public Integer getBindId() {
        return bindId;
    }

    public void setBindId(Integer bindId) {
        this.bindId = bindId;
    }

    public CommonPb.LifeSimulatorInfo ser() {
        CommonPb.LifeSimulatorInfo.Builder builder = CommonPb.LifeSimulatorInfo.newBuilder();
        builder.setType(this.type);
        builder.setStepId(this.stepId);
        builder.setAddDate(this.addDate);
        builder.setPauseTime(this.pauseTime);
        builder.setDelay(this.delay);
        builder.setBindType(this.bindType);
        builder.setBindId(this.bindId);
        return builder.build();
    }

    public LifeSimulatorInfo dser(CommonPb.LifeSimulatorInfo pb) {
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
