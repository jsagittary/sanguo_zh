package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticPartyJob.java
 * @Description 军团官职配置
 * @author TanDonghai
 * @date 创建时间：2017年4月26日 下午3:39:40
 *
 */
public class StaticPartyJob {
    private int job;
    private int maxNum;
    private List<Integer> privilege;
    private List<List<Integer>> val;

    public List<List<Integer>> getVal() {
        return val;
    }

    public void setVal(List<List<Integer>> val) {
        this.val = val;
    }

    public int getJob() {
        return job;
    }

    public void setJob(int job) {
        this.job = job;
    }

    public int getMaxNum() {
        return maxNum;
    }

    public void setMaxNum(int maxNum) {
        this.maxNum = maxNum;
    }

    public List<Integer> getPrivilege() {
        return privilege;
    }

    public void setPrivilege(List<Integer> privilege) {
        this.privilege = privilege;
    }

    @Override
    public String toString() {
        return "StaticPartyJob [job=" + job + ", maxNum=" + maxNum + ", privilege=" + privilege + "]";
    }
}
