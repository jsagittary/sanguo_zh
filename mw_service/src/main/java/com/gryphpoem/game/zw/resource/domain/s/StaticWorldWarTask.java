package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * Created by pengshuo on 2019/3/28 14:58
 * <br>Description: 世界争霸任务配置  每日计时任务
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class StaticWorldWarTask {
    /**keyId*/
    private Integer id;
    /**世界争霸的档位,任务奖励的时候使用*/
    private Integer worldWarType;
    /** 任务条件类型 */
    private Integer cond;
    /** 任务条件ID */
    private Integer condId;
    /** 任务完成次数 */
    private Integer schedule;
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

    public Integer getCond() {
        return cond;
    }

    public void setCond(Integer cond) {
        this.cond = cond;
    }

    public Integer getCondId() {
        return condId;
    }

    public void setCondId(Integer condId) {
        this.condId = condId;
    }

    public Integer getSchedule() {
        return schedule;
    }

    public void setSchedule(Integer schedule) {
        this.schedule = schedule;
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
        return "StaticWorldWarTask{" +
                "id=" + id +
                ", worldWarType=" + worldWarType +
                ", cond=" + cond +
                ", condId=" + condId +
                ", schedule=" + schedule +
                ", award=" + award +
                ", desc='" + desc + '\'' +
                '}';
    }
}
