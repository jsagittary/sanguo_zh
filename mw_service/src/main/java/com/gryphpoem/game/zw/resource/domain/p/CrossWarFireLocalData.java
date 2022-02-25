package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.gameplay.local.constant.cross.CrossFunction;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.util.HashMap;
import java.util.Map;

public class CrossWarFireLocalData extends CrossFunctionData {
    private int pos;
    //KEY:buffType, VALUE:lv
    private Map<Integer, Integer> buffs = new HashMap<>();
    /**
     * 购买记录 shop-购买次数
     */
    private Map<Integer, Integer> buyRecord = new HashMap<>();

    private Map<Integer, Integer> cityBuff = new HashMap<>();

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public CommonPb.SaveCrossWarFireData createDataPb() {
        CommonPb.SaveCrossWarFireData.Builder builder = CommonPb.SaveCrossWarFireData.newBuilder();
        builder.setCrossPlayerFunction(super.createPb(true));

        CommonPb.CrossWarFireLocalData.Builder crossWarFire = CommonPb.CrossWarFireLocalData.newBuilder();
        crossWarFire.setPos(pos);
        if (CheckNull.nonEmpty(buffs)) {
            for (Map.Entry<Integer, Integer> entry : buffs.entrySet()) {
                crossWarFire.addBuff(PbHelper.createTwoIntPb(entry.getKey(), entry.getValue()));
            }
        }
        if (CheckNull.nonEmpty(buyRecord)) {
            for (Map.Entry<Integer, Integer> entry : buyRecord.entrySet()) {
                crossWarFire.addBuyRecord(PbHelper.createTwoIntPb(entry.getKey(), entry.getValue()));
            }
        }
        if (CheckNull.nonEmpty(cityBuff)) {
            cityBuff.entrySet().forEach(entry -> {
                crossWarFire.addCityBuff(PbHelper.createTwoIntPb(entry.getKey(), entry.getValue()));
            });
        }
        builder.setCrossWarFire(crossWarFire.build());
        return builder.build();
    }

    public static CrossWarFireLocalData newCrossWarFireLocalData(CommonPb.SaveCrossWarFireData data) {
        CrossWarFireLocalData crossWarFireLocalData = new CrossWarFireLocalData();
        crossWarFireLocalData.dseData(data.getCrossPlayerFunction());
        crossWarFireLocalData.setPos(data.getCrossWarFire().getPos());
        crossWarFireLocalData.dseCrossWarFireData(data.getCrossWarFire());
        return crossWarFireLocalData;
    }

    public void dseCrossWarFireData(CommonPb.CrossWarFireLocalData data) {
        if (CheckNull.nonEmpty(data.getBuffList())) {
            for (CommonPb.TwoInt twoInt : data.getBuffList()) {
                buffs.put(twoInt.getV1(), twoInt.getV2());
            }
        }
        if (CheckNull.nonEmpty(data.getBuyRecordList())) {
            for (CommonPb.TwoInt twoInt : data.getBuyRecordList()) {
                buyRecord.put(twoInt.getV1(), twoInt.getV2());
            }
        }
        if (CheckNull.nonEmpty(data.getCityBuffList())) {
            data.getCityBuffList().forEach(buff -> cityBuff.put(buff.getV1(), buff.getV2()));
        }
    }

    public CommonPb.CrossWarFireLocalData createClientPb(int force, int crossWarFireCoin) {
        CommonPb.CrossWarFireLocalData.Builder builder = CommonPb.CrossWarFireLocalData.newBuilder();
        builder.setPos(pos);
        builder.setForce(force);
        if (CheckNull.nonEmpty(buffs)) {
            for (Map.Entry<Integer, Integer> entry : buffs.entrySet()) {
                builder.addBuff(PbHelper.createTwoIntPb(entry.getKey(), entry.getValue()));
            }
        }
        if (CheckNull.nonEmpty(buyRecord)) {
            for (Map.Entry<Integer, Integer> entry : buyRecord.entrySet()) {
                builder.addBuyRecord(PbHelper.createTwoIntPb(entry.getKey(), entry.getValue()));
            }
        }
        builder.setCrossWarFireCoin(crossWarFireCoin);
        return builder.build();
    }

    public Map<Integer, Integer> getBuffs() {
        return buffs;
    }

    public void setBuffs(Map<Integer, Integer> buffs) {
        this.buffs = buffs;
    }

    public Map<Integer, Integer> getBuyRecord() {
        return buyRecord;
    }

    public CrossWarFireLocalData(CrossFunction crossFunction, int keyId) {
        super(crossFunction, keyId);
    }

    public Map<Integer, Integer> getCityBuff() {
        return cityBuff;
    }

    public void setCityBuff(Map<Integer, Integer> cityBuff) {
        this.cityBuff = cityBuff;
    }

    @Override
    public void reset(int keyId) {
        if (keyId == 0)
            return;

        if (keyId != getPlanKey()) {
            this.pos = 0;
            this.buffs.clear();
            this.buyRecord.clear();
            this.cityBuff.clear();
            this.setInCross(false);
            this.setLeaveTime(0);
            this.setPlanKey(keyId);
        }
    }

    public CrossWarFireLocalData() {
    }

    public CrossWarFireLocalData(int keyId) {
        super(keyId);
    }
}
