package com.gryphpoem.game.zw.resource.domain.s;

/**
 * @author: ZhouJie
 * @date: Create in 2018-10-12 11:29
 * @description: 战机升级经验表
 * @modified By:
 */
public class StaticPlaneLv {

    private int id;             // 自增ID
    private int level;          // 战机等级
    private int quality;        // 战机品质
    private int qualityLevel;   // 战机品质等级
    private int exp;            // 升到下级所需经验

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
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

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
