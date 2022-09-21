package com.gryphpoem.game.zw.service.bandit;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.dataMgr.StaticFunctionDataMgr;
import com.gryphpoem.game.zw.dataMgr.StaticWorldDataMgr;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.manager.WorldDataManager;
import com.gryphpoem.game.zw.pb.GamePb5;
import com.gryphpoem.game.zw.resource.constant.FunctionConstant;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.TrophyConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticArea;
import com.gryphpoem.game.zw.resource.domain.s.StaticBanditArea;
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
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Description:
 * Author: zhangpeng
 * createTime: 2022-09-20 15:32
 */
@Component
public class BanditService extends AbsGameService implements GmCmdService {
    /** 普通叛军*/
    private static final int COMMON_REBEL = 1;
    /** 精英叛军*/
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
        if (req.getType() != COMMON_REBEL) {
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
                    throw new MwException(GameError.SEARCH_FOR_REBELS_TOO_HIGH, "搜索叛军等级过高, searchLv:%d, maxLv:%d", req.getLevel(), staticBanditArea.getMaxlv());
                }
                int curLv = player.trophy.getOrDefault(TrophyConstant.TROPHY_1, 0);
                if (curLv + 1 < req.getLevel()) {
                    throw new MwException(GameError.SEARCH_FOR_REBELS_TOO_HIGH, "搜索叛军等级比当前玩家最高等级高, searchLv:%d, maxLv:%d", req.getLevel(), curLv);
                }
            default:
                break;
        }

        List<Integer> allPosList = MapHelper.getRoundPos(player.lord.getPos(), 10);
        if (CheckNull.isEmpty(allPosList)) {
            throw new MwException(GameError.PARAM_ERROR, String.format("未在玩家周围找到任何点位, pos:%d", player.lord.getPos()));
        }

        // 异步获取441个点位是否有符合条件的叛军
        CompletableFuture.supplyAsync(() -> allPosList.stream().filter(pos -> {
            switch (req.getType()) {
                case COMMON_REBEL:
                    return worldDataManager.getBanditIdByPos(pos) == req.getLevel();
            }
            return false;
        }).collect(Collectors.toList()), SendMsgServer.getIns().getConnectServer().sendExcutor).thenApply(posList -> {
            if (CheckNull.isEmpty(posList)) return -1;
            if (posList.size() == 1) return posList.get(0);
            return worldDataManager.nearestPos(posList, player.lord.getPos());
        }).whenComplete((pos, t) -> {
            if (t != null) {
                LogUtil.error("", t);
                handAsyncInvokeException(GameError.SERVER_EXCEPTION.getCode(), t, player);
                return;
            }

            Player player_ = DataResource.ac.getBean(PlayerDataManager.class).getPlayer(roleId);
            GamePb5.SearchBanditRs.Builder builder = GamePb5.SearchBanditRs.
                    newBuilder().
                    setType(req.getType()).
                    setLevel(req.getLevel()).setPos(pos);
            if (pos != -1) {
                // 校验当前点位上是否是对应等级的叛军
                int banditLv = worldDataManager.getBanditIdByPos(pos);
                if (banditLv != req.getLevel()) {
                    LogUtil.error(String.format("异步未找到地图上的指定叛军的点, pos:%d", pos));
                    // 随机生成点位叛军
                    builder.setPos(worldDataManager.refreshOneBanditByPlayer(10, req.getLevel(), player_));
                }
            } else {
                // 没有找到叛军, 则刷新一个叛军在地图上
                LogUtil.error(String.format("异步未找到地图上的指定叛军的点, pos:%d", pos));
                // 随机生成点位叛军
                builder.setPos(worldDataManager.refreshOneBanditByPlayer(10, req.getLevel(), player_));
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
