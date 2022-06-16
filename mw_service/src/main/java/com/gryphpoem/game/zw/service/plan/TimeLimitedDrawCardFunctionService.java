package com.gryphpoem.game.zw.service.plan;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticDrawHeroDataMgr;
import com.gryphpoem.game.zw.manager.FunctionPlanDataManager;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticDrawHeoPlan;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.plan.FunctionPlanData;
import com.gryphpoem.game.zw.resource.pojo.plan.PlanFunction;
import com.gryphpoem.game.zw.resource.pojo.plan.draw.DrawCardTimeLimitedFunctionPlanData;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.service.plan.abs.AbsFunctionPlanService;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-16 11:45
 */
@Component
public class TimeLimitedDrawCardFunctionService extends AbsFunctionPlanService {

    @Autowired
    private DrawCardPlanTemplateService drawCardPlanTemplateService;
    @Autowired
    private FunctionPlanDataManager functionPlanDataManager;
    @Autowired
    private StaticDrawHeroDataMgr staticDrawHeroDataMgr;

    /**
     * 领取免费抽取次数
     *
     * @param roleId
     * @param req
     * @return
     */
    public GamePb5.ReceiveTimeLimitedDrawCountRs receiveTimeLimitedDrawCount(long roleId, GamePb5.ReceiveTimeLimitedDrawCountRq req) {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        PlanFunction planFunction = PlanFunction.convertTo(req.getFunctionId());
        if (CheckNull.isNull(planFunction) || !ArrayUtils.contains(functionId(), planFunction)) {
            throw new MwException(GameError.PARAM_ERROR, String.format("roleId:%d, function:%d", roleId, req.getFunctionId()));
        }

        Date now = new Date();
        checkAndGetPlan(req.getKeyId(), now, player, PlanFunction.PlanStatus.OPEN);
        FunctionPlanData functionPlanData = functionPlanDataManager.functionPlanData(player.getFunctionPlanData(), planFunction, req.getKeyId(), true);
        if (CheckNull.isNull(functionPlanData)) {
            throw new MwException(GameError.PARAM_ERROR, String.format("role:%d, function:%d, keyId:%d, no player function data", roleId, req.getFunctionId(), req.getKeyId()));
        }
        DrawCardTimeLimitedFunctionPlanData planData = (DrawCardTimeLimitedFunctionPlanData) functionPlanData;
        if (planData.getReceiveStatus() != FunctionPlanData.CAN_RECEIVE_STATUS) {
            throw new MwException(GameError.AWARD_HAD_GOT, String.format("roleId:%d, has received award, status:%d", roleId, planData.getReceiveStatus()));
        }
        if (CheckNull.isEmpty(HeroConstant.TIME_LIMITED_DRAW_DEFEATED_REBELS_NUM_AND_FREE_TIMES)) {
            throw new MwException(GameError.NO_CONFIG, String.format("roleId:%d, no config", roleId));
        }
        if (planData.getProgress() < HeroConstant.TIME_LIMITED_DRAW_DEFEATED_REBELS_NUM_AND_FREE_TIMES.get(0)) {
            throw new MwException(GameError.ACTIVITY_AWARD_NOT_GET, String.format("roleId:%d, not match condition, progress:%d", roleId, planData.getProgress()));
        }

        planData.operationFreeNum(HeroConstant.TIME_LIMITED_DRAW_DEFEATED_REBELS_NUM_AND_FREE_TIMES.get(1));
        planData.updateReceiveStatus(FunctionPlanData.HAS_RECEIVED_STATUS);
        GamePb5.ReceiveTimeLimitedDrawCountRs.Builder builder = GamePb5.ReceiveTimeLimitedDrawCountRs.newBuilder();
        builder.setData(planData.createPb(false));
        return builder.build();
    }

    @Override
    public PlanFunction[] functionId() {
        return new PlanFunction[]{PlanFunction.DRAW_CARD};
    }

    @Override
    public void handleOnPreviewTime(int keyId) {
        Collection<Player> onlinePlayer = playerDataManager.getAllOnlinePlayer().values();
        if (CheckNull.isEmpty(onlinePlayer))
            return;
        StaticDrawHeoPlan staticPlan = staticDrawHeroDataMgr.getDrawHeoPlanMap().get(keyId);
        if (CheckNull.isNull(staticPlan)) {
            LogUtil.error(String.format("plan keyId:%d, not found", keyId));
            return;
        }
        PlanFunction planFunction = PlanFunction.convertTo(staticPlan.getFunctionId());
        if (CheckNull.isNull(planFunction)) {
            LogUtil.error(String.format("planFunctionId:%, not found", staticPlan.getFunctionId()));
        }

        Date now = new Date();
        onlinePlayer.forEach(player -> {
            FunctionPlanData planData = functionPlanDataManager.functionPlanData(player.getFunctionPlanData(), planFunction, keyId, true);
            if (CheckNull.isNull(planData))
                return;
            drawCardPlanTemplateService.syncChangeDrawCardActPlan(player, planData, staticPlan, ACT_NEW, now);
        });
    }

    @Override
    public void handleOnBeginTime(int keyId) {

    }

