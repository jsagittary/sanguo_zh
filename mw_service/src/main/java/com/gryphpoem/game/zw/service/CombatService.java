package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.dataMgr.*;
import com.gryphpoem.game.zw.logic.FightSettleLogic;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.BasePb.Base;
import com.gryphpoem.game.zw.pb.BattlePb;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.pb.GamePb2.*;
import com.gryphpoem.game.zw.pb.GamePb4.SyncHeroEquipRs;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Fighter;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.constant.Constant.CombatType;
import com.gryphpoem.game.zw.resource.domain.Msg;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Combat;
import com.gryphpoem.game.zw.resource.domain.p.CombatFb;
import com.gryphpoem.game.zw.resource.domain.p.PitchCombat;
import com.gryphpoem.game.zw.resource.domain.p.StoneCombat;
import com.gryphpoem.game.zw.resource.domain.s.*;
import com.gryphpoem.game.zw.resource.pojo.Prop;
import com.gryphpoem.game.zw.resource.pojo.SuperEquip;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.hero.PartnerHero;
import com.gryphpoem.game.zw.resource.pojo.medal.Medal;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.service.activity.ActivityDiaoChanService;
import com.gryphpoem.game.zw.service.activity.ActivityService;
import com.gryphpoem.game.zw.service.totem.TotemService;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 关卡
 *
 * @author tyler
 */
