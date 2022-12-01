package com.gryphpoem.game.zw.gameplay.local.world.army;

import com.gryphpoem.game.zw.constant.FightConstant;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonDailyAttackTaskService;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonDailyRestrictTaskService;
import com.gryphpoem.game.zw.gameplay.local.util.MapCurdEvent;
import com.gryphpoem.game.zw.gameplay.local.util.MapEvent;
import com.gryphpoem.game.zw.gameplay.local.util.dto.RetreatArmyParamDto;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.WorldEntityType;
import com.gryphpoem.game.zw.gameplay.local.world.map.BaseWorldEntity;
import com.gryphpoem.game.zw.gameplay.local.world.map.MineMapEntity;
import com.gryphpoem.game.zw.logic.FightSettleLogic;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.Award;
import com.gryphpoem.game.zw.pb.CommonPb.MailCollect;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Fighter;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.domain.s.StaticHero;
import com.gryphpoem.game.zw.resource.domain.s.StaticMine;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.army.Guard;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.FightService;
import com.gryphpoem.game.zw.service.TaskService;
import com.gryphpoem.game.zw.service.WorldService;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author QiuKun
 * @ClassName CollectArmy.java
 * @Description 采集部队
 * @date 2019年3月22日
 */
public class CollectArmy extends BaseArmy {

    public CollectArmy(Army army) {
        super(army);
    }

    @Override
    protected void marchEnd(MapMarch mapMarchArmy, int now) {
        Player armyPlayer = checkAndGetAmryHasPlayer(mapMarchArmy);
        if (armyPlayer == null) {
            return;
        }
        int targetPos = getTargetPos();
        CrossWorldMap cmap = mapMarchArmy.getCrossWorldMap();
        BaseWorldEntity baseWorldEntity = cmap.getAllMap().get(targetPos);
        if (baseWorldEntity == null || baseWorldEntity.getType() != WorldEntityType.MINE) {// 矿点不存在,返回
            noMineRetreat(mapMarchArmy, now);
        } else { // 矿点存在
            MineMapEntity mineEntity = (MineMapEntity) baseWorldEntity;
            if (mineEntity.getGuard() != null) { // 矿点有人开打
                fightMineGuard(mapMarchArmy, mineEntity, armyPlayer);
            } else { // 开始采集
                startCollectArmy(mapMarchArmy);
            }
        }
    }

