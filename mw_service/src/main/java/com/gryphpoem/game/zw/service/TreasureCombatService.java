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
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticTreasureCombat;
import com.gryphpoem.game.zw.resource.domain.s.StaticVip;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.fight.FightLogic;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.treasureware.TreasureCombat;
import com.gryphpoem.game.zw.resource.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

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
        if (wipe == 1) {
            if (pass == 0) {
                throw new MwException(GameError.COMBAT_PASS_BEFORE.getCode(), String.format("挑战关卡时，当前关卡未通关, roleId: %s, combatId: %s", roleId, combatId));
            }
            int dailyWipeCnt = treasureCombat.getDailyWipeCnt();
            if (dailyWipeCnt + 1 > roleDailyWipeMaxCnt(player)) {
                throw new MwException(GameError.TREASURE_COMBAT_DAILY_PROMOTE_MAX.getCode(), String.format("挑战关卡时，已达每日扫荡上限, roleId: %s", roleId));
            }
            // 每日一次免费次数
            if (dailyWipeCnt >= Constant.TREASURE_COMBAT_DAILY_WIPE_MAX) {
                int wipePrice = wipePrice(dailyWipeCnt);
                rewardDataManager.checkMoneyIsEnough(player, AwardType.Money.GOLD, wipePrice);
                rewardDataManager.subGold(player, wipePrice, AwardFrom.TREASURE_COMBAT_WIPE_COST, combatId);
            }
            builder.setCost(player.lord.getGold());
            treasureCombat.setDailyWipeCnt(dailyWipeCnt + 1);
            int startTime = treasureCombat.getOnHook().getStartTime();
            int now = TimeHelper.getCurrentSecond();
            int interval = now - startTime;
            builder.addAllAward(rewardDataManager.addAwardDelaySync(player, addNaturalAward(sCombat, Constant.TREASURE_COMBAT_WIPE_AWARD), null, AwardFrom.TREASURE_COMBAT_WIPE_AWARD, interval, treasureCombat.getCurCombatId()));
            //  扫荡埋点
            LogLordHelper.commonLog("treasureCombatWipe", AwardFrom.TREASURE_COMBAT_WIPE_AWARD, player);
        } else if (wipe == 0) {
            if (pass != 0) {
                throw new MwException(GameError.COMBAT_PASSED.getCode(), String.format("挑战关卡时，当前关卡已通关, roleId: %s, combatId: %s", roleId, combatId));
            }
            // 将领校验和阵型记录
            checkHeroCombat(player, heroIds);
            treasureCombat.swapHeroForm(heroIds);
            // 每日推进副本校验
            if (treasureCombat.getDailyPromoteCnt() >= Constant.TREASURE_COMBAT_DAILY_PROMOTE_MAX) {
                throw new MwException(GameError.TREASURE_COMBAT_DAILY_PROMOTE_MAX.getCode(), String.format("挑战关卡时，已达每日推进上限, roleId: %s", roleId));
            }
            // 增益添加到Fighter#Force#AttrData中, 只参与计算
            Fighter attacker = fightService.createTreasureCombatFighter(player, heroIds);
            Fighter defender = fightService.createNpcFighter(sCombat.getForm());
            FightLogic fightLogic = new FightLogic(attacker, defender, true);
            fightLogic.fight();
            int winState = fightLogic.getWinState();
            if (winState == 1) {
                // 每日推进进度记录
                combatInfo.put(combatId, 1);
                treasureCombat.promoteCombat(combatId);
                builder.addAllAward(rewardDataManager.addAwardDelaySync(player, sCombat.getFirstAward(), null, AwardFrom.TREASURE_COMBAT_PROMOTE_AWARD));
                // 副本埋点
                LogLordHelper.commonLog("treasureCombatPromote", AwardFrom.TREASURE_COMBAT_PROMOTE_AWARD, player, combatId);
                if (CheckNull.nonEmpty(sCombat.getSectionAward())) {
                    // 可领取章节奖励
                    treasureCombat.getSectionStatus().put(sCombat.getCombatId(), 1);
                }

                DataResource.getBean(ActivityTriggerService.class).doTreasureCombat(player, combatId);
            } else {
                // do nothing
            }
            builder.setResult(winState);
            builder.setRecord(fightLogic.generateRecord());
            builder.addAllAtkHero(fightSettleLogic.stoneCombatCreateRptHero(player, attacker.forces));
            builder.addAllDefHero(defender.forces.stream().map(force -> PbHelper.createRptHero(Constant.Role.BANDIT, force.killed, 0, force.id, null, 0, 0, force.totalLost)).collect(Collectors.toList()));
        } else if (wipe == 2) {
            Integer nextSectionId = StaticTreasureWareDataMgr.getNextSectionId(treasureCombat.getSectionId());
            if (CheckNull.isNull(nextSectionId)) {
                throw new MwException(GameError.NO_CONFIG.getCode(), String.format("解锁新章节时，无关卡配置, roleId: %s, combatId: %s, sectionId: %s", roleId, combatId, treasureCombat.getSectionId()));
            }

            treasureCombat.setSectionId(nextSectionId);
            builder.setSectionId(nextSectionId);
        }

        builder.setCombat(treasureCombat.ser(true));
        return builder.build();
    }

    /**
     * 购买价格
     * @param wipeCount 扫荡次数
     * @return 价格
     */
    private int wipePrice(int wipeCount) {
        int maxKey = Constant.TREASURE_WIPE_INCREASE_PRICE.keySet().stream().max(Integer::compareTo).get();
        // 购买价格
        int key = Math.min(maxKey, wipeCount);
        return Constant.TREASURE_WIPE_INCREASE_PRICE.get(key);
    }

    /**
     * 自然产出奖励
     *
     * @param sCombat 副本配置
     * @param cnt     次数
     * @return 奖励
     */
    private List<List<Integer>> addNaturalAward(StaticTreasureCombat sCombat, int cnt) {
        List<List<Integer>> list = new ArrayList<>();
        List<List<Integer>> minuteAward = sCombat.getMinuteAward();
        for (List<Integer> award : minuteAward) {
            list.add(Arrays.asList(award.get(0), award.get(1), award.get(2) * cnt));
        }
        List<List<Integer>> minuteRandomAward = sCombat.getMinuteRandomAward();
        List<Integer> randomAward;
        while (cnt-- > 0) {
            randomAward = RandomUtil.getRandomByWeightAndRatio(minuteRandomAward, 3, false, (int) Constant.TEN_THROUSAND);
            if (ObjectUtils.isEmpty(randomAward))
                continue;
            list.add(randomAward);
        }

        return RewardDataManager.mergeAward(list);
    }

    /**
     * 副本挑战将领判断
     *
     * @param player  玩家
     * @param heroIds 将领id
     * @throws MwException 异常
     */
    private void checkHeroCombat(Player player, List<Integer> heroIds) throws MwException {
        long roleId = player.roleId;
        if (heroIds.size() > player.getTreasureCombat().getUnLockHeroPos() || heroIds.size() < 1) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("挑战关卡时, 将领数量有误, roleId: %s, heroSize: %s", roleId, heroIds.size()));
        }

        int count = 0;
        for (int heroId : heroIds) {
            if (heroId == 0)
                continue;
            Hero hero = player.heros.get(heroId);
            if (hero == null) {
                throw new MwException(GameError.HERO_NOT_FOUND.getCode(), String.format("挑战关卡时, 选择的将领不存在, roleId: %s, heroId: %s", roleId, heroId));
            }
            ++count;
        }

        if (count > player.getTreasureCombat().getUnLockHeroPos()) {
            throw new MwException(GameError.TREASURE_COMBAT_HERO_OUT_OF_INDEX.getCode(), String.format("挑战关卡时, 选择的将领不存在," +
                    " roleId: %s, heroIds: %s", roleId, StringUtils.join(heroIds.toArray(), ",")));
        }
    }


    /**
     * 解锁宝具副本将领上阵位
     *
     * @param roleId 玩家
     * @param index  要解锁的上阵位
     * @return 副本数据
     * @throws MwException 异常
     */
    public GamePb4.TreasureUnlockHeroPosRs treasureUnlockHeroPos(long roleId, int index) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        treasureWareService.checkOpenTreasureWare(player);

        TreasureCombat treasureCombat = player.getTreasureCombat();
        if (index > 8 || index < treasureCombat.getUnLockHeroPos()) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("解锁副本将领上阵位, 位置错误, roleId: %s, index: %s", roleId, index));
        }

        List<Integer> conf = Constant.TREASURE_UNLOCK_HERO_POS_CONF.stream().filter(c -> c.get(0) == index).findAny().orElse(null);
        if (CheckNull.isEmpty(conf)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), String.format("解锁副本将领上阵位, 未找到对应位置的配置, roleId: %s, index: %s", roleId, index));
        }
        int combatId = conf.get(1);
        if (treasureCombat.getCombatInfo().getOrDefault(combatId, 0) == 0) {
            throw new MwException(GameError.COMBAT_PASS_BEFORE.getCode(), String.format("挑战关卡时，当前关卡未通关, roleId: %s, combatId: %s", roleId, combatId));
        }

        int type = conf.get(2);
        int id = conf.get(3);
        int count = conf.get(4);

        rewardDataManager.checkAndSubPlayerRes(player, type, id, count, AwardFrom.TREASURE_UNLOCK_HERO_POS, true);
        treasureCombat.setUnLockHeroPos(index);

        return GamePb4.TreasureUnlockHeroPosRs.newBuilder().setCombat(treasureCombat.ser(true)).build();
    }

    /**
     * 玩家每日最大可扫荡次数
     *
     * @param player 玩家
     * @return 最大扫荡次数
     */
    private int roleDailyWipeMaxCnt(Player player) {
        StaticVip sVip = StaticVipDataMgr.getVipMap(player.lord.getVip());
        return Objects.isNull(sVip) ? Constant.TREASURE_COMBAT_DAILY_WIPE_MAX : Constant.TREASURE_COMBAT_DAILY_WIPE_MAX + sVip.getHangUpTime();
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
        if (count > 0) {
            builder.addAllAward(rewardDataManager.addAwardDelaySync(player, addNaturalAward(sCombat, count), null, AwardFrom.TREASURE_ON_HOOK_AWARD, interval, treasureCombat.getCurCombatId()));
            onHook.setStartTime(TimeHelper.getCurrentSecond());
        }

        taskDataManager.updTask(player, TaskType.COND_GET_TREASURE_WARE_HOOK_AWARD, 1);
        return builder.setHook(onHook.ser()).build();
    }

    /**
     * 领取章节奖励
     * @param roleId 角色id
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
        builder.addAllAward(rewardDataManager.addAwardDelaySync(player, sectionAward, null, AwardFrom.TREASURE_COMBAT_PROMOTE_AWARD, "treasureCombatSection"));
        return builder.build();
    }

    /**
     * 使用道具增加宝具副本相关道具
     *
     * @param propCount
     * @param player
     * @param addAward
     * @param propId
     * @param listAward
     * @param change
     * @throws MwException
     */
    public void useProp(int propCount, Player player, List<List<Integer>> addAward, int propId, List<CommonPb.Award> listAward, ChangeInfo change)  throws MwException {
        if (player.getTreasureCombat().getCurCombatId() <= 0) {
            throw new MwException(GameError.COMBAT_PASS_BEFORE.getCode(), String.format("领取章节奖励时，当前关卡未通关, roleId: %s, curCombatId: %s, propId: %s",
                    player.lord.getLordId(), player.getTreasureCombat().getCurCombatId(), propId));
        }

        StaticTreasureCombat sConf = StaticTreasureWareDataMgr.getTreasureCombatMap(player.getTreasureCombat().getCurCombatId());
        if (Objects.isNull(sConf)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), String.format("使用挂机奖励道具时, 无关卡配置, roleId: %s, combatId: %s, propId: %s",
                    player.lord.getLordId(), player.getTreasureCombat().getCurCombatId(), propId));
        }

        int multiple = 0;
        List<Integer> singleStaticAward = null;
        List<Integer> singleRandomAward = null;
        List<List<Integer>> singleAward = null;
        for (List<Integer> awardTime : addAward) {
            List<Integer> staticTmp = sConf.getMinuteAward().stream().filter(award -> award.get(0) == awardTime.get(0)
                    && award.get(1) == awardTime.get(1)).findAny().orElse(null);
            List<Integer> randomTmp = sConf.getMinuteRandomAward().stream().filter(award -> award.get(0) == awardTime.get(0)
                    && award.get(1) == awardTime.get(1)).findAny().orElse(null);
            if (ObjectUtils.isEmpty(staticTmp) && ObjectUtils.isEmpty(randomTmp))
                continue;

            //奖励次数
            multiple = awardTime.get(2) / Constant.TREASURE_WARE_RES_OUTPUT_TIME_UNIT;
            if (multiple <= 0)
                continue;
            multiple *= propCount;

            if (Objects.nonNull(staticTmp)) {
                singleStaticAward = Arrays.asList(staticTmp.get(0), staticTmp.get(1), staticTmp.get(2) * multiple);
            }
            if (Objects.nonNull(randomTmp)) {
                singleRandomAward = Arrays.asList(randomTmp.get(0), randomTmp.get(1), randomTmp.get(2) * multiple);
            }

            singleAward = CheckNull.isNull(singleAward) ? new ArrayList<>() : singleAward;
            if (!ObjectUtils.isEmpty(singleStaticAward)) {
                if (ObjectUtils.isEmpty(singleRandomAward)) {
                    singleAward.add(singleStaticAward);
                } else {
                    singleAward.add(Arrays.asList(singleStaticAward.get(0), singleStaticAward.get(1), singleStaticAward.get(2) + singleRandomAward.get(2)));
                }
            } else {
                singleAward.add(singleRandomAward);
            }
        }

        if (ObjectUtils.isEmpty(singleAward)) {
            throw new MwException(GameError.NO_CONFIG.getCode(), String.format("使用挂机奖励道具时, 没有相关资源增益, roleId: %s, combatId: %s, propId: %s",
                    player.lord.getLordId(), player.getTreasureCombat().getCurCombatId(), propId));
        }

        listAward.addAll(rewardDataManager.addAwardDelaySync(player, singleAward, change,
                AwardFrom.USE_PROP, propId));
    }


    @GmCmd("treasureWareCombat")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {
        switch (params[0]) {
            case "naturalAward":
                int forEachCount = Integer.parseInt(params[1]);
                int randomCount = Integer.parseInt(params[2]);
                long start = System.currentTimeMillis();
                LogUtil.error(String.format("暴力随机测试, start: %s", start));
                StaticTreasureCombat sCombat = StaticTreasureWareDataMgr.getTreasureCombatMap(8012);
                for (int i = 0; i < forEachCount; i++) {
                    addNaturalAward(sCombat, randomCount);
                }
                long end = System.currentTimeMillis();
                LogUtil.error(String.format("暴力随机测试, end: %s, elapsed_time: %s", end, end - start));
                break;
            case "clear":
                player.lord.setTreasureWareGolden(0);
                player.lord.setTreasureWareDust(0);
                player.lord.setTreasureWareEssence(0);
                break;
            case "clearCombat":
                player.setTreasureCombat(new TreasureCombat());
                break;
            case "setCombatId":
                player.getTreasureCombat().setCurCombatId(Integer.parseInt(params[1]));
        }
    }

    /**
     * 转点处理
     * @param p 玩家
     */
    public void acrossTheDayProcess(Player p) {
        if (Objects.isNull(p)) {
            return;
        }
        TreasureCombat treasureCombat = p.getTreasureCombat();
        // 重置每日扫荡次数和每日推进副本次数
        treasureCombat.setDailyWipeCnt(0);
        treasureCombat.setDailyPromoteCnt(0);
    }
}
