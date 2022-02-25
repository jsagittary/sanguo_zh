package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticActBlackhawk.java
 * @Description 黑鹰计划活动
 * @author QiuKun
 * @date 2017年7月10日
 */
public class StaticActBlackhawk {

    private int keyId;
    private int cond;// 商店格子编号，总共6格
    private List<List<Integer>> awardList; // 商店出售道具[[类型,ID,数量,权重],……]]，权重用于刷新几率
    private int price;// 初始价格

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getCond() {
        return cond;
    }

    public void setCond(int cond) {
        this.cond = cond;
    }

    public List<List<Integer>> getAwardList() {
        return awardList;
    }

    public void setAwardList(List<List<Integer>> awardList) {
        this.awardList = awardList;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "StaticActBlackhawk [keyId=" + keyId + ", cond=" + cond + ", awardList=" + awardList + ", price=" + price
                + "]";
    }

}
