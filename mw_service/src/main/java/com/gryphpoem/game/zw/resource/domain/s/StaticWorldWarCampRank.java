package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * Created by pengshuo on 2019/3/27 18:46
 * <br>Description: 世界争霸阵营排行榜
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class StaticWorldWarCampRank {
    /**id*/
    private Integer id;
    /**排行名次*/
    private Integer ranking;
    /**官职*/
    private Integer job;
    /**奖励 格式[[type,id,cnt]]*/
    private List<List<Integer>> award;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRanking() {
        return ranking;
    }

    public void setRanking(Integer ranking) {
        this.ranking = ranking;
    }

    public Integer getJob() {
        return job;
    }

    public void setJob(Integer job) {
        this.job = job;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }

    @Override
    public String toString() {
        return "StaticWorldWarCampRank{" +
                "id=" + id +
                ", ranking=" + ranking +
                ", job=" + job +
                ", award=" + award +
                '}';
    }
}
