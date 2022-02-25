package com.gryphpoem.game.zw.resource.pojo.world;

import com.gryphpoem.game.zw.pb.CommonPb;

import java.util.HashSet;
import java.util.Set;

/**
 * @ClassName GlobalRebellion.java
 * @Description 匪军叛乱的数据
 * @author QiuKun
 * @date 2018年10月24日
 */
public class GlobalRebellion extends CycleData {

    private int curTemplate;// 本轮的模板

    private Set<Long> joinRoleId = new HashSet<>(); // 参加的玩家

    public GlobalRebellion() {
    }

    public void dser(CommonPb.GlobalRebellion ser) {
        super.openCnt = ser.getOpenCnt();
        super.curPreViewTime = ser.getCurPreViewTime();
        super.curRoundStartTime = ser.getCurRoundStartTime();
        super.curRoundEndTime = ser.getCurRoundEndTime();
        super.curEndTime = ser.getCurEndTime();

        this.curRound = ser.getCurRound();
        this.curTemplate = ser.getCurTemplate();
        this.nextRoundTime = ser.getNextRoundTime();
        for (Long roleId : ser.getJoinRoleIdList()) {
            joinRoleId.add(roleId);
        }
    }

    public CommonPb.GlobalRebellion ser(boolean isDb) {
        CommonPb.GlobalRebellion.Builder builder = CommonPb.GlobalRebellion.newBuilder();
        builder.setOpenCnt(super.openCnt);
        builder.setCurPreViewTime(super.curPreViewTime);
        builder.setCurRoundStartTime(super.curRoundStartTime);
        builder.setCurRoundEndTime(super.curRoundEndTime);
        builder.setCurEndTime(super.curEndTime);
        builder.setCurRound(this.curRound);
        builder.setCurTemplate(this.curTemplate);
        builder.setNextRoundTime(this.nextRoundTime);
        if (isDb) {
            for (long roleId : joinRoleId) {
                builder.addJoinRoleId(roleId);
            }
        }
        return builder.build();
    }

    // 重置本轮数据
    public void reset() {
        this.curTemplate = 0;
        super.reset();
        joinRoleId.clear();
    }

    public int incrOpenCnt() {
        return super.incrOpenCnt();
    }

    public int incrCurRound() {
        return ++curRound;
    }

    public int getCurTemplate() {
        return curTemplate;
    }

    public void setCurTemplate(int curTemplate) {
        this.curTemplate = curTemplate;
    }

    public Set<Long> getJoinRoleId() {
        return joinRoleId;
    }

    public void setJoinRoleId(Set<Long> joinRoleId) {
        this.joinRoleId = joinRoleId;
    }

    @Override
    public String toString() {
        return "GlobalRebellion [openCnt=" + openCnt + ", curPreViewTime=" + curPreViewTime + ", curRoundStartTime="
                + curRoundStartTime + ", curRoundEndTime=" + curRoundEndTime + ", curEndTime=" + curEndTime
                + ", curRound=" + curRound + ", curTemplate=" + curTemplate + ", nextRoundTime=" + nextRoundTime;
    }

}
