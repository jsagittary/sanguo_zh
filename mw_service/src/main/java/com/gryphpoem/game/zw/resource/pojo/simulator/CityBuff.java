package com.gryphpoem.game.zw.resource.pojo.simulator;

/**
 * 模拟器获得的Buff, 也可用于记录其他buff效果
 *
 * @Author: GeYuanpeng
 * @Date: 2022/12/3 18:08
 */
public class CityBuff {

    private int type; // 对应s_city_buff的type

    private int addOrSub; // 1-增益; 2-减益

    private int value; // buff增益值

    private int valueType; // 1-固定值; 2-万分比

    private int startTime; // buff开始生效时间

    private int endTime; // buff结束时间, -1表示永久持续

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getAddOrSub() {
        return addOrSub;
    }

    public void setAddOrSub(int addOrSub) {
        this.addOrSub = addOrSub;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getValueType() {
        return valueType;
    }

    public void setValueType(int valueType) {
        this.valueType = valueType;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

}