@Service
public class CombatService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private FightService fightService;
    @Autowired
    private TaskDataManager taskDataManager;
    @Autowired
    private HeroService heroService;
    @Autowired
    private RankDataManager rankDataManager;
    @Autowired
    private ActivityDataManager activityDataManager;
    @Autowired
    private FightSettleLogic fightSettleLogic;
    @Autowired
    private VipDataManager vipDataManager;
    @Autowired
    private BuildingDataManager buildingDataManager;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private EquipService equipService;
    @Autowired
    private MedalDataManager medalDataManager;
    @Autowired
    private WarPlaneDataManager warPlaneDataManager;
    @Autowired
    private BattlePassDataManager battlePassDataManager;
    @Autowired
    private ActivityTriggerService activityTriggerService;
    @Autowired
    private RoyalArenaService royalArenaService;
    @Autowired
    private TotemService totemService;

    private static final int FAIL_COST_ACT = 2;

    /**
     * 获取关卡信息
     *
     * @param roleId
     * @return
     */
    public GetCombatRs getCombat(long roleId) {
        Player player = playerDataManager.getPlayer(roleId);
        if (player == null) {
            return null;
        }
        GetCombatRs.Builder builder = GetCombatRs.newBuilder();

        Iterator<Combat> it = player.combats.values().iterator();
        while (it.hasNext()) {
            builder.addCombat(PbHelper.createCombatPb(it.next()));
        }

        // 高级副本信息
        Iterator<CombatFb> it2 = player.combatFb.values().iterator();
        CombatFb fb;
        StaticCombat staticCombat;
        StaticSuperEquip staticSuperEquip;
        int itemId;
        while (it2.hasNext()) {
            // 过滤国器碎片已完成副本
            fb = it2.next();
            staticCombat = StaticCombatDataMgr.getStaticCombat(fb.getCombatId());
            if (staticCombat != null && staticCombat.getType() == CombatType.type_3 && fb.getStatus() == 0
                    && staticCombat.getExtand().size() > 2) {
                itemId = staticCombat.getExtand().get(0);
                Prop prop = player.props.get(itemId);
                int num = prop != null ? prop.getCount() : 0;
                int maxNum = staticCombat.getExtand().get(1);
                if (num >= maxNum) {
                    LogUtil.debug("已拥有国器碎片， 副本结束" + roleId);
                    fb.setCnt(0);
                    fb.setStatus(1);
                } else if (fb.getCnt() > num) {
                    fb.setCnt(maxNum - num);
                }
                for (Entry<Integer, SuperEquip> kv : player.supEquips.entrySet()) {
                    staticSuperEquip = StaticPropDataMgr.getSuperEquip(kv.getKey());
                    if (!staticSuperEquip.getMaterial().isEmpty() && !staticSuperEquip.getMaterial().get(0).isEmpty()
                            && staticSuperEquip.getMaterial().get(0).get(1) == itemId) {
                        LogUtil.debug("已拥有国器， 副本结束" + roleId);
                        fb.setCnt(0);
                        fb.setStatus(1);
                    }
                }
                // 国器副本在队列中的判断
                for (TwoInt kv : player.supEquipQue) {
                    staticSuperEquip = StaticPropDataMgr.getSuperEquip(kv.getV1());
                    if (!staticSuperEquip.getMaterial().isEmpty() && !staticSuperEquip.getMaterial().get(0).isEmpty()
                            && staticSuperEquip.getMaterial().get(0).get(1) == itemId) {
                        LogUtil.debug("已拥有国器,正在队列中,副本结束" + roleId);
                        fb.setCnt(0);
                        fb.setStatus(1);
                    }
                }
            }
            builder.addCombatFB(PbHelper.createCombatFBPb(fb));
        }

        builder.setCombatId(player.lord.combatId);
        if (!CheckNull.isEmpty(player.combatHeroForm)) {
            builder.addAllHeroId(player.combatHeroForm);
        }
        return builder.build();
    }

    /**
     * 挑战关卡 副本类型(1普通副本,3国器碎片副本,5资源建筑图纸副本,7建筑副本,不需要同步体力,客户端自己算)
     *
     * @param roleId
     * @param combatId
     * @param wipe
     * @return
     * @throws MwException
     */
    public DoCombatRs doCombat(long roleId, int combatId, boolean wipe, List<Integer> heroIds) throws MwException {
        Player player = playerDataManager.getPlayer(roleId);

        StaticCombat staticCombat = StaticCombatDataMgr.getStaticCombat(combatId);
        if (staticCombat == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(),
                    "挑战副本时，无关卡配置, roleId:" + roleId + ",combatId=" + combatId);
        }

        if (combatId != Constant.INIT_COMBAT_ID) {
            if (player.lord.combatId < staticCombat.getPreId()) {
                throw new MwException(GameError.COMBAT_PASS_BEFORE.getCode(), "挑战关卡时，前置关卡未通关, roleId:" + roleId
                        + ",nowId=" + player.lord.combatId + ",needId=" + staticCombat.getPreId());
            }
        }

        if (staticCombat.getType() == CombatType.type_1) {
            Combat combat = player.combats.get(combatId);
            if (staticCombat.getCnt() == 1 && combat != null && combat.getStar() > 0) {
                throw new MwException(GameError.COMBAT_PASSED.getCode(), "挑战关卡时，只能挑战一次, roleId:" + roleId);
            }
        }

        // 将领判断
        checkHeroCombat(player, heroIds);

        // 保存副本将领的顺序
        // List<Integer> combatPosList = player.heroBattlePos.get(HeroConstant.CHANGE_COMBAT_POS_TYPE);
        // if (CheckNull.isEmpty(combatPosList)) {
        // combatPosList = new ArrayList<>();
        // player.heroBattlePos.put(HeroConstant.CHANGE_COMBAT_POS_TYPE, combatPosList);
        // }
        // combatPosList.clear();
        // combatPosList.addAll(combatPos);

        // 扫荡不提前扣体力
        if (wipe) {
            return doCombatWipe(player, combatId);
        }
        if (staticCombat.getCost() > 0) {
            try {
                rewardDataManager.checkAndSubPlayerRes(player, AwardType.MONEY, AwardType.Money.ACT,
                        staticCombat.getCost(), AwardFrom.COMBAT_FIGHT, false);
            } catch (MwException mwException) {
                // 攻打副本行动力不足
                activityService.checkTriggerGiftSync(ActivityConst.TRIGGER_GIFT_DOCOMBAT_ACT, player);
                throw mwException;
            }
        }

        if (staticCombat.getType() == CombatType.type_1) {
            return doNormalCombat(player, staticCombat, heroIds);
        } else if (staticCombat.getType() == CombatType.type_2) {
            return doResCombat(player, staticCombat);
        } else if (staticCombat.getType() == CombatType.type_3 || staticCombat.getType() == CombatType.type_5) {
            return doWeaponCombat(player, staticCombat, heroIds);
        } else if (staticCombat.getType() == CombatType.type_4) {
            return doBuyCombat(player, staticCombat);
        } else if (staticCombat.getType() == CombatType.type_6) {
            return doBuyItemCombat(player, staticCombat);
        } else if (staticCombat.getType() == CombatType.type_7) {
            return doBuildingCombat(player, staticCombat, heroIds);
        } else if (staticCombat.getType() == CombatType.type_8) {
            return doGetRewardCombat(player, staticCombat);
        } else if (staticCombat.getType() == CombatType.type_9) {
            return doMiniGameCombat(player, staticCombat);
        }
        return null;
    }

    /**
     * 小游戏副本
     *
     * @param player       玩家
     * @param staticCombat 副本配置
     * @return
     */
    private DoCombatRs doMiniGameCombat(Player player, StaticCombat staticCombat) {
        int combatId = staticCombat.getCombatId();
        int awardMax = staticCombat.getExtand().get(0);
        CombatFb combatFb = player.combatFb.computeIfAbsent(combatId, (combat) -> new CombatFb(combatId, 0, 0, -1));
        DoCombatRs.Builder builder = DoCombatRs.newBuilder();

        if (combatFb.getBuyCnt() < awardMax) {
            builder.addAllAward(rewardDataManager.sendReward(player, staticCombat.getFirstAward(), 1, AwardFrom.GAIN_COMBAT));
            combatFb.setBuyCnt(combatFb.getBuyCnt() + 1);
        }

        // 小游戏副本增加日志记录 (基本信息、战力、关卡类型、关卡id、关卡消耗、状态)
        LogLordHelper.commonLog("combatFight", AwardFrom.COMBAT_FIGHT, player, player.lord.getFight(), staticCombat.getType(), staticCombat.getCombatId(), staticCombat.getCost(), 1);

        builder.addCombatFB(PbHelper.createCombatFBPb(combatFb));
        return builder.build();
    }

    /**
     * 副本星级计算
     *
     * @param lost
     * @param total
     * @return
     */
    public int combatStarCalc(int lost, int total) {
        int lostRatio = (int) ((lost * 1.0 / total) * Constant.TEN_THROUSAND);
        for (List<Integer> item : Constant.COMBAT_STAR_RULE) {
            if (item.get(1) <= lostRatio && lostRatio <= item.get(2)) {
                return item.get(0);
            }
        }
        return 1;
    }

    /**
     * 普通副本
     *
     * @param player
     * @param staticCombat
     * @return
     */
    private DoCombatRs doNormalCombat(Player player, StaticCombat staticCombat, List<Integer> heroIds)
            throws MwException {
        long roleId = player.lord.getLordId();
        int combatId = staticCombat.getCombatId();
        Combat combat = player.combats.get(combatId);
        boolean isFristDo = combat == null;
        Fighter attacker = fightService.createCombatPlayerFighter(player, heroIds);
        Fighter defender = fightService.createNpcFighter(isFristDo && !CheckNull.isEmpty(staticCombat.getFirstForm())
                ? staticCombat.getFirstForm() : staticCombat.getForm());
        FightLogic fightLogic = new FightLogic(attacker, defender, true);
        if (Constant.DONT_DODGE_CRIT_COMBATID.contains(combatId)) {
            fightLogic.setCareCrit(false);
            fightLogic.setCareDodge(false);
        }
        fightLogic.start();

        if (combat == null) {
            combat = new Combat(combatId, 0);
            player.combats.put(combatId, combat);
            // starChange = true;
        }
        // 获取活动翻倍
        int num = activityDataManager.getActDoubleNum(player);

        DoCombatRs.Builder builder = DoCombatRs.newBuilder();
        builder.setResult(fightLogic.getWinState());
        boolean firstPass = false;
        if (fightLogic.getWinState() == 1) {
            if (combat == null || combat.getStar() == 0) {
                firstPass = true;
            }
            if (combatId > player.lord.combatId) {
                player.lord.combatId = combatId;
                rankDataManager.setStars(player.lord);
            }

            // int star = fightLogic.estimateStar();
            int star = combatStarCalc(attacker.getLost(), attacker.getTotal());

            // if (staticCombat.getCnt() == 0) {
            // if (attacker.getLost() > attacker.getTotal() / 2) {
            // LogUtil.channel("combat atk,total=" + attacker.getTotal() + ",lost=" + attacker.getLost());
            // star = 1;
            // } else if (attacker.cntDieForce() > 0) {
            // LogUtil.channel("combat atk,total=" + attacker.getTotal() + ",lost=" + attacker.getLost());
            // star = 2;
            // }
            // } else {
            // star = 1;
            // }
            // builder.addAllAward(dropCombatAward(player, staticCombat.getDrop()));

            if (star > combat.getStar()) {
                combat.setStar(star);
            }
            if (firstPass && !CheckNull.isEmpty(staticCombat.getFirstAward())) {
                builder.addAllAward(
                        rewardDataManager.sendReward(player, staticCombat.getFirstAward(), num, AwardFrom.GAIN_COMBAT));
            }
            if (!firstPass && !CheckNull.isEmpty(staticCombat.getWinAward())) {
                if (staticCombat.getIndex() == 6) {// 每章最后一节才会有副本活动奖励
                    num *= activityDataManager.getActCombatDoubleNum(player);
                }
                builder.addAllAward(
                        rewardDataManager.sendReward(player, staticCombat.getWinAward(), num, AwardFrom.GAIN_COMBAT));
            }

            //处理随机奖励
            List<CommonPb.Award> randomAwardList = getRandomAward(player, staticCombat.getRandomAward());
            if (ListUtils.isNotBlank(randomAwardList)) {
                rewardDataManager.sendRewardByAwardList(player, randomAwardList, AwardFrom.GAIN_COMBAT);
                builder.addAllAward(randomAwardList);
            }

            if (!CheckNull.isEmpty(staticCombat.getTitanDrop()) && player.lord.getLevel() >= Constant.RED_DROP_AWARD_LV) {
                builder.addAllAward(rewardDataManager.sendReward(player, staticCombat.getTitanDrop(), 1, AwardFrom.GAIN_COMBAT));
            }
            // 副本掉落活动
            List<List<Integer>> actCombatDropAward = activityDataManager.getActCombatDrop(player, 1);
            if (!CheckNull.isEmpty(actCombatDropAward)) {
                builder.addAllAward(rewardDataManager.sendReward(player, actCombatDropAward, AwardFrom.GAIN_COMBAT));
            }

            //增加图腾掉落
            builder.addAllAward(totemService.dropTotem(player, 1, AwardFrom.TOTOEM_DROP_GUANQIA));

            // 勋章掉落
            List<Medal> medals = medalDataManager.getMedalBydoCombat(player);
            for (Medal medal : medals) {
                builder.addMedals(PbHelper.createMedalPb(medal));
            }

            builder.setStar(star);
            activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_COMBAT);
            // activityDataManager.updActivity(player, ActivityConst.ACT_COMBAT, 1, 0);
            taskDataManager.updTask(player, TaskType.COND_COMBAT_ID_WIN, 1, combatId);
            processNextCombat(player, combat, builder);
            taskDataManager.updTask(player, TaskType.COND_COMBAT_37, 1);
            battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_COMBAT_37, 1);
            royalArenaService.updTaskSchedule(player.roleId, TaskType.COND_COMBAT_37, 1);
            // 触发检测功能解锁
            buildingDataManager.refreshSourceData(player);
            // 挑战战役活动
            activityDataManager.updActivity(player, ActivityConst.ACT_CHALLENGE_COMBAT, 1, combatId, true);
            // 通关副本
            activityTriggerService.doCombatTriggerGift(player, combatId);

            //貂蝉任务-成功通关关卡，包含扫荡
            ActivityDiaoChanService.completeTask(player, ETask.PASS_BARRIER, 1);
            //喜悦金秋-日出而作-通关战役xx次（包含扫荡）
            TaskService.processTask(player, ETask.PASS_BARRIER, 1);
        } else {
            // 攻打副本失败
            // 更新触发式礼包进度
            activityDataManager.updateTriggerStatus(ActivityConst.TRIGGER_GIFT_DOCOMBAT_FAIL, player, 1);
            activityService.checkTriggerGiftSync(ActivityConst.TRIGGER_GIFT_DOCOMBAT_FAIL, player);
            builder.setResult(-1);
            builder.setStar(0);
            if (staticCombat.getCost() > 0) {
                int count = staticCombat.getCost() - FAIL_COST_ACT;
                builder.addAward(rewardDataManager.addAwardSignle(player, AwardType.MONEY, AwardType.Money.ACT, count,
                        AwardFrom.COMBAT_FIGHT));
            }
        }

        // 普通副本增加日志记录 (基本信息、战力、关卡类型、关卡id、关卡消耗、状态)
        LogLordHelper.commonLog("combatFight", AwardFrom.COMBAT_FIGHT, player, player.lord.getFight()
                , staticCombat.getType(), staticCombat.getCombatId(), staticCombat.getCost(), fightLogic.getWinState());

        taskDataManager.updTask(player, TaskType.COND_ENTER_COMBAT_34, 1, combatId);
        taskDataManager.updTask(player, TaskType.COND_995, 1);

        // 给将领加经验
        builder.addAllAtkHero(fightSettleLogic.combatFightHeroExpReward(player, attacker.getForces(), staticCombat,
                false, fightLogic.getWinState() == 1));
        // 防守方hero信息
        builder.addAllDefHero(defender.forces.stream().map(force -> PbHelper.createRptHero(Constant.Role.BANDIT, force.killed, 0, force, null, 0, 0, force.totalLost)).collect(Collectors.toList()));
        // 给战机加经验
        addPlaneExp(player, staticCombat, 1, builder, heroIds);

        BattlePb.BattleRoundPb record = fightLogic.generateRecord();
        builder.setRecord(record);

        return builder.build();
    }

    /**
     * 获取staticCombat randomAward字段信息
     *
     * @param player
     * @param rewardList
     * @return
     */
    private List<CommonPb.Award> getRandomAward(Player player, List<List<Integer>> rewardList) {
        if (CheckNull.isEmpty(rewardList))
            return null;
        List<CommonPb.Award> resultList = null;
        List<CommonPb.Award> awardList_ = null;
        for (List<Integer> award : rewardList) {
            if (CheckNull.isEmpty(award))
                continue;
            if (award.get(0) == AwardType.RANDOM) {
                for (int i = 0; i < award.get(2); i++) {
                    awardList_ = rewardDataManager.getRandomAward(player, award.get(1), award.get(2));
                    if (CheckNull.nonEmpty(awardList_)) {
                        Iterator<CommonPb.Award> awardIterator = awardList_.iterator();
                        while (awardIterator.hasNext()) {
                            CommonPb.Award awardPb_ = awardIterator.next();
                            List<Integer> dropList = Constant.BATTLE_PICK_BOX_DROP_CAP.stream().filter(list -> list.get(0) == awardPb_.getType()
                                    && list.get(1) == awardPb_.getId()).findFirst().orElse(null);
                            if (CheckNull.isEmpty(dropList))
                                continue;
                            int dropCount = player.combatInfo.getCount(awardPb_.getType(), awardPb_.getId());
                            if (dropCount >= dropList.get(2)) {
                                awardIterator.remove();
                                continue;
                            }
                            player.combatInfo.updateCount(awardPb_.getType(), awardPb_.getId(), awardPb_.getCount());
                        }
                        if (CheckNull.nonEmpty(awardList_)) {
                            if (CheckNull.isNull(resultList)) resultList = new ArrayList<>();
                            resultList.addAll(awardList_);
                        }
                    }
                }

                awardList_ = null;
            } else {
                List<Integer> dropList = Constant.BATTLE_PICK_BOX_DROP_CAP.stream().filter(list -> list.get(0) == award.get(0)
                        && list.get(1) == award.get(1)).findFirst().orElse(null);
                if (CheckNull.nonEmpty(dropList)) {
                    int dropCount = player.combatInfo.getCount(award.get(0), award.get(1));
                    if (dropCount >= dropList.get(2))
                        continue;
                    player.combatInfo.updateCount(award.get(0), award.get(1), award.get(2));
                }
                if (CheckNull.isNull(awardList_)) {
                    awardList_ = new ArrayList<>();
                }
                awardList_.add(PbHelper.createAwardPb(award.get(0), award.get(1), award.get(2)));
            }

            if (CheckNull.nonEmpty(awardList_)) {
                if (CheckNull.isNull(resultList)) resultList = new ArrayList<>();
                resultList.addAll(awardList_);
            }
        }

        return resultList;
    }

    /**
     * 生产资源副本的倒计时
     *
     * @param player
     * @param preCombat
     * @param builder
     */
    private void processNextCombat(Player player, Combat preCombat, DoCombatRs.Builder builder) {
        List<StaticCombat> list = StaticCombatDataMgr.getPreIdCombat(preCombat.getCombatId());
        if (list == null || list.isEmpty()) {
            return;
        }
        for (StaticCombat staticCombat : list) {
            if (staticCombat.getType() == CombatType.type_2 && staticCombat.getCombatId() != preCombat.getCombatId()) {
                CombatFb combatFb = player.combatFb.get(staticCombat.getCombatId());
                int combatId = staticCombat.getCombatId();
                if (combatFb == null) {
                    combatFb = new CombatFb(combatId, 0, Constant.COMBAT_RES_CNT,
                            TimeHelper.getCurrentSecond() + Constant.COMBAT_RES_TIME);
                    player.combatFb.put(combatId, combatFb);
                }
                builder.addCombatFB(PbHelper.createCombatFBPb(combatFb));
            }
        }
    }

    /**
     * 8直接获得奖励 [类型,id,数量]
     *
     * @param player
     * @param staticCombat
     * @return
     */
    private DoCombatRs doGetRewardCombat(Player player, StaticCombat staticCombat) throws MwException {
        long roleId = player.roleId;
        CombatFb combatFb = player.combatFb.get(staticCombat.getCombatId());
        int type = staticCombat.getExtand().get(0);
        int itemId = staticCombat.getExtand().get(1);
        int itemNum = staticCombat.getExtand().get(2);
        int combatId = staticCombat.getCombatId();
        if (type == AwardType.EQUIP) {
            rewardDataManager.checkBagCnt(player);
        }
        if (combatFb == null) {
            combatFb = new CombatFb(combatId, 0, 0, -1);
            player.combatFb.put(combatId, combatFb);
        }
        if (combatFb.getStatus() > 0) {
            throw new MwException(GameError.COMBAT_PASS_BEFORE.getCode(), "招募副本时，已完成, roleId:" + roleId);
        }
        // 招募副本增加日志记录 (基本信息、战力、关卡类型、关卡id、关卡消耗、状态)
        LogLordHelper.commonLog("combatFight", AwardFrom.COMBAT_FIGHT, player, player.lord.getFight()
                , staticCombat.getType(), staticCombat.getCombatId(), staticCombat.getCost(), 1);

        combatFb.setStatus(1);
        DoCombatRs.Builder builder = DoCombatRs.newBuilder();
        builder.addAward(rewardDataManager.sendRewardSignle(player, type, itemId, itemNum, AwardFrom.COMBAT_TYPE_8));
        builder.addCombatFB(PbHelper.createCombatFBPb(combatFb));
        // 给将领穿上装备,临时需要
        if (type == AwardType.HERO && Constant.REWARD_EQUIP_HERO_IDS.contains(itemId)) {
            // 本次的结果发给客户端
            Base.Builder msg = PbHelper.createRsBase(GameError.OK, DoCombatRs.EXT_FIELD_NUMBER, DoCombatRs.ext,
                    builder.build());
            MsgDataManager.getIns().add(new Msg(player.ctx, msg.build(), player.roleId));
            // 发送其他结果
            Hero hero = player.heros.get(itemId);
            if (hero == null) {
                return null;
            }
            if (!CheckNull.isEmpty(Constant.REWARD_HERO_EQUIP)) {
                List<Integer> keyList = new ArrayList<>(); // 装备keyId
                for (List<Integer> item : Constant.REWARD_HERO_EQUIP) {
                    Integer t = item.get(0);
                    if (t == AwardType.EQUIP) {
                        Integer equipId = item.get(1);
                        StaticEquip sEquip = StaticPropDataMgr.getEquipMap().get(equipId);
                        if (null == sEquip) continue;// 没有装备配置去掉
                        List<Integer> key = rewardDataManager.addEquip(player, equipId, 1, AwardFrom.COMBAT_TYPE_8);
                        if (!CheckNull.isEmpty(key)) {
                            keyList.addAll(key);
                            // 穿装备
                            equipService.heroOnEquipIsUpTask(player, hero, sEquip.getEquipPart(), key.get(0), false);
                        }
                    }
                }
                // 发推送
                SyncHeroEquipRs.Builder heBuild = SyncHeroEquipRs.newBuilder();
                heBuild.addHero(PbHelper.createHeroPb(hero, player));
                for (Integer equipKey : keyList) {
                    heBuild.addEquip(PbHelper.createEquipPb(player.equips.get(equipKey)));
                }
                Base.Builder heMsg = PbHelper.createRsBase(SyncHeroEquipRs.EXT_FIELD_NUMBER, SyncHeroEquipRs.ext,
                        heBuild.build());
                MsgDataManager.getIns().add(new Msg(player.ctx, heMsg.build(), player.roleId));
            }
            return null;
        }
        return builder.build();
    }

    /**
     * 2资源副本 [资源ID，资源数量，[第1次付费金币数，第2次付费金币数，……第10次付费金币数]; 副本类型(1普通副本,2资源副本,3国器碎片副本,4招募副本,5资源建筑图纸副本,6装备图纸副本,7建筑副本);
     *
     * @param player
     * @param staticCombat
     * @return
     * @throws MwException
     */
    private DoCombatRs doResCombat(Player player, StaticCombat staticCombat) throws MwException {
        long roleId = player.roleId;
        int resId = staticCombat.getExtand().get(0);
        int resNum = staticCombat.getExtand().get(1);
        CombatFb combatFb = player.combatFb.get(staticCombat.getCombatId());
        int combatId = staticCombat.getCombatId();
        if (combatFb == null) {
            combatFb = new CombatFb(combatId, 0, Constant.COMBAT_RES_CNT,
                    TimeHelper.getCurrentSecond() + Constant.COMBAT_RES_TIME);
            player.combatFb.put(combatId, combatFb);
        }
        if (combatFb.getStatus() > 0) {
            throw new MwException(GameError.COMBAT_FINISHED.getCode(), "资源副本，已完成, roleId:" + roleId);
        }
        if (combatFb.getCnt() <= 0) {
            throw new MwException(GameError.COMBAT_RES_NO_CNT.getCode(), "资源副本时，次数不够, roleId:" + roleId);
        }
        // 过时不允许买
        if (TimeHelper.getCurrentSecond() > combatFb.getEndTime()) {
            throw new MwException(GameError.COMBAT_RES_NO_TIME.getCode(), "资源副本时，已过时, roleId:" + roleId);
        }
        combatFb.setCnt(combatFb.getCnt() - 1);
        if (combatFb.getCnt() == 0) { // && combatFb.getEndTime() == -1
            // combatFb.setEndTime(TimeHelper.getCurrentSecond() + Constant.COMBAT_RES_TIME);

            // 最后一次用完了
            if (combatFb.getBuyCnt() >= staticCombat.getExtand().size() - 2) {
                combatFb.setStatus(1);
            }
        }
        rewardDataManager.sendRewardSignle(player, AwardType.RESOURCE, resId, resNum, AwardFrom.COMBAT_TYPE_2);

        // 资源副本增加日志记录 (基本信息、战力、关卡类型、关卡id、关卡消耗、状态)
        LogLordHelper.commonLog("combatFight", AwardFrom.COMBAT_FIGHT, player, player.lord.getFight()
                , staticCombat.getType(), staticCombat.getCombatId(), staticCombat.getCost(), 1);

        DoCombatRs.Builder builder = DoCombatRs.newBuilder();
        // 勋章掉落
        List<Medal> medals = medalDataManager.getMedalBydoCombat(player);
        for (Medal medal : medals) {
            builder.addMedals(PbHelper.createMedalPb(medal));
        }
        builder.addAward(PbHelper.createAwardPb(AwardType.RESOURCE, resId, resNum, 0));
        builder.addCombatFB(PbHelper.createCombatFBPb(combatFb));
        return builder.build();

    }

    /**
     * 4招募副本[将领ID，招募价格，招募几率, 将领ID，招募价格，招募几率]两个招募几率相加为100
     *
     * @param player
     * @param staticCombat
     * @return
     * @throws MwException
     */
    private DoCombatRs doBuyCombat(Player player, StaticCombat staticCombat) throws MwException {
        long roleId = player.roleId;
        CombatFb combatFb = player.combatFb.get(staticCombat.getCombatId());
        int combatId = staticCombat.getCombatId();
        if (combatFb == null) {
            combatFb = new CombatFb(combatId, 0, 0, -1);
            player.combatFb.put(combatId, combatFb);
        }
        if (combatFb.getStatus() > 0) {
            throw new MwException(GameError.COMBAT_FINISHED.getCode(), "招募副本时，已完成, roleId:" + roleId);
        }
        DoCombatRs.Builder builder = DoCombatRs.newBuilder();
        if (combatFb.getGain() > 0) {
            int gold = 0;
            int heroId = 0;
            if (staticCombat.getExtand().get(0) == combatFb.getGain()) {
                gold = staticCombat.getExtand().get(4);
                heroId = staticCombat.getExtand().get(3);
            } else if (staticCombat.getExtand().get(3) == combatFb.getGain()) {
                gold = staticCombat.getExtand().get(1);
                heroId = staticCombat.getExtand().get(0);
            } else {
                throw new MwException(GameError.COMBAT_SETTING_ERROR.getCode(), "招募将领时, 将领配置已更改");
            }
            rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, gold,
                    AwardFrom.COMBAT_TYPE_4, staticCombat.getCombatId());
            // rewardDataManager.checkMoneyIsEnough(player.lord, AwardType.Money.GOLD, gold, "招募副本");
            // rewardDataManager.subMoney(player, AwardType.Money.GOLD, gold, AwardFrom.COMBAT_TYPE_4, "招募副本");
            combatFb.setStatus(1);
            rewardDataManager.addAwardSignle(player, AwardType.HERO, heroId, 1, AwardFrom.COMBAT_TYPE_4,
                    staticCombat.getCombatId());
            builder.addAward(PbHelper.createAwardPb(AwardType.HERO, heroId, 1, 0));
        } else {
            int random = RandomUtils.nextInt(0, 100);
            int heroId = 0;
            if (staticCombat.getExtand().get(2) > random) {
                heroId = staticCombat.getExtand().get(0);
                combatFb.setGain(heroId);
                rewardDataManager.addAwardSignle(player, AwardType.HERO, heroId, 1, AwardFrom.COMBAT_TYPE_4,
                        staticCombat.getCombatId());
            } else {
                heroId = staticCombat.getExtand().get(3);
                combatFb.setGain(heroId);
                rewardDataManager.addAwardSignle(player, AwardType.HERO, heroId, 1, AwardFrom.COMBAT_TYPE_4,
                        staticCombat.getCombatId());
            }
            builder.addAward(PbHelper.createAwardPb(AwardType.HERO, heroId, 1, 0));
        }

        // 招募副本增加日志记录 (基本信息、战力、关卡类型、关卡id、关卡消耗、状态)
        LogLordHelper.commonLog("combatFight", AwardFrom.COMBAT_FIGHT, player, player.lord.getFight()
                , staticCombat.getType(), staticCombat.getCombatId(), staticCombat.getCost(), 1);

        // 勋章掉落
        List<Medal> medals = medalDataManager.getMedalBydoCombat(player);
        for (Medal medal : medals) {
            builder.addMedals(PbHelper.createMedalPb(medal));
        }

        builder.addCombatFB(PbHelper.createCombatFBPb(combatFb));
        return builder.build();
    }

    /**
     * 6装备图纸副本 [图纸ID，消耗金币数]
     *
     * @param player
     * @param staticCombat
     * @return
     * @throws MwException
     */
    private DoCombatRs doBuyItemCombat(Player player, StaticCombat staticCombat) throws MwException {
        long roleId = player.roleId;
        CombatFb combatFb = player.combatFb.get(staticCombat.getCombatId());
        int gold = staticCombat.getExtand().get(1);
        int itemId = staticCombat.getExtand().get(0);
        int combatId = staticCombat.getCombatId();
        if (combatFb == null) {
            combatFb = new CombatFb(combatId, 0, 1, -1);
            player.combatFb.put(combatId, combatFb);
        }
        if (combatFb.getStatus() > 0) {
            throw new MwException(GameError.COMBAT_FINISHED.getCode(), "招募副本时，已完成, roleId:" + roleId);
        }

        rewardDataManager.checkAndSubPlayerResHasSync(player, AwardType.MONEY, AwardType.Money.GOLD, gold,
                AwardFrom.COMBAT_TYPE_6, staticCombat.getCombatId());
        // rewardDataManager.checkMoneyIsEnough(player.lord, AwardType.Money.GOLD, gold, "装备图纸副本");
        // rewardDataManager.subMoney(player, AwardType.Money.GOLD, gold, AwardFrom.COMBAT_TYPE_6, "装备图纸副本");

        combatFb.setStatus(1);

        int count = 1;

        // 装备图纸副本增加日志记录 (基本信息、战力、关卡类型、关卡id、关卡消耗、状态)
        LogLordHelper.commonLog("combatFight", AwardFrom.COMBAT_FIGHT, player, player.lord.getFight()
                , staticCombat.getType(), staticCombat.getCombatId(), staticCombat.getCost(), 1);

        // 挑战战役活动
        activityDataManager.updActivity(player, ActivityConst.ACT_CHALLENGE_COMBAT, 1, combatId, true);

        DoCombatRs.Builder builder = DoCombatRs.newBuilder();
        // 勋章掉落
        List<Medal> medals = medalDataManager.getMedalBydoCombat(player);
        for (Medal medal : medals) {
            builder.addMedals(PbHelper.createMedalPb(medal));
        }
        builder.addAward(
                rewardDataManager.addAwardSignle(player, AwardType.PROP, itemId, count, AwardFrom.COMBAT_TYPE_3));
        builder.addCombatFB(PbHelper.createCombatFBPb(combatFb));
        return builder.build();
    }

    /**
     * 3国器碎片副本 配置需求 [国器碎片ID，可获取数量，攻打掉落获得道具几率] ; 5资源建筑图纸副本[图纸ID，可获取数量，攻打掉落获得道具几率];
     *
     * @param player
     * @param staticCombat
     * @return
     * @throws MwException
     */
    private DoCombatRs doWeaponCombat(Player player, StaticCombat staticCombat, List<Integer> heroIds)
            throws MwException {
        long roleId = player.roleId;
        CombatFb combatFb = player.combatFb.get(staticCombat.getCombatId());
        int combatId = staticCombat.getCombatId();
        int itemId = staticCombat.getExtand().get(0);
        int itemNum = staticCombat.getExtand().get(1);
        int pro = staticCombat.getExtand().get(2);
        if (combatFb == null) {
            combatFb = new CombatFb(combatId, 0, itemNum, -1);
            player.combatFb.put(combatId, combatFb);
        }

        if (combatFb.getStatus() > 0) {
            throw new MwException(GameError.COMBAT_FINISHED.getCode(), "招募副本时，已完成, roleId:" + roleId);
        }
        if (combatFb.getCnt() <= 0) {
            throw new MwException(GameError.COMBAT_RES_NO_CNT.getCode(), "资源副本时，次数不够, roleId:" + roleId);
        }

        Fighter attacker = fightService.createCombatPlayerFighter(player, heroIds);
        Fighter defender = fightService.createNpcFighter(staticCombat.getForm());
        FightLogic fightLogic = new FightLogic(attacker, defender, true);
        if (Constant.DONT_DODGE_CRIT_COMBATID.contains(combatId)) {
            fightLogic.setCareCrit(false);
            fightLogic.setCareDodge(false);
        }
        fightLogic.start();

        DoCombatRs.Builder builder = DoCombatRs.newBuilder();
        builder.setResult(fightLogic.getWinState());
        if (fightLogic.getWinState() == 1) {
            int count = 1;
            if (pro > RandomUtils.nextInt(0, 100)) {
                combatFb.setCnt(combatFb.getCnt() - 1);
                builder.addAward(rewardDataManager.sendRewardSignle(player, AwardType.PROP, itemId, count,
                        AwardFrom.COMBAT_TYPE_3));
                if (staticCombat.getType() == CombatType.type_3) {// 3国器碎片副本
                    long myPropCount = rewardDataManager.getRoleResByType(player, AwardType.PROP, itemId);
                    if (myPropCount >= itemNum) {
                        LogUtil.debug("国器副本已经足够,隐藏副本  roleId:", player.roleId, " , 国器的数量:", myPropCount, ", 国器的id:",
                                itemId);
                        combatFb.setStatus(1);
                    }
                } else if (staticCombat.getType() == CombatType.type_5) {// 5资源建筑图纸副本
                    if (combatFb.getCnt() <= 0) {
                        combatFb.setStatus(1);
                    }
                }
            }

            boolean firstPass = false;
            if (combatFb.getStatus() == 0 && itemNum == combatFb.getCnt()) {
                firstPass = true;
            }
            if (combatId > player.lord.combatId) {
                player.lord.combatId = combatId;
                LogUtil.debug("国器副本或资源副本设置的当前进度  combatId:" + combatId + ", roleId:" + player.lord.getLordId());
            }
            if (firstPass && !CheckNull.isEmpty(staticCombat.getFirstAward())) {
                builder.addAllAward(rewardDataManager.sendReward(player, staticCombat.getFirstAward(), count,
                        AwardFrom.GAIN_COMBAT));
            }
            if (!firstPass && !CheckNull.isEmpty(staticCombat.getWinAward())) {
                builder.addAllAward(
                        rewardDataManager.sendReward(player, staticCombat.getWinAward(), count, AwardFrom.GAIN_COMBAT));
            }
            if (!CheckNull.isEmpty(staticCombat.getTitanDrop()) && player.lord.getLevel() >= Constant.RED_DROP_AWARD_LV) {
                builder.addAllAward(rewardDataManager.sendReward(player, staticCombat.getTitanDrop(), count, AwardFrom.GAIN_COMBAT));
            }
            // 勋章掉落
            List<Medal> medals = medalDataManager.getMedalBydoCombat(player);
            for (Medal medal : medals) {
                builder.addMedals(PbHelper.createMedalPb(medal));
            }

            // 触发检测功能解锁
            buildingDataManager.refreshSourceData(player);
            // 挑战战役活动
            activityDataManager.updActivity(player, ActivityConst.ACT_CHALLENGE_COMBAT, 1, combatId, true);
        } else {
            // 攻打副本失败
            activityService.checkTriggerGiftSync(ActivityConst.TRIGGER_GIFT_DOCOMBAT_FAIL, player);
            builder.setResult(-1);
            builder.setStar(0);
            if (staticCombat.getCost() > 0) {
                int count = staticCombat.getCost() - FAIL_COST_ACT;
                builder.addAward(rewardDataManager.addAwardSignle(player, AwardType.MONEY, AwardType.Money.ACT, count,
                        AwardFrom.COMBAT_FIGHT));
            }
        }

        // 国器碎片副本增加日志记录 (基本信息、战力、关卡类型、关卡id、关卡消耗、状态)
        LogLordHelper.commonLog("combatFight", AwardFrom.COMBAT_FIGHT, player, player.lord.getFight()
                , staticCombat.getType(), staticCombat.getCombatId(), staticCombat.getCost(), fightLogic.getWinState());

        //更新任务
        taskDataManager.updTask(player, TaskType.COND_995, 1);


        // 给将领加经验
        builder.addAllAtkHero(fightSettleLogic.combatFightHeroExpReward(player, attacker.getForces(), staticCombat,
                false, fightLogic.getWinState() == 1));
        // 防守方hero信息
        builder.addAllDefHero(defender.forces.stream().map(force -> PbHelper.createRptHero(Constant.Role.BANDIT, force.killed, 0, force, null, 0, 0, force.totalLost)).collect(Collectors.toList()));
        // 给战机加经验
        addPlaneExp(player, staticCombat, 1, builder, heroIds);

        builder.addCombatFB(PbHelper.createCombatFBPb(combatFb));

        BattlePb.BattleRoundPb record = fightLogic.generateRecord();
        builder.setRecord(record);

        return builder.build();
    }

    /**
     * 7建筑副本
     *
     * @param player
     * @param staticCombat
     * @return
     * @throws MwException
     */
    private DoCombatRs doBuildingCombat(Player player, StaticCombat staticCombat, List<Integer> heroIds)
            throws MwException {
        long roleId = player.roleId;
        CombatFb combatFb = player.combatFb.get(staticCombat.getCombatId());
        int combatId = staticCombat.getCombatId();
        if (combatFb == null) {
            combatFb = new CombatFb(combatId, 0, 1, -1);
            player.combatFb.put(combatId, combatFb);
        }

        if (combatFb.getStatus() > 0) {
            throw new MwException(GameError.COMBAT_FINISHED.getCode(), "招募副本时，已完成, roleId:" + roleId);
        }
        if (combatFb.getCnt() <= 0) {
            throw new MwException(GameError.COMBAT_RES_NO_CNT.getCode(), "资源副本时，次数不够, roleId:" + roleId);
        }

        Fighter attacker = fightService.createCombatPlayerFighter(player, heroIds);
        Fighter defender = fightService.createNpcFighter(staticCombat.getForm());
        FightLogic fightLogic = new FightLogic(attacker, defender, true);

        fightLogic.start();

        DoCombatRs.Builder builder = DoCombatRs.newBuilder();
        builder.setResult(fightLogic.getWinState());
        if (fightLogic.getWinState() == 1) {
            int count = 1;

            boolean firstPass = false;
            // if (combatId > player.lord.combatId) {
            if (combatFb.getStatus() == 0) {
                combatFb.setStatus(1);
                firstPass = true;
            }
            if (combatId > player.lord.combatId) {
                player.lord.combatId = combatId;
                // 触发检测功能解锁
                buildingDataManager.refreshSourceData(player);
            }
            combatFb.setStatus(1);
            if (firstPass && !CheckNull.isEmpty(staticCombat.getFirstAward())) {
                builder.addAllAward(rewardDataManager.sendReward(player, staticCombat.getFirstAward(), count,
                        AwardFrom.GAIN_COMBAT));
            }
            if (!firstPass && !CheckNull.isEmpty(staticCombat.getWinAward())) {
                builder.addAllAward(
                        rewardDataManager.sendReward(player, staticCombat.getWinAward(), count, AwardFrom.GAIN_COMBAT));
            }
            if (!CheckNull.isEmpty(staticCombat.getTitanDrop()) && player.lord.getLevel() >= Constant.RED_DROP_AWARD_LV) {
                builder.addAllAward(rewardDataManager.sendReward(player, staticCombat.getTitanDrop(), count, AwardFrom.GAIN_COMBAT));
            }
            taskDataManager.updTask(player, TaskType.COND_COMBAT_ID_WIN, 1, combatId);

            // 勋章掉落
            List<Medal> medals = medalDataManager.getMedalBydoCombat(player);
            for (Medal medal : medals) {
                builder.addMedals(PbHelper.createMedalPb(medal));
            }
            // 挑战战役活动
            activityDataManager.updActivity(player, ActivityConst.ACT_CHALLENGE_COMBAT, 1, combatId, true);
        } else {
            // 攻打副本失败
            activityService.checkTriggerGiftSync(ActivityConst.TRIGGER_GIFT_DOCOMBAT_FAIL, player);
            builder.setResult(-1);
            if (staticCombat.getCost() > 0) {
                int count = staticCombat.getCost() - FAIL_COST_ACT;
                builder.addAward(rewardDataManager.addAwardSignle(player, AwardType.MONEY, AwardType.Money.ACT, count,
                        AwardFrom.COMBAT_FIGHT));
            }
        }

        // 建筑副本增加日志记录 (基本信息、战力、关卡类型、关卡id、关卡消耗、状态)
        LogLordHelper.commonLog("combatFight", AwardFrom.COMBAT_FIGHT, player, player.lord.getFight()
                , staticCombat.getType(), staticCombat.getCombatId(), staticCombat.getCost(), fightLogic.getWinState());


        taskDataManager.updTask(player, TaskType.COND_ENTER_COMBAT_34, 1, combatId);
        // 给将领加经验
        builder.addAllAtkHero(fightSettleLogic.combatFightHeroExpReward(player, attacker.getForces(), staticCombat,
                false, fightLogic.getWinState() == 1));
        // 防守方hero信息
        builder.addAllDefHero(defender.forces.stream().map(force -> PbHelper.createRptHero(Constant.Role.BANDIT, force.killed, 0, force, null, 0, 0, force.totalLost)).collect(Collectors.toList()));
        // 给战机加经验
        addPlaneExp(player, staticCombat, 1, builder, heroIds);

        builder.addCombatFB(PbHelper.createCombatFBPb(combatFb));

        BattlePb.BattleRoundPb record = fightLogic.generateRecord();
        builder.setRecord(record);

        return builder.build();
    }

    /**
     * 关卡扫荡
     *
     * @param player
     * @param combatId
     * @return
     * @throws MwException
     */
    public DoCombatRs doCombatWipe(Player player, int combatId) throws MwException {
        long roleId = player.lord.getLordId();
        StaticCombat staticCombat = StaticCombatDataMgr.getStaticCombat(combatId);
        if (staticCombat == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(),
                    "挑战副本时，无关卡配置, roleId:" + roleId + ",combatId=" + combatId);
        }

        if (combatId != Constant.INIT_COMBAT_ID || staticCombat.getType() != 1) {
            if (player.lord.combatId < staticCombat.getPreId()) {
                throw new MwException(GameError.COMBAT_PASS_BEFORE.getCode(), "挑战关卡时，前置关卡未通关, roleId:" + roleId);
            }
        }

        boolean isOpenWipe = vipDataManager.isOpen(player.lord.getVip(), VipConstant.WIPE);

        if (!player.combats.containsKey(combatId) || (!isOpenWipe && player.combats.get(combatId).getStar() < 3)) {
            throw new MwException(GameError.COMBAT_PASS_BEFORE.getCode(),
                    "扫荡卡时，未三星通关, roleId:" + roleId + ",combatId=" + combatId);
        }

        int cnt = player.lord.getPower() / staticCombat.getCost();
        // 西点军校没有开启只能
