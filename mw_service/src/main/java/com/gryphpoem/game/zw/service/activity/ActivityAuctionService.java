package com.gryphpoem.game.zw.service.activity;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.QuartzHelper;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.quartz.jobs.ActAuctionRoundJob;
import com.gryphpoem.game.zw.quartz.jobs.ActEndJob;
import com.gryphpoem.game.zw.quartz.jobs.ActOverJob;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.ActivityAuctionParam;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.p.ActivityAuction;
import com.gryphpoem.game.zw.resource.domain.p.ActivityAuctionConst;
import com.gryphpoem.game.zw.resource.domain.s.StaticActAuction;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityAuctionData;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityAuctionItem;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.PlayerService;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;

@Service
public class ActivityAuctionService extends AbsActivityService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private ChatDataManager chatDataManager;

    /**
     * 秋季拍卖活动
     */
    private static final int ACTIVITY_AUCTION = ActivityConst.ACT_AUCTION;

    /**
     * 获取玩家拍卖活动信息
     *
     * @param player
     * @return
     * @throws MwException
     */
    private Activity getActivityInfo(Player player) throws MwException {
        Activity activity = activityDataManager.getActivityInfo(player, ACTIVITY_AUCTION);
        if (CheckNull.isNull(activity)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 活动未开启(Player.Activity=null), roleId:,", player.lord.getLordId(), ", activityType:", ACTIVITY_AUCTION);
        }

        return activity;
    }

    /**
     * 获取拍卖品信息
     *
     * @return
     * @throws MwException
     */
    private static GlobalActivityData getGlobalActivity() throws MwException {
        GlobalActivityData globalActivityAuctionData = DataResource.ac.getBean(ActivityDataManager.class).getGlobalActivity(ACTIVITY_AUCTION);
        if (CheckNull.isNull(globalActivityAuctionData)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 活动未开启, activityType:", ACTIVITY_AUCTION);
        }

        return globalActivityAuctionData;
    }

    /**
     * 获取拍卖活动信息
     *
     * @param lordId
     * @return
     * @throws MwException
     */
    public GamePb4.GetActAuctionInfoRs getActAuctionInfo(long lordId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(lordId);
        Activity activity = getActivityInfo(player);

        GlobalActivityAuctionData globalActivityAuctionData = (GlobalActivityAuctionData) getGlobalActivity();
        CommonPb.TwoInt.Builder currentRoundStatus = globalActivityAuctionData.getCurrentRoundStatus();
        if (CheckNull.isNull(currentRoundStatus)) {
            LogUtil.error("秋季拍卖活动开始定时器未跑! activityId:", activity.getActivityId());
            return PbHelper.createNoAuction(activity, null, null);
        }

        if (ObjectUtils.isEmpty(globalActivityAuctionData.getParams())) {
            throw new MwException(GameError.NO_CONFIG.getCode(), " 没有配置回合时间, activityType:", ACTIVITY_AUCTION, ", activityId:", activity.getActivityId());
        }
        Optional<ActivityAuctionParam> optional = globalActivityAuctionData.getParams().stream().filter(base -> base.getRound() == currentRoundStatus.getV1()).findFirst();
        if (!optional.isPresent()) {
            throw new MwException(GameError.NO_CONFIG.getCode(), " 没有配置回合时间, activityType:", ACTIVITY_AUCTION, ", activityId:", activity.getActivityId());
        }

        //展示中，则没有当前正在拍卖的拍卖品
        if (checkRoundStatus(currentRoundStatus, ActivityAuctionConst.ROUND_ON_DISPLAY, ActivityAuctionConst.ACT_END)) {
            return PbHelper.createNoAuction(activity, currentRoundStatus, optional.get());
        }

        List<StaticActAuction> staticActAuctionList = StaticActivityDataMgr.getAuctionItemByRound(activity.getActivityId(), currentRoundStatus.getV1());
        if (ObjectUtils.isEmpty(staticActAuctionList)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), " 没有配置, activityType:", ACTIVITY_AUCTION, ", activityId:", activity.getActivityId());
        }

        GamePb4.GetActAuctionInfoRs.Builder builder = GamePb4.GetActAuctionInfoRs.newBuilder();
        builder.setActivityId(activity.getActivityId());
        builder.addAllConcernedItem(activity.getActivityAuction().getConcernedItem());
        builder.setRound(currentRoundStatus.getV1());
        builder.setStatus(currentRoundStatus.getV2());
        builder.setBeginTime((int) (optional.get().getStartTime().getTime() / TimeHelper.SECOND_MS));
        builder.setEndTime((int) (optional.get().getEndTime().getTime() / TimeHelper.SECOND_MS));
        staticActAuctionList.forEach(staticActAuction -> {
            if (CheckNull.isNull(staticActAuction)) {
                return;
            }

            GlobalActivityAuctionItem item = globalActivityAuctionData.getAuctionItem(staticActAuction.getId());
            builder.addGlobalActivityAuctionItem(item.serialization(false,
                    activity.getActivityAuction().getMyPrice(item, player.getLordId())));
        });

        return builder.build();
    }

    /**
     * 获得当前拍卖场已拍商品记录
     *
     * @param lordId
     * @return
     */
    public GamePb4.GetActAuctionRecordRs getActAuctionRecord(long lordId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(lordId);
        Activity activity = getActivityInfo(player);
        GlobalActivityAuctionData globalActivityAuctionData = (GlobalActivityAuctionData) getGlobalActivity();
        CommonPb.TwoInt.Builder currentRoundStatus = globalActivityAuctionData.getCurrentRoundStatus();

        List<ActivityAuctionParam> params = globalActivityAuctionData.getParams();
        if (ObjectUtils.isEmpty(params)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), " 秋季拍卖时间没有配置, activityId:", activity.getActivityId());
        }

        //活动没有拍卖记录信息
        if (checkRoundStatus(currentRoundStatus) || params.get(0).getRound() == currentRoundStatus.getV1()) {
            return PbHelper.createNoAuctionRecord(activity);
        }

        boolean contains = params.get(params.size() - 1).getRound() == currentRoundStatus.getV1();
        Map<Integer, List<StaticActAuction>> roundAuctionItems = StaticActivityDataMgr.getAuctionItemByLessThanRound
                (activity.getActivityId(), currentRoundStatus.getV1(), contains);
        GamePb4.GetActAuctionRecordRs.Builder builder = GamePb4.GetActAuctionRecordRs.newBuilder();
        builder.setActivityId(activity.getActivityId());
        roundAuctionItems.forEach((round, list) -> {
            ActivityAuctionParam tmp = globalActivityAuctionData.getParam(round);
            if (CheckNull.isNull(tmp))
                return;

            CommonPb.ActAuctionRoundRecord.Builder recordPb = CommonPb.ActAuctionRoundRecord.newBuilder();
            recordPb.setRound(round);
            recordPb.setTime((int) (tmp.getStartTime().getTime() / TimeHelper.SECOND_MS));
            if (!ObjectUtils.isEmpty(list)) {
                list.forEach(staticActAuction -> {
                    GlobalActivityAuctionItem auctionItem = globalActivityAuctionData.getItemMap().get(staticActAuction.getId());
                    if (CheckNull.isNull(auctionItem)) {
                        return;
                    }
                    if (!auctionItem.isSettle()) {
                        return;
                    }

                    CommonPb.GlobalActivityAuctionItem.Builder itemBuilder = CommonPb.GlobalActivityAuctionItem.newBuilder();
                    itemBuilder.setItemId(staticActAuction.getId());
                    Player joinPlayer = playerDataManager.getPlayer(auctionItem.getOwnerLordId());
                    itemBuilder.setNickName(CheckNull.isNull(joinPlayer) ? auctionItem.getNickName() : joinPlayer.lord.getNick());
                    itemBuilder.setCamp(CheckNull.isNull(joinPlayer) ? auctionItem.getCamp() : joinPlayer.lord.getCamp());
                    itemBuilder.setFinalPrice(auctionItem.getFinalPrice());

                    recordPb.addRecord(itemBuilder);
                });
            }

            builder.addRecord(recordPb);
        });

        return builder.build();
    }

    /**
     * 获取单件拍卖品全服记录
     *
     * @param lordId
     * @param itemId
     * @return
     */
    public GamePb4.GetActAuctionItemRecordRs getActAuctionItemRecord(long lordId, int itemId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(lordId);
        Activity activity = getActivityInfo(player);
        GlobalActivityAuctionData globalActivityAuctionData = (GlobalActivityAuctionData) getGlobalActivity();
        CommonPb.TwoInt.Builder currentRoundStatus = globalActivityAuctionData.getCurrentRoundStatus();

        StaticActAuction staticActAuction = StaticActivityDataMgr.getAuctionItemById(itemId);
        if (CheckNull.isNull(staticActAuction)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), " 没有配置, activityType:", ACTIVITY_AUCTION, ", itemId:", itemId);
        }
        if (staticActAuction.getActivityId() != activity.getActivityId()) {
            throw new MwException(GameError.ACTIVITY_NO_EXIST.getCode(), " 活动不存在, activityType:", ACTIVITY_AUCTION,
                    ", itemId:", itemId, ", 玩家活动ID:", activity.getActivityId());
        }
        if (staticActAuction.getRound() != currentRoundStatus.getV1()) {
            throw new MwException(GameError.INCORRECT_ROUND.getCode(), " 拍卖品非当前出售, activityType:", ACTIVITY_AUCTION,
                    ", itemId:", itemId, ", 当前活动轮数:", currentRoundStatus.getV1());
        }
        if (currentRoundStatus.getV2() != ActivityAuctionConst.ON_SALE.getType()) {
            throw new MwException(GameError.AUCTION_NOT_ON_SALE.getCode(), " 拍卖品未上架, activityType:", ACTIVITY_AUCTION,
                    ", itemId:", itemId, ", 当前活动轮数:", currentRoundStatus.getV1(), ", 当前活动轮数状态:", currentRoundStatus.getV2());
        }

        GlobalActivityAuctionItem auctionItem = globalActivityAuctionData.getAuctionItem(itemId);
        GamePb4.GetActAuctionItemRecordRs.Builder builder = GamePb4.GetActAuctionItemRecordRs.newBuilder();
        builder.setActivityId(activity.getActivityId());
        builder.setId(itemId);
        Optional.ofNullable(auctionItem.getRecordList()).ifPresent(recordList -> recordList.forEach(record -> {
            CommonPb.GlobalActivityAuctionItemRecord.Builder recordBuilder = CommonPb.GlobalActivityAuctionItemRecord.newBuilder();
            recordBuilder.setCostPrice(record.getCostPrice());
            recordBuilder.setBiddingTime(record.getBiddingTime());
            Player joinPlayer = playerDataManager.getPlayer(record.getLordId());
            recordBuilder.setNickName(CheckNull.isNull(joinPlayer) ? record.getNickName() : joinPlayer.lord.getNick());
            recordBuilder.setMakeDeal(record.isMakeDeal());
            recordBuilder.setItemId(itemId);
            builder.addRecord(recordBuilder);
        }));

        return builder.build();
    }

    /**
     * 获取自己的拍卖记录
     *
     * @param lordId
     * @return
     * @throws MwException
     */
    public GamePb4.GetMyAuctionRecordRs getMyAuctionRecord(long lordId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(lordId);
        Activity activity = getActivityInfo(player);
        GlobalActivityAuctionData globalActivityAuctionData = (GlobalActivityAuctionData) getGlobalActivity();
        CommonPb.TwoInt.Builder currentRoundStatus = globalActivityAuctionData.getCurrentRoundStatus();
        //活动未开始, 没有个人记录
        if (checkRoundStatus(currentRoundStatus)) {
            return PbHelper.createNoMyAuctionRecord(activity);
        }

        GamePb4.GetMyAuctionRecordRs.Builder builder = GamePb4.GetMyAuctionRecordRs.newBuilder();
        builder.setActivityId(activity.getActivityId());
        ActivityAuction activityAuction = activity.getActivityAuction();
        Optional.ofNullable(activityAuction).ifPresent(auction ->
                Optional.ofNullable(auction.getRecordList()).ifPresent(recordList -> recordList.forEach(record -> {
                    SerializePb.ActivityAuctionRecord.Builder recordBuilder = SerializePb.ActivityAuctionRecord.newBuilder();
                    recordBuilder.setStatus(record.getStatus().getType());
                    recordBuilder.setCostDiamond(record.getCostDiamond());
                    recordBuilder.setItemId(record.getItemId());
                    recordBuilder.setLordId(record.getLordId());
                    recordBuilder.setLogTime(record.getLogTime());
                    Player joinPlayer = playerDataManager.getPlayer(record.getLordId());
                    recordBuilder.setNickName(CheckNull.isNull(joinPlayer) ? record.getNickName() : joinPlayer.lord.getNick());

                    builder.addRecord(recordBuilder);
                })));

        return builder.build();
    }

    /**
     * 给拍卖品出价
     *
     * @param lordId
     * @param req
     * @return
     * @throws MwException
     */
    public GamePb4.PurchaseAuctionItemRs purchaseAuctionItem(long lordId, GamePb4.PurchaseAuctionItemRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(lordId);
        int itemId = req.getId();
        int type = req.getType();
        int cost = req.getCost();
        ActivityAuctionConst auctionConst = ActivityAuctionConst.purchase(type);
        if (CheckNull.isNull(auctionConst)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), " 参数错误, activityType:", ACTIVITY_AUCTION,
                    ", itemId:", itemId, ", 当前出价类型:", type);
        }

        //请求数据异常
        if (ActivityAuctionConst.ORDINARY_AUCTION.equals(auctionConst) && cost <= 0) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), " 参数错误, activityType:", ACTIVITY_AUCTION,
                    ", itemId:", itemId, ", 当前出价钻石:", cost);
        }

        GlobalActivityAuctionData globalActivityAuctionData = (GlobalActivityAuctionData) getGlobalActivity();
        CommonPb.TwoInt.Builder currentRoundStatus = globalActivityAuctionData.getCurrentRoundStatus();
        if (checkRoundStatus(currentRoundStatus, ActivityAuctionConst.ROUND_ON_DISPLAY, ActivityAuctionConst.ACT_END)) {
            throw new MwException(GameError.ACTIVITY_NOT_START.getCode(), " 拍卖活动未开始, activityType:", ACTIVITY_AUCTION,
                    ", itemId:", itemId, ", 当前出价类型:", auctionConst);
        }
        StaticActAuction staticActAuction = StaticActivityDataMgr.getAuctionItemById(itemId);
        if (CheckNull.isNull(staticActAuction)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), " 没有配置, activityType:", ACTIVITY_AUCTION, ", itemId:", itemId);
        }
        if (staticActAuction.getRound() != currentRoundStatus.getV1()) {
            throw new MwException(GameError.NOT_CURRENT_ROUND.getCode(), " 非当前回合拍卖品, activityType:", ACTIVITY_AUCTION,
                    ", itemId:", itemId, ", 当前活动轮数:", currentRoundStatus.getV1());
        }

        GlobalActivityAuctionItem auctionItem = globalActivityAuctionData.getAuctionItem(itemId);
        if (auctionItem.isMakeDeal()) {
            throw new MwException(GameError.HAS_MADE_DEAL.getCode(), " 拍卖品已一口价出售, activityType:", ACTIVITY_AUCTION,
                    ", itemId:", itemId);
        }
        if (auctionItem.isSettle()) {
            throw new MwException(GameError.HAS_SETTLED.getCode(), " 拍卖品已结算, activityType:", ACTIVITY_AUCTION,
                    ", itemId:", itemId);
        }

        if (!staticActAuction.canBidding(cost, auctionConst, auctionItem.getFinalPrice())) {
            throw new MwException(GameError.INSUFFICIENT_STARTING_PRICE.getCode(), " 竞拍价不足, activityType:", ACTIVITY_AUCTION,
                    ", itemId:", itemId, ", 出价钻石数:", cost, ", 出价类型:", auctionConst);
        }

        Activity activity = getActivityInfo(player);
        if (staticActAuction.getActivityId() != activity.getActivityId()) {
            throw new MwException(GameError.ACTIVITY_NO_EXIST.getCode(), " 活动不存在, activityType:", ACTIVITY_AUCTION,
                    ", itemId:", itemId, ", 玩家活动ID:", activity.getActivityId());
        }

        ActivityAuction activityAuction = activity.getActivityAuction();
        int preCost = activityAuction.getCostDiamond().getOrDefault(itemId, 0);
        //第一次竞拍或自己出价超过最高竞拍价(除去自己把自己超过) 校验最高价次数
        boolean check = auctionItem.getFinalPrice() == 0 || preCost < auctionItem.getFinalPrice();
        if (check && activityAuction.getHighestBidCount() >= ActParamConstant.HIGHEST_Bid_COUNT) {
            throw new MwException(GameError.MAXIMUM_PRICE_EXCEEDED.getCode(), " 超出最高价次数, highestBidCount:",
                    activityAuction.getHighestBidCount(), ", itemId:", itemId, ", 当前活动轮数:", currentRoundStatus.getV1(), ", lordId:", lordId);
        }
        int typeHighestBidCountCount = activityAuction.getTypeHighestBidCountCount().getOrDefault(staticActAuction.getType(), 0);
        int staticTypeHighestBidCountCount = ActParamConstant.ACT_AUCTION_TYPE_HIGHEST_COUNT.getOrDefault(staticActAuction.getType(), 0);
        if (check && staticTypeHighestBidCountCount != 0 && typeHighestBidCountCount >= staticTypeHighestBidCountCount) {
            throw new MwException(GameError.MAXIMUM_TYPE_PRICE_EXCEEDED.getCode(), " 超出类型最高价次数, typeHighestBidCount:",
                    typeHighestBidCountCount, ", itemId:", itemId, ", 当前活动轮数:", currentRoundStatus.getV1(), ", lordId:", lordId);
        }

        //扣除钻石
        cost = auctionConst.equals(ActivityAuctionConst.DIRECT_BIDDING) ? staticActAuction.getPurchasePrice() : cost;
        int realCost = auctionConst.equals(ActivityAuctionConst.DIRECT_BIDDING) ? cost : cost - preCost;
        // 检查玩家资源是否足够, 并扣除
        rewardDataManager.checkPlayerResIsEnough(player, AwardType.MONEY, AwardType.Money.GOLD, realCost);
        rewardDataManager.subGold(player, realCost, false, AwardFrom.JOIN_IN_BIDDING_AUCTION, itemId, auctionConst.getType());

        //处理个人活动记录
        Player otherPlayer = playerDataManager.getPlayer(auctionItem.getOwnerLordId());
        otherPlayer = auctionItem.getOwnerLordId() == 0l ? null : otherPlayer;

        int myRecordCost;
        int otherRecordCost = 0;
        int now = TimeHelper.getCurrentSecond();
        boolean overTakenMine = CheckNull.isNull(otherPlayer) ? false : otherPlayer.getLordId() == lordId;
        if (ActivityAuctionConst.DIRECT_BIDDING.equals(auctionConst) || CheckNull.isNull(otherPlayer)) {
            //一口价拍下或第一次竞拍 记录当前消耗总价
            myRecordCost = cost;
        } else {
            //自己超过自己记录差价
            myRecordCost = cost - preCost;
            if (!overTakenMine) {
                //超过他人, 记录他人将返还的钻石
                otherRecordCost = auctionItem.getFinalPrice();
            }
        }

        activity.getActivityAuction().purchase(lordId, player.lord.getNick(), cost, itemId, auctionConst, myRecordCost, now, true);
        activityAuction.addHighestBidCount(true, staticActAuction.getType());
        if (Objects.nonNull(otherPlayer)) {
            //邮件通知 竞拍被超过
            ActivityAuction otherActivityAuction = getActivityInfo(otherPlayer).getActivityAuction();
            otherActivityAuction.addHighestBidCount(false, staticActAuction.getType());
            mailDataManager.sendNormalMail(otherPlayer, MailConstant.BID_WAS_OVERTAKEN, TimeHelper.getCurrentSecond(), TimeHelper.getCurrentSecond(), itemId);
            if (!overTakenMine && ActivityAuctionConst.ORDINARY_AUCTION.equals(auctionConst)) {
                //不是自己竞拍且是普通竞拍 添加自己的记录
                otherActivityAuction.purchase(lordId, player.lord.getNick(), auctionItem.getFinalPrice(), itemId, auctionConst, otherRecordCost, now, false);
            }
        }

        //处理整体活动记录
        auctionItem.purchaseAuctionItem(lordId, player.lord.getNick(), player.lord.getCamp(), cost, auctionConst);
        //添加竞拍者
        globalActivityAuctionData.addParticipant(lordId);
        if (auctionConst.equals(ActivityAuctionConst.DIRECT_BIDDING)) {
            //发送跑马灯
            chatDataManager.sendSysChat(ChatConst.CHAT_ACT_DIRECT_BIDDING, 0, 0, player.lord.getNick(), itemId, player.getCamp());
        }

        GamePb4.PurchaseAuctionItemRs.Builder builder = GamePb4.PurchaseAuctionItemRs.newBuilder();
        builder.setActivityId(activity.getActivityId());
        builder.setId(itemId);

        //刷新钻石数
        ChangeInfo change = ChangeInfo.newIns();
        change.addChangeType(AwardType.MONEY, AwardType.Money.GOLD);
        rewardDataManager.syncRoleResChanged(player, change);

        syncActivityAuctionItemChange(auctionItem);
        return builder.build();
    }

    /**
     * 同步秋季拍卖品信息
     *
     * @param item
     */
    private void syncActivityAuctionItemChange(GlobalActivityAuctionItem item) {
        GamePb4.SyncActivityAuctionItemChangeRs.Builder builder = GamePb4.SyncActivityAuctionItemChangeRs.newBuilder();
        playerDataManager.getAllOnlinePlayer().values().forEach(player -> {
            Activity activity = activityDataManager.getActivityInfo(player, ACTIVITY_AUCTION);
            if (CheckNull.isNull(activity))
                return;
            builder.setGlobalActivityAuctionItem(item.serialization(true,
                    activity.getActivityAuction().getMyPrice(item, player.getLordId())));
            BasePb.Base msg = PbHelper.createSynBase(GamePb4.SyncActivityAuctionItemChangeRs.EXT_FIELD_NUMBER,
                    GamePb4.SyncActivityAuctionItemChangeRs.ext, builder.build()).build();
            playerService.syncMsgToPlayer(msg, player);

            builder.clear();
        });
    }

    /**
     * 关注(取关)拍卖品
     *
     * @param lordId
     * @param type
     * @return
     */
    public GamePb4.FollowAuctionsRs followAuctions(long lordId, int type, int itemId) throws MwException {
        ActivityAuctionConst auctionConst = ActivityAuctionConst.convertTo(type);
        if (CheckNull.isNull(auctionConst) || !(auctionConst.equals(ActivityAuctionConst.FOCUS_ON) ||
                auctionConst.equals(ActivityAuctionConst.UNSUBSCRIBE))) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), " 参数错误, type:", type);
        }
        StaticActAuction staticActAuction = StaticActivityDataMgr.getAuctionItemById(itemId);
        if (CheckNull.isNull(staticActAuction)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), " 没有配置, activityType:", ACTIVITY_AUCTION, ", itemId:", itemId);
        }

        Player player = playerDataManager.checkPlayerIsExist(lordId);
        ActivityAuction activityAuction = getActivityInfo(player).getActivityAuction();
        if (!activityAuction.operation(itemId, auctionConst)) {
            GameError gameError = ActivityAuctionConst.FOCUS_ON.equals(auctionConst) ? GameError.HAS_FOCUSED_ON : GameError.FOCUS_ON_NOT_YET;
            throw new MwException(gameError.getCode(), ", ", gameError.getMsg(), ", activityType:", ACTIVITY_AUCTION, ", itemId:", itemId);
        }

        GlobalActivityAuctionData globalActivityAuctionData = (GlobalActivityAuctionData) getGlobalActivity();
        CommonPb.TwoInt.Builder currentRoundStatus = globalActivityAuctionData.getCurrentRoundStatus();
        if (checkRoundStatus(currentRoundStatus, ActivityAuctionConst.ROUND_ON_DISPLAY, ActivityAuctionConst.ACT_END)) {
            throw new MwException(GameError.ACTIVITY_NOT_START.getCode(), " 拍卖活动未开始, activityType:", ACTIVITY_AUCTION,
                    ", itemId:", itemId, ", 当前关注操作类型:", staticActAuction);
        }

        activityAuction.followAuctions(auctionConst, Integer.valueOf(itemId));

        GamePb4.FollowAuctionsRs.Builder builder = GamePb4.FollowAuctionsRs.newBuilder();
        builder.setId(itemId);
        builder.addAllConcernedItem(activityAuction.getConcernedItem());

        return builder.build();
    }

    /**
     * 根据类型获取当前拍卖品信息
     *
     * @param lordId
     * @param type
     * @return
     * @throws MwException
     */
    public GamePb4.GetActAuctionTypeRs getActAuctionType(long lordId, int type) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(lordId);
        Activity activity = getActivityInfo(player);

        ActivityAuctionConst auctionConst = ActivityAuctionConst.inType(type);
        if (CheckNull.isNull(auctionConst)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), " 参数错误, type:", type);
        }

        GlobalActivityAuctionData globalActivityAuctionData = (GlobalActivityAuctionData) getGlobalActivity();
        CommonPb.TwoInt.Builder currentRoundStatus = globalActivityAuctionData.getCurrentRoundStatus();
        if (checkRoundStatus(currentRoundStatus, ActivityAuctionConst.ROUND_ON_DISPLAY, ActivityAuctionConst.ACT_END)) {
            throw new MwException(GameError.ACTIVITY_NOT_START.getCode(), " 拍卖活动未开始, activityType:", ACTIVITY_AUCTION,
                    ", type:", type, ", 当前关注操作类型:", auctionConst);
        }

        Set<Integer> list = null;
        if (auctionConst.equals(ActivityAuctionConst.FOLLOWED_PROPS_LIST)) {
            list = activity.getActivityAuction().getConcernedItem();
        } else {
            list = StaticActivityDataMgr.getAuctionItemByType(activity.
                    getActivityId(), auctionConst.getItemType(), currentRoundStatus.getV1());
            if (ObjectUtils.isEmpty(list)) {
                throw new MwException(GameError.ACT_AUCTION_CONFIG_ERROR.getCode(), " 拍卖活动配置出错, activityType:", ACTIVITY_AUCTION,
                        ", 当前关注操作类型:", auctionConst, ", 未找到当前类型拍卖道具");
            }
        }

        GamePb4.GetActAuctionTypeRs.Builder builder = GamePb4.GetActAuctionTypeRs.newBuilder();
        if (!ObjectUtils.isEmpty(list)) {
            list.forEach(item -> {
                GlobalActivityAuctionItem auctionItem = globalActivityAuctionData.getAuctionItem((Integer) item);
                builder.addGlobalActivityAuctionItem(auctionItem.serialization(false,
                        activity.getActivityAuction().getMyPrice(auctionItem, player.getLordId())));
            });
        }

        return builder.build();
    }


    /**
     * 校验当前活动是否有轮数开始 以及是否符合某些状态
     *
     * @param currentRoundStatus
     * @param activityStatus
     * @return
     */
    private static boolean checkRoundStatus(CommonPb.TwoInt.Builder currentRoundStatus, ActivityAuctionConst... activityStatus) {
        return CheckNull.isNull(currentRoundStatus) || (!ObjectUtils.isEmpty(activityStatus) &&
                Arrays.stream(activityStatus).anyMatch(activityAuctionConst -> currentRoundStatus.getV2() == activityAuctionConst.getType()));
    }

    @Override
    protected GeneratedMessage.Builder<GamePb4.GetActivityDataInfoRs.Builder>
    getActivityData(Player player, Activity activity, GlobalActivityData globalActivityData) {
        return null;
    }

    @Override
    protected int[] getActivityType() {
        return new int[]{ACTIVITY_AUCTION};
    }

    /**
     * 同步活动状态
     *
     * @param actId
     * @param round
     * @param status
     * @param beginTime
     * @param endTime
     */
    private static void syncActAuctionStatus(int actId, int round, ActivityAuctionConst status, long beginTime, long endTime, GlobalActivityAuctionData data) {
        GamePb4.SyncActivityAuctionStatusRs.Builder builder = GamePb4.SyncActivityAuctionStatusRs.newBuilder();
        builder.setActivityId(actId);
        builder.setStatus(status.getType());
        builder.setRound(round);
        builder.setBeginTime((int) beginTime);
        builder.setEndTime((int) endTime);

        if (ActivityAuctionConst.ON_SALE.equals(status)) {
            List<StaticActAuction> staticActAuctionList = StaticActivityDataMgr.getAuctionItemByRound(actId, round);
            if (!ObjectUtils.isEmpty(staticActAuctionList)) {
                staticActAuctionList.forEach(staticActAuction -> {
                    if (CheckNull.isNull(staticActAuction)) {
                        return;
                    }

                    GlobalActivityAuctionItem item = data.getAuctionItem(staticActAuction.getId());
                    builder.addGlobalActivityAuctionItem(item.serialization(false, 0));
                });
            }
        }

        BasePb.Base msg = PbHelper.createSynBase(GamePb4.SyncActivityAuctionStatusRs.EXT_FIELD_NUMBER, GamePb4.SyncActivityAuctionStatusRs.ext, builder.build()).build();
        DataResource.getBean(PlayerService.class).syncMsgToAll(msg);
    }

    /**
     * 活动开始
     *
     * @param activityType
     * @param activityId
     * @param keyId
     */
    @Override
    protected void handleOnBeginTime(int activityType, int activityId, int keyId) {
        if (ObjectUtils.isEmpty(StaticActivityDataMgr.getActivityList())) {
            return;
        }

        GlobalActivityAuctionData data = null;
        try {
            data = (GlobalActivityAuctionData) getGlobalActivity();
        } catch (MwException e) {
            LogUtil.error("秋季拍卖活动开始时，开始定时器出错, e:", e);
            return;
        }
        //活动开始，清除上一次活动状态
        data.beginNextAct();
        if (!data.checkInit(keyId)) {
            LogUtil.error("秋季拍卖活动开始时，活动回合时间配置有误, keyId:", keyId);
            return;
        }

        ActivityAuctionParam param = data.getParams().get(0);
        data.updateRoundStatus(param.getRound(), ActivityAuctionConst.ROUND_ON_DISPLAY);
        syncActAuctionStatus(activityId, param.getRound(), ActivityAuctionConst.ROUND_ON_DISPLAY,
                param.getStartTime().getTime() / TimeHelper.SECOND_MS, param.getEndTime().getTime() / TimeHelper.SECOND_MS, data);
    }

    /**
     * 服务器启动或gm重刷表时添加定时器
     *
     * @param activityBase
     * @param now
     * @param scheduler
     */
    public void addSchedule(ActivityBase activityBase, Date now, Scheduler scheduler, Object... objects) {
        if (TimeHelper.getCurrentSecond() >= activityBase.getEndTime().getTime()) {
            LogUtil.error("添加秋季活动定时器失败, 活动配置过期, actId:", activityBase.getActivityId());
            return;
        }

        StringBuffer jobName = new StringBuffer();
        List<ActivityAuctionParam> data = GlobalActivityAuctionData.initActivityAuctionBase(activityBase);
        if (!ObjectUtils.isEmpty(data)) {
            data.forEach(param -> {
                if (Objects.nonNull(param.getStartTime()) && param.getStartTime().getTime() > now.getTime()) {
                    jobName.append(ActAuctionRoundJob.NAME_ROUND_BEGIN).append(activityBase.getActivityId()).append("_").append(param.getRound());
                    QuartzHelper.addJob(scheduler, jobName.toString(), ActAuctionRoundJob.GROUP_NAME, ActAuctionRoundJob.class, param.getStartTime());
                    jobName.delete(0, jobName.length());
                }
                if (Objects.nonNull(param.getEndTime()) && param.getEndTime().getTime() > now.getTime()) {
                    jobName.append(ActAuctionRoundJob.NAME_ROUND_END).append(activityBase.getActivityId()).
                            append("_").append(param.getRound()).append("_").append(activityBase.getPlanKeyId());
                    QuartzHelper.addJob(scheduler, jobName.toString(), ActAuctionRoundJob.GROUP_NAME, ActAuctionRoundJob.class, param.getEndTime());
                    jobName.delete(0, jobName.length());
                }
                if (Objects.nonNull(param.getAboutToEndTime()) && param.getAboutToEndTime().getTime() > now.getTime()) {
                    jobName.append(ActAuctionRoundJob.NAME_ROUND_ABOUT_TO_END).append(activityBase.getActivityId()).
                            append("_").append(param.getRound()).append("_").append(activityBase.getPlanKeyId());
                    QuartzHelper.addJob(scheduler, jobName.toString(), ActAuctionRoundJob.GROUP_NAME, ActAuctionRoundJob.class, param.getAboutToEndTime());
                    jobName.delete(0, jobName.length());
                }
            });
        }

        //秋季拍卖活动 结束活动按正常活动结束
        jobName.append(activityBase.getActivityType()).append("_").
                append(activityBase.getActivityId()).append("_").append(activityBase.getPlan().getKeyId());
        if (activityBase.getEndTime().getTime() > now.getTime()) {
            QuartzHelper.removeJob(scheduler, jobName.toString(), "ACT_END");
            QuartzHelper.addJob(scheduler, jobName.toString(), "ACT_END", ActEndJob.class, activityBase.getEndTime());
        }

        if (Objects.nonNull(activityBase.getDisplayTime()) && activityBase.getDisplayTime().getTime() > now.getTime()) {
            QuartzHelper.removeJob(scheduler, jobName.toString(), "ACT_OVER");
            QuartzHelper.addJob(scheduler, jobName.toString(), "ACT_OVER", ActOverJob.class, activityBase.getDisplayTime());
        }

        //处理刷表时, 刷新配置时间
        GlobalActivityAuctionData globalActivityData = (GlobalActivityAuctionData) DataResource.ac.
                getBean(ActivityDataManager.class).getGlobalActivity(ACTIVITY_AUCTION);
        if (Objects.nonNull(globalActivityData) && Objects.nonNull(data) &&
                globalActivityData.getPlanKeyId() == activityBase.getPlanKeyId()) {
            globalActivityData.setParams(data);
            if (!ObjectUtils.isEmpty(objects)) {
                checkTimer();
            }
        }
    }

    /**
     * 容错校验
     */
    public void checkTimer() {
        GlobalActivityAuctionData data = (GlobalActivityAuctionData) DataResource.ac.
                getBean(ActivityDataManager.class).getGlobalActivity(ACTIVITY_AUCTION);
        if (CheckNull.isNull(data)) {
            LogUtil.error("全局秋季拍卖数据为空");
            return;
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(ACTIVITY_AUCTION);
        if (Objects.isNull(activityBase) || ObjectUtils.isEmpty(data.getParams())) {
            LogUtil.error("秋季拍卖活动，活动id:", data.getActivityId(), " 未找到活动配置 activityBase为空, 或活动时间配置为空");
            return;
        }

        long now = System.currentTimeMillis();
        if (now < activityBase.getBeginTime().getTime()) {
            return;
        }
        if (now == activityBase.getBeginTime().getTime()) {
            handleOnBeginTime(ACTIVITY_AUCTION, activityBase.getActivityId(), activityBase.getPlanKeyId());
            return;
        }

        CommonPb.TwoInt.Builder roundStatus = data.getCurrentRoundStatus();
        CommonPb.TwoInt.Builder current = data.inRoundStatus(now);
        if (CheckNull.isNull(current)) {
            return;
        }

        //容错处理
        if (Objects.nonNull(roundStatus))
            checkPreviousRound(roundStatus.getV1(), current.getV1(), data, activityBase);
        if (checkIncorrect(roundStatus, current)) {
            LogUtil.error("矫正拍卖活动状态数据, 当前拍卖活动数据库最新状态:", roundStatus == null ? "null" : roundStatus.getV1(), "---",
                    roundStatus == null ? "null" : roundStatus.getV2(), ", 正确的拍卖活动状态:", current.getV1(), "---", current.getV2());
            verifyRoundStatus(data, current, roundStatus, activityBase);
            roundStatus = data.getCurrentRoundStatus();
            LogUtil.error("矫正拍卖活动后的状态数据:", roundStatus == null ? "null" : roundStatus.getV1(), "---",
                    roundStatus == null ? "null" : roundStatus.getV2());
        }
    }

    /**
     * 校验以前的轮数数据
     * @param currentRound
     * @param data
     * @param base
     */
    private void checkPreviousRound(int currentRound, int correctRound, GlobalActivityAuctionData data, ActivityBase base) {
        Map<Integer, ActivityAuctionConst> map = data.getRoundStatus();
        if (ObjectUtils.isEmpty(map)) {
            return;
        }

        map.forEach((round, status) -> {
            if (round >= correctRound || round == currentRound) {
                return;
            }
            if (!ActivityAuctionConst.ACT_END.equals(status)) {
                //结算异常轮次
                settleWrongData(round, base);
            }
        });
    }

    /**
     * 结算之前的轮次数据
     * @param round
     * @param base
     */
    private void settleWrongData(int round, ActivityBase base) {
        List<StaticActAuction> auctionItemList = StaticActivityDataMgr.getAuctionItemByRound(base.getActivityId(), round);
        if (ObjectUtils.isEmpty(auctionItemList)) {
            LogUtil.error("秋季拍卖活动，活动id:", base.getActivityId(), ", 活动轮数：", round, " 没有拍卖品配置");
            return;
        }

        //结算拍卖品
        GlobalActivityAuctionData data = (GlobalActivityAuctionData) DataResource.getBean(ActivityDataManager.class).getGlobalActivity(ACTIVITY_AUCTION);
        if (CheckNull.isNull(data)) {
            LogUtil.error("秋季拍卖活动，GlobalActivityAuctionData is null");
            return;
        }
        //以前的轮次只结算道具，不结算返还钻石
        settleAuctionItems(data, auctionItemList);
        data.updateRoundStatus(round, ActivityAuctionConst.ACT_END);
    }

    /**
     * 校验拍卖场不同轮次或不同状态数据异常
     *
     * @param data
     * @param current
     * @param roundStatus
     * @param activityBase
     */
    private void verifyRoundStatus(GlobalActivityAuctionData data, CommonPb.TwoInt.Builder current, CommonPb.TwoInt.Builder roundStatus, ActivityBase activityBase) {
        ActivityAuctionConst auctionConst = ActivityAuctionConst.convertTo(current.getV2());
        switch (auctionConst) {
            case ROUND_ON_DISPLAY:
                handleInSame(data, current, roundStatus, activityBase);
                CommonPb.TwoInt.Builder newRoundStatus = data.getCurrentRoundStatus();
                //修改当前展示期状态
                if (checkIncorrect(newRoundStatus, current))
                    updateRoundStatus(data, data.getParam(current.getV1()), auctionConst);
                break;

            case ON_SALE:
                handleInSame(data, current, roundStatus, activityBase);
                //开始正确轮数
                activityAuctionRoundBegin(data.getActivityId(), current.getV1());
                break;

            case ACT_END:
                handleInSame(data, current, roundStatus, activityBase);
                //结算最后一轮
                newRoundStatus = data.getCurrentRoundStatus();
                if (checkIncorrect(newRoundStatus, current))
                    activityAuctionRoundEnd(data.getActivityId(), current.getV1(), activityBase.getPlanKeyId(), true);
                break;
        }
    }

    /**
     * 处理 (1.轮数不一致直接结算  2.轮数一致状态不一致, 先结算后改状态  3.轮数与状态都不一致，先结算后改状态)
     *
     * @param data
     * @param current
     * @param roundStatus
     * @param activityBase
     */
    private void handleInSame(GlobalActivityAuctionData data, CommonPb.TwoInt.Builder current, CommonPb.TwoInt.Builder roundStatus, ActivityBase activityBase) {
        if (!CheckNull.isNull(roundStatus)) {
            //结算异常轮次
            activityAuctionRoundEnd(data.getActivityId(), roundStatus.getV1(), activityBase.getPlanKeyId(), true);
            //清除当前不正常数据
            CommonPb.TwoInt.Builder newRoundStatus = data.getCurrentRoundStatus();
            data.removeLargerRound(newRoundStatus.getV1(), current.getV1());

            if (roundStatus.getV1() < current.getV1()) {
                //填充当前开启轮数拍卖品
                fillingAuctionItem(activityBase.getActivityId(), current.getV1(), data);
            }
        } else {
            //填充当前开启轮数拍卖品
            fillingAuctionItem(activityBase.getActivityId(), current.getV1(), data);
        }
    }

    private static void fillingAuctionItem(int activityId, int round, GlobalActivityAuctionData data) {
        List<StaticActAuction> staticActAuctionList = StaticActivityDataMgr.getAuctionItemByRound(activityId, round);
        if (ObjectUtils.isEmpty(staticActAuctionList)) {
            return;
        }

        staticActAuctionList.forEach(staticActAuction -> {
            if (CheckNull.isNull(staticActAuction)) {
                return;
            }

            data.getAuctionItem(staticActAuction.getId());
        });
    }

    private boolean checkIncorrect(CommonPb.TwoInt.Builder roundStatus, CommonPb.TwoInt.Builder current) {
        return CheckNull.isNull(roundStatus) || roundStatus.getV1() != current.getV1() || roundStatus.getV2() != current.getV2();
    }

    /**
     * 纠正异常数据状态
     *
     * @param data
     * @param param
     * @param auctionConst
     */
    private void updateRoundStatus(GlobalActivityAuctionData data, ActivityAuctionParam param, ActivityAuctionConst auctionConst) {
        if (ObjectUtils.isEmpty(StaticActivityDataMgr.getActivityList())) {
            return;
        }

        data.updateRoundStatus(param.getRound(), auctionConst);
        syncActAuctionStatus(data.getActivityId(), param.getRound(), auctionConst,
                param.getStartTime().getTime() / TimeHelper.SECOND_MS, param.getEndTime().getTime() / TimeHelper.SECOND_MS, data);
    }

    /**
     * 秋季拍卖活动 当前拍卖回合开始
     *
     * @param activityId
     * @param round
     */
    public static void activityAuctionRoundBegin(int activityId, int round) {
        List<StaticActAuction> auctionItemList = StaticActivityDataMgr.getAuctionItemByRound(activityId, round);
        if (ObjectUtils.isEmpty(auctionItemList)) {
            LogUtil.error("秋季拍卖活动，活动id:", activityId, ", 活动轮数：", round, " 没有拍卖品配置");
            return;
        }

        GlobalActivityAuctionData data = (GlobalActivityAuctionData) DataResource.getBean(ActivityDataManager.class).
                getGlobalActivity(ACTIVITY_AUCTION);
        if (CheckNull.isNull(data)) {
            LogUtil.error("秋季拍卖活动，活动id:", activityId, ", 活动轮数：", round, " 获取全局活动数据为空");
            return;
        }

        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(ACTIVITY_AUCTION);
        if (Objects.isNull(activityBase) || ObjectUtils.isEmpty(data.getParams())) {
            LogUtil.error("秋季拍卖活动，活动id:", activityId, ", 活动轮数：", round, " 未找到活动配置 activityBase为空, 或活动时间配置为空");
            return;
        }

        data.updateRoundStatus(round, ActivityAuctionConst.ON_SALE);
        Optional<ActivityAuctionParam> optional = data.getParams().stream().filter(base -> base.getRound() == round).findFirst();
        if (!optional.isPresent()) {
            LogUtil.error("秋季拍卖活动，活动id:", activityId, ", 活动轮数：", round, " 未找到活动时间配置");
            return;
        }

        syncActAuctionStatus(activityId, round, ActivityAuctionConst.ON_SALE,
                optional.get().getStartTime().getTime() / TimeHelper.SECOND_MS, optional.get().getEndTime().getTime() / TimeHelper.SECOND_MS, data);
        //发送跑马灯
        DataResource.getBean(ChatDataManager.class).sendSysChat(ChatConst.CHAT_ACT_AUCTION_ROUND_START, 0, 0);
    }

    /**
     * 拍卖活动即将结束跑马灯
     */
    public void aboutToEndAct() {
        //发送跑马灯
        chatDataManager.sendSysChat(ChatConst.CHAT_ACT_AUCTION_ROUND_END, 0, 0);
    }


    /**
     * 秋季拍卖活动 当前拍卖回合结束
     *
     * @param activityId
     * @param round
     * @throws MwException
     */
    public static void activityAuctionRoundEnd(int activityId, int round, int planKeyId, boolean check) {
        if (ObjectUtils.isEmpty(StaticActivityDataMgr.getActivityList())) {
            LogUtil.error("活动列表没有配置, planKeyId:", planKeyId);
            return;
        }

        ActivityBase activityBase = StaticActivityDataMgr.getActivityList().stream().
                filter(data -> data.getPlanKeyId() == planKeyId).findFirst().orElse(null);
        if (CheckNull.isNull(activityBase)) {
            LogUtil.error("秋季拍卖活动没有配置, planKeyId:", planKeyId);
            return;
        }

        List<StaticActAuction> auctionItemList = StaticActivityDataMgr.getAuctionItemByRound(activityId, round);
        if (ObjectUtils.isEmpty(auctionItemList)) {
            LogUtil.error("秋季拍卖活动，活动id:", activityId, ", 活动轮数：", round, " 没有拍卖品配置");
            return;
        }

        //结算拍卖品
        GlobalActivityAuctionData data = (GlobalActivityAuctionData) DataResource.getBean(ActivityDataManager.class).getGlobalActivity(ACTIVITY_AUCTION);
        if (CheckNull.isNull(data)) {
            LogUtil.error("秋季拍卖活动，GlobalActivityAuctionData is null");
            return;
        }
        ActivityAuctionConst roundStatus = data.getRoundStatus().get(round);
        if (!ActivityAuctionConst.ACT_END.equals(roundStatus)) {
            settleAuctionItems(data, auctionItemList);
            settleReturn(data);
            data.updateRoundStatus(round, ActivityAuctionConst.ACT_END);
        }

        //进入下一轮活动展示状态
        if (ObjectUtils.isEmpty(data.getParams())) {
            LogUtil.error("秋季拍卖活动配置出错, activityId:", activityId);
            return;
        }

        ActivityAuctionParam param = data.getParams().get(data.getParams().size() - 1);
        if (param.getRound() == round) {
            //已经是活动最后一轮
            data.updateRoundStatus(param.getRound(), ActivityAuctionConst.ACT_END);
            syncActAuctionStatus(activityId, param.getRound(), ActivityAuctionConst.ACT_END,
                    param.getStartTime().getTime() / TimeHelper.SECOND_MS, param.getEndTime().getTime() / TimeHelper.SECOND_MS, data);

            LogUtil.debug("秋季拍卖活动结束, activityId:", activityId);
            return;
        }
        param = data.getNextRound(round);
        if (ObjectUtils.isEmpty(param)) {
            LogUtil.error("秋季活动配置出错, 未找到下一轮. activityId:", activityId, ", 上一轮轮数:", round);
            return;
        }

        data.getParticipants().clear();
        data.updateRoundStatus(param.getRound(), ActivityAuctionConst.ROUND_ON_DISPLAY);
        syncActAuctionStatus(activityId, param.getRound(), ActivityAuctionConst.ROUND_ON_DISPLAY,
                param.getStartTime().getTime() / TimeHelper.SECOND_MS, param.getEndTime().getTime() / TimeHelper.SECOND_MS, data);
    }

    /**
     * 拍卖回合结束，结算拍卖场商品
     *
     * @param data
     * @param auctionItemList
     * @throws MwException
     */
    private static void settleAuctionItems(GlobalActivityAuctionData data, List<StaticActAuction> auctionItemList) {
        Map<StaticActAuction, GlobalActivityAuctionItem> settleMap = data.getRoundItem(auctionItemList);
        if (ObjectUtils.isEmpty(settleMap)) {
            LogUtil.debug("当前拍卖回合没有人参与, round:", auctionItemList.get(0).getRound());
            return;
        }

        //处理拍卖品得主,以及竞拍者
        PlayerDataManager manager = DataResource.getBean(PlayerDataManager.class);
        int now = TimeHelper.getCurrentSecond();
        settleMap.forEach((staticItem, item) -> {
            if (item.getOwnerLordId() == 0) {
                return;
            }
            Player owner = manager.getPlayer(item.getOwnerLordId());
            if (item.isSettle()) {
                return;
            }
            //排除小号影响
            if (Objects.nonNull(owner)) {
                //拍卖品邮件
                List<CommonPb.Award> awardList = new ArrayList<>();
                awardList.add(PbHelper.createAward(staticItem.getAuctionItem()));
                DataResource.getBean(MailDataManager.class).sendAttachMail(owner, awardList, MailConstant.SUCCESSFUL_BIDDING,
                        AwardFrom.SUCCESSFUL_BIDDING, now, now, item.getFinalPrice(), staticItem.getId());

                Activity ownerActivity = DataResource.getBean(ActivityDataManager.class).getActivityInfo(owner, ACTIVITY_AUCTION);
                if (Objects.nonNull(ownerActivity) && !item.isMakeDeal()) {
                    ownerActivity.getActivityAuction().getCostDiamond().remove(staticItem.getId());
                }
            }

            //结算拍卖品成功
            item.settle();
        });
    }

    /**
     * 结算返还钻石
     * @param data
     */
    private static void settleReturn(GlobalActivityAuctionData data) {
        //拍卖品竞拍失败者处理
        Set<Long> costPlayer = data.getParticipants();
        if (ObjectUtils.isEmpty(costPlayer)) {
            return;
        }

        costPlayer.forEach(lordId -> {
            Player joinPlayer = DataResource.getBean(PlayerDataManager.class).getPlayer(lordId);
            if (CheckNull.isNull(joinPlayer)) {
                return;
            }
            Activity joinActivity = DataResource.getBean(ActivityDataManager.class).getActivityInfo(joinPlayer, ACTIVITY_AUCTION);
            if (CheckNull.isNull(joinActivity)) {
                return;
            }

            int cost = joinActivity.getActivityAuction().settle();
            if (cost <= 0) {
                return;
            }

            DataResource.getBean(RewardDataManager.class).sendRewardSignle(joinPlayer, AwardType.MONEY, AwardType.Money.GOLD, cost,
                    AwardFrom.AUCTION_REFUND);
            //邮件通知 竞拍失败邮件通知
            DataResource.getBean(MailDataManager.class).sendNormalMail(joinPlayer, MailConstant.BID_FAILED, TimeHelper.getCurrentSecond(), TimeHelper.getCurrentSecond(), cost);
        });
    }

    @Override
    protected void handleOnEndTime(int activityType, int activityId, int keyId) {
        LogUtil.debug("秋季拍卖活动结束===定时器, do nothing");
    }

    @Override
    protected void handleOnDisplayTime(int activityType, int activityId, int keyId) {
        //TODO NOTHING
    }

    @Override
    protected void handleOnDay(Player player) {
        //TODO NOTHING
    }
}
