package com.gryphpoem.game.zw.resource.pojo;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pengshuo on 2019/4/2 11:42
 * <br>Description: 玩家 世界争霸 数据存储
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class PlayerWorldWarData {
    /** 每日杀敌数 */
    private int dailyAttack;
    /** 每日奖励领取记录 */
    private Map<Integer,Integer> dailyAttackAward = new HashMap<>();
    /** 限定任务完成情况 */
    private Map<Integer,Integer> restrictTask = new HashMap<>();
    /** 限定任务奖励领取记录 */
    private Map<Integer,Integer> restrictTaskAward = new HashMap<>();
    /** 周积分 */
    private int weekIntegral;
    /** 周奖励领取记录 */
    private Map<Integer,Integer> weekAward = new HashMap<>();
    /** 赛季积分 */
    private int seasonIntegral;
    /** 城市征战领取记录 */
    private Map<Integer,Integer> attackCityAward = new HashMap<>();
    /** 赛季商店物品兑换记录 */
    private Map<Integer,Integer> seasonShop = new HashMap<>();
    /** 最后获得积分的时间 */
    private int integralSecond;

    public int getDailyAttack() {
        return dailyAttack;
    }

    public void setDailyAttack(int dailyAttack) {
        this.dailyAttack = dailyAttack;
    }

    public Map<Integer, Integer> getDailyAttackAward() {
        return dailyAttackAward;
    }

    public void setDailyAttackAward(Map<Integer, Integer> dailyAttackAward) {
        this.dailyAttackAward = dailyAttackAward;
    }

    public Map<Integer, Integer> getRestrictTask() {
        return restrictTask;
    }

    public void setRestrictTask(Map<Integer, Integer> restrictTask) {
        this.restrictTask = restrictTask;
    }

    public Map<Integer, Integer> getRestrictTaskAward() {
        return restrictTaskAward;
    }

    public void setRestrictTaskAward(Map<Integer, Integer> restrictTaskAward) {
        this.restrictTaskAward = restrictTaskAward;
    }

    public int getWeekIntegral() {
        return weekIntegral;
    }

    public void setWeekIntegral(int weekIntegral) {
        this.weekIntegral = weekIntegral;
    }

    public Map<Integer, Integer> getWeekAward() {
        return weekAward;
    }

    public void setWeekAward(Map<Integer, Integer> weekAward) {
        this.weekAward = weekAward;
    }

    public int getSeasonIntegral() {
        return seasonIntegral;
    }

    public void setSeasonIntegral(int seasonIntegral) {
        this.seasonIntegral = seasonIntegral;
    }

    public Map<Integer, Integer> getAttackCityAward() {
        return attackCityAward;
    }

    public void setAttackCityAward(Map<Integer, Integer> attackCityAward) {
        this.attackCityAward = attackCityAward;
    }

    public Map<Integer, Integer> getSeasonShop() {
        return seasonShop;
    }

    public void setSeasonShop(Map<Integer, Integer> seasonShop) {
        this.seasonShop = seasonShop;
    }

    public int getIntegralSecond() {
        return integralSecond;
    }

    public void setIntegralSecond(int integralSecond) {
        this.integralSecond = integralSecond;
    }

    /** 玩家 世界争霸数据 序列化 */
    public SerializePb.SerPlayerWorldWar ser(){
        SerializePb.SerPlayerWorldWar.Builder builder  = SerializePb.SerPlayerWorldWar.newBuilder();
        // 每日杀敌数
        builder.setDailyAttack(dailyAttack);
        // 每日奖励领取记录
        builder.addAllDailyAttackAward(PbHelper.createTwoIntListByMap(dailyAttackAward));
        // 限定任务完成情况
        builder.addAllRestrictTask(PbHelper.createTwoIntListByMap(restrictTask));
        // 限定任务奖励领取记录
        builder.addAllRestrictTaskAward(PbHelper.createTwoIntListByMap(restrictTaskAward));
        // 周积分
        builder.setWeekIntegral(weekIntegral);
        // 周奖励领取记录
        builder.addAllWeekAward(PbHelper.createTwoIntListByMap(weekAward));
        // 赛季积分
        builder.setSeasonIntegral(seasonIntegral);
        // 城市征战领取记录
        builder.addAllAttackCityAward(PbHelper.createTwoIntListByMap(attackCityAward));
        // 赛季商店物品兑换记录
        builder.addAllSeasonShop(PbHelper.createTwoIntListByMap(seasonShop));
        // 积分时间
        builder.setIntegralSecond(integralSecond);
        return builder.build();
    }

    /** 玩家 世界争霸数据 反序列化  */
    public void deser(SerializePb.SerPlayerWorldWar ser){
        this.setDailyAttack(ser.getDailyAttack());
        List<CommonPb.TwoInt> dailyAttackAward = ser.getDailyAttackAwardList();
        if(dailyAttackAward != null){
            dailyAttackAward.forEach(e-> this.dailyAttackAward.put(e.getV1(),e.getV2()));
        }
        List<CommonPb.TwoInt> restrictTask = ser.getRestrictTaskList();
        if(restrictTask != null){
            restrictTask.forEach(e-> this.restrictTask.put(e.getV1(),e.getV2()));
        }
        List<CommonPb.TwoInt> restrictTaskAward = ser.getRestrictTaskAwardList();
        if(restrictTaskAward != null){
            restrictTaskAward.forEach(e-> this.restrictTaskAward.put(e.getV1(),e.getV2()));
        }
        this.setWeekIntegral(ser.getWeekIntegral());
        List<CommonPb.TwoInt> weekAward = ser.getWeekAwardList();
        if(weekAward != null){
            weekAward.forEach(e-> this.weekAward.put(e.getV1(),e.getV2()));
        }
        this.setSeasonIntegral(ser.getSeasonIntegral());
        List<CommonPb.TwoInt> attackCityAward = ser.getAttackCityAwardList();
        if(attackCityAward != null){
            attackCityAward.forEach(e-> this.attackCityAward.put(e.getV1(),e.getV2()));
        }
        List<CommonPb.TwoInt> seasonShop = ser.getSeasonShopList();
        if(seasonShop != null){
            seasonShop.forEach(e-> this.seasonShop.put(e.getV1(),e.getV2()));
        }
        this.setIntegralSecond(ser.getIntegralSecond());
    }

    @Override
    public String toString() {
        return "PlayerWorldWarData{" +
                "dailyAttack=" + dailyAttack +
                ", dailyAttackAward=" + dailyAttackAward +
                ", restrictTask=" + restrictTask +
                ", restrictTaskAward=" + restrictTaskAward +
                ", weekIntegral=" + weekIntegral +
                ", weekAward=" + weekAward +
                ", seasonIntegral=" + seasonIntegral +
                ", attackCityAward=" + attackCityAward +
                ", seasonShop=" + seasonShop +
                ", integralSecond=" + integralSecond +
                '}';
    }
}
