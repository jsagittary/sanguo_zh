package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticActivityDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticPropDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWarPlaneDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Activity;
import com.gryphpoem.game.zw.resource.domain.p.BuildingExt;
import com.gryphpoem.game.zw.resource.domain.s.StaticPlaneInit;
import com.gryphpoem.game.zw.resource.domain.s.StaticPlaneUpgrade;
import com.gryphpoem.game.zw.resource.domain.s.StaticProp;
import com.gryphpoem.game.zw.resource.domain.s.StaticSpecialPlan;
import com.gryphpoem.game.zw.resource.pojo.*;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.CalculateUtil;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author: ZhouJie
 * @date: Create in 2018-10-11 15:56
 * @description: 战机相关
 * @modified By:
 */
@Service public class WarPlaneService {

    @Autowired private ArmyService armyService;

    @Autowired private HeroService heroService;

    @Autowired private TechDataManager techDataManager;

    @Autowired private PlayerDataManager playerDataManager;

    @Autowired private WarPlaneDataManager warPlaneDataManager;

    @Autowired private RewardDataManager rewardDataManager;

    @Autowired private BuildingDataManager buildingDataManager;

    @Autowired private ActivityDataManager activityDataManager;

    @Autowired private TaskDataManager taskDataManager;

    /**
     * 获取所有战机
     *
     * @param roleId 角色id
     * @throws MwException
     */
    public GamePb1.GetWarPlanesRs getWarPlanes(long roleId) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        GamePb1.GetWarPlanesRs.Builder builder = GamePb1.GetWarPlanesRs.newBuilder();
        for (WarPlane warPlane : player.warPlanes.values()) {
            builder.addPlane(warPlaneDataManager.createWarPlanePb(warPlane, player));
        }
        for (PlaneChip planeChip : player.palneChips.values()) {
            if (planeChip.getCnt() > 0) {
                builder.addChips(PbHelper.createTwoIntPb(planeChip.getChipId(), planeChip.getCnt()));
            }
        }
        // 返回战机所有信息
        return builder.build();
    }

    /**
     * 获取部分战机数据
     *
     * @param roleId   角色id
     * @param planeIds 战机ids
     * @throws MwException
     */
    public GamePb1.GetPlaneByIdsRs getPlaneByIds(long roleId, List<Integer> planeIds) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        GamePb1.GetPlaneByIdsRs.Builder builder = GamePb1.GetPlaneByIdsRs.newBuilder();
        WarPlane plane;
        for (int planeId : planeIds) {
            plane = player.checkWarPlaneIsExist(planeId);
            if (CheckNull.isNull(plane)) {
                continue;
            }
            builder.addPlane(warPlaneDataManager.createWarPlanePb(plane, player));
        }
        snyThreeRebate(player);
        return builder.build();
    }

    /**
     * 战机替换
     *
     * @param roleId
     * @param req
     * @throws MwException
     */
    public GamePb1.PlaneSwapRs planeSwap(long roleId, GamePb1.PlaneSwapRq req) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int planeId = req.getPlaneId(); // 上阵战机
        int heroId = req.getHeroId(); // 上阵的将领
        int type = req.getType();
        int swapId = req.getSwapId(); // 被替换的战机
        boolean swap = false;
        WarPlane swapPlane = null; // 被替换的战机
        int battlePos = req.getPos(); // 上阵队列的位置

        if (swapId > 0) {
            swap = true;
            swapPlane = player.checkWarPlaneIsExist(swapId);
            if (CheckNull.isNull(swapPlane)) {
                throw new MwException(GameError.WAR_PLANE_NOT_FOUND.getCode(), "玩家没有这个战机, roleId:", roleId,
                        ", planeId:", planeId);
            }
            if (swapPlane.getHeroId() != heroId) {
                throw new MwException(GameError.SWAP_PLANE_ERROR.getCode(), "战机替换时候, 被替换战机信息有误, heroId:", heroId,
                        ", swapHeroId:", swapPlane.getHeroId());
            }
        }

        // 检查玩家是否有该将领
        Hero hero = heroService.checkHeroIsExist(player, heroId);
        if (!hero.isOnBattle() && !hero.isCommando()) {
            throw new MwException(GameError.HERO_NOT_BATTLE.getCode(), "战机替换时候, 将领未上阵, heroId:", heroId);
        }

        // 将领不是空闲状态
        if (!hero.isIdle() && hero.getState() != ArmyConstant.ARMY_STATE_RETREAT) {
            throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "将领不是空闲状态不能操作, heroId:", heroId);
        }

        // 检测玩家是否有该战机
        WarPlane plane = player.checkWarPlaneIsExist(planeId);
        if (CheckNull.isNull(plane)) {
            throw new MwException(GameError.WAR_PLANE_NOT_FOUND.getCode(), "玩家没有这个战机, roleId:", roleId, ", planeId:",
                    planeId);
        }

        if (type == PlaneConstant.PLANE_SWAP_TYPE_UP) { // 上阵
            if (swap && !CheckNull.isNull(swapPlane)) {
                if (hero.downPlane(swapId)) { // 被替换的战机下阵
                    battlePos = swapPlane.getBattlePos();
                    swapPlane.downBattle(hero);
                }
            }
            if (!plane.isIdle()) {
                throw new MwException(GameError.PLANE_NOT_IDLE.getCode(), "战机不是空闲状态不能操作, palneId:", planeId);
            }
            if (hero.upPlane(planeId, warPlaneDataManager.planeOpenSize(player)) && battlePos > 0) {
                plane.upBattle(hero, hero.isCommando()? hero.getCommandoPos() + HeroConstant.HERO_BATTLE_LEN : hero.getPos(), battlePos);
            } else {
                throw new MwException(GameError.PLANE_BATTLE_NUM_ERROR.getCode(), "玩家可上阵战机已满, roleId:", roleId,
                        ", heroId:", heroId);
            }
        } else if (type == PlaneConstant.PLANE_SWAP_TYPE_DOWN) { // 下阵
            if (hero.downPlane(planeId)) {
                plane.downBattle(hero);
            }
        } else {
            throw new MwException(GameError.SWAP_PLANE_ERROR.getCode(), "战机替换出错, 战机信息有误, type:", type);
        }

        ChangeInfo change = ChangeInfo.newIns();

        // 重新计算并更新将领属性
        CalculateUtil.processAttr(player, hero);
        if (techDataManager.isOpen(player, TechConstant.TYPE_19) && player.common.getAutoArmy() == 0) {
            // 研究自动补兵,并且关闭了自动补兵:不进行补兵
        } else {
            // 上阵将领自动补兵
            armyService.autoAddArmySingle(player, hero);
        }
        change.addChangeType(AwardType.ARMY, hero.getType());
        change.addChangeType(AwardType.HERO_ARM, hero.getHeroId());
        // 通知客户端玩家资源改变
        rewardDataManager.syncRoleResChanged(player, change);

        // 重新计算战斗力
        CalculateUtil.reCalcFight(player);

        GamePb1.PlaneSwapRs.Builder builder = GamePb1.PlaneSwapRs.newBuilder();
        builder.setUpPlane(warPlaneDataManager.createWarPlanePb(plane, player));
        if (swap) {
            builder.setDownPlane(warPlaneDataManager.createWarPlanePb(swapPlane, player));
        }
        return builder.build();
    }

    /**
     * 战机改造
     *
     * @param roleId
     * @param planeId 要改造的战机Id
     */
    public GamePb1.PlaneRemouldRs planeRemould(long roleId, int planeId) throws MwException {

        // 检查角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // 检测战机是否拥有
        WarPlane plane = player.checkWarPlaneIsExist(planeId);
        if (CheckNull.isNull(plane)) {
            throw new MwException(GameError.WAR_PLANE_NOT_FOUND.getCode(), "玩家没有这个战机, roleId:", roleId, ", planeId:",
                    planeId);
        }

        StaticPlaneUpgrade oldPlane = StaticWarPlaneDataMgr.getPlaneUpgradeById(planeId);
        if (CheckNull.isNull(oldPlane)) {
            throw new MwException(GameError.PLANE_CONFIG_NOT_FOUND.getCode(), "检测战机的时候, 没有找到战机的配置, planeId:", planeId);
        }
        int nextId = oldPlane.getNextId();
        // 新战机
        StaticPlaneUpgrade newPlane = StaticWarPlaneDataMgr.getPlaneUpgradeById(nextId);
        // 如果改造后的战机Quality等于5推送三倍返利活动
        if (oldPlane.getQuality() != PlaneConstant.PLANE_QUALITY_FIVE
                && newPlane.getQuality() == PlaneConstant.PLANE_QUALITY_FIVE) {
            snyThreeRebate(player);
        }
        if (nextId <= 0 || CheckNull.isNull(newPlane)) {
            throw new MwException(GameError.PLANE_NOT_REMOULD.getCode(), "战机已改造到最高品质, 或者没有找到改造后的战机配置, planeId:",
                    planeId, ", nextId:", nextId);
        }
        ChangeInfo change = ChangeInfo.newIns();

        // 扣战机碎片
        List<List<Integer>> reformNeed = oldPlane.getReformNeed();
        for (List<Integer> reform : reformNeed) {
            rewardDataManager.checkAndSubPlayerRes(player, reform.get(0), reform.get(1), reform.get(2),
                    AwardFrom.PLANE_REMOULD_CONSUME, false);
            change.addChangeType(reform.get(0), reform.get(1));
        }

        // 改造
        int heroId = plane.getHeroId();
        if (plane.remould(newPlane) && heroId > 0) { // 战机有将领佩戴, 并且改造成功
            Hero hero = heroService.checkHeroIsExist(player, heroId);
            hero.planeRemould(oldPlane.getPlaneId(), newPlane.getPlaneId());
            // 重新计算将领属性
            CalculateUtil.processAttr(player, hero);
        }

        // 改造时检测战机碎片可否转化
        warPlaneDataManager.checkChipTransform(plane, player, change);
        // 通知玩家消耗的资源类型
        rewardDataManager.syncRoleResChanged(player, change);

        GamePb1.PlaneRemouldRs.Builder builder = GamePb1.PlaneRemouldRs.newBuilder();
        builder.setPlane(warPlaneDataManager.createWarPlanePb(plane, player));
        return builder.build();
    }

    /**
     * 获取空军基地的信息
     *
     * @param roleId
     */
    public GamePb1.PlaneFactoryRs planeFactory(long roleId) throws MwException {

        // 检测角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // 解锁判断
        if (!buildingDataManager.checkBuildingLock(player, BuildingType.AIR_BASE)) {
            throw new MwException(GameError.FUNCTION_LOCK.getCode(),
                    "获取空基地信息时，建筑未解锁, roleId:" + roleId + ",buildingPos:" + BuildingType.AIR_BASE);
        }

        BuildingExt buildingExt = player.buildingExts.get(BuildingType.AIR_BASE);

        GamePb1.PlaneFactoryRs.Builder builder = GamePb1.PlaneFactoryRs.newBuilder();
        builder.setEndTime(buildingExt.getUnLockTime() + PlaneConstant.PLANE_FACTORY_AWARD_TIME.get(0));
        builder.setBeginTime(buildingExt.getUnLockTime());
        GlobalPlane globalPlane = warPlaneDataManager.getGlobalPlane();
        builder.setStatus(PbHelper.createTwoIntPb(player.getMixtureDataById(PlayerConstant.PLANE_FACTORY_MAKE_NUM),
                // 在空军基地开启的7天内高级抽取次数
                PlaneConstant.getPlaneFreeSearchCnt(player))); // 已使用免费次数(当天)
        if (!CheckNull.isNull(globalPlane.getLastChat())) {
            builder.setChat(globalPlane.getLastChat().ser());
        }
        builder.setCdTime(PlaneConstant.getLastPlaneFreeSearchCDTime(player));
        builder.setSearchAward(player.getMixtureDataById(PlayerConstant.PLANE_FACTORY_SEARCH_AWARD));
        builder.setSuperCdTime(PlaneConstant.getlastplaneSuperFreeSearchCDTime(player));
        return builder.build();
    }

    /**
     * 战机寻访
     *
     * @param roleId
     * @param req
     * @throws MwException
     */
    public GamePb1.SearchPlaneRs searchPlane(long roleId, GamePb1.SearchPlaneRq req) throws MwException {

        // 检测角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int searchType = req.getSearchType();
        int countType = req.getCountType();
        int costType = req.getCostType();
        // 检查参数是否正确
        if (!warPlaneDataManager.realSearchType(searchType) || !warPlaneDataManager.realCountType(countType)
                || !warPlaneDataManager.realSearchCostType(costType)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "战机寻访的时候, 参数错误, roleId:", roleId, ", searchType:",
                    searchType, ", countType:", countType, ", costType:", costType);
        }

        // 当天战机寻访记录信息
        int useFreeCnt = PlaneConstant.getPlaneFreeSearchCnt(player);
        int lastFreeTime = PlaneConstant.getLastPlaneFreeSearchCDTime(player);
        int lastSuperFreeTime = PlaneConstant.getlastplaneSuperFreeSearchCDTime(player);

        // 寻访方式校验
        if (searchType == PlaneConstant.SEARCH_TYPE_NORMAL && costType == PlaneConstant.SEARCH_COST_GOLD) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "低级寻访，无金币寻访, roleId:", player.roleId,
                    ", searchType:", searchType, ", countType:", countType, ", costType:", costType);
        }
        if ((searchType == PlaneConstant.SEARCH_TYPE_SUPER || searchType == PlaneConstant.SEARCH_TYPE_LIMIT)
                && countType == PlaneConstant.COUNT_TYPE_TEN && costType == PlaneConstant.SEARCH_COST_FREE) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "高级寻访或者限时寻访，无多次免费寻访, roleId:", player.roleId,
                    ", searchType:", searchType, ", countType:", countType, ", costType:", costType);
        }
        StaticSpecialPlan sPlan = StaticActivityDataMgr.getSpecialPlans().stream()
                .filter(plan -> plan.getActivityType() == ActivityConst.SPECIAL_ACT_PLANE_LIMIT_SEARCH).findFirst()
                .orElse(null);
        if (searchType == PlaneConstant.SEARCH_TYPE_LIMIT && CheckNull.isNull(sPlan)) {
            throw new MwException(GameError.PLANE_SEARCH_PARAM_ERROR.getCode(), "限时寻访的时候，已过活动时间, roleId:", player.roleId,
                    ", searchType:", searchType, ", countType:", countType, ", costType:", costType);
        }

        int gold = 0;// 如果金币寻访，记录需要消耗的金币数
        if (costType == PlaneConstant.SEARCH_COST_FREE) {// 如果是使用免费次数低级寻访，检查相关数据
            if (countType != PlaneConstant.COUNT_TYPE_ONE) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), "寻访，免费寻访只能单次寻访, roleId:", player.roleId,
                        ", searchType:", searchType, ", countType:", countType, ", costType:", costType);
            }
            if (searchType == PlaneConstant.SEARCH_TYPE_NORMAL && useFreeCnt >= PlaneConstant.PLANE_SEARCH_FREE_NUM
                    .get(0).get(1)) { // 当天免费寻访次数已用尽
                throw new MwException(GameError.PARAM_ERROR.getCode(), "寻访，免费寻访已达上限, roleId:", player.roleId,
                        ", searchType:", searchType, ", countType:", countType, ", costType:", costType,
                        ", useFreeCnt:", useFreeCnt);
            }
        } else if (costType == PlaneConstant.SEARCH_COST_GOLD) {
            gold = PlaneConstant.getHeroSearchGoldByType(countType);
            if (gold < 0) {
                throw new MwException(GameError.NO_CONFIG.getCode(), "寻访金币消耗未配置, roleId:", player.roleId,
                        ", searchType:", searchType, ", countType:", countType);
            }
        }

        int count = 0;// 记录本次准备寻访次数
        if (countType == PlaneConstant.COUNT_TYPE_ONE) {// 寻访1次
            count = 1;
        } else if (countType == PlaneConstant.COUNT_TYPE_TEN) {// 寻访10次
            count = 10;
        }
        // gold *= count; 已经算好了
        ChangeInfo change = ChangeInfo.newIns();// 记录玩家资源变更类型

        if (costType == PlaneConstant.SEARCH_COST_FREE) {
            int now = TimeHelper.getCurrentSecond();
            if (searchType == PlaneConstant.SEARCH_TYPE_NORMAL) { // 低级免费
                if (lastFreeTime > now) {
                    throw new MwException(GameError.PLANE_FREE_SEARCH_CD.getCode(), "低级寻访免费次数CD中, roleId:",
                            player.roleId, ", cdTime:", lastFreeTime, ", now:", now);
                }
                // 更新免费时间, 扣除免费次数
                player.setMixtureData(PlayerConstant.PLANE_LAST_FREE_SEARCH_TIME, now);
                player.setMixtureData(PlayerConstant.PLANE_FREE_SEARCH_CNT, useFreeCnt + 1);
            } else if (searchType == PlaneConstant.SEARCH_TYPE_SUPER) { // 高级免费
                if (lastSuperFreeTime > now) {
                    throw new MwException(GameError.PLANE_FREE_SEARCH_CD.getCode(), "高级寻访免费次数CD中, roleId:",
                            player.roleId, ", cdTime:", lastSuperFreeTime, ", now:", now);
                }
                // 更新免费时间
                player.setMixtureData(PlayerConstant.PLANE_SUPER_FREE_SEARCH_TIME, now);
            }
        } else if (costType == PlaneConstant.SEARCH_COST_PROP) { // 抽取卷
            List<Integer> cost;
            if (countType == PlaneConstant.COUNT_TYPE_ONE) {
                cost = PlaneConstant.getHeroSearchScrollByType(searchType);
                if (CheckNull.isEmpty(cost)) {
                    throw new MwException(GameError.NO_CONFIG.getCode(), "寻访道具消耗未配置, roleId:", player.roleId,
                            ", searchType:", searchType, ", countType:", countType);
                }
            } else {
                // 这里写死的十连抽的道具id
                cost = Arrays.asList(AwardType.PROP, PropConstant.WAR_PLANE_SEARCH_TEN, 1);
            }
            rewardDataManager.checkAndSubPlayerRes(player, cost.get(0), cost.get(1), cost.get(2),
                    AwardFrom.PLANE_SEARCH_CONSUME, false);
            change.addChangeType(AwardType.PROP, cost.get(1));
        } else if (costType == PlaneConstant.SEARCH_COST_GOLD) { // 金币
            if (searchType == PlaneConstant.SEARCH_TYPE_SUPER || searchType == PlaneConstant.SEARCH_TYPE_LIMIT) {
                rewardDataManager.checkAndSubPlayerRes(player, AwardType.MONEY, AwardType.Money.GOLD, gold,
                        AwardFrom.PLANE_SEARCH_CONSUME, false);
                change.addChangeType(AwardType.MONEY, AwardType.Money.GOLD);
            }
        }

        // 更新高级寻访抽取次数
        if (searchType == PlaneConstant.SEARCH_TYPE_SUPER && !warPlaneDataManager.isAfterSearchAwardTime(player)) {
            player.setMixtureData(PlayerConstant.PLANE_FACTORY_MAKE_NUM,
                    player.getMixtureDataById(PlayerConstant.PLANE_FACTORY_MAKE_NUM) + count);
        }

        // 通知玩家消耗的资源类型
        rewardDataManager.syncRoleResChanged(player, change);
        GamePb1.SearchPlaneRs.Builder builder = GamePb1.SearchPlaneRs.newBuilder();
        for (int i = 0; i < count; i++) {// 执行战机寻访逻辑
            int status = player.getMixtureDataById(PlayerConstant.PLANE_FACTORY_SEARCH_FIRST_COUNT);
            List<CommonPb.Award> awards;
            if (status == PlaneConstant.PLANE_SEARCH_FIRST) {
                ArrayList<CommonPb.Award> commonPbAwards = new ArrayList<>();
                commonPbAwards.add(rewardDataManager
                        .addAwardSignle(player, PlaneConstant.PLANE_FIRST_SEARCH_AWARD.get(0),
                                AwardFrom.PLANE_SEARCH_AWARD));
                awards = commonPbAwards;
                player.setMixtureData(PlayerConstant.PLANE_FACTORY_SEARCH_FIRST_COUNT,
                        PlaneConstant.PLANE_SEARCH_FIRST + 1);
            } else {
                awards = warPlaneDataManager.doSearchPlane(player, searchType);
            }
            if (!CheckNull.isEmpty(awards)) {
                builder.addAllAward(awards);
            }
        }
        // 每次抽奖都有的抽取奖励
        if (count > 0) {
            List<Integer> awards = PlaneConstant.getSearchlotteryAwardByType(searchType);
            if (!CheckNull.isEmpty(awards)) {
                builder.setLotteryAward(
                        rewardDataManager.addAwardSignle(player, awards, count, AwardFrom.PLANE_SEARCH_AWARD));
            }
        }
        builder.setCount(player.getMixtureDataById(PlayerConstant.PLANE_FACTORY_MAKE_NUM));
        builder.setCdTime(PlaneConstant.getLastPlaneFreeSearchCDTime(player));
        builder.setFreeNum(PlaneConstant.getPlaneFreeSearchCnt(player));

        // 任务更新
        if (searchType == PlaneConstant.SEARCH_TYPE_NORMAL) {
            taskDataManager.updTask(player, TaskType.COND_SEARCH_PLANE_LOW, count);
        }
        else if (searchType == PlaneConstant.SEARCH_TYPE_SUPER) {
            taskDataManager.updTask(player, TaskType.COND_SEARCH_PLANE_HIGH, count);
            // 更新活动进度
            activityDataManager.updActivity(player, ActivityConst.ACT_WAR_PLANE_SEARCH, count, 0, true);
        }
        else if (searchType == PlaneConstant.SEARCH_TYPE_LIMIT) {
            // 更新活动进度
            activityDataManager.updActivity(player, ActivityConst.ACT_WAR_PLANE_SEARCH, count, 0, true);
        }
        return builder.build();
    }

    /**
     * 获取寻访奖励(不判断开启时间)
     *
     * @param roleId
     */
    public GamePb1.GetSearchAwardRs getSearchAward(long roleId) throws MwException {

        // 检测角色是否存在
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        /*// 寻访时间已经结束了
        if (warPlaneDataManager.isAfterSearchAwardTime(player)) {
            throw new MwException(GameError.PLANE_AWARD_TIME_END.getCode(), "战机寻访奖励时间已经结束, roleId:", roleId);
        }*/

        int makeNum = player.getMixtureDataById(PlayerConstant.PLANE_FACTORY_MAKE_NUM);
        if (makeNum < PlaneConstant.PLANE_FACTORY_AWARD_TIME.get(1)) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "战机寻访奖励条件未达到, roleId:", roleId, ", num:", makeNum);
        }

        int searchAward = player.getMixtureDataById(PlayerConstant.PLANE_FACTORY_SEARCH_AWARD);
        if (searchAward != 0) { // 已领取
            throw new MwException(GameError.PLANE_SEARCH_AWARD_EXIST.getCode(), "战机寻访奖励已领取, roleId:", roleId);
        }

        List<CommonPb.Award> awards = warPlaneDataManager.doSearchPlane(player, PlaneConstant.SEARCH_TYPE_AWARD);
        player.setMixtureData(PlayerConstant.PLANE_FACTORY_SEARCH_AWARD, 1); // 更新寻访领取状态
        GamePb1.GetSearchAwardRs.Builder builder = GamePb1.GetSearchAwardRs.newBuilder();
        if (!CheckNull.isEmpty(awards)) {
            CommonPb.Award award = awards.get(0);
            builder.setAward(award);
        }
        // 获得新战机
        return builder.build();
    }

    /**
     * 合成战机
     *
     * @param roleId
     * @param planeType 战机类型
     */
    public GamePb1.SyntheticPlaneRs syntheticPlane(long roleId, int planeType) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        if (player.warPlanes.containsKey(planeType)) {
            throw new MwException(GameError.PLANE_TYPE_EXIST.getCode(), "合成战机的时候, 已拥有相同类型的战机, roleId:", roleId,
                    ", planeType:", planeType);
        }

        StaticPlaneInit planeInit = StaticWarPlaneDataMgr.getPlaneInitByType(planeType);
        if (CheckNull.isNull(planeInit)) {
            throw new MwException(GameError.PLANE_CONFIG_NOT_FOUND.getCode(),
                    "战机替换的时候, 没有找到战机的配置, planeType:", planeType);
        }
        rewardDataManager.checkAndSubPlayerRes(player, planeInit.getSynthesis().get(0), planeInit.getSynthesis().get(1),
                planeInit.getSynthesis().get(2), AwardFrom.PLANE_SYNTHETIC_CONSUME, true);

        // 获得新战机
        WarPlane plane = warPlaneDataManager.createPlane(planeInit);
        // 更新活动进度
        activityDataManager.updActivity(player, ActivityConst.ACT_WAR_PLANE_SEARCH, 1, planeInit.getQuality(), true);
        player.warPlanes.put(planeType, plane);
        // 三倍返利活动战机推送 ok
        StaticPlaneUpgrade newPlane = StaticWarPlaneDataMgr.getPlaneUpgradeById(plane.getPlaneId());
        if (newPlane.getQuality() == PlaneConstant.PLANE_QUALITY_FIVE) {
            snyThreeRebate(player);
        }
        GamePb1.SyntheticPlaneRs.Builder builder = GamePb1.SyntheticPlaneRs.newBuilder();
        builder.setPlane(warPlaneDataManager.createWarPlanePb(plane, player));
        return builder.build();
    }

    /**
     * 战机快速升级
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GamePb1.PlaneQuickUpRs planeQuickUp(long roleId, GamePb1.PlaneQuickUpRq req) throws MwException {

        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int planeId = req.getPlaneId();
        int type = req.getType();
        boolean useGold = false;
        if (req.hasUseGold()) {
            useGold = req.getUseGold();
        }

        StaticPlaneUpgrade sPlaneUpgrade = StaticWarPlaneDataMgr.getPlaneUpgradeById(planeId);
        if (CheckNull.isNull(sPlaneUpgrade)) {
            throw new MwException(GameError.PLANE_CONFIG_NOT_FOUND.getCode(), "检测战机的时候, 没有找到战机的配置, planeId:", planeId);
        }

        int propId = PlaneConstant.getPlaneExpPropByType(type);
        if (propId <= 0) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "战机升级道具与type没对应, roleId:", player.roleId, ", planeId:",
                    planeId, ", type:", type, ", propId:", propId);
        }
        StaticProp prop = StaticPropDataMgr.getPropMap(propId);
        if (null == prop) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "战机升级道具未配置, roleId:", player.roleId, ", planeId:",
                    planeId, ", type:", type, ", propId:", propId);
        }

        WarPlane plane = player.checkWarPlaneIsExist(planeId);
        if (CheckNull.isNull(plane)) {
            throw new MwException(GameError.WAR_PLANE_NOT_FOUND.getCode(), "玩家没有这个战机, roleId:", roleId, ", planeId:",
                    planeId);
        }

        int preLv = plane.getLevel();
        int roleLv = player.lord.getLevel();
        if (preLv >= PlaneConstant.PLANE_LEVEL_MAX) {
            throw new MwException(GameError.PLANE_LEVEL_MAX.getCode(), "战机升级时, 战机已到最大等级, roleId:", roleId, ", planeId:",
                    planeId, ", preLv:", preLv);
        }
        if (preLv >= roleLv) {
            throw new MwException(GameError.PLANE_LEVEL_UP_MAX.getCode(), "战机升级时, 战机等级无法超过指挥官等级, roleId:", roleId,
                    ", planeId:", planeId, ", preLv:", preLv, ", roleLv:", roleLv);
        }

        if (useGold) {
            int needGold = prop.getPrice();
            // 检查玩家金币是否足够
            rewardDataManager.checkMoneyIsEnough(player, AwardType.Money.GOLD, needGold, "战机金币升级");
            // 扣除相关金币
            rewardDataManager.subGold(player, needGold, AwardFrom.PLANE_QUICK_UP_CONSUME);
        } else {
            // 检查玩家是否有相关道具，一次只使用一个
            rewardDataManager.checkPropIsEnough(player, propId, 1, "战机快速升级");
            // 扣除道具
            rewardDataManager.subProp(player, propId, 1, AwardFrom.PLANE_QUICK_UP_CONSUME);
        }

        int maxLv = PlaneConstant.PLANE_LEVEL_MAX;
        if (roleLv < maxLv) {
            maxLv = roleLv;
        }

        // 将领增加经验
        int addExp = prop.getRewardList().get(0).get(2);
        warPlaneDataManager.addPlaneExp(plane, addExp, maxLv, player, sPlaneUpgrade);

        GamePb1.PlaneQuickUpRs.Builder builder = GamePb1.PlaneQuickUpRs.newBuilder();
        builder.setPlaneId(planeId);
        builder.setLv(plane.getLevel());
        builder.setExp(plane.getExp());
        if (useGold) {
            builder.setGold(player.lord.getGold());
        }
        return builder.build();
    }

    // 三倍返利活动的推送
    private void snyThreeRebate(Player player) {
        Activity activity = activityDataManager.getActivityInfo(player, ActivityConst.ACT_THREE_REBATE);
        if (activity != null) { // 活动结束不推送
            activityDataManager.syncActChange(player, ActivityConst.ACT_THREE_REBATE);
        }
    }
}
