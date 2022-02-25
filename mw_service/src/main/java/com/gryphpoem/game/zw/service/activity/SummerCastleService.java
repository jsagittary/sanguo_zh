package com.gryphpoem.game.zw.service.activity;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
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
import com.gryphpoem.game.zw.resource.domain.s.StaticSummerCastle;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;

/**
 * 夏日活动 - 沙雕
 * @author xwind
 * @date 2021/7/6
 */
@Service
public class SummerCastleService extends AbsActivityService {

    private int[] actTypes = {ActivityConst.ACT_SUMMER_CASTLE};

    @Autowired
    private PlayerService playerService;

    public GamePb4.SummerCastleGetAwardRs getAward(long roleId,int confId,int actType) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        ActivityBase activityBase = checkAndGetActivityBase(player,actType);
        Activity activity = checkAndGetActivity(player,actType);
        if(activityBase.getStep0() != ActivityConst.OPEN_STEP){
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(),GameError.err(roleId,"领取沙雕奖励，没活动未开放",actType));
        }
        StaticSummerCastle staticSummerCastle = StaticSummerMgr.getStaticSummerCastleMap().get(confId);
        if(Objects.isNull(staticSummerCastle)){
            throw new MwException(GameError.NO_CONFIG.getCode(),GameError.err(roleId,"领取沙雕奖励，未配置",actType,confId));
        }
        if(staticSummerCastle.getActivityId() != activity.getActivityId()){
            throw new MwException(GameError.INVALID_PARAM.getCode(),GameError.err(roleId,"领取沙雕奖励，参数错误",actType,activity.getActivityId(),confId));
        }
        int state = activity.getStatusMap().getOrDefault(confId,0);
        if(state != 0){
            throw new MwException(GameError.ACTIVITY_AWARD_GOT.getCode(),GameError.err(roleId,"领取沙雕奖励，已领取",actType,confId));
        }
        GlobalActivityData globalActivityData = checkAndGetGlobalActivity(player,actType);
        if(globalActivityData.getGoal() < staticSummerCastle.getOrder()){
            throw new MwException(GameError.ACTIVITY_AWARD_NOT_GET.getCode(),GameError.err(roleId,"领取沙雕奖励，条件不足",actType,confId,globalActivityData.getGoal()));
        }

        rewardDataManager.sendReward(player,staticSummerCastle.getAward(), AwardFrom.SUMMER_CASTLE_GET_AWARD);
        activity.getStatusMap().put(confId,2);

        GamePb4.SummerCastleGetAwardRs.Builder resp = GamePb4.SummerCastleGetAwardRs.newBuilder();
        resp.setActType(actType);
        resp.setSandCarvingInfo(buildSandCarvingInfo(activity,globalActivityData));
        return resp.build();
    }

    public void updateScore(Player player,int n,AwardFrom awardFrom){
        try {
            for (int type : getActivityType()) {
                ActivityBase activityBase = checkAndGetActivityBase(player,type);
                if(activityBase.getStep0() != ActivityConst.OPEN_STEP){
                    continue;
                }
                GlobalActivityData globalActivityData = checkAndGetGlobalActivity(player, type);
                globalActivityData.setGoal(globalActivityData.getGoal() + n);

                syncScoreToAll(activityBase,globalActivityData);

                LogLordHelper.activityScore("SummerCastle",awardFrom,player,globalActivityData.getGoal(),n,type);
            }
        }catch (Exception e) {
            LogUtil.error(e,"更新夏日城堡积分错误,roleId=" + player.roleId);
        }
    }

    private void syncScoreToAll(ActivityBase activityBase,GlobalActivityData globalActivityData){
        GamePb4.SyncSummerCastleRs.Builder builder = GamePb4.SyncSummerCastleRs.newBuilder();
        builder.setActType(activityBase.getActivityType());
        playerDataManager.getAllOnlinePlayer().values().forEach(p -> {
            Activity activity = p.activitys.get(activityBase.getActivityType());
            if(Objects.nonNull(activity)){
                builder.setInfo(buildSandCarvingInfo(activity,globalActivityData));
                BasePb.Base msg = PbHelper.createSynBase(GamePb4.SyncSummerCastleRs.EXT_FIELD_NUMBER, GamePb4.SyncSummerCastleRs.ext, builder.build()).build();
                playerService.syncMsgToPlayer(msg,p);
            }
        });
//        BasePb.Base msg = PbHelper.createSynBase(GamePb4.SyncSummerCastleRs.EXT_FIELD_NUMBER, GamePb4.SyncSummerCastleRs.ext, builder.build()).build();
//        Map<String,Object> params = new HashMap<>(3);
//        params.put("actType",activityBase.getActivityType());
//        params.put("actId",activityBase.getActivityId());
//        playerService.syncMsgToAll(msg, checkCanGetAward,params);
//        playerService.syncMsgToAll(msg);
    }

    private Function<Map, Boolean> checkCanGetAward = map -> {
        Player player = (Player) map.get("player");
        int actType = (int) map.get("actType");
        int actId = (int) map.get("actId");
        List<StaticSummerCastle> list = StaticSummerMgr.getStaticSummerCastleListByActivityId(actId);
        if(CheckNull.isEmpty(list)){
            return false;
        }
        GlobalActivityData globalActivityData = activityDataManager.getGlobalActivity(actType);
        if(Objects.isNull(globalActivityData)){
            return false;
        }
        Activity activity = player.activitys.get(actType);
        if(Objects.isNull(activity)){
            return false;
        }
        int globalVal = globalActivityData.getGoal();
        for (StaticSummerCastle staticSummerCastle : list) {
            int state = activity.getStatusMap().getOrDefault(staticSummerCastle.getId(),0);
            if(state == 0 && globalVal >= staticSummerCastle.getOrder()){
                return true;
            }
        }
        return false;
    };

    @Override
    protected GeneratedMessage.Builder<GamePb4.GetActivityDataInfoRs.Builder> getActivityData(Player player, Activity activity, GlobalActivityData globalActivityData) {
        GamePb4.GetActivityDataInfoRs.Builder builder = GamePb4.GetActivityDataInfoRs.newBuilder();
        builder.setSandCarvingInfo(buildSandCarvingInfo(activity,globalActivityData));
        return builder;
    }

    private CommonPb.SandCarvingInfo buildSandCarvingInfo(Activity activity,GlobalActivityData globalActivityData){
        CommonPb.SandCarvingInfo.Builder builder = CommonPb.SandCarvingInfo.newBuilder();
        builder.setScore(globalActivityData.getGoal());
        Optional.ofNullable(StaticSummerMgr.getStaticSummerCastleListByActivityId(activity.getActivityId())).ifPresent(tmps -> tmps.forEach(tmp -> {
            int state = activity.getStatusMap().getOrDefault(tmp.getId(),0);
            if(state == 0 && globalActivityData.getGoal() >= tmp.getOrder()){
                state = 1;
            }
            builder.addAwardState(PbHelper.createTwoIntPb(tmp.getId(),state));
        }));
        return builder.build();
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
            GlobalActivityData globalActivityData = activityDataManager.getActivityMap().get(activityType);
            if(Objects.nonNull(activity) && Objects.nonNull(globalActivityData)){
                List<StaticSummerCastle> list = StaticSummerMgr.getStaticSummerCastleListByActivityId(activityId);
                List<List<Integer>> nonGets = new ArrayList<>();
                Optional.ofNullable(list).ifPresent(tmps -> tmps.forEach(tmp -> {
                    int state = activity.getStatusMap().getOrDefault(tmp.getId(),0);
                    if(state == 0 && globalActivityData.getGoal() >= tmp.getOrder()){
                        nonGets.addAll(tmp.getAward());
                        activity.getStatusMap().put(tmp.getId(),2);
                    }
                }));
                if(ListUtils.isNotBlank(nonGets)){
                    List<CommonPb.Award> nonAwards = PbHelper.createAwardsPb(nonGets);
                    mailDataManager.sendAttachMail(player,nonAwards, MailConstant.MOLD_ACT_UNREWARDED_REWARD,AwardFrom.SUMMER_CASTLE_GET_AWARD, TimeHelper.getCurrentSecond(),activityType,activityId,activityType,activityId);
                }
            }
        });
    }

    @Override
    protected void handleOnDisplayTime(int activityType, int activityId, int keyId) {

    }

    @Override
    protected void handleOnDay(Player player) {

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
