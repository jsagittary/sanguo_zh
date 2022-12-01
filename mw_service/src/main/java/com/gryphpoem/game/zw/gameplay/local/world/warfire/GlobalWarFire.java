package com.gryphpoem.game.zw.gameplay.local.world.warfire;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.rank.RankItem;
import com.gryphpoem.game.zw.core.rank.SimpleRank4SkipSet;
import com.gryphpoem.game.zw.core.rank.SimpleRankComparatorFactory;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.dataMgr.StaticCrossWorldDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticLordDataMgr;
import com.gryphpoem.game.zw.gameplay.local.constant.CrossWorldMapConstant;
import com.gryphpoem.game.zw.gameplay.local.util.MapCurdEvent;
import com.gryphpoem.game.zw.gameplay.local.util.MapEvent;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.WorldEntityType;
import com.gryphpoem.game.zw.gameplay.local.world.army.AttackWFCityArmy;
import com.gryphpoem.game.zw.gameplay.local.world.army.BaseArmy;
import com.gryphpoem.game.zw.gameplay.local.world.army.MapMarch;
import com.gryphpoem.game.zw.gameplay.local.world.army.PlayerArmy;
import com.gryphpoem.game.zw.gameplay.local.world.battle.AttackPlayerBattle;
import com.gryphpoem.game.zw.gameplay.local.world.battle.MapWarData;
import com.gryphpoem.game.zw.gameplay.local.world.map.*;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb5.SyncMapCloseRs;
import com.gryphpoem.game.zw.pojo.p.Fighter;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.quartz.ScheduleManager;
import com.gryphpoem.game.zw.quartz.jobs.DefultJob;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.constant.task.FeatureCategory;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.army.Guard;
import com.gryphpoem.game.zw.resource.pojo.world.Battle;
import com.gryphpoem.game.zw.resource.pojo.world.City;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.resource.util.eventdata.EventDataUp;
import com.gryphpoem.game.zw.service.TaskService;
import com.gryphpoem.game.zw.service.WorldScheduleService;
import com.gryphpoem.game.zw.service.WorldService;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import com.hundredcent.game.ai.util.CheckNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 战火燎原开放计划
 *
 * @program: civilization_zh
 * @description:
 * @author: zhou jie
 * @create: 2020-12-24 11:13
 */
public class GlobalWarFire {

    private static final String WAR_FIRE_DIS_PLAYER_JOB = "WAR_FIRE_DIS_PLAYER_JOB";
    private static final String WAR_FIRE_BEGIN_JOB = "WAR_FIRE_BEGIN_JOB";
    //活动持续期间每秒中都需要执行的JOB
    private static final String WAR_FIRE_RUN_SEC_JOB = "WAR_FIRE_RUN_SEC_JOB";
    private static final String WAR_FIRE_END_JOB = "WAR_FIRE_END_JOB";
    private static final String WAR_FIRE_END_DIS_PLAYER_JOB = "WAR_FIRE_END_DIS_PLAYER_JOB";
    private static final String WAR_FIRE_REFRESH_MINE_JOB = "WAR_FIRE_REFRESH_MINE_JOB";
    // 活动城池占领定时器
    private static final String WAR_FIRE_CITY_OCCUPY_JOB = "WAR_FIRE_CITY_OCCUPY_JOB";
    // 活动城池保护罩定时器
    private static final String WAR_FIRE_CITY_PROTECT_JOB = "WAR_FIRE_CITY_PROTECT_JOB";

    /**
     * 未开始, 或已经结束
     */
    public static final int STAGE_OVER = 0;
    /**
     * 预备期
     */
    public static final int STAGE_DIS_PLAYER = 1;
    /**
     * 正式期
     */
    public static final int STAGE_RUNNING = 2;
    /**
     * 兑换期
     */
    public static final int STAGE_END_PLAYER = 3;

    /**
     * 预显示时间
     */
    private Date disPlayerDate;
    /**
     * 开始的时间
     */
    private Date beginDate;
    /**
     * 结束时间
     */
    private Date endDate;
    /**
     * 结束显示时间
     */
    private Date endDisPlayerDate;
    /**
     * 玩家数据
     */
    private Map<Long, PlayerWarFire> playerWarFireMap = new HashMap<>();

    /**
     * 阵营数据KEY:阵营ID, 阵营数据
     */
    private Map<Integer, CampWarFire> campMap = new HashMap<>();

    /**
     * 积分排名信息, KEY:阵营,0-全服排名 (1,2,3)阵营排名 VALUE: 排名列表
     */
    private Map<Integer, SimpleRank4SkipSet<Integer>> scoreRanks;

    /**
     * 阵营排行
     */
    private SimpleRank4SkipSet<Integer> campRanks;

    /**
     * 跨服地图
     */
    private final CrossWorldMap crossWorldMap;

    /**
     * 获取战火燎原, 玩家数据
     *
     * @param roleId 玩家id
     * @return 玩家数据
     */
    public PlayerWarFire getPlayerWarFire(long roleId) {
        return getPlayerWarFireMap().computeIfAbsent(roleId, (k) -> new PlayerWarFire(roleId));
    }

    public GlobalWarFire(CrossWorldMap crossWorldMap) {
        this.crossWorldMap = crossWorldMap;
        Comparator<RankItem<Integer>> comparator = SimpleRankComparatorFactory.createRankComparable();
        Comparator<RankItem<Integer>> reversedComparator = comparator.reversed();
        //积分排行初始化
        //阵营积分排名
        campRanks = new SimpleRank4SkipSet<>(3, reversedComparator);
        //玩家积分排名
        scoreRanks = new HashMap<>();
        scoreRanks.put(Constant.Camp.NPC, new SimpleRank4SkipSet<>(300, reversedComparator));
        for (int camp : Constant.Camp.camps) {
            scoreRanks.put(camp, new SimpleRank4SkipSet<>(100, reversedComparator));
            CampWarFire cwf = campMap.computeIfAbsent(camp, k -> new CampWarFire(camp));
            updateCampScoreRank(cwf);
        }
    }

    /**
     * 战火燎原的开放信息
     *
     * @param globalWarFire 战火燎原
     * @param player        玩家对象
     * @return WarFirePlanInfoPb
     */
    public static CommonPb.WarFirePlanInfoPb toWarFireInfoPb(GlobalWarFire globalWarFire, Player player) {
        CommonPb.WarFirePlanInfoPb.Builder builder = CommonPb.WarFirePlanInfoPb.newBuilder();
        if (!globalWarFire.uninitialized()) {
            builder.setDisPlayerTime(TimeHelper.dateToSecond(globalWarFire.getDisPlayerDate()));
            builder.setBeginTime(TimeHelper.dateToSecond(globalWarFire.getBeginDate()));
            builder.setEndTime(TimeHelper.dateToSecond(globalWarFire.getEndDate()));
            builder.setEndDisPlayerTime(TimeHelper.dateToSecond(globalWarFire.getEndDisPlayerDate()));
        }
        if (Objects.nonNull(player)) {
            PlayerWarFire playerWarFire = globalWarFire.getPlayerWarFire(player.roleId);
            if (Objects.nonNull(playerWarFire)) {
                CommonPb.PlayerWarFirePb.Builder pwfBuilder = PlayerWarFire.toPlayerInfoPb(playerWarFire);
                pwfBuilder.setWfcoin(player.getMixtureDataById(PlayerConstant.WAR_FIRE_PRICE));
                builder.addPlayer(pwfBuilder);
            }
        } else {
            globalWarFire.getPlayerWarFireMap().values().forEach(pwf -> builder.addPlayer(PlayerWarFire.toPlayerInfoPb(pwf)));
            if (!CheckNull.isEmpty(globalWarFire.getCampMap())) {//阵营信息
                globalWarFire.getCampMap().forEach((k, v) -> builder.addCamp(v.toPb()));
            }
        }
        return builder.build();
    }

