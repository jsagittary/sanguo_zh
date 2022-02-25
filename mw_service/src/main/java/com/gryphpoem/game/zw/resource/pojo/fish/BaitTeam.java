package com.gryphpoem.game.zw.resource.pojo.fish;

import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.domain.p.AwardItem;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.fish.FishingConst;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * 采集鱼饵队列
 * @author xwind
 * @date 2021/8/6
 */
public class BaitTeam {
    private int teamId;//队列id 1-5
    private int [] heroIds = new int[4];
    private int state;//队列状态 0未派遣 1已派遣 2可领取 3已领取
    private int groupId;//激活组合的组合配置id
    private int lastDay;//最后使用队列的日期
    private List<AwardItem> getAwards = new ArrayList<>();//采集获得的鱼饵
    private int dispatchTime;//派遣采集的时间戳
    private int needSec;//采集完成需要的秒数

    public BaitTeam(int teamId){
        this.teamId = teamId;
    }

    public BaitTeam(){}

    public SerializePb.SerBaitTeam ser(){
        SerializePb.SerBaitTeam.Builder builder = SerializePb.SerBaitTeam.newBuilder();
        builder.setTeamId(teamId);
        Arrays.stream(heroIds).forEach(id -> builder.addHeroIds(id));
        builder.setState(state);
        builder.setGroupId(groupId);
        builder.setLastDay(lastDay);
        getAwards.forEach(o -> builder.addGetAwards(PbHelper.createAward(o)));
        builder.setDispatchTime(dispatchTime);
        builder.setNeedSec(needSec);
        return builder.build();
    }

    public BaitTeam dser(SerializePb.SerBaitTeam serBaitTeam){
        this.teamId = serBaitTeam.getTeamId();
        Stream.iterate(0,i->i+1).limit(heroIds.length).forEach(idx -> heroIds[idx] = serBaitTeam.getHeroIds(idx));
        this.state = serBaitTeam.getState();
        this.groupId = serBaitTeam.getGroupId();
        this.lastDay = serBaitTeam.getLastDay();
        serBaitTeam.getGetAwardsList().forEach(o -> this.getAwards.add(new AwardItem(o.getType(),o.getId(),o.getCount())));
        this.dispatchTime = serBaitTeam.getDispatchTime();
        this.needSec = serBaitTeam.getNeedSec();
        return this;
    }

    public void reset() {
        if (this.state != FishingConst.TEAM_STATE_DOING && this.state != FishingConst.TEAM_STATE_NON) {
            Stream.iterate(0, i -> i + 1).limit(heroIds.length).forEach(idx -> heroIds[idx] = 0);
            this.setState(0);
            this.setGroupId(0);
            this.getGetAwards().clear();
            this.setDispatchTime(0);
            this.setNeedSec(0);
        }
    }

    public int teamState() {
        if (state == FishingConst.TEAM_STATE_DOING && dispatchTime + needSec <= TimeHelper.getCurrentSecond()) {
            return FishingConst.TEAM_STATE_GET;
        }
        return state;
    }

    public int usedTimes(){
        if(lastDay == TimeHelper.getCurrentDay()){
            return 1;
        }else {
            return 0;
        }
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public int[] getHeroIds() {
        return heroIds;
    }

    public void setHeroIds(List<Integer> idList) {
        Stream.iterate(0,i->i+1).limit(heroIds.length).forEach(idx -> heroIds[idx] = idList.get(idx));
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getLastDay() {
        return lastDay;
    }

    public void setLastDay(int lastDay) {
        this.lastDay = lastDay;
    }

    public List<AwardItem> getGetAwards() {
        return getAwards;
    }

    public void setGetAwards(List<AwardItem> getAwards) {
        this.getAwards = getAwards;
    }

    public int getDispatchTime() {
        return dispatchTime;
    }

    public void setDispatchTime(int dispatchTime) {
        this.dispatchTime = dispatchTime;
    }

    public int getNeedSec() {
        return needSec;
    }

    public void setNeedSec(int needSec) {
        this.needSec = needSec;
    }
}
