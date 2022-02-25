package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticMasterReaward.java
 * @Description 师徒奖励配置
 * @author QiuKun
 * @date 2017年7月3日
 */
public class StaticMasterReaward {
    private int id;// 商品id
    private List<List<Integer>> award;// 获得物品 格式:[[type,id,count],[type,id,count]]
    private List<Integer> condition;// 领取条件 格式:[level,count] 例如[30,20] 有一个徒弟达到30级,可领取一次,最多可领取20次

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }

    public List<Integer> getCondition() {
        return condition;
    }

    public void setCondition(List<Integer> condition) {
        this.condition = condition;
    }

}
