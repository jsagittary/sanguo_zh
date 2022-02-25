package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @description:
 * @author: zhou jie
 * @time: 2021/10/27 14:36
 */
public class StaticMusicFestivalBoxOffice {


    private int id;
    private int activityId;
    private int type;

    private List<List<Integer>> award;
    private int level;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
