package com.gryphpoem.game.zw.resource.pojo.season;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.domain.p.AwardItem;
import com.gryphpoem.game.zw.resource.pojo.FunCard;
import com.gryphpoem.game.zw.resource.util.ListUtils;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 赛季玩家数据
 * @author xwind
 * @date 2021/4/17
 */
public class PlayerSeasonData {

    //宝库任务
    private Map<Integer,SeasonTreasury> treasuryMap = new HashMap<>();
    private int treasuryState;//1进行中 2已生成奖励 3已领取了奖励
    private int treasuryAwardTime;//下次生成奖励时间戳
    private int treasuryResetTime;//下次重置任务时间戳
    //旅程任务
    private Map<Integer,SeasonTask> currTasks = new HashMap<>();
    //旅程任务已完成的
    private Map<Integer,SeasonTask> finishedTasks = new HashMap<>();
    //旅程任务积分
    private int taskScore;
    //旅程任务已领取的积分奖励
    private Map<Integer,Integer> gotScoreAward = new HashMap<>();
    //赛季月卡 key=配置id
    private Map<Integer, FunCard> monthCards = new HashMap<>();
    //赛季积分
    private int seasonScore;
    private int seasonScoreTime;
    //完成宝库任务个数
    private int finishedCount;
    //领取宝库奖励次数
    private int getAwardCount;
    //赛季天赋
    private SeasonTalent seasonTalent = new SeasonTalent();

    public SerializePb.SerPlayerSeasonInfo ser(){
        SerializePb.SerPlayerSeasonInfo.Builder builder = SerializePb.SerPlayerSeasonInfo.newBuilder();
        treasuryMap.forEach((k,v) -> builder.addTreasuryMap(buildSerSeasonTask(v.getTaskId(),v.getSchedule(),v.getStatus(), PbHelper.createAwards(v.getAwards()))));
        builder.setTreasuryState(this.treasuryState);
        builder.setTreasuryAwardTime(this.treasuryAwardTime);
        builder.setTreasuryResetTime(this.treasuryResetTime);
        currTasks.forEach((k,v) -> builder.addCurrTasks(buildSerSeasonTask(v.getTaskId(),v.getSchedule(),v.getStatus(),null)));
        finishedTasks.forEach((k,v) -> builder.addFinishedTasks(buildSerSeasonTask(v.getTaskId(),v.getSchedule(),v.getStatus(),null)));
        builder.setTaskScore(this.taskScore);
        gotScoreAward.forEach((k,v) -> builder.addGotScoreAward(PbHelper.createTwoIntPb(k,v)));
        monthCards.forEach((k,v) -> builder.addMonthCards(v.ser()));
        builder.setSeasonScore(this.seasonScore);
        builder.setSeasonScoreTime(this.seasonScoreTime);
        builder.setFinishedCount(this.finishedCount);
        builder.setGetAwardCount(this.getAwardCount);
        builder.setTalent(seasonTalent.ser());
        return builder.build();
    }

    private SerializePb.SerSeasonTask buildSerSeasonTask(int taskId, long schedule, int status, List<CommonPb.Award> list){
        SerializePb.SerSeasonTask.Builder builder = SerializePb.SerSeasonTask.newBuilder();
        builder.setTaskId(taskId);
        builder.setSchedule(schedule);
        builder.setStatus(status);
        if(ListUtils.isNotBlank(list)){
            builder.addAllAward(list);
        }
        return builder.build();
    }

