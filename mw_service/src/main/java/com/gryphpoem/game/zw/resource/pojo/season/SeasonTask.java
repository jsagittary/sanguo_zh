package com.gryphpoem.game.zw.resource.pojo.season;

import com.gryphpoem.game.zw.resource.pojo.Task;

/**
 * 赛季旅程任务
 * 0未完成1已完成2已领奖
 * @author xwind
 * @date 2021/4/17
 */
public class SeasonTask extends Task {

    public SeasonTask(){}

    public SeasonTask(int taskId) {
        super(taskId);
    }

    @Override
    public void setSchedule(long schedule) {
        super.setSchedule0(schedule);
    }
}
