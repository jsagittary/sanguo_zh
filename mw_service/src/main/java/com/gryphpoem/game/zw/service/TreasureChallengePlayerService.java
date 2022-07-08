package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticTreasureWareDataMgr;
import com.gryphpoem.game.zw.logic.FightSettleLogic;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RankDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.manager.TaskDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb4;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Lord;
import com.gryphpoem.game.zw.resource.domain.s.StaticTreasureCombat;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.fight.FightLogic;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.treasureware.TreasureChallengePlayer;
import com.gryphpoem.game.zw.resource.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.gryphpoem.game.zw.resource.constant.TreasureChallengePlayerConstant.*;

/**
 * @Description: 宝具副本挑战玩家
 * @Author: DuanShQ
 * @CreateTime: 2022-06-13
 */
@Service
public class TreasureChallengePlayerService implements GmCmdService {
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private FightService fightService;
    @Autowired
    private TreasureCombatService treasureCombatService;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private FightSettleLogic fightSettleLogic;
    @Autowired
    private RankDataManager rankDataManager;
    @Autowired
    private TaskDataManager taskDataManager;

    /**
     * 获取挑战玩家数据
     */
    public CommonPb.TreasureChallengePlayerData getChallengeData(Player player) {
        TreasureChallengePlayer challengePlayer = getAndRefreshChallengePlayerData(player);

        CommonPb.TreasureChallengePlayerData.Builder builder = CommonPb.TreasureChallengePlayerData.newBuilder();
        builder.setChallengePlayerInfo(getChallengePlayerInfo(challengePlayer.getChallengePlayerId()));
        builder.setRemaining(challengePlayer.getRemaining());
        builder.setPurchaseNum(challengePlayer.getPurchaseNum());
        builder.setMaxPurchaseNum(CAN_PURCHASE_NUM);
        builder.setRemainingForPlayer(challengePlayer.getRemainingForPlayer());
        builder.setRemainingRefreshTime(challengePlayer.getRemainingRefreshTime());
        builder.setNeedRefreshChallengePlayer(challengePlayer.isNeedRefreshChallengePlayer());
        return builder.build();
    }

    /**
     * 获取并刷新挑战玩家的数据, 刷新时间 -> 初始化时、零点刷新时
     */
    private TreasureChallengePlayer getAndRefreshChallengePlayerData(Player player) {
        TreasureChallengePlayer challengePlayer = player.getTreasureChallengePlayer();

        // 初始化要挑战的玩家, 第一次进入宝具副本、合服后、清理小号后导致玩家不存在
        long challengePlayerId = challengePlayer.getChallengePlayerId();
        if (challengePlayerId <= 0 || playerDataManager.getPlayer(challengePlayerId) == null) {
            challengePlayerId = randomChallengePlayer(player, challengePlayer);
            challengePlayer.setChallengePlayerId(challengePlayerId);
            challengePlayer.setWin(false);
            challengePlayer.setChallengeForPlayerNum(0);
        }
        return challengePlayer;
    }

    /**
     * 随机出一个挑战的玩家
     */
    private long randomChallengePlayer(Player player, TreasureChallengePlayer challengePlayer) {
        List<Long> challengeList = getChallengeListByBaoDi(player, challengePlayer);

        // 没有触发保底, 或者保底没有找到玩家
        if (challengeList.isEmpty()) {
            // 在上20名和下20名中随机
            challengeList = challengeListByRank(player, challengePlayer);
        }

        // 全服就你一人，自己打自己吧
        if (challengeList.isEmpty()) {
            return player.getLordId();
        }

        // 前10次内不出现重复挑战对象
        if (challengeList.size() > challengePlayer.getRefreshedRecord().size()) {
            challengeList.removeAll(challengePlayer.getRefreshedRecord());
        }

        int randomIndex = RandomHelper.randomInSize(challengeList.size());
        return challengeList.get(randomIndex);
    }

    /**
     * 按照排名随机
     */
    private List<Long> challengeListByRank(Player player, TreasureChallengePlayer challengePlayer) {
        int myRank = rankDataManager.getMyRankByTypeAndScop(Constant.RankType.type_1, player.lord, RankDataManager.WORLD_SCOPE);
        int startRank = Math.max(1, myRank - CHALLENGE_RANK_RANGE);
        int limit = Math.min(CHALLENGE_RANK_RANGE, myRank - 1);

        List<Lord> up20List = rankDataManager.getSubRank(rankDataManager.fightRankList, startRank, limit);
        List<Lord> low20List = rankDataManager.getSubRank(rankDataManager.fightRankList, myRank + 1, CHALLENGE_RANK_RANGE);

        long lastChallengePlayerId = challengePlayer.getChallengePlayerId();
        List<Long> challengeList = new ArrayList<>(up20List.size() + low20List.size());
        challengeList.addAll(filterInvalidPlayer(up20List, lastChallengePlayerId));
        challengeList.addAll(filterInvalidPlayer(low20List, lastChallengePlayerId));
        return challengeList;
    }

