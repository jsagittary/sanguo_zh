package com.gryphpoem.game.zw.resource.domain.p;

/**
 * 司令部官员招募
 * 
 * @author tyler
 *
 */
public class Gains {
    private int type;
    private int id;
    private int endTime;

    public Gains(int type) {
        this.type = type;
    }

    public Gains(int type, int id, int endTime) {
        this.type = type;
        this.id = id;
        this.endTime = endTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }
}