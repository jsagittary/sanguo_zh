package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * Created by pengshuo on 2019/3/27 18:47
 * <br>Description: 世界争霸个人排行榜
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class StaticWorldWarPersonalRank {
    /** id */
    private Integer id;
    /** 世界争霸的档位 */
    private Integer worldWarType;
    /**排行名次[名次开始,名次结束]*/
    private List<Integer> ranking;
    /** 奖励 格式[[type,id,cnt]] */
    private List<List<Integer>> award;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getWorldWarType() {
        return worldWarType;
    }

    public void setWorldWarType(Integer worldWarType) {
        this.worldWarType = worldWarType;
    }

    public List<Integer> getRanking() {
        return ranking;
    }

    public void setRanking(List<Integer> ranking) {
        this.ranking = ranking;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }

    @Override
    public String toString() {
        return "StaticWorldWarPersonalRank{" +
                "id=" + id +
                ", worldWarType=" + worldWarType +
                ", ranking=" + ranking +
                ", award=" + award +
                '}';
    }
}
