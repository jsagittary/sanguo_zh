package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * Created by pengshuo on 2019/3/8 9:53 <br>
 * Description: 世界进度奖励 <br>
 * Modified By: <br>
 * Version:
 *
 * @author pengshuo
 */
public class StaticScheduleRank {

    private int id;
    /** s_schedule表的id */
    private int scheduleId;
    /** 排名 */
    private int ranking;
    /** 领取的时玩家等级限制 */
    private int lvCond;
    /** 奖励 格式[[type,id,cnt]] */
    private List<List<Integer>> award;
    /** 描述 */
    private String desc;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    public int getRanking() {
        return ranking;
    }

    public void setRanking(int ranking) {
        this.ranking = ranking;
    }

    public int getLvCond() {
        return lvCond;
    }

    public void setLvCond(int lvCond) {
        this.lvCond = lvCond;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "StaticScheduleRank [id=" + id + ", scheduleId=" + scheduleId + ", ranking=" + ranking + ", lvCond="
                + lvCond + ", award=" + award + ", desc=" + desc + "]";
    }
}
