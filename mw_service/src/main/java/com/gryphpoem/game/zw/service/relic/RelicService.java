package com.gryphpoem.game.zw.service.relic;

import com.alibaba.fastjson.JSON;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.QuartzHelper;
import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.dataMgr.StaticDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.face.MergeSvrGlobal;
import com.gryphpoem.game.zw.face.MergeSvrPlayer;
import com.gryphpoem.game.zw.gameplay.local.world.WorldEntityType;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.*;
import com.gryphpoem.game.zw.quartz.ScheduleManager;
import com.gryphpoem.game.zw.quartz.jobs.RelicJob;
import com.gryphpoem.game.zw.quartz.jobs.RelicSafeExpireJob;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticCity;
import com.gryphpoem.game.zw.resource.domain.s.StaticRelic;
import com.gryphpoem.game.zw.resource.domain.s.StaticRelicShop;
import com.gryphpoem.game.zw.resource.pojo.GameGlobal;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.global.WorldSchedule;
import com.gryphpoem.game.zw.resource.pojo.relic.GlobalRelic;
import com.gryphpoem.game.zw.resource.pojo.relic.PlayerRelic;
import com.gryphpoem.game.zw.resource.pojo.relic.RelicCons;
import com.gryphpoem.game.zw.resource.pojo.relic.RelicEntity;
import com.gryphpoem.game.zw.resource.pojo.season.CampRankData;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.resource.util.eventdata.EventDataUp;
import com.gryphpoem.game.zw.service.*;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 王朝遗迹
 *
 * @author xwind
 * @date 2022/8/2
 */
@Service
public class RelicService extends AbsGameService implements GmCmdService, MergeSvrPlayer, MergeSvrGlobal {
    @Autowired
    private ServerSetting serverSetting;
    @Autowired
    private WorldScheduleService worldScheduleService;
    @Autowired
    private WorldDataManager worldDataManager;
    @Autowired
    private GlobalDataManager globalDataManager;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private RelicsFightService relicsFightService;
    @Autowired
    private ChatDataManager chatDataManager;
    @Autowired
    private TaskDataManager taskDataManager;

