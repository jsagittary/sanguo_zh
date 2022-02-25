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
public class StaticNewYorkWarAchievement {

    private int id;
    private int cond;
    private List<List<Integer>> awardList;
    private int count;
    private String desc;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCond() {
        return cond;
    }

    public void setCond(int cond) {
        this.cond = cond;
    }

    public List<List<Integer>> getAwardList() {
        return awardList;
    }

    public void setAwardList(List<List<Integer>> awardList) {
        this.awardList = awardList;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "StaticNewYorkWarAchievement{" +
                "id=" + id +
                ", cond=" + cond +
                ", awardList=" + awardList +
                ", count=" + count +
                ", desc='" + desc + '\'' +
                '}';
    }
}
