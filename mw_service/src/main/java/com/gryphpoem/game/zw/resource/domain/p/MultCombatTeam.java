package com.gryphpoem.game.zw.resource.domain.p;

import java.util.LinkedList;
import java.util.List;

/**
 * @ClassName MultCombatTeam.java
 * @Description 多人副本队伍信息
 * @author QiuKun
 * @date 2018年12月25日
 */
public class MultCombatTeam {
    private static int KEYID = 0;

    private int teamId;// 队伍id
    private int combatId;// 选择副本的id
    private long captainRoleId;// 队长的roleId
    private List<Long> teamMember = new LinkedList<>();// 队员信息其中包含队长信息
    private boolean autoStart;// 是否自动开始 (true表示勾选)
    private boolean autoJoin;// 是否允许自动加入(true表示勾选)

    public static MultCombatTeam createMultTeam(long captainRoleId, int combatId) {
        MultCombatTeam mct = new MultCombatTeam();
        mct.setCaptainRoleId(captainRoleId);
        mct.setCombatId(combatId);
        mct.setAutoStart(true);
        mct.setAutoJoin(true);
        mct.getTeamMember().add(captainRoleId);
        return mct;
    }

    private MultCombatTeam() {
        this.teamId = ++KEYID;
    }

    public int getTeamId() {
        return teamId;
    }

    public int getCombatId() {
        return combatId;
    }

    public void setCombatId(int combatId) {
        this.combatId = combatId;
    }

    public long getCaptainRoleId() {
        return captainRoleId;
    }

    public void setCaptainRoleId(long captainRoleId) {
        this.captainRoleId = captainRoleId;
    }

    public List<Long> getTeamMember() {
        return teamMember;
    }

    public void setTeamMember(List<Long> teamMember) {
        this.teamMember = teamMember;
    }

    public boolean isAutoStart() {
        return autoStart;
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }

    public boolean isAutoJoin() {
        return autoJoin;
    }

    public void setAutoJoin(boolean autoJoin) {
        this.autoJoin = autoJoin;
    }

    @Override
    public String toString() {
        return "MultCombatTeam [teamId=" + teamId + ", combatId=" + combatId + ", captainRoleId=" + captainRoleId
                + ", teamMember=" + teamMember + ", autoStart=" + autoStart + ", autoJoin=" + autoJoin + "]";
    }

}
