package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.List;
import java.util.Map;

/**
 * @author: ZhouJie
 * @date: Create in 2018-10-13 16:42
 * @description: 战机合成配置
 * @modified By:
 */
public class StaticPlaneInit {

    private int planeType;                      // 战机Type
    private int planeId;                        // 战机Id
    private List<Integer> synthesis;            // 合成需求物品
    private List<Integer> decompose;            // 分解所需物品
    private Map<Integer, Integer> attr;         // 基础属性: [[属性类型1，属性值1],[属性类型2，属性值2]...]
    private int skillType;                      // 技能类型: s_mentor_skill中的type
    private int quality;                        // 初始品质 1绿 2蓝 3紫 4橙 5红

    /**
     * 根据属性Id获取战机的基础属性
     * @param attrId
     * @return
     */
    public int getBaseAttrById(int attrId) {
        Integer val = getAttr().get(attrId);
        return CheckNull.isNull(val) ? 0 : val.intValue();
    }

    public int getPlaneType() {
        return planeType;
    }

    public void setPlaneType(int planeType) {
        this.planeType = planeType;
    }

    public List<Integer> getSynthesis() {
        return synthesis;
    }

    public void setSynthesis(List<Integer> synthesis) {
        this.synthesis = synthesis;
    }

    public List<Integer> getDecompose() {
        return decompose;
    }

    public void setDecompose(List<Integer> decompose) {
        this.decompose = decompose;
    }

    public Map<Integer, Integer> getAttr() {
        return attr;
    }

    public void setAttr(Map<Integer, Integer> attr) {
        this.attr = attr;
    }

    public int getPlaneId() {
        return planeId;
    }

    public void setPlaneId(int planeId) {
        this.planeId = planeId;
    }

    public int getSkillType() {
        return skillType;
    }

    public void setSkillType(int skillType) {
        this.skillType = skillType;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }
}
