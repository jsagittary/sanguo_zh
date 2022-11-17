package com.gryphpoem.game.zw.service.hero;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.dataMgr.StaticHeroDataMgr;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.RewardDataManager;
import com.gryphpoem.game.zw.manager.TaskDataManager;
import com.gryphpoem.game.zw.manager.TechDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb1;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticHero;
import com.gryphpoem.game.zw.resource.pojo.ChangeInfo;
import com.gryphpoem.game.zw.resource.pojo.WarPlane;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.hero.PartnerHero;
import com.gryphpoem.game.zw.resource.util.AccountHelper;
import com.gryphpoem.game.zw.resource.util.CalculateUtil;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.service.*;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-11-17 11:33
 */
@Component
public class HeroOnBattleService implements GmCmdService {

    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private HeroService heroService;
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private TechDataManager techDataManager;
    @Autowired
    private ArmyService armyService;
    @Autowired
    private TaskDataManager taskDataManager;
    @Autowired
    private WarFactoryService warFactoryService;
    @Autowired
    private WallService wallService;

    /**
     * 将领上阵
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GamePb1.HeroBattleRs heroBattle(long roleId, GamePb1.HeroBattleRq req) throws MwException {
        Player player = playerDataManager.getPlayer(roleId);

        // 检查角色是否存在
        AccountHelper.checkPlayerIsExist(player, roleId);

        int heroId = req.getHeroId();
        int pos = req.getPos();
        int heroRoleType = req.getRoleType();
        boolean swap = false;
        if (req.hasSwap()) {
            swap = req.getSwap();
        }
        boolean swapPlane = false;
        if (req.hasSwapPlane()) {
            swapPlane = req.getSwapPlane();
        }

        // 替换宝具标识
        boolean swapTreasure = false;
        if (req.hasSwapTreasure()) {
            swapTreasure = req.getSwapTreasure();
        }
        // 替换兵书标识
        boolean swapMedal = false;
        if (req.hasSwapMedal()) {
            swapMedal = req.getSwapMedal();
        }

        // 检查pos位是否正常
        if (pos < HeroConstant.HERO_BATTLE_1 || pos > HeroConstant.HERO_BATTLE_4) {
            throw new MwException(GameError.HERO_BATTLE_POS_ERROR.getCode(), "将领上阵队列位置不正确, roleId:", roleId, ", pos:",
                    pos);
        }
        if (heroRoleType != HeroConstant.HERO_ROLE_TYPE_PRINCIPAL && heroRoleType != HeroConstant.HERO_ROLE_TYPE_DEPUTY) {
            throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(), "客户端发送的上阵武将角色错误, roleId:", roleId, ", pos:",
                    pos, ", roleType: ", heroRoleType);
        }

        // 检查科技开启位置
        if (pos == HeroConstant.HERO_BATTLE_3) {
            if (!techDataManager.isOpen(player, TechConstant.TYPE_10)) {
                throw new MwException(GameError.HERO_BATTLE_POS_NEED_TECH.getCode(), "未开启,请提升科技等级 roleId:", roleId,
                        ", pos:", pos, "lv:", techDataManager.getTechEffect4SingleVal(player, TechConstant.TYPE_10));
            }
        } else if (pos == HeroConstant.HERO_BATTLE_4) {
            if (!techDataManager.isOpen(player, TechConstant.TYPE_20)) {
                throw new MwException(GameError.HERO_BATTLE_POS_NEED_TECH.getCode(), "未开启,请提升科技等级, roleId:", roleId,
                        ", pos:", pos, "lv:", techDataManager.getTechEffect4SingleVal(player, TechConstant.TYPE_20));
            }
        }

        // 检查玩家是否有该将领
        Hero hero = heroService.checkHeroIsExist(player, heroId);
        StaticHero onBattleHero = StaticHeroDataMgr.getHeroMap().get(heroId);
        // 判断该将领是否已经上阵
        if (player.isOnBattleHero(heroId) || player.isOnWallHero(heroId) || player.isOnAcqHero(heroId) ||
                player.isOnCommandoHero(heroId) || player.isOnDeputyHero(heroId)) {
            throw new MwException(GameError.HERO_BATTLE_REPEAT.getCode(), String.format("roleId :%d, heroId :%d, status :%d, pos :%d, wallPos :%d, acqPos :%d",
                    roleId, heroId, hero.getStatus(), hero.getPos(), hero.getWallPos(), hero.getAcqPos()));
        }

        GamePb1.HeroBattleRs.Builder builder = GamePb1.HeroBattleRs.newBuilder();
        Hero battleHero = player.getBattleHeroByPos(pos);

        int defPos = 0;
        if (battleHero != null) {
            defPos = battleHero.getDefPos();
            checkDeputyMatch(heroRoleType, heroId, roleId, battleHero, onBattleHero);
        }

        ChangeInfo change = ChangeInfo.newIns();
        boolean sysClientUpdateMedal = false;
        if (null != battleHero) {// 位置上已有其他将领存在，现将该将领下阵
            if (!battleHero.isIdle()) {
                throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "将领不是空闲状态不能操作");
            }
            if (swap) {// 如果需要交换装备，执行交换装备的逻辑
                // rewardDataManager.checkBagCnt(player);
                heroService.swapHeroEquip(player, hero, battleHero);
            }
            if (swapPlane) {// 如果需要交换战机, 执行交换战机的逻辑
                heroService.swapHeroPlanes(player, hero, battleHero, pos);
            } else {
                heroService.downHeroAllPlane(player, battleHero); // 下阵将领, 把战机也下阵
            }
            if (swapTreasure) {// 如果需要替换宝具，执行宝具替换的逻辑
                heroService.swapHeroTreasure(player, battleHero, hero);
            }
            if (swapMedal) {// 如果需要替换兵书，执行兵书替换的逻辑
                heroService.swapHeroMedal(player, battleHero, hero);
                sysClientUpdateMedal = true; // 只要玩家选择交互，则默认通知客户端需要重新拉取兵书信息
            }
            battleHero.onBattle(0);// 将领下阵，pos设置为0
            battleHero.onDef(0);// 防守将领下阵, pos设置为0
            // 主副将下阵, 修改武将角色类型
            battleHero.onDeputy(0, HeroConstant.HERO_STATUS_IDLE);
            // 告诉客户端武将兵书是否有更新
            builder.setUpdateMedal(sysClientUpdateMedal);

            // 士兵回营
            int sub = battleHero.getCount();
            battleHero.setCount(0);
            StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(battleHero.getHeroId());

            if (sub > 0) {
                rewardDataManager.modifyArmyResource(player, staticHero.getType(), sub, 0, AwardFrom.HERO_DOWN);
            }

            change.addChangeType(AwardType.ARMY, staticHero.getType());
            change.addChangeType(AwardType.HERO_ARM, battleHero.getHeroId());
            builder.setDownHero(PbHelper.createHeroPb(battleHero, player));
        }

        // 将领上阵
        hero.onBattle(pos);
        // 处理副将
        handlePartnerHero(battleHero, hero, onBattleHero, heroRoleType, pos, HeroConstant.HERO_STATUS_BATTLE);

        if (!CheckNull.isEmpty(hero.getWarPlanes())) {
            for (int planeId : hero.getWarPlanes()) {
                WarPlane plane = player.checkWarPlaneIsExist(planeId);
                if (!CheckNull.isNull(plane)) {
                    plane.setPos(pos);
                }
            }
        }
        player.heroBattle[pos] = heroId;// 更新已上阵将领队列信息
        if (defPos != 0) { // 更换将领
            hero.onDef(defPos);
            player.heroDef[defPos] = heroId;// 更新已防守将领队列信息
        } else { // 上阵防守将领
            hero.onDef(pos);
            player.heroDef[pos] = heroId;// 更新已防守将领队列信息
        }

        // 重新计算并更新将领属性
        CalculateUtil.processAttr(player, hero);
        if (Objects.nonNull(battleHero)) {
            // 重新计算并更新将领属性
            CalculateUtil.processAttr(player, battleHero);
        }
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

        // 返回将领上阵协议
        builder.setUpHero(PbHelper.createHeroPb(hero, player));
        taskDataManager.updTask(player, TaskType.COND_HERO_UP, 1, hero.getHeroId());
        taskDataManager.updTask(player, TaskType.COND_509, 1, hero.getQuality());
        CalculateUtil.reCalcFight(player);
        taskDataManager.updTask(player, TaskType.COND_28, 1, hero.getType());
        heroService.checkHeroQueueStatus(player);
        return builder.build();
    }


    /**
     * 内阁采集将领布置
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GamePb1.AcqHeroSetRs acqHeroSet(long roleId, GamePb1.AcqHeroSetRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);

        int pos = req.getPos();
        int type = req.getType();
        int heroId = req.getHeroId();
        int heroRoleType = req.getRoleType();
        boolean swap = req.hasSwap() && req.getSwap();
        boolean swapMedal = req.hasSwapMedal() && req.getSwapMedal();
        boolean swapTreasure = req.hasSwapTreasure() && req.getSwapTreasure();

        if (player.building.getWar() < Constant.CABINET_CONDITION.get(0)) {
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
        GamePb1.AcqHeroSetRs.Builder builder = GamePb1.AcqHeroSetRs.newBuilder();
        boolean sysClientUpdateMedal = false;
        if (1 == type) {// 上阵
            Hero hero = heroService.checkHeroIsExist(player, heroId);
            StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(heroId);
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

                // 检验如果替换下来的是副将, 校验换上去的武将是否与主将兵种匹配
                checkDeputyMatch(heroRoleType, heroId, roleId, downHero, staticHero);
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
                change = warFactoryService.downAcqHeroAndBackRes(player, downHero);
                // 记录返回下阵将领
                builder.setDownHero(PbHelper.createHeroPb(downHero, player));
            }

            // 采集将领上阵
            hero.onAcq(pos);
            // 处理副将相关
            handlePartnerHero(downHero, hero, staticHero, heroRoleType, pos, hero.getStatus());
            // 更新已上阵将领队列信息
            player.heroAcq[pos] = heroId;
            // 重新调整位置
            warFactoryService.reAdjustHeroPos(player.heroAcq, player.heros);
            // 重新计算并更新将领属性
            if (Objects.nonNull(downHero))
                CalculateUtil.processAttr(player, downHero);
            CalculateUtil.processAttr(player, hero);
            taskDataManager.updTask(player, TaskType.COND_510, 1, hero.getQuality());

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
                change = warFactoryService.downAcqHeroAndBackRes(player, downHero);
                // 重新调整
                player.heroAcq[myPos] = 0;// 下阵的位置清0
                warFactoryService.reAdjustHeroPos(player.heroAcq, player.heros);
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
     * 城墙布置
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public GamePb1.WallSetRs doWallSet(long roleId, GamePb1.WallSetRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        // 检测是否满足开启天策府
        if (player.building.getWar() < Constant.CABINET_CONDITION.get(1)) {
            // 内阁等级小于3级禁止开发
            throw new MwException(GameError.WAR_FACTORY_LV_NOT_ENOUGH.getCode(), "内阁 等级不够");
        }

        int pos = req.getPos();
        int type = req.getType();
        int heroId = req.getHeroId();
        int heroRoleType = req.getRoleType();
        boolean swap = req.hasSwap() && req.getSwap();
        boolean swapMedal = req.hasSwapMedal() && req.getSwapMedal();
        boolean swapTreasure = req.hasSwapTreasure() && req.getSwapTreasure();

        // 检查pos位是否正常
        if (pos < HeroConstant.HERO_BATTLE_1 || pos > HeroConstant.HERO_BATTLE_4) {
            throw new MwException(GameError.HERO_BATTLE_POS_ERROR.getCode(), "将领上阵队列位置不正确, roleId:", roleId, ", pos:",
                    pos);
        }

        // 检测配置是否正确
        List<Integer> lvRequire = Constant.GUARDS_HERO_REQUIRE;
        if (CheckNull.isEmpty(lvRequire) || lvRequire.size() != 4) {
            throw new MwException(GameError.NO_CONFIG.getCode(), "内阁采集将领上阵队列位置不正确, roleId:", roleId, ", pos:", pos);
        }
        int lv = player.lord.getLevel();
        // 检测等级是否满足
        if (pos == HeroConstant.HERO_BATTLE_1) {
            if (lv < lvRequire.get(0)) {
                throw new MwException(GameError.WAR_FACTORY_HERO_POS_NEED.getCode(), "未开启,内阁御林军将领布置等级不够 roleId:",
                        roleId, ", pos:", pos);
            }
        } else if (pos == HeroConstant.HERO_BATTLE_2) {
            if (lv < lvRequire.get(1)) {
                throw new MwException(GameError.WAR_FACTORY_HERO_POS_NEED.getCode(), "未开启,内阁御林军将领布置等级不够 roleId:",
                        roleId, ", pos:", pos);
            }
        } else if (pos == HeroConstant.HERO_BATTLE_3) {
            if (lv < lvRequire.get(2)) {
                throw new MwException(GameError.WAR_FACTORY_HERO_POS_NEED.getCode(), "未开启,内阁御林军将领布置等级不够 roleId:",
                        roleId, ", pos:", pos);
            }
        } else if (pos == HeroConstant.HERO_BATTLE_4) {
            if (lv < lvRequire.get(3)) {
                throw new MwException(GameError.WAR_FACTORY_HERO_POS_NEED.getCode(), "未开启,内阁御林军将领布置等级不够 roleId:",
                        roleId, ", pos:", pos);
            }
        }

        Hero hero = heroService.checkHeroIsExist(player, heroId);
        StaticHero staticHero = StaticHeroDataMgr.getHeroMap().get(heroId);
        GamePb1.WallSetRs.Builder builder = GamePb1.WallSetRs.newBuilder();
        boolean sysClientUpdateMedal = false;
        if (type == 1) {
            // 判断该将领是否在武将上阵
            if (player.isOnBattleHero(heroId) || player.isOnWallHero(heroId) || player.isOnAcqHero(heroId)) {
                throw new MwException(GameError.HERO_BATTLE_REPEAT.getCode(), "是武将将领已上阵, roleId:", roleId, ", heroId:",
                        heroId);
            }
            Hero battleHero = player.getWallHeroByPos(pos);

            if (null != battleHero) {// 位置上已有其他将领存在，现将该将领下阵
                if (!battleHero.isIdle()) {
                    throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "将领不是空闲状态不能操作");
                }
                // 检验如果替换下来的是副将, 校验换上去的武将是否与主将兵种匹配
                checkDeputyMatch(heroRoleType, heroId, roleId, battleHero, staticHero);

                // 位置上已有其他将领存在，现将该将领下阵
                if (swap) {// 如果需要交换装备，执行交换装备的逻辑
                    // rewardDataManager.checkBagCnt(player);
                    heroService.swapHeroEquip(player, hero, battleHero);
                }
                if (swapTreasure) {// 如果需要交换宝具，执行交换宝具的逻辑
                    heroService.swapHeroTreasure(player, battleHero, hero);
                }
                if (swapMedal) {// 如果需要交换兵书，执行交换兵书的逻辑
                    heroService.swapHeroMedal(player, battleHero, hero);
                    sysClientUpdateMedal = true;
                }
                wallService.downWallHeroAndBackRes(player, battleHero);
                // 下阵
                builder.setDownHero(PbHelper.createHeroPb(battleHero, player));
            }

            List<CommonPb.TwoInt> seasonTalentAttr = null;
            // 将领上阵
            hero.onWall(pos);
            // 处理副将
            handlePartnerHero(battleHero, hero, staticHero, heroRoleType, pos, hero.getStatus());

            player.heroWall[pos] = heroId;// 更新已上阵将领队列信息
            // 重新调整位置
            wallService.reAdjustHeroPos(player.heroWall, player.heros);
            // 重新计算并更新将领属性
            CalculateUtil.processAttr(player, hero);
            // 重新计算并更新将领属性
            if (Objects.nonNull(battleHero))
                CalculateUtil.processAttr(player, battleHero);
            // 返回将领上阵协议
            if (hero.isOnWall()) {
                //禁卫军赛季天赋加成
                List<CommonPb.TwoInt> janitorAttr = DataResource.getBean(SeasonTalentService.class).getSeasonTalentEffectTwoInt(player, hero, SeasonConst.TALENT_EFFECT_619);
                if (!ObjectUtils.isEmpty(janitorAttr)) {
                    seasonTalentAttr = new ArrayList<>(janitorAttr);
                }
            }

            builder.setUpHero(PbHelper.createHeroPb(hero, player, seasonTalentAttr));
        } else {
            // 下阵
            int myPos = 0;
            for (int i = 1; i < player.heroAcq.length; i++) {
                if (player.heroWall[i] == heroId) {
                    myPos = i;
                    break;
                }
            }
            Hero battleHero = player.getWallHeroByPos(myPos);
            if (null != battleHero) {// 位置上已有其他将领存在，现将该将领下阵
                if (!battleHero.isIdle()) {
                    throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "将领不是空闲状态不能操作");
                }
                wallService.downWallHeroAndBackRes(player, battleHero);
                player.heroWall[myPos] = 0; // 下阵的位置清0
                // 重新调整位置
                wallService.reAdjustHeroPos(player.heroWall, player.heros);
                // 重新计算并更新将领属性
                CalculateUtil.processAttr(player, battleHero);
                // 下阵
                builder.setDownHero(PbHelper.createHeroPb(battleHero, player));
            }
        }
        for (int i = 1; i < player.heroWall.length; i++) {
            if (player.heroWall[i] != 0)
                builder.addHeroIds(player.heroWall[i]);
        }

        builder.setUpdateMedal(sysClientUpdateMedal);
        return builder.build();
    }

    /**
     * 将领换位置
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public GamePb1.HeroPosSetRs heroPosSet(long roleId, GamePb1.HeroPosSetRq req) throws MwException {
        Player player = playerDataManager.getPlayer(roleId);
        Set<Integer> check = new HashSet<>();
        int posType = req.getPosType();
        if (posType < HeroConstant.CHANGE_POS_TYPE || posType > HeroConstant.CHANGE_TREASURE_WARE_POS_TYPE) {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "roleId :", roleId);
        }
        for (CommonPb.TwoInt kv : req.getHerosList()) {
            int pos = kv.getV1();

            if (check.contains(kv.getV1()) || check.contains(kv.getV2())) {
                throw new MwException(GameError.HERO_BATTLE_POS_ERROR.getCode(), "将领上阵队列位置不正确, roleId:", roleId,
                        ", pos:", pos);
            }
            check.add(kv.getV1());
            check.add(kv.getV2());

            if (pos < HeroConstant.HERO_BATTLE_1 || pos > HeroConstant.HERO_BATTLE_4) {
                throw new MwException(GameError.HERO_BATTLE_POS_ERROR.getCode(), "将领上阵队列位置不正确, roleId:", roleId, ", pos:", pos);
            }

            if (pos == HeroConstant.HERO_BATTLE_3) {
                if (!techDataManager.isOpen(player, TechConstant.TYPE_10)) {
                    throw new MwException(GameError.HERO_BATTLE_POS_NEED_TECH.getCode(), "未开启,请提升科技等级 roleId:", roleId, ", pos:", pos);
                }
            } else if (pos == HeroConstant.HERO_BATTLE_4) {
                if (!techDataManager.isOpen(player, TechConstant.TYPE_20)) {
                    throw new MwException(GameError.HERO_BATTLE_POS_NEED_TECH.getCode(), "未开启,请提升科技等级, roleId:", roleId, ", pos:", pos);
                }
            }
            Hero hero = heroService.checkHeroIsExist(player, kv.getV2());
            if (hero.getPos() <= 0) {
                throw new MwException(GameError.HERO_BATTLE_POS_ERROR.getCode(), "将领上阵队列位置不正确, roleId:", roleId, ", pos:", pos);
            }
        }

        if (posType == HeroConstant.CHANGE_POS_TYPE ||
                posType == HeroConstant.CHANGE_DEFEND_POS_TYPE ||
                posType == HeroConstant.CHANGE_TREASURE_WARE_POS_TYPE) {
            int[] heroArray = posType == HeroConstant.CHANGE_POS_TYPE ||
                    posType == HeroConstant.CHANGE_TREASURE_WARE_POS_TYPE ?
                    player.heroBattle : player.heroDef;
            Set<Integer> set = Arrays.stream(heroArray).filter(heroId -> heroId > 0).boxed().collect(Collectors.toSet());
            if (req.getHerosCount() != set.size()) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId :%d, hero arrays :%s", roleId, Arrays.toString(heroArray)));
            }
            for (CommonPb.TwoInt twoInt : req.getHerosList()) {
                if (!set.contains(twoInt.getV2())) {
                    throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId :%d, heroId :%d, not In arrays,  hero arrays :%s",
                            roleId, twoInt.getV2(), Arrays.toString(heroArray)));
                }
            }
        }


        GamePb1.HeroPosSetRs.Builder builder = GamePb1.HeroPosSetRs.newBuilder();
        List<Integer> posList = player.heroBattlePos.get(posType);
        if (posType == HeroConstant.CHANGE_COMBAT_POS_TYPE ||
                posType == HeroConstant.CHANGE_BATTLE_POS_TYPE ||
                posType == HeroConstant.CHANGE_TREASURE_WARE_POS_TYPE) {
            if (posList == null) {
                posList = new ArrayList<>();
                player.heroBattlePos.put(posType, posList);
            }
            posList.clear();
        }
        for (CommonPb.TwoInt kv : req.getHerosList()) {
            Hero hero = heroService.checkHeroIsExist(player, kv.getV2());
            if (posType == HeroConstant.CHANGE_POS_TYPE) {
                hero.onBattle(kv.getV1());
                player.heroBattle[kv.getV1()] = kv.getV2();// 更新已上阵将领队列信息
            } else if (posType == HeroConstant.CHANGE_DEFEND_POS_TYPE) {
                hero.onDef(kv.getV1());
                player.heroDef[kv.getV1()] = kv.getV2();// 更新已上阵防守将领队列信息
            } else {
                posList.add(kv.getV1());
            }
            builder.addHeros(PbHelper.createHeroPb(hero, player));
        }
        heroService.checkHeroQueueStatus(player);
        return builder.build();
    }

    /**
     * 处理主将或副将信息
     *
     * @param battleHero   阵上将领信息
     * @param hero         上阵的将领
     * @param onBattleHero 上阵将领配置
     * @param heroRoleType 主将或副将类型
     * @param pos          上阵阵位
     */
    private void handlePartnerHero(Hero battleHero, Hero hero, StaticHero onBattleHero, int heroRoleType, int pos, int heroStatus) {
        if (CheckNull.isNull(battleHero)) return;
        
        // 副将处理
        PartnerHero partnerHero = battleHero.getPartnerHero();
        if (Objects.nonNull(partnerHero)) {
            switch (heroRoleType) {
                case HeroConstant.HERO_ROLE_TYPE_PRINCIPAL:
                    // 若当前武将上阵为替换的为主将
                    // 检查是否与当前副将兵种匹配
                    List<Hero> assHeroList = partnerHero.getDeputyHeroList();
                    if (CheckNull.nonEmpty(assHeroList)) {
                        Iterator<Hero> it = assHeroList.iterator();
                        while (it.hasNext()) {
                            Hero ass = it.next();
                            if (CheckNull.isNull(ass)) {
                                it.remove();
                                continue;
                            }
                            StaticHero assHero = StaticHeroDataMgr.getHeroMap().get(ass.getHeroId());
                            if (CheckNull.isNull(assHero) || !onBattleHero.getDeputyArms().contains(assHero.getType())) {
                                // 与主将兵种不匹配, 下阵
                                ass.onBattle(0);// 将领下阵，pos设置为0
                                ass.onDef(0);// 防守将领下阵, pos设置为0
                                ass.onDeputy(0, HeroConstant.HERO_STATUS_IDLE);
                                ass.getPartnerHero().setPrincipalHero(null);
                                it.remove();
                            }
                        }
                        hero.getPartnerHero().setDeputyHeroList(new ArrayList<>(assHeroList));
                        assHeroList.clear();
                    }
                    break;
                case HeroConstant.HERO_ROLE_TYPE_DEPUTY:
                    // 若当前武将上阵为替换的为副将
                    Hero principalHero = partnerHero.getPrincipalHero();
                    if (Objects.nonNull(principalHero)) {
                        List<Hero> deputyHeroList;
                        if (CheckNull.nonEmpty(deputyHeroList = principalHero.getPartnerHero().getDeputyHeroList())) {
                            deputyHeroList.remove(battleHero);
                        }
                        principalHero.getPartnerHero().getDeputyHeroList().add(hero);
                        hero.getPartnerHero().setPrincipalHero(principalHero);
                    }
                    // 副将上阵
                    hero.onDeputy(pos, heroStatus);
                    if (Objects.nonNull(battleHero)) {
                        // 清除阵上将领的主将信息
                        battleHero.getPartnerHero().setPrincipalHero(null);
                    }
                    break;
            }
        }
    }

