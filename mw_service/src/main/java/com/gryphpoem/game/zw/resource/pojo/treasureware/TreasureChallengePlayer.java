package com.gryphpoem.game.zw.resource.pojo.treasureware;


import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.ArrayList;
import java.util.List;

import static com.gryphpoem.game.zw.resource.constant.TreasureChallengePlayerConstant.*;

/**
 * @Description: 宝具挑战玩家数据
 * @Author: DuanShQ
 * @CreateTime: 2022-06-13
 */
public class TreasureChallengePlayer {
    /** 挑战的玩家id */
    private long challengePlayerId;
    /** 是否已战胜 */
    private boolean win;

    /** 今日已挑战次数 */
    private int challengeNum;
    /** 对当前玩家的已挑战次数 */
    private int challengeForPlayerNum;
    /** 今日已购买次数 */
    private int purchaseNum;
    /** 冷却开始时间（单位 秒） */
    private int CDStartTime;

    /** 未获胜刷新次数, 用于触发保底 */
    private int failRefreshNum;

    /** 今日已刷新出来的玩家, 用于控制一天内避免刷新到重复对手 */
    private List<Long> refreshedRecord = new ArrayList<>();

    /**
     * 序列化
     */
    public CommonPb.TreasureChallengePlayerPb ser() {
        CommonPb.TreasureChallengePlayerPb.Builder builder = CommonPb.TreasureChallengePlayerPb.newBuilder();
        builder.setChallengePlayerId(challengePlayerId);
        builder.setChallengeNum(challengeNum);
        builder.setChallengeForPlayerNum(challengeForPlayerNum);
        builder.setPurchaseNum(purchaseNum);
        builder.setCDStartTime(CDStartTime);
        builder.setWin(win);
        builder.setFailRefreshNum(failRefreshNum);
        return builder.build();
    }

    /**
     * 反序列化
     */
    public void dSer(CommonPb.TreasureChallengePlayerPb ser) {
        challengePlayerId = ser.getChallengePlayerId();
        challengeNum = ser.getChallengeNum();
        challengeForPlayerNum = ser.getChallengeForPlayerNum();
        purchaseNum = ser.getPurchaseNum();
        CDStartTime = ser.getCDStartTime();
        win = ser.getWin();
        failRefreshNum = ser.getFailRefreshNum();
    }

    /**
     * 获取剩余挑战次数
     */
    public int getRemaining() {
        return FREE_CHALLENGE_NUM + purchaseNum - challengeNum;
    }

    /**
     * 获取对当前挑战玩家的剩余可挑战次数
     */
    public int getRemainingForPlayer() {
        return win ? 0 : CHALLENGE_FOR_PLAYER_NUM - challengeForPlayerNum;
    }

    /**
     * 获取剩余刷新时间
     */
    public int getRemainingRefreshTime() {
        return Math.max(CDStartTime + CHALLENGE_REFRESH_TIME - TimeHelper.getCurrentSecond(), 0);
    }

    /**
     * 是否需要刷新挑战的玩家
     */
    public boolean isNeedRefreshChallengePlayer() {
        return win || getRemainingForPlayer() <= 0;
    }

    /**
     * 增加1次今日已挑战次数
     */
    public void incChallengeNum() {
        challengeNum++;
    }

    /**
     * 增加1次对当前挑战玩家的挑战次数
     */
    public void incChallengeForPlayerNum() {
        challengeForPlayerNum++;
    }

    /**
     * 增加1次已购买的次数
     */
    public void incPurchaseNum() {
        purchaseNum++;
    }

    public void recordRefreshed(long roleId) {
        // 前 X 次刷新不会出现重复的挑战对象
        if (refreshedRecord.size() >= NOT_REPETITION_NUM) {
            return;
        }
        refreshedRecord.add(roleId);
    }

    //region ======================================== Getter and Setter ========================================

    public long getChallengePlayerId() {
        return challengePlayerId;
    }

    public void setChallengePlayerId(long challengePlayerId) {
        this.challengePlayerId = challengePlayerId;
    }

    public boolean isWin() {
        return win;
    }

    public void setWin(boolean win) {
        this.win = win;
    }

    public int getChallengeNum() {
        return challengeNum;
    }

    public void setChallengeNum(int challengeNum) {
        this.challengeNum = challengeNum;
    }

    public int getChallengeForPlayerNum() {
        return challengeForPlayerNum;
    }

    public void setChallengeForPlayerNum(int challengeForPlayerNum) {
        this.challengeForPlayerNum = challengeForPlayerNum;
    }

    public int getPurchaseNum() {
        return purchaseNum;
    }

    public void setPurchaseNum(int purchaseNum) {
        this.purchaseNum = purchaseNum;
    }

    public int getCDStartTime() {
        return CDStartTime;
    }

    public void setCDStartTime(int CDStartTime) {
        this.CDStartTime = CDStartTime;
    }

    public int getFailRefreshNum() {
        return failRefreshNum;
    }

    public void setFailRefreshNum(int failRefreshNum) {
        this.failRefreshNum = failRefreshNum;
    }

    public List<Long> getRefreshedRecord() {
        return refreshedRecord;
    }

    //endregion =================================== Getter and Setter end ========================================
}