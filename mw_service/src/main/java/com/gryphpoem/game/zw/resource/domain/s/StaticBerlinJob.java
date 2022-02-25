package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

import com.gryphpoem.game.zw.dataMgr.StaticBerlinWarDataMgr;

/**
 * @ClassName StaticBerlinJob.java
 * @Description 柏林会战官职
 * @author QiuKun
 * @date 2018年8月8日
 */
public class StaticBerlinJob {
    /** 司令部的官职id */
    public static final int BOSS_JOB_ID = 99;
    private int job;
    private List<Integer> buffId; // 拥有的buff

    public int getJob() {
        return job;
    }

    public void setJob(int job) {
        this.job = job;
    }

    public List<Integer> getBuffId() {
        return buffId;
    }

    public void setBuffId(List<Integer> buffId) {
        this.buffId = buffId;
    }

    /**
     * 获得该官职的buff加成值
     * 
     * @param buffType
     * @return
     */
    public int getBuffValByBuffType(int buffType) {
        int resVal = 0;
        for (int id : buffId) {
            StaticBerlinBuff sBuff = StaticBerlinWarDataMgr.getBerlinBuff().get(id);
            if (sBuff == null || sBuff.getType() != buffType) continue;
            resVal += sBuff.getBuffVal();
        }
        return resVal;
    }

    @Override
    public String toString() {
        return "StaticBerlinJob [job=" + job + ", buffId=" + buffId + "]";
    }

}
