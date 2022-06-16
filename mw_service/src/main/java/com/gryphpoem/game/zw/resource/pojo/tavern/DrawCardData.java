package com.gryphpoem.game.zw.resource.pojo.tavern;

import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.constant.HeroConstant;
import com.gryphpoem.game.zw.resource.pojo.GamePb;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.Turple;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-13 14:55
 */
public class DrawCardData implements GamePb<SerializePb.SerDrawCardData> {
    /**
     * 第一次抽取是否完成
     */
    private boolean firstFinish;
    /**
     * 免费次数 (只记录系统免费次数)
     */
    private int freeCount;
    /**
     * 免费抽卡cd时间
     */
    private long cdFreeTime;
    /**
     * 免费次数 (活动或任务免费次数)
     */
    private int otherFreeCount;
    /**
     * 心愿英雄 <英雄id, 次数>
     */
    private Turple<Integer, Integer> wishHero = new Turple<>(0, 0);
    /**
     * 每日使用折扣寻访时间
     */
    private Date firstCostMoneyDailyDate;
    /**
     * 已经领取的次数下标
     */
    private List<Integer> specifyRewardList = new ArrayList<>();
    /**
     * 已使用的活动抽取次数
     */
    private int activeDrawsUsedCount;
    /**
     * 抽取英雄循环次数
     */
    private int heroDrawCount;
    /**
     * 抽取英雄碎片循环次数
     */
    private int fragmentDrawCount;
    /**
     * 今日抽取次数
     */
    private int todayDrawCount;
    /**
     * 上次抽卡时间戳
     */
    private Date lastDrawCardDate;
    /**
     * 存储武将碎片信息
     */
    private Map<Integer, Integer> fragmentData = new ConcurrentHashMap<>();

    public boolean isFirstFinish() {
        return firstFinish;
    }

    public void setFirstFinish(boolean firstFinish) {
        this.firstFinish = firstFinish;
    }

    public int getFreeCount() {
        return freeCount;
    }

    public void setFreeCount(int freeCount) {
        this.freeCount = freeCount;
    }

    /**
     * 扣除免费次数
     *
     * @param count
     */
    public void subFreeCount(int count) {
        if (freeCount >= count) {
            freeCount -= count;
            return;
        }

        count -= freeCount;
        freeCount = 0;
        otherFreeCount -= count;
        // 增加活动抽取次数
        this.activeDrawsUsedCount += count;
    }

    public Turple<Integer, Integer> getWishHero() {
        return wishHero;
    }

    public void setWishHero(Turple<Integer, Integer> wishHero) {
        this.wishHero = wishHero;
    }

    public long getCdFreeTime() {
        return cdFreeTime;
    }

    public void setCdFreeTime(long cdFreeTime) {
        this.cdFreeTime = cdFreeTime;
    }

    public int getOtherFreeCount() {
        return otherFreeCount;
    }

    public void setOtherFreeCount(int otherFreeCount) {
        this.otherFreeCount = otherFreeCount;
    }

    public Date getFirstCostMoneyDailyDate() {
        return firstCostMoneyDailyDate;
    }

    public void setFirstCostMoneyDailyDate(Date firstCostMoneyDailyDate) {
        this.firstCostMoneyDailyDate = firstCostMoneyDailyDate;
    }

    public int getActiveDrawsUsedCount() {
        return activeDrawsUsedCount;
    }

    public void setActiveDrawsUsedCount(int activeDrawsUsedCount) {
        this.activeDrawsUsedCount = activeDrawsUsedCount;
    }

    public int getTodayDrawCount() {
        return todayDrawCount;
    }

    public void setTodayDrawCount(int todayDrawCount) {
        this.todayDrawCount = todayDrawCount;
    }

    public int getHeroDrawCount() {
        return heroDrawCount;
    }

    public void setHeroDrawCount(int heroDrawCount) {
        this.heroDrawCount = heroDrawCount;
    }

    public int getFragmentDrawCount() {
        return fragmentDrawCount;
    }

    public void setFragmentDrawCount(int fragmentDrawCount) {
        this.fragmentDrawCount = fragmentDrawCount;
    }

    public List<Integer> getSpecifyRewardList() {
        return specifyRewardList;
    }

    public void setSpecifyRewardList(List<Integer> specifyRewardList) {
        this.specifyRewardList = specifyRewardList;
    }

    public boolean isTodayFirst(Date now) {
        if (this.firstCostMoneyDailyDate == null)
            return false;
        return DateHelper.isSameDate(now, this.firstCostMoneyDailyDate);
    }

    public void addHeroDrawCount() {
        this.heroDrawCount++;
    }

    public void addFragmentDrawCount() {
        this.fragmentDrawCount++;
    }

