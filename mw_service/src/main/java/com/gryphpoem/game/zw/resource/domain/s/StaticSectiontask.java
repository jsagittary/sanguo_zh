package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticSectiontask.java
 * @Description 剧情配置(非剧情任务)
 * @author QiuKun
 * @date 2017年10月26日
 */
public class StaticSectiontask {
    private int sectionId;
    private List<Integer> sectionTask; // 本剧情包含的剧情任务
    private List<List<Integer>> sectionAward;// 剧情完成的奖励

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public List<Integer> getSectionTask() {
        return sectionTask;
    }

    public void setSectionTask(List<Integer> sectionTask) {
        this.sectionTask = sectionTask;
    }

    public List<List<Integer>> getSectionAward() {
        return sectionAward;
    }

    public void setSectionAward(List<List<Integer>> sectionAward) {
        this.sectionAward = sectionAward;
    }

    @Override
    public String toString() {
        return "StaticSectiontask [sectionId=" + sectionId + ", sectionTask=" + sectionTask + ", sectionAward="
                + sectionAward + "]";
    }

}
