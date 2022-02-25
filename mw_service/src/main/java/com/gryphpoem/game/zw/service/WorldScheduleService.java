package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.holder.BooleanHolder;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.logic.FightSettleLogic;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pb.GamePb4.GetScheduleBossRq;
import com.gryphpoem.game.zw.pb.GamePb4.GetScheduleBossRs;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.domain.s.StaticSchedule;
import com.gryphpoem.game.zw.resource.domain.s.StaticScheduleGoal;
import com.gryphpoem.game.zw.resource.pojo.WorldTask;
import com.gryphpoem.game.zw.resource.pojo.fight.FightLogic;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;
import com.gryphpoem.game.zw.resource.pojo.fight.NpcForce;
import com.gryphpoem.game.zw.resource.pojo.global.GlobalSchedule;
import com.gryphpoem.game.zw.resource.pojo.global.ScheduleBoss;
import com.gryphpoem.game.zw.resource.pojo.global.ScheduleGoal;
import com.gryphpoem.game.zw.resource.pojo.global.WorldSchedule;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author QiuKun
 * @ClassName WorldScheduleService.java
 * @Description 世界进度相关逻辑
 * @date 2019年2月21日
 */
@Component
public class WorldScheduleService {

    @Autowired
    private GlobalDataManager globalDataManager;

    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private WorldDataManager worldDataManager;

    @Autowired
    private RewardDataManager rewardDataManager;

    @Autowired
    private ActivityDataManager activityDataManager;

    @Autowired
    private HonorDailyDataManager honorDailyDataManager;

    @Autowired
    private MailDataManager mailDataManager;

    @Autowired
    private MedalDataManager medalDataManager;

    @Autowired
    private WorldService worldService;

    @Autowired
    private FightService fightService;

    @Autowired
    private FightSettleLogic fightSettleLogic;

    /** 世界进程排行榜数据 */
    @Autowired
    private WorldScheduleRankService worldScheduleRankService;

    @Autowired
    private ChatDataManager chatDataManager;

    @Autowired
    private CampService campService;

    @Autowired
    private RebelService rebelService;

    @Autowired
    private WarService warService;

    /**
     * 世界进度 , 引用是 GameGlobal对象的
     */
    private GlobalSchedule globalSchedule;

    public void init() {
        initSchedule();
        globalSchedule = globalDataManager.getGameGlobal().getGlobalSchedule();
        // 检测是否配置了新的世界进程
        globalSchedule.checkNewWorldSchedule();
        // 初始化时 检查是否有可以提前完成的世界进度
        globalSchedule.tryProcessRefreshSchedule(worldScheduleRankService);
    }

    /**
     * 初始化世界进度, 兼容线上老服的世界任务
     */
    private void initSchedule() {
        // 首次初始化
        if (CheckNull.isNull(globalDataManager.getGameGlobal().getGlobalSchedule())) {
            int curSheId = 1;
            // 兼容老服
            WorldTask worldTask = globalDataManager.getGameGlobal().getWorldTask();
            final int worldTaskId = CheckNull.isNull(worldTask) ? 0 : worldTask.getWorldTaskId().get();
            // 世界任务id小于3
            if (worldTaskId < 3 && worldTaskId > 0) {
                int openServerDay = DataResource.ac.getBean(ServerSetting.class).getOpenServerDay(new Date());
                // 开服时间大于2 , 从2阶段开始, 否则从1阶段开始(新开服务器开启是1阶段)
                curSheId = openServerDay > 2 ? 2 : curSheId;
            } else {
                // 否则保持老世界任务的进度
                curSheId = worldTaskId;
                // 当老服的世界进程是 9时需要判断是否 打过世界Boss
                if (worldTaskId == ScheduleConstant.SCHEDULE_BOOS_2_ID) {
                    com.gryphpoem.game.zw.pb.CommonPb.WorldTask boss2 = worldTask.getWorldTaskMap().get(worldTaskId);
                    if (boss2 != null && boss2.getHp() <= 0) {
                        curSheId = ScheduleConstant.SCHEDULE_BERLIN_ID;
                    }
                }
            }
            globalDataManager.getGameGlobal().setGlobalSchedule(new GlobalSchedule(curSheId));
        }
    }

    /**
     * 转点触发检测
     */
    public void triggerCheckWorldScheduleEnd() {
        try {
            int now = TimeHelper.getCurrentSecond();
            int currentScheduleId = globalSchedule.getCurrentScheduleId();
            WorldSchedule worldSchedule = globalSchedule.getWorldSchedule(currentScheduleId);
            int finishTime = worldSchedule.getFinishTime();
            if (finishTime > 0 && now > finishTime) {
                // 过了当前阶段的持续时间, 并且不是boss关卡
                globalSchedule.processRefreshSchedule();
                // 同步世界进程
                syncSchedule();
                // 发放当前进度结束奖励
                worldScheduleRankService.worldScheduleRankAward(currentScheduleId);
            }
        } catch (Exception e) {
            LogUtil.error("世界进度定时器出错", e);
        }

    }

    /**
     * 公共活动时间轮
     * @return time round
     */
    public int globalActNextOpenTemplate() {
        return Optional.ofNullable(globalSchedule).map(GlobalSchedule::getCurrentScheduleId).orElse(0) >= ScheduleConstant.SCHEDULE_ID_11 ? ScheduleConstant.GLOBAL_ACT_TIME_ROUND_TEMPLATE_2 : ScheduleConstant.GLOBAL_ACT_TIME_ROUND_TEMPLATE_1;
    }

    /**
     * 是否是boss关卡
     *
     * @param curId
     * @return
     */
    public static boolean bossSchedule(int curId) {
        return curId == ScheduleConstant.SCHEDULE_BOOS_1_ID || curId == ScheduleConstant.SCHEDULE_BOOS_2_ID;
    }