    public void deSer(CommonPb.WarFirePlanInfoPb wfPlanInfo) {
        if (wfPlanInfo.hasDisPlayerTime()) {
            this.disPlayerDate = TimeHelper.secondToDate(wfPlanInfo.getDisPlayerTime());
        }
        if (wfPlanInfo.hasBeginTime()) {
            this.beginDate = TimeHelper.secondToDate(wfPlanInfo.getBeginTime());
        }
        if (wfPlanInfo.hasEndTime()) {
            this.endDate = TimeHelper.secondToDate(wfPlanInfo.getEndTime());
        }
        if (wfPlanInfo.hasEndDisPlayerTime()) {
            this.endDisPlayerDate = TimeHelper.secondToDate(wfPlanInfo.getEndDisPlayerTime());
        }
        List<CommonPb.PlayerWarFirePb> playerList = wfPlanInfo.getPlayerList();
        if (!CheckNull.isEmpty(playerList)) {
            PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
            playerList.forEach(pwfPb -> {
                PlayerWarFire pwf = new PlayerWarFire(pwfPb);
                playerWarFireMap.put(pwf.getRoleId(), pwf);
                if (pwf.getScore() > 0) {//积分大于0则进入排行榜
                    Player player = playerDataManager.getPlayer(pwf.getRoleId());
                    if (Objects.nonNull(player)) {
                        updatePlayerScoreRank(player, pwf);
                    }
                }
            });

        }
        List<CommonPb.CampWarFirePb> campPbList = wfPlanInfo.getCampList();
        if (!CheckNull.isEmpty(campPbList)) {//阵营数据
            campPbList.forEach(pb -> {
                CampWarFire cwf = new CampWarFire(pb.getCamp(), pb.getScore());
                campMap.put(pb.getCamp(), cwf);
                updateCampScoreRank(cwf);
            });
        }
    }

    /**
     * 判断玩家是否购买了buff
     *
     * @return true 购买了, false 未购买
     */
    public boolean alreadyBuyBuff(Player player) {
        PlayerWarFire playerWarFire = playerWarFireMap.get(player.roleId);
        return Objects.nonNull(playerWarFire) && !CheckNull.isEmpty(playerWarFire.getBuffs());
    }


    /**
     * 是否可以进入地图
     *
     * @return true 可以进入
     */
    public boolean canEnterMap() {
        // 功能开启的配置
        List<List<String>> warFireTimeConf = WorldConstant.WAR_FIRE_TIME_CONF;
        if (CheckNull.isEmpty(warFireTimeConf)) {
            return false;
        }
        // 进入世界配置
        List<String> enterWarConf = warFireTimeConf.get(4);
        if (CheckNull.isEmpty(enterWarConf)) {
            return false;
        }
        // 提前分钟
        int aheadTime = Integer.parseInt(enterWarConf.get(0));
        int beginSecond = TimeHelper.dateToSecond(this.beginDate);
        Date enterDate = TimeHelper.secondToDate(beginSecond - (TimeHelper.MINUTE_S * aheadTime));
        Date nowDate = new Date();
        // 开启前5分钟, 到结束时间
        return getStage() == STAGE_RUNNING || (nowDate.after(enterDate) && nowDate.before(this.endDate));
    }


    /**
     * 是否可以兑换
     *
     * @return true 可以兑换
     */
    public boolean canExchange() {
        return getStage() != STAGE_OVER;
    }

    /**
     * 是否可以购买buff
     *
     * @return true 可以购买
     */
    public boolean canBuyBuff() {
        int stage = getStage();
        return stage == STAGE_DIS_PLAYER || stage == STAGE_RUNNING;
    }


    /**
     * 获取阶段
     *
     * @return 0 未开始, 或已经结束 <br/>
     * 1 预备期 <br/>
     * 2 正式期 <br/>
     * 3 兑换期 <br/>
     */
    public int getStage() {
        Date now = new Date();
        if (Objects.nonNull(disPlayerDate) && Objects.nonNull(beginDate) && Objects.nonNull(endDate) && Objects.nonNull(endDisPlayerDate)) {
            if (now.before(disPlayerDate) || now.after(endDisPlayerDate)) {
                // 结束
                return STAGE_OVER;
            } else if (now.after(disPlayerDate) && now.before(beginDate)) {
                // 预显示
                return STAGE_DIS_PLAYER;
            } else if (now.after(beginDate) && now.before(endDate)) {
                return STAGE_RUNNING;
            } else if (now.after(endDate) && now.before(endDisPlayerDate)) {
                return STAGE_END_PLAYER;
            }
        }
        return STAGE_OVER;
    }

    /**
     * 跨天执行、反序列化执行
     */
    public void onAcrossDayRun() {
        Date now = new Date();
        if (!checkUnlock(now)) {
            // 未解锁
            return;
        }
        // 未初始化或者已经过了开启时间
        if (uninitialized() || now.after(beginDate)) {
            initWarFireTime();
        }
        if (!uninitialized()) {
            initScheduleJob(now);
        }
    }

    /**
     * 初始化功能时间
     */
    private void initWarFireTime() {
        // 服务器时间
        Date now = new Date();

        // 功能开启的配置
        List<List<String>> warFireTimeConf = WorldConstant.WAR_FIRE_TIME_CONF;
        if (CheckNull.isEmpty(warFireTimeConf)) {
            return;
        }
        // 预显示时间配置
        List<String> preDisConf = warFireTimeConf.get(0);
        int preWeek = TimeHelper.getCalendarDayOfWeek(TimeHelper.getWeekEnConverNum(preDisConf.get(0)));
        String preViewTime = preDisConf.get(1);
        // 开启时间配置
        List<String> openConf = warFireTimeConf.get(1);
        int openWeek = TimeHelper.getCalendarDayOfWeek(TimeHelper.getWeekEnConverNum(openConf.get(0)));
        String beginTime = openConf.get(1);
        // 结束时间配置
        List<String> endConf = warFireTimeConf.get(2);
        int endWeek = TimeHelper.getCalendarDayOfWeek(TimeHelper.getWeekEnConverNum(endConf.get(0)));
        String endTime = endConf.get(1);
        List<String> endDisConf = warFireTimeConf.get(3);
        // 兑换时间配置
        int endDisWeek = TimeHelper.getCalendarDayOfWeek(TimeHelper.getWeekEnConverNum(endDisConf.get(0)));
        String endDisTime = endDisConf.get(1);
        // 功能循环配置
        List<String> cycleConf = warFireTimeConf.get(5);

        // 未初始化, 或者已经过了结束后延迟显示的时间, 并且已经过了上次预现实的第7天
        if (uninitialized() || (now.after(endDisPlayerDate) && DateHelper.dayiy(disPlayerDate, now) > Integer.parseInt(cycleConf.get(0)))) {
            // 开启时间
            Date nextBegin = DateHelper.afterStringTime(now, beginTime, openWeek);
            // 过了开启时间
//            if (now.after(nextBegin)) {
//                return;
//            }
            // 结束时间
            Date nextEnd = DateHelper.afterStringTime(now, endTime, endWeek);
            // 预显示时间
            Date nextPreView = DateHelper.afterStringTime(now, preViewTime, preWeek);
            // 结束后时间
            Date nextEndDis = DateHelper.afterStringTime(now, endDisTime, endDisWeek);
            // 时间初始化
            this.beginDate = nextBegin;
            this.endDate = nextEnd;
            this.disPlayerDate = nextPreView;
            this.endDisPlayerDate = nextEndDis;
            // 清空玩家的数据
            this.playerWarFireMap.clear();
            // 清空阵营数据
            this.campMap.clear();
            //清除排行榜记录
            this.campRanks.clear();
            this.scoreRanks.forEach((k, v) -> v.clear());
            for (int camp : Constant.Camp.camps) {
                CampWarFire wfc = this.campMap.computeIfAbsent(camp, CampWarFire::new);
                updateCampScoreRank(wfc);
            }
            // 清空城池状态
            crossWorldMap.getCityMap().values().forEach(CityMapEntity::clearStateToInit);
            // 初始化或者重置
            this.crossWorldMap.getMapEntityGenerator().initAndRefreshWfSafeArea();
            // 安全区
            List<WFSafeAreaMapEntity> campSafeArea = this.crossWorldMap.getSafeArea();

            if (campSafeArea.size() != Constant.Camp.camps.length) {
                LogUtil.error("战火燎原, 安全区配置数量有误！");
                return;
            }
            // 随机安全区的阵营
            List<Integer> camps = Arrays.stream(Constant.Camp.camps).boxed().collect(Collectors.toList());
            Collections.shuffle(camps);
            for (int i = 0; i < camps.size(); i++) {
                int camp = camps.get(i);
                WFSafeAreaMapEntity sam = campSafeArea.get(i);
                sam.setCamp(camp);
            }

            // 城池加上保护罩
            Map<Integer, StaticWarFire> staticWarFireMap = StaticCrossWorldDataMgr.getStaticWarFireMap();
            if (!CheckNull.isEmpty(staticWarFireMap)) {
                staticWarFireMap.forEach((cityId, swf) -> {
                    CityMapEntity mapEntity = this.crossWorldMap.getCityMap().get(cityId);
                    if (Objects.nonNull(mapEntity) && mapEntity instanceof WFCityMapEntity) {
                        City city = mapEntity.getCity();
                        // 城池保护罩
                        int protectTime = TimeHelper.dateToSecond(this.beginDate) + swf.getProtectTime();
                        city.setProtectTime(protectTime);
                        String scheduleName = WAR_FIRE_CITY_PROTECT_JOB + cityId;
                        // 添加到达保护罩时间的定时器
                        addSchedule(scheduleName, this::onProtectOver, protectTime, (WFCityMapEntity) mapEntity);
                    }
                });
            }
        }
    }

