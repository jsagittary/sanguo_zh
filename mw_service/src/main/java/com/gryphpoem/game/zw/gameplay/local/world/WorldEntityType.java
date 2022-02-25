package com.gryphpoem.game.zw.gameplay.local.world;

/**
 * @ClassName WorldEntityType.java
 * @Description
 * @author QiuKun
 * @date 2019年3月20日
 */
public enum WorldEntityType {
    PLAYER(1, "玩家"), BANDIT(2, "流寇"), MINE(3, "矿点"), AIRSHIP(7, "飞艇"), WAR_FIRE_SAFE_AREA(9, "战火燎原安全区"), CITY(999, "城池");

    private int type;
    private String desc;

    private WorldEntityType(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static WorldEntityType getWorldEntityTypeByTypeId(int typeId) {
        for (WorldEntityType wet : WorldEntityType.values()) {
            if (wet.getType() == typeId) {
                return wet;
            }
        }
        return null;
    }

    public int getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

}
