package com.gryphpoem.game.zw.service.activity;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticSummerMgr;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.MailConstant;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.s.StaticSummerCharge;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;
import com.gryphpoem.game.zw.resource.util.ListUtils;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.PlayerService;
import com.gryphpoem.game.zw.service.RechargeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 夏日活动 - 每日连续充值
 * @author xwind
 * @date 2021/7/6
 */
@Service
public class SummerChargeService extends AbsActivityService implements RechargeService {

    private int[] actTypes = {ActivityConst.ACT_SUMMER_CHARGE};

    @Autowired
    private PlayerService playerService;

    @Override
    protected GeneratedMessage.Builder<GamePb4.GetActivityDataInfoRs.Builder> getActivityData(Player player, Activity activity, GlobalActivityData globalActivityData) {
        GamePb4.GetActivityDataInfoRs.Builder builder = GamePb4.GetActivityDataInfoRs.newBuilder();
        builder.setDailyKeepRechargeInfo(buildDailyKeepRechargeInfo(activity));
        return builder;
    }

    public GamePb4.DailyKeepRechargeGetAwardRs getAward(long roleId,int confId,int activityType) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        ActivityBase activityBase = super.checkAndGetActivityBase(player,activityType);
        Activity activity = super.checkAndGetActivity(player,activityType);
        if(activityBase.getStep0() != ActivityConst.OPEN_STEP){
            throw new MwException(GameError.ACTIVITY_NO_OPEN.getCode(),GameError.err(roleId,"活动未开放",activityType,activityBase.getStep0()));
        }

        StaticSummerCharge staticSummerCharge = StaticSummerMgr.getStaticSummerChargeMap().get(confId);
        if(Objects.isNull(staticSummerCharge)){
            throw new MwException(GameError.INVALID_PARAM.getCode(),GameError.err(roleId,"配置不存在",confId));
        }
        if(staticSummerCharge.getActivityId() != activityBase.getActivityId()){
            throw new MwException(GameError.INVALID_PARAM.getCode(),GameError.err(roleId,"confId"));
        }
        List<CommonPb.Award> getAwards;
        if(staticSummerCharge.getType() == 1){
            int today = TimeHelper.getCurrentDay();
            int dayAmount = activity.getSaveMap().getOrDefault(today,0);
            if(dayAmount < staticSummerCharge.getOrder()){
                throw new MwException(GameError.INVALID_PARAM.getCode(),GameError.err(roleId,"领取每日奖励失败,充值金额不足",confId,dayAmount));
            }
            int dayState = activity.getStatusMap().getOrDefault(confId,0);
            if(dayState != 0 && dayState==today){
                throw new MwException(GameError.INVALID_PARAM.getCode(),GameError.err(roleId,"领取每日奖励失败,今日已领取",confId,dayState));
            }

            getAwards = PbHelper.createAwardsPb(staticSummerCharge.getAward());
            rewardDataManager.sendRewardByAwardList(player,getAwards, AwardFrom.SUMMER_CHARGE_GET_DAY_AWARD);

            activity.getStatusMap().put(confId,today);
        }else {
            int totalAmount = activity.getSaveMap().getOrDefault(0,0);
            if(totalAmount < staticSummerCharge.getOrder()){
                throw new MwException(GameError.INVALID_PARAM.getCode(),GameError.err(roleId,"领取累计奖励失败,充值金额不足",confId,totalAmount));
            }
            int totalState = activity.getStatusMap().getOrDefault(confId,0);
            if(totalState != 0){
                throw new MwException(GameError.INVALID_PARAM.getCode(),GameError.err(roleId,"领取累计奖励失败,已领取",confId,totalState));
            }

            getAwards = PbHelper.createAwardsPb(staticSummerCharge.getAward());
            rewardDataManager.sendRewardByAwardList(player,getAwards, AwardFrom.SUMMER_CHARGE_GET_TOTAL_AWARD);

            activity.getStatusMap().put(confId,1);
        }

