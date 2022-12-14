package com.gryphpoem.game.zw.service.plan;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.QuartzHelper;
import com.gryphpoem.game.zw.dataMgr.StaticDrawHeroDataMgr;
import com.gryphpoem.game.zw.manager.ActivityDataManager;
import com.gryphpoem.game.zw.manager.FunctionPlanDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.ActivityPb;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.quartz.ScheduleManager;
import com.gryphpoem.game.zw.quartz.jobs.FunctionJob;
import com.gryphpoem.game.zw.quartz.jobs.function.FunctionEndJob;
import com.gryphpoem.game.zw.quartz.jobs.function.FunctionPreviewJob;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.constant.DrawCardOperation;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticDrawCardWeight;
import com.gryphpoem.game.zw.resource.domain.s.StaticDrawHeoPlan;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.plan.FunctionPlanData;
import com.gryphpoem.game.zw.resource.pojo.plan.FunctionTrigger;
import com.gryphpoem.game.zw.resource.pojo.plan.PlanFunction;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.Turple;
import com.gryphpoem.game.zw.service.PlayerService;
import com.gryphpoem.game.zw.service.plan.abs.AbsDrawCardPlanService;
import org.apache.commons.lang3.ArrayUtils;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-06-16 11:00
 */
@Component
public class DrawCardPlanTemplateService {
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private FunctionPlanDataManager functionPlanDataManager;
    @Autowired
    private List<AbsDrawCardPlanService> functionServiceList;
    @Autowired
    private StaticDrawHeroDataMgr staticDrawHeroDataMgr;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private ActivityDataManager activityDataManager;

    /**
     * ????????????plan??????
     *
     * @param roleId
     * @return
     */
    public GamePb5.GetDrawHeroCardPlanRs getDrawHeroCardPlanList(long roleId) {
        playerDataManager.checkPlayerIsExist(roleId);
        Date now = new Date();
        List<StaticDrawHeoPlan> planList = staticDrawHeroDataMgr.getPlanList(now, PlanFunction.PlanStatus.PREVIEW, PlanFunction.PlanStatus.OPEN);
        if (CheckNull.isEmpty(planList)) {
            return GamePb5.GetDrawHeroCardPlanRs.newBuilder().build();
        }
        return GamePb5.GetDrawHeroCardPlanRs.newBuilder().addAllPlanList(planList.stream().map(plan -> {
            CommonPb.DrawCardPlan.Builder builder = plan.createPb(false);
            StaticDrawCardWeight weightConfig = staticDrawHeroDataMgr.checkConfigEmpty(plan.getSearchTypeId());
            builder.setWeightId(CheckNull.isNull(weightConfig) ? 0 : weightConfig.getId());
            return builder.build();
        }).collect(Collectors.toList())).build();
    }

    /**
     * ??????????????????
     *
     * @param roleId
     * @param req
     * @return
     */
    public GamePb5.GetDrawHeroCardActInfoRs getDrawHeroCardActInfo(long roleId, GamePb5.GetDrawHeroCardActInfoRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        AbsDrawCardPlanService service = getFunctionService(req.getFunctionId());
        if (CheckNull.isNull(service)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId:%d, keyId:%d, function:%d", roleId, req.getKeyId(), req.getFunctionId()));
        }

        Turple<PlanFunction, StaticDrawHeoPlan> planFunction = service.checkAndGetPlan(req.getKeyId(), new Date(), player, PlanFunction.PlanStatus.OPEN, PlanFunction.PlanStatus.PREVIEW);
        FunctionPlanData functionPlanData = functionPlanDataManager.functionPlanData(player.getFunctionPlanData(), planFunction.getA(), planFunction.getB().getId(), true);
        if (CheckNull.isNull(functionPlanData)) {
            throw new MwException(GameError.PARAM_ERROR, String.format("roleId:%d, no player plan data, keyId:%d", player.lord.getLordId(), req.getKeyId()));
        }
        GamePb5.GetDrawHeroCardActInfoRs.Builder builder = GamePb5.GetDrawHeroCardActInfoRs.newBuilder();
        builder.setData((ActivityPb.TimeLimitedDrawCardActData) functionPlanData.createPb(false));
        return builder.build();
    }

