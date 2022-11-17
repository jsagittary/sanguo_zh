package com.gryphpoem.game.zw.resource.domain.p;

/**
 * 建造队列
 * 
 * @author tyler
 *
 */
public class BuildQue {
    private int keyId;
    private int index;
    private int buildingType;
    private int pos;//对应的buildingId
    private int period;
    private int endTime;
    private int free;// 是否有免费加速，0 没有，1 资源采集获得的免费，2 VIP免费
    private int param;// 如果是个人资源采集获得的免费加速，记录免费时间
    private int fromType;// 队列的来源类型, 0 手动队列 , 1 自动建造队列  2 改建
    private int newType;// 改建后的type   fromType为2的时候才有值
    private int foundationId; // 初次建造时选择的地基id

    /**
     * 是否有免费加速没有使用
     * 
     * @return
     */
    public boolean haveFreeSpeed() {
        return free > 0;
    }

    /**
     * 清除免费加速
     */
    public void clearFree() {
        setFree(0);
        setParam(0);
    }

    /**
     * 使用免费加速
     */
    public void useFreeSpeed() {
        // 减结束时间
        endTime -= param;

        // 清除免费加速状态
        clearFree();
    }

    public int getBuildingType() {
        return buildingType;
    }

    public void setBuildingType(int buildingType) {
        this.buildingType = buildingType;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getFree() {
        return free;
    }

    public void setFree(int free) {
        this.free = free;
    }

    public int getParam() {
        return param;
    }

    public void setParam(int param) {
        this.param = param;
    }

    public int getFromType() {
        return fromType;
    }

    public void setFromType(int fromType) {
        this.fromType = fromType;
    }

    public int getNewType() {
		return newType;
	}

	public void setNewType(int newType) {
		this.newType = newType;
	}

    public int getFoundationId() {
        return foundationId;
    }

    public void setFoundationId(int foundationId) {
        this.foundationId = foundationId;
    }

    /**
     * @param keyId
     * @param buildingId
     * @param pos
     * @param period
     * @param endTime
     */
    public BuildQue(int keyId, int index, int buildingType, int pos, int period, int endTime) {
        super();
        this.keyId = keyId;
        this.buildingType = buildingType;
        this.pos = pos;
        this.period = period;
        this.endTime = endTime;
        this.index = index;
    }

    @Override
    public String toString() {
        return "BuildQue [keyId=" + keyId + ", index=" + index + ", buildingType=" + buildingType + ", pos=" + pos
                + ", period=" + period + ", endTime=" + endTime + ", free=" + free + ", param=" + param + ", fromType="
                + fromType + "]";
    }

}
