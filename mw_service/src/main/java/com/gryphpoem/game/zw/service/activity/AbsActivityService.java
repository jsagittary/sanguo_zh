package com.gryphpoem.game.zw.service.activity;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.manager.ActivityDataManager;
import com.gryphpoem.game.zw.manager.MailDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.pb.ActivityPb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.MailConstant;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.s.StaticActVoucher;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.resource.util.Turple;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * 活动抽象类，用于定义常规活动的通用方法
 * <p>1、若活动需要再endTime或displayTime处理逻辑，则实现相应的抽象方法即可</p>
 * <p>2、活动排行榜通用处理</p>
 *
 * @author xwind
 * @date 2021/5/24
 */
abstract class AbsActivityService {

    @Autowired
    protected PlayerDataManager playerDataManager;
    @Autowired
    protected ActivityDataManager activityDataManager;
    @Autowired
    protected RewardDataManager rewardDataManager;
    @Autowired
    protected MailDataManager mailDataManager;

    /**
     * 获取活动数据
     *
     * @param player
     * @param activity
     * @param globalActivityData
     * @return
     */
    protected abstract GeneratedMessage.Builder<GamePb4.GetActivityDataInfoRs.Builder> getActivityData(Player player, Activity activity, GlobalActivityData globalActivityData) throws Exception;

    /**
     * 获取活动数据
     *
     * @param player
     * @param activityType
     * @return
     */
    protected ActivityPb.ActivityData getActivityData(Player player, int activityType) {
        return null;
    }

    /**
     * 获取活动类型，在子类中设置
     *
     * @return
     */
    protected abstract int[] getActivityType();

    /**
     * 活动endTime时刻的处理
     *
     * @param activityType
     * @param activityId
     * @param keyId
     */
    protected abstract void handleOnBeginTime(int activityType, int activityId, int keyId);

    /**
     * 活动endTime时刻的处理
     *
     * @param activityType
     * @param activityId
     * @param keyId
     */
    protected abstract void handleOnEndTime(int activityType, int activityId, int keyId);

    /**
     * 活动displayTime时刻的处理
     *
     * @param activityType
     * @param activityId
     * @param keyId
     */
    protected abstract void handleOnDisplayTime(int activityType, int activityId, int keyId);

    /**
     * 每天跨天的处理
     *
     * @param player
     */
    protected abstract void handleOnDay(Player player);

    /**
     * 重新加载了配置文件
     */
    public void handleOnConfigReload() {
    }

    /**
     * 是否开放中
     *
     * @param activityType
     * @return
     */
    protected boolean isOpenStage(int activityType) {
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
        return isOpenStage(activityBase);
    }

    /**
     * 是否开放中
     *
     * @param activityBase
     * @return
     */
    protected boolean isOpenStage(ActivityBase activityBase) {
        if (Objects.isNull(activityBase)) {
            return false;
        }
        if (activityBase.getStep0() != ActivityConst.OPEN_STEP) {
            return false;
        }
        return true;
    }

    /**
     * 更新总排行榜
     *  取的当前的排行值
     * @param value              玩家当前值
     * @param activity           玩家的活动数据 statusCnt:key=0排行值 key=1入榜时间戳
     * @param globalActivityData 全局的活动数据
     */
    protected void addGeneralRank(Player player, long value, int time, Activity activity, GlobalActivityData globalActivityData) {
        if (canAddRank(player, value, activity, globalActivityData, 1)) {
            activity.getStatusCnt().put(0, value);
            activity.getStatusCnt().put(1, Long.valueOf(time));
            globalActivityData.addPlayerRank(player, value, activity.getActivityType(), time);
        }
    }

