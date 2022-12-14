package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.QuartzHelper;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.gameplay.local.constant.cross.CrossFunction;
import com.gryphpoem.game.zw.gameplay.local.manger.CrossWorldMapDataManager;
import com.gryphpoem.game.zw.gameplay.local.util.MapCurdEvent;
import com.gryphpoem.game.zw.gameplay.local.util.MapEvent;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.CommonPb.Report.Builder;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.pb.GamePb4.*;
import com.gryphpoem.game.zw.quartz.ScheduleManager;
import com.gryphpoem.game.zw.quartz.jobs.DefultJob;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.PlayerRebellion;
import com.gryphpoem.game.zw.resource.domain.p.RebelBuff;
import com.gryphpoem.game.zw.resource.domain.s.StaticHero;
import com.gryphpoem.game.zw.resource.domain.s.StaticRebelBuff;
import com.gryphpoem.game.zw.resource.domain.s.StaticRebelRound;
import com.gryphpoem.game.zw.resource.domain.s.StaticRebelShop;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.fight.FightLogic;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;
import com.gryphpoem.game.zw.resource.pojo.global.GlobalSchedule;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.world.Battle;
import com.gryphpoem.game.zw.resource.pojo.world.GlobalRebellion;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.resource.util.eventdata.EventDataUp;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @author QiuKun
 * @ClassName RebelService.java
 * @Description ??????????????????
 * @date 2018???10???24???
 */
@Component("rebelService")
public class RebelService extends BaseAwkwardDataManager {

    private static final String PRE_VIEW_CALLBACK_NAME = "PRE_VIEW_CALLBACK_NAME";
    private static final String START_ROUND_CALLBACK_NAME = "START_ROUND_CALLBACK_NAME";
    private static final String END_ROUND_CALLBACK_NAME = "END_ROUND_CALLBACK_NAME";
    private static final String END_CALLBACK_NAME = "END_CALLBACK_NAME";
    private static final String ROUND_NEXT_CALLBACK_NAME = "ROUND_NEXT_CALLBACK_NAME";

    @Autowired
    private WorldDataManager worldDataManager;
    @Autowired
    private ServerSetting serverSetting;
    @Autowired
    private GlobalDataManager globalDataManager;
    @Autowired
    private WorldService worldService;
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private WarDataManager warDataManager;
    @Autowired
    private WarService warService;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private FightService fightService;
    @Autowired
    private MedalDataManager medalDataManager;
    @Autowired
    private WallService wallService;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private WorldScheduleService worldScheduleService;
    @Autowired
    private CrossWorldMapDataManager crossWorldMapDataManager;
    @Autowired
    private BattlePassDataManager battlePassDataManager;
    @Autowired
    private SeasonTalentService seasonTalentService;

    /**
     * ??????????????????????????????
     *
     * @return true????????????
     */
    private boolean worldTaskCond() {
        // int curTask = worldDataManager.getWorldTask().getWorldTaskId().get();
        int curTask = worldScheduleService.getCurrentSchduleId();
        return curTask > Constant.REBEL_WORLD_TASKID_COND;
    }

    /**
     * ??????????????? ,????????????????????????
     *
     * @return
     */
    private boolean serverOpenTimeCond() {
        int now = TimeHelper.getCurrentSecond();
        int end = (int) (serverSetting.getOpenServerDate().getTime() / 1000) + 604800;
        return now >= end;
    }

    /**
     * ????????????????????????
     *
     * @return true????????????
     */
    private boolean weekOfMonthCond(Date date) {
        List<List<Integer>> timeCfg = Constant.REBEL_START_TIME_CFG;
        if (CheckNull.isEmpty(timeCfg)) {
            return false;
        }
        List<Integer> wOfMCfg = timeCfg.get(0);// ??????????????????
        int weekOfMonth = TimeHelper.getWeekOfMonth(date);
        return wOfMCfg.contains(weekOfMonth);
    }

    /**
     * ????????????????????????????????????
     *
     * @param neeDexpectProcess ????????????????????????
     */
    /*public void initRebellion(boolean neeDexpectProcess) {
        // ???????????????
        Date nowDate = new Date();
        // ???????????????????????????
        if (oldServerOpenRebellion()) {
            return;
        }
        GlobalRebellion globalRebellion = globalDataManager.getGameGlobal().getGlobalRebellion();
        // ????????????
        int curPreViewTime = globalRebellion.getCurPreViewTime();
        // ??????????????????????????????, ??????????????????????????????(????????????, ?????????????????????????????????)
        if (TimeHelper.dateToSecond(nowDate) < curPreViewTime) {
            initSchedTime(globalRebellion, neeDexpectProcess);
            return;
        }
        // ?????????????????????????????????
        List<List<Integer>> timeCfg = Constant.REBEL_START_TIME_CFG;
        Map<Integer, Integer> nextOpenMap = globalDataManager.getGameGlobal().getMixtureDataById(GlobalConstant.REBEL_NEXT_OPEN_TIME);
        // ????????????????????????????????????
        // if (CheckNull.isEmpty(timeCfg) || !worldTaskCond() || !serverOpenTimeCond() || !weekOfMonthCond(nowDate))
        //     return;

        // ???????????????????????????
        int nextTime = nextOpenMap.getOrDefault(0, 0);
        Date curOpen = TimeHelper.secondToDate(nextTime);
        // ???????????????????????????????????????
        if (CheckNull.isEmpty(timeCfg) || nextTime == 0 || (TimeHelper.dateToSecond(nowDate)) < nextTime) {
            LogUtil.debug("------??????????????????????????????????????????", DateHelper.formatDateMiniTime(TimeHelper.secondToDate(nextTime)), "-------");
            return;
        }
        // ???????????????
        int preHourCfg = timeCfg.get(2).get(0);
        // ??????????????????
        int preViewTime = (int) (TimeHelper.getSomeDayAfterOrBerfore(curOpen, 0, preHourCfg, 0, 0).getTime() / 1000);
        // ????????????????????????????????????, ??????????????????????????????
        if (TimeHelper.dateToSecond(nowDate) < preViewTime) {
            // ??????
            int hourCfg = timeCfg.get(1).get(1);
            // ??????????????????
            int roundStartTime = (int) (TimeHelper.getSomeDayAfterOrBerfore(curOpen, 0, hourCfg, 0, 0).getTime() / 1000);
            // ????????????
            int duringTimeCfg = timeCfg.get(1).get(2);
            // ??????????????????
            int roundEndTime = roundStartTime + duringTimeCfg;
            // ????????????
            int endTime = (int) (TimeHelper.getSomeDayAfterOrBerfore(curOpen, 1, 23, 59, 58).getTime() / 1000);
            // ?????????????????????????????????
            initRebellion(preViewTime, roundStartTime, roundEndTime, endTime, neeDexpectProcess);
        }
        // ?????????28?????????
        Date nextOpen = TimeHelper.getSomeDayAfterOrBerfore(curOpen, 28, 0, 0, 0);
        int value = TimeHelper.dateToSecond(nextOpen);
        nextOpenMap.put(0, value);
        globalDataManager.getGameGlobal().setMixtureData(GlobalConstant.REBEL_NEXT_OPEN_TIME, nextOpenMap);
    }*/


