package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticLightningWarDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.BattleRole;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Fighter;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.ActivityBase;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.s.StaticArea;
import com.gryphpoem.game.zw.resource.domain.s.StaticCity;
import com.gryphpoem.game.zw.resource.domain.s.StaticLightningWar;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.army.March;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.world.Battle;
import com.gryphpoem.game.zw.resource.pojo.world.LightningWarBoss;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author: ZhouJie
 * @date: Create in 2018-05-15 19:42
 * @description: 闪电战消息推送, 战斗逻辑, 加入闪电战, 同步闪电战信息
 * @modified By:
 */
@Service
public class LightningWarService {

    @Autowired
    private FightService fightService;

    @Autowired
    private WorldService worldService;

    @Autowired
    private WarService warService;

    @Autowired
    private WorldDataManager worldDataManager;

    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private ActivityDataManager activityDataManager;

    @Autowired
    private WarDataManager warDataManager;

    @Autowired
    private RewardDataManager rewardDataManager;

    @Autowired
    private MailDataManager mailDataManager;

    @Autowired
    private SolarTermsDataManager solarTermsDataManager;

    @Autowired
    private HonorDailyDataManager honorDailyDataManager;

    @Autowired
    private MedalDataManager medalDataManager;

    @Autowired
    private ChatDataManager chatDataManager;

    @Autowired
    private CounterAtkService counterAtkService;

    @Autowired
    private BattlePassDataManager battlePassDataManager;
    @Autowired
    private SeasonTalentService seasonTalentService;

    public void sendchat(int chatType, int cnt) {
        int now = TimeHelper.getCurrentSecond();
        StaticLightningWar lightningWar = StaticLightningWarDataMgr.getLightningWar();
        if (CheckNull.isNull(lightningWar)) {
            return;
        }
        switch (chatType) {
            case ActivityConst.ACT_BEGIN:
                chatDataManager.sendSysChat(ChatConst.CHAT_LIGHTNING_WAR_BEGIN, 0, 0);
                break;
            case ActivityConst.ATTACK_CNT:
                worldDataManager.getLightningWarBossMap().values().forEach(boss -> {
                    if (!CheckNull.isNull(boss)) {
                        if (!boss.isNotInitOrDead()) {
                            int area = MapHelper.getAreaIdByPos(boss.getPos());
                            // 闪电战战斗几轮推送
                            chatDataManager.sendSysChat(ChatConst.CHAT_ATK_CHAT, area, 0, cnt);
                            // 每15分钟重开一次战斗(10分钟战斗等待时间,5分钟空闲时间)
                            if (!warDataManager.getBattlePosMap().containsKey(boss.getPos())) {
                                createLightningWarBattle(now, lightningWar, boss);
                            }
                        }
                    }
                });
                break;
            case ActivityConst.ACT_END:
                worldDataManager.getLightningWarBossMap().values().forEach(boss -> {
                    // 活动时间结束,boss没有被击杀
                    if (!boss.isNotInitOrDead()) {
                        int area = MapHelper.getAreaIdByPos(boss.getPos());
                        chatDataManager.sendSysChat(ChatConst.CHAT_BOSS_NOT_DEAD, 0, 0, area);
                    }
                });
                break;
            case ActivityConst.ACT_ANN:
                chatDataManager.sendSysChat(ChatConst.CHAT_LIGHTNING_WAR_ANN, 0, 0, cnt);
                break;
        }
    }

