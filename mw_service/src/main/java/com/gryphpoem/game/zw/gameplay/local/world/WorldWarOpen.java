package com.gryphpoem.game.zw.gameplay.local.world;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.QuartzHelper;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.dataMgr.StaticCrossWorldDataMgr;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonMomentOverService;
import com.gryphpoem.game.zw.gameplay.local.world.army.BaseArmy;
import com.gryphpoem.game.zw.gameplay.local.world.army.MapMarch;
import com.gryphpoem.game.zw.gameplay.local.world.army.PlayerArmy;
import com.gryphpoem.game.zw.gameplay.local.world.battle.MapWarData;
import com.gryphpoem.game.zw.gameplay.local.world.map.MineMapEntity;
import com.gryphpoem.game.zw.gameplay.local.world.map.PlayerMapEntity;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.CommonPb.MailCollect;
import com.gryphpoem.game.zw.pb.GamePb5.SyncMapCloseRs;
import com.gryphpoem.game.zw.quartz.ScheduleManager;
import com.gryphpoem.game.zw.quartz.jobs.DefultJob;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticMine;
import com.gryphpoem.game.zw.resource.domain.s.StaticWorldWarPlan;
import com.gryphpoem.game.zw.resource.domain.s.StaticWorldwarOpen;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.quartz.Scheduler;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author QiuKun
 * @ClassName WorldWarOpen.java
 * @Description 世界战争开放规则
 * @date 2019年3月26日
 */

public class WorldWarOpen {

    /**
     * 开放计划,可能为null
     */
    private WorldWarPlanInfo worldWarPlanInfo;
    /**
     * 周结束
     */
    private static final String WEEK_TIME_END = "WEEK_TIME_END";
    /**
     * 赛季结束
     */
    private static final String SEASON_TIME_END = "SEASON_TIME_END";
    /**
     * 赛季活动显示时间结束
     */
    private static final String SEASON_DISPLAY_TIME_END = "SEASON_DISPLAY_TIME_END";

    private final CrossWorldMap crossWorldMap;

    public WorldWarOpen(CrossWorldMap crossWorldMap) {
        this.crossWorldMap = crossWorldMap;

    }

    public CrossWorldMap getCrossWorldMap() {
        return crossWorldMap;
    }

    /**
     * 跨天执行
     */
    public void onAcrossDayRun() {
        // 1. 时间的检查
        checkAndInitWorldWarPlan();
        if (worldWarPlanInfo == null || WorldWarPlanInfo.isFinish(worldWarPlanInfo)) {
            return;
        }
        // 2. 定时器初始
        if (worldWarPlanInfo.getStage() == WorldWarPlanInfo.STAGE_SEASON_RUNNING) {
            List<Integer> openWeek = Constant.WORLDWAR_OPEN_WEEK;
            int finishWeek = openWeek.get(openWeek.size() - 1);
            int curDayOfWeek = TimeHelper.getCNDayOfWeek();
            // 添加每轮结束
            if (finishWeek == curDayOfWeek) {
                // 当晚的 23.59.59秒执行
                int time = TimeHelper.getSomeDayAfter(0, 23, 59, 59);
                addSchedule(WEEK_TIME_END, this::onWeekEnd, time);
            }
            // 计算开了多少天,设置地图的开放状态
            setMapOpenType();
        }
        // 添加结束定时器
        addSchedule(SEASON_TIME_END, this::onSeasonEnd, worldWarPlanInfo.getEndTime());
        // 添加活动显示时间结束定时器
        addSchedule(SEASON_DISPLAY_TIME_END, this::onSeasonDisplayTimeEnd, worldWarPlanInfo.getDisplayTime());
    }

