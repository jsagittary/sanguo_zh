package com.gryphpoem.game.zw.service.activity;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticTurnplateMgr;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.s.StaticAutumnTurnplate;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;
import com.gryphpoem.game.zw.resource.util.ListUtils;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.RandomUtil;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.GmCmd;
import com.gryphpoem.game.zw.service.GmCmdService;
import com.gryphpoem.game.zw.service.PlayerService;
import com.gryphpoem.game.zw.service.RefreshTimerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 丰收转盘
 * 1、已使用的免费次数放在propMap, key=-1
 *      刷新免费次数放在propMap， key=-4
 * 2、进度放在propMap, key=-2
 * 3、下次刷新时间戳放在propMap, key=-3
 * 4、已领取的进度奖励放在propMap,key=配置id
 * 5、转盘奖励放在saveMap, key=转盘索引, value=奖励索引；奖励是否已获得key=0-转盘索引, value=状态 1已获得 0未获得
 *
 * @author xwind
 * @date 2021/9/14
 */
@Service
public class AutumnTurnplateService extends AbsActivityService implements RefreshTimerService, GmCmdService {

    private int[] actTypes = {ActivityConst.ACT_AUTUMN_TURNPLATE};

    @Autowired
    private PlayerService playerService;

    @Override
    protected GeneratedMessage.Builder<GamePb4.GetActivityDataInfoRs.Builder> getActivityData(Player player, Activity activity, GlobalActivityData globalActivityData) throws MwException {
        getAndInitTurnplateAward(player,activity);
        GamePb4.GetActivityDataInfoRs.Builder builder = GamePb4.GetActivityDataInfoRs.newBuilder();
        builder.setAutumnTurnplateInfo(buildAutumnTurnplateInfo(activity));
        return builder;
    }

    public GamePb4.AutumnTurnplateRefreshRs refresh(long roleId,int actType) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        ActivityBase activityBase = super.checkAndGetActivityBase(player, actType);
        Activity activity = super.checkAndGetActivity(player, actType);
        if (!super.isOpenStage(activityBase)) {
            throw new MwException(GameError.ACTIVITY_NO_OPEN.getCode(), GameError.err(roleId, "丰收转盘刷新 活动未开放", actType,activity.getActivityId(), activityBase.getStep0()));
        }

        List<List<Integer>> consumeList = new ArrayList<>();
        consumeList.add(ListUtils.createItem(AwardType.MONEY, AwardType.Money.GOLD, StaticTurnplateMgr.get抽奖价格(activity.getActivityId()).get(2)));
        rewardDataManager.checkAndSubPlayerRes(player,consumeList,AwardFrom.AUTUMN_TURNPLATE_REFRESH);

        activity.getSaveMap().clear();

        this.getAndInitTurnplateAward(player,activity);

