package com.gryphpoem.game.zw.service.activity.cross;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.cross.activity.CrossRechargeActivityService;
import com.gryphpoem.cross.activity.dto.ActivityRankDto;
import com.gryphpoem.cross.activity.dto.PlayerRechargeActivityDto;
import com.gryphpoem.cross.common.RankItemInt;
import com.gryphpoem.cross.player.RpcPlayerService;
import com.gryphpoem.cross.player.dto.PlayerLordDto;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.crosssimple.util.CrossPlayerPbHelper;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.pb.ActivityPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pb.GamePb6;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.GmCmdConst;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.rpc.DubboRpcService;
import com.gryphpoem.game.zw.service.GmCmd;
import com.gryphpoem.game.zw.service.GmCmdService;
import com.gryphpoem.game.zw.service.RechargeService;
import com.gryphpoem.game.zw.service.activity.AbsSimpleActivityService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-08-17 14:51
 */
@Service
public class CrossRechargeLocalActivityService extends AbsSimpleActivityService implements RechargeService, GmCmdService {

    @DubboReference(check = false, lazy = true, cluster = "failfast")
    private CrossRechargeActivityService crossRechargeActivityService;
    @DubboReference(check = false, lazy = true, cluster = "failfast",
            methods = {
                    @Method(name = "asyncUpdatePlayerLord", async = true, isReturn = false)
            })
    private RpcPlayerService rpcPlayerService;
    @Autowired
    private DubboRpcService dubboRpcService;

    @Override
    public int getActivityTips(ActivityBase base, Activity activity) {
        return 1;
    }

    @Override
    protected GeneratedMessage.Builder<GamePb4.GetActivityDataInfoRs.Builder> getActivityData(Player player, Activity activity, GlobalActivityData globalActivityData) throws Exception {
        ActivityBase base = checkAndGetActivityBase(player, activity.getActivityType());
        int step = base.getStep0();
        if (step != ActivityConst.OPEN_STEP && step != ActivityConst.OPEN_AWARD) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), String.format("lordId %d, 活动 %d, 未开始...", player.getLordId(), base.getPlanKeyId()));
        }

        CompletableFuture<PlayerRechargeActivityDto> future = crossRechargeActivityService.getPlayerRechargeActivity(player.getLordId(), base.getPlanKeyId());
        PlayerRechargeActivityDto dto = future.get();
        ActivityPb.CrossRechargeActivityPlayerData.Builder dataBuilder = ActivityPb.CrossRechargeActivityPlayerData.newBuilder();
        dataBuilder.setTotalRank(dto.getTotalRank())
                .setTotalRecharge(dto.getTotalRecharge())
                .setTodayRank(dto.getTodayRank())
                .setTodayRecharge(dto.getTodayRecharge())
                .setBestDailyRank(dto.getBestDailyRank())
                .setDays(dto.getDaily());
        GamePb4.GetActivityDataInfoRs.Builder rspBuilder = GamePb4.GetActivityDataInfoRs.newBuilder();
        rspBuilder.setCrossRecharge(dataBuilder);
        return rspBuilder;
    }

    /**
     * 获取跨服充值活动排名信息
     *
     * @param player
     * @param req
     * @return
     * @throws MwException
     */
    public GamePb6.GetCrossRechargeRankingRs getCrossRechargeRanking(Player player, GamePb6.GetCrossRechargeRankingRq req) throws Exception {
        int actType = req.getActType();
        ActivityBase base = checkAndGetActivityBase(player, actType);
        Date now = new Date();
        int rankType = req.getRankType();
        int daily = DateHelper.dayiy(base.getBeginTime(), now);
        if (rankType < 0 || rankType > daily)
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId %d, activityPlanId %d, daily %d not found rankType %d",
                    player.getLordId(), base.getPlanKeyId(), daily, rankType));
        CompletableFuture<ActivityRankDto> activityServiceFuture = crossRechargeActivityService.getActivityRanks(player.getLordId(), base.getPlanKeyId(), rankType);
        ActivityRankDto dto = activityServiceFuture.get();
        GamePb6.GetCrossRechargeRankingRs.Builder rsp = GamePb6.GetCrossRechargeRankingRs.newBuilder();
        if (CheckNull.nonEmpty(dto.getRanks())) {
            Set<Long> lordIdSet = dto.getRanks().stream().map(RankItemInt::getLordId).collect(Collectors.toSet());
            CompletableFuture<Map<Long, PlayerLordDto>> playerServiceFuture = rpcPlayerService.getPlayerLord(lordIdSet);
            Map<Long, PlayerLordDto> lordMap = playerServiceFuture.get(1, TimeUnit.SECONDS);
            for (RankItemInt rank : dto.getRanks()) {
                PlayerLordDto lordDto = lordMap.get(rank.getLordId());
                rsp.addItem(CrossPlayerPbHelper.buildCrossRankItem(lordDto, rank));
            }
        }
        rsp.setMyRank(CrossPlayerPbHelper.buildPlayerRankItem(player, dto.getMyRank()));
        return rsp.build();
    }