    /**
     * 按保底随机
     */
    private List<Long> getChallengeListByBaoDi(Player player, TreasureChallengePlayer challengePlayer) {
        int failRefreshNum = challengePlayer.getFailRefreshNum();
        if (failRefreshNum >= MIN_BAO_DI && failRefreshNum <= MAX_BAO_DI) {
            // 保底战力百分比范围
            long fight = player.lord.getFight();
            int index = failRefreshNum - MIN_BAO_DI;

            // 循环往下探，避免出现战斗力在某个百分比范围断层现象
            for (int i = index; i < BAO_DI_FIGHT_PER_RANGE.length; i++) {
                long upFight = (long) (fight / Constant.HUNDRED * BAO_DI_FIGHT_PER_RANGE[i][0]);
                long lowFight = (long) (fight / Constant.HUNDRED * BAO_DI_FIGHT_PER_RANGE[i][1]);
                List<Lord> list = rankDataManager.getSubRankByCondition(rankDataManager.fightRankList,
                        (rank, l) -> l.getFight() >= lowFight && l.getFight() <= upFight, 1);

                List<Long> idList = filterInvalidPlayer(list, challengePlayer.getChallengePlayerId());

                // 更新挑战失败刷新次数, 作用是下次刷新时跳过断层
                challengePlayer.setFailRefreshNum(i + MIN_BAO_DI);

                if (!idList.isEmpty()) {
                    return idList;
                }
            }
        }

        return Collections.emptyList();
    }

