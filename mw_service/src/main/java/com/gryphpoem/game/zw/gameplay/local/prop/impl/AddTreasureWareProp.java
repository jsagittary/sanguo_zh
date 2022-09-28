package com.gryphpoem.game.zw.gameplay.local.prop.impl;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticTreasureWareDataMgr;
import com.gryphpoem.game.zw.gameplay.local.prop.AbstractUseProp;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.PropConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticProp;
import com.gryphpoem.game.zw.resource.domain.s.StaticTreasureCombat;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.Prop;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-09-26 15:50
 */
public class AddTreasureWareProp extends AbstractUseProp {
    @Override
    public int propType() {
        return PropConstant.PropType.ADD_TREASURE_WARE_PROP;
    }

    @Override
    public void checkUseProp(int count, StaticProp staticProp, Player player, Prop prop, String params, long roleId, int propId, List<CommonPb.Award> listAward, ChangeInfo change, Object... paramArr) throws MwException {
        if (player.getTreasureCombat().getCurCombatId() <= 0) {
            throw new MwException(GameError.COMBAT_PASS_BEFORE.getCode(), String.format("领取章节奖励时，当前关卡未通关, roleId: %s, curCombatId: %s, propId: %s",
                    player.lord.getLordId(), player.getTreasureCombat().getCurCombatId(), propId));
        }

        StaticTreasureCombat sConf = StaticTreasureWareDataMgr.getTreasureCombatMap(player.getTreasureCombat().getCurCombatId());
        if (Objects.isNull(sConf)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), String.format("使用挂机奖励道具时, 无关卡配置, roleId: %s, combatId: %s, propId: %s",
                    player.lord.getLordId(), player.getTreasureCombat().getCurCombatId(), propId));
        }
    }

    @Override
    public List<CommonPb.Award> useProp(int propCount, StaticProp staticProp, Player player, Prop prop, String params, long roleId, int propId, List<CommonPb.Award> listAward, ChangeInfo change, Object... paramArr) {
        StaticTreasureCombat sConf = StaticTreasureWareDataMgr.getTreasureCombatMap(player.getTreasureCombat().getCurCombatId());
        
        int multiple = 0;
        List<Integer> singleStaticAward = null;
        List<Integer> singleRandomAward = null;
        List<List<Integer>> singleAward = null;
        for (List<Integer> awardTime : staticProp.getRewardList()) {
            List<Integer> staticTmp = sConf.getMinuteAward().stream().filter(award -> award.get(0) == awardTime.get(0)
                    && award.get(1) == awardTime.get(1)).findAny().orElse(null);
            List<Integer> randomTmp = sConf.getMinuteRandomAward().stream().filter(award -> award.get(0) == awardTime.get(0)
                    && award.get(1) == awardTime.get(1)).findAny().orElse(null);
            if (ObjectUtils.isEmpty(staticTmp) && ObjectUtils.isEmpty(randomTmp))
                continue;

            //奖励次数
            multiple = awardTime.get(2) / Constant.TREASURE_WARE_RES_OUTPUT_TIME_UNIT;
            if (multiple <= 0)
                continue;

            if (Objects.nonNull(staticTmp))
                multiple *= propCount;

            if (Objects.nonNull(staticTmp)) {
                singleStaticAward = Arrays.asList(staticTmp.get(0), staticTmp.get(1), staticTmp.get(2) * multiple);
            }
            if (Objects.nonNull(randomTmp)) {
                int count = (int) (randomTmp.get(2) * multiple * randomTmp.get(3) / Constant.TEN_THROUSAND);
                if (count == 0)
                    count = 1;
                count *= propCount;
                singleRandomAward = Arrays.asList(randomTmp.get(0), randomTmp.get(1), count);
            }

            singleAward = CheckNull.isNull(singleAward) ? new ArrayList<>() : singleAward;
            if (!ObjectUtils.isEmpty(singleStaticAward)) {
                if (ObjectUtils.isEmpty(singleRandomAward)) {
                    singleAward.add(singleStaticAward);
                } else {
                    singleAward.add(Arrays.asList(singleStaticAward.get(0), singleStaticAward.get(1), singleStaticAward.get(2) + singleRandomAward.get(2)));
                }
            } else {
                singleAward.add(singleRandomAward);
            }
        }

        if (CheckNull.nonEmpty(singleAward)) {
            listAward.addAll(DataResource.ac.getBean(RewardDataManager.class).addAwardDelaySync(player, singleAward, change,
                    AwardFrom.USE_PROP, propId));
        }
        return null;
    }
}
