package com.gryphpoem.game.zw.gameplay.local.world.army;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.gameplay.local.service.worldwar.WorldWarSeasonDailyRestrictTaskService;
import com.gryphpoem.game.zw.gameplay.local.util.MapCurdEvent;
import com.gryphpoem.game.zw.gameplay.local.util.MapEvent;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.WorldEntityType;
import com.gryphpoem.game.zw.gameplay.local.world.map.BaseWorldEntity;
import com.gryphpoem.game.zw.dataMgr.StaticBanditDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticNpcDataMgr;
import com.gryphpoem.game.zw.logic.FightSettleLogic;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.domain.s.StaticActBandit;
import com.gryphpoem.game.zw.resource.domain.s.StaticBandit;
import com.gryphpoem.game.zw.resource.domain.s.StaticNpc;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.fight.FightLogic;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.Turple;
import com.gryphpoem.game.zw.service.FightService;
import com.gryphpoem.game.zw.service.HonorDailyService;
import com.gryphpoem.game.zw.service.WorldService;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;

import java.util.*;

/**
 * @author QiuKun
 * @ClassName AttackBanditArmy.java
 * @Description ?????????????????????
 * @date 2019???3???28???
 */
public class AttackBanditArmy extends BaseArmy {

    public AttackBanditArmy(Army army) {
        super(army);
    }

