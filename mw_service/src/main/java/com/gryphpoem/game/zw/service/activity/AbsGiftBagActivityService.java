package com.gryphpoem.game.zw.service.activity;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.manager.ActivityDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb3;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.s.StaticPay;
import com.gryphpoem.game.zw.resource.domain.s.StaticPromotion;
import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.Map;
import java.util.Objects;

public interface AbsGiftBagActivityService {
    /**
     * 获取礼包活动信息
     *
     * @param player
     * @param activity
     * @return
     */
    default CommonPb.ActGiftBagInfo.Builder getActGiftBagInfoPb(Player player, Activity activity) {
        return CommonPb.ActGiftBagInfo.newBuilder();
    }

    /**
     * 礼包购买成功操作
     *
     * @param player
     * @param sPay
     */
    static void buyActGiftBag(Player player, StaticPay sPay) {
        Map<String, AbsGiftBagActivityService> resultMap = DataResource.ac.getBeansOfType(AbsGiftBagActivityService.class);
        if (CheckNull.isEmpty(resultMap))
            return;

        resultMap.values().forEach(service -> {
            service.processGiftBag(player, sPay);
        });
    }

    /**
     * 购买礼包后续操作
     *
     * @param player
     * @param sPay
     */
    void processGiftBag(Player player, StaticPay sPay);

    /**
     * 判断活动是否是礼包活动
     *
     * @param actType
     * @return
     */
    static boolean isActGiftBagAct(int actType) {
        Map<String, AbsGiftBagActivityService> resultMap = DataResource.ac.getBeansOfType(AbsGiftBagActivityService.class);
        if (CheckNull.isEmpty(resultMap))
            return false;

        for (AbsGiftBagActivityService service : resultMap.values()) {
            if (service instanceof AbsActivityService) {
                AbsActivityService absService = (AbsActivityService) service;
                for (int type : absService.getActivityType()) {
                    if (type == actType)
                        return true;
                }
            }
        }

        return false;
    }

    /**
     * 钻石购买礼包
     *
     * @param player
     * @param promotion
     * @return
     */
    static GamePb3.PromotionPropBuyRs buyActGiftBagByGameMoney(Player player, StaticPromotion promotion) {
        Map<String, AbsGiftBagActivityService> resultMap = DataResource.ac.getBeansOfType(AbsGiftBagActivityService.class);
        if (CheckNull.isEmpty(resultMap))
            return null;

        for (AbsGiftBagActivityService service : resultMap.values()) {
            GamePb3.PromotionPropBuyRs builder = service.processActGiftBagByGameMoney(player, promotion);
            if (Objects.nonNull(builder)) {
                return builder;
            }
        }

        return null;
    }

    GamePb3.PromotionPropBuyRs processActGiftBagByGameMoney(Player player, StaticPromotion promotion);

    /**
     * 获取礼包活动信息
     *
     * @param actType
     * @param staticPromotion
     * @param player
     * @return
     */
    default Activity getGiftBagAct(int actType, StaticPromotion staticPromotion, Player player) {
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(actType);
        if (Objects.isNull(activityBase)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 活动未开启 roleId:", player.roleId,
                    " keyId:", staticPromotion.getPromotionId());
        }
        Activity activity = DataResource.ac.getBean(ActivityDataManager.class).getActivityInfo(player, actType);
        if (Objects.isNull(activity)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), " 活动未开启 roleId:", player.roleId,
                    " keyId:", staticPromotion.getPromotionId());
        }

        return activity;
    }

    /**
     * 校验购买次数
     *
     * @param activity
     * @param promotion
     * @param player
     */
    void checkBuyCount(Activity activity, StaticPromotion promotion, Player player);
}

