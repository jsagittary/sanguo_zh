package com.gryphpoem.game.zw.pojo.p;

/**
 * @author TanDonghai
 * @ClassName FightRecord.java
 * @Description 玩家在战争中的兵力和收获记录
 * @date 创建时间：2017年4月18日 上午9:43:41
 */
public class FightRecord {
    private long roleId;
    private boolean attacker;
    private int killed;// 杀敌数
    private int lost;// 损兵数
    private int exploit;// 获得军功

    public FightRecord(long roleId) {
        this.roleId = roleId;
    }

    public void addKilled(int add) {
        this.killed += add;
    }

    public void addLost(int add) {
        this.lost += add;
    }

    public void addExploit(int add) {
        this.exploit += add;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public boolean isAttacker() {
        return attacker;
    }

    public void setAttacker(boolean attacker) {
        this.attacker = attacker;
    }

    public int getKilled() {
        return killed;
    }

    public void setKilled(int killed) {
        this.killed = killed;
    }

    public int getLost() {
        return lost;
    }

    public void setLost(int lost) {
        this.lost = lost;
    }

    public int getExploit() {
        return exploit;
    }

    public void setExploit(int exploit) {
        this.exploit = exploit;
    }

    @Override
    public String toString() {
        return "FightRecord [roleId=" + roleId + ", attacker=" + attacker + ", killed=" + killed + ", lost=" + lost
                + ", exploit=" + exploit + "]";
    }
}