    /**
     * 更新世界目标进度
     *
     * @param player
     * @param condId
     * @param param
     */
    public void updateScheduleGoal(Player player, int condId, int param) {
        if (CheckNull.isNull(globalSchedule)) {
            return;
        }
        List<StaticScheduleGoal> ssgList = StaticWorldDataMgr.getScheduleGoalMap().values().stream()
                .filter(ssg -> ssg.getCond() == condId).collect(Collectors.toList());
        BooleanHolder hasChange = new BooleanHolder(false);
        ssgList.forEach(sSchedGoal -> {
            int scheduleId = sSchedGoal.getScheduleId();
            WorldSchedule worldSchedule = globalSchedule.getWorldSchedule(scheduleId);
            if (CheckNull.isNull(worldSchedule)
                    || worldSchedule.getStatus() == ScheduleConstant.SCHEDULE_STATUS_NOT_YET_BEGIN) {
                return;
            }
            // 达成目标同步可领取信息
            if (worldSchedule.updateScheduleGoal(player, sSchedGoal, param)) {
                hasChange.set(true);
                syncScheduleGoal(player, sSchedGoal);
            }
        });

        if (hasChange.get()) {
            // 尝试提前结束世界进程
            globalSchedule.tryProcessRefreshSchedule(worldScheduleRankService);
            // 同步世界进程
            syncSchedule();
        }
    }

    /**
     * 推送世界进度
     *
     * @param player
     * @param sSchedGoal
     */
    private void syncScheduleGoal(Player player, StaticScheduleGoal sSchedGoal) {
        if (CheckNull.isNull(globalSchedule)) {
            return;
        }
        List<Player> syncPlayers = new ArrayList<>();
        if (sSchedGoal.getCond() == ScheduleConstant.GOAL_COND_FIGHT) {
            syncPlayers.add(player);
        } else {
            syncPlayers.addAll(playerDataManager.getPlayers().values());
        }
        WorldSchedule worldSchedule = globalSchedule.getWorldSchedule(sSchedGoal.getScheduleId());
        if (CheckNull.isNull(worldSchedule)) {
            return;
        }
        syncPlayers.stream().filter(p -> p.ctx != null).forEach(p -> {
            GamePb4.SyncScheduleRs.Builder builder = GamePb4.SyncScheduleRs.newBuilder();
            builder.addWorldSchedule(worldSchedule.ser(p, false));
            builder.setCurrentId(globalSchedule.getCurrentScheduleId());
            BasePb.Base msg = PbHelper
                    .createSynBase(GamePb4.SyncScheduleRs.EXT_FIELD_NUMBER, GamePb4.SyncScheduleRs.ext, builder.build())
                    .build();
            MsgDataManager.getIns().add(new Msg(p.ctx, msg, p.roleId));
            // 应用外推送, 完成世界任务
            PushMessageUtil.pushMessage(p.account, PushConstant.ENOUGH_WORLD_SCHEDULE, worldSchedule.getId());
        });
    }

    /**
     * 推送世界进度更新
     */
    public void syncSchedule() {
        GamePb4.SyncScheduleChangeRs.Builder builder = GamePb4.SyncScheduleChangeRs.newBuilder();
        BasePb.Base msg = PbHelper.createSynBase(GamePb4.SyncScheduleChangeRs.EXT_FIELD_NUMBER,
                GamePb4.SyncScheduleChangeRs.ext, builder.build()).build();

        playerDataManager.getAllOnlinePlayer().values()
                .forEach(player -> MsgDataManager.getIns().add(new Msg(player.ctx, msg, player.roleId)));
    }

    /**
     * 获取世界进度
     *
     * @param roleId
     * @param req
     * @throws MwException
     */
    public GamePb4.GetScheduleRs getSchedule(long roleId, GamePb4.GetScheduleRq req) throws MwException {

        GamePb4.GetScheduleRs.Builder builder = GamePb4.GetScheduleRs.newBuilder();
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        if (!CheckNull.isNull(globalSchedule)) {
            List<Integer> scheduleIds = req.getScheduleIdList();
            List<WorldSchedule> schedules;
            if (!CheckNull.isEmpty(scheduleIds)) {
                schedules = globalSchedule.getScheduleMap().values().stream()
                        .filter(sched -> scheduleIds.contains(sched.getId())).collect(Collectors.toList());
            } else {
                schedules = new ArrayList<>(globalSchedule.getScheduleMap().values());
            }
            schedules.forEach(sched -> builder.addWorldSchedule(sched.ser(player, false)));
        }
        CommonPb.PersonalWorldSchedule.Builder perosoal = CommonPb.PersonalWorldSchedule.newBuilder();
        int remainCnt = WorldConstant.ATK_BOSS_CNT_EVERDAY.get(0) + WorldConstant.ATK_BOSS_CNT_EVERDAY.get(1) - player.getMixtureDataById(PlayerConstant.WORLD_SCHEDULE_ATK_BOSS_CNT);
        perosoal.setAtkBossCnt(remainCnt >= 0 ? remainCnt : 0);
        // 阵营、区域排行
        perosoal.addAllCampRank(worldScheduleRankService.campRank(player));
        // 个人排行
        perosoal.addAllPersonalRank(worldScheduleRankService.personRank(player));
        builder.setPersonalSchedule(perosoal.build());
        builder.setCurrentId(globalSchedule.getCurrentScheduleId());
        return builder.build();
    }