    public void deser(SerializePb.SerPlayerSeasonInfo serPlayerSeasonInfo){
        Optional.ofNullable(serPlayerSeasonInfo).ifPresent(ser -> {
            ser.getTreasuryMapList().forEach(tmp -> {
                SeasonTreasury seasonTreasury = new SeasonTreasury();
                seasonTreasury.setTaskId(tmp.getTaskId());
                seasonTreasury.setSchedule(tmp.getSchedule());
                seasonTreasury.setStatus(tmp.getStatus());
                tmp.getAwardList().forEach(tmp1 -> {
                    AwardItem awardItem = new AwardItem(tmp1.getType(),tmp1.getId(),tmp1.getCount());
                    seasonTreasury.getAwards().add(awardItem);
                });
                this.treasuryMap.put(seasonTreasury.getTaskId(),seasonTreasury);
            });
            this.treasuryState = ser.getTreasuryState();
            this.treasuryAwardTime = ser.getTreasuryAwardTime();
            this.treasuryResetTime = ser.getTreasuryResetTime();
            ser.getCurrTasksList().forEach(tmp -> {
                SeasonTask seasonTask = new SeasonTask();
                seasonTask.setTaskId(tmp.getTaskId());
                seasonTask.setSchedule(tmp.getSchedule());
                seasonTask.setStatus(tmp.getStatus());
                this.currTasks.put(seasonTask.getTaskId(),seasonTask);
            });
            ser.getFinishedTasksList().forEach(tmp -> {
                SeasonTask seasonTask = new SeasonTask();
                seasonTask.setTaskId(tmp.getTaskId());
                seasonTask.setSchedule(tmp.getSchedule());
                seasonTask.setStatus(tmp.getStatus());
                this.finishedTasks.put(seasonTask.getTaskId(),seasonTask);
            });
            this.taskScore = ser.getTaskScore();
            ser.getGotScoreAwardList().forEach(tmp -> this.gotScoreAward.put(tmp.getV1(),tmp.getV2()));
            ser.getMonthCardsList().forEach(tmp -> {
                FunCard funCard = new FunCard(tmp.getType());
                funCard.dser(tmp);
                this.monthCards.put(funCard.getType(),funCard);
            });
            this.seasonScore = ser.getSeasonScore();
            this.seasonScoreTime = ser.getSeasonScoreTime();
            this.finishedCount = ser.getFinishedCount();
            this.getAwardCount = ser.getGetAwardCount();
            this.seasonTalent.deser(ser.getTalent());
        });
    }

    public void clearData(){
        treasuryMap.clear();
        treasuryState = 0;
        treasuryAwardTime = 0;
        treasuryResetTime = 0;
        currTasks.clear();
        finishedTasks.clear();
        taskScore = 0;
        gotScoreAward.clear();
        seasonScore = 0;
        seasonScoreTime = 0;
        finishedCount = 0;
        getAwardCount = 0;
    }

    public Map<Integer, SeasonTreasury> getTreasuryMap() {
        return treasuryMap;
    }

    public Map<Integer, SeasonTask> getFinishedTasks() {
        return finishedTasks;
    }

    public Map<Integer, SeasonTask> getCurrTasks() {
        return currTasks;
    }

    public int getTaskScore() {
        return taskScore;
    }

    public void setTaskScore(int taskScore) {
        this.taskScore = taskScore;
    }

    public Map<Integer, Integer> getGotScoreAward() {
        return gotScoreAward;
    }

    public Map<Integer, FunCard> getMonthCards() {
        return monthCards;
    }

    public int getTreasuryState() {
        return treasuryState;
    }

    public void setTreasuryState(int treasuryState) {
        this.treasuryState = treasuryState;
    }

    public int getTreasuryAwardTime() {
        return treasuryAwardTime;
    }

    public void setTreasuryAwardTime(int treasuryAwardTime) {
        this.treasuryAwardTime = treasuryAwardTime;
    }

    public int getTreasuryResetTime() {
        return treasuryResetTime;
    }

    public void setTreasuryResetTime(int treasuryResetTime) {
        this.treasuryResetTime = treasuryResetTime;
    }

    public int getSeasonScore() {
        return seasonScore;
    }

    public void setSeasonScore(int seasonScore) {
        this.seasonScore = seasonScore;
    }

    public int getSeasonScoreTime() {
        return seasonScoreTime;
    }

    public void setSeasonScoreTime(int seasonScoreTime) {
        this.seasonScoreTime = seasonScoreTime;
    }

    public int getFinishedCount() {
        return finishedCount;
    }

    public void setFinishedCount(int finishedCount) {
        this.finishedCount = finishedCount;
    }

    public int getGetAwardCount() {
        return getAwardCount;
    }

    public void setGetAwardCount(int getAwardCount) {
        this.getAwardCount = getAwardCount;
    }

    public SeasonTalent getSeasonTalent() {
        return seasonTalent;
    }

    public void setSeasonTalent(SeasonTalent seasonTalent) {
        this.seasonTalent = seasonTalent;
    }
}
