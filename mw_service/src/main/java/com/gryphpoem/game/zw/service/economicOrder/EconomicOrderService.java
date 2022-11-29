package com.gryphpoem.game.zw.service.economicOrder;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticBuildCityDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticBuildingDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.manager.MsgDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.manager.WorldDataManager;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.constant.BuildingType;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.AwardItem;
import com.gryphpoem.game.zw.resource.domain.s.StaticBuildingInit;
import com.gryphpoem.game.zw.resource.domain.s.StaticCity;
import com.gryphpoem.game.zw.resource.domain.s.StaticEconomicOrder;
import com.gryphpoem.game.zw.resource.pojo.buildHomeCity.BuildingState;
import com.gryphpoem.game.zw.resource.pojo.buildHomeCity.EconomicOrder;
import com.gryphpoem.game.zw.resource.pojo.world.Area;
import com.gryphpoem.game.zw.resource.pojo.world.City;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.RandomHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.BuildingService;
import com.gryphpoem.game.zw.service.GmCmd;
import com.gryphpoem.game.zw.service.GmCmdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
public class EconomicOrderService implements GmCmdService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private WorldDataManager worldDataManager;

    /**
     * 获取玩家经济订单信息
     *
     * @param roleId
     * @return
     */
    public GamePb1.GetEconomicOrderRs getEconomicOrder(long roleId) {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        // 校验玩家集市是否解锁
        if (player.building.getMall() <= 0) {
            throw new MwException(GameError.FOUNDATION_NOT_UNLOCK, String.format("获取经济订单信息时, 集市建筑未解锁, roleId:%s", roleId));
        }

        // 刷新下玩家的订单(可能会有新的订单刷新)
        randomNewPreOrder(player);
        GamePb1.GetEconomicOrderRs.Builder builder = GamePb1.GetEconomicOrderRs.newBuilder();
        Map<Integer, EconomicOrder> canSubmitOrderData = player.getCanSubmitOrderData();
        Map<Integer, EconomicOrder> preDisplayOrderData = player.getPreDisplayOrderData();
        for (EconomicOrder economicOrder : canSubmitOrderData.values()) {
            builder.addCanSubmitOrder(economicOrder.createPb());
        }
        for (EconomicOrder economicOrder : preDisplayOrderData.values()) {
            builder.addPreDisPlayOrder(economicOrder.createPb());
        }

        return builder.build();
    }

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
        if (TimeHelper.getCurrentSecond() > endTime) {
            throw new MwException(GameError.PARAM_ERROR, String.format("提交经济订单时, 该订单已过期, roleId:%s, orderKeyId:%s", roleId, keyId));
        }
        // 除集市外，来自别的城池的订单，如果被本国势力占领，会有额外加成
        int place = economicOrder.getPlace();
        int orderAdditionCoefficient = Constant.ORDER_ADDITION_BY_SAME_CAMP_PLACE;
        float addition = 1.00F;
        if (place != BuildingType.WHARF){
            int placeCamp = economicOrder.getPlaceCamp();
            int playerCamp = player.lord.getCamp();
            if (placeCamp == playerCamp) { // 同阵营
                addition += orderAdditionCoefficient / Constant.TEN_THROUSAND;
            }
        }
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
        randomNewPreOrder(player);
        synEconomicOrderChange(player);

        return builder.build();
    }

    /**
     * 给玩家随机新的预显示订单
     *
     * @param player
     */
    public int randomNewPreOrder(Player player) {
        /**
         * 如果有预显示订单, 预显示时间结束后, 刷为可提交订单;
         * 如果没有预显示订单, 且有空余订单栏位置, 则随机新的预显示订单, 不论空余订单栏位置有多少, 最多刷新两个
         */
        Map<Integer, EconomicOrder> canSubmitOrderData = player.getCanSubmitOrderData();
        int economicOrderMaxCnt = player.getEconomicOrderMaxCnt();
        int lordLv = player.lord.getLevel();
        // 如果可提交的经济订单已达上限, 则不随机新的订单
        if (canSubmitOrderData.size() >= economicOrderMaxCnt) {
            return 0;
        }
        // 预显示订单已经有2个了, 则不随机新的订单
        Map<Integer, EconomicOrder> preDisplayOrder = player.getPreDisplayOrderData();
        if (preDisplayOrder.size() >= 2) {
            return 0;
        }

        int randomCnt = 2 - preDisplayOrder.size();
        List<Integer> canUnlockEconomicCropIds = StaticBuildCityDataMgr.getCanUnlockEconomicCropIds(player.lord.getLevel());
        // 随机新的预显示订单
        List<StaticEconomicOrder> sEconomicOrderList = StaticBuildCityDataMgr.getStaticEconomicOrderList().stream()
                .filter(tmp -> lordLv >= tmp.getNeedLordLv().get(0) && lordLv <= tmp.getNeedLordLv().get(1))
                .collect(Collectors.toList());
        if (CheckNull.nonEmpty(sEconomicOrderList)) {
            int cnt = 0;
            int now = TimeHelper.getCurrentSecond();
            for (StaticEconomicOrder sEconomicOrder : sEconomicOrderList) {
                if (cnt >= randomCnt) {
                    break;
                }
                int orderId = sEconomicOrder.getId();
                int weight = sEconomicOrder.getWeight();
                if (weight < 0) {
                    throw new MwException(GameError.CONFIG_NOT_FOUND, String.format("经济订单配置错误, 权重小于0, orderId:%s", orderId));
                }
                int quantity = sEconomicOrder.getQuantity();
                // 计算内政属性加成, 品质3传说的权重增加，品质1普通的权重减少
                Map<Integer, BuildingState> buildingData = player.getBuildingData();
                BuildingState buildingState = buildingData.values().stream()
                        .filter(tmp -> tmp.getBuildingType() == BuildingType.MALL)
                        .findFirst()
                        .orElse(null);
                if (buildingState == null) {
                    StaticBuildingInit sBuildingInit = StaticBuildingDataMgr.getBuildingInitMapById(BuildingType.MALL);
                    buildingState = new BuildingState(sBuildingInit.getBuildingId(), BuildingType.MALL);
                    buildingState.setBuildingLv(sBuildingInit.getInitLv());
                    player.getBuildingData().put(sBuildingInit.getBuildingId(), buildingState);
                }
                int interiorEffect = DataResource.ac.getBean(BuildingService.class).calculateInteriorEffect(player, BuildingType.MALL);
                switch (quantity) {
                    case 1:
                        weight -= interiorEffect;
                        break;
                    case 3:
                        weight += interiorEffect;
                        break;
                }
                boolean hit = RandomHelper.isHitRangeIn10000(weight);
                if (hit) {
                    Map<Integer, Integer> demandList = new HashMap<>(3);
                    List<List<Integer>> orderDemand1 = sEconomicOrder.getOrderDemand1();// [[作物档位1, 作物档位1],[数量下限，数量上限]]
                    getEconomicOrderDemand(orderDemand1, orderId, demandList, canUnlockEconomicCropIds);
                    List<List<Integer>> orderDemand2 = sEconomicOrder.getOrderDemand2();
                    getEconomicOrderDemand(orderDemand2, orderId, demandList, canUnlockEconomicCropIds);
                    List<List<Integer>> orderDemand3 = sEconomicOrder.getOrderDemand3();
                    getEconomicOrderDemand(orderDemand3, orderId, demandList, canUnlockEconomicCropIds);

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
                    List<List<Integer>> rewardTypeRange1 = sEconomicOrder.getReward1();// [[rewardType, subType], [rewardType, subType], [rewardType, subType]]
                    List<Integer> rewardNumberRange1 = sEconomicOrder.getNumber1();// [下限数量，上限数量]
                    getEconomicOrderReward(rewardTypeRange1, rewardNumberRange1, orderId, rewardList);
                    List<List<Integer>> rewardTypeRange2 = sEconomicOrder.getReward2();
                    List<Integer> rewardNumberRange2 = sEconomicOrder.getNumber2();
                    getEconomicOrderReward(rewardTypeRange2, rewardNumberRange2, orderId, rewardList);

                    EconomicOrder economicOrder = new EconomicOrder();
                    int keyId = player.maxKey();
                    economicOrder.setKeyId(keyId);
                    economicOrder.setOrderId(orderId);
                    economicOrder.setQuantity(quantity);
                    List<Integer> placeRange = sEconomicOrder.getPlace(); // 订单来源处 [城池id]
                    if (CheckNull.isEmpty(placeRange)) {
                        throw new MwException(GameError.CONFIG_NOT_FOUND, String.format("经济订单配置错误, 订单出处配置格式错误, orderId:%s", orderId));
                    }
                    Collections.shuffle(placeRange);
                    int place = 0;
                    List<Integer> openArea = worldDataManager.getOpenArea().stream().map(Area::getArea).collect(Collectors.toList());
                    for (Integer placeId : placeRange) {
                        // 如果订单来源城池所在区域未解锁, 则不可随机
                        StaticCity staticCity = StaticWorldDataMgr.getCityMap().get(placeId);
                        if (placeId != BuildingType.MALL && !openArea.contains(staticCity.getArea())) {
                            continue;
                        }
                        place = placeId;
                        break;
                    }
                    if (place == 0) {
                        throw new MwException(GameError.CONFIG_NOT_FOUND, String.format("经济订单配置错误, 获取不到合法订单来源处, orderId:%s", orderId));
                    }
                    economicOrder.setPlace(place);
                    if (place != BuildingType.MALL) {
                        // 来源非集市时, 判断其占领势力
                        City city = worldDataManager.getCityById(place);
                        economicOrder.setPlaceCamp(city.getCamp());
                        // TODO 世界地图中各区块占领阵营发生变更时, 同步更新玩家订单中的出处阵营信息
                    } else {
                        economicOrder.setPlaceCamp(4);
                    }
                    economicOrder.setStartTime(now);
                    economicOrder.setPreDisplay(sEconomicOrder.getPreDisplayTime());
                    economicOrder.setEndTime(now + sEconomicOrder.getPreDisplayTime() + sEconomicOrder.getDurationTime());
                    economicOrder.setOrderDemand(demandList);
                    economicOrder.setReward(rewardList);

                    preDisplayOrder.put(keyId, economicOrder);
                    // 创建延时任务：检查玩家的预显示订单预显示时间是否结束, 如果结束, 则将其跟改为可提交的订单
                    // DELAY_QUEUE.add(new RefreshEconomicOrderDelayRun(player, 1, keyId, now + sEconomicOrder.getPreDisplayTime()));
                    cnt++;
                }
            }
        }

        return randomCnt;
    }

    /**
     * 刷新玩家经济订单：将预显示刷到可提交; 可提交订单有效期过了，则从订单列表中移除
     */
    public void refreshEconomicOrder() {
        Iterator<Player> iterator = playerDataManager.getPlayers().values().iterator();
        int now = TimeHelper.getCurrentSecond();
        while (iterator.hasNext()) {
            Player player = iterator.next();
            try {
                boolean synFlag = false;
                Map<Integer, EconomicOrder> preDisplayOrderData = player.getPreDisplayOrderData();
                Map<Integer, EconomicOrder> canSubmitOrderData = player.getCanSubmitOrderData();

                Map<Integer, EconomicOrder> newPreSubmitOrderData = new HashMap<>();
                for (EconomicOrder economicOrder : preDisplayOrderData.values()) {
                    if (economicOrder.getStartTime() + economicOrder.getPreDisplay() < now) {
                        // 预显示订单更改为可提交订单
                        economicOrder.setPreDisplay(0);
                        canSubmitOrderData.put(economicOrder.getKeyId(), economicOrder);
                    } else {
                        newPreSubmitOrderData.put(economicOrder.getKeyId(), economicOrder);
                    }
                    synFlag = true;
                }
                player.setPreDisplayOrderData(newPreSubmitOrderData);

                List<EconomicOrder> economicOrderList = canSubmitOrderData.values().stream()
                        .filter(tmp -> tmp.getEndTime() >= now)
                        .collect(Collectors.toList());
                canSubmitOrderData.clear();
                for (EconomicOrder economicOrder : economicOrderList) {
                    canSubmitOrderData.put(economicOrder.getKeyId(), economicOrder);
                }
                synFlag = randomNewPreOrder(player) > 0;

                if (synFlag) {
                    synEconomicOrderChange(player);
                }
            } catch (Exception e) {
                LogUtil.error("经济订单定时器报错, lordId:" + player.lord.getLordId(), e);
            }
        }

    }

    /**
     * 向玩家同步订单信息
     *
     * @param player
     */
    public void synEconomicOrderChange(Player player) {
        GamePb1.SynEconomicOrderRs.Builder builder = GamePb1.SynEconomicOrderRs.newBuilder();
        Map<Integer, EconomicOrder> canSubmitOrderData = player.getCanSubmitOrderData();
        Map<Integer, EconomicOrder> preDisplayOrderData = player.getPreDisplayOrderData();
        for (EconomicOrder economicOrder : canSubmitOrderData.values()) {
            builder.addCanSubmitOrder(economicOrder.createPb());
        }
        for (EconomicOrder economicOrder : preDisplayOrderData.values()) {
            builder.addPreDisPlayOrder(economicOrder.createPb());
        }
        BasePb.Base.Builder msg = PbHelper.createSynBase(GamePb1.SynEconomicOrderRs.EXT_FIELD_NUMBER, GamePb1.SynEconomicOrderRs.ext, builder.build());
        MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
    }

    /**
     * 获取订单需求
     *
     * @param orderDemandConfig
     * @param orderId
     * @param finalDemand
     */
    public void getEconomicOrderDemand(List<List<Integer>> orderDemandConfig,
                                       int orderId,
                                       Map<Integer, Integer> finalDemand,
                                       List<Integer> canUnlockEconomicCropIds) {
        if (CheckNull.isEmpty(orderDemandConfig) || orderDemandConfig.size() != 2) {
            throw new MwException(GameError.CONFIG_NOT_FOUND, String.format("经济订单配置错误, 订单需求格式错误, orderId:%s", orderId));
        }
        List<Integer> cropQualityRange = orderDemandConfig.get(0);
        if (CheckNull.isEmpty(cropQualityRange)) {
            throw new MwException(GameError.CONFIG_NOT_FOUND, String.format("经济订单配置错误, 订单需求的经济作物id格式错误, orderId:%s", orderId));
        }
        List<Integer> economicCropNumRange = orderDemandConfig.get(1);
        if (CheckNull.isEmpty(economicCropNumRange) || economicCropNumRange.size() != 2 || economicCropNumRange.get(0) >= economicCropNumRange.get(1)) {
            throw new MwException(GameError.CONFIG_NOT_FOUND, String.format("经济订单配置错误, 订单需求的经济作物数量格式错误, orderId:%s", orderId));
        }
        // 获取需要的经济作物Id(不能重复)
        int finalEconomicCropId = 0;
        /*Collections.shuffle(cropQualityRange);
        for (Integer economicId : cropQualityRange) {
            if (finalDemand.containsKey(economicId)) {
                continue;
            }
            finalEconomicCropId = economicId;
        }*/
        Collections.shuffle(cropQualityRange);
        List<Integer> cropIdsByQuality = StaticBuildCityDataMgr.getEconomicCropIdsByQuality(cropQualityRange.get(0));
        List<Integer> intersection = canUnlockEconomicCropIds.stream().filter(cropIdsByQuality::contains).collect(Collectors.toList());
        intersection.removeAll(finalDemand.keySet());
        Collections.shuffle(intersection);
        if (CheckNull.nonEmpty(intersection)) {
            finalEconomicCropId = intersection.get(0);
        }

        if (finalEconomicCropId == 0) {
            throw new MwException(GameError.CONFIG_NOT_FOUND, String.format("经济订单配置错误, 未随机到合适的作物id, orderId:%s", orderId));
        }

        int economicCropNum = RandomHelper.randomInArea(economicCropNumRange.get(0), economicCropNumRange.get(1) + 1);
        finalDemand.put(finalEconomicCropId, economicCropNum);
    }

    /**
     * 获取订单奖励
     *
     * @param rewardTypeRange
     * @param rewardNumberRange
     * @param orderId
     * @param finalAwardItems
     */
    public void getEconomicOrderReward(List<List<Integer>> rewardTypeRange,
                                       List<Integer> rewardNumberRange,
                                       int orderId,
                                       List<AwardItem> finalAwardItems) {
        if (CheckNull.isEmpty(rewardTypeRange)) {
            throw new MwException(GameError.CONFIG_NOT_FOUND, String.format("经济订单配置错误, 订单奖励类型范围格式错误, orderId:%s", orderId));
        }
        if (CheckNull.isEmpty(rewardNumberRange)) {
            throw new MwException(GameError.CONFIG_NOT_FOUND, String.format("经济订单配置错误, 订单奖励数量范围格式错误, orderId:%s", orderId));
        }

        // 随机奖励, 并保证最终奖励列表不存在重复奖励
        Collections.shuffle(rewardTypeRange);
        List<Integer> rewardType = null;
        for (List<Integer> randomRewardType : rewardTypeRange) {
            if (CheckNull.isEmpty(randomRewardType) || randomRewardType.size() != 2) {
                throw new MwException(GameError.CONFIG_NOT_FOUND, String.format("经济订单配置错误, 订单奖励类型格式错误, orderId:%s", orderId));
            }
            boolean existInResult = finalAwardItems.stream().anyMatch(tmp -> tmp.getType() == randomRewardType.get(0) && tmp.getId() == randomRewardType.get(1));
            if (existInResult) {
                continue;
            }
            rewardType = randomRewardType;
            break;
        }
        if (CheckNull.isEmpty(rewardType)) {
            throw new MwException(GameError.CONFIG_NOT_FOUND, String.format("经济订单配置错误, 订单奖励类型格式错误, orderId:%s", orderId));
        }
        int rewardNum = rewardNumberRange.get(RandomHelper.randomInSize(rewardNumberRange.size()));
        AwardItem awardItem = new AwardItem();
        awardItem.setType(rewardType.get(0));
        awardItem.setId(rewardType.get(1));
        awardItem.setCount(rewardNum);
        finalAwardItems.add(awardItem);
    }

    // private DelayQueue<RefreshEconomicOrderDelayRun> DELAY_QUEUE = new DelayQueue<>(this);
    //
    // @Override
    // public DelayQueue getDelayQueue() {
    //     return DELAY_QUEUE;
    // }

    @GmCmd("economicOrder")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {
        switch (params[0]) {
            case "clearAllOrder":
                // 重置所有已探索的格子、已开垦的地基、已解锁的建筑
                player.getCanSubmitOrderData().clear();
                player.getPreDisplayOrderData().clear();
                break;
            default:
        }
    }
}
