package com.gryphpoem.game.zw.service.plan;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticDrawHeroDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.manager.ChatDataManager;
import com.gryphpoem.game.zw.manager.FunctionPlanDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.constant.ChatConst;
import com.gryphpoem.game.zw.resource.constant.DrawCardOperation;
import com.gryphpoem.game.zw.resource.constant.DrawCardRewardType;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.HeroConstant;
import com.gryphpoem.game.zw.resource.constant.LogParamConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticDrawCardWeight;
import com.gryphpoem.game.zw.resource.domain.s.StaticDrawHeoPlan;
import com.gryphpoem.game.zw.resource.domain.s.StaticHero;
import com.gryphpoem.game.zw.resource.domain.s.StaticHeroSearch;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.plan.FunctionPlanData;
import com.gryphpoem.game.zw.resource.pojo.plan.PlanFunction;
import com.gryphpoem.game.zw.resource.pojo.plan.PlayerFunctionPlanData;
import com.gryphpoem.game.zw.resource.pojo.plan.constant.FunctionPlanConstant;
import com.gryphpoem.game.zw.resource.pojo.plan.draw.DrawCardTimeLimitedFunctionPlanData;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.ListUtils;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.service.HeroService;
import com.gryphpoem.game.zw.service.plan.abs.AbsDrawCardPlanService;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-16 11:45
 */
@Component
public class TimeLimitedDrawCardFunctionService extends AbsDrawCardPlanService {

    @Autowired
    private DrawCardPlanTemplateService drawCardPlanTemplateService;
    @Autowired
    private FunctionPlanDataManager functionPlanDataManager;
    @Autowired
    private StaticDrawHeroDataMgr staticDrawHeroDataMgr;
    @Autowired
    private HeroService heroService;
    @Autowired
    private ChatDataManager chatDataManager;
    @Autowired
    private StaticDrawHeroDataMgr dataMgr;

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
        Collection<Player> allPlayers = playerDataManager.getAllPlayer().values();
        if (CheckNull.isEmpty(allPlayers))
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
        allPlayers.forEach(player -> {
            if (Objects.nonNull(player.ctx) && player.isLogin) {
                FunctionPlanData planData = functionPlanDataManager.functionPlanData(player.getFunctionPlanData(), planFunction, keyId, false);
                drawCardPlanTemplateService.syncChangeDrawCardActPlan(player, planData, staticPlan, ACT_DELETE, now);
            }
            FunctionPlanData functionPlanData = functionPlanDataManager.removeFunctionPlanData(player.getFunctionPlanData(), keyId);
            if (Objects.nonNull(functionPlanData)) {
                player.getFunctionPlanData().updateExtData(PlanFunction.DRAW_CARD, FunctionPlanConstant.TOTAL_DRAW_CARD_COUNT,
                        ((DrawCardTimeLimitedFunctionPlanData) functionPlanData).getTotalDrawCount());
            }
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
                params[4] = goldNum;
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

    @Override
    public CommonPb.SearchHero onceDraw(Player player, FunctionPlanData drawCardData, StaticDrawHeoPlan planData, int costCount, DrawCardOperation.DrawCardCount drawCardCount, DrawCardOperation.DrawCardCostType drawCardCostType, StaticDrawCardWeight config, Date now) throws MwException {
        // 记录随机到的奖励信息
        StaticHeroSearch shs = randomPriorityReward(player.roleId, drawCardData, planData, now, config, player.getFunctionPlanData());
        if (CheckNull.isNull(shs) || CheckNull.isEmpty(shs.getRewardList())) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "寻访配置错误   drawCardCount:", drawCardCount, ", drawCardCostType:", drawCardCostType);
        }

        DrawCardRewardType rewardType = DrawCardRewardType.convertTo(shs.getRewardType());
        if (CheckNull.isNull(rewardType)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "寻访配置错误   drawCardCount:", drawCardCount, ", drawCardCostType:", drawCardCostType, ", reward:", shs);
        }

