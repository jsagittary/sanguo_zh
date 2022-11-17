package com.gryphpoem.game.zw.gameplay.local.world.army;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.gameplay.local.util.DelayInvokeEnvironment;
import com.gryphpoem.game.zw.gameplay.local.util.DelayQueue;
import com.gryphpoem.game.zw.gameplay.local.util.MapCurdEvent;
import com.gryphpoem.game.zw.gameplay.local.util.MapEvent;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.WorldEntityType;
import com.gryphpoem.game.zw.gameplay.local.world.map.BaseWorldEntity;
import com.gryphpoem.game.zw.gameplay.local.world.map.PlayerMapEntity;
import com.gryphpoem.game.zw.gameplay.local.world.map.WFCityMapEntity;
import com.gryphpoem.game.zw.manager.MailDataManager;
import com.gryphpoem.game.zw.resource.constant.MailConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.army.Guard;
import com.gryphpoem.game.zw.resource.pojo.hero.Hero;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.service.WorldService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author QiuKun
 * @ClassName MapMarch.java
 * @Description 地图上的部队相关
 * @date 2019年3月21日
 */
public class MapMarch implements DelayInvokeEnvironment {

    /**
     * 地图信息
     */
    private final CrossWorldMap crossWorldMap;

    /**
     * 玩家的部队信息
     */
    private final Map<Long, PlayerArmy> playerArmyMap;
    /**
     * 部队的延迟队列
     */
    private final DelayQueue<BaseArmy> delayArmysQueue;

    public MapMarch(CrossWorldMap crossWorldMap) {
        this.crossWorldMap = crossWorldMap;
        playerArmyMap = new ConcurrentHashMap<>();
        delayArmysQueue = new DelayQueue<>(this);
    }

    public CrossWorldMap getCrossWorldMap() {
        return crossWorldMap;
    }

    public Map<Long, PlayerArmy> getPlayerArmyMap() {
        return playerArmyMap;
    }

    public BaseArmy getBaseArmyByLordIdAndKeyId(long roleId, int keyId) {
        PlayerArmy playerArmy = playerArmyMap.get(roleId);
        if (playerArmy == null) return null;
        return playerArmy.getArmy().get(keyId);
    }

    public void addArmy(BaseArmy army) {
        delayArmysQueue.add(army); // 添加到队列
        long roleId = army.getLordId();
        PlayerArmy playerArmy = playerArmyMap.computeIfAbsent(roleId, PlayerArmy::new);
        playerArmy.getArmy().put(army.getKeyId(), army);
    }

    /**
     * 一般是异常情况的移除
     *
     * @param roleId
     * @param keyId
     */
    public void removeArmy(long roleId, int keyId) {
        PlayerArmy playerArmy = playerArmyMap.get(roleId);
        if (playerArmy != null) {
            BaseArmy baseArmy = playerArmy.getArmy().remove(keyId);
            if (baseArmy != null) delayArmysQueue.remove(baseArmy);
            // 移除战火燎原的城池里指向
            if (baseArmy instanceof AttackWFCityArmy) {
                int targetPos = baseArmy.getTargetPos();
                BaseWorldEntity baseWorldEntity = crossWorldMap.getAllMap().get(targetPos);
                if (Objects.nonNull(baseWorldEntity) && baseWorldEntity.getType() == WorldEntityType.CITY) {
                    WFCityMapEntity wfCityMapEntity = (WFCityMapEntity) baseWorldEntity;
                    wfCityMapEntity.getQueue().remove(baseArmy);
                }
            }
        }
    }

    /**
     * 部队正常结束时移除
     *
     * @param roleId
     * @param keyId
     */
    public void finishArmy(long roleId, int keyId) {
        PlayerArmy playerArmy = playerArmyMap.get(roleId);
        if (playerArmy != null) {
            playerArmy.getArmy().remove(keyId);
        }
    }

    /**
     * 撤回某个点的驻防部队
     *
     * @param pos   位置
     * @param isAll true表示全部撤回, false表示没兵撤回
     */
    public void retreatGuardArmy(int pos, boolean isAll) {
        BaseWorldEntity baseWorldEntity = crossWorldMap.getAllMap().get(pos);
        if (baseWorldEntity == null || baseWorldEntity.getType() != WorldEntityType.PLAYER) {
            return;
        }
        int now = TimeHelper.getCurrentSecond();
        List<MapEvent> mapEvents = new ArrayList<>();
        PlayerMapEntity playerMapEntity = (PlayerMapEntity) baseWorldEntity;
        List<Guard> helpGuradList = playerMapEntity.getHelpGurad();
        MailDataManager mailDataManager = DataResource.ac.getBean(MailDataManager.class);
        WorldService worldService = DataResource.ac.getBean(WorldService.class);

        for (Iterator<Guard> it = helpGuradList.iterator(); it.hasNext(); ) {
            Guard guard = it.next();
            Army army = guard.getArmy();
            BaseArmy baseArmy = getBaseArmyByLordIdAndKeyId(army.getLordId(), army.getKeyId());
            if (baseArmy == null) {
                it.remove();
                continue;
            }
            Player amryPlayer = baseArmy.checkAndGetAmryHasPlayer(this);
            if (amryPlayer == null) {
                it.remove();
                continue;
            }
            int heroId = baseArmy.getArmy().getHero().get(0).getPrincipleHeroId();
            Hero hero = amryPlayer.heros.get(heroId);
            int armCount = 0;
            if (hero != null) {
                armCount = hero.getCount();
            }
            if (armCount <= 0 || isAll) {
                int marchTime = crossWorldMap.calcDistance(amryPlayer.lord.getPos(), baseArmy.getTargetPos());
                baseArmy.retreatArmy(this, marchTime, marchTime);
                String targetNick = playerMapEntity.getPlayer().lord.getNick();
                // 邮件
                mailDataManager.sendNormalMail(amryPlayer, MailConstant.WALL_HELP_KILLED, now,
                        playerMapEntity.getPlayer().lord.getNick(), heroId, targetNick, heroId);
                worldService.synWallCallBackRs(0, army);
                mapEvents.add(baseArmy.createMapEvent(MapCurdEvent.UPDATE));
                it.remove();
            }
        }
        // 通知地图
        crossWorldMap.publishMapEvent(mapEvents);
    }

    /**
     * 跑秒定时器调用
     */
    public void runSec() {
        delayArmysQueue.runSec();
    }

    public DelayQueue<BaseArmy> getDelayArmysQueue() {
        return delayArmysQueue;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public DelayQueue getDelayQueue() {
        return getDelayArmysQueue();
    }

}
