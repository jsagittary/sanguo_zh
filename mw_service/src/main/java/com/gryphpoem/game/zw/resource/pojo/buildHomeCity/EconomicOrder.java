package com.gryphpoem.game.zw.resource.pojo.buildHomeCity;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.domain.p.AwardItem;
import com.gryphpoem.game.zw.resource.util.PbHelper;

import java.util.List;
import java.util.Map;

/**
 * 玩家经济订单信息
 *
 * @Author: GeYuanpeng
 * @Date: 2022/11/15 18:11
 */
public class EconomicOrder {

    private int keyId;

    /**
     * 订单id, 对应订单表的id
     */
    private int orderId;

    /**
     * 订单品质：1-普通; 2-稀有; 3-传说
     */
    private int quantity;

    /**
     * 订单出处, area id和集市建筑id, [集市，酒泉郡城，武陵郡城，南阳郡城，会稽郡城，丹阳郡城，阳平郡城，常山郡城，敦煌郡城，武威，江夏，交趾，巨鹿，洛阳]
     */
    private int place;

    /**
     * 订单来源非集市时的占领势力
     */
    private int placeCamp;

    /**
     * 开始展示时间(不论是否预显示)
     */
    private int startTime;

    /**
     * 订单结束时间
     */
    private int endTime;

    /**
     * 预展示时间
     */
    private int preDisplay;

    /**
     * 订单需求  [[经济产物的道具id, 数量], [经济产物的道具id, 数量], [经济产物的道具id, 数量]]
     */
    private Map<Integer, Integer> orderDemand;

    /**
     * 订单奖励  [[rewardType, subType, count], [rewardType, subType, count], [rewardType, subType, count]] <br>
     * 第1个放特殊奖励
     */
    private List<AwardItem> reward;

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
    }

    public int getPlaceCamp() {
        return placeCamp;
    }

    public void setPlaceCamp(int placeCamp) {
        this.placeCamp = placeCamp;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public int getPreDisplay() {
        return preDisplay;
    }

    public void setPreDisplay(int preDisplay) {
        this.preDisplay = preDisplay;
    }

    public Map<Integer, Integer> getOrderDemand() {
        return orderDemand;
    }

    public void setOrderDemand(Map<Integer, Integer> orderDemand) {
        this.orderDemand = orderDemand;
    }

    public List<AwardItem> getReward() {
        return reward;
    }

    public void setReward(List<AwardItem> reward) {
        this.reward = reward;
    }

    public CommonPb.EconomicOrder createPb() {
        CommonPb.EconomicOrder.Builder builder = CommonPb.EconomicOrder.newBuilder();
        builder.setKeyId(this.keyId);
        builder.setOrderId(this.orderId);
        builder.setQuantity(this.quantity);
        builder.setPlace(this.place);
        builder.setStartTime(this.startTime);
        builder.setPreDisplay(this.preDisplay);
        builder.setEndTime(this.endTime);
        builder.addAllOrderDemand(PbHelper.createTwoIntListByMap(this.orderDemand));
        builder.addAllReward(PbHelper.createAwards(this.reward));
        builder.setPlaceCamp(this.placeCamp);
        return builder.build();
    }
}