    protected void fightMineGuard(MapMarch mapMarchArmy, MineMapEntity mineEntity, Player atkplayer) {
        Guard guard = mineEntity.getGuard();
        Player defPlayer = mineEntity.getGuard().getPlayer();
        // 给被攻击玩家推送消息（应用外推送）
        // PushMessageUtil.pushMessage(defPlayer.account, PushConstant.COLLECT_BY_ATTCK, atkplayer.lord.getNick());
        CrossWorldMap cMap = mapMarchArmy.getCrossWorldMap();

        FightService fightService = DataResource.ac.getBean(FightService.class);
        WorldService worldService = DataResource.ac.getBean(WorldService.class);
        MedalDataManager medalDataManager = DataResource.ac.getBean(MedalDataManager.class);
        ActivityDataManager activityDataManager = DataResource.ac.getBean(ActivityDataManager.class);
        HonorDailyDataManager honorDailyDataManager = DataResource.ac.getBean(HonorDailyDataManager.class);
        BattlePassDataManager battlePassDataManager = DataResource.ac.getBean(BattlePassDataManager.class);
        FightSettleLogic fightSettleLogic = DataResource.ac.getBean(FightSettleLogic.class);
        WorldWarSeasonDailyAttackTaskService dailyAttackTaskService = DataResource.ac
                .getBean(WorldWarSeasonDailyAttackTaskService.class);
        SeasonTalentService seasonTalentService = DataResource.ac.getBean(SeasonTalentService.class);

        int now = TimeHelper.getCurrentSecond();
        Fighter attacker = fightService.createFighter(atkplayer, army.getHero());
        Fighter defender = fightService.createFighter(defPlayer, guard.getForm());
        FightLogic fightLogic = new FightLogic(attacker, defender, true);
        fightLogic.start();

        //貂蝉任务-杀敌阵亡数量
        ActivityDiaoChanService.killedAndDeathTask0(attacker, true, true);
        ActivityDiaoChanService.killedAndDeathTask0(defender, true, true);

        // 记录玩家有改变的资源类型, key:roleId
        Map<Long, ChangeInfo> changeMap = new HashMap<>();
        // 兵力恢复
        Map<Long, List<CommonPb.Award>> recoverArmyAwardMap = new HashMap<>();

        // 损兵处理
        if (attacker.lost > 0) {
            worldService.subHeroArm(atkplayer, attacker.forces, AwardFrom.ATTACK_GUARD, changeMap);
            // 执行勋章白衣天使特技逻辑
            medalDataManager.angelInWhite(attacker, recoverArmyAwardMap);
            //执行赛季天赋技能---伤病恢复
            seasonTalentService.execSeasonTalentEffect303(attacker, recoverArmyAwardMap);
            // 损兵排行
            activityDataManager.updRankActivity(atkplayer, ActivityConst.ACT_ARMY_RANK, attacker.lost);
            // 荣耀日报损兵进度
            honorDailyDataManager.addAndCheckHonorReport2s(atkplayer, HonorDailyConstant.COND_ID_14, attacker.lost);
            // 战令的损兵进度
            battlePassDataManager.updTaskSchedule(atkplayer.roleId, TaskType.COND_SUB_HERO_ARMY, attacker.lost);
            // 采集战玩家杀敌数量计入每日杀敌任务
            dailyAttackTaskService.addPlayerDailyAttackOther(defPlayer, attacker.lost);
        }
        if (defender.lost > 0) {
            worldService.subHeroArm(defPlayer, defender.forces, AwardFrom.DEFEND_GUARD, changeMap);
            // 执行勋章白衣天使特技逻辑
            medalDataManager.angelInWhite(defender, recoverArmyAwardMap);
            //执行赛季天赋技能---伤病恢复
            seasonTalentService.execSeasonTalentEffect303(defender, recoverArmyAwardMap);
            // 损兵排行
            activityDataManager.updRankActivity(defPlayer, ActivityConst.ACT_ARMY_RANK, defender.lost);
            // 荣耀日报损兵进度
            honorDailyDataManager.addAndCheckHonorReport2s(defPlayer, HonorDailyConstant.COND_ID_14, defender.lost);
            // 战令的损兵进度
            battlePassDataManager.updTaskSchedule(defPlayer.roleId, TaskType.COND_SUB_HERO_ARMY, defender.lost);
            // 采集战玩家杀敌数量计入每日杀敌任务
            dailyAttackTaskService.addPlayerDailyAttackOther(atkplayer, defender.lost);
        }

        // buff回血,只对防守者有效
        cMap.rebelBuffRecoverArmy(attacker, defender, recoverArmyAwardMap);

        afterFightMineGuard(mapMarchArmy, fightLogic, attacker, defender);

        // 战斗记录
        Lord atkLord = atkplayer.lord;
        Lord defLord = defPlayer.lord;
        boolean isSuccess = fightLogic.getWinState() == FightConstant.FIGHT_RESULT_SUCCESS;

        CommonPb.RptAtkPlayer.Builder rpt = CommonPb.RptAtkPlayer.newBuilder();
        rpt.setResult(isSuccess);
        rpt.setAttack(PbHelper.createRptMan(atkLord.getPos(), atkLord.getNick(), atkLord.getVip(), atkLord.getLevel()));
        rpt.setDefMan(PbHelper.createRptMan(defLord.getPos(), defLord.getNick(), defLord.getVip(), defLord.getLevel()));
        rpt.setAtkSum(PbHelper.createRptSummary(attacker.total, attacker.lost, atkLord.getCamp(), atkLord.getNick(),
                atkLord.getPortrait(), atkplayer.getDressUp().getCurPortraitFrame()));
        rpt.setDefSum(PbHelper.createRptSummary(defender.total, defender.lost, defLord.getCamp(), defLord.getNick(),
                defLord.getPortrait(), defPlayer.getDressUp().getCurPortraitFrame()));

        rpt.addAllAtkHero(fightSettleLogic.mineFightHeroExpReward(atkplayer, attacker.forces));
        rpt.addAllDefHero(fightSettleLogic.mineFightHeroExpReward(defPlayer, defender.forces));
        Turple<Integer, Integer> atkPos = cMap.posToTurple(atkLord.getPos());
        Turple<Integer, Integer> defPos = cMap.posToTurple(defLord.getPos());
        CommonPb.Report report = DataResource.ac.getBean(FightRecordDataManager.class).generateReport(rpt.build(), fightLogic, now);

        MailDataManager mailDataManager = DataResource.ac.getBean(MailDataManager.class);

        if (isSuccess) {// 进攻方胜利，原来采集的部队返回，进攻方部队开始采集
            // 防御方采集结算
            CommonPb.MailCollect collect = mineEntity.settleCollect(cMap);
            if (collect != null) {
                mailDataManager.sendCollectMail(defPlayer, report, MailConstant.MOLD_COLLECT_DEF_FAIL, collect, now,
                        recoverArmyAwardMap, defLord.getNick(), atkLord.getNick(), atkLord.getNick(), atkPos.getA(),
                        atkPos.getB(), defLord.getNick(), defPos.getA(), defPos.getB());
            }
            // 进攻方开始采集
            startCollectArmy(mapMarchArmy);
            // 发送战报邮件
            mailDataManager.sendCollectMail(atkplayer, report, MailConstant.MOLD_COLLECT_ATK_SUCC, null, now,
                    recoverArmyAwardMap, atkLord.getNick(), defLord.getNick(), atkLord.getNick(), atkPos.getA(),
                    atkPos.getB(), defLord.getNick(), defPos.getA(), defPos.getB());
        } else {// 进攻方失败，返回
            normalRetreatArmy(mapMarchArmy); // 当前部队返回
            // 发送战报邮件
            mailDataManager.sendCollectMail(atkplayer, report, MailConstant.MOLD_COLLECT_ATK_FAIL, null, now,
                    recoverArmyAwardMap, atkLord.getNick(), defLord.getNick(), atkLord.getNick(), atkPos.getA(),
                    atkPos.getB(), defLord.getNick(), defPos.getA(), defPos.getB());
            mailDataManager.sendCollectMail(defPlayer, report, MailConstant.MOLD_COLLECT_DEF_SUCC, null, now,
                    recoverArmyAwardMap, defLord.getNick(), atkLord.getNick(), atkLord.getNick(), atkPos.getA(),
                    atkPos.getB(), defLord.getNick(), defPos.getA(), defPos.getB());
        }
        // 通知客户端玩家资源变化
        worldService.sendRoleResChange(changeMap);
        boolean hasMine = cMap.getAllMap().containsKey(getTargetPos()); // 是否采集完
        MapEvent mineEvent = MapEvent.mapEntity(getTargetPos(), hasMine ? MapCurdEvent.UPDATE : MapCurdEvent.DELETE);
        MapEvent guardMaplineEvent = MapEvent.mapLine(guard.getRoleId(), guard.getArmy().getKeyId(),
                MapCurdEvent.UPDATE);
        cMap.publishMapEvent(mineEvent, guardMaplineEvent, createMapEvent(MapCurdEvent.UPDATE));
    }

