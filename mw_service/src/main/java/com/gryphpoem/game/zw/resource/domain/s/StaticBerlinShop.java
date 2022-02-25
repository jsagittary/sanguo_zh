package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.List;

/**
 * @ClassName StaticBerlinShop.java
 * @Description 柏林商店配置
 * @author QiuKun
 * @date 2018年8月2日
 */
public class StaticBerlinShop {

    private int id;// 商品id
    private int sort;
    private List<Integer> award;// 奖励的物品
    private int price; // 消耗军费

    private List<Integer> schedule; // 世界进程区间。在[1,10]表示1-10区间内的商店开放奖励

    /**
     * 是否在可购买的世界进程阶段
     * @param scId 世界进程阶段
     * @return true 可以购买 false 不可以购买
     */
    public boolean isInSchedule(int scId) {
        if (CheckNull.isEmpty(schedule)) {
            return true;
        }
        return scId >= schedule.get(0) && scId <= schedule.get(1);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public List<Integer> getAward() {
        return award;
    }

    public void setAward(List<Integer> award) {
        this.award = award;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public List<Integer> getSchedule() {
        return schedule;
    }

    public void setSchedule(List<Integer> schedule) {
        this.schedule = schedule;
    }

    @Override
    public String toString() {
        return "StaticBerlinShop{" +
                "id=" + id +
                ", sort=" + sort +
                ", award=" + award +
                ", price=" + price +
                ", schedule=" + schedule +
                '}';
    }

}