    /**
     * 消息推送定时处理(创建battle对象,同步客户端boss详情)
     */
    public void sendChatLogic() {
        int now = TimeHelper.getCurrentSecond();
        StaticLightningWar lightningWar = StaticLightningWarDataMgr.getLightningWar();
        if (CheckNull.isNull(lightningWar)) {
            return;
        }
        // 发送推送的次数
        int cnt = worldDataManager.currentSendChatCnt(WorldConstant.CHAT_TIME_KEY);
        ActivityBase actBase = StaticActivityDataMgr.getActivityByTypeIgnoreStep(ActivityConst.ACT_LIGHTNING_WAR);
        Date date = new Date(now * 1000L);
        if (DateHelper.dayiy(actBase.getBeginTime(), date) == 1) {
            if (cnt == 0) {
                if (lightningWar.isInBanAttackTime(now) && DateHelper.isAfterTime(date, lightningWar.getChatTime())) {
                    // 活动开启时推送
                    chatDataManager.sendSysChat(ChatConst.CHAT_LIGHTNING_WAR_BEGIN, 0, 0);
                    worldDataManager.updSendChatCnt(WorldConstant.CHAT_TIME_KEY, 1);
                    worldDataManager.initLightningWarBoss(lightningWar);
                }
            } else if (cnt > 0) {
                for (LightningWarBoss boss : worldDataManager.getLightningWarBossMap().values()) {
                    final int conTime = lightningWar.getIntervalTime() + lightningWar.getBattleTime();
                    Date nextSend = DateHelper.afterSecondDate(lightningWar.getStartTime(), conTime * (cnt - 1));
                    if (lightningWar.isInOpenTime(now) && DateHelper.isAfterTime(date, nextSend) && !CheckNull
                            .isNull(boss)) {
                        int chatCnt = worldDataManager.currentSendChatCnt(boss.getId());
                        if (!boss.isNotInitOrDead() && chatCnt < cnt) {
                            int area = MapHelper.getAreaIdByPos(boss.getPos());
                            // 闪电战战斗几轮推送
                            chatDataManager.sendSysChat(ChatConst.CHAT_ATK_CHAT, area, 0, cnt);
                            // worldDataManager.updSendChatCnt(WorldConstant.CHAT_TIME_KEY, 1);
                            worldDataManager.updSendChatCnt(boss.getId(), 1);
                            // 每15分钟重开一次战斗(10分钟战斗等待时间,5分钟空闲时间)
                            createLightningWarBattle(now, lightningWar, boss);
                        }
                    } else if (DateHelper.isAfterTime(date, lightningWar.getEndTime()) && !CheckNull.isNull(boss)) {
                        if (boss.isNotInitOrDead()) {
                            chatDataManager.sendSysChat(ChatConst.CHAT_BOSS_NOT_DEAD, 0, 0, boss.getId());
                        }
                    }
                }
            }
        }
        // for (Player player : playerDataManager.getPlayers().values()) {
        // LightningWarBoss lightningWarBoss = worldDataManager.getLightningWarBossByArea(player.lord.getArea());
        // Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_LIGHTNING_WAR);
        // final int conTime = lightningWar.getIntervalTime() + lightningWar.getBattleTime();
        // Date nextSend = DateHelper.afterSecondDate(lightningWar.getStartTime(), conTime * (cnt - 1));
        // if (!CheckNull.isNull(activity) && lightningWar.isInOpenTime(now)
        // && DateHelper.isAfterTime(date, nextSend) && !CheckNull.isNull(lightningWarBoss)) {
        // // 首次创建Fighter对象
        // if (CheckNull.isNull(lightningWarBoss.getFighter())) {
        // lightningWarBoss.setFighter(fightService.createLightningWarBossDefencer(lightningWar.getFormList()));
        // }
        // if (lightningWarBoss.isNotInitOrDead()) {
        // // 闪电战战斗几轮推送
        // chatService.sendSysChat(ChatConst.CHAT_ATK_CHAT, 0, 1, cnt);
        // worldDataManager.updSendChatCnt(WorldConstant.CHAT_TIME_KEY, 1);
        // // 每15分钟重开一次战斗(10分钟战斗等待时间,5分钟空闲时间)
        // createLightningWarBattle(now, lightningWar, player, lightningWarBoss);
        // }
        // }
        // }

    }

    /**
     * 创建闪电战战斗对象
     *
     * @param now
     * @param lightningWar
     */
    private void createLightningWarBattle(int now, StaticLightningWar lightningWar, LightningWarBoss boss) {
        Battle battle = new Battle();
        battle.setType(WorldConstant.BATTLE_TYPE_LIGHTNING_WAR);
        battle.setBattleTime(now + lightningWar.getBattleTime());
        battle.setBeginTime(now);
        battle.setPos(boss.getPos());
        battle.setDefCamp(Constant.Camp.NPC);
        battle.setDefArm(boss.currentHp());
        warDataManager.addSpecialBattle(battle);
        syncLightningWarBattle(battle, boss);
    }

