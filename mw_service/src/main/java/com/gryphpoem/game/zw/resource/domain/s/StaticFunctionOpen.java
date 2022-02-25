package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticFunctionOpen.java
 * @Description 建筑解锁等级,服务器这边主要用于资源建筑解锁使用,其他建筑还是之前的逻辑
 * @author QiuKun
 * @date 2017年8月21日
 */
public class StaticFunctionOpen {
    private int typeId; // 建筑ID，功能ID
    private int lv; // 解锁需要的玩家等级，1为默认开启
    private List<Integer> needBuilding; // 解锁需要的建筑等级，格式为[建筑类型ID，建筑数量，建筑等级]
    private int taskId; // 任务条件，完成任务解锁
    private int combatId; // 通关关卡解锁
    private int stoneCombatid;// 通关宝石关卡解锁
    private List<List<Integer>> serverId; // 区服的指定
    private int scheduleId; // 世界进度条件(世界进度id)

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public List<Integer> getNeedBuilding() {
        return needBuilding;
    }

    public void setNeedBuilding(List<Integer> needBuilding) {
        this.needBuilding = needBuilding;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getCombatId() {
        return combatId;
    }

    public void setCombatId(int combatId) {
        this.combatId = combatId;
    }

    public int getStoneCombatid() {
        return stoneCombatid;
    }

    public void setStoneCombatid(int stoneCombatid) {
        this.stoneCombatid = stoneCombatid;
    }

    public List<List<Integer>> getServerId() {
        return serverId;
    }

    public void setServerId(List<List<Integer>> serverId) {
        this.serverId = serverId;
    }

    public int getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }
}