    /**
     * ????????????????????????????????????
     *
     * @param neeDexpectProcess ????????????????????????
     */
    public void initRebellion(boolean neeDexpectProcess) {
        // ???????????????
        Date nowDate = new Date();
        // ???????????????????????????
        if (oldServerOpenRebellion()) {
            return;
        }
        Map<Integer, Integer> nextOpenMap = globalDataManager.getGameGlobal().getMixtureDataById(GlobalConstant.REBEL_NEXT_OPEN_TIME);
        // ???????????????????????????
        int nextTime = nextOpenMap.getOrDefault(0, 0);
        Date curOpen = TimeHelper.secondToDate(nextTime);
        // ???????????????????????????????????????
        int dayiy = DateHelper.dayiy(curOpen, nowDate);
        // ???????????????????????????
        GlobalRebellion globalRebellion = globalDataManager.getGameGlobal().getGlobalRebellion();
        // ?????????, ?????????????????????????????????????????????????????????
        if (dayiy == 1 || (TimeHelper.dateToSecond(nowDate) < globalRebellion.getCurRoundStartTime() && neeDexpectProcess)) {
            // ?????????????????????????????????(????????????, ?????????????????????????????????)
            if (nextTime == 0) {
                // ?????????????????????
                initSchedTime(globalRebellion, neeDexpectProcess);
            } else {
                // ?????????????????????BOSS???
                initRebellion(curOpen, neeDexpectProcess);
            }
        } else if (dayiy > 1) {
            // ?????????????????????, ????????????????????????BOSS??????
            if (nextTime != 0) {
                // ??????????????????, ?????????28?????????
                Date nextOpen = TimeHelper.getSomeDayAfterOrBerfore(curOpen, worldScheduleService.globalActNextOpenTemplate(), 20, 0, 0);
                int value = TimeHelper.dateToSecond(nextOpen);
                nextOpenMap.put(0, value);
                globalDataManager.getGameGlobal().setMixtureData(GlobalConstant.REBEL_NEXT_OPEN_TIME, nextOpenMap);
                LogUtil.debug("------????????????????????????????????????", DateHelper.formatDateMiniTime(curOpen), "-------");
            }
        } else {
            // ?????????????????????
            LogUtil.debug("------??????????????????????????????????????????", DateHelper.formatDateMiniTime(curOpen), "-------");
        }
    }

    /**
     * ???????????????????????????
     * @return
     */
    private boolean oldServerOpenRebellion() {
        //  ??????????????????????????????????????????
        Map<Integer, Integer> nextOpenMap = globalDataManager.getGameGlobal().getMixtureDataById(GlobalConstant.REBEL_NEXT_OPEN_TIME);
        // ???????????????boss???????????????, ????????????????????????????????????
        if (checkUnLock() && nextOpenMap.getOrDefault(0,0) == 0) {
            worldScheduleService.initRebellion();
            return true;
        }
        return false;
    }


    /**
     * ??????????????????????????????
     *
     * @return
     */
    public boolean checkUnLock() {
        return worldService.bossDeadState() > 0;
    }


    /**
     * ???????????????????????????, ??????????????????
     *
     * @param curOpen           ??????????????????
     * @param neeDexpectProcess ????????????????????????
     */
    public void initRebellion(Date curOpen, boolean neeDexpectProcess) {
        // ?????????????????????????????????
        List<List<Integer>> timeCfg = Constant.REBEL_START_TIME_CFG;
        if (CheckNull.isEmpty(timeCfg)) {
            LogUtil.error("------?????????????????????????????????");
            return;
        }
        // ???????????????
        int preHourCfg = timeCfg.get(2).get(0);
        // ??????????????????
        int preViewTime = (int) (TimeHelper.getSomeDayAfterOrBerfore(curOpen, 0, preHourCfg, 0, 0).getTime() / 1000);
        // ??????
        int hourCfg = timeCfg.get(1).get(1);
        // ??????????????????
        int roundStartTime = (int) (TimeHelper.getSomeDayAfterOrBerfore(curOpen, 0, hourCfg, 0, 0).getTime() / 1000);
        // ????????????
        int duringTimeCfg = timeCfg.get(1).get(2);
        // ??????????????????
        int roundEndTime = roundStartTime + duringTimeCfg;
        // ????????????
        int endTime = (int) (TimeHelper.getSomeDayAfterOrBerfore(curOpen, 1, 23, 59, 58).getTime() / 1000);
        initRebellion(preViewTime, roundStartTime, roundEndTime, endTime, neeDexpectProcess);
    }

    /**
     * ???????????????????????????, ??????????????????
     *
     * @param preViewTime       ???????????????
     * @param roundStartTime    ??????????????????
     * @param roundEndTime      ??????????????????
     * @param endTime           ??????????????????
     * @param neeDexpectProcess ????????????????????????
     */
    public void initRebellion(int preViewTime, int roundStartTime, int roundEndTime, int endTime, boolean neeDexpectProcess) {
        int nowTime = TimeHelper.getCurrentSecond();
        GlobalRebellion globalRebellion = globalDataManager.getGameGlobal().getGlobalRebellion();
        if (preViewTime != globalRebellion.getCurPreViewTime()
                && !(nowTime >= globalRebellion.getCurPreViewTime() && nowTime <= globalRebellion.getCurEndTime())) {
            globalRebellion.reset(); // ????????????
            warDataManager.getRebelBattleCacheMap().clear();
            globalRebellion.incrOpenCnt();// ?????????1

            // ???????????????????????????
            globalRebellion.setCurPreViewTime(preViewTime);
            globalRebellion.setCurRoundStartTime(roundStartTime);
            globalRebellion.setCurRoundEndTime(roundEndTime);
            globalRebellion.setCurEndTime(endTime);
        }
        initSchedTime(globalRebellion, neeDexpectProcess);
    }

