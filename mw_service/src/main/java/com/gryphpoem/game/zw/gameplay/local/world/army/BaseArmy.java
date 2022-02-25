package com.gryphpoem.game.zw.gameplay.local.world.army;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.gameplay.local.util.DelayInvokeEnvironment;
import com.gryphpoem.game.zw.gameplay.local.util.DelayRun;
import com.gryphpoem.game.zw.gameplay.local.util.MapCurdEvent;
import com.gryphpoem.game.zw.gameplay.local.util.MapEvent;
import com.gryphpoem.game.zw.gameplay.local.util.dto.RetreatArmyParamDto;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.battle.BaseMapBattle;
import com.gryphpoem.game.zw.manager.PlayerDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.CommonPb.TwoInt;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.army.March;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.pojo.world.Battle;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.WorldService;

import java.util.Iterator;
import java.util.List;

/**
 * @author QiuKun
 * @ClassName BaseArmy.java
 * @Description 部队的基础类
 * @date 2019年3月21日
 */
public abstract class BaseArmy implements DelayRun {

    protected final Army army;

    public BaseArmy(Army army) {
        this.army = army;
    }

    @Override
    public int deadlineTime() {
        return army.getEndTime();
    }

    public int getState() {
        return army.getState();
    }

    public long getLordId() {
        return army.getLordId();
    }

    public int getKeyId() {
        return army.getKeyId();
    }

    public Army getArmy() {
        return army;
    }

    public boolean isInMarch() {
        return army.getState() == ArmyConstant.ARMY_STATE_MARCH || army.getState() == ArmyConstant.ARMY_STATE_RETREAT;
    }

    public int getTargetPos() {
        return army.getTarget();
    }

    public CommonPb.Army toArmyPb() {
        return PbHelper.createArmyPb(army, true);
    }

    public int getRealArmyCnt() {
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        Player player = playerDataManager.getPlayer(getArmy().getLordId());
        if (player == null) return 0;
        int sum = army.getHero().stream().mapToInt(tw -> {
            Hero hero = player.heros.get(tw.getV1());
            if (hero == null) return 0;
            return hero.getCount();
        }).sum();
        return sum;

    }

    public static BaseArmy baseArmyFactory(Army army) {
        int armyType = army.getType();
        BaseArmy baseArmy = null;
        if (armyType == ArmyConstant.ARMY_TYPE_ATK_BANDIT) {
            baseArmy = new AttackBanditArmy(army);
        } else if (armyType == ArmyConstant.ARMY_TYPE_ATK_PLAYER) {
            baseArmy = new AttackPlayerArmy(army);
        } else if (armyType == ArmyConstant.ARMY_TYPE_COLLECT) {
            baseArmy = new CollectArmy(army);
        } else if (armyType == ArmyConstant.ARMY_TYPE_ATK_CAMP) {
            baseArmy = new AttackCityArmy(army);
        } else if (armyType == ArmyConstant.ARMY_TYPE_ATTACK_AIRSHIP) {
            baseArmy = new AirshipArmy(army);
        } else if (armyType == ArmyConstant.ARMY_TYPE_NEW_YORK_WAR) {
            baseArmy = new NewYorkWarArmy(army);
        } else if (armyType == ArmyConstant.ARMY_TYPE_WF_ATK_CITY) {
            baseArmy = new AttackWFCityArmy(army);
        }
        return baseArmy;
    }

    /**
     * 主动撤回部队
     *
     * @param param
     */
    public void retreat(RetreatArmyParamDto param) {
        Player invokePlayer = param.getInvokePlayer();
        int type = param.getType();
        int now = TimeHelper.getCurrentSecond();
        CrossWorldMap crossWorldMap = param.getCrossWorldMap();
        int marchTime = crossWorldMap.marchTime(crossWorldMap, invokePlayer, invokePlayer.lord.getPos(), getTargetPos());
        if (army.getEndTime() > now && army.getState() == ArmyConstant.ARMY_STATE_MARCH) {// 折半只有在行军过程中才有
            marchTime = now + army.getDuration() - army.getEndTime();
        }
        marchTime = retreatMarchTime(param, marchTime);
        if (type == ArmyConstant.MOVE_BACK_TYPE_1) {
            marchTime = marchTime / 2;
        } else if (type == ArmyConstant.MOVE_BACK_TYPE_2) {
            marchTime = 1;
        }
        retreatArmy(crossWorldMap.getMapMarchArmy(), marchTime, marchTime);
        // 地图同步
        crossWorldMap.publishMapEvent(createMapEvent(MapCurdEvent.UPDATE));
    }

