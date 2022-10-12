package com.gryphpoem.game.zw.resource.pojo.hero;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.HeroConstant;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @Author: GeYuanpeng
 * @Date: 2022/10/12 18:05
 */
public class TalentData {


    /**
     * 天赋页激活状态
     */
    private int status;

    /**
     * 天赋页中的天赋及其等级
     */
    private Map<Integer, Integer> talentArr; // key --> 天赋具体位置，s_hero_evolve的part；value --> 天赋等级，s_hero_evolve的lv

    /**
     * 天赋页索引
     */
    private int index;

    /**
     * 天赋部位最大索引：紫将-4；橙将-6
     */
    private int maxPart;

    /**
     * 天赋页天赋是否已全部升满
     */
    private int allPartActivated;

    public TalentData(CommonPb.TalentData talentData) {
        this();
        this.status = talentData.getStatus();
        for (CommonPb.TwoInt twoInt : talentData.getTalentArrList()) {
            this.talentArr.put(twoInt.getV1(), twoInt.getV2());
        }
        this.index = talentData.getIndex();
    }

    public TalentData() {
    }

    public TalentData(int index) {
        this.index = index;
    }

    /**
     * 根据天赋页激活状态、天赋页索引、天赋部位最大索引，初始化天赋页数据
     *
     * @param status
     * @param index
     * @param maxPart
     */
    public TalentData(int status, int index, int maxPart) {
        this.status = status;
        this.index = index;
        this.maxPart = maxPart;
        talentArr = new HashMap<>(maxPart);
        for (int i = 1; i <= maxPart; i++) {
            talentArr.put(i, 0);
        }
    }

    public Map<Integer, Integer> getTalentArr() {
        return talentArr;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * 是否激活了
     *
     * @return
     */
    public boolean isActivate() {
        return this.status == 1;
    }

    /**
     * 未升级点亮的天赋球
     *
     * @return 当前部位
     */
    public int curPart() {
        return Stream.iterate(HeroConstant.AWAKEN_PART_MIN, i -> ++i).limit(HeroConstant.AWAKEN_PART_MAX).filter(part -> talentArr.getOrDefault(part, 0) <= 0).sorted().findFirst().orElse(0);
    }

    /**
     * 已升级点亮的天赋球
     *
     * @return 下一个进化
     */
    public int lastPart() {
        return Stream.iterate(HeroConstant.AWAKEN_PART_MIN, i -> ++i).limit(HeroConstant.AWAKEN_PART_MAX).filter(part -> talentArr.getOrDefault(part, 0) > 1).max(Comparator.comparingInt(Integer::intValue)).orElse(0);
    }

    /**
     * 将基因重置
     */
    public void clearEvolution() {
        Stream.iterate(HeroConstant.AWAKEN_PART_MIN, part -> ++part).limit(HeroConstant.AWAKEN_PART_MAX).forEach(part -> talentArr.put(part, 0));
    }


    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public int getAllPartActivated() {
        return allPartActivated;
    }

    public void setTalentArr(Map<Integer, Integer> talentArr) {
        this.talentArr = talentArr;
    }

    public int getMaxPart() {
        return maxPart;
    }

    public void setMaxPart(int maxPart) {
        this.maxPart = maxPart;
    }

    public void setAllPartActivated(int allPartActivated) {
        this.allPartActivated = allPartActivated;
    }

    public boolean isAllPartActivated() {
        return allPartActivated == 1;
    }

    /**
     * 升级天赋
     * @param part
     */
    public void upgradeTalent(int part) {
        this.talentArr.merge(part, 1, Integer::sum);
    }
    
}
