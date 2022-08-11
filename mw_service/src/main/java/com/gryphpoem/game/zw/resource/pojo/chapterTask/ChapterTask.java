package com.gryphpoem.game.zw.resource.pojo.chapterTask;

import com.google.common.collect.Maps;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.pojo.Task;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.util.Map;

/**
 * desc: 章节任务个人数据记录
 * author: huangxm
 * date: 2022/5/25 9:52
 **/
public class ChapterTask {
    /**
     * 当前章节
     */
    private int chapterId;

    /**
     * 玩家身上的任务
     * k:taskId v:任务
     */
    private Map<Integer, Task> openTasks = Maps.newHashMap();
    /**
     * 章节奖励领取状态
     * k:taskId v:1
     */
    private Map<Integer, Integer> chapterStatus = Maps.newHashMap();

    public ChapterTask() {
        this.chapterId = 1;
    }

    public int getChapterId() {
        return chapterId;
    }

    public Map<Integer, Task> getOpenTasks() {
        return openTasks;
    }

    public Map<Integer, Integer> getChapterStatus() {
        return chapterStatus;
    }


    public void setChapter(int chapterId) {
        reset(chapterId);
        this.chapterId = chapterId;
    }

    /**
     * 当章节改变时  重置任务
     */
    private void reset(int chapterId) {
        if (this.chapterId == chapterId) return;
    }


    /**
     * 当前章节奖励是否领取
     *
     * @return
     */
    public boolean currentChapterIsReceive() {
        return chapterStatus.containsKey(chapterId);
    }

    public SerializePb.SerChapterTask ser() {
        SerializePb.SerChapterTask.Builder builder = SerializePb.SerChapterTask.newBuilder();
        builder.setChapterId(chapterId);
        openTasks.values().forEach(e -> {
            builder.addOpenTasks(PbHelper.createTaskPb(e));
        });
        chapterStatus.forEach((k, v) -> {
            builder.addChapterStatus(CommonPb.TwoInt.newBuilder().setV1(k).setV2(v).build());
        });
        return builder.build();
    }

    public void dser(SerializePb.SerChapterTask ser) {
        chapterId = ser.getChapterId() == 0 ? 1 : ser.getChapterId();
        ser.getOpenTasksList().forEach(e -> {
            Task task = new Task(e.getTaskId(), e.getSchedule(), e.getStatus(), 1);
            openTasks.put(task.getTaskId(), task);
        });
        ser.getChapterStatusList().forEach(e -> {
            chapterStatus.put(e.getV1(), e.getV2());
        });
    }

}
