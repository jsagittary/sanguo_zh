package com.gryphpoem.game.zw.service.activity;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticSummerMgr;
import com.gryphpoem.game.zw.manager.ChatDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.s.StaticSummerTurnplate;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;
import com.gryphpoem.game.zw.resource.util.ListUtils;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.RandomUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 夏日活动 - 夏日转盘
 * @author xwind
 * @date 2021/7/6
 */
@Service
public class SummerTurntableService extends AbsActivityService {

    private int[] actTypes = {ActivityConst.ACT_SUMMER_TURNPLATE};

    @Autowired
    private SummerCastleService summerCastleService;
    @Autowired
    private ChatDataManager chatDataManager;

    /**
     * 抽奖
     * @param roleId
     * @param count 1 or 10
     * @param actType 活动类型
     * @return
     * @throws MwException
     */
    public GamePb4.SummerTurntablePlayRs playTurntable(long roleId,int count,int actType) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        if(count != 1 && count != 10){
            throw new MwException(GameError.PARAM_ERROR.getCode(),GameError.err(roleId,"参数错误",count));
        }

        ActivityBase activityBase = super.checkAndGetActivityBase(player,actType);
        Activity activity = super.checkAndGetActivity(player,actType);
        if(!super.isOpenStage(activityBase)){
            throw new MwException(GameError.ACTIVITY_NO_OPEN.getCode(),GameError.err(roleId,"活动未开放",actType,activityBase.getStep0()));
        }

        int round = activity.getSaveMap().getOrDefault(0,1);
        List<StaticSummerTurnplate> configList = StaticSummerMgr.getStaticSummerTurnplateList(activity.getActivityId(),round);
        if(ListUtils.isBlank(configList)){
            throw new MwException(GameError.NO_CONFIG.getCode(),GameError.err(roleId,"配置不存在",activity.getActivityId(),round));
        }

        int realCount = count;
        if(count == 10){
            realCount = ActParamConstant.SUMMER_330.get(1);
        }

        long propCount = rewardDataManager.getRoleResByType(player,ActParamConstant.SUMMER_331.get(0),ActParamConstant.SUMMER_331.get(1));
        long gold = rewardDataManager.getRoleResByType(player, AwardType.MONEY, AwardType.Money.GOLD);
        List<List<Integer>> consumeList = new ArrayList<>();
        if(propCount >= realCount){//消耗道具
            consumeList.add(ListUtils.createItem(ActParamConstant.SUMMER_331.get(0),ActParamConstant.SUMMER_331.get(1),realCount));
        }else {//消耗道具+钻石
            long diffCount = realCount - propCount;
            long needGold = diffCount * ActParamConstant.SUMMER_330.get(0);
            if(gold < needGold){
                throw new MwException(GameError.GOLD_NOT_ENOUGH.getCode(),GameError.err(roleId,"夏日转盘钻石不够",count,propCount,gold,diffCount,needGold));
            }
            if(propCount > 0){
                consumeList.add(ListUtils.createItem(ActParamConstant.SUMMER_331.get(0),ActParamConstant.SUMMER_331.get(1), (int) propCount));
            }
            if(needGold > 0){
                consumeList.add(ListUtils.createItem(AwardType.MONEY, AwardType.Money.GOLD, (int) needGold));
            }
        }
        rewardDataManager.subPlayerResHasChecked(player,consumeList,true,AwardFrom.SUMMER_TURNTABLE_PLAY);

        GamePb4.SummerTurntablePlayRs.Builder resp = GamePb4.SummerTurntablePlayRs.newBuilder();
        resp.setActType(actType);