    /**
     * 通知客户端闪电战信息
     *
     * @param battle
     * @param lightningWarBoss
     */
    private void syncLightningWarBattle(Battle battle, LightningWarBoss lightningWarBoss) {
        GamePb4.SyncLightningWarRs.Builder builder = GamePb4.SyncLightningWarRs.newBuilder();
        builder.setBattle(PbHelper.createBattlePb(battle));
        BasePb.Base.Builder msg = PbHelper
                .createSynBase(GamePb4.SyncLightningWarRs.EXT_FIELD_NUMBER, GamePb4.SyncLightningWarRs.ext,
                        builder.build());
        int areaId = MapHelper.getAreaIdByPos(battle.getPos());
        StaticArea staticArea = StaticWorldDataMgr.getAreaMap().get(areaId);
        List<Integer> areaIdList = staticArea.getUnlockArea();// 通知所有与本区已开通关联的区域内，相关联的玩家

        ConcurrentHashMap<Long, Player> playerMap = playerDataManager.getPlayerByAreaList(areaIdList);
        for (Player player : playerMap.values()) {
            if (player.isLogin) {
                MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
            }
        }
    }

    /**
     * 特殊战斗的执行定时任务
     */
    public void batlleTimeLogic() {
        Battle battle;
        int now = TimeHelper.getCurrentSecond();
        // 移除的BattleId
        Set<Integer> removeBattleIdSet = new HashSet<>();
        Iterator<Battle> its = warDataManager.getSpecialBattleMap().values().iterator();
        while (its.hasNext()) {
            battle = its.next();
            try {
                if (now >= battle.getBattleTime()) {
                    if (battle.isLightningWar()) {
                        lightningWarFightLogic(battle, now, removeBattleIdSet);
                    } else if (battle.isCounterAtkBattle()) {
                        counterAtkService.processBattleLogic(battle, now, removeBattleIdSet);
                    }
                }
            } catch (Exception e) {
                LogUtil.error(e, "战争定时处理任务出现异常， battle:", battle);
                // 部队返回
                removeBattleIdSet.add(battle.getBattleId());
            }
        }

        for (Integer battleId : removeBattleIdSet) {
            battle = warDataManager.removeSpecialBattleById(battleId);
            LogUtil.debug("移除battleId=" + battleId);
            warService.retreatBattleArmy(battle, now);// 部队没有返回的立即返回
            warService.removePlayerJoinBattle(battle);// 移除玩家参与记录
        }
    }

