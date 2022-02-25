package com.gryphpoem.game.zw.crosssimple.util;

import com.gryphpoem.cross.common.RankItemInt;
import com.gryphpoem.cross.player.dto.PlayerLordDto;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWarPlaneDataMgr;
import com.gryphpoem.game.zw.manager.TechDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.*;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.HeroConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.domain.s.StaticHero;
import com.gryphpoem.game.zw.resource.domain.s.StaticPlaneUpgrade;
import com.gryphpoem.game.zw.resource.pojo.WarPlane;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.CalculateUtil;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.service.FightService;

import java.util.Map;
import java.util.Objects;

/**
 * @author QiuKun
 * @ClassName CrossPlayerPbHelper.java
 * @Description
 * @date 2019年5月16日
 */
public abstract class CrossPlayerPbHelper {

    public static CrossPlayerPb.Builder toCrossPlayerPb(Player player) {
        CrossPlayerPb.Builder builder = CrossPlayerPb.newBuilder();
        builder.setCrossLord(toCrossLordPb(player));
        return builder;
    }

    public static CrossRankItem.Builder buildCrossRankItem(PlayerLordDto lordDto, RankItemInt rank) {
        CrossRankItem.Builder builder = CrossRankItem.newBuilder();
        builder.setRoleId(rank.getLordId());
        builder.setRanking(rank.getRank());
        builder.setVal(rank.getRankValue());
        builder.setOriginalServerId(lordDto != null ? lordDto.getOriginalServerId() : 0);
        builder.setMainServerId(lordDto != null ? lordDto.getServerId() : 0);
        builder.setCamp(lordDto != null ? lordDto.getCamp() : Constant.Camp.EMPIRE);
        builder.setNick(lordDto != null ? lordDto.getRoleName() : String.valueOf(rank.getLordId()));
        if (Objects.nonNull(lordDto) && CheckNull.nonEmpty(lordDto.getAppearance())){
            builder.setPortrait(lordDto.getAppearance().getOrDefault(AwardType.PORTRAIT, 1));
        }else{
            builder.setPortrait(1);
        }
        return builder;
    }

    public static CrossRankItem.Builder buildPlayerRankItem(Player player, RankItemInt rank) {
        CrossRankItem.Builder builder = CrossRankItem.newBuilder();
        builder.setRoleId(rank.getLordId());
        builder.setRanking(rank.getRank());
        builder.setVal(rank.getRankValue());
        builder.setOriginalServerId(player.account.getServerId());
        ServerSetting serverSetting = DataResource.ac.getBean(ServerSetting.class);
        builder.setMainServerId(serverSetting.getServerID());
        builder.setCamp(player.getCamp());
        builder.setNick(player.lord.getNick());
        return builder;
    }

    /**
     * 生成lord
     *
     * @param player
     * @return
     */
    public static CrossLordPb.Builder toCrossLordPb(Player player) {
        CrossLordPb.Builder builder = CrossLordPb.newBuilder();
        Lord lord = player.lord;
        builder.setLordId(lord.getLordId());
        builder.setNick(lord.getNick());
        builder.setPortrait(lord.getPortrait());
        builder.setArea(lord.getArea());
        builder.setCamp(lord.getCamp());
        builder.setLevel(lord.getLevel());
        builder.setVip(lord.getVip());
        builder.setFight(lord.getFight());
        builder.setJob(lord.getJob());
        builder.setServerId(player.account.getServerId());
        builder.setRank(lord.getRanks());
        return builder;
    }

    /**
     * 返回 hero builder
     *
     * @param player
     * @param hero
     * @return
     */
    public static CrossHeroPb.Builder toCrossHeroBuilderPb(Player player, Hero hero) {
        FightService fightService = DataResource.ac.getBean(FightService.class);
        TechDataManager techDataManager = DataResource.ac.getBean(TechDataManager.class);

        CrossHeroPb.Builder heroPb = CrossHeroPb.newBuilder();
        heroPb.setLordId(player.lord.getLordId());
        heroPb.setHeroId(hero.getHeroId());
        heroPb.setHeroType(hero.getHeroType());
        heroPb.setLevel(hero.getLevel());
        heroPb.setDecorated(hero.getDecorated());
        heroPb.setCount(hero.getCount());

        Map<Integer, Integer> attrMap = CalculateUtil.processAttr(player, hero);
        attrMap.forEach((k, v) -> heroPb.addAttr(TwoInt.newBuilder().setV1(k).setV2(v)));

        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
        int line = fightService.calcHeroLine(player, hero, staticHero.getLine());
        int lead = (int) Math.ceil(hero.getAttr()[HeroConstant.ATTR_LEAD] * 1.0 / line);// 当兵力不能被整除时，向上取整
        int heroLv = techDataManager.getIntensifyLv4HeroType(player, staticHero.getType());// 等级
        int restrain = techDataManager.getIntensifyRestrain4HeroType(player, staticHero.getType());// 克制值
        heroPb.setLine(fightService.calcHeroLine(player, hero, staticHero.getLine()));
        heroPb.setLead(lead);
        heroPb.setIntensifyLv(heroLv);
        heroPb.setRestrain(restrain);
        heroPb.setStatus(hero.getStatus());
        heroPb.setPos(hero.getPos());

        hero.getWarPlanes().forEach(planeId -> {
            CrossPlanePb.Builder b = toCrossPlanePb(planeId, player);
            if (b != null) {
                heroPb.addPlaneList(b);
            }
        });
        return heroPb;
    }

    /**
     * 生成跨服英雄核心信息
     *
     * @param player
     * @param hero
     * @return
     */
    public static CrossHeroPb toCrossHeroPb(Player player, Hero hero) {
        return toCrossHeroBuilderPb(player, hero).build();
    }

    /**
     * 生成战机
     *
     * @param planeId
     * @param player
     * @return
     */
    public static CrossPlanePb.Builder toCrossPlanePb(int planeId, Player player) {
        CrossPlanePb.Builder planePb = CrossPlanePb.newBuilder();
        StaticPlaneUpgrade sPlaneUpgrade = StaticWarPlaneDataMgr.getPlaneUpgradeById(planeId);
        if (CheckNull.isNull(sPlaneUpgrade)) {
            return null;
        }
        WarPlane warPlane = player.warPlanes.get(sPlaneUpgrade.getPlaneType());
        planePb.setPlaneId(planeId);
        planePb.setLevel(warPlane.getLevel());
        planePb.setBattlePos(warPlane.getBattlePos());
        return planePb;
    }
}
