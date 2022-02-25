package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.Date;
import java.util.List;

/**
 * 战令活动计划配置
 *
 * @program: server
 * @description:
 * @author: zhou jie
 * @create: 2019-12-02 11:49
 */
public class StaticBattlePassPlan {

    private int keyId;
    /**
     * 开服第几天开启 ：0无， 1 为开服第一天（当天）
     */
    private int openBegin;
    /**
     * 时长: 3表示开3天
     */
    private int openDuration;
    /**
     * 具体开始时间
     */
    private Date beginTime;
    /**
     * 具体结束时间
     */
    private Date endTime;
    /**
     * 服务器id
     */
    private List<List<Integer>> serverId;

    /**
     * 计算后的开启时间
     */
    private Date realBeginDate;

    /**
     * 计算后的结束时间
     */
    private Date realEndDate;

    /**
     * 最大的直接购买等级
     */
    private int buyLevel;
    /**
     * 每日可领取的经验
     */
    private int dailyExp;

    /**
     * 模板id
     */
    private int planId;

    /**
     * 支付id
     */
    private int payId;


    /**
     * 开服时间
     *
     * @param openServerDate 开服时间
     */
    public void initDate(Date openServerDate) {
        // 以开服时间开启活动
        if (openBegin >= 1 && openDuration != 0) {
            // 开启时间 = 开服时间 + (开服第几天 - 1) 的0点0分0秒
            this.realBeginDate = TimeHelper.getSomeDayAfterOrBerfore(openServerDate, openBegin - 1, 0, 0, 0);
            // 结束时间 = 开启时间 + (持续时间 - 1) 的23点59分59秒
            this.realEndDate = TimeHelper.getSomeDayAfterOrBerfore(this.realBeginDate, openDuration - 1, 23, 59, 59);
        } else {
            // 具体的时间开启活动
            this.realBeginDate = this.beginTime;
            this.realEndDate = this.endTime;
        }
    }


    /**
     * 检测开放
     *
     * @param now 现在的时间
     * @return true
     */
    public boolean checkOpen(Date now) {
        return !CheckNull.isNull(realBeginDate) && !CheckNull.isNull(realEndDate) && now.after(realBeginDate) && now.before(realEndDate);
    }

    /**
     * 检测配置
     *
     * @return true 开放时间配置有问题
     */
    public boolean checkConfig() {
        // 如果开服时间设置为NULL, 具体开放时间也设置为NULL, 就报错
        return openBegin == 0 && openDuration == 0 && beginTime == null && endTime == null;
    }

    /**
     * 判断是否该服务器的战令活动
     *
     * @param sid 服务器id
     * @return true 说明是自己的服务器的plan
     */
    public boolean checkServerPlan(int sid) {
        if (!CheckNull.isEmpty(this.serverId)) {
            // 这里配置的conf[0]和[1] 是起始的serverId和结束的serverId
            List<Integer> startEndConf = this.serverId.stream().filter(conf -> conf.get(0) <= conf.get(1) && sid >= conf.get(0) && sid <= conf.get(1)).findFirst().orElse(null);
            return !CheckNull.isEmpty(startEndConf);
        }
        return true;
    }

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getOpenBegin() {
        return openBegin;
    }

    public void setOpenBegin(int openBegin) {
        this.openBegin = openBegin;
    }

    public int getOpenDuration() {
        return openDuration;
    }

    public void setOpenDuration(int openDuration) {
        this.openDuration = openDuration;
    }

    public Date getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(Date beginTime) {
        this.beginTime = beginTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public List<List<Integer>> getServerId() {
        return serverId;
    }

    public void setServerId(List<List<Integer>> serverId) {
        this.serverId = serverId;
    }

    public Date getRealBeginDate() {
        return realBeginDate;
    }

    public Date getRealEndDate() {
        return realEndDate;
    }

    public int getBuyLevel() {
        return buyLevel;
    }

    public void setBuyLevel(int buyLevel) {
        this.buyLevel = buyLevel;
    }

    public int getDailyExp() {
        return dailyExp;
    }

    public void setDailyExp(int dailyExp) {
        this.dailyExp = dailyExp;
    }

    public int getPlanId() {
        return planId;
    }

    public void setPlanId(int planId) {
        this.planId = planId;
    }

    public int getPayId() {
        return payId;
    }

    public void setPayId(int payId) {
        this.payId = payId;
    }
}