    /**
     * 单场闪电战boss战斗逻辑,并生成战报和推送 注意要点：战前会自动补兵，按顺序生成在城内的上阵将领，驻防军的战斗数据， 战斗结束后，如果boss被击杀,发送击杀奖励,发送击杀跑马灯,并结束当前区域的活动,并返回Army
     *
     * @param battle
     * @param now
     * @param removeBattleIdSet
     * @throws MwException
     */
    public void lightningWarFightLogic(Battle battle, int now, Set<Integer> removeBattleIdSet) throws MwException {
        if (null == battle)
            return;
        LogUtil.debug("开始战斗, battle:", battle);
        int pos = battle.getPos();
        CommonPb.RptAtkPlayer.Builder rpt = CommonPb.RptAtkPlayer.newBuilder();

        worldDataManager.updSendChatCnt(WorldConstant.CHAT_TIME_KEY, 1);
        Fighter defender = null;
        int area = WorldDataManager.getAreaIdByPos(pos);
        LightningWarBoss boss = worldDataManager.getLightningWarBossByArea(area);
        if (CheckNull.isNull(boss)) {
            LogUtil.error("未找到闪电战boss, pos:", pos);
            // 部队返回
            removeBattleIdSet.add(battle.getBattleId());
            return;
        }
        if (boss.isNotInitOrDead()) {
            LogUtil.error("闪电战boss未初始化,或已被击杀, pos:", pos);
            // 部队返回
            removeBattleIdSet.add(battle.getBattleId());
            return;
        }
        StaticLightningWar lightningWar = StaticLightningWarDataMgr.getLightningWar();
        if (CheckNull.isNull(lightningWar)) {
            LogUtil.error("闪电战boss未找到配置, pos:", pos);
            // 部队返回
            removeBattleIdSet.add(battle.getBattleId());
            return;
        }
        StaticCity staticCity = StaticWorldDataMgr.getCityByPos(pos);
        if (CheckNull.isNull(staticCity)) {
            LogUtil.error("创建闪电战Fighter， 未找到城池配置, pos:", pos);
            // 部队返回
            removeBattleIdSet.add(battle.getBattleId());
            return;
        }
        if (CheckNull.isEmpty(battle.getAtkList())) {
            LogUtil.error("没有进攻方", pos);
            // 部队返回
            removeBattleIdSet.add(battle.getBattleId());
            return;
        }
        defender = fightService.createBossNpcForce(boss.getFighter());
        Fighter attacker = fightService.createMultiPlayerFighter(battle, null);
        LogUtil.debug("defender=" + defender + ",attacker=" + attacker);
        FightLogic fightLogic = new FightLogic(attacker, defender, true, battle.getType());
        warDataManager.packForm(fightLogic.getRecordBuild(), attacker.forces, defender.forces);

        int totalHp = defender.getTotal();// 总血量
        int lostTotal = defender.getLost();// 总损失兵力

        fightLogic.start();// 战斗

        //貂蝉任务-杀敌阵亡数量
        ActivityDiaoChanService.killedAndDeathTask0(attacker, false, true);
        ActivityDiaoChanService.killedAndDeathTask0(defender, false, true);

        boolean atkSuccess = fightLogic.getWinState() == FightConstant.FIGHT_RESULT_SUCCESS;
        // 记录玩家有改变的资源类型, key:roleId
        Map<Long, ChangeInfo> changeMap = new HashMap<>();
        // 兵力恢复
        Map<Long, List<CommonPb.Award>> recoverArmyAwardMap = new HashMap<>();
        // 损兵处理
        if (attacker.lost > 0) {
            if (battle.isLightningWar()) {
                warService.subBattleHeroArm(attacker.forces, changeMap, AwardFrom.LIGHTNING_WAR_ATTACK);
                //执行勋章白衣天使特技逻辑
                medalDataManager.angelInWhite(attacker, recoverArmyAwardMap);
                //执行赛季天赋技能---伤病恢复
                seasonTalentService.execSeasonTalentEffect303(attacker, recoverArmyAwardMap);
                for (CommonPb.BattleRole battleRole : battle.getAtkList()) {
                    long roleId = battleRole.getRoleId();
                    Player player = playerDataManager.getPlayer(roleId);
                    // 损兵排行
                    activityDataManager.updRankActivity(player, ActivityConst.ACT_ARMY_RANK, attacker.lost);
                    // 荣耀日报损兵进度
                    honorDailyDataManager
                            .addAndCheckHonorReport2s(player, HonorDailyConstant.COND_ID_14, attacker.lost);
                    // 战令的损兵进度
                    battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_SUB_HERO_ARMY, attacker.lost);

                }
            }
        }

        CommonPb.Record record = fightLogic.generateRecord();
        rpt.setNightEffect(solarTermsDataManager.getNightEffect() != null);
        rpt.setResult(atkSuccess);
        rpt.setRecord(record);
        // // 记录双方汇总信息
        rpt.setAtkSum(PbHelper.createRptSummary(attacker.total, attacker.lost, 0, null, 0, 0));
        rpt.setDefCity(PbHelper.createRptCityPb(boss.getId(), pos));
        rpt.setDefSum(
                PbHelper.createRptSummary(totalHp - lostTotal, defender.lost - lostTotal, battle.getDefCamp(), null,
                        0, 0));

