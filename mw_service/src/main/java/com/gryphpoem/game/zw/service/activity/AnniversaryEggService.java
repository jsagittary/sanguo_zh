package com.gryphpoem.game.zw.service.activity;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticAnniversaryMgr;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.s.StaticAnniversaryEgg;
import com.gryphpoem.game.zw.resource.domain.s.StaticRandomLibrary;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.RandomUtil;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 周年彩蛋
 * Activity.saveMap：存放彩蛋刷新位置
 * GlobalActivityData.goal：刷新彩蛋的全局时间戳
 *
 * @author xwind
 * @date 2021/7/21
 */
@Service
public class AnniversaryEggService extends AbsActivityService {

    private int[] actTypes = {ActivityConst.ACT_ANNIVERSARY_EGG};

    @Autowired
    private PlayerService playerService;

    public GamePb4.AnniversaryEggOpenRs openEgg(long roleId, int actType, int eggId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        ActivityBase activityBase = checkAndGetActivityBase(player, actType);
        Activity activity = checkAndGetActivity(player, actType);
        if (!isOpenStage(activityBase)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), GameError.err(roleId, "彩蛋活动未开启", actType));
        }
        if (Objects.isNull(activity.getSaveMap().get(eggId))) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "彩蛋id不存在", eggId));
        }
        StaticAnniversaryEgg staticAnniversaryEgg = StaticAnniversaryMgr.getEggById(eggId);
        if (Objects.isNull(staticAnniversaryEgg)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), GameError.err(roleId, "彩蛋配置不存在", eggId));
        }
        StaticRandomLibrary staticRandomLibrary = StaticAnniversaryMgr.getRandomLibrary(staticAnniversaryEgg.getAward(), player.lord.getLevel());
        if (Objects.isNull(staticRandomLibrary)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), GameError.err(roleId, "彩蛋随机库配置没有", eggId));
        }
        List<List<Integer>> getProps = new ArrayList<>();
        int num = RandomUtil.randomIntIncludeEnd(1, staticAnniversaryEgg.getNumb());
        for (int i = 0; i < num; i++) {
            List<Integer> prop = RandomUtil.getWeightByList(staticRandomLibrary.getAwardList(), tmps -> tmps.get(3));
            List<Integer> prop_ = new ArrayList<>(prop);
            prop_.remove(prop_.size()-1);
            getProps.add(prop_);
        }
        List<CommonPb.Award> awardList = rewardDataManager.sendReward(player,getProps, AwardFrom.ANNIVERSARY_EGG_OPEN);

        activity.getSaveMap().remove(eggId);

        GamePb4.AnniversaryEggOpenRs.Builder resp = GamePb4.AnniversaryEggOpenRs.newBuilder();
        resp.setActType(actType);
        resp.addAllAwards(awardList);
        return resp.build();
    }

    private CommonPb.AnniversaryEgg buildAnniversaryEgg(Activity activity) {
        CommonPb.AnniversaryEgg.Builder builder = CommonPb.AnniversaryEgg.newBuilder();
        builder.addAllEggId(activity.getSaveMap().keySet());
        return builder.build();
    }

    public List<GlobalActivityData> checkRefreshEgg() {
        List<GlobalActivityData> list = new ArrayList<>();
        for (int actType : actTypes) {
            GlobalActivityData globalActivityData = activityDataManager.getGlobalActivity(actType);
            if (Objects.nonNull(globalActivityData) && globalActivityData.getOpen() == ActivityConst.OPEN_STEP
                    && TimeHelper.getCurrentSecond() - globalActivityData.getGoal() > StaticAnniversaryMgr.getEggRefreshInterval()) {
                list.add(globalActivityData);
                globalActivityData.setGoal(TimeHelper.getCurrentSecond());
            }
        }
        return list;
    }

    public void refreshEgg(List<GlobalActivityData> globalActivityDataList, Player player) {
        Optional.ofNullable(globalActivityDataList).ifPresent(list -> list.forEach(data -> {
            Activity activity = getActivity(player, data.getActivityType());
            if (Objects.nonNull(activity)) {
                activity.getSaveMap().clear();
                List<StaticAnniversaryEgg> refreshList = RandomUtil.randomList(StaticAnniversaryMgr.getEggList(), StaticAnniversaryMgr.getEggRefreshCount());
                refreshList.forEach(tmps -> activity.getSaveMap().put(tmps.getId(), 0));
                syncAnniversaryRefreshEggRs(activity, player);
                LogUtil.activity(String.format("刷新彩蛋位置,roleId=%d,time=%d,egg=%s",player.roleId,data.getGoal(), Arrays.toString(activity.getSaveMap().keySet().toArray())));
            }
        }));
    }

    private void syncAnniversaryRefreshEggRs(Activity activity, Player player) {
        GamePb4.SyncAnniversaryRefreshEggRs.Builder builder = GamePb4.SyncAnniversaryRefreshEggRs.newBuilder();
        builder.setAnniversaryEgg(buildAnniversaryEgg(activity));
        builder.setActType(activity.getActivityType());
        BasePb.Base msg = PbHelper.createSynBase(GamePb4.SyncAnniversaryRefreshEggRs.EXT_FIELD_NUMBER, GamePb4.SyncAnniversaryRefreshEggRs.ext, builder.build()).build();
        playerService.syncMsgToPlayer(msg, player);
    }

    @Override
    protected GeneratedMessage.Builder<GamePb4.GetActivityDataInfoRs.Builder> getActivityData(Player player, Activity activity, GlobalActivityData globalActivityData) {
        GamePb4.GetActivityDataInfoRs.Builder builder = GamePb4.GetActivityDataInfoRs.newBuilder();
        builder.setAnniversaryEgg(buildAnniversaryEgg(activity));
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
        //清除彩蛋礼盒
        playerDataManager.getPlayers().values().forEach(player -> {
            Activity activity = getActivity(player,activityType);
            if(Objects.nonNull(activity)){
                activity.getSaveMap().clear();
                syncAnniversaryRefreshEggRs(activity,player);
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
