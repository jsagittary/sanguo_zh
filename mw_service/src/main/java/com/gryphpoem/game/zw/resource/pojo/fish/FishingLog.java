package com.gryphpoem.game.zw.resource.pojo.fish;

import com.gryphpoem.game.zw.pb.SerializePb;

/**
 * 钓鱼历史记录
 */
public class FishingLog {
    public long logId;//记录id
    public int placeId;//区域id
    public int fishId;//鱼id
    public int fishNum;//鱼数量
    public int fishSize;//鱼尺寸
    public int shareTimes;//分享次数

    public FishingLog(int placeId, int fishId, int fishNum, int fishSize) {
        this.placeId = placeId;
        this.fishId = fishId;
        this.fishNum = fishNum;
        this.fishSize = fishSize;
        this.logId = System.currentTimeMillis();
    }

    public FishingLog() {}

    public SerializePb.SerFishingLog ser(){
        SerializePb.SerFishingLog.Builder builder = SerializePb.SerFishingLog.newBuilder();
        builder.setLogId(logId);
        builder.setPlaceId(placeId);
        builder.setFishId(fishId);
        builder.setFishNum(fishNum);
        builder.setFishSize(fishSize);
        builder.setShareTimes(shareTimes);
        return builder.build();
    }

    public FishingLog dser(SerializePb.SerFishingLog ser){
        this.logId = ser.getLogId();
        this.placeId = ser.getPlaceId();
        this.fishId = ser.getFishId();
        this.fishNum = ser.getFishNum();
        this.fishSize = ser.getFishSize();
        this.shareTimes = ser.getShareTimes();
        return this;
    }

    @Override
    public String toString() {
        return "FishingLog{" +
                "logId=" + logId +
                ", placeId=" + placeId +
                ", fishId=" + fishId +
                ", fishNum=" + fishNum +
                ", fishSize=" + fishSize +
                ", shareTimes=" + shareTimes +
                '}';
    }
}
