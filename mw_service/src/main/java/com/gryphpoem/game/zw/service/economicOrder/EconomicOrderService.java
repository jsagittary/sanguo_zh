package com.gryphpoem.game.zw.service.economicOrder;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticBuildCityDataMgr;
import com.gryphpoem.game.zw.gameplay.local.util.DelayInvokeEnvironment;
import com.gryphpoem.game.zw.gameplay.local.util.DelayQueue;
import com.gryphpoem.game.zw.manager.MsgDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.manager.WorldDataManager;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.AwardItem;
import com.gryphpoem.game.zw.resource.domain.s.StaticEconomicOrder;
import com.gryphpoem.game.zw.resource.pojo.buildHomeCity.EconomicOrder;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.RandomHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 经济订单相关
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/16 18:48
 */
@Service
public class EconomicOrderService implements DelayInvokeEnvironment {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private WorldDataManager worldDataManager;


    /**
     * 提交经济订单
     *
     * @param roleId
     * @param rq
     * @return
     */
    public GamePb1.SubmitEconomicOrderRs submitEconomicOrder(long roleId, GamePb1.SubmitEconomicOrderRq rq) {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Map<Integer, EconomicOrder> canSubmitOrderData = player.getCanSubmitOrderData();
        int keyId = rq.getKeyId();
        GamePb1.SubmitEconomicOrderRs.Builder builder = GamePb1.SubmitEconomicOrderRs.newBuilder();
        if (!canSubmitOrderData.containsKey(keyId)) {
            return builder.build();
        }

        EconomicOrder economicOrder = canSubmitOrderData.get(keyId);
        int endTime = economicOrder.getEndTime();
        if (TimeHelper.getCurrentSecond() < endTime) {
            throw new MwException(GameError.PARAM_ERROR, String.format("提交经济订单时, 该订单已过期, roleId:%s, orderKeyId:%s", roleId, keyId));
        }
        int place = economicOrder.getPlace();
        // TODO 除集市外，来自别的城池的订单，如果被本国势力占领，会有额外加成
        float addition = 0.00F;
        // 检测并扣除对应的经济作物, 并向客户端同步
        Map<Integer, Integer> orderDemand = economicOrder.getOrderDemand();
        orderDemand.forEach((cropId, cropCnt) -> {
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.PROP, cropId, cropCnt, AwardFrom.SUBMIT_ECONOMIC_ORDER, "");
        });
        // 获取对应的奖励
        List<AwardItem> reward = economicOrder.getReward();
        for (int i = 0; i < reward.size(); i++) {
            AwardItem awardItem = reward.get(i);
            int type = awardItem.getType();
            int id = awardItem.getId();
            int count = awardItem.getCount();
            if (i > 0) {
                // 特殊奖励不享受额外加成
                count = Math.round(count * addition);
            }
            rewardDataManager.sendRewardSignle(player, type, id, count, AwardFrom.SUBMIT_ECONOMIC_ORDER, "");
        }

