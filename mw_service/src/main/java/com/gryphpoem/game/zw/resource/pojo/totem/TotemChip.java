package com.gryphpoem.game.zw.resource.pojo.totem;

import com.gryphpoem.game.zw.pb.CommonPb;

/**
 * @author xwind
 * @date 2021/11/19
 */
public class TotemChip {
    private int chipId;
    private int count;

    public CommonPb.TotemChipInfo ser(){
        CommonPb.TotemChipInfo.Builder builder = CommonPb.TotemChipInfo.newBuilder();
        builder.setChipId(chipId);
        builder.setCount(count);
        return builder.build();
    }

    public void dser(CommonPb.TotemChipInfo totemChipInfo){
        this.setChipId(totemChipInfo.getChipId());
        this.setCount(totemChipInfo.getCount());
    }

    public TotemChip(int chipId, int count) {
        this.chipId = chipId;
        this.count = count;
    }

    public TotemChip() {
    }

    public int getChipId() {
        return chipId;
    }

    public void setChipId(int chipId) {
        this.chipId = chipId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
