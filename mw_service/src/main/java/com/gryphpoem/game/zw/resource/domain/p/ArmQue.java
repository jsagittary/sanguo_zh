package com.gryphpoem.game.zw.resource.domain.p;

/**
 * 招募队列
 * 
 * @author tyler
 *
 */
public class ArmQue {
    private int keyId;
    private int buildingId;
    private int addArm;
    private int endTime;
    private int time;// 募兵花费时间(几个5分钟)
    private int free;// 是否有免费加速，0 没有，1 资源采集获得的免费
    private int param;// 如果是个人资源采集获得的免费加速，记录免费时间
    private int needFood; // 消耗粮食, 用于取消返还
    private boolean isNotExtendQue;// 是否在非预备队列上,用于解决招募次数类型的任务
    private int needOIL;// 消耗燃油, 用于取消返还

    public ArmQue(int keyId, int buildingId, int addArm, int endTime, int time, int needFood, int needOIL) {
        this.keyId = keyId;
        this.buildingId = buildingId;
        this.addArm = addArm;
        this.endTime = endTime;
        this.time = time;
        this.needFood = needFood;
        this.needOIL = needOIL;
    }

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

        clearFree();
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(int buildingId) {
        this.buildingId = buildingId;
    }

    public int getAddArm() {
        return addArm;
    }

    public void setAddArm(int addArm) {
        this.addArm = addArm;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
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

    public int getNeedFood() {
        return needFood;
    }

    public void setNeedFood(int needFood) {
        this.needFood = needFood;
    }

    public boolean isNotExtendQue() {
        return isNotExtendQue;
    }

    public void setNotExtendQue(boolean isNotExtendQue) {
        this.isNotExtendQue = isNotExtendQue;
    }

    public int getNeedOIL() {
        return needOIL;
    }

    public void setNeedOIL(int needOIL) {
        this.needOIL = needOIL;
    }

    @Override
    public String toString() {
        return "ArmQue{" +
                "keyId=" + keyId +
                ", buildingId=" + buildingId +
                ", addArm=" + addArm +
                ", endTime=" + endTime +
                ", time=" + time +
                ", free=" + free +
                ", param=" + param +
                ", needFood=" + needFood +
                ", isNotExtendQue=" + isNotExtendQue +
                ", needOIL=" + needOIL +
                '}';
    }

}
