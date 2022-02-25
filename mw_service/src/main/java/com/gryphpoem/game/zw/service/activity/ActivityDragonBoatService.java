package com.gryphpoem.game.zw.service.activity;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.s.StaticActExchange;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 端午活动
 * 1. 端午兑换
 * 2. displayTime回收背包的活动道具
 * @author xwind
 * @date 2021/5/22
 */
@Service
public class ActivityDragonBoatService extends AbsActivityService {

    public GamePb4.DragonBoatExchangeRs doExchange(long roleId,int configId,int activityType) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        StaticActExchange staticActExchange = StaticActivityDataMgr.getActExchangeListByKeyId(configId);
        if(Objects.isNull(staticActExchange)){
            throw new MwException(GameError.ACTIVITY_DRAGON_BOAT_NO_CONFIG.getCode(),GameError.ACTIVITY_DRAGON_BOAT_NO_CONFIG.errMsg(roleId,configId));
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
        if(Objects.isNull(activityBase)){
            throw new MwException(GameError.ACTIVITY_NO_EXIST.getCode(),GameError.ACTIVITY_DRAGON_BOAT_NO_CONFIG.errMsg(roleId,configId,"activityBase=null"));
        }
        if(activityBase.getStep() != ActivityConst.OPEN_STEP){
            throw new MwException(GameError.ACTIVITY_NO_OPEN.getCode(),GameError.ACTIVITY_NO_OPEN.errMsg(roleId,configId,activityBase.getStep()));
        }
        Activity activity = getActivity(player,activityType);
        if(Objects.isNull(activity)){
            throw new MwException(GameError.ACTIVITY_NO_EXIST.getCode(),GameError.ACTIVITY_DRAGON_BOAT_NO_CONFIG.errMsg(roleId,configId,"Activity=null"));
        }
        int times = activity.getSaveMap().getOrDefault(configId,0);
        if(times >= staticActExchange.getNumberLimit()){
            throw new MwException(GameError.ACTIVITY_DRAGON_BOAT_LIMIT_TIMES.getCode(),GameError.ACTIVITY_DRAGON_BOAT_LIMIT_TIMES.errMsg(roleId,configId));
        }
//        if( activityType != getActivityType()){
//            throw new MwException(GameError.INVALID_PARAM.getCode(),GameError.INVALID_PARAM.errMsg(roleId,configId,activityType));
//        }

        rewardDataManager.checkPlayerResIsEnough(player,staticActExchange.getExpendProp(),"端午兑换");
        rewardDataManager.subPlayerResHasChecked(player,staticActExchange.getExpendProp(),true, AwardFrom.ACTIVITY_DRAGONBOAT_EXCHANGE);
        List<CommonPb.Award> getAwardds = PbHelper.createAwardsPb(staticActExchange.getAwardList());
        rewardDataManager.sendRewardByAwardList(player,getAwardds,AwardFrom.ACTIVITY_DRAGONBOAT_EXCHANGE);

        activity.getSaveMap().merge(configId,1,Integer::sum);

        GamePb4.DragonBoatExchangeRs.Builder resp = GamePb4.DragonBoatExchangeRs.newBuilder();
        resp.setDragonBoatExchangeInfo(buildDragonBoatExchangeInfo(activity));
        resp.setActivityType(activityType);
        resp.addAllGetAwards(getAwardds);
        return resp.build();
    }

    @Override
    protected GeneratedMessage.Builder<GamePb4.GetActivityDataInfoRs.Builder> getActivityData(Player player, Activity activity, GlobalActivityData globalActivityData) {
        GamePb4.GetActivityDataInfoRs.Builder builder = GamePb4.GetActivityDataInfoRs.newBuilder();
        builder.setDragonBoatExchangeInfo(buildDragonBoatExchangeInfo(activity));
        return builder;
    }

    private CommonPb.DragonBoatExchangeInfo buildDragonBoatExchangeInfo(Activity activity){
        CommonPb.DragonBoatExchangeInfo.Builder builder = CommonPb.DragonBoatExchangeInfo.newBuilder();
        activity.getSaveMap().forEach((k,v) -> builder.addTimes(PbHelper.createTwoIntPb(k,v)));
        return builder.build();
    }

    @Override
    protected int[] getActivityType() {
        return actTypes;
    }

    @Override
    protected void handleOnBeginTime(int activityType, int activityId, int keyId) {

    }

    private int[] actTypes = {ActivityConst.ACT_DRAGON_BOAT_EXCHANGE};

    @Override
    protected void handleOnEndTime(int activityType, int activityId,int keyId) {

    }

    @Override
    protected void handleOnDisplayTime(int activityType, int activityId, int keyId) {
        //回收活动道具
        playerDataManager.getPlayers().values().forEach(player -> super.autoConvertMail(player,activityType,activityId));
//        List<StaticActVoucher> staticActVoucherList = StaticActivityDataMgr.getActVoucherListByActId(activityId);
//        Optional.ofNullable(staticActVoucherList).ifPresent(tmps -> {
//            try {
//                List<List<Integer>> subItemList = new ArrayList<>();
//                List<List<Integer>> getItemList = new ArrayList<>();
//                tmps.forEach(tmp -> {
//                    int hasNum = Math.toIntExact(rewardDataManager.getRoleResByType(player, tmp.getConsume().get(0), tmp.getConsume().get(1)));
//                    if (hasNum > 0) {
//                        subItemList.addAll(ListUtils.createItems(tmp.getConsume().get(0), tmp.getConsume().get(1), hasNum));
//                        getItemList.addAll(ListUtils.createItems(tmp.getAwardList().get(0), tmp.getAwardList().get(1), hasNum * tmp.getAwardList().get(2)));
//                    }
//                });
//                if (ListUtils.isNotBlank(subItemList)) {
//                    rewardDataManager.checkAndSubPlayerRes(player, subItemList, AwardFrom.ACTIVITY_DRAGONBOAT_RECOVERY);
//                    List<CommonPb.Award> getAwards = PbHelper.createAwardsPb(getItemList);
//                    mailDataManager.sendAttachMail(player, getAwards, MailConstant.MOLD_ACT_EXCHANGE_REWARD, AwardFrom.ACTIVITY_DRAGONBOAT_RECOVERY
//                            , TimeHelper.getCurrentSecond(), activityType, activityId, activityType, activityId);
//                    LogUtil.activity("端午兑换活动结束自动回收,roleId=" + player.roleId + ",sub=" + JSON.toJSONString(subItemList) + ", get=" + JSON.toJSONString(getItemList));
//                }
//            } catch (MwException e) {
//                LogUtil.error("端午兑换活动回收报错,roleId=" + player.roleId, e);
//            }
//        });
    }

    @Override
    protected void handleOnDay(Player player) {

    }
}
