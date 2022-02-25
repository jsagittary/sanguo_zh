package com.gryphpoem.game.zw.resource.domain.p;

import com.gryphpoem.game.zw.pb.CommonPb;

/**
 * @ClassName DbFriend.java
 * @Description 存储数据库好友对象
 * @author QiuKun
 * @date 2017年6月27日
 */
public class DbFriend {

    /**
     * 等待对方验证状态
     */
    public static final int STATE_WAIT_FRIEND_APPROVAL = 0;
    /**
     * 待验自己证状态
     */
    public static final int STATE_WAIT_SELF_APPROVAL = 1;
    /**
     * 双方已经是好友状态
     */
    public static final int STATE_FRIEND = 2;

    /**
     * 该玩家的师傅
     */
    public static final int STATE_MASTER = 3;

    /**
     * 是该玩家的徒弟
     */
    public static final int STATE_APPRENTICE = 4;

    /**
     * 临时好友
     */
    public static final int STATE_TMP = 5;

    private long lordId; // 角色id
    private int addTime; // 好友添加的时间
    private int state; // 好友状态 0 等待对方验证状态, 1 待验证状态, 2 双方已经是好友状态 3 师傅, 4 徒弟, 5 临时好友; 状态变化 0-->2;1-->2;2-->3;2-->4

    public DbFriend() {

    }

    public DbFriend(long lordId, int addTime, int state) {
        this.lordId = lordId;
        this.addTime = addTime;
        this.state = state;
    }

    public DbFriend(CommonPb.DbFriend pb) {
        setLordId(pb.getLordId());
        setAddTime(pb.getAddTime());
        setState(pb.getState());
    }

    public long getLordId() {
        return lordId;
    }

    public void setLordId(long lordId) {
        this.lordId = lordId;
    }

    public int getAddTime() {
        return addTime;
    }

    public void setAddTime(int addTime) {
        this.addTime = addTime;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "DbFriend [lordId=" + lordId + ", addTime=" + addTime + ", state=" + state + "]";
    }

}
