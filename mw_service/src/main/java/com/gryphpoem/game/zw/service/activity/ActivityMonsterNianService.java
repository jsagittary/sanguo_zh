package com.gryphpoem.game.zw.service.activity;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
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
import com.gryphpoem.game.zw.resource.domain.s.StaticActAward;
import com.gryphpoem.game.zw.resource.domain.s.StaticActBandit;
import com.gryphpoem.game.zw.resource.domain.s.StaticActivityPlan;
import com.gryphpoem.game.zw.resource.pojo.Prop;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 赶年兽活动
 *
 * @Description
 * @Author zhangdh
 * @Date 2021-01-22 2:47
 */
@Service
public class ActivityMonsterNianService {

    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private MailDataManager mailDataManager;

    private Activity getMonsterNianActivity(Player player) throws MwException {
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_MONSTER_NIAN);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "玩家活动数据异常");
        }
        return activity;
    }

    private ActivityBase getMonsterNianActivityBase(Date now) throws MwException {
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(ActivityConst.ACT_MONSTER_NIAN);
        if (activityBase == null
                || activityBase.getPlan() == null
                || now.before(activityBase.getBeginTime())
                || now.after(activityBase.getEndTime())) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启");
        }
        return activityBase;
    }

    public int buildActivity(GamePb3.GetActivityRs.Builder builder, ActivityBase base, Activity activity){
        //累计已经点燃的鞭炮数量
        Map<Integer, Integer> saveMap = activity.getSaveMap();
        //今天已经掉落的数量
        int curDate = TimeHelper.getCurrentDay();
        int todayDropCount = saveMap.getOrDefault(curDate, 0);
        builder.addParam(todayDropCount);
        List<StaticActAward> ataList = StaticActivityDataMgr.getActAwardById(base.getActivityId());
        if (Objects.nonNull(ataList)){
            ataList.forEach(ata->{
                int status = activity.getStatusMap().getOrDefault(ata.getKeyId(), 0);
                builder.addActivityCond(PbHelper.createActivityCondPb(ata, status, status));
            });
        }
        return saveMap.getOrDefault(PropConstant.ITEM_ID_1925, 0);
    }


    /**
     * 放鞭炮驱赶年兽
     *
     * @param roleId
     * @param req
     * @return
     * @throws Exception
     */
    public GamePb4.SetOffFirecrackersRs setOffFirecrackers(long roleId, GamePb4.SetOffFirecrackersRq req) throws MwException {
        Date now = new Date();
        ActivityBase actBase = getMonsterNianActivityBase(now);
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = getMonsterNianActivity(player);
        //活动
        List<StaticActAward> staticActAwards = StaticActivityDataMgr.getActAwardById(actBase.getActivityId());
        if (CheckNull.isEmpty(staticActAwards)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), String.format("活动ID :%d 奖励项配置错误...", actBase.getActivityId()));
        }
        Prop prop = player.props.get(PropConstant.ITEM_ID_1925);
        int propCount = prop != null ? prop.getCount() : 0;
        if (propCount <= 0) throw new MwException(GameError.PROP_NOT_ENOUGH.getCode(), "鞭炮数量不足");


        //所有奖励都领完了
        Map<Integer, Integer> statusMap = activity.getStatusMap();
        Map<Integer, Integer> saveMap = activity.getSaveMap();
        //活动期间已经点燃的鞭炮数量
        int totalBanishCount = saveMap.getOrDefault(PropConstant.ITEM_ID_1925, 0);
        //需要上缴的最大数量
        StaticActAward maxAward = staticActAwards.get(staticActAwards.size() - 1);
        int maxCount = maxAward.getParam().get(0);//本次活动最高上缴数量
        if (totalBanishCount >= maxCount) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("活动ID :%d 奖励已经领取完毕...", actBase.getActivityId()));
        }
        int subCount = Math.min(maxCount, totalBanishCount + propCount) - totalBanishCount;
        if (subCount <= 0) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("活动ID :%d 扣除道具数量错误...", actBase.getActivityId()));
        }
        //检测并扣除玩家身上鞭炮
        rewardDataManager.checkAndSubPlayerRes(player, AwardType.PROP, PropConstant.ITEM_ID_1925, subCount,
                AwardFrom.ACT_MONSTER_NIAN_BANISH, true, ActivityConst.ACT_MONSTER_NIAN);
        totalBanishCount += subCount;
        saveMap.put(PropConstant.ITEM_ID_1925, totalBanishCount);
        //给玩家发放奖励
        GamePb4.SetOffFirecrackersRs.Builder rsb = GamePb4.SetOffFirecrackersRs.newBuilder();
        List<CommonPb.Award> rsbAwards = new ArrayList<>();
        for (StaticActAward acta : staticActAwards) {
            if (!statusMap.containsKey(acta.getKeyId())) {
                if (totalBanishCount >= acta.getParam().get(0)) {
                    statusMap.put(acta.getKeyId(), 1);
                    List<CommonPb.Award> awards = rewardDataManager.sendReward(player, acta.getAwardList(), AwardFrom.ACT_MONSTER_NIAN_BANISH,
                            ActivityConst.ACT_MONSTER_NIAN, acta.getKeyId());
                    rsbAwards.addAll(awards);
                    rsb.addActivityCond(PbHelper.createActivityCondPb(acta, 1, 1));
                }
            }
        }
        rsb.setBanishCnt(totalBanishCount);
        rsb.setRemainCnt(prop.getCount());
        if (rsbAwards.size() > 1) rsbAwards = RewardDataManager.mergeAward2(rsbAwards);
        if (!CheckNull.isEmpty(rsbAwards)) {
            rsb.addAllAward(rsbAwards);
        }
        return rsb.build();
    }

    public void sendUnrewardedMail(String jobKeyName) {
        LogUtil.activity("赶年兽活动结束, 开始回收玩家身上未使用的鞭炮");
        String[] keys = jobKeyName.split("_");
        if (keys.length == 3) {
            int planKeyId = Integer.parseInt(keys[2]);
            List<ActivityBase> activityList = StaticActivityDataMgr.getActivityList();
            if (!CheckNull.isEmpty(activityList)) {
                ActivityBase findAtb = activityList.stream()
                        .filter(atb -> atb.getPlan().getKeyId() == planKeyId)
                        .findFirst().orElse(null);
                if (Objects.nonNull(findAtb)) {
                    sendConvertMail(findAtb);
                } else {
                    LogUtil.error("新年赶年兽活动 activity planKeyId : ", planKeyId, " 不存在");
                }
            }
        } else {
            LogUtil.error("赶年兽活动, 活动结束自动兑换鞭炮道具JOB key 配置错误 : ", jobKeyName);
        }
    }

    private void sendConvertMail(ActivityBase activityBase) {
        StaticActivityPlan plan = activityBase.getPlan();
        int planKeyId = plan.getKeyId();
        int activityId = plan.getActivityId();
        List<StaticActBandit> actBandits = StaticActivityDataMgr.getActBanditList();
        if (CheckNull.isEmpty(actBandits)) {
            LogUtil.error("赶年兽活动掉落配置表缺失!!!");
            return;
        }

        // 活动配置
        StaticActBandit sActBandits = actBandits.stream()
                .filter(sab -> sab.getActivityId() == activityId)
                .findFirst().orElse(null);
        List<List<Integer>> convertList = sActBandits != null ? sActBandits.getConvert() : null;
        if (CheckNull.isEmpty(convertList)) {
            LogUtil.error(String.format("赶年兽活动 %s, 配置缺失!!!", activityId));
            return;
        }

        int nowSec = TimeHelper.getCurrentSecond();
        playerDataManager.getAllPlayer().forEach((k, player) -> {
            try {
                Prop prop1925 = player.props.get(PropConstant.ITEM_ID_1925);
                int itemCount = prop1925 != null ? prop1925.getCount() : 0;
                if (itemCount > 0) {
                    //扣除道具
                    rewardDataManager.checkAndSubPlayerRes(player, AwardType.PROP, PropConstant.ITEM_ID_1925, itemCount,
                            AwardFrom.ACT_MONSTER_NIAN_RECOVERY, true, planKeyId);
                    List<CommonPb.Award> awardList = new ArrayList<>();
                    for (List<Integer> convert : convertList) {
                        long giveCount = ((long) convert.get(2)) * itemCount;
                        if (giveCount > Integer.MAX_VALUE) giveCount = Integer.MAX_VALUE;
                        CommonPb.Award award = PbHelper.createAwardPb(convert.get(0), convert.get(1), (int) giveCount);
                        awardList.add(award);
                    }
                    //发放资源
                    mailDataManager.sendAttachMail(player, awardList, MailConstant.MOLD_ACT_EXCHANGE_REWARD,
                            AwardFrom.ACT_UNREWARDED_RETURN, nowSec, plan.getActivityType(), plan.getActivityId(), plan.getActivityType(), plan.getActivityId());
                }
            } catch (Exception e) {
                LogUtil.error("", e);
            }
        });
    }

}
