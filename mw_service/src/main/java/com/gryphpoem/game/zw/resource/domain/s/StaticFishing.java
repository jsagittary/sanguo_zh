package com.gryphpoem.game.zw.resource.domain.s;

/**
 * @author xwind
 * @date 2021/12/27
 */
public class StaticFishing {
    private int id;
    private int activityId;
    private int score;
    private int occupiedSpace;

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

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getOccupiedSpace() {
        return occupiedSpace;
    }

    public void setOccupiedSpace(int occupiedSpace) {
        this.occupiedSpace = occupiedSpace;
    }
}
