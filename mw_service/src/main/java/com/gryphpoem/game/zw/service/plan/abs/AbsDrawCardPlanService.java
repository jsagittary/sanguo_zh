package com.gryphpoem.game.zw.service.plan.abs;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.DrawCardOperation;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticDrawCardWeight;
import com.gryphpoem.game.zw.resource.domain.s.StaticDrawHeoPlan;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroSearch;
import com.gryphpoem.game.zw.resource.domain.s.StaticPlaneSearch;
import com.gryphpoem.game.zw.resource.pojo.plan.FunctionPlanData;
import com.gryphpoem.game.zw.resource.pojo.plan.PlanFunction;
import com.gryphpoem.game.zw.resource.pojo.tavern.DrawCardData;

import java.util.Date;
import java.util.List;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-22 17:30
 */
public abstract class AbsDrawCardPlanService extends AbsFunctionPlanService{
    @Override
    public PlanFunction[] functionId() {
        return new PlanFunction[0];
    }

    @Override
    public void handleOnPreviewTime(int keyId) {

    }

    @Override
    public void handleOnBeginTime(int keyId) {

    }

    @Override
    public void handleOnEndTime(int keyId) {

    }

    @Override
    public void handleOnDisplayTime(int keyId) {

    }

    @Override
    public void handleOnDay(Player player) {

    }

    @Override
    public void checkCondition(Player player, Object... params) throws MwException {

    }

    @Override
    public void updateFunctionData(Player player, Object... params) {

    }

    /**
     * 一次抽卡
     *
     * @param player
     * @param costCount
     * @param drawCardCount
     * @param drawCardCostType
     * @param config
     * @param now
     * @return
     * @throws MwException
     */
    public abstract CommonPb.SearchHero onceDraw(Player player, FunctionPlanData drawCardData,  StaticDrawHeoPlan planData, int costCount, DrawCardOperation.DrawCardCount drawCardCount,
                                        DrawCardOperation.DrawCardCostType drawCardCostType, StaticDrawCardWeight config, Date now) throws MwException;

    /**
     * 随机奖励
     *
     * @param roleId
     * @param drawCardData
     * @param now
     * @param config
     * @return
     * @throws MwException
     */
    public abstract StaticHeroSearch randomPriorityReward(long roleId, FunctionPlanData drawCardData, StaticDrawHeoPlan planData, Date now, StaticDrawCardWeight config) throws MwException;
}
