package com.gryphpoem.game.zw.resource.pojo.hero;

import com.gryphpoem.game.zw.resource.constant.HeroConstant;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-17 15:47
 */
@Data
public class PlayerFormation {
    /**
     * 上阵将领，记录上阵将领的id，0位为补位，1-4位为上阵将领id
     */
    private PartnerHero[] heroBattle = new PartnerHero[HeroConstant.HERO_BATTLE_LEN + 1];

    /**
     * 防守将领, 记录防守将领的id, 0位为补位，1-4位为上阵将领id
     */
    private PartnerHero[] heroDef = new PartnerHero[HeroConstant.HERO_BATTLE_LEN + 1];

    /**
     * 城防将领
     */
    private PartnerHero[] heroWall = new PartnerHero[HeroConstant.HERO_BATTLE_LEN + 1];
    /**
     * 采集将领
     */
    private PartnerHero[] heroAcq = new PartnerHero[HeroConstant.HERO_BATTLE_LEN + 1];

    /**
     * 上阵将领在其他位置的映射(基于上阵将领为基础,存储的是位置,而非将领id) key:2表示副本 key:3出征将领
     */
    private Map<Integer, List<Integer>> heroBattlePos = new HashMap<>();
}
