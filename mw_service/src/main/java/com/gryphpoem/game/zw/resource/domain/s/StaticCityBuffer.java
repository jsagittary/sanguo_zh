package com.gryphpoem.game.zw.resource.domain.s;

import com.gryphpoem.game.zw.resource.constant.Constant;

/**
 * @author: ZhouJie
 * @date: Create in 2018-08-21 17:44
 * @description: 名城Buffer
 * @modified By:
 */
public class StaticCityBuffer {

    private int id;
    /**
     * 增益类型<br>
     * 1.燃油采集产量提升<br>
     * 2.电能采集产量提升<br>
     * 3.补给采集产量提升<br>
     * 4.矿石采集产量提升<br>
     * 5.上阵将领攻击提升<br>
     * 6.上阵将领防御提升<br>
     * 7.行军时间减少<br>
     * 8.击败流寇，获取的资源增益<br>
     * 9.军事禁区buff：行军时间增加20%<br>
     * 10.建筑建造时间缩短10%<br>
     * 10.科技研究时间缩短10%<br>
     */
    private int type;
    private int sort;           // 1 万份比, 2 固定值
    private int value;          // 数值

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    /**
     * 加成的值, sort 1 万份比, 2 固定值
     * @return
     */
    public double getBuff() {
        return sort == 2 ? value : value / Constant.TEN_THROUSAND;
    }
}