    /**
     * ????????????
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GamePb5.DrawActHeroCardRs drawActHeroCard(long roleId, GamePb5.DrawActHeroCardRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        AbsDrawCardPlanService service = getFunctionService(req.getFunctionId());
        if (CheckNull.isNull(service)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId:%d, costType:%d, countType:%d", roleId, req.getCostType(), req.getCountType()));
        }
        Date now = new Date();
        Turple<PlanFunction, StaticDrawHeoPlan> planFunction = service.checkAndGetPlan(req.getKeyId(), now, player, PlanFunction.PlanStatus.OPEN);
        FunctionPlanData functionPlanData = functionPlanDataManager.functionPlanData(player.getFunctionPlanData(), planFunction.getA(), planFunction.getB().getId(), true);
        if (CheckNull.isNull(functionPlanData)) {
            throw new MwException(GameError.PARAM_ERROR, String.format("roleId:%d, no player plan data, keyId:%d", player.lord.getLordId(), req.getKeyId()));
        }

        StaticDrawCardWeight weightConfig = staticDrawHeroDataMgr.checkConfigEmpty(planFunction.getB().getSearchTypeId());
        if (CheckNull.isNull(weightConfig)) {
            throw new MwException(GameError.NO_CONFIG, String.format("roleId:%d, no plan config, keyId:%d", player.lord.getLordId(), req.getKeyId()));
        }
        DrawCardOperation.DrawCardCount drawCardCount = DrawCardOperation.DrawCardCount.convertTo(req.getCountType());
        DrawCardOperation.DrawCardCostType drawCardCostType = DrawCardOperation.DrawCardCostType.convertTo(req.getCostType());
        if (CheckNull.isNull(drawCardCount) || CheckNull.isNull(drawCardCostType)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId:%d, costType:%d, countType:%d", roleId, req.getCostType(), req.getCountType()));
        }

        // ????????????????????????????????????
        int costGoldCnt = 0;
        ChangeInfo change = ChangeInfo.newIns();// ??????????????????????????????
        switch (planFunction.getA()) {
            case DRAW_CARD:
                service.checkCondition(player, drawCardCostType, drawCardCount, change, functionPlanData, costGoldCnt);
                break;
            default:
                throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId:%d, costType:%d, countType:%d", roleId, req.getCostType(), req.getCountType()));
        }

        // ?????????????????????????????????
        rewardDataManager.syncRoleResChanged(player, change);
        GamePb5.DrawActHeroCardRs.Builder builder = GamePb5.DrawActHeroCardRs.newBuilder();
        // ????????????????????????
        for (int i = 0; i < drawCardCount.getCount(); i++) {
            CommonPb.SearchHero sh = service.onceDraw(player, functionPlanData, planFunction.getB(), costGoldCnt, drawCardCount, drawCardCostType, weightConfig, now);
            if (null != sh) {
                builder.addHero(sh);
            }
        }

        // ??????????????????-????????????
        activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_CUMULATIVE_RESIDENT_DRAW_CARD);
        builder.setData((ActivityPb.TimeLimitedDrawCardActData) functionPlanData.createPb(false));
        return builder.build();
    }

    /**
     * ????????????????????????
     *
     * @param player
     * @param data
     * @param plan
     * @param state
     * @param now
     */
    public void syncChangeDrawCardActPlan(Player player, FunctionPlanData data, StaticDrawHeoPlan plan, int state, Date now) {
        if (CheckNull.isNull(data) || CheckNull.isNull(player) || player.isRobot || !player.isLogin || player.ctx == null)
            return;

        GamePb5.SyncChangeDrawCardActPlanRs.Builder builder = GamePb5.SyncChangeDrawCardActPlanRs.newBuilder();
        builder.setState(state);
        if (Objects.nonNull(plan)) {
            CommonPb.DrawCardPlan.Builder drawPlanPb = plan.createPb(false);
            StaticDrawCardWeight weightConfig = staticDrawHeroDataMgr.checkConfigEmpty(plan.getSearchTypeId());
            drawPlanPb.setWeightId(CheckNull.isNull(weightConfig) ? 0 : weightConfig.getId());
            builder.setPlan(drawPlanPb);
        }
        if (Objects.nonNull(data))
            builder.setData((ActivityPb.TimeLimitedDrawCardActData) data.createPb(false));
        if (state == AbsDrawCardPlanService.ACT_DELETE) {
            // ???????????????, ????????????????????????
            List<StaticDrawHeoPlan> planList = staticDrawHeroDataMgr.getPlanList(now, PlanFunction.PlanStatus.OVER);
            if (CheckNull.nonEmpty(planList)) {
                builder.addAllSearchTypeIds(planList.stream().map(plan_ -> {
                    StaticDrawCardWeight staticData = staticDrawHeroDataMgr.checkConfigEmpty(plan_.getSearchTypeId());
                    if (CheckNull.isNull(staticData)) return 0;
                    return staticData.getId();
                }).filter(id -> id > 0).collect(Collectors.toList()));
            }
        }
        BasePb.Base.Builder basePb = PbHelper.createSynBase(GamePb5.SyncChangeDrawCardActPlanRs.EXT_FIELD_NUMBER, GamePb5.SyncChangeDrawCardActPlanRs.ext, builder.build());
        playerService.syncMsgToPlayer(basePb.build(), player);
    }

