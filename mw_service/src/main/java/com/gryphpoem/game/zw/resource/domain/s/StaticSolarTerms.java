package com.gryphpoem.game.zw.resource.domain.s;

/**
 * @ClassName StaticSolarTerms.java
 * @Description 节气配置
 * @author QiuKun
 * @date 2017年11月21日
 */
public class StaticSolarTerms {
    private int id;
    private int week;// 星期几
    private int type;// 1 油, 2 电, 3 补给, 4 矿石, 5 募兵
    private int levyBonus;// 征收加成 万分比
    private int recruitBonus;// 募兵加成
    private int collectBonus;// 采集加成

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLevyBonus() {
        return levyBonus;
    }

    public void setLevyBonus(int levyBonus) {
        this.levyBonus = levyBonus;
    }

    public int getCollectBonus() {
        return collectBonus;
    }

    public void setCollectBonus(int collectBonus) {
        this.collectBonus = collectBonus;
    }

    public int getRecruitBonus() {
        return recruitBonus;
    }

    public void setRecruitBonus(int recruitBonus) {
        this.recruitBonus = recruitBonus;
    }

    @Override
    public String toString() {
        return "StaticSolarTerms [id=" + id + ", week=" + week + ", type=" + type + ", levyBonus=" + levyBonus
                + ", recruitBonus=" + recruitBonus + ", collectBonus=" + collectBonus + "]";
    }

}
