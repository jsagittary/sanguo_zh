package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.pb.CommonPb;

/**
 * @ClassName Sectiontask.java
 * @Description 剧情任务的章节
 * @author QiuKun
 * @date 2017年10月26日
 */
public class Sectiontask {
    private int sectionId;
    private int status; // 章节的状态 0 不可领取, 1 可领取, 2 已领取,和任务的状态一样,详见TaskType

    public Sectiontask(int sectionId, int status) {
        this.sectionId = sectionId;
        this.status = status;
    }

    public Sectiontask(CommonPb.Sectiontask ser) {
        this.sectionId = ser.getSectionId();
        this.status = ser.getStatus();
    }

    public CommonPb.Sectiontask ser() {
        CommonPb.Sectiontask.Builder builder = CommonPb.Sectiontask.newBuilder();
        builder.setSectionId(sectionId);
        builder.setStatus(status);
        return builder.build();
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Sectiontask [sectionId=" + sectionId + ", status=" + status + "]";
    }

}
