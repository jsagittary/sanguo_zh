package com.gryphpoem.game.zw.server;

import com.gryphpoem.game.zw.constant.MergeUtils;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.domain.MergePlayer;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.s.StaticActAward;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.activity.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 合服活动的处理
 */
@Component
public class MergActivityService {

    private Map<Integer, MultiAwardActProcess> multiAwardActMap = new HashMap<>();

    @Autowired
    private ActivityService activityService;

    @PostConstruct
    public void registInit() {
        registMultiAwardActPrc(ActivityConst.ACT_LEVEL, this::actLevelProcess);
        registMultiAwardActPrc(ActivityConst.ACT_WISHING_WELL, this::actWishingProcess);
        registMultiAwardActPrc(ActivityConst.ACT_BUILD_GIFT, this::actBuildGiftProcess);
        registMultiAwardActPrc(ActivityConst.ACT_PAY_7DAY, this::actPay7dayProcess);
        registMultiAwardActPrc(ActivityConst.ACT_MONOPOLY, this::actMonopolyProcess);
        registMultiAwardActPrc(ActivityConst.ACT_SUPPLY_DORP, this::actSupplyDorpProcess);
        registMultiAwardActPrc(ActivityConst.ACT_CHARGE_TOTAL, this::actChargeTotalProcess);
        registMultiAwardActPrc(ActivityConst.ACT_PAY_TURNPLATE, this::actPayTurnplateProcess);
        registMultiAwardActPrc(ActivityConst.ACT_GIFT_PAY, this::actGiftPayProcess);
        registMultiAwardActPrc(ActivityConst.ACT_COST_GOLD, this::actCostGoldProcess);
        // registMultiAwardActPrc(ActivityConst.ACT_CONSUME_GOLD_RANK, this::actConsumeGoldProcess);
        registMultiAwardActPrc(ActivityConst.ACT_VIP_BAG, this::actVipBagProcess);
    }

    /**
     * 多档位的活动处理
     *  @param actType
     * @param player
     * @param masterServerActBase
     */
    public void multiAwardActProcess(int actType, Player player, ActivityBase masterServerActBase) {
        MultiAwardActProcess prc = multiAwardActMap.get(actType);
        Activity activity = player.activitys.get(actType);
        if (prc != null && activity != null) {
            prc.pocess(player, activity, masterServerActBase);
        }
    }

