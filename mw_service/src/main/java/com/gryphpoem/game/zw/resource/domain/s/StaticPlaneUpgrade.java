package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.List;
import java.util.Map;

/**
 * @author: ZhouJie
 * @date: Create in 2018-10-12 10:33
 * @description: 战机升级改造的配置
 * @modified By:
 */
public class StaticPlaneUpgrade {

    private int planeId;                    // 战机Id
    private int planeType;                  // 战机类型
    private int quality;                    // 品质
    private int qualityLevel;               // 品质等级
    private int nextId;                     // 进阶后战机ID
    private int skillId;                    // 技能ID
    private List<List<Integer>> reformNeed; // 改造需要的材料
    private Map<Integer, Integer> aptitude; // 成长资质

    /**
     * 根据属性id获取将领的资质影响系数（万分比）
     * @param attrId
     * @return 资质
     */
    public int getBaseRatioById(int attrId) {
        Integer val = getAptitude().get(attrId);
        return CheckNull.isNull(val) ? 0 : val.intValue();
    }

    public int getPlaneId() {
        return planeId;
    }

    public void setPlaneId(int planeId) {
        this.planeId = planeId;
    }

    public int getPlaneType() {
        return planeType;
    }

    public void setPlaneType(int planeType) {
        this.planeType = planeType;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getQualityLevel() {
        return qualityLevel;
    }

    public void setQualityLevel(int qualityLevel) {
        this.qualityLevel = qualityLevel;
    }

    public List<List<Integer>> getReformNeed() {
        return reformNeed;
    }

    public void setReformNeed(List<List<Integer>> reformNeed) {
        this.reformNeed = reformNeed;
    }

    public int getNextId() {
        return nextId;
    }

    public void setNextId(int nextId) {
        this.nextId = nextId;
    }

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

    public Map<Integer, Integer> getAptitude() {
        return aptitude;
    }

    public void setAptitude(Map<Integer, Integer> aptitude) {
        this.aptitude = aptitude;
    }
}
