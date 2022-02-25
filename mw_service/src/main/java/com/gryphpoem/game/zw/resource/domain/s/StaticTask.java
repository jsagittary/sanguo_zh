package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.resource.constant.TaskType;

import java.util.List;

public class StaticTask {

    private int taskId;
    private int type;
    private int typeChild;
    private int triggerId;
    private int cond;
    private int condId;
    private int schedule;
    private int probability;
    private List<List<Integer>> awardList;
    private int isOpen;// 是否在出生就开启任务记录,针对累计计数的任务
    private int isGet;// 是否给予奖励,0不给,只做显示,1给奖励
    private int subType; // 子类型 1主线,2装备,3科技,4建筑,5军事,6募兵,7流寇,8副本
    private String desc; // 描述
    private int mainSort;// 主线任务排序使用

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getTypeChild() {
        return typeChild;
    }

    public void setTypeChild(int typeChild) {
        this.typeChild = typeChild;
    }

    public int getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(int triggerId) {
        this.triggerId = triggerId;
    }

    public int getCond() {
        return cond;
    }

    public void setCond(int cond) {
        this.cond = cond;
    }

    public int getSchedule() {
        return schedule;
    }

    public void setSchedule(int schedule) {
        this.schedule = schedule;
    }

    public int getBuildTaskLv() {
        int lv = 0;
        if (cond == TaskType.COND_BUILDING_TYPE_LV) {
            lv = schedule;
        } else if (cond == TaskType.COND_RES_FOOD_CNT || cond == TaskType.COND_RES_OIL_CNT || cond == TaskType.COND_RES_ELE_CNT || cond == TaskType.COND_RES_ORE_CNT) {
            lv = condId;
        }
        return lv;
    }

    public int getCondId() {
        return condId;
    }

    public void setCondId(int condId) {
        this.condId = condId;
    }

    public int getProbability() {
        return probability;
    }

    public void setProbability(int probability) {
        this.probability = probability;
    }

    public List<List<Integer>> getAwardList() {
        return awardList;
    }

    public void setAwardList(List<List<Integer>> awardList) {
        this.awardList = awardList;
    }

    public int getIsOpen() {
        return isOpen;
    }

    public void setIsOpen(int isOpen) {
        this.isOpen = isOpen;
    }

    public int getIsGet() {
        return isGet;
    }

    public void setIsGet(int isGet) {
        this.isGet = isGet;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getSubType() {
        return subType;
    }

    public void setSubType(int subType) {
        this.subType = subType;
    }
    

    public int getMainSort() {
        return mainSort;
    }

    public void setMainSort(int mainSort) {
        this.mainSort = mainSort;
    }

    @Override
    public String toString() {
        return "StaticTask [taskId=" + taskId + ", type=" + type + ", typeChild=" + typeChild + ", triggerId="
                + triggerId + ", cond=" + cond + ", condId=" + condId + ", schedule=" + schedule + ", probability="
                + probability + ", awardList=" + awardList + ", isOpen=" + isOpen + ", isGet=" + isGet + ", subType="
                + subType + ", desc=" + desc + ", mainSort=" + mainSort + "]";
    }

}
