package com.gryphpoem.game.zw.resource.pojo.party;


import com.gryphpoem.game.zw.dataMgr.StaticPartyDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: ZhouJie
 * @date: Create in 2019-02-18 17:36
 * @description: 补给记录
 * @modified By:
 * @see SupplyRecord#statusMap 玩家、阵营,补给类型的触发次数记录
 * @see SupplyRecord#superSupplyStatus 玩家个人超级补给领取记录
 */
public class SupplyRecord {

    /**
     * key=SupplyId, value=count
     */
    private Map<Integer, Integer> statusMap = new HashMap<>(50);

    /**
     * key=SuperSupplyLv, value, 0 未领取, 1 领取了
     */
    private Map<Integer, Integer> superSupplyStatus = new HashMap<>(50);

    /**
     * 未领取
     */
    public static final int UN_RECEIVED = 0;

    /**
     * 已领取
     */
    public static final int ALREADY_RECEIVED = 1;

    /**
     * 获取可领取的奖励等级
     * @param lv
     * @return
     */
    public int getAwardLv(int lv) {
        int canAwardLv = 0;
        for (int i = 1; i < lv; i++) {
            if (!superSupplyStatus.containsKey(i) || superSupplyStatus.getOrDefault(i, UN_RECEIVED) == UN_RECEIVED) {
                ++canAwardLv;
            }
        }
        return canAwardLv;
    }

    /**
     * 获取最小可领取的奖励等级
     * @param lv
     * @return
     */
    public int getMinAwardLv(int lv) {
        int canAwardLv = 0;
        for (int i = 1; i < lv; i++) {
            if (!superSupplyStatus.containsKey(i) || superSupplyStatus.getOrDefault(i, UN_RECEIVED) == UN_RECEIVED) {
                return i;
            }
        }
        return canAwardLv;
    }

    /**
     * 有没有可以领取的超级补给
     * @param lv
     * @return
     */
    public boolean canGetAwardLv(int lv) {
        int awardLv = getAwardLv(lv);
        return awardLv > 0;
    }

    /**
     * 记录领取奖励
     * @param lv
     */
    public void receiveAward(int lv) {
        this.superSupplyStatus.put(lv, ALREADY_RECEIVED);
    }

    /**
     * 根据补给类型获取触发次数
     * @param type
     * @return
     */
    public int supplyStatusByType(int type) {
        return this.statusMap.getOrDefault(type, 0);
    }

    /**
     * 记录补给的触发次数
     * @param sPartySupply
     */
    public void recordSupply(int sPartySupply) {
        int cnt = supplyStatusByType(sPartySupply);
        this.statusMap.put(sPartySupply, cnt + 1);
    }

    /**
     * 序列化
     * @return
     */
    public SerializePb.SerSupplyRecord ser() {
        SerializePb.SerSupplyRecord.Builder builder = SerializePb.SerSupplyRecord.newBuilder();
        if (!CheckNull.isEmpty(statusMap)) {
            for (Map.Entry<Integer, Integer> en : statusMap.entrySet()) {
                builder.addStatusMap(PbHelper.createTwoIntPb(en.getKey(), en.getValue()));
            }
        }

        if (!CheckNull.isEmpty(superSupplyStatus)) {
            for (Map.Entry<Integer, Integer> en : superSupplyStatus.entrySet()) {
                builder.addSuperSupplyStatus(PbHelper.createTwoIntPb(en.getKey(), en.getValue()));
            }
        }
        return builder.build();
    }

    /**
     * 反序列化
     * @param supplyRecord
     */
    public void dser(SerializePb.SerSupplyRecord supplyRecord) {
        List<CommonPb.TwoInt> statusMapList = supplyRecord.getStatusMapList();
        if (!CheckNull.isEmpty(statusMapList)) {
            for (CommonPb.TwoInt twoInt : statusMapList) {
                this.statusMap.put(twoInt.getV1(), twoInt.getV2());
            }
        }
        List<CommonPb.TwoInt> superSupplyList = supplyRecord.getSuperSupplyStatusList();
        if (!CheckNull.isEmpty(superSupplyList)) {
            for (CommonPb.TwoInt twoInt : superSupplyList) {
                this.superSupplyStatus.put(twoInt.getV1(), twoInt.getV2());
            }
        }
    }


    /**
     * gm清除
     */
    public void gmClear() {
        this.statusMap.clear();
        this.superSupplyStatus.clear();
    }

    /**
     * 合服处理超级补给领取进度
     * @param maxLv 主服的超级补给等级为上限
     */
    public void mergeLogic(int maxLv) {
        if (maxLv < StaticPartyDataMgr.getMinSuperSupplyLv() || maxLv > StaticPartyDataMgr.getMaxSuperSupplyLv()) {
            return;
        }
        this.superSupplyStatus.keySet().stream().filter(lv -> lv > maxLv).forEach(lv -> this.superSupplyStatus.put(lv, UN_RECEIVED));
    }
}