        if(ListUtils.isNotBlank(configList)) {
            List<CommonPb.Award> awardList = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                //检查并去掉特殊奖励
                List<StaticSummerTurnplate> tmpList = new ArrayList<>();
                configList.forEach(o -> {
                    if(o.getType() != 1 || (o.getType() == 1 && !isGotAllSpecial(activity,o))){
                        tmpList.add(o);
                    }
                });
                StaticSummerTurnplate hitStatic = RandomUtil.getWeightByList(tmpList, StaticSummerTurnplate::getWeight);
                //命中特殊奖励，过滤掉已获得的奖励
                List<List<Integer>> tmpAwardList = new ArrayList<>();
                if(hitStatic.getType() == 1){
                    hitStatic.getAward().forEach(o -> {
                        if(!activity.getPropMap().containsKey(o.get(1))){
                            tmpAwardList.add(o);
                        }
                    });
                }else {
                    tmpAwardList.addAll(hitStatic.getAward());
                }
                List<Integer> hitAward = RandomUtil.randomAwardByWeight(tmpAwardList);
                CommonPb.Award award = PbHelper.createAward(hitAward);
                awardList.add(award);
                resp.addPlayOne(buildSummerTurntablePlayOne(hitStatic.getSort(), award));

                //判断特殊奖励
                if (hitStatic.getType() == 1) {
                    activity.getPropMap().put(hitAward.get(1), hitAward.get(2));

                    //特殊奖励跑马灯
                    chatDataManager.sendSysChat(ChatConst.CHAT_FAMOUS_GENERAL_TURNPLATE_GLOBAL_NUM,player.lord.getCamp(), 0,
                            player.lord.getCamp(), player.lord.getNick(), hitAward.get(0),hitAward.get(1), hitAward.get(2), activityBase.getActivityId());
                }
            }
            if (!awardList.isEmpty()) {
                rewardDataManager.sendRewardByAwardList(player, awardList, AwardFrom.SUMMER_TURNTABLE_PLAY);
            }
        }
        resp.setSummerTurntableInfo(buildSummerTurntableInfo(activity));

        summerCastleService.updateScore(player,count,AwardFrom.SUMMER_TURNTABLE_PLAY);

        return resp.build();
    }

    /**
     * 下一轮
     * @param roleId
     * @param actType
     * @return
     * @throws MwException
     */
    public GamePb4.SummerTurntableNextRs nextRound(long roleId,int actType) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        ActivityBase activityBase = checkAndGetActivityBase(player,actType);
        Activity activity = checkAndGetActivity(player,actType);
        if(!super.isOpenStage(activityBase)){
            throw new MwException(GameError.ACTIVITY_NO_OPEN.getCode(),GameError.err(roleId,"下一轮,活动未开放",actType,activityBase.getStep0()));
        }
        int round = activity.getSaveMap().getOrDefault(0,1);
        List<StaticSummerTurnplate> staticSummerTurnplateList = StaticSummerMgr.getStaticSummerTurnplateList(activity.getActivityId(),round);
        if(ListUtils.isBlank(staticSummerTurnplateList)){
            throw new MwException(GameError.NO_CONFIG.getCode(),GameError.err(roleId,"下一轮,当前轮没有配置",actType,round));
        }
        StaticSummerTurnplate specialStatic = staticSummerTurnplateList.stream().filter(o -> o.getType() == 1).findFirst().orElse(null);
        if(!isGotAllSpecial(activity,specialStatic)){
            throw new MwException(GameError.SUMMER_NOT_NEXT_ROUND.getCode(),GameError.err(roleId,"下一轮条件未满足",actType,round));
        }
        //下一轮
        int nextRound = round + 1;
        staticSummerTurnplateList = StaticSummerMgr.getStaticSummerTurnplateList(activity.getActivityId(),nextRound);
        if(Objects.isNull(staticSummerTurnplateList)){
            //没有下一轮，则设置到第一轮
            nextRound = 1;
        }
        activity.getSaveMap().put(0,nextRound);
        activity.getPropMap().clear();

        GamePb4.SummerTurntableNextRs.Builder resp = GamePb4.SummerTurntableNextRs.newBuilder();
        resp.setActType(actType);
        resp.setSummerTurntableInfo(buildSummerTurntableInfo(activity));

        return resp.build();
    }

    private boolean isGotAllSpecial(Activity activity,StaticSummerTurnplate config){
        for (List<Integer> list : config.getAward()) {
            if(!activity.getPropMap().containsKey(list.get(1))){
                return false;
            }
        }
        return true;
    }

    private CommonPb.SummerTurntablePlayOne buildSummerTurntablePlayOne(int hitSort, CommonPb.Award award){
        CommonPb.SummerTurntablePlayOne.Builder builder = CommonPb.SummerTurntablePlayOne.newBuilder();
        builder.setHitSort(hitSort);
        builder.addAward(award);
        return builder.build();
    }

    @Override
    protected GeneratedMessage.Builder<GamePb4.GetActivityDataInfoRs.Builder> getActivityData(Player player, Activity activity, GlobalActivityData globalActivityData) {
        GamePb4.GetActivityDataInfoRs.Builder builder = GamePb4.GetActivityDataInfoRs.newBuilder();
        builder.setSummerTurntableInfo(buildSummerTurntableInfo(activity));
        return builder;
    }

    private CommonPb.SummerTurntableInfo buildSummerTurntableInfo(Activity activity){
        CommonPb.SummerTurntableInfo.Builder builder = CommonPb.SummerTurntableInfo.newBuilder();
        builder.setCurrRound(activity.getSaveMap().getOrDefault(0,1));
        Optional.ofNullable(StaticSummerMgr.getStaticSummerTurnplateList(activity.getActivityId(),builder.getCurrRound())).
                ifPresent(o -> Optional.ofNullable(o.stream().filter(o1 -> o1.getType() == 1).findFirst().orElse(null)).
                        ifPresent(o1 -> builder.setIsAllSpecial(isGotAllSpecial(activity,o1))));
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
