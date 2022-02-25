package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticAirship.java
 * @Description 飞艇的配置
 * @author QiuKun
 * @date 2019年1月16日
 */
public class StaticAirship {
    private int id;
    private int lv; // 等级
    private int liveTime; // 存活时间 秒值
    private int rebirthInterval; // 复活间隔 秒值
    private List<Integer> form; // 兵力阵型，格式：[npcId,npcId...]
    private int participate; // 可参与人数
    /**
     * 固定掉落奖励
     */
    private List<List<Integer>> awardRegular;
    /**
     * 额外概率掉落奖励
     */
    private List<List<Integer>> awardExtra;
    /**
     * 每日首杀奖励
     */
    private List<List<Integer>> awardFirst;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public int getLiveTime() {
        return liveTime;
    }

    public void setLiveTime(int liveTime) {
        this.liveTime = liveTime;
    }

    public int getRebirthInterval() {
        return rebirthInterval;
    }

    public void setRebirthInterval(int rebirthInterval) {
        this.rebirthInterval = rebirthInterval;
    }

    public List<Integer> getForm() {
        return form;
    }

    public void setForm(List<Integer> form) {
        this.form = form;
    }

    public int getParticipate() {
        return participate;
    }

    public void setParticipate(int participate) {
        this.participate = participate;
    }

    public List<List<Integer>> getAwardRegular() {
        return awardRegular;
    }

    public void setAwardRegular(List<List<Integer>> awardRegular) {
        this.awardRegular = awardRegular;
    }

    public List<List<Integer>> getAwardExtra() {
        return awardExtra;
    }

    public void setAwardExtra(List<List<Integer>> awardExtra) {
        this.awardExtra = awardExtra;
    }

    public List<List<Integer>> getAwardFirst() {
        return awardFirst;
    }

    public void setAwardFirst(List<List<Integer>> awardFirst) {
        this.awardFirst = awardFirst;
    }

    @Override
    public String toString() {
        return "StaticAirship{" +
                "id=" + id +
                ", lv=" + lv +
                ", liveTime=" + liveTime +
                ", rebirthInterval=" + rebirthInterval +
                ", form=" + form +
                ", participate=" + participate +
                ", awardRegular=" + awardRegular +
                ", awardExtra=" + awardExtra +
                ", awardFirst=" + awardFirst +
                '}';
    }

}
