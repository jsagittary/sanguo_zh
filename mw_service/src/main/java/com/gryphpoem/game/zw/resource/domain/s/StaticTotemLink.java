package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;
import java.util.Map;

/**
 * @author xwind
 * @date 2021/11/18
 */
public class StaticTotemLink {
    private int id;
    private int quality;
    private int lv;
    private Map<Integer,Integer> attr;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public Map<Integer, Integer> getAttr() {
        return attr;
    }

    public void setAttr(Map<Integer, Integer> attr) {
        this.attr = attr;
    }
}
