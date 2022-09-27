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
    public List<CommonPb.Award> useProp(int count, StaticProp staticProp, Player player, Prop prop, String params, long roleId, int propId, List<CommonPb.Award> listAward, ChangeInfo change, Object... paramArr) {
        //因跑马灯在此判断，因此将判断加在这里
        if (count != 1) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "自选箱使用非一个, roleId: ", player.roleId,
                    "usedCount: ", prop.getCount(), ", count = ", count);
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

        List<Integer> reward = null;
        List<List<Integer>> rewardArr;
        for (List<Integer> tmp : staticProp.getRewardList()) {
            if (CheckNull.isEmpty(tmp) || tmp.size() < 3) {
                throw new MwException(GameError.PROP_CONFIG_ERROR.getCode(), GameError.PROP_CONFIG_ERROR.errMsg(roleId, propId));
            }
            if (tmp.get(1) == choosePropId.intValue()) {
                reward = tmp;
                break;
            }
        }
        if (ObjectUtils.isEmpty(reward)) {
            throw new MwException(GameError.CHOOSE_PROP_ERROR.getCode(), GameError.CHOOSE_PROP_ERROR.errMsg(roleId, propId));
        }

        rewardArr = new ArrayList<>();
        rewardArr.add(reward);
        listAward.addAll(DataResource.ac.getBean(RewardDataManager.class).addAwardDelaySync(player, rewardArr, change,
                AwardFrom.USE_PROP));
        return null;
    }
}
