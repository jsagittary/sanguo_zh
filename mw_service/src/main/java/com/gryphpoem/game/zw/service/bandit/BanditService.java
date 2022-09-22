package com.gryphpoem.game.zw.service.bandit;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.WorldDataManager;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.resource.constant.*;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticAirship;
import com.gryphpoem.game.zw.resource.domain.s.StaticArea;
import com.gryphpoem.game.zw.resource.domain.s.StaticBanditArea;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.army.March;
import com.gryphpoem.game.zw.resource.pojo.world.AirshipWorldData;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.MapHelper;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.server.SendMsgServer;
import com.gryphpoem.game.zw.service.AbsGameService;
import com.gryphpoem.game.zw.service.GmCmd;
import com.gryphpoem.game.zw.service.GmCmdService;
import com.gryphpoem.game.zw.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-09-20 15:32
 */
@Component
public class BanditService extends AbsGameService implements GmCmdService {
    /**
     * 普通叛军
     */
    private static final int COMMON_REBEL = 1;
    /**
     * 精英叛军
     */
    private static final int ELITE_REBELS = 2;

    @Autowired
    private PlayerDataManager playerDataManager;
    @Autowired
    private WorldDataManager worldDataManager;
    @Autowired
    private PlayerService playerService;

    /**
     * 在地图内搜索普通叛军
     *
     * @param roleId
     * @param req
     * @throws MwException
     */
    public void searchBandit(long roleId, GamePb5.SearchBanditRq req) throws MwException {
        Player player = playerDataManager.checkPlayerIsExist(roleId);
        if (player.lord.getPos() < 0) {
            throw new MwException(GameError.PARAM_ERROR, String.format("roleId:%d, 还未进入世界地图", roleId));
        }
        if (req.getLevel() <= 0) {
            throw new MwException(GameError.PARAM_ERROR, String.format("roleId:%d, 搜索叛军等级参数错误, req:%d", roleId, req.getLevel()));
        }
        if (!StaticFunctionDataMgr.funcitonIsOpen(player, FunctionConstant.SEARCH_BANDIT)) {
            throw new MwException(GameError.FUNCTION_LOCK, String.format("roleId:%d, function lock", roleId));
        }
        if (req.getType() != COMMON_REBEL && req.getType() != ELITE_REBELS) {
            throw new MwException(GameError.PARAM_ERROR, String.format("roleId:%d, 发送参数错误, req:%d", req.getType()));
        }

        StaticArea staticArea = StaticWorldDataMgr.getAreaMap().get(player.lord.getArea());
        if (staticArea == null) {
            throw new MwException(GameError.NO_CONFIG, String.format("roleId:%d, area:%d 未找到配置", roleId, player.lord.getArea()));
        }
        StaticBanditArea staticBanditArea = worldDataManager.getStaticBanditAreaByWorldProgress(staticArea.getOpenOrder());
        if (staticBanditArea == null) {
            throw new MwException(GameError.NO_CONFIG, String.format("roleId:%d, openOrder:%d 未找到配置", roleId, staticArea.getOpenOrder()));
        }
        switch (req.getType()) {
            case COMMON_REBEL:
                if (req.getLevel() > staticBanditArea.getMaxlv()) {
                    throw new MwException(GameError.SEARCH_FOR_REBELS_TOO_HIGH, String.format("搜索叛军等级过高, searchLv:%d, maxLv:%d", req.getLevel(), staticBanditArea.getMaxlv()));
                }
                int curLv = player.trophy.getOrDefault(TrophyConstant.TROPHY_1, 0);
                if (curLv + 1 < req.getLevel()) {
                    throw new MwException(GameError.SEARCH_FOR_REBELS_TOO_HIGH, String.format("搜索叛军等级比当前玩家最高等级高, searchLv:%d, maxLv:%d", req.getLevel(), curLv));
                }
            case ELITE_REBELS:
                if (CheckNull.isEmpty(worldDataManager.getAllAirshipWorldData())) {
                    throw new MwException(GameError.SEARCH_NO_ELITE_REBELS, "没有搜索到精英叛军, req:%d", req.getLevel());
                }
        }

        // 异步获取441个点位是否有符合条件的叛军
        CompletableFuture.supplyAsync(() -> {
                    List<Integer> posList = MapHelper.getRoundPos(player.lord.getPos(), WorldConstant.SEARCH_THE_RANGE_OF_THE_REBELS);
                    if (CheckNull.isEmpty(posList))
                        return null;

                    // 搜索普通叛军时, 若有前往该点的行军线
                    Map<Integer, Army> armyMap = null;
                    if (req.getType() == COMMON_REBEL) {
                        Map<Integer, March> marchMap = worldDataManager.getMarchMap().get(player.lord.getArea());
                        if (Objects.nonNull(marchMap)) {
                            armyMap = marchMap.values().stream().map(March::getArmy).filter(
                                            army -> Objects.nonNull(army) && army.getState() != ArmyConstant.ARMY_STATE_RETREAT).
                                    collect(Collectors.toMap(Army::getTarget, Function.identity(), (key1, key2) -> key2));
                        }
                    }

                    Map<Integer, Army> finalArmyMap = armyMap;
                    return posList.stream().filter(pos -> {
                                switch (req.getType()) {
                                    case COMMON_REBEL:
                                        if (worldDataManager.getBanditIdByPos(pos) != req.getLevel())
                                            return false;

                                        Army army;
                                        if (Objects.nonNull(finalArmyMap) && (army = finalArmyMap.get(pos)) != null) {
                                            if (army.getState() != ArmyConstant.ARMY_STATE_RETREAT) return false;
                                        }
                                        return true;
                                    case ELITE_REBELS:
                                        AirshipWorldData airshipWorld = worldDataManager.getAirshipWorldDataMap().get(pos);
                                        if (CheckNull.isNull(airshipWorld)) return false;
                                        StaticAirship sAirship = StaticWorldDataMgr.getAirshipMap().get(airshipWorld.getId());
                                        if (CheckNull.isNull(sAirship)) return false;
                                        return req.getLevel() == sAirship.getLv();
                                    default:
                                        return false;
                                }
                            }
                    ).collect(Collectors.toList());
                }
                , SendMsgServer.getIns().getConnectServer().sendExcutor).thenApply(posList -> {
            if (posList == null)
                throw new MwException(GameError.PARAM_ERROR, String.format("玩家点位周围没有点位, pos:%d", player.lord.getPos()));
            // 查找离玩家最近的点位
            if (CheckNull.isEmpty(posList)) return -1;
            if (posList.size() == 1) return posList.get(0);
            return worldDataManager.nearestPos(posList, player.lord.getPos());
        }).whenComplete((pos, t) -> {
            Player player_ = DataResource.ac.getBean(PlayerDataManager.class).getPlayer(roleId);
            if (t != null) {
                LogUtil.error("", t);
                handAsyncInvokeException(GamePb5.SearchBanditRs.EXT_FIELD_NUMBER, t, player_);
                return;
            }

            GamePb5.SearchBanditRs.Builder builder = GamePb5.SearchBanditRs.
                    newBuilder().
                    setType(req.getType()).
                    setLevel(req.getLevel()).setPos(pos);
            if (pos != -1) {
                switch (req.getType()) {
                    case COMMON_REBEL:
                        // 校验当前点位上是否是对应等级的叛军
                        int banditLv = worldDataManager.getBanditIdByPos(pos);
                        if (banditLv != req.getLevel()) {
                            LogUtil.error(String.format("异步找到地图上的指定叛军的点不存在, pos:%d", pos));
                            // 随机生成点位叛军
                            builder.setPos(worldDataManager.refreshOneBanditByPlayer(WorldConstant.SEARCH_THE_RANGE_OF_THE_REBELS, req.getLevel(), player_));
                        }
                        break;
                    case ELITE_REBELS:
                        AirshipWorldData airshipWorld = worldDataManager.getAirshipWorldDataMap().get(pos);
                        if (CheckNull.isNull(airshipWorld)) {
                            LogUtil.error(String.format("异步查找的精英叛军不存在, role:%d, pos:%d", roleId, pos));
                            builder.setPos(-1);
                            break;
                        }
                        StaticAirship sAirship = StaticWorldDataMgr.getAirshipMap().get(airshipWorld.getId());
                        if (CheckNull.isNull(sAirship)) {
                            LogUtil.error(String.format("异步查找的精英叛军配置不存在, role:%d, pos:%d", roleId, pos));
                            builder.setPos(-1);
                            break;
                        }
                        if (sAirship.getLv() != req.getLevel()) {
                            LogUtil.error(String.format("异步查找的精英叛军等级不匹配, role:%d, req:%d, lv:%d", roleId, req.getLevel(), sAirship.getLv()));
                            builder.setPos(-1);
                            break;
                        }
                        break;
                }
            } else {
                // 没有找到叛军或精英叛军
                LogUtil.error(String.format("异步未找到地图上的指定叛军的点, pos:%d", pos));
                switch (req.getType()) {
                    case COMMON_REBEL:
                        // 刷新一个叛军在地图上
                        // 随机生成点位叛军
                        builder.setPos(worldDataManager.refreshOneBanditByPlayer(WorldConstant.SEARCH_THE_RANGE_OF_THE_REBELS, req.getLevel(), player_));
                        break;
                    case ELITE_REBELS:
                        break;
                }
            }

            playerService.syncMsgToPlayer(PbHelper.createRsBase(
                    GamePb5.SearchBanditRs.EXT_FIELD_NUMBER, GamePb5.SearchBanditRs.ext, builder.build()).build(), player_);
        });
    }

    @GmCmd("bandit")
    @Override
    public void handleGmCmd(Player player, String... params) throws Exception {

    }
}