    /**
     * 校验主将是否与副将兵种匹配
     *
     * @param heroRoleType
     * @param heroId
     * @param roleId
     * @param battleHero
     * @param onBattleHero
     */
    private void checkDeputyMatch(int heroRoleType, int heroId, long roleId, Hero battleHero, StaticHero onBattleHero) {
        Hero principalHero;
        PartnerHero partnerHero = battleHero.getPartnerHero();
        if (heroRoleType == HeroConstant.HERO_ROLE_TYPE_DEPUTY && Objects.nonNull(partnerHero) &&
                Objects.nonNull(principalHero = partnerHero.getPrincipalHero())) {
            StaticHero curBattleHero = StaticHeroDataMgr.getHeroMap().get(principalHero.getHeroId());
            // 上阵的是副将, 检验当前武将是否与主将兵种匹配
            if (Objects.nonNull(curBattleHero) && Objects.nonNull(curBattleHero.getDeputyArms()) &&
                    !curBattleHero.getDeputyArms().contains(onBattleHero.getType())) {
                throw new MwException(GameError.DEPUTY_HERO_CANNOT_ON_BATTLE.getCode(), String.format("roleId:%d, curBattleHeroId:%d, onBattleHeroId:%d",
                        roleId, battleHero.getHeroId(), heroId));
            }
        }
    }

    @GmCmd("heroOnBattle")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {

    }
}
