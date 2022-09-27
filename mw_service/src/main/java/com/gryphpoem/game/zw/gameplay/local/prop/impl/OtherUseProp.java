package com.gryphpoem.game.zw.gameplay.local.prop.impl;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.gameplay.local.prop.AbstractUseProp;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticProp;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.Prop;
import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.List;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-09-26 15:52
 */
public class OtherUseProp extends AbstractUseProp {
    @Override
    public int propType() {
        return -1;
    }

    @Override
    public void checkUseProp(int count, StaticProp staticProp, Player player, Prop prop, String params, long roleId, int propId, List<CommonPb.Award> listAward, ChangeInfo change, Object... paramArr) throws MwException {
    }

    @Override
    public List<CommonPb.Award> useProp(int count, StaticProp staticProp, Player player, Prop prop, String params, long roleId, int propId, List<CommonPb.Award> listAward, ChangeInfo change, Object... paramArr) {
        for (int i = 0; i < count; i++) {
            if (!CheckNull.isEmpty(staticProp.getRewardList())) {
                listAward.addAll(DataResource.ac.getBean(RewardDataManager.class).addAwardDelaySync(player, staticProp.getRewardList(), change,
                        AwardFrom.USE_PROP));
            }
        }

        return null;
    }
}
