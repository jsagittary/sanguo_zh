package com.gryphpoem.game.zw.gameplay.local.prop.impl;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.dataMgr.StaticPropDataMgr;
import com.gryphpoem.game.zw.gameplay.local.prop.AbstractUseProp;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.PropConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticProp;
import com.gryphpoem.game.zw.resource.domain.s.StaticWeightBoxProp;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.Prop;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.RandomUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-09-26 16:11
 */
public class WeightCntBoxProp extends AbstractUseProp {
    @Override
    public int propType() {
        return PropConstant.PropType.WEIGHT_CNT_BOX_PROP_TYPE;
    }

    @Override
    public void checkUseProp(int count, StaticProp staticProp, Player player, Prop prop, String params, long roleId, int propId, List<CommonPb.Award> listAward, ChangeInfo change, Object... paramArr) throws MwException {

    }

    @Override
    public List<CommonPb.Award> useProp(int count, StaticProp staticProp, Player player, Prop prop, String params, long roleId, int propId, List<CommonPb.Award> listAward, ChangeInfo change, Object... paramArr) {
        List<CommonPb.Award> awardList = null;
        StaticWeightBoxProp sWeightBoxProp = StaticPropDataMgr.getWeightBoxPropMapById(staticProp.getPropId());
        if (sWeightBoxProp != null) {
            awardList = new ArrayList<>(count);
            RewardDataManager rewardDataManager = DataResource.ac.getBean(RewardDataManager.class);
            for (int i = 0; i < count; i++) {
                List<Integer> maxMin = RandomUtil.getWeightByList(sWeightBoxProp.getRandomNum(), w -> w.get(2));
                if (!CheckNull.isEmpty(maxMin)) {
                    int cnt = RandomHelper.randomInArea(maxMin.get(0), maxMin.get(1) + 1);
                    if (cnt > 0) {
                        awardList.add(rewardDataManager.addAwardSignle(player, sWeightBoxProp.getReward().get(0),
                                sWeightBoxProp.getReward().get(1), cnt, AwardFrom.USE_PROP));
                    }
                }
            }
        }

        return awardList;
    }
}