    /**
     * ????????????plan?????????
     */
    public void loadFunctionPlanJob() {
        Date now = new Date();
        List<StaticDrawHeoPlan> planList = staticDrawHeroDataMgr.getPlanList(now, PlanFunction.PlanStatus.NOT_START, PlanFunction.PlanStatus.PREVIEW, PlanFunction.PlanStatus.OPEN);
        if (CheckNull.isEmpty(planList))
            return;
        planList.forEach(plan -> addFunctionJob(plan, now));
    }

    /**
     * ?????????????????????
     *
     * @param staticDrawHeoPlan
     * @param now
     */
    private void addFunctionJob(StaticDrawHeoPlan staticDrawHeoPlan, Date now) {
        Scheduler scheduler = ScheduleManager.getInstance().getSched();
        String jobName = staticDrawHeoPlan.getFunctionId() + "_" + staticDrawHeoPlan.getId();
        if (staticDrawHeoPlan.getEndTime().after(now)) {
            QuartzHelper.removeJob(scheduler, jobName, FunctionJob.NAME_END);
            QuartzHelper.addJob(scheduler, jobName, FunctionJob.NAME_END, FunctionEndJob.class, staticDrawHeoPlan.getEndTime());
        }
        if (Objects.nonNull(staticDrawHeoPlan.getPreviewTime()) && staticDrawHeoPlan.getPreviewTime().after(now)) {
            QuartzHelper.removeJob(scheduler, jobName, FunctionJob.NAME_PREVIEW);
            QuartzHelper.addJob(scheduler, jobName, FunctionJob.NAME_PREVIEW, FunctionPreviewJob.class, staticDrawHeoPlan.getPreviewTime());
        }
    }

    /**
     * ???????????????????????????
     *
     * @param functionId
     * @param keyId
     */
    public void execFunctionPreview(int functionId, int keyId) {
        AbsDrawCardPlanService service = getFunctionService(functionId);
        if (Objects.nonNull(service)) {
            service.handleOnPreviewTime(keyId);
        }
    }

    /**
     * ??????????????????
     *
     * @param functionId
     * @param keyId
     */
    public void execFunctionBegin(int functionId, int keyId) {
        AbsDrawCardPlanService service = getFunctionService(functionId);
        if (Objects.nonNull(service)) {
            service.handleOnBeginTime(keyId);
        }
    }

    /**
     * ??????????????????
     *
     * @param functionId
     * @param planKeyId
     */
    public void execActivityEnd(int functionId, int planKeyId) {
        AbsDrawCardPlanService service = getFunctionService(functionId);
        if (Objects.nonNull(service)) {
            service.handleOnEndTime(planKeyId);
        }
    }

    /**
     * ??????function service
     *
     * @param functionId
     * @return
     */
    public AbsDrawCardPlanService getFunctionService(int functionId) {
        PlanFunction planFunction = PlanFunction.convertTo(functionId);
        if (CheckNull.isNull(planFunction))
            return null;

        for (AbsDrawCardPlanService service : functionServiceList) {
            PlanFunction[] functions = service.functionId();
            if (ArrayUtils.contains(functions, planFunction)) {
                return service;
            }
        }
        return null;
    }

    /**
     * ????????????
     *
     * @param player
     */
    public void execFunctionDay(Player player) {
        functionServiceList.forEach(service -> {
            service.handleOnDay(player);
        });
    }

    /**
     * ??????????????????
     *
     * @param player
     * @param params
     */
    public void updateFunctionData(Player player, FunctionTrigger functionTrigger, Object... params) {
        if (ObjectUtils.isEmpty(functionTrigger.getFunctions()))
            return;

        functionServiceList.forEach(service -> {
            if (ObjectUtils.isEmpty(service.functionId()))
                return;
            for (PlanFunction planFunction : service.functionId()) {
                if (!ArrayUtils.contains(functionTrigger.getFunctions(), planFunction))
                    continue;
                service.updateFunctionData(player, params);
            }
        });
    }
}
