package com.gryphpoem.game.zw.resource.pojo.fish;

import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.service.fish.FishingConst;

/**
 * 鱼竿
 */
public class FishRod {
    private int rodId;//鱼竿ID 兼容后续多个鱼竿
    private int state;//鱼竿状态 0空闲中 1钓鱼中
    private int baitId;//挂的鱼饵id
    private int sliderIdx;//滑块索引1-4
    private int fishId; //抛竿后确认鱼id

    public void reset(){
        this.rodId = 0;
        this.state = FishingConst.ROD_STATE_NON;
        this.baitId = 0;
        this.sliderIdx = 0;
        this.fishId = 0;
    }

    public SerializePb.SerFishRod ser(){
        SerializePb.SerFishRod.Builder builder = SerializePb.SerFishRod.newBuilder();
        builder.setRodId(rodId);
        builder.setState(state);
        builder.setBaitId(baitId);
        builder.setSliderIdx(sliderIdx);
        builder.setFishId(fishId);
        return builder.build();
    }

    public void dser(SerializePb.SerFishRod serFishRod){
        this.rodId = serFishRod.getRodId();
        this.state = serFishRod.getState();
        this.baitId = serFishRod.getBaitId();
        this.sliderIdx = serFishRod.getSliderIdx();
        this.fishId = serFishRod.getFishId();
    }

    public int getSliderIdx() {
        return sliderIdx;
    }

    public void setSliderIdx(int sliderIdx) {
        this.sliderIdx = sliderIdx;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getBaitId() {
        return baitId;
    }

    public void setBaitId(int baitId) {
        this.baitId = baitId;
    }

    public int getRodId() {
        return rodId;
    }

    public void setRodId(int rodId) {
        this.rodId = rodId;
    }

    @Override
    public String toString() {
        return "FishRod{" +
                "rodId=" + rodId +
                ", state=" + state +
                ", baitId=" + baitId +
                '}';
    }

    public int getFishId() {
        return fishId;
    }

    public void setFishId(int fishId) {
        this.fishId = fishId;
    }
}
