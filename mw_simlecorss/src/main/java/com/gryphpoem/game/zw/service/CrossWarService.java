package com.gryphpoem.game.zw.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.quartz.JobExecutionContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.hundredcent.game.ai.util.CheckNull;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.QuartzHelper;
import com.gryphpoem.game.zw.crosssimple.service.CrossOpenTimeService;
import com.gryphpoem.game.zw.mgr.CrossRankMgr;
import com.gryphpoem.game.zw.mgr.FortressMgr;
import com.gryphpoem.game.zw.mgr.PlayerMgr;
import com.gryphpoem.game.zw.mgr.timer.CrontabJob;
import com.gryphpoem.game.zw.mgr.timer.CrontabJob.AcrossDayJob;
import com.gryphpoem.game.zw.mgr.timer.CrontabJob.RunSecJob;
import com.gryphpoem.game.zw.mgr.timer.DefultLogicMainJob;
import com.gryphpoem.game.zw.mgr.timer.TimerMgr;
import com.gryphpoem.game.zw.model.CrossPlayer;
import com.gryphpoem.game.zw.model.fort.FortForce;
import com.gryphpoem.game.zw.model.fort.Fortress;
import com.gryphpoem.game.zw.model.fort.NpcFortForce;
import com.gryphpoem.game.zw.model.fort.RoleForce;
import com.gryphpoem.game.zw.model.player.CrossHero;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.CrossWarReport;
import com.gryphpoem.game.zw.pb.GamePb5.*;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossWarRank;
import com.gryphpoem.game.zw.resource.pojo.PeriodTime;
import com.gryphpoem.game.zw.resource.pojo.fight.FightLogic;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;
import com.gryphpoem.game.zw.resource.pojo.fight.NpcForce;
import com.gryphpoem.game.zw.resource.util.DateHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.util.CrossFightHelper;
import com.gryphpoem.game.zw.util.CrossWarFinlishClear;
import com.gryphpoem.game.zw.util.PbMsgUtil;

/**
 * @ClassName CrossWarService.java
 * @Description ???????????????
 * @author QiuKun
 * @date 2019???5???23???
 */
@Component
public class CrossWarService implements ApplicationContextAware {

    @Autowired
    private PlayerMgr playerMgr;
    @Autowired
    private TimerMgr timerMgr;
    @Autowired
    private FortressMgr fortressMgr;
    @Autowired
    private CrossOpenTimeService crossOpenTimeService; // ??????????????????
    @Autowired
    private HeroRevivalService heroRevivalService;
    @Autowired
    private CrossRankMgr crossRankMgr;

    private ApplicationContext ac;

    public void startInit() {
        crossDayProcess(null);
        // ???????????????????????????
        CrontabJob.registQuartzJobRun(AcrossDayJob.class, "??????????????????", this::crossDayProcess);
        CrontabJob.registQuartzJobRun(RunSecJob.class, "??????????????????", heroRevivalService::runSecProcess);
    }

    /**
     * ????????????
     * 
     * @param context
     */
    private void crossDayProcess(JobExecutionContext context) {
        crossOpenTimeService.refreshTime();
        initRunTimer();
    }

    /**
     * ????????????????????????
     */
    private void initRunTimer() {
        int now = TimeHelper.getCurrentSecond();
        PeriodTime curOpentTime = crossOpenTimeService.getCurOpentTime();
        if (curOpentTime != null) {
            Date startDate = TimeHelper.secondToDate(curOpentTime.getStartTime());
            if (DateHelper.isToday(startDate)) { // ?????????????????????,??????????????????
                Date endDate = TimeHelper.secondToDate(curOpentTime.getEndTime());
                Date finishDate = TimeHelper.secondToDate(curOpentTime.getEndTime() + 10);// 10??????????????????
                if (now <= curOpentTime.getEndTime()) {
                    QuartzHelper.addJobAtDate(timerMgr.getScheduler(), DefultLogicMainJob.newInstance("finlish"),
                            this::finishTimeProcess, finishDate);
                }
                QuartzHelper.addJobPeriod(timerMgr.getScheduler(), DefultLogicMainJob.newInstance("fightRun"),
                        this::fightRunProcess, startDate, endDate, 3); // ??????????????????
            }
        }
    }

