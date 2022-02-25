package com.gryphpoem.game.zw.gameplay.cross.serivce.warfire;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.cross.common.RankItemInt;
import com.gryphpoem.cross.common.RanksDto;
import com.gryphpoem.cross.gameplay.common.Game2CrossRequest;
import com.gryphpoem.cross.gameplay.warfire.g2c.dto.PlayerWarFireEvent;
import com.gryphpoem.cross.gameplay.warfire.g2c.dto.WarFireCampSummary;
import com.gryphpoem.cross.gameplay.warfire.g2c.dto.WarFireScoreRankRq;
import com.gryphpoem.cross.gameplay.warfire.g2c.service.Game2CrossWarFireService;
import com.gryphpoem.cross.player.RpcPlayerService;
import com.gryphpoem.cross.player.dto.PlayerLordDto;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.QuartzHelper;
import com.gryphpoem.game.zw.crosssimple.util.CrossPlayerPbHelper;
import com.gryphpoem.game.zw.crosssimple.util.PbCrossUtil;
import com.gryphpoem.game.zw.dataMgr.StaticCrossWarFireDataMgr;
import com.gryphpoem.game.zw.dataMgr.cross.StaticNewCrossDataMgr;
import com.gryphpoem.game.zw.gameplay.cross.serivce.CrossFunctionTemplateService;
import com.gryphpoem.game.zw.gameplay.cross.serivce.CrossGamePlayService;
import com.gryphpoem.game.zw.gameplay.local.constant.cross.CrossFunction;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb6;
import com.gryphpoem.game.zw.pb.GamePb6.*;
import com.gryphpoem.game.zw.quartz.jobs.CrossGamePlanJob;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.PlayerConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.CrossWarFireLocalData;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossGamePlayPlan;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossGroup;
import com.gryphpoem.game.zw.resource.domain.s.StaticRebelBuff;
import com.gryphpoem.game.zw.resource.domain.s.StaticWarFireBuffCross;
import com.gryphpoem.game.zw.resource.util.CalculateUtil;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.service.GmCmd;
import com.gryphpoem.game.zw.service.GmCmdService;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.gryphpoem.game.zw.gameplay.local.constant.cross.NewCrossConstant.*;


/**
 * 跨服战火积分调用
 *
 * @Description
 * @Author zhangdh
 * @Date 2021-12-20 16:46
 */
@Service
public class WarFireScoreLocalService extends CrossFunctionTemplateService implements GmCmdService {
    @Autowired
    private ServerSetting serverSetting;
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private Game2CrossWarFireService game2CrossWarFireService;
    @Autowired
    private RpcPlayerService rpcPlayerService;
    @Autowired
    private RewardDataManager rewardDataManager;

    /**
     * 刷新玩家当前信息, 注意此协议调用频繁[3-5]秒, 所以返回应该尽量简单
     *
     * @param player 玩家
     * @param req    req
     * @return 玩家信息
     */
    public CompletableFuture<RefreshGetCrossWarFirePlayerInfoRs> refreshPlayerInfo(Player player, RefreshGetCrossWarFirePlayerInfoRq req) {
        StaticCrossGamePlayPlan plan = getGamePlayPlan(player, req.getFunctionId());
        Game2CrossRequest crossRequest = new Game2CrossRequest(serverSetting.getServerID(), player.getCamp(), player.getLordId(), plan.getKeyId());
        return game2CrossWarFireService.refreshGetWarFireScore(crossRequest).thenApply(score -> {
            RefreshGetCrossWarFirePlayerInfoRs.Builder builder = RefreshGetCrossWarFirePlayerInfoRs.newBuilder();
            builder.setScore(score);
            return builder.build();
        });
    }

