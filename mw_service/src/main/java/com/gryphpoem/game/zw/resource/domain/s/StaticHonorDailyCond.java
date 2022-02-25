package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticHonorDailyCond.java
 * @Description 荣耀日报触发条件表(其他类型)
 * @author QiuKun
 * @date 2018年8月28日
 */
public class StaticHonorDailyCond {
    private int cond;
    private List<Integer> param;

    public int getCond() {
        return cond;
    }

    public void setCond(int cond) {
        this.cond = cond;
    }

    public List<Integer> getParam() {
        return param;
    }

    public void setParam(List<Integer> param) {
        this.param = param;
    }

    @Override
    public String toString() {
        return "StaticHonorDailyCond [cond=" + cond + ", param=" + param + "]";
    }

}
