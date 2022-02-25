package com.gryphpoem.game.zw.resource.pojo;

import com.gryphpoem.game.zw.pb.CommonPb;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: ZhouJie
 * @date: Create in 2019-03-21 16:14
 * @description: 装备戒指
 * @modified By:
 */
public class EquipJewel {
    /**
     * 戒指id
     */
    private int jewelId;
    /**
     * 拥有的数量
     */
    private int count;
    /**
     * 被镶嵌的数量
     */
    private AtomicInteger inlaid = new AtomicInteger();

    /**
     * 空参构造
     */
    public EquipJewel() {
    }

    /**
     * 反序列化装备宝石构造
     * @param jewel
     */
    public EquipJewel(CommonPb.EquipJewel jewel) {
        this.jewelId = jewel.getJewelId();
        this.count = jewel.getCount();
        this.inlaid = new AtomicInteger(jewel.getInlaid());
    }

    /**
     * 获取可使用的宝石数量
     * @return
     */
    public int canUseCnt() {
        return count - inlaid.get();
    }

    /**
     * 添加拥有的宝石数量
     * @param add
     */
    public void addCount(int add) {
        this.count += add;
    }

    /**
     * 减少拥有的宝石数量
     * @param count
     */
    public void subCount(int count) {
        this.count -= count;
        if (count <= 0) {
            this.count = 0;
        }
    }

    public int getJewelId() {
        return jewelId;
    }

    public void setJewelId(int jewelId) {
        this.jewelId = jewelId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public AtomicInteger getInlaid() {
        return inlaid;
    }

    public void setInlaid(AtomicInteger inlaid) {
        this.inlaid = inlaid;
    }

}
