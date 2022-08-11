package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * desc: 任务章节配置
 * author: huangxm
 * date: 2022/5/25 13:50
 **/
public class StaticTaskChapter {
    // 章节&排序
    private int chapterId;
    // 备注
    private String desc;
    // 章节奖励
    private List<List<Integer>> awardList;
    // 对应的迭代周期，如当前所处迭代周期小于此值，则前后端屏蔽该章节。
    private int generation;
    // 该本章开启的时代。1无；2青铜时代；3黑暗时代；4封建时代；5城堡时代；6帝王时代
    private int age;
    // 本章涵盖的任务id
    private List<Integer> taskId;

    public int getChapterId() {
        return chapterId;
    }

    public void setChapterId(int chapterId) {
        this.chapterId = chapterId;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<List<Integer>> getAwardList() {
        return awardList;
    }

    public void setAwardList(List<List<Integer>> awardList) {
        this.awardList = awardList;
    }

    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public List<Integer> getTaskId() {
        return taskId;
    }

    public void setTaskId(List<Integer> taskId) {
        this.taskId = taskId;
    }

    @Override
    public String toString() {
        return "StaticTaskChapter{" +
                "chapterId=" + chapterId +
                ", desc='" + desc + '\'' +
                ", awardList=" + awardList +
                ", generation=" + generation +
                ", age=" + age +
                ", taskId=" + taskId +
                '}';
    }
}
