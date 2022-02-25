package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 科技升级表
 * 
 * @author tyler
 *
 */
public class StaticTechLv {
    private int id;// 扩建次数
    private int techId;
    private int lv;
    private int cnt;
    private int needTime;
    private List<Integer> effect;// 加成
    private List<Integer> needTech;
    private List<List<Integer>> needBuilding;
    private List<List<Integer>> cost;// 消耗

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTechId() {
        return techId;
    }

    public void setTechId(int techId) {
        this.techId = techId;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }

    public int getNeedTime() {
        return needTime;
    }

    public void setNeedTime(int needTime) {
        this.needTime = needTime;
    }

    public List<Integer> getNeedTech() {
        return needTech;
    }

    public void setNeedTech(List<Integer> needTech) {
        this.needTech = needTech;
    }

    public List<List<Integer>> getNeedBuilding() {
        return needBuilding;
    }

    public void setNeedBuilding(List<List<Integer>> needBuilding) {
        this.needBuilding = needBuilding;
    }

    public List<List<Integer>> getCost() {
        return cost;
    }

    public void setCost(List<List<Integer>> cost) {
        this.cost = cost;
    }

    public List<Integer> getEffect() {
        return effect;
    }

    public void setEffect(List<Integer> effect) {
        this.effect = effect;
    }

}
