package com.gryphpoem.game.zw.service;

import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticBuildingDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.manager.CampDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.manager.TaskDataManager;
import com.gryphpoem.game.zw.manager.TechDataManager;
import com.gryphpoem.game.zw.manager.WorldDataManager;
import com.gryphpoem.game.zw.pb.GamePb1.AcqHeroSetRs;
import com.gryphpoem.game.zw.pb.GamePb1.CabinetFinishRs;
import com.gryphpoem.game.zw.pb.GamePb1.CabinetLvFinishRs;
import com.gryphpoem.game.zw.pb.GamePb1.ComandoHeroSetRs;
import com.gryphpoem.game.zw.pb.GamePb1.CreateLeadRs;
import com.gryphpoem.game.zw.pb.GamePb1.GetCabinetRs;
import com.gryphpoem.game.zw.resource.constant.AwardFrom;
import com.gryphpoem.game.zw.resource.constant.AwardType;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.HeroConstant;
import com.gryphpoem.game.zw.resource.constant.TaskType;
import com.gryphpoem.game.zw.resource.constant.TechConstant;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Cabinet;
import com.gryphpoem.game.zw.resource.domain.s.StaticCabinetLv;
import com.gryphpoem.game.zw.resource.domain.s.StaticCabinetPlan;
import com.gryphpoem.game.zw.resource.domain.s.StaticHero;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.WarPlane;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.party.Camp;
import com.gryphpoem.game.zw.resource.util.CalculateUtil;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author QiuKun
 * @ClassName WarFactoryService.java
 * @Description 西点军校(内阁)
 * @date 2017年7月14日
 */
@Service public class WarFactoryService {

    @Autowired private PlayerDataManager playerDataManager;

    @Autowired private HeroService heroService;

    @Autowired private RewardDataManager rewardDataManager;

    @Autowired private CampDataManager campDataManager;

    @Autowired private WorldDataManager worldDataManager;

    @Autowired private TechDataManager techDataManager;

    @Autowired private ArmyService armyService;

    @Autowired private TaskDataManager taskDataManager;

    public ComandoHeroSetRs commandoHeroSet(long roleId, int pos, int heroId, int type, boolean swap, boolean swapPlane) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (player.building.getWarFactory() < Constant.CABINET_CONDITION.get(3)) {
            // 内阁等级小于1级禁止开放
            throw new MwException(GameError.WAR_FACTORY_LV_NOT_ENOUGH.getCode(), "内阁 等级不够");
        }
        // 检查pos位是否正常
        if (pos < HeroConstant.HERO_BATTLE_1 || pos > Constant.COMMANDO_HERO_REQUIRE.size()) {
            throw new MwException(GameError.HERO_BATTLE_POS_ERROR.getCode(), "内阁特攻将领上阵队列位置不正确, roleId:", roleId,
                    ", pos:", pos);
        }

