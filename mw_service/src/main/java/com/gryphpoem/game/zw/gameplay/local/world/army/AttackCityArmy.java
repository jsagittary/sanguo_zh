package com.gryphpoem.game.zw.gameplay.local.world.army;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.gameplay.local.util.MapCurdEvent;
import com.gryphpoem.game.zw.gameplay.local.util.MapEvent;
import com.gryphpoem.game.zw.gameplay.local.util.dto.RetreatArmyParamDto;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.battle.BaseMapBattle;
import com.gryphpoem.game.zw.manager.MailDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.constant.MailConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.util.Turple;

/**
 * @author: ZhouJie
 * @date: Create in 2019-03-27 16:13
 * @description:
 * @modified By:
 */
public class AttackCityArmy extends BaseArmy {

    public AttackCityArmy(Army army) {
        super(army);
    }

    @Override protected void marchEnd(MapMarch mapMarchArmy, int now) {
        Player armyPlayer = checkAndGetAmryHasPlayer(mapMarchArmy);
        if (armyPlayer == null) {
            return;
        }
        CrossWorldMap cMap = mapMarchArmy.getCrossWorldMap();
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        MailDataManager mailDataManager = DataResource.ac.getBean(MailDataManager.class);

        int targetPos = army.getTarget();
        Turple<Integer, Integer> xy = cMap.posToTurple(targetPos);
        int marchTime = cMap.marchTime(cMap, armyPlayer, armyPlayer.lord.getPos(), targetPos);
        // 获得战斗类型
        Integer battleId = army.getBattleId();
        BaseMapBattle baseBattle = cMap.getMapWarData().getAllBattles().get(battleId);

        // 没有战斗就返回
        if (baseBattle == null) {
            Player tarPlayer = playerDataManager.getPlayer(army.getTarLordId());
            if (tarPlayer != null) {
                mailDataManager.sendReportMail(armyPlayer, null, MailConstant.MOLD_ATK_TARGET_NOT_FOUND, null, now,
                        xy.getA(), xy.getB(), xy.getA(), xy.getB());
            }
            retreatArmy(mapMarchArmy, marchTime, marchTime);
        }

        // 设置部队状态
        army.setState(ArmyConstant.ARMY_STATE_BATTLE);
        // 设置玩家将领状态
        setArmyPlayerHeroState(mapMarchArmy, ArmyConstant.ARMY_STATE_BATTLE);
        // 加入战斗
        baseBattle.addBattleArmy(cMap, this);

        // 事件通知
        cMap.publishMapEvent(createMapEvent(MapCurdEvent.UPDATE),
                MapEvent.mapEntity(getTargetPos(), MapCurdEvent.UPDATE));
    }

    @Override public void retreat(RetreatArmyParamDto param) {
        super.retreat(param);
        Player invokePlayer = param.getInvokePlayer();
        CrossWorldMap crossWorldMap = param.getCrossWorldMap();
        Integer battleId = army.getBattleId();
        if (null != battleId && battleId > 0) {
            BaseMapBattle baseMapBattle = crossWorldMap.getMapWarData().getAllBattles().get(battleId);
            if (baseMapBattle != null) {
                // 移除battle的兵力
                removeBattleArmy(baseMapBattle.getBattle(), invokePlayer);
            }
        }
    }
}
