package com.gryphpoem.game.zw.gameplay.cross.serivce.map;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.Java8Utils;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.core.util.RandomHelper;
import com.gryphpoem.game.zw.dataMgr.cross.StaticNewCrossDataMgr;
import com.gryphpoem.game.zw.gameplay.cross.serivce.CrossGamePlayService;
import com.gryphpoem.game.zw.gameplay.cross.util.CrossEntity2Dto;
import com.gryphpoem.game.zw.gameplay.local.constant.CrossWorldMapConstant;
import com.gryphpoem.game.zw.gameplay.local.constant.cross.CrossFunction;
import com.gryphpoem.game.zw.gameplay.local.constant.cross.NewCrossConstant;
import com.gryphpoem.game.zw.gameplay.local.manger.aop.CrossGameMapDataMgr;
import com.gryphpoem.game.zw.manager.*;
import com.gryphpoem.game.zw.pb.GamePb6;
import com.gryphpoem.game.zw.resource.common.ServerSetting;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.CrossFunctionData;
import com.gryphpoem.game.zw.resource.domain.s.StaticCrossGamePlayPlan;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.hero.PartnerHero;
import com.gryphpoem.game.zw.resource.pojo.world.CounterAttack;
import com.gryphpoem.game.zw.resource.pojo.world.GlobalRebellion;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.MapHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.rpc.DubboRpcService;
import com.gryphpoem.game.zw.service.GmCmd;
import com.gryphpoem.game.zw.service.GmCmdService;
import com.gryphpoem.game.zw.service.WarService;
import com.gryphpoem.game.zw.service.WorldService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GameLocal2CrossMapService implements GmCmdService {
    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private CrossGameMapDataMgr crossGameMapDataMgr;
    @Autowired
    private ServerSetting serverSetting;
    @Autowired
    private RewardDataManager rewardDataManager;
    @Autowired
    private WarService warService;
    @Autowired
    private WorldDataManager worldDataManager;
    @Autowired
    private WorldService worldService;
    @Autowired
    private MailDataManager mailDataManager;
    @Autowired
    private GlobalDataManager globalDataManager;
    @Autowired
    private VipDataManager vipDataManager;
    @Autowired
    private CrossGamePlayService crossGamePlayService;
    @Autowired
    private DubboRpcService dubboRpcService;

    /**
     * 获取跨服战火地图信息
     *
     * @param roleId
     * @param req
     * @return
     * @throws MwException
     */
    public void getNewCrossMap(long roleId, GamePb6.GetCrossWarFireMapRq req) throws Exception {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        List<Integer> cellList = req.getBlockList();
        if (ObjectUtils.isEmpty(cellList)) {
            throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(), "客户端请求参数错误, roleId: ", roleId);
        }

        StaticCrossGamePlayPlan plan = crossGamePlayService.checkConfigOpen(player, req.getFunctionId(), true);
        crossGameMapDataMgr.getCrossWarFireMap(CrossEntity2Dto.createGame2CrossRequest(player, plan.getKeyId()), cellList);
    }


    /**
     * 获取地图行军线
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public void getNewCrossMarch(long roleId, int functionId) throws Exception {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        StaticCrossGamePlayPlan plan = crossGamePlayService.checkConfigOpen(player, functionId, true);
        crossGameMapDataMgr.getAllCrossWarFireMarch(CrossEntity2Dto.createGame2CrossRequest(player, plan.getKeyId()));
    }

    /**
     * 获取所有战火燎原小地图信息
     *
     * @param roleId
     * @return
     * @throws MwException
     */
    public void getNewCrossArea(long roleId, int functionId) throws Exception {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        StaticCrossGamePlayPlan plan = crossGamePlayService.checkConfigOpen(player, functionId, true);
        crossGameMapDataMgr.getCrossWarFireArea(CrossEntity2Dto.createGame2CrossRequest(player, plan.getKeyId()));
    }

    /**
     * 跨服战火燎原迁城
     *
     * @param roleId
     * @param req
     * @return
     */
    public void newCrossMoveCity(long roleId, GamePb6.CrossWarFireMoveCityRq req) throws Exception {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        int moveType = req.getType();
        int pos = req.getPos();

        NewCrossConstant.CrossMoveCity prop = NewCrossConstant.CrossMoveCity.convertTo(moveType);
        if (CheckNull.isNull(prop) || pos < 0) {
            throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(), "客户端请求参数错误, roleId: ", roleId);
        }

        crossGamePlayService.checkHeroUseful(player, player.heros.values().stream().map(Hero::getHeroId).collect(Collectors.toList()), false);
        // 消耗的道具
        if (moveType == CrossWorldMapConstant.MOVE_CITY_TYPE_ENTER) {
            processMoveCityEnter(player, prop.getPropId(), req.getFunctionId());
        } else if (moveType == CrossWorldMapConstant.MOVE_CITY_TYPE_LEAVE) {
            processMoveCityLeave(player, req.getFunctionId());
        } else if (moveType == CrossWorldMapConstant.MOVE_CITY_TYPE_RANDOM) {
            processMoveCityRandom(player, prop.getPropId(), req.getFunctionId());
        } else if (moveType == CrossWorldMapConstant.MOVE_CITY_TYPE_POS) {
            processMoveCityPos(player, pos, prop.getPropId(), req.getFunctionId());
        } else {
            throw new MwException(GameError.PARAM_ERROR.getCode(), "跨服战火燎原迁城参数错误, roleId:", roleId, ", moveType:", moveType);
        }