        GamePb4.AutumnTurnplateRefreshRs.Builder resp = GamePb4.AutumnTurnplateRefreshRs.newBuilder();
        resp.setActType(actType);
        resp.setAutumnTurnplateInfo(buildAutumnTurnplateInfo(activity));
        return resp.build();
    }

    public GamePb4.AutumnTurnplateGetProgressAwardRs getProgressAward(long roleId,int actType,int confId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        ActivityBase activityBase = super.checkAndGetActivityBase(player, actType);
        Activity activity = super.checkAndGetActivity(player, actType);
        if (!super.isOpenStage(activityBase)) {
            throw new MwException(GameError.ACTIVITY_NO_OPEN.getCode(), GameError.err(roleId, "丰收转盘领取进度奖励 活动未开放", actType,activity.getActivityId(), activityBase.getStep0()));
        }
        int progress = activity.getPropMap().getOrDefault(-2,0);
        StaticAutumnTurnplate staticAutumnTurnplate = StaticTurnplateMgr.getStaticAutumnTurnplate(confId);
        if(Objects.isNull(staticAutumnTurnplate) || staticAutumnTurnplate.getActivityId() != activity.getActivityId()){
            throw new MwException(GameError.NO_CONFIG.getCode(), GameError.err(roleId, "丰收转盘领取进度奖励 配置不存在", actType, activity.getActivityId(),confId));
        }
        if(progress < staticAutumnTurnplate.getTimes()){
            throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "丰收转盘领取进度奖励 条件不足", actType, activity.getActivityId(),progress,confId));
        }
        int isGot = activity.getPropMap().getOrDefault(confId,0);
        if(isGot > 0){
            throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "丰收转盘领取进度奖励 已被领取", actType, activity.getActivityId(),confId));
        }

        List<CommonPb.Award> awardList = rewardDataManager.sendReward(player,staticAutumnTurnplate.getAward(),AwardFrom.AUTUMN_TURNPLATE_GET_PROGRESS_AWARD);

        activity.getPropMap().put(confId,1);

        GamePb4.AutumnTurnplateGetProgressAwardRs.Builder resp = GamePb4.AutumnTurnplateGetProgressAwardRs.newBuilder();
        resp.setActType(actType);
        resp.setAutumnTurnplateInfo(buildAutumnTurnplateInfo(activity));
        resp.addAllGetAward(awardList);
        return resp.build();
    }

    public GamePb4.AutumnTurnplatePlayRs play(long roleId, int actType, int playCount) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (playCount != 1 && playCount != 5) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "丰收转盘 参数错误", playCount));
        }
        ActivityBase activityBase = super.checkAndGetActivityBase(player, actType);
        Activity activity = super.checkAndGetActivity(player, actType);
        if (!super.isOpenStage(activityBase)) {
            throw new MwException(GameError.ACTIVITY_NO_OPEN.getCode(), GameError.err(roleId, "丰收转盘 活动未开放", actType, activityBase.getStep0()));
        }
        //剩余未命中的格子
        List<StaticAutumnTurnplate> gridIdxList = this.getNonHitGrid(player,activity);
        if(ListUtils.isBlank(gridIdxList)){
            throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "丰收转盘 当前奖励已全部获得", actType, ListUtils.toString(gridIdxList)));
        }
        //实际能转的次数
        if (gridIdxList.size() < playCount) {
            playCount = gridIdxList.size();
        }
        int realCount = playCount;
        if (playCount == 5) {
            realCount = StaticTurnplateMgr.get抽奖价格(activity.getActivityId()).get(1);
        }
        int freeTimes = getFreeTimes(activity);
        int leftFreeTimes = 1 - freeTimes;
        int costCount = realCount - leftFreeTimes;
        long propCount = rewardDataManager.getRoleResByType(player, StaticTurnplateMgr.get抽奖道具().get(0).get(0), StaticTurnplateMgr.get抽奖道具().get(0).get(1));
        long gold = rewardDataManager.getRoleResByType(player, AwardType.MONEY, AwardType.Money.GOLD);
        List<List<Integer>> consumeList = new ArrayList<>();
        int consumeFreeTimes;
        if(costCount > 0){//有消耗
            if (propCount >= costCount) {
                consumeList.add(ListUtils.createItem(StaticTurnplateMgr.get抽奖道具().get(0).get(0), StaticTurnplateMgr.get抽奖道具().get(0).get(1), costCount));
            } else {
                long diffCount = costCount - propCount;
                long needGold = diffCount * StaticTurnplateMgr.get抽奖价格(activity.getActivityId()).get(0);
                if (gold < needGold) {
                    throw new MwException(GameError.GOLD_NOT_ENOUGH.getCode(), GameError.err(roleId, "丰收转盘钻石不够", playCount, propCount, needGold, gold));
                }
                if (propCount > 0) {
                    consumeList.add(ListUtils.createItem(StaticTurnplateMgr.get抽奖道具().get(0).get(0), StaticTurnplateMgr.get抽奖道具().get(0).get(1), (int) propCount));
                }
                if (needGold > 0) {
                    consumeList.add(ListUtils.createItem(AwardType.MONEY, AwardType.Money.GOLD, (int) needGold));
                }
            }
            consumeFreeTimes = leftFreeTimes;
        }else {//免费
            consumeFreeTimes = realCount;
        }

        GamePb4.AutumnTurnplatePlayRs.Builder resp = GamePb4.AutumnTurnplatePlayRs.newBuilder();
        resp.setActType(actType);

        List<CommonPb.Award> awardList = new ArrayList<>();
        for (int i = 0; i < playCount; i++) {
            List<StaticAutumnTurnplate> nonHitGrids = this.getNonHitGrid(player,activity);
            StaticAutumnTurnplate hitGrid = RandomUtil.randomByWeight(nonHitGrids,StaticAutumnTurnplate::getWight);
            Integer awardIdx = activity.getSaveMap().get(hitGrid.getId());
            if(Objects.isNull(awardIdx)){
                throw new MwException(GameError.SERVER_EXCEPTION.getCode(), String.format("丰收转盘命中的格子%s没有奖励索引%s",hitGrid.getId(),JSON.toJSONString(activity.getSaveMap())));
            }
            CommonPb.Award award = PbHelper.createAward(hitGrid.getAward().get(awardIdx));
            awardList.add(award);
            activity.getSaveMap().put(-hitGrid.getId(),1);

            resp.addPlayOne(CommonPb.SummerTurntablePlayOne.newBuilder().setHitSort(hitGrid.getGridIdx()).addAward(award).build());
        }
        if(ListUtils.isNotBlank(awardList)){
            rewardDataManager.sendRewardByAwardList(player,awardList,AwardFrom.AUTUMN_TURNPLATE_PLAY);
        }
        if(ListUtils.isNotBlank(consumeList)){
            rewardDataManager.subPlayerResHasChecked(player, consumeList, true, AwardFrom.AUTUMN_TURNPLATE_PLAY);
    }
        if(consumeFreeTimes > 0){
            activity.getPropMap().put(-1,freeTimes + consumeFreeTimes);
        }
        activity.getPropMap().merge(-2,playCount,Integer::sum);

        //检查刷新
        gridIdxList = this.getNonHitGrid(player,activity);
        if(ListUtils.isBlank(gridIdxList)){
            activity.getSaveMap().clear();
            this.getAndInitTurnplateAward(player,activity);
        }

        resp.setAutumnTurnplateInfo(this.buildAutumnTurnplateInfo(activity));

        return resp.build();
    }

    private CommonPb.AutumnTurnplateInfo buildAutumnTurnplateInfo(Activity activity){
        CommonPb.AutumnTurnplateInfo.Builder builder = CommonPb.AutumnTurnplateInfo.newBuilder();
        activity.getSaveMap().entrySet().forEach(entry -> {
            if(entry.getKey() > 0){
                boolean isGot = activity.getSaveMap().getOrDefault(-entry.getKey(),0) == 1 ? true : false;
                builder.addTurnplateAward(CommonPb.TurnplateAward.newBuilder().setIdx(entry.getKey()).setAwardIdx(entry.getValue()).setIsGot(isGot).build());
            }
        });
        builder.setProgress(activity.getPropMap().getOrDefault(-2,0));
        activity.getPropMap().entrySet().forEach(entry -> {
            if(entry.getKey() > 0){
                builder.addProgressGot(PbHelper.createTwoIntPb(entry.getKey(),entry.getValue()));
            }
        });
        builder.setNextRefresh(activity.getPropMap().getOrDefault(-3,0));
        builder.setFreeTimes(1-getFreeTimes(activity));
        return builder.build();
    }

    /**
     * 获取当前已使用的免费次数
     * @param activity
     * @return
     */
    private int getFreeTimes(Activity activity){
        int reset = activity.getPropMap().getOrDefault(-4,0);
        if(reset != TimeHelper.getCurrentDay()){
            activity.getPropMap().put(-4,TimeHelper.getCurrentDay());
            activity.getPropMap().remove(-1);
        }
        return activity.getPropMap().getOrDefault(-1,0);
    }

    /**
     * 返回未被命中的格子配置
     * @param activity
     * @return
     * @throws MwException
     */
    private List<StaticAutumnTurnplate> getNonHitGrid(Player player,Activity activity) throws MwException {
        Set<Integer> gridIdxSet = getAndInitTurnplateAward(player,activity);
        List<StaticAutumnTurnplate> gridIdxList = new ArrayList<>();
        gridIdxSet.forEach(idx -> {
            int got = activity.getSaveMap().getOrDefault(-idx, 0);
            if (got != 1) {
                gridIdxList.add(StaticTurnplateMgr.getStaticAutumnTurnplate(idx));
            }
        });
        return gridIdxList;
    }

    /**
     * 初始化并返回格子索引集合
     * @param activity
     * @return
     * @throws MwException
     */
    private Set<Integer> getAndInitTurnplateAward(Player player,Activity activity) throws MwException {
        if (activity.getSaveMap().isEmpty()) {
            List<StaticAutumnTurnplate> staticAutumnTurnplateList = StaticTurnplateMgr.getStaticAutumnTurnplateList(activity.getActivityId(), 1);
            if (ListUtils.isBlank(staticAutumnTurnplateList)) {
                throw new MwException(GameError.SERVER_EXCEPTION.getCode(), "初始化丰收转盘数据异常,roleId=" + player.roleId);
            }
            staticAutumnTurnplateList.forEach(o -> {
                int idx = RandomUtil.randomAwardIdxByWeight(o.getAward());
                activity.getSaveMap().put(o.getId(), idx);
            });
            activity.getPropMap().put(-3, TimeHelper.getCurrentSecond() + 12 * 60 * 60);

            LogUtil.common(String.format("%s丰收转盘刷新转盘奖励, saveMap=%s, propMap=%s",player.roleId,JSON.toJSONString(activity.getSaveMap()),JSON.toJSONString(activity.getPropMap())));
        }
        return activity.getSaveMap().keySet().stream().filter(o -> o > 0).collect(Collectors.toSet());
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
            //处理未领取的奖励
            Activity activity = player.activitys.get(activityType);
            if(Objects.nonNull(activity)){
                List<CommonPb.Award> nonGotAwards = new ArrayList<>();
                Optional.ofNullable(StaticTurnplateMgr.getStaticAutumnTurnplateList(activityId,2)).ifPresent(confs -> {
                    confs.forEach(conf -> {
                        if(activity.getPropMap().getOrDefault(conf.getId(),0) == 0
                                && activity.getPropMap().getOrDefault(-2,0) >= conf.getTimes()){
                            nonGotAwards.addAll(PbHelper.createAwardsPb(conf.getAward()));
                            activity.getPropMap().put(conf.getId(),1);
                        }
                    });
                });
                if(ListUtils.isNotBlank(nonGotAwards)){
                    mailDataManager.sendAttachMail(player,nonGotAwards, MailConstant.MOLD_ACT_UNREWARDED_REWARD,AwardFrom.AUTUMN_TURNPLATE_GET_PROGRESS_AWARD, TimeHelper.getCurrentSecond(),activityType,activityId,activityType,activityId);
                }
            }
            //回收活动道具
            super.autoConvertMail(player,activityType,activityId);
        });
    }

    @Override
    protected void handleOnDisplayTime(int activityType, int activityId, int keyId) {

    }

    @Override
    protected void handleOnDay(Player player) {

    }

    @Override
    protected void levelUp(Player player, int level) {

    }

    @Override
    public void checkAndRefresh(Player player) {
        for (int actType : actTypes) {
            ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(actType);
            if(Objects.isNull(activityBase)) continue;
            Activity activity = super.getActivity(player,actType);
            if(Objects.isNull(activity)) continue;
            if(!super.isOpenStage(activityBase)) continue;
            int refreshStamp = activity.getPropMap().getOrDefault(-3,0);
            if(refreshStamp != 0 && TimeHelper.getCurrentSecond() > refreshStamp){
                //刷新
                activity.getSaveMap().clear();
                try {
                    this.getAndInitTurnplateAward(player,activity);
                    this.syncAutumnTurnplateRefresh(player,activity);
                } catch (Exception e) {
                    LogUtil.error(String.format("丰收转盘自动刷新转盘奖励异常,roleId=%s,活动类型=%s",player.roleId,actType),e);
                }
            }
        }
    }

    private void syncAutumnTurnplateRefresh(Player player,Activity activity){
        GamePb4.SyncAutumnTurnplateRefreshRs.Builder builder = GamePb4.SyncAutumnTurnplateRefreshRs.newBuilder();
        builder.setActType(activity.getActivityType());
        builder.setAutumnTurnplateInfo(buildAutumnTurnplateInfo(activity));
        BasePb.Base msg = PbHelper.createSynBase(GamePb4.SyncAutumnTurnplateRefreshRs.EXT_FIELD_NUMBER, GamePb4.SyncAutumnTurnplateRefreshRs.ext,builder.build()).build();
        playerService.syncMsgToPlayer(msg,player);
    }

    @GmCmd("autumnTurnplate")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {
        if(params[0].equalsIgnoreCase("clear")){
            Activity activity = player.activitys.get(Integer.parseInt(params[1]));
            if(Objects.nonNull(activity)){
                activity.getPropMap().clear();
                activity.getSaveMap().clear();
            }
        }
        if(params[0].equalsIgnoreCase("handleOnEndTime")){
            Activity activity = player.activitys.get(Integer.parseInt(params[1]));
            ActivityBase activityBase = super.checkAndGetActivityBase(player,activity.getActivityType());
            if(Objects.nonNull(activity)){
                this.handleOnEndTime(activity.getActivityType(),activity.getActivityId(),activityBase.getPlan().getKeyId());
            }
        }
    }
}
