package com.gryphpoem.game.zw.gameplay.local.prop.impl;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.gameplay.local.prop.AbstractUseProp;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.PropConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticProp;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.FunCard;
import com.gryphpoem.game.zw.resource.pojo.Prop;

import java.util.List;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-09-26 16:14
 */
public class OnHookCardProp extends AbstractUseProp {
    @Override
    public int propType() {
        return PropConstant.PropType.ON_HOOK_CARD_TYPE;
    }

    @Override
    public void checkUseProp(int count, StaticProp staticProp, Player player, Prop prop, String params, long roleId, int propId, List<CommonPb.Award> listAward, ChangeInfo change, Object... paramArr) throws MwException {
    }

    @Override
    public List<CommonPb.Award> useProp(int count, StaticProp staticProp, Player player, Prop prop, String params, long roleId, int propId, List<CommonPb.Award> listAward, ChangeInfo change, Object... paramArr) {
        FunCard funCard = player.funCards.get(FunCard.CARD_TYPE[8]);
        if (funCard == null) {
            funCard = new FunCard(FunCard.CARD_TYPE[8]);
            player.funCards.put(funCard.getType(), funCard);
        }
        funCard.addExpireSecond(staticProp.getDuration() * count);
        return null;
    }
}
