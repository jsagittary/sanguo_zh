package com.gryphpoem.game.zw.service.activity;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.manager.ActivityDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.ActivityConst;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.pojo.GlobalActivityData;
import com.gryphpoem.game.zw.service.GmCmd;
import com.gryphpoem.game.zw.service.GmCmdService;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 用来替代补充臃肿的 ActivityService 类
 *
 * @Description
 * @Author zhangdh
 * @Date 2021-07-26 20:34
 */
@Service
public class ActivityTemplateService implements GmCmdService {
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private List<AbsActivityService> activityServiceList;
    @Autowired
    private ActivityDiaoChanService activityDiaoChanService;

    /**
     * 获取活动数据
     *
     * @param roleId
     * @param activityType
     * @return
     * @throws MwException
     */
    public GamePb4.GetActivityDataInfoRs getActivityDatainfo(long roleId, int activityType) throws Exception {
        GamePb4.GetActivityDataInfoRs.Builder resp = GamePb4.GetActivityDataInfoRs.newBuilder();
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        AbsActivityService absActivityService = this.getActivityService(activityType);
        if (Objects.isNull(absActivityService)) {
            throw new MwException(GameError.ACTIVITY_NO_EXIST.getCode(), GameError.err(roleId, activityType));
        }
        Activity activity = activityDataManager.getActivityInfo(player, activityType);
        if (Objects.isNull(activity)) {
            throw new MwException(GameError.ACTIVITY_NO_EXIST.getCode(), GameError.err(roleId, activityType, "Activity=null"));
        }
        GlobalActivityData globalActivityData = activityDataManager.getGlobalActivity(activityType);
        if (Objects.isNull(globalActivityData)) {
            throw new MwException(GameError.ACTIVITY_NO_EXIST.getCode(), GameError.err(roleId, activityType, "GlobalActivityData=null"));
        }
        GeneratedMessage.Builder<GamePb4.GetActivityDataInfoRs.Builder> builder = absActivityService.getActivityData(player, activity, globalActivityData);
        if (Objects.nonNull(builder)) {
            resp.mergeFrom(builder.build());
        }

        resp.setActivityType(activityType);
        return resp.build();
    }

    public void execActivityBegin(int activityType, int activityId, int keyId) {
        AbsActivityService absActivityService = getActivityService(activityType);
        if (Objects.nonNull(absActivityService)) {
            absActivityService.handleOnBeginTime(activityType, activityId, keyId);
        }
    }


    public void execActivityEnd(int activityType, int activityId, int planKeyId) {
        AbsActivityService absActivityService = getActivityService(activityType);
        if (Objects.nonNull(absActivityService)) {
            absActivityService.handleOnEndTime(activityType, activityId, planKeyId);
        } else {
            playerDataManager.getPlayers().values().forEach(player -> {
                if (activityType == ActivityConst.ACT_DIAOCHAN || activityType == ActivityConst.ACT_SEASON_HERO) {
                    activityDiaoChanService.handleEnd(player, activityType, activityId, planKeyId);
                }
            });
        }
    }

    /**
     * 活动displayTime执行
     *
     * @param activityType
     * @param activityId
     * @param keyId
     */
    public void execActivityOver(int activityType, int activityId, int keyId) {
        AbsActivityService absActivityService = getActivityService(activityType);
        if (Objects.nonNull(absActivityService)) {
            absActivityService.handleOnDisplayTime(activityType, activityId, keyId);
        } else {
            playerDataManager.getPlayers().values().forEach(player -> {
                if (activityType == ActivityConst.ACT_DIAOCHAN) {
                    activityDiaoChanService.handleOver(player, activityType, activityId, keyId);
                }
            });
        }
    }

    public void execActivityDay(Player player) {
        activityServiceList.forEach(service -> service.handleOnDay(player));
    }

    public void execLoadRankOnStartup() {
        activityServiceList.forEach(AbsActivityService::loadRankOnStartup);
    }

    public void addOtherJob(ActivityBase activityBase, Date now){
        AbsActivityService absActivityService = getActivityService(activityBase.getActivityType());
        absActivityService.addOtherJob(activityBase,now);
    }

    public AbsActivityService getActivityService(int activityType) {
        for (AbsActivityService activityService : activityServiceList) {
            int[] actTypes = activityService.getActivityType();
            if (ArrayUtils.contains(actTypes, activityType)){
                return activityService;
            }
        }
        return null;
//        return activityServiceList.stream().filter(tmp -> ArrayUtils.contains(tmp.getActivityType(), activityType)).findFirst().orElse(null);
    }

    public void handleReloadActivityConfig(){
        for (AbsActivityService activityService : activityServiceList) {
            try{
                activityService.handleOnConfigReload();
            }catch (Exception e){
                LogUtil.error("", e);
            }
        }
    }

    // <editor-fold desc="自己测试用的方法" defaultstate="collapsed">

