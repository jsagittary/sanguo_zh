package com.gryphpoem.game.zw.resource.domain.p;

import java.util.HashMap;
import java.util.Map;

/**
 * 科技
 * 
 * @author tyler
 *
 */
public class Tech {
    private Map<Integer, TechLv> techLv;
    private TechQue que;

    public Tech() {
        this.techLv = new HashMap<>();
        // this.que = new TechQue();
    }

    public Map<Integer, TechLv> getTechLv() {
        return techLv;
    }

    public void setTechLv(Map<Integer, TechLv> techLv) {
        this.techLv = techLv;
    }

    public TechQue getQue() {
        return que;
    }

    public void setQue(TechQue que) {
        this.que = que;
    }

    /**
     * 获取科技等级
     * 
     * @param techId
     * @return
     */
    public int getTechLvById(int techId) {
        TechLv techLv = getTechLv().get(techId);
        return null == techLv ? 0 : techLv.getLv();
    }

}