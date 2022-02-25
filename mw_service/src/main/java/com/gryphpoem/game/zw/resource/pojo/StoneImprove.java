package com.gryphpoem.game.zw.resource.pojo;

import com.gryphpoem.game.zw.pb.CommonPb;

/**
 * @ClassName StoneImprove.java
 * @Description 宝石进阶
 * @author QiuKun
 * @date 2018年11月10日
 */
public class StoneImprove {
    private int keyId;// 唯一id值
    private int stoneImproveId; // 对应s_stone_improve表的id值
    private int exp; // 当前经验
    private int holeIndex; // 安装到孔位, 0表示没有安装
    private boolean breakThrough; // 是否突破, 0表示没有突破

    public void dser(CommonPb.StoneImprove ser) {
        this.keyId = ser.getKeyId();
        this.stoneImproveId = ser.getStoneImproveId();
        this.exp = ser.getExp();
        this.holeIndex = ser.getHoleIndex();
        this.breakThrough = ser.getBreakThrough();
    }

    public CommonPb.StoneImprove ser() {
        CommonPb.StoneImprove.Builder builder = CommonPb.StoneImprove.newBuilder();
        builder.setKeyId(this.keyId);
        builder.setStoneImproveId(this.stoneImproveId);
        builder.setExp(this.exp);
        builder.setHoleIndex(this.holeIndex);
        builder.setBreakThrough(this.breakThrough);
        return builder.build();
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getStoneImproveId() {
        return stoneImproveId;
    }

    public void setStoneImproveId(int stoneImproveId) {
        this.stoneImproveId = stoneImproveId;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getHoleIndex() {
        return holeIndex;
    }

    public void setHoleIndex(int holeIndex) {
        this.holeIndex = holeIndex;
    }

    public boolean isBreakThrough() {
        return breakThrough;
    }

    public void setBreakThrough(boolean breakThrough) {
        this.breakThrough = breakThrough;
    }

    @Override
    public String toString() {
        return "StoneImprove{" +
                "keyId=" + keyId +
                ", stoneImproveId=" + stoneImproveId +
                ", exp=" + exp +
                ", holeIndex=" + holeIndex +
                ", breakThrough=" + breakThrough +
                '}';
    }

}