    @Override
    protected void marchEnd(MapMarch mapMarchArmy, int now) {
        Player armyPlayer = checkAndGetAmryHasPlayer(mapMarchArmy);
        if (armyPlayer == null) {
            return;
        }
        CrossWorldMap cMap = mapMarchArmy.getCrossWorldMap();
        FightService fightService = DataResource.ac.getBean(FightService.class);
        MailDataManager mailDataManager = DataResource.ac.getBean(MailDataManager.class);
        int targetPos = army.getTarget();
        int marchTime = cMap.marchTime(cMap, armyPlayer, armyPlayer.lord.getPos(), targetPos);

        int banditId = this.army.getTargetId();
        StaticBandit staticBandit = StaticBanditDataMgr.getBanditMap().get(banditId);

        // ????????????
        Map<Long, List<CommonPb.Award>> recoverArmyAwardMap = new HashMap<>();
        Turple<Integer, Integer> xy = cMap.posToTurple(targetPos);
        BaseWorldEntity baseWorldEntity = cMap.getAllMap().get(targetPos);
        if (baseWorldEntity == null || baseWorldEntity.getType() != WorldEntityType.BANDIT) {
            mailDataManager.sendReportMail(armyPlayer, null, MailConstant.MOLD_ATK_TARGET_NOT_FOUND, null, now,
                    recoverArmyAwardMap, xy.getA(), xy.getB(), xy.getA(), xy.getB());
            // ????????????
            retreatArmy(mapMarchArmy, marchTime, marchTime);
            return;
        }

        if (!checkAtkBandit(armyPlayer, banditId, staticBandit)) {
            // ??????????????????
            mailDataManager.sendReportMail(armyPlayer, null, MailConstant.MOLD_ATK_TARGET_NOT_FOUND, null, now,
                    recoverArmyAwardMap, xy.getA(), xy.getB(), xy.getA(), xy.getB());
            // ????????????
            retreatArmy(mapMarchArmy, marchTime, marchTime);
            return;
        }

        StaticNpc npc;
        for (Integer npcId : staticBandit.getForm()) {
            npc = StaticNpcDataMgr.getNpcMap().get(npcId);
            if (null == npc) {
                LogUtil.error("NPCid?????????, npcId:", npcId);
                // ????????????
                retreatArmy(mapMarchArmy, marchTime, marchTime);
                return;
            }
        }

        // ????????????
        Fighter attacker = fightService.createFighter(armyPlayer, army.getHero());
        Fighter defender = fightService.createBanditFighter(banditId);
        FightLogic fightLogic = new FightLogic(attacker, defender, true);
        fightLogic.fight();

        //????????????-??????????????????
        ActivityDiaoChanService.killedAndDeathTask0(attacker,false,true);

        RewardDataManager rewardDataManager = DataResource.ac.getBean(RewardDataManager.class);
        ActivityDataManager activityDataManager = DataResource.ac.getBean(ActivityDataManager.class);
        HonorDailyDataManager honorDailyDataManager = DataResource.ac.getBean(HonorDailyDataManager.class);
        MedalDataManager medalDataManager = DataResource.ac.getBean(MedalDataManager.class);
        WorldService worldService = DataResource.ac.getBean(WorldService.class);
        FightSettleLogic fightSettleLogic = DataResource.ac.getBean(FightSettleLogic.class);
        BattlePassDataManager battlePassDataManager = DataResource.ac.getBean(BattlePassDataManager.class);
        SeasonTalentService seasonTalentService = DataResource.ac.getBean(SeasonTalentService.class);

        // ????????????????????????????????????, key:roleId
        Map<Long, ChangeInfo> changeMap = new HashMap<>(10);
        // ????????????
        List<CommonPb.Award> recoverArmyAward = new ArrayList<>(10);
        // ????????????
        // noinspection Duplicates
        if (attacker.lost > 0) {
            worldService.subHeroArm(armyPlayer, attacker.forces, AwardFrom.ATTACK_BANDIT, changeMap);
            // ????????????
            activityDataManager.updRankActivity(armyPlayer, ActivityConst.ACT_ARMY_RANK, attacker.lost);
            // ????????????????????????
            honorDailyDataManager.addAndCheckHonorReport2s(armyPlayer, HonorDailyConstant.COND_ID_14, attacker.lost);
            // ?????????????????????
            battlePassDataManager.updTaskSchedule(armyPlayer.roleId, TaskType.COND_SUB_HERO_ARMY, attacker.lost);
            // ?????????????????????
            List<List<Integer>> armyAward = worldService.attckBanditLostRecvCalc(armyPlayer, attacker.forces, now,
                    staticBandit.getLv(), WorldConstant.LOST_RECV_CALC_NIGHT);
            if (!CheckNull.isEmpty(armyAward)) {
                // "????????????????????????????????????"
                recoverArmyAward = rewardDataManager.sendReward(armyPlayer, armyAward, AwardFrom.RECOVER_ARMY);
            }
            // ????????????????????????????????????
            medalDataManager.angelInWhite(attacker, recoverArmyAwardMap);
            //????????????????????????---????????????
            seasonTalentService.execSeasonTalentEffect303(attacker, recoverArmyAwardMap);
            if (!CheckNull.isEmpty(recoverArmyAwardMap)) {
                List<CommonPb.Award> awards = recoverArmyAwardMap.get(armyPlayer.roleId);
                if (!CheckNull.isEmpty(awards)) {
                    recoverArmyAward.addAll(awards);
                }
            }
        }

        // ????????????
        CommonPb.Record record = fightLogic.generateRecord();

        Lord lord = armyPlayer.lord;
        boolean isSuccess = fightLogic.getWinState() == ArmyConstant.FIGHT_RESULT_SUCCESS;
        CommonPb.RptAtkBandit.Builder rpt = CommonPb.RptAtkBandit.newBuilder();
        rpt.setResult(isSuccess);
        rpt.setAttack(PbHelper.createRptMan(lord.getPos(), lord.getNick(), lord.getVip(), lord.getLevel()));
        rpt.setDefend(PbHelper.createRptBandit(banditId, targetPos));
        rpt.setAtkSum(PbHelper.createRptSummary(attacker.total, attacker.lost, lord.getCamp(), lord.getNick(),
                lord.getPortrait(), armyPlayer.getDressUp().getCurPortraitFrame()));
        rpt.setDefSum(PbHelper.createRptSummary(defender.total, defender.lost, 0, null, -1, -1));
        // ??????????????????
        rpt.addAllAtkHero(fightSettleLogic.banditFightHeroExpReward(armyPlayer, attacker.forces));
        worldService.buildRptHeroData(defender, rpt, false);
        rpt.setRecord(record);
        // ??????????????????
        fightResultLogic(now, armyPlayer, cMap, targetPos, staticBandit, attacker, recoverArmyAward, isSuccess, rpt);

        // ????????????
        retreatArmy(mapMarchArmy, marchTime, marchTime);
        // ?????????????????????????????????
        worldService.sendRoleResChange(changeMap);
        // ????????????
        if (isSuccess) {
            cMap.publishMapEvent(createMapEvent(MapCurdEvent.UPDATE),
                    MapEvent.mapEntity(getTargetPos(), MapCurdEvent.DELETE));
        } else {
            cMap.publishMapEvent(createMapEvent(MapCurdEvent.UPDATE));
        }
    }

