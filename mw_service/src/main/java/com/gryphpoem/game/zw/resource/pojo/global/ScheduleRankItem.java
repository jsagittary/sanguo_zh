package com.gryphpoem.game.zw.resource.pojo.global;

/**
 * @author QiuKun
 * @ClassName ScheduleRankItem.java
 * @Description
 * @date 2019年2月21日
 */
public class ScheduleRankItem {

    /**
     * 阵营
     */
    private int camp;

    /**
     * 区域
     */
    private int area;

    /**
     * 角色id
     */
    private long roleId;

    /**
     * 分值
     */
    private long value;

    public int getCamp() {
        return camp;
    }

    public void setCamp(int camp) {
        this.camp = camp;
    }

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public void addValue(long value) {
        this.value += value;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public ScheduleRankItem() {
    }

    public ScheduleRankItem(int camp, long value) {
        this.camp = camp;
        this.value = value;
    }

    public ScheduleRankItem(int camp, int area, long value) {
        this.camp = camp;
        this.area = area;
        this.value = value;
    }

    public ScheduleRankItem(long roleId, long value) {
        this.roleId = roleId;
        this.value = value;
    }

    @Override
    public String toString() {
        return "ScheduleRankItem{" +
                "camp=" + camp +
                ", area=" + area +
                ", roleId=" + roleId +
                ", value=" + value +
                '}';
    }
}
