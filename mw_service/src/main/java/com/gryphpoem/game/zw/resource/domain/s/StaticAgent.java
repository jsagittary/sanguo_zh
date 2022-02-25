package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticAgent.java
 * @Description 特工
 * @author QiuKun
 * @date 2018年6月5日
 */
public class StaticAgent {

    private int autoId;
    private int id; // 特工id
    private int quality; //品质
    private List<Integer> unlock;// 解锁条件, 为null代表不需要条件
    private List<Integer> unlock1;//解锁条件，为null代表不需要条件 unlock || unlock1
    private int armyType;// 属性生效的兵种, 4表示全兵种
    private int attributeId;// 属性编号
    private int attributeVal;// 属性区间
    /**
     * 具体值请看{@link com.gryphpoem.game.zw.resource.constant.CiaConstant} </br>
     * 特工技编号 1燃油增产 2电力增产 3补给增产 4矿石增产 5战车募兵加速 6坦克募兵加速 7火箭募兵加速 8建筑加速 9科技加速 </br>
     * 10战车补给消耗降低 11坦克补给消耗降低 12火箭补给消耗降低
     */
    private int skillId;
    private int skillVal;// 技能数值区间 万分比
    private int IntimacyVal;// 升至下级需要的好感度/亲密度
    private int dailyFree;  // 每日免费次数
    private int addIntimacy; // 每次免费次数增加的好感度

    public int getAutoId() {
        return autoId;
    }

    public void setAutoId(int autoId) {
        this.autoId = autoId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getArmyType() {
        return armyType;
    }

    public void setArmyType(int armyType) {
        this.armyType = armyType;
    }

    public int getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(int attributeId) {
        this.attributeId = attributeId;
    }

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

    public int getIntimacyVal() {
        return IntimacyVal;
    }

    public void setIntimacyVal(int intimacyVal) {
        IntimacyVal = intimacyVal;
    }

    public int getAttributeVal() {
        return attributeVal;
    }

    public void setAttributeVal(int attributeVal) {
        this.attributeVal = attributeVal;
    }

    public int getSkillVal() {
        return skillVal;
    }

    public void setSkillVal(int skillVal) {
        this.skillVal = skillVal;
    }

    public int getDailyFree() {
        return dailyFree;
    }

    public void setDailyFree(int dailyFree) {
        this.dailyFree = dailyFree;
    }

    public int getAddIntimacy() {
        return addIntimacy;
    }

    public void setAddIntimacy(int addIntimacy) {
        this.addIntimacy = addIntimacy;
    }

    public List<Integer> getUnlock() {
        return unlock;
    }

    public void setUnlock(List<Integer> unlock) {
        this.unlock = unlock;
    }

    public List<Integer> getUnlock1() {
        return unlock1;
    }

    public void setUnlock1(List<Integer> unlock1) {
        this.unlock1 = unlock1;
    }

    @Override
    public String toString() {
        return "StaticAgent{" +
                "autoId=" + autoId +
                ", id=" + id +
                ", quality=" + quality +
                ", unlock=" + unlock +
                ", armyType=" + armyType +
                ", attributeId=" + attributeId +
                ", attributeVal=" + attributeVal +
                ", skillId=" + skillId +
                ", skillVal=" + skillVal +
                ", IntimacyVal=" + IntimacyVal +
                ", dailyFree=" + dailyFree +
                ", addIntimacy=" + addIntimacy +
                '}';
    }

}
