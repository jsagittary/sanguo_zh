package com.gryphpoem.game.zw.service.activity;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticMusicFestivalMgr;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.p.Pay;
import com.gryphpoem.game.zw.resource.domain.s.StaticMusicFestivalBoxOffice;
import com.gryphpoem.game.zw.resource.domain.s.StaticMusicFestivalBoxOfficeParam;
import com.gryphpoem.game.zw.resource.domain.s.StaticPay;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: zhou jie
 * @time: 2021/10/26 16:02
 */
@Service
public class ActivityBoxOfficeService extends AbsSimpleActivityService {

    // 默认解锁
    private int TYPE_COMMON = 1;
    private int TYPE_2 = 2;
    private int TYPE_3 = 3;

    @Autowired
    private RewardDataManager rewardDataManager;

    @Override
    protected int[] getActivityType() {
        return new int[]{ActivityConst.ACT_MUSIC_FESTIVAL_BOX_OFFICE};
    }

    @Override
    public int getActivityTips(ActivityBase base, Activity activity) {
        List<StaticMusicFestivalBoxOffice> boxOffices = canReceiveAward(activity);
        if (!CheckNull.isEmpty(boxOffices)) {
            return boxOffices.size();
        }
        return 0;
    }

    @Override
    protected GeneratedMessage.Builder<GamePb4.GetActivityDataInfoRs.Builder> getActivityData(Player player, Activity activity, GlobalActivityData globalActivityData) throws MwException {
        GamePb4.GetActivityDataInfoRs.Builder builder = GamePb4.GetActivityDataInfoRs.newBuilder();
        builder.setBoxOfficeInfo(buildBoxOfficeInfo(activity));
        return builder;
    }

    public List<StaticMusicFestivalBoxOffice> canReceiveAward(Activity activity) {
        List<StaticMusicFestivalBoxOffice> boxOffices = StaticMusicFestivalMgr.findBoxOfficesByActId(activity.getActivityId());
        if (CheckNull.isEmpty(boxOffices)) {
            return null;
        }
        Set<Integer> unLockTypes = activity.getStatusMap().keySet();
        int level = activity.getSaveMap().getOrDefault(0, 0);

        return boxOffices.stream()
                .filter(conf -> unLockTypes.contains(conf.getType()) && level >= conf.getLevel() && !activity.getPropMap().containsKey(conf.getId()))
                .collect(Collectors.toList());
    }

    private CommonPb.MusicFestivalBoxOfficeInfo buildBoxOfficeInfo(Activity activity) {
        CommonPb.MusicFestivalBoxOfficeInfo.Builder builder = CommonPb.MusicFestivalBoxOfficeInfo.newBuilder();
        builder.addAllProgressGot(activity.getPropMap().entrySet().stream().map(en -> PbHelper.createTwoIntPb(en.getKey(), en.getValue())).collect(Collectors.toList()));
        builder.setLevel(activity.getSaveMap().getOrDefault(0, 0));
        builder.setExp(activity.getSaveMap().getOrDefault(1, 0));
        if (CheckNull.isEmpty(activity.getStatusMap())) {
            // TYPE_COMMON默认开启就解锁
            activity.getStatusMap().put(TYPE_COMMON, 1);
        }
        builder.addAllUnlockType(activity.getStatusMap().keySet());
        return builder.build();
    }


