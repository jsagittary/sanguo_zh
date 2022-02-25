package com.gryphpoem.game.zw.resource.pojo.party;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: ZhouJie
 * @date: Create in 2019-02-18 17:22
 * @description: 军团补给
 * @modified By:
 */
public class PartySupply {

    public static int INCREMENT_KEY = 0;

    /**
     * 唯一标识
     */
    private int key;

    /**
     * 结束时间
     */
    private int endTime;

    /**
     * 配置id
     */
    private int id;

    /**
     * 奖励状态, key=roleId, val=status, 0 未领取, 1 已领取
     */
    private Map<Long, Integer> awardStatus = new HashMap<>(500);

    /**
     * 未领取
     */
    public static final int UN_RECEIVED = 0;

    /**
     * 已领取
     */
    public static final int ALREADY_RECEIVED = 1;


    public PartySupply() {
    }

    /**
     * 反序列化
     * @param supply
     */
    public PartySupply(CommonPb.PartySupply supply) {
        this();
        this.key = supply.getKey();
        this.endTime = supply.getEndTime();
        this.id = supply.getId();
        PartySupply.INCREMENT_KEY = supply.getIncrementKey();
        for (CommonPb.LongInt longInt : supply.getStatusList()) {
            awardStatus.put(longInt.getV1(), longInt.getV2());
        }
    }

    public PartySupply(int endTime, int id, List<Long> ids) {
        this();
        this.key = ++INCREMENT_KEY;
        this.endTime = endTime;
        this.id = id;
        ids.stream().forEach(roleId -> awardStatus.put(roleId, UN_RECEIVED));
    }


    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    /**
     * 判断玩家是否可以领取奖励
     * 还没有到补给的结束时间并且玩家满足领取条件
     * @param roleId
     * @return
     */
    public boolean canAward(long roleId) {
        return TimeHelper.getCurrentSecond() < endTime && awardStatus.containsKey(roleId) && awardStatus.get(roleId).equals(UN_RECEIVED);
    }

    /**
     * 记录玩家领取信息
     * @param roleId
     */
    public void receiveAward(long roleId) {
        awardStatus.put(roleId, ALREADY_RECEIVED);
    }

    public Map<Long, Integer> getAwardStatus() {
        return awardStatus;
    }

    @Override public String toString() {
        return "PartySupply{" + "key=" + key + ", endTime=" + endTime + ", id=" + id + ", awardStatus=" + awardStatus
                + '}';
    }
}
