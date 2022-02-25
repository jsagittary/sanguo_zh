package com.gryphpoem.game.zw.service.activity;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticAnniversaryMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.s.StaticAnniversaryTurntable;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;
import com.gryphpoem.game.zw.resource.util.ListUtils;
import com.gryphpoem.game.zw.resource.util.RandomUtil;
import com.gryphpoem.game.zw.service.GmCmd;
import com.gryphpoem.game.zw.service.GmCmdService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 周年转盘
 * <p>
 * saveMap：key=0轮数 key=1当前轮抽奖次数<br/>
 * propMap：抽中的记录
 * </p>
 * @author xwind
 * @date 2021/7/21
 */
@Service
public class AnniversaryTurntableService extends AbsActivityService implements GmCmdService {

    private int[] actTypes = {ActivityConst.ACT_ANNIVERSARY_TURNTABLE};

    public GamePb4.AnniversaryTurntablePlayRs playTurntable(long roleId,int activityType,int times) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = checkAndGetActivity(player,activityType);
        ActivityBase activityBase = checkAndGetActivityBase(player,activityType);
        if(!isOpenStage(activityBase)){
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(),GameError.err(roleId,"活动未开启",activityType));
        }
        int round = activity.getSaveMap().getOrDefault(0,1);
        List<StaticAnniversaryTurntable> configList =  StaticAnniversaryMgr.getStaticAnniversaryTurntableList(activityBase.getActivityId(),round);
        if(ListUtils.isBlank(configList)){
            throw new MwException(GameError.NO_CONFIG.getCode(),GameError.err(roleId,"找不到配置",activityType));
        }
        int playTimes = activity.getSaveMap().getOrDefault(1,0);
        List<StaticAnniversaryTurntable> playList = new ArrayList<>();
        configList.forEach(tmp -> {
            if(!isGot(activity,tmp.getId()) && ((tmp.getBetterAward()==1&&playTimes>=tmp.getDownFrequency())||tmp.getBetterAward()!=1)){
                playList.add(tmp);
            }
        });
        if(ListUtils.isBlank(playList)){
            throw new MwException(GameError.ANNIVERSARY_ROUND_GOT_ALL.getCode(),GameError.err(roleId,"当前轮奖励已全部获得"));
        }

        //扣除道具or钻石
        List<List<Integer>> consumeList = new ArrayList<>();
        long propCount = rewardDataManager.getRoleResByType(player, StaticAnniversaryMgr.getPlayTurntableProp().get(0),StaticAnniversaryMgr.getPlayTurntableProp().get(1));
        if(propCount >= 1){
            consumeList.add(StaticAnniversaryMgr.getPlayTurntableProp());
        }else {
            long gold = rewardDataManager.getRoleResByType(player, AwardType.MONEY, AwardType.Money.GOLD);
            if(gold < StaticAnniversaryMgr.getPlayTurntablePrice()){
                throw new MwException(GameError.GOLD_NOT_ENOUGH.getCode(),GameError.err(roleId,"钻石不够",propCount,gold));
            }
            consumeList.add(ListUtils.createItem(AwardType.MONEY, AwardType.Money.GOLD, StaticAnniversaryMgr.getPlayTurntablePrice()));
        }

        StaticAnniversaryTurntable hitConfig = RandomUtil.getWeightByList(playList, StaticAnniversaryTurntable::getWeight);
        if(Objects.isNull(hitConfig)){
            throw new MwException(GameError.RANDOM_BY_WEIGHT_NON.getCode(),GameError.err(roleId,"权重随机异常",activityType));
        }

        rewardDataManager.subPlayerResHasChecked(player,consumeList,true,AwardFrom.ANNIVERSARY_TURNTABLE_PLAY);

        List<CommonPb.Award> awardList = rewardDataManager.sendReward(player,hitConfig.getAward(), AwardFrom.ANNIVERSARY_TURNTABLE_PLAY);

        activity.getSaveMap().put(1,playTimes + 1);
        activity.getPropMap().put(hitConfig.getId(),0);

        //check can next round
        List<StaticAnniversaryTurntable> nextList =  StaticAnniversaryMgr.getStaticAnniversaryTurntableList(activityBase.getActivityId(),round+1);
        if(ListUtils.isNotBlank(nextList)){
            boolean isGotAll = true;
            for (StaticAnniversaryTurntable tmp : configList) {
                if(!isGot(activity,tmp.getId())){
                    isGotAll = false;
                    break;
                }
            }
            if(isGotAll){
                activity.getSaveMap().put(0,round + 1);
                activity.getPropMap().clear();
                activity.getSaveMap().put(1,0);
            }
        }

        GamePb4.AnniversaryTurntablePlayRs.Builder resp = GamePb4.AnniversaryTurntablePlayRs.newBuilder();
        resp.setActType(activityType);
        resp.addAllAwards(awardList);
        resp.setAnniversaryTurntable(buildAnniversaryTurntable(activity));
        resp.setConfigId(hitConfig.getId());
        return resp.build();
    }

    private boolean isGot(Activity activity,int id){
        return activity.getPropMap().containsKey(id);
    }

    private CommonPb.AnniversaryTurntable buildAnniversaryTurntable(Activity activity){
        CommonPb.AnniversaryTurntable.Builder builder = CommonPb.AnniversaryTurntable.newBuilder();
        builder.setRound(activity.getSaveMap().getOrDefault(0,1));
        builder.addAllGotIds(activity.getPropMap().keySet());
        builder.setMaxRound(StaticAnniversaryMgr.getTurntableMaxRoundMap().getOrDefault(activity.getActivityId(),1));
        return builder.build();
    }

    @Override
    protected GeneratedMessage.Builder<GamePb4.GetActivityDataInfoRs.Builder> getActivityData(Player player, Activity activity, GlobalActivityData globalActivityData) {
        GamePb4.GetActivityDataInfoRs.Builder builder = GamePb4.GetActivityDataInfoRs.newBuilder();
        builder.setAnniversaryTurntable(buildAnniversaryTurntable(activity));
        return builder;
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
        playerDataManager.getPlayers().values().forEach(player -> super.autoConvertMail(player,activityType,activityId));
    }

    @Override
    protected void handleOnDisplayTime(int activityType, int activityId, int keyId) {

    }

    @Override
    protected void handleOnDay(Player player) {

    }

    @GmCmd("周年转盘")
    @Override
    public void handleGmCmd(Player player, String... params) {

    }
}
