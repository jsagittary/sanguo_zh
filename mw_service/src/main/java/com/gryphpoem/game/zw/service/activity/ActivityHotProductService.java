package com.gryphpoem.game.zw.service.activity;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.manager.ActivityDataManager;
import com.gryphpoem.game.zw.manager.MailDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb3;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.s.StaticActHotProduct;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 热销商品活动
 *
 * @program: empire_en
 * @description:
 * @author: zhou jie
 * @create: 2020-08-25 16:19
 */
@Service
public class ActivityHotProductService {

    @Autowired
    private RewardDataManager rewardDataManager;

    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private ActivityDataManager activityDataManager;

    @Autowired
    private MailDataManager mailDataManager;

    /**
     * 购买热销礼包, 或者领取消耗奖励
     *
     * @param roleId 角色Id
     * @param keyId  配置的keyId
     * @return 响应信息
     * @throws MwException 自定义异常
     */
    public GamePb4.ActHotProductAwardRs actHotProductAward(long roleId, int keyId) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        StaticActHotProduct sConf = StaticActivityDataMgr.getActHotProductByKey(keyId);
        if (Objects.isNull(sConf)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "找不到配置, roleId:,", roleId);
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(ActivityConst.ACT_HOT_PRODUCT);
        if (activityBase == null || activityBase.getPlan() == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启");
        }
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_HOT_PRODUCT);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启, roleId:,", roleId, ", type:", ActivityConst.ACT_HOT_PRODUCT);
        }
        if (sConf.getActivityId() != activity.getActivityId()) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "找不到配置, roleId:,", roleId);
        }
        GamePb4.ActHotProductAwardRs.Builder builder = GamePb4.ActHotProductAwardRs.newBuilder();
        int tab = sConf.getTab();
        if (tab == ActivityConst.ActHotProduct.STATUS_BUY_COUNT) {
            // 购买
            int buyCnt = activity.getStatusMap().getOrDefault(keyId, 0);
            if (buyCnt >= sConf.getTime()) {
                throw new MwException(GameError.ACT_HOT_PRODUCT_ALREADY_MAX_CNT.getCode(), "活动商品购买已经达到次数上限, roleId:", roleId, ", keyId:", keyId);
            }
            int count = activityDataManager.currentActivity(player, activity, ActivityConst.ActHotProduct.STATUS_BUY_COUNT);
            // 折扣后的价格
            int price = (int) Math.ceil(sConf.getPrice() * (getDiscountByCount(count, sConf) / Constant.TEN_THROUSAND));
            // 检测并扣除金币
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, price, AwardFrom.ACT_HOT_PRODUCT_BUY_COST);
            // 更新购买礼包的钻石消耗
            activityDataManager.updActivity(player, ActivityConst.ACT_HOT_PRODUCT, price, ActivityConst.ActHotProduct.STATUS_SPEND_SUM, false);
            // 更新领取次数
            activityDataManager.updActivity(player, ActivityConst.ACT_HOT_PRODUCT, 1, ActivityConst.ActHotProduct.STATUS_BUY_COUNT, false);
            // 当前礼包购买次数
            activity.getStatusMap().put(keyId, buyCnt + 1);
            Optional.ofNullable(rewardDataManager.addAwardDelaySync(player, sConf.getAwardList(), null, AwardFrom.ACT_HOT_PRODUCT_BUY_AWARD))
                    .ifPresent(builder::addAllAward);
            builder.setBuyCnt(activity.getStatusMap().getOrDefault(keyId, 0));
        } else if (tab == ActivityConst.ActHotProduct.STATUS_SPEND_SUM) {
            // 领取消耗奖励
            if (activity.getStatusMap().containsKey(keyId)) {
                throw new MwException(GameError.AWARD_HAD_GOT.getCode(), "已领取奖励, roleId:,", roleId);
            }
            int count = activityDataManager.currentActivity(player, activity, ActivityConst.ActHotProduct.STATUS_SPEND_SUM);
            if (count < sConf.getSpend()) {
                throw new MwException(GameError.ACT_HOT_PRODUCT_NOT_ENOUGH_SPEND.getCode(), ", 活动领取限制未达到, roleId:", roleId, ", keyId:", keyId, ", spend:", sConf.getSpend(), ", count:", count);
            }
            Optional.ofNullable(rewardDataManager.addAwardDelaySync(player, sConf.getAwardList(), null, AwardFrom.ACT_HOT_PRODUCT_SPEND_AWARD))
                    .ifPresent(builder::addAllAward);
            activity.getStatusMap().put(keyId, 1);
        }
        return builder.build();
    }

    /**
     * 获取当前的折扣力度
     *
     * @param count 购买次数
     * @param conf  折扣配置
     * @return 10000 没有折扣
     */
    private int getDiscountByCount(int count, StaticActHotProduct conf) {
        // if (count >= 1) {
        //     List<Integer> discount = conf.getDiscount();
        //     if (count >= discount.size()) {
        //         return discount.get(discount.size() - 1);
        //     } else {
        //         return discount.get(count - 1);
        //     }
        // }
        if (count >= 0) {
            List<Integer> discount = conf.getDiscount();
            if (count + 1 >= discount.size()) {
                return discount.get(discount.size() - 1);
            } else {
                return discount.get(count);
            }
        }

        return (int) Constant.TEN_THROUSAND;
    }

    /**
     * 获取热销商品活动的信息
     *
     * @param player   玩家对象
     * @param activity 活动
     * @param builder  活动响应数据
     */
    public void getActivity(Player player, Activity activity, GamePb3.GetActivityRs.Builder builder) {
        int activityId = activity.getActivityId();
        // 热销商品购买次数
        int count = activityDataManager.currentActivity(player, activity, ActivityConst.ActHotProduct.STATUS_BUY_COUNT);
        // 消息的钻石数量
        int spend = activityDataManager.currentActivity(player, activity, ActivityConst.ActHotProduct.STATUS_SPEND_SUM);
        Optional.ofNullable(StaticActivityDataMgr.getActHotProductByActId(activityId))
                .ifPresent(productMap -> {
                    Optional.ofNullable(productMap.get(ActivityConst.ActHotProduct.STATUS_SPEND_SUM))
                            .ifPresent(conf -> {
                                conf.forEach(sahp -> {
                                    int status = activity.getStatusMap().containsKey(sahp.getKeyId()) ? 2 : spend >= sahp.getSpend() ? 1 : 0;
                                    builder.addActivityCond(PbHelper.createActivityCondPb(sahp, status, spend));
                                });
                            });
                    Optional.ofNullable(productMap.get(ActivityConst.ActHotProduct.STATUS_BUY_COUNT))
                            .ifPresent(conf -> {
                                conf.forEach(sahp -> {
                                    builder.addActivityCond(PbHelper.createActivityCondPb(sahp, 0, activity.getStatusMap().getOrDefault(sahp.getKeyId(), 0)));
                                });
                            });
                });
        // 当前的购买次数
        builder.addParam(count);
    }

    /**
     * 获取可以领取的奖励个数
     *
     * @param player 玩家对象
     * @return 奖励个数
     */
    public int getCurActTips(Player player) {
        // warp tips
        AtomicInteger count = new AtomicInteger();
        Optional.ofNullable(activityDataManager.getActivityInfo(player, ActivityConst.ACT_HOT_PRODUCT))
                .ifPresent(activity -> {
                    // 消耗的钻石数量
                    int spend = activityDataManager.currentActivity(player, activity, ActivityConst.ActHotProduct.STATUS_SPEND_SUM);
                    Optional.ofNullable(StaticActivityDataMgr.getActHotProductByActId(activity.getActivityId()))
                            .ifPresent(productMap -> {
                                Optional.ofNullable(productMap.get(ActivityConst.ActHotProduct.STATUS_SPEND_SUM))
                                        .ifPresent(conf -> {
                                            conf.forEach(sahp -> {
                                                int keyId = sahp.getKeyId();
                                                if (!activity.getStatusMap().containsKey(keyId) && spend >= sahp.getSpend()) {
                                                    count.incrementAndGet();
                                                }
                                            });
                                        });
                            });
                });
        return count.get();
    }

    /**
     * 活动结束的时候, 未领取的奖励发送邮件
     */
    public void sendUnrewardedMail() {
        int actType = ActivityConst.ACT_HOT_PRODUCT;
        playerDataManager.getPlayers().values().stream()
                .filter(p -> Objects.nonNull(p.activitys.get(actType)))
                .forEach(player -> {
                    Activity activity = player.activitys.get(actType);
                    int activityId = activity.getActivityId();
                    Optional.ofNullable(StaticActivityDataMgr.getActHotProductByActId(activityId))
                            .ifPresent(productMap -> {
                                Optional.ofNullable(productMap.get(ActivityConst.ActHotProduct.STATUS_SPEND_SUM))
                                        .ifPresent(conf -> {
                                            List<CommonPb.Award> awards = conf.stream()
                                                    .filter(sahp -> {
                                                        int spend = Math.toIntExact(activity.getStatusCnt().getOrDefault(ActivityConst.ActHotProduct.STATUS_SPEND_SUM, 0L));
                                                        int keyId = sahp.getKeyId();
                                                        return !activity.getStatusMap().containsKey(keyId) && spend >= sahp.getSpend();
                                                    })
                                                    .flatMap(sahp -> PbHelper.createAwardsPb(sahp.getAwardList()).stream())
                                                    .collect(Collectors.toList());
                                            if (!awards.isEmpty()) {
                                                mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_ACT_UNREWARDED_REWARD, AwardFrom.ACT_UNREWARDED_RETURN, TimeHelper.getCurrentSecond(), actType, activityId, actType, activityId);
                                            }
                                        });
                            });
                });
    }
}