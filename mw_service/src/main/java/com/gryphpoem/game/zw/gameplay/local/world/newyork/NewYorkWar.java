package com.gryphpoem.game.zw.gameplay.local.world.newyork;

import com.gryphpoem.game.zw.resource.pojo.IntegralRank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pengshuo on 2019/5/10 17:15
 * <br>Description:
 * <br>Modified By:
 * <br>Version:
 *
 * @author pengshuo
 */
public class NewYorkWar {
    /** 阵营杀敌数 */
    private Map<Integer, IntegralRank> campIntegral = new HashMap<>();
    /** 玩家杀敌数 */
    private Map<Long, NewYorkPlayerIntegralRank> playersIntegral = new HashMap<>();
    /** 纽约争霸状态 */
    private int status;
    /** 最终占领纽约的阵营 */
    private int finalOccupyCamp;
    /** 预显示时间 （秒）*/
    private int preViewDate;
    /** 开启时间 （秒）*/
    private int beginDate;
    /** 结束时间（秒） */
    private int endDate;
    /** 当前轮开始时间 */
    private List<Integer> beginRoundDate = new ArrayList<>();
    /** 当前轮结束时间 */
    private List<Integer> endRoundDate = new ArrayList<>();
    /** 总计轮数 */
    private int totalRound;
    /** 当前轮数 */
    private int currentRound;

    public int getFinalOccupyCamp() {
        return finalOccupyCamp;
    }

    public void setFinalOccupyCamp(int finalOccupyCamp) {
        this.finalOccupyCamp = finalOccupyCamp;
    }

    public Map<Integer, IntegralRank> getCampIntegral() {
        return campIntegral;
    }

    public void setCampIntegral(Map<Integer, IntegralRank> campIntegral) {
        this.campIntegral = campIntegral;
    }

    public Map<Long, NewYorkPlayerIntegralRank> getPlayersIntegral() {
        return playersIntegral;
    }

    public void setPlayersIntegral(Map<Long, NewYorkPlayerIntegralRank> playersIntegral) {
        this.playersIntegral = playersIntegral;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getPreViewDate() {
        return preViewDate;
    }

    public void setPreViewDate(int preViewDate) {
        this.preViewDate = preViewDate;
    }

    public int getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(int beginDate) {
        this.beginDate = beginDate;
    }

    public int getEndDate() {
        return endDate;
    }

    public void setEndDate(int endDate) {
        this.endDate = endDate;
    }

    public List<Integer> getBeginRoundDate() {
        return beginRoundDate;
    }

    public void setBeginRoundDate(List<Integer> beginRoundDate) {
        this.beginRoundDate = beginRoundDate;
    }

    public List<Integer> getEndRoundDate() {
        return endRoundDate;
    }

    public void setEndRoundDate(List<Integer> endRoundDate) {
        this.endRoundDate = endRoundDate;
    }

    public int getTotalRound() {
        return totalRound;
    }

    public void setTotalRound(int totalRound) {
        this.totalRound = totalRound;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }
}