    /**
     * 获取世界进度的世界boss信息
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GetScheduleBossRs getScheduleBoss(long roleId, GetScheduleBossRq req) throws MwException {
        playerDataManager.checkPlayerIsExist(roleId);
        int remainHp = 0;
        WorldSchedule worldSchedule = getGlobalSchedule().getScheduleMap().get(getCurrentSchduleId());
        if (worldSchedule != null && worldSchedule.getBoss() != null) {
            remainHp = worldSchedule.getBoss().getRemainHp();
        }
        GetScheduleBossRs.Builder builder = GetScheduleBossRs.newBuilder();
        builder.setRemainHp(remainHp);
        return builder.build();
    }

    /**
     * 进攻世界boss
     */
    public GamePb4.AttckScheduleBossRs attackScheduleBoss(long roleId) throws MwException{
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // 检查等级是否开启
        // if (WorldConstant.ATK_BOSS_REQUIRE_LEVEL > player.lord.getLevel()) {
        //     throw new MwException(GameError.LV_NOT_ENOUGH.getCode(), "攻打世界boss等级不足, roleId:" + roleId);
        // }

        if (!player.isOnBattle()) {
            throw new MwException(GameError.HERO_NOT_BATTLE.getCode(), "攻打世界boss未上阵, roleId:" + roleId);
        }

        int curId = globalSchedule.getCurrentScheduleId();
        if (!bossSchedule(curId)) {
            throw new MwException(GameError.ATTACK_SCHEDULE_BOSS_PARAM_ERROR.getCode(),
                    "进攻世界boss, 当前不是世界boss进度, curId:", curId);
        }

        WorldSchedule bossSchedule = globalSchedule.getWorldSchedule(curId);
        if (CheckNull.isNull(bossSchedule) || CheckNull.isNull(bossSchedule.getBoss())) {
            throw new MwException(GameError.ATTACK_SCHEDULE_BOSS_PARAM_ERROR.getCode(), "进攻世界boss, 进攻的boss不存在, curId:",
                    curId);
        }

        ScheduleBoss boss = bossSchedule.getBoss();
        if (boss.getRemainHp() <= 0) {
            throw new MwException(GameError.ATTATCK_SCHEDULE_BOSS_IS_DEAD.getCode(), "进攻世界boss, 当前boss已死亡");
        }

        int atkCnt = player.getMixtureDataById(PlayerConstant.WORLD_SCHEDULE_ATK_BOSS_CNT);
        if (atkCnt >= WorldConstant.ATK_BOSS_CNT_EVERDAY.get(0) + WorldConstant.ATK_BOSS_CNT_EVERDAY.get(1)) {
            throw new MwException(GameError.ATTACK_SCHEDULE_BOSS_MAX_CNT.getCode(), "进攻世界boss, 进攻次数不足, atkCnt:",
                    atkCnt);
        }

        // 判断是否免费打
        if (atkCnt >= WorldConstant.ATK_BOSS_CNT_EVERDAY.get(0)) {
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD,
                    Constant.BUY_WORLD_BOSS_GOLD, AwardFrom.WORLD_BOSS_BUY, curId);
        }

