package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.constant.EArmyType;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.util.*;

/**
 * @author xwind
 * @date 2021/3/17
 */
public class PlayerOnHook {
    private int maxRebelLv;//最大可挂等级
    private int curRebelLv;//当前挂机等级
    private Map<Integer,Integer> armys;//3个兵种的兵力
    private int state;//1run 0stop  挂机中 or 停止
    private List<AwardItem> drops;//挂机的掉落
    private int lastStamp;//最后一次战斗的时间戳
    private Integer askAnnihilateNumber;//每次请求剿灭叛军阈值
    private Integer askLastAnnihilateNumber;//每次剩余剿灭叛军数量

    public PlayerOnHook(){
        this.maxRebelLv = 1;
        this.curRebelLv = 1;
        this.armys = new HashMap<>();
        for (EArmyType value : EArmyType.values()) {
            this.armys.put(value.getType(),0);
        }
        drops = new ArrayList<>();
    }

    public int getMaxRebelLv() {
        return maxRebelLv;
    }

    public void setMaxRebelLv(int maxRebelLv) {
        this.maxRebelLv = maxRebelLv;
    }

    public int getCurRebelLv() {
        return curRebelLv;
    }

    public void setCurRebelLv(int curRebelLv) {
        this.curRebelLv = curRebelLv;
    }

    public Map<Integer, Integer> getArmys() {
        return armys;
    }

    public void setArmys(Map<Integer, Integer> armys) {
        this.armys = armys;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public List<AwardItem> getDrops() {
        return drops;
    }

    public void setDrops(List<AwardItem> drops) {
        this.drops = drops;
    }

    public int getLastStamp() {
        return lastStamp;
    }

    public void setLastStamp(int lastStamp) {
        this.lastStamp = lastStamp;
    }

    public Integer getAskAnnihilateNumber() {
        return askAnnihilateNumber;
    }

    public void setAskAnnihilateNumber(Integer askAnnihilateNumber) {
        this.askAnnihilateNumber = askAnnihilateNumber;
    }

    public Integer getAskLastAnnihilateNumber() {
        return askLastAnnihilateNumber;
    }

    public void setAskLastAnnihilateNumber(Integer askLastAnnihilateNumber) {
        this.askLastAnnihilateNumber = askLastAnnihilateNumber;
    }

    public void addDropAward(List<CommonPb.Award> awardList){
        awardList.forEach(obj -> {
            AwardItem awardItem = drops.stream().filter(obj_ -> obj_.getType() == obj.getType() && obj_.getId() == obj.getId()).findFirst().orElse(null);
            if(Objects.isNull(awardItem)){
                awardItem = new AwardItem(obj.getType(),obj.getId(),obj.getCount());
                drops.add(awardItem);
            }else {
                awardItem.setCount(awardItem.getCount() + obj.getCount());
            }
        });
    }

    public SerializePb.SerPlayerOnHook ser(){
        SerializePb.SerPlayerOnHook.Builder builder = SerializePb.SerPlayerOnHook.newBuilder();
        builder.setMaxRebelLv(this.maxRebelLv);
        builder.setCurRebelLv(this.curRebelLv);
        armys.entrySet().forEach(entry -> builder.addArmys(PbHelper.createTwoIntPb(entry.getKey(),entry.getValue())));
        builder.setState(this.state);
        drops.forEach(o -> builder.addDrops(PbHelper.createAward(o)));
        builder.setLastStamp(this.lastStamp);
        if (Objects.nonNull(this.getAskAnnihilateNumber()))
        {
            builder.setAskAnnihilateNumber(this.getAskAnnihilateNumber());
        }
        if (Objects.nonNull(this.getAskLastAnnihilateNumber()))
        {
            builder.setAskLastAnnihilateNumber(this.getAskLastAnnihilateNumber());
        }
        return builder.build();
    }

    public void deser(SerializePb.SerPlayerOnHook obj){
        Optional.ofNullable(obj).ifPresent(o -> {
            this.setMaxRebelLv(o.getMaxRebelLv() == 0 ? 1 : o.getMaxRebelLv());
            this.setCurRebelLv(o.getCurRebelLv() == 0 ? 1 : o.getCurRebelLv());
            Optional.of(o.getArmysList()).ifPresent(tmps -> tmps.forEach(tmp -> this.armys.put(tmp.getV1(),tmp.getV2())));
            this.setState(o.getState());
            Optional.of(o.getDropsList()).ifPresent(tmps -> tmps.forEach(tmp -> this.drops.add(new AwardItem(tmp.getType(),tmp.getId(),tmp.getCount()))));
            this.setLastStamp(o.getLastStamp());
            this.setAskAnnihilateNumber(o.getAskAnnihilateNumber());
            this.setAskLastAnnihilateNumber(o.getAskLastAnnihilateNumber());
        });
    }
}
