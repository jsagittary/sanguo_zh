package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * Created by pengshuo on 2019/5/10 15:26
 * <br>Description: 纽约争霸成就奖励
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class StaticNewYorkWarPersonalRank {

    private int id;
    private List<Integer> ranking;
    private List<List<Integer>> awardList;
    private String desc;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getRanking() {
        return ranking;
    }

    public void setRanking(List<Integer> ranking) {
        this.ranking = ranking;
    }

    public List<List<Integer>> getAwardList() {
        return awardList;
    }

    public void setAwardList(List<List<Integer>> awardList) {
        this.awardList = awardList;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "StaticNewYorkWarPersonalRank{" +
                "id=" + id +
                ", ranking=" + ranking +
                ", awardList=" + awardList +
                ", desc='" + desc + '\'' +
                '}';
    }
}
