package com.gryphpoem.game.zw.resource.pojo.season;

import com.gryphpoem.game.zw.resource.domain.p.AwardItem;
import com.gryphpoem.game.zw.resource.pojo.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * 赛季 宝库
 * state 0未完成 1已完成 2已领取
 * @author xwind
 * @date 2021/4/17
 */
public class SeasonTreasury extends Task {
//    private int taskId;
//    private int progress;
//    private int state;//
    private List<AwardItem> awards = new ArrayList<>();

    public SeasonTreasury(){}

    public SeasonTreasury(int taskId) {
        super(taskId);
    }

    public List<AwardItem> getAwards() {
        return awards;
    }

    public void setAwards(List<AwardItem> awards) {
        this.awards = awards;
    }

    @Override
    public void setSchedule(long schedule) {
        super.setSchedule0(schedule);
    }
}