        int heroLogId = 0;
        String awardLogStr = "";
        CommonPb.SearchHero.Builder builder = CommonPb.SearchHero.newBuilder().setSearchId(shs.getAutoId());
        switch (rewardType) {
            case ORANGE_HERO:
            case PURPLE_HERO:
                int heroId = heroLogId = shs.getRewardList().get(0).get(1);
                StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(heroId);
                if (null == staticHero) {
                    LogUtil.error("将领寻访，寻访到的将领未找到配置信息, heroId:", heroId);
                    return null;
                }

                // 查找玩家是否拥有此英雄
                Hero hero_ = heroService.hasOwnedHero(player, heroId, staticHero);
                if (Objects.nonNull(hero_)) {
                    heroLogId = 0;
                    // 拥有英雄转为碎片
                    rewardDataManager.sendRewardSignle(player, AwardType.HERO_FRAGMENT, heroId,
                            HeroConstant.DRAW_DUPLICATE_HERO_TO_TRANSFORM_FRAGMENTS, AwardFrom.HERO_NORMAL_SEARCH);
                    awardLogStr = AwardType.HERO_FRAGMENT + "," + heroId + "," + HeroConstant.DRAW_DUPLICATE_HERO_TO_TRANSFORM_FRAGMENTS;
                } else {
                    rewardDataManager.sendReward(player, shs.getRewardList(), AwardFrom.HERO_NORMAL_SEARCH);
                    // 返回新得到的将领信息
                    Hero hero = player.heros.get(staticHero.getHeroId());
                    builder.setHero(PbHelper.createHeroPb(hero, player));
                }

                if (staticHero.getQuality() == HeroConstant.QUALITY_ORANGE_HERO) {
                    chatDataManager.sendSysChat(ChatConst.CHAT_RECRUIT_HERO, player.lord.getCamp(), 0,
                            player.lord.getNick(), heroId);
                }
                break;
            case PROP_REWARD:
            case ORANGE_HERO_FRAGMENT:
            case PURPLE_HERO_FRAGMENT:
                rewardDataManager.sendReward(player, shs.getRewardList(), AwardFrom.HERO_NORMAL_SEARCH);
                awardLogStr = ListUtils.toString(shs.getRewardList());
                break;
        }