//    public GamePb5.GetCrossRechargeRankingRs getCrossRechargeRanking(Player player, GamePb5.GetCrossRechargeRankingRq req) throws MwException {
//        int actType = req.getActType();
//        ActivityBase base = checkAndGetActivityBase(player, actType);
//        Date now = new Date();
//        int rankType = req.getRankType();
//        int daily = DateHelper.dayiy(base.getBeginTime(), now);
//        if (rankType < 0 || rankType > daily)
//            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId %d, activityPlanId %d, daily %d not found rankType %d",
//                    player.getLordId(), base.getPlanKeyId(), daily, actType));
//        try {
//            CompletableFuture<ActivityRankDto> future = crossRechargeActivityService.getActivityRanks(player.getLordId(), base.getPlanKeyId(), rankType);
//            CompletableFuture<GamePb5.GetCrossRechargeRankingRs> rspFuture = future.thenApply(dto -> {
//                GamePb5.GetCrossRechargeRankingRs.Builder builder = GamePb5.GetCrossRechargeRankingRs.newBuilder();
//                if (CheckNull.nonEmpty(dto.getRanks())) {
//                    Set<Long> lordIdSet = dto.getRanks().stream().map(RankItemInt::getLordId).collect(Collectors.toSet());
//                    CompletableFuture<Map<Long, PlayerLordDto>> future2 = rpcPlayerService.getPlayerLord(lordIdSet);
//                    future2.thenAccept(lordMap -> {
//                        for (RankItemInt rank : dto.getRanks()) {
//                            PlayerLordDto lordDto = lordMap.get(rank.getLordId());
//                            builder.addItem(CrossPlayerPbHelper.buildCrossRankItem(lordDto, rank));
//                        }
//                    });
//                }
//                return builder.build();
//            });
//            return rspFuture.get(2, TimeUnit.SECONDS);
//        } catch (Exception e) {
//            LogUtil.error("", e);
//            throw new MwException(GameError.SERVER_EXCEPTION.getCode());
//        }
//    }

    @Override
    protected int[] getActivityType() {
        return new int[]{ActivityConst.CROSS_ACT_RECHARGE_RANK};
    }

    @Override
    public void afterRecharge(Player player, int amount, int diamond) {
        for (int activityType : getActivityType()) {
            ActivityBase base = StaticActivityDataMgr.getActivityByType(activityType);
            if (Objects.nonNull(base)) {
                if (ActivityConst.OPEN_STEP == base.getStep0()) {
                    try {
                        LogUtil.common(String.format("lordId: %d, 跨服活动: %d, 充值金额: %d, 充值钻石: %d, level: %d",
                                player.getLordId(), base.getPlanKeyId(), amount, diamond, player.lord.getLevel()));
                        dubboRpcService.updatePlayerLord2CrossPlayerServer(player);
                        crossRechargeActivityService.asyncUpdatePlayerActivityValue(player.getLordId(), base.getPlanKeyId(), amount, player.lord.getLevel());
                    } catch (Exception e) {
                        LogUtil.error("", e);
                    }
                }
            }
        }
    }

    @GmCmd(GmCmdConst.crossRechargeActivity)
    @Override
    public void handleGmCmd(Player player, String... params) {
        dubboRpcService.updatePlayerLord2CrossPlayerServer(player);
        String crossGmCmd = params[0];
        String[] cmdParams = Arrays.copyOfRange(params, 1, params.length);
        crossRechargeActivityService.handGmCmd(crossGmCmd, cmdParams);
    }
}
