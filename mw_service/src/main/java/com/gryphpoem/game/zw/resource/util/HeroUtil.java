package com.gryphpoem.game.zw.resource.util;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.HeroConstant;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.hero.PartnerHero;

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
        onePb.setHeroRoleType(HeroConstant.HERO_ROLE_TYPE_PRINCIPAL);
        if (hero.getPos() > 0) {
            onePb.setPos(hero.getPos());
        } else if (hero.getWallPos() > 0) {
            onePb.setWallPos(hero.getWallPos());
        } else if (hero.getAcqPos() > 0) {
            onePb.setAcqPos(hero.getAcqPos());
        }
        return onePb;
    }
}
