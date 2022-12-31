package com.gryphpoem.game.zw.resource.pojo.world;

import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.domain.s.StaticHero;
import com.gryphpoem.game.zw.resource.domain.s.StaticSuperMine;
import com.gryphpoem.game.zw.resource.pojo.army.Army;

/**
 * @author QiuKun
 * @ClassName SuperGuard.java
 * @Description 超级矿点驻军
 * @date 2018年7月18日
 */
public class SuperGuard {
    private int armyArriveTime;// 部队达到的时刻
    private Army army; // 部队信息
    private int startTime;// 开始的采集的时间
    private final int maxCollectTime;// 最大采集时间配置的数据
    private int collectTime;// 已采集时间 用于状态切换时使用
    private int canMaxCollectTime;// 当前情况下可采集的最大时间
    private SuperMine superMine;// 所在的矿点

    private SuperGuard(Army army, int maxCollectTime) {
        this.army = army;
        this.maxCollectTime = maxCollectTime;
    }

    public SuperGuard(CommonPb.SuperGuard ser, SuperMine superMine) {
        this.armyArriveTime = ser.getArmyArriveTime();
        this.startTime = ser.getStartTime();
        this.maxCollectTime = ser.getMaxCollectTime();
        this.collectTime = ser.getCollectTime();
        this.canMaxCollectTime = ser.getCanMaxCollectTime();
        this.army = new Army(ser.getArmy());
        this.superMine = superMine;
    }

    /**
     * 创建一个新的超级矿点驻军
     *
     * @param army
     * @param superMine
     * @param now
     * @return
     */
    public static SuperGuard createSuperGuard(Army army, SuperMine superMine, int now) {
        // 获取可采集最大时间
        StaticHero sHero = StaticHeroDataMgr.getHeroMap().get(army.getHero().get(0).getPrincipleHeroId());
        StaticSuperMine sSm = StaticWorldDataMgr.getSuperMineById(superMine.getConfigId());
        int maxCollectTime = sSm.getCollectTime().get(sHero.getQuality());
        SuperGuard sg = new SuperGuard(army, maxCollectTime);
        sg.startTime = now;
        sg.superMine = superMine;
        sg.armyArriveTime = now;
        return sg;
    }

    /**
     * 停产状态
     *
     * @param now
     */
    public void stopState(int now) {
        int cTime = now - startTime;
        this.collectTime += cTime;// 已采集时间
        army.setEndTime(-1);// 部队时间设置为-1标志
    }

    /**
     * 停产 恢复到 生产
     *
     * @param now
     */
    public void reProducedState(int now) {
        this.startTime = now; // 重新开始采集
    }

    /**
     * 计算获取已经采集时间
     *
     * @return
     */
    public int calcCollectedTime(int now) {
        int time = superMine.getState() == SuperMine.STATE_PRODUCED ? now - startTime : 0;// 只有生产状态才能使用 now-startTime
        int collectedTime = time + collectTime;
        return Math.min(maxCollectTime, collectedTime);
    }

    /**
     * 计算已采集到的资源
     *
     * @param now
     * @return
     */
    public int calcCollectedResCnt(int now) {
        StaticSuperMine sSm = StaticWorldDataMgr.getSuperMineById(superMine.getConfigId());
        return (int) Math.floor((calcCollectedTime(now) * 1.0 / Constant.HOUR) * sSm.getSpeed());
    }

    /**
     * 资源充足情况下还可以采集多长时间
     *
     * @param now
     * @return
     */
    public int furtherCollectTime(int now) {
        return maxCollectTime - calcCollectedTime(now);
    }

    /**
     * 设置部队时间
     *
     * @param now
     * @param durationTime
     */
    public void setArmyTime(int now, int durationTime) {
        if (superMine.getState() == SuperMine.STATE_PRODUCED) {
            army.setDuration(durationTime + calcCollectedTime(now));
            army.setEndTime(now + durationTime);
        }
    }

    /**
     * 当矿点足够时并且是生产状态的采集时间计算
     *
     * @param now
     */
    public void setArmyTimeInEnoughRes(int now) {
        if (superMine.getState() == SuperMine.STATE_PRODUCED) {
            int endTime = maxCollectTime - calcCollectedTime(now);
            army.setDuration(maxCollectTime);
            army.setEndTime(now + endTime);
        }
    }

    /**
     * 矿点充足情况设置最大可采集量
     */
    public void setCanMaxCollectTimeEnoughRes() {
        this.canMaxCollectTime = maxCollectTime;
    }

    /**
     * 矿点资源不足情况设置最大可采集量
     *
     * @param now
     * @param durationTime
     */
    public void setCanMaxCollectTime(int now, int durationTime) {
        this.canMaxCollectTime = durationTime + calcCollectedTime(now);
    }

    /**
     * 是否是相同的army
     *
     * @param am
     * @return
     */
    public boolean isSameArmy(Army am) {
        return am.getLordId() == this.army.getLordId() && am.getKeyId() == this.army.getKeyId();
    }

    public Army getArmy() {
        return army;
    }

    public void setArmy(Army army) {
        this.army = army;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public SuperMine getSuperMine() {
        return superMine;
    }

    public int getMaxCollectTime() {
        return maxCollectTime;
    }

    public int getCollectTime() {
        return collectTime;
    }

    public void setCollectTime(int collectTime) {
        this.collectTime = collectTime;
    }

    public void setSuperMine(SuperMine superMine) {
        this.superMine = superMine;
    }

    public int getCanMaxCollectTime() {
        return canMaxCollectTime;
    }

    public void setCanMaxCollectTime(int canMaxCollectTime) {
        this.canMaxCollectTime = canMaxCollectTime;
    }

    public int getArmyArriveTime() {
        return armyArriveTime;
    }

    public void setArmyArriveTime(int armyArriveTime) {
        this.armyArriveTime = armyArriveTime;
    }

    @Override
    public String toString() {
        return "SuperGuard [army=" + army + ", startTime=" + startTime + ", maxCollectTime=" + maxCollectTime
                + ", collectTime=" + collectTime + ", canMaxCollectTime=" + canMaxCollectTime + "]";
    }

}
