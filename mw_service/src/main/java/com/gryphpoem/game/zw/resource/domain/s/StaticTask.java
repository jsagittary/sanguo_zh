package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.resource.constant.TaskType;

import java.util.List;

public class StaticTask {
    // 任务编号
    private int taskId;
    // 1.主线任务 2支线任务 3.日常任务 4.活跃任务 5.剧情任务
    private int type;
    // 任务子类型，主线，剧情1，支线2.3.4……
    private int subType;
    // 任务显示图标：1主线,2装备,3科技,4建筑,5军事,6募兵,7流寇,8副本
    private int displayType;
    //
    private int mainSort;
    // 触发该任务的任务ID，强引任务用
    private int triggerId;
    // 软前置任务id，触发当前任务引导时，先判断preid的任务是否完成。是，则执行当前任务引导；否，则执行preid对应的任务引导。配置需支持嵌套触发。该字段与triggerId只填其一
    private int preId;
    // 是否在出生就开启任务记录,针对累计计数的任务
    private int isOpen;
    // 任务条件类型
    private int cond;
    // 任务条件ID
    private int condId;
    // 任务完成次数
    private int schedule;
    // 物品道具奖励[[itemType,itemId,itemCount]...]
    private List<List<Integer>> awardList;
    // 是否给予奖励,0不给,只做显示,1给奖励
    private int isGet;
    // 该任务对应的前置章节；领取该章的章节奖励是开启此任务的前置条件
    private int preChapter;
    // 主线任务对应的章节。策划备注看
    private int chapter;
    // 任务名,只用于查看表内数据
    private String desc;
    // 引导ID,任务开始弹出引导
    private int guideId;
    // 非强引章节对应的引导id配置。guideId和guideIdNew都不配置参数时，该任务无【前往】按钮。
    private int guideIdNew;
    private int nextGuideId;
    // 迭代周期
    private int generation;

    public int getBuildTaskLv() {
        int lv = 0;
        if (cond == TaskType.COND_BUILDING_TYPE_LV) {
            lv = schedule;
        } else if (cond == TaskType.COND_RES_FOOD_CNT || cond == TaskType.COND_RES_OIL_CNT || cond == TaskType.COND_RES_ELE_CNT || cond == TaskType.COND_RES_ORE_CNT) {
            lv = condId;
        }
        return lv;
    }

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

    public int getSubType() {
        return subType;
    }

    public void setSubType(int subType) {
        this.subType = subType;
    }

    public int getDisplayType() {
        return displayType;
    }

    public void setDisplayType(int displayType) {
        this.displayType = displayType;
    }

    public int getMainSort() {
        return mainSort;
    }

    public void setMainSort(int mainSort) {
        this.mainSort = mainSort;
    }

    public int getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(int triggerId) {
        this.triggerId = triggerId;
    }

    public int getPreId() {
        return preId;
    }

    public void setPreId(int preId) {
        this.preId = preId;
    }

    public int getIsOpen() {
        return isOpen;
    }

    public void setIsOpen(int isOpen) {
        this.isOpen = isOpen;
    }

    public int getCond() {
        return cond;
    }

    public void setCond(int cond) {
        this.cond = cond;
    }

    public int getCondId() {
        return condId;
    }

    public void setCondId(int condId) {
        this.condId = condId;
    }

    public int getSchedule() {
        return schedule;
    }

    public void setSchedule(int schedule) {
        this.schedule = schedule;
    }

    public List<List<Integer>> getAwardList() {
        return awardList;
    }

    public void setAwardList(List<List<Integer>> awardList) {
        this.awardList = awardList;
    }

    public int getIsGet() {
        return isGet;
    }

    public void setIsGet(int isGet) {
        this.isGet = isGet;
    }

    public int getPreChapter() {
        return preChapter;
    }

    public void setPreChapter(int preChapter) {
        this.preChapter = preChapter;
    }

    public int getChapter() {
        return chapter;
    }

    public void setChapter(int chapter) {
        this.chapter = chapter;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getGuideId() {
        return guideId;
    }

    public void setGuideId(int guideId) {
        this.guideId = guideId;
    }

    public int getGuideIdNew() {
        return guideIdNew;
    }

    public void setGuideIdNew(int guideIdNew) {
        this.guideIdNew = guideIdNew;
    }

    public int getNextGuideId() {
        return nextGuideId;
    }

    public void setNextGuideId(int nextGuideId) {
        this.nextGuideId = nextGuideId;
    }


    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    @Override
    public String toString() {
        return "StaticTaskNew{" +
                "taskId=" + taskId +
                ", type=" + type +
                ", subType=" + subType +
                ", displayType=" + displayType +
                ", mainSort=" + mainSort +
                ", triggerId=" + triggerId +
                ", preId=" + preId +
                ", isOpen=" + isOpen +
                ", cond=" + cond +
                ", condId=" + condId +
                ", schedule=" + schedule +
                ", awardList=" + awardList +
                ", isGet=" + isGet +
                ", preChapter=" + preChapter +
                ", chapter=" + chapter +
                ", desc='" + desc + '\'' +
                ", guideId=" + guideId +
                ", guideIdNew=" + guideIdNew +
                ", nextGuideId=" + nextGuideId +
                ", generation=" + generation +
                '}';
    }

}
