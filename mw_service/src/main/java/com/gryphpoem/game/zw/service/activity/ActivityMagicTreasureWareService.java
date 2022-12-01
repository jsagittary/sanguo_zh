package com.gryphpoem.game.zw.service.activity;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.manager.ActivityDataManager;
import com.gryphpoem.game.zw.manager.GlobalDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb3;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.ActTurnplat;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.ActRank;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.GameGlobal;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.LogLordHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.GmCmd;
import com.gryphpoem.game.zw.service.GmCmdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * activity:statusMap 领取箱子次数; propMap 计算转盘次数; statusCnt 排行榜积分; saveMap 礼包购买次数
 */
@Service
public class ActivityMagicTreasureWareService extends AbsSimpleActivityService implements AbsTurnPlatActivityService, AbsRankActivityService, AbsGiftBagActivityService, GmCmdService {

    @Override
    public boolean functionOpen(Player player, int actType) {
        return StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.ACT_MAGIC_TREASURE_WARE);
    }

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private ServerSetting serverSetting;

    @Override
    protected GeneratedMessage.Builder<GamePb4.GetActivityDataInfoRs.Builder> getActivityData(Player player, Activity activity, GlobalActivityData globalActivityData) throws MwException {
        if (CheckNull.isNull(player) || CheckNull.isNull(activity) || CheckNull.isNull(globalActivityData)) {
            LogUtil.error("activity data is empty, actType : ", Arrays.toString(getActivityType()));
            return null;
        }

        if (!functionOpen(player, false)) {
            return null;
        }
        CommonPb.ActTurnPlatInfo.Builder actTurnPlatInfoPb = getTurnPlatPb(player, activity);
        CommonPb.ActRankInfo.Builder actRankInfoPb = getActRank(player.lord.getLordId(), activity.getActivityType());
        CommonPb.ActGiftBagInfo.Builder actGiftBagInfoPb = getActGiftBagInfoPb(player, activity);

        GamePb4.GetActivityDataInfoRs.Builder builder = GamePb4.GetActivityDataInfoRs.newBuilder();
        CommonPb.MagicTreasureWareAct.Builder magicTwAct = CommonPb.MagicTreasureWareAct.newBuilder();
        if (Objects.nonNull(actGiftBagInfoPb))
            magicTwAct.setBagInfo(actGiftBagInfoPb.build());
        if (Objects.nonNull(actRankInfoPb))
            magicTwAct.setRankInfo(actRankInfoPb.build());
        if (Objects.nonNull(actTurnPlatInfoPb))
            magicTwAct.setTurnPlatInfo(actTurnPlatInfoPb.build());
        builder.setMagicTwAct(magicTwAct.build());
        return builder;
    }

    @Override
    public CommonPb.ActGiftBagInfo.Builder getActGiftBagInfoPb(Player player, Activity activity) {
        if (CheckNull.isNull(player) || CheckNull.isNull(activity))
            return null;

        CommonPb.ActGiftBagInfo.Builder builder = CommonPb.ActGiftBagInfo.newBuilder();
        activity.getSaveMap().forEach((keyId, cnt) -> builder.addStatusCnt(PbHelper.createTwoIntPb(keyId, cnt)));
        return builder;
    }

    /**
     * 抽取转盘奖励
     *
     * @param roleId
     * @param id
     * @return
     * @throws MwException
     */
    public GamePb5.DrawTwTurntableAwardRs drawTurntableAward(long roleId, int id) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        functionOpen(player, true);
        StaticTurnplateConf turntableConf = getTurntableConf(id, roleId);
        int activityType = turntableConf.getType();

        ActTurnplat turntable = getActTurntable(activityType, roleId, player);
        checkDrawCount(turntable, ActivityConst.LUCKY_TURNTABLE_ACT_EXCLUSIVE_TIMES, turntableConf, player);
        ChangeInfo change = ChangeInfo.newIns();// 记录玩家资源变更类型
        List<CommonPb.Award> awardList = sendTurntableAward(turntableConf, turntable, player, change);
        // 通知玩家消耗的资源类型
        rewardDataManager.syncRoleResChanged(player, change);

        GamePb5.DrawTwTurntableAwardRs.Builder builder = GamePb5.DrawTwTurntableAwardRs.newBuilder();
        CommonPb.DrawTurntableAwardResult.Builder resultPb = CommonPb.DrawTurntableAwardResult.newBuilder();
        resultPb.setFreeCount(turntable.getRefreshCount());
        resultPb.setCnt(turntable.getCnt());
        if (CheckNull.nonEmpty(awardList))
            resultPb.addAllAward(awardList);
        builder.setResult(resultPb.build());
        return builder.build();
    }

    /**
     * 领取转盘次数箱子奖励
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GamePb5.ReceiveMtwTurntableCntAwardRs receiveMtwTurntableCntAward(long roleId, GamePb5.ReceiveMtwTurntableCntAwardRq req) throws MwException {
        functionOpen(playerDataManager.checkPlayerIsExist(roleId), true);
        CommonPb.ReceiveTurntableCntAwardResult.Builder resultPb = receiveCntAward(roleId, req.getActType(), req.getKeyId());
        return GamePb5.ReceiveMtwTurntableCntAwardRs.newBuilder().setRs(resultPb.build()).build();
    }

    @Override
    protected int[] getActivityType() {
        return new int[]{ActivityConst.ACT_MAGIC_TREASURE_WARE};
    }

    @Override
    public List<StaticActAward> getStaticActAwardList(int actId, int activityType, long roleId) {
        List<StaticActAward> sActAward = StaticActivityDataMgr.getRankActAwardByActId(actId);
        if (CheckNull.isEmpty(sActAward)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "排行活动未开启配置错误 roleId:", roleId, ", actId:", actId,
                    ", actType:", activityType);
        }

        return sActAward;
    }

    @Override
    public void addShowRankList(CommonPb.ActRankInfo.Builder builder, List<ActRank> showRankList) {
        if (CheckNull.isEmpty(showRankList)) {
            return;
        }

        for (ActRank ar : showRankList) {
            Player p = playerDataManager.getPlayer(ar.getLordId());
            if (p == null) {
                continue;
            }
            builder.addActRank(PbHelper.createActRank(ar, p.lord.getNick(), p.lord.getCamp(), p.lord.getPortrait(), p.getDressUp().getCurPortraitFrame()));
        }
    }

    @Override
    public void addExtParam(GlobalActivityData gActDate) {
    }

    @Override
    public void loadRank(ActivityBase e, GlobalActivityData gAct) {
        if (e.getStep0() == ActivityConst.OPEN_CLOSE) {
            return;
        }
        int actType = gAct.getActivityType();
        for (Player player : playerDataManager.getPlayers().values()) {
            ActTurnplat act = getActTurntable(actType, player.lord.getLordId(), player);
            if (act == null) continue;
            Long value = act.getStatusCnt().get(ActivityMagicTwConst.RANK_SCHEDULE_INDEX); // 个人的排行榜进度或存在 0 的位置
            Long time = act.getStatusCnt().get(ActivityMagicTwConst.RANK_UPDATE_TIME_INDEX);
            if (value != null && time != null) {
                int timeInt = time == null ? 0 : time.intValue();
                gAct.addPlayerRank(player, value, actType, timeInt); // 添加玩家
            }
        }
    }

    @Override
    public void sendSettleRankAward(Player player, int now, Activity activity) {
        if (activity == null) {
            LogUtil.debug(String.format("player:%d, not join this activity", player.lord.getLordId()));
            return;
        }
        Map<Integer, GlobalActivityData> activityMap = activityDataManager.getActivityMap();
        if (activityMap == null) {
            LogUtil.debug(String.format("player:%d, not have any global activity data", player.lord.getLordId()));
            return;
        }
        GlobalActivityData gActData = activityMap.get(activity.getActivityType());
        if (gActData == null) { // 该排行榜没有数据
            LogUtil.debug(String.format("player:%d, not have this global activity data, actType:%d", player.lord.getLordId(), activity.getActivityType()));
            return;
        }
        if (activity.getStatusCnt().isEmpty()) {// 没有积分信息
            LogUtil.debug(String.format("player:%d, not join this activity", player.lord.getLordId()));
            return;
        }
        int actType = activity.getActivityType();
        ActRank rank = gActData.getPlayerRank(player, actType, player.roleId);
        if (rank == null) {
            LogUtil.debug(String.format("player:%d, not add this activity rank", player.lord.getLordId()));
            return;
        }

        int rankAwardSchedule = rank.getRank();
        // 按照档次发奖励的排行
        StaticActAward myAward = StaticActivityDataMgr.findRankAward(activity.getActivityId(), rank.getRank());
        if (myAward == null) {
            LogUtil.debug(String.format("player:%d, config error, not have this actRank award, rank:%d", player.lord.getLordId(), rankAwardSchedule));
            return;
        }
        mailDataManager.sendAttachMail(player, PbHelper.createAwardsPb(myAward.getAwardList()), MailConstant.WORLD_WAR_PERSONAL_RANK_REWARD,
                AwardFrom.SEND_END_MAGIC_TREASURE_WARE_ACT_AWARD, now, actType, actType, rankAwardSchedule);
    }

    @Override
    public void addCntStatus(ActTurnplat turntable, CommonPb.ActTurnPlatInfo.Builder builder) {
        List<StaticTurnplateExtra> sExtrasConf = StaticActivityDataMgr.getActTurnplateExtraByActId(turntable.getActivityId());
        if (!CheckNull.isEmpty(sExtrasConf)) {
            for (StaticTurnplateExtra sExtra : sExtrasConf) {
                int id = sExtra.getId();
                builder.addStatus(PbHelper.createTwoIntPb(id, turntable.getStatusMap().getOrDefault(id, 0)));
            }

            builder.setCnt(turntable.getCnt());
        }
    }

    @Override
    public void updateActDrawCount(Player player, long progress, Object... param) {
        for (int actType : getActivityType()) {
            try {
                switch (actType) {
                    case ActivityConst.ACT_MAGIC_TREASURE_WARE:
                        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(actType);
                        if (CheckNull.isNull(activityBase)) {
                            break;
                        }
                        ActTurnplat turntable = (ActTurnplat) activityDataManager.getActivityInfo(player, actType);
                        if (CheckNull.isNull(turntable)) {
                            break;
                        }
                        int quality = Integer.parseInt(param[0].toString());
                        for (List<Integer> list : ActParamConstant.ACT_MAGIC_TREASURE_WARE_TURNTABLE_CNT) {
                            if (list.get(0) == quality) {
                                turntable.getPropMap().merge(quality, (int) progress, Integer::sum);
                                int remain = turntable.getPropMap().getOrDefault(quality, 0);
                                int count = remain / list.get(1);
                                if (count > 0) {
                                    turntable.setRefreshCount(turntable.getRefreshCount() + count * list.get(2));
                                    turntable.getPropMap().put(quality, remain % list.get(1));
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                LogUtil.error("", e);
                continue;
            }
        }
    }

    @Override
    public List<List<Integer>> getPropIdsInChat() {
        return ActParamConstant.ACT_MAGIC_PROP_IDS_IN_CHAT;
    }

    @Override
    public void upActRankSchedule(Player player, long progress, Object... param) {
        long now = System.currentTimeMillis();
        Long schedule = null;
        for (int actType : getActivityType()) {
            try {
                switch (actType) {
                    case ActivityConst.ACT_MAGIC_TREASURE_WARE:
                        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(actType);
                        if (CheckNull.isNull(activityBase)) {
                            break;
                        }
                        ActTurnplat turntable = (ActTurnplat) activityDataManager.getActivityInfo(player, actType);
                        if (CheckNull.isNull(turntable)) {
                            break;
                        }
                        int quality = Integer.parseInt(param[0].toString());
                        for (List<Integer> list : ActParamConstant.ACT_MAGIC_TREASURE_WARE_RANK_SCORE) {
                            if (list.get(0) == quality) {
                                long progress_ = list.get(1) * progress;
                                turntable.getStatusCnt().merge(ActivityMagicTwConst.RANK_SCHEDULE_INDEX, progress_, Long::sum);
                                schedule = turntable.getStatusCnt().get(ActivityMagicTwConst.RANK_SCHEDULE_INDEX);
                                turntable.getStatusCnt().put(ActivityMagicTwConst.RANK_UPDATE_TIME_INDEX, now);
                            }
                        }
                        break;
                    default:
                        break;
                }

                if (Objects.nonNull(schedule)) {
                    GlobalActivityData usualActivityData = getGlobalActivity(actType);
                    if (Objects.nonNull(usualActivityData))
                        usualActivityData.addPlayerRank(player, schedule, actType, (int) (now / TimeHelper.SECOND_MS));
                }
            } catch (Exception e) {
                LogUtil.error("", e);
                continue;
            } finally {
                schedule = null;
            }

        }
    }

    @Override
    public boolean checkDrawCount(ActTurnplat turntable, int costType, StaticTurnplateConf turntableConf, Player player) {
        if (turntable.getRefreshCount() < turntableConf.getCount()) {
            throw new MwException(GameError.ACT_LUCKY_TURNPLATE_MAX_COUNT.getCode(), " 转盘免费抽奖, 次数不足 roleId:",
                    player.lord.getLordId(), ", cnt: ", turntable.getRefreshCount());
        }
        return true;
    }

    @Override
    public int getChatId() {
        return ChatConst.CHAT_FAMOUS_GENERAL_TURNPLATE_GLOBAL_NUM;
    }

    @Override
    public int getChatConstId() {
        return ChatConst.CHAT_FAMOUS_GENERAL_TURNPLATE_GLOBAL_NUM;
    }

    @Override
    protected void handleOnEndTime(int activityType, int activityId, int keyId) {
        GameGlobal gameGlobal;
        if (Objects.nonNull(gameGlobal = DataResource.ac.getBean(GlobalDataManager.class).
                getGameGlobal()) && gameGlobal.removedActData.contains(keyId)) {
            LogUtil.error("神兵宝具活动结束, 结算被屏蔽掉的keyId: ", keyId);
            return;
        }

        //未领取的奖励发放
        Collection<Player> joinPlayers = playerDataManager.getAllPlayer().values();
        if (ObjectUtils.isEmpty(joinPlayers)) {
            return;
        }

        LogUtil.common("starting settle magic treasureWare rank data===");
        List<StaticTurnplateExtra> sExtrasConf = StaticActivityDataMgr.getActTurnplateExtraByActId(activityId);
        if (CheckNull.nonEmpty(sExtrasConf)) {
            int now = TimeHelper.getCurrentSecond();
            List<List<Integer>> awardList = new ArrayList<>();
            joinPlayers.forEach(player -> {
                Activity activity = player.activitys.get(activityType);
                if (CheckNull.isNull(activity)) {
                    return;
                }

                ActTurnplat actTurnplat = (ActTurnplat) activity;
                if (actTurnplat.getCnt() > 0) {
                    sExtrasConf.forEach(extra -> {
                        if (actTurnplat.getStatusMap().getOrDefault(extra.getId(), 0) > 0 ||
                                actTurnplat.getCnt() < extra.getTimes())
                            return;

                        awardList.addAll(extra.getAwardList());
                    });

                    if (CheckNull.nonEmpty(awardList))
                        mailDataManager.sendAttachMail(player, PbHelper.createAwardsPb(awardList), MailConstant.MOLD_ACT_UNREWARDED_REWARD,
                                AwardFrom.SEND_END_MAGIC_TREASURE_WARE_ACT_AWARD, now, activityType, activityId, activityType, activityId);
                }
                //排行榜奖励
                sendSettleRankAward(player, now, activity);
                awardList.clear();
            });
        }
        LogUtil.common("end settle magic treasureWare rank data===");
    }

    @Override
    public void processGiftBag(Player player, StaticPay sPay) {
        StaticPromotion staticPromotion;
        for (int actType : getActivityType()) {
            boolean sendAward = false;
            try {
                List<StaticPromotion> promotionList = StaticActivityDataMgr.getStaticPromotionListByType(actType);
                if (CheckNull.isEmpty(promotionList)) {
                    throw new MwException(GameError.NO_CONFIG.getCode(),
                            "没有这个活动的礼包, roleId:", player.roleId, " actType:", actType);
                }
                staticPromotion = promotionList.stream().filter(promotion -> promotion.getPayId() == sPay.getPayId()).findFirst().orElse(null);
                if (CheckNull.isNull(staticPromotion)) {
                    throw new MwException(GameError.NO_CONFIG.getCode(),
                            "没有活动的payId的礼包, roleId:", player.roleId, " actType:", actType, ", payId: ", sPay.getPayId());
                }

                Activity activity = getGiftBagAct(actType, staticPromotion, player);
                checkBuyCount(activity, staticPromotion, player);

                // 直接同步奖励
                List<CommonPb.Award> awardList = rewardDataManager.sendReward(player, staticPromotion.getList(),
                        AwardFrom.BUY_MAGIC_TREASURE_WARE_GIFT_BAG, serverSetting.getServerID(), activity.getActivityKeyId(), staticPromotion.getPromotionId());
                sendAward = true;
                mailDataManager.sendReportMail(player, null, MailConstant.ACT_MAGIC_TREASURE_WARE_GIFT_BAG, awardList,
                        TimeHelper.getCurrentSecond(), staticPromotion.getPromotionId(), staticPromotion.getPromotionId());
                activity.getSaveMap().merge(staticPromotion.getPromotionId(), 1, Integer::sum);
            } catch (Exception e) {
                LogUtil.error("", e);
                if (!sendAward)
                    rewardDataManager.sendRewardSignle(player, AwardType.MONEY, AwardType.Money.GOLD, sPay.getPrice() * 10, AwardFrom.BUY_MAGIC_TREASURE_WARE_GIFT_BAG);
                continue;
            }
        }
    }

    @Override
    public GamePb3.PromotionPropBuyRs processActGiftBagByGameMoney(Player player, StaticPromotion promotion) {
        if (CheckNull.isNull(player) || CheckNull.isNull(promotion)) {
            LogUtil.error("processActGiftBagByGameMoney player is null or promotion is null");
            return null;
        }

        for (int actType : getActivityType()) {
            if (promotion.getType() == actType) {
                Activity activity = getGiftBagAct(actType, promotion, player);
                checkBuyCount(activity, promotion, player);
                // 检查背包
                int keyId = promotion.getPromotionId();
                rewardDataManager.checkBag(player, promotion.getList());
                rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD,
                        promotion.getPrice(), AwardFrom.BUY_MAGIC_TREASURE_WARE_GIFT_BAG, keyId);// 购买打折礼包

                activity.getSaveMap().merge(promotion.getPromotionId(), 1, Integer::sum);// 更新礼包的购买次数

                LogLordHelper.gift(AwardFrom.BUY_MAGIC_TREASURE_WARE_GIFT_BAG, player.account, player.lord, promotion.getPrice(),
                        promotion.getPromotionId(), activity.getActivityType());

                GamePb3.PromotionPropBuyRs.Builder builder = GamePb3.PromotionPropBuyRs.newBuilder();
                builder.addAllAward(rewardDataManager.addAwardDelaySync(player, promotion.getList(), null,
                        AwardFrom.BUY_MAGIC_TREASURE_WARE_GIFT_BAG, serverSetting.getServerID(), activity.getActivityKeyId(), keyId));
                builder.setGold(player.lord.getGold());
                return builder.build();
            }
        }

        return null;
    }

    @Override
    public void checkBuyCount(Activity activity, StaticPromotion promotion, Player player) {
        int keyId = promotion.getPromotionId();
        long count = activity.getSaveMap().getOrDefault(keyId, 0);
        if (promotion.getCount() > 0 && count >= promotion.getCount()) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId :%d, keyId :%d, 已达购买次数上限 [%d / %d] ",
                    player.getLordId(), keyId, count, promotion.getCount()));
        }
    }

    /**
     * 活动是否开启
     *
     * @param player
     * @param sendError
     * @return
     */
    public boolean functionOpen(Player player, boolean sendError) {
        try {
            if (!StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.ACT_MAGIC_TREASURE_WARE)) {
                throw new MwException(GameError.FUNCTION_LOCK.getCode(), "function未解锁, roleId:", player.roleId);
            }
        } catch (MwException e) {
            if (sendError)
                throw e;
            return false;
        }
        return true;
    }

    @GmCmd("magicTw")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {
        String cmd = params[0];
        if ("addRank".equalsIgnoreCase(cmd)) {
            int now = TimeHelper.getCurrentSecond();
            playerDataManager.getAllPlayer().values().stream().limit(Long.parseLong(params[2])).collect(Collectors.toList()).forEach(p -> {
                long progress = RandomHelper.randomInSize(10000) + 10000;
                Activity activity = activityDataManager.getActivityInfo(p, Integer.parseInt(params[1]));
                activity.getStatusCnt().merge(ActivityMagicTwConst.RANK_SCHEDULE_INDEX, progress, Long::sum);
                GlobalActivityData usualActivityData = getGlobalActivity(Integer.parseInt(params[1]));
                activity.getStatusCnt().put(ActivityMagicTwConst.RANK_UPDATE_TIME_INDEX, now * 1000l);
                if (Objects.nonNull(usualActivityData))
                    usualActivityData.addPlayerRank(p, activity.getStatusCnt().get(ActivityMagicTwConst.RANK_SCHEDULE_INDEX), Integer.parseInt(params[1]), (int) (now / TimeHelper.SECOND_MS));
            });
        }
        if ("addCount".equalsIgnoreCase(cmd)) {
            ActTurnplat actTurnplat = getActTurntable(ActivityConst.ACT_MAGIC_TREASURE_WARE, player.lord.getLordId(), player);
            if (CheckNull.isNull(actTurnplat))
                return;

            actTurnplat.setRefreshCount(actTurnplat.getRefreshCount() + Integer.parseInt(params[1]));
        }
    }
}
