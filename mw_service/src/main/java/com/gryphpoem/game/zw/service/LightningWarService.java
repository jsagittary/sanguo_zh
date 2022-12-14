package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticLightningWarDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.BattleRole;
import com.gryphpoem.game.zw.pb.GamePb4;
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
import com.gryphpoem.game.zw.resource.pojo.fight.FightLogic;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;
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
 * @description: ?????????????????????, ????????????, ???????????????, ?????????????????????
 * @modified By:
 */
@Service public class LightningWarService {

    @Autowired private FightService fightService;

    @Autowired private WorldService worldService;

    @Autowired private WarService warService;

    @Autowired private WorldDataManager worldDataManager;

    @Autowired private PlayerDataManager playerDataManager;

    @Autowired private ActivityDataManager activityDataManager;

    @Autowired private WarDataManager warDataManager;

    @Autowired private RewardDataManager rewardDataManager;

    @Autowired private MailDataManager mailDataManager;

    @Autowired private SolarTermsDataManager solarTermsDataManager;

    @Autowired private HonorDailyDataManager honorDailyDataManager;

    @Autowired private MedalDataManager medalDataManager;

    @Autowired private ChatDataManager chatDataManager;

    @Autowired private CounterAtkService counterAtkService;

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
                            // ???????????????????????????
                            chatDataManager.sendSysChat(ChatConst.CHAT_ATK_CHAT, area, 0, cnt);
                            // ???15????????????????????????(10????????????????????????,5??????????????????)
                            if (!warDataManager.getBattlePosMap().containsKey(boss.getPos())) {
                                createLightningWarBattle(now, lightningWar, boss);
                            }
                        }
                    }
                });
                break;
            case ActivityConst.ACT_END:
                worldDataManager.getLightningWarBossMap().values().forEach(boss -> {
                    // ??????????????????,boss???????????????
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
     * ????????????????????????(??????battle??????,???????????????boss??????)
     */
    public void sendChatLogic() {
        int now = TimeHelper.getCurrentSecond();
        StaticLightningWar lightningWar = StaticLightningWarDataMgr.getLightningWar();
        if (CheckNull.isNull(lightningWar)) {
            return;
        }
        // ?????????????????????
        int cnt = worldDataManager.currentSendChatCnt(WorldConstant.CHAT_TIME_KEY);
        ActivityBase actBase = StaticActivityDataMgr.getActivityByTypeIgnoreStep(ActivityConst.ACT_LIGHTNING_WAR);
        Date date = new Date(now * 1000L);
        if (DateHelper.dayiy(actBase.getBeginTime(), date) == 1) {
            if (cnt == 0) {
                if (lightningWar.isInBanAttackTime(now) && DateHelper.isAfterTime(date, lightningWar.getChatTime())) {
                    // ?????????????????????
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
                            // ???????????????????????????
                            chatDataManager.sendSysChat(ChatConst.CHAT_ATK_CHAT, area, 0, cnt);
                            // worldDataManager.updSendChatCnt(WorldConstant.CHAT_TIME_KEY, 1);
                            worldDataManager.updSendChatCnt(boss.getId(), 1);
                            // ???15????????????????????????(10????????????????????????,5??????????????????)
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
        // // ????????????Fighter??????
        // if (CheckNull.isNull(lightningWarBoss.getFighter())) {
        // lightningWarBoss.setFighter(fightService.createLightningWarBossDefencer(lightningWar.getFormList()));
        // }
        // if (lightningWarBoss.isNotInitOrDead()) {
        // // ???????????????????????????
        // chatService.sendSysChat(ChatConst.CHAT_ATK_CHAT, 0, 1, cnt);
        // worldDataManager.updSendChatCnt(WorldConstant.CHAT_TIME_KEY, 1);
        // // ???15????????????????????????(10????????????????????????,5??????????????????)
        // createLightningWarBattle(now, lightningWar, player, lightningWarBoss);
        // }
        // }
        // }

    }

    /**
     * ???????????????????????????
     *
     * @param now
     * @param lightningWar
     * @param player
     * @param lightningWarBoss
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
     * ??????????????????????????????
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
        List<Integer> areaIdList = staticArea.getUnlockArea();// ?????????????????????????????????????????????????????????????????????

        ConcurrentHashMap<Long, Player> playerMap = playerDataManager.getPlayerByAreaList(areaIdList);
        for (Player player : playerMap.values()) {
            if (player.isLogin) {
                MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
            }
        }
    }

    /**
     * ?????????????????????????????????
     */
    public void batlleTimeLogic() {
        Battle battle;
        int now = TimeHelper.getCurrentSecond();
        // ?????????BattleId
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
                LogUtil.error(e, "??????????????????????????????????????? battle:", battle);
                // ????????????
                removeBattleIdSet.add(battle.getBattleId());
            }
        }

        for (Integer battleId : removeBattleIdSet) {
            battle = warDataManager.removeSpecialBattleById(battleId);
            LogUtil.debug("??????battleId=" + battleId);
            warService.retreatBattleArmy(battle, now);// ?????????????????????????????????
            warService.removePlayerJoinBattle(battle);// ????????????????????????
        }
    }

    /**
     * ???????????????boss????????????,???????????????????????? ???????????????????????????????????????????????????????????????????????????????????????????????????????????? ????????????????????????boss?????????,??????????????????,?????????????????????,??????????????????????????????,?????????Army
     *
     * @param battle
     * @param now
     * @param removeBattleIdSet
     * @throws MwException
     */
    public void lightningWarFightLogic(Battle battle, int now, Set<Integer> removeBattleIdSet) throws MwException {
        if (null == battle)
            return;
        LogUtil.debug("????????????, battle:", battle);
        int pos = battle.getPos();
        CommonPb.RptAtkPlayer.Builder rpt = CommonPb.RptAtkPlayer.newBuilder();

        worldDataManager.updSendChatCnt(WorldConstant.CHAT_TIME_KEY, 1);
        Fighter defender = null;
        int area = WorldDataManager.getAreaIdByPos(pos);
        LightningWarBoss boss = worldDataManager.getLightningWarBossByArea(area);
        if (CheckNull.isNull(boss)) {
            LogUtil.error("??????????????????boss, pos:", pos);
            // ????????????
            removeBattleIdSet.add(battle.getBattleId());
            return;
        }
        if (boss.isNotInitOrDead()) {
            LogUtil.error("?????????boss????????????,???????????????, pos:", pos);
            // ????????????
            removeBattleIdSet.add(battle.getBattleId());
            return;
        }
        StaticLightningWar lightningWar = StaticLightningWarDataMgr.getLightningWar();
        if (CheckNull.isNull(lightningWar)) {
            LogUtil.error("?????????boss???????????????, pos:", pos);
            // ????????????
            removeBattleIdSet.add(battle.getBattleId());
            return;
        }
        StaticCity staticCity = StaticWorldDataMgr.getCityByPos(pos);
        if (CheckNull.isNull(staticCity)) {
            LogUtil.error("???????????????Fighter??? ?????????????????????, pos:", pos);
            // ????????????
            removeBattleIdSet.add(battle.getBattleId());
            return;
        }
        if (CheckNull.isEmpty(battle.getAtkList())) {
            LogUtil.error("???????????????", pos);
            // ????????????
            removeBattleIdSet.add(battle.getBattleId());
            return;
        }
        defender = fightService.createBossNpcForce(boss.getFighter());
        Fighter attacker = fightService.createMultiPlayerFighter(battle, null);
        LogUtil.debug("defender=" + defender + ",attacker=" + attacker);
        FightLogic fightLogic = new FightLogic(attacker, defender, true, battle.getType());
        warDataManager.packForm(fightLogic.getRecordBuild(), attacker.forces, defender.forces);

        int totalHp = defender.getTotal();// ?????????
        int lostTotal = defender.getLost();// ???????????????

        fightLogic.fight();// ??????

        //????????????-??????????????????
        ActivityDiaoChanService.killedAndDeathTask0(attacker,false,true);
        ActivityDiaoChanService.killedAndDeathTask0(defender,false,true);

        boolean atkSuccess = fightLogic.getWinState() == ArmyConstant.FIGHT_RESULT_SUCCESS;
        // ????????????????????????????????????, key:roleId
        Map<Long, ChangeInfo> changeMap = new HashMap<>();
        // ????????????
        Map<Long, List<CommonPb.Award>> recoverArmyAwardMap = new HashMap<>();
        // ????????????
        if (attacker.lost > 0) {
            if (battle.isLightningWar()) {
                warService.subBattleHeroArm(attacker.forces, changeMap, AwardFrom.LIGHTNING_WAR_ATTACK);
                //????????????????????????????????????
                medalDataManager.angelInWhite(attacker, recoverArmyAwardMap);
                //????????????????????????---????????????
                seasonTalentService.execSeasonTalentEffect303(attacker, recoverArmyAwardMap);
                for (CommonPb.BattleRole battleRole : battle.getAtkList()) {
                    long roleId = battleRole.getRoleId();
                    Player player = playerDataManager.getPlayer(roleId);
                    // ????????????
                    activityDataManager.updRankActivity(player, ActivityConst.ACT_ARMY_RANK, attacker.lost);
                    // ????????????????????????
                    honorDailyDataManager
                            .addAndCheckHonorReport2s(player, HonorDailyConstant.COND_ID_14, attacker.lost);
                    // ?????????????????????
                    battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_SUB_HERO_ARMY, attacker.lost);

                }
            }
        }

        CommonPb.Record record = fightLogic.generateRecord();
        rpt.setNightEffect(solarTermsDataManager.getNightEffect() != null);
        rpt.setResult(atkSuccess);
        rpt.setRecord(record);
        // // ????????????????????????
        rpt.setAtkSum(PbHelper.createRptSummary(attacker.total, attacker.lost, 0, null, 0, 0));
        rpt.setDefCity(PbHelper.createRptCityPb(boss.getId(), pos));
        rpt.setDefSum(
                PbHelper.createRptSummary(totalHp - lostTotal, defender.lost - lostTotal, battle.getDefCamp(), null,
                        0, 0));

        LinkedList<Battle> list = warDataManager.getBattlePosMap().get(battle.getPos());
        Map<Long, List<CommonPb.Award>> awardProp = new HashMap<>();
        List<CommonPb.Award> tmp;
        HashMap<Long, List<List<Integer>>> attackAward = new HashMap<>();

        // ??????????????????,??????????????? * ??????????????????(????????????)
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

        // ??????????????????=????????????+????????????/2??????????????????????????????
        if (battle.isLightningWar()) {
            warService.addBattleHeroExp(attacker.forces, AwardFrom.LIGHTNING_WAR_ATTACK, rpt, true, false, false,
                    changeMap, true, null);
            warService.addBattleHeroExp(defender.forces, AwardFrom.LIGHTNING_WAR_DEFEND, rpt, false, false, false,
                    changeMap, false, null);
            if (atkSuccess) {
                // ????????????
                battle.getAtkList().forEach(battleRole -> {
                    long roleId = battleRole.getRoleId();
                    Player player = playerDataManager.getPlayer(roleId);
                    List<CommonPb.Award> temp = rewardDataManager
                            .sendReward(player, lightningWar.getKillAward(), AwardFrom.LIGHTNING_WAR_BOSS_DEAD_REWARD);
                    mailDataManager.sendAttachMail(player, temp, MailConstant.MOLD_ATK_LIGHTNING_WAR_BOSS_SUCC,
                            AwardFrom.LIGHTNING_WAR_BOSS_DEAD_REWARD, now, player.lord.getNick(),
                            staticCity.getCityId());
                });
                // ??????????????????????????????????????????
                for (Battle battle2 : list) {
                    LogUtil.debug("??????????????????battleId=" + battle2.getBattleId());
                    removeBattleIdSet.add(battle2.getBattleId());
                }
                // boss?????????
                boss.setStatus(WorldConstant.BOSS_STATUS_DEAD);
                // ???????????????
                chatDataManager.sendSysChat(ChatConst.CHAT_BOSS_DEAD, 0, 0, staticCity.getArea());
            }
            // ??????
            removeBattleIdSet.add(battle.getBattleId());

            CommonPb.Report.Builder report = worldService.createAtkPlayerReport(rpt.build(), now);
            // ??????????????????
            sendLightningWarBattleMail(battle, staticCity, atkSuccess, report, awardProp, now, recoverArmyAwardMap);

            // ??????????????????
            List<Integer> posList = new ArrayList<>();
            posList.add(boss.getPos());
            // ??????????????????????????????
            EventBus.getDefault()
                    .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_LINE_TYPE));
        }
        boss.setFighter(defender);
        // ?????????????????????????????????
        warService.sendRoleResChange(changeMap);
        // ???????????????
        warService.logBattle(battle, fightLogic.getWinState(),attacker,defender, rpt.getAtkHeroList(), rpt.getDefHeroList());
    }





    /**
     * ?????????????????????boss????????????
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
            // ????????????
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
     * ???????????????????????????????????????
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
                    ", ?????????boss???????????????:" + (boss.currentHp() - curLost), ", ????????????:", curLost, ", ?????????:", totalHp, ", ????????????:",
                    kill);
        }
    }

    /**
     * ???????????????????????????
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
     * ????????????????????????????????????
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
     * ?????????????????????????????????
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
     * ?????????????????????
     *
     * @param roleId
     */
    public GamePb4.JoinLightningWarBattleRs joinLightningWarBattle(long roleId, GamePb4.JoinLightningWarBattleRq req)
            throws MwException {
        int now = TimeHelper.getCurrentSecond();
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (WorldConstant.ATTACK_STATE_NEED_LV > player.lord.getLevel()) {
            throw new MwException(GameError.ATTACK_STATE_NEED_LV.getCode(), "?????????????????????45???????????????????????????, roleId:", roleId);
        }
        StaticLightningWar lightningWar = StaticLightningWarDataMgr.getLightningWar();
        if (CheckNull.isNull(lightningWar)) {
            throw new MwException(GameError.LIGHTNING_WAR_CONF_NOT_FOUND.getCode(), "??????????????????");
        }
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_LIGHTNING_WAR);
        if (CheckNull.isNull(activity)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "????????????????????????, roleId:", roleId);
        }
        if (!lightningWar.isInOpenTime(now)) {
            throw new MwException(GameError.ACTIVITY_NOT_OPEN.getCode(), "????????????????????????, roleId:", roleId);
        }
        LightningWarBoss boss = worldDataManager.getLightningWarBossByArea(player.lord.getArea());
        if (CheckNull.isNull(boss)) {
            throw new MwException(GameError.LIGHTNING_WAR_BOOS_NOT_INIT.getCode(), "?????????Boss????????????, area:",
                    player.lord.getArea());
        }
        if (CheckNull.isNull(boss.getFighter())) {
            throw new MwException(GameError.LIGHTNING_WAR_BOOS_NOT_INIT.getCode(), "?????????Boss????????????, area:",
                    player.lord.getArea());
        }
        int camp = player.lord.getCamp();
        int battleId = req.getBattleId();
        Battle battle = warDataManager.getSpecialBattleMap().get(battleId);
        if (CheckNull.isNull(battle)) {
            throw new MwException(GameError.BATTLE_NOT_FOUND.getCode(), "?????????????????????, roleId:", roleId, ", battleId:",
                    battleId);
        }
        StaticArea targetSArea = StaticWorldDataMgr.getAreaMap().get(MapHelper.getAreaIdByPos(battle.getPos()));
        StaticArea mySArea = StaticWorldDataMgr.getAreaMap().get(player.lord.getArea());
        // ??????????????????
        worldService.checkArea(player, targetSArea, mySArea);
        int pos = battle.getPos();
        // ????????????????????????
        List<Integer> heroIdList = req.getHeroIdList();
        worldService.checkFormHero(player, heroIdList);

        int armCount = 0;
        Hero hero;
        for (Integer heroId : heroIdList) {
            hero = player.heros.get(heroId);
            armCount += hero.getCount();
        }

        int marchTime = worldService.marchTime(player, pos);

        // ???????????????????????????
        if (now + marchTime > battle.getBattleTime()) {
            throw new MwException(GameError.BATTLE_CD_TIME.getCode(), "???????????????,???????????????, roleId:", roleId, ", pos:",
                    pos + ",????????????=" + (now + marchTime) + ",??????????????????=" + battle.getBattleTime());
        }

        // ????????????
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

        // ??????????????????
        March march = new March(player, army);
        worldDataManager.addMarch(march);

        // ????????? ?????????
        if (camp != battle.getDefCamp()) {
            worldService.removeProTect(player,AwardFrom.LIGHTNING_WAR,pos);
            battle.getAtkRoles().add(roleId);
        }

        // ????????????????????????
        battle.updateAtkBoss(camp, armCount);
        // ?????????????????????
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
