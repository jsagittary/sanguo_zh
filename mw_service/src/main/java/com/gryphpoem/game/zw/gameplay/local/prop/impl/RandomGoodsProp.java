package com.gryphpoem.game.zw.gameplay.local.prop.impl;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.dataMgr.StaticPropDataMgr;
import com.gryphpoem.game.zw.gameplay.local.prop.AbstractUseProp;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.constant.PropConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticProp;
import com.gryphpoem.game.zw.resource.domain.s.StaticRandomProp;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.Prop;
import com.gryphpoem.game.zw.resource.util.RandomHelper;
import com.gryphpoem.game.zw.resource.util.Turple;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-09-26 16:04
 */
public class RandomGoodsProp extends AbstractUseProp {
    @Override
    public int propType() {
        return PropConstant.PropType.RANDMON_PROP_TYPE;
    }

    @Override
    public List<CommonPb.Award> useProp(int count, StaticProp staticProp, Player player, Prop prop, String params, long roleId, int propId, List<CommonPb.Award> listAward, ChangeInfo change, Object... paramArr) {
        StaticRandomProp sRandomProp = StaticPropDataMgr.getRandomPropById(staticProp.getPropId());
        List<CommonPb.Award> awardList = new ArrayList<>(count);
        if (sRandomProp != null) {
            for (int i = 0; i < count; i++) {
                // 比较那个找出物品数量最小
                int minPropId = sRandomProp.getCompare().stream().map(pId -> {
                    Prop p = player.props.get(pId);
                    int cnt = p == null ? 0 : p.getCount();
                    return new Turple<>(pId, cnt);
                }).min(Comparator.comparingInt(Turple::getB)).get().getA();

                int minPropCnt = RandomHelper.randomInArea(sRandomProp.getRandomNum().get(0),
                        sRandomProp.getRandomNum().get(1));
                awardList.add(DataResource.ac.getBean(RewardDataManager.class).addAwardSignle(player, AwardType.PROP, minPropId, minPropCnt,
                        AwardFrom.USE_PROP));
            }
        }

        return awardList;
    }
}
