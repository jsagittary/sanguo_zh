package com.gryphpoem.game.zw.gameplay.local.prop.impl;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
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
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: 自选宝箱跑马灯判断
 * Author: zhangpeng
 * createTime: 2022-09-26 15:47
 */
public class ChooseGoodsProp extends AbstractUseProp {
    @Override
    public int propType() {
        return PropConstant.PropType.CHOOSE_PROP_TYPE;
    }

    @Override
    public void checkUseProp(int count, StaticProp staticProp, Player player, Prop prop, String params, long roleId, int propId, List<CommonPb.Award> listAward, ChangeInfo change, Object... paramArr) throws MwException {
        //因跑马灯在此判断，因此将判断加在这里
        if (!staticProp.canBatchUse()) {
            if (count != 1) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "自选箱使用非一个, roleId: ", player.roleId,
                        "usedCount: ", prop.getCount(), ", count = ", count);
            }
        }

        Integer choosePropId;
        try {
            choosePropId = Integer.parseInt(params);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            throw new MwException(GameError.PARAM_ERROR.getCode(), "自选箱参数错误, roleId:", player.roleId,
                    "usedCount", prop.getCount(), ", params=" + params);
        }
        if (ObjectUtils.isEmpty(choosePropId)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "自选箱参数错误, roleId:", player.roleId,
                    "usedCount", prop.getCount(), ", params=" + params);
        }

        if (CheckNull.isEmpty(staticProp.getRewardList())) {
            throw new MwException(GameError.PROP_CONFIG_ERROR.getCode(), GameError.PROP_CONFIG_ERROR.errMsg(roleId, propId));
        }
    }

    @Override
    public List<CommonPb.Award> useProp(int count, StaticProp staticProp, Player player, Prop prop, String params, long roleId, int propId, List<CommonPb.Award> listAward, ChangeInfo change, Object... paramArr) {
        List<Integer> reward = null;
        List<List<Integer>> rewardArr = new ArrayList<>();
        Integer choosePropId = Integer.parseInt(params);

        if (staticProp.canBatchUse()) {
            for (List<Integer> configReward : staticProp.getRewardList()) {
                if (CheckNull.isEmpty(configReward) || configReward.size() < 3) {
                    continue;
                }
                if (configReward.get(1) == choosePropId.intValue()) {
                    Integer num = configReward.get(2);
                    configReward.set(2, num * count);
                    reward = configReward;
                    rewardArr.add(reward);
                    break;
                }
            }
        } else {
            for (List<Integer> tmp : staticProp.getRewardList()) {
                if (CheckNull.isEmpty(tmp) || tmp.size() < 3) {
                    continue;
                }
                if (tmp.get(1) == choosePropId.intValue()) {
                    reward = tmp;
                    rewardArr.add(reward);
                    break;
                }
            }
        }

        if (CheckNull.nonEmpty(rewardArr)) {
            listAward.addAll(DataResource.ac.getBean(RewardDataManager.class).addAwardDelaySync(player, rewardArr, change, AwardFrom.USE_PROP));
        }

        return null;
    }
}
