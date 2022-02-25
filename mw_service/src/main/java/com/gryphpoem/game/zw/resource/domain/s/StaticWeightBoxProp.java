package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticWeightBoxProp.java
 * @Description 权重次数箱配置
 * @author QiuKun
 * @date 2018年8月22日
 */
public class StaticWeightBoxProp {
    private int propid;// 道具id
    private List<List<Integer>> randomNum; // [[次数最小值,次数最大值,权重]]
    private List<Integer> reward;

    public int getPropid() {
        return propid;
    }

    public void setPropid(int propid) {
        this.propid = propid;
    }

    public List<List<Integer>> getRandomNum() {
        return randomNum;
    }

    public void setRandomNum(List<List<Integer>> randomNum) {
        this.randomNum = randomNum;
    }

    public List<Integer> getReward() {
        return reward;
    }

    public void setReward(List<Integer> reward) {
        this.reward = reward;
    }

    @Override
    public String toString() {
        return "StaticWeightBoxProp [propid=" + propid + ", randomNum=" + randomNum + ", reward=" + reward + "]";
    }

}
