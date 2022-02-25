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
 * @Description 跨服战处理
 * @author QiuKun
 * @date 2019年5月23日
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
    private CrossOpenTimeService crossOpenTimeService; // 跨服时间管理
    @Autowired
    private HeroRevivalService heroRevivalService;
    @Autowired
    private CrossRankMgr crossRankMgr;

    private ApplicationContext ac;

    public void startInit() {
        crossDayProcess(null);
        // 注册转点定时器事件
        CrontabJob.registQuartzJobRun(AcrossDayJob.class, "堡垒转点刷新", this::crossDayProcess);
        CrontabJob.registQuartzJobRun(RunSecJob.class, "复活队列处理", heroRevivalService::runSecProcess);
    }

    /**
     * 跨天处理
     * 
     * @param context
     */
    private void crossDayProcess(JobExecutionContext context) {
        crossOpenTimeService.refreshTime();
        initRunTimer();
    }

    /**
     * 初始化堡垒定时器
     */
    private void initRunTimer() {
        int now = TimeHelper.getCurrentSecond();
        PeriodTime curOpentTime = crossOpenTimeService.getCurOpentTime();
        if (curOpentTime != null) {
            Date startDate = TimeHelper.secondToDate(curOpentTime.getStartTime());
            if (DateHelper.isToday(startDate)) { // 开始时间为当天,初始化定时器
                Date endDate = TimeHelper.secondToDate(curOpentTime.getEndTime());
                Date finishDate = TimeHelper.secondToDate(curOpentTime.getEndTime() + 10);// 10秒后进行结算
                if (now <= curOpentTime.getEndTime()) {
                    QuartzHelper.addJobAtDate(timerMgr.getScheduler(), DefultLogicMainJob.newInstance("finlish"),
                            this::finishTimeProcess, finishDate);
                }
                QuartzHelper.addJobPeriod(timerMgr.getScheduler(), DefultLogicMainJob.newInstance("fightRun"),
                        this::fightRunProcess, startDate, endDate, 3); // 每轮打的间隔
            }
        }
    }

    /**
     * 每隔1秒的战斗处理
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
        if (f.isDefCamp(camp)) {// 自己是防守方
            List<RoleForce> atkQueue = f.getAtkQueue();
            if (!CheckNull.isEmpty(atkQueue)) {
                return atkQueue.get(0);
            }
        } else {
            // 自己试进攻方 , 获取与自己不同阵营的敌方
            List<RoleForce> noMyCampQueue = f.getRoleQueue().stream().filter(rf -> rf.getCamp() != camp)
                    .collect(Collectors.toList());
            if (!CheckNull.isEmpty(noMyCampQueue)) {
                return noMyCampQueue.get(0);
            } else {// 没有人就去NPC队列找
                NpcFortForce<NpcForce> npcQueue = f.getNpcQueue();
                if (!CheckNull.isEmpty(npcQueue)) {
                    return npcQueue;
                }
            }
        }
        return null;
    }

    /**
     * 获取单挑对手的 Fighter
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
     * 单挑
     */
    public void fightSolo(CrossPlayer player, CrossHero crossHero) {
        if (!crossOpenTimeService.isInCrossTimeCond()) return;
        Fortress fortress = fortressMgr.getFortress(crossHero.getFortId());
        List<CommonPb.CrossWarReport> rptList = null;
        RoleForce myRoleForce = fortress.findRoleForce(player.getLordId(), crossHero.getHeroId());
        if (myRoleForce != null && myRoleForce.getCount() > 0 && fortress.canSolo(player.getCamp())) {
            // 单挑逻辑
            rptList = new ArrayList<>();
            int maxCnt = 0;
            while (true) {
                // 攻打只要不是自己阵营的 所有势力
                if (maxCnt++ > 100 || crossHero.getCount() <= 0) {// 自己死亡
                    break;
                }
                if (!fortress.canSolo(player.getCamp())) {
                    // 自己没有死,并且打败所有敌方,修改阵营
                    if (!fortress.isDefCamp(player.getCamp())) {
                        fortress.changeCamp(player.getCamp());
                    }
                    break;
                }
                FortForce opponentFortForce = getSoloOpponentFortForce(fortress, player.getCamp());
                if (opponentFortForce == null) {// 没有对手
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
                // 自己扣血
                subRoleCnt(myRoleForce, myFighter);
                // 敌方扣血
                if (opponentFortForce instanceof RoleForce) {
                    RoleForce rf = (RoleForce) opponentFortForce;
                    subRoleCnt(rf, opponentFighter);
                } else if (opponentFortForce instanceof NpcFortForce) {
                    @SuppressWarnings("unchecked")
                    NpcFortForce<NpcForce> rf = (NpcFortForce<NpcForce>) opponentFortForce;
                    subNpcCnt(rf, opponentFighter);
                }
                // 重新计算兵力
                fortress.reCalcCnt();
                boolean atkSuccess = fightLogic.getWinState() == ArmyConstant.FIGHT_RESULT_SUCCESS;
                // 战报的生产
                int now = TimeHelper.getCurrentSecond();
                CommonPb.CrossWarReport warRpt = fortress.isDefCamp(player.getCamp())
                        ? CrossFightHelper.toCrossWarReport(fortress, opponentFighter, myFighter, now, atkSuccess)
                        : CrossFightHelper.toCrossWarReport(fortress, myFighter, opponentFighter, now, atkSuccess);
                rptList.add(warRpt);
            }
        }
        // 发给发起单挑的人
        SyncSoloMsgRs.Builder builder = SyncSoloMsgRs.newBuilder();
        builder.setHero(crossHero.toFortHeroPb());
        if (!CheckNull.isEmpty(rptList)) {
            builder.addAllReport(rptList);
        }
        PbMsgUtil.sendOkMsgToPlayer(player, SyncSoloMsgRs.EXT_FIELD_NUMBER, SyncSoloMsgRs.ext, builder.build());
        fortressMgr.broadcastCrossFort();
        // 广播给全部人
        broadcastWarRpt(rptList);
    }

    /**
     * 广播战报
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
     * 每个堡垒的战斗
     * 
     * @param f
     */
    private CommonPb.CrossWarReport fortressProcess(Fortress f) {
        List<RoleForce> atkQueue = f.getAtkQueue();
        if (CheckNull.isEmpty(atkQueue)) {
            // 没有进攻者,就是空闲跳过
            return null;
        }
        RoleForce atkRoleForce = atkQueue.get(0);
        // 说明没有防守方,直接胜利
        f.reCalcCnt();
        if (f.getDefCnt() <= 0) {
            f.changeCamp(atkRoleForce.getCamp());
            return null;
        }
        Fighter attacker = CrossFightHelper.createRoleForceFighter(atkRoleForce);
        Fighter defender = getDefenderByFortress(f);
        if (defender == null || attacker == null) {
            LogUtil.error("打战斗时 防守方或进攻方为空, fortId:", f.getId());
            return null;
        }
        FightLogic fightLogic = new FightLogic(attacker, defender, true);
        fightLogic.fight();
        // 进攻方扣兵
        subRoleCnt(atkRoleForce, attacker);
        // 防守方扣兵
        if (defender.roleType == Constant.Role.CITY) {
            LinkedList<NpcForce> defNpcForce = f.getNpcQueue(); // npc当做一个整体
            subNpcCnt(defNpcForce, defender);
        } else {
            List<RoleForce> defQueue = f.getDefQueue();
            if (!CheckNull.isEmpty(defQueue)) {
                subRoleCnt(defQueue.get(0), defender);
            }
        }
        // 重新计算兵力
        f.reCalcCnt();
        boolean atkSuccess = fightLogic.getWinState() == ArmyConstant.FIGHT_RESULT_SUCCESS;
        if (atkSuccess && f.getDefCnt() <= 0) {
            f.setCamp(atkRoleForce.getCamp()); // 替换阵营
            f.reCalcCnt(); // 阵营替换还需计算一次
        }
        // 战报的生产
        int now = TimeHelper.getCurrentSecond();
        return CrossFightHelper.toCrossWarReport(f, attacker, defender, now, atkSuccess);
    }

    private Fighter getDefenderByFortress(Fortress f) {
        if (f.isNpcCamp()) {
            LinkedList<NpcForce> defNpcForce = f.getNpcQueue(); // npc当做一个整体
            return CrossFightHelper.createNpcFighter(defNpcForce);
        } else { // 非NPC堡垒, 先看是否有玩家 如果没有玩家 再看是否有npc
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
     * 扣玩家血量
     * 
     * @param roleForce
     * @param fighter
     */
    private void subRoleCnt(RoleForce roleForce, Fighter fighter) {
        roleForce.subCount(fighter.lost);
        if (!roleForce.isLive()) { // 设置死亡状态,并且设置复活状态
            // 加入到复活队列
            heroRevivalService.addRevivalQueue(roleForce);
        }
        // 同步信息给游戏服和客户端
        CrossPlayer crossPlayer = playerMgr.getPlayer(roleForce.getCrossHero().getLordId());
        if (crossPlayer != null) {
            int killNum = 0;

            if (fighter.oppoFighter.roleType == Constant.Role.PLAYER) { // 打玩家才算杀敌
                killNum = fighter.hurt;
            }
            crossRankMgr.killNumChange(crossPlayer, killNum); // 杀敌数改变
            SyncFortHeroRs.Builder builder = SyncFortHeroRs.newBuilder();
            builder.setHero(roleForce.getCrossHero().toFortHeroPb());
            builder.setKillNum(killNum);
            PbMsgUtil.sendOkMsgToPlayer(crossPlayer, SyncFortHeroRs.EXT_FIELD_NUMBER, SyncFortHeroRs.ext,
                    builder.build());
        }
    }

    /**
     * 扣除npc血量
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
     * 获取胜利方的阵营
     * 
     * @return 0说明没有获胜方
     */
    private int findWinCamp() {
        // 结束时统计3个阵营所占领的数量 <camp,fortCnt>
        Map<Integer, Integer> campFortCnt = fortressMgr.getFortresses().values().stream()
                .filter(f -> f.getCamp() != Constant.Camp.NPC).collect(Collectors.groupingBy(Fortress::getCamp,
                        Collectors.collectingAndThen(Collectors.toList(), l -> l.size())));

        // 找出堡垒占领最多的阵营
        Entry<Integer, Integer> maxCampCnt = campFortCnt.entrySet().stream()
                .max(Comparator.comparingInt(e -> e.getValue())).orElse(null);
        if (maxCampCnt == null) {
            return 0;
        }
        int occupyCnt = maxCampCnt.getValue();
        int winCamp = maxCampCnt.getKey();
        // 统计相同占领数的数量有几个
        long count = campFortCnt.values().stream().filter(cnt -> cnt == occupyCnt).count();
        if (count == 1) { // 只有一个
            return winCamp;
        }
        return 0;
    }

    /**
     * 结束时间处理
     * 
     * @param context
     */
    private void finishTimeProcess(JobExecutionContext context) {
        int winCamp = findWinCamp();
        // 活动结束 ,给各个玩家发结果
        for (CrossPlayer player : playerMgr.getPlayerMap().values()) {
            SyncCrossWarFinishRs.Builder builder = SyncCrossWarFinishRs.newBuilder();
            builder.setWinCamp(winCamp);
            // 阵营的名次
            int campRanking = crossRankMgr.findCampRanking(player.getMainServerId(), player.getCamp());
            if (campRanking > 0) {
                builder.addRankRes(PbHelper.createTwoIntPb(StaticCrossWarRank.RANK_TYPE_CAMP, campRanking));
            }
            // 个人排行名次
            int personalRanking = crossRankMgr.findPersonalRanking(player.getLordId());
            if (personalRanking > 0) {
                builder.addRankRes(PbHelper.createTwoIntPb(StaticCrossWarRank.RANK_TYPE_PERSONAL, personalRanking));
            }
            builder.setKillNum(player.getCrossWarModel().getKillNum());
            // 发送消息
            PbMsgUtil.sendOkMsgToPlayer(player, SyncCrossWarFinishRs.EXT_FIELD_NUMBER, SyncCrossWarFinishRs.ext,
                    builder.build());
        }
        // 一堆清除操作
        ac.getBeansOfType(CrossWarFinlishClear.class).values().forEach(c -> c.clear());
    }

    @Override
    public void setApplicationContext(ApplicationContext ac) throws BeansException {
        this.ac = ac;
    }

}
