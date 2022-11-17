package com.gryphpoem.game.zw.resource.pojo.hero;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.pojo.GamePb;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-17 17:03
 */
@Data
public class PartnerHeroId implements GamePb<CommonPb.PartnerHeroIdPb> {
    /**
     * 若当前武将为主将, 副将列表
     */
    private List<Integer> deputyHeroList;
    /**
     * 若当前武将为副将, 主将信息
     */
    private int principalHero;
    /**
     * 兵力
     */
    private int count;

    public PartnerHeroId() {
        deputyHeroList = new ArrayList<>();
    }

    @Override
    public CommonPb.PartnerHeroIdPb createPb(boolean isSaveDb) {
        CommonPb.PartnerHeroIdPb.Builder builder = CommonPb.PartnerHeroIdPb.newBuilder();
        builder.setPrincipleHeroId(this.principalHero);
        if (Objects.nonNull(deputyHeroList))
            builder.addAllDeputyHeroId(this.deputyHeroList);
        builder.setCount(this.count);
        return builder.build();
    }

}
