package com.gryphpoem.game.zw.resource.domain.p;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.gryphpoem.game.zw.pb.CommonPb;

/**
 * @ClassName Summon.java
 * @Description 召唤的信息
 * @author QiuKun
 * @date 2017年8月4日
 */
public class Summon {
    private int count; // 当天已召唤次数
    private int lastTime; // 最近一次召唤的时间
    private int sum;// 可召唤的总人数
    private List<Long> respondId = new CopyOnWriteArrayList<>();// 响应召唤人的id
    private int status;// 召唤状态 0.未开启召唤 ,1.召唤中

    public Summon() {

    }

    public Summon(CommonPb.Summon ser) {
        setCount(ser.getCount());
        setLastTime(ser.getLastTime());
        setSum(ser.getSum());
        setStatus(ser.getStatus());
        for (Long lordid : ser.getRespondIdList()) {
            respondId.add(lordid);
        }
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getLastTime() {
        return lastTime;
    }

    public void setLastTime(int lastTime) {
        this.lastTime = lastTime;
    }

    public List<Long> getRespondId() {
        return respondId;
    }

    public void setRespondId(List<Long> respondId) {
        this.respondId = respondId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

}
