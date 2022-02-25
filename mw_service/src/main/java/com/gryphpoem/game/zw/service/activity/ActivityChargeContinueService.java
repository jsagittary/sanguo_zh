package com.gryphpoem.game.zw.service.activity;

import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.manager.ActivityDataManager;
import com.gryphpoem.game.zw.manager.MailDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.MailConstant;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.s.StaticActAward;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-04-02 10:08
 */
@Service
public class ActivityChargeContinueService {

    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private MailDataManager mailDataManager;

    /**
     * 连续充值进度更新
     *
     * @param activityBase
     * @param activity
     * @param schedule
     */
    public void updateChargeContinueActivity(ActivityBase activityBase, Activity activity, long schedule) {
        List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityBase.getActivityId());
        if (condList == null || condList.isEmpty()) {
            return;
        }

        Integer day = activityBase.getDayiyBegin();
        int awardDay = getChargeContineMaxDay(activityBase);
        if (day > awardDay) {
            // 已经过了最大的活动奖励天数了
            return;
        }
        // 当前天数已充值的钱
        int chargeGold = activity.getSaveMap().get(day) == null ? 0 : activity.getSaveMap().get(day);
        chargeGold = (int) (chargeGold + schedule);
        activity.getSaveMap().put(day, chargeGold);
        for (StaticActAward a : condList) {
            if (a.getParam().indexOf(day) == -1) {
                continue;
            }
            Integer s = activity.getStatusMap().get(a.getKeyId());
            if (s != null) {
                continue;
            }
            if (chargeGold >= a.getCond()) {
                // 符合要求的奖励放进去
                activity.getStatusMap().put(a.getKeyId(), 0);
            }
        }
    }

    /**
     * 获取连续充值奖励个数
     *
     * @param player
     * @param actType
     * @return
     */
    public int getChargeContinueTips(Player player, int actType) {
        int tips = 0;
        Activity activity = activityDataManager.getActivityInfo(player, actType);
        if (activity == null) return tips;
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        int activityKeyId = activityBase.getActivityId();
        List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(activityKeyId);
        if (condList != null) {
            for (StaticActAward e : condList) {
                int keyId = e.getKeyId();
                Map<Integer, Integer> rm = activity.getStatusMap();
                Integer st = rm.get(keyId);
                if (st != null && st == 0) {
                    tips++;
                }
            }
        }
        return tips;
    }

    /**
     * 获取连续充值的最大活动天数
     *
     * @return
     */
    public int getChargeContineMaxDay(ActivityBase base) {
        if (base.getActivityType() != ActivityConst.ACT_CHARGE_CONTINUE
                && base.getActivityType() != ActivityConst.ACT_MERGE_CHARGE_CONTINUE) {
            return -1;
        }
        List<StaticActAward> condList = StaticActivityDataMgr.getActAwardById(base.getActivityId());
        int maxDay = 0;
        for (StaticActAward s : condList) {
            if (s.getParam().get(0) > maxDay) {
                maxDay = s.getParam().get(0);
            }
        }
        return maxDay;
    }

    /**
     * 发送未领取的连续充值奖励
     *
     * @param player
     * @param actType
     * @param now
     */
    public void sendUnrewardedMailByChargeContinue(Player player, int actType, int now) {
        Activity activity = player.activitys.get(actType);
        if (activity == null) {
            return;
        }
        List<StaticActAward> actAwardList = StaticActivityDataMgr.getActAwardById(activity.getActivityId());
        if (CheckNull.isEmpty(actAwardList)) {
            return;
        }
        List<CommonPb.Award> awards = new ArrayList<>();
        for (StaticActAward saa : actAwardList) {
            // 连续充值奖励，如果领取状态不存在或者为1 则不发送奖励
            Integer s = activity.getStatusMap().get(saa.getKeyId());
            if (s == null || s == 1) {
                continue;
            }
            int day = saa.getParam().get(0);
            int chargeGold = activity.getSaveMap().get(day) == null ? 0 : activity.getSaveMap().get(day);
            if (chargeGold < saa.getCond()) {
                // 充值金额不到
                continue;
            }
            awards.addAll(PbHelper.createAwardsPb(saa.getAwardList()));
            activity.getStatusMap().put(saa.getKeyId(), 1);
        }
        if (!awards.isEmpty()) {
            mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_ACT_UNREWARDED_REWARD,
                    AwardFrom.ACT_UNREWARDED_RETURN, now, actType, activity.getActivityId(), actType, activity.getActivityId());
        }
    }
}
