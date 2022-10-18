package com.gryphpoem.game.zw.resource.pojo;

import com.gryphpoem.game.zw.pb.SerializePb;

/**
 * @Author: GeYuanpeng
 * @Date: 2022/10/17 18:24
 */
public class CollectDropRecord {

    public static final int MINE_TYPE_1 = 1; // [[104,111],[204,211]] 累计300秒掉落
    public static final int MINE_TYPE_2 = 2; // [[304,311]] 累计500秒掉落

    private int mineType; // 掉落所属矿点范围

    private int date; // 采集日期

    private int propId; // 掉落的道具id

    private int totalGainCount;  // 当日累计获取数量

    public int getMineType() {
        return mineType;
    }

    public void setMineType(int mineType) {
        this.mineType = mineType;
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


    /**
     * 序列化
     * @return
     */
    public SerializePb.SerCollectDropRecord ser() {
        SerializePb.SerCollectDropRecord.Builder builder = SerializePb.SerCollectDropRecord.newBuilder();
        builder.setMineType(this.mineType);
        builder.setDate(this.date);
        builder.setPropId(this.propId);
        builder.setTotalGainCount(this.totalGainCount);

        return builder.build();
    }

    /**
     * 反序列化
     * @param serCollectDropRecord
     */
    public CollectDropRecord dser(SerializePb.SerCollectDropRecord serCollectDropRecord) {
        this.setMineType(serCollectDropRecord.getMineType());
        this.setDate(serCollectDropRecord.getDate());
        this.setPropId(serCollectDropRecord.getPropId());
        this.setTotalGainCount(serCollectDropRecord.getTotalGainCount());
        return this;
    }

    @Override
    public String toString() {
        return "CollectDropRecord{" +
                "mineType=" + mineType +
                ", date=" + date +
                ", propId=" + propId +
                ", totalGainCount=" + totalGainCount +
                '}';
    }
}
