package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticAgentStar.java
 * @Description 特工星级
 * @date 2020年7月20日
 */
public class StaticAgentStar {

    private int autoId;
    private int agentId; // 特工id
    private List<Integer> unlock;// 解锁条件, 为null代表不需要条件
    private int star;
    private int armyType;// 属性生效的兵种, 4表示全兵种
    private int attributeId;// 属性编号
    private int attributeVal;// 属性区间
    private List<List<Integer>> cost; //升星消耗
    private List<List<Integer>> fixedAward; //约会固定奖励
    private List<List<Integer>> randomAward; //约会随机奖励
    private int randomCount; //约会随机奖励掉落种类数量
    /**
     * 具体值请看{@link com.gryphpoem.game.zw.resource.constant.CiaConstant} </br>
     * 特工技编号 1燃油增产 2电力增产 3补给增产 4矿石增产 5战车募兵加速 6坦克募兵加速 7火箭募兵加速 8建筑加速 9科技加速 </br>
     * 10战车补给消耗降低 11坦克补给消耗降低 12火箭补给消耗降低
     */


    public int getAutoId() {
        return autoId;
    }

    public void setAutoId(int autoId) {
        this.autoId = autoId;
    }

    public int getAgentId() {
        return agentId;
    }

    public void setAgentId(int agentId) {
        this.agentId = agentId;
    }

    public List<Integer> getUnlock() {
        return unlock;
    }

    public void setUnlock(List<Integer> unlock) {
        this.unlock = unlock;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
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

    public int getAttributeVal() {
        return attributeVal;
    }

    public void setAttributeVal(int attributeVal) {
        this.attributeVal = attributeVal;
    }

    public List<List<Integer>> getCost() {
        return cost;
    }

    public void setCost(List<List<Integer>> cost) {
        this.cost = cost;
    }

    public List<List<Integer>> getFixedAward() {
        return fixedAward;
    }

    public void setFixedAward(List<List<Integer>> fixedAward) {
        this.fixedAward = fixedAward;
    }

    public List<List<Integer>> getRandomAward() {
        return randomAward;
    }

    public void setRandomAward(List<List<Integer>> randomAward) {
        this.randomAward = randomAward;
    }

    public int getRandomCount() {
        return randomCount;
    }

    public void setRandomCount(int randomCount) {
        this.randomCount = randomCount;
    }

    @Override
    public String toString() {
        return "StaticAgentStar{" +
                "autoId=" + autoId +
                ", agentId=" + agentId +
                ", unlock=" + unlock +
                ", star=" + star +
                ", armyType=" + armyType +
                ", attributeId=" + attributeId +
                ", attributeVal=" + attributeVal +
                ", cost=" + cost +
                ", fixedAward=" + fixedAward +
                ", randomAward=" + randomAward +
                ", randomCount=" + randomCount +
                '}';
    }
}
