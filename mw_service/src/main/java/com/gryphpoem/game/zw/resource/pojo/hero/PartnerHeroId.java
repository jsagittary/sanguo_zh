package com.gryphpoem.game.zw.resource.pojo.hero;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-17 17:03
 */
public class PartnerHeroId {
    /**
     * 若当前武将为主将, 副将列表
     */
    private List<Integer> deputyHeroList;
    /**
     * 若当前武将为副将, 主将信息
     */
    private int principalHero;

    public PartnerHeroId() {
        deputyHeroList = new ArrayList<>();
    }
}