    public GamePb4.MusicFestivalBoxOfficeActionRs boxOfficeAction(Player player, GamePb4.MusicFestivalBoxOfficeActionRq req) throws MwException {
        int actType = req.getActType();
        //验证活动类型是否正确
        validateActivityType(actType);
        //获取并验证活动是否开启
        checkAndGetActivityBase(player, actType);

        Activity activity = checkAndGetActivity(player, actType);

        StaticMusicFestivalBoxOfficeParam sBoxOfficeConf = StaticMusicFestivalMgr.findBoxOfficeParamByActId(activity.getActivityId());
        if (Objects.isNull(sBoxOfficeConf)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), String.format("roleId :%d, s_box_office_param表缺少: %d的配置", player.roleId, activity.getActivityId()));
        }

        List<StaticMusicFestivalBoxOffice> boxOffices = StaticMusicFestivalMgr.findBoxOfficesByActId(activity.getActivityId());
        if (CheckNull.isEmpty(boxOffices)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), String.format("roleId :%d, s_box_office表缺少: %d的配置", player.roleId, activity.getActivityId()));
        }

        GamePb4.MusicFestivalBoxOfficeActionRs.Builder builder = GamePb4.MusicFestivalBoxOfficeActionRs.newBuilder();
        int level = activity.getSaveMap().getOrDefault(0, 0);
        Set<Integer> unLockTypes = activity.getStatusMap().keySet();
        int action = req.getAction();
        if (action == 1) {
            if (level >= sBoxOfficeConf.getPoints().size()) {
                throw new MwException(GameError.BOX_OFFICE_LEVEL_MAX.getCode(), String.format("roleId :%d, 音乐节售票处等级已达上限", player.roleId));
            }
            // 道具数量
            int count = req.getParam();
            int propId = sBoxOfficeConf.getPropId();
            rewardDataManager.checkPropIsEnough(player, propId, count);

            int exp = activity.getSaveMap().getOrDefault(1, 0);
            int addExp = count * sBoxOfficeConf.getExp();
            int afterExp = exp + addExp;
            if (afterExp > sBoxOfficeConf.getPoints().stream().max(Comparator.comparingInt(Integer::intValue)).orElse(0)) {
                throw new MwException(GameError.BOX_OFFICE_LEVEL_MAX.getCode(), String.format("roleId :%d, 音乐节售票处经验超出能获取的上限", player.roleId));
            }
            rewardDataManager.subAndSyncProp(player, AwardType.PROP, propId, count, AwardFrom.BOX_OFFICE_EXP_COST);
            activity.getSaveMap().put(1, afterExp);
            activity.getSaveMap().put(0, sBoxOfficeConf.getLevel(afterExp));
        } else if (action == 2) {
            List<List<Integer>> receiveAward = new ArrayList<>();
            List<StaticMusicFestivalBoxOffice> canReceiveAward = canReceiveAward(activity);
            if (CheckNull.isEmpty(canReceiveAward)) {
                throw new MwException(GameError.BOX_OFFICE_NONE_CAN_RECEIVE_AWARD.getCode(), String.format("roleId :%d, 音乐节售票处没有可领取的奖励", player.roleId));
            }
            canReceiveAward.stream()
                    // 记录已领取状态
                    .peek(conf -> {
                        activity.getPropMap().put(conf.getId(), 1);
                        // TODO: 2021/10/27 活动奖励领取埋点
                    })
                    .map(StaticMusicFestivalBoxOffice::getAward)
                    .forEach(receiveAward::addAll);
            // 合并奖励
            List<List<Integer>> mergeAward = RewardDataManager.mergeAward(receiveAward);
            List<CommonPb.Award> awards = rewardDataManager.addAwardDelaySync(player, mergeAward, null, AwardFrom.RECEIVE_BP_AWARD);
            if (!CheckNull.isEmpty(awards)) {
                builder.addAllAward(awards);
            }
        } else if (action == 3) {
            // 解锁的索引
            int index = req.getParam();
            Map<Integer, Integer> goldCost = sBoxOfficeConf.getGoldCost();
            if (CheckNull.isEmpty(goldCost) || !goldCost.containsKey(index)) {
                throw new MwException(GameError.NO_CONFIG.getCode(), String.format("roleId :%d, s_box_office_param表缺少: %d的配置, goldCost: %d", player.roleId, activity.getActivityId(), index));
            }
            if (unLockTypes.contains(index)) {
                throw new MwException(GameError.BOX_OFFICE_TYPE_GOLD_ALREADY_UNLOCK.getCode(), String.format("roleId :%d, 音乐节售票处已经解锁过钻石类型: %d", player.roleId, index));
            }
            rewardDataManager.checkAndSubPlayerRes(player, AwardType.MONEY, AwardType.Money.GOLD, goldCost.get(index), AwardFrom.BOX_OFFICE_GOLD_TYPE_UNLOCK_COST, true);
            activity.getStatusMap().put(index, 1);
        }

        return builder.setInfo(buildBoxOfficeInfo(activity)).build();
    }

    public void pay4BoxOffice(Player player, Pay pay, StaticPay sPay) {

        for (int actType : getActivityType()) {
            try {
                //验证活动类型是否正确
                validateActivityType(actType);
                //获取并验证活动是否开启
                checkAndGetActivityBase(player, actType);
                Activity activity = checkAndGetActivity(player, actType);
                StaticMusicFestivalBoxOfficeParam sBoxOfficeConf = StaticMusicFestivalMgr.findBoxOfficeParamByActId(activity.getActivityId());
                if (Objects.isNull(sBoxOfficeConf)) {
                    throw new MwException(GameError.NO_CONFIG.getCode(), String.format("roleId :%d, s_box_office表缺少: %d的配置", player.roleId, activity.getActivityId()));
                }
                Map<Integer, Integer> payId = sBoxOfficeConf.getPayId();
                // 不是该档位的活动
                if (CheckNull.isEmpty(payId) || !payId.containsValue(sPay.getPayId())) {
                    continue;
                }
                // 售票处的索引
                int index = payId.entrySet().stream().filter(en -> en.getValue() == sPay.getPayId()).map(Map.Entry::getKey).findAny().orElse(0);
                if (activity.getStatusMap().containsKey(index) || index == 0) {
                    throw new MwException(GameError.BOX_OFFICE_TYPE_PAY_ALREADY_UNLOCK.getCode(), String.format("roleId :%d, s_box_office表缺少: %d的配置", player.roleId, activity.getActivityId()));
                }

                List<StaticMusicFestivalBoxOffice> boxOffices = StaticMusicFestivalMgr.findBoxOfficesByActId(activity.getActivityId());
                if (CheckNull.isEmpty(boxOffices)) {
                    throw new MwException(GameError.NO_CONFIG.getCode(), String.format("roleId :%d, s_box_office表缺少: %d的配置", player.roleId, activity.getActivityId()));
                }
                activity.getStatusMap().put(index, 1);
            } catch (MwException e) {
                LogUtil.error("", e);

            }
        }
    }

    @Override
    protected void handleOnEndTime(int activityType, int activityId, int keyId) {
        playerDataManager.getPlayers().values().forEach(player -> autoConvertMail(player, activityType, activityId));

        //未领取的奖励发放
        Collection<Player> joinPlayers = playerDataManager.getAllPlayer().values();
        if (ObjectUtils.isEmpty(joinPlayers)) {
            return;
        }

        int now = TimeHelper.getCurrentSecond();
        joinPlayers.forEach(player -> {
            Activity activity = player.activitys.get(activityType);
            if (CheckNull.isNull(activity) || activity.getActivityId() != activityId)
                return;

            List<StaticMusicFestivalBoxOffice> canReceiveAward = canReceiveAward(activity);
            if (ObjectUtils.isEmpty(canReceiveAward))
                return;

            List<List<Integer>> receiveAward = new ArrayList<>();
            canReceiveAward.stream()
                    .peek(conf -> {
                        activity.getPropMap().put(conf.getId(), 1);
                    })
                    .map(StaticMusicFestivalBoxOffice::getAward)
                    .forEach(receiveAward::addAll);
            if (ObjectUtils.isEmpty(receiveAward))
                return;

            // 合并奖励
            List<List<Integer>> mergeAward = RewardDataManager.mergeAward(receiveAward);
            List<CommonPb.Award> awards = rewardDataManager.addAwardDelaySync(player, mergeAward, null, AwardFrom.RECEIVE_BP_AWARD);
            mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_ACT_UNREWARDED_REWARD,
                    AwardFrom.ACT_CREATIVE_OFFICE_CONDUCT_UNREWARDED_REWARD, now, activityType, activityId, activityType, activityId);
        });


    }
}
