package com.gryphpoem.game.zw.service.activity;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.s.StaticActAward;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;
import com.gryphpoem.game.zw.resource.util.ActivityUtil;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 周年庆-拼图活动
 * <p>
 * saveMap：key=拼图索引 value=0 未拼 value=1 已拼<br/>
 * statusMap：领奖的记录
 * </p>
 *
 * @description:
 * @author: zhou jie
 * @time: 2021/7/22 16:53
 */
@Service
public class AnniversaryJigsawService extends AbsActivityService {

    private int[] actTypes = {ActivityConst.ACT_ANNIVERSARY_JIGSAW};

    /**
     * 拼图活动
     *
     * @param req          请求参数
     * @param roleId       玩家id
     * @param activityType 活动类型
     * @return 拼图活动数据
     * @throws MwException 活动数据异常
     */
    public GamePb4.AnniversaryJigsawRs anniversaryJigsaw(GamePb4.AnniversaryJigsawRq req, long roleId, int activityType) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = checkAndGetActivity(player, activityType);
        ActivityBase activityBase = checkAndGetActivityBase(player, activityType);
        if (!isOpenStage(activityBase)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), GameError.err(roleId, "周年庆-拼图活动未开启", activityType));
        }
        if (!req.hasAction()) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "周年庆-拼图活动请求参数action缺失", activityType));
        }
        int action = req.getAction();
        GamePb4.AnniversaryJigsawRs.Builder builder = GamePb4.AnniversaryJigsawRs.newBuilder();
        // 拼图
        if (action == 1) {
            // 拼图总数
            int jigsawSum = getJigsawSum(activity);
            if (activity.getSaveMap().size() >= jigsawSum) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "周年庆-拼图活动拼图, 所有拼图都已完成", activityType));
            }
            List<Integer> jigsawRecord = Stream.iterate(1, i -> ++i).limit(jigsawSum).filter(index -> activity.getSaveMap().getOrDefault(index, 0) == 0).collect(Collectors.toList());
            if (CheckNull.isEmpty(jigsawRecord)) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "周年庆-拼图活动拼图, 所有拼图都已完成", activityType));
            }
            // 检查并消耗资源或道具
            rewardDataManager.checkAndSubPlayerRes(player, AwardType.PROP, PropConstant.PROP_ID_JIGSAW, 1, AwardFrom.ANNIVERSARY_JIGSAW_CONSUME, true, "拼图消耗道具");
            Collections.shuffle(jigsawRecord);
            // 这里默认拼一次
            jigsawRecord.stream()
                    .limit(1)
                    .findAny()
                    .ifPresent(index -> {
                        activity.getSaveMap().put(index, 1);
                        builder.setIndex(index);
                    });
        } else if (action == 2) {
            // 领奖
            if (!req.hasKeyId()) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "周年庆-拼图活动领奖, 请求参数keyId缺失", activityType));
            }
            int keyId = req.getKeyId();
            StaticActAward actAward = StaticActivityDataMgr.getActAward(keyId);
            if (Objects.isNull(actAward)) {
                throw new MwException(GameError.NO_CONFIG.getCode(), GameError.err(roleId, "领取拼图奖励, 未配置", activityType, keyId));
            }
            if (actAward.getActivityId() != activity.getActivityId()) {
                throw new MwException(GameError.INVALID_PARAM.getCode(), GameError.err(roleId, "领取拼图奖励, 参数错误", activityType, activity.getActivityId(), keyId));
            }
            // 需要拼图数量
            int need = actAward.getParam().get(0);
            if (activity.getSaveMap().size() < need) {
                throw new MwException(GameError.ACTIVITY_AWARD_NOT_GET.getCode(), GameError.err(roleId, "领取拼图奖励, 未满足条件", activityType, keyId));

            }
            int state = activity.getStatusMap().getOrDefault(keyId, 0);
            if (state != 0) {
                throw new MwException(GameError.ACTIVITY_AWARD_GOT.getCode(), GameError.err(roleId, "领取拼图奖励, 已领取", activityType, keyId));
            }
            // 领取奖励
            activity.getStatusMap().put(keyId, 1);
            List<CommonPb.Award> awardList = rewardDataManager.sendReward(player, actAward.getAwardList(), AwardFrom.ANNIVERSARY_JIGSAW_AWARD);
            if (!CheckNull.isEmpty(awardList)) {
                builder.addAllAwards(awardList);
            }
        }
        builder.setAction(action);
        builder.setActType(activityType);
        builder.setInfo(buildAnniversaryJigsaw(activity));
        return builder.build();
    }

    /**
     * 获取当前活动碎片的总数
     *
     * @param activity 活动
     * @return s_act_award表中param[n] n最大值就是碎片的总数
     */
    private int getJigsawSum(Activity activity) {
        List<StaticActAward> actAwardList = StaticActivityDataMgr.getActAwardById(activity.getActivityId());
        if (!CheckNull.isEmpty(actAwardList)) {
            return actAwardList.stream().map(StaticActAward::getParam).filter(param -> !CheckNull.isEmpty(param)).mapToInt(param -> param.get(0)).max().orElse(0);
        }
        return 0;
    }

    @Override
    protected GeneratedMessage.Builder<GamePb4.GetActivityDataInfoRs.Builder> getActivityData(Player player, Activity activity, GlobalActivityData globalActivityData) {
        GamePb4.GetActivityDataInfoRs.Builder builder = GamePb4.GetActivityDataInfoRs.newBuilder();
        builder.setAnniversaryJigsaw(buildAnniversaryJigsaw(activity));
        return builder;
    }

    private CommonPb.AnniversaryJigsaw buildAnniversaryJigsaw(Activity activity) {
        CommonPb.AnniversaryJigsaw.Builder builder = CommonPb.AnniversaryJigsaw.newBuilder();
        builder.addAllGotIds(activity.getStatusMap().keySet());
        builder.addAllJigsaw(activity.getSaveMap().keySet());
        return builder.build();
    }

    @Override
    protected int[] getActivityType() {
        return actTypes;
    }

    @Override
    protected void handleOnBeginTime(int activityType, int activityId, int keyId) {

    }

    /**
     * 活动结束时, 自动兑换拼图道具
     * @param activityType 活动类型
     * @param activityId 活动id
     * @param keyId 活动planId
     */
    @Override
    protected void handleOnEndTime(int activityType, int activityId, int keyId) {
        Optional.ofNullable(ActivityUtil.getActivityBase(keyId))
                .ifPresent(activityBase -> ActivityUtil.sendConvertMail(activityBase, AwardFrom.ACT_OVER_AUTO_EXCHANGE_NON_USED_ITEMS));
    }

    @Override
    protected void handleOnDisplayTime(int activityType, int activityId, int keyId) {

    }

    @Override
    protected void handleOnDay(Player player) {

    }
}
