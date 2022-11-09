package com.gryphpoem.game.zw.gameplay.local.world.army;

import com.gryphpoem.game.zw.core.common.DataResource;
import com.gryphpoem.game.zw.core.util.Turple;
import com.gryphpoem.game.zw.gameplay.local.util.MapCurdEvent;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.map.BaseWorldEntity;
import com.gryphpoem.game.zw.gameplay.local.world.map.MineMapEntity;
import com.gryphpoem.game.zw.gameplay.local.world.map.WFMineMapEntity;
import com.gryphpoem.game.zw.gameplay.local.world.warfire.GlobalWarFire;
import com.gryphpoem.game.zw.gameplay.local.world.warfire.PlayerWarFire;
import com.gryphpoem.game.zw.gameplay.local.world.warfire.WarFireUtil;
import com.gryphpoem.game.zw.manager.MailDataManager;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pojo.p.FightLogic;
import com.gryphpoem.game.zw.pojo.p.Fighter;
import com.gryphpoem.game.zw.pojo.p.Force;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.constant.MailConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.util.CheckNull;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 战火燎原资源点
 *
 * @program: civilization_zh
 * @description:
 * @author: zhou jie
 * @create: 2021-01-28 21:32
 */
public class WFCollectArmy extends CollectArmy {

    public WFCollectArmy(Army army) {
        super(army);
    }

    /**
     * 重写矿点有人采集的处理, 如果正在采集是同阵营的玩家
     *
     * @param mapMarchArmy 地图上的部队相关
     * @param mineEntity   资源点
     * @param atkPlayer    进攻玩家
     */
    @Override
    protected void fightMineGuard(MapMarch mapMarchArmy, MineMapEntity mineEntity, Player atkPlayer) {
        Player defPlayer = mineEntity.getGuard().getPlayer();
        if (atkPlayer.lord.getCamp() == defPlayer.lord.getCamp()) {
            int now = TimeHelper.getCurrentSecond();
            MailDataManager mailDataManager = DataResource.ac.getBean(MailDataManager.class);
            Turple<Integer, Integer> minePos = mapMarchArmy.getCrossWorldMap().posToTurple(mineEntity.getPos());
            // 发送说明邮件
            mailDataManager.sendNormalMail(atkPlayer, MailConstant.MOLD_WAR_FIRE_SAME_CAMP_COLLECT, now,
                    mineEntity.getMineId(), minePos.getA(), minePos.getB());
            // 采集的是本阵营的玩家
            noMineRetreat(mapMarchArmy, now);
            return;
        }
        super.fightMineGuard(mapMarchArmy, mineEntity, atkPlayer);
    }


    protected void afterFightMineGuard(MapMarch mapMarch, FightLogic fightLogic, Fighter attker, Fighter defer) {
        CrossWorldMap cMap = mapMarch.getCrossWorldMap();
        GlobalWarFire globalWarFire = cMap.getGlobalWarFire();
        if (globalWarFire.getStage() == GlobalWarFire.STAGE_RUNNING) {
            BaseWorldEntity baseWorldEntity = cMap.getAllMap().get(getTargetPos());
            if (Objects.nonNull(baseWorldEntity) && baseWorldEntity instanceof WFMineMapEntity) {
                List<Force> allForce = Stream.of(attker.forces, defer.forces).flatMap(Collection::stream).collect(Collectors.toList());
                if (CheckNull.nonEmpty(allForce)) {
                    allForce.forEach(force -> {
                        long roleId = force.ownerId;
                        PlayerWarFire pwf = globalWarFire.getPlayerWarFire(roleId);
                        globalWarFire.addKillCnt(pwf, force.killed);
                    });
                    globalWarFire.logWarFireFightEvent(attker.forces.get(0), defer.forces.get(0), baseWorldEntity);
                }
            }
        }
    }


    /**
     * 采集部队正常到达, 不加采集资源
     *
     * @param mapMarchArmy 地图行军线
     * @param now          时间
     */
    @Override
    protected void retreatEnd(MapMarch mapMarchArmy, int now) {
        mapMarchArmy.finishArmy(getLordId(), getKeyId());
        // 事件通知
        mapMarchArmy.getCrossWorldMap().publishMapEvent(createMapEvent(MapCurdEvent.DELETE));
        // 还原hero的状态
        setArmyPlayerHeroState(mapMarchArmy, ArmyConstant.ARMY_STATE_IDLE);
    }

    @Override
    public void startCollectArmy(MapMarch mapMarchArmy) {
        super.startCollectArmy(mapMarchArmy);
        CrossWorldMap cMap = mapMarchArmy.getCrossWorldMap();
        BaseWorldEntity baseWorldEntity = cMap.getAllMap().get(getTargetPos());
        if (baseWorldEntity instanceof WFMineMapEntity) {
            WFMineMapEntity wfm = (WFMineMapEntity) baseWorldEntity;
            wfm.setLastScoreTime(army.getBeginTime());
            //初始化奖励, 避免采集时间不到1分钟的时候撤回部队时没有奖励(邮件)报错
            List<CommonPb.Award> grabList = WarFireUtil.createMineAwards(wfm, 0);
            army.setGrab(grabList);
        }
    }
}