//        if (!buildingDataManager.checkBuildingLock(player, BuildingType.WAR_FACTORY) && cnt < 5) {
//            activityService.checkTriggerGiftSync(ActivityConst.TRIGGER_GIFT_DOCOMBAT_ACT, player);
//            throw new MwException(GameError.ACT_NOT_ENOUGH.getCode(),
//                    "西点军校没开启 , 扫荡副本体力不足, roleId:" + roleId + ",combatId:" + combatId, ",cnt:", cnt);
//        }
        if (cnt > 0) {
            cnt = cnt >= 5 ? 5 : cnt;
            rewardDataManager.checkAndSubPlayerRes(player, AwardType.MONEY, AwardType.Money.ACT,
                    staticCombat.getCost() * cnt, AwardFrom.COMBAT_FIGHT, false);
        } else {
            // 攻打副本行动力不足
            activityService.checkTriggerGiftSync(ActivityConst.TRIGGER_GIFT_DOCOMBAT_ACT, player);
            throw new MwException(GameError.ACT_NOT_ENOUGH.getCode(),
                    "扫荡副本体力不足, roleId:" + roleId + ",combatId:" + combatId, ",cnt:", cnt);
        }

        LogUtil.debug("扫荡次数=" + cnt);

        //扫荡直接成功 普通副本增加日志记录 (基本信息、战力、关卡类型、关卡id、关卡消耗、状态)
        LogLordHelper.commonLog("combatFight", AwardFrom.COMBAT_FIGHT, player, player.lord.getFight()
                , staticCombat.getType(), staticCombat.getCombatId(), staticCombat.getCost() * cnt, 1);

        // 获取活动翻倍
        int num = activityDataManager.getActDoubleNum(player);

        DoCombatRs.Builder builder = DoCombatRs.newBuilder();

        // 玩家经验或者友谊积分奖励
        builder.addAllAward(expExchangeCredit(player, staticCombat, cnt, num));

        // 只处理普通副本随机掉落
        if (staticCombat.getType() == Constant.CombatType.type_1) {
            if (CheckNull.nonEmpty(staticCombat.getRandomAward())) {
                List<List<Integer>> randomList = new ArrayList<>(staticCombat.getRandomAward().size());
                for (List<Integer> award : staticCombat.getRandomAward()) {
                    if (CheckNull.isEmpty(award))
                        continue;
                    List<Integer> award_ = new ArrayList<>(award);
                    randomList.add(award_);
                    int oldCount = award_.get(2);
                    award_.set(2, oldCount * cnt);
                }

                List<CommonPb.Award> randomAwardList = getRandomAward(player, randomList);
                if (ListUtils.isNotBlank(randomAwardList)) {
                    rewardDataManager.sendRewardByAwardList(player, randomAwardList, AwardFrom.GAIN_COMBAT);
                    builder.addAllAward(randomAwardList);
                }
            }
        }

        //喜悦金秋-日出而作-通关战役xx次（包含扫荡）
        TaskService.processTask(player, ETask.PASS_BARRIER, cnt);

        taskDataManager.updTask(player, TaskType.COND_995, cnt);

        // 副本掉落活动
        List<List<Integer>> actCombatDropAward = activityDataManager.getActCombatDrop(player, cnt);
        if (!CheckNull.isEmpty(actCombatDropAward)) {
            builder.addAllAward(rewardDataManager.sendReward(player, actCombatDropAward, AwardFrom.GAIN_COMBAT));
        }

        //增加图腾掉落
        Stream.iterate(0, i -> i + 1).limit(cnt).forEach(j -> builder.addAllAward(totemService.dropTotem(player, 1, AwardFrom.TOTOEM_DROP_GUANQIA)));

        // 给将领加经验 平分
        int addExp = num * cnt * staticCombat.getExp();
        addExp = heroService.adaptHeroAddExp(player, addExp);
        int roleLv = player.lord.getLevel();
        if (addExp > 0) {
            int addHeroExp = 0;
            CommonPb.RptHero.Builder atkHeroPb = CommonPb.RptHero.newBuilder();
            for (int pos = 0; pos < player.getPlayerFormation().getHeroBattle().length; pos++) {
                PartnerHero partnerHero = player.getPlayerFormation().getHeroBattle()[pos];
                if (HeroUtil.isEmptyPartner(partnerHero)) continue;
                Hero hero = partnerHero.getPrincipalHero();
                if (hero != null) {
                    addHeroExp = 0;
                    if (hero.getLevel() < roleLv) {
                        addHeroExp = heroService.addHeroExp(hero, addExp, player.lord.getLevel(), player);
                    }
                    atkHeroPb.addEntity(PbHelper.createChiefBattleEntity(Constant.Role.PLAYER, 0, 0, hero.getHeroId(),
                            player.lord.getNick(), hero.getLevel(), addHeroExp, 0, hero));
                }
                if (CheckNull.nonEmpty(partnerHero.getDeputyHeroList())) {
                    for (Hero deputyHero : partnerHero.getDeputyHeroList()) {
                        if (CheckNull.isNull(deputyHero)) continue;
                        addHeroExp = 0;
                        if (deputyHero.getLevel() < roleLv) {
                            addHeroExp = heroService.addHeroExp(deputyHero, addExp, player.lord.getLevel(), player);
                        }
                        atkHeroPb.addEntity(PbHelper.createAssBattleEntity(Constant.Role.PLAYER, hero.getHeroId(),
                                player.lord.getNick(), hero.getLevel(), addHeroExp, hero));
                    }
                }
                builder.addAtkHero(atkHeroPb.build());
                atkHeroPb.clear();
            }
        }

        // 给战机加经验
