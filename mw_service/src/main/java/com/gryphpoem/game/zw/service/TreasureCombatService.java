package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticTreasureWareDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticVipDataMgr;
import com.gryphpoem.game.zw.logic.FightSettleLogic;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.manager.TaskDataManager;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticTreasureCombat;
import com.gryphpoem.game.zw.resource.domain.s.StaticVip;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
import com.gryphpoem.game.zw.resource.pojo.fight.FightLogic;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.pojo.treasureware.TreasureChallengePlayer;
import com.gryphpoem.game.zw.resource.pojo.treasureware.TreasureCombat;
import com.gryphpoem.game.zw.resource.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.gryphpoem.game.zw.resource.constant.TreasureChallengePlayerConstant.TREASURE_SPECIAL_PROPS_ID;
import static com.gryphpoem.game.zw.resource.constant.TreasureChallengePlayerConstant.TREASURE_SPECIAL_PROPS_OUTPUT_NUM;

/**
 * @description:
 * @author: zhou jie
 * @time: 2021/11/16 14:06
 */
@Service
public class TreasureCombatService implements GmCmdService {

    @Autowired
    private PlayerDataManager playerDataManager;

    @Autowired
    private FightService fightService;

    @Autowired
    private RewardDataManager rewardDataManager;

    @Autowired
    private FightSettleLogic fightSettleLogic;

    @Autowired
    private TaskDataManager taskDataManager;

    @Autowired
    private TreasureWareService treasureWareService;

    @Autowired
    private TreasureChallengePlayerService treasureChallengePlayerService;

