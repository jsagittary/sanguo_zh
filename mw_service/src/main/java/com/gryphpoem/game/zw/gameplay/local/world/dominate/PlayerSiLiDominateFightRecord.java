package com.gryphpoem.game.zw.gameplay.local.world.dominate;

import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.pojo.GamePb;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-24 11:11
 */
public class PlayerSiLiDominateFightRecord implements GamePb<SerializePb.SerPlayerSiLiDominateFightRecord.Builder> {
    private int killCnt;        // 总击杀兵力
    private int killRankTime;   // 累积击杀上榜时间
    private Map<Integer, Integer> continuousKillCntMap;

    public PlayerSiLiDominateFightRecord() {
        this.continuousKillCntMap = new HashMap<>();
    }

    public PlayerSiLiDominateFightRecord(SerializePb.SerPlayerSiLiDominateFightRecord ser) {
        this.killCnt = ser.getKillCnt();
        this.killRankTime = ser.getKillRankTime();
        this.continuousKillCntMap = new HashMap<>();
        if (CheckNull.nonEmpty(ser.getContinuousKillCntList())) {
            ser.getContinuousKillCntList().forEach(t -> this.continuousKillCntMap.put(t.getV1(), t.getV2()));
        }
    }

    public void addKillCnt(int hurt, int now) {
        this.killCnt += hurt;
        this.killRankTime = now;
    }

    public int getKillCnt() {
        return killCnt;
    }

    public int getKillRankTime() {
        return killRankTime;
    }

    public Map<Integer, Integer> getContinuousKillCntMap() {
        return continuousKillCntMap;
    }

    public Map<Integer, Integer> getContinuousKillCnt() {
        return continuousKillCntMap;
    }

    /**
     * 增加连杀次数
     */
    public int incContinuousKillCnt(int cityId) {
        int killCnt = this.continuousKillCntMap.getOrDefault(cityId, 0);
        this.continuousKillCntMap.put(cityId, ++killCnt);
        return killCnt;
    }

    /**
     * 清除连杀次数
     *
     * @param cityId
     */
    public void clearContinuousKillCnt(int cityId) {
        int killCnt = this.continuousKillCntMap.getOrDefault(cityId, 0);
        if (killCnt > 0) killCnt = 0;
        this.continuousKillCntMap.put(cityId, killCnt);
    }

    @Override
    public SerializePb.SerPlayerSiLiDominateFightRecord.Builder createPb(boolean isSaveDb) {
        SerializePb.SerPlayerSiLiDominateFightRecord.Builder builder = SerializePb.SerPlayerSiLiDominateFightRecord.newBuilder();
        builder.setKillCnt(this.killCnt);
        builder.setKillRankTime(this.killRankTime);
        if (CheckNull.nonEmpty(this.continuousKillCntMap)) {
            this.continuousKillCntMap.entrySet().stream().map(en -> PbHelper.createTwoIntPb(en.getKey(), en.getValue())).collect(Collectors.toList());
        }
        return builder;
    }
}