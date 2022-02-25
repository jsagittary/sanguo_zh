package com.gryphpoem.game.zw.resource.pojo;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.domain.s.StaticFunCard;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

import java.util.Calendar;

/**
 * @ClassName FunCard.java
 * @Description 功能卡
 * @author QiuKun
 * @date 2018年11月20日
 */
public class FunCard {
    public static final int[] CARD_TYPE = {0,1,2,3,4,5,6,7,8,9};

    private int type;// 对应 s_fun_card表的type字段
    private int remainCardDay; // 剩余天数 , 0表示没有剩余天数或没有购买永久卡, -10表示购买永久卡
    private int lastTime; // 最近一次发放月卡时间 调用的是 TimeHelper.getCurrentDay() 存储是这种20181111
    private int expire;//到期时间戳

    public void dser(CommonPb.FunCard ser) {
        this.remainCardDay = ser.getRemainCardDay();
        this.lastTime = ser.getLastTime();
        this.expire = ser.getExpire();
    }

    public CommonPb.FunCard ser() {
        CommonPb.FunCard.Builder builder = CommonPb.FunCard.newBuilder();
        builder.setType(this.type);
        builder.setRemainCardDay(this.remainCardDay);
        builder.setLastTime(this.lastTime);
        builder.setExpire(this.expire);
        return builder.build();
    }

    public FunCard(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getRemainCardDay() {
        return remainCardDay;
    }

    public void setRemainCardDay(int remainCardDay) {
        this.remainCardDay = remainCardDay;
    }

    public int getLastTime() {
        return lastTime;
    }

    public void setLastTime(int lastTime) {
        this.lastTime = lastTime;
    }

    public int incrRemainCardDay(int day) {
        if (day == StaticFunCard.FOREVER_DAY) {
            this.remainCardDay = StaticFunCard.FOREVER_DAY;
        } else if (day > 0 && remainCardDay != StaticFunCard.FOREVER_DAY) {
            this.remainCardDay += day;
        }
        return this.remainCardDay;
    }

    public void addExpireDay(int day){
        incrRemainCardDay(day);
        if(day < 0){
            this.expire = -1;
        }else if(day > 0) {
            if(expire > TimeHelper.getCurrentSecond()){
                expire += day * TimeHelper.DAY_S;
            }else {
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DAY_OF_MONTH,day);
                this.expire = (int) (c.getTimeInMillis()/1000L);
            }
        }else {}
    }

    public void addExpireSecond(int second){
        if(expire > TimeHelper.getCurrentSecond()){
            expire += second;
        }else {
            this.expire = TimeHelper.getCurrentSecond() + second;
        }
    }

    public void subRemainCardDay() {
        if (remainCardDay != StaticFunCard.FOREVER_DAY && remainCardDay > 0) {
            remainCardDay--;
        }
    }

    /**
     * 是否是终生卡
     * 
     * @return
     */
    public boolean isForeverCard() {
        return this.remainCardDay == StaticFunCard.FOREVER_DAY;
    }

    @Override
    public String toString() {
        return "FunCard [type=" + type + ", remainCardDay=" + remainCardDay + ", lastTime=" + lastTime + "]";
    }

    public int getExpire() {
        return expire;
    }

    public void setExpire(int expire) {
        this.expire = expire;
    }
}
