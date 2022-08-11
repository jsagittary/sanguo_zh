package com.gryphpoem.game.zw.resource.domain.p;

/**
 * 全服活动记录
 * 
 * @author tyler
 *
 */
public class GlobalActivity {
    private int activityType;
    private int actKeyId;
    private int goal;
    private int equipTurLuckNums;//装备转盘全服抽奖次数统计
    private int sortord;
    private long topupa;// 阵营a的充值记录
    private long topupb;
    private long topupc;
    private byte[] params;
    private int activityTime;
    private int recordTime;
    private byte[] royalArena;
    /**
     * 秋季拍卖活动
     */
    private byte[] auction;

    public long getTopupa() {
        return topupa;
    }

    public void setTopupa(long topupa) {
        this.topupa = topupa;
    }

    public long getTopupb() {
        return topupb;
    }

    public void setTopupb(long topupb) {
        this.topupb = topupb;
    }

    public long getTopupc() {
        return topupc;
    }

    public void setTopupc(long topupc) {
        this.topupc = topupc;
    }

    public int getActivityType() {
        return activityType;
    }

    public void setActivityType(int activityType) {
        this.activityType = activityType;
    }

    public int getGoal() {
        return goal;
    }

    public void setGoal(int goal) {
        this.goal = goal;
    }

    public int getEquipTurLuckNums() {
		return equipTurLuckNums;
	}

	public void setEquipTurLuckNums(int equipTurLuckNums) {
		this.equipTurLuckNums = equipTurLuckNums;
	}

	public int getSortord() {
        return sortord;
    }

    public void setSortord(int sortord) {
        this.sortord = sortord;
    }

    public byte[] getParams() {
        return params;
    }

    public void setParams(byte[] params) {
        this.params = params;
    }

    public int getActivityTime() {
        return activityTime;
    }

    public void setActivityTime(int activityTime) {
        this.activityTime = activityTime;
    }

    public int getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(int recordTime) {
        this.recordTime = recordTime;
    }

    public byte[] getRoyalArena() {
        return royalArena;
    }

    public void setRoyalArena(byte[] royalArena) {
        this.royalArena = royalArena;
    }

    public byte[] getAuction() {
        return auction;
    }

    public void setAuction(byte[] auction) {
        this.auction = auction;
    }

    public int getActKeyId() {
        return actKeyId;
    }

    public void setActKeyId(int actKeyId) {
        this.actKeyId = actKeyId;
    }

    @Override
    public String toString() {
        return "GlobalActivity [activityType=" + activityType + ", goal=" + goal + ", sortord=" + sortord + ", topupa="
                + topupa + ", topupb=" + topupb + ", topupc=" + topupc + ", params=" + params + ", activityTime="
                + activityTime + ", recordTime=" + recordTime + "]";
    }

}