        // 向玩家同步订单信息
        canSubmitOrderData.remove(keyId);
        GamePb1.SynEconomicOrderRs.Builder synBuilder = GamePb1.SynEconomicOrderRs.newBuilder();
        for (EconomicOrder tmp : canSubmitOrderData.values()) {
            synBuilder.addCanSubmitOrder(tmp.createPb());
        }
        BasePb.Base.Builder msg = PbHelper.createSynBase(GamePb1.SynEconomicOrderRs.EXT_FIELD_NUMBER, GamePb1.SynEconomicOrderRs.ext, synBuilder.build());
        MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));

        return builder.build();
    }

    /**
     * 给玩家随机新的预显示订单
     *
     * @param player
     */
    public void randomNewPreOrder(Player player, int randomCnt) {
        // GamePb1.SynEconomicOrderRs.Builder builder = GamePb1.SynEconomicOrderRs.newBuilder();

        /**
         * 如果有预显示订单, 预显示时间结束后, 刷为可提交订单;
         * 如果没有预显示订单, 且有空余订单栏位置, 则随机新的预显示订单, 不论空余订单栏位置有多少, 最多刷新两个
         */
        Map<Integer, EconomicOrder> canSubmitOrderData = player.getCanSubmitOrderData();
        Map<Integer, EconomicOrder> preDisplayOrder = player.getPreDisplayOrderData();
        int economicOrderMaxCnt = player.getEconomicOrderMaxCnt();
        int lordLv = player.lord.getLevel();
        if (canSubmitOrderData.size() >= economicOrderMaxCnt || preDisplayOrder.size() >= 2) {
            // 如果可提交的经济订单已达上限, 或者预显示订单已经有2个了, 则不随机新的订单
            return;
        }

        // 随机新的预显示订单
        int newOrderCnt = 2 -  preDisplayOrder.size();// 需要随机数量
        List<StaticEconomicOrder> staticEconomicOrderList = StaticBuildCityDataMgr.getStaticEconomicOrderList().stream()
                .filter(tmp -> lordLv >= tmp.getNeedLordLv().get(0) && lordLv <= tmp.getNeedLordLv().get(1))
                .collect(Collectors.toList());
        if (CheckNull.nonEmpty(staticEconomicOrderList)) {
            int cnt = 0;
            int now = TimeHelper.getCurrentSecond();
            for (StaticEconomicOrder sEconomicOrder : staticEconomicOrderList) {
                if (cnt >= newOrderCnt) {
                    break;
                }
                int weight = sEconomicOrder.getWeight();
                int orderId = sEconomicOrder.getId();
                if (weight <= 0) {
                    throw new MwException(GameError.CONFIG_NOT_FOUND, String.format("经济订单配置错误, 权重小于或等于0, orderId:%s", orderId));
                }
                boolean hit = RandomHelper.isHitRangeIn10000(weight);
                if (hit) {
                    Map<Integer, Integer> demandList = new HashMap<>(3);
                    List<List<Integer>> orderDemand1 = sEconomicOrder.getOrderDemand1();// [[经济产物的道具id1，经济产物的道具id2],[数量下限，数量上限]]
                    getEconomicOrderDemand(orderDemand1, orderId, demandList);
                    List<List<Integer>> orderDemand2 = sEconomicOrder.getOrderDemand2();
                    getEconomicOrderDemand(orderDemand2, orderId, demandList);
                    List<List<Integer>> orderDemand3 = sEconomicOrder.getOrderDemand3();
                    getEconomicOrderDemand(orderDemand3, orderId, demandList);

                    List<AwardItem> rewardList = new ArrayList<>();
                    List<List<Integer>> specialReward = sEconomicOrder.getSpecialReward();
                    if (CheckNull.isEmpty(specialReward)) {
                        throw new MwException(GameError.CONFIG_NOT_FOUND, String.format("经济订单配置错误, 特殊奖励配置格式错误, orderId:%s", orderId));
                    }
                    // 特殊奖励
                    int specialRewardIndex = RandomHelper.randomInSize(specialReward.size());
                    List<Integer> finalSpecialReward = specialReward.get(specialRewardIndex);
                    AwardItem specialAwardItem = new AwardItem();
                    specialAwardItem.setType(finalSpecialReward.get(0));
                    specialAwardItem.setId(finalSpecialReward.get(1));
                    specialAwardItem.setCount(finalSpecialReward.get(2));
                    rewardList.add(specialAwardItem);
                    // 普通奖励
                    List<Integer> randomRewardTypeIndexResult = new ArrayList<>(2);
                    List<List<Integer>> rewardTypeRange1 = sEconomicOrder.getReward1();// [[rewardType, subType], [rewardType, subType], [rewardType, subType]]
                    List<Integer> rewardNumberRange1 = sEconomicOrder.getNumber1();// [下限数量，上限数量]
                    getEconomicOrderReward(rewardTypeRange1, rewardNumberRange1, orderId, rewardList, randomRewardTypeIndexResult);
                    List<List<Integer>> rewardTypeRange2 = sEconomicOrder.getReward2();
                    List<Integer> rewardNumberRange2 = sEconomicOrder.getNumber2();
                    getEconomicOrderReward(rewardTypeRange2, rewardNumberRange2, orderId, rewardList, randomRewardTypeIndexResult);

                    EconomicOrder economicOrder = new EconomicOrder();
                    int keyId = player.maxKey();
                    economicOrder.setKeyId(keyId);
                    economicOrder.setOrderId(orderId);
                    economicOrder.setQuantity(sEconomicOrder.getQuantity());
                    List<Integer> placeRange = sEconomicOrder.getPlace();
                    if (CheckNull.isEmpty(placeRange)) {
                        throw new MwException(GameError.CONFIG_NOT_FOUND, String.format("经济订单配置错误, 订单出处配置格式错误, orderId:%s", orderId));
                    }
                    economicOrder.setPlace(placeRange.get(RandomHelper.randomInSize(placeRange.size())));
                    economicOrder.setStartTime(now);
                    economicOrder.setPreDisplay(sEconomicOrder.getPreDisplayTime());
                    economicOrder.setEndTime(now + sEconomicOrder.getPreDisplayTime() + sEconomicOrder.getDurationTime());
                    economicOrder.setOrderDemand(demandList);
                    economicOrder.setReward(rewardList);

                    // builder.addPreDisPlayOrder(economicOrder.createPb());
                    preDisplayOrder.put(keyId, economicOrder);

                    preDisplayOrder.put(keyId, economicOrder);
                    // 创建延时任务：检查玩家的预显示订单预显示时间是否结束, 如果结束, 则将其跟改为可提交的订单
                    DELAY_QUEUE.add(new RefreshEconomicOrderDelayRun(player, 1, keyId, now + sEconomicOrder.getPreDisplayTime()));
                    cnt++;
                }
            }
        }

        // BasePb.Base.Builder msg = PbHelper.createSynBase(GamePb1.SynEconomicOrderRs.EXT_FIELD_NUMBER, GamePb1.SynEconomicOrderRs.ext,
        //         builder.build());
        // MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
    }

    /**
     * 刷新玩家经济订单：将预显示刷到可提交; 可提交订单有效期过了，则从订单列表中移除
     */
    public void refreshEconomicOrder(Player player, int type, int keyId) {
        Map<Integer, EconomicOrder> canSubmitOrderData = player.getCanSubmitOrderData();
        Map<Integer, EconomicOrder> preDisplayOrderData = player.getPreDisplayOrderData();

        if (type == 1) {
            // 将预显示订单刷新到可提交订单
            EconomicOrder economicOrder = preDisplayOrderData.get(keyId);
            economicOrder.setPreDisplay(0);
            canSubmitOrderData.put(keyId, economicOrder);
            preDisplayOrderData.remove(keyId);
            // 创建可提交订单过期清除的延时任务
            DELAY_QUEUE.add(new RefreshEconomicOrderDelayRun(player, 2, keyId, economicOrder.getEndTime()));

            // 随机新的预显示订单
            randomNewPreOrder(player, 1);
        }

        if (type == 2) {
            // 清除除过期订单
            canSubmitOrderData.remove(keyId);
        }

        // 向玩家同步订单信息
        GamePb1.SynEconomicOrderRs.Builder builder = GamePb1.SynEconomicOrderRs.newBuilder();
        for (EconomicOrder economicOrder : canSubmitOrderData.values()) {
            builder.addCanSubmitOrder(economicOrder.createPb());
        }
        for (EconomicOrder economicOrder : preDisplayOrderData.values()) {
            builder.addPreDisPlayOrder(economicOrder.createPb());
        }
        BasePb.Base.Builder msg = PbHelper.createSynBase(GamePb1.SynEconomicOrderRs.EXT_FIELD_NUMBER, GamePb1.SynEconomicOrderRs.ext,
                builder.build());
        MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
    }

    /**
     * 获取订单需求
     *
     * @param orderDemandConfig
     * @param orderId
     * @param finalDemand
     */
    public void getEconomicOrderDemand(List<List<Integer>> orderDemandConfig, int orderId, Map<Integer, Integer> finalDemand) {
        if (CheckNull.isEmpty(orderDemandConfig) || orderDemandConfig.size() != 2) {
            throw new MwException(GameError.CONFIG_NOT_FOUND, String.format("经济订单配置错误, 订单需求格式错误, orderId:%s", orderId));
        }
        List<Integer> economicCropIdRange = orderDemandConfig.get(0);
        if (CheckNull.isEmpty(economicCropIdRange) || economicCropIdRange.size() != 2 || economicCropIdRange.get(0) >= economicCropIdRange.get(1)) {
            throw new MwException(GameError.CONFIG_NOT_FOUND, String.format("经济订单配置错误, 订单需求的经济作物id格式错误, orderId:%s", orderId));
        }
        List<Integer> economicCropNumRange = orderDemandConfig.get(1);
        if (CheckNull.isEmpty(economicCropNumRange) || economicCropNumRange.size() != 2 || economicCropNumRange.get(0) >= economicCropNumRange.get(1)) {
            throw new MwException(GameError.CONFIG_NOT_FOUND, String.format("经济订单配置错误, 订单需求的经济作物数量格式错误, orderId:%s", orderId));
        }
        // 获取需要的经济作物Id(不能重复)
        int economicCropId = RandomHelper.randomInArea(economicCropIdRange.get(0), economicCropIdRange.get(1) + 1);
        while (finalDemand.containsKey(economicCropId)) {
            int i = RandomHelper.randomInSize(2);
            if (i == 0) {
                economicCropId = RandomHelper.randomInArea(economicCropIdRange.get(0), economicCropId);
            } else {
                economicCropId = RandomHelper.randomInArea(economicCropId + 1, economicCropIdRange.get(1) + 1);
            }
        }

        int economicCropNum = RandomHelper.randomInArea(economicCropNumRange.get(0), economicCropNumRange.get(1) + 1);
        finalDemand.put(economicCropId, economicCropNum);
    }

    /**
     * 获取订单奖励
     *
     * @param rewardTypeRange
     * @param rewardNumberRange
     * @param orderId
     * @param finalAwardItems
     * @param randomRewardTypeIndexResult
     */
    public void getEconomicOrderReward(List<List<Integer>> rewardTypeRange, List<Integer> rewardNumberRange, int orderId, List<AwardItem> finalAwardItems, List<Integer> randomRewardTypeIndexResult) {
        if (CheckNull.isEmpty(rewardTypeRange)) {
            throw new MwException(GameError.CONFIG_NOT_FOUND, String.format("经济订单配置错误, 订单奖励类型范围格式错误, orderId:%s", orderId));
        }
        if (CheckNull.isEmpty(rewardNumberRange) || rewardNumberRange.size() != 2 || rewardNumberRange.get(0) >= rewardNumberRange.get(1)) {
            throw new MwException(GameError.CONFIG_NOT_FOUND, String.format("经济订单配置错误, 订单奖励数量范围格式错误, orderId:%s", orderId));
        }

        // 随机奖励, 并保证最终奖励列表不存在重复奖励
        int typeIndex = RandomHelper.randomInSize(rewardTypeRange.size()); // 随不到最大size, 不会索引越界
        while (randomRewardTypeIndexResult.contains(typeIndex)) {
            int i = RandomHelper.randomInSize(2);
            if (i == 0) {
                typeIndex = RandomHelper.randomInSize(typeIndex);
            } else {
                typeIndex = RandomHelper.randomInArea(typeIndex + 1, rewardTypeRange.size());
            }
        }
        List<Integer> rewardType = rewardTypeRange.get(typeIndex);
        if (CheckNull.isEmpty(rewardType) || rewardType.size() != 2) {
            throw new MwException(GameError.CONFIG_NOT_FOUND, String.format("经济订单配置错误, 订单奖励类型格式错误, orderId:%s", orderId));
        }

        int rewardNum = RandomHelper.randomInArea(rewardNumberRange.get(0), rewardNumberRange.get(1) + 1);
        AwardItem awardItem = new AwardItem();
        awardItem.setType(rewardType.get(0));
        awardItem.setId(rewardType.get(1));
        awardItem.setCount(rewardNum);
        finalAwardItems.add(awardItem);
        randomRewardTypeIndexResult.add(typeIndex);
    }

    private DelayQueue<RefreshEconomicOrderDelayRun> DELAY_QUEUE = new DelayQueue<>(this);

    @Override
    public DelayQueue getDelayQueue() {
        return DELAY_QUEUE;
    }
}
