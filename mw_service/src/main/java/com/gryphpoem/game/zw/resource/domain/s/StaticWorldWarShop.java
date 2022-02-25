package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * Created by pengshuo on 2019/3/26 14:16
 * <br>Description:
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class StaticWorldWarShop {
    /** id */
    private int id;
    /** 消耗 */
    private int costPoint;
    /** 兑换所得物品 格式[type,id,cnt,weight] */
    private List<List<Integer>> award;
    /** 道具可购买次数 填0表示购买不受限制 */
    private int buyCnt;
    /** 描述 */
    private String desc;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCostPoint() {
        return costPoint;
    }

    public void setCostPoint(int costPoint) {
        this.costPoint = costPoint;
    }

    public List<List<Integer>> getAward() {
        return award;
    }

    public void setAward(List<List<Integer>> award) {
        this.award = award;
    }

    public int getBuyCnt() {
        return buyCnt;
    }

    public void setBuyCnt(int buyCnt) {
        this.buyCnt = buyCnt;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "StaticWorldWarShop{" +
                "id=" + id +
                ", costPoint=" + costPoint +
                ", award=" + award +
                ", buyCnt=" + buyCnt +
                ", desc='" + desc + '\'' +
                '}';
    }
}
