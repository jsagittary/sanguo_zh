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
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: 自选已拥有的武将碎片
 * Author: zhangpeng
 * createTime: 2022-09-26 15:44
 */
public class ChooseHeroFragmentProp extends AbstractUseProp {

    @Override
    public int propType() {
        return PropConstant.PropType.CHOOSE_HERO_FRAGMENT_LIMIT_HAVE;
    }

    @Override
    public void checkUseProp(int count, StaticProp staticProp, Player player, Prop prop, String params, long roleId, int propId, List<CommonPb.Award> listAward, ChangeInfo change, Object... paramArr) throws MwException {
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

        // 必须拥有此武将才可以领取碎片
        Hero hero = player.heros.get(choosePropId);
        if (CheckNull.isNull(hero)) {
            throw new MwException(GameError.HERO_NOT_FOUND, String.format("hero not found, heroId:%d, roleId:%d", choosePropId, player.roleId));
        }
        if (CheckNull.isEmpty(staticProp.getRewardList())) {
            throw new MwException(GameError.PROP_CONFIG_ERROR.getCode(), GameError.PROP_CONFIG_ERROR.errMsg(roleId, propId));
        }
    }

    @Override
    public List<CommonPb.Award> useProp(int count, StaticProp staticProp, Player player, Prop prop, String params, long roleId, int propId, List<CommonPb.Award> listAward, ChangeInfo change, Object... paramArr) {
        List<Integer> reward = null;
        List<List<Integer>> rewardArr;
        Integer choosePropId = Integer.parseInt(params);
        for (List<Integer> tmp : staticProp.getRewardList()) {
            if (CheckNull.isEmpty(tmp) || tmp.size() < 3) {
                continue;
            }
            if (tmp.get(1) == choosePropId.intValue()) {
                reward = tmp;
                break;
            }
        }

        if (CheckNull.nonEmpty(reward)) {
            rewardArr = new ArrayList<>();
            rewardArr.add(reward);
            listAward.addAll(DataResource.ac.getBean(RewardDataManager.class).sendReward(player, rewardArr, count, AwardFrom.USE_PROP));
        }

        return null;
    }
}
