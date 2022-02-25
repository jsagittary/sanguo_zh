package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * 飞艇刷新规则
 * @ClassName StaticAirshipArea.java
 * @Description 飞艇刷新规则
 * @author QiuKun
 * @date 2019年1月16日
 */
public class StaticAirshipArea {

    private int keyId;
    private int areaOrder; // 对应area表的areaOrder
    private List<Integer> block;// 10*10的分块id列表，格式：[block,block...]
    private List<List<Integer>> airship;// 飞艇等级配置.格式：[[id,count]...]
    private List<Integer> effect; // 增益类型，需支持多个增益
    private int num; // 区域击杀数量，可以激活增益

    public int getKeyId() {
        return keyId;
    }

    public void setKeyId(int keyId) {
        this.keyId = keyId;
    }

    public int getAreaOrder() {
        return areaOrder;
    }

    public void setAreaOrder(int areaOrder) {
        this.areaOrder = areaOrder;
    }

    public List<Integer> getBlock() {
        return block;
    }

    public void setBlock(List<Integer> block) {
        this.block = block;
    }

    public List<List<Integer>> getAirship() {
        return airship;
    }

    public void setAirship(List<List<Integer>> airship) {
        this.airship = airship;
    }

    public List<Integer> getEffect() {
        return effect;
    }

    public void setEffect(List<Integer> effect) {
        this.effect = effect;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    @Override
    public String toString() {
        return "StaticAirshipArea{" +
                "keyId=" + keyId +
                ", areaOrder=" + areaOrder +
                ", block=" + block +
                ", airship=" + airship +
                ", effect=" + effect +
                ", num=" + num +
                '}';
    }

}
