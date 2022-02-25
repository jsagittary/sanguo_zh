package com.gryphpoem.game.zw.gameplay.local.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.gryphpoem.game.zw.core.exception.MwException;
import com.gryphpoem.game.zw.core.util.Java8Utils;
import com.gryphpoem.game.zw.gameplay.local.util.MapCurdEvent;
import com.gryphpoem.game.zw.gameplay.local.util.MapEvent;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.army.PlayerArmy;
import com.gryphpoem.game.zw.gameplay.local.world.battle.BaseMapBattle.CancelBattleType;
import com.gryphpoem.game.zw.dataMgr.StaticPartyDataMgr;
import com.gryphpoem.game.zw.pb.GamePb2.SummonRespondRs;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.constant.GameError;
import com.gryphpoem.game.zw.resource.constant.PartyConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Summon;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.service.WorldService;

/**
 * @ClassName CrossSummonService.java
 * @Description 新地图地图召唤
 * @author QiuKun
 * @date 2019年5月5日
 */
@Component
public class CrossSummonService {
    @Autowired
    private CrossWorldMapService crossWorldMapService;
    @Autowired
    private WorldService worldService;

    /**
     * 响应召唤
     * 
     * @param invokePlayer
     * @param summonPlayer
     * @return
     * @throws MwException
     */
    public SummonRespondRs summonRespond(Player invokePlayer, Player summonPlayer) throws MwException {
        int area = summonPlayer.lord.getArea();
        CrossWorldMap cMap = crossWorldMapService.checkCrossWorldMap(invokePlayer, area);
        PlayerArmy playerArmy = cMap.getMapMarchArmy().getPlayerArmyMap().get(invokePlayer.roleId);
        if (playerArmy != null && !CheckNull.isEmpty(playerArmy.getArmy())) {
            throw new MwException(GameError.MOVE_HERO_OUT.getCode(), "响应召唤，有将领未返回, roleId:,", invokePlayer.roleId);
        }
        List<Integer> scope = cMap.getRoundPos(summonPlayer.lord.getPos(), Constant.SUMMON_RESPOND_RADIUS);
        if (scope.contains(invokePlayer.lord.getPos())) {
            // 距离对方距离太近禁止迁城
            throw new MwException(GameError.SUMMON_DISTANCE_TOO_CLOSE.getCode(), "距离太近不能响应召唤");
        }
        List<Integer> scope2 = scope.stream().filter(pos -> cMap.isEmptyPos(pos)).collect(Java8Utils.toShuffledList());
        if (CheckNull.isEmpty(scope2)) {
            throw new MwException(GameError.SUMMON_NOT_POS.getCode(), "对方周围没有空位");
        }

        // 给召唤者添加人数
        Summon summon = summonPlayer.summon;
        List<Integer> val = StaticPartyDataMgr.getJobPrivilegeVal(summonPlayer.lord.getJob(),
                PartyConstant.PRIVILEGE_CALL);
        int sum = val.get(2); // 可召唤的人数
        summon.getRespondId().add(invokePlayer.roleId);
        if (summon.getRespondId().size() >= sum) { // 如果满了就结束
            summon.setStatus(0);
            summon.getRespondId().clear();
        }
        // 推送召唤数据
        worldService.syncSummonState(summonPlayer, summonPlayer.lord.getLordId(), summon);
        // 迁城逻辑
        int newPos = scope2.get(0);
        CrossWorldMapService.commonMoveCity(invokePlayer, cMap, newPos, CancelBattleType.DEFMOVECITY);
        SummonRespondRs.Builder builder = SummonRespondRs.newBuilder();
        builder.setPos(newPos);
        cMap.publishMapEvent(MapEvent.mapEntity(summonPlayer.lord.getPos(), MapCurdEvent.UPDATE));
        return builder.build();
    }

}
