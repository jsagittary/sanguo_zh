package com.gryphpoem.game.zw.gameplay.local.util.dto;

import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.GamePb5.AttackCrossPosRs;
import com.gryphpoem.game.zw.resource.domain.Player;

import java.util.List;

/**
 * @ClassName AttackParamDto.java
 * @Description 攻击某个点的参数
 * @author QiuKun
 * @date 2019年3月23日
 */
public class AttackParamDto {

    private AttackCrossPosRs.Builder builder;
    private Player invokePlayer;
    private CrossWorldMap crossWorldMap;
    private List<Integer> heroIdList;
    private CommonPb.Army army;
    private CommonPb.Battle battle;
    /**
     * 1 闪电战，2 奔袭战，3 远征战
     */
    private int battleType;
    private int marchTime;
    /**
     * 攻击者的兵力
     */
    private int armCount;
    private int needFood;
    private int battleId;

    public AttackCrossPosRs.Builder getBuilder() {
        return builder;
    }

    public void setBuilder(AttackCrossPosRs.Builder builder) {
        this.builder = builder;
    }

    public Player getInvokePlayer() {
        return invokePlayer;
    }

    public void setInvokePlayer(Player invokePlayer) {
        this.invokePlayer = invokePlayer;
    }

    public CrossWorldMap getCrossWorldMap() {
        return crossWorldMap;
    }

    public void setCrossWorldMap(CrossWorldMap crossWorldMap) {
        this.crossWorldMap = crossWorldMap;
    }

    public List<Integer> getHeroIdList() {
        return heroIdList;
    }

    public void setHeroIdList(List<Integer> heroIdList) {
        this.heroIdList = heroIdList;
    }

    public int getBattleType() {
        return battleType;
    }

    public void setBattleType(int battleType) {
        this.battleType = battleType;
    }

    public int getMarchTime() {
        return marchTime;
    }

    public void setMarchTime(int marchTime) {
        this.marchTime = marchTime;
    }

    public int getArmCount() {
        return armCount;
    }

    public void setArmCount(int armCount) {
        this.armCount = armCount;
    }

    public int getNeedFood() {
        return needFood;
    }

    public void setNeedFood(int needFood) {
        this.needFood = needFood;
    }

    public int getBattleId() {
        return battleId;
    }

    public void setBattleId(int battleId) {
        this.battleId = battleId;
    }

    public CommonPb.Army getArmy() {
        return army;
    }

    public void setArmy(CommonPb.Army army) {
        this.army = army;
    }

    public CommonPb.Battle getBattle() {
        return battle;
    }

    public void setBattle(CommonPb.Battle battle) {
        this.battle = battle;
    }
}