    /**
     * ??????1??????????????????
     * 
     * @param context
     */
    private void fightRunProcess(JobExecutionContext context) {
        List<CommonPb.CrossWarReport> rptList = new ArrayList<>();
        for (Fortress f : fortressMgr.getFortresses().values()) {
            CrossWarReport rpt = fortressProcess(f);
            if (rpt != null) {
                rptList.add(rpt);
            }
        }
        broadcastWarRpt(rptList);
    }

    private FortForce getSoloOpponentFortForce(Fortress f, int camp) {
        if (f.isDefCamp(camp)) {// ??????????????????
            List<RoleForce> atkQueue = f.getAtkQueue();
            if (!CheckNull.isEmpty(atkQueue)) {
                return atkQueue.get(0);
            }
        } else {
            // ?????????????????? , ????????????????????????????????????
            List<RoleForce> noMyCampQueue = f.getRoleQueue().stream().filter(rf -> rf.getCamp() != camp)
                    .collect(Collectors.toList());
            if (!CheckNull.isEmpty(noMyCampQueue)) {
                return noMyCampQueue.get(0);
            } else {// ???????????????NPC?????????
                NpcFortForce<NpcForce> npcQueue = f.getNpcQueue();
                if (!CheckNull.isEmpty(npcQueue)) {
                    return npcQueue;
                }
            }
        }
        return null;
    }

    /**
     * ????????????????????? Fighter
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    private Fighter createFightByFortForce(FortForce fortForce) {
        if (fortForce == null) {
            return null;
        }
        if (fortForce instanceof RoleForce) {
            RoleForce rf = (RoleForce) fortForce;
            return CrossFightHelper.createRoleForceFighter(rf);
        } else if (fortForce instanceof NpcFortForce) {
            return CrossFightHelper.createNpcFighter((List<NpcForce>) fortForce);
        }
        return null;
    }

    /**
     * ??????
     */
    public void fightSolo(CrossPlayer player, CrossHero crossHero) {
        if (!crossOpenTimeService.isInCrossTimeCond()) return;
        Fortress fortress = fortressMgr.getFortress(crossHero.getFortId());
        List<CommonPb.CrossWarReport> rptList = null;
        RoleForce myRoleForce = fortress.findRoleForce(player.getLordId(), crossHero.getHeroId());
        if (myRoleForce != null && myRoleForce.getCount() > 0 && fortress.canSolo(player.getCamp())) {
            // ????????????
            rptList = new ArrayList<>();
            int maxCnt = 0;
            while (true) {
                // ????????????????????????????????? ????????????
                if (maxCnt++ > 100 || crossHero.getCount() <= 0) {// ????????????
                    break;
                }
                if (!fortress.canSolo(player.getCamp())) {
                    // ???????????????,????????????????????????,????????????
                    if (!fortress.isDefCamp(player.getCamp())) {
                        fortress.changeCamp(player.getCamp());
                    }
                    break;
                }
                FortForce opponentFortForce = getSoloOpponentFortForce(fortress, player.getCamp());
                if (opponentFortForce == null) {// ????????????
                    if (!fortress.isDefCamp(player.getCamp())) {
                        fortress.changeCamp(player.getCamp());
                    }
                    break;
                }
                Fighter opponentFighter = createFightByFortForce(opponentFortForce);
                Fighter myFighter = CrossFightHelper.createRoleForceFighter(myRoleForce);

                FightLogic fightLogic = fortress.isDefCamp(player.getCamp())
                        ? new FightLogic(opponentFighter, myFighter, true)
                        : new FightLogic(myFighter, opponentFighter, true);
                fightLogic.fight();
                // ????????????
                subRoleCnt(myRoleForce, myFighter);
                // ????????????
                if (opponentFortForce instanceof RoleForce) {
                    RoleForce rf = (RoleForce) opponentFortForce;
                    subRoleCnt(rf, opponentFighter);
                } else if (opponentFortForce instanceof NpcFortForce) {
                    @SuppressWarnings("unchecked")
                    NpcFortForce<NpcForce> rf = (NpcFortForce<NpcForce>) opponentFortForce;
                    subNpcCnt(rf, opponentFighter);
                }
                // ??????????????????
                fortress.reCalcCnt();
                boolean atkSuccess = fightLogic.getWinState() == ArmyConstant.FIGHT_RESULT_SUCCESS;
                // ???????????????
                int now = TimeHelper.getCurrentSecond();
                CommonPb.CrossWarReport warRpt = fortress.isDefCamp(player.getCamp())
                        ? CrossFightHelper.toCrossWarReport(fortress, opponentFighter, myFighter, now, atkSuccess)
                        : CrossFightHelper.toCrossWarReport(fortress, myFighter, opponentFighter, now, atkSuccess);
                rptList.add(warRpt);
            }
        }
        // ????????????????????????
        SyncSoloMsgRs.Builder builder = SyncSoloMsgRs.newBuilder();
        builder.setHero(crossHero.toFortHeroPb());
        if (!CheckNull.isEmpty(rptList)) {
            builder.addAllReport(rptList);
        }
        PbMsgUtil.sendOkMsgToPlayer(player, SyncSoloMsgRs.EXT_FIELD_NUMBER, SyncSoloMsgRs.ext, builder.build());
        fortressMgr.broadcastCrossFort();
        // ??????????????????
        broadcastWarRpt(rptList);
    }

