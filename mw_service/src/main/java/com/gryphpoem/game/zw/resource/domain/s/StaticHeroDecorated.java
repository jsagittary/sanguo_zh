package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;
import java.util.Map;

/**
 * @ClassName StaticHeroDecorated.java
 * @Description 将领授勋相关配置
 * @author QiuKun
 * @date 2018年8月13日
 */
public class StaticHeroDecorated {
    private int cnt;// 授勋次数
    private int heroLv;// 需要将领等级
    private int heroQuality;// 需要将领品质
    private List<List<Integer>> needEquip;// 需要的装备id [[equipId,equipId,attrId],[equipId,equipId,attrId]],最后一个值是attrId
    private Map<Integer, Integer> addAttr;// 授勋后加的属性
    private int setsOf;// 授勋后加的属性

    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }

    public int getHeroLv() {
        return heroLv;
    }

    public void setHeroLv(int heroLv) {
        this.heroLv = heroLv;
    }

    public int getHeroQuality() {
        return heroQuality;
    }

    public void setHeroQuality(int heroQuality) {
        this.heroQuality = heroQuality;
    }

    public List<List<Integer>> getNeedEquip() {
        return needEquip;
    }

    public void setNeedEquip(List<List<Integer>> needEquip) {
        this.needEquip = needEquip;
    }

    public Map<Integer, Integer> getAddAttr() {
        return addAttr;
    }

    public void setAddAttr(Map<Integer, Integer> addAttr) {
        this.addAttr = addAttr;
    }

    public int getSetsOf() {
        return setsOf;
    }

    public void setSetsOf(int setsOf) {
        this.setsOf = setsOf;
    }

    /**
     * 查找装备属性
     * 
     * @param equipId
     * @return null 说明没找到
     */
    public Integer findAttrByEquipId(int equipId) {
        if (this.needEquip == null || this.needEquip.isEmpty()) {
            return null;
        }
        for (List<Integer> l : this.needEquip) {
            if (l == null || l.size() < 2) {
                continue;
            }
            int lSize = l.size();
            long count = l.stream().limit(lSize - 1).filter(eqid -> eqid == equipId).count();
            if (count > 0) {
                return l.get(lSize - 1);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "StaticHeroDecorated [cnt=" + cnt + ", heroLv=" + heroLv + ", heroQuality=" + heroQuality
                + ", needEquip=" + needEquip + ", addAttr=" + addAttr + "]";
    }

}
