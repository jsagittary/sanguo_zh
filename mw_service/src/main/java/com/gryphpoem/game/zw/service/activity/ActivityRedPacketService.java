package com.gryphpoem.game.zw.service.activity;

import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.manager.ActivityDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.resource.constant.ActParamConstant;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.service.RedPacketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 处理红包活动的类
 */
@Component
public class ActivityRedPacketService {


    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private RedPacketService redPacketService;

    /**
     * 活动里面存储着充值数量
     */
    private final static int chargeAllId = -1;


    /**
     * 充值调用
     *
     * @param player
     * @param chargeNum
     */
    public void redPacketActivity(Player player, int chargeNum) {

        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(ActivityConst.ACT_RED_PACKET);
        if (activityBase == null) {
            return;
        }

        Activity activity = activityDataManager.getActivityInfo(player, activityBase.getActivityType());
        if (activity == null) {
            return;
        }

        //这个总充值金额 活动期间每天重置 所以要存 存储到 propMap 中
        Map<Integer, Integer> propMap = activity.getPropMap();
        if (!propMap.containsKey(chargeAllId)) {
            propMap.put(chargeAllId, 0);
        }
        //充值总金额
        int chargeAll = propMap.get(chargeAllId) + chargeNum;
        propMap.put(chargeAllId, chargeAll);

        List<List<Integer>> actRedPacket = ActParamConstant.ACT_RED_PACKET;

        actRedPacket.forEach(list -> {
            int re = list.get(0);
            int redId = list.get(1);

            //说明需要发红包了
            if (chargeAll >= re && !propMap.containsKey(redId)) {
                propMap.put(redId, chargeAll);
                redPacketService.sendSysRedPacket(redId, activityBase.getActivityType(), player.lord.getNick(),player.lord.getPortrait()+"",re+"");
            }

        });

    }


    /**
     * 获取充值数量
     *
     * @param player
     * @return
     */
    public int getRedPacketActivityRecharge(Player player) {

        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(ActivityConst.ACT_RED_PACKET);
        if (activityBase == null) {
            return 0;
        }

        Activity activity = activityDataManager.getActivityInfo(player, activityBase.getActivityType());
        if (activity == null) {
            return 0;
        }
        Map<Integer, Integer> propMap = activity.getPropMap();
        if (!propMap.containsKey(chargeAllId)) {
            return 0;
        }
        //充值总金额
        return propMap.get(chargeAllId);
    }
}
