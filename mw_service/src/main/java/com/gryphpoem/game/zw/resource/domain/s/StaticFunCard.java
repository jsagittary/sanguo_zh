package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticFunCard.java
 * @Description 功能卡
 * @author QiuKun
 * @date 2018年11月20日
 */
public class StaticFunCard {
    /**
     * 永久天
     */
    public static final int FOREVER_DAY = -10;

    private int id;
    private int type;// 类型 同一种特权，填相同的字段
    private int payId;// 对应的充值表的payId
    private int day;// 购买获得天数, -10 表示永久时间
    private List<List<Integer>> award;// 每日发放邮件的奖励 [[type,id,count]]
    private int season;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPayId() {
        return payId;
    }

    public void setPayId(int payId) {
        this.payId = payId;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "StaticFunCard [id=" + id + ", type=" + type + ", payId=" + payId + ", day=" + day + ", award=" + award
                + "]";
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }
}
