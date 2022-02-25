package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticCrossPersonalTrophy.java
 * @Description 跨服个人成就
 * @author QiuKun
 * @date 2019年5月29日
 */
public class StaticCrossPersonalTrophy {

    private int id;
    private int killNum;
    private List<List<Integer>> award;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getKillNum() {
        return killNum;
    }

    public void setKillNum(int killNum) {
        this.killNum = killNum;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }

    @Override
    public String toString() {
        return "StaticCrossPersonalTrophy [id=" + id + ", killNum=" + killNum + ", award=" + award + "]";
    }
    
    

}