    /**
     * 特价礼包活动的特殊处理
     *
     * @param player              玩家对象
     * @param activity            活动对象
     * @param masterServerActBase 主服的actBase
     */
    private void actVipBagProcess(Player player, Activity activity, ActivityBase masterServerActBase) {
        if (player instanceof MergePlayer) {
            MergePlayer p = (MergePlayer) player;
            if (p.getToServerId() != 0 && p.getOldServerId() != p.getToServerId()) {
                // 非主服玩家, 玩家需要合到其他服去
                Iterator<Map.Entry<Integer, Long>> iterator = activity.getStatusCnt().entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Integer, Long> statusCnt = iterator.next();
                    int planKey = statusCnt.getKey();
                    // 如果不是新手的特价礼包就清除掉
                    if (!MergeUtils.REATIN_VIP_BAG_GIFT_IDS.contains(planKey)) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    /**
     * 建筑礼包
     *
     * @param player              玩家
     * @param activity            活动
     * @param masterServerActBase 主服的活动的ActBase
     */
    private void actBuildGiftProcess(Player player, Activity activity, ActivityBase masterServerActBase) {
        actLevelProcess(player, activity, masterServerActBase);
    }

    /**
     * 许愿池处理
     *
     * @param player              玩家
     * @param activity            活动
     * @param masterServerActBase 主服的活动的ActBase
     */
    private void actWishingProcess(Player player, Activity activity, ActivityBase masterServerActBase) {
        actLevelProcess(player, activity, masterServerActBase);
    }

    /**
     * 消费有礼
     *
     * @param player              玩家
     * @param activity            活动
     * @param masterServerActBase 主服的活动的ActBase
     */
    private void actCostGoldProcess(Player player, Activity activity, ActivityBase masterServerActBase) {
        if (player instanceof MergePlayer) {
            MergePlayer p = (MergePlayer) player;
            if (p.getToServerId() != 0 && p.getOldServerId() != p.getToServerId()) {
                // 非主服玩家, 玩家需要合到其他服去
                if (masterServerActBase != null && masterServerActBase.getStep() == ActivityConst.OPEN_STEP) {
                    // 主服开放活动
                    actLevelProcess(player, activity, masterServerActBase);
                } else {
                    // 主服未开放活动
                    activityService.sendUnrewardedMailByNormal(player, activity.getActivityType(), TimeHelper.getCurrentSecond());
                    activity.cleanActivity(false); // 清除数据
                    LogUtil.common("消费有礼合服未领取邮件 roleId:", p.roleId, ", mServerId:", p.getToServerId());
                }
            }
        }
    }


    /**
     * 消费排行活动, 非主服玩家, 并且主服的活动未在开启时间内, 发送奖励邮件
     *
     * @param player              玩家对象
     * @param activity            活动对象
     * @param masterServerActBase 主服的该活动ActBase对象
     */
    private void actConsumeGoldProcess(Player player, Activity activity, ActivityBase masterServerActBase) {
        if (player instanceof MergePlayer) {
            MergePlayer p = (MergePlayer) player;

            // 不论主服或者非主服的开放情况，统一清除排行进度，且发送奖励邮件 雷亦江2020-04-20
            activityService.sendUnrewardedMailByRank(player,activity.getActivityType(), TimeHelper.getCurrentSecond());
            activity.cleanActivity(false); // 清除数据
            LogUtil.common("消费有礼合服未领取邮件 roleId:", p.roleId, ", mServerId:", p.getToServerId());
        }
    }

    /**
     * 充值有礼
     *
     * @param player
     * @param activity
     * @param masterServerActBase
     */
    private void actGiftPayProcess(Player player, Activity activity, ActivityBase masterServerActBase) {
        actLevelProcess(player, activity, masterServerActBase);
}

    /**
     * 充值转盘
     *
     * @param player
     * @param activity
     * @param masterServerActBase
     */
    private void actPayTurnplateProcess(Player player, Activity activity, ActivityBase masterServerActBase) {
        // 清理领奖就可以了
        activity.getStatusMap().clear();
    }

    /**
     * 累计充值
     *
     * @param player
     * @param activity
     * @param masterServerActBase
     */
    private void actChargeTotalProcess(Player player, Activity activity, ActivityBase masterServerActBase) {
        actLevelProcess(player, activity, masterServerActBase);
    }

    /**
     * 空降补给
     *
     * @param player
     * @param activity
     * @param masterServerActBase
     */
    private void actSupplyDorpProcess(Player player, Activity activity, ActivityBase masterServerActBase) {
        if (player instanceof MergePlayer) {
            MergePlayer p = (MergePlayer) player;
            if (p.getOldServerId() != p.getToServerId()) {
                // 不是主服的玩家,返还金币和奖励
                activityService.sendUnrewardedMailBySupplyDorp(player, TimeHelper.getCurrentSecond());
                activity.cleanActivity(false); // 清除数据
                LogUtil.common("空降补给合服未领取邮件 roleId:", p.roleId, ", mServerId:", p.getToServerId());
            }
        }
    }

    /**
     * 大富翁
     *
     * @param player
     * @param activity
     * @param masterServerActBase
     */
    private void actMonopolyProcess(Player player, Activity activity, ActivityBase masterServerActBase) {
        // 不用处理,修改actId就可以
    }

    /**
     * 七日充值处理
     *
     * @param player
     * @param activity
     * @param masterServerActBase
     */
    private void actPay7dayProcess(Player player, Activity activity, ActivityBase masterServerActBase) {
        actLevelProcess(player, activity, masterServerActBase);
    }

    /**
     * 成长基金处理
     *
     * @param player
     * @param activity
     * @param masterServerActBase
     */
    private void actLevelProcess(Player player, Activity activity, ActivityBase masterServerActBase) {
        if (masterServerActBase == null) {
            return;
        }
        List<Integer> srcAward = StaticActivityDataMgr.getActAwardById(activity.getActivityId()).stream()
                .sorted(Comparator.comparing(StaticActAward::getCond)).map(StaticActAward::getKeyId)
                .collect(Collectors.toList());
        List<Integer> dstAward = StaticActivityDataMgr.getActAwardById(masterServerActBase.getActivityId()).stream()
                .sorted(Comparator.comparing(StaticActAward::getCond)).map(StaticActAward::getKeyId)
                .collect(Collectors.toList());
        int dstAwardSize = dstAward.size();
        List<Integer> awardIds = new ArrayList<>();// 对应的奖励
        Set<Integer> keySet = activity.getStatusMap().keySet(); // 已领取的奖励
        for (Integer id : keySet) {
            int index = srcAward.indexOf(id);
            if (index != -1 && index < dstAwardSize) {
                awardIds.add(dstAward.get(index));
            } else {
                awardIds.add(id);
            }
        }
        for (Integer id : awardIds) {
            activity.getStatusMap().put(id, 1);
        }
    }

    /**
     * 和主服统一activityId和开始时间
     *
     * @param player
     * @param activity
     * @param masterServerActBase
     */
    public void actIdAndBeginTimeUnity(Player player, Activity activity, ActivityBase masterServerActBase) {
        int oldActId = activity.getActivityId();
        int oldActBegin = activity.getBeginTime();
        // activityId统一
        int activityId = masterServerActBase.getActivityId();
        activity.setActivityId(activityId);
        Date beginTime = masterServerActBase.getBeginTime();
        int begin = TimeHelper.getDay(beginTime); // 时间重置
        activity.setBeginTime(begin);
        LogUtil.common("合服修复活动的档位id和开启时间 roleId:", player.roleId, ", oldActId:", oldActId, ", newActId:", activityId, ", oldBeginTime:", oldActBegin, ", newActBegin:", begin);
    }

    private void registMultiAwardActPrc(int actType, MultiAwardActProcess actPrc) {
        multiAwardActMap.put(actType, actPrc);
    }

    @FunctionalInterface
    static interface MultiAwardActProcess {

        void pocess(Player player, Activity activity, ActivityBase masterServerActBase);
    }
}