    /**
     * 开始采集
     */
    public void startCollectArmy(MapMarch mapMarchArmy) {
        CrossWorldMap cMap = mapMarchArmy.getCrossWorldMap();
        BaseWorldEntity baseWorldEntity = cMap.getAllMap().get(getTargetPos());
        int now = TimeHelper.getCurrentSecond();
        if (baseWorldEntity == null || baseWorldEntity.getType() != WorldEntityType.MINE) {
            noMineRetreat(mapMarchArmy, now);
            return;
        }
        MineMapEntity mineMapEntity = (MineMapEntity) baseWorldEntity;
        int heroId = getArmy().getHero().get(0).getPrincipleHeroId();
        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(heroId);
        int collectMaxTime = mineMapEntity.canCollectMaxTime(cMap, staticHero);
        Player armyPlayer = checkAndGetAmryHasPlayer(mapMarchArmy);
        Guard guard = new Guard(armyPlayer, this.army);
        this.army.setState(ArmyConstant.ARMY_STATE_COLLECT);
        setArmyPlayerHeroState(mapMarchArmy, ArmyConstant.ARMY_STATE_COLLECT);
        this.army.setDuration(collectMaxTime);
        setEndTime(mapMarchArmy, now + collectMaxTime);

        mineMapEntity.setGuard(guard); // 设置到驻点

        cMap.publishMapEvent(createMapEvent(MapCurdEvent.UPDATE),
                MapEvent.mapEntity(getTargetPos(), MapCurdEvent.UPDATE));
    }