    /**
     * 主动撤回部队的行军时间,如果需要改变,子类覆写
     *
     * @param param
     * @return
     */
    protected int retreatMarchTime(RetreatArmyParamDto param, int marchTime) {
        return marchTime;
    }

    /**
     * 部队返回
     *
     * @param mapMarchArmy
     * @param duration
     * @param marchTime
     */
    public void retreatArmy(MapMarch mapMarchArmy, int duration, int marchTime) {
        Player armyPlayer = checkAndGetAmryHasPlayer(mapMarchArmy);
        if (armyPlayer == null) {
            return;
        }
        int now = TimeHelper.getCurrentSecond();
        army.setState(ArmyConstant.ARMY_STATE_RETREAT);
        army.setDuration(duration);
        int endTime = now + marchTime;
        setArmyPlayerHeroState(mapMarchArmy, ArmyConstant.ARMY_STATE_RETREAT);
        setEndTime(mapMarchArmy, endTime);
    }

    /**
     * 正常的返回
     *
     * @param mapMarchArmy
     */
    public void normalRetreatArmy(MapMarch mapMarchArmy) {
        Player armyPlayer = checkAndGetAmryHasPlayer(mapMarchArmy);
        int marchTime = mapMarchArmy.getCrossWorldMap().marchTime(mapMarchArmy.getCrossWorldMap(), armyPlayer, armyPlayer.lord.getPos(), getTargetPos());
        retreatArmy(mapMarchArmy, marchTime, marchTime);
    }

    /**
     * 检测部队是否有玩家归属,没有就直接删除
     *
     * @param mapMarchArmy
     * @return
     */
    public Player checkAndGetAmryHasPlayer(MapMarch mapMarchArmy) {
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        Player armyPlayer = playerDataManager.getPlayer(army.getLordId());
        if (armyPlayer == null) {
            mapMarchArmy.removeArmy(getLordId(), army.getKeyId());
            return null;
        }
        return armyPlayer;
    }

    /**
     * 重新设置触发时间
     *
     * @param endTime
     * @param mapMarchArmy
     */
    public final void setEndTime(MapMarch mapMarchArmy, int endTime) {
        this.army.setEndTime(endTime);
        changeDeadlineTime(mapMarchArmy.getDelayArmysQueue());
        Player armyPlayer = checkAndGetAmryHasPlayer(mapMarchArmy);
        int now = TimeHelper.getCurrentSecond();
        WorldService worldService = DataResource.ac.getBean(WorldService.class);
        worldService.synRetreatArmy(armyPlayer, army, now);
    }

    @Override
    public final void deadRun(int runTime, DelayInvokeEnvironment env) {
        if (env instanceof MapMarch) {
            MapMarch mapMarchArmy = (MapMarch) env;
            timeEndTrigger(mapMarchArmy, runTime);
        }
    }

    /**
     * 时间到了进行触发
     *
     * @param mapMarchArmy
     * @param now
     */
    private final void timeEndTrigger(MapMarch mapMarchArmy, int now) {
        if (getState() == ArmyConstant.ARMY_STATE_MARCH) {
            // 到了目的地干点嘛
            marchEnd(mapMarchArmy, now);
        } else if (getState() == ArmyConstant.ARMY_STATE_RETREAT) {
            // 返回目的地干点嘛
            retreatEnd(mapMarchArmy, now);
            PlayerArmy playerArmy = mapMarchArmy.getPlayerArmyMap().get(getLordId());
            if (playerArmy != null) {
                // 移除玩家对应的部队信息
                playerArmy.getArmy().remove(getKeyId());
            }
        } else if (getState() == ArmyConstant.ARMY_STATE_COLLECT) { // 采集结束
            finishCollect(mapMarchArmy);
        }
    }

    /**
     * 采集完成
     *
     * @param mapMarchArmy
     */
    protected void finishCollect(MapMarch mapMarchArmy) {

    }

    /**
     * 转成mapLine
     *
     * @param mapMarchArmy
     * @return
     */
    public CommonPb.MapLine toMapLinePb(MapMarch mapMarchArmy) {
        Army army = getArmy();
        PlayerDataManager playerDataManager = DataResource.ac.getBean(PlayerDataManager.class);
        Player armyP = playerDataManager.getPlayer(army.getLordId());
        if (armyP != null) {
            March march = new March(armyP, army);
            int battleType = 0;
            if (army.getBattleId() != null) {
                BaseMapBattle baseBattle = mapMarchArmy.getCrossWorldMap().getMapWarData().getAllBattles()
                        .get(army.getBattleId());
                if (baseBattle != null) {
                    battleType = baseBattle.getBattle().getBattleType();
                }
            }
            return PbHelper.createMapLinePb(march, battleType);
        }
        return null;
    }

