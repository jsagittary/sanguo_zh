package com.gryphpoem.game.zw.gameplay.local.util.dto;

import com.gryphpoem.game.zw.gameplay.local.world.CrossWorldMap;
import com.gryphpoem.game.zw.resource.domain.Player;

/**
 * @ClassName RetreatArmyParamDto.java
 * @Description 主动撤回部队参数
 * @author QiuKun
 * @date 2019年3月27日
 */
public class RetreatArmyParamDto {
    /** 调用者 */
    private Player invokePlayer;
    /** 撤回类型 */
    private int type;
    private CrossWorldMap crossWorldMap;

    public Player getInvokePlayer() {
        return invokePlayer;
    }

    public void setInvokePlayer(Player invokePlayer) {
        this.invokePlayer = invokePlayer;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public CrossWorldMap getCrossWorldMap() {
        return crossWorldMap;
    }

    public void setCrossWorldMap(CrossWorldMap crossWorldMap) {
        this.crossWorldMap = crossWorldMap;
    }

}
