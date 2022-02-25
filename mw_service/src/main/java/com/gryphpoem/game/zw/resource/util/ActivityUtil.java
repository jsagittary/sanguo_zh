package com.gryphpoem.game.zw.resource.util;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.manager.MailDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.constant.MailConstant;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticActVoucher;
import com.gryphpoem.game.zw.resource.pojo.Prop;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-03-17 14:15
 */
public class ActivityUtil {

    /**
     * @param jobKeyName 格式: name.append(plan.getActivityType()).append("_").append(plan.getActivityId()).append("_").append(plan.getKeyId());
     * @return base
     * @see com.gryphpoem.game.zw.quartz.ScheduleManager #loadActMailSchedule()
     * 根据定时器中的 jobKeyName 查找 activity base
     */
    public static ActivityBase getActivityBase(String jobKeyName) {
        String[] keys = jobKeyName.split("_");
        if (keys.length == 3) {
            return getActivityBase(Integer.parseInt(keys[2]));
        } else {
            LogUtil.error("JOB key 配置错误 : ", jobKeyName);
        }
        return null;
    }

    /**
     * 根据s_activity_plan表的keyId, 查询ActivityBase
     * @param planKeyId s_activity_plan表的keyId
     * @return ActivityBase
     */
    public static ActivityBase getActivityBase(int planKeyId) {
        if (planKeyId > 0) {
            List<ActivityBase> activityList = StaticActivityDataMgr.getActivityList();
            if (!CheckNull.isEmpty(activityList)) {
                ActivityBase base = activityList.stream()
                        .filter(t -> t.getPlan().getKeyId() == planKeyId)
                        .findAny()
                        .orElse(null);
                if (Objects.nonNull(base)) {
                    return base;
                } else {
                    LogUtil.error("activity base notFound !!!", planKeyId, " 不存在");
                }
            }
        } else {
            LogUtil.error("JOB key 配置错误 : ", planKeyId);
        }
        return null;
    }

    /**
     * 活动结束后将玩家剩余活动道具自动兑换为资源
     * 使用s_act_voucher 表中对应
     *
     * @param jobKeyName
     */
    public static void sendConvertMail(String jobKeyName) {
        ActivityBase base = ActivityUtil.getActivityBase(jobKeyName);
        sendConvertMail(base, AwardFrom.ACT_OVER_AUTO_EXCHANGE_NON_USED_ITEMS);
    }

    /**
     * 活动结束后将玩家剩余活动道具自动兑换为资源
     * 使用s_act_voucher 表中对应
     *
     * @param base 活动对象
     */
    public static void sendConvertMail(ActivityBase base, AwardFrom from) {
        Objects.requireNonNull(base);
        int activityId = base.getActivityId();
        StaticActVoucher sav = StaticActivityDataMgr.getActVoucherByActId(activityId);
        if (Objects.isNull(sav)
                || CheckNull.isEmpty(sav.getConsume())
                || sav.getConsume().size() != 3
                || CheckNull.isEmpty(sav.getAwardList())
                || sav.getAwardList().size() != 3) {
            LogUtil.error(String.format("活动ID :%d, 结束自动将道具转换成资源时配置异常!!!", activityId));
            return;
        }
        int nowSec = TimeHelper.getCurrentSecond();
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        RewardDataManager rewardDataManager = DataResource.ac.getBean(RewardDataManager.class);
        MailDataManager mailDataManager = DataResource.ac.getBean(MailDataManager.class);
        int propId = sav.getConsume().get(1);
        for (Map.Entry<String, Player> entry : playerDataManager.getAllPlayer().entrySet()) {
            Player player = entry.getValue();
            Prop prob = player.props.get(propId);
            int itemCount = prob != null ? prob.getCount() : 0;
            if (itemCount <= 0) continue;
            try {
                rewardDataManager.checkAndSubPlayerRes(player, AwardType.PROP, propId, itemCount,
                        from, true, base.getActivityType(), base.getActivityId(), base.getPlan().getKeyId());
                long awardCount = sav.getAwardList().get(2) * itemCount;
                if (awardCount > Integer.MAX_VALUE) awardCount = Integer.MAX_VALUE;
                CommonPb.Award pbAward = PbHelper.createAwardPb(sav.getAwardList().get(0), sav.getAwardList().get(1), (int) awardCount);
                List<CommonPb.Award> awardList = Collections.singletonList(pbAward);
                //发放资源
                mailDataManager.sendAttachMail(player, awardList, MailConstant.MOLD_ACT_EXCHANGE_REWARD,
                        from, nowSec, base.getActivityType(), base.getActivityId(), base.getActivityType(), base.getActivityId());
            } catch (Exception e) {
                LogUtil.error(String.format("活动ID :%d, 玩家 :%d, 灯油道具数量 :%d, 转换成资源时道具不足!!!", activityId, player.getLordId(), itemCount), e);
            }
        }
    }
}
