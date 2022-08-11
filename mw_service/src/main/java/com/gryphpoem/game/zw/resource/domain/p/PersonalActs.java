package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.pojo.GamePb;
import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.*;


public class PersonalActs implements GamePb<List<CommonPb.TwoInt>> {
    /** key -> activityType, value -> actPlanKeyId*/
    private Map<Integer, Integer> typeKeyMap = new HashMap<>();

    public Map<Integer, Integer> getTypeKeyMap() {
        return typeKeyMap;
    }

    public void setTypeKeyMap(Map<Integer, Integer> typeKeyMap) {
        this.typeKeyMap = typeKeyMap;
    }

    public Integer getKeyId(int actType) {
        return typeKeyMap.get(actType);
    }

    public void saveData(int actType, int actKeyId) {
        if (Objects.nonNull(this.typeKeyMap.get(actType)))
            return;

        this.typeKeyMap.put(actType, actKeyId);
    }

    public boolean containActType(int actType) {
        return this.typeKeyMap.containsKey(actType);
    }

    public boolean containActKey(int actType, int actKeyId) {
        Integer keyId = this.typeKeyMap.get(actType);
        return Objects.nonNull(keyId) && keyId.intValue() == actKeyId;
    }

    public PersonalActs() {
    }

    public PersonalActs(List<CommonPb.TwoInt> twoIntList) {
        if (CheckNull.isEmpty(twoIntList))
            return;
        twoIntList.forEach(twoInt -> this.typeKeyMap.put(twoInt.getV1(), twoInt.getV2()));
    }

    @Override
    public List<CommonPb.TwoInt> createPb(boolean isSaveDb) {
        if (CheckNull.isEmpty(this.typeKeyMap))
            return new ArrayList<>();
        CommonPb.TwoInt.Builder twoInt = CommonPb.TwoInt.newBuilder();
        List<CommonPb.TwoInt> list = new ArrayList<>(this.typeKeyMap.size());
        typeKeyMap.forEach((actType, actId) -> {
            list.add(twoInt.setV1(actType).setV2(actId).build());
            twoInt.clear();
        });
        return list;
    }
}
