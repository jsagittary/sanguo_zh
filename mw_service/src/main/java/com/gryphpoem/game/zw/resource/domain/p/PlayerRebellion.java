package com.gryphpoem.game.zw.resource.domain.p;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.SerializePb.DbPlayerRebellion;
import com.gryphpoem.game.zw.resource.util.PbHelper;

/**
 * @ClassName PlayerRebellion.java
 * @Description 玩家存储的匪军叛乱信息
 * @author QiuKun
 * @date 2018年10月26日
 */
public class PlayerRebellion {


    private Map<Integer, RebelBuff> buffs = new HashMap<>();    // key:buffType
    private int credit;                                         // 匪军积分
    private volatile boolean isDead;                            // 是否死亡,true表示挂了
    private int round;                                          // 挺过了几轮
    private Map<Integer, Integer> buyRecord = new HashMap<>();  // 购买记录

    public DbPlayerRebellion ser() {
        DbPlayerRebellion.Builder builder = DbPlayerRebellion.newBuilder();
        for (RebelBuff rBuff : getBuffs().values()) {
            builder.addBuffs(rBuff.ser());
        }
        builder.setCredit(getCredit());
        builder.setIsDead(isDead());
        builder.setRound(getRound());
        builder.addAllBuyRecord(buyRecord.entrySet().stream().map(en -> PbHelper.createTwoIntPb(en.getKey(), en.getValue())).collect(Collectors.toList()));
        return builder.build();
    }

    public void dser(DbPlayerRebellion ser) {
        for (CommonPb.RebelBuff buffPb : ser.getBuffsList()) {
            RebelBuff buff = new RebelBuff();
            buff.dser(buffPb);
            this.buffs.put(buff.getType(), buff);
        }
        if (ser.hasIsDead()) this.isDead = ser.getIsDead();
        if (ser.hasRound()) this.round = ser.getRound();
        if (ser.hasCredit()) this.credit = ser.getCredit();
        ser.getBuyRecordList().forEach(twoInt -> buyRecord.put(twoInt.getV1(), twoInt.getV2()));
    }

    public void cleanCurRound() {
        this.isDead = false;
        this.round = 0;
    }

    public Map<Integer, RebelBuff> getBuffs() {
        return buffs;
    }

    public void setBuffs(Map<Integer, RebelBuff> buffs) {
        this.buffs = buffs;
    }

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        this.credit = credit;
    }

    public boolean isDead() {
        return isDead;
    }

    public void setDead(boolean isDead) {
        this.isDead = isDead;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public int addAndGetCredit(int num) {
        if (num > 0) {
            this.credit += num;
        }
        return this.credit;
    }

    public Map<Integer, Integer> getBuyRecord() {
        return buyRecord;
    }

    public void setBuyRecord(Map<Integer, Integer> buyRecord) {
        this.buyRecord = buyRecord;
    }
}
