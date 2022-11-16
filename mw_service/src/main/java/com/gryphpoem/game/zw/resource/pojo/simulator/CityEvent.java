package com.gryphpoem.game.zw.resource.pojo.simulator;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.pb.SerializePb;
import com.gryphpoem.game.zw.resource.util.CheckNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 周期性城镇事件
 *
 * @Author: GeYuanpeng
 * @Date: 2022/10/28 14:14
 */
public class CityEvent {

    private int startTime; // 周期开始时间

    private int endTime; // 周期结束时间

    private int totalCountCurPeriod; // 当前周期内累计事件数量

    private int periodCount; // 累计周期数

    private List<LifeSimulatorInfo> lifeSimulatorInfoList = new ArrayList<>(); // 周期内的城镇事件(模拟器信息)

    public CityEvent() {
        this.startTime = 0;
        this.endTime = 0;
        this.totalCountCurPeriod = 0;
        this.periodCount = 0;
        this.lifeSimulatorInfoList = new ArrayList<>();
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

    public int getTotalCountCurPeriod() {
        return totalCountCurPeriod;
    }

    public void setTotalCountCurPeriod(int totalCountCurPeriod) {
        this.totalCountCurPeriod = totalCountCurPeriod;
    }

    public int getPeriodCount() {
        return periodCount;
    }

    public void setPeriodCount(int periodCount) {
        this.periodCount = periodCount;
    }

    public List<LifeSimulatorInfo> getLifeSimulatorInfoList() {
        return lifeSimulatorInfoList;
    }

    public void setLifeSimulatorInfoList(List<LifeSimulatorInfo> lifeSimulatorInfoList) {
        this.lifeSimulatorInfoList = lifeSimulatorInfoList;
    }

    public SerializePb.CityEvent ser() {
        SerializePb.CityEvent.Builder builder = SerializePb.CityEvent.newBuilder();
        builder.setStartTime(this.startTime);
        builder.setEndTime(this.endTime);
        builder.setTotalCountCurPeriod(this.totalCountCurPeriod);
        builder.setPeriodCount(this.periodCount);
        for (LifeSimulatorInfo lifeSimulatorInfo : this.lifeSimulatorInfoList) {
            builder.addLifeSimulatorInfo(lifeSimulatorInfo.ser());
        }
        return builder.build();
    }

    public CityEvent dser(SerializePb.CityEvent pb) {
        this.startTime = pb.getStartTime();
        this.endTime = pb.getEndTime();
        this.totalCountCurPeriod = pb.getTotalCountCurPeriod();
        this.periodCount = pb.getPeriodCount();
        if (CheckNull.nonEmpty(pb.getLifeSimulatorInfoList())) {
            for (CommonPb.LifeSimulatorInfo lifeSimulatorInfo : pb.getLifeSimulatorInfoList()) {
                this.lifeSimulatorInfoList.add(new LifeSimulatorInfo().dser(lifeSimulatorInfo));
            }
        }
        return this;
    }
}