//
//        GamePb6.CrossWarFireMoveCityRs.Builder builder = GamePb6.CrossWarFireMoveCityRs.newBuilder();
//        builder.setPos(player.lord.getPos());
//        if (player.props.get(prop.getPropId()) != null) {
//            builder.setProp(PbHelper.createPropPb(player.props.get(prop.getPropId())));
//        }
//        builder.setArea(player.lord.getArea());
//        return null;
    }

    /**
     * 迁入战火燎原地图
     *
     * @param player
     * @param costPropId
     */
    private void processMoveCityEnter(Player player, int costPropId, int functionId) throws Exception {
        long roleId = player.lord.getLordId();
        int prePos = player.lord.getPos();

        //请求进入跨服战火燎原地图前的校验
        StaticCrossGamePlayPlan plan = crossGamePlayService.checkConfigOpen(player, functionId, false);
        if (!CheckNull.isEmpty(player.armys)) {
            throw new MwException(GameError.MOVE_HERO_OUT.getCode(), "迁城，有将领未返回, roleId:,", roleId);
        }
        if (player.getDecisiveInfo().isDecisive()) {
            throw new MwException(GameError.DECISIVE_BATTLE_ING.getCode(), "玩家正在决战中,不能迁城, roleId:", roleId);
        }
        if (costPropId > 0) {
            rewardDataManager.checkPlayerResIsEnough(player, AwardType.PROP, costPropId, Constant.ENTER_CROSS_MAP_PROP.get(1));
        }
        // 离开的时间
        int leaveTime = player.crossPlayerLocalData.getLeaveTime(functionId, plan.getKeyId());
        if (leaveTime > 0) {
            int canEnterTime = leaveTime + WorldConstant.CROSS_WAR_FIRE_ENTER_CD;
            if (canEnterTime > TimeHelper.getCurrentSecond()) {
                throw new MwException(GameError.DO_NOT_ENTRY_WAR_FIRE_MAP_LEAVE_TIME.getCode(), "玩家退出不足15分钟, roleId:", roleId);
            }
        }

        //请求进入跨服战火燎原地图
        crossGameMapDataMgr.processMoveCityEnter(CrossEntity2Dto.createGame2CrossRequest(player, plan.getKeyId()),
                CrossEntity2Dto.uploadCrossPlayer(player, functionId, true));
        //进入地图的时候同步一次玩家数据到player 服务器
        dubboRpcService.updatePlayerLord2CrossPlayerServer(player);
    }

    /**
     * 离开战火燎原地图
     *
     * @param player
     * @throws MwException
     */
    private void processMoveCityLeave(Player player, int functionId) throws Exception {
        CrossFunction crossFunction = CrossFunction.convertTo(functionId);
        if (CheckNull.isNull(crossFunction)) {
            throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(), "客户端请求参数错误, roleId: ", player.getLordId(), ", functionId: ", functionId);
        }

        StaticCrossGamePlayPlan plan = crossGamePlayService.checkConfigOpen(player, functionId, true);
        crossGameMapDataMgr.leaveCrossMap(CrossEntity2Dto.createGame2CrossRequest(player, plan.getKeyId()));
    }

    /**
     * 在跨服战火燎原地图内随机迁城
     *
     * @param player
     * @param costPropId
     * @throws Exception
     */
    private void processMoveCityRandom(Player player, int costPropId, int functionId) throws Exception {
        //校验活动配置
        StaticCrossGamePlayPlan plan = crossGamePlayService.checkConfigOpen(player, functionId, true);
        //校验当前资源是否足够
        rewardDataManager.checkPlayerResIsEnough(player, AwardType.PROP, costPropId, 1);
        crossGameMapDataMgr.crossMoveCityInMap(CrossEntity2Dto.createGame2CrossRequest(player, plan.getKeyId()), -1, PbHelper.createAwardByte(AwardType.PROP, costPropId, 1));
    }

    /**
     * 在跨服战火燎原地图内定点迁城
     *
     * @param player
     * @param pos
     * @param costPropId
     * @throws MwException
     */
    private void processMoveCityPos(Player player, int pos, int costPropId, int functionId) throws Exception {
        //校验活动配置
        StaticCrossGamePlayPlan plan = crossGamePlayService.checkConfigOpen(player, functionId, true);
        //校验当前资源是否足够
        rewardDataManager.checkPlayerResIsEnough(player, AwardType.PROP, costPropId, 1);
        crossGameMapDataMgr.crossMoveCityInMap(CrossEntity2Dto.createGame2CrossRequest(player, plan.getKeyId()),
                pos, PbHelper.createAwardByte(AwardType.PROP, costPropId, 1));
    }

    /**
     * 进入或推出地图界面
     *
     * @param roleId
     * @param req
     * @return
     */
    public void enterLeaveNewCrossMap(long roleId, GamePb6.EnterLeaveCrossWarFireRq req) throws Exception {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        StaticCrossGamePlayPlan plan = crossGamePlayService.checkConfigOpen(player, req.getFunctionId(), false);

        crossGameMapDataMgr.enterLeaveCrossWarFire(CrossEntity2Dto.createGame2CrossRequest(player, plan.getKeyId()), req.getEnter());
    }

    /**
     * 获取跨服地图矿点西信息
     *
     * @param roleId
     * @param req
     * @return
     * @throws Exception
     */
    public void getNewCrossMine(long roleId, GamePb6.GetNewCrossMineRq req) throws Exception {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        StaticCrossGamePlayPlan plan = crossGamePlayService.checkConfigOpen(player, req.getFunctionId(), true);

        int mineKeyId = req.getMineKeyId();
        if (mineKeyId <= 0) {
            throw new MwException(GameError.ONHOOK_PARAMS_ERROR.getCode(), "客户端请求参数错误, roleId: ", roleId);
        }
        GamePb6.GetNewCrossMineRs.Builder builder = GamePb6.GetNewCrossMineRs.newBuilder();
        crossGameMapDataMgr.getNewCrossMineInfo(CrossEntity2Dto.createGame2CrossRequest(player, plan.getKeyId()), mineKeyId);
    }

    @GmCmd("crossGamePlay")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {
        String crossGmCmd = params[0];
        if (StringUtils.equals(crossGmCmd, "movecity")) {
            GamePb6.CrossWarFireMoveCityRq.Builder builder = GamePb6.CrossWarFireMoveCityRq.newBuilder();
            builder.setType(1);
            newCrossMoveCity(player.getLordId(), builder.build());
        }
        if (StringUtils.equals(crossGmCmd, "getmap")) {
            GamePb6.GetCrossWarFireMapRq.Builder builder = GamePb6.GetCrossWarFireMapRq.newBuilder();
            List<Integer> list = new ArrayList<>();
            list.add(1);
            builder.addAllBlock(list);
            getNewCrossMap(player.getLordId(), builder.build());
        }
        if (StringUtils.equals(crossGmCmd, "clearCrossWf")) {
            CrossFunction crossFunction = CrossFunction.convertTo(Integer.parseInt(params[1]));
            if (CheckNull.isNull(crossFunction)) {
                LogUtil.error("crossFunction is null");
                return;
            }

            if (player.crossPlayerLocalData.inFunction(crossFunction.getFunctionId()) ||
                    player.lord.getArea() >= NewCrossConstant.CROSS_WAR_FIRE_MAP) {
                int newPos = worldDataManager.randomKingAreaPos(player.lord.getCamp());
                player.lord.setPos(newPos);
                player.lord.setArea(MapHelper.getAreaIdByPos(newPos));

                CrossFunctionData data = player.crossPlayerLocalData.getCrossFunctionData(crossFunction, 0, false);
                player.crossPlayerLocalData.leaveCross(CrossFunction.convertTo(Integer.parseInt(params[1])), data.getPlanKey(), true);
                try {
                    crossGameMapDataMgr.leaveCrossMap(CrossEntity2Dto.createGame2CrossRequest(player, data.getPlanKey()));
                } catch (Exception e) {
                    LogUtil.error("", e);
                }
            }
        }
        if (StringUtils.equals(crossGmCmd, "enter")) {
            int count_ = 0;
            int now = TimeHelper.getCurrentSecond();
            Integer count = Integer.parseInt(params[1]);
            List<Long> players = new ArrayList<>();
            StaticCrossGamePlayPlan plan = StaticNewCrossDataMgr.getOpenPlan(player, CrossFunction.CROSS_WAR_FIRE.getFunctionId());
            if (CheckNull.isNull(plan)) {
                return;
            }

            List<Long> randomPlayers = new ArrayList<Long>(playerDataManager.getPlayers().keySet());
            for (; ; ) {
                Player tmp = playerDataManager.getPlayers().get(randomPlayers.get(RandomHelper.randomInSize(randomPlayers.size())));
                if (players.contains(tmp.getLordId()))
                    continue;
                crossGameMapDataMgr.processMoveCityEnter(CrossEntity2Dto.createGame2CrossRequest(tmp, plan.getKeyId()),
                        CrossEntity2Dto.uploadCrossPlayer(tmp, CrossFunction.CROSS_WAR_FIRE.getFunctionId(), true));
                players.add(tmp.getLordId());
                if (++count_ >= count) {
                    break;
                }
            }
        }
        if ("goToNewMap".equalsIgnoreCase(crossGmCmd)) {
            Java8Utils.syncMethodInvoke(() -> {
                worldDataManager.getPlayerInArea(WorldConstant.AREA_TYPE_13).stream().filter(player_ -> {
                    StaticCrossGamePlayPlan plan = StaticNewCrossDataMgr.getOpenPlan(player_, CrossFunction.CROSS_WAR_FIRE.getFunctionId());
                    // 匪军叛乱 或 德意志反攻期间内不让进入世界争霸
                    int now = TimeHelper.getCurrentSecond();
                    GlobalRebellion globalRebellion = globalDataManager.getGameGlobal().getGlobalRebellion();
                    CounterAttack counterAttack = globalDataManager.getGameGlobal().getCounterAttack();
                    return CheckNull.isEmpty(player_.armys) && !player_.getDecisiveInfo().isDecisive() && !(counterAttack.getCurRoundEndTime() > now
                            && counterAttack.getCampHitRoleId(player_.lord.getCamp()).contains(player_.getLordId())) && !(globalRebellion.getCurRoundEndTime()
                            > now && globalRebellion.getJoinRoleId().contains(player_.getLordId())
                            && !player_.getAndCreateRebellion().isDead()) && Objects.nonNull(plan);
                }).forEach(player_ -> {
                    StaticCrossGamePlayPlan plan = StaticNewCrossDataMgr.getOpenPlan(player_, CrossFunction.CROSS_WAR_FIRE.getFunctionId());
                    //请求进入跨服战火燎原地图
                    crossGameMapDataMgr.processMoveCityEnter(CrossEntity2Dto.createGame2CrossRequest(player_, plan.getKeyId()),
                            CrossEntity2Dto.uploadCrossPlayer(player_, CrossFunction.CROSS_WAR_FIRE.getFunctionId(), true));
                });
            });
        }
        if ("autoAtkWarFire".equalsIgnoreCase(crossGmCmd)) {
            Java8Utils.syncMethodInvoke(() -> {
                String[] cityPos = params[1].split(",");
                String[] split = params[2].split(",");
                int count = Integer.parseInt(params[3]);
                Optional.ofNullable(playerDataManager.getAllPlayer().values())
                        .ifPresent(players -> {
                            players.stream()
                                    .filter(p -> {
                                        boolean flag = false;
                                        StaticCrossGamePlayPlan plan = StaticNewCrossDataMgr.getOpenPlan(p, CrossFunction.CROSS_WAR_FIRE.getFunctionId());
                                        if (CheckNull.isNull(plan)) return false;
                                        if (!p.crossPlayerLocalData.inFunction(CrossFunction.CROSS_WAR_FIRE.getFunctionId()))
                                            return false;
                                        int force = CrossEntity2Dto.getForce(p, CrossFunction.CROSS_WAR_FIRE.getFunctionId(), serverSetting.getServerID());
                                        for (String s : split) {
                                            if (Integer.valueOf(s) == force) {
                                                flag = true;
                                                break;
                                            }
                                        }
                                        return flag;
                                    }).limit(count)
                                    .forEach(p -> {
                                        List<PartnerHero> heroes = p.getAllOnBattleHeroList();
                                        heroes.stream().filter(hero -> hero.getPrincipalHero().getState() == ArmyConstant.ARMY_STATE_IDLE)
                                                .forEach(ph -> {
                                                    try {
                                                        Hero hero = ph.getPrincipalHero();
                                                        hero.setCount(hero.getAttr()[HeroConstant.ATTR_LEAD]);
                                                        GamePb6.CrossWarFireAttackPosRq.Builder req = GamePb6.CrossWarFireAttackPosRq.newBuilder();
                                                        req.setPos(GameLocal2CrossMapService.xyToPos(Integer.parseInt(cityPos[0]), Integer.parseInt(cityPos[1])));
                                                        req.setFunctionId(CrossFunction.CROSS_WAR_FIRE.getFunctionId());
                                                        req.addHeroId(hero.getHeroId());
                                                        DataResource.getBean(Game2CrossAttackService.class).newCrossAttackPos(p.getLordId(), req.build());
                                                    } catch (Exception e) {
                                                        LogUtil.error(e, "自动进攻跨服战火燎原城池");
                                                    }
                                                });
                                    });

                        });
            });
        }
        if ("clear".equalsIgnoreCase(crossGmCmd)) {
            DataResource.getBean(CrossGamePlayService.class).syncLeaveCross(Integer.parseInt(params[1]));
        }
    }

    public static int xyToPos(int x, int y) {
        if (!checkXyIsValid(x, y)) {
            return -1;
        }
        return y * 50 + x;
    }

    public static boolean checkXyIsValid(int x, int y) {
        return x >= 0 && x < 50 && y >= 0 && y < 50;
    }
}
