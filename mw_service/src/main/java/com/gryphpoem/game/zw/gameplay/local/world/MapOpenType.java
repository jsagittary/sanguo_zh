package com.gryphpoem.game.zw.gameplay.local.world;

/**
 * @ClassName MapOpenType.java
 * @Description 地图开放程度,从外圈开始开放 -> 中圈 -> 内圈(全都地图就开放了)
 * @author QiuKun
 * @date 2019年3月26日
 */
public enum MapOpenType {

    OUTER(1, "外圈"),
    MIDDLE(2, "中圈"),
    INNER(3, "内圈");

    private int id;
    private String desc;

    private MapOpenType(int id, String desc) {
        this.id = id;
        this.desc = desc;
    }

    public int getId() {
        return id;
    }

    public String getDesc() {
        return desc;
    }

    public static MapOpenType getById(int id) {
        for (MapOpenType e : MapOpenType.values()) {
            if (e.getId() == id) {
                return e;
            }
        }
        return null;
    }
}
