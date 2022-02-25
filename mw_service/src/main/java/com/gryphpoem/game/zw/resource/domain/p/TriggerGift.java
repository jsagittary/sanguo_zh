package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;

/**
 * @Author: ZhouJie
 * @Date: Create in 2018-03-01 14:24
 * @Description:
 * @Modified By:
 */
public class TriggerGift {

    private int keyId;
    private int giftId;
    private int beginTime;
    private int endTime;
    private int count;// 购买次数
    private int state;// 领取状态, 0: 未触发, 1: 触发, 2: 领取
    private int status;// 触发状态, triggerId : 9 用于记录玩家单日被击飞次数

    public TriggerGift(SerializePb.SerTriggerGift serTriggerGift) {
        this.giftId = serTriggerGift.getGiftId();
        this.beginTime = serTriggerGift.getBeginTime();
        this.endTime = serTriggerGift.getEndTime();
        this.count = serTriggerGift.getCount();
        this.state = serTriggerGift.getState();
        this.status = serTriggerGift.getStatus();
        if (serTriggerGift.hasKeyId()) {
            this.keyId = serTriggerGift.getKeyId();
        }
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getGiftId() {
        return giftId;
    }

    public void setGiftId(int giftId) {
        this.giftId = giftId;
    }

    public int getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(int beginTime) {
        this.beginTime = beginTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void maxCount() {
        count++;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public TriggerGift() {
    }

    public TriggerGift(int giftId) {
        this.giftId = giftId;
    }

    public TriggerGift(int giftId,int keyId) {
        this.giftId = giftId;
        this.keyId = keyId;
    }

    public void isRestart(boolean reStart) {
        if (reStart) {
            beginTime = 0;
            endTime = 0;
            count = 0;
            state = ActivityConst.NOT_TRIGGER_STATUS;
            status = 0;
        }
    }
}
