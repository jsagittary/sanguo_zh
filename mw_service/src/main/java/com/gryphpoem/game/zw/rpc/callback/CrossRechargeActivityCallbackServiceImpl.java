package com.gryphpoem.game.zw.rpc.callback;

import com.gryphpoem.cross.activity.CrossRechargeActivityCallbackService;
import com.gryphpoem.cross.activity.CrossRechargeActivityService;
import com.gryphpoem.cross.activity.dto.PlayerRechargeActivityDto;
import com.gryphpoem.cross.player.RpcPlayerService;
import com.gryphpoem.cross.player.dto.PlayerLordDto;
import com.gryphpoem.game.zw.core.util.Java8Utils;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.manager.ChatDataManager;
import com.gryphpoem.game.zw.manager.MailDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.ActivityTaskType;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.ChatConst;
import com.gryphpoem.game.zw.resource.constant.MailConstant;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticActAward;
import com.gryphpoem.game.zw.resource.domain.s.StaticActivityPlan;
import com.gryphpoem.game.zw.resource.util.*;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.Method;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-08-17 15:53
 */
@Service
public class CrossRechargeActivityCallbackServiceImpl implements CrossRechargeActivityCallbackService {

    @Autowired
    private ServerSetting serverSetting;
    @Autowired
    private PlayerDataManager playerDataManager;
    @DubboReference(check = false, lazy = true, cluster = "failfast")
    private CrossRechargeActivityService crossRechargeActivityService;
    @DubboReference(check = false, lazy = true, cluster = "failfast",
            methods = {
                    @Method(name = "asyncUpdatePlayerLord", async = true, isReturn = false)
            })
    private RpcPlayerService rpcPlayerService;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private ChatDataManager chatDataManager;