    /**
     * ????????????
     * 
     * @param rptList
     */
    public void broadcastWarRpt(List<CommonPb.CrossWarReport> rptList) {
        if (CheckNull.isEmpty(rptList)) return;
        fortressMgr.broadcastCrossFort();
        SyncCrossWarRptRs rsPb = SyncCrossWarRptRs.newBuilder().addAllReport(rptList).build();
        playerMgr.getPlayerMap().values().stream().filter(CrossPlayer::isFouce).forEach(crossPlayer -> {
            PbMsgUtil.sendOkMsgToPlayer(crossPlayer, SyncCrossWarRptRs.EXT_FIELD_NUMBER, SyncCrossWarRptRs.ext, rsPb);
        });
    }

    /**
     * ?????????????????????
     * 
     * @param f
     */
    private CommonPb.CrossWarReport fortressProcess(Fortress f) {
        List<RoleForce> atkQueue = f.getAtkQueue();
        if (CheckNull.isEmpty(atkQueue)) {
            // ???????????????,??????????????????
            return null;
        }
        RoleForce atkRoleForce = atkQueue.get(0);
        // ?????????????????????,????????????
        f.reCalcCnt();
        if (f.getDefCnt() <= 0) {
            f.changeCamp(atkRoleForce.getCamp());
            return null;
        }
        Fighter attacker = CrossFightHelper.createRoleForceFighter(atkRoleForce);
        Fighter defender = getDefenderByFortress(f);
        if (defender == null || attacker == null) {
            LogUtil.error("???????????? ???????????????????????????, fortId:", f.getId());
            return null;
        }
        FightLogic fightLogic = new FightLogic(attacker, defender, true);
        fightLogic.fight();
        // ???????????????
        subRoleCnt(atkRoleForce, attacker);
        // ???????????????
        if (defender.roleType == Constant.Role.CITY) {
            LinkedList<NpcForce> defNpcForce = f.getNpcQueue(); // npc??????????????????
            subNpcCnt(defNpcForce, defender);
        } else {
            List<RoleForce> defQueue = f.getDefQueue();
            if (!CheckNull.isEmpty(defQueue)) {
                subRoleCnt(defQueue.get(0), defender);
            }
        }
        // ??????????????????
        f.reCalcCnt();
        boolean atkSuccess = fightLogic.getWinState() == ArmyConstant.FIGHT_RESULT_SUCCESS;
        if (atkSuccess && f.getDefCnt() <= 0) {
            f.setCamp(atkRoleForce.getCamp()); // ????????????
            f.reCalcCnt(); // ??????????????????????????????
        }
        // ???????????????
        int now = TimeHelper.getCurrentSecond();
        return CrossFightHelper.toCrossWarReport(f, attacker, defender, now, atkSuccess);
    }

    private Fighter getDefenderByFortress(Fortress f) {
        if (f.isNpcCamp()) {
            LinkedList<NpcForce> defNpcForce = f.getNpcQueue(); // npc??????????????????
            return CrossFightHelper.createNpcFighter(defNpcForce);
        } else { // ???NPC??????, ????????????????????? ?????????????????? ???????????????npc
            List<RoleForce> defQueue = f.getDefQueue();
            if (CheckNull.isEmpty(defQueue)) {
                LinkedList<NpcForce> defNpcForce = f.getNpcQueue();
                return CrossFightHelper.createNpcFighter(defNpcForce);
            } else {
                return CrossFightHelper.createRoleForceFighter(defQueue.get(0));
            }
        }
    }