        GamePb4.DailyKeepRechargeGetAwardRs.Builder resp = GamePb4.DailyKeepRechargeGetAwardRs.newBuilder();
        resp.addAllAward(getAwards);
        resp.setInfo(this.buildDailyKeepRechargeInfo(activity));
        resp.setActType(activityType);
        return resp.build();
    }

    @Override
    protected int[] getActivityType() {
        return actTypes;
    }

    @Override
    protected void handleOnBeginTime(int activityType, int activityId, int keyId) {

    }

    @Override
    protected void handleOnEndTime(int activityType, int activityId, int keyId) {
        playerDataManager.getPlayers().values().forEach(player -> {
            Activity activity = player.activitys.get(activityType);
            if(Objects.nonNull(activity)){
                int totalAmount = activity.getSaveMap().getOrDefault(0,0);
                List<StaticSummerCharge> list = StaticSummerMgr.getStaticSummerChargeGroupMap().get(activityId);
                List<List<Integer>> nonGets = new ArrayList<>();
                list.forEach(tmp -> {
                    if(tmp.getType() != 1){
                        if(totalAmount >= tmp.getOrder() && activity.getStatusMap().getOrDefault(tmp.getId(),0) == 0){
                            nonGets.addAll(tmp.getAward());
                            activity.getStatusMap().put(tmp.getId(),1);
                        }
                    }
                });
                if(ListUtils.isNotBlank(nonGets)){
                    List<CommonPb.Award> nonAwards = PbHelper.createAwardsPb(nonGets);
                    mailDataManager.sendAttachMail(player,nonAwards, MailConstant.MOLD_ACT_UNREWARDED_REWARD,AwardFrom.SUMMER_CHARGE_GET_TOTAL_AWARD, TimeHelper.getCurrentSecond(),activityType,activityId,activityType,activityId);
                }
            }
        });
    }

    @Override
    protected void handleOnDisplayTime(int activityType, int activityId, int keyId) {

    }

    @Override
    protected void handleOnDay(Player player) {
//        for (int type : getActivityType()) {
//            ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(type);
//        }
//        int today = TimeHelper.getCurrentDay();
//        int dayAmount = activity.getSaveMap().getOrDefault(today,0);
    }

    @Override
    public void afterRecharge(Player player, int amount, int diamond) {
        for (int type : this.getActivityType()) {
            if(!isOpenStage(type)){
                return;
            }
            Activity activity = getActivity(player,type);
            Optional.ofNullable(activity).ifPresent(act -> {
                act.getSaveMap().merge(0,amount,Integer::sum);
                act.getSaveMap().merge(TimeHelper.getCurrentDay(),amount,Integer::sum);
                syncDailyKeepRechargeInfo(player,type);
            });
        }
    }

    private void syncDailyKeepRechargeInfo(Player player,int actType){
        GamePb4.SyncDailyKeepRechargeInfoRs.Builder builder = GamePb4.SyncDailyKeepRechargeInfoRs.newBuilder();
        builder.setInfo(buildDailyKeepRechargeInfo(getActivity(player,actType)));
        BasePb.Base msg = PbHelper.createSynBase(GamePb4.SyncDailyKeepRechargeInfoRs.EXT_FIELD_NUMBER, GamePb4.SyncDailyKeepRechargeInfoRs.ext, builder.build()).build();
        playerService.syncMsgToPlayer(msg,player);
    }

    private CommonPb.DailyKeepRechargeInfo buildDailyKeepRechargeInfo(Activity activity){
        CommonPb.DailyKeepRechargeInfo.Builder builder = CommonPb.DailyKeepRechargeInfo.newBuilder();
        int day = TimeHelper.getCurrentDay();
        builder.setTodayAmount(activity.getSaveMap().getOrDefault(day,0));
        builder.setTotalAmount(activity.getSaveMap().getOrDefault(0,0));
        List<StaticSummerCharge> staticSummerChargeList = StaticSummerMgr.getStaticSummerChargeGroupMap().get(activity.getActivityId());
        staticSummerChargeList.forEach(obj -> {
            int state = activity.getStatusMap().getOrDefault(obj.getId(),0);
            if(obj.getType() == 1){
                int today = TimeHelper.getCurrentDay();
                builder.addAwardState(PbHelper.createTwoIntPb(obj.getId(),(state==0||state!=today)?0:1));
            }else {
                builder.addAwardState(PbHelper.createTwoIntPb(obj.getId(),state));
            }
        });
        return builder.build();
    }

    // <editor-fold desc="GM命令" defaultstate="collapsed">
    public void test_clear(Player player,int actType) throws MwException {
        Activity activity = checkAndGetActivity(player,actType);
        GlobalActivityData globalActivityData = checkAndGetGlobalActivity(player,actType);
        activity.getSaveMap().clear();
        activity.getPropMap().clear();
        activity.getStatusMap().clear();
        globalActivityData.setGoal(0);

    }
    // </editor-fold>
}
