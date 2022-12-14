package com.gryphpoem.game.zw.gameplay.local.world;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.gryphpoem.game.zw.gameplay.local.world.army.BaseArmy;
import com.gryphpoem.game.zw.gameplay.local.world.army.MapMarch;
import com.gryphpoem.game.zw.gameplay.local.world.battle.MapWarData;
import com.gryphpoem.game.zw.gameplay.local.world.map.MineMapEntity;
import com.gryphpoem.game.zw.gameplay.local.world.map.PlayerMapEntity;
import org.quartz.Scheduler;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.QuartzHelper;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonMomentOverService;
import com.gryphpoem.game.zw.gameplay.local.world.army.PlayerArmy;
import com.gryphpoem.game.zw.dataMgr.StaticCrossWorldDataMgr;
import com.gryphpoem.game.zw.manager.MailDataManager;
import com.gryphpoem.game.zw.manager.MsgDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.manager.WorldDataManager;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.CommonPb.MailCollect;
import com.gryphpoem.game.zw.pb.GamePb5.SyncMapCloseRs;
import com.gryphpoem.game.zw.quartz.ScheduleManager;
import com.gryphpoem.game.zw.quartz.jobs.DefultJob;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.MailConstant;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticMine;
import com.gryphpoem.game.zw.resource.domain.s.StaticWorldWarPlan;
import com.gryphpoem.game.zw.resource.domain.s.StaticWorldwarOpen;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.RandomHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

/**
 * @ClassName WorldWarOpen.java
 * @Description ????????????????????????
 * @author QiuKun
 * @date 2019???3???26???
 */

public class WorldWarOpen {

    /** ????????????,?????????null */
    private WorldWarPlanInfo worldWarPlanInfo;
    /** ????????? */
    private static final String WEEK_TIME_END = "WEEK_TIME_END";
    /** ???????????? */
    private static final String SEASON_TIME_END = "SEASON_TIME_END";
    /** ?????????????????????????????? */
    private static final String SEASON_DISPLAY_TIME_END = "SEASON_DISPLAY_TIME_END";

    private final CrossWorldMap crossWorldMap;

    public WorldWarOpen(CrossWorldMap crossWorldMap) {
        this.crossWorldMap = crossWorldMap;

    }

    public CrossWorldMap getCrossWorldMap() {
        return crossWorldMap;
    }

    /**
     * ????????????
     */
    public void onAcrossDayRun() {
        // 1. ???????????????
        checkAndInitWorldWarPlan();
        if (worldWarPlanInfo == null || WorldWarPlanInfo.isFinish(worldWarPlanInfo)) {
            return;
        }
        // 2. ???????????????
        if (worldWarPlanInfo.getStage() == WorldWarPlanInfo.STAGE_SEASON_RUNNING) {
            List<Integer> openWeek = Constant.WORLDWAR_OPEN_WEEK;
            int finishWeek = openWeek.get(openWeek.size() - 1);
            int curDayOfWeek = TimeHelper.getCNDayOfWeek();
            // ??????????????????
            if (finishWeek == curDayOfWeek) {
                // ????????? 23.59.59?????????
                int time = TimeHelper.getSomeDayAfter(0, 23, 59, 59);
                addSchedule(WEEK_TIME_END, this::onWeekEnd, time);
            }
            // ?????????????????????,???????????????????????????
            setMapOpenType();
        }
        // ?????????????????????
        addSchedule(SEASON_TIME_END, this::onSeasonEnd, worldWarPlanInfo.getEndTime());
        // ???????????????????????????????????????
        addSchedule(SEASON_DISPLAY_TIME_END, this::onSeasonDisplayTimeEnd, worldWarPlanInfo.getDisplayTime());
    }

    /**
     * ???????????????????????????
     */
    private void setMapOpenType() {
        if (crossWorldMap.getMapOpenType() == MapOpenType.INNER) { // ??????????????? ????????????
            return;
        }
        Date beginDate = new Date(worldWarPlanInfo.getBeginTime() * 1000L);
        int dayNum = DateHelper.dayiy(beginDate, new Date());// ???????????????

        StaticWorldwarOpen openCfg = StaticCrossWorldDataMgr.getOpenMap().values().stream()
                .filter(o -> dayNum >= o.getOpen()).max(Comparator.comparing(StaticWorldwarOpen::getId)).orElse(null);

        if (openCfg != null) {
            if (openCfg.getId() != crossWorldMap.getMapOpenType().getId()) {
                MapOpenType mapOpenType = MapOpenType.getById(openCfg.getId());
                if (mapOpenType != null) {
                    crossWorldMap.setMapOpenType(mapOpenType);
                }
            }
        }
    }