    /**
     * 更新总排行榜
     * @param player
     * @param addVal 增加值
     * @param time 时间戳
     * @param activity
     * @param globalActivityData
     */
    protected void addGeneralRank0(Player player,long addVal,int time,Activity activity,GlobalActivityData globalActivityData){
        if(canAddRank(player,addVal,activity,globalActivityData,1)){
            if(addVal > 0){
                long val = activity.getStatusCnt().merge(0,addVal,Long::sum);
                activity.getStatusCnt().put(1,Long.valueOf(time));
                globalActivityData.addPlayerRank(player,val,activity.getActivityType(),time);
            }
        }
    }

    /**
     * 更新每日排行榜,活动类型值需小于8位数，防止和每日key冲突
     *
     * @param player
     * @param value              排行值
     * @param time               排行时间戳
     * @param activity           玩家活动数据
     * @param globalActivityData 全局活动数据
     * @param rankKey            排行key
     */
    protected void addDayRank(Player player, int value, int time, Activity activity, GlobalActivityData globalActivityData, int rankKey) {
        if (canAddRank(player, value, activity, globalActivityData, 2)) {
            activity.getDayScore().computeIfAbsent(rankKey, v -> new Turple<>(value, time));
            globalActivityData.addPlayerRank(player, (long) value, rankKey, time);
        }
    }

