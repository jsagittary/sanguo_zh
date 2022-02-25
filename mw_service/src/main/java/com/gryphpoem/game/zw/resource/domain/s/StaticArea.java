package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

public class StaticArea {
    private int area; // 分区唯一id,1~25
    private boolean isOpen; // 该区域是否开启，1 是，0 否
    private int openOrder; // 开启次序，初始默认开启:1，首次解锁后开启:2，皇城:3
    private int lowWeight; // 低级迁城，玩家随即到该分区的权重
    private List<Integer> unlockArea; // 首次解锁地图后可以联通的其他区域，格式：[1,2...]
    private List<Integer> openAreaId;// 世界boss1打死后能飞的区域
    private int openSequence; // 郡导入玩家顺序，州和要塞按世界任务开启，0表示不参与排序
    private int unlockTotalWeight;// 记录该区开放后，可关联区的总lowWeight
    private List<Integer> gotoArea; // 前往国家战区配置

    public int getArea() {
        return area;
    }

    public void setArea(int area) {
        this.area = area;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public int getOpenOrder() {
        return openOrder;
    }

    public void setOpenOrder(int openOrder) {
        this.openOrder = openOrder;
    }

    public int getLowWeight() {
        return lowWeight;
    }

    public void setLowWeight(int lowWeight) {
        this.lowWeight = lowWeight;
    }

    public List<Integer> getUnlockArea() {
        return unlockArea;
    }

    public void setUnlockArea(List<Integer> unlockArea) {
        this.unlockArea = unlockArea;
    }

    public int getUnlockTotalWeight() {
        return unlockTotalWeight;
    }

    public void setUnlockTotalWeight(int unlockTotalWeight) {
        this.unlockTotalWeight = unlockTotalWeight;
    }

    public void addUnlockWeight(int weight) {
        this.unlockTotalWeight += weight;
    }

    public List<Integer> getOpenAreaId() {
        return openAreaId;
    }

    public void setOpenAreaId(List<Integer> openAreaId) {
        this.openAreaId = openAreaId;
    }

    public int getOpenSequence() {
        return openSequence;
    }

    public void setOpenSequence(int openSequence) {
        this.openSequence = openSequence;
    }

    public List<Integer> getGotoArea() {
        return gotoArea;
    }

    public void setGotoArea(List<Integer> gotoArea) {
        this.gotoArea = gotoArea;
    }

    public boolean openSequenceAvailable() {
        return this.openSequence > 0;
    }

    @Override
    public String toString() {
        return "StaticArea [area=" + area + ", isOpen=" + isOpen + ", openOrder=" + openOrder + ", lowWeight="
                + lowWeight + ", unlockArea=" + unlockArea + ", openAreaId=" + openAreaId + ", openSequence="
                + openSequence + ", unlockTotalWeight=" + unlockTotalWeight + "]";
    }
}
