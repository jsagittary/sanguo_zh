package com.gryphpoem.game.zw.gameplay.local.manger.aop;

import com.gryphpoem.cross.gameplay.common.Game2CrossRequest;
import com.gryphpoem.cross.gameplay.map.g2c.service.Game2CrossMapService;
import com.gryphpoem.cross.gameplay.player.common.CrossPlayer;
import com.gryphpoem.game.zw.gameplay.cross.util.CrossEntity2Dto;
import com.gryphpoem.game.zw.resource.domain.Player;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
public class CrossGameMapDataMgr {

    @DubboReference(check = false, lazy = true, cluster = "failfast",
            methods = {
                    @Method(name = "enterCrossMap"),
                    @Method(name = "leaveCrossMap"),
                    @Method(name = "getCrossMap"),
                    @Method(name = "getCrossMarch"),
                    @Method(name = "getCrossArea"),
                    @Method(name = "getCrossBattle"),
                    @Method(name = "getCrossCityInfo"),
                    @Method(name = "crossMovePoint"),
                    @Method(name = "attackCrossPos"),
                    @Method(name = "joinBattleCross"),
                    @Method(name = "enterLeaveCross"),
                    @Method(name = "getCrossArmy"),
                    @Method(name = "retreatCross"),
                    @Method(name = "getCrossMilitarySituation")
            })
    private Game2CrossMapService crossWarFireMapService;

    /**
     * 从跨服获取战火燎原地图信息
     *
     * @param cellList
     * @return
     * @throws Exception
     */
    public void getCrossWarFireMap(Game2CrossRequest Game2CrossRequest, List<Integer> cellList) throws Exception {
        crossWarFireMapService.getCrossMap(Game2CrossRequest, new HashSet<>(cellList));
    }

    public void getAllCrossWarFireMarch(Game2CrossRequest Game2CrossRequest) throws Exception {
        crossWarFireMapService.getCrossMarch(Game2CrossRequest);
    }

    /**
     * 获取战火燎原小地图信息
     *
     * @return
     * @throws Exception
     */
    public void getCrossWarFireArea(Game2CrossRequest request) throws Exception {
        crossWarFireMapService.getCrossArea(request);
    }

    /**
     * 获取跨服战斗信息
     *
     * @param battleId
     * @param pos
     * @return
     * @throws Exception
     */
    public void getCrossWarFireBattle(Game2CrossRequest request, int battleId, int pos) throws Exception {
        crossWarFireMapService.getCrossBattle(request, battleId, pos);
    }

    /**
     * 进入战火燎原地图
     *
     * @param crossPlayer
     * @return
     * @throws Exception
     */
    public void processMoveCityEnter(Game2CrossRequest request, CrossPlayer crossPlayer) {
        crossWarFireMapService.enterCrossMap(request, crossPlayer);
    }

    /**
     * 离开跨服战火燎原地图
     *
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void leaveCrossMap(Game2CrossRequest request) throws Exception {
        crossWarFireMapService.leaveCrossMap(request);
    }

    /**
     * 在跨服战火燎原地图内迁城
     *
     * @param pos
     * @return
     * @throws Exception
     */
    public Integer crossMoveCityInMap(Game2CrossRequest request, int pos, List<byte[]> consume) throws Exception {
        crossWarFireMapService.crossMovePoint(request, pos, consume);

//        CrossResponse crossResponse = crossResponseCompletableFuture.get();
//        if (crossResponse.getCode() != GameError.OK.getCode()) {
//            LogUtil.error("跨服迁城出错!, code: ", crossResponse.getCode(), ", message: ", crossResponse.getMessage());
//            throw new MwException(GameError.CROSS_DATA_NULL.getCode(), "跨服数据为空, roleId:,", lordId, ", pos:", pos);
//        }
//
//        return (Integer) crossResponse.getDto();
        return null;
    }

    public void retreatCross(Game2CrossRequest request, int keyId, int type, List<byte[]> consume) throws Exception {
        crossWarFireMapService.retreatCross(request, keyId, type, consume);
    }

    /**
     * 添加移除焦点
     *
     * @param enter
     * @throws RuntimeException
     */
    public void enterLeaveCrossWarFire(Game2CrossRequest request, boolean enter) throws RuntimeException {
        crossWarFireMapService.enterLeaveCross(request, enter);
    }

    public void getCrossWarFireArmy(Game2CrossRequest request, int armyKeyId) throws Exception {
        crossWarFireMapService.getCrossArmy(request, armyKeyId);
    }

    /**
     * 获取跨服城市信息
     *
     * @param cityId
     * @return
     * @throws Exception
     */
    public void getCrossWarFireCityInfo(Game2CrossRequest request, int cityId) throws Exception {
        crossWarFireMapService.getCrossCityInfo(request, cityId);
    }

    /**
     * 攻击点位
     *
     * @param pos
     * @param type
     * @param heroIds
     * @param player
     */
    public void attackCrossWarFirePos(Game2CrossRequest request, int pos, int type, int functionId, List<Integer> heroIds, Player player) throws Exception {
        crossWarFireMapService.attackCrossPos(request, CrossEntity2Dto.createCrossAttackPos(pos, type, heroIds, player), CrossEntity2Dto.uploadCrossPlayer(player, functionId, true));
    }


    /**
     * 获取军情信息
     *
     * @return
     * @throws Exception
     */
    public void getCrossMilitarySituation(Game2CrossRequest request) throws Exception {
        crossWarFireMapService.getCrossMilitarySituation(request);
    }

    public void accelerateCrossArmy(Game2CrossRequest request, int keyId, int type, List<byte[]> consume) throws Exception {
        crossWarFireMapService.accelerateArmy(request, keyId, type, consume);
    }

    /**
     * 跨服地图获取矿点信息
     *
     * @param keyId
     * @return
     * @throws Exception
     */
    public void getNewCrossMineInfo(Game2CrossRequest request, int keyId) throws Exception {
        crossWarFireMapService.getCrossMineInfo(request, keyId);
    }

    /**
     * 上传跨服玩家数据
     *
     * @param player
     */
    public void uploadCrossPlayer(Player player) {

    }
}

