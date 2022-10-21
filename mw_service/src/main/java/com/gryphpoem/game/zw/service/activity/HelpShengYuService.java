package com.gryphpoem.game.zw.service.activity;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.manager.GlobalDataManager;
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
import com.gryphpoem.game.zw.resource.domain.s.StaticActAward;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;
import com.gryphpoem.game.zw.resource.pojo.world.BerlinWar;
import com.gryphpoem.game.zw.resource.util.ListUtils;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.GmCmd;
import com.gryphpoem.game.zw.service.GmCmdService;
import com.gryphpoem.game.zw.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 助力圣域
 *
 * @author xwind
 * @date 2021/10/22
 */
@Service
public class HelpShengYuService extends AbsActivityService implements GmCmdService {

    private int[] actTypes = {ActivityConst.ACT_HELP_SHENGYU};

    @Autowired
    private PlayerService playerService;
    @Autowired
    private GlobalDataManager globalDataManager;

    @Override
    protected GeneratedMessage.Builder<GamePb4.GetActivityDataInfoRs.Builder> getActivityData(Player player, Activity activity, GlobalActivityData globalActivityData) throws MwException {
        GamePb4.GetActivityDataInfoRs.Builder builder = GamePb4.GetActivityDataInfoRs.newBuilder();
        builder.setHelpShengYuInfo(buildHelpShengYuInfo(activity));
        return builder;
    }

    private int shengyuSchId() {
        BerlinWar berlinWar = globalDataManager.getGameGlobal().getBerlinWar();
        if (Objects.nonNull(berlinWar)) {
            return berlinWar.getScheduleId();
        }
        return 0;
    }

    private CommonPb.HelpShengYuInfo buildHelpShengYuInfo(Activity activity) {
        CommonPb.HelpShengYuInfo.Builder builder = CommonPb.HelpShengYuInfo.newBuilder();
        builder.setVal(activity.getSaveMap().getOrDefault(0, 0));
        int currSchId = shengyuSchId();
        List<StaticActAward> staticActAwardList = StaticActivityDataMgr.getActAwardById(activity.getActivityId())
                .stream().filter(o -> o.getParam().get(0) <= currSchId && o.getParam().get(1) >= currSchId).collect(Collectors.toList());
        Optional.ofNullable(staticActAwardList).ifPresent(tmps -> tmps.forEach(o -> {
            int state = 0;
            if (activity.getStatusMap().getOrDefault(o.getKeyId(), 0) == 1) {
                state = 2;
            } else {
                if (builder.getVal() >= o.getCond()) {
                    state = 1;
                }
            }
            builder.addStatus(PbHelper.createTwoIntPb(o.getKeyId(), state));
        }));
        return builder.build();
    }

    public void updateProgress(Player player, int val) {
        for (int actType : actTypes) {
            Activity activity = super.getActivity(player, actType);
            if (Objects.isNull(activity)) {
                continue;
            }
            int currSchId = shengyuSchId();
            if (currSchId < 10) {
                continue;
            }
            activity.getSaveMap().merge(0, val, (v1, v2) -> v1 + v2);
            syncHelpShengYuInfo(player, activity);
        }
    }

    private void syncHelpShengYuInfo(Player player, Activity activity) {
        GamePb4.SyncHelpShengYuInfoRs.Builder builder = GamePb4.SyncHelpShengYuInfoRs.newBuilder();
        builder.setActivityType(activity.getActivityType());
        builder.setHelpShengYuInfo(buildHelpShengYuInfo(activity));
        BasePb.Base msg = PbHelper.createSynBase(GamePb4.SyncHelpShengYuInfoRs.EXT_FIELD_NUMBER, GamePb4.SyncHelpShengYuInfoRs.ext, builder.build()).build();
        playerService.syncMsgToPlayer(msg, player);
    }

