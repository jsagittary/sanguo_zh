package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;
import java.util.Map;

public class StaticBanditArea {
    private int id;
    private List<Integer> schedule;
    private int areaOrder;// 分区次序（等级），1 默认开启（郡），2 初次解锁开启（州），3 皇城
    private int minLv;// 可刷新流寇的最低等级
    private int maxlv;// 可刷新流寇的最高等级
    private int maxNum;// 分区内最多可同时存在流寇数量
    private List<Integer> block;// 10*10的分块id
    private Map<Integer, Integer> bandits;// 流寇各等级最多可刷新最大数量，格式：[[lv,maxNum]...]
    private Map<Integer, Integer> roleBandis;// 玩家基地周围流寇刷新配置

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getSchedule() {
        return schedule;
    }

    public void setSchedule(List<Integer> schedule) {
        this.schedule = schedule;
    }

    public Map<Integer, Integer> getRoleBandis() {
        return roleBandis;
    }

    public void setRoleBandis(Map<Integer, Integer> roleBandis) {
        this.roleBandis = roleBandis;
    }

    public int getAreaOrder() {
        return areaOrder;
    }

    public void setAreaOrder(int areaOrder) {
        this.areaOrder = areaOrder;
    }

    public int getMinLv() {
        return minLv;
    }

    public void setMinLv(int minLv) {
        this.minLv = minLv;
    }

    public int getMaxlv() {
        return maxlv;
    }

    public void setMaxlv(int maxlv) {
        this.maxlv = maxlv;
    }

    public int getMaxNum() {
        return maxNum;
    }

    public void setMaxNum(int maxNum) {
        this.maxNum = maxNum;
    }

    public Map<Integer, Integer> getBandits() {
        return bandits;
    }

    public void setBandits(Map<Integer, Integer> bandits) {
        this.bandits = bandits;
    }

    public List<Integer> getBlock() {
        return block;
    }

    public void setBlock(List<Integer> block) {
        this.block = block;
    }

}
