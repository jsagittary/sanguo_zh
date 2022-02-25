package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @author: ZhouJie
 * @date: Create in 2019-02-18 17:04
 * @description: 补给配置信息
 * @modified By:
 */
public class StaticPartySupply {

    private int id;

    private int type;

    private int actionObject;

    private int count;

    private List<Integer> param;

    private List<List<Integer>> award;

    private int energy;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getActionObject() {
        return actionObject;
    }

    public void setActionObject(int actionObject) {
        this.actionObject = actionObject;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Integer> getParam() {
        return param;
    }

    public void setParam(List<Integer> param) {
        this.param = param;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = energy;
    }

    /**
     * 作用每个人
     * @return
     */
    public boolean actionEverOne() {
        return this.actionObject == 0;
    }

    /**
     * 作用于每一次
     * @return
     */
    public boolean actionEverTime() {
        return this.count == 0;
    }
}
