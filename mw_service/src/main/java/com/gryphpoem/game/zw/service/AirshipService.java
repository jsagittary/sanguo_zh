package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.dataMgr.StaticNpcDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.gameplay.local.service.CrossAirshipService;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.CommonPb.BattleRole;
import com.gryphpoem.game.zw.pb.GamePb4.AttackAirshipRq;
import com.gryphpoem.game.zw.pb.GamePb4.AttackAirshipRs;
import com.gryphpoem.game.zw.pb.GamePb4.GetAirshipListRq;
import com.gryphpoem.game.zw.pb.GamePb4.GetAirshipListRs;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.AbsDailyClear;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.fight.NpcForce;
import com.gryphpoem.game.zw.resource.pojo.global.GlobalSchedule;
import com.gryphpoem.game.zw.resource.pojo.global.WorldSchedule;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.world.AirshipPersonData;
import com.gryphpoem.game.zw.resource.pojo.world.AirshipWorldData;
import com.gryphpoem.game.zw.resource.pojo.world.Area;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.service.activity.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author QiuKun
 * @ClassName AirshipService.java
 * @Description 飞艇相关逻辑
 * @date 2019年1月16日
 */
@Component
public class AirshipService {

    @Autowired
    private WorldDataManager worldDataManager;
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private WorldService worldService;
    @Autowired
    private ArmyService armyService;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private ChatDataManager chatDataManager;
    @Autowired
    private WorldScheduleService worldScheduleService;
    @Autowired
    private CrossAirshipService crossAirshipService;
    @Autowired
    private GlobalDataManager globalDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private PropService propService;
    @Autowired
    private CampService campService;


    // 飞艇活动结束时间
    // private int airshipFinishTime;
    // 飞艇活动开始时间
    // private int airshipStartTime;


    /**
     * 获取飞艇列表
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GetAirshipListRs getAirshipList(long roleId, GetAirshipListRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (player.lord.getArea() > WorldConstant.AREA_MAX_ID) { // 新地图使用
            return crossAirshipService.getAirshipList(player, req);
        }
        List<Integer> areaIdList = req.getAreaIdList(); // 区域列表
        if (areaIdList.size() > MapHelper.AREA_MAP.length) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "区域size 数据异常, roleId:", roleId, ", size:",
                    areaIdList.size());
        }
        int airshipKeyId = req.getAirshipKeyId(); // 具体的飞艇keyId
        GetAirshipListRs.Builder builder = GetAirshipListRs.newBuilder();
        // 列表
        if (!CheckNull.isEmpty(areaIdList)) {
            List<AirshipWorldData> allAirshipWorldData = worldDataManager.getAllAirshipWorldData();
            allAirshipWorldData.stream().filter(aswd -> areaIdList.contains(aswd.getAreaId()))
                    .forEach(aswd -> builder.addAirshipList(
                            PbHelper.createAirshipShowClientPb(aswd, player.lord.getCamp(), false, playerDataManager)));
        }
        // 详情
        if (airshipKeyId > 0) {
            List<AirshipWorldData> allAirshipWorldData = worldDataManager.getAllAirshipWorldData();
            allAirshipWorldData.stream()
                    .filter(aswd -> aswd.getKeyId() == airshipKeyId)
                    .findFirst()
                    .ifPresent(airshipWorldData -> builder.setAirshipDetail(PbHelper.createAirshipShowClientPb(airshipWorldData, player.lord.getCamp(), true, playerDataManager)));
        }
        AirshipPersonData airshipPersonData = player.getAndCreateAirshipPersonData();
        // airshipPersonData.refresh();
        builder.setKillAwardCnt(airshipPersonData.getKillAwardCnt());
        builder.setAttendAwardCnt(airshipPersonData.getAttendAwardCnt());
        builder.setAreaKillCnt(getKill(player.lord.getArea(), player.lord.getCamp()));
        return builder.build();
    }

    /**
     * 攻击飞艇
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public AttackAirshipRs attackAirship(long roleId, AttackAirshipRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int pos = req.getPos();
        List<Integer> heroIdList = req.getHeroIdList().stream().distinct().collect(Collectors.toList()); // 派出去的将领 + 去重
        if (!worldDataManager.isAirshipWorldData(pos)) {
            throw new MwException(GameError.AIRSHIP_NOT_EXIST.getCode(), "飞艇不存在, roleId:", roleId, ", pos:", pos);
        }
        AirshipWorldData airshipWorldData = worldDataManager.getAirshipWorldDataMap().get(pos);
        if (CheckNull.isNull(airshipWorldData) || !airshipWorldData.isLiveStatus()) {
            throw new MwException(GameError.AIRSHIP_NOT_EXIST.getCode(), "飞艇不存在或者不是存活状态, roleId:", roleId, ", pos:", pos);
        }
        // worldService.checkSameArea(player, pos); // 跨区域判断

        boolean isAcceSpeed = false;
        StaticArea targetSArea = StaticWorldDataMgr.getAreaMap().get(MapHelper.getAreaIdByPos(pos));
        StaticArea mySArea = StaticWorldDataMgr.getAreaMap().get(player.lord.getArea());
        if (mySArea.getOpenOrder() == WorldConstant.AREA_ORDER_3) { // 自己在皇城区域,任何地方都可以打
            if (targetSArea.getOpenOrder() != WorldConstant.AREA_ORDER_3) {
                isAcceSpeed = true;
            }
        } else if (mySArea.getOpenOrder() == WorldConstant.AREA_ORDER_2) {// 自己在州的情况,州只能打州
            if (targetSArea.getOpenOrder() != WorldConstant.AREA_ORDER_2) {
                throw new MwException(GameError.CAMP_BATTLE_AREA_ERROR.getCode(), "跨区域打飞艇, roleId:", roleId,
                        ", my area:", mySArea.getArea(), ", target area:", targetSArea.getArea());
            }
        } else if (mySArea.getOpenOrder() == WorldConstant.AREA_ORDER_1) { // 自己在郡只能打本区域的
            if (targetSArea.getArea() != mySArea.getArea()) {
                throw new MwException(GameError.CAMP_BATTLE_AREA_ERROR.getCode(), "跨区域打飞艇, roleId:", roleId,
                        ", my area:", mySArea.getArea(), ", target area:", targetSArea.getArea());
            }
        }
        checkHeroState(player, heroIdList);// 将领状态检测


        // worldService.removeProTect(player); // 移除罩子 2020-07-21 雷同学说不破罩子
        int now = TimeHelper.getCurrentSecond();
        // 部队类型
        Army army = armyService.checkAndcreateArmy(player, pos, heroIdList, now, ArmyConstant.ARMY_TYPE_ATTACK_AIRSHIP);
        army.setTargetId(airshipWorldData.getKeyId());
        // 皇城跨区加速
        if (isAcceSpeed) {
            int duration = 150; // 2分半
            army.setDuration(duration);
            army.setEndTime(now + duration);
        }

        if (army.getEndTime() >= airshipWorldData.getTriggerTime()) {
            throw new MwException(GameError.ATK_MAX_TIME.getCode(), "时间太长，不能使用改类型战斗, roleId:", player.lord.getLordId());
        }

        armyService.addMarchAndChangeHeroState(player, army, pos); // 添加行军线
        AttackAirshipRs.Builder builder = AttackAirshipRs.newBuilder();
        builder.setArmy(PbHelper.createArmyPb(army, false));
        return builder.build();
    }

    /**
     * 同步飞艇信息
     */
    // private void syncAirshipInfo() {
    //     Map<Integer, Integer> airShipData = globalDataManager.getGameGlobal().getMixtureDataById(GlobalConstant.AIR_SHIP_CUR_OPEN_TIME);
    //     Base msg = PbHelper.createSynBase(SyncAirshipInfoRs.EXT_FIELD_NUMBER, SyncAirshipInfoRs.ext,
    //             SyncAirshipInfoRs.newBuilder().setStartTime(airShipData.getOrDefault(1, 0)).setFinishTime(airShipData.getOrDefault(2, 0)).build())
    //             .build();
    //     playerDataManager.getAllOnlinePlayer().values().stream().filter(p -> p.ctx != null).forEach(p -> MsgDataManager.getIns().add(new Msg(p.ctx, msg, p.roleId)));
    // }

