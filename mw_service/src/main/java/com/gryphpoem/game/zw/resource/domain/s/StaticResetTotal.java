package com.gryphpoem.game.zw.resource.domain.s;

import java.util.List;

/**
 * @ClassName StaticResetTotal.java
 * @Description 英雄总属性洗髓配置
 * @author QiuKun
 * @date 2017年7月3日
 */
public class StaticResetTotal {
    private int id;
    private int lowerlimit;// 总属性值下限
    private int upperlimit;// 总属性值上限
    private List<List<Integer>> free; // 免费洗髓(总属性) [[值,权重],[值,权重]]
    private List<List<Integer>> pay; // 付费洗髓(总属性) [[值,权重],[值,权重]]

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLowerlimit() {
        return lowerlimit;
    }

    public void setLowerlimit(int lowerlimit) {
        this.lowerlimit = lowerlimit;
    }

    public int getUpperlimit() {
        return upperlimit;
    }

    public void setUpperlimit(int upperlimit) {
        this.upperlimit = upperlimit;
    }

    public List<List<Integer>> getFree() {
        return free;
    }

    public void setFree(List<List<Integer>> free) {
        this.free = free;
    }

    public List<List<Integer>> getPay() {
        return pay;
    }

    public void setPay(List<List<Integer>> pay) {
        this.pay = pay;
    }

}