    private void addSchedule(String name, TimeCallBack r, int time) {
        int now = TimeHelper.getCurrentSecond();
        if (now < time) {
            Scheduler sched = ScheduleManager.getInstance().getSched();
            if (!QuartzHelper.isExistSched(sched, name, DefultJob.DEFULT_GROUP)) {
                Date atDate = TimeHelper.secondToDate(time);
                ScheduleManager.getInstance().addOrModifyDefultJob(DefultJob.createDefult(name), job -> {
                    r.onRun();
                }, atDate);
                LogUtil.debug("????????????????????????????????????  name:", name, ", time:", DateHelper.formatDateMiniTime(atDate));
            } else {
                Date startTime = QuartzHelper.getStartTime(sched, name, DefultJob.DEFULT_GROUP);
                Date atDate = TimeHelper.secondToDate(time);
                if (!startTime.equals(atDate)) {
                    // ???????????????
                    QuartzHelper.removeJob(sched, name, DefultJob.DEFULT_GROUP);
                    // ????????????
                    ScheduleManager.getInstance().addOrModifyDefultJob(DefultJob.createDefult(name), job -> {
                        r.onRun();
                    }, atDate);
                    LogUtil.debug("??????????????????????????????????????????  name:", name, ", time:", DateHelper.formatDateMiniTime(atDate));
                }
            }
        }
    }

    public void checkAndInitWorldWarPlan() {
        if (worldWarPlanInfo == null) {
            // ????????? ????????????
            StaticWorldWarPlan planCfg = StaticCrossWorldDataMgr.getNowPlan();
            if (planCfg != null) {
                worldWarPlanInfo = WorldWarPlanInfo.createByCfg(planCfg);
            }
        } else {
            if (WorldWarPlanInfo.isFinish(worldWarPlanInfo)) {
                // ???????????????,??????????????????????????????
                StaticWorldWarPlan planCfg = StaticCrossWorldDataMgr.getNowPlan();
                if (planCfg != null) {
                    worldWarPlanInfo.refreshByCfg(planCfg);
                }
            } else { // ??????????????????
                StaticWorldWarPlan planCfg = StaticCrossWorldDataMgr.getWorldWarPlan().stream()
                        .filter(plan -> plan.getId() == worldWarPlanInfo.getId()).findFirst().orElse(null);
                if (planCfg != null) {
                    worldWarPlanInfo.refreshByCfg(planCfg);
                }
            }
        }
    }

    @FunctionalInterface
    private static interface TimeCallBack {
        void onRun();
    }

    public WorldWarPlanInfo getWorldWarPlanInfo() {
        return worldWarPlanInfo;
    }

    public void setWorldWarPlanInfo(WorldWarPlanInfo worldWarPlanInfo) {
        this.worldWarPlanInfo = worldWarPlanInfo;
    }

    /**
     * ????????????????????????
     */
    public void onSeasonEnd() {
        LogUtil.debug("??????????????????????????????????????????");
        processClose();
        // ??????????????????
        crossWorldMap.getCityMap().values().forEach(cityEntity -> cityEntity.getCity().clearStateToInit());
        crossWorldMap.setMapOpenType(MapOpenType.OUTER);
        // ??????????????????????????????????????????????????????????????????
        WorldWarSeasonMomentOverService seasonMomentOverService = DataResource.ac
                .getBean(WorldWarSeasonMomentOverService.class);
        seasonMomentOverService.seasonOver();
    }

    /**
     * ?????????????????? ?????????????????????????????????????????????????????????
     */
    private void onSeasonDisplayTimeEnd() {
        // ?????????????????? ?????????????????????????????????????????????????????????
        WorldWarSeasonMomentOverService seasonMomentOverService = DataResource.ac
                .getBean(WorldWarSeasonMomentOverService.class);
        seasonMomentOverService.seasonOverClearIntegral();
    }

