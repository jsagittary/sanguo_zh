package com.gryphpoem.game.zw.resource.domain.s;

/**
 * @ClassName StaticWorldRule.java
 * @Description 流寇和矿点刷新的时间
 * @author QiuKun
 * @date 2018年2月6日
 */
public class StaticWorldRule {
    private int id;
    private int type;// 类型, 1 流寇 , 2 矿点
    private int rule;// 规则序号
    private String cron;// cron表达式

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getRule() {
        return rule;
    }

    public void setRule(int rule) {
        this.rule = rule;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

}
