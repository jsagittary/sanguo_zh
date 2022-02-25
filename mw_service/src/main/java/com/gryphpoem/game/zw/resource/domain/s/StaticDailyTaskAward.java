package com.gryphpoem.game.zw.resource.domain.s;

import java.util.Date;
import java.util.List;

/**
 * @ClassName StaticDailyTaskAward.java
 * @Description
 * @author QiuKun
 * @date 2018年5月15日
 */
public class StaticDailyTaskAward {
    private int id;// 奖励唯一ID
    private int value;// 领取奖励需要达到的活跃度的值
    private int season;
    private List<List<Integer>> award;// 奖励
    private List<Integer> level;
    /**
     * 奖励对应的赛季开始时间
     */
    private Date seasonBeginTime;
    /**
     * 奖励对应的赛季结束时间
     */
    private Date seasonEndTime;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }

    @Override
    public String toString() {
        return "StaticDailyTaskAward [id=" + id + ", value=" + value + ", award=" + award + "]";
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public List<Integer> getLevel() {
        return level;
    }

    public void setLevel(List<Integer> level) {
        this.level = level;
    }

    public Date getSeasonBeginTime() {
        return seasonBeginTime;
    }

    public void setSeasonBeginTime(Date seasonBeginTime) {
        this.seasonBeginTime = seasonBeginTime;
    }

    public Date getSeasonEndTime() {
        return seasonEndTime;
    }

    public void setSeasonEndTime(Date seasonEndTime) {
        this.seasonEndTime = seasonEndTime;
    }
}
