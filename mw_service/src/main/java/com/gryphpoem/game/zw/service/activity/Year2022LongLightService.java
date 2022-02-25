package com.gryphpoem.game.zw.service.activity;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.s.StaticActLogin;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;
import com.gryphpoem.game.zw.resource.util.ListUtils;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 长明灯
 * 点灯次数放在 saveMap
 * 点灯记录放在 statusMap
 * @author xwind
 * @date 2021/12/17
 */
@Service
public class Year2022LongLightService extends AbsActivityService {
    private int[] actType = {ActivityConst.ACT_NEWYEAR_2022_LONGLIGHT};

    public GamePb5.LongLightIgniteRs ignite(long roleId,int actType) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = super.checkAndGetActivity(player,actType);
        ActivityBase activityBase = super.checkAndGetActivityBase(player,actType);
        if(!super.isOpenStage(activityBase)){
            throw new MwException(GameError.ACTIVITY_NO_OPEN.getCode(),GameError.err(roleId,"长明灯活动未开放",actType,activityBase.getStep0()));
        }
        int igniteTimes = activity.getSaveMap().getOrDefault(0,0);
        if(igniteTimes >= StaticActivityDataMgr.getMaxActLogin(activity.getActivityId()).getTime()){
            throw new MwException(GameError.LONG_LIGHT_IGNITE_TIMES_LIMIT.getCode(),GameError.err(roleId,"长明灯活动点灯上限"));
        }
        int today = TimeHelper.getCurrentDay();
        if(Objects.nonNull(activity.getStatusMap().get(today))){
            throw new MwException(GameError.LONG_LIGHT_IGNITED.getCode(),GameError.err(roleId,"长明灯活动今日已点灯"));
        }
        List<List<Integer>> awardList = Constant.LONG_LIGHT_DAY_AWARD.stream().filter(tmps -> tmps.get(0)==activity.getActivityId()).collect(Collectors.toList());
        if(ListUtils.isBlank(awardList)){
            throw new MwException(GameError.LONG_LIGHT_IGNITE_DAYAWARD_ERROR.getCode(),GameError.err(roleId,"长明灯活动每日奖励未配置"));
        }
        List<List<Integer>> awardList_ = new ArrayList<>();
        for (List<Integer> list : awardList) {
            awardList_.add(ListUtils.createItem(list.get(1),list.get(2),list.get(3)));
        }
        List<CommonPb.Award> awards = rewardDataManager.sendReward(player,awardList_, AwardFrom.LONG_LIGHT_IGNITE);
        activity.getSaveMap().put(0,++igniteTimes);
        activity.getStatusMap().put(today,0);

        GamePb5.LongLightIgniteRs.Builder resp = GamePb5.LongLightIgniteRs.newBuilder();
        resp.setActType(actType);
        resp.addAllAwards(awards);
        resp.setLongLightInfo(buildLongLightInfo(activity));
        return resp.build();
    }

    private CommonPb.LongLightInfo buildLongLightInfo(Activity activity){
        CommonPb.LongLightInfo.Builder builder = CommonPb.LongLightInfo.newBuilder();
        builder.setIgniteTimes(activity.getSaveMap().getOrDefault(0,0));
        builder.setState(Objects.isNull(activity.getStatusMap().get(TimeHelper.getCurrentDay()))?0:1);
        return builder.build();
    }

    @Override
    protected GeneratedMessage.Builder<GamePb4.GetActivityDataInfoRs.Builder> getActivityData(Player player, Activity activity, GlobalActivityData globalActivityData) throws MwException {
        GamePb4.GetActivityDataInfoRs.Builder builder = GamePb4.GetActivityDataInfoRs.newBuilder();
        builder.setLongLightInfo(buildLongLightInfo(activity));
        return builder;
    }

    @Override
    protected int[] getActivityType() {
        return actType;
    }

    @Override
    protected void handleOnBeginTime(int activityType, int activityId, int keyId) {

    }

    @Override
    protected void handleOnEndTime(int activityType, int activityId, int keyId) {
        playerDataManager.getPlayers().values().forEach(player -> {
            Activity activity = player.activitys.get(activityType);
            if(Objects.nonNull(player) && Objects.nonNull(activity)){
                int times = activity.getSaveMap().getOrDefault(0,0);
                StaticActLogin staticActLogin = StaticActivityDataMgr.getActLogin(activityId,times);
                if(Objects.nonNull(staticActLogin)){
                    List<CommonPb.Award> awards = PbHelper.createAwardsPb(staticActLogin.getAwardList());
                    mailDataManager.sendAttachMail(player, awards, MailConstant.MOD_LONG_LIGHT, AwardFrom.LONG_LIGHT_TOTAL_AWARD, TimeHelper.getCurrentSecond(),activityId, activity.getSaveMap().getOrDefault(0,0));
                }else {
                    LogUtil.activity(String.format("活动结束时处理奖励找不到配置,actType=%s,actId=%s,times=%s",activityType,activityId,times));
                }
            }else {
                LogUtil.activity(String.format("活动结束时处理奖励异常player或activity为null,actType=%s,actId=%s",activityType,activityId));
            }
        });
    }

    @Override
    protected void handleOnDisplayTime(int activityType, int activityId, int keyId) {

    }

    @Override
    protected void handleOnDay(Player player) {

    }
}