    /**
     * 主动返回部队
     *
     * @param player
     * @param army
     * @param type
     */
    public void retreatArmy(Player player, Army army, int type) {
        int now = TimeHelper.getCurrentSecond();
        int pos = army.getTarget();
        AirshipWorldData airshipWorldData = worldDataManager.getAirshipWorldDataMap().get(pos);
        if (airshipWorldData == null) {
            worldService.retreatArmy(player, army, now, type);
            return;
        }
        if (army.getState() == ArmyConstant.ARMY_STATE_ATTACK_AIRSHIP_WAIT) {
            // 移除掉飞艇中的部队信息
            List<BattleRole> roleList = airshipWorldData.getJoinRoles().get(player.lord.getCamp());
            if (!CheckNull.isEmpty(roleList)) {
                // 推送满足条件的集结
                campService.syncCancelRallyBattle(player, null, airshipWorldData);
                roleList.removeIf(rb -> rb.getRoleId() == player.roleId && rb.getKeyId() == army.getKeyId());
            }
        }
        worldService.retreatArmy(player, army, now, type);
    }

    /**
     * 检测将领状态
     *
     * @param player
     * @param heroIdList
     */
    private void checkHeroState(Player player, List<Integer> heroIdList) throws MwException {
        long roleId = player.roleId;
        for (Integer heroId : heroIdList) {
            Hero hero = player.heros.get(heroId);
            if (null == hero) {
                throw new MwException(GameError.HERO_NOT_FOUND.getCode(), "玩家没有这个将领, roleId:", roleId, ", heroId:",
                        heroId);
            }
            if (!player.isOnBattleHero(heroId) && !player.isOnCommandoHero(heroId)) {
                throw new MwException(GameError.HERO_NOT_BATTLE.getCode(), "将领未上阵 roleId:", roleId, ", heroId:",
                        heroId);
            }
            if (!hero.isIdle()) {
                throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "将领不在空闲中, roleId:", roleId, ", heroId:",
                        heroId, ", state:", hero.getState());
            }
            if (hero.getCount() <= 0) {
                throw new MwException(GameError.HERO_NO_ARM.getCode(), "将领没有带兵, roleId:", roleId, ", heroId:", heroId,
                        ", count:", hero.getCount());
            }
        }
    }

    /**
     * 世界任务条件
     *
     * @return true满足
     */
    private boolean worldTaskCond() {
        // 解锁的世界进程阶段
        int openScheduleId = Constant.AIRSHIP_WORLDTASKID_CFG;
        GlobalSchedule globalSchedule = worldScheduleService.getGlobalSchedule();
        int currentScheduleId = globalSchedule.getCurrentScheduleId();
        if (currentScheduleId >= openScheduleId) {
            WorldSchedule worldSchedule = globalSchedule.getWorldSchedule(openScheduleId);
            if (worldSchedule != null) {
                if (worldSchedule.getStatus() == ScheduleConstant.SCHEDULE_STATUS_FINISH) {
                    // 配置的阶段已经过了, 直接解锁
                    return true;
                } else if (worldSchedule.getStatus() == ScheduleConstant.SCHEDULE_STATUS_PROGRESS) {
                    // 配置的阶段正在进行, 开启的第二天解锁
                    int startTime = worldSchedule.getStartTime();
                    int dayiy = DateHelper.dayiy(TimeHelper.secondToDate(startTime), new Date());
                    return dayiy > 1;
                }
            }
        }
        return false;
    }

    /**
     * 初始化飞艇下次开放时间
     */
    private void checkAndinitAirNextOpenTime() {
        // 当前的时间
        Date today = new Date();
        Map<Integer, Integer> nextOpenMap = globalDataManager.getGameGlobal().getMixtureDataById(GlobalConstant.AIR_SHIP_NEXT_OPEN_TIME);
        // 下次开放时间
        int nextOpenTime = nextOpenMap.getOrDefault(0, 0);
        Date curOpen = TimeHelper.secondToDate(nextOpenTime);
        // 首次开放, 或者已经过了下次要开放的时间
        if (0 == nextOpenTime || DateHelper.isAfterTime(today, curOpen)) {
            // 获取下次开放的时间
            Integer nextOpen = getNextOpenDate(today);
            if (nextOpen > 0) {
                nextOpenMap.put(0, nextOpen);
                globalDataManager.getGameGlobal().setMixtureData(GlobalConstant.AIR_SHIP_NEXT_OPEN_TIME, nextOpenMap);
                LogUtil.debug("------飞艇下次开启的时间：", DateHelper.formatDateMiniTime(TimeHelper.secondToDate(nextOpen)), "-------");
            }
        }
    }

    /**
     * 获取下次开放时间
     *
     * @param openDate 开放的Date
     * @return 下次开放的time
     */
    private Integer getNextOpenDate(Date openDate) {
        List<List<Integer>> timeCfg = Constant.AIRSHIP_TIME_CFG;
        if (CheckNull.isEmpty(timeCfg)) {
            LogUtil.error("飞艇开启的时间未配置, systemId:", SystemId.AIRSHIP_TIME_CFG);
            return 0;
        }
        // 获取开放的星期
        int week = TimeHelper.getCNDayOfWeek(openDate);
        if (week == 5) {
            // 如果今天是周五, 就获取周六的时间
            return getNextOpenDate(TimeHelper.getDayOfWeekByDate(openDate, DateHelper.SATURDAY));
        }
        // 过滤当前还未开放的配置
        List<Integer> matchConf = getMatchConf(openDate, timeCfg);
        if (CheckNull.isEmpty(matchConf)) {
            // 如果开放日期的开启时间都过了
            return getNextOpenDate(DateHelper.afterDayTimeDate(openDate, 2));
        } else {
            return getStartTime(matchConf, openDate);
        }
    }

    /**
     * 过滤当前还未开放的配置
     *
     * @param openDate 开放时间
     * @param timeCfg  开放配置
     * @return 还未到的配置
     */
    private List<Integer> getMatchConf(Date openDate, List<List<Integer>> timeCfg) {
        return timeCfg
                .stream()
                // 过滤今天还没到开启时间的配置
                .filter(conf -> TimeHelper.getCurrentSecond() < getStartTime(conf, openDate))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取配置时间
     *
     * @param timeCfg  配置[开启的小时(小时: 12), 持续的时间(秒: 3600)]
     * @param openDate 日期
     * @return 开启时间
     */
    private int getStartTime(List<Integer> timeCfg, Date openDate) {
        if (CheckNull.isEmpty(timeCfg)) {
            return 0;
        }
        final int startHour = timeCfg.get(0); // 开始时间
        final Date startDate = TimeHelper.getSomeDayAfterOrBerfore(openDate, 0, startHour, 0, 0);
        return (int) (startDate.getTime() / TimeHelper.SECOND_MS);
    }

    /**
     * 首次开放
     */
    // @Deprecated
    // private boolean firstOpenAirShip() {
    //     Map<Integer, Integer> nextOpenMap = globalDataManager.getGameGlobal().getMixtureDataById(GlobalConstant.AIR_SHIP_NEXT_OPEN_TIME);
    //     // 没有下一次的开启时间
    //     if (nextOpenMap.getOrDefault(0, 0) == 0) {
    //         // 当前的时间
    //         Date today = new Date();
    //         // 开启时间
    //         Date openTimeDate = TimeHelper.getDayOfWeekByDate(today, DateHelper.SATURDAY);
    //         if (today.after(openTimeDate)) {
    //             openTimeDate = TimeHelper.getSomeDayAfterOrBerfore(openTimeDate, 7, 0, 0, 0);
    //         }
    //         LogUtil.debug("------飞艇首次开放时间：", DateHelper.formatDateMiniTime(openTimeDate), "-------");
    //         // 首次开放的时间
    //         int value = TimeHelper.dateToSecond(openTimeDate);
    //         nextOpenMap.put(0, value);
    //         globalDataManager.getGameGlobal().setMixtureData(GlobalConstant.AIR_SHIP_NEXT_OPEN_TIME, nextOpenMap);
    //         return true;
    //     }
    //     return false;
    // }

    /**
     * 是否在活动期间内
     *
     * @return true在活动期间内
     */
    // public boolean isInAirshipDurationTime(int now) {
    //     Map<Integer, Integer> airShipData = globalDataManager.getGameGlobal().getMixtureDataById(GlobalConstant.AIR_SHIP_CUR_OPEN_TIME);
    //     return now >= airShipData.getOrDefault(1, 0) && now < airShipData.getOrDefault(2, 0);
    // }

    /**
     * 飞艇活动开始
     */
    // private void onStartAirship() {
    //     LogUtil.debug("-----------飞艇活动开始-----------");
    //     refreshAirship(); // 老地图飞艇
    //     crossAirshipService.refreshAirship();
    //     syncAirshipInfo();// 推送
    // }

    /**
     * 飞艇活动结束
     */
    // private void onEndAirShip() {
    //     int now = TimeHelper.getCurrentSecond();
    //     try {
    //         List<AirshipWorldData> allAirshipWorldData = worldDataManager.getAllAirshipWorldData();
    //         if (!CheckNull.isEmpty(allAirshipWorldData)) {
    //             for (AirshipWorldData aswd : allAirshipWorldData) {
    //                 airshipRunAway(aswd, now);
    //                 allAirshipWorldData.remove(aswd);// 彻底移除飞艇
    //             }
    //         }
    //         // TODO: 2020/7/10 触发下一轮定时器
    //         triggerInitAirship();
    //     } catch (Exception e) {
    //         LogUtil.error("飞艇活动结束定时器报错", e);
    //     }
    //     LogUtil.debug("-----------飞艇活动结束-----------");
    // }

    /**
     * 根据块获取飞艇
     *
     * @param block
     * @return
     */
    public List<AirshipWorldData> getAirshipByBlock(int block) {
        // int now = TimeHelper.getCurrentSecond();
        // if (!worldTaskCond()) {
        //     return null;
        // }
        Map<Integer, AirshipWorldData> airshipWorldDataMap = worldDataManager.getAirshipWorldDataMap();
        if (CheckNull.isEmpty(airshipWorldDataMap)) {
            return null;
        }
        return airshipWorldDataMap.values()
                .stream()
                .filter(aswd -> block == MapHelper.block(aswd.getPos()) && aswd.isLiveStatus())
                .collect(Collectors.toList());
    }

    /**
     * 找到可用点
     *
     * @param areaId
     * @param blockList
     * @param needCnt
     * @return
     */
    private List<Integer> findEmptyPos(int areaId, List<Integer> blockList, int needCnt) {
        if (needCnt < 1 || CheckNull.isEmpty(blockList)) {
            LogUtil.error("错误参数 needPos<1 或block为空 areaId:", areaId);
            return null;
        }
        int cnt = 0;
        final int blockSize = blockList.size();
        List<Integer> emptyPosList = new ArrayList<>(needCnt);
        while (emptyPosList.size() < needCnt && cnt++ < 100) {
            int index = RandomHelper.randomInSize(blockSize);
            int b = blockList.get(index);
            List<Integer> posList = worldDataManager.randomEmptyByAreaBlock(areaId, b, 1);
            if (!CheckNull.isEmpty(posList)) {
                emptyPosList.add(posList.get(0));
            }
        }
        return emptyPosList;
    }

    /**
     * 根据配置飞艇列表
     *
     * @param sAirshipList
     * @return
     */
    public static List<AirshipWorldData> createAirshipWorldDataList(List<List<Integer>> sAirshipList, int areaId) {
        List<AirshipWorldData> aswdList = new ArrayList<>();
        for (List<Integer> idCnt : sAirshipList) {
            if (!CheckNull.isEmpty(idCnt) && idCnt.size() > 1) {
                int id = idCnt.get(0);
                int cnt = idCnt.get(1);
                for (int i = 0; i < cnt; i++) {
                    AirshipWorldData airshipWorldData = createAirshipWorldData(id, areaId);
                    if (Objects.isNull(airshipWorldData)) {
                        continue;
                    }
                    aswdList.add(airshipWorldData);
                }
            }
        }
        return aswdList;
    }

    /**
     * 创建单个飞艇,不代坐标点
     *
     * @param id     飞艇的id
     * @param areaId 数量
     * @return
     */
    public static AirshipWorldData createAirshipWorldData(int id, int areaId) {
        StaticAirship sAirship = StaticWorldDataMgr.getAirshipMap().get(id);
        if (sAirship == null) {
            LogUtil.error("飞艇配置未找到 id:", id);
            return null;
        }
        AirshipWorldData aswd = new AirshipWorldData(id);
        aswd.setStatus(AirshipWorldData.STATUS_LIVE);
        aswd.setTriggerTime(TimeHelper.getCurrentSecond() + sAirship.getLiveTime());
        aswd.setAreaId(areaId);
        addNpcForce(aswd, sAirship.getForm());// 添加npc
        return aswd;
    }

    /**
     * 给飞艇添加npc
     *
     * @param aswd
     * @param form
     */
    public static void addNpcForce(AirshipWorldData aswd, List<Integer> form) {
        if (aswd != null && !CheckNull.isEmpty(form)) {
            aswd.getNpc().clear(); // 先清除原来的npc
            for (Integer npcId : form) {
                StaticNpc staticNpc = StaticNpcDataMgr.getNpcMap().get(npcId);
                if (staticNpc != null) {
                    int hp = staticNpc.getAttr().getOrDefault(Constant.AttrId.LEAD, 1);
                    NpcForce npcForce = new NpcForce(staticNpc.getNpcId(), hp, 0);
                    aswd.getNpc().add(npcForce);
                }
            }
        }
    }

    /**
     * 检测区域是否开放
     *
     * @param staticArea 区域
     * @return true 开放了
     */
    private boolean checkAreaIsOpen(StaticArea staticArea) {
        final int areaId = staticArea.getArea();
        Area areaData = worldDataManager.getAreaMap().get(areaId);
        if (staticArea.getOpenOrder() == WorldConstant.AREA_ORDER_1) {
            return areaData.isOpen();
        } else {
            return areaData.canPass();
        }

    }

    /**
     * 跑秒定时器会执行
     */
    public void runSecTimer() {
        try {
            List<AirshipWorldData> allAirshipWorldData = worldDataManager.getAllAirshipWorldData();
            if (!CheckNull.isEmpty(allAirshipWorldData)) {
                int now = TimeHelper.getCurrentSecond();
                Map<Integer, List<AirshipWorldData>> areaAirShip = allAirshipWorldData.stream().collect(Collectors.groupingBy(AirshipWorldData::getAreaId));
                if (CheckNull.isEmpty(areaAirShip)) {
                    return;
                }
                // 每个区域, 只会刷新一个, 解决地图和跑马灯疯狂推送的问题
                areaAirShip.forEach((areaId, curAreaAirs) -> {
                    // 过滤到了刷新时间, 取刷新时间最早的
                    AirshipWorldData aswd = curAreaAirs.stream().filter(airshipWorldData -> airshipWorldData.getTriggerTime() < now).min(Comparator.comparingInt(AirshipWorldData::getTriggerTime)).orElse(null);
                    // 非空判断
                    if (CheckNull.isNull(aswd)) {
                        return;
                    }
                    // 处理刷新逻辑
                    processRefreshAirship(aswd, now);
                });
            } else {
                // 说明首次开放飞艇, 还没有飞艇的数据
                refreshAirship();
            }
            // }
        } catch (Exception e) {
            LogUtil.error("飞艇跑秒定时器报错 ", e);
        }
    }

    /**
     * 修改飞艇的玩家归属
     *
     * @param originRoleId
     * @param destRoleId
     */
    public void changAirshipBelong(long originRoleId, long destRoleId) {
        // if (worldTaskCond()) {
        // List<AirshipWorldData> allAirshipWorldData = worldDataManager.getAllAirshipWorldData();
        // if (!CheckNull.isEmpty(allAirshipWorldData)) {
        // Player op = playerDataManager.getPlayer(originRoleId);
        // Player sp = playerDataManager.getPlayer(destRoleId);
        // Turple<Integer, Integer> spXy = MapHelper.reducePos(sp.lord.getPos());
        // List<Integer> emptyPos = null;
        // for (AirshipWorldData aswd : allAirshipWorldData) {
        // if (aswd.getBelongRoleId() == originRoleId) {
        // aswd.setBelongRoleId(destRoleId);
        // int aswdPos = aswd.getPos();
        // Turple<Integer, Integer> aswdXy = MapHelper.reducePos(aswdPos);
        // if (op != null && sp != null) {
        // int now = TimeHelper.getCurrentSecond();
        // mailDataManager.sendNormalMail(sp, MailConstant.MOLD_AIRSHIP_FIGHT_BELONG, now,
        // aswd.getId(), aswd.getId(), op.lord.getNick(), aswdPos);
        // mailDataManager.sendNormalMail(op, MailConstant.MOLD_AIR_SHIP_LOST_BELONG, now,
        // sp.lord.getCamp(), sp.lord.getNick(), aswd.getId(), aswdXy.getA(), aswdXy.getB(),
        // sp.lord.getCamp(), sp.lord.getNick(), spXy.getA(), spXy.getB());
        // }
        // if (emptyPos == null) {
        // emptyPos = new ArrayList<>(2);
        // }
        // emptyPos.add(aswdPos);
        // }
        // }
        // if (!CheckNull.isEmpty(emptyPos)) {
        // EventBus.getDefault()
        // .post(new Events.AreaChangeNoticeEvent(emptyPos, Events.AreaChangeNoticeEvent.MAP_TYPE));
        // }
        // }
        // }
    }

    /**
     * 获取多人判断行军加成
     *
     * @param player    玩家
     * @param marchTime 行军时间
     * @return
     */
    public int getAirShipMarchTime(Player player, int marchTime) {
        // 多人叛军行军加成[%]
        List<StaticAirShipBuff> areaBuff = getAreaBuff(player);
        if (!CheckNull.isEmpty(areaBuff)) {
            for (StaticAirShipBuff shipBuff : areaBuff) {
                if (shipBuff.getType() == EffectConstant.AIRSHIP_WALK_SPEED) {
                    marchTime = (int) (marchTime * (1 - (shipBuff.getBuffVal() / Constant.TEN_THROUSAND)));
                }
            }
        }
        return marchTime;
    }

    /**
     * 获取玩家当前区域的buff
     *
     * @param player 玩家对象
     * @return 当前区域生效的buff
     */
    private List<StaticAirShipBuff> getAreaBuff(Player player) {
        int area = player.lord.getArea();
        Integer killNum = getKill(area, player.lord.getCamp());
        StaticArea staticArea = StaticWorldDataMgr.getAreaMap().get(area);
        if (!CheckNull.isNull(staticArea)) {
            List<StaticAirshipArea> areaOrderMap = StaticWorldDataMgr.getAirshipAreaOrderMap(staticArea.getOpenOrder());
            if (!CheckNull.isEmpty(areaOrderMap)) {
                StaticAirshipArea sAirArea = areaOrderMap.get(0);
                if (!CheckNull.isNull(sAirArea)) {
                    // 杀敌数量大于配置数量
                    if (killNum >= sAirArea.getNum()) {
                        List<Integer> effect = sAirArea.getEffect();
                        if (!CheckNull.isEmpty(effect)) {
                            return effect.stream().map(StaticWorldDataMgr::getAirShipBuffById).collect(Collectors.toList());
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 获取当前杀敌数量
     *
     * @param area 区域
     * @return 杀敌数量
     */
    public Integer getKill(int area, int camp) {
        Map<Integer, Integer> curAreaKill = globalDataManager.getGameGlobal().getMixtureDataById(GlobalConstant.AIR_SHIP_CUR_AREA_KILL_NUM, camp);
        return curAreaKill.getOrDefault(area, 0);
    }

    /**
     * 自增并获取杀敌数量
     *
     * @param area 区域
     * @return 自增后的杀敌数量
     */
    public Integer incrementAndGetKill(int area, int camp) {
        Map<Integer, Integer> curAreaKill = globalDataManager.getGameGlobal().getMixtureDataById(GlobalConstant.AIR_SHIP_CUR_AREA_KILL_NUM, camp);
        Integer killNum = curAreaKill.getOrDefault(area, 0);
        int incrementNum = killNum + 1;
        curAreaKill.put(area, incrementNum);
        globalDataManager.getGameGlobal().setMixtureData(GlobalConstant.AIR_SHIP_CUR_AREA_KILL_NUM + camp, curAreaKill);
        return incrementNum;
    }

    // /**
    //  * 给区域内的玩家发放Buffer
    //  *
    //  * @param sAirArea 飞艇刷新规则
    //  * @param area     区域
    //  */
    // @Deprecated
    // public void updateBuff(StaticAirshipArea sAirArea, int area) {
    //     List<Integer> effectList = sAirArea.getEffect();
    //     if (CheckNull.isEmpty(effectList)) {
    //         return;
    //     }
    //     // buff配置
    //     Map<Integer, StaticAirShipBuff> airShipBuffMap = effectList
    //             .stream()
    //             .map(StaticWorldDataMgr::getAirShipBuffById)
    //             .collect(Collectors.toMap(StaticAirShipBuff::getBuffId, conf -> conf, (oldV, newV) -> newV));
    //     // 当前区域所有玩家获得buff加成
    //     playerDataManager.getPlayerByArea(area)
    //             .values()
    //             .forEach(p -> {
    //                 if (CheckNull.isNull(p)) {
    //                     return;
    //                 }
    //                 airShipBuffMap.forEach((buffId, sasb) -> {
    //                     int effectType = sasb.getType();
    //                     int effectVal = sasb.getBuffVal();
    //                     int time = sasb.getTime();
    //                     Effect effect = new Effect(effectType, effectVal, time);
    //                     p.getEffect().put(effectType, effect);
    //                     // 同步effect
    //                     propService.syncBuffRs(p, effect);
    //                 });
    //                 CalculateUtil.reCalcBattleHeroAttr(p);
    //             });
    // }

    /**
     * gm强制刷新飞艇
     */
    public void gmRefreshAirship() {
        int now = TimeHelper.getCurrentSecond();
        // 先撤回部队
        worldDataManager.getAllAirshipWorldData().forEach(aswd -> returnArmyAuto(aswd, now, null));
        // 先清空
        worldDataManager.getAirshipWorldDataMap().clear();
        worldDataManager.getAllAirshipWorldData().clear();
        refreshAirship();
    }

    /**
     * 飞艇刷新
     */
    public void refreshAirship() {
        // int now = TimeHelper.getCurrentSecond();
        // 先撤回部队
        // worldDataManager.getAllAirshipWorldData().forEach(aswd -> returnArmyAuto(aswd, now, null));
        // 先清空
        // worldDataManager.getAirshipWorldDataMap().clear();
        // worldDataManager.getAllAirshipWorldData().clear();
        // 刷飞艇
        StaticWorldDataMgr.getAreaMap().values()
                .stream()
                .filter(StaticArea::isOpen)
                .forEach(sa -> {
                    // TODO: 2020/7/14 暂时给所有的区域刷新飞艇
                    // if (checkAreaIsOpen(sa)) { // 区域已经开放才会进入
                    List<StaticAirshipArea> areaOrderMap = StaticWorldDataMgr.getAirshipAreaOrderMap(sa.getOpenOrder());
                    if (!CheckNull.isEmpty(areaOrderMap)) {
                        areaOrderMap.forEach(sAirshipArea -> {
                            if (sAirshipArea != null && !CheckNull.isEmpty(sAirshipArea.getAirship())) {
                                List<AirshipWorldData> aswdList = createAirshipWorldDataList(sAirshipArea.getAirship(),
                                        sa.getArea());// 飞艇列表
                                List<Integer> emptyPos = findEmptyPos(sa.getArea(), sAirshipArea.getBlock(), aswdList.size()); // 空点列表
                                if (CheckNull.isEmpty(emptyPos)) {
                                    LogUtil.error("创建飞艇时空点位置不足 emptyPosSize:", 0, ", airshipSize:", aswdList.size());
                                    return;
                                }
                                // 设置pos
                                int cnt = Math.min(aswdList.size(), emptyPos.size());
                                for (int i = 0; i < cnt; i++) {
                                    aswdList.get(i).setPos(emptyPos.get(i));
                                }
                                // 刷飞艇的空点不够
                                if (emptyPos.size() < aswdList.size()) {
                                    LogUtil.error("创建飞艇时空点位置不足 emptyPosSize:", emptyPos.size(), ", airshipSize:", aswdList.size());
                                }
                                aswdList.stream().filter(aswd -> aswd.getPos() >= 0).forEach(aswd -> {
                                    // 添加到地图上
                                    worldDataManager.getAirshipWorldDataMap().put(aswd.getPos(), aswd);
                                    worldDataManager.getAllAirshipWorldData().add(aswd);
                                    Turple<Integer, Integer> rPos = MapHelper.reducePos(aswd.getPos());
                                    LogUtil.debug("----------添加飞艇 xy:", rPos.getA(), ",", rPos.getB(), " pos:", aswd.getPos(),
                                            ", areaId:", sa.getArea(), ", block:", MapHelper.block(aswd.getPos()), ", id:",
                                            aswd.getId(), ", triggerTime:", aswd.getTriggerTime(), "------------");
                                });
                                // 地图通知
                                EventBus.getDefault()
                                        .post(new Events.AreaChangeNoticeEvent(emptyPos, Events.AreaChangeNoticeEvent.MAP_TYPE));
                            }
                        });
                    }
                    // }
                });
    }

    /**
     * 处理刷新逻辑
     *
     * @param aswd 飞艇
     */
    private void processRefreshAirship(AirshipWorldData aswd, int now) {
        if (aswd.isLiveStatus()) {
            airshipRunAway(aswd, now);
        } else if (aswd.isRefreshStatus()) {
            // 地图上重新生成
            int areaId = aswd.getAreaId();
            StaticArea staticArea = StaticWorldDataMgr.getAreaMap().get(areaId);
            if (staticArea != null) {
                StaticAirship sAirship = StaticWorldDataMgr.getAirshipMap().get(aswd.getId());
                if (sAirship == null) {
                    LogUtil.error("飞艇配置未找到 id:", aswd.getId());
                    return;
                }
                List<StaticAirshipArea> areaOrderMap = StaticWorldDataMgr.getAirshipAreaOrderMap(staticArea.getOpenOrder());
                if (!CheckNull.isEmpty(areaOrderMap)) {
                    StaticAirshipArea staticAirshipArea = areaOrderMap.stream().filter(saa -> {
                        List<List<Integer>> airshipList = saa.getAirship();
                        for (List<Integer> conf : airshipList) {
                            int airConfId = conf.get(0);
                            if (airConfId == sAirship.getId()) {
                                return true;
                            }
                        }
                        return false;
                    }).findFirst().orElse(null);
                    if (staticAirshipArea != null && !CheckNull.isEmpty(staticAirshipArea.getBlock())) {
                        List<Integer> emptyPos = findEmptyPos(staticArea.getArea(), staticAirshipArea.getBlock(), 1); // 空点列表
                        if (!CheckNull.isEmpty(emptyPos)) {
                            aswd.setPos(emptyPos.get(0));
                            aswd.setStatus(AirshipWorldData.STATUS_LIVE);
                            aswd.setTriggerTime(now + sAirship.getLiveTime());
                            addNpcForce(aswd, sAirship.getForm());
                            worldDataManager.getAirshipWorldDataMap().put(aswd.getPos(), aswd);
                            chatDataManager.sendSysChat(ChatConst.CHAT_AIRSHIP_REAPPEAR, aswd.getAreaId(), 0, aswd.getId(),
                                    aswd.getPos());
                            // 通知地图
                            List<Integer> posList = new ArrayList<>(1);
                            posList.add(emptyPos.get(0));
                            EventBus.getDefault()
                                    .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_TYPE));
                            Turple<Integer, Integer> rPos = MapHelper.reducePos(aswd.getPos());
                            LogUtil.debug("----------重新刷新飞艇 xy:", rPos.getA(), ",", rPos.getB(), " pos:", aswd.getPos(),
                                    ", areaId:", areaId, ", block:", MapHelper.block(aswd.getPos()), ", id:",
                                    aswd.getId(), ", triggerTime:", aswd.getTriggerTime(), "------------");
                        }
                    }
                }
            }
        }
    }

    /**
     * 飞艇逃跑处理
     *
     * @param aswd
     * @param now
     */
    private void airshipRunAway(AirshipWorldData aswd, int now) {
        List<Integer> posList = new ArrayList<>();
        if (aswd.getPos() > 0) {
            posList.add(aswd.getPos());
        }
        returnArmyAuto(aswd, now, posList);
        // 发邮件
        aswd.getJoinRoles().values().stream().flatMap(Collection::stream).map(BattleRole::getRoleId).distinct()
                .forEach(roleId -> {
                    Player p = playerDataManager.getPlayer(roleId);
                    if (p != null) {
                        mailDataManager.sendNormalMail(p, MailConstant.MOLD_AIRSHIP_RUN_AWAY, now, aswd.getId(),
                                aswd.getId());
                    }
                });
        if (aswd.getBelongRoleId() > 0) {
            Player bp = playerDataManager.getPlayer(aswd.getBelongRoleId());
            boolean belongHasInJoin = aswd.getJoinRoles().values().stream().flatMap(Collection::stream).anyMatch(br -> br.getRoleId() == aswd.getBelongRoleId());
            if (bp != null && !belongHasInJoin) {
                mailDataManager.sendNormalMail(bp, MailConstant.MOLD_AIRSHIP_RUN_AWAY, now, aswd.getId(), aswd.getId());
            }
        }
        // 移除飞艇
        removeAirshipFromMap(aswd, now, AirshipWorldData.STATUS_REFRESH);

        if (!CheckNull.isEmpty(posList)) {
            EventBus.getDefault()
                    .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));
        }
        Turple<Integer, Integer> rPos = MapHelper.reducePos(aswd.getPos());
        LogUtil.debug("----------飞艇逃跑 xy:", rPos.getA(), ",", rPos.getB(), " pos:", aswd.getPos(),
                ", areaId:", aswd.getAreaId(), ", block:", MapHelper.block(aswd.getPos()), ", id:",
                aswd.getId(), ", triggerTime:", aswd.getTriggerTime(), "------------");
    }

    private void returnArmyAuto(AirshipWorldData aswd, int now, List<Integer> posList) {
        // 撤回部队
        aswd.getJoinRoles().values().stream().flatMap(Collection::stream).forEach(br -> {
            long roleId = br.getRoleId();
            Player p = playerDataManager.getPlayer(roleId);
            if (p != null) {
                int armyKeyId = br.getKeyId();
                Army army = p.armys.get(armyKeyId);
                if (army != null) {
                    worldService.retreatArmy(p, army, now);
                    worldService.synRetreatArmy(p, army, now);
                    if (posList != null && !posList.contains(p.lord.getPos())) {
                        posList.add(p.lord.getPos());
                    }
                }
            }
        });
    }

    /**
     * 地图上移除飞艇
     *
     * @param aswd
     * @param now
     * @param airState {@link AirshipWorldData#STATUS_REFRESH} or {@link AirshipWorldData#STATUS_DEAD_REFRESH}
     */
    public void removeAirshipFromMap(AirshipWorldData aswd, int now, int airState) {
        // 推送满足条件的集结(取消)
        campService.syncCancelRallyBattle(null, null, aswd);
        // 移除飞艇
        worldDataManager.getAirshipWorldDataMap().remove(aswd.getPos());
        aswd.setBelongRoleId(0); // 归属去掉
        aswd.setStatus(airState); // 状态修改
        int rebirthInterval = TimeHelper.HALF_HOUR_S;
        StaticAirship sAirship = StaticWorldDataMgr.getAirshipMap().get(aswd.getId());
        if (sAirship != null) {
            rebirthInterval = sAirship.getRebirthInterval();
        } else {
            LogUtil.error("==========错误飞艇配置缺少 id:", aswd.getId());
        }
        aswd.setTriggerTime(now + rebirthInterval);
        aswd.setPos(-1);
        aswd.getNpc().clear();// 清除npc信息
        aswd.getJoinRoles().clear();// 清除加入人员

    }

    /**
     * 飞艇的转点处理
     */
    public void onAcrossTheDayRun() {
        playerDataManager.getPlayers().values()
                .stream()
                .map(Player::getAndCreateAirshipPersonData)
                // 刷新飞艇的击杀数量
                .forEach(AbsDailyClear::refresh);
    }
}
