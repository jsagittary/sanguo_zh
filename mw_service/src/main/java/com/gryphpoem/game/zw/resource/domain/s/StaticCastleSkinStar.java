package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 皮肤升星等级配表s_castle_skin_star
 * @program: civilization_zh
 * @description:
 * @author: zhou jie
 * @create: 2020-11-12 10:49
 */
public class StaticCastleSkinStar {

    private int id;
    private int type;
    private int baseprob;
    private int star;
    private List<List<Integer>> consume;
    private int effectVal;

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

    public int getBaseprob() {
        return baseprob;
    }

    public void setBaseprob(int baseprob) {
        this.baseprob = baseprob;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }

    public List<List<Integer>> getConsume() {
        return consume;
    }

    public void setConsume(List<List<Integer>> consume) {
        this.consume = consume;
    }

    public int getEffectVal() {
        return effectVal;
    }

    public void setEffectVal(int effectVal) {
        this.effectVal = effectVal;
    }
}