//        addPlaneExp(player, staticCombat, cnt, builder,
//                Arrays.stream(player.get).boxed().collect(Collectors.toList()));

        // 勋章掉落
        for (int i = 0; i < cnt; i++) {
            List<Medal> medals = medalDataManager.getMedalBydoCombat(player);
            for (Medal medal : medals) {
                builder.addMedals(PbHelper.createMedalPb(medal));
            }
        }
        // 挑战战役活动
        activityDataManager.updActivity(player, ActivityConst.ACT_CHALLENGE_COMBAT, cnt, combatId, true);
        taskDataManager.updTask(player, TaskType.COND_COMBAT_37, cnt);
        battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_COMBAT_37, cnt);
        royalArenaService.updTaskSchedule(player.roleId, TaskType.COND_COMBAT_37, cnt);

        builder.setResult(1);
        builder.setStar(player.combats.get(combatId).getStar());
        return builder.build();
    }

    /**
     * 给战机加经验
     *
     * @param player       角色信息
     * @param staticCombat 副本配置
     * @param cnt          次数
     * @param builder
     * @param heroIds      上阵将领
     */
    private List<TwoInt> addPlaneExp(Player player, StaticCombat staticCombat, int cnt, DoCombatRs.Builder builder,
                                     List<Integer> heroIds) {
        List<TwoInt> planeExp = new ArrayList<>();
//        int addExp;// 给战机加经验
//        if (CheckNull.isNull(player) || CheckNull.isNull(staticCombat)) {
//            return planeExp;
//        }
//        int roleLv = player.lord.getLevel();
//        int maxLv = PlaneConstant.PLANE_LEVEL_MAX;
//        if (roleLv < maxLv) {
//            maxLv = roleLv;
//        }
//        addExp = cnt * staticCombat.getPlaneExp();
//        if (addExp > 0) {
//            int addPlaneExp = 0;
//            for (int pos = 0; pos < player.heroBattle.length; pos++) {
//                Hero hero = player.heros.get(player.heroBattle[pos]);
//                if (hero != null && !CheckNull.isEmpty(hero.getWarPlanes()) && heroIds.contains(hero.getHeroId())) {
//                    for (Integer planeId : hero.getWarPlanes()) {
//                        StaticPlaneUpgrade sPlaneUpgrade = StaticWarPlaneDataMgr.getPlaneUpgradeById(planeId);
//                        if (CheckNull.isNull(sPlaneUpgrade)) {
//                            continue;
//                        }
//                        WarPlane plane = player.warPlanes.get(sPlaneUpgrade.getPlaneType());
//                        if (CheckNull.isNull(plane)) {
//                            continue;
//                        }
//                        if (plane.getLevel() < maxLv) {
//                            addPlaneExp = warPlaneDataManager.addPlaneExp(plane, addExp, maxLv, player, sPlaneUpgrade);
//                        }
//                        TwoInt twoIntPb = PbHelper.createTwoIntPb(planeId, addPlaneExp);
//                        planeExp.add(twoIntPb);
//                        if (!CheckNull.isNull(builder)) {
//                            builder.addPlaneExp(twoIntPb);
//                        }
//                    }
//                }
//            }
//        }
        return planeExp;
    }

    /**
     * 高级关卡扫荡
     *
     * @param roleId
     * @param type
     * @param combatId
     * @param heroIds  指定将领的
     * @return
     * @throws MwException
     */
    public DoCombatWipeRs doCombatWipe(long roleId, int type, int combatId, List<Integer> heroIds) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        StaticCombat staticCombat = StaticCombatDataMgr.getStaticCombat(combatId);
        if (staticCombat == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(),
                    "挑战副本时，无关卡配置, roleId:" + roleId + ",combatId=" + combatId);
        }
        if (combatId != Constant.INIT_COMBAT_ID) {
            if (player.lord.combatId < staticCombat.getPreId()) {
                throw new MwException(GameError.COMBAT_PASS_BEFORE.getCode(), "挑战关卡时，前置关卡未通关, roleId:" + roleId);
            }
        }
        if (staticCombat.getType() != CombatType.type_1) {
            throw new MwException(GameError.NO_CONFIG.getCode(),
                    "非普通副本不能扫荡, roleId:" + roleId + ",combatId=" + combatId);
        }
        // 判断西点军校是否开启
        if (!buildingDataManager.checkBuildingLock(player, BuildingType.WAR_FACTORY)) {
            throw new MwException(GameError.FUNCTION_LOCK.getCode(), "高级副本扫荡,西点军校(内阁)解锁条件不满足 roleId:" + roleId);
        }
        // 将领判断
        if (heroIds.size() > 4 || heroIds.size() < 1) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "副本扫荡将领数量有误, roleId:" + roleId, " heroSize:",
                    heroIds.size()); // 将领数量有误
        }

        boolean isOpenWipe = vipDataManager.isOpen(player.lord.getVip(), VipConstant.WIPE);
        if (!player.combats.containsKey(combatId) || (!isOpenWipe && player.combats.get(combatId).getStar() < 3)) {
            throw new MwException(GameError.COMBAT_PASS_BEFORE.getCode(),
                    "扫荡卡时，未三星通关, roleId:" + roleId + ",combatId=" + combatId);
        }

        int cnt = player.lord.getPower() / staticCombat.getCost();
        if (cnt > 0) {
            cnt = cnt >= 5 ? 5 : cnt;
            rewardDataManager.checkAndSubPlayerRes(player, AwardType.MONEY, AwardType.Money.ACT,
                    staticCombat.getCost() * cnt, AwardFrom.COMBAT_FIGHT, false);
        } else {
            // 攻打副本行动力不足
            activityService.checkTriggerGiftSync(ActivityConst.TRIGGER_GIFT_DOCOMBAT_ACT, player);
            throw new MwException(GameError.ACT_NOT_ENOUGH.getCode(),
                    "扫荡副本体力不足, roleId:" + roleId + ",combatId:" + combatId, ",cnt:", cnt);
        }
        LogUtil.debug("扫荡次数=" + cnt);

        // 扫荡副本增加日志记录 (基本信息、战力、关卡类型、关卡id、关卡消耗、状态)
        LogLordHelper.commonLog("combatFight", AwardFrom.COMBAT_FIGHT, player, player.lord.getFight()
                , staticCombat.getType(), staticCombat.getCombatId(), staticCombat.getCost() * cnt, 1);

        // 阵容重新赋值
        player.combatHeroForm.clear();
        player.combatHeroForm.addAll(heroIds);

        // 获取活动翻倍
        int num = activityDataManager.getActDoubleNum(player);

        DoCombatWipeRs.Builder builder = DoCombatWipeRs.newBuilder();

        // 玩家经验或者友谊积分奖励
        builder.addAllAward(expExchangeCredit(player, staticCombat, cnt, num));
        // 只处理普通副本随机掉落
        if (staticCombat.getType() == Constant.CombatType.type_1) {
            if (CheckNull.nonEmpty(staticCombat.getRandomAward())) {
                List<List<Integer>> randomList = new ArrayList<>(staticCombat.getRandomAward().size());
                for (List<Integer> award : staticCombat.getRandomAward()) {
                    if (CheckNull.isEmpty(award))
                        continue;
                    List<Integer> award_ = new ArrayList<>(award);
                    randomList.add(award_);
                    int oldCount = award_.get(2);
                    award_.set(2, oldCount * cnt);
                }

                List<CommonPb.Award> randomAwardList = getRandomAward(player, randomList);
                if (ListUtils.isNotBlank(randomAwardList)) {
                    rewardDataManager.sendRewardByAwardList(player, randomAwardList, AwardFrom.GAIN_COMBAT);
                    builder.addAllAward(randomAwardList);
                }
            }
        }

        // 副本掉落活动
        List<List<Integer>> actCombatDropAward = activityDataManager.getActCombatDrop(player, cnt);
        if (!CheckNull.isEmpty(actCombatDropAward)) {
            builder.addAllAward(rewardDataManager.sendReward(player, actCombatDropAward, AwardFrom.GAIN_COMBAT));
        }
        //增加图腾掉落
        Stream.iterate(0, i -> i + 1).limit(cnt).forEach(j -> builder.addAllAward(totemService.dropTotem(player, 1, AwardFrom.TOTOEM_DROP_GUANQIA)));
        // 给将领加经验 平分
        int addExp = num * cnt * staticCombat.getExp();
        addExp = heroService.adaptHeroAddExp(player, addExp);
        int roleLv = player.lord.getLevel();
        if (addExp > 0) {
            for (Integer heroId : heroIds) {// 给指定将领加经验
                Hero hero = player.heros.get(heroId);
                if (hero != null) {
                    int addHeroExp = 0;
                    if (hero.getLevel() < roleLv) {
                        addHeroExp = heroService.addHeroExp(hero, addExp, player.lord.getLevel(), player);
                    }
//                    builder.addAtkHero(PbHelper.createRptHero(Constant.Role.PLAYER, 0, 0, hero.getHeroId(),
//                            player.lord.getNick(), hero.getLevel(), addHeroExp, 0, hero));
                }
            }
        }
        taskDataManager.updTask(player, TaskType.COND_COMBAT_37, cnt);
        taskDataManager.updTask(player, TaskType.COND_995, cnt);
        battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_COMBAT_37, cnt);
        royalArenaService.updTaskSchedule(player.roleId, TaskType.COND_COMBAT_37, cnt);
        // 挑战战役活动
        activityDataManager.updActivity(player, ActivityConst.ACT_CHALLENGE_COMBAT, cnt, combatId, true);

        //貂蝉任务-成功通关关卡，包含扫荡
        ActivityDiaoChanService.completeTask(player, ETask.PASS_BARRIER, cnt);
        //喜悦金秋-日出而作-通关战役xx次（包含扫荡）
        TaskService.processTask(player, ETask.PASS_BARRIER, cnt);

        // 给战机加经验
        List<TwoInt> planeExp = addPlaneExp(player, staticCombat, cnt, null, heroIds);
        if (!CheckNull.isEmpty(planeExp)) {
            builder.addAllPlaneExp(planeExp);
        }

        // 勋章掉落
        for (int i = 0; i < cnt; i++) {
            List<Medal> medals = medalDataManager.getMedalBydoCombat(player);
            for (Medal medal : medals) {
                builder.addMedals(PbHelper.createMedalPb(medal));
            }
        }

        builder.setResult(1);
        builder.setStar(player.combats.get(combatId).getStar());
        return builder.build();
    }

    /**
     * 玩家经验或友谊积分奖励
     *
     * @param player
     * @param staticCombat
     * @param cnt
     * @param num
     * @return
     */
    private List<CommonPb.Award> expExchangeCredit(Player player, StaticCombat staticCombat, int cnt, int num) {
        List<CommonPb.Award> awards = new ArrayList<>();
        // 副本活动翻倍
        int d = activityDataManager.getActCombatDoubleNum(player);
        // 每章最后一节才会有副本活动奖励
        int temp = staticCombat.getIndex() == 6 ? cnt * d * num : cnt;
        if (player.lord.getLevel() == Constant.MAX_ROLE_LV) { // 给玩家加友谊积分
            int sumExp = staticCombat.getWinAward().stream().mapToInt(award -> {
                if (award.get(0) == AwardType.MONEY && award.get(1) == AwardType.Money.EXP) return award.get(2) * temp;
                return 0;
            }).sum();
            int exchangeCnt = (int) Math.floor(sumExp / Constant.EXP_EXCHANGE_CREDIT.get(0));
            if (exchangeCnt > 0) {
                awards.add(rewardDataManager.sendRewardSignle(player, AwardType.MONEY, AwardType.Money.CREDIT,
                        exchangeCnt * Constant.EXP_EXCHANGE_CREDIT.get(1), AwardFrom.EXP_EXCHANGE_CREDIT));
            }
        } else { // 给玩家加经验
            awards.addAll(rewardDataManager.sendReward(player, staticCombat.getWinAward(), temp, AwardFrom.GAIN_COMBAT));
        }
        if (!CheckNull.isEmpty(staticCombat.getTitanDrop()) && player.lord.getLevel() >= Constant.RED_DROP_AWARD_LV) {
            awards.addAll(rewardDataManager.sendReward(player, staticCombat.getTitanDrop(), cnt, AwardFrom.GAIN_COMBAT));
        }
        return awards;
    }

    /**
     * 资源副本购买
     *
     * @param roleId
     * @param combatId
     * @return
     * @throws MwException
     */
    public BuyCombatRs buyCombat(Long roleId, int combatId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        StaticCombat staticCombat = StaticCombatDataMgr.getStaticCombat(combatId);
        if (staticCombat == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(),
                    "资源副本购买时，无关卡配置, roleId:" + roleId + ",combatId=" + combatId);
        }
        CombatFb combatFb = player.combatFb.get(staticCombat.getCombatId());
        if (combatFb == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(),
                    "资源副本购买时，无关卡配置, roleId:" + roleId + ",combatId=" + combatId);
        }

        if (combatFb.getStatus() > 0) {
            throw new MwException(GameError.COMBAT_FINISHED.getCode(), "招募副本时，已完成, roleId:" + roleId);
        }

        // 过时不允许买
        if (TimeHelper.getCurrentSecond() > combatFb.getEndTime()) {
            throw new MwException(GameError.COMBAT_PASS_BEFORE.getCode(), "招募副本时，已完成, roleId:" + roleId);
        }
        if (combatFb.getCnt() > 0) {
            throw new MwException(GameError.COMBAT_PASS_BEFORE.getCode(), "资源副本时，次数使用中, roleId:" + roleId);
        }
        if (combatFb.getBuyCnt() > staticCombat.getExtand().size() - 2) {
            throw new MwException(GameError.NO_CONFIG.getCode(),
                    "资源副本购买时，购买次数已满, roleId:" + roleId + ",combatId=" + combatId + ",cnt=" + combatFb.getBuyCnt());
        }
        int needGold = staticCombat.getExtand().get(combatFb.getBuyCnt() + 2);
        if (needGold <= 0) {
            throw new MwException(GameError.NO_CONFIG.getCode(),
                    "资源副本购买时，购买次数消费错误, roleId:" + roleId + ",combatId=" + combatId + ",needGold=" + needGold);
        }

        rewardDataManager.checkMoneyIsEnough(player, AwardType.Money.GOLD, needGold, "资源副本购买");
        rewardDataManager.subMoney(player, AwardType.Money.GOLD, needGold, AwardFrom.COMBAT_TYPE_2_BUY, combatId);

        combatFb.setCnt(Constant.COMBAT_RES_CNT);
        combatFb.setBuyCnt(combatFb.getBuyCnt() + 1);
        BuyCombatRs.Builder builder = BuyCombatRs.newBuilder();
        builder.setCombatFB(PbHelper.createCombatFBPb(combatFb));
        builder.setGold(player.lord.getGold());
        return builder.build();
    }

    /**
     * 副本挑战将领判断
     *
     * @param player
     * @param heroIds
     * @throws MwException
     */
    private void checkHeroCombat(Player player, List<Integer> heroIds) throws MwException {
        long roleId = player.roleId;
        if (heroIds.size() > 4 || heroIds.size() < 1) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "挑战关卡时 将领数量有误, roleId:" + roleId, " heroSize:",
                    heroIds.size()); // 将领数量有误
        }
        List<Integer> combatPos = new ArrayList<>();
        for (int heroId : heroIds) {
            Hero hero = player.heros.get(heroId);
            if (hero == null) {
                throw new MwException(GameError.HERO_NOT_FOUND.getCode(), "挑战关卡时,选择的将领不存在, roleId:", roleId,
                        ", heroId:", heroId);
            }
            if (!hero.isOnBattle()) {
                throw new MwException(GameError.HERO_NOT_BATTLE.getCode(), "挑战关卡时,选择的将领未上阵, roleId:", roleId,
                        ", heroId:", heroId);
            }
            if (hero.isCommando()) {
                throw new MwException(GameError.COMMANDO_HERO_NOT_ATK.getCode(), "挑战关卡时,选择的将领不能进攻, roleId:", roleId,
                        ", heroId:", heroId);
            }
            for (int i = 1; i < player.getPlayerFormation().getHeroBattle().length; i++) {
                PartnerHero partnerHero = player.getPlayerFormation().getHeroBattle()[i];
                if (HeroUtil.isEmptyPartner(partnerHero)) continue;
                if (partnerHero.getPrincipalHero().getHeroId() == heroId) {
                    combatPos.add(i);
                    break;
                }
            }
        }
        if (!player.isOnBattle()) {
            throw new MwException(GameError.HERO_NOT_BATTLE.getCode(), "挑战关卡时,未上阵, roleId:" + roleId);
        }
    }

    /*---------------------------------------宝石副本----------------------------------------------*/

    /**
     * 获取宝石副本信息
     */
    public GetStoneCombatRs getStoneCombat(long roleId, GetStoneCombatRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int option = req.getOption();// 0 获取全部数据, 1 仅获取剩余次数,不会给副本数据
        GetStoneCombatRs.Builder builder = GetStoneCombatRs.newBuilder();
        builder.setCombatCnt(player.getStoneInfo().getAttackCombatCnt());
        builder.setBuyCnt(player.getStoneInfo().getBuyCombatCnt());
        if (option == 0) {
            for (StoneCombat c : player.stoneCombats.values()) {
                builder.addCombat(PbHelper.createStoneCombatPb(c));
            }
            // 计算进度
            Collection<StoneCombat> sc = player.stoneCombats.values();
            if (!CheckNull.isEmpty(sc)) {
                Integer combatIdMax = sc.stream().max(Comparator.comparingInt(StoneCombat::getCombatId))
                        .flatMap(s -> Optional.ofNullable(s.getCombatId())).orElse(0);
                builder.setCombatId(combatIdMax);
            }
        }
        return builder.build();
    }

    /**
     * 宝石副本挑战
     *
     * @param roleId
     * @param combatId
     * @param wipe     0 挑战关卡, 1 单次扫荡, 2 多次扫荡, 扫荡不回传record
     * @param heroIds
     * @return
     * @throws MwException
     */
    public DoStoneCombatRs doStoneCombat(long roleId, int combatId, int wipe, boolean useProp, List<Integer> heroIds)
            throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        // if (player.lord.getLevel() < 40) {
        // throw new MwException(GameError.LV_NOT_ENOUGH.getCode(), "挑战宝石副本 玩家等级不足, roleId:", roleId);
        // }
        StaticStoneCombat sCombat = StaticCombatDataMgr.getStoneCombatById(combatId);
        if (sCombat == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(),
                    "挑战副本时，无关卡配置, roleId:" + roleId + ",combatId=" + combatId);
        }
        if (sCombat.getPreId() != 0) {
            StoneCombat preCombat = player.stoneCombats.get(sCombat.getPreId());
            if (preCombat == null || preCombat.getPassCnt() < 1) {
                throw new MwException(GameError.COMBAT_PASS_BEFORE.getCode(), "挑战关卡时，前置关卡未通关, roleId:", roleId,
                        " preId:", sCombat.getPreId(), ", combatId:", combatId);
            }
        }
        // 将领判断
        checkHeroCombat(player, heroIds);

        // 不是使用道具
        if (!useProp && player.getStoneInfo().getAttackCombatCnt() + sCombat.getCost() > Constant.STONE_COMBAT_CNT_CAPACITY) {// 检测次数够不够
            throw new MwException(GameError.COMBAT_RES_NO_CNT.getCode(),
                    "关卡宝石挑战次数已经用完:" + roleId + ",combatId=" + combatId);
        }

        StoneCombat sc = player.stoneCombats.get(combatId);
        if (wipe != 0) { // 扫荡副本
            if (sCombat.getCnt() != 0) {
                throw new MwException(GameError.COMBAT_PASSED.getCode(),
                        "关卡不能扫荡, roleId:" + roleId + ",combatId=" + combatId);
            }
            if (sc == null || sc.getPassCnt() < 1) {
                throw new MwException(GameError.COMBAT_PASS_BEFORE.getCode(),
                        "扫荡卡时,未通关, roleId:" + roleId + ",combatId=" + combatId);
            }

            return stoneCombatWipe(player, sCombat, wipe, useProp);
        }
        // 挑战关卡
        if (sCombat.getCnt() != 0 && sc != null && sc.getPassCnt() >= sCombat.getCnt()) {
            throw new MwException(GameError.COMBAT_RES_NO_CNT.getCode(), "超过挑战关卡次数 roleId:", roleId, ", combatId:",
                    combatId);
        }
        DoStoneCombatRs.Builder builder = DoStoneCombatRs.newBuilder();
        Fighter attacker = fightService.createCombatPlayerFighter(player, heroIds);
        Fighter defender = fightService.createNpcFighter(sCombat.getForm());
        FightLogic fightLogic = new FightLogic(attacker, defender, true);
        fightLogic.start();
        if (fightLogic.getWinState() == 1) { // 胜利
            // 更新战火试炼进度
            activityDataManager.updActivity(player, ActivityConst.ACT_WAR_ROAD, 1, 0, true);
            activityDataManager.updActivity(player, ActivityConst.ACT_WAR_ROAD_DAILY, 1, 0, true);
            activityDataManager.updDay7ActSchedule(player, ActivityConst.ACT_TASK_STONE_COMBAT_CNT, combatId);
            if (useProp) {
                // 检测消耗道具
                rewardDataManager.checkAndSubPlayerRes(player, AwardType.PROP, Constant.STONE_FREE_PROP_ID, 1, AwardFrom.STONE_COMBAT_CHALLENGE, true);
            } else {
                // 攻打次数增加
                player.getStoneInfo().addAttackCombatCnt(sCombat.getCost());
            }
            if (sc == null) {
                sc = new StoneCombat(combatId, 1);
                player.stoneCombats.put(combatId, sc);
            } else {
                sc.addPassCnt(1);
            }
            builder.addAllAward(rewardDataManager.addAwardDelaySync(player, addStoneAward(sCombat, 1), null,
                    AwardFrom.STONE_COMBAT_CHALLENGE));
            LogLordHelper.stoneCombat(AwardFrom.STONE_COMBAT_CHALLENGE, player.account, player.lord, combatId, 1);
            taskDataManager.updTask(player, TaskType.COND_STONE_COMBAT_47, 1, combatId);
            battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_STONE_COMBAT_47, 1);
            royalArenaService.updTaskSchedule(player.roleId, TaskType.COND_STONE_COMBAT_47, 1);
            // 触发检测功能解锁
            buildingDataManager.refreshSourceData(player);
            // 首次通关帝国远征关卡
            activityTriggerService.doExpeditionCombatTriggerGift(player, combatId);

            //貂蝉任务-成功通关帝国远征，包含扫荡
            ActivityDiaoChanService.completeTask(player, ETask.PASS_EXPEDITION, 1);
            //喜悦金秋-日出而作-通关帝国远征xx次（包含扫荡）
            TaskService.processTask(player, ETask.PASS_EXPEDITION, 1);

        } else {
            // 更新触发式礼包进度（帝国远征失败2次后触发限时特殊礼包）
            activityDataManager.updateTriggerStatus(ActivityConst.TRIGGER_GIFT_EXPEDITION_FAIL, player, 1);
            activityService.checkTriggerGiftSync(ActivityConst.TRIGGER_GIFT_EXPEDITION_FAIL, player);
        }
        // 宝石副本增加日志记录 (基本信息、战力、关卡类型、关卡id、关卡消耗、状态)
        LogLordHelper.commonLog("combatFight", AwardFrom.STONE_COMBAT_CHALLENGE, player, player.lord.getFight()
                , sCombat.getType(), sCombat.getCombatId(), sCombat.getCost(), fightLogic.getWinState());
        builder.setCombatCnt(player.getStoneInfo().getAttackCombatCnt());
        builder.setResult(fightLogic.getWinState());
        builder.setRecord(fightLogic.generateRecord());
        builder.addAllAtkHero(fightSettleLogic.stoneCombatCreateRptHero(player, attacker.forces));
        // 防守方hero信息
        builder.addAllDefHero(defender.forces.stream().map(force -> PbHelper.createRptHero(Constant.Role.BANDIT, force.killed, 0, force, null, 0, 0, force.totalLost)).collect(Collectors.toList()));
        return builder.build();
    }

    /**
     * 宝石副本扫荡
     *
     * @param player
     * @param sCombat
     * @param wipe
     * @param useProp
     * @return
     */
    private DoStoneCombatRs stoneCombatWipe(Player player, StaticStoneCombat sCombat, int wipe, boolean useProp) throws MwException {
        int combatId = sCombat.getCombatId();
        int wipeCnt = 1; // 次数
        if (wipe == 2) {
            if (useProp) {
                int hasCnt = (int) rewardDataManager.getRoleResByType(player, AwardType.PROP, Constant.STONE_FREE_PROP_ID);
                wipeCnt = Math.min(hasCnt, 5);
            } else {
                int hasCnt = Constant.STONE_COMBAT_CNT_CAPACITY - player.getStoneInfo().getAttackCombatCnt(); // 拥有点数
                wipeCnt = Math.min(hasCnt / sCombat.getCost(), 5);
            }
        }
        if (useProp) {
            // 检测消耗道具
            rewardDataManager.checkAndSubPlayerRes(player, AwardType.PROP, Constant.STONE_FREE_PROP_ID, wipeCnt, AwardFrom.STONE_COMBAT_CHALLENGE, true);
        } else {
            // 攻打次数增加
            player.getStoneInfo().addAttackCombatCnt(sCombat.getCost() * wipeCnt);
        }
        // 更新战火试炼进度
        activityDataManager.updActivity(player, ActivityConst.ACT_WAR_ROAD, wipeCnt, 0, true);
        activityDataManager.updActivity(player, ActivityConst.ACT_WAR_ROAD_DAILY, wipeCnt, 0, true);

        // 宝石副本增加日志记录 (基本信息、战力、关卡类型、关卡id、关卡消耗、状态)
        LogLordHelper.commonLog("combatFight", AwardFrom.STONE_COMBAT_WIPE, player, player.lord.getFight()
                , sCombat.getType(), sCombat.getCombatId(), sCombat.getCost() * wipeCnt, 1);

        DoStoneCombatRs.Builder builder = DoStoneCombatRs.newBuilder();
        builder.addAllAward(rewardDataManager.addAwardDelaySync(player, addStoneAward(sCombat, wipeCnt), null,
                AwardFrom.STONE_COMBAT_WIPE));
        LogLordHelper.stoneCombat(AwardFrom.STONE_COMBAT_WIPE, player.account, player.lord, combatId, wipeCnt);
        builder.setCombatCnt(player.getStoneInfo().getAttackCombatCnt());

        taskDataManager.updTask(player, TaskType.COND_STONE_COMBAT_47, wipeCnt);
        battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_STONE_COMBAT_47, wipeCnt);
        royalArenaService.updTaskSchedule(player.roleId, TaskType.COND_STONE_COMBAT_47, wipeCnt);

        //貂蝉任务-成功通关帝国远征，包含扫荡
        ActivityDiaoChanService.completeTask(player, ETask.PASS_EXPEDITION, wipeCnt);
        //喜悦金秋-日出而作-通关帝国远征xx次（包含扫荡）
        TaskService.processTask(player, ETask.PASS_EXPEDITION, wipeCnt);

        return builder.build();
    }

    /**
     * 宝石副本将领计算
     *
     * @param sCombat
     * @param cnt     次数
     * @return
     */
    private List<List<Integer>> addStoneAward(StaticStoneCombat sCombat, int cnt) {
        List<List<Integer>> list = new ArrayList<>();
        while (cnt-- > 0) {
            // 通关奖励必掉1种[[type,道具id，个数下限，个数上限，权重],········]
            List<List<Integer>> winAward1 = sCombat.getWinAward1();
            List<Integer> award1 = RandomUtil.getRandomByWeight(winAward1, 4, false);
            int award1Cnt = RandomHelper.randomInArea(award1.get(2), award1.get(3) + 1);
            List<Integer> a1 = new ArrayList<>();
            a1.add(award1.get(0));
            a1.add(award1.get(1));
            a1.add(award1Cnt);
            list.add(a1);

            // 通关奖励全随机[[type,道具id，个数下限，个数上限，独立概率万分比],········]
            List<List<Integer>> winAward2 = sCombat.getWinAward2();
            for (List<Integer> award2 : winAward2) {
                if (RandomHelper.isHitRangeIn10000(award2.get(4))) {
                    int award2Cnt = RandomHelper.randomInArea(award2.get(2), award2.get(3) + 1);
                    List<Integer> a2 = new ArrayList<>();
                    a2.add(award2.get(0));
                    a2.add(award2.get(1));
                    a2.add(award2Cnt);
                    list.add(a2);
                }
            }
        }
        return cnt > 1 ? list : RewardDataManager.mergeAward(list);
    }

    /**
     * 宝石副本次数购买
     *
     * @throws MwException
     */
    public BuyStoneCombatRs buyStoneCombat(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int attackCombatCnt = player.getStoneInfo().getAttackCombatCnt();
        if (attackCombatCnt < Constant.STONE_COMBAT_CNT_CAPACITY) {
            throw new MwException(GameError.STONE_COMBAT_ATTCK_CNT_ERR.getCode(), "宝石副本次数未用完: roleId", roleId,
                    ", attackCombatCnt:" + attackCombatCnt);
        }
        int vip = player.lord.getVip();
        StaticVip sVip = StaticVipDataMgr.getVipMap(vip);
        if (sVip == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "vip配置为找到 vip:", vip);
        }
        int buyStone = sVip.getBuyStone();
        int myBuyCombatCnt = player.getStoneInfo().getBuyCombatCnt();
        if (myBuyCombatCnt + 1 > buyStone) {
            throw new MwException(GameError.STONE_COMBAT_BUY_CNT_ERR.getCode(), "购买次数已经用完 roleId", roleId);
        }
        int needGold = myBuyCombatCnt > Constant.STONE_COMBAT_BUY_PRICE.size() - 1
                ? Constant.STONE_COMBAT_BUY_PRICE.get(Constant.STONE_COMBAT_BUY_PRICE.size() - 1)
                : Constant.STONE_COMBAT_BUY_PRICE.get(myBuyCombatCnt);
        rewardDataManager.checkAndSubPlayerRes(player, AwardType.MONEY, AwardType.Money.GOLD, needGold,
                AwardFrom.STONE_COMBAT_BUY_CNT, false);
        battlePassDataManager.updTaskSchedule(player.roleId, TaskType.COND_STONE_COMBAT_BUY_CNT, 1);
        player.getStoneInfo().setAttackCombatCnt(0);
        player.getStoneInfo().addBuyCombatCnt(1);
        BuyStoneCombatRs.Builder builder = BuyStoneCombatRs.newBuilder();
        builder.setBuyCnt(player.getStoneInfo().getBuyCombatCnt());
        builder.setCombatCnt(player.getStoneInfo().getAttackCombatCnt());
        builder.setGold(player.lord.getGold());
        return builder.build();
    }

    /**
     * 获取荣耀演习场副本
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GetPitchCombatRs getPitchCombat(long roleId, GetPitchCombatRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int type = req.getType();
        List<StaticPitchCombat> pitchCombatCfgList = StaticCombatDataMgr.getPitchCombatGroupByType(type);
        if (CheckNull.isEmpty(pitchCombatCfgList)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "未找到副本配置 type:", type, ", roleId:", roleId);
        }
        PitchCombat pitchCombat = player.getOrCreatePitchCombat(type);
        pitchCombat.refresh();
        GetPitchCombatRs.Builder builder = GetPitchCombatRs.newBuilder();
        builder.setPc(pitchCombat.ser());
        return builder.build();
    }

    /**
     * 挑战荣耀演习场
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public DoPitchCombatRs doPitchCombat(long roleId, DoPitchCombatRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        //解锁判断
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, BuildingType.PITCHCOMBAT_LOCK_ID)) {
            throw new MwException(GameError.FUNCTION_UNLOCK_NO_CONFIG.getCode(), "roleId:", roleId, "荣耀演习场未解锁");
        }
        int type = req.getType();
        int combatId = req.getCombatId();
        int operate = req.hasOperate() ? req.getOperate() : 0; // 0是挑战, 1是扫荡
        List<Integer> heroIds = req.getHeroIdList();

        List<StaticPitchCombat> pitchCombatCfgList = StaticCombatDataMgr.getPitchCombatGroupByType(type);
        if (CheckNull.isEmpty(pitchCombatCfgList)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "挑战副本时未找到副本配置 type:", type, ", roleId:", roleId);
        }
        StaticPitchCombat sPitchCombat = pitchCombatCfgList.stream().filter(spc -> spc.getCombatId() == combatId)
                .findFirst().orElse(null);
        if (sPitchCombat == null) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "挑战副本时未找到副本配置 type:", type, ", combatId:", combatId,
                    ", roleId:", roleId);
        }

        // 将领判断
        checkHeroCombat(player, heroIds);
        PitchCombat pitchCombat = player.getOrCreatePitchCombat(type);
        pitchCombat.refresh();

        DoPitchCombatRs.Builder builder = DoPitchCombatRs.newBuilder();
        List<List<Integer>> showAward = new ArrayList<>();
        int passCnt = 0; // 通关个数
        if (operate == 0) {// 挑战
            int todayCombatId = pitchCombat.getTodayCombatId();
            StaticPitchCombat expectSpc = findNextPitchCombat(todayCombatId, pitchCombatCfgList);
            if (expectSpc == null) {
                throw new MwException(GameError.COMBAT_FINISHED.getCode(), "副本已经通关 type:", type, ", combatId:",
                        combatId, ", todayCombatId:", todayCombatId, ", roleId:", roleId);
            }
            if (expectSpc.getCombatId() != combatId) {
                throw new MwException(GameError.COMBAT_PASS_BEFORE.getCode(), "前置关卡未通关 type:", type, ", combatId:",
                        combatId, ", todayCombatId:", todayCombatId, ", roleId:", roleId);
            }
            // 背包的限制
            rewardDataManager.checkMentorEquipBag(player, sPitchCombat.getGearCount());
            // 战斗逻辑
            Fighter attacker = fightService.createCombatPlayerFighter(player, heroIds);
            Fighter defender = fightService.createNpcFighter(sPitchCombat.getForm());
            FightLogic fightLogic = new FightLogic(attacker, defender, true);
            fightLogic.start();
            if (fightLogic.getWinState() == 1) { // 胜利
                boolean isFirst = combatId > pitchCombat.getHighestCombatId(); // 首次奖励
                pitchCombat.updateCombatId(combatId); // 进度更新
                // 给予奖励
                pitchCombatAward(player, pitchCombat, sPitchCombat, isFirst, showAward);
                // 埋点
                if (isFirst) {
                    player.lord.setPitchType1CombatId(pitchCombat.getHighestCombatId());
                    rankDataManager.setPitchType1CombatId(player.lord);
                    LogLordHelper.commonLog("firstPitchCombat", AwardFrom.DO_PITCHCOMBAT_AWARD, player,
                            sPitchCombat.getType(), sPitchCombat.getCombatId());
                }
                passCnt++;
            } else {// 失败啥都不干
            }
            builder.setResult(fightLogic.getWinState());
            builder.setRecord(fightLogic.generateRecord());
            builder.addAllAtkHero(fightSettleLogic.stoneCombatCreateRptHero(player, attacker.forces));
        } else if (operate == 1) {// 扫荡
            int highestCombatId = pitchCombat.getHighestCombatId();
            int todayCombatId = pitchCombat.getTodayCombatId();
            if (pitchCombat.getHighestCombatId() == 0) { // 没有可扫荡的关卡
                throw new MwException(GameError.PITCH_COMBAT_NOT_WIPE.getCode(), "关卡不能扫荡 type:", type, ", combatId:",
                        combatId, ", highestCombatId:", highestCombatId, ", roleId:", roleId);
            }
            if (pitchCombat.getWipeCnt() >= 1) {
                throw new MwException(GameError.PITCH_COMBAT_WIPE_CNT_NOT_ENOUGH.getCode(), "关卡扫荡点数不足 type:", type,
                        ", combatId:", combatId, ", highestCombatId:", highestCombatId, ", roleId:", roleId);
            }
            int equipCnt = 0;
            for (StaticPitchCombat spc : pitchCombatCfgList) {
                if (spc.getCombatId() <= highestCombatId && spc.getCombatId() > todayCombatId) {
                    equipCnt += spc.getGearCount();
                }
            }
            // 背包的限制
            rewardDataManager.checkMentorEquipBag(player, equipCnt);
            // 给予奖励
            for (StaticPitchCombat spc : pitchCombatCfgList) {
                if (spc.getCombatId() <= highestCombatId && spc.getCombatId() > todayCombatId) {
                    pitchCombatAward(player, pitchCombat, spc, false, showAward);
                    passCnt++;
                }
            }
            pitchCombat.updateCombatId(pitchCombat.getHighestCombatId()); // 进度更新
            pitchCombat.setWipeCnt(pitchCombat.getWipeCnt() + 1);
            LogLordHelper.commonLog("wipePitchCombat", AwardFrom.DO_PITCHCOMBAT_AWARD, player, highestCombatId);
        } else {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "挑战副本时操作参数错误 operate:", operate, ", roleId:",
                    roleId);
        }
        if (passCnt > 0) {// 进度更新
            taskDataManager.updTask(player, TaskType.COND_PITCHCOMBAT_PASS_CNT, passCnt, type);
        }
        builder.addAllAward(PbHelper.createAwardsPb(RewardDataManager.mergeAward(showAward)));
        builder.setPc(pitchCombat.ser());
        return builder.build();
    }

    /**
     * 副本奖励
     *
     * @param player
     * @param pitchCombat
     * @param spc
     * @param isFirst
     * @param showAward
     */
    private void pitchCombatAward(Player player, PitchCombat pitchCombat, StaticPitchCombat spc, boolean isFirst,
                                  List<List<Integer>> showAward) {
        // 副本点数奖励
        int pointAward = isFirst ? spc.getFirstAwardpoint() : spc.getWinAwardpoint();
        if (pointAward > 0) {
            pitchCombat.addCombatPoint(pointAward);
            if (showAward != null) {
                List<Integer> e = new ArrayList<>(3);
                e.add(AwardType.PITCHCOMBAT_POINT);
                e.add(pitchCombat.getType());
                e.add(pointAward);
                showAward.add(e);
            }
        }
        // 装备的奖励
        List<List<Integer>> awardRand = isFirst ? spc.getFirstAwardRand() : spc.getWinAwardRand();
        for (List<Integer> it : awardRand) {
            int gearOrder = it.get(0);
            int cnt = it.get(1);
            List<StaticMentorEquip> sMentorEquipByGearOrder = StaticMentorDataMgr.getsMentorEquipTypeMap(gearOrder);
            if (CheckNull.isEmpty(sMentorEquipByGearOrder) || cnt < 1) continue;
            for (int i = 0; i < cnt; i++) {
                StaticMentorEquip sMentorEquip = RandomUtil.getWeightByList(sMentorEquipByGearOrder,
                        mEuipe -> mEuipe.getGearWeight());
                rewardDataManager.addMentorEquip(player, sMentorEquip.getId(), 1, AwardFrom.DO_PITCHCOMBAT_AWARD,
                        spc.getCombatId());
                if (showAward != null) {
                    List<Integer> e = new ArrayList<>(3);
                    e.add(AwardType.MENTOR_EQUIP);
                    e.add(sMentorEquip.getId());
                    e.add(1);
                    showAward.add(e);
                }
            }
        }
        // 其他奖励
        List<List<Integer>> otherAward = isFirst ? spc.getFirstAward() : spc.getWinAward();
        if (!CheckNull.isEmpty(otherAward)) {
            rewardDataManager.addAwardDelaySync(player, otherAward, null, AwardFrom.DO_PITCHCOMBAT_AWARD,
                    spc.getCombatId());
            if (showAward != null) {
                for (List<Integer> aw : otherAward) {
                    List<Integer> e = new ArrayList<>(aw);
                    showAward.add(e);
                }
            }
        }

    }

    /**
     * 找到下一个副本的配置
     *
     * @param curCombatId
     * @param pitchCombatCfgList
     * @return 返回 null说明没有下一个副本 到顶了或没找到
     */
    private static StaticPitchCombat findNextPitchCombat(int curCombatId, List<StaticPitchCombat> pitchCombatCfgList) {
        if (curCombatId <= 0) {
            return pitchCombatCfgList.get(0);
        }
        int index = 0;
        int size = pitchCombatCfgList.size();
        for (int i = 0; i < size; i++) {
            StaticPitchCombat spc = pitchCombatCfgList.get(i);
            if (spc.getCombatId() == curCombatId) {
                index = i;
                break;
            }
        }
        if (index < size - 1) {
            return pitchCombatCfgList.get(index + 1);
        }
        return null;
    }

}