    /**
     * ?????????????????????
     */
    @Override
    public void initRebellion() {
        initRebellion(true);
    }

    /**
     * ????????????????????????
     */
    public void reloadData() {
        GlobalRebellion globalRebellion = globalDataManager.getGameGlobal().getGlobalRebellion();
        onEndTime(globalRebellion, null);
        Scheduler sched = ScheduleManager.getInstance().getSched();
        // ???????????????
        QuartzHelper.removeJob(sched, PRE_VIEW_CALLBACK_NAME, DefultJob.DEFULT_GROUP);
        QuartzHelper.removeJob(sched, START_ROUND_CALLBACK_NAME, DefultJob.DEFULT_GROUP);
        QuartzHelper.removeJob(sched, END_ROUND_CALLBACK_NAME, DefultJob.DEFULT_GROUP);
        QuartzHelper.removeJob(sched, END_CALLBACK_NAME, DefultJob.DEFULT_GROUP);
        initRebellion(false);
    }

    /**
     * ??????????????????
     *
     * @param globalRebellion   ???????????????????????????
     * @param neeDexpectProcess ????????????????????????
     */
    private void initSchedTime(GlobalRebellion globalRebellion, boolean neeDexpectProcess) {
        addSchedule(PRE_VIEW_CALLBACK_NAME, this::onPreView, globalRebellion.getCurPreViewTime(), globalRebellion); // ???????????????????????????
        addSchedule(START_ROUND_CALLBACK_NAME, this::onRoundStart, globalRebellion.getCurRoundStartTime(),
                globalRebellion);
        addSchedule(END_ROUND_CALLBACK_NAME, this::onRoundEnd, globalRebellion.getCurRoundEndTime(), globalRebellion);
        addSchedule(END_CALLBACK_NAME, this::onEndTime, globalRebellion.getCurEndTime(), globalRebellion);

        int now = TimeHelper.getCurrentSecond();

        // if (now >= globalRebellion.getCurRoundStartTime() && now < globalRebellion.getCurRoundEndTime()
        // && !QuartzHelper.isExistSched(ScheduleManager.getInstance().getSched(), ROUND_NEXT_CALLBACK_NAME,
        // DefultJob.DEFULT_GROUP)) {
        //
        // }
        // ???????????????
        if (now >= globalRebellion.getCurPreViewTime() && now < globalRebellion.getCurRoundStartTime()) {
            onPreView(globalRebellion, null);
        }
        if (neeDexpectProcess) { // ????????????
            if (now >= globalRebellion.getCurRoundStartTime() && now < globalRebellion.getCurRoundEndTime()) {// ???????????????
                onRoundStartRoundEndExceptionStop(globalRebellion);
            } else if (now >= globalRebellion.getCurRoundEndTime() && now < globalRebellion.getCurEndTime()) {
                onRoundEnd(globalRebellion, null);
            } else if (now >= globalRebellion.getCurEndTime()) {
                onRoundEnd(globalRebellion, null);
                onEndTime(globalRebellion, null);
            }
        }
        // int now = TimeHelper.getCurrentSecond();
        // if (now < globalRebellion.getCurPreViewTime()) { // ???????????????
        // } else if (now >= globalRebellion.getCurPreViewTime() && now < globalRebellion.getCurRoundStartTime()) {//
        // ?????????
        // } else if (now >= globalRebellion.getCurRoundStartTime() && now < globalRebellion.getCurRoundEndTime()) { //
        // ?????????
        // } else if (now >= globalRebellion.getCurRoundEndTime() && now < globalRebellion.getCurEndTime()) {
        // } else { // ?????????
        // }
    }

    private void addSchedule(String name, RebelCallback r, int time, GlobalRebellion globalRebellion) {
        int now = TimeHelper.getCurrentSecond();
        if (now < time) {
            Scheduler sched = ScheduleManager.getInstance().getSched();
            Date atDate = TimeHelper.secondToDate(time);
            ScheduleManager.getInstance().addOrModifyDefultJob(DefultJob.createDefult(name), job -> {
                r.onRun(globalRebellion, job);
            }, atDate);
            LogUtil.debug("??????????????????????????????  name:", name, ", time:", DateHelper.formatDateMiniTime(atDate), ", ?????????:",
                    globalRebellion.getCurRound());
        } else {
            LogUtil.error("??????????????????????????? name:", name, ", time:", TimeHelper.secondToDate(time), ", ?????????:",
                    globalRebellion.getCurRound(), ", now:", now, ", time:", time);
        }
    }

    /**
     * ??????????????????templateId
     *
     * @param globalRebellion
     * @return
     */
    private int getTemplateId(GlobalRebellion globalRebellion) {
        if (worldService.bossDeadState() == 2) { // ??????????????????
            int curSchId = Optional.ofNullable(globalDataManager.getGameGlobal().getGlobalSchedule()).map(GlobalSchedule::getCurrentScheduleId).orElse(0);
            if (curSchId == ScheduleConstant.SCHEDULE_ID_11) {
                return StaticRebelRound.TEMPLATE_4;
            } else if (curSchId == ScheduleConstant.SCHEDULE_ID_12) {
                return StaticRebelRound.TEMPLATE_5;
            } else if (curSchId == ScheduleConstant.SCHEDULE_ID_13) {
                return StaticRebelRound.TEMPLATE_6;
            }
            return StaticRebelRound.TEMPLATE_3;
        } else {
            if (globalRebellion.getOpenCnt() == 1) {// ?????????
                return StaticRebelRound.TEMPLATE_1;
            } else {
                return StaticRebelRound.TEMPLATE_2;
            }
        }
    }

    /**
     * ?????????
     *
     * @param globalRebellion
     */
    private void onPreView(GlobalRebellion globalRebellion, DefultJob job) {
        LogUtil.debug("---------------------????????????onPreView---------------------");
        // ????????????
        int templateId = getTemplateId(globalRebellion);
        globalRebellion.setCurTemplate(templateId);
        if (!globalRebellion.getJoinRoleId().isEmpty()) {// ?????????????????????
            return;
        }
        // ????????????
        List<Long> roleIds = playerDataManager.getPlayers().values().stream().filter(
                p -> p.lord.getLevel() >= Constant.REBEL_ROLE_LV_COND && p.lord.getArea() <= WorldConstant.AREA_MAX_ID)
                .map(p -> p.roleId).collect(Collectors.toList());
        globalRebellion.getJoinRoleId().addAll(roleIds);
        for (Long roleId : globalRebellion.getJoinRoleId()) {
            Player player = playerDataManager.getPlayer(roleId);
            if (player != null) {
                PlayerRebellion pRebel = player.getAndCreateRebellion();
                pRebel.cleanCurRound();
                pRebel.getBuyRecord().clear();
                syncRebellion(player, globalRebellion);
            }
        }
    }

