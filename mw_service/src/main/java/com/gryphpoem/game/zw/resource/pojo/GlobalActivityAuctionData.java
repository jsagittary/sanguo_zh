package com.gryphpoem.game.zw.resource.pojo;

import com.google.protobuf.InvalidProtocolBufferException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.constant.ActParamConstant;
import com.gryphpoem.game.zw.resource.domain.ActivityAuctionParam;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.p.ActivityAuctionConst;
import com.gryphpoem.game.zw.resource.domain.p.GlobalActivity;
import com.gryphpoem.game.zw.resource.domain.s.StaticActAuction;
import com.gryphpoem.game.zw.resource.util.ActParamTabLoader;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

public class GlobalActivityAuctionData extends GlobalActivityData {
    private int planKeyId;
    /**
     * 竞拍物品信息
     */
    private Map<Integer, GlobalActivityAuctionItem> itemMap;

    /**
     * 轮数-状态
     */
    private Map<Integer, ActivityAuctionConst> roundStatus;

    /**
     * 参与者 key:itemId value:参与者idList
     */
    private Set<Long> participants;

    /**
     * 回合开始结束时间配置
     */
    private List<ActivityAuctionParam> params;

    /**
     * 初始化
     *
     * @param activityBase
     * @param begin
     */
    public GlobalActivityAuctionData(ActivityBase activityBase, int begin) {
        super(activityBase, begin);
        this.planKeyId = activityBase.getPlanKeyId();
        List<ActivityAuctionParam> paramsTmp = initActivityAuctionBase(activityBase);
        if (!ObjectUtils.isEmpty(paramsTmp)) {
            setParams(paramsTmp);
        }
    }

    public static void checkInit(GlobalActivityData activity, int keyId) {
        if (activity instanceof GlobalActivityAuctionData) {
            GlobalActivityAuctionData data = (GlobalActivityAuctionData) activity;
            data.checkInit(keyId);
        }
    }

    /**
     * 初始化拍卖回合时间数据
     *
     * @return
     */
    public static List<ActivityAuctionParam> initActivityAuctionBase(ActivityBase activityBase) {
        if (CheckNull.isNull(activityBase)) {
            return null;
        }

        List<List<Integer>> actAuctionTimeList = ActParamConstant.ACT_AUCTION_TIME_LIST;
        if (ObjectUtils.isEmpty(actAuctionTimeList)) {
            return null;
        }

        long startTime = 0l;
        int preHour = -1;
        Date auctionBeginTime = new Date(activityBase.getBeginTime().getTime());

        Calendar roundTime = Calendar.getInstance();
        List<ActivityAuctionParam> result = new ArrayList<>();
        for (List<Integer> actAuctionTime : actAuctionTimeList) {
            if (ObjectUtils.isEmpty(actAuctionTime) || actAuctionTime.size() != 2) {
                LogUtil.error("秋季拍卖配置出错! config:", ActParamConstant.ACT_AUCTION_TIME_LIST);
                continue;
            }

            try {
                if (actAuctionTime.get(1) < preHour) {
                    startTime = TimeHelper.getSomeDayAfter(auctionBeginTime, 1, 0, 0, 0);
                    auctionBeginTime.setTime(startTime * TimeHelper.SECOND_MS);
                }

                startTime = TimeHelper.getSomeDayAfter(auctionBeginTime, 0, actAuctionTime.get(1), 0, 0);
                ActivityAuctionParam activityAuctionParam = new ActivityAuctionParam();
                activityAuctionParam.setRound(actAuctionTime.get(0));
                roundTime.setTimeInMillis(startTime * TimeHelper.SECOND_MS);
                activityAuctionParam.setStartTime(roundTime.getTime());
                roundTime.setTimeInMillis((startTime + ActParamConstant.ACT_AUCTION_ROUND_PERIOD) * TimeHelper.SECOND_MS);
                activityAuctionParam.setEndTime(roundTime.getTime());
                roundTime.setTimeInMillis(activityAuctionParam.getEndTime().getTime() -
                        (ActParamConstant.ACT_AUCTION_ABOUT_TO_END * TimeHelper.SECOND_MS));
                activityAuctionParam.setAboutToEndTime(roundTime.getTime());

                result.add(activityAuctionParam);
            } catch (Exception e) {
                LogUtil.error("初始化秋季拍卖时间出错, e:", e);
                continue;
            } finally {
                preHour = actAuctionTime.get(1);
            }
        }

        result = ActivityAuctionParam.sort(result);
        return result;
    }

    public int getPlanKeyId() {
        return planKeyId;
    }

    public void setPlanKeyId(int planKeyId) {
        this.planKeyId = planKeyId;
    }

