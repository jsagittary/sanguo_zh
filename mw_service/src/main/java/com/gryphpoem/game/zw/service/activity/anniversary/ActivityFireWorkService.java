package com.gryphpoem.game.zw.service.activity.anniversary;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticAnniversaryMgr;
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
import com.gryphpoem.game.zw.resource.domain.s.StaticRandomLibrary;
import com.gryphpoem.game.zw.resource.pojo.Prop;
import com.gryphpoem.game.zw.resource.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 周年庆活动---放烟花
 *
 * @Description
 * @Author zhangdh
 * @Date 2021-07-22 14:32
 */
@Service
public class ActivityFireWorkService {
    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private MailDataManager mailDataManager;

    //saveMap.key = 0;
    private static final int SAVE_KEY_0 = 0;

    private Activity getActivity(Player player, int activityType) throws MwException {
        Activity activity = activityDataManager.getActivityInfo(player, activityType);
        if (activity == null) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "玩家活动数据异常");
        }
        return activity;
    }

    private ActivityBase getActivityBase(Date now, int activityType) throws MwException {
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
        if (activityBase == null
                || activityBase.getPlan() == null
                || now.before(activityBase.getBeginTime())
                || now.after(activityBase.getEndTime())) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "活动未开启");
        }
        return activityBase;
    }

    public int buildActivity(GamePb3.GetActivityRs.Builder builder, ActivityBase activityBase, Activity activity) {
        //烟花道具ID, 每消耗 randomAwardCostCount 给予 randomId 的随机奖励
        int propId = 0, randomAwardCostCount = 0;
        for (List<Integer> params : ActParamConstant.ACT_FIRE_WORK) {
            if (params == null || params.size() != 4) continue;
            if (params.get(0) == activityBase.getActivityId()) {
                propId = params.get(1);
                randomAwardCostCount = params.get(2);
            }
        }
        Map<Integer, Integer> saveMap = activity.getSaveMap();
        //累计已经点燃的鞭炮数量
        int totalBanishCount = saveMap.getOrDefault(propId, 0);
        //累计的领取随机奖励的烟花燃放数量
        int randomBanishCount = saveMap.getOrDefault(SAVE_KEY_0, 0);
        List<StaticActAward> ataList = StaticActivityDataMgr.getActAwardById(activityBase.getActivityId());
        if (Objects.nonNull(ataList)) {
            ataList.forEach(ata -> {
                int status = activity.getStatusMap().getOrDefault(ata.getKeyId(), 0);
                int state = totalBanishCount >= ata.getParam().get(0) ? 1 : 0;
                builder.addActivityCond(PbHelper.createActivityCondPb(ata, status, state));
            });
        }
        builder.addParam(randomAwardCostCount - randomBanishCount);
        return totalBanishCount;
    }

    public int getActivityValue(Player player, Activity activity, ActivityBase activityBase, StaticActAward actAward) throws MwException {
        //烟花道具ID, 每消耗 randomAwardCostCount 给予 randomId 的随机奖励
        int propId = 0;
        for (List<Integer> params : ActParamConstant.ACT_FIRE_WORK) {
            if (params == null || params.size() != 4) continue;
            if (params.get(0) == activityBase.getActivityId()) {
                propId = params.get(1);
            }
        }
        if (propId == 0) {
            throw new MwException(GameError.NO_CONFIG.getCode(), String.format("planId :%d, act award :%d, not found act param",
                    activityBase.getPlan().getKeyId(), actAward.getKeyId()));
        }

        Map<Integer, Integer> saveMap = activity.getSaveMap();
        //累计已经点燃的鞭炮数量
        return saveMap.getOrDefault(propId, 0);
    }

    public int getTips(Activity activity, ActivityBase activityBase) {
        //烟花道具ID, 每消耗 randomAwardCostCount 给予 randomId 的随机奖励
        int propId = 0;
        for (List<Integer> params : ActParamConstant.ACT_FIRE_WORK) {
            if (params == null || params.size() != 4) continue;
            if (params.get(0) == activityBase.getActivityId()) {
                propId = params.get(1);
            }
        }
        Map<Integer, Integer> saveMap = activity.getSaveMap();
        //累计已经点燃的鞭炮数量
        int totalBanishCount = saveMap.getOrDefault(propId, 0);
        int tips = 0;
        List<StaticActAward> staticActAwards = StaticActivityDataMgr.getActAwardById(activityBase.getActivityId());
        if (CheckNull.nonEmpty(staticActAwards)) {
            for (StaticActAward sActAward : staticActAwards) {
                int status = activity.getStatusMap().getOrDefault(sActAward.getKeyId(), 0);
                if (status == 0 && totalBanishCount >= sActAward.getParam().get(0)) {
                    tips++;
                }
            }
        }
        return tips;
    }

    public int checkDrawSubActivityAward(Player player, Activity activity, ActivityBase activityBase, StaticActAward actAward) throws MwException {
        //累计已经点燃的鞭炮数量
        int totalBanishCount = getActivityValue(player, activity, activityBase, actAward);
        if (totalBanishCount < actAward.getParam().get(0)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(),
                    String.format("roleId :%d, plan id :%d, act award id :%d, ", player.getLordId(), activityBase.getPlanKeyId(), actAward.getKeyId()));
        }
        return totalBanishCount;
    }


    /**
     * 燃放烟花
     *
     * @param player
     * @param req
     * @return
     * @throws Exception
     */
    public GamePb4.FireFireWorkRs fireWork(Player player, GamePb4.FireFireWorkRq req) throws MwException {
        int actType = req.getActType();//留给策划换皮
        if (actType != ActivityConst.ACT_ANNIVERSARY_FIREWORK) {
            throw new MwException(GameError.PARAM_ERROR.getCode());
        }
        Date now = new Date();
        //烟花道具ID, 每消耗 randomAwardCostCount 给予 randomId 的随机奖励
        int propId = 0, randomAwardCostCount = 0, randomAwardId = 0;
        ActivityBase actBase = getActivityBase(now, actType);
        for (List<Integer> params : ActParamConstant.ACT_FIRE_WORK) {
            if (params == null || params.size() != 4) continue;
            if (params.get(0) == actBase.getActivityId()) {
                propId = params.get(1);
                randomAwardCostCount = params.get(2);
                randomAwardId = params.get(3);
            }
        }
        if (propId <= 0 || randomAwardCostCount <= 0 || randomAwardId <= 0) {
            throw new MwException(GameError.NO_CONFIG.getCode());
        }


        Activity activity = getActivity(player, actType);
        //活动
        List<StaticActAward> staticActAwards = StaticActivityDataMgr.getActAwardById(actBase.getActivityId());
        if (CheckNull.isEmpty(staticActAwards)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), String.format("活动ID :%d 奖励项配置错误...", actBase.getActivityId()));
        }
        Prop prop = player.props.get(propId);
        int propCount = prop != null ? prop.getCount() : 0;
        if (propCount <= 0) throw new MwException(GameError.PROP_NOT_ENOUGH.getCode(), "烟花数量不足");


        Map<Integer, Integer> saveMap = activity.getSaveMap();
        //活动期间已经点燃的烟花数量
        int totalBanishCount = saveMap.getOrDefault(propId, 0);
        int randomAwardBanishCount = saveMap.getOrDefault(SAVE_KEY_0, 0);
        //检测并扣除玩家身上鞭炮
        rewardDataManager.checkAndSubPlayerRes(player, AwardType.PROP, propId, propCount,
                AwardFrom.ACT_MONSTER_NIAN_BANISH, true, ActivityConst.ACT_MONSTER_NIAN);

        totalBanishCount += propCount;
        randomAwardBanishCount += propCount;
        saveMap.put(propId, totalBanishCount);
        List<CommonPb.Award> pbRandomAwards = null;
        if (randomAwardBanishCount >= randomAwardCostCount) {
            //可以发放随机奖励
            int sendCount = randomAwardBanishCount / randomAwardCostCount;
            List<List<Integer>> randomAwards = getRandomAwards(player, actBase, randomAwardId, sendCount);
            if (CheckNull.nonEmpty(randomAwards)) {
                //发放随机奖励
                randomAwardBanishCount = randomAwardBanishCount % randomAwardCostCount;
                saveMap.put(SAVE_KEY_0, randomAwardBanishCount);
                pbRandomAwards = rewardDataManager.sendReward(player, randomAwards, AwardFrom.ANNIVERSARY_FIRE_FIREWORK, actBase.getPlan().getKeyId(), totalBanishCount, randomAwardBanishCount);
            }
        } else {
            saveMap.put(SAVE_KEY_0, randomAwardBanishCount);
        }
        GamePb4.FireFireWorkRs.Builder rsb = GamePb4.FireFireWorkRs.newBuilder();
        for (StaticActAward sActAward : staticActAwards) {
            int status = activity.getStatusMap().getOrDefault(sActAward.getKeyId(), 0);
            int state = totalBanishCount >= sActAward.getParam().get(0) ? 1 : 0;
            rsb.addActivityCond(PbHelper.createActivityCondPb(sActAward, status, state));
        }
        rsb.setTotalBanishCount(totalBanishCount);
        rsb.setRandomBanishCount(Math.max(0, randomAwardCostCount - randomAwardBanishCount));
        rsb.setRemainCnt(prop.getCount());
        if (CheckNull.nonEmpty(pbRandomAwards)) {
            rsb.addAllRandomAward(pbRandomAwards);
        }
        return rsb.build();
    }

    private List<List<Integer>> getRandomAwards(Player player, ActivityBase actBase, int randomAwardId, int sendCount) throws MwException {
        //给玩家发放随机奖励
        StaticRandomLibrary staticRandomLib = StaticAnniversaryMgr.getRandomLibrary(randomAwardId, player.lord.getLevel());
        if (Objects.isNull(staticRandomLib) || CheckNull.isEmpty(staticRandomLib.getAwardList())) {
            throw new MwException(GameError.NO_CONFIG.getCode(), String.format("roleId :%d, planId :%d, randomId :%d, level:%d randomLib not Found!!!",
                    player.getLordId(), actBase.getPlan().getKeyId(), randomAwardId, player.lord.getLevel()));
        }
        List<List<Integer>> awardList = new ArrayList<>();
        for (int i = 0; i < sendCount; i++) {
            List<Integer> list = RandomUtil.getRandomByWeight(staticRandomLib.getAwardList(), 3, false);
            awardList.add(list);
        }
        return awardList;
    }


    /**
     * 点灯笼活动结束
     *
     * @param jobKey 活动
     */
    public void activityFinish(String jobKey) {
        ActivityUtil.sendConvertMail(jobKey);
        sendUnrewardedMail(jobKey);

    }

    private void sendUnrewardedMail(String jobKey) {
        try {
            ActivityBase base = ActivityUtil.getActivityBase(jobKey);
            Objects.requireNonNull(base);
            int propId = -1, actType = base.getActivityType(), activityId = base.getActivityId();
            for (List<Integer> params : ActParamConstant.ACT_FIRE_WORK) {
                if (params == null || params.size() != 4) continue;
                if (params.get(0) == base.getActivityId()) {
                    propId = params.get(1);
                }
            }
            List<StaticActAward> staticActAwards = StaticActivityDataMgr.getActAwardById(base.getActivityId());
            for (Player player : playerDataManager.getAllPlayer().values()) {
                Activity activity = player.activitys.get(base.getActivityType());
                if (Objects.isNull(activity)) continue;
                int totalBanishCount = activity.getSaveMap().getOrDefault(propId, 0);
                if (totalBanishCount <= 0) continue;
                Map<Integer, Integer> statusMap = activity.getStatusMap();
                List<CommonPb.Award> awards = null;
                for (StaticActAward sActAward : staticActAwards) {
                    int status = statusMap.getOrDefault(sActAward.getKeyId(), 0);
                    if (status >= sActAward.getCond()) continue;
                    if (totalBanishCount < sActAward.getParam().get(0)) continue;
                    for (List<Integer> list : sActAward.getAwardList()) {
                        if (awards == null) awards = new ArrayList<>();
                        awards.add(PbHelper.createAwardPb(list.get(0), list.get(1), list.get(2)));
                    }
                }
                if (CheckNull.nonEmpty(awards)) {
                    mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_ACT_UNREWARDED_REWARD, AwardFrom.ACT_UNREWARDED_RETURN,
                            TimeHelper.getCurrentSecond(), actType, activityId, actType, activityId);
                }
            }
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

}