    public GamePb4.HelpShengYuGetAwardRs getAward(long roleId, int activityType, int keyId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        Activity activity = super.checkAndGetActivity(player, activityType);
        ActivityBase activityBase = super.checkAndGetActivityBase(player, activityType);
        if (!isOpenStage(activityBase)) {
            throw new MwException(GameError.ACTIVITY_NO_OPEN.getCode(), GameError.err(roleId, "助力圣域活动领取奖励，活动不是开放阶段", activityType));
        }
        StaticActAward staticActAward = StaticActivityDataMgr.getActAward(keyId);
        if (Objects.isNull(staticActAward)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), GameError.err(roleId, "助力圣域活动领取奖励，找不到奖励配置", activityType, keyId));
        }
        if (activity.getActivityId() != staticActAward.getActivityId()) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "助力圣域活动领取奖励，活动id不一致", activityType, activity.getActivityId(), keyId));
        }
        int currSchId = shengyuSchId();
        if (currSchId < 10 || currSchId < staticActAward.getParam().get(0) || currSchId > staticActAward.getParam().get(1)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(roleId, "助力圣域活动领取奖励，与当前世界进程id不一致", activityType, activity.getActivityId(), keyId, currSchId));
        }
        int state = activity.getStatusMap().getOrDefault(keyId, 0);
        if (state != 0) {
            throw new MwException(GameError.ACTIVITY_AWARD_GOT.getCode(), GameError.err(roleId, "助力圣域活动领取奖励，奖励已领取", activityType, keyId));
        }
        int val = activity.getSaveMap().getOrDefault(0, 0);
        if (val < staticActAward.getCond()) {
            throw new MwException(GameError.ACTIVITY_AWARD_NOT_GET.getCode(), GameError.err(roleId, "助力圣域活动领取奖励，条件不足", activityType, keyId, val));
        }
        List<CommonPb.Award> awardList = rewardDataManager.sendReward(player, staticActAward.getAwardList(), AwardFrom.HELP_SHENGYU_GET_AWARD);
        activity.getStatusMap().put(keyId, 1);

        GamePb4.HelpShengYuGetAwardRs.Builder resp = GamePb4.HelpShengYuGetAwardRs.newBuilder();
        resp.addAllAward(awardList);
        resp.setActivityType(activityType);
        resp.setHelpShengYuInfo(buildHelpShengYuInfo(activity));
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
            if (Objects.nonNull(activity)) {
                List<List<Integer>> nonGets = new ArrayList<>();
                int val = activity.getSaveMap().getOrDefault(0, 0);
                int currSchId = shengyuSchId();
                List<StaticActAward> staticActAwardList = StaticActivityDataMgr.getActAwardById(activity.getActivityId()).stream().filter(o -> currSchId >= o.getParam().get(0) && currSchId <= o.getParam().get(1)).collect(Collectors.toList());
                Optional.ofNullable(staticActAwardList).ifPresent(tmps -> tmps.forEach(tmp -> {
                    if (val >= tmp.getCond() && activity.getStatusMap().getOrDefault(tmp.getKeyId(), 0) == 0) {
                        nonGets.addAll(tmp.getAwardList());
                    }
                }));
                if (ListUtils.isNotBlank(nonGets)) {
                    List<CommonPb.Award> nonAwards = PbHelper.createAwardsPb(nonGets);
                    mailDataManager.sendAttachMail(player, nonAwards, MailConstant.MOLD_ACT_UNREWARDED_REWARD, AwardFrom.HELP_SHENGYU_NON_GET_ON_END, TimeHelper.getCurrentSecond(), activityType, activityId, activityType, activityId);
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

    @GmCmd("helpshengyu")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {
        if (params[0].equalsIgnoreCase("addScore")) {
            int val = Integer.parseInt(params[1]);
            this.updateProgress(player, val);
        }

        if (params[0].equalsIgnoreCase("getInfo")) {
            Activity activity = activityDataManager.getActivityInfo(player, actTypes[0]);
            this.getActivityData(player, activity, null);
        }

        if (params[0].equalsIgnoreCase("getAward")) {
            getAward(player.getLordId(), actTypes[0], Integer.valueOf(params[1]));
        }
    }
}