        // 检测配置是否正确
        List<Integer> lvRequire = Constant.COMMANDO_HERO_REQUIRE;
        if (CheckNull.isEmpty(lvRequire) || lvRequire.size() != 2) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "内阁特攻将领上阵队列位置不正确, roleId:", roleId, ", pos:", pos);
        }
        int lv = player.lord.getLevel();
        // 检测等级是否满足
        if ((pos == HeroConstant.HERO_BATTLE_1 && lv < lvRequire.get(0)) || (pos == HeroConstant.HERO_BATTLE_2
                && lv < lvRequire.get(1))) {
            throw new MwException(GameError.WAR_FACTORY_HERO_POS_NEED.getCode(), "未开启,内阁特攻将领布置等级不够 roleId:", roleId,
                    ", pos:", pos);
        }

        // 将领是否存在
        ChangeInfo change = ChangeInfo.newIns();
        ComandoHeroSetRs.Builder builder = ComandoHeroSetRs.newBuilder();
        if (1 == type) {// 上阵
            Hero hero = heroService.checkHeroIsExist(player, heroId);
            // 判断该将领是否在武将上阵
            if (player.isOnBattleHero(heroId) || player.isOnWallHero(heroId) || player.isOnAcqHero(heroId) || player.isOnCommandoHero(heroId)) {
                throw new MwException(GameError.HERO_BATTLE_REPEAT.getCode(), "是武将将领已上阵, roleId:", roleId, ", heroId:",
                        heroId);
            }

            Hero downHero = player.getCommandoHeroByPos(pos);
            if (null != downHero) {
                if (!downHero.isIdle()) {
                    throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "将领不是空闲状态不能操作  roleId:", roleId, " state:",
                            downHero.getState());
                }
                // 位置上已有其他将领存在，现将该将领下阵
                if (swap) {// 如果需要交换装备，执行交换装备的逻辑
                    // rewardDataManager.checkBagCnt(player);
                    heroService.swapHeroEquip(player, hero, downHero);
                }
                if (swapPlane) {// 如果需要交换战机, 执行交换战机的逻辑
                    heroService.swapHeroPlanes(player, hero, downHero, pos);
                }
                downHero.onCommando(0);
                // 士兵回营
                int sub = downHero.getCount();

                downHero.setCount(0);
                StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(downHero.getHeroId());
//                if (Objects.nonNull(staticHero)) {
                    // 获取武将对应类型的兵力
//                    int armType = staticHero.getType();
                    // LogLordHelper.heroArm(AwardFrom.HERO_DOWN, player.account, player.lord, heroId, downHero.getCount(), -sub, staticHero.getType(), Constant.ACTION_ADD);

                    // 上报玩家兵力变化
//                    LogLordHelper.playerArm(
//                            AwardFrom.HERO_DOWN,
//                            player,
//                            armType,
//                            Constant.ACTION_ADD,
//                            -sub,
//                            playerDataManager.getArmCount(player.resource, armType)
//                    );
//                }

                rewardDataManager.modifyArmyResource(player, staticHero.getType(), sub, 0, AwardFrom.HERO_DOWN);

                change.addChangeType(AwardType.ARMY, staticHero.getType());
                change.addChangeType(AwardType.HERO_ARM, downHero.getHeroId());
                // 记录返回下阵将领
                builder.setDownHero(PbHelper.createHeroPb(downHero, player));

                // 重新计算并更新将领属性
                CalculateUtil.processAttr(player, downHero);
            }

            // 特攻将领上阵
            hero.onCommando(pos);
            player.heroCommando[pos] = heroId;// 更新已上阵将领队列信息
            if (!CheckNull.isNull(hero) && !CheckNull.isEmpty(hero.getWarPlanes())) {
                for (int planeId : hero.getWarPlanes()) {
                    WarPlane plane = player.checkWarPlaneIsExist(planeId);
                    if (!CheckNull.isNull(plane)) {
                        plane.setPos(pos);
                    }
                }
            }

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

            builder.setUpHero(PbHelper.createHeroPb(hero, player));
        } else {
            int myPos = 0;
            for (int i = 1; i < player.heroCommando.length; i++) {
                if (player.heroCommando[i] == heroId) {
                    myPos = i;
                    break;
                }
            }
            // 下阵
            Hero downHero = player.getCommandoHeroByPos(myPos);
            if (downHero != null) {
                if (!downHero.isIdle()) {
                    throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "将领不是空闲状态不能操作 roleId:", roleId, " state:",
                            downHero.getState(), ", heroId:", downHero.getHeroId());
                }
                downHero.onCommando(0);

                // 下阵将领, 把战机也下阵
                heroService.downHeroAllPlane(player, downHero);
                // 士兵回营
                int sub = downHero.getCount();
                downHero.setCount(0);
                StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(downHero.getHeroId());
//                if (Objects.nonNull(staticHero)) {
//                    // 获取武将对应类型的兵力
//                    int armType = staticHero.getType();
//                    // LogLordHelper.heroArm(AwardFrom.HERO_DOWN, player.account, player.lord, heroId, downHero.getCount(), -sub, staticHero.getType(), Constant.ACTION_ADD);
//
//                    // 上报玩家兵力变化
//                    LogLordHelper.playerArm(
//                            AwardFrom.HERO_DOWN,
//                            player,
//                            armType,
//                            Constant.ACTION_ADD,
//                            -sub,
//                            playerDataManager.getArmCount(player.resource, armType));
//                }

                rewardDataManager.modifyArmyResource(player, staticHero.getType(), sub, 0, AwardFrom.HERO_DOWN);

                change.addChangeType(AwardType.ARMY, staticHero.getType());
                change.addChangeType(AwardType.HERO_ARM, downHero.getHeroId());
                // 记录返回下阵将领
                builder.setDownHero(PbHelper.createHeroPb(downHero, player));
                // 重新调整
                player.heroCommando[myPos] = 0;// 下阵的位置清0
                // 重新计算并更新将领属性
                CalculateUtil.processAttr(player, downHero);
            }
        }
        // 通知客户端玩家资源改变
        if (change != null)
            rewardDataManager.syncRoleResChanged(player, change);

        for (int i = 1; i < player.heroCommando.length; i++) {
            if (player.heroCommando[i] != 0)
                builder.addHeroIds(player.heroCommando[i]);
        }
        return builder.build();
    }

    /**
     * 内阁采集将领布置
     *
     * @param roleId
     * @param pos    位置
     * @param heroId
     * @param type   1上阵 2下阵
     * @return
     * @throws MwException
     */
    public AcqHeroSetRs acqHeroSet(long roleId, int pos, int heroId, int type, boolean swap, boolean swapTreasure, boolean swapMedal) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (player.building.getWarFactory() < Constant.CABINET_CONDITION.get(0)) {
            // 内阁等级小于1级禁止开放
            throw new MwException(GameError.WAR_FACTORY_LV_NOT_ENOUGH.getCode(), "内阁 等级不够");
        }
        // 检查pos位是否正常
        if (pos < HeroConstant.HERO_BATTLE_1 || pos > HeroConstant.HERO_BATTLE_4) {
            throw new MwException(GameError.HERO_BATTLE_POS_ERROR.getCode(), "内阁采集将领上阵队列位置不正确, roleId:", roleId,
                    ", pos:", pos);
        }

        // 检测配置是否正确
        List<Integer> lvRequire = Constant.ACQ_HERO_REQUIRE;
        if (CheckNull.isEmpty(lvRequire) || lvRequire.size() != 4) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "内阁采集将领上阵队列位置不正确, roleId:", roleId, ", pos:", pos);
        }
        int lv = player.lord.getLevel();
        // 检测等级是否满足
        if (pos == HeroConstant.HERO_BATTLE_1) {
            if (lv < lvRequire.get(0)) {
                throw new MwException(GameError.WAR_FACTORY_HERO_POS_NEED.getCode(), "未开启,内阁采集将领布置等级不够 roleId:", roleId,
                        ", pos:", pos);
            }
        } else if (pos == HeroConstant.HERO_BATTLE_2) {
            if (lv < lvRequire.get(1)) {
                throw new MwException(GameError.WAR_FACTORY_HERO_POS_NEED.getCode(), "未开启,内阁采集将领布置等级不够 roleId:", roleId,
                        ", pos:", pos);
            }
        } else if (pos == HeroConstant.HERO_BATTLE_3) {
            if (lv < lvRequire.get(2)) {
                throw new MwException(GameError.WAR_FACTORY_HERO_POS_NEED.getCode(), "未开启,内阁采集将领布置等级不够 roleId:", roleId,
                        ", pos:", pos);
            }
        } else if (pos == HeroConstant.HERO_BATTLE_4) {
            if (lv < lvRequire.get(3)) {
                throw new MwException(GameError.WAR_FACTORY_HERO_POS_NEED.getCode(), "未开启,内阁采集将领布置等级不够 roleId:", roleId,
                        ", pos:", pos);
            }
        }

        // 将领是否存在
        ChangeInfo change = null;
        AcqHeroSetRs.Builder builder = AcqHeroSetRs.newBuilder();
        boolean sysClientUpdateMedal = false;
        if (1 == type) {// 上阵
            Hero hero = heroService.checkHeroIsExist(player, heroId);
            // 判断该将领是否在武将上阵
            if (player.isOnBattleHero(heroId) || player.isOnWallHero(heroId) || player.isOnAcqHero(heroId) || player.isOnCommandoHero(heroId)) {
                throw new MwException(GameError.HERO_BATTLE_REPEAT.getCode(), "是武将将领已上阵, roleId:", roleId, ", heroId:",
                        heroId);
            }

            Hero downHero = player.getAcqHeroByPos(pos);
            if (null != downHero) {
                if (!downHero.isIdle()) {
                    throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "将领不是空闲状态不能操作  roleId:", roleId, " state:",
                            downHero.getState());
                }
                // 位置上已有其他将领存在，现将该将领下阵
                if (swap) {// 如果需要交换装备，执行交换装备的逻辑
                    // rewardDataManager.checkBagCnt(player);
                    heroService.swapHeroEquip(player, hero, downHero);
                }
                if (swapTreasure) {// 如果需要交换宝具，执行交换宝具的逻辑
                    heroService.swapHeroTreasure(player, downHero, hero);
                }
                if (swapMedal) {// 如果需要交换兵书，执行交换兵书的逻辑
                    heroService.swapHeroMedal(player, downHero, hero);
                    sysClientUpdateMedal = true;
                }
                change = downAcqHeroAndBackRes(player, downHero);
                // 记录返回下阵将领
                builder.setDownHero(PbHelper.createHeroPb(downHero, player));

                // 重新计算并更新将领属性
                CalculateUtil.processAttr(player, downHero);
            }

            // 采集将领上阵
            hero.onAcq(pos);
            player.heroAcq[pos] = heroId;// 更新已上阵将领队列信息
            // 重新调整位置
            reAdjustHeroPos(player.heroAcq, player.heros);
            // 重新计算并更新将领属性
            CalculateUtil.processAttr(player, hero);
            taskDataManager.updTask(player,TaskType.COND_510,1,hero.getQuality());

            if (techDataManager.isOpen(player, TechConstant.TYPE_19) && player.common.getAutoArmy() == 0) {
                // 研究自动补兵,并且关闭了自动补兵:不进行补兵
            } else {
                // 上阵将领自动补兵
                armyService.autoAddArmySingle(player, hero);
            }
            change = ChangeInfo.newIns();
            change.addChangeType(AwardType.ARMY, hero.getType());
            change.addChangeType(AwardType.HERO_ARM, hero.getHeroId());

            builder.setUpHero(PbHelper.createHeroPb(hero, player));
        } else {
            int myPos = 0;
            for (int i = 1; i < player.heroAcq.length; i++) {
                if (player.heroAcq[i] == heroId) {
                    myPos = i;
                    break;
                }
            }
            // 下阵
            Hero downHero = player.getAcqHeroByPos(myPos);
            if (downHero != null) {
                if (!downHero.isIdle()) {
                    throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "将领不是空闲状态不能操作 roleId:", roleId, " state:",
                            downHero.getState(), ", heroId:", downHero.getHeroId());
                }
                // 士兵回营
                change = downAcqHeroAndBackRes(player, downHero);
                // 重新调整
                player.heroAcq[myPos] = 0;// 下阵的位置清0
                reAdjustHeroPos(player.heroAcq, player.heros);
                // 重新计算并更新将领属性
                CalculateUtil.processAttr(player, downHero);
                // 记录返回下阵将领
                builder.setDownHero(PbHelper.createHeroPb(downHero, player));
            }
        }
        // 通知客户端玩家资源改变
        if (change != null)
            rewardDataManager.syncRoleResChanged(player, change);

        for (int i = 1; i < player.heroAcq.length; i++) {
            if (player.heroAcq[i] != 0)
                builder.addHeroIds(player.heroAcq[i]);
        }

        builder.setUpdateMedal(sysClientUpdateMedal);
        return builder.build();
    }

    /**
     * 下将领并返还兵数
     *
     * @param player
     * @param downHero
     * @return
     */
    private ChangeInfo downAcqHeroAndBackRes(Player player, Hero downHero) {
        downHero.onAcq(0); // 将领下阵，pos设置为0
        // 士兵回营
        int sub = downHero.getCount();
        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(downHero.getHeroId());
        int armyType = staticHero.getType();
        downHero.setCount(0);
        rewardDataManager.modifyArmyResource(player, armyType, sub, 0, AwardFrom.HERO_DOWN);
        //记录玩家兵力变化信息
        // LogLordHelper.filterHeroArm(AwardFrom.HERO_DOWN, player.account, player.lord, downHero.getHeroId(), downHero.getCount(), -sub,
        //         Constant.ACTION_SUB, armyType, downHero.getQuality());

        // 上报玩家兵力变化
//        LogLordHelper.playerArm(
//                AwardFrom.HERO_DOWN,
//                player,
//                armyType,
//                Constant.ACTION_SUB,
//                -sub,
//                playerDataManager.getArmCount(player.resource, armyType)
//        );

        ChangeInfo change = ChangeInfo.newIns();
        change.addChangeType(AwardType.ARMY, staticHero.getType());
        change.addChangeType(AwardType.HERO_ARM, downHero.getHeroId());
        return change;
    }

    /**
     * 重新调整位置
     * @param heroIds 将领ids
     * @param heros   将领
     */
    public void reAdjustHeroPos(int[] heroIds, Map<Integer, Hero> heros) {
        // 重新调整位置
        List<Integer> heroList = new ArrayList<>();
        for (int i = 1; i < heroIds.length; i++) {
            int hid = heroIds[i];
            if (heros.get(hid) != null) {
                heroList.add(hid);
            }
        }
        for (int i = 0; i < heroIds.length - 1; i++) {
            int pos = i + 1;
            if (i < heroList.size()) {
                int hId = heroList.get(i);
                heroIds[pos] = hId;
                heros.get(hId).onAcq(pos);
            } else {
                // 尾部 清空
                heroIds[pos] = 0;
            }
        }
    }

    /**
     * 获取天策府数据
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GetCabinetRs getCabinetInfo(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // 检测是否满足开启天策府
        if (player.building.getWarFactory() < Constant.CABINET_CONDITION.get(2)) {
            // 内阁等级小于3级禁止开发
            throw new MwException(GameError.WAR_FACTORY_LV_NOT_ENOUGH.getCode(), "内阁 等级不够  roleId:", roleId, " 等级:",
                    player.building.getWarFactory());
        }
        if (null == player.cabinet) {
            // 进行初始化
            player.cabinet = new Cabinet();
        }
        // 获取点兵统领的级别 和经验
        Camp camp = campDataManager.getParty(player.lord.getCamp());
        int leadLv = camp.getCabinetLeadLv();
        long leadExp = camp.getCabinetLeadExp();
        GetCabinetRs.Builder builder = GetCabinetRs.newBuilder();
        builder.setLeadLv(leadLv);
        builder.setLeadExp(leadExp);
        builder.setPlanId(player.cabinet.getCurPlanId());
        builder.setLeadStep(player.cabinet.getLeadStep());
        builder.setIsCreateLead(player.cabinet.isCreateLead());
        builder.setIsFinish(player.cabinet.isFinsh());
        builder.setIsLvFinish(player.cabinet.isLvFinish());
        // 查看点兵统领坐标
        if (player.cabinet.isCreateLead()) {
            StaticCabinetPlan staticCabinetPlan = StaticBuildingDataMgr
                    .getCabinetPlanById(player.cabinet.getCurPlanId());
            if (staticCabinetPlan != null && player.cabinet.getLeadStep() < staticCabinetPlan.getNpcCount()) {
                // 查点兵统领坐标
                List<Integer> posList = worldDataManager.getCabinetLeadMap().values().stream()
                        .filter(c -> c.getRoleId() == roleId).map(c -> c.getPos()).collect(Collectors.toList());
                if (!CheckNull.isEmpty(posList)) {
                    builder.addAllCabinetLeadPos(posList);
                }
            }
        }
        return builder.build();
    }

    /**
     * 完成点兵第一阶段
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public CabinetLvFinishRs cabinetLvFinish(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // 检测是否满足开启天策府
        if (player.building.getWarFactory() < Constant.CABINET_CONDITION.get(2)) {
            // 内阁等级小于3级禁止开放
            throw new MwException(GameError.WAR_FACTORY_LV_NOT_ENOUGH.getCode(), "内阁 等级不够 roleId:", roleId, " 等级:",
                    player.building.getWarFactory());
        }
        if (null == player.cabinet) {
            throw new MwException(GameError.CABINET_PLAN_NOT_INIT.getCode(), "内阁天策府未初始化数据 roleId:", roleId);
        }
        // 检测条件等级条件
        Camp camp = campDataManager.getParty(player.lord.getCamp());
        int leadLv = camp.getCabinetLeadLv();
        long leadExp = camp.getCabinetLeadExp();
        // StaticCabinetLv cabinetLvByLv = StaticBuildingDataMgr.getCabinetLvByLv(leadLv);
        // if (null == cabinetLvByLv) {
        // throw new MwException(GameError.CABINET_CONFING_ERR.getCode(), "内阁天策府等级配置出错");
        // }
        // Map<Integer, Integer> lvMap = cabinetLvByLv.getLineLv();
        // if (CheckNull.isEmpty(lvMap)) {
        // throw new MwException(GameError.CABINET_CONFING_ERR.getCode(), "内阁天策府等级配置出错");
        // }
        int curPlanId = player.cabinet.getCurPlanId();
        // Integer needPlayerLv = lvMap.get(curPlanId);
        // if (null == needPlayerLv) {
        // throw new MwException(GameError.CABINET_CONFING_ERR.getCode(), "内阁天策府等级配置出错");
        // }
        // int playerLv = player.lord.getLevel();
        // if (playerLv < needPlayerLv) {
        // throw new MwException(GameError.LV_NOT_ENOUGH.getCode(), "内阁天策完成等级任务,玩家等级不够");
        // }

        // 检测点兵统领数量是否满足
        StaticCabinetPlan staticCabinetPlan = StaticBuildingDataMgr.getCabinetPlanById(curPlanId);
        if (staticCabinetPlan == null) {
            throw new MwException(GameError.CABINET_CONFING_ERR.getCode(), "内阁天策府配置出错 roleId:", roleId);
        }
        if (player.cabinet.getLeadStep() < staticCabinetPlan.getNpcCount()) {
            throw new MwException(GameError.CABINET_FINISH_NOT_ENOUGH.getCode(), "击杀点兵统领个数不足够 roleId:", roleId,
                    " 个数:" + player.cabinet.getLeadStep());
        }

        // 修改状态
        player.cabinet.setLvFinish(true);

        CabinetLvFinishRs.Builder builder = CabinetLvFinishRs.newBuilder();
        builder.setLeadLv(leadLv);
        builder.setLeadExp(leadExp);
        builder.setPlanId(player.cabinet.getCurPlanId());
        builder.setIsLvFinish(player.cabinet.isLvFinish());
        return builder.build();

    }

    /**
     * 创建点兵统领
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public CreateLeadRs createLead(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        // 检测是否满足开启天策府
        if (null == player.cabinet) {
            throw new MwException(GameError.CABINET_PLAN_NOT_INIT.getCode(), "内阁天策府未初始化数据 roleId:", roleId);
        }
        if (player.lord.getArea() > WorldConstant.AREA_MAX_ID) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "只能在老地图才能创建点兵统领 roleId:", roleId);
        }
        // if (!player.cabinet.isLvFinish()) {
        // throw new MwException(GameError.CABINET_FINISH_NOT_ENOUGH.getCode(), "点兵统领等级任务未完成,不能创建点兵统领");
        // }
        // 已经创建过点兵统领,就不用创建
        if (!player.cabinet.isCreateLead()) {
            // throw new MwException(GameError.CABINET_LEAD_ISCREATED.getCode(), "点兵统领已经创建过");
            int curPlanId = player.cabinet.getCurPlanId();
            StaticCabinetPlan staticCabinetPlan = StaticBuildingDataMgr.getCabinetPlanById(curPlanId);
            // 创建点兵统领
            worldDataManager.refreshCabinetLead(player, staticCabinetPlan);
            player.cabinet.setCreateLead(true);
        }

        // 通知其他玩家地图数据改变
        List<Integer> posList = new ArrayList<>();
        posList.add(player.lord.getPos());
        EventBus.getDefault()
                .post(new Events.AreaChangeNoticeEvent(posList, Events.AreaChangeNoticeEvent.MAP_AND_AREA_TYPE));

        CreateLeadRs.Builder builder = CreateLeadRs.newBuilder();
        Camp camp = campDataManager.getParty(player.lord.getCamp());
        int leadLv = camp.getCabinetLeadLv();
        long leadExp = camp.getCabinetLeadExp();
        builder.setLeadLv(leadLv);
        builder.setLeadExp(leadExp);
        builder.setPlanId(player.cabinet.getCurPlanId());
        builder.setIsCreateLead(player.cabinet.isCreateLead());
        return builder.build();
    }

    /**
     * 完成当前点兵任务
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public CabinetFinishRs cabinetFinish(long roleId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        // 检测是否满足开启天策府
        if (player.building.getWarFactory() < Constant.CABINET_CONDITION.get(2)) {
            // 内阁等级小于3级禁止开放
            throw new MwException(GameError.WAR_FACTORY_LV_NOT_ENOUGH.getCode(), "内阁 等级不够 roleId:", roleId);
        }
        if (null == player.cabinet || 0 == player.cabinet.getCurPlanId()) {
            throw new MwException(GameError.CABINET_PLAN_NOT_INIT.getCode(), "内阁天策府未初始化数据  roleId:", roleId);
        }
        // 检测击杀点兵统领个数是否足够
        Cabinet cabinet = player.cabinet;
        if (cabinet.isFinsh()) {
            throw new MwException(GameError.CABINET_IS_FINISHED.getCode(), "内阁天策府当前点兵任务已经完成过  roleId:", roleId);
        }
        // 点兵第一阶段是否完成
        if (!cabinet.isLvFinish()) {
            throw new MwException(GameError.LV_NOT_ENOUGH.getCode(), "内阁天策府点兵第一阶段未完成   roleId:", roleId);
        }

        // 检测等级是否足够
        Camp camp = campDataManager.getParty(player.lord.getCamp());
        int leadLv = camp.getCabinetLeadLv();
        long leadExp = camp.getCabinetLeadExp();
        StaticCabinetLv cabinetLvByLv = StaticBuildingDataMgr.getCabinetLvByLv(leadLv);
        if (null == cabinetLvByLv) {
            throw new MwException(GameError.CABINET_CONFING_ERR.getCode(), "内阁天策府等级配置出错  roleId:", roleId);
        }
        Map<Integer, Integer> lvMap = cabinetLvByLv.getLineLv();
        if (CheckNull.isEmpty(lvMap)) {
            throw new MwException(GameError.CABINET_CONFING_ERR.getCode(), "内阁天策府等级配置出错   roleId:", roleId);
        }
        int curPlanId = player.cabinet.getCurPlanId();
        Integer needPlayerLv = lvMap.get(curPlanId);
        if (null == needPlayerLv) {
            throw new MwException(GameError.CABINET_CONFING_ERR.getCode(), "内阁天策府等级配置出错  roleId:", roleId);
        }
        int playerLv = player.lord.getLevel();
        if (playerLv < needPlayerLv) {
            throw new MwException(GameError.LV_NOT_ENOUGH.getCode(), "内阁天策府创建点兵统领,玩家等级不够  roleId:", roleId);
        }

        StaticCabinetPlan cabinetPlan = StaticBuildingDataMgr.getCabinetPlanById(cabinet.getCurPlanId());
        if (null == cabinetPlan) {
            throw new MwException(GameError.CABINET_CONFING_ERR.getCode(), "内阁天策府配置出错 roleId:", roleId);
        }
        if (cabinet.getLeadStep() < cabinetPlan.getNpcCount()) {
            throw new MwException(GameError.CABINET_FINISH_NOT_ENOUGH.getCode(), "击杀点兵统领个数不足够 roleId:", roleId);
        }
        if (cabinetPlan.getNextId() <= 0) {
            // 说明完成的是最后一个点兵任务
            cabinet.setFinsh(true);
        } else {
            // 设置成下一个点兵任务的数据
            cabinet.setPrePlanId(cabinet.getCurPlanId());
            cabinet.setCurPlanId(cabinetPlan.getNextId());
            cabinet.setLeadStep(0);
            cabinet.setFinsh(false);
            cabinet.setCreateLead(false);
            cabinet.setLvFinish(false);
        }

        CabinetFinishRs.Builder builder = CabinetFinishRs.newBuilder();
        // 重新计算并更新武将,采集将 的属性
        for (int i = 1; i < HeroConstant.HERO_BATTLE_LEN + 1; i++) {
            Hero acqHero = player.heros.get(player.heroAcq[i]);
            Hero battleHero = player.heros.get(player.heroBattle[i]);
            if (acqHero != null && acqHero.isOnAcq()) {
                // 先计算
                CalculateUtil.processAttr(player, acqHero);
                builder.addAcqHeros(PbHelper.createHeroPb(acqHero, player));
            }
            if (battleHero != null && battleHero.isOnBattle()) {
                // 先计算
                CalculateUtil.processAttr(player, battleHero);
                builder.addBattleHeros(PbHelper.createHeroPb(battleHero, player));
            }
        }
        // 更新战力排行榜
        CalculateUtil.reCalcFight(player);
        builder.setLeadLv(leadLv);
        builder.setLeadExp(leadExp);
        builder.setPlanId(player.cabinet.getCurPlanId());
        builder.setLeadStep(player.cabinet.getLeadStep());
        builder.setIsCreateLead(player.cabinet.isCreateLead());
        builder.setIsFinish(player.cabinet.isFinsh());
        builder.setIsLvFinish(player.cabinet.isLvFinish());
        return builder.build();
    }

}
