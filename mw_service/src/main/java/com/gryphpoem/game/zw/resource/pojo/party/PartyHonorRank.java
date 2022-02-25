package com.gryphpoem.game.zw.resource.pojo.party;

/**
 * @ClassName PartyHonorRank.java
 * @Description 军团荣誉排行信息
 * @author TanDonghai
 * @date 创建时间：2017年5月4日 下午5:03:37
 *
 */
public class PartyHonorRank implements Comparable<PartyHonorRank> {
    private long roleId;
    private int rankType;// 排行榜类型
    private int rank;
    private String nick;
    private int count;
    private int rankTime;// 上次更新时间

    public PartyHonorRank() {
    }

    public PartyHonorRank(com.gryphpoem.game.zw.pb.CommonPb.PartyHonorRank rank, int rankType) {
        setRoleId(rank.getRoleId());
        setRankType(rankType);
        setRank(rank.getRank());
        setNick(rank.getName());
        setCount(rank.getCount());
        setRankTime(rank.getRankTime());
    }

    public com.gryphpoem.game.zw.pb.CommonPb.PartyHonorRank ser() {
        com.gryphpoem.game.zw.pb.CommonPb.PartyHonorRank.Builder ser = com.gryphpoem.game.zw.pb.CommonPb.PartyHonorRank
                .newBuilder();
        ser.setRoleId(getRoleId());
        ser.setRank(getRank());
        ser.setName(getNick());
        ser.setCount(getCount());
        ser.setRankTime(getRankTime());
        return ser.build();
    }

    /**
     * 返回玩家次数对应的票数
     * 
     * @return
     */
    public int getVote() {
        return 0;
    }

    /**
     * 先按次数排序，次数多的在前面，相同时按更新时间爱你排序，先到达该次数的在前面, 玩家id小的在前面
     */
    @Override
    public int compareTo(PartyHonorRank o) {
        if (getCount() > o.getCount()) {
            return -1;
        } else if (getCount() < o.getCount()) {
            return 1;
        } else {
            if (getRankTime() < o.getRankTime()) {
                return -1;
            } else if (getRankTime() > o.getRankTime()) {
                return 1;
            } else {
                if (getRoleId() > o.getRoleId()) {
                    return 1;
                } else if (getRoleId() < o.getRoleId()) {
                    return -1;
                }
            }
        }
        return 0;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getRankType() {
        return rankType;
    }

    public void setRankType(int rankType) {
        this.rankType = rankType;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getRankTime() {
        return rankTime;
    }

    public void setRankTime(int rankTime) {
        this.rankTime = rankTime;
    }

    public void addCount(int count) {
        this.count += count;
    }

    @Override
    public String toString() {
        return "PartyHonorRank [roleId=" + roleId + ", rankType=" + rankType + ", rank=" + rank + ", nick=" + nick
                + ", count=" + count + ", rankTime=" + rankTime + "]";
    }
}
