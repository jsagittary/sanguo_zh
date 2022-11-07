package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 主城地图格
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/5 15:56
 */
public class StaticHomeCityCell {

    private Integer id; // 格子id

    private List<Integer> route; // [起点格子id, 终点格子id] 客户端计算npc路线需要

    private List<Integer> bindCellList; // 绑定解锁的格子id[1, 2, ...]

    private Integer level; // 解锁该格需要领主等级

    private Integer exploreTime; // 探索需要时长 单位秒

    private Integer reclaimTime; // 开垦需要时长 单位秒

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<Integer> getRoute() {
        return route;
    }

    public void setRoute(List<Integer> route) {
        this.route = route;
    }

    public List<Integer> getBindCellList() {
        return bindCellList;
    }

    public void setBindCellList(List<Integer> bindCellList) {
        this.bindCellList = bindCellList;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getExploreTime() {
        return exploreTime;
    }

    public void setExploreTime(Integer exploreTime) {
        this.exploreTime = exploreTime;
    }

    public Integer getReclaimTime() {
        return reclaimTime;
    }

    public void setReclaimTime(Integer reclaimTime) {
        this.reclaimTime = reclaimTime;
    }

}
