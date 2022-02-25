package com.gryphpoem.game.zw.gameplay.cross.serivce;

import com.google.protobuf.GeneratedMessage;
import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.eventbus.EventBus;
import com.gryphpoem.game.zw.core.eventbus.Subscribe;
import com.gryphpoem.game.zw.core.eventbus.ThreadMode;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.dataMgr.cross.StaticNewCrossDataMgr;
import com.gryphpoem.game.zw.gameplay.cross.util.CrossEntity2Dto;
import com.gryphpoem.game.zw.gameplay.local.constant.cross.CrossFunction;
import com.gryphpoem.game.zw.gameplay.local.constant.cross.NewCrossConstant;
import com.gryphpoem.game.zw.gameplay.local.manger.aop.CrossGameMapDataMgr;
import com.gryphpoem.game.zw.manager.GlobalDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.WarDataManager;
import com.gryphpoem.game.zw.manager.WorldDataManager;
import com.gryphpoem.game.zw.pb.BasePb;
import com.gryphpoem.game.zw.pb.GamePb6;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Events;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.CrossWarFireLocalData;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossGamePlayPlan;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossGroup;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.world.Battle;
import com.gryphpoem.game.zw.resource.pojo.world.CounterAttack;
import com.gryphpoem.game.zw.resource.pojo.world.GlobalRebellion;
import com.gryphpoem.game.zw.resource.util.*;
import com.gryphpoem.game.zw.service.GmCmd;
import com.gryphpoem.game.zw.service.GmCmdService;
import com.gryphpoem.game.zw.service.PlayerService;
import com.gryphpoem.game.zw.service.WorldService;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 跨服玩法
 *
 * @description:
 * @time: 2021/11/30 14:55
 */
@Service
public class CrossGamePlayService implements GmCmdService {

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private List<CrossFunctionTemplateService> functionList;

    @PostConstruct
    public void init() {
        EventBus.getDefault().register(this);
    }

    public GamePb6.GetCrossPlayerDataRs getCrossPlayerData(long roleId, int functionId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        CrossFunction crossFunction = CrossFunction.convertTo(functionId);
        GamePb6.GetCrossPlayerDataRs.Builder builder = GamePb6.GetCrossPlayerDataRs.newBuilder();
        if (CheckNull.isNull(crossFunction)) {
            return builder.build();
        }

        StaticCrossGamePlayPlan staticCrossGamePlayPlan = StaticNewCrossDataMgr.getNotOverPlan(player, functionId);
        if (CheckNull.isNull(staticCrossGamePlayPlan)) {
            return builder.build();
        }

        CrossFunctionTemplateService crossFunctionTemplateService = getActivityService(functionId);
        if (Objects.isNull(crossFunctionTemplateService)) {
            return builder.build();
        }

        Optional.ofNullable(player.crossPlayerLocalData.getCrossFunctionData(crossFunction, staticCrossGamePlayPlan.getKeyId(), false)).
                ifPresent(function -> builder.setCrossPlayerFunction(function.createPb(false)));
        builder.setCrossGamePlayConfigPb(staticCrossGamePlayPlan.createPb(functionId));
        GeneratedMessage.Builder<GamePb6.GetCrossPlayerDataRs.Builder> rsp = crossFunctionTemplateService.getCrossPlayerData(player, functionId);
        if (Objects.nonNull(rsp)) {
            builder.mergeFrom(rsp.build());
        }

        return builder.build();
    }

    /**
     * 获取跨服分组信息
     *
     * @param roleId
     * @param functionId
     * @return
     * @throws MwException
     */
    public GamePb6.GetCrossGroupInfoRs getCrossGroupInfo(long roleId, int functionId) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        CrossFunction crossFunction = CrossFunction.convertTo(functionId);

        GamePb6.GetCrossGroupInfoRs.Builder builder = GamePb6.GetCrossGroupInfoRs.newBuilder();
        if (CheckNull.isNull(crossFunction)) {
            LogUtil.error("no this cross function, functionId: ", functionId);
            return builder.build();
        }

