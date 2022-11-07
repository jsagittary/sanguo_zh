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

    private Integer startTime; // 周期开始时间

    private Integer endTime; // 周期结束时间

    private Integer totalCountCurPeriod; // 当前周期内累计事件数量

    private Integer periodCount; // 累计周期数

    private List<LifeSimulatorInfo> lifeSimulatorInfoList = new ArrayList<>(); // 周期内的城镇事件(模拟器信息)

    public CityEvent() {
        this.startTime = 0;
        this.endTime = 0;
        this.totalCountCurPeriod = 0;
        this.periodCount = 0;
        this.lifeSimulatorInfoList = new ArrayList<>();
    }

    public Integer getStartTime() {
        return startTime;
    }

    public void setStartTime(Integer startTime) {
        this.startTime = startTime;
    }

    public Integer getEndTime() {
        return endTime;
    }

    public void setEndTime(Integer endTime) {
        this.endTime = endTime;
    }

    public Integer getTotalCountCurPeriod() {
        return totalCountCurPeriod;
    }

    public void setTotalCountCurPeriod(Integer totalCountCurPeriod) {
        this.totalCountCurPeriod = totalCountCurPeriod;
    }

    public Integer getPeriodCount() {
        return periodCount;
    }

    public void setPeriodCount(Integer periodCount) {
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