    /**
     * 加入定时任务
     */
    private void initScheduleJob(Date now) {
        if (now.before(disPlayerDate)) {
            addSchedule(WAR_FIRE_DIS_PLAYER_JOB, this::onDisPlayer, this.disPlayerDate, this);
        }
        if (now.before(beginDate)) {
            addSchedule(WAR_FIRE_BEGIN_JOB, this::onBegin, this.beginDate, this);
        }
        if (now.before(endDate)) {
            addSchedule(WAR_FIRE_END_JOB, this::onEnd, this.endDate, this);
            // 功能开启的配置
            List<List<String>> warFireTimeConf = WorldConstant.WAR_FIRE_TIME_CONF;
            if (CheckNull.isEmpty(warFireTimeConf)) {
                return;
            }
            // 刷新资源点配置
            List<String> refreshRule = warFireTimeConf.get(6);
            int refreshAfterBegin = Integer.parseInt(refreshRule.get(0));
            int interval = Integer.parseInt(refreshRule.get(1));
            Date scheduleStartTime = beginDate;
            if (now.after(beginDate)) scheduleStartTime = now;
            Date refreshStart = TimeHelper.secondToDate(TimeHelper.dateToSecond(scheduleStartTime) + refreshAfterBegin);
            if (refreshStart.before(endDate)) {
                addSchedule(WAR_FIRE_REFRESH_MINE_JOB, this::onRefreshMine, refreshStart, this.endDate, interval, this);
            }
            Date runSecStart = TimeHelper.secondToDate(TimeHelper.dateToSecond(scheduleStartTime) + 10);
            if (runSecStart.before(endDate)) {
                addSchedule(WAR_FIRE_RUN_SEC_JOB, this::onSecond, runSecStart, this.endDate, 1, this);
            }
        }
        if (now.before(endDisPlayerDate)) {
            addSchedule(WAR_FIRE_END_DIS_PLAYER_JOB, this::onEndDisPlayer, this.endDisPlayerDate, this);
        }
    }


    /**
     * 预显示
     *
     * @param globalWarFire
     * @param defultJob
     */
    private void onDisPlayer(GlobalWarFire globalWarFire, DefultJob defultJob) {
        LogUtil.world("---------------------战火燎原预显示---------------------");

    }

    /**
     * 开启
     *
     * @param globalWarFire
     * @param defultJob
     */
    private void onBegin(GlobalWarFire globalWarFire, DefultJob defultJob) {
        LogUtil.world("---------------------战火燎原开启---------------------");
        // 购买了buff的玩家, 功能开启的时候, 重新计算将领数据
        crossWorldMap.getPlayerMap().values().stream().map(PlayerMapEntity::getPlayer).filter(this::alreadyBuyBuff).forEach(CalculateUtil::reCalcAllHeroAttr);
    }

    private void onSecond(GlobalWarFire globalWarFire, DefultJob defultJob) {
        //        LogUtil.world("---------------------战火燎原每秒中执行一次---------------------");
        if (getStage() != STAGE_RUNNING) return;//活动已结束不再跑秒计算
        int nowSec = TimeHelper.getCurrentSecond();
        //据点资源产出
        crossWorldMap.getCityMap().values().stream()
                .filter(v -> v instanceof WFCityMapEntity)
                .map(v -> (WFCityMapEntity) v)
                .filter(wfc -> wfc.getCity().getCamp() > 0)
                .filter(wfc -> wfc.getStatus() == WFCityMapEntity.WF_CITY_STATUS_2)//过滤未被占领的据点
                .filter(wfc -> nowSec - wfc.getLastScoreTime() >= TimeHelper.MINUTE_S)//过滤产出时间不足的据点
                .forEach(wfc -> cityProductScore(wfc, nowSec));//产出资源


        //采集矿点产出
        for (Map.Entry<Integer, BaseWorldEntity> entry : crossWorldMap.getAllMap().entrySet()) {
            BaseWorldEntity entity = entry.getValue();
            if (!(entity instanceof WFMineMapEntity)) continue;//采集矿点判定
            WFMineMapEntity wfm = (WFMineMapEntity) entity;
            StaticMine staticMine = wfm.getCfgMine();
            if (staticMine == null || CheckNull.isEmpty(staticMine.getReward())) {
                LogUtil.error("战火燎原 --- 采集点 : ", wfm.getMineId(), " 没有配置");
                continue;
            }
            //铁矿石资源没有了, 则将其从地图中删除
            if (wfm.getRemainRes() <= 0) {
                if (wfm.getGuard() == null) {
                    //防止出现异常状况时0资源矿点不能删除,并且玩家部队不能返回
                    wfm.removeWorldWFMineEntity(crossWorldMap);
                    continue;
                }
            }

            //距离上次采集时间不足1分钟
            if (nowSec - wfm.getLastScoreTime() < TimeHelper.MINUTE_S) {
                continue;//累计时间不足一分钟
            }

            Guard guard = wfm.getGuard();
            if (guard == null) continue;//采集点没有驻军
            Player player = guard.getPlayer();
            PlayerWarFire pwf = getPlayerWarFire(player.getLordId());
            //采集一次积分
            collectMine(wfm, pwf, nowSec);
        }
    }

    public void collectMine(WFMineMapEntity wfMine, PlayerWarFire pwf, int nowSec) {
        int speedMin = wfMine.getCfgMine().getSpeed() / 60;
        wfMine.setLastScoreTime(nowSec);
        addPlayerScore(pwf, speedMin, AwardFrom.WAR_FIRE_SCORE_MINE_MINUTE_OUTPUT, wfMine.getRemainRes());
        updateMine(wfMine, speedMin);
        Army army = wfMine.getGuard() != null ? wfMine.getGuard().getArmy() : null;
        int heroId = army != null ? army.getHero().get(0).getPrincipleHeroId() : 0;
        if (heroId > 0) {
            pwf.addWarFireEvent(WarFireEvent.ETY_MINE_OUTPUT, wfMine.getCfgMine().getMineId(), wfMine.getPos(), heroId, speedMin);
        }
        LogUtil.world(String.format("战火燎原 --- 玩家 :%d, 采集矿点ID :%d, 矿点位置 :%d, 持续产出积分 :%d, 当前积分 :%d , 矿点剩余资源 :%d",
                pwf.getRoleId(), wfMine.getCfgMine().getMineId(), wfMine.getPos(), speedMin, pwf.getScore(), wfMine.getRemainRes()));
    }

    /**
     * 更新资源点资源
     *
     * @param wfm    矿点对象
     * @param change 变动
     */
    public void updateMine(WFMineMapEntity wfm, int change) {
        Guard guard = wfm.getGuard();
        if (guard == null) return;
        wfm.setRemainRes(Math.max(0, wfm.getRemainRes() - change));
        int grabCount = change;
        if (!CheckNull.isEmpty(guard.getGrab())) {
            CommonPb.Award award = guard.getGrab().get(0);
            grabCount = award.getCount() + change;
        }
        List<CommonPb.Award> grabList = WarFireUtil.createMineAwards(wfm, grabCount);
        guard.getArmy().setGrab(grabList);
    }