    @Override
    public void handleOnEndTime(int keyId) {
        Collection<Player> onlinePlayer = playerDataManager.getAllOnlinePlayer().values();
        if (CheckNull.isEmpty(onlinePlayer))
            return;
        StaticDrawHeoPlan staticPlan = staticDrawHeroDataMgr.getDrawHeoPlanMap().get(keyId);
        if (CheckNull.isNull(staticPlan)) {
            LogUtil.error(String.format("plan keyId:%d, not found", keyId));
            return;
        }
        PlanFunction planFunction = PlanFunction.convertTo(staticPlan.getFunctionId());
        if (CheckNull.isNull(planFunction)) {
            LogUtil.error(String.format("planFunctionId:%, not found", staticPlan.getFunctionId()));
        }

        Date now = new Date();
        onlinePlayer.forEach(player -> {
            FunctionPlanData planData = functionPlanDataManager.functionPlanData(player.getFunctionPlanData(), planFunction, keyId, false);
            drawCardPlanTemplateService.syncChangeDrawCardActPlan(player, planData, staticPlan, ACT_DELETE, now);
        });
    }

    @Override
    public void handleOnDisplayTime(int keyId) {

    }

    @Override
    public void handleOnDay(Player player) {
        Date now = new Date();
        List<StaticDrawHeoPlan> planList = staticDrawHeroDataMgr.getPlanList(now, PlanFunction.DRAW_CARD.getFunctionId(), PlanFunction.PlanStatus.OPEN);
        if (CheckNull.isEmpty(planList))
            return;
        planList.forEach(plan -> {
            DrawCardTimeLimitedFunctionPlanData planData = (DrawCardTimeLimitedFunctionPlanData) functionPlanDataManager.functionPlanData(player.getFunctionPlanData(), PlanFunction.DRAW_CARD, plan.getId(), true);
            if (CheckNull.isNull(planData))
                return;
            planData.resetDaily();
            drawCardPlanTemplateService.syncChangeDrawCardActPlan(player, planData, plan, ACT_UPDATE, now);
        });
    }

    @Override
    public void checkCondition(Player player, Object... params) throws MwException {
        DrawCardOperation.DrawCardCostType costType = (DrawCardOperation.DrawCardCostType) params[0];
        DrawCardOperation.DrawCardCount countType = (DrawCardOperation.DrawCardCount) params[1];
        ChangeInfo changeInfo = (ChangeInfo) params[2];
        DrawCardTimeLimitedFunctionPlanData functionPlanData = (DrawCardTimeLimitedFunctionPlanData) params[3];
        switch (costType) {
            case FREE:
                int totalFreeCount = functionPlanData.getFreeNum();
                if (totalFreeCount < countType.getCount()) {
                    throw new MwException(GameError.FREE_DRAW_CARD_COUNT_NOT_ENOUGH.getCode(), "免费次数不足, roleId:", player.roleId, ", totalFreeCount:", totalFreeCount);
                }
                // 扣除次数
                functionPlanData.operationFreeNum(-countType.getCount());
                break;
            case MONEY:
                Integer goldNum = HeroConstant.TIME_LIMITED_SEARCH_FOR_GOLD_COIN_CONSUMPTION.get(countType.getType() - 1);
                if (CheckNull.isNull(goldNum) || goldNum <= 0) {
                    throw new MwException(GameError.NO_CONFIG.getCode(), "将领寻访玉璧消耗未配置, roleId:", player.roleId,
                            ", costType:", costType, ", drawCount:", countType.getCount());
                }

                rewardDataManager.checkMoneyIsEnough(player, AwardType.Money.GOLD, goldNum, "draw permanent card");
                rewardDataManager.subGold(player, goldNum, AwardFrom.HERO_SUPER_SEARCH);
                changeInfo.addChangeType(AwardType.MONEY, AwardType.Money.GOLD);
                break;
        }
    }

    @Override
    public void updateFunctionData(Player player, Object... params) {
        Date now = new Date();
        List<StaticDrawHeoPlan> planList = staticDrawHeroDataMgr.getPlanList(now, PlanFunction.DRAW_CARD.getFunctionId(), PlanFunction.PlanStatus.OPEN);
        if (CheckNull.isEmpty(planList))
            return;
        planList.forEach(plan -> {
            DrawCardTimeLimitedFunctionPlanData planData = (DrawCardTimeLimitedFunctionPlanData) functionPlanDataManager.functionPlanData(player.getFunctionPlanData(), PlanFunction.DRAW_CARD, plan.getId(), true);
            if (CheckNull.isNull(planData))
                return;
            // 若任务还未完成 则更新
            if (planData.getReceiveStatus() != FunctionPlanData.CANNOT_RECEIVE_STATUS) {
                return;
            }
            planData.operationProgress((int) params[0]);
            if (planData.getProgress() >= HeroConstant.TIME_LIMITED_DRAW_DEFEATED_REBELS_NUM_AND_FREE_TIMES.get(0)) {
                planData.updateReceiveStatus(FunctionPlanData.CAN_RECEIVE_STATUS);
            }
            drawCardPlanTemplateService.syncChangeDrawCardActPlan(player, planData, plan, ACT_UPDATE, now);
        });
    }
}
