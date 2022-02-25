package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * Created by pengshuo on 2019/3/27 13:46
 * <br>Description: 世界争霸 阵营军威值奖励
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class StaticWorldWarCampCityAward {
    /** PRIMARY KEY */
    private Integer id;
    /**世界争霸的档位*/
    private Integer worldWarType;
    /**军威值的条件*/
    private Integer value;
    /**奖励[[typeId,id,count]]*/
    private List<List<Integer>> award;
    /** 描述 */
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
        return "StaticWorldWarCampCityAward{" +
                "id=" + id +
                ", worldWarType=" + worldWarType +
                ", value=" + value +
                ", award=" + award +
                ", desc='" + desc + '\'' +
                '}';
    }
}