    /**
     * 结束
     *
     * @param globalWarFire
     * @param defultJob
     */
    public void onEnd(GlobalWarFire globalWarFire, DefultJob defultJob) {
        LogUtil.world("---------------------战火燎原结束---------------------");
        //计算阵营额外积分, 并返回阵营排名信息 KEY:阵营ID, VALUE:阵营排名奖励配置
        Map<Integer, CampWarFire.FinishCalc> finishCalcMap = new HashMap<>();
        Map<Integer, StaticWarFireRankCamp> campRankAwardMap = calcCampRankAwards(finishCalcMap);
        MailDataManager maildataMgr = DataResource.ac.getBean(MailDataManager.class);
        PlayerDataManager playerDataMgr = DataResource.ac.getBean(PlayerDataManager.class);
        RewardDataManager rewardDataManager = DataResource.ac.getBean(RewardDataManager.class);
        int nowSec = TimeHelper.getCurrentSecond();
        try {
            //对前三名发送称号
            SimpleRank4SkipSet<Integer> playerRanks = globalWarFire.getScoreRanks().get(Constant.Camp.NPC);
            List<RankItem<Integer>> rankList = playerRanks.getRankList(0, 3);
            int haveRank = 3;
            if (rankList.size() < 3) {
                haveRank = rankList.size();
            }
            for (int i = 0; i < haveRank; i++) {
                RankItem<Integer> item = rankList.get(i);
                Player player = playerDataMgr.getPlayer(item.getLordId());
                //增加限时称号
                StaticTitle staticTitle = StaticLordDataMgr.getTitleMapById(StaticCastleSkin.WAR_FIRE_WINNER_TITLE_ID);
                rewardDataManager.addAward(player, AwardType.TITLE, staticTitle.getId(), Math.toIntExact(staticTitle.getDuration()), AwardFrom.WAR_FIRE_PERSONAL_RANK_AWARD);
            }
        } catch (Exception e) {
            LogUtil.error("对前三名发送称号", e);
        }
        playerWarFireMap.forEach((k, v) -> {
            if (v.getStatus() == PlayerWarFire.NOT_REGISTRY_STATUS) {
                return;//玩家没有参与过该活动(未进入过战火地图)
            }
            Player player = playerDataMgr.getPlayer(k);
            CampWarFire cwf = campMap.get(player.getCamp());//阵营数据
            CampWarFire.FinishCalc calc = finishCalcMap.computeIfAbsent(player.getCamp(), t -> new CampWarFire.FinishCalc());
            int grCnt = 0, cpCnt = 0;//个人奖励铸铁币数量,阵营排名奖励铸铁币数量
            //阵营排名奖励配置
            StaticWarFireRankCamp staticCp = campRankAwardMap.get(player.getCamp());
            List<Integer> cpAwards = staticCp.getAwardList();
            if (cpAwards != null && cwf.getScore() > 0) cpCnt = cpAwards.get(2);//阵营排名奖励铸铁币数量
            //个人奖励配置
            StaticWarFireRankGr staticGr = calcPlayerScoreAwards(v);
            List<Integer> grAwards = staticGr != null ? staticGr.getAwardList() : null;
            if (grAwards != null) grCnt = grAwards.get(2);//个人奖励铸铁币数量
            if (v.getScore() < WorldConstant.WAR_FIRE_AWARDS_SCORE_LIMIT) {
                LogUtil.world(String.format("战火燎原 --- 玩家 :%d, 积分 :%d, 未达到发放奖励所需积分下限 :%d !!!", k, v.getScore(), WorldConstant.WAR_FIRE_AWARDS_SCORE_LIMIT));
                sendPlayerWarfireFinishMail(player, v, cwf, calc, grCnt, cpCnt, nowSec, maildataMgr);
                return;
            }

            int preAwardCount = cpCnt + grCnt;
            //赛季天赋优化， 积分加成
            int awardCountBuff = (int) (preAwardCount * (1 + (DataResource.getBean(SeasonTalentService.class).
                    getSeasonTalentEffectValue(player, SeasonConst.TALENT_EFFECT_620) / Constant.TEN_THROUSAND)));
            CommonPb.Award awardPb = PbHelper.createAwardPb(AwardType.MONEY, AwardType.Money.WAR_FIRE_COIN, awardCountBuff);
            List<CommonPb.Award> finalAwards = new ArrayList<>(1);
            finalAwards.add(awardPb);
            LogUtil.world(String.format("战火燎原 --- 玩家 :%d, 积分 :%d, 个人奖励 :%d, 阵营奖励 :%d", k, v.getScore(), grCnt, cpCnt));
            sendPlayerWarfireFinishAwardsMail(player, v, cwf, calc, grCnt, cpCnt, finalAwards, nowSec, maildataMgr,
                    DataResource.getBean(SeasonTalentService.class).getSeasonTalentIdStr(player, SeasonConst.TALENT_EFFECT_620),
                    awardCountBuff - preAwardCount);
        });
        // 战火燎原结束逻辑, 玩家迁城、重置
        endLogic();
    }

    /**
     * 感谢领主参与本次战火燎原活动
     * 1.您所在的%s阵营在活动结束时
     * 获得阵营资源：%s
     * 占领了%s个1级据点；%s个2级据点；%s个3级据点，额外获得%s阵营资源。
     * 总计获得%s阵营资源，排名%s，可获得%s铸铁币
     * 2.您获得了：%s个人资源，
     * 累计杀敌%s，额外获得%s个人资源
     * 总计获得%s个人资源，可获得%s铸铁币
     * 3.您已达到活动要求最低要求个人资源：2000，最终获得铸铁币：%s
     * 请到战火燎原兑换商店，换取心仪的物品。
     *
     * @param player      玩家对象
     * @param pwf         玩家战火数据
     * @param cwf         玩家所在阵营战火数据
     * @param calc        玩家所在阵营战火数据统计
     * @param grCnt       个人获得到的铸铁币
     * @param cpCnt       阵营排名奖励的铸铁币
     * @param finalAwards 最终奖励列表
     * @param nowSec      当前时间
     * @param mailMgr     mgr
     */
    private void sendPlayerWarfireFinishAwardsMail(Player player, PlayerWarFire pwf, CampWarFire cwf, CampWarFire.FinishCalc calc, int grCnt, int cpCnt,
                                                   List<CommonPb.Award> finalAwards, int nowSec, MailDataManager mailMgr, String seasonTalentId, int seasonTalentDifference) {
        int killedScore = (pwf.getKilled() / WorldConstant.WAR_FIRE_KILL_SCORE.get(0)) * WorldConstant.WAR_FIRE_KILL_SCORE.get(1);
        mailMgr.sendAttachMail(player, finalAwards, MailConstant.MOLD_ACT_WAR_FIRE_AWARD_NEW, AwardFrom.WAR_FIRE_SCORE_AWARDS, nowSec,
                //阵营信息
                player.getCamp(),
                cwf.getScore() - calc.extScore,//结束时获得到的阵营资源
                calc.tyCntMap.getOrDefault(1, 0), calc.tyCntMap.getOrDefault(2, 0), calc.tyCntMap.getOrDefault(3, 0), calc.extScore,
                cwf.getScore(), calc.rank, cpCnt,
                //个人信息
                pwf.getScore() - killedScore,
                pwf.getKilled(), killedScore,
                pwf.getScore(), grCnt,
                grCnt + cpCnt + seasonTalentDifference, seasonTalentId, seasonTalentDifference);
    }

    /**
     * 感谢领主参与本次战火燎原活动
     * 1.您所在的%s阵营在活动结束时
     * 获得阵营资源：%s
     * 占领了%s个1级据点；%s个2级据点；%s个3级据点，额外获得%s阵营资源。
     * 总计获得%s阵营资源，排名%s，可获得%s铸铁币
     * 2.您获得了：%s个人资源，
     * 累计杀敌%s，额外获得%s个人资源
     * 总计获得%s个人资源，可获得%s铸铁币
     * 3.您未达活动要求最低要求个人资源：2000，无法获得铸铁币，请再接再厉。
     *
     * @param player
     * @param pwf
     * @param cwf
     * @param calc
     * @param grCnt
     * @param cpCnt
     * @param nowSec
     * @param mailMgr
     */
    private void sendPlayerWarfireFinishMail(Player player, PlayerWarFire pwf, CampWarFire cwf, CampWarFire.FinishCalc calc, int grCnt, int cpCnt,
                                             int nowSec, MailDataManager mailMgr) {
        int killedScore = (pwf.getKilled() / WorldConstant.WAR_FIRE_KILL_SCORE.get(0)) * WorldConstant.WAR_FIRE_KILL_SCORE.get(1);
        mailMgr.sendNormalMail(player, MailConstant.MOLD_ACT_WAR_FIRE_SCROE_NOT_ENOUGH_NEW, nowSec,
                //阵营信息
                player.getCamp(),
                cwf.getScore() - calc.extScore,//结束时获得到的阵营资源
                calc.tyCntMap.getOrDefault(1, 0), calc.tyCntMap.getOrDefault(2, 0), calc.tyCntMap.getOrDefault(3, 0), calc.extScore,
                cwf.getScore(), calc.rank, cpCnt,
                //个人信息
                pwf.getScore() - killedScore,
                pwf.getKilled(), killedScore,
                pwf.getScore(), grCnt);
    }

