package com.gryphpoem.game.zw.resource.pojo.party;

import com.gryphpoem.game.zw.pb.CommonPb.PartyJob;

/**
 * @ClassName Official.java
 * @Description 军团官员信息
 * @author TanDonghai
 * @date 创建时间：2017年4月25日 下午4:01:44
 *
 */
public class Official {
    private long roleId;
    private String nick;
    private int job;

    public Official() {
    }

    public Official(long roleId, String nick, int job) {
        setRoleId(roleId);
        setNick(nick);
        setJob(job);
    }

    public Official(PartyJob job) {
        setRoleId(job.getRoleId());
        setNick(job.getName());
        setJob(job.getJob());
    }

    public PartyJob ser() {
        PartyJob.Builder ser = PartyJob.newBuilder();
        ser.setRoleId(roleId);
        ser.setName(nick);
        ser.setJob(job);
        return ser.build();
    }

    public PartyJob build(int lv, long fight, int area, String nick, int portrait, int ranks, int curPortraitFrame) {
        PartyJob.Builder ser = PartyJob.newBuilder();
        ser.setRoleId(roleId);
        this.nick = nick;
        ser.setName(nick);
        ser.setJob(job);
        ser.setLv(lv);
        ser.setFight(fight);
        ser.setArea(area);
        ser.setPortrait(portrait);
        ser.setRanks(ranks);
        ser.setPortraitFrame(curPortraitFrame);
        return ser.build();
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public int getJob() {
        return job;
    }

    public void setJob(int job) {
        this.job = job;
    }

    @Override
    public String toString() {
        return "Official [roleId=" + roleId + ", nick=" + nick + ", job=" + job + "]";
    }
}
