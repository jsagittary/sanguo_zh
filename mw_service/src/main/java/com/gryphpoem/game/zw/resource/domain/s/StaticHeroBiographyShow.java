package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-08-12 16:50
 */
public class StaticHeroBiographyShow {
    /** s_hero_biography_attr 表中的type字段*/
    private int id;
    private List<Integer> heroId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Integer> getHeroId() {
        return heroId;
    }

    public void setHeroId(List<Integer> heroId) {
        this.heroId = heroId;
    }
}
