package com.gryphpoem.game.zw.resource.pojo.buildHomeCity;

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
     * 订单需求  [<经济产物的道具id, 数量>, <经济产物的道具id, 数量>, <经济产物的道具id, 数量>]
     */
    private List<Map<Integer, Integer>> orderDemand;

    /**
     * 订单奖励  [[rewardType, subType, count], [rewardType, subType, count], [rewardType, subType, count]] <br>
     * 第1个放特殊奖励
     */
    private List<List<Integer>> reward;

}