    /**
     * 获取活动数据
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GamePb6.GetRelicDataInfoRs getRelicDataInfo(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        GamePb6.GetRelicDataInfoRs.Builder builder = GamePb6.GetRelicDataInfoRs.newBuilder();
        int score = player.getPlayerRelic().getScore();
        builder.setScore(score);
        player.getPlayerRelic().getGotMap().entrySet().forEach(entry -> builder.addGot(PbHelper.createTwoIntPb(entry.getKey(), entry.getValue())));
        //所在区域没有遗迹就随便找个
        int pos, holdCamp;
        List<CampRankData> campRankDataList = null;
        GlobalRelic globalRelic = globalDataManager.getGameGlobal().getGlobalRelic();
        if (globalRelic.getRelicEntityBackMap().isEmpty()) {
            pos = 0;
            holdCamp = 0;
        } else {
            RelicEntity relicEntity = globalRelic.getRelicEntityBackMap().values().stream().filter(o -> o.getArea() == player.lord.getArea()).findFirst().orElse(null);
            if (Objects.nonNull(relicEntity)) {
                pos = relicEntity.getPos();
            } else {
                pos = (int) globalRelic.getRelicEntityBackMap().keySet().toArray()[0];
                relicEntity = globalRelic.getRelicEntityBackMap().get(pos);
            }
            campRankDataList = sortCampRank(relicEntity);
            holdCamp = relicEntity.getHoldCamp();
        }
        builder.setRelicPos(pos);
        builder.setHoldCamp(holdCamp);
        if (pos > 0) {
            builder.setOverStamp(globalRelic.getOverExpire());
        }
        if (CheckNull.isEmpty(campRankDataList)) {
            for (int camp : Constant.Camp.camps) {
                CommonPb.CampRankInfo.Builder tmp = CommonPb.CampRankInfo.newBuilder();
                tmp.setCamp(camp);
                tmp.setValue(0);
                tmp.setTime(0);
                tmp.setRank(camp);
                builder.addCampRankInfo(tmp);
            }
        } else {
            campRankDataList.forEach(o -> builder.addCampRankInfo(PbHelper.buildCampRankInfo(o)));
        }
        try {
            CronExpression cronExpression = new CronExpression(ActParamConstant.ACT_RELIC_REFRESH);
            Date nextDate = cronExpression.getNextValidTimeAfter(new Date());
            builder.setNextStamp((int) (nextDate.getTime() / 1000l));
        } catch (ParseException e) {

        }

        CommonPb.RelicMaxScoreRoleInfo maxScoreRoleInfo = getMaxScoreRoleInfo();
        if (Objects.nonNull(maxScoreRoleInfo)) {
            builder.setMaxScoreRoleInfo(maxScoreRoleInfo);
        }

        // 更新阵营任务
        if (globalRelic.state() == RelicCons.OPEN && score > 0) {
            taskDataManager.updTask(player, TaskType.COND_RELIC_SCORE, score);
        }

        int curScheduleId = globalRelic.getCurScheduleId();
        builder.setCurScheduleId(globalRelic.getCurScheduleId());
        if (curScheduleId == 0) {
            builder.setCurScheduleId(worldScheduleService.getCurrentSchduleId());
        }
        return builder.build();
    }

    public void checkArmy(Player player, int pos) throws MwException {
        checkPlayerFunctionOpen(player);
        GlobalRelic globalRelic = globalDataManager.getGameGlobal().getGlobalRelic();
        if (globalRelic.state() != RelicCons.OPEN) {
            throw new MwException(GameError.RELIC_PROB_SAFE.getCode(), GameError.err(player.roleId, "遗迹探索,遗迹处于保护期"));
        }
        RelicEntity relicEntity = worldDataManager.getRelicEntityMap().get(pos);
        if (relicEntity.getHoldCamp() == player.lord.getCamp() && relicEntity.getDefendList().size() >= ActParamConstant.MAXIMUM_NUMBER_OF_RELICS_DEFENSE_QUEUE) {
            throw new MwException(GameError.THE_NUMBER_OF_RELICS_DEFENSE_QUEUE_HAS_REACHED_THE_MAXIMUM.getCode(), GameError.err(player.roleId, "遗迹探索, 遗迹防守队列军团数量达到上限"));
        }
    }

    /**
     * 校验部队行军时间
     *
     * @param player
     * @param marchTime
     * @throws MwException
     */
    public void checkArmyMarchTime(Player player, int marchTime) throws MwException {
        GlobalRelic globalRelic = globalDataManager.getGameGlobal().getGlobalRelic();
        if (globalRelic.getOverExpire() - marchTime <= TimeHelper.getCurrentSecond()) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), GameError.err(player.roleId, "遗迹探索, 行军部队行军时间不够"));
        }
    }

    /**
     * 查看遗迹详细
     *
     * @param roleId
     * @param pos
     * @return
     * @throws MwException
     */
    public GamePb6.GetRelicDetailRs getRelicDetail(long roleId, int pos) throws MwException {
        playerDataManager.checkPlayerIsExist(roleId);
        RelicEntity relicEntity = worldDataManager.getRelicEntityMap().get(pos);
        if (Objects.isNull(relicEntity)) {
            throw new MwException(GameError.INVALID_PARAM.getCode(), GameError.err(roleId, "遗迹详细,坐标错误", pos));
        }

        GamePb6.GetRelicDetailRs.Builder resp = GamePb6.GetRelicDetailRs.newBuilder();

        relicEntity.getDefendList().forEach(turple -> {
            Player p = playerDataManager.getPlayer(turple.getA());
            Army army = p.armys.get(turple.getB());
            if (Objects.nonNull(army)) {
                int armCount = army.getHero().stream().mapToInt(CommonPb.PartnerHeroIdPb::getCount).sum();
                List<Integer> heroIds = army.getHero().stream().map(o -> o.getPrincipleHeroId()).collect(Collectors.toList());
                CommonPb.RelicProbArmy.Builder builder = CommonPb.RelicProbArmy.newBuilder();
                builder.addAllHeroId(heroIds);
                builder.setLordLv(p.lord.getLevel());
                builder.setLordName(p.lord.getNick());
                builder.setArmyLead(armCount);
                List<CommonPb.TwoInt> fatigueDeBuffList;
                if ((fatigueDeBuffList = checkFatigueDeBuff(relicEntity.holdTime(turple.getA(), turple.getB()))) != null)
                    builder.addAllFatigueDeBuff(fatigueDeBuffList);
                resp.addProbArmy(builder);
            }
        });
        return resp.build();
    }

    /**
     * 校验当前队伍是否有疲劳de buff
     *
     * @param holdTime
     * @return
     */
    private List<CommonPb.TwoInt> checkFatigueDeBuff(long holdTime) {
        if (holdTime == 0l) return null;
        if (CheckNull.isEmpty(ActParamConstant.FATIGUE_DE_BUFF_PARAMETER))
            return null;
        long nowMills = System.currentTimeMillis();
        long intervalTime = (nowMills - holdTime) / 1000l;
        return ActParamConstant.FATIGUE_DE_BUFF_PARAMETER.stream().map(config_ -> {
            int attrId = config_.get(0), ratio = 0;
            if (intervalTime >= config_.get(1)) {
                ratio += config_.get(3);
            } else
                return null;
            ratio += (intervalTime - config_.get(1)) / config_.get(2) * config_.get(3);
            ratio = Math.min(ratio, config_.get(4));
            if (ratio <= 0) return null;
            return PbHelper.createTwoIntPb(attrId, ratio);
        }).filter(t -> Objects.nonNull(t)).collect(Collectors.toList());
    }

    /**
     * 领取积分奖励
     *
     * @param roleId
     * @param cfgId
     * @return
     * @throws MwException
     */
    public GamePb6.GetRelicScoreAwardRs getRelicScoreAward(long roleId, int cfgId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        StaticRelicShop staticRelicShop = StaticDataMgr.getStaticRelicShopById(cfgId);
        if (Objects.isNull(staticRelicShop) || CheckNull.isEmpty(staticRelicShop.getArea()) || staticRelicShop.getArea().size() < 2) {
            throw new MwException(GameError.INVALID_PARAM.getCode(), GameError.err(roleId, "遗迹领取积分奖励,配置id无效", cfgId));
        }
        int currentScheduleId = globalDataManager.getGameGlobal().getGlobalRelic().getCurScheduleId();
        if (staticRelicShop.getArea().get(0) > currentScheduleId || staticRelicShop.getArea().get(1) < currentScheduleId) {
            throw new MwException(GameError.INVALID_PARAM.getCode(), GameError.err(roleId, "遗迹领取积分奖励,迭代不匹配", cfgId));
        }
        if (player.getPlayerRelic().getScore() < staticRelicShop.getPrice()) {
            throw new MwException(GameError.INVALID_PARAM.getCode(), GameError.err(roleId, "遗迹领取积分奖励,积分不足", cfgId));
        }
        if (player.getPlayerRelic().getGotMap().containsKey(cfgId)) {
            throw new MwException(GameError.INVALID_PARAM.getCode(), GameError.err(roleId, "遗迹领取积分奖励,已被领取", cfgId));
        }
        List<CommonPb.Award> awardList = rewardDataManager.sendReward(player, staticRelicShop.getAward(), AwardFrom.RELIC_GET_SCORE_AWARD);
        player.getPlayerRelic().getGotMap().put(cfgId, 1);

        GamePb6.GetRelicScoreAwardRs.Builder resp = GamePb6.GetRelicScoreAwardRs.newBuilder();
        resp.addAllGetAward(awardList);
        player.getPlayerRelic().getGotMap().entrySet().forEach(entry -> resp.addGot(PbHelper.createTwoIntPb(entry.getKey(), entry.getValue())));

        return resp.build();
    }

    /**
     * 刷新遗迹
     */
    public void refreshRelic() {
        if (!checkFunctionOpen()) {
            return;
        }
        int nowStamp = TimeHelper.getCurrentSecond();
        GlobalRelic globalRelic = globalDataManager.getGameGlobal().getGlobalRelic();
        int currEra = worldScheduleService.getCurrentSchduleId();
        globalRelic.setCurScheduleId(currEra);
        List<StaticRelic> staticRelicList = StaticDataMgr.getStaticRelic(currEra);
        LogUtil.common((String.format("refresh relic, currEra=%s, config=%s", currEra, JSON.toJSONString(staticRelicList))));
        List<Integer> posList = new ArrayList<>();
        //此处做方便测试做容错正常情况一定是empty
        if (!globalRelic.getRelicEntityMap().isEmpty()) {
            globalRelic.getRelicEntityMap().values().forEach(relicEntity -> {
                posList.add(relicEntity.getPos());
                //遗迹中的部队返回
                List<Turple<Long, Integer>> tmpList = new ArrayList<>();
                tmpList.addAll(relicEntity.getDefendList());
                tmpList.forEach(turple -> {
                    Player player = playerDataManager.getPlayer(turple.getA());
                    Army army = player.armys.get(turple.getB());
                    if (Objects.nonNull(player) && Objects.nonNull(army)) {
                        retreatArmy(player, army);
                    }
                });
            });
            worldDataManager.clearRelicMap();
        }
        globalRelic.getRelicEntityBackMap().clear();
        if (!CheckNull.isEmpty(staticRelicList)) {
            staticRelicList.forEach(staticRelic -> {
                List<Integer> tmpList = RandomUtil.randomByWeight(staticRelic.getArea(), tmp -> tmp.get(2));
                StaticCity centre = StaticWorldDataMgr.getMaxTypeCityByArea(tmpList.get(0));
                int pos = worldDataManager.randomEmptyPosInRadius(centre, 30);
                if (pos > 0) {
                    RelicEntity relicEntity = new RelicEntity(staticRelic.getId(), pos, WorldEntityType.RELIC, tmpList.get(0), MapHelper.block(pos));
                    globalRelic.getRelicEntityMap().put(pos, relicEntity);
                    posList.add(pos);
                    globalRelic.getRelicEntityBackMap().put(pos, relicEntity);
                    Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
                    LogUtil.common(String.format("refresh relic, pos=%s, xy=%s", pos, xy));
                }
            });
            int safeExpire = nowStamp + ActParamConstant.ACT_RELIC_STAMP.get(0);
            int overExpire = safeExpire + ActParamConstant.ACT_RELIC_STAMP.get(1);
            globalRelic.setSafeExpire(safeExpire);
            globalRelic.setOverExpire(overExpire);
            globalRelic.setKeep(false);
            globalRelic.setFightId(0L);
            EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_TYPE));
            syncRelicRefresh();
        }
        //清理玩家积分
        playerDataManager.getAllPlayer().values().forEach(p -> {
            PlayerRelic playerRelic = p.getPlayerRelic();
            playerRelic.setStartProbe(0);
            playerRelic.setScore(0);
            playerRelic.getGotMap().clear();
            playerRelic.clearContinuousKillCnt();
        });

        // 清理遗迹聊天频道
        globalDataManager.getGameGlobal().getRelicChat().clear();

        // 清理上次的榜一信息
        globalRelic.setMaxScoreRole(null);

        // 遗迹刷新广播
        broadcastRelicRefresh();

        // 保护期结束定时器
        QuartzHelper.addJob(ScheduleManager.getInstance().getSched(), "RelicSafeExpireJob", "RefreshGroup",
                RelicSafeExpireJob.class, DateHelper.secStampToCron(globalRelic.getSafeExpire()));
    }

    /**
     * 遗迹刷新广播
     */
    private void broadcastRelicRefresh() {
        GlobalRelic globalRelic = this.globalDataManager.getGameGlobal().getGlobalRelic();
        globalRelic.getRelicEntityMap().values().forEach(relic -> {
            chatDataManager.sendSysChat(ChatConst.CHAT_RELICS_REFRESH, 0, 0, relic.getArea());
        });
    }

    /**
     * 遗迹保护期结束广播
     */
    public void broadcastRelicSafeExpire() {
        GlobalRelic globalRelic = this.globalDataManager.getGameGlobal().getGlobalRelic();
        globalRelic.getRelicEntityMap().values().forEach(relic -> {
            chatDataManager.sendSysChat(ChatConst.CHAT_RELICS_SAFE_EXPIRE, 0, 0, relic.getArea(), relic.getPos());
        });
    }

    public void syncRelicRefresh() {
        GamePb6.SyncRelicRefreshRs.Builder builder = GamePb6.SyncRelicRefreshRs.newBuilder();
        BasePb.Base msg = PbHelper.createSynBase(GamePb6.SyncRelicRefreshRs.EXT_FIELD_NUMBER, GamePb6.SyncRelicRefreshRs.ext, builder.build()).build();
        playerService.syncMsgToAll(msg);
    }

    public void getMapForce(int block, GamePb2.GetMapRs.Builder builder) {
        GlobalRelic globalRelic = globalDataManager.getGameGlobal().getGlobalRelic();
        worldDataManager.getRelicEntityMap().values().stream()
                .filter(o -> o.getBlock() == block)
                .forEach(o -> {
                    CommonPb.MapForce.Builder forceBuilder = CommonPb.MapForce.newBuilder();
                    forceBuilder.setPos(o.getPos());
                    forceBuilder.setType(WorldConstant.FORCE_TYPE_RELIC);
                    forceBuilder.setSafeExpire(globalRelic.getSafeExpire());
                    forceBuilder.setOverExpire(globalRelic.getOverExpire());
                    forceBuilder.setParam(0);
                    forceBuilder.setCamp(o.getHoldCamp());
                    forceBuilder.setProt(globalRelic.getSafeExpire() > TimeHelper.getCurrentSecond() ? 1 : 0);
                    builder.addForce(forceBuilder.build());
                });
    }

    public void buildActivity(Player player, GamePb3.GetActivityListRs.Builder listBuilder) {
        if (checkFunctionOpen()) {
            CommonPb.Activity.Builder builder = CommonPb.Activity.newBuilder();
            builder.setActivityId(0);
            builder.setName("");
            builder.setBeginTime(0);
            GlobalRelic globalRelic = globalDataManager.getGameGlobal().getGlobalRelic();
            builder.setEndTime(globalRelic.getOverExpire());
            builder.setOpen(false);
            builder.setTips(0);
            builder.setType(ActivityConst.ACT_RELIC);
            listBuilder.addActivity(builder);
        }
    }

    /**
     * 检查功能开放
     *
     * @return
     */
    public boolean checkFunctionOpen() {
        int curScheduleId = worldScheduleService.getCurrentSchduleId();
        if (curScheduleId < ActParamConstant.ACT_RELIC_STAMP.get(4))
            return false;
        WorldSchedule worldSchedule = worldScheduleService.getGlobalSchedule().getWorldSchedule(ActParamConstant.ACT_RELIC_STAMP.get(4));
        if (CheckNull.isNull(worldSchedule)) return false;
        if (DateHelper.isToday(new Date(worldSchedule.getStartTime() * 1000l)))
            return false;
        return true;
    }

    /**
     * 检查玩家开放
     *
     * @param player
     * @return
     */
    public void checkPlayerFunctionOpen(Player player) {
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_ID_RELIC)) {
            throw new MwException(GameError.FUNCTION_LOCK.getCode(), GameError.err(player.roleId, "王朝遗迹未解锁"));
        }
    }

    public void retreatArmy(Player player, Army army) {
        RelicEntity relic = globalDataManager.getGameGlobal().getGlobalRelic().getRelicEntityMap().get(army.getTarget());
        Turple<Long, Integer> turple = relic.getDefendList().stream().filter(o -> o.getB() == army.getKeyId()).findFirst().orElse(null);
        if (Objects.nonNull(turple)) {
            relic.getDefendList().remove(turple);
            relic.removeHolder(player.roleId, army.getKeyId());
        }
        relicsFightService.retreatArmy(player, army, null, TimeHelper.getCurrentSecond(), true);
        // 同步遗迹防守队伍已空
        if (CheckNull.isEmpty(relic.getDefendList()))
            syncRelicHolder(relic.getPos(), false);
    }

    public void retreatCheckProbing(Player player, Army army) {
        RelicEntity relic = globalDataManager.getGameGlobal().getGlobalRelic().getRelicEntityMap().get(army.getTarget());
        if (Objects.nonNull(relic)) {
            boolean probing = relic.isHavingProbe(player.roleId);
            if (!probing) {
                PlayerRelic playerRelic = player.getPlayerRelic();
                int score = playerRelic.setStartProbe0(0);
                //探索结束了
                mailDataManager.sendNormalMail(player, MailConstant.MOLD_RELIC_PROBE_OVER, TimeHelper.getCurrentSecond(), score);

                taskDataManager.updTask(player, TaskType.COND_RELIC_SCORE, playerRelic.getScore());
            }
        }
    }

    /**
     * 同步遗迹防守队伍信息
     *
     * @param pos
     * @param hasHolder
     */
    public void syncRelicHolder(int pos, boolean hasHolder) {
//        GamePb6.SyncRelicHolderRs.Builder builder = GamePb6.SyncRelicHolderRs.newBuilder().setPos(pos).setHasHolder(hasHolder);
//        BasePb.Base msg = PbHelper.createSynBase(GamePb6.SyncRelicHolderRs.EXT_FIELD_NUMBER, GamePb6.SyncRelicHolderRs.ext, builder.build()).build();
//        playerService.syncMsgToAll(msg);
    }

    @Override
    public void handleOnStartup() {
        this.reloadCfg();
    }

    public void reloadCfg() {
        //添加定时器
        QuartzHelper.addJob(ScheduleManager.getInstance().getSched(), "RefreshRelicJob", "RefreshGroup", RelicJob.class, ActParamConstant.ACT_RELIC_REFRESH);
    }

    public void gameRunSec() {
        GlobalRelic globalRelic = globalDataManager.getGameGlobal().getGlobalRelic();
        if (globalRelic.getOverExpire() != 0 && globalRelic.state() == RelicCons.OVER && !globalRelic.isKeep()) {
            globalRelic.setKeep(true);
            //阵营排名奖励
            int currentScheduleId = globalRelic.getCurScheduleId();
            List<StaticRelicShop> configList = StaticDataMgr.getStaticRelicShopList(currentScheduleId);
            StaticRelicShop staticRelicShop = null;
            if (CheckNull.nonEmpty(configList)) {
                staticRelicShop = configList.stream().filter(s -> Objects.nonNull(s) && CheckNull.nonEmpty(s.getPartyAward())).findFirst().orElse(null);
            }

            List<CommonPb.Award> awardList = null;
            if (Objects.nonNull(staticRelicShop))
                awardList = PbHelper.createAwardsPb(staticRelicShop.getPartyAward());
            List<CommonPb.Award> finalAwardList = awardList;
            globalRelic.getRelicEntityMap().values().forEach(entity -> {
                //固定阵营积分
                entity.setStartHold0(0);
                entity.setHoldCamp(0);
                //遗迹中的部队返回
                List<Turple<Long, Integer>> defList = new ArrayList<>(entity.getDefendList());
                defList.forEach(turple -> {
                    Player player = playerDataManager.getPlayer(turple.getA());
                    Army army = player.armys.get(turple.getB());
                    if (Objects.nonNull(player) && Objects.nonNull(army)) {
                        retreatArmy(player, army);
                    }
                });
                //阵营排名奖励
                List<CampRankData> list = sortCampRank(entity);
                if (!CheckNull.isEmpty(list)) {
                    ConcurrentHashMap<Long, Player> campPlayerMap = playerDataManager.getPlayerByCamp(list.get(0).camp);
                    if (Objects.nonNull(campPlayerMap)) {
                        campPlayerMap.values().forEach(p -> {
                            rewardDataManager.sendRewardByAwardList(p, finalAwardList, AwardFrom.RELIC_OVER_CAMPRANK_AWARD);
                            mailDataManager.sendReportMail(p, null, MailConstant.RELIC_OVER_CAMPRANK_AWARD, finalAwardList, TimeHelper.getCurrentSecond());
                        });
                    }
                }
            });

            // 榜一
            Player maxScorePlayer = null;

            //未领取的积分奖励
            for (Player p : playerDataManager.getAllPlayer().values()) {
                PlayerRelic playerRelic = p.getPlayerRelic();
                if (playerRelic.getScore() <= 0 && playerRelic.getStartProbe() <= 0) {
                    continue;
                }
                //固定玩家积分
                playerRelic.setStartProbe(0);
                mailDataManager.sendNormalMail(p, MailConstant.MOLD_RELIC_PROBE_VANISH, TimeHelper.getCurrentSecond());
                //合并发送未领取积分奖励
                List<List<Integer>> tmps = new ArrayList<>();
                configList.forEach(o -> {
                    boolean b = p.getPlayerRelic().getGotMap().containsKey(o.getId());
                    if (!b && p.getPlayerRelic().getScore() >= o.getPrice()) {
                        tmps.addAll(o.getAward());
                        p.getPlayerRelic().getGotMap().put(o.getId(), 1);
                    }
                });
                if (!tmps.isEmpty()) {
                    List<CommonPb.Award> awardList1 = rewardDataManager.sendReward(p, tmps, AwardFrom.RELIC_GET_SCORE_AWARD);
                    mailDataManager.sendReportMail(p, null, MailConstant.RELIC_OVER_SCORE_AWARD, awardList1, TimeHelper.getCurrentSecond());
                }

                // 查找榜一
                int playerScore = p.getPlayerRelic().getScore();
                if (playerScore > 0) {
                    if (Objects.isNull(maxScorePlayer)
                            || playerScore > maxScorePlayer.getPlayerRelic().getScore()
                            || (playerScore == maxScorePlayer.getPlayerRelic().getScore() && p.lord.getFight() > maxScorePlayer.lord.getFight())) {
                        maxScorePlayer = p;
                    }

                    // 遗迹结束, 更新遗迹积分
                    taskDataManager.updTask(p, TaskType.COND_RELIC_SCORE, playerScore);
                    LogLordHelper.commonLog("relicScore", AwardFrom.RELIC_GET_SCORE_AWARD, p.account, p.lord, playerScore);
                }
            }

            handleMaxScorePlayer(maxScorePlayer);

            //移除遗迹建筑
            List<Integer> posList = new ArrayList<>();
            posList.addAll(worldDataManager.getRelicEntityMap().keySet());
            worldDataManager.clearRelicMap();
            EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_TYPE));
        }
    }

    /**
     * 遗迹结束后榜一玩家的处理
     */
    private void handleMaxScorePlayer(Player maxScorePlayer) {
        if (Objects.isNull(maxScorePlayer)) {
            return;
        }

        // 记录榜一信息
        CommonPb.LongInt maxScoreRole = CommonPb.LongInt.newBuilder()
                .setV1(maxScorePlayer.roleId)
                .setV2(maxScorePlayer.getPlayerRelic().getScore())
                .build();

        GlobalRelic globalRelic = globalDataManager.getGameGlobal().getGlobalRelic();
        globalRelic.setMaxScoreRole(maxScoreRole);
    }

    private List<CampRankData> sortCampRank(RelicEntity relicEntity) {
        List<CampRankData> campRankDataList = relicEntity.getCampRankDataMap().values().stream().map(CampRankData::copyNew).collect(Collectors.toList());
        //first calc the camp hold time
        if (relicEntity.getHoldCamp() > 0 && relicEntity.getStartHold() > 0) {
            CampRankData campRankData = campRankDataList.stream().filter(o -> o.camp == relicEntity.getHoldCamp()).findFirst().orElse(null);
            campRankData.value += TimeHelper.getCurrentSecond() - relicEntity.getStartHold();
        }
        return sortCampRank(campRankDataList);
    }

    private List<CampRankData> sortCampRank(Collection<CampRankData> cols) {
        List<CampRankData> list = new ArrayList<>(cols);
        list.sort(COMPARATOR_CAMP_RANK);
        int i = 0;
        for (CampRankData campRankData : list) {
            campRankData.rank = ++i;
        }
        return list;
    }

    private static final Comparator<CampRankData> COMPARATOR_CAMP_RANK = (o1, o2) -> {
        if (o1.value < o2.value) {
            return 1;
        } else if (o1.value > o2.value) {
            return -1;
        } else {
            if (o1.time > o2.time) {
                return 1;
            } else if (o1.time < o2.time) {
                return -1;
            }
        }
        return 0;
    };

    /**
     * 获取本场遗迹榜一玩家信息
     */
    private CommonPb.RelicMaxScoreRoleInfo getMaxScoreRoleInfo() {
        GlobalRelic globalRelic = globalDataManager.getGameGlobal().getGlobalRelic();
        CommonPb.LongInt l = globalRelic.getMaxScoreRole();

        if (Objects.nonNull(l)) {
            Player maxScorePlayer = playerDataManager.getPlayer(l.getV1());
            if (Objects.nonNull(maxScorePlayer)) {
                CommonPb.RelicMaxScoreRoleInfo.Builder builder = CommonPb.RelicMaxScoreRoleInfo.newBuilder();
                builder.setId(maxScorePlayer.roleId);
                builder.setCamp(maxScorePlayer.getCamp());
                builder.setNick(maxScorePlayer.lord.getNick());
                builder.setScore(l.getV2());
                return builder.build();
            }
        }
        return null;
    }

    /**
     * 增加玩家遗迹积分处理
     */
    public void addPlayerScoreHandle(Player player, int addScore) {
        PlayerRelic playerRelic = player.getPlayerRelic();
        playerRelic.addScore(addScore);
        // 积分任务
        taskDataManager.updTask(player, TaskType.COND_RELIC_SCORE, playerRelic.getScore());
        EventDataUp.credits(player.account, player.lord, playerRelic.getScore(), addScore, CreditsConstant.RELIC_SCORE, AwardFrom.RELIC_FIGHT);
    }

    @GmCmd("relic")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {
        if (params[0].equalsIgnoreCase("refresh")) {
            refreshRelic();
        }
        if (params[0].equalsIgnoreCase("clearAll")) {
            GlobalRelic globalRelic = globalDataManager.getGameGlobal().getGlobalRelic();
            globalRelic.getRelicEntityMap().clear();
            globalRelic.getRelicEntityBackMap().clear();
            List<Integer> posList = new ArrayList<>();
            posList.addAll(worldDataManager.getRelicEntityMap().keySet());
            worldDataManager.clearRelicMap();
            EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_TYPE));
            playerDataManager.getAllPlayer().values().forEach(p -> {
                p.getPlayerRelic().getGotMap().clear();
                p.getPlayerRelic().setScore(0);
                p.getPlayerRelic().setStartProbe(0);
            });
        }
        if (params[0].equalsIgnoreCase("retreat")) {
            GlobalRelic globalRelic = globalDataManager.getGameGlobal().getGlobalRelic();
            globalRelic.getRelicEntityMap().values().forEach(relicEntity -> {
                relicEntity.setHoldCamp(0);
                relicEntity.setStartHold(0);
                relicEntity.getDefendList().forEach(turple -> {
                    Player player1 = playerDataManager.getPlayer(turple.getA());
                    Army army = player1.armys.get(turple.getB());
                    relicsFightService.retreatArmy(player1, army, null, TimeHelper.getCurrentSecond(), true);
                });
            });
        }
        if (params[0].equalsIgnoreCase("over")) {
            GlobalRelic globalRelic = globalDataManager.getGameGlobal().getGlobalRelic();
            if (globalRelic.getOverExpire() > TimeHelper.getCurrentSecond()) {
                globalRelic.setOverExpire(TimeHelper.getCurrentSecond() + 10);
            }
        }
    }

    @Override
    public void mergePlayer(Player player) {
        player.getPlayerRelic().clear();

        // 清除称号数据
//        if (!RELIC_MAX_SCORE_PLAYER_WARD.isEmpty()) {
//            for (List<Integer> list : RELIC_MAX_SCORE_PLAYER_WARD) {
//                if (list.get(0) == AwardType.TITLE) {
//                    Integer titleId = list.get(1);
//                    DressUp dressUp = player.getDressUp();
//                    Map<Integer, BaseDressUpEntity> titleMap = dressUp.getDressUpEntityMapByType(AwardType.TITLE);
//                    titleMap.remove(titleId);
//                    if (dressUp.getCurrTitle() == titleId) {
//                        // 恢复默认称号
//                        dressUp.setCurrTitle(0);
//                    }
//                }
//            }
//        }
    }

    @Override
    public void mergeGlobal(GameGlobal gameGlobal) {
        gameGlobal.getGlobalRelic().clear();
    }
}
