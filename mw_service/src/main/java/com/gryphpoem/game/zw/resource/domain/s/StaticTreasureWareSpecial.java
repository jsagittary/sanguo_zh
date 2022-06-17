package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

public class StaticTreasureWareSpecial {
    /** 主键id */
    private int id;
    /** 品质 */
    private int quality;
    /** 专属属性id */
    private int specialId;
    /** 阶段等级 */
    private int classLevel;
    /** 强化等级 */
    private int level;
    /** 指定英雄生效 */
    private Integer heroType;
    /** 特殊属性数值 */
    private List<List<Integer>> attrSpecial;
    /** 分解材料 */
    private List<List<Integer>> resolve;
    /** 专属效果类型 */
    private int type;
    private int armyType;//1 2 3 步兵/骑兵/弓兵英雄专属技能，0表示全兵种英雄专属

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

    public int getSpecialId() {
        return specialId;
    }

    public void setSpecialId(int specialId) {
        this.specialId = specialId;
    }

    public int getClassLevel() {
        return classLevel;
    }

    public void setClassLevel(int classLevel) {
        this.classLevel = classLevel;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<List<Integer>> getAttrSpecial() {
        return attrSpecial;
    }

    public void setAttrSpecial(List<List<Integer>> attrSpecial) {
        this.attrSpecial = attrSpecial;
    }

    public List<List<Integer>> getResolve() {
        return resolve;
    }

    public void setResolve(List<List<Integer>> resolve) {
        this.resolve = resolve;
    }

    public Integer getHeroType() {
        return heroType;
    }

    public void setHeroType(Integer heroType) {
        this.heroType = heroType;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getArmyType() {
        return armyType;
    }

    public void setArmyType(int armyType) {
        this.armyType = armyType;
    }
}