    /**
     * 记录抽取次数
     *
     * @param now
     */
    public void addDrawCount(Date now) {
        boolean add = false;
        if (CheckNull.isNull(this.lastDrawCardDate) || !DateHelper.isSameDate(now, lastDrawCardDate)) {
            this.todayDrawCount = 1;
            this.lastDrawCardDate = now;
            add = true;
        } else {
            if (this.todayDrawCount < HeroConstant.DAILY_DRAW_CARD_CAN_INCREASE_WISH_POINTS) {
                this.todayDrawCount++;
                add = true;
            }
        }

        if (add) {
            Integer count = this.wishHero.getB();
            if (count < HeroConstant.DRAW_CARD_WISH_VALUE_LIMIT)
                this.wishHero.setB(++count);
        }
    }

    /**
     * 获取下次必出奖励
     *
     * @return
     */
    public List<Integer> getNextRewardList() {
        List<Integer> nextRewardList = HeroConstant.ACTIVE_DRAWS_USED_COUNT_HERO_REWARD.stream().
                filter(list -> CheckNull.nonEmpty(list) && list.get(0) == this.activeDrawsUsedCount).findFirst().orElse(null);
        if (CheckNull.isEmpty(nextRewardList))
            return null;

        if (this.specifyRewardList.contains(nextRewardList.get(0)))
            return null;
        return nextRewardList;
    }

    public Date getLastDrawCardDate() {
        return lastDrawCardDate;
    }

    public void setLastDrawCardDate(Date lastDrawCardDate) {
        this.lastDrawCardDate = lastDrawCardDate;
    }

    public Map<Integer, Integer> getFragmentData() {
        return fragmentData;
    }

    public void setFragmentData(Map<Integer, Integer> fragmentData) {
        this.fragmentData = fragmentData;
    }

    /**
     * 刷新免费次数
     */
    public void refreshData() {
        // 只记录系统免费次数，因此大于0不再增加次数
        if (freeCount == 0) {
            if (System.currentTimeMillis() >= cdFreeTime) {
                freeCount++;
                cdFreeTime = System.currentTimeMillis() + HeroConstant.DRAW_HERO_CARD_FREE_TIMES_TIME_INTERVAL * 1000l;
            }
        }
    }

    /**
     * 反序列化信息
     *
     * @param pb
     */
    public void deSer(SerializePb.SerDrawCardData pb) {
        this.firstFinish = pb.getFirst();
        this.freeCount = pb.getFreeCount();
        this.cdFreeTime = pb.getCdFreeTime();
        this.otherFreeCount = pb.getOtherFreeCount();
        this.wishHero.setA(pb.getWishHero().getV1());
        this.wishHero.setB(pb.getWishHero().getV2());

        Calendar calendar = Calendar.getInstance();
        if (pb.hasFirstCostMoneyDailyDate()) {
            calendar.setTimeInMillis(pb.getFirstCostMoneyDailyDate());
            this.firstCostMoneyDailyDate = calendar.getTime();
        }
        if (CheckNull.nonEmpty(pb.getSpecifyRewardListList())) {
            this.specifyRewardList.addAll(pb.getSpecifyRewardListList());
        }
        this.activeDrawsUsedCount = pb.getActiveDrawsUsedCount();
        this.heroDrawCount = pb.getHeroDrawCount();
        this.fragmentDrawCount = pb.getFragmentDrawCount();
        this.todayDrawCount = pb.getTodayDrawCount();
        if (pb.hasLastDrawCardDate()) {
            calendar.setTimeInMillis(pb.getLastDrawCardDate());
            this.lastDrawCardDate = calendar.getTime();
        }
        if (CheckNull.nonEmpty(pb.getFragmentDataList())) {
            pb.getFragmentDataList().forEach(data -> fragmentData.put(data.getV1(), data.getV2()));
        }
    }

    @Override
    public SerializePb.SerDrawCardData createPb(boolean isSaveDb) {
        SerializePb.SerDrawCardData.Builder builder = SerializePb.SerDrawCardData.newBuilder();
        builder.setFirst(this.firstFinish);
        builder.setFreeCount(freeCount);
        builder.setCdFreeTime(cdFreeTime);
        builder.setOtherFreeCount(otherFreeCount);
        builder.setWishHero(PbHelper.createTwoIntPb(this.wishHero.getA(), this.wishHero.getB()));
        if (Objects.nonNull(firstCostMoneyDailyDate))
            builder.setFirstCostMoneyDailyDate(firstCostMoneyDailyDate.getTime());
        if (CheckNull.nonEmpty(specifyRewardList)) {
            builder.addAllSpecifyRewardList(this.specifyRewardList);
        }
        builder.setActiveDrawsUsedCount(activeDrawsUsedCount);
        builder.setHeroDrawCount(heroDrawCount);
        builder.setFragmentDrawCount(fragmentDrawCount);
        builder.setTodayDrawCount(todayDrawCount);
        if (Objects.nonNull(lastDrawCardDate)) {
            builder.setLastDrawCardDate(lastDrawCardDate.getTime());
        }
        if (CheckNull.nonEmpty(this.fragmentData)) {
            this.fragmentData.entrySet().forEach(entry -> builder.addFragmentData(PbHelper.createTwoIntPb(entry.getKey(), entry.getValue())));
        }
        return builder.build();
    }
}
