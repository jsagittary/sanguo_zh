package com.gryphpoem.game.zw.gameplay.local.world.army;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.gameplay.local.util.MapCurdEvent;
import com.gryphpoem.game.zw.gameplay.local.util.MapEvent;
import com.gryphpoem.game.zw.gameplay.local.util.dto.RetreatArmyParamDto;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.battle.BaseMapBattle;
import com.gryphpoem.game.zw.gameplay.local.world.map.BaseWorldEntity;
import com.gryphpoem.game.zw.manager.MailDataManager;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.constant.EffectConstant;
import com.gryphpoem.game.zw.resource.constant.MailConstant;
import com.gryphpoem.game.zw.resource.constant.WorldConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.p.Effect;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.service.WorldService;

/**
 * @author QiuKun
 * @ClassName AttackPlayerArmy.java
 * @Description
 * @date 2019年3月23日
 */
public class AttackPlayerArmy extends BaseArmy {

    public AttackPlayerArmy(Army army) {
        super(army);
    }

    @Override
    protected void marchEnd(MapMarch mapMarchArmy, int now) {
        Player armyPlayer = checkAndGetAmryHasPlayer(mapMarchArmy);
        if (armyPlayer == null) {
            return;
        }
        CrossWorldMap cMap = mapMarchArmy.getCrossWorldMap();
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        MailDataManager mailDataManager = DataResource.ac.getBean(MailDataManager.class);
        int targetPos = army.getTarget();
        int marchTime = cMap.marchTime(cMap, armyPlayer, armyPlayer.lord.getPos(), targetPos);
        // 获得战斗类型
        Integer battleId = army.getBattleId();
        BaseMapBattle baseBattle = cMap.getMapWarData().getAllBattles().get(battleId);

        if (baseBattle == null) { // 没有战斗就返回
            Player tarPlayer = playerDataManager.getPlayer(army.getTarLordId());
            if (tarPlayer != null) {
                mailDataManager.sendNormalMail(armyPlayer, MailConstant.MOLD_CITY_DEF_FLEE_ATK, now,
                        tarPlayer.lord.getNick(), tarPlayer.lord.getNick());
            }
            retreatArmy(mapMarchArmy, marchTime, marchTime);
        } else { // 有战斗情况
            int battleType = baseBattle.getBattle().getBattleType();
            BaseWorldEntity baseWorldEntity = cMap.getAllMap().get(targetPos);
            if (baseWorldEntity != null) {
                // 检测是否有保护罩
                // checkTagetPlayerHasProtect(baseBattle, now, armyPlayer, mailDataManager);
                if (baseBattle.getBattle().getDefencer() != null) {
                    Effect effect = baseBattle.getBattle().getDefencer().getEffect().get(EffectConstant.PROTECT);
                    if (effect != null && effect.getEndTime() > now) { // 对方开了保护罩
                        Player defencer = baseBattle.getBattle().getDefencer();
                        String nick = defencer.lord.getNick();
                        Turple<Integer, Integer> rPos = cMap.posToTurple(defencer.lord.getPos());
                        mailDataManager.sendNormalMail(armyPlayer, MailConstant.MOLD_ATTACK_TARGET_HAS_PROTECT, now,
                                nick, rPos.getA(), rPos.getB(), nick, rPos.getA(), rPos.getB());
                    }
                }
            }
            if (battleType == WorldConstant.CITY_BATTLE_BLITZ) { // 闪电战,部队直接返回
                // retreatArmy(mapMarchArmy, marchTime, marchTime);
            } else {// 非闪电战 就加入战斗
                // 此处不用 加入army队列中 因为在battle中会让部队返回
                army.setState(ArmyConstant.ARMY_STATE_BATTLE);
            }
            // 修改hero的state状态
            setArmyPlayerHeroState(mapMarchArmy, ArmyConstant.ARMY_STATE_BATTLE);
            // 加入战斗
            baseBattle.addBattleArmy(cMap, this);
        }
        cMap.publishMapEvent(createMapEvent(MapCurdEvent.UPDATE),
                MapEvent.mapEntity(getTargetPos(), MapCurdEvent.UPDATE)); // 事件通知
    }


    @Override
    public void retreat(RetreatArmyParamDto param) {
        super.retreat(param);
        // 打玩家
        Player invokePlayer = param.getInvokePlayer();
        // int type = param.getType();
        // int now = TimeHelper.getCurrentSecond();
        CrossWorldMap crossWorldMap = param.getCrossWorldMap();
        Integer battleId = army.getBattleId();
        if (null != battleId && battleId > 0) {
            BaseMapBattle baseMapBattle = crossWorldMap.getMapWarData().getAllBattles().get(battleId);
            if (baseMapBattle != null) {
                int camp = invokePlayer.lord.getCamp();
                int armCount = army.getArmCount();
                com.gryphpoem.game.zw.resource.pojo.world.Battle battle = baseMapBattle.getBattle();
                battle.updateArm(camp, -armCount);
                if (battle.getSponsor() != null && battle.getSponsor().roleId == invokePlayer.roleId) {
                    // 计算是否为发起者的最后一个部队
                    boolean isLastArmy = checkArmyOnPosLast(crossWorldMap.getMapMarchArmy(), this);
                    if (isLastArmy) { // 取消改点的城战
                        baseMapBattle.cancelBattleAndReturnArmy(crossWorldMap.getMapWarData(),
                                BaseMapBattle.CancelBattleType.ATKCANCEL);
                        // 军情的取消
                        WorldService worldService = DataResource.ac.getBean(WorldService.class);
                        worldService.syncAttackRole(battle.getDefencer(), invokePlayer.lord, battle.getBattleTime(),
                                WorldConstant.ATTACK_ROLE_0);
                    }
                } else {
                    // 不是发起者, 移除battle的兵力
                    removeBattleArmy(battle, invokePlayer);
                }
            }
        }

    }

    /**
     * 检测这个支部队 ，是不是这个玩家在这个点的最后一支部队
     *
     * @param mapMarch
     * @param army
     * @return true是最后一支部队
     */
    private static boolean checkArmyOnPosLast(MapMarch mapMarch, BaseArmy army) {
        PlayerArmy playerArmy = mapMarch.getPlayerArmyMap().get(army.getLordId());
        if (playerArmy != null) {
            long armyCount = playerArmy.getArmy()
                    .values().stream().filter(ba -> ba.getTargetPos() == army.getTargetPos()
                            && !ba.getArmy().isRetreat() && army.getArmy().getKeyId() != ba.getArmy().getKeyId())
                    .count();
            if (armyCount > 0) {
                return false;
            }
        }
        return true;
    }

}
