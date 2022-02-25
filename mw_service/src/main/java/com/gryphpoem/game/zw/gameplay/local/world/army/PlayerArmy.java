package com.gryphpoem.game.zw.gameplay.local.world.army;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName PlayerArmy.java
 * @Description
 * @author QiuKun
 * @date 2019年3月21日
 */
public class PlayerArmy {
    private final long roleId; // 玩家的id
    /** 玩家在该地图的部队 <armyKeyId,army> */
    private final Map<Integer, BaseArmy> army;

    public PlayerArmy(long roleId) {
        this.roleId = roleId;
        this.army = new HashMap<>();
    }

    public long getRoleId() {
        return roleId;
    }

    public Map<Integer, BaseArmy> getArmy() {
        return army;
    }

}