    /**
     * 过滤无效的挑战对象
     */
    private List<Long> filterInvalidPlayer(List<Lord> list, long lastChallengePlayerId) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> result = new ArrayList<>(list.size());
        for (Lord lord : list) {
            long lordId = lord.getLordId();
            // 不能是上次挑战的玩家
            if (lordId == lastChallengePlayerId) {
                continue;
            }

            Player player = playerDataManager.getPlayer(lordId);
            if (player != null) {
                for (int heroId : player.heroBattle) {
                    if (heroId > 0) {
                        result.add(lordId);
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * 获取要挑战的玩家信息
     */
    private CommonPb.TreasureChallengePlayerInfo getChallengePlayerInfo(long challengePlayerId) {
        Player challengingPlayer = playerDataManager.checkPlayerIsExist(challengePlayerId);

        CommonPb.TreasureChallengePlayerInfo.Builder builder = CommonPb.TreasureChallengePlayerInfo.newBuilder();
        builder.setRoleId(challengePlayerId);
        builder.setName(challengingPlayer.lord.getNick());
        builder.setLv(challengingPlayer.lord.getLevel());
        builder.setFight(challengingPlayer.lord.getFight());
        builder.setCamp(challengingPlayer.lord.getCamp());
        builder.setIcon(challengingPlayer.lord.getPortrait());
        builder.setPortraitFrame(challengingPlayer.getDressUp().getCurPortraitFrame());
        challengingPlayer.getAllOnBattleHeros().forEach(h -> builder.addHero(createChallengeHero(h)));
        return builder.build();
    }

    private CommonPb.ChallengeHero createChallengeHero(Hero hero) {
        CommonPb.ChallengeHero.Builder builder = CommonPb.ChallengeHero.newBuilder();
        builder.setHeroId(hero.getHeroId());
        builder.setLevel(hero.getLevel());
        builder.setCount(hero.getAttr()[Constant.AttrId.LEAD]);
        builder.setPos(hero.getPos());
        builder.setGradeKeyId(hero.getGradeKeyId());
        builder.setDecorated(hero.getDecorated());
        return builder.build();
    }

    /**
     * 挑战玩家
     */
    public GamePb4.TreasureChallengePlayerRs challengePlayer(Long roleId, GamePb4.TreasureChallengePlayerRq req) {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        TreasureChallengePlayer challengePlayer = getAndRefreshChallengePlayerData(player);
        checkChallengePlayer(player, req.getHeroIdList(), challengePlayer);
        return doChallengePlayer(player, req.getHeroIdList(), challengePlayer);
    }

    /**
     * 检测是否能挑战玩家
     */
    private void checkChallengePlayer(Player player, List<Integer> heroIdList, TreasureChallengePlayer challengePlayer) {
        // 检测上阵英雄
        List<Integer> battleHeroId = Arrays.stream(player.heroBattle)
                .filter(i -> i > 0)
                .boxed()
                .collect(Collectors.toList());

        List<Integer> heroList = heroIdList.stream()
                .filter(id -> id > 0)
                .collect(Collectors.toList());

        if (!battleHeroId.containsAll(heroList)) {
            throw new MwException(GameError.PARAM_ERROR, "宝具副本挑战玩家 - 上阵英雄错误; roleId = " + player.getLordId() + "heroIdList = " + heroIdList);
        }

        // 检测是否可以挑战
        if (challengePlayer.getRemaining() <= 0) {
            throw new MwException(GameError.TREASURE_CHALLENGE_NUM_NOT_ENOUGH, "宝具副本挑战玩家 - 挑战次数不足; roleId = " + player.getLordId());
        }

        if (challengePlayer.isWin()) {
            throw new MwException(GameError.TREASURE_CHALLENGE_NUM_FOR_PLAYER_NOT_ENOUGH, "宝具副本挑战玩家 - 需要刷新挑战玩家; roleId = " + player.getLordId());
        }

        if (challengePlayer.getRemainingForPlayer() <= 0) {
            throw new MwException(GameError.TREASURE_CHALLENGE_NUM_FOR_PLAYER_NOT_ENOUGH, "宝具副本挑战玩家 - 对同一玩家挑战次数达到上限; roleId = " + player.getLordId());
        }
    }

    /**
     * 挑战玩家
     */
    private GamePb4.TreasureChallengePlayerRs doChallengePlayer(Player player, List<Integer> heroIdList, TreasureChallengePlayer challengePlayer) {
        long challengePlayerId = challengePlayer.getChallengePlayerId();
        Player challengingPlayer = playerDataManager.checkPlayerIsExist(challengePlayerId);

        Fighter attacker = fightService.createCombatPlayerFighter(player, heroIdList);
        Fighter defender = fightService.createCombatPlayerFighter(challengingPlayer, challengingPlayer.getAllOnBattleHeros().stream().map(Hero::getHeroId).collect(Collectors.toList()));
        FightLogic fightLogic = new FightLogic(attacker, defender, true);
        fightLogic.fight();

        challengePlayer.incChallengeForPlayerNum();

        List<CommonPb.Award> awards = null;
        int winState = fightLogic.getWinState();
        if (winState == 1) {
            challengePlayer.incChallengeNum();
            // 挑战胜利清空冷却时间
            challengePlayer.setCDStartTime(0);
            challengePlayer.setWin(true);

            // 挑战胜利的奖励
            int combatId = player.getTreasureCombat().getCurCombatId();
            StaticTreasureCombat sCombat = StaticTreasureWareDataMgr.getTreasureCombatMap(combatId);
            List<List<Integer>> awardList = treasureCombatService.addNaturalAward(player, sCombat, Constant.TREASURE_COMBAT_WIPE_AWARD);
            awards = rewardDataManager.addAwardDelaySync(player, awardList, null, AwardFrom.TREASURE_CHALLENGE_PLAYER_AWARD);

            // 埋点
            LogLordHelper.commonLog("treasureChallengePlayer", AwardFrom.TREASURE_CHALLENGE_PLAYER_AWARD, player,
                    challengingPlayer.getLordId(), challengingPlayer.lord.getFight());

            taskDataManager.updTask(player, TaskType.COND_533, 1);
        }

        GamePb4.TreasureChallengePlayerRs.Builder builder = GamePb4.TreasureChallengePlayerRs.newBuilder();
        builder.setChallengePlayer(getChallengeData(player));
        builder.setResult(winState);
        builder.setRecord(fightLogic.generateRecord());
        builder.addAllAtkHero(fightSettleLogic.stoneCombatCreateRptHero(player, attacker.forces));
        builder.addAllDefHero(defender.forces.stream().map(force -> {
            Hero hero = challengingPlayer.heros.get(force.id);
            if (CheckNull.isNull(hero))
                return PbHelper.createRptHero(Constant.Role.BANDIT, force.killed, 0, force.id, null, 0, 0, force.totalLost);
            return PbHelper.createRptHero(Constant.Role.BANDIT, force.killed, 0, hero, null, 0, 0, force.totalLost);
        }).collect(Collectors.toList()));
        if (Objects.nonNull(awards)) {
            builder.addAllAward(awards);
        }

        taskDataManager.updTask(player, TaskType.COND_530, 1);
        return builder.build();
    }

    /**
     * 刷新挑战的玩家
     */
    public GamePb4.TreasureRefreshChallengeRs refreshChallenge(Long roleId, boolean costDiamond) {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        TreasureChallengePlayer challengePlayer = getAndRefreshChallengePlayerData(player);

        int remainingRefreshTime = challengePlayer.getRemainingRefreshTime();
        if (!costDiamond && remainingRefreshTime > 0) {
            throw new MwException(GameError.TREASURE_CHALLENGE_REFRESH_IN_CD, "宝具副本 - 刷新挑战玩家冷却时间未到; roleId = " + roleId + "remainingRefreshTime = " + remainingRefreshTime);
        }

        if (costDiamond) {
            rewardDataManager.checkMoneyIsEnough(player, AwardType.Money.GOLD, CHALLENGE_REFRESH_COST_DIAMOND, "宝具挑战玩家刷新消耗钻石");
            rewardDataManager.subGold(player, CHALLENGE_REFRESH_COST_DIAMOND, AwardFrom.TREASURE_CHALLENGE_PLAYER_REFRESH);

            ChangeInfo change = ChangeInfo.newIns();
            change.addChangeType(AwardType.MONEY, AwardType.Money.GOLD);
            rewardDataManager.syncRoleResChanged(player, change);
        }

        // 增加挑战失败刷新次数
        boolean reset = challengePlayer.isWin() || challengePlayer.getFailRefreshNum() >= MAX_BAO_DI;
        challengePlayer.setFailRefreshNum(reset ? 1 : challengePlayer.getFailRefreshNum() + 1);

        long randomRoleId = randomChallengePlayer(player, challengePlayer);
        challengePlayer.recordRefreshed(randomRoleId);
        challengePlayer.setChallengePlayerId(randomRoleId);
        challengePlayer.setChallengeForPlayerNum(0);
        challengePlayer.setWin(false);
        if (!costDiamond) {
            challengePlayer.setCDStartTime(TimeHelper.getCurrentSecond());
        }

        GamePb4.TreasureRefreshChallengeRs.Builder builder = GamePb4.TreasureRefreshChallengeRs.newBuilder();
        builder.setChallengePlayer(getChallengeData(player));
        return builder.build();
    }

    /**
     * 购买挑战次数
     */
    public GamePb4.TreasureChallengePurchaseRs purchaseChallengeNum(Long roleId) {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        TreasureChallengePlayer challengePlayer = getAndRefreshChallengePlayerData(player);

        int purchaseNum = challengePlayer.getPurchaseNum();
        if (purchaseNum >= CAN_PURCHASE_NUM) {
            throw new MwException(GameError.TREASURE_CHALLENGE_PURCHASE_NUM_NOT_ENOUGH, "宝具副本 - 每日可购买挑战次数已达到上限; roleId = " + roleId);
        }
        Integer num = PURCHASE_COST.get(purchaseNum);
        rewardDataManager.checkMoneyIsEnough(player, AwardType.Money.GOLD, num, "宝具购买挑战次数消耗钻石");
        rewardDataManager.subGold(player, num, AwardFrom.TREASURE_CHALLENGE_PLAYER_PURCHASE_NUM);

        ChangeInfo change = ChangeInfo.newIns();
        change.addChangeType(AwardType.MONEY, AwardType.Money.GOLD);
        rewardDataManager.syncRoleResChanged(player, change);

        challengePlayer.incPurchaseNum();

        GamePb4.TreasureChallengePurchaseRs.Builder builder = GamePb4.TreasureChallengePurchaseRs.newBuilder();
        builder.setChallengePlayer(getChallengeData(player));
        return builder.build();
    }

    /**
     * 转点处理
     */
    public void acrossTheDayProcess(Player p) {
        if (Objects.isNull(p)) {
            return;
        }
        TreasureChallengePlayer challengePlayer = p.getTreasureChallengePlayer();
        challengePlayer.setChallengeNum(0);
        challengePlayer.setPurchaseNum(0);
        challengePlayer.getRefreshedRecord().clear();
    }

    @GmCmd("TreasureChallenge")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {
        // 清空挑战玩家数据
        if (params[0].equalsIgnoreCase("clearPurchaseNum")) {
            TreasureChallengePlayer challengePlayer = getAndRefreshChallengePlayerData(player);
            challengePlayer.setPurchaseNum(0);
        }
    }
}