        StaticCrossGamePlayPlan staticCrossGamePlayPlan = StaticNewCrossDataMgr.getNotOverPlan(player, functionId);
        if (CheckNull.isNull(staticCrossGamePlayPlan)) {
            LogUtil.error("no this cross function game plan, functionId: ", functionId, ", camp: ", player.getCamp());
            return builder.build();
        }

        StaticCrossGroup staticCrossGroup = StaticNewCrossDataMgr.getStaticCrossGroup(staticCrossGamePlayPlan.getGroup());
        if (CheckNull.isNull(staticCrossGroup)) {
            LogUtil.error("no this cross function group config, functionId: ", functionId, ", camp: ", player.getCamp());
            return builder.build();
        }

        try {
            staticCrossGroup.createPb(builder);
        } catch (IllegalAccessException e) {
            LogUtil.error("CrossGamePlayService", e);
        }

        return builder.build();
    }

    /**
     * 移除跨服地图焦点
     *
     * @param player
     */
    public void enterLeaveCrossMap(Player player) {
        if (CheckNull.isEmpty(functionList)) {
            LogUtil.error("CrossGamePlayService functionList is empty");
            return;
        }

        functionList.forEach(function -> {
            StaticCrossGamePlayPlan plan = null;
            try {
                plan = checkConfigOpen(player, function.getFunctionId(), true);
            } catch (MwException e) {
                LogUtil.debug("leave new cross map, e: ", e);
                return;
            }
            Optional.ofNullable(plan).ifPresent(data -> {
                DataResource.getBean(CrossGameMapDataMgr.class).enterLeaveCrossWarFire
                        (CrossEntity2Dto.createGame2CrossRequest(player, data.getKeyId()), false);
            });
        });
    }

    /**
     * 添加结束跨服活动定时器
     *
     * @param scheduler
     */
    public void initSchedule(Scheduler scheduler) {
        if (CheckNull.isEmpty(functionList)) {
            LogUtil.error("CrossGamePlayService functionList is empty");
            return;
        }

        Date now = new Date();
        Optional.ofNullable(functionList).ifPresent(list -> {
            list.forEach(function -> {
                Optional.ofNullable(StaticNewCrossDataMgr.getPlans(function.getFunctionId())).ifPresent(plans -> {
                    plans.forEach(plan -> {
                        function.addSchedule(plan, scheduler, now);
                    });
                });
            });
        });
    }

    /**
     * 执行定时任务
     *
     * @param functionId
     * @param planKey
     * @param jobName
     */
    public void executeGamePlan(int functionId, int planKey, String jobName) {
        if (CheckNull.isEmpty(functionList)) {
            LogUtil.error("CrossGamePlayService functionList is empty");
            return;
        }

        for (CrossFunctionTemplateService service : functionList) {
            if (service.getFunctionId() != functionId)
                continue;

            Optional.ofNullable(service).ifPresent(service_ -> service_.executeSchedule(planKey, jobName));
        }
    }

    /**
     * 通知活动状态
     *
     * @param gamePlanKey
     * @param stage
     * @param functionId
     */
    public void syncCrossPlanStage(int gamePlanKey, int stage, int functionId) {
        StaticCrossGamePlayPlan playPlan = StaticNewCrossDataMgr.getStaticCrossGamePlayPlan(gamePlanKey);
        if (CheckNull.isNull(playPlan)) {
            LogUtil.error("gamePlay is null, gamePlanKey: ", gamePlanKey);
        }
        if (ObjectUtils.isEmpty(playerDataManager.getAllOnlinePlayer())) {
            LogUtil.debug("onlinePlayer is null, gamePlanKey: ", gamePlanKey);
            return;
        }

        GamePb6.SyncCrossPlanStageRs.Builder builder = GamePb6.SyncCrossPlanStageRs.newBuilder().
                setStage(stage).setPlanKey(gamePlanKey).setFunctionId(functionId);
        BasePb.Base msg = PbHelper.createSynBase(GamePb6.SyncCrossPlanStageRs.EXT_FIELD_NUMBER,
                GamePb6.SyncCrossPlanStageRs.ext, builder.build()).build();
        Optional.ofNullable(playerDataManager.getAllOnlinePlayer()).ifPresent(onlineMap -> {
            onlineMap.values().forEach(player -> {
                DataResource.getBean(PlayerService.class).syncMsgToPlayer(msg, player);
            });
        });
    }

    /**
     * 跨服活动结束，退出当前跨服活动
     *
     * @param gamePlanKey
     */
    public void syncLeaveCross(int gamePlanKey) {
        StaticCrossGamePlayPlan playPlan = StaticNewCrossDataMgr.getStaticCrossGamePlayPlan(gamePlanKey);
        if (CheckNull.isNull(playPlan)) {
            LogUtil.error("gamePlay is null, gamePlanKey: ", gamePlanKey);
            return;
        }

        GamePb6.SyncLeaveCrossRs.Builder builder = GamePb6.SyncLeaveCrossRs.newBuilder();
        CrossFunction crossFunction = CrossFunction.convertTo(playPlan.getActivityType());
        Optional.ofNullable(playerDataManager.getPlayers().values()).ifPresent(players -> {
            players.forEach(player -> {
                if (!player.crossPlayerLocalData.inFunction(playPlan.getActivityType()) &&
                        player.lord.getArea() < NewCrossConstant.CROSS_WAR_FIRE_MAP)
                    return;

                LogUtil.common("player in cross, reset data, inCross: ",
                        player.crossPlayerLocalData.inFunction(playPlan.getActivityType()), ", area: ", player.lord.getArea());
                fixHeroStatus(player);
                leaveCrossMap(crossFunction, playPlan, player);
                builder.setArea(player.lord.getArea());
                builder.setPos(player.lord.getPos());

                BasePb.Base msg = PbHelper.createSynBase(GamePb6.SyncLeaveCrossRs.EXT_FIELD_NUMBER,
                        GamePb6.SyncLeaveCrossRs.ext, builder.build()).build();
                DataResource.getBean(PlayerService.class).syncMsgToPlayer(msg, player);

                builder.clear();
            });
        });
    }

    public void fixHeroStatus(Player player) {
        Optional.ofNullable(player.heroBattle).ifPresent(heroes -> {
            for (Integer heroId : heroes) {
                Hero hero = player.heros.get(heroId);
                if (CheckNull.isNull(hero))
                    continue;

                hero.setState(HeroConstant.HERO_STATE_IDLE);
            }
        });

        Optional.ofNullable(player.heroAcq).ifPresent(heroes -> {
            for (Integer heroId : heroes) {
                Hero hero = player.heros.get(heroId);
                if (CheckNull.isNull(hero))
                    continue;

                hero.setState(HeroConstant.HERO_STATE_IDLE);
            }
        });

        Optional.ofNullable(player.heroWall).ifPresent(heroes -> {
            for (Integer heroId : heroes) {
                Hero hero = player.heros.get(heroId);
                if (CheckNull.isNull(hero))
                    continue;

                hero.setState(HeroConstant.HERO_STATE_IDLE);
            }
        });
    }

    public void leaveCrossMap(CrossFunction crossFunction, StaticCrossGamePlayPlan staticCrossGamePlayPlan, Player player) {
        // 在德意志找个点迁过去
        int newPos = DataResource.getBean(WorldDataManager.class).randomKingAreaPos(player.lord.getCamp());
        int newArea = MapHelper.getAreaIdByPos(newPos);
        LogLordHelper.commonLog("crossMapLeave", AwardFrom.WAR_FIRE_LEAVE, player, player.lord.getPos(), newPos, player.lord.getArea(), newArea);
        player.lord.setPos(newPos);
        player.lord.setArea(newArea);

        // 退出地图重新计算玩家所有将领属性
        DataResource.getBean(WorldDataManager.class).putPlayer(player);
        player.crossPlayerLocalData.leaveCross(crossFunction, staticCrossGamePlayPlan.getKeyId(), staticCrossGamePlayPlan.isRunning());
        CalculateUtil.reCalcAllHeroAttr(player);
        List<Integer> posList = new ArrayList<>();
        posList.add(newPos);
        EventBus.getDefault().post(
                new Events.AreaChangeNoticeEvent(posList, player.getLordId(), Events.AreaChangeNoticeEvent.MAP_AND_AREA_TYPE));
    }

    public CrossFunctionTemplateService getActivityService(int functionId) {
        for (CrossFunctionTemplateService functionTemplateService : functionList) {
            if (functionTemplateService.getFunctionId() == functionId) {
                return functionTemplateService;
            }
        }

        return null;
    }


    /**
     * 校验活动是否开放
     *
     * @param player
     * @return
     * @throws MwException
     */
    public StaticCrossGamePlayPlan checkConfigOpen(Player player, int functionId, boolean checkIn) throws MwException {
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_CROSS_WAR_FIRE)) {
            throw new MwException(GameError.FUNCTION_LOCK.getCode(), "跨服战火燎原功能未解锁, roleId: ", player.roleId, ", level: ", player.lord.getLevel());
        }

        StaticCrossGamePlayPlan staticCrossGamePlayPlan = StaticNewCrossDataMgr.getOpenPlan(player, functionId);
        if (CheckNull.isNull(staticCrossGamePlayPlan)) {
            throw new MwException(GameError.CROSS_WAR_FIRE_CONFIG_ERROR.getCode(), "跨服战火燎原配置出错, roleId: ", player.getLordId());
        }
        if (checkIn && !player.crossPlayerLocalData.inFunction(functionId)) {
            throw new MwException(GameError.NOT_IN_CROSS_WAR_FIRE_MAP.getCode(), "不在跨服战火燎原地图中, roleId: ", player.getLordId());
        }
        if (null == CrossFunction.convertTo(functionId)) {
            throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(), "客户端请求参数错误, roleId: ", player.getLordId(), ", functionId: ", functionId);
        }
        return staticCrossGamePlayPlan;
    }

    public StaticCrossGamePlayPlan checkNotOverConfig(Player player, int functionId, boolean checkIn) throws MwException {
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.FUNC_CROSS_WAR_FIRE)) {
            throw new MwException(GameError.FUNCTION_LOCK.getCode(), "跨服战火燎原功能未解锁, roleId: ", player.roleId, ", level: ", player.lord.getLevel());
        }

        StaticCrossGamePlayPlan staticCrossGamePlayPlan = StaticNewCrossDataMgr.getNotOverPlan(player, functionId);
        if (CheckNull.isNull(staticCrossGamePlayPlan)) {
            throw new MwException(GameError.CROSS_WAR_FIRE_CONFIG_ERROR.getCode(), "跨服战火燎原配置出错, roleId: ", player.getLordId());
        }
        if (checkIn && !player.crossPlayerLocalData.inFunction(functionId)) {
            throw new MwException(GameError.NOT_IN_CROSS_WAR_FIRE_MAP.getCode(), "不在跨服战火燎原地图中, roleId: ", player.getLordId());
        }
        if (null == CrossFunction.convertTo(functionId)) {
            throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(), "客户端请求参数错误, roleId: ", player.getLordId(), ", functionId: ", functionId);
        }
        return staticCrossGamePlayPlan;
    }

    /**
     * 检测英雄可用性
     *
     * @param player
     * @param heroIdList
     * @throws MwException
     */
    public void checkHeroUseful(Player player, Collection<Integer> heroIdList, boolean attackPos) throws MwException {
        long roleId = player.roleId;
        playerDataManager.autoAddArmy(player);
        if (CheckNull.isEmpty(heroIdList)) {
            throw new MwException(GameError.ATTACK_POS_NO_HERO.getCode(), "NewCrossAttackPos 未设置将领, roleId:", roleId);
        }

        Hero hero;
        for (Integer heroId : heroIdList) {
            hero = player.heros.get(heroId);
            if (null == hero) {
                throw new MwException(GameError.HERO_NOT_FOUND.getCode(), "NewCrossAttackPos，玩家没有这个将领, roleId:", roleId,
                        ", heroId:", heroId);
            }

            if (!hero.isIdle()) {
                throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "NewCrossAttackPos，将领不在空闲中, roleId:", roleId,
                        ", heroId:", heroId, ", state:", hero.getState());
            }

            if (attackPos) {
                if (!player.isOnBattleHero(heroId) && !player.isOnAcqHero(heroId) && !player.isOnCommandoHero(heroId)) {
                    throw new MwException(GameError.HERO_NOT_BATTLE.getCode(), "NewCrossAttackPos，将领未上阵,又未在采集队列中上阵 roleId:", roleId,
                            ", heroId:", heroId);
                }

                if (!hero.isIdle()) {
                    throw new MwException(GameError.HERO_NOT_IDLE.getCode(), "NewCrossAttackPos，将领不在空闲中, roleId:", roleId,
                            ", heroId:", heroId, ", state:", hero.getState());
                }

                if (hero.getCount() <= 0) {
                    throw new MwException(GameError.HERO_NO_ARM.getCode(), "NewCrossAttackPos，将领没有带兵, roleId:", roleId, ", heroId:",
                            heroId, ", count:", hero.getCount());
                }
            }

        }
    }

    /**
     * 地图内迁城更新坐标
     *
     * @param player
     * @param functionId
     * @param newPos
     */
    public void commonMoveCity(Player player, int functionId, int newPos) {
        // 添加新位置
        player.lord.setPos(newPos);
        CrossFunction crossFunction = CrossFunction.convertTo(functionId);
        Optional.ofNullable(player.crossPlayerLocalData.getCrossFunctionData(crossFunction, 0,true)).ifPresent(crossFunctionData -> {
            switch (crossFunction) {
                case CROSS_WAR_FIRE:
                    CrossWarFireLocalData crossWarFireLocalData = (CrossWarFireLocalData) crossFunctionData;
                    crossWarFireLocalData.setPos(newPos);
                    break;
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void updateCrossStatus(Events.CrossPlayerChangeEvent event) {
        Optional.ofNullable(event.roleIds).ifPresent(playerIds -> {
            playerIds.forEach(playerId -> {
                Player player = playerDataManager.getPlayer(playerId);
                if (CheckNull.isNull(player))
                    return;

                player.crossPlayerLocalData.updateData(player, event);
            });
        });
    }

    /**
     * 进入跨服新地图后，移除匪军叛乱 或 德意志反攻期间产生的战斗
     *
     * @param player
     * @param now
     */
    public void afterEnterCrossMap(Player player, int now) {
        WarDataManager warDataManager = DataResource.getBean(WarDataManager.class);
        GlobalRebellion globalRebellion = DataResource.getBean(GlobalDataManager.class).getGameGlobal().getGlobalRebellion();
        if (globalRebellion.getCurRoundEndTime() > now && globalRebellion.getJoinRoleId().contains(player.getLordId())) {
            if (warDataManager.getRebelBattleCacheMap().containsKey(player.getLordId())) {
                Integer battleId = warDataManager.getRebelBattleCacheMap().get(player.getLordId());
                if (Objects.nonNull(battleId)) {
                    warDataManager.getBattleMap().remove(battleId);
                }
            }
        }

        CounterAttack counterAttack = DataResource.getBean(GlobalDataManager.class).getGameGlobal().getCounterAttack();
        if (counterAttack.getCurRoundEndTime() > now
                && counterAttack.getCampHitRoleId(player.lord.getCamp()).contains(player.getLordId())) {
            Optional.ofNullable(warDataManager.getSpecialBattleMap().values().stream()
                    .filter(b -> b.getType() == WorldConstant.BATTLE_TYPE_COUNTER_ATK &&
                            b.getDefencerId() == player.lord.getLordId()).map(b -> b.getBattleId())
                    .distinct().collect(Collectors.toList())).ifPresent(ids -> ids.forEach(id -> {
                Battle battle = warDataManager.getSpecialBattleMap().remove(id);
                DataResource.ac.getBean(WorldService.class).syncAttackRole(player, null, battle.getBattleTime(), WorldConstant.ATTACK_ROLE_0,
                        WorldConstant.BATTLE_TYPE_COUNTER_ATK, counterAttack.getCurrentAtkCnt(), counterAttack.getCityId());
            }));
        }
    }

    @GmCmd("newCross")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {
        String crossGmCmd = params[0];
        if (StringUtils.equals(crossGmCmd, "group")) {
            getCrossGroupInfo(player.getLordId(), 3001);
        }
    }
}
