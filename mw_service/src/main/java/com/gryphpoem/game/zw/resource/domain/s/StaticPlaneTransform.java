package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @author: ZhouJie
 * @date: Create in 2018-10-17 10:09
 * @description: 战机碎片转化配置
 * @modified By:
 */
public class StaticPlaneTransform {

    private int id;             // 唯一标识
    private int planeType;      // 战机Type
    private int quality;        // 品质
    private List<List<Integer>> transform;      // 转化的材料

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPlaneType() {
        return planeType;
    }

    public void setPlaneType(int planeType) {
        this.planeType = planeType;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public List<List<Integer>> getTransform() {
        return transform;
    }

    public void setTransform(List<List<Integer>> transform) {
        this.transform = transform;
    }
}
