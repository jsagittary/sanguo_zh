package com.gryphpoem.game.zw.resource.pojo.fish;

import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.HashMap;
import java.util.Map;

public class FishShop {
    private int lastDay;
    private Map<Integer,Integer> limit;

    public FishShop() {
        this.lastDay = TimeHelper.getCurrentDay();
        limit = new HashMap<>();
    }

    public SerializePb.SerFishShop ser(){
        SerializePb.SerFishShop.Builder builder = SerializePb.SerFishShop.newBuilder();
        builder.setLastDay(lastDay);
        limit.entrySet().forEach(o -> builder.addLimit(PbHelper.createTwoIntPb(o.getKey(),o.getValue())));
        return builder.build();
    }

    public void dser(SerializePb.SerFishShop ser){
        this.lastDay = ser.getLastDay();
        ser.getLimitList().forEach(o -> limit.put(o.getV1(),o.getV2()));
    }

    public int getLastDay() {
        return lastDay;
    }

    public void setLastDay(int lastDay) {
        this.lastDay = lastDay;
    }

    public Map<Integer, Integer> getLimit() {
        return limit;
    }

    public void setLimit(Map<Integer, Integer> limit) {
        this.limit = limit;
    }
}