    /**
     * 战火燎原结束逻辑
     */
    public void endLogic() {
        // 玩家迁城
        WorldDataManager worldDataManager = DataResource.ac.getBean(WorldDataManager.class);
        MailDataManager mailDataManager = DataResource.ac.getBean(MailDataManager.class);
        int now = TimeHelper.getCurrentSecond();

        // 部队处理
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        MapMarch mapMarchArmy = crossWorldMap.getMapMarchArmy();
        // 先清空队列
        mapMarchArmy.getDelayArmysQueue().clearQueue();
        for (PlayerArmy playerArmy : mapMarchArmy.getPlayerArmyMap().values()) {
            long roleId = playerArmy.getRoleId();
            Player player = playerDataManager.getPlayer(roleId);
            if (player != null) {
                for (BaseArmy baseArmy : playerArmy.getArmy().values()) {
                    // 事件通知
                    mapMarchArmy.getCrossWorldMap().publishMapEvent(baseArmy.createMapEvent(MapCurdEvent.DELETE));
                    baseArmy.setArmyPlayerHeroState(mapMarchArmy, ArmyConstant.ARMY_STATE_IDLE);
                }
            }
        }
        mapMarchArmy.getPlayerArmyMap().clear();

        // battle的处理
        MapWarData mapWarData = crossWorldMap.getMapWarData();
        mapWarData.getDelayBattleQueue().clearQueue();
        WorldService worldService = DataResource.ac.getBean(WorldService.class);
        // 结束推送军情取消
        mapWarData.getAllBattles().values()
                .stream()
                .filter(be -> be instanceof AttackPlayerBattle)
                .forEach(be -> {
                    Battle battle = be.getBattle();
                    Optional.ofNullable(battle.getSponsor())
                            .ifPresent(invokePlayer -> {
                                // 军情的取消
                                worldService.syncAttackRole(battle.getDefencer(), invokePlayer.lord, battle.getBattleTime(), WorldConstant.ATTACK_ROLE_0);
                            });
                });
        mapWarData.getAllBattles().values().clear();
        mapWarData.getBattlePosCache().clear();

        // 矿点处理驻军处理
        RewardDataManager rewardDataManager = DataResource.ac.getBean(RewardDataManager.class);
        crossWorldMap.getAllMap().values().stream()
                .filter(baseEntity -> baseEntity.getType() == WorldEntityType.MINE)
                .map(m -> (MineMapEntity) m)
                .forEach(m -> {
                    if (Objects.nonNull(m.getGuard())) {
                        Player amryPlayer = m.getGuard().getPlayer(); // 提前存着player数据
                        CommonPb.MailCollect mailCollect = m.settleCollect(crossWorldMap);
                        if (amryPlayer != null) {
                            List<CommonPb.Award> grab = mailCollect.getGrabList();
                            rewardDataManager.sendRewardByAwardList(amryPlayer, grab, AwardFrom.COLLECT);
                            StaticMine staticMine = m.getCfgMine();
                            mailDataManager.sendCollectMail(amryPlayer, null, MailConstant.MOLD_CROSSMAP_COLLECT_BREAK, mailCollect,
                                    now, staticMine.getMineId(), grab.get(0).getCount(), staticMine.getMineId(),
                                    grab.get(0).getCount());
                        }
                    }
                    // 移除采集点
                    crossWorldMap.getAllMap().remove(m.getPos());
                });

        Map<Long, PlayerMapEntity> playerMap = crossWorldMap.getPlayerMap();
        List<Integer> posList = new ArrayList<>();
        for (PlayerMapEntity playerEntity : playerMap.values()) {
            Player player = playerEntity.getPlayer();
            // 在德意志找个点迁过去
            int newPos = worldDataManager.randomKingAreaPos(player.lord.getCamp());
            player.lord.setPos(newPos);
            player.lord.setArea(MapHelper.getAreaIdByPos(newPos));
            worldDataManager.putPlayer(player);
            // 新地图移除
            crossWorldMap.getAllMap().remove(playerEntity.getPos());
            // 发送消息给该用户
            sendSyncCrossMapChgMsg(player, crossWorldMap.getMapId());
            posList.add(newPos);
            // 退出地图重新计算玩家所有将领属性
            CalculateUtil.reCalcAllHeroAttr(player);
            LogUtil.world("战火燎原结束 , 将玩家迁移到圣城区域 roleId:", player.lord.getLordId(), ", pos:", player.lord.getPos(), ", area:", player.lord.getArea());
        }
        // 地图通知
        EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_AREA_TYPE));
        // 清空
        crossWorldMap.getPlayerMap().clear();
    }

    /**
     * 同步玩家状态
     *
     * @param player
     * @param mapId
     */
    private void sendSyncCrossMapChgMsg(Player player, int mapId) {
        if (player.ctx != null && player.isLogin) {
            SyncMapCloseRs.Builder builder = SyncMapCloseRs.newBuilder();
            builder.setMapId(mapId);
            builder.setArea(player.lord.getArea());
            builder.setPos(player.lord.getPos());
            BasePb.Base.Builder msg = PbHelper.createSynBase(SyncMapCloseRs.EXT_FIELD_NUMBER, SyncMapCloseRs.ext,
                    builder.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
        }
    }

    /**
     * 结束显示
     *
     * @param globalWarFire
     * @param defultJob
     */
    private void onEndDisPlayer(GlobalWarFire globalWarFire, DefultJob defultJob) {
        LogUtil.world("---------------------战火燎原结束显示---------------------");

    }

    /**
     * 刷新资源点
     *
     * @param globalWarFire
     * @param defultJob
     */
    public void onRefreshMine(GlobalWarFire globalWarFire, DefultJob defultJob) {
        LogUtil.world("---------------------战火燎原刷新资源点---------------------");
        // 功能开启的配置
        List<List<String>> warFireTimeConf = WorldConstant.WAR_FIRE_TIME_CONF;
        if (CheckNull.isEmpty(warFireTimeConf)) {
            return;
        }
        // 刷新资源点配置
        List<String> refreshRule = warFireTimeConf.get(6);
        int param1 = Integer.parseInt(refreshRule.get(2));
        int param2 = Integer.parseInt(refreshRule.get(3));
        if (globalWarFire.getStage() == GlobalWarFire.STAGE_RUNNING) {
            // 战火燎原资源点
            List<WFMineMapEntity> specialCity = crossWorldMap.getAllMap().values().stream()
                    .filter(baseEntity -> baseEntity.getType() == WorldEntityType.MINE && baseEntity instanceof WFMineMapEntity)
                    .map(m -> (WFMineMapEntity) m)
                    .collect(Collectors.toList());
            int alreadyHave = specialCity.size();
            // 参入活动的玩家
            int size = crossWorldMap.getPlayerMap().size();
            int needCnt = (int) Math.ceil(size * 1.00 / param1 * (param2 / Constant.TEN_THROUSAND));
            if (alreadyHave >= needCnt) {
                LogUtil.world("战火燎原刷新资源点, 已有资源点cnt: ", alreadyHave, ", needCnt: ", needCnt, ", joinRole: ", size);
                return;
            }
            int addCnt = needCnt - alreadyHave;
            getCrossWorldMap().getMapEntityGenerator().initAndRefreshWFMine(addCnt);
        }
    }


    /**
     * 未初始化
     *
     * @return true 未初始化、false 已初始化
     */
    private boolean uninitialized() {
        return Objects.isNull(disPlayerDate) || Objects.isNull(beginDate) || Objects.isNull(endDate) || Objects.isNull(endDisPlayerDate);
    }

    /**
     * 检测功能开放
     *
     * @param now 服务器时间
     * @return true 功能开放, false 未开放
     */
    private boolean checkUnlock(Date now) {
        // 战火燎原等级区服配置
        List<List<Integer>> warFireOpenCondConf = WorldConstant.WAR_FIRE_OPEN_COND_CONF;
        if (CheckNull.isEmpty(warFireOpenCondConf)) {
            return false;
        }
        ServerSetting serverSetting = DataResource.ac.getBean(ServerSetting.class);
        WorldScheduleService scheduleService = DataResource.ac.getBean(WorldScheduleService.class);
        // 当前的世界进程
        int curSchedule = scheduleService.getCurrentSchduleId();
        // 开服的第多少天
        int openServerDay = serverSetting.getOpenServerDay(now);
        // 开服天数
        int openServerConf = warFireOpenCondConf.get(1).get(0);
        // 区服配置
        List<Integer> serverConf = warFireOpenCondConf.get(2);
        // 开服30天, 世界进程
        return openServerDay > openServerConf && serverSetting.getServerID() >= serverConf.get(0) && serverSetting.getServerID() <= serverConf.get(1) && curSchedule >= ScheduleConstant.SCHEDULE_BERLIN_ID;
    }

    /**
     * 加入定时器
     *
     * @param name              定时器名称
     * @param r                 战火燎原定时任务
     * @param time              定时时间
     * @param globalWarFireOpen 战火燎原开放时间
     */
    private void addSchedule(String name, WarFireJob r, Date time, GlobalWarFire globalWarFireOpen) {
        Date now = new Date();
        if (now.before(time)) {
            ScheduleManager.getInstance().addOrModifyDefultJob(DefultJob.createDefult(name), job -> r.onRun(globalWarFireOpen, job), time);
            LogUtil.world("战火燎原定时器初始  name:", name, ", time:", DateHelper.formatDateMiniTime(time));
        } else {
            LogUtil.error("战火燎原定时器超时 name:", name, ", time:", DateHelper.formatDateMiniTime(time), ", now:", now, ", time:", time);
        }
    }

    /**
     * 加入定时器
     *
     * @param name              定时器名称
     * @param r                 战火燎原定时任务
     * @param beginDate         开始时间
     * @param endDate           结束时间
     * @param intervalInSeconds 间隔时间, 单位(秒)
     * @param globalWarFireOpen 战火燎原开放时间
     */
    private void addSchedule(String name, WarFireJob r, Date beginDate, Date endDate, int intervalInSeconds, GlobalWarFire globalWarFireOpen) {
        Date now = new Date();
        if (now.before(beginDate)) {
            ScheduleManager.getInstance().addOrModifyDefultJob(DefultJob.createDefult(name), job -> r.onRun(globalWarFireOpen, job), beginDate, endDate, intervalInSeconds);
            LogUtil.world("战火燎原定时器初始  name:", name, ", beginTime:", DateHelper.formatDateMiniTime(beginDate), ", endTime:", DateHelper.formatDateMiniTime(endDate));
        } else {
            LogUtil.error("战火燎原定时器超时 name:", name, ", beginTime:", DateHelper.formatDateMiniTime(beginDate), ", endTime:", DateHelper.formatDateMiniTime(endDate), ", now:", now);
        }
    }

    /**
     * 添加战火燎原城池定时器
     *
     * @param wfCityMapEntity 城池对象
     */
    public void addSchedule(WFCityMapEntity wfCityMapEntity) {
        int cityId = wfCityMapEntity.getCity().getCityId();
        // 定时器名称
        String scheduleName = WAR_FIRE_CITY_OCCUPY_JOB + cityId;
        // 定时器执行时间
        int occupyTime = wfCityMapEntity.getOccupyTime();
        // 添加到达占领时间的定时器
        addSchedule(scheduleName, this::onOccupyCity, occupyTime, wfCityMapEntity);
    }

    /**
     * 城池保护罩时间到期, 发送跑马灯
     *
     * @param wfCityMapEntity 战火燎原城池
     * @param defultJob       定时器任务
     */
    private void onProtectOver(WFCityMapEntity wfCityMapEntity, DefultJob defultJob) {
        City city = wfCityMapEntity.getCity();
        ChatDataManager chatDataManager = DataResource.ac.getBean(ChatDataManager.class);
        int cityId = city.getCityId();
        Optional.ofNullable(StaticCrossWorldDataMgr.getStaticWarFireMap().get(cityId))
                .ifPresent(sWF -> {
                    int protectChat = sWF.getProtectChat();
                    if (protectChat > 0) {
                        // 发送跑马灯
                        chatDataManager.sendSysChat(protectChat, CrossWorldMapConstant.CITY_AREA, 0, cityId);
                    }
                    StaticCity sCity = StaticCrossWorldDataMgr.getCityMap().get(cityId);
                    if (Objects.nonNull(sCity)) {
                        // 地图刷新
                        crossWorldMap.publishMapEvent(MapEvent.mapArea(sCity.getCityPos(), MapCurdEvent.UPDATE), MapEvent.mapEntity(sCity.getCityPos(), MapCurdEvent.UPDATE));
                    }
                });
    }

    /**
     * 城池到达占领时间, 发送跑马灯
     *
     * @param wfCity    战火燎原城池
     * @param defultJob 定时器任务
     */
    private void onOccupyCity(WFCityMapEntity wfCity, DefultJob defultJob) {
        City city = wfCity.getCity();
        ChatDataManager chatDataManager = DataResource.ac.getBean(ChatDataManager.class);
        int cityId = city.getCityId();
        int camp = city.getCamp();
        Optional.ofNullable(StaticCrossWorldDataMgr.getStaticWarFireMap().get(cityId))
                .ifPresent(sWF -> {
                    int occupyChat = sWF.getOccupyChat();
                    if (occupyChat > 0) {
                        // 占领城池需要发送跑马灯
                        chatDataManager.sendSysChat(occupyChat, CrossWorldMapConstant.CITY_AREA, 0, cityId, camp);
                    }
                    List<List<Integer>> buffs = sWF.getBuff();
                    if (!CheckNull.isEmpty(buffs)) {
                        if (buffs.stream().anyMatch(buff -> buff.get(0) == StaticWarFire.BUFF_TYPE_3 || buff.get(0) == StaticWarFire.BUFF_TYPE_4)) {
                            // 有给玩家加属性的buff
                            // 给占领阵营的玩家加buff
                            crossWorldMap.getPlayerMap().values().stream().map(PlayerMapEntity::getPlayer).filter(p -> p.lord.getCamp() == camp).forEach(CalculateUtil::reCalcAllHeroAttr);
                        }
                    }
                    //首杀积分奖励
                    if (wfCity.getFirstBloodCamp() == 0) {
                        cityProductFirstBloodScore(wfCity, sWF);
                    } else {
                        String _1stNames = wfCity.getFirstBloodRoleNames().size() > 0 ? String.join(", ", wfCity.getFirstBloodRoleNames()) : "";
                        LogUtil.world(String.format("战火燎原 --- 据点ID :%d, 首杀已经被阵营 :%d, 玩家 : %s  拿走了...", cityId, wfCity.getFirstBloodCamp(), _1stNames));
                    }
                });
    }

    /**
     * 添加战火燎原城池定时器
     *
     * @param name            定时器名称
     * @param r
     * @param time
     * @param wfCityMapEntity
     */
    private void addSchedule(String name, WarFireCityJob r, int time, WFCityMapEntity wfCityMapEntity) {
        int now = TimeHelper.getCurrentSecond();
        Date executeDate = TimeHelper.secondToDate(time);
        if (now < time) {
            ScheduleManager.getInstance().addOrModifyDefultJob(DefultJob.createDefult(name), job -> r.onRun(wfCityMapEntity, job), executeDate);
            LogUtil.world("战火燎原城池定时器初始  name:", name, ", executeDate:", DateHelper.formatDateMiniTime(executeDate));
        } else {
            LogUtil.error("战火燎原城池定时器超时 name:", name, ", executeDate:", DateHelper.formatDateMiniTime(executeDate), ", now:", now);
        }
    }

    /**
     * @param wfCity
     * @param staticWarFire
     */
    public void cityProductFirstBloodScore(WFCityMapEntity wfCity, StaticWarFire staticWarFire) {
        int campId = wfCity.getCity().getCamp(), cityId = staticWarFire.getId();
        wfCity.setLastScoreTime(TimeHelper.getCurrentSecond());
        wfCity.setFirstBloodCamp(campId);//记录首杀阵营ID
        CampWarFire wfCamp = campMap.get(campId);
        int campScore = staticWarFire.getCampFirst();
        addCampScore(wfCamp, campScore, AwardFrom.WAR_FIRE_SCORE_CITY_FIRST_BLOOD_CAMP, staticWarFire.getId());
        LogUtil.world(String.format("战火燎原 --- 阵营 :%d, 据点首次被占领 :%d, 获得首杀积分 :%d, 阵营总积分 :%d", campId, cityId, campScore, wfCamp.getScore()));
        List<AttackWFCityArmy> guardArms = wfCity.getArmyByCamp(Collections.singletonList(campId));
        if (CheckNull.isEmpty(guardArms)) {
            LogUtil.world(String.format("战火燎原 --- 据点ID :%d 首杀被阵营 :%d, 拿走, 但结算首杀时没有玩家驻防...", cityId, campId));
            return;
        }
        Map<Long, List<AttackWFCityArmy>> armyMap = guardArms.stream().collect(Collectors.groupingBy(AttackWFCityArmy::getLordId));
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        int playerFirstBloodScore = staticWarFire.getPersonFirst();
        armyMap.forEach((lordId, armys) -> {
            PlayerWarFire pwf = getPlayerWarFire(lordId);
            //给驻防玩家增加
            addPlayerScore(pwf, playerFirstBloodScore, AwardFrom.WAR_FIRE_SCORE_CITY_FIRST_BLOOD, cityId);
            pwf.setFirstOccupyScore(pwf.getFirstOccupyScore() + playerFirstBloodScore);
            LogUtil.world(String.format("战火燎原 --- 玩家 :%d, 据点 :%d, 获得首杀积分 :%d, 当前累计首杀积分 :%d", lordId, cityId, playerFirstBloodScore, pwf.getFirstOccupyScore()));
            //记录首杀奖励Log
            AttackWFCityArmy army = CheckNull.isEmpty(armys) ? null : armys.get(0);
            int heroId = army != null ? army.getArmy().getHero().get(0).getPrincipleHeroId() : 0;
            if (heroId > 0) {
                pwf.addWarFireEvent(WarFireEvent.ETY_CITY_FIRST_BLOOD, cityId, wfCity.getPos(), heroId, playerFirstBloodScore);
            }
            //记录首杀玩家名字列表
            Player player = playerDataManager.getPlayer(lordId);
            if (Objects.nonNull(player)) {
                wfCity.getFirstBloodRoleNames().add(player.lord.getNick());
            } else {
                LogUtil.world(String.format("战火燎原 ---  参与据点ID :%d 首杀的玩家 :%d 不存在!!!", cityId, lordId));
            }
        });
    }

    /**
     * 据点每分钟给占领阵营和驻防成员增加积分
     *
     * @param wfCity 据点数据
     */
    public void cityProductScore(WFCityMapEntity wfCity, int nowSec) {
        try {
            City city = wfCity.getCity();
            int cityId = city.getCityId();
            StaticWarFire staticWarFire = StaticCrossWorldDataMgr.getStaticWarFireMap().get(cityId);
            if (Objects.isNull(staticWarFire)) {
                LogUtil.error("战火燎原 --- 据点ID :% ", cityId, " 不存在...");
                return;
            }
            int campId = city.getCamp();
            CampWarFire wfCamp = campMap.get(campId);
            //给阵营加积分
            int addCampScore = staticWarFire.getCampContinue();
            addCampScore(wfCamp, addCampScore, AwardFrom.WAR_FIRE_SCORE_CITY_MINUTE_OUTPUT_CAMP, cityId);
            wfCity.setLastScoreTime(nowSec);
            LogUtil.world(String.format("战火燎原 --- 阵营 :%d, 占领据点 :%d, 持续产出积分 :%d, 阵营总积分 :%d", campId, cityId, addCampScore, wfCamp.getScore()));
            int addPlayerScore = staticWarFire.getPersonContinue();
            //给驻防部队加积分, 一个玩家多个部队驻防也只能获得一次资源
            List<AttackWFCityArmy> guardArms = wfCity.getArmyByCamp(Collections.singletonList(campId));
            if (!CheckNull.isEmpty(guardArms)) {
                Map<Long, List<AttackWFCityArmy>> armyMap = guardArms.stream()
                        .filter(wfcArmy -> nowSec >= wfcArmy.getArmy().getEndTime() && wfcArmy.getState() == ArmyConstant.ARMY_STATE_WAR_FIRE_CITY)
                        .collect(Collectors.groupingBy(AttackWFCityArmy::getLordId));
                armyMap.forEach((lordId, armys) -> {
                    //给驻防玩家增加
                    PlayerWarFire pwf = getPlayerWarFire(lordId);
                    addPlayerScore(pwf, addPlayerScore, AwardFrom.WAR_FIRE_SCORE_CITY_MINUTE_OUTPUT, cityId);
                    AttackWFCityArmy army = CheckNull.isEmpty(armys) ? null : armys.get(0);
                    int heroId = army != null ? army.getArmy().getHero().get(0).getPrincipleHeroId() : 0;
                    if (heroId > 0) {
                        pwf.addWarFireEvent(WarFireEvent.ETY_CITY_OUTPUT, cityId, wfCity.getPos(), heroId, staticWarFire.getPersonContinue());
                    }
                    LogUtil.world(String.format("战火燎原 --- 玩家 :%d, 据点 :%d, 持续产出积分 :%d, 总积分 :%d", lordId, cityId, addPlayerScore, pwf.getScore()));
                });
            }
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }


    /**
     * 给阵营增加积分
     *
     * @param cwf      阵营数据
     * @param addScore 积分增加数量
     * @param from     积分来源
     * @param params   据点ID
     */
    public void addCampScore(CampWarFire cwf, int addScore, AwardFrom from, Object params) {
        int oldScore = cwf.getScore();
        cwf.setScore(oldScore + addScore);
        updateCampScoreRank(cwf);
        int serverId = DataResource.ac.getBean(ServerSetting.class).getServerID();
        LogLordHelper.otherLog("warFireCampScore", from, serverId,
                cwf.getCamp(), oldScore, addScore, params);
    }

    private void updateCampScoreRank(CampWarFire cwf) {
        campRanks.update(new RankItem<>(cwf.getCamp(), cwf.getScore()));
    }

    /**
     * 增加玩家战火积分
     *
     * @param pwf      玩家战火数据
     * @param addScore 0:初始化玩家排名
     */
    public void addPlayerScore(PlayerWarFire pwf, int addScore, AwardFrom from, Object... params) {
        int oldScore = pwf.getScore();
        pwf.setScore(pwf.getScore() + addScore);
        PlayerDataManager playerDataMgr = DataResource.ac.getBean(PlayerDataManager.class);
        Player player = playerDataMgr.getPlayer(pwf.getRoleId());
        if (Objects.nonNull(player)) {
            updatePlayerScoreRank(player, pwf);
            LogLordHelper.warFireScore(from, player, oldScore, addScore, params);
        } else {
            LogUtil.error("玩家lordId :", pwf.getRoleId(), "不存在!!!");
        }

        //上报数数
        EventDataUp.credits(player.account, player.lord, pwf.getScore(), addScore, CreditsConstant.WARFIRE, from);

        //参与活动任务
        TaskService.handleTask(player, ETask.JOIN_ACTIVITY, FeatureCategory.WAR_FIRE.getCategory());
        ActivityDiaoChanService.completeTask(player, ETask.JOIN_ACTIVITY, FeatureCategory.WAR_FIRE.getCategory());
        TaskService.processTask(player, ETask.JOIN_ACTIVITY, FeatureCategory.WAR_FIRE.getCategory());
    }

    private void updatePlayerScoreRank(Player player, PlayerWarFire pwf) {
        //本阵营排名
        SimpleRank4SkipSet<Integer> campPlayerRanks = scoreRanks.get(player.getCamp());
        RankItem<Integer> rit = new RankItem<>(pwf.getRoleId(), pwf.getScore());
        campPlayerRanks.update(rit);
        //全服排名
        SimpleRank4SkipSet<Integer> allPlayerRanks = scoreRanks.get(Constant.Camp.NPC);
        allPlayerRanks.update(rit);
    }

    /**
     * 计算阵营在活动结束时的排名奖励
     *
     * @param finishCalcMap KEY:阵营ID, KEY1:据点类型, VALUE:数量
     * @return KEY:阵营ID, VALUE: 奖励铸铁币数量
     */
    private Map<Integer, StaticWarFireRankCamp> calcCampRankAwards(Map<Integer, CampWarFire.FinishCalc> finishCalcMap) {
        for (Map.Entry<Integer, CityMapEntity> entry : crossWorldMap.getCityMap().entrySet()) {
            City city = entry.getValue().getCity();
            int cityId = city.getCityId();
            StaticWarFire staticWarFire = StaticCrossWorldDataMgr.getStaticWarFireMap().get(cityId);
            if (staticWarFire == null) {
                LogUtil.error("战火燎原积分累加, 未找到配置信息 : cityId : ", cityId);
                continue;
            }
            if (city.getCamp() > 0) {
                CampWarFire wfc = campMap.get(city.getCamp());
                addCampScore(wfc, staticWarFire.getCampExtra(), AwardFrom.WAR_FIRE_SCORE_CITY_EXTRA_CAMP, cityId);
                //统计阵营占领据点情况
                CampWarFire.FinishCalc calc = finishCalcMap.computeIfAbsent(city.getCamp(), t -> new CampWarFire.FinishCalc());
                calc.tyCntMap.merge(staticWarFire.getType(), 1, Integer::sum);
                calc.extScore += staticWarFire.getCampExtra();
            }
        }
        //更新阵营积分排名
        Map<Integer, StaticWarFireRankCamp> campRankAwardMap = new HashMap<>();
        Map<Integer, StaticWarFireRankCamp> campRankAwards = StaticCrossWorldDataMgr.getStaticWarFireRankCampMap();
        Stream.iterate(1, i -> ++i).limit(campRankAwards.size()).forEach(i -> {
            RankItem<Integer> rit = campRanks.getRankItem(i);
            if (rit == null) {
                LogUtil.error("战火燎原结算阵营排名奖励, 第 ", i, " 名 没有阵营");
                return;
            }
            StaticWarFireRankCamp staticAward = campRankAwards.get(i);
            if (staticAward == null) {
                LogUtil.error("战火燎原结算阵营排名奖励, 第 ", i, " 名 没有配置奖励, 阵营 : ", rit.getLordId());
                return;
            }
            campRankAwardMap.put((int) rit.getLordId(), staticAward);
            LogUtil.world(String.format("战火燎原结算阵营排名奖励 第 :%d 名 所属阵营 :%d, 奖励铸铁币数量 :%d",
                    i, rit.getLordId(), staticAward.getAwardList().get(2)));
            //统计排名
            CampWarFire.FinishCalc calc = finishCalcMap.computeIfAbsent((int) rit.getLordId(), t -> new CampWarFire.FinishCalc());
            calc.rank = i;
        });
        return campRankAwardMap;
    }

    /**
     * 计算玩家积分对应的档位奖励
     *
     * @return KEY:roleId, Value: 铸铁币数量
     */
    private StaticWarFireRankGr calcPlayerScoreAwards(PlayerWarFire pwf) {
        TreeMap<Integer, StaticWarFireRankGr> grRankMap = StaticCrossWorldDataMgr.getStaticWarFireRankGrMap();
        NavigableMap<Integer, StaticWarFireRankGr> descMap = grRankMap.descendingMap();//个人积分奖励配表, 倒排序
        int score = pwf.getScore();
        StaticWarFireRankGr staticGrRank = descMap.values().stream().filter(t -> score >= t.getGr())
                .findFirst().orElse(null);
        if (Objects.nonNull(staticGrRank)) {
            LogUtil.world(String.format("战火燎原 --- 玩家 :%d, 积分 :%d, 获取积分奖励ID :%d, 所需积分 :%d", pwf.getRoleId(), score,
                    staticGrRank.getId(), staticGrRank.getGr()));
            return staticGrRank;
        } else {
            LogUtil.world(String.format("战火燎原 --- 玩家 :%d, 积分 :%d, 未能获得任何个人积分档位的积分奖励", pwf.getRoleId(), score));
        }
        return null;
    }

    /**
     * 累计玩家杀敌数
     *
     * @param pwf
     * @param addKilled
     */
    public void addKillCnt(PlayerWarFire pwf, int addKilled) {
        //杀敌数满1W才给计算积分
        int killedScoreBase = WorldConstant.WAR_FIRE_KILL_SCORE.get(0);
        int remain = pwf.getKilled() % killedScoreBase;//上次杀敌数计算积分时不足1W的余数
        if (remain + addKilled > killedScoreBase) {
            int addKilledScore = ((remain + addKilled) / killedScoreBase) * WorldConstant.WAR_FIRE_KILL_SCORE.get(1);
            addPlayerScore(pwf, addKilledScore, AwardFrom.WAR_FIRE_SCORE_BATTLE_KILL, pwf.getKilled(), addKilled);
            LogUtil.world(String.format("战火燎原 --- 玩家 :%d, 杀敌 [%d + %d] 获得积分 :%d", pwf.getRoleId(), addKilled, pwf.getKilled(), addKilledScore));
        }
        pwf.setKilled(pwf.getKilled() + addKilled);
    }

    /**
     * 记录战斗事件
     *
     * @param atkForce
     * @param defForce
     * @param baseWorldEntity
     */
    public void logWarFireFightEvent(Force atkForce, Force defForce, BaseWorldEntity baseWorldEntity) {
        try {
//            Force atkForce = atker.forces.get(0);
            PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
            Player atkPlayer = playerDataManager.getPlayer(atkForce.ownerId);
            PlayerWarFire atkWarfire = getPlayerWarFire(atkForce.ownerId);

//            Force defForce = defer.forces.get(0);
            Player defPlayer = playerDataManager.getPlayer(defForce.ownerId);
            PlayerWarFire defWarfire = getPlayerWarFire(defForce.ownerId);

            if (baseWorldEntity instanceof WFMineMapEntity) {
                WFMineMapEntity wfMine = (WFMineMapEntity) baseWorldEntity;
                //记录进攻事件
                atkWarfire.addWarFireEvent(WarFireEvent.ETY_MINE_ATTACKER, wfMine.getMineId(), wfMine.getPos(),
                        atkForce.id, atkForce.killed, atkForce.totalLost, defPlayer.lord.getNick(), defPlayer.lord.getPos(), defForce.id);
                //记录矿点防守事件
                defWarfire.addWarFireEvent(WarFireEvent.ETY_MINE_DEFENDER, wfMine.getMineId(), wfMine.getPos(),
                        defForce.id, defForce.killed, defForce.totalLost, atkPlayer.lord.getNick(), atkPlayer.lord.getPos(), atkForce.id);
            } else if (baseWorldEntity instanceof WFCityMapEntity) {
                WFCityMapEntity wfCity = (WFCityMapEntity) baseWorldEntity;
                int cityId = wfCity.getCity().getCityId();
                //记录进攻事件
                atkWarfire.addWarFireEvent(WarFireEvent.ETY_CITY_ATTACKER, cityId, wfCity.getPos(),
                        atkForce.id, defForce.totalLost, defForce.killed, defPlayer.lord.getNick(), defPlayer.lord.getPos(), defForce.id);
                //记录矿点防守事件
                defWarfire.addWarFireEvent(WarFireEvent.ETY_CITY_DEFENDER, cityId, wfCity.getPos(),
                        defForce.id, defForce.killed, defForce.totalLost, atkPlayer.lord.getNick(), atkPlayer.lord.getPos(), atkForce.id);
            }
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

    public void gmClearWarfireEvents(Player player) {
        PlayerWarFire pwf = getPlayerWarFire(player.getLordId());
        pwf.getEvents().clear();
    }

    public void attackPlayerBattleFinish(Fighter atker, Fighter defer, Battle battle, BaseWorldEntity baseWorldEntity) {
        if (getStage() == GlobalWarFire.STAGE_RUNNING) {
            //进攻记录
            Map<Long, Turple<Integer, Integer>> atkCalcMap = WarFireUtil.calcKillAndLost(atker);
            for (Map.Entry<Long, Turple<Integer, Integer>> entry : atkCalcMap.entrySet()) {
                Turple<Integer, Integer> turple = entry.getValue();
                PlayerWarFire atkWarfire = getPlayerWarFire(entry.getKey());
                addKillCnt(atkWarfire, turple.getA());
                atkWarfire.addWarFireEvent(WarFireEvent.ETY_PLAYER_ATTACKER, battle.getDefencer().lord.getNick(), baseWorldEntity.getPos(), turple.getA(), turple.getB());
            }
            //防守统计
            Map<Long, Turple<Integer, Integer>> defCalcMap = WarFireUtil.calcKillAndLost(defer);
            for (Map.Entry<Long, Turple<Integer, Integer>> entry : defCalcMap.entrySet()) {
                Turple<Integer, Integer> turple = entry.getValue();
                PlayerWarFire defWarfire = getPlayerWarFire(entry.getKey());
                addKillCnt(defWarfire, turple.getA());
                defWarfire.addWarFireEvent(WarFireEvent.ETY_PLAYER_DEFENDER, battle.getSponsor().lord.getNick(), battle.getSponsor().lord.getPos(), turple.getA(), turple.getB());
            }
        }
    }

    /**
     * 战火燎原定时任务接口
     */
    private interface WarFireJob {

        void onRun(GlobalWarFire globalWarFireOpen, DefultJob job);
    }

    private interface WarFireCityJob {

        void onRun(WFCityMapEntity cityMapEntity, DefultJob job);

    }

    public Date getDisPlayerDate() {
        return disPlayerDate;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getEndDisPlayerDate() {
        return endDisPlayerDate;
    }

    public CrossWorldMap getCrossWorldMap() {
        return crossWorldMap;
    }

    public SimpleRank4SkipSet<Integer> getCampRanks() {
        return campRanks;
    }

    public Map<Integer, SimpleRank4SkipSet<Integer>> getScoreRanks() {
        return scoreRanks;
    }

    public Map<Integer, CampWarFire> getCampMap() {
        return campMap;
    }

    public Map<Long, PlayerWarFire> getPlayerWarFireMap() {
        return playerWarFireMap;
    }
}
