package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.pb.CommonPb;
import com.gryphpoem.game.zw.resource.util.TimeHelper;

/**
 * @ClassName RedPacketRole.java
 * @Description 领取红包玩家信息
 * @author QiuKun
 * @date 2018年6月8日
 */
public class RedPacketRole {
    private long roleId;
    private int time;// 领取的时间
    private String nick;// 玩家昵称
    private int awardId;// 领取红包奖励id对应(s_redpacket_list表id)
    private int msgId;// 留言id(s_red_packet_message表id)
    private int portrait = 1;// 头像(默认头像)

    public RedPacketRole(long roleId, String nick, int awardId, int msgId) {
        this.roleId = roleId;
        this.nick = nick;
        this.awardId = awardId;
        this.msgId = msgId;
        this.time = TimeHelper.getCurrentSecond();
    }

    public RedPacketRole(CommonPb.RedPacketRole ser) {
        this.roleId = ser.getRoleId();
        this.time = ser.getTime();
        this.nick = ser.getNick();
        this.awardId = ser.getAwardId();
        this.msgId = ser.getMsgId();
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public int getAwardId() {
        return awardId;
    }

    public void setAwardId(int awardId) {
        this.awardId = awardId;
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int msgId) {
        this.msgId = msgId;
    }

    public int getPortrait() {
        return portrait;
    }

    public void setPortrait(int portrait) {
        this.portrait = portrait;
    }

    @Override
    public String toString() {
        return "RedPacketRole [roleId=" + roleId + ", time=" + time + ", nick=" + nick + ", awardId=" + awardId
                + ", msgId=" + msgId + "]";
    }

}
