package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.pb.SerializePb;

/**
 * @author: ZhouJie
 * @date: Create in 2018-07-16 19:18
 * @description: 柏林会战记录
 * @modified By:
 */
public class BerlinRecord {

    private int killCnt;        // 总击杀兵力
    private int tempKill;       // 临时存储
    private int tempKillHero;   // 临时存储,将领击杀
    private int killStreakHero; // 连续击杀将领
    private int killStreak;     // 连续击杀
    private int killRankTime;   // 累积击杀上榜时间
    private int streakRankTime; // 连续击杀上榜时间
    private int exploit;        // 获取的军功

    public BerlinRecord() {
    }

    public BerlinRecord(SerializePb.SerBerlinRecord record) {
        this();
        this.killCnt = record.getKillCnt();
        this.tempKill = record.getTempKill();
        this.tempKillHero = record.getTempKillHero();
        this.killStreakHero = record.getKillStreakHero();
        this.killStreak = record.getKillStreak();
        this.killRankTime = record.getKillRankTime();
        this.streakRankTime = record.getStreakRankTime();
        this.exploit = record.getExploit();
    }

    public void addKillCnt(int hurt, int now) {
        this.killCnt += hurt;
        this.killRankTime = now;
    }

    public void addTempKill(int hurt, int now) {
        this.tempKill += hurt;
        this.tempKillHero++;
        if (this.tempKill > killStreak) {
            this.killStreak = tempKill;
        }
        if (this.tempKillHero > killStreakHero) {
            this.killStreakHero = tempKillHero;
            this.streakRankTime = now;
        }
    }

    public void clearTempKill() {
        this.tempKill = 0;
        this.tempKillHero = 0;
    }

    public int getKillStreakHero() {
        return killStreakHero;
    }

    public int getKillStreak() {
        return killStreak;
    }

    public int getKillCnt() {
        return killCnt;
    }

    public int getKillRankTime() {
        return killRankTime;
    }

    public int getStreakRankTime() {
        return streakRankTime;
    }

    public int getExploit() {
        return exploit;
    }

    public void addExploit(int add) {
        if (add > 0) {
            exploit += add;
        }
    }

    /**
     * 序列化
     * @param roleId
     * @return
     */
    public SerializePb.SerBerlinRecord ser(Long roleId) {
        SerializePb.SerBerlinRecord.Builder builder = SerializePb.SerBerlinRecord.newBuilder();
        builder.setKillCnt(getKillCnt());
        builder.setTempKill(this.tempKill);
        builder.setTempKillHero(this.tempKillHero);
        builder.setKillStreak(this.killStreak);
        builder.setKillStreakHero(this.killStreakHero);
        builder.setKillRankTime(this.killRankTime);
        builder.setStreakRankTime(this.streakRankTime);
        builder.setRoleId(roleId);
        builder.setExploit(this.exploit);
        return builder.build();
    }
}