    /**
     * 更新排行榜时检查是否能添加到排行榜
     *
     * @param player
     * @param value              排行值
     * @param activity           玩家的活动数据
     * @param globalActivityData 全局活动数据
     * @param rankType           1 or 2
     * @return
     */
    private boolean canAddRank(Player player, long value, Activity activity, GlobalActivityData globalActivityData, int rankType) {
        if (Objects.isNull(player) || value == 0 || Objects.isNull(activity) || Objects.isNull(globalActivityData)) {
            return false;
        }
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activity.getActivityType());
        if (Objects.isNull(activityBase)) {
            return false;
        }
        int actState = activityBase.getStep0();
        if (actState != ActivityConst.OPEN_STEP) {
            return false;
        }
        if (!checkOnRankCondition(player, rankType)) {
            return false;
        }
        return true;
    }

    /**
     * 检查上榜条件，若有条件限制则在子类中重写
     *
     * @param player
     * @param rankType 1 or 2 ，1：总榜 2：日榜
     * @return
     */
    protected boolean checkOnRankCondition(Player player, int rankType) {
        return true;
    }

    /**
     * 启服时加载排行榜数据
     */
    protected void loadRankOnStartup() {
        for (int type : getActivityType()) {
            ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(type);
            if (Objects.isNull(activityBase) || activityBase.getStep0() == ActivityConst.OPEN_CLOSE) {
                return;
            }
            GlobalActivityData globalActivityData = activityDataManager.getGlobalActivity(type);
            if (Objects.isNull(globalActivityData)) {
                return;
            }
            playerDataManager.getPlayers().values().forEach(player -> {
                Activity activity = getActivity(player, type);
                if (Objects.nonNull(activity)) {
                    if (hasGeneralRank()) {
                        Long value = activity.getStatusCnt().get(0);
                        Long time = activity.getStatusCnt().get(1);
                        if (Objects.nonNull(value) && Objects.nonNull(time)) {
                            this.addGeneralRank(player, value, time.intValue(), activity, globalActivityData);
                        }
                    }
                    if (hasDayRank()) {
                        getRankKeys().forEach(key -> {
                            Turple<Integer, Integer> turple = activity.getDayScore().get(key);
                            if (Objects.nonNull(turple)) {
                                this.addDayRank(player, turple.getA(), turple.getB(), activity, globalActivityData, key);
                            }
                        });
                    }
                }
            });
        }
    }

    /**
     * 用于启服加载排行数据时控制是否加载全局排行
     *
     * @return
     */
    protected boolean hasGeneralRank() {
        return false;
    }

    /**
     * 用于启服加载排行数据时控制是否加载每日排行
     *
     * @return
     */
    protected boolean hasDayRank() {
        return false;
    }

    /**
     * 获取每日排行的key，此处只获取当天的每日排行key，不同需求在子类中重写
     *
     * @return
     */
    protected List<Integer> getRankKeys() {
        List<Integer> list = new ArrayList<>(1);
        list.add(TimeHelper.getCurrentDay());
        return list;
    }

    protected Activity getActivity(Player player, int activityType) {
        Activity activity = activityDataManager.getActivityInfo(player, activityType);
        return activity;
    }

    protected ActivityBase checkAndGetActivityBase(Player player, int activityType) throws MwException {
        ActivityBase activityBase = StaticActivityDataMgr.getActivityByType(activityType);
        if (Objects.isNull(activityBase)) {
            throw new MwException(GameError.ACTIVITY_NO_EXIST.getCode(), GameError.err(player.roleId, "activityBase=null", activityType));
        }
        return activityBase;
    }

    protected Activity checkAndGetActivity(Player player, int activityType) throws MwException {
        Activity activity = getActivity(player, activityType);
        if (Objects.isNull(activity)) {
            throw new MwException(GameError.ACTIVITY_NO_EXIST.getCode(), GameError.err(player.roleId, "Activity=null", activityType));
        }
        return activity;
    }

    protected GlobalActivityData checkAndGetGlobalActivity(Player player, int activityType) throws MwException {
        GlobalActivityData globalActivityData = activityDataManager.getGlobalActivity(activityType);
        if (Objects.isNull(globalActivityData)) {
            throw new MwException(GameError.ACTIVITY_NO_EXIST.getCode(), GameError.err(player.roleId, "GlobalActivityData=null", activityType));
        }
        return globalActivityData;
    }

    protected GlobalActivityData getGlobalActivity(int activityType){
        return activityDataManager.getGlobalActivity(activityType);
    }

    protected void addOtherJob(ActivityBase activityBase,Date now){

    }

    /**
     * 活动结束后自动回收活动道具
     *
     * @param player
     * @param activityType
     * @param activityId
     */
    protected void autoConvertMail(Player player, int activityType, int activityId) {
        List<StaticActVoucher> staticActVoucherList = StaticActivityDataMgr.getActVoucherListByActId(activityId);
        Optional.ofNullable(staticActVoucherList).ifPresent(tmps -> {
            try {
                for (StaticActVoucher tmp : tmps) {
                    if (Objects.nonNull(tmp)) {
                        long hasCount = rewardDataManager.getRoleResByType(player, tmp.getConsume().get(0), tmp.getConsume().get(1));
                        if (hasCount > 0) {
                            rewardDataManager.checkAndSubPlayerRes(player, tmp.getConsume().get(0), tmp.getConsume().get(1), (int) hasCount, AwardFrom.ANNIVERSARY_TURNTABLE_AUTO_RECOVERY, true, activityType, activityId);
                            List<CommonPb.Award> awards = Collections.singletonList(PbHelper.createAwardPb(tmp.getAwardList().get(0), tmp.getAwardList().get(1), (int) (tmp.getAwardList().get(2) * hasCount)));
                            mailDataManager.sendAttachMail(player, awards, MailConstant.MOLD_ACT_EXCHANGE_REWARD, AwardFrom.ANNIVERSARY_TURNTABLE_AUTO_RECOVERY, TimeHelper.getCurrentSecond(), activityType, activityId, activityType, activityId);
                            LogUtil.activity(String.format("活动%d_%d结束自动回收,roleId=%d,回收道具=%s,数量=%d,获得奖励=%s", activityType, activityId, player.roleId, ArrayUtils.toString(tmp.getConsume()), hasCount, ArrayUtils.toString(tmp.getAwardList())));
                        }
                    }
                }
            } catch (Exception e) {
                LogUtil.error("活动结束自动回收错误,roleId=" + player.roleId + ", activityType=" + activityType + ", activityId=" + activityId);
            }
        });
    }

    protected String joinTriggerName(int actType, int actId, int planId) {
        return actType + "_" + actId + "_" + planId;
    }
}