        LinkedList<Battle> list = warDataManager.getBattlePosMap().get(battle.getPos());
        Map<Long, List<CommonPb.Award>> awardProp = new HashMap<>();
        List<CommonPb.Award> tmp;
        HashMap<Long, List<List<Integer>>> attackAward = new HashMap<>();

        // 参与的进攻方,根据杀敌数 * 比例发送奖励(打印日志)
        addAttackReaward(attacker.forces, AwardFrom.LIGHTNING_WAR_ATTACK, attackAward, defender, boss, battle,
                defender.total, defender.lost - lostTotal);

        Set<Long> ids = new HashSet<>();
        for (BattleRole battleRole : battle.getAtkList()) {
            long roleId = battleRole.getRoleId();
            if (ids.contains(roleId))
                continue;
            ids.add(roleId);
            Player player = playerDataManager.getPlayer(roleId);
            List<List<Integer>> awards = RewardDataManager.mergeAward(attackAward.get(roleId));
            if (!CheckNull.isEmpty(awards)) {
                List<CommonPb.Award> temp = rewardDataManager
                        .sendReward(player, awards, AwardFrom.LIGHTNING_WAR_REWARD);
                if (temp != null && awardProp != null) {
                    awardProp.put(roleId, temp);
                }
            }
        }

        // 给将领加经验=（杀敌数+损兵数）/2，并记录双方将领信息
        if (battle.isLightningWar()) {
            warService.addBattleHeroExp(attacker.forces, AwardFrom.LIGHTNING_WAR_ATTACK, rpt, true, false, false,
                    changeMap, true, null);
            warService.addBattleHeroExp(defender.forces, AwardFrom.LIGHTNING_WAR_DEFEND, rpt, false, false, false,
                    changeMap, false, null);
            if (atkSuccess) {
                // 击杀奖励
                battle.getAtkList().forEach(battleRole -> {
                    long roleId = battleRole.getRoleId();
                    Player player = playerDataManager.getPlayer(roleId);
                    List<CommonPb.Award> temp = rewardDataManager
                            .sendReward(player, lightningWar.getKillAward(), AwardFrom.LIGHTNING_WAR_BOSS_DEAD_REWARD);
                    mailDataManager.sendAttachMail(player, temp, MailConstant.MOLD_ATK_LIGHTNING_WAR_BOSS_SUCC,
                            AwardFrom.LIGHTNING_WAR_BOSS_DEAD_REWARD, now, player.lord.getNick(),
                            staticCity.getCityId());
                });
                // 城战攻方胜利后，后续城战取消
                for (Battle battle2 : list) {
                    LogUtil.debug("移除后续城战battleId=" + battle2.getBattleId());
                    removeBattleIdSet.add(battle2.getBattleId());
                }
                // boss被击杀
                boss.setStatus(WorldConstant.BOSS_STATUS_DEAD);
                // 击杀跑马灯
                chatDataManager.sendSysChat(ChatConst.CHAT_BOSS_DEAD, 0, 0, staticCity.getArea());
            }
            // 移除
            removeBattleIdSet.add(battle.getBattleId());

            CommonPb.Report.Builder report = worldService.createAtkPlayerReport(rpt.build(), now);
            // 发送邮件通知
            sendLightningWarBattleMail(battle, staticCity, atkSuccess, report, awardProp, now, recoverArmyAwardMap);

            // 通知周围玩家
            List<Integer> posList = new ArrayList<>();
            posList.add(boss.getPos());
            // 通知其他玩家数据改变
            EventBus.getDefault()
                    .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));
        }
        boss.setFighter(defender);
        // 通知客户端玩家资源变化
        warService.sendRoleResChange(changeMap);
        // 战斗打日志
        warService.logBattle(battle, fightLogic.getWinState(), attacker, defender, rpt.getAtkHeroList(), rpt.getDefHeroList());
    }


    /**
     * 发送攻打闪电战boss奖励邮件
     *
     * @param battle
     * @param staticCity
     * @param atkSuccess
     * @param report
     * @param awardProp
     * @param now
     * @param recoverArmyAwardMap
     */
    private void sendLightningWarBattleMail(Battle battle, StaticCity staticCity, boolean atkSuccess,
                                            CommonPb.Report.Builder report, Map<Long, List<CommonPb.Award>> awardProp, int now,
                                            Map<Long, List<CommonPb.Award>> recoverArmyAwardMap) {
        List<CommonPb.BattleRole> atkList = battle.getAtkList();
        Set<Long> ids = new HashSet<>();
        for (CommonPb.BattleRole battleRole : atkList) {
            long roleId = battleRole.getRoleId();
            if (ids.contains(roleId))
                continue;
            Player player = playerDataManager.getPlayer(roleId);
            if (player == null)
                continue;
            ids.add(roleId);
            List<CommonPb.Award> recoverList = null;
            if (recoverArmyAwardMap.containsKey(roleId)) {
                recoverList = recoverArmyAwardMap.get(roleId);
            }
            // 邮件参数
            List<String> tParam = new ArrayList<>();
            tParam.add(player.lord.getNick());
            tParam.add(String.valueOf(staticCity.getCityId()));
            List<String> cParam = new ArrayList<>();
            Turple<Integer, Integer> xy = MapHelper.reducePos(player.lord.getPos());
            Turple<Integer, Integer> cityXy = MapHelper.reducePos(staticCity.getCityPos());
            cParam.add(player.lord.getNick());
            cParam.add(String.valueOf(xy.getA()));
            cParam.add(String.valueOf(xy.getB()));
            cParam.add(String.valueOf(staticCity.getCityId()));
            cParam.add(String.valueOf(cityXy.getA()));
            cParam.add(String.valueOf(cityXy.getB()));
            cParam.add(player.lord.getNick());
            List<CommonPb.Award> awards = awardProp.get(roleId);
            mailDataManager
                    .sendReportMail(player, report, MailConstant.MOLD_ATK_LIGHTNING_WAR_BOSS, awards, now, tParam,
                            cParam, recoverList);
        }
    }

    /**
     * 添加闪电战的进攻方进攻奖励
     *
     * @param forces
     * @param lightningWarAttack
     * @param attackAward
     * @param defender
     * @param boss
     * @param battle
     */
    private void addAttackReaward(List<Force> forces, AwardFrom lightningWarAttack,
                                  HashMap<Long, List<List<Integer>>> attackAward, Fighter defender, LightningWarBoss boss, Battle battle,
                                  int totalHp, int curLost) {
        HashMap<Long, Integer> killCnt = mergeKill(forces);
        for (Map.Entry<Long, Integer> entry : killCnt.entrySet()) {
            Long roleId = entry.getKey();
            Integer kill = entry.getValue();
            List<List<Integer>> baseAwards = attackAward.get(roleId);
            if (CheckNull.isEmpty(baseAwards)) {
                baseAwards = new ArrayList<>();
                attackAward.put(roleId, baseAwards);
            }
            Player player = playerDataManager.getPlayer(roleId);
            if (CheckNull.isNull(player)) {
                continue;
            }
            baseAwards.add(Arrays.asList(AwardType.MONEY, AwardType.Money.EXP, (int) Math.ceil(kill * 0.5f) + 500));
            baseAwards
                    .add(Arrays.asList(AwardType.RESOURCE, AwardType.Resource.OIL, (int) Math.ceil(kill * 12) + 12000));
            baseAwards.add(Arrays
                    .asList(AwardType.RESOURCE, AwardType.Resource.ELE, (int) Math.ceil(kill * 7.2f) + 7200));
            baseAwards
                    .add(Arrays.asList(AwardType.RESOURCE, AwardType.Resource.FOOD, (int) Math.ceil(kill * 6) + 6000));
            LogUtil.debug("lightingWar:", battle.getBattleId(), ", roleId:", player.roleId, ", cityId:", boss.getId(),
                    ", 闪电战boss当前剩余血:" + (boss.currentHp() - curLost), ", 本次扣血:", curLost, ", 总血量:", totalHp, ", 造成伤害:",
                    kill);
        }
    }

    /**
     * 闪电战进攻方杀敌数
     *
     * @param forces
     * @return
     */
    private HashMap<Long, Integer> mergeKill(List<Force> forces) {
        HashMap<Long, Integer> killCnt = new HashMap<>();
        for (Force force : forces) {
            Integer kill = killCnt.get(force.ownerId);
            kill = CheckNull.isNull(kill) ? 0 : kill;
            if (force.roleType == Constant.Role.PLAYER) {
                Player player = playerDataManager.getPlayer(force.ownerId);
                if (player == null) {
                    continue;
                }
                killCnt.put(force.ownerId, kill + force.killed);
            }
        }
        return killCnt;
    }

    /**
     * 获取所有区域的闪电战信息
     *
     * @return
     */
    public GamePb4.GetAllLightningWarListRs getAllLightningWarList() {
        GamePb4.GetAllLightningWarListRs.Builder builder = GamePb4.GetAllLightningWarListRs.newBuilder();
        for (Battle battle : warDataManager.getSpecialBattleMap().values()) {
            if (battle.isLightningWar()) {
                builder.addBattles(PbHelper.createBattlePb(battle));
            }
        }
        worldDataManager.getLightningWarBossMap().values().forEach(boss -> {
            if (!CheckNull.isNull(boss) && !CheckNull.isNull(boss.getFighter())) {
                builder.addBossHp(PbHelper.createTwoIntPb(boss.getPos(), boss.currentHp()));
            }
        });
        return builder.build();
    }

    /**
     * 获取当前区域闪电战信息
     *
     * @param roleId
     * @param pos
     * @throws MwException
     */
    public GamePb4.GetLightningWarRs getLightningWar(long roleId, int pos) throws MwException {
        int area = MapHelper.getAreaIdByPos(pos);
        LightningWarBoss boss = worldDataManager.getLightningWarBossByArea(area);
        GamePb4.GetLightningWarRs.Builder builder = GamePb4.GetLightningWarRs.newBuilder();
        if (!CheckNull.isNull(boss)) {
            for (Battle battle : warDataManager.getSpecialBattleMap().values()) {
                if (battle.isLightningWar() && battle.getPos() == boss.getPos()) {
                    builder.setBattle(PbHelper.createBattlePb(battle));
                    if (!CheckNull.isNull(boss.getFighter())) {
                        builder.setBossHp(PbHelper.createTwoIntPb(boss.getPos(), boss.currentHp()));
                    }
                }
            }
        }
        return builder.build();
    }

    /**
     * 加入闪电战战斗
     *
     * @param roleId
     */
    public GamePb4.JoinLightningWarBattleRs joinLightningWarBattle(long roleId, GamePb4.JoinLightningWarBattleRq req)
            throws MwException {
        int now = TimeHelper.getCurrentSecond();
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (WorldConstant.ATTACK_STATE_NEED_LV > player.lord.getLevel()) {
            throw new MwException(GameError.ATTACK_STATE_NEED_LV.getCode(), "指挥官先磨砺至45级，再发动阵营战吧, roleId:", roleId);
        }
        StaticLightningWar lightningWar = StaticLightningWarDataMgr.getLightningWar();
        if (CheckNull.isNull(lightningWar)) {
            throw new MwException(GameError.LIGHTNING_WAR_CONF_NOT_FOUND.getCode(), "闪电战未配置");
        }
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_LIGHTNING_WAR);
        if (CheckNull.isNull(activity)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "闪电战活动未开启, roleId:", roleId);
        }
        if (!lightningWar.isInOpenTime(now)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "闪电战活动未开启, roleId:", roleId);
        }
        LightningWarBoss boss = worldDataManager.getLightningWarBossByArea(player.lord.getArea());
        if (CheckNull.isNull(boss)) {
            throw new MwException(GameError.LIGHTNING_WAR_BOOS_NOT_INIT.getCode(), "闪电战Boss未初始化, area:",
                    player.lord.getArea());
        }
        if (CheckNull.isNull(boss.getFighter())) {
            throw new MwException(GameError.LIGHTNING_WAR_BOOS_NOT_INIT.getCode(), "闪电战Boss未初始化, area:",
                    player.lord.getArea());
        }
        int camp = player.lord.getCamp();
        int battleId = req.getBattleId();
        Battle battle = warDataManager.getSpecialBattleMap().get(battleId);
        if (CheckNull.isNull(battle)) {
            throw new MwException(GameError.BATTLE_NOT_FOUND.getCode(), "战争信息未找到, roleId:", roleId, ", battleId:",
                    battleId);
        }
        StaticArea targetSArea = StaticWorldDataMgr.getAreaMap().get(MapHelper.getAreaIdByPos(battle.getPos()));
        StaticArea mySArea = StaticWorldDataMgr.getAreaMap().get(player.lord.getArea());
        // 城战区域检测
        worldService.checkArea(player, targetSArea, mySArea);
        int pos = battle.getPos();
        // 检查出征将领信息
        List<Integer> heroIdList = req.getHeroIdList();
        worldService.checkFormHero(player, heroIdList);

        int armCount = 0;
        Hero hero;
        for (Integer heroId : heroIdList) {
            hero = player.heros.get(heroId);
            armCount += hero.getCount();
        }

        int marchTime = worldService.marchTime(player, pos);

        // 计算时间是否赶得上
        if (now + marchTime > battle.getBattleTime()) {
            throw new MwException(GameError.BATTLE_CD_TIME.getCode(), "加入闪电战,赶不上时间, roleId:", roleId, ", pos:",
                    pos + ",行军时间=" + (now + marchTime) + ",闪电战倒计时=" + battle.getBattleTime());
        }

        // 计算补给
        int needFood = worldService.checkMarchFood(player, marchTime, armCount);
        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.RESOURCE, AwardType.Resource.FOOD, needFood,
                AwardFrom.ATK_POS);

        List<CommonPb.TwoInt> form = new ArrayList<>();
        for (Integer heroId : heroIdList) {
            hero = player.heros.get(heroId);
            hero.setState(ArmyConstant.ARMY_STATE_MARCH);
            form.add(PbHelper.createTwoIntPb(heroId, hero.getCount()));
        }

        Army army = new Army(player.maxKey(), ArmyConstant.ARMY_TYPE_LIGHTNING_WAR, pos, ArmyConstant.ARMY_STATE_MARCH,
                form, marchTime, now + marchTime, player.getDressUp());
        army.setBattleId(battleId);
        army.setLordId(roleId);
        army.setBattleTime(battle != null ? battle.getBattleTime() : 0);
        army.setOriginPos(player.lord.getPos());
        army.setHeroMedals(heroIdList.stream()
                .map(heroId -> medalDataManager.getHeroMedalByHeroIdAndIndex(player, heroId, MedalConst.HERO_MEDAL_INDEX_0))
                .filter(Objects::nonNull)
                .map(PbHelper::createMedalPb)
                .collect(Collectors.toList()));

        player.armys.put(army.getKeyId(), army);
        HashSet<Integer> set = player.battleMap.get(pos);
        if (set == null) {
            set = new HashSet<>();
            player.battleMap.put(pos, set);
        }
        set.add(battle.getBattleId());

        // 添加行军路线
        March march = new March(player, army);
        worldDataManager.addMarch(march);

        // 破护盾 攻击方
        if (camp != battle.getDefCamp()) {
            worldService.removeProTect(player, AwardFrom.LIGHTNING_WAR, pos);
            battle.getAtkRoles().add(roleId);
        }

        // 添加兵力到进攻方
        battle.updateAtkBoss(camp, armCount);
        // 推送区数据改变
        List<Integer> posList = MapHelper.getAreaStartPos(MapHelper.getLineAcorss(player.lord.getPos(), pos));
        posList.add(pos);
        posList.add(player.lord.getPos());
        EventBus.getDefault().post(new Events.AreaChangeNoticeEvent(posList, roleId,
                Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));
        GamePb4.JoinLightningWarBattleRs.Builder builder = GamePb4.JoinLightningWarBattleRs.newBuilder();

        builder.setArmy(PbHelper.createArmyPb(army, false));
        builder.setBattle(PbHelper.createBattlePb(battle));
        return builder.build();
    }

}