    /**
     * 设置地图的开放状态
     */
    private void setMapOpenType() {
        if (crossWorldMap.getMapOpenType() == MapOpenType.INNER) { // 内圈都开了 不用判断
            return;
        }
        Date beginDate = new Date(worldWarPlanInfo.getBeginTime() * 1000L);
        int dayNum = DateHelper.dayiy(beginDate, new Date());// 相隔多少天

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
                LogUtil.debug("添加世界争霸相关定时任务  name:", name, ", time:", DateHelper.formatDateMiniTime(atDate));
            } else {
                Date startTime = QuartzHelper.getStartTime(sched, name, DefultJob.DEFULT_GROUP);
                Date atDate = TimeHelper.secondToDate(time);
                if (!startTime.equals(atDate)) {
                    // 移除原来的
                    QuartzHelper.removeJob(sched, name, DefultJob.DEFULT_GROUP);
                    // 重新添加
                    ScheduleManager.getInstance().addOrModifyDefultJob(DefultJob.createDefult(name), job -> {
                        r.onRun();
                    }, atDate);
                    LogUtil.debug("重新修改世界争霸相关定时任务  name:", name, ", time:", DateHelper.formatDateMiniTime(atDate));
                }
            }
        }
    }

    public void checkAndInitWorldWarPlan() {
        if (worldWarPlanInfo == null) {
            // 去检测 并初始化
            StaticWorldWarPlan planCfg = StaticCrossWorldDataMgr.getNowPlan();
            if (planCfg != null) {
                worldWarPlanInfo = WorldWarPlanInfo.createByCfg(planCfg);
            }
        } else {
            if (WorldWarPlanInfo.isFinish(worldWarPlanInfo)) {
                // 时间结束了,需要检测下是否有一轮
                StaticWorldWarPlan planCfg = StaticCrossWorldDataMgr.getNowPlan();
                if (planCfg != null) {
                    worldWarPlanInfo.refreshByCfg(planCfg);
                }
            } else { // 重新读取时间
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
     * 赛季结束时候触发
     */
    public void onSeasonEnd() {
        LogUtil.debug("新地图周赛季结束逻辑处理执行");
        processClose();
        // 清空城池状态
        crossWorldMap.getCityMap().values().forEach(cityEntity -> cityEntity.getCity().clearStateToInit());
        crossWorldMap.setMapOpenType(MapOpenType.OUTER);
        // 赛季结束发放赛季（阵营、个人）排行榜奖励邮件
        WorldWarSeasonMomentOverService seasonMomentOverService = DataResource.ac
                .getBean(WorldWarSeasonMomentOverService.class);
        seasonMomentOverService.seasonOver();
    }

    /**
     * 赛季展示结束 清除玩家赛季数据和阵营赛季积分、军威值
     */
    private void onSeasonDisplayTimeEnd() {
        // 赛季展示结束 清除玩家赛季数据和阵营赛季积分、军威值
        WorldWarSeasonMomentOverService seasonMomentOverService = DataResource.ac
                .getBean(WorldWarSeasonMomentOverService.class);
        seasonMomentOverService.seasonOverClearIntegral();
    }

    private void processClose() {
        // 玩家迁城
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
            // 回德意志
            Integer emptyPos = worldDataManager
                    .randomEmptyByAreaBlock(WorldConstant.AREA_TYPE_13, RandomHelper.randomInArea(1, 100), 1).get(0);
            // Integer emptyPos = worldDataManager
            // .randomEmptyByAreaBlock(staticArea.getArea(), RandomHelper.randomInArea(1, 100), 1).get(0);
            player.lord.setPos(emptyPos);
            player.lord.setArea(WorldConstant.AREA_TYPE_13);
            worldDataManager.putPlayer(player);
            // 新地图移除
            crossWorldMap.getAllMap().remove(playerEntity.getPos());
            // 发送消息给该用户
            sendSyncCrossMapChgMsg(player, crossWorldMap.getMapId());
            // 发邮件
            mailDataManager.sendNormalMail(player, MailConstant.MOLD_WORLD_WAR_CLOSE, now);
            posList.add(emptyPos);
            LogUtil.debug("世界争霸关闭,玩家统一回到老地图 roleId:", player.lord.getLordId(), ", pos:", player.lord.getPos(),
                    ", area:", player.lord.getArea());
        }
        // 地图通知
        EventBus.getDefault()
                .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_AREA_TYPE));
        crossWorldMap.getPlayerMap().clear(); // 清空

        // 部队处理
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        MapMarch mapMarchArmy = crossWorldMap.getMapMarchArmy();
        mapMarchArmy.getDelayArmysQueue().clearQueue(); // 先清空队列
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

        // battle的处理
        MapWarData mapWarData = crossWorldMap.getMapWarData();
        mapWarData.getDelayBattleQueue().clearQueue();
        mapWarData.getAllBattles().clear();
        mapWarData.getBattlePosCache().clear();

        // 矿点处理驻军处理
        List<MineMapEntity> mineList = crossWorldMap.getAllMap().values().stream()
                .filter(baseEntity -> baseEntity.getType() == WorldEntityType.MINE).map(m -> (MineMapEntity) m)
                .filter(m -> m.getGuard() != null).collect(Collectors.toList());
        RewardDataManager rewardDataManager = DataResource.ac.getBean(RewardDataManager.class);
        for (MineMapEntity mineEntity : mineList) {
            // 处理正在采集的玩家
            crossWorldMap.getAllMap().remove(mineEntity.getPos());
            Player amryPlayer = mineEntity.getGuard().getPlayer(); // 提前存着player数据
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
     * 每周的结束
     */
    public void onWeekEnd() {
        LogUtil.debug("新地图周结束逻辑处理执行");
        processClose();
    }

}
