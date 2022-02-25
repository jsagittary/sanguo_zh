package com.gryphpoem.game.zw.resource.pojo;

import com.gryphpoem.game.zw.pb.CommonPb;

/**
 * @ClassName StoneHole.java
 * @Description 宝石的孔位
 * @author QiuKun
 * @date 2018年5月7日
 */
public class StoneHole {
    public static final int TYPE_STONE = 0;
    public static final int TYPE_STONE_IMPROVE = 1;

    private int holeIndex; // 孔位
    private int stoneId;// 镶嵌宝石的id,或宝石进阶的keyId
    private int type; // 安装宝石的类型, 0:原来宝石id, 1:stoneImprove的keyId

    public StoneHole(CommonPb.StoneHole ser) {
        this.holeIndex = ser.getHoleIndex();
        this.stoneId = ser.getStoneId();
        if (ser.hasType()) {
            this.type = ser.getType();
        }
    }

    public StoneHole(int holeIndex) {
        this.holeIndex = holeIndex;
    }

    public int getHoleIndex() {
        return holeIndex;
    }

    public void setHoleIndex(int holeIndex) {
        this.holeIndex = holeIndex;
    }

    public int getStoneId() {
        return stoneId;
    }

    public void setStoneId(int stoneId) {
        this.stoneId = stoneId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "StoneHole [holeIndex=" + holeIndex + ", stoneId=" + stoneId + ", type=" + type + "]";
    }

}
