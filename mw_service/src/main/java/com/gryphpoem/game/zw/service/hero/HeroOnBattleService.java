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
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.service.*;
import com.gryphpoem.game.zw.service.session.SeasonTalentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.function.Consumer;
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
        int deputyPos = req.getDeputyPos();
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
        if (player.isOnBattleHero(heroId) || player.isOnWallHero(heroId) || player.isOnAcqHero(heroId)) {
            throw new MwException(GameError.HERO_BATTLE_REPEAT.getCode(), String.format("roleId :%d, heroId :%d, status :%d, pos :%d, wallPos :%d, acqPos :%d",
                    roleId, heroId, hero.getStatus(), hero.getPos(), hero.getWallPos(), hero.getAcqPos()));
        }

        GamePb1.HeroBattleRs.Builder builder = GamePb1.HeroBattleRs.newBuilder();
        PartnerHero partnerHero = player.getBattleHeroByPos(pos);

        int defPos = 0;
        if (!HeroUtil.isEmptyPartner(partnerHero)) {
            defPos = partnerHero.getPrincipalHero().getDefPos();
            checkDeputyMatch(heroRoleType, heroId, roleId, partnerHero, onBattleHero);
        } else {
            // 当前没有主将在阵位上, 玩家不可上阵副将
            if (heroRoleType == HeroConstant.HERO_ROLE_TYPE_DEPUTY) {
                throw new MwException(GameError.CHIEF_HERO_ON_BATTLE_FIRST.getCode(), String.format("roleId:%d, onBattleHeroId:%d, 先上阵主将",
                        roleId, heroId));
            }
        }

        Hero battleHero = null;
        if (!HeroUtil.isEmptyPartner(partnerHero)) {
            battleHero = partnerHero.getCurHero(heroId);
        }
        ChangeInfo change = ChangeInfo.newIns();
        boolean sysClientUpdateMedal = false;
        if (Objects.nonNull(battleHero)) {// 位置上已有其他将领存在，现将该将领下阵
            if (!battleHero.isIdle()) {
                throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "将领不是空闲状态不能操作");
            }
            if (heroRoleType == HeroConstant.HERO_ROLE_TYPE_DEPUTY && battleHero.getPartnerPosIndex() != deputyPos) {
                throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId:%d, curPos:%d, changePos:%d, 替换的副将阵位与传参阵位不匹配",
                        roleId, battleHero.getPartnerPosIndex(), deputyPos));
            }

            if (swap) {// 如果需要交换装备，执行交换装备的逻辑
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
            battleHero.setPartnerPosIndex(0);// 将领下阵, 副将阵位索引为0
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
        }

        // 将领上阵
        hero.onBattle(pos);
        if (CheckNull.isNull(partnerHero)) {
            partnerHero = new PartnerHero();
            player.getPlayerFormation().getHeroBattle()[pos] = partnerHero;
        }
        // 处理副将
        handlePartnerHero(battleHero, hero, partnerHero, onBattleHero, heroRoleType, req.getDeputyPos(), (ass) -> {
            if (CheckNull.isNull(ass)) return;
            // 将副将置为无类型 (与上阵主将兵种冲突的副将下阵)
            ass.setRoleType(HeroConstant.HERO_ROLE_TYPE_NOTHING);
            ass.onBattle(0);
            ass.onDef(0);
            ass.setPartnerPosIndex(0);
            CalculateUtil.processAttr(player, ass);
            builder.addDownHero(PbHelper.createHeroPb(ass, player));
        });

        if (!CheckNull.isEmpty(hero.getWarPlanes())) {
            for (int planeId : hero.getWarPlanes()) {
                WarPlane plane = player.checkWarPlaneIsExist(planeId);
                if (!CheckNull.isNull(plane)) {
                    plane.setPos(pos);
                }
            }
        }
        hero.onDef(defPos);
        player.getPlayerFormation().getHeroDef()[defPos] = partnerHero;

        // 重新计算并更新将领属性
        CalculateUtil.processAttr(player, hero);
        if (Objects.nonNull(battleHero)) {
            // 重新计算并更新将领属性
            CalculateUtil.processAttr(player, battleHero);
            builder.addDownHero(PbHelper.createHeroPb(battleHero, player));
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
        int deputyPos = req.getDeputyPos();
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
        if (heroRoleType != HeroConstant.HERO_ROLE_TYPE_PRINCIPAL && heroRoleType != HeroConstant.HERO_ROLE_TYPE_DEPUTY) {
            throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(), "客户端发送的上阵武将角色错误, roleId:", roleId, ", pos:",
                    pos, ", roleType: ", heroRoleType);
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

            PartnerHero partnerHero = player.getAcqHeroByPos(pos);
            Hero downHero = null;
            if (!HeroUtil.isEmptyPartner(partnerHero)) {
                downHero = partnerHero.getCurHero(heroId);
            } else {
                // 当前没有主将在阵位上, 玩家不可上阵副将
                if (heroRoleType == HeroConstant.HERO_ROLE_TYPE_DEPUTY) {
                    throw new MwException(GameError.CHIEF_HERO_ON_BATTLE_FIRST.getCode(), String.format("roleId:%d, onBattleHeroId:%d, 先上阵主将",
                            roleId, heroId));
                }
            }

            if (null != downHero) {
                if (!downHero.isIdle()) {
                    throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "将领不是空闲状态不能操作  roleId:", roleId, " state:",
                            downHero.getState());
                }
                if (heroRoleType == HeroConstant.HERO_ROLE_TYPE_DEPUTY && downHero.getPartnerPosIndex() != deputyPos) {
                    throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId:%d, curPos:%d, changePos:%d, 替换的副将阵位与传参阵位不匹配",
                            roleId, downHero.getPartnerPosIndex(), deputyPos));
                }

                // 检验如果替换下来的是副将, 校验换上去的武将是否与主将兵种匹配
                checkDeputyMatch(heroRoleType, heroId, roleId, partnerHero, staticHero);
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
                warFactoryService.downAcqHeroAndBackRes(player, downHero);
            }

            // 采集将领上阵
            hero.onAcq(pos);
            // 更新已上阵将领队列信息
            if (CheckNull.isNull(partnerHero)) {
                partnerHero = new PartnerHero();
                player.getPlayerFormation().getHeroAcq()[pos] = partnerHero;
            }
            // 处理副将相关
            handlePartnerHero(downHero, hero, partnerHero, staticHero, heroRoleType, deputyPos, (ass) -> {
                ass.onAcq(0); // 将领下阵，pos设置为0
                ass.setPartnerPosIndex(0);
                ass.setRoleType(HeroConstant.HERO_ROLE_TYPE_NOTHING);
                CalculateUtil.processAttr(player, ass);
                builder.addDownHero(PbHelper.createHeroPb(ass, player));
            });

            // 重新调整位置
            warFactoryService.reAdjustHeroPos(player.getPlayerFormation().getHeroAcq(), player.heros);
            // 重新计算并更新将领属性
            if (Objects.nonNull(downHero)) {
                CalculateUtil.processAttr(player, downHero);
                // 记录返回下阵将领
                builder.addDownHero(PbHelper.createHeroPb(downHero, player));
            }
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
            Hero downHero = null;
            PartnerHero partnerHero = null;
            for (int i = 1; i < player.getPlayerFormation().getHeroAcq().length; i++) {
                partnerHero = player.getPlayerFormation().getHeroAcq()[i];
                if (HeroUtil.isEmptyPartner(partnerHero)) continue;
                if ((downHero = partnerHero.getCurHero(heroId)) != null) {
                    break;
                }
            }
            if (CheckNull.isNull(downHero)) {
                throw new MwException(GameError.HERO_NOT_ON_BATTLE.getCode(), "武将不在阵上 roleId:", roleId, ", heroId:", heroId);
            }
            // 下阵
            if (downHero != null) {
                if (!downHero.isIdle()) {
                    throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "将领不是空闲状态不能操作 roleId:", roleId, " state:",
                            downHero.getState(), ", heroId:", downHero.getHeroId());
                }
                // 士兵回营
                change = warFactoryService.downAcqHeroAndBackRes(player, downHero);
                downHero(downHero, partnerHero, ass -> {
                    ass.onAcq(0); // 将领下阵，pos设置为0
                    ass.setRoleType(HeroConstant.HERO_ROLE_TYPE_NOTHING);
                    ass.setPartnerPosIndex(0);
                    CalculateUtil.processAttr(player, ass);
                    builder.addDownHero(PbHelper.createHeroPb(ass, player));
                });

                warFactoryService.reAdjustHeroPos(player.getPlayerFormation().getHeroAcq(), player.heros);
                // 重新计算并更新将领属性
                CalculateUtil.processAttr(player, downHero);
                // 记录返回下阵将领
                builder.addDownHero(PbHelper.createHeroPb(downHero, player));
            }
        }
        // 通知客户端玩家资源改变
        if (change != null)
            rewardDataManager.syncRoleResChanged(player, change);

        for (int i = 1; i < player.getPlayerFormation().getHeroAcq().length; i++) {
            PartnerHero partnerHero = player.getPlayerFormation().getHeroAcq()[i];
            if (HeroUtil.isEmptyPartner(partnerHero)) continue;
            builder.addHeroIds(partnerHero.createPb(false));
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
        int deputyPos = req.getDeputyPos();
        int heroRoleType = req.getRoleType();
        boolean swap = req.hasSwap() && req.getSwap();
        boolean swapMedal = req.hasSwapMedal() && req.getSwapMedal();
        boolean swapTreasure = req.hasSwapTreasure() && req.getSwapTreasure();

        // 检查pos位是否正常
        if (pos < HeroConstant.HERO_BATTLE_1 || pos > HeroConstant.HERO_BATTLE_4) {
            throw new MwException(GameError.HERO_BATTLE_POS_ERROR.getCode(), "将领上阵队列位置不正确, roleId:", roleId, ", pos:",
                    pos);
        }
        if (heroRoleType != HeroConstant.HERO_ROLE_TYPE_PRINCIPAL && heroRoleType != HeroConstant.HERO_ROLE_TYPE_DEPUTY) {
            throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(), "客户端发送的上阵武将角色错误, roleId:", roleId, ", pos:",
                    pos, ", roleType: ", heroRoleType);
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
            PartnerHero partnerHero = player.getWallHeroByPos(pos);
            Hero battleHero = null;
            if (!HeroUtil.isEmptyPartner(partnerHero)) {
                battleHero = partnerHero.getCurHero(heroId);
            } else {
                // 当前没有主将在阵位上, 玩家不可上阵副将
                if (heroRoleType == HeroConstant.HERO_ROLE_TYPE_DEPUTY) {
                    throw new MwException(GameError.CHIEF_HERO_ON_BATTLE_FIRST.getCode(), String.format("roleId:%d, onBattleHeroId:%d, 先上阵主将",
                            roleId, heroId));
                }
            }

            if (null != battleHero) {// 位置上已有其他将领存在，现将该将领下阵
                if (!battleHero.isIdle()) {
                    throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "将领不是空闲状态不能操作");
                }
                if (heroRoleType == HeroConstant.HERO_ROLE_TYPE_DEPUTY && battleHero.getPartnerPosIndex() != deputyPos) {
                    throw new MwException(GameError.PARAM_ERROR.getCode(), String.format("roleId:%d, curPos:%d, changePos:%d, 替换的副将阵位与传参阵位不匹配",
                            roleId, battleHero.getPartnerPosIndex(), deputyPos));
                }
                // 检验如果替换下来的是副将, 校验换上去的武将是否与主将兵种匹配
                checkDeputyMatch(heroRoleType, heroId, roleId, partnerHero, staticHero);

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
            }

            List<CommonPb.TwoInt> seasonTalentAttr = null;
            // 将领上阵
            hero.onWall(pos);
            // 更新已上阵将领队列信息
            if (CheckNull.isNull(partnerHero)) {
                partnerHero = new PartnerHero();
                player.getPlayerFormation().getHeroWall()[pos] = partnerHero;
            }
            // 处理副将相关
            handlePartnerHero(battleHero, hero, partnerHero, staticHero, heroRoleType, deputyPos, (ass) -> {
                ass.onWall(0); // 将领下阵，pos设置为0
                ass.setPartnerPosIndex(0);
                ass.setRoleType(HeroConstant.HERO_ROLE_TYPE_NOTHING);
                CalculateUtil.processAttr(player, ass);
                builder.addDownHero(PbHelper.createHeroPb(ass, player));
            });

            // 重新调整位置
            wallService.reAdjustHeroPos(player.getPlayerFormation().getHeroWall(), player.heros);
            // 重新计算并更新将领属性
            CalculateUtil.processAttr(player, hero);
            // 重新计算并更新将领属性
            if (Objects.nonNull(battleHero)) {
                CalculateUtil.processAttr(player, battleHero);
                // 下阵
                builder.addDownHero(PbHelper.createHeroPb(battleHero, player));
            }
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
            Hero battleHero = null;
            PartnerHero partnerHero = null;
            for (int i = 1; i < player.getPlayerFormation().getHeroWall().length; i++) {
                partnerHero = player.getPlayerFormation().getHeroWall()[i];
                if (HeroUtil.isEmptyPartner(partnerHero)) continue;
                if ((battleHero = partnerHero.getCurHero(heroId)) != null) {
                    break;
                }
            }
            if (CheckNull.isNull(battleHero)) {
                throw new MwException(GameError.HERO_NOT_ON_BATTLE.getCode(), "武将不在阵上 roleId:", roleId, ", heroId:", heroId);
            }

            if (null != battleHero) {// 位置上已有其他将领存在，现将该将领下阵
                if (!battleHero.isIdle()) {
                    throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "将领不是空闲状态不能操作");
                }
                wallService.downWallHeroAndBackRes(player, battleHero);
                // 重新调整位置
                wallService.reAdjustHeroPos(player.getPlayerFormation().getHeroWall(), player.heros);
                downHero(battleHero, partnerHero, ass -> {
                    ass.onAcq(0); // 将领下阵，pos设置为0
                    ass.setPartnerPosIndex(0);
                    ass.setRoleType(HeroConstant.HERO_ROLE_TYPE_NOTHING);
                    CalculateUtil.processAttr(player, ass);
                    builder.addDownHero(PbHelper.createHeroPb(ass, player));
                });

                // 重新计算并更新将领属性
                CalculateUtil.processAttr(player, battleHero);
                // 下阵
                builder.addDownHero(PbHelper.createHeroPb(battleHero, player));
            }
        }
        for (int i = 1; i < player.getPlayerFormation().getHeroWall().length; i++) {
            PartnerHero partnerHero = player.getPlayerFormation().getHeroWall()[i];
            if (!HeroUtil.isEmptyPartner(partnerHero))
                builder.addHeroIds(partnerHero.createPb(false));
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
            PartnerHero[] heroArray = posType == HeroConstant.CHANGE_POS_TYPE ||
                    posType == HeroConstant.CHANGE_TREASURE_WARE_POS_TYPE ?
                    player.getPlayerFormation().getHeroBattle() : player.getPlayerFormation().getHeroDef();
            Set<Integer> set = Arrays.stream(heroArray).filter(heroId -> !HeroUtil.isEmptyPartner(heroId)).map(p -> p.getPrincipalHero().getHeroId()).collect(Collectors.toSet());
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
        List<Integer> posList = player.getPlayerFormation().getHeroBattlePos().get(posType);
        if (posType == HeroConstant.CHANGE_COMBAT_POS_TYPE ||
                posType == HeroConstant.CHANGE_BATTLE_POS_TYPE ||
                posType == HeroConstant.CHANGE_TREASURE_WARE_POS_TYPE) {
            if (posList == null) {
                posList = new ArrayList<>();
                player.getPlayerFormation().getHeroBattlePos().put(posType, posList);
            }
            posList.clear();
        }
        for (CommonPb.TwoInt kv : req.getHerosList()) {
            PartnerHero partnerHero = player.getPlayerFormation().getPartnerHero(kv.getV2());
            if (HeroUtil.isEmptyPartner(partnerHero)) continue;
            if (posType == HeroConstant.CHANGE_POS_TYPE) {
                partnerHero.onBattle(kv.getV1());
                player.getPlayerFormation().getHeroBattle()[kv.getV1()] = partnerHero;// 更新已上阵将领队列信息
            } else if (posType == HeroConstant.CHANGE_DEFEND_POS_TYPE) {
                partnerHero.onDef(kv.getV1());
                player.getPlayerFormation().getHeroDef()[kv.getV1()] = partnerHero;// 更新已上阵防守将领队列信息
            } else {
                posList.add(kv.getV1());
            }
            builder.addHeros(PbHelper.createHeroPb(partnerHero.getPrincipalHero(), player));
            if (CheckNull.nonEmpty(partnerHero.getDeputyHeroList())) {
                partnerHero.getDeputyHeroList().forEach(hero_ -> builder.addHeros(PbHelper.createHeroPb(hero_, player)));
            }
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
     */
    private void handlePartnerHero(Hero battleHero, Hero hero, PartnerHero partnerHero,
                                   StaticHero onBattleHero, int heroRoleType, int deputyPosIndex, Consumer<Hero> consumer) {
        // 副将处理
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
                                consumer.accept(ass);
                                it.remove();
                            }
                        }
                    }
                    partnerHero.setPrincipalHero(hero);
                    break;
                case HeroConstant.HERO_ROLE_TYPE_DEPUTY:
                    // 若当前武将上阵为替换的为副将
                    List<Hero> deputyHeroList;
                    if (CheckNull.nonEmpty(deputyHeroList = partnerHero.getDeputyHeroList()) && Objects.nonNull(battleHero)) {
                        deputyHeroList.remove(battleHero);
                    }
                    hero.setPartnerPosIndex(deputyPosIndex);
                    deputyHeroList.add(hero);
                    break;
            }
        }

        if (Objects.nonNull(battleHero)) {
            battleHero.setRoleType(HeroConstant.HERO_ROLE_TYPE_NOTHING);
        }
        hero.setRoleType(heroRoleType);
    }

    /**
     * 校验主将是否与副将兵种匹配
     *
     * @param heroRoleType
     * @param heroId
     * @param roleId
     * @param partnerHero
     * @param onBattleHero
     */
    private void checkDeputyMatch(int heroRoleType, int heroId, long roleId, PartnerHero partnerHero, StaticHero onBattleHero) {
        if (CheckNull.isNull(partnerHero)) return;

        Hero principalHero;
        if (heroRoleType == HeroConstant.HERO_ROLE_TYPE_DEPUTY && Objects.nonNull(partnerHero) &&
                Objects.nonNull(principalHero = partnerHero.getPrincipalHero())) {
            StaticHero curBattleHero = StaticHeroDataMgr.getHeroMap().get(principalHero.getHeroId());
            // 上阵的是副将, 检验当前武将是否与主将兵种匹配
            if (Objects.nonNull(curBattleHero) && Objects.nonNull(curBattleHero.getDeputyArms()) &&
                    !curBattleHero.getDeputyArms().contains(onBattleHero.getType())) {
                throw new MwException(GameError.DEPUTY_HERO_CANNOT_ON_BATTLE.getCode(), String.format("roleId:%d, curBattleHeroId:%d, onBattleHeroId:%d",
                        roleId, principalHero.getHeroId(), heroId));
            }
        }
    }

    /**
     * 采集阵容或城防武将下阵
     *
     * @param battleHero
     * @param partnerHero
     * @param consumer
     */
    private void downHero(Hero battleHero, PartnerHero partnerHero, Consumer<Hero> consumer) {
        if (CheckNull.isNull(battleHero)) return;
        switch (battleHero.getRoleType()) {
            case HeroConstant.HERO_ROLE_TYPE_PRINCIPAL:
                if (CheckNull.nonEmpty(partnerHero.getDeputyHeroList())) {
                    // 主将下阵, 副将都下阵
                    for (Hero hero : partnerHero.getDeputyHeroList()) {
                        if (CheckNull.isNull(hero)) continue;
                        consumer.accept(hero);
                        hero.setRoleType(HeroConstant.HERO_ROLE_TYPE_NOTHING);
                    }

                    partnerHero.getDeputyHeroList().clear();
                }
                partnerHero.setPrincipalHero(null);
                break;
            case HeroConstant.HERO_ROLE_TYPE_DEPUTY:
                if (CheckNull.nonEmpty(partnerHero.getDeputyHeroList())) {
                    // 副将下阵容
                    Iterator<Hero> it = partnerHero.getDeputyHeroList().iterator();
                    while (it.hasNext()) {
                        Hero hero = it.next();
                        if (CheckNull.isNull(hero) || hero.getHeroId() != battleHero.getHeroId())
                            continue;
                        it.remove();
                        break;
                    }
                }
                break;
        }

        battleHero.setRoleType(HeroConstant.HERO_ROLE_TYPE_NOTHING);
    }

    @GmCmd("heroOnBattle")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {
        if ("reset".equalsIgnoreCase(params[0])) {
            switch (params[1]) {
                case "1":
                    Arrays.stream(player.getPlayerFormation().getHeroBattle()).forEach(a -> {
                        if (HeroUtil.isEmptyPartner(a))
                            return;
                        a.getPrincipalHero().onBattle(0);
                        a.getPrincipalHero().onDef(0);
                        a.getPrincipalHero().setPartnerPosIndex(0);
                        a.getPrincipalHero().setRoleType(HeroConstant.HERO_ROLE_TYPE_NOTHING);
                        if (CheckNull.nonEmpty(a.getDeputyHeroList())) {
                            a.getDeputyHeroList().forEach(hero -> {
                                hero.onBattle(0);
                                hero.onDef(0);
                                hero.setPartnerPosIndex(0);
                                hero.setRoleType(HeroConstant.HERO_ROLE_TYPE_NOTHING);
                            });
                        }
                        a.setPrincipalHero(null);
                        a.getDeputyHeroList().clear();
                    });
                    break;
                case "2":
                    Arrays.stream(player.getPlayerFormation().getHeroAcq()).forEach(a -> {
                        if (HeroUtil.isEmptyPartner(a))
                            return;
                        a.getPrincipalHero().onAcq(0);
                        a.getPrincipalHero().setPartnerPosIndex(0);
                        a.getPrincipalHero().setRoleType(HeroConstant.HERO_ROLE_TYPE_NOTHING);
                        if (CheckNull.nonEmpty(a.getDeputyHeroList())) {
                            a.getDeputyHeroList().forEach(hero -> {
                                hero.onAcq(0);
                                hero.setPartnerPosIndex(0);
                                hero.setRoleType(HeroConstant.HERO_ROLE_TYPE_NOTHING);
                            });
                        }
                        a.setPrincipalHero(null);
                        a.getDeputyHeroList().clear();
                    });
                    break;
                case "3":
                    Arrays.stream(player.getPlayerFormation().getHeroWall()).forEach(a -> {
                        if (HeroUtil.isEmptyPartner(a))
                            return;
                        a.getPrincipalHero().onWall(0);
                        a.getPrincipalHero().setPartnerPosIndex(0);
                        a.getPrincipalHero().setRoleType(HeroConstant.HERO_ROLE_TYPE_NOTHING);
                        if (CheckNull.nonEmpty(a.getDeputyHeroList())) {
                            a.getDeputyHeroList().forEach(hero -> {
                                hero.onWall(0);
                                hero.setPartnerPosIndex(0);
                                hero.setRoleType(HeroConstant.HERO_ROLE_TYPE_NOTHING);
                            });
                        }
                        a.setPrincipalHero(null);
                        a.getDeputyHeroList().clear();
                    });
                    break;
            }
        }

        if ("resetAll".equalsIgnoreCase(params[0])) {
            player.getPlayerFormation().setHeroBattle(new PartnerHero[HeroConstant.HERO_BATTLE_LEN + 1]);
            player.getPlayerFormation().setHeroAcq(new PartnerHero[HeroConstant.HERO_BATTLE_LEN + 1]);
            player.getPlayerFormation().setHeroWall(new PartnerHero[HeroConstant.HERO_BATTLE_LEN + 1]);
            for (Hero hero : player.heros.values()) {
                if (CheckNull.isNull(hero)) continue;
                hero.onBattle(0);
                hero.onAcq(0);
                hero.onDef(0);
                hero.setPartnerPosIndex(0);
                hero.setRoleType(HeroConstant.HERO_ROLE_TYPE_NOTHING);
            }
        }
    }
}