    public MapEvent createMapEvent(MapCurdEvent curdEvent) {
        return MapEvent.mapLine(getLordId(), getKeyId(), curdEvent);
    }

    /**
     * 回到自己家干点嘛呢？默认移除部队,通知地图
     *
     * @param mapMarchArmy
     * @param now
     */
    protected void retreatEnd(MapMarch mapMarchArmy, int now) {
        mapMarchArmy.finishArmy(getLordId(), getKeyId());
        mapMarchArmy.getCrossWorldMap().publishMapEvent(createMapEvent(MapCurdEvent.DELETE)); // 事件通知
        // 还原hero的状态
        setArmyPlayerHeroState(mapMarchArmy, ArmyConstant.ARMY_STATE_IDLE);
    }

    /**
     * 设置部队所属玩家 将领的状态
     *
     * @param mapMarchArmy
     * @param state
     */
    public void setArmyPlayerHeroState(MapMarch mapMarchArmy, int state) {
        Player armyPlayer = checkAndGetAmryHasPlayer(mapMarchArmy);
        if (armyPlayer != null) {
            for (TwoInt h : army.getHero()) {
                Hero hero = armyPlayer.heros.get(h.getV1());
                if (hero != null) {
                    hero.setState(state);
                }
            }
        }
    }

    /**
     * 将玩家兵力从battle中移除
     *
     * @param battle
     * @param player
     */
    public void removeBattleArmy(Battle battle, Player player) {
        boolean isAtk = player.lord.getCamp() == battle.getAtkCamp();
        battle.updateArm(player.lord.getCamp(), -getRealArmyCnt());
        int keyId = this.getKeyId();
        Long roleId = player.roleId;
        List<CommonPb.BattleRole> list;
        if (isAtk) {
            list = battle.getAtkList();
        } else {
            list = battle.getDefList();
        }
        if (list == null || list.isEmpty()) {
            return;
        }
        LogUtil.debug("行军召回removeBattleArmy=" + list + ",roleId=" + roleId + ",keyId=" + this.getKeyId());
        Iterator<CommonPb.BattleRole> it = list.iterator();
        while (it.hasNext()) {
            CommonPb.BattleRole battleRole = it.next();
            if (battleRole.getKeyId() == keyId) {
                LogUtil.debug("removeBattleArmy,armyMap=" + battleRole);
                it.remove();
            }
        }
        // 该玩家在该battle中没有任何兵力就移除
        if (!list.stream().anyMatch(br -> br.getRoleId() == roleId)) {
            if (isAtk) {
                battle.getAtkRoles().remove(roleId);
            } else {
                battle.getDefRoles().remove(roleId);
            }
        }

    }

    /**
     * 将玩家兵力从纽约争霸battle中移除
     *
     * @param battle
     * @param player
     */
    public void removeNewYorkWarBattleArmy(Battle battle, Player player) {
        int camp = player.lord.getCamp();
        boolean isDef = camp == battle.getDefCamp();
        int minus = getRealArmyCnt();
        if (camp == battle.getDefCamp()) {
            battle.setDefArm(battle.getDefArm() - minus);
            if (battle.getDefArm() < 0) {
                battle.setDefArm(0);
            }
        } else {
            battle.setAtkArm(battle.getAtkArm() - minus);
            if (battle.getAtkArm() < 0) {
                battle.setAtkArm(0);
            }
        }
        int keyId = this.getKeyId();
        Long roleId = player.roleId;
        List<CommonPb.BattleRole> list;
        if (isDef) {
            list = battle.getDefList();
        } else {
            list = battle.getAtkList();
        }
        if (list == null || list.isEmpty()) {
            return;
        }
        LogUtil.debug("行军召回removeBattleArmy=" + list + ",roleId=" + roleId + ",keyId=" + this.getKeyId());
        Iterator<CommonPb.BattleRole> it = list.iterator();
        while (it.hasNext()) {
            CommonPb.BattleRole battleRole = it.next();
            if (battleRole.getKeyId() == keyId) {
                LogUtil.debug("removeBattleArmy,armyMap = " + battleRole);
                it.remove();
            }
        }
        // 该玩家在该battle中没有任何兵力就移除
        if (!list.stream().anyMatch(br -> br.getRoleId() == roleId)) {
            if (isDef) {
                battle.getDefRoles().remove(roleId);
            } else {
                battle.getAtkRoles().remove(roleId);
            }
        }
    }

    /**
     * 到目的地干点嘛呢？
     *
     * @param mapMarchArmy
     * @param now
     */
    protected abstract void marchEnd(MapMarch mapMarchArmy, int now);

}
