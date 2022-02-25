package com.gryphpoem.game.zw.resource.domain.s;

/**
 * 永久活动
 * 
 * @author tyler
 *
 */
public class StaticActivityOpen {

    private int keyId;
    private int activityId;
    private int openBegin;//开服第几天开启 ：0无， 1 为开服第一天（当天）
    private int openDuration;//时长: 3表示开3天
    private int moldId;
    private int activityType;

    public int getActivityType() {
        return activityType;
    }

    public void setActivityType(int activityType) {
        this.activityType = activityType;
    }

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

    public int getOpenBegin() {
        return openBegin;
    }

    public void setOpenBegin(int openBegin) {
        this.openBegin = openBegin;
    }

    public int getOpenDuration() {
        return openDuration;
    }

    public void setOpenDuration(int openDuration) {
        this.openDuration = openDuration;
    }

    public int getMoldId() {
        return moldId;
    }

    public void setMoldId(int moldId) {
        this.moldId = moldId;
    }

}
