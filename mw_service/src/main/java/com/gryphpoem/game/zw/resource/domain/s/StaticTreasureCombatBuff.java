package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;
import java.util.Map;

/**
 * 宝具副本buff配置, 仅在宝具副本生效, 且不上将领面板, 不会提升战斗力
 * @description:
 * @author: zhou jie
 * @time: 2021/11/18 16:34
 */
public class StaticTreasureCombatBuff {

    private int id;
    private int armyType;
    private int armyNum;
    private Map<Integer, Integer> attr;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getArmyType() {
        return armyType;
    }

    public void setArmyType(int armyType) {
        this.armyType = armyType;
    }

    public int getArmyNum() {
        return armyNum;
    }

    public void setArmyNum(int armyNum) {
        this.armyNum = armyNum;
    }

    public Map<Integer, Integer> getAttr() {
        return attr;
    }

    public void setAttr(Map<Integer, Integer> attr) {
        this.attr = attr;
    }
}
