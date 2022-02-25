package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticRebelShop.java
 * @Description 匪军商店
 * @author QiuKun
 * @date 2018年10月24日
 */
public class StaticRebelShop {
    private int id;
    private int template;           // 模板
    private List<Integer> award;    // 获得物品
    private int price;              // 消耗匪军积分
    private int count;              // 购买次数

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTemplate() {
        return template;
    }

    public void setTemplate(int template) {
        this.template = template;
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

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "StaticRebelShop{" +
                "id=" + id +
                ", template=" + template +
                ", award=" + award +
                ", price=" + price +
                ", count=" + count +
                '}';
    }

}