    /**
     * ??????????????????
     *
     * @param roleId ??????
     * @return ????????????
     */
    public GamePb4.GetTreasureCombatRs getTreasureCombat(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        GamePb4.GetTreasureCombatRs.Builder builder = GamePb4.GetTreasureCombatRs.newBuilder();
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_TREASURE_WARE)) {
            return builder.build();
        }

        // ??????????????????
        TreasureCombat treasureCombat = player.getTreasureCombat();
        if (Objects.nonNull(treasureCombat)) {
            builder.setCombat(treasureCombat.ser(true));

            if (treasureCombat.getCurCombatId() > 0) {
                // ??????????????????
                builder.setChallengePlayer(treasureChallengePlayerService.getChallengeData(player));
            }
        }
        return builder.build();
    }

    /**
     * ????????????
     *
     * @param roleId ??????id
     * @param req    ????????????
     * @return ????????????
     * @throws MwException ??????
     */
    public GamePb4.DoTreasureCombatRs doTreasureCombat(long roleId, GamePb4.DoTreasureCombatRq req) throws MwException {
        int combatId = req.getCombatId();
        int wipe = req.getWipe();
        List<Integer> heroIds = req.getHeroIdList();

        Player player = playerDataManager.checkPlayerIsExist(roleId);
        treasureWareService.checkOpenTreasureWare(player);

        StaticTreasureCombat sCombat = StaticTreasureWareDataMgr.getTreasureCombatMap(combatId);
        if (Objects.isNull(sCombat)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), String.format("?????????????????????????????????, roleId: %s, combatId: %s", roleId, combatId));
        }

        // ??????????????????
        TreasureCombat treasureCombat = player.getTreasureCombat();

        // ????????????
        int preId = sCombat.getPreId();
        Map<Integer, Integer> combatInfo = treasureCombat.getCombatInfo();
        if (preId != 0) {
            if (!combatInfo.containsKey(preId) || combatInfo.getOrDefault(preId, 0) < 1) {
                throw new MwException(GameError.COMBAT_PASS_BEFORE.getCode(), String.format("???????????????????????????????????????, roleId: %s, preId: %s, combatId: %s", roleId, preId, combatId));
            }
        }

        GamePb4.DoTreasureCombatRs.Builder builder = GamePb4.DoTreasureCombatRs.newBuilder();

        int pass = combatInfo.getOrDefault(combatId, 0);
        if (wipe == 0) {
            if (pass != 0) {
                throw new MwException(GameError.COMBAT_PASSED.getCode(), String.format("???????????????????????????????????????, roleId: %s, combatId: %s", roleId, combatId));
            }

            // ?????????????????????????????????
            if (player.lord.getLevel() < sCombat.getNeedLv()) {
                throw new MwException(GameError.TREASURE_COMBAT_LEVELS_NOT_UNLOCK.getCode(), String.format("????????????????????????, ??????????????????, roleId: %s, combatId: %s, needLv: %s, lv: %s", roleId, combatId, sCombat.getNeedLv(), player.lord.getLevel()));
            }

            // ???????????????????????????
            checkHeroCombat(player, heroIds);
            Fighter attacker = fightService.createCombatPlayerFighter(player, heroIds);
            Fighter defender = fightService.createNpcFighter(sCombat.getForm());

            FightLogic fightLogic = new FightLogic(attacker, defender, true);
            fightLogic.fight();
            int winState = fightLogic.getWinState();
            if (winState == 1) {
                combatInfo.put(combatId, 1);
                treasureCombat.promoteCombat(combatId);
                builder.addAllAward(rewardDataManager.addAwardDelaySync(player, sCombat.getFirstAward(), null, AwardFrom.TREASURE_COMBAT_PROMOTE_AWARD));
                if (CheckNull.nonEmpty(sCombat.getSectionAward())) {
                    // ?????????????????????
                    treasureCombat.getSectionStatus().put(sCombat.getCombatId(), 1);
                }

                DataResource.getBean(ActivityTriggerService.class).doTreasureCombat(player, combatId);
                taskDataManager.updTask(player, TaskType.COND_525, 1);
                taskDataManager.updTask(player, TaskType.COND_531, 1, combatId);

                // ????????????????????????
                TaskService.processTask(player, ETask.PASS_TREASURE_WARE_COMBAT_ID, combatId);
            } else {
                // do nothing
            }

            // ????????????
            LogLordHelper.treasureCombatPromote("treasureCombatPromote", AwardFrom.TREASURE_COMBAT_PROMOTE_AWARD, player, combatId, winState, player.lord.getFight(), ListUtils.toString(heroIds));
            builder.setResult(winState);
            builder.setRecord(fightLogic.generateRecord());
            builder.setChallengePlayer(treasureChallengePlayerService.getChallengeData(player));
            builder.addAllAtkHero(fightSettleLogic.stoneCombatCreateRptHero(player, attacker.forces));
            builder.addAllDefHero(defender.forces.stream().map(force -> PbHelper.createRptHero(Constant.Role.BANDIT, force.killed, 0, force.id, null, 0, 0, force.totalLost)).collect(Collectors.toList()));
        } else if (wipe == 2) {
            checkOpenNextSection(player, treasureCombat);
            Integer nextSectionId = StaticTreasureWareDataMgr.getNextSectionId(treasureCombat.getSectionId());
            if (CheckNull.isNull(nextSectionId)) {
                throw new MwException(GameError.NO_CONFIG.getCode(), String.format("????????????????????????????????????, roleId: %s, combatId: %s, sectionId: %s", roleId, combatId, treasureCombat.getSectionId()));
            }

            StaticTreasureCombat staticTreasureCombat = StaticTreasureWareDataMgr.getTreasureCombatMap().values().stream()
                    .filter(s -> s.getPreId() == treasureCombat.getCurCombatId())
                    .findFirst()
                    .orElse(null);
            if (CheckNull.isNull(staticTreasureCombat)) {
                throw new MwException(GameError.NO_CONFIG.getCode(), String.format("????????????????????????????????????, roleId: %s, combatId: %s, sectionId: %s", roleId, combatId, treasureCombat.getSectionId()));
            }

            treasureCombat.setSectionId(nextSectionId);
            builder.setSectionId(nextSectionId);
        }

        builder.setCombat(treasureCombat.ser(true));
        return builder.build();
    }

    /**
     * ??????????????????????????????
     */
    private void checkOpenNextSection(Player player, TreasureCombat treasureCombat) {
        int curCombatId = treasureCombat.getCurCombatId();
        int sectionId = treasureCombat.getSectionId();
        Integer maxCombatId = StaticTreasureWareDataMgr.getTreasureCombatMaxCombatMap().getOrDefault(sectionId, 0);
        if (curCombatId != maxCombatId) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("???????????? - ??????????????????????????????????????????, roleId: %s, combatId: %s, sectionId: %s", player.getLordId(), curCombatId, sectionId));
        }
    }

    /**
     * ??????????????????
     *
     * @param sCombat ????????????
     * @param cnt     ??????
     * @return ??????
     */
    public List<List<Integer>> addNaturalAward(Player player, StaticTreasureCombat sCombat, int cnt) {
        List<List<Integer>> list = new ArrayList<>();
        List<List<Integer>> minuteAward = sCombat.getMinuteAward();
        for (List<Integer> award : minuteAward) {
            list.add(Arrays.asList(award.get(0), award.get(1), award.get(2) * cnt));
        }

        while (cnt-- > 0) {
            List<List<Integer>> minuteRandomAward = handRandomPropNumLimit(player, sCombat);
            for (List<Integer> awardList : minuteRandomAward) {
                if (awardList.size() >= 4 && RandomHelper.isHitRangeIn10000(awardList.get(3))) {
                    list.add(Arrays.asList(awardList.get(0), awardList.get(1), awardList.get(2)));

                    // ?????????????????????????????????
                    if (awardList.get(0) == AwardType.PROP && TREASURE_SPECIAL_PROPS_ID.contains(awardList.get(1))) {
                        player.getTreasureCombat().getRandomPropNumMap().merge(awardList.get(1), awardList.get(2), Integer::sum);
                    }
                }
            }
        }

        return RewardDataManager.mergeAward(list);
    }

    /**
     * ??????????????????????????????, 1607???????????????2???, 1608???????????????1???
     */
    private List<List<Integer>> handRandomPropNumLimit(Player player, StaticTreasureCombat sCombat) {
        // ???1607???1608??????????????????????????????
        if (Objects.isNull(player.getTreasureCombat())) {
            return Collections.emptyList();
        }
        Map<Integer, Integer> randomPropNumMap = player.getTreasureCombat().getRandomPropNumMap();

        List<Integer> removePropIdList = TREASURE_SPECIAL_PROPS_OUTPUT_NUM.stream()
                .filter(l -> l.size() >= 2 && randomPropNumMap.getOrDefault(l.get(0), 0) >= l.get(1))
                .map(l -> l.get(0))
                .collect(Collectors.toList());

        if (removePropIdList.isEmpty()) {
            return sCombat.getMinuteRandomAward();
        }

        return sCombat.getMinuteRandomAward().stream()
                .filter(l -> !removePropIdList.contains(l.get(1)))
                .collect(Collectors.toList());
    }


    /**
     * ????????????????????????
     *
     * @param player  ??????
     * @param heroIds ??????id
     * @throws MwException ??????
     */
    private void checkHeroCombat(Player player, List<Integer> heroIds) throws MwException {
        // ??????????????????
        List<Integer> battleHeroId = Arrays.stream(player.heroBattle)
                .filter(i -> i > 0)
                .boxed()
                .collect(Collectors.toList());

        List<Integer> heroList = heroIds.stream()
                .filter(id -> id > 0)
                .collect(Collectors.toList());

        if (!battleHeroId.containsAll(heroList)) {
            throw new MwException(GameError.PARAM_ERROR, "???????????????????????? - ??????????????????; roleId = " + player.getLordId() + "heroIdList = " + heroIds);
        }

//        TreasureChallengePlayer treasureChallengePlayer = treasureChallengePlayerService.getAndRefreshChallengePlayerData(player);
//        treasureChallengePlayer.setBattleHeroList(heroList);
    }

    /**
     * ?????????????????????
     *
     * @param player ??????
     * @return ??????????????????
     */
    private int roleOnHookMaxInterval(Player player) {
        StaticVip sVip = StaticVipDataMgr.getVipMap(player.lord.getVip());
        return Objects.isNull(sVip) ? Constant.TREASURE_ON_HOOK_AGGREGATE : Constant.TREASURE_ON_HOOK_AGGREGATE + sVip.getCumulativeTime();
    }

    /**
     * ????????????????????????
     *
     * @param roleId ??????id
     * @return ??????
     * @throws MwException ??????
     */
    public GamePb4.TreasureOnHookAwardRs treasureOnHookAward(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        treasureWareService.checkOpenTreasureWare(player);

        TreasureCombat treasureCombat = player.getTreasureCombat();
        TreasureCombat.TreasureOnHook onHook = treasureCombat.getOnHook();
        int startTime = onHook.getStartTime();
        int curCombatId = treasureCombat.getCurCombatId();
        if (startTime == 0 || curCombatId == 0) {
            throw new MwException(GameError.COMBAT_PASS_BEFORE.getCode(), String.format("???????????????????????????????????????, roleId: %s", roleId));
        }

        StaticTreasureCombat sCombat = StaticTreasureWareDataMgr.getTreasureCombatMap(curCombatId);
        if (Objects.isNull(sCombat)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), String.format("?????????????????????, ???????????????, roleId: %s, combatId: %s", roleId, curCombatId));
        }

        int now = TimeHelper.getCurrentSecond();
        int maxInterval = roleOnHookMaxInterval(player);
        int interval = now - startTime;
        // ?????????????????????????????????
        interval = interval > maxInterval ? maxInterval : interval;

        // ???????????? / 15 ??? = ??????????????????
        int count = interval / Constant.TREASURE_WARE_RES_OUTPUT_TIME_UNIT;
        GamePb4.TreasureOnHookAwardRs.Builder builder = GamePb4.TreasureOnHookAwardRs.newBuilder();
        List<List<Integer>> randomAwardList = null;
        if (count > 0) {
            randomAwardList = addNaturalAward(player, sCombat, count);
            builder.addAllAward(rewardDataManager.addAwardDelaySync(player, randomAwardList, null, AwardFrom.TREASURE_ON_HOOK_AWARD, interval, treasureCombat.getCurCombatId()));
            onHook.setStartTime(TimeHelper.getCurrentSecond());
        }

        if (CheckNull.nonEmpty(randomAwardList))
            taskDataManager.updTask(player, TaskType.COND_GET_TREASURE_WARE_HOOK_AWARD, 1);
        return builder.setHook(onHook.ser()).build();
    }

    /**
     * ??????????????????
     *
     * @param roleId   ??????id
     * @param combatId ??????id
     * @return ??????
     * @throws MwException ??????
     */
    public GamePb4.TreasureSectionAwardRs treasureSectionAward(long roleId, int combatId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        treasureWareService.checkOpenTreasureWare(player);

        TreasureCombat treasureCombat = player.getTreasureCombat();
        Map<Integer, Integer> sectionStatus = treasureCombat.getSectionStatus();
        if (sectionStatus.get(combatId) == 2) {
            throw new MwException(GameError.TREASURE_COMBAT_SECTION_AWARD_ERROR.getCode(), String.format("????????????????????????????????????????????????, roleId: %s, sectionId: %s", roleId, combatId));
        }

        StaticTreasureCombat sConf = StaticTreasureWareDataMgr.getTreasureCombatMap(combatId);
        if (Objects.isNull(sConf) || ObjectUtils.isEmpty(sConf.getSectionAward())) {
            throw new MwException(GameError.NO_CONFIG.getCode(), String.format("?????????????????????, ???????????????, roleId: %s, combatId: %s", roleId, combatId));
        }
        if (treasureCombat.getCombatInfo().getOrDefault(combatId, 0) != 1) {
            throw new MwException(GameError.COMBAT_PASS_BEFORE.getCode(), String.format("?????????????????????????????????????????????, roleId: %s, combatId: %s", roleId, combatId));
        }

        // ????????????
        List<List<Integer>> sectionAward = sConf.getSectionAward();
        sectionStatus.put(combatId, 2);
        GamePb4.TreasureSectionAwardRs.Builder builder = GamePb4.TreasureSectionAwardRs.newBuilder();
        builder.setCombatId(combatId);
        builder.setStatus(2);
        builder.addAllAward(rewardDataManager.addAwardDelaySync(player, sectionAward, null, AwardFrom.TREASURE_ON_HOOK_AWARD));
        return builder.build();
    }

    /**
     * ????????????
     */
    public void acrossTheDayProcess(Player p) {
        if (Objects.isNull(p)) {
            return;
        }
        TreasureCombat treasureCombat = p.getTreasureCombat();
        if (Objects.nonNull(treasureCombat)) {
            // ????????????????????????
            treasureCombat.getRandomPropNumMap().clear();
        }
    }

    @GmCmd("treasureWareCombat")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {
        switch (params[0]) {
            case "addCombatAward":
                int combatId = Integer.parseInt(params[1]);
                int times = Integer.parseInt(params[2]);
                StaticTreasureCombat sCombat = StaticTreasureWareDataMgr.getTreasureCombatMap(combatId);
                if (sCombat == null) {
                    LogUtil.error("?????????????????????combatId: " + combatId);
                    return;
                }

                List<List<Integer>> lists = addNaturalAward(player, sCombat, times);
                rewardDataManager.addAwardDelaySync(player, lists, null, AwardFrom.TREASURE_ON_HOOK_AWARD, times * Constant.TREASURE_WARE_RES_OUTPUT_TIME_UNIT, combatId);

                Map<Integer, Integer> resultMap = new HashMap<>();
                lists.forEach(l -> resultMap.merge(l.get(1), l.get(2), Integer::sum));
                LogUtil.error("????????????", resultMap);
                break;
            case "clear":
                player.lord.setTreasureWareGolden(0);
                player.lord.setTreasureWareDust(0);
                player.lord.setTreasureWareEssence(0);
                break;
            case "clearCombat":
                player.setTreasureCombat(new TreasureCombat());
                player.setTreasureChallengePlayer(new TreasureChallengePlayer());
                break;
            case "setCombatId":
                StaticTreasureCombat staticTreasureCombat = StaticTreasureWareDataMgr.getTreasureCombatMap(Integer.parseInt(params[1]));
                if (staticTreasureCombat != null) {
                    TreasureCombat treasureCombat = player.getTreasureCombat();
                    treasureCombat.setCurCombatId(staticTreasureCombat.getCombatId());
                    treasureCombat.setSectionId(staticTreasureCombat.getSectionId());

                    Map<Integer, Integer> combatInfo = treasureCombat.getCombatInfo();
                    StaticTreasureWareDataMgr.getTreasureCombatMap().keySet().forEach(id -> {
                        if (id <= staticTreasureCombat.getCombatId()) {
                            combatInfo.put(id, 1);
                        }
                    });
                }
                break;
            case "restOnHook":
                player.getTreasureCombat().getOnHook().setStartTime(TimeHelper.getCurrentSecond());
                break;
        }
    }

}