    /**
     * ??????????????????
     *
     * @param now ??????????????????
     * @param armyPlayer ??????????????????
     * @param cMap CrossWorldMap??????
     * @param targetPos ????????????
     * @param staticBandit ??????????????????
     * @param attacker ?????????Fighter??????
     * @param recoverArmyAward ????????????
     * @param isSuccess ??????????????????
     * @param rpt ????????????
     */
    private void fightResultLogic(int now, Player armyPlayer, CrossWorldMap cMap, int targetPos,
            StaticBandit staticBandit, Fighter attacker, List<CommonPb.Award> recoverArmyAward, boolean isSuccess,
            CommonPb.RptAtkBandit.Builder rpt) {
        MailDataManager mailDataManager = DataResource.ac.getBean(MailDataManager.class);
        RewardDataManager rewardDataManager = DataResource.ac.getBean(RewardDataManager.class);
        ActivityDataManager activityDataManager = DataResource.ac.getBean(ActivityDataManager.class);
        MedalDataManager medalDataManager = DataResource.ac.getBean(MedalDataManager.class);
        WorldService worldService = DataResource.ac.getBean(WorldService.class);
        HonorDailyService honorDailyService = DataResource.ac.getBean(HonorDailyService.class);
        WorldWarSeasonDailyRestrictTaskService dailyRestrictTaskService = DataResource.ac
                .getBean(WorldWarSeasonDailyRestrictTaskService.class);
        TaskDataManager taskDataManager = DataResource.ac.getBean(TaskDataManager.class);

        Turple<Integer, Integer> xy;// ????????????
        List<String> tParam = new ArrayList<>();
        tParam.add(armyPlayer.lord.getNick());
        tParam.add(String.valueOf(staticBandit.getLv()));
        List<String> cParam = new ArrayList<>();
        cParam.add(armyPlayer.lord.getNick());
        xy = cMap.posToTurple(armyPlayer.lord.getPos());
        cParam.add(String.valueOf(xy.getA()));
        cParam.add(String.valueOf(xy.getB()));
        cParam.add(String.valueOf(staticBandit.getLv()));
        xy = cMap.posToTurple(targetPos);
        cParam.add(String.valueOf(xy.getA()));
        cParam.add(String.valueOf(xy.getB()));

        // ?????????????????????????????????
        if (isSuccess) {
            int historyLv = armyPlayer.trophy.getOrDefault(TrophyConstant.TROPHY_1, 1);
            if (staticBandit.getLv() > historyLv) {
                armyPlayer.trophy.put(TrophyConstant.TROPHY_1, staticBandit.getLv());
            }
            // ?????????????????????
            cMap.removeWorldEntity(targetPos);

            List<CommonPb.Award> dropList = new ArrayList<>();
            // ????????????
            double medalNum = medalDataManager.aSurpriseAttackOnTheBanditArmy(attacker);
            // ??????????????????
            int num = activityDataManager.getActDoubleNum(armyPlayer);
            // ??????????????????
            double resNum = activityDataManager.getActBanditRes(armyPlayer);
            List<List<Integer>> baseAwards = new ArrayList<>();
            // noinspection Duplicates
            for (List<Integer> award : staticBandit.getAwardBase()) {
                List<Integer> newAward = new ArrayList<>();
                newAward.add(award.get(0));
                newAward.add(award.get(1));
                int count = (int) (award.get(2) * medalNum * num * resNum);
                newAward.add(count);
                baseAwards.add(newAward);
            }

            List<CommonPb.Award> tmp = rewardDataManager.sendReward(armyPlayer, baseAwards, AwardFrom.BANDIT_DROP);
            if (tmp != null) {
                dropList.addAll(tmp);
            }
            int drawingNum = activityDataManager.getActBanditDrawing(armyPlayer);
            tmp = rewardDataManager.sendReward(armyPlayer, staticBandit.getAwardDrawing(), drawingNum,
                    AwardFrom.BANDIT_DROP);
            if (tmp != null) {
                dropList.addAll(tmp);
            }
            int moveNum = activityDataManager.getActBanditMove(armyPlayer);
            tmp = rewardDataManager.sendReward(armyPlayer, staticBandit.getAwardProp(), moveNum, AwardFrom.BANDIT_DROP);
            if (tmp != null) {
                dropList.addAll(tmp);
            }
            tmp = rewardDataManager.sendReward(armyPlayer, staticBandit.getAwardOthers(), AwardFrom.BANDIT_DROP);
            if (tmp != null) {
                dropList.addAll(tmp);
            }
            // ?????????????????????
            tmp = rewardDataManager.sendReward(armyPlayer, staticBandit.getAwardPlanePieces(), AwardFrom.BANDIT_DROP);
            if (tmp != null) {
                dropList.addAll(tmp);
            }
            // ????????????????????????
            tmp = activityDataManager.upActBanditAcce(armyPlayer);
            if (tmp != null) {
                dropList.addAll(tmp);
            }
            // ??????????????????
            tmp = activityDataManager.getActHitDrop(armyPlayer, staticBandit.getLv(),
                    StaticActBandit.ACT_HIT_DROP_TYPE_2);
            if (tmp != null) {
                dropList.addAll(tmp);
            }
            cParam.add(armyPlayer.lord.getNick());
            // ????????????
            mailDataManager.sendReportMail(armyPlayer, worldService.createAtkBanditReport(rpt.build(), now),
                    MailConstant.MOLD_ATK_BANDIT_SUCC, dropList, now, tParam, cParam, recoverArmyAward);
            activityDataManager.updDay7ActSchedule(armyPlayer, ActivityConst.ACT_TASK_ATK_BANDIT, staticBandit.getLv());

            // ????????????++
            armyPlayer.setBanditCnt(armyPlayer.getBanditCnt() + 1);
            // ??????
            taskDataManager.updTask(armyPlayer, TaskType.COND_BANDIT_LV_CNT, 1, staticBandit.getLv());
            // ???????????? ?????????????????????
            honorDailyService.addAndCheckHonorReport2s(armyPlayer, HonorDailyConstant.COND_ID_3);
            // ????????????????????????????????????
            dailyRestrictTaskService.updatePlayerDailyRestrictTaskAttackBandit(armyPlayer, staticBandit);
        } else {
            // ????????????
            mailDataManager.sendReportMail(armyPlayer, worldService.createAtkBanditReport(rpt.build(), now),
                    MailConstant.MOLD_ATK_BANDIT_FAIL, null, now, tParam, cParam, recoverArmyAward);
            // ???????????? ?????????????????????
            honorDailyService.addAndCheckHonorReport2s(armyPlayer, HonorDailyConstant.COND_ID_9);
        }
    }

    /**
     * ??????????????????
     *
     * @param armyPlayer ????????????
     * @param banditId ??????id
     * @param staticBandit ????????????
     * @return true ?????? , false ??????
     */
    private boolean checkAtkBandit(Player armyPlayer, int banditId, StaticBandit staticBandit) {

        if (null == staticBandit) {
            LogUtil.debug("??????id?????????????????????, banditId:", banditId);
            return false;
        }

        // ?????????
        Integer historyLv = armyPlayer.trophy.get(TrophyConstant.TROPHY_1);
        historyLv = historyLv != null ? historyLv : 0;
        if (staticBandit.getLv() > historyLv + 1) {
            LogUtil.common("??????????????????, banditId:", banditId);
            return false;
        }
        return true;
    }

}
