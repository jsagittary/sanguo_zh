package com.gryphpoem.game.zw.service.activity;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.pb.ActivityPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;

import java.util.Objects;

/**
 * @Description
 * @Author zhangdh
 * @Date 2021-07-26 19:27
 */
public abstract class AbsSimpleActivityService extends AbsActivityService {

    public static final int SAVE_IDX_0 = 0;

    public int getActivityTips(ActivityBase base, Activity activity){
        return 0;
    }

    /**
     * 验证活动是否有效
     *
     * @param activityType
     * @throws MwException
     */
    protected void validateActivityType(int activityType) throws MwException {
        for (int actType : getActivityType()) {
            if (actType == activityType) {
                return;
            }
        }
        throw new MwException(GameError.PARAM_ERROR.getCode(), "活动类型 :", activityType, "不合法!!!");
    }

    @Override
    protected void handleOnEndTime(int activityType, int activityId, int keyId) {
        //do nothing
    }

    @Override
    protected void handleOnDisplayTime(int activityType, int activityId, int keyId) {
        //do nothing
    }

    @Override
    protected void handleOnDay(Player player) {
        //do nothing
    }

    @Override
    protected void handleOnBeginTime(int activityType, int activityId, int keyId) {
        //do nothing
    }

    @Override
    protected GeneratedMessage.Builder<GamePb4.GetActivityDataInfoRs.Builder> getActivityData(Player player, Activity activity, GlobalActivityData globalActivityData) throws Exception {
        return null;
    }

    protected <T> ActivityPb.ActivityData buildActivityData(int activityType, T t, GeneratedMessage.GeneratedExtension<ActivityPb.ActivityData, T> ext, int extNum) {
        ActivityPb.ActivityData.Builder baseBuilder = ActivityPb.ActivityData.newBuilder();
        baseBuilder.setActivityType(activityType);
        baseBuilder.setExtNum(extNum);
        if (Objects.nonNull(t)) {
            baseBuilder.setExtension(ext, t);
        }
        return baseBuilder.build();
    }

}