    /**
     * 注意此功能因为特殊性所以不需要考虑并发问题
     *
     * @param activityPlanId 活动ID
     * @param daily          第几天的结算0-总榜结算
     * @param lordId         榜首ID
     */
    public void sendDailySettlement(int activityPlanId, int daily, long lordId) {
        try {
            LogUtil.common(String.format("activityPlanId %d daily %d 榜首玩家 %d", activityPlanId, daily, lordId));
            int serverId = serverSetting.getServerID();
            ActivityBase base = ActivityUtil.getActivityBase(activityPlanId);
            if (Objects.isNull(base)) return;
            //活动配置错误
            StaticActivityPlan plan = base.getPlan();
            if (CheckNull.isEmpty(plan.getServerId())) {
                LogUtil.warn(String.format("activityPlanId: %d not config server list", activityPlanId));
                return;
            }
            //当前区服不在活动范围内
            if (!ListUtils.isInList(serverId, plan.getServerId())) {
                return;
            }

            PlayerLordDto lordDto = rpcPlayerService.getPlayerLord(lordId);
            if (Objects.nonNull(lordDto)) {
                int sysChatId = daily == 0 ? ChatConst.CHAT_CROSS_RECHARGE_SETTLE_TOTAL : ChatConst.CHAT_CROSS_RECHARGE_SETTLE_DAILY;
                chatDataManager.sendSysChat(sysChatId, 0, 0, lordDto.getOriginalServerId(), lordDto.getRoleName());
            } else {
                LogUtil.error(String.format("activityPlanId %d, 在角色服务器中未找到玩家 %d 的信息", activityPlanId, lordId));
            }
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

    @Override
    public void callbackActivityFinish(int activityPlanId) {
        try {
            int serverId = serverSetting.getServerID();
            LogUtil.common(String.format("serverId %d, 收到CrossServer回调游戏服, 活动 :%d 已经结束. ", serverId, activityPlanId));
            ActivityBase base = ActivityUtil.getActivityBase(activityPlanId);
            if (Objects.isNull(base)) {
                LogUtil.common(String.format("区服ID %d, 未找到活动 %d, 配置!!!", serverId, activityPlanId));
                return;
            }
            List<PlayerRechargeActivityDto> rtList = crossRechargeActivityService.getActivityEndSettle(activityPlanId, serverSetting.getServerID());
            Map<Long, PlayerRechargeActivityDto> dailyRankMap = new HashMap<>();
            Map<Long, PlayerRechargeActivityDto> totalRankMap = new HashMap<>();
            for (PlayerRechargeActivityDto dto : rtList) {
                if (dto.getBestDailyRank() > 0) {
                    totalRankMap.put(dto.getLordId(), dto);
                }
                if (dto.getTotalRank() > 0) {
                    dailyRankMap.put(dto.getLordId(), dto);
                }
            }
            List<StaticActAward> awardList = StaticActivityDataMgr.getActAwardById(base.getActivityId());
            Map<Integer, TreeMap<Integer, StaticActAward>> awardTypeMap = awardList.stream().collect(Java8Utils.groupByMapTreeMap(StaticActAward::getTaskType, StaticActAward::getKeyId));

            //发放日榜排名奖励
            if (!dailyRankMap.isEmpty()) {
                TreeMap<Integer, StaticActAward> awardTreeMap = awardTypeMap.get(ActivityTaskType.crossRechargeTotalRankType);
                if (CheckNull.nonEmpty(awardTreeMap)) {
                    Set<Long> alreadyAwardPlayer = sendRankAward(totalRankMap, awardTreeMap, PlayerRechargeActivityDto::getTotalRank,
                            AwardFrom.CROSS_RECHARGE_SEND_TOTAL_RANK_AWARD, MailConstant.MOLD_ACT_CROSS_RECHARGE_TOTAL);
                    crossRechargeActivityService.asyncPlayerDrawAwardFinish(activityPlanId, ActivityTaskType.crossRechargeTotalRankType, alreadyAwardPlayer);
                } else {
                    LogUtil.error(String.format("活动ID %d activityId %d 排名奖励类型 %d 未找到配置列表!!!", activityPlanId, base.getActivityId(), ActivityTaskType.crossRechargeTotalRankType));
                }
            }
            //发放总榜排名奖励
            if (!totalRankMap.isEmpty()) {
                TreeMap<Integer, StaticActAward> awardTreeMap = awardTypeMap.get(ActivityTaskType.crossRechargeDailyRankType);
                if (CheckNull.nonEmpty(awardTreeMap)) {
                    Set<Long> alreadyAwardPlayer = sendRankAward(totalRankMap, awardTreeMap, PlayerRechargeActivityDto::getBestDailyRank,
                            AwardFrom.CROSS_RECHARGE_SEND_DAILY_RANK_AWARD, MailConstant.MOLD_ACT_CROSS_RECHARGE_DAILY);
                    crossRechargeActivityService.asyncPlayerDrawAwardFinish(activityPlanId, ActivityTaskType.crossRechargeDailyRankType, alreadyAwardPlayer);
                } else {
                    LogUtil.error(String.format("活动ID %d activityId %d 排名奖励类型 %d 未找到配置列表!!!", activityPlanId, base.getActivityId(), ActivityTaskType.crossRechargeDailyRankType));
                }
            }
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

    /**
     * @param playerRankMap 玩家列表
     * @param awardTreeMap  奖励列表
     * @param fn            获取排名
     * @param from          AwardFrom
     * @param mailId        MailId
     * @return
     */
    private Set<Long> sendRankAward(Map<Long, PlayerRechargeActivityDto> playerRankMap, TreeMap<Integer, StaticActAward> awardTreeMap,
                                    Function<PlayerRechargeActivityDto, Integer> fn, AwardFrom from, int mailId) {
        Map<Integer, List<CommonPb.Award>> awardPbMap = new HashMap<>();
        for (Map.Entry<Integer, StaticActAward> entry : awardTreeMap.entrySet()) {
            List<CommonPb.Award> awardsPb = PbHelper.createAwardsPb(entry.getValue().getAwardList());
            awardPbMap.put(entry.getKey(), awardsPb);
        }
        int nowSec = TimeHelper.getCurrentSecond();
        Set<Long> alreadyPlayer = new HashSet<>();
        for (Map.Entry<Long, PlayerRechargeActivityDto> entry : playerRankMap.entrySet()) {
            PlayerRechargeActivityDto dto = entry.getValue();
            Player player = playerDataManager.getPlayer(dto.getLordId());
            if (Objects.nonNull(player)) {
                LogUtil.common(String.format("玩家 %d 累计充值金额 %d 总榜排名 %d  日榜第 %d 天最高名次为 %d",
                        dto.getLordId(), dto.getTotalRecharge(), dto.getTotalRank(), dto.getBestDailyRank(), dto.getBestDaily()));
            } else {
                LogUtil.common(String.format("本服 %d 没有找到玩家 %d !!!!", serverSetting.getServerID(), dto.getLordId()));
                continue;
            }
            for (Map.Entry<Integer, StaticActAward> awardEntry : awardTreeMap.entrySet()) {
                StaticActAward actAward = awardEntry.getValue();
                int rank = fn.apply(dto);
                int begin = actAward.getParam().get(0);
                int end = actAward.getParam().get(1);
                if (begin <= rank && rank <= end) {
                    List<CommonPb.Award> awardPb = awardPbMap.get(awardEntry.getKey());
                    if (CheckNull.nonEmpty(awardPb)) {
                        mailDataManager.sendAttachMail(player, awardPb, mailId, from, nowSec, rank);
                        alreadyPlayer.add(player.getLordId());
                    } else {
                        LogUtil.error(String.format("玩家 %d 排名 %d awardKeyId %d 没有找到奖励!!!", entry.getKey(), rank, actAward.getKeyId()));
                    }
                }
            }
        }
        return alreadyPlayer;
    }

}
