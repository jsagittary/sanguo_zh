package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticRandomProp.java
 * @Description 随机箱子处理
 * @author QiuKun
 * @date 2018年7月16日
 */
public class StaticRandomProp {
    private int id;
    private int propid;// 道具id
    private List<Integer> compare; // 需要比较的道具id,那个最少选那个
    private List<Integer> randomNum;// 随机道具的数量 最小值,最大值

    public int getPropid() {
        return propid;
    }

    public void setPropid(int propid) {
        this.propid = propid;
    }

    public List<Integer> getCompare() {
        return compare;
    }

    public void setCompare(List<Integer> compare) {
        this.compare = compare;
    }

    public List<Integer> getRandomNum() {
        return randomNum;
    }

    public void setRandomNum(List<Integer> randomNum) {
        this.randomNum = randomNum;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "StaticRandomProp [propid=" + propid + ", compare=" + compare + ", randomNum=" + randomNum + "]";
    }

}