    /**
     * 没有矿点
     *
     * @param mapMarchArmy
     * @param now
     */
    void noMineRetreat(MapMarch mapMarchArmy, int now) {
        Player armyPlayer = checkAndGetAmryHasPlayer(mapMarchArmy);
        MailDataManager mailDataManager = DataResource.ac.getBean(MailDataManager.class);
        mailDataManager.sendCollectMail(armyPlayer, null, MailConstant.MOLD_COLLECT_NO_TARGET, null, now);
        normalRetreatArmy(mapMarchArmy);
        mapMarchArmy.getCrossWorldMap().publishMapEvent(createMapEvent(MapCurdEvent.UPDATE));
    }

    /**
     * 采集结束
     *
     * @param mapMarchArmy
     */
    @Override
    public void finishCollect(MapMarch mapMarchArmy) {
        CrossWorldMap cMap = mapMarchArmy.getCrossWorldMap();
        BaseWorldEntity baseWorldEntity = cMap.getAllMap().get(getTargetPos());
        if (baseWorldEntity != null && baseWorldEntity.getType() == WorldEntityType.MINE) {
            Player amryPlayer = checkAndGetAmryHasPlayer(mapMarchArmy);
            MineMapEntity mineMapEntity = (MineMapEntity) baseWorldEntity;
            StaticMine staticMine = mineMapEntity.getCfgMine();
            Guard guard = mineMapEntity.getGuard();
            if (guard != null && guard.getPlayer() != amryPlayer) {
                return;
            }
            MailDataManager mailDataManager = DataResource.ac.getBean(MailDataManager.class);
            WorldService worldService = DataResource.ac.getBean(WorldService.class);
            int now = TimeHelper.getCurrentSecond();
            // 结算采集
            MailCollect mailCollect = mineMapEntity.settleCollect(cMap);
            if (mailCollect == null) {
                return;
            }
            List<Award> grab = mailCollect.getGrabList();
            boolean hasMine = cMap.getAllMap().containsKey(getTargetPos());
            Turple<Integer, Integer> xy = cMap.posToTurple(getTargetPos());
            if (hasMine) {
                //采集撤回邮件类型
                mailDataManager.sendCollectMail(amryPlayer, null, MailConstant.MOLD_COLLECT_RETREAT, mailCollect, now,
                        staticMine.getLv(), staticMine.getMineId(), xy.getA(), xy.getB());
            } else {//采集完成邮件类型
                mailDataManager.sendCollectMail(amryPlayer, null, MailConstant.MOLD_COLLECT, mailCollect, now,
                        grab.get(0).getType(), grab.get(0).getId(), grab.get(0).getCount(), staticMine.getLv(),
                        staticMine.getMineId(), xy.getA(), xy.getB());
                worldService.pushMsgCollect(amryPlayer, grab.get(0));
            }

            MapEvent mineEvent = MapEvent.mapEntity(getTargetPos(),
                    hasMine ? MapCurdEvent.UPDATE : MapCurdEvent.DELETE);
            mapMarchArmy.getCrossWorldMap().publishMapEvent(createMapEvent(MapCurdEvent.UPDATE), mineEvent);
            // 记录玩家采集铀矿时间
            WorldWarSeasonDailyRestrictTaskService restrictTaskService = DataResource.ac
                    .getBean(WorldWarSeasonDailyRestrictTaskService.class);
            restrictTaskService.updatePlayerDailyRestrictTask(amryPlayer, TaskType.COND_WORLD_WAR_MINE_CNT,
                    this.army.getCollectTime());

            //貂蝉任务-采集资源
            ActivityDiaoChanService.completeTask(amryPlayer, ETask.COLLECT_RES, staticMine.getMineType(), grab.get(0).getCount());
            TaskService.processTask(amryPlayer, ETask.COLLECT_RES, staticMine.getMineType(), grab.get(0).getCount());
        }
    }

    @Override
    protected void retreatEnd(MapMarch mapMarchArmy, int now) {
        List<Award> grab = getArmy().getGrab();
        if (!CheckNull.isEmpty(grab)) {
            Player amryPlayer = checkAndGetAmryHasPlayer(mapMarchArmy);
            RewardDataManager rewardDataManager = DataResource.ac.getBean(RewardDataManager.class);
            rewardDataManager.sendRewardByAwardList(amryPlayer, grab, AwardFrom.COLLECT);
        }
        super.retreatEnd(mapMarchArmy, now);
    }

    @Override
    public void retreat(RetreatArmyParamDto param) {
        finishCollect(param.getCrossWorldMap().getMapMarchArmy());
        super.retreat(param);
    }

    protected void afterFightMineGuard(MapMarch mapMarch, FightLogic fightLogic, Fighter attker, Fighter defer) {
        //等待别人调用
    }
}
