package com.gryphpoem.game.zw.resource.pojo.party;

import com.gryphpoem.game.zw.pb.CommonPb;

/**
 * @ClassName PartyElection.java
 * @Description 军团选举信息
 * @author TanDonghai
 * @date 创建时间：2017年5月8日 下午5:39:07
 *
 */
public class PartyElection implements Comparable<PartyElection> {
    private long roleId;
    private String nick;
    /**
     * 等级
     */
    private int lv;
    /**
     * 战力
     */
    private long fight;
    /**
     * 票数
     */
    private int votes;
    /**
     * 军阶
     */
    private int ranks;

    public PartyElection() {
    }

    public PartyElection(long roleId, String nick, int lv, long fight, int votes, int ranks) {
        setRoleId(roleId);
        setNick(nick);
        setLv(lv);
        setFight(fight);
        setVotes(votes);
        setRanks(ranks);
    }

    public PartyElection(com.gryphpoem.game.zw.pb.CommonPb.PartyElection elect) {
        setRoleId(elect.getRoleId());
        setNick(elect.getName());
        setLv(elect.getLv());
        setFight(elect.getFight());
        setVotes(elect.getVotes());
        setRanks(elect.getRanks());
    }

    /**
     * 增加玩家得票数
     * 
     * @param add
     * @return
     */
    public int addVote(int add) {
        if (add > 0) {
            votes += add;
        }
        return votes;
    }

    public void cleanVote() {
        votes = 0;
    }

    public CommonPb.PartyElection ser() {
        CommonPb.PartyElection.Builder ser = CommonPb.PartyElection.newBuilder();
        ser.setRoleId(roleId);
        ser.setVotes(votes);
        ser.setName(nick);
        ser.setLv(lv);
        ser.setFight(fight);
        ser.setRanks(ranks);
        return ser.build();
    }

    public CommonPb.PartyElection ser(String nick, int lv, long fight, int ranks) {
        CommonPb.PartyElection.Builder ser = CommonPb.PartyElection.newBuilder();
        ser.setRoleId(roleId);
        ser.setVotes(votes);
        ser.setName(nick);
        ser.setLv(lv);
        ser.setFight(fight);
        ser.setRanks(ranks);
        setNick(nick);
        setLv(lv);
        setFight(fight);
        setRanks(ranks);
        return ser.build();
    }

    /**
     * 军团官员选举排序对比
     */
    @Override
    public int compareTo(PartyElection o) {
        // 判断顺序 票数 -> 等级 -> 战力 -> 军衔
        if (votes > o.getVotes()) {
            return -1;
        } else if (votes < o.getVotes()) {
            return 1;
        } else {
            if (lv > o.getLv()) {
                return -1;
            } else if (lv < o.getLv()) {
                return 1;
            } else {
                if (fight > o.getFight()) {
                    return -1;
                } else if (fight < o.getFight()) {
                    return 1;
                } else {
                    if (ranks > o.getRanks()) {
                        return -1;
                    } else if (ranks < o.getRanks()) {
                        return 1;
                    }
                }
            }
            //  之前的判断顺序 票数 -> 军衔 -> 战力 -> 等级
        /*if (votes > o.getVotes()) {
            return -1;
        } else if (votes < o.getVotes()) {
            return 1;
        } else {
            if (ranks > o.getRanks()) {
                return -1;
            } else if (ranks < o.getRanks()) {
                return 1;
            } else {
                if (fight > o.getFight()) {
                    return -1;
                } else if (fight < o.getFight()) {
                    return 1;
                } else {
                    if (lv > o.getLv()) {
                        return -1;
                    } else if (lv < o.getLv()) {
                        return 1;
                    }
                }
            }
        }*/
            return 0;
        }
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

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public long getFight() {
        return fight;
    }

    public void setFight(long fight) {
        this.fight = fight;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public int getRanks() {
        return ranks;
    }

    public void setRanks(int ranks) {
        this.ranks = ranks;
    }

    @Override
    public String toString() {
        return "PartyElection [roleId=" + roleId + ", nick=" + nick + ", lv=" + lv + ", fight=" + fight + ", votes="
                + votes + ", ranks=" + ranks + "]";
    }

}
