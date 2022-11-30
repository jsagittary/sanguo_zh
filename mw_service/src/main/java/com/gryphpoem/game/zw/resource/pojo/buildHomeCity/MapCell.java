package com.gryphpoem.game.zw.resource.pojo.buildHomeCity;

/**
 * @Author: GeYuanpeng
 * @Date: 2022/11/30 16:06
 */
public class MapCell {

    private int cellId; // 格子id

    private boolean reclaimed; // 开垦状态

    private int npcId; // 土匪对应的npcId

    private int simType; // 土匪对应的对话引导模拟器

    private int banditRefreshTime; // 土匪刷新的时间, -1表示侦察时出现的, 不清剿不会自动消失

    public MapCell() {
    }

    public MapCell(int cellId, boolean reclaimed, int npcId, int simType, int banditRefreshTime) {
        this.cellId = cellId;
        this.reclaimed = reclaimed;
        this.npcId = npcId;
        this.simType = simType;
        this.banditRefreshTime = banditRefreshTime;
    }

    public int getCellId() {
        return cellId;
    }

    public void setCellId(int cellId) {
        this.cellId = cellId;
    }

    public boolean isReclaimed() {
        return reclaimed;
    }

    public void setReclaimed(boolean reclaimed) {
        this.reclaimed = reclaimed;
    }

    public int getNpcId() {
        return npcId;
    }

    public void setNpcId(int npcId) {
        this.npcId = npcId;
    }

    public int getSimType() {
        return simType;
    }

    public void setSimType(int simType) {
        this.simType = simType;
    }

    public int getBanditRefreshTime() {
        return banditRefreshTime;
    }

    public void setBanditRefreshTime(int banditRefreshTime) {
        this.banditRefreshTime = banditRefreshTime;
    }
}
