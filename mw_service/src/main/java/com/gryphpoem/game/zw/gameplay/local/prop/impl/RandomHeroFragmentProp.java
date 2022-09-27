package com.gryphpoem.game.zw.gameplay.local.prop.impl;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.gameplay.local.prop.AbstractUseProp;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.PropConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticProp;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.Prop;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.RandomHelper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-09-26 16:51
 */
public class RandomHeroFragmentProp extends AbstractUseProp {
    @Override
    public int propType() {
        return PropConstant.PropType.RANDOM_HERO_FRAGMENT_PROP;
    }

    @Override
    public void checkUseProp(int count, StaticProp staticProp, Player player, Prop prop, String params, long roleId, int propId, List<CommonPb.Award> listAward, ChangeInfo change, Object... paramArr) throws MwException {
        if (CheckNull.isEmpty(staticProp.getRewardList())) {
            throw new MwException(GameError.PROP_CONFIG_ERROR.getCode(), GameError.PROP_CONFIG_ERROR.errMsg(roleId, propId));
        }

        List<List<Integer>> awardList = staticProp.getRewardList().stream().filter(list -> CheckNull.nonEmpty(list) && list.size() >= 2 && player.heros.get(list.get(1)) != null).collect(Collectors.toList());
        if (CheckNull.isEmpty(awardList)) {
            throw new MwException(GameError.HERO_NOT_FOUND, String.format("hero not found, propId:%d, roleId:%d", staticProp.getPropId(), player.roleId));
        }
    }

    @Override
    public List<CommonPb.Award> useProp(int count, StaticProp staticProp, Player player, Prop prop, String params, long roleId, int propId, List<CommonPb.Award> listAward, ChangeInfo change, Object... paramArr) {
        int temp = 0;
        List<List<Integer>> awardList = staticProp.getRewardList().stream().filter(list -> player.heros.get(list.get(1)) != null).collect(Collectors.toList());
        int totalWeight = awardList.stream().filter(list -> CheckNull.nonEmpty(list) && list.size() >= 4).mapToInt(list -> list.get(3)).sum();
        int random = RandomHelper.randomInSize(totalWeight);
        RewardDataManager rewardDataManager = DataResource.ac.getBean(RewardDataManager.class);
        for (int i = 0; i < count; i++) {
            for (List<Integer> shs : awardList) {
                if (CheckNull.isEmpty(shs) || shs.size() < 4)
                    continue;
                temp += shs.get(3);
                if (temp >= random) {
                    listAward.add(rewardDataManager.addAwardSignle(player, shs.get(0), shs.get(1), shs.get(2), AwardFrom.USE_PROP));
                    break;
                }
            }
        }
        return null;
    }
}
