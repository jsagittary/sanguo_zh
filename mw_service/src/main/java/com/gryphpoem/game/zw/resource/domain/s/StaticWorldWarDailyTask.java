package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * Created by pengshuo on 2019/3/28 14:58
 * <br>Description: 世界争霸每日任务配置 每日杀敌任务
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class StaticWorldWarDailyTask {
    /**keyId*/
    private Integer id;
    /**世界争霸的档位,任务奖励的时候使用*/
    private Integer worldWarType;
    /** 领取奖励需要达到的活跃度的值 */
    private Integer value;
    /** 奖励 格式[[type,id,cnt]] */
    private List<List<Integer>> award;
    /**desc*/
    private String desc;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getWorldWarType() {
        return worldWarType;
    }

    public void setWorldWarType(Integer worldWarType) {
        this.worldWarType = worldWarType;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "StaticWorldWarDailyTask{" +
                "id=" + id +
                ", worldWarType=" + worldWarType +
                ", value=" + value +
                ", award=" + award +
                ", desc='" + desc + '\'' +
                '}';
    }
}
