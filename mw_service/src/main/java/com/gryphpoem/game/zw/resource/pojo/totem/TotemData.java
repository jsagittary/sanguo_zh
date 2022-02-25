package com.gryphpoem.game.zw.resource.pojo.totem;

import com.gryphpoem.game.zw.pb.CommonPb;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xwind
 * @date 2021/11/19
 */
public class TotemData {
    private Map<Integer,Totem> totemMap = new HashMap<>();
//    private Map<Integer,TotemChip> totemChipMap = new HashMap<>();

    public CommonPb.TotemDataInfo ser(){
        CommonPb.TotemDataInfo.Builder builder = CommonPb.TotemDataInfo.newBuilder();
        totemMap.values().forEach(o -> builder.addTotems(o.ser()));
//        totemChipMap.values().forEach(o -> builder.addTotemChips(o.ser()));
        return builder.build();
    }

    public void dser(CommonPb.TotemDataInfo totemDataInfo){
        totemDataInfo.getTotemsList().forEach(o -> {
            Totem totem = new Totem();
            totem.dser(o);
            totemMap.put(o.getTotemKey(),totem);
        });
//        totemDataInfo.getTotemChipsList().forEach(o -> totemChipMap.put(o.getChipId(),new TotemChip(o.getChipId(),o.getCount())));
    }

    public Totem newTotem(int totemKey,int totemId){
        Totem totem = new Totem(totemKey,totemId);
        totemMap.put(totemKey,totem);
        return totem;
    }

//    public void newTotemChip(int chipId,int count){
//        TotemChip totemChip = totemChipMap.get(chipId);
//        if(Objects.isNull(totemChip)){
//            totemChip = new TotemChip(chipId,count);
//            totemChipMap.put(chipId,totemChip);
//        }else {
//            totemChip.setCount(totemChip.getCount() + count);
//        }
//    }

    public Totem getTotem(int totemKey){
        return totemMap.get(totemKey);
    }

    public Map<Integer, Totem> getTotemMap() {
        return totemMap;
    }

//    public Map<Integer, TotemChip> getTotemChipMap() {
//        return totemChipMap;
//    }
//
//    public TotemChip getTotemChip(int chipId){
//        return totemChipMap.get(chipId);
//    }
}
