package com.gryphpoem.game.zw.resource.pojo.hero;

import com.gryphpoem.game.zw.pb.CommonPb;

import java.util.HashMap;
import java.util.Map;

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
    private Map<Integer, Integer> talentArr = new HashMap<>(); // key --> 天赋具体位置，s_hero_evolve的part；value --> 天赋等级，s_hero_evolve的lv

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
        this.allPartActivated = talentData.getAllPartActivated();
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
        this.talentArr = new HashMap<>(maxPart);
        for (int i = 1; i <= maxPart; i++) {
            this.talentArr.put(i, 0);
        }
        this.allPartActivated = 0;
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

    /**
     * 天赋页是否已激活
     *
     * @return
     */
    public boolean isActivate() {
        return this.status == 1;
    }

    /**
     * 当前天赋页的天赋球是否已全部满级
     * @return
     */
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
