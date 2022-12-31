package com.gryphpoem.game.zw.resource.util;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.hero.PartnerHero;

import java.util.Objects;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-17 17:27
 */
public class HeroUtil {
    public static boolean isEmptyPartner(PartnerHero partnerHero) {
        return CheckNull.isNull(partnerHero) || CheckNull.isNull(partnerHero.getPrincipalHero());
    }

    public static void heroOnDef(PartnerHero partnerHero) {
        if (isEmptyPartner(partnerHero))
            return;
        partnerHero.getPrincipalHero().onDef(partnerHero.getPrincipalHero().getPos());
        if (CheckNull.nonEmpty(partnerHero.getDeputyHeroList())) {
            partnerHero.getDeputyHeroList().forEach(hero -> {
                if (CheckNull.isNull(hero)) return;
                hero.onDef(hero.getPos());
            });
        }
    }

    public static CommonPb.OnePartnerHeroPb.Builder onePartnerHeroPb(Hero hero) {
        CommonPb.OnePartnerHeroPb.Builder onePb = CommonPb.OnePartnerHeroPb.newBuilder();
        onePb.setHeroId(hero.getHeroId());
        onePb.setLevel(hero.getLevel());
        onePb.setExp(hero.getExp());
        onePb.setCount(hero.getCount());
        onePb.setDecorated(hero.getDecorated());
        onePb.setGradeKeyId(hero.getGradeKeyId());
        onePb.setHeroRoleType(hero.getRoleType());
        if (hero.getPos() > 0) {
            onePb.setPos(hero.getPos());
        } else if (hero.getWallPos() > 0) {
            onePb.setWallPos(hero.getWallPos());
        } else if (hero.getAcqPos() > 0) {
            onePb.setAcqPos(hero.getAcqPos());
        }
        onePb.setPartnerPosIndex(hero.getPartnerPosIndex());
        return onePb;
    }

    public static void setHeroState(CommonPb.PartnerHeroIdPb partnerHeroIdPb, Player player, int state) {
        Hero hero = player.heros.get(partnerHeroIdPb.getPrincipleHeroId());
        if (Objects.nonNull(hero)) {
            hero.setState(state);
        }
        if (CheckNull.nonEmpty(partnerHeroIdPb.getDeputyHeroIdList())) {
            partnerHeroIdPb.getDeputyHeroIdList().forEach(heroId -> {
                Hero hero_ = player.heros.get(partnerHeroIdPb.getPrincipleHeroId());
                if (Objects.nonNull(hero_)) {
                    hero_.setState(state);
                }
            });
        }
    }

    /**
     * 将领经验=（杀敌数+损兵数）/ (s_system表中Id389)
     *
     * @param force
     * @return
     */
    public static float addHeroExpExp(Force force) {
        return (force.killed + force.totalLost) * Constant.NORMAL_ATTACK_COPY_AND_OTHER_COMBAT_EXPERIENCE_COEFFICIENT;
    }
}
