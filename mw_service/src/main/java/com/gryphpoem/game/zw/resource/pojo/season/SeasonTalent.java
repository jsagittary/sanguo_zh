package com.gryphpoem.game.zw.resource.pojo.season;

import com.gryphpoem.game.zw.pb.CommonPb;

import java.util.HashSet;
import java.util.Set;

/**
 * 玩家赛季天赋
 *
 * @Description
 * @Author zhangdh
 * @Date 2021-06-02 14:53
 */
public class SeasonTalent {
    //天赋所属赛季ID
    private int seasonId;
    private int seasonPlanId;
    //0-未初始化天赋, 1-进攻天赋, 2-防守天赋, 3-种田天赋
    private int classifier;
    //剩余天赋点数
    private int remainStone;
    //已经用掉的天赋点数
    private int costStone;
    //赛季期间总共获得的天赋点数
    private int totalStone;
    //本周重置次数
    private int resetClassifierCountWeek;
    //最后一次重置天赋种类时间
    private long lastResetClassifierTime;
    private int effect501Count;//和平主义生效次数
    private long lastEffect501Time;//和平主义生效时间
    private int openTalentProgress;//开启天赋进度条
    private boolean openTalent;//是否开启天赋


    //天赋列表
    private Set<Integer> learns = new HashSet<>();

    public void reset(int seasonId, int seasonPlanId) {
        this.seasonId = seasonId;
        this.seasonPlanId = seasonPlanId;
        this.classifier = 0;
        //不再清除原有石头
//        this.remainStone = 0;
        this.costStone = 0;
        this.totalStone = 0;
        this.learns.clear();
        this.resetClassifierCountWeek = 0;
        this.lastResetClassifierTime = 0;
        this.effect501Count = 0;
        this.lastEffect501Time = 0;
        this.openTalentProgress = 0;
        this.openTalent = false;
    }

    public void deser(CommonPb.PbSeasonTalent pb) {
        this.seasonId = pb.getSeasonId();
        this.seasonPlanId = pb.getSeasonPlanId();
        this.classifier = pb.getClassifier();
        this.remainStone = pb.getRemainStone();
        this.costStone = pb.getCostStone();
        this.totalStone = pb.getTotalStone();
        this.resetClassifierCountWeek = pb.getResetClassifierCountWeek();
        this.lastResetClassifierTime = pb.getLastResetClassifierTime();
        this.effect501Count = pb.getEffect501Count();
        this.lastEffect501Time = pb.getLastEffect501Time();
        this.openTalentProgress = pb.getOpenTalentProgress();
        this.openTalent = pb.getOpenTalent();
        if (pb.getLearnCount() > 0) {
            learns.addAll(pb.getLearnList());
        }
    }

    public CommonPb.PbSeasonTalent ser() {
        CommonPb.PbSeasonTalent.Builder pb = CommonPb.PbSeasonTalent.newBuilder();
        pb.setSeasonId(this.seasonId);
        pb.setSeasonPlanId(this.seasonPlanId);
        pb.setClassifier(this.classifier);
        pb.setRemainStone(this.remainStone);
        pb.setCostStone(this.costStone);
        pb.setTotalStone(this.totalStone);
        pb.setResetClassifierCountWeek(this.resetClassifierCountWeek);
        pb.setLastResetClassifierTime(this.lastResetClassifierTime);
        pb.setEffect501Count(this.effect501Count);
        pb.setLastEffect501Time(this.lastEffect501Time);
        pb.setOpenTalentProgress(this.openTalentProgress);
        pb.setOpenTalent(this.isOpenTalent());
        if (!learns.isEmpty()) {
            pb.addAllLearn(learns);
        }
        return pb.build();
    }

    public int getClassifier() {
        return classifier;
    }

    public void setClassifier(int classifier) {
        this.classifier = classifier;
    }

    public int getRemainStone() {
        return remainStone;
    }

    public void setRemainStone(int remainStone) {
        this.remainStone = remainStone;
    }

    public int getCostStone() {
        return costStone;
    }

    public void setCostStone(int costStone) {
        this.costStone = costStone;
    }

    public int getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(int seasonId) {
        this.seasonId = seasonId;
    }

    public int getTotalStone() {
        return totalStone;
    }

    public void setTotalStone(int totalStone) {
        this.totalStone = totalStone;
    }

    public Set<Integer> getLearns() {
        return learns;
    }

    public void setLearns(Set<Integer> learns) {
        this.learns = learns;
    }

    public int getResetClassifierCountWeek() {
        return resetClassifierCountWeek;
    }

    public void setResetClassifierCountWeek(int resetClassifierCountWeek) {
        this.resetClassifierCountWeek = resetClassifierCountWeek;
    }

    public long getLastResetClassifierTime() {
        return lastResetClassifierTime;
    }

    public void setLastResetClassifierTime(long lastResetClassifierTime) {
        this.lastResetClassifierTime = lastResetClassifierTime;
    }

    public int getEffect501Count() {
        return effect501Count;
    }

    public void setEffect501Count(int effect501Count) {
        this.effect501Count = effect501Count;
    }

    public long getLastEffect501Time() {
        return lastEffect501Time;
    }

    public void setLastEffect501Time(long lastEffect501Time) {
        this.lastEffect501Time = lastEffect501Time;
    }

    public int getSeasonPlanId() {
        return seasonPlanId;
    }

    public void setSeasonPlanId(int seasonPlanId) {
        this.seasonPlanId = seasonPlanId;
    }

    public int getOpenTalentProgress() {
        return openTalentProgress;
    }

    public void setOpenTalentProgress(int openTalentProgress) {
        this.openTalentProgress = openTalentProgress;
    }

    public void addOpenTalentProgress(int add) {
        this.openTalentProgress += add;
    }

    public boolean isOpenTalent() {
        return openTalent;
    }

    public void setOpenTalent(boolean openTalent) {
        this.openTalent = openTalent;
    }
}
