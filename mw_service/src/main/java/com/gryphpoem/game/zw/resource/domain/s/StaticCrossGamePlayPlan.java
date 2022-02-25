package com.gryphpoem.game.zw.resource.domain.s;


import com.gryphpoem.game.zw.gameplay.local.constant.cross.NewCrossConstant;
import com.gryphpoem.game.zw.pb.CommonPb;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

public class StaticCrossGamePlayPlan {
    private int keyId;

    private int activityId;

    private int activityType;

    private String name;

    private int displayOpen;

    private int openBegin;

    private int awardBegin;

    private int openDuration;

    private int sendMail;

    private Date displayBegin;

    private Date beginTime;

    private Date awardTime;

    private Date endTime;

    private Date displayTime;

    private int crossServerId;

    private int group;

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public int getActivityType() {
        return activityType;
    }

    public void setActivityType(int activityType) {
        this.activityType = activityType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDisplayOpen() {
        return displayOpen;
    }

    public void setDisplayOpen(int displayOpen) {
        this.displayOpen = displayOpen;
    }

    public int getOpenBegin() {
        return openBegin;
    }

    public void setOpenBegin(int openBegin) {
        this.openBegin = openBegin;
    }

    public int getAwardBegin() {
        return awardBegin;
    }

    public void setAwardBegin(int awardBegin) {
        this.awardBegin = awardBegin;
    }

    public int getOpenDuration() {
        return openDuration;
    }

    public void setOpenDuration(int openDuration) {
        this.openDuration = openDuration;
    }

    public int getSendMail() {
        return sendMail;
    }

    public void setSendMail(int sendMail) {
        this.sendMail = sendMail;
    }

    public Date getDisplayBegin() {
        return displayBegin;
    }

    public void setDisplayBegin(Date displayBegin) {
        this.displayBegin = displayBegin;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getAwardTime() {
        return awardTime;
    }

    public void setAwardTime(Date awardTime) {
        this.awardTime = awardTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getDisplayTime() {
        return displayTime;
    }

    public void setDisplayTime(Date displayTime) {
        this.displayTime = displayTime;
    }

    public int getCrossServerId() {
        return crossServerId;
    }

    public void setCrossServerId(int crossServerId) {
        this.crossServerId = crossServerId;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public int getStage() {
        Date now = new Date();
        if (Objects.nonNull(displayBegin) && Objects.nonNull(beginTime) && Objects.nonNull(endTime) && Objects.nonNull(displayTime)) {
            if (now.before(displayBegin) || now.after(displayTime)) {
                // 结束
                return NewCrossConstant.STAGE_OVER;
            } else if (now.after(displayBegin) && now.before(beginTime)) {
                // 预显示
                return NewCrossConstant.STAGE_DIS_PLAYER;
            } else if (now.after(beginTime) && now.before(endTime)) {
                return NewCrossConstant.STAGE_RUNNING;
            } else if (now.after(endTime) && now.before(displayTime)) {
                return NewCrossConstant.STAGE_END_PLAYER;
            }
        }
        return NewCrossConstant.STAGE_OVER;
    }

    public boolean isRunning() {
        return getStage() == NewCrossConstant.STAGE_RUNNING;
    }

    public CommonPb.CrossGamePlayConfigPb createPb(int functionId) {
        CommonPb.CrossGamePlayConfigPb.Builder builder = CommonPb.CrossGamePlayConfigPb.newBuilder();
        builder.setPlanGameKey(this.keyId);
        Optional.ofNullable(this.displayTime).ifPresent(time -> builder.setDisplayTime((int) (this.displayTime.getTime() / 1000l)));
        Optional.ofNullable(this.displayBegin).ifPresent(time -> builder.setDisplayBeginTime((int) (this.displayBegin.getTime() / 1000l)));
        Optional.ofNullable(this.endTime).ifPresent(time -> builder.setEndTime((int) (this.endTime.getTime() / 1000l)));
        Optional.ofNullable(this.beginTime).ifPresent(time -> builder.setStartTime((int) (this.beginTime.getTime() / 1000l)));
        builder.setStage(getStage());
        builder.setFunctionId(functionId);

        return builder.build();
    }
}