    @GmCmd("activity")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {
        int activityType = Integer.parseInt(params[0]);
        String cmd = params[1];
//        if(activityType == ActivityConst.ACT_DRAGON_BOAT_EXCHANGE){
        if (cmd.equalsIgnoreCase("getinfo")) {
            LogUtil.c2sMessage(getActivityDatainfo(player.roleId, activityType), player.roleId);
        }
//        }
        if (activityType == ActivityConst.ACT_SUMMER_CASTLE) {
            if (cmd.equalsIgnoreCase("add")) {
                int add = Integer.parseInt(params[2]);
                DataResource.getBean(SummerCastleService.class).updateScore(player, add, AwardFrom.DO_SOME);
            }
            if (cmd.equalsIgnoreCase("clear")) {
                DataResource.getBean(SummerCastleService.class).test_clear(player, activityType);
            }
        }
        if (activityType == ActivityConst.ACT_SUMMER_TURNPLATE) {
            if (cmd.equalsIgnoreCase("play")) {
                int n = Integer.parseInt(params[2]);
                LogUtil.c2sMessage(DataResource.getBean(SummerTurntableService.class).playTurntable(player.roleId, n, activityType), player.roleId);
            }
            if (cmd.equalsIgnoreCase("clear")) {
                DataResource.getBean(SummerTurntableService.class).test_clear(player, activityType);
            }
        }
        if (activityType == ActivityConst.ACT_SUMMER_CHARGE) {
            if (cmd.equalsIgnoreCase("clear")) {
                DataResource.getBean(SummerChargeService.class).test_clear(player, activityType);
            }
        }
        if (activityType == ActivityConst.ACT_ANNIVERSARY_EGG) {
            if (cmd.equalsIgnoreCase("refresh")) {
                AnniversaryEggService anniversaryEggService = DataResource.getBean(AnniversaryEggService.class);
                List<GlobalActivityData> dataList = anniversaryEggService.checkRefreshEgg();
                anniversaryEggService.refreshEgg(dataList, player);
            }
        }
        //喜悅金秋活動
        if (activityType == ActivityConst.ACT_GOLDEN_AUTUMN_FARM) {
            //农场
            if (cmd.equalsIgnoreCase("empireFarm")) {
                GoldenAutumnFarmService goldenAutumnFarmService = DataResource.getBean(GoldenAutumnFarmService.class);
                Objects.requireNonNull(goldenAutumnFarmService).test_handlerEmpireFarm(player, activityType, Integer.valueOf(params[2]));
            }
            if (cmd.equalsIgnoreCase("openTreasureChest")) {
                GoldenAutumnFarmService goldenAutumnFarmService = DataResource.getBean(GoldenAutumnFarmService.class);
                Objects.requireNonNull(goldenAutumnFarmService).test_OpenTreasureChest(player, activityType);
            }
            if (cmd.equalsIgnoreCase("GoldenAutumnInfo")) {
                GoldenAutumnFarmService goldenAutumnFarmService = DataResource.getBean(GoldenAutumnFarmService.class);
                Objects.requireNonNull(goldenAutumnFarmService).test_GoldenAutumnInfo(player, activityType);
            }
            //硕果
            if (cmd.equalsIgnoreCase("goldenAutumnFruitful")) {
                GoldenAutumnFruitfulService goldenAutumnFruitfulService = DataResource.getBean(GoldenAutumnFruitfulService.class);
                Objects.requireNonNull(goldenAutumnFruitfulService).test_handlerFruitful(player, activityType, Integer.valueOf(params[2]), Integer.valueOf(params[3]));
            }
            if (cmd.equalsIgnoreCase("goldenAutumnFruitfulBuild")) {
                GoldenAutumnFruitfulService goldenAutumnFruitfulService = DataResource.getBean(GoldenAutumnFruitfulService.class);
                Objects.requireNonNull(goldenAutumnFruitfulService).test_buildGoldenAutumnInfo(player, activityType);
            }
            //任务
            if (cmd.equalsIgnoreCase("goldenAutumnSunriseSingleTask")) {
                GoldenAutumnSunriseService goldenAutumnSunriseService = DataResource.getBean(GoldenAutumnSunriseService.class);
                Objects.requireNonNull(goldenAutumnSunriseService).test_receiveSingleTaskAward(player, activityType, Integer.valueOf(params[2]));
            }
            if (cmd.equalsIgnoreCase("goldenAutumnSunriseTreasureChest")) {
                GoldenAutumnSunriseService goldenAutumnSunriseService = DataResource.getBean(GoldenAutumnSunriseService.class);
                Objects.requireNonNull(goldenAutumnSunriseService).test_openTreasureChest(player, activityType, Integer.valueOf(params[2]));
            }
            if (cmd.equalsIgnoreCase("goldenAutumnSunriseBuild")) {
                GoldenAutumnSunriseService goldenAutumnSunriseService = DataResource.getBean(GoldenAutumnSunriseService.class);
                Objects.requireNonNull(goldenAutumnSunriseService).test_buildGoldenAutumnInfo(player, activityType);
            }
            //获取金秋活动
            if (cmd.equalsIgnoreCase("goldenAutumn")) {
                this.getActivityDatainfo(player.roleId, ActivityConst.ACT_GOLDEN_AUTUMN_FARM);
            }
        }
    }
    // </editor-fold>

}
