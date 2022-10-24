package com.gryphpoem.game.zw.resource.pojo;

import com.gryphpoem.game.zw.pb.SerializePb;

/**
 * @Author: GeYuanpeng
 * @Date: 2022/10/17 18:24
 */
public class DropPropRecord {

    public static final int DROP_TYPE_1 = 1; // 击飞玩家掉落
    public static final int DROP_TYPE_2 = 2; // 攻打叛军掉落
    public static final int DROP_TYPE_3 = 3; // 攻打精英叛军掉落
    public static final int DROP_TYPE_4 = 4; // 采集掉落

    private int dropType; // 掉落类型

    private int date; // 采集日期

    private int propId; // 掉落的道具id

    private int totalGainCount;  // 当日累计获取数量

    private int activityId; // 活动id

    private int activityType; // 活动类型

    public int getDropType() {
        return dropType;
    }

    public void setDropType(int dropType) {
        this.dropType = dropType;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public int getPropId() {
        return propId;
    }

    public void setPropId(int propId) {
        this.propId = propId;
    }

    public int getTotalGainCount() {
        return totalGainCount;
    }

    public void setTotalGainCount(int totalGainCount) {
        this.totalGainCount = totalGainCount;
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

    /**
     * 序列化
     *
     * @return
     */
    public SerializePb.SerDropPropRecord ser() {
        SerializePb.SerDropPropRecord.Builder builder = SerializePb.SerDropPropRecord.newBuilder();
        builder.setDropType(this.dropType);
        builder.setDate(this.date);
        builder.setPropId(this.propId);
        builder.setTotalGainCount(this.totalGainCount);
        builder.setActivityId(this.activityId);
        builder.setActivityType(this.activityType);
        return builder.build();
    }

    /**
     * 反序列化
     *
     * @param serDropPropRecord
     */
    public DropPropRecord dser(SerializePb.SerDropPropRecord serDropPropRecord) {
        this.setDropType(serDropPropRecord.getDropType());
        this.setDate(serDropPropRecord.getDate());
        this.setPropId(serDropPropRecord.getPropId());
        this.setTotalGainCount(serDropPropRecord.getTotalGainCount());
        this.setActivityId(serDropPropRecord.getActivityId());
        this.setActivityType(serDropPropRecord.getActivityType());
        return this;
    }

    @Override
    public String toString() {
        return "DropPropRecord{" +
                "dropCondition=" + dropType +
                ", date=" + date +
                ", propId=" + propId +
                ", totalGainCount=" + totalGainCount +
                ", activityId=" + activityId +
                ", activityType=" + activityType +
                '}';
    }
}
