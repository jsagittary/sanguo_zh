package com.gryphpoem.game.zw.resource.pojo.fish;

import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author xwind
 * @date 2021/8/6
 */
public class FishingData {
    public static final int BAIT_TEAMS = 5;

    private List<BaitTeam> baitTeams;
    private FishRod fishRod;//当前鱼竿
    private int masteries;//熟练度
    private int titleId;//称号id
    private int placeId;//场地id
    private int weather;//渔场天气
    private LinkedList<FishingLog> fishingLogs;
    private Map<Integer,FishAltas> fishAltasMap;
    private Map<Integer,BaitAltas> baitAltasMap;

    private int score;//积分
    private FishShop fishShop;//商店

    private int guide;//引导 0去采集 1去领取采集奖励 2去抛竿 3去收杆
    private int shareTimes;//
    private int stLastDay;//

    private int lastResetDay;//最后重置队列时间

    public FishingData() {
        baitTeams = new ArrayList<>(BAIT_TEAMS);
        Stream.iterate(1,n->n+1).limit(BAIT_TEAMS).forEach(o -> baitTeams.add(new BaitTeam(o)));
        fishRod = new FishRod();
        fishingLogs = new LinkedList<>();
        titleId = 1;
        fishAltasMap = new HashMap<>();
        baitAltasMap = new HashMap<>();
        fishShop = new FishShop();
    }

    public SerializePb.SerFishingData ser(){
        SerializePb.SerFishingData.Builder builder = SerializePb.SerFishingData.newBuilder();
        baitTeams.forEach(o -> builder.addSerBaitTeam(o.ser()));
        builder.setSerFishRod(fishRod.ser());
        builder.setMasteries(masteries);
        builder.setTitleId(titleId);
        builder.setPlaceId(placeId);
        builder.setWeather(weather);
        fishingLogs.forEach(o -> builder.addSerFishingLog(o.ser()));
        fishAltasMap.values().forEach(o -> builder.addSerFishAltas(SerializePb.SerFishAltas.newBuilder().setId(o.getId()).setStamp(o.getStamp()).setSize(o.getSize()).setIsNew(o.isNew()).build()));
        baitAltasMap.values().forEach(o -> builder.addSerBaitAltas(SerializePb.SerBaitAltas.newBuilder().setId(o.getId()).setStamp(o.getStamp()).setIsNew(o.isNew()).build()));
        builder.setScore(score);
        builder.setSerFishShop(fishShop.ser());
        builder.setGuide(guide);
        builder.setShareTimes(shareTimes);
        builder.setStLastDay(stLastDay);
        builder.setLastResetDay(lastResetDay);
        return builder.build();
    }

    public void dser(SerializePb.SerFishingData ser){
        ser.getSerBaitTeamList().forEach(o -> baitTeams.stream().filter(tmp -> tmp.getTeamId()==o.getTeamId()).findFirst().ifPresent(obj -> obj.dser(o)));
        this.fishRod.dser(ser.getSerFishRod());
        this.masteries = ser.getMasteries();
        this.titleId = ser.getTitleId();
        this.placeId = ser.getPlaceId();
        this.weather = ser.getWeather();
        ser.getSerFishingLogList().forEach(o -> this.fishingLogs.addLast(new FishingLog().dser(o)));
        ser.getSerFishAltasList().forEach(o -> this.fishAltasMap.put(o.getId(),new FishAltas(o.getStamp(),o.getId(),o.getSize(),o.getIsNew())));
        ser.getSerBaitAltasList().forEach(o -> this.baitAltasMap.put(o.getId(),new BaitAltas(o.getStamp(),o.getId(),o.getIsNew())));
        this.score = ser.getScore();
        this.fishShop.dser(ser.getSerFishShop());
        this.guide = ser.getGuide();
        this.shareTimes = ser.getShareTimes();
        this.stLastDay = ser.getStLastDay();
        this.lastResetDay = ser.getLastResetDay();
    }

    public List<BaitTeam> getBaitTeams() {
        int currDay = TimeHelper.getCurrentDay();
        if(lastResetDay != currDay){
            baitTeams.forEach(o -> o.reset());
            this.lastResetDay = currDay;
        }
        return baitTeams;
    }

    public void setBaitTeams(List<BaitTeam> baitTeams) {
        this.baitTeams = baitTeams;
    }

    public BaitTeam getBaitTeam(int teamId){
        return getBaitTeams().stream().filter(o -> o.getTeamId()==teamId).findFirst().orElse(null);
    }

    public FishRod getFishRod() {
        return fishRod;
    }

    public void setFishRod(FishRod fishRod) {
        this.fishRod = fishRod;
    }

    public int getMasteries() {
        return masteries;
    }

    public void setMasteries(int masteries) {
        this.masteries = masteries;
    }

    public int getPlaceId() {
        return placeId;
    }

    public void setPlaceId(int placeId) {
        this.placeId = placeId;
    }

    public int getWeather() {
        return weather;
    }

    public void setWeather(int weather) {
        this.weather = weather;
    }

    public List<FishingLog> getFishingLogs() {
        return fishingLogs;
    }

    public void addFishingLog(FishingLog fishingLog){
        if(fishingLogs.size() > 9){
            fishingLogs.removeLast();
        }
        fishingLogs.addFirst(fishingLog);
    }

    public int getTitleId() {
        return titleId;
    }

    public void setTitleId(int titleId) {
        this.titleId = titleId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Map<Integer, FishAltas> getFishAltasMap() {
        return fishAltasMap;
    }

    public Map<Integer, BaitAltas> getBaitAltasMap() {
        return baitAltasMap;
    }

    public FishShop getFishShop() {
        if(fishShop.getLastDay() != TimeHelper.getCurrentDay()){
            fishShop.getLimit().clear();
            fishShop.setLastDay(TimeHelper.getCurrentDay());
        }
        return fishShop;
    }

    public void setFishShop(FishShop fishShop) {
        this.fishShop = fishShop;
    }

    public int getGuide() {
        return guide;
    }

    public void setGuide(int guide) {
        this.guide = guide;
    }

    public int getShareTimes() {
        if(stLastDay != TimeHelper.getCurrentDay()){
            stLastDay = TimeHelper.getCurrentDay();
            this.shareTimes = 0;
        }
        return shareTimes;
    }

    public void setShareTimes(int shareTimes) {
        this.shareTimes = shareTimes;
    }

    public int getStLastDay() {
        return stLastDay;
    }

    public void setStLastDay(int stLastDay) {
        this.stLastDay = stLastDay;
    }

    public int getLastResetDay() {
        return lastResetDay;
    }

    public void setLastResetDay(int lastResetDay) {
        this.lastResetDay = lastResetDay;
    }
}