    /**
     * 校验定时器
     *
     * @param keyId
     * @return
     */
    public boolean checkInit(int keyId) {
        if (ObjectUtils.isEmpty(this.getParams())) {
            ActivityBase base = StaticActivityDataMgr.getActivityList().stream().
                    filter(activityBase -> activityBase.getPlanKeyId() == keyId).findFirst().get();
            List<ActivityAuctionParam> params = GlobalActivityAuctionData.initActivityAuctionBase(base);
            if (ObjectUtils.isEmpty(params)) {
                return false;
            }

            this.setParams(params);
        }

        this.setPlanKeyId(keyId);
        return true;
    }

    public Map<Integer, GlobalActivityAuctionItem> getItemMap() {
        if (CheckNull.isNull(this.itemMap)) {
            this.itemMap = new HashMap<>();
        }

        return itemMap;
    }

    public GlobalActivityAuctionItem getAuctionItem(int itemId) {
        GlobalActivityAuctionItem auctionItem = getItemMap().get(itemId);
        if (CheckNull.isNull(auctionItem)) {
            auctionItem = new GlobalActivityAuctionItem(itemId);
            getItemMap().put(itemId, auctionItem);
        }

        return auctionItem;
    }

    public void setItemMap(Map<Integer, GlobalActivityAuctionItem> itemMap) {
        this.itemMap = itemMap;
    }

    public Map<Integer, ActivityAuctionConst> getRoundStatus() {
        if (CheckNull.isNull(this.roundStatus)) {
            this.roundStatus = new HashMap<>();
        }

        return roundStatus;
    }

    public void setRoundStatus(Map<Integer, ActivityAuctionConst> roundStatus) {
        this.roundStatus = roundStatus;
    }

    public Set<Long> getParticipants() {
        if (CheckNull.isNull(participants)) {
            this.participants = new HashSet<>();
        }

        return participants;
    }

    public void setParticipants(Set<Long> participants) {
        this.participants = participants;
    }

    /**
     * 添加竞拍者，一口价拍卖下来的人不进入该列表
     *
     * @param lordId
     */
    public void addParticipant(long lordId) {
        this.getParticipants().add(lordId);
    }

    public void removeLargerRound(int currentRound, int round) {
        if (currentRound <= round) {
            return;
        }

        int tmpRound = ++round;
        for (int i = tmpRound; i <= currentRound; i++) {
            this.getRoundStatus().remove(i);
        }
    }

    /**
     * 获得当前轮数拍卖品
     *
     * @param list
     * @return
     */
    public Map<StaticActAuction, GlobalActivityAuctionItem> getRoundItem(List<StaticActAuction> list) {
        if (ObjectUtils.isEmpty(this.itemMap)) {
            return null;
        }

        Map<StaticActAuction, GlobalActivityAuctionItem> roundItemMap = new HashMap<>();
        list.forEach(staticActAuction -> {
            GlobalActivityAuctionItem item = this.getItemMap().get(staticActAuction.getId());
            if (CheckNull.isNull(item)) {
                return;
            }

            roundItemMap.put(staticActAuction, item);
        });

        return roundItemMap;
    }

    /**
     * 获得当前拍卖活动轮数
     *
     * @return
     */
    public CommonPb.TwoInt.Builder getCurrentRoundStatus() {
        if (ObjectUtils.isEmpty(roundStatus)) {
            return null;
        }

        CommonPb.TwoInt.Builder twoInt = CommonPb.TwoInt.newBuilder();
        Integer maxRound = roundStatus.keySet().stream().filter(e -> e != null).
                max(Comparator.naturalOrder()).orElse(null);
        twoInt.setV1(maxRound);
        twoInt.setV2(roundStatus.get(maxRound).getType());

        return twoInt;
    }

    public ActivityAuctionParam getParam(int round) {
        if (ObjectUtils.isEmpty(params)) {
            return null;
        }

        for (ActivityAuctionParam tmp : params) {
            if (tmp.getRound() == round) {
                return tmp;
            }
        }

        return null;
    }

    public List<ActivityAuctionParam> getParams() {
        if (ObjectUtils.isEmpty(params)) {
            this.params = new ArrayList<>();
        }

        return params;
    }

    public void setParams(List<ActivityAuctionParam> params) {
        this.params = params;
    }

