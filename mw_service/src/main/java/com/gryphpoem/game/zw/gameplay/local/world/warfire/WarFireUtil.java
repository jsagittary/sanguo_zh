package com.gryphpoem.game.zw.gameplay.local.world.warfire;

import com.gryphpoem.game.zw.core.util.LogUtil;
import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.gameplay.local.world.army.AttackWFCityArmy;
import com.gryphpoem.game.zw.gameplay.local.world.army.PlayerArmy;
import com.gryphpoem.game.zw.gameplay.local.world.map.BaseWorldEntity;
import com.gryphpoem.game.zw.gameplay.local.world.map.CityMapEntity;
import com.gryphpoem.game.zw.gameplay.local.world.map.WFCityMapEntity;
import com.gryphpoem.game.zw.gameplay.local.world.map.WFMineMapEntity;
import com.gryphpoem.game.zw.dataMgr.StaticCrossWorldDataMgr;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.constant.ArmyConstant;
import com.gryphpoem.game.zw.resource.domain.Player;
import com.gryphpoem.game.zw.resource.domain.s.StaticMine;
import com.gryphpoem.game.zw.resource.domain.s.StaticWarFire;
import com.gryphpoem.game.zw.resource.pojo.army.Army;
import com.gryphpoem.game.zw.resource.pojo.army.Guard;
import com.gryphpoem.game.zw.resource.pojo.fight.Fighter;
import com.gryphpoem.game.zw.resource.pojo.fight.Force;
import com.gryphpoem.game.zw.resource.pojo.world.City;
import com.gryphpoem.game.zw.resource.util.PbHelper;
import com.gryphpoem.game.zw.resource.util.TimeHelper;
import com.gryphpoem.game.zw.resource.util.Turple;
import com.hundredcent.game.ai.util.CheckNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 战火燎原工具
 *
 * @Description
 * @Author zhangdh
 * @Date 2021-01-07 21:00
 */
public final class WarFireUtil {
    /**
     * 计算玩家每分钟的(据点+采集点)产出
     *
     * @param gwf GlobalWarFire
     * @return 玩家每分钟的积分产出
     */
    public static int getPlayerOutputMin(GlobalWarFire gwf, Player player) {
        CrossWorldMap cMap = gwf.getCrossWorldMap();
        PlayerArmy playerArmy = cMap.getMapMarchArmy().getPlayerArmyMap().get(player.getLordId());
        if (Objects.isNull(playerArmy) || CheckNull.isEmpty(playerArmy.getArmy())) return 0;
        int outputMin = 0;//统计产出
        //矿点计算
        for (Map.Entry<Integer, BaseWorldEntity> entry : cMap.getAllMap().entrySet()) {
            BaseWorldEntity baseWorldEntity = entry.getValue();
            if (baseWorldEntity instanceof WFMineMapEntity) {
                WFMineMapEntity wfm = (WFMineMapEntity) baseWorldEntity;
                Guard guard = wfm.getGuard();
                Army army = guard != null ? guard.getArmy() : null;
                if (Objects.nonNull(army) && army.getLordId() == player.getLordId()) {
                    StaticMine staticMine = wfm.getCfgMine();
                    outputMin += staticMine.getSpeed() / 60;
                }
            }
        }
        int nowSec = TimeHelper.getCurrentSecond();
        //据点计算
        for (Map.Entry<Integer, CityMapEntity> entry : cMap.getCityMap().entrySet()) {
            WFCityMapEntity wfCity = (WFCityMapEntity) entry.getValue();
            if (wfCity.getStatus() == WFCityMapEntity.WF_CITY_STATUS_2) {
                StaticWarFire swf = StaticCrossWorldDataMgr.getStaticWarFireMap().get(wfCity.getCity().getCityId());
                if (Objects.nonNull(swf)) {
                    List<AttackWFCityArmy> cityArmies = wfCity.getArmysInCity(player);
                    if (!CheckNull.isEmpty(cityArmies)) {
                        AttackWFCityArmy guardArmy = cityArmies.stream()
                                .filter(wfcArmy -> nowSec >= wfcArmy.getArmy().getEndTime() && wfcArmy.getState() == ArmyConstant.ARMY_STATE_WAR_FIRE_CITY)
                                .findFirst().orElse(null);
                        if (Objects.nonNull(guardArmy)) {
                            outputMin += swf.getPersonContinue();
                        }
                    }
                }
            }
        }
        return outputMin;
    }

    /**
     * 分组统计所有阵营每分钟的产量
     *
     * @param gwf GlobalWarFire
     * @return KEY:阵营ID, VALUE:每分钟的产量
     */
    public static Map<Integer, Integer> calcCampOutputMin(GlobalWarFire gwf) {
        CrossWorldMap crossWorldMap = gwf.getCrossWorldMap();
        return crossWorldMap.getCityMap().values().stream()
                .filter(entity -> entity.getCity().getCamp() > 0)
                .collect(Collectors.toMap(entity -> entity.getCity().getCamp(), entity -> {
                    City city = entity.getCity();
                    int cityId = city.getCityId();
                    StaticWarFire staticWarFire = StaticCrossWorldDataMgr.getStaticWarFireMap().get(cityId);
                    if (staticWarFire == null) {
                        LogUtil.error("未找到配置信息 : cityId : ", cityId);
                        return 0;
                    }
                    return staticWarFire.getCampContinue();
                }, Integer::sum));

    }


    /**
     * 创建战火燎原矿点资源采集奖励
     *
     * @param wfm       矿点对象
     * @param grabCount 采集数量
     * @return
     */
    public static List<CommonPb.Award> createMineAwards(WFMineMapEntity wfm, int grabCount) {
        StaticMine staticMine = wfm.getCfgMine();
        List<List<Integer>> rewards = staticMine.getReward();
        List<Integer> award = rewards.get(0);
        CommonPb.Award grabPb = PbHelper.createAwardPb(award.get(0), award.get(1), grabCount);
        List<CommonPb.Award> grabPbList = new ArrayList<>(1);
        grabPbList.add(grabPb);
        return grabPbList;
    }

    /**
     * 计算杀敌与损兵
     * @param fighter
     * @return KEY:roleId,VALUE: 杀敌与损兵
     */
    public static Map<Long, Turple<Integer, Integer>> calcKillAndLost(Fighter fighter) {
        Map<Long, List<Force>> roleMap = fighter.forces.stream().collect(Collectors.groupingBy(force -> force.ownerId));
        Map<Long, Turple<Integer, Integer>> turpleMap = new HashMap<>();
        if (Objects.nonNull(roleMap)) {
            for (Map.Entry<Long, List<Force>> entry : roleMap.entrySet()) {
                int totalKilled = 0, totalLost = 0;
                for (Force force : entry.getValue()) {
                    totalKilled += force.killed;
                    totalLost += force.totalLost;
                }
                turpleMap.put(entry.getKey(), new Turple<>(totalKilled, totalLost));
            }
        }
        return turpleMap;
    }
}
