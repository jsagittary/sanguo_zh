package com.gryphpoem.game.zw.resource.domain.s;

import java.util.Map;

/**
 * @ClassName StaticCabinetLv.java
 * @Description 内阁天策府点兵升级配置表
 * @author QiuKun
 * @date 2017年7月15日
 */
public class StaticCabinetLv {
    private int id;
    private int lv;// 等级
    private long needExp;// 需要升级的经验
    private Map<Integer, Integer> lineLv;// 点兵激活等级[[id,lv],[id,lv]...]
    private int femaleHero;// 女将开启等级

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public long getNeedExp() {
        return needExp;
    }

    public void setNeedExp(long needExp) {
        this.needExp = needExp;
    }

    public Map<Integer, Integer> getLineLv() {
        return lineLv;
    }

    public void setLineLv(Map<Integer, Integer> lineLv) {
        this.lineLv = lineLv;
    }

    public int getFemaleHero() {
        return femaleHero;
    }

    public void setFemaleHero(int femaleHero) {
        this.femaleHero = femaleHero;
    }

}