    public CommonPb.TwoInt.Builder inRoundStatus(long now) {
        ActivityAuctionParam preParam = null;

        List<ActivityAuctionParam> list = this.params.stream().sorted(Comparator.comparingInt
                (ActivityAuctionParam::getRound).reversed()).collect(Collectors.toList());
        CommonPb.TwoInt.Builder roundStatus = CommonPb.TwoInt.newBuilder();
        for (int i = 0; i < list.size(); i++) {
            ActivityAuctionParam param = list.get(i);
            if (CheckNull.isNull(preParam)) {
                //处于最后一轮展示期
                if (now >= param.getEndTime().getTime()) {
                    roundStatus.setV1(param.getRound());
                    roundStatus.setV2(ActivityAuctionConst.ACT_END.getType());
                    return roundStatus;
                }
            } else {
                //处于两个轮数的展示期
                if (now < preParam.getStartTime().getTime() && now >= param.getEndTime().getTime()) {
                    roundStatus.setV1(preParam.getRound());
                    roundStatus.setV2(ActivityAuctionConst.ROUND_ON_DISPLAY.getType());
                    return roundStatus;
                }
            }

            if (now >= param.getStartTime().getTime() && now < param.getEndTime().getTime()) {
                roundStatus.setV1(param.getRound());
                roundStatus.setV2(ActivityAuctionConst.ON_SALE.getType());
                return roundStatus;
            }

            if (i == list.size() - 1) {
                if (now < param.getStartTime().getTime()) {
                    //第一轮展示期
                    roundStatus.setV1(param.getRound());
                    roundStatus.setV2(ActivityAuctionConst.ROUND_ON_DISPLAY.getType());
                    return roundStatus;
                }
            }

            preParam = param;
        }

        return null;
    }

    /**
     * 新的拍卖活动开始，清除上一次活动信息
     */
    public void beginNextAct() {
        getRoundStatus().clear();
        getItemMap().clear();
        getParticipants().clear();
        getParams().clear();
    }

    public void updateRoundStatus(int round, ActivityAuctionConst auctionConst) {
        getRoundStatus().put(round, auctionConst);
    }

    /**
     * 反序列化
     *
     * @param globalActivity 数据库数据
     * @throws InvalidProtocolBufferException
     */
    public GlobalActivityAuctionData(GlobalActivity globalActivity) throws InvalidProtocolBufferException {
        super(globalActivity);
        byte[] auction = globalActivity.getAuction();
        if (ObjectUtils.isEmpty(auction)) {
            return;
        }


        SerializePb.DbGlobalAuctionData auctionData = SerializePb.DbGlobalAuctionData.parseFrom(auction);
        this.planKeyId = auctionData.getPlanKeyId();
                itemMap = new HashMap<>();
        Optional.ofNullable(auctionData.getGlobalActivityAuctionItemList()).ifPresent(list -> list.forEach(item -> {
            if (CheckNull.isNull(item)) {
                return;
            }
            itemMap.put(item.getItemId(), GlobalActivityAuctionItem.deserialization(item));
        }));

        this.roundStatus = new HashMap<>();
        Optional.ofNullable(auctionData.getRoundStatusList()).ifPresent(roundStatus -> roundStatus.forEach(twoInt -> {
            if (CheckNull.isNull(twoInt)) {
                return;
            }
            this.roundStatus.put(twoInt.getV1(), ActivityAuctionConst.convertTo(twoInt.getV2()));
        }));

        this.participants = new HashSet<>();
        Optional.ofNullable(auctionData.getParticipantsList()).ifPresent(participantList -> this.setParticipants(new HashSet<>(participantList)));
    }

    /**
     * 序列化
     *
     * @return
     */
    @Override
    public GlobalActivity copyData() {
        GlobalActivity globalActivity = super.copyData();
        SerializePb.DbGlobalAuctionData.Builder builder = SerializePb.DbGlobalAuctionData.newBuilder();
        Optional.ofNullable(this.itemMap).ifPresent(items -> items.forEach((id, item) -> {
            builder.addGlobalActivityAuctionItem(item.serialization(true, 0));
        }));
        Optional.ofNullable(this.roundStatus).ifPresent(roundStatusMap -> roundStatusMap.forEach((round, status) -> {
            CommonPb.TwoInt.Builder twoIntPb = CommonPb.TwoInt.newBuilder();
            twoIntPb.setV1(round);
            twoIntPb.setV2(status.getType());
            builder.addRoundStatus(twoIntPb);
        }));
        builder.addAllParticipants(this.getParticipants());
        builder.setPlanKeyId(this.planKeyId);
        globalActivity.setAuction(builder.build().toByteArray());
        return globalActivity;
    }

    /**
     * 秋季拍卖获得下一轮活动
     *
     * @param round
     * @return
     */
    public ActivityAuctionParam getNextRound(int round) {
        if (ObjectUtils.isEmpty(this.getParams())) {
            return null;
        }

        boolean current = false;
        for (ActivityAuctionParam param : this.getParams()) {
            if (current) {
                return param;
            }
            if (param.getRound() == round) {
                current = true;
            }
        }

        return null;
    }

    public ActivityAuctionParam getLastRound(int round) {
        if (ObjectUtils.isEmpty(this.params)) {
            return null;
        }

        ActivityAuctionParam preParam = null;
        for (ActivityAuctionParam param : this.params) {
            if (param.getRound() == round) {
                return preParam;
            }

            preParam = param;
        }

        return null;
    }
}
