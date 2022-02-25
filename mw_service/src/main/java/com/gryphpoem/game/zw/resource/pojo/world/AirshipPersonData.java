package com.gryphpoem.game.zw.resource.pojo.world;

import com.gryphpoem.game.zw.pb.SerializePb.DbAirshipPersonData;
import com.gryphpoem.game.zw.resource.constant.Constant;
import com.gryphpoem.game.zw.resource.pojo.AbsDailyClear;

import java.util.List;

/**
 * @ClassName AirshipPersonData.java
 * @Description 飞艇的个人数据
 * @author QiuKun
 * @date 2019年1月22日
 */
public class AirshipPersonData extends AbsDailyClear {

    /**
     * 每日击杀数量
     */
    private int killAwardCnt;
    /**
     * 每日参与数量
     */
    private int attendAwardCnt;

    /**
     * 反序列化
     * @param ser DB
     */
    public void dser(DbAirshipPersonData ser) {
        this.killAwardCnt = ser.getBelongAwardCnt();
        this.attendAwardCnt = ser.getHelpAwardCnt();
        this.lastRefreshDate = ser.getLastRefreshDate();
    }

    /**
     * 序列化
     * @return DB
     */
    public DbAirshipPersonData ser() {
        DbAirshipPersonData.Builder builder = DbAirshipPersonData.newBuilder();
        builder.setBelongAwardCnt(this.killAwardCnt);
        builder.setHelpAwardCnt(this.attendAwardCnt);
        builder.setLastRefreshDate(this.lastRefreshDate);
        return builder.build();
    }

    public AirshipPersonData() {
        dailyclearData();
    }

    @Override
    protected void dailyclearData() {
        List<Integer> cntCfg = Constant.AIRSHIP_CAN_AWARD_CNT;
        this.killAwardCnt = cntCfg.get(0);
        this.attendAwardCnt = cntCfg.get(1);
    }

    public void subKillAwardCnt(int cnt) {
        this.killAwardCnt -= cnt;
        if (this.killAwardCnt < 0) {
            this.killAwardCnt = 0;
        }
    }

    public void subAttendAwardCnt(int cnt) {
        this.attendAwardCnt -= cnt;
        if (this.attendAwardCnt < 0) {
            this.attendAwardCnt = 0;
        }
    }

    public int getKillAwardCnt() {
        return killAwardCnt;
    }

    public int getAttendAwardCnt() {
        return attendAwardCnt;
    }

    @Override
    public String toString() {
        return "AirshipPersonData [killAwardCnt=" + killAwardCnt + ", attendAwardCnt=" + attendAwardCnt + "]";
    }

}
