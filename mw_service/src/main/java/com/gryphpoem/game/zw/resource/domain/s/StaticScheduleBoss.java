package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @author: ZhouJie
 * @date: Create in 2019-03-07 11:34
 * @description: 世界进度的boss
 * @modified By:
 */
public class StaticScheduleBoss {

    /**
     * 主键
     */
    private int id;

    /**
     * s_schedule表的id
     */
    private int scheduleId;

    /**
     * 坐标
     */
    private int pos;

    /**
     * 占用的坐标 格式 [pos,pos]
     */
    private List<Integer> posList;

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

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public List<Integer> getPosList() {
        return posList;
    }

    public void setPosList(List<Integer> posList) {
        this.posList = posList;
    }
}
