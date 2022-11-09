package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticTreasureWareDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticVipDataMgr;
import com.gryphpoem.game.zw.logic.FightSettleLogic;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.manager.TaskDataManager;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Fighter;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticTreasureCombat;
import com.gryphpoem.game.zw.resource.domain.s.StaticVip;
import com.gryphpoem.game.zw.resource.pojo.activity.ETask;
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
     * 宝具副本详情
     *
     * @param roleId 玩家
     * @return 副本详情
     */
    public GamePb4.GetTreasureCombatRs getTreasureCombat(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        GamePb4.GetTreasureCombatRs.Builder builder = GamePb4.GetTreasureCombatRs.newBuilder();
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_TREASURE_WARE)) {
            return builder.build();
        }

        // 宝具副本信息
        TreasureCombat treasureCombat = player.getTreasureCombat();
        if (Objects.nonNull(treasureCombat)) {
            builder.setCombat(treasureCombat.ser(true));

            if (treasureCombat.getCurCombatId() > 0) {
                // 挑战玩家信息
                builder.setChallengePlayer(treasureChallengePlayerService.getChallengeData(player));
            }
        }
        return builder.build();
    }

    /**
     * 宝具副本
     *
     * @param roleId 玩家id
     * @param req    请求参数
     * @return 副本详情
     * @throws MwException 异常
     */
    public GamePb4.DoTreasureCombatRs doTreasureCombat(long roleId, GamePb4.DoTreasureCombatRq req) throws MwException {
        int combatId = req.getCombatId();
        int wipe = req.getWipe();
        List<Integer> heroIds = req.getHeroIdList();

        Player player = playerDataManager.checkPlayerIsExist(roleId);
        treasureWareService.checkOpenTreasureWare(player);

        StaticTreasureCombat sCombat = StaticTreasureWareDataMgr.getTreasureCombatMap(combatId);
        if (Objects.isNull(sCombat)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), String.format("挑战副本时，无关卡配置, roleId: %s, combatId: %s", roleId, combatId));
        }

        // 宝具副本详情
        TreasureCombat treasureCombat = player.getTreasureCombat();

        // 前置关卡
        int preId = sCombat.getPreId();
        Map<Integer, Integer> combatInfo = treasureCombat.getCombatInfo();
        if (preId != 0) {
            if (!combatInfo.containsKey(preId) || combatInfo.getOrDefault(preId, 0) < 1) {
                throw new MwException(GameError.COMBAT_PASS_BEFORE.getCode(), String.format("挑战关卡时，前置关卡未通关, roleId: %s, preId: %s, combatId: %s", roleId, preId, combatId));
            }
        }

        GamePb4.DoTreasureCombatRs.Builder builder = GamePb4.DoTreasureCombatRs.newBuilder();

        int pass = combatInfo.getOrDefault(combatId, 0);
        if (wipe == 0) {
            if (pass != 0) {
                throw new MwException(GameError.COMBAT_PASSED.getCode(), String.format("挑战关卡时，当前关卡已通关, roleId: %s, combatId: %s", roleId, combatId));
            }

            // 关卡要求的领主等级限制
            if (player.lord.getLevel() < sCombat.getNeedLv()) {
                throw new MwException(GameError.TREASURE_COMBAT_LEVELS_NOT_UNLOCK.getCode(), String.format("挑战宝具副本关卡, 领主等级不足, roleId: %s, combatId: %s, needLv: %s, lv: %s", roleId, combatId, sCombat.getNeedLv(), player.lord.getLevel()));
            }

            // 将领校验和阵型记录
            checkHeroCombat(player, heroIds);
            Fighter attacker = fightService.createCombatPlayerFighter(player, heroIds);
            Fighter defender = fightService.createNpcFighter(sCombat.getForm());

            FightLogic fightLogic = new FightLogic(attacker, defender, true);
            fightLogic.start();
            int winState = fightLogic.getWinState();
            if (winState == 1) {
                combatInfo.put(combatId, 1);
                treasureCombat.promoteCombat(combatId);
                builder.addAllAward(rewardDataManager.addAwardDelaySync(player, sCombat.getFirstAward(), null, AwardFrom.TREASURE_COMBAT_PROMOTE_AWARD));
                if (CheckNull.nonEmpty(sCombat.getSectionAward())) {
                    // 可领取章节奖励
                    treasureCombat.getSectionStatus().put(sCombat.getCombatId(), 1);
                }

                DataResource.getBean(ActivityTriggerService.class).doTreasureCombat(player, combatId);
                taskDataManager.updTask(player, TaskType.COND_525, 1);
                taskDataManager.updTask(player, TaskType.COND_531, 1, combatId);

                // 宝具征程活动任务
                TaskService.processTask(player, ETask.PASS_TREASURE_WARE_COMBAT_ID, combatId);
            } else {
                // do nothing
            }

            // 副本埋点
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
                throw new MwException(GameError.NO_CONFIG.getCode(), String.format("解锁新章节时，无关卡配置, roleId: %s, combatId: %s, sectionId: %s", roleId, combatId, treasureCombat.getSectionId()));
            }

            StaticTreasureCombat staticTreasureCombat = StaticTreasureWareDataMgr.getTreasureCombatMap().values().stream()
                    .filter(s -> s.getPreId() == treasureCombat.getCurCombatId())
                    .findFirst()
                    .orElse(null);
            if (CheckNull.isNull(staticTreasureCombat)) {
                throw new MwException(GameError.NO_CONFIG.getCode(), String.format("解锁新章节时，无关卡配置, roleId: %s, combatId: %s, sectionId: %s", roleId, combatId, treasureCombat.getSectionId()));
            }

            treasureCombat.setSectionId(nextSectionId);
            builder.setSectionId(nextSectionId);
        }

        builder.setCombat(treasureCombat.ser(true));
        return builder.build();
    }

    /**
     * 检测能否开启下一章节
     */
    private void checkOpenNextSection(Player player, TreasureCombat treasureCombat) {
        int curCombatId = treasureCombat.getCurCombatId();
        int sectionId = treasureCombat.getSectionId();
        Integer maxCombatId = StaticTreasureWareDataMgr.getTreasureCombatMaxCombatMap().getOrDefault(sectionId, 0);
        if (curCombatId != maxCombatId) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("宝具副本 - 非最后一个关卡不能开启新章节, roleId: %s, combatId: %s, sectionId: %s", player.getLordId(), curCombatId, sectionId));
        }
    }

    /**
     * 自然产出奖励
     *
     * @param sCombat 副本配置
     * @param cnt     次数
     * @return 奖励
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

                    // 记录特殊道具的掉落个数
                    if (awardList.get(0) == AwardType.PROP && TREASURE_SPECIAL_PROPS_ID.contains(awardList.get(1))) {
                        player.getTreasureCombat().getRandomPropNumMap().merge(awardList.get(1), awardList.get(2), Integer::sum);
                    }
                }
            }
        }

        return RewardDataManager.mergeAward(list);
    }

    /**
     * 随机掉落数量限制处理, 1607每天最多掉2次, 1608每天最多掉1次
     */
    private List<List<Integer>> handRandomPropNumLimit(Player player, StaticTreasureCombat sCombat) {
        // 对1607、1608材料的掉落数量做限制
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
     * 副本挑战将领判断
     *
     * @param player  玩家
     * @param heroIds 将领id
     * @throws MwException 异常
     */
    private void checkHeroCombat(Player player, List<Integer> heroIds) throws MwException {
        // 检测上阵英雄
        List<Integer> battleHeroId = Arrays.stream(player.heroBattle)
                .filter(i -> i > 0)
                .boxed()
                .collect(Collectors.toList());

        List<Integer> heroList = heroIds.stream()
                .filter(id -> id > 0)
                .collect(Collectors.toList());

        if (!battleHeroId.containsAll(heroList)) {
            throw new MwException(GameError.PARAM_ERROR, "宝具副本挑战关卡 - 上阵英雄错误; roleId = " + player.getLordId() + "heroIdList = " + heroIds);
        }

//        TreasureChallengePlayer treasureChallengePlayer = treasureChallengePlayerService.getAndRefreshChallengePlayerData(player);
//        treasureChallengePlayer.setBattleHeroList(heroList);
    }

    /**
     * 玩家单次最大可
     *
     * @param player 玩家
     * @return 最大扫荡次数
     */
    private int roleOnHookMaxInterval(Player player) {
        StaticVip sVip = StaticVipDataMgr.getVipMap(player.lord.getVip());
        return Objects.isNull(sVip) ? Constant.TREASURE_ON_HOOK_AGGREGATE : Constant.TREASURE_ON_HOOK_AGGREGATE + sVip.getCumulativeTime();
    }

    /**
     * 宝具挂机奖励领取
     *
     * @param roleId 角色id
     * @return 奖励
     * @throws MwException 异常
     */
    public GamePb4.TreasureOnHookAwardRs treasureOnHookAward(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        treasureWareService.checkOpenTreasureWare(player);

        TreasureCombat treasureCombat = player.getTreasureCombat();
        TreasureCombat.TreasureOnHook onHook = treasureCombat.getOnHook();
        int startTime = onHook.getStartTime();
        int curCombatId = treasureCombat.getCurCombatId();
        if (startTime == 0 || curCombatId == 0) {
            throw new MwException(GameError.COMBAT_PASS_BEFORE.getCode(), String.format("挑战关卡时，未通关任何副本, roleId: %s", roleId));
        }

        StaticTreasureCombat sCombat = StaticTreasureWareDataMgr.getTreasureCombatMap(curCombatId);
        if (Objects.isNull(sCombat)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), String.format("领取挂机奖励时, 无关卡配置, roleId: %s, combatId: %s", roleId, curCombatId));
        }

        int now = TimeHelper.getCurrentSecond();
        int maxInterval = roleOnHookMaxInterval(player);
        int interval = now - startTime;
        // 单次最大可领取挂机时间
        interval = interval > maxInterval ? maxInterval : interval;

        // 间隔时间 / 15 秒 = 挂机奖励次数
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
     * 领取章节奖励
     *
     * @param roleId   角色id
     * @param combatId 章节id
     * @return 奖励
     * @throws MwException 异常
     */
    public GamePb4.TreasureSectionAwardRs treasureSectionAward(long roleId, int combatId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        treasureWareService.checkOpenTreasureWare(player);

        TreasureCombat treasureCombat = player.getTreasureCombat();
        Map<Integer, Integer> sectionStatus = treasureCombat.getSectionStatus();
        if (sectionStatus.get(combatId) == 2) {
            throw new MwException(GameError.TREASURE_COMBAT_SECTION_AWARD_ERROR.getCode(), String.format("领取章节奖励时，已经领取章节奖励, roleId: %s, sectionId: %s", roleId, combatId));
        }

        StaticTreasureCombat sConf = StaticTreasureWareDataMgr.getTreasureCombatMap(combatId);
        if (Objects.isNull(sConf) || ObjectUtils.isEmpty(sConf.getSectionAward())) {
            throw new MwException(GameError.NO_CONFIG.getCode(), String.format("领取章节奖励时, 无关卡配置, roleId: %s, combatId: %s", roleId, combatId));
        }
        if (treasureCombat.getCombatInfo().getOrDefault(combatId, 0) != 1) {
            throw new MwException(GameError.COMBAT_PASS_BEFORE.getCode(), String.format("领取章节奖励时，当前关卡未通关, roleId: %s, combatId: %s", roleId, combatId));
        }

        // 章节奖励
        List<List<Integer>> sectionAward = sConf.getSectionAward();
        sectionStatus.put(combatId, 2);
        GamePb4.TreasureSectionAwardRs.Builder builder = GamePb4.TreasureSectionAwardRs.newBuilder();
        builder.setCombatId(combatId);
        builder.setStatus(2);
        builder.addAllAward(rewardDataManager.addAwardDelaySync(player, sectionAward, null, AwardFrom.TREASURE_ON_HOOK_AWARD));
        return builder.build();
    }

    /**
     * 转点处理
     */
    public void acrossTheDayProcess(Player p) {
        if (Objects.isNull(p)) {
            return;
        }
        TreasureCombat treasureCombat = p.getTreasureCombat();
        if (Objects.nonNull(treasureCombat)) {
            // 清空每日掉落数据
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
                    LogUtil.error("无效的宝具副本combatId: " + combatId);
                    return;
                }

                List<List<Integer>> lists = addNaturalAward(player, sCombat, times);
                rewardDataManager.addAwardDelaySync(player, lists, null, AwardFrom.TREASURE_ON_HOOK_AWARD, times * Constant.TREASURE_WARE_RES_OUTPUT_TIME_UNIT, combatId);

                Map<Integer, Integer> resultMap = new HashMap<>();
                lists.forEach(l -> resultMap.merge(l.get(1), l.get(2), Integer::sum));
                LogUtil.error("奖励结果", resultMap);
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
