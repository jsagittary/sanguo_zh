package com.gryphpoem.game.zw.resource.pojo.attr;

import com.gryphpoem.game.zw.pb.CommonPb;

/**
 * 宝具属性的百分比
 * Description:
 * Author: zhangdh
 * createTime: 2022-03-01 15:45
 */
public class TreasureWareAttrItem extends AttrItem {
    //初始属性
    private int initValue;
    //属性栏目品阶 (上上品, 上品, 中品, 中下品, 下品, 下下品)
    private int stage;
    //属性百分比, 用来计算初始属性与强化属性
    private int percent;
    //该属性将替换目标法宝上的第几条属性
    private int trainTargetIndex;

    public TreasureWareAttrItem(int attrId, int value) {
        super(attrId, value);
        this.initValue = value;
    }

    public TreasureWareAttrItem(CommonPb.TreasureWareAttrItem pb) {
        CommonPb.AttrItem attrPb = pb.getAttr();
        this.index = attrPb.getIndex();
        this.attrId = attrPb.getAttrId();
        this.value = attrPb.getValue();
        this.level = attrPb.getLevel();
        this.quality = attrPb.getQuality();
        this.initValue = pb.getInitValue();
        this.stage = pb.getStage();
        this.percent = pb.getPercent();
        this.trainTargetIndex = pb.getTrainTargetIndex();
    }

    public TreasureWareAttrItem copy() {
        TreasureWareAttrItem copy = new TreasureWareAttrItem(this.attrId, this.value);
        copy.index = this.index;
        copy.quality = this.quality;
        copy.level = this.level;
        copy.initValue = this.initValue;
        copy.stage = this.stage;
        copy.percent = this.percent;
        copy.trainTargetIndex = trainTargetIndex;
        return copy;
    }

    public int getInitValue() {
        return initValue;
    }

    public void setInitValue(int initValue) {
        this.initValue = initValue;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public int getTrainTargetIndex() {
        return trainTargetIndex;
    }

    public void setTrainTargetIndex(int trainTargetIndex) {
        this.trainTargetIndex = trainTargetIndex;
    }

    @Override
    public String logAttr() {
        return index + "," + attrId + "," + value + "," + stage + "," + initValue + ";";
    }
}