    /**
     * 购买跨服战火BUFF
     *
     * @param player 玩家
     * @param req req
     * @return 玩家BUFF信息
     */
    public BuyCrossWarFireBuffRs buyCrossWarFireBuff(Player player, BuyCrossWarFireBuffRq req) {
        StaticCrossGamePlayPlan plan = StaticNewCrossDataMgr.getNotOverPlan(player, req.getFunctionId());
        if (Objects.isNull(plan)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId: %d, functionId: %d, not found gamePlayPlan", player.getLordId(), req.getFunctionId()));
        }

        int stage = plan.getStage();
        long lordId = player.getLordId();
        if (stage != STAGE_DIS_PLAYER && stage != STAGE_RUNNING) {
            throw new MwException(GameError.ACTIVITY_TIME_ERROR, String.format("lordId: %d, buff 购买时间不对 stage: %d", lordId, stage));
        }
        //活动类型错误
        CrossFunction crossFunction = CrossFunction.convertTo(req.getFunctionId());
        if (Objects.isNull(crossFunction) || crossFunction != CrossFunction.CROSS_WAR_FIRE) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("lordId: %d, 活动类型: %d, 错误!!!", lordId, req.getFunctionId()));
        }

        //BUFF 类型错误
        int buffType = req.getBuffType();
        if (buffType < StaticWarFireBuffCross.BUFF_TYPE_ATTK || buffType > StaticWarFireBuffCross.BUFF_TYPE_RECOVER_ARMY) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId: ", player.getLordId(), " buffType: ", buffType, " error !!!");
        }

        CrossWarFireLocalData data = (CrossWarFireLocalData) player.crossPlayerLocalData.getCrossFunctionData(crossFunction, plan.getKeyId(), true);
        Map<Integer, Integer> buffMap = data.getBuffs();
        int buffLevel = buffMap.getOrDefault(buffType, 0);//当前BUFF等级

        //BUFF 买满了
        TreeMap<Integer, StaticWarFireBuffCross> levelTreeMap = StaticCrossWarFireDataMgr.getWarFireBuffs(buffType);
        if (CheckNull.isEmpty(levelTreeMap)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), String.format("roleId: %d, buffType: %d not found!!!!", lordId, buffType));
        }
        if (buffLevel >= levelTreeMap.lastKey()) {
            throw new MwException(GameError.WAR_FIRE_SHOP_BUY_UP_LIMIT.getCode(),
                    String.format("roleId: %d, buffType: %d 已达购买上限", lordId, buffType));
        }
        //BUFF 等级配置不存在
        int upLevel = buffLevel + 1;
        StaticWarFireBuffCross sBuff = StaticCrossWarFireDataMgr.getWarFireBuffByTypeLv(req.getBuffType(), upLevel);
        if (Objects.isNull(sBuff) || sBuff.getCost() <= 0) {
            throw new MwException(GameError.NO_CONFIG.getCode(), String.format("roleId: %d, buffType: %d lv: %d not found", lordId, buffType, upLevel));
        }
        // 扣钱
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, sBuff.getCost(),
                AwardFrom.CROSS_WAR_FIRE_BUY_BUFF, sBuff.getBuffId(), upLevel);
        buffMap.put(buffType, upLevel);

        // 通知跨服BUFF购买成功
        Game2CrossRequest crossRequest = new Game2CrossRequest(serverSetting.getServerID(), player.getCamp(), player.getLordId(), plan.getKeyId());
        crossRequest.setData(new int[]{buffType, upLevel});
        if (data.isInCross() && buffType != StaticRebelBuff.BUFF_TYPE_RECOVER_ARMY) {
            // 在打的期间重新计算战斗力进行同步
            CalculateUtil.reCalcAllHeroAttr(player);
        }
        //异步通知跨服更新BUFF信息
        game2CrossWarFireService.playerBuyBuffSuccess(crossRequest);
        BuyCrossWarFireBuffRs.Builder builder = BuyCrossWarFireBuffRs.newBuilder();
        builder.setBuff(PbHelper.createTwoIntPb(buffType, upLevel));
        return builder.build();
    }

    /**
     * 获取玩家的实时战况
     *
     * @param player 玩家信息
     * @param req    req
     * @return 玩家的实时战况
     */
    public CompletableFuture<GetCrossWarFirePlayerLiveRs> getPlayerLive(Player player, GetCrossWarFirePlayerLiveRq req) {
        StaticCrossGamePlayPlan plan = getGamePlayPlan(player, req.getFunctionId());
        Game2CrossRequest crossRequest = new Game2CrossRequest(serverSetting.getServerID(), player.getCamp(), player.getLordId(), plan.getKeyId());
        return game2CrossWarFireService.getPlayerWarFireLive(crossRequest).thenApply(dto -> {
            GetCrossWarFirePlayerLiveRs.Builder builder = GetCrossWarFirePlayerLiveRs.newBuilder();
            builder.setFirstBloodScore(dto.getFirstBloodScore());
            builder.setKilled(dto.getKilled());
            builder.setOutputMin(dto.getOutputMin());
            builder.setTotalScore(dto.getTotalScore());
            if (CheckNull.nonEmpty(dto.getEvents())) {
                for (PlayerWarFireEvent event : dto.getEvents()) {
                    CommonPb.WarFireEventPb.Builder pbb = CommonPb.WarFireEventPb.newBuilder();
                    pbb.setEnemyPos(event.getEnemyPos());
                    pbb.setEnemyHeroId(event.getEnemyHeroId());
                    if (!CheckNull.isNullTrim(event.getEnemyName())) {
                        pbb.setEnemyName(event.getEnemyName());
                    }
                    pbb.setPos(event.getPos());
                    pbb.setTime(event.getTime());
                    pbb.setEntityId(event.getEntityId());
                    pbb.setEty(event.getEty());
                    pbb.setHeroId(event.getHeroId());
                    pbb.setKilled(event.getKilled());
                    pbb.setLost(event.getLost());
                    pbb.setScore(event.getScore());
                    builder.addWfevt(pbb);
                }
            }
            return builder.build();
        });
    }

    /**
     * 获取据点占领信息
     *
     * @param player 玩家
     * @param req    请求
     * @return KEY: 据点ID, VALUE: 占领的势力ID
     */
    public CompletableFuture<GetCrossWarFireCityOccupyRs> getCityOccupy(Player player, GetCrossWarFireCityOccupyRq req) {
        StaticCrossGamePlayPlan plan = StaticNewCrossDataMgr.getNotOverPlan(player, req.getFunctionId());
        long lordId = player.getLordId();
        if (Objects.isNull(plan)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId: %d, functionId: %d, not found gamePlayPlan", lordId, req.getFunctionId()));
        }

        Game2CrossRequest crossRequest = new Game2CrossRequest(serverSetting.getServerID(), player.getCamp(), player.getLordId(), plan.getKeyId());
        return game2CrossWarFireService.getCityOccupy(crossRequest).thenApply(occupyMap -> {
            GetCrossWarFireCityOccupyRs.Builder rsp = GetCrossWarFireCityOccupyRs.newBuilder();
            for (Map.Entry<Integer, Integer> entry : occupyMap.entrySet()) {
                rsp.addCity(PbHelper.createTwoIntPb(entry.getKey(), entry.getValue()));
            }
            return rsp.build();
        });
    }

    /**
     * 每分钟获取一次阵营概要信息
     *
     * @param player 玩家
     * @param req    req
     * @return 所有阵营信息
     */
    public CompletableFuture<GetCrossWarFireCampSummaryRs> getAllCampSummary(Player player, GetCrossWarFireCampSummaryRq req) {
        StaticCrossGamePlayPlan plan = getGamePlayPlan(player, req.getFunctionId());
        Game2CrossRequest crossRequest = new Game2CrossRequest(serverSetting.getServerID(), player.getCamp(), player.getLordId(), plan.getKeyId());
        return game2CrossWarFireService.getAllCampSummary(crossRequest).thenApply(summaries -> {
            GetCrossWarFireCampSummaryRs.Builder rsp = GetCrossWarFireCampSummaryRs.newBuilder();
            for (WarFireCampSummary summary : summaries) {
                CommonPb.WarFireCampSummaryPb.Builder builder = CommonPb.WarFireCampSummaryPb.newBuilder();
                builder.setCamp(summary.getCamp());
                builder.setScore(summary.getScore());
                builder.setOutputMin(summary.getOutputMin());
                builder.setPlayerCount(summary.getPlayerCount());
                rsp.addCampSummary(builder);
            }
            return rsp.build();
        });
    }


    /**
     * 获取跨服战火排行榜信息
     *
     * @param player 玩家
     * @param req    排名请求
     * @return 排名结果
     */
    public CompletableFuture<GetCrossWarFireRanksRs> getCrossWarFireRanks(Player player, GetCrossWarFireRanksRq req) {
        StaticCrossGamePlayPlan plan = StaticNewCrossDataMgr.getNotOverPlan(player, req.getFunctionId());
        long lordId = player.getLordId();
        if (Objects.isNull(plan)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId: %d, functionId: %d, not found gamePlayPlan", lordId, req.getFunctionId()));
        }

        Game2CrossRequest crossRequest = new Game2CrossRequest(serverSetting.getServerID(), player.getCamp(), player.getLordId(), plan.getKeyId());
        crossRequest.setData(new WarFireScoreRankRq(plan.getKeyId(), req.getCrossCamp(), req.getPage()));
        return game2CrossWarFireService.getWarFireScoreRank(crossRequest).thenApply(rankDto -> {
            RanksDto campRank = rankDto.getCampRank();
            RanksDto roleRankDto = rankDto.getRoleRank();
            GetCrossWarFireRanksRs.Builder rsp = GetCrossWarFireRanksRs.newBuilder();
            //个人积分排名
            if (CheckNull.nonEmpty(roleRankDto.getRanks())) {
                Set<Long> lordIdSet = roleRankDto.getRanks().stream().map(RankItemInt::getLordId).collect(Collectors.toSet());
                CompletableFuture<Map<Long, PlayerLordDto>> completableFuture = rpcPlayerService.getPlayerLord(lordIdSet);
                try {
                    Map<Long, PlayerLordDto> lordMap = completableFuture.get(10, TimeUnit.SECONDS);
                    rsp.addAllItem(PbCrossUtil.buildRanks(roleRankDto.getRanks(), lordMap));
                } catch (Exception e) {
                    LogUtil.error("获取玩家信息失败!!!", e);
                }
            }
            //阵营排名
            if (Objects.nonNull(campRank.getRanks())) {
                for (RankItemInt rit : campRank.getRanks()) {
                    rsp.addCamps(PbHelper.createTwoIntPb((int) rit.getLordId(), rit.getRankValue()));
                }
            }
            //自己的排名
            if (Objects.nonNull(roleRankDto.getMyRank())) {
                CommonPb.CrossRankItem.Builder builder = CrossPlayerPbHelper.buildPlayerRankItem(player, roleRankDto.getMyRank());
                builder.setCamp(roleRankDto.getMyRank().getForces());
                rsp.setMyRank(builder);
            }
            return rsp.build();
        });
    }

    private StaticCrossGamePlayPlan getGamePlayPlan(Player player, int functionId) {
        StaticCrossGamePlayPlan plan = StaticNewCrossDataMgr.getOpenPlan(player, functionId);
        long lordId = player.getLordId();
        if (Objects.isNull(plan)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId: %d, functionId: %d, not found gamePlayPlan", lordId, functionId));
        }
        return plan;
    }

    @Override
    public GeneratedMessage.Builder<GetCrossPlayerDataRs.Builder> getCrossPlayerData(Player player, int functionId) {
        GamePb6.GetCrossPlayerDataRs.Builder builder = GamePb6.GetCrossPlayerDataRs.newBuilder();
        CrossFunction crossFunction = CrossFunction.convertTo(functionId);
        if (Objects.isNull(crossFunction)) return null;
        StaticCrossGamePlayPlan plan = StaticNewCrossDataMgr.getNotOverPlan(player, functionId);
        if (CheckNull.isNull(plan)) return null;

        StaticCrossGroup staticCrossGroup = StaticNewCrossDataMgr.getStaticCrossGroup(plan.getGroup());
        Optional.ofNullable(player.crossPlayerLocalData.getCrossFunctionData(crossFunction, plan.getKeyId(), false)).ifPresent(warFireLocalData -> {
            builder.setWarFireData(((CrossWarFireLocalData) warFireLocalData).createClientPb(staticCrossGroup.
                            getForceBySidAndCamp(serverSetting.getServerID(), player.lord.getCamp()),
                    player.getMixtureDataById(PlayerConstant.CROSS_WAR_FIRE_PRICE)));
        });
        return builder;
    }

    @Override
    protected int getFunctionId() {
        return CrossFunction.CROSS_WAR_FIRE.getFunctionId();
    }

    @Override
    protected void executeSchedule(int planKey, String jobName) {
        if (ObjectUtils.isEmpty(jobName)) {
            LogUtil.error("jobName is empty! planKey: ", planKey);
            return;
        }

        switch (jobName) {
            case CrossGamePlanJob.NAME_END:
                DataResource.ac.getBean(CrossGamePlayService.class).syncLeaveCross(planKey);
                break;
            case CrossGamePlanJob.NAME_DISPLAY:
                DataResource.ac.getBean(CrossGamePlayService.class).syncCrossPlanStage(planKey, STAGE_OVER, getFunctionId());
                break;
            case CrossGamePlanJob.NAME_DISPLAY_BEGIN:
                DataResource.ac.getBean(CrossGamePlayService.class).syncCrossPlanStage(planKey, STAGE_DIS_PLAYER, getFunctionId());
                break;
            default:
                LogUtil.error("unknown jobName: ", jobName);
                break;
        }
    }

    @Override
    protected void addSchedule(StaticCrossGamePlayPlan plan, Scheduler scheduler, Date now) {
        if (Objects.nonNull(plan.getDisplayBegin()) && now.before(plan.getDisplayBegin())) {
            QuartzHelper.addJob(scheduler, CrossGamePlanJob.NAME_DISPLAY_BEGIN + plan.getActivityType() + "_" + plan.getKeyId(),
                    CrossGamePlanJob.GROUP, CrossGamePlanJob.class, plan.getDisplayBegin());
        }
        if (Objects.nonNull(plan.getEndTime()) && now.before(plan.getEndTime())) {
            QuartzHelper.addJob(scheduler, CrossGamePlanJob.NAME_END + plan.getActivityType() + "_" + plan.getKeyId(),
                    CrossGamePlanJob.GROUP, CrossGamePlanJob.class, plan.getEndTime());
        }
        if (Objects.nonNull(plan.getDisplayTime()) && now.before(plan.getDisplayTime())) {
            QuartzHelper.addJob(scheduler, CrossGamePlanJob.NAME_DISPLAY + plan.getActivityType() + "_" + plan.getKeyId(),
                    CrossGamePlanJob.GROUP, CrossGamePlanJob.class, plan.getDisplayTime());
        }
    }

    @GmCmd("crossWarFire")
    @Override
    public void handleGmCmd(Player player, String... params)  {
        String cmd = params[0];
        if ("addScore".equalsIgnoreCase(cmd)) {
            player.addMixtureData(PlayerConstant.CROSS_WAR_FIRE_PRICE, Integer.parseInt(params[1]));
        }
    }
}
