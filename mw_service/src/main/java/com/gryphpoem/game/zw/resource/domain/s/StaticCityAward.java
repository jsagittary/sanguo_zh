package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticCityAward.java
 * @Description 德意志都城升级表
 * @author QiuKun
 * @date 2018年7月30日
 */
public class StaticCityAward {
    private int num;// 建设次数
    private int exp;// 每次建设获取的经验
    private int exploit;// 每次建设获取的军功奖励
    private List<List<Integer>> consume; // 消耗的物质

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getExploit() {
        return exploit;
    }

    public void setExploit(int exploit) {
        this.exploit = exploit;
    }

    public List<List<Integer>> getConsume() {
        return consume;
    }

    public void setConsume(List<List<Integer>> consume) {
        this.consume = consume;
    }

    @Override
    public String toString() {
        return "StaticCityAward [num=" + num + ", exp=" + exp + ", exploit=" + exploit + "]";
    }

}