    /**
     * ???????????????
     * 
     * @param roleForce
     * @param fighter
     */
    private void subRoleCnt(RoleForce roleForce, Fighter fighter) {
        roleForce.subCount(fighter.lost);
        if (!roleForce.isLive()) { // ??????????????????,????????????????????????
            // ?????????????????????
            heroRevivalService.addRevivalQueue(roleForce);
        }
        // ????????????????????????????????????
        CrossPlayer crossPlayer = playerMgr.getPlayer(roleForce.getCrossHero().getLordId());
        if (crossPlayer != null) {
            int killNum = 0;

            if (fighter.oppoFighter.roleType == Constant.Role.PLAYER) { // ?????????????????????
                killNum = fighter.hurt;
            }
            crossRankMgr.killNumChange(crossPlayer, killNum); // ???????????????
            SyncFortHeroRs.Builder builder = SyncFortHeroRs.newBuilder();
            builder.setHero(roleForce.getCrossHero().toFortHeroPb());
            builder.setKillNum(killNum);
            PbMsgUtil.sendOkMsgToPlayer(crossPlayer, SyncFortHeroRs.EXT_FIELD_NUMBER, SyncFortHeroRs.ext,
                    builder.build());
        }
    }

    /**
     * ??????npc??????
     * 
     * @param defNpcForce
     * @param defender
     */
    private void subNpcCnt(LinkedList<NpcForce> defNpcForce, Fighter defender) {
        if (defender.roleType == Constant.Role.CITY) {
            defNpcForce.clear();
            for (Force f : defender.forces) {
                if (f.alive()) {
                    defNpcForce.add(new NpcForce(f.id, f.hp, f.curLine));
                }
            }
        }
    }

    /**
     * ????????????????????????
     * 
     * @return 0?????????????????????
     */
    private int findWinCamp() {
        // ???????????????3??????????????????????????? <camp,fortCnt>
        Map<Integer, Integer> campFortCnt = fortressMgr.getFortresses().values().stream()
                .filter(f -> f.getCamp() != Constant.Camp.NPC).collect(Collectors.groupingBy(Fortress::getCamp,
                        Collectors.collectingAndThen(Collectors.toList(), l -> l.size())));

        // ?????????????????????????????????
        Entry<Integer, Integer> maxCampCnt = campFortCnt.entrySet().stream()
                .max(Comparator.comparingInt(e -> e.getValue())).orElse(null);
        if (maxCampCnt == null) {
            return 0;
        }
        int occupyCnt = maxCampCnt.getValue();
        int winCamp = maxCampCnt.getKey();
        // ???????????????????????????????????????
        long count = campFortCnt.values().stream().filter(cnt -> cnt == occupyCnt).count();
        if (count == 1) { // ????????????
            return winCamp;
        }
        return 0;
    }

    /**
     * ??????????????????
     * 
     * @param context
     */
    private void finishTimeProcess(JobExecutionContext context) {
        int winCamp = findWinCamp();
        // ???????????? ,????????????????????????
        for (CrossPlayer player : playerMgr.getPlayerMap().values()) {
            SyncCrossWarFinishRs.Builder builder = SyncCrossWarFinishRs.newBuilder();
            builder.setWinCamp(winCamp);
            // ???????????????
            int campRanking = crossRankMgr.findCampRanking(player.getMainServerId(), player.getCamp());
            if (campRanking > 0) {
                builder.addRankRes(PbHelper.createTwoIntPb(StaticCrossWarRank.RANK_TYPE_CAMP, campRanking));
            }
            // ??????????????????
            int personalRanking = crossRankMgr.findPersonalRanking(player.getLordId());
            if (personalRanking > 0) {
                builder.addRankRes(PbHelper.createTwoIntPb(StaticCrossWarRank.RANK_TYPE_PERSONAL, personalRanking));
            }
            builder.setKillNum(player.getCrossWarModel().getKillNum());
            // ????????????
            PbMsgUtil.sendOkMsgToPlayer(player, SyncCrossWarFinishRs.EXT_FIELD_NUMBER, SyncCrossWarFinishRs.ext,
                    builder.build());
        }
        // ??????????????????
        ac.getBeansOfType(CrossWarFinlishClear.class).values().forEach(c -> c.clear());
    }

    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        this.ac = ac;
    }

}
