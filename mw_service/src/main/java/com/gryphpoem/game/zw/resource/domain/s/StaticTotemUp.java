package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;
import java.util.Map;

/**
 * @author xwind
 * @date 2021/11/18
 */
public class StaticTotemUp {
    private int id;
    private int upType;
    private int lv;
    private int quality;
    private int prob;
    private Map<Integer, Integer> attr1;
    private Map<Integer, Integer> attr2;
    private Map<Integer, Integer> attr3;
    private Map<Integer, Integer> attr4;
    private Map<Integer, Integer> attr5;
    private Map<Integer, Integer> attr6;
    private Map<Integer, Integer> attr7;
    private Map<Integer, Integer> attr8;
    private List<List<Integer>> upNeed;
    private List<List<Integer>> analysis;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUpType() {
        return upType;
    }

    public void setUpType(int upType) {
        this.upType = upType;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public List<List<Integer>> getUpNeed() {
        return upNeed;
    }

    public void setUpNeed(List<List<Integer>> upNeed) {
        this.upNeed = upNeed;
    }

    public List<List<Integer>> getAnalysis() {
        return analysis;
    }

    public void setAnalysis(List<List<Integer>> analysis) {
        this.analysis = analysis;
    }

    public int getProb() {
        return prob;
    }

    public void setProb(int prob) {
        this.prob = prob;
    }

    public Map<Integer, Integer> getAttr1() {
        return attr1;
    }

    public void setAttr1(Map<Integer, Integer> attr1) {
        this.attr1 = attr1;
    }

    public Map<Integer, Integer> getAttr2() {
        return attr2;
    }

    public void setAttr2(Map<Integer, Integer> attr2) {
        this.attr2 = attr2;
    }

    public Map<Integer, Integer> getAttr3() {
        return attr3;
    }

    public void setAttr3(Map<Integer, Integer> attr3) {
        this.attr3 = attr3;
    }

    public Map<Integer, Integer> getAttr4() {
        return attr4;
    }

    public void setAttr4(Map<Integer, Integer> attr4) {
        this.attr4 = attr4;
    }

    public Map<Integer, Integer> getAttr5() {
        return attr5;
    }

    public void setAttr5(Map<Integer, Integer> attr5) {
        this.attr5 = attr5;
    }

    public Map<Integer, Integer> getAttr6() {
        return attr6;
    }

    public void setAttr6(Map<Integer, Integer> attr6) {
        this.attr6 = attr6;
    }

    public Map<Integer, Integer> getAttr7() {
        return attr7;
    }

    public void setAttr7(Map<Integer, Integer> attr7) {
        this.attr7 = attr7;
    }

    public Map<Integer, Integer> getAttr8() {
        return attr8;
    }

    public void setAttr8(Map<Integer, Integer> attr8) {
        this.attr8 = attr8;
    }

    public Map<Integer,Integer> getAttrByIdx(int idx){
        if(idx == 1) return attr1;
        if(idx == 2) return attr2;
        if(idx == 3) return attr3;
        if(idx == 4) return attr4;
        if(idx == 5) return attr5;
        if(idx == 6) return attr6;
        if(idx == 7) return attr7;
        if(idx == 8) return attr8;
        return null;
    }
}