    /**
     * ????????????
     *
     * @param globalRebellion
     */
    private void onRoundStart(GlobalRebellion globalRebellion, DefultJob job) {
        LogUtil.debug("---------------------????????????onRoundStart---------------------");
        for (Long roleId : globalRebellion.getJoinRoleId()) {
            Player player = playerDataManager.getPlayer(roleId);
            if (player != null && player.getPlayerRebellion() != null
                    && !CheckNull.isEmpty(player.getPlayerRebellion().getBuffs())) {
                boolean needReCalc = needReCalcFight(player.getPlayerRebellion().getBuffs());
                // ?????????????????????
                if (needReCalc) CalculateUtil.reCalcAllHeroAttr(player);
            }
        }
        // ???????????????
        processTriggerNextRound(globalRebellion);

    }

    /**
     * ???????????????
     *
     * @param globalRebellion
     */
    private void processTriggerNextRound(GlobalRebellion globalRebellion) {
        // ??????+1
        globalRebellion.incrCurRound();
        final int curRound = globalRebellion.getCurRound();
        final int template = globalRebellion.getCurTemplate();
        List<StaticRebelRound> srrList = StaticWorldDataMgr.getRebelRoundsByTemplate(globalRebellion.getCurTemplate());
        if (CheckNull.isEmpty(srrList)) {
            LogUtil.error("??????????????????????????? template:" + template + ", round:" + curRound);
            return;
        }
        StaticRebelRound ssr = srrList.stream().filter(s -> s.getRound() == curRound).findFirst().orElse(null);
        if (ssr == null) {
            LogUtil.common("??????????????????????????? template:" + template + ", round:" + curRound);
            return;
        }
        int interval = ssr.getInterval();
        int now = TimeHelper.getCurrentSecond();
        int battleTime = now + interval;
        // if (globalRebellion.getCurRoundEndTime() < battleTime) {
        // LogUtil.error("????????????????????????????????????????????? id:", ssr.getId());
        // return;
        // }
        globalRebellion.setNextRoundTime(battleTime);
        List<Integer> posList = null;
        // <areaId,List<MapEvent>>
        Map<Integer, List<MapEvent>> mapEventMap = null;

        if (curRound == 1) {// ?????????????????????
            posList = new ArrayList<>();
            mapEventMap = new HashMap<>();
        }
        for (Long roleId : globalRebellion.getJoinRoleId()) {
            Player player = playerDataManager.getPlayer(roleId);
            if (joinActivity(player)) {
                // ??????battle
                Battle battle = new Battle();
                battle.setType(WorldConstant.BATTLE_TYPE_REBELLION);
                battle.setBattleType(ssr.getId()); //
                battle.setBattleTime(battleTime - 1);
                battle.setBeginTime(now);
                battle.setDefencerId(player.roleId);
                battle.setPos(player.lord.getPos());
                battle.setDefencer(player);
                battle.setAtkCamp(Constant.Camp.NPC);
                battle.setDefCamp(player.lord.getCamp());
                battle.addAtkArm(ssr.getArmCnt());// ???????????????
                warDataManager.addBattle(player, battle); // ????????????
                syncRebellion(player, globalRebellion);// ???????????????
                if (posList != null && player.lord.getArea() <= WorldConstant.AREA_MAX_ID) {
                    posList.add(player.lord.getPos());
                } else if (mapEventMap != null && player.lord.getArea() > WorldConstant.AREA_MAX_ID) {
                    List<MapEvent> mapEventList = mapEventMap.computeIfAbsent(player.lord.getArea(),
                            (k) -> new ArrayList<>());
                    mapEventList.add(MapEvent.mapEntity(player.lord.getPos(), MapCurdEvent.UPDATE));
                }
            }
        }
        // ???????????????????????????
        addSchedule(ROUND_NEXT_CALLBACK_NAME, this::onRoundNext, battleTime + 2, globalRebellion);
        // ??????????????????
        if (posList != null) {
            EventBus.getDefault()
                    .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_TYPE));
        }
        // ??????????????????
        if (mapEventMap != null) {
            for (Iterator<Entry<Integer, List<MapEvent>>> it = mapEventMap.entrySet().iterator(); it.hasNext(); ) {
                Entry<Integer, List<MapEvent>> kv = it.next();
                Integer mapId = kv.getKey();
                CrossWorldMap cMap = crossWorldMapDataManager.getCrossWorldMapById(mapId);
                if (cMap != null) {
                    cMap.publishMapEvent(kv.getValue());
                }
            }
        }
    }

    /**
     * ????????????????????????????????????
     *
     * @param player
     * @return
     */
    private boolean joinActivity(Player player) {
        return player != null && !player.getAndCreateRebellion().isDead() &&
                !player.crossPlayerLocalData.inFunction(CrossFunction.CROSS_WAR_FIRE.getFunctionId());
    }

    /**
     * ???RoundStart~RoundEnd??????????????????
     *
     * @param globalRebellion
     */
    private void onRoundStartRoundEndExceptionStop(GlobalRebellion globalRebellion) {
        warDataManager.getRebelBattleCacheMap().clear();
        for (Long roleId : globalRebellion.getJoinRoleId()) {
            Player player = playerDataManager.getPlayer(roleId);
            if (player != null) {
                PlayerRebellion playerRebellion = player.getAndCreateRebellion();
                boolean needReCalc = needReCalcFight(playerRebellion.getBuffs());
                playerRebellion.getBuffs().clear(); // ??????buff
                if (needReCalc) CalculateUtil.reCalcAllHeroAttr(player); // ?????????????????????
                playerRebellion.setDead(true);
            }
        }
    }

    private boolean needReCalcFight(Map<Integer, RebelBuff> buffs) {
        return buffs != null && (buffs.containsKey(StaticRebelBuff.BUFF_TYPE_ATTK)
                || buffs.containsKey(StaticRebelBuff.BUFF_TYPE_ATTK));
    }

    /**
     * ????????????
     *
     * @param p
     * @return
     */
    public int getRoundIdByPlayer(Player p) {
        Integer battleId = warDataManager.getRebelBattleCacheMap().get(p.roleId);
        if (battleId != null && warDataManager.getBattleMap().containsKey(battleId)) {
            Battle battle = warDataManager.getBattleMap().get(battleId);
            if (battle != null && battle.isRebellionBattle()) {
                int roundId = battle.getBattleType();
                StaticRebelRound sRound = StaticWorldDataMgr.getRebelRoundById(roundId);
                if (sRound != null) {
                    return sRound.getRound();
                }
            }
        }
        return 0;
    }

    /**
     * ????????????
     *
     * @param globalRebellion
     */
    private void onRoundEnd(GlobalRebellion globalRebellion, DefultJob job) {
        LogUtil.debug("---------------------????????????onRoundEnd---------------------");
        // ????????????????????????
        Set<Integer> camps = new HashSet<>();
        // ??????????????????
        warDataManager.getRebelBattleCacheMap().clear();
        List<Integer> posList = new ArrayList<>();
        for (Long roleId : globalRebellion.getJoinRoleId()) {
            Player player = playerDataManager.getPlayer(roleId);
            if (player != null) {
                posList.add(player.lord.getPos());
                PlayerRebellion playerRebellion = player.getPlayerRebellion();
                if (playerRebellion != null) {
                    boolean needReCalc = needReCalcFight(playerRebellion.getBuffs());
                    playerRebellion.getBuffs().clear(); // ??????buff
                    if (needReCalc) CalculateUtil.reCalcAllHeroAttr(player); // ?????????????????????
                    if (!playerRebellion.isDead()) {
                        // ????????????
                        camps.add(player.lord.getCamp());
                    }
                }
            }
        }
        // ??????????????????
        EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_TYPE));
        if (job != null) {
            // ?????????
            int now = TimeHelper.getCurrentSecond();
            if (!CheckNull.isEmpty(camps)) {
                for (Player p : playerDataManager.getPlayers().values()) {
                    if (camps.contains(p.lord.getCamp())) {
                        PlayerRebellion pRebellion = p.getAndCreateRebellion();
                        pRebellion.addAndGetCredit(Constant.REBEL_ALL_PASS_AWARD); // ?????????
                        LogLordHelper.commonLog("rebelAllPass", AwardFrom.REBELLION_ALL_PASS, p,
                                Constant.REBEL_ALL_PASS_AWARD);
                        mailDataManager.sendNormalMail(p, MailConstant.MOLD_REBEL_ALL_PASS, now, p.lord.getCamp());
                    }
                }
            }
        }
    }

    /**
     * ??????
     *
     * @param globalRebellion
     */
    private void onEndTime(GlobalRebellion globalRebellion, DefultJob job) {
        LogUtil.debug("---------------------????????????onEndTime---------------------");
        // ?????????????????????
        for (Long roleId : globalRebellion.getJoinRoleId()) {
            Player player = playerDataManager.getPlayer(roleId);
            if (player != null) {
                PlayerRebellion playerRebellion = player.getAndCreateRebellion();
                playerRebellion.cleanCurRound();
                playerRebellion.getBuffs().clear();
            }
        }
        // ?????????????????????
        globalRebellion.reset();
    }

    /**
     * ??????????????????
     *
     * @param globalRebellion
     * @param job
     */
    private void onRoundNext(GlobalRebellion globalRebellion, DefultJob job) {
        LogUtil.debug("---------------------????????????onRoundNext--------------------- globalRebellion:"
                + globalRebellion.toString());
        processTriggerNextRound(globalRebellion);
    }

    private static interface RebelCallback {
        void onRun(GlobalRebellion globalRebellion, DefultJob job);
    }

    /*--------------------------------???????????????????????????-------------------------------*/

    /**
     * ??????????????????
     *
     * @param battle
     * @param now
     * @param removeBattleIdSet
     */
    public void processBattleLogic(Battle battle, int now, Set<Integer> removeBattleIdSet) {
        int roundId = battle.getBattleType();
        StaticRebelRound sRound = StaticWorldDataMgr.getRebelRoundById(roundId);
        if (sRound == null) {
            LogUtil.error("???????????????????????????????????? roundId", roundId);
            return;
        }

        // ?????????,?????? ??????
        warService.addCityDefendRoleHeros(battle);
        Fighter attacker = fightService.createNpcFighter(sRound.getFrom()); // npc??????
        Fighter defender = fightService.createCampBattleDefencer(battle, null);
        FightLogic fightLogic = new FightLogic(attacker, defender, true, battle.getType());
        warDataManager.packForm(fightLogic.getRecordBuild(), attacker.forces, defender.forces);
        fightLogic.fight();

        //????????????-??????????????????
        ActivityDiaoChanService.killedAndDeathTask0(attacker,true,true);
        ActivityDiaoChanService.killedAndDeathTask0(defender,true,true);

        boolean defSucce = !(fightLogic.getWinState() == ArmyConstant.FIGHT_RESULT_SUCCESS);
        // ????????????????????????????????????, key:roleId
        Map<Long, ChangeInfo> changeMap = new HashMap<>();
        // ????????????
        if (defender.lost > 0) {
            warService.subBattleHeroArm(defender.forces, changeMap, AwardFrom.REBELLION_BATTLE_DEF);
        }

        long defRoleId = battle.getDefencerId();
        Player defPlayer = playerDataManager.getPlayer(defRoleId);
        PlayerRebellion playerRebellion = defPlayer.getAndCreateRebellion();

        // ??????????????????
        wallService.retreatArmy(defPlayer.lord.getPos(), !defSucce, battle.getDefencer());

        // ????????????
        Map<Long, List<Award>> recoverArmyAwardMap = new HashMap<>();
        // ????????????????????????????????????
        medalDataManager.angelInWhite(defender, recoverArmyAwardMap);
        //????????????????????????---????????????
        seasonTalentService.execSeasonTalentEffect303(defender, recoverArmyAwardMap);
        // buff??????,?????????????????????
        rebelBuffRecoverArmy(defPlayer, defender, recoverArmyAwardMap);

        // ????????????
        CommonPb.RptAtkBandit.Builder rpt = fightService.createRptBuilderPb(roundId, attacker, defender, fightLogic,
                defSucce, defPlayer);
        CommonPb.Report.Builder report = worldService.createAtkBanditReport(rpt.build(), now);
        List<Award> dropList = new ArrayList<>();
        if (defSucce) { // ????????????
            // ?????????
            LogLordHelper.commonLog("expenditure", AwardFrom.REBELLION_BATTLE_DEF, defPlayer.account, defPlayer.lord,
                    playerRebellion.getCredit(), sRound.getCredit());
            playerRebellion.addAndGetCredit(sRound.getCredit());
            //????????????
            EventDataUp.credits(defPlayer.account, defPlayer.lord,playerRebellion.getCredit(),sRound.getCredit(),CreditsConstant.REBELLION,AwardFrom.REBELLION_BATTLE_DEF);
            if (sRound.getCredit() > 0) {
                dropList.add(PbHelper.createAwardPb(AwardType.MONEY, AwardType.Money.REBEL_CREDIT, sRound.getCredit()));
            }
            playerRebellion.setRound(sRound.getRound());// ????????????
            // ????????????
            List<List<Integer>> awardList = sRound.getAward();
            if (!CheckNull.isEmpty(awardList)) {
                List<Award> sendReward = rewardDataManager.sendReward(defPlayer, awardList,
                        AwardFrom.REBELLION_BATTLE_DEF);
                dropList.addAll(sendReward);
            }
        } else { // ????????????
            playerRebellion.setDead(true);
            // ???????????????????????????battleId
            warDataManager.getRebelBattleCacheMap().remove(defRoleId);
        }
        battlePassDataManager.updTaskSchedule(defPlayer.roleId, TaskType.COND_REBEL_ATK_CNT, 1);

        //????????????-????????????


        // ????????????
        sendRebelBattleMail(battle, report, defSucce, dropList, now, recoverArmyAwardMap, sRound);
        LogLordHelper.commonLog("rebelBattle", AwardFrom.REBELLION_BATTLE_DEF, defPlayer, sRound.getId(),
                sRound.getRound(), defSucce);
        // ????????????
        warService.logBattle(battle, fightLogic.getWinState(),attacker,defender, rpt.getAtkHeroList(), rpt.getDefHeroList());
        // ????????????????????????
        warService.retreatBattleArmy(battle, now);
    }

    /**
     * ????????????
     *
     * @param battle
     * @param report
     * @param defSucce
     * @param dropList
     * @param now
     * @param recoverArmyAwardMap
     */
    private void sendRebelBattleMail(Battle battle, Builder report, boolean defSucce, List<Award> dropList, int now,
                                     Map<Long, List<Award>> recoverArmyAwardMap, StaticRebelRound sRound) {
        final long defRoleId = battle.getDefencerId();
        Player defPlayer = battle.getDefencer();
        List<Player> helpPlayerList = battle.getDefList().stream().filter(br -> br.getRoleId() != defRoleId)
                .map(br -> br.getRoleId()).distinct().map(rId -> playerDataManager.getPlayer(rId))
                .filter(p -> p != null).collect(Collectors.toList());
        String defNick = defPlayer.lord.getNick();
        int round = sRound.getRound();
        Turple<Integer, Integer> xy = MapHelper.reducePos(defPlayer.lord.getPos());
        int defX = xy.getA();
        int defY = xy.getB();
        Object[] params = {defNick, round, defNick, defX, defY, round};
        // ???????????????
        mailDataManager.sendReportMail(defPlayer, report,
                defSucce ? MailConstant.MOLD_REBEL_DEF_SUCC_JOIN : MailConstant.MOLD_REBEL_DEF_FAIL_JOIN, dropList, now,
                recoverArmyAwardMap, params);
        // ?????????????????????
        helpPlayerList.stream().forEach(p -> {
            if (defSucce) {
                mailDataManager.sendReportMail(p, report, MailConstant.MOLD_REBEL_DEF_SUCC_HELP, null, now,
                        recoverArmyAwardMap, params);
            } else {
                mailDataManager.sendReportMail(p, report, MailConstant.MOLD_REBEL_DEF_FAIL_HELP, null, now,
                        recoverArmyAwardMap, params);
            }
        });
    }

    /**
     * buff????????????
     *
     * @param defPlayer
     * @param defender
     */
    private void rebelBuffRecoverArmy(Player defPlayer, Fighter defender,
                                      Map<Long, List<CommonPb.Award>> recoverArmyAwardMap) {
        if (defPlayer == null) return;
        PlayerRebellion playerRebellion = defPlayer.getAndCreateRebellion();
        RebelBuff recArmyBuff = playerRebellion.getBuffs().get(StaticRebelBuff.BUFF_TYPE_RECOVER_ARMY);
        int now = TimeHelper.getCurrentSecond();
        if (recArmyBuff != null && now >= recArmyBuff.getStartTime() && now < recArmyBuff.getEndTime()) {
            StaticRebelBuff sBuff = StaticWorldDataMgr.getRebelBuffByTypeLv(recArmyBuff.getType(), recArmyBuff.getLv());
            if (sBuff == null) {
                LogUtil.error("??????????????????buff???????????????  type:", recArmyBuff.getType(), ", lv:", recArmyBuff.getLv());
                return;
            }
            // key:armyType
            Map<Integer, Integer> cntMap = new HashMap<>();

            List<CommonPb.Award> awards = recoverArmyAwardMap.get(defPlayer.roleId);
            if (CheckNull.isNull(awards)) {
                awards = new ArrayList<>();
                recoverArmyAwardMap.put(defPlayer.roleId, awards);
            }
            for (Force force : defender.getForces()) {
                if (force.ownerId == defPlayer.roleId) {
                    int recArmy = (int) (force.totalLost * (sBuff.getBuffVal() / Constant.TEN_THROUSAND));
                    if (recArmy > 0) {
                        Hero hero = defPlayer.heros.get(force.id);
                        if (hero == null) continue;
                        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(hero.getHeroId());
                        if (!CheckNull.isNull(staticHero)) {
                            int heroArmyCapacity = hero.getAttr()[HeroConstant.ATTR_LEAD];
                            int addArm = recArmy + hero.getCount() >= heroArmyCapacity
                                    ? heroArmyCapacity - hero.getCount() : recArmy;
                            hero.addArm(addArm); // ????????????
                            int armyType = staticHero.getType();
                            int cnt = cntMap.getOrDefault(armyType, 0);
                            cntMap.put(armyType, cnt + addArm);
                            LogUtil.debug("???????????????????????? roleId:", defPlayer.roleId, ", heroId:", hero.getHeroId(),
                                    ", recArm:", addArm);
                            //??????????????????????????????
                            // LogLordHelper.filterHeroArm(AwardFrom.REBEL_BUFF_ACTION, defPlayer.account, defPlayer.lord, hero.getHeroId(), hero.getCount(), addArm,
                            //         Constant.ACTION_ADD, armyType, hero.getQuality());

                            // ????????????????????????
                            LogLordHelper.playerArm(
                                    AwardFrom.REBEL_BUFF_ACTION,
                                    defPlayer,
                                    armyType,
                                    Constant.ACTION_ADD,
                                    addArm
                            );
                        }
                    }
                }
            }
            // ????????????
            if (!CheckNull.isEmpty(cntMap)) {
                for (Map.Entry<Integer, Integer> kv : cntMap.entrySet()) {
                    awards.add(PbHelper.createAwardPb(AwardType.ARMY, kv.getKey(), kv.getValue()));
                }
            }
        }
    }

    /**
     * ????????????????????????Battle????????????
     *
     * @param player
     */
    public void processJoinPlayerMovePos(Player player, int prePos, int newPos) {
        Integer battleId = warDataManager.getRebelBattleCacheMap().get(player.roleId);
        if (battleId != null) {
            Battle battle = warDataManager.getBattleMap().get(battleId);
            if (battle != null && battle.isRebellionBattle()) {
                int now = TimeHelper.getCurrentSecond();
                warService.retreatAllBattleArmy(battle, now);
                List<Integer> posList = MapHelper.getAreaStartPos((MapHelper.getLineAcorss(prePos, newPos)));
                posList.add(prePos);
                posList.add(newPos);
                EventBus.getDefault()
                        .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.LINE_TYPE));
                battle.setPos(newPos);
            }

        }
    }

    /**
     * ????????????????????????
     *
     * @param player
     * @param army
     * @param now
     */
    public void marchEndRebelHelpArmy(Player player, Army army, int now) {
        int pos = army.getTarget();
        Player target = worldDataManager.getPosData(pos);
        Integer battleIdObj = army.getBattleId();
        int battleId = battleIdObj == null ? 0 : battleIdObj.intValue();
        Battle battle = warDataManager.getBattleMap().get(battleId);
        if (battle != null && target != null) {
            army.setState(ArmyConstant.ARMY_STATE_REBEL_BATTLE);
            List<Integer> heroIdList = new ArrayList<>();
            for (TwoInt twoInt : army.getHero()) {
                Hero hero = player.heros.get(twoInt.getV1());
                hero.setState(ArmyConstant.ARMY_STATE_REBEL_BATTLE);
                heroIdList.add(hero.getHeroId());
            }
            worldService.addBattleArmy(battle, player.roleId, heroIdList, army.getKeyId(), false);
        } else {
            // ????????????
            Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
            mailDataManager.sendReportMail(player, null, MailConstant.MOLD_ATK_TARGET_NOT_FOUND, null, now, xy.getA(),
                    xy.getB(), xy.getA(), xy.getB());
            worldService.retreatArmyByDistance(player, army, now);
        }
    }

    /**
     * ????????????
     *
     * @param player
     * @param army
     * @param type
     */
    public void retreatRebelHelpArmy(Player player, Army army, int type) {
        int now = TimeHelper.getCurrentSecond();
        Integer battleId = army.getBattleId();
        if (battleId != null) {
            Battle battle = warDataManager.getBattleMap().get(battleId);
            if (battle != null) {
                int camp = player.lord.getCamp();
                int armCount = army.getArmCount();
                battle.updateArm(camp, -armCount);
                worldService.removeBattleArmy(battle, player.roleId, army.getKeyId(), false); // ????????????
            }
        }
        worldService.retreatArmy(player, army, now, type);
    }

    /*--------------------------------???????????????????????????-------------------------------*/

    /*--------------------------------????????????-------------------------------*/

    /**
     * ????????????????????????
     *
     * @param player
     */
    public void syncRebellion(Player player, GlobalRebellion globalRebellion) {
        if (player != null && player.isLogin && player.ctx != null) {
            SyncRebellionRs.Builder builder = SyncRebellionRs.newBuilder();
            builder.setGRebellion(globalRebellion.ser(false));
            builder.setMyJoin(globalRebellion.getJoinRoleId().contains(player.roleId));
            PlayerRebellion pr = player.getPlayerRebellion();
            builder.setCredit(pr == null ? 0 : pr.getCredit());
            builder.setIsDead(pr != null && pr.isDead());
            if (Objects.nonNull(pr) && !CheckNull.isEmpty(pr.getBuyRecord())) {
                builder.addAllBuyRecord(pr.getBuyRecord().entrySet().stream().map(en -> PbHelper.createTwoIntPb(en.getKey(), en.getValue())).collect(Collectors.toList()));
            }
            Base.Builder baseBuilder = PbHelper.createSynBase(SyncRebellionRs.EXT_FIELD_NUMBER, SyncRebellionRs.ext,
                    builder.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, baseBuilder.build(), player.roleId));
        }
    }

    /**
     * ????????????????????????
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GetRebellionRs getRebellion(long roleId, GetRebellionRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        GlobalRebellion globalRebellion = globalDataManager.getGameGlobal().getGlobalRebellion();
        GetRebellionRs.Builder builder = GetRebellionRs.newBuilder();
        PlayerRebellion pr = player.getPlayerRebellion();
        builder.setCredit(pr == null ? 0 : pr.getCredit());
        builder.setGRebellion(globalRebellion.ser(false));
        builder.setMyJoin(globalRebellion.getJoinRoleId().contains(player.roleId));
        builder.setIsDead(pr != null && pr.isDead());
        int openServerDay = serverSetting.getOpenServerDay(new Date());
        builder.setUnLock(openServerDay <= 3 || checkUnLock());
        if (Objects.nonNull(pr) && !CheckNull.isEmpty(pr.getBuyRecord())) {
            builder.addAllBuyRecord(pr.getBuyRecord().entrySet().stream().map(en -> PbHelper.createTwoIntPb(en.getKey(), en.getValue())).collect(Collectors.toList()));
        }
        Map<Integer, Integer> rebelNextOpenMap = globalDataManager.getGameGlobal().getMixtureDataById(GlobalConstant.REBEL_NEXT_OPEN_TIME);
        int rebelNextTime = rebelNextOpenMap.getOrDefault(0, 0);
        builder.setNextOpen(rebelNextTime);
        return builder.build();

    }

    /**
     * ???????????????????????????buff
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GetRebelBuffRs getRebelBuff(long roleId, GetRebelBuffRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        GetRebelBuffRs.Builder builder = GetRebelBuffRs.newBuilder();
        PlayerRebellion playerRebellion = player.getPlayerRebellion();
        if (playerRebellion != null) {
            int now = TimeHelper.getCurrentSecond();
            for (Iterator<Entry<Integer, RebelBuff>> it = playerRebellion.getBuffs().entrySet().iterator(); it
                    .hasNext(); ) {
                Entry<Integer, RebelBuff> next = it.next();
                RebelBuff buff = next.getValue();
                if (now > buff.getEndTime()) {
                    it.remove(); // ??????????????????
                } else {
                    builder.addBuffs(buff.ser());
                }
            }
        }
        return builder.build();
    }

    /**
     * ??????????????????buff
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public BuyRebelBuffRs buyRebelBuff(long roleId, BuyRebelBuffRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        GlobalRebellion globalRebellion = globalDataManager.getGameGlobal().getGlobalRebellion();
        int now = TimeHelper.getCurrentSecond();
        if (!(now > globalRebellion.getCurPreViewTime() && now < globalRebellion.getCurRoundEndTime())) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId, ", ??????????????????buff????????????");
        }
        if (!globalRebellion.getJoinRoleId().contains(roleId)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId, ", ???????????????????????????????????????,????????????buff");
        }
        int buffType = req.getBuffType();
        PlayerRebellion rebellion = player.getAndCreateRebellion();
        if (rebellion.isDead()) {
            throw new MwException(GameError.HAS_BEEN_ELIMINATED_IN_REBEL.getCode(), "roleId:", roleId, ", ???????????????,????????????buff");
        }
        RebelBuff rebelBuff = rebellion.getBuffs().get(buffType);
        StaticRebelBuff sBuff = null;
        if (rebelBuff == null) {// ?????????????????????buff
            sBuff = StaticWorldDataMgr.getRebelBuffByTypeLv(buffType, 1);
        } else {
            sBuff = StaticWorldDataMgr.getRebelBuffByTypeLv(buffType, rebelBuff.getLv() + 1);
        }
        if (sBuff == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "roleId:", roleId,
                    ", ??????????????????buff????????????  buffType:" + buffType);
        }
        // ??????
        int needGood = sBuff.getCost();
        rewardDataManager.checkAndSubPlayerRes(player, AwardType.MONEY, AwardType.Money.GOLD, needGood,
                AwardFrom.BUY_REBELBUFF, false, sBuff.getBuffId());
        // ???buff
        if (rebelBuff == null) {
            rebelBuff = new RebelBuff(globalRebellion.getCurRoundStartTime(), globalRebellion.getCurRoundEndTime(),
                    sBuff.getType(), sBuff.getLv());
        } else {
            rebelBuff.setLv(sBuff.getLv());// ?????????
        }
        rebellion.getBuffs().put(buffType, rebelBuff); // ??????buff

        if (now >= globalRebellion.getCurRoundStartTime() && now < globalRebellion.getCurRoundEndTime()
                && buffType != StaticRebelBuff.BUFF_TYPE_RECOVER_ARMY) {
            // ????????????????????????????????????????????????
            CalculateUtil.reCalcAllHeroAttr(player);
        }
        BuyRebelBuffRs.Builder builder = BuyRebelBuffRs.newBuilder();
        builder.setGold(player.lord.getGold());
        builder.setBuff(rebelBuff.ser());
        return builder.build();
    }

    /**
     * ????????????????????????
     *
     * @param roleId
     * @param req
     * @return
     */
    public BuyRebelShopRs buyRebelShop(long roleId, BuyRebelShopRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int shopId = req.getShopId();
        GlobalRebellion globalRebellion = globalDataManager.getGameGlobal().getGlobalRebellion();
        int now = TimeHelper.getCurrentSecond();
        if (!(now > globalRebellion.getCurPreViewTime() && now < globalRebellion.getCurEndTime())) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId:", roleId, ", ???????????????????????????????????????");
        }
        StaticRebelShop sRShop = StaticWorldDataMgr.getRebelShopMapById(shopId);
        if (sRShop == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "roleId:", roleId, ", ??????????????????id????????? shopId:", shopId);
        }
        if (sRShop.getTemplate() != globalRebellion.getCurTemplate()) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "roleId:", roleId, ", ????????????????????????????????? shopId:", shopId);
        }
        int price = sRShop.getPrice();
        // ????????????????????????
        PlayerRebellion playerRebellion = player.getPlayerRebellion();
        if (playerRebellion == null || playerRebellion.getCredit() < price) {
            throw new MwException(GameError.BUY_REBELSHOP_CREDIT_NOT_ENOUGH.getCode(), "roleId:", roleId,
                    ", ????????????????????????????????????");
        }
        Map<Integer, Integer> buyRecord = playerRebellion.getBuyRecord();
        int count = buyRecord.getOrDefault(shopId, 0);
        if (count >= sRShop.getCount()) {
            throw new MwException(GameError.BUY_REBELSHOP_CREDIT_NOT_ENOUGH.getCode(), "roleId:", roleId,
                    ", ????????????????????????????????????");
        }
        // ?????????,????????????
        LogLordHelper.commonLog("expenditure", AwardFrom.BUY_REBEL_SHOP, player.account, player.lord,
                player.getPlayerRebellion().getCredit(), -price);
        int hasCredit = player.getPlayerRebellion().getCredit();
        player.getPlayerRebellion().setCredit(hasCredit - price);
        //????????????
        EventDataUp.credits(player.account, player.lord,playerRebellion.getCredit(),- price,CreditsConstant.REBELLION,AwardFrom.REBELLION_BATTLE_DEF);
        // ??????????????????
        buyRecord.put(shopId, count + 1);
        // ?????????
        Award awardSignle = rewardDataManager.addAwardSignle(player, sRShop.getAward(), AwardFrom.BUY_REBEL_SHOP);
        BuyRebelShopRs.Builder builder = BuyRebelShopRs.newBuilder();
        builder.setCredit(player.getPlayerRebellion().getCredit());
        builder.setAward(awardSignle);
        builder.setBuyRecord(PbHelper.createTwoIntPb(shopId, count + 1));
        return builder.build();

    }
    /*--------------------------------????????????-------------------------------*/

}
