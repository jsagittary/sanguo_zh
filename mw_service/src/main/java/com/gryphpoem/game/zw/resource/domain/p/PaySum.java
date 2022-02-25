package com.gryphpoem.game.zw.resource.domain.p;

/**
 * @ClassName PaySum.java
 * @Description 某个角色的充值金额总数
 * @author QiuKun
 * @date 2018年5月14日
 */
public class PaySum {
    private long roleId;
    private int sumAmoumt;

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getSumAmoumt() {
        return sumAmoumt;
    }

    public void setSumAmoumt(int sumAmoumt) {
        this.sumAmoumt = sumAmoumt;
    }

    @Override
    public String toString() {
        return "PaySum [roleId=" + roleId + ", sumAmoumt=" + sumAmoumt + "]";
    }

}
