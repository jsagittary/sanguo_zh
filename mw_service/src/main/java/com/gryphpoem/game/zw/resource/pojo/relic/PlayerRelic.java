package com.gryphpoem.game.zw.resource.pojo.relic;

import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xwind
 * @date 2022/8/5
 */
public class PlayerRelic {
    private int score;
    private Map<Integer, Integer> gotMap = new HashMap<>();
    private int startProbe;//开始探索时间戳
    /**
     * 连杀次数
     */
    private int continuousKillCnt;

    public void clear() {
        this.score = 0;
        this.gotMap.clear();
        this.startProbe = 0;
        this.continuousKillCnt = 0;
    }

    public int finalScore() {
        int n = 0;
        if (startProbe > 0) {
            n = TimeHelper.getCurrentSecond() - startProbe;
        }
        return score + n;
    }

    public int getScore() {
        return finalScore();
    }

    public void addScore(int add) {
        this.score += add;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Map<Integer, Integer> getGotMap() {
        return gotMap;
    }

    public void setGotMap(Map<Integer, Integer> gotMap) {
        this.gotMap = gotMap;
    }

    public int getStartProbe() {
        return startProbe;
    }

    public void setStartProbe(int stamp) {
        if (startProbe > 0) {
            this.score += TimeHelper.getCurrentSecond() - startProbe;
        }
        this.startProbe = stamp;
    }

    public int setStartProbe0(int stamp) {
        int n = 0;
        if (startProbe > 0) {
            n = TimeHelper.getCurrentSecond() - startProbe;
            ;
            this.score += n;
        }
        this.startProbe = stamp;
        return n;
    }

    public SerializePb.SerPlayerRelic ser() {
        SerializePb.SerPlayerRelic.Builder builder = SerializePb.SerPlayerRelic.newBuilder();
        builder.setScore(this.score);
        builder.setStartProbe(this.startProbe);
        this.gotMap.entrySet().forEach(entry -> builder.addGotMap(PbHelper.createTwoIntPb(entry.getKey(), entry.getValue())));
        return builder.build();
    }

    public void dser(SerializePb.SerPlayerRelic serPlayerRelic) {
        this.score = serPlayerRelic.getScore();
        this.startProbe = serPlayerRelic.getStartProbe();
        if (serPlayerRelic.getGotMapCount() > 0) {
            serPlayerRelic.getGotMapList().forEach(o -> gotMap.put(o.getV1(), o.getV2()));
        }
    }

    public int getContinuousKillCnt() {
        return continuousKillCnt;
    }

    public void setContinuousKillCnt(int continuousKillCnt) {
        this.continuousKillCnt = continuousKillCnt;
    }

    /**
     * 增加连杀次数
     */
    public void incContinuousKillCnt() {
        this.continuousKillCnt++;
    }

    /**
     * 清空连杀次数
     */
    public void clearContinuousKillCnt() {
        this.continuousKillCnt = 0;
    }
}
