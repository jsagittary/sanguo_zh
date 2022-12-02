package com.gryphpoem.game.zw.resource.pojo.buildHomeCity;

import com.gryphpoem.game.zw.pb.CommonPb;

/**
 * @Author: GeYuanpeng
 * @Date: 2022/11/30 16:06
 */
public class MapCell {

    private int cellId; // 格子id

    private boolean reclaimed; // 开垦状态

    private int npcId; // 土匪对应的npcId, s_sim_npc的id

    private int simType; // 对话类型：1, sim_beginnerguide的type; 2, s_beginnerguide_new的keyId

    private int simId; // sim_beginnerguide的type或s_beginnerguide_new的keyId

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

    public boolean getReclaimed() {
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

    public boolean isReclaimed() {
        return reclaimed;
    }

    public int getSimId() {
        return simId;
    }

    public void setSimId(int simId) {
        this.simId = simId;
    }

    public int getBanditRefreshTime() {
        return banditRefreshTime;
    }

    public void setBanditRefreshTime(int banditRefreshTime) {
        this.banditRefreshTime = banditRefreshTime;
    }

    public CommonPb.MapCell ser() {
        CommonPb.MapCell.Builder builder = CommonPb.MapCell.newBuilder();
        builder.setCellId(this.cellId);
        builder.setReclaimed(this.reclaimed);
        builder.setNpcId(this.npcId);
        builder.setSimType(this.simType);
        builder.setBanditRefreshTime(this.banditRefreshTime);
        builder.setSimId(this.simId);
        return builder.build();
    }

    public MapCell dser(CommonPb.MapCell pb) {
        this.setCellId(pb.getCellId());
        this.setReclaimed(pb.getReclaimed());
        this.setNpcId(pb.getNpcId());
        this.setSimType(pb.getSimType());
        this.setBanditRefreshTime(pb.getBanditRefreshTime());
        this.setSimId(pb.getSimId());
        return this;
    }

}