    private void processClose() {
        // ????????????
        WorldDataManager worldDataManager = DataResource.ac.getBean(WorldDataManager.class);
        Map<Long, PlayerMapEntity> playerMap = crossWorldMap.getPlayerMap();
        // List<StaticArea> areaList = StaticWorldDataMgr.getAreaMap().values().stream()
        // .filter(sa -> sa.getOpenOrder() == WorldConstant.AREA_ORDER_1).collect(Collectors.toList());
        // int areaSize = areaList.size();
        List<Integer> posList = new ArrayList<>();
        int now = TimeHelper.getCurrentSecond();
        MailDataManager mailDataManager = DataResource.ac.getBean(MailDataManager.class);
        for (PlayerMapEntity playerEntity : playerMap.values()) {
            Player player = playerEntity.getPlayer();
            // StaticArea staticArea = areaList.get(RandomHelper.randomInSize(areaSize));
            // ????????????
            Integer emptyPos = worldDataManager
                    .randomEmptyByAreaBlock(WorldConstant.AREA_TYPE_13, RandomHelper.randomInArea(1, 100), 1).get(0);
            // Integer emptyPos = worldDataManager
            // .randomEmptyByAreaBlock(staticArea.getArea(), RandomHelper.randomInArea(1, 100), 1).get(0);
            player.lord.setPos(emptyPos);
            player.lord.setArea(WorldConstant.AREA_TYPE_13);
            worldDataManager.putPlayer(player);
            // ???????????????
            crossWorldMap.getAllMap().remove(playerEntity.getPos());
            // ????????????????????????
            sendSyncCrossMapChgMsg(player, crossWorldMap.getMapId());
            // ?????????
            mailDataManager.sendNormalMail(player, MailConstant.MOLD_WORLD_WAR_CLOSE, now);
            posList.add(emptyPos);
            LogUtil.debug("??????????????????,??????????????????????????? roleId:", player.lord.getLordId(), ", pos:", player.lord.getPos(),
                    ", area:", player.lord.getArea());
        }
        // ????????????
        EventBus.getDefault()
                .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_AREA_TYPE));
        crossWorldMap.getPlayerMap().clear(); // ??????

        // ????????????
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        MapMarch mapMarchArmy = crossWorldMap.getMapMarchArmy();
        mapMarchArmy.getDelayArmysQueue().clearQueue(); // ???????????????
        for (PlayerArmy playerArmy : mapMarchArmy.getPlayerArmyMap().values()) {
            long roleId = playerArmy.getRoleId();
            Player player = playerDataManager.getPlayer(roleId);
            if (player != null) {
                for (BaseArmy baseArmy : playerArmy.getArmy().values()) {
                    baseArmy.setArmyPlayerHeroState(mapMarchArmy, ArmyConstant.ARMY_STATE_IDLE);
                }
            }
        }
        mapMarchArmy.getPlayerArmyMap().clear();

        // battle?????????
        MapWarData mapWarData = crossWorldMap.getMapWarData();
        mapWarData.getDelayBattleQueue().clearQueue();
        mapWarData.getAllBattles().clear();
        mapWarData.getBattlePosCache().clear();

        // ????????????????????????
        List<MineMapEntity> mineList = crossWorldMap.getAllMap().values().stream()
                .filter(baseEntity -> baseEntity.getType() == WorldEntityType.MINE).map(m -> (MineMapEntity) m)
                .filter(m -> m.getGuard() != null).collect(Collectors.toList());
        RewardDataManager rewardDataManager = DataResource.ac.getBean(RewardDataManager.class);
        for (MineMapEntity mineEntity : mineList) {
            // ???????????????????????????
            crossWorldMap.getAllMap().remove(mineEntity.getPos());
            Player amryPlayer = mineEntity.getGuard().getPlayer(); // ????????????player??????
            MailCollect mailCollect = mineEntity.settleCollect(crossWorldMap);
            if (amryPlayer != null) {
                List<Award> grab = mailCollect.getGrabList();
                rewardDataManager.sendRewardByAwardList(amryPlayer, grab, AwardFrom.COLLECT);
                StaticMine staticMine = mineEntity.getCfgMine();
                mailDataManager.sendCollectMail(amryPlayer, null, MailConstant.MOLD_CROSSMAP_COLLECT_BREAK, mailCollect,
                        now, staticMine.getMineId(), grab.get(0).getCount(), staticMine.getMineId(),
                        grab.get(0).getCount());
            }
        }
    }

    private void sendSyncCrossMapChgMsg(Player player, int mapId) {
        if (player.ctx != null && player.isLogin) {
            SyncMapCloseRs.Builder builder = SyncMapCloseRs.newBuilder();
            builder.setMapId(mapId);
            builder.setArea(player.lord.getArea());
            builder.setPos(player.lord.getPos());
            Base.Builder msg = PbHelper.createSynBase(SyncMapCloseRs.EXT_FIELD_NUMBER, SyncMapCloseRs.ext,
                    builder.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        }
    }

    /**
     * ???????????????
     */
    public void onWeekEnd() {
        LogUtil.debug("????????????????????????????????????");
        processClose();
    }

}