        int finalHeroLogId = heroLogId;
        String finalAwardLogStr = awardLogStr;
        DrawCardTimeLimitedFunctionPlanData functionPlanData = (DrawCardTimeLimitedFunctionPlanData) drawCardData;
        int totalDrawCount = functionPlanData.getTotalDrawHeroCount();
        LogUtil.getLogThread().addCommand(() -> LogLordHelper.gameLog(LogParamConstant.DRAW_HERO_CARD_LOG, player,
                AwardFrom.DRAW_HERO_CARD_NEW, drawCardCount.getType(), LogParamConstant.TIME_LIMITED_DRAW_CARD_TYPE,
                finalHeroLogId, finalAwardLogStr, costCount, totalDrawCount, functionPlanData.getKeyId()));
        return builder.build();
    }

    @Override
    public StaticHeroSearch randomPriorityReward(long roleId, FunctionPlanData functionPlanData, StaticDrawHeoPlan planData, Date now, StaticDrawCardWeight config, PlayerFunctionPlanData playerFunctionPlanData) throws MwException {
        StaticHeroSearch staticData;
        // 当前次数到必出武将次数
        DrawCardTimeLimitedFunctionPlanData drawCardData = (DrawCardTimeLimitedFunctionPlanData) functionPlanData;
        try {
            if (drawCardData.getHeroDrawCount() + 1 >= HeroConstant.DRAW_MINIMUM_NUMBER_OF_ORANGE_HERO) {
                drawCardData.clearHeroDrawCount();
                staticData = dataMgr.randomSpecifyType(config, DrawCardRewardType.ORANGE_HERO, now);
                return staticData;
            }
            // 碎片保底
            if (drawCardData.getFragmentDrawCount() + 1 >= HeroConstant.DRAW_ORANGE_HERO_FRAGMENT_GUARANTEED_TIMES) {
                drawCardData.clearFragmentDrawCount();
                drawCardData.addHeroDrawCount();
                staticData = dataMgr.randomGuaranteeHero(config, HeroConstant.DRAW_CARD_GUARANTEE_QUALITY_WEIGHT_OF_PURPLE_ORANGE, now);
                return staticData;
            }

            // 记录抽取次数
            drawCardData.addHeroDrawCount();
            drawCardData.addFragmentDrawCount();
            // 随机奖励
            staticData = dataMgr.randomReward(config, now);
            return staticData;
        } finally {
            drawCardData.addTotalDrawHeroCount(playerFunctionPlanData);
        }
    }

    /**
     * 武将限时寻访累计次数购买武将自选箱
     */
    public GamePb5.BuyOptionalBoxFromTimeLimitedDrawCardRs BuyOptionalBoxByDrawCardCount(long roleId, GamePb5.BuyOptionalBoxFromTimeLimitedDrawCardRq req) {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // 获取根据functionId，判断该活动是否配置并处于开启时间
        PlanFunction planFunction = PlanFunction.convertTo(req.getFunctionId());
        if (CheckNull.isNull(planFunction) || !ArrayUtils.contains(functionId(), planFunction)) {
            throw new MwException(GameError.PARAM_ERROR, String.format("roleId:%d, function:%d", roleId, req.getFunctionId()));
        }

        Date now = new Date();
        int keyId = req.getKeyId(); // 区分不同的限时活动：义薄云天、雄姿英发、拔矢啖睛等等。对应s_hero_search_plan的id
        checkAndGetPlan(keyId, now, player, PlanFunction.PlanStatus.OPEN);
        FunctionPlanData functionPlanData = functionPlanDataManager.functionPlanData(player.getFunctionPlanData(), planFunction, keyId, true);
        if (CheckNull.isNull(functionPlanData)) {
            throw new MwException(GameError.PARAM_ERROR, String.format("role:%d, function:%d, keyId:%d, no player function data", roleId, req.getFunctionId(), keyId));
        }

        // 确认保底自选宝箱配置
        if (CheckNull.isEmpty(HeroConstant.OPTIONAL_BOX_FROM_TIME_LIMITED_DRAW_CARD_CONFIG) || HeroConstant.OPTIONAL_BOX_FROM_TIME_LIMITED_DRAW_CARD_CONFIG.size() < 1) {
            throw new MwException(GameError.NO_CONFIG, String.format("roleId:%d, no config", roleId));
        }

        // 玩家限时抽卡活动详情
        DrawCardTimeLimitedFunctionPlanData drawCardTimeLimitedFunctionPlanData = (DrawCardTimeLimitedFunctionPlanData) functionPlanData;

        // 剩余抽卡次数
        int leftHeroDrawCountAfterBuyBox = drawCardTimeLimitedFunctionPlanData.getLeftHeroDrawCountAfterBuyBox();

        List<List<Integer>> optionalBoxConfigList = HeroConstant.OPTIONAL_BOX_FROM_TIME_LIMITED_DRAW_CARD_CONFIG;
        List<Integer> optionalBoxActiveCountConfig = HeroConstant.TIME_LIMITED_OPTIONAL_BOX_ACTIVE_COUNT_CONFIG; // [60,70,80,90,100,110,120]
        if (CheckNull.isEmpty(optionalBoxConfigList) || CheckNull.isEmpty(optionalBoxActiveCountConfig)) {
            throw new MwException(GameError.NO_CONFIG, String.format("roleId:%d, no config", roleId));
        }
        GamePb5.BuyOptionalBoxFromTimeLimitedDrawCardRs.Builder builder = GamePb5.BuyOptionalBoxFromTimeLimitedDrawCardRs.newBuilder();
        for (List<Integer> optionalBoxConfig : optionalBoxConfigList) {
            if (keyId == optionalBoxConfig.get(0)) {
                // 配置的可购买自选箱需要累计寻访的次数
                Integer needDrawCardCount = null;
                int curBuyCount = drawCardTimeLimitedFunctionPlanData.getOptionalBoxBuyCount();
                if (curBuyCount + 1 >= optionalBoxActiveCountConfig.size()) {
                    needDrawCardCount = optionalBoxActiveCountConfig.get(optionalBoxActiveCountConfig.size() - 1);
                } else {
                    needDrawCardCount = optionalBoxActiveCountConfig.get(curBuyCount);
                }

                if (leftHeroDrawCountAfterBuyBox < needDrawCardCount) {
                    throw new MwException(GameError.ACTIVITY_AWARD_NOT_GET, String.format("roleId:%d, not match condition", roleId));
                }
                // 配置的可购买自选箱id
                Integer optionalBoxId = optionalBoxConfig.get(1);
                // 配置的购买道具所需玉璧
                Integer optionalBoxPrice = optionalBoxConfig.get(2);
                // 检查购买宝箱需要的资源是否充足并扣减
                rewardDataManager.checkAndSubPlayerRes(player, AwardType.MONEY, AwardType.Money.GOLD, optionalBoxPrice, AwardFrom.ACTIVITY_BUY, true, "");
                // 更新剩余抽卡次数
                drawCardTimeLimitedFunctionPlanData.subLeftHeroDrawCountAfterBuyBox(needDrawCardCount);
                // 增加购买自选箱的次数
                drawCardTimeLimitedFunctionPlanData.addTotalOptionalBoxBuyCount();
                // 给玩家新增宝箱道具
                CommonPb.Award award = rewardDataManager.sendRewardSignle(player, AwardType.PROP, optionalBoxId, 1, AwardFrom.ACTIVITY_BUY, "");
                builder.addAward(award);
                break;
            }
        }

        return builder.build();
    }
}