        return doAtkWorldBoss(player, bossSchedule);
    }

    public GamePb4.AttckScheduleBossRs doAtkWorldBoss(Player player, WorldSchedule schedule) {
        int curId = schedule.getId();

        GamePb4.AttckScheduleBossRs.Builder builder = GamePb4.AttckScheduleBossRs.newBuilder();
        builder.setScheduleId(curId);

        Fighter attacker = fightService.createWorldBossPlayerFighter(player);
        ScheduleBoss boss = schedule.getBoss();
        int oldRemainHp = boss.getRemainHp();
        Fighter defender = fightService.createFighter(boss.getNpc());

        FightLogic fightLogic = new FightLogic(attacker, defender, true);
        fightLogic.fight();

        //貂蝉任务-杀敌阵亡数量
        ActivityDiaoChanService.killedAndDeathTask0(attacker,false,true);
        ActivityDiaoChanService.killedAndDeathTask0(defender,false,true);

        // 更新世界目标进度，攻打世界boss
        updateScheduleGoal(player, ScheduleConstant.GOAL_COND_ATTACK_BOSS, curId);
        // 记录当天攻打世界boss次数
        player.addMixtureData(PlayerConstant.WORLD_SCHEDULE_ATK_BOSS_CNT, 1);
        // 世界进程第五、九阶段（记录伤害高低排出阵营排名）
        worldScheduleRankService.addBossWorldScheduleRankData(curId, player.lord.getCamp(), player.lord.getArea(),
                attacker.hurt);

        boolean isSuccess = defender.getTotal() - defender.getLost() <= 0;
        builder.setResult(isSuccess);

        StaticSchedule staticSchedule = StaticWorldDataMgr.getScheduleById(curId);
        List<CommonPb.Award> awards = new ArrayList<>();

        // 战斗记录
        CommonPb.Record record = fightLogic.generateRecord();

        CommonPb.RptAtkBandit.Builder rpt = createAtkScheduleBossRpt(player, 0, curId, attacker, defender, record,
                player.lord, isSuccess);
        CommonPb.Report.Builder report = worldService.createAtkBanditReport(rpt.build(), TimeHelper.getCurrentSecond());

        // 有伤害
        if (attacker.hurt > 0) {
            // 增加个人伤害排行
            worldScheduleRankService.addBossWorldScheduleRankData(curId, player, attacker.hurt);
            // 每日对BOSS的累积伤害
            player.addMixtureData(PlayerConstant.DAILY_ATK_BOSS_VAL, attacker.hurt);
        }
        // 计算经验
        int addExp = 0;
        if (attacker.hurt > 0) {
            addExp = attacker.hurt / attacker.lost * WorldConstant.SCHEDULE_BOSS_AWARD.get(1);
            addExp = addExp > WorldConstant.SCHEDULE_BOSS_AWARD.get(2) ? WorldConstant.SCHEDULE_BOSS_AWARD.get(2) : addExp;
        }
        // 保底
        addExp = addExp > 0 ? addExp : WorldConstant.SCHEDULE_BOSS_AWARD.get(0);
        if (addExp > 0) {
            // 增加指挥官经验
            awards.add(rewardDataManager.addAwardSignle(player, AwardType.MONEY, AwardType.Money.EXP, addExp, AwardFrom.SCHEDULE_BOSS_AWARD));
        }

        if (attacker.lost > 0) {
            // 发送军工
            awards.add(rewardDataManager.addAwardSignle(player, AwardType.MONEY, AwardType.Money.EXPLOIT, attacker.lost, AwardFrom.SCHEDULE_BOSS_AWARD));
        }
        // 不论输赢都给奖励
        if (!CheckNull.isEmpty(staticSchedule.getBossDrop())) {
            List<Integer> randomAward = RandomUtil.getRandomByWeight(staticSchedule.getBossDrop(), 3, false);
            if (!CheckNull.isEmpty(randomAward)) {
                awards.add(rewardDataManager.addAwardSignle(player, randomAward.get(0), randomAward.get(1), randomAward.get(2), AwardFrom.SCHEDULE_BOSS_AWARD));
                mailDataManager.sendReportMail(player, report, MailConstant.MOLD_ATK_BOSS_AWARD, awards, TimeHelper.getCurrentSecond());
            }
        }

        builder.addAllAward(awards);

        // 给将领加经验
        builder.addAllAtkHero(rpt.getAtkHeroList());
        builder.setRecord(fightLogic.generateRecord());

        // 清楚boss的npc
        boss.getNpc().clear();

        int totalHp = defender.getTotal();
        int lostTotal = defender.getLost(); // 总损失兵力
        int curLost = oldRemainHp - (defender.getTotal() - lostTotal);// 本次损失兵力
        LogLordHelper.commonLog("worldBoss", AwardFrom.WORLD_TASK, player, curId, oldRemainHp, curLost, lostTotal,
                totalHp);
        if (isSuccess) {
            // 发送广播通知
            if (curId == ScheduleConstant.SCHEDULE_BOOS_1_ID) {
                // 开启军团官员功能
                campService.openPartyJobDelay();
                chatDataManager.sendSysChat(ChatConst.CHAT_WORLD_BOSS_1, player.lord.getCamp(),
                        0,ScheduleConstant.SCHEDULE_BOOS_1_ID);
                // 开启匪军叛乱
                initRebellion();
            } else if (curId == ScheduleConstant.SCHEDULE_BOOS_2_ID) {
                chatDataManager.sendSysChat(ChatConst.CHAT_WORLD_BOSS_2, player.lord.getCamp(),
                        0,ScheduleConstant.SCHEDULE_BOOS_2_ID);
            }
            // 处理刷新世界进度
            globalSchedule.processRefreshSchedule();
            // 同步世界进度
            syncSchedule();
            // 发放世界进度奖励 scheduleId
            worldScheduleRankService.worldScheduleRankAward(curId);
            // 世界boss被击杀，开启area
            globalDataManager.openAreaData(
                    curId == ScheduleConstant.SCHEDULE_BOOS_1_ID ? WorldConstant.AREA_ORDER_2 : WorldConstant.AREA_ORDER_3);
        } else {
            // 防守方损兵处理
            for (Force force : defender.forces) {
                if (force.alive()) {
                    boss.getNpc().add(new NpcForce(force.id, force.hp, force.curLine));
                }
            }
        }

        int remainCnt = WorldConstant.ATK_BOSS_CNT_EVERDAY.get(0) + WorldConstant.ATK_BOSS_CNT_EVERDAY.get(1)
                - player.getMixtureDataById(PlayerConstant.WORLD_SCHEDULE_ATK_BOSS_CNT);
        builder.setAtkBossCnt(Math.max(0, remainCnt));

        return builder.build();
    }

    /**
     * 进攻世界进度boss
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    /*public GamePb4.AttckScheduleBossRs attackScheduleBoss(long roleId, GamePb4.AttckScheduleBossRq req)
            throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int pos = req.getPos();
        List<Integer> ids = req.getHeroIdList();

        // 检查区域
        worldService.checkSameArea(player, pos);

        int atkCnt = player.getMixtureDataById(PlayerConstant.WORLD_SCHEDULE_ATK_BOSS_CNT);
        if (atkCnt >= WorldConstant.ATK_BOSS_CNT_EVERDAY) {
            throw new MwException(GameError.ATTACK_SCHEDULE_BOSS_MAX_CNT.getCode(), "进攻世界boss, 可进攻次数不足, atkCnt:",
                    atkCnt);
        }

        int curId = globalSchedule.getCurrentScheduleId();
        if (!bossSchedule(curId)) {
            throw new MwException(GameError.ATTACK_SCHEDULE_BOSS_PARAM_ERROR.getCode(),
                    "进攻世界boss, 当前不是世界boss进度, curId:", curId);
        }

        List<StaticScheduleBoss> scheduleBosses = StaticWorldDataMgr.getScheduleBossById(curId);
        if (CheckNull.isEmpty(scheduleBosses)) {
            throw new MwException(GameError.ATTACK_SCHEDULE_BOSS_PARAM_ERROR.getCode(),
                    "进攻世界boss, StaticScheduleBoss表配置出错, curId:", curId);
        }
        StaticScheduleBoss staticScheduleBoss = scheduleBosses.stream().filter(ssb -> ssb.getPos() == pos).findFirst()
                .orElse(null);
        if (CheckNull.isNull(staticScheduleBoss)) {
            throw new MwException(GameError.ATTACK_SCHEDULE_BOSS_PARAM_ERROR.getCode(), "进攻世界boss, 进攻的pos没有boss, pos:",
                    pos);
        }

        GamePb4.AttckScheduleBossRs.Builder builder = GamePb4.AttckScheduleBossRs.newBuilder();

        WorldSchedule bossSchedule = globalSchedule.getWorldSchedule(curId);
        if (!CheckNull.isNull(bossSchedule) && !CheckNull.isNull(bossSchedule.getBoss())) {
            ScheduleBoss boss = bossSchedule.getBoss();
            if (boss.getRemainHp() <= 0) {
                throw new MwException(GameError.ATTATCK_SCHEDULE_BOSS_IS_DEAD.getCode(), "进攻世界boss, 当前boss已死亡");
            }

            Hero hero;
            int armCount = 0;
            List<CommonPb.TwoInt> form = new ArrayList<>();
            for (Integer heroId : ids) {
                hero = player.heros.get(heroId);
                form.add(PbHelper.createTwoIntPb(heroId, hero.getCount()));
                armCount += hero.getCount();
            }

            // 行军时间
            int now = TimeHelper.getCurrentSecond();
            int marchTime = worldService.marchTime(player, pos);
            // 检查补给
            int needFood = worldService.checkMarchFood(player, marchTime, armCount);
            // 战斗消耗
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.RESOURCE, AwardType.Resource.FOOD, needFood,
                    AwardFrom.ATK_POS);

            Army army = new Army(player.maxKey(), ArmyConstant.ARMY_TYPE_ATTACK_SCHEDULE_BOSS, pos,
                    ArmyConstant.ARMY_STATE_MARCH, form, marchTime - 1, now + marchTime - 1);
            army.setLordId(roleId);
            army.setOriginPos(player.lord.getPos());
            player.armys.put(army.getKeyId(), army);

            // 添加行军路线
            March march = new March(player, army);
            worldDataManager.addMarch(march);

            // 改变行军状态
            for (Integer heroId : ids) {
                hero = player.heros.get(heroId);
                hero.setState(ArmyConstant.ARMY_STATE_MARCH);
            }

            builder.setArmy(PbHelper.createArmyPb(army, false));
            // 区域变化推送
            List<Integer> posList = MapHelper.getAreaStartPos(MapHelper.getLineAcorss(pos, player.lord.getPos()));
            posList.add(pos);
            posList.add(player.lord.getPos());
            EventBus.getDefault().post(
                    new Events.AreaChangeNoticeEvent(posList, roleId, Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));
        }

        return builder.build();
    }*/

    /**
     * 检测世界进度的boss状态
     *
     * @param curId
     * @param worldSchedule
     * @return
     */
    private boolean checkSchedBoss(int curId, WorldSchedule worldSchedule) {
        if (!bossSchedule(curId)) {
            LogUtil.debug("当前世界boss已被击杀, curId:", curId);
            return true;
        }

        if (CheckNull.isNull(worldSchedule)) {
            LogUtil.error("当前世界进度未初始化, curId:", curId);
            return true;
        }

        ScheduleBoss boss = worldSchedule.getBoss();
        if (CheckNull.isNull(boss) || boss.getRemainHp() <= 0) {
            LogUtil.error("当前世界进度未初始化或者boss血量为0, curId:", curId);
            return true;
        }
        return false;
    }

    /**
     * 获取世界boss状态,主要给 {@link WorldService#bossDeadState()} 调用,为了兼容原来的代码
     *
     * @return 0 第1个boss未打死, 1 第1个boss打死, 2 第2个boss打死 </br>
     *         对应的常量:{@link ScheduleConstant#BOSS_NO_DEAD} {@link ScheduleConstant#BOSS_1_DEAD}
     *         {@link ScheduleConstant#BOSS_2_DEAD}
     */
    public int getBossDeadState() {
        int curId = globalSchedule.getCurrentScheduleId();
        if (curId <= ScheduleConstant.SCHEDULE_BOOS_1_ID) {
            return ScheduleConstant.BOSS_NO_DEAD;
        } else if (ScheduleConstant.SCHEDULE_BOOS_1_ID < curId && curId <= ScheduleConstant.SCHEDULE_BOOS_2_ID) {
            return ScheduleConstant.BOSS_1_DEAD;
        } else {
            return ScheduleConstant.BOSS_2_DEAD;
        }
    }

    /**
     * 世界boss战斗处理
     *
     * @param player
     * @param army
     * @param now
     */
    /*public void fightSchedBossLogic(Player player, Army army, int now) {
        int pos = army.getTarget();
        Turple<Integer, Integer> xy = MapHelper.reducePos(pos);
        int curId = globalSchedule.getCurrentScheduleId();
        WorldSchedule worldSchedule = globalSchedule.getWorldSchedule(curId);

        if (checkSchedBoss(curId, worldSchedule)) {
            // 发送目标丢失的邮件
            mailDataManager.sendNormalMail(player, MailConstant.MOLD_ATK_TARGET_NOT_FOUND, now, xy.getA(), xy.getB(),
                    xy.getA(), xy.getB());
            // 部队返回
            worldService.retreatArmy(player, army, now);
            return;
        }
        int atkCnt = player.getMixtureDataById(PlayerConstant.WORLD_SCHEDULE_ATK_BOSS_CNT);
        if (atkCnt >= WorldConstant.ATK_BOSS_CNT_EVERDAY) {
            LogUtil.error("进攻世界boss, 可进攻次数不足, roleId:", player.roleId);
            // 发送可进攻次数不足
            mailDataManager.sendNormalMail(player, MailConstant.MOLD_ATK_BOSS_CNT_EVERDAY, now);
            // 部队返回
            worldService.retreatArmy(player, army, now);
            return;
        }

        // 战斗计算
        Fighter attacker = fightService.createFighter(player, army.getHero());
        Fighter defender = fightService.createFighter(worldSchedule.getBoss().getNpc());
        FightLogic fightLogic = new FightLogic(attacker, defender, true);
        fightLogic.fight();

        // 更新世界目标进度, 攻打boss防线
        updateScheduleGoal(player, ScheduleConstant.GOAL_COND_ATTACK_BOSS, curId);
        // 记录当天攻打世界boss次数
        player.setMixtureData(PlayerConstant.WORLD_SCHEDULE_ATK_BOSS_CNT, player.getMixtureDataById(PlayerConstant.WORLD_SCHEDULE_ATK_BOSS_CNT) + 1);
        // 世界进程第五、九阶段（记录伤害高低排出阵营排名）
        worldScheduleRankService.addBossWorldScheduleRankData(curId, player.lord.getCamp(), player.lord.getArea(),
                attacker.hurt);
         LogLordHelper.commonLog("worldBoss",AwardFrom.WORLD_TASK,player,curId,
                 worldSchedule.getBoss().getRemainHp(),attacker.hurt);
        // 记录玩家有改变的资源类型, key:roleId
        Map<Long, ChangeInfo> changeMap = new HashMap<>();
        // 兵力恢复
        List<CommonPb.Award> recoverArmyAward = new ArrayList<>();
        // 兵力恢复
        Map<Long, List<CommonPb.Award>> recoverArmyAwardMap = new HashMap<>();

        // 损兵处理
        if (attacker.lost > 0) {
            worldService.subHeroArm(player, attacker.forces, AwardFrom.ATTACK_BANDIT, changeMap);
            // 损兵排行
            activityDataManager.updRankActivity(player, ActivityConst.ACT_ARMY_RANK, attacker.lost);
            // 荣耀日报损兵进度
            honorDailyDataManager.addAndCheckHonorReport2s(player, HonorDailyConstant.COND_ID_14, attacker.lost);
            // 执行勋章白衣天使特技逻辑
            medalDataManager.angelInWhite(attacker, recoverArmyAwardMap);
            if (!CheckNull.isEmpty(recoverArmyAwardMap)) {
                List<CommonPb.Award> awards = recoverArmyAwardMap.get(player.roleId);
                if (!CheckNull.isEmpty(awards)) {
                    recoverArmyAward.addAll(awards);
                }
            }
        }

        // 战斗记录
        CommonPb.Record record = fightLogic.generateRecord();

        Lord lord = player.lord;
        boolean isSuccess = fightLogic.getWinState() == ArmyConstant.FIGHT_RESULT_SUCCESS;

        CommonPb.RptAtkBandit.Builder rpt = createAtkScheduleBossRpt(player, pos, curId, attacker, defender, record,
                lord, isSuccess);
        CommonPb.Report.Builder report = worldService.createAtkBanditReport(rpt.build(), now);

        // 清除boss的npc
        worldSchedule.getBoss().getNpc().clear();
        if (isSuccess) {
            List<StaticScheduleBoss> scheduleBosses = StaticWorldDataMgr.getScheduleBossById(curId);
            // 通知周围玩家
            List<Integer> posList = scheduleBosses.stream().map(StaticScheduleBoss::getPos).collect(Collectors.toList());
            // 通知其他玩家数据改变
            EventBus.getDefault()
                    .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_TYPE));
            // 发送广播通知
            if (curId == ScheduleConstant.SCHEDULE_BOOS_1_ID) {
                // 开启匪军叛乱
                initRebellion();
                // 开启军团官员功能
                campService.openPartyJobDelay();
                chatDataManager.sendSysChat(ChatConst.CHAT_WORLD_BOSS_1, player.lord.getCamp(),
                        0,ScheduleConstant.SCHEDULE_BOOS_1_ID);
            } else if (curId == ScheduleConstant.SCHEDULE_BOOS_2_ID) {
                chatDataManager.sendSysChat(ChatConst.CHAT_WORLD_BOSS_2, player.lord.getCamp(),
                        0,ScheduleConstant.SCHEDULE_BOOS_2_ID);
            }
            // 处理刷新世界进度
            globalSchedule.processRefreshSchedule();
            // 放世界进度奖励 scheduleId
            worldScheduleRankService.worldScheduleRankAward(curId);
            // 世界boss被击杀, 开启area
            globalDataManager.openAreaData(curId == ScheduleConstant.SCHEDULE_BOOS_1_ID ? WorldConstant.AREA_ORDER_2 : WorldConstant.AREA_ORDER_3);
        } else {
            // 防守方损兵处理
            for (Force force : defender.forces) {
                if (force.alive()) {
                    worldSchedule.getBoss().getNpc().add(new NpcForce(force.id, force.hp, force.curLine));
                }
            }
        }
        List<CommonPb.Award> dropList = new ArrayList<>();
        // 发送进攻世界boss奖励
        if (!CheckNull.isEmpty(WorldConstant.SCHEDULE_BOSS_AWARD)) {
            for (List<Integer> list : WorldConstant.SCHEDULE_BOSS_AWARD) {
                CommonPb.Award award = rewardDataManager.addAwardSignle(player, list, AwardFrom.SCHEDULE_BOSS_AWARD);
                if (CheckNull.isNull(award)) {
                    continue;
                }
                dropList.add(award);
                ChangeInfo info = changeMap.get(player.roleId);
                if (null == info) {
                    info = ChangeInfo.newIns();
                    changeMap.put(player.roleId, info);
                }
                info.addChangeType(award.getType(), award.getId());
            }
        }
        // 发送攻打世界boss的战报邮件
        sendAtkScheduleBossMail(player, now, pos, curId, recoverArmyAward, lord, isSuccess, report, dropList);
        // 返回玩家army, 并且同步
        worldService.retreatArmy(player, army, now);
        worldService.synRetreatArmy(player, army, now);
        // 通知客户端玩家资源变化
        worldService.sendRoleResChange(changeMap);
    }*/

    /**
     * 初始化匪军叛乱: 两种情况, 正常打死的第一个世界boss, 修复老服的匪军叛乱
     */
    public void initRebellion() {
        // 当前的时间
        Date today = new Date();
        // 匪军叛乱的配置
        List<List<Integer>> timeCfg = Constant.REBEL_START_TIME_CFG;
        if (CheckNull.isEmpty(timeCfg)) {
            LogUtil.error("打死第一个世界boss初始化匪军叛乱，配置有问题！！！");
            return;
        }
        // 周几
        int weekCfg = timeCfg.get(1).get(0);
        // 开启天数
        Date openTimeDate = TimeHelper.getDayOfWeekByDate(today, weekCfg);
        // 过了0点啦
        if (today.after(openTimeDate)) {
            openTimeDate = TimeHelper.getSomeDayAfterOrBerfore(openTimeDate, 7, 20, 0, 0);
        } else {
            // 客户端需要显示倒计时
            openTimeDate = TimeHelper.getSomeDayAfterOrBerfore(openTimeDate, 0, 20, 0, 0);
        }
        Map<Integer, Integer> nextOpenMap = globalDataManager.getGameGlobal().getMixtureDataById(GlobalConstant.REBEL_NEXT_OPEN_TIME);
        int value = TimeHelper.dateToSecond(openTimeDate);
        nextOpenMap.put(0, value);
        globalDataManager.getGameGlobal().setMixtureData(GlobalConstant.REBEL_NEXT_OPEN_TIME, nextOpenMap);
    }

    /**
     * 发送攻打世界boss的邮件
     *
     * @param player
     * @param now
     * @param pos
     * @param curId
     * @param recoverArmyAward
     * @param lord
     * @param isSuccess
     * @param report
     * @param dropList
     */
    private void sendAtkScheduleBossMail(Player player, int now, int pos, int curId,
            List<CommonPb.Award> recoverArmyAward, Lord lord, boolean isSuccess, CommonPb.Report.Builder report,
            List<CommonPb.Award> dropList) {
        Turple<Integer, Integer> xy;
        List<String> tParam = new ArrayList<>();
        tParam.add(lord.getNick());
        tParam.add(String.valueOf(curId));
        List<String> cParam = new ArrayList<>();
        xy = MapHelper.reducePos(lord.getPos());
        cParam.add(lord.getNick());
        cParam.add(String.valueOf(xy.getA()));
        cParam.add(String.valueOf(xy.getB()));
        cParam.add(String.valueOf(curId));
        xy = MapHelper.reducePos(pos);
        cParam.add(String.valueOf(xy.getA()));
        cParam.add(String.valueOf(xy.getB()));
        mailDataManager.sendReportMail(player, report,
                isSuccess ? MailConstant.MOLD_ATK_SCHEDULE_BOSS_SUC : MailConstant.MOLD_ATK_SCHEDULE_BOSS_FAIL,
                dropList, now, tParam, cParam, recoverArmyAward);
    }

    /**
     * 创建攻打世界boss的Rpt对象
     *
     * @param player
     * @param pos
     * @param curId
     * @param attacker
     * @param defender
     * @param record
     * @param lord
     * @param isSuccess
     * @return
     */
    private CommonPb.RptAtkBandit.Builder createAtkScheduleBossRpt(Player player, int pos, int curId, Fighter attacker,
            Fighter defender, CommonPb.Record record, Lord lord, boolean isSuccess) {
        CommonPb.RptAtkBandit.Builder rpt = CommonPb.RptAtkBandit.newBuilder();
        rpt.setResult(isSuccess);
        rpt.setAttack(PbHelper.createRptMan(lord.getPos(), lord.getNick(), lord.getVip(), lord.getLevel()));
        rpt.setDefend(PbHelper.createRptBandit(curId, pos));
        rpt.setAtkSum(PbHelper.createRptSummary(attacker.total, attacker.lost, lord.getCamp(), lord.getNick(),
                lord.getPortrait(), player.getDressUp().getCurPortraitFrame()));
        rpt.setDefSum(PbHelper.createRptSummary(defender.total, defender.lost, 0, null, -1, -1));
        // 给将领加经验
        rpt.addAllAtkHero(fightSettleLogic.banditFightHeroExpReward(player, attacker.forces));
        for (Force force : defender.forces) {
            rpt.addDefHero(
                    PbHelper.createRptHero(Constant.Role.BANDIT, force.killed, 0, force.id, null, 0, 0, force.lost));
        }
        rpt.setRecord(record);
        return rpt;
    }

    /**
     * 领取限时目标奖励
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GamePb4.GainGoalAwardRs gainGoalAward(long roleId, GamePb4.GainGoalAwardRq req) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        if (CheckNull.isNull(globalSchedule)) {
            throw new MwException(GameError.GIAN_GOAL_AWARD_ERROR.getCode(), "领取限时目标奖励, GlobalSchedule未初始化");
        }

        int goalId = req.getGoal();

        StaticScheduleGoal sScheduleGoal = StaticWorldDataMgr.getScheduleGoalById(goalId);
        if (CheckNull.isNull(sScheduleGoal)) {
            throw new MwException(GameError.WORLD_SCHEDULE_CONF_ERROR.getCode(), "领取限时目标奖励, 限时目标未配置, goalId:", goalId);

        }

        WorldSchedule worldSchedule = globalSchedule.getWorldSchedule(sScheduleGoal.getScheduleId());

        boolean award = false;
        switch (worldSchedule.getStatus()) {
            case ScheduleConstant.SCHEDULE_STATUS_NOT_YET_BEGIN:
                throw new MwException(GameError.GIAN_GOAL_AWARD_ERROR.getCode(), "领取限时目标奖励, 限时目标还未开始, scheduleId:",
                        sScheduleGoal.getScheduleId());
            case ScheduleConstant.SCHEDULE_STATUS_FINISH:
                // 世界进度, 已经结束, 需要判断是否可以结束后领取
                if (sScheduleGoal.canFinishGain()) {// 结束后可以领奖
                    award = true;
                } else {// 结束后看是否打成了条件，达成条件也可以领取奖励
                    award = worldSchedule.enoughSchedule(player, sScheduleGoal);
                }
                break;
            case ScheduleConstant.SCHEDULE_STATUS_PROGRESS:
                // 世界进度, 正在进行, 需要判断可否领取限时目标
                award = worldSchedule.enoughSchedule(player, sScheduleGoal);
                break;
            default:
                break;
        }

        ScheduleGoal scheduleGoal = worldSchedule.getGoal().get(goalId);
        if (CheckNull.isNull(scheduleGoal)) {
            throw new MwException(GameError.GIAN_GOAL_AWARD_ERROR.getCode(), "领取限时目标奖励, scheduleGoal未初始化");
        }

        GamePb4.GainGoalAwardRs.Builder builder = GamePb4.GainGoalAwardRs.newBuilder();

        // 没有领奖
        if (award && !worldSchedule.alreadyReward(player, scheduleGoal)) {
            // 领奖限时目标
            for (List<Integer> list : sScheduleGoal.getAward()) {
                builder.addAward(rewardDataManager.addAwardSignle(player, list, AwardFrom.SCHEDULE_GOAL_COND_AWARD,
                    sScheduleGoal.getScheduleId(), sScheduleGoal.getId()));
            }
            scheduleGoal.receiveAward(roleId);
        }
        return builder.build();
    }

    private HashMap<Integer, SchedCondHandler> schedCondHandlerMap;

    private interface SchedCondHandler {

        /**
         * 获取当前配置的目标完成进度
         *
         * @param ssg goal配置信息
         * @return 完成进度
         */
        int achieveGoal(StaticScheduleGoal ssg);
    }

    {
        schedCondHandlerMap = new HashMap<>(5);
        schedCondHandlerMap.put(ScheduleConstant.GOAL_COND_COMMAND_LV, this::achieveGoalCommandLv);
        schedCondHandlerMap.put(ScheduleConstant.GOAL_COND_CONQUER_CITY, this::achieveGoalConquerCity);
        schedCondHandlerMap.put(ScheduleConstant.GOAL_COND_HERO_DECORATED, this::achieveGoalHeroDecorated);
    }

    /**
     * 获取指定配置的目标完成进度
     *
     * @param ssg
     * @return
     */
    public int achieveGoal(StaticScheduleGoal ssg, Player player) {
        if (!CheckNull.isEmpty(schedCondHandlerMap)) {
            SchedCondHandler hander = schedCondHandlerMap.get(ssg.getCond());
            if (!CheckNull.isNull(hander)) {
                return hander.achieveGoal(ssg);
            } else {
                return ssg.getCond() == ScheduleConstant.GOAL_COND_FIGHT
                        ? CheckNull.isNull(player) ? 0 : (int) player.lord.getFight() : 0;
            }
        }
        return 0;
    }


    /**
     * 全服有N个N次觉醒英雄
     * @param ssg 进程限时目标配置
     * @return 达成条件的觉醒将领数量
     */
    private int achieveGoalHeroDecorated(StaticScheduleGoal ssg) {
        int lv = ssg.getCondId();
        return playerDataManager.getPlayers().values().stream().mapToInt(p -> (int) p.heros.values().stream().filter(hero -> hero.getDecorated() >= lv).count()).sum();
    }

    /**
     * 当前配置的指挥官基地完成进度
     *
     * @param ssg
     * @return
     */
    public int achieveGoalCommandLv(StaticScheduleGoal ssg) {
        int lv = ssg.getCondId();
        return (int) playerDataManager.getPlayers().values().stream()
                .filter(p -> BuildingDataManager.getBuildingLv(BuildingType.COMMAND, p) >= lv).count();
    }

    /**
     * 当前配置的城市被攻克完成进度
     *
     * @param ssg
     * @return
     */
    private int achieveGoalConquerCity(StaticScheduleGoal ssg) {
        int cityType = ssg.getCondId();
        return (int) worldDataManager.getCityMap().values().stream()
                .filter(city -> !city.isNpcCity())
                .map(city -> StaticWorldDataMgr.getCityMap().get(city.getCityId()))
                .filter(city -> !CheckNull.isNull(city) && city.getType() == cityType)
                .distinct().count();
    }

    /**
     * 获取世界进度,需要初始化之后获取,在 {@link WorldScheduleService#init()} 之后掉用
     *
     * @return
     */
    public GlobalSchedule getGlobalSchedule() {
        return globalSchedule;
    }

    /**
     * 获取当前任务进度
     *
     * @return
     */
    public int getCurrentSchduleId() {
        GlobalSchedule gs = getGlobalSchedule();
        if (gs != null) {
            return gs.getCurrentScheduleId();
        }
        return 1;
    }

    /**
     * 清除boss位置上的点
     */
    public void clearBossPos() {
        StaticWorldDataMgr.getBossPosSet().forEach(pos -> worldService.clearPos(pos));
    }
}
