package com.gryphpoem.game.zw.model.player;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName HeroModel.java
 * @Description
 * @author QiuKun
 * @date 2019年5月11日
 */
public class HeroModel extends BasePlayerModel {

    // 玩家将领<heroid,hero>
    private Map<Integer, CrossHero> heros = new HashMap<>();

    @Override
    public PlayerModelType getModelType() {
        return PlayerModelType.HERO_MODEL;
    }

    public Map<Integer, CrossHero> getHeros() {
        return heros;
    }

}
