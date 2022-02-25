package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-06-03 9:31
 */
public class StaticSeasonTalent {
    //唯一ID
    private int id;
    //天赋分类, 1-进攻类, 2-防守类, 3-种田
    private int classifier;
    //天赋类型, 1-普通类型, 2-特殊技能
    private int talentType;
    //天赋ID
    private int talentId;
    //等级
    private int lv;
    //解锁节点
    private List<Integer> needTalent;
    //升级花费天赋石
    private int cost;
    //更换天赋技能消耗
    private int changeCost;
    //天赋效果
    private int effect;
    //天赋效果参数
    private List<List<Integer>> effectParam;
    /**
     * 天赋名称
     */
    private String desc;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClassifier() {
        return classifier;
    }

    public void setClassifier(int classifier) {
        this.classifier = classifier;
    }

    public int getTalentType() {
        return talentType;
    }

    public void setTalentType(int talentType) {
        this.talentType = talentType;
    }

    public int getTalentId() {
        return talentId;
    }

    public void setTalentId(int talentId) {
        this.talentId = talentId;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public List<Integer> getNeedTalent() {
        return needTalent;
    }

    public void setNeedTalent(List<Integer> needTalent) {
        this.needTalent = needTalent;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getChangeCost() {
        return changeCost;
    }

    public void setChangeCost(int changeCost) {
        this.changeCost = changeCost;
    }

    public int getEffect() {
        return effect;
    }

    public void setEffect(int effect) {
        this.effect = effect;
    }

    public List<List<Integer>> getEffectParam() {
        return effectParam;
    }

    public void setEffectParam(List<List<Integer>> effectParam) {
        this.effectParam = effectParam;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
