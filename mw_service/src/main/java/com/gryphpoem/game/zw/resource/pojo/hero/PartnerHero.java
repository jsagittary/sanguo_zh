package com.gryphpoem.game.zw.resource.pojo.hero;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.HeroConstant;
import com.gryphpoem.game.zw.resource.pojo.GamePb;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.HeroUtil;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-17 10:21
 */
@Data
public class PartnerHero implements GamePb<CommonPb.PartnerHeroPb> {
    /**
     * 若当前武将为主将, 副将列表
     */
    private List<Hero> deputyHeroList;
    /**
     * 若当前武将为副将, 主将信息
     */
    private Hero principalHero;

    public PartnerHero() {
        deputyHeroList = new ArrayList<>(1);
    }

    public void setState(int state) {
        this.principalHero.setState(state);
        if (CheckNull.isEmpty(deputyHeroList)) return;
        deputyHeroList.forEach(hero -> {
            if (CheckNull.isNull(hero)) return;
            hero.setState(state);
        });
    }

    public void setStatus(int status) {
        this.principalHero.setStatus(status);
        if (CheckNull.isEmpty(deputyHeroList)) return;
        deputyHeroList.forEach(hero -> {
            if (CheckNull.isNull(hero)) return;
            hero.setStatus(status);
        });
    }

    public Hero getCurHero(int heroType, int partnerPosIndex) {
        switch (heroType) {
            case HeroConstant.HERO_ROLE_TYPE_PRINCIPAL:
                return this.principalHero;
            case HeroConstant.HERO_ROLE_TYPE_DEPUTY:
                if (CheckNull.isEmpty(this.deputyHeroList))
                    return null;
                return this.deputyHeroList.stream().filter(hero -> hero.getPartnerPosIndex() == partnerPosIndex).findFirst().orElse(null);
        }

        return null;
    }

    public void onBattle(int pos) {
        this.principalHero.onBattle(pos);
        if (CheckNull.isEmpty(deputyHeroList)) return;
        deputyHeroList.forEach(hero -> {
            if (CheckNull.isNull(hero)) return;
            hero.onBattle(pos);
        });
    }

    public void onDef(int pos) {
        this.principalHero.onDef(pos);
        if (CheckNull.isEmpty(deputyHeroList)) return;
        deputyHeroList.forEach(hero -> {
            if (CheckNull.isNull(hero)) return;
            hero.onDef(pos);
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartnerHero that = (PartnerHero) o;
        if (that.getPrincipalHero() == null) return false;
        return principalHero.getHeroId() == that.getPrincipalHero().getHeroId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(principalHero.getHeroId());
    }

    @Override
    public CommonPb.PartnerHeroPb createPb(boolean isSaveDb) {
        CommonPb.PartnerHeroPb.Builder builder = CommonPb.PartnerHeroPb.newBuilder();
        if (Objects.nonNull(this.principalHero)) {
            builder.addOne(HeroUtil.onePartnerHeroPb(this.principalHero));
        }
        if (CheckNull.nonEmpty(this.getDeputyHeroList())) {
            this.deputyHeroList.forEach(hero -> builder.addOne(HeroUtil.onePartnerHeroPb(hero)));
        }

        return builder.build();
    }

    public CommonPb.PartnerHeroIdPb convertTo() {
        CommonPb.PartnerHeroIdPb.Builder builder = CommonPb.PartnerHeroIdPb.newBuilder();
        builder.setPrincipleHeroId(this.principalHero.getHeroId());
        if (CheckNull.nonEmpty(this.getDeputyHeroList())) {
            this.getDeputyHeroList().forEach(hero -> builder.addDeputyHeroId(hero.getHeroId()));
        }
        builder.setCount(this.principalHero.getCount());
        return builder.build();
    }

}
